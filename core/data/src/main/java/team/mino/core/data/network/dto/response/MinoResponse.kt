package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 서버가 모든 성공 응답을 감싸는 `{ "data": ... }` 봉투.
 *
 * 봉투는 데이터가 아니라 전송 형식이므로 `ApiService`가 `body<MinoResponse<T>>().data`로 벗기고,
 * `DataSource` 위 레이어는 알맹이만 본다. 엔드포인트별 래퍼 DTO를 따로 만들지 않는다.
 * 본문 스키마가 없는 응답(예: 202)에는 쓰지 않는다.
 *
 * 결정 배경은 ADR `docs/adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md` 참조.
 */
@Serializable
internal data class MinoResponse<T>(
    val data: T,
)
