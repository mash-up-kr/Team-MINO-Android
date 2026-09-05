package team.mino.feature.notifications.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import team.mino.core.common.kotlin.util.ElapsedTime
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.image.MinoAsyncImage
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.notifications.R
import team.mino.feature.notifications.main.model.NotificationItemUiModel
import team.mino.feature.notifications.main.model.NotificationThumbnail

/**
 * 알림 한 건. 썸네일·유형 문구·대상 이름·경과 시간 네 요소가 한 행에 놓인다(spec FR-002).
 *
 * **행 전체가 클릭 영역이다**(spec UX-005) — 썸네일을 누르든 시간을 누르든 [onClick] 하나로 모인다. 그래서
 * 클릭을 행 바깥 테두리에 걸고 안쪽 요소에는 따로 걸지 않는다.
 *
 * **유형 문구와 대상 이름은 각각 한 줄로 자른다**(spec UX-007). 넘치면 줄바꿈이 아니라 말줄임이라, 문구가
 * 길어져도 행 높이가 썸네일 높이로 묶여 목록의 모든 행이 같은 높이를 유지한다.
 *
 * **읽음 여부에 따른 차이를 두지 않는다**(spec FR-016·UX-009). 배경·색·굵기·점 어느 것으로도 갈리지 않으므로
 * 이 컴포저블은 읽음 상태를 인자로 받지 않는다.
 *
 * 유형 문구([NotificationItemUiModel.typeLabel])와 대상 이름은 **서버가 완성해 준 문자열을 그대로 그린다** —
 * 6종 문구를 클라이언트가 들고 있지 않다(`docs/specs/notifications/research.md` D4).
 */
@Composable
internal fun NotificationRow(
    item: NotificationItemUiModel,
    imageFallback: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MinoAndroidTheme.colors.backgroundNormalNormal)
            .rippleSingleClickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = RowHorizontalPadding, vertical = RowVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(ContentElapsedSpacing),
        verticalAlignment = Alignment.Top,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(ThumbnailTextSpacing),
            verticalAlignment = Alignment.Top,
        ) {
            NotificationRowThumbnail(thumbnail = item.thumbnail, imageFallback = imageFallback)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(TypeLabelTargetNameSpacing),
            ) {
                Text(
                    text = item.typeLabel,
                    color = MinoAndroidTheme.colors.labelNormal,
                    style = MinoAndroidTheme.typography.label1NormalBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.targetName,
                    color = MinoAndroidTheme.colors.labelNeutral,
                    style = MinoAndroidTheme.typography.label1NormalRegular,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            modifier = Modifier.width(ElapsedWidth),
            text = elapsedLabel(item.elapsed),
            color = MinoAndroidTheme.colors.labelAlternative,
            style = MinoAndroidTheme.typography.caption1Regular,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
    }
}

/**
 * 행 왼쪽 그림. 두 갈래가 서로 다른 그림이라 하나로 합치지 않는다
 * (`docs/specs/notifications/data-model.md` §2.2).
 *
 * - [NotificationThumbnail.Image]는 서버가 준 주소를 그대로 띄우고, 주소가 없거나 로딩에 실패하면 자리표시자
 *   글리프로 대신한다(spec TS-054). 방 목록을 불러 합성하지 않는다.
 * - [NotificationThumbnail.SaveError]는 유형이 정하는 **고정 이미지**라 서버 값을 보지 않는다(spec TS-010).
 *
 * 자리표시자의 생김새는 디자인에 상태가 없어 [MinoAsyncImage]를 쓰는 다른 목록과 같은 것으로 맞췄다.
 *
 * [imageFallback]을 밖에서 받는 것은 `rememberVectorPainter`가 페인터마다 서브컴포지션을 하나씩 세우기
 * 때문이다. 행 안에서 만들면 화면에 살아 있는 행 수만큼 벡터 컴포지션이 생기고 행이 재활용될 때마다 다시
 * 선다(같은 이유로 호이스팅한 선례가 `feature/room`의 `PlaceImage`다). 페인터는 상태가 없어 행끼리 나눠 써도
 * 안전하다.
 */
