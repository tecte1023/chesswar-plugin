# 🏗️ ChessWar Architecture

이 문서는 ChessWar 프로젝트의 설계 원칙, 시스템 경계 및 개발 프로세스 규칙을 정의하는 기술 청사진입니다.
구체적인 설계 의사결정 이력과 기술적 타협의 근거는 `ADR.md`에 기록합니다.

---

## 1. Core Pillars

이 프로젝트의 아키텍처는 상태와 로직을 결합하는 웹 백엔드의 DDD 및 교과서적 불변성이나,
객체를 해체하고 배열로 다루는 네이티브 게임 엔진의 순수 ECS 및 DOD를 맹목적으로 따르지 않습니다.
자바 생태계와 마인크래프트 서버의 제약을 극복하기 위해 아래 세 가지 축을 아키텍처의 핵심 원칙으로 삼습니다.

1. **State Modeling 👉 `Component-Based OOP (CB-OOP)`**:
   백엔드의 깊은 상속 트리나 순수 ECS의 메모리 배열 구조 대신,
   객체 지향의 유연성을 유지하면서 상태와 제어를 분리하는 **`CB-OOP`** 패러다임을 채택합니다.
   상태를 담는 데이터 객체와 이를 제어하는 게임 로직 주체를 철저히 격리하여 조립의 유연성과 정합성을 확보합니다.
2. **Performance Optimization 👉 `Zero-GC Driven Mutation`**:
   서버의 20 TPS 유지를 최우선 가치로 삼으며, 이를 위해 백엔드 구조의 모범 답안인 불변성을 의도적으로 폐기합니다.
   힙 메모리 할당으로 인한 GC 스파이크를 원천 차단하기 위해,
   객체의 상태를 직접 수정하는 **`In-place Mutation`을** 핵심 최적화 전략으로 강제합니다.
3. **Boundary Isolation 👉 `Side-Effect Isolation`**:
   마인크래프트 물리 엔진과의 실용적인 데이터 결합은 수용하되,
   상태 갱신 과정에서 **시각 및 물리적 부수 효과를 유발하는 연출 API의 직접 호출은 게임 로직 내부에서 엄격히 금지**합니다.
   모든 사이드 이펙트 처리는 아키텍처 외곽으로 완전히 격리하여 **게임 로직**의 오염을 방어합니다.

---

## 2. Object Roles

`Feature` 단위 패키지 내에서 동작하는 각 객체들의 고유한 역할과 제약 사항을 정의합니다.

### 2.1. Composite Entity

논리적인 식별자와 하위 `Component`를 관리하는 순수 데이터 루트 객체입니다.
Java 환경의 특성상 단순 정수 식별자가 아닌 클래스로 래핑하여 구현하는 것이 원칙입니다.
스스로 비즈니스 로직을 수행하지 않으며, 소속된 데이터의 생명주기 전파만 책임져야 합니다.
`Composite Entity` 내부에 위임 메서드를 작성하는 것은 엄격히 금지됩니다.
이는 객체가 비대해지는 안티 패턴을 방지하기 위함이며, `Manager`가 내부 `Component`를 직접 조회하고 조작해야 합니다.

### 2.2. Component

특정 상태를 담아 `Composite Entity`에 부착 및 해제하기 위한 데이터 구조체입니다.
내부에 일괄 갱신과 같은 게임 로직을 포함해서는 안 되며,
다른 `Component`나 `Composite Entity`를 참조하는 것은 금지되고 원시 타입 필드와 `Value Object` 참조만 예외적으로 허용됩니다.
`Component` 인스턴스는 `Composite Entity`의 고정된 구조적 슬롯이므로 객체 자체를 통째로 교체하는 것은 금지되며,
내부 상태 갱신을 위한 **`In-place Mutation`만이** 허용됩니다.

### 2.3. Value Object

