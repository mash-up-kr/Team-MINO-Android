package team.mino.feature.onboarding.tutorial.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import team.mino.feature.onboarding.R

/**
 * 공유 방법 튜토리얼의 스텝 목록. 선언 순서가 곧 페이지 순서다.
 *
 * 스텝 번호·안내 문구·예시 이미지·캐릭터를 한 값이 함께 들어, 서로 다른 스텝을 가리키는 조합이
 * 만들어지지 않는다. 화면은 페이지 인덱스로 이 값 하나를 고르고 네 요소를 거기서 읽는다.
 */
internal enum class TutorialStep(
    @get:StringRes val guideRes: Int,
    @get:DrawableRes val exampleImageRes: Int,
    @get:DrawableRes val mascotRes: Int,
) {
    STEP_1(
        guideRes = R.string.onboarding_tutorial_step_1_guide,
        exampleImageRes = R.drawable.tutorial_example_step_1,
        mascotRes = R.drawable.tutorial_mascot_step_1,
    ),
    STEP_2(
        guideRes = R.string.onboarding_tutorial_step_2_guide,
        exampleImageRes = R.drawable.tutorial_example_step_2,
        mascotRes = R.drawable.tutorial_mascot_step_2,
    ),
    STEP_3(
        guideRes = R.string.onboarding_tutorial_step_3_guide,
        exampleImageRes = R.drawable.tutorial_example_step_3,
        mascotRes = R.drawable.tutorial_mascot_step_3,
    ),
    STEP_4(
        guideRes = R.string.onboarding_tutorial_step_4_guide,
        exampleImageRes = R.drawable.tutorial_example_step_4,
        mascotRes = R.drawable.tutorial_mascot_step_4,
    ),
    STEP_5(
        guideRes = R.string.onboarding_tutorial_step_5_guide,
        exampleImageRes = R.drawable.tutorial_example_step_5,
        mascotRes = R.drawable.tutorial_mascot_step_5,
    ),
    ;

    /** 화면에 보이는 스텝 번호. 선언 순서가 곧 페이지 순서라 순서에서 파생시킨다. */
    val number: Int get() = ordinal + 1
}
