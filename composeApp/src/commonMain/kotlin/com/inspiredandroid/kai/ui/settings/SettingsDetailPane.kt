package com.inspiredandroid.kai.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.linux.LinuxDistro
import com.inspiredandroid.kai.ui.components.VerticalScrollbarForList

/**
 * Detail pane for the settings screen (Plan 2, Phase 2). Renders the scrollable
 * content for the currently selected tab. Extracted so the adaptive shell can
 * place it beside a navigation rail on expanded windows.
 */
@Composable
fun SettingsDetailPane(
    uiState: SettingsUiState,
    actions: SettingsActions,
    sandboxState: SandboxUiState,
    onToggleSandbox: (Boolean) -> Unit,
    onSelectDistro: (LinuxDistro) -> Unit,
    onSetupSandbox: () -> Unit,
    onCancelSandbox: () -> Unit,
    onResetSandbox: () -> Unit,
    onInstallPackages: () -> Unit,
    onMigrateHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settingsListState = rememberLazyListState()
    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            state = settingsListState,
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item { Spacer(Modifier.height(16.dp)) }

            val maxContentWidth = when (uiState.currentTab) {
                SettingsTab.Services -> 500.dp
                else -> 900.dp
            }
            item {
                Column(
                    Modifier.widthIn(max = maxContentWidth).fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { liveRegion = LiveRegionMode.Polite },
                    ) {
                        when (uiState.currentTab) {
                            SettingsTab.General -> {
                                SettingsOverview(uiState = uiState, sandboxInstalled = sandboxState.sandboxInstalled)
                                Spacer(Modifier.height(SettingsSpacing.md))
                                GeneralContent(uiState = uiState, actions = actions)
                            }

                            SettingsTab.Agent -> {
                                AgentContent(uiState = uiState, actions = actions)
                            }

                            SettingsTab.Services -> {
                                ServicesContent(uiState = uiState, actions = actions)
                            }

                            SettingsTab.Tools -> {
                                ToolsTabContent(
                                    uiState = uiState,
                                    actions = actions,
                                    sandboxState = sandboxState,
                                )
                            }

                            SettingsTab.Integrations -> {
                                IntegrationsContent()
                            }

                            SettingsTab.Sandbox -> {
                                if (sandboxState.showSandbox) {
                                    SandboxSettingsCard(
                                        sandboxState = sandboxState,
                                        onToggleSandbox = onToggleSandbox,
                                        onSelectDistro = onSelectDistro,
                                        onSetupSandbox = onSetupSandbox,
                                        onCancelSandbox = onCancelSandbox,
                                        onResetSandbox = onResetSandbox,
                                        onInstallPackages = onInstallPackages,
                                        onMigrateHome = onMigrateHome,
                                    )
                                } else {
                                    SandboxUnavailableContent()
                                }
                            }

                            SettingsTab.Data -> {
                                DataControlContent(uiState = uiState, actions = actions)
                            }

                            SettingsTab.Account -> {
                                AccountPlanContent(uiState = uiState)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
            item {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    BottomInfo()
                }
            }
        }

        VerticalScrollbarForList(
            listState = settingsListState,
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        )
    }
}

@Composable
private fun SandboxUnavailableContent() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Linux Sandbox",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "The Linux sandbox is only available on Android devices.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun DataControlContent(
    uiState: SettingsUiState,
    actions: SettingsActions,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Data Control",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Manage your chat history and stored data.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = { actions.onClearAllConversations() }) {
            Text("Clear all conversations")
        }
    }
}

@Composable
fun AccountPlanContent(
    uiState: SettingsUiState,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Account and Plan",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Brox Kai 2.0",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Version " + com.inspiredandroid.kai.Version.appVersion,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
