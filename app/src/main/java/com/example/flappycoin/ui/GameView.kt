package com.example.flappycoin

import android.content.Context
import android.graphics.*
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), Runnable {
    companion object { private const val TAG = "GameView" }

    // ----- Thread & timing -----
    private var playing = false
    private var gameThread: Thread? = null
    private val targetFrameMs = 17L // ~60 FPS
    private var lastFrameTime = System.currentTimeMillis()

    // ----- Screen -----
    private val screenW = context.resources.displayMetrics.widthPixels
    private val screenH = context.resources.displayMetrics.heightPixels

    // ----- Paints -----
    private val paint = Paint()
    private val hudPaint = Paint().apply {
        color = Color.WHITE; textSize = 48f; isFakeBoldText = true
        setShadowLayer(4f, 0f, 0f, Color.BLACK)
    }
    private val hudRectPaint = Paint().apply { color = Color.argb(150, 0, 0, 0) }
    private val coinPaint = Paint().apply { textSize = 80f; color = Color.YELLOW; textAlign = Paint.Align.CENTER }
    private var coinYOffset = 0f
    private val gameOverPaint = Paint().apply {
        color = Color.RED; textSize = 96f; isFakeBoldText = true; textAlign = Paint.Align.CENTER
        setShadowLayer(6f, 0f, 0f, Color.BLACK)
    }

    // ----- Bitmaps (preload & scale once) -----
    private val bgBmp: Bitmap
    private val baseBmp: Bitmap
    private val pipeBmp: Bitmap
    private val pipeTopBmp: Bitmap
    private val birdFrames = mutableListOf<Bitmap>()

    // ----- Scrolling state for bg/base -----
    private var bgX1 = 0f
    private var bgX2 = 0f
    private var baseX1 = 0f
    private var baseX2 = 0f
    private val bgSpeed = 7f

    // ----- Bird -----
    private var birdX = 0f
    private var birdY = 0f
    private var birdW = 0
    private var birdH = 0
    private var birdVel = 0f
    private val gravity = 2.2f
    private val jumpImpulse = -36f
    private var birdAnimIndex = 0
    private var birdAnimTimer = 0L
    private val birdAnimFrameMs = 100L

    // ----- Pipes -----
    data class PipePair(var x: Float, var centerY: Float, var passed: Boolean = false)
    private val pipes = mutableListOf<PipePair>()
    private var pipeGap = (screenH * 0.34f)            // vertical gap size (will be clamped later)
    private var pipeHorizontalSpacing = (screenW * 0.6f) // will be re-evaluated after pipeBmp loaded
    private val visiblePairsInitial = 4
    private var pipeW: Float = 0f
    private var pipeH: Float = 0f

    // ----- Coins -----
    data class Coin(var x: Float, var y: Float)
    private val coins = mutableListOf<Coin>()
    private var coinsCollected = 0

    // ----- Score & stats -----
    private var score = 0
    private var distance = 0f
    private var elapsedMs = 0L

    // ----- Game state -----
    private var isGameOver = false

    // ----- Audio -----
    private val soundPool: SoundPool
    private val sWing: Int
    private val sPoint: Int
    private val sHit: Int
    private val sDie: Int

    // ----- Initialization -----
    init {
        // Preload and scale background to screen
        val rawBg = BitmapFactory.decodeResource(resources, R.drawable.background)
        bgBmp = Bitmap.createScaledBitmap(rawBg, screenW, screenH, true)
        rawBg.recycleQuietly()

        // Preload & scale base (fit width)
        val rawBase = BitmapFactory.decodeResource(resources, R.drawable.base)
        val baseScaled = Bitmap.createScaledBitmap(
            rawBase,
            screenW,
            (rawBase.height * (screenW / rawBase.width.toFloat())).toInt(),
            true
        )
        baseBmp = baseScaled
        rawBase.recycleQuietly()

        // Initialize bg/base positions (two copies each)
        bgX1 = 0f; bgX2 = screenW.toFloat()
        baseX1 = 0f; baseX2 = screenW.toFloat()

        // Load pipe and scale once
        val rawPipe = BitmapFactory.decodeResource(resources, R.drawable.pipe_green)
        val desiredPipeW = (screenW * 0.14f).toInt()
        val desiredPipeH = (screenH * 0.62f).toInt()
        pipeBmp = Bitmap.createScaledBitmap(rawPipe, desiredPipeW, desiredPipeH, true)
        pipeTopBmp = createVerticalFlip(pipeBmp)
        rawPipe.recycleQuietly()
        pipeW = pipeBmp.width.toFloat()
        pipeH = pipeBmp.height.toFloat()

        // choose spacing safe relative to pipeW and screenW
        pipeHorizontalSpacing = max(screenW * 0.6f, pipeW * 2.5f)

        // Load bird frames & scale
        val b1 = BitmapFactory.decodeResource(resources, R.drawable.redbird_upflap)
        val b2 = BitmapFactory.decodeResource(resources, R.drawable.redbird_midflap)
        val b3 = BitmapFactory.decodeResource(resources, R.drawable.redbird_downflap)
        birdW = (screenW * 0.10f).toInt()
        birdH = (b1.height * (birdW / b1.width.toFloat())).toInt()
        birdFrames.add(Bitmap.createScaledBitmap(b1, birdW, birdH, true))
        birdFrames.add(Bitmap.createScaledBitmap(b2, birdW, birdH, true))
        birdFrames.add(Bitmap.createScaledBitmap(b3, birdW, birdH, true))
        b1.recycleQuietly(); b2.recycleQuietly(); b3.recycleQuietly()

        // Start bird position
        birdX = (screenW * 0.28f)
        birdY = (screenH / 2f) - (birdH / 2f)

        // Enforce reasonable pipe gap bounds for small screens
        pipeGap = max((screenH * 0.25f), pipeGap)
        pipeGap = min(pipeGap, screenH * 0.45f)

        // Coin baseline offset for centered emoji
        val fm = coinPaint.fontMetrics
        coinYOffset = (fm.ascent + fm.descent) / 2f

        // Initialize pipes and coins aligned properly
        resetAllPipesAndCoins()

        // Audio init
        val audioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(audioAttr).build()
        sWing = soundPool.load(context, R.raw.wing, 1)
        sPoint = soundPool.load(context, R.raw.point, 1)
        sHit = soundPool.load(context, R.raw.hit, 1)
        sDie = soundPool.load(context, R.raw.die, 1)

        Log.d(TAG, "GameView init - screen ${screenW}x${screenH}, pipeW=${pipeW.toInt()}, pipeGap=${pipeGap.toInt()}, spacing=${pipeHorizontalSpacing.toInt()}")
    }

    // ----- Helpers -----
    private fun Bitmap.recycleQuietly() {
        try { if (!isRecycled) recycle() } catch (_: Throwable) {}
    }
    private fun createVerticalFlip(src: Bitmap): Bitmap {
        val m = Matrix(); m.preScale(1f, -1f)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
    }

    // Generate strictly-aligned initial pipes + coins so nothing looks scrambled
    private fun resetAllPipesAndCoins() {
        pipes.clear()
        coins.clear()
        var startX = screenW.toFloat() + (screenW * 0.25f) // place first pair just off-screen
        for (i in 0 until visiblePairsInitial) {
            val center = safeRandomGapCenter()
            pipes.add(PipePair(startX, center, passed = false))
            coins.add(Coin(startX + pipeW / 2f, center))
            startX += pipeHorizontalSpacing
        }
    }

    private fun safeRandomGapCenter(): Float {
        val margin = (screenH * 0.12f).toInt()
        val minCenter = (margin + pipeGap / 2f).toInt()
        val maxCenter = (screenH - margin - pipeGap / 2f).toInt()
        return if (maxCenter <= minCenter) {
            minCenter.toFloat()
        } else {
            Random.nextInt(minCenter, maxCenter).toFloat()
        }
    }

    // ----- Main loop -----
    override fun run() {
        try {
            while (playing) {
                val now = System.currentTimeMillis()
                val delta = now - lastFrameTime
                lastFrameTime = now
                if (delta <= 0) { Thread.sleep(1); continue }
                update(delta)
                draw()
                control()
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Game loop exception", e)
        }
    }

    private fun update(deltaMs: Long) {
        // cap frameScale to avoid huge jumps if the app was paused/blocked
        val frameScale = min(deltaMs / 16f, 2f)

        if (isGameOver) {
            elapsedMs += deltaMs
            return
        }
        elapsedMs += deltaMs

        // scroll bg + base (frame-time scaled)
        bgX1 -= bgSpeed * frameScale; bgX2 -= bgSpeed * frameScale
        baseX1 -= bgSpeed * frameScale; baseX2 -= bgSpeed * frameScale

        if (bgX1 <= -screenW) bgX1 = bgX2 + screenW
        if (bgX2 <= -screenW) bgX2 = bgX1 + screenW
        if (baseX1 <= -screenW) baseX1 = baseX2 + screenW
        if (baseX2 <= -screenW) baseX2 = baseX1 + screenW

        // bird physics (time-scaled)
        birdVel += gravity
        birdY += birdVel * frameScale
        if (birdY < 0f) { birdY = 0f; birdVel = 0f }
        val baseTop = (screenH - baseBmp.height).toFloat()
        if (birdY + birdH > baseTop) {
            birdY = baseTop - birdH
            triggerGameOver()
            return
        }

        // bird animation
        birdAnimTimer += deltaMs
        if (birdAnimTimer >= birdAnimFrameMs) {
            birdAnimIndex = (birdAnimIndex + 1) % birdFrames.size
            birdAnimTimer = 0L
        }

        // move pipes, recycle when off-screen
        for (p in pipes) p.x -= (bgSpeed * frameScale)
        if (pipes.isNotEmpty() && pipes.first().x + pipeW < 0f) {
            // remove first elements safely
            try {
                pipes.removeAt(0)
                if (coins.isNotEmpty()) coins.removeAt(0)
            } catch (e: Exception) {
                Log.w(TAG, "safe remove error: ${e.message}")
            }
            // add new pair aligned exactly after last pair
            val lastX = pipes.lastOrNull()?.x ?: screenW.toFloat()
            val newX = lastX + pipeHorizontalSpacing
            val center = safeRandomGapCenter()
            pipes.add(PipePair(newX, center, passed = false))
            coins.add(Coin(newX + pipeW / 2f, center))
        }

        // collision detection with pipes (slightly smaller bird hitbox for fairness)
        val birdRect = RectF(birdX + 8f, birdY + 8f, birdX + birdW - 8f, birdY + birdH - 8f)
        for (p in pipes) {
            val topRect = RectF(p.x, 0f, p.x + pipeW, p.centerY - (pipeGap / 2f))
            val bottomRect = RectF(p.x, p.centerY + (pipeGap / 2f), p.x + pipeW, screenH.toFloat())
            if (RectF.intersects(birdRect, topRect) || RectF.intersects(birdRect, bottomRect)) {
                triggerGameOver()
                break
            }
        }

        // collect coins
        val it = coins.iterator()
        while (it.hasNext()) {
            val c = it.next()
            val coinRect = RectF(c.x - 40f, c.y - 40f, c.x + 40f, c.y + 40f)
            if (RectF.intersects(birdRect, coinRect)) {
                coinsCollected++; score++
                try { soundPool.play(sPoint, 1f, 1f, 0, 0, 1f) } catch (_: Throwable) {}
                it.remove()
            }
        }

        // increment score when passing pairs (use center of pipe)
        for (p in pipes) {
            if (!p.passed && p.x + pipeW / 2f < birdX) {
                p.passed = true
                score++
                try { soundPool.play(sPoint, 0.6f, 0.6f, 0, 0, 1f) } catch (_: Throwable) {}
            }
        }

        // update distance (time-scaled but clamped)
        distance += (bgSpeed * frameScale)
    }

    // ----- Drawing -----
    private fun draw() {
        if (!holder.surface.isValid) return
        val canvas = holder.lockCanvas() ?: return
        try {
            // background two copies
            canvas.drawBitmap(bgBmp, bgX1.toInt().toFloat(), 0f, paint)
            canvas.drawBitmap(bgBmp, bgX2.toInt().toFloat(), 0f, paint)

            // pipes: top (flipped) and bottom
            for (p in pipes) {
                val topY = p.centerY - (pipeGap / 2f) - pipeH
                val bottomY = p.centerY + (pipeGap / 2f)
                canvas.drawBitmap(pipeTopBmp, p.x.toInt().toFloat(), topY.toInt().toFloat(), paint)
                canvas.drawBitmap(pipeBmp, p.x.toInt().toFloat(), bottomY.toInt().toFloat(), paint)
            }

            // base two copies
            val baseY = (screenH - baseBmp.height).toFloat()
            canvas.drawBitmap(baseBmp, baseX1.toInt().toFloat(), baseY, paint)
            canvas.drawBitmap(baseBmp, baseX2.toInt().toFloat(), baseY, paint)

            // coins emoji (centered using baseline offset)
            for (c in coins) canvas.drawText("🪙", c.x, c.y - coinYOffset, coinPaint)

            // bird (scaled frame)
            val birdBmp = birdFrames[birdAnimIndex]
            canvas.drawBitmap(birdBmp, birdX.toInt().toFloat(), birdY.toInt().toFloat(), paint)

            // HUD right-side boxes
            val rectW = min(320f, screenW * 0.28f)
            val rectH = 110f
            val pad = 22f
            val rRight = screenW - pad
            val rLeft = rRight - rectW

            // distance
            canvas.drawRoundRect(RectF(rLeft, 40f, rRight, 40f + rectH), 12f, 12f, hudRectPaint)
            canvas.drawText("Distance: ${distance.toInt()} mm", rLeft + 14f, 40f + rectH / 2f + 14f, hudPaint)

            // time -> kilo-seconds (ms -> ks)
            canvas.drawRoundRect(RectF(rLeft, 40f + rectH + 16f, rRight, 40f + 2 * rectH + 16f), 12f, 12f, hudRectPaint)
            val kiloSeconds = elapsedMs.toFloat() / 1_000_000f
            canvas.drawText("Time: %.2f ks".format(kiloSeconds), rLeft + 14f, 40f + rectH + 16f + rectH / 2f + 14f, hudPaint)

            // coins & score top-left
            canvas.drawText("Coins: $coinsCollected", 40f, 80f, hudPaint)
            canvas.drawText("Score: $score", 40f, 140f, hudPaint)

            // Game Over overlay
            if (isGameOver) {
                val overlay = Paint().apply { color = Color.argb(200, 0, 0, 0) }
                canvas.drawRect(0f, 0f, screenW.toFloat(), screenH.toFloat(), overlay)
                val cx = screenW / 2f
                val cy = screenH / 2f
                canvas.drawText("GAME OVER", cx, cy - 120f, gameOverPaint)
                canvas.drawText("Distance: ${distance.toInt()} mm", cx, cy - 40f, hudPaint)
                canvas.drawText("Time: %.2f ks".format(kiloSeconds), cx, cy + 20f, hudPaint)
                canvas.drawText("Score: $score", cx, cy + 80f, hudPaint)
                canvas.drawText("Coins: $coinsCollected", cx, cy + 140f, hudPaint)
                val small = Paint().apply { color = Color.LTGRAY; textSize = 36f; textAlign = Paint.Align.CENTER }
                canvas.drawText("Tap to restart", cx, cy + 200f, small)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "draw error", e)
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun control() {
        try { Thread.sleep(targetFrameMs) } catch (_: InterruptedException) {}
    }

    // ----- Input -----
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (isGameOver) {
                // manual restart only (no racing with delayed handler)
                resetGame()
                isGameOver = false
                resume()
            } else {
                birdVel = jumpImpulse
                try { soundPool.play(sWing, 1f, 1f, 0, 0, 1f) } catch (_: Throwable) {}
            }
        }
        return true
    }

    // ----- Game over / reset -----
    private fun triggerGameOver() {
        if (!isGameOver) {
            isGameOver = true; playing = false
            try { soundPool.play(sHit, 1f, 1f, 0, 0, 1f); soundPool.play(sDie, 1f, 1f, 0, 0, 1f) } catch (_: Throwable) {}
        }
    }

    private fun resetGame() {
        try {
            birdY = (screenH / 2f) - (birdH / 2f)
            birdVel = 0f
            score = 0; coinsCollected = 0; distance = 0f; elapsedMs = 0L
            lastFrameTime = System.currentTimeMillis()
            resetAllPipesAndCoins()
        } catch (e: Throwable) {
            Log.e(TAG, "resetGame error", e)
        }
    }

    // ----- Lifecycle -----
    fun pause() {
        playing = false
        try { gameThread?.join() } catch (_: InterruptedException) {}
        try { soundPool.autoPause() } catch (_: Throwable) {}
    }

    fun resume() {
        if (playing) return
        playing = true
        try { soundPool.autoResume() } catch (_: Throwable) {}
        gameThread = Thread(this)
        lastFrameTime = System.currentTimeMillis()
        gameThread?.start()
    }

    // free bitmaps if needed
    fun releaseResources() {
        try {
            for (b in birdFrames) if (!b.isRecycled) b.recycle()
            if (!bgBmp.isRecycled) bgBmp.recycle()
            if (!baseBmp.isRecycled) baseBmp.recycle()
            if (!pipeBmp.isRecycled) pipeBmp.recycle()
            if (!pipeTopBmp.isRecycled) pipeTopBmp.recycle()
        } catch (e: Throwable) { Log.w(TAG, "release error: ${e.message}") }
        try { soundPool.release() } catch (_: Throwable) {}
    }
}
