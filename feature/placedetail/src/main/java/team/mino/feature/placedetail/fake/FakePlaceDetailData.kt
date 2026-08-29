package team.mino.feature.placedetail.fake

import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.PlaceComment
import team.mino.core.domain.model.PlaceCommentAuthor
import team.mino.core.domain.model.PlaceDetail
import team.mino.core.domain.model.PlaceRegistrant
import team.mino.core.domain.model.RoomColor

/**
 * **이번 UI 라운드 한정 샘플 데이터다. `tasks.md` T063이 `fake/` 패키지를 통째로 지운다.**
 *
 * API 연결 없이 화면을 띄워 검증하기 위한 원천이며, Preview와 Fake Repository가 이것 하나를 함께 쓴다.
 * 실제 구현은 `:core:data`가 갖는다 — `docs/specs/place-detail/contracts/place-repository.md`.
 *
 * **방 목록은 담지 않는다.** 마커 색과 [다른방에 공유] 시트는 실제 `getRooms()`를 쓴다
 * (`docs/specs/place-detail/research.md` D15).
 *
 * 샘플이 덮는 조합은 `docs/specs/place-detail/quickstart.md` §3의 시나리오다 —
 * 이미지 3장/1장/0장, 코멘트 0건/3건/페이지 경계를 넘는 다수, 200자 코멘트, `canDelete` 혼재,
 * 원문 링크 있음·없음, 한 줄을 넘는 장소명·주소.
 */
internal object FakePlaceDetailData {
    /** 서버 기본 페이지 크기(`comment-api.md`의 `example: 20`)를 그대로 흉내 낸다. */
    const val PAGE_SIZE: Int = 20

    private const val MANY_COMMENT_COUNT = 45

    private const val FILLER_SENTENCE = "여기 루프탑에서 보는 노을이 진짜 좋아요. 브런치는 오픈런이 아니면 웨이팅이 길고, 주차는 골목 안쪽 공영주차장이 편해요. "

    /** 코멘트 입력 상한(FR-012). 200자 꽉 찬 샘플을 만드는 데 쓴다. */
    const val COMMENT_MAX_LENGTH: Int = 200

    /**
     * 샘플 핀이 속한 방의 id.
     *
     * **실제 서버의 방 id가 아니므로 마커 색을 찾지 못하고, 그동안 마커는 그려지지 않는다**
     * (`research.md` D15). 지도 마커를 눈으로 확인하려면 이 값을 내 계정의 실제 `roomId`로 바꿔
     * 다시 빌드한다.
     */
    const val ROOM_ID: String = "fake-room-id"

    /** 이미지 3장 · 긴 장소명·주소 · 원문 링크 있음 · 코멘트 3건(`canDelete` 혼재, 200자 포함). */
    const val PIN_ID_IMAGES_THREE: String = "fake-pin-images-3"

    /** 이미지 1장 · 원문 링크 없음 · 코멘트 0건. EC-008·EC-017을 본다. */
    const val PIN_ID_IMAGES_ONE: String = "fake-pin-images-1"

    /** 이미지 0장 · 등록자 없음 · 코멘트 0건. EC-004·EC-009와, 짧은 콘텐츠의 EC-007을 본다. */
    const val PIN_ID_IMAGES_NONE: String = "fake-pin-images-0"

    /** 코멘트 45건 — [PAGE_SIZE] 기준 3페이지라 역방향 페이징(D11)을 확인할 수 있다. */
    const val PIN_ID_COMMENTS_MANY: String = "fake-pin-comments-many"

    /**
     * `adb`로 넘긴 `pinId`가 위 목록에 없을 때 대신 쓰는 샘플.
     *
     * 실제 서버의 `pinId`로 화면을 띄워도 빈 화면이 되지 않게 한다(`docs/specs/place-detail/quickstart.md` §2.2).
     */
    const val DEFAULT_PIN_ID: String = PIN_ID_IMAGES_THREE

    private val registrantWithAvatar =
        PlaceRegistrant(
            userId = "fake-user-1",
            nickname = "성수동산책러",
            avatarColor = RoomColor.LIME,
        )

    private val registrantWithoutAvatarColor =
        PlaceRegistrant(
            userId = "fake-user-2",
            nickname = "주말미식가",
            avatarColor = null,
        )

    private val places: Map<String, PlaceDetail> =
        listOf(
            PlaceDetail(
                pinId = PIN_ID_IMAGES_THREE,
                roomId = ROOM_ID,
                placeId = "fake-place-1",
                name = "성수동 골목 안쪽에 숨어 있는 통유리 루프탑 브런치 카페 미노스테이션 2호점",
                address = "서울특별시 성동구 아차산로17길 48 성수낙낙 지하 1층 101호 (성수동2가)",
                location = GeoPoint(latitude = 37.5446, longitude = 127.0559),
                imageUrls =
                    listOf(
                        imageUrl("place-3-a"),
                        imageUrl("place-3-b"),
                        imageUrl("place-3-c"),
                    ),
                registrant = registrantWithAvatar,
                sourceUrl = "https://www.instagram.com/p/fake-place-detail-source/",
                mapUrl = "https://map.naver.com/p/entry/place/1000000001",
            ),
            PlaceDetail(
                pinId = PIN_ID_IMAGES_ONE,
                roomId = ROOM_ID,
                placeId = "fake-place-2",
                name = "연남 소금집",
                address = "서울특별시 마포구 성미산로 161-8",
                location = GeoPoint(latitude = 37.5636, longitude = 126.9257),
                imageUrls = listOf(imageUrl("place-1-a")),
                registrant = registrantWithoutAvatarColor,
                sourceUrl = null,
                mapUrl = "https://map.naver.com/p/entry/place/1000000002",
            ),
            PlaceDetail(
                pinId = PIN_ID_IMAGES_NONE,
                roomId = ROOM_ID,
                placeId = "fake-place-3",
                name = "이름도 주소도 짧은 동네 분식집",
                address = "서울특별시 광진구 아차산로 200",
                location = GeoPoint(latitude = 37.5385, longitude = 127.0708),
                imageUrls = emptyList(),
                registrant = null,
                sourceUrl = null,
                mapUrl = null,
            ),
            PlaceDetail(
                pinId = PIN_ID_COMMENTS_MANY,
                roomId = ROOM_ID,
                placeId = "fake-place-4",
                name = "모두가 한마디씩 남기고 간 한강 뷰 통창 베이커리 카페 미노브레드 본점",
                address = "서울특별시 용산구 이촌로 300 한강대로변 상가동 2층 201호",
                location = GeoPoint(latitude = 37.5172, longitude = 126.9707),
                imageUrls = listOf(imageUrl("place-many-a"), imageUrl("place-many-b")),
                registrant = registrantWithAvatar,
                sourceUrl = "https://blog.naver.com/fake/place-detail-source",
                mapUrl = "https://map.naver.com/p/entry/place/1000000004",
            ),
        ).associateBy(PlaceDetail::pinId)

