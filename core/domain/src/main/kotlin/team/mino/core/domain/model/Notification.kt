package team.mino.core.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 알림함에 실리는 알림 한 건 (`docs/specs/notifications/data-model.md` §1.1).
 *
 * **읽음 여부 필드가 없다** — spec FR-016이 그 상태 자체를 두지 않는다.
 *
 * @property id 서버 UUID. 목록의 키이자 탭 Intent가 싣는 유일한 값이다.
 * @property type 도착지와 썸네일 갈래를 정한다. 화면 문구를 정하는 데는 쓰지 않는다.
 * @property typeLabel 화면에 그대로 그리는 유형 문구. 서버가 완성해 준다 —
 *  공동방 참가 문구가 참가자 이름을 품는 가변 문구인데 `payload`에 그 이름이 없어 클라이언트가 조립할
 *  재료가 없다(`docs/specs/notifications/research.md` D4). **6종 문구를 클라이언트가 갖지 않는다.**
 * @property targetName 대상 이름(장소명 또는 공동방 이름). **저장 오류 행에서는 이름이 아니라 서버가 준
 *  고정 안내 문구가 온다** — 이름 자리로 보고 가공하면 안 된다(같은 data-model §1.1).
 * @property thumbnailUrl 서버가 고른 대표 이미지. `null`이면 화면이 플레이스홀더를 그린다.
 *  저장 오류는 이 값과 무관하게 고정 오류 아이콘을 쓴다(같은 research D5).
 * @property target 이동 대상 참조. 도착지 판정은 이 값 하나로 갈리며 네트워크 조회가 없다
 *  (`docs/specs/notifications/contracts/notification-repository.md` §2).
 * @property createdAt 발생 시각. 경과 시간 문구로 미리 바꾸지 않는다 — 언제를 기준으로 셀지는 목록을
 *  받은 화면이 정한다(spec EC-005 · 같은 research D12).
 */
@OptIn(ExperimentalTime::class)
data class Notification(
    val id: String,
    val type: NotificationType,
    val typeLabel: String,
    val targetName: String,
    val thumbnailUrl: String?,
    val target: NotificationTarget,
    val createdAt: Instant,
)
