# 리서치: 방 리스트 탭 (Room List Tab)

**대상 spec**: [spec.md](./spec.md) 2.1.0 · **대상 plan**: [plan.md](./plan.md)

이 문서는 room-list feature 안에서만 유효한 설계 선택을 담는다. 다른 feature에도 구속력을 갖는 결정은 완료 보고에서 ADR 승격을 제안한다.

---

## D1. 신규 tab feature 모듈 `:feature:room`

- **Decision**: 방 리스트 탭을 `:feature:room` 모듈(탭 feature)로 신설한다. `settings.gradle.kts`에 `include(":feature:room")`을 추가하고 `:feature:main`이 의존한다.
- **Rationale**: `docs/architecture/modularization.md`에 room 관련 모듈이 아직 없다. [SCR-004]는 바텀 네비게이션 `저장` 탭 자체이므로 `docs/architecture/feature-module.md` 1장의 "탭 feature" 정의(바텀 네비게이션 탭에 해당, 셸의 그래프에 중첩 편입)에 정확히 들어맞는다. 이름은 기존 탭 feature `home`처럼 단일 영단어로 짓고, PRD가 화면을 부르는 이름(방 리스트)이 아니라 도메인 개념(방)을 딴다 — 이후 이 tab 안에 방 관련 화면이 늘어도(예: 정렬 옵션 시트 등 nested route) 모듈을 새로 파지 않기 위함.
- **Alternatives considered**: `:feature:roomlist` — 기각. `room-detail`([SCR-005])이 이 spec 범위 밖([spec.md](./spec.md) §3.2)이라 별개 모듈이 될 가능성이 높은데(→ D5), 그 경우 "list"라는 접미사가 애매해진다. 짧고 도메인 중심인 `room`이 tab feature 네이밍 관례(`home`)에 더 맞는다.
- **(plan 2.1.0에서 결정)**

## D2. 시트 단계(Peek/Half/Full)는 Route가 아니라 화면 상태

- **Decision**: `Peek`/`Half`/`Full`은 별도 Compose Navigation 목적지가 아니라 `RoomListUiState`의 `sheetLevel: BottomSheetLevel` 필드로 모델링한다. 화면은 `RoomListMain` 단일 Route 하나만 갖는다.
- **Rationale**: spec의 세 단계는 같은 지도+리스트 화면의 밀도 변화일 뿐, 뒤로가기 스택에 남길 별개의 목적지가 아니다(진입 조건·완료 조건이 모두 "같은 화면 안"으로 서술됨, [spec.md §1 유저 플로우 1](./spec.md)). `feature-module.md` 4장의 Route↔Screen 분리 원칙에 따라 상태로 표현 가능한 것을 Route로 쪼개지 않는다.
- **Alternatives considered**: 세 단계를 각각 Route로 분리 — 기각. 뒤로가기 시맨틱이 어색해지고(예: `Full`에서 시스템 뒤로가기가 `Half`로 "이동"해야 하는데 이는 네비게이션이 아니라 UI 상태 전이다), `EC-007`(방 상세 [X] 복귀 시 상태 유지)을 시작 Route 인자 하나로 깔끔히 표현할 수 없다.

## D3. Room 도메인 모델 신설 위치

- **Decision**: `:core:domain`에 `model/Room.kt`(`Room` 데이터 클래스), `model/RoomThumbnail.kt`(sealed: `ColorAndCharacter`/`Collage`), `model/RoomMemberSummary.kt`(아바타 표시용), `repository/RoomRepository.kt`(인터페이스)를 새로 둔다.
- **Rationale**: `core/domain/README.md` §3 — 비즈니스 개념을 표현하는 순수 Kotlin 타입은 `model/`, 데이터 접근 계약은 `repository/`. 기존에 Room 관련 도메인 모델이 전혀 없어(코드베이스 전수 조사 결과 없음) 이 spec이 최초로 정의한다.
- **Alternatives considered**: `:feature:room` 모듈 안에 로컬 모델로 두기 — 기각. `room-detail`([SCR-005])·`room-form`([SYS-001])·`home`([SCR-003] 방 변경 시트) 등 이미 spec/PRD에 이름이 오른 소비자가 여럿이라(→ D4·D5·D6) `:core:domain`이 SSOT여야 중복 정의를 피한다(헌법 원칙 I).

## D4. 방 카드·방 색상 칩 컴포넌트는 `:core:design-system` 승격 대상

