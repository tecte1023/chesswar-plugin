# 📜 ChessWar Code Conventions

본 문서는 ChessWar 프로젝트의 자바 문법 규칙 및 코드 레벨 통제 스타일을 정의한다.
객체의 역할, `DOD` 최적화 철학 등 구조적 설계 원칙은 [ARCHITECTURE.md](ARCHITECTURE.md)를 참조한다.

---

## 1. Naming Conventions

클래스의 역할에 따라 시뮬레이션 객체의 의미를 명확히 전달하는 다음 명명 규칙을 강제한다.

### 1.1. Classes and Roles
* **Entity**: 접미사 없이 게임 내 순수 명사만 사용.
* **Manager**: 구체적인 통제 성격에 따라 `Registry`, `Lifecycle`, `Factory`, `Dispatcher`, `Bus`, `Context` 접미사 사용.
* **Component**: `Component` 접미사 사용.
* **Processor**: `Processor` 접미사 사용.
* **View**: `View` 접미사 사용.
* **Input Controller**: 마인크래프트 이벤트를 수신하는 객체는 `Listener`, 채팅 명령어를 수신하는 객체는 `Command` 접미사 사용.
* **DTO**: 마인크래프트 명령어 객체와의 혼동 방지를 위해 `Command` 접미사를 금지하고 유저 입력을 캡슐화한 데이터 객체는 `Action` 또는 `Request` 접미사 강제.
* **Simulation Event**: 시뮬레이션 상태 갱신 후 발행되는 통지 객체는 `Event` 접미사를 사용하며 버킷 엔진의 이벤트 객체와의 혼동 방지를 위해 최상위 마커 인터페이스는 `SimulationEvent` 사용.
* **Hot-Path Method**: 메인 루프에 의해 반복 호출되는 연산 진입점은 `tick` 또는 `onTick`으로 명칭 고정.

### 1.2. Time & Coordinate Boundaries
* **Time Units**: `Tick` 단위와 밀리초 단위 혼동 방지를 위해 시간 관련 변수에 `Ticks`, `Ms` 단위 접미사 필수 사용.
* **Coordinate Spaces**: 아키텍처의 논리 및 물리 격리 원칙에 따라 `grid`, `world` 좌표 공간 접두사 필수 사용.
* **Location vs Vector**: Bukkit 클래스인 `Location` 객체는 위치와 회전을 포함하므로 `loc` 명명 강제, 순수 이동 좌표인 `Vector` 객체만 `pos` 명명 강제.

### 1.3. Game Simulation Semantics
* **Lifecycle & Action**: 백엔드식 데이터 조작 어휘 대신 `spawn`, `despawn`, `applyDamage` 등 생명주기 및 물리적 상호작용 동사 사용.
* **Contextual Simplification**: `Component` 패턴 내부 필드 및 메서드 명명 시 클래스명과 중복되는 용어 생략.

### 1.4. Abbreviations
* **Math & Physics**: 수식 연산의 수평적 가독성을 위해 `pos`, `vel`, `dir`, `dist`, `rot`, `dt` 벡터 및 좌표 약어 적극 허용.
* **Common Game Stats**: `hp`, `mp`, `xp`, `atk`, `def` 등 범용적인 게임 스탯 약어 허용.
* **System Classes**: `Manager`, `Component`, `Controller`, `Inventory` 등 시스템 핵심 객체군 약어 절대 금지.

## 2. Syntax & Formatting

* **Lombok Annotations**: 캡슐화 붕괴 및 은닉된 런타임 오버헤드 유발 어노테이션 전면 금지.
  * **허용**: `@Getter`, `@RequiredArgsConstructor`, `@AllArgsConstructor`, `@Slf4j`, 불변 객체 한정 자바 `record` 적극 사용.
  * **금지**: `@Data` 전면 금지 및 가변 필드를 포함하는 `Component` 등의 객체에 `@EqualsAndHashCode` 전면 금지.
  * **제한적 허용**: 상태 변경 시 `@Setter` 제한적 허용, 직렬화 요구 시 `@NoArgsConstructor` 허용, 객체 할당 오버헤드가 존재하는 `@Builder`는 `Cold-Path` 한정 허용 및 `Hot-Path` 내부 금지.
* **Comments**: 코드의 동작을 단순히 번역하는 기계적인 주석 전면 금지.
  * **Inline Comments**: 동작 설명 주석은 전면 금지하되 엔진 버그 우회 등 불가피한 구현 이유 설명 시에만 제한적 허용.
  * **Javadoc**: 원칙적 작성 금지 및 타 서브시스템 호출 시 메인 스레드 블로킹 경고 표지판 목적으로만 극히 제한적 허용.
* **Nullability**: 시스템 내부 모든 객체는 널이 아님을 기본값으로 간주하여 무분별한 어노테이션 도배 전면 금지 및 비즈니스 로직 가독성 확보.
  * **@Nullable**: 외부 물리 경계선에서만 제한적 허용하며 분기 예측 방해를 막기 위해 `Hot-Path` 내부 방어적 널 체크 및 반환을 전면 금지하고 빈 구조체 반환 강제.
  * **@NotNull**: 롬복 통제로 초기 조립 무결성이 보장되므로 IDE 경고 차단 목적의 기계적 도배 전면 금지.
  ```java
  // 🟢 DO: 무결성 데이터를 신뢰하여 널 체크 생략 및 빈 배열 반환
  public Component[] getValidComponents() {
      if (size == 0) {
          return EMPTY_ARRAY;
      }

      for (int i = 0; i < size; i++) {
          components[i].tick();
      }

      return components;
  }

  // ❌ DON'T: Hot-Path 내부의 방어적 널 체크로 인한 분기 예측 방해 및 null 반환
  public Component[] getValidComponents() {
      if (components == null || size == 0) {
          return null;
      }

      for (int i = 0; i < size; i++) {
          if (components[i] == null) {
              continue;
          }

          components[i].tick();
      }

      return components;
  }
  ```
