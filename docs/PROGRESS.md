# 📝 ChessWar Development Progress

## 1. MVP Core Loop - [100%] ✅
- [x] 체스판 설정 및 팀 데이터 관리 시스템 구축
- [x] 막사 생성 및 기물 엔티티 소환 로직 구현
- [x] 기물 선택 UI 및 기본 스탯 적용 완성
- [x] 턴 기반 이동/공격 로직 및 승리 조건 처리 완성

## 2. UX and Flow Enhancement - [100%] ✅
- [x] 게임 페이즈별 자동 흐름 연결 및 텔레포트 시스템 정교화
- [x] 무기 아이템 기반 조작 및 시각적 이동 가이드 최적화
- [x] 팀 참가/탈퇴 상호작용 및 예외 상황 방어 로직 강화
- [x] 공유 타이머 및 점수판 기반 정보 출력 시스템 완성
- [x] 킹 전용 NPC 지휘 시스템 및 실시간 가이드 연동

## 3. Infrastructure Refactoring - [100%] ✅
- [x] Feature-Based 패키지 구조 재배치 및 의존성 격리
- [x] 입력 컨트롤러와 매니저 간 역할 경계 명문화 및 위임 패턴 정착
- [x] 자율 관찰 기반 폴링 패턴 도입으로 매니저 간 순환 참조 해결
- [x] `NamespacedKey` 캐싱 및 DI 구조 개선을 통한 엔진 부하 최소화
- [x] `MoveValidator` Stateless 전환 및 파라미터 하향 제어 구조 구축
- [x] `State-Manager Decoupling` 통한 순환 참조 차단 및 `FSM` 데이터 객체화
- [x] `Persistent Data Container` 기반 상태 동기화 및 `PDC Mapper` 구축
- [x] `Engine-Native` 피드백 시스템 구현을 통한 출력 오버헤드 최소화
- [x] 시스템 GUI 전역 보안 정책 (Global Total Lockout) 구축 및 시각적 글리치 방어
- [x] 바닐라 물리 엔진 및 데미지 방어를 위한 **이벤트 기반 데미지 제어 (Event-Based Damage Control)** 시스템 정립
- [x] **Null Safety (Lombok @NonNull)** 기술 표준 수립 및 문서화 완성
- [x] 가변 객체 방어 및 성능 절충 하이브리드 설계 표준 수립 및 문서화 완료
- [x] 킹의 지휘권 고유 능력 분리 및 `commanderTarget` 상태 격리
- [x] `Piece` 생성 시점 엔티티 ID 주입 구조 개선


## 4. Feature Expansion - [100%] ✅
### Economy and Growth System - [100%] ✅
- [x] 턴별 골드 수급 및 처치 시 보상 획득 시스템 구축
- [x] 상점 시스템 구축 (킹 클래스 강화 및 개인 스탯 강화 GUI)
- [x] 개인 강화 원자적 결제 로직 및 스탯 합성 엔진 연동

### Piece Abilities and Lifecycle - [100%] ✅
- [x] 전 기물 초기 스탯 동기화 및 룩의 황금 체력 리필 능력
- [x] 비숍의 아군 회복, 나이트의 비선제 추가 피해 로직 구현
- [x] 퀸의 룩/비숍 능력 복합 계승 아키텍처 구축
- [x] **벙커 시스템 (Bunker System)** 도입으로 엔티티 생명주기 및 대기열 관리 최적화

### Combat Details and Tactical Items - [100%] ✅
- [x] 전술 아이템 인프라 및 '도약' 아이템 구현 (경로 우회 엔진 확장)
- [x] 장거리 기물 대상 초반 '도약' 아이템 자동 지급 밸런싱
- [x] 침묵 및 포박 같은 상태 이상과 도약 버프 및 디버프 통합 관리 시스템 리팩토링 완성

## 5. Polishing and Optimization - [80%] ⏳
- [x] F 키(Swap Hands) 기반 상점 호출 UX 최적화
- [x] Adventure MiniMessage 적용 및 사운드/파티클 피드백 강화
- [x] 보스바(BossBar)를 활용한 게임 단계 및 타이머 시각화
- [x] 스코어보드(Scoreboard)를 활용한 실시간 전장 정보 출력 고도화
- [ ] 프로파일링 기반 엔진 틱 최적화 및 프로덕션 수준 안정화
