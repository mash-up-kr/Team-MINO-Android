package team.mino.core.data.datasource.mock

import kotlinx.coroutines.delay
import team.mino.core.data.network.dto.response.CardAvatarResponse
import team.mino.core.data.network.dto.response.CardCreatedByResponse
import team.mino.core.data.network.dto.response.CardPlaceResponse
import team.mino.core.data.network.dto.response.CardResponse
import team.mino.core.domain.model.DeckSort
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException
import javax.inject.Inject

/**
 * `GET /api/v1/rooms/{roomId}/cards`가 배포될 때까지 그 응답을 대신하는 원천. 계약의 소유자는
 * `docs/specs/home-deck-exploration/contracts/deck-api.md` §2·§4다.
 *
 * **계약을 지어내지 않는다.** 응답 형태·`labelGroup` 값·10장 상한·짧은 덱·순서 유지를 서버 PR Node#94가 확정한
 * 실제 계약에 맞췄으므로, 실서버로 바뀌어도 Mapper와 호출부는 그대로다.
 *
 * `RoomMockStore`와 달리 상태를 갖지 않아 `@Singleton`도 `Mutex`도 두지 않는다 — 이 mock은 읽기 전용이고
 * 카드를 만들거나 고치는 계약이 없다. 방 목록은 이미 실서버(`RoomListRemoteDataSource`)에서 오므로 고정된
 * `roomId`로 시드를 찾지 못하고, 아래 [roomDeckOf]가 `roomId`를 프로필 하나에 고정 배정한다.
 *
 * 실패를 이 클래스가 직접 도메인 예외로 던지는 이유는 mock이 `HttpClient`의 전역 매핑
 * (`convertDomainException`)을 타지 않기 때문이다. 실서버로 바뀌면 그 자리를 매핑이 대신하므로,
 * 위 계약 §4의 전환 지점 ①에서 이 파일이 `DeckMockRemoteDataSourceImpl`과 함께 통째로 빠진다.
 */
internal class DeckMockStore @Inject constructor() {
    suspend fun getCards(
        roomId: String,
        sort: DeckSort,
        lat: Double?,
        lng: Double?,
    ): List<CardResponse> {
        // 계약 §2.1의 400을 그대로 재현한다. 정상 경로에서는 Repository가 막아 닿지 않지만,
        // 계약을 어긴 호출을 드러내는 안전망으로 남긴다.
        if (sort == DeckSort.NEAREST && (lat == null || lng == null)) {
            throw MinoDomainException.Http(
                HTTP_BAD_REQUEST,
                IOException("mock: sort=nearby requires lat/lng"),
            )
        }
        delay(LATENCY_MILLIS)
        // 서버가 자르는 자리다. 이 상한이 여기 있으므로 Repository·Mapper는 다시 자르지 않는다(FR-004).
        return roomDeckOf(roomId).cardsOf(sort).take(MAX_DECK_SIZE)
    }

    private companion object {
        const val LATENCY_MILLIS = 600L

        const val MAX_DECK_SIZE = 10

        const val HTTP_BAD_REQUEST = 400
    }
}

/** 방 하나가 정렬 3종에 대해 내려주는 후보. 세 정렬이 같은 카드를 나눠 갖는 것이 정상이다(spec §4 가정 — 중복 허용). */
private class MockRoomDeck(
    val ggukPick: List<CardResponse>,
    val latest: List<CardResponse>,
    val nearest: List<CardResponse>,
) {
    fun cardsOf(sort: DeckSort): List<CardResponse> =
        when (sort) {
            DeckSort.GGUK_PICK -> ggukPick
            DeckSort.LATEST -> latest
            DeckSort.NEAREST -> nearest
        }
}

/**
 * 등록자. **`null`인 자리를 일부러 남긴다** — 계약상 `createdBy` 자체가 없을 수 있고(탈퇴 등),
 * 아바타를 고르지 않은 사용자도 있다. 두 부재를 메우는 것은 `DeckMapper`의 몫이라 mock이 미리 채우지 않는다.
 */
private val MOCK_REGISTRANTS: List<CardCreatedByResponse?> =
    listOf(
        CardCreatedByResponse(userId = "mock-user-1", nickname = "구구", avatar = CardAvatarResponse(id = 1)),
        CardCreatedByResponse(userId = "mock-user-2", nickname = "민초", avatar = CardAvatarResponse(id = 4)),
        CardCreatedByResponse(userId = "mock-user-3", nickname = "아직 아바타 없음", avatar = null),
        null,
    )

/**
 * 카드가 넉넉한 방.
 *
 * - `ggukPick` — **10장이고 `labelGroup` 4종이 모두 섞여 있다**(TS-014). 라벨별로 묶지 않은 순서 그대로다.
 * - `latest` — 같은 카드를 역순으로 돌려준다. **정렬 간 후보가 겹치는 경우**로, 홈이 덱 사이 중복을 지우지 않음을 드러낸다.
 * - `nearest` — 3장. **10장 미만인 덱**(TS-005)이다.
 */
