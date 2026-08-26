package team.mino.core.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 방(개인방/공동방) 도메인 모델.
 *
 * room-list(목록 조회)와 group-room-form(생성·편집)이 각자 독립적으로 만들었다가 develop 병합
 * 과정에서 합쳐졌다 — 필드는 두 소비처의 합집합이다.
 *
 * - **검증 규칙**: `isPersonal == true`인 [Room]은 사용자당 최대 1개다(개인방은 하나, PRD 「개인방」정의).
 *   `placeCount == 0`이면 `lastPlaceSavedAt == null`이다.
 * - **관계**: `Room` 1 — N `Place`(다른 spec 소유). 이 모델은 `Place` 자체를 담지 않고
 *   [placeCount]·[lastPlaceSavedAt]·[commentCount]로 집계값만 소비한다.
 * - [description]은 nullable이 아니다. 설명이 없는 방은 빈 문자열을 갖고, 서버가 내려준 `null`은
 *   Mapper가 흡수한다.
 * - [color]도 nullable이 아니다. 사용자가 색을 고르지 않았더라도 저장된 방은 이미 [RoomColor.GRAY]로
 *   확정된 상태다 — `docs/adr/2026-08-14-room-color-palette-in-design-system.md`.
 */
@OptIn(ExperimentalTime::class)
data class Room(
    val id: String,
    val name: String,
    val description: String,
    val color: RoomColor,
    val ownerId: String,
    val isPersonal: Boolean,
    val placeCount: Int,
    val thumbnail: RoomThumbnail,
    val memberSummary: RoomMemberSummary,
    val lastPlaceSavedAt: Instant?,
    val commentCount: Int,
)
