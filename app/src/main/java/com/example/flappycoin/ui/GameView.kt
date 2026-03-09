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
import kotlin.random.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.cos

class GameView(context: Context) : SurfaceView(context), Runnable {

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
    private var birdW = 0
    private var birdH = 0
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
        var animOffset: Float = 0f // Pour animations
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

    // ================== SCORE & COMBO ==================
    private var score = 0
    private var coinsCount = 0
    private var bestScore = 0
    private var bestCoins = 0
    private var comboCounter = 0
    private var comboMultiplier = 1f
    private var lastCoinTime = 0L
    private val comboDuration = 5000L // 5 secondes pour maintenir le combo

    // ================== DIFFICULTÉ PROGRESSIVE ==================
    private var difficultyLevel = 1
    private var gapsCollected = 0
    private val gapsForDifficulty = 5 // Augmenter difficulté tous les 5 gaps

    // ================== AUDIO ==================
    private val soundPool: SoundPool
    private val sWing: Int
    private val sPoint: Int
    private val sHit: Int
    private val sDie: Int
    private val sCombo: Int

    // ================== PAINTS ==================
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
    private val comboPaint = Paint().apply {
        color = Color.RED
        textSize = 80f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val coinEmojiPaint = Paint().apply {
        textSize = 70f
        textAlign = Paint.Align.CENTER
    }
    private val particlePaint = Paint().apply {
        isAntiAlias = true
    }

    // ================== HITBOX RÉUTILISÉES ==================
    private val birdRect = RectF()
    private val topPipeRect = RectF()
    private val bottomPipeRect = RectF()
    private val coinRect = RectF()

    init {
        // Chargement des assets
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

        // Oiseau animé
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

        // Spike bitmap (optionnel)
        spikeBmp = try {
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.spike),
                (pipeW * 0.3f).toInt(),
                (pipeW * 0.3f).toInt(),
                true
            )
        } catch (e: Exception) {
            null
        }

        // Gap amélioré avec scaling de difficulté
        pipeGap = birdH * 3.5f
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
        sCombo = soundPool.load(context, R.raw.point, 1) // Peut être changé

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
        comboCounter = 0
        comboMultiplier = 1f
        difficultyLevel = 1
        gapsCollected = 0
        scrollSpeed = 6.5f
        elapsedTimeMs = 0L
        obstacles.clear()
        coins.clear()
        particles.clear()

