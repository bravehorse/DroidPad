package com.github.umer0586.droidpad.ui.components.propertieseditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@Composable
fun ColorPickerWithHex(
    initialColor: Color,
    onColorChanged: (Color) -> Unit
) {
    val controller = rememberColorPickerController()
    var hexString by remember(initialColor) { 
        mutableStateOf(String.format("%08X", initialColor.toArgb())) 
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        HsvColorPicker(
            modifier = Modifier.size(200.dp),
            controller = controller,
            initialColor = initialColor,
            onColorChanged = { envelope ->
                if (envelope.fromUser) {
                    val color = envelope.color
                    hexString = String.format("%08X", color.toArgb())
                    onColorChanged(color)
                }
            }
        )

        OutlinedTextField(
            value = hexString,
            onValueChange = { newValue ->
                val formatted = newValue.uppercase().filter { it in "0123456789ABCDEF" }
                if (formatted.length <= 8) {
                    hexString = formatted
                    if (formatted.length == 8) {
                        try {
                            val colorInt = formatted.toLong(16).toInt()
                            val newColor = Color(colorInt)
                            controller.selectByColor(newColor, false)
                            onColorChanged(newColor)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            },
            label = { Text("HEX (ARGB)") },
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.6f),
            singleLine = true,
            shape = RoundedCornerShape(50)
        )
    }
}
