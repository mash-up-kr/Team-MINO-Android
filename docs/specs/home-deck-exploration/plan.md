# 구현 계획: [SCR-003] 홈 탭

**대상 스펙 경로**: `docs/specs/home-deck-exploration`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 4.0.0

**최초 작성일**: 2026-08-26

**최종 수정일**: 2026-09-03

**버전**: 3.0.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

[SCR-003] 홈 탭 전체를 세운다 — 상단 셸(방 뱃지·캐릭터·인사 문구)·정렬 칩 3종·카드 덱·방 변경 시트·최초 진입 가이드·완료/빈 상태. 가장 큰 조각은 **정렬·방 자동 전환**(FR-010~016·024·025)이다. 사용자가 우측 영역 스와이프만 하면 **한 정렬로 모든 방을 훑고 다음 정렬로** 넘어가며, (정렬, 방) 격자의 칸이 모두 소진될 때까지 이어진다.

> **3.0.0은 두 벌의 개정을 한 번에 흡수한다.** ① spec 4.0.0이 탐색 축을 뒤집고 자동·수동 규칙을 갈랐다(FR-011·012·024·025). ② 시안이 방 캐릭터·완료 안내·툴팁 위치를 바꿨다. 개정 항목별 대응은 §개정 요약을 따른다.

기술적 접근은 셋이다.

1. **전환 규칙을 `:core:domain`의 순수 UseCase로 뽑는다**(R-003). 다만 **함수가 둘이다**(R-014) — 자동 전환은 격자 전체를 훑고(`ResolveNextDeckUseCase`), 수동 방 변경은 고른 방 하나만 훑는다(`ResolveRoomEntryDeckUseCase`). 규칙이 아니라 탐색 범위가 다르므로 플래그가 아니라 함수 경계로 가른다. TS-015~021·028a~c가 요구하는 검증이 정확히 두 함수의 입출력 검증이라 JVM 테스트로 그대로 옮긴다.
2. **정렬 3종 후보와 장소분류 라벨은 실서버를 쓴다.** `GET /api/v1/rooms/{roomId}/cards`가 2026-08-29에 배포되어 mock(R-001·R-002)은 이미 걷혔다. 재현이 필요한 응답 다섯 가지는 테스트 픽스처로 옮겨져 있다([`contracts/deck-api.md`](./contracts/deck-api.md) §4).
3. **홈은 탭 feature이므로 공개 표면은 `HomeNavigation.kt` 하나로 유지한다.** 홈 안에서 끝나는 전환(방 시트·액션 메뉴·가이드)은 콜백으로 내보내지 않고 전부 `HomeUiState`의 상태로 둔다.

4. **「확인」이 두 이벤트로 갈렸다**(spec 3.0.0 FR-023). 카드를 넘기는 것은 서버와 무관한 화면 상태 변화이고, 카드를 눌러 상세를 여는 것은 서버에 알리되 덱을 건드리지 않는다. **호출 경로를 아예 분리해** 이 독립을 코드에서 보이게 한다(R-012).

5. **이미 있는 것을 쓴다 — 새로 만드는 것이 셋뿐이다**(3.0.0). `다른 방 저장`의 시트는 [SYS-002]가 구현해 둔 것을 `:core:common:ui`로 승격해 쓰고(R-017), 서버 호출 둘은 `PlaceRepository`의 것을 쓰고(R-019), 툴팁의 새 위치는 `MinoTooltip`의 기존 파라미터로 표현한다(R-016). 새로 만드는 것은 **방 캐릭터 에셋 12종·완료 안내 일러스트·`ResolveRoomEntryDeckUseCase`** 다.

## 개정 요약 (3.0.0)

