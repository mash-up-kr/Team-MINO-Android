package team.mino.feature.room.main.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.actionarea.ActionAreaAction
import team.mino.core.designsystem.component.actionarea.ActionAreaVariant
import team.mino.core.designsystem.component.actionarea.MinoActionArea
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [RoomNudgeSheet] 치수 토큰. `spec.md` 유저 플로우 4는 이 화면에 별도 Figma 노드를 달지 않은
 * 텍스트 중심 화면이라([contracts/room-list-main-contract.md] Figma 절 참고) 실측 여백만 둔다.
 */
private object RoomNudgeSheetTokens {
    val HorizontalPadding = 20.dp
    val TopPadding = 32.dp
    val TitleSubtitleSpacing = 8.dp
    val SubtitleActionAreaSpacing = 24.dp
}

/**
 * 공동방 0개 사용자에게 첫 공동방 생성을 유도하는 Nudge(FR-008, [research.md D9]).
 *
 * 이 화면은 별도 Figma 노드가 없어(spec.md 유저 플로우 4) 문구는 PRD 원문을 그대로 쓰고,
 * 버튼 두 개는 `:core:design-system`의 [MinoActionArea]를 `Strong` 배치로 재사용해 조립한다
 * (공용 컴포넌트가 없으면 만들지 않는다는 규칙에 맞춰, 이미 있는 액션 영역 컴포넌트를 쓴다).
 *
 * 재노출 여부는 이 컴포저블이 아니라 호출부의 `showNudge`(=`groupRooms.isEmpty()` 파생값)가
 * 결정한다 — [onDismissClick]은 그 상태를 로컬로 한 번 접을 뿐 재계산 로직은 갖지 않는다([TS-014]).
 *
 * @param onCreateClick [공동방 만들기] 클릭(FR-008) — `NavigateToRoomForm` 재사용.
 * @param onDismissClick [나중에 만들래요] 클릭 — `showNudge`만 로컬로 `false`로 접는다.
 */
@Composable
internal fun RoomNudgeSheet(
    onCreateClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = RoomNudgeSheetTokens.HorizontalPadding,
                vertical = RoomNudgeSheetTokens.TopPadding,
            ),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "공동방을 생성해보세요!",
            color = MinoAndroidTheme.colors.labelNormal,
            style = MinoAndroidTheme.typography.title2Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = RoomNudgeSheetTokens.TitleSubtitleSpacing),
            text = "\"저번에 말한 거기가 어디였지?\" 더 이상 묻지 마세요.",
            color = MinoAndroidTheme.colors.labelAlternative,
            style = MinoAndroidTheme.typography.body2NormalRegular,
            textAlign = TextAlign.Center,
        )
        MinoActionArea(
            modifier = Modifier.padding(top = RoomNudgeSheetTokens.SubtitleActionAreaSpacing),
            variant = ActionAreaVariant.Strong,
            mainAction = ActionAreaAction(text = "공동방 만들기", onClick = onCreateClick),
            subAction = ActionAreaAction(text = "나중에 만들래요", onClick = onDismissClick),
        )
    }
}
