package team.mino.core.designsystem.component.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import team.mino.core.designsystem.component.button.MinoTextButton
import team.mino.core.designsystem.component.button.TextButtonStyle

/**
 * 제목·본문과 확인/취소 두 버튼을 갖는 범용 알림 다이얼로그.
 *
 * 디자인 시스템 라이브러리에서 대응하는 Figma 컴포넌트 정의 노드를 찾지 못해(근거는
 * [team.mino.core.designsystem.component.dialog.token.DialogTokens] 참조) Material3 `AlertDialog`의
 * 기본 구조를 그대로 쓰고 색·타이포·셰이프만 디자인 토큰으로 대체한 임시 구현이다.
 *
 * 화면 전용 문구를 하드코딩하지 않는다 — [title]·[message]와 두 버튼 라벨을 모두 호출부가
 * 채운다. "이미 부여된 권한은 앱이 취소할 수 없다"·"영구 거부로 시스템 팝업이 다시 뜨지 않는다"
 * 같은 안내에, OS 앱 설정 화면 이동을 유도하는 용도로 쓸 수 있다.
 *
 * @param onDismissRequest 시스템 뒤로가기·바깥 영역 클릭으로 닫힐 때의 콜백. [properties]가
 *   그 경로를 막아 두었으면 호출되지 않는다.
 * @param confirmLabel 오른쪽(끝) 버튼 라벨.
 * @param onConfirmClick 오른쪽 버튼 클릭 콜백.
 * @param cancelLabel 왼쪽(시작) 버튼 라벨.
 * @param onCancelClick 왼쪽 버튼 클릭 콜백.
 */
@Composable
fun MinoDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirmClick: () -> Unit,
    cancelLabel: String,
    onCancelClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        confirmButton = {
            MinoTextButton(text = confirmLabel, onClick = onConfirmClick)
        },
        dismissButton = {
            MinoTextButton(text = cancelLabel, onClick = onCancelClick, style = TextButtonStyle.Assistive)
        },
        title = {
            Text(text = title, style = MinoDialogDefaults.titleTextStyle, color = MinoDialogDefaults.titleColor)
        },
        text = {
            Text(text = message, style = MinoDialogDefaults.messageTextStyle, color = MinoDialogDefaults.messageColor)
        },
        shape = MinoDialogDefaults.shape,
        containerColor = MinoDialogDefaults.containerColor,
        // M3 기본 tonalElevation은 MaterialTheme 표면 틴트를 얹는다. 우리는 containerColor를
        // 토큰으로 직접 지정하므로 틴트가 겹치지 않도록 0으로 끈다.
        tonalElevation = 0.dp,
        properties = properties,
    )
}
