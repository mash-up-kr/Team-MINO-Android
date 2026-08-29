package team.mino.feature.placedetail.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import team.mino.feature.placedetail.R

/**
 * 코멘트가 한 건도 없을 때 `친구들의 코멘트` 영역을 대신 채우는 자리(spec FR-011·TS-019).
 *
 * 마지막 남은 코멘트를 지웠을 때에도 같은 자리로 돌아온다(spec EC-014) — 그 전환을 판단하는 것은 목록이고,
 * 이 컴포저블은 자기 그림만 안다.
 *
 * 일러스트는 여러 겹의 벡터가 겹쳐진 정적 삽화라 코드로 다시 조립하지 않고 밀도별 래스터 한 장으로 둔다
 * (`docs/conventions/component-asset-placement.md` §1.1). 이 화면 말고 쓰는 곳이 없어 자리는 이 feature다.
 * 글자가 같은 것을 말하므로 그림에는 접근성 설명을 달지 않는다.
 */
@Composable
internal fun PlaceCommentEmpty(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = BottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(IllustrationTextSpacing),
    ) {
        Image(
            modifier = Modifier.width(IllustrationWidth),
            painter = painterResource(R.drawable.placedetail_comment_empty),
            contentDescription = null,
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.placedetail_comment_empty),
            color = MinoAndroidTheme.colors.labelAlternative,
            style = MinoAndroidTheme.typography.body1ReadingRegular,
            textAlign = TextAlign.Center,
        )
    }
}

private val IllustrationWidth = 220.dp

private val IllustrationTextSpacing = 24.dp

private val BottomPadding = 20.dp

@UiModePreviews
@Composable
private fun PlaceCommentEmptyPreview() {
    MinoAndroidAppTheme {
        PlaceCommentEmpty()
    }
}