상태 변이 빈도가 낮아 갱신 시 객체 전체를 재할당해도 가비지 컬렉션 부하가 경미한 모든 불변 데이터 구조체입니다.
모든 필드를 `final`로 선언하여 생성 후 상태 변경을 차단해야 합니다.
내부 구조가 복잡한 거대 불변 상태 객체의 경우,
`Composite Entity`와 달리 내부 구조 은닉과 결합도 저하를 위해 위임 메서드 제공이 적극 권장됩니다.

### 2.4. Manager

게임 세션의 상태를 생성자를 통해 참조를 전달받아 소유하며,
`Engine Event`나 `Internal Event` 등의 이벤트에 반응하여 전체 생명주기와 게임 로직을 통제하는 제어 객체입니다.
해당 `Feature`를 대표하는 `Composite Entity`가 존재하지 않더라도,
데이터에 대한 논리적 주권을 행사하고 게임 규칙 검증의 실행 흐름을 제어하기 위해 `Manager`는 반드시 존재해야 합니다.
스스로 제어 흐름 관리를 위한 상태 외에 자체적인 가변 상태를 소유하는 것은 허용되지 않습니다.
복잡한 수학 공식 연산이나 기하학 탐색을 직접 수행하는 것은 허용되지 않으며,
이는 `System`에 원시 데이터를 전달하여 결과값만 수신하는 것이 원칙입니다.

### 2.5. System

절대 상태를 소유하지 않으며 외부 입력이나 어떠한 `Engine Event`
또는 `Internal Event`에도 능동적으로 반응하지 않는 순수 연산 객체입니다.
`Manager`로부터 원시 배열 및 좌표 데이터를 파라미터로 전달받아 일괄 처리를 수행합니다. 상태의 소유와 연산을 분리함으로써 성능을 극대화합니다.
`Component` 상태의 직접 수정이나 시각·청각적 부수 효과 유발은 엄격히 금지되며, 오직 연산 결과값 반환에만 집중해야 합니다.

### 2.6. Input Controller

물리 엔진에서 발생하는 `Engine Event`와 `External Input`을 수신하여 파싱한 후 `Manager`에게 제어권을 넘겨야 합니다.
물리적 입력에 대한 1차 검증만 수행하며, `Feature`의 내부 상태를 사전 조회하여 게임 규칙을 검증하는 행위는 엄격히 금지됩니다.
대표적인 구현체로는 `Listener`와 `Command`가 있습니다.

### 2.7. Presenter

`Manager`의 명령을 받아 엔진의 연출 API를 호출하여 게임의 결과를 월드에 표현해야 합니다.
게임 로직을 보호하는 `Shield Pattern` 역할을 수행하며,
중복 명령 발생 시 스스로 멱등성을 보장해 주며 에러를 은닉하는 행위는 데싱크를 유발하므로 금지합니다.
조건이 맞지 않으면 즉시 `return`하는 `Fail-Fast` 원칙을 준수해야 합니다.

---

## 3. Instantiation Rules

인스턴스 생성 규칙은 객체의 역할이 아닌 "**생성 과정에 어떠한 형태의 로직이 개입하는가?**"를 단일 기준으로 삼아 결정합니다.

---

## 4. Engine Coupling & Presentation Rules

Java 환경 최적화를 위해 실용적 결합을 허용하되, 프레젠테이션 코드는 철저히 격리해야 합니다.

* **First-Class Object**:
  3D 공간 좌표 연산을 위한 Bukkit 객체인 `Location`, `Vector` 등은 핵심 상태 데이터로 간주하여
  `Component`가 필드로 소유하거나 `Manager`가 직접 연산에 사용하는 것을 허용합니다.
* **Side-Effect Isolation**:
  시각 및 청각적 연출 효과를 유발하는 `playSound`, `spawnParticle` 등의
  API 호출은 **오직 `Presenter` 내부로 격리하는 것이 원칙입니다**.
* **Visual Offset Prohibition**:
  좌표를 연산하여 반환하는 `System` 및 `Value Object`는 순수 기하학적 좌표와 논리적 방향만 반환해야 합니다.
  Y축 보정이나 `BlockData` 캐스팅 등 시각 연출용 `Visual Offset`은 배제하고 `Presenter`로 위임하는 것을 지향합니다.
