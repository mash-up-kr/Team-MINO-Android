package team.mino.core.designsystem.component.headerroom

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
import androidx.compose.ui.graphics.vector.ImageVector
import team.mino.core.designsystem.component.headerroom.token.HeaderRoomTokens
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

/**
 * 모임방 상세 화면 상단에 놓는 헤더(Figma `Header_Room`, 15852:88515).
 * 제목·선택적 메모·위치 개수·썸네일 모아보기 진입 버튼을 담고, 아래쪽에 1px 구분선을 그린다.
 *
 * Figma `show memo` boolean 변형은 [memo]의 null 여부로 대응한다 — `null`이면 memo 텍스트를
 * 그리지 않고(`show memo=off`), 값이 있으면 그린다(`show memo=on`).
 *
 * 우측 버튼은 Figma `Button/Icon/Normal`(16215:38424)의 `Badge=false` 인스턴스 자리다. Figma
 * 예시는 `Icon/Normal/Thumbnail`(썸네일 모아보기)이지만, 호출부마다 다른 동작(검색·설정 등)을 이
 * 자리에 얹을 수 있어 아이콘을 [thumbnailIcon]으로 받는다.
 *
 * 클릭 영역은 아이콘 크기([HeaderRoomTokens.ThumbnailIconSize], 24dp) 그대로다. Figma에는 그 위에
 * 40x40 `Interaction` 레이어가 얹혀 있지만 `position: absolute`라 레이아웃엔 관여하지 않는 순수 눌림
 * 표시용 오버레이다 — 실제 배치 기준 박스는 24dp 고정인 `Icon Button` 인스턴스다. 이걸 40dp 박스로
 * 구현하면 하단 row의 `Alignment.Bottom` 정렬 기준이 아이콘이 아니라 40dp 박스가 되어, 아이콘이
 * 위치 텍스트보다 위로 떠 보이는 정렬 오차가 생긴다.
 *
 * 이 화면 하나만 쓰는 자리라 별도 컴포넌트로 빼지 않고 이 파일 안에 직접 그린다 — `Badge=true`(푸시
 * 배지)·`Disable` 변형은 쓰지 않아 만들지 않았다. 다른 화면에서 같은 아이콘 버튼이 또 필요해지면 그때
 * `component/iconbutton/`으로 추출한다.
 *
 * @param resourceCountText 위치 아이콘 옆에 표시할 개수 문구. "999+개"처럼 상한 클램핑을 포함한
 *   최종 표시 문자열을 호출부가 만들어 넘긴다 — 이 컴포넌트는 서식을 모른다.
 * @param resourceIcon [resourceCountText] 옆에 그릴 아이콘. Figma 예시는 `Icon/Normal/Location`
 *   (위치 개수)이지만 호출부가 세는 대상이 다를 수 있어 아이콘을 파라미터로 받는다.
 * @param onThumbnailClick 우측 버튼의 클릭 콜백.
 * @param thumbnailIcon 우측 버튼에 그릴 아이콘. 색은 [MinoHeaderRoomDefaults.resourceColor]로
 *   고정한다(Figma 실측 `Semantic/Label/Alternative`) — 아이콘 모양만 호출부가 고른다.
 * @param thumbnailContentDescription 우측 버튼의 접근성 설명. 아이콘 전용 버튼이라 스크린 리더가 읽을
 *   문구가 없으면 그 버튼의 용도를 알 수 없다 — 가능하면 호출부가 의미 있는 문구를 넘긴다.
 */
@Composable
fun MinoHeaderRoom(
    title: String,
    resourceCountText: String,
    resourceIcon: ImageVector,
    onThumbnailClick: () -> Unit,
    thumbnailIcon: ImageVector,
    modifier: Modifier = Modifier,
    thumbnailContentDescription: String? = null,
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
                    style = HeaderRoomTokens.TitleFont.value,
                )
                if (memo != null) {
                    Text(
                        text = memo,
                        color = MinoHeaderRoomDefaults.memoColor,
                        style = HeaderRoomTokens.MemoFont.value,
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
                        imageVector = resourceIcon,
                        contentDescription = null,
                        modifier = Modifier.size(HeaderRoomTokens.LocationIconSize),
                        tint = MinoHeaderRoomDefaults.resourceColor,
                    )
                    Text(
                        text = resourceCountText,
                        color = MinoHeaderRoomDefaults.resourceColor,
                        style = HeaderRoomTokens.ResourceFont.value,
                    )
                }

                Icon(
                    imageVector = thumbnailIcon,
                    contentDescription = thumbnailContentDescription,
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
