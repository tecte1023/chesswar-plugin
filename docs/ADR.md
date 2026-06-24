# 📄 ChessWar Architecture Decision Records (ADR)

이 문서는 ChessWar 개발 과정에서 결정된 구체적인 기술적 선택,
핵심 게임 설계, 그리고 성능 최적화 의사결정 기록을 보관합니다.

## Architectural Background

본 프로젝트는 약 1년간의 개발 과정을 거치며 아키텍처의 근본적인 전환인 `Soft Reset`을 단행했습니다.

초기에는 엔터프라이즈 환경의 DDD 및 헥사고날 아키텍처를 채택하였으나,
Minecraft의 물리적 `Tick` 환경에서는 이러한 추상화가 한계에 부딪혔습니다.
Minecraft 서버의 20 TPS 한계를 방어하면서도 유지보수성을 확보하기 위해, 기획 중심의 GDD 용어를 수용하는 방향으로 구조를 전면 개편했습니다.
아래 기록들은 프로젝트 초기의 `Soft Reset` 과정 및 그 이후의 지속적인 개발 과정에서 수립된 핵심 아키텍처와 세부 구현 타협안의 근거들입니다.

---

## ADR-001: Component-Based OOP Adoption

### Context

`Soft Reset`을 단행하며 깊은 클래스 상속 구조의 결합도 문제를 해결하고 성능을 최적화하기 위해,
DDD, DOD, 순수 ECS 등의 아키텍처 도입을 다각도로 검토했습니다.

### Decision

DDD, DOD, 순수 ECS를 모두 기각하고,
식별자만 가진 깡통 객체에 플랫한 `Component`를 결합하여 사용하는 Component-Based OOP 패턴을 최종 채택합니다.

### Rationale

각 대안을 기각하고 `Component` 조립 방식을 채택한 기술적 이유는 다음과 같습니다.

1. **DDD 및 헥사고날 기각**: `Tick` 내에서 발생하는 과도한 객체 매핑 오버헤드가 개발 속도와 서버 성능을 심각하게 저하시킵니다.
2. **DOD 및 순수 ECS 기각**:
   메모리 연속성이 보장되는 C++과 달리,
   Java 객체는 힙 영역의 메모리 파편화 특성상 데이터 지향 설계가 주는 CPU 캐시 히트 최적화의 이점을 얻기 불가능합니다.
3. **결론**:
   따라서 Java 생태계의 물리적 한계를 인정하고,
   다중 상속 불가 문제를 회피하면서도 유지보수성과 성능의 최적 균형을 낼 수 있는 Component-Based OOP 방식을 채택했습니다.

---

## ADR-002: State-Manager Decoupling

* **Status**: Superseded by ADR-017

### Context

상태 클래스가 `Manager`를 참조하거나 둘 사이에 순환 의존성이 발생하면,
상태 전이의 추적이 어려워지고 동시 수정 예외나 메모리 누수 버그로 이어집니다.

### Decision

상태 객체를 순수 데이터 컨테이너로 철저히 고립합니다.
핵심 상태 객체 내부에 비즈니스 로직이나 복잡한 상태 전이 규칙을 작성하지 않으며,
`Manager`는 상태 객체를 파라미터로 전달받거나 단방향으로 주입받아 제어합니다.

### Rationale

객체가 서로를 역참조하는 순환 의존성을 제거하여 비동기 `Tick Loop`에서의 런타임 에러를 원천 차단하고
구조적 복잡도를 소거하기 위해 단방향 데이터 흐름을 강제합니다.

---

## ADR-003: Presentation-Domain Separation

### Context

`Minecraft` 엔진 특성상 `Piece`나 `ChessBoard`는 공간 좌표계인 `Location`과 결합되어야 합니다.
그러나 전투 공식 등 서버 측 논리 연산 중에 `Sound` 발송, 대량의 `Particle` 생성 로직이 강결합되면,
시청각 연출 부하가 메인 `Tick` 연산 속도에 직접 영향을 주어 랙을 유발합니다.

### Decision

`Location`과 `Vector` 같은 수학/공간 객체는 `Domain`에서 1급 객체로 허용하되,
`Presentation` 계층은 물리적으로 완전히 격리합니다.
`Manager` 계층은 도메인 논리만 처리하고 `Presentation Delegate` 인터페이스를 호출해 연출을 위임합니다.

### Rationale

