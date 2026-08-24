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
import team.mino.core.navigation.activity.launcher.RoomFormLauncher
import javax.inject.Inject

/**
 * 임시 스텁 — `:feature:roomdetail`·`:feature:roomform`이 생기면 이 파일을 지우고
 * 그 모듈의 `di/`가 [RoomDetailLauncher]·[RoomFormLauncher] 바인딩을 넘겨받는다.
 *
 * 두 feature 모듈이 아직 없어 실제 Activity로 전환할 대상이 없으므로,
 * `:feature:room`이 단독으로 빌드·Hilt 그래프 조립이 가능하도록 no-op 구현만 바인딩한다.
 * `launch()` 호출 시 실제 화면 전환은 일어나지 않는다.
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

internal class RoomFormLauncherStub @Inject constructor() : RoomFormLauncher {
    override fun launch(
        activity: Activity,
        resultLauncher: ActivityResultLauncher<Intent>?,
        withFinish: Boolean,
        intentBuilder: (Intent.() -> Intent)?,
    ) {
        // no-op: :feature:roomform이 생기기 전까지 실제 화면 전환 없음
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class RoomLauncherStubModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun bindRoomDetailLauncher(impl: RoomDetailLauncherStub): RoomDetailLauncher

    @Binds
    @ActivityRetainedScoped
    abstract fun bindRoomFormLauncher(impl: RoomFormLauncherStub): RoomFormLauncher
}
