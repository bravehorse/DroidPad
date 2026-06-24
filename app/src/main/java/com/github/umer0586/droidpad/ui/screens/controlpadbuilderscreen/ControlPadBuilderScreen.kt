/*
 *     This file is a part of DroidPad (https://www.github.com/UmerCodez/DroidPad)
 *     Copyright (C) 2025 Umer Farooq (umerfarooq2383@gmail.com)
 *
 *     DroidPad is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     DroidPad is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with DroidPad. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.github.umer0586.droidpad.ui.screens.controlpadbuilderscreen

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.umer0586.droidpad.R
import com.github.umer0586.droidpad.data.ButtonProperties
import com.github.umer0586.droidpad.data.DpadProperties
import com.github.umer0586.droidpad.data.ExternalData
import com.github.umer0586.droidpad.data.GaugeProperties
import com.github.umer0586.droidpad.data.JoyStickProperties
import com.github.umer0586.droidpad.data.LEDProperties
import com.github.umer0586.droidpad.data.LabelProperties
import com.github.umer0586.droidpad.data.Resolution
import com.github.umer0586.droidpad.data.SliderProperties
import com.github.umer0586.droidpad.data.SteeringWheelProperties
import com.github.umer0586.droidpad.data.StepSliderProperties
import com.github.umer0586.droidpad.data.SwitchProperties
import com.github.umer0586.droidpad.data.database.entities.ControlPad
import com.github.umer0586.droidpad.data.database.entities.ControlPadItem
import com.github.umer0586.droidpad.data.database.entities.ItemType
import com.github.umer0586.droidpad.data.database.entities.Orientation
import com.github.umer0586.droidpad.data.database.entities.offset
import com.github.umer0586.droidpad.ui.bottomBarHeight
import com.github.umer0586.droidpad.ui.components.ControlPadButton
import com.github.umer0586.droidpad.ui.components.ControlPadDpad
import com.github.umer0586.droidpad.ui.components.ControlPadGauge
import com.github.umer0586.droidpad.ui.components.ControlPadJoyStick
import com.github.umer0586.droidpad.ui.components.ControlPadLED
import com.github.umer0586.droidpad.ui.components.ControlPadLabel
import com.github.umer0586.droidpad.ui.components.ControlPadSlider
import com.github.umer0586.droidpad.ui.components.ControlPadSteeringWheel
import com.github.umer0586.droidpad.ui.components.ControlPadStepSlider
import com.github.umer0586.droidpad.ui.components.ControlPadSwitch
import com.github.umer0586.droidpad.ui.components.LEDSTATE
import com.github.umer0586.droidpad.ui.components.propertieseditor.ItemPropertiesEditorSheet
import com.github.umer0586.droidpad.ui.components.rotateBy
import com.github.umer0586.droidpad.ui.theme.DroidPadTheme
import com.github.umer0586.droidpad.ui.utils.LockScreenOrientation
import kotlinx.coroutines.delay


// TODO: Add color picker for choosing background color of ControlPad
@Composable
fun ControlPadBuilderScreen(
    externalData: ExternalData? = null,
    controlPad: ControlPad,
    tempOpen: Boolean = false,
    viewModel: ControlPadBuilderScreenViewModel = hiltViewModel(),
    onSaveClick: (() -> Unit)? = null,
    onBackPress: (() -> Unit)? = null,
    onTempOpenCompleted: ((ExternalData?) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("ControlPadBuilderScreen", "LaunchedEffect(Unit) : ${controlPad.id}")
        if (!tempOpen)
            viewModel.loadControlPadItemsFor(controlPad)
    }

    LockScreenOrientation(
        orientation = when (controlPad.orientation) {
            Orientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Orientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    )

    ControlPadBuilderScreenContent(
        tempOpen = tempOpen,
        controlPad = controlPad,
        uiState = uiState,
        onUiEvent = { event ->
            viewModel.onEvent(event)

            if (event is ControlPadBuilderScreenEvent.OnSaveClick)
                onSaveClick?.invoke()
            else if (event is ControlPadBuilderScreenEvent.OnBackPress)
                onBackPress?.invoke()
            else if (event is ControlPadBuilderScreenEvent.OnTempOpenCompleted)
                onTempOpenCompleted?.invoke(externalData)

        }
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlPadBuilderScreenContent(
    tempOpen: Boolean = false,
    controlPad: ControlPad,
    uiState: ControlPadBuilderScreenState,
    onUiEvent: (ControlPadBuilderScreenEvent) -> Unit

) {

    var showEditorAids by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {

            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .height(bottomBarHeight)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {

                var showModificationAlert by remember { mutableStateOf(false) }

                // When back button on device is pressed
                BackHandler {
                    if (uiState.isModified) {
                        showModificationAlert = true
                        return@BackHandler
                    }
                    onUiEvent(ControlPadBuilderScreenEvent.OnBackPress)
                }

                if (showModificationAlert) {
                    AlertDialog(
                        onDismissRequest = { showModificationAlert = false },
                        title = { Text(text = stringResource(R.string.unsaved_changes)) },
                        text = { Text(text = stringResource(R.string.interface_modified)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showModificationAlert = false
                                    onUiEvent(ControlPadBuilderScreenEvent.OnBackPress)
                                }
                            ) { Text(stringResource(R.string.discard_changes)) }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showModificationAlert = false
                                    onUiEvent(ControlPadBuilderScreenEvent.OnSaveClick)
                                }
                            ) { Text(stringResource(R.string.save_changes)) }
                        }
                    )
                }

                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = {
                        onUiEvent(ControlPadBuilderScreenEvent.OnAddItemClick)
                    },
                    content = {
                        Icon(
                            modifier = Modifier.clickable {

                                if (uiState.isModified) {
                                    showModificationAlert = true
                                    return@clickable
                                }

                                onUiEvent(ControlPadBuilderScreenEvent.OnBackPress)
                            },
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                )

                Row(
                    Modifier
                        .clip(shape = RoundedCornerShape(50.dp))
                        .background(MaterialTheme.colorScheme.onPrimary),
                ) {

                    IconButton(
                        onClick = {
                            onUiEvent(ControlPadBuilderScreenEvent.OnAddItemClick)
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null
                            )
                        }
                    )
                    IconButton(
                        onClick = {
                            showEditorAids = true
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null
                            )
                        }
                    )
                    IconButton(
                        onClick = {
                            onUiEvent(ControlPadBuilderScreenEvent.OnSaveClick)
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = null
                            )
                        }
                    )

                }
            }

        }
    ) { innerPadding ->
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .background(Color(controlPad.backgroundColor.toULong()))
                .padding(innerPadding)
                .clipToBounds()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { onUiEvent(ControlPadBuilderScreenEvent.OnItemSelect(null)) }
        ) {

            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }

            if (uiState.useGridSnap) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridStep = uiState.gridSize * uiState.density
                    val margin = uiState.boundaryMargin
                    val color = Color.LightGray.copy(alpha = 0.2f)

                    var x = margin
                    while (x <= size.width) {
                        drawLine(
                            color = color,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                        x += gridStep
                    }

                    var y = margin
                    while (y <= size.height) {
                        drawLine(
                            color = color,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += gridStep
                    }
                }
            }

            LaunchedEffect(Unit) {
                onUiEvent(
                    ControlPadBuilderScreenEvent.OnResolutionReported(
                        controlPad = controlPad,
                        builderScreenResolution = Resolution(
                            widthPx.toInt(),
                            heightPx.toInt()
                        ),
                        density = density.density,
                        tempOpen = tempOpen
                    )
                )

                if (tempOpen) {
                    delay(5000)
                    onUiEvent(ControlPadBuilderScreenEvent.OnTempOpenCompleted)
                }
            }

            if (tempOpen) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(R.string.please_wait))
                    Spacer(modifier = Modifier.height(20.dp))
                    LinearProgressIndicator()
                }

                return@BoxWithConstraints
            }


            uiState.controlPadItems.forEach { controlPadItem ->

                val baseUnit = uiState.baseUnit.dp

                if (controlPadItem.itemType == ItemType.SWITCH && uiState.transformableStatesMap[controlPadItem.id] != null) {

                    ControlPadSwitch(
                        modifier = Modifier.size(baseUnit),
                        offset = controlPadItem.offset,
                        rotation = controlPadItem.rotation,
                        scale = controlPadItem.scale,
                        transformableState = uiState.transformableStatesMap[controlPadItem.id],
                        properties = SwitchProperties.fromJson(controlPadItem.properties),
                        enabled = false,
                        showControls = uiState.showControls,
                        isSelected = uiState.selectedItemId == controlPadItem.id,
                        onSelect = { onUiEvent(ControlPadBuilderScreenEvent.OnItemSelect(controlPadItem.id)) },
                        onDeleteClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnDeleteItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        },
                        onEditClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnEditItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        }

                    )
                } else if (controlPadItem.itemType == ItemType.SLIDER && uiState.transformableStatesMap[controlPadItem.id] != null) {
                    val properties = SliderProperties.fromJson(controlPadItem.properties)
                    ControlPadSlider(
                        modifier = Modifier.size(width = baseUnit * 2, height = baseUnit),
                        offset = controlPadItem.offset,
                        rotation = controlPadItem.rotation,
                        scale = controlPadItem.scale,
                        transformableState = uiState.transformableStatesMap[controlPadItem.id],
                        properties = properties,
                        enabled = false,
                        showControls = uiState.showControls,
                        isSelected = uiState.selectedItemId == controlPadItem.id,
                        onSelect = { onUiEvent(ControlPadBuilderScreenEvent.OnItemSelect(controlPadItem.id)) },
                        value = (properties.minValue + properties.maxValue) / 2,
                        onDeleteClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnDeleteItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        },
                        onEditClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnEditItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        }


                    )
                } else if (controlPadItem.itemType == ItemType.STEP_SLIDER && uiState.transformableStatesMap[controlPadItem.id] != null) {
                    val properties = StepSliderProperties.fromJson(controlPadItem.properties)
                    ControlPadStepSlider(
                        modifier = Modifier.size(width = baseUnit * 2, height = baseUnit),
                        offset = controlPadItem.offset,
                        rotation = controlPadItem.rotation,
                        scale = controlPadItem.scale,
                        transformableState = uiState.transformableStatesMap[controlPadItem.id],
                        properties = properties,
                        enabled = false,
                        showControls = uiState.showControls,
                        isSelected = uiState.selectedItemId == controlPadItem.id,
                        onSelect = { onUiEvent(ControlPadBuilderScreenEvent.OnItemSelect(controlPadItem.id)) },
                        value = (properties.minValue + properties.maxValue) / 2,
                        onDeleteClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnDeleteItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        },
                        onEditClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnEditItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        }
                    )
                } else if (controlPadItem.itemType == ItemType.LABEL && uiState.transformableStatesMap[controlPadItem.id] != null) {
                    ControlPadLabel(
                        modifier = Modifier.size(width = baseUnit, height = baseUnit / 2),
                        offset = controlPadItem.offset,
                        rotation = controlPadItem.rotation,
                        scale = controlPadItem.scale,
                        transformableState = uiState.transformableStatesMap[controlPadItem.id],
                        properties = LabelProperties.fromJson(controlPadItem.properties),
                        showControls = uiState.showControls,
                        isSelected = uiState.selectedItemId == controlPadItem.id,
                        onSelect = { onUiEvent(ControlPadBuilderScreenEvent.OnItemSelect(controlPadItem.id)) },
                        onDeleteClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnDeleteItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        },
                        onEditClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnEditItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        }
                    )
                } else if (controlPadItem.itemType == ItemType.BUTTON && uiState.transformableStatesMap[controlPadItem.id] != null) {

                    ControlPadButton(
                        modifier = Modifier.size(baseUnit),
                        offset = controlPadItem.offset,
                        rotation = controlPadItem.rotation,
                        scale = controlPadItem.scale,
                        transformableState = uiState.transformableStatesMap[controlPadItem.id],
                        properties = ButtonProperties.fromJson(controlPadItem.properties),
                        enabled = false,
                        showControls = uiState.showControls,
                        isSelected = uiState.selectedItemId == controlPadItem.id,
                        onSelect = { onUiEvent(ControlPadBuilderScreenEvent.OnItemSelect(controlPadItem.id)) },
                        onDeleteClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnDeleteItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        },
                        onEditClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnEditItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        }
                    )
                } else if (controlPadItem.itemType == ItemType.DPAD && uiState.transformableStatesMap[controlPadItem.id] != null) {

                    ControlPadDpad(
                        modifier = Modifier.size(baseUnit * 2),
                        offset = controlPadItem.offset,
                        rotation = controlPadItem.rotation,
                        scale = controlPadItem.scale,
                        transformableState = uiState.transformableStatesMap[controlPadItem.id],
                        properties = DpadProperties.fromJson(controlPadItem.properties),
                        enabled = false,
                        showControls = uiState.showControls,
                        isSelected = uiState.selectedItemId == controlPadItem.id,
                        onSelect = { onUiEvent(ControlPadBuilderScreenEvent.OnItemSelect(controlPadItem.id)) },
                        onDeleteClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnDeleteItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        },
                        onEditClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnEditItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        }
                    )
                } else if (controlPadItem.itemType == ItemType.JOYSTICK && uiState.transformableStatesMap[controlPadItem.id] != null) {

                    ControlPadJoyStick(
                        modifier = Modifier.size(baseUnit * 2),
                        offset = controlPadItem.offset,
                        rotation = controlPadItem.rotation,
                        scale = controlPadItem.scale,
                        transformableState = uiState.transformableStatesMap[controlPadItem.id],
                        properties = JoyStickProperties.fromJson(controlPadItem.properties),
                        enabled = false,
                        showControls = uiState.showControls,
                        isSelected = uiState.selectedItemId == controlPadItem.id,
                        onSelect = { onUiEvent(ControlPadBuilderScreenEvent.OnItemSelect(controlPadItem.id)) },
                        onDeleteClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnDeleteItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        },
                        onEditClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnEditItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        }
                    )
                } else if (controlPadItem.itemType == ItemType.STEERING_WHEEL && uiState.transformableStatesMap[controlPadItem.id] != null) {
                    ControlPadSteeringWheel(
                        modifier = Modifier.size(baseUnit * 2),
                        offset = controlPadItem.offset,
                        rotation = controlPadItem.rotation,
                        scale = controlPadItem.scale,
                        transformableState = uiState.transformableStatesMap[controlPadItem.id],
                        properties = SteeringWheelProperties.fromJson(controlPadItem.properties),
                        enabled = false,
                        showControls = uiState.showControls,
                        isSelected = uiState.selectedItemId == controlPadItem.id,
                        onSelect = { onUiEvent(ControlPadBuilderScreenEvent.OnItemSelect(controlPadItem.id)) },
                        onDeleteClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnDeleteItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        },
                        onEditClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnEditItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        }
                    )
                } else if (controlPadItem.itemType == ItemType.LED && uiState.transformableStatesMap[controlPadItem.id] != null) {

                    ControlPadLED(
                        modifier = Modifier.size(baseUnit),
                        offset = controlPadItem.offset,
                        rotation = controlPadItem.rotation,
                        scale = controlPadItem.scale,
                        transformableState = uiState.transformableStatesMap[controlPadItem.id],
                        properties = LEDProperties.fromJson(controlPadItem.properties),
                        state = LEDSTATE.OFF,
                        showControls = uiState.showControls,
                        isSelected = uiState.selectedItemId == controlPadItem.id,
                        onSelect = { onUiEvent(ControlPadBuilderScreenEvent.OnItemSelect(controlPadItem.id)) },
                        onDeleteClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnDeleteItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        },
                        onEditClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnEditItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        }

                    )
                } else if (controlPadItem.itemType == ItemType.GAUGE && uiState.transformableStatesMap[controlPadItem.id] != null) {

                    ControlPadGauge(
                        modifier = Modifier.size(baseUnit * 2),
                        value = 10f,
                        offset = controlPadItem.offset,
                        rotation = controlPadItem.rotation,
                        scale = controlPadItem.scale,
                        transformableState = uiState.transformableStatesMap[controlPadItem.id],
                        showControls = uiState.showControls,
                        isSelected = uiState.selectedItemId == controlPadItem.id,
                        onSelect = { onUiEvent(ControlPadBuilderScreenEvent.OnItemSelect(controlPadItem.id)) },
                        properties = GaugeProperties.fromJson(controlPadItem.properties)
                            .copy(minValue = 0f, maxValue = 20f),
                        onDeleteClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnDeleteItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        },
                        onEditClick = {
                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnEditItemClick(
                                    controlPadItem = controlPadItem
                                )
                            )
                        }
                    )
                }


            }

            val primary = MaterialTheme.colorScheme.primary
            val onPrimary = MaterialTheme.colorScheme.onPrimary


            if (uiState.showItemChooser) {
                ModalBottomSheet(
                    modifier = Modifier.zIndex(5f),
                    onDismissRequest = { onUiEvent(ControlPadBuilderScreenEvent.OnItemChooserDismissRequest) },
                ) {
                    ItemSelectionBottomSheetContent(
                        onItemClick = { itemType ->

                            val properties = when (itemType) {
                                ItemType.LABEL -> LabelProperties().toJson()
                                ItemType.BUTTON -> ButtonProperties(
                                    buttonColor = primary.value,
                                    textColor = onPrimary.value,
                                    iconColor = onPrimary.value,
                                ).toJson()

                                ItemType.SWITCH -> SwitchProperties(
                                    trackColor = primary.value,
                                    thumbColor = onPrimary.value,
                                ).toJson()

                                ItemType.SLIDER -> SliderProperties(
                                    trackColor = primary.value,
                                    thumbColor = primary.value,
                                ).toJson()

                                ItemType.STEP_SLIDER -> StepSliderProperties(
                                    trackColor = primary.value,
                                    thumbColor = primary.value,
                                ).toJson()

                                ItemType.DPAD -> DpadProperties(
                                    backgroundColor = primary.value,
                                    buttonColor = onPrimary.value,
                                ).toJson()

                                ItemType.JOYSTICK -> JoyStickProperties(
                                    backgroundColor = primary.value,
                                    handleColor = onPrimary.value,
                                ).toJson()

                                ItemType.STEERING_WHEEL -> SteeringWheelProperties(
                                    color = primary.value
                                ).toJson()

                                ItemType.LED -> LEDProperties(
                                    color = primary.value
                                ).toJson()

                                ItemType.GAUGE -> GaugeProperties(
                                    color = primary.value
                                ).toJson()
                            }

                            onUiEvent(
                                ControlPadBuilderScreenEvent.OnItemTypeSelected(
                                    itemType = itemType,
                                    controlPad = controlPad,
                                    properties = properties
                                )
                            )


                        }
                    )

                }
            }

            if (uiState.showItemEditor && uiState.itemToBeEdited != null) {
                ModalBottomSheet(
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    onDismissRequest = { onUiEvent(ControlPadBuilderScreenEvent.OnItemEditorDismissRequest) },
                ) {
                    ItemPropertiesEditorSheet(
                        controlPadItem = uiState.itemToBeEdited,
                        onSaveSubmit = {
                            onUiEvent(ControlPadBuilderScreenEvent.OnItemEditSubmit(it))

                        }
                    )
                }
            }
            if (showEditorAids) {
                ModalBottomSheet(
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    onDismissRequest = { showEditorAids = false }
                ) {
                    EditorAidsBottomSheetContent(uiState = uiState, onUiEvent = onUiEvent)
                }
            }

            if (uiState.showDeleteConfirmation && uiState.itemToBeDeleted != null) {
                AlertDialog(
                    onDismissRequest = { onUiEvent(ControlPadBuilderScreenEvent.OnDeleteConfirmationDismissRequest) },
                    title = { Text(text = stringResource(R.string.delete_item_confirm)) },
                    text = { Text(text = stringResource(R.string.item_will_be_removed, stringResource(uiState.itemToBeDeleted.itemType.getDisplayNameRes()))) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onUiEvent(ControlPadBuilderScreenEvent.OnDeleteItemConfirm)
                            }
                        ) { Text(stringResource(R.string.delete)) }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                onUiEvent(ControlPadBuilderScreenEvent.OnDeleteConfirmationDismissRequest)
                            }
                        ) { Text(stringResource(R.string.cancel)) }
                    }
                )
            }

            if (uiState.selectedItemId != null) {
                val selectedItem = uiState.controlPadItems.find { it.id == uiState.selectedItemId }
                if (selectedItem != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .fillMaxWidth(0.8f)
                            .zIndex(10f),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.scale_item, selectedItem.itemIdentifier, selectedItem.scale),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Slider(
                                value = selectedItem.scale,
                                onValueChange = { onUiEvent(ControlPadBuilderScreenEvent.OnItemScaleChange(selectedItem.id, it)) },
                                valueRange = 0.25f..6f
                            )
                        }
                    }
                }
            }
        }
    }

}


@Composable
private fun ItemSelectionBottomSheetContent(
    modifier: Modifier = Modifier,
    controlPadItemTypes: Array<ItemType> = ItemType.entries.toTypedArray(),
    onItemClick: ((ItemType) -> Unit)? = null
) {

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(controlPadItemTypes) { item ->
            Row(
                modifier = Modifier
                    .clickable {
                        onItemClick?.invoke(item)
                    }
                    .fillMaxWidth(0.5f)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(
                        when (item) {
                            ItemType.SWITCH -> R.drawable.ic_switch
                            ItemType.JOYSTICK -> R.drawable.ic_joystick
                            ItemType.STEERING_WHEEL -> R.drawable.ic_steering_wheel
                            ItemType.DPAD -> R.drawable.ic_dpad
                            ItemType.SLIDER -> R.drawable.ic_slider
                            ItemType.STEP_SLIDER -> R.drawable.ic_slider
                            ItemType.LABEL -> R.drawable.ic_label
                            ItemType.BUTTON -> R.drawable.ic_button_circle
                            ItemType.LED -> R.drawable.ic_light
                            ItemType.GAUGE -> R.drawable.ic_gauge
                        }
                    ),
                    contentDescription = item.name,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    modifier = Modifier.weight(0.3f),
                    text = stringResource(item.getDisplayNameRes())
                )

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun ItemSelectionBottomSheetContentPreview() {
    DroidPadTheme {
        Surface {
            ItemSelectionBottomSheetContent()
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ControlPadBuilderScreenContentInteractiveXPreview() {
    val uiState = remember {
        mutableStateOf(
            ControlPadBuilderScreenState(
                controlPadItems = mutableStateListOf(),
                transformableStatesMap = SnapshotStateMap(),
                isModified = true
            )
        )
    }
    var itemId by remember { mutableStateOf(0L) }

    val controlPad = ControlPad(
        id = 100,
        name = "myController",
        orientation = Orientation.LANDSCAPE,
    )

    val controlPadItems = remember {
        mutableStateListOf(
            ControlPadItem(
                id = 1000,
                itemIdentifier = "label1",
                controlPadId = controlPad.id,
                itemType = ItemType.SWITCH,
            ),
            ControlPadItem(
                id = itemId++,
                offsetY = 100f,
                itemIdentifier = "slider1",
                controlPadId = controlPad.id,
                itemType = ItemType.SLIDER,
            )
        )
    }

    val minScale = 1f
    val maxScale = 6f

    DroidPadTheme {
        ControlPadBuilderScreenContent(
            tempOpen = true,
            controlPad = controlPad,
            uiState = uiState.value,
            onUiEvent = { event ->
                when (event) {
                    is ControlPadBuilderScreenEvent.OnAddItemClick -> {
                        uiState.value = uiState.value.copy(showItemChooser = true)
                    }

                    is ControlPadBuilderScreenEvent.OnDeleteItemClick -> {

                        uiState.value.controlPadItems.remove(event.controlPadItem)
                        uiState.value.transformableStatesMap.remove(event.controlPadItem.id)

                    }

                    is ControlPadBuilderScreenEvent.OnItemTypeSelected -> {
                        uiState.value = uiState.value.copy(showItemChooser = false)

                        itemId++
                        val newItem = ControlPadItem(
                            id = itemId,
                            itemIdentifier = "${event.itemType.name.lowercase()}$itemId",
                            controlPadId = event.controlPad.id,
                            itemType = event.itemType,
                        )
                        uiState.value.controlPadItems.add(newItem)

                        uiState.value.transformableStatesMap[newItem.id] =
                            TransformableState { zoomChange, offsetChange, rotationChange ->

                                val index =
                                    uiState.value.controlPadItems.indexOfFirst { it.id == newItem.id }

                                val controlPadItem = uiState.value.controlPadItems[index]

                                val newScale = controlPadItem.scale * zoomChange
                                val newRotation = controlPadItem.rotation + rotationChange
                                val newOffset =
                                    controlPadItem.offset + offsetChange.rotateBy(newRotation) * newScale

                                uiState.value.controlPadItems[index] =
                                    controlPadItem.copy(
                                        offsetX = newOffset.x,
                                        offsetY = newOffset.y,
                                        rotation = newRotation,
                                        scale = newScale.coerceIn(minScale, maxScale)
                                    )

                            }
                    }

                    is ControlPadBuilderScreenEvent.OnEditItemClick -> {
                        uiState.value = uiState.value.copy(
                            showItemEditor = true,
                            itemToBeEdited = event.controlPadItem
                        )
                    }

                    ControlPadBuilderScreenEvent.OnItemChooserDismissRequest -> {
                        uiState.value = uiState.value.copy(showItemChooser = false)
                    }

                    ControlPadBuilderScreenEvent.OnItemEditorDismissRequest -> {
                        uiState.value = uiState.value.copy(showItemEditor = false)
                    }

                    is ControlPadBuilderScreenEvent.OnItemEditSubmit -> {
                        uiState.value = uiState.value.copy(showItemEditor = false)

                        val index =
                            uiState.value.controlPadItems.indexOfFirst { it.id == event.controlPadItem.id }
                        val controlPadItem = uiState.value.controlPadItems[index]
                        uiState.value.controlPadItems[index] = controlPadItem.copy(
                            itemIdentifier = event.controlPadItem.itemIdentifier,
                            properties = event.controlPadItem.properties
                        )


                        //database operation

                    }

                    else -> TODO("Not Yet Implemented")
                }
            }

        )
    }

}

@Preview(showBackground = true)
@Composable
private fun EditorAidsBottomSheetContentPreview() {
    val uiState = remember {
        mutableStateOf(
            ControlPadBuilderScreenState(
                controlPadItems = mutableStateListOf(),
                transformableStatesMap = SnapshotStateMap(),
                isModified = true
            )
        )
    }
    DroidPadTheme {
        Surface {
            EditorAidsBottomSheetContent(uiState = uiState.value, onUiEvent = {})
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorAidsBottomSheetContent(
    modifier: Modifier = Modifier,
    uiState: ControlPadBuilderScreenState,
    onUiEvent: (ControlPadBuilderScreenEvent) -> Unit)
{

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ListItem(
            modifier = Modifier.fillMaxWidth(0.7f),
            headlineContent = { Text(text = stringResource(R.string.enable_angle_snap)) },
            supportingContent = {
                Text(
                    text = stringResource(R.string.angle_snap_desc),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            trailingContent = {
                Switch(
                    checked = uiState.useAngleSnap,
                    onCheckedChange = {
                        onUiEvent(ControlPadBuilderScreenEvent.OnUseAngleSnapChange(it))
                    }
                )
            }
        )
        AnimatedVisibility(uiState.useAngleSnap) {
            ListItem(
                modifier = Modifier.fillMaxWidth(0.7f),
                headlineContent = {
                    Slider(
                        valueRange = 4f..36f,
                        value = uiState.angleSnapDivision.toFloat(),
                        onValueChange = {newValue ->
                            onUiEvent(ControlPadBuilderScreenEvent.OnAngleSnapChange(newValue))
                        }
                    )
                },
                supportingContent = {
                    val angle = 360f / uiState.angleSnapDivision
                    Text(stringResource(R.string.snap_per_angle, angle, uiState.angleSnapDivision))
                },
            )

        }

        ListItem(
            modifier = Modifier.fillMaxWidth(0.7f),
            headlineContent = { Text(text = stringResource(R.string.enable_grid_snap)) },
            supportingContent = {
                Text(
                    text = stringResource(R.string.grid_snap_desc),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            trailingContent = {
                Switch(
                    checked = uiState.useGridSnap,
                    onCheckedChange = {
                        onUiEvent(ControlPadBuilderScreenEvent.OnUseGridSnapChange(it))
                    }
                )
            }
        )
        AnimatedVisibility(uiState.useGridSnap) {
            ListItem(
                modifier = Modifier.fillMaxWidth(0.7f),
                headlineContent = {
                    Slider(
                        valueRange = 5f..100f,
                        steps = 18,
                        value = uiState.gridSize,
                        onValueChange = {newValue ->
                            onUiEvent(ControlPadBuilderScreenEvent.OnGridSizeChange(newValue))
                        }
                    )
                },
                supportingContent = {
                    Text(stringResource(R.string.grid_size, uiState.gridSize.toInt()))
                },
            )

        }

        ListItem(
            modifier = Modifier.fillMaxWidth(0.7f),
            headlineContent = { Text(text = stringResource(R.string.base_unit_size)) },
            supportingContent = {
                Column {
                    Slider(
                        valueRange = 40f..200f,
                        value = uiState.baseUnit,
                        onValueChange = { newValue ->
                            onUiEvent(ControlPadBuilderScreenEvent.OnBaseUnitChange(newValue))
                        }
                    )
                    Text(
                        text = stringResource(R.string.scale_base_unit, uiState.baseUnit.toInt()),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        )

        ListItem(
            modifier = Modifier.fillMaxWidth(0.7f),
            headlineContent = { Text(text = stringResource(R.string.boundary_margin)) },
            supportingContent = {
                Column {
                    Slider(
                        valueRange = 0f..100f,
                        value = uiState.boundaryMargin,
                        onValueChange = { onUiEvent(ControlPadBuilderScreenEvent.OnBoundaryMarginChange(it)) }
                    )
                    Text(
                        text = stringResource(R.string.margin_px, uiState.boundaryMargin.toInt()),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        )

        ListItem(
            modifier = Modifier.fillMaxWidth(0.7f),
            headlineContent = { Text(text = stringResource(R.string.show_controls)) },
            supportingContent = {
                Text(
                    text = stringResource(R.string.show_item_controls),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            trailingContent = {
                Switch(
                    checked = uiState.showControls,
                    onCheckedChange = {
                        onUiEvent(ControlPadBuilderScreenEvent.OnShowControlsChange(it))
                    }
                )
            }
        )
    }
}

