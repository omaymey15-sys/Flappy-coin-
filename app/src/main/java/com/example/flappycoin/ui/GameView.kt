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

        // Spike optionnel
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

        // Gaps
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

        resetGame()
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

    fun revive() {
        currentState = GameState.PLAYING
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
        val f = deltaMs / 16.6f
        elapsedTimeMs += deltaMs

        if (currentState != GameState.GAME_OVER) {
            // Défilement
            baseX1 -= scrollSpeed * f
            baseX2 -= scrollSpeed * f
            if (baseX1 + screenW <= 0) baseX1 = baseX2 + screenW
            if (baseX2 + screenW <= 0) baseX2 = baseX1 + screenW

            bgX1 -= (scrollSpeed / 4) * f
            bgX2 -= (scrollSpeed / 4) * f
            if (bgX1 + screenW <= 0) bgX1 = bgX2 + screenW
            if (bgX2 + screenW <= 0) bgX2 = baseX1 + screenW

            // Animation
            birdAnimTimer += deltaMs
            if (birdAnimTimer >= birdAnimFrameMs) {
                birdAnimIndex = (birdAnimIndex + 1) % birdFrames.size
                birdAnimTimer = 0
            }

            // Combo timeout
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

                // Collision sol/plafond
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

                // Obstacles
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
                        score += (10 * comboMultiplier).toInt()
                        soundPool.play(sPoint, 0.7f, 0.7f, 0, 0, 1f)
                        gapsCollected++

                        if (gapsCollected % gapsForDifficulty == 0) {
                            increaseDifficulty()
                        }
                    }

                    if (o.x + pipeW < 0) {
                        itObstacle.remove()
                        newObstacleX = obstacles.last().x + pipeHorizontalSpacing
                    }
                }

                if (newObstacleX > 0) {
                    val c = randomGapCenter()
                    val type = selectObstacleType()
                    obstacles.add(Obstacle(newObstacleX, c, type))
                    coins.add(Coin(newObstacleX + pipeW / 2, c))
                }

                // Coins
                val itCoin = coins.iterator()
                while (itCoin.hasNext()) {
                    val c = itCoin.next()
                    c.x -= scrollSpeed * f
                    c.scale = 1f + sin((elapsedTimeMs / 300.0).toFloat()) * 0.15f

                    if (!c.collected) {
                        coinRect.set(c.x - 25, c.y - 25, c.x + 25, c.y + 25)
                        if (RectF.intersects(birdRect, coinRect)) {
                            c.collected = true
                            coinsCount += (10 * comboMultiplier).toInt()
                            comboCounter++
                            comboMultiplier = 1f + (comboCounter * 0.1f).coerceAtMost(5f)
                            lastCoinTime = System.currentTimeMillis()
                            soundPool.play(sPoint, 0.9f, 0.9f, 0, 0, 1.8f)
                            spawnCoinParticles(c.x, c.y)
                        }
                    }

                    if (c.x + 50 < 0) itCoin.remove()
                }

                // Particules
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

        // Fond
        canvas.drawBitmap(bgBmp, bgX1, 0f, paint)
        canvas.drawBitmap(bgBmp, bgX2, 0f, paint)

        // Obstacles
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

        // Coins
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

        // Oiseau
        canvas.save()
        canvas.rotate(birdRotation, birdX + birdW / 2f, birdY + birdH / 2f)
        canvas.drawBitmap(birdFrames[birdAnimIndex], birdX, birdY, paint)
        canvas.restore()

        // HUD
        when (currentState) {
            GameState.START -> {
                canvas.drawARGB(100, 0, 0, 0)
                canvas.drawText("FLAPPY COIN", screenW / 2f, screenH * 0.25f, scorePaint)
                canvas.drawText("TAP TO START", screenW / 2f, screenH * 0.45f, hudPaint)
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

    private fun drawGameHUD(canvas: Canvas) {
        val timeSeconds = elapsedTimeMs / 1000
        val timeMinutes = timeSeconds / 60
        val timeSecs = timeSeconds % 60

        // Carré haut gauche avec stats
        canvas.drawRect(8f, 8f, screenW / 2.5f, 120f, cardPaint)

        canvas.drawText(score.toString(), 40f, 45f, statsHudPaint)
        canvas.drawText("${distance / 10}m", 40f, 75f, smallTextPaint)
        canvas.drawText(String.format("%02d:%02d", timeMinutes, timeSecs), 40f, 105f, smallTextPaint)

        // Coins haut droit
        canvas.drawRect(screenW - 150f, 8f, screenW - 8f, 80f, cardPaint)
        canvas.drawText("🪙", screenW - 80f, 45f, hudPaint)
        canvas.drawText(coinsCount.toString(), screenW - 80f, 75f, statsHudPaint)
    }

    private fun drawGameOverScreen(canvas: Canvas) {
        canvas.drawARGB(180, 0, 0, 0)
        canvas.drawText("GAME OVER", screenW / 2f, screenH * 0.30f, scorePaint)
        
        val timeSeconds = elapsedTimeMs / 1000
        canvas.drawText(
            "Score: $score | 🪙 $coinsCount | ${distance / 10}m | ${timeSeconds}s",
            screenW / 2f,
            screenH * 0.50f,
            hudPaint
        )

        if (System.currentTimeMillis() - gameOverTime > 1000) {
            canvas.drawText("TAP FOR OPTIONS", screenW / 2f, screenH * 0.72f, smallTextPaint)
        }
    }

    private fun triggerGameOver() {
        if (currentState != GameState.GAME_OVER) {
            currentState = GameState.GAME_OVER
            gameOverTime = System.currentTimeMillis()
            triggerShake()
            soundPool.play(sHit, 1f, 1f, 0, 0, 1f)
            soundPool.play(sDie, 1f, 1f, 0, 0, 1f)

            // Callback avec stats
            onGameOver(score, coinsCount, distance / 10, elapsedTimeMs)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.