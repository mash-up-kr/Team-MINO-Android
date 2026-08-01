package team.mino.core.designsystem.component.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.R
import team.mino.core.designsystem.component.avatar.token.AvatarTokens
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.PersonFill
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.image.MinoAsyncImage
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * Avatar 형태. 형태에 따라 클리핑 모양과 placeholder 글리프가 달라진다.
 *
 * - [Person] 원형 + 사람 실루엣.
 * - [Company] 둥근 사각형 + 빌딩 실루엣.
 * - [Academy] 둥근 사각형 + 학사모 실루엣.
 */
enum class MinoAvatarVariant {
    Person,
    Company,
    Academy,
}

/**
 * Avatar 크기(dp). Figma `Size` variant 축에 대응한다.
 */
enum class MinoAvatarSize(val dp: Dp) {
    XSmall(24.dp),
    Small(32.dp),
    Medium(40.dp),
    Large(48.dp),
    XLarge(56.dp),
}

/**
 * 사용자/회사/학교를 나타내는 Avatar.
 *
 * Figma(MU_Wanted / Montage)의 `Avatar/Avatar` 스펙을 따른다. [imageUrl]이 있으면 웹 이미지를
 * (Coil로) 로드해 형태에 맞게 클리핑하고, 없거나 **로딩에 실패하면** [variant]별 기본 placeholder
 * 글리프를 보여준다.
 *
 * @param variant 형태(Person 원형 / Company·Academy 둥근 사각형).
 * @param size 크기([MinoAvatarSize]).
 * @param imageUrl 표시할 웹 이미지 URL. null이면 placeholder 글리프를 표시한다.
 * @param contentDescription 접근성 설명.
 *
 * 실제 URL 로딩에는 앱 레벨에서 Coil 네트워크 컴포넌트(coil-network-ktor3)가 구성돼 있어야 한다.
 */
@Composable
fun MinoAvatar(
    modifier: Modifier = Modifier,
    variant: MinoAvatarVariant = MinoAvatarVariant.Person,
    size: MinoAvatarSize = MinoAvatarSize.Medium,
    imageUrl: String? = null,
    contentDescription: String? = null,
) {
    val shape = MinoAvatarDefaults.shape(variant)

    MinoAsyncImage(
        imageUrl = imageUrl,
        fallback = variant.placeholderPainter(),
        fallbackTint = MinoAvatarDefaults.placeholderTint,
        modifier = modifier
            .size(size.dp)
            .clip(shape)
            .background(MinoAvatarDefaults.backgroundColor)
            .border(AvatarTokens.BorderWidth, MinoAvatarDefaults.borderColor, shape),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
    )
}

/**
 * variant별 placeholder 글리프. Person은 공용 [MinoIcons] 벡터를 쓰고, Company·Academy는
 * 아직 아이콘 세트에 없어 전용 drawable을 유지한다.
 */
@Composable
private fun MinoAvatarVariant.placeholderPainter(): Painter =
    when (this) {
        MinoAvatarVariant.Person -> rememberVectorPainter(MinoIcons.PersonFill)
        MinoAvatarVariant.Company -> painterResource(R.drawable.ic_avatar_company)
        MinoAvatarVariant.Academy -> painterResource(R.drawable.ic_avatar_academy)
    }

@UiModePreviews
@Composable
private fun MinoAvatarPreview() {
    MinoAndroidAppTheme {
        Row(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .size(width = 260.dp, height = 72.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MinoAvatar(variant = MinoAvatarVariant.Person, size = MinoAvatarSize.Large)
            MinoAvatar(variant = MinoAvatarVariant.Company, size = MinoAvatarSize.Large)
            MinoAvatar(variant = MinoAvatarVariant.Academy, size = MinoAvatarSize.Large)
            MinoAvatar(variant = MinoAvatarVariant.Person, size = MinoAvatarSize.XSmall)
        }
    }
}
