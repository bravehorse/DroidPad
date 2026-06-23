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

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.umer0586.droidpad.data.Resolution
import com.github.umer0586.droidpad.data.database.entities.ControlPad
import com.github.umer0586.droidpad.data.database.entities.ControlPadItem
import com.github.umer0586.droidpad.data.database.entities.ItemType
import com.github.umer0586.droidpad.data.database.entities.Orientation
import com.github.umer0586.droidpad.data.database.entities.offset
import com.github.umer0586.droidpad.data.repositories.ControlPadItemRepository
import com.github.umer0586.droidpad.data.repositories.ControlPadRepository
import com.github.umer0586.droidpad.data.repositories.PreferenceRepository
import com.github.umer0586.droidpad.data.repositories.updatePreference
import com.github.umer0586.droidpad.ui.components.rotateBy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt


data class ControlPadBuilderScreenState(
    val controlPadItems: SnapshotStateList<ControlPadItem> = SnapshotStateList(),
    val showItemChooser: Boolean = false,
    val showItemEditor: Boolean = false,
    val itemToBeEdited: ControlPadItem? = null,
    val transformableStatesMap: SnapshotStateMap<Long, TransformableState> = SnapshotStateMap(),
    val isModified: Boolean = false,
    val useAngleSnap: Boolean = true,
    val angleSnapDivision:Int = 4,
    val useGridSnap: Boolean = true,
    val gridSize: Float = 20f,
    val baseUnit: Float = 80f,
    val showDeleteConfirmation: Boolean = false,
    val itemToBeDeleted: ControlPadItem? = null,
    val showControls: Boolean = true,
    val selectedItemId: Long? = null,
    val boundaryWidth: Int = 0,
    val boundaryHeight: Int = 0,
    val boundaryMargin: Float = 0f,
    val density: Float = 1f
    )

sealed interface ControlPadBuilderScreenEvent {
    data object OnAddItemClick : ControlPadBuilderScreenEvent
    data class OnDeleteItemClick(val controlPadItem: ControlPadItem) :  ControlPadBuilderScreenEvent
    data class OnItemTypeSelected(val itemType: ItemType, val controlPad: ControlPad, val properties: String) :  ControlPadBuilderScreenEvent
    data class OnEditItemClick(val controlPadItem: ControlPadItem) :  ControlPadBuilderScreenEvent
    data class OnItemEditSubmit(val controlPadItem: ControlPadItem) : ControlPadBuilderScreenEvent
    data object OnItemChooserDismissRequest : ControlPadBuilderScreenEvent
    data object OnItemEditorDismissRequest : ControlPadBuilderScreenEvent
    data object OnSaveClick: ControlPadBuilderScreenEvent
    data object OnBackPress: ControlPadBuilderScreenEvent
    data class OnResolutionReported(val controlPad: ControlPad, val builderScreenResolution: Resolution, val density: Float, val tempOpen : Boolean = false) : ControlPadBuilderScreenEvent
    data object OnTempOpenCompleted : ControlPadBuilderScreenEvent
    data class OnUseAngleSnapChange(val useAngleSnap: Boolean): ControlPadBuilderScreenEvent
    data class OnAngleSnapChange(val newValue:Float) : ControlPadBuilderScreenEvent
    data class OnUseGridSnapChange(val useGridSnap: Boolean): ControlPadBuilderScreenEvent
    data class OnGridSizeChange(val newValue: Float) : ControlPadBuilderScreenEvent
    data class OnBaseUnitChange(val newValue: Float) : ControlPadBuilderScreenEvent
    data class OnBoundaryMarginChange(val margin: Float) : ControlPadBuilderScreenEvent
    data class OnItemSelect(val id: Long?) : ControlPadBuilderScreenEvent
    data class OnItemScaleChange(val id: Long, val scale: Float) : ControlPadBuilderScreenEvent
    data object OnDeleteItemConfirm: ControlPadBuilderScreenEvent
    data object OnDeleteConfirmationDismissRequest: ControlPadBuilderScreenEvent
    data class OnShowControlsChange(val showControls: Boolean) : ControlPadBuilderScreenEvent

}

