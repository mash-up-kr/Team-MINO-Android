package team.mino.feature.room.main.vm

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomListSortOption
import team.mino.core.domain.repository.RoomRepository
import team.mino.core.navigation.activity.launcher.RoomDetailLauncher
import team.mino.core.navigation.activity.launcher.RoomFormLauncher
import team.mino.feature.room.main.component.DefaultMapCenter
import team.mino.feature.room.main.model.BottomSheetLevel
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 방 리스트 탭의 유일한 Route(RoomListMain) ViewModel.
 *
 * 계약: docs/specs/room-list/contracts/room-list-main-contract.md
 *
 * 지금 단계(US1)는 지도·3단 시트·필터 관련 Intent만 처리한다. 방 목록·상세 진입(US2)·공동방
 * 생성(US3)·Nudge(US4) 관련 Intent는 각 사용자 스토리 단계에서 채운다.
 */
@HiltViewModel
class RoomListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val roomRepository: RoomRepository,
    val roomDetailLauncher: RoomDetailLauncher,
    val roomFormLauncher: RoomFormLauncher,
) : ViewModel(),
    MviContainer<RoomListUiState, RoomListSideEffect> by mviContainer(RoomListUiState()) {
    init {
        observeMyRooms()
    }

    /**
     * [contracts/room-list-main-contract.md 「재조회」] `personalRoom`·`groupRooms`는
     * `RoomRepository.observeMyRooms()` 구독으로 항상 최신 유지된다. `groupRooms`가 갱신될 때마다
     * `showNudge`·`showGhostCard`를 `groupRooms.isEmpty()` 파생값으로 함께 계산한다
     * (FR-008~FR-010, [contracts/room-list-main-contract.md 「분기 규칙 — Nudge·Ghost Card 노출」]).
     */
    private fun observeMyRooms() {
        launchSafely {
            roomRepository.observeMyRooms().collect { rooms -> onRoomsUpdated(rooms) }
        }
    }

    private fun onRoomsUpdated(rooms: List<Room>) {
        val personal = rooms.firstOrNull { it.isPersonal }
        val group = rooms.filterNot { it.isPersonal }
        updateState {
            val sortedGroup = group.sortedByRoomListOption(roomListSort).toImmutableList()
            copy(
                personalRoom = personal,
                groupRooms = sortedGroup,
                showNudge = sortedGroup.isEmpty(),
                showGhostCard = sortedGroup.isEmpty(),
            )
        }
    }

    /**
     * [FR-001]·[EC-007] 진입 시 초기 sheetLevel 결정. `RoomListRoute`가 시작 인자를 받는 즉시(첫
     * 컴포지션) 한 번 호출한다 — 아직 `RoomMain` Route에 인자가 없어(EC-007 크로스 feature 배선은
     * `room-detail`[SCR-005] 미구현 상태) `SavedStateHandle.toRoute()` 대신 Route가 직접 넘겨준다.
     */
    fun resolveInitialSheetLevel(sheetLevelOverride: BottomSheetLevel?) {
        updateState { copy(sheetLevel = sheetLevelOverride ?: BottomSheetLevel.HALF) }
    }

    fun processIntent(intent: RoomListIntent) {
        when (intent) {
            RoomListIntent.OnScreenEntered -> onScreenEntered()
            RoomListIntent.OnSheetDraggedUp -> onSheetDraggedUp()
            RoomListIntent.OnSheetDraggedDown -> onSheetDraggedDown()
            is RoomListIntent.OnMapSortSelected -> updateState { copy(mapMarkerSort = intent.option) }
            is RoomListIntent.OnCategoryFilterSelected -> updateState { copy(categoryFilter = intent.category) }
            RoomListIntent.OnCurrentLocationClick -> onCurrentLocationClick()
            is RoomListIntent.OnLocationPermissionResult -> onLocationPermissionResult(intent.granted)
            is RoomListIntent.OnRoomListSortSelected -> onRoomListSortSelected(intent.option)
            is RoomListIntent.OnRoomCardClick -> onRoomCardClick(intent.roomId)
            RoomListIntent.OnAddRoomClick -> onAddRoomClick()
            is RoomListIntent.OnRoomFormResult -> onRoomFormResult(intent.createdRoomId)

            // [FR-008][FR-009] Ghost Card·Nudge의 [공동방 만들기]는 [+] 버튼(OnAddRoomClick)과 같은
            // 전환 결정을 낸다 — NavigateToRoomForm을 재사용한다(T046과 동일 SideEffect).
            RoomListIntent.OnGhostCardClick,
            RoomListIntent.OnNudgeCreateClick,
            -> onAddRoomClick()
        }
    }

    /**
     * [D8] 상태 캐싱 없이 매 진입마다 OS 권한을 직접 조회한다.
     *
     * 재진입마다 `groupRooms.isEmpty()`로 `showNudge`·`showGhostCard`를 다시 계산한다([research.md D9]).
     */
    private fun onScreenEntered() {
        if (hasLocationPermission()) {
            launchSafely {
                val center = resolveMapCenter(granted = true)
                updateState { copy(mapCenter = center, mapCenterRequestId = mapCenterRequestId + 1) }
            }
        } else {
            launchSafely { postSideEffect(RoomListSideEffect.RequestLocationPermission) }
        }
    }

    /** [EC-002] 거부 시 기본 디폴트 좌표, 허용 시 실제 위치로 `mapCenter`를 설정한다. */
    private fun onLocationPermissionResult(granted: Boolean) {
        launchSafely {
            val center = resolveMapCenter(granted)
            updateState { copy(mapCenter = center, mapCenterRequestId = mapCenterRequestId + 1) }
        }
    }

    /** [research.md D10] 현재 위치 버튼 최소 구현 — `mapCenter`만 갱신한다. */
    private fun onCurrentLocationClick() {
        if (!hasLocationPermission()) return
        launchSafely {
            val center = resolveMapCenter(granted = true)
            updateState { copy(mapCenter = center, mapCenterRequestId = mapCenterRequestId + 1) }
        }
    }

    /** 거부 시 기본 디폴트 좌표, 허용 시 실제 위치로 해석한다(EC-002). 세 호출부가 공유하는 단일 규칙. */
    private suspend fun resolveMapCenter(granted: Boolean): GeoPoint =
        if (granted) currentDeviceLocation() ?: DefaultMapCenter else DefaultMapCenter

    /** [contracts/room-list-main-contract.md 「분기 규칙 — 시트 드래그 전이」] */
    private fun onSheetDraggedUp() {
        updateState {
            when (sheetLevel) {
                BottomSheetLevel.PEEK -> copy(sheetLevel = BottomSheetLevel.HALF)
                BottomSheetLevel.HALF -> copy(sheetLevel = BottomSheetLevel.FULL)
                BottomSheetLevel.FULL -> this
            }
        }
    }

    private fun onSheetDraggedDown() {
        updateState {
            when (sheetLevel) {
                BottomSheetLevel.PEEK -> this
                BottomSheetLevel.HALF -> copy(sheetLevel = BottomSheetLevel.PEEK)
                BottomSheetLevel.FULL -> copy(sheetLevel = BottomSheetLevel.HALF)
            }
        }
    }

    /** [FR-005] 개인방 고정은 [RoomListUiState.personalRoom]이 별도 필드라 자동으로 유지되고, 여기서는
     * [RoomListUiState.groupRooms]만 재정렬한다. */
    private fun onRoomListSortSelected(option: RoomListSortOption) {
        updateState {
            copy(
                roomListSort = option,
                groupRooms = groupRooms.sortedByRoomListOption(option).toImmutableList(),
            )
        }
    }

    /** [FR-006] 방 카드 선택 → [RoomListSideEffect.NavigateToRoomDetail] 발행(전환 결정만, 실제 호출은 Route). */
    private fun onRoomCardClick(roomId: String) {
        launchSafely { postSideEffect(RoomListSideEffect.NavigateToRoomDetail(roomId)) }
    }

    /** [FR-007] 시트 우상단 [+] → [RoomListSideEffect.NavigateToRoomForm] 발행(전환 결정만, 실제 호출은 Route). */
    private fun onAddRoomClick() {
        launchSafely { postSideEffect(RoomListSideEffect.NavigateToRoomForm) }
    }

    /**
     * [FR-007] 공동방 생성 폼 결과 수신. `createdRoomId`가 있으면(생성 완료) 곧바로
     * [RoomListSideEffect.NavigateToRoomDetail]로 체이닝한다(새 SideEffect를 만들지 않고 [FR-006]의
     * 것을 재사용) — `null`이면(취소) 아무 것도 하지 않는다.
     */
    private fun onRoomFormResult(createdRoomId: String?) {
        if (createdRoomId == null) return
        launchSafely { postSideEffect(RoomListSideEffect.NavigateToRoomDetail(createdRoomId)) }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * 기기 위치. `:core:map`은 지도 렌더링만 담당하고 위치 조회 인프라가 없어(README 확인 완료)
     * 프레임워크 `LocationManager`로 직접 조회한다 — 별도 SDK 의존을 새로 들이지 않는다.
     *
     * 캐시된 마지막 위치(`getLastKnownLocation`)부터 확인하고, 없으면(다른 앱이 최근에 위치를 요청한
     * 적이 없는 기기에서는 모든 provider가 `null`을 반환한다) 활성화된 provider로 새 위치를 능동적으로
     * 요청한다. `LOCATION_FETCH_TIMEOUT`을 넘기면 [resolveMapCenter]가 기본 좌표로 폴백한다.
     */
    @SuppressLint("MissingPermission")
    private suspend fun currentDeviceLocation(): GeoPoint? {
        val locationManager = context.getSystemService(LocationManager::class.java) ?: return null
        val cached = locationManager.allProviders
            .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
        if (cached != null) return cached.toGeoPoint()

        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return null
        }
        return withTimeoutOrNull(LOCATION_FETCH_TIMEOUT) { requestSingleLocationUpdate(locationManager, provider) }
    }

    /** [requestSingleUpdate]는 API 21부터 지원한다(minSdk 29) — `getCurrentLocation`(API 30+)보다 넓은 범위를 커버한다. */
    private suspend fun requestSingleLocationUpdate(
        locationManager: LocationManager,
        provider: String,
    ): GeoPoint? =
        suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) continuation.resume(location.toGeoPoint())
                }
            }
            continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }
            runCatching {
                locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            }.onFailure {
                if (continuation.isActive) continuation.resume(null)
            }
        }

    private fun Location.toGeoPoint(): GeoPoint = GeoPoint(latitude = latitude, longitude = longitude)

    private companion object {
        val LOCATION_FETCH_TIMEOUT = 10.seconds
    }
}

/**
 * [RoomListSortOption.ALL]은 서버가 내려준 원래 순서를 유지한다(별도 정렬 기준 없음).
 * 저장된 장소가 없어 [Room.lastPlaceSavedAt]이 `null`인 방은 "최근 저장 순"에서 가장 뒤로 보낸다.
 */
@OptIn(ExperimentalTime::class)
private fun List<Room>.sortedByRoomListOption(option: RoomListSortOption): List<Room> =
    when (option) {
        RoomListSortOption.ALL -> this
        RoomListSortOption.RECENTLY_SAVED -> sortedByDescending { it.lastPlaceSavedAt ?: Instant.DISTANT_PAST }
        RoomListSortOption.MOST_COMMENTED -> sortedByDescending { it.commentCount }
    }
