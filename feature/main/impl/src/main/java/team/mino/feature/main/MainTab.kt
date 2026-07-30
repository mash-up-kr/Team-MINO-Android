package team.mino.feature.main

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.BellFill
import team.mino.core.designsystem.foundation.icons.icons.FolderFill
import team.mino.core.designsystem.foundation.icons.icons.HomeFill
import team.mino.core.designsystem.foundation.icons.icons.PersonFill
import team.mino.core.navigation.screen.Route

/**
 * 하단 네비게이션 탭 목록의 단일 출처. 선언 순서가 곧 탭 배치 순서이며, 첫 항목이 시작 목적지다.
 *
 * 아이콘은 선택 여부와 무관하게 한 벌만 쓰고 색으로만 구분한다(→ `MinoBottomNavigationItem`).
 */
internal enum class MainTab(
    val route: Route,
    val icon: ImageVector,
    @get:StringRes val labelRes: Int,
) {
    HOME(Home, MinoIcons.HomeFill, R.string.main_tab_home),
    SAVED(Saved, MinoIcons.FolderFill, R.string.main_tab_saved),
    NOTIFICATION(Notification, MinoIcons.BellFill, R.string.main_tab_notification),
    MY_PAGE(MyPage, MinoIcons.PersonFill, R.string.main_tab_my_page),
}