* **Shield Pattern**:
  `Feature State`와 `View State`는 철저히 분리되어야 합니다.
  `Presenter`는 엔진 연출을 위한 자체적인 시각화 상태 맵을 가질 수 있으나,
  이 상태가 `Component`나 `Manager`로 유출되어 게임 로직을 오염시키는 것은 금지됩니다.

```java
// ❌ DON'T: Manager에서 직접 엔진 연출 API 호출 (상태 오염 및 테스트 불가)
public void processAction(CompositeEntity entity, Vector target) {
    entity.getTransform().setPosition(target);
    EngineAPI.playSound(target, SoundType.EXPLOSION);
}

// 🟢 DO: 상태 변경을 완결한 후 Presenter로 연출 위임
public void processAction(CompositeEntity entity, Vector target) {
    entity.getTransform().setPosition(target);
    presenter.onActionCompleted(target);
}
```

---

## 5. Performance & GC Defense

서버의 `20 TPS` 방어를 위해, 실행 흐름(`Path`)과 데이터(`Data`)를 독립적인 최적화 축으로 분리하여 관리합니다.

- **Hot/Warm/Cold Path**:
  코드가 얼마나 자주 실행되거나, 한 번 실행될 때 얼마나 많은 객체를 순회하여 분기 예측 실패와 CPU 비용을 유발하는가?
- **Hot/Warm/Cold Data**: 데이터가 얼마나 자주 갱신되거나, 한 번 갱신될 때 얼마나 많은 객체를 새로 할당하여 가비지 스파이크를 유발하는가?

아래는 데이터의 갱신 빈도 및 규모에 따른 **`Component` 내부 상태의** 메모리 할당 방어 규칙입니다.

* **Cold Data**: 부팅 시 한 번 로드되어 게임 세션 내내 사용되는 **저빈도 갱신** 데이터는 완벽한 불변 객체로 캐싱하는 것이 원칙입니다.
* **Warm Data**:
  턴 전환 등 간헐적으로 갱신되는 **중빈도 갱신** 데이터는 사이드 이펙트 방지를 위해 `Value Object`로 유지하고,
  갱신 시 `setter`를 통해 참조만 교체하는 것을 권장합니다.
* **Hot Data**:
  매 `Tick`마다 갱신되거나, 간헐적으로 실행되더라도 대규모 객체를 일괄 갱신하는 데이터는 객체 생성이 엄격히 금지됩니다.
  해당 데이터는 가변 객체의 내부 필드를 직접 수정하는 `In-place Mutation`을 강제합니다.

```java
// 🚨 Hot Data: 가비지 생성을 엄격히 차단
// ❌ DON'T: 연산 시 새로운 객체를 생성하여 할당 (GC 스파이크 유발)
public void applyGravity() {
    Vector current = this.velocityComp.getVelocity();
    this.velocityComp.setVelocity(new Vector(current.getX(), current.getY() - 0.1, current.getZ()));
}

// 🟢 DO: 기존 객체 참조를 유지한 채 내부 필드만 직접 수정하는 `In-place Mutation` 수행
public void applyGravity() {
    Vector current = this.velocityComp.getVelocity();
    current.setY(current.getY() - 0.1);
}

// ---------------------------------------------------------

// 🔄 Warm/Cold Data: 사이드 이펙트 방지를 위해 불변성 유지
// 🟢 DO: `System`이 `Value Object`를 연산하여 새로운 인스턴스를 반환하면, 참조만 교체
public void changeTurn() {
    MatchState currentState = this.matchStateComp.getMatchState();
    MatchState nextState = TurnPolicy.calculateNext(currentState);
    this.matchStateComp.setMatchState(nextState);
}
```

* **Primitive Type Priority**:
  `Hot Path` 연산에 사용되는 숫자 데이터는 오토박싱 방지를 위해 래퍼 클래스 대신 원시 타입을 사용하는 것이 원칙입니다.