| 개정 항목 | 출처 | 설계 대응 |
|---|---|---|
| 탐색 축 반전 — 「한 정렬로 모든 방 → 다음 정렬」 | spec 4.0.0 FR-011·012·025 | `ResolveNextDeckUseCase` 규칙 재작성, `NextDeck.NextRoom`에 `sort` 추가 (R-014) |
| 수동 방 변경만 `꾹 Pick` 초기화 · 범위를 고른 방으로 한정 | spec 4.0.0 FR-024 | `ResolveRoomEntryDeckUseCase` 신설 (R-014) |
| 방 순회 순서 = 개인방 먼저 · 생성 오래된 순 | spec 4.0.0 FR-012 | `HomeDeckRepository.getRoomSummaries`가 순서를 확정 (R-014) |
| `다른 방 저장`이 「방 선택 시트」로 교체 · 복수 선택 | spec 4.0.0 FR-005 | [SYS-002]의 `RoomPickerSheet`를 `:core:common:ui`로 승격 (R-017), 시트 상태를 둘로 분리 |
| **방 캐릭터가 방 색별 12 variant** | 시안 `4306:63718` | 에셋 12종을 `:feature:home`에 두고 `RoomColor` 대응표로 고른다 (R-015) |
| **완료 안내 일러스트·문구 교체** | 시안 `5073:101117` | 209×209 일러스트 재export + 문구 변경. **spec FR-014 개정이 선행 조건** (R-018) |
| **방 전환 툴팁 위치** | 시안 `2809:143382` | `MinoTooltip(position = Right, align = Center)` + 조립부 오프셋 (R-016) |
| 방 선택 시트의 방 썸네일 | 시안 `4306:63731` | **변경 없음** — 기존 `room_thumbnail_*` 에셋과 같은 그림임을 대조로 확인 |
| 서버 호출 계약 중복 제거 | 설계 중 발견 | `recordPlaceOpened`·`savePinToRoom`을 걷고 `PlaceRepository`를 쓴다 (R-019) |

현재 `:feature:home`은 spec 3.0.0 기준으로 **이미 구현되어 머지된 상태**다. 이 계획은 그 구현을 위 표대로 옮기는 것이 된다 — 새로 세우는 것이 아니라 축이 바뀐 자리를 갈아 끼운다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin · Jetpack Compose (버전은 `gradle/libs.versions.toml`이 단일 출처)

**주요 의존성**: Hilt · Ktor Client · AndroidX DataStore · Navigation Compose. 모듈 구성은 [`architecture/modularization.md`](../../architecture/modularization.md)를 따른다.

**저장소**: DataStore (마지막으로 보던 방 · 가이드 닫은 이력 **둘만**, spec 3.0.0 FR-022·FR-019, R-004). 마지막으로 보던 방은 서버에 올리지도, 홈 진입 시 묻지도 않는다.

**테스트**: JUnit — `:core:domain`의 JVM 단위 테스트(전환 규칙)와 `:feature:home`의 ViewModel 테스트(화면 상태). 상세는 [`quickstart.md`](./quickstart.md) §3.

**대상 플랫폼**: Android

**프로젝트 유형**: mobile-app — 다중 Gradle 모듈

**성능 목표**: 카드 전환이 프레임 드랍 없이 이어지고, 애니메이션 중 입력으로 카드가 두 장 넘어가지 않는다(SC-005). 정량 기준은 spec이 정하지 않았으므로 새로 만들지 않는다.

**제약 조건**: 덱 전환 판정은 **전환이 일어나는 시점에 매번** 수행한다(FR-011) — 방이나 정렬에 진입할 때 계산해 두고 재사용하지 않는다. 빈 덱을 노출하지 않는다(EC-013). 두 확인 이벤트는 서로를 건드리지 않는다(FR-023) — 넘김은 서버를 부르지 않고, 상세 진입은 덱을 바꾸지 않는다. **탐색 축은 (정렬, 방) 격자이고 자동 전환은 정렬을 유지한다**(FR-011·012·025) — 정렬을 되감는 것은 수동 방 변경뿐이며 그 탐색 범위는 고른 방 하나다(FR-024).

**규모/범위**: 화면 1장(홈 탭) + 오버레이 4종(홈 방 시트·방 선택 시트·액션 메뉴·가이드) + 상태 5종(덱·완료·빈 상태·로딩·에러). FR 24건 · UX 4건 · SC 8건 · TS 41건 · EC 21건.

