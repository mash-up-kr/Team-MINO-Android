package team.mino.feature.room.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.component.roomcard.MinoRoomCard
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Close
import team.mino.core.designsystem.foundation.icons.icons.Plus
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.domain.model.Room
import team.mino.feature.room.main.model.BottomSheetLevel
import team.mino.feature.room.main.model.toRoomCardParams

/**
 * 방 리스트 시트 [FR-002] 높이 고정값. 화면 비율이 아닌 dp 값을 그대로 쓴다(UX-001).
 *
 * 드래그 제스처 인식·전환 애니메이션 자체는 이 spec의 범위 밖이다([spec.md §3.2] — 디자인 시스템 공용
 * 컴포넌트(이슈 #144)가 아직 없어, 여기서는 표준 Compose 드래그 감지로 discrete up/down 이벤트만
 * 판정하는 최소 구현을 둔다.
 */
private object RoomListBottomSheetTokens {
    val PeekHeight = 88.dp
    val HalfHeightNoGroupRoom = 256.dp
    val HalfHeightOneGroupRoom = 360.dp
    val HalfHeightManyGroupRooms = 380.dp
    val Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val FullShape = RoundedCornerShape(0.dp)
    val HandleWidth = 36.dp
    val HandleHeight = 4.dp
    val DragThreshold = 24.dp
    val RoomCardHorizontalPadding = 20.dp
}

private fun halfHeight(groupRoomCount: Int): Dp =
    when {
        groupRoomCount <= 0 -> RoomListBottomSheetTokens.HalfHeightNoGroupRoom
        groupRoomCount == 1 -> RoomListBottomSheetTokens.HalfHeightOneGroupRoom
        else -> RoomListBottomSheetTokens.HalfHeightManyGroupRooms
    }

/**
 * `Peek`/`Half`에서 시트가 차지하는 높이(`Full`은 화면을 채워 지도 위 컨트롤 자체가 숨겨지므로 `null`).
 * Figma 003-2-1/003-1-2/003-2-2 대조 결과 현재 위치 버튼이 이 높이 + 20dp 위에 뜬다 — [RoomListScreen]이
 * 버튼 위치를 시트 높이에 맞춰 띄우도록 이 값을 공유한다.
 */
internal fun bottomSheetHeightOrNull(
    sheetLevel: BottomSheetLevel,
    groupRoomCount: Int,
): Dp? =
    when (sheetLevel) {
        BottomSheetLevel.PEEK -> RoomListBottomSheetTokens.PeekHeight
        BottomSheetLevel.HALF -> halfHeight(groupRoomCount)
        BottomSheetLevel.FULL -> null
    }

/**
 * 방 리스트 지도 위 3단(`Peek`/`Half`/`Full`) 바텀시트. 별도 Route가 아니라 [BottomSheetLevel] 상태
 * 하나로 세 단계를 표현한다(research.md D2).
 *
 * @param groupRoomCount `Half` 고정 높이 산정 기준(FR-002).
 * @param content `Half`/`Full`에서 헤더 아래에 그릴 방 카드 목록([RoomListRoomCardList], FR-004). `Half`는
 *   고정 높이([halfHeight])로 잘려 스크롤 어포던스를 암시하고(UX-003), `Full`은 전체를 스크롤로 훑는다 —
 *   같은 콘텐츠를 시트 높이 제약만 다르게 적용해 재사용한다.
 */
@Composable
internal fun RoomListBottomSheet(
    sheetLevel: BottomSheetLevel,
    groupRoomCount: Int,
    onDraggedUp: () -> Unit,
    onDraggedDown: () -> Unit,
    onAddRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    val heightModifier = when (sheetLevel) {
        BottomSheetLevel.PEEK -> Modifier.height(RoomListBottomSheetTokens.PeekHeight)
        BottomSheetLevel.HALF -> Modifier.height(halfHeight(groupRoomCount))
        BottomSheetLevel.FULL -> Modifier.fillMaxSize()
    }
    // Full은 화면 전체를 덮어 뒤 배경(지도)이 보이지 않으므로 둥근 모서리를 두지 않는다(Figma
    // 003-1-3/003-2-3 대조) — Peek/Half만 지도 위에 떠 있는 카드 형태라 위쪽 모서리를 둥글린다.
    val shape = if (sheetLevel == BottomSheetLevel.FULL) {
        RoomListBottomSheetTokens.FullShape
    } else {
        RoomListBottomSheetTokens.Shape
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(heightModifier)
            .clip(shape)
            .background(MinoAndroidTheme.colors.backgroundElevatedNormal),
    ) {
        RoomListBottomSheetHeader(
            sheetLevel = sheetLevel,
            onAddRoomClick = onAddRoomClick,
            onDraggedUp = onDraggedUp,
            onDraggedDown = onDraggedDown,
        )
        if (sheetLevel != BottomSheetLevel.PEEK) {
            content()
        }
    }
}

