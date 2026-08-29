# 구현 계획: 장소 상세 & 코멘트 (Place Detail & Comments)

**대상 스펙 경로**: `docs/specs/place-detail`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 3.0.0

**최초 작성일**: 2026-08-28

**최종 수정일**: 2026-08-29

**버전**: 1.1.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

장소 상세([SCR-006], 이슈 #165)는 지도 위 `Half`(369dp)/`Full` 2단 바텀시트로 장소의 요약·대표 이미지·코멘트를 보여주고, 코멘트를 쓰고 지우며, 외부 지도 앱·원문으로 나가고, 다른 방에 공유하는 화면이다.

**두 가지가 이 plan의 뼈대다.**

**첫째, 진입형 feature다.** 사용자가 이번 대화에서 직접 정했다 — 장소 상세를 `:feature:room`의 nested Route(방 상세 화면의 상태)로 두지 않고, 자체 Activity를 갖는 **신규 모듈 `:feature:placedetail`** 으로 만든다([feature-module.md 1장](../../architecture/feature-module.md)의 진입형 골격). 대안과 그 대가는 [research.md D1](./research.md)에 그대로 기록했다. 이 선택의 실질적 이점은 방 상세(이슈 #161)가 아직 머지되지 않은 상태에서 그 모듈의 계약을 건드리지 않고 독립적으로 진행할 수 있다는 것이다 — `:feature:room`은 이 워크트리에 존재하지 않는다.

**둘째, 화면의 식별자는 `pinId`다.** 서버는 장소를 **핀(pin) = (장소, 방) 쌍** 단위로 다룬다(`GET /api/v1/pins/{pinId}`). 같은 장소가 A·B방에 저장돼 있으면 핀이 둘이고 `pinId`가 각각 다르다. 그래서 spec이 3.0.0에서 새로 도입한 「지금 보고 있는 방」(FR-027)이 별도 상태 필드가 아니라 **`pinId` 안에 이미 내포된다** — 어느 핀으로 들어왔는지가 곧 어느 방의 눈으로 보는지다([research.md D4](./research.md)).

- 화면 전환: `:core:navigation`에 `PlaceDetailLauncher`와 `EXTRA_PLACE_DETAIL_*`를 신설한다([contracts/place-detail-launcher.md](./contracts/place-detail-launcher.md)). 진입점 네 곳(지도 마커·[SCR-005] 방 상세·[SCR-003] 홈 카드·[SCR-007] 알림) 모두 `pinId`를 실어 이 Launcher로 연다.
- 바텀 네비게이션 숨김(FR-020)은 **구현할 것이 없다.** 진입형 Activity는 탭 셸(`:feature:main`) 밖에서 뜨므로 바텀바가 애초에 그려지지 않는다([research.md D3](./research.md)).
- 시트 2단(`Half` 369dp / `Full`)과 헤더 확장·축소 전환은 Route가 아니라 화면 상태다([research.md D5](./research.md)).
- 신규 도메인: `:core:domain`에 `PlaceDetail`·`PlaceComment`·`PlaceLabel`과 `PlaceRepository`·`PlaceCommentRepository`를 처음 정의한다. 방 목록은 이미 있는 `RoomSummary`·`RoomRepository`를 재사용하되 `hasPlace` 한 필드를 늘린다([research.md D8·D9](./research.md)).

**이번 범위에서 빠지는 것이 있다.** 서버 API가 spec을 덮지 못하는 지점이 세 곳 있고, 사용자 결정으로 아래처럼 닫았다. 셋 다 spec 위반이 아니라 **구현 보류**이며, 완료 보고와 [research.md](./research.md)에 근거를 남겼다.

| spec 요구사항 | 서버 현황 | 이번 plan의 처리 |
|---|---|---|
| FR-023·FR-024·FR-025 저장된 방 전환 | `GET /rooms?showHasPlaceId=`가 `roomId`·`hasPlace`만 주고 **대상 방의 `pinId`를 주지 않는다.** 전환할 핀을 특정할 수 없다 | **구현 보류.** [저장된 방] 버튼을 항상 비활성으로 노출하되 [현재 위치]와 함께 배치는 한다([research.md D16](./research.md)). 서버 협의 후 별도 개정 |
| FR-005 장소분류 라벨 | `labelGroup`이 홈 카드 응답에만 있고 **핀 상세 응답에 없다** | 서버 협의 항목으로 세우고, 그전까지 EC-005가 정한 기본값 `가볼 만한 곳`을 표시 |
| FR-009 [나가기] 목적지 | 서버 문제가 아니라 **방 상세 화면이 아직 없다**(#161 미머지) | Activity `finish()`로 호출자 복귀까지만 구현. "지금 보고 있는 방의 방 상세로" 배선은 `[TBD]` |

상세 근거는 [research.md](./research.md), 데이터 형태는 [data-model.md](./data-model.md), 계약은 [contracts/](./contracts/), 검증 절차는 [quickstart.md](./quickstart.md)를 참조.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin (버전 카탈로그와 `mino.android.*`/`mino.kotlin.*` 컨벤션 플러그인 기준, [`docs/constitution.md`](../../constitution.md) 「기술 표준과 제약」)

**주요 의존성**: Jetpack Compose · Hilt · Ktor(`:core:data`의 `HttpClient`) · `:core:map`(`MinoMap`·`GeoPoint`) · `:core:design-system`(`MinoButton`·`MinoTextButton`·`MinoChip`·`MinoMenu`·`MinoTextArea`·`MinoProfileAvatar`·`MinoAsyncImage`·`MinoRoomCheckBoxCard`·`MinoActionArea` 등 기존 컴포넌트) · `:core:common:android`(MVI `MviContainer`) · `:core:common:ui`(`MinoScaffold`·`CollectSideEffect`·`CollectDomainError`) · `:core:navigation`(`ActivityLauncher`·`MinoNavHost`, `PlaceDetailLauncher` 신설) · `:core:domain`(`PlaceDetail`·`PlaceComment`·`PlaceRepository`·`PlaceCommentRepository` 신설, `RoomSummary`·`RoomColor`·`RoomRepository` 재사용) · `:core:error-handling`(`MinoDomainException`)

**저장소**: 없음. 원격 조회·전송만 한다. 로컬 영속화 요구가 spec에 없고, 코멘트 초안조차 방 전환 시 버리는 것이 FR-025의 결정이다.

**테스트**: `:feature:profile`이 ViewModel 단위 테스트(`ProfileViewModelTest` + `FakeProfileRepository`)를 두고 있어 그 형태를 따를 수 있다. 다만 이 저장소에 전면적인 자동 테스트 컨벤션은 없다(헌법 「검증 장치의 한계」). 어디까지 테스트를 둘지는 `tasks.md`가 정한다.

**대상 플랫폼**: Android (Jetpack Compose)

**프로젝트 유형**: mobile-app — **신규 Gradle 모듈 1개**(`:feature:placedetail`)를 추가하고, 기존 `:core:domain`·`:core:data`·`:core:navigation`에 파일을 더한다.

**성능 목표**: [spec.md SC-002](./spec.md) — 장소명·주소 길이, 대표 이미지 장수, 코멘트 건수가 달라져도 `Half` 시트 높이는 369dp에서 변하지 않는다. [SC-001](./spec.md) — 드래그 1회로 코멘트 영역까지 도달.

**제약 조건**: `Half` 369dp는 화면 비율이 아니라 고정 dp다([spec.md FR-001](./spec.md)). 코멘트 본문은 높이 제한 없이 전문 노출(FR-021)이라 시트 콘텐츠 전체가 하나의 스크롤 축을 공유해야 하고, 코멘트 입력 영역이 그 스크롤 안 마지막에 놓인다([spec.md §4](./spec.md) 가정). 코멘트 목록 API가 **역방향 페이징**(최신 페이지부터)이라 화면의 오름차순 배치와 방향이 반대다([research.md D11](./research.md)).

**규모/범위**: 화면 1개(`PlaceDetailMain`, 시트 2단은 화면 상태), 신규 도메인 모델 3종 + Repository 2종, 신규 feature 모듈 1개, `:core:navigation` 신규 공개 API 1종(`PlaceDetailLauncher` + `EXTRA_*` 3건), 서버 엔드포인트 6개 연동

**참조 API 문서**: <https://api.gguk.org/api-docs-json> (Team MINO API 1.0.0, 오퍼레이션 25개) — 조회 시점 **2026-08-28T22:54:07+09:00**. 대조 결과는 [contracts/place-api.md](./contracts/place-api.md)·[contracts/comment-api.md](./contracts/comment-api.md)가 소유한다.

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

| 원칙 | 판정 | 근거 |
|---|---|---|
| I. SSOT | PASS | `PlaceDetail`·`PlaceComment`·`PlaceLabel`과 두 Repository는 이 spec이 최초 정의한다(중복 없음). 방 목록은 새 타입을 만들지 않고 기존 `RoomSummary`를 재사용하며, 필요한 `hasPlace` 한 필드만 늘린다([research.md D9](./research.md)). 색은 `RoomColor`를, 아바타는 `:core:design-system`의 `profileavatar`를 그대로 쓴다. |
| II. 레이어 경계 | PASS (설계 후 재확인 필요) | `:feature:placedetail` → `:core:*` 방향만 있고 역방향이 없다. **다른 feature 모듈을 의존하지 않는다** — 진입은 `:core:navigation`의 `PlaceDetailLauncher` 계약 한 겹이고, 호출자(`:feature:room`·`:feature:home` 등)는 이 모듈을 컴파일 타임에 모른다([ADR](../../adr/2026-08-01-single-module-navigation-contract.md)). `PlaceDetailLauncherImpl` 바인딩은 구현을 가진 이 모듈의 `di/`가 소유한다([ADR](../../adr/2026-08-02-di-binding-ownership.md)). |
| III. 결정 기록 | 조건부 PASS | D1(지도를 쓰는 몰입 화면을 진입형 Activity로 둔다)은 방 상세([SCR-005])와 상충할 수 있는 패턴이라 **ADR 후보**다 — 완료 보고에서 승격을 제안한다(이 스킬이 직접 쓰지 않음). |
| IV. Spec-First | PASS | [spec.md](./spec.md) 3.0.0이 CREATED / 체크리스트 PASS(16/16)로 닫힌 뒤 이 plan을 시작했다. |
| V. 컨벤션 게이트 | PASS | 진입형 feature 골격([feature-module.md](../../architecture/feature-module.md) 1~3장), 전환 계약([feature-navigation.md](../../architecture/feature-navigation.md) 1장), 데이터 레이어 형태([core/data README](../../../core/data/README.md)), 에러 처리([error_handling.md](../../conventions/error_handling.md))를 따른다. 브랜치는 이슈 #165의 base(`feature/165-place-detail/base`)로 [base-branch.md](../../conventions/base-branch.md)에 맞는다. |

**Phase 1 설계 후 재확인**: 아래 「프로젝트 구조」·[data-model.md](./data-model.md)·[contracts/](./contracts/)를 확정한 뒤에도 위 판정은 그대로다. 남은 리스크는 원칙 위반이 아니라 **가용성**이다 — 서버가 `pinId`(FR-024)와 `labelGroup`(FR-005)을 내려주지 않아 두 요구사항이 보류·기본값으로 닫혔고, 방 상세 부재로 FR-009 목적지가 `[TBD]`다. 셋 다 `/mino-task`로 넘긴다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/place-detail/
├── plan.md               # 이 파일
├── research.md           # Phase 0 산출물
├── data-model.md         # Phase 1 산출물
├── quickstart.md         # Phase 1 산출물
├── contracts/
│   ├── place-detail-launcher.md    # 진입 계약 — Launcher · EXTRA_* · 결과
│   ├── place-detail-main-contract.md  # UiState · Intent · SideEffect · 분기 규칙
│   ├── place-repository.md         # 도메인 Repository 2종 계약
│   ├── place-api.md                # 핀 상세 · 접근 기록 · 복제 · 방 목록 대조
│   └── comment-api.md              # 코멘트 조회 · 작성 · 삭제 대조
└── tasks.md              # /mino-task 산출물 (아직 없음)
```

### 소스 코드 (Repository Root 기준)

```text
# 모바일(Android, 다중 Gradle 모듈) — docs/architecture/modularization.md 기준

settings.gradle.kts                # include(":feature:placedetail") 추가
app/build.gradle.kts               # implementation(projects.feature.placedetail) 추가

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/
│   ├── PlaceDetail.kt             # data-model.md §1 — 신규
│   ├── PlaceComment.kt            # data-model.md §2 — 신규
│   ├── PlaceLabel.kt              # data-model.md §3 — 신규 (4종 enum, 기본값 WORTH_VISITING)
│   └── RoomSummary.kt             # 기존 파일 수정 — hasPlace 필드 추가 (research.md D9)
└── repository/
    ├── PlaceRepository.kt         # contracts/place-repository.md §1 — 신규
    ├── PlaceCommentRepository.kt  # contracts/place-repository.md §2 — 신규
    └── RoomRepository.kt          # 기존 파일 수정 — getRooms(placeId: String?) 확장

core/data/src/main/java/team/mino/core/data/
├── network/
│   ├── service/
│   │   ├── PinApiService.kt       # 기존 파일에 병합 — getPinDetail · recordAccess · duplicatePin
│   │   ├── CommentApiService.kt   # 신규 — 코멘트 3종
│   │   └── RoomApiService.kt      # 기존 파일 수정 — listRooms(showHasPlaceId)
│   └── dto/response/              # PinDetailResponse · PlaceResponse · CommentResponse · PaginationResponse 신규
├── datasource/
│   ├── PinRemoteDataSource.kt     # 기존 파일에 병합 (+Impl)
│   └── CommentRemoteDataSource.kt # 신규 (+Impl)
└── repository/
    ├── PlaceRepositoryImpl.kt         # 신규
    ├── PlaceCommentRepositoryImpl.kt  # 신규
    ├── RoomRepositoryImpl.kt          # 기존 파일 수정
    ├── mapper/PlaceDetailMapper.kt    # 신규
    ├── mapper/PlaceCommentMapper.kt   # 신규
    └── di/                            # 신규 바인딩 2건 추가

core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/
├── PlaceDetailLauncher.kt         # 신규 — interface PlaceDetailLauncher : ActivityLauncher
└── ExtraTag.kt                    # 기존 파일 수정 — EXTRA_PLACE_DETAIL_* 3건 추가

feature/placedetail/               # 신규 모듈 (진입형 골격 — feature-module.md 2장)
├── build.gradle.kts
├── src/main/AndroidManifest.xml   # PlaceDetailActivity 등록
└── src/main/java/team/mino/feature/placedetail/
    ├── PlaceDetailActivity.kt        # public — 셸 호스팅 + 진입 인자 파싱 + 결과 반환
    ├── PlaceDetailDestinations.kt    # @Serializable PlaceDetailMain(pinId)
    ├── PlaceDetailShell.kt           # MinoScaffold + navController + TrackScreenViews
    ├── PlaceDetailNavHost.kt         # MinoNavHost + screen<PlaceDetailMain>
    ├── di/
    │   ├── PlaceDetailLauncherImpl.kt
    │   ├── PlaceDetailNavigationModule.kt
    │   └── PlaceDetailFakeDataModule.kt   # UI 라운드 한정 — Phase 10(T063)에서 삭제
    ├── fake/                              # UI 라운드 한정 — Phase 10(T063)에서 패키지째 삭제
    │   ├── FakePlaceDetailData.kt         # Preview·Fake 공용 샘플 원천
    │   ├── FakePlaceRepository.kt
    │   └── FakePlaceCommentRepository.kt
    └── main/
        ├── screen/    PlaceDetailRoute.kt · PlaceDetailScreen.kt · PlaceDetailScreenPreview.kt
        ├── vm/        PlaceDetailViewModel · PlaceDetailUiState · PlaceDetailIntent · PlaceDetailSideEffect
        ├── model/     PlaceSheetLevel.kt · PlaceHeaderMode.kt · PlaceCommentUiModel.kt · RoomPickerItem.kt
        └── component/
            ├── PlaceDetailMap.kt          # MinoMap + 선택 핀 1개 + PlaceMapControls 합성
            ├── PlaceMapControls.kt        # [현재 위치] + [저장된 방] 버튼 행 (FR-023, research.md D16)
            ├── PlaceDetailSheet.kt        # Half/Full 앵커와 스크롤 축 소유
            ├── PlaceDetailHeader.kt       # 확장형·축소형 전환 (FR-003·FR-008)
            ├── PlaceActionRow.kt          # 장소보기·원문보기·다른방에 공유 가로 스크롤
            ├── PlaceImageCarousel.kt      # FR-007
            ├── PlaceCommentList.kt · PlaceCommentItem.kt · PlaceCommentEmpty.kt
            ├── PlaceCommentInput.kt       # MinoTextArea + 200자 카운터 · 등록 버튼 (FR-012~014)
            ├── PlaceCommentMenu.kt        # MinoMenu — `댓글 삭제` 한 항목 (FR-015)
            ├── SavedRoomsButton.kt        # FR-023 — 이번 범위에선 항상 비활성 (research.md D10)
            ├── CurrentLocationButton.kt   # FR-023 — 렌더링만, 동작은 [SYS-004] 소관 (research.md D16)
            └── RoomShareSheet.kt          # FR-018 [SYS-003] 호출부
```

**`fake/`와 `PlaceDetailFakeDataModule.kt`는 이번 라운드에만 있는 것이다.** API 연결 없이 UI만 만들기로 한 결정([research.md D15](./research.md))의 산물이며, Repository 인터페이스는 `:core:domain`에 정식으로 두되 구현만 feature 안 Fake가 맡는다. Phase 10에서 `:core:data`의 실구현으로 교체하면서 이 두 자리가 사라진다.

**구조 결정**: 다중 Gradle 모듈 Android 앱([modularization.md](../../architecture/modularization.md))에서 **진입형 feature 모듈을 하나 신설**한다. 골격은 `:feature:roomform`(같은 진입형)을 그대로 본뜬다 — `XActivity`·`XDestinations`·`XShell`·`XNavHost`·`di/` 다섯 파일이 모듈 루트에 있고, 화면은 `main/` 아래 `screen`·`vm`·`model`·`component`로 나뉜다. `tests/` 트리는 `tasks.md`가 테스트 범위를 정한 뒤에 만든다.

## 복잡도 추적 (Complexity Tracking)

| 위반 사항 | 필요한 이유 | 더 단순한 대안을 기각한 이유 |
|---|---|---|
| 신규 Gradle 모듈 1개 추가 | 사용자가 진입형 feature로 결정했고, 진입형은 자체 Activity·셸·그래프를 갖는 독립 모듈이어야 한다([feature-module.md 1장](../../architecture/feature-module.md)) | 기존 모듈에 얹는 대안(`:feature:room`의 nested Route)은 사용자가 기각했다([research.md D1](./research.md)). 헌법이 금지하는 구조가 아니라 규약이 정한 두 종류 중 하나를 고른 것이므로, 엄밀히는 위반이 아니라 비용 기록이다 |
