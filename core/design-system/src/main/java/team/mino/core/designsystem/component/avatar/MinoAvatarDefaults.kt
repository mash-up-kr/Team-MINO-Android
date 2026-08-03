package team.mino.core.designsystem.component.avatar

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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

    /** [variant]별 클리핑 셰이프(Person=원형, 그 외=둥근 사각형). */
    fun shape(variant: MinoAvatarVariant): Shape =
        when (variant) {
            MinoAvatarVariant.Person -> CircleShape
            MinoAvatarVariant.Company, MinoAvatarVariant.Academy -> SquircleShape
        }
}

/** 아바타마다 새로 만들지 않도록 한 번만 만들어 재사용한다. */
private val SquircleShape = RoundedCornerShape(percent = AvatarTokens.SquircleCornerPercent)
