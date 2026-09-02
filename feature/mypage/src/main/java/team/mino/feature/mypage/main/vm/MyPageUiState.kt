package team.mino.feature.mypage.main.vm

import team.mino.core.common.android.architecture.UiState
import team.mino.core.domain.model.PermissionType
import team.mino.core.domain.model.ProfileAvatar

data class MyPageUiState(
    val nickname: String = "",
    val avatar: ProfileAvatar? = null,
    val isNotificationSwitchOn: Boolean = false,
    val isLocationSwitchOn: Boolean = false,
    val permissionSettingsDialogTarget: PermissionType? = null,
) : UiState
