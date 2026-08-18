# 계약: 프로필 설정 화면 (`:feature:profile`)

온보딩·마이페이지 두 진입점이 공유하는 단일 화면(FR-001). 골격은 [feature-module.md](../../../architecture/feature-module.md) 4장의 Route↔Screen 분리를 따른다.

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
| `SaveClicked` | `isSaving`이면 무시. 아니면 `isSaving=true` 후 `SaveProfileUseCase(nickname, 아바타 id)` 호출 | FR-007, UX-003, EC-004 |

- 비활성 상태의 버튼은 인텐트를 만들지 않는다(UX-004, EC-011). 활성 여부 판정은 화면이 UiState로 계산한다.
- `SaveClicked` 처리는 `launchSafely` 안에서 `runCatchingDomain`으로 감싸고, 성공·실패 어느 쪽이든 `isSaving=false`로 되돌린다([에러 처리 규약](../../../conventions/error_handling.md) §4).
- 넘기는 아바타 id는 `selectedAvatar ?: 기본 아바타`를 `Int`로 옮긴 값이다(EC-002). enum ↔ id 매핑은 이 feature가 소유한다([data-model.md §4](../data-model.md)).
- 등록(`POST /users`)과 수정(`PATCH /users/me`) 중 무엇이 나가는지는 화면이 모른다 — UseCase가 캐시 상태로 고른다([repository 계약](profile-repository-contract.md)).
- 마이페이지 진입의 프리필(FR-006)은 `ProfileRepository.observeProfile()`의 첫 값으로 채운다. 화면이 서버를 조회하지 않는다.

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

## 화면 구성 (`ProfileScreen`)

Figma [010-1 기본](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2314-95662&m=dev) · [010-2 입력 완료](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2314-95709&m=dev) · [010-3 입력 오류](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2314-95754&m=dev)

| 자리 | 사용할 것 | 비고 |
|---|---|---|
| 상단 바 | `MinoTopNavigation` (신설, [design-system 계약](design-system-contract.md)) | 제목 `프로필 설정`, 뒤로가기는 진입점에 따라 노출 |
| 안내 문구 | 텍스트 | `친구들에게 어떻게 보일까요?` |
| 상단 썸네일 | `MinoProfileAvatarImage` (신설) | 선택 아바타 또는 기본 아바타 |
| 닉네임 입력 | `MinoTextField` | `label="이름 또는 닉네임"`, `required=true`, `placeholder="한글·영문 2글자 이상"`, `helperText`는 오류 시 `한글·영문 2글자 이상을 입력해주세요.`, `status`는 오류 시 `Negative` |
| 아바타 목록 | 4열 × 3행 그리드 + 제목 `프로필 이미지 선택` | 칸은 `MinoProfileAvatarImage`, 배치는 화면이 소유 |
| 하단 액션 | `MinoActionArea` | `지우기`·`저장` 두 버튼이 가로로 놓이고 오른쪽이 메인이다 — variant 판정은 구현 단계에서 Figma 대조로 확정 |

- 바텀 네비게이션을 노출하지 않는다(UX-006). 이 화면은 탭 셸 밖의 진입형 feature이므로 구조상 자연히 만족한다.
- 스크롤·인셋은 `ProfileShell`이 연 `MinoScaffold` 안에서 처리한다. 화면이 `Scaffold`를 또 열지 않는다(feature-module.md 4장).
- 정확한 치수·색·간격은 구현 단계에서 [figma-design-fidelity.md](../../../conventions/figma-design-fidelity.md)의 판정 절차로 정한다. 이 계약은 무엇을 쓰는지까지만 정한다.

## 뒤로가기

| 진입점 | 상단 뒤로가기 | 시스템 back |
|---|---|---|
| 온보딩 | 노출하되 비활성 | `BackHandler(enabled = true) {}`로 삼킨다 |
| 마이페이지 | 활성 | 저장하지 않고 화면을 닫는다(`finish()`) |

- 마이페이지 진입에서는 확인 모달을 띄우지 않고 수정 내용을 버린다(FR-013, EC-005).
