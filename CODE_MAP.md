# 🗺️ ChessWar Architectural Map

이 문서는 프로젝트 내 모든 파일의 아키텍처적 역할과 생명주기를 정의합니다. 새로운 파일 추가 시 반드시 이 지도에 등록하고 역할을 명시해야 합니다.

## 🔴 Object Role Legend
1. **[순수 데이터 객체] (State / Component)**: 로직이 없는 데이터 컨테이너.
2. **[코어 시스템 객체] (Rule / Physics)**: 도메인의 수학적/물리적 불변 법칙 소유.
3. **[관리자 및 시스템] (Manager / System)**: 비즈니스 로직 통제 및 상태 조율.
4. **[입력 컨트롤러] (Command / Listener)**: 외부 입력 감지 및 제어권 위임.

---

## 📦 Root Package
* **ChessWar**: **[관리자 및 시스템]** - 플러그인 메인 부트스트랩 및 코어 의존성 주입 관리.

---

## 📦 board (체스 보드 및 공간 관리)
### [순수 데이터 객체]
* **Coordinate**: **[Hot Path]** - 체스 그리드 좌표. Flyweight 패턴 적용.
* **BoardState**: **[Session Path]** - 현재 활성화된 보드 및 배럭 상태 정보.
* **GuideType**: **[Cold Path]** - 이동 가이드 시각화 타입 정의.

### [코어 시스템 객체]
* **ChessBoard**: 월드 좌표와 그리드 간의 수학적 변환 엔진.
* **Barracks**: 팀별 대기 공간의 물리적 레이아웃 및 위치 데이터.
* **MoveValidator**: 체스 기물별 이동 규칙 검증 엔진.
* **ChessFormation**: 보드 규격 및 초기 기물 배치 법칙.

### [관리자 및 시스템]
* **BoardManager**: 보드 및 배럭의 물리적 생명주기 관리자.
* **BoardVisualManager**: 유저용 시각적 피드백(가이드, 테두리) 출력 시스템.

### [입력 컨트롤러]
* **BoardBlockListener**: 보드 구역 내 블록 상호작용 및 파괴 제어.

---

## 📦 team (팀 시스템)
* **Team**: **[순수 데이터 객체] (Cold Path)** - WHITE/BLACK 팀 정의 및 기본 속성.

---

## 📦 piece (체스 기물 및 전투)
### [순수 데이터 객체]
* **Piece**: **[Session Path]** - 개별 기물의 인스턴스 데이터 (ID, 팀, 타입, 체력, 타겟 등).
* **PieceType**: **[Cold Path]** - 기물의 종류별 불변 속성 (기본 체력, 기호, 설명).
* **PieceState**: **[Session Path]** - 전장 내 모든 기물의 위치 및 엔티티 매핑 데이터. 2D Array 기반 관리.
* **StatBuff**: **[Hot Path]** - 기물의 스탯 강화 수치 데이터.
* **StatType**: **[Cold Path]** - 강화 가능한 스탯의 종류 정의.

### [관리자 및 시스템]
* **PieceManager**: 기물의 생성, 이동, 제거 및 물리 엔티티와의 동기화 조율.
* **PieceVisualManager**: BetterModel API를 이용한 기물의 시각적 모델 제어.
* **PiecePdcMapper**: Bukkit 엔티티의 Persistent Data와 도메인 객체 간 매핑.

### [입력 컨트롤러]
* **PieceDamageListener**: 기물의 피해 발생 시 도메인 규칙(CombatPolicy) 적용.
* **PieceInteractListener**: 우클릭/상호작용을 통한 기물 조작 및 능력 발동 감지.

---

## 📦 game (게임 루프 및 흐름)
### [순수 데이터 객체]
* **GameContext**: **[Session Path]** - 현재 게임 세션의 전역 상태 정보.
* **Participant**: **[Session Path]** - 게임 참가자의 정보 및 지휘권 데이터.

### [코어 시스템 객체]
* **CombatPolicy**: 전투 데미지 계산 및 상성 물리 법칙 정의.

### [관리자 및 시스템]
* **GameManager**: 전체 게임 루프(준비-전투-정산)의 상태 전이 통제.
* **CombatManager**: 기물 간 전투 로직 및 능력 상호작용 실행.
* **ScoreboardManager**: 실시간 게임 정보의 시각화 시스템.
* **GameAnnouncer**: 유저 대상 사운드/메시지 피드백 전파기.

---

## 📦 economy (경제 시스템)
### [순수 데이터 객체]
* **EconomyState**: **[Session Path]** - 팀/개인별 골드 보유 현황.
* **GoldComponent**: **[Session Path]** - 골드 수급 및 차감 단위 데이터.

### [관리자 및 시스템]
* **EconomyManager**: 골드 수급 로직 및 트랜잭션 관리.
* **ShopController**: 상점 GUI 렌더링 및 구매 로직 처리.

---

## 📦 admin (관리 기능)
### [입력 컨트롤러]
* **AdminCommand**: 게임 강제 시작, 중지, 리소스 청소 등 관리자 전용 기능 제공.

---
