# 구현 계획: 홈 탭 카드덱(Card Deck)

**대상 스펙 경로**: `docs/specs/home-card-deck`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 5.0.0

**최초 작성일**: 2026-08-19

**최종 수정일**: 2026-08-23

**버전**: 2.0.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

장소 목록을 **입력으로 받아** 스택 카드로 그리고, 화면 우측 영역의 스와이프로 넘기고 되돌리며, 카드 액션 메뉴와 덱 보충 버튼을 제공하는 컴포넌트를 만든다. spec 5.0.0이 "순수 카드덱 UI"로 범위를 좁혀 두었으므로 **목록 조회·정렬·방 전환은 이 계획에 없다.**

2.0.0에서 두 가지가 바뀌었다 — **카드 한 장(`MinoHomeCard`)의 소속 모듈이 `:core:design-system`으로 재판정**되었고([research.md](./research.md) D10), **`장소 더 보기`의 노출 조건에 "덱이 10장을 채웠는가"가 더해졌다**(spec 5.0.0 FR-007, [research.md](./research.md) D11).

기술적 접근은 `:feature:home`의 **상태 호이스팅 컴포넌트**다 — `CardDeck` 컴포저블 + `CardDeckState` 쌍이며 자체 ViewModel을 갖지 않는다. 목록은 파라미터로 들어오고, 확인·보충 요청·다른 방 저장은 콜백으로 나간다. 이 형태가 spec SC-005("홈 셸 없이 목록만 주입해 단독 검증 가능")를 그대로 만족시킨다.

도메인은 `:core:domain`의 `PlaceCard`·`PlaceCategoryLabel`·`CardFeedRepository` 인터페이스까지만 정한다. 근거로 삼은 서버 API의 카드 피드 엔드포인트가 `[TBD]` 상태라 `:core:data` 구현은 의도적으로 미룬다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin · Jetpack Compose (버전은 `gradle/libs.versions.toml`이 단일 출처)

**주요 의존성**: Hilt(DI), Compose Foundation 제스처 API, `:core:design-system`(토큰), `:core:common:ui`(MVI·셸)

**저장소**: N/A — 카드덱은 서버·로컬 저장소에 접근하지 않는다. 덱 진행 상태는 `rememberSaveable` 범위의 프로세스 로컬 상태다.

**테스트**: 이 저장소에 CI가 없고 Compose 테스트 관행이 정립되어 있지 않다. 검증은 [quickstart.md](./quickstart.md)의 수동 시나리오 + `@Preview`로 수행한다.

**대상 플랫폼**: Android (앱 모듈 설정을 단일 출처로 함)

**프로젝트 유형**: mobile-app — 다중 Gradle 모듈

**성능 목표**: 카드 전환이 프레임 드랍 없이 매끄럽게 보일 것. 수치 목표는 spec에 근거가 없어 두지 않는다.

**제약 조건**:
- 스와이프 인식은 **화면 우측 영역 한정**(FR-003) — 좌측 영역과 제스처가 겹치지 않아야 한다.
- 애니메이션 진행 중 입력 무시(UX-001) — 연속 입력으로 두 장이 넘어가면 안 된다.
- 네트워크 호출 금지 — 카드덱은 콜백으로만 밖과 통신한다(spec §3.2).
- `장소 더 보기`는 **10장으로 구성된 덱에서만** 노출(FR-007) — 판정 기준은 덱 구성 시점의 장수이며, 넘김·`장소 가리기`로 줄어든 장수가 아니다.
- `MinoHomeCard`는 `:core:design-system` 소속이므로 **도메인 타입을 알지 못한다** — `PlaceCategoryLabel` → `HomeCardCategory` 변환은 `:feature:home`이 갖는다.

**규모/범위**: 컴포넌트 1개(카드덱) + **디자인 시스템 이관 1건(`HomeCard` → `MinoHomeCard`)** + 도메인 모델 3개 + Repository 인터페이스 1개. 화면(Route/Screen/ViewModel) 신설 없음.

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