순수 Java 객체로의 변환 오버헤드를 줄이는 실용성은 챙기면서,
`Particle` 연산 등 시각적 처리 부하를 `Tick Loop`에서 분리하여 서버 성능을 방어하고 연출 튜닝의 반복 작업 속도를 극대화합니다.

---

## ADR-004: 2D Array Spatial Grid vs HashMap

### Context

8x8 체스판과 같이 고정된 공간에서 기물의 위치를 탐색할 때,
`Coordinate`를 키로 하는 `HashMap<Coordinate, Piece>` 구조를 사용하면 매 탐색마다 해시 계산 비용과 객체 박싱 오버헤드가 발생합니다.

### Decision

해시 기반의 `Map` 구조를 기각하고, `Piece[][]` 형태의 다차원 배열 구조를 메인 공간 도메인 저장소로 채택합니다.

### Rationale

고정 크기의 격자 공간 탐색에서는 배열의 인덱스 기반 `O(1)` 조회가 압도적으로 빠르며,
메모리 연속성 및 CPU 캐시 히트율을 극대화하여 `Tick`당 탐색 연산 비용을 획기적으로 낮출 수 있습니다.

---

## ADR-005: Primitive Type Enforcement for GC Defense

### Context

Java 환경에서 `Integer`나 `Double`과 같은 래퍼 클래스를 `Tick Loop` 내부에서 무분별하게 사용하면
매 연산마다 오토박싱 및 언박싱이 발생하여 엄청난 양의 가비지를 생성합니다.

### Decision

매 `Tick`마다 연산되는 `Component` 내부의 숫자 데이터나 공간 수학 계산을 담당하는 `Manager`에서는 래퍼 클래스 및 내부적으로 박싱을 유발하는
`List<Integer>` 사용을 금지하고, 반드시 `int`, `double` 등의 원시 타입 및 원시 타입 배열을 사용합니다.

### Rationale

잦은 객체 할당으로 인한 Stop-the-world 현상을 원천적으로 방어하기 위해, 아키텍처 수준에서 원시 타입 사용을 강제합니다.

---

## ADR-006: Hot Path Mutability & Component In-Place Update

### Context

순수 객체 지향 및 일반적인 불변성 원칙을 따르면 상태 변경 시 새로운 객체를 반환해야 하지만,
매 `Tick`마다 지속시간이 차감되는 `Status Effect`나 `Particle` 좌표의 경우 `Tick`마다 새 객체를 반환하면 힙 메모리가 급격히 팽창합니다.

### Decision

턴 전환 등 중빈도로 발생하는 `Warm Path`에서는 불변성을 유지하고 객체를 교체하지만,
매 `Tick` 갱신되는 `Hot Path`에서는 예외적으로 객체 생성을 금지하고 내부 필드를 직접 변경하는 것을 강제합니다.
이를 위해 `Piece` 등 상태 객체 내부에 자신을 제어하는 In-place 갱신용 헬퍼 메서드를 허용합니다.

### Rationale

Minecraft 단일 스레드 환경에서 20 TPS를 사수하기 위해 이상적인 불변성을 포기하고 실리적인 객체 재사용성을 택합니다.

---

## ADR-007: Polling vs Event-Driven Communication

### Context

`Manager` 간 통신 결합도를 낮추기 위해 전면적인 `Event-Driven` 구조를 도입할 경우,
초당 수백 번 발생하는 `Tick` 갱신 로직까지 `Event`를 발행하게 되어 `Event` 객체 생성 부하가 치명적 수준에 이릅니다.

### Decision

데이터의 성격에 따라 통신 프로토콜을 분리합니다.
지속 데미지 등 `Continuous` 감시가 필요한 로직은 `Manager`가 직접 상태를 읽어오는 `Polling` 방식을 사용하고,
기물 파괴나 턴 종료 같은 `Discrete` 전이 로직에만 가벼운 `Domain Event`를 발행하여 결합도를 끊습니다.

### Rationale

결합도 완화라는 객체지향적 이점과 `Tick` 성능 방어라는 물리적 한계 사이에서 균형을 맞추기 위한 필수적인 프로토콜 타협입니다.

---

## ADR-008: Role-Based Constructor Design

* **Status**: Superseded by ADR-018

### Context

저장소 복구가 필요한 `Piece`와 수학적 고정값을 가지는 `ChessBoard`,
빈 상태로 시작하는 `BoardState` 등 도메인 객체들의 초기화 요구사항이 판이하게 다릅니다.

