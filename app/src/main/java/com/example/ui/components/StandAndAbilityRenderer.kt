package com.example.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.CharacterData
import com.example.model.CombatBall
import com.example.model.CounterArchetype
import com.example.model.JoJoStandStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders JoJo Stand spirits (Star Platinum, The World, GER, King Crimson, Killer Queen,
 * Made in Heaven, Hermit Purple), anime-accurate character clothing/accessories, Ultimate form
 * manifestations (Susanoo, Kurama tails, Nika cloud scarf, Anti-Magic wing), and active Counter shields.
 */
object StandAndAbilityRenderer {

    private val standPath = Path()
    private val ink = Color(0xFF12151E)

    fun drawBehindSphereManifestations(
        scope: DrawScope,
        character: CharacterData,
        center: Offset,
        radius: Float,
        isUltimateActive: Boolean,
        isCounterActive: Boolean,
        globalTime: Float,
        allowDetailedFx: Boolean = true
    ) {
        if (!allowDetailedFx && !character.hasStandSpirit && !isUltimateActive) return
        with(scope) {
            if (character.hasStandSpirit) {
                drawJoJoStandSpirit(
                    style = character.joJoStandStyle,
                    center = center,
                    r = radius,
                    isPoweredUp = isUltimateActive || isCounterActive,
                    t = globalTime
                )
            }

            if (isUltimateActive) {
                drawSignatureUltimateAvatarForm(character, center, radius, globalTime)
            }
        }
    }

