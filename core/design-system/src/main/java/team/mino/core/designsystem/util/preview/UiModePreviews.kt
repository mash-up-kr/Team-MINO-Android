package team.mino.core.designsystem.util.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "LightMode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "DarkMode")
annotation class UiModePreviews
