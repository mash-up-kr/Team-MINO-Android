package team.mino.feature.room.fake

import team.mino.core.domain.model.AnonymousSession
import team.mino.core.domain.repository.AnonymousAuthRepository

/**
 * `:feature:room` 테스트용 [AnonymousAuthRepository] 테스트 더블.
 *
 * [RoomListViewModel]은 목록 구독 전에 `ensureSession()`을 기다린다 — 이 더블은 항상 즉시 성공해
 * 그 대기가 목록 관련 테스트를 막지 않게 한다. 세션 확보 자체의 성공·실패·지연은 이 모듈의 관심사가
 * 아니다(그건 진입 화면 소관).
 */
internal class FakeAnonymousAuthRepository : AnonymousAuthRepository {
    var session: AnonymousSession = AnonymousSession(userId = "anonymous-user-1")

    override suspend fun ensureSession(): AnonymousSession = session

    override suspend fun currentSession(): AnonymousSession = session
}
