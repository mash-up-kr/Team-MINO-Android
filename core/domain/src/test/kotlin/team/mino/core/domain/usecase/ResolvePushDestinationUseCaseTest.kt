package team.mino.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.domain.model.PushDestination
import team.mino.core.domain.model.PushMessage
import team.mino.core.domain.model.PushMessageType

/**
 * 파싱된 [PushMessage]에서 알림 탭 시 **열 도착지**를 정하는 규칙을 본다
 * (`data-model.md` §3 라우팅 표 · FR-009 · FR-012 · FR-013 · EC-009).
 *
 * 판정하는 것은 **[PushMessage] → [PushDestination]** 뿐이다. `type == null`(EC-008)은 호출자가 걸러
 * 이 함수에 도달하지 않으므로 케이스로 두지 않는다 — 여기서 그 갈래를 정하면 호출자가 보지 않기로 한 책임을
 * 이 함수에 떠넘기게 된다.
 */
class ResolvePushDestinationUseCaseTest {
    private val resolvePushDestination = ResolvePushDestinationUseCase()

    @Test
    fun `PIN_DUPLICATED에 targetId가 있으면 그 pinId의 장소 상세를 연다`() {
        // FR-009 · FR-013
        val destination = resolvePushDestination(message(PushMessageType.PIN_DUPLICATED, PIN_ID))

        assertEquals(PushDestination.PlaceDetail(PIN_ID), destination)
    }

    @Test
    fun `TOP_COMMENTED_PLACE에 targetId가 있으면 그 pinId의 장소 상세를 연다`() {
        val destination = resolvePushDestination(message(PushMessageType.TOP_COMMENTED_PLACE, PIN_ID))

        assertEquals(PushDestination.PlaceDetail(PIN_ID), destination)
    }

    @Test
    fun `NEARBY_PLACE에 targetId가 있으면 그 pinId의 장소 상세를 연다`() {
        val destination = resolvePushDestination(message(PushMessageType.NEARBY_PLACE, PIN_ID))

        assertEquals(PushDestination.PlaceDetail(PIN_ID), destination)
    }

    @Test
    fun `ROOM_MEMBER_JOINED에 targetId가 있으면 그 roomId의 공동방 상세를 연다`() {
        val destination = resolvePushDestination(message(PushMessageType.ROOM_MEMBER_JOINED, ROOM_ID))

        assertEquals(PushDestination.RoomDetail(ROOM_ID), destination)
    }

    @Test
    fun `ROOM_JOINED_SELF에 targetId가 있으면 그 roomId의 공동방 상세를 연다`() {
        val destination = resolvePushDestination(message(PushMessageType.ROOM_JOINED_SELF, ROOM_ID))

        assertEquals(PushDestination.RoomDetail(ROOM_ID), destination)
    }

    @Test
    fun `장소 대상 유형에 targetId가 null이면 알림 탭으로 떨어진다`() {
        // EC-009 — 해석 불가한 식별자는 상세로 보내지 않는다.
        listOf(
            PushMessageType.PIN_DUPLICATED,
            PushMessageType.TOP_COMMENTED_PLACE,
            PushMessageType.NEARBY_PLACE,
        ).forEach { type ->
            assertEquals(type.name, PushDestination.NotificationTab, resolvePushDestination(message(type, null)))
        }
    }

    @Test
    fun `장소 대상 유형에 targetId가 빈 문자열이면 알림 탭으로 떨어진다`() {
        // EC-009
        listOf(
            PushMessageType.PIN_DUPLICATED,
            PushMessageType.TOP_COMMENTED_PLACE,
            PushMessageType.NEARBY_PLACE,
        ).forEach { type ->
            assertEquals(type.name, PushDestination.NotificationTab, resolvePushDestination(message(type, "")))
        }
    }

    @Test
    fun `공동방 대상 유형에 targetId가 null이면 알림 탭으로 떨어진다`() {
        // EC-009
        listOf(
            PushMessageType.ROOM_MEMBER_JOINED,
            PushMessageType.ROOM_JOINED_SELF,
        ).forEach { type ->
            assertEquals(type.name, PushDestination.NotificationTab, resolvePushDestination(message(type, null)))
        }
    }

    @Test
    fun `공동방 대상 유형에 targetId가 빈 문자열이면 알림 탭으로 떨어진다`() {
        // EC-009
        listOf(
            PushMessageType.ROOM_MEMBER_JOINED,
            PushMessageType.ROOM_JOINED_SELF,
        ).forEach { type ->
            assertEquals(type.name, PushDestination.NotificationTab, resolvePushDestination(message(type, "")))
        }
    }

    @Test
    fun `SAVE_FAILED는 알림 탭을 연다`() {
        // FR-009 — targetId 없음이 정상이다.
        val destination = resolvePushDestination(message(PushMessageType.SAVE_FAILED, null))

        assertEquals(PushDestination.NotificationTab, destination)
    }

    @Test
    fun `NEARBY_PLACE_SUMMARY는 알림 탭을 연다`() {
        // FR-012 — US4의 근거. 여러 장소를 하나로 묶은 대표 알림은 특정 장소로 보내지 않는다.
        val destination = resolvePushDestination(message(PushMessageType.NEARBY_PLACE_SUMMARY, null))

        assertEquals(PushDestination.NotificationTab, destination)
    }

    private fun message(
        type: PushMessageType,
        targetId: String?,
    ): PushMessage =
        PushMessage(
            type = type,
            title = "제목",
            body = "본문",
            imageUrl = null,
            targetId = targetId,
        )

    private companion object {
        const val PIN_ID = "pin-uuid"
        const val ROOM_ID = "4c1d8e20-7b93-4a6f-9e52-0d3fa8b61c47"
    }
}
