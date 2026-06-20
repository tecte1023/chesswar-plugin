# 📄 ChessWar Architecture Decision Records (ADR)

이 문서는 ChessWar 개발 과정에서 결정된 구체적인 기술적 선택, `Domain` 수준의 설계, 그리고 성능 최적화 의사결정의 역사적 기록(판례)을 보관합니다.

## Architectural Background

본 프로젝트는 약 1년간의 개발 과정을 거치며 아키텍처의 근본적인 전환인 `Soft Reset`을 단행했습니다.

초기에는 엔터프라이즈 환경의 `DDD` 및 헥사고날 아키텍처를 채택하였으나, 마인크래프트의 물리적 `Tick Engine` 환경에서는 이러한 추상화가 한계에 부딪혔습니다. 마인크래프트 서버의 20 TPS 한계를 방어하면서도 유지보수성을 확보하기 위해, 기획 중심의 `GDD` 용어를 수용하는 방향으로 구조를 전면 개편했습니다. 아래 기록들은 이 `Soft Reset` 과정에서 수립된 핵심 아키텍처와 세부 구현 타협안의 근거들입니다.

---

## ADR-001: Component-Based OOP Adoption

### Context
`Soft Reset`을 단행하며 깊은 `Class` 상속 구조의 결합도 문제를 해결하고 성능을 최적화하기 위해, `DDD`, `DOD`(Data-Oriented Design), 순수 `ECS`(Entity Component System) 등의 아키텍처 도입을 다각도로 검토했습니다.

### Decision
`DDD`, `DOD`, 순수 `ECS`를 모두 기각하고, 식별자만 가진 깡통 객체에 플랫한 `Component`를 결합하여 사용하는 `Component-Based OOP` 패턴을 최종 채택합니다.

### Rationale
각 대안을 기각하고 `Component` 조립 방식을 채택한 기술적 이유는 다음과 같습니다.
1. **DDD 및 헥사고날 기각**: 게임 루프(`Tick`) 내에서 발생하는 과도한 객체 매핑 `Overhead`가 개발 속도와 서버 성능을 심각하게 저하시킵니다.
2. **DOD 및 순수 ECS 기각**: 메모리 연속성이 보장되는 `C++`과 달리, `Java` 객체는 힙 영역의 메모리 파편화 특성상 데이터 지향 설계가 주는 CPU 캐시 히트 최적화의 이점을 얻기 불가능합니다.
3. **결론**: 따라서 자바 생태계의 물리적 한계를 인정하고, 다중 상속 불가 문제를 회피하면서도 유지보수성과 성능의 최적 균형을 낼 수 있는 `Component-Based OOP` 방식을 채택했습니다.

---

## ADR-002: State-Manager Decoupling

### Context
상태 `Class`가 `Manager`를 참조하거나 둘 사이에 순환 의존성이 발생하면, 상태 전이의 추적이 어려워지고 동시 수정 예외나 `Memory Leak` 버그로 이어집니다.

### Decision
상태 객체를 순수 `Data Container`로 철저히 고립합니다. `Domain` 상태 객체 내부에 `Business Logic`이나 복잡한 상태 전이 규칙을 작성하지 않으며, `Manager`는 상태 객체를 파라미터로 전달받거나 단방향으로 주입받아 제어합니다.

### Rationale
객체가 서로를 역참조하는 순환 의존성을 제거하여 비동기 `Tick Loop`에서의 `Runtime Error`를 원천 차단하고 구조적 복잡도를 소거하기 위해 단방향 데이터 흐름을 강제합니다.

---

## ADR-003: Presentation-Domain Separation

### Context
마인크래프트 엔진 특성상 `Piece`나 `ChessBoard`는 공간 좌표계인 `Location`과 결합되어야 합니다. 그러나 전투 공식 등 서버 측 논리 연산 중에 `Sound` 발송, 대량의 `Particle` 생성 로직이 강결합되면, 시청각 연출 부하가 메인 `Tick` 연산 속도에 직접 영향을 주어 `Lag`을 유발합니다.

### Decision
`Location`과 `Vector` 같은 수학/공간 객체는 `Domain`에서 1급 객체로 허용하되, 시청각 연출 계층(`Presentation`)은 물리적으로 완전히 격리합니다. `Manager` 계층은 도메인 논리만 처리하고 `Presentation Delegate` 인터페이스를 호출해 연출을 위임합니다.

### Rationale
순수 자바 객체로의 변환 `Overhead`를 줄이는 실용성은 챙기면서, `Particle` 연산 등 시각적 처리 부하를 `Tick Loop`에서 분리하여 서버 성능을 방어하고 연출 튜닝의 반복 작업 속도를 극대화합니다.

