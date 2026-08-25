package team.mino.core.data.datasource.mock

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.data.repository.mapper.toIdentifier
import team.mino.core.domain.model.RoomColor
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 서버가 없는 동안 방 API를 대신하는 인메모리 저장소. 계약의 소유자는
 * `docs/specs/group-room-form/contracts/room-api-mock.md` §3이다.
 *
 * 보관은 프로세스 수명 동안만 유지된다. HTTP 레이어를 건너뛰므로 `{ data }` 봉투도 흉내내지 않고 DTO를 그대로 돌려준다.
 *
 * 실패 주입 스위치를 두지 않으며 403도 두지 않는다 — 현재 사용자가 고정 상수라 도달 경로가 없다.
 * 실패 경로의 검증은 Fake Repository를 쓰는 ViewModel 테스트가 소유한다.
 *
 * 없는 방을 찾는 실패를 이 클래스가 직접 도메인 예외로 던지는 이유는, mock이 `HttpClient`의 전역 매핑
 * (`convertDomainException`)을 타지 않기 때문이다. 실서버로 바뀌면 그 자리를 매핑이 대신하므로 위 계약 문서 §4의
 * 전환 지점에 이 클래스가 통째로 빠진다.
 */
@Singleton
internal class RoomMockStore @Inject constructor() {
    /** 맵 접근 보호가 목적이다. 중복 생성 차단은 이 잠금이 아니라 폼의 `isSubmitting`이 한다(research.md R-012). */
    private val mutex = Mutex()

    private val rooms: MutableMap<String, RoomResponse> = linkedMapOf()

    private var lastIdNumber = 0

    init {
        // 주입 전 단독 생성 시점이라 아직 경합이 없다. 시드만 잠금 없이 넣는다.
        // 시드는 도메인 색으로 부르고 식별자는 `RoomMapper`를 거쳐 얻는다. 서버 표현이 바뀌어도 고칠 곳이 그 한 파일이도록,
        // 대응표는 물론 그 결과 문자열도 여기 옮겨 적지 않는다.
        seed(name = "야호", description = "야호호", color = RoomColor.RED)
        seed(name = "내 장소", description = null, color = RoomColor.GRAY)
    }

    suspend fun getRoom(roomId: String): RoomResponse {
        awaitMockLatency()
        return mutex.withLock { rooms[roomId] } ?: throw roomNotFound(roomId)
    }

    suspend fun createRoom(request: RoomRequest): RoomResponse {
        awaitMockLatency()
        return mutex.withLock {
            val room =
                RoomResponse(
                    id = nextId(),
                    name = request.name,
                    description = request.description,
                    color = request.color,
                    ownerId = OWNER_ID,
                )
            rooms[room.id] = room
            room
        }
    }

    suspend fun updateRoom(
        roomId: String,
        request: RoomRequest,
    ): RoomResponse {
        awaitMockLatency()
        return mutex.withLock {
            val stored = rooms[roomId] ?: throw roomNotFound(roomId)
            // 폼이 다루는 세 값만 갈아끼운다. 식별자와 방장은 방이 만들어질 때 정해진 값 그대로다.
            val updated =
                stored.copy(
                    name = request.name,
                    description = request.description,
                    color = request.color,
                )
            rooms[roomId] = updated
            updated
        }
    }

    /**
     * 로딩과 중복 제출 차단이 눈에 보이도록 각 함수 앞에 두는 지연이다.
     *
     * 잠금 밖에서 기다린다 — 잠금 안에서 기다리면 동시 요청이 직렬화되어, 두 요청이 각각 방을 만든다는 사실이
     * 가려진다. 중복 제출 차단이 실제로 무엇에 달려 있는지 눈으로 확인하려면 그 사실이 가려지면 안 된다.
     */
    private suspend fun awaitMockLatency() = delay(LATENCY_MILLIS)

    private fun seed(
        name: String,
        description: String?,
        color: RoomColor,
    ) {
        val id = nextId()
        rooms[id] =
            RoomResponse(
                id = id,
                name = name,
                description = description,
                color = color.toIdentifier(),
                ownerId = OWNER_ID,
            )
    }

    /** `UUID`를 쓰지 않는다. 사람이 로그를 눈으로 읽고 그대로 편집 경로에 넣을 수 있어야 한다. */
    private fun nextId(): String = "room-${++lastIdNumber}"

    private fun roomNotFound(roomId: String): MinoDomainException =
        MinoDomainException.Http(HTTP_NOT_FOUND, IOException("mock: room $roomId not found"))

    private companion object {
        /**
         * 고정된 현재 사용자. 만들어진 방의 방장이 항상 이 값이므로 "방장이 아니다"라는 상황이 생기지 않는다.
         */
        const val OWNER_ID = "mock-owner"

        const val LATENCY_MILLIS = 800L

        const val HTTP_NOT_FOUND = 404
    }
}
