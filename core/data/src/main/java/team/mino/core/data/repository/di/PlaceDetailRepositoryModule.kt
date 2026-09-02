package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.PlaceCommentRepositoryImpl
import team.mino.core.data.repository.PlaceRepositoryImpl
import team.mino.core.domain.repository.PlaceCommentRepository
import team.mino.core.domain.repository.PlaceRepository
import javax.inject.Singleton

/**
 * 장소 상세가 쓰는 두 계약의 바인딩. 인터페이스를 가른 것은 생애 차이지만
 * (`docs/specs/place-detail/research.md` D8) 화면 하나가 함께 주입받는 짝이라 파일을 나누지 않았다 —
 * `HomeRepositoryModule`과 같은 판단이다.
 *
 * 바인딩을 구현이 있는 `:core:data`가 소유하는 근거는
 * `docs/adr/2026-08-02-di-binding-ownership.md`.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class PlaceDetailRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPlaceRepository(impl: PlaceRepositoryImpl): PlaceRepository

    @Binds
    @Singleton
    abstract fun bindPlaceCommentRepository(impl: PlaceCommentRepositoryImpl): PlaceCommentRepository
}