[`docs/constitution.md`](../../constitution.md) **2.1.0** 기준. (plan 1.0.0은 2.0.1 기준이었고, 2.1.0이 신설한 §기술 표준과 제약의 **자산 소속 모듈 조항**이 아래 II·V 판정을 바꿨다.)

| 원칙 | 게이트 | Phase 0 전 | Phase 1 후 |
|---|---|---|---|
| I. 단일 출처 문서화 | 규약 본문을 복제하지 않고 링크로 지목했는가 | ✅ | ✅ 계약·모델 문서가 서로를 링크로 참조하고 정의를 한 곳에만 둠 |
| II. 레이어 경계 | 의존 방향이 `feature` → `domain`인가, feature 간 의존이 없는가 | ⚠️ `HomeCard`가 `:feature:sample`에 있음 | ✅ `:core:design-system`으로 **이관**해 해소. 공용 모듈이므로 feature 간 의존이 애초에 성립하지 않는다([research.md](./research.md) D10) |
| II. DI 바인딩 소유 | 구현을 가진 모듈이 바인딩을 소유하는가 | ✅ | ✅ `CardFeedRepository` 인터페이스는 `:core:domain`, 구현·`@Binds`는 `:core:data` |
| III. 결정 기록 | 되돌리기 어려운 결정을 기록했는가 | ✅ | ✅ [research.md](./research.md) D1~D9 |
| IV. 명세 선행 | spec에 근거 없는 요구사항을 넣지 않았는가 | ✅ | ✅ 아래 *범위 확인* 참조 |
| V. 컨벤션 게이트 | 에러 처리·Lint·디자인 토큰·자산 배치 규약을 따르는가 | ⚠️ 배치 판정 근거가 모듈 README에 흩어져 있었음 | ✅ 배치는 [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1.2·§2.4로 판정(D10). 에러는 호출자 소비(D9). 카드 시각 스타일은 비목표라 토큰 판정 대상 없음. **`CardDeck` 시그니처는 `ComposeParameterOrder`를 따른다** — [contracts/card-deck-component.md](./contracts/card-deck-component.md) §1 정정 |

**정당화가 필요한 위반**: 없음. [복잡도 추적](#복잡도-추적-complexity-tracking) 표는 비어 있다.

**범위 확인 — spec에 근거 없는 설계가 있는가**

이번 계획은 사용자 요청("스웨거로 data layer·domain layer 설계")과 spec §3.2("장소 목록의 조회는 이 스펙이 정의하지 않는다")가 어긋나는 상태에서 시작했다. 2026-08-19 사용자 결정으로 **도메인 모델 + Repository 인터페이스까지**로 범위를 정했고, 그 근거는 다음과 같다.

- `PlaceCard`·`PlaceCategoryLabel`은 spec이 "카드덱은 장소 목록을 입력으로 받는다"고 한 그 **목록의 타입**이다. 타입이 없으면 FR-006·FR-009를 구현할 수 없으므로 spec 범위 안이다.
- `CardFeedRepository`는 그 목록이 어디서 오는지의 **계약**이다. 호출 주체가 홈 셸이라는 점을 계약에 명시해 경계를 지켰다.
- `:core:data` 구현(DTO·DataSource·Mapper)은 **설계하지 않았다.** 조회 구현은 spec 비목표이며, 근거 API도 `[TBD]`다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/home-card-deck/
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물 — 설계 결정 D1~D9
├── data-model.md        # Phase 1 산출물 — 도메인 모델·Repository·UI 상태
├── quickstart.md        # Phase 1 산출물 — 수동 검증 시나리오 A~F
├── contracts/           # Phase 1 산출물
│   ├── card-deck-component.md    # CardDeck 컴포넌트 API + 동작 계약 C-01~C-17
│   └── card-feed-repository.md   # CardFeedRepository 인터페이스 + API 갭
└── tasks.md             # /mino-task 산출물 (/mino-plan 이 생성하지 않음)
```

### 소스 코드 (Repository Root 기준)

```text
core/domain/    —  team/mino/core/domain/
├── model/
│   ├── PlaceCard.kt              # 신규 — 카드 한 장의 도메인 값
│   ├── PlaceCategoryLabel.kt     # 신규 — 장소분류 라벨 4종 enum
│   └── Registrant.kt             # 신규 — 등록자(아바타만)
└── repository/
    └── CardFeedRepository.kt     # 신규 — getCards · recordAccess

core/design-system/ —  team/mino/core/designsystem/component/homecard/
├── MinoHomeCard.kt               # 이관 — :feature:sample 에서 가져와 Mino 접두사로 개명
├── MinoHomeCardDefaults.kt       # 신규 — imageCount·avatarImageUrl 기본값 (README §6.1)
├── HomeCardPreview.kt            # 신규 — @UiModePreviews 카탈로그
└── token/
    └── HomeCardTokens.kt         # 신규 — internal. 실측 치수 매핑 (README §6.1)

feature/home/   —  team/mino/feature/home/
└── main/
    └── component/
        ├── CardDeck.kt           # 신규 — 스택 배치·제스처·덱 구성
        ├── CardDeckState.kt      # 신규 — 상태 홀더 + rememberCardDeckState
        ├── CardActionMenu.kt     # 신규 — [...] 액션 메뉴 (Figma 시안 없는 신규 UI)
        ├── LoadMoreButton.kt     # 신규 — `장소 더 보기` Floating Button
        └── PlaceCardMapper.kt    # 신규 — PlaceCategoryLabel → HomeCardCategory 변환

feature/sample/ —  team/mino/feature/sample/main/component/
└── HomeCard.kt                   # 삭제 — :core:design-system 으로 이관
```

**구조 결정 — 카드 한 장과 덱의 소속이 갈린다.** 카드 한 장은 Figma 디자인 시스템 컴포넌트셋이라 `:core:design-system`이고(D10), 덱 스택·액션 메뉴·보충 버튼은 화면 파일에만 있는 시안이라 `:feature:home`이다. 판정 축은 사용처 개수가 아니라 *Figma 디자인 시스템 컴포넌트인가* 하나다([`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1.2).

화면을 신설하지 않으므로 `:feature:home`에 `screen/`·`vm/`을 만들지 않는다. 카드덱은 [`docs/architecture/feature-module.md`](../../architecture/feature-module.md) §2의 `component/` — "Screen을 구성하는 컴포저블 단위" — 에 해당한다. 이 컴포넌트를 실제 화면에 배치하고 ViewModel을 붙이는 일은 **홈 화면 셸의 몫**이다.

`:core:data`에는 이번 계획으로 만드는 파일이 없다. `CardFeedRepositoryImpl`·DTO·Mapper는 API 확정 후 별도 작업이다([contracts/card-feed-repository.md](./contracts/card-feed-repository.md) TBD-1·2).

## 미해결 사항 (Open Questions)

| # | 내용 | 막히는 것 | 소유 |
|---|---|---|---|
| **[TBD-1]** | `Card` 응답에 **장소분류 라벨 필드가 없어** spec FR-009를 충족할 수 없다 | `:core:data` 구현 착수 | 백엔드 — 필드 추가 요청 필요 |
| **[TBD-2]** | `GET /rooms/{roomId}/cards`가 `[TBD]`(큐레이션 기획 변경 중) | DTO·Mapper 확정 | 백엔드 |
| **[TBD-3]** | `getCards`에 정렬 파라미터가 없다 | 홈 셸의 정렬 칩·자동 전환 | 홈 화면 셸 plan |

**세 항목 모두 카드덱 UI 구현을 막지 않는다.** 카드덱은 목록을 주입받아 동작하므로 고정 목록으로 완성·검증할 수 있다([quickstart.md](./quickstart.md)).

## 복잡도 추적 (Complexity Tracking)

> **헌장 준수 확인에서 정당화가 필요한 위반이 있는 경우에만 작성**

해당 없음 — 정당화가 필요한 헌법 위반이 없다.
