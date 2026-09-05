package team.mino.feature.notifications.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
 * 목록 맨 끝에 붙는 자리. 다음 묶음을 받는 중이면 진행 표시를, 그 조회가 깨졌으면 재시도를 그린다
 * (spec UX-011·UX-012·EC-016).
 *
 * **이미 그린 행을 건드리지 않는다.** 추가 로드가 실패해도 화면 전체가 오류로 바뀌지 않으므로
 * ([NotificationErrorContent]와 갈리는 지점), 실패는 목록 끝의 이 한 자리에서만 알린다.
 *
 * **`더 보기` 버튼이 아니다**(spec UX-011). 다음 묶음은 목록 끝에 닿는 것만으로 요청되고, 여기 서는 버튼은
 * 그 요청이 **실패했을 때만** 나타난다.
 *
 * 그릴 것이 없으면 아무것도 내보내지 않는다 — 목록 끝에 빈 높이가 남으면 마지막 행 아래로 까닭 없는 여백이
 * 생기고, 그만큼 끝 도달 판정이 앞당겨진다.
 *
 * [isAppending]과 [appendError]는 동시에 서지 않는다. 추가 조회가 깨질 때 ViewModel이 [isAppending]을 함께
 * 내리기 때문이며, 그래도 [appendError]를 먼저 보는 것은 둘이 겹쳤을 때 사용자가 조작할 수 있는 쪽을 남기기
 * 위해서다.
 */
@Composable
internal fun NotificationListFooter(
    isAppending: Boolean,
    appendError: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!isAppending && !appendError) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = FooterHorizontalPadding, vertical = FooterVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        if (appendError) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MessageButtonSpacing),
            ) {
                Text(
                    text = stringResource(R.string.notification_append_error),
                    color = MinoAndroidTheme.colors.labelAlternative,
                    style = MinoAndroidTheme.typography.label1NormalRegular,
                    textAlign = TextAlign.Center,
                )
                MinoButton(
                    text = stringResource(R.string.notification_error_retry),
                    onClick = onRetryClick,
                    size = ButtonSize.Small,
                    style = ButtonStyle.OutlinedAssistive,
                )
            }
        } else {
            CircularProgressIndicator(color = MinoAndroidTheme.colors.primaryNormal)
        }
    }
}

private val FooterHorizontalPadding = 20.dp

// 목록 끝의 이 자리에는 대조할 디자인이 없다. 세로 여백은 마지막 행과의 간격이 행 사이 간격과 같아지도록
// [NotificationRow]의 세로 패딩을 따랐고, 문구와 버튼 사이는 [NotificationErrorContent]와 같은 값으로 둔다.
// 디자인이 그려지면 두 값과, 문구를 [NotificationErrorContent]보다 흐리게 둔 것을 함께 맞춘다
private val FooterVerticalPadding = 12.dp
private val MessageButtonSpacing = 16.dp

/** 다음 묶음을 받는 중 — 목록 끝에 진행 표시만 선다. */
@UiModePreviews
@Composable
private fun NotificationListFooterPreview() {
    MinoAndroidAppTheme {
        NotificationListFooter(isAppending = true, appendError = false, onRetryClick = {})
    }
}

/** 추가 조회가 깨진 뒤 — 같은 자리가 재시도로 바뀐다. */
@UiModePreviews
@Composable
private fun NotificationListFooterAppendErrorPreview() {
    MinoAndroidAppTheme {
        NotificationListFooter(isAppending = false, appendError = true, onRetryClick = {})
    }
}
