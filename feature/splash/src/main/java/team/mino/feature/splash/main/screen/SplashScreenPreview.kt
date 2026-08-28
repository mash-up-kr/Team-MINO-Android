package team.mino.feature.splash.main.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.splash.R

/*
 * SplashScreen의 렌더 프리뷰.
 *
 * 브랜드 레이어는 상태를 받지 않아 갈래가 없고, 그 위에 얹히는 스피너·토스트가 각각 한 장씩이다.
 * 셸이 깔아 주는 배경은 프리뷰에 없으므로 컨테이너가 같은 토큰으로 대신 깐다.
 */

/** 진입부터 전환까지 내내 같은 모습인 브랜드 레이어. */
@UiModePreviews
@Composable
private fun SplashScreenPreview() {
    SplashScreenPreviewContainer()
}

/** 최소 노출이 끝나도 진입 지점이 확보되지 않아 스피너가 올라온 상태. */
@UiModePreviews
@Composable
private fun SplashScreenSpinnerPreview() {
    SplashScreenPreviewContainer(isSpinnerVisible = true)
}

/** 오류를 알리는 토스트가 떠 있는 상태. 브랜드 레이어는 그대로 남는다. */
@UiModePreviews
@Composable
private fun SplashScreenToastPreview() {
    SplashScreenPreviewContainer(toastMessage = stringResource(R.string.splash_error_network))
}

@Composable
private fun SplashScreenPreviewContainer(
    modifier: Modifier = Modifier,
    isSpinnerVisible: Boolean = false,
    toastMessage: String? = null,
) {
    MinoAndroidAppTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MinoAndroidTheme.colors.backgroundNormalNormal),
        ) {
            SplashScreen(isSpinnerVisible = isSpinnerVisible, toastMessage = toastMessage)
        }
    }
}
