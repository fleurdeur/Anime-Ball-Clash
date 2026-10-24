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
import com.example.model.CharacterAlignment
import com.example.model.CharacterData
import com.example.model.CombatBall
import com.example.model.CrownVisualStyle
import com.example.model.ElementTag
import com.example.model.PassiveArchetype
import com.example.model.PhysicsMesh
import com.example.model.RenderMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object AnimeBallRenderer {

    private val sharedPath = Path()
    private val inkColor = Color(0xFF12151E)

    // Preallocated spike offset arrays (zero GC pressure at 60–120 FPS)
    private val SPIKES_BOWL_ULT = floatArrayOf(-0.78f, -0.42f, -0.10f, 0.22f, 0.56f, 0.84f)
    private val SPIKES_DEMON = floatArrayOf(-0.48f, 0f, 0.48f)
    private val SPIKES_ROYAL = floatArrayOf(-0.55f, 0.55f)
    private val SPIKES_SAMURAI = floatArrayOf(-0.62f, 0f, 0.62f)
    private val SPIKES_HALO = floatArrayOf(-0.52f, -0.18f, 0.18f, 0.52f)
    private val SPIKES_SHINOBI = floatArrayOf(-0.68f, -0.32f, 0f, 0.32f, 0.68f)
    private val SPIKES_SLICK = floatArrayOf(-0.74f, -0.44f, -0.14f, 0.18f)
    private val SPIKES_FLOWING = floatArrayOf(-0.58f, -0.22f, 0.22f, 0.58f)
    private val SPIKES_SAIYAN = floatArrayOf(-0.88f, -0.54f, -0.20f, 0.14f, 0.48f, 0.84f)
    private val SPIKES_SHONEN = floatArrayOf(-0.60f, -0.28f, 0f, 0.28f, 0.60f)

    /**
     * Zero-allocation 2.5D cel-shaded portrait renderer for roster cards, stage previews, and headers.
     */
    fun drawPreviewAvatar(
        scope: DrawScope,
        character: CharacterData,
        weaponAngle: Float = 0.65f,
        isUltimatePreview: Boolean = false,
        globalTime: Float = 0f
    ) {
        with(scope) {
            val r = (minOf(size.width, size.height) * 0.31f).coerceAtLeast(12f)
            val cx = size.width / 2f
            val cy = size.height / 2f + r * 0.10f
            val center = Offset(cx, cy)

            drawOval(
                color = Color.Black.copy(alpha = 0.24f),
                topLeft = Offset(cx - r * 0.80f, cy + r * 0.72f),
                size = Size(r * 1.60f, r * 0.36f)
            )

            drawCircle(
                color = character.auraColor.copy(alpha = if (isUltimatePreview) 0.30f else 0.16f),
                radius = r * (if (isUltimatePreview) 1.38f else 1.20f),
                center = center
            )
            if (isUltimatePreview) {
                drawCircle(
                    color = character.auraColor.copy(alpha = 0.76f),
                    radius = r * 1.38f,
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
                spinBurst = if (isUltimatePreview) 4f else 0f,
                weaponScale = CombatBall.computeUltimateWeaponScale(character, isUltimatePreview),
                globalTime = globalTime
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

            // 1. Dynamic speed ribbon trail (scaled by RenderMode)
            if (ball.trailPositions.isNotEmpty() && renderMode.maxTrailNodes > 0) {
                val count = ball.trailPositions.size
                var idx = 0
                for (pos in ball.trailPositions) {
                    val ratio = 1f - (idx.toFloat() / count)
                    drawCircle(
                        color = char.auraColor.copy(alpha = ratio * (if (isUlt) 0.26f else 0.15f)),
                        radius = r * (0.26f + ratio * 0.38f),
                        center = Offset(pos.x, pos.y)
                    )
                    idx++
                }
            }

            // 2. Ground shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.34f),
                topLeft = Offset(center.x - r * 0.82f, center.y + r * 0.72f),
                size = Size(r * 1.64f, r * 0.40f)
            )

            // 3. Multi-ring Awakening Aura, Bloom Halo, Elemental Special FX & Upgrade Halo
            val pulse = 1f + sin(globalTime * 8.5f) * 0.055f
            val auraColor = when {
                isUlt && char.id == "kakarot_z" -> Color(0xFF38BDF8)
                isUlt && char.id == "kamado_sun" -> Color(0xFFF97316)
                isUlt && char.id == "mugiwara_joy" -> Color.White
                isUlt && char.id == "frieza_emperor" -> Color(0xFFFACC15)
                isUlt && char.id == "guts_berserk" -> Color(0xFFDC2626)
                else -> char.auraColor
            }
            val outerAuraR = r * (if (isUlt) 1.50f else 1.17f) * pulse

            if (renderMode.bloomGlow) {
                drawCircle(
                    color = auraColor.copy(alpha = if (isUlt) 0.22f else 0.09f),
                    radius = outerAuraR * 1.25f,
                    center = center
                )
            }

            drawCircle(
                color = auraColor.copy(alpha = if (isUlt) 0.32f else 0.14f),
                radius = outerAuraR,
                center = center
            )

            if (renderMode.ambientAuraFx) {
                drawElementalSpecialEffects(
                    char = char,
                    center = center,
                    r = r,
                    auraColor = auraColor,
                    isUlt = isUlt,
                    isUltra = renderMode == RenderMode.ULTRA,
                    globalTime = globalTime
                )
            }

            if (isUlt) {
                drawCircle(
                    color = auraColor.copy(alpha = 0.85f),
                    radius = outerAuraR,
                    center = center,
                    style = Stroke(width = 3.2f)
                )
                val sparkCount = if (renderMode == RenderMode.ULTRA) 8 else 6
                for (k in 0 until sparkCount) {
                    val sparkA = globalTime * 6.8f + k * (2f * PI.toFloat() / sparkCount)
                    val pIn = Offset(center.x + cos(sparkA) * r * 1.10f, center.y + sin(sparkA) * r * 1.10f)
                    val pMid = Offset(center.x + cos(sparkA + 0.16f) * r * 1.32f, center.y + sin(sparkA + 0.16f) * r * 1.32f)
                    val pOut = Offset(center.x + cos(sparkA + 0.04f) * outerAuraR, center.y + sin(sparkA + 0.04f) * outerAuraR)
                    drawLine(Color.White.copy(alpha = 0.88f), pIn, pMid, strokeWidth = 2.4f, cap = StrokeCap.Round)
                    drawLine(auraColor, pMid, pOut, strokeWidth = 2.2f, cap = StrokeCap.Round)
                }
            } else if (ball.canActivateUltimate) {
                drawCircle(
                    color = Color(0xFFFACC15).copy(alpha = 0.52f + sin(globalTime * 10f) * 0.22f),
                    radius = r * 1.29f,
                    center = center,
                    style = Stroke(width = 2.5f)
                )
            }

            // Passive proc flare ring
            if (ball.passiveFlashTimer > 0f) {
                val pAlpha = (ball.passiveFlashTimer / 0.85f).coerceIn(0f, 1f)
                val flareR = r * (1.22f + (1f - pAlpha) * 0.42f)
                drawCircle(
                    color = auraColor.copy(alpha = pAlpha * 0.72f),
                    radius = flareR,
                    center = center,
                    style = Stroke(width = 2.6f)
                )
                for (q in 0 until 4) {
                    val qa = globalTime * 5f + q * (PI.toFloat() * 0.5f)
                    drawLine(
                        color = Color.White.copy(alpha = pAlpha * 0.80f),
                        start = Offset(center.x + cos(qa) * (flareR - r * 0.12f), center.y + sin(qa) * (flareR - r * 0.12f)),
                        end = Offset(center.x + cos(qa) * (flareR + r * 0.18f), center.y + sin(qa) * (flareR + r * 0.18f)),
                        strokeWidth = 2.4f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // High-tier Upgrade mastery ring
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
            val sx = if (useElastic) ball.stretchFactor.coerceIn(0.84f, 1.18f) else 1f
            val sy = if (useElastic) ball.squashFactor.coerceIn(0.84f, 1.18f) else 1f
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

            // 5. Signature orbiting weapon (scales up dramatically during Ultimate!)
            WeaponRenderer.drawWeapon(
                scope = this,
                character = char,
                center = center,
                radius = r,
                angle = ball.weaponAngle,
                isUltimateActive = isUlt,
                spinBurst = ball.weaponSpinBurst,
                weaponScale = ball.weaponScale,
                globalTime = globalTime
            )

            // 6. Status & Passive Barrier overlays
            drawStatusOverlays(ball, center, r, globalTime)
        }
    }

    /**
     * Ambient elemental special effects tailored to each character's ElementTag & Ultimate state.
     */
    private fun DrawScope.drawElementalSpecialEffects(
        char: CharacterData,
        center: Offset,
        r: Float,
        auraColor: Color,
        isUlt: Boolean,
        isUltra: Boolean,
        globalTime: Float
    ) {
        val count = when {
            isUlt && isUltra -> 6
            isUlt || isUltra -> 4
            else -> 3
        }
        val orbitR = r * (if (isUlt) 1.30f else 1.14f)

        // Signature character-specific Ultimate ability manifestations
        if (isUlt) {
            when (char.id) {
                "satoru_void" -> {
                    // Orbiting Lapse Blue + Reversal Red converging into Hollow Purple
                    val redPos = Offset(center.x + cos(globalTime * 5.2f) * r * 1.24f, center.y + sin(globalTime * 5.2f) * r * 1.24f)
                    val bluePos = Offset(center.x + cos(globalTime * 5.2f + PI.toFloat()) * r * 1.24f, center.y + sin(globalTime * 5.2f + PI.toFloat()) * r * 1.24f)
                    drawCircle(Color(0xFFEF4444).copy(alpha = 0.78f), radius = r * 0.14f, center = redPos)
                    drawCircle(Color(0xFF38BDF8).copy(alpha = 0.78f), radius = r * 0.14f, center = bluePos)
                }
                "sukuna_shrine" -> {
                    // Malevolent Shrine rapid invisible dismantle cleave lines
                    for (s in 0 until 4) {
                        val sa = globalTime * 9f + s * 1.57f
                        val c = Offset(center.x + cos(sa) * r * 1.18f, center.y + sin(sa) * r * 1.18f)
                        drawLine(
                            Color(0xFFE11D48).copy(alpha = 0.85f),
                            Offset(c.x - r * 0.24f, c.y - r * 0.18f),
                            Offset(c.x + r * 0.24f, c.y + r * 0.18f),
                            strokeWidth = 2.4f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                "mugiwara_joy" -> {
                    // Gear 5 Nika golden/crimson Haki lightning + floating Joyboy cloud puffs
                    for (cIdx in 0 until 5) {
                        val ca = -globalTime * 3.4f + cIdx * 1.256f
                        val cp = Offset(center.x + cos(ca) * r * 1.25f, center.y + sin(ca) * r * 1.25f)
                        drawCircle(Color.White.copy(alpha = 0.86f), radius = r * 0.13f, center = cp)
                    }
                }
                "jinwoo_monarch" -> {
                    // Shadow Monarch rising violet-black abyssal flames
                    for (w in 0 until 4) {
                        val wa = globalTime * 4.2f + w * 1.57f
                        val wp = Offset(center.x + cos(wa) * r * 1.16f, center.y + sin(wa) * r * 1.16f)
                        drawCircle(Color(0xFF09090B).copy(alpha = 0.75f), radius = r * 0.15f, center = wp)
                        drawCircle(Color(0xFFA855F7).copy(alpha = 0.85f), radius = r * 0.08f, center = wp)
                    }
                }
                "kamado_sun" -> {
                    // Hinokami Kagura spiraling sun-breathing fire ring
                    drawArc(
                        color = Color(0xFFF97316).copy(alpha = 0.82f),
                        startAngle = (globalTime * 240f) % 360f,
                        sweepAngle = 215f,
                        useCenter = false,
                        topLeft = Offset(center.x - r * 1.28f, center.y - r * 1.28f),
                        size = Size(r * 2.56f, r * 2.56f),
                        style = Stroke(width = r * 0.10f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        when (char.element) {
            ElementTag.FIRE -> {
                for (i in 0 until count) {
                    val phase = (globalTime * 2.2f + i * 0.37f) % 1f
                    val xOff = sin(globalTime * 4.5f + i * 2.1f) * r * 0.78f
                    val yOff = r * 0.45f - phase * r * 1.65f
                    val wispR = r * (0.13f * (1f - phase * 0.65f)).coerceAtLeast(0.04f)
                    val p = Offset(center.x + xOff, center.y + yOff)
                    drawCircle(auraColor.copy(alpha = (1f - phase) * 0.68f), radius = wispR, center = p)
                    drawCircle(Color(0xFFFEF08A).copy(alpha = (1f - phase) * 0.72f), radius = wispR * 0.48f, center = p)
                }
            }

            ElementTag.ELEC -> {
                for (i in 0 until count) {
                    val a = globalTime * 7.5f + i * (2f * PI.toFloat() / count)
                    val p1 = Offset(center.x + cos(a) * r * 0.96f, center.y + sin(a) * r * 0.96f)
                    val p2 = Offset(center.x + cos(a + 0.22f) * orbitR, center.y + sin(a + 0.22f) * orbitR)
                    val p3 = Offset(center.x + cos(a + 0.40f) * r * 1.02f, center.y + sin(a + 0.40f) * r * 1.02f)
                    drawLine(Color(0xFF38BDF8).copy(alpha = 0.78f), p1, p2, strokeWidth = 2.0f, cap = StrokeCap.Round)
                    drawLine(Color.White.copy(alpha = 0.80f), p2, p3, strokeWidth = 1.6f, cap = StrokeCap.Round)
                }
            }

            ElementTag.SLASH -> {
                for (i in 0 until count) {
                    val deg = (globalTime * 155f + i * (360f / count)) % 360f
                    drawArc(
                        color = auraColor.copy(alpha = if (isUlt) 0.58f else 0.34f),
                        startAngle = deg,
                        sweepAngle = 42f,
                        useCenter = false,
                        topLeft = Offset(center.x - orbitR, center.y - orbitR),
                        size = Size(orbitR * 2f, orbitR * 2f),
                        style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                    )
                }
            }

            ElementTag.HEAVY -> {
                val wave = (globalTime * 1.6f) % 1f
                drawCircle(
                    color = auraColor.copy(alpha = (1f - wave) * 0.38f),
                    radius = r * (1.02f + wave * 0.40f),
                    center = center,
                    style = Stroke(width = 2.0f)
                )
            }

            ElementTag.MAGIC, ElementTag.ALL -> {
                for (i in 0 until count) {
                    val a = -globalTime * 2.8f + i * (2f * PI.toFloat() / count)
                    val mx = center.x + cos(a) * orbitR
                    val my = center.y + sin(a) * orbitR
                    val d = r * 0.085f
                    sharedPath.reset()
                    sharedPath.moveTo(mx, my - d)
                    sharedPath.lineTo(mx + d, my)
                    sharedPath.lineTo(mx, my + d)
                    sharedPath.lineTo(mx - d, my)
                    sharedPath.close()
                    drawPath(sharedPath, auraColor.copy(alpha = 0.72f))
                }
            }
        }
    }

    private fun DrawScope.drawCharacterCrownAndBack(
        char: CharacterData,
        r: Float,
        swayRad: Float,
        isUlt: Boolean
    ) {
        if (char.hasCapeOrWings || char.id == "caped_baldy" || char.id == "tetsuo_psychic" || (char.id == "guts_berserk" && isUlt)) {
            if (isUlt || (char.alignment == CharacterAlignment.ANTAGONIST && char.hasCapeOrWings && char.id != "caped_baldy")) {
                val wingFill = when (char.id) {
                    "accelerator_vector", "guts_berserk" -> Color(0xFF09090B)
                    else -> char.secondaryColor
                }
                for (sideIdx in 0..1) {
                    val side = if (sideIdx == 0) -1 else 1
                    sharedPath.reset()
                    sharedPath.moveTo(side * r * 0.65f, -r * 0.12f)
                    sharedPath.lineTo(side * r * 1.58f, -r * 0.72f + swayRad * r * 0.5f)
                    sharedPath.lineTo(side * r * 1.32f, r * 0.24f)
                    sharedPath.close()
                    drawPath(sharedPath, wingFill)
                    drawPath(sharedPath, char.auraColor, style = Stroke(width = 2.0f, join = StrokeJoin.Round))
                }
            } else {
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

        if (char.id == "mugiwara_joy") {
            if (!isUlt) {
                drawStrawHatCrown(r, Color(0xFFFACC15), Color(0xFFDC2626))
                return
            } else {
                // Gear 5 Sun God Nika floating white Hagoromo cloud-scarf + blazing white hair
                drawArc(
                    color = Color.White,
                    startAngle = 195f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(-r * 1.32f, -r * 1.30f),
                    size = Size(r * 2.64f, r * 1.90f),
                    style = Stroke(width = r * 0.16f, cap = StrokeCap.Round)
                )
            }
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
            for (sideIdx in 0..1) {
                val side = if (sideIdx == 0) -1 else 1
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
            isUlt && char.id == "guts_berserk" -> Color(0xFF09090B)
            else -> char.hairColor
        }

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
                    drawSpikyHairCrown(r, swayRad, hairColor, SPIKES_BOWL_ULT, 1.48f)
                }
            }

            CrownVisualStyle.TWIN_TAILS -> drawTwinTailsAndElfEars(r, swayRad, hairColor)

            CrownVisualStyle.DEMON_HORNS -> {
                drawSpikyHairCrown(r, swayRad, hairColor, SPIKES_DEMON, 1.24f)
                for (sideIdx in 0..1) {
                    val side = if (sideIdx == 0) -1 else 1
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
                drawSpikyHairCrown(r, swayRad, hairColor, SPIKES_ROYAL, 1.22f)
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
                drawRoundRect(char.secondaryColor, Offset(-r * 0.20f, -r * 1.26f), Size(r * 0.40f, r * 0.42f), CornerRadius(4f, 4f))
                for (sideIdx in 0..1) {
                    val side = if (sideIdx == 0) -1 else 1
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
                drawSpikyHairCrown(r, swayRad, hairColor, SPIKES_SAMURAI, 1.26f)
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
                drawSpikyHairCrown(r, swayRad, hairColor, SPIKES_HALO, 1.28f)
                drawOval(
                    color = Color(0xFFFACC15),
                    topLeft = Offset(-r * 0.68f, -r * 1.46f),
                    size = Size(r * 1.36f, r * 0.28f),
                    style = Stroke(width = r * 0.11f)
                )
            }

            CrownVisualStyle.SHINOBI_HEADBAND -> drawSpikyHairCrown(r, swayRad, hairColor, SPIKES_SHINOBI, 1.40f)
            CrownVisualStyle.SLICK_VILLAIN -> drawSpikyHairCrown(r, swayRad, hairColor, SPIKES_SLICK, 1.30f)

            CrownVisualStyle.FLOWING_MANE -> {
                for (sideIdx in 0..1) {
                    val side = if (sideIdx == 0) -1 else 1
                    sharedPath.reset()
                    sharedPath.moveTo(side * r * 0.72f, -r * 0.35f)
                    sharedPath.lineTo(side * r * 1.22f + swayRad * r * 0.7f, r * 0.55f)
                    sharedPath.lineTo(side * r * 0.80f + swayRad * r * 0.5f, r * 0.68f)
                    sharedPath.close()
                    drawPath(sharedPath, hairColor)
                    drawPath(sharedPath, inkColor, style = Stroke(width = 2f, join = StrokeJoin.Round))
                }
                drawSpikyHairCrown(r, swayRad, hairColor, SPIKES_FLOWING, 1.34f)
            }

            CrownVisualStyle.SAIYAN_WILD -> drawSpikyHairCrown(r, swayRad, hairColor, SPIKES_SAIYAN, 1.52f)
            CrownVisualStyle.SHONEN_SPIKY -> drawSpikyHairCrown(r, swayRad, hairColor, SPIKES_SHONEN, 1.36f)
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
        for (sideIdx in 0..1) {
            val side = if (sideIdx == 0) -1 else 1
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
        for (i in spikeOffsets.indices) {
            val a = crownCenterAngle + spikeOffsets[i]
            sharedPath.reset()
            sharedPath.moveTo(cos(a - 0.28f) * r * 0.78f, sin(a - 0.28f) * r * 0.78f)
            sharedPath.lineTo(cos(a) * r * spikeScale, sin(a) * r * spikeScale)
            sharedPath.lineTo(cos(a + 0.28f) * r * 0.78f, sin(a + 0.28f) * r * 0.78f)
            sharedPath.close()
            drawPath(sharedPath, hairColor)
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
            isUlt && char.id == "guts_berserk" -> Color(0xFF18181B)
            else -> char.primaryColor
        }

        drawCircle(color = baseColor, radius = r, center = Offset.Zero)

        drawArc(
            color = Color.Black.copy(alpha = 0.22f),
            startAngle = 12f,
            sweepAngle = 156f,
            useCenter = true,
            topLeft = Offset(-r, -r * 0.72f),
            size = Size(r * 2f, r * 1.72f)
        )

        drawArc(
            color = char.secondaryColor.copy(alpha = 0.90f),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = true,
            topLeft = Offset(-r * 0.96f, -r * 0.56f),
            size = Size(r * 1.92f, r * 1.54f)
        )

        // Detailed character-specific anime attire, collars, scars, armor plates & insignias
        drawCharacterOutfitDetails(char = char, r = r, isUlt = isUlt)

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
            color = if (isUlt) (if (char.id == "guts_berserk") Color(0xFFEF4444) else Color.White) else inkColor,
            radius = r,
            center = Offset.Zero,
            style = Stroke(width = (r * 0.082f).coerceAtLeast(2.5f))
        )
    }

    /**
     * Renders iconic anime outfits, collars, chest scars, insignias, armor plates, and Ultimate form
     * transformations across both flagship heroes/villains and all 224 roster characters.
     * Zero heap allocations per frame.
     */
    private fun DrawScope.drawCharacterOutfitDetails(
        char: CharacterData,
        r: Float,
        isUlt: Boolean
    ) {
        when (char.id) {
            "kakarot_z" -> {
                // Turtle Hermit orange martial-arts gi + navy undershirt V-neck + obi sash + kanji crest
                sharedPath.reset()
                sharedPath.moveTo(-r * 0.36f, r * 0.36f)
                sharedPath.lineTo(0f, r * 0.68f)
                sharedPath.lineTo(r * 0.36f, r * 0.36f)
                sharedPath.close()
                drawPath(sharedPath, Color(0xFF1E3A8A))
                drawLine(Color(0xFFEA580C), Offset(-r * 0.46f, r * 0.38f), Offset(0f, r * 0.72f), strokeWidth = r * 0.10f, cap = StrokeCap.Round)
                drawLine(Color(0xFFEA580C), Offset(r * 0.46f, r * 0.38f), Offset(0f, r * 0.72f), strokeWidth = r * 0.10f, cap = StrokeCap.Round)
                drawLine(Color(0xFF1D4ED8), Offset(-r * 0.56f, r * 0.76f), Offset(r * 0.56f, r * 0.76f), strokeWidth = r * 0.12f, cap = StrokeCap.Round)
                drawCircle(Color.White, radius = r * 0.10f, center = Offset(-r * 0.38f, r * 0.58f))
                drawCircle(inkColor, radius = r * 0.05f, center = Offset(-r * 0.38f, r * 0.58f))
            }

            "kamado_sun" -> {
                // Ichimatsu green-black checkered haori + crisp Demon Slayer Corps white collar button
                val sq = r * 0.22f
                for (row in 0..1) {
                    for (col in -2..1) {
                        val isBlack = (row + col) % 2 == 0
                        drawRect(
                            color = if (isBlack) inkColor else Color(0xFF10B981),
                            topLeft = Offset(col * sq, r * 0.44f + row * sq * 0.85f),
                            size = Size(sq, sq * 0.85f)
                        )
                    }
                }
                drawRoundRect(Color(0xFF18181B), Offset(-r * 0.22f, r * 0.34f), Size(r * 0.44f, r * 0.20f), CornerRadius(4f, 4f))
                drawCircle(Color(0xFFF8FAFC), radius = r * 0.055f, center = Offset(0f, r * 0.44f))
            }

            "raikiri_shinobi" -> {
                // Konoha olive tactical flak vest + twin chest pockets + center zipper
                drawRoundRect(Color(0xFF365314), Offset(-r * 0.64f, r * 0.42f), Size(r * 1.28f, r * 0.46f), CornerRadius(8f, 8f))
                drawLine(inkColor, Offset(0f, r * 0.42f), Offset(0f, r * 0.88f), strokeWidth = 2.2f)
                drawRect(Color(0xFF4D7C0F), Offset(-r * 0.46f, r * 0.52f), Size(r * 0.28f, r * 0.20f))
                drawRect(Color(0xFF4D7C0F), Offset(r * 0.18f, r * 0.52f), Size(r * 0.28f, r * 0.20f))
            }

            "mugiwara_joy" -> {
                // Open captain vest + iconic X-shaped chest scar + yellow sash
                val vestColor = if (isUlt) Color.White else Color(0xFFDC2626)
                drawLine(vestColor, Offset(-r * 0.52f, r * 0.38f), Offset(-r * 0.36f, r * 0.84f), strokeWidth = r * 0.18f, cap = StrokeCap.Round)
                drawLine(vestColor, Offset(r * 0.52f, r * 0.38f), Offset(r * 0.36f, r * 0.84f), strokeWidth = r * 0.18f, cap = StrokeCap.Round)
                // Iconic X chest scar
                drawLine(Color(0xFF991B1B), Offset(-r * 0.18f, r * 0.44f), Offset(r * 0.18f, r * 0.72f), strokeWidth = r * 0.08f, cap = StrokeCap.Round)
                drawLine(Color(0xFF991B1B), Offset(r * 0.18f, r * 0.44f), Offset(-r * 0.18f, r * 0.72f), strokeWidth = r * 0.08f, cap = StrokeCap.Round)
                drawLine(if (isUlt) Color(0xFF7E22CE) else Color(0xFFFACC15), Offset(-r * 0.50f, r * 0.82f), Offset(r * 0.50f, r * 0.82f), strokeWidth = r * 0.11f, cap = StrokeCap.Round)
            }

            "satoru_void" -> {
                // High Jujutsu Tech dark collar + gold swirl button
                drawRoundRect(Color(0xFF0F172A), Offset(-r * 0.58f, r * 0.36f), Size(r * 1.16f, r * 0.48f), CornerRadius(8f, 8f))
                drawCircle(Color(0xFFFACC15), radius = r * 0.075f, center = Offset(r * 0.22f, r * 0.52f))
            }

            "roronoa_ashura" -> {
                // Diagonal Mihawk chest scar + green haramaki belly sash + dark green kimono lapel
                drawLine(Color(0xFF14532D), Offset(-r * 0.54f, r * 0.40f), Offset(r * 0.12f, r * 0.84f), strokeWidth = r * 0.16f, cap = StrokeCap.Round)
                drawLine(Color(0xFF7F1D1D), Offset(-r * 0.30f, r * 0.40f), Offset(r * 0.28f, r * 0.74f), strokeWidth = r * 0.075f, cap = StrokeCap.Round)
                drawLine(Color(0xFF22C55E), Offset(-r * 0.52f, r * 0.80f), Offset(r * 0.52f, r * 0.80f), strokeWidth = r * 0.13f, cap = StrokeCap.Round)
            }

            "guts_berserk" -> {
                // Layered Black Swordsman / Berserker rib-plates + diagonal bandolier + crimson Brand of Sacrifice
                for (rib in 0..2) {
                    val ry = r * (0.44f + rib * 0.14f)
                    drawLine(
                        color = if (isUlt) Color(0xFFEF4444) else Color(0xFF52525B),
                        start = Offset(-r * (0.58f - rib * 0.08f), ry),
                        end = Offset(r * (0.58f - rib * 0.08f), ry),
                        strokeWidth = r * 0.065f,
                        cap = StrokeCap.Round
                    )
                }
                if (!isUlt) {
                    drawLine(Color(0xFF78350F), Offset(-r * 0.52f, r * 0.38f), Offset(r * 0.42f, r * 0.82f), strokeWidth = r * 0.10f, cap = StrokeCap.Round)
                }
                // Glowing crimson Brand of Sacrifice
                drawLine(Color(0xFFEF4444), Offset(r * 0.34f, r * 0.32f), Offset(r * 0.34f, r * 0.52f), strokeWidth = 2.2f)
                drawLine(Color(0xFFEF4444), Offset(r * 0.27f, r * 0.40f), Offset(r * 0.41f, r * 0.40f), strokeWidth = 2.0f)
            }

            "chainsaw_devil" -> {
                // White shirt + black tie + iconic yellow triangular chainsaw starter pull-cord!
                drawRoundRect(Color(0xFFF8FAFC), Offset(-r * 0.52f, r * 0.48f), Size(r * 1.04f, r * 0.40f), CornerRadius(6f, 6f))
                drawLine(inkColor, Offset(0f, r * 0.48f), Offset(0f, r * 0.86f), strokeWidth = r * 0.13f, cap = StrokeCap.Round)
                sharedPath.reset()
                sharedPath.moveTo(-r * 0.13f, r * 0.64f)
                sharedPath.lineTo(r * 0.13f, r * 0.64f)
                sharedPath.lineTo(0f, r * 0.82f)
                sharedPath.close()
                drawPath(sharedPath, Color(0xFFFACC15))
                drawPath(sharedPath, inkColor, style = Stroke(width = 1.6f))
            }

            "kurosaki_hollow" -> {
                // Shinigami black shihakusho + crisp white inner kimono collar + Hollow hole in Ult
                drawLine(Color.White, Offset(-r * 0.44f, r * 0.40f), Offset(0f, r * 0.78f), strokeWidth = r * 0.09f, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(r * 0.44f, r * 0.40f), Offset(0f, r * 0.78f), strokeWidth = r * 0.09f, cap = StrokeCap.Round)
                if (isUlt) {
                    drawCircle(Color(0xFF09090B), radius = r * 0.13f, center = Offset(0f, r * 0.62f))
                    drawCircle(Color(0xFFEF4444), radius = r * 0.13f, center = Offset(0f, r * 0.62f), style = Stroke(width = 2.0f))
                }
            }

            "caped_baldy" -> {
                // Saitama yellow hero suit + white collar + center zipper + red belt buckle
                drawArc(Color(0xFFFACC15), 20f, 140f, true, Offset(-r * 0.94f, -r * 0.52f), Size(r * 1.88f, r * 1.48f))
                drawLine(Color.White, Offset(-r * 0.46f, r * 0.46f), Offset(r * 0.46f, r * 0.46f), strokeWidth = r * 0.09f, cap = StrokeCap.Round)
                drawLine(inkColor, Offset(0f, r * 0.48f), Offset(0f, r * 0.86f), strokeWidth = 2.0f)
                drawRoundRect(Color(0xFFFACC15), Offset(-r * 0.14f, r * 0.76f), Size(r * 0.28f, r * 0.14f), CornerRadius(3f, 3f))
            }

            "sukuna_shrine" -> {
                // Sukuna white kimono lapels + dark curse tattoo chest bands
                drawLine(Color(0xFFF8FAFC), Offset(-r * 0.48f, r * 0.42f), Offset(0f, r * 0.80f), strokeWidth = r * 0.10f, cap = StrokeCap.Round)
                drawLine(Color(0xFFF8FAFC), Offset(r * 0.48f, r * 0.42f), Offset(0f, r * 0.80f), strokeWidth = r * 0.10f, cap = StrokeCap.Round)
                drawLine(inkColor, Offset(-r * 0.34f, r * 0.56f), Offset(r * 0.34f, r * 0.56f), strokeWidth = 2.6f, cap = StrokeCap.Round)
                drawLine(inkColor, Offset(-r * 0.26f, r * 0.66f), Offset(r * 0.26f, r * 0.66f), strokeWidth = 2.4f, cap = StrokeCap.Round)
            }

            "frieza_emperor" -> {
                // Glossy violet bio-armor chest gem + white/gold shoulder armor plates
                drawOval(Color(0xFF7E22CE), Offset(-r * 0.24f, r * 0.48f), Size(r * 0.48f, r * 0.28f))
                drawOval(Color(0xFFD8B4FE).copy(alpha = 0.65f), Offset(-r * 0.12f, r * 0.52f), Size(r * 0.20f, r * 0.10f))
                drawOval(inkColor, Offset(-r * 0.24f, r * 0.48f), Size(r * 0.48f, r * 0.28f), style = Stroke(width = 1.8f))
            }

            "kujo_star" -> {
                // Stand-up gakuran collar + heavy golden delinquent chain
                drawLine(Color(0xFFFACC15), Offset(-r * 0.48f, r * 0.38f), Offset(-r * 0.08f, r * 0.78f), strokeWidth = r * 0.085f, cap = StrokeCap.Round)
                drawCircle(Color(0xFFFEF08A), radius = r * 0.055f, center = Offset(-r * 0.28f, r * 0.58f))
            }

            "dio_world" -> {
                // Emerald heart chest brooch + golden vampire collar trim
                drawLine(Color(0xFFFACC15), Offset(-r * 0.46f, r * 0.42f), Offset(0f, r * 0.76f), strokeWidth = r * 0.08f, cap = StrokeCap.Round)
                drawLine(Color(0xFFFACC15), Offset(r * 0.46f, r * 0.42f), Offset(0f, r * 0.76f), strokeWidth = r * 0.08f, cap = StrokeCap.Round)
                drawCircle(Color(0xFF22C55E), radius = r * 0.11f, center = Offset(0f, r * 0.62f))
            }

            else -> {
                // Rich procedural anime outfit archetypes across all 200+ roster fighters
                when (char.resolvedCrownStyle) {
                    CrownVisualStyle.SHINOBI_HEADBAND -> {
                        // Shinobi armored flak collar + scroll clasp
                        drawLine(Color(0xFF334155), Offset(-r * 0.50f, r * 0.46f), Offset(r * 0.50f, r * 0.46f), strokeWidth = r * 0.11f, cap = StrokeCap.Round)
                        drawLine(char.auraColor, Offset(0f, r * 0.44f), Offset(0f, r * 0.84f), strokeWidth = 2.4f)
                    }

                    CrownVisualStyle.KNIGHT_HELM, CrownVisualStyle.SAMURAI_KABUTO, CrownVisualStyle.ROYAL_CROWN -> {
                        // Segmented armor breastplate + royal centerpiece gem
                        drawLine(Color(0xFFE2E8F0), Offset(-r * 0.52f, r * 0.46f), Offset(0f, r * 0.76f), strokeWidth = r * 0.085f, cap = StrokeCap.Round)
                        drawLine(Color(0xFFE2E8F0), Offset(r * 0.52f, r * 0.46f), Offset(0f, r * 0.76f), strokeWidth = r * 0.085f, cap = StrokeCap.Round)
                        drawCircle(Color(0xFFFACC15), radius = r * 0.105f, center = Offset(0f, r * 0.60f))
                        drawCircle(char.auraColor, radius = r * 0.06f, center = Offset(0f, r * 0.60f))
                    }

                    CrownVisualStyle.MECHA_VFIN -> {
                        // Mecha chest vents + glowing cockpit reactor core
                        drawRect(Color(0xFFFACC15), Offset(-r * 0.44f, r * 0.52f), Size(r * 0.24f, r * 0.14f))
                        drawRect(Color(0xFFFACC15), Offset(r * 0.20f, r * 0.52f), Size(r * 0.24f, r * 0.14f))
                        drawCircle(Color(0xFF22D3EE), radius = r * 0.11f, center = Offset(0f, r * 0.60f))
                        drawCircle(Color.White, radius = r * 0.05f, center = Offset(0f, r * 0.60f))
                    }

                    CrownVisualStyle.WITCH_HOOD, CrownVisualStyle.TWIN_TAILS, CrownVisualStyle.HALO_CROWN -> {
                        // Arcane mage capelet trim + mystic diamond clasp
                        drawLine(Color(0xFFFACC15), Offset(-r * 0.48f, r * 0.46f), Offset(0f, r * 0.72f), strokeWidth = r * 0.07f, cap = StrokeCap.Round)
                        drawLine(Color(0xFFFACC15), Offset(r * 0.48f, r * 0.46f), Offset(0f, r * 0.72f), strokeWidth = r * 0.07f, cap = StrokeCap.Round)
                        sharedPath.reset()
                        sharedPath.moveTo(0f, r * 0.48f)
                        sharedPath.lineTo(r * 0.10f, r * 0.60f)
                        sharedPath.lineTo(0f, r * 0.72f)
                        sharedPath.lineTo(-r * 0.10f, r * 0.60f)
                        sharedPath.close()
                        drawPath(sharedPath, char.auraColor)
                        drawPath(sharedPath, Color.White, style = Stroke(width = 1.4f))
                    }

                    else -> {
                        // Crossed martial-arts / coat lapels + belt buckle accent
                        drawLine(
                            color = char.auraColor.copy(alpha = 0.85f),
                            start = Offset(-r * 0.46f, r * 0.44f),
                            end = Offset(0f, r * 0.74f),
                            strokeWidth = (r * 0.068f).coerceAtLeast(2.0f),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = char.auraColor.copy(alpha = 0.85f),
                            start = Offset(r * 0.46f, r * 0.44f),
                            end = Offset(0f, r * 0.74f),
                            strokeWidth = (r * 0.068f).coerceAtLeast(2.0f),
                            cap = StrokeCap.Round
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.75f),
                            topLeft = Offset(-r * 0.11f, r * 0.70f),
                            size = Size(r * 0.22f, r * 0.10f),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                }
            }
        }
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

        if (ball.slowTimer > 0f) {
            for (i in 0 until 4) {
                val a = globalTime * 2.5f + i * (PI.toFloat() * 0.5f)
                val p = Offset(center.x + cos(a) * r * 1.16f, center.y + sin(a) * r * 1.16f)
                drawCircle(Color(0xFF38BDF8).copy(alpha = 0.72f), radius = r * 0.08f, center = p)
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
