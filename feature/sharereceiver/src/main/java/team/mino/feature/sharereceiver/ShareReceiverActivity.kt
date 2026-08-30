package team.mino.feature.sharereceiver

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import team.mino.core.common.ui.error.CollectUncaughtError
import team.mino.core.designsystem.component.snackbar.MinoSnackbar
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.CheckThick
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.domain.usecase.ExtractSharedUrlUseCase
import team.mino.feature.sharereceiver.picker.screen.ShareReceiverRoute
import team.mino.feature.sharereceiver.picker.vm.ShareReceiverViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import team.mino.core.common.ui.R as CommonUiR

/**
 * OS 공유 인텐트를 받는 진입 Activity. 이 feature가 외부 세계에 노출하는 유일한 표면이다
 * (`contracts/share-intent.md` §1).
 *
 * 셸도 `NavHost`도 두지 않고 [ShareReceiverRoute]를 직접 호스팅한다 — 내부 전환 대상이 없고, 딤 위에
 * 시트만 띄우는 화면에 불투명 배경을 여는 셸이 방해가 된다(`research.md` R-008).
 *
 * 이 Activity가 혼자 쥐는 것은 넷이다. 공유 텍스트에서 URL을 뽑아 시트에 넘기는 것, **시트가 떠 있는 동안
 * 도착한 새 공유를 받아 링크를 갈아 끼우는 것**([onNewIntent]), 시트를 걷어내고 완료 토스트를 3초 세는 것,
 * 그리고 종료다. 시트는 끝났다는 신호만 올릴 뿐 자기를 닫지 못한다.
 *
 * 셸이 없으므로 셸이 갖고 있던 미처리 예외 수집은 [UncaughtErrorHost]가 대신 쥔다
 * (`error_handling.md` §6). 화면 조회 로깅을 `AnalyticsTracker` 직접 호출로 대체한 것과 같은 방식이다.
 */