- **Decision**: `MinoRoomCard`·`MinoRoomCheckBoxCard`·`MinoChipRoom`·`MinoHeaderRoom`(현재 `:feature:sample/main/component/`에 프로토타입으로 존재)을 `:core:design-system`으로 승격해 `:feature:room`이 그것을 그대로 쓴다. 이 plan은 승격 자체를 수행하지 않고(Plan 단계는 구현하지 않음), 완료 보고에서 ADR로 제안한다.
- **Rationale**: `docs/adr/2026-08-14-room-color-palette-in-design-system.md`가 이미 같은 논리로 방 색상 팔레트를 `:core:design-system`에 두기로 결정했다 — "소비자가 여럿이면 SSOT가 필요하다"는 근거가 `MinoRoomCard`에도 그대로 적용된다(room-list Full 방 카드, room-detail 헤더, home 방 변경 시트가 모두 유사한 방 카드/칩을 쓴다, [spec.md FR-004](./spec.md)). `core/common/ui/README.md` §5의 승격 기준("2개 이상의 feature가 실제로 공유", "특정 feature 도메인에 묶이지 않음")도 만족하되, **토큰(디자인 값) 성격이 강해 `core:common:ui`가 아니라 `core:design-system`**이 맞다(같은 README §4).
- **Alternatives considered**: `:feature:room`에 새로 복제해서 만든다 — 기각. 이미 `:feature:sample`에 검증된 구현이 있는데 중복 구현하면 두 컴포넌트가 갈라지고(헌법 원칙 I), room-detail·home이 각자 또 만들게 된다.

## D5. [SCR-005] 방 상세 전환은 Activity 기반 진입형 feature 계약으로 모델링

