package team.mino.feature.room.di

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import team.mino.core.navigation.activity.launcher.RoomDetailLauncher
import javax.inject.Inject

/**
 * 임시 스텁 — `:feature:roomdetail`이 생기면 이 파일을 지우고 그 모듈의 `di/`가
 * [RoomDetailLauncher] 바인딩을 넘겨받는다.
 *
 * `:feature:roomform`은 이제 실제 모듈이라(group-room-form) 그쪽의 `RoomFormNavigationModule`이
 * [team.mino.core.navigation.activity.launcher.RoomFormLauncher] 바인딩을 직접 제공한다 — 여기서
 * 같이 바인딩하면 Hilt가 중복 바인딩으로 거부한다. `:feature:roomdetail`은 아직 없어 그 대상만
 * no-op 구현으로 남긴다. `launch()` 호출 시 실제 화면 전환은 일어나지 않는다.
 */
internal class RoomDetailLauncherStub @Inject constructor() : RoomDetailLauncher {
    override fun launch(
        activity: Activity,
        resultLauncher: ActivityResultLauncher<Intent>?,
        withFinish: Boolean,
        intentBuilder: (Intent.() -> Intent)?,
    ) {
        // no-op: :feature:roomdetail이 생기기 전까지 실제 화면 전환 없음
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class RoomLauncherStubModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun bindRoomDetailLauncher(impl: RoomDetailLauncherStub): RoomDetailLauncher
}
