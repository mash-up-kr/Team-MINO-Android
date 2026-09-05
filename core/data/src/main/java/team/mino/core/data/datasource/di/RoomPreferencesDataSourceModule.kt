package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.RoomPreferencesLocalDataSource
import team.mino.core.data.datasource.RoomPreferencesLocalDataSourceImpl
import javax.inject.Singleton

/**
 * 방 리스트의 로컬 저장 전용 바인딩. `HomePreferencesDataSourceModule`과 같은 이유로
 * DataSource 하나당 모듈 하나(`core/data/README.md` §5)를 따른다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class RoomPreferencesDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindRoomPreferencesLocalDataSource(
        impl: RoomPreferencesLocalDataSourceImpl,
    ): RoomPreferencesLocalDataSource
}
