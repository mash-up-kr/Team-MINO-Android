package team.mino.feature.main.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.navigation.deeplink.MainDeepLinkIntentFactory
import javax.inject.Singleton

/**
 * `ActivityRetainedComponent`인 [MainNavigationModule]과 달리 `SingletonComponent`다 —
 * 소비자가 Activity가 아니라 `:core:notification`의 Service이기 때문이다(push-deeplink-contract.md §2).
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class MainDeepLinkModule {
    @Binds
    @Singleton
    abstract fun bindMainDeepLinkIntentFactory(impl: MainDeepLinkIntentFactoryImpl): MainDeepLinkIntentFactory
}
