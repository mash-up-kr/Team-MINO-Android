package team.mino.feature.room.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.component.avatar.MinoAvatar
import team.mino.core.designsystem.component.avatar.MinoAvatarSize
import team.mino.core.designsystem.component.avatar.MinoAvatarVariant
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Check
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.selectable.rippleSingleSelectable
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.domain.model.RoomMember
import team.mino.feature.room.detail.model.image
import team.mino.feature.room.detail.vm.LeaveDialogState

/**
 * 방장 나가기 확인·위임 모달([SYS-007] Flow B).
 *
 * `docs/specs/room-detail/contracts/room-detail-main-contract.md` "분기 규칙 — 나가기 플로우"
 * 대로 [leaveDialogState]에 따라 서로 다른 콘텐츠를 그린다.
 *
 * - [LeaveDialogState.ConfirmOwnerSingle](1인 방) — 확인 문구만. `leaveRoom` 호출 시 서버가
 *   방을 자동 삭제한다. 정확한 문구는 spec.md·PRD 어디에도 지정이 없어 [RoomLeaveConfirmDialog]와
 *   같은 근거로 [TBD] 최소 구현.
 * - [LeaveDialogState.DelegateOwner](N인 방) — `leaveRoom` 호출이 `409 OWNER_TRANSFER_REQUIRED`로
 *   응답한 뒤 전이되는 상태. 위임 대상 멤버 목록(체크박스 단일 선택) + [다음] 버튼을 그린다.
 *   멤버 한 명당 레이아웃(48x48dp 원형 아바타 + 이름 + 16x16dp 체크박스)은 Figma node 2542:125613
 *   (리드가 조회) 기준이다.
 * - 그 외 상태([LeaveDialogState.None]·[LeaveDialogState.ConfirmMember])에서는 아무것도 그리지
 *   않는다 — `ConfirmMember`는 [RoomLeaveConfirmDialog]가 담당한다.
 *
 * @param leaveDialogState 현재 나가기/위임 모달 상태.
 * @param roomMembers 위임 대상 후보 목록(`RoomRepository.getMembers` 결과, 본인 제외는 호출부 책임).
 * @param selectedMemberId 현재 선택된 위임 대상 `userId`. null이면 미선택.
 * @param onMemberSelected 멤버 행 클릭 — 단일 선택으로 갱신한다.
 * @param onConfirm [ConfirmOwnerSingle]의 확인 또는 [DelegateOwner]의 [다음] 클릭.
 * @param onCancel 취소/바깥 영역 클릭 — 모달을 닫는다.
 */
@Composable
internal fun RoomOwnerLeaveDialog(
    leaveDialogState: LeaveDialogState,
    roomMembers: ImmutableList<RoomMember>,
    selectedMemberId: String?,
    onMemberSelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (leaveDialogState) {
        LeaveDialogState.ConfirmOwnerSingle -> {
            OwnerLeaveSingleConfirmDialog(onConfirm = onConfirm, onCancel = onCancel, modifier = modifier)
        }

        LeaveDialogState.DelegateOwner -> {
            OwnerDelegateDialog(
                roomMembers = roomMembers,
                selectedMemberId = selectedMemberId,
                onMemberSelected = onMemberSelected,
                onConfirm = onConfirm,
                onCancel = onCancel,
                modifier = modifier,
            )
        }

        LeaveDialogState.None, LeaveDialogState.ConfirmMember -> Unit
    }
}

/** 1인 방 방장 나가기 확인 모달 — [PlaceDeleteConfirmDialog]("이 장소를 삭제할까요?")와 같은 카드. */
@Composable
private fun OwnerLeaveSingleConfirmDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoomConfirmDialog(onDismiss = onCancel, modifier = modifier) {
        RoomConfirmDialogCard(
            // [TBD] 정확한 문구는 Figma·PRD 대조 필요.
            title = "방을 나가면 방이 삭제돼요",
            description = "나 혼자 있는 방이라, 나가면 방과 저장된 모든 장소가 함께 삭제돼요.",
            cancelText = "취소",
            onCancel = onCancel,
            confirmText = "나가기",
            onConfirm = onConfirm,
        )
    }
}

