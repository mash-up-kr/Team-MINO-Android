package team.mino.core.domain.model

/**
 * 이 앱 설치가 온보딩의 어디까지 왔는지. 설치에 하나만 존재한다.
 *
 * [lastStep]은 마지막으로 머무른 스텝이며, 저장된 값이 없으면 [OnboardingStep.PROFILE]이다.
 *
 * [createdRoomId]는 온보딩에서 만든 공동방의 id다. 공동방 생성을 건너뛴 설치에서는 계속 `null`이다.
 * 이 값이 진행 상태에 있는 이유는 재개 경로에 폼의 결과 인텐트가 없기 때문이다 —
 * 친구 초대 스텝에서 중단한 뒤 다시 켜면 초대 링크를 여기 저장된 id로 다시 확보한다.
 * 한 온보딩에서 한 번만 채워지며, 이미 차 있으면 공동방 스텝을 다시 열지 않는다.
 *
 * [isCompleted]는 한 번 `true`가 되면 되돌아가지 않는다. 이 값을 읽는 주체는 온보딩 밖(스플래시)이며,
 * 원천은 이 설치의 로컬 저장소 하나다 — 서버에 묻지 않는다.
 *
 * 프로필·개인방의 존재 여부는 여기 담지 않는다. 그것은 프로필·방 쪽 원천이 갖는다.
 */
data class OnboardingProgress(
    val lastStep: OnboardingStep = OnboardingStep.PROFILE,
    val createdRoomId: String? = null,
    val isCompleted: Boolean = false,
)
