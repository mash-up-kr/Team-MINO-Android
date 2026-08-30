package team.mino.feature.sample.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.sample.R
import team.mino.feature.sample.main.component.token.InvitationTokens

/**
 * 봉투에서 카드가 삐져나온 형태의 초대장. Figma `invitation`(16108-23382, AOS variant) 스펙을 따른다.
 *
 * 봉투 앞·뒷면 일러스트는 항상 같은 모양이라 고정 에셋(`R.drawable.invitation_envelope_back/front`)으로
 * 두고, [MinoInvitationCard]만 그 사이에 끼워 넣는 조합이다. 카드 내용 파라미터는 [MinoInvitationCard]와
 * 동일하다. 세로 좌표는 Figma 노드 실측값을 그대로 dp로 옮겼다(1px = 1dp 가정); 카드는 가로 중앙 정렬한다.
 */
@Composable
fun MinoInvitation(
    title: String,
    description: String,
    participantImageUrls: ImmutableList<String?>,
    countLabel: String,
    modifier: Modifier = Modifier,
    coverImageUrl: String? = null,
    coverBackgroundColor: Color = MinoInvitationDefaults.coverBackgroundColor,
) {
    Box(
        modifier = modifier.size(InvitationTokens.EnvelopeSize),
    ) {
        EnvelopeLayer(
            res = R.drawable.invitation_envelope_back,
            top = InvitationTokens.EnvelopeBackTop,
            height = InvitationTokens.EnvelopeBackHeight,
        )

        MinoInvitationCard(
            title = title,
            description = description,
            participantImageUrls = participantImageUrls,
            countLabel = countLabel,
            modifier = Modifier.align(Alignment.TopCenter),
            coverImageUrl = coverImageUrl,
            coverBackgroundColor = coverBackgroundColor,
        )

        EnvelopeLayer(
            res = R.drawable.invitation_envelope_front,
            top = InvitationTokens.EnvelopeFrontTop,
            height = InvitationTokens.EnvelopeFrontHeight,
        )
    }
}

@Composable
private fun EnvelopeLayer(
    res: Int,
    top: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = modifier
            .offset(y = top)
            .fillMaxWidth()
            .height(height),
        contentScale = ContentScale.FillBounds,
    )
}

@UiModePreviews
@Composable
private fun MinoInvitationPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        Box(
            modifier = modifier
                .background(MinoAndroidTheme.colors.backgroundNormalAlternative)
                .padding(24.dp),
        ) {
            MinoInvitation(
                title = "5월의 약속 : 우리끼리",
                description = "우리 모임 장소 픽업 공간.",
                participantImageUrls = persistentListOf(null, null, null),
                countLabel = "장소 999+개",
                coverBackgroundColor = Color(0xFFFEECFB),
            )
        }
    }
}
