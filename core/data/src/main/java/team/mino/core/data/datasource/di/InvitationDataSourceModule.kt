package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.InvitationRemoteDataSource
import team.mino.core.data.datasource.InvitationRemoteDataSourceImpl
import javax.inject.Singleton

/**
 * 초대 원격 출처의 바인딩. 구현체는 실서버를 무는 [InvitationRemoteDataSourceImpl] 하나뿐이다.
 *
 * `@Singleton`은 DataSource 공통 규칙(`core/data/README.md` §5)을 따른 것으로, 이 구현체가 상태를
 * 들고 있어서가 아니다 — 초대 코드는 캐시하지 않는다(계약 §1.1).
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class InvitationDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindInvitationRemoteDataSource(impl: InvitationRemoteDataSourceImpl): InvitationRemoteDataSource
}
