# 📄 ChessWar Architecture Decision Records (ADR)

이 문서는 ChessWar 프로젝트의 핵심 아키텍처 의사결정과 기술적 타협의 근거를 기록하는 `SSOT`이자 과거 실패의 오답 노트이다.  
본 문서는 의사결정의 번호순이 아닌 개념적 위계 순서로 정렬되므로 번호가 순차적으로 배치되지 않는다.  
마인크래프트의 20 `TPS`라는 프로젝트의 성능적 제약과 기획적 유연성을 확보하기 위해 내린 굵직한 결단들을 보관한다.

---

## [Core Paradigm]

### ADR-001: Rich Simulation Model with Component Pattern

### Decision
깊은 클래스 상속 구조의 한계를 극복하기 위해 `Component` 패턴 기반의 조립형 객체 지향 설계를 채택한다.  
`Entity`는 자체 비즈니스 로직 없이 부착된 `Component`의 생명주기를 관리하고 전파하는 역할만 수행하며, `Component`는 특정 시뮬레이션 영역의 데이터와 그 데이터를 조작하는 고유 비즈니스 로직을 하나로 묶어 완벽히 캡슐화한다.  
`Manager`는 개별 `Component`의 내부 로직에 개입하지 않으며, 전역 상태 통제와 시뮬레이션 로직 간의 굵직한 이벤트 조율만을 전담한다.

### Rationale
마인크래프트의 20 `TPS` 루프 환경에서 유지보수성과 성능의 최적 균형을 찾기 위한 타협안이다.  
객체의 데이터와 행동을 결합하는 객체 지향의 본질을 지킴으로써 시뮬레이션 로직의 응집도를 높이고, 외부에서 상태를 임의로 조작하는 것을 막아 상태 불일치 버그를 원천 차단한다.  
`Manager`가 모든 로직을 독점하는 구조를 방지하여 확장이 유연해진다.

### 🛑 Anti-Patterns & Past Failures
* **DDD and Hexagonal Architecture Adoption**: 프로젝트 초기 엔터프라이즈 환경의 `DDD` 및 Hexagonal Architecture를 도입했으나, 물리 엔진의 매 `Tick` 객체 매핑 오버헤드가 서버 성능을 극도로 저하시켜 기각.
* **Pure ECS and State-Manager Decoupling**: 데이터와 로직을 완전히 분리하는 순수 `ECS` 사상을 도입했으나, 자바 힙 특성상 캐시 히트 이점이 없고 무분별한 상태 조작으로 강결합 절차적 코드가 양산되어 폐기.
* **Entity as God Object**: 체스판과 기물의 관계를 직역하여 체스판을 물리적 `Entity`로, 기물을 그 `Component`로 부착할 경우 단일 체스판 객체가 32개 기물의 상태와 생명주기를 모두 통제하는 God Object로 전락하므로 독립된 각 기물을 개별 `Entity`로 분리하여 캡슐화 보존.

---

### ADR-006: Pragmatic Law of Demeter in Game Architecture

### Decision
거시적 시스템 통제 영역과 `Cold-Path` 시뮬레이션 객체에서는 디미터 법칙을 엄격하게 준수한다.  
단, 게임 구조의 실용성을 위해 다음 5가지 특수 구역에서는 메서드 체이닝을 통한 디미터 법칙 위반을 합법적 패턴으로 전면 허용한다.  
(1) Entity-Component Boundary (2) `Hot-Path` (3) Data Structure (4) Math & Physics (5) External Engine API 영역이다.

### Rationale
웹 백엔드의 `Rich Domain Model`을 게임 환경에 맹목적으로 직역하여 `Entity`가 내부 `Component`의 메서드를 모두 감싸도록 강제할 경우, 무의미한 위임 메서드가 수십 개 양산되어 실질적 이득 없이 클래스의 극심한 비대화를 초래한다.  
최신 자바 JIT 컴파일러 환경에서 단순 위임에 의한 콜 스택 오버헤드는 미미할 수 있으나, 캡슐화라는 명목으로 발생하는 인지적 오버헤드는 심각하다.  
또한 게임 특유의 3D 수학 연산 시 자기 자신을 반환하여 덮어쓰는 체이닝 기법을 막으면 공식 형태의 코드 가독성이 심각하게 훼손된다.

### 🛑 Anti-Patterns & Past Failures
* **Blind Delegation in Entity**: 디미터 법칙 준수를 위해 `Entity`가 내부 `Component`의 모든 행동을 위임받아 노출한 결과, 수십 개의 래핑 메서드를 가진 `God Class`로 전락.