    private fun DrawScope.drawJoJoStandSpirit(
        style: JoJoStandStyle,
        center: Offset,
        r: Float,
        isPoweredUp: Boolean,
        t: Float
    ) {
        val bob = sin(t * 4.6f) * r * 0.07f
        val scale = if (isPoweredUp) 1.18f else 0.92f
        val spiritAlpha = if (isPoweredUp) 0.90f else 0.68f
        val sx = center.x - r * 0.56f * scale
        val sy = center.y - r * 0.72f * scale + bob

        when (style) {
            JoJoStandStyle.STAR_PLATINUM -> {
                val violet = Color(0xFF7E22CE).copy(alpha = spiritAlpha)
                val lavender = Color(0xFFC084FC).copy(alpha = spiritAlpha)
                val gold = Color(0xFFFACC15).copy(alpha = spiritAlpha)
                // Spirit aura glow
                drawCircle(lavender.copy(alpha = 0.28f), radius = r * 0.68f * scale, center = Offset(sx, sy + r * 0.16f))
                // Muscular shoulders & scarf
                drawRoundRect(violet, Offset(sx - r * 0.44f, sy + r * 0.14f), Size(r * 0.88f, r * 0.42f), CornerRadius(r * 0.18f))
                drawCircle(gold, radius = r * 0.16f, center = Offset(sx - r * 0.40f, sy + r * 0.22f))
                drawCircle(gold, radius = r * 0.16f, center = Offset(sx + r * 0.40f, sy + r * 0.22f))
                // Flowing dark Stand hair spikes
                standPath.reset()
                standPath.moveTo(sx - r * 0.26f, sy)
                standPath.lineTo(sx - r * 0.38f, sy - r * 0.42f)
                standPath.lineTo(sx - r * 0.08f, sy - r * 0.28f)
                standPath.lineTo(sx + r * 0.06f, sy - r * 0.48f)
                standPath.lineTo(sx + r * 0.28f, sy - r * 0.24f)
                standPath.close()
                drawPath(standPath, Color(0xFF1E1B4B).copy(alpha = spiritAlpha))
                // Stand head + golden headband + glowing eyes
                drawCircle(lavender, radius = r * 0.25f, center = Offset(sx, sy))
                drawRect(gold, Offset(sx - r * 0.23f, sy - r * 0.10f), Size(r * 0.46f, r * 0.08f))
                drawLine(Color.White, Offset(sx - r * 0.12f, sy), Offset(sx - r * 0.03f, sy + r * 0.02f), strokeWidth = 2.4f)
                drawLine(Color.White, Offset(sx + r * 0.04f, sy + r * 0.02f), Offset(sx + r * 0.14f, sy), strokeWidth = 2.4f)
                if (isPoweredUp) {
                    drawStandRushFists(center, r, lavender, gold, t)
                }
            }

            JoJoStandStyle.THE_WORLD -> {
                val gold = Color(0xFFFACC15).copy(alpha = spiritAlpha)
                val amber = Color(0xFFCA8A04).copy(alpha = spiritAlpha)
                val emerald = Color(0xFF22C55E).copy(alpha = spiritAlpha)
                drawCircle(gold.copy(alpha = 0.26f), radius = r * 0.70f * scale, center = Offset(sx, sy + r * 0.15f))
                // Twin golden diver cylinders on back
                drawRoundRect(amber, Offset(sx - r * 0.34f, sy + r * 0.05f), Size(r * 0.15f, r * 0.48f), CornerRadius(r * 0.06f))
                drawRoundRect(amber, Offset(sx + r * 0.19f, sy + r * 0.05f), Size(r * 0.15f, r * 0.48f), CornerRadius(r * 0.06f))
                // Armored golden shoulders
                drawRoundRect(gold, Offset(sx - r * 0.46f, sy + r * 0.16f), Size(r * 0.92f, r * 0.40f), CornerRadius(r * 0.16f))
                // Flat-topped Pharaoh helmet head
                standPath.reset()
                standPath.moveTo(sx - r * 0.28f, sy - r * 0.28f)
                standPath.lineTo(sx + r * 0.28f, sy - r * 0.28f)
                standPath.lineTo(sx + r * 0.22f, sy + r * 0.16f)
                standPath.lineTo(sx - r * 0.22f, sy + r * 0.16f)
                standPath.close()
                drawPath(standPath, gold)
                // Emerald heart crest + crimson vampire Stand eyes
                drawCircle(emerald, radius = r * 0.08f, center = Offset(sx, sy - r * 0.14f))
                drawCircle(Color(0xFFEF4444), radius = r * 0.045f, center = Offset(sx - r * 0.09f, sy))
                drawCircle(Color(0xFFEF4444), radius = r * 0.045f, center = Offset(sx + r * 0.09f, sy))
                if (isPoweredUp) {
                    drawStandRushFists(center, r, gold, emerald, t)
                }
            }

            JoJoStandStyle.GOLD_EXPERIENCE_REQUIEM -> {
                val gold = Color(0xFFFDE047).copy(alpha = spiritAlpha)
                val rose = Color(0xFFEC4899).copy(alpha = spiritAlpha)
                drawCircle(gold.copy(alpha = 0.28f), radius = r * 0.72f * scale, center = Offset(sx, sy + r * 0.12f))
                // Slender golden Requiem shoulders + ladybug orbs
                drawRoundRect(gold, Offset(sx - r * 0.40f, sy + r * 0.16f), Size(r * 0.80f, r * 0.38f), CornerRadius(r * 0.18f))
                drawCircle(rose, radius = r * 0.09f, center = Offset(sx, sy + r * 0.28f))
                // Crown-horned Requiem head
                drawCircle(gold, radius = r * 0.24f, center = Offset(sx, sy))
                drawLine(gold, Offset(sx - r * 0.16f, sy - r * 0.16f), Offset(sx - r * 0.26f, sy - r * 0.40f), strokeWidth = r * 0.09f, cap = StrokeCap.Round)
                drawLine(gold, Offset(sx + r * 0.16f, sy - r * 0.16f), Offset(sx + r * 0.26f, sy - r * 0.40f), strokeWidth = r * 0.09f, cap = StrokeCap.Round)
                // Big piercing Requiem eyes
                drawCircle(rose, radius = r * 0.055f, center = Offset(sx - r * 0.09f, sy))
                drawCircle(rose, radius = r * 0.055f, center = Offset(sx + r * 0.09f, sy))
                if (isPoweredUp) {
                    drawStandRushFists(center, r, gold, rose, t)
                }
            }

            JoJoStandStyle.KING_CRIMSON -> {
                val crimson = Color(0xFFE11D48).copy(alpha = spiritAlpha)
                val silver = Color(0xFFF1F5F9).copy(alpha = spiritAlpha)
                drawRoundRect(crimson, Offset(sx - r * 0.42f, sy + r * 0.15f), Size(r * 0.84f, r * 0.40f), CornerRadius(r * 0.15f))
                // White grid stripes
                drawLine(silver, Offset(sx - r * 0.30f, sy + r * 0.18f), Offset(sx + r * 0.30f, sy + r * 0.50f), strokeWidth = 2.0f)
                drawLine(silver, Offset(sx + r * 0.30f, sy + r * 0.18f), Offset(sx - r * 0.30f, sy + r * 0.50f), strokeWidth = 2.0f)
                // Head + Epitaph miniature sub-face on brow!
                drawCircle(crimson, radius = r * 0.25f, center = Offset(sx, sy))
                drawCircle(Color(0xFFF472B6), radius = r * 0.08f, center = Offset(sx, sy - r * 0.16f))
                drawCircle(Color(0xFF4ADE80), radius = r * 0.045f, center = Offset(sx - r * 0.09f, sy + r * 0.02f))
                drawCircle(Color(0xFF4ADE80), radius = r * 0.045f, center = Offset(sx + r * 0.09f, sy + r * 0.02f))
            }

            JoJoStandStyle.KILLER_QUEEN -> {
                val palePink = Color(0xFFF9A8D4).copy(alpha = spiritAlpha)
                val gold = Color(0xFFFACC15).copy(alpha = spiritAlpha)
                drawRoundRect(palePink, Offset(sx - r * 0.40f, sy + r * 0.16f), Size(r * 0.80f, r * 0.38f), CornerRadius(r * 0.16f))
                drawCircle(gold, radius = r * 0.09f, center = Offset(sx, sy + r * 0.32f))
                drawCircle(palePink, radius = r * 0.24f, center = Offset(sx, sy))
                // Feline triangular cat ears
                standPath.reset()
                standPath.moveTo(sx - r * 0.22f, sy - r * 0.10f)
                standPath.lineTo(sx - r * 0.26f, sy - r * 0.36f)
                standPath.lineTo(sx - r * 0.06f, sy - r * 0.20f)
                standPath.moveTo(sx + r * 0.22f, sy - r * 0.10f)
                standPath.lineTo(sx + r * 0.26f, sy - r * 0.36f)
                standPath.lineTo(sx + r * 0.06f, sy - r * 0.20f)
                standPath.close()
                drawPath(standPath, palePink)
                drawCircle(Color(0xFFEF4444), radius = r * 0.045f, center = Offset(sx - r * 0.09f, sy))
                drawCircle(Color(0xFFEF4444), radius = r * 0.045f, center = Offset(sx + r * 0.09f, sy))
            }

            JoJoStandStyle.MADE_IN_HEAVEN -> {
                val white = Color(0xFFF8FAFC).copy(alpha = spiritAlpha)
                val gold = Color(0xFFFACC15).copy(alpha = spiritAlpha)
                // Cosmic clockwork halo behind Stand
                drawCircle(gold, radius = r * 0.48f * scale, center = Offset(sx, sy), style = Stroke(width = 2.4f))
                drawCircle(white, radius = r * 0.23f, center = Offset(sx, sy))
                // Feathered crown crest
                for (i in -2..2) {
                    val a = -PI.toFloat() * 0.5f + i * 0.28f
                    drawLine(
                        gold,
                        Offset(sx + cos(a) * r * 0.20f, sy + sin(a) * r * 0.20f),
                        Offset(sx + cos(a) * r * 0.44f, sy + sin(a) * r * 0.44f),
                        strokeWidth = 2.6f,
                        cap = StrokeCap.Round
                    )
                }
            }

            JoJoStandStyle.HERMIT_PURPLE -> {
                val vineColor = Color(0xFFA855F7).copy(alpha = 0.85f)
                val hamonGold = Color(0xFFFDE047).copy(alpha = 0.85f)
                for (v in 0 until 3) {
                    val baseA = t * 2.6f + v * 2.09f
                    val p1 = Offset(center.x + cos(baseA) * r * 1.05f, center.y + sin(baseA) * r * 1.05f)
                    val p2 = Offset(center.x + cos(baseA + 0.7f) * r * 1.28f, center.y + sin(baseA + 0.7f) * r * 1.28f)
                    val p3 = Offset(center.x + cos(baseA + 1.3f) * r * 1.08f, center.y + sin(baseA + 1.3f) * r * 1.08f)
                    drawLine(vineColor, p1, p2, strokeWidth = 3.2f, cap = StrokeCap.Round)
                    drawLine(vineColor, p2, p3, strokeWidth = 2.8f, cap = StrokeCap.Round)
                    if (isPoweredUp) {
                        drawCircle(hamonGold, radius = r * 0.07f, center = p2)
                    }
                }
            }

            JoJoStandStyle.NONE -> Unit
        }

        // Menacing JoJo "ゴゴ" aura strokes floating near right shoulder
        val mx = center.x + r * 0.96f
        val my = center.y - r * 0.68f - bob * 0.8f
        val menaceCol = Color(0xFFC084FC).copy(alpha = if (isPoweredUp) 0.90f else 0.62f)
        drawLine(menaceCol, Offset(mx, my), Offset(mx + r * 0.22f, my), strokeWidth = 2.6f, cap = StrokeCap.Round)
        drawLine(menaceCol, Offset(mx + r * 0.22f, my), Offset(mx + r * 0.22f, my + r * 0.22f), strokeWidth = 2.6f, cap = StrokeCap.Round)
        drawLine(menaceCol, Offset(mx, my + r * 0.22f), Offset(mx + r * 0.22f, my + r * 0.22f), strokeWidth = 2.6f, cap = StrokeCap.Round)
        drawCircle(menaceCol, radius = 2.2f, center = Offset(mx + r * 0.28f, my - r * 0.04f))
        drawCircle(menaceCol, radius = 2.2f, center = Offset(mx + r * 0.34f, my + r * 0.01f))
    }

