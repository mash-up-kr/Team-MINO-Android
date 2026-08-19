# 계약: MyPageMain 화면

`:feature:mypage/main/` — 마이페이지 탭의 시작 화면. 탭 feature이므로 `XShell`·`XNavHost`가 없고 `:feature:main`의 셸 안에서 그려진다(`feature-module.md` 1장).

## UiState

```kotlin
data class MyPageUiState(
    val nickname: String = "",
    val avatarId: Int? = null,   // 백엔드 Avatar.id 타입에 맞춤 (research.md D9)
    val isNotificationSwitchOn: Boolean = false,     // (OS 알림 권한 허용 AND 앱 자체 발송 설정) 합성값 — data-model.md §4
    val isLocationSwitchOn: Boolean = false,          // OS 위치 권한 상태 그대로
    val permissionSettingsDialogTarget: PermissionType? = null,  // null이면 다이얼로그 비노출
) : UiState
```

## Intent

```kotlin
sealed interface MyPageIntent : Intent {
    data object OnScreenResumed : MyPageIntent
    data object OnEditProfileClick : MyPageIntent

    // rationale은 Route(Activity 전용 API)가 계산해 함께 싣는다 — research.md D3
    data class OnNotificationSwitchClick(val canShowSystemDialog: Boolean) : MyPageIntent
    data class OnLocationSwitchClick(val canShowSystemDialog: Boolean) : MyPageIntent

    data class OnNotificationPermissionResult(val granted: Boolean) : MyPageIntent
    data class OnLocationPermissionResult(val granted: Boolean) : MyPageIntent

    data object OnPermissionSettingsDialogConfirmed : MyPageIntent
    data object OnPermissionSettingsDialogDismissed : MyPageIntent

    data object OnTermsClick : MyPageIntent
    data object OnAppReviewClick : MyPageIntent
}
```

## SideEffect

```kotlin
sealed interface MyPageSideEffect : SideEffect {
    data object NavigateToProfileSetup : MyPageSideEffect
    data object RequestNotificationPermission : MyPageSideEffect   // Route가 launcher.launch(POST_NOTIFICATIONS) 호출
    data object RequestLocationPermission : MyPageSideEffect        // Route가 launcher.launch([FINE, COARSE]) 호출
    data object OpenAppSettings : MyPageSideEffect
    data class OpenUrl(val url: String) : MyPageSideEffect
    data object OpenPlayStoreListing : MyPageSideEffect
}
```

## 분기 규칙 — 알림 스위치 클릭 (FR-007·FR-009·FR-010·FR-014)

| 현재 상태 | `canShowSystemDialog` | `hasRequestedPermissionBefore` | 동작 |
|---|---|---|---|
| ON (권한 허용 + 발송 설정 ON) | — | — | 발송 설정만 OFF로 저장(FR-014). SideEffect 없음, OS 권한 유지 |
| OFF, OS 권한 이미 허용됨(발송 설정만 꺼져 있던 경우) | — | — | 발송 설정만 ON으로 저장. SideEffect 없음 |
| OFF, OS 권한 미허용 | `true` | — | `SideEffect.RequestNotificationPermission` (시스템 팝업, FR-007) |
| OFF, OS 권한 미허용 | `false` | `false` | `SideEffect.RequestNotificationPermission` (최초 요청, research.md D3) |
| OFF, OS 권한 미허용 | `false` | `true` | `permissionSettingsDialogTarget = NOTIFICATION` (영구 거부, FR-010) |

권한 결과 콜백(`OnNotificationPermissionResult`)에서: `granted == true`면 `markPermissionRequested`는 이미 요청 시점에 호출됐다는 전제로 발송 설정을 ON 저장하고 `PushNotificationRepository.syncPushToken()`을 호출한 뒤 재조회한다(research.md D10 — 서버에 발송 억제 API가 없어 토큰은 등록해 두고 표시만 로컬에서 걸러낸다). `granted == false`면 `markPermissionRequested(NOTIFICATION)`만 기록하고 스위치는 OFF 유지, 토큰 등록 없음.

## 분기 규칙 — 위치 스위치 클릭 (FR-008·FR-009·FR-010·FR-015)

| 현재 상태 | `canShowSystemDialog` | `hasRequestedPermissionBefore` | 동작 |
|---|---|---|---|
| ON (OS 권한 허용됨) | — | — | 앱이 권한을 취소할 수 없음 → `permissionSettingsDialogTarget = LOCATION` (EC-007, FR-015) |
| OFF, OS 권한 미허용 | `true` | — | `SideEffect.RequestLocationPermission` (FR-008) |
| OFF, OS 권한 미허용 | `false` | `false` | `SideEffect.RequestLocationPermission` (최초 요청) |
| OFF, OS 권한 미허용 | `false` | `true` | `permissionSettingsDialogTarget = LOCATION` (영구 거부, FR-010) |

위치는 알림과 달리 로컬 억제 플래그가 없으므로 결과 콜백은 재조회(`isLocationPermissionGranted()`)만 트리거한다.

## 재조회 (FR-009, data-model.md §4)

`OnScreenResumed`(및 `init`)에서 `ProfileRepository.getProfile()` · `AppSettingsRepository.observeNotificationDeliveryEnabled()` · `PermissionRepository.isNotificationPermissionGranted()`/`isLocationPermissionGranted()`를 모두 다시 읽어 `UiState`를 재계산한다. `isNotificationSwitchOn = isNotificationPermissionGranted && notificationDeliveryEnabled`, `isLocationSwitchOn = isLocationPermissionGranted`.

## Figma

[마이페이지 메인](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2792-151806&m=dev) — 구현 단계에서 `MinoDialog`·`MinoSwitch`를 조립해 그린다(대조 절차는 `figma-design-fidelity.md`).
