package com.example.calculator

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.presentation.viewmodel.CalculatorViewModel
import com.example.calculator.ui.theme.CalculatorTheme
import com.example.utils.SoundManager
import com.example.utils.TiltManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var tiltManager: TiltManager

    private val viewModel: CalculatorViewModel by viewModels()
    private lateinit var vibrator: Vibrator

    @Inject
    lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager.loadSound(this, R.raw.roblox_death_sound_effect)
        tiltManager = TiltManager(this)
        installSplashScreen()
        enableEdgeToEdge()

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        setContent {
            CalculatorApp(
                viewModel = viewModel,
                vibrate = ::vibrate,
                playErrorSound = { soundManager.playSound(R.raw.roblox_death_sound_effect) },
                tiltManager = tiltManager
            )
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

    override fun onResume() {
        super.onResume()
        // Listen accelerometer data
        tiltManager.startListening { x, y, z ->
            updateTiltEffect(x, y, z)
        }
    }

    override fun onPause() {
        super.onPause()
        tiltManager.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }

    private fun vibrate() {
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50,
                    VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(50)
            }
        }
    }

    private fun updateTiltEffect(x: Float, y: Float, z: Float) {

    }
}

@Composable
fun CalculatorApp(viewModel: CalculatorViewModel,
                  vibrate: () -> Unit,
                  playErrorSound: () -> Unit,
                  tiltManager: TiltManager){
    val input by viewModel.input.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val useDegrees by  viewModel.useDegrees.collectAsStateWithLifecycle()

    var color1 by remember { mutableStateOf(Color.White) }
    var color2 by remember { mutableStateOf(Color.Gray) }

    val animatedColor1 by animateColorAsState(targetValue = color1,
        animationSpec = tween(1000, easing = LinearEasing))
    val animatedColor2 by animateColorAsState(targetValue = color2,
        animationSpec = tween(1000, easing = LinearEasing))

//    var backgroundColor by remember { mutableStateOf(Color.White) }
    var gradientColors by remember { mutableStateOf(listOf(Color.White, Color.Gray)) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE


    LaunchedEffect(Unit) {
        tiltManager.startListening { x, y, z ->
            val red1 = ((x + 12) / 24).coerceIn(0f, 1f)
            val green1 = ((y + 12) / 24).coerceIn(0f, 1f)
            val blue1 = ((z + 12) / 24).coerceIn(0f, 1f)

            val red2 = ((y + 12) / 24).coerceIn(0f, 1f)
            val green2 = ((z + 12) / 24).coerceIn(0f, 1f)
            val blue2 = ((x + 12) / 24).coerceIn(0f, 1f)


            color1 = Color(red1, green1, blue1, 1f)
            color2 = Color(red2, green2, blue2, 1f)
//            gradientColors = listOf(
//                Color(red1, green1, blue1, 1f),
//                Color(red2, green2, blue2, 1f)
//            )
//            backgroundColor = Color(red, green, blue, 1f)
        }
    }

    if (result.startsWith("Error")) {
        LaunchedEffect(result) {
            playErrorSound()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(animatedColor1, animatedColor2),
                    center = Offset(500f, 500f),
                    radius = 700f
                )
            )
    ) {
        if (isLandscape) {
            LandscapeLayout(
                input = input,
                result = result,
                onButtonClick = { viewModel.onButtonClick(it); vibrate() },
                useDegrees = useDegrees,
                onToggleDegrees = { viewModel.toggleUseDegrees() }
            )
        } else {
            PortraitLayout(
                input = input,
                result = result,
                onButtonClick = { viewModel.onButtonClick(it); vibrate() }
            )
        }
    }

}

@Composable
fun CalculatorButtons(onButtonClick: (String) -> Unit) {

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val buttonFontSize = if (isLandscape) 18.sp else 24.sp
    val buttonAspectRatio = if (isLandscape) 1.5f else 1f
    val buttonSize = if (isLandscape) 64.dp else 80.dp


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
                    verticalArrangement = Arrangement.spacedBy(2.dp)
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(12.dp))
                .border(2.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = input,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Field for Result
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color(0xFFDFFFD6), shape = RoundedCornerShape(12.dp))
                .border(2.dp, Color.Green, shape = RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = result,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                color = Color(0xFF388E3C),
                modifier = Modifier.fillMaxWidth()
            )
        }

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
            .padding(2.dp),
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(12.dp))
                        .border(2.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = input,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(Color(0xFFDFFFD6), shape = RoundedCornerShape(12.dp))
                        .border(2.dp, Color.Green, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = result,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = Color(0xFF388E3C),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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