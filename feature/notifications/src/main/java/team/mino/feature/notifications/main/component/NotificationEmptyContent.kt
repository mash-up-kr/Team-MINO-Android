package team.mino.feature.notifications.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import team.mino.feature.notifications.R

/**
 * 받은 알림이 0건일 때 목록 대신 서는 자리. 스팟 일러스트와 안내 한 줄이 전부다(spec FR-006·TS-008).
 *
 * **조회 중에는 이것을 그리지 않는다**(spec UX-001). 0건이 확정됐는지의 판정은
 * [team.mino.feature.notifications.main.vm.NotificationPhase]가 이미 들고 있어, 이 컴포저블은 그 판정을 다시
 * 하지 않고 그리기만 한다 — 여기서 조건을 한 번 더 두면 판정 출처가 둘로 갈린다.
 *
 * **권한을 켜라고 권하는 배너를 두지 않는다**(spec EC-015·FR-017). 알림 권한을 거부한 사용자의 0건도 같은
 * 화면을 본다.
 *
 * 배경을 칠하지 않는다 — Figma의 이 프레임도 투명이고, 흰 바탕은 셸의 `MinoScaffold`가 이미 깔았다.
 */
@Composable
internal fun NotificationEmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = IllustrationMessageSpacing,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Image(
            painter = painterResource(R.drawable.notification_empty_illustration),
            contentDescription = null,
            modifier = Modifier.size(IllustrationSize),
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.notification_empty_message),
            color = MinoAndroidTheme.colors.labelStrong,
            style = MinoAndroidTheme.typography.headline1Bold,
            textAlign = TextAlign.Center,
        )
    }
}

private val IllustrationSize = 173.dp

private val IllustrationMessageSpacing = 25.dp

@UiModePreviews
@Composable
private fun NotificationEmptyContentPreview() {
    MinoAndroidAppTheme {
        NotificationEmptyContent()
    }
}
