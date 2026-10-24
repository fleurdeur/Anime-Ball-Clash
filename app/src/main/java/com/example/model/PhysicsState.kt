package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2(val x: Float = 0f, val y: Float = 0f) {
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2(x * scalar, y * scalar)
    operator fun div(scalar: Float) = if (scalar != 0f) Vector2(x / scalar, y / scalar) else Vector2(0f, 0f)

    fun length(): Float = sqrt(x * x + y * y)
    fun lengthSquared(): Float = x * x + y * y

    fun normalized(): Vector2 {
        val len = length()
        return if (len > 0.0001f) Vector2(x / len, y / len) else Vector2(0f, 0f)
    }

    fun dot(other: Vector2): Float = x * other.x + y * other.y
    fun distanceTo(other: Vector2): Float = (this - other).length()

    fun rotate(radians: Float): Vector2 {
        val cosA = cos(radians)
        val sinA = sin(radians)
        return Vector2(x * cosA - y * sinA, x * sinA + y * cosA)
    }

    companion object {
        val Zero = Vector2(0f, 0f)
    }
}

data class VerletHairNode(
    var x: Float,
    var y: Float,
    var oldX: Float,
    var oldY: Float
) {
    fun update(friction: Float = 0.90f) {
        val vx = (x - oldX) * friction
        val vy = (y - oldY) * friction
        oldX = x
        oldY = y
        x += vx
        y += vy
    }
}

enum class AbilityVfxStyle {
    SHOCKWAVE,
    SLASH_CROSS,
    DOMAIN_SIGIL,
    LIGHTNING_NOVA,
    GRAVITY_IMPLODE,
    HEAL_BLOOM,
    TIME_DIAL,
    METEOR_BURST
}

data class CombatBall(
    var id: String,
    var name: String,
    var position: Vector2,
    var velocity: Vector2,
    var radius: Float = 52f,
    var mass: Float = 1.0f,
    var currentHp: Float = -1f,
    var delayedHp: Float = -1f,
    var maxHp: Float = -1f,
    var character: CharacterData,
    var upgrades: CharacterUpgradeState = CharacterUpgradeState(),
    var squashFactor: Float = 1.0f,
    var stretchFactor: Float = 1.0f,
    var deformAngle: Float = 0f,
    var weaponAngle: Float = 0f,
    var weaponSpinBurst: Float = 0f,
    var weaponScale: Float = 1.0f,
    var hitFlashTimer: Float = 0f,
    var passiveFlashTimer: Float = 0f,
    var hairNodes: List<VerletHairNode> = emptyList(),
    var ultimateGauge: Float = 0f,
    var isUltimateActive: Boolean = false,
    var ultimateTimeRemaining: Float = 0f,
    var ultimateCooldownRemaining: Float = 0f,
    var aiUltReactionTimer: Float = 0f,
    var bleedTicksRemaining: Int = 0,
    var bleedTimer: Float = 0f,
    var burnTicksRemaining: Int = 0,
    var burnTimer: Float = 0f,
    var stunTimeRemaining: Float = 0f,
    var slowTimer: Float = 0f,
    var shieldVisualTimer: Float = 0f,
    var resumeDirection: Vector2 = Vector2(0f, -1f),
    var speedBoostTimer: Float = 0f,
    var speedBoostMultiplier: Float = 1.0f,
    var kamuiBounceStacks: Int = 0,
    var jinwooSpeedStacks: Int = 0,
    var meruemDamageStacks: Int = 0,
    var hitCounter: Int = 0,
    var totalWallBounces: Int = 0,
    var isShieldActive: Boolean = false,
    var isSeriousActive: Boolean = false,
    var counterActiveTimer: Float = 0f,
    var counterCooldownRemaining: Float = 0f,
    var wallBounceCombo: Int = 0,
    var matchupBalance: LoreMatchupBalance = LoreMatchupBalance(),
    var ultVolleyTimer: Float = 0f,
    val trailPositions: ArrayDeque<Vector2> = ArrayDeque(12)
) {
    init {
        val computedMax = upgrades.effectiveMaxHp(character.maxHp).toFloat()
        if (maxHp <= 0f) {
            maxHp = computedMax
        }
        if (currentHp < 0f) {
            currentHp = maxHp
        }
        if (delayedHp < 0f) {
            delayedHp = currentHp
        }
    }

    val isDead: Boolean get() = currentHp <= 0f
    val isStunned: Boolean get() = stunTimeRemaining > 0f
    val isUltimateOnCooldown: Boolean get() = ultimateCooldownRemaining > 0f
    val canActivateUltimate: Boolean
        get() = !isUltimateActive && ultimateCooldownRemaining <= 0f && ultimateGauge >= 100f

    val counterDurationSec: Float get() = 0.85f
    val counterCooldownSec: Float
        get() = (2.8f - upgrades.cooldownLevel * 0.12f).coerceAtLeast(1.75f)
    val isCounterActive: Boolean get() = counterActiveTimer > 0.01f
    val isCounterOnCooldown: Boolean get() = counterCooldownRemaining > 0.01f
    val canActivateCounter: Boolean
        get() = !isDead && !isCounterActive && !isCounterOnCooldown

    val effectiveAutoCounterChance: Float
        get() = (
            character.baseAutoCounterChance +
                upgrades.cooldownLevel * 0.008f +
                matchupBalance.autoCounterBonusChance
            ).coerceIn(0.18f, 0.48f)

    val effectiveAutoCounterPercent: Int
        get() = (effectiveAutoCounterChance * 100f + 0.5f).toInt()

    val effectiveBaseDamage: Int
        get() = upgrades.effectiveBaseDamage(character.baseDamage)

    val effectiveUltMultiplier: Float
        get() = upgrades.effectiveUltimateMultiplier(character.ultimateDamageMultiplier)

    val effectiveUltCooldownSec: Float
        get() = upgrades.effectiveUltimateCooldown(character.ultimateCooldownSeconds)

    /**
     * Multi-Dash in Ultimate Form:
     * Normal dash cooldown is ~3.0s; during Ultimate form it drops to 0.32s for rapid multi-dashes!
     */
    val effectiveRedirectCooldownSec: Float
        get() = if (isUltimateActive) {
            0.32f
        } else {
            (3.0f - upgrades.redirectCooldownReduction()).coerceAtLeast(1.65f)
        }

    val targetWeaponScale: Float
        get() = computeUltimateWeaponScale(character, isUltimateActive)

    fun addUltimate(amount: Float) {
        if (isUltimateActive || ultimateCooldownRemaining > 0f) return
        val gainMult = when {
            character.passiveArchetype == PassiveArchetype.ARCANE_OVERCHARGE -> 1.28f
            character.passiveArchetype == PassiveArchetype.ADAPTIVE_EVOLUTION -> 1.14f
            currentHp < maxHp * 0.45f && character.alignment == CharacterAlignment.PROTAGONIST -> 1.12f
            else -> 1.0f
        } * matchupBalance.ultChargeMultiplier
        ultimateGauge = (ultimateGauge + amount * gainMult).coerceIn(0f, 100f)
    }

    companion object {
        fun computeUltimateWeaponScale(character: CharacterData, isUltimateActive: Boolean): Float {
            if (!isUltimateActive) return 1.0f
            if (character.id == "guts_berserk") return 1.92f
            if (character.id == "simon_spiral") return 1.88f
            if (character.id == "asta_grimoire") return 1.78f
            return when (character.weaponType) {
                WeaponVisualType.GRIMOIRE_GREATSWORD -> 1.76f
                WeaponVisualType.EXCALIBUR_BLADE -> 1.72f
                WeaponVisualType.SPIRAL_DRILL -> 1.80f
                WeaponVisualType.ZANGETSU_BLADE -> 1.65f
                WeaponVisualType.CRESCENT_SCYTHE -> 1.62f
                WeaponVisualType.DEMON_KANABO -> 1.60f
                WeaponVisualType.CHAINSAW_BLADE -> 1.58f
                WeaponVisualType.SERIOUS_GLOVE -> 1.64f
                WeaponVisualType.GOMU_FIST -> 1.68f
                WeaponVisualType.LIMITLESS_ORBS -> 1.66f
                WeaponVisualType.ALCHEMY_SPEAR -> 1.56f
                WeaponVisualType.NICHIRIN_KATANA,
                WeaponVisualType.TRIPLE_KATANA,
                WeaponVisualType.TITAN_BLADES -> 1.48f
                else -> 1.42f
            }
        }
    }
}

