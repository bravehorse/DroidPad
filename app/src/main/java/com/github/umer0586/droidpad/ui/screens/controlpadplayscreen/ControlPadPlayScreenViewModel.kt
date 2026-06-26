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

package com.github.umer0586.droidpad.ui.screens.controlpadplayscreen

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.umer0586.droidpad.data.ButtonEvent
import com.github.umer0586.droidpad.data.ButtonProperties
import com.github.umer0586.droidpad.data.DPadEvent
import com.github.umer0586.droidpad.data.GaugeEvent
import com.github.umer0586.droidpad.data.GaugeProperties
import com.github.umer0586.droidpad.data.JoyStickEvent
import com.github.umer0586.droidpad.data.LabelEvent
import com.github.umer0586.droidpad.data.LedEvent
import com.github.umer0586.droidpad.data.LogEvent
import com.github.umer0586.droidpad.data.SliderEvent
import com.github.umer0586.droidpad.data.SliderProperties
import com.github.umer0586.droidpad.data.StepSliderProperties
import com.github.umer0586.droidpad.data.SteeringWheelEvent
import com.github.umer0586.droidpad.data.SwitchEvent
import com.github.umer0586.droidpad.data.SwitchProperties
import com.github.umer0586.droidpad.data.ValueSliderEvent
import com.github.umer0586.droidpad.data.ValueSliderProperties
import com.github.umer0586.droidpad.data.connection.BluetoothConnection
import com.github.umer0586.droidpad.data.connection.BluetoothLEConnection
import com.github.umer0586.droidpad.data.connection.Connection
import com.github.umer0586.droidpad.data.connection.ConnectionFactory
import com.github.umer0586.droidpad.data.connection.ConnectionState
import com.github.umer0586.droidpad.data.connection.Mqttv3Connection
import com.github.umer0586.droidpad.data.connection.Mqttv5Connection
import com.github.umer0586.droidpad.data.connection.TCPConnection
import com.github.umer0586.droidpad.data.connection.UDPConnection
import com.github.umer0586.droidpad.data.connection.WebsocketConnection
import com.github.umer0586.droidpad.data.connection.WebsocketServerConnection
import com.github.umer0586.droidpad.data.database.entities.ConnectionType
import com.github.umer0586.droidpad.data.database.entities.ControlPad
import com.github.umer0586.droidpad.data.database.entities.ControlPadItem
import com.github.umer0586.droidpad.data.database.entities.ItemType
import com.github.umer0586.droidpad.data.repositories.ConnectionConfigRepository
import com.github.umer0586.droidpad.data.repositories.ControlPadRepository
import com.github.umer0586.droidpad.data.repositories.ControlPadSensorRepository
import com.github.umer0586.droidpad.data.repositories.PreferenceRepository
import com.github.umer0586.droidpad.data.sensor.SensorEventProvider
import com.github.umer0586.droidpad.data.util.bluetooth.BluetoothUtil
import com.github.umer0586.droidpad.data.util.vibrator.VibratorUtil
import com.github.umer0586.droidpad.ui.components.DPAD_BUTTON
import com.github.umer0586.droidpad.ui.components.LEDSTATE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject


