package com.example.calculator

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SetupPassKeyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Схемы для ключей
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Схемы для значений
        )

        setContent {
            SetupPassKeyScreen(sharedPreferences)
        }
    }

    @Composable
    fun SetupPassKeyScreen(sharedPreferences: SharedPreferences) {
        var passKey by remember { mutableStateOf("") }
        var confirmPassKey by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = passKey,
                onValueChange = { passKey = it },
                label = { Text("Enter Pass Key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = confirmPassKey,
                onValueChange = { confirmPassKey = it },
                label = { Text("Confirm Pass Key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                when {
                    passKey.isEmpty() -> errorMessage = "Pass Key can't be empty"
                    passKey != confirmPassKey -> errorMessage = "Pass Key does not match"
                    else -> savePassKey(passKey, sharedPreferences)
                }
            }) {
                Text("Set")
            }
            errorMessage?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }

    private fun savePassKey(passKey: String, sharedPreferences: SharedPreferences) {
        val credentialManager = CredentialManager.create(this) // Системный менеджер учетных данных
        CoroutineScope(Dispatchers.IO).launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                val request = CreatePasswordRequest("calculator_user", passKey)
                credentialManager.createCredential(this@SetupPassKeyActivity, request)
                sharedPreferences.edit().putString("pass_key", passKey).apply()
                runOnUiThread {
                    Toast.makeText(this@SetupPassKeyActivity, "Pass Key set", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SetupPassKeyActivity, LoginActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@SetupPassKeyActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}