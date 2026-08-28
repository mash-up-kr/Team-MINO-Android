package team.mino.feature.home.fake

import team.mino.core.domain.repository.HomePreferencesRepository

/**
 * `:feature:home` 테스트용 [HomePreferencesRepository] 테스트 더블. 홈의 ViewModel 테스트 전부가 이 하나를 공유한다.
 *
 * 기본값은 **앱을 처음 설치해 실행한 기기**다 — 마지막 방이 없고([lastRoomId] `null`) 가이드를 닫은 적이 없다
 * ([guideDismissed] `false`). 재진입을 보려면 값을 세우면 된다(FR-022, TS-032·033).
 *
 * **[guideDismissed]는 덱을 다루는 테스트에서도 반드시 의식해야 하는 값이다.** 가이드가 떠 있는 동안 홈은
 * [DismissGuide][team.mino.feature.home.main.vm.HomeIntent.DismissGuide]를 뺀 모든 Intent를 버리므로
 * (FR-019), 스와이프를 보려는 테스트는 `guideDismissed = true`로 세워야 한다.
 *
 * [recordedLastRoomIds]와 [dismissGuideCallCount]를 남기는 이유는 **쓰기가 실제로 일어났는지**가 상태로
 * 드러나지 않기 때문이다. 마지막 방 저장은 다음 실행에서만, 가이드 닫음 기록은 다음 설치 생애에서만 보인다.
 * 여기서 세지 않으면 영속 저장을 통째로 빠뜨린 구현도 통과한다.
 */
internal class FakeHomePreferencesRepository : HomePreferencesRepository {
    /** 마지막으로 보던 방. [setLastRoomId]가 덮어쓴다. */
    var lastRoomId: String? = null

    /** 홈 사용 가이드를 닫은 이력. [dismissGuide]가 `true`로 만든다. */
    var guideDismissed: Boolean = false

    /** [dismissGuide]가 호출된 횟수. */
    var dismissGuideCallCount: Int = 0
        private set

    private val lastRoomIdWrites = mutableListOf<String>()

    /** [setLastRoomId]로 저장된 방 id들. 호출 순서대로 쌓인다. */
    val recordedLastRoomIds: List<String> get() = lastRoomIdWrites

    override suspend fun getLastRoomId(): String? = lastRoomId

    override suspend fun setLastRoomId(roomId: String) {
        lastRoomId = roomId
        lastRoomIdWrites += roomId
    }

    override suspend fun isGuideDismissed(): Boolean = guideDismissed

    override suspend fun dismissGuide() {
        dismissGuideCallCount++
        guideDismissed = true
    }
}
