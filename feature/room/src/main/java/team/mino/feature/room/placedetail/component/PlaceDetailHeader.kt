package team.mino.feature.room.placedetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.avatar.MinoAvatar
import team.mino.core.designsystem.component.avatar.MinoAvatarSize
import team.mino.core.designsystem.component.avatar.MinoAvatarVariant
import team.mino.core.designsystem.component.button.MinoOutlinedIconButton
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Close
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.room.R

/**
 * 시트 맨 위에서 이 장소가 무엇인지 알려 주는 확장형 헤더.
 *
 * 첫 줄은 등록자 아바타와 등록자 닉네임, 그리고 오른쪽 끝의 [나가기]다. 그 아래 장소명과 주소가 각각 한 줄을
 * 차지한다(spec FR-003).
 *
 * **첫 줄은 「누가 이 장소를 담았는가」만 말한다.** 아바타와 닉네임이 한 사람을 가리키고, 장소의 성격을 판정한
 * 장소분류 라벨은 그 옆에 두지 않는다 — 라벨은 홈 카드 한 곳에만 붙는다(spec UX-014 · FR-005 · EC-005).
 *
 * **닉네임·장소명·주소는 아무리 길어도 줄을 늘리지 않는다.** 넘치는 글자는 `...`으로 접힌다
 * (spec FR-004 · FR-005) — 시트 높이가 콘텐츠 길이에 흔들리지 않아야 하기 때문이다(spec SC-002).
 *
 * **손잡이도 위쪽 여백도 그리지 않는다.** 시트 상단의 손잡이와 헤더 위의 여백은 모두 `PlaceDetailSheet`이
 * 소유한다 — 단계에 따라 그 자리를 손잡이가 채우기도 하고 빈 띠가 채우기도 해서, 헤더가 자기 위쪽 여백까지 들면
 * 두 번 벌어진다.
 *
 * @param registrantNickname `null`이 등록자가 없는 장소다. 아바타 자리는 비우지 않고 기본 아바타가 채운다
 *  (spec EC-004).
 */