### Decision

역할에 따라 생성자를 다르게 강제합니다. `Piece` 등 `Warm Path` 객체는 `@AllArgsConstructor`만 열어두어 쓰레기 객체 생성을 막고,
공간 기하를 제어하는 객체는 생성자 내부에서 관계식을 직접 계산하여 캡슐화를 보장합니다.

### Rationale

선언부 초기화 후 주입값으로 덮어쓰는 방식은 최초 생성된 인스턴스를 즉각적인 가비지로 만들어버리므로,
생성 단계에서부터 객체 역할에 맞는 1:1 대입 방식을 규격화합니다.

---

## ADR-009: Null Safety and Layered Defense

### Context

서버 스레드는 트랜잭션 롤백을 지원하지 않아, 비즈니스 로직 실행 도중 예외가 터지면 가상 재화 불일치 문제가 일어납니다.
반면 매번 코어 로직에서 검증을 거치면 연산 지연이 발생합니다.

### Decision

명령어나 외부 이벤트가 들어오는 최외곽 경계 계층에서 철저한 검증 및 조기 종료를 수행하고,
코어 `Domain` 계층에서는 정적 분석에만 의존하여 런타임 체크를 과감히 생략합니다.

### Rationale

보안 경계와 속도 경계를 분리하여, 코어 로직의 불필요한 조건문 순회 오버헤드를 줄입니다.

---

## ADR-010: Bunker System Adoption

### Context

전투 개시 시점에 Minecraft 월드 내에 몹 `Entity`를 대량으로 실시간 스폰하면 메인 스레드 병목이 유발되어 시각적 끊김이 발생합니다.

### Decision

준비 단계에서 필요한 `Entity`를 보이지 않는 격리 구역에 미리 스폰해두고,
필요 시 텔레포트만으로 필드에 배치하는 벙커 시스템을 적용합니다.

### Rationale

서버 병목을 준비 시간으로 분산시켜 플레이어 경험을 쾌적하게 유지하기 위한 실전 최적화 기법입니다.

---

## ADR-011: Event-Based Custom Damage Control

### Context

Minecraft 바닐라의 데미지 판정 로직은 아군 보호나 페이즈 무적 같은 기획의 세밀한 요구사항을 완벽히 수용하지 못합니다.

### Decision

서버 엔진의 기본 데미지 이벤트를 최외곽에서 가로채어 강제 취소시키고,
독자적으로 계산된 커스텀 데미지만을 대상 `Entity`에 원자적으로 적용합니다.

### Rationale

게임의 밸런스와 전투 공식을 바닐라 엔진의 불확실성으로부터 독립시켜 룰의 일관성을 100% 통제합니다.

---

## ADR-012: Feature-Driven Packaging & Hybrid Sub-packaging Strategy

### Context

`Layer-driven` 패키징의 파편화 문제를 해결하기 위해 `Feature-driven` 패키징을 도입했습니다.
그러나 도메인 파일이 많아져 역할별 하위 패키지로 분할할 경우, Java 생태계의 특성상 `package-private` 접근 제어자를 사용할 수 없어
상태 객체의 가변 메서드를 `public`으로 열어야 하는 캡슐화 붕괴 문제가 발생합니다.
반대로 캡슐화 방어를 위해 기능의 초기 단계부터 무조건 `I/O` 패키지를 분리하여 강제 격리하는 방안도 검토했으나,
이는 잦은 디렉토리 이동과 불필요한 보일러플레이트를 유발하여 빠른 이터레이션을 저해한다는 딜레마가 있었습니다.

### Decision

`Feature-driven` 패키징을 유지하되, Start Flat, Grow Deep 철학 기반의 하이브리드 확장 전략을 채택합니다.

1. **Flat Initial Phase**:
   파일 수가 적은 초기에는 패키지 격리를 통한 컴파일러 방어를 유보하고, 한 폴더에 모든 클래스를 모아 개발 속도를 극대화합니다.
   `I/O` 객체의 상태 조작 통제는 코드 리뷰 규약으로 방어합니다.
2. **Hybrid Expansion**:
   파일 수가 임계치를 넘어가면 디렉토리를 분할합니다.
   단, `Entity`, `VO`, `Component`, `Manager`는 동일 부모 패키지에 고립시켜 `package-private` 방어선을 유지하고,
   파일 개수가 팽창하는 `Input Controller`와 `Presentation Delegate` 계층만 하위 패키지로 물리적으로 밀어냅니다.