@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {
    @Inject
    lateinit var extractSharedUrl: ExtractSharedUrlUseCase

    /**
     * [onNewIntent]가 받아 시트에 넘길 새 링크. 넘기고 나면 시트가 [Composable] 쪽에서 지운다.
     *
     * 값을 여기 두는 것은 `onNewIntent`가 컴포지션 밖에서 도착하기 때문이다. `setIntent`가 태스크 레코드를
     * 갈아 끼워 프로세스 재생성을 덮고, 이 상태가 지금 떠 있는 컴포지션을 덮는다(research.md R-024).
     */
    private var sharedUrlReplacement by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* URL이 없으면 시트를 띄우지 않고 아무 요청도 보내지 않은 채 물러난다(§2.2·§3, EC-002).
         * 알림은 서버가 남기며, URL 없이 부를 저장 엔드포인트가 클라이언트에는 없다. */
        val sharedUrl = extractSharedUrl(intent?.getStringExtra(Intent.EXTRA_TEXT).orEmpty())
        if (sharedUrl == null) {
            finish()
            return
        }

        /* 시트의 ViewModel은 이 URL을 SavedStateHandle로 받는다. Activity의 인텐트 extra가 곧
         * defaultArgs이므로, 여기에 실어 두면 화면 회전·프로세스 재생성에도 값이 따라간다. */
        intent.putExtra(ShareReceiverViewModel.KEY_SHARED_URL, sharedUrl)

        /* 투명 테마는 시스템 바 색을 지정하지 않는다. 다른 Activity와 같이 코드에서 걷어낸다.
         * 이 화면은 열려 있는 내내 딤이 전면을 덮으므로, 라이트 모드에서 어두운 아이콘을 내는
         * 기본값(auto) 대신 밝은 아이콘을 고정한다. */
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            MinoAndroidAppTheme {
                /* 저장 신호를 받은 뒤로는 시트를 다시 그리지 않는다. 토스트는 시트가 걷힌 화면 하단에
                 * 뜨고(계약 §4), 그 사이의 회전이 시트를 되살리지 않도록 저장해 둔다. 걷히는 것은
                 * 시트 컨테이너뿐이라 딤은 [SavedToast]가 이어서 그린다(대조 노드 013-2). */
                var saved by rememberSaveable { mutableStateOf(false) }
                val replacement = sharedUrlReplacement

                /* 토스트 중에 새 공유가 도착하면 토스트를 걷고 시트를 되살린다(계약 §4·§2.3). 앞선 저장은
                 * 이미 워커로 넘어가 있어 되돌아가지 않는다. */
                LaunchedEffect(replacement) {
                    if (replacement != null) saved = false
                }

                UncaughtErrorHost(hasSnackbarSlot = !saved) {
                    if (saved) {
                        SavedToast(onDurationElapsed = ::finish)
                    } else {
                        ShareReceiverRoute(
                            replacedSharedUrl = replacement,
                            onReplacementConsumed = { sharedUrlReplacement = null },
                            onSavedAndFinish = { saved = true },
                            onFinish = ::finish,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }

    /**
     * 시트가 떠 있는 동안 도착한 공유를 받는다. `launchMode=singleTask`라 새 인스턴스가 아니라 여기로 온다
     * (`contracts/share-intent.md` §1·§2.3).
     *
     * **URL이 없는 새 인텐트는 무시하고 떠 있는 시트를 유지한다.** EC-002가 정한 것은 "시트를 띄우지
     * 않는다"이지 "떠 있는 시트를 걷는다"가 아니며, 사용자 조작 없이 시트가 사라지는 경로를 FR-012가
     * 두지 않는다.
     *
     * `setIntent`로 태스크 레코드의 인텐트를 갈아 끼우는 것은 프로세스가 통째로 재생성될 때를 덮기
     * 위해서다. 지금 떠 있는 시트는 [sharedUrlReplacement]가 덮고, ViewModel의 `SavedStateHandle`은
     * 시트가 스스로 덮는다(research.md R-024).
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val replacedUrl = extractSharedUrl(intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()) ?: return

        intent.putExtra(ShareReceiverViewModel.KEY_SHARED_URL, replacedUrl)
        setIntent(intent)
        sharedUrlReplacement = replacedUrl
    }
}

/**
 * 미처리 예외(버그)를 수집해 안내하는 자리. 셸이 없는 이 진입점에서 `MinoScaffold`가 소유하던
 * `CollectUncaughtError` + `SnackbarHost` 짝만 그대로 옮겨 왔다(`error_handling.md` §6 · R-008).
 *
 * 셸이 그렇듯 `setContent` 바로 아래, 화면 분기 **바깥**에 한 번만 둔다. 분기 안에 두면 시트가 걷히는
 * 순간 수집이 끊기고, `UncaughtErrorHandler`는 프로세스 전역 채널이라 그때 도착한 예외가 버퍼에 남아
 * 다음에 뜨는 다른 Activity의 셸에 엉뚱한 스낵바로 뜬다.
 *
 * @param hasSnackbarSlot 하단에 스낵바를 놓을 자리가 있는가. 완료 토스트가 그 자리를 독차지하는 동안에는
 *  `false`다 — 수집은 그대로 이어져 채널이 비지만(이 화면 밖으로 새지 않는다), 3초 뒤 사라질 화면에
 *  안내를 겹쳐 띄우지는 않는다.
 * @param content 이 자리 아래에 그릴 화면.
 */
@Composable
private fun UncaughtErrorHost(
    hasSnackbarSlot: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val message = stringResource(CommonUiR.string.error_unknown)

    CollectUncaughtError {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        content()

        if (hasSnackbarSlot) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
            )
        }
    }
}

/**
 * 저장 완료 토스트. [MinoSnackbar]가 지속 시간을 갖지 않으므로 호스트가 3초를 재고 종료까지 잇는다
 * (계약 §4 · UX-006 · FR-011).
 *
 * @param onDurationElapsed 3초가 지났다. 토스트가 사라지는 것과 화면이 물러나는 것이 같은 순간이다.
 */
@Composable
private fun SavedToast(
    onDurationElapsed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(TOAST_DURATION)
        onDurationElapsed()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MinoAndroidTheme.colors.materialDimmer),
        contentAlignment = Alignment.BottomCenter,
    ) {
        MinoSnackbar(
            message = stringResource(R.string.sharereceiver_saved_toast),
            leadingIcon = rememberVectorPainter(MinoIcons.CheckThick),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = ToastHorizontalMargin,
                    end = ToastHorizontalMargin,
                    bottom = ToastBottomMargin,
                ),
        )
    }
}

private val TOAST_DURATION = 3.seconds

private val ToastHorizontalMargin = 20.dp

/** 시스템 바가 아니라 화면 하단에서 잰다(UX-006). */
private val ToastBottomMargin = 40.dp
