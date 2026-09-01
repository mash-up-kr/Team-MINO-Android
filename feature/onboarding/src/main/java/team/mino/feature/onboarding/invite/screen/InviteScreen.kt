package team.mino.feature.onboarding.invite.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.actionarea.ActionAreaAction
import team.mino.core.designsystem.component.actionarea.MinoActionArea
import team.mino.core.designsystem.component.topnavigation.MinoTopNavigation
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Close
import team.mino.core.designsystem.foundation.icons.icons.Link
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.feature.onboarding.R
import team.mino.feature.onboarding.invite.component.InviteIllustration
import team.mino.feature.onboarding.invite.vm.InviteIntent

/**
 * 온보딩 친구 초대 스텝의 화면.
 *
 * **상태를 받지 않는다.** 링크를 확보했는지, 확보에 실패했는지가 이 화면을 바꾸지 않기 때문이다
 * (`contracts/onboarding-flow-ui.md` §3.2·§3.4) — 두 액션은 언제나 눌리고, 링크가 없는 채로
 * 눌리면 그때 실패를 알린다. 확보 여부를 그리기 시작하면 그 요구사항이 조용히 뒤집힌다.
 *
 * **참여자 목록도, 건너뛰기도, 진행 표시도 없다**(같은 문서 §3.1). 이 스텝은 몇 번째인지도,
 * 누가 이미 들어와 있는지도 보여주지 않는다.
 *
 * `Scaffold`를 열지 않는다. chrome과 인셋은 셸이 소유하고 이 화면은 셸이 내준 영역 안을 그린다.
 * 그래서 디자인이 액션 영역 아래에 두는 홈 인디케이터 여백도 여기서 다시 두지 않는다 — 셸이 이미
 * 실제 인셋만큼 비워 놓은 자리다.
 *
 * @param onClose 우상단 [X]. 스텝을 넘기는 조작이라 이 화면의 Intent가 아니라 콜백으로 올라간다
 *  (같은 문서 §3.3). 무엇으로 넘어갈지는 플로우 ViewModel이 정한다.
 */
@Composable
internal fun InviteScreen(
    onIntent: (InviteIntent) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MinoAndroidTheme.colors.backgroundNormalNormal),
    ) {
        MinoTopNavigation(
            title = "",
            actionIcon = MinoIcons.Close,
            actionIconContentDescription = CLOSE_ACTION_DESCRIPTION,
            onActionClick = onClose,
        )
        InviteGuide(modifier = Modifier.weight(1f))
        InviteActions(onIntent = onIntent)
    }
}

/**
 * 제목과 일러스트·본문 묶음. 제목은 위에, 묶음은 아래에 붙고 남는 높이가 둘 사이로 간다 —
 * 화면이 길어지면 그 사이만 벌어지고 본문은 액션 영역 위 자리를 지킨다.
 */
@Composable
private fun InviteGuide(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ContentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.onboarding_invite_title),
            modifier = Modifier.fillMaxWidth(),
            style = MinoAndroidTheme.typography.title2Bold,
            color = MinoAndroidTheme.colors.primaryNormal,
            textAlign = TextAlign.Center,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = IllustrationGroupVerticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(IllustrationDescriptionSpacing),
        ) {
            InviteIllustration()
            Text(
                text = stringResource(R.string.onboarding_invite_description),
                modifier = Modifier.fillMaxWidth(),
                style = MinoAndroidTheme.typography.label1NormalRegular,
                color = MinoAndroidTheme.colors.primaryNormal,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * 공유와 복사. **둘 다 언제나 활성이다** — 링크를 아직 확보하지 못했어도 잠그지 않는다
 * (`contracts/onboarding-flow-ui.md` §3.2). 잠그는 대신 눌린 뒤에 실패를 알린다.
 *
 * 서로에게 조건을 걸지도 않는다. 복사한 뒤 이어서 공유하거나 연달아 복사하는 경로가 그래서 성립한다.
 */
@Composable
private fun InviteActions(
    onIntent: (InviteIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoActionArea(
        modifier = modifier,
        mainAction = ActionAreaAction(
            text = stringResource(R.string.onboarding_invite_action_share),
            onClick = { onIntent(InviteIntent.ShareLink) },
        ),
        alternativeAction = ActionAreaAction(
            text = stringResource(R.string.onboarding_invite_action_copy),
            onClick = { onIntent(InviteIntent.CopyLink) },
            leadingIcon = {
                Icon(imageVector = MinoIcons.Link, contentDescription = null)
            },
        ),
    )
}

/**
 * 우상단 [X]의 접근성 설명. 계약이 문구를 정해 두지 않아 이 화면이 정한다.
 *
 * "건너뛰기"가 아니라 "닫기"인 것은 이 화면이 건너뛰기를 주지 않기 때문이다 — 계약이 [건너뛰기]
 * 텍스트 버튼을 명시적으로 배제했으므로, 그 말을 아이콘 설명으로 되살리면 화면에 없는 조작을
 * 화면 낭독자에게만 있는 것처럼 만든다.
 *
 * 문자열 리소스가 아닌 것은 표시되는 문구가 아니라서다. 상단 표시줄의 뒤로가기도 같은 자리에서
 * 같은 방식으로 설명을 갖는다.
 */
private const val CLOSE_ACTION_DESCRIPTION = "닫기"

// Figma base lg 변수 대응 — 토큰 미존재
private val ContentPadding = 20.dp

private val IllustrationGroupVerticalPadding = 24.dp

private val IllustrationDescriptionSpacing = 40.dp