    private fun DrawScope.drawStandRushFists(
        center: Offset,
        r: Float,
        fistColor: Color,
        knuckleColor: Color,
        t: Float
    ) {
        for (i in 0 until 5) {
            val phase = (t * 14f + i * 1.25f) % (2f * PI.toFloat())
            val reach = r * (1.15f + (sin(phase * 2f) + 1f) * 0.28f)
            val angle = -0.55f + (i - 2) * 0.34f + sin(t * 9f + i) * 0.08f
            val fx = center.x + cos(angle) * reach
            val fy = center.y + sin(angle) * reach
            drawLine(
                fistColor.copy(alpha = 0.55f),
                Offset(center.x + cos(angle) * r * 0.85f, center.y + sin(angle) * r * 0.85f),
                Offset(fx, fy),
                strokeWidth = r * 0.14f,
                cap = StrokeCap.Round
            )
            drawCircle(fistColor, radius = r * 0.14f, center = Offset(fx, fy))
            drawCircle(knuckleColor, radius = r * 0.07f, center = Offset(fx, fy))
        }
    }

    private fun DrawScope.drawSignatureUltimateAvatarForm(
        char: CharacterData,
        center: Offset,
        r: Float,
        t: Float
    ) {
        when (char.id) {
            "madara_uchiha", "sasuke_rinnegan" -> {
                val susanoo = if (char.id == "madara_uchiha") Color(0xFF6366F1) else Color(0xFFA855F7)
                // Ethereal Susanoo ribcage & spectral aura arcs
                for (rib in -1..1) {
                    val ry = center.y + rib * r * 0.28f
                    drawArc(
                        color = susanoo.copy(alpha = 0.72f),
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = Offset(center.x - r * 1.34f, ry - r * 0.36f),
                        size = Size(r * 2.68f, r * 0.72f),
                        style = Stroke(width = 3.4f, cap = StrokeCap.Round)
                    )
                }
            }
            "naruto_baryon" -> {
                // Blazing Nine-Tails Kurama / Baryon chakra tails fanning behind
                for (tail in -2..2) {
                    val a = -PI.toFloat() * 0.5f + tail * 0.36f + sin(t * 7f + tail) * 0.08f
                    drawLine(
                        Color(0xFFF97316).copy(alpha = 0.78f),
                        Offset(center.x + cos(a) * r * 0.85f, center.y + sin(a) * r * 0.85f),
                        Offset(center.x + cos(a) * r * 1.52f, center.y + sin(a) * r * 1.52f),
                        strokeWidth = r * 0.16f,
                        cap = StrokeCap.Round
                    )
                }
            }
            "mugiwara_joy" -> {
                // Gear 5 white Sun God Nika cloud hagoromo ribbon floating overhead
                drawArc(
                    color = Color.White.copy(alpha = 0.90f),
                    startAngle = 195f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(center.x - r * 1.32f, center.y - r * 1.32f),
                    size = Size(r * 2.64f, r * 2.20f),
                    style = Stroke(width = r * 0.16f, cap = StrokeCap.Round)
                )
            }
            "asta_grimoire" -> {
                // Black-and-crimson Anti-Magic Devil wing & horn
                standPath.reset()
                standPath.moveTo(center.x + r * 0.55f, center.y - r * 0.10f)
                standPath.lineTo(center.x + r * 1.55f, center.y - r * 0.85f)
                standPath.lineTo(center.x + r * 1.25f, center.y + r * 0.25f)
                standPath.close()
                drawPath(standPath, Color(0xFF09090B).copy(alpha = 0.88f))
                drawPath(standPath, Color(0xFFEF4444).copy(alpha = 0.78f), style = Stroke(width = 2.2f))
            }
        }
    }

