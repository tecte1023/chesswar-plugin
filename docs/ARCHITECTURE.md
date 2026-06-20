# 🏗️ ChessWar Architecture

이 문서는 ChessWar 프로젝트의 설계 원칙, 시스템 경계 및 개발 프로세스 규칙을 정의하는 기술 청사진(법전)입니다. 구체적인 설계 의사결정 이력과 기술적 타협의 근거는 `ADR.md`에 기록합니다.

---

## 1. Core Pillars

프로젝트의 모든 코드는 자바 생태계와 마인크래프트 서버 환경의 특수성을 고려하여 아래 세 가지 축을 반드시 준수해야 합니다.

1. **State Modeling 👉 Component-Based OOP**: 구조적 정합성을 지키는 컴포넌트 조립 패턴을 사용해야 합니다. 깊은 클래스 상속을 배제하고, 식별자를 가진 엔티티에 데이터 컴포넌트를 동적으로 장착하여 도메인을 모델링합니다.
2. **Business Control 👉 State-Manager Decoupling**: 상태 데이터와 비즈니스 로직을 완전히 분리해야 합니다. 로직은 무상태 구조의 매니저 계층이 전담하며, 상태 데이터를 단방향으로 주입받아 규칙을 집행합니다.
3. **Presentation Control 👉 Presentation-Domain Separation**: 도메인 로직이 시각/청각적 출력 책임을 직접 소유하는 것을 금지합니다. 모든 시청각 처리는 전용 위임 객체를 통해서만 간접적으로 처리해야 합니다.

---

## 2. Domain Object Roles

모든 객체는 의존성의 단방향 흐름에 따라 아래 범주로 분류하며, 각자의 역할 범위를 엄격히 지켜야 합니다.

### 2.1. Domain State Object
오직 상태 유지와 캡슐화 정합성에만 전념하는 순수 데이터 객체입니다.
* **`Composite Entity`**: 여러 컴포넌트를 소유하는 루트 객체입니다. 스스로 비즈니스 규칙을 판단하지 않으며, 소속 컴포넌트들의 수명주기 전파만 책임집니다.
* **`Component`**: 특정 상태 속성이나 지속 효과를 담는 모듈화된 객체입니다. 다른 컴포넌트나 매니저를 절대 역참조해서는 안 됩니다.
* **`Value Object`**: 불변 성격을 가지는 데이터 구조체입니다. 모든 필드를 `final`로 선언하여 생성 후 상태 변경을 차단해야 합니다.

### 2.2. Manager & System
비즈니스 로직을 집행하는 무상태 구조의 시스템입니다. 도메인 상태 객체를 단방향으로 주입받아 이동, 데미지, 페이즈 전환 등을 처리합니다.

### 2.3. Interface Layer
* **`Input Controller`**: 플레이어의 명령어 및 이벤트를 가로채어 파싱한 후 `Manager`로 즉시 제어권을 이양합니다.
* **`Presentation Delegate`**: 도메인의 처리 결과를 마인크래프트 엔진의 시청각 요소로 번역하여 출력합니다.

---

## 3. Engine Coupling & Presentation Rules

자바 환경 최적화를 위해 실용적 결합을 허용하되, 연출 코드는 철저히 격리합니다.

* **1급 객체 허용**: 3D 공간 좌표 연산을 위한 Bukkit 객체(`Location`, `Vector` 등)는 도메인 핵심 데이터로 간주하여 `Manager` 및 `Component` 내부에서 직접 사용을 허용합니다.
* **시청각 Side-Effect 격리**: 상태를 변경하는 API 호출(`playSound`, `spawnParticle` 등)은 **오직 `Presentation Delegate` 내부로만 제한**합니다.

```java
// ❌ DON'T: 매니저 계층에서 직접 엔진 연출 API 호출 (상태 오염 및 테스트 불가)
public void movePiece(Piece piece, Location target) {
    piece.setLocation(target);
    target.getWorld().playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
}

// 🟢 DO: 상태 변경을 완결한 후 Presentation Delegate로 연출 위임
public void movePiece(Piece piece, Location target) {
    piece.setLocation(target);
    visualDelegate.onPieceMoved(target);
}
```

---

## 4. Performance & GC Defense

메인 틱(20 TPS)을 방어하기 위해, 데이터의 갱신 빈도에 따라 가변성을 차등 적용합니다.

* **`Cold Path` (저빈도)**: 고정 설정 데이터는 완벽한 불변 객체로 캐싱해야 합니다.
* **`Warm Path` (중빈도)**: 턴 전환 시 변경되는 컴포넌트는 내부 필드 수정을 지양하고, 새 객체를 생성하여 통째로 교체해야 합니다.
* **`Hot Path` (고빈도)**: 매 틱마다 갱신되는 데이터는 객체 생성을 엄격히 금지하며, 방어적 가변 조작을 예외적으로 허용합니다.

```java
// ❌ DON'T: Hot Path(매 틱 실행)에서 새로운 객체 반환 (GC 렉 유발)
public void tick() {
    this.velocity = new Vector(this.velocity.getX(), this.velocity.getY() - 0.1, this.velocity.getZ());
}

// 🟢 DO: 기존 객체의 내부 필드를 직접 수정하여 가비지 생성 차단
public void tick() {
    this.velocity.setY(this.velocity.getY() - 0.1);
}
```

* **원시 타입 우선**: 틱 연산에 사용되는 숫자 데이터는 오토박싱 방지를 위해 래퍼 클래스 대신 원시 타입을 사용해야 합니다.
* **고속 공간 연산 구조**: 공간 탐색 시 해시 연산 부하를 유발하는 객체 대신, 메모리 연속성과 인덱스 기반 조회가 보장되는 평탄화된 자료구조를 우선 채택해야 합니다.

