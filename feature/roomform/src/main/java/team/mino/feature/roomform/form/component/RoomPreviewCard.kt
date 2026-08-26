package team.mino.feature.roomform.form.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.RoomColor
import team.mino.feature.roomform.R
import team.mino.feature.roomform.form.model.thumbnailRes

/**
 * 폼 위쪽에서 현재 입력값을 그대로 보여주는 미리보기 카드.
 *
 * 세 입력값만 받아 그리고 검증 결과는 받지 않는다 — 방 이름이 오류 상태여도 카드는 현재 입력값을
 * 그대로 반영한다. 오류 표시는 입력 필드와 CTA의 몫이다.
 *
 * @param name 방 이름. 비어 있으면 이름 자리에 입력 안내 문구를 대신 그린다.
 * @param description 방 설명. 길면 줄바꿈해 여러 줄이 된다. 비어 있으면 한 줄로 잘리는 입력 안내 문구를 대신 그린다.
 * @param color 대표 색상. `null`이면 아직 고르지 않은 것이라 회색 썸네일이 놓인다.
 */
@Composable
internal fun RoomPreviewCard(
    name: String,
    description: String,
    color: RoomColor?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .surface(
                shape = CardShape,
                containerColor = MinoAndroidTheme.colors.backgroundElevatedNormal,
                borderColor = MinoAndroidTheme.colors.lineSolidNormal,
                borderWidth = CardBorderWidth,
            ).padding(CardPadding),
        horizontalArrangement = Arrangement.spacedBy(ThumbnailSpacing),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(color.thumbnailRes),
            contentDescription = null,
            modifier = Modifier
                .size(ThumbnailSize)
                .clip(ThumbnailShape),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(TextSpacing),
        ) {
            Text(
                text = name.ifEmpty { stringResource(R.string.roomform_name_placeholder) },
                style = MinoAndroidTheme.typography.body1NormalBold,
                color = MinoAndroidTheme.colors.labelNormal,
            )
            val hasDescription = description.isNotEmpty()
            Text(
                text = if (hasDescription) description else stringResource(R.string.roomform_description_placeholder),
                style = MinoAndroidTheme.typography.caption2Medium,
                color = MinoAndroidTheme.colors.labelAlternative,
                maxLines = if (hasDescription) Int.MAX_VALUE else 1,
                overflow = if (hasDescription) TextOverflow.Clip else TextOverflow.Ellipsis,
            )
        }
    }
}

private val CardShape = RoundedCornerShape(20.dp)

private val CardBorderWidth = 1.dp

private val CardPadding = 14.dp

private val ThumbnailSpacing = 12.dp

private val ThumbnailSize = 80.dp

// Figma Radius 변수 대응 — 토큰 미존재
private val ThumbnailShape = RoundedCornerShape(14.dp)

private val TextSpacing = 4.dp

@UiModePreviews
@Composable
private fun RoomPreviewCardPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .background(MinoAndroidTheme.colors.backgroundNormalNormal)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            RoomPreviewCard(name = "", description = "", color = null)
            RoomPreviewCard(
                name = "민호야 잘하자",
                description = "팀 회식 장소 모음",
                color = RoomColor.CYAN,
            )
        }
    }
}
