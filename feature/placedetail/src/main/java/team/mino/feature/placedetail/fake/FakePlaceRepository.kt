package team.mino.feature.placedetail.fake

import kotlinx.coroutines.delay
import team.mino.core.domain.model.PlaceDetail
import team.mino.core.domain.repository.PlaceRepository
import team.mino.core.errorhandling.MinoDomainException
import team.mino.core.errorhandling.runCatchingDomain
import javax.inject.Inject
import javax.inject.Singleton

/**
 * **이번 UI 라운드 한정 [PlaceRepository] 구현이다. `tasks.md` T063이 `fake/` 패키지를 통째로 지운다.**
 *
 * API 연결 없이 화면을 검증하기 위한 것이며, 실제 구현은 `:core:data`가 갖는다(T060). 데이터 원천은
 * [FakePlaceDetailData] 하나다.
 *
 * 지연을 넣어 로딩 구간을 눈으로 볼 수 있게 한다 — 시트가 로딩 상태를 그리는지, 마커가 두 조회가 끝난 뒤에
 * 나타나는지(`docs/specs/place-detail/research.md` D15)를 이 지연이 드러낸다.
 */
@Singleton
internal class FakePlaceRepository @Inject constructor() : PlaceRepository {
    override suspend fun getPlaceDetail(pinId: String): PlaceDetail {
        delay(PLACE_DETAIL_DELAY_MS)
        return FakePlaceDetailData.placeOf(pinId)
    }

    /**
     * 인터페이스 규약대로 **예외를 밖으로 내보내지 않는다.** 취소만 그대로 전파한다 — [runCatchingDomain]이
     * [MinoDomainException]만 잡고 `CancellationException`은 통과시킨다.
     */
    override suspend fun recordAccess(pinId: String) {
        runCatchingDomain { delay(RECORD_ACCESS_DELAY_MS) }
    }

    /**
     * 성공만 흉내 낸다. 이미 저장된 방과의 `409` 경합은 이번 범위에서 재현하지 않는다
     * (`docs/specs/place-detail/research.md` D14·D15 — `hasPlace`가 없어 그 상태 자체를 만들 수 없다).
     */
    override suspend fun duplicatePin(
        pinId: String,
        roomIds: List<String>,
    ) {
        delay(DUPLICATE_DELAY_MS)
    }

    private companion object {
        const val PLACE_DETAIL_DELAY_MS = 700L
        const val RECORD_ACCESS_DELAY_MS = 300L
        const val DUPLICATE_DELAY_MS = 500L
    }
}
