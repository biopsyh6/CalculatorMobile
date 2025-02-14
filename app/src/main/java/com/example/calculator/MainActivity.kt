package com.example.calculator

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.presentation.viewmodel.CalculatorViewModel
import com.example.calculator.ui.theme.CalculatorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
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
    val useDegrees by  viewModel.useDegrees.collectAsStateWithLifecycle()
//    val showAdvancedButtons by viewModel.showAdvancedButtons.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeLayout(input, result, viewModel::onButtonClick, useDegrees,
            onToggleDegrees = { viewModel.toggleUseDegrees() })
    } else {
        PortraitLayout(input, result, viewModel::onButtonClick)
    }

//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center
//    ) {
//        // Enter
//        Text(
//            text = input,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp),
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold
//        )
//        // Result
//        Text(
//            text = result,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp),
//            fontSize = 32.sp,
//            fontWeight = FontWeight.Bold
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        // Calculator buttons
//        CalculatorButtons(onButtonClick = viewModel::onButtonClick)
//    }
}

@Composable
fun CalculatorButtons(onButtonClick: (String) -> Unit) {

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val buttonFontSize = if (isLandscape) 18.sp else 24.sp
    val buttonAspectRatio = if (isLandscape) 1.5f else 1f
    val buttonSize = if (isLandscape) 64.dp else 80.dp

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
        listOf("(", ")", "%", "C", "7", "8", "9", "/"),
        listOf("x²", "^", "√", "!", "4", "5", "6", "*"),
        listOf("sin", "cos", "tan", "1/x", "1", "2", "3", "-"),
        listOf("lg", "ln", "π", "e", "0", ".", "=", "+")
        )

//    val advancedButtons = listOf(
//        listOf("", "", "", ""),
//        listOf("", "", "", ""),
//        listOf("x²", "^", "√", "!"),
//        listOf("sin", "cos", "tan", "1/x"),
//        listOf("log", "ln", "π", "e"),
//
//    )

    val maxWidth = if (isLandscape) 600.dp else 400.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
    ) {
        if (isLandscape) {
            // basicButtons and advancedButtons
            Row(
                modifier = Modifier
                    .width(maxWidth)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // advancedButtons
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    advancedButtons.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { buttonValue ->
                                if (buttonValue.isNotEmpty()) {
                                    Button(
                                        onClick = { onButtonClick(buttonValue) },
                                        modifier = Modifier
                                            .aspectRatio(buttonAspectRatio)
                                            .size(buttonSize)
                                            .weight(1f, fill = false),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = buttonValue,
                                            fontSize = buttonFontSize,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(buttonSize))
                                }
                            }
                        }
                    }
                }
                // basicButtons
//                Column(
//                    modifier = Modifier.weight(1f),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    basicButtons.forEach { row ->
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            row.forEach { buttonValue ->
//                                Button(
//                                    onClick = { onButtonClick(buttonValue) },
//                                    modifier = Modifier
//                                        .aspectRatio(buttonAspectRatio)
//                                        .size(buttonSize)
//                                        .weight(1f, fill = false),
//                                    contentPadding = PaddingValues(0.dp)
//                                ) {
//                                    Text(
//                                        text = buttonValue,
//                                        fontSize = buttonFontSize,
//                                        fontWeight = FontWeight.Bold
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
            }
        } else {
          // basicButtons
            Column(
                modifier = Modifier
                    .width(maxWidth)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
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
                                    .size(buttonSize)
                                    .weight(1f, fill = false)
                                    .aspectRatio(buttonAspectRatio),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = buttonValue,
                                    fontSize = buttonFontSize,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

    }

}

@Composable
fun PortraitLayout(input: String, result: String, onButtonClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Field for Enter
        Text(
            text = input,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        // Field for Result
        Text(
            text = result,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        CalculatorButtons(onButtonClick = onButtonClick)
    }
}

@Composable
fun LandscapeLayout(input: String,
                    result: String,
                    onButtonClick: (String) -> Unit,
                    useDegrees: Boolean,
                    onToggleDegrees: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Field for Enter and Result
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = input,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = result,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // Calculator Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = "Radians")
            Switch(
                checked = useDegrees,
                onCheckedChange = onToggleDegrees,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(text = "Degrees")
            CalculatorButtons(onButtonClick = onButtonClick)
        }

//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(0.7f)
//        ) {
//
//        }
    }
}

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