# 계약: 프로필 설정 화면 (`:feature:profile`)

온보딩·마이페이지 두 진입점이 공유하는 단일 화면(FR-001). 골격은 [feature-module.md](../../../architecture/feature-module.md) 4장의 Route↔Screen 분리를 따른다.

> **범위**: 화면은 저장이 어디로 나가는지 모른다. 그 뒤가 서버인지 캐시인지, 등록인지 수정인지가 이 계약의 어느 항목도 바꾸지 않는다([research.md D36](../research.md#d36-원격-연동-착수--원천은-서버-로컬-datastore는-캐시)·[D38](../research.md#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)).
>
> **plan 4.0.0에서 바뀐 것은 세 줄이다** — `SaveClicked`가 넘기는 아바타의 타입, 마이페이지 진입 시 갱신 호출, 그리고 실패 통로가 실제로 발화한다는 사실. UiState의 필드도 Intent·SideEffect의 목록도 화면 구성도 그대로다.

## Route (feature 내부)

```kotlin
@Serializable
internal data class ProfileMain(val entryPoint: String) : Route
```

- 진입 값은 컴포저블로 드릴링하지 않는다. `ProfileActivity`가 Intent extra를 읽어 시작 라우트에 싣고, `ProfileViewModel`이 `savedStateHandle.toRoute<ProfileMain>()`로 복원한다([feature-navigation.md](../../../architecture/feature-navigation.md) 2장).
- `String`(primitive)이므로 `typeMap`을 두지 않는다. `ProfileEntryPoint`로의 해석은 ViewModel이 한다.

## UiState

[`data-model.md` §5](../data-model.md)가 필드와 파생 값을 정의한다. 이 계약은 그 상태를 누가 바꾸는지만 정한다.

## Intent

```kotlin
internal sealed interface ProfileIntent : Intent {
    data class NicknameChanged(val value: String) : ProfileIntent
    data class AvatarSelected(val avatar: MinoProfileAvatar) : ProfileIntent
    data object ClearClicked : ProfileIntent
    data object SaveClicked : ProfileIntent
}
```

| Intent | 처리 | 근거 |
|---|---|---|
| `NicknameChanged` | `nickname` 갱신, `ValidateNicknameUseCase`로 `isNicknameValid` 재계산, `isNicknameTouched = true` | FR-002, UX-002 |
| `AvatarSelected` | `selectedAvatar`를 그 값으로 교체(항상 단일 선택) | FR-003, TS-004 |
| `ClearClicked` | `nickname=""`, `selectedAvatar=null`, `isNicknameValid=false`, `isNicknameTouched=false` | FR-005, TS-015 |
| `SaveClicked` | `isSaving`이면 무시. 아니면 `isSaving=true` 후 `SaveProfileUseCase(nickname, 아바타)` 호출 | FR-007, UX-003, EC-004 |

- 비활성 상태의 버튼은 인텐트를 만들지 않는다(UX-004, EC-011). 활성 여부 판정은 화면이 UiState로 계산한다.
- `SaveClicked` 처리는 `launchSafely` 안에서 `runCatchingDomain`으로 감싸고, 성공·실패 어느 쪽이든 `isSaving=false`로 되돌린다([에러 처리 규약](../../../conventions/error_handling.md) §4).
- 넘기는 아바타는 `selectedAvatar ?: 기본 아바타`를 도메인 `ProfileAvatar`로 옮긴 값이다(EC-002). 두 enum의 매핑은 이 feature가 소유한다([data-model.md §4](../data-model.md)).
- 저장이 어디에 어떻게 쓰이는지는 화면이 모른다. `SaveProfileUseCase`의 시그니처(`rawNickname`·`avatar`)만 안다([repository 계약](profile-repository-contract.md)). **등록인지 수정인지도 화면이 알지 않는다** — 판정은 데이터 레이어에 있다([D38](../research.md#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)).
- **프리필(FR-006)은 두 단계다**([D45](../research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)).
  1. 진입 즉시 `ProfileRepository.observeProfile()`의 **첫 값**(캐시)으로 채운다.
  2. `refreshProfile()`이 **성공하면** `isNicknameTouched == false && !isSaving`일 때만 갱신된 값으로 한 번 더 채운다.
- **흐름을 계속 구독하지 않는다.** 구독하면 저장 직후 값이 사용자 입력을 덮어쓴다 — 이미 구현된 `prefill()`의 KDoc이 그 근거를 갖고 있고, 이 계약은 그 판단을 유지한 채 갱신 시점의 재읽기만 더한다.
- **마이페이지 진입에서만 `ProfileRepository.refreshProfile()`을 한 번 부른다.** 프리필의 원천을 서버로 맞추고(FR-006), 등록 여부를 캐시에 확정하기 위해서다([D39](../research.md#d39-repository-표면--observeprofile--refreshprofile--saveprofile-세-멤버)). 온보딩 진입에서는 아예 부르지 않는다 — 스플래시가 같은 `GET /api/v1/users/me`로 미등록을 확정해 연 화면이라, 갱신은 같은 401을 한 번 더 받고 이미 빈 캐시를 다시 비우는 것으로 끝난다. 판정은 `ProfileEntryPoint.needsRefresh`가 소유한다. 실패하면 실패 통로로 나가고 화면은 캐시 값(또는 빈 상태)으로 계속 선다 — **별도 로딩 상태를 두지 않는다**(spec에 진입 로딩 표현이 없다). `launchSafely` + `runCatchingDomain`으로 감싸는 것은 저장과 같다.

## SideEffect

```kotlin
internal sealed interface ProfileSideEffect : SideEffect {
    data object SaveCompleted : ProfileSideEffect
}
```

- `SaveCompleted`는 "저장이 끝났다"만 말한다. 다음 목적지는 화면 밖에서 정한다 — `ProfileActivity`가 이 신호를 받아 `setResult(RESULT_OK)` 후 `finish()`한다(research.md D2).
- 저장 실패는 SideEffect가 아니라 `DomainErrorEmitter`로 나간다(research.md D8).

## 실패 통로

```kotlin
CollectDomainError(viewModel) { error -> /* 스낵바 표시 */ }
```

- `ProfileRoute`가 수집하고, 스낵바 호스트는 셸이 제공한 `LocalSnackbarHostState`에서 읽는다(에러 처리 규약 §5·§6).
- 리프 → 문구 매핑(`when(error)`)은 `ProfileRoute`가 자기 파일에 둔다. 공통 매퍼를 만들지 않는다(§8).
- 실패 시 UiState는 `isSaving=false` 외에 아무것도 바꾸지 않는다 — 입력값이 그대로 남아야 한다(FR-012, EC-003, EC-007, SC-006).
- **이 통로는 이제 실제로 발화한다.** 네트워크 단절·서버 거절(15자 초과 닉네임 포함)·`409` 재등록 충돌이 `MinoDomainException`으로 올라온다([D25](../research.md#d25-저장-실패-경로--통로는-지금-배선하고-발화-원천은-후속-작업에-남긴다) 보정). 리프는 `Http`·`Network` 둘뿐이며 새로 늘지 않는다([D30](../research.md#d30-로컬-저장-실패용-도메인-예외-리프를-추가하지-않는다) 보정).

## 화면 구성 (`ProfileScreen`)

Figma [010-1 기본](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2314-95662&m=dev) · [010-2 입력 완료](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2314-95709&m=dev) · [010-3 입력 오류](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2314-95754&m=dev)

| 자리 | 사용할 것 | 비고 |
|---|---|---|
| 상단 바 | `MinoTopNavigation` (신설, [design-system 계약](design-system-contract.md)) | 제목 `프로필 설정`. **온보딩 진입은 `onBackClick = null`로 버튼을 그리지 않고**, 마이페이지 진입만 노출한다 |
| 안내 문구 | 텍스트 | `친구들에게 어떻게 보일까요?` |
| 상단 썸네일 | `MinoProfileAvatarImage` (신설) | 선택 아바타 또는 기본 아바타 |
| 닉네임 입력 | `MinoTextField` | `label="이름 또는 닉네임"`, `required=true`, `placeholder="한글·영문 2글자 이상"`. **`helperText`는 상태와 무관하게 항상 `한글·영문 2글자 이상을 입력해주세요.`이고 `status`만 오류 시 `Negative`로 갈린다** — 원본이 세 상태 모두 같은 문구를 두고 색만 바꾼다 |
| 아바타 목록 | 4열 × 3행 그리드 + 제목 `프로필 이미지 선택` | 칸은 `MinoProfileAvatarImage`, 배치는 화면이 소유. 고정 `Column`+`Row`로 그리고 `LazyVerticalGrid`를 쓰지 않는다([research.md D26](../research.md#d26-아바타-그리드의-배치--화면이-소유하고-lazyverticalgrid를-쓰지-않는다)). **선택된 칸의 시각 표시는 없다**([D28](../research.md#d28-아바타-선택-상태의-시각-표시를-만들지-않는다)) — 선택은 상단 썸네일로만 드러난다 |
| 하단 액션 | `MinoActionArea` | `variant = Neutral`, `alternativeAction = 지우기`(좌·Outlined) · `mainAction = 저장`(우·Solid). 폭 균등 분할이며 원본 대조로 확정했다 |

- **상단 바와 하단 액션 영역은 고정이고 본문만 스크롤한다.** 바깥 `Column`에 상단 바 → 본문 `weight(1f)` + `verticalScroll`(안내 문구 → 썸네일 → 입력 필드 → 그리드) → 액션 영역 순으로 놓는다. 원본이 상단 바 y=0·액션 영역 bottom=0으로 고정이기 때문이며([research.md D32](../research.md#d32-화면은-상단-바와-액션-영역을-고정하고-본문만-스크롤한다)), 그리드는 본문 안에 고정 높이로 놓여 중첩 스크롤이 생기지 않는다.
- 바텀 네비게이션을 노출하지 않는다(UX-006). 이 화면은 탭 셸 밖의 진입형 feature이므로 구조상 자연히 만족한다.
- 스크롤·인셋은 `ProfileShell`이 연 `MinoScaffold` 안에서 처리한다. 화면이 `Scaffold`를 또 열지 않는다(feature-module.md 4장).
- 정확한 치수·색·간격은 구현 단계에서 [figma-design-fidelity.md](../../../conventions/figma-design-fidelity.md)의 판정 절차로 정한다. 이 계약은 무엇을 쓰는지까지만 정한다.

## 뒤로가기

| 진입점 | 상단 뒤로가기 | 시스템 back |
|---|---|---|
| 온보딩 | **노출하지 않는다**(`onBackClick = null`, 자리만 남음) | `BackHandler(enabled = true) {}`로 삼킨다 |
| 마이페이지 | 활성 | 저장하지 않고 화면을 닫는다(`finish()`) |

- 마이페이지 진입에서는 확인 모달을 띄우지 않고 수정 내용을 버린다(FR-013, EC-005).
- **온보딩에서 버튼을 숨기는 것은 spec FR-010의 "노출하되 비활성"과 다르다.** 화면을 벗어나지 못하게 한다는 목적은 그대로이며, 근거는 [research.md D29](../research.md#d29-온보딩-진입에서-뒤로가기를-노출하지-않는다)에 있다.
- 온보딩 진입에서 저장이 끝난 뒤 이 화면으로 되돌아오는 경로는 화면 밖에도 없다(TS-018, EC-013). 화면은 `finish()`로 스스로를 닫으므로 스택에 남지 않는다.
