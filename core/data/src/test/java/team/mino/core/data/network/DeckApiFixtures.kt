package team.mino.core.data.network

/**
 * `cards` 태그 테스트가 공유하는 경로·본문.
 *
 * **`DeckMockStore`가 재현하던 응답 형태를 이 자리로 옮겨 왔다.** mock을 실서버로 걷어내면
 * `docs/specs/home-deck-exploration/contracts/deck-api.md` §4의 「반드시 재현해야 하는 경우」를 앱이
 * 만들어 낼 방법이 없어지므로, 그 경우들을 픽스처로 남겨 계약이 지켜지는지를 계속 검사한다.
 */
internal fun cardsPath(roomId: String): String = "/api/v1/rooms/$roomId/cards"

/**
 * 카드 리터럴을 `data`가 담는 모양(계약 §2.2)으로 감싼다.
 *
 * 감싸는 자리를 하나로 두는 이유는 이미 두 번 바뀌었기 때문이다 — `data`가 배열이던 것이 `{room, cards}`
 * 객체가 됐다. 본문마다 손으로 적어 두면 다음 변경도 픽스처 수만큼 고쳐야 한다.
 *
 * [room]은 [MIXED_LABEL_DECK_BODY]만 싣는다. 홈이 그 값을 담지 않고 흘려보낸다는 것은 한 자리에서 확인되고,
 * 나머지에도 붙이면 검사되는 것이 늘지 않은 채 본문만 길어진다.
 */
private fun deckBody(
    cards: String,
    room: String = "",
): String = """{"data":{$room"cards":[$cards]}}"""

/**
 * 라벨 4종이 한 덱에 섞여 있다(TS-014). 라벨별로 묶이지 않은 응답 순서 그대로다.
 *
 * `room`(홈 헤더용 방 메타)을 함께 싣는 유일한 본문이기도 하다. `CardFeedResponse`가 그 값을 담지 않고
 * `ignoreUnknownKeys`가 흡수하는데, 흡수에 실패하면 이 본문을 먹는 테스트가 먼저 빨개진다.
 */
internal val MIXED_LABEL_DECK_BODY =
    deckBody(
        room = """"room":{"id":"room-1","type":"shared","name":"맛집 탐방","color":"red"},""",
        cards = """
        {"id":"pin-1","place":{"name":"연남동 감자탕","address":"서울 마포구 연남로 21"},
         "images":["https://img/1"],"createdBy":{"userId":"u-1","nickname":"구구","avatar":{"color":"blue"}},
         "labelGroup":"worthVisiting"},
        {"id":"pin-2","place":{"name":"성수 로스터리","address":"서울 성동구 연무장길 45"},
         "images":[],"createdBy":{"userId":"u-2","nickname":"민초","avatar":null},
         "labelGroup":"manySaves"},
        {"id":"pin-3","place":{"name":"망원시장 분식","address":"서울 마포구 포은로8길 14"},
         "images":[],"createdBy":null,"labelGroup":"manyComments"},
        {"id":"pin-4","place":{"name":"한강뷰 루프탑","address":"서울 용산구 이촌로 302"},
         "images":[],"createdBy":null,"labelGroup":"manyViews"}
        """,
    )

/** 후보가 적어 10장을 채우지 못한 덱(TS-005). 서버가 정원 미달을 채우지 않는다는 계약 §2.3 그대로다. */
internal val SHORT_DECK_BODY =
    deckBody(
        """
        {"id":"pin-1","place":{"name":"상암 백반집","address":"서울 마포구 월드컵북로 396"},
         "images":[],"createdBy":null,"labelGroup":"worthVisiting"},
        {"id":"pin-2","place":{"name":"신촌 만두집","address":"서울 서대문구 연세로5길 21"},
         "images":[],"createdBy":null,"labelGroup":"worthVisiting"}
        """,
    )

/** 후보 0건인 정렬(TS-017·TS-023·EC-013). 홈은 이것을 「소진」으로 흡수한다. */
internal val EMPTY_DECK_BODY = deckBody("")

/**
 * 지표가 전부 0인 방. 자격 미달분을 `worthVisiting`이 흡수해도 라벨은 항상 존재한다(FR-008, 계약 §2.4).
 *
 * [SHORT_DECK_BODY]의 별칭으로 두지 않는다 — 같은 입력을 이름만 바꿔 두 번 태우면 검사되는 것이 겹쳐,
 * 별개의 경우가 확인되는 것처럼 읽히기만 한다.
 */
internal val ALL_WORTH_VISITING_DECK_BODY =
    deckBody(
        """
        {"id":"pin-11","place":{"name":"상암 백반집","address":"서울 마포구 월드컵북로 396"},
         "images":[],"createdBy":null,"labelGroup":"worthVisiting"},
        {"id":"pin-12","place":{"name":"신촌 만두집","address":"서울 서대문구 연세로5길 21"},
         "images":[],"createdBy":null,"labelGroup":"worthVisiting"},
        {"id":"pin-13","place":{"name":"공덕 칼국수","address":"서울 마포구 백범로 194"},
         "images":[],"createdBy":null,"labelGroup":"worthVisiting"},
        {"id":"pin-14","place":{"name":"청파동 카페","address":"서울 용산구 청파로47길 4"},
         "images":[],"createdBy":null,"labelGroup":"worthVisiting"},
        {"id":"pin-15","place":{"name":"대흥동 파스타","address":"서울 마포구 대흥로 111"},
         "images":[],"createdBy":null,"labelGroup":"worthVisiting"}
        """,
    )

/**
 * 계약에 있는 필드가 빠진 카드. 배포 스키마에 `required`가 없어 실제로 올 수 있는 형태이며,
 * 이것 때문에 덱 전체가 실패하면 안 된다.
 */
internal val MISSING_FIELDS_DECK_BODY = deckBody("""{"images":["https://img/1"]}""")

/** 아바타 객체가 색 없이 오는 응답. 이것 때문에 덱 전체가 죽으면 안 된다. */
internal val AVATAR_WITHOUT_COLOR_DECK_BODY =
    deckBody(
        """
        {"id":"pin-1","place":{"name":"연남동 감자탕","address":"서울 마포구 연남로 21"},
         "images":[],"createdBy":{"userId":"u-1","nickname":"구구","avatar":{}},
         "labelGroup":"worthVisiting"}
        """,
    )
