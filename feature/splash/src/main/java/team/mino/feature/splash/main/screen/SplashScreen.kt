package team.mino.feature.splash.main.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
 * 스플래시의 브랜드 레이어. 워드마크·태그라인과 마스코트를 그린다.
 *
 * 브랜드 레이어 자체는 상태를 받지 않는다 — 세션 확보의 성패·지연과 무관하게 **항상** 같은
 * 모습으로 노출된다(FR-001·UX-004). [isSpinnerVisible]과 [toastMessage]는 그 위에 얹히는
 * 별개의 레이어라, 켜지고 꺼져도 아래 레이어의 배치를 건드리지 않는다.
 *
 * [toastMessage]는 지금 띄울 문구이고, 얼마나 띄울지는 호출자가 정한다. 여기서 정하는 것은
 * 자리뿐이다.
 *
 * **높이는 브랜드 문구 위아래 두 여백이 나눠 갖는다.** 문구와 마스코트는 자기 크기를 먼저
 * 가져가고, 남은 높이만 [BrandTopGapWeight]와 [BrandMascotGapWeight]의 비로 갈린다. 두 몫은 각
 * 여백의 디자인 높이라, 디자인 높이에서는 두 여백이 그 값 그대로 나오고, 낮은 기기에서는 둘이 함께 줄어 문구와
 * 마스코트가 겹치지 않는다. 한쪽 여백만 고정하면 남는 쪽이 음수가 되어 겹치는 지점이 생긴다.
 *
 * **마스코트는 디자인에서 화면 좌우·아래로 넘치는 크기다.** 넘치는 부분까지 에셋에 담지 않고
 * 프레임에 잘린 모습 그대로 내보내, 화면 폭을 채우는 것으로 같은 그림을 얻는다. 잘라 둔 덕에
 * 폭이 다른 기기에서도 좌우 여백 없이 내비바 바로 위까지 닿는다.
 *
 * **위아래 인셋을 다르게 다룬다.** 디자인에서 상태바는 화면 위에 겹쳐 그려져 있고 위 여백은
 * 상태바 아래가 아니라 화면 최상단부터 잰다. 그래서 상단 인셋은 소비하지 않는다 — 그 여백은
 * 어떤 기기에서도 상태바보다 훨씬 두꺼워 문구가 가릴 일이 없다. 반면 마스코트는 내비게이션 바
 * 위에서 잘려 있어 하단 인셋은 소비한다. 내비바 자리는 배경색으로 남는다.
 */
@Composable
internal fun SplashScreen(
    isSpinnerVisible: Boolean,
    modifier: Modifier = Modifier,
    toastMessage: String? = null,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
        ) {
            Spacer(modifier = Modifier.weight(BrandTopGapWeight))

            Image(
                painter = painterResource(R.drawable.splash_wordmark),
                contentDescription = stringResource(R.string.splash_wordmark_description),
                modifier = Modifier.size(width = WordmarkWidth, height = WordmarkHeight),
            )

            Text(
                text = stringResource(R.string.splash_tagline),
                style = MinoAndroidTheme.typography.body1ReadingRegular,
                color = MinoAndroidTheme.colors.staticBlack,
                modifier = Modifier.padding(top = WordmarkTaglineSpacing),
            )

            Spacer(modifier = Modifier.weight(BrandMascotGapWeight))

            Image(
                painter = painterResource(R.drawable.splash_mascot),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
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

private val BrandTopGapWeight = 176f
private val BrandMascotGapWeight = 138.34f

private val WordmarkWidth = 189.91.dp
private val WordmarkHeight = 81.33.dp
private val WordmarkTaglineSpacing = 14.dp
private val SpinnerSize = 28.dp
private val ToastHorizontalMargin = 20.dp
private val ToastBottomMargin = 40.dp
