package com.example.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.model.CharacterData
import com.example.model.CombatBall
import com.example.model.PhysicsMesh
import com.example.model.RenderMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object AnimeBallRenderer {

    private val sharedPath = Path()
    private val inkColor = Color(0xFF12151E)

    /**
     * Zero-allocation portrait renderer for roster cards, stage previews, and headers.
     */
    fun drawPreviewAvatar(
        scope: DrawScope,
        character: CharacterData,
        weaponAngle: Float = 0.65f,
        isUltimatePreview: Boolean = false
    ) {
        with(scope) {
            val r = (minOf(size.width, size.height) * 0.33f).coerceAtLeast(12f)
            val cx = size.width / 2f
            val cy = size.height / 2f + r * 0.10f
            val center = Offset(cx, cy)

            drawOval(
                color = Color.Black.copy(alpha = 0.24f),
                topLeft = Offset(cx - r * 0.78f, cy + r * 0.72f),
                size = Size(r * 1.56f, r * 0.34f)
            )

            drawCircle(
                color = character.auraColor.copy(alpha = 0.16f),
                radius = r * 1.22f,
                center = center
            )

            // Unified local space around Offset.Zero so crown, hair, hats, and face never detach
            withTransform({
                translate(center.x, center.y)
            }) {
                drawCharacterCrownAndBack(
                    char = character,
                    r = r,
                    swayRad = 0f,
                    isUlt = isUltimatePreview
                )
                drawCharacterSphereCore(
                    char = character,
                    r = r,
                    isUlt = isUltimatePreview,
                    isSerious = false,
                    hitFlash = 0f
                )
            }

            WeaponRenderer.drawWeapon(
                scope = this,
                character = character,
                center = center,
                radius = r,
                angle = weaponAngle,
                isUltimateActive = isUltimatePreview,
                spinBurst = 0f
            )
        }
    }

    /**
     * Arena combat ball renderer:
     * All head/body/hair/hat/eye details share ONE unified local origin (Offset.Zero) with
     * explicit pivot = Offset.Zero so zero details ever lag behind or separate from the sphere!
     */
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
            val char = ball.character
            val isUlt = ball.isUltimateActive

            // 1. Subtle aura speed streak (smaller than sphere so it never looks like detached parts)
            if (ball.trailPositions.isNotEmpty() && renderMode != RenderMode.PIXELIZED) {
                val count = ball.trailPositions.size
                var idx = 0
                for (pos in ball.trailPositions) {
                    val ratio = 1f - (idx.toFloat() / count)
                    drawCircle(
                        color = char.auraColor.copy(alpha = ratio * 0.14f),
                        radius = r * (0.28f + ratio * 0.34f),
                        center = Offset(pos.x, pos.y)
                    )
                    idx++
                }
            }

            // 2. Ground shadow right beneath ball
            drawOval(
                color = Color.Black.copy(alpha = 0.36f),
                topLeft = Offset(center.x - r * 0.80f, center.y + r * 0.70f),
                size = Size(r * 1.60f, r * 0.40f)
            )

            // 3. Aura halo
            val pulse = 1f + sin(globalTime * 8f) * 0.05f
            val auraColor = when {
                isUlt && char.id == "kakarot_z" -> Color(0xFF38BDF8)
                isUlt && char.id == "kamado_sun" -> Color(0xFFF97316)
                isUlt && char.id == "mugiwara_joy" -> Color.White
                else -> char.auraColor
            }
            val outerAuraR = r * (if (isUlt) 1.44f else 1.16f) * pulse
            drawCircle(
                color = auraColor.copy(alpha = if (isUlt) 0.26f else 0.12f),
                radius = outerAuraR,
                center = center
            )
            if (isUlt) {
                drawCircle(
                    color = auraColor.copy(alpha = 0.65f),
                    radius = outerAuraR,
                    center = center,
                    style = Stroke(width = 2.8f)
                )
            }

            // 4. Unified local transform with explicit pivot = Offset.Zero!
            val rotDeg = Math.toDegrees(ball.deformAngle.toDouble()).toFloat()
            val useElastic = physicsMesh == PhysicsMesh.DYNAMIC
            val sx = if (useElastic) ball.stretchFactor.coerceIn(0.86f, 1.15f) else 1f
            val sy = if (useElastic) ball.squashFactor.coerceIn(0.86f, 1.15f) else 1f
            val hairSway = (-ball.velocity.x / 3200f).coerceIn(-0.14f, 0.14f)

            withTransform({
                translate(center.x, center.y)
                if (useElastic && (sx != 1f || sy != 1f)) {
                    rotate(rotDeg, pivot = Offset.Zero)
                    scale(scaleX = sx, scaleY = sy, pivot = Offset.Zero)
                    rotate(-rotDeg, pivot = Offset.Zero)
                }
            }) {
                drawCharacterCrownAndBack(
                    char = char,
                    r = r,
                    swayRad = hairSway,
                    isUlt = isUlt
                )
                drawCharacterSphereCore(
                    char = char,
                    r = r,
                    isUlt = isUlt,
                    isSerious = ball.isSeriousActive,
                    hitFlash = ball.hitFlashTimer
                )
            }

            // 5. Tightly orbiting signature weapon (also fixed with pivot = Offset.Zero)
            WeaponRenderer.drawWeapon(
                scope = this,
                character = char,
                center = center,
                radius = r,
                angle = ball.weaponAngle,
                isUltimateActive = isUlt,
                spinBurst = ball.weaponSpinBurst
            )

            // 6. Status rings
            drawStatusOverlays(ball, center, r, globalTime)
        }
    }

    /**
     * Drawn in local sphere space centered at Offset.Zero so crowns/hats/hair/capes stay 100% locked to the ball.
     */
    private fun DrawScope.drawCharacterCrownAndBack(
        char: CharacterData,
        r: Float,
        swayRad: Float,
        isUlt: Boolean
    ) {
        // Saitama's white hero cape attached directly behind shoulders
        if (char.id == "caped_baldy") {
            sharedPath.reset()
            sharedPath.moveTo(-r * 0.72f, r * 0.20f)
            sharedPath.lineTo(-r * 0.92f + swayRad * r * 1.4f, r * 1.24f)
            sharedPath.lineTo(r * 0.92f + swayRad * r * 1.4f, r * 1.24f)
            sharedPath.lineTo(r * 0.72f, r * 0.20f)
            sharedPath.close()
            drawPath(sharedPath, Color(0xFFF8FAFC))
            drawPath(sharedPath, inkColor, style = Stroke(width = 2.4f, join = StrokeJoin.Round))
            return
        }

        // Luffy's Straw Hat locked onto top crown
        if (char.id == "mugiwara_joy" && !isUlt) {
            val brimY = -r * 0.72f
            drawOval(
                color = Color(0xFFFACC15),
                topLeft = Offset(-r * 1.04f, brimY - r * 0.20f),
                size = Size(r * 2.08f, r * 0.40f)
            )
            drawOval(
                color = inkColor,
                topLeft = Offset(-r * 1.04f, brimY - r * 0.20f),
                size = Size(r * 2.08f, r * 0.40f),
                style = Stroke(width = 2.4f)
            )
            drawArc(
                color = Color(0xFFFACC15),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(-r * 0.60f, brimY - r * 0.54f),
                size = Size(r * 1.20f, r * 0.86f)
            )
            drawLine(
                color = Color(0xFFDC2626),
                start = Offset(-r * 0.56f, brimY - r * 0.12f),
                end = Offset(r * 0.56f, brimY - r * 0.12f),
                strokeWidth = r * 0.13f,
                cap = StrokeCap.Round
            )
            return
        }

        // Frieren's elf ears & twin-tails locked to sides
        if (char.id == "frieren_zoltraak") {
            for (side in intArrayOf(-1, 1)) {
                sharedPath.reset()
                sharedPath.moveTo(side * r * 0.84f, -r * 0.12f)
                sharedPath.lineTo(side * r * 1.28f, -r * 0.22f)
                sharedPath.lineTo(side * r * 0.86f, r * 0.12f)
                sharedPath.close()
                drawPath(sharedPath, Color(0xFFFDE68A))
                drawPath(sharedPath, inkColor, style = Stroke(width = 2f))

                sharedPath.reset()
                sharedPath.moveTo(side * r * 0.64f, -r * 0.54f)
                sharedPath.lineTo(side * r * 1.40f + swayRad * r * 0.6f, r * 0.34f)
                sharedPath.lineTo(side * r * 0.92f + swayRad * r * 0.4f, r * 0.52f)
                sharedPath.close()
                drawPath(sharedPath, Color(0xFFE2E8F0))
                drawPath(sharedPath, inkColor, style = Stroke(width = 2.2f, join = StrokeJoin.Round))
            }
            return
        }

        // Alucard's crimson fedora brim
        if (char.id == "alucard_hellsing") {
            val hatY = -r * 0.74f
            drawOval(
                color = Color(0xFF991B1B),
                topLeft = Offset(-r * 1.14f, hatY - r * 0.18f),
                size = Size(r * 2.28f, r * 0.36f)
            )
            drawOval(
                color = inkColor,
                topLeft = Offset(-r * 1.14f, hatY - r * 0.18f),
                size = Size(r * 2.28f, r * 0.36f),
                style = Stroke(width = 2.4f)
            )
            return
        }

        // Jotaro's gakuran cap crown
        if (char.id == "kujo_star") {
            drawRoundRect(
                color = Color(0xFF18181B),
                topLeft = Offset(-r * 0.74f, -r * 1.04f),
                size = Size(r * 1.48f, r * 0.48f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawCircle(Color(0xFFFACC15), radius = r * 0.12f, center = Offset(-r * 0.24f, -r * 0.82f))
            return
        }

        // Mob's signature bowl-cut (rises into spikes during ???% Ultimate)
        if (char.id == "kageyama_esper" && !isUlt) {
            drawArc(
                color = Color(0xFF09090B),
                startAngle = 185f,
                sweepAngle = 170f,
                useCenter = true,
                topLeft = Offset(-r * 1.04f, -r * 1.06f),
                size = Size(r * 2.08f, r * 1.36f)
            )
            return
        }

        if (char.hairColor.alpha == 0f) return

        val hairColor = when {
            isUlt && char.id == "kakarot_z" -> Color(0xFF38BDF8)
            isUlt && char.id == "mugiwara_joy" -> Color.White
            isUlt && char.id == "kageyama_esper" -> Color(0xFF18181B)
            else -> char.hairColor
        }

        val crownCenterAngle = -PI.toFloat() * 0.5f + swayRad
        val spikeOffsets = if (char.id == "kakarot_z") {
            floatArrayOf(-0.82f, -0.46f, -0.14f, 0.20f, 0.56f, 0.88f)
        } else {
            floatArrayOf(-0.58f, -0.27f, 0f, 0.27f, 0.58f)
        }
        val spikeScale = if (char.id == "kakarot_z") 1.50f else 1.34f

        for (offset in spikeOffsets) {
            val a = crownCenterAngle + offset
            sharedPath.reset()
            sharedPath.moveTo(
                cos(a - 0.28f) * r * 0.78f,
                sin(a - 0.28f) * r * 0.78f
            )
            sharedPath.lineTo(
                cos(a) * r * spikeScale,
                sin(a) * r * spikeScale
            )
            sharedPath.lineTo(
                cos(a + 0.28f) * r * 0.78f,
                sin(a + 0.28f) * r * 0.78f
            )
            sharedPath.close()
            drawPath(sharedPath, hairColor)
            drawPath(sharedPath, inkColor, style = Stroke(width = (r * 0.06f).coerceAtLeast(2f), join = StrokeJoin.Round))
        }
    }

    /**
     * Drawn in local sphere space centered at Offset.Zero.
     */
    private fun DrawScope.drawCharacterSphereCore(
        char: CharacterData,
        r: Float,
        isUlt: Boolean,
        isSerious: Boolean,
        hitFlash: Float
    ) {
        val baseColor = if (isUlt && char.id == "mugiwara_joy") Color(0xFFF8FAFC) else char.primaryColor

        // 1. Cel-shaded base + lower shadow crescent + soft specular highlight
        drawCircle(color = baseColor, radius = r, center = Offset.Zero)
        drawArc(
            color = char.secondaryColor.copy(alpha = 0.55f),
            startAngle = 15f,
            sweepAngle = 150f,
            useCenter = true,
            topLeft = Offset(-r, -r * 0.85f),
            size = Size(r * 2f, r * 1.85f)
        )
        drawOval(
            color = Color.White.copy(alpha = 0.36f),
            topLeft = Offset(-r * 0.56f, -r * 0.70f),
            size = Size(r * 0.48f, r * 0.25f)
        )

        // 2. Character-specific anime details
        when (char.id) {
            "kakarot_z" -> {
                drawLine(Color(0xFF1D4ED8), Offset(-r * 0.54f, r * 0.44f), Offset(r * 0.54f, r * 0.44f), strokeWidth = r * 0.19f, cap = StrokeCap.Round)
                drawScowlEyes(r, if (isUlt) Color(0xFF38BDF8) else inkColor)
            }

            "kamado_sun" -> {
                val sq = r * 0.29f
                for (col in -2..1) {
                    if (col % 2 == 0) {
                        drawRect(inkColor, Offset(col * sq, r * 0.32f), Size(sq, sq * 0.68f))
                    }
                }
                drawLine(Color(0xFF991B1B), Offset(-r * 0.50f, -r * 0.52f), Offset(-r * 0.20f, -r * 0.36f), strokeWidth = r * 0.10f, cap = StrokeCap.Round)
                for (side in intArrayOf(-1, 1)) {
                    drawRect(Color.White, Offset(side * r * 0.82f - r * 0.07f, r * 0.05f), Size(r * 0.14f, r * 0.28f))
                    drawCircle(Color(0xFFDC2626), radius = r * 0.045f, center = Offset(side * r * 0.82f, r * 0.13f))
                }
                drawScowlEyes(r, Color(0xFF991B1B))
            }

            "raikiri_shinobi" -> {
                drawArc(Color(0xFF0F172A), 8f, 164f, true, Offset(-r * 0.95f, -r * 0.84f), Size(r * 1.90f, r * 1.78f))
                drawRoundRect(Color(0xFFCBD5E1), Offset(-r * 0.65f, -r * 0.62f), Size(r * 1.30f, r * 0.28f), CornerRadius(6f, 6f))
                val lx = -r * 0.32f
                val rx = r * 0.32f
                val ey = -r * 0.05f
                drawLine(Color(0xFF991B1B), Offset(lx, ey - r * 0.26f), Offset(lx, ey + r * 0.24f), strokeWidth = 2.4f)
                drawOval(Color.White, Offset(lx - r * 0.16f, ey - r * 0.11f), Size(r * 0.32f, r * 0.22f))
                drawCircle(Color(0xFFEF4444), r * 0.09f, Offset(lx, ey))
                drawOval(Color.White, Offset(rx - r * 0.16f, ey - r * 0.11f), Size(r * 0.32f, r * 0.22f))
                drawCircle(inkColor, r * 0.08f, Offset(rx, ey))
            }

            "mugiwara_joy" -> {
                drawScowlEyes(r, if (isUlt) Color(0xFFEF4444) else inkColor)
                drawLine(inkColor, Offset(-r * 0.45f, r * 0.14f), Offset(-r * 0.18f, r * 0.18f), strokeWidth = 2f)
                drawArc(Color.White, 15f, 150f, true, Offset(-r * 0.40f, r * 0.06f), Size(r * 0.80f, r * 0.44f))
                drawArc(inkColor, 15f, 150f, true, Offset(-r * 0.40f, r * 0.06f), Size(r * 0.80f, r * 0.44f), style = Stroke(width = 2f))
            }

            "satoru_void" -> {
                drawRoundRect(
                    color = Color(0xFF09090B),
                    topLeft = Offset(-r * 0.93f, -r * 0.28f),
                    size = Size(r * 1.86f, r * 0.50f),
                    cornerRadius = CornerRadius(r * 0.14f, r * 0.14f)
                )
                drawCircle(Color(0xFF38BDF8), r * 0.13f, Offset(-r * 0.32f, -r * 0.03f))
                drawCircle(Color(0xFF38BDF8), r * 0.13f, Offset(r * 0.32f, -r * 0.03f))
                drawCircle(Color.White, r * 0.05f, Offset(-r * 0.32f, -r * 0.03f))
                drawCircle(Color.White, r * 0.05f, Offset(r * 0.32f, -r * 0.03f))
            }

            "roronoa_ashura" -> {
                val lx = -r * 0.32f
                val rx = r * 0.32f
                val ey = -r * 0.04f
                drawLine(inkColor, Offset(lx - r * 0.18f, ey), Offset(lx + r * 0.18f, ey), strokeWidth = 3f, cap = StrokeCap.Round)
                drawLine(Color(0xFF7F1D1D), Offset(lx - r * 0.04f, ey - r * 0.26f), Offset(lx + r * 0.04f, ey + r * 0.26f), strokeWidth = 2.4f)
                drawOval(Color.White, Offset(rx - r * 0.18f, ey - r * 0.12f), Size(r * 0.36f, r * 0.24f))
                drawCircle(if (isUlt) Color(0xFFEF4444) else inkColor, r * 0.09f, Offset(rx, ey))
                for (e in 0..2) {
                    drawCircle(Color(0xFFFACC15), r * 0.05f, Offset(-r * 0.86f, r * (0.04f + e * 0.12f)))
                }
            }

            "chainsaw_devil" -> {
                sharedPath.reset()
                sharedPath.moveTo(-r * 0.13f, -r * 0.20f)
                sharedPath.lineTo(0f, -r * 1.40f)
                sharedPath.lineTo(r * 0.13f, -r * 0.20f)
                sharedPath.close()
                drawPath(sharedPath, Color(0xFFE4E4E7))
                drawPath(sharedPath, inkColor, style = Stroke(width = 2.4f))
                drawScowlEyes(r, Color(0xFFFACC15))
                drawRoundRect(Color.White, Offset(-r * 0.44f, r * 0.24f), Size(r * 0.88f, r * 0.22f), CornerRadius(4f, 4f))
                for (t in -2..2) {
                    val tx = t * r * 0.15f
                    drawLine(inkColor, Offset(tx, r * 0.24f), Offset(tx, r * 0.46f), strokeWidth = 2f)
                }
            }

            "dragneel_ignis" -> {
                drawRoundRect(Color(0xFFF8FAFC), Offset(-r * 0.80f, r * 0.28f), Size(r * 1.60f, r * 0.34f), CornerRadius(8f, 8f))
                for (c in -2..2) {
                    drawLine(Color(0xFF94A3B8), Offset(c * r * 0.25f, r * 0.28f), Offset(c * r * 0.25f, r * 0.62f), strokeWidth = 1.8f)
                }
                drawScowlEyes(r, Color(0xFFD97706))
            }

            "elric_fullmetal" -> {
                drawCircle(Color(0xFFD4D4D8), r * 0.20f, Offset(r * 0.60f, r * 0.36f))
                drawCircle(inkColor, r * 0.055f, Offset(r * 0.60f, r * 0.36f))
                drawScowlEyes(r, Color(0xFFEAB308))
            }

            "asta_grimoire" -> {
                drawRoundRect(Color(0xFF18181B), Offset(-r * 0.84f, -r * 0.56f), Size(r * 1.68f, r * 0.28f), CornerRadius(6f, 6f))
                drawCircle(Color(0xFFDC2626), r * 0.10f, Offset(-r * 0.40f, -r * 0.42f))
                drawScowlEyes(r, if (isUlt) Color(0xFFEF4444) else Color(0xFF16A34A))
            }

            "kurosaki_hollow" -> {
                drawArc(
                    color = Color(0xFFF8FAFC),
                    startAngle = 90f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(-r * 0.95f, -r * 0.95f),
                    size = Size(r * 1.90f, r * 1.90f)
                )
                drawLine(Color(0xFFDC2626), Offset(-r * 0.52f, -r * 0.62f), Offset(-r * 0.34f, r * 0.52f), strokeWidth = r * 0.10f, cap = StrokeCap.Round)
                drawScowlEyes(r, Color(0xFFFACC15))
            }

            "yeager_titan" -> {
                drawScowlEyes(r, Color(0xFF10B981))
                for (side in intArrayOf(-1, 1)) {
                    for (m in -1..1) {
                        val mx = side * r * 0.32f + m * r * 0.08f
                        drawLine(Color(0xFF7C2D12), Offset(mx, r * 0.14f), Offset(mx, r * 0.32f), strokeWidth = 2f)
                    }
                }
            }

            "killua_godspeed" -> {
                drawRoundRect(Color(0xFF1E1B4B), Offset(-r * 0.70f, r * 0.40f), Size(r * 1.40f, r * 0.34f), CornerRadius(6f, 6f))
                drawScowlEyes(r, Color(0xFF3B82F6))
            }

            "kageyama_esper" -> {
                if (isUlt) {
                    drawCircle(Color.White, r * 0.17f, Offset(-r * 0.32f, -r * 0.02f))
                    drawCircle(Color.White, r * 0.17f, Offset(r * 0.32f, -r * 0.02f))
                } else {
                    drawCircle(Color.White, r * 0.15f, Offset(-r * 0.30f, 0f))
                    drawCircle(Color.White, r * 0.15f, Offset(r * 0.30f, 0f))
                    drawCircle(inkColor, r * 0.055f, Offset(-r * 0.30f, 0f))
                    drawCircle(inkColor, r * 0.055f, Offset(r * 0.30f, 0f))
                }
                drawCircle(Color(0xFFFACC15), r * 0.085f, Offset(0f, r * 0.54f))
            }

            "kujo_star" -> {
                drawLine(Color(0xFF09090B), Offset(-r * 0.76f, -r * 0.25f), Offset(r * 0.76f, -r * 0.25f), strokeWidth = r * 0.15f)
                drawScowlEyes(r, Color(0xFF14B8A6))
                drawLine(Color(0xFFFACC15), Offset(-r * 0.44f, r * 0.40f), Offset(-r * 0.10f, r * 0.65f), strokeWidth = 3.2f, cap = StrokeCap.Round)
            }

            "caped_baldy" -> {
                if (isUlt || isSerious) {
                    drawScowlEyes(r, Color(0xFFEF4444))
                } else {
                    drawCircle(Color.White, r * 0.17f, Offset(-r * 0.30f, -r * 0.04f))
                    drawCircle(Color.White, r * 0.17f, Offset(r * 0.30f, -r * 0.04f))
                    drawCircle(inkColor, r * 0.05f, Offset(-r * 0.30f, -r * 0.04f))
                    drawCircle(inkColor, r * 0.05f, Offset(r * 0.30f, -r * 0.04f))
                }
                drawCircle(Color.White, r * 0.10f, Offset(-r * 0.50f, r * 0.46f))
                drawCircle(Color.White, r * 0.10f, Offset(r * 0.50f, r * 0.46f))
            }

            "alucard_hellsing" -> {
                val lx = -r * 0.32f
                val rx = r * 0.32f
                val ey = -r * 0.04f
                drawLine(inkColor, Offset(lx, ey), Offset(rx, ey), strokeWidth = 2.4f)
                drawCircle(Color(0xFFF97316), r * 0.21f, Offset(lx, ey))
                drawCircle(Color(0xFFF97316), r * 0.21f, Offset(rx, ey))
                drawCircle(inkColor, r * 0.21f, Offset(lx, ey), style = Stroke(width = 2.2f))
                drawCircle(inkColor, r * 0.21f, Offset(rx, ey), style = Stroke(width = 2.2f))
                drawLine(Color.White, Offset(-r * 0.24f, r * 0.34f), Offset(r * 0.24f, r * 0.34f), strokeWidth = 2.6f, cap = StrokeCap.Round)
            }

            "sukuna_shrine" -> {
                drawScowlEyes(r, Color(0xFFE11D48))
                drawCircle(Color(0xFFE11D48), r * 0.065f, Offset(-r * 0.35f, r * 0.19f))
                drawCircle(Color(0xFFE11D48), r * 0.065f, Offset(r * 0.35f, r * 0.19f))
                drawLine(inkColor, Offset(-r * 0.46f, -r * 0.42f), Offset(r * 0.46f, -r * 0.42f), strokeWidth = 2.8f, cap = StrokeCap.Round)
            }

            "frieren_zoltraak" -> {
                val lx = -r * 0.30f
                val rx = r * 0.30f
                val ey = -r * 0.02f
                drawOval(Color(0xFF0D9488), Offset(lx - r * 0.14f, ey - r * 0.12f), Size(r * 0.28f, r * 0.24f))
                drawOval(Color(0xFF0D9488), Offset(rx - r * 0.14f, ey - r * 0.12f), Size(r * 0.28f, r * 0.24f))
                drawCircle(Color.White, r * 0.05f, Offset(lx - r * 0.04f, ey - r * 0.04f))
                drawCircle(Color.White, r * 0.05f, Offset(rx - r * 0.04f, ey - r * 0.04f))
                drawLine(Color(0xFFCA8A04), Offset(-r * 0.62f, r * 0.44f), Offset(r * 0.62f, r * 0.44f), strokeWidth = r * 0.11f, cap = StrokeCap.Round)
                drawCircle(Color(0xFFDC2626), r * 0.10f, Offset(0f, r * 0.44f))
            }

            "jinwoo_monarch" -> {
                val lx = -r * 0.32f
                val rx = r * 0.32f
                val ey = -r * 0.04f
                drawLine(Color(0xFFA855F7), Offset(lx, ey), Offset(lx - r * 0.20f, ey - r * 0.26f), strokeWidth = 2.8f, cap = StrokeCap.Round)
                drawLine(Color(0xFFA855F7), Offset(rx, ey), Offset(rx + r * 0.20f, ey - r * 0.26f), strokeWidth = 2.8f, cap = StrokeCap.Round)
                drawOval(Color(0xFFC084FC), Offset(lx - r * 0.17f, ey - r * 0.10f), Size(r * 0.34f, r * 0.20f))
                drawOval(Color(0xFFC084FC), Offset(rx - r * 0.17f, ey - r * 0.10f), Size(r * 0.34f, r * 0.20f))
                drawCircle(Color.White, r * 0.055f, Offset(lx, ey))
                drawCircle(Color.White, r * 0.055f, Offset(rx, ey))
            }

            else -> {
                drawScowlEyes(r, inkColor)
            }
        }

        // 3. Impact flash
        if (hitFlash > 0f) {
            drawCircle(
                color = Color.White.copy(alpha = (hitFlash * 0.58f).coerceIn(0f, 0.62f)),
                radius = r,
                center = Offset.Zero
            )
        }

        // 4. Crisp manga rim outline
        drawCircle(
            color = if (isUlt) Color.White else inkColor,
            radius = r,
            center = Offset.Zero,
            style = Stroke(width = (r * 0.08f).coerceAtLeast(2.5f))
        )
    }

    private fun DrawScope.drawScowlEyes(r: Float, irisColor: Color) {
        val eyeY = -r * 0.04f
        val leftX = -r * 0.32f
        val rightX = r * 0.32f

        drawOval(Color.White, Offset(leftX - r * 0.17f, eyeY - r * 0.12f), Size(r * 0.34f, r * 0.23f))
        drawOval(Color.White, Offset(rightX - r * 0.17f, eyeY - r * 0.12f), Size(r * 0.34f, r * 0.23f))

        drawCircle(irisColor, r * 0.085f, Offset(leftX + r * 0.02f, eyeY))
        drawCircle(irisColor, r * 0.085f, Offset(rightX - r * 0.02f, eyeY))

        drawLine(
            color = inkColor,
            start = Offset(leftX - r * 0.20f, eyeY - r * 0.19f),
            end = Offset(leftX + r * 0.17f, eyeY - r * 0.08f),
            strokeWidth = (r * 0.075f).coerceAtLeast(2.2f),
            cap = StrokeCap.Round
        )
        drawLine(
            color = inkColor,
            start = Offset(rightX + r * 0.20f, eyeY - r * 0.19f),
            end = Offset(rightX - r * 0.17f, eyeY - r * 0.08f),
            strokeWidth = (r * 0.075f).coerceAtLeast(2.2f),
            cap = StrokeCap.Round
        )
    }

    private fun DrawScope.drawStatusOverlays(
        ball: CombatBall,
        center: Offset,
        r: Float,
        globalTime: Float
    ) {
        if (ball.isStunned) {
            for (i in 0 until 6) {
                val a = i * (PI.toFloat() / 3f) + globalTime * 12f
                val p1 = Offset(center.x + cos(a) * r * 0.88f, center.y + sin(a) * r * 0.88f)
                val p2 = Offset(center.x + cos(a + 0.25f) * r * 1.24f, center.y + sin(a + 0.25f) * r * 1.24f)
                drawLine(Color(0xFF38BDF8), p1, p2, strokeWidth = 3f, cap = StrokeCap.Round)
            }
        }

        if (ball.bleedTicksRemaining > 0) {
            for (i in 0 until ball.bleedTicksRemaining) {
                val a = globalTime * 4f + i * 1.57f
                val drop = Offset(center.x + cos(a) * r * 1.10f, center.y + sin(a) * r * 1.10f)
                drawCircle(Color(0xFFEF4444), radius = r * 0.09f, center = drop)
            }
        }

        if (ball.character.id == "satoru_void") {
            drawCircle(
                color = Color(0xFF818CF8).copy(alpha = 0.34f),
                radius = r * 1.18f,
                center = center,
                style = Stroke(width = 2f)
            )
        }
    }
}
