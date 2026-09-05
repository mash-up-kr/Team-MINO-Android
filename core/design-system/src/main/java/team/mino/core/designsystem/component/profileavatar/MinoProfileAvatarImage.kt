package team.mino.core.designsystem.component.profileavatar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import team.mino.core.designsystem.util.modifier.selectable.rippleSingleSelectable

/**
 * 번들 아바타 한 종을 원형으로 그리는 컴포넌트.
 *
 * 그림에는 배경 원과 캐릭터가 함께 굽혀 있고 원 밖은 비어 있어, 배경색을 따로 깔지 않고
 * 테두리만 얹는다. 아바타 여러 개를 늘어놓는 **배치는 화면이 소유한다** — 이 컴포넌트는
 * 자기 한 칸만 안다.
 *
 * @param avatar 그릴 아바타. `null`은 아직 고르지 않은 상태이며 기본 아바타를 그린다 — 기본 아바타는
 *   팔레트 12종에 끼지 않는 별개의 그림이라 [MinoProfileAvatar] 항목이 아니다.
 * @param size 아바타가 놓이는 자리([MinoProfileAvatarSize]). 지름과 테두리 두께가 함께 정해진다.
 * @param selected 선택 여부. **디자인에 선택 표시가 정의되어 있지 않아 시각적으로는 아무것도
 *   그리지 않고**, 스크린 리더가 현재 선택을 읽도록 접근성 시맨틱에만 싣는다. 시맨틱은 선택을
 *   바꿀 수 있을 때만 뜻이 있으므로 [onClick]이 있을 때만 노출된다. 선택 표시가 디자인에
 *   생기면 이 컴포넌트만 고치면 되고, 화면이 테두리를 덧그리지 않는다.
 * @param onClick 누를 수 있는 아바타로 만든다. null이면 클릭도 선택 시맨틱도 받지 않는다.
 *   디자인의 인터랙션 영역은 아바타 바깥까지 번지지만 Compose는 리플을 바운즈 밖으로 그리지 못해
 *   리플이 원 안에 머문다.
 * @param contentDescription 접근성 설명.
 */
@Composable
fun MinoProfileAvatarImage(
    avatar: MinoProfileAvatar?,
    modifier: Modifier = Modifier,
    size: MinoProfileAvatarSize = MinoProfileAvatarSize.Grid,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
) {
    val selectableModifier = if (onClick != null) {
        Modifier.rippleSingleSelectable(selected = selected, onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size.diameter)
            .clip(CircleShape)
            .then(selectableModifier)
            .border(
                width = size.borderWidth,
                color = MinoProfileAvatarDefaults.borderColor,
                shape = CircleShape,
            ),
    ) {
        ProfileAvatarPainting(avatar = avatar, contentDescription = contentDescription)
    }
}