    /**
     * Draws anime-accurate clothing collars, badges, earrings, and accessories in local sphere space (Offset.Zero).
     */
    fun drawAuthenticOutfitOverlay(
        scope: DrawScope,
        char: CharacterData,
        r: Float,
        isUlt: Boolean
    ) {
        with(scope) {
            when (char.id) {
                "kujo_star" -> {
                    // Jotaro's heavy gold gakuran collar chain + golden cap badge
                    drawArc(
                        color = Color(0xFFFACC15),
                        startAngle = 25f,
                        sweepAngle = 65f,
                        useCenter = false,
                        topLeft = Offset(-r * 0.72f, r * 0.18f),
                        size = Size(r * 0.95f, r * 0.62f),
                        style = Stroke(width = r * 0.09f, cap = StrokeCap.Round)
                    )
                    drawCircle(Color(0xFFFACC15), radius = r * 0.09f, center = Offset(-r * 0.24f, -r * 0.62f))
                }
                "dio_world" -> {
                    // DIO's emerald-green heart brow circlet & golden collar studs
                    drawRect(Color(0xFFFACC15), Offset(-r * 0.52f, -r * 0.52f), Size(r * 1.04f, r * 0.11f))
                    drawCircle(Color(0xFF16A34A), radius = r * 0.11f, center = Offset(0f, -r * 0.47f))
                }
                "giorno_requiem" -> {
                    // Giorno's 3 golden brow curls + crimson ladybug brooches on pink suit
                    for (c in -1..1) {
                        drawCircle(Color(0xFFFACC15), radius = r * 0.12f, center = Offset(c * r * 0.24f, -r * 0.58f))
                        drawCircle(ink, radius = r * 0.045f, center = Offset(c * r * 0.24f, -r * 0.58f))
                    }
                    drawCircle(Color(0xFFEF4444), radius = r * 0.085f, center = Offset(-r * 0.28f, r * 0.56f))
                    drawCircle(Color(0xFFEF4444), radius = r * 0.085f, center = Offset(r * 0.28f, r * 0.56f))
                }
                "kamado_sun" -> {
                    // Tanjiro's green & black checkered haori + Hanafuda sun earrings
                    val sq = r * 0.18f
                    for (col in -2..1) {
                        val c = if (col % 2 == 0) Color(0xFF10B981) else Color(0xFF18181B)
                        drawRect(c, Offset(col * sq, r * 0.54f), Size(sq, sq * 0.85f))
                    }
                    // Hanafuda earrings
                    drawRect(Color.White, Offset(-r * 0.92f, r * 0.04f), Size(r * 0.14f, r * 0.26f))
                    drawCircle(Color(0xFFDC2626), radius = r * 0.045f, center = Offset(-r * 0.85f, r * 0.12f))
                    drawRect(Color.White, Offset(r * 0.78f, r * 0.04f), Size(r * 0.14f, r * 0.26f))
                    drawCircle(Color(0xFFDC2626), radius = r * 0.045f, center = Offset(r * 0.85f, r * 0.12f))
                }
                "kakarot_z" -> {
                    // Goku's navy undershirt V-neck + white kanji crest circle on orange gi
                    drawCircle(Color.White, radius = r * 0.12f, center = Offset(r * 0.34f, r * 0.56f))
                    drawCircle(ink, radius = r * 0.06f, center = Offset(r * 0.34f, r * 0.56f), style = Stroke(width = 1.6f))
                }
                "satoru_void" -> {
                    // Gojo's dark jujutsu blindfold band—glowing bright Six Eyes cyan in Ultimate!
                    if (isUlt) {
                        drawCircle(Color(0xFF38BDF8), radius = r * 0.13f, center = Offset(-r * 0.28f, -r * 0.06f))
                        drawCircle(Color(0xFF38BDF8), radius = r * 0.13f, center = Offset(r * 0.28f, -r * 0.06f))
                        drawCircle(Color.White, radius = r * 0.06f, center = Offset(-r * 0.28f, -r * 0.06f))
                        drawCircle(Color.White, radius = r * 0.06f, center = Offset(r * 0.28f, -r * 0.06f))
                    } else {
                        drawRoundRect(Color(0xFF0F172A), Offset(-r * 0.76f, -r * 0.22f), Size(r * 1.52f, r * 0.28f), CornerRadius(r * 0.08f))
                    }
                }
                "mugiwara_joy" -> {
                    // Luffy's iconic eye scar under left eye + yellow sash accent
                    drawLine(ink, Offset(-r * 0.38f, r * 0.14f), Offset(-r * 0.18f, r * 0.18f), strokeWidth = 2.0f)
                    drawRect(Color(0xFFFACC15), Offset(-r * 0.36f, r * 0.64f), Size(r * 0.72f, r * 0.12f))
                }
                "kurosaki_hollow" -> {
                    if (isUlt) {
                        // Ichigo's white & crimson-striped Hollow Mask half-plate
                        drawArc(
                            color = Color.White,
                            startAngle = 190f,
                            sweepAngle = 150f,
                            useCenter = true,
                            topLeft = Offset(-r * 0.78f, -r * 0.78f),
                            size = Size(r * 1.15f, r * 1.25f)
                        )
                        drawLine(Color(0xFFDC2626), Offset(-r * 0.46f, -r * 0.56f), Offset(-r * 0.32f, r * 0.18f), strokeWidth = 3.0f)
                        drawLine(Color(0xFFDC2626), Offset(-r * 0.26f, -r * 0.60f), Offset(-r * 0.14f, r * 0.14f), strokeWidth = 2.6f)
                    }
                }
                "asta_grimoire" -> {
                    // Asta's black headband with crimson clover emblem
                    drawRect(Color(0xFF18181B), Offset(-r * 0.72f, -r * 0.54f), Size(r * 1.44f, r * 0.16f))
                    drawCircle(Color(0xFFDC2626), radius = r * 0.08f, center = Offset(-r * 0.32f, -r * 0.46f))
                }
            }
        }
    }

