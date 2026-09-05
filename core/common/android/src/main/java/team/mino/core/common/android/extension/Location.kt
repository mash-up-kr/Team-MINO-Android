package team.mino.core.common.android.extension

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import team.mino.core.common.kotlin.geo.GeoPoint
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 기기 위치. 캐시된 마지막 위치(`getLastKnownLocation`)부터 확인하고, 없으면(다른 앱이 최근에 위치를
 * 요청한 적이 없는 기기에서는 모든 provider가 `null`을 반환한다) 활성화된 provider로 새 위치를
 * 능동적으로 요청한다. [timeout]을 넘기면 `null`로 포기한다.
 *
 * 호출 전에 위치 권한이 있는지는 호출부가 확인해야 한다 — 권한이 없는 상태에서 부르면
 * `SecurityException`이 난다.
 *
 * `RoomListViewModel`(지도 카메라 중심)과 `HomeRoute`(홈 "가까운 순" 카드 정렬)가 같은 방식·같은
 * 타임아웃을 각자 구현해 뒀던 것을 이 자리로 모았다 — 위치가 필요한 세 번째 화면이 생겨도 이 함수
 * 하나만 호출하면 된다.
 */
@SuppressLint("MissingPermission")
suspend fun Context.currentDeviceLocation(timeout: Duration = DEFAULT_LOCATION_FETCH_TIMEOUT): GeoPoint? {
    val locationManager = getSystemService(LocationManager::class.java) ?: return null
    val cached = try {
        locationManager.allProviders
            .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
    } catch (permissionRevoked: SecurityException) {
        null
    }
    if (cached != null) return cached.toGeoPoint()

    val provider = when {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> return null
    }
    return withTimeoutOrNull(timeout) { requestSingleLocationUpdate(locationManager, provider) }
}

/** [requestSingleUpdate]는 API 21부터 지원한다(minSdk 29) — `getCurrentLocation`(API 30+)보다 넓은 범위를 커버한다.
 * `@Suppress("DEPRECATION")`은 그 이유로 대체하지 않고 그대로 쓰기로 한 의도적 선택이다. */
@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
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

private val DEFAULT_LOCATION_FETCH_TIMEOUT = 10.seconds
