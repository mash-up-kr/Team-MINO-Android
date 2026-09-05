package team.mino.feature.notifications.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.notifications.R

/**
 * 첫 페이지를 못 받았을 때 목록 대신 서는 자리. 안내 한 줄과 재시도 버튼이 전부다(spec UX-002·EC-001).
 *
 * **[NotificationEmptyContent]와 다른 얼굴이어야 한다.** 조회에 실패한 것을 `받은 알림이 없어요`로 덮으면
 * 사용자가 「알림이 없다」와 「알림을 못 불러왔다」를 구분할 수 없다 — 스팟 일러스트도 그 문구도 여기 쓰지
 * 않는 이유다.
 *
 * **추가 로드 실패는 이 화면으로 오지 않는다.** 이미 그린 목록을 덮지 않고 목록 끝에서만 알리므로
 * [NotificationListFooter]가 맡는다(spec UX-012·EC-016).
 *
 * 오류의 종류를 인자로 받지 않는다 — 화면 상태가 첫 페이지 실패 하나뿐이고(`NotificationPhase.Error`), 리프별
 * 문구 매핑은 도메인 에러를 수집하는 Route의 몫이다(에러 처리 규약 §5).
 */
@Composable
internal fun NotificationErrorContent(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ContentHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = MessageButtonSpacing,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Text(
            text = stringResource(R.string.notification_error_load_failed),
            color = MinoAndroidTheme.colors.labelNeutral,
            style = MinoAndroidTheme.typography.body1NormalMedium,
            textAlign = TextAlign.Center,
        )
        MinoButton(
            text = stringResource(R.string.notification_error_retry),
            onClick = onRetryClick,
            size = ButtonSize.Medium,
            style = ButtonStyle.OutlinedAssistive,
        )
    }
}

private val ContentHorizontalPadding = 20.dp

// 이 화면에는 대조할 디자인이 없다. 문구와 버튼 사이는 같은 자리의 선례(장소 상세의 로드 실패 화면)를 따라
// 둔 값이고, 디자인이 그려지면 그때 맞춘다. 문구를 [NotificationListFooter]보다 진하게 둔 것도 그때 함께 본다
private val MessageButtonSpacing = 16.dp

@UiModePreviews
@Composable
private fun NotificationErrorContentPreview() {
    MinoAndroidAppTheme {
        NotificationErrorContent(onRetryClick = {})
    }
}