---

## 5. Dependency & Communication Strategy

`Manager` 간의 상호작용은 틱 성능과 결합도를 고려하여 상황별로 3가지 프로토콜을 구분하여 사용해야 합니다.

1. **연속적 상태 감시 (`Polling`)**
    * 대상: 지속 데미지 등 매 틱마다 갱신이 필요한 로직
    * 규칙: 이벤트 객체 생성을 금지하며, 매니저가 직접 대상 상태 객체를 반복 조회합니다.
2. **이산적 전이 (`Event-Driven`)**
    * 대상: 기물 파괴, 턴 종료 등 비연속적인 상태 전이
    * 규칙: 역참조를 금지하며, 가벼운 도메인 이벤트를 발행하여 비동기식으로 전달합니다.
3. **원자적 제어 (`Top-Down Direct Call`)**
    * 대상: 게임 부팅 등 엄격한 순서가 보장되어야 하는 제어
    * 규칙: 상위 매니저가 하위 매니저의 메서드를 직접 순차 호출합니다.

---

## 6. Naming & Packaging Rules

### 6.1. Package Structure
역할 기반 패키징을 배제하고, 기획서의 핵심 시스템 단위를 기준으로 묶는 `Feature-driven` 패키징을 사용해야 합니다.

* **Cohesion**: 동일한 도메인의 `Entity`, `Component`, `Manager`, `Presenter`, `Listener`, `Command`는 1차적으로 모두 같은 패키지 내부에 위치시켜야 합니다.
* **Access Control**: 상태 객체의 가변 헬퍼 메서드는 `package-private` 접근 제어자를 사용해야 합니다. 오직 동일 패키지 레벨에 존재하는 `Manager`만 상태를 조작하도록 강제해야 합니다.
* **Logical Isolation**: 파일이 단일 패키지에 모여 있는 초기 단계라 하더라도, 입력 계층과 연출 계층이 `package-private` 헬퍼 메서드를 호출하여 상태를 직접 조작하는 것을 엄격히 금지합니다.

### 6.2. Package Growth Strategy
단일 패키지 내부의 클래스 파일 수가 임계치(약 15~20개)를 초과할 경우 하위 패키지 분할을 허용하되, 캡슐화 보호를 위해 다음 기준을 반드시 준수해야 합니다.

1. **Core Isolation**: `Java`의 `package-private` 가시성 유지를 위해 `Entity`, `Component`와 이를 제어하는 `Manager`를 하위 패키지로 분리하는 것을 금지합니다. 반드시 최상단 부모 패키지에 함께 배치해야 합니다.
2. **I/O Isolation**: 상태를 직접 조작하지 않고 읽기 및 이벤트 전달만 수행하는 입력 및 연출 위임 객체들은 `controller` 및 `presentation` 하위 패키지로 물리적으로 분리하여 격리해야 합니다.

### 6.3. Naming Conventions
* **Packages**: 마인크래프트 엔진 기본 객체와의 충돌 방지를 위해 반드시 소문자 단수형을 사용해야 합니다.
* **Classes**: 클래스명은 역할군 접미사(`Manager`, `Presenter`)를 필수로 붙여야 합니다. 단, 다음 두 가지 예외를 허용합니다.
   * `Entity`, `Component`, `Value Object`는 시스템 접미사를 생략해야 합니다.
   * `Input Controller` 계층은 엔진 관례에 따라 `Listener` 또는 `Command` 접미사를 허용합니다.
* **Methods**: 상태 조작 시에는 동사형을, 상태 조회 시에는 명사 또는 형용사형 체이닝을 지향해야 합니다.

---

## 7. Feature Development Lifecycle

새로운 시스템을 추가할 때는 오버엔지니어링을 방지하기 위해 다음 6단계의 주기를 엄격히 따릅니다.

1. **MVP (흐름 검증)**: 연출과 예외 처리를 배제하고, `Manager`와 `Component` 간의 순수 논리적 상태 변화만 콘솔로 검증합니다.
2. **UX Alignment (조작 통합)**: `Input Controller`를 연결하여 플레이어의 명령어/클릭 이벤트와 도메인 로직을 연동합니다.
3. **Refactoring (부채 소거)**: 기능 동작 확인 후, 매직 넘버를 분리하고 패키지 의존성 규칙에 맞게 코드를 재배치합니다.
4. **Feature Expansion (규칙 확장)**: 기획서에 명시된 예외 케이스와 수학적 규칙을 `Manager`에 추가합니다.
5. **Polishing (시각 연출 강화)**: `Presentation Delegate`를 연결하여 사운드, 파티클 등 엔진 종속적인 시청각 요소를 결합합니다.
6. **Optimization (성능 최적화)**: 프로파일러를 통해 틱 로드를 확인하고, 병목이 발생하는 영역에 한해 아키텍처 타협을 진행합니다.

---

## 8. The Priority Pyramid

원칙과 실제 개발 상황이 충돌할 때, 아래 우선순위를 기준으로 의사결정을 내립니다.

1. **👑 1순위: MVP와 생존** - 동작 가능한 최초 기능 구현 및 기획 검증이 최우선입니다.
2. **🥈 2순위: 엔진의 법칙** - 서버 틱을 유지하기 위한 직관적인 최적화는 아키텍처 원칙보다 우선합니다.
3. **🥉 3순위: 기획 기반 가독성** - 복잡한 디자인 패턴보다 기획서의 의도를 명확하게 투영한 코드가 우수합니다.
4. **🔨 4순위: 구현 패턴 준수** - 패턴은 단순 로직에 강제하지 않습니다.
