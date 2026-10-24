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
import com.example.model.CrownVisualStyle
import com.example.model.PassiveArchetype
import com.example.model.PhysicsMesh
import com.example.model.RenderMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object AnimeBallRenderer {

    private val sharedPath = Path()
    private val inkColor = Color(0xFF12151E)

    /**
     * Zero-allocation 2.5D cel-shaded portrait renderer for roster cards, stage previews, and headers.
     */
    fun drawPreviewAvatar(
        scope: DrawScope,
        character: CharacterData,
        weaponAngle: Float = 0.65f,
        isUltimatePreview: Boolean = false
    ) {
        with(scope) {
            val r = (minOf(size.width, size.height) * 0.32f).coerceAtLeast(12f)
            val cx = size.width / 2f
            val cy = size.height / 2f + r * 0.10f
            val center = Offset(cx, cy)

            drawOval(
                color = Color.Black.copy(alpha = 0.28f),
                topLeft = Offset(cx - r * 0.80f, cy + r * 0.72f),
                size = Size(r * 1.60f, r * 0.36f)
            )

            drawCircle(
                color = character.auraColor.copy(alpha = if (isUltimatePreview) 0.28f else 0.16f),
                radius = r * (if (isUltimatePreview) 1.34f else 1.20f),
                center = center
            )
            if (isUltimatePreview) {
                drawCircle(
                    color = character.auraColor.copy(alpha = 0.70f),
                    radius = r * 1.34f,
                    center = center,
                    style = Stroke(width = 2.4f)
                )
            }

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
                spinBurst = if (isUltimatePreview) 4f else 0f
            )
        }
    }

    /**
     * Arena combat ball renderer:
     * All head/body/hair/hat/eye/collar details share ONE unified local origin (Offset.Zero)
     * with explicit pivot = Offset.Zero so zero details ever lag behind or detach!
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

            // 1. Dynamic speed ribbon trail
            if (ball.trailPositions.isNotEmpty() && renderMode != RenderMode.PIXELIZED) {
                val count = ball.trailPositions.size
                var idx = 0
                for (pos in ball.trailPositions) {
                    val ratio = 1f - (idx.toFloat() / count)
                    drawCircle(
                        color = char.auraColor.copy(alpha = ratio * (if (isUlt) 0.24f else 0.14f)),
                        radius = r * (0.28f + ratio * 0.36f),
                        center = Offset(pos.x, pos.y)
                    )
                    idx++
                }
            }

            // 2. Ground shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.38f),
                topLeft = Offset(center.x - r * 0.82f, center.y + r * 0.72f),
                size = Size(r * 1.64f, r * 0.40f)
            )

            // 3. Multi-ring Awakening Aura, Lightning Sparks & Upgrade Halo
            val pulse = 1f + sin(globalTime * 8.5f) * 0.055f
            val auraColor = when {
                isUlt && char.id == "kakarot_z" -> Color(0xFF38BDF8)
                isUlt && char.id == "kamado_sun" -> Color(0xFFF97316)
                isUlt && char.id == "mugiwara_joy" -> Color.White
                isUlt && char.id == "frieza_emperor" -> Color(0xFFFACC15)
                else -> char.auraColor
            }
            val outerAuraR = r * (if (isUlt) 1.48f else 1.17f) * pulse
            drawCircle(
                color = auraColor.copy(alpha = if (isUlt) 0.30f else 0.13f),
                radius = outerAuraR,
                center = center
            )

            if (isUlt) {
                drawCircle(
                    color = auraColor.copy(alpha = 0.82f),
                    radius = outerAuraR,
                    center = center,
                    style = Stroke(width = 3.2f)
                )
                // Rotating Awakening energy sparks around sphere
                for (k in 0 until 6) {
                    val sparkA = globalTime * 6.5f + k * (PI.toFloat() / 3f)
                    val pIn = Offset(center.x + cos(sparkA) * r * 1.12f, center.y + sin(sparkA) * r * 1.12f)
                    val pMid = Offset(center.x + cos(sparkA + 0.16f) * r * 1.32f, center.y + sin(sparkA + 0.16f) * r * 1.32f)
                    val pOut = Offset(center.x + cos(sparkA + 0.04f) * outerAuraR, center.y + sin(sparkA + 0.04f) * outerAuraR)
                    drawLine(Color.White.copy(alpha = 0.85f), pIn, pMid, strokeWidth = 2.4f, cap = StrokeCap.Round)
                    drawLine(auraColor, pMid, pOut, strokeWidth = 2.2f, cap = StrokeCap.Round)
                }
            } else if (ball.canActivateUltimate) {
                drawCircle(
                    color = Color(0xFFFACC15).copy(alpha = 0.50f + sin(globalTime * 10f) * 0.22f),
                    radius = r * 1.29f,
                    center = center,
                    style = Stroke(width = 2.4f)
                )
            }

            // High-tier Upgrade Forge mastery ring
            if (ball.upgrades.totalUpgrades >= 3) {
                val upgAlpha = (0.25f + (ball.upgrades.totalUpgrades.coerceAtMost(15) * 0.025f)).coerceAtMost(0.65f)
                drawCircle(
                    color = Color(0xFFFACC15).copy(alpha = upgAlpha),
                    radius = r * 1.09f,
                    center = center,
                    style = Stroke(width = 1.8f)
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

            // 6. Status & Passive Barrier overlays
            drawStatusOverlays(ball, center, r, globalTime)
        }
    }

    private fun DrawScope.drawCharacterCrownAndBack(
        char: CharacterData,
        r: Float,
        swayRad: Float,
        isUlt: Boolean
    ) {
        // Optional Heroic Cape or Awakening Energy Wings
        if (char.hasCapeOrWings || char.id == "caped_baldy" || char.id == "tetsuo_psychic") {
            if (isUlt || char.alignment == com.example.model.CharacterAlignment.ANTAGONIST && char.hasCapeOrWings && char.id != "caped_baldy") {
                // Dramatic twin battle wings
                val wingFill = if (char.id == "accelerator_vector") Color(0xFF09090B) else char.secondaryColor
                for (side in intArrayOf(-1, 1)) {
                    sharedPath.reset()
                    sharedPath.moveTo(side * r * 0.65f, -r * 0.12f)
                    sharedPath.lineTo(side * r * 1.58f, -r * 0.72f + swayRad * r * 0.5f)
                    sharedPath.lineTo(side * r * 1.32f, r * 0.24f)
                    sharedPath.close()
                    drawPath(sharedPath, wingFill)
                    drawPath(sharedPath, char.auraColor, style = Stroke(width = 2.0f, join = StrokeJoin.Round))
                }
            } else {
                // Flowing anime cape
                val capeColor = when (char.id) {
                    "caped_baldy" -> Color(0xFFF8FAFC)
                    "tetsuo_psychic" -> Color(0xFFB91C1C)
                    else -> char.secondaryColor
                }
                sharedPath.reset()
                sharedPath.moveTo(-r * 0.74f, r * 0.18f)
                sharedPath.lineTo(-r * 0.94f + swayRad * r * 1.3f, r * 1.24f)
                sharedPath.lineTo(r * 0.94f + swayRad * r * 1.3f, r * 1.24f)
                sharedPath.lineTo(r * 0.74f, r * 0.18f)
                sharedPath.close()
                drawPath(sharedPath, capeColor)
                drawPath(sharedPath, inkColor, style = Stroke(width = 2.4f, join = StrokeJoin.Round))
            }
            if (char.id == "caped_baldy") return
        }

        // Bespoke iconic headwear for classic headliners
        if (char.id == "mugiwara_joy" && !isUlt) {
            drawStrawHatCrown(r, Color(0xFFFACC15), Color(0xFFDC2626))
            return
        }
        if (char.id == "alucard_hellsing" || (char.id == "muzan_progenitor" && !isUlt)) {
            val hatColor = if (char.id == "muzan_progenitor") Color(0xFFF8FAFC) else Color(0xFF991B1B)
            val bandColor = if (char.id == "muzan_progenitor") Color(0xFF09090B) else Color(0xFF450A0A)
            drawFedoraCrown(r, hatColor, bandColor)
            return
        }
        if (char.id == "blackbeard_tremor") {
            drawPirateTricorne(r, Color(0xFF18181B), Color(0xFFFACC15))
            return
        }
        if (char.id == "frieren_zoltraak") {
            drawTwinTailsAndElfEars(r, swayRad, Color(0xFFE2E8F0))
            return
        }
        if (char.id == "kujo_star") {
            drawRoundRect(Color(0xFF18181B), Offset(-r * 0.76f, -r * 1.06f), Size(r * 1.52f, r * 0.50f), CornerRadius(8f, 8f))
            drawRoundRect(inkColor, Offset(-r * 0.76f, -r * 1.06f), Size(r * 1.52f, r * 0.50f), CornerRadius(8f, 8f), style = Stroke(width = 2.2f))
            drawCircle(Color(0xFFFACC15), radius = r * 0.13f, center = Offset(-r * 0.24f, -r * 0.82f))
            return
        }
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
        if (char.id == "caiman_dorohedoro") {
            for (s in -1..1) {
                val a = -PI.toFloat() * 0.5f + s * 0.34f
                sharedPath.reset()
                sharedPath.moveTo(cos(a - 0.16f) * r * 0.86f, sin(a - 0.16f) * r * 0.86f)
                sharedPath.lineTo(cos(a) * r * 1.26f, sin(a) * r * 1.26f)
                sharedPath.lineTo(cos(a + 0.16f) * r * 0.86f, sin(a + 0.16f) * r * 0.86f)
                sharedPath.close()
                drawPath(sharedPath, Color(0xFF365314))
                drawPath(sharedPath, inkColor, style = Stroke(width = 2f))
            }
            return
        }

        val hairColor = when {
            isUlt && char.id == "kakarot_z" -> Color(0xFF38BDF8)
            isUlt && char.id == "mugiwara_joy" -> Color.White
            isUlt && char.id == "ryuko_scissor" -> Color(0xFFFACC15)
            else -> char.hairColor
        }

        // Full 18-style CrownVisualStyle system across all 224 anime characters
        when (char.resolvedCrownStyle) {
            CrownVisualStyle.BALD_GLEAM -> Unit

            CrownVisualStyle.STRAW_HAT -> drawStrawHatCrown(r, Color(0xFFFACC15), char.primaryColor)

            CrownVisualStyle.FEDORA_TRILBY -> drawFedoraCrown(r, char.secondaryColor, char.auraColor)

            CrownVisualStyle.PIRATE_TRICORNE -> drawPirateTricorne(r, Color(0xFF18181B), Color(0xFFFACC15))

            CrownVisualStyle.BOWL_ESPER -> {
                if (!isUlt) {
                    drawArc(hairColor, 182f, 176f, true, Offset(-r * 1.05f, -r * 1.08f), Size(r * 2.10f, r * 1.38f))
                    drawArc(inkColor, 182f, 176f, false, Offset(-r * 1.05f, -r * 1.08f), Size(r * 2.10f, r * 1.38f), style = Stroke(width = 2.2f))
                } else {
                    drawSpikyHairCrown(r, swayRad, hairColor, floatArrayOf(-0.78f, -0.42f, -0.10f, 0.22f, 0.56f, 0.84f), 1.48f)
                }
            }

            CrownVisualStyle.TWIN_TAILS -> drawTwinTailsAndElfEars(r, swayRad, hairColor)

            CrownVisualStyle.DEMON_HORNS -> {
                drawSpikyHairCrown(r, swayRad, hairColor, floatArrayOf(-0.48f, 0f, 0.48f), 1.24f)
                for (side in intArrayOf(-1, 1)) {
                    sharedPath.reset()
                    sharedPath.moveTo(side * r * 0.42f, -r * 0.78f)
                    sharedPath.quadraticTo(side * r * 0.92f, -r * 1.22f, side * r * 0.68f, -r * 1.56f)
                    sharedPath.quadraticTo(side * r * 0.48f, -r * 1.18f, side * r * 0.20f, -r * 0.86f)
                    sharedPath.close()
                    drawPath(sharedPath, if (isUlt) char.auraColor else Color(0xFFFEF08A))
                    drawPath(sharedPath, inkColor, style = Stroke(width = 2.2f, join = StrokeJoin.Round))
                }
            }

            CrownVisualStyle.ROYAL_CROWN -> {
                drawSpikyHairCrown(r, swayRad, hairColor, floatArrayOf(-0.55f, 0.55f), 1.22f)
                sharedPath.reset()
                sharedPath.moveTo(-r * 0.68f, -r * 0.78f)
                sharedPath.lineTo(-r * 0.58f, -r * 1.34f)
                sharedPath.lineTo(-r * 0.24f, -r * 0.98f)
                sharedPath.lineTo(0f, -r * 1.46f)
                sharedPath.lineTo(r * 0.24f, -r * 0.98f)
                sharedPath.lineTo(r * 0.58f, -r * 1.34f)
                sharedPath.lineTo(r * 0.68f, -r * 0.78f)
                sharedPath.close()
                drawPath(sharedPath, Color(0xFFFACC15))
                drawPath(sharedPath, inkColor, style = Stroke(width = 2.2f, join = StrokeJoin.Round))
                drawCircle(char.auraColor, radius = r * 0.10f, center = Offset(0f, -r * 1.02f))
            }

            CrownVisualStyle.MECHA_VFIN -> {
                // Gundam / Mecha armored crest + golden V-fin antenna
                drawRoundRect(char.secondaryColor, Offset(-r * 0.20f, -r * 1.26f), Size(r * 0.40f, r * 0.42f), CornerRadius(4f, 4f))
                for (side in intArrayOf(-1, 1)) {
                    sharedPath.reset()
                    sharedPath.moveTo(side * r * 0.06f, -r * 0.94f)
                    sharedPath.lineTo(side * r * 1.28f, -r * 1.48f)
                    sharedPath.lineTo(side * r * 0.32f, -r * 0.80f)
                    sharedPath.close()
                    drawPath(sharedPath, Color(0xFFFACC15))
                    drawPath(sharedPath, inkColor, style = Stroke(width = 2.0f, join = StrokeJoin.Round))
                }
                drawCircle(Color(0xFF22D3EE), radius = r * 0.11f, center = Offset(0f, -r * 0.94f))
            }

            CrownVisualStyle.KNIGHT_HELM -> {
                // Plumed Chivalric Crest
                sharedPath.reset()
                sharedPath.moveTo(-r * 0.16f, -r * 0.86f)
                sharedPath.quadraticTo(swayRad * r * 1.4f, -r * 1.68f, r * 0.42f + swayRad * r, -r * 1.38f)
                sharedPath.lineTo(r * 0.18f, -r * 0.86f)
                sharedPath.close()
                drawPath(sharedPath, char.auraColor)
                drawPath(sharedPath, inkColor, style = Stroke(width = 2f))
                drawArc(Color(0xFFCBD5E1), 195f, 150f, true, Offset(-r * 0.92f, -r * 1.06f), Size(r * 1.84f, r * 0.92f))
                drawArc(inkColor, 195f, 150f, false, Offset(-r * 0.92f, -r * 1.06f), Size(r * 1.84f, r * 0.92f), style = Stroke(width = 2.2f))
            }

            CrownVisualStyle.SAMURAI_KABUTO -> {
                drawSpikyHairCrown(r, swayRad, hairColor, floatArrayOf(-0.62f, 0f, 0.62f), 1.26f)
                // Crescent Maedate Kabuto Crest
                drawArc(
                    color = Color(0xFFFACC15),
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(-r * 0.86f, -r * 1.48f),
                    size = Size(r * 1.72f, r * 0.92f),
                    style = Stroke(width = r * 0.14f, cap = StrokeCap.Round)
                )
            }

            CrownVisualStyle.WITCH_HOOD -> {
                drawOval(char.secondaryColor, Offset(-r * 1.24f, -r * 0.88f), Size(r * 2.48f, r * 0.36f))
                drawOval(inkColor, Offset(-r * 1.24f, -r * 0.88f), Size(r * 2.48f, r * 0.36f), style = Stroke(width = 2.2f))
                sharedPath.reset()
                sharedPath.moveTo(-r * 0.68f, -r * 0.74f)
                sharedPath.lineTo(r * 0.18f + swayRad * r, -r * 1.62f)
                sharedPath.lineTo(r * 0.68f, -r * 0.74f)
                sharedPath.close()
                drawPath(sharedPath, char.secondaryColor)
                drawPath(sharedPath, inkColor, style = Stroke(width = 2.2f, join = StrokeJoin.Round))
            }

            CrownVisualStyle.HALO_CROWN -> {
                drawSpikyHairCrown(r, swayRad, hairColor, floatArrayOf(-0.52f, -0.18f, 0.18f, 0.52f), 1.28f)
                drawOval(
                    color = Color(0xFFFACC15),
                    topLeft = Offset(-r * 0.68f, -r * 1.46f),
                    size = Size(r * 1.36f, r * 0.28f),
                    style = Stroke(width = r * 0.11f)
                )
            }

            CrownVisualStyle.SHINOBI_HEADBAND -> {
                drawSpikyHairCrown(r, swayRad, hairColor, floatArrayOf(-0.68f, -0.32f, 0f, 0.32f, 0.68f), 1.40f)
            }

            CrownVisualStyle.SLICK_VILLAIN -> {
                drawSpikyHairCrown(r, swayRad, hairColor, floatArrayOf(-0.74f, -0.44f, -0.14f, 0.18f), 1.30f)
            }

            CrownVisualStyle.FLOWING_MANE -> {
                // Side flowing locks + top spikes
                for (side in intArrayOf(-1, 1)) {
                    sharedPath.reset()
                    sharedPath.moveTo(side * r * 0.72f, -r * 0.35f)
                    sharedPath.lineTo(side * r * 1.22f + swayRad * r * 0.7f, r * 0.55f)
                    sharedPath.lineTo(side * r * 0.80f + swayRad * r * 0.5f, r * 0.68f)
                    sharedPath.close()
                    drawPath(sharedPath, hairColor)
                    drawPath(sharedPath, inkColor, style = Stroke(width = 2f, join = StrokeJoin.Round))
                }
                drawSpikyHairCrown(r, swayRad, hairColor, floatArrayOf(-0.58f, -0.22f, 0.22f, 0.58f), 1.34f)
            }

            CrownVisualStyle.SAIYAN_WILD -> {
                drawSpikyHairCrown(
                    r = r,
                    swayRad = swayRad,
                    hairColor = hairColor,
                    spikeOffsets = floatArrayOf(-0.88f, -0.54f, -0.20f, 0.14f, 0.48f, 0.84f),
                    spikeScale = 1.52f
                )
            }

            CrownVisualStyle.SHONEN_SPIKY -> {
                drawSpikyHairCrown(
                    r = r,
                    swayRad = swayRad,
                    hairColor = hairColor,
                    spikeOffsets = floatArrayOf(-0.60f, -0.28f, 0f, 0.28f, 0.60f),
                    spikeScale = 1.36f
                )
            }
        }
    }

    private fun DrawScope.drawStrawHatCrown(r: Float, hatColor: Color, bandColor: Color) {
        val brimY = -r * 0.72f
        drawOval(hatColor, Offset(-r * 1.06f, brimY - r * 0.20f), Size(r * 2.12f, r * 0.40f))
        drawOval(inkColor, Offset(-r * 1.06f, brimY - r * 0.20f), Size(r * 2.12f, r * 0.40f), style = Stroke(width = 2.4f))
        drawArc(hatColor, 180f, 180f, true, Offset(-r * 0.60f, brimY - r * 0.54f), Size(r * 1.20f, r * 0.86f))
        drawLine(bandColor, Offset(-r * 0.56f, brimY - r * 0.12f), Offset(r * 0.56f, brimY - r * 0.12f), strokeWidth = r * 0.13f, cap = StrokeCap.Round)
    }

    private fun DrawScope.drawFedoraCrown(r: Float, hatColor: Color, bandColor: Color) {
        val hatY = -r * 0.74f
        drawOval(hatColor, Offset(-r * 1.12f, hatY - r * 0.18f), Size(r * 2.24f, r * 0.36f))
        drawOval(inkColor, Offset(-r * 1.12f, hatY - r * 0.18f), Size(r * 2.24f, r * 0.36f), style = Stroke(width = 2.4f))
        drawArc(hatColor, 180f, 180f, true, Offset(-r * 0.62f, hatY - r * 0.52f), Size(r * 1.24f, r * 0.80f))
        drawLine(bandColor, Offset(-r * 0.58f, hatY - r * 0.12f), Offset(r * 0.58f, hatY - r * 0.12f), strokeWidth = r * 0.12f, cap = StrokeCap.Round)
    }

    private fun DrawScope.drawPirateTricorne(r: Float, hatColor: Color, trimColor: Color) {
        sharedPath.reset()
        sharedPath.moveTo(-r * 1.15f, -r * 0.62f)
        sharedPath.lineTo(0f, -r * 1.28f)
        sharedPath.lineTo(r * 1.15f, -r * 0.62f)
        sharedPath.close()
        drawPath(sharedPath, hatColor)
        drawPath(sharedPath, trimColor, style = Stroke(width = 2.4f, join = StrokeJoin.Round))
    }

    private fun DrawScope.drawTwinTailsAndElfEars(r: Float, swayRad: Float, hairColor: Color) {
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
            sharedPath.lineTo(side * r * 1.42f + swayRad * r * 0.6f, r * 0.36f)
            sharedPath.lineTo(side * r * 0.92f + swayRad * r * 0.4f, r * 0.54f)
            sharedPath.close()
            drawPath(sharedPath, hairColor)
            drawPath(sharedPath, inkColor, style = Stroke(width = 2.2f, join = StrokeJoin.Round))
        }
    }

    private fun DrawScope.drawSpikyHairCrown(
        r: Float,
        swayRad: Float,
        hairColor: Color,
        spikeOffsets: FloatArray,
        spikeScale: Float
    ) {
        if (hairColor.alpha == 0f) return
        val crownCenterAngle = -PI.toFloat() * 0.5f + swayRad
        val highlight = Color.White.copy(alpha = 0.26f)
        for (offset in spikeOffsets) {
            val a = crownCenterAngle + offset
            sharedPath.reset()
            sharedPath.moveTo(cos(a - 0.28f) * r * 0.78f, sin(a - 0.28f) * r * 0.78f)
            sharedPath.lineTo(cos(a) * r * spikeScale, sin(a) * r * spikeScale)
            sharedPath.lineTo(cos(a + 0.28f) * r * 0.78f, sin(a + 0.28f) * r * 0.78f)
            sharedPath.close()
            drawPath(sharedPath, hairColor)
            // Inner anime hair sheen highlight
            drawLine(
                color = highlight,
                start = Offset(cos(a - 0.08f) * r * 0.84f, sin(a - 0.08f) * r * 0.84f),
                end = Offset(cos(a) * r * (spikeScale - 0.12f), sin(a) * r * (spikeScale - 0.12f)),
                strokeWidth = (r * 0.07f).coerceAtLeast(1.8f),
                cap = StrokeCap.Round
            )
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

        // 1. Primary 2.5D sphere fill
        drawCircle(color = baseColor, radius = r, center = Offset.Zero)

        // 2. Cel-shaded lower ambient shadow band
        drawArc(
            color = Color.Black.copy(alpha = 0.22f),
            startAngle = 12f,
            sweepAngle = 156f,
            useCenter = true,
            topLeft = Offset(-r, -r * 0.72f),
            size = Size(r * 2f, r * 1.72f)
        )

        // 3. Tailored anime outfit / armor collar band + center emblem clasp
        drawArc(
            color = char.secondaryColor.copy(alpha = 0.86f),
            startAngle = 22f,
            sweepAngle = 136f,
            useCenter = true,
            topLeft = Offset(-r * 0.96f, -r * 0.62f),
            size = Size(r * 1.92f, r * 1.58f)
        )
        // Crisp V-lapel trim on outfit collar
        drawLine(
            color = char.auraColor.copy(alpha = 0.78f),
            start = Offset(-r * 0.42f, r * 0.48f),
            end = Offset(0f, r * 0.72f),
            strokeWidth = (r * 0.055f).coerceAtLeast(1.8f),
            cap = StrokeCap.Round
        )
        drawLine(
            color = char.auraColor.copy(alpha = 0.78f),
            start = Offset(r * 0.42f, r * 0.48f),
            end = Offset(0f, r * 0.72f),
            strokeWidth = (r * 0.055f).coerceAtLeast(1.8f),
            cap = StrokeCap.Round
        )

        // 4. Dual glossy anime specular highlights (zero Brush allocation!)
        drawOval(
            color = Color.White.copy(alpha = 0.40f),
            topLeft = Offset(-r * 0.58f, -r * 0.74f),
            size = Size(r * 0.48f, r * 0.24f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.62f),
            radius = r * 0.075f,
            center = Offset(-r * 0.62f, -r * 0.44f)
        )

        // 5. Character facial markings, headbands, visors, and expressive anime eyes
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

        // 6. Outer bold manga ink rim
        drawCircle(
            color = if (isUlt) Color.White else inkColor,
            radius = r,
            center = Offset.Zero,
            style = Stroke(width = (r * 0.082f).coerceAtLeast(2.5f))
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
                val p2 = Offset(center.x + cos(a + 0.25f) * r * 1.26f, center.y + sin(a + 0.25f) * r * 1.26f)
                drawLine(Color(0xFF38BDF8), p1, p2, strokeWidth = 3f, cap = StrokeCap.Round)
            }
        }

        if (ball.bleedTicksRemaining > 0) {
            for (i in 0 until ball.bleedTicksRemaining) {
                val a = globalTime * 4.2f + i * 1.57f
                val drop = Offset(center.x + cos(a) * r * 1.12f, center.y + sin(a) * r * 1.12f)
                drawCircle(Color(0xFFEF4444), radius = r * 0.09f, center = drop)
            }
        }

        if (ball.burnTicksRemaining > 0) {
            for (i in 0 until ball.burnTicksRemaining) {
                val a = -globalTime * 5.0f + i * 2.09f
                val ember = Offset(center.x + cos(a) * r * 1.15f, center.y + sin(a) * r * 1.15f)
                drawCircle(Color(0xFFF97316), radius = r * 0.10f, center = ember)
                drawCircle(Color(0xFFFEF08A), radius = r * 0.05f, center = ember)
            }
        }

        if (ball.isShieldActive ||
            ball.character.passiveArchetype == PassiveArchetype.INFINITY_BARRIER ||
            ball.character.passiveArchetype == PassiveArchetype.VECTOR_REFLECT
        ) {
            val shieldAlpha = if (ball.shieldVisualTimer > 0f) 0.58f else 0.28f
            drawCircle(
                color = ball.character.auraColor.copy(alpha = shieldAlpha),
                radius = r * 1.18f,
                center = center,
                style = Stroke(width = if (ball.shieldVisualTimer > 0f) 2.8f else 1.8f)
            )
        }
    }
}