data class ControlPadPlayScreenState(
    val controlPadItems: List<ControlPadItem> = emptyList(),
    val connectionState: ConnectionState = ConnectionState.NONE,
    val switchStates: SnapshotStateMap<Long,Boolean> = mutableStateMapOf(),
    val sliderStates: SnapshotStateMap<Long,Float> = mutableStateMapOf(),
    val valueSliderStates: SnapshotStateMap<Long, Int> = mutableStateMapOf(),
    val ledStates: SnapshotStateMap<Long, LEDSTATE> = mutableStateMapOf(),
    val labelStates: SnapshotStateMap<Long, String> = mutableStateMapOf(),
    val logState: SnapshotStateList<LogEvent> = mutableStateListOf(),
    val gaugeStates: SnapshotStateMap<Long, Float> = mutableStateMapOf(),
    val enabledStates: SnapshotStateMap<Long, Boolean> = mutableStateMapOf(),
    val sliderProperties: SnapshotStateMap<Long, SliderProperties> = mutableStateMapOf(),
    val stepSliderProperties: SnapshotStateMap<Long, StepSliderProperties> = mutableStateMapOf(),
    val connectionType: ConnectionType = ConnectionType.TCP,
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val controlPadBackgroundColor : ULong = Color.Red.value,
    val hostAddress: String = "",
    val isBluetoothEnabled: Boolean = false,
    val keepScreenOn: Boolean = false,
    val baseUnit: Float = 80f
)
sealed interface ControlPadPlayScreenEvent {
    data object OnConnectClick : ControlPadPlayScreenEvent
    data object OnDisconnectClick : ControlPadPlayScreenEvent
    data class OnSwitchCheckedChange(val id: String, val idLong: Long, val checked: Boolean) : ControlPadPlayScreenEvent
    data class OnSliderValueChange(val id: String, val idLong: Long, val value: Float) : ControlPadPlayScreenEvent
    data class OnValueSliderValueChange(val id: String, val idLong: Long, val index: Int, val value: Float) : ControlPadPlayScreenEvent
    data class OnButtonPress(val id: String) : ControlPadPlayScreenEvent
    data class OnButtonRelease(val id: String) : ControlPadPlayScreenEvent
    data class OnButtonClick(val id: String) : ControlPadPlayScreenEvent
    data class OnDpadButtonPress(val id: String, val dPadButton: DPAD_BUTTON) : ControlPadPlayScreenEvent
    data class OnDpadButtonRelease(val id: String, val dPadButton: DPAD_BUTTON) : ControlPadPlayScreenEvent
    data class OnDpadButtonClick(val id: String, val dPadButton: DPAD_BUTTON) : ControlPadPlayScreenEvent
    data class OnJoyStickMove(val id: String, val x: Float, val y: Float) : ControlPadPlayScreenEvent
    data class OnSteeringWheelRotate(val id: String, val angle: Float) : ControlPadPlayScreenEvent
    data object OnBackPress : ControlPadPlayScreenEvent
}



