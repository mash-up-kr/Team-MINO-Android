package team.mino.feature.splash.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.navigation.deeplink.SplashDeepLinkIntentFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SplashDeepLinkModule {
    @Binds
    @Singleton
    abstract fun bindSplashDeepLinkIntentFactory(impl: SplashDeepLinkIntentFactoryImpl): SplashDeepLinkIntentFactory
}
