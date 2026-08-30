package team.mino.core.data.auth.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.auth.AnonymousAuthProvider
import team.mino.core.data.auth.AnonymousAuthProviderImpl
import team.mino.core.data.auth.IdTokenProvider
import team.mino.core.data.auth.IdTokenProviderImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AuthProviderModule {
    @Binds
    @Singleton
    abstract fun bindAnonymousAuthProvider(impl: AnonymousAuthProviderImpl): AnonymousAuthProvider

    @Binds
    @Singleton
    abstract fun bindIdTokenProvider(impl: IdTokenProviderImpl): IdTokenProvider
}
