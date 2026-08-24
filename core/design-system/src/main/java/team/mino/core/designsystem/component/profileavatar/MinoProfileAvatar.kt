package team.mino.core.designsystem.component.profileavatar

import androidx.annotation.DrawableRes
import team.mino.core.designsystem.R

/**
 * 앱이 번들한 프로필 아바타 12종.
 *
 * 각 항목은 자기 그림 하나만 안다. 저장 식별자·"미선택"·그리드 배치는 갖지 않으며, 그것들은
 * 이 목록을 소비하는 feature가 소유한다. 선언 순서는 디자인 목록의 순서(좌→우, 상→하)를 따르므로,
 * 순서에 기대는 매핑을 두는 쪽은 항목이 끼어들 때 함께 흔들린다는 점을 감안해야 한다.
 *
 * 그림에는 배경 원과 캐릭터가 함께 굽혀 있어 배경색을 코드에서 따로 입히지 않는다.
 *
 * @property drawableRes 항목의 그림. 모듈 밖에서 직접 그리지 못하도록 `internal`로 둔다.
 */
enum class MinoProfileAvatar(
    @get:DrawableRes internal val drawableRes: Int,
) {
    Person1(R.drawable.profile_avatar_person_01),
    Person2(R.drawable.profile_avatar_person_02),
    Person3(R.drawable.profile_avatar_person_03),
    Person4(R.drawable.profile_avatar_person_04),
    Person5(R.drawable.profile_avatar_person_05),
    Person6(R.drawable.profile_avatar_person_06),
    Person7(R.drawable.profile_avatar_person_07),
    Person8(R.drawable.profile_avatar_person_08),
    Person9(R.drawable.profile_avatar_person_09),
    Person10(R.drawable.profile_avatar_person_10),
    Person11(R.drawable.profile_avatar_person_11),
    Person12(R.drawable.profile_avatar_person_12),
}