@HiltViewModel
class ControlPadPlayScreenViewModel @Inject constructor(
    private val controlPadRepository: ControlPadRepository,
    private val connectionConfigRepository: ConnectionConfigRepository,
    private val connectionFactory: ConnectionFactory,
    private val bluetoothUtil: BluetoothUtil,
    private val preferenceRepository: PreferenceRepository,
    private val controlPadSensorRepository: ControlPadSensorRepository,
    private val sensorEventProvider: SensorEventProvider,
    private val vibratorUtil: VibratorUtil
) : ViewModel() {

    private var _uiState = MutableStateFlow(
        ControlPadPlayScreenState()
    )

    val uiState = _uiState.asStateFlow()

    private var connection: Connection? = null
    private var sendJsonOverBluetooth = false
    private var samplingRate = 200000
    private var vibrate = false

    private val tag = javaClass.simpleName

    init {

        Log.d(tag, "init : ${hashCode()}")
        viewModelScope.launch {
            bluetoothUtil.bluetoothState.collect{ bluetoothState ->
                _uiState.update { it.copy(isBluetoothEnabled = bluetoothState.isEnable) }
            }
        }

        viewModelScope.launch {
            preferenceRepository.preference.collect{ preference->
                sendJsonOverBluetooth = preference.sendJsonOverBluetooth
                samplingRate = preference.sensorSamplingRate
                vibrate = preference.vibrate
                _uiState.update {
                    it.copy(
                        keepScreenOn = preference.keepScreenOn,
                        baseUnit = preference.baseUnit
                    )
                }
            }
        }

        viewModelScope.launch {
            // Even though connection.sendData(data) is safe to call on the main thread,
            // we collect the sensor event flow on the IO dispatcher to avoid frequent
            // execution on the main thread, as sensor events are emitted very frequently.
            sensorEventProvider.events.flowOn(Dispatchers.IO).collect{ sensorEvent ->
                val data = if((connection?.connectionType == ConnectionType.BLUETOOTH_LE || connection?.connectionType == ConnectionType.BLUETOOTH) && !sendJsonOverBluetooth)
                    sensorEvent.toCsv()
                else
                    sensorEvent.toJson()

                    connection?.sendData(data)

            }
        }

    }

    fun loadControlPadItemsFor(controlPad: ControlPad) {

        viewModelScope.launch {
            val controlPadItems = controlPadRepository.getControlPadItemsOf(controlPad)
            _uiState.value = _uiState.value.copy(
                controlPadItems = controlPadItems,
                controlPadBackgroundColor = controlPad.backgroundColor,
            )


            controlPadRepository.getControlPadItemsOf(controlPad)
                .filter { it.itemType == ItemType.SWITCH }.forEach { switch ->
                val properties = SwitchProperties.fromJson(switch.properties)
                uiState.value.switchStates[switch.id] = false
                uiState.value.enabledStates[switch.id] = properties.enabled
            }

            controlPadRepository.getControlPadItemsOf(controlPad)
                .filter { it.itemType == ItemType.BUTTON }.forEach { button ->
                    val properties = ButtonProperties.fromJson(button.properties)
                    uiState.value.enabledStates[button.id] = properties.enabled
                }

            controlPadRepository.getControlPadItemsOf(controlPad)
                .filter { it.itemType == ItemType.LED }.forEach { led ->
                uiState.value.ledStates[led.id] = LEDSTATE.OFF
            }

            controlPadRepository.getControlPadItemsOf(controlPad)
                .filter { it.itemType == ItemType.LABEL }.forEach { label ->
                    uiState.value.labelStates[label.id] = ""
                }

            uiState.value.logState.clear()

            controlPadRepository.getControlPadItemsOf(controlPad)
                .filter { it.itemType == ItemType.SLIDER }.forEach { slider ->
                    val sliderProperties = SliderProperties.fromJson(slider.properties)
                    uiState.value.sliderStates[slider.id] = sliderProperties.minValue
                    uiState.value.enabledStates[slider.id] = sliderProperties.enabled
                    uiState.value.sliderProperties[slider.id] = sliderProperties
                }

            controlPadRepository.getControlPadItemsOf(controlPad)
                .filter { it.itemType == ItemType.STEP_SLIDER }.forEach { slider ->
                    val sliderProperties = StepSliderProperties.fromJson(slider.properties)
                    uiState.value.sliderStates[slider.id] = sliderProperties.minValue
                    uiState.value.enabledStates[slider.id] = sliderProperties.enabled
                    uiState.value.stepSliderProperties[slider.id] = sliderProperties
                }

            controlPadRepository.getControlPadItemsOf(controlPad)
                .filter { it.itemType == ItemType.VALUE_SLIDER }.forEach { slider ->
                    val properties = ValueSliderProperties.fromJson(slider.properties)
                    val valueList = properties.values.split(",").map { it.trim().toFloatOrNull() ?: 0f }
                    val defaultIndex = valueList.indexOf(properties.defaultValue).coerceAtLeast(0)
                    uiState.value.valueSliderStates[slider.id] = defaultIndex
                    uiState.value.enabledStates[slider.id] = properties.enabled
                }

            controlPadRepository.getControlPadItemsOf(controlPad)
                .filter { it.itemType == ItemType.GAUGE }.forEach { gauge ->
                    val gaugeProperties = GaugeProperties.fromJson(gauge.properties)
                    uiState.value.gaugeStates[gauge.id] = gaugeProperties.minValue
                }

            connectionConfigRepository.getConfigForControlPad(controlPad.id)
                ?.also { connectionConfig ->
                    connection = connectionFactory.getConnection(connectionConfig, scope = viewModelScope)

                    if(connection?.connectionType == ConnectionType.UDP)
                        connection?.setup()

                    handleIncomingData(controlPad, controlPadItems)

                    _uiState.update {
                        it.copy(
                            connectionType = connectionConfig.connectionType,
                            hostAddress = when(connectionConfig.connectionType){
                                ConnectionType.TCP -> (connection as TCPConnection).tcpConfig.address
                                ConnectionType.UDP -> (connection as UDPConnection).udpConfig.address
                                ConnectionType.WEBSOCKET -> (connection as WebsocketConnection).webSocketConfig.address
                                ConnectionType.WEBSOCKET_SERVER -> (connection as WebsocketServerConnection).websocketServerConfig.address
                                ConnectionType.MQTT_V5 -> (connection as Mqttv5Connection).mqttConfig.brokerAddress
                                ConnectionType.MQTT_V3 -> (connection as Mqttv3Connection).mqttConfig.brokerAddress
                                ConnectionType.BLUETOOTH_LE -> (connection as BluetoothLEConnection).bluetoothDisplayName
                                ConnectionType.BLUETOOTH -> (connection as BluetoothConnection).bluetoothConfig.remoteDevice?.address ?: "No Device"

                            }
                        )
                    }

                    if(connectionConfig.connectionType == ConnectionType.UDP){
                        viewModelScope.launch {
                            val controlPadSensorsTypes = controlPadSensorRepository.getControlPadSensorsByControlPadId(controlPad.id).map { it.sensorType }
                            sensorEventProvider.provideEventsFor(controlPadSensorsTypes, samplingRate)
                        }
                    }

                    if(connectionConfig.connectionType == ConnectionType.WEBSOCKET_SERVER){
                        launch {
                            (connection as WebsocketServerConnection).hostAddress.filterNotNull().collect{ hostAddress ->
                                _uiState.update { it.copy(hostAddress = hostAddress) }
                            }
                        }
                    }


                    launch {
                        connection?.connectionState?.collect { connectionState ->
                            Log.d("Play", "Connection state: $connectionState")

                            val isConnecting = when(connectionState){
                                ConnectionState.TCP_CONNECTING -> true
                                ConnectionState.WEBSOCKET_CONNECTING ->true
                                ConnectionState.MQTT_CONNECTING -> true
                                ConnectionState.BLUETOOTH_CONNECTING -> true
                                else -> false
                            }


                            val isConnected = when(connectionState){
                                ConnectionState.TCP_CONNECTED -> true
                                ConnectionState.WEBSOCKET_CONNECTED -> true
                                ConnectionState.MQTT_CONNECTED -> true
                                ConnectionState.BLUETOOTH_CLIENT_CONNECTED -> true
                                ConnectionState.BLUETOOTH_CONNECTED -> true
                                // Treat WebSocket server start as a connected state
                                // to avoid introducing a separate state variable and additional logic
                                ConnectionState.WEBSOCKET_SERVER_STARTED -> true
                                else -> false
                            }

                            if (isConnected) {
                                launch {
                                    val controlPadSensorsTypes =
                                        controlPadSensorRepository.getControlPadSensorsByControlPadId(
                                            controlPad.id
                                        ).map { it.sensorType }
                                    sensorEventProvider.provideEventsFor(controlPadSensorsTypes, samplingRate)
                                }
                            } else if(!isConnecting){ // if not connected and not connecting
                                // if not connected and not connecting then it means we are in disconnected state,
                                sensorEventProvider.stopProvidingEvents()
                            }


                            _uiState.update { uiState ->
                                uiState.copy(
                                    isConnecting = isConnecting,
                                    connectionState = connectionState,
                                    isConnected = isConnected
                                )
                            }


                        }
                    }
                }
        }


    }

    fun onEvent(event: ControlPadPlayScreenEvent) {
        when (event) {
            is ControlPadPlayScreenEvent.OnConnectClick -> {

                if(connection?.connectionType == ConnectionType.BLUETOOTH_LE || connection?.connectionType == ConnectionType.BLUETOOTH){
                    _uiState.update {
                        it.copy(isBluetoothEnabled = bluetoothUtil.isBluetoothEnabled())
                    }
                    if(!bluetoothUtil.isBluetoothEnabled())
                        return
                }


                viewModelScope.launch {
                    connection?.setup()
                }
            }

            is ControlPadPlayScreenEvent.OnDisconnectClick -> {
                viewModelScope.launch {
                    connection?.tearDown()
                }
                sensorEventProvider.stopProvidingEvents()
            }

            is ControlPadPlayScreenEvent.OnSwitchCheckedChange -> {
                val data = if((connection?.connectionType == ConnectionType.BLUETOOTH_LE || connection?.connectionType == ConnectionType.BLUETOOTH) && !sendJsonOverBluetooth)
                    SwitchEvent(id = event.id, state = event.checked).toCSV()
                else
                    SwitchEvent(id = event.id, state = event.checked).toJson()

                uiState.value.switchStates[event.idLong] = event.checked

                vibrate()
                viewModelScope.launch {
                    connection?.sendData(data)
                }

            }


            is ControlPadPlayScreenEvent.OnSliderValueChange -> {

                val roundedValue = String.format("%.3f", event.value).toFloat()

                if (uiState.value.sliderStates[event.idLong] == roundedValue) return

                val data = if((connection?.connectionType == ConnectionType.BLUETOOTH_LE || connection?.connectionType == ConnectionType.BLUETOOTH) && !sendJsonOverBluetooth)
                    SliderEvent(id = event.id, value = roundedValue).toCSV()
                else
                    SliderEvent(id = event.id, value = roundedValue).toJson()

                uiState.value.sliderStates[event.idLong] = roundedValue

                viewModelScope.launch {
                    connection?.sendData(data)
                }
            }

            is ControlPadPlayScreenEvent.OnValueSliderValueChange -> {
                if (uiState.value.valueSliderStates[event.idLong] == event.index) return

                val data = if((connection?.connectionType == ConnectionType.BLUETOOTH_LE || connection?.connectionType == ConnectionType.BLUETOOTH) && !sendJsonOverBluetooth)
                    ValueSliderEvent(id = event.id, value = event.value).toCSV()
                else
                    ValueSliderEvent(id = event.id, value = event.value).toJson()

                uiState.value.valueSliderStates[event.idLong] = event.index

                viewModelScope.launch {
                    connection?.sendData(data)
                }
            }

            is ControlPadPlayScreenEvent.OnButtonClick -> {

                val data = if((connection?.connectionType == ConnectionType.BLUETOOTH_LE || connection?.connectionType == ConnectionType.BLUETOOTH) && !sendJsonOverBluetooth)
                    ButtonEvent(id = event.id, state = "CLICK").toCSV()
                else
                    ButtonEvent(id = event.id, state = "CLICK").toJson()

                vibrate()
                viewModelScope.launch {
                    connection?.sendData(data)
                }
            }

            is ControlPadPlayScreenEvent.OnBackPress -> {
                viewModelScope.launch {
                    connection?.tearDown()
                }
                sensorEventProvider.stopProvidingEvents()
            }

            is ControlPadPlayScreenEvent.OnButtonPress -> {

                val data = if((connection?.connectionType == ConnectionType.BLUETOOTH_LE || connection?.connectionType == ConnectionType.BLUETOOTH) && !sendJsonOverBluetooth)
                    ButtonEvent(id = event.id, state = "PRESS").toCSV()
                else
                    ButtonEvent(id = event.id, state = "PRESS").toJson()

                vibrate()
                viewModelScope.launch {
                    connection?.sendData(data)
                }
            }
            is ControlPadPlayScreenEvent.OnButtonRelease -> {
                val data = if((connection?.connectionType == ConnectionType.BLUETOOTH_LE || connection?.connectionType == ConnectionType.BLUETOOTH) && !sendJsonOverBluetooth)
                    ButtonEvent(id = event.id, state = "RELEASE").toCSV()
                else
                    ButtonEvent(id = event.id, state = "RELEASE").toJson()

                vibrate()
                viewModelScope.launch {
                    connection?.sendData(data)
                }
            }

            is ControlPadPlayScreenEvent.OnDpadButtonClick -> {

                val data = if((connection?.connectionType == ConnectionType.BLUETOOTH_LE || connection?.connectionType == ConnectionType.BLUETOOTH) && !sendJsonOverBluetooth)
                    DPadEvent(id = event.id, button = event.dPadButton, state = "CLICK").toCSV()
                else
                    DPadEvent(id = event.id, button = event.dPadButton, state = "CLICK").toJson()

                vibrate()
                viewModelScope.launch {
                    connection?.sendData(data)
                }
            }
            is ControlPadPlayScreenEvent.OnDpadButtonPress -> {

                val data = if((connection?.connectionType == ConnectionType.BLUETOOTH_LE || connection?.connectionType == ConnectionType.BLUETOOTH) && !sendJsonOverBluetooth)
                    DPadEvent(id = event.id, button = event.dPadButton, state = "PRESS").toCSV()
                else
                    DPadEvent(id = event.id, button = event.dPadButton, state = "PRESS").toJson()


                vibrate()
                viewModelScope.launch {
                    connection?.sendData(data)
                }
            }
            is ControlPadPlayScreenEvent.OnDpadButtonRelease -> {

                val data = if((connection?.connectionType == ConnectionType.BLUETOOTH_LE || connection?.connectionType == ConnectionType.BLUETOOTH) && !sendJsonOverBluetooth)
                    DPadEvent(id = event.id, button = event.dPadButton, state = "RELEASE").toCSV()
                else
                    DPadEvent(id = event.id, button = event.dPadButton, state = "RELEASE").toJson()

                vibrate()
                viewModelScope.launch {
                    connection?.sendData(data)
                }
            }

            is ControlPadPlayScreenEvent.OnJoyStickMove -> {
                val data = if((connection?.connectionType == ConnectionType.BLUETOOTH_LE || connection?.connectionType == ConnectionType.BLUETOOTH) && !sendJsonOverBluetooth)
                    JoyStickEvent(id = event.id, x = event.x, y = event.y).toCSV()
                else
                    JoyStickEvent(id = event.id, x = event.x, y = event.y).toJson()

                viewModelScope.launch {
                    connection?.sendData(data)
                }
            }

            is ControlPadPlayScreenEvent.OnSteeringWheelRotate -> {
                val data = if((connection?.connectionType == ConnectionType.BLUETOOTH_LE || connection?.connectionType == ConnectionType.BLUETOOTH) && !sendJsonOverBluetooth)
                    SteeringWheelEvent(id = event.id, angle = event.angle).toCSV()
                else
                    SteeringWheelEvent(id = event.id, angle = event.angle).toJson()

                viewModelScope.launch {
                    connection?.sendData(data)
                }

            }
        }
    }

    private fun vibrate() {
        if(vibrate){
            vibratorUtil.vibrate()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            connection?.tearDown()
        }

        sensorEventProvider.stopProvidingEvents()
        sensorEventProvider.cleanUp()
        bluetoothUtil.cleanUp()
        Log.d(tag, "onCleared: ${hashCode()}")
    }

    private fun handleIncomingData(controlPad: ControlPad, controlPadItems: List<ControlPadItem>){
        viewModelScope.launch {
            connection?.receivedData?.collect{ jsonString ->

                val jsonElement = try {
                    Json.parseToJsonElement(jsonString)
                }catch (e: Exception){
                    e.printStackTrace()
                    return@collect
                }

                if(jsonElement is JsonObject){

                    try {

                        if ("type" in jsonElement.keys && jsonElement["type"]?.jsonPrimitive?.content == "SWITCH") {
                            val switchEvent = SwitchEvent.fromJson(jsonString)
                            controlPadItems.filter { it.itemType == ItemType.SWITCH }
                                .find { switchItem ->
                                    switchItem.itemIdentifier == switchEvent.id
                                }?.also { switchItem ->
                                    switchEvent.state?.let { uiState.value.switchStates[switchItem.id] = it }
                                    switchEvent.enabled?.let { uiState.value.enabledStates[switchItem.id] = it }
                                }
                        }
                        else if ("type" in jsonElement.keys && jsonElement["type"]?.jsonPrimitive?.content == "BUTTON") {
                            val buttonEvent = ButtonEvent.fromJson(jsonString)
                            controlPadItems.filter { it.itemType == ItemType.BUTTON }
                                .find { buttonItem ->
                                    buttonItem.itemIdentifier == buttonEvent.id
                                }?.also { buttonItem ->
                                    buttonEvent.enabled?.let { uiState.value.enabledStates[buttonItem.id] = it }
                                }
                        }
                        else if ("type" in jsonElement.keys && jsonElement["type"]?.jsonPrimitive?.content == "SLIDER") {
                            val sliderEvent = SliderEvent.fromJson(jsonString)
                            controlPadItems.filter { it.itemType == ItemType.SLIDER || it.itemType == ItemType.STEP_SLIDER }
                                .find { sliderItem ->
                                    sliderItem.itemIdentifier == sliderEvent.id
                                }?.also { sliderItem ->
                                    if(sliderItem.itemType == ItemType.SLIDER){
                                        val props = uiState.value.sliderProperties[sliderItem.id] ?: SliderProperties.fromJson(sliderItem.properties)
                                        val newProps = props.copy(
                                            minValue = sliderEvent.minValue ?: props.minValue,
                                            maxValue = sliderEvent.maxValue ?: props.maxValue,
                                            enabled = sliderEvent.enabled ?: props.enabled
                                        )
                                        uiState.value.sliderProperties[sliderItem.id] = newProps
                                        sliderEvent.value?.let { uiState.value.sliderStates[sliderItem.id] = it.coerceIn(newProps.minValue, newProps.maxValue) }
                                        sliderEvent.enabled?.let { uiState.value.enabledStates[sliderItem.id] = it }
                                    } else {
                                        val props = uiState.value.stepSliderProperties[sliderItem.id] ?: StepSliderProperties.fromJson(sliderItem.properties)
                                        val newProps = props.copy(
                                            minValue = sliderEvent.minValue ?: props.minValue,
                                            maxValue = sliderEvent.maxValue ?: props.maxValue,
                                            steps = sliderEvent.steps ?: props.steps,
                                            enabled = sliderEvent.enabled ?: props.enabled
                                        )
                                        uiState.value.stepSliderProperties[sliderItem.id] = newProps
                                        sliderEvent.value?.let { uiState.value.sliderStates[sliderItem.id] = it.coerceIn(newProps.minValue, newProps.maxValue) }
                                        sliderEvent.enabled?.let { uiState.value.enabledStates[sliderItem.id] = it }
                                    }
                                }
                        }
                        else if ("type" in jsonElement.keys && jsonElement["type"]?.jsonPrimitive?.content == "VALUE_SLIDER") {
                            val valueSliderEvent = ValueSliderEvent.fromJson(jsonString)
                            controlPadItems.filter { it.itemType == ItemType.VALUE_SLIDER }
                                .find { item ->
                                    item.itemIdentifier == valueSliderEvent.id
                                }?.also { item ->
                                    valueSliderEvent.value?.let { value ->
                                        val properties = ValueSliderProperties.fromJson(item.properties)
                                        val valueList = properties.values.split(",").map { it.trim().toFloatOrNull() ?: 0f }
                                        val index = valueList.indexOf(value)
                                        if (index != -1) {
                                            uiState.value.valueSliderStates[item.id] = index
                                        }
                                    }
                                    valueSliderEvent.enabled?.let { uiState.value.enabledStates[item.id] = it }
                                }
                        }
                        else if("type" in jsonElement.keys && jsonElement["type"]?.jsonPrimitive?.content == "LED"){
                            val ledEvent = LedEvent.fromJson(jsonString)
                            controlPadItems.filter { it.itemType == ItemType.LED }
                                .find { ledItem ->
                                    ledItem.itemIdentifier == ledEvent.id
                                }?.also { ledItem ->
                                    uiState.value.ledStates[ledItem.id] = ledEvent.state
                                }
                        }
                        else if(controlPad.logging && "type" in jsonElement.keys && jsonElement["type"]?.jsonPrimitive?.content == "LOG"){

                            val timestamp = SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(Date())
                            val logEvent = LogEvent.fromJson(jsonString).copy(timestamp = timestamp)

                            uiState.value.logState.add(logEvent)
                        }
                        else if("type" in jsonElement.keys && jsonElement["type"]?.jsonPrimitive?.content == "GAUGE"){
                            val gaugeEvent = GaugeEvent.fromJson(jsonString)
                            controlPadItems.filter { it.itemType == ItemType.GAUGE }
                                .find { gaugeItem ->
                                    gaugeItem.itemIdentifier == gaugeEvent.id
                                }?.also { gaugeItem ->
                                    uiState.value.gaugeStates[gaugeItem.id] = gaugeEvent.value
                                }
                        }
                        else if("type" in jsonElement.keys && jsonElement["type"]?.jsonPrimitive?.content == "LABEL"){
                            val labelEvent = LabelEvent.fromJson(jsonString)
                            controlPadItems.filter { it.itemType == ItemType.LABEL }
                                .find { labelItem ->
                                    labelItem.itemIdentifier == labelEvent.id
                                }?.also { labelItem ->
                                    uiState.value.labelStates[labelItem.id] = labelEvent.text
                                }
                        }


                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }
        }
    }



}

