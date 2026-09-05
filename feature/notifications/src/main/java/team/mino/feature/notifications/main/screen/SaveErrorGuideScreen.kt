package team.mino.feature.notifications.main.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.topnavigation.MinoTopNavigation
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.notifications.R

/**
 * 저장 오류 알림을 눌렀을 때 서는 안내 화면. 스팟 일러스트와 제목, 안내 세 줄이 전부다
 * (spec FR-010·TS-028).
 *
 * **ViewModel도 파라미터도 갖지 않는다**(spec EC-013). 어느 저장 오류 알림을 눌렀든 같은 화면이라
 * 알림별로 달라지는 값이 없다 — 실패한 원본 링크별 사유를 여기서 보여 주지 않는다.
 *
 * 바텀 네비게이션은 이 화면이 그리지 않는다(spec TS-030). 알림 탭 그래프 안의 목적지라 셸의 하단 바가
 * 그대로 남는 것이고, 여기서 다시 그리면 셸의 것과 겹친다.
 *
 * 배경을 칠하지 않는다 — 셸의 `MinoScaffold`가 이미 깔았다.
 *
 * @param onBackClick 상단 뒤로가기. 목록으로 되돌리는 것은 호출부의 몫이다(spec FR-011·TS-029).
 */
@Composable
internal fun SaveErrorGuideScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        // 제목 없이 뒤로가기만 두는 표시줄이다. 우측 액션도 없다.
        MinoTopNavigation(title = "", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = ContentTopOffset),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = IllustrationMessageSpacing,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            Image(
                painter = painterResource(R.drawable.notification_save_error_illustration),
                contentDescription = null,
                modifier = Modifier.size(IllustrationSize),
            )
            Column(
                modifier = Modifier.width(MessageWidth),
                verticalArrangement = Arrangement.spacedBy(TitleGuideSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.notification_save_error_guide_title),
                    color = MinoAndroidTheme.colors.labelStrong,
                    style = MinoAndroidTheme.typography.headline1Bold,
                    textAlign = TextAlign.Center,
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(GuideLineSpacing),
                ) {
                    GuideLines.forEach { line ->
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(line),
                            color = MinoAndroidTheme.colors.labelAlternative,
                            style = MinoAndroidTheme.typography.label1NormalRegular,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

/** 서버가 주지 않는 고정 문구다(spec FR-010) — 순서까지 화면이 들고 있다. */
private val GuideLines = listOf(
    R.string.notification_save_error_guide_line_1,
    R.string.notification_save_error_guide_line_2,
    R.string.notification_save_error_guide_line_3,
)

private val ContentTopOffset = 17.dp

private val IllustrationSize = 197.dp

private val IllustrationMessageSpacing = 40.dp

private val MessageWidth = 235.dp

private val TitleGuideSpacing = 12.dp

private val GuideLineSpacing = 8.dp

@UiModePreviews
@Composable
private fun SaveErrorGuideScreenPreview() {
    MinoAndroidAppTheme {
        SaveErrorGuideScreen(onBackClick = {})
    }
}
