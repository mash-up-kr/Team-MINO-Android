package team.mino.feature.splash.main.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.snackbar.MinoSnackbar
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.CircleExclamation
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.feature.splash.R

/**
 * 스플래시의 브랜드 레이어. 캐릭터·구름·워드마크·태그라인을 그린다.
 *
 * 브랜드 레이어 자체는 상태를 받지 않는다 — 세션 확보의 성패·지연과 무관하게 **항상** 같은
 * 모습으로 노출된다(FR-001·UX-004). [isSpinnerVisible]과 [toastMessage]는 그 위에 얹히는
 * 별개의 레이어라, 켜지고 꺼져도 아래 레이어의 배치를 건드리지 않는다.
 *
 * [toastMessage]는 지금 띄울 문구이고, 얼마나 띄울지는 호출자가 정한다. 여기서 정하는 것은
 * 자리뿐이다.
 *
 * 캐릭터는 위에서, 구름과 그 위의 브랜드 문구는 아래에서 각각 고정한다. 화면 높이가 디자인과
 * 다를 때 늘고 주는 곳은 캐릭터와 구름 사이 여백 하나뿐이라, 구름 위 워드마크의 자리는 어떤
 * 높이에서도 유지된다.
 *
 * 이 화면은 셸이 인셋을 걸지 않은 전체 영역을 받는다(SplashShell). 상단 인셋을 피해야 하는 것은
 * 캐릭터뿐이라 그 하나만 직접 소비하고, 구름·브랜드 문구·스피너·토스트는 시스템 바가 아니라
 * 화면 자체를 기준으로 놓인다.
 */
@Composable
internal fun SplashScreen(
    isSpinnerVisible: Boolean,
    modifier: Modifier = Modifier,
    toastMessage: String? = null,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.splash_characters),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .padding(top = CharactersTopOffset)
                .size(width = CharactersWidth, height = CharactersHeight),
        )

        // 구름은 캐릭터보다 위에 쌓인다.
        Image(
            painter = painterResource(R.drawable.splash_cloud),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WordmarkTaglineSpacing),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = BrandBottomOffset),
        ) {
            Image(
                painter = painterResource(R.drawable.splash_wordmark),
                contentDescription = stringResource(R.string.splash_wordmark_description),
                modifier = Modifier.size(width = WordmarkWidth, height = WordmarkHeight),
            )

            Text(
                text = stringResource(R.string.splash_tagline),
                style = MinoAndroidTheme.typography.body1ReadingRegular,
                color = MinoAndroidTheme.colors.staticBlack,
            )
        }

        if (isSpinnerVisible) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(SpinnerSize),
            )
        }

        if (toastMessage != null) {
            // 재시도 버튼도 안내 문구도 두지 않으므로 액션과 닫기 버튼이 없다(UX-001).
            MinoSnackbar(
                message = toastMessage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = ToastHorizontalMargin,
                        end = ToastHorizontalMargin,
                        bottom = ToastBottomMargin,
                    ).fillMaxWidth(),
                leadingIcon = rememberVectorPainter(MinoIcons.CircleExclamation),
            )
        }
    }
}

private val CharactersTopOffset = 80.dp
private val CharactersWidth = 323.dp
private val CharactersHeight = 353.dp
private val WordmarkWidth = 189.91.dp
private val WordmarkHeight = 81.33.dp
private val WordmarkTaglineSpacing = 14.dp
private val BrandBottomOffset = 91.67.dp
private val SpinnerSize = 28.dp
private val ToastHorizontalMargin = 20.dp
private val ToastBottomMargin = 40.dp
