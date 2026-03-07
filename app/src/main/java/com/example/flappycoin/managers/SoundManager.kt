package com.example.flappycoin.managers

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.flappycoin.R

/**
 * Gestion centralisée des sons du jeu
 * Compatible API 21+
 */
object SoundManager {
    private lateinit var soundPool: SoundPool
    private var sWing = 0
    private var sPoint = 0
    private var sHit = 0
    private var sDie = 0

    fun init(context: Context) {
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attr)
            .build()

        try {
            sWing = soundPool.load(context, R.raw.wing, 1)
            sPoint = soundPool.load(context, R.raw.point, 1)
            sHit = soundPool.load(context, R.raw.hit, 1)
            sDie = soundPool.load(context, R.raw.die, 1)
        } catch (e: Exception) {
            // Fichiers sons manquants en dev
            e.printStackTrace()
        }
    }

    fun playWing() = play(sWing, 0.5f)
    fun playPoint() = play(sPoint, 0.9f)
    fun playHit() = play(sHit, 1.0f)
    fun playDie() = play(sDie, 1.0f)
    fun playTap() = play(sWing, 0.5f)

    private fun play(soundId: Int, volume: Float = 0.7f) {
        if (GamePreferences.isAudioEnabled() && soundId > 0) {
            soundPool.play(soundId, volume, volume, 0, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}