    /**
     * [prefix]로 시작해 길이가 정확히 [COMMENT_MAX_LENGTH]인 코멘트 본문.
     *
     * 문장을 이어 붙인 뒤 잘라 길이를 맞춘다 — 손으로 센 문자열을 두면 고칠 때마다 다시 세어야 하고,
     * 상한을 넘겨 버리면 200자가 잘리지 않는지 보려던 TS-027이 성립하지 않는다.
     */
    private fun maxLengthContent(prefix: String = ""): String =
        buildString {
            append(prefix)
            while (length < COMMENT_MAX_LENGTH) {
                append(FILLER_SENTENCE)
            }
        }.take(COMMENT_MAX_LENGTH)

    private val authors =
        listOf(
            PlaceCommentAuthor(userId = "fake-user-1", nickname = "성수동산책러", avatarColor = RoomColor.LIME),
            PlaceCommentAuthor(userId = "fake-user-2", nickname = "주말미식가", avatarColor = null),
            PlaceCommentAuthor(userId = "fake-user-3", nickname = "카페탐험대", avatarColor = RoomColor.VIOLET),
            PlaceCommentAuthor(userId = "fake-user-4", nickname = "동네한바퀴", avatarColor = RoomColor.RED_ORANGE),
        )

    /** 내 코멘트. [PlaceComment.canDelete]가 `true`라 [⋮]가 붙는다(D6). 새 코멘트의 작성자도 이 사람이다. */
    val ME = authors[0]

    /**
     * 코멘트 3건 — 오래된 것이 먼저다(FR-010).
     *
     * 1건은 200자 꽉 찼고(TS-027), [PlaceComment.canDelete]가 섞여 있어 [⋮] 노출 차이를 함께 본다
     * (TS-025·TS-026).
     */
    private val threeComments =
        listOf(
            comment(id = "fake-comment-1", content = "여기 웨이팅 길어요. 평일 오전 추천!", author = authors[1]),
            comment(id = "fake-comment-2", content = maxLengthContent(), author = ME),
            comment(id = "fake-comment-3", content = "주차 자리 넉넉했어요 👍", author = authors[2]),
        )

    /**
     * 코멘트 45건 — 오래된 것이 먼저다.
     *
     * [PAGE_SIZE] 20 기준으로 세 페이지(20·20·5)가 나와 페이지 경계가 실제로 생긴다. 본문에 순번을
     * 적어 두어 위로 스크롤할 때 **더 오래된** 코멘트가 앞에 붙는지 눈으로 확인할 수 있다(D11).
     */
    private val manyComments =
        List(MANY_COMMENT_COUNT) { index ->
            val ordinal = index + 1
            val content =
                if (ordinal == 1) {
                    maxLengthContent(prefix = "1번째 코멘트(가장 오래됨) · ")
                } else {
                    "${ordinal}번째 코멘트 · 페이지 경계를 넘어 오래된 것이 위에 붙는지 보는 샘플이에요."
                }
            comment(
                id = "fake-comment-many-$ordinal",
                content = content,
                author = authors[index % authors.size],
            )
        }

    private val comments: Map<String, List<PlaceComment>> =
        mapOf(
            PIN_ID_IMAGES_THREE to threeComments,
            PIN_ID_IMAGES_ONE to emptyList(),
            PIN_ID_IMAGES_NONE to emptyList(),
            PIN_ID_COMMENTS_MANY to manyComments,
        )

    /**
     * [pinId]의 샘플 핀. 등록되지 않은 [pinId]면 [DEFAULT_PIN_ID] 샘플을 그 id로 바꿔 돌려준다.
     */
    fun placeOf(pinId: String): PlaceDetail = places[pinId] ?: places.getValue(DEFAULT_PIN_ID).copy(pinId = pinId)

    /**
     * [pinId]의 코멘트 전량. 오래된 것이 먼저이며, 페이지로 자르는 것은 Repository의 몫이다.
     */
    fun commentsOf(pinId: String): List<PlaceComment> = comments[pinId] ?: comments.getValue(DEFAULT_PIN_ID)

    private fun comment(
        id: String,
        content: String,
        author: PlaceCommentAuthor,
    ) = PlaceComment(
        id = id,
        content = content,
        author = author,
        canDelete = author == ME,
    )

    /** 캐러셀 스와이프를 눈으로 구분하려면 장마다 다른 그림이어야 해서 seed를 준다. */
    private fun imageUrl(seed: String) = "https://picsum.photos/seed/$seed/1080/720"
}
