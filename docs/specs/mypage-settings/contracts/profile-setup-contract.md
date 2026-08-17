# 계약: MyPageProfile 화면

`:feature:mypage/profile/` — 마이페이지 내부 Route(`internal data class MyPageProfile : Route`)로 전환되는 프로필 설정 화면. PRD [SYS-011]의 "마이페이지에서 진입" 분기만 이 feature가 구현한다(온보딩 쪽 재사용은 §3.2 비목표).

## UiState

```kotlin
data class ProfileUiState(
    val nickname: String = "",
    val avatarId: String? = null,
    val isSaveEnabled: Boolean = false,   // nickname 한글·영문 2자 이상 && avatarId != null (FR-004)
) : UiState
```

- 진입 시 `MyPageProfile`은 인자를 받지 않는다 — 기존 프로필은 화면이 직접 `ProfileRepository.getProfile()`로 읽어 초기 상태를 채운다(단순 단일 조회, `core:domain/README.md` §4의 ViewModel 직접 호출 4조건을 만족해 UseCase를 두지 않는다).

## Intent

```kotlin
sealed interface ProfileIntent : Intent {
    data class OnNicknameChanged(val value: String) : ProfileIntent
    data class OnAvatarSelected(val avatarId: String) : ProfileIntent
    data object OnClearClick : ProfileIntent    // FR-013 — 닉네임·아바타 동시 초기화, EC-006
    data object OnSaveClick : ProfileIntent      // FR-002·FR-003
}
```

## SideEffect

```kotlin
sealed interface ProfileSideEffect : SideEffect {
    data object NavigateBack : ProfileSideEffect   // 저장 완료 → MyPageMain으로 popBackStackIfResumed
}
```

## 저장 후 반영 (FR-003)

`MyPageProfile`은 별도의 전역 상태나 콜백으로 `MyPageMain`에 값을 되돌려주지 않는다. `NavigateBack` 뒤 `MyPageMain`이 다시 RESUMED되면 [`mypage-main-contract.md`](mypage-main-contract.md)의 재조회 규칙이 자동으로 최신 프로필을 읽어온다 — 재조회 지점 하나로 두 화면 간 동기화를 해결한다(data-model.md §4).

## Figma

[프로필 설정](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2792-151449&m=dev) — 아바타 12종 그리드는 `component/`에 화면 전용 컴포저블로 조립한다(design-system 승격 기준 미충족, 단일 사용처).
