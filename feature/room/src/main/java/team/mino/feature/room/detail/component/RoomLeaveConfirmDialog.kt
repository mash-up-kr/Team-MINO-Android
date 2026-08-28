package team.mino.feature.room.detail.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * 나가기 확인 모달([SYS-007] Flow A — 일반 멤버, `LeaveDialogState.ConfirmMember`).
 *
 * [PlaceDeleteConfirmDialog]와 동일하게, 이 저장소·`:core:design-system` 어디에도 확인 다이얼로그
 * 컴포넌트 선례가 없고 이 모달에 대응하는 Figma 노드도 브리프에 없어 Compose Material3
 * [AlertDialog]에 [MinoAndroidTheme] 색·타이포 토큰만 입혀 최소 구현한다.
 *
 * 정확한 문구는 spec.md·PRD 어디에도 지정이 없다 — [TBD] 아래 문구는 합리적 추정으로 채운 최소
 * 구현이며, 실제 디자인·문구 대조는 후속 Figma 노드 확보가 선행돼야 한다.
 *
 * @param onConfirm [나가기] 클릭 — `RoomRepository.leaveRoom(roomId)` 호출로 이어진다.
 * @param onCancel [취소] 클릭. 되돌리기 없이 모달만 닫는다.
 * @param onDismiss 모달 바깥을 눌러 닫으려 할 때 호출된다. [onCancel]과 같은 동작이라 하나로 합친다.
 */
@Composable
internal fun RoomLeaveConfirmDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = onCancel,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            // [TBD] 정확한 문구는 Figma·PRD 대조 필요.
            Text(
                text = "이 방에서 나갈까요?",
                style = MinoAndroidTheme.typography.body1NormalBold,
                color = MinoAndroidTheme.colors.labelNormal,
            )
        },
        text = {
            // [TBD] 정확한 문구는 Figma·PRD 대조 필요.
            Text(
                text = "나가면 이 방의 장소와 대화 내용을 더 이상 볼 수 없어요.",
                style = MinoAndroidTheme.typography.label1NormalRegular,
                color = MinoAndroidTheme.colors.labelAlternative,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "나가기",
                    style = MinoAndroidTheme.typography.body2NormalMedium,
                    color = MinoAndroidTheme.colors.statusNegative,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = "취소",
                    style = MinoAndroidTheme.typography.body2NormalMedium,
                    color = MinoAndroidTheme.colors.labelAlternative,
                )
            }
        },
        containerColor = MinoAndroidTheme.colors.backgroundElevatedNormal,
    )
}