@Composable
internal fun PlaceDetailExpandedHeader(
    name: String,
    address: String,
    registrantNickname: String?,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .surface(
                    shape = RectangleShape,
                    containerColor = MinoAndroidTheme.colors.backgroundNormalNormal,
                ).padding(
                    start = HorizontalPadding,
                    end = HorizontalPadding,
                    bottom = SummaryRowBottomPadding,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Registrant(
                nickname = registrantNickname,
                modifier = Modifier.weight(1f, fill = false),
            )

            MinoOutlinedIconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = MinoIcons.Close,
                    contentDescription = stringResource(R.string.placedetail_header_close),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .surface(
                    shape = RectangleShape,
                    containerColor = MinoAndroidTheme.colors.backgroundNormalNormal,
                ).padding(
                    start = HorizontalPadding,
                    end = HorizontalPadding,
                    bottom = TitleBlockBottomPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(TitleBlockSpacing),
        ) {
            Text(
                text = name,
                style = MinoAndroidTheme.typography.title3Bold,
                color = MinoAndroidTheme.colors.labelStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = address,
                style = MinoAndroidTheme.typography.label1NormalRegular,
                color = MinoAndroidTheme.colors.labelNeutral,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 콘텐츠가 스크롤된 동안 시트 위쪽에 남는 축소형 헤더.
 *
 * 장소명과 [나가기]만 남고 등록자 아바타·등록자 닉네임·주소는 빠진다(spec UX-005). 그래도 [나가기]는 확장형과
 * 같은 자리인 헤더 우측에 있어, 스크롤 상태와 무관하게 같은 곳을 누르면 된다(spec UX-002 · EC-006).
 *
 * **스크롤 축 밖에 선다.** `PlaceDetailSheet`의 고정 자리에 넘겨져 콘텐츠와 함께 밀려 올라가지 않는다
 * (spec FR-008). 언제 이 헤더로 바뀌는지는 화면이 정하고, 이 컴포저블은 그 결정을 판정하지 않는다.
 *
 * **장소명은 아무리 길어도 한 줄이다.** 남는 폭을 다 쓰고 넘치는 글자는 `...`으로 접힌다(spec FR-004).
 */
@Composable
internal fun PlaceDetailCollapsedHeader(
    name: String,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .surface(
                shape = RectangleShape,
                containerColor = MinoAndroidTheme.colors.backgroundNormalNormal,
            ).padding(
                start = HorizontalPadding,
                end = HorizontalPadding,
                bottom = SummaryRowBottomPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(TitleCloseSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MinoAndroidTheme.typography.title3Bold,
            color = MinoAndroidTheme.colors.labelStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        MinoOutlinedIconButton(onClick = onCloseClick) {
            Icon(
                imageVector = MinoIcons.Close,
                contentDescription = stringResource(R.string.placedetail_header_close),
            )
        }
    }
}

/**
 * 이 장소를 담은 사람 — 아바타와 닉네임 한 쌍.
 *
 * **[나가기] 자리를 침범하지 않는다.** 호출부가 넘긴 [modifier]의 가중치가 이 행의 상한을 남는 폭으로 묶고,
 * 닉네임은 그 안에서 한 줄을 유지한 채 넘치는 글자를 `...`으로 접는다(spec TS-009). 시안의 확장형 헤더는 이
 * 자리를 고정 폭으로 잡아 두었으나, 고정 폭은 기기 폭이 달라지면 [나가기]와의 간격이 함께 흔들려 옮기지 않았다.
 *
 * **등록자가 있어도 지금은 기본 아바타가 그려진다.** 핀 상세 응답이 아바타를 색으로만 주어 어떤 그림인지 고를 수
 * 없기 때문이며, 어느 표현이 정본인지는 서버 협의 항목으로 남아 있다
 * (`docs/specs/place-detail/contracts/place-api.md` §5). 등록자가 아예 없을 때 기본 아바타를 두는 것은 그와
 * 별개로 확정된 규칙이라(spec EC-004) 협의가 닫혀도 이 자리는 비지 않는다.
 */
@Composable
private fun Registrant(
    nickname: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AvatarNicknameSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MinoAvatar(
            variant = MinoAvatarVariant.Person,
            size = MinoAvatarSize.Small,
            contentDescription = nickname?.let {
                stringResource(R.string.placedetail_header_registrant_avatar, it)
            },
        )
        if (nickname != null) {
            Text(
                text = nickname,
                modifier = Modifier.weight(1f, fill = false),
                style = MinoAndroidTheme.typography.label1NormalMedium,
                color = MinoAndroidTheme.colors.labelNeutral,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val HorizontalPadding = 20.dp

private val SummaryRowBottomPadding = 18.dp

private val TitleCloseSpacing = 40.dp

private val AvatarNicknameSpacing = 6.dp

private val TitleBlockSpacing = 4.dp

private val TitleBlockBottomPadding = 12.dp

@UiModePreviews
@Composable
private fun PlaceDetailExpandedHeaderPreview() {
    MinoAndroidAppTheme {
        PlaceDetailExpandedHeader(
            name = "레이어스튜디오 10",
            address = "서울 성동구 상원4길 10",
            registrantNickname = "닉네임닉네임닉네임닉네임닉네임",
            onCloseClick = {},
        )
    }
}

@UiModePreviews
@Composable
private fun PlaceDetailExpandedHeaderNoRegistrantPreview() {
    MinoAndroidAppTheme {
        PlaceDetailExpandedHeader(
            name = "레이어스튜디오 10",
            address = "서울 성동구 상원4길 10",
            registrantNickname = null,
            onCloseClick = {},
        )
    }
}

@UiModePreviews
@Composable
private fun PlaceDetailCollapsedHeaderPreview() {
    MinoAndroidAppTheme {
        PlaceDetailCollapsedHeader(
            name = "레이어스튜디오 10",
            onCloseClick = {},
        )
    }
}

@UiModePreviews
@Composable
private fun PlaceDetailCollapsedHeaderOverflowPreview() {
    MinoAndroidAppTheme {
        PlaceDetailCollapsedHeader(
            name = "성수동 골목 안쪽에 숨어 있는 통유리 루프탑 브런치 카페 미노스테이션 2호점",
            onCloseClick = {},
        )
    }
}

@UiModePreviews
@Composable
private fun PlaceDetailExpandedHeaderOverflowPreview() {
    MinoAndroidAppTheme {
        PlaceDetailExpandedHeader(
            name = "성수동 골목 안쪽에 숨어 있는 통유리 루프탑 브런치 카페 미노스테이션 2호점",
            address = "서울특별시 성동구 아차산로17길 48 성수낙낙 지하 1층 101호 (성수동2가)",
            registrantNickname = "성수동골목산책러닉네임최대",
            onCloseClick = {},
        )
    }
}
