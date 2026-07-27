package team.mino.core.data.device

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DeviceInfoProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : DeviceInfoProvider {
    // ANDROID_ID는 일부 비정상 기기·커스텀 롬에서 null이나 빈 값이 보고된 사례가 있어 nullable로 노출한다.
    // HardwareIds: 광고·분석이 아닌 기기 신원(토큰 발급) 용도의 의도적 사용
    // — docs/adr/2026-07-27-preferences-datastore-local-storage.md·이슈 #89
    @SuppressLint("HardwareIds")
    override suspend fun getDeviceId(): String? =
        withContext(Dispatchers.IO) {
            // Settings provider 접근은 동기 바인더 IPC라 메인 스레드에서 호출되지 않도록 여기서 격리한다.
            Settings.Secure
                .getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?.takeIf { it.isNotBlank() }
        }
}
