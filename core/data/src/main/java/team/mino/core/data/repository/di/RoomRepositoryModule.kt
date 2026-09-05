package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.RoomPreferencesRepositoryImpl
import team.mino.core.data.repository.RoomRepositoryImpl
import team.mino.core.domain.repository.RoomPreferencesRepository
import team.mino.core.domain.repository.RoomRepository
import javax.inject.Singleton

/**
 * 방 리스트가 함께 주입받는 두 계약의 바인딩 — `HomeRepositoryModule`과 같은 이유로 화면 하나가 쓰는
 * 짝이라 파일을 나누지 않았다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class RoomRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRoomRepository(impl: RoomRepositoryImpl): RoomRepository

    @Binds
    @Singleton
    abstract fun bindRoomPreferencesRepository(impl: RoomPreferencesRepositoryImpl): RoomPreferencesRepository
}
