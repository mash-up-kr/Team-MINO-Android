package team.mino.feature.onboarding.relay.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * 프로필·공동방 스텝을 다른 feature의 Activity에 위임하는 동안 온보딩이 머무르는 화면.
 *
 * **상태도 표시할 내용도 없다.** 위임 Activity가 이 화면 위에 얹히므로 사용자는 이것을 보지
 * 못하고, 결과를 기다리는 것도 그리는 쪽이 아니라 Activity 스코프의 플로우 ViewModel이다.
 * 그럼에도 화면이 필요한 이유는 살아 있는 Activity가 무언가를 그려야 하고 백스택을 정리할
 * 대상이 Route여야 하기 때문이다(`docs/specs/onboarding-flow/research.md` R-005).
 *
 * 배경을 직접 칠하는 것은 위임 Activity가 전환 애니메이션 중 반투명해지는 순간에도 이 자리가
 * 앱 기본 배경으로 보이게 하기 위한 것이다.
 */
@Composable
internal fun OnboardingRelayScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MinoAndroidTheme.colors.backgroundNormalNormal),
    )
}