        var startX = screenW * 1.2f
        for (i in 0..4) {
            val center = randomGapCenter()
            val obstacleType = selectObstacleType()
            obstacles.add(Obstacle(startX, center, obstacleType))
            coins.add(Coin(startX + pipeW / 2f, center))
            startX += pipeHorizontalSpacing
        }
        currentState = GameState.START
    }

    // ================== SÉLECTION OBSTACLE VARIÉ ==================
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

    // ================== GAME LOOP ==================
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
                Thread.sleep(targetFrameMs)
            } catch (_: Exception) {
            }
        }
    }

    // ================== MISE À JOUR LOGIQUE ==================
    private fun update(deltaMs: Long) {
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

            // Combo timeout
            if (System.currentTimeMillis() - lastCoinTime > comboDuration) {
                comboCounter = 0
                comboMultiplier = 1f
            }
        }

        when (currentState) {
            GameState.START -> {
                // Menu flottant
                menuBob += 0.08f * f
                birdY = (screenH / 2.5f) + (sin(menuBob.toDouble()) * 20).toFloat()
                birdVel = 0f
            }

            GameState.PLAYING -> {
                // Physique améliorée
                gravity = 0.6f + (difficultyLevel * 0.05f) // Gravité qui augmente
                birdVel += gravity * f
                birdVel = min(birdVel, maxFallSpeed)
                birdVel = max(birdVel, maxRiseSpeed)
                birdY += birdVel * f

                // Rotation réaliste améliorée
                birdAngularVel = birdVel * 0.15f
                birdRotation += birdAngularVel * f
                birdRotation = birdRotation.coerceIn(-25f, 90f)

                // Shake sur collision
                if (isShaking) {
                    shakeIntensity -= 0.1f * f
                    if (shakeIntensity <= 0) isShaking = false
                    birdX += (Random.nextFloat() - 0.5f) * shakeIntensity * 10
                }

                // Collision Sol/Plafond
                val groundY = screenH - baseBmp.height.toFloat()
                if (birdY + birdH > groundY) {
                    birdY = groundY - birdH
                    triggerGameOver()
                }
                if (birdY < 0) {
                    birdY = 0f
                    triggerGameOver()
                }

                // Hitbox précise
                birdRect.set(birdX + 5, birdY + 5, birdX + birdW - 5, birdY + birdH - 5)

                // Gestion Obstacles avec animations
                val itObstacle = obstacles.iterator()
                var newObstacleX = 0f
                while (itObstacle.hasNext()) {
                    val o = itObstacle.next()
                    o.x -= scrollSpeed * f

                    // Animation des obstacles mobiles
                    if (o.type == ObstacleType.MOVING_PIPE) {
                        o.animOffset = sin((elapsedTimeMs / 500.0).toFloat()) * 50
                        o.centerY += o.animOffset * f
                    }

                    // Hitbox collision
                    topPipeRect.set(o.x, 0f, o.x + pipeW, o.centerY - pipeGap / 2)
                    bottomPipeRect.set(o.x, o.centerY + pipeGap / 2, o.x + pipeW, screenH.toFloat())

                    if (RectF.intersects(birdRect, topPipeRect) || RectF.intersects(birdRect, bottomPipeRect)) {
                        triggerGameOver()
                    }

                    // Scoring
                    if (!o.passed && o.x + pipeW < birdX) {
                        o.passed = true
                        score += (10 * comboMultiplier).toInt()
                        soundPool.play(sPoint, 0.7f, 0.7f, 0, 0, 1f)
                        gapsCollected++
                        
                        // Augmentation de difficulté
                        if (gapsCollected % gapsForDifficulty == 0) {
                            increaseDifficulty()
                        }
                    }

                    if (o.x + pipeW < 0) {
                        itObstacle.remove()
                        newObstacleX = obstacles.last().x + pipeHorizontalSpacing
                    }
                }

                // Créer nouveaux obstacles
                if (newObstacleX > 0) {
                    val c = randomGapCenter()
                    val type = selectObstacleType()
                    obstacles.add(Obstacle(newObstacleX, c, type))
                    coins.add(Coin(newObstacleX + pipeW / 2, c))
                }

                // Gestion Pièces
                val itCoin = coins.iterator()
                while (itCoin.hasNext()) {
                    val c = itCoin.next()
                    c.x -= scrollSpeed * f
                    
                    // Animation pièce
                    c.scale = 1f + sin((elapsedTimeMs / 300.0).toFloat()) * 0.15f

                    if (!c.collected) {
                        coinRect.set(c.x - 25, c.y - 25, c.x + 25, c.y + 25)
                        if (RectF.intersects(birdRect, coinRect)) {
                            c.collected = true
                            coinsCount += (10 * comboMultiplier).toInt()
                            comboCounter++
                            comboMultiplier = 1f + (comboCounter * 0.1f).coerceAtMost(5f)
                            lastCoinTime = System.currentTimeMillis()
                            
                            soundPool.play(sCombo, 0.9f, 0.9f, 0, 0, 1.8f)
                            spawnCoinParticles(c.x, c.y)
                        }
                    }

                    if (c.x + 50 < 0) itCoin.remove()
                }

                // Mise à jour particules
                val itParticle = particles.iterator()
                while (itParticle.hasNext()) {
                    val p = itParticle.next()
                    p.x += p.vx * f
                    p.y += p.vy * f
                    p.vy += 0.3f * f // Gravité particules
                    p.life -= 0.02f * f
                    if (p.life <= 0) itParticle.remove()
                }

                // Accélération progressive
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

                if (score > bestScore) bestScore = score
                if (coinsCount > bestCoins) bestCoins = coinsCount
                
                GamePreferences.setBestScore(bestScore)
            }

            else -> {}
        }
    }

    // ================== AUGMENTATION DIFFICULTÉ ==================
    private fun increaseDifficulty() {
        difficultyLevel++
        
        // Augmenter la difficulté progressivement
        pipeGap * 0.97f // Gaps plus petits
        gravity += 0.05f
        jumpImpulse -= 0.5f
        
        playAscendingSound()
    }

    private fun playAscendingSound() {
        soundPool.play(sPoint, 0.5f, 0.5f, 0, 0, 0.8f)
    }

    // ================== EFFETS VISUELS ==================
    private fun spawnCoinParticles(x: Float, y: Float) {
        repeat(8) {
            val angle = (it * 45).toFloat() * (Math.PI / 180f).toFloat()
            val vx = cos(angle.toDouble()).toFloat() * 200
            val vy = sin(angle.toDouble()).toFloat() * 200
            particles.add(Particle(
                x = x,
                y = y,
                vx = vx,
                vy = vy,
                color = Color.YELLOW,
                size = 8f
            ))
        }
    }

    private fun triggerShake() {
        isShaking = true
        shakeIntensity = 5f
        vibrate(100)
    }

    private fun vibrate(ms: Long) {
        vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    // ================== DESSIN ==================
    private fun draw() {
        if (!holder.surface.isValid) return
        val canvas = holder.lockCanvas() ?: return

        // Fond parallaxe
        canvas.drawBitmap(bgBmp, bgX1, 0f, paint)
        canvas.drawBitmap(bgBmp, bgX2, 0f, paint)

        // Obstacles avec effets
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
                    // Effet de vent
                    drawWindEffect(canvas, o.x)
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
                    repeat(5) { i ->
                        drawWindWave(canvas, o.x, (i * 100).toFloat())
                    }
                }
            }
        }

        // Pièces avec animation
        for (c in coins) {
            if (!c.collected) {
                canvas.save()
                canvas.scale(c.scale, c.scale, c.x, c.y)
                canvas.drawText("🪙", c.x, c.y + 20, coinEmojiPaint)
                canvas.restore()
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
            GameState.GAME_OVER -> {
                drawGameOverScreen(canvas)
            }
            else -> {}
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
            strokeWidth = 3f
        }
        repeat(3) { i ->
            val offset = (elapsedTimeMs / 300.0).toFloat() + (i * 0.3f)
            canvas.drawLine(x - 50 + offset * 100, screenH / 2f + (i * 30).toFloat(),
                x + 50 + offset * 100, screenH / 2f + (i * 30).toFloat(), windPaint)
        }
    }

    private fun drawWindWave(canvas: Canvas, x: Float, baseY: Float) {
        val waveY = baseY + sin((elapsedTimeMs / 500.0 + x / 100).toDouble()) * 20
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

    // ================== PAUSE/RESUME ==================
    fun pause() {
        playing = false
        gameThread?.join()
    }

    fun resume() {
        playing = true
        lastFrameTime = System.currentTimeMillis()
        gameThread = Thread(this).apply { start() }
    }
}