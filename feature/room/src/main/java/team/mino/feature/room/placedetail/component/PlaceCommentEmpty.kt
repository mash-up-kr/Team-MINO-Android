package team.mino.feature.room.placedetail.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.room.R

/**
 * 코멘트가 한 건도 없을 때 `친구들의 코멘트` 영역을 대신 채우는 자리(spec FR-011·TS-019).
 *
 * 마지막 남은 코멘트를 지웠을 때에도 같은 자리로 돌아온다(spec EC-014) — 그 전환을 판단하는 것은 목록이고,
 * 이 컴포저블은 자기 그림만 안다.
 *
 * 일러스트는 손으로 그린 획이 그대로 살아 있는 정적 삽화라 코드로 다시 조립하지 않고 밀도별 래스터 한 장으로
 * 둔다(`docs/conventions/component-asset-placement.md` §1.1). 이 화면 말고 쓰는 곳이 없어 자리는 이
 * feature다. 글자가 같은 것을 말하므로 그림에는 접근성 설명을 달지 않는다.
 */
@Composable
internal fun PlaceCommentEmpty(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = ContentVerticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(IllustrationTextSpacing),
    ) {
        Image(
            modifier = Modifier.size(IllustrationSize),
            painter = painterResource(R.drawable.placedetail_comment_empty),
            contentDescription = null,
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.placedetail_comment_empty),
            color = MinoAndroidTheme.colors.labelNeutral,
            style = MinoAndroidTheme.typography.headline2Medium,
            textAlign = TextAlign.Center,
        )
    }
}

/** 시안이 이 자리를 정사각으로 잡아 두어 그림이 상자 안에서 세로를 채운다. */
private val IllustrationSize = 209.dp

private val IllustrationTextSpacing = 24.dp

/** 좌우는 이 자리를 내주는 코멘트 섹션이 이미 냈으므로 세로만 든다. */
private val ContentVerticalPadding = 20.dp

@UiModePreviews
@Composable
private fun PlaceCommentEmptyPreview() {
    MinoAndroidAppTheme {
        PlaceCommentEmpty()
    }
}
