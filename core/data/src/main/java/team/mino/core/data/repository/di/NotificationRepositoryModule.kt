package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.NotificationRepositoryImpl
import team.mino.core.domain.repository.NotificationRepository
import javax.inject.Singleton

/**
 * 알림함 목록 계약의 바인딩. 바인딩을 구현이 있는 `:core:data`가 소유하는 근거는
 * `docs/adr/2026-08-02-di-binding-ownership.md`이며, 위치는
 * `docs/specs/notifications/contracts/notification-repository.md` §4가 지정한다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class NotificationRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
}
