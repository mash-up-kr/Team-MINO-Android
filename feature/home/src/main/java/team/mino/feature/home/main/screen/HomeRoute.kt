package team.mino.feature.home.main.screen

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.core.common.ui.error.CollectDomainError
import team.mino.core.common.ui.scaffold.LocalSnackbarHostState
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.home.R
import team.mino.feature.home.main.vm.HomeIntent
import team.mino.feature.home.main.vm.HomeSideEffect
import team.mino.feature.home.main.vm.HomeViewModel

/**
 * [HomeScreen]의 연결부. ViewModel을 얻어 상태를 구독하고 의도를 넘긴다.
 *
 * 밖으로 나가는 전환은 SideEffect로 받아 셸이 준 콜백으로 올려보낸다. 홈 안에서 끝나는 전환(방 시트·액션
 * 메뉴·가이드)은 여기 오지 않고 `HomeUiState`의 상태로만 흐른다
 * (→ `docs/specs/home-deck-exploration/contracts/home-ui.md` §1·§3).
 *
 * 액션의 일회성 실패는 상태가 아니라 ViewModel 인스턴스별 채널로 오므로 셸이 아니라 여기서 수집해
 * 셸이 내려준 스낵바 호스트에 띄운다(`docs/conventions/error_handling.md` §5·§6).
 *
 * @param onNavigateToPlaceDetail 카드 본문 탭 → [SCR-006] 장소 상세(spec FR-007).
 * @param onNavigateToRoomForm 방 시트의 `방 만들기` 칸(spec EC-015).
 * @param onCreateRoomFromEmpty 빈 상태 안내의 `공동방 만들기` CTA(spec FR-020).
 */
@Composable
internal fun HomeRoute(
    onNavigateToPlaceDetail: (pinId: String) -> Unit,
    onNavigateToRoomForm: () -> Unit,
    onCreateRoomFromEmpty: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current
    val context = LocalContext.current

    // 권한 요청 자체는 [SYS-004]의 소관이고 홈이 정하는 것은 「물어야 하는 시점」뿐이다(R-009).
    // 거부도 좌표를 못 얻은 것도 결과가 같아 둘 다 null로 되돌린다 — ViewModel이 소진으로 흡수한다(EC-009).
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val location = if (grants.values.any { it }) context.lastKnownLocation() else null
            viewModel.processIntent(HomeIntent.LocationPermissionResult(location))
        }

    CollectSideEffect(viewModel.sideEffect) { effect ->
        when (effect) {
            is HomeSideEffect.NavigateToPlaceDetail -> onNavigateToPlaceDetail(effect.pinId)
            // 지금 이 신호를 내는 Intent가 없다 — 방 시트의 `방 만들기`는 아래 HomeScreen 콜백으로 바로
            // 나간다. 나중에 ViewModel이 이 길로 보내더라도 가는 곳은 같도록 같은 콜백을 잇는다.
            HomeSideEffect.NavigateToRoomForm -> onNavigateToRoomForm()
            HomeSideEffect.RequestLocationPermission -> locationPermissionLauncher.launch(LocationPermissions)
            HomeSideEffect.ShowSaveResult ->
                scope.launch {
                    // 저장 성공만 여기로 오고 문구는 화면이 정한다(spec FR-005, contracts §3).
                    // 실패는 아래 CollectDomainError가 받는다(conventions/error_handling.md §5).
                    snackbarHostState.showSnackbar(resources.getString(R.string.home_save_result_success))
                }
        }
    }

    // 수집기가 받는 것은 ViewModel이 아니라 에러 방출자다. 그 자리에 ViewModel을 그대로 놓으면 하위로
    // ViewModel을 흘려보내는 것과 구분되지 않으므로 넘길 능력만 남겨 타입을 좁힌다.
    val errorEmitter: DomainErrorEmitter = viewModel
    CollectDomainError(errorEmitter) { error ->
        scope.launch { snackbarHostState.showSnackbar(resources.getString(actionErrorMessageResOf(error))) }
    }

    HomeScreen(
        state = state,
        onIntent = viewModel::processIntent,
        onCreateRoom = onNavigateToRoomForm,
        onCreateRoomFromEmpty = onCreateRoomFromEmpty,
        modifier = modifier,
    )
}

/**
 * 마지막으로 알려진 좌표. 없으면 `null`이고 거부와 같은 길로 흡수된다(spec EC-009, R-013).
 *
 * 새 측위를 걸지 않는다 — 덱 요청 하나를 위해 GPS를 깨우면 첫 카드가 그만큼 늦게 뜬다.
 * 응답과 조회 사이에 권한이 철회되면 `SecurityException`이 나므로 그것까지 좌표 없음으로 흡수한다.
 */
private fun Context.lastKnownLocation(): GeoPoint? {
    val manager = getSystemService(LocationManager::class.java) ?: return null
    val location = try {
        manager.getProviders(true).firstNotNullOfOrNull(manager::getLastKnownLocation)
    } catch (permissionRevoked: SecurityException) {
        null
    }
    return location?.let { GeoPoint(latitude = it.latitude, longitude = it.longitude) }
}

/** 둘 중 하나만 허용돼도 좌표를 얻을 수 있다. 정렬에 쓰는 거리라 대략적 위치로 충분하다. */
private val LocationPermissions =
    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

/**
 * 액션 실패 문구. 리프를 구분하지 않고 한 줄로 안내한다 — 사용자가 할 수 있는 일이 재시도로 같아
 * 원인을 갈라 봐야 행동이 달라지지 않는다.
 *
 * `else`를 두지 않아 리프가 늘면 컴파일이 멈추고 여기서 다시 판단하게 된다. 공통 매퍼를 두지 않는 이유는
 * `docs/conventions/error_handling.md` §8이 소유한다.
 */
@StringRes
private fun actionErrorMessageResOf(error: MinoDomainException): Int =
    when (error) {
        is MinoDomainException.Network,
        is MinoDomainException.Http,
        is MinoDomainException.Auth,
        -> R.string.home_error_action_failed
    }
