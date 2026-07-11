# 🗺️ ChessWar Architectural Map

이 문서는 프로젝트 내 모든 파일의 아키텍처적 역할과 생명주기를 정의합니다. 새로운 파일 추가 시 반드시 이 지도에 등록하고 역할을 명시해야 합니다.

## 🔴 Object Role Legend

* **Composite Entity**: 하위 `Component`를 소유하는 루트 데이터 객체
* **Component**: 특정 상태를 담는 탈부착형 데이터 구조체
* **Value Object**: 상태 변경이 불가능한 불변 데이터 구조체
* **Manager**: 상태 제어 및 비즈니스 로직을 통제하는 무상태 제어 객체
* **System**: 데이터 연산 후 결과만 반환하는 무상태 순수 연산 객체
* **Input Controller**: 엔진 이벤트를 파싱하여 제어권을 이양하는 입력 계층
* **Presenter**: 처리 결과를 엔진 시청각 요소로 출력하는 연출 계층

### 2. Data & Execution Lifecycle

* **Cold Data**: 세션 내내 유지되는 저빈도 갱신 불변 데이터
* **Warm Data**: 턴 전환 등 간헐적으로 교체되는 중빈도 가변 데이터
* **Hot Data**: 매 `Tick` 갱신되거나 대량 일괄 처리되어 객체 생성이 엄격히 금지되는 성능 민감 데이터
* **Cold Path**: 서버 부팅 및 매치 초기화 시 1회성으로 실행되는 로직
* **Warm Path**: 유저 상호작용 및 턴 전환 시 간헐적으로 실행되는 로직
* **Hot Path**: 매 `Tick` 실행되거나 대규모 순회가 발생하여 CPU 파이프라인(분기 예측 등)에 극도로 민감한 핵심 로직

---

## 📦 Root Package

* **[ChessWar](../src/main/java/dev/tecte/chesswar/ChessWar.java)**:
  [`Manager` | `Warm Path`] - 플러그인 메인 부트스트랩 및 코어 의존성 주입 관리

---

## 📦 game

* **[GamePhaseComponent](../src/main/java/dev/tecte/chesswar/game/GamePhaseComponent.java)**:
  [`Component` | `Warm Data`] - 현재 `Phase` 상태를 담는 가변 컨테이너
* **[GamePhase](../src/main/java/dev/tecte/chesswar/game/GamePhase.java)**:
  [`Value Object` | `Cold Data`] - 매치의 현재 상태 단계(대기, 전투 등) 정의 열거형

---

## 📦 board

* **[Board](../src/main/java/dev/tecte/chesswar/board/Board.java)**:
  [`Composite Entity` | `Cold Data`] - `Grid`와 `Barracks`를 묶는 `Board` 논리 루트
* **[BoardComponent](../src/main/java/dev/tecte/chesswar/board/BoardComponent.java)**:
  [`Component` | `Warm Data`] - 현재 활성화된 `Board` 객체를 담는 탈부착 슬롯
* **[BoardUIComponent](../src/main/java/dev/tecte/chesswar/board/BoardUIComponent.java)**:
  [`Component` | `Hot Data`] - 시각화를 위한 `Grid` UI 상태 및 식별자 보관
* **[Grid](../src/main/java/dev/tecte/chesswar/board/Grid.java)**:
  [`Value Object` | `Cold Data`] - `Board` 및 `Barracks`의 독립적 좌표-공간 변환계
* **[Barracks](../src/main/java/dev/tecte/chesswar/board/Barracks.java)**:
  [`Value Object` | `Cold Data`] - `Barracks`의 논리적 레이아웃 데이터
* **[Coordinate](../src/main/java/dev/tecte/chesswar/board/Coordinate.java)**:
  [`Value Object` | `Hot Data`] - 체스 `Grid` 좌표
* **[BoardManager](../src/main/java/dev/tecte/chesswar/board/BoardManager.java)**:
  [`Manager` | `Warm Path`] - `Board` 논리 통제 및 생명주기 제어
* **[BoardPresenter](../src/main/java/dev/tecte/chesswar/board/BoardPresenter.java)**:
  [`Presenter` | `Hot Path`] - `Grid` 및 `Board` 시각 요소 파티클/블록 렌더링

---

## 📦 teamSide

