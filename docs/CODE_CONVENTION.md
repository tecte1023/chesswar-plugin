# 📜 ChessWar Code Conventions

이 문서는 마인크래프트 20 `TPS` 환경에서의 생존을 위한 `Zero-GC` 철학과
상태를 격리하는 `CB-OOP` 철학이 반영된 체스워 전용 실전 코드 컨벤션입니다.

---

## 1. Architecture-Driven Conventions

클래스의 아키텍처 역할에 따라 명명 규칙과 캡슐화 전략이 엄격하게 분리됩니다.

### 1.1 Data Objects

> 적용 대상: `Composite Entity`, `Component`, `Value Object`

게임의 상태를 담는 데이터 객체는 행위를 갖지 않는 순수 데이터 구조체로 취급합니다.

* **Naming**:
    * **Composite Entity**: `Match`, `Board` 등 게임 세션 내내 살아있는 거시적인 사물/개념 명사를 사용합니다.
    * **Component**:
      `Component` 접미사를 강제하며 접근자 접두사 대신 필드명 자체를 메서드명으로 사용합니다.
      렌더링이나 UI 제어용 상태는 `UIComponent`, `VisualComponent` 접미사를 강제합니다.
    * **Value Object**: `Coordinate` 등 매 틱 요동치는 미시적인 단위/속성 명사를 사용합니다.
    * **Hot Data**:
      객체 할당을 방어하기 위해 불변성을 포기하고, 기존 객체의 상태 필드를 직접 수정하는 `In-place Mutation`을 강제합니다.
      `addLocal()`, `mutate~` 등 직접 덮어쓰는 동사를 사용합니다.
    * **Warm/Cold Data**: 결합도 저하를 위해 새 객체를 할당하여 참조만 교체함을 암시하는 `with~` 전치사를 사용합니다.
* **Instantiation**:
  불완전한 상태 조립을 막기 위해 전체 인자 생성자 직접 호출만 허용하며, 부분 인자 생성자를 절대 금지합니다.
  데이터 대입 외의 계산이나 조립 등 어떠한 초기화 로직도 허용되지 않습니다.
* **Encapsulation**:
  가변 데이터 구조체인 `Component` 내부에 게임 규칙을 내포하는 위임 메서드 작성을 전면 금지합니다.
  필드는 제어 주체인 동일 패키지의 `Manager`가 직접 조작할 수 있도록
  `package-private` 접근 제어자와 `@Accessors(fluent = true)`를 사용하여 C 구조체처럼 순수 데이터로 노출합니다.
  단, 불변 데이터 구조체인 `Value Object`는 외부 결합도를 낮추기 위해 내부 자료구조를 은닉하는 위임 메서드 제공을 권장합니다.

```java
// 🟢 DO: 역할에 맞는 명명, Visual 접미사, 전체 인자 생성자 강제
public class Match {
    public Match() {
    }
}

public class Coordinate {
    public Coordinate(int x, int y) {
    }
}

// 🟢 DO: C 구조체 형태의 컴포넌트 및 package-private 제어
@Accessors(fluent = true)
public class VisualPositionComponent {
    @Getter
    @Setter(AccessLevel.PACKAGE)
    int x; // Setter만 package-private 강제

    public VisualPositionComponent(int x) {
        this.x = x;
    }

    public void addLocal(int dx) {
        x += dx;
    }
}

// ❌ DON'T: 부분 인자 생성자 사용 및 가변 객체 내부에 로직 내포
public class VisualPositionComponent {
    private int x;

    // ❌ 부분 인자 생성자 금지
    public VisualPositionComponent() {
    }

    // ❌ 로직 내포 금지
    public void moveToStart() {
        x = 0;
    }
}
```

### 1.2 Logic Controllers

> 적용 대상: `Manager`, `System`, `Input Controller`, `Presenter`

* **Naming**:
    * `Manager`, `Presenter` 접미사를 강제합니다.
      `Input Controller`는 Bukkit 등 외부 엔진 규격을 따르는 `Listener` 또는 `Command` 접미사를 명시적으로 사용합니다.
      인터페이스와 구현체의 역할군 접미사는 일치해야 합니다.
    * `System` 객체는 `Manager` 접미사를 절대 금지하며 `~Pattern`, `~Policy` 등의 직관적인 명사형 복합어를 사용합니다.
    * 객체 역할에 따라 용어의 성격을 분리합니다.
      `Manager`는 `kill`, `move` 등 기획서 상의 게임 묘사 용어를, `System`은 `calculate`, `intersect` 등 수학/기하학 기술 용어를,
      `Input Controller`는 `interact`, `spawn` 등 외부 엔진 기술 표준 용어를 사용합니다.
