package team.mino.feature.roomform.form.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.actionarea.ActionAreaAction
import team.mino.core.designsystem.component.actionarea.MinoActionArea
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.component.textinput.MinoTextArea
import team.mino.core.designsystem.component.textinput.MinoTextField
import team.mino.core.designsystem.component.textinput.MinoTextFieldStatus
import team.mino.core.designsystem.component.topnavigation.MinoTopNavigation
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomNameValidation
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.roomform.R
import team.mino.feature.roomform.form.component.RoomColorPalette
import team.mino.feature.roomform.form.component.RoomFormConfirmDialog
import team.mino.feature.roomform.form.component.RoomPreviewCard
import team.mino.feature.roomform.form.model.RoomFormDialog
import team.mino.feature.roomform.form.model.RoomFormMode
import team.mino.feature.roomform.form.vm.RoomFormIntent
import team.mino.feature.roomform.form.vm.RoomFormUiState
import team.mino.core.common.ui.R as CommonUiR

/**
 * 공동방 생성·편집 폼 화면.
 *
 * 입력 영역만 스크롤하고 CTA는 하단에 고정된다 — 폼 어디를 보고 있든 저장 수단이 사라지지 않는다.
 * 고정된 액션 영역은 스크롤 영역을 밀어내지 않고 그 **위에 겹친다.** 액션 영역이 자기 위쪽 가장자리에
 * 페이드를 갖고 있어, 지나가는 콘텐츠가 그 아래로 흐려지며 사라져야 하기 때문이다. 대신 스크롤 영역은
 * 겹친 높이만큼 아래쪽 여백을 더 얻어, 끝까지 내렸을 때 마지막 입력이 액션 영역에 가리지 않는다.
 *
 * `Scaffold`를 열지 않는다. chrome과 인셋은 셸이 소유하고, 이 화면은 셸이 내준 영역 안을 그린다.
 * 상단 내비게이션만은 화면 고유 chrome이라 여기서 직접 배치한다.
 *
 * 상단 내비게이션 아래는 셋 중 하나만 그린다 — 폼·로딩·로드 실패. 편집 진입의 초기값 조회는 폼이
 * 읽는 주 데이터라, 아직 오지 않았거나 실패한 값을 빈 폼으로 위장하면 사용자가 그 빈칸을 자기 방의
 * 현재 값으로 읽고 그대로 저장하게 된다. 실패는 화면 전체를 재시도 가능한 오류로 바꾼다(에러 처리
 * 규약 §5). 생성 진입에는 조회가 없어 두 갈래 모두 성립하지 않는다.
 *
 * 화면을 벗어날 길은 어느 갈래에서도 남으므로 상단 내비게이션은 분기 밖에 둔다.
 *
 * 그 벗어날 길이 무엇인지는 진입 맥락으로 갈린다. 온보딩은 아직 가입 절차 안이라 돌아갈 이전 화면이
 * 없고 대신 절차를 건너뛸 수단을 준다. 그 밖의 진입은 반대다 — 온 곳이 있으니 돌아갈 수 있고,
 * 건너뛸 절차는 없다. 둘이 함께 보이는 상태는 없다.
 *
 * @param descriptionState 방 설명의 편집 버퍼. 소유자는 `RoomFormRoute`다 — 이 화면은 받아서
 *  [MinoTextArea]에 넘기기만 하므로 stateless로 남는다. 30자 상한을 자르는 것도 그 컴포넌트라
 *  여기서 다시 자르지 않는다.
 */
@Composable
internal fun RoomFormScreen(
    state: RoomFormUiState,
    descriptionState: TextFieldState,
    onIntent: (RoomFormIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MinoAndroidTheme.colors.backgroundNormalNormal),
    ) {
        TopBar(
            title = stringResource(state.mode.titleRes),
            onBackClick = if (state.isOnboarding) {
                null
            } else {
                { onIntent(RoomFormIntent.BackClicked) }
            },
            actionLabel = if (state.isOnboarding) {
                stringResource(R.string.roomform_action_skip)
            } else {
                null
            },
            onActionClick = { onIntent(RoomFormIntent.SkipClicked) },
        )
        Box(modifier = Modifier.weight(1f)) {
            when {
                // 오류가 로딩보다 앞선다. 재시도가 로딩을 켜면서 오류를 지우므로 둘이 함께 참인
                // 순간은 없지만, 순서를 정해야 한다면 사용자가 할 일이 있는 쪽이 이겨야 한다.
                state.loadError != null ->
                    LoadErrorContent(
                        error = state.loadError,
                        onRetryClick = { onIntent(RoomFormIntent.RetryLoad) },
                        modifier = Modifier.fillMaxSize(),
                    )

                state.isLoading -> LoadingContent(modifier = Modifier.fillMaxSize())

                else ->
                    FormContent(
                        state = state,
                        descriptionState = descriptionState,
                        onIntent = onIntent,
                    )
            }
        }

        // 모달은 자기 창에 그려져 이 `Column`의 자리를 차지하지 않는다. 딤도 그 창이 직접 깔아
        // 화면이 따로 덮지 않는다.
        state.dialog?.let { dialog ->
            RoomFormConfirmDialog(
                title = stringResource(dialog.titleRes),
                cancelLabel = stringResource(R.string.roomform_dialog_cancel),
                confirmLabel = stringResource(dialog.confirmLabelRes),
                onConfirm = { onIntent(dialog.confirmIntent) },
                onDismiss = { onIntent(RoomFormIntent.DialogDismissed) },
            )
        }
    }
}