---

## ADR-004: 2D Array Spatial Grid vs HashMap

### Context
8x8 체스판과 같이 고정된 공간에서 기물의 위치를 탐색할 때, `Coordinate`를 키로 하는 `HashMap<Coordinate, Piece>` 구조를 사용하면 매 탐색마다 해시 계산 비용과 객체 박싱 `Overhead`가 발생합니다.

### Decision
해시 기반의 `Map` 구조를 기각하고, `Piece[][]` 형태의 다차원 배열 구조를 메인 공간 도메인 저장소로 채택합니다.

### Rationale
고정 크기의 격자 공간 탐색에서는 배열의 인덱스 기반 `O(1)` 조회가 압도적으로 빠르며, 메모리 연속성 및 CPU 캐시 히트율을 극대화하여 틱당 탐색 연산 비용을 획기적으로 낮출 수 있습니다.

---

## ADR-005: Primitive Type Enforcement for GC Defense

### Context
자바 환경에서 `Integer`나 `Double`과 같은 래퍼 클래스를 `Tick Loop` 내부에서 무분별하게 사용하면 매 연산마다 오토박싱 및 언박싱이 발생하여 엄청난 양의 `Garbage`를 생성합니다.

### Decision
매 틱마다 연산되는 `Component` 내부의 숫자 데이터나 공간 수학 계산을 담당하는 `Manager`에서는 래퍼 클래스 및 내부적으로 박싱을 유발하는 `List<Integer>` 사용을 금지하고, 반드시 `int`, `double` 등의 원시 타입 및 원시 타입 배열(`int[]`)을 사용합니다.

### Rationale
잦은 객체 할당으로 인한 `Stop-the-world` 현상을 원천적으로 방어하기 위해, 아키텍처 수준에서 원시 타입 사용을 강제합니다.

---

## ADR-006: Hot Path Mutability & Component In-Place Update

### Context
순수 객체 지향 및 일반적인 불변성 원칙을 따르면 상태 변경 시 새로운 객체를 반환해야 하지만, 매 틱마다 지속시간이 차감되는 `Status Effect`나 파티클 좌표의 경우 틱마다 새 객체를 반환하면 힙 메모리가 급격히 팽창합니다.

### Decision
`Warm Path`(턴 전환 등 중빈도)에서는 불변성을 유지하고 객체를 교체하지만, 매 틱 갱신되는 `Hot Path`에서는 예외적으로 객체 생성을 금지하고 내부 `Field`를 직접 수정(`Mutation`)하는 것을 강제합니다. 이를 위해 `Piece` 등 상태 객체 내부에 자신을 제어하는 인플레이스 갱신용 헬퍼 메서드를 허용합니다.

### Rationale
마인크래프트 싱글 스레드 환경에서 20 TPS를 사수하기 위해 이상적인 불변성을 포기하고 실리적인 객체 재사용성(성능 최적화)을 택합니다.

---

## ADR-007: Polling vs Event-Driven Communication

### Context
매니저 간 통신 결합도를 낮추기 위해 전면적인 이벤트 기반(`Event-Driven`) 구조를 도입할 경우, 초당 수백 번 발생하는 틱 갱신 로직까지 이벤트를 발행하게 되어 이벤트 객체 생성 부하가 치명적 수준에 이릅니다.

### Decision
데이터의 성격에 따라 통신 프로토콜을 분리합니다. 지속 데미지 등 연속적 감시(`Continuous`)가 필요한 로직은 매니저가 직접 상태를 읽어오는 `Polling` 방식을 사용하고, 기물 파괴나 턴 종료 같은 이산적 전이(`Discrete`) 로직에만 가벼운 도메인 이벤트를 발행하여 결합도를 끊습니다.

### Rationale
결합도 완화라는 객체지향적 이점과 틱 성능 방어라는 물리적 한계 사이에서 균형을 맞추기 위한 필수적인 프로토콜 타협입니다.

---

## ADR-008: Role-Based Constructor Design

### Context
저장소 복구가 필요한 `Piece`와 수학적 고정값을 가지는 `ChessBoard`, 빈 상태로 시작하는 `BoardState` 등 도메인 객체들의 초기화 요구사항이 판이하게 다릅니다.

### Decision
역할에 따라 생성자를 다르게 강제합니다. `Piece` 등 `Warm Path` 객체는 `@AllArgsConstructor`만 열어두어 쓰레기 객체 생성을 막고, 공간 기하를 제어하는 객체는 생성자 내부에서 관계식을 직접 계산하여 캡슐화를 보장합니다.

