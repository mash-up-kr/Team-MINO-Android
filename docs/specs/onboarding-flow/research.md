# 리서치: 앱 온보딩 플로우

**대상 스펙 경로**: `docs/specs/onboarding-flow` · **부속 문서**: [plan.md](./plan.md)

> 이 문서는 **누적**한다. 기존 항목을 덮어쓰거나 지우지 않고, 뒤집힌 결정은 취소선과 `재검토됨(plan X.Y.Z)` 표시를 남긴 뒤 새 항목을 덧붙인다.
>
> 각 항목은 이 feature 안에서만 유효한 선택이다. 다른 feature를 구속하는 결정은 `승격 후보`로 표시하고 [`docs/adr/`](../../adr/README.md)가 최종 소유자가 된다. **승격이 끝난 항목은 표시를 `ADR 기록됨`으로 바꾸고 그 ADR을 지목한다** — 이후 근거의 소유자는 ADR이며, 여기 남은 본문은 이 계획이 그 결정에 이른 경위의 기록이다.

> **Figma 노드 표기**: `NNNN-NNNNN`은 [MU_디자인](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8) 파일의 노드 ID다. 디자인 시스템 라이브러리 노드는 그 자리에 파일을 밝힌다. 표기 규칙은 [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md) §5.

---

## R-001. 온보딩을 진입형 feature 모듈 `:feature:onboarding` 하나로 만든다 *(plan 1.0.0)*

**Decision**: 신규 진입형 feature 모듈 `:feature:onboarding`을 만들고, 공개 표면은 `OnboardingActivity` 하나와 `:core:navigation`의 `OnboardingLauncher` 계약이다.

