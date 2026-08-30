package team.mino.feature.onboarding.tutorial.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.onboarding.tutorial.model.TutorialStep

/*
 * TutorialScreen의 페이지별 렌더 프리뷰.
 *
 * 화면이 캐러셀 상태를 들지 않으므로 여기서 만들어 넘긴다. 세 장은 페이지 인덱스만 다르며,
 * 첫 장과 마지막 장이 상단 [건너뛰기]와 하단 액션 영역이 갈리는 두 모습이다.
 */

/** 첫 장. 상단에 건너뛸 수단이 있고 하단은 비어 있다. */
@UiModePreviews
@Composable
private fun TutorialScreenFirstStepPreview() {
    TutorialScreenPreviewContainer(initialPage = 0)
}

/** 가운데 장. 첫 장과 달라지는 것은 배지 숫자·문구·예시 이미지와 짙은 dot의 자리뿐이다. */
@UiModePreviews
@Composable
private fun TutorialScreenMiddleStepPreview() {
    TutorialScreenPreviewContainer(initialPage = MIDDLE_PAGE)
}

/** 마지막 장. 상단 액션이 사라지고 하단에 시작 버튼이 놓인다. */
@UiModePreviews
@Composable
private fun TutorialScreenLastStepPreview() {
    TutorialScreenPreviewContainer(initialPage = TutorialStep.entries.lastIndex)
}

@Composable
private fun TutorialScreenPreviewContainer(
    initialPage: Int,
    modifier: Modifier = Modifier,
) {
    MinoAndroidAppTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MinoAndroidTheme.colors.backgroundNormalNormal),
        ) {
            TutorialScreen(
                pagerState = rememberPagerState(initialPage = initialPage) { TutorialStep.entries.size },
                onDotClick = {},
                onFinish = {},
            )
        }
    }
}

private const val MIDDLE_PAGE = 2
