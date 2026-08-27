package team.mino.core.designsystem.component.topnavigation.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Top Navigation 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 * Figma `Top Navigation/Top Navigation`의 `Platform=iOS` 변형 중 `Bar` 프레임 기준이다.
 * 그 위에 겹쳐진 상태 표시줄 영역은 셸이 처리하므로 여기에는 들어오지 않는다.
 */
internal object TopNavigationTokens {
    val BarHeight = 44.dp
    val BarHorizontalPadding = 16.dp

    /** 뒤로가기가 놓이는 자리의 크기. 뒤로가기가 없어도 이 자리는 비워 둔 채 유지된다. */
    val LeadingSlotSize = 24.dp

    /**
     * 뒤로가기 터치·리플 영역의 크기. 아이콘 자리보다 커서 사방으로 넘치며,
     * 넘치는 만큼은 [BackIconPadding]으로 되돌려 아이콘 자체는 [LeadingSlotSize]로 그린다.
     */
    val BackInteractionSize = 40.dp
    val BackIconPadding = 8.dp

    /** 텍스트 액션이 놓이는 자리의 높이. 액션이 없어도 이 자리는 비워 둔 채 유지된다. */
    val TrailingSlotHeight = 24.dp

    /**
     * 텍스트 액션 터치·리플 영역의 크기. 글자보다 위아래·좌우로 넘치며,
     * 좌우로 넘치는 만큼은 [ActionInteractionOverhang]으로 되돌려 글자 자체는 자리 오른쪽 끝에 붙인다.
     */
    val ActionInteractionHeight = 28.dp
    val ActionInteractionOverhang = 6.dp
    val ActionInteractionShape = RoundedCornerShape(6.dp)

    val TitleHorizontalPadding = 4.dp

    val TitleFont = TypographyAccessKeyToken.Headline2Bold
    val TitleColor = ColorAccessKeyToken.LabelStrong
    val BackIconColor = ColorAccessKeyToken.LabelNormal
    val ActionLabelFont = TypographyAccessKeyToken.Label1NormalMedium
    val ActionLabelColor = ColorAccessKeyToken.LabelNormal
}
