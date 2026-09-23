# 📄 ChessWar Architecture Decision Records (ADR)

이 문서는 ChessWar 프로젝트의 핵심 아키텍처 의사결정과 기술적 타협의 근거를 기록하는 `SSOT`이자 과거 실패의 오답 노트이다.  
마인크래프트의 20 `TPS`라는 프로젝트의 성능적 제약과 기획적 유연성을 확보하기 위해 내린 굵직한 결단들을 보관한다.

---

### ADR-001: Rich Domain Model with Component Pattern

### Decision
깊은 클래스 상속 구조의 한계를 극복하기 위해 `Component` 패턴 기반의 조립형 객체 지향 설계를 채택한다.  
`Entity`는 자체 비즈니스 로직 없이 부착된 `Component`의 생명주기를 관리하고 전파하는 역할만 수행하며, `Component`는 특정 도메인의 데이터와 그 데이터를 조작하는 고유 비즈니스 로직을 하나로 묶어 완벽히 캡슐화한다.
`Manager`는 개별 `Component`의 내부 로직에 개입하지 않으며, 전역 상태 통제와 도메인 간의 굵직한 이벤트 조율만을 전담한다.

### Rationale
마인크래프트의 20 `TPS` 루프 환경에서 유지보수성과 성능의 최적 균형을 찾기 위한 타협안이다.  
객체의 데이터와 행동을 결합하는 객체 지향의 본질을 지킴으로써 도메인의 응집도를 높이고, 외부에서 상태를 임의로 조작하는 것을 막아 상태 불일치 버그를 원천 차단한다.  
`Manager`가 모든 로직을 독점하는 구조를 방지하여 확장이 유연해진다.

### 🛑 Anti-Patterns & Past Failures
* **DDD and Hexagonal Architecture Adoption**: 프로젝트 초기 엔터프라이즈 환경의 `DDD` 및 헥사고날 아키텍처를 도입했으나, 물리 엔진의 매 `Tick` 객체 매핑 오버헤드가 서버 성능을 극도로 저하시켜 기각되었다.
* **Pure ECS and State-Manager Decoupling**: 데이터와 로직을 완전히 분리하는 순수 `ECS` 사상을 도입했으나, 자바 힙 특성상 캐시 히트 이점이 없고 무분별한 상태 조작으로 강결합 절차적 코드가 양산되어 폐기되었다.

---

### ADR-002: DOD-Inspired Hot-Path Optimizations

### Decision
이동, 충돌 판정, 파티클 연산 등 초당 20번 호출되어 `Tick` 성능에 직결되는 `Hot-Path`에 한해서만 객체 지향의 캡슐화를 제한적으로 포기하고 `DOD` 철학을 차용한다.  
새로운 객체 할당을 엄격히 금지하는 `Object Pooling`과, 반환 대신 기존 인스턴스 내부 필드를 직접 덮어쓰는 `In-place Mutation`을 강제한다. 래퍼 클래스로 인한 오버헤드를 막기 위해 원시 타입 배열을 적극 활용한다.
이 영역에서 다량의 원시 데이터를 추출하고 집중적으로 연산하는 객체는 반드시 `Processor`라는 명칭을 부여한다.

### Rationale
마인크래프트 엔진 위에서 대규모 기물 전투를 구현하기 위한 생존 전략이다. 자바의 `GC` 스파이크는 서버 지연의 주범이므로, `Tick`마다 반복되는 무거운 연산 구간에서는 객체의 불변성과 캡슐화라는 객체 지향의 미덕보다 메모리 할당 방어와 CPU 캐시 지역성 확보가 절대적으로 우선되어야 한다.

### 🛑 Anti-Patterns & Past Failures
* **Immutable Operations in Hot-Path**: `Hot-Path` 내 불변성 유지를 위해 `Vector` 등의 객체를 매번 할당하는 것은 단명 객체를 폭증시켜 `Minor GC` 폭주 및 서버 `TPS` 붕괴를 유발하므로 절대 금기이다.
* **System Naming Confusion and Over-generalization**: 무상태 연산 객체를 `System`으로 통칭한 결과, 매 `Tick` 실행되는 물리 연산과 단발성 `Policy` 규칙이 동일 계층으로 혼동되었다. 이에 `System` 명칭을 폐기하고 다량의 데이터를 일괄 연산하는 객체에만 `Processor` 명칭을 부여하여 최적화 초점을 명확히 했다.
* **Over-categorization of Path and Data Lifecycle**: `Hot/Cold-Path`와 `Hot/Warm/Cold-Data`를 교차시킨 정밀한 매트릭스를 고안했으나, 성능 저하의 핵심은 `Tick`이나 `PlayerMoveEvent` 같은 고빈도 이벤트의 집중 실행 여부이다. `Cold-Path`의 대규모 객체 생성으로 인한 스파이크는 데이터 성질 분류가 아닌 후행 프로파일링으로 해결할 문제이므로, 무의미한 인지적 오버헤드를 유발하는 데이터 관점 매트릭스는 전면 폐기되었다.