@HiltViewModel
class ControlPadBuilderScreenViewModel @Inject constructor(
    private val controlPadRepository: ControlPadRepository,
    private val controlPadItemRepository: ControlPadItemRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val tag = ControlPadBuilderScreenViewModel::class.simpleName
    private val _uiState = MutableStateFlow(ControlPadBuilderScreenState())
    val uiState = _uiState.asStateFlow()

    private val minScale = 0.25f
    private val maxScale = 6f

    init {
        Log.d(tag,"init ${hashCode()}")
        viewModelScope.launch {
            preferenceRepository.preference.collect { preference ->
                _uiState.update { it.copy(baseUnit = preference.baseUnit) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(tag,"onCleared() ${hashCode()}")
    }

    private fun snappedRotation(input: Float): Float {
        return input.div(360)
            .times(_uiState.value.angleSnapDivision)// Scale the input into [-angleSnapDivision,angleSnapDivision]
            .roundToInt()// Snap!
            .times(360).toFloat()
            .div(_uiState.value.angleSnapDivision)// Scale back to normal and convert
    }

    private fun snappedOffset(input: Float): Float {
        val margin = _uiState.value.boundaryMargin
        val gridSizePx = _uiState.value.gridSize * _uiState.value.density
        return margin + ((input - margin) / gridSizePx).roundToInt() * gridSizePx
    }

    private fun snappedScale(input: Float): Float {
        return (input / 0.25f).roundToInt() * 0.25f
    }

    private fun getItemSize(itemType: ItemType, baseUnit: Float): Offset {
        return when (itemType) {
            ItemType.SWITCH, ItemType.BUTTON, ItemType.LED -> Offset(baseUnit, baseUnit)
            ItemType.SLIDER, ItemType.STEP_SLIDER -> Offset(baseUnit * 2, baseUnit)
            ItemType.LABEL -> Offset(baseUnit, baseUnit / 4)
            ItemType.DPAD, ItemType.JOYSTICK, ItemType.STEERING_WHEEL, ItemType.GAUGE -> Offset(baseUnit * 2, baseUnit * 2)
        }
    }

    fun loadControlPadItemsFor(controlPad: ControlPad){
        _uiState.update {
            it.copy(isModified = false)
        }
        viewModelScope.launch {
            Log.d(tag, "loadControlPadItemsFor: ")
            controlPadRepository.getControlPadItemsOf(controlPad).also{ items ->
                Log.d(tag, items.toString())
                _uiState.value.controlPadItems.clear()
                items.forEach { item ->
                    var rawOffset = item.offset

                    uiState.value.controlPadItems.add(item)
                    uiState.value.transformableStatesMap[item.id] =
                        TransformableState { _, offsetChange, _ ->

                            val index = uiState.value.controlPadItems.indexOfFirst { it.id == item.id }
                            if (index == -1) return@TransformableState
                            val controlPadItem = uiState.value.controlPadItems[index]

                            val currentRotation = controlPadItem.rotation
                            val currentScale = controlPadItem.scale

                            rawOffset += offsetChange.rotateBy(currentRotation) * currentScale

                            val itemSize = getItemSize(controlPadItem.itemType, _uiState.value.baseUnit)
                            val margin = _uiState.value.boundaryMargin
                            val minX = margin
                            val maxX = _uiState.value.boundaryWidth - (itemSize.x * currentScale) - margin
                            val minY = margin
                            val maxY = _uiState.value.boundaryHeight - (itemSize.y * currentScale) - margin

                            val constrainedOffset = Offset(
                                rawOffset.x.coerceIn(minX, maxX.coerceAtLeast(minX)),
                                rawOffset.y.coerceIn(minY, maxY.coerceAtLeast(minY))
                            )

                            val displayOffset = if (_uiState.value.useGridSnap) {
                                Offset(
                                    snappedOffset(constrainedOffset.x),
                                    snappedOffset(constrainedOffset.y)
                                )
                            } else {
                                constrainedOffset
                            }

                            uiState.value.controlPadItems[index] =
                                controlPadItem.copy(
                                    offsetX = displayOffset.x,
                                    offsetY = displayOffset.y
                                )

                            if (_uiState.value.isModified != true) {
                                _uiState.update {
                                    it.copy(isModified = true)
                                }
                            }

                        }
                }

            }
        }
    }

    fun onEvent(event: ControlPadBuilderScreenEvent){
        when(event){
            ControlPadBuilderScreenEvent.OnAddItemClick -> {
                _uiState.update {
                    it.copy(showItemChooser = true)
                }
            }
            is ControlPadBuilderScreenEvent.OnDeleteItemClick -> {
                _uiState.update {
                    it.copy(
                        showDeleteConfirmation = true,
                        itemToBeDeleted = event.controlPadItem
                    )
                }
            }

            is ControlPadBuilderScreenEvent.OnDeleteItemConfirm -> {
                viewModelScope.launch {
                    _uiState.value.itemToBeDeleted?.also { itemToBeDeleted ->
                        controlPadItemRepository.delete(itemToBeDeleted)
                        _uiState.value.controlPadItems.remove(itemToBeDeleted)

                        _uiState.update {
                            it.copy(showDeleteConfirmation = false, selectedItemId = null)
                        }
                    }

                }
            }

            is ControlPadBuilderScreenEvent.OnDeleteConfirmationDismissRequest -> {
                _uiState.update {
                    it.copy(showDeleteConfirmation = false)
                }
            }

            // When user click "Edit" icon on the item
            is ControlPadBuilderScreenEvent.OnEditItemClick -> {
                _uiState.update {
                    it.copy(
                        showItemEditor = true,
                        itemToBeEdited = event.controlPadItem
                    )
                }
            }
            ControlPadBuilderScreenEvent.OnItemChooserDismissRequest -> {
                _uiState.update {
                    it.copy(showItemChooser = false)
                }
            }
            is ControlPadBuilderScreenEvent.OnItemEditSubmit -> {

                _uiState.update { it.copy(showItemEditor = false) }

                viewModelScope.launch {
                    // save changes to database
                    controlPadItemRepository.update(event.controlPadItem)

                    // reflect changes in state
                    val index = uiState.value.controlPadItems.indexOfFirst { it.id == event.controlPadItem.id }
                    if (index != -1) {
                        val controlPadItem = uiState.value.controlPadItems[index]
                        uiState.value.controlPadItems[index] = controlPadItem.copy(
                            itemIdentifier = event.controlPadItem.itemIdentifier,
                            properties = event.controlPadItem.properties
                        )
                    }
                }
            }
            ControlPadBuilderScreenEvent.OnItemEditorDismissRequest -> {
                _uiState.update {
                    it.copy(showItemEditor = false)
                }
            }
            is ControlPadBuilderScreenEvent.OnItemTypeSelected -> {
                _uiState.update { it.copy(showItemChooser = false)  }

                val newItem = ControlPadItem(
                    itemIdentifier = event.itemType.name.lowercase(),
                    controlPadId = event.controlPad.id,
                    itemType = event.itemType,
                    properties = event.properties
                )
                viewModelScope.launch {
                    controlPadItemRepository.save(newItem).also { newId ->
                        controlPadItemRepository.getById(newId).also { newItem ->
                            if (newItem == null) return@also
                            uiState.value.controlPadItems.add(newItem)
                            var rawOffset: Offset = newItem.offset

                            uiState.value.transformableStatesMap[newItem.id] =
                                TransformableState { _, offsetChange, _ ->
                                    val index = uiState.value.controlPadItems.indexOfFirst { it.id == newItem.id }
                                    if (index == -1) return@TransformableState
                                    val controlPadItem = uiState.value.controlPadItems[index]

                                    val currentScale = controlPadItem.scale
                                    val currentRotation = controlPadItem.rotation

                                    rawOffset += offsetChange.rotateBy(currentRotation) * currentScale

                                    val itemSize = getItemSize(controlPadItem.itemType, _uiState.value.baseUnit)
                                    val margin = _uiState.value.boundaryMargin
                                    val minX = margin
                                    val maxX = _uiState.value.boundaryWidth - (itemSize.x * currentScale) - margin
                                    val minY = margin
                                    val maxY = _uiState.value.boundaryHeight - (itemSize.y * currentScale) - margin

                                    val constrainedOffset = Offset(
                                        rawOffset.x.coerceIn(minX, maxX.coerceAtLeast(minX)),
                                        rawOffset.y.coerceIn(minY, maxY.coerceAtLeast(minY))
                                    )

                                    val displayOffset = if (_uiState.value.useGridSnap) {
                                        Offset(
                                            snappedOffset(constrainedOffset.x),
                                            snappedOffset(constrainedOffset.y)
                                        )
                                    } else {
                                        constrainedOffset
                                    }

                                    uiState.value.controlPadItems[index] =
                                        controlPadItem.copy(
                                            offsetX = displayOffset.x,
                                            offsetY = displayOffset.y
                                        )

                                    if (_uiState.value.isModified != true) {
                                        _uiState.update {
                                            it.copy(isModified = true)
                                        }
                                    }

                                }
                        }
                    }

                }


            }

            is ControlPadBuilderScreenEvent.OnSaveClick -> {
                viewModelScope.launch {
                    _uiState.value.controlPadItems.forEach {
                        controlPadItemRepository.update(it)
                    }
                }

            }

            is ControlPadBuilderScreenEvent.OnResolutionReported -> {
                _uiState.update {
                    it.copy(
                        boundaryWidth = event.builderScreenResolution.width,
                        boundaryHeight = event.builderScreenResolution.height,
                        density = event.density
                    )
                }
                saveResolution(
                    controlPad = event.controlPad,
                    builderScreenResolution = event.builderScreenResolution,
                    tempOpen = event.tempOpen
                )

            }

            is ControlPadBuilderScreenEvent.OnUseAngleSnapChange ->{
                _uiState.update {
                    it.copy(useAngleSnap = event.useAngleSnap)
                }
            }
            is ControlPadBuilderScreenEvent.OnAngleSnapChange -> {
                _uiState.update {
                    it.copy(angleSnapDivision = event.newValue.toInt())
                }
            }

            is ControlPadBuilderScreenEvent.OnUseGridSnapChange -> {
                _uiState.update { it.copy(useGridSnap = event.useGridSnap) }
            }

            is ControlPadBuilderScreenEvent.OnGridSizeChange -> {
                _uiState.update { it.copy(gridSize = event.newValue) }
            }

            is ControlPadBuilderScreenEvent.OnBaseUnitChange -> {
                val snappedBase = (event.newValue / 10f).roundToInt() * 10f
                val clampedBase = snappedBase.coerceIn(40f, 200f)
                _uiState.update { it.copy(baseUnit = clampedBase) }
                viewModelScope.launch {
                    preferenceRepository.updatePreference { it.copy(baseUnit = clampedBase) }
                }
            }

            is ControlPadBuilderScreenEvent.OnBoundaryMarginChange -> {
                _uiState.update { it.copy(boundaryMargin = event.margin) }
            }

            is ControlPadBuilderScreenEvent.OnItemSelect -> {
                _uiState.update { it.copy(selectedItemId = event.id) }
            }

            is ControlPadBuilderScreenEvent.OnItemScaleChange -> {
                val index = _uiState.value.controlPadItems.indexOfFirst { it.id == event.id }
                if (index != -1) {
                    val item = _uiState.value.controlPadItems[index]
                    _uiState.value.controlPadItems[index] = item.copy(scale = snappedScale(event.scale))
                    _uiState.update { it.copy(isModified = true) }
                }
            }

            is ControlPadBuilderScreenEvent.OnShowControlsChange -> {
                _uiState.update {
                    it.copy(showControls = event.showControls)
                }
            }


            ControlPadBuilderScreenEvent.OnBackPress -> {}
            ControlPadBuilderScreenEvent.OnTempOpenCompleted -> {}
        }
    }

    private fun saveResolution(controlPad: ControlPad, builderScreenResolution: Resolution, tempOpen: Boolean){

        if(!tempOpen) {
            viewModelScope.launch {
                controlPadRepository.updateControlPad(
                    controlPad.copy(
                        width = builderScreenResolution.width,
                        height = builderScreenResolution.height
                    )
                )
            }
        }

        viewModelScope.launch {

            if (controlPad.orientation == Orientation.PORTRAIT) {
                preferenceRepository.updatePreference { pref ->
                    pref.copy(
                        builderScreenPortraitResolution = builderScreenResolution,
                    )
                }
            }

            if(controlPad.orientation == Orientation.LANDSCAPE) {
                preferenceRepository.updatePreference { pref ->
                    pref.copy(
                        builderScreenLandscapeResolution = builderScreenResolution,
                    )
                }
            }
        }
    }

}
