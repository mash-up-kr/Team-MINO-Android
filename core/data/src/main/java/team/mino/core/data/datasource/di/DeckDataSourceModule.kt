package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.DeckMockRemoteDataSourceImpl
import team.mino.core.data.datasource.DeckRemoteDataSource
import javax.inject.Singleton

/**
 * `GET /api/v1/rooms/{roomId}/cards`가 배포될 때 갈아 끼우는 곳이 아래 `@Binds`의 인자 타입 하나다 —
 * `docs/specs/home-deck-exploration/contracts/deck-api.md` §4가 적은 세 곳 중 두 번째다.
 * 같은 형태를 `RoomDataSourceModule`이 먼저 통과했다(research.md R-001).
 *
 * mock이 상태를 갖지 않는데도 `@Singleton`인 것은 실구현으로 바뀐 뒤에도 유지되는 DataSource 공통 규칙
 * (`core/data/README.md` §5)을 따른 것이다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DeckDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindDeckRemoteDataSource(impl: DeckMockRemoteDataSourceImpl): DeckRemoteDataSource
}
