package team.mino.feature.placedetail.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.domain.repository.PlaceCommentRepository
import team.mino.core.domain.repository.PlaceRepository
import team.mino.feature.placedetail.fake.FakePlaceCommentRepository
import team.mino.feature.placedetail.fake.FakePlaceRepository
import javax.inject.Singleton

/**
 * **이번 UI 라운드 한정 모듈이다. `tasks.md` T063이 `fake/` 패키지와 함께 이 파일을 지운다.**
 *
 * Phase 10에서 `:core:data`의 실구현이 `PlaceRepositoryImpl`·`PlaceCommentRepositoryImpl`로 같은 두
 * 인터페이스를 바인딩하며, 그 바인딩은 구현을 소유한 `:core:data`가 갖는다
 * (`docs/adr/2026-08-02-di-binding-ownership.md`). 그때까지만 구현을 가진 이 모듈이 소유한다.
 *
 * **두 Fake가 `@Singleton`이라 바인딩도 `@Singleton`이다.** 코멘트 작성·삭제 결과를 인스턴스가 들고 있어서,
 * 스코프가 갈리면 같은 장소를 다시 열 때 다른 데이터를 보게 된다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class PlaceDetailFakeDataModule {
    @Binds
    @Singleton
    abstract fun bindPlaceRepository(impl: FakePlaceRepository): PlaceRepository

    @Binds
    @Singleton
    abstract fun bindPlaceCommentRepository(impl: FakePlaceCommentRepository): PlaceCommentRepository
}