    /**
     * Draws an unmistakable active Counter Ability barrier / immunity ring around the sphere.
     */
    fun drawActiveCounterOverlay(
        scope: DrawScope,
        ball: CombatBall,
        center: Offset,
        r: Float,
        globalTime: Float
    ) {
        if (!ball.isCounterActive) return
        with(scope) {
            val progress = (ball.counterActiveTimer / ball.counterDurationSec).coerceIn(0f, 1f)
            val ringColor = when (ball.character.counterArchetype) {
                CounterArchetype.TELEPORT_STRIKE -> Color(0xFFFACC15)
                CounterArchetype.TIME_IMMUNITY -> Color(0xFFFACC15)
                CounterArchetype.ANTI_MAGIC_NULL -> Color(0xFFEF4444)
                CounterArchetype.INFINITY_BARRIER_COUNTER -> Color(0xFF38BDF8)
                CounterArchetype.STAND_RUSH_COUNTER -> Color(0xFFC084FC)
                CounterArchetype.VECTOR_REFLECT_COUNTER -> Color(0xFF4ADE80)
                CounterArchetype.SHADOW_DODGE -> Color(0xFFA855F7)
                CounterArchetype.TITAN_ARMOR_COUNTER -> Color(0xFFF59E0B)
                CounterArchetype.GUARD_PARRY -> Color(0xFF38BDF8)
            }

            val shieldR = r * 1.36f
            drawCircle(
                color = ringColor.copy(alpha = 0.20f * progress),
                radius = shieldR,
                center = center
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(center.x - shieldR, center.y - shieldR),
                size = Size(shieldR * 2f, shieldR * 2f),
                style = Stroke(width = 4.2f, cap = StrokeCap.Round)
            )

            // Rotating archetype ticks around the Counter ring
            val ticks = 6
            for (i in 0 until ticks) {
                val a = globalTime * 6.5f + i * (2f * PI.toFloat() / ticks)
                drawLine(
                    color = Color.White.copy(alpha = 0.88f),
                    start = Offset(center.x + cos(a) * (shieldR - r * 0.10f), center.y + sin(a) * (shieldR - r * 0.10f)),
                    end = Offset(center.x + cos(a) * (shieldR + r * 0.10f), center.y + sin(a) * (shieldR + r * 0.10f)),
                    strokeWidth = 2.6f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
