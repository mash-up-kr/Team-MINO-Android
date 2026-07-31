package team.mino.feature.sample.map.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.feature.sample.map.component.SampleMapContent
import team.mino.feature.sample.map.vm.SampleMapUiState

@Composable
fun SampleMapScreen(
    state: SampleMapUiState,
    modifier: Modifier = Modifier,
) {
    SampleMapContent(
        cameraCenter = state.cameraCenter,
        zoom = state.zoom,
        areaPoints = state.areaPoints,
        modifier = modifier,
    )
}