* **System Method**:
  부수 효과를 암시하는 동사(`update~`, `modify~` 등)를 금지하고 순수 수학적 동사(`calculate~`, `evaluate~`)만 허용합니다.
* **Manager Method**:
  상태 갱신 시 `try~` 등 검증 시도 및 `force~`, `apply~` 등 강제 갱신 의미의 접두사를 강제하며,
  `onPlayerClick`과 같은 엔진 이벤트 이름을 그대로 차용하는 것을 금지합니다.
* **Presenter Method**:
  멱등성 파괴를 경고하기 위해 모호한 `render~` 동사를 금지하고 유무가 갈리는 `show~`, `hide~` 동사를 강제합니다.
* **Instantiation**:
  내부 값 계산, 조립, 캐시 조회 등 단순 대입 이상의 제어 로직이 필요한 경우, 비공개 생성자와 정적 팩토리 메서드를 강제합니다.
  순수 연산 기능만 수행하는 `System` 객체는 무의미한 인스턴스화를 방지하기 위해 열거형이나 정적 싱글톤을 강제합니다.
* **Validation Boundary**:
  `Input Controller` 경계에서만 철저한 물리적 검증(Null 체크 등) 및 조기 종료 패턴을 수행합니다.
  `Manager`와 `System` 내부 루프에서는 분기 오버헤드를 막기 위해 런타임 Null 체크를 생략하고,
  `Lombok @NonNull` 등을 통한 컴파일 타임 정적 분석에 완전히 의존합니다.

```java
// 🟢 DO: 역할에 맞는 접미사와 명확한 동사 사용
public enum TurnPolicy {
    INSTANCE; // 싱글톤 시스템 객체 강제

    public PlayerId calculateNextTurn(Match match) {
        return match.getCurrentPlayer();
    }
}

public class MatchManager {
    public boolean tryMovePiece(PlayerId player, Coordinate from, Coordinate to) {
        return true;
    }
}

public class ParticlePresenter {
    public void showExplosion(int x, int y, int z) {
    }
}

// ❌ DON'T: 엔진 이벤트명 직접 노출 및 모호한 동사 사용
public class TurnSystem {
    public void onPlayerClick(Event event) {
        event.cancel();
    }
}

public class ParticlePresenter {
    // ❌ render 금지
    public void renderExplosion(Vector pos) {
    }
}
```

### 1.3 Event

> 적용 대상: `Internal Event`, `Engine Event`

* **Naming**: 내부 로직 통신을 위한 `Internal Event`는 반드시 과거형 동사 + `Event` 형태로 명명하여 상태 전이가 이미 발생했음을 명시합니다.
* **Engine Isolation**:
  아키텍처 오염과 순환 참조를 방지하기 위해,
  자체 정의하는 `Internal Event`가 `org.bukkit.event.Event` 등 외부 엔진 클래스를 상속받는 것을 절대 금지합니다.

```java
// 🟢 DO: 과거형 + Event 명명 및 순수 자바 이벤트 객체
public class PieceMovedEvent {
    private final UUID pieceId;

    public PieceMovedEvent(UUID pieceId) {
        this.pieceId = pieceId;
    }
}

// ❌ DON'T: 외부 엔진 이벤트 상속 및 모호한 현재형 명명
public class PieceMoveEvent extends org.bukkit.event.Event {
    private final UUID pieceId;
}
```

---

## 2. Zero-GC and Performance Pipeline

초당 20 `Tick` 방어를 위해 할당을 유발하는 자바 문법을 실행 경로에 따라 엄격하게 통제합니다.

### 2.1 Loop and Iteration

* **Stream & Lambda Prohibition**:
  보이지 않는 런타임 클로저 할당 및 디버깅 추적 방해를 원천 차단하기 위해, `Stream API`와 캡처링 람다의 사용을 전역적으로 절대 금지합니다.
* **Hot Path**:
  컬렉션 순회 시 `Iterator` 할당을 방지하기 위해 인덱스 기반 `for` 루프만 사용을 강제합니다.
  (단, `Iterator`를 생성하지 않는 원시 배열에 대한 향상된 `for` 루프는 예외적으로 허용)
