package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class BattleSimulation(
    var arenaWidth: Float = 1000f,
    var arenaHeight: Float = 1500f,
    val playerBall: CombatBall,
    val opponentBall: CombatBall,
    var engineSettings: EngineSettings,
    val onCollisionImpact: (isPlayerAttacker: Boolean, damage: Int, isCrit: Boolean, passiveLabel: String) -> Unit,
    val onWallBounce: (ball: CombatBall) -> Unit,
    val onKnockout: (isPlayerWinner: Boolean) -> Unit
) {
    val particles = ArrayList<Particle>(64)
    val shockwaves = ArrayList<ShockwaveRing>(12)
    val floatingTexts = ArrayList<FloatingCombatText>(12)
    val fireHazards = ArrayList<FireHazardZone>(10)

    private var nextEntityId = 1L

    var globalTime: Float = 0f
    var isRoundActive: Boolean = true
    var isClashStarted: Boolean = false
    var isArenaInitialized: Boolean = false
    var screenShakeAmount: Float = 0f
    var slowMoScale: Float = 1.0f

    // Tactical mid-flight redirect cooldowns (seconds)
    var playerRedirectCooldown: Float = 0f
    var aiRedirectCooldown: Float = 2.4f
    val maxRedirectCooldown: Float = 3.0f

    // Wall impact flashes (0..1)
    var wallFlashLeft: Float = 0f
    var wallFlashRight: Float = 0f
    var wallFlashTop: Float = 0f
    var wallFlashBottom: Float = 0f

    private var collisionCooldown: Float = 0f
    private var kamadoTrailCooldown: Float = 0f

    val wallPadding: Float get() = max(16f, min(arenaWidth, arenaHeight) * 0.026f)
    val wallLeft: Float get() = wallPadding + playerBall.radius
    val wallRight: Float get() = arenaWidth - wallPadding - playerBall.radius
    val wallTop: Float get() = wallPadding + playerBall.radius
    val wallBottom: Float get() = arenaHeight - wallPadding - playerBall.radius

    val baseArenaSpeed: Float
        get() = hypot(arenaWidth, arenaHeight) * 0.54f

    fun configureArenaBounds(widthPx: Float, heightPx: Float) {
        if (widthPx < 100f || heightPx < 100f) return
        arenaWidth = widthPx
        arenaHeight = heightPx

        val scaledRadius = (min(widthPx, heightPx) * 0.072f).coerceIn(30f, 84f)
        playerBall.radius = scaledRadius
        opponentBall.radius = scaledRadius

        if (!isArenaInitialized) {
            isArenaInitialized = true
            playerBall.position = Vector2(widthPx * 0.5f, heightPx * 0.76f)
            opponentBall.position = Vector2(widthPx * 0.5f, heightPx * 0.24f)
            playerBall.velocity = Vector2.Zero
            opponentBall.velocity = Vector2.Zero
            playerBall.resumeDirection = Vector2(0.35f, -0.93f).normalized()
            opponentBall.resumeDirection = Vector2(-0.35f, 0.93f).normalized()
            initVerletHair(playerBall)
            initVerletHair(opponentBall)
        } else {
            playerBall.position = Vector2(
                playerBall.position.x.coerceIn(wallLeft, wallRight),
                playerBall.position.y.coerceIn(wallTop, wallBottom)
            )
            opponentBall.position = Vector2(
                opponentBall.position.x.coerceIn(wallLeft, wallRight),
                opponentBall.position.y.coerceIn(wallTop, wallBottom)
            )
        }
    }

    private fun initVerletHair(ball: CombatBall) {
        val seg = ball.radius * 0.32f
        ball.hairNodes = List(4) { i ->
            VerletHairNode(
                x = ball.position.x,
                y = ball.position.y - i * seg,
                oldX = ball.position.x,
                oldY = ball.position.y - i * seg
            )
        }
    }

    fun computeTargetCruiseSpeed(ball: CombatBall, difficultySpeedMod: Float = 1f): Float {
        var mult = ball.character.speedRating * difficultySpeedMod
        if (ball.speedBoostTimer > 0f) {
            mult *= ball.speedBoostMultiplier
        }
        if (ball.jinwooSpeedStacks > 0) {
            mult *= (1f + ball.jinwooSpeedStacks * 0.03f)
        }
        if (ball.isUltimateActive) {
            when (ball.character.id) {
                "kakarot_z" -> mult *= 1.30f
                "chainsaw_devil" -> mult *= 1.28f
                "kurosaki_hollow" -> mult *= 1.35f
                "killua_godspeed" -> mult *= 1.40f
                "raikiri_shinobi" -> mult *= (1f + min(6, ball.kamuiBounceStacks) * 0.08f)
            }
        }
        return baseArenaSpeed * mult.coerceIn(0.85f, 2.25f)
    }

    /**
     * Starts the nonstop clash: launches BOTH the player's sphere and the AI opponent's sphere
     * simultaneously. From this moment on, neither sphere ever stops moving until one hits 0 HP.
     */
    fun startNonstopClash(playerDirection: Vector2, difficulty: BotDifficulty) {
        val pDir = sanitizeLaunchAngle(
            if (playerDirection.length() > 0.01f) playerDirection.normalized()
            else Vector2(0.42f, -0.91f).normalized()
        )
        val aiDir = sanitizeLaunchAngle(computeAiInterceptDirection(difficulty))

        isClashStarted = true
        playerRedirectCooldown = 1.2f
        aiRedirectCooldown = difficulty.redirectCooldownSec * 0.75f

        applyImpulseLaunch(playerBall, pDir, 1.0f)
        applyImpulseLaunch(opponentBall, aiDir, difficulty.speedFactor)
    }

    /**
     * Direct launch helper (also activates nonstop mode for individual ball tests).
     */
    fun launchPlayer(direction: Vector2, powerRatio: Float = 1.0f) {
        val dir = sanitizeLaunchAngle(direction.normalized())
        if (dir.length() < 0.01f) return
        isClashStarted = true
        applyImpulseLaunch(playerBall, dir, powerRatio.coerceIn(0.75f, 1.25f))
    }

    fun launchOpponent(direction: Vector2, powerRatio: Float = 1.0f) {
        val dir = sanitizeLaunchAngle(direction.normalized())
        if (dir.length() < 0.01f) return
        isClashStarted = true
        applyImpulseLaunch(opponentBall, dir, powerRatio.coerceIn(0.75f, 1.25f))
    }

    /**
     * Mid-flight tactical redirect for Player or AI without ever stopping the ball.
     */
    fun redirectPlayerBall(direction: Vector2): Boolean {
        if (!isClashStarted || !isRoundActive || playerRedirectCooldown > 0f || playerBall.isStunned) {
            return false
        }
        val dir = sanitizeLaunchAngle(direction.normalized())
        if (dir.length() < 0.01f) return false

        playerRedirectCooldown = maxRedirectCooldown
        playerBall.speedBoostMultiplier = 1.26f
        playerBall.speedBoostTimer = 0.65f
        applyImpulseLaunch(playerBall, dir, 1.0f)
        spawnFloatingText("REDIRECT!", playerBall.position.x, playerBall.position.y - playerBall.radius * 1.15f, Color(0xFF38BDF8))
        return true
    }

    fun performAiTacticalStep(dt: Float, difficulty: BotDifficulty) {
        if (!isClashStarted || !isRoundActive) return

        // AI triggers Ultimate automatically as soon as gauge hits 100%
        if (opponentBall.ultimateGauge >= 100f && !opponentBall.isUltimateActive && !opponentBall.isStunned) {
            triggerUltimate(opponentBall)
        }

        aiRedirectCooldown = max(0f, aiRedirectCooldown - dt)
        if (aiRedirectCooldown <= 0f && !opponentBall.isStunned) {
            aiRedirectCooldown = difficulty.redirectCooldownSec
            val dir = sanitizeLaunchAngle(computeAiInterceptDirection(difficulty))
            opponentBall.speedBoostMultiplier = 1.18f
            opponentBall.speedBoostTimer = 0.55f
            applyImpulseLaunch(opponentBall, dir, difficulty.speedFactor)
        }
    }

    fun computeAiInterceptDirection(difficulty: BotDifficulty): Vector2 {
        val origin = opponentBall.position
        val leadPos = playerBall.position + (playerBall.velocity * 0.18f)
        val clampedLead = Vector2(
            leadPos.x.coerceIn(wallLeft, wallRight),
            leadPos.y.coerceIn(wallTop, wallBottom)
        )

        val useBank = Random.nextFloat() < 0.32f
        val rawDir = if (useBank) {
            val bounceLeft = Random.nextBoolean()
            val mirrorX = if (bounceLeft) (2f * wallLeft - clampedLead.x) else (2f * wallRight - clampedLead.x)
            (Vector2(mirrorX, clampedLead.y) - origin).normalized()
        } else {
            (clampedLead - origin).normalized()
        }

        val fallback = if (rawDir.length() < 0.01f) Vector2(0.45f, 0.89f).normalized() else rawDir
        val jitterMax = (1f - difficulty.aimAccuracy) * 0.35f
        val jitter = (Random.nextFloat() * 2f - 1f) * jitterMax
        return fallback.rotate(jitter).normalized()
    }

    private fun applyImpulseLaunch(ball: CombatBall, dir: Vector2, speedScale: Float) {
        val targetSpeed = computeTargetCruiseSpeed(ball, speedScale)
        ball.resumeDirection = dir
        ball.velocity = dir * targetSpeed
        ball.stretchFactor = 1.24f
        ball.squashFactor = 0.80f
        ball.deformAngle = atan2(dir.y, dir.x)
        ball.weaponSpinBurst = 12f
        spawnShockwave(ball.position.x, ball.position.y, ball.character.auraColor, ball.radius * 1.9f)
        spawnImpactBurst(ball.position.x, ball.position.y, ball.character.auraColor, engineSettings.renderMode.particleCountOnHit / 2)
    }

    private fun sanitizeLaunchAngle(dir: Vector2): Vector2 {
        if (dir.length() < 0.001f) return Vector2(0.5f, -0.866f)
        var vx = dir.x
        var vy = dir.y
        // Prevent dead-flat horizontal or vertical paths so spheres continually cross the court
        if (abs(vx) < 0.22f) vx = if (vx >= 0f) 0.24f else -0.24f
        if (abs(vy) < 0.22f) vy = if (vy >= 0f) 0.24f else -0.24f
        return Vector2(vx, vy).normalized()
    }

    fun triggerUltimate(ball: CombatBall) {
        if (ball.ultimateGauge < 100f || ball.isUltimateActive) return
        ball.ultimateGauge = 0f
        ball.isUltimateActive = true
        ball.ultimateTimeRemaining = ball.character.ultimateDurationSeconds
        ball.kamuiBounceStacks = 0
        ball.weaponSpinBurst = 20f

        val rival = if (ball === playerBall) opponentBall else playerBall

        when (ball.character.id) {
            "satoru_void" -> {
                if (rival.velocity.length() > 1f) rival.resumeDirection = rival.velocity.normalized()
                rival.velocity = Vector2.Zero
                rival.stunTimeRemaining = 1.5f
                spawnFloatingText("UNLIMITED VOID!", rival.position.x, rival.position.y - rival.radius * 1.25f, Color(0xFF818CF8), isCrit = true)
            }
            "kujo_star" -> {
                if (rival.velocity.length() > 1f) rival.resumeDirection = rival.velocity.normalized()
                rival.velocity = Vector2.Zero
                rival.stunTimeRemaining = 1.6f
                spawnFloatingText("THE WORLD • TIME STOP!", rival.position.x, rival.position.y - rival.radius * 1.25f, Color(0xFFC084FC), isCrit = true)
            }
            "caped_baldy" -> {
                ball.isSeriousActive = true
                spawnFloatingText("SERIOUS PUNCH ARMED!", ball.position.x, ball.position.y - ball.radius * 1.25f, Color(0xFFFACC15), isCrit = true)
            }
            "yeager_titan" -> {
                val delta = rival.position - ball.position
                val dist = delta.length().coerceAtLeast(1f)
                rival.currentHp = max(0f, rival.currentHp - 12f)
                rival.hitFlashTimer = 0.8f
                val pushDir = sanitizeLaunchAngle(delta / dist)
                rival.resumeDirection = pushDir
                if (!rival.isStunned) {
                    rival.velocity = pushDir * (computeTargetCruiseSpeed(rival) * 1.25f)
                }
                spawnFloatingText("-12 COLOSSUS STEAM!", rival.position.x, rival.position.y - rival.radius, Color(0xFFFB923C), isCrit = true)
            }
            "elric_fullmetal" -> {
                ball.currentHp = min(ball.maxHp, ball.currentHp + 12f)
                spawnFloatingText("+12 ALCHEMY REPAIR!", ball.position.x, ball.position.y - ball.radius * 1.25f, Color(0xFFFBBF24), isCrit = true)
            }
            "asta_grimoire" -> {
                if (rival.isUltimateActive) {
                    rival.isUltimateActive = false
                    rival.ultimateTimeRemaining = 0f
                    rival.isSeriousActive = false
                    spawnFloatingText("ULTIMATE NULLIFIED!", rival.position.x, rival.position.y - rival.radius * 1.3f, Color(0xFFEF4444), isCrit = true)
                } else {
                    spawnFloatingText("BLACK DIVIDER!", ball.position.x, ball.position.y - ball.radius * 1.25f, Color(0xFFEF4444), isCrit = true)
                }
            }
            else -> {
                spawnFloatingText(
                    "${ball.character.ultimateName.uppercase()}!",
                    ball.position.x,
                    ball.position.y - ball.radius * 1.25f,
                    ball.character.auraColor,
                    isCrit = true
                )
            }
        }

        spawnShockwave(ball.position.x, ball.position.y, ball.character.auraColor, ball.radius * 3.8f)
        spawnImpactBurst(ball.position.x, ball.position.y, ball.character.auraColor, engineSettings.renderMode.particleCountOnHit)
        screenShakeAmount = 18f
    }

    fun update(rawDt: Float) {
        val dt = (rawDt * slowMoScale).coerceIn(0.001f, 0.035f)
        globalTime += dt

        screenShakeAmount = max(0f, screenShakeAmount - rawDt * 45f)
        wallFlashLeft = max(0f, wallFlashLeft - rawDt * 3.8f)
        wallFlashRight = max(0f, wallFlashRight - rawDt * 3.8f)
        wallFlashTop = max(0f, wallFlashTop - rawDt * 3.8f)
        wallFlashBottom = max(0f, wallFlashBottom - rawDt * 3.8f)
        collisionCooldown = max(0f, collisionCooldown - dt)
        kamadoTrailCooldown = max(0f, kamadoTrailCooldown - dt)
        playerRedirectCooldown = max(0f, playerRedirectCooldown - rawDt)

        playerBall.delayedHp += (playerBall.currentHp - playerBall.delayedHp) * min(1f, rawDt * 5.5f)
        opponentBall.delayedHp += (opponentBall.currentHp - opponentBall.delayedHp) * min(1f, rawDt * 5.5f)
        playerBall.hitFlashTimer = max(0f, playerBall.hitFlashTimer - rawDt * 5.5f)
        opponentBall.hitFlashTimer = max(0f, opponentBall.hitFlashTimer - rawDt * 5.5f)

        if (!isRoundActive) {
            updateVfx(dt)
            return
        }

        if (isClashStarted) {
            updateUltimate(playerBall, dt)
            updateUltimate(opponentBall, dt)
            updateStatusEffects(playerBall, dt)
            updateStatusEffects(opponentBall, dt)
            updateHazards(dt)

            // 4 sub-steps for crisp tunnel-free high-velocity ricochets
            val subSteps = 4
            val subDt = dt / subSteps
            for (step in 0 until subSteps) {
                stepNonstopMovement(playerBall, subDt)
                stepNonstopMovement(opponentBall, subDt)
                checkWallCollision(playerBall)
                checkWallCollision(opponentBall)
                checkBallToBallCollision()
            }
        }

        updateProceduralVisuals(playerBall, dt)
        updateProceduralVisuals(opponentBall, dt)
        updateVfx(dt)

        checkKnockoutState()
    }

    private fun checkKnockoutState() {
        if (!isRoundActive) return
        if (playerBall.isDead) {
            isRoundActive = false
            slowMoScale = 0.25f
            screenShakeAmount = 26f
            spawnShockwave(playerBall.position.x, playerBall.position.y, Color(0xFFE5392D), playerBall.radius * 4.5f)
            spawnImpactBurst(playerBall.position.x, playerBall.position.y, Color(0xFFE5392D), 28)
            onKnockout(false)
        } else if (opponentBall.isDead) {
            isRoundActive = false
            slowMoScale = 0.25f
            screenShakeAmount = 26f
            spawnShockwave(opponentBall.position.x, opponentBall.position.y, Color(0xFFFACC15), opponentBall.radius * 4.5f)
            spawnImpactBurst(opponentBall.position.x, opponentBall.position.y, Color(0xFFFACC15), 28)
            onKnockout(true)
        }
    }

    /**
     * Perpetual Nonstop Ricochet Integrator:
     * Zero air friction. Continuously maintains each sphere's kinetic cruise velocity so
     * neither ball can ever decelerate or stall until one reaches 0 HP.
     */
    private fun stepNonstopMovement(ball: CombatBall, subDt: Float) {
        if (ball.isStunned) {
            ball.velocity = Vector2.Zero
            return
        }

        val targetSpeed = computeTargetCruiseSpeed(ball)
        val currentSpeed = ball.velocity.length()

        if (currentSpeed < 12f) {
            // Immediately resume along stored direction if recovering from stun or zero-vel collision
            val resume = sanitizeLaunchAngle(ball.resumeDirection)
            ball.velocity = resume * targetSpeed
        } else {
            ball.resumeDirection = ball.velocity / currentSpeed
            // Smoothly converge speed back to targetSpeed (keeps knockback surges lively while preventing any slowdown!)
            val regulatedSpeed = when {
                currentSpeed < targetSpeed -> targetSpeed
                currentSpeed > targetSpeed * 1.45f -> targetSpeed * 1.45f
                else -> currentSpeed + (targetSpeed - currentSpeed) * min(1f, subDt * 4.5f)
            }
            ball.velocity = ball.resumeDirection * regulatedSpeed
        }

        ball.position = ball.position + (ball.velocity * subDt)
    }

    private fun updateUltimate(ball: CombatBall, dt: Float) {
        if (ball.speedBoostTimer > 0f) {
            ball.speedBoostTimer = max(0f, ball.speedBoostTimer - dt)
            if (ball.speedBoostTimer <= 0f) {
                ball.speedBoostMultiplier = 1.0f
            }
        }

        if (!ball.isUltimateActive) return
        ball.ultimateTimeRemaining -= dt

        if (ball.character.id == "kamado_sun" && kamadoTrailCooldown <= 0f && !ball.isStunned) {
            kamadoTrailCooldown = 0.38f
            spawnFireHazard(ball.id, ball.position.x, ball.position.y, ball.radius * 0.82f, 3.4f)
        }

        if (ball.ultimateTimeRemaining <= 0f) {
            ball.isUltimateActive = false
            ball.isSeriousActive = false
            ball.kamuiBounceStacks = 0
        }
    }

    private fun updateStatusEffects(ball: CombatBall, dt: Float) {
        if (ball.stunTimeRemaining > 0f) {
            ball.stunTimeRemaining = max(0f, ball.stunTimeRemaining - dt)
            if (ball.stunTimeRemaining <= 0f) {
                val resume = sanitizeLaunchAngle(ball.resumeDirection)
                ball.velocity = resume * computeTargetCruiseSpeed(ball)
            } else {
                ball.velocity = Vector2.Zero
            }
        }

        if (ball.bleedTicksRemaining > 0) {
            ball.bleedTimer += dt
            if (ball.bleedTimer >= 0.5f) {
                ball.bleedTimer -= 0.5f
                ball.bleedTicksRemaining--
                ball.currentHp = max(0f, ball.currentHp - 2f)
                ball.hitFlashTimer = 0.45f
                spawnFloatingText("-2 BLEED", ball.position.x, ball.position.y - ball.radius, Color(0xFFEF4444))
            }
        }
    }

    private fun updateHazards(dt: Float) {
        val iter = fireHazards.iterator()
        while (iter.hasNext()) {
            val hazard = iter.next()
            hazard.life -= dt
            if (hazard.life <= 0f) {
                iter.remove()
                continue
            }
            applyHazardBurn(playerBall, hazard, dt)
            applyHazardBurn(opponentBall, hazard, dt)
        }
    }

    private fun applyHazardBurn(ball: CombatBall, hazard: FireHazardZone, dt: Float) {
        if (hazard.ownerId == ball.id) return
        val dx = ball.position.x - hazard.x
        val dy = ball.position.y - hazard.y
        val maxDist = ball.radius + hazard.radius * 0.78f
        if (dx * dx + dy * dy < maxDist * maxDist) {
            ball.currentHp = max(0f, ball.currentHp - 5.5f * dt)
        }
    }

    private fun checkWallCollision(ball: CombatBall) {
        var bouncedX = false
        var bouncedY = false
        var normal = Vector2.Zero

        if (ball.position.x <= wallLeft) {
            ball.position = Vector2(wallLeft, ball.position.y)
            ball.velocity = Vector2(abs(ball.velocity.x), ball.velocity.y)
            bouncedX = true
            normal = Vector2(1f, 0f)
            wallFlashLeft = 1f
        } else if (ball.position.x >= wallRight) {
            ball.position = Vector2(wallRight, ball.position.y)
            ball.velocity = Vector2(-abs(ball.velocity.x), ball.velocity.y)
            bouncedX = true
            normal = Vector2(-1f, 0f)
            wallFlashRight = 1f
        }

        if (ball.position.y <= wallTop) {
            ball.position = Vector2(ball.position.x, wallTop)
            ball.velocity = Vector2(ball.velocity.x, abs(ball.velocity.y))
            bouncedY = true
            normal = Vector2(0f, 1f)
            wallFlashTop = 1f
        } else if (ball.position.y >= wallBottom) {
            ball.position = Vector2(ball.position.x, wallBottom)
            ball.velocity = Vector2(ball.velocity.x, -abs(ball.velocity.y))
            bouncedY = true
            normal = Vector2(0f, -1f)
            wallFlashBottom = 1f
        }

        if (bouncedX || bouncedY) {
            ball.wallBounceCombo++
            ball.addUltimate(3.5f)

            // Character wall-bounce passives & ultimates
            if (ball.character.id == "mugiwara_joy") {
                ball.speedBoostMultiplier = 1.18f
                ball.speedBoostTimer = 0.75f
                if (ball.isUltimateActive) {
                    spawnShockwave(ball.position.x, ball.position.y, Color.White, ball.radius * 2.1f)
                }
            }
            if (ball.isUltimateActive && ball.character.id == "raikiri_shinobi") {
                ball.kamuiBounceStacks = min(6, ball.kamuiBounceStacks + 1)
            }
            if (ball.isUltimateActive && ball.character.id == "dragneel_ignis") {
                spawnFireHazard(ball.id, ball.position.x, ball.position.y, ball.radius * 0.85f, 3.2f)
            }

            // Prevent boring wall-hugging loops: guarantee healthy inward cross-court angle + slight rival magnetism
            val speed = max(ball.velocity.length(), computeTargetCruiseSpeed(ball))
            val rival = if (ball === playerBall) opponentBall else playerBall
            val toRival = (rival.position - ball.position).normalized()
            val blendedDir = sanitizeLaunchAngle((ball.velocity.normalized() * 0.88f + toRival * 0.12f).normalized())

            // Make sure the ball still moves away from the wall it just touched!
            val finalVx = when {
                ball.position.x <= wallLeft + 1f -> abs(blendedDir.x)
                ball.position.x >= wallRight - 1f -> -abs(blendedDir.x)
                else -> blendedDir.x
            }
            val finalVy = when {
                ball.position.y <= wallTop + 1f -> abs(blendedDir.y)
                ball.position.y >= wallBottom - 1f -> -abs(blendedDir.y)
                else -> blendedDir.y
            }
            val cleanDir = Vector2(finalVx, finalVy).normalized()
            ball.resumeDirection = cleanDir
            ball.velocity = cleanDir * speed

            ball.squashFactor = 0.72f
            ball.stretchFactor = 1.22f
            ball.deformAngle = atan2(normal.y, normal.x)

            spawnImpactBurst(
                ball.position.x - normal.x * ball.radius * 0.75f,
                ball.position.y - normal.y * ball.radius * 0.75f,
                ball.character.auraColor,
                max(3, engineSettings.renderMode.particleCountOnHit / 3)
            )
            onWallBounce(ball)
        }
    }

    private fun checkBallToBallCollision() {
        val delta = opponentBall.position - playerBall.position
        val dist = delta.length()
        // Roronoa's triple orbiting katanas grant slightly wider effective strike contact
        val reachBonus = if (playerBall.character.id == "roronoa_ashura" || opponentBall.character.id == "roronoa_ashura") 6f else 0f
        val minDist = playerBall.radius + opponentBall.radius + reachBonus

        if (dist >= minDist || dist < 0.0001f) return

        val normal = delta / dist

        // Resolve overlap cleanly
        val overlap = minDist - dist
        playerBall.position = Vector2(
            (playerBall.position.x - normal.x * overlap * 0.52f).coerceIn(wallLeft, wallRight),
            (playerBall.position.y - normal.y * overlap * 0.52f).coerceIn(wallTop, wallBottom)
        )
        opponentBall.position = Vector2(
            (opponentBall.position.x + normal.x * overlap * 0.52f).coerceIn(wallLeft, wallRight),
            (opponentBall.position.y + normal.y * overlap * 0.52f).coerceIn(wallTop, wallBottom)
        )

        val relVel = opponentBall.velocity - playerBall.velocity
        val velAlongNormal = relVel.dot(normal)
        if (velAlongNormal > 0f && collisionCooldown > 0f) return

        val pSpeed = playerBall.velocity.length()
        val oSpeed = opponentBall.velocity.length()
        val isPlayerAttacker = pSpeed >= oSpeed
        val attacker = if (isPlayerAttacker) playerBall else opponentBall
        val defender = if (isPlayerAttacker) opponentBall else playerBall

        val m1 = playerBall.character.massRating
        val m2 = opponentBall.character.massRating

        // Elastic momentum exchange + guaranteed nonstop rebound speeds
        val effectiveRel = min(velAlongNormal, -baseArenaSpeed * 0.45f)
        var impulseMag = -2.0f * effectiveRel / ((1f / m1) + (1f / m2))
        if (attacker.character.id == "kakarot_z") {
            impulseMag *= 1.40f
        }
        val impulseVec = normal * impulseMag

        if (!playerBall.isStunned) {
            val rawV1 = if (playerBall.character.id == "yeager_titan" && !isPlayerAttacker) {
                playerBall.velocity - (impulseVec / (m1 * 1.8f))
            } else {
                playerBall.velocity - (impulseVec / m1)
            }
            val dir1 = sanitizeLaunchAngle(if (rawV1.length() > 5f) rawV1.normalized() else (normal * -1f))
            val boost1 = if (!isPlayerAttacker && attacker.character.id == "kakarot_z") 1.32f else 1.08f
            playerBall.resumeDirection = dir1
            playerBall.velocity = dir1 * (computeTargetCruiseSpeed(playerBall) * boost1)
        }

        if (!opponentBall.isStunned) {
            val rawV2 = if (opponentBall.character.id == "yeager_titan" && isPlayerAttacker) {
                opponentBall.velocity + (impulseVec / (m2 * 1.8f))
            } else {
                opponentBall.velocity + (impulseVec / m2)
            }
            val dir2 = sanitizeLaunchAngle(if (rawV2.length() > 5f) rawV2.normalized() else normal)
            val boost2 = if (isPlayerAttacker && attacker.character.id == "kakarot_z") 1.32f else 1.08f
            opponentBall.resumeDirection = dir2
            opponentBall.velocity = dir2 * (computeTargetCruiseSpeed(opponentBall) * boost2)
        }

        playerBall.squashFactor = 0.68f
        playerBall.stretchFactor = 1.24f
        playerBall.deformAngle = atan2(normal.y, normal.x)
        playerBall.weaponSpinBurst = 16f

        opponentBall.squashFactor = 0.68f
        opponentBall.stretchFactor = 1.24f
        opponentBall.deformAngle = atan2(-normal.y, -normal.x)
        opponentBall.weaponSpinBurst = 16f

        if (collisionCooldown > 0f) return
        collisionCooldown = 0.16f

        resolveCollisionCombat(attacker, defender, isPlayerAttacker, pSpeed, oSpeed)

        // Reset bank-shot bounce combo after impact lands
        attacker.wallBounceCombo = 0
    }

    private fun resolveCollisionCombat(
        attacker: CombatBall,
        defender: CombatBall,
        isPlayerAttacker: Boolean,
        pSpeed: Float,
        oSpeed: Float
    ) {
        val bankBonus = min(10, attacker.wallBounceCombo * 2)
        var damage = attacker.character.baseDamage.toFloat() + bankBonus
        var isCrit = bankBonus >= 4

        if (attacker.isUltimateActive) {
            damage *= attacker.character.ultimateDamageMultiplier
            isCrit = true
        }

        // Attacker Archetype Passives
        when (attacker.character.id) {
            "kamado_sun" -> {
                defender.bleedTicksRemaining = 4
                defender.bleedTimer = 0f
            }
            "roronoa_ashura" -> {
                defender.bleedTicksRemaining = max(defender.bleedTicksRemaining, 3)
                defender.bleedTimer = 0f
            }
            "raikiri_shinobi" -> {
                if (!defender.isStunned) {
                    if (defender.velocity.length() > 1f) defender.resumeDirection = defender.velocity.normalized()
                    defender.stunTimeRemaining = 0.22f
                }
            }
            "chainsaw_devil" -> {
                val heal = if (attacker.isUltimateActive) 6f else 3f
                attacker.currentHp = min(attacker.maxHp, attacker.currentHp + heal)
                spawnFloatingText("+${heal.toInt()} HP", attacker.position.x, attacker.position.y - attacker.radius, Color(0xFF4ADE80))
            }
            "dragneel_ignis" -> {
                spawnFireHazard(attacker.id, defender.position.x, defender.position.y, defender.radius * 0.90f, 3.6f)
            }
            "asta_grimoire" -> {
                defender.ultimateGauge = max(0f, defender.ultimateGauge - 12f)
            }
            "kurosaki_hollow" -> {
                val mySpd = if (isPlayerAttacker) pSpeed else oSpeed
                val theirSpd = if (isPlayerAttacker) oSpeed else pSpeed
                if (mySpd > theirSpd) damage += 4f
            }
            "killua_godspeed" -> {
                damage += 3f
            }
            "kujo_star" -> {
                damage += 4f
            }
            "caped_baldy" -> {
                if (attacker.isSeriousActive) {
                    damage = 38f + bankBonus
                    isCrit = true
                    attacker.isSeriousActive = false
                    screenShakeAmount = 28f
                    spawnFloatingText("SERIOUS PUNCH!", defender.position.x, defender.position.y - defender.radius * 1.4f, Color(0xFFFACC15), isCrit = true)
                }
            }
            "alucard_hellsing" -> {
                val siphon = if (attacker.isUltimateActive) 6f else 3f
                attacker.currentHp = min(attacker.maxHp, attacker.currentHp + siphon)
            }
            "sukuna_shrine" -> {
                if (defender.currentHp < 50f) {
                    damage *= 1.25f
                    isCrit = true
                }
                if (attacker.isUltimateActive) {
                    defender.bleedTicksRemaining = 4
                    defender.bleedTimer = 0f
                }
            }
            "jinwoo_monarch" -> {
                attacker.jinwooSpeedStacks = min(8, attacker.jinwooSpeedStacks + 1)
                if (attacker.isUltimateActive) {
                    attacker.currentHp = min(attacker.maxHp, attacker.currentHp + 4f)
                }
            }
        }

        // Defender Archetype Passives (unless bypassed by Frieren's Zoltraak Ult)
        val ignoresDefense = attacker.character.id == "frieren_zoltraak" && attacker.isUltimateActive
        if (!ignoresDefense) {
            if (defender.character.id == "satoru_void") {
                damage *= 0.80f
                defender.isShieldActive = true
            }
            if (defender.character.id == "killua_godspeed" && defender.isUltimateActive) {
                damage *= 0.50f
            }
            if (defender.character.id == "elric_fullmetal") {
                val reflected = max(1, (damage * 0.25f).toInt())
                attacker.currentHp = max(0f, attacker.currentHp - reflected)
                attacker.hitFlashTimer = 0.5f
            }
        }

        val finalDmg = max(1, damage.toInt())
        defender.currentHp = max(0f, defender.currentHp - finalDmg)
        defender.hitFlashTimer = 0.9f

        // Charge Ultimate meters
        attacker.addUltimate(18f)
        val defGain = if (defender.character.id == "kageyama_esper") 21f else 14f
        defender.addUltimate(defGain)

        val contact = (playerBall.position + opponentBall.position) * 0.5f
        spawnShockwave(contact.x, contact.y, attacker.character.auraColor, attacker.radius * (if (isCrit) 3.1f else 2.2f))
        spawnImpactBurst(contact.x, contact.y, attacker.character.auraColor, engineSettings.renderMode.particleCountOnHit)
        screenShakeAmount = max(screenShakeAmount, if (isCrit) 18f else 10f)

        val hitLabel = buildString {
            if (bankBonus > 0) append("BANK ")
            if (isCrit) append("CRIT ")
            append("-$finalDmg")
        }
        spawnFloatingText(
            text = hitLabel,
            x = defender.position.x,
            y = defender.position.y - defender.radius * 0.95f,
            color = if (isCrit) Color(0xFFFACC15) else Color.White,
            isCrit = isCrit
        )

        onCollisionImpact(isPlayerAttacker, finalDmg, isCrit, attacker.character.passiveName)
    }

    private fun updateProceduralVisuals(ball: CombatBall, dt: Float) {
        val speed = ball.velocity.length()

        ball.weaponSpinBurst = max(0f, ball.weaponSpinBurst - dt * 18f)
        val spinSpeed = 3.4f + (speed / 260f) + ball.weaponSpinBurst
        ball.weaponAngle = (ball.weaponAngle + spinSpeed * dt) % (2f * Math.PI.toFloat())

        if (speed > 40f) {
            ball.deformAngle = atan2(ball.velocity.y, ball.velocity.x)
            val speedNorm = (speed / (baseArenaSpeed * 1.6f)).coerceIn(0f, 1f)
            val targetStretch = 1f + speedNorm * 0.18f
            val targetSquash = 1f - speedNorm * 0.12f
            ball.stretchFactor += (targetStretch - ball.stretchFactor) * min(1f, dt * 14f)
            ball.squashFactor += (targetSquash - ball.squashFactor) * min(1f, dt * 14f)
        } else {
            ball.stretchFactor += (1f - ball.stretchFactor) * min(1f, dt * 14f)
            ball.squashFactor += (1f - ball.squashFactor) * min(1f, dt * 14f)
        }

        if (engineSettings.physicsMesh == PhysicsMesh.DYNAMIC && ball.hairNodes.isNotEmpty()) {
            updateVerletHair(ball)
        }

        if (speed > 80f && engineSettings.renderMode != RenderMode.PIXELIZED) {
            val maxTrail = if (engineSettings.renderMode == RenderMode.REALISTIC) 7 else 4
            ball.trailPositions.addFirst(ball.position)
            while (ball.trailPositions.size > maxTrail) {
                ball.trailPositions.removeLast()
            }
        } else if (ball.trailPositions.isNotEmpty()) {
            ball.trailPositions.removeLast()
        }
    }

    private fun updateVerletHair(ball: CombatBall) {
        val nodes = ball.hairNodes
        if (nodes.isEmpty()) return

        val speed = ball.velocity.length()
        val anchorDir = if (speed > 30f) (ball.velocity * -1f).normalized() else Vector2(0f, -1f)
        nodes[0].x = ball.position.x + anchorDir.x * (ball.radius * 0.55f)
        nodes[0].y = ball.position.y + anchorDir.y * (ball.radius * 0.55f)

        for (i in 1 until nodes.size) {
            nodes[i].update(friction = 0.86f)
            nodes[i].y -= 0.7f
        }

        val segLength = ball.radius * 0.30f
        repeat(2) {
            for (i in 0 until nodes.size - 1) {
                val p1 = nodes[i]
                val p2 = nodes[i + 1]
                val dx = p2.x - p1.x
                val dy = p2.y - p1.y
                val dist = hypot(dx, dy)
                if (dist > 0.0001f) {
                    val diff = (dist - segLength) / dist
                    if (i == 0) {
                        p2.x -= dx * diff
                        p2.y -= dy * diff
                    } else {
                        p1.x += dx * diff * 0.45f
                        p1.y += dy * diff * 0.45f
                        p2.x -= dx * diff * 0.55f
                        p2.y -= dy * diff * 0.55f
                    }
                }
            }
        }
    }

    private fun spawnFireHazard(ownerId: String, x: Float, y: Float, radius: Float, duration: Float) {
        if (fireHazards.size >= 8) {
            fireHazards.removeAt(0)
        }
        fireHazards.add(
            FireHazardZone(
                id = nextEntityId++,
                ownerId = ownerId,
                x = x,
                y = y,
                radius = radius,
                life = duration,
                maxLife = duration
            )
        )
    }

    private fun spawnShockwave(x: Float, y: Float, color: Color, maxRadius: Float) {
        if (shockwaves.size >= 10) {
            shockwaves.removeAt(0)
        }
        shockwaves.add(
            ShockwaveRing(
                id = nextEntityId++,
                x = x,
                y = y,
                color = color,
                maxRadius = maxRadius,
                life = 0.30f,
                maxLife = 0.30f
            )
        )
    }

    private fun spawnImpactBurst(x: Float, y: Float, color: Color, count: Int) {
        val speedScale = max(1f, min(arenaWidth, arenaHeight) / 440f)
        for (i in 0 until count) {
            if (particles.size >= 64) {
                particles.removeAt(0)
            }
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = (130f + Random.nextFloat() * 360f) * speedScale
            particles.add(
                Particle(
                    id = nextEntityId++,
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = if (i % 3 == 0) Color.White else color,
                    life = 0.28f + Random.nextFloat() * 0.26f,
                    maxLife = 0.54f,
                    size = (3.5f + Random.nextFloat() * 4.5f) * speedScale * 0.6f,
                    isPixel = engineSettings.renderMode == RenderMode.PIXELIZED
                )
            )
        }
    }

    fun spawnFloatingText(text: String, x: Float, y: Float, color: Color, isCrit: Boolean = false) {
        if (floatingTexts.size >= 10) {
            floatingTexts.removeAt(0)
        }
        val clampedX = x.coerceIn(arenaWidth * 0.16f, arenaWidth * 0.84f)
        val clampedY = y.coerceIn(arenaHeight * 0.10f, arenaHeight * 0.88f)
        floatingTexts.add(
            FloatingCombatText(
                id = nextEntityId++,
                text = text,
                x = clampedX,
                y = clampedY,
                color = color,
                life = 0.90f,
                maxLife = 0.90f,
                isCrit = isCrit
            )
        )
    }

    private fun updateVfx(dt: Float) {
        var i = particles.size - 1
        while (i >= 0) {
            val p = particles[i]
            p.life -= dt
            if (p.life <= 0f) {
                particles.removeAt(i)
            } else {
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.vx *= 0.92f
                p.vy *= 0.92f
            }
            i--
        }

        var s = shockwaves.size - 1
        while (s >= 0) {
            val ring = shockwaves[s]
            ring.life -= dt
            if (ring.life <= 0f) shockwaves.removeAt(s)
            s--
        }

        var t = floatingTexts.size - 1
        while (t >= 0) {
            val ft = floatingTexts[t]
            ft.life -= dt
            if (ft.life <= 0f) {
                floatingTexts.removeAt(t)
            } else {
                ft.y -= 58f * dt
            }
            t--
        }
    }
}
