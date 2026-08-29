package team.mino.core.designsystem.component.topnavigation

import androidx.compose.runtime.Composable
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Close
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * Figma `Top Navigation` 컴포넌트셋의 속성 축을 옮긴 프리뷰.
 *
 * 컴포넌트셋의 축은 `Platform`·`Bar`·`Scrolled` 셋이고, 만든 것은 `Platform=iOS`에 나머지 둘이 기본값인
 * 조합 하나뿐이다. 만들지 않은 축과 그 이유는 아래와 같고, 그래서 대응 블록도 없다.
 *
 * - `Platform`의 Android·Web: 화면 목업이 iOS 인스턴스를 쓰고 있어 사용자가 그쪽에 맞추기로 결정했다.
 * - `Bar`를 끈 값: 표시줄 자체를 지우고 상태 표시줄 자리만 남긴다. 그 상태가 필요한 화면은 이 컴포넌트를
 *   아예 그리지 않으면 되므로 축으로 열 이유가 없다.
 * - `Scrolled`를 켠 값: 콘텐츠가 밀려 올라갔을 때 배경과 구분선을 켠다. 이 컴포넌트를 쓰는 화면 중 그
 *   전환을 요구하는 것이 아직 없고, 열려면 컴포넌트가 스크롤 상태를 받아야 해 표면이 커진다.
 *   요구하는 화면이 나올 때 넓힌다.
 *
 * `onBackClick`·`actionLabel`·`actionIcon`은 Figma에 축이 없는 코드 축이다 — 디자인 시스템 계약이 정한
 * 동작이라 대조가 아니라 동작 확인용으로 둔다. 우측 액션은 Figma에서 인스턴스가 무는 자식 컴포넌트
 * (텍스트 버튼 / 아이콘 버튼)로 갈리는데, 코드는 그것을 오버로드로 갈랐다.
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
        PreviewProperty(name = "actionIcon") {
            MinoTopNavigation(
                title = "아이콘 액션",
                actionIcon = MinoIcons.Close,
                actionIconContentDescription = "닫기",
                onActionClick = {},
                onBackClick = {},
            )
            MinoTopNavigation(
                title = "",
                actionIcon = MinoIcons.Close,
                actionIconContentDescription = "닫기",
                onActionClick = {},
            )
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
