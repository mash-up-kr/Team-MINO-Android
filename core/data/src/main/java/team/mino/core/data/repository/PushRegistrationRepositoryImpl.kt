package team.mino.core.data.repository

import team.mino.core.data.datasource.UserRemoteDataSource
import team.mino.core.data.push.PushTokenProvider
import team.mino.core.domain.repository.PushRegistrationRepository
import team.mino.core.errorhandling.MinoDomainException
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import javax.inject.Inject

/** `PUT /api/v1/users/me/push-token`의 `platform` 값 — `docs/specs/push-notification/contracts/push-token-api.md` §1. */
private const val PLATFORM_ANDROID = "android"

/**
 * [PushRegistrationRepository]의 구현 — 계약 문구는 `docs/specs/push-notification/data-model.md` §4가 소유한다.
 *
 * 토큰 조회([PushTokenProvider])와 서버 등록([UserRemoteDataSource])을 여기서 묶는다. 등록 엔드포인트가
 * OpenAPI `user` 태그라 전용 DataSource를 두지 않고 기존 것을 넓혔다
 * (`docs/specs/push-notification/research.md` D5). 매퍼·로컬 저장·재시도 큐는 두지 않는다 — 앱 시작마다
 * 값과 무관하게 한 번 시도하는 것이 계약이다.
 */
internal class PushRegistrationRepositoryImpl @Inject constructor(
    private val pushTokenProvider: PushTokenProvider,
    private val userRemoteDataSource: UserRemoteDataSource,
) : PushRegistrationRepository {
    /**
     * **실패를 삼키는 함수다.** 등록 실패는 사용자에게 보이지 않고 앱의 다른 기능을 막지 않는다는 것이
     * 계약이므로(FR-004·UX-002), [PlaceRepositoryImpl.recordAccess]와 같은 형태로 여기서 끝낸다.
     *
     * [runCatchingDomain]은 [MinoDomainException]만 잡고 `CancellationException`과 버그는 통과시킨다 —
     * 취소를 삼키면 코루틴 취소가 깨지고, 버그를 삼키면 CEH에 닿지 못한다(`docs/conventions/error_handling.md`
     * §3·§4). 실패 분기는 [onDomainFailure]로 반드시 소비한다(같은 문서 §7-4).
     *
     * 토큰 조회 실패(EC-001)와 서버 등록 실패(EC-002)를 여기서 따로 가르지 않는다 — 둘 다 원천의 매핑 지점
     * (`push/extension/Task.kt`·`HttpClient`의 `convertDomainException`)에서 [MinoDomainException]으로 바뀌어
     * 같은 분기로 들어온다. 지역 catch를 두지 않는 것은 원천당 매핑 지점 하나라는 규칙(같은 문서 §3)이다.
     */
    override suspend fun registerCurrentToken() {
        runCatchingDomain { userRemoteDataSource.putPushToken(pushTokenProvider.currentToken(), PLATFORM_ANDROID) }
            .onDomainFailure {
                // 알릴 곳도 되돌릴 상태도 없다 — 다음 앱 시작에서 다시 시도한다(EC-001·EC-002·EC-004).
            }
    }
}