### Rationale

초기 구조의 오버엔지니어링보다는 Minecraft 플러그인 생태계에 맞는 빠른 개발 속도를 우선시한 결정입니다.
동시에 프로젝트가 거대해져 패키지를 분리해야 하는 시점이 오더라도 객체지향의 가장 중요한 원칙인 상태 캡슐화를 절대 포기하지 않도록
Java 최적의 방어 구조를 내재화했습니다.

---

## ADR-017: Clarification of Manager Statelessness and Constructor DI

### Context

기존 `ADR-002` 및 `ARCHITECTURE.md`에서 `Manager`의 `Stateless`를 강조하며 "상태를 파라미터로 전달받아"라고 명시한 구절이,
`Constructor DI`마저 금지하는 함수형 프로그래밍의 순수 함수 제약으로 과잉 해석되는 문제가 발생했습니다.
이로 인해 모든 `Manager` 메서드에 `GameContext`를 매번 파라미터로 넘겨야 하는 비효율이 초래되었습니다.

### Decision

`ADR-002`의 기존 규칙을 대체하여 새로운 기준을 확립합니다.
`Manager`의 무상태성은 내부에 변경 가능한 가변 상태를 직접 소유하지 않음을 의미합니다.
`Manager`가 `GameContext`나 `Component` 등의 상태 컨테이너를 생성자 주입을 통해 전달받아 `final` 필드로
그 참조를 보관하고, 이를 통해 상태를 폴링하거나 조작하는 것은 아키텍처적으로 합법이며 적극 권장합니다.

### Rationale

메서드 파라미터 주입을 강제하는 것보다 생성자를 통한 참조 보관이 OOP의 응집도를 높이고 메서드 시그니처를 깔끔하게 유지합니다.
또한 상태 분리의 개념이 극단적인 순수 함수로 오해되어 객체지향적 설계가 훼손되는 현상을 원천 차단할 수 있습니다.

---

## ADR-018: Separation of Initialization and Restoration via Static Factories

### Context

과거 `ADR-008`은 객체 생성 시 가비지를 막기 위해 공간 기하 객체 등의 생성자 내부에서 관계식을 직접 계산하도록 허용했습니다.
그러나 이 방식은 시스템 데이터를 스냅샷으로부터 복구할 때 치명적인 문제를 일으킵니다.
객체를 메모리에 적재하기 위해 생성자를 호출하는 순간 내부의 계산 로직이 재실행되어,
전달받은 복구 데이터를 초기값으로 덮어쓰는 사이드 이펙트가 발생합니다.

### Decision

`ADR-008`의 기존 규칙을 대체하여 새로운 기준을 확립합니다.
`Entity`, `Component`, `VO` 등 도메인 상태 객체의 생성자 내부에 초기화 계산식을 포함하는 것을 엄격히 금지합니다.
모든 객체의 생성자는 `PRIVATE`으로 닫아두고 순수한 필드 대입만 수행해야 합니다.
대신 최초 생성용 `createInitial`과 상태 복구용 `fromSnapshot` 등 목적이 명확히 구분된 정적 팩토리 메서드를 통해서만 객체를 생성해야 합니다.

### Rationale

생성자를 순수 데이터 바인딩 공간으로 격하시키고 정적 팩토리 메서드로 역할을 위임함으로써, 메모리 할당과 상태 초기화 로직을 완벽하게 분리합니다.
이를 통해 가비지 생성을 방어한다는 기존의 목적을 달성하면서도 상태 복원 시 발생할 수 있는 데이터 오염을 원천 차단합니다.

---

## ADR-019: Branchless Optimization and 1D Flattening in Hot Path

### Context

`ADR-004`를 통해 도입된 Java의 2차원 배열은 내부적으로 배열의 배열 구조를 가지므로 힙 메모리 파편화와 캐시 미스를 유발합니다.
`ADR-006`의 `Hot Path` 최적화 원칙을 극한으로 끌어올릴 구체적인 데이터 구조 규약이 필요합니다.

### Decision

`Hot Path`에서 실행되는 공간 탐색 로직에 대해 다음 최적화를 강제합니다.

1. 논리 격자 배열을 `worldXTable`, `worldZTable`과 같은 1차원 배열로 평탄화하여 공간 지역성을 극대화합니다.
2. `applyCenterTo` 내부의 방향성 분기문을 제거하고,
   `create` 시점에 수학적 `Offset`을 미리 계산하여 `LUT` 형태로 저장하는 브랜치리스 설계를 적용합니다.
