package team.mino.feature.profile.main.vm

import team.mino.core.common.android.architecture.SideEffect

/**
 * 저장 실패는 여기에 없다. 실패는 `DomainErrorEmitter`로 나가고 화면이 스낵바로 알린다.
 */
internal sealed interface ProfileSideEffect : SideEffect {
    /** "저장이 끝났다"만 말한다. 다음 목적지는 이 신호를 받는 쪽이 정한다. */
    data object SaveCompleted : ProfileSideEffect
}
