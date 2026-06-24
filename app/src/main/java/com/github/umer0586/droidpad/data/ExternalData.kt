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



@file:UseSerializers(ColorSerializer::class)

package com.github.umer0586.droidpad.data

import com.github.umer0586.droidpad.data.database.entities.ConnectionConfig
import com.github.umer0586.droidpad.data.database.entities.ControlPad
import com.github.umer0586.droidpad.data.database.entities.ControlPadItem
import com.github.umer0586.droidpad.data.database.entities.ItemType
import com.github.umer0586.droidpad.data.util.ColorSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

private val ExportJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class ExternalData(
    val controlPad: ControlPad,
    val controlPadItems: List<ControlPadItem>,
    val connectionConfig: ConnectionConfig
){
    fun toJson(): String {
        val normalizedItems = controlPadItems.map { item ->
            val propertiesJson = item.properties
            val upgradedProperties = try {
                when (item.itemType) {
                    ItemType.LABEL -> LabelProperties.fromJson(propertiesJson).toJson()
                    ItemType.BUTTON -> ButtonProperties.fromJson(propertiesJson).toJson()
                    ItemType.SWITCH -> SwitchProperties.fromJson(propertiesJson).toJson()
                    ItemType.SLIDER -> SliderProperties.fromJson(propertiesJson).toJson()
                    ItemType.STEP_SLIDER -> StepSliderProperties.fromJson(propertiesJson).toJson()
                    ItemType.DPAD -> DpadProperties.fromJson(propertiesJson).toJson()
                    ItemType.JOYSTICK -> JoyStickProperties.fromJson(propertiesJson).toJson()
                    ItemType.STEERING_WHEEL -> SteeringWheelProperties.fromJson(propertiesJson).toJson()
                    ItemType.LED -> LEDProperties.fromJson(propertiesJson).toJson()
                    ItemType.GAUGE -> GaugeProperties.fromJson(propertiesJson).toJson()
                }
            } catch (e: Exception) {
                propertiesJson
            }
            item.copy(properties = upgradedProperties)
        }
        return ExportJson.encodeToString(this.copy(controlPadItems = normalizedItems))
    }

    companion object {
        fun fromJson(json: String) = ExportJson.decodeFromString<ExternalData>(json)
    }
}
