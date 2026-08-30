package team.mino.feature.sharereceiver.di

import android.content.Context
import android.content.res.Resources
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * 방 선택 시트의 ViewModel이 요구하는 [Resources] 제공.
 *
 * 도메인 모델을 카드가 그릴 형태로 옮기는 변환이 `장소 N개` 포맷을 소유하므로
 * (`docs/specs/shared-link-receiver/data-model.md` §5.2) 목록을 만드는 ViewModel이 리소스에 닿는다.
 *
 * [ViewModelComponent]에 두는 것은 이 바인딩을 요구하는 곳이 이 모듈의 ViewModel 하나뿐이기 때문이다.
 * 앱 전역 그래프에 올리면 소유자 없는 공용 바인딩이 feature에서 자라난다.
 */
@Module
@InstallIn(ViewModelComponent::class)
internal object ShareReceiverResourcesModule {
    @Provides
    @ViewModelScoped
    fun provideResources(
        @ApplicationContext context: Context,
    ): Resources = context.resources
}
