package team.mino.core.designsystem.component.topnavigation

import androidx.compose.runtime.Composable
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * Figma `Top Navigation` 컴포넌트셋의 속성 축을 옮긴 프리뷰.
 *
 * Figma의 축은 `Platform` 하나뿐이고 그중 iOS만 만들었으므로 Android·Web 값은 블록이 없다.
 * `onBackClick`·`actionLabel`은 Figma에 축이 없는 코드 축이다 — 디자인 시스템 계약이 정한 동작이라
 * 대조가 아니라 동작 확인용으로 둔다.
 *
 * 컴포넌트가 배경을 깔지 않아 프리뷰 페이지 배경이 그대로 비친다. 실제 화면에서는 셸이 배경을 깐다.
 */
@UiModePreviews
@Composable
private fun TopNavigationPreview() {
    PreviewPage {
        PreviewProperty(name = "Platform", values = "iOS") {
            MinoTopNavigation(title = "제목", onBackClick = {})
        }
        PreviewProperty(name = "onBackClick") {
            MinoTopNavigation(title = "뒤로가기 있음", onBackClick = {})
            MinoTopNavigation(title = "뒤로가기 없음")
        }
        PreviewProperty(name = "actionLabel") {
            MinoTopNavigation(
                title = "액션 있음",
                onBackClick = {},
                actionLabel = "건너뛰기",
                onActionClick = {},
            )
            MinoTopNavigation(title = "액션 없음", onBackClick = {})
        }
        PreviewProperty(name = "title") {
            MinoTopNavigation(
                title = "한 줄을 넘기면 말줄임으로 잘리는 아주 긴 제목입니다",
                onBackClick = {},
                actionLabel = "건너뛰기",
                onActionClick = {},
            )
        }
    }
}
