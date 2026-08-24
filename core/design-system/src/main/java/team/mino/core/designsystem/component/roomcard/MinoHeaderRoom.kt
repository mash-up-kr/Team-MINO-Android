package team.mino.core.designsystem.component.roomcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import team.mino.core.designsystem.component.roomcard.token.HeaderRoomTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.LocationFill
import team.mino.core.designsystem.foundation.icons.icons.Thumbnail
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

/**
 * 모임방 상세 화면 상단에 놓는 헤더(Figma `Header_Room`, 15852:88515).
 * 제목·선택적 메모·위치 개수·썸네일 모아보기 진입 버튼을 담고, 아래쪽에 1px 구분선을 그린다.
 *
 * Figma `show memo` boolean 변형은 [memo]의 null 여부로 대응한다 — `null`이면 memo 텍스트를
 * 그리지 않고(`show memo=off`), 값이 있으면 그린다(`show memo=on`).
 *
 * 우측 버튼은 Figma `Button/Icon/Normal`(16215:38424)의 `Badge=false` 인스턴스이고, 아이콘은
 * `Icon/Normal/Thumbnail`(썸네일 모아보기)로 고정이다. 좌측 개수 아이콘도 `Icon/Normal/Location`
 * 고정이라 둘 다 파라미터로 열지 않는다 — `Header_Room` 컴포넌트 세트의 변형 축은 `show memo`
 * 하나뿐이라 아이콘을 바꿔 끼우는 용법 자체가 시안에 없다.
 *
 * 아이콘이 고정이면 그 의미도 이 컴포넌트가 안다. 그래서 접근성 설명을 파라미터로 받지 않고
 * `MinoSnackbar`("닫기")·`MinoTextField`("입력 삭제")과 같은 방식으로 안에서 직접 붙인다 —
 * 아이콘 전용 버튼의 설명을 nullable 파라미터로 열어 두면 호출부가 안 넘겼을 때 스크린 리더가
 * 아무것도 읽지 않는 버튼이 조용히 생긴다. 좌측 개수 아이콘은 바로 옆 텍스트("999+개")가 같은
 * 정보를 읽어 주므로 장식으로 보고 `contentDescription = null`이다.
 *
 * 클릭 영역은 아이콘 크기([HeaderRoomTokens.ThumbnailIconSize], 24dp) 그대로다. Figma에는 그 위에
 * 40x40 `Interaction` 레이어가 얹혀 있지만 `position: absolute`라 레이아웃엔 관여하지 않는 순수 눌림
 * 표시용 오버레이다 — 실제 배치 기준 박스는 24dp 고정인 `Icon Button` 인스턴스다. 이걸 40dp 박스로
 * 구현하면 하단 row의 `Alignment.Bottom` 정렬 기준이 아이콘이 아니라 40dp 박스가 되어, 아이콘이
 * 위치 텍스트보다 위로 떠 보이는 정렬 오차가 생긴다.
 *
 * 이 화면 하나만 쓰는 자리라 별도 컴포넌트로 빼지 않고 이 파일 안에 직접 그린다 — `Badge=true`(푸시
 * 배지)·`Disable` 변형은 쓰지 않아 만들지 않았다. 다른 화면에서 같은 아이콘 버튼이 또 필요해지면 그때
 * `core:design-system`의 `component/iconbutton/`으로 추출한다.
 *
 * @param resourceCountText 위치 아이콘 옆에 표시할 개수 문구. "999+개"처럼 상한 클램핑을 포함한
 *   최종 표시 문자열을 호출부가 만들어 넘긴다 — 이 컴포넌트는 서식을 모른다.
 * @param onThumbnailClick 우측 썸네일 모아보기 버튼의 클릭 콜백.
 * @param memo 제목 아래 메모. `null`이면 Figma `show memo=off`.
 */
@Composable
fun MinoHeaderRoom(
    title: String,
    resourceCountText: String,
    onThumbnailClick: () -> Unit,
    modifier: Modifier = Modifier,
    memo: String? = null,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HeaderRoomTokens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(HeaderRoomTokens.ContentRowSpacing),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(HeaderRoomTokens.TitleMemoSpacing)) {
                Text(
                    text = title,
                    color = MinoHeaderRoomDefaults.titleColor,
                    style = MinoHeaderRoomDefaults.titleFont,
                )
                if (memo != null) {
                    Text(
                        text = memo,
                        color = MinoHeaderRoomDefaults.memoColor,
                        style = MinoHeaderRoomDefaults.memoFont,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(HeaderRoomTokens.LocationIconTextSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = MinoIcons.LocationFill,
                        contentDescription = null,
                        modifier = Modifier.size(HeaderRoomTokens.LocationIconSize),
                        tint = MinoHeaderRoomDefaults.resourceColor,
                    )
                    Text(
                        text = resourceCountText,
                        color = MinoHeaderRoomDefaults.resourceColor,
                        style = MinoHeaderRoomDefaults.resourceFont,
                    )
                }

                Icon(
                    imageVector = MinoIcons.Thumbnail,
                    contentDescription = "썸네일 모아보기",
                    modifier = Modifier
                        .size(HeaderRoomTokens.ThumbnailIconSize)
                        .clip(CircleShape)
                        .rippleSingleClickable(onClick = onThumbnailClick),
                    tint = MinoHeaderRoomDefaults.resourceColor,
                )
            }
        }

        HorizontalDivider(
            thickness = HeaderRoomTokens.DividerThickness,
            color = MinoHeaderRoomDefaults.dividerColor,
        )
    }
}