---

## [Performance Constraints]

### ADR-002: DOD-Inspired Hot-Path Optimizations

### Decision
이동, 충돌 판정, 파티클 연산 등 초당 20번 호출되어 `Tick` 성능에 직결되는 `Hot-Path`에 한해서만 객체 지향의 캡슐화와 방어적 프로그래밍을 제한적으로 포기하고 `DOD` 철학을 차용한다.  
새로운 객체 할당을 엄격히 금지하는 Object Pooling과, 반환 대신 In-place Mutation 방식을 강제한다.  
래퍼 클래스로 인한 오버헤드를 막기 위해 원시 타입 배열을 적극 활용하며, 분기 예측 방해를 막기 위해 널 반환 및 방어적 널 체크를 전면 금지하고 빈 구조체를 활용한다.  
이 영역에서 다량의 원시 데이터를 추출하고 집중적으로 연산하는 객체는 반드시 `Processor`라는 명칭을 부여한다.

### Rationale
마인크래프트 엔진 위에서 대규모 기물 전투를 구현하기 위한 생존 전략이다.  
자바의 `GC` 스파이크는 서버 지연의 주범이므로, 매 `Tick`마다 반복되는 무거운 연산 구간에서는 객체의 불변성과 캡슐화라는 미덕보다 메모리 할당 방어와 CPU 캐시 지역성 확보가 절대적으로 우선되어야 연산 지연을 막을 수 있다.  
방어적 널 체크로 인한 파이프라인 스톨을 막기 위해 무결성 데이터 유입을 강제하여 연산 중단을 차단한다.

### 🛑 Anti-Patterns & Past Failures
* **Immutable Operations in Hot-Path**: `Hot-Path` 내 불변성 유지를 위해 `Vector` 등의 객체를 매번 할당하는 것은 단명 객체를 폭증시켜 `Minor GC` 폭주 및 서버 `TPS` 붕괴를 유발하므로 절대 금기.
* **Defensive Null Checking in Hot-Path**: `NullPointerException`을 예방하기 위해 메인 `Tick` 루프 내부에 방어적 분기문을 삽입하는 것은 분기 예측을 지속적으로 붕괴시켜 `TPS` 저하로 직결되므로 절대 금기.
* **Premature DOD Optimization in Cold-Path**: 턴제 이동처럼 초당 수십 번 호출되지 않는 환경임에도 객체 할당을 막기 위해 In-place Mutation을 맹목적으로 강제하면 가독성과 불변성이 훼손되므로 `Cold-Path` 영역은 순수 불변 레코드 채택.
* **System Naming Confusion and Over-generalization**: 무상태 연산 객체를 `System`으로 통칭한 결과, 매 `Tick` 실행되는 물리 연산과 단발성 `Policy` 규칙이 동일한 역할군으로 혼동되어 `System` 명칭을 폐기하고 다량의 데이터를 일괄 연산하는 객체에만 `Processor` 명칭을 부여함으로써 최적화 초점 교정.
* **Over-categorization of Path and Data Lifecycle**: 성능 저하의 핵심은 `Tick` 같은 고빈도 이벤트의 집중 실행 여부이며 `Cold-Path`의 스파이크는 후행 프로파일링으로 해결할 문제이므로, 무의미한 인지적 오버헤드를 유발하는 데이터 관점 매트릭스는 전면 폐기.

---

### ADR-007: In-Memory Thin Event Architecture

### Decision
시스템 간 상태 변화 통지 시 자바 레코드 기반의 얇은 이벤트를 발행한다.  
고유 식별자 대신 메모리 참조 포인터를 직접 전달하며, 상태 갱신 직후 유실되는 1회성 변화량 정보만 객체에 담는 것을 예외적으로 허용한다.  
기존 상태를 이벤트 내부로 중복 복사하는 것은 엄격히 금지한다.

### Rationale
단일 인메모리를 공유하는 게임 서버에서는 웹 분산 환경처럼 데이터를 직렬화하여 전체를 전달할 필요가 없다.  
무거운 스냅샷 객체를 매번 할당하는 두꺼운 이벤트는 `GC` 스파이크를 유발한다.  
반대로 식별자만 전달할 경우 수신처마다 레지스트리를 탐색하는 오버헤드가 발생하므로, 객체 참조 자체를 던지는 것이 탐색 비용을 없애는 최적의 통신 방식이다.