* **Warm Path and Cold Path**:
  가비지 부하가 적은 영역이므로 일반적인 제네릭 컬렉션에 대한 향상된 `for`문 사용을 허용합니다.
  (단, `Stream API`는 여전히 전역 금지 대상입니다)

```java
// 🟢 DO: Hot Path에서의 컬렉션 순회 시 인덱스 기반 접근
public void updatePositions(List<Entity> entities) {
    for (int i = 0; i < entities.size(); i++) {
        Entity entity = entities.get(i);
        entity.update();
    }
}

// ❌ DON'T: Hot Path에서의 향상된 for문 사용으로 인한 Iterator 가비지 발생
public void updatePositions(List<Entity> entities) {
    for (Entity entity : entities) {
        entity.update();
    }
}
```

### 2.2 Primitive and Scalar Pipeline

* **Wrapper Class Restriction**:
  오토박싱 방지를 위해 `Hot Path`에서는 래퍼 클래스(`Integer`, `Double` 등) 사용을 명시적으로 금지하고
  원시 타입(`int`, `double` 등) 파라미터 및 필드를 강제합니다.
* **Scalar Unpacking Restriction**:
  객체 할당을 막기 위해 `Hot Path`에서는 `Vector`나 `Location` 같은 임시 래퍼 객체를 매개변수로 넘기는 것을 엄격히 금지합니다.
  구조체를 해체하여 `x`, `y`, `z` 등 원시 타입 스칼라 파라미터 파이프라인으로 호출해야 합니다.
* **Type Inference Limit**:
  런타임 오토박싱 은닉을 방지하기 위해 숫자 연산 및 물리량 조작 시 `var` 사용을 절대 금지하고 명시적 원시 타입을 선언합니다.
  `Hot Path` 내 제네릭 컬렉션 순회 시에도 사용을 금지합니다.
* **Type Inference Allowance**:
  우변만으로 객체 타입이 완전히 명확한 스택 참조 복사 환경에서는 시각적 피로도를 낮추기 위해 `var`를 적극 허용합니다.
  허용되는 기준은 명시적 형변환, `new` 생성자 호출, 클래스 리터럴 주입, 반환 타입과 클래스명이 동일한 정적 팩토리 메서드로 한정합니다.
  `Warm/Cold Path`의 순회 시에도 명시적 타입 선언을 권장하나,
  컬렉션 변수명으로 타입 유추가 완벽히 가능한 경우에만 제한적으로 `var` 활용을 허용합니다.

```java
// 🟢 DO: 스칼라 파이프라인(원시 타입 해체) 적용 및 객체 타입이 명확한 var 사용
public void applyDamage(int entityId, double x, double y, double z) {
    var list = new ArrayList<String>();
    var player = (Player) sender;
    var position = Coordinate.fromXYZ(1, 2, 3);
}

// ❌ DON'T: 불필요한 래퍼 객체 생성 및 반환 타입이 모호한 var 사용
public void applyDamage(Integer entityId, Vector targetPosition) {
    var stat = 10.5;
    var phase = TurnPolicy.calculateNext();
}
```

---

## 3. Clean Code and Mechanical Sympathy

문법 무결성과 CPU 캐시 적중률을 높이기 위한 클래스 내부 배치 규칙입니다.

### 3.1 Syntax and Variable Rules

* **Package Naming**: 자바 표준 관례를 따르며 복수형 명명 논쟁을 방지하기 위해 `piece`, `manager`와 같이 단수형 소문자 사용을 강제합니다.
* **Variable Naming**:
  `Player` 등 외부 엔진 객체의 직접 캐싱을 금지하며, `~Id` 식별자로만 선언하여 단순 메모리 포인터임을 명시합니다.
  `playerList`처럼 변수명에 자료구조 이름 노출을 금지하고 `players`처럼 순수 복수형만 허용합니다.
* **Constant Placement**:
  재컴파일 방지를 위해 게임 밸런스 수치는 `Value Object`나 열거형으로 격리합니다.
  단 90도 직각, 4방향 등 불변의 기하학적 진리는 상수로 추상화하지 않고 원시 수치 그대로 두거나 `System` 내부에 선언합니다.

