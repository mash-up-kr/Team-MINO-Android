# 구현 계획: [SCR-003] 홈 탭

**대상 스펙 경로**: `docs/specs/home-deck-exploration`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 3.0.0

**최초 작성일**: 2026-08-26

**최종 수정일**: 2026-08-27

**버전**: 2.0.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

[SCR-003] 홈 탭 전체를 세운다 — 상단 셸(방 뱃지·캐릭터·인사 문구)·정렬 칩 3종·카드 덱·방 변경 시트·최초 진입 가이드·완료/빈 상태. 가장 큰 조각은 **정렬·방 자동 전환**(FR-010~016)이다. 사용자가 우측 영역 스와이프만 하면 한 방의 세 덱을 다 훑고 다음 방으로 넘어가며, 모든 방을 소진할 때까지 이어진다.

기술적 접근은 셋이다.

1. **전환 규칙을 `:core:domain`의 순수 UseCase로 뽑는다**(R-003). FR-011·012·013과 EC-009·013이 물린 하나의 판정이고, TS-015~019·021이 요구하는 검증이 정확히 이 함수의 입출력 검증이다. JVM 테스트로 그대로 옮긴다.
2. **정렬 3종 후보와 장소분류 라벨은 mock DataSource로 진행하되, 계약은 확정된 실제 것을 그대로 따른다**(R-001·R-002). `GET /api/v1/rooms/{roomId}/cards`가 서버 PR [Node#94](https://github.com/mash-up-kr/Team-MINO-Node/pull/94)로 설계·리뷰까지 끝나고 **배포만 남았다.** mock의 응답 형태·`labelGroup` enum·10장 절단·순서 유지를 실제와 맞춰, 전환 때 매퍼와 호출부가 바뀌지 않게 한다.
3. **홈은 탭 feature이므로 공개 표면은 `HomeNavigation.kt` 하나로 유지한다.** 홈 안에서 끝나는 전환(방 시트·액션 메뉴·가이드)은 콜백으로 내보내지 않고 전부 `HomeUiState`의 상태로 둔다.

4. **「확인」이 두 이벤트로 갈렸다**(spec 3.0.0 FR-023). 카드를 넘기는 것은 서버와 무관한 화면 상태 변화이고, 카드를 눌러 상세를 여는 것은 서버에 알리되 덱을 건드리지 않는다. **호출 경로를 아예 분리해** 이 독립을 코드에서 보이게 한다(R-012).

현재 `:feature:home`은 `title: String` 하나짜리 플레이스홀더 스텁이다. 이 계획은 그 자리를 채운다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin · Jetpack Compose (버전은 `gradle/libs.versions.toml`이 단일 출처)

**주요 의존성**: Hilt · Ktor Client · AndroidX DataStore · Navigation Compose. 모듈 구성은 [`architecture/modularization.md`](../../architecture/modularization.md)를 따른다.

**저장소**: DataStore (마지막으로 보던 방 · 가이드 닫은 이력 **둘만**, spec 3.0.0 FR-022·FR-019, R-004). 마지막으로 보던 방은 서버에 올리지도, 홈 진입 시 묻지도 않는다.

**테스트**: JUnit — `:core:domain`의 JVM 단위 테스트(전환 규칙)와 `:feature:home`의 ViewModel 테스트(화면 상태). 상세는 [`quickstart.md`](./quickstart.md) §3.

**대상 플랫폼**: Android

**프로젝트 유형**: mobile-app — 다중 Gradle 모듈

**성능 목표**: 카드 전환이 프레임 드랍 없이 이어지고, 애니메이션 중 입력으로 카드가 두 장 넘어가지 않는다(SC-005). 정량 기준은 spec이 정하지 않았으므로 새로 만들지 않는다.

**제약 조건**: 덱 전환 판정은 **전환이 일어나는 시점에 매번** 수행한다(FR-011) — 방 진입 시 계산해 두고 재사용하지 않는다. 빈 덱을 노출하지 않는다(EC-013). 두 확인 이벤트는 서로를 건드리지 않는다(FR-023) — 넘김은 서버를 부르지 않고, 상세 진입은 덱을 바꾸지 않는다.

**규모/범위**: 화면 1장(홈 탭) + 오버레이 3종(방 시트·액션 메뉴·가이드) + 상태 4종(덱·완료·빈 상태·로딩). FR 22건 · UX 4건 · TS 33건 · EC 16건.

**참조 API 문서**: `https://api.gguk.org/api-docs-json` · 조회 시점 **2026-08-27T21:12:20+09:00** · Team MINO API 1.0.0 · 오퍼레이션 24개. 홈 덱의 주 계약 `GET /api/v1/rooms/{roomId}/cards`는 **이 배포본에 아직 없고** 서버 PR [Node#94](https://github.com/mash-up-kr/Team-MINO-Node/pull/94)(OPEN)가 확정한 계약을 근거로 삼는다. 대조 결과는 [`contracts/deck-api.md`](./contracts/deck-api.md)가 소유한다.

## 헌법 준수 확인 게이트 (Constitution Check)

[`docs/constitution.md`](../../constitution.md) 2.1.0 기준. **Phase 0 전 판정과 Phase 1 후 재판정이 모두 통과다.**

| 게이트 | 근거 원칙 | Phase 0 전 | Phase 1 후 | 판정 근거 |
|---|---|---|---|---|
| 규칙 본문을 복제하지 않고 링크로 지목했는가 | I. SSOT | ✅ | ✅ | 이 문서와 부속 산출물이 규약 본문을 옮겨 적은 곳이 없다. 컴포넌트 배치·에러 처리·Figma 판정은 전부 소유 문서 링크 |
| 의존 방향이 바깥 → 안쪽인가 | II. 레이어 경계 | ✅ | ✅ | `:feature:home` → `:core:domain` ← `:core:data`. 도메인은 Android를 모른다. `ResolveNextDeckUseCase`는 순수 함수 |
| feature가 다른 feature를 의존하지 않는가 | II | ✅ | ✅ | 밖으로 나가는 전환 3종을 전부 콜백으로 셸에 넘긴다([`contracts/home-ui.md`](./contracts/home-ui.md) §1) |
| 바인딩을 구현 소유 모듈이 갖는가 | II | ✅ | ✅ | `DeckDataSourceModule`·`HomeDeckRepositoryModule`을 `:core:data`의 `di/`에 둔다. `:app`은 조립만 |
| 되돌리기 어려운 결정을 기록했는가 | III | ✅ | ✅ | mock 채택 근거를 [`research.md`](./research.md) R-001에 남겼다. ADR 승격 후보는 아래 §복잡도 추적 |
| 명세가 구현에 선행했는가 | IV. Spec-First | ✅ | ✅ | spec 2.0.0이 머지된 뒤 착수했다. plan에만 있고 spec에 근거 없는 요구사항 0건 |
| 근거 없는 빈틈을 지어내지 않았는가 | IV | ✅ | ✅ | 서버 미구현 2건과 협의 필요 6건을 봉합하지 않고 [`contracts/deck-api.md`](./contracts/deck-api.md) §3·§4에 드러냈다 |
| 산출물이 `docs/specs/{feature-name}/`에 모이는가 | IV | ✅ | ✅ | 전부 `docs/specs/home-deck-exploration/` 아래 |
| 템플릿을 먼저 복사했는가 | IV | ✅ | ✅ | `mino-sdd/template/plan-template.md`를 복사한 뒤 제자리 편집했다 |
| 디자인 토큰 판정 절차를 따르는가 | 기술 표준 | ✅ | ✅ | 값이 일치하는 토큰이 있으면 토큰, 없으면 Figma 실측값. 판정은 [`conventions/figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md) |
| 컴포넌트·에셋의 소속 모듈을 규약대로 정했는가 | 기술 표준 | ✅ | ✅ | [`contracts/home-ui.md`](./contracts/home-ui.md) §5. 판정은 [`conventions/component-asset-placement.md`](../../conventions/component-asset-placement.md) |

**정당화가 필요한 위반: 없음.** 아래 「복잡도 추적」은 비어 있다.

> **2.0.0 재평가** — 11개 게이트를 다시 판정했고 뒤집힌 것은 없다. 이번 개정이 건드린 것은 도메인 계약의 시그니처와 호출 시점이며, 모듈 경계·의존 방향·바인딩 소유는 그대로다. `GeoPoint`를 새로 만들지 않고 [`core/map`](../../../core/map/README.md)이 소유한 타입을 쓰는 것이 원칙 I(SSOT)에 부합한다.

> **한 가지 유의**: 헌법 §개발 워크플로와 품질 게이트의 「에이전트 행동 규칙」은 *"에이전트는 `git commit`을 직접 실행하지 않는다 … 커밋은 사용자가 실행한다"* 를 MUST로 둔다. 이 계획의 구현 단계에서도 그대로 적용된다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/home-deck-exploration/
├── spec.md              # 2.0.0 (/mino-spec 산출물)
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물 — 결정 11건
├── data-model.md        # Phase 1 산출물
├── quickstart.md        # Phase 1 산출물
├── contracts/
│   ├── deck-api.md      # 서버 API 대조 + mock 계약 + 협의 항목
│   └── home-ui.md       # 모듈 공개 표면 · Intent/SideEffect · 도메인 계약
├── quality/
│   └── spec-checklist.md
└── tasks.md             # /mino-task 산출물 (이 명령이 만들지 않음)
```

### 소스 코드 (Repository Root 기준)

```text
core/domain/src/main/kotlin/team/mino/core/domain/
├── model/
│   ├── DeckSort.kt · PlaceLabel.kt · PlaceCard.kt · Registrant.kt
│   ├── Deck.kt · DeckKey.kt · DeckContext.kt · NextDeck.kt
│   └── RoomSummary.kt
├── repository/
│   ├── HomeDeckRepository.kt
│   └── HomePreferencesRepository.kt
└── usecase/
    └── ResolveNextDeckUseCase.kt        # 전환 규칙의 단일 출처 (R-003)

core/data/src/main/java/team/mino/core/data/
├── datasource/
│   ├── DeckRemoteDataSource.kt          # getCards(roomId, sort, lat?, lng?)
│   ├── DeckMockRemoteDataSourceImpl.kt  # R-001·R-002 — 실서버 전환 지점 ①
│   ├── HomePreferencesLocalDataSource.kt
│   └── di/DeckDataSourceModule.kt       # 실서버 전환 지점 ②
├── network/dto/response/
│   └── CardResponse.kt                  # 실서버 전환 지점 ③
├── repository/
│   ├── HomeDeckRepositoryImpl.kt
│   ├── HomePreferencesRepositoryImpl.kt
│   ├── mapper/DeckMapper.kt
│   └── di/HomeRepositoryModule.kt

feature/home/src/main/java/team/mino/feature/home/
├── HomeNavigation.kt                    # 유일한 public 표면 (기존 파일 수정)
└── main/
    ├── screen/  HomeRoute.kt · HomeScreen.kt
    ├── vm/      HomeViewModel · HomeUiState · HomeIntent · HomeSideEffect
    ├── model/   HomePhase.kt · HomeTooltip.kt · RoomAppearance.kt
    └── component/
        ├── HomeTopShell.kt · SortChipRow.kt
        ├── CardDeck.kt · PlaceCardItem.kt · CardActionMenu.kt
        ├── HomeRoomSheet.kt · HomeTooltipOverlay.kt
        ├── HomeGuideOverlay.kt
        └── AllExhaustedContent.kt · EmptyContent.kt
```

**구조 결정**: 안드로이드 앱 단일 저장소 구조를 그대로 쓴다. 서버는 별도 저장소이므로 이 트리에 두지 않는다. 홈은 **탭 feature**이므로 `HomeActivity`·`HomeShell`·`HomeNavHost`·`di/`를 만들지 않는다 — 셸은 `:feature:main`이 소유하고 진입은 `homeGraph()` 등록 함수로 이뤄진다([`architecture/feature-module.md`](../../architecture/feature-module.md) §1·§2).

화면이 한 장이라 `main/` 하나만 둔다. 방 시트·액션 메뉴·가이드는 별도 Route가 아니라 같은 화면 위의 오버레이이므로 `component/`에 들어간다.

## 복잡도 추적 (Complexity Tracking)

정당화가 필요한 헌법 위반이 없다. 이 표는 비어 있다.

> mock DataSource 채택(R-001)은 위반이 아니라 **`group-room-form`이 이미 쓴 기존 패턴의 재적용**이다. 새 구조를 들이지 않으며, 실서버가 붙으면 `@Binds` 인자 타입 하나로 교체된다. 2.0.0에서는 mock이 흉내 낼 계약이 확정돼 있어 임의성이 더 줄었다.
