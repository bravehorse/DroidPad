package com.github.umer0586.droidpad.ui.screens.preferencescreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.umer0586.droidpad.R
import com.github.umer0586.droidpad.ui.theme.DroidPadTheme

@Composable
fun PreferenceScreen(
    viewModel: PreferenceScreenViewModel = hiltViewModel(),
    onBackClick: (() -> Unit)? = null
) {

    val uiState by viewModel.uiState.collectAsState()
    PreferenceScreenContent(
        uiState = uiState,
        onEvent = { event ->
            viewModel.onEvent(event)

            if(event is PreferenceScreenEvent.OnBackClick)
                onBackClick?.invoke()
        }
    )


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceScreenContent(
    uiState: PreferenceScreenState,
    onEvent: (PreferenceScreenEvent) -> Unit
) {

    BackHandler {
        onEvent(PreferenceScreenEvent.OnBackClick)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable {
                                onEvent(PreferenceScreenEvent.OnBackClick)
                            },
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                },
                title = { Text(stringResource(R.string.preferences)) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.json_over_bluetooth)) },
                supportingContent = { Text(stringResource(R.string.json_over_bluetooth_desc)) },
                trailingContent = {
                    Switch(
                        checked = uiState.jsonForBluetooth,
                        onCheckedChange = {
                            onEvent(PreferenceScreenEvent.OnJsonForBluetoothChange(it))
                        }
                    )
                }
            )

            ListItem(
                headlineContent = {Text(stringResource(R.string.sensor_sampling_rate))},
                trailingContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.microseconds))
                        Slider(
                            modifier = Modifier.fillMaxWidth(0.5f).padding(start = 10.dp),
                            valueRange = 0f..200000f,
                            value = uiState.sensorSamplingRate.toFloat(),
                            onValueChange = {
                                onEvent(PreferenceScreenEvent.OnSensorSamplingRateChange(it.toInt()))
                            },
                            onValueChangeFinished = {
                                onEvent(PreferenceScreenEvent.OnSensorSamplingRateChangeFinished)
                            }

                        )

                        Text("${uiState.sensorSamplingRate} μs")
                    }
                },
                supportingContent = { Text(stringResource(R.string.sampling_rate_desc))}
            )

            ListItem(
                headlineContent = {Text(stringResource(R.string.vibrate))},
                supportingContent = { Text(stringResource(R.string.vibrate_desc)) },
                trailingContent = {
                    Switch(
                        checked = uiState.vibrate,
                        onCheckedChange = {
                            onEvent(PreferenceScreenEvent.OnVibrateChange(it))
                        }
                    )
                }

            )

            ListItem(
                headlineContent = {Text(stringResource(R.string.keep_screen_on))},
                supportingContent = { Text(stringResource(R.string.keep_screen_on_desc)) },
                trailingContent = {
                    Switch(
                        checked = uiState.keepScreenOn,
                        onCheckedChange = {
                            onEvent(PreferenceScreenEvent.OnKeepScreenOnChange(it))
                        }
                    )
                }

            )
        }
    }

}


@Preview(showBackground = true)
@Composable
private fun PreferenceScreenContentPreview() {

    DroidPadTheme {
        PreferenceScreenContent(
            uiState = PreferenceScreenState(),
            onEvent = {}
        )
    }

}