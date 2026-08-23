# Architecture Decision Records (ADR)

설계 과정에서 내린 **중요한 결정과 그 이유**를 기록하는 문서 모음이다. 코드만 보면 "무엇을" 했는지는 알 수 있지만 "왜" 그렇게 했는지는 사라진다. ADR은 그 맥락을 남겨, 나중에 합류한 사람(과 에이전트)이 같은 고민을 반복하거나 이미 폐기된 방식으로 되돌리는 것을 막는다.

## 언제 ADR이 남는가

- 라이브러리·프레임워크 선택 또는 교체
- 모듈 경계·레이어 구조에 영향을 주는 결정
- 되돌리기 어렵거나, 나중에 "왜 이렇게 했지?"라는 의문이 생길 만한 결정
- 의도적으로 **하지 않기로** 한 것 (예: 특정 우회책을 금지)

자명하거나 쉽게 되돌릴 수 있는 결정은 남기지 않는다. 구체적인 판단 기준(트리거·안티-트리거)은 [`adr-writer` 스킬](../../.claude/skills/adr-writer/SKILL.md)의 입력 섹션이 단일 출처다.

## 작성 규칙

ADR은 [`adr-writer` 스킬](../../.claude/skills/adr-writer/SKILL.md)이 자동으로 작성하고 아래 인덱스까지 갱신한다. 문서 형식(파일명·상태 값·템플릿)은 [`adr-format.md`](../../.claude/skills/adr-writer/adr-format.md)를 단일 출처로 따른다.

## 인덱스

