package team.mino.core.designsystem.component.avatar

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import team.mino.core.designsystem.component.avatar.token.AvatarTokens
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.typography.token.value

/**
 * [MinoAvatar]·[MinoAvatarGroup]의 기본값 모음.
 *
 * Avatar는 enabled 같은 상태가 없어 M3 `BadgeDefaults`처럼 Colors 클래스 없이
 * Defaults의 단일 값 프로퍼티로 색을 노출한다.
 */
object MinoAvatarDefaults {
    /** 이미지가 없을 때 채우는 배경색. */
    val backgroundColor: Color
        @Composable @ReadOnlyComposable get() = AvatarTokens.BackgroundColor.value

    /** 테두리 색. */
    val borderColor: Color
        @Composable @ReadOnlyComposable get() = AvatarTokens.BorderColor.value

    /** placeholder 글리프 tint. */
    val placeholderTint: Color
        @Composable @ReadOnlyComposable get() = AvatarTokens.PlaceholderTint.value

    /** Avatar Group에서 아바타·overflow 뱃지 슬롯을 감싸는 배경 링 색. */
    val groupRingColor: Color
        @Composable @ReadOnlyComposable get() = AvatarTokens.GroupRingColor.value

    /** Avatar Group 전체를 감싸는 pill 컨테이너 배경색. */
    val groupContainerColor: Color
        @Composable @ReadOnlyComposable get() = AvatarTokens.GroupContainerBackground.value

    /** [MinoAvatarGroupAddButton] 배경색. */
    val addButtonBackgroundColor: Color
        @Composable @ReadOnlyComposable get() = AvatarTokens.AddButtonBackgroundColor.value

    /** [MinoAvatarGroupAddButton] 아이콘 색. */
    val addButtonIconColor: Color
        @Composable @ReadOnlyComposable get() = AvatarTokens.AddButtonIconColor.value

    /** [MinoAvatarGroupOverflowBadge] 배경색. */
    val overflowBackgroundColor: Color
        @Composable @ReadOnlyComposable get() = AvatarTokens.OverflowBackgroundColor.value

    /** [MinoAvatarGroupOverflowBadge] 라벨 색. */
    val overflowLabelColor: Color
        @Composable @ReadOnlyComposable get() = AvatarTokens.OverflowLabelColor.value

    /** [MinoAvatarGroupOverflowBadge] 라벨 폰트. */
    val overflowLabelFont: TextStyle
        @Composable @ReadOnlyComposable get() = AvatarTokens.OverflowLabelFont.value

    /** [variant]별 클리핑 셰이프(Person=원형, 그 외=둥근 사각형). */
    fun shape(variant: MinoAvatarVariant): Shape =
        when (variant) {
            MinoAvatarVariant.Person -> CircleShape
            MinoAvatarVariant.Company, MinoAvatarVariant.Academy ->
                RoundedCornerShape(percent = AvatarTokens.SquircleCornerPercent)
        }
}
