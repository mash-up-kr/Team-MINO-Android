package team.mino.feature.roomform.form.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.clickable.singleClickable
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.roomform.R

/**
 * 폼 위에 얹히는 확인 모달. 제목 한 줄과 버튼 두 개가 전부이며 본문 문구를 두지 않는다.
 *
 * **어느 모달인지 모른다.** 저장 확인이든 이탈 확인이든 이 컴포저블에게는 제목과 버튼 라벨의 차이일
 * 뿐이라, 종류를 뜻하는 타입을 받지 않는다. 무엇을 띄울지 아는 것은 화면이다.
 *
 * 둘 이상이 동시에 뜨지 않는 것은 상태의 단일 슬롯이 보장하므로 여기서 다시 막지 않는다.
 *
 * @param onConfirm 파괴적 선택([confirmLabel] 버튼)을 명시적으로 고른 경우에만 불린다.
 * @param onDismiss 취소 버튼 · 딤 바깥 탭 · 뒤로가기가 모두 이 하나로 올라온다. 사용자가 고르지
 *  않은 결과를 만들지 않으려면 셋의 처리가 같아야 하므로 콜백을 나누지 않는다.
 */
@Composable
internal fun RoomFormConfirmDialog(
    title: String,
    cancelLabel: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            // 딤을 창 전체로 넓히려면 창이 화면을 다 쓰고 시스템 바 뒤까지 그려야 한다.
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        // 창이 기본으로 까는 스크림을 끈다. 그 스크림은 검정의 불투명도만 조절할 수 있어
        // 디자인이 지정한 딤 색을 낼 수 없고, 오버레이가 그리는 딤과 겹쳐 두 번 어두워진다.
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        LaunchedEffect(dialogWindow) { dialogWindow?.setDimAmount(0f) }

        ConfirmDialogOverlay(
            title = title,
            cancelLabel = cancelLabel,
            confirmLabel = confirmLabel,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            modifier = modifier,
        )
    }
}

/**
 * 딤 레이어와 그 위의 카드. 딤이 화면 전체를 덮어 뒤의 폼은 떠 있는 동안 터치를 받지 못한다.
 */
@Composable
private fun ConfirmDialogOverlay(
    title: String,
    cancelLabel: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(MinoAndroidTheme.colors.materialDimmer)
                .singleClickable(onClick = onDismiss),
        )
        Column(
            modifier = Modifier
                .width(ContainerWidth)
                .surface(
                    shape = ContainerShape,
                    containerColor = MinoAndroidTheme.colors.backgroundNormalNormal,
                    borderColor = MinoAndroidTheme.colors.lineNormalAlternative,
                    borderWidth = ContainerBorderWidth,
                )
                // 카드가 히트 테스트에 잡혀야 그 위의 탭이 뒤의 딤으로 내려가지 않는다.
                .pointerInput(Unit) {}
                .padding(
                    start = ContainerHorizontalPadding,
                    end = ContainerHorizontalPadding,
                    top = ContainerTopPadding,
                    bottom = ContainerBottomPadding,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TitleActionSpacing),
        ) {
            Text(
                text = title,
                style = MinoAndroidTheme.typography.body1ReadingBold,
                color = MinoAndroidTheme.colors.labelNormal,
                textAlign = TextAlign.Center,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(ActionSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ConfirmDialogButton(
                    text = cancelLabel,
                    containerColor = MinoAndroidTheme.colors.backgroundNormalAlternative,
                    // Figma는 Static/Black을 물리지만 그 색은 모드와 무관해, 배경이 뒤집히는
                    // 다크에서 어두운 배경 위 검은 글자가 된다. LabelStrong은 배경과 함께 반전된다.
                    contentColor = MinoAndroidTheme.colors.labelStrong,
                    onClick = onDismiss,
                )
                ConfirmDialogButton(
                    text = confirmLabel,
                    containerColor = MinoAndroidTheme.colors.primaryNormal,
                    // 위와 같은 이유로 Static/White 대신 InversePrimary를 쓴다. Primary/Normal이
                    // 다크에서 흰색이 되므로 글자도 함께 반전되어야 대비가 남는다.
                    contentColor = MinoAndroidTheme.colors.inversePrimary,
                    onClick = onConfirm,
                )
            }
        }
    }
}

/**
 * 모달 전용 버튼. 크기가 고정이라 두 버튼이 라벨 길이와 무관하게 같은 폭으로 놓인다.
 */
@Composable
private fun ConfirmDialogButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = ActionWidth, height = ActionHeight)
            .surface(shape = ActionShape, containerColor = containerColor)
            .rippleSingleClickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MinoAndroidTheme.typography.body1NormalMedium,
            color = contentColor,
        )
    }
}

private val ContainerWidth = 300.dp

private val ContainerShape = RoundedCornerShape(24.dp)

private val ContainerBorderWidth = 1.dp

private val ContainerHorizontalPadding = 24.dp

private val ContainerTopPadding = 24.dp

private val ContainerBottomPadding = 20.dp

private val TitleActionSpacing = 28.dp

private val ActionSpacing = 12.dp

private val ActionWidth = 120.dp

private val ActionHeight = 44.dp

private val ActionShape = RoundedCornerShape(8.dp)

@UiModePreviews
@Composable
private fun RoomFormConfirmDialogPreview() {
    MinoAndroidAppTheme {
        ConfirmDialogOverlay(
            title = stringResource(R.string.roomform_dialog_save_title),
            cancelLabel = stringResource(R.string.roomform_dialog_cancel),
            confirmLabel = stringResource(R.string.roomform_dialog_save_confirm),
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@UiModePreviews
@Composable
private fun RoomFormConfirmExitDialogPreview() {
    MinoAndroidAppTheme {
        ConfirmDialogOverlay(
            title = stringResource(R.string.roomform_dialog_exit_create_title),
            cancelLabel = stringResource(R.string.roomform_dialog_cancel),
            confirmLabel = stringResource(R.string.roomform_dialog_exit_confirm),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