3. `Location` 객체의 신규 할당을 막기 위해 기존 인스턴스를 상태 변경하는 `applyTo` 패턴을 강제합니다.

### Rationale

초기화 시점의 연산량 증가를 감수하더라도, `Hot Path`에서의 `0-GC` 달성과 메모리 연속성 확보가 `Tick` 방어에 압도적인 우위를 제공합니다.

---

## ADR-020: Strict Immutability of Warm Path Domain State Objects

### Context

`ADR-006`에서 명시한 `Warm Path`의 불변성 원칙을 게임 세션 생명주기에 적용해야 합니다.
라운드가 전환될 때 기존 게임의 상태가 잔존하여 런타임 오류를 일으키는 상태 누수 버그를 원천 차단하는 구조적 제약이 필요합니다.

### Decision

`Board`, `Grid`, `Barracks` 등 세션을 대표하는 최상위 `Entity`는 모든 필드를 `final`로 선언하고
`Setter`를 허용하지 않는 완전한 불변 객체로 설계합니다. 게임 설정이 변경되거나 새 라운드가 시작될 경우,
기존 객체를 재활용하지 않고 이전 객체의 참조를 끊어 GC에 넘긴 뒤 완전히 새로운 인스턴스를 생성하여 교체합니다.

### Rationale

객체 할당 오버헤드보다 상태 누수로 인한 비즈니스 로직 오작동을 막는 것이 시스템 안정성에 더 큰 가치를 제공합니다.
`Hot Path`의 재사용 원칙과 물리적으로 분리하여, `Warm Path` 관리에는 철저한 불변성을 강제합니다.

---

## ADR-021: Top-Down Spatial Composition and Autonomous Layout

### Context

초기에는 `Manager` 계층이 `Location`과 `BlockFace`를 직접 계산하여
`Grid`, `Barracks` 등의 개별 `Component`를 생성한 뒤 `Board`와 같은 최상위 객체에 주입하는 방식을 취했습니다.
그러나 이 방식은 `Manager`가 도메인의 공간 기하학적 구조와 기획 밸런스를 모두 알아야 하는 결합도 문제를 낳았고,
`Team`이 확장될 때마다 `Manager` 로직을 수정해야 하는 OCP 위반을 초래했습니다.

### Decision

공간과 배치를 갖는 모든 최상위 `Entity`는 하향식 자율 조립 방식을 강제합니다.

1. `Manager`는 오직 기준이 되는 1개의 앵커 `Location`만 최상위 객체에 전달합니다.
2. 최상위 객체의 정적 팩토리 메서드 내부에서, 도메인 상수와 동적 루프를 활용해 하위 컴포넌트들의 위치를 스스로 계산하고 초기화합니다.
3. 공간 계산을 위한 `Location` 조작 시,
   엔진의 가변 객체가 유발하는 사이드 이펙트를 막기 위해 반드시 `clone()`을 통한 방어적 연산을 수행해야 합니다.

### Rationale

도메인 객체가 자신의 내부 구조와 배치 룰을 스스로 책임지는 정보 전문가 원칙을 확립합니다.
이를 통해 `Manager` 계층은 복잡한 공간 연산에서 해방되어 순수 비즈니스 흐름 통제에만 집중할 수 있으며,
팀 확장이나 맵 구조 변경 시 도메인 코드만 수정하면 되는 완벽한 데이터 주도 팩토리 구조를 완성합니다.

---

## ADR-022: Composition Root for Manager Systems

### Context

도메인 객체는 `ADR-021`에 따라 하향식 자율 조립 방식으로 조립되는데,
`Manager` 계층도 스스로 하위 `Manager`를 조립해야 하는가에 대한 기준이 모호했습니다.

### Decision

`Manager`는 스스로 다른 `Manager`를 생성하지 않으며,
오직 `Plugin` 진입점인 `ChessWar.java`를 `Composition Root`로 삼아 수동으로 `DI`를 강제합니다.

### Rationale

데이터 구조와 달리 `Manager`는 상호 참조가 빈번한 `Graph` 구조를 가집니다.
`Manager` 간의 자율 조립은 필연적으로 순환 의존성 문제를 발생시킵니다.
게임 엔진 환경에서 시스템 생명주기를 투명하게 통제하기 위해,
모든 시스템을 평등하게 메모리에 올리고 서로 의존성을 주입하는 시스템 레지스트리 패턴을 적용합니다.

