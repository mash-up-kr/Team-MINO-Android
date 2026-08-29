package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.DeckRemoteDataSource
import team.mino.core.data.datasource.DeckRemoteDataSourceImpl
import javax.inject.Singleton

/**
 * `GET /api/v1/rooms/{roomId}/cards`가 배포되면서 mock 바인딩을 실구현으로 갈아 끼운 자리다 —
 * `docs/specs/home-deck-exploration/contracts/deck-api.md` §4가 적은 전환 지점 세 곳 중 두 번째이며,
 * 인터페이스도 호출부도 바뀌지 않았다(research.md R-001).
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DeckDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindDeckRemoteDataSource(impl: DeckRemoteDataSourceImpl): DeckRemoteDataSource
}
