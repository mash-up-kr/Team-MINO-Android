package team.mino.feature.roomform

import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route

/**
 * 방 생성·편집 화면의 시작 라우트.
 *
 * 두 인자에 기본값이 있는 이유는 **테스트에서 ViewModel을 만들 수 있게 하기 위해서다.**
 * `savedStateHandle.toRoute<RoomForm>()`는 값을 `Bundle`로 다시 싸서 읽는데, JVM 단위 테스트의
 * 스텁 `android.jar`에서는 `Bundle.putAll`이 동작하지 않아 non-null 인자 디코딩이
 * `Unexpected null value for non-nullable argument`로 실패한다. 기본값이 있으면 빈
 * `SavedStateHandle`에서 해당 필드가 skip되어 기본값으로 디코딩되므로 ViewModel 생성이 가능하다.
 *
 * **대가:** 기본값은 프로덕션 호출 경로의 인자 누락을 컴파일 시점에 감춘다. 이 라우트를 만드는 곳은
 * `RoomFormActivity` 한 곳이므로, 그 자리에서 두 인자를 **항상 명시**해야 한다.
 *
 * @param roomId 편집할 방 ID. `null`이면 생성 모드.
 * @param isOnboarding 온보딩 흐름에서 진입했는지 여부.
 */
@Serializable
internal data class RoomForm(
    val roomId: String? = null,
    val isOnboarding: Boolean = false,
) : Route
