package com.example.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.example.domain.model.CalculationHistory
import com.example.domain.model.ThemeSettings
import com.example.domain.repository.DeviceRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val sharedPreferences: SharedPreferences
): DeviceRepository {
    override fun getOrCreateUuid(): String {
        val savedUuid = sharedPreferences.getString("device_uuid", null)
        return if (savedUuid == null) {
            val newUuid = UUID.randomUUID().toString()
            sharedPreferences.edit().putString("device_uuid", newUuid).apply()
            saveUuidToFirestore(newUuid)
            newUuid
        } else {
            savedUuid
        }
    }

    override fun saveUuidToFirestore(uuid: String) {
        val deviceRef = firestore.collection("devices").document(uuid)
        deviceRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && !document.exists()) {
                    deviceRef.set(mapOf("uuid" to uuid, "created_at" to FieldValue.serverTimestamp()))
                }
            }
        }
    }

    override fun saveCalculationHistory(uuid: String, history: CalculationHistory) {
        firestore.collection("devices").document(uuid)
            .collection("history")
            .add(history)
            .addOnSuccessListener {
                Log.d("DeviceRepository", "Calculation history saved successfully")
            }
            .addOnFailureListener {
                e -> Log.e("DeviceRepository", "Error saving calculation history", e)
            }
    }

    override fun getCalculationHistory(
        uuid: String,
        onSuccess: (List<CalculationHistory>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("devices")
            .document(uuid)
            .collection("history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val historyList = result.toObjects(CalculationHistory::class.java)
                onSuccess(historyList)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    override fun saveThemeSettings(
        uuid: String,
        themeSettings: ThemeSettings,
        onComplete: (Boolean) -> Unit
    ) {
        firestore.collection("devices")
            .document(uuid)
            .collection("settings")
            .document("theme")
            .set(themeSettings)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("DeviceRepository", "Error saving theme settings", e)
                onComplete(false)
            }
    }

    override fun getThemeSettings(
        uuid: String,
        onSuccess: (ThemeSettings) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("devices")
            .document(uuid)
            .collection("settings")
            .document("theme")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val themeSettings = document.toObject(ThemeSettings::class.java)
                    if (themeSettings != null) {
                        onSuccess(themeSettings)
                    } else {
                        onFailure(Exception("Theme settings not found"))
                    }
                } else {
                    // Default theme
                    onSuccess(ThemeSettings())
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}