| 작성일 | 제목 | 상태 | 작성자 |
|---|---|---|---|
| 2026-06-29 | [직렬화 DTO의 IDE opt-in 경고에 `@OptIn` 우회를 쓰지 않는다](2026-06-29-serialization-optin-ide-warning.md) | Accepted | full_avocado |
| 2026-07-15 | [plan-gen 고도화는 에이전트 팀이 아닌 서브에이전트 오케스트레이션으로 한다](2026-07-15-plan-gen-subagent-orchestration.md) | Superseded by [SDD 파이프라인은 단계별 단일 스킬로 구성하고 spec-gen·plan-gen 서브에이전트 오케스트레이션을 폐기한다](2026-08-07-sdd-single-skill-per-stage.md) | Jaesung Lee |
| 2026-07-18 | [화면 트래킹은 호출부 조립 방식을 유지하고 TrackNavHost 래퍼는 강제화 시점까지 보류한다](2026-07-18-screen-tracking-composition.md) | Accepted | full_avocado |
| 2026-07-21 | [AnalyticsTracker.logEvent 파라미터는 Map<String, Any>를 유지하고 타입 세이프 빌더는 커스텀 이벤트 도입 시점까지 보류한다](2026-07-21-analytics-event-params-typing.md) | Accepted | full_avocado |
| 2026-07-24 | [시스템 폰트 배율을 무시하고 텍스트 크기를 고정한다 (1sp = 1dp)](2026-07-24-fixed-font-scale.md) | Accepted | Jaesung Lee |
| 2026-07-25 | [디자인시스템 컴포넌트 API는 Material3 컴포넌트 패턴(Defaults·Colors·컴포넌트 토큰)을 따른다](2026-07-25-design-system-component-m3-pattern.md) | Accepted | Jaesung Lee |
| 2026-07-25 | [UiState는 sealed 화면 상태 대신 isLoading 분리형으로 설계한다](2026-07-25-uistate-isloading-over-sealed-status.md) | Proposed | Jaesung Lee |
| 2026-07-25 | [에러 처리는 도메인 예외 매핑 + CEH 안전망의 2단 구조로 한다](2026-07-25-error-handling-two-tier-convention.md) | Accepted | Jaesung Lee |
| 2026-07-27 | [로컬 키-값 저장소로 Preferences DataStore를 채택한다](2026-07-27-preferences-datastore-local-storage.md) | Accepted | Jaesung Lee |
| 2026-07-27 | [ADR·실패 기록 파일명은 일련번호 대신 날짜 접두어를 쓴다](2026-07-27-record-filename-date-prefix.md) | Accepted | Jaesung Lee |
| 2026-07-29 | [Scaffold·insets는 화면이 아니라 네비게이션 셸(`XNavHost`)이 소유한다](2026-07-29-scaffold-ownership-navhost.md) | Accepted | Jaesung Lee |
| 2026-07-30 | [feature 모듈은 `api`/`impl`로 나누지 않고 단일 모듈로 둔다](2026-07-30-single-feature-module.md) | Accepted | Jaesung Lee |
| 2026-07-30 | [SUITE 폰트의 빈 글리프를 cmap에서 제거해 OS 시스템 폰트 폴백으로 처리한다](2026-07-30-font-empty-glyph-strip-for-fallback.md) | Accepted | full_avocado |
| 2026-07-31 | [네비게이션 셸을 `MinoScaffold`로 승격하고 feature 셸(`XShell`)과 화면 그래프(`XNavHost`)를 분리한다](2026-07-31-common-shell-mino-scaffold.md) | Accepted | Jaesung Lee |
| 2026-08-01 | [feature 간 전환 계약은 `:core:navigation`에 두고, 탭 feature는 등록 함수로 셸 그래프에 편입한다](2026-08-01-single-module-navigation-contract.md) | Accepted | Jaesung Lee |
| 2026-08-01 | [래스터 이미지 리소스는 WebP 확장자를 사용한다](2026-08-01-webp-for-raster-images.md) | Superseded by [공유 래스터 이미지는 `:core:common:ui`에 두고, WebP·밀도 규칙은 배치 규약이 소유한다](2026-08-19-raster-image-placement-and-format.md) | full_avocado |
| 2026-08-01 | [컴포저블 안의 람다는 수동 `remember`로 감싸지 않는다](2026-08-01-compose-lambda-memoization.md) | Accepted | Jaesung Lee |
| 2026-08-02 | [Hilt 구현 바인딩은 구현을 소유한 모듈이 갖고, `:app`은 그래프 조립만 한다](2026-08-02-di-binding-ownership.md) | Accepted | Jaesung Lee |
| 2026-08-03 | [Category 항목은 `MinoChip`으로 흡수하지 않고 전용으로 그리되, 치수 토큰만 공유한다](2026-08-03-category-item-dedicated-chip.md) | Accepted | Jaesung Lee |
| 2026-08-07 | [SDD 파이프라인은 단계별 단일 스킬로 구성하고 spec-gen·plan-gen 서브에이전트 오케스트레이션을 폐기한다](2026-08-07-sdd-single-skill-per-stage.md) | Accepted | Jaesung Lee |
| 2026-08-09 | [SDD Implement 단계는 오케스트레이터 + 전문가 서브에이전트로 실행하고 `.claude/agents/`를 다시 둔다](2026-08-09-implement-agent-orchestration.md) | Accepted | Jaesung Lee |
| 2026-08-13 | [모서리(셰이프)는 토큰 foundation을 두지 않고 컴포넌트 토큰이 실측값을 직접 든다](2026-08-13-no-shape-token-foundation.md) | Accepted | Jaesung Lee |
| 2026-08-13 | [디자인 토큰은 값이 일치할 때만 강제하고, 없으면 실측 raw를 그대로 쓴다](2026-08-13-design-token-when-value-matches.md) | Accepted | Jaesung Lee |
| 2026-08-14 | [방 대표 색상 12종 팔레트와 색상 칩은 `:core:design-system`이 소유한다](2026-08-14-room-color-palette-in-design-system.md) | Accepted | Jaesung Lee |
| 2026-08-16 | [mino-implement는 배정을 잘게 쪼개 병렬로 돌리고, Figma 값은 착수 시점에 한 번만 추출해 공유한다](2026-08-16-implement-chunking-parallelism-figma-value-sheet.md) | Accepted | Jaesung Lee |
| 2026-08-18 | [디자인 검수는 Phase 종료가 아니라 컴포넌트가 완성되는 즉시 붙인다](2026-08-18-design-audit-at-component-completion.md) | Accepted | Jaesung Lee |
| 2026-08-18 | [방 카드·방 칩·방 헤더 컴포넌트는 `:core:design-system`이 소유한다](2026-08-18-room-card-components-in-design-system.md) | Accepted | Chea-yunzi |
| 2026-08-19 | [공유 래스터 이미지는 `:core:common:ui`에 두고, WebP·밀도 규칙은 배치 규약이 소유한다](2026-08-19-raster-image-placement-and-format.md) | Accepted | Jaesung Lee |
