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
    fun update(friction: Float = 0.92f) {
        val vx = (x - oldX) * friction
        val vy = (y - oldY) * friction
        oldX = x
        oldY = y
        x += vx
        y += vy
    }
}

data class CombatBall(
    var id: String,
    var name: String,
    var position: Vector2,
    var velocity: Vector2,
    var radius: Float = 56f,
    var mass: Float = 1.0f,
    var currentHp: Float = 100f,
    var delayedHp: Float = 100f, // Smooth fighting-game ghost damage bar
    var maxHp: Float = 100f,
    var character: CharacterData,
    var squashFactor: Float = 1.0f,
    var stretchFactor: Float = 1.0f,
    var deformAngle: Float = 0f,
    var weaponAngle: Float = 0f,
    var weaponSpinBurst: Float = 0f,
    var hitFlashTimer: Float = 0f,
    var hairNodes: List<VerletHairNode> = emptyList(),
    var ultimateGauge: Float = 25f, // Start with 25% tactical meter
    var isUltimateActive: Boolean = false,
    var ultimateTimeRemaining: Float = 0f,
    var bleedTicksRemaining: Int = 0,
    var bleedTimer: Float = 0f,
    var stunTimeRemaining: Float = 0f,
    var isShieldActive: Boolean = false,
    var isSeriousActive: Boolean = false,
    var wallBounceCombo: Int = 0,
    var trailPositions: MutableList<Vector2> = mutableListOf()
) {
    val isDead: Boolean get() = currentHp <= 0f
    val isStunned: Boolean get() = stunTimeRemaining > 0f

    fun addUltimate(amount: Float) {
        if (!isUltimateActive) {
            ultimateGauge = (ultimateGauge + amount).coerceIn(0f, 100f)
        }
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
    val maxLife: Float
)

data class FloatingCombatText(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var life: Float = 1.1f,
    val maxLife: Float = 1.1f,
    val isCrit: Boolean = false
)

data class FireHazardZone(
    val id: Long,
    val x: Float,
    val y: Float,
    val radius: Float = 52f,
    var life: Float = 5.0f,
    val maxLife: Float = 5.0f
)
