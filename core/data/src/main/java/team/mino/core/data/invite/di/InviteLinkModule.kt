package team.mino.core.data.invite.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.invite.InviteLinkBuilderImpl
import team.mino.core.domain.invite.InviteLinkBuilder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class InviteLinkModule {
    @Binds
    @Singleton
    abstract fun bindInviteLinkBuilder(impl: InviteLinkBuilderImpl): InviteLinkBuilder
}
