package team.mino.feature.room.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 장소 상세의 ViewModel이 요구하는 [Clock] 제공.
 *
 * 코멘트 작성 시각을 구간 표기로 옮기려면 `createdAt` 말고 「지금」이 필요한데, 그 값을 환산 함수 안에서
 * `Clock.System.now()`로 읽지 않는다(`docs/specs/place-detail/contracts/place-detail-main-contract.md` §6.1).
 * ViewModel이 목록을 상태로 올릴 때 한 번 읽어 기준 시각을 함께 실어야 언제 판정했는지가 상태에 드러나고,
 * 주입 가능한 시계여야 구간 경계를 고정 시각으로 재현할 수 있다.
 *
 * [ViewModelComponent]에 두는 것은 이 바인딩을 요구하는 곳이 이 모듈의 ViewModel 하나뿐이기 때문이다.
 * 앱 전역 그래프에 올리면 소유자 없는 공용 바인딩이 feature에서 자라난다.
 */
@OptIn(ExperimentalTime::class)
@Module
@InstallIn(ViewModelComponent::class)
internal object PlaceDetailClockModule {
    @Provides
    fun provideClock(): Clock = Clock.System
}
