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
    var isRemoteMultiplayer: Boolean = false,
    val onCollisionImpact: (isPlayerAttacker: Boolean, damage: Int, isCrit: Boolean, passiveLabel: String) -> Unit,
    val onWallBounce: (ball: CombatBall) -> Unit,
    val onKnockout: (isPlayerWinner: Boolean) -> Unit,
    val onUltimateActivated: (isPlayer: Boolean, ultimateName: String) -> Unit = { _, _ -> },
    val onPassiveTriggered: (isPlayer: Boolean, passiveName: String) -> Unit = { _, _ -> },
    val onRedirectUsed: (isPlayer: Boolean) -> Unit = {}
) {
    val particles = ArrayList<Particle>(72)
    val shockwaves = ArrayList<ShockwaveRing>(14)
    val floatingTexts = ArrayList<FloatingCombatText>(14)
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
    var aiRedirectCooldown: Float = 1.8f
    val maxRedirectCooldown: Float get() = playerBall.effectiveRedirectCooldownSec

    // Wall impact flashes (0..1)
    var wallFlashLeft: Float = 0f
    var wallFlashRight: Float = 0f
    var wallFlashTop: Float = 0f
    var wallFlashBottom: Float = 0f

    private var collisionCooldown: Float = 0f
    private var playerTrailCooldown: Float = 0f
    private var aiTrailCooldown: Float = 0f

    val wallPadding: Float get() = max(16f, min(arenaWidth, arenaHeight) * 0.026f)
    val wallLeft: Float get() = wallPadding + playerBall.radius
    val wallRight: Float get() = arenaWidth - wallPadding - playerBall.radius
    val wallTop: Float get() = wallPadding + playerBall.radius
    val wallBottom: Float get() = arenaHeight - wallPadding - playerBall.radius

    val baseArenaSpeed: Float
        get() = hypot(arenaWidth, arenaHeight) * 0.52f

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

    fun computeTargetCruiseSpeed(ball: CombatBall, difficultySpeedMod: Float = 1f): Float {
        var mult = ball.character.speedRating * difficultySpeedMod
        if (ball.speedBoostTimer > 0f) {
            mult *= ball.speedBoostMultiplier
        }
        if (ball.slowTimer > 0f) {
            mult *= 0.80f
        }
        if (ball.jinwooSpeedStacks > 0) {
            mult *= (1f + ball.jinwooSpeedStacks * 0.035f)
        }
        if (ball.isUltimateActive) {
            when (ball.character.ultimateArchetype) {
                UltimateArchetype.HYPER_AWAKEN -> mult *= 1.34f
                UltimateArchetype.ULT_NULLIFY -> mult *= 1.28f
                UltimateArchetype.SOUL_DEVOUR -> mult *= 1.26f
                UltimateArchetype.HAZARD_STORM -> mult *= 1.22f
                else -> mult *= 1.18f
            }
            if (ball.kamuiBounceStacks > 0) {
                mult *= (1f + min(6, ball.kamuiBounceStacks) * 0.05f)
            }
        }
        return baseArenaSpeed * mult.coerceIn(0.78f, 2.55f)
    }

    /**
     * Starts the nonstop clash: launches BOTH the player's sphere and the opponent's sphere.
     * In remote multiplayer (LAN / Online Room / Ranked), launches with deterministic mirrored vector
     * until remote peer sends their exact opening/dash vectors.
     */
    fun startNonstopClash(playerDirection: Vector2, difficulty: BotDifficulty) {
        val pDir = sanitizeLaunchAngle(
            if (playerDirection.length() > 0.01f) playerDirection.normalized()
            else Vector2(0.42f, -0.91f).normalized()
        )
        val oppDir = if (isRemoteMultiplayer) {
            sanitizeLaunchAngle(Vector2(-pDir.x, -pDir.y))
        } else {
            sanitizeLaunchAngle(computeAiInterceptDirection(difficulty))
        }

        isClashStarted = true
        playerRedirectCooldown = 1.1f
        aiRedirectCooldown = (difficulty.redirectCooldownSec - opponentBall.upgrades.redirectCooldownReduction())
            .coerceAtLeast(1.15f) * 0.68f

        applyImpulseLaunch(playerBall, pDir, 1.0f)
        applyImpulseLaunch(opponentBall, oppDir, if (isRemoteMultiplayer) 1.0f else difficulty.speedFactor)
    }

    fun applyRemoteOpponentCommand(mirroredDir: Vector2, isOpening: Boolean) {
        if (!isRoundActive) return
        val dir = sanitizeLaunchAngle(
            if (mirroredDir.length() > 0.01f) mirroredDir.normalized()
            else Vector2(-0.38f, 0.92f).normalized()
        )
        if (!isClashStarted) {
            isClashStarted = true
            playerRedirectCooldown = 1.0f
            val defaultLocal = sanitizeLaunchAngle(Vector2(-dir.x, -dir.y))
            if (playerBall.velocity.length() < 15f) {
                applyImpulseLaunch(playerBall, defaultLocal, 1.0f)
            }
        }
        if (!isOpening) {
            opponentBall.speedBoostMultiplier = 1.26f
            opponentBall.speedBoostTimer = 0.65f
            spawnFloatingText(
                "RIVAL DASH!",
                opponentBall.position.x,
                opponentBall.position.y - opponentBall.radius * 1.15f,
                Color(0xFFF43F5E)
            )
            onRedirectUsed(false)
        }
        applyImpulseLaunch(opponentBall, dir, 1.0f)
    }

    /**
     * Smoothly reconciles guest arena positions/velocities/HP against host authoritative LAN packet
     * (mirrored vertically so guest remains at bottom of their own screen).
     */
    fun applyMirroredLanSync(
        localNormX: Float,
        localNormY: Float,
        localNormVx: Float,
        localNormVy: Float,
        remoteNormX: Float,
        remoteNormY: Float,
        remoteNormVx: Float,
        remoteNormVy: Float,
        localHp: Float,
        remoteHp: Float,
        localUlt: Float,
        remoteUlt: Float
    ) {
        if (!isArenaInitialized || !isRoundActive) return
        val targetLocalPos = Vector2(
            (localNormX * arenaWidth).coerceIn(wallLeft, wallRight),
            (localNormY * arenaHeight).coerceIn(wallTop, wallBottom)
        )
        val targetRemotePos = Vector2(
            (remoteNormX * arenaWidth).coerceIn(wallLeft, wallRight),
            (remoteNormY * arenaHeight).coerceIn(wallTop, wallBottom)
        )
        val speedRef = baseArenaSpeed.coerceAtLeast(200f)
        playerBall.position = playerBall.position + (targetLocalPos - playerBall.position) * 0.42f
        opponentBall.position = opponentBall.position + (targetRemotePos - opponentBall.position) * 0.48f
        if (!playerBall.isStunned) {
            playerBall.velocity = Vector2(localNormVx * speedRef, localNormVy * speedRef)
        }
        if (!opponentBall.isStunned) {
            opponentBall.velocity = Vector2(remoteNormVx * speedRef, remoteNormVy * speedRef)
        }
        playerBall.currentHp = localHp.coerceIn(0f, playerBall.maxHp)
        opponentBall.currentHp = remoteHp.coerceIn(0f, opponentBall.maxHp)
        if (!playerBall.isUltimateActive && !playerBall.isUltimateOnCooldown) {
            playerBall.ultimateGauge = localUlt.coerceIn(0f, 100f)
        }
        if (!opponentBall.isUltimateActive && !opponentBall.isUltimateOnCooldown) {
            opponentBall.ultimateGauge = remoteUlt.coerceIn(0f, 100f)
        }
        if (!isClashStarted) {
            isClashStarted = true
        }
        checkKnockoutState()
    }

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

    fun redirectPlayerBall(direction: Vector2): Boolean {
        if (!isClashStarted || !isRoundActive || playerRedirectCooldown > 0f || playerBall.isStunned) {
            return false
        }
        val dir = sanitizeLaunchAngle(direction.normalized())
        if (dir.length() < 0.01f) return false

        playerRedirectCooldown = playerBall.effectiveRedirectCooldownSec
        playerBall.speedBoostMultiplier = 1.26f
        playerBall.speedBoostTimer = 0.65f
        applyImpulseLaunch(playerBall, dir, 1.0f)
        spawnFloatingText(
            "DASH!",
            playerBall.position.x,
            playerBall.position.y - playerBall.radius * 1.15f,
            Color(0xFF38BDF8)
        )
        onRedirectUsed(true)
        return true
    }

    /**
     * AI tactical step:
     * - Normal (MEDIUM) plays aggressively like a Hard veteran (fast Ult, predictive bank-shots, rapid dashes).
     * - Hard (HARD) plays at Extreme Grandmaster level (instant Ult, multi-wall prediction, evasive counter-dash).
     * Disabled automatically when `isRemoteMultiplayer == true`.
     */
    fun performAiTacticalStep(dt: Float, difficulty: BotDifficulty) {
        if (isRemoteMultiplayer || !isClashStarted || !isRoundActive) return

        // Symmetric AI Ultimate activation (respects gauge == 100%, not active, not on cooldown, not stunned)
        if (opponentBall.canActivateUltimate && !opponentBall.isStunned) {
            opponentBall.aiUltReactionTimer += dt
            val reactionDelay = when (difficulty) {
                BotDifficulty.EASY -> 0.80f
                BotDifficulty.MEDIUM -> 0.18f
                BotDifficulty.HARD -> 0.06f
            }
            if (opponentBall.aiUltReactionTimer >= reactionDelay) {
                opponentBall.aiUltReactionTimer = 0f
                triggerUltimate(opponentBall)
            }
        } else {
            opponentBall.aiUltReactionTimer = 0f
        }

        aiRedirectCooldown = max(0f, aiRedirectCooldown - dt)

        // Extreme mode threat-evasion: if player triggers Serious Punch or Ultimate at close range, counter-dash early!
        val playerDangerous = (playerBall.isSeriousActive || (playerBall.isUltimateActive && !opponentBall.isUltimateActive))
        val distToPlayer = (playerBall.position - opponentBall.position).length()
        val emergencyEvade = difficulty == BotDifficulty.HARD &&
            playerDangerous &&
            distToPlayer < playerBall.radius * 5.2f &&
            aiRedirectCooldown < 0.45f

        if ((aiRedirectCooldown <= 0f || emergencyEvade) && !opponentBall.isStunned) {
            val minCd = when (difficulty) {
                BotDifficulty.EASY -> 2.8f
                BotDifficulty.MEDIUM -> 1.55f
                BotDifficulty.HARD -> 1.08f
            }
            aiRedirectCooldown = (difficulty.redirectCooldownSec - opponentBall.upgrades.redirectCooldownReduction())
                .coerceAtLeast(minCd)

            val rawDir = if (emergencyEvade) {
                val away = (opponentBall.position - playerBall.position).normalized()
                away.rotate(if (Random.nextBoolean()) 0.72f else -0.72f)
            } else {
                computeAiInterceptDirection(difficulty)
            }
            val dir = sanitizeLaunchAngle(rawDir)
            opponentBall.speedBoostMultiplier = when (difficulty) {
                BotDifficulty.EASY -> 1.14f
                BotDifficulty.MEDIUM -> 1.24f
                BotDifficulty.HARD -> 1.32f
            }
            opponentBall.speedBoostTimer = if (difficulty == BotDifficulty.HARD) 0.72f else 0.58f
            applyImpulseLaunch(opponentBall, dir, difficulty.speedFactor)
            onRedirectUsed(false)
        }
    }

    fun computeAiInterceptDirection(difficulty: BotDifficulty): Vector2 {
        val origin = opponentBall.position
        val leadTime = when (difficulty) {
            BotDifficulty.EASY -> 0.14f
            BotDifficulty.MEDIUM -> 0.25f
            BotDifficulty.HARD -> 0.33f
        }
        val predicted = predictReflectedPosition(playerBall.position, playerBall.velocity, leadTime)

        val rawDir = when (difficulty) {
            BotDifficulty.EASY -> {
                (predicted - origin).normalized()
            }
            BotDifficulty.MEDIUM -> {
                if (Random.nextFloat() < 0.45f) {
                    val bounceLeft = predicted.x < arenaWidth * 0.5f
                    val mirrorX = if (bounceLeft) (2f * wallLeft - predicted.x) else (2f * wallRight - predicted.x)
                    (Vector2(mirrorX, predicted.y) - origin).normalized()
                } else {
                    (predicted - origin).normalized()
                }
            }
            BotDifficulty.HARD -> {
                // Extreme solver: pick deadly bank-shot when opponent has Elastic/Combo synergy or clean angle, else pinpoint intercept
                val wantsBank = opponentBall.character.passiveArchetype == PassiveArchetype.ELASTIC_MOMENTUM ||
                    Random.nextFloat() < 0.52f
                if (wantsBank) {
                    val distLeft = abs(origin.x - wallLeft) + abs(predicted.x - wallLeft)
                    val distRight = abs(wallRight - origin.x) + abs(wallRight - predicted.x)
                    val mirrorX = if (distLeft <= distRight) (2f * wallLeft - predicted.x) else (2f * wallRight - predicted.x)
                    (Vector2(mirrorX, predicted.y) - origin).normalized()
                } else {
                    (predicted - origin).normalized()
                }
            }
        }

        val fallback = if (rawDir.length() < 0.01f) Vector2(0.45f, 0.89f).normalized() else rawDir
        val jitterMax = (1f - difficulty.aimAccuracy) * 0.32f
        val jitter = (Random.nextFloat() * 2f - 1f) * jitterMax
        return fallback.rotate(jitter).normalized()
    }

    private fun predictReflectedPosition(pos: Vector2, vel: Vector2, dt: Float): Vector2 {
        var px = pos.x + vel.x * dt
        var py = pos.y + vel.y * dt
        if (px < wallLeft) px = wallLeft + (wallLeft - px)
        if (px > wallRight) px = wallRight - (px - wallRight)
        if (py < wallTop) py = wallTop + (wallTop - py)
        if (py > wallBottom) py = wallBottom - (py - wallBottom)
        return Vector2(px.coerceIn(wallLeft, wallRight), py.coerceIn(wallTop, wallBottom))
    }

    private fun applyImpulseLaunch(ball: CombatBall, dir: Vector2, speedScale: Float) {
        val targetSpeed = computeTargetCruiseSpeed(ball, speedScale)
        ball.resumeDirection = dir
        ball.velocity = dir * targetSpeed
        ball.stretchFactor = 1.20f
        ball.squashFactor = 0.84f
        ball.deformAngle = atan2(dir.y, dir.x)
        ball.weaponSpinBurst = 12f
        spawnShockwave(ball.position.x, ball.position.y, ball.character.auraColor, ball.radius * 1.9f)
        spawnImpactBurst(ball.position.x, ball.position.y, ball.character.auraColor, engineSettings.renderMode.particleCountOnHit / 2)
    }

    private fun sanitizeLaunchAngle(dir: Vector2): Vector2 {
        if (dir.length() < 0.001f) return Vector2(0.5f, -0.866f)
        var vx = dir.x
        var vy = dir.y
        if (abs(vx) < 0.22f) vx = if (vx >= 0f) 0.24f else -0.24f
        if (abs(vy) < 0.22f) vy = if (vy >= 0f) 0.24f else -0.24f
        return Vector2(vx, vy).normalized()
    }

    /**
     * Triggers Ultimate Awakening for either Player or AI across all 224 characters
     * via the 10 deterministic UltimateArchetypes.
     */
    fun triggerUltimate(ball: CombatBall): Boolean {
        if (!ball.canActivateUltimate) return false
        ball.ultimateGauge = 0f
        ball.isUltimateActive = true
        ball.ultimateTimeRemaining = ball.character.ultimateDurationSeconds
        ball.kamuiBounceStacks = 0
        ball.weaponSpinBurst = 24f
        ball.weaponScale = ball.targetWeaponScale
        ball.passiveFlashTimer = 0.85f

        val isPlayer = ball === playerBall
        val rival = if (isPlayer) opponentBall else playerBall
        val prefix = if (isPlayer) "" else "BOT "
        val pwrBonus = ball.upgrades.powerLevel
        var ultWaveStyle = AbilityVfxStyle.SHOCKWAVE

        when (ball.character.ultimateArchetype) {
            UltimateArchetype.TIME_STOP -> {
                ultWaveStyle = AbilityVfxStyle.TIME_DIAL
                if (rival.velocity.length() > 1f) rival.resumeDirection = rival.velocity.normalized()
                rival.velocity = Vector2.Zero
                rival.stunTimeRemaining = 1.65f
                spawnShockwave(rival.position.x, rival.position.y, Color(0xFFFACC15), rival.radius * 3.2f, AbilityVfxStyle.TIME_DIAL, 0.52f)
                val callout = if (ball.character.id == "dio_world") "${prefix}TIME STOP!" else "$prefix${ball.character.ultimateName.uppercase()}!"
                spawnFloatingText(callout, rival.position.x, rival.position.y - rival.radius * 1.25f, Color(0xFFFACC15), isCrit = true)
            }

            UltimateArchetype.DOMAIN_STUN -> {
                ultWaveStyle = AbilityVfxStyle.DOMAIN_SIGIL
                if (rival.velocity.length() > 1f) rival.resumeDirection = rival.velocity.normalized()
                rival.velocity = Vector2.Zero
                rival.stunTimeRemaining = 1.50f
                rival.slowTimer = 2.6f
                spawnFireHazard(ball.id, rival.position.x, rival.position.y, rival.radius * 1.05f, 4.0f, ball.character.auraColor)
                spawnShockwave(rival.position.x, rival.position.y, ball.character.auraColor, rival.radius * 3.4f, AbilityVfxStyle.DOMAIN_SIGIL, 0.55f)
                spawnFloatingText("$prefix${ball.character.ultimateName.uppercase()}!", rival.position.x, rival.position.y - rival.radius * 1.25f, Color(0xFF818CF8), isCrit = true)
            }

            UltimateArchetype.SERIOUS_ONE_PUNCH -> {
                ultWaveStyle = AbilityVfxStyle.SLASH_CROSS
                ball.isSeriousActive = true
                ball.speedBoostMultiplier = 1.28f
                ball.speedBoostTimer = 2.5f
                spawnFloatingText("$prefix${ball.character.ultimateName.uppercase()} READY!", ball.position.x, ball.position.y - ball.radius * 1.25f, Color(0xFFFACC15), isCrit = true)
            }

            UltimateArchetype.METEOR_NUKE -> {
                ultWaveStyle = AbilityVfxStyle.METEOR_BURST
                val burstDmg = 28f + pwrBonus * 3f
                val delta = rival.position - ball.position
                val dist = delta.length().coerceAtLeast(1f)
                rival.currentHp = max(0f, rival.currentHp - burstDmg)
                rival.hitFlashTimer = 0.90f
                val pushDir = sanitizeLaunchAngle(delta / dist)
                rival.resumeDirection = pushDir
                if (!rival.isStunned) {
                    rival.velocity = pushDir * (computeTargetCruiseSpeed(rival) * 1.28f)
                }
                spawnFireHazard(ball.id, rival.position.x, rival.position.y, rival.radius * 1.05f, 4.2f, ball.character.auraColor)
                spawnShockwave(rival.position.x, rival.position.y, ball.character.auraColor, rival.radius * 3.6f, AbilityVfxStyle.METEOR_BURST, 0.48f)
                spawnFloatingText("-${burstDmg.toInt()} ${ball.character.ultimateName.uppercase()}!", rival.position.x, rival.position.y - rival.radius, ball.character.auraColor, isCrit = true)
            }

            UltimateArchetype.GRAVITY_CRUSH -> {
                ultWaveStyle = AbilityVfxStyle.GRAVITY_IMPLODE
                val crushDmg = 22f + pwrBonus * 2.5f
                rival.currentHp = max(0f, rival.currentHp - crushDmg)
                rival.hitFlashTimer = 0.85f
                if (rival.velocity.length() > 1f) rival.resumeDirection = rival.velocity.normalized()
                rival.stunTimeRemaining = 0.85f
                rival.slowTimer = 2.8f
                spawnShockwave(rival.position.x, rival.position.y, ball.character.auraColor, rival.radius * 3.3f, AbilityVfxStyle.GRAVITY_IMPLODE, 0.50f)
                spawnFloatingText("-${crushDmg.toInt()} ${ball.character.ultimateName.uppercase()}!", rival.position.x, rival.position.y - rival.radius, ball.character.auraColor, isCrit = true)
            }

            UltimateArchetype.HEAL_REFORGE -> {
                ultWaveStyle = AbilityVfxStyle.HEAL_BLOOM
                val healAmt = max(48f, ball.maxHp * 0.18f + pwrBonus * 4f)
                ball.currentHp = min(ball.maxHp, ball.currentHp + healAmt)
                ball.isShieldActive = true
                ball.shieldVisualTimer = 3.6f
                spawnFloatingText("+${healAmt.toInt()} HP!", ball.position.x, ball.position.y - ball.radius * 1.25f, Color(0xFF4ADE80), isCrit = true)
            }

            UltimateArchetype.SOUL_DEVOUR -> {
                ultWaveStyle = AbilityVfxStyle.GRAVITY_IMPLODE
                val drainHp = 22f + pwrBonus * 2.5f
                rival.currentHp = max(0f, rival.currentHp - drainHp)
                rival.hitFlashTimer = 0.8f
                ball.currentHp = min(ball.maxHp, ball.currentHp + drainHp)
                rival.ultimateGauge = max(0f, rival.ultimateGauge - 24f)
                spawnShockwave(rival.position.x, rival.position.y, Color(0xFF38BDF8), rival.radius * 2.8f, AbilityVfxStyle.GRAVITY_IMPLODE, 0.42f)
                spawnFloatingText("±${drainHp.toInt()} STEAL!", ball.position.x, ball.position.y - ball.radius * 1.25f, Color(0xFF38BDF8), isCrit = true)
            }

            UltimateArchetype.HAZARD_STORM -> {
                ultWaveStyle = AbilityVfxStyle.METEOR_BURST
                spawnFireHazard(ball.id, ball.position.x, ball.position.y, ball.radius * 1.15f, 4.6f, ball.character.auraColor)
                spawnFireHazard(ball.id, rival.position.x, rival.position.y, ball.radius * 0.98f, 4.2f, ball.character.auraColor)
                spawnFireHazard(ball.id, arenaWidth * 0.5f, arenaHeight * 0.5f, ball.radius * 1.10f, 4.4f, ball.character.auraColor)
                spawnFloatingText("$prefix${ball.character.ultimateName.uppercase()}!", ball.position.x, ball.position.y - ball.radius * 1.25f, ball.character.auraColor, isCrit = true)
            }

            UltimateArchetype.ULT_NULLIFY -> {
                ultWaveStyle = AbilityVfxStyle.SLASH_CROSS
                if (rival.isUltimateActive) {
                    rival.isUltimateActive = false
                    rival.ultimateTimeRemaining = 0f
                    rival.isSeriousActive = false
                    rival.ultimateCooldownRemaining = rival.effectiveUltCooldownSec * 0.78f
                    spawnShockwave(rival.position.x, rival.position.y, Color(0xFFEF4444), rival.radius * 3.2f, AbilityVfxStyle.SLASH_CROSS, 0.46f)
                    spawnFloatingText("ULT BLOCKED!", rival.position.x, rival.position.y - rival.radius * 1.3f, Color(0xFFEF4444), isCrit = true)
                } else {
                    rival.ultimateGauge = max(0f, rival.ultimateGauge - 35f)
                    spawnFloatingText("$prefix${ball.character.ultimateName.uppercase()}!", ball.position.x, ball.position.y - ball.radius * 1.25f, Color(0xFFEF4444), isCrit = true)
                }
            }

            UltimateArchetype.HYPER_AWAKEN -> {
                ultWaveStyle = when {
                    ball.character.id == "guts_berserk" || ball.character.element == ElementTag.SLASH -> AbilityVfxStyle.SLASH_CROSS
                    ball.character.element == ElementTag.ELEC -> AbilityVfxStyle.LIGHTNING_NOVA
                    else -> AbilityVfxStyle.SHOCKWAVE
                }
                ball.speedBoostMultiplier = 1.25f
                ball.speedBoostTimer = 1.4f
                val label = if (ball.character.id == "guts_berserk") {
                    "${prefix}GIANT SWORD! BERSERKER!"
                } else {
                    "$prefix${ball.character.ultimateName.uppercase()}!"
                }
                spawnFloatingText(
                    label,
                    ball.position.x,
                    ball.position.y - ball.radius * 1.25f,
                    ball.character.auraColor,
                    isCrit = true
                )
            }
        }

        spawnShockwave(ball.position.x, ball.position.y, ball.character.auraColor, ball.radius * 4.1f, ultWaveStyle, 0.48f)
        spawnImpactBurst(ball.position.x, ball.position.y, ball.character.auraColor, engineSettings.renderMode.particleCountOnHit)
        screenShakeAmount = 20f
        onUltimateActivated(isPlayer, ball.character.ultimateName)
        checkKnockoutState()
        return true
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
        playerTrailCooldown = max(0f, playerTrailCooldown - dt)
        aiTrailCooldown = max(0f, aiTrailCooldown - dt)
        playerRedirectCooldown = max(0f, playerRedirectCooldown - rawDt)

        playerBall.delayedHp += (playerBall.currentHp - playerBall.delayedHp) * min(1f, rawDt * 5.5f)
        opponentBall.delayedHp += (opponentBall.currentHp - opponentBall.delayedHp) * min(1f, rawDt * 5.5f)
        playerBall.hitFlashTimer = max(0f, playerBall.hitFlashTimer - rawDt * 5.5f)
        opponentBall.hitFlashTimer = max(0f, opponentBall.hitFlashTimer - rawDt * 5.5f)
        playerBall.passiveFlashTimer = max(0f, playerBall.passiveFlashTimer - rawDt * 2.1f)
        opponentBall.passiveFlashTimer = max(0f, opponentBall.passiveFlashTimer - rawDt * 2.1f)

        if (!isRoundActive) {
            updateVfx(dt)
            return
        }

        if (isClashStarted) {
            updateUltimate(playerBall, dt, isPlayer = true)
            updateUltimate(opponentBall, dt, isPlayer = false)
            updateStatusEffects(playerBall, dt)
            updateStatusEffects(opponentBall, dt)
            updateHazards(dt)

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

    private fun stepNonstopMovement(ball: CombatBall, subDt: Float) {
        if (ball.isStunned) {
            ball.velocity = Vector2.Zero
            return
        }

        val targetSpeed = computeTargetCruiseSpeed(ball)
        val currentSpeed = ball.velocity.length()

        if (currentSpeed < 12f) {
            val resume = sanitizeLaunchAngle(ball.resumeDirection)
            ball.velocity = resume * targetSpeed
        } else {
            ball.resumeDirection = ball.velocity / currentSpeed
            val regulatedSpeed = when {
                currentSpeed < targetSpeed -> targetSpeed
                currentSpeed > targetSpeed * 1.48f -> targetSpeed * 1.48f
                else -> currentSpeed + (targetSpeed - currentSpeed) * min(1f, subDt * 4.5f)
            }
            ball.velocity = ball.resumeDirection * regulatedSpeed
        }

        ball.position = ball.position + (ball.velocity * subDt)
    }

    /**
     * Symmetric Ultimate state machine for BOTH Player and AI:
     * 1. Active Awakening -> drains `ultimateTimeRemaining`, then enters `effectiveUltCooldownSec`.
     * 2. Cooldown -> drains `ultimateCooldownRemaining` (gauge locked at 0%).
     * 3. Charging -> builds `ultimateGauge` from 0% to 100%.
     */
    private fun updateUltimate(ball: CombatBall, dt: Float, isPlayer: Boolean) {
        if (ball.speedBoostTimer > 0f) {
            ball.speedBoostTimer = max(0f, ball.speedBoostTimer - dt)
            if (ball.speedBoostTimer <= 0f) {
                ball.speedBoostMultiplier = 1.0f
            }
        }

        if (ball.isUltimateActive) {
            ball.ultimateTimeRemaining -= dt

            val trailCd = if (isPlayer) playerTrailCooldown else aiTrailCooldown
            val leavesTrail = ball.character.ultimateArchetype == UltimateArchetype.HAZARD_STORM ||
                ball.character.passiveArchetype == PassiveArchetype.SCORCH_BURN
            if (leavesTrail && trailCd <= 0f && !ball.isStunned) {
                if (isPlayer) playerTrailCooldown = 0.48f else aiTrailCooldown = 0.48f
                spawnFireHazard(ball.id, ball.position.x, ball.position.y, ball.radius * 0.80f, 3.2f, ball.character.auraColor)
            }

            if (ball.ultimateTimeRemaining <= 0f) {
                ball.isUltimateActive = false
                ball.ultimateTimeRemaining = 0f
                ball.isSeriousActive = false
                ball.kamuiBounceStacks = 0
                ball.ultimateGauge = 0f
                // Enter mandatory post-Ultimate cooldown (reduced by Chrono Drive CD upgrades!)
                ball.ultimateCooldownRemaining = ball.effectiveUltCooldownSec
            }
            return
        }

        if (ball.ultimateCooldownRemaining > 0f) {
            ball.ultimateCooldownRemaining = max(0f, ball.ultimateCooldownRemaining - dt)
            ball.ultimateGauge = 0f
            return
        }

        // Steady combat spirit charge while actively ricocheting
        ball.addUltimate(4.2f * dt)
    }

    private fun updateStatusEffects(ball: CombatBall, dt: Float) {
        if (ball.slowTimer > 0f) {
            ball.slowTimer = max(0f, ball.slowTimer - dt)
        }
        if (ball.shieldVisualTimer > 0f) {
            ball.shieldVisualTimer = max(0f, ball.shieldVisualTimer - dt)
            if (ball.shieldVisualTimer <= 0f) ball.isShieldActive = false
        }

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
                ball.currentHp = max(0f, ball.currentHp - 3f)
                ball.hitFlashTimer = 0.40f
                spawnFloatingText("-3 BLEED", ball.position.x, ball.position.y - ball.radius, Color(0xFFEF4444))
            }
        }

        if (ball.burnTicksRemaining > 0) {
            ball.burnTimer += dt
            if (ball.burnTimer >= 0.55f) {
                ball.burnTimer -= 0.55f
                ball.burnTicksRemaining--
                ball.currentHp = max(0f, ball.currentHp - 4f)
                ball.hitFlashTimer = 0.45f
                spawnFloatingText("-4 BURN", ball.position.x, ball.position.y - ball.radius * 0.8f, Color(0xFFF97316))
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
            ball.currentHp = max(0f, ball.currentHp - 6.5f * dt)
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
            val isPlayer = ball === playerBall
            ball.wallBounceCombo++
            ball.totalWallBounces++
            ball.addUltimate(3.6f)

            when (ball.character.passiveArchetype) {
                PassiveArchetype.ELASTIC_MOMENTUM -> {
                    ball.speedBoostMultiplier = 1.22f
                    ball.speedBoostTimer = 0.85f
                    if (ball.isUltimateActive) {
                        spawnShockwave(ball.position.x, ball.position.y, ball.character.auraColor, ball.radius * 2.0f)
                    }
                    if (ball.wallBounceCombo == 2) {
                        onPassiveTriggered(isPlayer, ball.character.passiveName)
                    }
                }
                PassiveArchetype.REGEN_IMMORTAL -> {
                    if (ball.totalWallBounces % 3 == 0 && ball.currentHp < ball.maxHp) {
                        val heal = 5f + ball.upgrades.healthLevel * 0.5f
                        ball.currentHp = min(ball.maxHp, ball.currentHp + heal)
                        spawnFloatingText("+${heal.toInt()} REGEN", ball.position.x, ball.position.y - ball.radius, Color(0xFF4ADE80))
                        onPassiveTriggered(isPlayer, ball.character.passiveName)
                    }
                }
                else -> Unit
            }

            if (ball.isUltimateActive) {
                ball.kamuiBounceStacks = min(6, ball.kamuiBounceStacks + 1)
                if (ball.character.passiveArchetype == PassiveArchetype.SCORCH_BURN) {
                    spawnFireHazard(ball.id, ball.position.x, ball.position.y, ball.radius * 0.82f, 3.0f, ball.character.auraColor)
                }
            }

            val speed = max(ball.velocity.length(), computeTargetCruiseSpeed(ball))
            val rival = if (isPlayer) opponentBall else playerBall
            val toRival = (rival.position - ball.position).normalized()
            val blendedDir = sanitizeLaunchAngle((ball.velocity.normalized() * 0.88f + toRival * 0.12f).normalized())

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

            ball.squashFactor = 0.80f
            ball.stretchFactor = 1.15f
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
        val hasWideReach = playerBall.character.weaponType in WIDE_REACH_WEAPONS ||
            opponentBall.character.weaponType in WIDE_REACH_WEAPONS
        val baseReach = if (hasWideReach) 6f else 0f
        val pUltReach = (playerBall.weaponScale - 1f).coerceAtLeast(0f) * playerBall.radius * 0.45f
        val oUltReach = (opponentBall.weaponScale - 1f).coerceAtLeast(0f) * opponentBall.radius * 0.45f
        val reachBonus = baseReach + pUltReach + oUltReach
        val minDist = playerBall.radius + opponentBall.radius + reachBonus

        if (dist >= minDist || dist < 0.0001f) return

        val normal = delta / dist

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

        val m1 = playerBall.character.massRating *
            (if (playerBall.character.passiveArchetype == PassiveArchetype.TITAN_JUGGERNAUT) 1.55f else 1f)
        val m2 = opponentBall.character.massRating *
            (if (opponentBall.character.passiveArchetype == PassiveArchetype.TITAN_JUGGERNAUT) 1.55f else 1f)

        val effectiveRel = min(velAlongNormal, -baseArenaSpeed * 0.45f)
        var impulseMag = -2.0f * effectiveRel / ((1f / m1) + (1f / m2))
        if (attacker.character.passiveArchetype == PassiveArchetype.SHOCKWAVE_KNOCKBACK) {
            impulseMag *= 1.36f
        }
        val impulseVec = normal * impulseMag

        if (!playerBall.isStunned) {
            val rawV1 = playerBall.velocity - (impulseVec / m1)
            val dir1 = sanitizeLaunchAngle(if (rawV1.length() > 5f) rawV1.normalized() else (normal * -1f))
            val boost1 = if (!isPlayerAttacker && attacker.character.passiveArchetype == PassiveArchetype.SHOCKWAVE_KNOCKBACK) 1.32f else 1.08f
            playerBall.resumeDirection = dir1
            playerBall.velocity = dir1 * (computeTargetCruiseSpeed(playerBall) * boost1)
        }

        if (!opponentBall.isStunned) {
            val rawV2 = opponentBall.velocity + (impulseVec / m2)
            val dir2 = sanitizeLaunchAngle(if (rawV2.length() > 5f) rawV2.normalized() else normal)
            val boost2 = if (isPlayerAttacker && attacker.character.passiveArchetype == PassiveArchetype.SHOCKWAVE_KNOCKBACK) 1.32f else 1.08f
            opponentBall.resumeDirection = dir2
            opponentBall.velocity = dir2 * (computeTargetCruiseSpeed(opponentBall) * boost2)
        }

        playerBall.squashFactor = 0.78f
        playerBall.stretchFactor = 1.16f
        playerBall.deformAngle = atan2(normal.y, normal.x)
        playerBall.weaponSpinBurst = 16f

        opponentBall.squashFactor = 0.78f
        opponentBall.stretchFactor = 1.16f
        opponentBall.deformAngle = atan2(-normal.y, -normal.x)
        opponentBall.weaponSpinBurst = 16f

        if (collisionCooldown > 0f) return
        collisionCooldown = 0.22f

        resolveCollisionCombat(attacker, defender, isPlayerAttacker, pSpeed, oSpeed)
        attacker.wallBounceCombo = 0
    }

    /**
     * Executes every single character's PassiveArchetype (all 18 archetypes across 224 fighters)
     * plus defender passives, upgrade scaling, and SFX/VFX feedback!
     */
    private fun resolveCollisionCombat(
        attacker: CombatBall,
        defender: CombatBall,
        isPlayerAttacker: Boolean,
        pSpeed: Float,
        oSpeed: Float
    ) {
        attacker.hitCounter++
        val bankPerBounce = if (attacker.character.passiveArchetype == PassiveArchetype.ELASTIC_MOMENTUM) 4 else 2
        val maxBank = if (attacker.character.passiveArchetype == PassiveArchetype.ELASTIC_MOMENTUM) 20 else 12
        val bankBonus = min(maxBank, attacker.wallBounceCombo * bankPerBounce)

        var damage = attacker.effectiveBaseDamage.toFloat() +
            bankBonus +
            (attacker.meruemDamageStacks * 2f) +
            (attacker.jinwooSpeedStacks * 1.5f)
        var isCrit = bankBonus >= 4
        var passiveFired = false
        var activePassiveTag = attacker.character.passiveName

        // Armed Serious One-Punch Ultimate Override
        if (attacker.isSeriousActive) {
            damage = 54f + (attacker.upgrades.powerLevel * 5f) + bankBonus
            isCrit = true
            attacker.isSeriousActive = false
            screenShakeAmount = 28f
            spawnFloatingText(
                "${attacker.character.ultimateName.uppercase()}!",
                defender.position.x,
                defender.position.y - defender.radius * 1.45f,
                Color(0xFFFACC15),
                isCrit = true
            )
        } else if (attacker.isUltimateActive) {
            damage *= attacker.effectiveUltMultiplier
            isCrit = true
        }

        // --- ATTACKER PASSIVES (All 18 PassiveArchetypes — 100% functional across all 224 characters!) ---
        when (attacker.character.passiveArchetype) {
            PassiveArchetype.SHOCKWAVE_KNOCKBACK -> {
                damage += 4f
                spawnShockwave(defender.position.x, defender.position.y, attacker.character.auraColor, defender.radius * 2.8f)
                passiveFired = true
            }

            PassiveArchetype.SCORCH_BURN -> {
                defender.burnTicksRemaining = max(defender.burnTicksRemaining, 3)
                defender.burnTimer = 0f
                spawnFireHazard(attacker.id, defender.position.x, defender.position.y, defender.radius * 0.88f, 3.6f, attacker.character.auraColor)
                passiveFired = true
            }

            PassiveArchetype.CRIMSON_BLEED -> {
                defender.bleedTicksRemaining = max(defender.bleedTicksRemaining, 4)
                defender.bleedTimer = 0f
                attacker.speedBoostMultiplier = 1.15f
                attacker.speedBoostTimer = 0.65f
                spawnShockwave(defender.position.x, defender.position.y, Color(0xFFEF4444), defender.radius * 2.3f, AbilityVfxStyle.SLASH_CROSS, 0.34f)
                passiveFired = true
            }

            PassiveArchetype.LIGHTNING_PARALYZE -> {
                damage += 3f
                if (attacker.hitCounter % 2 == 0 && !defender.isStunned) {
                    if (defender.velocity.length() > 1f) defender.resumeDirection = defender.velocity.normalized()
                    defender.stunTimeRemaining = 0.26f
                    spawnShockwave(defender.position.x, defender.position.y, Color(0xFF38BDF8), defender.radius * 2.5f, AbilityVfxStyle.LIGHTNING_NOVA, 0.36f)
                    spawnFloatingText("STUNNED!", defender.position.x, defender.position.y - defender.radius * 1.25f, Color(0xFF38BDF8))
                    passiveFired = true
                }
            }

            PassiveArchetype.ELASTIC_MOMENTUM -> {
                val mySpd = if (isPlayerAttacker) pSpeed else oSpeed
                val theirSpd = if (isPlayerAttacker) oSpeed else pSpeed
                if (mySpd >= theirSpd) {
                    damage += 4f
                    passiveFired = true
                }
            }

            PassiveArchetype.VAMPIRIC_SIPHON -> {
                val siphon = (if (attacker.isUltimateActive) 9f else 5f) + attacker.upgrades.powerLevel * 0.5f
                attacker.currentHp = min(attacker.maxHp, attacker.currentHp + siphon)
                spawnShockwave(attacker.position.x, attacker.position.y, Color(0xFF4ADE80), attacker.radius * 2.0f, AbilityVfxStyle.HEAL_BLOOM, 0.34f)
                spawnFloatingText("+${siphon.toInt()} HP", attacker.position.x, attacker.position.y - attacker.radius, Color(0xFF4ADE80))
                passiveFired = true
            }

            PassiveArchetype.ANTI_MAGIC_DRAIN -> {
                defender.ultimateGauge = max(0f, defender.ultimateGauge - 14f)
                if (defender.character.element == ElementTag.MAGIC || defender.isUltimateActive) {
                    damage += 5f
                }
                spawnShockwave(defender.position.x, defender.position.y, Color(0xFFEF4444), defender.radius * 2.2f, AbilityVfxStyle.SLASH_CROSS, 0.32f)
                spawnFloatingText("-14% ULT", defender.position.x, defender.position.y - defender.radius * 1.2f, Color(0xFFEF4444))
                passiveFired = true
            }

            PassiveArchetype.BERSERKER_RAGE -> {
                val missingRatio = ((attacker.maxHp - attacker.currentHp) / attacker.maxHp.coerceAtLeast(1f)).coerceIn(0f, 1f)
                val rageBonus = 1f + missingRatio * 0.48f
                damage *= rageBonus
                if (missingRatio >= 0.25f) {
                    isCrit = true
                    spawnShockwave(defender.position.x, defender.position.y, Color(0xFFDC2626), defender.radius * 2.6f, AbilityVfxStyle.SLASH_CROSS, 0.36f)
                    passiveFired = true
                }
            }

            PassiveArchetype.SHADOW_MONARCH_STACK -> {
                attacker.jinwooSpeedStacks = min(8, attacker.jinwooSpeedStacks + 1)
                if (attacker.isUltimateActive) {
                    attacker.currentHp = min(attacker.maxHp, attacker.currentHp + 5f)
                }
                passiveFired = true
            }

            PassiveArchetype.ADAPTIVE_EVOLUTION -> {
                attacker.meruemDamageStacks = min(8, attacker.meruemDamageStacks + 1)
                if (attacker.ultimateCooldownRemaining > 0f) {
                    attacker.ultimateCooldownRemaining = max(0f, attacker.ultimateCooldownRemaining - 0.35f)
                }
                passiveFired = true
            }

            PassiveArchetype.FROST_GRAVITY_SLOW -> {
                if (defender.slowTimer > 0f) {
                    damage += 4f
                }
                defender.slowTimer = 1.75f
                spawnShockwave(defender.position.x, defender.position.y, Color(0xFF38BDF8), defender.radius * 2.2f, AbilityVfxStyle.GRAVITY_IMPLODE, 0.35f)
                passiveFired = true
            }

            PassiveArchetype.BLACK_FLASH_CRIT -> {
                if (attacker.hitCounter % 3 == 0) {
                    damage *= 1.45f
                    isCrit = true
                    attacker.addUltimate(8f)
                    spawnShockwave(defender.position.x, defender.position.y, Color(0xFFE11D48), defender.radius * 3.3f, AbilityVfxStyle.LIGHTNING_NOVA, 0.42f)
                    spawnFloatingText("BLACK FLASH!", defender.position.x, defender.position.y - defender.radius * 1.3f, Color(0xFFE11D48), isCrit = true)
                    passiveFired = true
                } else {
                    damage += 2f
                }
            }

            PassiveArchetype.EXECUTE_SHRINE -> {
                if (defender.currentHp < defender.maxHp * 0.50f) {
                    damage *= 1.32f
                    isCrit = true
                    defender.bleedTicksRemaining = max(defender.bleedTicksRemaining, 3)
                    defender.bleedTimer = 0f
                    spawnShockwave(defender.position.x, defender.position.y, Color(0xFFE11D48), defender.radius * 2.8f, AbilityVfxStyle.SLASH_CROSS, 0.38f)
                    passiveFired = true
                }
            }

            PassiveArchetype.ARCANE_OVERCHARGE -> {
                if (attacker.ultimateGauge >= 50f || attacker.isUltimateActive) {
                    damage += 6f
                    spawnShockwave(defender.position.x, defender.position.y, attacker.character.auraColor, defender.radius * 2.4f, AbilityVfxStyle.DOMAIN_SIGIL, 0.34f)
                    passiveFired = true
                }
            }

            PassiveArchetype.TITAN_JUGGERNAUT -> {
                damage += 5f
                passiveFired = true
            }

            PassiveArchetype.INFINITY_BARRIER -> {
                attacker.isShieldActive = true
                attacker.shieldVisualTimer = 0.85f
                damage += 2f
            }

            PassiveArchetype.VECTOR_REFLECT -> {
                damage += 3f
            }

            PassiveArchetype.REGEN_IMMORTAL -> {
                if (attacker.currentHp < attacker.maxHp) {
                    attacker.currentHp = min(attacker.maxHp, attacker.currentHp + 3f)
                    passiveFired = true
                }
            }
        }

        // --- DEFENDER PASSIVES (unless attacker is firing an Anti-Magic / Domain / Nullify piercing Ultimate) ---
        val ignoresDefense = attacker.isUltimateActive && (
            attacker.character.ultimateArchetype == UltimateArchetype.ULT_NULLIFY ||
                attacker.character.ultimateArchetype == UltimateArchetype.DOMAIN_STUN
            )

        if (!ignoresDefense) {
            when (defender.character.passiveArchetype) {
                PassiveArchetype.INFINITY_BARRIER -> {
                    damage *= 0.78f
                    defender.isShieldActive = true
                    defender.shieldVisualTimer = 0.90f
                    defender.addUltimate(5f)
                    onPassiveTriggered(!isPlayerAttacker, defender.character.passiveName)
                }

                PassiveArchetype.VECTOR_REFLECT -> {
                    val reflected = max(2, (damage * 0.28f).toInt())
                    damage *= 0.86f
                    attacker.currentHp = max(0f, attacker.currentHp - reflected)
                    attacker.hitFlashTimer = 0.55f
                    defender.isShieldActive = true
                    defender.shieldVisualTimer = 0.65f
                    spawnFloatingText("-$reflected REFLECT", attacker.position.x, attacker.position.y - attacker.radius, Color(0xFF38BDF8))
                    onPassiveTriggered(!isPlayerAttacker, defender.character.passiveName)
                }

                PassiveArchetype.TITAN_JUGGERNAUT -> {
                    damage *= 0.86f
                }

                PassiveArchetype.REGEN_IMMORTAL -> {
                    if (defender.currentHp < defender.maxHp * 0.60f) {
                        defender.currentHp = min(defender.maxHp, defender.currentHp + 4f)
                    }
                }

                else -> Unit
            }

            if (defender.isShieldActive && defender.character.ultimateArchetype == UltimateArchetype.HEAL_REFORGE) {
                damage *= 0.78f
            }
        }

        val finalDmg = max(1, damage.toInt())
        defender.currentHp = max(0f, defender.currentHp - finalDmg)
        defender.hitFlashTimer = 0.9f

        if (passiveFired) {
            attacker.passiveFlashTimer = 0.65f
            onPassiveTriggered(isPlayerAttacker, activePassiveTag)
        }

        // Charge Ultimate meters symmetrically (only charges when not active and not on cooldown)
        attacker.addUltimate(15f)
        defender.addUltimate(12f)

        val contact = (playerBall.position + opponentBall.position) * 0.5f
        val hitStyle = when {
            attacker.isUltimateActive && (attacker.character.id == "guts_berserk" || attacker.character.element == ElementTag.SLASH) -> AbilityVfxStyle.SLASH_CROSS
            attacker.isUltimateActive && attacker.character.element == ElementTag.ELEC -> AbilityVfxStyle.LIGHTNING_NOVA
            else -> AbilityVfxStyle.SHOCKWAVE
        }
        spawnShockwave(contact.x, contact.y, attacker.character.auraColor, attacker.radius * (if (isCrit) 3.2f else 2.2f), hitStyle)
        spawnImpactBurst(contact.x, contact.y, attacker.character.auraColor, engineSettings.renderMode.particleCountOnHit)
        screenShakeAmount = max(screenShakeAmount, if (isCrit) 18f else 10f)

        val hitLabel = buildString {
            if (bankBonus > 0) append("BOUNCE ")
            if (isCrit) append("CRIT ")
            append("-$finalDmg")
        }
        spawnFloatingText(
            text = hitLabel,
            x = defender.position.x,
            y = defender.position.y - defender.radius * 0.95f,
            color = if (isCrit) Color(0xFFFACC15) else Color.Unspecified,
            isCrit = isCrit
        )

        onCollisionImpact(isPlayerAttacker, finalDmg, isCrit, activePassiveTag)
    }

    private fun updateProceduralVisuals(ball: CombatBall, dt: Float) {
        val speed = ball.velocity.length()

        ball.weaponScale += (ball.targetWeaponScale - ball.weaponScale) * min(1f, dt * 11f)
        ball.weaponSpinBurst = max(0f, ball.weaponSpinBurst - dt * 18f)
        val spinSpeed = 3.4f + (speed / 260f) + ball.weaponSpinBurst
        ball.weaponAngle = (ball.weaponAngle + spinSpeed * dt) % (2f * Math.PI.toFloat())

        if (speed > 40f) {
            ball.deformAngle = atan2(ball.velocity.y, ball.velocity.x)
            val speedNorm = (speed / (baseArenaSpeed * 1.6f)).coerceIn(0f, 1f)
            val targetStretch = 1f + speedNorm * 0.14f
            val targetSquash = 1f - speedNorm * 0.10f
            ball.stretchFactor += (targetStretch - ball.stretchFactor) * min(1f, dt * 14f)
            ball.squashFactor += (targetSquash - ball.squashFactor) * min(1f, dt * 14f)
        } else {
            ball.stretchFactor += (1f - ball.stretchFactor) * min(1f, dt * 14f)
            ball.squashFactor += (1f - ball.squashFactor) * min(1f, dt * 14f)
        }

        val maxTrail = engineSettings.renderMode.maxTrailNodes
        if (maxTrail <= 0) {
            if (ball.trailPositions.isNotEmpty()) ball.trailPositions.clear()
        } else if (speed > 80f) {
            ball.trailPositions.addFirst(ball.position)
            while (ball.trailPositions.size > maxTrail) {
                ball.trailPositions.removeLast()
            }
        } else if (ball.trailPositions.isNotEmpty()) {
            ball.trailPositions.removeLast()
        }
    }

    private fun spawnFireHazard(
        ownerId: String,
        x: Float,
        y: Float,
        radius: Float,
        duration: Float,
        color: Color = Color(0xFFF97316)
    ) {
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
                maxLife = duration,
                color = color
            )
        )
    }

    private fun spawnShockwave(
        x: Float,
        y: Float,
        color: Color,
        maxRadius: Float,
        style: AbilityVfxStyle = AbilityVfxStyle.SHOCKWAVE,
        duration: Float = 0.34f
    ) {
        val cap = engineSettings.renderMode.maxShockwaves
        while (shockwaves.size >= cap && shockwaves.isNotEmpty()) {
            shockwaves.removeAt(0)
        }
        shockwaves.add(
            ShockwaveRing(
                id = nextEntityId++,
                x = x,
                y = y,
                color = color,
                maxRadius = maxRadius,
                life = duration,
                maxLife = duration,
                style = style
            )
        )
    }

    private fun spawnImpactBurst(x: Float, y: Float, color: Color, count: Int) {
        val cap = engineSettings.renderMode.maxParticles
        val speedScale = max(1f, min(arenaWidth, arenaHeight) / 440f)
        for (i in 0 until count) {
            if (particles.size >= cap && particles.isNotEmpty()) {
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
                    color = if (i % 3 == 0) Color(0xFFFDE047) else color,
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
                life = 0.88f,
                maxLife = 0.88f,
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

    companion object {
        private val WIDE_REACH_WEAPONS = setOf(
            WeaponVisualType.TRIPLE_KATANA,
            WeaponVisualType.CRESCENT_SCYTHE,
            WeaponVisualType.ROYAL_GATES,
            WeaponVisualType.ALCHEMY_SPEAR,
            WeaponVisualType.EXCALIBUR_BLADE,
            WeaponVisualType.GRIMOIRE_GREATSWORD,
            WeaponVisualType.ZANGETSU_BLADE,
            WeaponVisualType.SPIRAL_DRILL
        )
    }
}
