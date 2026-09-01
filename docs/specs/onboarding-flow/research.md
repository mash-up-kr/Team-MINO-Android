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

## ~~R-009. 초대 링크는 `RoomRepository.getRoom()`으로 다시 확보하고, 결과 인텐트로 실어 나르지 않는다~~ *(plan 1.0.0)* — **재검토됨(plan 2.0.0)**

> **이 항목의 전제가 무너졌다.** `GET /api/v1/rooms/{roomId}` 응답에 `inviteCode`가 없다(2026-08-29 조회). 코드를 얻는 경로는 [R-021](#r-021-초대-코드는-전용-발급-엔드포인트에서-받는다-plan-200)이 갖는다. **살아남은 절반**: "결과 인텐트로 실어 나르지 않고 진입 시 확보한다"는 판단은 그대로다 — 재개 경로에 결과 인텐트가 없다는 이유는 API가 바뀌어도 유효하다.

**Decision**: 친구 초대 스텝은 진입 시 `roomId`로 방을 조회해 `inviteCode`를 얻는다. 공동방 폼의 결과 계약은 `roomId`까지만 싣는다(그 계약을 바꾸지 않는다).

**Rationale**: EC-021이 "친구 초대 스텝에서 중단한 뒤 다시 켜면 직전에 만든 공동방의 초대 링크를 **다시 확보한다**"를 요구한다. 앱을 다시 켠 경로에는 결과 인텐트가 없으므로 어차피 조회 경로가 있어야 하고, 그 경로 하나로 두 진입(방금 만든 직후 / 재개)을 모두 덮으면 코드가 한 갈래로 닫힌다. `getRoom`은 이미 공동방 폼 계획이 확정한 계약이라 새로 만들 것도 없다.

**Alternatives considered**:
- 폼 결과에 `inviteCode`를 실어 보낸다 — 재개 경로를 덮지 못해 조회 경로가 어차피 필요하고, 다른 feature의 결과 계약을 이 feature 사정으로 넓히게 된다. 기각.
- 온보딩이 방 생성 API를 직접 호출한다 — 폼이 이미 생성 소유자다. 두 번 만들게 된다(UX-001 위반). 기각.

---

## ~~R-010. `Room` 도메인 모델에 `inviteCode`를 더한다~~ *(plan 1.0.0)* — **철회됨(plan 2.0.0)**

> **서버에 그런 필드가 없다.** `GET /api/v1/rooms/{roomId}`의 응답은 `id·type·name·description·color·ownerId·createdAt·pinCount·memberCount`이고 초대 코드는 별도 리소스로 분리됐다(2026-08-29 조회). 1.0.0이 근거로 삼은 swagger 초안의 `Room.inviteCode`는 그 문서가 스스로 예고한 대로("invitation 테이블/리소스로 분리되면 이 필드는 응답에서 빠질 수 있다") 실제로 빠졌다. **`Room` 모델·`RoomResponse`·`RoomMapper`를 이 계획이 건드리지 않는다.** 대체 결정은 [R-021](#r-021-초대-코드는-전용-발급-엔드포인트에서-받는다-plan-200)·[R-022](#r-022-invitation-태그의-데이터-계층을-새로-만들고-도메인-계약은-관심사-단위로-둔다-plan-200)다. 1.0.0이 기각했던 "초대 전용 도메인 모델을 새로 만든다"가 **서버 구조가 바뀌면서 옳은 선택이 됐다** — 기각 사유("`getRoom` 응답에서 이미 함께 오는 값을 나누어 담게 된다")의 전제가 사라졌기 때문이다.

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

**남는 구멍**: 호스트 값의 소유자는 [SYS-010]이다 — [열린 항목 D](#열린-항목). 경로 형식은 2.0.0에서 좁혀졌다 — 서버 API 문서가 `gguk.org/r/{code}`를 발급 엔드포인트 설명에 명시하고 "클라이언트가 조립한다"고 못박았다.

**2.0.0 재확인**: 서버가 코드 발급을 별도 리소스로 분리한 뒤에도 이 결정은 그대로다. 오히려 근거가 강해졌다 — 서버가 `code`만 주고 조립을 클라이언트 몫으로 명시했으므로, "코드는 서버가 주고 호스트는 빌드 설정이 안다"는 이 항목의 전제가 API 문서로 확인됐다. **ADR을 뒤집지 않는다.**

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

~~**남는 구멍**: 튜토리얼 **스텝 5의 예시 이미지가 Figma에 아직 없다** — `3798-167139`의 해당 프레임은 `꾹 앱 지도화면에 핀마커가 표시되어있는 장면`이라는 설명 텍스트만 든 자리표시자다. [열린 항목 C](#열린-항목).~~ **메워짐(2026-08-31, 이슈 #273)** — 스텝 5 프레임이 `4396-184972`로 다시 그려지며 지도 그림이 채워졌다.

**갱신(2026-08-31, 이슈 #273)**: 친구 초대 화면의 **구름 배경은 확정 브랜딩에서 사라졌다.** 이 항목이 정한 소유 모듈은 그대로이고, 대상이 캐릭터 일러스트 한 장(`5073-101129`)으로 줄었다.

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

> **2.0.0 갱신**: 컴포넌트가 머지되어 열린 항목 F는 닫혔다. "소비만 한다"는 판단도 대체로 유지되지만, **우측 슬롯이 텍스트 액션만 받아 친구 초대의 [X]를 그릴 수 없다**는 사실이 드러났다. 축을 넓히는 결정은 [R-025](#r-025-minotopnavigation에-우측-아이콘-액션-축을-더한다-plan-200)가 갖는다 — 신설이 아니라 확장이므로 이 항목을 뒤집지는 않는다.

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

## R-021. 초대 코드는 전용 발급 엔드포인트에서 받는다 *(plan 2.0.0)*

**Decision**: 친구 초대 스텝은 진입 시 `POST /api/v1/rooms/{roomId}/invitations`를 불러 `code`를 받고, 그것을 `InviteLinkBuilder`에 넘겨 링크를 만든다. 방 조회(`getRoom`)를 쓰지 않는다.

**Rationale**: 서버 API 문서(2026-08-29T01:09:27+09:00 조회) 대조 결과 `GET /api/v1/rooms/{roomId}` 응답에 초대 코드가 없다. `invitation` 태그에 발급 오퍼레이션이 따로 서 있고, 그 설명이 **"멤버당 초대 1개다. 이미 발급했다면 같은 code를 돌려준다(재발급·만료 없음)"** 로 못박았다. 이 성질 덕분에 R-009가 지키려던 것 — 재개 경로에서 같은 링크를 다시 확보하는 것(EC-021) — 이 그대로 성립한다. 여러 번 불러도 같은 값이 오므로 클라이언트가 코드를 영속 저장할 이유가 없고, `POST`이지만 호출자 입장에서는 조회와 다르지 않다.

**대가**: 이 스텝이 **새 API 호출을 하나 낸다.** 1.0.0은 "이번 범위가 새로 호출하는 엔드포인트가 없다"였다. 그만큼 데이터 계층이 한 벌 늘어난다([R-022](#r-022-invitation-태그의-데이터-계층을-새로-만들고-도메인-계약은-관심사-단위로-둔다-plan-200)).

**실패 갈래가 늘었다**: 발급 API는 `403`을 **개인방(`PERSONAL_ROOM_NOT_ALLOWED`)과 비멤버(`NOT_ROOM_MEMBER`)** 에 쓰고 `404`를 방 없음에 쓴다. 온보딩 경로에서는 셋 다 도달할 수 없다 — 방금 만든 공동방의 id만 들어오기 때문이다(FR-004·`createdRoomId`). 도달하면 그것은 저장된 진행 상태가 손상된 것이므로, 다른 HTTP 실패와 같이 다뤄 화면을 유지하고 [X]로 나아가게 둔다(EC-008). **리프를 새로 만들지 않는다** — [`error-body-type-and-no-error-code-leaf` ADR](../../adr/2026-08-28-error-body-type-and-no-error-code-leaf.md)이 서버 코드를 도메인 리프로 세우지 않기로 정했다.

**Alternatives considered**:
- `getRoom` 응답에 코드가 다시 실릴 때까지 기다린다 — 서버가 의도적으로 분리한 리소스다. 되돌아올 근거가 없다. 기각.
- 코드를 받아 `OnboardingProgress`에 함께 저장한다 — 서버가 멱등을 보장하므로 저장이 사는 것이 없고, 저장된 코드가 서버 상태와 갈릴 위험만 는다. 기각.
- 방 생성 응답에서 코드를 받는다 — `POST /api/v1/rooms` 응답에도 코드가 없고, 재개 경로를 덮지 못한다(R-009의 판단 그대로). 기각.

---

## R-022. `invitation` 태그의 데이터 계층을 새로 만들고, 도메인 계약은 관심사 단위로 둔다 *(plan 2.0.0)*

**Decision**: `:core:data`에 `InvitationApiService` · `InvitationRemoteDataSource`를 신설하고, `:core:domain`에는 `RoomInvitationRepository`(함수 하나: `issueInviteCode(roomId): String`)를 둔다. `RoomApiService`·`RoomRepository`를 넓히지 않는다.

**Rationale**: [`api-service-owned-per-server-tag` ADR](../../adr/2026-08-28-api-service-owned-per-server-tag.md)이 `ApiService`의 단위를 **OpenAPI 태그**로 정했다. 발급 오퍼레이션의 태그는 `invitation`이고, 그 태그를 부르는 코드가 저장소에 없다(`grep -rn invitation core/ feature/` → 0건). 태그 소유자가 없으므로 새로 만드는 것이 규칙대로다. 반대로 **도메인 Repository는 태그 규칙의 대상이 아니라 관심사 단위**라고 같은 ADR이 명시했다 — "초대 코드 발급"은 방의 CRUD와 다른 관심사이므로 `RoomRepository`에 얹지 않는다.

**경로가 `/api/v1/rooms/...`로 시작하는데 태그가 `invitation`인 것은 문제가 아니다.** 같은 ADR이 경로 접두어 대신 태그를 고른 이유로 **정확히 이 중첩**(`/api/v1/rooms/{roomId}/members`가 `room`과 `invitation` 양쪽에 걸린다)을 들었다.

**대가**: 방 하나를 다루는 데이터 계층이 둘이 된다(`RoomApiService`·`InvitationApiService`). 서버가 태그를 합치면 그때 합친다.

**Alternatives considered**:
- `RoomApiService`에 `createInvitation`을 더한다 — 경로가 `/rooms/`로 시작한다는 이유뿐이고, 태그가 다르다. ADR을 어긴다. 기각.
- `RoomRepository`에 `issueInviteCode`를 더한다 — 데이터 계층에서 합쳐진 것도 아닌데 도메인에서 합치는 것이라 근거가 없다. 방 목록·조회만 쓰는 호출자가 초대 함수를 보게 된다. 기각.

---

## R-023. 스플래시의 온보딩 진입을 `ProfileLauncher` 직접 호출에서 `OnboardingLauncher`로 바꾼다 *(plan 2.0.0)*

**Decision**: `SplashActivity`의 `onNavigateToOnboarding`이 `profileLauncher.launch(..., PROFILE_ENTRY_POINT_ONBOARDING)` 대신 `onboardingLauncher.launch(this, withFinish = true)`를 부른다. 프로필 진입 인자를 싣는 주체는 온보딩으로 옮겨간다.

**Rationale**: 지금은 스플래시가 프로필 화면을 **온보딩의 첫 스텝으로서가 아니라 그냥** 연다. 프로필이 저장되면 `ProfileActivity`가 `setResult(RESULT_OK); finish()`로 닫히는데, 스플래시는 `withFinish = true`로 이미 죽어 있어 결과를 받을 주체가 없다 — **앱이 빈 태스크로 떨어진다.** 스텝 2~4가 존재하지 않으므로 FR-001·FR-003·FR-004가 성립할 수 없다. 온보딩 Activity가 그 결과를 받는 자리이므로 진입점도 그쪽이어야 한다.

**이 변경이 스플래시 스펙과 충돌하지 않는다**: `docs/specs/splash-screen` FR-003은 "프로필 설정([SYS-011])으로 시작하는 온보딩([SCR-002])으로 이동한다"이고, 그 스펙 §비목표가 "온보딩의 화면 구성·진행 로직은 이 스펙이 정의하지 않는다. 스플래시에서 그 화면으로의 진입 시점까지만 다룬다"로 적었다. **어느 Activity가 그 진입을 받는지는 온보딩의 몫**이라는 뜻이다.

**대가**: `:feature:splash`가 `ProfileLauncher` 의존을 잃고 `OnboardingLauncher` 의존을 얻는다. 두 계약 모두 `:core:navigation`에 있어 모듈 의존 그래프는 그대로다.

**Alternatives considered**:
- 스플래시가 프로필을 그대로 열고, 프로필이 끝나면 온보딩을 연다 — 온보딩 스텝 머신이 첫 스텝을 건너뛴 채 시작하게 되고, 재개(FR-023)에서 `lastStep = PROFILE`을 열 주체가 사라진다. 기각.
- 온보딩이 프로필 스텝을 직접 그린다 — `:feature:profile`을 의존하게 되어 헌법 원칙 II 위반. 기각(R-002의 판단 그대로).

---

## R-024. 진입 판정의 소유자는 온보딩이고, 스플래시의 `ResolveSplashEntryUseCase`가 그것을 함께 읽는다 *(plan 2.0.0)* — **ADR 기록됨**

> **근거의 소유자는 [앱 진입 화면 판정 ADR](../../adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md)이다**(승격: plan 2.0.0). 아래는 이 계획이 그 결정에 이른 경위이며, 다른 feature가 따라야 할 규칙은 ADR을 읽는다.

**Decision**: `ResolveSplashEntryUseCase`가 `OnboardingProgressRepository`를 함께 주입받아, **프로필이 등록되어 있고 온보딩 완료 표시도 있을 때만** `SplashEntry.Main`을 돌려준다. 둘 중 하나라도 없으면 `SplashEntry.Onboarding`이다. 1.0.1의 `onboarding-progress.md` §4가 열어 둔 두 안 중 (a)다.

**Rationale**: FR-022가 "프로필이 있어도 완료 표시가 없으면 온보딩을 끝내지 못한 것으로 보고 온보딩을 연다"를 요구하는데, 지금 구현은 `profileRegistrationRepository.isRegistered()` 하나만 본다. **프로필을 저장한 뒤 공동방 스텝에서 앱을 지운 사용자가 다음 실행에서 홈으로 밀려나 남은 스텝을 영영 못 본다**(TS-037·TS-038 실패).

1.0.1은 이 선택을 "스플래시 계획의 개정이 정한다"로 미뤘다. **spec 1.2.0이 그 미룸을 닫았다** — §3.2가 "온보딩을 띄울지 메인 탭을 띄울지 가르는 판정 기준은 이 문서(FR-021·FR-022)가 소유한다"로, §4 가정이 "스플래시 스펙이 완료 표시 기반 판정을 따르도록 개정되어야 한다"로 못박았다. 소유자가 정해졌으므로 계획도 그것을 따른다.

(a)를 고른 이유는 **판정이 한 자리에 모여야 TS-038·TS-039가 한 테스트로 덮이기 때문**이다. UseCase가 Repository 둘을 아는 것은 그 UseCase의 존재 이유(두 근거를 합쳐 진입을 정한다)에 부합한다.

**다른 feature를 구속한다**: 스플래시가 소유한 UseCase를 온보딩의 요구로 넓히는 것이고, 앞으로 진입 분기의 근거가 늘 때마다 같은 자리가 늘어난다. 두 feature의 경계를 정하는 결정이라 ADR로 승격했다 — 일반화된 규칙("화면 B가 화면 A로 보낼지의 조건은 A가 소유한다")과 `isRegistered()` 선행 호출 제약은 그 ADR이 소유한다.

**Alternatives considered**:
- `IsOnboardingCompletedUseCase`를 온보딩 쪽에 두고 스플래시가 조합한다((b)) — 판정 규칙이 스플래시 ViewModel로 새어 나가 두 조건의 결합을 테스트로 덮기 어려워진다. 기각.
- 완료 표시를 프로필 등록 여부로 대신한다 — FR-022가 명시적으로 기각한 안이다. spec §5가 이유를 든다. 기각.
- 스플래시를 손대지 않고 온보딩이 진입 직후 스스로 홈으로 튕겨 낸다 — 완료한 사용자가 온보딩 화면을 한 프레임 보게 되고, SC-003("다시 노출되는 경우 0건")을 어긴다. 기각.

---

## R-025. `MinoTopNavigation`에 우측 아이콘 액션 축을 더한다 *(plan 2.0.0)*

**Decision**: `:core:design-system`의 `MinoTopNavigation`이 우측 슬롯에 **텍스트 액션과 아이콘 액션 중 하나**를 받도록 축을 넓힌다. 친구 초대의 우상단 [X]가 그 아이콘 액션이다. `:feature:onboarding`이 자기 상단 바를 따로 그리지 않는다.

**Rationale**: 1.0.1의 [열린 항목 F](#열린-항목)는 "신설 소유자가 둘"이라 소비만 하기로 한 것이었는데, 그 컴포넌트가 이미 머지됐다. 다만 우측이 `actionLabel: String?`만 받아 아이콘을 그릴 수 없다. **그 파일이 스스로 "액션 아이콘·검색 등 나머지 구성은 필요한 화면이 나올 때 축을 넓힌다"고 적어 둔 자리**이며, 친구 초대가 그 화면이다.

[`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1.2의 "디자인 시스템의 표면은 Figma가 정한다"에 따라, Figma `Top Navigation/Top Navigation` 컴포넌트셋에 속한 구성은 사용처가 하나여도 `:core:design-system`이 소유한다. 축을 넓히는 것이지 컴포넌트를 새로 만드는 것이 아니므로 [`design-system README`](../../../core/design-system/README.md) §6.1의 M3 패턴(Defaults·토큰)을 그대로 따른다.

**대가**: 이 컴포넌트를 쓰는 화면(프로필·공동방 폼)이 회귀 확인 대상이 된다. 기존 파라미터의 기본값을 유지해 호출부가 깨지지 않게 한다.

**남는 판정**: 아이콘 자산·크기·터치 영역·틴트 토큰은 구현 착수 시 노드 대조로 정한다([figma-design-fidelity.md](../../conventions/figma-design-fidelity.md) §2). 이 항목은 표면만 정한다.

**Alternatives considered**:
- feature가 자기 상단 바를 그린다 — Figma가 DS 컴포넌트로 가진 것을 복제하게 되어 배치 규약 §1.2 위반. 기각.
- `actionLabel` 대신 우측 슬롯을 `@Composable () -> Unit`으로 연다 — 호출부가 무엇이든 넣을 수 있어 디자인 시스템이 정한 구성 축이 무너진다. M3 패턴이 아니다. 기각.
- [X]를 좌측 뒤로가기 자리에 놓는다 — Figma가 우상단에 뒀고(`2314-95568`), 좌측은 뒤로가기 자리라 FR-006과 혼동된다. 기각.

---

## R-026. 두 위임 화면의 온보딩 백프레스를 이 계획이 고치지 않는다 *(plan 2.0.0, R-006 보충)*

**Decision**: `ProfileRoute`의 `BackHandler(enabled = !state.isBackEnabled) {}`와 `RoomFormRoute`의 `state.isOnboarding -> Unit` 갈래를 그대로 둔다. 둘 다 제스처를 삼켜 **무반응**이며, FR-007이 요구하는 `앱을 백그라운드로`가 아니다.

**Rationale**: 1.0.0 시점에는 두 화면이 코드로 없었고 [열린 항목 A](#열린-항목)는 세 spec의 문서 불일치였다. 지금은 **문서대로 구현까지 끝난 상태**라 불일치가 코드에 박혀 있다. 그럼에도 고치지 않는 이유는 셋이다.

- 두 화면의 뒤로가기는 각자의 spec(`profile` EC-001 · `group-room-form` FR-022·EC-015)이 소유한다. 계획이 spec을 앞질러 구현을 바꾸면 원칙 IV(명세가 구현에 선행한다)를 어긴다.
- 온보딩 spec 1.2.0은 이 어긋남을 **spec 차원에서 인지하고도 FR-007을 바꾸지 않았다.** 어느 쪽이 옳은지는 세 spec 사이의 조정 사항이지 이 계획의 판단이 아니다.
- 사용자가 보는 차이는 "뒤로가기가 먹지 않는다" 대 "앱이 내려간다"이며, 어느 쪽도 앞 스텝으로 되돌아가지 않는다. **원칙(FR-006 — 앞으로만 흐른다)은 두 구현 모두 지킨다.**

**남는 몫**: 두 spec의 `/mino-spec` 개정과 그에 따른 구현 수정 — [열린 항목 A](#열린-항목).

**Alternatives considered**:
- 온보딩이 두 Activity를 `moveTaskToBack`하도록 밖에서 조작한다 — 다른 feature의 화면 동작을 바깥에서 바꾸는 것이라 경계가 무너진다. 기각.
- FR-007을 `무반응`으로 되돌리도록 온보딩 spec 개정을 제안한다 — spec 1.1.0이 명시적 결정으로 바꾼 것을 계획이 되돌리자고 할 근거가 없다(spec §5). 기각.

---

## R-027. 개인방 생성은 클라이언트가 하지 않는다 *(plan 2.0.0)*

**Decision**: 온보딩·프로필 어느 쪽도 개인방(`내 장소`)을 만드는 호출을 하지 않는다. FR-002의 "프로필 저장이 개인방 생성을 촉발한다"는 서버가 이행한다.

**Rationale**: 서버 API 문서(2026-08-29 조회)의 `POST /api/v1/users` 설명이 **"익명 인증 토큰의 uid로 등록한다. 개인방(내 장소) 생성이 같은 흐름에서 처리되며 응답에는 포함하지 않는다"** 이다. `:feature:profile`이 `ProfileRepositoryImpl.saveProfile` → `UserRemoteDataSource.register`로 그 API를 이미 부르고 있으므로, 프로필 저장이 곧 개인방 생성이다.

이로써 1.0.0의 [열린 항목 G](#열린-항목)가 닫힌다. 그 항목은 "프로필 계획 2.0.0이 원격 계층을 후속으로 미뤘다"가 근거였고, 그 미룸이 해소됐다.

**검증에 남는 제약**: 응답이 개인방을 싣지 않으므로 **클라이언트가 생성 사실을 직접 확인할 수 없다.** SC-002는 방 목록(`GET /api/v1/rooms`)에 `내 장소`가 보이는 것으로 간접 확인한다([quickstart.md §5](./quickstart.md)).

**Alternatives considered**:
- 클라이언트가 등록 후 개인방을 따로 만든다 — 서버가 이미 만들어 방이 둘이 된다(UX-001·SC-008 위반). 기각.
- 등록 응답에 개인방을 실어 달라고 요청한다 — 확인 편의를 위해 서버 계약을 넓히는 것이고, 방 목록 조회로 대신할 수 있다. 서버팀 협의 항목으로도 세우지 않는다. 기각.

---

## 열린 항목

이 계획이 스스로 닫을 수 없는 것들이다. 각 항목은 **닫는 주체**가 이 feature 밖에 있다.

**2.0.0에서 닫힌 것 넷** — B(스플래시 진입 판정)는 spec 1.2.0이 소유자를 정해 이 계획이 가져왔고, F(상단 바 소유자)·G(개인방 생성)·H(방 데이터 mock)는 선행 작업이 머지되며 사라졌다. 아래 표에 취소선으로 남긴다.

| # | 무엇 | 이 계획의 처리 | 닫는 조건 |
|---|---|---|---|
| **A** | **백프레스 정책이 세 spec에서 어긋나고, 이제 구현까지 그렇게 되어 있다.** 온보딩 spec 1.2.0 FR-007은 프로필 설정·공동방 생성 스텝에서 `앱을 백그라운드로`를 요구하지만, 프로필 spec EC-001과 공동방 폼 spec FR-022·EC-015는 `무반응`이고 `ProfileRoute`·`RoomFormRoute`가 그대로 구현했다 | 온보딩이 소유한 지점(친구 초대·튜토리얼·릴레이)만 FR-007대로 만들고 두 화면을 건드리지 않는다([R-026](#r-026-두-위임-화면의-온보딩-백프레스를-이-계획이-고치지-않는다-plan-200-r-006-보충)) | 두 spec의 `/mino-spec` 개정과 그에 따른 구현 수정. 온보딩 spec 1.1.0의 정책 변경(2026-08-20)이 두 문서에 아직 전파되지 않았다 |
| ~~**C**~~ | ~~**튜토리얼 스텝 5의 예시 이미지가 Figma에 없다.** `3798-167139`의 이미지 자리는 설명 텍스트만 든 자리표시자다~~ **닫힘(2026-08-31, 이슈 #273)** — 스텝 5 프레임이 `4396-184972`로 다시 그려지며 이미지가 채워졌다 | 스텝 1~4 에셋만 확정하고, 스텝 5는 에셋 슬롯을 비운 채 구조를 만든다 | — |
| ~~**D**~~ | ~~**초대 링크의 flavor별 호스트가 미확정이다.**~~ **가정으로 닫음(plan 2.1.0)** — 전 flavor에 프로덕션 호스트를 쓰기로 정했다([R-021](#r-021-초대-링크-호스트는-전-flavor에서-프로덕션-값을-쓴다-plan-210)). **답을 받은 것이 아니라 답 없이 진행하기로 한 것이다.** 원래 내용: 서버 API 문서가 `gguk.org/r/{code}`를 발급 엔드포인트 설명에 적고 조립을 클라이언트 몫으로 명시했으나, dev·qa 호스트가 그것과 같은지는 알 수 없다. spec §3.2는 형식을 [SYS-010]의 몫으로 뒀다 | `InviteLinkBuilder` 인터페이스까지 확정하고 값은 구현 단계로 넘긴다([R-011](#r-011-초대-링크-문자열-조립은-도메인-인터페이스--데이터-구현으로-가른다-plan-100--adr-기록됨) · [ADR](../../adr/2026-08-24-invite-link-assembly-domain-interface.md)). 고칠 자리가 구현체 한 파일이다. **2.0.0에서 경로 형식은 좁혀졌고 호스트만 남았다** | [SYS-010] 스펙 또는 서버팀 확정 |
| **E** | ~~**토스트 40dp의 소유자가 둘로 갈린다.**~~ **소유자 판정은 닫혔다(plan 1.0.1)** — [토스트 소유자 ADR](../../adr/2026-08-24-snackbar-host-owned-by-mino-scaffold.md)이 `MinoScaffold`로 정했다. 다만 **`MinoScaffold`가 아직 M3 기본 `SnackbarHost`를 그리고 오프셋도 없다** — ADR이 요구하는 상태가 코드에 없다 | 이 계획이 그 변경을 낸다([contracts/design-system-additions.md §3](./contracts/design-system-additions.md)) | **스플래시 스펙·계획의 개정.** 그 문서가 아직 "표출 위치는 Screen 컴포저블이 갖는다"로 적혀 ADR과 어긋난다. `SplashRoute`도 그렇게 구현되어 있다 |
| ~~**B**~~ | ~~**스플래시 분기 근거에 완료 표시가 없다.**~~ **닫힘(plan 2.0.0)** — spec 1.2.0 §3.2·§4가 이 판정의 소유자를 온보딩 spec으로 못박았다 | 이 계획이 `ResolveSplashEntryUseCase`를 넓힌다([R-024](#r-024-진입-판정의-소유자는-온보딩이고-스플래시의-resolvesplashentryusecase가-그것을-함께-읽는다-plan-200--adr-기록됨)) | — |
| ~~**F**~~ | ~~**`MinoTopNavigation`의 신설 소유자가 둘이다.**~~ **닫힘(plan 2.0.0)** — 컴포넌트가 머지됐다 | 신설하지 않고 **축만 넓힌다**([R-025](#r-025-minotopnavigation에-우측-아이콘-액션-축을-더한다-plan-200)) | — |
| ~~**G**~~ | ~~**개인방 생성이 아직 아무 데서도 일어나지 않는다.**~~ **닫힘(plan 2.0.0)** — 서버가 `POST /api/v1/users`에서 함께 만들고 `:feature:profile`이 그 API를 부른다 | 클라이언트가 만드는 것이 없다([R-027](#r-027-개인방-생성은-클라이언트가-하지-않는다-plan-200)). SC-002는 방 목록으로 간접 확인한다 | — |
| ~~**H**~~ | ~~**`RoomRepository` 뒤가 아직 인메모리 mock이다.**~~ **닫힘(plan 2.0.0)** — `RoomRemoteDataSourceImpl`·`RoomApiService`가 실제 서버를 부른다 | 이 계획이 그 계약을 소비하지 않게 됐다([R-021](#r-021-초대-코드는-전용-발급-엔드포인트에서-받는다-plan-200)) | — |

### 서버팀 협의 항목

| # | 무엇 | 왜 협의가 필요한가 |
|---|---|---|
| S-1 | 초대 링크의 **flavor별 호스트** | API 문서가 `gguk.org/r/{code}`를 프로덕션 기준으로만 적었다. dev·qa 빌드가 같은 호스트를 쓰는지, 다르다면 무엇인지가 `InviteLinkBuilderImpl`의 유일한 미정 값이다(열린 항목 D) |

---

## R-021. 초대 링크 호스트는 전 flavor에서 프로덕션 값을 쓴다 *(plan 2.1.0)*

**결정**: `InviteLinkBuilderImpl`이 dev·qa·prod 모든 flavor에서 `https://gguk.org/r/{code}`를 만든다. flavor별 분기를 두지 않는다.

**근거**: 서버 API 문서(2026-08-29T01:09:27+09:00 조회)가 발급 엔드포인트 설명에 *"클라이언트가 `gguk.org/r/{code}` 형태로 링크를 조립한다"* 를 적은 것이 현재 알려진 전부다. dev·qa 호스트가 다른지는 **확인되지 않았고**, 그것 하나 때문에 나머지 57개 작업을 멈추는 비용이 더 크다고 판단했다.

**이것은 확인된 사실이 아니라 가정이다.** 서버 문서가 프로덕션 기준으로만 적은 값을 전 flavor에 적용한 것이며, dev·qa가 다른 호스트를 쓴다면 **그 빌드에서 만들어진 초대 링크는 열리지 않는다.** QA 중 초대 링크를 눌러 방에 들어가는 시나리오가 실패하면 이 가정부터 의심한다.

**되돌리는 비용은 한 파일이다.** [초대 링크 조립 ADR](../../adr/2026-08-24-invite-link-assembly-domain-interface.md)이 조립을 `:core:data`의 구현체 하나에 가둬 두었기 때문이다 — 호스트가 확정되면 `InviteLinkBuilderImpl`에 flavor 분기를 넣는 것으로 끝나고, 도메인·화면·계약 어디도 바뀌지 않는다. **이 ADR이 값어치를 하는 첫 사례다.**

**남는 확인 사항**: 서버팀에 dev·qa 호스트를 물어야 한다(협의 항목 S-1은 닫히지 않았다 — 답을 받은 것이 아니라 답 없이 진행하기로 정했을 뿐이다).

**Alternatives considered**:
- *서버팀 확인까지 착수를 미룬다* — T031 하나 때문에 나머지 57개가 멈춘다. 기각.
- *T031을 제외하고 나머지를 먼저 돌린다* — 착수는 가능하지만 US3의 초대 링크 경로가 끝까지 이어지지 않아 수동 검증(quickstart §4.1의 7번)을 할 수 없다. 기각.
