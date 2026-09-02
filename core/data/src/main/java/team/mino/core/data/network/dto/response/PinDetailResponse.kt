package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/pins/{pinId}` 핀 상세 응답 DTO.
 *
 * 방 상세 카드가 쓰는 [PinResponse]와 같은 엔드포인트를 보지만 **다른 표현이다.** 저 쪽은 목록
 * (`GET /api/v1/pins`)과 공유하는 축약형이고, 이 쪽은 장소 상세 화면이 요구하는 [sourceUrl]과
 * 닉네임·아바타까지 실린 [createdBy]를 담는다. 둘을 한 타입으로 합치면 목록 쪽이 쓰지도 않는 필드를
 * 지고 다니게 되므로 소비자별로 나눠 둔다.
 *
 * `{ "data": ... }` 봉투는 여기에 넣지 않는다 — `ApiService`가 [MinoResponse]로 벗긴다
 * (ADR `docs/adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md`).
 *
 * [place]는 `GET /api/v1/pins`와 완전히 같은 스키마여서 [PlaceResponse]를 그대로 재사용한다.
 * 화면이 읽지 않는 필드(`provider`·`city`·`category` 등)까지 담고 있지만, DTO는 서버 응답의 거울이고
 * 무엇을 도메인에 올릴지는 Mapper가 정한다 —
 * `docs/specs/place-detail/contracts/place-api.md` §1.2.
 *
 * [createdBy]가 nullable인 것은 서버 스키마가 `nullable: true`이기 때문이다. `null`이면 등록자 자리를
 * 기본 아바타로 그린다(EC-004).
 *
 * [sourceUrl]이 `null`이면 [원문보기]가 비활성이다(EC-017).
 *
 * [createdAt]은 ISO-8601 문자열이다. 도메인 `PlaceDetail`이 쓰지 않으나 서버 응답을 그대로 비춰 둔다.
 *
 * 계약은 `docs/specs/place-detail/contracts/place-api.md` §1이 소유한다.
 */
@Serializable
internal data class PinDetailResponse(
    val id: String,
    val roomId: String,
    val place: PlaceResponse,
    val images: List<String> = emptyList(),
    val createdBy: PinDetailCreatedByResponse? = null,
    val createdAt: String,
    val sourceUrl: String? = null,
)

/**
 * [PinDetailResponse.createdBy]의 서버 표현 — 핀을 저장한 멤버 프로필.
 *
 * 목록 응답의 `PinCreatedByResponse`가 `userId` 하나만 담는 것과 달리 닉네임·아바타까지 온다.
 * `nickname`은 헤더 첫 줄의 주인공이다(FR-005).
 *
 * [avatar]의 색은 문자열로 받는다. 이 엔드포인트는 `enum` 제약이 없고
 * `GET /pins/{pinId}/comments`의 같은 값에는 13색 `enum`이 걸려 있어 **서버 두 자리가 어긋나 있다**
 * (`docs/specs/place-detail/contracts/place-api.md` §1.3, 서버팀 협의 항목). DTO가 어느 한쪽으로
 * 좁히면 다른 쪽 응답에서 파싱이 깨지므로, 13색 팔레트 해석과 모르는 값의 `null` 처리는 Mapper가 한다.
 */
@Serializable
internal data class PinDetailCreatedByResponse(
    val userId: String,
    val nickname: String,
    val avatar: AvatarResponse? = null,
)
