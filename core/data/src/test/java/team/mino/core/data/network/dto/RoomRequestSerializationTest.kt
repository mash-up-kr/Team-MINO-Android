package team.mino.core.data.network.dto

import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.network.dto.request.RoomRequest

/**
 * 요청 본문에 `"description": null`이 실제로 실리는지 확인한다.
 *
 * `RoomRequest.description`에 기본값이 있으면 `encodeDefaults = false`(kotlinx의 기본값)가 그 필드를 본문에서
 * 통째로 빼고, PATCH에서 빠진 필드는 "건드리지 않았다"로 읽혀 **설명을 지운 변경이 조용히 사라진다**
 * — contracts/room-api.md §5 · research.md R-027.
 *
 * mock 구간에는 직렬화 자체가 없어 이 결함이 드러날 수 없었다. 실기기가 아니라 이 테스트가 잡는다.
 */
class RoomRequestSerializationTest {
    /** `NetworkModule.provideHttpClient`의 ContentNegotiation 설정과 같은 값이다. 여기서 옵션을 더 켜면 테스트가 거짓 통과한다. */
    private val json = Json { ignoreUnknownKeys = true }

    private fun encodedObject(request: RoomRequest): JsonObject =
        json.parseToJsonElement(json.encodeToString(request)) as JsonObject

    @Test
    fun `설명이 없으면 description 키가 null 값으로 본문에 실린다`() {
        val body = encodedObject(RoomRequest(name = "맛집 탐방", description = null, color = "gray"))

        assertTrue(
            "description 키가 본문에서 빠졌다. PATCH에서 이 누락은 '설명을 건드리지 않았다'로 읽혀 삭제가 사라진다. 본문=$body",
            body.containsKey("description"),
        )
        assertEquals(JsonNull, body["description"])
    }

    @Test
    fun `설명이 있으면 그대로 실린다`() {
        val body = encodedObject(RoomRequest(name = "맛집 탐방", description = "동네 맛집", color = "blue"))

        assertEquals(JsonPrimitive("동네 맛집"), body["description"])
    }

    @Test
    fun `본문은 선언된 모든 필드를 하나도 빠뜨리지 않고 담는다`() {
        val body = encodedObject(RoomRequest(name = "맛집 탐방", description = null, color = "gray"))

        // 키 집합을 손으로 적으면 기본값을 단 새 필드가 본문에서 빠져도 이 단언이 통과한다 —
        // R-027이 진단한 결함이 그대로 재발한다. 선언부를 출처로 삼아야 그때 여기서 걸린다.
        val declaredFields =
            RoomRequest
                .serializer()
                .descriptor
                .elementNames
                .toSet()

        assertEquals(declaredFields, body.keys)
        assertEquals(JsonPrimitive("맛집 탐방"), body["name"])
        assertEquals(JsonPrimitive("gray"), body["color"])
    }
}