* **[TeamRosterComponent](../src/main/java/dev/tecte/chesswar/team/TeamRosterComponent.java)**:
  [`Component` | `Warm Data`] - 특정 `TeamSide`에 속한 플레이어 UUID 목록
* **[TeamSide](../src/main/java/dev/tecte/chesswar/team/TeamSide.java)**:
  [`Value Object` | `Cold Data`] - 백/흑 `TeamSide` 구분 열거형
* **[TeamManager](../src/main/java/dev/tecte/chesswar/team/TeamManager.java)**:
  [`Manager` | `Warm Path`] - `Team` 참가, 퇴장 및 인원 상태 제어
* **[TeamSelectionListener](../src/main/java/dev/tecte/chesswar/team/TeamSelectionListener.java)**:
  [`Input Controller` | `Warm Path`] - 플레이어의 `Team` 선택 입력 이벤트 처리
* **[TeamPresenter](../src/main/java/dev/tecte/chesswar/team/TeamPresenter.java)**:
  [`Presenter` | `Warm Path`] - `Team` 색상, 이름표 등 시각화 제어

---

## 📦 piece

* **[Piece](../src/main/java/dev/tecte/chesswar/piece/Piece.java)**:
  [`Composite Entity` | `Warm Data`] - 식별자와 하위 `Component`를 묶는 루트 객체
* **[StatComponent](../src/main/java/dev/tecte/chesswar/piece/StatComponent.java)**:
  [`Component` | `Hot Data`] - 체력 등 `Piece`의 실시간 능력치 수치 보관 컨테이너
* **[ActionMaskComponent](../src/main/java/dev/tecte/chesswar/piece/ActionMaskComponent.java)**:
  [`Component` | `Hot Data`] - `Piece`의 이동/공격 범위 비트마스크 보관
* **[AbilityComponent](../src/main/java/dev/tecte/chesswar/piece/AbilityComponent.java)**:
  [`Component` | `Cold Data`] - `Piece` 고유 스킬 리스트 컨테이너
* **[EffectComponent](../src/main/java/dev/tecte/chesswar/piece/EffectComponent.java)**:
  [`Component` | `Warm Data`] - 상태 이상 리스트 및 개인 버프 보관 컨테이너
* **[PieceType](../src/main/java/dev/tecte/chesswar/piece/PieceType.java)**:
  [`Value Object` | `Cold Data`] - `Piece` 종류별 불변 정의 정보
* **[ActionPattern](../src/main/java/dev/tecte/chesswar/piece/ActionPattern.java)**:
  [`Value Object` | `Cold Data`] - `Piece`의 행동/이동 패턴 추상화 구조체
* **[LeaperPattern](../src/main/java/dev/tecte/chesswar/piece/LeaperPattern.java)**:
  [`Value Object` | `Cold Data`] - `Knight` 등 도약형 패턴
* **[PawnPattern](../src/main/java/dev/tecte/chesswar/piece/PawnPattern.java)**:
  [`Value Object` | `Cold Data`] - `Pawn` 전용 전진/대각 공격 패턴
* **[SliderPattern](../src/main/java/dev/tecte/chesswar/piece/SliderPattern.java)**:
  [`Value Object` | `Cold Data`] - `Rook`, `Bishop` 등 미끄러지는 패턴
* **[ActionPatternTable](../src/main/java/dev/tecte/chesswar/piece/ActionPatternTable.java)**:
  [`Value Object` | `Cold Data`] - 모든 `Piece`의 패턴을 매핑해둔 데이터 주도 레지스트리
* **[EffectType](../src/main/java/dev/tecte/chesswar/piece/EffectType.java)**:
  [`Value Object` | `Cold Data`] - 버프/디버프 성격 구분 열거형

---

## 📦 economy

* **[GoldComponent](../src/main/java/dev/tecte/chesswar/economy/GoldComponent.java)**:
  [`Component` | `Warm Data`] - `Gold` 수급 단위 데이터
* **[GoldSource](../src/main/java/dev/tecte/chesswar/economy/GoldSource.java)**:
  [`Value Object` | `Cold Data`] - `Gold` 수급처 구분 열거형

---

## 📦 admin

* **[BoardAdminCommand](../src/main/java/dev/tecte/chesswar/admin/command/BoardAdminCommand.java)**:
  [`Input Controller` | `Warm Path`] - `Board` 강제 초기화 및 디버그용 관리자 명령어
