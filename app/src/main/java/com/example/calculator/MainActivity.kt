package com.example.calculator

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.presentation.viewmodel.CalculatorViewModel
import com.example.calculator.ui.theme.CalculatorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorApp(viewModel)
//            CalculatorTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
        }
    }
}

@Composable
fun CalculatorApp(viewModel: CalculatorViewModel){
    val input by viewModel.input.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
//    val showAdvancedButtons by viewModel.showAdvancedButtons.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Enter
        Text(
            text = input,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        // Result
        Text(
            text = result,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Calculator buttons
        CalculatorButtons(onButtonClick = viewModel::onButtonClick)
    }
}

@Composable
fun CalculatorButtons(onButtonClick: (String) -> Unit) {

//    val buttonFontSize = if (showAdvancedButtons) 18.sp else 24.sp
//    val buttonSize = if (showAdvancedButtons) 64.dp else 80.dp

    val basicButtons = listOf(
        listOf("(", ")", "C", "/"),
        listOf("7", "8", "9", "*"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=", "%")
    )

    val advancedButtons = listOf(
        listOf("√", "x²", "sin", "cos"),
        listOf("tan", "log", "ln", "π"),
        listOf("e", "^", "mod", "%")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        basicButtons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { buttonValue ->
                    Button(
                        onClick = { onButtonClick(buttonValue) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    ) {
                        Text(
                            text = buttonValue,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LandscapeLayout()

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CalculatorTheme {
        Greeting("Android")
    }
}