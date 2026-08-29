package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.RoomInvitationRepositoryImpl
import team.mino.core.domain.repository.RoomInvitationRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RoomInvitationRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRoomInvitationRepository(impl: RoomInvitationRepositoryImpl): RoomInvitationRepository
}
