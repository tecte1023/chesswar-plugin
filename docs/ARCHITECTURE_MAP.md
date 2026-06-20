# 🗺️ ChessWar Architectural Map

이 문서는 프로젝트 내 모든 파일의 아키텍처적 역할과 생명주기를 정의합니다. 새로운 파일 추가 시 반드시 이 지도에 등록하고 역할을 명시해야 합니다.

## 🔴 Object Role Legend
* **`Composite Entity`**: 여러 컴포넌트를 소유하고 수명주기를 전파하는 루트 도메인 상태 객체.
* **`Component`**: 특정 상태 속성이나 지속 효과를 담는 모듈화된 도메인 상태 객체.
* **`Value Object`**: 생성 후 불변 성격을 가지는 순수 데이터 구조체.
* **`Manager`**: 비즈니스 로직을 집행하고 상태를 제어하는 무상태 시스템.
* **`Controller`**: 엔진의 이벤트를 감지하여 파싱한 후 `Manager`로 제어권을 이양하는 입력 계층.
* **`Presentation`**: 도메인 처리 결과를 엔진의 시청각 요소(파티클, 사운드 등)로 번역하여 출력하는 연출 계층.
* **`Cold Path`**: 부팅 시 한 번 로드되어 매치 내내 조회용으로 사용되는 불변 데이터/로직.
* **`Warm Path`**: 매치 세션 동안 수명이 유지되며, 턴 전환 등 중빈도로 교체되는 가변 상태 데이터/로직.
* **`Hot Path`**: 매 틱마다 갱신되어 객체 생성이 엄격히 금지되는 성능 민감 데이터/로직.

---

## 📦 Root Package
* **[ChessWar](../src/main/java/dev/tecte/chesswar/ChessWar.java)**: **`Manager`** - 플러그인 메인 부트스트랩 및 코어 의존성 주입 관리.

---

## 📦 game (게임 루프 및 전투 제어 시스템)
전체 게임의 생명주기와 기물 간의 전투 상호작용을 총괄합니다.

* **[GameContext](../src/main/java/dev/tecte/chesswar/game/GameContext.java)**: **`Component`**, **`Warm Path`** - 매치 전역 상태 정보.
* **[Participant](../src/main/java/dev/tecte/chesswar/game/Participant.java)**: **`Component`**, **`Warm Path`** - 게임 참가자 및 지휘권 데이터.
* **[GameManager](../src/main/java/dev/tecte/chesswar/game/GameManager.java)**: **`Manager`** - 게임 루프 생명주기 및 상태 전이 통제.
* **[CombatManager](../src/main/java/dev/tecte/chesswar/game/CombatManager.java)**: **`Manager`** - 기물 간 전투 로직 실행.
* **[CombatPolicy](../src/main/java/dev/tecte/chesswar/game/CombatPolicy.java)**: **`Manager`** - 전투 데미지 계산 공식 정의.

### ↳ presentation
* **[ScoreboardManager](../src/main/java/dev/tecte/chesswar/game/presentation/ScoreboardManager.java)**: **`Presentation`** - 실시간 스코어보드 시각화.
* **[GameAnnouncer](../src/main/java/dev/tecte/chesswar/game/presentation/GameAnnouncer.java)**: **`Presentation`** - 사운드 및 타이틀 메시지 피드백 전파.

---

## 📦 board (체스판 공간 및 논리 시스템)
체스판의 물리적 레이아웃과 기하학적 룰을 관리하는 패키지입니다.

* **[BoardState](../src/main/java/dev/tecte/chesswar/board/BoardState.java)**: **`Component`**, **`Warm Path`** - 현재 활성화된 보드 상태 정보.
* **[Coordinate](../src/main/java/dev/tecte/chesswar/board/Coordinate.java)**: **`Value Object`**, **`Hot Path`** - 체스 그리드 좌표.
* **[GuideType](../src/main/java/dev/tecte/chesswar/board/GuideType.java)**: **`Value Object`**, **`Cold Path`** - 가이드 시각화 타입 정의.
* **[ChessBoard](../src/main/java/dev/tecte/chesswar/board/ChessBoard.java)**: **`Manager`** - 월드 좌표와 그리드 간 수학적 변환 엔진.
* **[BoardManager](../src/main/java/dev/tecte/chesswar/board/BoardManager.java)**: **`Manager`** - 보드 및 배럭의 물리적 생명주기 관리.
* **[MoveValidator](../src/main/java/dev/tecte/chesswar/board/MoveValidator.java)**: **`Manager`** - 이동 규칙 검증 엔진.
* **[ChessFormation](../src/main/java/dev/tecte/chesswar/board/ChessFormation.java)**: **`Manager`** - 보드 규격 및 초기 배치 법칙.
* **[Barracks](../src/main/java/dev/tecte/chesswar/board/Barracks.java)**: **`Manager`** - 대기 공간의 물리적 레이아웃 데이터.