### 🛑 Anti-Patterns & Past Failures
* **Fat Event State Snapshotting**: 이벤트 객체 내부에 갱신된 좌표 등을 깊은 복사하여 담아 보낼 경우 막대한 단명 객체 생성으로 메인 스레드 지연 유발.
* **ID-only Event Payload**: 메모리 참조 대신 무조건 고유 식별자만 전달하여, 이벤트 수신처마다 불필요한 맵 탐색 연산 강제.

---

### ADR-008: Segregation of Tickable and View Interfaces

### Decision
시청각 연출을 전담하는 객체는 순수 마커 용도의 `View` 인터페이스만을 기본적으로 구현한다.  
지속적인 시각적 보간 연산 등 메인 루프에서 집중적인 호출이 필수적인 객체에 한해서만 `Tickable` 인터페이스를 명시적으로 조립하여 구현한다.

### Rationale
매 `Tick`마다 상태 갱신이 불필요한 객체에게 빈 업데이트 메서드를 강제하는 것은 인터페이스 분리 원칙을 위반하는 설계이다.  
`Tickable` 인터페이스를 물리적으로 분리하면 오케스트레이터가 실제 연산 타겟만 필터링하여 순회할 수 있어 전체 루프 성능이 극대화된다.

### 🛑 Anti-Patterns & Past Failures
* **God View Interface**: 단일 `View` 인터페이스 내부에 루프 진입점을 강제하여, 정적 시각 객체들에게 빈 구현체 작성을 강요하고 무의미한 순회 연산 유발.
* **Leaking This on View Creation**: `View` 생성자 내부에서 이벤트 버스를 구독하여, 객체가 완전히 초기화되기 전에 콜백이 실행되어 NPE 및 상태 불일치를 유발하는 불완전 객체 노출 안티패턴.

---

## [External Boundaries & Communication]

### ADR-003: Engine Object Isolation and Dumb View Principle

### Decision
엔진이 생명주기를 관리하는 무거운 객체를 시뮬레이션 영역의 멤버 변수로 영구 저장하는 것을 절대 금지하며, 고유 식별자나 순수 데이터로 변환하여 보관한다.  
모든 시청각 부수 효과는 단방향 읽기 전용 모듈인 `View`로 완전히 격리한다.  
`View`는 넘겨받은 좌표에 어떠한 기하학적 오프셋도 임의로 더하거나 빼서는 안 된다는 Dumb View 원칙을 지켜야 하며, 메인 로직의 오류를 몰래 수습하는 방어 코드를 내포해서도 안 된다.

### Rationale
핵심 게임 로직을 외부 엔진의 종속성으로부터 완벽히 보호하기 위함이다.  
물리 엔진과 시뮬레이션 로직의 생명주기가 어긋날 때 발생하는 치명적인 메모리 누수와 데싱크를 방지할 수 있다.  
또한 시각 연출 책임을 완전히 분리함으로써 렌더링 방식이 변경되어도 코어 전투 로직이 오염되는 것을 차단한다.

### 🛑 Anti-Patterns & Past Failures
* **Engine Item API as Source of Truth**: 피아 식별이나 능력치를 엔진의 메타데이터에 저장하는 것은 시뮬레이션 데이터 영구 유실을 유발하므로 절대 금기.
* **Visual Offset Calculation by View**: 로직 오염 방지를 위해 렌더러가 자체적으로 위치 오프셋을 더하도록 설계한 결과, 좌표계가 이중 왜곡되는 버그 발생.

---

### ADR-009: Engine Lifecycle Late Binding

### Decision
플러그인의 생명주기에 종속되어 런타임 명령어를 통해 뒤늦게 기준점이 셋업되어야 하는 전역 인프라 객체에 한해, 멤버 변수의 불변성 강제를 예외적으로 해제하고 지연 초기화를 허용한다.  
이 경우 접근 전 검증을 위한 런타임 안전망을 반드시 제공한다.

### Rationale
마인크래프트 플러그인은 서버 시작 시점에 모든 인게임 의존성을 완성할 수 없다.  
이론적 무결성을 지키기 위해 불변성을 억지로 강제하면, 실질적인 이득 없이 상태를 라우팅하기 위한 거대한 컨테이너 객체들을 남발하는 극심한 오버엔지니어링이 필연적으로 발생한다.  
빠른 생존과 검증이 최우선이라는 1원칙에 따라 실용적인 지연 초기화를 채택한다.

