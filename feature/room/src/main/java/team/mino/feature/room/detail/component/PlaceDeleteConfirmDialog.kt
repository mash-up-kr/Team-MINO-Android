package team.mino.feature.room.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
 * 장소 삭제 확인 모달(UX-001, FR-010, Figma `004-1-3-1_장소 삭제 클릭시` 딤 처리, node `3222-87768`).
 * `Card_Location` 목록 위로 딤이 깔리고 카드 한 장이 화면 중앙에 뜬다.
 *
 * `:feature:roomform`의 `RoomFormConfirmDialog`와 같은 딤·`Dialog` 배선을 쓴다(모듈이 달라 그대로
 * 재사용할 수 없어 이 모듈 안에 다시 둔다) — 창 기본 스크림을 끄고 [MinoAndroidTheme.colors.materialDimmer]
 * 하나로만 어둡게 하는 이유는 그쪽 KDoc 참고. 다만 이 모달은 제목 아래 설명 문구가 한 줄 더 있고
 * (`RoomFormConfirmDialog`는 제목만 있음), 버튼 두 개가 고정 폭이 아니라 카드 너비를 절반씩 나눠 채운다
 * — Figma 실측이 서로 달라 각자 값을 쓴다.
 *
 * @param onConfirm [삭제] 클릭(FR-010 — 해당 방에서만 장소 제거).
 * @param onCancel [취소] 클릭 · 딤 바깥 탭이 모두 이 하나로 올라온다. 사용자가 고르지 않은 결과를
 *   만들지 않으려면 셋의 처리가 같아야 하므로 콜백을 나누지 않는다.
 */
@Composable
internal fun PlaceDeleteConfirmDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onCancel,
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

        PlaceDeleteConfirmDialogOverlay(
            onConfirm = onConfirm,
            onCancel = onCancel,
            modifier = modifier,
        )
    }
}

/**
 * 딤 레이어와 그 위의 카드. 딤이 화면 전체를 덮어 뒤의 카드 목록은 떠 있는 동안 터치를 받지 못한다.
 */
@Composable
internal fun PlaceDeleteConfirmDialogOverlay(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MinoAndroidTheme.colors.materialDimmer)
                .singleClickable(onClick = onCancel),
        )
        Column(
            modifier = Modifier
                .width(PlaceDeleteDialogTokens.ContainerWidth)
                .surface(
                    shape = PlaceDeleteDialogTokens.ContainerShape,
                    containerColor = MinoAndroidTheme.colors.backgroundNormalNormal,
                    borderColor = MinoAndroidTheme.colors.lineNormalAlternative,
                    borderWidth = PlaceDeleteDialogTokens.ContainerBorderWidth,
                )
                // 카드가 히트 테스트에 잡혀야 그 위의 탭이 뒤의 딤으로 내려가지 않는다.
                .pointerInput(Unit) {}
                .padding(
                    start = PlaceDeleteDialogTokens.ContainerHorizontalPadding,
                    end = PlaceDeleteDialogTokens.ContainerHorizontalPadding,
                    top = PlaceDeleteDialogTokens.ContainerTopPadding,
                    bottom = PlaceDeleteDialogTokens.ContainerBottomPadding,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PlaceDeleteDialogTokens.TitleActionSpacing),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PlaceDeleteDialogTokens.TitleDescriptionSpacing),
            ) {
                Text(
                    text = "이 장소를 삭제할까요?",
                    style = MinoAndroidTheme.typography.body1NormalBold,
                    color = MinoAndroidTheme.colors.labelNormal,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "장소에 등록된 사진과 댓글이 모두 삭제되며,\n다시 되돌릴 수 없어요.",
                    style = MinoAndroidTheme.typography.label2Regular,
                    color = MinoAndroidTheme.colors.labelNeutral,
                    textAlign = TextAlign.Center,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PlaceDeleteDialogTokens.ActionSpacing),
            ) {
                PlaceDeleteDialogButton(
                    text = "취소",
                    containerColor = MinoAndroidTheme.colors.backgroundNormalAlternative,
                    // Figma는 Static/Black을 물리지만 그 색은 모드와 무관해, 배경이 뒤집히는
                    // 다크에서 어두운 배경 위 검은 글자가 된다. LabelStrong은 배경과 함께 반전된다.
                    contentColor = MinoAndroidTheme.colors.labelStrong,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                )
                PlaceDeleteDialogButton(
                    text = "삭제",
                    containerColor = MinoAndroidTheme.colors.primaryNormal,
                    // 위와 같은 이유로 Static/White 대신 InversePrimary를 쓴다. Primary/Normal이
                    // 다크에서 흰색이 되므로 글자도 함께 반전되어야 대비가 남는다.
                    contentColor = MinoAndroidTheme.colors.inversePrimary,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * 모달 전용 버튼. 두 버튼이 [Modifier.weight]로 카드 너비를 절반씩 나눠 채운다.
 */
@Composable
private fun PlaceDeleteDialogButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(PlaceDeleteDialogTokens.ActionHeight)
            .surface(shape = PlaceDeleteDialogTokens.ActionShape, containerColor = containerColor)
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

/** [PlaceDeleteConfirmDialog] 치수 토큰 — Figma node `3222-87796`(카드)·`3222-87800`(버튼 행) 실측값. */
private object PlaceDeleteDialogTokens {
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
