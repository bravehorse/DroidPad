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


package com.github.umer0586.droidpad.ui.screens.controlpadimporterscreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.umer0586.droidpad.R
import com.github.umer0586.droidpad.data.ExternalData
import com.github.umer0586.droidpad.data.database.entities.ConnectionConfig
import com.github.umer0586.droidpad.data.database.entities.ConnectionType
import com.github.umer0586.droidpad.data.database.entities.ControlPad
import com.github.umer0586.droidpad.data.database.entities.Orientation
import com.github.umer0586.droidpad.ui.components.EnumDropdown
import com.github.umer0586.droidpad.ui.theme.DroidPadTheme

@Composable
fun ControlPadImporterScreen(
    externalData: ExternalData,
    viewModel: ControlPadImporterScreenViewModel = hiltViewModel(),
    onBackPress: (() -> Unit)? = null,
    onBuilderScreenResRequired: ((ExternalData) -> Unit)? = null,
    onControlPadReady: ((ControlPad) -> Unit)? = null,
) {

    viewModel.onControlPadReady {
        onControlPadReady?.invoke(it)
    }

    viewModel.onBuilderScreenResRequired {
        onBuilderScreenResRequired?.invoke(it)
    }

    val uiState by viewModel.uiState.collectAsState()

    ImporterScreenContent(
        externalData = externalData,
        uiState = uiState,
        onEvent = { event ->
            viewModel.onEvent(event)

            if (event is ImporterScreenEvent.OnBackPress)
                onBackPress?.invoke()
        }
    )

}

@Composable
private fun ImporterScreenContent(
    externalData: ExternalData,
    uiState: ImporterScreenState,
    onEvent: (ImporterScreenEvent) -> Unit,
) {


    BackHandler {
        onEvent(ImporterScreenEvent.OnBackPress)
    }

    var showImportOptionsSheet by remember { mutableStateOf(false) }

    if (!LocalInspectionMode.current) {
        LaunchedEffect(Unit) {
            onEvent(ImporterScreenEvent.OnExternalDataProvided(externalData))
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.importing) {
                LinearProgressIndicator()
            } else if (uiState.importFailed) {
                Column {
                    Text(stringResource(R.string.import_failed))
                    Button(
                        onClick = {
                            onEvent(ImporterScreenEvent.OnExternalDataProvided(externalData))
                        }
                    ) { Text(stringResource(R.string.try_again)) }
                }
            } else if (uiState.differentResolutionsDetected) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(50.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    var selectedOptionImportOption by remember { mutableStateOf(ImportOptions.IMPORT_ADJUST_POSITION_CENTER) }

                    Text(
                        text = getDescriptionForImportOption(selectedOptionImportOption),
                        textAlign = TextAlign.Center,
                    )

                    EnumDropdown<ImportOptions>(
                        selectedValue = selectedOptionImportOption,
                        label = stringResource(R.string.import_options),
                        labelMapper = { it.name.replace("IMPORT_", "").replace("_", " ") },
                        onValueSelected = { selectedOptionImportOption = it }
                    )
                    TextButton(
                        modifier = Modifier.fillMaxWidth(0.3f),
                        shape = RoundedCornerShape(50),
                        onClick = {
                            showImportOptionsSheet = false
                            onEvent(
                                ImporterScreenEvent.OnOptionSelected(
                                    externalData,
                                    selectedOptionImportOption
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),

                        ) { Text(stringResource(R.string.import_val)) }
                }
            } else if (uiState.controlPadReady) {
                Text(stringResource(R.string.successfully_imported))
            }
        }
    }


}

@Composable
private fun getDescriptionForImportOption(importOptions: ImportOptions): String {

    return when (importOptions) {
        ImportOptions.IMPORT_UN_CHANGED -> stringResource(R.string.import_desc_unchanged)
        ImportOptions.IMPORT_ADJUST_POSITION -> stringResource(R.string.import_desc_adjust)
        ImportOptions.IMPORT_SCALE -> stringResource(R.string.import_desc_scale)
        ImportOptions.IMPORT_SCALE_CENTER -> stringResource(R.string.import_desc_scale_center)
        ImportOptions.IMPORT_ADJUST_POSITION_CENTER -> stringResource(R.string.import_desc_adjust_center)
    }
}

@Preview
@Composable
fun ImporterScreenContentPreview(modifier: Modifier = Modifier) {


    DroidPadTheme {
        ImporterScreenContent(
            uiState = ImporterScreenState(
                differentResolutionsDetected = true
            ),
            onEvent = {},
            externalData = ExternalData(
                controlPad = ControlPad(
                    name = "test", orientation = Orientation.PORTRAIT
                ),
                controlPadItems = emptyList(),
                connectionConfig = ConnectionConfig(
                    controlPadId = 0, connectionType = ConnectionType.TCP, configJson = "{}"
                )
            )
        )
    }
}