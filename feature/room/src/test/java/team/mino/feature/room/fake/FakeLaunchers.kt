package team.mino.feature.room.fake

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import team.mino.core.navigation.activity.launcher.RoomFormLauncher

/**
 * `RoomListViewModel` 생성자가 요구하는 런처 계약의 no-op 테스트 더블.
 *
 * ViewModel은 전환 SideEffect만 발행하고 실제 `launch()` 호출은 Route가 담당하므로,
 * 이 더블은 어떤 테스트에서도 실제로 호출되지 않는다 — 생성자를 만족시키기 위한 자리다.
 */
internal class FakeRoomFormLauncher : RoomFormLauncher {
    override fun launch(
        activity: Activity,
        resultLauncher: ActivityResultLauncher<Intent>?,
        withFinish: Boolean,
        intentBuilder: (Intent.() -> Intent)?,
    ) = Unit
}
