package team.mino.core.data.repository

import team.mino.core.data.datasource.PinRemoteDataSource
import team.mino.core.data.network.dto.request.PinDuplicateRequest
import team.mino.core.data.repository.mapper.toDomain
import team.mino.core.domain.model.PlaceDetail
import team.mino.core.domain.repository.PlaceRepository
import team.mino.core.errorhandling.MinoDomainException
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import javax.inject.Inject

/**
 * [PlaceRepository]의 구현 — 계약은 `docs/specs/place-detail/contracts/place-repository.md` §1이 소유한다.
 *
 * 세 함수 모두 [PinRemoteDataSource] 하나를 쓴다. 핀 상세·접근 기록·복제가 같은 `pins` 엔드포인트 묶음이라
 * 출처가 갈리지 않는다. 코멘트는 생애가 달라 [PlaceCommentRepositoryImpl]로 갈랐다
 * (`docs/specs/place-detail/research.md` D8).
 *
 * [recordAccess]를 뺀 나머지는 예외를 잡지 않는다 — 매핑은 `HttpClient`의 `convertDomainException`이 전역
 * 수행하고 실패는 `MinoDomainException`으로 그대로 전파된다(`core/data/README.md` §6).
 */
internal class PlaceRepositoryImpl @Inject constructor(
    private val pinRemoteDataSource: PinRemoteDataSource,
) : PlaceRepository {
    override suspend fun getPlaceDetail(pinId: String): PlaceDetail = pinRemoteDataSource.getPinDetail(pinId).toDomain()

    /**
     * **실패를 삼키는 유일한 함수다.** 기록 실패가 화면 동작에 영향을 주지 않는다는 것이 계약이므로
     * (EC-022), 호출부가 `try`로 감싸야 한다면 그 규칙이 호출부마다 새어 나간다.
     *
     * [runCatchingDomain]은 [MinoDomainException]만 잡고 `CancellationException`과 버그는 통과시킨다 —
     * 취소를 삼키면 코루틴 취소가 깨지고(`docs/conventions/error_handling.md` §3), 버그를 삼키면 CEH에
     * 닿지 못한다. 그래서 `try`/`catch (Throwable)`이 아니라 이 헬퍼를 쓴다. 실패 분기는 [onDomainFailure]로
     * 반드시 소비한다(같은 문서 §7-4) — 삼킨다는 것이 이 계약의 내용이므로, 그 사실을 결과를 버리는 대신
     * 빈 소비로 드러낸다.
     *
     * 재시도·디바운스·중복 제거를 하지 않는다(EC-022·EC-023). append-only 로그라 서버도 중복을 문제
     * 삼지 않는다.
     *
     * [HomeDeckRepositoryImpl.recordPlaceOpened]가 같은 엔드포인트를 치면서 실패를 삼키지 않는 것과
     * 어긋나 보이지만 계약이 다르다 — 그쪽은 결과를 기다릴지를 호출자가 정한다.
     */
    override suspend fun recordAccess(pinId: String) {
        runCatchingDomain { pinRemoteDataSource.recordAccess(pinId) }
            .onDomainFailure {
                // 알릴 곳도 되돌릴 상태도 없다 — 계약이 결과를 호출자에게 주지 않으므로 여기서 끝낸다(EC-022).
            }
    }

    /**
     * 빈 [roomIds]를 막지 않는다 — 서버 스키마가 `minItems: 1`이지만 그것을 막는 자리는 화면의 [공유하기]
     * 비활성 규칙(FR-022)이고, 이 구현은 그 전제를 신뢰한다.
     *
     * 이미 저장된 방이 섞여 서버가 `409`를 주면 그대로 전파한다 — 저장되지 않은 것이 저장된 것으로 보이면
     * 안 된다(`docs/specs/place-detail/research.md` D14).
     */
    override suspend fun duplicatePin(
        pinId: String,
        roomIds: List<String>,
    ) = pinRemoteDataSource.duplicatePin(
        pinId = pinId,
        request = PinDuplicateRequest(roomIds = roomIds),
    )
}
