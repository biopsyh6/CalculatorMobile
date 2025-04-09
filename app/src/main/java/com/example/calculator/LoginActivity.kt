package com.example.calculator

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val passKey = sharedPreferences.getString("pass_key", null)
        if (passKey == null) {
            startActivity(Intent(this, SetupPassKeyActivity::class.java))
            finish()
            return
        }

        setContent {
            LoginScreen(sharedPreferences)
        }

//        autoLogin(sharedPreferences)
    }

    @Composable
    fun LoginScreen(sharedPreferences: SharedPreferences) {
        var passKeyInput by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = passKeyInput,
                onValueChange = { passKeyInput = it },
                label = { Text("Enter Pass Key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (validatePassKey(passKeyInput, sharedPreferences)) {
                    grantAccess(sharedPreferences)
                } else {
                    errorMessage = "Incorrect Pass Key"
                }
            }) {
                Text("Login")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = {
                setupBiometricAuthentication(sharedPreferences)
            }) {
                Text("Reset Pass Key")
            }
            errorMessage?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }

    private fun validatePassKey(enteredKey: String, sharedPreferences: SharedPreferences): Boolean {
        return enteredKey == sharedPreferences.getString("pass_key", null)
    }

    private fun grantAccess(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit().putBoolean("is_authenticated", true).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun autoLogin(sharedPreferences: SharedPreferences) {
        val credentialManager = CredentialManager.create(this)
        val request = GetCredentialRequest(listOf(GetPasswordOption()))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = credentialManager.getCredential(this@LoginActivity, request)
                val credential = result.credential
                if (credential is PasswordCredential) {
                    if (validatePassKey(credential.password, sharedPreferences)) {
                        runOnUiThread { grantAccess(sharedPreferences) }
                    }
                }
            } catch (e: Exception) {
                Log.e("AutoLogin", "AutoLogin Error: ${e.message}")
            }
        }
    }

    private fun setupBiometricAuthentication(sharedPreferences: SharedPreferences) {
        // Объект, который выполняет задачи в главном потоке
        val executor = ContextCompat.getMainExecutor(this)
        // Объект биометрической аутентификации
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Toast.makeText(this@LoginActivity, "Biometrics error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                resetPassKey(sharedPreferences)
                startActivity(Intent(this@LoginActivity, SetupPassKeyActivity::class.java))
                finish()
            }

            override fun onAuthenticationFailed() {
                Toast.makeText(this@LoginActivity, "Biometrics failed", Toast.LENGTH_SHORT).show()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Identity verification")
            .setSubtitle("Use Biometrics to Reset Pass Key")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun resetPassKey(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit().remove("pass_key").apply()
    }
}