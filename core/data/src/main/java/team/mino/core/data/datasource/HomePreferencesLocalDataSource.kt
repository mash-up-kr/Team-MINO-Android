package team.mino.core.data.datasource

/**
 * 홈이 기기에 남기는 값의 저장소.
 *
 * **영속 대상은 마지막으로 보던 방과 가이드를 닫은 이력 둘뿐이다.** 현재 정렬·덱별 소진 여부·되돌리기 이력은
 * 화면 상태로만 살고 여기 오지 않는다 — `docs/specs/home-deck-exploration/research.md` R-004.
 */
internal interface HomePreferencesLocalDataSource {
    /** 마지막으로 보던 방. 저장된 적이 없으면 `null`이며, 그 경우의 시작 방 결정은 위 레이어가 한다(FR-022). */
    suspend fun getLastRoomId(): String?

    suspend fun setLastRoomId(roomId: String)

    /** 홈 사용 가이드를 닫은 적이 있는지. 닫은 적이 없으면 `false`다(FR-019). */
    suspend fun isGuideDismissed(): Boolean

    suspend fun dismissGuide()
}
