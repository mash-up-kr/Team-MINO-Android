package team.mino.core.designsystem.component.invitation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.component.avatar.MinoAvatarGroup
import team.mino.core.designsystem.component.avatar.MinoAvatarVariant
import team.mino.core.designsystem.component.invitation.token.InvitationTokens
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.image.FallbackAsyncImage
import team.mino.core.designsystem.util.modifier.shadow.dropShadow
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * 모임·룸으로의 초대 정보를 요약해 보여주는 카드. Figma `invitation_card`(16108-23190) 스펙을 따른다.
 *
 * 흰 배경 카드 위에 cover 이미지, 제목·부제, 참여자 아바타 그룹과 카운트 텍스트를 쌓는다. 카드
 * 내용(cover·문구·참여자·카운트)은 전부 파라미터로 열려 있어 룸마다 다른 값을 채워 넣을 수 있다.
 *
 * @param title 제목(예: "5월의 약속 : 우리끼리").
 * @param description 부제(예: "우리 모임 장소 픽업 공간.").
 * @param participantImageUrls 참여자 아바타 이미지 URL 목록(각각 null이면 placeholder).
 * @param countLabel 아바타 그룹 옆 카운트 텍스트(예: "장소 999+개"). 포맷은 호출부가 결정한다.
 * @param coverImageUrl cover 박스에 표시할 이미지 URL. null이거나 로딩 실패 시 placeholder 글리프.
 * @param coverBackgroundColor cover 박스 배경색. 룸·카테고리별로 다른 색을 쓰려면 덮어쓴다.
 */
@Composable
fun MinoInvitationCard(
    title: String,
    description: String,
    participantImageUrls: ImmutableList<String?>,
    countLabel: String,
    modifier: Modifier = Modifier,
    coverImageUrl: String? = null,
    coverBackgroundColor: Color = MinoInvitationDefaults.coverBackgroundColor,
) {
    val cardShape = MinoInvitationDefaults.cardShape

    Column(
        modifier = modifier
            .width(InvitationTokens.CardWidth)
            .dropShadow(shape = cardShape, shadow = MinoInvitationDefaults.cardShadow)
            .surface(shape = cardShape, containerColor = MinoInvitationDefaults.cardBackgroundColor)
            .padding(
                horizontal = InvitationTokens.CardHorizontalPadding,
                vertical = InvitationTokens.CardVerticalPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(InvitationTokens.ContentSpacing),
    ) {
        InvitationCover(
            imageUrl = coverImageUrl,
            backgroundColor = coverBackgroundColor,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(InvitationTokens.TextSpacing),
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = MinoInvitationDefaults.titleFont,
                color = MinoInvitationDefaults.titleColor,
                textAlign = TextAlign.Center,
            )
            Text(
                text = description,
                modifier = Modifier.fillMaxWidth(),
                style = MinoInvitationDefaults.descriptionFont,
                color = MinoInvitationDefaults.descriptionColor,
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    InvitationTokens.FooterSpacing,
                    Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MinoAvatarGroup(
                    imageUrls = participantImageUrls,
                    variant = MinoAvatarVariant.Person,
                    size = InvitationTokens.AvatarSize,
                    containerColor = Color.Transparent,
                )
                Text(
                    text = countLabel,
                    style = MinoInvitationDefaults.countFont,
                    color = MinoInvitationDefaults.countColor,
                )
            }
        }
    }
}

@Composable
private fun InvitationCover(
    imageUrl: String?,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    FallbackAsyncImage(
        imageUrl = imageUrl,
        fallback = rememberVectorPainter(MinoIcons.Image),
        fallbackTint = MinoInvitationDefaults.coverPlaceholderTint,
        modifier = modifier
            .size(InvitationTokens.CoverSize)
            .surface(shape = MinoInvitationDefaults.coverShape, containerColor = backgroundColor)
            .padding(InvitationTokens.CoverImagePadding),
        contentScale = ContentScale.Fit,
    )
}

@UiModePreviews
@Composable
private fun MinoInvitationCardPreview() {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalAlternative.value)
                .padding(24.dp),
        ) {
            MinoInvitationCard(
                title = "5월의 약속 : 우리끼리",
                description = "우리 모임 장소 픽업 공간.",
                participantImageUrls = persistentListOf(null, null, null),
                countLabel = "장소 999+개",
                coverBackgroundColor = Color(0xFFFEECFB),
            )
        }
    }
}
