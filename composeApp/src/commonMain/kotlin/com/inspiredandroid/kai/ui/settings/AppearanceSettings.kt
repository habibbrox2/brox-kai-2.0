package com.inspiredandroid.kai.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.TextPrimary
import com.inspiredandroid.kai.ui.TextSecondary

@Composable
internal fun AppearanceContent(
    uiState: SettingsUiState,
    actions: SettingsActions,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            androidx.compose.material3.Text(
                text = "Appearance",
                style = KaiTypography.titleLarge,
                color = TextPrimary,
            )
            androidx.compose.material3.Text(
                text = "Customize the look and feel of the app",
                style = KaiTypography.bodyMedium,
                color = TextSecondary,
            )
        }
        ThemeModeSwitch(
            themeMode = uiState.themeMode,
            onChangeThemeMode = actions.onChangeThemeMode,
        )
    }
}
