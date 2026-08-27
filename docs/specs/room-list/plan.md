# 구현 계획: 방 리스트 탭 (Room List Tab)

**대상 스펙 경로**: `docs/specs/room-list`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 2.1.4

**최초 작성일**: 2026-08-18

**최종 수정일**: 2026-08-26

**버전**: 2.0.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

방 리스트 탭([SCR-004])은 바텀 네비게이션 `저장` 탭에 해당하는 신규 tab feature다. 지도 위 3단(`Peek`/`Half`/`Full`) 바텀시트로 내가 속한 모든 방의 장소 마커를 훑고, `Full`에서 방 카드 목록(개인방 고정 + 공동방)을 정렬해 방 상세로 진입하거나 새 공동방을 만든다. 신규 tab feature 모듈 `:feature:room`을 만들고, `Room` 도메인 모델과 `RoomRepository`를 `:core:domain`에 처음 정의하며, 공동방 생성 폼([SYS-001])으로의 전환은 아직 존재하지 않는 다른 feature 모듈을 향한 `:core:navigation` 계약으로 미리 선언한다(구현은 그 모듈의 몫).

> **2.0.0 재검토**: 방 상세([SCR-005], 이슈 #161)는 더 이상 별도 진입형 feature/모듈이 아니다 — 사용자가 PR #186·#234 리뷰에서 "저장 탭 → 방 리스트 → 방 상세는 하나의 Activity"로 결정을 뒤집으면서, `:feature:room` 모듈 안의 nested Route(`RoomDetailMain`, `detail/` 패키지)로 재설계됐다. `RoomDetailLauncher`·`EXTRA_ROOM_DETAIL_ROOM_ID` 계약은 폐기했고, 바텀 네비게이션 숨김은 `:core:navigation`의 `ImmersiveRoute` 마커 인터페이스로 다시 모델링했다. 상세 설계는 [room-detail/plan.md](../room-detail/plan.md)가 갖는다. 상세 근거는 [research.md](./research.md) D13·D14, 데이터 형태는 [data-model.md](./data-model.md), 계약은 [contracts/](./contracts/)를 참조.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin (프로젝트 표준 버전 카탈로그 `mino.android.*`/`mino.kotlin.*` 컨벤션 플러그인 기준, `docs/constitution.md` 「기술 표준과 제약」)

**주요 의존성**: Jetpack Compose · Hilt · `:core:map`(Google Maps Compose 래퍼) · `:core:design-system`(`MinoMenu`·`MinoChip` 재사용 — [research.md D11](./research.md)) · `:core:common:android`(MVI) · `:core:common:ui`(`MinoScaffold`) · `:core:navigation` · `:core:domain`

**저장소**: 없음(이 spec 범위에서는 원격 조회만 — `RoomRepository.observeMyRooms()`가 유일한 데이터 진입점, [contracts/room-repository.md](./contracts/room-repository.md)). 로컬 영속화(캐시·DB)는 요구되지 않는다 — Nudge 재노출도 상태 저장 없이 매 진입 판정([research.md D9](./research.md)). 백엔드 API는 아직 draft 단계라 `RoomRepositoryImpl`(`:core:data`, 이 spec 범위 밖) 구현 시 필드 갭을 임시 목데이터로 메운다 — [research.md D12](./research.md), [contracts/room-repository.md §구현 위치](./contracts/room-repository.md).

**테스트**: 이 저장소에 확립된 자동 테스트 컨벤션이 없다(헌법 「검증 장치의 한계」 — CI 없음, 최소 게이트는 `./gradlew :app:assembleQaDebug`). `tasks.md`에서 ViewModel 단위 테스트(JVM, `:core:domain` 순수 로직 포함) 도입 여부를 정한다.

**대상 플랫폼**: Android (Jetpack Compose)

**프로젝트 유형**: mobile-app — 다중 Gradle 모듈, 신규 tab feature 모듈 1개 추가

**성능 목표**: [spec.md SC-001](./spec.md) — 탭 진입 후 3초 이내 지도+`Peek`(사실상 `Half`, FR-001 갱신 반영) 렌더링

**제약 조건**: 시트 높이는 화면 비율이 아니라 고정 dp([UX-001](./spec.md)) — 기기 크기 무관 동일 렌더링을 구현이 보장해야 한다. 위치 권한 미허용 시에도 기본 좌표로 지도가 그려져야 한다([EC-002](./spec.md)).

**규모/범위**: 화면 1개(`RoomListMain`, 시트 3단계는 화면 상태 — [research.md D2](./research.md)), 신규 domain 모델 4종([data-model.md](./data-model.md) §1), 신규 tab feature 모듈 1개, 크로스 feature 계약 1건(미구현 대상 모듈용 — [research.md D6](./research.md)). 방 상세([SCR-005]) 전환은 크로스 feature 계약이 아니라 같은 모듈의 nested Route다([research.md D13](./research.md)).

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

| 원칙 | 판정 | 근거 |
|---|---|---|
| I. SSOT | PASS | 새 도메인 모델·계약은 이 spec이 최초 정의(중복 없음). `MinoRoomCard` 등 기존 `:feature:sample` 프로토타입 재사용을 결정했고(중복 구현 안 함), 승격은 구현하지 않고 ADR로 제안만 한다([research.md D4](./research.md)). |
| II. 레이어 경계 | PASS (2.0.0 재확인) | `:feature:room` → `:core:domain`/`:core:navigation`/`:core:design-system`/`:core:map` 방향만 있고 역방향·feature 간 직접 의존이 없다. 방 상세([SCR-005])는 더 이상 크로스 feature 전환이 아니라 `:feature:room` 내부 nested Route(`RoomDetailMain`)라 애초에 계약이 필요 없다([research.md D13](./research.md)). `RoomFormLauncher` 계약만 `:core:navigation`에 남아 `:feature:room`이 대상 feature를 모르게 한다(D6, `feature-navigation.md` 1장). 바텀 네비게이션 숨김은 새 `ImmersiveRoute` 마커 인터페이스(`:core:navigation`)로 판정해, `:feature:main`이 `:feature:room`의 구체 화면 구성을 몰라도 되게 했다([research.md D14](./research.md), room-detail plan 소유). |
| III. 결정 기록 | 조건부 PASS | `MinoRoomCard` 등 승격은 다른 feature에도 구속력을 갖는 결정이라 ADR 후보 — 완료 보고에서 제안(이 plan이 직접 쓰지 않음, `mino-plan` SKILL.md 「research.md와 ADR의 경계」). `ImmersiveRoute`(D14)도 같은 성격이며, 그 완료 보고는 [room-detail/plan.md](../room-detail/plan.md)가 낸다. |
| IV. Spec-First | PASS | spec.md 컨펌(CREATED/PASS) 이후 이 plan을 시작했다. |
| V. 컨벤션 게이트 | PASS | 브랜치 `feature/154-room-list/plan`을 base 브랜치에서 분기(`docs/conventions/base-branch.md`). |

**Phase 1 설계 후 재확인**: 아래 「프로젝트 구조」·[data-model.md](./data-model.md)·[contracts/](./contracts/) 확정 이후에도 위 판정은 그대로 유지된다. 유일한 미해결 리스크는 원칙 II가 아니라 **가용성**(D6의 미구현 모듈 의존)이며, 이는 레이어 경계 위반이 아니라 순서 의존이라 게이트 실패로 보지 않는다 — `/mino-task`에 그 사실을 넘긴다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/room-list/
├── plan.md               # 이 파일
├── research.md           # Phase 0 산출물
├── data-model.md         # Phase 1 산출물
├── quickstart.md         # Phase 1 산출물
├── contracts/
│   ├── room-list-main-contract.md   # UiState·Intent·SideEffect·분기 규칙 (mypage-settings 계약 패턴과 동일)
│   ├── room-repository.md
│   └── navigation-launchers.md
└── tasks.md               # /mino-task 산출물 (아직 없음)
```

### 소스 코드 (Repository Root 기준)

```text
# Option 3: 모바일(Android, 다중 Gradle 모듈) — docs/architecture/modularization.md 기준

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/
│   ├── Room.kt                    # data-model.md §1
│   ├── RoomThumbnail.kt
│   ├── RoomMemberSummary.kt
│   ├── RoomListSortOption.kt
│   ├── MapMarkerSortOption.kt
│   └── PlaceCategoryFilter.kt
└── repository/
    └── RoomRepository.kt          # contracts/room-repository.md

core/navigation/src/main/java/team/mino/core/navigation/
├── activity/launcher/
│   └── RoomFormLauncher.kt        # contracts/navigation-launchers.md — RoomDetailLauncher·EXTRA_ROOM_DETAIL_ROOM_ID는 2.0.0에서 폐기(research.md D13)
└── screen/
    └── ImmersiveRoute.kt          # 신규 — 몰입 화면 마커 인터페이스(research.md D14, room-detail/plan.md 소유)

feature/room/                      # 신규 tab feature 모듈 (:feature:room)
├── build.gradle.kts
└── src/main/java/team/mino/feature/room/
    ├── RoomNavigation.kt          # RoomGraph(public) + roomGraph() 등록 함수 — 2.0.0부터 screen<RoomDetailMain> 등록도 포함(room-detail/plan.md)
    ├── main/
    │   ├── screen/  RoomListRoute.kt · RoomListScreen.kt
    │   ├── vm/      RoomListViewModel.kt · RoomListUiState.kt · RoomListSideEffect.kt · RoomListIntent.kt
    │   ├── model/   BottomSheetLevel.kt (UiState 구성 enum — RoomThumbnail 등은 domain 재사용)
    │   └── component/
    │       ├── RoomListMap.kt          # :core:map MinoMap 래핑, 마커 오버레이
    │       ├── RoomListBottomSheet.kt  # Peek/Half/Full 렌더 분기
    │       ├── RoomNudgeSheet.kt
    │       └── RoomGhostCard.kt
    └── detail/                    # 신규(2.0.0) — 방 상세([SCR-005]) nested Route. 상세 파일 목록은 room-detail/plan.md 소유

# 승격 후보(구현하지 않음, ADR 제안 대상 — research.md D4)
core/design-system/src/main/java/team/mino/core/designsystem/component/
├── roomcard/       # feature/sample의 MinoRoomCard 등이 이곳으로 이동할 후보
└── roomcolorchip/  # 이미 존재 (ADR 2026-08-14) — 참고용, 이 spec이 만들지 않음
```

**구조 결정**: Option 3(모바일, 다중 Gradle 모듈)을 그대로 쓰되 이 저장소 고유의 feature/core 레이아웃(`docs/architecture/modularization.md`)으로 구체화했다. `tests/` 트리는 이 저장소의 확립된 테스트 컨벤션이 없어(Technical Context 참고) 이번 plan에서 만들지 않으며, 필요 여부는 `/mino-task`가 판단한다.

## 복잡도 추적 (Complexity Tracking)

> 헌법 위반은 없다(위 게이트 전부 PASS). 아래는 위반은 아니지만 `/mino-task`가 반드시 알아야 하는 순서 의존이다.

| 항목 | 필요한 이유 | 단순 대안을 기각한 이유 |
|---|---|---|
| `RoomFormLauncher`를 구현 없는 대상 모듈(`:feature:roomform`)용으로 미리 선언 | [FR-007]이 요구하는 전환을 room-list 혼자 로컬로 만들면 room-form spec과 중복·충돌한다([research.md D6](./research.md)) | room-list 안에 방 생성 폼을 직접 구현 — 기각. 별도 spec/이슈로 이미 갈라져 있어 이 spec의 범위(`spec.md §3.2`)를 벗어난다. `/mino-task`가 최소 스텁 바인딩 작업을 포함해야 room-list 단독으로도 빌드·수동 검증이 가능하다([quickstart.md](./quickstart.md) 선행 조건). |
| `ImmersiveRoute` 마커 인터페이스(`:core:navigation`) 신설 — `:feature:main`의 `MainShell`이 참조해야 방 상세 진입 중 바텀 네비게이션을 숨길 수 있다 | 방 상세([SCR-005])가 2.0.0부터 `:feature:room` 내부 nested Route가 되며(D13), 탭 셸이 "이 목적지에서는 바텀바 숨김"을 판정할 방법이 필요해졌다([research.md D14](./research.md)) | `MainShell`이 `RoomDetailMain` 구체 타입을 직접 참조 — 기각. 탭 셸이 하위 feature의 화면 구성을 알게 되는 결합이 생긴다(`feature-module.md` 3장 금지 패턴). 이 항목의 실제 구현·ADR 승격 제안은 [room-detail/plan.md](../room-detail/plan.md)가 담당한다 — room-list는 D14 배경만 공유한다. |
| `RoomRepositoryImpl`(`:core:data`)이 백엔드 draft API의 필드 갭(썸네일·아바타 URL·최근 저장일·코멘트 수)을 임시 목데이터로 채워야 함 | 백엔드가 아직 `0.1.0-draft` 단계라 spec이 요구하는 필드 일부가 없다([research.md D12](./research.md)) | `Room` 도메인 모델을 draft API 형태로 축소 — 기각(spec.md가 이미 확정한 요구사항을 미확정 draft가 지배하게 됨, 헌법 원칙 IV). 이 spec은 계약·모델을 바꾸지 않고, 갭 처리를 구현 단계(`/mino-task`)의 별도 작업으로 남긴다. |