- **Decision**: 방 카드 선택 시([FR-006](./spec.md)) `:core:navigation`에 `RoomDetailLauncher` 인터페이스(+ `EXTRA_ROOM_DETAIL_ROOM_ID`)를 선언하고, `:feature:room`은 이 계약만 주입받아 호출한다. 구현체(`RoomDetailLauncherImpl`)는 `:feature:roomdetail`(가칭, 미구현)의 `di/`가 갖는다.
- **Rationale**: `room-detail` spec의 화면 특성 — "몰입감을 위해 [SYS-005] 바텀 네비게이션 비노출"([room-detail/spec.md](../room-detail/spec.md) 시스템 연동) — 은 `feature-module.md` 1장이 구분한 "진입형 feature"(Activity 진입점, 독립 플로우) 특징과 일치한다. 탭 그래프에 중첩된 Route로 두면 `:feature:main`의 셸이 "이 목적지에서는 바텀바 숨김"이라는 예외를 알아야 해서, 탭 셸이 하위 feature의 화면 구성을 알게 되는 결합이 생긴다(`feature-module.md` 3장이 명시적으로 금지하는 패턴).
- **미구현 의존성**: `:feature:roomdetail` 모듈은 아직 없다(이슈 #161, 별도 base 브랜치). 이 계약은 room-list 쪽에서 먼저 선언할 수 있지만, **Hilt 바인딩이 없으면 컴파일이 안 된다** — `/mino-task`가 이 작업을 room-detail 구현 이후로 순서를 매기거나, 임시 스텁 바인딩을 별도 작업으로 넣어야 한다. 완료 보고에서 이 사실을 다시 언급한다.
- **Alternatives considered**: room-detail을 `:feature:room`의 nested Route(`detail/`)로 둔다 — 기각(위 근거). tab 내부에 두면 짧게는 구현이 빠르지만 바텀 네비게이션 숨김 예외가 셸에 새고, 두 spec(#154/#161)이 이미 별도 base 브랜치로 나뉘어 있어 한 모듈에 합치면 두 PR이 같은 파일을 두고 경합한다.

## D6. [SYS-001] 공동방 생성 폼 진입도 Activity 기반 계약으로 모델링

- **Decision**: `[+]`·Nudge·Ghost Card의 "공동방 생성 폼 호출"([FR-007]·[FR-008]·[FR-009])은 `:core:navigation`의 `RoomFormLauncher` 계약(+ 결과 콜백으로 생성된 `roomId` 수신)으로 모델링한다. 구현은 `:feature:roomform`(가칭, 미구현)이 갖는다.
- **Rationale**: PRD [SYS-001]은 "생성과 편집이 같은 화면을 공유하며 온보딩(진입형 feature)·방 리스트·방 상세·홈 방 변경 시트·방 선택 시트에서 공통 호출"된다 — 이미 진입형 feature로 존재가 전제된 기능이다(`docs/adr/2026-08-14-room-color-palette-in-design-system.md`도 "첫 적용은 `:feature:roomform`"이라고 기록). room-list 혼자 폼 UI를 만들면 온보딩·room-detail이 각자 또 만들게 된다.
- **미구현 의존성**: D5와 동일한 사정 — `:feature:roomform`이 없어 Hilt 바인딩이 비어 있다. `/mino-task`가 순서를 정하거나 스텁이 필요하다.
- **Alternatives considered**: room-list 안에 방 생성 폼을 로컬로 구현 — 기각(위 근거, 그리고 [spec.md §3.2](./spec.md)가 "공동방 생성/편집 폼 자체의 입력 필드·검증 규칙은 [SYS-001] 관련 spec이 정의한다"고 이미 범위를 나눠 놓았다).

## D7. 지도 마커 필터(5종)와 방 카드 정렬(3종)은 별개 enum

- **Decision**: `MapMarkerSortOption`(전체/꾹 Pick/최신순/거리순/코멘트순, [FR-011])과 `RoomListSortOption`(전체/최근 저장 순/코멘트 순, [FR-005])을 서로 다른 `:core:domain` enum으로 둔다. 공용 옵션(`전체`·`코멘트순` 등 이름이 겹치는 항목)도 통합하지 않는다.
- **Rationale**: spec 자체가 두 값 집합을 명시적으로 다르게 정의했다(`Peek`/`Half`의 지도 정렬 드롭다운 5종 vs `Full`의 방 카드 정렬 칩 3종, [spec.md 유저 플로우 1·2](./spec.md)). 하나로 합치면 UI가 어느 화면에 몇 개를 보여줘야 하는지가 데이터로 표현이 안 되고, 항목이 겹치지 않는 값(`꾹 Pick`·`거리순`은 지도 전용, `최근 저장 순`은 카드 전용)이 실수로 다른 화면에 노출될 위험이 생긴다.
- **Alternatives considered**: 하나의 enum + "이 화면에서 보이는지" 플래그 — 기각. 화면 결합 정보를 도메인 enum에 넣는 건 UI 관심사가 domain으로 새는 것이라 레이어 경계(헌법 원칙 II)에 어긋난다.

## D8. 위치 권한 조건부 요청은 상태 저장 없이 OS 권한 조회로 판정

- **Decision**: [FR-001]의 "이미 허용됐다면 다시 묻지 않는다"는 별도의 앱 전역 상태(플래그·DataStore 등)를 두지 않고, 진입 시점에 `ContextCompat.checkSelfPermission`로 **그 순간의 OS 권한 상태**를 직접 조회해 판정한다.
- **Rationale**: OS 권한 허용 여부 자체가 이미 영속적인 단일 진실 공급원이다([SCR-003]에서 허용했든 [SCR-008]에서 허용했든 결과는 같은 OS 권한 상태로 남는다). 앱이 별도로 "허용했음"을 기록하면 OS 상태와 어긋날 자리(사용자가 앱 설정에서 껐다 켜는 경우)가 생긴다.
- **Alternatives considered**: `:core:common:android`에 권한 허용 여부를 캐싱하는 상태 보관소 추가 — 기각. OS API 호출 비용이 무시할 수준이라 캐싱의 이득이 없고, 캐시-실제 상태 불일치 버그 위험만 남긴다.

## D9. Nudge 재노출은 클라이언트 상태를 두지 않는다

- **Decision**: `[SYS-009]` Nudge의 "재진입마다 다시 표출"([FR-008])은 화면 진입 시 서버 응답(공동방 개수)만으로 판정하고, "이전에 닫았음"을 기억하는 로컬 상태(SavedStateHandle 등)를 두지 않는다.
- **Rationale**: [spec.md §5 TBD 확인 기록](./spec.md)이 이미 "닫힘을 기억하는 상태 저장 없이" 재노출된다고 확정했다. 구현이 이 결정을 뒤집지 않도록 여기서도 못박는다.

---

## D10. "현재 위치" 버튼은 room-list가 최소 구현하고, `:core:map` 승격은 별도 결정으로 미룬다

- **Decision**: `[UX-002]`(`Full` 상태에서 [SYS-004] 현재 위치 버튼 숨김)를 만족하려면 그 버튼이 먼저 존재해야 하는데, 저장소 전수 조사 결과 `:core:map`·`:core:common:*`·`:feature:sample` 어디에도 "현재 위치로 이동" 버튼 컴포넌트가 없다. `:feature:room`이 자기 화면 안에 최소 구현(아이콘 버튼 + `MinoMapCameraState`를 현재 위치로 이동)을 두고, `sheetLevel == FULL`일 때 숨긴다.
- **Rationale**: PRD [SYS-004] Flow E가 이 버튼을 "지도를 쓰는 화면에서 공통으로 노출"이라 정의해 room-detail·장소 상세도 결국 같은 버튼이 필요해지지만, 그 승격(→ D4의 `MinoRoomCard`처럼 `:core:map`으로 이동) 여부는 room-list 혼자 결정할 일이 아니다 — 아직 두 번째 소비자가 실존하지 않는 시점에 미리 공용 모듈로 만들면 `core/common/ui/README.md` §5가 경계한 "검증되지 않은 API를 공용 표면으로 굳히는" 실수가 된다.
- **Alternatives considered**: 지금 바로 `:core:map`에 공용 컴포넌트로 만든다 — 기각(위 근거, 승격 기준 미달). `[UX-002]` 자체를 이번 구현에서 보류한다 — 기각. spec.md가 이미 확정한 핵심 UX 규칙이라 임의로 미룰 근거가 없다.
- **(mino-analyze 발견 E1 대응, 2026-08-18 추가)**

---

## NEEDS CLARIFICATION 해소 현황

Technical Context에 남겼던 미확정 항목은 모두 위 결정으로 해소됐다. 남은 진짜 미확정은 D5·D6의 **미구현 크로스 feature 의존성**뿐이며, 이는 설계 공백이 아니라 다른 issue의 진행 상태에 대한 의존이다.
