package team.mino.core.domain.repository

import kotlinx.coroutines.flow.Flow
import team.mino.core.domain.model.Profile

interface ProfileRepository {
    /**
     * 저장된 프로필을 흘린다. 아직 저장된 적이 없으면 `null`을 흘리며, [saveProfile]로 값이 바뀔 때마다 새 값을 흘린다.
     *
     * 도메인 실패를 정의하지 않는다 — 이 흐름은 오류로 끝나지 않는다.
     */
    fun observeProfile(): Flow<Profile?>

    /**
     * 프로필을 저장한다. 닉네임과 아바타 식별자는 항상 함께 덮어써지며, 둘 중 하나만 반영된 중간 상태는 관측되지 않는다.
     *
     * 저장된 값을 돌려주지 않는다 — 저장 결과를 읽는 원천은 [observeProfile] 하나다.
     * 저장에 실패하면 `MinoDomainException`으로 던지고 취소는 그대로 전파한다.
     * 다만 이번 범위는 로컬 저장소 단독이라 그 예외로 변환되는 지점이 아직 없다 — 실제 저장 실패는 도메인 예외가 아니라 CEH까지 간다.
     * 원격 연동에서 매핑 지점이 생길 때 닫힌다 (`docs/specs/profile/research.md` D25).
     */
    suspend fun saveProfile(profile: Profile)
}
