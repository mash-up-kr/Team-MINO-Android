package team.mino.feature.sample.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.avatar.MinoAvatar
import team.mino.core.designsystem.component.avatar.MinoAvatarSize
import team.mino.core.designsystem.component.avatar.MinoAvatarVariant
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.MoreVertical
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * 홈 카드의 분류(카테고리). variant별로 상단 뱃지의 라벨과 Accent 색이 달라진다.
 * Figma `Aos_home_card`의 `Property 1` 4종에 대응한다.
 */
enum class HomeCardCategory(val label: String) {
    FriendsMostViewed("친구들이 많이 본 곳"),
    MostTalked("이야기 많은 곳"),
    MostSaved("여럿이 저장한 곳"),
    WorthVisiting("가볼 만한 곳"),
}

/**
 * 홈 화면에서 장소를 소개하는 카드.
 *
 * Figma(MU_Wanted / Montage)의 `Aos_home_card` 스펙을 따른다. 상단 헤더(아바타 + 분류 뱃지 +
 * 더보기) · 제목 · 주소 · 하단 대표 이미지 2칸으로 구성된다. 카드 골격은 [category]와 무관하게
 * 동일하고, 분류 뱃지의 라벨·색만 [category]에 따라 바뀐다.
 *
 * design-system 공용 컴포넌트가 아니라 sample 화면 내부 컴포넌트다. 색·타이포·라운드는
 * `core:design-system` 토큰으로 조립하고, 아바타·아이콘은 design-system 컴포넌트를 재사용한다.
 *
 * @param imageCount 하단에 표시할 이미지 슬롯 수(기본 2). 실제 이미지 로딩은 이후 도입 예정이라
 *   현재는 placeholder로 그린다(spec Open Questions TBD-2).
 * @param avatarImageUrl 헤더 아바타 이미지 URL. null이면 placeholder 글리프.
 * @param onMoreClick 더보기(⋮) 클릭 콜백.
 */
@Composable
fun HomeCard(
    category: HomeCardCategory,
    title: String,
    address: String,
    modifier: Modifier = Modifier,
    imageCount: Int = 2,
    avatarImageUrl: String? = null,
    onMoreClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MinoAndroidTheme.colors.backgroundNormalNormal)
            .border(1.dp, MinoAndroidTheme.colors.lineNormalAlternative, CardShape)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HomeCardHeader(
            category = category,
            title = title,
            address = address,
            avatarImageUrl = avatarImageUrl,
            onMoreClick = onMoreClick,
        )
        HomeCardImages(imageCount = imageCount)
    }
}

@Composable
private fun HomeCardHeader(
    category: HomeCardCategory,
    title: String,
    address: String,
    avatarImageUrl: String?,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MinoAvatar(
                    variant = MinoAvatarVariant.Person,
                    size = MinoAvatarSize.Small,
                    imageUrl = avatarImageUrl,
                )
                CategoryBadge(category = category)
            }
            Icon(
                imageVector = MinoIcons.MoreVertical,
                contentDescription = "더보기",
                tint = MinoAndroidTheme.colors.labelNormal,
                modifier = Modifier
                    .clip(RoundedCornerShape(1000.dp))
                    .rippleSingleClickable(onClick = onMoreClick)
                    .padding(7.dp)
                    .size(18.dp),
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MinoAndroidTheme.typography.body1NormalBold,
                color = MinoAndroidTheme.colors.labelNormal,
            )
            Text(
                text = address,
                style = MinoAndroidTheme.typography.label2Regular,
                color = MinoAndroidTheme.colors.labelAlternative,
            )
        }
    }
}

/**
 * 분류 뱃지. Figma는 Accent Foreground 색 텍스트 + 같은 색 8% 배경이다.
 * design-system `MinoContentBadge`는 Accent가 Cyan 단색으로 고정돼 4색을 표현할 수 없어 직접 구성한다.
 */
@Composable
private fun CategoryBadge(
    category: HomeCardCategory,
    modifier: Modifier = Modifier,
) {
    val accentColor = category.accentColor()
    Box(
        modifier = modifier
            .clip(BadgeShape)
            .background(accentColor.copy(alpha = BadgeBackgroundAlpha))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Text(
            text = category.label,
            style = MinoAndroidTheme.typography.label2Medium,
            color = accentColor,
        )
    }
}

@Composable
private fun HomeCardImages(
    imageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(212.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(imageCount.coerceAtLeast(1)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(ImageShape)
                    .background(MinoAndroidTheme.colors.fillAlternative)
                    .height(212.dp),
            )
        }
    }
}

@Composable
private fun HomeCardCategory.accentColor(): Color =
    when (this) {
        HomeCardCategory.FriendsMostViewed -> MinoAndroidTheme.colors.accentForegroundLightBlue
        HomeCardCategory.MostTalked -> MinoAndroidTheme.colors.accentForegroundPink
        HomeCardCategory.MostSaved -> MinoAndroidTheme.colors.accentForegroundRedOrange
        HomeCardCategory.WorthVisiting -> MinoAndroidTheme.colors.accentForegroundLime
    }

private val CardShape = RoundedCornerShape(24.dp)
private val ImageShape = RoundedCornerShape(16.dp)
private val BadgeShape = RoundedCornerShape(8.dp)
private val BadgeBackgroundAlpha = 0.08f

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun HomeCardPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .background(MinoAndroidTheme.colors.backgroundNormalAlternative)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HomeCardCategory.entries.forEach { category ->
                HomeCard(
                    category = category,
                    title = "레이어스튜디오 10",
                    address = "서울 성동구 상원4길 10",
                )
            }
        }
    }
}
