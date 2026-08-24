package team.mino.core.designsystem.component.profileavatar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.profileavatar.token.ProfileAvatarTokens
import team.mino.core.designsystem.foundation.color.token.value

/**
 * [MinoProfileAvatarImage]의 기본값 모음.
 *
 * 프로필 아바타는 enabled 같은 상태가 없어 M3 `BadgeDefaults`처럼 Colors 클래스 없이
 * Defaults의 단일 값 프로퍼티로 색을 노출한다. 지름·테두리 두께는 자리마다 한 쌍으로 묶여 있어
 * [MinoProfileAvatarSize]가 든다.
 */
object MinoProfileAvatarDefaults {
    /** 아바타를 두르는 테두리 색. 그림에 굽혀 있지 않아 컴포넌트가 그린다. */
    val borderColor: Color
        @Composable @ReadOnlyComposable get() = ProfileAvatarTokens.BorderColor.value
}