---

## ADR-023: Lifecycle-Based State Wrapping and Atomic Swap

### Context

거대한 상태 객체인 `Board`를 재사용하며 `clear` 메서드로 데이터를 비우는 방식은 상태 누수 버그를 유발할 위험이 매우 높습니다.
또한, 상태를 감싸는 컨테이너 구조가 데메테르의 법칙을 위반한다는 아키텍처적 의구심이 제기되었습니다.

### Decision

상태를 보관하는 `Component`와 불변 데이터인 `Entity`를 명확히 분리합니다.
생명주기가 동일한 `VO`인 `Grid`는 `Board` 내부에 원시적으로 선언하고,
생명주기가 매치 단위로 다른 `Board` 객체는 `BoardState` 내부의 참조로 보관합니다.
비즈니스 로직이 없는 순수 상태 컨테이너는 데메테르의 법칙의 적용 예외 대상으로 둡니다.

구체적인 구현 규약은 다음과 같습니다:

1. **Class-level Getter**: 데이터 개방 의도를 명확히 하기 위해 클래스 레벨에 `@Getter`를 선언합니다.
2. **Package-private Setter**: `Manager` 패키지 내부에서만 상태를 교체할 수 있도록 `@Setter(AccessLevel.PACKAGE)`를 강제합니다.
3. **No Internal Logic**: `Component` 내부에는 `initBoard`와 같은 `Domain` 객체 생성이나 초기화 로직을 절대 포함하지 않습니다.

### Rationale

`Board` 객체를 원자적 교체 방식으로 교체하여 이전 매치의 상태 누수를 원천 차단합니다.
상태 컨테이너를 활용하면 `Manager` 계층이 방어적 프로그래밍 없이 무결성이 보장된 불변 객체를 획득할 수 있습니다.
상태 컨테이너는 단순한 `Pointer Holder` 역할만 수행하므로, 내부 속성을 투명하게 접근하더라도 객체의 캡슐화를 침해하지 않습니다.
또한 `Package-private Setter`와 로직 배제 원칙을 통해,
`Component`가 스스로 책임질 수 없는 비즈니스 흐름을 갖게 되는 안티 패턴을 구조적으로 방어합니다.

---

## ADR-024: Layer-Specific Ubiquitous Language and Component Naming Convention

### Context

기존 아키텍처에서는 데이터와 로직을 분리하면서 `Component`에도 접미사 생략을 강제했습니다.
이로 인해 순수 데이터인 `Board`와 이를 게임 시스템에 부착하기 위한 래퍼 객체인 `BoardState` 간의 명칭 구분이 모호해졌고,
백엔드식 `State` 네이밍 관성이 유입되는 부작용이 발생했습니다.
또한 도메인 용어를 모든 계층에 강제하려다 보니, 수학 연산이나 외부 엔진 제어 시 직관성이 떨어지는 문제가 있었습니다.

### Decision

네이밍 규약을 `Component-Based OOP` 철학과 계층별 유비쿼터스 언어에 맞게 전면 개정합니다.

1. **Component Naming**:
   시스템에 부착되는 데이터 슬롯 역할의 객체는 `Component` 접미사를 강제합니다. 순수 데이터 컨테이너임에도 불구하고,
   도메인의 원본 알맹이와 이를 시스템에 부착하는 래퍼를 구조적으로 구분하기 위한 예외적 조치입니다.
   대표적인 예로 `Board`와 `BoardComponent`가 있습니다.
2. **Linguistic Context Switching**: 역할별로 명확히 다른 언어 사전을 사용합니다.
    * `Manager` 및 핵심 `Entity`: 비즈니스 로직을 통제하므로 기획 의도를 담은 GDD 행동 용어를 사용합니다.
    * `Value Object` 등 공간 데이터: 공간 기하를 다루므로 오해의 소지가 없는 명시적인 수학/기하학 기술 용어를 사용합니다.
    * `Input Controller` 및 `Presenter`: 외부 모듈과 상호작용하므로 상호작용하는 엔진 API의 명명 규칙을 따릅니다.

### Rationale

