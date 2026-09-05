package team.mino.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import team.mino.core.domain.model.PushMessageType

/**
 * FCM data 페이로드(`Map<String, String>`)를 [team.mino.core.domain.model.PushMessage]로 옮기는 규칙을 본다
 * (`contracts/push-payload-contract.md` §1·§4 · `data-model.md` §2 · EC-008).
 *
 * 판정하는 것은 **입력 맵 → 반환 모델의 다섯 필드**뿐이다. 유형별 식별자 필드가 `pinId`/`roomId`로 갈리는 것을
 * 단일 `targetId`로 흡수하는지, 모르는 `type`을 `null`로 표현하는지, 누락 필드를 예외 없이 채우는지를 본다.
 * `placeId`는 모델에 없으므로 어디에도 실리지 않았다는 것만 확인한다.
 */
class ParsePushMessageUseCaseTest {
    private val parsePushMessage = ParsePushMessageUseCase()

    @Test
    fun `PIN_DUPLICATED는 pinId를 targetId로 삼고 placeId는 버린다`() {
        val message = parsePushMessage(placePayload("PIN_DUPLICATED"))

        assertEquals(PushMessageType.PIN_DUPLICATED, message.type)
        assertEquals(PIN_ID, message.targetId)
        assertEquals("패스트리 순간", message.title)
        assertEquals("이미 저장해둔 곳이에요", message.body)
        assertEquals(IMAGE_URL, message.imageUrl)
    }

    @Test
    fun `TOP_COMMENTED_PLACE는 pinId를 targetId로 삼는다`() {
        val message = parsePushMessage(placePayload("TOP_COMMENTED_PLACE"))

        assertEquals(PushMessageType.TOP_COMMENTED_PLACE, message.type)
        assertEquals(PIN_ID, message.targetId)
        assertEquals(IMAGE_URL, message.imageUrl)
    }

    @Test
    fun `NEARBY_PLACE는 pinId를 targetId로 삼는다`() {
        val message = parsePushMessage(placePayload("NEARBY_PLACE"))

        assertEquals(PushMessageType.NEARBY_PLACE, message.type)
        assertEquals(PIN_ID, message.targetId)
        assertEquals(IMAGE_URL, message.imageUrl)
    }

    @Test
    fun `장소 대상 유형에서 placeId만 오고 pinId가 없으면 targetId는 null이다`() {
        // D2 — 도착지는 pinId로만 정한다. placeId가 있어도 targetId로 대체하지 않는다.
        val message =
            parsePushMessage(
                mapOf(
                    "type" to "PIN_DUPLICATED",
                    "placeId" to PLACE_ID,
                    "title" to "패스트리 순간",
                    "body" to "이미 저장해둔 곳이에요",
                ),
            )

        assertEquals(PushMessageType.PIN_DUPLICATED, message.type)
        assertNull(message.targetId)
    }

    @Test
    fun `ROOM_MEMBER_JOINED는 roomId를 targetId로 삼는다`() {
        val message = parsePushMessage(roomPayload("ROOM_MEMBER_JOINED"))

        assertEquals(PushMessageType.ROOM_MEMBER_JOINED, message.type)
        assertEquals(ROOM_ID, message.targetId)
        assertEquals("성수 맛집 탐방", message.title)
        assertEquals("지연님이 들어왔어요", message.body)
        assertNull(message.imageUrl)
    }

    @Test
    fun `ROOM_JOINED_SELF는 roomId를 targetId로 삼는다`() {
        val message = parsePushMessage(roomPayload("ROOM_JOINED_SELF"))

        assertEquals(PushMessageType.ROOM_JOINED_SELF, message.type)
        assertEquals(ROOM_ID, message.targetId)
        assertNull(message.imageUrl)
    }

    @Test
    fun `SAVE_FAILED는 targetId와 imageUrl이 null이다`() {
        val message =
            parsePushMessage(
                mapOf(
                    "type" to "SAVE_FAILED",
                    "title" to "잠시 후 다시 시도해주세요",
                    "body" to "장소를 저장하지 못했어요.",
                ),
            )

        assertEquals(PushMessageType.SAVE_FAILED, message.type)
        assertNull(message.targetId)
        assertNull(message.imageUrl)
        assertEquals("잠시 후 다시 시도해주세요", message.title)
        assertEquals("장소를 저장하지 못했어요.", message.body)
    }

    @Test
    fun `NEARBY_PLACE_SUMMARY는 targetId와 imageUrl이 null이다`() {
        val message =
            parsePushMessage(
                mapOf(
                    "type" to "NEARBY_PLACE_SUMMARY",
                    "title" to "근처에 저장한 곳 3개가 있어요",
                    "body" to "반경 3km",
                ),
            )

        assertEquals(PushMessageType.NEARBY_PLACE_SUMMARY, message.type)
        assertNull(message.targetId)
        assertNull(message.imageUrl)
    }

    @Test
    fun `모르는 type 문자열이면 type은 null이다`() {
        // EC-008 · 계약 §4 — 별도 UNKNOWN 멤버 없이 null로만 표현한다.
        val message =
            parsePushMessage(
                mapOf(
                    "type" to "SOMETHING_NEW",
                    "title" to "제목",
                    "body" to "본문",
                ),
            )

        assertNull(message.type)
    }

    @Test
    fun `type 필드 자체가 없으면 type은 null이다`() {
        val message = parsePushMessage(mapOf("title" to "제목", "body" to "본문"))

        assertNull(message.type)
    }

    @Test
    fun `title이 없으면 빈 문자열로 채운다`() {
        // data-model §2 — 필수 필드 누락은 예외가 아니라 빈 문자열이다.
        val message = parsePushMessage(mapOf("type" to "SAVE_FAILED", "body" to "본문"))

        assertEquals("", message.title)
        assertEquals("본문", message.body)
    }

    @Test
    fun `body가 없으면 빈 문자열로 채운다`() {
        val message = parsePushMessage(mapOf("type" to "SAVE_FAILED", "title" to "제목"))

        assertEquals("제목", message.title)
        assertEquals("", message.body)
    }

    @Test
    fun `imageUrl이 없으면 null이다`() {
        val message =
            parsePushMessage(
                mapOf(
                    "type" to "PIN_DUPLICATED",
                    "placeId" to PLACE_ID,
                    "pinId" to PIN_ID,
                    "title" to "제목",
                    "body" to "본문",
                ),
            )

        assertNull(message.imageUrl)
    }

    @Test
    fun `빈 맵이면 예외 없이 type null·빈 문자열·null로 채운다`() {
        val message = parsePushMessage(emptyMap())

        assertNull(message.type)
        assertEquals("", message.title)
        assertEquals("", message.body)
        assertNull(message.imageUrl)
        assertNull(message.targetId)
    }

    private fun placePayload(type: String): Map<String, String> =
        mapOf(
            "type" to type,
            "placeId" to PLACE_ID,
            "pinId" to PIN_ID,
            "title" to "패스트리 순간",
            "body" to "이미 저장해둔 곳이에요",
            "imageUrl" to IMAGE_URL,
        )

    private fun roomPayload(type: String): Map<String, String> =
        mapOf(
            "type" to type,
            "roomId" to ROOM_ID,
            "title" to "성수 맛집 탐방",
            "body" to "지연님이 들어왔어요",
        )

    private companion object {
        const val PLACE_ID = "place-uuid"
        const val PIN_ID = "pin-uuid"
        const val ROOM_ID = "4c1d8e20-7b93-4a6f-9e52-0d3fa8b61c47"
        const val IMAGE_URL = "https://cdn.example/place.jpg"
    }
}
