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


package com.github.umer0586.droidpad.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.umer0586.droidpad.data.ValueSliderProperties
import com.github.umer0586.droidpad.ui.theme.DroidPadTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlPadValueSlider(
    modifier: Modifier = Modifier,
    offset: Offset = Offset.Zero,
    rotation: Float = 0f,
    scale: Float = 1f,
    transformableState: TransformableState? = null,
    properties: ValueSliderProperties = ValueSliderProperties(),
    showControls: Boolean = true,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    selectedIndex: Int = 0,
    onValueChange: ((Int, Float) -> Unit)? = null,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    onSelect: (() -> Unit)? = null,
){
    val valueList = remember(properties.values) {
        properties.values.split(",").map { it.trim().toFloatOrNull() ?: 0f }
    }
    val labelList = remember(properties.labels) {
        properties.labels.split(",").map { it.trim() }
    }

    val maxIndex = (valueList.size - 1).coerceAtLeast(0)
    val safeSelectedIndex = selectedIndex.coerceIn(0, maxIndex)

    ControlPadItemBase(
        modifier = modifier,
        offset = offset,
        rotation = rotation,
        scale = scale,
        transformableState = transformableState,
        showControls = showControls,
        isSelected = isSelected,
        onSelect = onSelect,
        onEditClick = onEditClick,
        onDeleteClick = onDeleteClick,
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(properties.showValue && valueList.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    valueList.forEachIndexed { index, value ->
                        val label = if (index < labelList.size && labelList[index].isNotEmpty()) {
                            labelList[index]
                        } else {
                            if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()
                        }
                        val isCurrentSelected = index == safeSelectedIndex
                        Text(
                            modifier = Modifier.graphicsLayer(rotationZ = -rotation),
                            text = label,
                            color = if (isCurrentSelected) Color(properties.thumbColor) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = if (isCurrentSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            Slider(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                enabled = enabled,
                steps = if (valueList.size > 1) valueList.size - 2 else 0,
                value = safeSelectedIndex.toFloat(),
                valueRange = 0f..maxIndex.toFloat(),
                onValueChange = { 
                    val index = it.toInt()
                    if (index < valueList.size) {
                        onValueChange?.invoke(index, valueList[index])
                    }
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color(properties.thumbColor),
                    activeTrackColor = Color(properties.trackColor),
                    inactiveTrackColor = Color(properties.trackColor),
                    disabledThumbColor = Color(properties.thumbColor),
                    disabledActiveTrackColor = Color(properties.trackColor),
                    disabledInactiveTrackColor = Color(properties.trackColor),
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                    disabledActiveTickColor = Color.Transparent,
                    disabledInactiveTickColor = Color.Transparent
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = 40.dp)
                            .background(
                                color = Color(properties.thumbColor),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ControlPadValueSliderPreview(){
    var selectedIndex by remember { mutableFloatStateOf(0f) }
    DroidPadTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ControlPadValueSlider(
                modifier = Modifier.fillMaxWidth(),
                properties = ValueSliderProperties(
                    showValue = true,
                    values = "0,50,100",
                    labels = "L,M,H"
                ),
                offset = Offset.Zero,
                rotation = 0f,
                selectedIndex = selectedIndex.toInt(),
                onValueChange = { index, _ -> selectedIndex = index.toFloat() }
            )
        }
    }
}