private val MIXED_ROOM_CARDS: List<CardResponse> =
    listOf(
        mockCard(1, "연남동 감자탕", "서울 마포구 연남로 21", LABEL_WORTH_VISITING),
        mockCard(2, "성수 로스터리", "서울 성동구 연무장길 45", LABEL_MANY_SAVES),
        mockCard(3, "망원시장 분식", "서울 마포구 포은로8길 14", LABEL_MANY_COMMENTS),
        mockCard(4, "한강뷰 루프탑", "서울 용산구 이촌로 302", LABEL_MANY_VIEWS),
        mockCard(5, "을지로 노포 호프", "서울 중구 을지로11길 5", LABEL_WORTH_VISITING),
        mockCard(6, "연희동 베이커리", "서울 서대문구 연희로11가길 39", LABEL_MANY_SAVES),
        mockCard(7, "서촌 국숫집", "서울 종로구 자하문로7길 24", LABEL_MANY_COMMENTS, imageCount = 0),
        mockCard(8, "후암동 사진관", "서울 용산구 두텁바위로1길 41", LABEL_MANY_VIEWS),
        mockCard(9, "잠실 야장", "서울 송파구 백제고분로7길 30", LABEL_WORTH_VISITING, imageCount = 1),
        mockCard(10, "합정 심야책방", "서울 마포구 월드컵로3길 14", LABEL_WORTH_VISITING),
    )

private val MIXED_ROOM =
    MockRoomDeck(
        ggukPick = MIXED_ROOM_CARDS,
        latest = MIXED_ROOM_CARDS.asReversed(),
        nearest = MIXED_ROOM_CARDS.take(3),
    )

/**
 * 지표가 전부 0인 방.
 *
 * 저장·댓글·조회가 없어 정원을 채우지 못한 분량을 `worthVisiting`이 흡수한 결과다(계약 §2.4).
 * **모든 카드가 `worthVisiting`이어도 라벨은 항상 존재한다**(FR-008).
 */
private val ZERO_METRIC_ROOM_CARDS: List<CardResponse> =
    listOf(
        mockCard(11, "상암 백반집", "서울 마포구 월드컵북로 396", LABEL_WORTH_VISITING),
        mockCard(12, "신촌 만두집", "서울 서대문구 연세로5길 21", LABEL_WORTH_VISITING),
        mockCard(13, "공덕 칼국수", "서울 마포구 백범로 194", LABEL_WORTH_VISITING),
        mockCard(14, "청파동 카페", "서울 용산구 청파로47길 4", LABEL_WORTH_VISITING),
        mockCard(15, "대흥동 파스타", "서울 마포구 대흥로 111", LABEL_WORTH_VISITING),
    )

private val ZERO_METRIC_ROOM =
    MockRoomDeck(
        ggukPick = ZERO_METRIC_ROOM_CARDS,
        latest = ZERO_METRIC_ROOM_CARDS.asReversed(),
        // 후보 0건인 정렬. 좌표를 받아도 반경 안에 아무것도 없는 방이다(TS-017).
        nearest = emptyList(),
    )

/**
 * 장소는 있으나 어떤 정렬도 후보를 내지 못하는 방.
 *
 * 세 덱이 처음부터 소진 상태라 방을 열자마자 재판정이 일어난다 — TS-023·EC-013이 이 방으로 재현된다.
 */
private val EMPTY_ROOM =
    MockRoomDeck(
        ggukPick = emptyList(),
        latest = emptyList(),
        nearest = emptyList(),
    )

/**
 * `roomId` → 프로필. 같은 방은 언제 열어도 같은 덱을 받는다.
 *
 * [MIXED_ROOM]이 두 번 들어 있는 것은 의도다. 방이 하나뿐인 사용자가 빈 홈만 보게 되면 mock으로 확인할 수 있는 것이
 * 오히려 줄어든다.
 */
private val ROOM_PROFILES = listOf(MIXED_ROOM, MIXED_ROOM, ZERO_METRIC_ROOM, EMPTY_ROOM)

/** `hashCode()`가 음수여도 `mod`가 음이 아닌 나머지를 준다 — `%`나 `abs`와 달리 경계에서 깨지지 않는다. */
private fun roomDeckOf(roomId: String): MockRoomDeck = ROOM_PROFILES[roomId.hashCode().mod(ROOM_PROFILES.size)]

private fun mockCard(
    seed: Int,
    placeName: String,
    address: String,
    labelGroup: String,
    imageCount: Int = 2,
): CardResponse =
    CardResponse(
        id = "mock-pin-$seed",
        place = CardPlaceResponse(name = placeName, address = address),
        images = List(imageCount) { index -> "https://picsum.photos/seed/mino-$seed-$index/600/800" },
        createdBy = MOCK_REGISTRANTS[seed.mod(MOCK_REGISTRANTS.size)],
        labelGroup = labelGroup,
    )

// 서버가 내려주는 문자열 그대로다. 도메인 enum과의 대응은 `DeckMapper`만 알아야 하므로 여기서 PlaceLabel을 쓰지 않는다.
private const val LABEL_WORTH_VISITING = "worthVisiting"
private const val LABEL_MANY_SAVES = "manySaves"
private const val LABEL_MANY_COMMENTS = "manyComments"
private const val LABEL_MANY_VIEWS = "manyViews"