/**
 * 입력 영역과 그 위에 겹치는 액션 영역.
 *
 * 액션 영역의 높이를 재서 스크롤 영역의 아래쪽 여백으로 되먹인다 — 겹쳐 놓았기 때문에 그만큼을
 * 돌려주지 않으면 끝까지 내렸을 때 마지막 입력이 가린다. CTA 라벨이 갈리며 높이가 달라질 수 있어
 * 상수로 적어 두지 않는다.
 */
@Composable
private fun FormContent(
    state: RoomFormUiState,
    descriptionState: TextFieldState,
    onIntent: (RoomFormIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var actionAreaHeightPx by remember { mutableIntStateOf(0) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = ContentPadding,
                    top = ContentPadding,
                    end = ContentPadding,
                    bottom = ContentPadding + with(density) { actionAreaHeightPx.toDp() },
                ),
            verticalArrangement = Arrangement.spacedBy(SectionSpacing),
        ) {
            RoomPreviewCard(
                name = state.values.name,
                description = state.values.description,
                color = state.values.color,
            )
            MinoTextField(
                value = state.values.name,
                onValueChange = { onIntent(RoomFormIntent.NameChanged(it)) },
                label = stringResource(R.string.roomform_label_name),
                required = true,
                placeholder = stringResource(R.string.roomform_name_placeholder),
                helperText = stringResource(R.string.roomform_name_helper),
                status = state.nameValidation.fieldStatus,
                showClearButton = false,
            )
            MinoTextArea(
                state = descriptionState,
                label = stringResource(R.string.roomform_label_description),
                placeholder = stringResource(R.string.roomform_description_placeholder),
                maxLength = DESCRIPTION_MAX_LENGTH,
            )
            ColorSection(
                selectedColor = state.values.color,
                onColorSelect = { onIntent(RoomFormIntent.ColorSelected(it)) },
            )
        }
        MinoActionArea(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { actionAreaHeightPx = it.height },
            mainAction = ActionAreaAction(
                text = stringResource(state.mode.ctaRes),
                onClick = { onIntent(RoomFormIntent.SubmitClicked) },
                enabled = state.canSubmit,
            ),
            sticky = true,
        )
    }
}

/**
 * 편집 진입 초기값을 기다리는 동안의 표시.
 *
 * 값이 오기 전의 폼을 미리 그려 두지 않는다 — 빈 입력이 잠깐 보였다가 채워지면 사용자가 그 빈칸을
 * 자기 방의 현재 값으로 읽는다.
 */
@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MinoAndroidTheme.colors.primaryNormal)
    }
}

/**
 * 편집 진입 초기값 조회가 실패했을 때의 화면. 안내 한 줄과 재시도 버튼이 전부다.
 *
 * 스낵바가 아니라 화면을 통째로 바꾸는 이유는 이것이 폼의 주 데이터이기 때문이다 — 잠깐 떴다 사라지는
 * 안내 뒤에 빈 폼을 남기면 사용자가 그 폼을 저장할 수 있게 된다(에러 처리 규약 §5).
 */
@Composable
private fun LoadErrorContent(
    error: MinoDomainException,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(ContentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LoadErrorSpacing),
        ) {
            Text(
                text = stringResource(loadErrorMessageRes(error)),
                style = MinoAndroidTheme.typography.body1NormalMedium,
                color = MinoAndroidTheme.colors.labelNeutral,
                textAlign = TextAlign.Center,
            )
            MinoButton(
                text = stringResource(R.string.roomform_error_retry),
                onClick = onRetryClick,
                size = ButtonSize.Medium,
                style = ButtonStyle.OutlinedAssistive,
            )
        }
    }
}

/**
 * 로드 실패 문구. 리프를 가르지 않는다 — 어느 쪽이든 사용자가 할 수 있는 일이 재시도로 같아, 원인을
 * 나눠도 행동이 달라지지 않는다.
 *
 * 문구는 이 feature가 아니라 `:core:common:ui`가 가진 것을 쓴다. 원인을 특정하지 않는 안내라
 * roomform 고유의 말이 아니고, 같은 말을 feature마다 다시 적으면 표현이 갈린다.
 *
 * `else`를 두지 않아 리프가 늘면 컴파일이 멈추고 여기서 다시 판단하게 된다.
 */
@StringRes
private fun loadErrorMessageRes(error: MinoDomainException): Int =
    when (error) {
        is MinoDomainException.Network,
        is MinoDomainException.Http,
        is MinoDomainException.Auth,
        -> CommonUiR.string.error_unknown
    }

