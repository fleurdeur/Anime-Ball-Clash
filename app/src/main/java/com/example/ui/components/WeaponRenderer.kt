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
import com.example.model.WeaponVisualType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object WeaponRenderer {

    private val bladePath = Path()
    private val ink = Color(0xFF12151E)

    fun drawWeapon(
        scope: DrawScope,
        character: CharacterData,
        center: Offset,
        radius: Float,
        angle: Float,
        isUltimateActive: Boolean,
        spinBurst: Float
    ) {
        with(scope) {
            val orbitDist = radius * (if (isUltimateActive) 1.32f else 1.26f)
            val weaponPos = Offset(
                center.x + cos(angle) * orbitDist,
                center.y + sin(angle) * orbitDist
            )

            // High-velocity weapon slash arc + inner energy ribbon
            if (spinBurst > 1.8f || isUltimateActive) {
                val deg = Math.toDegrees(angle.toDouble()).toFloat()
                val sweep = if (isUltimateActive) 118f else 88f
                drawArc(
                    color = character.auraColor.copy(alpha = if (isUltimateActive) 0.68f else 0.50f),
                    startAngle = deg - sweep,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - orbitDist, center.y - orbitDist),
                    size = Size(orbitDist * 2f, orbitDist * 2f),
                    style = Stroke(width = radius * 0.12f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color.White.copy(alpha = if (isUltimateActive) 0.55f else 0.30f),
                    startAngle = deg - sweep * 0.65f,
                    sweepAngle = sweep * 0.65f,
                    useCenter = false,
                    topLeft = Offset(center.x - orbitDist, center.y - orbitDist),
                    size = Size(orbitDist * 2f, orbitDist * 2f),
                    style = Stroke(width = radius * 0.045f, cap = StrokeCap.Round)
                )
            }

            val u = radius * 0.29f
            val rotDeg = Math.toDegrees(angle.toDouble()).toFloat() + 90f

            when (character.weaponType) {
                WeaponVisualType.KI_HALO -> {
                    val glow = if (isUltimateActive) Color(0xFF38BDF8) else character.auraColor
                    for (i in 0 until 2) {
                        val subA = angle + i * PI.toFloat()
                        val p = Offset(center.x + cos(subA) * orbitDist, center.y + sin(subA) * orbitDist)
                        drawCircle(glow.copy(alpha = 0.35f), radius = u * 1.25f, center = p)
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
                        hiltColor = character.secondaryColor
                    )
                }

                WeaponVisualType.TRIPLE_KATANA -> {
                    val hiltColors = arrayOf(Color(0xFFF8FAFC), Color(0xFFA855F7), Color(0xFFDC2626))
                    for (i in 0 until 3) {
                        val subAngle = angle + i * (2f * PI.toFloat() / 3f)
                        val p = Offset(center.x + cos(subAngle) * orbitDist, center.y + sin(subAngle) * orbitDist)
                        val subRot = Math.toDegrees(subAngle.toDouble()).toFloat() + 90f
                        drawSingleKatana(
                            pos = p,
                            rotDeg = subRot,
                            u = u * 0.88f,
                            bladeColor = if (isUltimateActive) character.auraColor else Color(0xFFCBD5E1),
                            edgeColor = Color.White,
                            hiltColor = hiltColors[i]
                        )
                    }
                }

                WeaponVisualType.ZANGETSU_BLADE -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 2.15f)
                        bladePath.lineTo(u * 0.64f, u * 0.55f)
                        bladePath.lineTo(-u * 0.26f, u * 0.78f)
                        bladePath.close()
                        drawPath(bladePath, if (isUltimateActive) Color(0xFF09090B) else Color(0xFF334155))
                        drawPath(bladePath, ink, style = Stroke(width = 2.2f, join = StrokeJoin.Round))
                        drawLine(
                            if (isUltimateActive) character.auraColor else Color(0xFFF8FAFC),
                            Offset(0f, -u * 2.0f),
                            Offset(u * 0.54f, u * 0.48f),
                            strokeWidth = 2.8f,
                            cap = StrokeCap.Round
                        )
                        drawLine(Color(0xFFDC2626), Offset(0f, u * 0.78f), Offset(0f, u * 1.50f), strokeWidth = u * 0.26f, cap = StrokeCap.Round)
                    }
                }

                WeaponVisualType.GRIMOIRE_GREATSWORD -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 2.20f)
                        bladePath.lineTo(u * 0.72f, u * 0.54f)
                        bladePath.lineTo(-u * 0.72f, u * 0.54f)
                        bladePath.close()
                        drawPath(bladePath, Color(0xFF1F2937))
                        drawPath(bladePath, if (isUltimateActive) Color(0xFFEF4444) else ink, style = Stroke(width = 2.5f, join = StrokeJoin.Round))
                        drawLine(character.auraColor, Offset(0f, -u * 1.55f), Offset(0f, u * 0.36f), strokeWidth = 2.8f, cap = StrokeCap.Round)
                        drawLine(Color(0xFFFACC15), Offset(-u * 0.78f, u * 0.54f), Offset(u * 0.78f, u * 0.54f), strokeWidth = u * 0.22f, cap = StrokeCap.Round)
                        drawLine(Color(0xFF52525B), Offset(0f, u * 0.54f), Offset(0f, u * 1.45f), strokeWidth = u * 0.30f, cap = StrokeCap.Round)
                    }
                }

                WeaponVisualType.LIMITLESS_ORBS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    if (isUltimateActive) {
                        drawCircle(Color(0xFFA855F7).copy(alpha = 0.42f), radius = u * 1.48f, center = weaponPos)
                        drawCircle(Color(0xFFA855F7), radius = u * 1.12f, center = weaponPos)
                        drawCircle(Color.White, radius = u * 0.50f, center = weaponPos)
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
                    for ((idx, pos) in arrayOf(weaponPos, opp).withIndex()) {
                        withTransform({
                            translate(pos.x, pos.y)
                            rotate(rotDeg + idx * 180f, pivot = Offset.Zero)
                        }) {
                            bladePath.reset()
                            bladePath.moveTo(0f, -u * 1.50f)
                            bladePath.lineTo(u * 0.54f, 0f)
                            bladePath.lineTo(0f, u * 0.38f)
                            bladePath.lineTo(-u * 0.54f, 0f)
                            bladePath.close()
                            drawPath(bladePath, if (isUltimateActive) Color(0xFF38BDF8) else Color(0xFFE2E8F0))
                            drawPath(bladePath, ink, style = Stroke(width = 2f))
                            drawCircle(character.auraColor, radius = u * 0.30f, center = Offset(0f, u * 0.76f), style = Stroke(width = 2.5f))
                        }
                    }
                }

                WeaponVisualType.SERIOUS_GLOVE -> {
                    drawCircle(Color(0xFFDC2626), radius = u * 1.02f, center = weaponPos)
                    drawCircle(ink, radius = u * 1.02f, center = weaponPos, style = Stroke(width = 2.6f))
                    drawCircle(Color.White.copy(alpha = 0.85f), radius = u * 0.30f, center = Offset(weaponPos.x - u * 0.25f, weaponPos.y - u * 0.25f))
                }

                WeaponVisualType.GOMU_FIST -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    val c = if (isUltimateActive) Color(0xFFF8FAFC) else Color(0xFF18181B)
                    for (pos in arrayOf(weaponPos, opp)) {
                        drawCircle(c, radius = u * 0.90f, center = pos)
                        drawCircle(character.auraColor, radius = u * 0.90f, center = pos, style = Stroke(width = 2.5f))
                        drawCircle(Color.White.copy(alpha = 0.45f), radius = u * 0.25f, center = Offset(pos.x - u * 0.22f, pos.y - u * 0.22f))
                    }
                }

                WeaponVisualType.CHAINSAW_BLADE -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        drawRoundRect(
                            color = Color(0xFFEA580C),
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
                        for (t in -3..2) {
                            drawCircle(Color(0xFFF8FAFC), radius = u * 0.19f, center = Offset(u * 0.44f, t * u * 0.38f))
                            drawCircle(Color(0xFFF8FAFC), radius = u * 0.19f, center = Offset(-u * 0.44f, t * u * 0.38f))
                        }
                    }
                }

                WeaponVisualType.CURSED_CLEAVE -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    for ((idx, pos) in arrayOf(weaponPos, opp).withIndex()) {
                        drawArc(
                            color = character.auraColor,
                            startAngle = rotDeg - 54f + idx * 180f,
                            sweepAngle = 108f,
                            useCenter = false,
                            topLeft = Offset(pos.x - u * 1.05f, pos.y - u * 1.05f),
                            size = Size(u * 2.1f, u * 2.1f),
                            style = Stroke(width = u * 0.32f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color.White,
                            startAngle = rotDeg - 36f + idx * 180f,
                            sweepAngle = 72f,
                            useCenter = false,
                            topLeft = Offset(pos.x - u * 1.05f, pos.y - u * 1.05f),
                            size = Size(u * 2.1f, u * 2.1f),
                            style = Stroke(width = u * 0.11f, cap = StrokeCap.Round)
                        )
                    }
                }

                WeaponVisualType.TITAN_BLADES -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    drawSingleKatana(weaponPos, rotDeg, u * 0.92f, Color(0xFFE2E8F0), Color.White, Color(0xFF57534E))
                    drawSingleKatana(opp, rotDeg + 180f, u * 0.92f, Color(0xFFE2E8F0), Color.White, Color(0xFF57534E))
                }

                WeaponVisualType.DRAGON_CLAW -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        drawCircle(character.auraColor, radius = u * 0.92f, center = Offset.Zero)
                        drawCircle(Color(0xFFFEF08A), radius = u * 0.48f, center = Offset.Zero)
                        for (c in -1..1) {
                            drawLine(Color.White, Offset(c * u * 0.34f, -u * 0.45f), Offset(c * u * 0.42f, -u * 1.38f), strokeWidth = u * 0.18f, cap = StrokeCap.Round)
                        }
                        drawCircle(ink, radius = u * 0.92f, center = Offset.Zero, style = Stroke(width = 2.2f))
                    }
                }

                WeaponVisualType.SHADOW_DAGGERS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    drawSingleKatana(weaponPos, rotDeg, u * 0.84f, character.auraColor, Color(0xFFE9D5FF), Color(0xFF0F172A))
                    drawSingleKatana(opp, rotDeg + 180f, u * 0.84f, character.auraColor, Color(0xFFE9D5FF), Color(0xFF0F172A))
                }

                WeaponVisualType.PSYCHIC_DEBRIS -> {
                    for (i in 0 until 3) {
                        val a = angle + i * (2f * PI.toFloat() / 3f)
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
                        bladePath.moveTo(0f, -u * 2.15f)
                        bladePath.lineTo(u * 0.54f, -u * 0.82f)
                        bladePath.lineTo(0f, -u * 1.05f)
                        bladePath.lineTo(-u * 0.54f, -u * 0.82f)
                        bladePath.close()
                        drawPath(bladePath, if (isUltimateActive) character.auraColor else Color(0xFFE4E4E7))
                        drawPath(bladePath, ink, style = Stroke(width = 2.1f, join = StrokeJoin.Round))
                    }
                }

                WeaponVisualType.STAR_FISTS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    for (pos in arrayOf(weaponPos, opp)) {
                        drawCircle(character.auraColor, radius = u * 0.86f, center = pos)
                        drawCircle(Color(0xFFFACC15), radius = u * 0.86f, center = pos, style = Stroke(width = 2.8f))
                        drawCircle(Color.White, radius = u * 0.26f, center = pos)
                    }
                }

                WeaponVisualType.ALLOY_YOYOS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    for (pos in arrayOf(weaponPos, opp)) {
                        drawLine(character.auraColor.copy(alpha = 0.75f), center, pos, strokeWidth = 1.8f)
                        drawCircle(Color(0xFFE2E8F0), radius = u * 0.78f, center = pos)
                        drawCircle(character.auraColor, radius = u * 0.42f, center = pos)
                        drawCircle(ink, radius = u * 0.78f, center = pos, style = Stroke(width = 2.1f))
                    }
                }

                WeaponVisualType.HELLSING_PISTOLS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    drawPistol(weaponPos, rotDeg, u, Color(0xFFE2E8F0), character.auraColor)
                    drawPistol(opp, rotDeg + 180f, u, Color(0xFF27272A), character.auraColor)
                }

                WeaponVisualType.MAGE_STAFF -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
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
                        drawCircle(character.auraColor, radius = u * 0.34f, center = Offset(0f, -u * 1.25f))
                        drawCircle(Color.White, radius = u * 0.14f, center = Offset(0f, -u * 1.25f))
                    }
                }

                // --- 10 NEW SIGNATURE ANIME WEAPONS ---
                WeaponVisualType.CRESCENT_SCYTHE -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        drawLine(Color(0xFF1E293B), Offset(0f, -u * 1.35f), Offset(0f, u * 1.65f), strokeWidth = u * 0.24f, cap = StrokeCap.Round)
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 1.35f)
                        bladePath.quadraticTo(u * 1.65f, -u * 1.45f, u * 1.85f, -u * 0.15f)
                        bladePath.quadraticTo(u * 1.05f, -u * 0.78f, 0f, -u * 0.88f)
                        bladePath.close()
                        drawPath(bladePath, if (isUltimateActive) character.auraColor else Color(0xFFE11D48))
                        drawPath(bladePath, Color.White, style = Stroke(width = 1.8f, join = StrokeJoin.Round))
                    }
                }

                WeaponVisualType.SPIRAL_DRILL -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 2.15f)
                        bladePath.lineTo(u * 0.78f, u * 0.65f)
                        bladePath.lineTo(-u * 0.78f, u * 0.65f)
                        bladePath.close()
                        drawPath(bladePath, if (isUltimateActive) Color(0xFF22D3EE) else Color(0xFFFACC15))
                        drawPath(bladePath, ink, style = Stroke(width = 2.3f, join = StrokeJoin.Round))
                        for (g in -1..1) {
                            val y = g * u * 0.52f - u * 0.35f
                            drawLine(Color.White, Offset(-u * 0.42f, y), Offset(u * 0.42f, y + u * 0.22f), strokeWidth = 2.4f, cap = StrokeCap.Round)
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
                        drawLine(Color(0xFFFEF08A), Offset(-u * 0.35f, 0f), Offset(u * 1.35f, 0f), strokeWidth = u * 0.18f, cap = StrokeCap.Round)
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
                            bladePath.moveTo(0f, -u * 1.15f)
                            bladePath.lineTo(u * 0.56f, u * 0.68f)
                            bladePath.lineTo(0f, u * 0.35f)
                            bladePath.lineTo(-u * 0.56f, u * 0.68f)
                            bladePath.close()
                            drawPath(bladePath, Color(0xFFF8FAFC))
                            drawPath(bladePath, ink, style = Stroke(width = 2f, join = StrokeJoin.Round))
                            drawCircle(character.auraColor, radius = u * 0.24f, center = Offset(0f, -u * 0.15f))
                        }
                    }
                }

                WeaponVisualType.ROYAL_GATES -> {
                    for (i in 0 until 3) {
                        val a = angle + i * (2f * PI.toFloat() / 3f)
                        val p = Offset(center.x + cos(a) * orbitDist, center.y + sin(a) * orbitDist)
                        val rDeg = Math.toDegrees(a.toDouble()).toFloat() + 90f
                        drawCircle(Color(0xFFFACC15).copy(alpha = 0.55f), radius = u * 0.72f, center = p)
                        drawCircle(Color(0xFFFACC15), radius = u * 0.72f, center = p, style = Stroke(width = 2.2f))
                        drawSingleKatana(p, rDeg, u * 0.76f, Color(0xFFFEF08A), Color.White, Color(0xFFB45309))
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
                            color = ink,
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
                    for (pos in arrayOf(weaponPos, opp)) {
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
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 2.25f)
                        bladePath.lineTo(u * 0.44f, u * 0.55f)
                        bladePath.lineTo(-u * 0.44f, u * 0.55f)
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
                        drawCircle(character.auraColor, radius = u * 0.34f, center = Offset(0f, -u * 1.45f))
                        drawCircle(Color.White, radius = u * 0.16f, center = Offset(0f, -u * 1.45f))
                    }
                }
            }
        }
    }

    private fun DrawScope.drawSingleKatana(
        pos: Offset,
        rotDeg: Float,
        u: Float,
        bladeColor: Color,
        edgeColor: Color,
        hiltColor: Color
    ) {
        withTransform({
            translate(pos.x, pos.y)
            rotate(rotDeg, pivot = Offset.Zero)
        }) {
            drawLine(bladeColor, Offset(0f, -u * 1.72f), Offset(0f, u * 0.72f), strokeWidth = u * 0.31f, cap = StrokeCap.Round)
            drawLine(edgeColor, Offset(u * 0.06f, -u * 1.60f), Offset(u * 0.06f, u * 0.62f), strokeWidth = u * 0.11f, cap = StrokeCap.Round)
            drawLine(Color(0xFFFACC15), Offset(-u * 0.50f, u * 0.74f), Offset(u * 0.50f, u * 0.74f), strokeWidth = u * 0.21f, cap = StrokeCap.Round)
            drawLine(hiltColor, Offset(0f, u * 0.74f), Offset(0f, u * 1.48f), strokeWidth = u * 0.27f, cap = StrokeCap.Round)
        }
    }

    private fun DrawScope.drawPistol(pos: Offset, rotDeg: Float, u: Float, barrelColor: Color, glowColor: Color) {
        withTransform({
            translate(pos.x, pos.y)
            rotate(rotDeg, pivot = Offset.Zero)
        }) {
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
            drawCircle(glowColor, radius = u * 0.12f, center = Offset(0f, -u * 1.35f))
            drawRoundRect(
                color = Color(0xFF451A03),
                topLeft = Offset(-u * 0.24f, u * 0.18f),
                size = Size(u * 0.66f, u * 0.52f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}
