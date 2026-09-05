package team.mino.core.designsystem.component.avatar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.R
import team.mino.core.designsystem.component.avatar.token.AvatarTokens
import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar
import team.mino.core.designsystem.component.profileavatar.ProfileAvatarPainting
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.PersonFill
import team.mino.core.designsystem.util.image.MinoAsyncImage
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface

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
 * 그림 식별자로 내려오는 사람(방 멤버 등)은 이 함수가 아니라 **번들 아바타 오버로드**를 쓴다.
 *
 * @param variant 형태(Person 원형 / Company·Academy 둥근 사각형).
 * @param size 크기([MinoAvatarSize]).
 * @param imageUrl 표시할 웹 이미지 URL. null이면 placeholder 글리프를 표시한다.
 * @param contentDescription 접근성 설명.
 * @param onClick 누를 수 있는 아바타로 만든다(Figma `interaction`). null이면 클릭을 받지 않는다.
 *   Figma의 인터랙션 레이어는 아바타 바깥으로 8dp 튀어나오지만 Compose는 리플을 바운즈 밖으로
 *   그릴 수 없어, 리플이 아바타 안쪽에 머문다.
 * @param pushBadge 우상단에 얹히는 알림 배지 슬롯(Figma `pushBadge`). Figma는 배지 프레임의
 *   **중심을 아바타 우상단 모서리에 맞춰** 절반이 밖으로 나가게 두며, 크기는 아바타와 무관하게
 *   20dp 고정이다. 배지 그래픽 자체는 화면마다 달라 슬롯으로 연다.
 *
 * 실제 URL 로딩에는 앱 레벨에서 Coil 네트워크 컴포넌트(coil-network-ktor3)가 구성돼 있어야 한다.
 */
@Composable
fun MinoAvatar(
    modifier: Modifier = Modifier,
    variant: MinoAvatarVariant = MinoAvatarVariant.Person,
    size: MinoAvatarSize = MinoAvatarSize.Small,
    imageUrl: String? = null,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    pushBadge: (@Composable () -> Unit)? = null,
) {
    AvatarFrame(
        shape = MinoAvatarDefaults.shape(variant),
        size = size,
        onClick = onClick,
        pushBadge = pushBadge,
        modifier = modifier,
    ) {
        MinoAsyncImage(
            imageUrl = imageUrl,
            fallback = variant.placeholderPainter(),
            fallbackTint = MinoAvatarDefaults.placeholderTint,
            modifier = Modifier.fillMaxSize(),
            fallbackModifier = Modifier.padding(size.dp * variant.placeholderInsetRatio),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
        )
    }
}

/**
 * 번들 아바타로 사람을 그리는 [MinoAvatar]. 서버가 이미지를 URL이 아니라 그림 식별자로 내려주는
 * 사람(방 멤버 등)을 그릴 때 쓴다. 번들 아바타는 언제나 사람이라 형태를 받지 않는다.
 *
 * @param profileAvatar 표시할 번들 아바타. `null`이면 기본 아바타를 그린다([ProfileAvatarPainting]) —
 *   URL 갈래의 `imageUrl = null`(= placeholder 글리프)과는 뜻이 다르다.
 * @param size 크기([MinoAvatarSize]).
 * @param contentDescription 접근성 설명.
 * @param onClick 누를 수 있는 아바타로 만든다. null이면 클릭을 받지 않는다.
 * @param pushBadge 우상단에 얹히는 알림 배지 슬롯.
 */
@Composable
fun MinoAvatar(
    profileAvatar: MinoProfileAvatar?,
    modifier: Modifier = Modifier,
    size: MinoAvatarSize = MinoAvatarSize.Small,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    pushBadge: (@Composable () -> Unit)? = null,
) {
    AvatarFrame(
        shape = MinoAvatarDefaults.shape(MinoAvatarVariant.Person),
        size = size,
        onClick = onClick,
        pushBadge = pushBadge,
        modifier = modifier,
    ) {
        ProfileAvatarPainting(avatar = profileAvatar, contentDescription = contentDescription)
    }
}

@Composable
private fun AvatarFrame(
    shape: Shape,
    size: MinoAvatarSize,
    onClick: (() -> Unit)?,
    pushBadge: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.size(size.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .surface(
                    shape = shape,
                    containerColor = MinoAvatarDefaults.backgroundColor,
                    borderColor = MinoAvatarDefaults.borderColor,
                    borderWidth = AvatarTokens.BorderWidth,
                ).then(if (onClick != null) Modifier.rippleSingleClickable(onClick = onClick) else Modifier),
        ) {
            content()
        }

        if (pushBadge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = AvatarTokens.PushBadgeSize / 2, y = -AvatarTokens.PushBadgeSize / 2)
                    .size(AvatarTokens.PushBadgeSize),
                contentAlignment = Alignment.Center,
            ) {
                pushBadge()
            }
        }
    }
}

/**
 * placeholder 글리프를 아바타 안쪽으로 얼마나 들여 그릴지의 비율.
 *
 * Figma placeholder는 글리프가 아바타의 **약 50%**를 차지한다(`Vector` 레이어 inset 좌우 25.28% ·
 * 상 24.6% · 하 23.88%). Person은 공용 아이콘 세트의 벡터라 자체 여백이 없어 여기서 25%를 넣어야
 * 그 비율이 나온다. Company·Academy는 아바타 전용 drawable이라 여백이 이미 그려져 있어 0이다.
 */
private val MinoAvatarVariant.placeholderInsetRatio: Float
    get() =
        when (this) {
            MinoAvatarVariant.Person -> 0.25f
            MinoAvatarVariant.Company, MinoAvatarVariant.Academy -> 0f
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
