package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.HomePreferencesLocalDataSource
import team.mino.core.data.datasource.HomePreferencesLocalDataSourceImpl
import javax.inject.Singleton

/**
 * 홈의 로컬 저장 전용 바인딩. 원격 덱을 물고 있는 `DeckDataSourceModule`과 나뉜 것은
 * DataSource 하나당 모듈 하나라는 `core/data/README.md` §5의 형태를 따른 것이다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class HomePreferencesDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindHomePreferencesLocalDataSource(
        impl: HomePreferencesLocalDataSourceImpl,
    ): HomePreferencesLocalDataSource
}
