package team.mino.feature.profile.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar
import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatarImage

/**
 * 번들 아바타 12종을 4열 × 3행으로 늘어놓는 그리드.
 *
 * 항목이 고정 12개라 `LazyVerticalGrid`가 아니라 `Column` + `Row`로 그린다 — 화면 전체가
 * 세로 스크롤 하나로 흐르는 구조에서 지연 그리드를 중첩하면 높이 제약이 무너진다.
 *
 * 한 칸을 어떻게 그리는지는 `MinoProfileAvatarImage`가 알고, 이 컴포넌트는 배치만 소유한다.
 * 선택 표시도 그리지 않는다 — 디자인에 선택된 칸을 구별하는 표현이 없어, 선택은 칸의
 * 접근성 시맨틱으로만 전달된다.
 *
 * @param selectedAvatar 현재 선택. `null`이면 아무 칸도 선택되지 않은 상태다.
 * @param onAvatarSelect 칸을 누를 때의 콜백.
 */
@Composable
internal fun ProfileAvatarGrid(
    selectedAvatar: MinoProfileAvatar?,
    onAvatarSelect: (MinoProfileAvatar) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RowSpacing),
    ) {
        AvatarRows.forEach { rowAvatars ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                rowAvatars.forEach { avatar ->
                    MinoProfileAvatarImage(
                        avatar = avatar,
                        selected = avatar == selectedAvatar,
                        onClick = { onAvatarSelect(avatar) },
                        contentDescription = "프로필 이미지 ${avatar.ordinal + 1}",
                    )
                }
            }
        }
    }
}

// 12종을 화면 폭에 맞춰 나누지 않고 열 수를 고정한다 — 디자인이 4열을 고정하고 있어,
// 폭에 따라 열 수가 바뀌면 원본과 어긋난다.
private const val COLUMN_COUNT = 4

private val AvatarRows = MinoProfileAvatar.entries.chunked(COLUMN_COUNT)

private val RowSpacing = 10.dp