`Component` 접미사를 명시함으로써, 해당 객체 내부에 로직을 배치해서는 안 된다는 아키텍처적 금기를 이름만으로 강력하게 전달합니다.
역할별로 언어 컨텍스트 스위칭을 수행하면,
메서드 이름만 보아도 현재 어느 계층의 코드를 다루고 있는지 파악할 수 있어 유지보수성과 가독성이 극대화됩니다.

---

## ADR-025: Data-Driven Delegation over Conditional Branching

### Context

기존 게임 로직에서 `Manager` 계층이 기물 종류 등 `Entity`의 상태에 따라 동작을 다르게 하기 위해
내부적으로 분기문을 사용하는 방식은 유지보수 비용을 증가시킵니다.
새로운 기물이나 룰이 추가될 때마다 여러 `Manager`의 코드를 수정해야 하는 개방-폐쇄 원칙 위반이 발생하며,
분기 예측 페널티로 인한 `Tick` 성능 저하 가능성도 존재합니다.

### Decision

`Entity` 종속적인 상태값 검사와 행동 분기는 `Manager` 내부에서 직접 수행하는 것을 원칙적으로 금지합니다.
이러한 로직은 **데이터 주도 위임** 패턴을 사용하여 처리합니다.
특정 타입이나 상태에 따른 분기 로직은 `enum` 내부에 상수로 내장하거나, 다형성을 활용하여 데이터 주도 방식으로 위임해야 합니다.

### Rationale

데이터 자체에 고유의 속성이나 계산식을 위임함으로써, `Manager`는 상태를 분기문으로 묻지 않고 오직 데이터를 조회하여 실행하기만 하면 됩니다.
이는 `Component-Based OOP` 철학에 부합하며, 로직의 확장성을 열어두고 분기 연산을 최소화하여 서버 `Tick`을 방어하는 최적의 설계입니다.

---

## ADR-026: Legacy Terminology and Class Migration Glossary

### Context

과거 작성된 의사결정 기록(ADR-001 ~ ADR-025)과 현재의 `ARCHITECTURE.md` 간에 용어 및 클래스 명칭의 불일치가 존재합니다.
과거의 의사결정 기록은 무단으로 수정할 수 없으므로,
현재 아키텍처에 더 이상 존재하지 않거나 이름이 변경된 과거의 용어 및 핵심 클래스들을 매핑하는 공식 번역 가이드가 필요해졌습니다.

### Decision

과거 ADR 문서를 열람할 때는 아래의 매핑 규칙을 기준으로 해석합니다.

1. **아키텍처 개념 및 용어 매핑**
    * **Layer**: 과거의 수직적 계층 구조는 폐기되었습니다. 현재는 `Feature` 중심 패키지 내부의 `Role` 개념으로 완전히 대체되었습니다.
    * **Domain**: 백엔드식 용어인 도메인은 현재 맥락에 따라 기획 룰인 게임 로직 또는 시스템 묶음인 `Feature`로 번역합니다.
    * **Domain Event**: 현재 플러그인 내 통신 방식인 `Event`로 대체되었습니다.
    * **Data Container / State Container**: 내부에 로직을 갖지 않는 데이터 구조체는 현재 `Component` 용어로 통일되었습니다.
    * **Presentation Delegate**: 현재 아키텍처 네이밍 규칙에 따라 `Presenter`로 대체되었습니다.

2. **클래스 명칭 변경 및 삭제 이력**
    * **`State` 접미사 클래스 (예: `BoardState`, `PieceState`)**:
      `Component` 접미사 강제 규약에 따라 현재 `BoardComponent`, `PieceComponent` 등으로 이름이 변경되었습니다.
    * **`Coordinate`**: 2차원 배열과 마인크래프트 `Location` 객체 중심의 평탄화 작업으로 인해 구조에서 완전히 삭제되었습니다.
    * **복잡한 래퍼 클래스들**: 원시 타입 우선 원칙에 따라 객체 생성을 최소화하기 위해 다수가 삭제되고 원시 타입 배열로 대체되었습니다.

### Rationale

명확한 매핑 가이드를 통해 과거 의사결정 기록의 가치를 보존하면서도,
새롭게 합류하는 팀원이 현재의 `ARCHITECTURE.md`와 과거 ADR을 읽을 때 겪는 인지적 부하와 혼동을 최소화합니다.
또한, 이름이 변경되거나 삭제된 클래스를 함께 명시함으로써 리팩토링의 결과를 문서로 투명하게 추적할 수 있도록 지원합니다.

---

