package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.RoomRemoteDataSource
import team.mino.core.data.datasource.RoomRemoteDataSourceImpl
import javax.inject.Singleton

/**
 * 방 원격 출처의 바인딩. 구현체는 실서버를 무는 [RoomRemoteDataSourceImpl] 하나뿐이며,
 * 레이어 구성은 `docs/specs/group-room-form/contracts/room-api.md` §4가 소유한다.
 *
 * 되돌릴 mock 구현도, 출처를 고르는 스위치도 두지 않는다 — 실행되지 않는 분기를 프로덕션에 남기지
 * 않기 위해서다(`docs/specs/group-room-form/research.md` R-024).
 *
 * `@Singleton`은 DataSource 공통 규칙(`core/data/README.md` §5)을 따른 것으로, 이 구현체가 상태를
 * 들고 있어서가 아니다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class RoomDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindRoomRemoteDataSource(impl: RoomRemoteDataSourceImpl): RoomRemoteDataSource
}
