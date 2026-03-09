package com.example.flappycoin.ui

import android.content.Context
import android.graphics.*
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.SurfaceHolder
import com.example.flappycoin.R
import com.example.flappycoin.managers.GamePreferences
import kotlin.random.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.sqrt

class GameView(
    context: Context,
    private val onGameOver: (score: Int, coins: Int, distance: Int, time: Long) -> Unit
) : SurfaceView(context), Runnable {

    // ================== ÉNUMÉRATIONS ==================
    enum class GameState { START, PLAYING, PAUSED, GAME_OVER }
    enum class ObstacleType { PIPE, SPIKE, MOVING_PIPE, WIND_CURRENT }

    // ================== ÉTATS DU JEU ==================
    private var currentState = GameState.START
    private var playing = false
    private var gameThread: Thread? = null
    private val targetFrameMs = 16L
    private var lastFrameTime = System.currentTimeMillis()
    private var gameOverTime = 0L
    private var elapsedTimeMs = 0L
    private var distance = 0

    // ================== ÉCRAN & DIMENSIONS ==================
    private val screenW = context.resources.displayMetrics.widthPixels
    private val screenH = context.resources.displayMetrics.heightPixels
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    // ================== BITMAPS ==================
    private val bgBmp: Bitmap
    private val baseBmp: Bitmap
    private val pipeBmp: Bitmap
    private val pipeTopBmp: Bitmap
    private val birdFrames = mutableListOf<Bitmap>()
    private val spikeBmp: Bitmap?
    private var birdW = 0
    private var birdH = 0

    // ================== DÉCOR ==================
    private var bgX1 = 0f
    private var bgX2 = 0f
    private var baseX1 = 0f
    private var baseX2 = 0f
    private var scrollSpeed = 6.5f
    private val scrollAcceleration = 0.001f // Accélération progressive

    // ================== OISEAU (Physique avancée) ==================
    private var birdX = 0f
    private var birdY = 0f
    private var birdVel = 0f
    private var birdRotation = 0f
    private var birdAngularVel = 0f
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

    // ================== OBSTACLES ==================
    data class Obstacle(
        var x: Float,
        var centerY: Float,
        var type: ObstacleType = ObstacleType.PIPE,
        var passed: Boolean = false,
        var coinCollected: Boolean = false,
        var animOffset: Float = 0f
    )

    data class Coin(
        var x: Float,
        var y: Float,
        var collected: Boolean = false,
        var scale: Float = 1f
    )

    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var color: Int = Color.YELLOW,
        var size: Float = 5f,
        var life: Float = 1f
    )

    private val obstacles = mutableListOf<Obstacle>()
    private val coins = mutableListOf<Coin>()
    private val particles = mutableListOf<Particle>()

    // ================== COLLISIONS ==================
    private var score = 0
    private var coinsCount = 0
    private var bestScore = 0
    private var bestCoins = 0
    private var comboCounter = 0
    private var difficultyLevel = 1

    // ================== SONS ==================
    private lateinit var soundPool: SoundPool
    private var sWing = 0
    private var sPoint = 0
    private var sHit = 0
    private var sDie = 0
    private var sCombo = 0

    // ================== PEINTURE ==================
    private val paint = Paint()
    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textSize = 80f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val hudPaint = Paint().apply {
        color = Color.WHITE
        textSize = 50f
        textAlign = Paint.Align.CENTER
    }
    private val comboPaint = Paint().apply {
        color = Color.YELLOW
        textSize = 70f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val particlePaint = Paint().apply {
        color = Color.YELLOW
    }

    // ================== CONSTANTES ==================
    private var pipeW = 0
    private var pipeH = 0
    private var pipeHorizontalSpacing = 0f
    private var pipeGap = 0f

    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                start()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                pause()
            }
        })

        // Initialisation des bitmaps
        bgBmp = BitmapFactory.decodeResource(context.resources, R.drawable.bg)
        baseBmp = BitmapFactory.decodeResource(context.resources, R.drawable.base)
        pipeBmp = BitmapFactory.decodeResource(context.resources, R.drawable.pipe)
        pipeTopBmp = BitmapFactory.decodeResource(context.resources, R.drawable.pipe_top)
        spikeBmp = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.spike)
        } catch (e: Exception) {
            null
        }

        // Charger les frames de l'oiseau
        birdFrames.add(BitmapFactory.decodeResource(context.resources, R.drawable.bird_0))
        birdFrames.add(BitmapFactory.decodeResource(context.resources, R.drawable.bird_1))
        birdFrames.add(BitmapFactory.decodeResource(context.resources, R.drawable.bird_2))

        pipeW = pipeBmp.width
        pipeH = pipeBmp.height
        birdW = birdFrames[0].width
        birdH = birdFrames[0].height
        pipeGap = (birdH * 3.5f)
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
        sCombo = soundPool.load(context, R.raw.point, 1)

        resetGame()
    }

    // ================== INITIALISATION JEU ==================
    private fun resetGame() {
        birdX = screenW * 0.25f
        birdY = screenH / 2.5f
        birdVel = 0f
        birdRotation = 0f
        birdAngularVel = 0f

        score = 0
        coinsCount = 0
        distance = 0
        comboCounter = 0
        difficultyLevel = 1
        scrollSpeed = 6.5f
        elapsedTimeMs = 0L

        obstacles.clear()
        coins.clear()
        particles.clear()

        bestScore = GamePreferences.getBestScore()
        bestCoins = GamePreferences.getBestCoins()

        currentState = GameState.START

        generateInitialObstacles()
    }

    private fun generateInitialObstacles() {
        for (i in 0..5) {
            val centerY = Random.nextFloat() * (screenH * 0.5f) + screenH * 0.25f
            obstacles.add(
                Obstacle(
                    x = screenW + i * pipeHorizontalSpacing,
                    centerY = centerY,
                    type = when (Random.nextInt(4)) {
                        0 -> ObstacleType.PIPE
                        1 -> ObstacleType.SPIKE
                        2 -> ObstacleType.MOVING_PIPE
                        else -> ObstacleType.WIND_CURRENT
                    }
                )
            )
            coins.add(Coin(screenW + i * pipeHorizontalSpacing + pipeW / 2, centerY))
        }
    }

    // ================== JEU PRINCIPAL ==================
    override fun run() {
        while (playing) {
            val currentTime = System.currentTimeMillis()
            val deltaMs = currentTime - lastFrameTime
            lastFrameTime = currentTime

            if (deltaMs > 0) {
                updateGame(deltaMs)
                drawGame()
            }
        }
    }

    private fun updateGame(deltaMs: Long) {
        val f = deltaMs / 16.6f
        elapsedTimeMs += deltaMs

        if (currentState != GameState.GAME_OVER) {
            // Défilement décor
            baseX1 -= scrollSpeed * f
            baseX2 -= scrollSpeed * f
            if (baseX1 + screenW <= 0) baseX1 = baseX2 + screenW
            if (baseX2 + screenW <= 0) baseX2 = baseX1 + screenW

            bgX1 -= (scrollSpeed / 4) * f
            bgX2 -= (scrollSpeed / 4) * f
            if (bgX1 + screenW <= 0) bgX1 = bgX2 + screenW
            if (bgX2 + screenW <= 0) bgX2 = baseX1 + screenW

            // Animation ailes
            birdAnimTimer += deltaMs
            if (birdAnimTimer >= birdAnimFrameMs) {
                birdAnimIndex = (birdAnimIndex + 1) % birdFrames.size
                birdAnimTimer = 0
            }
        } else {
            // Menu flottant
            menuBob += 0.05f
        }

        if (currentState == GameState.PLAYING) {
            // Physique oiseau
            birdVel = min(birdVel + gravity * f, maxFallSpeed)
            birdY += birdVel * f

            // Rotation oiseau
            birdAngularVel = birdVel / 5f
            birdRotation = min(birdAngularVel * 10f, 90f)

            // Shake sur collision
            if (isShaking) {
                shakeIntensity -= 0.1f * f
                if (shakeIntensity <= 0) isShaking = false
                birdX += (Random.nextFloat() - 0.5f) * shakeIntensity * 10
            }

            // Obstacles
            for (obs in obstacles) {
                obs.x -= scrollSpeed * f

                // Animation obstacles mobiles
                if (obs.type == ObstacleType.MOVING_PIPE) {
                    obs.animOffset = sin((elapsedTimeMs / 500.0).toFloat()) * 50
                }

                if (!obs.passed && obs.x < birdX) {
                    obs.passed = true
                    score++
                    distance++
                    soundPool.play(sPoint, 1f, 1f, 0, 0, 1f)

                    // Difficulté croissante
                    if (score % 10 == 0) {
                        difficultyLevel++
                        scrollSpeed += scrollAcceleration * 100f
                    }
                }

                // Générer coins
                if (!obs.coinCollected && obs.x - screenW < birdX && obs.x > 0) {
                    if (Random.nextFloat() > 0.7f) {
                        coins.add(
                            Coin(
                                x = obs.x + pipeW / 2f,
                                y = obs.centerY
                            )
                        )
                        obs.coinCollected = true
                    }
                }
            }

            // Collecter coins
            for (coin in coins) {
                if (!coin.collected) {
                    val distX = coin.x - (birdX + birdW / 2)
                    val distY = coin.y - (birdY + birdH / 2)
                    val dist = sqrt(distX * distX + distY * distY)

                    if (dist < 50) {
                        coin.collected = true
                        coinsCount += 10
                        comboCounter++
                        soundPool.play(sCombo, 1f, 1f, 0, 0, 1f)

                        // Particules
                        repeat(5) {
                            particles.add(
                                Particle(
                                    x = coin.x,
                                    y = coin.y,
                                    vx = Random.nextFloat() * 200 - 100,
                                    vy = -Random.nextFloat() * 300,
                                    color = Color.YELLOW,
                                    size = 8f
                                )
                            )
                        }
                    }
                }
            }

            // Mettre à jour particules
            particles.removeAll { p ->
                p.x += p.vx * (deltaMs / 1000f)
                p.y += p.vy * (deltaMs / 1000f)
                p.vy += gravity * (deltaMs / 1000f)
                p.life -= 0.02f
                p.life <= 0
            }

            // Vérifier collisions
            for (obs in obstacles) {
                val topPipeY = obs.centerY - pipeGap / 2
                val bottomPipeY = obs.centerY + pipeGap / 2

                if (obs.x < birdX + birdW && obs.x + pipeW > birdX) {
                    if (birdY < topPipeY || birdY + birdH > bottomPipeY) {
                        triggerGameOver()
                    }
                }
            }

            // Collision base
            if (birdY + birdH >= screenH - baseBmp.height) {
                triggerGameOver()
            }

            // Supprimer obstacles passés
            obstacles.removeAll { it.x < -pipeW }

            // Générer nouveaux obstacles
            if (obstacles.isEmpty() || obstacles.last().x < screenW * 1.5f) {
                val centerY = Random.nextFloat() * (screenH * 0.5f) + screenH * 0.25f
                obstacles.add(
                    Obstacle(
                        x = (obstacles.lastOrNull()?.x ?: screenW.toFloat()) + pipeHorizontalSpacing,
                        centerY = centerY,
                        type = when (Random.nextInt(4)) {
                            0 -> ObstacleType.PIPE
                            1 -> ObstacleType.SPIKE
                            2 -> ObstacleType.MOVING_PIPE
                            else -> ObstacleType.WIND_CURRENT
                        }
                    )
                )
            }
        }
    }

    private fun drawGame() {
        val holder = surfaceHolder
        val canvas: Canvas?
        try {
            canvas = holder.lockCanvas()
            if (canvas != null) {
                drawFrame(canvas)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun drawFrame(canvas: Canvas) {
        // Fond
        canvas.drawBitmap(bgBmp, bgX1, 0f, paint)
        canvas.drawBitmap(bgBmp, bgX2, 0f, paint)

        // Obstacles
        for (obs in obstacles) {
            when (obs.type) {
                ObstacleType.PIPE -> {
                    val topPipeY = obs.centerY - pipeGap / 2 - pipeH
                    val bottomPipeY = obs.centerY + pipeGap / 2
                    canvas.drawBitmap(pipeTopBmp, obs.x, topPipeY, paint)
                    canvas.drawBitmap(pipeBmp, obs.x, bottomPipeY, paint)
                }
                ObstacleType.SPIKE -> {
                    spikeBmp?.let {
                        val topY = obs.centerY - pipeGap / 2 - 50
                        val bottomY = obs.centerY + pipeGap / 2 + 30
                        canvas.drawBitmap(it, obs.x + pipeW / 2 - it.width / 2, topY, paint)
                        canvas.drawBitmap(it, obs.x + pipeW / 2 - it.width / 2, bottomY, paint)
                    }
                }
                ObstacleType.MOVING_PIPE -> {
                    val adjustedY = obs.animOffset
                    val topPipeY = obs.centerY - pipeGap / 2 - pipeH + adjustedY
                    val bottomPipeY = obs.centerY + pipeGap / 2 + adjustedY
                    canvas.drawBitmap(pipeTopBmp, obs.x, topPipeY, paint)
                    canvas.drawBitmap(pipeBmp, obs.x, bottomPipeY, paint)
                }
                ObstacleType.WIND_CURRENT -> {
                    drawWindEffect(canvas, obs.x)
                }
            }
        }

        // Coins
        for (coin in coins) {
            if (!coin.collected) {
                val scaleFactor = 1f + (sin((elapsedTimeMs / 500.0).toFloat()) * 0.1f)
                canvas.drawCircle(coin.x, coin.y, 15f * scaleFactor, Paint().apply { color = Color.YELLOW })
            }
        }

        // Particules
        for (p in particles) {
            particlePaint.color = p.color
            particlePaint.alpha = (p.life * 255).toInt()
            canvas.drawCircle(p.x, p.y, p.size, particlePaint)
        }

        // Base
        canvas.drawBitmap(baseBmp, baseX1, screenH - baseBmp.height.toFloat(), paint)
        canvas.drawBitmap(baseBmp, baseX2, screenH - baseBmp.height.toFloat(), paint)

        // Oiseau avec rotation
        canvas.save()
        canvas.rotate(birdRotation, birdX + birdW / 2f, birdY + birdH / 2f)
        canvas.drawBitmap(birdFrames[birdAnimIndex], birdX, birdY, paint)
        canvas.restore()

        // HUD
        when (currentState) {
            GameState.START -> {
                drawStartScreen(canvas)
            }
            GameState.PLAYING -> {
                drawGameHUD(canvas)
            }
            GameState.PAUSED -> {
                canvas.drawARGB(100, 0, 0, 0)
                canvas.drawText("JEU EN PAUSE", screenW / 2f, screenH * 0.45f, scorePaint)
                canvas.drawText("Appuyez pour continuer", screenW / 2f, screenH * 0.6f, hudPaint)
            }
            GameState.GAME_OVER -> {
                drawGameOverScreen(canvas)
            }
        }

        holder.unlockCanvasAndPost(canvas)
    }

    private fun drawStartScreen(canvas: Canvas) {
        canvas.drawARGB(100, 0, 0, 0)
        canvas.drawText("FLAPPY COIN", screenW / 2f, screenH * 0.25f, scorePaint)
        canvas.drawText("TAP TO START", screenW / 2f, screenH * 0.45f, hudPaint)
        canvas.drawText("Best: $bestScore | 🪙 $bestCoins", screenW / 2f, screenH * 0.7f,
            Paint().apply { textSize = 40f; color = Color.WHITE; textAlign = Paint.Align.CENTER })
    }

    private fun drawGameHUD(canvas: Canvas) {
        canvas.drawText(score.toString(), screenW / 2f, screenH * 0.12f, scorePaint)
        canvas.drawText("🪙 $coinsCount", 80f, 100f, hudPaint)

        // Affichage difficulté
        val diffPaint = Paint().apply {
            color = Color.RED
            textSize = 40f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("Lvl $difficultyLevel", screenW - 50f, 100f, diffPaint)

        // Combo
        if (comboCounter > 0) {
            comboPaint.alpha = 200
            canvas.drawText("COMBO x$comboCounter", screenW / 2f, screenH / 2f, comboPaint)
        }
    }

    private fun drawGameOverScreen(canvas: Canvas) {
        canvas.drawARGB(180, 0, 0, 0)
        canvas.drawText("GAME OVER", screenW / 2f, screenH * 0.30f, scorePaint)
        canvas.drawText("Score: $score", screenW / 2f, screenH * 0.45f, hudPaint)
        canvas.drawText("🪙 Coins: $coinsCount", screenW / 2f, screenH * 0.52f, hudPaint)
        canvas.drawText("Best: $bestScore", screenW / 2f, screenH * 0.59f, hudPaint)

        if (System.currentTimeMillis() - gameOverTime > 1000) {
            canvas.drawText("TAP TO RESTART", screenW / 2f, screenH * 0.72f, hudPaint)
        }
    }

    private fun drawWindEffect(canvas: Canvas, x: Float) {
        val windPaint = Paint().apply {
            color = Color.CYAN
            alpha = 100
            strokeWidth = 2f
        }
        repeat(3) { i ->
            val offset = (elapsedTimeMs / 300.0).toFloat() + (i * 0.3f)
            canvas.drawLine(x - 50 + offset * 100, screenH / 2f + (i * 30).toFloat(),
                x + 50 + offset * 100, screenH / 2f + (i * 30).toFloat(), windPaint)
        }
    }

    private fun drawWindWave(canvas: Canvas, x: Float, baseY: Float) {
        val waveY = baseY + (sin((elapsedTimeMs / 500.0 + x / 100).toDouble()).toFloat()) * 20
        val wavePaint = Paint().apply {
            color = Color.CYAN
            alpha = 80
            strokeWidth = 2f
        }
        canvas.drawCircle(x + pipeW / 2, waveY, 30f, wavePaint)
    }

    // ================== GAME OVER ==================
    private fun triggerGameOver() {
        if (currentState != GameState.GAME_OVER) {
            currentState = GameState.GAME_OVER
            gameOverTime = System.currentTimeMillis()
            triggerShake()
            soundPool.play(sHit, 1f, 1f, 0, 0, 1f)
            soundPool.play(sDie, 1f, 1f, 0, 0, 1f)
            GamePreferences.incrementGames()

            // Appeler le callback avec les stats du jeu
            onGameOver(score, coinsCount, distance, elapsedTimeMs)
        }
    }

    // ================== ENTRÉES ==================
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            when (currentState) {
                GameState.START -> currentState = GameState.PLAYING
                GameState.PLAYING -> {
                    birdVel = jumpImpulse
                    soundPool.play(sWing, 0.5f, 0.5f, 0, 0, 1f)
                    spawnJumpParticles()
                }
                GameState.PAUSED -> currentState = GameState.PLAYING
                GameState.GAME_OVER -> {
                    if (System.currentTimeMillis() - gameOverTime > 1000) {
                        resetGame()
                    }
                }
            }
        }
        return true
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

    // ================== CONTRÔLE CYCLE DE VIE ==================
    fun start() {
        if (!playing) {
            playing = true
            lastFrameTime = System.currentTimeMillis()
            gameThread = Thread(this).apply { start() }
        }
    }

    fun pause() {
        playing = false
        gameThread?.join()
    }

    fun resume() {
        if (!playing) {
            playing = true
            lastFrameTime = System.currentTimeMillis()
            gameThread = Thread(this).apply { start() }
        }
    }

    // ✅ MÉTHODE REVIVE - Relancer le jeu après Game Over
    fun revive() {
        if (currentState == GameState.GAME_OVER) {
            resetGame()
            currentState = GameState.PLAYING
            playing = true
        }
    }

    private fun triggerShake() {
        if (GamePreferences.isVibrationEnabled()) {
            isShaking = true
            shakeIntensity = 5f
            val effect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator?.vibrate(effect)
        }
    }
}