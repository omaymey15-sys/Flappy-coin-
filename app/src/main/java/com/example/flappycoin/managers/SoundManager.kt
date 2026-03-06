package com.example.flappycoin.managers

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.flappycoin.R

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

        sWing = soundPool.load(context, R.raw.wing, 1)
        sPoint = soundPool.load(context, R.raw.point, 1)
        sHit = soundPool.load(context, R.raw.hit, 1)
        sDie = soundPool.load(context, R.raw.die, 1)
    }

    fun playWing() = play(sWing)
    fun playPoint() = play(sPoint)
    fun playHit() = play(sHit)
    fun playDie() = play(sDie)

    private fun play(soundId: Int) {
        if (GamePreferences.isAudioEnabled()) {
            soundPool.play(soundId, 0.7f, 0.7f, 0, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}