* **Type Inference Limit**: 숫자 연산에서 `var` 사용을 금지하고 우변만으로 객체 타입이 명백할 때만 제한적 허용.
* **Final Modifier**: 식별자, 주입 필드, 람다를 제외한 파라미터는 무조건 `final`로 선언하되 엔진 생명주기상 지연 초기화가 필수적인 인프라 객체에 한해 예외적 생략 허용 및 초기화 검증 로직 강제.
* **Braces Mandate**: 한 줄짜리 제어문이라도 중괄호 생략 금지.
* **this Keyword Omission**: 생성자 및 Setter 내부의 섀도잉 방어를 제외한 모든 인스턴스 필드 접근 시 `this` 키워드 전면 생략.
* **Constructor & Factory Method**: 생성자 내부는 부수 효과가 없는 순수 데이터 할당만 허용하며, 이벤트 구독 등 로직이 개입될 경우 `private` 생성자와 정적 팩토리 메서드로 객체 생명주기 완벽 분리.
* **Law of Demeter**: 시스템 및 시뮬레이션 객체 내 디미터 법칙을 엄수하되 `Entity`-`Component` 경계, `Hot-Path` 다이렉트 위임, 데이터 구조체, 수학 체이닝, 외부 엔진 API 등 5대 구역에 한해 예외적 체이닝 허용.

## 3. Hot-Path Syntax Restraints

`Hot-Path` 내부에서는 무의미한 힙 할당 방지와 연산 성능 극대화를 위해 다음 구문 제약을 강제한다.

### 3.1. Primitive Over Wrappers
힙 할당과 캐시 미스 방지를 위해 순수 원시 타입만 사용한다.
```java
// 🟢 DO: 순수 원시 타입 사용 (할당 없음)
public double calculateDamage(final int baseDamage) {
    return baseDamage * 1.5;
}

// ❌ DON'T: 래퍼 클래스 사용으로 힙 메모리 할당 및 포인터 역참조 오버헤드 유발
public Double calculateDamage(final Integer baseDamage) {
    return baseDamage * 1.5;
}
```

### 3.2. Zero-Allocation Loops
람다 인스턴스와 `Iterator` 객체 생성을 방지하기 위해 원시 배열이나 인덱스 기반 순회만 허용한다.
```java
// 🟢 DO: 원시 배열 또는 인덱스 기반 순회로 Zero-Allocation 달성
for (int i = 0; i < componentList.size(); i++) {
    componentList.get(i).tick();
}

// ❌ DON'T: Stream API 및 향상된 for문 사용으로 매 Tick마다 단명 객체 양산
components.stream()
        .filter(c -> c.isActive())
        .forEach(c -> c.tick());

for (Component c : componentList) {
    c.tick();
}
```

### 3.3. No String Concatenation
`Hot-Path`에서의 임시 `String` 객체 할당을 엄격히 금지한다.
```java
// 🟢 DO: 데이터가 변할 때만 조합하여 캐싱하거나 View 모듈로 위임
public void takeDamage(final int damage) {
    hp -= damage;
    cachedActionbarText = "남은 체력: " + hp; 
}

// ❌ DON'T: 매 Tick마다 무의미한 String 가비지 생성 (TPS 스파이크의 주범)
public void tick() {
    actionbarText = "남은 체력: " + hp; 
}
```

### 3.4. Thin Event Constraints
이벤트 객체는 `record`로 선언하여 불변성 및 Logic Zero를 보장한다.
힙 메모리 복사에 의한 `GC` 스파이크 방지를 위해 데이터 스냅샷 생성을 원천 금지하며, 룩업 방지를 위해 객체 참조와 일회성 Delta 정보만 포함한다.
```java
// 🟢 DO: 객체 참조와 일회성 Delta 정보만 전달하는 얇은 이벤트 (record 활용)
public record PieceDamageEvent(Entity target, int damageAmount) implements SimulationEvent {}

// ❌ DON'T: 틱 루프 내에서 상태 복사본을 힙에 꽉 채우는 두꺼운 이벤트 및 Map 룩업 유도
public class PieceDamageEvent implements SimulationEvent {
    private final UUID targetId;
    private final int currentHp;
    // ... 생략
}
```

## 4. Physical Ordering

클래스 내부 코드는 데이터의 변동성을 시각화하고 실행 흐름을 쉽게 파악할 수 있는 논리적 순서로 배치한다.

* **Annotation Ordering**: 클래스의 시스템 정체성 어노테이션을 최상단에, 단순 유틸리티 어노테이션을 최하단에 배치.
* **Field Ordering**:
  1. 불변 식별자
  2. 상태 객체 참조
  3. 가변 상태 데이터
* **Method Ordering**:
  1. 생성자 및 정적 팩토리 메서드
  2. `Hot-Path` 진입점
  3. `Cold-Path` 진입점
  4. `private` 헬퍼 메서드: 호출하는 `public` 메서드 근처 배치.
* **Parameter Ordering**:
  1. 공간 좌표: `x`, `y`, `z` 및 `width`, `height` 수학적 공간 배열 절대 엄수.
  2. 상호작용: `target`, `instigator`, `value` 순서의 주객 관계 배치 통일.
