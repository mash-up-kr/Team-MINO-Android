package team.mino.feature.sharereceiver.fake

import team.mino.core.domain.model.AnonymousSession
import team.mino.core.domain.repository.AnonymousAuthRepository

/**
 * `:feature:sharereceiver` 테스트용 [AnonymousAuthRepository] 테스트 더블.
 *
 * **[ensureSession]은 호출되면 터진다.** 이 진입점은 스플래시를 거치지 않으므로 세션을 새로 확보하지 않고,
 * 없으면 없는 대로 빈 목록으로 넘어가야 한다(FR-019 · `research.md` R-012·R-020). 확보를 부르는 순간
 * 네트워크 왕복이 시트 표출 앞을 막아 UX-010이 무너지는데, 그 위반은 상태만 봐서는 드러나지 않는다 —
 * 성공하면 화면이 정상으로 보이기 때문이다. 그래서 상태가 아니라 **호출 자체**를 실패로 만든다.
 *
 * [session]이 `null`인 것이 "복원할 세션이 없다"이며, 앱을 지웠다 깐 직후(EC-011)와 온보딩 전(EC-004)이
 * 여기로 합류한다.
 */
internal class FakeAnonymousAuthRepository : AnonymousAuthRepository {
    /** [currentSession]이 돌려줄 세션. `null`이면 복원할 세션이 없는 기기다. */
    var session: AnonymousSession? = AnonymousSession(userId = USER_ID)

    /** [currentSession]이 호출된 횟수. 세션 확인이 실제로 조회 경로를 지났는지 본다. */
    var currentSessionCallCount: Int = 0
        private set

    override suspend fun ensureSession(): AnonymousSession =
        error("공유 수신 진입점은 세션을 확보하지 않는다 — currentSession()만 쓴다(research.md R-012·R-020).")

    override suspend fun currentSession(): AnonymousSession? {
        currentSessionCallCount++
        return session
    }

    companion object {
        /** 복원된 세션의 uid. 이 값이 화면에 드러나지는 않는다 — 세션 유무만 경로를 가른다. */
        const val USER_ID = "anonymous-user-1"
    }
}