data class AbilityProjectile(
    val id: Long,
    val ownerId: String,
    val style: CanonProjectileStyle,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var angle: Float,
    val speed: Float,
    val radius: Float,
    val damage: Float,
    val primaryColor: Color,
    val secondaryColor: Color,
    var hoverTimer: Float = 0f,
    val anchorOffsetX: Float = 0f,
    val anchorOffsetY: Float = 0f,
    var life: Float = 1.6f,
    val maxLife: Float = 1.6f,
    var fadeOutTimer: Float = -1f,
    val maxFadeOut: Float = 0.34f,
    var hasHit: Boolean = false,
    val piercesThrough: Boolean = false,
    val teleportsOwnerBehindOnHit: Boolean = false
) {
    val isFadingOut: Boolean get() = fadeOutTimer >= 0f
    val currentAlpha: Float
        get() = if (fadeOutTimer >= 0f) {
            (fadeOutTimer / maxFadeOut.coerceAtLeast(0.05f)).coerceIn(0f, 1f)
        } else {
            1f
        }
}

data class Particle(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var life: Float,
    val maxLife: Float,
    val size: Float,
    val isPixel: Boolean = false
)

data class ShockwaveRing(
    val id: Long,
    val x: Float,
    val y: Float,
    val color: Color,
    val maxRadius: Float,
    var life: Float,
    val maxLife: Float,
    val style: AbilityVfxStyle = AbilityVfxStyle.SHOCKWAVE
)

data class FloatingCombatText(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var life: Float = 0.95f,
    val maxLife: Float = 0.95f,
    val isCrit: Boolean = false
)

data class FireHazardZone(
    val id: Long,
    val ownerId: String,
    val x: Float,
    val y: Float,
    val radius: Float = 52f,
    var life: Float = 4.2f,
    val maxLife: Float = 4.2f,
    val color: Color = Color(0xFFF97316)
)
