package team.mino.core.domain.usecase

import team.mino.core.domain.model.Profile
import team.mino.core.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * 프로필을 저장한다. 판정 통과를 확인하고, 앞뒤 공백을 제거한 닉네임으로 [Profile]을 만들어 저장한다.
 *
 * 판정 실패는 화면이 이미 막았어야 하는 경로라 도메인 예외로 감싸지 않고 프로그래머 오류로 전파한다
 * (에러 처리 규약 §1). [avatarId]는 유효한 값이 온다고 전제한다 — 미선택 시 기본값을 채우는 것은 화면의 책임이다.
 *
 * 저장 실패는 잡지 않는다. `MinoDomainException`의 소비는 ViewModel의 `runCatchingDomain`이 한다.
 */
class SaveProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val validateNickname: ValidateNicknameUseCase,
) {
    suspend operator fun invoke(
        rawNickname: String,
        avatarId: Int,
    ) {
        require(validateNickname(rawNickname)) { "저장할 수 없는 닉네임이다: $rawNickname" }

        profileRepository.saveProfile(Profile(nickname = rawNickname.trim(), avatarId = avatarId))
    }
}
