package team.mino.core.domain.model

/**
 * 수신한 푸시 알림. payload를 앱이 해석한 결과이며, [title]·[body]는 서버가 완성해 보낸 문구 그대로다.
 *
 * @property type 알림 유형. payload의 `type`이 [PushMessageType]에 없으면 `null`이며, 그 알림은 표시하지 않는다.
 * @property targetId 도착지 식별자. 장소 대상 알림은 `pinId`, 공동방 대상 알림은 `roomId`, 그 외는 `null`.
 *   서버 필드명이 유형마다 다르므로 파싱이 단일 필드로 흡수한다. `placeId`는 도착지 판정에 쓰지 않으므로 싣지 않는다.
 */
data class PushMessage(
    val type: PushMessageType?,
    val title: String,
    val body: String,
    val imageUrl: String?,
    val targetId: String?,
)
