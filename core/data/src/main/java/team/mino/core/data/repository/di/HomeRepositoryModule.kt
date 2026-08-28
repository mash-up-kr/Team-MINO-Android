package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.HomeDeckRepositoryImpl
import team.mino.core.data.repository.HomePreferencesRepositoryImpl
import team.mino.core.domain.repository.HomeDeckRepository
import team.mino.core.domain.repository.HomePreferencesRepository
import javax.inject.Singleton

/**
 * 홈이 쓰는 두 계약의 바인딩. 화면 하나가 함께 주입받는 짝이라 파일을 나누지 않았다 —
 * `docs/specs/home-deck-exploration/contracts/home-ui.md` §4.2·§4.3.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class HomeRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindHomeDeckRepository(impl: HomeDeckRepositoryImpl): HomeDeckRepository

    @Binds
    @Singleton
    abstract fun bindHomePreferencesRepository(impl: HomePreferencesRepositoryImpl): HomePreferencesRepository
}
