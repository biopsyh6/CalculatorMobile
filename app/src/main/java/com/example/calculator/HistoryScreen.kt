package com.example.calculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CalculationHistory
import java.text.SimpleDateFormat

@Composable
fun HistoryScreen(
    history: List<CalculationHistory>,
    onBackClick: () -> Unit // previous screen
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Back Button
        Button(
            onClick = onBackClick,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Back")
        }

        // History List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(history) { entry ->
                HistoryItem(entry)
            }
        }
    }
}

@Composable
fun HistoryItem(history: CalculationHistory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Expression: ${history.expression}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Result: ${history.result}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Time: ${SimpleDateFormat("dd/MM/yyyy HH:mm").format(history.timestamp)}",
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }
    }
}