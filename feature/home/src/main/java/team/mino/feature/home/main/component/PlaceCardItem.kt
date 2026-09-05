package team.mino.feature.home.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.avatar.MinoAvatar
import team.mino.core.designsystem.component.avatar.MinoAvatarSize
import team.mino.core.designsystem.component.avatar.MinoAvatarVariant
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.foundation.icons.icons.MoreVertical
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.image.MinoAsyncImage
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.PlaceCard
import team.mino.core.domain.model.PlaceLabel
import team.mino.core.domain.model.ProfileAvatar
import team.mino.core.domain.model.Registrant
import team.mino.feature.home.R

/**
 * 덱을 이루는 장소 카드 한 장.
 *
 * 헤더(등록자 아바타 + 장소분류 라벨 뱃지 + `[...]`) · 장소명 · 주소 · 대표 이미지 2칸으로 구성된다.
 * **저장 경과일을 표시하지 않는다**(spec §2.3) — [PlaceCard]에 그 값 자체가 없다.
 *
 * 폭은 호출자가 정한다. 높이는 내용으로 결정되며, 카드 스택이 뒷장을 줄여 그리는 것도 호출자 몫이다.
 *
 * 등록자 정보가 없는 카드(탈퇴 등)는 `DeckMapper`가 빈 [Registrant]로 흡수해 넘긴다. 이때도 **아바타 자리를
 * 비우지 않고** 기본 글리프를 그린다 — 자리를 지우면 라벨 뱃지가 왼쪽으로 밀려 카드 구조가 달라진다.
 * 닉네임이 없으면 접근성 설명만 생략한다.
 *
 * @param card 그릴 카드. 이 컴포저블은 상태를 갖지 않는다.
 * @param isActionMenuOpen 이 카드의 액션 메뉴가 열려 있는가. 판정은 호출자가 하고 여기서는 그리기만 한다.
 * @param onMoreClick `[...]` 클릭. `null`이면 클릭 영역을 **아예 붙이지 않는다** — 덱의 뒷장이나 가이드
 *  사본처럼 보여 주기만 하는 자리에 쓴다. 클릭 영역이 있으면 그 위에서 시작한 제스처의 down을 먼저
 *  삼키므로, 조작을 받지 않는 카드는 영역 자체를 두지 않는다.
 * @param onSaveToAnotherRoom 액션 메뉴의 `다른 방 저장` 선택.
 * @param onDismissActionMenu 메뉴 바깥 탭·뒤로가기. 스와이프로 닫는 경로는
 *  [team.mino.feature.home.main.vm.HomeViewModel]이 판정하므로 여기서 다시 만들지 않는다.
 */
@Composable
internal fun PlaceCardItem(
    card: PlaceCard,
    isActionMenuOpen: Boolean,
    onMoreClick: (() -> Unit)?,
    onSaveToAnotherRoom: () -> Unit,
    onDismissActionMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MinoAndroidTheme.colors.backgroundNormalNormal)
            .border(CardBorderWidth, MinoAndroidTheme.colors.backgroundNormalAlternative, CardShape)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PlaceCardHeader(
            card = card,
            isActionMenuOpen = isActionMenuOpen,
            onMoreClick = onMoreClick,
            onSaveToAnotherRoom = onSaveToAnotherRoom,
            onDismissActionMenu = onDismissActionMenu,
        )
        PlaceCardImageGrid(
            firstImageUrl = card.imageUrls.getOrNull(0),
            secondImageUrl = card.imageUrls.getOrNull(1),
        )
    }
}