**참조 API 문서**: `https://api.gguk.org/api-docs-json` · 조회 시점 **2026-09-03T17:49:28+09:00** · Team MINO API 1.0.0 · 오퍼레이션 28개. 홈이 쓰는 네 계약(`GET /rooms/{roomId}/cards` · `GET /rooms` · `POST /pins/{pinId}/accesses` · `POST /pins/{pinId}/duplicate`)이 **모두 배포되어 있다.** 대조 결과와 조회 이력은 [`contracts/deck-api.md`](./contracts/deck-api.md) §1이 소유한다.

## 헌법 준수 확인 게이트 (Constitution Check)

[`docs/constitution.md`](../../constitution.md) 2.1.0 기준. 아래 표는 1.0.0~2.0.0의 판정이고, **3.0.0 재판정에서 원칙 IV 한 칸이 조건부로 내려갔다** — 표 아래 인용 블록을 따른다.

| 게이트 | 근거 원칙 | Phase 0 전 | Phase 1 후 | 판정 근거 |
|---|---|---|---|---|
| 규칙 본문을 복제하지 않고 링크로 지목했는가 | I. SSOT | ✅ | ✅ | 이 문서와 부속 산출물이 규약 본문을 옮겨 적은 곳이 없다. 컴포넌트 배치·에러 처리·Figma 판정은 전부 소유 문서 링크 |
| 의존 방향이 바깥 → 안쪽인가 | II. 레이어 경계 | ✅ | ✅ | `:feature:home` → `:core:domain` ← `:core:data`. 도메인은 Android를 모른다. `ResolveNextDeckUseCase`는 순수 함수 |
| feature가 다른 feature를 의존하지 않는가 | II | ✅ | ✅ | 밖으로 나가는 전환 3종을 전부 콜백으로 셸에 넘긴다([`contracts/home-ui.md`](./contracts/home-ui.md) §1) |
| 바인딩을 구현 소유 모듈이 갖는가 | II | ✅ | ✅ | `DeckDataSourceModule`·`HomeDeckRepositoryModule`을 `:core:data`의 `di/`에 둔다. `:app`은 조립만 |
| 되돌리기 어려운 결정을 기록했는가 | III | ✅ | ✅ | [`research.md`](./research.md)에 결정 19건을 누적했다. ADR 승격 후보는 R-017 |
| 명세가 구현에 선행했는가 | IV. Spec-First | ✅ | ⚠️ | **3.0.0에서 조건부.** spec 4.0.0이 머지된 뒤 착수했으나 완료 안내 문구 하나가 spec을 앞질렀다 — §복잡도 추적 #1 |
| 근거 없는 빈틈을 지어내지 않았는가 | IV | ✅ | ✅ | 3.0.0 재조회 기준 **서버 미구현 0건**. 남은 디자인 결손(`brown` variant 부재)을 봉합하지 않고 §복잡도 추적과 [`contracts/home-ui.md`](./contracts/home-ui.md) §5에 드러냈다 |
| 산출물이 `docs/specs/{feature-name}/`에 모이는가 | IV | ✅ | ✅ | 전부 `docs/specs/home-deck-exploration/` 아래 |
| 템플릿을 먼저 복사했는가 | IV | ✅ | ✅ | `mino-sdd/template/plan-template.md`를 복사한 뒤 제자리 편집했다 |
| 디자인 토큰 판정 절차를 따르는가 | 기술 표준 | ✅ | ✅ | 값이 일치하는 토큰이 있으면 토큰, 없으면 Figma 실측값. 판정은 [`conventions/figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md) |
| 컴포넌트·에셋의 소속 모듈을 규약대로 정했는가 | 기술 표준 | ✅ | ✅ | [`contracts/home-ui.md`](./contracts/home-ui.md) §5. 판정은 [`conventions/component-asset-placement.md`](../../conventions/component-asset-placement.md) |

**정당화가 필요한 위반: 1건** — 완료 안내 문구가 spec 개정을 앞질렀다(원칙 IV). 아래 「복잡도 추적」에 기록했다.

> **3.0.0 재평가** — 11개 게이트를 다시 판정했고 **한 칸이 뒤집혔다.**
>
> | 게이트 | 3.0.0 판정 | 사유 |
> |---|---|---|
> | 명세가 구현에 선행했는가 (IV) | ⚠️ **조건부** | 완료 안내 **문구**를 시안 값으로 정했으나 spec FR-014·PRD Flow E는 아직 옛 값이다. spec 개정을 이 plan의 **선행 조건**으로 걸어 순서를 지킨다(R-018) |
> | 규칙 본문 복제 안 함 (I) | ✅ | 시안 실측값을 [`home-ui.md`](./contracts/home-ui.md) §5 한 표에 모으고 다른 문서는 그 표를 지목한다 |
> | 되돌리기 어려운 결정 기록 (III) | ✅ | R-014~R-019로 6건 기록. `RoomPickerSheet` 승격(R-017)은 **ADR 승격 후보**다 |
> | 의존 방향 · feature 간 무의존 (II) | ✅ | 홈이 `:feature:sharereceiver`를 의존하지 않는다 — 시트를 `:core:common:ui`로 올려서 푼다(R-017) |
> | 컴포넌트·에셋 소속 (기술 표준) | ✅ | 캐릭터 12종은 사용처가 홈뿐이라 `:feature:home`(R-015), 시트는 세 번째 사용처라 승격(R-017). 판정은 [`component-asset-placement.md`](../../conventions/component-asset-placement.md) |
> | SSOT (I) | ✅ | 같은 서버 호출에 계약이 둘이던 것을 걷었다(R-019) |
>
> 나머지 게이트는 2.0.0과 같은 근거로 통과다. 모듈 경계·의존 방향·바인딩 소유는 바뀌지 않았다.

> **2.0.0 재평가** — 11개 게이트를 다시 판정했고 뒤집힌 것은 없다. 이번 개정이 건드린 것은 도메인 계약의 시그니처와 호출 시점이며, 모듈 경계·의존 방향·바인딩 소유는 그대로다. `GeoPoint`를 새로 만들지 않고 [`core/map`](../../../core/map/README.md)이 소유한 타입을 쓰는 것이 원칙 I(SSOT)에 부합한다.

> **한 가지 유의**: 헌법 §개발 워크플로와 품질 게이트의 「에이전트 행동 규칙」은 *"에이전트는 `git commit`을 직접 실행하지 않는다 … 커밋은 사용자가 실행한다"* 를 MUST로 둔다. 이 계획의 구현 단계에서도 그대로 적용된다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/home-deck-exploration/
├── spec.md              # 4.0.0 (/mino-spec 산출물)
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물 — 결정 19건 (누적)
├── data-model.md        # Phase 1 산출물
├── quickstart.md        # Phase 1 산출물
├── contracts/
│   ├── deck-api.md      # 서버 API 대조 이력 + 걷어낸 mock 기록 + 협의 항목
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
    ├── ResolveNextDeckUseCase.kt        # 자동 전환 — 격자 순회 (R-003·R-014, 규칙 재작성)
    └── ResolveRoomEntryDeckUseCase.kt   # 수동 방 변경 — 고른 방 한정 (R-014, 신설)

core/data/src/main/java/team/mino/core/data/
├── datasource/
│   ├── DeckRemoteDataSource.kt          # getCards(roomId, sort, lat?, lng?)
│   ├── DeckRemoteDataSourceImpl.kt      # 실서버 (mock은 2026-08-29에 걷혔다)
│   ├── HomePreferencesLocalDataSource.kt
│   └── di/DeckDataSourceModule.kt
├── network/dto/response/
│   └── CardResponse.kt
├── repository/
│   ├── HomeDeckRepositoryImpl.kt        # getRoomSummaries가 순회 순서를 확정 (R-014)
│   ├── HomePreferencesRepositoryImpl.kt
│   ├── mapper/DeckMapper.kt
│   └── di/HomeRepositoryModule.kt

feature/home/src/main/java/team/mino/feature/home/
├── HomeNavigation.kt                    # 유일한 public 표면 (기존 파일 수정)
└── main/
    ├── screen/  HomeRoute.kt · HomeScreen.kt
    ├── vm/      HomeViewModel · HomeUiState · HomeIntent · HomeSideEffect
    ├── model/   HomePhase.kt · HomeTooltip.kt · RoomColorUiModel.kt
    ├── res/     drawable-{m,x,xx}hdpi/home_room_character_{12색}.webp   # 신설 (R-015)
    │            drawable-{m,x,xx}hdpi/home_all_exhausted_illustration.webp  # 재export (R-018)
    └── component/
        ├── HomeTopShell.kt · SortChipRow.kt          # RoomCharacter가 방 색을 받는다 (R-015)
        ├── CardDeck.kt · PlaceCardItem.kt · CardActionMenu.kt
        ├── HomeRoomSheet.kt · HomeTooltipOverlay.kt  # 툴팁 position/align 교체 (R-016)
        ├── HomeGuideOverlay.kt
        └── AllExhaustedContent.kt · EmptyContent.kt

core/common/ui/src/main/java/team/mino/core/common/ui/component/
└── roompicker/                          # :feature:sharereceiver에서 승격 (R-017)
    └── RoomPickerSheet.kt · RoomPickerList.kt · RoomPickerHeader.kt · RoomPickerActionArea.kt
```

**`:core:common:ui`로의 승격은 두 feature를 건드린다.** 옮기는 작업과 `:feature:sharereceiver`의 참조를 고쳐 잇는 작업을 `tasks.md`에서 독립 작업으로 세운다 — 홈 화면 작업에 묶으면 홈을 되돌릴 때 [SYS-002]가 함께 깨진다.

**구조 결정**: 안드로이드 앱 단일 저장소 구조를 그대로 쓴다. 서버는 별도 저장소이므로 이 트리에 두지 않는다. 홈은 **탭 feature**이므로 `HomeActivity`·`HomeShell`·`HomeNavHost`·`di/`를 만들지 않는다 — 셸은 `:feature:main`이 소유하고 진입은 `homeGraph()` 등록 함수로 이뤄진다([`architecture/feature-module.md`](../../architecture/feature-module.md) §1·§2).

화면이 한 장이라 `main/` 하나만 둔다. 방 시트·액션 메뉴·가이드는 별도 Route가 아니라 같은 화면 위의 오버레이이므로 `component/`에 들어간다.

## 복잡도 추적 (Complexity Tracking)

| # | 위반 | 원칙 | 정당화 | 해소 조건 |
|---|---|---|---|---|
| 1 | 완료 안내 **문구**를 시안 값(`모든 장소를 다 봤어요!`)으로 정했으나 spec FR-014·PRD [SCR-003] Flow E는 `꾹 눌러둔 장소를 모두 둘러봤어요`를 값으로 못박고 있다 | IV. Spec-First | 같은 화면에서 일러스트는 새 시안, 문구는 옛 spec으로 갈라 두는 것이 더 나쁘다고 판단했다(R-018). 사용자가 「시안 문구로 바꾸고 spec도 이어서 개정」을 택했다 | **PRD → spec 개정이 머지되면 해소된다.** `tasks.md`에서 이 문구 작업을 개정 이후로 잠그고, 그때까지 [`quickstart.md`](./quickstart.md) §4.9의 3번은 **미검증**으로 남긴다 |

**미결 협의 1건** — 시안 컴포넌트셋 두 벌(`Home_Avatar`·`Room Thumbnail_HOME`)에 `brown` variant가 없다. 서버 `color` enum과 `RoomColor`·`MinoRoomColor`에는 있는 색이므로 **디자인 쪽의 공백**이다(R-015). 협의 전까지 `RoomColor.BROWN`은 `black` variant로 떨어뜨려 두고, 그 사실을 [`quickstart.md`](./quickstart.md) §4.9에 검증 항목으로 남겼다. 이것은 헌법 위반이 아니라 **디자인 입력의 결손**이라 위 표에 넣지 않았다.

> **2.0.0까지 이 자리에 있던 mock 항목(R-001)은 사라졌다.** `/cards`가 배포되어 mock이 걷혔으므로 정당화할 것이 남지 않는다.
>
> **`RoomPickerSheet` 승격(R-017)도 위반이 아니다.** [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §2.1이 정한 승격 기준(두 번째 사용처)을 세 번째 사용처에서 뒤늦게 따르는 것이고, 새 구조를 들이지 않는다. 다만 두 feature를 건드리는 결정이라 **ADR 승격 후보**로 보고한다.