### Rationale
선언부 초기화 후 주입값으로 덮어쓰는 방식은 최초 생성된 인스턴스를 즉각적인 가비지로 만들어버리므로, 생성 단계에서부터 객체 역할에 맞는 1:1 대입 방식을 규격화합니다.

---

## ADR-009: Null Safety and Layered Defense

### Context
서버 `Thread`는 트랜잭션 롤백을 지원하지 않아, 비즈니스 로직 실행 도중 예외가 터지면 가상 재화 불일치 문제가 일어납니다. 반면 매번 코어 로직에서 검증을 거치면 연산 지연이 발생합니다.

### Decision
명령어나 외부 이벤트가 들어오는 최외곽 경계 계층(`Input Controller`)에서 철저한 검증 및 조기 종료를 수행하고, 코어 `Domain` 계층에서는 정적 분석에만 의존하여 런타임 체크를 과감히 생략합니다.

### Rationale
보안 경계와 속도 경계를 분리하여, 코어 로직의 불필요한 조건문 순회 오버헤드를 줄입니다.

---

## ADR-010: Bunker System Adoption

### Context
전투 개시 시점에 마인크래프트 월드 내에 몹 엔티티를 대량으로 실시간 `Spawn`하면 메인 `Thread` 병목이 유발되어 시각적 끊김이 발생합니다.

### Decision
준비 단계에서 필요한 엔티티를 보이지 않는 격리 구역에 미리 `Spawn` 해두고, 필요 시 위치 이동(`Teleport`)만으로 필드에 배치하는 벙커 시스템을 적용합니다.

### Rationale
서버 병목을 준비 시간으로 분산시켜 플레이어 경험을 쾌적하게 유지하기 위한 실전 최적화 기법입니다.

---

## ADR-011: Event-Based Custom Damage Control

### Context
마인크래프트 바닐라의 데미지 판정 로직은 아군 보호나 페이즈 무적 같은 기획의 세밀한 요구사항을 완벽히 수용하지 못합니다.

### Decision
서버 엔진의 기본 `Damage Event`를 최외곽에서 가로채어 강제 취소(`Cancel`)시키고, 독자적으로 계산된 커스텀 데미지만을 대상 엔티티에 원자적으로 적용합니다.

### Rationale
게임의 밸런스와 전투 공식을 바닐라 엔진의 불확실성으로부터 독립시켜 룰의 일관성을 100% 통제합니다.

---

## ADR-012: Feature-Driven Packaging & Hybrid Sub-packaging Strategy

### Context
`Layer-driven` 패키징의 파편화 문제를 해결하기 위해 `Feature-driven` 패키징을 도입했습니다. 그러나 도메인 파일이 많아져 역할별 하위 패키지로 분할할 경우, `Java` 생태계의 특성상 `package-private` 접근 제어자를 사용할 수 없어 상태 객체의 가변 메서드를 `public`으로 열어야 하는 캡슐화 붕괴 문제가 발생합니다.
반대로 캡슐화 방어를 위해 기능의 초기 단계부터 무조건 `I/O` 패키지를 분리하여 강제 격리하는 방안도 검토했으나, 이는 잦은 디렉토리 이동과 불필요한 `Boilerplate`를 유발하여 빠른 `Iteration`을 저해한다는 딜레마가 있었습니다.

### Decision
`Feature-driven` 패키징을 유지하되, `Start Flat, Grow Deep` 철학 기반의 하이브리드 확장 전략을 채택합니다.
1. **Flat Initial Phase**: 파일 수가 적은 초기에는 패키지 격리를 통한 컴파일러 방어를 유보하고, 한 폴더에 모든 클래스를 모아 개발 속도를 극대화합니다. `I/O` 객체의 상태 조작 통제는 코드 리뷰 규약으로 방어합니다.
2. **Hybrid Expansion**: 파일 수가 임계치를 넘어가면 디렉토리를 분할합니다. 단, `Entity`, `Component`, `Manager`는 동일 부모 패키지에 고립시켜 `package-private` 방어선을 유지하고, 파일 개수가 팽창하는 `Input Controller`와 `Presentation Delegate` 계층만 하위 패키지로 물리적으로 밀어냅니다.

### Rationale
초기 구조의 `Over-engineering`보다는 마인크래프트 플러그인 생태계에 맞는 빠른 개발 속도를 우선시한 결정입니다. 동시에 프로젝트가 거대해져 패키지를 분리해야 하는 시점이 오더라도 객체지향의 가장 중요한 원칙인 상태 캡슐화를 절대 포기하지 않도록 `Java` 최적의 방어 구조를 내재화했습니다.