/**
 * N인 방 방장 위임 대상 선택 모달 — [PlaceDeleteConfirmDialog]("이 장소를 삭제할까요?")와 같은 카드에
 * 설명 문구 대신 위임 대상 목록을 얹는다. 멤버 행 레이아웃은 Figma node 2542:125613(리드가 조회) 기준.
 */
@Composable
private fun OwnerDelegateDialog(
    roomMembers: ImmutableList<RoomMember>,
    selectedMemberId: String?,
    onMemberSelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoomConfirmDialog(onDismiss = onCancel, modifier = modifier) {
        RoomConfirmDialogCard(
            // [TBD] 정확한 문구는 Figma·PRD 대조 필요.
            title = "다음 방장을 선택해 주세요",
            cancelText = "취소",
            onCancel = onCancel,
            confirmText = "다음",
            onConfirm = onConfirm,
            confirmEnabled = selectedMemberId != null,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = OwnerDelegateTokens.MemberListMaxHeight),
            ) {
                items(items = roomMembers, key = { it.userId }) { member ->
                    OwnerDelegateMemberRow(
                        member = member,
                        selected = member.userId == selectedMemberId,
                        onClick = { onMemberSelected(member.userId) },
                    )
                }
            }
        }
    }
}

/**
 * 위임 대상 멤버 한 행 — 48x48dp 원형 아바타 + 이름(`label1NormalMedium`, `labelAlternative`) +
 * 16x16dp 체크박스(radius 4dp). Figma node 2542:125613(리드가 조회) 기준.
 */
@Composable
private fun OwnerDelegateMemberRow(
    member: RoomMember,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .rippleSingleClickable(onClick = onClick)
            .padding(vertical = OwnerDelegateTokens.RowVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(OwnerDelegateTokens.RowContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MinoAvatar(
            variant = MinoAvatarVariant.Person,
            size = MinoAvatarSize.Large,
            profileAvatar = member.avatar.image,
            contentDescription = member.nickname,
        )

        Text(
            text = member.nickname,
            style = MinoAndroidTheme.typography.label1NormalMedium,
            color = MinoAndroidTheme.colors.labelAlternative,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        OwnerDelegateCheckbox(checked = selected, onCheckedChange = { onClick() })
    }
}

/** 위임 대상 단일 선택 체크박스 — 미체크(1.5dp 테두리) / 체크됨(검정 배경 + 흰 체크). */
@Composable
private fun OwnerDelegateCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val boxModifier = if (checked) {
        modifier
            .size(OwnerDelegateTokens.CheckboxSize)
            .surface(
                shape = OwnerDelegateTokens.CheckboxShape,
                containerColor = MinoAndroidTheme.colors.labelNormal,
            )
    } else {
        modifier
            .size(OwnerDelegateTokens.CheckboxSize)
            .surface(
                shape = OwnerDelegateTokens.CheckboxShape,
                containerColor = MinoAndroidTheme.colors.backgroundElevatedNormal,
                borderColor = MinoAndroidTheme.colors.lineNormalNeutral,
                borderWidth = OwnerDelegateTokens.CheckboxBorderWidth,
            )
    }

    Box(
        modifier = boxModifier.rippleSingleSelectable(
            selected = checked,
            role = Role.Checkbox,
            onClick = { onCheckedChange(!checked) },
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = MinoIcons.Check,
                contentDescription = null,
                tint = MinoAndroidTheme.colors.staticWhite,
                modifier = Modifier.size(OwnerDelegateTokens.CheckboxIconSize),
            )
        }
    }
}

/**
 * [OwnerDelegateMemberRow]·[OwnerDelegateCheckbox] 치수 토큰. 실측 근거는 Figma
 * node 2542:125613(리드가 직접 조회) — 브리프 "멤버 선택 리스트 UI" 절 참고.
 */
private object OwnerDelegateTokens {
    val RowVerticalPadding = 8.dp
    val RowContentSpacing = 12.dp
    val MemberListMaxHeight = 240.dp
    val CheckboxSize = 16.dp
    val CheckboxShape = RoundedCornerShape(4.dp)
    val CheckboxBorderWidth = 1.5.dp
    val CheckboxIconSize = 12.dp
}
