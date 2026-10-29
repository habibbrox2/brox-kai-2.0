package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.inspiredandroid.kai.data.ServiceEntry
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.BorderSoft
import com.inspiredandroid.kai.ui.CardFill
import com.inspiredandroid.kai.ui.KaiShapes
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.SurfaceRaised
import com.inspiredandroid.kai.ui.TextPrimary
import com.inspiredandroid.kai.ui.TextSecondary
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.isOledFlavor
import com.inspiredandroid.kai.ui.settings.SettingsModel
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.settings_model_label
import kai.composeapp.generated.resources.settings_model_loading
import kai.composeapp.generated.resources.settings_tab_services
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
internal fun ServiceSelector(
    services: ImmutableList<ServiceEntry>,
    onSelectService: (String) -> Unit,
    models: ImmutableList<SettingsModel> = persistentListOf(),
    isLoadingModels: Boolean = false,
    onSelectModel: (String) -> Unit = {},
    onLoadModels: () -> Unit = {},
    showLabel: Boolean = false,
) {
    if (services.isEmpty()) return

    val current = services.first()
    var expanded by remember { mutableStateOf(false) }
    // The model the chat will actually use: the effective id of the active instance, shown
    // under the name the provider gives it when the list is loaded.
    val currentModelLabel = remember(current.modelId, models) {
        models.firstOrNull { it.id == current.modelId }?.let { model ->
            model.displayName?.takeIf { it.isNotBlank() } ?: model.id
        } ?: current.modelId
    }

    // Ask for the provider's model list when the picker opens, so the choices are complete
    // without spending a request on every chat start.
    val currentOnLoadModels by rememberUpdatedState(onLoadModels)
    LaunchedEffect(expanded) {
        if (expanded) currentOnLoadModels()
    }

    // Focus management for popup items
    val popupFocusRequester = remember { FocusRequester() }
    val focusableItemCount = (if (models.isNotEmpty()) models.size else 0) + services.size
    var focusedIndex by remember { mutableStateOf(0) }

    Box {
        if (showLabel) {
            // Pill variant used by the Plan 7 top bar: icon + name + chevron.
            val isOled = MaterialTheme.colorScheme.isOledFlavor
            val pillBg = if (isOled) {
                com.inspiredandroid.kai.ui.SurfaceRaised.copy(alpha = 0.9f)
            } else {
                SurfaceRaised
            }
            val pillBorder = if (isOled) {
                com.inspiredandroid.kai.ui.BorderSoft
            } else {
                BorderSoft
            }

            Row(
                modifier = Modifier
                    .clip(KaiShapes.pill)
                    .background(pillBg, KaiShapes.pill)
                    .border(BorderStroke(1.dp, pillBorder), KaiShapes.pill)
                    .clickable { expanded = true }
                    .handCursor()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = vectorResource(current.icon),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = TextSecondary,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = current.serviceName,
                    style = KaiTypography.labelSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 100.dp),
                )
                if (currentModelLabel.isNotBlank()) {
                    Text(
                        text = " · $currentModelLabel",
                        style = KaiTypography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 120.dp),
                    )
                }
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = current.serviceName,
                    modifier = Modifier.size(16.dp),
                    tint = TextSecondary,
                )
            }
        } else {
            val isOled = MaterialTheme.colorScheme.isOledFlavor
            val circleBg = if (isOled) {
                com.inspiredandroid.kai.ui.SurfaceRaised.copy(alpha = 0.9f)
            } else {
                SurfaceRaised
            }
            val circleBorder = if (isOled) {
                com.inspiredandroid.kai.ui.BorderSoft
            } else {
                BorderSoft
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(circleBg, CircleShape)
                    .border(BorderStroke(1.dp, circleBorder), CircleShape)
                    .clickable { expanded = true }
                    .handCursor(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = vectorResource(current.icon),
                    contentDescription = current.serviceName,
                    modifier = Modifier.size(18.dp),
                    tint = TextSecondary,
                )
            }
        }

        if (expanded) {
            val spacingPx = with(LocalDensity.current) { 8.dp.roundToPx() }
            val isOled = MaterialTheme.colorScheme.isOledFlavor
            val popupBg = if (isOled) {
                com.inspiredandroid.kai.ui.CardFill
            } else {
                CardFill
            }
            val popupBorder = BorderStroke(1.dp, BorderSoft)

            Popup(
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
                popupPositionProvider = remember(spacingPx) { AnchorAbovePositionProvider(spacingPx) },
            ) {
                BoxWithConstraints {
                    val maxMenuHeight = maxHeight - 24.dp // keep a margin from screen edges
                    Box(
                        modifier = Modifier
                            .clip(KaiShapes.card)
                            .background(popupBg, KaiShapes.card)
                            .border(popupBorder, KaiShapes.card)
                            .onKeyEvent { event ->
                                if (event.key == Key.Escape) {
                                    expanded = false
                                    true
                                } else if (event.key == Key.Tab || event.key == Key.DirectionDown || event.key == Key.DirectionUp) {
                                    val direction = if (event.key == Key.DirectionUp) -1 else 1
                                    focusedIndex = (focusedIndex + direction).coerceIn(0, focusableItemCount - 1)
                                    true
                                } else {
                                    false
                                }
                            }
                            .onFocusChanged { focused ->
                                if (focused.isFocused) {
                                    focusedIndex = 0
                                }
                            },
                    ) {
                        Column(
                            modifier = Modifier
                                .heightIn(max = maxMenuHeight)
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 4.dp)
                                .focusRequester(popupFocusRequester),
                        ) {
                            var itemIndex = 0
                            // Models of the active service: the model that is loaded and the
                            // alternatives the user can switch to.
                            if (models.isNotEmpty()) {
                                PickerSectionLabel(stringResource(Res.string.settings_model_label))
                                models.forEach { model ->
                                    val isCurrent = model.id == current.modelId
                                    val index = itemIndex
                                    ModelMenuItem(
                                        model = model,
                                        isCurrent = isCurrent,
                                        onClick = {
                                            expanded = false
                                            if (!isCurrent) onSelectModel(model.id)
                                        },
                                        modifier = Modifier
                                            .focusable()
                                            .onFocusChanged { focus -> focusedIndex = if (focus.isFocused) index else focusedIndex }
                                            .focusRequester(if (index == 0) popupFocusRequester else FocusRequester()),
                                    )
                                    itemIndex++
                                }
                                HorizontalDivider(
                                    color = TextSecondary.copy(alpha = 0.12f),
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                            } else if (isLoadingModels) {
                                PickerSectionLabel(stringResource(Res.string.settings_model_label))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = AccentPrimary,
                                    )
                                    Text(
                                        text = stringResource(Res.string.settings_model_loading),
                                        style = KaiTypography.labelSmall,
                                        color = TextSecondary,
                                    )
                                }
                            }

                            PickerSectionLabel(stringResource(Res.string.settings_tab_services))
                            services.forEach { entry ->
                                key(entry.instanceId) {
                                    val isCurrent = entry.instanceId == current.instanceId
                                    val index = itemIndex
                                    ServiceMenuItem(
                                        entry = entry,
                                        isCurrent = isCurrent,
                                        onClick = {
                                            expanded = false
                                            if (!isCurrent) {
                                                onSelectService(entry.instanceId)
                                            }
                                        },
                                        modifier = Modifier
                                            .focusable()
                                            .onFocusChanged { focus -> focusedIndex = if (focus.isFocused) index else focusedIndex }
                                            .focusRequester(if (index == 0 && models.isEmpty() && !isLoadingModels) popupFocusRequester else FocusRequester()),
                                    )
                                    itemIndex++
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section heading inside the picker popup: "Model", "Services".
 */
@Composable
private fun PickerSectionLabel(text: String) {
    Text(
        text = text,
        style = KaiTypography.labelSmall,
        color = TextSecondary,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/**
 * A model the active service can run. The loaded one is highlighted.
 */
@Composable
private fun ModelMenuItem(
    model: SettingsModel,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOled = MaterialTheme.colorScheme.isOledFlavor
    val rowBackground = if (isCurrent) {
        com.inspiredandroid.kai.ui.AccentPrimary.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }
    val textColor = if (isCurrent) {
        com.inspiredandroid.kai.ui.AccentPrimary
    } else {
        TextPrimary
    }
    val shortName = model.displayName?.takeIf { it.isNotBlank() } ?: model.id
    Row(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clip(KaiShapes.sm)
            .background(rowBackground)
            .clickable(onClick = onClick)
            .handCursor()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .widthIn(min = 180.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = shortName,
            style = KaiTypography.bodySmall,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ServiceMenuItem(
    entry: ServiceEntry,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOled = MaterialTheme.colorScheme.isOledFlavor
    val rowBackground = if (isCurrent) {
        com.inspiredandroid.kai.ui.AccentPrimary.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }
    val textColor = if (isCurrent) {
        com.inspiredandroid.kai.ui.AccentPrimary
    } else {
        TextPrimary
    }
    val subTextColor = if (isCurrent) {
        com.inspiredandroid.kai.ui.AccentPrimary.copy(alpha = 0.7f)
    } else {
        TextSecondary
    }
    Row(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clip(KaiShapes.sm)
            .background(rowBackground)
            .clickable(onClick = onClick)
            .handCursor()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .widthIn(min = 180.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = vectorResource(entry.icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = textColor,
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = entry.serviceName,
                style = KaiTypography.bodySmall,
                color = textColor,
            )
            if (entry.modelId.isNotEmpty()) {
                Text(
                    text = entry.modelId,
                    style = KaiTypography.labelSmall,
                    color = subTextColor,
                )
            }
        }
    }
}

private class AnchorAbovePositionProvider(
    private val verticalSpacing: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val x = (anchorBounds.right - popupContentSize.width).coerceIn(0, maxX)
        val above = anchorBounds.top - popupContentSize.height - verticalSpacing
        val y = if (above >= 0) {
            above
        } else {
            val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)
            (anchorBounds.bottom + verticalSpacing).coerceAtMost(maxY)
        }
        return IntOffset(x, y)
    }
}