@Composable
private fun NotificationRowThumbnail(
    thumbnail: NotificationThumbnail,
    imageFallback: Painter,
    modifier: Modifier = Modifier,
) {
    when (thumbnail) {
        is NotificationThumbnail.Image ->
            MinoAsyncImage(
                imageUrl = thumbnail.url,
                fallback = imageFallback,
                fallbackTint = MinoAndroidTheme.colors.labelAssistive,
                modifier = modifier
                    .size(ThumbnailSize)
                    .clip(ImageThumbnailShape)
                    .background(MinoAndroidTheme.colors.fillAlternative),
                fallbackModifier = Modifier.padding(ThumbnailSize / 4),
            )

        NotificationThumbnail.SaveError ->
            Image(
                painter = painterResource(R.drawable.notification_thumbnail_save_error),
                contentDescription = null,
                modifier = modifier
                    .size(ThumbnailSize)
                    .clip(SaveErrorThumbnailShape)
                    .border(
                        width = SaveErrorThumbnailBorderWidth,
                        color = MinoAndroidTheme.colors.lineNormalNeutral,
                        shape = SaveErrorThumbnailShape,
                    ),
                contentScale = ContentScale.Crop,
            )
    }
}

/**
 * 고른 구간에 문구를 입힌다(spec FR-003).
 *
 * 구간 판정(`elapsedTime`)과 갈라 둔 것은 판정만 순수 함수로 남기기 위해서다 — 문구는 리소스를 읽어야 해서
 * 컴포지션 밖에서 부를 수 없고, 그래서 화면 모델도 문자열이 아니라 갈래를 든다
 * (`docs/specs/notifications/data-model.md` §2.1.1).
 */
@Composable
private fun elapsedLabel(elapsed: ElapsedTime): String =
    when (elapsed) {
        ElapsedTime.JustNow -> stringResource(R.string.notification_elapsed_just_now)
        is ElapsedTime.HoursAgo ->
            stringResource(R.string.notification_elapsed_hours_ago, elapsed.hours)

        is ElapsedTime.DaysAgo ->
            stringResource(R.string.notification_elapsed_days_ago, elapsed.days)

        is ElapsedTime.AbsoluteDate ->
            stringResource(R.string.notification_elapsed_date, elapsed.month, elapsed.day)
    }

private val RowHorizontalPadding = 20.dp

private val RowVerticalPadding = 12.dp

private val ContentElapsedSpacing = 24.dp

private val ThumbnailTextSpacing = 12.dp

private val TypeLabelTargetNameSpacing = 4.dp

private val ElapsedWidth = 48.dp

private val ThumbnailSize = 56.dp

// Figma Radius 변수 대응 — 토큰 미존재. 변수는 원본 크기의 컴포넌트에 걸려 있고 이 행은 그보다 작은
// 인스턴스라, 실제로 렌더되는 반경은 [ThumbnailSize] 축소 비율만큼 줄어든 값이다
private val ImageThumbnailShape: Shape = RoundedCornerShape(9.8.dp)

private val SaveErrorThumbnailShape: Shape = RoundedCornerShape(12.dp)

private val SaveErrorThumbnailBorderWidth = 1.dp

/**
 * 미리보기 표본. 컴포저블 밖에 두는 것은 [NotificationThumbnail.Image]를 부르는 자리를 컴포지션에서 빼기
 * 위해서다 — 이름이 같은 컴포저블이 있어 컴포지션 안에서는 Compose Lint가 UI를 그리는 호출로 읽는다.
 */
private val ImageThumbnailSample = NotificationItemUiModel(
    id = "1",
    typeLabel = "이미 저장해둔 곳이에요",
    targetName = "연남동 스탠딩 커피",
    elapsed = ElapsedTime.HoursAgo(1),
    thumbnail = NotificationThumbnail.Image(url = null),
)

private val SaveErrorSample = NotificationItemUiModel(
    id = "2",
    typeLabel = "장소를 저장하지 못했어요. 잠시 후 다시 시도해주세요",
    targetName = "잠시 후 다시 시도해주세요",
    elapsed = ElapsedTime.AbsoluteDate(year = 2027, month = 8, day = 10),
    thumbnail = NotificationThumbnail.SaveError,
)

/** 장소를 가리키는 알림 — 주소를 못 받았을 때의 자리표시자와 `N시간 전` 갈래를 함께 본다. */
@UiModePreviews
@Composable
private fun NotificationRowPreview() {
    MinoAndroidAppTheme {
        NotificationRow(
            item = ImageThumbnailSample,
            imageFallback = rememberVectorPainter(MinoIcons.Image),
            onClick = {},
        )
    }
}

/** 저장 오류 알림 — 고정 오류 아이콘과, 문구가 길어졌을 때의 말줄임을 함께 본다. */
@UiModePreviews
@Composable
private fun NotificationRowSaveErrorPreview() {
    MinoAndroidAppTheme {
        NotificationRow(
            item = SaveErrorSample,
            imageFallback = rememberVectorPainter(MinoIcons.Image),
            onClick = {},
        )
    }
}
