package com.example.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.presentation.viewmodel.ThemeViewModel
import com.example.domain.model.ThemeSettings

@Composable
fun ThemeSettingsScreen(
    themeViewModel: ThemeViewModel,
    uuid: String,
    onBackClick: () -> Unit
) {
    val themeSettings by themeViewModel.themeSettings.collectAsStateWithLifecycle()

    var backgroundColor by remember { mutableStateOf(themeSettings.backgroundColor) }
    var textColor by remember { mutableStateOf(themeSettings.textColor) }

    val backgroundColorAsColor = Color(android.graphics.Color.parseColor(backgroundColor))
//    var isDarkMode by remember { mutableStateOf(themeSettings.isDarkMode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // Field for selecting the background color
        Text(text = "Background Color", style = MaterialTheme.typography.titleMedium, color = Color.White)
        ColorPicker(selectedColor = backgroundColor, onColorSelected = { backgroundColor = it })

        Spacer(modifier = Modifier.height(16.dp))

        // Field for selecting the text color
        Text(text = "Text Color", style = MaterialTheme.typography.titleMedium, color = Color.White)
        ColorPicker(selectedColor = textColor, onColorSelected = { textColor = it })

        Spacer(modifier = Modifier.height(16.dp))

        // Switch for dark theme
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Text(text = "Dark Mode", style = MaterialTheme.typography.titleMedium)
//            Switch(
//                checked = isDarkMode,
//                onCheckedChange = { isDarkMode = it }
//            )
//        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                val newThemeSettings = ThemeSettings(backgroundColor, textColor)
                themeViewModel.saveThemeSettings(uuid, newThemeSettings)
                onBackClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Save")
        }
    }
}

@Composable
fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#FFFFFF", "#000000", "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF"
    )

    LazyRow {
        items(colors) { color ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
                    .background(Color(android.graphics.Color.parseColor(color)))
                    .border(
                        width = if (color == selectedColor) 2.dp else 1.dp,
                        color = if (color == selectedColor) Color.Black else Color.Gray,
                        shape = RectangleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}