**Rationale**: [`feature-module.md`](../../architecture/feature-module.md) 1장의 구분 기준이 그대로 적용된다 — 온보딩은 탭 셸의 그래프에 편입되는 화면이 아니라 Activity로 독립 진입하는 플로우이고, 바텀 네비게이션을 노출하지 않는다(FR-005). 같은 문서가 "온보딩·로그인은 호출자가 하나여도 진입형"이라고 예시로 못박고 있다. [스플래시 계획](https://github.com/mash-up-kr/Team-MINO-Android/issues/149)이 남긴 `TBD-P4`(온보딩 feature 부재 → `OnboardingLauncher` 대상 없음)를 이 결정이 닫는다.

**Alternatives considered**:
- `:feature:main`에 온보딩 화면을 얹는다 — 탭 셸과 생애주기가 섞이고, 온보딩 중 바텀 네비를 감추기 위해 셸에 조건 분기가 생긴다. 기각.
- 온보딩을 스텝별 모듈로 쪼갠다 — 스텝 4개 중 2개는 이미 다른 feature의 화면이고 나머지 2개는 이 플로우 밖에서 쓰이지 않는다. 모듈만 늘고 얻는 것이 없다. 기각.

---

## R-002. 프로필 설정·공동방 생성 스텝은 만들지 않고 두 feature의 진입 계약으로 위임한다 *(plan 1.0.0)*

**Decision**: 온보딩은 프로필 설정 화면과 공동방 생성 폼을 직접 그리지 않는다. `:core:navigation`의 [`ProfileLauncher`](https://github.com/mash-up-kr/Team-MINO-Android/issues/159)·[`RoomFormLauncher`](https://github.com/mash-up-kr/Team-MINO-Android/issues/146)를 주입받아 결과를 받는 Activity 전환으로 두 스텝을 소비한다.

| 스텝 | 계약 | 진입 인자 | 소비하는 결과 |
|---|---|---|---|
| 프로필 설정 | `ProfileLauncher` | `EXTRA_PROFILE_ENTRY_POINT = PROFILE_ENTRY_POINT_ONBOARDING` | `RESULT_OK` = 저장됨 → 공동방 스텝 |
| 공동방 생성 | `RoomFormLauncher` | `EXTRA_ROOM_FORM_ONBOARDING = true` | `ROOM_FORM_OUTCOME_CREATED`(+`roomId`) → 친구 초대 / `ROOM_FORM_OUTCOME_SKIPPED` → 튜토리얼 |

**Rationale**: spec §3.2가 두 화면의 내부 규칙을 각각 `docs/specs/profile`·`docs/specs/group-room-form`의 몫으로 못박았고, 두 스펙의 계획이 이미 진입·결과 계약을 확정해 두었다. 헌법 원칙 II(feature 간 결합은 `:core:navigation` 계약 한 겹)와 원칙 I(SSOT)이 같은 답을 가리킨다. 두 계약이 **온보딩 호출자를 명시적으로 예고하고 있다** — 프로필 계약의 "온보딩 진입" 예시와 공동방 계약의 `EXTRA_ROOM_FORM_ONBOARDING`·`ROOM_FORM_OUTCOME_SKIPPED`가 그것이다. 이 계획은 그 예고된 호출자를 실제로 배선하는 첫 소비자다.

**Alternatives considered**:
- 온보딩이 두 화면을 Compose Route로 다시 그린다 — 화면이 두 벌이 되어 SSOT가 깨진다. 기각.
- 두 화면을 `:core:common:ui`로 올려 공유한다 — 화면이 각자 ViewModel·도메인 계약에 묶여 있어 [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1.2의 승격 조건을 만족하지 않는다. 기각.

---

## R-003. 스텝 오케스트레이션은 Activity 스코프 ViewModel이 갖고, Activity는 실행만 한다 *(plan 1.0.0)*

**Decision**: `OnboardingFlowViewModel`(Activity 스코프)이 현재 스텝과 전이 규칙을 소유하고 `SideEffect`로 "무엇을 열어라"를 발행한다. `OnboardingActivity`는 `ActivityResultLauncher` 등록·`Launcher` 호출·결과 전달만 한다.

**Rationale**: `registerForActivityResult`는 Activity(또는 Fragment)에서만 등록할 수 있고, [`feature-navigation.md`](../../architecture/feature-navigation.md) 1장은 "전환은 Activity가 시작하고 화면은 콜백만 올려보낸다"고 정한다. 반면 스텝 전이 규칙(FR-001·FR-003·FR-004·FR-023)은 순수 로직이라 Android 없이 검증할 수 있어야 한다. 둘을 가르면 규칙은 ViewModel에, 프레임워크 결합은 Activity에 남는다.

**Alternatives considered**:
- Activity가 스텝 상태까지 직접 든다 — 회전·프로세스 회수에서 상태가 날아가고 규칙을 JVM 테스트로 덮을 수 없다. 기각.
- 스텝 머신을 `:core:domain` UseCase 하나로 전부 옮긴다 — 전이는 사용자 조작 이벤트에 대한 반응이라 상태 보유가 필요하고, 도메인에 화면 이벤트가 흘러든다. 재개 지점 결정만 UseCase로 잘랐다(R-004). 기각.

---

## R-004. 재개 지점 결정만 도메인 UseCase로 자른다 *(plan 1.0.0)*

**Decision**: `ResolveOnboardingStepUseCase(progress: OnboardingProgress): OnboardingStep`을 `:core:domain`에 두고, 저장된 진행 상태에서 열어야 할 스텝을 계산한다. 계산 규칙은 [contracts/onboarding-progress.md](./contracts/onboarding-progress.md) §3이 소유한다.

**Rationale**: [`core/domain/README.md`](../../../core/domain/README.md) §4의 "상태 전이" 예시에 정확히 해당한다 — 저장된 값에서 다음 화면을 판단하는 비즈니스 규칙이고, 방어 규칙(`INVITE`인데 `createdRoomId`가 없으면 `TUTORIAL`)이 붙는다. Android 없이 JVM 테스트로 FR-023·EC-021·EC-022를 덮을 수 있다.

**Alternatives considered**:
- ViewModel이 `when`으로 판단한다 — `core/domain/README.md` §4의 네 조건 중 "비즈니스 규칙 없음"을 만족하지 않는다. 기각.

---

## R-005. 위임 스텝 동안 온보딩은 빈 릴레이 Route에 머무른다 *(plan 1.0.0)*

**Decision**: 온보딩 NavHost의 Route는 셋이다 — `OnboardingRelay`(배경만 그리는 빈 화면) · `OnboardingInvite(roomId)` · `OnboardingTutorial`. 프로필·공동방 스텝 동안 온보딩 Activity는 `OnboardingRelay`에 머무르고 그 위에 다른 feature의 Activity가 얹힌다.

**Rationale**: 위임 스텝에서도 온보딩 Activity는 살아 있어야 한다 — 결과를 받아야 하고(`ActivityResultLauncher`), 사용자가 앱을 백그라운드로 보냈다 돌아왔을 때 그 자리가 남아 있어야 한다(EC-005·EC-012). 살아 있는 Activity는 무언가를 그려야 하므로 빈 목적지가 필요하다. NavHost 자체는 화면이 둘뿐이어도 유지한다 — 진입 인자 복원(`toRoute`)과 화면 조회 로깅이 거기 딸려 온다([`feature-module.md`](../../architecture/feature-module.md) 4장, 프로필 계획의 같은 판단과 일치).

**Alternatives considered**:
- 위임 스텝일 때 셸이 NavHost 대신 빈 `Box`를 그린다 — 조건 분기가 셸로 올라가고, 릴레이가 Route가 아니라서 `popUpTo`로 백스택을 정리할 대상이 없어진다. 기각.
- 위임 스텝의 시작 목적지를 `OnboardingInvite`로 두고 가린다 — `roomId`가 아직 없어 인자를 채울 수 없다. 기각.

---

## R-006. 앞 스텝 복귀 차단은 백스택 정리로, 루트 뒤로가기는 `moveTaskToBack`으로 만든다 *(plan 1.0.0)*

**Decision**

| 지점 | 수단 | 근거 |
|---|---|---|
| 스텝 전환 | `navigate(다음) { popUpTo(현재) { inclusive = true } }` — 온보딩 백스택에 앞 스텝이 남지 않는다 | FR-006·TS-007·EC-003 |
| 튜토리얼 스텝 2~5 | 튜토리얼 Route의 `BackHandler(enabled = step > 1)` → 한 스텝 앞 | FR-007·TS-034·EC-014 |
| 그 밖의 온보딩 소유 지점(친구 초대·튜토리얼 스텝 1·릴레이) | 셸의 `BackHandler` → Activity의 `moveTaskToBack(true)` | FR-007·TS-035·EC-015·EC-016 |

**Rationale**: 백스택을 비우면 "되돌아갈 곳이 없다"가 구조로 보장되어 FR-006이 화면 chrome 규칙이 아니라 그래프의 성질이 된다. 그러나 백스택이 빈 상태의 시스템 뒤로가기는 기본적으로 **Activity를 종료**시키는데, spec §5는 "온보딩을 종료하지 않고 앱을 백그라운드로"를 확정했다(EC-005: 돌아오면 그 스텝이 그대로 복원된다). `finish()`는 온보딩 Activity를 없애 스플래시부터 다시 타게 하므로 확정 사항과 다르다. 따라서 명시적으로 `moveTaskToBack(true)`를 부른다. 중첩된 `BackHandler`는 안쪽의 활성 핸들러가 이기므로 튜토리얼 스텝 2~5가 셸 핸들러보다 먼저 먹는다.

**Alternatives considered**:
- `onBackPressedDispatcher`에 Activity가 콜백 하나만 등록하고 튜토리얼 스텝까지 거기서 분기한다 — Activity가 튜토리얼 내부 스텝을 알게 되어 화면 상태가 Activity로 샌다. 기각.
- 백스택을 쌓아 두고 뒤로가기만 막는다 — 막는 코드가 화면마다 필요하고, 한 곳이라도 빠지면 FR-006이 뚫린다. 기각.

**남는 구멍**: 프로필 설정·공동방 생성 스텝의 뒤로가기는 그 두 Activity가 소유한다. 두 스펙은 아직 `무반응`으로 적혀 있어 이 결정과 어긋난다 — [열린 항목 A](#열린-항목).

---

## R-007. 온보딩 진행 상태는 공유 Preferences DataStore에 둔다 *(plan 1.0.0)*

**Decision**: `OnboardingProgress`(마지막 스텝 · 만든 공동방 id · 완료 표시)를 `:core:data`가 기존 공유 `DataStore<Preferences>`(`core/data/storage/DataStoreModule`, 파일명 `mino_preferences`)에 3개 키로 저장한다. 서버에 두지 않는다.

**Rationale**: spec §4가 "온보딩 진행 상태는 서버가 아니라 이 설치 안에서 판정한다. 앱을 지웠다 다시 설치하면 처음부터"라고 못박았고, 그것이 곧 앱 프라이빗 저장소의 성질이다. [Preferences DataStore ADR](../../adr/2026-07-27-preferences-datastore-local-storage.md)이 이미 로컬 저장 수단을 정했고, 저장소 인스턴스는 파일당 하나여야 하므로 새 DataStore를 만들지 않고 기존 것을 쓴다(`DeviceIdLocalDataSource`와 같은 자리).

**Alternatives considered**:
- 서버에 온보딩 완료 플래그를 둔다 — swagger에 해당 필드가 없고, spec §4 가정("설치 안에서 판정")과 정면으로 어긋난다. 기각.
- 온보딩 전용 DataStore 파일을 새로 만든다 — 같은 앱 설치의 단일 설정 묶음을 두 파일로 가른다. 기존 파일이 이미 그 역할이다. 기각.

---

## R-008. 완료 여부의 원천은 완료 표시 하나이고, 프로필 존재 여부와 합치지 않는다 *(plan 1.0.0)* — **승격 후보**

**Decision**: FR-022가 요구하는 판정은 `OnboardingProgress.isCompleted` 단독이다. 온보딩은 이 값을 `OnboardingProgressRepository`로 밖에 열고, **스플래시가 그것을 읽어 분기한다.**

**Rationale**: spec §5가 "프로필이 있어도 완료 표시가 없으면 온보딩을 끝내지 못한 것"이라고 확정했다. 프로필 존재만으로 판정하면 중단한 사용자가 홈으로 밀려나 남은 스텝을 영영 못 본다(TS-038). 이 규칙은 온보딩 밖(스플래시)에서 소비되므로 계약을 도메인에 두어야 한다.

**다른 feature를 구속한다**: 스플래시 계획의 `ResolveSplashEntryUseCase`는 지금 **프로필 유무만** 본다. FR-022를 만족하려면 그 UseCase가 완료 표시를 함께 읽어야 한다 — [열린 항목 B](#열린-항목).

**Alternatives considered**:
- 프로필 존재를 완료로 본다 — TS-038이 그대로 실패한다. 기각.
- 온보딩 Activity가 진입할 때마다 스스로 완료 여부를 보고 홈으로 튕긴다 — 온보딩 화면이 한 프레임 보였다 사라지고, 판정 지점이 스플래시와 둘로 갈린다. 기각.

---

## R-009. 초대 링크는 `RoomRepository.getRoom()`으로 다시 확보하고, 결과 인텐트로 실어 나르지 않는다 *(plan 1.0.0)*

**Decision**: 친구 초대 스텝은 진입 시 `roomId`로 방을 조회해 `inviteCode`를 얻는다. 공동방 폼의 결과 계약은 `roomId`까지만 싣는다(그 계약을 바꾸지 않는다).

**Rationale**: EC-021이 "친구 초대 스텝에서 중단한 뒤 다시 켜면 직전에 만든 공동방의 초대 링크를 **다시 확보한다**"를 요구한다. 앱을 다시 켠 경로에는 결과 인텐트가 없으므로 어차피 조회 경로가 있어야 하고, 그 경로 하나로 두 진입(방금 만든 직후 / 재개)을 모두 덮으면 코드가 한 갈래로 닫힌다. `getRoom`은 이미 공동방 폼 계획이 확정한 계약이라 새로 만들 것도 없다.

**Alternatives considered**:
- 폼 결과에 `inviteCode`를 실어 보낸다 — 재개 경로를 덮지 못해 조회 경로가 어차피 필요하고, 다른 feature의 결과 계약을 이 feature 사정으로 넓히게 된다. 기각.
- 온보딩이 방 생성 API를 직접 호출한다 — 폼이 이미 생성 소유자다. 두 번 만들게 된다(UX-001 위반). 기각.

---

## R-010. `Room` 도메인 모델에 `inviteCode`를 더한다 *(plan 1.0.0)*

**Decision**: 공동방 폼 계획이 만든 `:core:domain/model/Room.kt`에 `inviteCode: String` 필드를 추가하고, `:core:data`의 `RoomResponse`·`RoomMapper`도 함께 넓힌다.

**Rationale**: 그 계획의 `data-model.md` §2가 `inviteCode`를 뺀 이유를 "이 feature가 쓰지 않는다"로 적고 **"다른 feature가 필요로 할 때 필드를 더한다"**고 열어 두었다. 온보딩이 그 다른 feature다. swagger `Room.inviteCode`(최대 16자)가 서버 계약이므로 매핑 지점도 이미 정해져 있다.

**Alternatives considered**:
- 초대 전용 도메인 모델 `RoomInvitation`을 새로 만든다 — 필드 하나 때문에 모델이 둘이 되고, `getRoom` 응답에서 이미 함께 오는 값을 나누어 담게 된다. 기각.
- 온보딩이 DTO를 직접 본다 — [`core/domain/README.md`](../../../core/domain/README.md) §5의 "DTO가 경계를 넘지 않는다"를 어긴다. 기각.

---

## R-011. 초대 링크 문자열 조립은 도메인 인터페이스 + 데이터 구현으로 가른다 *(plan 1.0.0)* — **ADR 기록됨**

> **근거의 소유자는 [초대 링크 조립 ADR](../../adr/2026-08-24-invite-link-assembly-domain-interface.md)이다**(승격: plan 1.0.1). 아래는 이 계획이 그 결정에 이른 경위이며, 다른 feature가 따라야 할 규칙은 ADR을 읽는다.

**Decision**: `:core:domain`에 `InviteLinkBuilder`(순수 Kotlin 인터페이스)와 `GetInviteLinkUseCase`를 두고, 호스트를 아는 구현 `InviteLinkBuilderImpl`은 `:core:data`가 갖는다. 화면은 완성된 링크 문자열만 받는다.

**Rationale**: 링크는 `https://<host>/r/{inviteCode}` 꼴이라 **코드는 서버가 주고 호스트는 빌드 설정이 안다.** 호스트는 flavor마다 달라질 수 있고, 이 저장소는 이미 `HttpClient`의 baseUrl을 flavor BuildConfig로 다루고 있어 같은 자리가 있다. 도메인이 URL 상수를 들면 `:core:domain`이 배포 환경을 알게 되고, feature가 조립하면 서버 소유의 링크 형식이 UI로 샌다.

**다른 feature를 구속한다**: 방 상세의 초대 바텀시트([SYS-006] Flow B)가 같은 링크를 쓴다. 두 번째 사용처가 생길 때 이 자리가 그대로 쓰이도록 도메인에 두었다.

**남는 구멍**: 호스트 값과 경로 형식의 소유자는 [SYS-010]이다 — [열린 항목 D](#열린-항목).

**Alternatives considered**:
- Repository가 `getInviteLink(roomId): String`을 노출한다 — 데이터 접근 계약에 표현 형식이 섞이고, 링크가 필요 없는 호출자도 그 함수를 보게 된다. 기각.
- feature가 `"https://gguk.org/r/" + code`를 만든다 — 형식이 화면 코드에 박혀 [SYS-010]이 확정될 때 고칠 자리가 화면마다 흩어진다. 기각.

---

## R-012. 초대 링크 확보 실패는 화면을 유지한 채 액션 시점에 알린다 *(plan 1.0.0)*

**Decision**: 링크 확보 실패를 `UiState.inviteLink = null`로 남긴다. 에러 화면으로 갈아 끼우지 않고, 두 액션이 눌리면 `DomainErrorEmitter`로 실패를 알린 뒤 재확보를 시도한다. 우상단 [X]는 언제나 동작한다.

**Rationale**: [`error_handling.md`](../../conventions/error_handling.md) §5의 두 통로 중 어느 쪽도 그대로 맞지 않는 경계 사례다 — 링크는 주 데이터지만, EC-008이 요구하는 처리는 "에러 화면 + 재시도"가 아니라 **"화면은 그대로 두고 잘못된 링크를 내보내지 않으며 실패를 알린다"**이고, 사용자는 이탈 수단으로 앞으로 나아갈 수 있어야 한다. 같은 문서 §8이 "경계 사례의 분류는 첫 적용 화면 구현 시 결정한다"고 열어 둔 자리이므로, 이 화면의 요구(EC-008 + UX-002)에 맞춰 State 보관 + 액션 시점 알림으로 정한다.

**Alternatives considered**:
- 에러 화면 + 재시도 버튼 — 디자인(`2314-95550`)에 그런 상태가 없고, 안내 문구와 두 액션이 사라져 UX-002가 깨진다. 기각.
- 진입 즉시 스낵바 — 사용자가 아직 아무것도 누르지 않았는데 실패를 알린다. 눌렀을 때 알리는 편이 EC-008의 문장에 정확히 맞는다. 기각.

---

## R-013. 튜토리얼 화면은 ViewModel을 두지 않는다 *(plan 1.0.0)*

**Decision**: 튜토리얼 5스텝은 `TutorialStep` enum(문구·이미지·번호)과 `PagerState`만으로 그린다. 완료·건너뛰기는 콜백으로 `OnboardingFlowViewModel`에 올린다.

**Rationale**: 이 화면에는 도메인 호출도, 비동기 상태도, 복원 대상도 없다 — EC-022가 "튜토리얼 내부 스텝 위치까지 복원하지는 않는다"고 못박아 스텝 인덱스는 화면 수명과 같다. [`feature-module.md`](../../architecture/feature-module.md) 2장이 `vm/`을 필수로 두지 않고 "액션이 있을 때만" `XIntent`를 두라고 적은 것과 같은 결의 판단이다. 완료 기록(FR-024)은 온보딩 스텝 단위의 사건이므로 플로우 ViewModel의 몫이다.

**Alternatives considered**:
- 튜토리얼 전용 ViewModel — 상태가 `pagerState.currentPage` 하나이고 그것을 ViewModel이 들면 Pager와 상태가 이중화된다. 기각.

---

## R-014. 튜토리얼 스텝 이동은 dot 탭과 좌우 스와이프 둘 다 받는다 *(plan 1.0.0)*

**Decision**: `HorizontalPager`(페이지 5) + `MinoPaginationDots`. dot 탭은 `animateScrollToPage`, 스와이프는 Pager 기본 동작이다.

**Rationale**: FR-016이 dot 탭을 명시하고, spec §4 가정이 "dot 인디케이터를 둔 5화면 캐러셀의 통상 조작"으로 좌우 스와이프를 기본값으로 두었다. Pager를 쓰면 EC-017·EC-018(양 끝에서 더 못 넘어감)이 별도 코드 없이 성립한다. UX-004(문구·이미지·dot이 함께 바뀐다)도 페이지 인덱스라는 단일 원천에서 나온다.

**Alternatives considered**:
- 상태 하나로 화면을 갈아 끼운다(`AnimatedContent`) — 스와이프를 직접 구현해야 하고 EC-017·EC-018을 손으로 막아야 한다. 기각.

---

## R-015. `Pagination/Dots`는 `:core:design-system`이 신설한다 *(plan 1.0.0)*

**Decision**: `MinoPaginationDots`를 `:core:design-system`에 만든다. 온보딩은 소비만 한다.

**Rationale**: [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1.2의 판정 그대로다 — `search_design_system` 조회 결과 `Pagination/Dots`는 **MU_Wanted Design System (Community)** 라이브러리의 컴포넌트셋(`assetType: component_set`)이다. 사용처가 지금 하나여도 디자인 시스템의 자산이다.

**Alternatives considered**:
- 온보딩이 점 5개를 직접 그린다 — 위 규약의 "거꾸로는 성립하지 않는다" 조항 이전에, Figma 디자인 시스템 컴포넌트라는 사실이 먼저 판정을 끝낸다. 기각.

---

## R-016. 튜토리얼 예시 이미지·친구 초대 일러스트는 `:feature:onboarding`이 갖는다 *(plan 1.0.0)*

**Decision**: 튜토리얼 스텝 이미지 5종과 친구 초대 화면의 캐릭터·구름 배경을 `:feature:onboarding`의 `res/drawable-{mdpi,xhdpi,xxhdpi}`에 WebP로 둔다.

**Rationale**: [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1.1의 기본값이다 — 사용처가 이 feature의 화면뿐이다. 포맷·밀도 규칙은 [래스터 이미지 배치·포맷 ADR](../../adr/2026-08-19-raster-image-placement-and-format.md)이 소유한다.

**남는 구멍**: 튜토리얼 **스텝 5의 예시 이미지가 Figma에 아직 없다** — `3798-167139`의 해당 프레임은 `꾹 앱 지도화면에 핀마커가 표시되어있는 장면`이라는 설명 텍스트만 든 자리표시자다. [열린 항목 C](#열린-항목).

**Alternatives considered**:
- `:core:common:ui`에 둔다 — 두 번째 사용처가 없다. 선제 승격은 같은 문서가 금지한다. 기각.

---

## R-017. 복사 완료 토스트는 셸의 스낵바 호스트로 띄우고, 40dp 위치를 `MinoScaffold`가 소유한다 *(plan 1.0.0)* — **ADR 기록됨**

> **근거의 소유자는 [토스트 소유자 ADR](../../adr/2026-08-24-snackbar-host-owned-by-mino-scaffold.md)이다**(승격: plan 1.0.1). 그 ADR이 스플래시 계획과의 소유권 갈림도 함께 닫았다 — 아래 "다른 feature를 구속한다" 항목이 가리키던 충돌이다.

**Decision**: 친구 초대 Route가 `LocalSnackbarHostState`로 토스트를 띄운다. 그러려면 `:core:common:ui`의 `MinoScaffold`가 두 가지를 갖춰야 한다 — (1) 스낵바 호스트를 `MinoSnackbar`로 그린다, (2) 호스트를 스크린 하단에서 40dp 띄운다. 이 변경은 `MinoScaffold` 한 파일에서 끝난다.

**Rationale**: 지금 `MinoScaffold`는 M3 기본 `SnackbarHost`를 그대로 열고 있어 Figma의 `Snackbar/Snackbar`(`2370-112921`)와 다르게 보인다. 40dp 오프셋(UX-003)은 이 spec만의 요구가 아니라 [`SCR-001` 토스트 표출 위치 규칙]에서 온 앱 공통 규칙이고, 스낵바 호스트를 소유한 곳이 `MinoScaffold` 하나뿐이므로(같은 문서 §6: "Activity당 하나") 그 자리가 SSOT다. 화면마다 오프셋을 얹으면 규칙이 화면 수만큼 복제된다.

**다른 feature를 구속한다**: 스플래시 계획은 같은 40dp를 **Screen 컴포저블의 몫**으로 적어 두었다. 두 결정이 부딪힌다 — [열린 항목 E](#열린-항목).

**Alternatives considered**:
- 친구 초대 화면이 자체 오버레이로 토스트를 그린다 — 앱 공통 규칙이 화면 로컬 코드가 되고, 셸의 호스트와 두 개의 토스트 체계가 공존한다. 기각.
- `MinoScaffold`에 `snackbarHost` 슬롯을 열어 화면이 넘긴다 — 미처리 예외 안내까지 그 슬롯을 타므로, 화면이 슬롯을 잘못 채우면 §6의 안전망이 조용히 깨진다. 기각.

---

## R-018. `MinoTopNavigation`은 소비만 하고 신설을 이 계획이 떠맡지 않는다 *(plan 1.0.0)*

**Decision**: 친구 초대·튜토리얼 두 화면의 상단 바는 `:core:design-system`의 `MinoTopNavigation`을 쓴다. 이 계획은 그 컴포넌트를 만들지 않는다.

**Rationale**: 두 화면 모두 Figma에서 `Top Navigation/Top Navigation` 인스턴스다(`2314-95568`·`3798-167080`). [프로필 계획]과 [공동방 폼 계획]이 **각각 이 컴포넌트의 신설을 이미 선언해 두었다.** 세 계획이 같은 파일을 만들면 먼저 머지되는 하나만 유효하고 나머지는 충돌한다.

**남는 구멍**: 신설 소유자가 둘로 갈려 있다 — [열린 항목 F](#열린-항목).

**필요한 variant**: 친구 초대는 `우측 [X]`, 튜토리얼 스텝 1~4는 `제목 튜토리얼 + 우측 텍스트 [건너뛰기]`, 스텝 5는 `제목만`이다. 세 조합이 그 컴포넌트의 속성 축으로 표현되는지는 구현 착수 시 노드 대조로 판정한다([figma-design-fidelity.md](../../conventions/figma-design-fidelity.md) §2).

---

## R-019. 홈 진입은 `MainLauncher`를 `withFinish = true`로 부른다 *(plan 1.0.0)*

**Decision**: 완료 표시를 먼저 기록하고, `mainLauncher.launch(activity, withFinish = true)`로 메인 탭을 연다.

**Rationale**: 온보딩은 완료 후 되돌아올 대상이 아니다(FR-021·TS-005·TS-036). [`core:navigation` README](../../../core/navigation/README.md) §2.1이 정한 `withFinish`의 용법("되돌아오는 것 자체가 플로우상 잘못된 진입")에 정확히 해당한다. 기록이 먼저인 이유는 전환 직후 프로세스가 죽어도 완료가 남아야 하기 때문이다(SC-003).

**Alternatives considered**:
- `finish()`를 직접 부른다 — 전환과 종료가 두 지점으로 갈려 순서가 어긋날 수 있다. 계약이 이미 한 호출로 묶어 준다. 기각.

---

## R-020. 공동방 스텝이 `RESULT_CANCELED`로 돌아오면 같은 스텝을 다시 연다 *(plan 1.0.0)*

**Decision**: 온보딩 진입의 공동방 폼은 `CREATED`·`SKIPPED` 둘 중 하나로 끝나야 하지만, `RESULT_CANCELED`가 오면 스텝을 넘기지 않고 공동방 생성 스텝을 다시 연다.

**Rationale**: 공동방 폼 spec FR-022가 온보딩에서의 이탈 수단을 없앴으므로 정상 경로에서는 이 결과가 오지 않는다. 그러나 오면 온보딩은 빈 릴레이 화면에 갇힌다 — 사용자에게 아무 조작 수단이 없는 상태다. 같은 스텝을 다시 여는 편이 UX-002("항상 나아갈 수단이 있다")를 지킨다. 방을 만들지 않았으므로 다시 열어도 중복 생성이 없다(UX-001).

**Alternatives considered**:
- 튜토리얼로 넘긴다 — 사용자가 건너뛰기를 누르지 않았는데 건너뛴 것으로 처리된다. FR-003과 다르다. 기각.
- 아무것도 하지 않는다 — 조작 수단 없는 빈 화면이 남는다. 기각.

---

## 열린 항목

이 계획이 스스로 닫을 수 없는 것들이다. 각 항목은 **닫는 주체**가 이 feature 밖에 있다.

| # | 무엇 | 이 계획의 처리 | 닫는 조건 |
|---|---|---|---|
| **A** | **백프레스 정책이 세 spec에서 어긋난다.** 온보딩 spec 1.1.0 FR-007은 프로필 설정·공동방 생성 스텝에서 `앱을 백그라운드로`를 요구하지만, 프로필 spec EC-001은 `화면을 그대로 유지`(무반응), 공동방 폼 spec FR-022·EC-015는 `이 제스처로도 폼을 벗어날 수 없다`(무반응)로 적혀 있다 | 온보딩이 소유한 지점(친구 초대·튜토리얼·릴레이)만 FR-007대로 만든다. 두 위임 화면의 동작을 이 계획이 바꾸지 않는다 | 두 spec의 `/mino-spec` 개정. 온보딩 spec 1.1.0의 정책 변경(2026-08-20)이 두 문서에 아직 전파되지 않았다 |
| **B** | **스플래시 분기 근거에 완료 표시가 없다.** 스플래시 계획의 `ResolveSplashEntryUseCase`는 프로필 유무만 본다 — FR-022·TS-038이 요구하는 판정과 다르다 | `OnboardingProgressRepository`를 도메인에 열어 소비 가능한 상태로 둔다([R-008](#r-008-완료-여부의-원천은-완료-표시-하나이고-프로필-존재-여부와-합치지-않는다-plan-100--승격-후보)) | 스플래시 계획의 개정. 그 UseCase가 완료 표시를 함께 읽어야 한다 |
| **C** | **튜토리얼 스텝 5의 예시 이미지가 Figma에 없다.** `3798-167139`의 이미지 자리는 설명 텍스트만 든 자리표시자다 | 스텝 1~4 에셋만 확정하고, 스텝 5는 에셋 슬롯을 비운 채 구조를 만든다 | 디자이너가 스텝 5 이미지를 그린다 |
| **D** | **초대 링크의 호스트와 경로 형식이 미확정이다.** swagger는 `gguk.org/r/{code}`를 `inviteCode` 설명 문장에만 적고, spec §3.2는 형식을 [SYS-010]의 몫으로 뒀다 | `InviteLinkBuilder` 인터페이스까지 확정하고 값은 구현 단계로 넘긴다([R-011](#r-011-초대-링크-문자열-조립은-도메인-인터페이스--데이터-구현으로-가른다-plan-100--adr-기록됨) · [ADR](../../adr/2026-08-24-invite-link-assembly-domain-interface.md)). 고칠 자리가 구현체 한 파일이다 | [SYS-010] 스펙 또는 서버팀 확정 |
| **E** | ~~**토스트 40dp의 소유자가 둘로 갈린다.** 이 계획은 `MinoScaffold`, 스플래시 계획은 Screen 컴포저블로 적었다~~ **소유자 판정은 닫혔다(plan 1.0.1)** — [토스트 소유자 ADR](../../adr/2026-08-24-snackbar-host-owned-by-mino-scaffold.md)이 `MinoScaffold`로 정했다([R-017](#r-017-복사-완료-토스트는-셸의-스낵바-호스트로-띄우고-40dp-위치를-minoscaffold가-소유한다-plan-100--adr-기록됨)) | 이 계획이 할 일은 없다 | **스플래시 계획의 개정.** 그 계획의 데이터 모델이 아직 "표출 위치는 Screen 컴포저블이 갖는다"로 적혀 있어 ADR과 어긋난다 |
| **F** | **`MinoTopNavigation`의 신설 소유자가 둘이다.** 프로필 계획과 공동방 폼 계획이 각각 신설을 선언했다 | 소비만 하고 신설하지 않는다([R-018](#r-018-minotopnavigation은-소비만-하고-신설을-이-계획이-떠맡지-않는다-plan-100)) | 먼저 머지되는 쪽이 소유자가 된다. 나머지 계획이 소비로 바뀌어야 한다 |
| **G** | **개인방(`내 장소`) 생성이 아직 아무 데서도 일어나지 않는다.** swagger는 `POST /api/v1/users`가 개인방을 함께 만든다고 적었지만, 프로필 계획 2.0.0은 원격 계층 전체를 후속으로 미뤘다 | 온보딩은 "프로필 저장이 개인방 생성을 촉발한다"는 사실만 다루므로 이 계획이 만드는 것은 없다. SC-002가 이 구간에서 검증 불가임을 [quickstart.md](./quickstart.md) §5가 든다 | 프로필의 원격 연동 작업 |
| **H** | **`RoomRepository` 뒤가 아직 인메모리 mock이다.** 공동방 폼 계획이 `DataSource` 구현만 mock으로 채웠다 | 온보딩은 계약(`getRoom`)만 소비하므로 갈아 끼우는 지점이 `@Binds` 한 줄이라는 전제를 그대로 물려받는다. mock 저장소가 `inviteCode`를 채워야 친구 초대 스텝을 눌러 볼 수 있다 | 서버 연동 작업 |
