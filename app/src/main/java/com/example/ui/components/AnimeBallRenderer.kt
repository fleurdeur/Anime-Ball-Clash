package com.example.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.model.CombatBall
import com.example.model.PhysicsMesh
import com.example.model.RenderMode
import com.example.model.Vector2
import com.example.model.VerletHairNode
import com.example.model.WeaponVisualType
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object AnimeBallRenderer {

    fun drawPreviewAvatar(
        scope: DrawScope,
        character: com.example.model.CharacterData,
        globalTime: Float = 0f
    ) {
        val r = (minOf(scope.size.width, scope.size.height) * 0.34f).coerceAtLeast(14f)
        val cx = scope.size.width / 2f
        val cy = scope.size.height / 2f + r * 0.08f
        val dummy = CombatBall(
            id = "avatar_${character.id}",
            name = character.name,
            position = Vector2(cx, cy),
            velocity = Vector2.Zero,
            radius = r,
            character = character,
            weaponAngle = globalTime * 2.2f,
            hairNodes = List(5) { idx ->
                VerletHairNode(cx, cy - r * 0.5f - idx * (r * 0.28f), cx, cy - r * 0.5f - idx * (r * 0.28f))
            }
        )
        drawBall(scope, dummy, RenderMode.BALANCED, PhysicsMesh.DYNAMIC, globalTime)
    }

    fun drawBall(
        scope: DrawScope,
        ball: CombatBall,
        renderMode: RenderMode,
        physicsMesh: PhysicsMesh,
        globalTime: Float
    ) {
        with(scope) {
            val center = Offset(ball.position.x, ball.position.y)
            val r = ball.radius

            // 0. MOTION AFTERIMAGE TRAIL
            if (ball.trailPositions.isNotEmpty() && renderMode != RenderMode.PIXELIZED) {
                val count = ball.trailPositions.size
                ball.trailPositions.forEachIndexed { idx, pos ->
                    val ratio = 1f - (idx.toFloat() / count)
                    drawCircle(
                        color = ball.character.auraColor.copy(alpha = ratio * 0.26f),
                        radius = r * (0.55f + ratio * 0.42f),
                        center = Offset(pos.x, pos.y)
                    )
                }
            }

            // 1. DROP SHADOW ON ARENA FLOOR
            drawOval(
                color = Color.Black.copy(alpha = 0.42f),
                topLeft = Offset(center.x - r * 0.85f, center.y + r * 0.65f),
                size = Size(r * 1.7f, r * 0.52f)
            )

            // 2. KI / CURSED AURA FLARE
            drawAura(ball, center, r, renderMode, globalTime)

            // 3. CAPE / VERLET HAIR BEHIND SPHERE
            if (ball.character.id == "one_punch") {
                drawHeroCape(ball, center, r, globalTime)
            }
            if (physicsMesh == PhysicsMesh.DYNAMIC && ball.hairNodes.isNotEmpty()) {
                drawVerletAnimeHair(ball, center, r)
            }

            // 4. SQUASH & STRETCH CEL-SHADED SPHERE BODY
            val rotDeg = Math.toDegrees(ball.deformAngle.toDouble()).toFloat()
            withTransform({
                translate(center.x, center.y)
                rotate(rotDeg)
                scale(scaleX = ball.stretchFactor, scaleY = ball.squashFactor, pivot = Offset.Zero)
                rotate(-rotDeg)
            }) {
                drawCelShadedSphere(ball, Offset.Zero, r, renderMode, globalTime)
            }

            // 5. ORBITING CANON WEAPON + SLASH ARC
            drawOrbitingWeapon(ball, center, r, globalTime)

            // 6. STATUS OVERLAYS (Stun, Bleed, Barrier)
            drawStatusEffects(ball, center, r, globalTime)
        }
    }

    private fun DrawScope.drawHeroCape(ball: CombatBall, center: Offset, radius: Float, globalTime: Float) {
        val vel = ball.velocity
        val dirAngle = if (vel.length() > 30f) atan2(-vel.y, -vel.x) else (PI.toFloat() * 0.5f)
        val flutter = sin(globalTime * 11f) * 0.18f
        val tipDist = radius * 1.85f
        val leftShoulder = Offset(
            center.x + cos(dirAngle - 0.9f) * radius * 0.75f,
            center.y + sin(dirAngle - 0.9f) * radius * 0.75f
        )
        val rightShoulder = Offset(
            center.x + cos(dirAngle + 0.9f) * radius * 0.75f,
            center.y + sin(dirAngle + 0.9f) * radius * 0.75f
        )
        val tipLeft = Offset(
            center.x + cos(dirAngle - 0.38f + flutter) * tipDist,
            center.y + sin(dirAngle - 0.38f + flutter) * tipDist
        )
        val tipRight = Offset(
            center.x + cos(dirAngle + 0.38f + flutter) * tipDist,
            center.y + sin(dirAngle + 0.38f + flutter) * tipDist
        )
        val cape = Path().apply {
            moveTo(leftShoulder.x, leftShoulder.y)
            lineTo(tipLeft.x, tipLeft.y)
            lineTo(tipRight.x, tipRight.y)
            lineTo(rightShoulder.x, rightShoulder.y)
            close()
        }
        drawPath(cape, color = Color(0xFFE53935))
        drawPath(cape, color = Color(0xFF1A0D12), style = Stroke(width = 3f))
    }

    private fun DrawScope.drawAura(
        ball: CombatBall,
        center: Offset,
        radius: Float,
        renderMode: RenderMode,
        globalTime: Float
    ) {
        val isUlt = ball.isUltimateActive
        val pulse = sin(globalTime * 9f) * 0.12f + 1.0f
        val auraColor = when {
            isUlt && ball.character.id == "saiyan_spirit" -> Color(0xFF00F0FF)
            isUlt && ball.character.id == "demon_slash" -> Color(0xFFFF5E1A)
            else -> ball.character.auraColor
        }

        val auraRadius = radius * (if (isUlt) 1.72f else 1.32f) * pulse

        if (renderMode == RenderMode.PIXELIZED) {
            drawCircle(
                color = auraColor.copy(alpha = if (isUlt) 0.75f else 0.42f),
                radius = auraRadius,
                center = center,
                style = Stroke(width = 4f)
            )
            return
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    auraColor.copy(alpha = if (isUlt) 0.78f else 0.38f),
                    auraColor.copy(alpha = if (isUlt) 0.32f else 0.12f),
                    Color.Transparent
                ),
                center = center,
                radius = auraRadius
            ),
            radius = auraRadius,
            center = center
        )

        if (isUlt || renderMode == RenderMode.REALISTIC) {
            val count = if (isUlt) 8 else 4
            for (i in 0 until count) {
                val a = globalTime * (if (isUlt) 6f else 3f) + (i * 2f * PI.toFloat() / count)
                val dist = radius * (1.18f + (i % 2) * 0.18f)
                drawCircle(
                    color = if (i % 2 == 0) Color.White else auraColor,
                    radius = radius * (if (isUlt) 0.11f else 0.06f),
                    center = Offset(center.x + cos(a) * dist, center.y + sin(a) * dist)
                )
            }
        }
    }

    private fun DrawScope.drawVerletAnimeHair(
        ball: CombatBall,
        center: Offset,
        radius: Float
    ) {
        val char = ball.character
        if (char.hairColor.alpha == 0f || ball.hairNodes.size < 3) return

        val hairColor = when {
            ball.isUltimateActive && char.id == "saiyan_spirit" -> Color(0xFF00F0FF)
            ball.isUltimateActive && char.id == "demon_slash" -> Color(0xFFFF5E1A)
            ball.isUltimateActive && char.id == "rubber_stretch" -> Color.White
            else -> char.hairColor
        }

        val tip = ball.hairNodes.last()
        val mid = ball.hairNodes[ball.hairNodes.size / 2]
        val dx = tip.x - center.x
        val dy = tip.y - center.y
        val baseAngle = atan2(dy, dx)

        // Draw 5 spiky cel-shaded anime hair locks fanning around the Verlet trail axis
        val lockAngles = floatArrayOf(-0.62f, -0.30f, 0f, 0.30f, 0.62f)
        val lockLengths = floatArrayOf(0.82f, 1.05f, 1.22f, 1.05f, 0.82f)

        for (i in lockAngles.indices) {
            val angle = baseAngle + lockAngles[i]
            val lenScale = lockLengths[i]
            val spikeTip = Offset(
                center.x + (dx * lenScale) + cos(angle) * radius * 0.55f,
                center.y + (dy * lenScale) + sin(angle) * radius * 0.55f
            )
            val rootLeft = Offset(
                center.x + cos(angle - 0.36f) * radius * 0.78f,
                center.y + sin(angle - 0.36f) * radius * 0.78f
            )
            val rootRight = Offset(
                center.x + cos(angle + 0.36f) * radius * 0.78f,
                center.y + sin(angle + 0.36f) * radius * 0.78f
            )
            val ctrl = Offset(
                (mid.x + spikeTip.x) * 0.5f,
                (mid.y + spikeTip.y) * 0.5f
            )

            val spike = Path().apply {
                moveTo(rootLeft.x, rootLeft.y)
                quadraticBezierTo(ctrl.x, ctrl.y, spikeTip.x, spikeTip.y)
                lineTo(rootRight.x, rootRight.y)
                close()
            }

            drawPath(spike, color = hairColor)
            drawPath(
                spike,
                color = Color(0xFF0A0C14),
                style = Stroke(width = (radius * 0.065f).coerceAtLeast(2f), join = StrokeJoin.Round)
            )
        }
    }

    private fun DrawScope.drawCelShadedSphere(
        ball: CombatBall,
        center: Offset,
        radius: Float,
        renderMode: RenderMode,
        globalTime: Float
    ) {
        val char = ball.character
        val isUlt = ball.isUltimateActive
        val bodyColor = if (isUlt && char.id == "rubber_stretch") Color(0xFFF5F7FF) else char.primaryColor

        // Base cel-shaded gradient sphere
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.72f),
                    bodyColor,
                    char.secondaryColor
                ),
                center = Offset(center.x - radius * 0.32f, center.y - radius * 0.35f),
                radius = radius * 1.38f
            ),
            radius = radius,
            center = center
        )

        // Signature Archetype Markings & Expressive Shonen Eyes
        when (char.id) {
            "saiyan_spirit" -> {
                // Blue martial arts sash across lower torso
                drawArc(
                    color = Color(0xFF1565C0),
                    startAngle = 22f,
                    sweepAngle = 136f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.82f, center.y - radius * 0.65f),
                    size = Size(radius * 1.64f, radius * 1.48f),
                    style = Stroke(width = radius * 0.24f, cap = StrokeCap.Round)
                )
                drawAnimeScowlEyes(center, radius, eyeColor = if (isUlt) Color(0xFF00F0FF) else Color(0xFF11131D))
            }

            "demon_slash" -> {
                // Checkered green/black lower band
                val sq = radius * 0.34f
                for (col in -2..1) {
                    if (col % 2 == 0) {
                        drawRect(
                            color = Color(0xFF12151C),
                            topLeft = Offset(center.x + col * sq, center.y + radius * 0.24f),
                            size = Size(sq, sq * 0.85f)
                        )
                    }
                }
                // Crimson mark on upper-left brow
                drawLine(
                    color = Color(0xFFB71C1C),
                    start = Offset(center.x - radius * 0.52f, center.y - radius * 0.52f),
                    end = Offset(center.x - radius * 0.22f, center.y - radius * 0.36f),
                    strokeWidth = radius * 0.10f,
                    cap = StrokeCap.Round
                )
                drawAnimeScowlEyes(center, radius, eyeColor = Color(0xFFD50000))
            }

            "six_glow_magic" -> {
                // Jet-black jujutsu blindfold + glowing cyan Six-Eyes slit
                drawRoundRect(
                    color = Color(0xFF0B0D17),
                    topLeft = Offset(center.x - radius * 0.94f, center.y - radius * 0.32f),
                    size = Size(radius * 1.88f, radius * 0.56f),
                    cornerRadius = CornerRadius(radius * 0.16f, radius * 0.16f)
                )
                val eyeGlow = if (isUlt) 1f else (0.65f + sin(globalTime * 6f) * 0.25f)
                drawCircle(
                    color = Color(0xFF00F0FF).copy(alpha = eyeGlow),
                    radius = radius * 0.13f,
                    center = Offset(center.x - radius * 0.32f, center.y - radius * 0.04f)
                )
                drawCircle(
                    color = Color(0xFF00F0FF).copy(alpha = eyeGlow),
                    radius = radius * 0.13f,
                    center = Offset(center.x + radius * 0.32f, center.y - radius * 0.04f)
                )
            }

            "lightning_ninja" -> {
                // Ninja dark mask on lower half
                drawArc(
                    color = Color(0xFF1A2234),
                    startAngle = 8f,
                    sweepAngle = 164f,
                    useCenter = true,
                    topLeft = Offset(center.x - radius * 0.96f, center.y - radius * 0.90f),
                    size = Size(radius * 1.92f, radius * 1.86f)
                )
                // Metallic ninja forehead plate
                drawRoundRect(
                    color = Color(0xFFB0BEC5),
                    topLeft = Offset(center.x - radius * 0.68f, center.y - radius * 0.68f),
                    size = Size(radius * 1.36f, radius * 0.34f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawAnimeScowlEyes(center, radius, eyeColor = Color(0xFF00F0FF))
            }

            "one_punch" -> {
                // Iconic deadpan vs serious eyes!
                if (isUlt || ball.isSeriousActive) {
                    drawAnimeScowlEyes(center, radius, eyeColor = Color(0xFFFF1E42))
                } else {
                    // Deadpan oval eyes
                    drawCircle(Color.White, radius * 0.19f, Offset(center.x - radius * 0.30f, center.y - radius * 0.05f))
                    drawCircle(Color.White, radius * 0.19f, Offset(center.x + radius * 0.30f, center.y - radius * 0.05f))
                    drawCircle(Color.Black, radius * 0.06f, Offset(center.x - radius * 0.30f, center.y - radius * 0.05f))
                    drawCircle(Color.Black, radius * 0.06f, Offset(center.x + radius * 0.30f, center.y - radius * 0.05f))
                }
                // Specular dome shine
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = radius * 0.20f,
                    center = Offset(center.x - radius * 0.36f, center.y - radius * 0.45f)
                )
            }

            else -> {
                drawAnimeScowlEyes(center, radius, eyeColor = Color(0xFF11131D))
            }
        }

        // Hit impact white flash overlay
        if (ball.hitFlashTimer > 0f) {
            drawCircle(
                color = Color.White.copy(alpha = (ball.hitFlashTimer * 0.65f).coerceIn(0f, 0.7f)),
                radius = radius,
                center = center
            )
        }

        // Crisp Manga Ink Rim Outline
        drawCircle(
            color = if (isUlt) Color.White else Color(0xFF090B14),
            radius = radius,
            center = center,
            style = Stroke(width = (radius * 0.085f).coerceAtLeast(3f))
        )
    }

    private fun DrawScope.drawAnimeScowlEyes(center: Offset, radius: Float, eyeColor: Color) {
        val eyeY = center.y - radius * 0.04f
        val leftX = center.x - radius * 0.32f
        val rightX = center.x + radius * 0.32f

        // Sclera
        drawOval(
            color = Color.White,
            topLeft = Offset(leftX - radius * 0.18f, eyeY - radius * 0.13f),
            size = Size(radius * 0.36f, radius * 0.25f)
        )
        drawOval(
            color = Color.White,
            topLeft = Offset(rightX - radius * 0.18f, eyeY - radius * 0.13f),
            size = Size(radius * 0.36f, radius * 0.25f)
        )
        // Iris
        drawCircle(eyeColor, radius * 0.09f, Offset(leftX + radius * 0.02f, eyeY))
        drawCircle(eyeColor, radius * 0.09f, Offset(rightX - radius * 0.02f, eyeY))

        // Angled fierce eyebrows
        drawLine(
            color = Color(0xFF090B14),
            start = Offset(leftX - radius * 0.22f, eyeY - radius * 0.20f),
            end = Offset(leftX + radius * 0.18f, eyeY - radius * 0.09f),
            strokeWidth = (radius * 0.08f).coerceAtLeast(2.5f),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF090B14),
            start = Offset(rightX + radius * 0.22f, eyeY - radius * 0.20f),
            end = Offset(rightX - radius * 0.18f, eyeY - radius * 0.09f),
            strokeWidth = (radius * 0.08f).coerceAtLeast(2.5f),
            cap = StrokeCap.Round
        )
    }

    private fun DrawScope.drawOrbitingWeapon(
        ball: CombatBall,
        center: Offset,
        radius: Float,
        globalTime: Float
    ) {
        val angle = ball.weaponAngle
        val orbitDist = radius * 1.38f
        val weaponPos = Offset(
            center.x + cos(angle) * orbitDist,
            center.y + sin(angle) * orbitDist
        )

        // Draw slash arc trail when spinning rapidly on hit/launch
        if (ball.weaponSpinBurst > 2f) {
            val deg = Math.toDegrees(angle.toDouble()).toFloat()
            drawArc(
                color = ball.character.auraColor.copy(alpha = 0.65f),
                startAngle = deg - 110f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(center.x - orbitDist, center.y - orbitDist),
                size = Size(orbitDist * 2f, orbitDist * 2f),
                style = Stroke(width = radius * 0.14f, cap = StrokeCap.Round)
            )
        }

        val scaleUnit = radius * 0.32f
        when (ball.character.weaponType) {
            WeaponVisualType.KATANA_SHEATH -> {
                val rot = Math.toDegrees(angle.toDouble()).toFloat() + 90f
                withTransform({
                    translate(weaponPos.x, weaponPos.y)
                    rotate(rot)
                }) {
                    drawLine(
                        color = if (ball.isUltimateActive) Color(0xFFFF5E1A) else Color(0xFF00F0FF),
                        start = Offset(0f, -scaleUnit * 1.7f),
                        end = Offset(0f, scaleUnit * 0.9f),
                        strokeWidth = scaleUnit * 0.32f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFFFC800),
                        start = Offset(-scaleUnit * 0.55f, scaleUnit * 0.9f),
                        end = Offset(scaleUnit * 0.55f, scaleUnit * 0.9f),
                        strokeWidth = scaleUnit * 0.25f
                    )
                    drawLine(
                        color = Color(0xFF1A1D2B),
                        start = Offset(0f, scaleUnit * 0.9f),
                        end = Offset(0f, scaleUnit * 1.65f),
                        strokeWidth = scaleUnit * 0.28f,
                        cap = StrokeCap.Round
                    )
                }
            }

            WeaponVisualType.ENERGY_RINGS -> {
                val c = if (ball.isUltimateActive) Color(0xFF00F0FF) else Color(0xFFFFC800)
                drawCircle(c, radius = scaleUnit * 0.85f, center = weaponPos, style = Stroke(width = scaleUnit * 0.28f))
                drawCircle(Color.White, radius = scaleUnit * 0.36f, center = weaponPos)
            }

            WeaponVisualType.MAGIC_ORBS -> {
                val oppPos = Offset(
                    center.x + cos(angle + PI.toFloat()) * orbitDist,
                    center.y + sin(angle + PI.toFloat()) * orbitDist
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color.White, Color(0xFFFF1E42)), weaponPos, scaleUnit * 0.85f),
                    radius = scaleUnit * 0.85f,
                    center = weaponPos
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color.White, Color(0xFF00B0FF)), oppPos, scaleUnit * 0.85f),
                    radius = scaleUnit * 0.85f,
                    center = oppPos
                )
            }

            WeaponVisualType.ELECTRIC_KUNAI -> {
                val rot = Math.toDegrees(angle.toDouble()).toFloat() + 90f
                withTransform({
                    translate(weaponPos.x, weaponPos.y)
                    rotate(rot)
                }) {
                    val kunai = Path().apply {
                        moveTo(0f, -scaleUnit * 1.4f)
                        lineTo(scaleUnit * 0.55f, 0f)
                        lineTo(0f, scaleUnit * 0.35f)
                        lineTo(-scaleUnit * 0.55f, 0f)
                        close()
                    }
                    drawPath(kunai, Color(0xFFECEFF1))
                    drawCircle(Color(0xFF00F0FF), radius = scaleUnit * 0.32f, center = Offset(0f, scaleUnit * 0.72f), style = Stroke(width = 2.5f))
                }
            }

            WeaponVisualType.BOXING_GLOVE, WeaponVisualType.RUBBER_FIST -> {
                drawCircle(Color(0xFFE53935), radius = scaleUnit * 0.95f, center = weaponPos)
                drawCircle(Color(0xFF0D0F18), radius = scaleUnit * 0.95f, center = weaponPos, style = Stroke(width = 2.5f))
                drawCircle(Color.White.copy(alpha = 0.75f), radius = scaleUnit * 0.30f, center = Offset(weaponPos.x - scaleUnit * 0.25f, weaponPos.y - scaleUnit * 0.25f))
            }

            else -> {
                drawCircle(ball.character.auraColor, radius = scaleUnit * 0.8f, center = weaponPos)
                drawCircle(Color.White, radius = scaleUnit * 0.35f, center = weaponPos)
            }
        }
    }

    private fun DrawScope.drawStatusEffects(
        ball: CombatBall,
        center: Offset,
        radius: Float,
        globalTime: Float
    ) {
        if (ball.isStunned) {
            for (i in 0 until 6) {
                val a = i * (PI.toFloat() / 3f) + globalTime * 15f
                val p1 = Offset(center.x + cos(a) * radius * 0.85f, center.y + sin(a) * radius * 0.85f)
                val p2 = Offset(center.x + cos(a + 0.25f) * radius * 1.32f, center.y + sin(a + 0.25f) * radius * 1.32f)
                drawLine(Color(0xFF00F0FF), p1, p2, strokeWidth = 3.5f, cap = StrokeCap.Round)
            }
        }

        if (ball.bleedTicksRemaining > 0) {
            for (i in 0 until ball.bleedTicksRemaining) {
                val a = globalTime * 4f + i * 1.6f
                val drop = Offset(center.x + cos(a) * radius * 1.12f, center.y + sin(a) * radius * 1.12f)
                drawCircle(Color(0xFFFF1E42), radius = radius * 0.11f, center = drop)
            }
        }

        if (ball.character.id == "six_glow_magic") {
            drawCircle(
                color = Color(0xFFB388FF).copy(alpha = 0.36f + sin(globalTime * 5f) * 0.12f),
                radius = radius * 1.24f,
                center = center,
                style = Stroke(width = 2.5f)
            )
        }
    }
}
