package team.mino.feature.room.fake

import android.content.ContextWrapper
import android.content.pm.PackageManager

/**
 * [RoomListViewModel][team.mino.feature.room.main.vm.RoomListViewModel] 생성에 필요한 최소 [Context] 더블.
 *
 * 위치 권한 판정(`ContextCompat.checkSelfPermission`)만 통제한다. `getSystemService(Class<T>)`는
 * 실제 `Context`에서 `final`이라 오버라이드할 수 없는데, 이 모듈의 단위 테스트 mockable android.jar
 * (`isReturnDefaultValues = true`)가 그 호출을 가로채 항상 `null`을 돌려주므로 손댈 필요가 없다 —
 * `RoomListViewModel.currentDeviceLocation()`이 `LocationManager`를 못 구해 `null`을 반환하는 경로가
 * 이 더블을 쓰는 모든 테스트에서 항상 결정적으로 재현된다. 실제 GPS 픽스를 흉내 내려면 Robolectric이
 * 필요한데 이 저장소에는 없어, "권한 허용 + 위치를 못 구해 기본 좌표로 떨어지는" 경로까지만 검증한다.
 */
internal class FakeLocationContext(
    permissionGranted: Boolean = false,
) : ContextWrapper(null) {
    private val permissionResult =
        if (permissionGranted) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED

    override fun checkPermission(
        permission: String,
        pid: Int,
        uid: Int,
    ): Int = permissionResult

    override fun checkSelfPermission(permission: String): Int = permissionResult
}
