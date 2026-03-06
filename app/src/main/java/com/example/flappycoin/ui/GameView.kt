package com.example.flappycoin.ui

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

class GameView(
    context: Context,
    private val onGameOver: (score: Int, coins: Int, distance: Int, time: Long) -> Unit
) : SurfaceView(context), Runnable {

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

    // ----- Bitmaps -----
    private val bgBmp: Bitmap
    private val baseBmp: Bitmap
    private val pipeBmp: Bitmap
    private val pipeTopBmp: Bitmap
    private val birdFrames = mutableListOf<Bitmap>()

    // ----- Background/base scrolling -----
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
    private var pipeGap = (screenH * 0.34f)
    private var pipeHorizontalSpacing = (screenW * 0.6f)
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
        // Background
        val rawBg = BitmapFactory.decodeResource(resources, com.example.flappycoin.R.drawable.background)
        bgBmp = Bitmap.createScaledBitmap(rawBg, screenW, screenH, true)
        rawBg.recycleQuietly()

        // Base
        val rawBase = BitmapFactory.decodeResource(resources, com.example.flappycoin.R.drawable.base)
        baseBmp = Bitmap.createScaledBitmap(rawBase, screenW, (rawBase.height * (screenW / rawBase.width.toFloat())).toInt(), true)
        rawBase.recycleQuietly()
        bgX1 = 0f; bgX2 = screenW.toFloat()
        baseX1 = 0f; baseX2 = screenW.toFloat()

        // Pipes
        val rawPipe = BitmapFactory.decodeResource(resources, com.example.flappycoin.R.drawable.pipe_green)
        pipeBmp = Bitmap.createScaledBitmap(rawPipe, (screenW * 0.14f).toInt(), (screenH * 0.62f).toInt(), true)
        pipeTopBmp = createVerticalFlip(pipeBmp)
        rawPipe.recycleQuietly()
        pipeW = pipeBmp.width.toFloat()
        pipeH = pipeBmp.height.toFloat()
        pipeHorizontalSpacing = max(screenW * 0.6f, pipeW * 2.5f)

        // Bird
        val b1 = BitmapFactory.decodeResource(resources, com.example.flappycoin.R.drawable.redbird_upflap)
        val b2 = BitmapFactory.decodeResource(resources, com.example.flappycoin.R.drawable.redbird_midflap)
        val b3 = BitmapFactory.decodeResource(resources, com.example.flappycoin.R.drawable.redbird_downflap)
        birdW = (screenW * 0.10f).toInt()
        birdH = (b1.height * (birdW / b1.width.toFloat())).toInt()
        birdFrames.add(Bitmap.createScaledBitmap(b1, birdW, birdH, true))
        birdFrames.add(Bitmap.createScaledBitmap(b2, birdW, birdH, true))
        birdFrames.add(Bitmap.createScaledBitmap(b3, birdW, birdH, true))
        b1.recycleQuietly(); b2.recycleQuietly(); b3.recycleQuietly()
        birdX = screenW * 0.28f
        birdY = screenH / 2f - birdH / 2f

        pipeGap = max(screenH * 0.25f, pipeGap)
        pipeGap = min(pipeGap, screenH * 0.45f)

        val fm = coinPaint.fontMetrics
        coinYOffset = (fm.ascent + fm.descent) / 2f

        resetAllPipesAndCoins()

        // Audio
        val audioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(audioAttr).build()
        sWing = soundPool.load(context, com.example.flappycoin.R.raw.wing, 1)
        sPoint = soundPool.load(context, com.example.flappycoin.R.raw.point, 1)
        sHit = soundPool.load(context, com.example.flappycoin.R.raw.hit, 1)
        sDie = soundPool.load(context, com.example.flappycoin.R.raw.die, 1)
    }

    // ----- Helpers -----
    private fun Bitmap.recycleQuietly() { try { if (!isRecycled) recycle() } catch (_: Throwable) {} }
    private fun createVerticalFlip(src: Bitmap) = Bitmap.createBitmap(src, 0, 0, src.width, src.height, Matrix().apply { preScale(1f, -1f) }, true)

    private fun resetAllPipesAndCoins() {
        pipes.clear()
        coins.clear()
        var startX = screenW.toFloat() + screenW * 0.25f
        for (i in 0 until visiblePairsInitial) {
            val center = safeRandomGapCenter()
            pipes.add(PipePair(startX, center))
            coins.add(Coin(startX + pipeW / 2f, center))
            startX += pipeHorizontalSpacing
        }
    }

    private fun safeRandomGapCenter(): Float {
        val margin = screenH * 0.12f
        val minCenter = margin + pipeGap / 2f
        val maxCenter = screenH - margin - pipeGap / 2f
        return if (maxCenter <= minCenter) minCenter else Random.nextInt(minCenter.toInt(), maxCenter.toInt()).toFloat()
    }

    // ----- Main loop -----
    override fun run() {
        while (playing) {
            val now = System.currentTimeMillis()
            val delta = now - lastFrameTime
            lastFrameTime = now
            if (delta <= 0) { Thread.sleep(1); continue }
            update(delta)
            draw()
            control()
        }
    }

    private fun update(deltaMs: Long) {
        val frameScale = min(deltaMs / 16f, 2f)
        if (isGameOver) { elapsedMs += deltaMs; return }
        elapsedMs += deltaMs

        // Scroll bg/base
        bgX1 -= bgSpeed * frameScale; bgX2 -= bgSpeed * frameScale
        baseX1 -= bgSpeed * frameScale; baseX2 -= bgSpeed * frameScale
        if (bgX1 <= -screenW) bgX1 = bgX2 + screenW
        if (bgX2 <= -screenW) bgX2 = bgX1 + screenW
        if (baseX1 <= -screenW) baseX1 = baseX2 + screenW
        if (baseX2 <= -screenW) baseX2 = baseX1 + screenW

        // Bird physics
        birdVel += gravity
        birdY += birdVel * frameScale
        if (birdY < 0f) { birdY = 0f; birdVel = 0f }
        val baseTop = (screenH - baseBmp.height).toFloat()
        if (birdY + birdH > baseTop) { birdY = baseTop - birdH; triggerGameOver(); return }

        // Bird animation
        birdAnimTimer += deltaMs
        if (birdAnimTimer >= birdAnimFrameMs) { birdAnimIndex = (birdAnimIndex + 1) % birdFrames.size; birdAnimTimer = 0L }

        // Pipes movement
        for (p in pipes) p.x -= bgSpeed * frameScale
        if (pipes.isNotEmpty() && pipes.first().x + pipeW < 0f) {
            pipes.removeAt(0); if (coins.isNotEmpty()) coins.removeAt(0)
            val lastX = pipes.lastOrNull()?.x ?: screenW.toFloat()
            val newX = lastX + pipeHorizontalSpacing
            val center = safeRandomGapCenter()
            pipes.add(PipePair(newX, center))
            coins.add(Coin(newX + pipeW / 2f, center))
        }

        // Collision
        val birdRect = RectF(birdX + 8f, birdY + 8f, birdX + birdW - 8f, birdY + birdH - 8f)
        for (p in pipes) {
            val topRect = RectF(p.x, 0f, p.x + pipeW, p.centerY - pipeGap / 2f)
            val bottomRect = RectF(p.x, p.centerY + pipeGap / 2f, p.x + pipeW, screenH.toFloat())
            if (RectF.intersects(birdRect, topRect) || RectF.intersects(birdRect, bottomRect)) { triggerGameOver(); break }
        }

        // Coins
        val it = coins.iterator()
        while (it.hasNext()) {
            val c = it.next()
            val coinRect = RectF(c.x - 40f, c.y - 40f, c.x + 40f, c.y + 40f)
            if (RectF.intersects(birdRect, coinRect)) { coinsCollected++; score++; it.remove(); try { soundPool.play(sPoint, 1f, 1f, 0,0,1f) } catch (_: Throwable){} }
        }

        // Passing pipes
        for (p in pipes) if (!p.passed && p.x + pipeW/2f < birdX) { p.passed = true; score++; try { soundPool.play(sPoint, 0.6f,0.6f,0,0,1f)} catch(_:{}) }

        distance += bgSpeed * frameScale
    }

    private fun triggerGameOver() {
        if (!isGameOver) {
            isGameOver = true
            playing = false
            try { soundPool.play(sHit,1f,1f,0,0,1f); soundPool.play(sDie,1f,1f,0,0,1f) } catch (_: Throwable){}
            onGameOver(score, coinsCollected, distance.toInt(), elapsedMs)
        }
    }

    // ----- Touch -----
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (isGameOver) { resetGame(); isGameOver=false; resume() }
            else { birdVel = jumpImpulse; try { soundPool.play(sWing,1f,1f,0,0,1f) } catch (_: Throwable){} }
        }
        return true
    }

    private fun resetGame() {
        birdY = screenH /2f - birdH/2f
        birdVel = 0f
        score = 0; coinsCollected = 0; distance = 0f; elapsedMs = 0L
        lastFrameTime = System.currentTimeMillis()
        resetAllPipesAndCoins()
    }

    private fun control() { try { Thread.sleep(targetFrameMs) } catch (_: InterruptedException) {} }

    // ----- Lifecycle -----
    fun pause() { playing=false; try { gameThread?.join() } catch (_: InterruptedException){}; try { soundPool.autoPause() } catch (_:Throwable){} }
    fun resume() { if(playing) return; playing=true; try{ soundPool.autoResume() } catch(_:_){ }; gameThread=Thread(this); lastFrameTime=System.currentTimeMillis(); gameThread?.start() }

    fun releaseResources() {
        for(b in birdFrames) if(!b.isRecycled) b.recycle()
        if(!bgBmp.isRecycled) bgBmp.recycle()
        if(!baseBmp.isRecycled) baseBmp.recycle()
        if(!pipeBmp.isRecycled) pipeBmp.recycle()
        if(!pipeTopBmp.isRecycled) pipeTopBmp.recycle()
        try { soundPool.release() } catch(_:_){}
    }
}
