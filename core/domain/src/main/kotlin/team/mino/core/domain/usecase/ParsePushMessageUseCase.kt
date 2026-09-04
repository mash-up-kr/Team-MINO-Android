package team.mino.core.domain.usecase

import team.mino.core.domain.model.PushMessage
import team.mino.core.domain.model.PushMessageType
import javax.inject.Inject

/**
 * FCM data 페이로드를 [PushMessage]로 옮긴다. 유형마다 다른 식별자 필드(`pinId`/`roomId`)를 단일 `targetId`로 흡수하고,
 * 모르는 `type`은 `null`로만 표현한다. 필수 필드 누락은 예외가 아니라 빈 문자열이다.
 */
class ParsePushMessageUseCase @Inject constructor() {
    operator fun invoke(data: Map<String, String>): PushMessage {
        val type = data[KEY_TYPE]?.let(::toMessageType)
        return PushMessage(
            type = type,
            title = data[KEY_TITLE].orEmpty(),
            body = data[KEY_BODY].orEmpty(),
            imageUrl = data[KEY_IMAGE_URL],
            targetId = type?.let { data[targetIdKeyOf(it)] },
        )
    }

    private fun toMessageType(raw: String): PushMessageType? = PushMessageType.entries.firstOrNull { it.name == raw }

    private fun targetIdKeyOf(type: PushMessageType): String? =
        when (type) {
            PushMessageType.PIN_DUPLICATED,
            PushMessageType.TOP_COMMENTED_PLACE,
            PushMessageType.NEARBY_PLACE,
            -> KEY_PIN_ID

            PushMessageType.ROOM_MEMBER_JOINED,
            PushMessageType.ROOM_JOINED_SELF,
            -> KEY_ROOM_ID

            PushMessageType.NEARBY_PLACE_SUMMARY,
            PushMessageType.SAVE_FAILED,
            -> null
        }

    private companion object {
        const val KEY_TYPE = "type"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_IMAGE_URL = "imageUrl"
        const val KEY_PIN_ID = "pinId"
        const val KEY_ROOM_ID = "roomId"
    }
}
