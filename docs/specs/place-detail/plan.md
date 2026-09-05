# 구현 계획: 장소 상세 & 코멘트 (Place Detail & Comments)

**대상 스펙 경로**: `docs/specs/place-detail`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 4.0.1

**최초 작성일**: 2026-08-28

**최종 수정일**: 2026-09-02

**버전**: 2.1.1

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

장소 상세([SCR-006])는 지도 위 `Half`(369dp)/`Full` 2단 바텀시트로 장소의 요약·대표 이미지·코멘트를 보여주고, 코멘트를 쓰고 지우며, 외부 지도·원문으로 나가고, 다른 방에 공유하고, 저장된 방을 바꾸는 화면이다.

**이 개정(2.0.0)은 구조를 바꾼다.** plan 1.1.0이 세운 진입형 Activity를 해체하고, 장소 상세를 **저장 탭 안으로 편입**한다. 함께 이번 라운드(이슈 #270)가 실 API를 연결한다.

### 이번 개정의 뼈대 넷

**첫째, 장소 상세는 `:feature:room`의 세 번째 시트 분기다.** 별도 Activity도 Route도 아니다. 방 리스트 Route(`RoomMain`) 하나가 지도(`RoomListMap`) 한 벌을 그리고, 그 위에서 **리스트 · 방 상세 · 장소 상세**의 시트만 갈아끼운다. 근거는 [research.md D17](./research.md).

- plan 1.1.0의 진입형 선택([research.md D1](./research.md))은 근거가 둘이었고 **둘 다 소멸했다** — 사용자가 이번에 편입으로 다시 결정했고, "#161 미머지라 독립 진행 가능"이라는 유일한 실질 이점은 #161이 머지되며(`2537a6a3`) 사라졌다.
- 머지된 room-detail은 [ADR 2026-08-29](../../adr/2026-08-29-entry-feature-for-cross-tab-immersive-screen.md)가 기각한 대안보다 **더 멀리 갔다** — 방 리스트와 방 상세를 Route로도 나누지 않고 `selectedRoomId` 하나로 갈랐고, 그 이유를 `RoomNavigation.kt` KDoc에 적어 뒀다("지도를 하나의 컴포지션에서 계속 살려 두어야 카메라가 리셋되지 않는다"). ADR이 "측정값이 아니라 예상"이라 적어 둔 지도 재생성 비용이 **회피해야 할 실제 결함으로 확인된 것**이다.
- 새 메커니즘이 없다. `RoomListScreen`은 이미 `detailContent: (@Composable BoxScope.() -> Unit)?` 슬롯을 갖고 `RoomDetailScreen`·`RoomDetailRoute`는 이미 `BoxScope` 확장이다. 장소 상세는 **그 슬롯의 세 번째 분기**다.

**둘째, spec이 저장 탭을 귀착지로 못박는다.** TS-007(알림 진입)·TS-037(홈 진입)이 [나가기]를 "알림 탭이 아니라"·"홈 탭이 아니라" **그 방의 방 상세 `Half`로** 보내라고 규정한다. 어느 진입점이든 사용자가 남는 자리는 저장 탭이다. 편입 구조에서 FR-009는 `selectedPinId = null` 한 줄이 된다 — 방 상세 시트가 그 아래에 그대로 살아 있기 때문이다.

**셋째, 화면의 식별자는 여전히 `pinId`다.** 서버는 장소를 **핀 = (장소, 방) 쌍**으로 다룬다. 「지금 보고 있는 방」(FR-027)이 별도 상태가 아니라 `pinId` 안에 내포된다는 [research.md D4](./research.md)의 판단은 그대로 유효하며, 편입 구조에서 오히려 더 잘 맞는다 — [저장된 방] 전환이 `selectedPinId` 교체 하나로 끝난다.

**넷째, 이번 조회에서 서버가 두 갭을 닫았다.** 2026-09-01 대조 결과 plan 1.1.0이 남긴 세 개의 미해결 중 **둘이 해소되고 하나는 소멸했다.**

| plan 1.1.0의 미해결 | 이번 상태 |
|---|---|
| FR-023~025 저장된 방 전환 — "서버가 대상 방의 `pinId`를 안 준다" | **해소.** `GET /rooms?showHasPlaceId=`가 `matchedPinId`를 함께 준다 → 구현한다 |
| FR-005 장소분류 라벨 — "핀 상세에 `labelGroup`이 없다" | **소멸.** spec 4.0.0이 라벨 노출을 없애고 그 자리를 등록자 닉네임으로 바꿨다. `createdBy.nickname`이 이미 있다 |
| FR-009 [나가기] 목적지 — "방 상세 화면이 없다" | **해소.** #161 머지 + 편입 구조 |

> **2.0.1(PATCH)은 설계를 바꾸지 않았다.** spec이 4.0.1로 올랐으나 그 개정은 기준 PRD를 11.1.0으로 맞춘 헤더 변경뿐이고, PRD 11.1.0의 유일한 델타([SYS-009] Nudge 바텀시트 2주 스누즈)는 이 화면 밖이다. 서버 스키마도 2026-09-02 재조회에서 동일함을 확인했다.
>
> **2.1.0(MINOR)이 FR-028의 빈틈 하나를 닫는다.** 2.0.0은 코멘트 작성 시각을 "도메인이 `Instant`를, 화면이 표기를" 갖는 데까지 정하고 **경과를 재는 기준 시각(「지금」)을 어디서 얻는지는 비워 두었다.** 이번 개정이 `kotlin.time.Clock` 주입으로 그 자리를 채운다 — 결정은 [research.md D26](./research.md), 계약은 [place-detail-main-contract.md §6.1](./contracts/place-detail-main-contract.md)이 갖는다. 기존 설계는 그대로 유지되고 작업이 붙기만 한다.

> **2.1.1(PATCH)은 계약의 오류를 고친다.** 「저장된 방 시트」 계약이 FR-024와 **정반대**로 적혀 있었다 — 지금 보고 있는 방을 목록에서 **빼야** 하는데 「선택 상태로 표시된다」로 쓰여 있었고, 같은 오류가 `quickstart.md` §7과 `tasks.md` T093에도 번져 있었다. 설계가 바뀐 것이 아니라 **문서가 spec을 잘못 옮긴 것**이라 PATCH다. 함께 FR-024가 못박은 치수(442dp·312dp)를 계약에 실어, T093이 `[SYS-003]`의 다른 시트(`RoomShareSheet` 676dp)를 따라가지 않게 했다.

### spec 4.0.0이 새로 요구하는 것

| 요구사항 | 처리 | 서버 |
|---|---|---|
| **FR-005** 헤더에 등록자 닉네임(라벨 제거) | `PlaceLabel` 타입과 `PlaceDetail.label` 삭제. `registrant.nickname` 사용 | `createdBy.nickname` — 대응 있음 |
| **FR-028** 코멘트 작성 시각 4구간 표기 | `PlaceComment.createdAt: kotlin.time.Instant` 추가. 구간 판정은 feature의 UI 매핑이 하고, 기준 시각은 주입한 `Clock`이 공급한다(2.1.0, [D26](./research.md)) | `createdAt` — 대응 있음 |

상세 근거는 [research.md](./research.md), 데이터 형태는 [data-model.md](./data-model.md), 계약은 [contracts/](./contracts/), 검증 절차는 [quickstart.md](./quickstart.md)를 참조.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin (버전 카탈로그와 `mino.android.*`/`mino.kotlin.*` 컨벤션 플러그인 기준, [`docs/constitution.md`](../../constitution.md) 「기술 표준과 제약」)

**주요 의존성**: Jetpack Compose · Hilt · Ktor(`:core:data`의 `HttpClient`) · `:core:map`(`MinoMap`) · `:core:design-system` · `:core:common:android`(MVI) · `:core:common:ui`(`MinoScaffold`·`CollectSideEffect`·`CollectDomainError`·`LocalBottomNavVisibility`·`RoomMapPin`) · `:core:navigation`(`PlaceDetailRequestHolder` 신설) · `:core:domain`(`PlaceDetail`·`PlaceComment` 기존, `PlaceLabel` **삭제**, `RoomSummary`·`RoomRepository` 확장) · `:core:error-handling`

**저장소**: 없음. 원격 조회·전송만 한다. 코멘트 초안조차 방 전환 시 버리는 것이 FR-025의 결정이다.

**테스트**: `:feature:room`이 이미 `RoomListViewModelTest`와 `fake/` 트리를 갖고 있어 그 형태를 따를 수 있다. **구조 전환이 `RoomListViewModel`의 상태 분기를 건드리므로 기존 테스트가 회귀 방어선이 된다.** 어디까지 늘릴지는 `tasks.md`가 정한다.

**대상 플랫폼**: Android (Jetpack Compose)

**프로젝트 유형**: mobile-app — **Gradle 모듈이 하나 줄어든다**(`:feature:placedetail` 삭제). 기존 `:core:domain`·`:core:data`·`:core:navigation`·`:core:common:ui`·`:feature:room`·`:feature:main`에 파일을 더하고 고친다.

**성능 목표**: [spec.md SC-002](./spec.md) — 장소명·주소 길이, 이미지 장수, 코멘트 건수가 달라져도 `Half` 시트 높이는 369dp에서 변하지 않는다. [SC-001](./spec.md) — 드래그 1회로 코멘트 영역 도달. **편입이 더하는 목표**: 방 상세 ↔ 장소 상세 전환에서 지도 타일이 다시 로드되지 않는다([quickstart.md §4-2](./quickstart.md)).

**제약 조건**: `Half` 369dp는 고정 dp다(FR-001). 코멘트 본문이 전문 노출(FR-021)이라 시트 콘텐츠가 하나의 스크롤 축을 공유하고 입력 영역이 그 축의 마지막에 놓인다. 코멘트 목록 API가 **역방향 페이징**이라 화면의 오름차순 배치와 방향이 반대다([research.md D11](./research.md)). **편입이 더하는 제약**: 지도·카메라·지도 컨트롤의 소유자가 `RoomListViewModel` 하나여야 한다 — 장소 상세가 자기 카메라 상태를 들면 방 상세가 겪었던 "[현재 위치]를 눌러도 지도가 안 움직이는" 실기기 결함을 재현한다([research.md D25](./research.md)).

**규모/범위**: 화면 1개(저장 탭의 세 번째 시트 분기), 도메인 모델 2종 확장 + 1종 삭제, Repository 2종 실구현 + 1종 확장, **모듈 1개 삭제**, `:core:navigation` 신규 공개 API 1종(`PlaceDetailRequestHolder`) 및 기존 1종 삭제(`PlaceDetailLauncher`), `:feature:room` 신규 DI 모듈 1종(`Clock` 제공, 2.1.0), 서버 엔드포인트 6개 연동

**참조 API 문서**: <https://api.gguk.org/api-docs-json> (Team MINO API 1.0.0, 오퍼레이션 **27개**) — 대조 시점 **2026-09-01T21:46:23+09:00**. 대조 결과는 [contracts/place-api.md](./contracts/place-api.md)·[contracts/comment-api.md](./contracts/comment-api.md)가 소유한다.
> plan 1.1.0의 조회 시점은 2026-08-28T22:54:07+09:00(25개)이었다. 그 사이의 변화는 [place-api.md §0](./contracts/place-api.md)이 표로 갖는다.
> **2.0.1 재확인 — 2026-09-02T00:07:00+09:00.** 오퍼레이션 27개로 그대로이고, 이 설계가 쓰는 7개 오퍼레이션(`GET /pins/{pinId}` · `POST /pins/{pinId}/accesses` · `POST /pins/{pinId}/duplicate` · `GET /rooms` · 코멘트 3종)의 요청·응답 스키마가 위 대조 시점과 **동일하다**. `matchedPinId`·`createdBy.nickname`·`createdAt`·`canDelete`와 `content` `maxLength: 200`, `nickname` `2~15`, `roomIds` `minItems: 1`, 13색 팔레트 enum이 모두 그대로다. 계약 문서는 손대지 않았다 — 위 대조 시점이 스키마 전사의 근거이고, 이 줄은 그것이 아직 유효하다는 확인이다.

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

| 원칙 | 판정 | 근거 |
|---|---|---|
| I. SSOT | PASS | 편입이 **중복을 줄인다** — 지도·카메라·지도 컨트롤·방 목록이 각 한 벌이 된다(1.1.0에서는 두 벌이었다). `PlaceLabel` 삭제로 아무도 읽지 않는 타입이 사라진다. 방 목록은 새 타입 없이 `RoomSummary`에 두 필드만 늘린다([research.md D24](./research.md)). |
| II. 레이어 경계 | PASS | `:feature:room` → `:core:*` 방향만 있고 역방향이 없다. **feature 모듈끼리 여전히 서로를 모른다** — `:feature:home`과 `:feature:room`은 `:core:navigation`의 `PlaceDetailRequestHolder`만 공유하며, 그 계약을 공용 모듈에 두는 것이 [ADR 2026-08-01](../../adr/2026-08-01-single-module-navigation-contract.md)이 지키려는 방향 그대로다. |
| III. 결정 기록 | **조건부 PASS** | [ADR 2026-08-29](../../adr/2026-08-29-entry-feature-for-cross-tab-immersive-screen.md)가 뒤집힌다. 그 ADR 자신이 `Proposed` 상태로 "#161이 머지되어 두 패턴이 같은 코드베이스에 놓이기 전에는 확인할 수 없다. **어긋나면 두 결정을 화해시키는 새 ADR로 대체한다**"고 적어 둔 경로다. **완료 보고에서 승격을 제안한다**(이 스킬이 ADR을 직접 쓰지 않는다). |
| IV. Spec-First | PASS | [spec.md](./spec.md) 4.0.0이 CREATED로 닫힌 뒤 이 개정을 시작했다. |
| V. 컨벤션 게이트 | PASS | **2.1.0의 `Clock` 바인딩은 요구하는 ViewModel을 가진 `:feature:room`이 `ViewModelComponent`에 설치한다** — 구현을 가진 모듈이 바인딩을 갖는다는 [ADR 2026-08-02](../../adr/2026-08-02-di-binding-ownership.md) 방향이고, `ShareReceiverResourcesModule`이 같은 판단(요구처가 한 ViewModel뿐이면 전역 그래프에 올리지 않는다)을 KDoc으로 남긴 선례다. 탭 feature 골격([feature-module.md 3장](../../architecture/feature-module.md)), 탭 그래프 편입([feature-navigation.md 3장](../../architecture/feature-navigation.md)), 데이터 레이어 형태([core/data README](../../../core/data/README.md)), API Service 배치([ADR 2026-08-28](../../adr/2026-08-28-api-service-owned-per-server-tag.md)), 응답 봉투([ADR 2026-08-27](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)), DI 바인딩 소유([ADR 2026-08-02](../../adr/2026-08-02-di-binding-ownership.md)), 에러 처리([error_handling.md](../../conventions/error_handling.md))를 따른다. 브랜치는 이슈 #270의 base(`feature/270-place-detail-api/base`)로 [base-branch.md](../../conventions/base-branch.md)에 맞는다. |

**Phase 1 설계 후 재확인**: [data-model.md](./data-model.md)·[contracts/](./contracts/)를 확정한 뒤에도 위 판정은 그대로다. **2.0.1에서 여섯 원칙을 다시 훑었고 판정 변동은 없었다** — spec 4.0.1이 요구사항을 건드리지 않아 게이트가 보는 근거가 그대로였기 때문이다. **2.1.0의 `Clock` 주입도 판정을 바꾸지 않는다** — 새 바인딩이 하나 늘지만 소유 규칙(원칙 V)과 레이어 방향(원칙 II)을 그대로 따르고, 기준 시각이 상태 한 곳에서만 나오게 되어 오히려 SSOT(원칙 I)에 맞는다. **1.1.0에서 남아 있던 가용성 리스크 셋 중 둘이 해소되고 하나가 소멸했다**(요약 §넷째). 남은 항목은 둘이다.

| 남은 항목 | 성격 | 처리 |
|---|---|---|
| 아바타 색 enum이 엔드포인트마다 다르다 | 서버팀 협의 | Mapper가 두 자리를 같은 13색 팔레트로 해석하고 모르는 값은 `null`(기본 아바타) — [place-api.md §1.3](./contracts/place-api.md) |
| [SYS-003] 방 선택 시트의 내부 규칙 | `[TBD]` | spec §3.2가 소유권을 [SYS-003]에 위임한 상태 그대로 둔다 — [research.md D13](./research.md) |

두 항목 모두 원칙 위반이 아니라 **이 spec이 소유하지 않은 결정**이다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/place-detail/
├── plan.md               # 이 파일
├── research.md           # Phase 0 산출물 — 결정 이력을 누적한다(D1~D25)
├── data-model.md         # Phase 1 산출물
├── quickstart.md         # Phase 1 산출물
├── contracts/
│   ├── place-detail-entry.md          # 진입·복귀 계약 (place-detail-launcher.md를 대체)
│   ├── place-detail-main-contract.md  # UiState · Intent · SideEffect · 소유권 경계
│   ├── place-repository.md            # 도메인 Repository 2종 + RoomRepository 델타
│   ├── place-api.md                   # 핀 상세 · 접근 기록 · 복제 · 방 목록 대조
│   └── comment-api.md                 # 코멘트 조회 · 작성 · 삭제 대조
└── tasks.md              # /mino-task 산출물 — 이 개정으로 전면 재작성 필요
```

### 소스 코드 (Repository Root 기준)

```text
# 모바일(Android, 다중 Gradle 모듈) — docs/architecture/modularization.md 기준

settings.gradle.kts                # include(":feature:placedetail") 삭제
app/build.gradle.kts               # implementation(projects.feature.placedetail) 삭제

feature/placedetail/               # ★ 모듈째 삭제 — 화면은 :feature:room으로 옮긴다

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/
│   ├── PlaceDetail.kt             # 수정 — label 필드 삭제
│   ├── PlaceComment.kt            # 수정 — createdAt: kotlin.time.Instant 추가 (+@OptIn(ExperimentalTime))
│   ├── PlaceLabel.kt              # ★ 삭제 (research.md D21)
│   └── RoomSummary.kt             # 수정 — hasPlace · matchedPinId 추가
└── repository/
    ├── PlaceRepository.kt         # 수정 — 사실이 아니게 된 KDoc 두 문단 정리
    ├── PlaceCommentRepository.kt  # 유지 (시그니처 불변)
    └── RoomRepository.kt          # 수정 — getRooms(placeId: String? = null)

core/data/src/main/java/team/mino/core/data/
├── network/
│   ├── service/
│   │   ├── PinApiService.kt       # 기존 파일에 병합 — getPinDetail · recordAccess · duplicatePin
│   │   ├── CommentApiService.kt   # 신규 — 코멘트 3종 (comment 태그)
│   │   └── RoomApiService.kt      # 수정 — listRooms(showHasPlaceId)
│   └── dto/response/              # PinDetailResponse · CommentResponse · PaginationResponse 신규
├── datasource/
│   ├── PinRemoteDataSource.kt     # 기존 파일에 병합 (+Impl)
│   └── CommentRemoteDataSource.kt # 신규 (+Impl)
└── repository/
    ├── PlaceRepositoryImpl.kt         # 신규
    ├── PlaceCommentRepositoryImpl.kt  # 신규
    ├── RoomRepositoryImpl.kt          # 수정
    ├── mapper/PlaceDetailMapper.kt    # 신규
    ├── mapper/PlaceCommentMapper.kt   # 신규
    └── di/                            # 바인딩 2건 추가

core/navigation/src/main/java/team/mino/core/navigation/
├── entry/PlaceDetailRequestHolder.kt   # ★ 신규 (contracts/place-detail-entry.md §3)
└── activity/launcher/
    ├── PlaceDetailLauncher.kt          # ★ 삭제
    └── ExtraTag.kt                     # 수정 — EXTRA_PLACE_DETAIL_PIN_ID 삭제

feature/main/src/main/java/team/mino/feature/main/
├── MainActivity.kt                # 수정 — launchPlaceDetail → holder.request + 탭 전환
└── MainNavHost.kt                 # 수정 — onNavigateToPlaceDetail의 목적지 교체

feature/room/src/main/java/team/mino/feature/room/
├── di/
│   └── PlaceDetailClockModule.kt   # ★ 신규 — kotlin.time.Clock 제공 (ViewModelComponent, research.md D26)
├── main/
│   ├── screen/RoomListScreen.kt   # 수정 — 시트 세 갈래 분기, 지도 컨트롤 판정 확장
│   ├── screen/RoomListRoute.kt    # 수정 — placeDetailContent, BackHandler, 바텀바 판정, 요청 홀더 구독
│   ├── component/RoomListMap.kt   # 수정 — 선택 핀(selected) 반영
│   ├── model/MapPinUiModel.kt     # 수정 — selected 필드
│   └── vm/                        # 수정 — selectedPinId, 진입·복귀·전환 인텐트
├── detail/vm/RoomDetailSideEffect.kt  # 수정 — NavigateToPlaceDetail(placeId → pinId)
├── detail/screen/RoomDetailRoute.kt   # 수정 — 그 이펙트를 실제로 배선(현재 -> Unit)
└── placedetail/                   # ★ 신규 패키지 — :feature:placedetail의 main/ 이식
    ├── screen/    PlaceDetailRoute.kt (BoxScope 확장) · PlaceDetailScreen.kt · …Preview.kt
    ├── vm/        PlaceDetailViewModel(@AssistedInject pinId) · UiState · Intent · SideEffect
    ├── model/     PlaceSheetLevel · PlaceHeaderMode · PlaceCommentUiModel · RoomPickerItem
    └── component/
        ├── PlaceDetailSheet.kt · PlaceDetailHeader.kt · PlaceActionRow.kt
        ├── PlaceImageCarousel.kt · SheetParts.kt
        ├── PlaceCommentList.kt · PlaceCommentItem.kt · PlaceCommentEmpty.kt
        ├── PlaceCommentInput.kt · PlaceCommentMenu.kt
        ├── PlaceMapControls.kt        # [현재 위치] + [저장된 방] — RoomListViewModel에 연결
        ├── SavedRoomsSheet.kt         # ★ 신규 — FR-024 (기존 SavedRoomsButton 대체)
        └── RoomShareSheet.kt
        # PlaceDetailMap.kt · CurrentLocationButton.kt 는 이식하지 않는다 (research.md D25)
```

**`fake/`와 `PlaceDetailFakeDataModule`은 이식하지 않는다.** UI 라운드 한정 장치였고([research.md D15](./research.md)), 이번 라운드가 `:core:data` 실구현으로 교체하며 폐기한다([research.md D23](./research.md)).

**구조 결정**: 다중 Gradle 모듈 Android 앱에서 **진입형 모듈 하나를 없애고 그 화면을 탭 feature에 편입**한다. `:feature:room`이 방 리스트·방 상세·장소 상세 셋을 갖되, 셋 다 "저장 탭의 지도 위 시트"라는 한 관심사라 응집도가 유지된다. 골격은 이미 있는 `detail/`을 그대로 본뜬다 — `Route`가 `BoxScope` 확장이고 `Screen`이 컨트롤·시트만 얹으며 지도는 호출부가 소유하는 형태다.

## 복잡도 추적 (Complexity Tracking)

| 위반 사항 | 필요한 이유 | 더 단순한 대안을 기각한 이유 |
|---|---|---|
| `:feature:room` 모듈이 커진다 (화면 3개) | 지도 한 벌을 공유하려면 세 화면이 한 컴포지션 안에 있어야 한다. 모듈을 가르면 지도 상태(`mapCenter`·`mapCenterRequestId`·`mapPins`)가 모듈 경계를 가로질러 드릴링된다 | `:feature:main`이 슬롯으로 주입하는 절충안 — 기각. 모듈 경계를 지키려다 셸이 "장소 상세는 저장 탭 지도 위에 그려진다"는 배치까지 알게 되어 [feature-module.md 3장](../../architecture/feature-module.md)이 경계하는 결합이 더 깊어진다([research.md D17](./research.md)) |
| `:core:navigation`에 상태 홀더가 는다 | 탭 간 진입 인자를 Route로 나를 수 없다 — `navigateToTab`의 `restoreState = true`가 복원된 항목의 옛 인자를 되살려 새 `pinId`를 무시한다 | Route 인자 확장 — 기각. `restoreState`를 끄면 탭을 오갈 때마다 방 목록·시트 단계가 초기화된다. 같은 성격의 선례가 이미 둘 있다(`LocalBottomNavVisibility`·`ImmersiveRouteRegistry`) — [research.md D18](./research.md) |
| 이미 머지된 코드를 되돌린다 | plan 1.1.0의 진입형 구조가 근거를 잃었다([research.md D17](./research.md)). 그대로 두면 지도 2벌·컨트롤 2벌과 FR-009 위반이 영구 부채가 된다 | 현행 유지 — 기각(사용자 결정). 화면 내부(vm·component·model 약 25파일)는 거의 그대로 이식되므로 폐기되는 것은 셸·Activity·Launcher·지도 5자리다 |

**되돌리는 규모를 과소평가하지 않는다.** 다만 이 개정이 지우는 것은 **화면이 아니라 화면을 감싸던 껍데기**다 — 시트·헤더·캐러셀·코멘트·입력은 `BoxScope` 확장으로 시그니처를 바꿔 그대로 옮겨간다.
