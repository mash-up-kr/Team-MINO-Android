package team.mino.feature.home.main.component

import team.mino.core.domain.model.PlaceCategoryLabel

/**
 * 장소분류 라벨을 카드가 그리는 분류로 옮긴다. 두 enum이 1:1이므로 `else` 분기를 두지 않는다 —
 * 라벨이 늘면 컴파일 에러로 드러나는 편이 낫다.
 */
internal fun PlaceCategoryLabel.toHomeCardCategory(): HomeCardCategory =
    when (this) {
        PlaceCategoryLabel.FRIENDS_MOST_VIEWED -> HomeCardCategory.FriendsMostViewed
        PlaceCategoryLabel.MOST_TALKED -> HomeCardCategory.MostTalked
        PlaceCategoryLabel.MOST_SAVED -> HomeCardCategory.MostSaved
        PlaceCategoryLabel.WORTH_VISITING -> HomeCardCategory.WorthVisiting
    }
