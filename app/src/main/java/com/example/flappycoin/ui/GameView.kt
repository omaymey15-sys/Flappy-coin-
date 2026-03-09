package com.example.flappycoin

import android.content.Context
import android.graphics.*
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.*
import kotlin.random.Random

enum class GameState { START, PLAYING, GAME_OVER }
enum class ObstacleType { PIPE, MOVING_PIPE, SPIKE, WIND_CURRENT }

data class Obstacle(
    var x: Float,
    var centerY: Float,
    var type: ObstacleType = ObstacleType.PIPE,
    var passed: Boolean = false,
    var coinCollected: Boolean = false,
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

class GameView(context: Context) : SurfaceView(context), Runnable {
    private val holder: SurfaceHolder = getHolder()
    private var gameThread: Thread? = null
    private var playing = false

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private var currentState: GameState = GameState.START
    private var onGameOver: (score: Int, coins: Int, distance: Int, timeMs: Long) -> Unit = { _, _, _, _ -> }

    // ================= SCREEN =================
    private val screenW: Int
    private val screenH: Int

    // ================= BIRD =================
    private var birdX = 0f
    private var birdY = 0f
    private var birdW = 0
    private var birdH = 0
    private var birdVel = 0f
    private var birdRotation = 0f
    private var gravity = 0.6f
    private var jumpImpulse = -15f
    private var scrollAcceleration = 0.008f

    private val birdFrames = mutableListOf<Bitmap>()
    private var bgBmp: Bitmap? = null
    private var baseBmp: Bitmap? = null
    private var pipeBmp: Bitmap? = null
    private var pipeTopBmp: Bitmap? = null
    private var spikeBmp: Bitmap? = null

    private val maxFallSpeed = 20f
    private val maxRiseSpeed = -18f
    private var birdAnimIndex = 0
    private var birdAnimTimer = 0L
    private val birdAnimFrameMs = 80L
    private var menuBob = 0f
    private var isShaking = false
    private var shakeIntensity = 0f

    // ================= SCROLLING =================
    private var bgX1 = 0f
    private var bgX2 = 0f
    private var baseX1 = 0f
    private var baseX2 = 0f
    private var scrollSpeed = 6.5f

    // ================= OBSTACLES =================
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
    private val gapsForDifficulty = 5

    // ================= AUDIO =================
    private val soundPool: SoundPool
    private val sWing: Int
    private val sPoint: Int
    private val sHit: Int
    private val sDie: Int

    // ================= PAINTS (Pré-initialisées) =================
    private val paintSystem = PaintSystem()

    // ================= HITBOX (Pré-allouées) =================
    private val birdRect = RectF()
    private val topPipeRect = RectF()
    private val bottomPipeRect = RectF()
    private val coinRect = RectF()

    init {
        screenW = resources.displayMetrics.widthPixels
        screenH = resources.displayMetrics.heightPixels
        holder.addCallback(SurfaceCallback())

        loadAssets()
        initializeGameDimensions()
        initializeAudio()
        resetGame()
    }

    // ================= ASSET LOADING =================
    private fun loadAssets() {
        try {
            val rawBg = BitmapFactory.decodeResource(resources, R.drawable.background)
            bgBmp = Bitmap.createScaledBitmap(rawBg, screenW, screenH, true)
            rawBg.recycle()

            val rawBase = BitmapFactory.decodeResource(resources, R.drawable.base)
            val baseWidth = screenW
            val baseHeight = (rawBase.height * (baseWidth / rawBase.width.toFloat())).toInt()
            baseBmp = Bitmap.createScaledBitmap(rawBase, baseWidth, baseHeight, true)
            rawBase.recycle()

            loadBirdFrames()
            loadPipeAssets()
            loadSpikeAsset()
        } catch (e: Exception) {
            android.util.Log.e("GameView", "Erreur chargement assets", e)
        }
    }

    private fun loadBirdFrames() {
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
    }

    private fun loadPipeAssets() {
        val rawPipe = BitmapFactory.decodeResource(resources, R.drawable.pipe_green)
        val pW = (screenW * 0.18f).toInt()
        val pH = screenH
        pipeBmp = Bitmap.createScaledBitmap(rawPipe, pW, pH, true)

        val matrix = Matrix().apply { postScale(1f, -1f) }
        pipeTopBmp = Bitmap.createBitmap(pipeBmp!!, 0, 0, pipeBmp!!.width, pipeBmp!!.height, matrix, true)
        rawPipe.recycle()

        pipeW = pipeBmp!!.width.toFloat()
        pipeH = pipeBmp!!.height.toFloat()
    }

    private fun loadSpikeAsset() {
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
    }

    private fun initializeGameDimensions() {
        bgX1 = 0f
        bgX2 = screenW.toFloat()
        baseX1 = 0f
        baseX2 = screenW.toFloat()

        pipeGap = birdH * 3.5f
        val groundY = screenH - baseBmp!!.height
        val topMargin = screenH * 0.12f
        val bottomMargin = groundY - (screenH * 0.12f)
        minGapCenter = topMargin + pipeGap / 2
        maxGapCenter = bottomMargin - pipeGap / 2
        pipeHorizontalSpacing = screenW * 0.70f
    }

    private fun initializeAudio() {
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

    // ================= GAME STATE =================
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

        var startX = screenW * 1.2f
        repeat(5) {
            val center = randomGapCenter()
            val obstacleType = selectObstacleType()
            obstacles.add(Obstacle(startX, center, obstacleType))
            coins.add(Coin(startX + pipeW / 2f, center))
            startX += pipeHorizontalSpacing
        }
        currentState = GameState.START
    }

    fun revive() {
        currentState = GameState.PLAYING
    }

    // ================= GAME LOGIC =================
    private fun selectObstacleType(): ObstacleType = when {
        difficultyLevel >= 7 && Random.nextFloat() < 0.3f -> {
            if (Random.nextBoolean()) ObstacleType.MOVING_PIPE else ObstacleType.WIND_CURRENT
        }
        difficultyLevel >= 5 && Random.nextFloat() < 0.2f -> ObstacleType.SPIKE
        else -> ObstacleType.PIPE
    }

    private fun randomGapCenter(): Float = Random.nextFloat() * (maxGapCenter - minGapCenter) + minGapCenter

    private fun increaseDifficulty() {
        difficultyLevel++
        gravity += 0.05f
        jumpImpulse -= 0.5f
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
        val f = deltaMs / 16.6f
        elapsedTimeMs += deltaMs

        updateCombo()
        updateState(f)
    }

    private fun updateCombo() {
        if (System.currentTimeMillis() - lastCoinTime > Constants.COMBO_DURATION_MS) {
            comboCounter = 0
            comboMultiplier = 1f
        }
    }

    private fun updateState(f: Float) {
        when (currentState) {
            GameState.START -> updateStartState(f)
            GameState.PLAYING -> updatePlayingState(f)
            GameState.GAME_OVER -> updateGameOverState(f)
        }
    }

    private fun updateStartState(f: Float) {
        menuBob += 0.08f * f
        birdY = (screenH / 2.5f) + (sin(menuBob.toDouble()) * 20).toFloat()
        birdVel = 0f
    }

    private fun updatePlayingState(f: Float) {
        updateScrolling(f)
        updateBirdPhysics(f)
        updateShake(f)
        updateObstacles(f)
        updateCoins(f)
        updateParticles(f)
    }

    private fun updateScrolling(f: Float) {
        baseX1 -= scrollSpeed * f
        baseX2 -= scrollSpeed * f
        if (baseX1 + screenW <= 0) baseX1 = baseX2 + screenW
        if (baseX2 + screenW <= 0) baseX2 = baseX1 + screenW

        bgX1 -= (scrollSpeed / 4) * f
        bgX2 -= (scrollSpeed / 4) * f
        if (bgX1 + screenW <= 0) bgX1 = bgX2 + screenW
        if (bgX2 + screenW <= 0) bgX2 = baseX1 + screenW

        birdAnimTimer += f.toLong()
        if (birdAnimTimer >= birdAnimFrameMs) {
            birdAnimIndex = (birdAnimIndex + 1) % birdFrames.size
            birdAnimTimer = 0
        }
    }

    private fun updateBirdPhysics(f: Float) {
        gravity = 0.6f + (difficultyLevel * 0.05f)
        birdVel += gravity * f
        birdVel = birdVel.coerceIn(maxRiseSpeed, maxFallSpeed)
        birdY += birdVel * f
        birdRotation = (birdVel * 4f).coerceIn(-25f, 90f)

        checkBoundaryCollisions()
    }

    private fun checkBoundaryCollisions() {
        val groundY = screenH - baseBmp!!.height.toFloat()
        if (birdY + birdH > groundY || birdY < 0) {
            triggerGameOver()
        }
    }

    private fun updateShake(f: Float) {
        if (isShaking) {
            shakeIntensity -= 0.1f * f
            if (shakeIntensity <= 0) isShaking = false
            birdX += (Random.nextFloat() - 0.5f) * shakeIntensity * 10
        }
    }

    private fun updateObstacles(f: Float) {
        birdRect.set(birdX + 5, birdY + 5, birdX + birdW - 5, birdY + birdH - 5)

        val itObstacle = obstacles.iterator()
        var newObstacleX = 0f

        while (itObstacle.hasNext()) {
            val o = itObstacle.next()
            o.x -= scrollSpeed * f
            distance = (distance + (scrollSpeed * f)).toInt()

            updateObstacleAnimation(o, f)
            checkObstacleCollision(o)
            checkObstaclePassed(o)

            if (o.x + pipeW < 0) {
                itObstacle.remove()
                newObstacleX = obstacles.lastOrNull()?.x?.plus(pipeHorizontalSpacing) ?: 0f
            }
        }

        if (newObstacleX > 0) {
            spawnNewObstacle(newObstacleX)
        }
    }

    private fun updateObstacleAnimation(o: Obstacle, f: Float) {
        if (o.type == ObstacleType.MOVING_PIPE) {
            o.animOffset = sin((elapsedTimeMs / 500.0).toFloat()) * 50
            o.centerY += o.animOffset * f
        }
    }

    private fun checkObstacleCollision(o: Obstacle) {
        topPipeRect.set(o.x, 0f, o.x + pipeW, o.centerY - pipeGap / 2)
        bottomPipeRect.set(o.x, o.centerY + pipeGap / 2, o.x + pipeW, screenH.toFloat())

        if (RectF.intersects(birdRect, topPipeRect) || RectF.intersects(birdRect, bottomPipeRect)) {
            triggerGameOver()
        }
    }

    private fun checkObstaclePassed(o: Obstacle) {
        if (!o.passed && o.x + pipeW < birdX) {
            o.passed = true
            score += (10 * comboMultiplier).toInt()
            soundPool.play(sPoint, 0.7f, 0.7f, 0, 0, 1f)
            gapsCollected++

            if (gapsCollected % gapsForDifficulty == 0) {
                increaseDifficulty()
            }
        }
    }

    private fun spawnNewObstacle(x: Float) {
        val center = randomGapCenter()
        val type = selectObstacleType()
        obstacles.add(Obstacle(x, center, type))
        coins.add(Coin(x + pipeW / 2, center))
    }

    private fun updateCoins(f: Float) {
        val itCoin = coins.iterator()
        while (itCoin.hasNext()) {
            val c = itCoin.next()
            c.x -= scrollSpeed * f
            c.scale = 1f + sin((elapsedTimeMs / 300.0).toFloat()) * 0.15f

            if (!c.collected) {
                coinRect.set(c.x - 25, c.y - 25, c.x + 25, c.y + 25)
                if (RectF.intersects(birdRect, coinRect)) {
                    collectCoin(c)
                }
            }

            if (c.x + 50 < 0) itCoin.remove()
        }
    }

    private fun collectCoin(c: Coin) {
        c.collected = true
        coinsCount += (10 * comboMultiplier).toInt()
        comboCounter++
        comboMultiplier = (1f + (comboCounter * 0.1f)).coerceAtMost(5f)
        lastCoinTime = System.currentTimeMillis()
        soundPool.play(sPoint, 0.9f, 0.9f, 0, 0, 1.8f)
        spawnCoinParticles(c.x, c.y)
    }

    private fun updateParticles(f: Float) {
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

    private fun updateGameOverState(f: Float) {
        val groundY = screenH - baseBmp!!.height.toFloat()
        if (birdY + birdH < groundY) {
            birdVel += gravity * f
            birdVel = min(birdVel, maxFallSpeed)
            birdY += birdVel * f
            birdRotation = 90f
        }
    }

    // ================= PARTICLE EFFECTS =================
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

    // ================= FEEDBACK =================
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

    private fun triggerGameOver() {
        if (currentState != GameState.GAME_OVER) {
            currentState = GameState.GAME_OVER
            gameOverTime = System.currentTimeMillis()
            triggerShake()
            soundPool.play(sHit, 1f, 1f, 0, 0, 1f)
            soundPool.play(sDie, 1f, 1f, 0, 0, 1f)
            onGameOver(score, coinsCount, distance / 10, elapsedTimeMs)
        }
    }

    // ================= RENDERING =================
    private fun draw() {
        if (!holder.surface.isValid) return
        val canvas = holder.lockCanvas() ?: return

        try {
            drawBackground(canvas)
            drawObstacles(canvas)
            drawCoins(canvas)
            drawParticles(canvas)
            drawBase(canvas)
            drawBird(canvas)
            drawHUD(canvas)
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawBitmap(bgBmp!!, bgX1, 0f, paintSystem.paint)
        canvas.drawBitmap(bgBmp!!, bgX2, 0f, paintSystem.paint)
    }

    private fun drawObstacles(canvas: Canvas) {
        for (o in obstacles) {
            when (o.type) {
                ObstacleType.PIPE -> drawPipe(canvas, o.x, o.centerY)
                ObstacleType.MOVING_PIPE -> drawMovingPipe(canvas, o.x, o.centerY + o.animOffset)
                ObstacleType.SPIKE -> drawSpikes(canvas, o.x, o.centerY)
                ObstacleType.WIND_CURRENT -> drawWindCurrent(canvas, o.x)
            }
        }
    }

    private fun drawPipe(canvas: Canvas, x: Float, centerY: Float) {
        canvas.drawBitmap(pipeTopBmp!!, x, centerY - pipeGap / 2 - pipeH, paintSystem.paint)
        canvas.drawBitmap(pipeBmp!!, x, centerY + pipeGap / 2, paintSystem.paint)
    }

    private fun drawMovingPipe(canvas: Canvas, x: Float, centerY: Float) {
        canvas.drawBitmap(pipeTopBmp!!, x, centerY - pipeGap / 2 - pipeH, paintSystem.paint)
        canvas.drawBitmap(pipeBmp!!, x, centerY + pipeGap / 2, paintSystem.paint)
    }

    private fun drawSpikes(canvas: Canvas, x: Float, centerY: Float) {
        spikeBmp?.let {
            canvas.drawBitmap(it, x + pipeW / 2 - it.width / 2, centerY - pipeGap / 2 - 50, paintSystem.paint)
            canvas.drawBitmap(it, x + pipeW / 2 - it.width / 2, centerY + pipeGap / 2 + 30, paintSystem.paint)
        }
    }

    private fun drawWindCurrent(canvas: Canvas, x: Float) {
        canvas.drawBitmap(pipeBmp!!, x, 0f, paintSystem.paint)
        canvas.drawBitmap(pipeTopBmp!!, x, screenH - pipeH, paintSystem.paint)
    }

    private fun drawCoins(canvas: Canvas) {
        for (c in coins) {
            if (!c.collected) {
                canvas.save()
                canvas.scale(c.scale, c.scale, c.x, c.y)
                canvas.drawText("🪙", c.x, c.y + 20, paintSystem.coinEmojiPaint)
                canvas.restore()
            }
        }
    }

    private fun drawParticles(canvas: Canvas) {
        for (p in particles) {
            paintSystem.particlePaint.color = p.color
            paintSystem.particlePaint.alpha = (p.life * 255).toInt()
            canvas.drawCircle(p.x, p.y, p.size, paintSystem.particlePaint)
        }
    }

    private fun drawBase(canvas: Canvas) {
        canvas.drawBitmap(baseBmp!!, baseX1, screenH - baseBmp!!.height.toFloat(), paintSystem.paint)
        canvas.drawBitmap(baseBmp!!, baseX2, screenH - baseBmp!!.height.toFloat(), paintSystem.paint)
    }

    private fun drawBird(canvas: Canvas) {
        canvas.save()
        canvas.rotate(birdRotation, birdX + birdW / 2f, birdY + birdH / 2f)
        canvas.drawBitmap(birdFrames[birdAnimIndex], birdX, birdY, paintSystem.paint)
        canvas.restore()
    }

    private fun drawHUD(canvas: Canvas) {
        when (currentState) {
            GameState.START -> drawStartScreen(canvas)
            GameState.PLAYING -> drawGameHUD(canvas)
            GameState.GAME_OVER -> drawGameOverScreen(canvas)
        }
    }

    private fun drawStartScreen(canvas: Canvas) {
        canvas.drawARGB(100, 0, 0, 0)
        canvas.drawText("FLAPPY COIN", screenW / 2f, screenH * 0.25f, paintSystem.scorePaint)
        canvas.drawText("TAP TO START", screenW / 2f, screenH * 0.45f, paintSystem.hudPaint)
    }

    private fun drawGameHUD(canvas: Canvas) {
        val (minutes, seconds) = formatTime(elapsedTimeMs)

        // Stats haut gauche
        canvas.drawRect(8f, 8f, screenW / 2.5f, 120f, paintSystem.cardPaint)
        canvas.drawText(score.toString(), 40f, 45f, paintSystem.statsHudPaint)
        canvas.drawText("${distance / 10}m", 40f, 75f, paintSystem.smallTextPaint)
        canvas.drawText(String.format("%02d:%02d", minutes, seconds), 40f, 105f, paintSystem.smallTextPaint)

        // Coins haut droit
        canvas.drawRect(screenW - 150f, 8f, screenW - 8f, 80f, paintSystem.cardPaint)
        canvas.drawText("🪙", screenW - 80f, 45f, paintSystem.hudPaint)
        canvas.drawText(coinsCount.toString(), screenW - 80f, 75f, paintSystem.statsHudPaint)
    }

    private fun drawGameOverScreen(canvas: Canvas) {
        canvas.drawARGB(180, 0, 0, 0)
        canvas.drawText("GAME OVER", screenW / 2f, screenH * 0.30f, paintSystem.scorePaint)

        val (_, seconds) = formatTime(elapsedTimeMs)
        canvas.drawText(
            "Score: $score | 🪙 $coinsCount | ${distance / 10}m | ${seconds}s",
            screenW / 2f,
            screenH * 0.50f,
            paintSystem.hudPaint
        )

        if (System.currentTimeMillis() - gameOverTime > 1000) {
            canvas.drawText("TAP FOR OPTIONS", screenW / 2f, screenH * 0.72f, paintSystem.smallTextPaint)
        }
    }

    private fun formatTime(ms: Long): Pair<Long, Long> {
        val seconds = ms / 1000
        return Pair(seconds / 60, seconds % 60)
    }

    // ================= INPUT =================
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            when (currentState) {
                GameState.START -> currentState = GameState.PLAYING
                GameState.PLAYING -> {
                    birdVel = jumpImpulse
                    soundPool.play(sWing, 0.5f, 0.5f, 0, 0, 1f)
                    spawnJumpParticles()
                }
                else -> {}
            }
        }
        return true
    }

    // ================= LIFECYCLE =================
    fun pause() {
        playing = false
        gameThread?.join()
    }

    fun resume() {
        playing = true
        lastFrameTime = System.currentTimeMillis()
        gameThread = Thread(this).apply { start() }
    }

    fun destroy() {
        soundPool.release()
        birdFrames.forEach { it.recycle() }
        bgBmp?.recycle()
        baseBmp?.recycle()
        pipeBmp?.recycle()
        pipeTopBmp?.recycle()
        spikeBmp?.recycle()
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            playing = true
            gameThread = Thread(this@GameView).apply { start() }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            pause()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    }

    // ================= PAINT SYSTEM =================
    private inner class PaintSystem {
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        val scorePaint = Paint().apply {
            color = Color.WHITE
            textSize = 120f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 4f, Color.BLACK)
        }
        val hudPaint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            isFakeBoldText = true
            setShadowLayer(5f, 0f, 2f, Color.BLACK)
        }
        val statsHudPaint = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            isFakeBoldText = true
            setShadowLayer(4f, 0f, 2f, Color.BLACK)
        }
        val smallTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 0f, 2f, Color.BLACK)
        }
        val coinEmojiPaint = Paint().apply {
            textSize = 70f
            textAlign = Paint.Align.CENTER
        }
        val particlePaint = Paint().apply {
            isAntiAlias = true
        }
        val cardPaint = Paint().apply {
            color = Color.argb(180, 44, 44, 44)
        }
    }
}

object Constants {
    const val FRAME_TIME_MS = 16L
    const val COMBO_DURATION_MS = 2000L
}