@Composable
private fun PlaceCardHeader(
    card: PlaceCard,
    isActionMenuOpen: Boolean,
    onMoreClick: (() -> Unit)?,
    onSaveToAnotherRoom: () -> Unit,
    onDismissActionMenu: () -> Unit,
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
                    contentDescription = card.registrant.nickname.ifEmpty { null },
                )
                PlaceLabelBadge(label = card.label)
            }
            // 메뉴는 이 Box를 앵커로 잡는다 — `[...]` 바로 아래에서 열려야 어느 카드의 메뉴인지 드러난다(spec UX-002).
            Box {
                val clickable = if (onMoreClick != null) {
                    Modifier.rippleSingleClickable(onClick = onMoreClick)
                } else {
                    Modifier
                }
                Icon(
                    imageVector = MinoIcons.MoreVertical,
                    contentDescription = stringResource(R.string.home_card_more),
                    tint = MinoAndroidTheme.colors.labelNormal,
                    modifier = Modifier
                        .clip(CircleShape)
                        .then(clickable)
                        .padding(7.dp)
                        .size(18.dp),
                )
                if (isActionMenuOpen) {
                    CardActionMenu(
                        onSaveToAnotherRoom = onSaveToAnotherRoom,
                        onDismissRequest = onDismissActionMenu,
                    )
                }
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = card.placeName,
                style = MinoAndroidTheme.typography.body1NormalBold,
                color = MinoAndroidTheme.colors.labelNormal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = card.address,
                style = MinoAndroidTheme.typography.label2Regular,
                color = MinoAndroidTheme.colors.labelAlternative,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 장소분류 라벨 뱃지. 라벨은 항상 존재하고 4종 중 1종이라(FR-008) 「라벨 없음」 자리를 두지 않는다.
 *
 * design-system `MinoContentBadge`의 `Accent`는 Cyan 한 색으로 고정돼 4색을 표현할 수 없어 여기서 조립한다.
 * Content Badge의 `Color` 축이 넓어지면 이 뱃지를 그쪽으로 옮긴다.
 */
@Composable
private fun PlaceLabelBadge(
    label: PlaceLabel,
    modifier: Modifier = Modifier,
) {
    val accentColor = label.accentColor()
    Box(
        modifier = modifier
            .clip(BadgeShape)
            .background(accentColor.copy(alpha = BADGE_BACKGROUND_ALPHA))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Text(
            text = label.text,
            style = MinoAndroidTheme.typography.label2Medium,
            color = accentColor,
        )
    }
}

/**
 * 대표 이미지 2칸. 칸 수는 디자인이 2로 고정했고, 이미지가 모자란 칸은 빈 자리로 남는다.
 *
 * 이미지가 없거나 로딩에 실패한 칸의 표시는 디자인에 정의가 없어, 같은 글리프 하나로 두 경우를 함께 다룬다.
 */
@Composable
private fun PlaceCardImageGrid(
    firstImageUrl: String?,
    secondImageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ImageGridHeight),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PlaceCardImageSlot(imageUrl = firstImageUrl, modifier = Modifier.weight(1f))
        PlaceCardImageSlot(imageUrl = secondImageUrl, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PlaceCardImageSlot(
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(ImageShape)
            .background(MinoAndroidTheme.colors.fillAlternative),
    ) {
        MinoAsyncImage(
            imageUrl = imageUrl,
            fallback = rememberVectorPainter(MinoIcons.Image),
            fallbackTint = MinoAndroidTheme.colors.labelAssistive,
            modifier = Modifier.fillMaxSize(),
            fallbackModifier = Modifier.wrapContentSize().size(ImageFallbackGlyphSize),
        )
    }
}

/** 라벨 표시 문구. 서버 식별자와의 대응은 `DeckMapper`가, 문구는 화면이 소유한다(spec FR-008). */
private val PlaceLabel.text: String
    get() =
        when (this) {
            PlaceLabel.WORTH_VISITING -> "가볼 만한 곳"
            PlaceLabel.MANY_SAVES -> "여럿이 저장한 곳"
            PlaceLabel.MANY_COMMENTS -> "이야기 많은 곳"
            PlaceLabel.MANY_VIEWS -> "친구들이 많이 본 곳"
        }

@Composable
private fun PlaceLabel.accentColor(): Color =
    when (this) {
        PlaceLabel.WORTH_VISITING -> MinoAndroidTheme.colors.accentForegroundLime
        PlaceLabel.MANY_SAVES -> MinoAndroidTheme.colors.accentForegroundRedOrange
        PlaceLabel.MANY_COMMENTS -> MinoAndroidTheme.colors.accentForegroundPink
        PlaceLabel.MANY_VIEWS -> MinoAndroidTheme.colors.accentForegroundLightBlue
    }

private val CardShape = RoundedCornerShape(24.dp)
private val CardBorderWidth = 1.dp
private val BadgeShape = RoundedCornerShape(8.dp)

// Figma Opacity/8 변수 대응 — 토큰 미존재
private const val BADGE_BACKGROUND_ALPHA = 0.08f
private val ImageShape = RoundedCornerShape(16.dp)
private val ImageGridHeight = 184.dp

/** 빈 칸 글리프 크기. 디자인에 빈 칸이 없어 근거가 되는 값이 없다. */
private val ImageFallbackGlyphSize = 24.dp

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun PlaceCardItemPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .background(MinoAndroidTheme.colors.backgroundNormalAlternative)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlaceLabel.entries.forEach { label ->
                PlaceCardItem(
                    card = PlaceCard(
                        pinId = "pin-${label.ordinal}",
                        placeName = "레이어스튜디오 10",
                        address = "서울 성동구 상원4길 10",
                        imageUrls = emptyList(),
                        label = label,
                        registrant = Registrant(userId = "u1", nickname = "미노", avatar = ProfileAvatar.Person1),
                    ),
                    isActionMenuOpen = false,
                    onMoreClick = {},
                    onSaveToAnotherRoom = {},
                    onDismissActionMenu = {},
                )
            }
        }
    }
}