* **Flat Data Structure**:
  해시 연산 부하를 유발하는 객체 대신, 메모리 연속성과 인덱스 기반 조회가 보장되는 평탄화된 자료구조의 채택을 권장합니다.
* **Nullability Partitioning**:
  `Hot Path` 내부의 분기 연산을 최소화하기 위해 배열과 컬렉션은 `NotNull` 강제를 위해 빈 구조체 반환을 원칙으로 합니다.
  엔진 포인터와 같이 레지스트리 조회가 강제되는 무거운 객체에 한해 예외적으로 `null`을 허용합니다.
* **JIT & Branch Prediction Optimization**:
  소규모 데이터의 순회 시 CPU 분기 예측 실패를 막기 위해 원시 배열 기반의 선형 탐색을 적극 권장하며,
  로직 분기 제거를 위해 2차원 원시 배열의 사용을 허용합니다.
* **Stream API & Capturing Lambda Prohibition**:
  보이지 않는 동적 힙 메모리 할당 폭탄을 차단하고 `Tick` 드랍 환경에서의 디버거 무결성을 확보하기 위해,
  `Hot/Warm Path`를 불문하고 `Stream API`와 람다의 사용을 전면 금지하며 순수 `for` 루프를 강제합니다.

---

## 6. Dependency & Communication Strategy

`Manager` 간의 상호작용 및 로직 제어는 결합도와 `Hot Path` 성능 부하를 고려하여 아래 규칙을 따릅니다.

1. **Continuous Polling**
    * 대상: 지속 피해 등 매 `Tick`마다 갱신이 필요한 로직
    * 규칙: 이벤트 객체 생성이 허용되지 않으며, `Manager`가 직접 대상 상태 객체를 반복 조회해야 합니다.
2. **Ascending Cost Order**
    * 대상: `Input Controller` 등 외부 물리적 경계 지점에서의 이벤트 검증 파이프라인
    * 규칙:
      하드웨어 친화적인 `Mechanical Sympathy` 최적화를 위해 가장 가벼운 상수 비교부터 가장 무거운 게임 로직 연산 순으로
      방어 로직을 배치하여 불필요한 사이클을 조기에 차단합니다.
3. **Discrete Event-Driven**
    * 대상: 기물 파괴, 턴 종료 등 비연속적인 상태 전이
    * 규칙:
      역참조는 배제되며, 가벼운 내부 논리 신호인 `Internal Event`를 발행하여 호출과 실행의 결합을 끊는 방식을 원칙으로 합니다.
      무거운 엔진 이벤트를 상속받는 것은 금지됩니다.
4. **Atomic Top-Down Direct Call**
    * 대상: 게임 부팅 등 엄격한 순서가 보장되어야 하는 제어
    * 규칙: 상위 `Manager`가 하위 `Manager`의 메서드를 직접 순차 호출해야 합니다.
5. **Lazy Evaluation**
    * 대상: 복잡한 상태 갱신 연산이 동반되는 고비용 `Hot Path` 연산
    * 규칙:
      부분 갱신에 따른 상태 불일치를 방지하기 위해,
      즉각적인 Push 방식의 연산을 배제하고 조회가 발생하는 시점에만 상태를 갱신하는 지연 평가를 지향합니다.

---

## 7. Naming & Packaging Rules

### 7.1. Package Structure

역할 기반 패키징을 배제하고, 기획서의 핵심 시스템 단위를 기준으로 묶는 `Feature` 중심 패키징 사용을 권장합니다.

* **Cohesion**:
  동일한 `Feature`에 속하는 `Composite Entity`, `Value Object`, `Component`, `System`, `Manager`, `Presenter`,
  `Input Controller`는 1차적으로 모두 같은 패키지 내부에 위치시키는 것이 원칙입니다.
  단, 특정 `Feature`에 종속되지 않고 재사용성이 극대화된 범용 `System`은 공통 패키지로 분리하는 것을 예외적으로 허용합니다.
