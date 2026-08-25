# 구현 계획: 방 상세 (Room Detail)

**대상 스펙 경로**: `docs/specs/room-detail`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 2.1.3

**최초 작성일**: 2026-08-26

**최종 수정일**: 2026-08-26

**버전**: 1.0.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

방 상세([SCR-005], 이슈 #161)는 [SCR-004] 방 리스트에서 방 카드를 선택하면 진입하는 몰입 화면이다. 그 방에 저장된 장소만 지도·3단(`Peek`/`Half`/`Full`) 바텀시트로 탐색하고, 정렬·카테고리 필터·리스트형/카드형 뷰 전환을 제공하며, 장소 카드 더보기에서 다른 방에 공유·삭제를, 화면 더보기에서 친구 초대·방 편집·나가기를 호출한다.

**모듈 배치가 이 plan의 핵심 결정이다**: 방 상세는 신규 모듈이 아니라 room-list([SCR-004])와 **같은 `:feature:room` 모듈의 `detail/` nested Route**(`RoomDetailMain`)다. room-list의 plan 1.2.0은 D5에서 방 상세를 별도 진입형 feature(Activity)로 모델링했지만, 사용자가 PR #186·#234 리뷰에서 "방 리스트와 방 상세는 별개 기능이 아니다 — 하나의 Activity여야 한다"고 이 결정을 직접 뒤집었다(room-list plan 2.0.0, [room-list/research.md D13](../room-list/research.md)). 이 plan은 그 뒤집힌 결정 위에서 방 상세 고유의 설계를 채운다.

- 화면 전환: `RoomListMain`에서 `navController.navigate(RoomDetailMain(roomId))`, `[X]` 복귀는 `popBackStackIfResumed(entry)`(`feature-navigation.md` 2장). `RoomListMain`이 백스택에 남아 있는 동안 그 `sheetLevel` 상태가 NavHost에 그대로 보존되므로, EC-007([X] 복귀 시 방 리스트 시트 상태 유지)이 별도 계약 없이 해결된다.
- 바텀 네비게이션 숨김([FR-003] 등 몰입 화면 요구)은 `:core:navigation`의 신규 마커 인터페이스 `ImmersiveRoute`로 판정한다. `RoomDetailMain`이 이를 구현하고, `:feature:main`의 `MainShell`은 현재 목적지가 이 마커를 구현하는지만 검사해 `bottomBar` 슬롯을 조건부로 그린다 — feature 이름이나 구체 타입을 하드코딩하지 않는다.
- 공동방 생성 폼([SYS-001])은 room-list가 이미 선언한 `RoomFormLauncher`(`:core:navigation`)를 편집 모드로 재사용한다.
- 다른 방에 공유([SYS-003])·초대 시트([SYS-006])·나가기·위임([SYS-007])은 Activity 전환이 아니라 `:feature:room/detail/component/`의 내부 바텀시트·다이얼로그다 — 셋 다 고정 높이 시트/모달로, 진입형 feature의 특징(독립 플로우·Activity 진입점)과 맞지 않는다. 각 시스템의 실제 저장·초대·나가기 로직(API 계약)은 이 spec 범위 밖(SYS-003·SYS-006·SYS-007 각각의 spec 소관)이라, UI 골격만 이 plan이 정의하고 데이터 계약은 [TBD]로 남긴다.
- 신규 도메인: `:core:domain`에 `Place`(장소 카드/리스트 렌더링에 필요한 필드만)와 `PlaceRepository`를 처음 정의한다. 정렬 드롭다운([FR-005])과 카테고리 칩([FR-006])은 room-list가 이미 정의한 `MapMarkerSortOption`·`PlaceCategoryFilter`(`:core:domain`)를 그대로 재사용한다(PRD가 두 화면의 "공통 정렬 드롭다운"이라고 명시).

상세 근거는 [research.md](./research.md), 데이터 형태는 [data-model.md](./data-model.md), 계약은 [contracts/](./contracts/)를 참조.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin (프로젝트 표준 버전 카탈로그 `mino.android.*`/`mino.kotlin.*` 컨벤션 플러그인 기준, `docs/constitution.md` 「기술 표준과 제약」)

**주요 의존성**: Jetpack Compose · Hilt · `:core:map`(Google Maps Compose 래퍼, room-list와 공유) · `:core:design-system`(`MinoMenu`·`MinoChip`·room-list D4가 승격 제안한 방 카드류 재사용) · `:core:common:android`(MVI) · `:core:common:ui`(`MinoScaffold`) · `:core:navigation`(`ImmersiveRoute` 신설, `RoomFormLauncher` 재사용) · `:core:domain`(`Room`·`RoomRepository`는 room-list가 이미 정의, `Place`·`PlaceRepository`는 이 plan이 신규 정의) — 이 plan은 신규 Gradle 모듈을 추가하지 않는다. `:feature:room`은 이미 room-list plan이 만든다.

**저장소**: 없음(이 spec 범위에서도 원격 조회만 — `PlaceRepository.observePlaces(roomId)`가 유일한 데이터 진입점). 로컬 영속화는 요구되지 않는다. 백엔드 API는 room-list와 마찬가지로 draft 단계라 필드 갭은 구현 단계의 임시 목데이터로 메운다([room-list/research.md D12](../room-list/research.md)와 동일한 사정 — 이 spec은 draft를 참고만 하고 계약을 거기 맞춰 축소하지 않는다).

**테스트**: room-list와 동일하게 이 저장소에 확립된 자동 테스트 컨벤션이 없다(헌법 「검증 장치의 한계」). `tasks.md`가 ViewModel 단위 테스트 도입 여부를 정한다.

**대상 플랫폼**: Android (Jetpack Compose)

**프로젝트 유형**: mobile-app — 신규 모듈 없음, 기존 `:feature:room`에 `detail/` 패키지 추가

**성능 목표**: [spec.md SC-001](./spec.md) — 방 상세 진입 후 3초 이내 지도+`Half` 시트 렌더링

**제약 조건**: `Peek`(88dp)/`Half`(256dp) 고정 높이는 화면 비율이 아니라 고정 dp([spec.md §4](./spec.md)). 방 상세 진입 시 기본 시트 상태는 `Half`다 — room-list의 진입 기본값(`Half`, [room-list/spec.md FR-001])과 우연히 같은 값이지만 근거는 서로 다른 요구사항([spec.md §4](./spec.md))이라 상수를 공유하지 않는다.

**규모/범위**: 화면 1개(`RoomDetailMain`, 시트 3단계는 화면 상태 — room-list D2 패턴 재사용), 신규 domain 모델 1종(`Place`) + repository 1종(`PlaceRepository`), 신규 feature 내부 컴포넌트(바텀시트 2종·모달 2종), `:core:navigation` 신규 공개 API 1종(`ImmersiveRoute`), 크로스 feature 계약은 `RoomFormLauncher` 재사용 1건뿐([research.md D9](./research.md))

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

| 원칙 | 판정 | 근거 |
|---|---|---|
| I. SSOT | PASS | `Place`·`PlaceRepository`는 이 spec이 최초 정의(중복 없음). 정렬·카테고리 필터는 room-list가 이미 정의한 `MapMarkerSortOption`·`PlaceCategoryFilter`를 그대로 재사용해 새로 만들지 않았다([research.md D4](./research.md)). `BottomSheetLevel`도 room-list가 정의한 `feature/room/main/model/BottomSheetLevel.kt`를 재사용한다(중복 정의 금지). |
| II. 레이어 경계 | PASS (설계 후 재확인 필요) | `:feature:room`(`detail/`) → `:core:domain`/`:core:navigation`/`:core:design-system`/`:core:map` 방향만 있고 역방향·feature 간 직접 의존이 없다. `RoomFormLauncher` 재사용은 기존 계약을 그대로 쓰는 것이라 새 의존을 만들지 않는다. `ImmersiveRoute`는 `:core:navigation`(양쪽이 이미 의존하는 공용 모듈)에 두어 `:feature:main`이 `:feature:room`을 몰라도 되게 했다([research.md D3](./research.md)). |
| III. 결정 기록 | 조건부 PASS | `ImmersiveRoute`는 다른 feature에도 구속력을 갖는 결정이라 ADR 후보 — 완료 보고에서 제안(이 plan이 직접 쓰지 않음, `mino-plan` SKILL.md 「research.md와 ADR의 경계」). |
| IV. Spec-First | PASS | spec.md 컨펌(CREATED/PASS, 16/16 통과) 이후 이 plan을 시작했다. |
| V. 컨벤션 게이트 | 조건부 PASS | 방 상세가 room-list와 같은 `:feature:room` 모듈로 재설계되며, 이 plan은 이슈 #161의 base 브랜치(`feature/161-room-detail/base`)가 아니라 room-list의 계획 브랜치 `feature/154-room-list/plan`에서 작성했다 — `docs/conventions/base-branch.md`가 상정하는 "이슈당 base 브랜치" 원칙에서 벗어난 예외이며, 사용자가 PR #186·#234 리뷰에서 직접 지시했다(단일 Activity 요구). 두 이슈(#154·#161)의 브랜치 정리 방식은 `/mino-task` 이전에 사용자가 결정해야 한다. |

**Phase 1 설계 후 재확인**: 아래 「프로젝트 구조」·[data-model.md](./data-model.md)·[contracts/](./contracts/) 확정 이후에도 위 판정은 그대로 유지된다. 미해결 리스크는 원칙 II 위반이 아니라 **가용성**(SYS-003·SYS-006·SYS-007의 실제 로직을 정의하는 spec이 아직 없어 데이터 계약이 [TBD]로 남는 것)이며, `/mino-task`에 그 사실을 넘긴다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/room-detail/
├── plan.md               # 이 파일
├── research.md           # Phase 0 산출물
├── data-model.md         # Phase 1 산출물
├── quickstart.md         # Phase 1 산출물
├── contracts/
│   ├── room-detail-main-contract.md   # UiState·Intent·SideEffect·분기 규칙
│   ├── place-repository.md
│   └── entry-dependencies.md          # RoomFormLauncher 재사용 + SYS-003·SYS-006·SYS-007 미구현 의존성
└── tasks.md               # /mino-task 산출물 (아직 없음)
```

### 소스 코드 (Repository Root 기준)

```text
# Option 3: 모바일(Android, 다중 Gradle 모듈) — docs/architecture/modularization.md 기준
# 신규 모듈 없음. 기존 :feature:room·:core:domain·:core:navigation에 파일을 추가한다.

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/
│   └── Place.kt                   # data-model.md §1 — 신규
└── repository/
    └── PlaceRepository.kt         # contracts/place-repository.md — 신규

core/navigation/src/main/java/team/mino/core/navigation/screen/
└── ImmersiveRoute.kt              # 신규 — 빈 마커 인터페이스(research.md D3)

feature/room/src/main/java/team/mino/feature/room/
├── RoomNavigation.kt              # 기존 파일에 병합 — roomGraph()에 screen<RoomDetailMain> 등록 추가
└── detail/                        # 신규(이 plan이 만든다)
    ├── screen/  RoomDetailRoute.kt · RoomDetailScreen.kt
    ├── vm/      RoomDetailViewModel.kt · RoomDetailUiState.kt · RoomDetailSideEffect.kt · RoomDetailIntent.kt
    ├── model/   PlaceViewType.kt (리스트형/카드형 UI 상태 enum — data-model.md §2)
    └── component/
        ├── RoomDetailMap.kt            # :core:map MinoMap 래핑, 해당 방 장소만 마커로
        ├── RoomDetailBottomSheet.kt    # Peek/Half/Full 렌더 분기, 정렬·카테고리 필터·뷰 토글
        ├── PlaceCardList.kt · PlaceCardGrid.kt   # 리스트형/카드형 장소 카드
        ├── PlaceActionMenu.kt          # 장소 카드 더보기 — 다른 방에 공유·삭제
        ├── PlaceDeleteConfirmDialog.kt # UX-001 문구 고정
        ├── RoomSelectSheet.kt          # [SYS-003] 다른 방에 공유 — Full 676dp 고정
        ├── RoomInviteSheet.kt          # [SYS-006] Flow B — 424dp 고정, 참여자 목록 288dp 스크롤
        ├── RoomMoreMenu.kt             # 화면 더보기[⋮] — 방 편집(방장 전용)/나가기, Peek 상단·그 외 하단(FR-003)
        ├── RoomLeaveConfirmDialog.kt   # [SYS-007] Flow A — 일반 멤버
        └── RoomOwnerLeaveDialog.kt     # [SYS-007] Flow B — 방장(위임 모달 포함)

# feature/main — 기존 파일 수정(이 plan이 설계만 하고 구현은 /mino-task 몫)
feature/main/src/main/java/team/mino/feature/main/
└── MainShell.kt                   # bottomBar 슬롯을 ImmersiveRoute 판정으로 조건부 렌더링(research.md D3)
```

**구조 결정**: Option 3(모바일, 다중 Gradle 모듈)을 그대로 쓰되, room-list와 같은 `:feature:room` 모듈 안에 `detail/` 패키지를 추가하는 형태로 구체화했다(`docs/architecture/feature-module.md` 2장 "탭 feature" 패키지 구조 — `main/`·`detail/` 두 화면 디렉터리를 갖는 탭 feature). `tests/` 트리는 room-list와 동일한 이유로 이번 plan에서 만들지 않는다.

## 복잡도 추적 (Complexity Tracking)

> 헌법 위반은 없다(위 게이트 전부 PASS). 아래는 위반은 아니지만 `/mino-task`가 반드시 알아야 하는 순서 의존·미확정 사항이다.

| 항목 | 필요한 이유 | 단순 대안을 기각한 이유 |
|---|---|---|
| `RoomFormLauncher`를 편집 모드로 재사용하려면 `:feature:roomform`(미구현)의 구현이 먼저 필요 | [FR-012]가 방 편집 폼 호출을 요구하는데 그 폼 자체는 room-list D6이 이미 미구현 의존성으로 남긴 모듈이다 | room-detail이 편집 폼을 로컬로 또 구현 — 기각(중복 구현, `spec.md §3.2`가 이미 범위 밖으로 뺌). `/mino-task`가 room-list와 순서를 맞춰야 한다. |
| `:core:navigation`에 `ImmersiveRoute` 신설 + `:feature:main`의 `MainShell` 수정 | 방 상세가 nested Route가 되며 몰입 화면 판정을 셸이 해야 한다([research.md D3](./research.md)) | `MainShell`이 `RoomDetailMain` 구체 타입을 직접 참조 — 기각(탭 셸이 하위 feature 화면 구성을 아는 결합, `feature-module.md` 3장 금지). `/mino-task`는 이 작업을 room-detail의 다른 화면 작업과 별개로 `:core:navigation`·`:feature:main` 양쪽에 걸친 작업으로 인지해야 한다. |
| [SYS-003]·[SYS-006]·[SYS-007]의 실제 데이터 계약(방 선택 후 복제 API, 초대 링크 생성·공유, 나가기·위임 API)이 미확정 | 세 시스템 모두 전용 spec이 아직 이 저장소에 없다([research.md D10·D11·D12](./research.md)) | 계약을 지금 추측해서 확정 — 기각(헌법 원칙 IV, 근거 없는 빈틈을 지어내지 않는다). UI 골격(바텀시트·모달 컴포넌트)만 이 plan이 정의하고, 데이터 계약은 [TBD]로 남겨 후속 spec/plan이 채우게 한다. |
