package team.mino.feature.onboarding.tutorial.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.actionarea.ActionAreaAction
import team.mino.core.designsystem.component.actionarea.MinoActionArea
import team.mino.core.designsystem.component.pagination.MinoPaginationDots
import team.mino.core.designsystem.component.topnavigation.MinoTopNavigation
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.feature.onboarding.R
import team.mino.feature.onboarding.tutorial.component.TutorialExampleImage
import team.mino.feature.onboarding.tutorial.component.TutorialMascot
import team.mino.feature.onboarding.tutorial.component.TutorialStepBadge
import team.mino.feature.onboarding.tutorial.model.TutorialStep

/**
 * 공유 방법을 다섯 장으로 넘겨 보는 튜토리얼 화면.
 *
 * **캐러셀 상태를 들지 않는다.** [pagerState]를 만드는 것도, dot을 눌렀을 때 실제로 페이지를
 * 옮기는 것도 이 화면을 여는 Route다. 화면은 받은 상태를 그리고 조작을 올려보내기만 하며,
 * 저장을 일으키지 않아 ViewModel도 두지 않는다
 * (`docs/specs/onboarding-flow/contracts/onboarding-flow-ui.md` §4.2).
 *
 * **상단 [건너뛰기]와 하단 시작 버튼은 현재 페이지 하나에서 갈린다.** 마지막 장에는 건너뛸 것이
 * 남아 있지 않아 상단 액션이 사라지고, 그 자리를 하단 버튼이 대신한다. 두 조건을 따로 들지 않으므로
 * 마지막 장에 갔다가 앞 장으로 되돌아오면 두 자리도 함께 되돌아온다.
 *
 * 둘은 같은 곳으로 간다 — 어느 쪽을 눌러도 하는 일은 튜토리얼을 끝내는 것 하나다.
 *
 * `Scaffold`는 셸이 열고 이 화면은 열지 않는다. 상단 표시줄만은 화면 고유 chrome이라 여기서 놓는다.
 *
 * @param onDotClick 눌린 dot의 인덱스. 그 인덱스로 페이지를 옮기는 것은 [pagerState]를 든 쪽이다.
 * @param onFinish 상단 [건너뛰기]와 하단 시작 버튼이 함께 부른다.
 */
@Composable
internal fun TutorialScreen(
    pagerState: PagerState,
    onDotClick: (index: Int) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentPage = pagerState.currentPage
    val isLastStep = currentPage == TutorialStep.entries.lastIndex

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            MinoTopNavigation(
                title = stringResource(R.string.onboarding_tutorial_title),
                actionLabel = if (isLastStep) {
                    null
                } else {
                    stringResource(R.string.onboarding_tutorial_action_skip)
                },
                onActionClick = onFinish,
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                TutorialStepPage(step = TutorialStep.entries[page])
            }

            MinoPaginationDots(
                count = TutorialStep.entries.size,
                selectedIndex = currentPage,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onDotClick = onDotClick,
            )

            // 마지막 장의 액션 영역이 들어올 자리. 앞 장에서도 비워 둔 채 유지해야 장을 넘길 때
            // dot이 제자리에 남는다.
            Spacer(modifier = Modifier.height(ActionSlotHeight))
        }

        if (isLastStep) {
            MinoActionArea(
                mainAction = ActionAreaAction(
                    text = stringResource(R.string.onboarding_tutorial_action_start),
                    onClick = onFinish,
                ),
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/**
 * 한 장에 담기는 것 — 스텝 번호·안내 문구·예시 이미지·캐릭터. 넷 다 [step] 하나에서 나와 서로
 * 다른 스텝을 가리키는 조합이 생기지 않는다.
 *
 * 안내 문구가 접히는 자리는 문구 자체가 들고 있다. 감기는 폭으로 정하면 문구가 바뀔 때마다
 * 접히는 자리가 따라 흔들린다.
 *
 * 캐릭터는 예시 이미지와 같은 상자에 넣어 그 카드의 위쪽 모서리를 기준으로 놓는다. 상자 밖으로
 * 넘겨 그리므로 이 자리에서 잘리지 않아야 한다.
 */
@Composable
private fun TutorialStepPage(
    step: TutorialStep,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = ContentHorizontalPadding,
                top = ContentTopPadding,
                end = ContentHorizontalPadding,
                bottom = ContentBottomPadding,
            ),
    ) {
        TutorialStepBadge(number = step.number)

        Spacer(modifier = Modifier.height(BadgeGuideSpacing))

        Text(
            text = stringResource(step.guideRes),
            style = MinoAndroidTheme.typography.heading1Bold,
            color = MinoAndroidTheme.colors.primaryNormal,
        )

        Spacer(modifier = Modifier.height(GuideExampleSpacing))

        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .align(Alignment.CenterHorizontally),
        ) {
            TutorialExampleImage(step = step)

            TutorialMascot(
                step = step,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = -MascotEndInset, y = -MascotTopOverhang),
            )
        }
    }
}

private val ContentHorizontalPadding = 35.dp

private val ContentTopPadding = 40.dp

/** 예시 이미지와 dot 사이에 남겨 두는 최소 간격. 화면이 길면 그만큼 더 벌어진다. */
private val ContentBottomPadding = 16.dp

private val BadgeGuideSpacing = 8.dp

private val GuideExampleSpacing = 30.dp

/** 캐릭터의 오른쪽 끝이 예시 카드의 오른쪽 모서리에서 안쪽으로 들어와 있는 만큼. */
private val MascotEndInset = 17.dp

/** 캐릭터가 예시 카드의 위쪽 모서리보다 위로 올라와 있는 만큼. */
private val MascotTopOverhang = 63.dp

/** 하단 액션 영역이 차지하는 높이. 그 영역이 없는 장에서도 이만큼을 비워 둔다. */
private val ActionSlotHeight = 88.dp