* **Access Control**:
  상태 객체의 가변 헬퍼 메서드는 `package-private` 접근 제어자를 사용해야 합니다.
  오직 동일 패키지 레벨에 존재하는 `Manager`만 상태를 조작해야 합니다.
* **Logical Isolation**:
  파일이 단일 패키지에 모여 있는 초기 단계라 하더라도,
  `Input Controller`와 `Presenter`가 `package-private` 헬퍼 메서드를 호출하여 상태를 직접 조작하는 것은 엄격히 금지됩니다.

### 7.2. Package Growth Strategy

단일 패키지 내부의 클래스 파일 수가 약 15~20개의 임계치를 초과할 경우 하위 패키지 분할을 허용하되,
캡슐화 보호를 위해 다음 기준이 필수적으로 요구됩니다.

1. **Core Isolation**:
   Java의 `package-private` 가시성 유지를 위해 `Composite Entity`, `Value Object`, `Component`, `System`과
   해당 `Feature`를 제어하는 `Manager`를 하위 패키지로 분리하는 것은 허용되지 않습니다. 반드시 최상단 부모 패키지에 함께 배치해야 합니다.
2. **I/O Isolation**:
   상태를 직접 조작하지 않고 읽기 및 엔진 전달만 수행하는 `Input Controller` 및 `Presenter`는
   `controller` 및 `presenter` 하위 패키지로 물리적으로 분리하여 격리하는 것이 원칙입니다.
3. **Feature Split**:
   `Composite Entity`, `Value Object`, `Component`, `Manager` 등 코어 객체의 개수만으로 임계치를 초과할 경우,
   이는 SRP 위반의 경고 신호로 간주됩니다. 이 경우 단순히 I/O 기능만 하위 패키지로 격리하여 표면적인 파일 수만 줄이는 것은 지양하며,
   책임과 역할을 분석하여 완전히 독립된 새로운 부모 패키지로 `Feature` 자체를 분리할 것을 권장합니다.

---

## 8. Feature Development Lifecycle

새로운 시스템을 추가할 때는 오버엔지니어링을 방지하기 위해 다음 6단계의 주기를 엄격히 따릅니다.

1. **MVP**:
   연출과 예외 처리를 배제하고, `Manager`와 `Component` 간의 순수 논리적 상태 변화만 콘솔로 검증하여 흐름을 1차적으로 확인합니다.
2. **UX Alignment**: `Input Controller`를 연결하여 플레이어의 명령어와 클릭 이벤트를 게임 로직에 연동합니다.
3. **Refactoring**: 기능의 정상 동작을 확인한 후, 매직 넘버를 분리하고 패키지 구조 규칙에 맞게 코드를 재배치하여 기술 부채를 소거합니다.
4. **Feature Expansion**: 기획서에 명시된 예외 케이스와 게임 규칙 검증을 `Manager`에 추가하여 게임 로직을 확장합니다.
5. **Polishing**: `Presenter`를 연결하여 사운드, 파티클 등 엔진 종속적인 시각 및 청각적 연출 효과를 결합해 표현력을 강화합니다.
6. **Optimization**: 프로파일러를 통해 `Tick` 부하를 확인하고, 병목이 발생하는 영역에 한해 아키텍처 타협을 진행하여 성능을 최적화합니다.

---

## 9. The Priority Pyramid

원칙과 실제 개발 상황이 충돌할 때, 아래 우선순위를 기준으로 의사결정을 내립니다.

1. **👑 1순위 MVP 생존** - 동작 가능한 최초 기능 구현 및 기획 검증이 최우선입니다.
2. **🥈 2순위 엔진 법칙** - 서버 `Tick`을 유지하기 위한 직관적인 최적화는 아키텍처 원칙보다 우선합니다.
3. **🥉 3순위 기획 기반 가독성** - 복잡한 디자인 패턴보다 기획서의 의도를 명확하게 투영한 코드가 우수합니다.
4. **🔨 4순위 구현 패턴 준수** - 패턴은 단순 로직에 강제하지 않습니다.
