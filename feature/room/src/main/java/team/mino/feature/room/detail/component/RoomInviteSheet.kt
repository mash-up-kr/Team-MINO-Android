package team.mino.feature.room.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.component.avatar.MinoAvatar
import team.mino.core.designsystem.component.avatar.MinoAvatarSize
import team.mino.core.designsystem.component.avatar.MinoAvatarVariant
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.domain.model.RoomMember
import team.mino.feature.room.detail.model.image

/**
 * [RoomInviteSheet] 치수 토큰. 시트 chrome(핸들·모서리)은 `RoomSelectSheet`(같은 디렉터리)와 동일 패턴을
 * 재사용한다 — 같은 시트 계열이라 값도 같다.
 *
 * 전체 높이 424dp·참여자 목록 스크롤 288dp는 spec.md FR-011 고정값(Figma node 2542-125613, 리드가
 * 직접 조회) 그대로다. 참여자 행 실측값(아바타 48×48dp, 4행에 288px → 행 높이 72dp)도 같은 노드 조회
 * 결과다.
 */
private object RoomInviteSheetTokens {
    val SheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val HandleSize = DpSize(38.dp, 4.dp)
    val HandleShape = RoundedCornerShape(4.dp)
    val HandleTopPadding = 8.dp
    val HandleBottomPadding = 8.dp

    val SheetHeight = 424.dp
    val MemberListHeight = 288.dp
    val MemberRowHeight = 72.dp
    val MemberRowHorizontalPadding = 20.dp
    val MemberRowSpacing = 12.dp

    val InviteLinkHorizontalPadding = 20.dp
}

/**
 * [친구 +] 초대 바텀시트([SYS-006] Flow B, FR-011) — 초대 링크 표시 + 참여자 전체 목록.
 *
 * 전체 높이는 [RoomInviteSheetTokens.SheetHeight](424dp), 참여자 목록 스크롤 영역은
 * [RoomInviteSheetTokens.MemberListHeight](288dp)로 고정한다. 두 영역 사이 초대 링크 부분은
 * `Modifier.weight(1f)`로 남는 높이를 채워 전체 424dp를 맞춘다 — 링크 영역 자체를 별도로 고정할
 * 근거가 없기 때문이다.
 *
 * 이 시트는 참여자를 순수 조회만 한다 — 체크박스가 붙은 변형(방장 위임 대상 선택)은 같은
 * [RoomMember] 데이터를 다른 화면([RoomOwnerLeaveDialog])이 다르게 그리는 것이라 여기 없다.
 *
 * [TBD] 초대 링크 클립보드 복사·OS 공유 시트 연동은 [SYS-006] 전용 spec이 아직 없어 범위 밖이다 —
 * 코드/링크 텍스트를 보여주는 것까지만 구현한다(research.md D11).
 *
 * @param inviteCode 서버가 발급한 초대 코드. `null`이면 아직 발급 중인 상태로 본다.
 * @param roomMembers 방 참여자 전체 목록(`RoomRepository.getMembers`, [FR-011]).
 */
@Composable
internal fun RoomInviteSheet(
    inviteCode: String?,
    roomMembers: ImmutableList<RoomMember>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(RoomInviteSheetTokens.SheetHeight)
            .surface(
                shape = RoomInviteSheetTokens.SheetShape,
                containerColor = MinoAndroidTheme.colors.backgroundElevatedNormal,
            ),
    ) {
        RoomInviteDragHandle()

        HorizontalDivider(color = MinoAndroidTheme.colors.lineNormalNeutral)

        RoomInviteLinkSection(
            inviteCode = inviteCode,
            modifier = Modifier.weight(1f),
        )

        HorizontalDivider(color = MinoAndroidTheme.colors.lineNormalNeutral)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(RoomInviteSheetTokens.MemberListHeight),
        ) {
            items(items = roomMembers, key = { it.userId }) { member ->
                RoomInviteMemberRow(member = member)
            }
        }
    }
    // onDismiss는 RoomSelectSheet와 같은 이유로 이 컴포저블이 스스로 소비하지 않는다 — 호스팅하는
    // 바텀시트 컨테이너가 바깥 영역 클릭·백 제스처에 연결한다.
}

@Composable
private fun RoomInviteDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = RoomInviteSheetTokens.HandleTopPadding,
                bottom = RoomInviteSheetTokens.HandleBottomPadding,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(RoomInviteSheetTokens.HandleSize.width, RoomInviteSheetTokens.HandleSize.height)
                .background(color = MinoAndroidTheme.colors.fillNormal, shape = RoomInviteSheetTokens.HandleShape),
        )
    }
}

/**
 * 초대 링크 표시 영역. `inviteCode`가 `null`이면 아직 발급 요청이 끝나지 않은 상태로 보고 로딩 문구를
 * 최소 표시한다 — 디자인 시스템에 이 상태 전용 인디케이터가 없어 텍스트로 대체했다.
 */
@Composable
private fun RoomInviteLinkSection(
    inviteCode: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RoomInviteSheetTokens.InviteLinkHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = inviteCode?.let { buildInviteLinkText(it) } ?: "발급 중...",
            style = MinoAndroidTheme.typography.body1NormalBold,
            color = MinoAndroidTheme.colors.labelNormal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 초대 코드를 초대 링크 문구로 조립한다(research.md D16, `gguk.org/r/{code}`). */
private fun buildInviteLinkText(inviteCode: String): String = "gguk.org/r/$inviteCode"

/** 참여자 목록 한 행 — 48×48dp 아바타 + 닉네임. 체크박스 없는 순수 조회 행이다. */
@Composable
private fun RoomInviteMemberRow(
    member: RoomMember,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(RoomInviteSheetTokens.MemberRowHeight)
            .padding(horizontal = RoomInviteSheetTokens.MemberRowHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(RoomInviteSheetTokens.MemberRowSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MinoAvatar(
            variant = MinoAvatarVariant.Person,
            size = MinoAvatarSize.Large,
            profileAvatar = member.avatar.image,
        )
        Text(
            text = member.nickname,
            style = MinoAndroidTheme.typography.label1NormalMedium,
            color = MinoAndroidTheme.colors.labelAlternative,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
