package team.mino.feature.sample.map.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.feature.sample.map.component.SampleMapContent
import team.mino.feature.sample.map.vm.SampleMapUiState

@Composable
fun SampleMapScreen(
    state: SampleMapUiState,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        SampleMapContent(
            cameraCenter = state.cameraCenter,
            zoom = state.zoom,
            areaPoints = state.areaPoints,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
