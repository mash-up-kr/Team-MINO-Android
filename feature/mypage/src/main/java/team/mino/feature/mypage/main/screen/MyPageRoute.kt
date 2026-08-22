package team.mino.feature.mypage.main.screen

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import team.mino.core.common.android.extension.openAppSettings
import team.mino.core.common.android.extension.openPlayStoreListing
import team.mino.core.common.android.extension.openUrl
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.core.common.ui.error.CollectDomainError
import team.mino.core.common.ui.scaffold.LocalSnackbarHostState
import team.mino.feature.mypage.R
import team.mino.feature.mypage.main.vm.MyPageIntent
import team.mino.feature.mypage.main.vm.MyPageSideEffect
import team.mino.feature.mypage.main.vm.MyPageViewModel

// CollectDomainError는 DomainErrorEmitter를 위임한 ViewModel을 그대로 받는 것이 의도된 사용법이다
// (core/common/ui/README.md §"에러 소비") — ComposeViewModelForwarding이 잡는 일반적 안티패턴과 다르다.
@Suppress("ComposeViewModelForwarding")
@Composable
internal fun MyPageRoute(
    onNavigateToProfileSetup: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as Activity

    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val errorMessage = stringResource(R.string.mypage_error_domain)
    CollectDomainError(emitter = viewModel) {
        scope.launch { snackbarHostState.showSnackbar(errorMessage) }
    }

    // init { refresh() }는 최초 진입만 커버한다. 프로필 저장 후 복귀처럼 재진입 시에도
    // 최신 프로필을 반영하려면 화면이 다시 RESUMED될 때마다 재조회를 트리거해야 한다.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.processIntent(MyPageIntent.OnScreenResumed)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.processIntent(MyPageIntent.OnNotificationPermissionResult(granted))
        }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val granted = results.values.any { it }
            viewModel.processIntent(MyPageIntent.OnLocationPermissionResult(granted))
        }

    CollectSideEffect(sideEffect = viewModel.sideEffect) { effect ->
        when (effect) {
            MyPageSideEffect.NavigateToProfileSetup -> onNavigateToProfileSetup()

            MyPageSideEffect.RequestNotificationPermission ->
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

            MyPageSideEffect.RequestLocationPermission ->
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )

            MyPageSideEffect.OpenAppSettings -> context.openAppSettings()

            is MyPageSideEffect.OpenUrl -> context.openUrl(effect.url)

            MyPageSideEffect.OpenPlayStoreListing -> context.openPlayStoreListing()
        }
    }

    // 알림·위치 스위치 클릭 시 rationale은 Activity 전용 API로만 계산할 수 있어(research.md D3),
    // Screen이 보낸 canShowSystemDialog 값을 여기서 항상 최신 값으로 다시 계산해 덮어쓴다.
    val onIntent: (MyPageIntent) -> Unit = { intent ->
        val resolvedIntent =
            when (intent) {
                is MyPageIntent.OnNotificationSwitchClick ->
                    MyPageIntent.OnNotificationSwitchClick(
                        canShowSystemDialog =
                            activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS),
                    )

                is MyPageIntent.OnLocationSwitchClick ->
                    MyPageIntent.OnLocationSwitchClick(
                        canShowSystemDialog =
                            activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION),
                    )

                else -> intent
            }
        viewModel.processIntent(resolvedIntent)
    }

    MyPageScreen(
        state = state,
        onIntent = onIntent,
        modifier = modifier,
    )
}
