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
import com.example.model.WeaponVisualType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object WeaponRenderer {

    private val bladePath = Path()
    private val detailPath = Path()
    private val ink = Color(0xFF12151E)

    fun drawWeapon(
        scope: DrawScope,
        character: CharacterData,
        center: Offset,
        radius: Float,
        angle: Float,
        isUltimateActive: Boolean,
        spinBurst: Float,
        weaponScale: Float = CombatBall.computeUltimateWeaponScale(character, isUltimateActive),
        globalTime: Float = 0f
    ) {
        with(scope) {
            val clampedScale = weaponScale.coerceIn(0.85f, 2.25f)
            val isGuts = character.id == "guts_berserk"
            val orbitFactor = 1.26f + (clampedScale - 1f) * (if (isGuts) 0.36f else 0.26f)
            val orbitDist = radius * orbitFactor
            val weaponPos = Offset(
                center.x + cos(angle) * orbitDist,
                center.y + sin(angle) * orbitDist
            )

            // High-velocity weapon slash arc + Ultimate energy cleave ribbon
            if (spinBurst > 1.6f || isUltimateActive) {
                val deg = Math.toDegrees(angle.toDouble()).toFloat()
                val sweep = when {
                    isUltimateActive && isGuts -> 158f
                    isUltimateActive -> 126f
                    else -> 88f
                }
                val slashColor = if (isUltimateActive && isGuts) Color(0xFFDC2626) else character.auraColor
                drawArc(
                    color = slashColor.copy(alpha = if (isUltimateActive) 0.72f else 0.48f),
                    startAngle = deg - sweep,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - orbitDist, center.y - orbitDist),
                    size = Size(orbitDist * 2f, orbitDist * 2f),
                    style = Stroke(width = radius * (if (isUltimateActive) 0.16f else 0.11f), cap = StrokeCap.Round)
                )
                drawArc(
                    color = (if (isUltimateActive && isGuts) Color(0xFF09090B) else Color.White)
                        .copy(alpha = if (isUltimateActive) 0.62f else 0.30f),
                    startAngle = deg - sweep * 0.68f,
                    sweepAngle = sweep * 0.68f,
                    useCenter = false,
                    topLeft = Offset(center.x - orbitDist, center.y - orbitDist),
                    size = Size(orbitDist * 2f, orbitDist * 2f),
                    style = Stroke(width = radius * 0.052f, cap = StrokeCap.Round)
                )
            }

            val u = radius * 0.29f * clampedScale
            val rotDeg = Math.toDegrees(angle.toDouble()).toFloat() + 90f

            // Bespoke iconic Dragon Slayer slab greatsword for Guts (grows 1.92x colossal in Berserker Ultimate!)
            if (isGuts) {
                drawGutsDragonSlayer(
                    pos = weaponPos,
                    rotDeg = rotDeg,
                    u = u,
                    isUltimateActive = isUltimateActive,
                    globalTime = globalTime
                )
                return
            }

            when (character.weaponType) {
                WeaponVisualType.KI_HALO -> {
                    val glow = if (isUltimateActive) Color(0xFF38BDF8) else character.auraColor
                    val count = if (isUltimateActive) 3 else 2
                    for (i in 0 until count) {
                        val subA = angle + i * (2f * PI.toFloat() / count)
                        val p = Offset(center.x + cos(subA) * orbitDist, center.y + sin(subA) * orbitDist)
                        drawCircle(glow.copy(alpha = 0.36f), radius = u * 1.28f, center = p)
                        drawCircle(glow, radius = u * 0.88f, center = p)
                        drawCircle(Color.White, radius = u * 0.44f, center = p)
                        drawCircle(ink, radius = u * 0.88f, center = p, style = Stroke(width = 2.2f))
                    }
                }

                WeaponVisualType.NICHIRIN_KATANA -> {
                    drawSingleKatana(
                        pos = weaponPos,
                        rotDeg = rotDeg,
                        u = u * 1.05f,
                        bladeColor = if (isUltimateActive) character.auraColor else Color(0xFF1E293B),
                        edgeColor = if (isUltimateActive) Color(0xFFFEF08A) else Color(0xFFE2E8F0),
                        hiltColor = character.secondaryColor,
                        flameAura = if (isUltimateActive) character.auraColor else null
                    )
                }

                WeaponVisualType.TRIPLE_KATANA -> {
                    for (i in 0 until 3) {
                        val hiltColor = when (i) {
                            0 -> Color(0xFFF8FAFC)
                            1 -> Color(0xFFA855F7)
                            else -> Color(0xFFDC2626)
                        }
                        val subAngle = angle + i * (2f * PI.toFloat() / 3f)
                        val p = Offset(center.x + cos(subAngle) * orbitDist, center.y + sin(subAngle) * orbitDist)
                        val subRot = Math.toDegrees(subAngle.toDouble()).toFloat() + 90f
                        drawSingleKatana(
                            pos = p,
                            rotDeg = subRot,
                            u = u * 0.88f,
                            bladeColor = if (isUltimateActive) character.auraColor else Color(0xFFCBD5E1),
                            edgeColor = Color.White,
                            hiltColor = hiltColor,
                            flameAura = if (isUltimateActive) character.auraColor else null
                        )
                    }
                }

                WeaponVisualType.ZANGETSU_BLADE -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        if (isUltimateActive) {
                            drawOval(
                                color = character.auraColor.copy(alpha = 0.34f),
                                topLeft = Offset(-u * 0.90f, -u * 2.40f),
                                size = Size(u * 1.80f, u * 3.40f)
                            )
                        }
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 2.18f)
                        bladePath.lineTo(u * 0.66f, u * 0.55f)
                        bladePath.lineTo(-u * 0.28f, u * 0.78f)
                        bladePath.close()
                        drawPath(bladePath, if (isUltimateActive) Color(0xFF09090B) else Color(0xFF334155))
                        drawPath(
                            bladePath,
                            if (isUltimateActive) character.auraColor else ink,
                            style = Stroke(width = 2.3f, join = StrokeJoin.Round)
                        )
                        drawLine(
                            if (isUltimateActive) Color(0xFFEF4444) else Color(0xFFF8FAFC),
                            Offset(0f, -u * 2.0f),
                            Offset(u * 0.54f, u * 0.48f),
                            strokeWidth = 3.0f,
                            cap = StrokeCap.Round
                        )
                        drawLine(Color(0xFFDC2626), Offset(0f, u * 0.78f), Offset(0f, u * 1.52f), strokeWidth = u * 0.26f, cap = StrokeCap.Round)
                    }
                }

                WeaponVisualType.GRIMOIRE_GREATSWORD -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        if (isUltimateActive) {
                            drawOval(
                                color = Color(0xFFDC2626).copy(alpha = 0.32f),
                                topLeft = Offset(-u * 0.96f, -u * 2.45f),
                                size = Size(u * 1.92f, u * 3.45f)
                            )
                        }
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 2.25f)
                        bladePath.lineTo(u * 0.74f, u * 0.54f)
                        bladePath.lineTo(-u * 0.74f, u * 0.54f)
                        bladePath.close()
                        drawPath(bladePath, if (isUltimateActive) Color(0xFF09090B) else Color(0xFF1F2937))
                        drawPath(
                            bladePath,
                            if (isUltimateActive) Color(0xFFEF4444) else ink,
                            style = Stroke(width = 2.6f, join = StrokeJoin.Round)
                        )
                        drawLine(
                            if (isUltimateActive) Color(0xFFEF4444) else character.auraColor,
                            Offset(0f, -u * 1.65f),
                            Offset(0f, u * 0.38f),
                            strokeWidth = 3.2f,
                            cap = StrokeCap.Round
                        )
                        drawLine(Color(0xFFFACC15), Offset(-u * 0.80f, u * 0.54f), Offset(u * 0.80f, u * 0.54f), strokeWidth = u * 0.22f, cap = StrokeCap.Round)
                        drawLine(Color(0xFF52525B), Offset(0f, u * 0.54f), Offset(0f, u * 1.48f), strokeWidth = u * 0.30f, cap = StrokeCap.Round)
                    }
                }

                WeaponVisualType.LIMITLESS_ORBS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    if (isUltimateActive) {
                        // Hollow Purple gravitational singularity
                        drawCircle(Color(0xFFA855F7).copy(alpha = 0.36f), radius = u * 1.55f, center = weaponPos)
                        drawCircle(Color(0xFF7E22CE), radius = u * 1.12f, center = weaponPos)
                        drawCircle(Color(0xFFE9D5FF), radius = u * 0.76f, center = weaponPos, style = Stroke(width = 2.4f))
                        drawCircle(Color.White, radius = u * 0.44f, center = weaponPos)
                        drawCircle(ink, radius = u * 1.12f, center = weaponPos, style = Stroke(width = 2.2f))
                    } else {
                        drawCircle(Color(0xFFEF4444), radius = u * 0.82f, center = weaponPos)
                        drawCircle(Color.White, radius = u * 0.32f, center = weaponPos)
                        drawCircle(ink, radius = u * 0.82f, center = weaponPos, style = Stroke(width = 2f))
                        drawCircle(Color(0xFF3B82F6), radius = u * 0.82f, center = opp)
                        drawCircle(Color.White, radius = u * 0.32f, center = opp)
                        drawCircle(ink, radius = u * 0.82f, center = opp, style = Stroke(width = 2f))
                    }
                }

                WeaponVisualType.LIGHTNING_KUNAI -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    for (idx in 0..1) {
                        val pos = if (idx == 0) weaponPos else opp
                        withTransform({
                            translate(pos.x, pos.y)
                            rotate(rotDeg + idx * 180f, pivot = Offset.Zero)
                        }) {
                            if (isUltimateActive) {
                                drawCircle(Color(0xFF38BDF8).copy(alpha = 0.34f), radius = u * 1.15f, center = Offset(0f, -u * 0.45f))
                            }
                            bladePath.reset()
                            bladePath.moveTo(0f, -u * 1.55f)
                            bladePath.lineTo(u * 0.56f, 0f)
                            bladePath.lineTo(0f, u * 0.38f)
                            bladePath.lineTo(-u * 0.56f, 0f)
                            bladePath.close()
                            drawPath(bladePath, if (isUltimateActive) Color(0xFF38BDF8) else Color(0xFFE2E8F0))
                            drawPath(bladePath, ink, style = Stroke(width = 2f))
                            drawCircle(character.auraColor, radius = u * 0.30f, center = Offset(0f, u * 0.76f), style = Stroke(width = 2.5f))
                        }
                    }
                }

                WeaponVisualType.SERIOUS_GLOVE -> {
                    if (isUltimateActive) {
                        drawCircle(Color(0xFFFACC15).copy(alpha = 0.40f), radius = u * 1.42f, center = weaponPos)
                        drawCircle(Color(0xFFEF4444), radius = u * 1.24f, center = weaponPos, style = Stroke(width = 2.6f))
                    }
                    drawCircle(Color(0xFFDC2626), radius = u * 1.04f, center = weaponPos)
                    drawCircle(ink, radius = u * 1.04f, center = weaponPos, style = Stroke(width = 2.6f))
                    drawCircle(Color.White.copy(alpha = 0.85f), radius = u * 0.30f, center = Offset(weaponPos.x - u * 0.25f, weaponPos.y - u * 0.25f))
                }

                WeaponVisualType.GOMU_FIST -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    val c = if (isUltimateActive) Color(0xFFF8FAFC) else Color(0xFF18181B)
                    for (idx in 0..1) {
                        val pos = if (idx == 0) weaponPos else opp
                        if (isUltimateActive) {
                            drawCircle(Color.White.copy(alpha = 0.35f), radius = u * 1.22f, center = pos)
                        }
                        drawCircle(c, radius = u * 0.92f, center = pos)
                        drawCircle(character.auraColor, radius = u * 0.92f, center = pos, style = Stroke(width = 2.6f))
                        drawCircle(Color.White.copy(alpha = 0.48f), radius = u * 0.25f, center = Offset(pos.x - u * 0.22f, pos.y - u * 0.22f))
                    }
                }

                WeaponVisualType.CHAINSAW_BLADE -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        if (isUltimateActive) {
                            drawOval(Color(0xFFEF4444).copy(alpha = 0.32f), Offset(-u * 0.72f, -u * 1.75f), Size(u * 1.44f, u * 2.95f))
                        }
                        drawRoundRect(
                            color = if (isUltimateActive) Color(0xFFDC2626) else Color(0xFFEA580C),
                            topLeft = Offset(-u * 0.38f, -u * 1.48f),
                            size = Size(u * 0.76f, u * 2.48f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = ink,
                            topLeft = Offset(-u * 0.38f, -u * 1.48f),
                            size = Size(u * 0.76f, u * 2.48f),
                            cornerRadius = CornerRadius(6f, 6f),
                            style = Stroke(width = 2.2f)
                        )
                        val toothShift = if (isUltimateActive) sin(globalTime * 28f) * u * 0.08f else 0f
                        for (t in -3..2) {
                            drawCircle(Color(0xFFF8FAFC), radius = u * 0.20f, center = Offset(u * 0.45f, t * u * 0.38f + toothShift))
                            drawCircle(Color(0xFFF8FAFC), radius = u * 0.20f, center = Offset(-u * 0.45f, t * u * 0.38f - toothShift))
                        }
                    }
                }

                WeaponVisualType.CURSED_CLEAVE -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    for (idx in 0..1) {
                        val pos = if (idx == 0) weaponPos else opp
                        drawArc(
                            color = character.auraColor,
                            startAngle = rotDeg - 56f + idx * 180f,
                            sweepAngle = 112f,
                            useCenter = false,
                            topLeft = Offset(pos.x - u * 1.08f, pos.y - u * 1.08f),
                            size = Size(u * 2.16f, u * 2.16f),
                            style = Stroke(width = u * 0.34f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color.White,
                            startAngle = rotDeg - 36f + idx * 180f,
                            sweepAngle = 72f,
                            useCenter = false,
                            topLeft = Offset(pos.x - u * 1.08f, pos.y - u * 1.08f),
                            size = Size(u * 2.16f, u * 2.16f),
                            style = Stroke(width = u * 0.12f, cap = StrokeCap.Round)
                        )
                    }
                }

                WeaponVisualType.TITAN_BLADES -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    val aura = if (isUltimateActive) character.auraColor else null
                    drawSingleKatana(weaponPos, rotDeg, u * 0.92f, Color(0xFFE2E8F0), Color.White, Color(0xFF57534E), aura)
                    drawSingleKatana(opp, rotDeg + 180f, u * 0.92f, Color(0xFFE2E8F0), Color.White, Color(0xFF57534E), aura)
                }

                WeaponVisualType.DRAGON_CLAW -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        if (isUltimateActive) {
                            drawCircle(character.auraColor.copy(alpha = 0.36f), radius = u * 1.32f, center = Offset.Zero)
                        }
                        drawCircle(character.auraColor, radius = u * 0.92f, center = Offset.Zero)
                        drawCircle(Color(0xFFFEF08A), radius = u * 0.48f, center = Offset.Zero)
                        for (c in -1..1) {
                            drawLine(Color.White, Offset(c * u * 0.34f, -u * 0.45f), Offset(c * u * 0.44f, -u * 1.46f), strokeWidth = u * 0.19f, cap = StrokeCap.Round)
                        }
                        drawCircle(ink, radius = u * 0.92f, center = Offset.Zero, style = Stroke(width = 2.2f))
                    }
                }

                WeaponVisualType.SHADOW_DAGGERS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    val aura = if (isUltimateActive) Color(0xFFA855F7) else null
                    drawSingleKatana(weaponPos, rotDeg, u * 0.86f, character.auraColor, Color(0xFFE9D5FF), Color(0xFF0F172A), aura)
                    drawSingleKatana(opp, rotDeg + 180f, u * 0.86f, character.auraColor, Color(0xFFE9D5FF), Color(0xFF0F172A), aura)
                }

                WeaponVisualType.PSYCHIC_DEBRIS -> {
                    val count = if (isUltimateActive) 4 else 3
                    for (i in 0 until count) {
                        val a = angle + i * (2f * PI.toFloat() / count)
                        val p = Offset(center.x + cos(a) * orbitDist, center.y + sin(a) * orbitDist)
                        withTransform({
                            translate(p.x, p.y)
                            rotate(rotDeg + i * 45f, pivot = Offset.Zero)
                        }) {
                            drawRoundRect(
                                color = character.auraColor,
                                topLeft = Offset(-u * 0.44f, -u * 0.44f),
                                size = Size(u * 0.88f, u * 0.88f),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(-u * 0.44f, -u * 0.44f),
                                size = Size(u * 0.88f, u * 0.88f),
                                cornerRadius = CornerRadius(4f, 4f),
                                style = Stroke(width = 1.9f)
                            )
                        }
                    }
                }

                WeaponVisualType.ALCHEMY_SPEAR -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        drawLine(Color(0xFF71717A), Offset(0f, -u * 1.1f), Offset(0f, u * 1.55f), strokeWidth = u * 0.25f, cap = StrokeCap.Round)
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 2.20f)
                        bladePath.lineTo(u * 0.56f, -u * 0.82f)
                        bladePath.lineTo(0f, -u * 1.05f)
                        bladePath.lineTo(-u * 0.56f, -u * 0.82f)
                        bladePath.close()
                        drawPath(bladePath, if (isUltimateActive) character.auraColor else Color(0xFFE4E4E7))
                        drawPath(bladePath, ink, style = Stroke(width = 2.1f, join = StrokeJoin.Round))
                    }
                }

                WeaponVisualType.STAR_FISTS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    for (idx in 0..1) {
                        val pos = if (idx == 0) weaponPos else opp
                        drawCircle(character.auraColor, radius = u * 0.86f, center = pos)
                        drawCircle(Color(0xFFFACC15), radius = u * 0.86f, center = pos, style = Stroke(width = 2.8f))
                        drawCircle(Color.White, radius = u * 0.26f, center = pos)
                    }
                }

                WeaponVisualType.ALLOY_YOYOS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    for (idx in 0..1) {
                        val pos = if (idx == 0) weaponPos else opp
                        drawLine(character.auraColor.copy(alpha = 0.75f), center, pos, strokeWidth = 1.8f)
                        drawCircle(Color(0xFFE2E8F0), radius = u * 0.78f, center = pos)
                        drawCircle(character.auraColor, radius = u * 0.42f, center = pos)
                        drawCircle(ink, radius = u * 0.78f, center = pos, style = Stroke(width = 2.1f))
                    }
                }

                WeaponVisualType.HELLSING_PISTOLS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    drawPistol(weaponPos, rotDeg, u, Color(0xFFE2E8F0), character.auraColor, isUltimateActive)
                    drawPistol(opp, rotDeg + 180f, u, Color(0xFF27272A), character.auraColor, isUltimateActive)
                }

                WeaponVisualType.MAGE_STAFF -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        if (isUltimateActive) {
                            drawCircle(character.auraColor.copy(alpha = 0.34f), radius = u * 0.92f, center = Offset(0f, -u * 1.25f))
                        }
                        drawLine(Color(0xFF92400E), Offset(0f, -u * 1.05f), Offset(0f, u * 1.55f), strokeWidth = u * 0.25f, cap = StrokeCap.Round)
                        drawArc(
                            color = Color(0xFFFACC15),
                            startAngle = 35f,
                            sweepAngle = 290f,
                            useCenter = false,
                            topLeft = Offset(-u * 0.65f, -u * 1.90f),
                            size = Size(u * 1.30f, u * 1.30f),
                            style = Stroke(width = u * 0.26f, cap = StrokeCap.Round)
                        )
                        drawCircle(character.auraColor, radius = u * 0.36f, center = Offset(0f, -u * 1.25f))
                        drawCircle(Color.White, radius = u * 0.16f, center = Offset(0f, -u * 1.25f))
                    }
                }

                WeaponVisualType.CRESCENT_SCYTHE -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        drawLine(Color(0xFF1E293B), Offset(0f, -u * 1.35f), Offset(0f, u * 1.65f), strokeWidth = u * 0.24f, cap = StrokeCap.Round)
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 1.38f)
                        bladePath.quadraticTo(u * 1.72f, -u * 1.48f, u * 1.92f, -u * 0.12f)
                        bladePath.quadraticTo(u * 1.05f, -u * 0.78f, 0f, -u * 0.88f)
                        bladePath.close()
                        drawPath(bladePath, if (isUltimateActive) character.auraColor else Color(0xFFE11D48))
                        drawPath(bladePath, Color.White, style = Stroke(width = 1.9f, join = StrokeJoin.Round))
                    }
                }

                WeaponVisualType.SPIRAL_DRILL -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        if (isUltimateActive) {
                            drawOval(Color(0xFF22D3EE).copy(alpha = 0.34f), Offset(-u * 1.05f, -u * 2.35f), Size(u * 2.10f, u * 3.20f))
                        }
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 2.22f)
                        bladePath.lineTo(u * 0.80f, u * 0.65f)
                        bladePath.lineTo(-u * 0.80f, u * 0.65f)
                        bladePath.close()
                        drawPath(bladePath, if (isUltimateActive) Color(0xFF22D3EE) else Color(0xFFFACC15))
                        drawPath(bladePath, ink, style = Stroke(width = 2.3f, join = StrokeJoin.Round))
                        for (g in -1..1) {
                            val y = g * u * 0.52f - u * 0.35f
                            drawLine(Color.White, Offset(-u * 0.44f, y), Offset(u * 0.44f, y + u * 0.22f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                        }
                    }
                }

                WeaponVisualType.SPIRIT_BOW -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        drawArc(
                            color = character.auraColor,
                            startAngle = -75f,
                            sweepAngle = 150f,
                            useCenter = false,
                            topLeft = Offset(-u * 0.95f, -u * 1.45f),
                            size = Size(u * 1.90f, u * 2.90f),
                            style = Stroke(width = u * 0.26f, cap = StrokeCap.Round)
                        )
                        drawLine(Color.White, Offset(u * 0.25f, -u * 1.38f), Offset(u * 0.25f, u * 1.38f), strokeWidth = 1.8f)
                        drawLine(Color(0xFFFEF08A), Offset(-u * 0.35f, 0f), Offset(u * 1.42f, 0f), strokeWidth = u * 0.20f, cap = StrokeCap.Round)
                    }
                }

                WeaponVisualType.MECHA_FUNNELS -> {
                    for (i in 0 until 3) {
                        val a = angle + i * (2f * PI.toFloat() / 3f)
                        val p = Offset(center.x + cos(a) * orbitDist, center.y + sin(a) * orbitDist)
                        val rDeg = Math.toDegrees(a.toDouble()).toFloat() + 90f
                        withTransform({
                            translate(p.x, p.y)
                            rotate(rDeg, pivot = Offset.Zero)
                        }) {
                            bladePath.reset()
                            bladePath.moveTo(0f, -u * 1.18f)
                            bladePath.lineTo(u * 0.56f, u * 0.68f)
                            bladePath.lineTo(0f, u * 0.35f)
                            bladePath.lineTo(-u * 0.56f, u * 0.68f)
                            bladePath.close()
                            drawPath(bladePath, Color(0xFFF8FAFC))
                            drawPath(bladePath, ink, style = Stroke(width = 2f, join = StrokeJoin.Round))
                            drawCircle(character.auraColor, radius = u * 0.25f, center = Offset(0f, -u * 0.15f))
                        }
                    }
                }

                WeaponVisualType.ROYAL_GATES -> {
                    for (i in 0 until 3) {
                        val a = angle + i * (2f * PI.toFloat() / 3f)
                        val p = Offset(center.x + cos(a) * orbitDist, center.y + sin(a) * orbitDist)
                        val rDeg = Math.toDegrees(a.toDouble()).toFloat() + 90f
                        drawCircle(Color(0xFFFACC15).copy(alpha = 0.55f), radius = u * 0.74f, center = p)
                        drawCircle(Color(0xFFFACC15), radius = u * 0.74f, center = p, style = Stroke(width = 2.2f))
                        drawSingleKatana(p, rDeg, u * 0.78f, Color(0xFFFEF08A), Color.White, Color(0xFFB45309), null)
                    }
                }

                WeaponVisualType.DEMON_KANABO -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        drawRoundRect(
                            color = Color(0xFF27272A),
                            topLeft = Offset(-u * 0.46f, -u * 1.65f),
                            size = Size(u * 0.92f, u * 2.45f),
                            cornerRadius = CornerRadius(u * 0.35f, u * 0.35f)
                        )
                        drawRoundRect(
                            color = if (isUltimateActive) character.auraColor else ink,
                            topLeft = Offset(-u * 0.46f, -u * 1.65f),
                            size = Size(u * 0.92f, u * 2.45f),
                            cornerRadius = CornerRadius(u * 0.35f, u * 0.35f),
                            style = Stroke(width = 2.4f)
                        )
                        for (s in -2..1) {
                            val sy = s * u * 0.52f - u * 0.25f
                            drawCircle(Color(0xFFFACC15), radius = u * 0.16f, center = Offset(-u * 0.48f, sy))
                            drawCircle(Color(0xFFFACC15), radius = u * 0.16f, center = Offset(u * 0.48f, sy))
                        }
                        drawLine(Color(0xFF991B1B), Offset(0f, u * 0.80f), Offset(0f, u * 1.48f), strokeWidth = u * 0.28f, cap = StrokeCap.Round)
                    }
                }

                WeaponVisualType.CHAKRAM_RINGS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    for (idx in 0..1) {
                        val pos = if (idx == 0) weaponPos else opp
                        drawCircle(character.auraColor, radius = u * 0.88f, center = pos, style = Stroke(width = u * 0.28f))
                        drawCircle(Color.White, radius = u * 0.88f, center = pos, style = Stroke(width = 1.6f))
                        drawCircle(ink, radius = u * 1.02f, center = pos, style = Stroke(width = 1.8f))
                    }
                }

                WeaponVisualType.ARCANE_CARDS -> {
                    for (i in 0 until 4) {
                        val a = angle + i * (PI.toFloat() / 2f)
                        val p = Offset(center.x + cos(a) * orbitDist, center.y + sin(a) * orbitDist)
                        val rDeg = Math.toDegrees(a.toDouble()).toFloat() + 90f
                        withTransform({
                            translate(p.x, p.y)
                            rotate(rDeg, pivot = Offset.Zero)
                        }) {
                            drawRoundRect(
                                color = Color(0xFFF8FAFC),
                                topLeft = Offset(-u * 0.38f, -u * 0.56f),
                                size = Size(u * 0.76f, u * 1.12f),
                                cornerRadius = CornerRadius(3f, 3f)
                            )
                            drawRoundRect(
                                color = character.auraColor,
                                topLeft = Offset(-u * 0.38f, -u * 0.56f),
                                size = Size(u * 0.76f, u * 1.12f),
                                cornerRadius = CornerRadius(3f, 3f),
                                style = Stroke(width = 2.2f)
                            )
                            drawCircle(character.auraColor, radius = u * 0.16f, center = Offset.Zero)
                        }
                    }
                }

                WeaponVisualType.EXCALIBUR_BLADE -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        if (isUltimateActive) {
                            drawOval(
                                color = Color(0xFFFACC15).copy(alpha = 0.38f),
                                topLeft = Offset(-u * 0.85f, -u * 2.55f),
                                size = Size(u * 1.70f, u * 3.45f)
                            )
                        }
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 2.28f)
                        bladePath.lineTo(u * 0.46f, u * 0.55f)
                        bladePath.lineTo(-u * 0.46f, u * 0.55f)
                        bladePath.close()
                        drawPath(bladePath, if (isUltimateActive) Color(0xFFFEF08A) else Color(0xFFF8FAFC))
                        drawPath(bladePath, Color(0xFFFACC15), style = Stroke(width = 2.4f, join = StrokeJoin.Round))
                        drawLine(Color(0xFF2563EB), Offset(0f, -u * 1.55f), Offset(0f, u * 0.42f), strokeWidth = 2.6f, cap = StrokeCap.Round)
                        drawLine(Color(0xFFFACC15), Offset(-u * 0.72f, u * 0.58f), Offset(u * 0.72f, u * 0.58f), strokeWidth = u * 0.24f, cap = StrokeCap.Round)
                        drawLine(Color(0xFF1E3A8A), Offset(0f, u * 0.60f), Offset(0f, u * 1.45f), strokeWidth = u * 0.26f, cap = StrokeCap.Round)
                    }
                }

                WeaponVisualType.CYBER_CANNON -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(-u * 0.46f, -u * 1.45f),
                            size = Size(u * 0.92f, u * 2.25f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = character.auraColor,
                            topLeft = Offset(-u * 0.46f, -u * 1.45f),
                            size = Size(u * 0.92f, u * 2.25f),
                            cornerRadius = CornerRadius(6f, 6f),
                            style = Stroke(width = 2.2f)
                        )
                        drawCircle(character.auraColor, radius = u * 0.36f, center = Offset(0f, -u * 1.45f))
                        drawCircle(Color.White, radius = u * 0.16f, center = Offset(0f, -u * 1.45f))
                    }
                }
            }
        }
    }

    /**
     * Guts' iconic Dragon Slayer: a massive, thick slab of raw black iron with wide silver beveled
     * cutting edges, wrapped two-handed hilt, and iron pommel ring. During Berserker Armor Ultimate,
     * `u` is already scaled by 1.92x AND the blade ignites with dark-crimson Berserker aura + blood lightning!
     */
    private fun DrawScope.drawGutsDragonSlayer(
        pos: Offset,
        rotDeg: Float,
        u: Float,
        isUltimateActive: Boolean,
        globalTime: Float
    ) {
        withTransform({
            translate(pos.x, pos.y)
            rotate(rotDeg, pivot = Offset.Zero)
        }) {
            val halfW = u * 0.80f
            val tipY = -u * 2.58f
            val shoulderY = -u * 2.02f
            val baseY = u * 0.62f

            if (isUltimateActive) {
                // Colossal Berserker blood-mist aura enveloping the giant Dragon Slayer
                drawOval(
                    color = Color(0xFFDC2626).copy(alpha = 0.36f),
                    topLeft = Offset(-halfW * 1.65f, tipY - u * 0.25f),
                    size = Size(halfW * 3.30f, (baseY - tipY) + u * 1.15f)
                )
            }

            // Outer silver-steel beveled slab
            bladePath.reset()
            bladePath.moveTo(0f, tipY)
            bladePath.lineTo(halfW, shoulderY)
            bladePath.lineTo(halfW, baseY)
            bladePath.lineTo(-halfW, baseY)
            bladePath.lineTo(-halfW, shoulderY)
            bladePath.close()
            drawPath(bladePath, if (isUltimateActive) Color(0xFFE2E8F0) else Color(0xFFCBD5E1))

            // Inner raw dark-iron core slab (characteristic Dragon Slayer two-tone iron look)
            val coreW = halfW * 0.58f
            detailPath.reset()
            detailPath.moveTo(0f, tipY + u * 0.34f)
            detailPath.lineTo(coreW, shoulderY + u * 0.10f)
            detailPath.lineTo(coreW, baseY - u * 0.04f)
            detailPath.lineTo(-coreW, baseY - u * 0.04f)
            detailPath.lineTo(-coreW, shoulderY + u * 0.10f)
            detailPath.close()
            drawPath(detailPath, if (isUltimateActive) Color(0xFF09090B) else Color(0xFF1E293B))

            // Crisp manga ink outline (glowing crimson rim in Berserker Armor!)
            drawPath(
                bladePath,
                if (isUltimateActive) Color(0xFFEF4444) else ink,
                style = Stroke(width = if (isUltimateActive) 2.8f else 2.2f, join = StrokeJoin.Round)
            )

            // Center ridge / fuller + ricasso collar link
            drawLine(
                color = if (isUltimateActive) Color(0xFFEF4444) else Color(0xFF64748B),
                start = Offset(0f, tipY + u * 0.42f),
                end = Offset(0f, baseY - u * 0.12f),
                strokeWidth = if (isUltimateActive) 3.4f else 2.0f,
                cap = StrokeCap.Round
            )

            // Crackling Berserker dark-red lightning arcs along the enlarged blade
            if (isUltimateActive) {
                val j1 = sin(globalTime * 22f) * halfW * 0.55f
                val j2 = cos(globalTime * 19f) * halfW * 0.55f
                drawLine(Color(0xFFFCA5A5), Offset(-halfW * 0.45f, -u * 1.85f), Offset(j1, -u * 1.15f), strokeWidth = 2.4f, cap = StrokeCap.Round)
                drawLine(Color(0xFFEF4444), Offset(j1, -u * 1.15f), Offset(j2, -u * 0.35f), strokeWidth = 2.6f, cap = StrokeCap.Round)
                drawLine(Color(0xFFFCA5A5), Offset(j2, -u * 0.35f), Offset(halfW * 0.42f, u * 0.32f), strokeWidth = 2.2f, cap = StrokeCap.Round)
            }

            // Heavy iron guard collar + cloth-wrapped greatsword handle + pommel ring
            drawRoundRect(
                color = Color(0xFF71717A),
                topLeft = Offset(-halfW * 0.86f, baseY - u * 0.06f),
                size = Size(halfW * 1.72f, u * 0.24f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawLine(
                color = Color(0xFF44403C),
                start = Offset(0f, baseY + u * 0.18f),
                end = Offset(0f, baseY + u * 1.08f),
                strokeWidth = u * 0.28f,
                cap = StrokeCap.Round
            )
            // White grip bandage wraps
            for (w in 0..2) {
                val wy = baseY + u * (0.34f + w * 0.26f)
                drawLine(Color(0xFFE7E5E4), Offset(-u * 0.14f, wy), Offset(u * 0.14f, wy + u * 0.06f), strokeWidth = 1.8f)
            }
            drawCircle(
                color = Color(0xFF94A3B8),
                radius = u * 0.16f,
                center = Offset(0f, baseY + u * 1.20f),
                style = Stroke(width = 2.2f)
            )
        }
    }

    private fun DrawScope.drawSingleKatana(
        pos: Offset,
        rotDeg: Float,
        u: Float,
        bladeColor: Color,
        edgeColor: Color,
        hiltColor: Color,
        flameAura: Color?
    ) {
        withTransform({
            translate(pos.x, pos.y)
            rotate(rotDeg, pivot = Offset.Zero)
        }) {
            if (flameAura != null) {
                drawOval(
                    color = flameAura.copy(alpha = 0.30f),
                    topLeft = Offset(-u * 0.45f, -u * 1.88f),
                    size = Size(u * 0.90f, u * 2.70f)
                )
            }
            drawLine(bladeColor, Offset(0f, -u * 1.72f), Offset(0f, u * 0.72f), strokeWidth = u * 0.31f, cap = StrokeCap.Round)
            drawLine(edgeColor, Offset(u * 0.06f, -u * 1.60f), Offset(u * 0.06f, u * 0.62f), strokeWidth = u * 0.11f, cap = StrokeCap.Round)
            drawLine(Color(0xFFFACC15), Offset(-u * 0.50f, u * 0.74f), Offset(u * 0.50f, u * 0.74f), strokeWidth = u * 0.21f, cap = StrokeCap.Round)
            drawLine(hiltColor, Offset(0f, u * 0.74f), Offset(0f, u * 1.48f), strokeWidth = u * 0.27f, cap = StrokeCap.Round)
        }
    }

    private fun DrawScope.drawPistol(
        pos: Offset,
        rotDeg: Float,
        u: Float,
        barrelColor: Color,
        glowColor: Color,
        isUltimateActive: Boolean
    ) {
        withTransform({
            translate(pos.x, pos.y)
            rotate(rotDeg, pivot = Offset.Zero)
        }) {
            if (isUltimateActive) {
                drawCircle(glowColor.copy(alpha = 0.42f), radius = u * 0.42f, center = Offset(0f, -u * 1.48f))
            }
            drawRoundRect(
                color = barrelColor,
                topLeft = Offset(-u * 0.25f, -u * 1.35f),
                size = Size(u * 0.50f, u * 1.70f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = ink,
                topLeft = Offset(-u * 0.25f, -u * 1.35f),
                size = Size(u * 0.50f, u * 1.70f),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 1.8f)
            )
            drawCircle(glowColor, radius = u * 0.14f, center = Offset(0f, -u * 1.35f))
            drawRoundRect(
                color = Color(0xFF451A03),
                topLeft = Offset(-u * 0.24f, u * 0.18f),
                size = Size(u * 0.66f, u * 0.52f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}
