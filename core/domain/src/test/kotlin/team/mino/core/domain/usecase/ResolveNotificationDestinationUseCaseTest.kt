@file:OptIn(ExperimentalTime::class)

package team.mino.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.domain.model.Notification
import team.mino.core.domain.model.NotificationDestination
import team.mino.core.domain.model.NotificationTarget
import team.mino.core.domain.model.NotificationType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 알림 하나가 어느 화면으로 가는지를 본다
 * (`docs/specs/notifications/contracts/notification-repository.md` §2 판정 표 · FR-005 · FR-022).
 *
 * 판정하는 것은 **[Notification.target] → [NotificationDestination]** 뿐이다. 세 갈래를 각각 세우고,
 * 유형이 도착지를 흔들지 않는다는 것을 대상별 유형 전량으로 확인한다
 * (`docs/specs/notifications/data-model.md` §1.2 대상 열 · §1.3 · §1.5).
 *
 * 「대상이 아직 살아 있는가」는 케이스로 두지 않는다 — spec 7.0.0 UX-006이 그 판정을 도착지 화면으로
 * 옮겨 [NotificationDestination]에 `Unreachable` 갈래가 없다.
 */
class ResolveNotificationDestinationUseCaseTest {
    private val resolveNotificationDestination = ResolveNotificationDestinationUseCase()

    @Test
    fun `핀 대상이면 그 pinId의 장소 상세를 연다`() {
        // 계약 §2 판정 표 — Pin(pinId) → PlaceDetail(pinId). FR-022
        val destination = resolveNotificationDestination(
            notification(NotificationTarget.Pin(PIN_ID), NotificationType.PLACE_DUPLICATED),
        )

        assertEquals(NotificationDestination.PlaceDetail(PIN_ID), destination)
    }

    @Test
    fun `방 대상이면 그 roomId의 방 상세를 연다`() {
        // 계약 §2 판정 표 — Room(roomId) → RoomDetail(roomId)
        val destination = resolveNotificationDestination(
            notification(NotificationTarget.Room(ROOM_ID), NotificationType.ROOM_MEMBER_JOINED),
        )

        assertEquals(NotificationDestination.RoomDetail(ROOM_ID), destination)
    }

    @Test
    fun `대상이 없으면 저장 오류 안내로 간다`() {
        // 계약 §2 판정 표 — None → SaveErrorGuide. FR-010
        val destination = resolveNotificationDestination(
            notification(NotificationTarget.None, NotificationType.SAVE_FAILED),
        )

        assertEquals(NotificationDestination.SaveErrorGuide, destination)
    }

    @Test
    fun `장소 대상 3종은 유형과 무관하게 같은 장소 상세로 간다`() {
        // data-model §1.2 「대상 = 핀」인 유형 전량. 유형은 도착지 분기 조건이 아니다(계약 §2).
        listOf(
            NotificationType.PLACE_DUPLICATED,
            NotificationType.NEARBY_PLACE,
            NotificationType.TOP_COMMENTED_PLACE,
        ).forEach { type ->
            val destination = resolveNotificationDestination(
                notification(NotificationTarget.Pin(PIN_ID), type),
            )

            assertEquals(type.name, NotificationDestination.PlaceDetail(PIN_ID), destination)
        }
    }

    @Test
    fun `공동방 참가 2종은 유형과 무관하게 같은 방 상세로 간다`() {
        // data-model §1.2 「대상 = 방」인 유형 전량.
        listOf(
            NotificationType.ROOM_MEMBER_JOINED,
            NotificationType.ROOM_JOINED_SELF,
        ).forEach { type ->
            val destination = resolveNotificationDestination(
                notification(NotificationTarget.Room(ROOM_ID), type),
            )

            assertEquals(type.name, NotificationDestination.RoomDetail(ROOM_ID), destination)
        }
    }

    private fun notification(
        target: NotificationTarget,
        type: NotificationType,
    ): Notification =
        Notification(
            id = "b0f3a5d1-2c47-4e88-9a10-6d5e7f2b3c94",
            type = type,
            typeLabel = "유형 문구",
            targetName = "대상 이름",
            thumbnailUrl = null,
            target = target,
            createdAt = Instant.fromEpochSeconds(1_756_000_000),
        )

    private companion object {
        const val PIN_ID = "pin-uuid"
        const val ROOM_ID = "4c1d8e20-7b93-4a6f-9e52-0d3fa8b61c47"
    }
}
