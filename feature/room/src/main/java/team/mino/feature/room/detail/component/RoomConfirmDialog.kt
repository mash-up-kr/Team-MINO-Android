package team.mino.feature.room.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.clickable.singleClickable
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 방 상세의 확인 모달 공통 뼈대 — [PlaceDeleteConfirmDialog]("이 장소를 삭제할까요?")와 같은 딤·카드·
 * 버튼 배치를 [RoomLeaveConfirmDialog]·[RoomOwnerLeaveDialog]도 함께 쓴다. 셋 다 대응하는 Figma 노드가
 * 브리프에 없어 이 카드 하나를 표준으로 삼는다 — 모달마다 다른 카드 스타일을 새로 만들지 않는다.
 *
 * [PlaceDeleteConfirmDialog]와 마찬가지로 창 기본 스크림을 끄고 [MinoAndroidTheme.colors.materialDimmer]
 * 하나로만 어둡게 한다.
 */
@Composable
internal fun RoomConfirmDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        LaunchedEffect(dialogWindow) { dialogWindow?.setDimAmount(0f) }

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MinoAndroidTheme.colors.materialDimmer)
                    .singleClickable(onClick = onDismiss),
            )
            content()
        }
    }
}

/**
 * 딤 위에 뜨는 카드 — 제목(+선택적 설명 또는 커스텀 [content]) 아래 취소/확인 버튼 두 개를 절반씩
 * 나눠 채운다. [confirmEnabled]가 `false`면 확인 버튼이 눌리지 않고 비활성 색으로 바뀐다
 * (예: [RoomOwnerLeaveDialog]의 위임 대상 미선택 상태).
 */
@Composable
internal fun RoomConfirmDialogCard(
    title: String,
    cancelText: String,
    onCancel: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    confirmEnabled: Boolean = true,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .width(RoomConfirmDialogTokens.ContainerWidth)
            .surface(
                shape = RoomConfirmDialogTokens.ContainerShape,
                containerColor = MinoAndroidTheme.colors.backgroundNormalNormal,
                borderColor = MinoAndroidTheme.colors.lineNormalAlternative,
                borderWidth = RoomConfirmDialogTokens.ContainerBorderWidth,
            )
            // 카드가 히트 테스트에 잡혀야 그 위의 탭이 뒤의 딤으로 내려가지 않는다.
            .pointerInput(Unit) {}
            .padding(
                start = RoomConfirmDialogTokens.ContainerHorizontalPadding,
                end = RoomConfirmDialogTokens.ContainerHorizontalPadding,
                top = RoomConfirmDialogTokens.ContainerTopPadding,
                bottom = RoomConfirmDialogTokens.ContainerBottomPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RoomConfirmDialogTokens.TitleActionSpacing),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RoomConfirmDialogTokens.TitleDescriptionSpacing),
        ) {
            Text(
                text = title,
                style = MinoAndroidTheme.typography.body1NormalBold,
                color = MinoAndroidTheme.colors.labelNormal,
                textAlign = TextAlign.Center,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MinoAndroidTheme.typography.label2Regular,
                    color = MinoAndroidTheme.colors.labelNeutral,
                    textAlign = TextAlign.Center,
                )
            }
            content?.invoke(this)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RoomConfirmDialogTokens.ActionSpacing),
        ) {
            RoomConfirmDialogButton(
                text = cancelText,
                containerColor = MinoAndroidTheme.colors.backgroundNormalAlternative,
                // Figma는 Static/Black을 물리지만 그 색은 모드와 무관해, 배경이 뒤집히는 다크에서
                // 어두운 배경 위 검은 글자가 된다. LabelStrong은 배경과 함께 반전된다.
                contentColor = MinoAndroidTheme.colors.labelStrong,
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            )
            RoomConfirmDialogButton(
                text = confirmText,
                containerColor = MinoAndroidTheme.colors.primaryNormal,
                // 위와 같은 이유로 Static/White 대신 InversePrimary를 쓴다. Primary/Normal이 다크에서
                // 흰색이 되므로 글자도 함께 반전되어야 대비가 남는다.
                contentColor = MinoAndroidTheme.colors.inversePrimary,
                onClick = onConfirm,
                enabled = confirmEnabled,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** 모달 전용 버튼. 두 버튼이 [Modifier.weight]로 카드 너비를 절반씩 나눠 채운다. */
@Composable
private fun RoomConfirmDialogButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val resolvedContainerColor = if (enabled) containerColor else MinoAndroidTheme.colors.interactionDisable
    Box(
        modifier = modifier
            .height(RoomConfirmDialogTokens.ActionHeight)
            .surface(shape = RoomConfirmDialogTokens.ActionShape, containerColor = resolvedContainerColor)
            .rippleSingleClickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MinoAndroidTheme.typography.body1NormalMedium,
            color = if (enabled) contentColor else MinoAndroidTheme.colors.labelDisable,
        )
    }
}

/** [RoomConfirmDialogCard] 치수 토큰 — [PlaceDeleteConfirmDialog]와 같은 값(Figma node `3222-87796`·`3222-87800`). */
private object RoomConfirmDialogTokens {
    val ContainerWidth = 300.dp
    val ContainerShape = RoundedCornerShape(20.dp)
    val ContainerBorderWidth = 1.dp
    val ContainerHorizontalPadding = 24.dp
    val ContainerTopPadding = 24.dp
    val ContainerBottomPadding = 20.dp
    val TitleActionSpacing = 28.dp
    val TitleDescriptionSpacing = 4.dp
    val ActionSpacing = 12.dp
    val ActionHeight = 44.dp
    val ActionShape = RoundedCornerShape(8.dp)
}
