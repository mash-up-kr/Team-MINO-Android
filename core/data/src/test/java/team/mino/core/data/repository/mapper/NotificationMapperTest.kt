@file:OptIn(ExperimentalTime::class)

package team.mino.core.data.repository.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.network.dto.response.NotificationPageResponse
import team.mino.core.data.network.dto.response.NotificationPayloadResponse
import team.mino.core.data.network.dto.response.NotificationResponse
import team.mino.core.data.network.dto.response.PaginationResponse
import team.mino.core.domain.model.NotificationTarget
import team.mino.core.domain.model.NotificationType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 이 매퍼가 지키는 것은 셋이다 — 행으로 그릴 수 없는 **항목만** 버리고 묶음 전체는 살리는 것
 * (`docs/specs/notifications/contracts/notification-repository.md` §1), 유형이 정하는 갈래대로 `payload`를
 * `NotificationTarget` 셋으로 흡수하는 것(`docs/specs/notifications/data-model.md` §1.3), 그리고 서버 enum
 * 대응표를 이 자리 하나에만 두는 것(같은 문서 §1.2)이다.
 *
 * `NotificationResponse.toDomainOrNull()`이 private이라 항목 단위 판정도 묶음을 통해 확인한다 —
 * 걸러내기가 실제로 일어나는 자리가 그곳이기도 하다.
 */
class NotificationMapperTest {
    @Test
    fun `알 수 없는 유형의 항목만 버리고 나머지는 남긴다`() {
        val page =
            pageResponse(
                items =
                    listOf(
                        notificationResponse(id = "n1", type = "NEARBY_PLACE", payload = pinPayload()),
                        notificationResponse(id = "n2", type = "PLACE_SHARED_BY_FRIEND", payload = pinPayload()),
                        notificationResponse(id = "n3", type = "SAVE_FAILED", payload = null),
                    ),
            ).toDomain()

        assertEquals(listOf("n1", "n3"), page.items.map { it.id })
    }

    @Test
    fun `버린 항목이 있어도 hasNext는 서버가 준 값 그대로다`() {
        val page =
            pageResponse(
                items = listOf(notificationResponse(type = "UNKNOWN_TYPE")),
                hasNext = true,
            ).toDomain()

        assertTrue(page.items.isEmpty())
        assertTrue(page.hasNext)
    }

    @Test
    fun `서버가 준 순서를 뒤집지 않는다`() {
        val page =
            pageResponse(
                items =
                    listOf(
                        notificationResponse(id = "n1", createdAt = "2026-09-01T10:00:00Z"),
                        notificationResponse(id = "n2", createdAt = "2026-09-01T12:00:00Z"),
                    ),
            ).toDomain()

        assertEquals(listOf("n1", "n2"), page.items.map { it.id })
    }

    @Test
    fun `장소 대상 유형은 payload의 pinId를 Pin으로 흡수한다`() {
        val notification = single(type = "NEARBY_PLACE", payload = NotificationPayloadResponse(pinId = "pin-1"))

        assertEquals(NotificationTarget.Pin("pin-1"), notification.target)
    }

    @Test
    fun `공동방 유형은 payload의 roomId를 Room으로 흡수한다`() {
        val notification =
            single(type = "ROOM_MEMBER_JOINED", payload = NotificationPayloadResponse(roomId = "room-1"))

        assertEquals(NotificationTarget.Room("room-1"), notification.target)
    }

    @Test
    fun `저장 오류는 payload가 없어도 None이다`() {
        val notification = single(type = "SAVE_FAILED", payload = null)

        assertEquals(NotificationTarget.None, notification.target)
    }

    @Test
    fun `저장 오류는 서버가 payload를 실어 보내도 None이다`() {
        val notification = single(type = "SAVE_FAILED", payload = NotificationPayloadResponse(pinId = "pin-1"))

        assertEquals(NotificationTarget.None, notification.target)
    }

    @Test
    fun `서버 PIN_DUPLICATED를 도메인 PLACE_DUPLICATED로 읽는다`() {
        val notification = single(type = "PIN_DUPLICATED", payload = pinPayload())

        assertEquals(NotificationType.PLACE_DUPLICATED, notification.type)
        assertEquals(NotificationTarget.Pin("pin-1"), notification.target)
    }

    @Test
    fun `도메인 이름 PLACE_DUPLICATED는 서버 enum이 아니어서 버린다`() {
        val page = pageResponse(items = listOf(notificationResponse(type = "PLACE_DUPLICATED"))).toDomain()

        assertTrue(page.items.isEmpty())
    }

