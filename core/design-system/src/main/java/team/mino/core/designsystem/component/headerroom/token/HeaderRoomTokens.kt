package team.mino.core.designsystem.component.headerroom.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Header Room 컴포넌트 슬롯 → 디자인 토큰·크기 매핑. Figma `Header_Room`(15852:88515) 기준.
 */
internal object HeaderRoomTokens {
    val ContentPadding = PaddingValues(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
    val DividerThickness = 1.dp

    /** title·memo 사이 간격. */
    val TitleMemoSpacing = 4.dp

    /** title·memo 블록과 하단 row 사이 간격. */
    val ContentRowSpacing = 10.dp

    /** 위치 아이콘·개수 텍스트 사이 간격. */
    val LocationIconTextSpacing = 2.dp

    val LocationIconSize = 18.dp

    val ThumbnailIconSize = 24.dp

    val TitleColor = ColorAccessKeyToken.LabelStrong
    val TitleFont = TypographyAccessKeyToken.Title3Bold

    val MemoColor = ColorAccessKeyToken.LabelNeutral
    val MemoFont = TypographyAccessKeyToken.Label1NormalRegular

    /** 위치 개수 텍스트·위치 아이콘·썸네일 아이콘이 공유하는 색(Figma 실측 `Semantic/Label/Alternative`). */
    val ResourceColor = ColorAccessKeyToken.LabelAlternative
    val ResourceFont = TypographyAccessKeyToken.Label1NormalRegular

    val DividerColor = ColorAccessKeyToken.LineSolidAlternative
}
