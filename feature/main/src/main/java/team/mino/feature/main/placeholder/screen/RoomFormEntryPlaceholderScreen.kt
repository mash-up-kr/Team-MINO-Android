package team.mino.feature.main.placeholder.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.feature.main.placeholder.RoomFormEntryPoint

/**
 * 방 폼을 세 갈래(생성 · 온보딩 생성 · 시드 방 편집)로 열어 보고 돌아온 결과를 확인하는 임시 화면.
 *
 * 실제 진입점 feature가 생기면 [RoomFormEntryPoint]와 함께 제거한다
 * (→ `docs/specs/group-room-form/plan.md` §범위 경계).
 */
@Composable
internal fun RoomFormEntryPlaceholderScreen(
    entryPoint: RoomFormEntryPoint,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "방 폼 임시 진입점",
            color = MinoAndroidTheme.colors.labelNormal,
            style = MinoAndroidTheme.typography.body1NormalBold,
        )

        Button(
            onClick = entryPoint.onCreate,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "공동방 만들기")
        }
        Button(
            onClick = entryPoint.onCreateWithOnboarding,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "온보딩으로 열기")
        }
        Button(
            onClick = entryPoint.onEditSeedRoom,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "시드 방(room-1) 편집")
        }

        Text(
            text = "결과: ${entryPoint.lastResult ?: "아직 없음"}",
            color = MinoAndroidTheme.colors.labelNormal,
            style = MinoAndroidTheme.typography.body2NormalRegular,
        )
    }
}
