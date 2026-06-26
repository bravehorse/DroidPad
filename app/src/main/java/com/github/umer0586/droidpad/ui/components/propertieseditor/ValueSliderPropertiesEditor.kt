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

package com.github.umer0586.droidpad.ui.components.propertieseditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.umer0586.droidpad.R
import com.github.umer0586.droidpad.data.ValueSliderProperties
import com.github.umer0586.droidpad.data.database.entities.ControlPadItem
import com.github.umer0586.droidpad.ui.components.ControlPadValueSlider

@Composable
fun ValueSliderPropertiesEditor(
    modifier: Modifier = Modifier,
    controlPadItem: ControlPadItem,
    onValueSliderPropertiesChange: ((ValueSliderProperties) -> Unit)? = null,
) {

    var valueSliderProperties by remember { mutableStateOf(ValueSliderProperties.fromJson(controlPadItem.properties)) }
    
    val textFieldShape = RoundedCornerShape(50.dp)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ControlPadValueSlider(
            offset = Offset.Zero,
            scale = 1f,
            rotation = 0f,
            showControls = false,
            properties = valueSliderProperties,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(0.8f),
            singleLine = true,
            label = { Text(stringResource(R.string.default_value)) },
            value = valueSliderProperties.defaultValue,
            onValueChange = {
                valueSliderProperties = valueSliderProperties.copy(defaultValue = it)
                onValueSliderPropertiesChange?.invoke(valueSliderProperties)
            },
            shape = textFieldShape
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text(stringResource(R.string.value_list)) },
            value = valueSliderProperties.values,
            onValueChange = {
                valueSliderProperties = valueSliderProperties.copy(values = it)
                onValueSliderPropertiesChange?.invoke(valueSliderProperties)
            },
            shape = textFieldShape
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text(stringResource(R.string.label_list)) },
            value = valueSliderProperties.labels,
            onValueChange = {
                valueSliderProperties = valueSliderProperties.copy(labels = it)
                onValueSliderPropertiesChange?.invoke(valueSliderProperties)
            },
            shape = textFieldShape
        )

        ListItem(
            modifier = Modifier.fillMaxWidth(0.7f),
            headlineContent = { Text(text = stringResource(R.string.show_value)) },
            trailingContent = {
                Switch(
                    checked = valueSliderProperties.showValue,
                    onCheckedChange = {
                        valueSliderProperties = valueSliderProperties.copy(showValue = it)
                        onValueSliderPropertiesChange?.invoke(valueSliderProperties)
                    }
                )
            }
        )

        var showColorPickerForThumb by remember { mutableStateOf(false) }
        var showColorPickerForTrack by remember { mutableStateOf(false) }

        AnimatedVisibility(visible = showColorPickerForThumb) {
            ColorPickerWithHex(
                initialColor = Color(valueSliderProperties.thumbColor),
                onColorChanged = { color ->
                    valueSliderProperties = valueSliderProperties.copy(
                        thumbColor = color.value
                    )
                    onValueSliderPropertiesChange?.invoke(valueSliderProperties)
                }
            )
        }

        AnimatedVisibility(visible = showColorPickerForTrack) {
            ColorPickerWithHex(
                initialColor = Color(valueSliderProperties.trackColor),
                onColorChanged = { color ->
                    valueSliderProperties = valueSliderProperties.copy(
                        trackColor = color.value
                    )
                    onValueSliderPropertiesChange?.invoke(valueSliderProperties)
                }
            )
        }

        ListItem(
            modifier = Modifier.fillMaxWidth(0.7f),
            headlineContent = { Text(text = stringResource(R.string.thumb_color)) },
            trailingContent = {
                Box(
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(valueSliderProperties.thumbColor))
                        .clickable {
                            showColorPickerForThumb = !showColorPickerForThumb
                            showColorPickerForTrack = false
                        })
            }
        )

        ListItem(
            modifier = Modifier.fillMaxWidth(0.7f),
            headlineContent = { Text(text = stringResource(R.string.track_color)) },
            trailingContent = {
                Box(
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(valueSliderProperties.trackColor))
                        .clickable {
                            showColorPickerForTrack = !showColorPickerForTrack
                            showColorPickerForThumb = false
                        })
            }
        )

        ListItem(
            modifier = Modifier.fillMaxWidth(0.7f),
            headlineContent = { Text(text = stringResource(R.string.enabled)) },
            trailingContent = {
                Switch(
                    checked = valueSliderProperties.enabled,
                    onCheckedChange = {
                        valueSliderProperties = valueSliderProperties.copy(enabled = it)
                        onValueSliderPropertiesChange?.invoke(valueSliderProperties)
                    }
                )
            }
        )

    }
}
