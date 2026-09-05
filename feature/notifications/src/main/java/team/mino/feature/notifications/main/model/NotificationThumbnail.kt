package team.mino.feature.notifications.main.model

import androidx.compose.runtime.Immutable

/**
 * 알림 행 좌측에 무엇을 그릴지 정해진 결과
 * (`docs/specs/notifications/data-model.md` §2.2, spec FR-012).
 *
 * **갈래는 둘뿐이다.** 유형 6종 중 저장 오류만 고정 아이콘이고, 나머지 다섯은 대상이 장소든 방이든 서버가 준
 * 이미지 한 장을 그대로 쓴다 — 무엇을 대표 이미지로 삼을지는 서버가 정하므로 클라이언트가 방 목록을 불러
 * 합성하지 않는다(`docs/specs/notifications/research.md` D5).
 *
 * `SaveError`를 [Image]`(null)`로 합치지 않는다. 저장 오류는 **고정 오류 아이콘**을, 나머지의 `null`은
 * **플레이스홀더**를 그려 서로 다른 그림이다.
 *
 * 화면 상태가 들고 컴포저블로 흘러가므로 [Immutable]을 붙인다 — sealed interface는 그대로 두면 Compose가
 * 불안정으로 보고 리컴포지션을 건너뛰지 못한다.
 */
@Immutable
internal sealed interface NotificationThumbnail {
    /**
     * 서버가 알림과 함께 준 이미지.
     *
     * @property url 서버가 주지 않았으면 `null`이며, 그 자리에는 플레이스홀더가 놓인다(spec TS-054).
     */
    data class Image(val url: String?) : NotificationThumbnail

    /** 저장 오류 알림의 고정 오류 아이콘(spec TS-010). */
    data object SaveError : NotificationThumbnail
}
