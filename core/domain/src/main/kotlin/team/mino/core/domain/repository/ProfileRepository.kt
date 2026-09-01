package team.mino.core.domain.repository

import kotlinx.coroutines.flow.Flow
import team.mino.core.domain.model.Profile

/**
 * 프로필을 읽고 쓰는 계약. 원천은 서버이고 로컬 저장소는 캐시다.
 *
 * [refreshProfile]·[saveProfile] 모두 값을 돌려주지 않는다 — 결과를 읽는 원천은 [observeProfile] 하나다.
 */
interface ProfileRepository {
    /**
     * 캐시된 프로필을 흘린다. 캐시가 비어 있으면 `null`을 흘리며, [refreshProfile]이나 [saveProfile]로 값이 바뀔 때마다
     * 새 값을 흘린다.
     *
     * 도메인 실패를 정의하지 않는다 — 이 흐름은 오류로 끝나지 않는다.
     */
    fun observeProfile(): Flow<Profile?>

    /**
     * 서버에서 프로필을 받아 캐시를 맞춘다.
     *
     * **미등록은 실패가 아니다.** 서버에 아직 사용자가 없으면 캐시를 비우고 정상 종료한다 — 프로필을 처음 만드는
     * 사용자에게 온보딩 진입은 정상 상태이므로, 그 경우까지 예외로 던지면 화면에 들어서자마자 오류를 보게 된다.
     *
     * 네트워크 단절과 미등록이 아닌 HTTP 실패는 `MinoDomainException`으로 던지고, 취소는 그대로 전파한다.
     */
    suspend fun refreshProfile()

    /**
     * 프로필을 저장한다. 닉네임과 아바타는 항상 함께 나가며, 둘 중 하나만 반영된 중간 상태는 관측되지 않는다.
     *
     * **원격 성공 → 캐시 갱신 순서다.** 원격이 실패하면 캐시를 건드리지 않는다 — 저장이 실패했는데 캐시가 바뀌면
     * 화면을 다시 열었을 때 저장되지 않은 값이 프리필된다.
     *
     * 저장에 실패하면 `MinoDomainException`으로 던지고 취소는 그대로 전파한다.
     */
    suspend fun saveProfile(profile: Profile)

    /**
     * 서버가 발급한 내 user id. [Profile]은 이 값을 담지 않는다(어느 요구사항도 쓰지 않아 도메인에 올리지
     * 않기로 한 결정) — 방장 판정처럼 서버 리소스의 소유자 id와 직접 비교해야 하는 소비처만 이 함수를 쓴다.
     *
     * Firebase 익명 로그인 uid와는 다른 식별자 체계다 — 서버 리소스(`Room.ownerId` 등)는 이 값으로만
     * 비교해야 한다.
     *
     * 미등록이면 `null`이다. 그 밖의 실패는 `MinoDomainException`으로 던지고 취소는 그대로 전파한다.
     */
    suspend fun currentUserId(): String?
}
