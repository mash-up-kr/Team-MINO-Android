package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.RoomListRemoteDataSource
import team.mino.core.data.datasource.RoomListRemoteDataSourceImpl
import javax.inject.Singleton

/**
 * 방 목록 조회 전용 바인딩. mock을 물고 있는 `RoomDataSourceModule`과 나뉘어 있는 근거는
 * `docs/specs/shared-link-receiver/research.md` R-015.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class RoomListDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindRoomListRemoteDataSource(impl: RoomListRemoteDataSourceImpl): RoomListRemoteDataSource
}