/**
 * 상단 내비게이션과 그 아래 경계선.
 *
 * [MinoTopNavigation]은 표시줄 내용만 그리고 표면은 호출자에게 넘긴다. 이 화면의 표면은 바깥
 * `Column`의 배경이고, 스크롤 영역과의 경계선만 남아 여기서 긋는다. 표시줄 높이를 늘리지 않도록
 * 겹쳐 놓는다 — 디자인도 표시줄 안쪽 아래 변에 그린 테두리다.
 *
 * 좌우 두 자리에 무엇을 놓을지는 이 함수가 정하지 않는다 — 진입 맥락을 아는 것은 호출자다.
 *
 * @param onBackClick `null`이면 뒤로 갈 수단을 주지 않는다.
 * @param actionLabel `null`이면 우측 액션을 주지 않는다.
 */
@Composable
private fun TopBar(
    title: String,
    onBackClick: (() -> Unit)?,
    actionLabel: String?,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        MinoTopNavigation(
            title = title,
            onBackClick = onBackClick,
            actionLabel = actionLabel,
            onActionClick = onActionClick,
        )
        HorizontalDivider(
            modifier = Modifier.align(Alignment.BottomCenter),
            thickness = TopBarDividerThickness,
            color = MinoAndroidTheme.colors.lineNormalNeutral,
        )
    }
}

/**
 * 색 고르기 영역. 제목과 팔레트를 묶는다 — 팔레트는 칩 배치만 알고 제목은 다른 입력 필드의 제목과
 * 나란히 놓여야 해서 그리드 바깥이다.
 *
 * 필수 입력이 아니라 제목에 `*`를 붙이지 않는다.
 */
@Composable
private fun ColorSection(
    selectedColor: RoomColor?,
    onColorSelect: (RoomColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ColorSectionSpacing),
    ) {
        Text(
            text = stringResource(R.string.roomform_label_color),
            style = MinoAndroidTheme.typography.label1NormalBold,
            color = MinoAndroidTheme.colors.labelNeutral,
        )
        RoomColorPalette(
            selectedColor = selectedColor,
            onColorSelect = onColorSelect,
        )
    }
}

/**
 * 이름 판정을 필드가 아는 시각 상태로 옮긴다.
 *
 * 오류는 이 필드 안에서만 알린다 — 상단 배너·토스트를 따로 띄우지 않는다.
 *
 * [RoomNameValidation.Blank]는 오류가 아니다. 아직 손대지 않은 폼을 빨갛게 칠하면 사용자가
 * 저지르지도 않은 잘못을 알리는 꼴이 된다. 저장이 막혔다는 것은 CTA 비활성이 이미 말한다.
 *
 * [RoomNameValidation.Valid]도 마찬가지로 평상 상태다 — 제대로 쓴 이름에 성공 표시를 얹지 않는다.
 */
private val RoomNameValidation.fieldStatus: MinoTextFieldStatus
    get() = when (this) {
        RoomNameValidation.InvalidCharacter -> MinoTextFieldStatus.Negative
        RoomNameValidation.Valid, RoomNameValidation.Blank -> MinoTextFieldStatus.Normal
    }

/**
 * 확인 버튼이 올려보낼 인텐트. 문구와 달리 이것은 모달이 아니라 폼의 어휘라 모델이 아닌 여기서 고른다.
 *
 * 이탈 2종은 나가는 일이 같아 같은 인텐트로 모인다.
 */
private val RoomFormDialog.confirmIntent: RoomFormIntent
    get() = when (this) {
        RoomFormDialog.Save -> RoomFormIntent.SaveConfirmed
        RoomFormDialog.ExitCreate, RoomFormDialog.ExitEdit -> RoomFormIntent.ExitConfirmed
    }

/** 상단 타이틀은 진입 맥락으로 갈린다. */
@get:StringRes
private val RoomFormMode.titleRes: Int
    get() = when (this) {
        RoomFormMode.Create -> R.string.roomform_title_create
        is RoomFormMode.Edit -> R.string.roomform_title_edit
    }

/** CTA 라벨도 타이틀과 같은 기준으로 갈린다. */
@get:StringRes
private val RoomFormMode.ctaRes: Int
    get() = when (this) {
        RoomFormMode.Create -> R.string.roomform_cta_create
        is RoomFormMode.Edit -> R.string.roomform_cta_edit
    }

/** FR-005의 방 설명 상한. 자르는 것도 세는 것도 [MinoTextArea]가 한다. */
private const val DESCRIPTION_MAX_LENGTH = 30

private val ContentPadding = 20.dp

private val TopBarDividerThickness = 0.5.dp

private val SectionSpacing = 30.dp

private val ColorSectionSpacing = 20.dp

/** 로드 실패 화면에는 대조할 디자인이 없다. 이 값은 디자인이 그려지면 그때 맞춘다. */
private val LoadErrorSpacing = 16.dp
