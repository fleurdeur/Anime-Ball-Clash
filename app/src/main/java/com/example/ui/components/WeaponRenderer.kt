package com.example.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
            val orbitDist = radius * 1.26f
            val weaponPos = Offset(
                center.x + cos(angle) * orbitDist,
                center.y + sin(angle) * orbitDist
            )

            if (spinBurst > 2f) {
                val deg = Math.toDegrees(angle.toDouble()).toFloat()
                drawArc(
                    color = character.auraColor.copy(alpha = 0.55f),
                    startAngle = deg - 88f,
                    sweepAngle = 88f,
                    useCenter = false,
                    topLeft = Offset(center.x - orbitDist, center.y - orbitDist),
                    size = Size(orbitDist * 2f, orbitDist * 2f),
                    style = Stroke(width = radius * 0.11f, cap = StrokeCap.Round)
                )
            }

            val u = radius * 0.28f
            val rotDeg = Math.toDegrees(angle.toDouble()).toFloat() + 90f

            when (character.weaponType) {
                WeaponVisualType.KI_HALO -> {
                    val glow = if (isUltimateActive) Color(0xFF38BDF8) else Color(0xFFFACC15)
                    drawCircle(glow, radius = u * 0.90f, center = weaponPos)
                    drawCircle(Color.White, radius = u * 0.42f, center = weaponPos)
                    drawCircle(ink, radius = u * 0.90f, center = weaponPos, style = Stroke(width = 2.2f))
                }

                WeaponVisualType.NICHIRIN_KATANA -> {
                    drawSingleKatana(
                        pos = weaponPos,
                        rotDeg = rotDeg,
                        u = u,
                        bladeColor = if (isUltimateActive) Color(0xFFF97316) else Color(0xFF1E293B),
                        edgeColor = if (isUltimateActive) Color(0xFFFEF08A) else Color(0xFFCBD5E1),
                        hiltColor = Color(0xFFDC2626)
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
                            u = u * 0.86f,
                            bladeColor = if (isUltimateActive) Color(0xFF22C55E) else Color(0xFFCBD5E1),
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
                        bladePath.moveTo(0f, -u * 2.0f)
                        bladePath.lineTo(u * 0.60f, u * 0.55f)
                        bladePath.lineTo(-u * 0.24f, u * 0.75f)
                        bladePath.close()
                        drawPath(bladePath, if (isUltimateActive) Color(0xFF18181B) else Color(0xFF334155))
                        drawLine(Color(0xFFE2E8F0), Offset(0f, -u * 1.9f), Offset(u * 0.52f, u * 0.48f), strokeWidth = 2.4f)
                        drawLine(Color(0xFFDC2626), Offset(0f, u * 0.75f), Offset(0f, u * 1.45f), strokeWidth = u * 0.25f, cap = StrokeCap.Round)
                    }
                }

                WeaponVisualType.GRIMOIRE_GREATSWORD -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 2.1f)
                        bladePath.lineTo(u * 0.68f, u * 0.52f)
                        bladePath.lineTo(-u * 0.68f, u * 0.52f)
                        bladePath.close()
                        drawPath(bladePath, Color(0xFF27272A))
                        drawPath(bladePath, if (isUltimateActive) Color(0xFFEF4444) else ink, style = Stroke(width = 2.4f))
                        drawLine(Color(0xFFDC2626), Offset(0f, -u * 1.4f), Offset(0f, u * 0.32f), strokeWidth = 2.4f)
                        drawLine(Color(0xFF52525B), Offset(0f, u * 0.52f), Offset(0f, u * 1.40f), strokeWidth = u * 0.30f, cap = StrokeCap.Round)
                    }
                }

                WeaponVisualType.LIMITLESS_ORBS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    if (isUltimateActive) {
                        drawCircle(Color(0xFFA855F7), radius = u * 1.12f, center = weaponPos)
                        drawCircle(Color.White, radius = u * 0.48f, center = weaponPos)
                    } else {
                        drawCircle(Color(0xFFEF4444), radius = u * 0.80f, center = weaponPos)
                        drawCircle(Color.White, radius = u * 0.30f, center = weaponPos)
                        drawCircle(Color(0xFF3B82F6), radius = u * 0.80f, center = opp)
                        drawCircle(Color.White, radius = u * 0.30f, center = opp)
                    }
                }

                WeaponVisualType.LIGHTNING_KUNAI -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 1.45f)
                        bladePath.lineTo(u * 0.52f, 0f)
                        bladePath.lineTo(0f, u * 0.36f)
                        bladePath.lineTo(-u * 0.52f, 0f)
                        bladePath.close()
                        drawPath(bladePath, Color(0xFFE2E8F0))
                        drawPath(bladePath, ink, style = Stroke(width = 2f))
                        drawCircle(Color(0xFF38BDF8), radius = u * 0.30f, center = Offset(0f, u * 0.74f), style = Stroke(width = 2.4f))
                    }
                }

                WeaponVisualType.SERIOUS_GLOVE -> {
                    drawCircle(Color(0xFFDC2626), radius = u * 0.96f, center = weaponPos)
                    drawCircle(ink, radius = u * 0.96f, center = weaponPos, style = Stroke(width = 2.4f))
                    drawCircle(Color.White.copy(alpha = 0.8f), radius = u * 0.28f, center = Offset(weaponPos.x - u * 0.24f, weaponPos.y - u * 0.24f))
                }

                WeaponVisualType.GOMU_FIST -> {
                    val c = if (isUltimateActive) Color(0xFFF8FAFC) else Color(0xFF18181B)
                    drawCircle(c, radius = u * 0.94f, center = weaponPos)
                    drawCircle(Color(0xFFEF4444), radius = u * 0.94f, center = weaponPos, style = Stroke(width = 2.4f))
                }

                WeaponVisualType.CHAINSAW_BLADE -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        drawRoundRect(
                            color = Color(0xFFEA580C),
                            topLeft = Offset(-u * 0.36f, -u * 1.4f),
                            size = Size(u * 0.72f, u * 2.35f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        for (t in -3..2) {
                            drawCircle(Color(0xFFF8FAFC), radius = u * 0.18f, center = Offset(u * 0.42f, t * u * 0.38f))
                            drawCircle(Color(0xFFF8FAFC), radius = u * 0.18f, center = Offset(-u * 0.42f, t * u * 0.38f))
                        }
                    }
                }

                WeaponVisualType.CURSED_CLEAVE -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    drawArc(
                        color = Color(0xFFE11D48),
                        startAngle = rotDeg - 52f,
                        sweepAngle = 104f,
                        useCenter = false,
                        topLeft = Offset(weaponPos.x - u, weaponPos.y - u),
                        size = Size(u * 2f, u * 2f),
                        style = Stroke(width = u * 0.30f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFFE11D48),
                        startAngle = rotDeg + 128f,
                        sweepAngle = 104f,
                        useCenter = false,
                        topLeft = Offset(opp.x - u, opp.y - u),
                        size = Size(u * 2f, u * 2f),
                        style = Stroke(width = u * 0.30f, cap = StrokeCap.Round)
                    )
                }

                WeaponVisualType.TITAN_BLADES -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    drawSingleKatana(weaponPos, rotDeg, u * 0.88f, Color(0xFFE2E8F0), Color.White, Color(0xFF57534E))
                    drawSingleKatana(opp, rotDeg + 180f, u * 0.88f, Color(0xFFE2E8F0), Color.White, Color(0xFF57534E))
                }

                WeaponVisualType.DRAGON_CLAW -> {
                    drawCircle(Color(0xFFF97316), radius = u * 0.90f, center = weaponPos)
                    drawCircle(Color(0xFFFEF08A), radius = u * 0.45f, center = weaponPos)
                    drawCircle(ink, radius = u * 0.90f, center = weaponPos, style = Stroke(width = 2f))
                }

                WeaponVisualType.SHADOW_DAGGERS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    drawSingleKatana(weaponPos, rotDeg, u * 0.80f, Color(0xFFA855F7), Color(0xFFE9D5FF), Color(0xFF0F172A))
                    drawSingleKatana(opp, rotDeg + 180f, u * 0.80f, Color(0xFFA855F7), Color(0xFFE9D5FF), Color(0xFF0F172A))
                }

                WeaponVisualType.PSYCHIC_DEBRIS -> {
                    for (i in 0 until 3) {
                        val a = angle + i * (2f * PI.toFloat() / 3f)
                        val p = Offset(center.x + cos(a) * orbitDist, center.y + sin(a) * orbitDist)
                        drawRect(
                            color = Color(0xFFA855F7),
                            topLeft = Offset(p.x - u * 0.42f, p.y - u * 0.42f),
                            size = Size(u * 0.84f, u * 0.84f)
                        )
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(p.x - u * 0.42f, p.y - u * 0.42f),
                            size = Size(u * 0.84f, u * 0.84f),
                            style = Stroke(width = 1.8f)
                        )
                    }
                }

                WeaponVisualType.ALCHEMY_SPEAR -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        drawLine(Color(0xFF71717A), Offset(0f, -u * 1.1f), Offset(0f, u * 1.5f), strokeWidth = u * 0.24f, cap = StrokeCap.Round)
                        bladePath.reset()
                        bladePath.moveTo(0f, -u * 2.0f)
                        bladePath.lineTo(u * 0.48f, -u * 0.85f)
                        bladePath.lineTo(-u * 0.48f, -u * 0.85f)
                        bladePath.close()
                        drawPath(bladePath, Color(0xFFE4E4E7))
                        drawPath(bladePath, ink, style = Stroke(width = 2f))
                    }
                }

                WeaponVisualType.STAR_FISTS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    drawCircle(Color(0xFFA855F7), radius = u * 0.82f, center = weaponPos)
                    drawCircle(Color(0xFFFACC15), radius = u * 0.82f, center = weaponPos, style = Stroke(width = 2.6f))
                    drawCircle(Color(0xFFA855F7), radius = u * 0.82f, center = opp)
                    drawCircle(Color(0xFFFACC15), radius = u * 0.82f, center = opp, style = Stroke(width = 2.6f))
                }

                WeaponVisualType.ALLOY_YOYOS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    for (pos in arrayOf(weaponPos, opp)) {
                        drawLine(Color(0xFF93C5FD), center, pos, strokeWidth = 1.5f)
                        drawCircle(Color(0xFFE2E8F0), radius = u * 0.76f, center = pos)
                        drawCircle(Color(0xFF2563EB), radius = u * 0.40f, center = pos)
                        drawCircle(ink, radius = u * 0.76f, center = pos, style = Stroke(width = 2f))
                    }
                }

                WeaponVisualType.HELLSING_PISTOLS -> {
                    val opp = Offset(center.x + cos(angle + PI.toFloat()) * orbitDist, center.y + sin(angle + PI.toFloat()) * orbitDist)
                    drawPistol(weaponPos, rotDeg, u, Color(0xFFE2E8F0))
                    drawPistol(opp, rotDeg + 180f, u, Color(0xFF27272A))
                }

                WeaponVisualType.MAGE_STAFF -> {
                    withTransform({
                        translate(weaponPos.x, weaponPos.y)
                        rotate(rotDeg, pivot = Offset.Zero)
                    }) {
                        drawLine(Color(0xFF92400E), Offset(0f, -u * 1.05f), Offset(0f, u * 1.5f), strokeWidth = u * 0.24f, cap = StrokeCap.Round)
                        drawArc(
                            color = Color(0xFFFACC15),
                            startAngle = 35f,
                            sweepAngle = 290f,
                            useCenter = false,
                            topLeft = Offset(-u * 0.62f, -u * 1.85f),
                            size = Size(u * 1.24f, u * 1.24f),
                            style = Stroke(width = u * 0.25f, cap = StrokeCap.Round)
                        )
                        drawCircle(Color(0xFFEF4444), radius = u * 0.28f, center = Offset(0f, -u * 1.22f))
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
            drawLine(bladeColor, Offset(0f, -u * 1.68f), Offset(0f, u * 0.72f), strokeWidth = u * 0.30f, cap = StrokeCap.Round)
            drawLine(edgeColor, Offset(u * 0.06f, -u * 1.58f), Offset(u * 0.06f, u * 0.62f), strokeWidth = u * 0.10f, cap = StrokeCap.Round)
            drawLine(Color(0xFFFACC15), Offset(-u * 0.48f, u * 0.74f), Offset(u * 0.48f, u * 0.74f), strokeWidth = u * 0.20f, cap = StrokeCap.Round)
            drawLine(hiltColor, Offset(0f, u * 0.74f), Offset(0f, u * 1.45f), strokeWidth = u * 0.26f, cap = StrokeCap.Round)
        }
    }

    private fun DrawScope.drawPistol(pos: Offset, rotDeg: Float, u: Float, barrelColor: Color) {
        withTransform({
            translate(pos.x, pos.y)
            rotate(rotDeg, pivot = Offset.Zero)
        }) {
            drawRoundRect(
                color = barrelColor,
                topLeft = Offset(-u * 0.24f, -u * 1.30f),
                size = Size(u * 0.48f, u * 1.65f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFF451A03),
                topLeft = Offset(-u * 0.24f, u * 0.18f),
                size = Size(u * 0.65f, u * 0.52f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}
