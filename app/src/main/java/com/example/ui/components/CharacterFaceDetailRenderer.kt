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
import com.example.model.CharacterData

/**
 * Character-specific facial markings, visors, masks, scars, and eyes for all 48 fighters.
 * Every helper draws strictly in local coordinates centered at Offset.Zero inside
 * AnimeBallRenderer's unified withTransform so zero details ever lag or detach!
 */
object CharacterFaceDetailRenderer {

    private val sharedPath = Path()
    private val inkColor = Color(0xFF12151E)

    fun drawFaceDetails(
        scope: DrawScope,
        char: CharacterData,
        r: Float,
        isUlt: Boolean,
        isSerious: Boolean
    ) = with(scope) {
        when (char.id) {
            "kakarot_z" -> {
                drawLine(Color(0xFF1D4ED8), Offset(-r * 0.54f, r * 0.44f), Offset(r * 0.54f, r * 0.44f), strokeWidth = r * 0.19f, cap = StrokeCap.Round)
                drawScowlEyes(r, if (isUlt) Color(0xFF38BDF8) else inkColor)
            }

            "kamado_sun" -> {
                val sq = r * 0.29f
                for (col in -2..1) {
                    if (col % 2 == 0) drawRect(inkColor, Offset(col * sq, r * 0.32f), Size(sq, sq * 0.68f))
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
                drawRoundRect(Color(0xFF09090B), Offset(-r * 0.93f, -r * 0.28f), Size(r * 1.86f, r * 0.50f), CornerRadius(r * 0.14f, r * 0.14f))
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
                drawArc(Color(0xFFF8FAFC), 90f, 180f, true, Offset(-r * 0.95f, -r * 0.95f), Size(r * 1.90f, r * 1.90f))
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

            // --- 28 NEW PROTAGONISTS & ANTAGONISTS ---
            "midoriya_ofa" -> {
                drawScowlEyes(r, Color(0xFF10B981))
                // Iconic cheek freckles + silver respirator collar
                for (side in intArrayOf(-1, 1)) {
                    drawCircle(inkColor, r * 0.035f, Offset(side * r * 0.50f, r * 0.14f))
                    drawCircle(inkColor, r * 0.035f, Offset(side * r * 0.58f, r * 0.21f))
                }
                drawRoundRect(Color(0xFFCBD5E1), Offset(-r * 0.56f, r * 0.36f), Size(r * 1.12f, r * 0.24f), CornerRadius(6f, 6f))
            }

            "shigaraki_decay" -> {
                drawScowlEyes(r, Color(0xFFEF4444))
                // Pale Father-hand clasp gripping across nose/bridge
                drawRoundRect(Color(0xFFCBD5E1), Offset(-r * 0.24f, -r * 0.26f), Size(r * 0.48f, r * 0.42f), CornerRadius(6f, 6f))
                for (f in -1..1) {
                    drawLine(Color(0xFF94A3B8), Offset(f * r * 0.14f, -r * 0.26f), Offset(f * r * 0.18f, -r * 0.55f), strokeWidth = r * 0.09f, cap = StrokeCap.Round)
                }
            }

            "frieza_emperor" -> {
                // Glossy purple dome Bio-Gem + vertical cheek lines + crimson tyrant eyes
                drawOval(Color(0xFF7E22CE), Offset(-r * 0.46f, -r * 0.86f), Size(r * 0.92f, r * 0.52f))
                drawOval(Color(0xFFC084FC).copy(alpha = 0.5f), Offset(-r * 0.24f, -r * 0.78f), Size(r * 0.30f, r * 0.20f))
                drawLine(Color(0xFF581C87), Offset(-r * 0.32f, r * 0.08f), Offset(-r * 0.32f, r * 0.42f), strokeWidth = 2.4f)
                drawLine(Color(0xFF581C87), Offset(r * 0.32f, r * 0.08f), Offset(r * 0.32f, r * 0.42f), strokeWidth = 2.4f)
                drawScowlEyes(r, Color(0xFFDC2626))
            }

            "muzan_progenitor" -> {
                // glowing plum demon cat-eyes + crimson tie
                drawScowlEyes(r, Color(0xFFE11D48))
                drawLine(inkColor, Offset(-r * 0.32f, -r * 0.11f), Offset(-r * 0.32f, r * 0.03f), strokeWidth = 2.2f)
                drawLine(inkColor, Offset(r * 0.32f, -r * 0.11f), Offset(r * 0.32f, r * 0.03f), strokeWidth = 2.2f)
                drawLine(Color(0xFFE11D48), Offset(0f, r * 0.34f), Offset(0f, r * 0.68f), strokeWidth = r * 0.14f, cap = StrokeCap.Round)
            }

            "makima_control" -> {
                // Hypnotic golden concentric ringed Control Devil eyes!
                for (sx in floatArrayOf(-r * 0.32f, r * 0.32f)) {
                    drawCircle(Color(0xFFFEF08A), r * 0.18f, Offset(sx, -r * 0.02f))
                    drawCircle(Color(0xFFDC2626), r * 0.14f, Offset(sx, -r * 0.02f), style = Stroke(width = 2.2f))
                    drawCircle(Color(0xFFDC2626), r * 0.07f, Offset(sx, -r * 0.02f), style = Stroke(width = 1.8f))
                }
                drawLine(Color.White, Offset(-r * 0.25f, r * 0.38f), Offset(0f, r * 0.56f), strokeWidth = 3f)
                drawLine(Color.White, Offset(r * 0.25f, r * 0.38f), Offset(0f, r * 0.56f), strokeWidth = 3f)
            }

            "dio_world" -> {
                // Emerald heart headband + crimson vampire eyes
                drawRoundRect(Color(0xFF15803D), Offset(-r * 0.78f, -r * 0.54f), Size(r * 1.56f, r * 0.22f), CornerRadius(4f, 4f))
                drawCircle(Color(0xFF22C55E), r * 0.11f, Offset(0f, -r * 0.43f))
                drawScowlEyes(r, Color(0xFFDC2626))
            }

            "aizen_kyoka" -> {
                drawScowlEyes(r, Color(0xFF7C3AED))
                // Glowing violet Hogyoku orb embedded in center collar + single hair bang
                drawLine(Color(0xFF451A03), Offset(r * 0.05f, -r * 0.75f), Offset(-r * 0.06f, r * 0.08f), strokeWidth = 3.2f, cap = StrokeCap.Round)
                drawCircle(Color(0xFFA855F7), r * 0.13f, Offset(0f, r * 0.46f))
                drawCircle(Color.White, r * 0.05f, Offset(0f, r * 0.46f))
            }

            "blackbeard_tremor" -> {
                drawScowlEyes(r, inkColor)
                // Scruffy black pirate beard + iconic missing-tooth grin
                drawArc(Color(0xFF09090B), 10f, 160f, true, Offset(-r * 0.85f, -r * 0.20f), Size(r * 1.70f, r * 1.15f))
                drawRoundRect(Color.White, Offset(-r * 0.36f, r * 0.24f), Size(r * 0.72f, r * 0.20f), CornerRadius(4f, 4f))
                drawRect(inkColor, Offset(r * 0.04f, r * 0.24f), Size(r * 0.13f, r * 0.12f))
            }

            "madara_uchiha" -> {
                // Glowing violet Rinnegan concentric ripple eyes
                for (sx in floatArrayOf(-r * 0.32f, r * 0.32f)) {
                    drawCircle(Color(0xFFC4B5FD), r * 0.17f, Offset(sx, -r * 0.03f))
                    drawCircle(Color(0xFF4C1D95), r * 0.13f, Offset(sx, -r * 0.03f), style = Stroke(width = 1.8f))
                    drawCircle(inkColor, r * 0.05f, Offset(sx, -r * 0.03f))
                }
            }

            "rimuru_tempest" -> {
                // Translucent cute golden slime eyes + rosy slime blush marks
                drawOval(Color(0xFFFACC15), Offset(-r * 0.42f, -r * 0.10f), Size(r * 0.22f, r * 0.26f))
                drawOval(Color(0xFFFACC15), Offset(r * 0.20f, -r * 0.10f), Size(r * 0.22f, r * 0.26f))
                drawLine(Color(0xFFF472B6), Offset(-r * 0.56f, r * 0.18f), Offset(-r * 0.44f, r * 0.18f), strokeWidth = 2.6f, cap = StrokeCap.Round)
                drawLine(Color(0xFFF472B6), Offset(r * 0.44f, r * 0.18f), Offset(r * 0.56f, r * 0.18f), strokeWidth = 2.6f, cap = StrokeCap.Round)
            }

            "okarun_turbo" -> {
                // Round glowing spectacles + black Turbo Granny jaw-teeth mask!
                for (sx in floatArrayOf(-r * 0.33f, r * 0.33f)) {
                    drawCircle(Color.White, r * 0.20f, Offset(sx, -r * 0.08f))
                    drawCircle(Color(0xFFEF4444), r * 0.20f, Offset(sx, -r * 0.08f), style = Stroke(width = 2.4f))
                    drawCircle(inkColor, r * 0.06f, Offset(sx, -r * 0.08f))
                }
                drawRoundRect(Color(0xFF09090B), Offset(-r * 0.68f, r * 0.18f), Size(r * 1.36f, r * 0.42f), CornerRadius(8f, 8f))
                for (t in -2..2) {
                    drawRect(Color.White, Offset(t * r * 0.20f - r * 0.06f, r * 0.24f), Size(r * 0.12f, r * 0.28f))
                }
            }

            "kafka_kaiju" -> {
                // Glowing cyan Kaiju No. 8 eyes + skeletal cyan monster jaw fissures
                drawScowlEyes(r, Color(0xFF22D3EE))
                drawRoundRect(Color(0xFF22D3EE), Offset(-r * 0.48f, r * 0.24f), Size(r * 0.96f, r * 0.24f), CornerRadius(6f, 6f))
                for (t in -2..2) {
                    drawLine(inkColor, Offset(t * r * 0.18f, r * 0.22f), Offset(t * r * 0.18f, r * 0.50f), strokeWidth = 2.4f)
                }
            }

            "meruem_chimera" -> {
                // Royal Chimera green carapace dome ridge + crimson eyes
                drawArc(Color(0xFF166534), 190f, 160f, true, Offset(-r * 0.90f, -r * 0.95f), Size(r * 1.80f, r * 0.85f))
                drawScowlEyes(r, Color(0xFFE11D48))
            }

            "guts_berserk" -> {
                // Closed scarred right eye + blazing crimson left eye + white hair streak
                val lx = -r * 0.32f
                val rx = r * 0.32f
                val ey = -r * 0.04f
                drawOval(Color.White, Offset(lx - r * 0.18f, ey - r * 0.12f), Size(r * 0.36f, r * 0.24f))
                drawCircle(if (isUlt) Color(0xFFEF4444) else inkColor, r * 0.09f, Offset(lx, ey))
                drawLine(inkColor, Offset(rx - r * 0.18f, ey), Offset(rx + r * 0.18f, ey), strokeWidth = 3f, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(-r * 0.15f, -r * 0.88f), Offset(-r * 0.05f, -r * 0.45f), strokeWidth = r * 0.12f, cap = StrokeCap.Round)
            }

            "griffith_femto" -> {
                // Sharp falcon helm beak visor + glowing hawk eyes
                sharedPath.reset()
                sharedPath.moveTo(-r * 0.75f, -r * 0.40f)
                sharedPath.lineTo(0f, r * 0.18f)
                sharedPath.lineTo(r * 0.75f, -r * 0.40f)
                sharedPath.close()
                drawPath(sharedPath, Color(0xFF1E1B4B))
                drawScowlEyes(r, Color(0xFFEF4444))
            }

            "ryuko_scissor" -> {
                // Iconic crimson bang streak + golden Senketsu collar eyes
                drawLine(Color(0xFFEF4444), Offset(-r * 0.36f, -r * 0.86f), Offset(-r * 0.52f, -r * 0.18f), strokeWidth = r * 0.15f, cap = StrokeCap.Round)
                drawScowlEyes(r, Color(0xFF3B82F6))
                drawCircle(Color(0xFFFACC15), r * 0.09f, Offset(-r * 0.38f, r * 0.48f))
            }

            "gabimaru_hollow" -> {
                // Dark shinobi mouth-scarf + golden hollow eyes
                drawScowlEyes(r, Color(0xFFFACC15))
                drawRoundRect(Color(0xFF0F172A), Offset(-r * 0.76f, r * 0.18f), Size(r * 1.52f, r * 0.46f), CornerRadius(10f, 10f))
            }

            "ainz_overlord" -> {
                // Dark skeletal eye sockets with blazing crimson pinprick soul-flames!
                for (sx in floatArrayOf(-r * 0.32f, r * 0.32f)) {
                    drawCircle(Color(0xFF09090B), r * 0.21f, Offset(sx, -r * 0.04f))
                    drawCircle(Color(0xFFEF4444), r * 0.08f, Offset(sx, -r * 0.04f))
                }
                for (t in -2..2) {
                    drawLine(Color(0xFF475569), Offset(t * r * 0.14f, r * 0.28f), Offset(t * r * 0.14f, r * 0.48f), strokeWidth = 2.2f)
                }
            }

            "gilgamesh_babylon" -> {
                drawScowlEyes(r, Color(0xFFDC2626))
                // Crimson tattoos + golden collar
                drawLine(Color(0xFFDC2626), Offset(-r * 0.62f, r * 0.22f), Offset(-r * 0.32f, r * 0.38f), strokeWidth = 2.4f)
                drawLine(Color(0xFFDC2626), Offset(r * 0.62f, r * 0.22f), Offset(r * 0.32f, r * 0.38f), strokeWidth = 2.4f)
            }

            "simon_spiral" -> {
                // Blazing crimson star-visor Gurren shades!
                sharedPath.reset()
                sharedPath.moveTo(-r * 0.86f, -r * 0.24f)
                sharedPath.lineTo(0f, -r * 0.04f)
                sharedPath.lineTo(r * 0.86f, -r * 0.24f)
                sharedPath.lineTo(r * 0.52f, r * 0.14f)
                sharedPath.lineTo(0f, 0f)
                sharedPath.lineTo(-r * 0.52f, r * 0.14f)
                sharedPath.close()
                drawPath(sharedPath, Color(0xFFEF4444))
                drawPath(sharedPath, Color.White, style = Stroke(width = 2f, join = StrokeJoin.Round))
            }

            "vash_stampede" -> {
                // Iconic round amber-orange tinted sunglasses!
                drawLine(inkColor, Offset(-r * 0.55f, -r * 0.04f), Offset(r * 0.55f, -r * 0.04f), strokeWidth = 2.4f)
                for (sx in floatArrayOf(-r * 0.30f, r * 0.30f)) {
                    drawCircle(Color(0xFFF59E0B), r * 0.20f, Offset(sx, -r * 0.04f))
                    drawCircle(inkColor, r * 0.20f, Offset(sx, -r * 0.04f), style = Stroke(width = 2.2f))
                }
            }

            "bondrewd_dawn" -> {
                // Unmistakable Made in Abyss vertical glowing magenta visor slit!
                drawRoundRect(Color(0xFF09090B), Offset(-r * 0.82f, -r * 0.82f), Size(r * 1.64f, r * 1.64f), CornerRadius(r, r))
                drawLine(Color(0xFFD946EF), Offset(0f, -r * 0.84f), Offset(0f, r * 0.84f), strokeWidth = r * 0.18f, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(0f, -r * 0.74f), Offset(0f, r * 0.74f), strokeWidth = r * 0.06f, cap = StrokeCap.Round)
            }

            "phosphophyllite_gem" -> {
                drawScowlEyes(r, Color(0xFF2DD4BF))
                // Kintsugi golden alloy veins across cheek
                drawLine(Color(0xFFFACC15), Offset(-r * 0.65f, r * 0.10f), Offset(-r * 0.20f, r * 0.45f), strokeWidth = 2.8f, cap = StrokeCap.Round)
                drawLine(Color(0xFFFACC15), Offset(r * 0.58f, -r * 0.25f), Offset(r * 0.28f, r * 0.35f), strokeWidth = 2.5f, cap = StrokeCap.Round)
            }

            "caiman_dorohedoro" -> {
                // Black cross-marks directly over Caiman's reptile eyes!
                for (sx in floatArrayOf(-r * 0.32f, r * 0.32f)) {
                    drawLine(inkColor, Offset(sx, -r * 0.32f), Offset(sx, r * 0.24f), strokeWidth = 3.2f)
                    drawLine(inkColor, Offset(sx - r * 0.22f, -r * 0.04f), Offset(sx + r * 0.22f, -r * 0.04f), strokeWidth = 3.2f)
                }
                drawScowlEyes(r, Color(0xFFDC2626))
            }

            "en_dorohedoro" -> {
                // Fungal crimson mushroom mask across lower face
                drawRoundRect(Color(0xFF991B1B), Offset(-r * 0.56f, r * 0.08f), Size(r * 1.12f, r * 0.48f), CornerRadius(10f, 10f))
                drawCircle(Color(0xFFFECDD3), r * 0.07f, Offset(-r * 0.26f, r * 0.28f))
                drawCircle(Color(0xFFFECDD3), r * 0.07f, Offset(r * 0.24f, r * 0.32f))
                drawScowlEyes(r, inkColor)
            }

            "hyakkimaru_dororo" -> {
                // Serene porcelain samurai gaze + crimson scarf
                drawScowlEyes(r, Color(0xFF78350F))
                drawLine(Color(0xFF991B1B), Offset(-r * 0.64f, r * 0.44f), Offset(r * 0.64f, r * 0.44f), strokeWidth = r * 0.16f, cap = StrokeCap.Round)
            }

            "tetsuo_psychic" -> {
                // Intense psychic forehead veins + crimson Akira cape collar
                drawScowlEyes(r, if (isUlt) Color(0xFFEF4444) else inkColor)
                drawLine(Color(0xFFDC2626), Offset(-r * 0.70f, r * 0.42f), Offset(r * 0.70f, r * 0.42f), strokeWidth = r * 0.22f, cap = StrokeCap.Round)
            }

            "accelerator_vector" -> {
                // Snowy jagged hair + piercing crimson esper eyes + black/white chevron collar
                drawScowlEyes(r, Color(0xFFEF4444))
                drawLine(Color.White, Offset(-r * 0.46f, r * 0.42f), Offset(0f, r * 0.28f), strokeWidth = 2.8f)
                drawLine(Color.White, Offset(0f, r * 0.28f), Offset(r * 0.46f, r * 0.42f), strokeWidth = 2.8f)
            }

            else -> {
                drawScowlEyes(r, inkColor)
            }
        }
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
}
