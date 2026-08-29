package team.mino.feature.home.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.clickable.singleClickable
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.home.R

/**
 * 홈 상단에 늘 남는 방 뱃지·인사 문구(spec FR-021).
 *
 * **[RoomCharacter]를 품지 않는다.** 시안에서 캐릭터는 이 둘과 형제가 아니라 화면 프레임의 마지막
 * 자식이라 정렬 칩 위에 얹힌다. 여기 넣으면 칩 행이 캐릭터 위로 올라와 겹치는 띠의 탭을 가로챈다.
 *
 * **세로 원점을 그 캐릭터와 공유한다.** 캐릭터가 뱃지보다 위에서 시작하므로 조립부는 둘을 같은 위쪽
 * 기준에 놓고, 이 컴포저블이 그 차이만큼 스스로 내려온다.
 *
 * 상태를 읽지 않는다 — 방 이름은 [roomName]으로 받고, 뱃지와 캐릭터 **양쪽 모두** 같은 곳으로 잇는다
 * (spec FR-017, TS-025·TS-026). 계약의 의도도
 * [team.mino.feature.home.main.vm.HomeIntent.OpenRoomSheet] 하나뿐이라 호출부가 둘을 갈라 받을 이유가 없다.
 *
 * @param roomName 지금 보고 있는 방의 이름. 뱃지에 그대로 실린다.
 * @param onRoomChangeClick 방 뱃지를 눌렀을 때. 호출부가
 *  [team.mino.feature.home.main.vm.HomeIntent.OpenRoomSheet]로 잇는다.
 */
@Composable
internal fun HomeTopShell(
    roomName: String,
    onRoomChangeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(start = ShellStartPadding, top = ShellTopOffset),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing),
    ) {
        RoomBadge(roomName = roomName, onClick = onRoomChangeClick)
        Text(
            // 줄바꿈이 문구 안에 박혀 있어 너비를 묶지 않는다.
            text = stringResource(R.string.home_greeting),
            color = MinoAndroidTheme.colors.primaryNormal,
            style = MinoAndroidTheme.typography.heading1Bold,
        )
    }
}

/**
 * 방 캐릭터. 자리는 호출자가 정한다 — 화면 오른쪽 끝에 붙어 있어 콘텐츠 좌우 여백을 받지 않는다.
 *
 * @param onClick 캐릭터를 눌렀을 때. 홈에서는 [HomeTopShell]의 뱃지와 같은 곳으로 간다.
 */
@Composable
internal fun RoomCharacter(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(R.drawable.home_room_character),
        contentDescription = stringResource(R.string.home_room_change),
        modifier = modifier
            .size(CharacterWidth, CharacterHeight)
            // 투명 영역이 넓은 이미지라 사각형 리플이 캐릭터 밖까지 번진다. 눌림 표시를 두지 않는다.
            .singleClickable(role = Role.Button, onClick = onClick),
    )
}

/**
 * 방 이름 뱃지.
 *
 * `MinoContentBadge`를 쓰지 않는다. 크기 계열은 `Medium`과 같지만 색이 다르다 — 디자인 시스템의
 * `Neutral`은 `Fill/Normal`·`Label/Alternative`를 쓰는데 이 뱃지는 다른 색 하나에서 글자와 배경을
 * 파생한다. 값이 다른 토큰을 끌어다 쓰는 것은 `figma-design-fidelity.md` §2의 위반이라 여기서 조립한다.
 */
@Composable
internal fun RoomBadge(
    roomName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = roomName,
        modifier = modifier
            .clip(RoomBadgeShape)
            .background(RoomBadgeColor.copy(alpha = ROOM_BADGE_CONTAINER_OPACITY))
            .rippleSingleClickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.home_room_change),
                onClick = onClick,
            ).padding(horizontal = RoomBadgeHorizontalPadding, vertical = RoomBadgeVerticalPadding),
        color = RoomBadgeColor,
        style = MinoAndroidTheme.typography.label2Medium,
    )
}

/** 캐릭터가 뱃지 열보다 위에서 시작하는 만큼 글 쪽을 내린다. */
private val ShellTopOffset = 22.dp

private val ShellStartPadding = 20.dp

private val CharacterWidth = 126.dp

private val CharacterHeight = 164.dp

// Figma md 변수 대응 — 토큰 미존재
private val ShellSpacing = 12.dp

private val RoomBadgeShape: Shape = RoundedCornerShape(8.dp)

private val RoomBadgeHorizontalPadding = 8.dp

private val RoomBadgeVerticalPadding = 5.dp

// Figma Atomic/Neutral/60 변수 대응 — :core:design-system 밖에서 닿는 토큰 미존재
private val RoomBadgeColor = Color(0xFF8A8A8A)

// Figma Opacity/8 변수 대응 — 토큰 미존재
private const val ROOM_BADGE_CONTAINER_OPACITY = 0.08f

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun HomeTopShellPreview() {
    MinoAndroidAppTheme {
        Box(modifier = Modifier.background(MinoAndroidTheme.colors.backgroundNormalAlternative)) {
            HomeTopShell(roomName = "내 장소", onRoomChangeClick = {})
        }
    }
}