```java
// 🟢 DO: 단수형 패키지 사용, 식별자 기반 의존성 및 순수 복수형 컬렉션 명명
// Directory: chesswar/piece/
public class PlayerComponent {
    private final UUID playerId;
    private final List<UUID> enemies;
    private int degrees = 90;
}

// ❌ DON'T: 엔진 객체 직접 캐싱, 복수형 패키지 및 자료구조 이름 노출
// Directory: chesswar/pieces/ (❌ 복수형 패키지)
public class PlayerComponent {
    private static final int RIGHT_ANGLE = 90;

    private final Player player;
    private final List<UUID> enemyList;
}
```

### 3.2 Physical Ordering

백엔드의 기계적인 접근 제어자(`public -> private`) 정렬 관성을 폐기하고, 하드웨어 친화적 설계와 인지 부하 감소를 위한 배치 원칙을 따릅니다.

* **Field Ordering**:
  상태 변동성 빈도를 기준으로 하향식 정렬을 강제합니다.
  불변 식별자 및 외부 주입 객체 👉 참조형 상태 👉 원시 타입 상태(매 틱 변동) 순으로 배치하여 캐시 효율을 높입니다.
* **Method Ordering**:
  실행 빈도인 `Path`를 최우선으로 하여 `Hot Path` 👉 `Warm Path` 👉 `Cold Path` 순서로 전진 배치합니다.
  공통 유틸리티는 맨 밑바닥으로 격리하며, 1:1 전용 `private` 헬퍼 메서드는 호출하는 `public` 메서드 바로 밑에 둡니다.
* **Chronological Ordering**: 동일한 빈도 그룹 내에서는 게임 객체의 생명주기(`Join` 👉 `Update` 👉 `Leave`) 시간순으로 정렬합니다.
* **Parameter Ordering**: `playerId` 👉 `damage` 처럼 상태 변이의 논리적 흐름인 문맥 주체 👉 변이 데이터 순서를 강제합니다.

```java
// 🟢 DO: 변동성 기반의 필드 배치와 생명주기 파라미터 배치
public class PlayerComponent {
    private final UUID playerId;
    private Rank currentRank;
    private int currentStamina;

    public void initPlayer() {
    }

    public void updatePlayer() {
    }

    public void decreaseStamina(int amount) {
        currentStamina -= amount;
    }
}

// ❌ DON'T: 기계적인 접근 제어자 기반 필드 정렬 및 생명주기 무시
public class PlayerComponent {
    private int currentStamina;
    private final UUID playerId;
    private Rank currentRank;

    public void decreaseStamina(int amount) {
        currentStamina -= amount;
    }

    public void updatePlayer() {
    }

    public void initPlayer() {
    }
}
```

### 3.3 Syntax Integrity

* **Import Declaration**: 인라인 코드 내에서 패키지 전체 이름 사용을 절대 금지하고 반드시 파일 상단에 `import`문을 선언해야 합니다.
* **Braces Mandate**: 제어문 사용 시 코드가 단 한 줄이더라도 반드시 중괄호를 강제하여 컨텍스트 분리를 명확히 합니다.
* **Parameter Final Mandate**:
  모든 메서드의 파라미터는 무조건 `final`로 선언하여 메서드 내부에서의 재할당으로 인한 부수 효과를 원천 차단합니다.
* **Inline Comment Limitation**:
  주석은 코드의 'What'이 아닌 'Why'를 설명할 때만 제한적으로 사용합니다.
  불필요한 인라인 주석(`//`)은 피하고, 중요한 컨텍스트는 마크다운 문서로 분리합니다.

```java
// 🟢 DO: final 파라미터, 단일 줄 제어문 중괄호 강제, 명시적 import

import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener {
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (event.getPlayer().hasPlayedBefore()) {
            return;
        }

        teleportToTutorial(event.getPlayer());
        // Bukkit 1.20.4 버그 우회: teleport 직후 바로 아이템을 지급하면 
        // 클라이언트 인벤토리 데싱크가 발생하므로 1틱 지연 후 지급함
        scheduleItemDelivery(event.getPlayer());
    }
}

// ❌ DON'T: final 누락, 제어문 중괄호 생략, What을 설명하는 무의미한 주석
public class JoinListener {
    public void onPlayerJoin(PlayerJoinEvent event) { // ❌ final 누락
        // 기존 접속 유저인지 확인 (❌ 무의미한 주석)
        if (event.getPlayer().hasPlayedBefore()) return; // ❌ 중괄호 생략

        teleportToTutorial(event.getPlayer());
        // 아이템 지급 (❌ 코드를 한글로 번역한 주석)
        scheduleItemDelivery(event.getPlayer());
    }
}
```
