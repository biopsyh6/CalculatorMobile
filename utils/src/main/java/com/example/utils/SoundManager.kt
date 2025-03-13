package com.example.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<Int, Int>() // Key - Resourse Id, Value - Sound Id

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    fun loadSound(context: Context, @RawRes soundResId: Int) {
        val soundId = soundPool.load(context, soundResId, 1)
        soundMap[soundResId] = soundId
    }

    fun playSound(@RawRes soundResId: Int) {
        val soundId = soundMap[soundResId] ?: return
        soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }
}