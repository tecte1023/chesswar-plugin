# 📜 ChessWar Code Conventions

본 문서는 ChessWar 프로젝트의 자바 문법 규칙 및 코드 레벨 통제 스타일을 정의한다.  
객체의 역할, `DOD` 최적화 철학 등 구조적 설계 원칙은 [ARCHITECTURE.md](ARCHITECTURE.md)를 참조한다.

---

## 1. Naming Conventions

클래스의 역할에 따라 도메인 의미를 명확히 하는 다음 명명 규칙을 강제한다.

* **Entity**: 접미사 없이 순수 도메인 명사만 사용.
* **Manager**: 구체적인 통제 성격에 따라 `Registry`, `Lifecycle`, `Factory`, `Dispatcher` 접미사 사용.
* **Component**: `Component` 접미사 사용.
* **Processor**: `Processor` 접미사 사용.
* **View**: `View` 접미사 사용.
* **Input Controller**: `Listener` 또는 `Command` 접미사 사용.
* **Hot-Path Method**: 메인 루프에 의해 반복 호출되는 연산 진입점은 `tick()` 또는 `onTick()`으로 명칭 고정.

## 2. Syntax & Formatting

* **Constructor Annotations**: 롬복 `@AllArgsConstructor` 및 `@RequiredArgsConstructor` 적극 활용. 불완전 객체 생성을 유발하는 `@NoArgsConstructor`는 원칙적으로 금지하되, 직렬화 프레임워크 요구 시에만 `access = AccessLevel.PRIVATE`로 캡슐화하여 허용.
* **Type Inference Limit**: 숫자 연산에서 `var` 사용 금지. 우변만으로 객체 타입이 명백할 때만 제한적으로 허용.
* **Final Modifier**: 식별자 및 의존성 주입 객체 필드, 그리고 메서드 파라미터는 무조건 `final`로 선언하여 부수 효과 차단 및 불변성 보장.
* **Braces Mandate**: 한 줄짜리 제어문이라도 중괄호 생략 금지.

## 3. Hot-Path Syntax Restraints

`Hot-Path` 내부에서는 무의미한 힙 할당 방지를 위해 다음 자바 문법을 원천 금지한다.

### 3.1. Primitive Over Wrappers
힙 할당과 캐시 미스 방지를 위해 순수 원시 타입만 사용.
```java
// 🟢 DO: 순수 원시 타입 사용 (할당 없음)
public double calculateDamage(int baseDamage) {
    return baseDamage * 1.5;
}

// ❌ DON'T: 래퍼 클래스 사용으로 힙 메모리 할당 및 포인터 역참조 오버헤드 유발
public Double calculateDamage(Integer baseDamage) {
    return baseDamage * 1.5;
}
```

### 3.2. Zero-Allocation Loops
람다 인스턴스와 `Iterator` 객체 생성을 방지하기 위해 원시 배열이나 인덱스 기반 순회만 허용.
```java
// 🟢 DO: 원시 배열 또는 인덱스 기반 순회로 Zero-Allocation 달성
for (int i = 0; i < componentList.size(); i++) {
    componentList.get(i).update();
}

// ❌ DON'T: Stream API 및 향상된 for문 사용으로 매 Tick마다 단명 객체 양산
components.stream()
        .filter(c -> c.isActive())
        .forEach(c -> c.update());

for (Component c : componentList) {
    c.update();
}
```

### 3.3. No String Concatenation
`Hot-Path`에서의 임시 `String` 객체 할당 엄격히 금지.
```java
// 🟢 DO: 데이터가 변할 때만 조합하여 캐싱하거나 View 계층으로 위임
public void takeDamage(int damage) {
    this.hp -= damage;
    this.cachedActionbarText = "남은 체력: " + hp; 
}

// ❌ DON'T: 매 Tick마다 무의미한 String 가비지 생성 (TPS 스파이크의 주범)
public void tick() {
    this.actionbarText = "남은 체력: " + hp; 
}
```

## 4. Physical Ordering

클래스 내부 코드는 데이터의 변동성을 시각화하고 실행 흐름을 쉽게 파악할 수 있는 논리적 순서로 배치한다.

* **Field Ordering**:
  1. 불변 식별자
  2. 상태 객체 참조
  3. 가변 상태 데이터
* **Method Ordering**:
  1. `Hot-Path` 진입점
  2. `Cold-Path` 진입점
  3. `private` 헬퍼 메서드 (호출하는 `public` 메서드 바로 아래 배치)
