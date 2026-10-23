package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
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
    val particles = mutableListOf<Particle>()
    val shockwaves = mutableListOf<ShockwaveRing>()
    val floatingTexts = mutableListOf<FloatingCombatText>()
    val fireHazards = mutableListOf<FireHazardZone>()

    private var nextEntityId = 1L

    var globalTime: Float = 0f
    var isRoundActive: Boolean = true
    var isArenaInitialized: Boolean = false
    var screenShakeAmount: Float = 0f
    var slowMoScale: Float = 1.0f

    // Wall impact glow flashes (0..1)
    var wallFlashLeft: Float = 0f
    var wallFlashRight: Float = 0f
    var wallFlashTop: Float = 0f
    var wallFlashBottom: Float = 0f

    // AI aiming telegraph state (rendered on Canvas during TurnState.AI_AIMING)
    var aiAimDirection: Vector2 = Vector2(0f, 1f)
    var aiAimCharge: Float = 0f
    var isAiAimingVisible: Boolean = false

    private var collisionCooldown: Float = 0f

    val wallPadding: Float get() = max(18f, min(arenaWidth, arenaHeight) * 0.028f)
    val wallLeft: Float get() = wallPadding + playerBall.radius
    val wallRight: Float get() = arenaWidth - wallPadding - playerBall.radius
    val wallTop: Float get() = wallPadding + playerBall.radius
    val wallBottom: Float get() = arenaHeight - wallPadding - playerBall.radius

    val maxLaunchSpeed: Float
        get() = hypot(arenaWidth, arenaHeight) * 1.48f

    val stopVelocityThreshold: Float
        get() = max(24f, hypot(arenaWidth, arenaHeight) * 0.018f)

    fun configureArenaBounds(widthPx: Float, heightPx: Float) {
        if (widthPx < 100f || heightPx < 100f) return
        arenaWidth = widthPx
        arenaHeight = heightPx

        val scaledRadius = (min(widthPx, heightPx) * 0.074f).coerceIn(32f, 92f)
        playerBall.radius = scaledRadius
        opponentBall.radius = scaledRadius

        if (!isArenaInitialized) {
            isArenaInitialized = true
            playerBall.position = Vector2(widthPx * 0.5f, heightPx * 0.77f)
            opponentBall.position = Vector2(widthPx * 0.5f, heightPx * 0.23f)
            playerBall.velocity = Vector2.Zero
            opponentBall.velocity = Vector2.Zero
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
        val seg = ball.radius * 0.34f
        val nodes = List(5) { i ->
            VerletHairNode(
                x = ball.position.x,
                y = ball.position.y - i * seg,
                oldX = ball.position.x,
                oldY = ball.position.y - i * seg
            )
        }
        ball.hairNodes = nodes
    }

    /**
     * Launches the player's sphere based on normalized pullback power (0..1) and launch direction.
     */
    fun launchPlayer(direction: Vector2, powerRatio: Float) {
        val dir = direction.normalized()
        if (dir.length() < 0.01f) return

        playerBall.wallBounceCombo = 0
        opponentBall.wallBounceCombo = 0

        var speedMod = 0.85f + (playerBall.character.speedRating * 0.22f)
        if (playerBall.isUltimateActive && playerBall.character.id == "saiyan_spirit") {
            speedMod *= 1.35f
        }

        val speed = maxLaunchSpeed * powerRatio.coerceIn(0.22f, 1.0f) * speedMod
        playerBall.velocity = dir * speed
        playerBall.stretchFactor = 1.28f
        playerBall.squashFactor = 0.78f
        playerBall.deformAngle = atan2(dir.y, dir.x)
        playerBall.weaponSpinBurst = 14f

        spawnShockwave(playerBall.position.x, playerBall.position.y, playerBall.character.auraColor, playerBall.radius * 2.2f)
        spawnImpactBurst(playerBall.position.x, playerBall.position.y, playerBall.character.auraColor, 14)
    }

    /**
     * Computes a smart AI shot direction (direct intercept or 1-wall ricochet bank shot).
     */
    fun computeAiAimDirection(difficulty: BotDifficulty): Vector2 {
        val origin = opponentBall.position
        val target = playerBall.position

        val useBankShot = Random.nextFloat() < difficulty.bankShotChance
        val rawDir = if (useBankShot) {
            // Reflect target across left or right wall for a stylish bank shot!
            val bounceLeft = Random.nextBoolean()
            val mirroredX = if (bounceLeft) {
                (2f * wallLeft) - target.x
            } else {
                (2f * wallRight) - target.x
            }
            (Vector2(mirroredX, target.y) - origin).normalized()
        } else {
            (target - origin).normalized()
        }

        val maxJitterRad = (1f - difficulty.aimAccuracy) * 0.42f
        val jitter = (Random.nextFloat() * 2f - 1f) * maxJitterRad
        return rawDir.rotate(jitter).normalized()
    }

    /**
     * Launches the AI opponent's sphere after its aiming telegraph completes.
     */
    fun launchOpponent(direction: Vector2, powerRatio: Float) {
        val dir = direction.normalized()
        if (dir.length() < 0.01f) return

        playerBall.wallBounceCombo = 0
        opponentBall.wallBounceCombo = 0
        isAiAimingVisible = false

        var speedMod = 0.85f + (opponentBall.character.speedRating * 0.22f)
        if (opponentBall.isUltimateActive && opponentBall.character.id == "saiyan_spirit") {
            speedMod *= 1.35f
        }

        val speed = maxLaunchSpeed * powerRatio.coerceIn(0.45f, 1.05f) * speedMod
        opponentBall.velocity = dir * speed
        opponentBall.stretchFactor = 1.28f
        opponentBall.squashFactor = 0.78f
        opponentBall.deformAngle = atan2(dir.y, dir.x)
        opponentBall.weaponSpinBurst = 14f

        spawnShockwave(opponentBall.position.x, opponentBall.position.y, opponentBall.character.auraColor, opponentBall.radius * 2.2f)
        spawnImpactBurst(opponentBall.position.x, opponentBall.position.y, opponentBall.character.auraColor, 14)
    }

    fun areBallsAtRest(): Boolean {
        return playerBall.velocity.length() < stopVelocityThreshold &&
            opponentBall.velocity.length() < stopVelocityThreshold
    }

    fun haltBalls() {
        playerBall.velocity = Vector2.Zero
        opponentBall.velocity = Vector2.Zero
    }

    fun triggerUltimate(ball: CombatBall) {
        if (ball.ultimateGauge < 100f || ball.isUltimateActive) return
        ball.ultimateGauge = 0f
        ball.isUltimateActive = true
        ball.ultimateTimeRemaining = ball.character.ultimateDurationSeconds
        ball.weaponSpinBurst = 22f

        val target = if (ball === playerBall) opponentBall else playerBall
        when (ball.character.id) {
            "six_glow_magic" -> {
                target.velocity = Vector2.Zero
                target.stunTimeRemaining = 1.8f
                spawnFloatingText(
                    "DOMAIN: UNLIMITED VOID!",
                    target.position.x,
                    target.position.y - target.radius * 1.3f,
                    Color(0xFFD500F9),
                    isCrit = true
                )
            }
            "one_punch" -> {
                ball.isSeriousActive = true
                spawnFloatingText(
                    "SERIOUS SERIES ARMED!",
                    ball.position.x,
                    ball.position.y - ball.radius * 1.3f,
                    Color(0xFFFF1E42),
                    isCrit = true
                )
            }
            "titan_buster" -> {
                // Colossus Steam Eruption: AoE knockback + 15 burn damage if close
                val delta = target.position - ball.position
                val dist = delta.length().coerceAtLeast(1f)
                if (dist < arenaWidth * 0.55f) {
                    target.currentHp = max(0f, target.currentHp - 15f)
                    target.velocity = (delta / dist) * (maxLaunchSpeed * 0.55f)
                    spawnFloatingText("-15 STEAM BLAST!", target.position.x, target.position.y - target.radius, Color(0xFFFF5E1A), isCrit = true)
                }
            }
            else -> {
                spawnFloatingText(
                    "${ball.character.ultimateName.uppercase()}!",
                    ball.position.x,
                    ball.position.y - ball.radius * 1.3f,
                    Color(0xFF00F0FF),
                    isCrit = true
                )
            }
        }

        spawnShockwave(ball.position.x, ball.position.y, ball.character.auraColor, ball.radius * 4.2f)
        spawnImpactBurst(ball.position.x, ball.position.y, ball.character.auraColor, 36)
        screenShakeAmount = 22f
    }

    fun update(rawDt: Float) {
        val dt = (rawDt * slowMoScale).coerceIn(0.001f, 0.04f)
        globalTime += dt

        // Decay screen shake & wall glow flashes
        screenShakeAmount = max(0f, screenShakeAmount - rawDt * 48f)
        wallFlashLeft = max(0f, wallFlashLeft - rawDt * 3.5f)
        wallFlashRight = max(0f, wallFlashRight - rawDt * 3.5f)
        wallFlashTop = max(0f, wallFlashTop - rawDt * 3.5f)
        wallFlashBottom = max(0f, wallFlashBottom - rawDt * 3.5f)
        collisionCooldown = max(0f, collisionCooldown - dt)

        // Smooth ghost HP bars (fighting game style)
        playerBall.delayedHp += (playerBall.currentHp - playerBall.delayedHp) * min(1f, rawDt * 5.5f)
        opponentBall.delayedHp += (opponentBall.currentHp - opponentBall.delayedHp) * min(1f, rawDt * 5.5f)

        playerBall.hitFlashTimer = max(0f, playerBall.hitFlashTimer - rawDt * 5f)
        opponentBall.hitFlashTimer = max(0f, opponentBall.hitFlashTimer - rawDt * 5f)

        if (!isRoundActive) {
            updateVfx(dt)
            return
        }

        // 1. Update Ultimates, Status Effects & Hazards
        updateUltimate(playerBall, dt)
        updateUltimate(opponentBall, dt)
        updateStatusEffects(playerBall, dt)
        updateStatusEffects(opponentBall, dt)
        updateHazards(dt)

        // 2. Sub-stepped Rigid-Body Ball Physics (4 sub-steps for crisp high-speed ricochets)
        val subSteps = 4
        val subDt = dt / subSteps
        for (step in 0 until subSteps) {
            stepBallMovement(playerBall, subDt)
            stepBallMovement(opponentBall, subDt)
            checkWallCollision(playerBall)
            checkWallCollision(opponentBall)
            checkBallToBallCollision()
        }

        // 3. Procedural Deformation, Weapon Spin, Trails & Verlet Hair (once per frame)
        updateProceduralVisuals(playerBall, dt)
        updateProceduralVisuals(opponentBall, dt)

        // 4. Particles, Shockwaves & Floating Text
        updateVfx(dt)

        // 5. Knockout Check
        if (playerBall.isDead && isRoundActive) {
            isRoundActive = false
            slowMoScale = 0.25f
            screenShakeAmount = 30f
            spawnShockwave(playerBall.position.x, playerBall.position.y, Color(0xFFFF1E42), playerBall.radius * 5f)
            spawnImpactBurst(playerBall.position.x, playerBall.position.y, Color(0xFFFF1E42), 45)
            onKnockout(false)
        } else if (opponentBall.isDead && isRoundActive) {
            isRoundActive = false
            slowMoScale = 0.25f
            screenShakeAmount = 30f
            spawnShockwave(opponentBall.position.x, opponentBall.position.y, Color(0xFFFFC800), opponentBall.radius * 5f)
            spawnImpactBurst(opponentBall.position.x, opponentBall.position.y, Color(0xFFFFC800), 45)
            onKnockout(true)
        }
    }

    private fun stepBallMovement(ball: CombatBall, subDt: Float) {
        if (ball.isStunned) {
            ball.velocity = Vector2.Zero
            return
        }

        // Frame-rate-independent exponential air/floor resistance
        val dragCoeff = if (ball.isUltimateActive && ball.character.id == "lightning_ninja") {
            0.18f // Almost zero friction during Lightning Speed!
        } else {
            1.38f
        }
        val damping = exp(-dragCoeff * subDt)
        ball.velocity = ball.velocity * damping

        if (ball.velocity.length() < stopVelocityThreshold * 0.45f) {
            ball.velocity = Vector2.Zero
        }

        ball.position = ball.position + (ball.velocity * subDt)
    }

    private fun updateProceduralVisuals(ball: CombatBall, dt: Float) {
        val speed = ball.velocity.length()

        // Continuous orbiting weapon + impact slash burst
        ball.weaponSpinBurst = max(0f, ball.weaponSpinBurst - dt * 18f)
        val spinSpeed = 3.2f + (speed / 220f) + ball.weaponSpinBurst
        ball.weaponAngle = (ball.weaponAngle + spinSpeed * dt) % (2f * Math.PI.toFloat())

        // Squash & Stretch spring physics
        if (speed > stopVelocityThreshold * 1.4f) {
            ball.deformAngle = atan2(ball.velocity.y, ball.velocity.x)
            val speedNorm = (speed / maxLaunchSpeed).coerceIn(0f, 1f)
            val targetStretch = 1f + speedNorm * 0.26f
            val targetSquash = 1f - speedNorm * 0.16f
            ball.stretchFactor += (targetStretch - ball.stretchFactor) * min(1f, dt * 14f)
            ball.squashFactor += (targetSquash - ball.squashFactor) * min(1f, dt * 14f)
        } else {
            ball.stretchFactor += (1f - ball.stretchFactor) * min(1f, dt * 14f)
            ball.squashFactor += (1f - ball.squashFactor) * min(1f, dt * 14f)
        }

        // Verlet Hair Rope Simulation
        if (engineSettings.physicsMesh == PhysicsMesh.DYNAMIC && ball.hairNodes.isNotEmpty()) {
            updateVerletHair(ball)
        }

        // Motion Blur / Speed Trail
        if (speed > maxLaunchSpeed * 0.14f && engineSettings.renderMode != RenderMode.PIXELIZED) {
            ball.trailPositions.add(0, ball.position)
            val maxTrail = if (engineSettings.renderMode == RenderMode.REALISTIC) 10 else 5
            while (ball.trailPositions.size > maxTrail) {
                ball.trailPositions.removeAt(ball.trailPositions.lastIndex)
            }
        } else if (ball.trailPositions.isNotEmpty()) {
            ball.trailPositions.removeAt(ball.trailPositions.lastIndex)
        }
    }

    private fun updateVerletHair(ball: CombatBall) {
        val nodes = ball.hairNodes
        if (nodes.isEmpty()) return

        // Root anchor lags opposite velocity slightly on top/back of sphere
        val speed = ball.velocity.length()
        val anchorDir = if (speed > 30f) {
            (ball.velocity * -1f).normalized()
        } else {
            Vector2(0f, -1f)
        }

        nodes[0].x = ball.position.x + anchorDir.x * (ball.radius * 0.55f)
        nodes[0].y = ball.position.y + anchorDir.y * (ball.radius * 0.55f)

        for (i in 1 until nodes.size) {
            nodes[i].update(friction = 0.88f)
            // Subtle upward anime aura lift
            nodes[i].y -= 0.9f
        }

        val segLength = ball.radius * 0.34f
        repeat(4) {
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

    private fun updateUltimate(ball: CombatBall, dt: Float) {
        if (!ball.isUltimateActive) return
        ball.ultimateTimeRemaining -= dt

        // Demon Slash Hinokami Dance leaves blazing fire zones while moving
        if (ball.character.id == "demon_slash" && ball.velocity.length() > stopVelocityThreshold * 2f) {
            if (Random.nextFloat() < 0.30f) {
                fireHazards.add(
                    FireHazardZone(
                        id = nextEntityId++,
                        x = ball.position.x,
                        y = ball.position.y,
                        radius = ball.radius * 0.85f,
                        life = 4.0f,
                        maxLife = 4.0f
                    )
                )
            }
        }

        if (ball.ultimateTimeRemaining <= 0f) {
            ball.isUltimateActive = false
            ball.isSeriousActive = false
        }
    }

    private fun updateStatusEffects(ball: CombatBall, dt: Float) {
        if (ball.stunTimeRemaining > 0f) {
            ball.stunTimeRemaining = max(0f, ball.stunTimeRemaining - dt)
            ball.velocity = Vector2.Zero
        }

        if (ball.bleedTicksRemaining > 0) {
            ball.bleedTimer += dt
            if (ball.bleedTimer >= 0.5f) {
                ball.bleedTimer -= 0.5f
                ball.bleedTicksRemaining--
                ball.currentHp = max(0f, ball.currentHp - 3f)
                ball.hitFlashTimer = 0.5f
                spawnFloatingText("-3 BLEED", ball.position.x, ball.position.y - ball.radius, Color(0xFFFF1E42))
                spawnImpactBurst(ball.position.x, ball.position.y, Color(0xFFFF1E42), 6)
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
        // Demon Slash doesn't burn in its own Hinokami flames while ultimate is active
        if (ball.isUltimateActive && ball.character.id == "demon_slash") return
        val dist = ball.position.distanceTo(Vector2(hazard.x, hazard.y))
        if (dist < ball.radius + hazard.radius * 0.8f) {
            ball.currentHp = max(0f, ball.currentHp - 5f * dt)
            if (Random.nextFloat() < 0.08f) {
                spawnFloatingText("BURN", ball.position.x, ball.position.y - ball.radius * 0.8f, Color(0xFFFF5E1A))
            }
        }
    }

    private fun checkWallCollision(ball: CombatBall) {
        var bounced = false
        var normal = Vector2.Zero

        val baseRestitution = when {
            ball.isUltimateActive && ball.character.id == "lightning_ninja" -> 1.08f
            ball.character.id == "rubber_stretch" -> 0.96f
            else -> 0.88f
        }

        if (ball.position.x < wallLeft) {
            ball.position = Vector2(wallLeft, ball.position.y)
            ball.velocity = Vector2(kotlin.math.abs(ball.velocity.x) * baseRestitution, ball.velocity.y * baseRestitution.coerceAtMost(1.02f))
            bounced = true
            normal = Vector2(1f, 0f)
            wallFlashLeft = 1f
        } else if (ball.position.x > wallRight) {
            ball.position = Vector2(wallRight, ball.position.y)
            ball.velocity = Vector2(-kotlin.math.abs(ball.velocity.x) * baseRestitution, ball.velocity.y * baseRestitution.coerceAtMost(1.02f))
            bounced = true
            normal = Vector2(-1f, 0f)
            wallFlashRight = 1f
        }

        if (ball.position.y < wallTop) {
            ball.position = Vector2(ball.position.x, wallTop)
            ball.velocity = Vector2(ball.velocity.x * baseRestitution.coerceAtMost(1.02f), kotlin.math.abs(ball.velocity.y) * baseRestitution)
            bounced = true
            normal = Vector2(0f, 1f)
            wallFlashTop = 1f
        } else if (ball.position.y > wallBottom) {
            ball.position = Vector2(ball.position.x, wallBottom)
            ball.velocity = Vector2(ball.velocity.x * baseRestitution.coerceAtMost(1.02f), -kotlin.math.abs(ball.velocity.y) * baseRestitution)
            bounced = true
            normal = Vector2(0f, -1f)
            wallFlashBottom = 1f
        }

        if (bounced && ball.velocity.length() > stopVelocityThreshold * 1.5f) {
            ball.wallBounceCombo++
            ball.squashFactor = 0.68f
            ball.stretchFactor = 1.24f
            ball.deformAngle = atan2(normal.y, normal.x)

            // Cap maximum runaway velocity
            val spd = ball.velocity.length()
            if (spd > maxLaunchSpeed * 1.45f) {
                ball.velocity = ball.velocity.normalized() * (maxLaunchSpeed * 1.45f)
            }

            // Flame Dragon ultimate spawns fireballs on wall bounce
            if (ball.isUltimateActive && ball.character.id == "flame_dragon") {
                fireHazards.add(
                    FireHazardZone(
                        id = nextEntityId++,
                        x = ball.position.x,
                        y = ball.position.y,
                        radius = ball.radius * 0.9f,
                        life = 3.5f,
                        maxLife = 3.5f
                    )
                )
            }

            spawnImpactBurst(
                ball.position.x - normal.x * ball.radius * 0.8f,
                ball.position.y - normal.y * ball.radius * 0.8f,
                ball.character.auraColor,
                engineSettings.renderMode.particleCountOnHit / 2
            )
            onWallBounce(ball)
        }
    }

    private fun checkBallToBallCollision() {
        val delta = opponentBall.position - playerBall.position
        val dist = delta.length()
        val minDist = playerBall.radius + opponentBall.radius

        if (dist >= minDist || dist < 0.0001f) return

        val normal = delta / dist

        // Always resolve positional overlap immediately so spheres never stick together
        val overlap = minDist - dist
        playerBall.position = playerBall.position - (normal * (overlap * 0.52f))
        opponentBall.position = opponentBall.position + (normal * (overlap * 0.52f))

        val relVel = opponentBall.velocity - playerBall.velocity
        val velAlongNormal = relVel.dot(normal)

        // Already separating
        if (velAlongNormal > 0f) return

        val pSpeed = playerBall.velocity.length()
        val oSpeed = opponentBall.velocity.length()
        val isPlayerAttacker = pSpeed >= oSpeed
        val attacker = if (isPlayerAttacker) playerBall else opponentBall
        val defender = if (isPlayerAttacker) opponentBall else playerBall

        var m1 = playerBall.character.massRating
        var m2 = opponentBall.character.massRating
        if (playerBall.isUltimateActive && playerBall.character.id == "saiyan_spirit") m1 *= 1.2f
        if (opponentBall.isUltimateActive && opponentBall.character.id == "saiyan_spirit") m2 *= 1.2f

        val restitution = 0.92f
        var impulseMag = -(1f + restitution) * velAlongNormal / ((1f / m1) + (1f / m2))
        // Ensure lively minimum separation bounce on grazing hits
        impulseMag = max(impulseMag, maxLaunchSpeed * 0.24f)

        if (attacker.character.id == "saiyan_spirit") {
            impulseMag *= 1.40f // +40% Ki Blast Shockwave knockback!
        }

        val impulseVec = normal * impulseMag

        // Titan Buster passive: Hardening Strike resists incoming knockback
        if (playerBall.character.id == "titan_buster" && !isPlayerAttacker) {
            playerBall.velocity = playerBall.velocity * 0.4f
        } else {
            playerBall.velocity = playerBall.velocity - (impulseVec / m1)
        }

        if (opponentBall.character.id == "titan_buster" && isPlayerAttacker) {
            opponentBall.velocity = opponentBall.velocity * 0.4f
        } else {
            opponentBall.velocity = opponentBall.velocity + (impulseVec / m2)
        }

        // Six-Glow Magic passive: neutralizes 30% of incoming counter-impact force
        if (defender.character.id == "six_glow_magic") {
            defender.velocity = defender.velocity * 0.70f
            defender.isShieldActive = true
        }

        // Squash & weapon slash spin on both balls
        playerBall.squashFactor = 0.66f
        playerBall.stretchFactor = 1.25f
        playerBall.deformAngle = atan2(normal.y, normal.x)
        playerBall.weaponSpinBurst = 18f

        opponentBall.squashFactor = 0.66f
        opponentBall.stretchFactor = 1.25f
        opponentBall.deformAngle = atan2(-normal.y, -normal.x)
        opponentBall.weaponSpinBurst = 18f

        // Skip duplicate damage if still in micro-cooldown
        if (collisionCooldown > 0f) return
        collisionCooldown = 0.14f

        // Calculate Combat Damage + Bank-Shot Wall Bounce Bonus!
        val bankBonus = min(10, attacker.wallBounceCombo * 2)
        var damage = attacker.character.baseDamage.toFloat() + bankBonus
        var isCrit = bankBonus >= 4

        if (attacker.isUltimateActive) {
            damage *= attacker.character.ultimateDamageMultiplier
            isCrit = true
        }

        // Archetype Canon Passives
        when (attacker.character.id) {
            "demon_slash" -> {
                defender.bleedTicksRemaining = 4
                defender.bleedTimer = 0f
                spawnFloatingText("BLEED x4!", defender.position.x, defender.position.y - defender.radius * 1.4f, Color(0xFFFF1E42))
            }
            "lightning_ninja" -> {
                defender.stunTimeRemaining = 0.45f
                spawnFloatingText("CHIDORI STUN!", defender.position.x, defender.position.y - defender.radius * 1.4f, Color(0xFF00F0FF))
            }
            "one_punch" -> {
                if (attacker.isSeriousActive) {
                    damage = 40f + bankBonus
                    isCrit = true
                    attacker.isSeriousActive = false
                    screenShakeAmount = 32f
                    spawnFloatingText("SERIOUS PUNCH!", defender.position.x, defender.position.y - defender.radius * 1.5f, Color(0xFFFF1E42), isCrit = true)
                }
            }
            "flame_dragon" -> {
                fireHazards.add(
                    FireHazardZone(
                        id = nextEntityId++,
                        x = defender.position.x,
                        y = defender.position.y,
                        radius = defender.radius * 0.95f,
                        life = 4.0f,
                        maxLife = 4.0f
                    )
                )
            }
        }

        if (defender.character.id == "six_glow_magic") {
            damage *= 0.78f
        }

        val finalDmg = max(1, damage.toInt())
        defender.currentHp = max(0f, defender.currentHp - finalDmg)
        defender.hitFlashTimer = 1.0f

        // Charge Ultimate Gauges (+20% attacker, +12% defender for fast, exciting awakenings!)
        attacker.addUltimate(20f)
        defender.addUltimate(12f)

        val contact = (playerBall.position + opponentBall.position) * 0.5f
        spawnShockwave(contact.x, contact.y, attacker.character.auraColor, attacker.radius * (if (isCrit) 3.6f else 2.5f))
        spawnImpactBurst(contact.x, contact.y, attacker.character.auraColor, engineSettings.renderMode.particleCountOnHit)
        screenShakeAmount = max(screenShakeAmount, if (isCrit) 22f else 13f)

        val hitLabel = buildString {
            if (bankBonus > 0) append("BANK ")
            if (isCrit) append("CRIT! ")
            append("-$finalDmg")
        }
        spawnFloatingText(
            text = hitLabel,
            x = defender.position.x,
            y = defender.position.y - defender.radius * 0.9f,
            color = if (isCrit) Color(0xFFFFC800) else Color(0xFFFFFFFF),
            isCrit = isCrit
        )

        onCollisionImpact(isPlayerAttacker, finalDmg, isCrit, attacker.character.passiveName)
    }

    private fun spawnShockwave(x: Float, y: Float, color: Color, maxRadius: Float) {
        shockwaves.add(
            ShockwaveRing(
                id = nextEntityId++,
                x = x,
                y = y,
                color = color,
                maxRadius = maxRadius,
                life = 0.35f,
                maxLife = 0.35f
            )
        )
    }

    private fun spawnImpactBurst(x: Float, y: Float, color: Color, count: Int) {
        val speedScale = max(1f, min(arenaWidth, arenaHeight) / 420f)
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = (140f + Random.nextFloat() * 420f) * speedScale
            particles.add(
                Particle(
                    id = nextEntityId++,
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = if (i % 3 == 0) Color.White else color,
                    life = 0.35f + Random.nextFloat() * 0.35f,
                    maxLife = 0.70f,
                    size = (4f + Random.nextFloat() * 6f) * speedScale * 0.65f,
                    isPixel = engineSettings.renderMode == RenderMode.PIXELIZED
                )
            )
        }
    }

    fun spawnFloatingText(text: String, x: Float, y: Float, color: Color, isCrit: Boolean = false) {
        val clampedX = x.coerceIn(arenaWidth * 0.16f, arenaWidth * 0.84f)
        val clampedY = y.coerceIn(arenaHeight * 0.10f, arenaHeight * 0.90f)
        floatingTexts.add(
            FloatingCombatText(
                id = nextEntityId++,
                text = text,
                x = clampedX,
                y = clampedY,
                color = color,
                life = 1.05f,
                maxLife = 1.05f,
                isCrit = isCrit
            )
        )
    }

    private fun updateVfx(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.life -= dt
            if (p.life <= 0f) {
                pIter.remove()
                continue
            }
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vx *= 0.92f
            p.vy *= 0.92f
        }

        val sIter = shockwaves.iterator()
        while (sIter.hasNext()) {
            val s = sIter.next()
            s.life -= dt
            if (s.life <= 0f) sIter.remove()
        }

        val tIter = floatingTexts.iterator()
        while (tIter.hasNext()) {
            val ft = tIter.next()
            ft.life -= dt
            if (ft.life <= 0f) {
                tIter.remove()
                continue
            }
            ft.y -= 62f * dt
        }
    }
}