@Composable
private fun RoomListBottomSheetHeader(
    sheetLevel: BottomSheetLevel,
    onAddRoomClick: () -> Unit,
    onDraggedUp: () -> Unit,
    onDraggedDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(onDraggedUp, onDraggedDown) {
                var accumulatedDrag = 0f
                val thresholdPx = RoomListBottomSheetTokens.DragThreshold.toPx()
                detectVerticalDragGestures(
                    onDragStart = { accumulatedDrag = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        accumulatedDrag += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        when {
                            accumulatedDrag <= -thresholdPx -> onDraggedUp()
                            accumulatedDrag >= thresholdPx -> onDraggedDown()
                        }
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
                .size(width = RoomListBottomSheetTokens.HandleWidth, height = RoomListBottomSheetTokens.HandleHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(MinoAndroidTheme.colors.lineSolidNeutral),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "방 리스트",
                color = MinoAndroidTheme.colors.labelNormal,
                style = MinoAndroidTheme.typography.title3Bold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .rippleSingleClickable(onClick = onAddRoomClick),
                    imageVector = MinoIcons.Plus,
                    contentDescription = null,
                    tint = MinoAndroidTheme.colors.labelNormal,
                )
                if (sheetLevel == BottomSheetLevel.FULL) {
                    Icon(
                        modifier = Modifier
                            .size(24.dp)
                            .rippleSingleClickable(onClick = onDraggedDown),
                        imageVector = MinoIcons.Close,
                        contentDescription = null,
                        tint = MinoAndroidTheme.colors.labelNormal,
                    )
                }
            }
        }
    }
}

/**
 * `Full`(및 `Half`)에서 그리는 방 카드 목록 — 개인방(`내 장소`)을 최상단에 고정하고 공동방이 이어진다
 * (FR-004). `Room` → `MinoRoomCard` 파라미터 매핑은 [toRoomCardParams]가 소유한다(T041, ADR
 * 2026-08-18 「결정」 — `MinoRoomCard`는 stateless라 도메인 모델을 모른다).
 *
 * @param personalRoom `null`이면 아직 개인방 정보가 준비되지 않은 상태(EC-001은 `placeCount == 0`인
 *   개인방 자체는 항상 존재한다고 전제 — PRD 「개인방」정의, 이 화면은 그 값을 그대로 그린다).
 * @param showGhostCard [RoomGhostCard] 노출 여부(FR-009, `groupRooms.isEmpty()` 파생값). `true`면
 *   개인방 다음 자리(EC-003 — 공동방이 없을 때 목록에 남는 유일한 항목)에 그린다.
 */
@Composable
internal fun RoomListRoomCardList(
    personalRoom: Room?,
    groupRooms: ImmutableList<Room>,
    showGhostCard: Boolean,
    onRoomCardClick: (String) -> Unit,
    onGhostCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        personalRoom?.let { room ->
            item(key = room.id) {
                RoomListRoomCard(
                    room = room,
                    onClick = { onRoomCardClick(room.id) },
                )
            }
        }
        if (showGhostCard) {
            item(key = "ghost-card") {
                RoomGhostCard(
                    modifier = Modifier.padding(horizontal = RoomListBottomSheetTokens.RoomCardHorizontalPadding),
                    onClick = onGhostCardClick,
                )
            }
        }
        items(items = groupRooms, key = { it.id }) { room ->
            RoomListRoomCard(
                room = room,
                onClick = { onRoomCardClick(room.id) },
            )
        }
    }
}

@Composable
private fun RoomListRoomCard(
    room: Room,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val params = remember(room) { room.toRoomCardParams() }
    MinoRoomCard(
        title = params.title,
        placeCountLabel = params.placeCountLabel,
        participantImageUrls = params.participantImageUrls,
        onClick = onClick,
        modifier = modifier.padding(horizontal = RoomListBottomSheetTokens.RoomCardHorizontalPadding),
        coverImageUrl = params.coverImageUrl,
        memo = params.memo,
    )
}