---

### ADR-003: Engine Object Isolation and Dumb View Principle

### Decision
`Player`, `World`, `Block` 등 `Bukkit` 엔진이 생명주기를 관리하는 무거운 객체를 시뮬레이션 영역의 멤버 변수로 영구 저장하는 것을 절대 금지하며, 고유 식별자인 `UUID`나 순수 `Vector` 데이터로 변환하여 보관한다.  
파티클, 사운드 등 모든 시청각 부수 효과는 단방향 읽기 전용 모듈인 `View`로 완전히 격리한다. `View`는 넘겨받은 좌표에 어떠한 기하학적 오프셋도 임의로 더하거나 빼서는 안 된다는 `Dumb View` 원칙을 지켜야 하며, 메인 로직의 오류를 몰래 수습하는 방어 코드를 내포해서도 안 된다.

### Rationale
핵심 게임 로직을 외부 엔진의 종속성으로부터 완벽히 보호하기 위함이다. 물리 엔진과 도메인의 생명주기가 어긋날 때 발생하는 치명적인 메모리 누수와 `Desync` 현상을 막을 수 있다. 또한 시각 연출 책임을 완전히 분리함으로써 렌더링 방식이 변경되어도 코어 전투 로직이 오염되는 것을 원천 차단한다.

### 🛑 Anti-Patterns & Past Failures
* **Engine Item API as Source of Truth**: 피아 식별이나 능력치를 엔진의 아이템 메타데이터나 스코어보드 API에 저장하는 것은 이벤트 발생 시 도메인 데이터가 영구 유실되므로 절대 사용해서는 안 된다.
* **Visual Offset Calculation by View**: 로직 오염 방지를 위해 렌더러가 자체적으로 Y축 오프셋을 더하도록 설계한 결과, 타 시스템의 정밀한 좌표 조작과 중첩되어 위치가 이중 왜곡되는 `Desync` 버그가 발생했다.

---

### ADR-004: Feature-Driven Hybrid Packaging

### Decision
기술 계층 중심이 아닌 게임의 특정 기능 묶음인 `Feature` 단위로 패키징 전략을 채택한다.  
새로운 기능 설계 시에는 `Start Flat, Grow Deep` 원칙에 따라, 초기에는 모든 클래스를 하나의 `Feature` 패키지 내부에 평탄하게 배치한다. 기능이 팽창하여 하위 패키지로 분할해야 하는 임계치에 도달하면, `View`나 `Controller` 같은 엔진 입출력 계층만 하위 패키지로 밀어내고 핵심 상태를 가진 객체군은 동일 패키지에 남겨 자바의 `package-private` 접근 제어자로 은닉성을 수호한다.

### Rationale
자바의 패키지 특성상 폴더를 분리하면 서로 참조하기 위해 클래스의 상태 변이 메서드를 `public`으로 열어두어야 하며 이는 캡슐화의 붕괴를 초래한다. 초기부터 무조건 폴더를 엄격하게 나누는 오버엔지니어링은 기획 변경이 잦은 게임 개발 속도를 늦추므로, 초기에는 실용적인 `Flat` 구조로 출발하되 필요 시 캡슐화 방어벽을 유지할 수 있는 하이브리드 확장 전략을 사용한다.

### 🛑 Anti-Patterns & Past Failures
* **Strict Hexagonal Directory Separation**: 초기부터 기능별로 엄격하게 하위 패키지를 분리한 결과, 캡슐화가 붕괴되어 내부 객체의 상태 갱신 메서드가 `public`으로 열리는 주객전도가 발생했고 잦은 디렉토리 이동으로 생산성이 심각하게 저하되었다.
