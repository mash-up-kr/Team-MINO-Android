package team.mino.core.domain.repository

/**
 * 홈이 기기에 영속 저장하는 값의 계약.
 *
 * 대상은 **마지막으로 보던 방과 홈 사용 가이드를 닫은 이력, 둘뿐이다**
 * (`docs/specs/home-deck-exploration/research.md` R-004). 현재 정렬·덱별 소진 여부·되돌리기 이력은
 * `HomeUiState`에만 두고 저장하지 않는다 — 소진 이력까지 이어 붙이면 재진입 사용자가 볼 카드 없이 완료 화면부터 만난다.
 *
 * 서버에 올리지도, 홈 진입 시 묻지도 않는다(FR-022). 앱이 값을 쥐고 있어야 방 목록과 덱을 동시에 요청할 수 있다.
 *
 * `docs/specs/home-deck-exploration/contracts/home-ui.md` §4.3.
 */
interface HomePreferencesRepository {
    /** 마지막으로 보던 방. 저장된 적이 없으면 `null`이며, 이때 시작 방은 방 목록의 첫 방이다 — FR-022, TS-033. */
    suspend fun getLastRoomId(): String?

    /** 마지막으로 보던 방을 [roomId]로 덮어쓴다. */
    suspend fun setLastRoomId(roomId: String)

    /** 홈 사용 가이드를 닫은 적이 있으면 `true` — FR-019, TS-031. */
    suspend fun isGuideDismissed(): Boolean

    /** 홈 사용 가이드를 닫은 것으로 기록한다. 이후 [isGuideDismissed]는 `true`를 돌려준다. */
    suspend fun dismissGuide()
}
