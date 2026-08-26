package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.RoomMockRemoteDataSourceImpl
import team.mino.core.data.datasource.RoomRemoteDataSource
import javax.inject.Singleton

/**
 * 실서버가 붙을 때 갈아 끼우는 지점이 아래 `@Binds`의 인자 타입 하나다 —
 * `docs/specs/group-room-form/contracts/room-api-mock.md` §4가 적은 세 곳 중 마지막이다.
 *
 * `@Singleton`인 이유는 mock 저장소가 프로세스 수명 동안 만들어진 방을 들고 있어야 하기 때문이 아니다.
 * 그 상태는 `RoomMockStore`가 `@Singleton`으로 소유하며, 여기의 스코프는 실구현으로 바뀐 뒤에도
 * 유지되는 DataSource 공통 규칙(`core/data/README.md` §5)을 따른 것이다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class RoomDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindRoomRemoteDataSource(impl: RoomMockRemoteDataSourceImpl): RoomRemoteDataSource
}
