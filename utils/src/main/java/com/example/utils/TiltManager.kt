package com.example.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class TiltManager(context: Context): SensorEventListener {

    private val sensorManager: SensorManager = context
        .getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var tiltListener: ((x: Float, y: Float, z: Float) -> Unit) = { _, _, _, -> }

    fun startListening(listener: (x: Float, y: Float, z: Float) -> Unit) {
        tiltListener = listener
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]
            tiltListener(x, y, z)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}