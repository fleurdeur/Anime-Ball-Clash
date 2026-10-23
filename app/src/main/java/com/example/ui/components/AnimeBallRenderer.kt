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

            // 1. Subtle aura speed streak
            if (ball.trailPositions.isNotEmpty() && renderMode != RenderMode.PIXELIZED) {
                val count = ball.trailPositions.size
                var idx = 0
                for (pos in ball.trailPositions) {
                    val ratio = 1f - (idx.toFloat() / count)
                    drawCircle(
                        color = char.auraColor.copy(alpha = ratio * 0.13f),
                        radius = r * (0.26f + ratio * 0.32f),
                        center = Offset(pos.x, pos.y)
                    )
                    idx++
                }
            }

            // 2. Ground shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.36f),
                topLeft = Offset(center.x - r * 0.80f, center.y + r * 0.70f),
                size = Size(r * 1.60f, r * 0.40f)
            )

            // 3. Aura halo & Ultimate-ready ring
            val pulse = 1f + sin(globalTime * 8f) * 0.05f
            val auraColor = when {
                isUlt && char.id == "kakarot_z" -> Color(0xFF38BDF8)
                isUlt && char.id == "kamado_sun" -> Color(0xFFF97316)
                isUlt && char.id == "mugiwara_joy" -> Color.White
                isUlt && char.id == "frieza_emperor" -> Color(0xFFFACC15)
                else -> char.auraColor
            }
            val outerAuraR = r * (if (isUlt) 1.44f else 1.16f) * pulse
            drawCircle(
                color = auraColor.copy(alpha = if (isUlt) 0.28f else 0.12f),
                radius = outerAuraR,
                center = center
            )
            if (isUlt) {
                drawCircle(
                    color = auraColor.copy(alpha = 0.72f),
                    radius = outerAuraR,
                    center = center,
                    style = Stroke(width = 3.0f)
                )
            } else if (ball.canActivateUltimate) {
                drawCircle(
                    color = Color(0xFFFACC15).copy(alpha = 0.45f + sin(globalTime * 10f) * 0.20f),
                    radius = r * 1.28f,
                    center = center,
                    style = Stroke(width = 2.2f)
                )
            }

            // 4. Unified local transform with explicit pivot = Offset.Zero
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

            // 5. Signature orbiting weapon (locked with pivot = Offset.Zero)
            WeaponRenderer.drawWeapon(
                scope = this,
                character = char,
                center = center,
                radius = r,
                angle = ball.weaponAngle,
                isUltimateActive = isUlt,
                spinBurst = ball.weaponSpinBurst
            )

            // 6. Status overlays
            drawStatusOverlays(ball, center, r, globalTime)
        }
    }

    private fun DrawScope.drawCharacterCrownAndBack(
        char: CharacterData,
        r: Float,
        swayRad: Float,
        isUlt: Boolean
    ) {
        // Saitama's white hero cape or Tetsuo's crimson Akira cape
        if (char.id == "caped_baldy" || char.id == "tetsuo_psychic") {
            val capeColor = if (char.id == "tetsuo_psychic") Color(0xFFB91C1C) else Color(0xFFF8FAFC)
            sharedPath.reset()
            sharedPath.moveTo(-r * 0.72f, r * 0.20f)
            sharedPath.lineTo(-r * 0.92f + swayRad * r * 1.3f, r * 1.24f)
            sharedPath.lineTo(r * 0.92f + swayRad * r * 1.3f, r * 1.24f)
            sharedPath.lineTo(r * 0.72f, r * 0.20f)
            sharedPath.close()
            drawPath(sharedPath, capeColor)
            drawPath(sharedPath, inkColor, style = Stroke(width = 2.4f, join = StrokeJoin.Round))
            if (char.id == "caped_baldy") return
        }

        // Accelerator's Dark Vector Wings during Ultimate!
        if (char.id == "accelerator_vector" && isUlt) {
            for (side in intArrayOf(-1, 1)) {
                sharedPath.reset()
                sharedPath.moveTo(side * r * 0.65f, -r * 0.10f)
                sharedPath.lineTo(side * r * 1.65f, -r * 0.75f)
                sharedPath.lineTo(side * r * 1.35f, r * 0.25f)
                sharedPath.close()
                drawPath(sharedPath, Color(0xFF09090B))
                drawPath(sharedPath, Color(0xFFEF4444), style = Stroke(width = 2f))
            }
        }

        // Luffy's Straw Hat
        if (char.id == "mugiwara_joy" && !isUlt) {
            val brimY = -r * 0.72f
            drawOval(Color(0xFFFACC15), Offset(-r * 1.04f, brimY - r * 0.20f), Size(r * 2.08f, r * 0.40f))
            drawOval(inkColor, Offset(-r * 1.04f, brimY - r * 0.20f), Size(r * 2.08f, r * 0.40f), style = Stroke(width = 2.4f))
            drawArc(Color(0xFFFACC15), 180f, 180f, true, Offset(-r * 0.60f, brimY - r * 0.54f), Size(r * 1.20f, r * 0.86f))
            drawLine(Color(0xFFDC2626), Offset(-r * 0.56f, brimY - r * 0.12f), Offset(r * 0.56f, brimY - r * 0.12f), strokeWidth = r * 0.13f, cap = StrokeCap.Round)
            return
        }

        // Muzan's white trilby fedora OR Alucard's crimson fedora
        if (char.id == "alucard_hellsing" || (char.id == "muzan_progenitor" && !isUlt)) {
            val hatColor = if (char.id == "muzan_progenitor") Color(0xFFF8FAFC) else Color(0xFF991B1B)
            val bandColor = if (char.id == "muzan_progenitor") Color(0xFF09090B) else Color(0xFF450A0A)
            val hatY = -r * 0.74f
            drawOval(hatColor, Offset(-r * 1.12f, hatY - r * 0.18f), Size(r * 2.24f, r * 0.36f))
            drawOval(inkColor, Offset(-r * 1.12f, hatY - r * 0.18f), Size(r * 2.24f, r * 0.36f), style = Stroke(width = 2.4f))
            drawArc(hatColor, 180f, 180f, true, Offset(-r * 0.62f, hatY - r * 0.52f), Size(r * 1.24f, r * 0.80f))
            drawLine(bandColor, Offset(-r * 0.58f, hatY - r * 0.12f), Offset(r * 0.58f, hatY - r * 0.12f), strokeWidth = r * 0.12f, cap = StrokeCap.Round)
            return
        }

        // Blackbeard's pirate captain tricorne hat
        if (char.id == "blackbeard_tremor") {
            sharedPath.reset()
            sharedPath.moveTo(-r * 1.15f, -r * 0.62f)
            sharedPath.lineTo(0f, -r * 1.26f)
            sharedPath.lineTo(r * 1.15f, -r * 0.62f)
            sharedPath.close()
            drawPath(sharedPath, Color(0xFF18181B))
            drawPath(sharedPath, Color(0xFFFACC15), style = Stroke(width = 2.4f, join = StrokeJoin.Round))
            return
        }

        // Frieren's elf ears & twin-tails
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

        // Jotaro's gakuran cap
        if (char.id == "kujo_star") {
            drawRoundRect(Color(0xFF18181B), Offset(-r * 0.74f, -r * 1.04f), Size(r * 1.48f, r * 0.48f), CornerRadius(8f, 8f))
            drawCircle(Color(0xFFFACC15), radius = r * 0.12f, center = Offset(-r * 0.24f, -r * 0.82f))
            return
        }

        // Ainz's golden Overlord horns / Meruem's ear pods / Frieza's ear pods
        if (char.id == "ainz_overlord" || char.id == "frieza_emperor" || char.id == "meruem_chimera") {
            val podColor = when (char.id) {
                "ainz_overlord" -> Color(0xFFFACC15)
                "meruem_chimera" -> Color(0xFF166534)
                else -> Color(0xFF7E22CE)
            }
            for (side in intArrayOf(-1, 1)) {
                drawOval(podColor, Offset(side * r * 0.94f - r * 0.14f, -r * 0.18f), Size(r * 0.28f, r * 0.42f))
                drawOval(inkColor, Offset(side * r * 0.94f - r * 0.14f, -r * 0.18f), Size(r * 0.28f, r * 0.42f), style = Stroke(width = 2f))
            }
            return
        }

        // Caiman's dorsal reptile spikes
        if (char.id == "caiman_dorohedoro") {
            for (s in -1..1) {
                val a = -PI.toFloat() * 0.5f + s * 0.34f
                sharedPath.reset()
                sharedPath.moveTo(cos(a - 0.16f) * r * 0.86f, sin(a - 0.16f) * r * 0.86f)
                sharedPath.lineTo(cos(a) * r * 1.24f, sin(a) * r * 1.24f)
                sharedPath.lineTo(cos(a + 0.16f) * r * 0.86f, sin(a + 0.16f) * r * 0.86f)
                sharedPath.close()
                drawPath(sharedPath, Color(0xFF365314))
                drawPath(sharedPath, inkColor, style = Stroke(width = 2f))
            }
            return
        }

        // Mob's bowl-cut
        if (char.id == "kageyama_esper" && !isUlt) {
            drawArc(Color(0xFF09090B), 185f, 170f, true, Offset(-r * 1.04f, -r * 1.06f), Size(r * 2.08f, r * 1.36f))
            return
        }

        if (char.hairColor.alpha == 0f) return

        val hairColor = when {
            isUlt && char.id == "kakarot_z" -> Color(0xFF38BDF8)
            isUlt && char.id == "mugiwara_joy" -> Color.White
            isUlt && char.id == "ryuko_scissor" -> Color(0xFFFACC15)
            isUlt && char.id == "kageyama_esper" -> Color(0xFF18181B)
            else -> char.hairColor
        }

        val crownCenterAngle = -PI.toFloat() * 0.5f + swayRad
        val spikeOffsets = when (char.id) {
            "kakarot_z", "madara_uchiha", "vash_stampede" -> floatArrayOf(-0.82f, -0.46f, -0.14f, 0.20f, 0.56f, 0.88f)
            else -> floatArrayOf(-0.58f, -0.27f, 0f, 0.27f, 0.58f)
        }
        val spikeScale = when (char.id) {
            "vash_stampede" -> 1.54f
            "kakarot_z", "dio_world", "gilgamesh_babylon" -> 1.48f
            else -> 1.34f
        }

        for (offset in spikeOffsets) {
            val a = crownCenterAngle + offset
            sharedPath.reset()
            sharedPath.moveTo(cos(a - 0.28f) * r * 0.78f, sin(a - 0.28f) * r * 0.78f)
            sharedPath.lineTo(cos(a) * r * spikeScale, sin(a) * r * spikeScale)
            sharedPath.lineTo(cos(a + 0.28f) * r * 0.78f, sin(a + 0.28f) * r * 0.78f)
            sharedPath.close()
            drawPath(sharedPath, hairColor)
            drawPath(sharedPath, inkColor, style = Stroke(width = (r * 0.06f).coerceAtLeast(2f), join = StrokeJoin.Round))
        }
    }

    private fun DrawScope.drawCharacterSphereCore(
        char: CharacterData,
        r: Float,
        isUlt: Boolean,
        isSerious: Boolean,
        hitFlash: Float
    ) {
        val baseColor = when {
            isUlt && char.id == "mugiwara_joy" -> Color(0xFFF8FAFC)
            isUlt && char.id == "frieza_emperor" -> Color(0xFFFACC15)
            else -> char.primaryColor
        }

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

        CharacterFaceDetailRenderer.drawFaceDetails(
            scope = this,
            char = char,
            r = r,
            isUlt = isUlt,
            isSerious = isSerious
        )

        if (hitFlash > 0f) {
            drawCircle(
                color = Color.White.copy(alpha = (hitFlash * 0.58f).coerceIn(0f, 0.62f)),
                radius = r,
                center = Offset.Zero
            )
        }

        drawCircle(
            color = if (isUlt) Color.White else inkColor,
            radius = r,
            center = Offset.Zero,
            style = Stroke(width = (r * 0.08f).coerceAtLeast(2.5f))
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

        if (ball.character.id == "satoru_void" || ball.character.id == "accelerator_vector") {
            drawCircle(
                color = ball.character.auraColor.copy(alpha = 0.34f),
                radius = r * 1.18f,
                center = center,
                style = Stroke(width = 2f)
            )
        }
    }
}