## ADR-027: Hidden Allocation Restriction for GC Defense

### Context

Minecraft 플러그인 환경의 가장 핵심적인 과제는 메인 스레드 `Tick`을 방어하는 것입니다.
로직을 전개할 때 Stream API(`stream()`, `forEach()`), `Optional` 객체 생성, 캡처링 람다 등을 남발하면,
내부적으로 파이프라인 처리나 래퍼 생성을 위한 수많은 임시 객체가 할당되며 이는 곧바로 GC 압박으로 이어집니다.

### Decision

데이터 갱신 빈도에 따라 숨겨진 객체 할당을 유발하는 문법의 사용 기준을 엄격히 분리합니다.

1. `Hot Path` 및 `Warm Path` (인게임 로직):
   Stream API, `Iterable.forEach`, `Optional` 생성 및 반환, 캡처링 람다의 사용을 **엄격히 금지하며**,
   반드시 전통적인 for 루프와 명시적 null 체크 등 객체 할당이 없는 원시적인 방식을 사용해야 합니다.
2. `Cold Path` (서버 부팅, 설정 초기화): 성능 오버헤드보다 가독성의 이점이 크므로 해당 모던 Java 문법들의 사용을 **허용합니다**.

### Rationale

`Hot Path`에서의 임시 객체 할당 누적은 TPS 저하의 직접적인 원인이 되므로 타협의 여지가 없는 금지 대상입니다.
`Warm Path`의 경우 `Hot Path`만큼 치명적이지는 않으나, 동일한 인게임 로직 맥락 내에서 개발자가 매번 성능 영향을 판단해야 하는
인지 부하를 원천적으로 제거하고 일관성을 유지하기 위해 금지로 통일하는 것이 구조적으로 안전합니다.
반면 서버 부팅 시 1회성으로 동작하는 `Cold Path`는 성능 제약이 없으므로, 무의미한 제약으로 개발 생산성과 가독성을 떨어뜨릴 필요가 없습니다.

---

## ADR-028: Terminology Split - Execution Path vs Data Lifecycle

### Context

기존 ARCHITECTURE.md 문서와 여러 ADR 기록들(ADR-006, 019, 020 등)에서
`Path`라는 단일 용어가 두 가지 상이한 축의 최적화를 통칭하는 데 혼용되었습니다.
1. **Execution Flow 최적화 축**: 메인 틱 루프 등 로직이 얼마나 자주 실행되는가.
2. **Data Lifecycle 최적화 축**: 데이터 객체가 얼마나 자주 갱신되거나 메모리에 새로 할당되는가.

이로 인해 "실행 빈도는 매우 높지만,
데이터 생명주기는 영구 불변인 객체(`Grid` 등)"를 어떻게 분류해야 하는지 아키텍처적 인지 부조화가 발생했습니다.

### Decision

마인크래프트 서버 20 TPS 방어를 위한 두 개의 최적화 축을 산업 표준에 맞추어 명확히 분리합니다.

1. **실행 흐름 (Execution 차원)**: CS 표준 용어인 `Path`를 사용합니다.
   - `Hot Path`: 틱 루프 등 초고빈도로 실행되는 로직.
   - `Warm Path`: 턴 전환, 이벤트 등 간헐적으로 실행되는 로직. (참고: CS 표준을 간헐적 실행 특성에 맞게 확장 정의한 용어임)
   - `Cold Path`: 부팅/종료 등 1회성으로 실행되는 로직.
2. **데이터 생명주기 (Lifecycle 차원)**: DB/스토리지 산업 표준 용어인 `Data`를 사용합니다.
   - `Hot Data`: 매 틱마다 내부 필드가 갱신되어야 하므로 불변성을 포기하고 가변 조작을 허용하는 데이터.
   - `Warm Data`: 매치/턴 단위로 수명이 유지되며, 갱신 시 객체를 통째로 새로 교체하는 데이터.
   - `Cold Data`: 시스템 부팅 시 고정되어 영구히 불변으로 유지되는 데이터.

### Rationale

용어의 Overloading 문제를 해소함으로써, 코드의 실행 빈도와 메모리의 생명주기를 독립적으로 서술할 수 있습니다.
예를 들어 "Grid 클래스는 `Hot Path`에서 끊임없이 호출되지만,
데이터 자체는 완벽한 불변이므로 `Cold Data`로 취급한다"와 같이 모순 없이 아키텍처를 정의할 수 있게 됩니다.
