package com.example.flappycoin.ui

import android.content.Context
import android.graphics.*
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.SurfaceView
import com.example.flappycoin.R
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.utils.Constants
import kotlin.random.Random
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.cos

class GameView(
    context: Context,
    private val onGameOver: (score: Int, coins: Int, distance: Int, time: Long) -> Unit,
    private val onWatchAd: () -> Unit,
    private val onMenuClick: () -> Unit
) : SurfaceView(context), Runnable {

    enum class GameState { START, PLAYING, PAUSED, GAME_OVER }
    enum class ObstacleType { PIPE, SPIKE, MOVING_PIPE, WIND_CURRENT }

    private var currentState = GameState.START
    private var playing = false
    private var gameThread: Thread? = null

    private val screenW = context.resources.displayMetrics.widthPixels
    private val screenH = context.resources.displayMetrics.heightPixels
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    // ================= BOUTONS =================
    private val replayButtonRect = RectF()
    private val adButtonRect = RectF()
    private val menuButtonRect = RectF()
    private val pauseButtonRect = RectF()
    private val resumeButtonRect = RectF()
    private var showGameOverButtons = false
    private var buttonScale = 1f
    private var buttonPulseDir = 1f

    // ================= ASSETS =================
    private val bgBmp: Bitmap
    private val baseBmp: Bitmap
    private val pipeBmp: Bitmap
    private val pipeTopBmp: Bitmap
    private val birdFrames = mutableListOf<Bitmap>()
    private var spikeBmp: Bitmap? = null

    // ================= DÉCOR =================
    private var bgX1 = 0f
    private var bgX2 = 0f
    private var baseX1 = 0f
    private var baseX2 = 0f
    private var scrollSpeed = 6.5f
    private val scrollAcceleration = 0.001f

    // ================= OISEAU =================
    private var birdX = 0f
    private var birdY = 0f
    private var birdW = 0
    private var birdH = 0
    private var birdVel = 0f
    private var birdRotation = 0f
    private var gravity = 0.6f
    private var jumpImpulse = -12f
    private val maxFallSpeed = 20f
    private val maxRiseSpeed = -18f
    private var birdAnimIndex = 0
    private var birdAnimTimer = 0L
    private val birdAnimFrameMs = 80L
    private var menuBob = 0f
    private var isShaking = false
    private var shakeIntensity = 0f

    // ================= OBSTACLES =================
    data class Obstacle(
        var x: Float,
        var centerY: Float,
        var type: ObstacleType = ObstacleType.PIPE,
        var passed: Boolean = false,
        var animOffset: Float = 0f
    )

    data class Coin(var x: Float, var y: Float, var collected: Boolean = false, var scale: Float = 1f)
    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float = 1f,
        var color: Int = Color.YELLOW,
        var size: Float = 5f
    )

    private val obstacles = mutableListOf<Obstacle>()
    private val coins = mutableListOf<Coin>()
    private val particles = mutableListOf<Particle>()
    private val pipeGap: Float
    private val pipeHorizontalSpacing: Float
    private val pipeW: Float
    private val pipeH: Float
    private val minGapCenter: Float
    private val maxGapCenter: Float

    // ================= SCORE & STATS =================
    private var score = 0
    private var coinsCount = 0
    private var distance = 0
    private var elapsedTimeMs = 0L
    private var lastFrameTime = System.currentTimeMillis()
    private var gameOverTime = 0L
    private var comboCounter = 0
    private var comboMultiplier = 1f
    private var lastCoinTime = 0L
    private var difficultyLevel = 1
    private var gapsCollected = 0
    private val gapsForDifficulty = Constants.DIFFICULTY_INCREASE_INTERVAL
    private var highScore = 0

    // ================= AUDIO =================
    private val soundPool: SoundPool
    private val sWing: Int
    private val sPoint: Int
    private val sHit: Int
    private val sDie: Int
    private val sButton: Int

    // ================= PAINTS =================
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textSize = 120f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        setShadowLayer(8f, 0f, 4f, Color.BLACK)
    }
    private val hudPaint = Paint().apply {
        color = Color.WHITE
        textSize = 50f
        isFakeBoldText = true
        setShadowLayer(5f, 0f, 2f, Color.BLACK)
    }
    private val statsHudPaint = Paint().apply {
        color = Color.WHITE
        textSize = 32f
        isFakeBoldText = true
        setShadowLayer(4f, 0f, 2f, Color.BLACK)
    }
    private val smallTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 0f, 2f, Color.BLACK)
    }
    private val coinEmojiPaint = Paint().apply {
        textSize = 70f
        textAlign = Paint.Align.CENTER
    }
    private val particlePaint = Paint().apply {
        isAntiAlias = true
    }
    private val cardPaint = Paint().apply {
        color = Color.argb(180, 44, 44, 44)
    }
    private val buttonPaint = Paint().apply {
        isAntiAlias = true
        setShadowLayer(10f, 0f, 5f, Color.BLACK)
    }
    private val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    private val buttonTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val adButtonPaint = Paint().apply {
        color = Color.argb(200, 255, 165, 0)
        isAntiAlias = true
        setShadowLayer(10f, 0f, 5f, Color.BLACK)
    }

    // ================= HITBOX =================
    private val birdRect = RectF()
    private val topPipeRect = RectF()
    private val bottomPipeRect = RectF()
    private val coinRect = RectF()

    init {
        // Chargement assets
        val rawBg = BitmapFactory.decodeResource(resources, R.drawable.background)
        bgBmp = Bitmap.createScaledBitmap(rawBg, screenW, screenH, true)
        rawBg.recycle()

        val rawBase = BitmapFactory.decodeResource(resources, R.drawable.base)
        val baseWidth = screenW
        val baseHeight = (rawBase.height * (baseWidth / rawBase.width.toFloat())).toInt()
        baseBmp = Bitmap.createScaledBitmap(rawBase, baseWidth, baseHeight, true)
        rawBase.recycle()

        bgX1 = 0f
        bgX2 = screenW.toFloat()
        baseX1 = 0f
        baseX2 = screenW.toFloat()

        val rawPipe = BitmapFactory.decodeResource(resources, R.drawable.pipe_green)
        val pW = (screenW * 0.18f).toInt()
        val pH = screenH
        pipeBmp = Bitmap.createScaledBitmap(rawPipe, pW, pH, true)
        val matrix = Matrix().apply { postScale(1f, -1f) }
        pipeTopBmp = Bitmap.createBitmap(pipeBmp, 0, 0, pipeBmp.width, pipeBmp.height, matrix, true)
        rawPipe.recycle()
        pipeW = pipeBmp.width.toFloat()
        pipeH = pipeBmp.height.toFloat()

        // Oiseau
        val b1 = BitmapFactory.decodeResource(resources, R.drawable.redbird_upflap)
        birdW = (screenW * 0.12f).toInt()
        birdH = (b1.height * (birdW / b1.width.toFloat())).toInt()
        birdFrames.add(Bitmap.createScaledBitmap(b1, birdW, birdH, true))
        birdFrames.add(Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.redbird_midflap),
            birdW, birdH, true
        ))
        birdFrames.add(Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.redbird_downflap),
            birdW, birdH, true
        ))

        // Spike
        try {
            spikeBmp = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.spike),
                (pipeW * 0.3f).toInt(),
                (pipeW * 0.3f).toInt(),
                true
            )
        } catch (e: Exception) {
            spikeBmp = null
        }

        // Gaps - 5.5x la taille de l'oiseau
        pipeGap = birdH * 5.5f
        
        val groundY = screenH - baseBmp.height
        val topMargin = screenH * 0.12f
        val bottomMargin = groundY - (screenH * 0.12f)
        minGapCenter = topMargin + pipeGap / 2
        maxGapCenter = bottomMargin - pipeGap / 2
        pipeHorizontalSpacing = screenW * 0.70f

        // Audio
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
        sButton = soundPool.load(context, R.raw.button_click, 1)

        loadHighScore()
        resetGame()
    }

    private fun loadHighScore() {
        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        highScore = prefs.getInt(Constants.PrefsKeys.BEST_SCORE, 0)
    }

    private fun saveHighScore() {
        if (score > highScore) {
            highScore = score
            val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
            prefs.edit().putInt(Constants.PrefsKeys.BEST_SCORE, highScore).apply()
        }
    }

    private fun resetGame() {
        birdX = screenW * 0.25f
        birdY = screenH / 2.5f
        birdVel = 0f
        birdRotation = 0f
        score = 0
        coinsCount = 0
        distance = 0
        elapsedTimeMs = 0L
        comboCounter = 0
        comboMultiplier = 1f
        difficultyLevel = 1
        gapsCollected = 0
        scrollSpeed = 6.5f
        obstacles.clear()
        coins.clear()
        particles.clear()
        showGameOverButtons = false

        var startX = screenW * 1.2f
        for (i in 0..4) {
            val center = randomGapCenter()
            val obstacleType = selectObstacleType()
            obstacles.add(Obstacle(startX, center, obstacleType))
            coins.add(Coin(startX + pipeW / 2f, center))
            startX += pipeHorizontalSpacing
        }
        
        setupButtons()
    }

    private fun setupButtons() {
        pauseButtonRect.set(screenW - 120f, 20f, screenW - 20f, 70f)
        
        val buttonWidth = screenW * 0.7f
        val buttonHeight = 80f
        val buttonX = (screenW - buttonWidth) / 2f
        val startY = screenH * 0.55f
        
        adButtonRect.set(buttonX, startY, buttonX + buttonWidth, startY + buttonHeight)
        replayButtonRect.set(buttonX, startY + buttonHeight + 15f, buttonX + buttonWidth, startY + buttonHeight * 2 + 15f)
        menuButtonRect.set(buttonX, startY + buttonHeight * 2 + 30f, buttonX + buttonWidth, startY + buttonHeight * 3 + 30f)
        resumeButtonRect.set(buttonX, screenH * 0.5f, buttonX + buttonWidth, screenH * 0.5f + buttonHeight)
    }

    private fun selectObstacleType(): ObstacleType {
        return when {
            difficultyLevel >= 7 && Random.nextFloat() < 0.3f -> {
                if (Random.nextBoolean()) ObstacleType.MOVING_PIPE else ObstacleType.WIND_CURRENT
            }
            difficultyLevel >= 5 && Random.nextFloat() < 0.2f -> ObstacleType.SPIKE
            else -> ObstacleType.PIPE
        }
    }

    private fun randomGapCenter(): Float {
        return Random.nextFloat() * (maxGapCenter - minGapCenter) + minGapCenter
    }

    override fun run() {
        while (playing) {
            val now = System.currentTimeMillis()
            val delta = now - lastFrameTime
            lastFrameTime = now
            if (delta > 0) {
                update(delta)
                draw()
            }
            try {
                Thread.sleep(Constants.FRAME_TIME_MS)
            } catch (_: Exception) {
            }
        }
    }

    private fun update(deltaMs: Long) {
        if (currentState == GameState.PAUSED) return

        val f = deltaMs / 16.6f
        elapsedTimeMs += deltaMs

        if ((currentState == GameState.GAME_OVER && showGameOverButtons) || currentState == GameState.PAUSED) {
            buttonScale += 0.01f * buttonPulseDir
            if (buttonScale > 1.1f || buttonScale < 0.9f) buttonPulseDir *= -1
        }

        if (currentState != GameState.GAME_OVER) {
            baseX1 -= scrollSpeed * f
            baseX2 -= scrollSpeed * f
            if (baseX1 + screenW <= 0) baseX1 = baseX2 + screenW
            if (baseX2 + screenW <= 0) baseX2 = baseX1 + screenW

            bgX1 -= (scrollSpeed / 4) * f
            bgX2 -= (scrollSpeed / 4) * f
            if (bgX1 + screenW <= 0) bgX1 = bgX2 + screenW
            if (bgX2 + screenW <= 0) bgX2 = bgX1 + screenW

            birdAnimTimer += deltaMs
            if (birdAnimTimer >= birdAnimFrameMs) {
                birdAnimIndex = (birdAnimIndex + 1) % birdFrames.size
                birdAnimTimer = 0
            }

            if (System.currentTimeMillis() - lastCoinTime > Constants.COMBO_DURATION_MS) {
                comboCounter = 0
                comboMultiplier = 1f
            }
        }

        when (currentState) {
            GameState.START -> {
                menuBob += 0.08f * f
                birdY = (screenH / 2.5f) + (sin(menuBob.toDouble()) * 20).toFloat()
                birdVel = 0f
            }

            GameState.PLAYING -> {
                gravity = 0.6f + (difficultyLevel * 0.05f)
                birdVel += gravity * f
                birdVel = min(birdVel, maxFallSpeed)
                birdVel = max(birdVel, maxRiseSpeed)
                birdY += birdVel * f
                birdRotation = (birdVel * 4f).coerceIn(-25f, 90f)

                if (isShaking) {
                    shakeIntensity -= 0.1f * f
                    if (shakeIntensity <= 0) isShaking = false
                    birdX += (Random.nextFloat() - 0.5f) * shakeIntensity * 10
                }

                val groundY = screenH - baseBmp.height.toFloat()
                if (birdY + birdH > groundY) {
                    birdY = groundY - birdH
                    triggerGameOver()
                }
                if (birdY < 0) {
                    birdY = 0f
                    triggerGameOver()
                }

                birdRect.set(birdX + 5, birdY + 5, birdX + birdW - 5, birdY + birdH - 5)

                val itObstacle = obstacles.iterator()
                var newObstacleX = 0f
                while (itObstacle.hasNext()) {
                    val o = itObstacle.next()
                    o.x -= scrollSpeed * f
                    distance = (distance + (scrollSpeed * f)).toInt()

                    if (o.type == ObstacleType.MOVING_PIPE) {
                        o.animOffset = sin((elapsedTimeMs / 500.0).toFloat()) * 50
                        o.centerY += o.animOffset * f
                    }

                    topPipeRect.set(o.x, 0f, o.x + pipeW, o.centerY - pipeGap / 2)
                    bottomPipeRect.set(o.x, o.centerY + pipeGap / 2, o.x + pipeW, screenH.toFloat())

                    if (RectF.intersects(birdRect, topPipeRect) || RectF.intersects(birdRect, bottomPipeRect)) {
                        triggerGameOver()
                    }

                    if (!o.passed && o.x + pipeW < birdX) {
                        o.passed = true
                        score += (1 * comboMultiplier).toInt()
                        soundPool.play(sPoint, 0.7f, 0.7f, 0, 0, 1f)
                        gapsCollected++

                        if (gapsCollected % gapsForDifficulty == 0) {
                            increaseDifficulty()
                        }
                    }

                    if (o.x + pipeW < 0) {
                        itObstacle.remove()
                        if (obstacles.isNotEmpty()) {
                            newObstacleX = obstacles.last().x + pipeHorizontalSpacing
                        }
                    }
                }

                if (newObstacleX > 0) {
                    val c = randomGapCenter()
                    val type = selectObstacleType()
                    obstacles.add(Obstacle(newObstacleX, c, type))
                    coins.add(Coin(newObstacleX + pipeW / 2, c))
                }

                val itCoin = coins.iterator()
                while (itCoin.hasNext()) {
                    val c = itCoin.next()
                    c.x -= scrollSpeed * f
                    c.scale = 1f + sin((elapsedTimeMs / 300.0).toFloat()) * 0.15f

                    if (!c.collected) {
                        coinRect.set(c.x - 25, c.y - 25, c.x + 25, c.y + 25)
                        if (RectF.intersects(birdRect, coinRect)) {
                            c.collected = true
                            coinsCount += (1 * comboMultiplier).toInt()
                            comboCounter++
                            comboMultiplier = 1f + (comboCounter * 0.1f).coerceAtMost(5f)
                            lastCoinTime = System.currentTimeMillis()
                            soundPool.play(sPoint, 0.9f, 0.9f, 0, 0, 1.8f)
                            spawnCoinParticles(c.x, c.y)
                        }
                    }

                    if (c.x + 50 < 0) itCoin.remove()
                }

                val itParticle = particles.iterator()
                while (itParticle.hasNext()) {
                    val p = itParticle.next()
                    p.x += p.vx * f
                    p.y += p.vy * f
                    p.vy += 0.3f * f
                    p.life -= 0.02f * f
                    if (p.life <= 0) itParticle.remove()
                }

                scrollSpeed += scrollAcceleration * f * difficultyLevel
            }

            GameState.GAME_OVER -> {
                val groundY = screenH - baseBmp.height.toFloat()
                if (birdY + birdH < groundY) {
                    birdVel += gravity * f
                    birdVel = min(birdVel, maxFallSpeed)
                    birdY += birdVel * f
                    birdRotation = 90f
                }
                
                if (System.currentTimeMillis() - gameOverTime > 1000) {
                    showGameOverButtons = true
                }
            }

            else -> {}
        }
    }

    private fun increaseDifficulty() {
        difficultyLevel++
        gravity += 0.05f
        jumpImpulse -= 0.5f
    }

    private fun spawnCoinParticles(x: Float, y: Float) {
        repeat(8) {
            val angle = (it * 45).toFloat() * (Math.PI / 180f).toFloat()
            val vx = cos(angle.toDouble()).toFloat() * 200
            val vy = sin(angle.toDouble()).toFloat() * 200
            particles.add(Particle(x, y, vx, vy, color = Color.YELLOW, size = 8f))
        }
    }

    private fun spawnJumpParticles() {
        repeat(4) {
            particles.add(Particle(
                x = birdX + birdW / 2,
                y = birdY + birdH / 2,
                vx = Random.nextFloat() * 200 - 100,
                vy = -Random.nextFloat() * 300,
                color = Color.YELLOW,
                size = 5f
            ))
        }
    }

    private fun triggerShake() {
        isShaking = true
        shakeIntensity = 5f
        vibrate(100)
    }

    private fun vibrate(ms: Long) {
        if (GamePreferences.isVibrationEnabled()) {
            vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun draw() {
        if (!holder.surface.isValid) return
        val canvas = holder.lockCanvas() ?: return

        canvas.drawBitmap(bgBmp, bgX1, 0f, paint)
        canvas.drawBitmap(bgBmp, bgX2, 0f, paint)

        for (o in obstacles) {
            when (o.type) {
                ObstacleType.PIPE -> {
                    canvas.drawBitmap(pipeTopBmp, o.x, o.centerY - pipeGap / 2 - pipeH, paint)
                    canvas.drawBitmap(pipeBmp, o.x, o.centerY + pipeGap / 2, paint)
                }
                ObstacleType.MOVING_PIPE -> {
                    val adjustedY = o.centerY + o.animOffset
                    canvas.drawBitmap(pipeTopBmp, o.x, adjustedY - pipeGap / 2 - pipeH, paint)
                    canvas.drawBitmap(pipeBmp, o.x, adjustedY + pipeGap / 2, paint)
                }
                ObstacleType.SPIKE -> {
                    spikeBmp?.let {
                        canvas.drawBitmap(it, o.x + pipeW / 2 - it.width / 2,
                            o.centerY - pipeGap / 2 - 50, paint)
                        canvas.drawBitmap(it, o.x + pipeW / 2 - it.width / 2,
                            o.centerY + pipeGap / 2 + 30, paint)
                    }
                }
                ObstacleType.WIND_CURRENT -> {
                    canvas.drawBitmap(pipeBmp, o.x, 0f, paint)
                    canvas.drawBitmap(pipeTopBmp, o.x, screenH - pipeH, paint)
                }
            }
        }

        for (c in coins) {
            if (!c.collected) {
                canvas.save()
                canvas.scale(c.scale, c.scale, c.x, c.y)
                canvas.drawText("🪙", c.x, c.y + 20, coinEmojiPaint)
                canvas.restore()
            }
        }

        for (p in particles) {
            particlePaint.color = p.color
            particlePaint.alpha = (p.life * 255).toInt()
            canvas.drawCircle(p.x, p.y, p.size, particlePaint)
        }

        canvas.drawBitmap(baseBmp, baseX1, screenH - baseBmp.height.toFloat(), paint)
        canvas.drawBitmap(baseBmp, baseX2, screenH - baseBmp.height.toFloat(), paint)

        canvas.save()
        canvas.rotate(birdRotation, birdX + birdW / 2f, birdY + birdH / 2f)
        canvas.drawBitmap(birdFrames[birdAnimIndex], birdX, birdY, paint)
        canvas.restore()

        when (currentState) {
            GameState.START -> drawStartScreen(canvas)
            GameState.PLAYING -> drawPlayingHUD(canvas)
            GameState.PAUSED -> drawPauseScreen(canvas)
            GameState.GAME_OVER -> drawGameOverScreen(canvas)
        }

        holder.unlockCanvasAndPost(canvas)
    }

    private fun drawStartScreen(canvas: Canvas) {
        canvas.drawARGB(100, 0, 0, 0)
        canvas.drawText("FLAPPY COIN", screenW / 2f, screenH * 0.25f, scorePaint)
        canvas.drawText("TAP TO START", screenW / 2f, screenH * 0.45f, hudPaint)
        canvas.drawText("Meilleur: $highScore", screenW / 2f, screenH * 0.60f, smallTextPaint)
    }

    private fun drawPlayingHUD(canvas: Canvas) {
        val timeSeconds = elapsedTimeMs / 1000
        val timeMinutes = timeSeconds / 60
        val timeSecs = timeSeconds % 60

        canvas.drawRect(8f, 8f, screenW / 2.5f, 120f, cardPaint)
        canvas.drawText(score.toString(), 40f, 45f, statsHudPaint)
        canvas.drawText("${distance / 10}m", 40f, 75f, smallTextPaint)
        canvas.drawText(String.format("%02d:%02d", timeMinutes, timeSecs), 40f, 105f, smallTextPaint)

        canvas.drawRect(pauseButtonRect, cardPaint)
        canvas.drawText("🪙", screenW - 70f, 45f, hudPaint)
        canvas.drawText(coinsCount.toString(), screenW - 70f, 75f, statsHudPaint)
        
        buttonPaint.color = Color.argb(200, 100, 100, 100)
        canvas.drawRoundRect(pauseButtonRect, 20f, 20f, buttonPaint)
        canvas.drawText("⏸️", screenW - 70f, 55f, buttonTextPaint)
        
        if (comboCounter > 1) {
            canvas.drawText("x${String.format("%.1f", comboMultiplier)}", 
                screenW / 2f, screenH * 0.15f, hudPaint)
        }
    }

    private fun drawPauseScreen(canvas: Canvas) {
        canvas.drawARGB(180, 0, 0, 0)
        canvas.drawText("PAUSE", screenW / 2f, screenH * 0.35f, scorePaint)
        
        buttonPaint.color = Color.argb(200, 76, 175, 80)
        canvas.save()
        canvas.scale(buttonScale, buttonScale, resumeButtonRect.centerX(), resumeButtonRect.centerY())
        canvas.drawRoundRect(resumeButtonRect, 50f, 50f, buttonPaint)
        canvas.drawRoundRect(resumeButtonRect, 50f, 50f, borderPaint)
        canvas.restore()
        canvas.drawText("REPRENDRE", screenW / 2f, resumeButtonRect.centerY() + 15f, buttonTextPaint)
        
        buttonPaint.color = Color.argb(200, 100, 100, 100)
        canvas.drawRoundRect(menuButtonRect, 50f, 50f, buttonPaint)
        canvas.drawRoundRect(menuButtonRect, 50f, 50f, borderPaint)
        canvas.drawText("MENU", screenW / 2f, menuButtonRect.centerY() + 15f, buttonTextPaint)
    }

    private fun drawGameOverScreen(canvas: Canvas) {
        canvas.drawARGB(200, 0, 0, 0)
        canvas.drawText("GAME OVER", screenW / 2f, screenH * 0.20f, scorePaint)
        
        val timeSeconds = elapsedTimeMs / 1000
        val localCurrency = String.format("%.2f", coinsCount / Constants.COINS_PER_DOLLAR.toFloat())
        
        canvas.drawText("Score: $score", screenW / 2f, screenH * 0.28f, hudPaint)
        canvas.drawText("Pièces: $coinsCount ($$localCurrency)", screenW / 2f, screenH * 0.33f, statsHudPaint)
        canvas.drawText("Distance: ${distance / 10}m | Temps: ${timeSeconds}s", screenW / 2f, screenH * 0.38f, statsHudPaint)

        if (score > highScore) {
            canvas.drawText("🏆 NOUVEAU RECORD 🏆", screenW / 2f, screenH * 0.44f, hudPaint)
        }

        if (showGameOverButtons) {
            canvas.save()
            canvas.scale(buttonScale, buttonScale, adButtonRect.centerX(), adButtonRect.centerY())
            canvas.drawRoundRect(adButtonRect, 40f, 40f, adButtonPaint)
            canvas.drawRoundRect(adButtonRect, 40f, 40f, borderPaint)
            canvas.restore()
            canvas.drawText("📺 REGARDER UNE PUB", screenW / 2f, adButtonRect.centerY() + 15f, buttonTextPaint)

            buttonPaint.color = Color.argb(200, 76, 175, 80)
            canvas.drawRoundRect(replayButtonRect, 40f, 40f, buttonPaint)
            canvas.drawRoundRect(replayButtonRect, 40f, 40f, borderPaint)
            canvas.drawText("🔄 REJOUER", screenW / 2f, replayButtonRect.centerY() + 15f, buttonTextPaint)

            buttonPaint.color = Color.argb(200, 100, 100, 100)
            canvas.drawRoundRect(menuButtonRect, 40f, 40f, buttonPaint)
            canvas.drawRoundRect(menuButtonRect, 40f, 40f, borderPaint)
            canvas.drawText("🏠 MENU", screenW / 2f, menuButtonRect.centerY() + 15f, buttonTextPaint)
        }
    }

    private fun triggerGameOver() {
        if (currentState != GameState.GAME_OVER) {
            currentState = GameState.GAME_OVER
            gameOverTime = System.currentTimeMillis()
            saveHighScore()

            triggerShake()
            soundPool.play(sHit, 1f, 1f, 0, 0, 1f)
            soundPool.play(sDie, 1f, 1f, 0, 0, 1f)

            post {
                onGameOver(score, coinsCount, distance / 10, elapsedTimeMs)
            }
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                
                when (currentState) {
                    GameState.START -> {
                        currentState = GameState.PLAYING
                        soundPool.play(sWing, 0.5f, 0.5f, 0, 0, 1f)
                    }
                    
                    GameState.PLAYING -> {
                        if (x >= pauseButtonRect.left && x <= pauseButtonRect.right &&
                            y >= pauseButtonRect.top && y <= pauseButtonRect.bottom) {
                            currentState = GameState.PAUSED
                            soundPool.play(sButton, 0.5f, 0.5f, 0, 0, 1f)
                        } else {
                            birdVel = jumpImpulse
                            soundPool.play(sWing, 0.5f, 0.5f, 0, 0, 1f)
                            spawnJumpParticles()
                        }
                    }
                    
                    GameState.PAUSED -> {
                        soundPool.play(sButton, 0.5f, 0.5f, 0, 0, 1f)
                        
                        if (x >= resumeButtonRect.left && x <= resumeButtonRect.right &&
                            y >= resumeButtonRect.top && y <= resumeButtonRect.bottom) {
                            currentState = GameState.PLAYING
                        } else if (x >= menuButtonRect.left && x <= menuButtonRect.right &&
                                y >= menuButtonRect.top && y <= menuButtonRect.bottom) {
                            resetGame()
                            currentState = GameState.START
                            onMenuClick()
                        }
                    }
                    
                    GameState.GAME_OVER -> {
                        if (showGameOverButtons) {
                            soundPool.play(sButton, 0.5f, 0.5f, 0, 0, 1f)
                            
                            if (x >= adButtonRect.left && x <= adButtonRect.right &&
                                y >= adButtonRect.top && y <= adButtonRect.bottom) {
                                onWatchAd()
                            } else if (x >= replayButtonRect.left && x <= replayButtonRect.right &&
                                    y >= replayButtonRect.top && y <= replayButtonRect.bottom) {
                                resetGame()
                                currentState = GameState.PLAYING
                            } else if (x >= menuButtonRect.left && x <= menuButtonRect.right &&
                                    y >= menuButtonRect.top && y <= menuButtonRect.bottom) {
                                resetGame()
                                currentState = GameState.START
                                onMenuClick()
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    fun revive() {
        if (currentState == GameState.GAME_OVER) {
            birdY = screenH / 2.5f
            birdVel = 0f
            birdRotation = 0f
            currentState = GameState.PLAYING
            showGameOverButtons = false
            triggerShake()
            soundPool.play(sWing, 0.5f, 0.5f, 0, 0, 1f)
            particles.clear()
        }
    }

    fun pause() {
        if (currentState == GameState.PLAYING) {
            currentState = GameState.PAUSED
        }
        playing = false
        gameThread?.join()
    }

    fun resume() {
        playing = true
        lastFrameTime = System.currentTimeMillis()
        gameThread = Thread(this).apply { start() }
    }
}