### ↳ controller
* **[BoardBlockListener](../src/main/java/dev/tecte/chesswar/board/controller/BoardBlockListener.java)**: **`Controller`** - 보드 구역 내 블록 상호작용 제어.

### ↳ presentation
* **[BoardVisualManager](../src/main/java/dev/tecte/chesswar/board/presentation/BoardVisualManager.java)**: **`Presentation`** - 가이드, 테두리 파티클 출력 시스템.

---

## 📦 piece (기물 시뮬레이션 시스템)
기물의 생명주기 및 런타임 상태를 관리하는 패키지입니다.

* **[Piece](../src/main/java/dev/tecte/chesswar/piece/Piece.java)**: **`Composite Entity`**, **`Warm Path`** - 개별 기물의 매치 상태.
* **[PieceState](../src/main/java/dev/tecte/chesswar/piece/PieceState.java)**: **`Component`**, **`Warm Path`** - 전장 내 모든 기물 위치 관리 (2D Array).
* **[PieceEffect](../src/main/java/dev/tecte/chesswar/piece/PieceEffect.java)**: **`Component`**, **`Warm Path`** - 기물 상태 이상 데이터.
* **[StatBuff](../src/main/java/dev/tecte/chesswar/piece/StatBuff.java)**: **`Component`**, **`Warm Path`** - 스탯 강화 수치 데이터.
* **[PieceType](../src/main/java/dev/tecte/chesswar/piece/PieceType.java)**: **`Value Object`**, **`Cold Path`** - 기물 종류별 불변 정의 정보.
* **[EffectType](../src/main/java/dev/tecte/chesswar/piece/EffectType.java)**: **`Value Object`**, **`Cold Path`** - 버프/디버프 성격 구분 열거형.
* **[StatType](../src/main/java/dev/tecte/chesswar/piece/StatType.java)**: **`Value Object`**, **`Cold Path`** - 강화 가능한 스탯의 종류 정의.
* **[PieceManager](../src/main/java/dev/tecte/chesswar/piece/PieceManager.java)**: **`Manager`** - 기물의 이동, 제거, 상태 무결성 제어.

### ↳ controller
* **[PieceDamageListener](../src/main/java/dev/tecte/chesswar/piece/controller/PieceDamageListener.java)**: **`Controller`** - 피해 발생 감지 및 취소.
* **[PieceInteractListener](../src/main/java/dev/tecte/chesswar/piece/controller/PieceInteractListener.java)**: **`Controller`** - 상호작용 능력 발동 감지.

### ↳ presentation
* **[PieceVisualManager](../src/main/java/dev/tecte/chesswar/piece/presentation/PieceVisualManager.java)**: **`Presentation`** - 모델 및 파티클 시각 효과 제어.
* **[PiecePdcMapper](../src/main/java/dev/tecte/chesswar/piece/presentation/PiecePdcMapper.java)**: **`Presentation`** - 엔진 Persistent Data 매핑 로직.

---

## 📦 economy (경제 및 상점 시스템)
* **[EconomyState](../src/main/java/dev/tecte/chesswar/economy/EconomyState.java)**: **`Component`**, **`Warm Path`** - 팀/개인별 골드 보유 현황.
* **[GoldComponent](../src/main/java/dev/tecte/chesswar/economy/GoldComponent.java)**: **`Component`**, **`Warm Path`** - 골드 수급 단위 데이터.
* **[EconomyManager](../src/main/java/dev/tecte/chesswar/economy/EconomyManager.java)**: **`Manager`** - 골드 수급 로직 및 트랜잭션 관리.

### ↳ controller
* **[ShopController](../src/main/java/dev/tecte/chesswar/economy/controller/ShopController.java)**: **`Controller`** - 상점 클릭 및 구매 로직 처리.

---

## 📦 team (팀 데이터)
* **[Team](../src/main/java/dev/tecte/chesswar/team/Team.java)**: **`Value Object`**, **`Cold Path`** - 팀 정의 및 기본 속성.

---

## 📦 admin (관리자 도구)
* **[AdminCommand](../src/main/java/dev/tecte/chesswar/admin/AdminCommand.java)**: **`Controller`** - 강제 시작/중지 등 관리자 예외 권한 명령어.
