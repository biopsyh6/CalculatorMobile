package com.example.calculator

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.calculator.presentation.viewmodel.CalculatorViewModel
import com.example.calculator.presentation.viewmodel.DeviceViewModel
import com.example.calculator.presentation.viewmodel.ThemeViewModel
import com.example.calculator.ui.theme.CalculatorTheme
import com.example.utils.SoundManager
import com.example.utils.TiltManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var tiltManager: TiltManager

    private val viewModel: CalculatorViewModel by viewModels()
    private val deviceViewModel: DeviceViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private lateinit var vibrator: Vibrator

    @Inject
    lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC), // Ключ для шифрования
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Схема шифрования ключей
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Схема шифрования значений
        )

        val isAuthenticated = sharedPreferences.getBoolean("is_authenticated", false)
        if (!isAuthenticated) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = "FCM Token: $token"
            Log.d(TAG, msg)
        })

        val uuid = deviceViewModel.getOrCreateUuid()
        Log.d("MainActivity", "Device UUID: $uuid")

        viewModel.loadCalculationHistory(uuid)

        soundManager.loadSound(this, R.raw.roblox_death_sound_effect)
        tiltManager = TiltManager(this)
        installSplashScreen()
        enableEdgeToEdge()

        // Navigation Bar Settings


        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        setContent {
            AppNavigation(
                viewModel = viewModel,
                themeViewModel = themeViewModel,
                uuid = uuid,
                vibrate = ::vibrate,
                playErrorSound = { soundManager.playSound(R.raw.roblox_death_sound_effect) },
                tiltManager = tiltManager
            )

//            CalculatorApp(
//                viewModel = viewModel,
//                uuid = uuid,
//                vibrate = ::vibrate,
//                playErrorSound = { soundManager.playSound(R.raw.roblox_death_sound_effect) },
//                tiltManager = tiltManager
//            )

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
        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit().putBoolean("is_authenticated", false).apply()
    }

    override fun onStop() {
        super.onStop()
        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit().putBoolean("is_authenticated", false).apply()
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
fun AppNavigation(
    viewModel: CalculatorViewModel,
    themeViewModel: ThemeViewModel,
    uuid: String,
    vibrate: () -> Unit,
    playErrorSound: () -> Unit,
    tiltManager: TiltManager
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "calculator"
    ) {
        composable("calculator") {
            CalculatorApp(
                viewModel = viewModel,
                themeViewModel = themeViewModel,
                uuid = uuid,
                vibrate = vibrate,
                playErrorSound = playErrorSound,
                tiltManager = tiltManager,
                navigateToHistory = {
                    navController.navigate("history")
                },
                navigateToSettings = {
                    navController.navigate("themeSettings")
                }
            )
        }

        composable("history") {
            HistoryScreen(
                history = viewModel.history.collectAsStateWithLifecycle().value,
                themeViewModel = themeViewModel,
                onBackClick = {
                    navController.popBackStack() // Previous screen
                }
            )
        }

        composable("themeSettings") {
            ThemeSettingsScreen(
                themeViewModel = themeViewModel,
                uuid = uuid,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun CalculatorApp(
    viewModel: CalculatorViewModel,
    themeViewModel: ThemeViewModel,
    uuid: String,
    vibrate: () -> Unit,
    playErrorSound: () -> Unit,
    tiltManager: TiltManager,
    navigateToHistory: () -> Unit,
    navigateToSettings: () -> Unit
){
    val themeSettings by themeViewModel.themeSettings.collectAsStateWithLifecycle()

    // Accept theme settings
    val backgroundColor = Color(android.graphics.Color.parseColor(themeSettings.backgroundColor))
    val textColor = Color(android.graphics.Color.parseColor(themeSettings.textColor))

    val input by viewModel.input.collectAsStateWithLifecycle() // Subscribe on Flow and Convert Values into State
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
                backgroundColor
//                brush = Brush.radialGradient(
//                    colors = listOf(animatedColor1, animatedColor2),
//                    center = Offset(500f, 500f),
//                    radius = 700f
//                )
            )
    ) {

        Column {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                Button(
//                    onClick = navigateToHistory,
//                    modifier = Modifier.align(Alignment.TopEnd)
//                ) {
//                    Text(text = "History")
//                }
//            }

            if (isLandscape) {
                LandscapeLayout(
                    input = input,
                    result = result,
                    onButtonClick = { buttonValue, uuid ->
                        viewModel.onButtonClick(buttonValue, uuid)
                        vibrate()
                    },
                    useDegrees = useDegrees,
                    onToggleDegrees = { viewModel.toggleUseDegrees() },
                    uuid = uuid,
                    navigateToHistory = navigateToHistory,
                    navigateToSettings = navigateToSettings,
                    textColor = textColor
                )
            } else {
                PortraitLayout(
                    input = input,
                    result = result,
                    onButtonClick = { buttonValue, uuid ->
                        viewModel.onButtonClick(buttonValue, uuid)
                        vibrate()
                    },
                    uuid = uuid,
                    navigateToHistory = navigateToHistory,
                    navigateToSettings = navigateToSettings,
                    textColor = textColor
                )
            }
        }
    }

    // Load theme settings at startup
    LaunchedEffect(Unit) {
        themeViewModel.loadThemeSettings(uuid)
    }
}

@Composable
fun CalculatorButtons(onButtonClick: (String, String) -> Unit,
                      uuid: String,
                      textColor: Color) {

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
                                        onClick = { onButtonClick(buttonValue, uuid) },
                                        modifier = Modifier
                                            .aspectRatio(buttonAspectRatio)
                                            .size(buttonSize)
                                            .weight(1f, fill = false),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = buttonValue,
                                            fontSize = buttonFontSize,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
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
                                onClick = { onButtonClick(buttonValue, uuid) },
                                modifier = Modifier
                                    .size(buttonSize)
                                    .weight(1f, fill = false)
                                    .aspectRatio(buttonAspectRatio),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = buttonValue,
                                    fontSize = buttonFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
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
fun PortraitLayout(input: String,
                   result: String,
                   onButtonClick: (String, String) -> Unit,
                   uuid: String,
                   navigateToHistory: () -> Unit,
                   navigateToSettings: () -> Unit,
                   textColor: Color
                   ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(
            onClick = navigateToHistory,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(8.dp)
        ) {
            Text(text = "History", color = textColor)
        }

        Button(
            onClick = navigateToSettings,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(8.dp)
        ) {
            Text(text = "Settings", color = textColor)
        }

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
        CalculatorButtons(
            onButtonClick = onButtonClick,
            uuid = uuid,
            textColor = textColor
        )
    }
}

@Composable
fun LandscapeLayout(input: String,
                    result: String,
                    onButtonClick: (String, String) -> Unit,
                    useDegrees: Boolean,
                    onToggleDegrees: (Boolean) -> Unit,
                    uuid: String,
                    navigateToHistory: () -> Unit,
                    navigateToSettings: () -> Unit,
                    textColor: Color
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
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Radians", color = textColor)
                    Switch(
                        checked = useDegrees,
                        onCheckedChange = onToggleDegrees,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(text = "Degrees", color = textColor)
                }
                Row {
                    Button(
                        onClick = navigateToHistory,
                        modifier = Modifier
//                            .align(Alignment.End)
                            .padding(8.dp)
                    ) {
                        Text(text = "History", color = textColor)
                    }
                }
                Row {
                    Button(
                        onClick = navigateToSettings,
                        modifier = Modifier
//                            .align(Alignment.End)
                            .padding(8.dp)
                    ) {
                        Text(text = "Settings", color = textColor)
                    }
                }

            }

            CalculatorButtons(
                onButtonClick = onButtonClick,
                uuid = uuid,
                textColor = textColor
            )
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