    @Test
    fun `이름이 같은 나머지 5종은 그대로 읽는다`() {
        val types =
            listOf(
                "SAVE_FAILED" to NotificationType.SAVE_FAILED,
                "NEARBY_PLACE" to NotificationType.NEARBY_PLACE,
                "TOP_COMMENTED_PLACE" to NotificationType.TOP_COMMENTED_PLACE,
                "ROOM_MEMBER_JOINED" to NotificationType.ROOM_MEMBER_JOINED,
                "ROOM_JOINED_SELF" to NotificationType.ROOM_JOINED_SELF,
            )

        types.forEach { (serverType, expected) ->
            assertEquals(expected, single(type = serverType, payload = fullPayload()).type)
        }
    }

    @Test
    fun `장소 대상 유형에 payload가 없으면 그 항목을 버린다`() {
        val page =
            pageResponse(items = listOf(notificationResponse(type = "NEARBY_PLACE", payload = null))).toDomain()

        assertTrue(page.items.isEmpty())
    }

    @Test
    fun `장소 대상 유형에 pinId가 없으면 그 항목을 버린다`() {
        val page =
            pageResponse(
                items =
                    listOf(
                        notificationResponse(
                            type = "TOP_COMMENTED_PLACE",
                            payload = NotificationPayloadResponse(roomId = "room-1"),
                        ),
                    ),
            ).toDomain()

        assertTrue(page.items.isEmpty())
    }

    @Test
    fun `공동방 유형에 roomId가 없으면 그 항목을 버린다`() {
        val page =
            pageResponse(
                items =
                    listOf(
                        notificationResponse(
                            type = "ROOM_JOINED_SELF",
                            payload = NotificationPayloadResponse(pinId = "pin-1"),
                        ),
                    ),
            ).toDomain()

        assertTrue(page.items.isEmpty())
    }

    @Test
    fun `대상 식별자가 빈 항목을 버려도 나머지는 남긴다`() {
        val page =
            pageResponse(
                items =
                    listOf(
                        notificationResponse(id = "n1", type = "NEARBY_PLACE", payload = null),
                        notificationResponse(id = "n2", type = "NEARBY_PLACE", payload = pinPayload()),
                    ),
            ).toDomain()

        assertEquals(listOf("n2"), page.items.map { it.id })
    }

    @Test
    fun `typeLabel은 서버 문구를 그대로 싣는다`() {
        val notification = single(typeLabel = "지은님이 참가했어요")

        assertEquals("지은님이 참가했어요", notification.typeLabel)
    }

    @Test
    fun `createdAt을 절대 시각 그대로 옮긴다`() {
        val notification = single(createdAt = "2026-09-01T12:34:56Z")

        assertEquals(Instant.parse("2026-09-01T12:34:56Z"), notification.createdAt)
    }

    @Test
    fun `thumbnailUrl이 없으면 null을 메우지 않는다`() {
        assertNull(single(thumbnailUrl = null).thumbnailUrl)
    }

    private fun single(
        type: String = "NEARBY_PLACE",
        typeLabel: String = "근처에 저장한 장소가 있어요",
        thumbnailUrl: String? = "https://cdn.mino.team/thumb.jpg",
        payload: NotificationPayloadResponse? = fullPayload(),
        createdAt: String = "2026-09-01T12:00:00Z",
    ) = pageResponse(
        items =
            listOf(
                notificationResponse(
                    type = type,
                    typeLabel = typeLabel,
                    thumbnailUrl = thumbnailUrl,
                    payload = payload,
                    createdAt = createdAt,
                ),
            ),
    ).toDomain().items.single()

    private fun pinPayload() = NotificationPayloadResponse(pinId = "pin-1")

    private fun fullPayload() = NotificationPayloadResponse(pinId = "pin-1", roomId = "room-1")

    private fun pageResponse(
        items: List<NotificationResponse> = listOf(notificationResponse()),
        hasNext: Boolean = false,
    ): NotificationPageResponse =
        NotificationPageResponse(
            data = items,
            pagination = PaginationResponse(page = 0, pageSize = 20, hasNext = hasNext),
        )

    private fun notificationResponse(
        id: String = "n1",
        type: String = "NEARBY_PLACE",
        typeLabel: String = "근처에 저장한 장소가 있어요",
        targetName: String = "성수동 카페",
        thumbnailUrl: String? = "https://cdn.mino.team/thumb.jpg",
        payload: NotificationPayloadResponse? = fullPayload(),
        createdAt: String = "2026-09-01T12:00:00Z",
    ): NotificationResponse =
        NotificationResponse(
            id = id,
            type = type,
            typeLabel = typeLabel,
            targetName = targetName,
            thumbnailUrl = thumbnailUrl,
            payload = payload,
            createdAt = createdAt,
        )
}