### 🛑 Anti-Patterns & Past Failures
* **Over-engineered Session Routing**: 불변성 규칙을 맹목적으로 지키기 위해 불필요하게 복잡한 상태 주입 객체를 양산하여 개발 생산성 저하.

---

### ADR-005: Immediate Direct Call for Input Handling

### Decision
입력 컨트롤러가 외부 입력을 시뮬레이션 영역으로 전달할 때 이벤트 버스나 커맨드 큐를 거치지 않고 순수 데이터 객체를 생성하여 진입점 메서드를 직접 동기적으로 호출한다.

### Rationale
마인크래프트는 `Tick`당 50ms라는 매우 긴 시간 간격을 가지므로 입력을 큐에 버퍼링하여 다음 `Tick`에 처리할 경우 극심한 입력 지연이 발생해 조작감이 훼손된다.  
기획이 턴제 기반이므로 동시성 충돌을 막기 위해 입력을 대기시키는 구조는 불필요하다.  
동일 프레임 내에서 즉각 검증하고 피드백을 반환하는 직접 호출 방식이 응답성을 확보하기 위한 최적의 통신 방식이다.

### 🛑 Anti-Patterns & Past Failures
* **Input Queue Buffering**: 유저 입력을 큐에 담아 다음 `Tick`에 처리하는 방식을 고려했으나, 50ms `Tick` 제약으로 인해 극심한 입력 지연이 필연적으로 예상되어 도입 전 폐기.
* **Event-Driven Input Handling**: 사용자 입력을 내부 이벤트 버스에 태워 처리할 경우, 콜스택이 끊어져 입력 검증 실패 시 동기적 피드백 처리가 불가능해지므로 도입 검토 단계에서 기각.

---

## [Physical Structure]

### ADR-004: Feature-Driven Hybrid Packaging

### Decision
기술 계층 중심이 아닌 특정 `Feature` 단위로 패키징 전략을 채택한다.  
`Feature` 설계 초기에는 모든 클래스를 하나의 패키지 내부에 평탄하게 배치한다.  
`Feature`가 팽창하여 하위 패키지로 분할해야 하는 임계치에 도달하면, 엔진 입출력 모듈만 하위 패키지로 밀어내고 핵심 상태를 가진 객체군은 동일 패키지에 남겨 자바의 접근 제어자로 은닉성을 수호한다.

### Rationale
패키지를 분리하면 서로 참조하기 위해 상태 변이 메서드를 개방해야 하며 이는 캡슐화 붕괴를 초래한다.  
초기부터 폴더를 엄격하게 나누는 오버엔지니어링은 개발 속도를 늦추므로, 실용적인 평탄화 구조로 출발하되 필요 시 방어벽을 유지할 수 있는 하이브리드 확장 전략을 사용한다.

### 🛑 Anti-Patterns & Past Failures
* **Strict Hexagonal Directory Separation**: 초기부터 `Feature`별로 엄격하게 하위 패키지를 분리한 결과 내부 상태 갱신 메서드가 개방되는 주객전도 발생 및 잦은 디렉토리 이동으로 생산성 저하.

---

### ADR-010: Command Routing via Abstract Base Class

### Decision
명령어 라우팅 구현 시 단일 커맨드 라우터에서 외부 입력 텍스트를 수동으로 분기 처리하는 방식을 전면 폐기한다.  
대신 공통 베이스 클래스를 신설하여 각 `Feature` 패키지의 커맨드 컨트롤러가 이를 상속받아 라우팅과 메시징 책임을 분산하도록 설계한다.

### Rationale
수동 분기 방식은 모듈 간 응집도를 떨어뜨리고 아키텍처의 패키징 규칙을 위배한다.  
베이스 클래스 상속을 통해 명령어 메타데이터와 공통 메시징 책임을 재사용하면, `SSOT`를 유지하면서도 완전히 독립된 `Feature` 커맨드 확장이 가능해진다.

### 🛑 Anti-Patterns & Past Failures
* **Monolithic Command Router**: 단일 라우터 객체가 최상단 입력 인자를 분석하여 모든 `Feature` 영역의 비즈니스를 강제로 분기 처리하는 강결합 양산.
* **Hardcoded Message Prefix**: 개별 커맨드 모듈마다 플러그인 접두사를 직접 하드코딩하여 메시징 정책 변경 시 유지보수 비용 폭증 유발.
