package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import team.mino.core.data.network.dto.response.NotificationPageResponse
import javax.inject.Inject

/**
 * 알림함 엔드포인트를 호출하는 서비스. 계약은
 * `docs/specs/notifications/contracts/notification-api.md`가 소유한다.
 *
 * FCM 토큰 등록(`PUT /api/v1/users/me/push-token`)은 서버 리소스가 달라 [UserApiService]에 있다 —
 * 서비스의 단위는 feature가 아니라 서버 리소스다
 * (`docs/adr/2026-08-28-api-service-owned-per-server-tag.md`).
 *
 * 신원 증명 헤더는 `MinoIdentityProofPlugin`이 싣고, 예외는 `convertDomainException`이
 * `MinoDomainException`으로 바꿔 던지므로 여기서 잡지 않는다.
 */
internal class NotificationApiService @Inject constructor(
    private val client: HttpClient,
) {
    /**
     * 알림 목록의 [page]쪽을 가져온다 — 같은 계약 §1.
     *
     * **[CommentApiService.getComments]와 같은 이유로 `MinoResponse`로 봉투를 벗기지 않는다.** 서버가
     * `data`와 나란히 `pagination`을 실어 알맹이만 남기면 `hasNext`가 사라진다
     * ([NotificationPageResponse] KDoc).
     *
     * **`pageSize`를 싣지 않는다.** 한 묶음의 크기를 화면이 정하지 않고 서버 기본값 20을 따르며,
     * `20`을 박아 보내면 그 전제가 거짓이 된다(같은 계약 §1 · spec §4).
     *
     * 서버가 두 쿼리 파라미터를 `integer`가 아니라 `string`으로 선언해 [page]를 문자열로 넘긴다.
     *
     * [page] 0이 최신 묶음이고, 묶음 안의 순서는 서버가 정한 최신순 그대로다 — 클라이언트가 재정렬하지
     * 않는다(FR-001).
     */
    suspend fun getNotifications(page: Int): NotificationPageResponse =
        client
            .get("api/v1/notifications") {
                parameter("page", page.toString())
            }.body<NotificationPageResponse>()
}
