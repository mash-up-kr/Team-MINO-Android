package team.mino.core.data.datasource

internal interface OnboardingProgressLocalDataSource {
    /** 저장된 세 값을 한 번에 읽는다. 관찰하지 않는다 — 읽는 시점은 온보딩 진입과 스플래시 분기뿐이다. */
    suspend fun getProgress(): OnboardingProgressEntry

    /** 마지막으로 머무른 스텝의 이름을 덮어쓴다. */
    suspend fun setLastStepName(stepName: String)

    /** 온보딩에서 만든 공동방 id를 덮어쓴다. */
    suspend fun setCreatedRoomId(roomId: String)

    /** 완료 표시를 세운다. 되돌리는 함수를 두지 않는다 — 한 번 완료된 설치는 온보딩으로 돌아가지 않는다. */
    suspend fun markCompleted()
}
