package team.mino.core.designsystem.component.dialog.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Dialog 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 *
 * 디자인 시스템 라이브러리 파일(fileKey 5P3HE7q8MGc6yAr4rTOSZn)에서 Dialog/AlertDialog 정의
 * 노드를 찾지 못했다 — 최상위 페이지가 컴포넌트 라이브러리가 아니라 플로우 문서였고, 전체
 * 텍스트에서 관련 키워드로도 정의 노드가 나오지 않았다. 노드를 확보하기 전까지 Material3
 * `AlertDialog`의 기본 구조를 그대로 쓰고, 컨테이너 셰이프·색은 이 모듈의 다른 모달형 표면
 * 컴포넌트([team.mino.core.designsystem.component.menu.token.MenuTokens])가 쓰는 슬롯을
 * 준용한 실측값이다. Figma 노드가 확보되면 이 파일부터 다시 맞춘다.
 */
internal object DialogTokens {
    val ContainerShape: Shape = RoundedCornerShape(16.dp)
    val ContainerColor = ColorAccessKeyToken.BackgroundElevatedNormal

    val TitleColor = ColorAccessKeyToken.LabelNormal
    val TitleFont = TypographyAccessKeyToken.Heading2Bold

    val MessageColor = ColorAccessKeyToken.LabelAlternative
    val MessageFont = TypographyAccessKeyToken.Body2NormalRegular
}
