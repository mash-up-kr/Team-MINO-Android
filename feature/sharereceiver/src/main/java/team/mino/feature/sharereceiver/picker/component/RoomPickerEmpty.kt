package team.mino.feature.sharereceiver.picker.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.sharereceiver.R

/**
 * 저장할 방이 하나도 없을 때 방 카드 목록 자리를 대신하는 안내. 헤더와 하단 액션 영역은 그대로
 * 남으므로, 시트 자체는 다른 사용자가 보는 것과 같은 모습이다.
 *
 * **여기서 나가는 길을 만들지 않는다.** 온보딩으로 보내는 버튼도, 다시 불러오는 버튼도 두지 않는다 —
 * 사용자는 이 시트를 닫고 스스로 앱을 연다.
 *
 * 방 목록 조회에 실패한 경우도 이 상태로 수렴한다. 방이 없는 것과 불러오지 못한 것을 사용자에게
 * 구분해 보이지 않는다.
 *
 * **문구와 시각 표현이 아직 확정되지 않았다.** 디자인에 이 상태의 화면이 없어, 지금은 저장할 수 없다는
 * 사실만 한 줄로 전달한다. 확정되면 이 컴포저블이 통째로 바뀔 수 있다.
 *
 * 크기를 스스로 정하지 않는다 — 목록이 쓰던 공간을 [modifier]로 받아 그 안에서 가운데 놓인다.
 */
@Composable
internal fun RoomPickerEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.sharereceiver_empty_message),
            style = MinoAndroidTheme.typography.label1NormalRegular,
            color = MinoAndroidTheme.colors.labelAlternative,
            textAlign = TextAlign.Center,
        )
    }
}

private val HorizontalPadding = 20.dp

@UiModePreviews
@Composable
private fun RoomPickerEmptyPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        RoomPickerEmpty(modifier = modifier.height(EmptyPreviewHeight))
    }
}

private val EmptyPreviewHeight = 240.dp
