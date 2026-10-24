package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.model.AbilityVfxStyle
import com.example.model.FireHazardZone
import com.example.model.FloatingCombatText
import com.example.model.Particle
import com.example.model.RenderMode
import com.example.model.ShockwaveRing
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object CombatVfxRenderer {

    private val sigilPath = Path()
    private var cachedFont: FontFamily? = null
    private var cachedNormalStyle: TextStyle? = null
    private var cachedCritStyle: TextStyle? = null

    private fun getCachedStyle(font: FontFamily, isCrit: Boolean): TextStyle {
        if (cachedFont !== font || cachedNormalStyle == null || cachedCritStyle == null) {
            cachedFont = font
            cachedNormalStyle = TextStyle(
                fontFamily = font,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
            cachedCritStyle = TextStyle(
                fontFamily = font,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }
        return if (isCrit) cachedCritStyle!! else cachedNormalStyle!!
    }

    fun drawFloorHazards(
        scope: DrawScope,
        hazards: List<FireHazardZone>,
        globalTime: Float
    ) {
        with(scope) {
            for (i in hazards.indices) {
                val hazard = hazards[i]
                val alpha = (hazard.life / hazard.maxLife).coerceIn(0f, 0.88f)
                val pulse = sin(globalTime * 10f + hazard.id) * 0.08f + 1.0f
                val r = hazard.radius * pulse
                val c = Offset(hazard.x, hazard.y)
                val baseColor = hazard.color

                drawCircle(
                    color = baseColor.copy(alpha = alpha * 0.26f),
                    radius = r,
                    center = c
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.22f),
                    radius = r * 0.56f,
                    center = c
                )
                drawCircle(
                    color = baseColor.copy(alpha = alpha * 0.78f),
                    radius = r,
                    center = c,
                    style = Stroke(width = 2.6f)
                )

                for (spoke in 0 until 6) {
                    val a = globalTime * 2.6f + spoke * (PI.toFloat() / 3f)
                    drawLine(
                        color = baseColor.copy(alpha = alpha * 0.65f),
                        start = Offset(c.x + cos(a) * r * 0.34f, c.y + sin(a) * r * 0.34f),
                        end = Offset(c.x + cos(a) * r * 0.88f, c.y + sin(a) * r * 0.88f),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }

    fun drawVfx(
        scope: DrawScope,
        particles: List<Particle>,
        shockwaves: List<ShockwaveRing>,
        floatingTexts: List<FloatingCombatText>,
        textMeasurer: TextMeasurer,
        displayFont: FontFamily,
        renderMode: RenderMode,
        defaultTextColor: Color = Color(0xFFF8FAFC),
        isDarkStage: Boolean = true
    ) {
        with(scope) {
            // 1. Ability-specific anime shockwaves, sword slashes, domain sigils, time dials & lightning bursts
            for (i in shockwaves.indices) {
                val ring = shockwaves[i]
                val progress = 1f - (ring.life / ring.maxLife).coerceIn(0f, 1f)
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val currentR = ring.maxRadius * (0.24f + progress * 0.76f)
                val center = Offset(ring.x, ring.y)
                drawStyledShockwave(ring.style, center, currentR, ring.color, progress, alpha)
            }

            // 2. Hit sparks & velocity streaks
            val usePixel = renderMode == RenderMode.PIXELIZED
            val useBloom = renderMode.bloomGlow
            for (i in particles.indices) {
                val p = particles[i]
                val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                val pColor = p.color.copy(alpha = alpha)

                if (p.isPixel || usePixel) {
                    drawRect(
                        color = pColor,
                        topLeft = Offset(p.x - p.size, p.y - p.size),
                        size = Size(p.size * 2f, p.size * 2f)
                    )
                } else {
                    if (useBloom) {
                        drawCircle(
                            color = p.color.copy(alpha = alpha * 0.28f),
                            radius = p.size * 2.1f,
                            center = Offset(p.x, p.y)
                        )
                    }
                    drawLine(
                        color = pColor,
                        start = Offset(p.x, p.y),
                        end = Offset(p.x - p.vx * 0.018f, p.y - p.vy * 0.018f),
                        strokeWidth = (p.size * 1.35f).coerceAtLeast(2f),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = pColor,
                        radius = p.size,
                        center = Offset(p.x, p.y)
                    )
                }
            }

            // 3. Cached-layout floating combat numbers & callouts (100% TextMeasurer cache hit rate!)
            val shadowBase = if (isDarkStage) Color(0xFF09090B) else Color(0xFFFFFFFF)
            for (i in floatingTexts.indices) {
                val ft = floatingTexts[i]
                val alpha = (ft.life / ft.maxLife).coerceIn(0f, 1f)
                val baseStyle = getCachedStyle(displayFont, ft.isCrit)
                val layout = textMeasurer.measure(text = ft.text, style = baseStyle)
                val tx = ft.x - layout.size.width / 2f
                val ty = ft.y
                val fgColor = if (ft.color == Color.Unspecified) {
                    defaultTextColor
                } else if (!isDarkStage && ft.color == Color(0xFFFACC15)) {
                    Color(0xFFB45309)
                } else {
                    ft.color
                }

                drawText(
                    textLayoutResult = layout,
                    color = shadowBase.copy(alpha = alpha * 0.90f),
                    topLeft = Offset(tx + 1.8f, ty + 1.8f)
                )
                drawText(
                    textLayoutResult = layout,
                    color = fgColor.copy(alpha = alpha),
                    topLeft = Offset(tx, ty)
                )
            }
        }
    }

    private fun DrawScope.drawStyledShockwave(
        style: AbilityVfxStyle,
        center: Offset,
        r: Float,
        color: Color,
        progress: Float,
        alpha: Float
    ) {
        when (style) {
            AbilityVfxStyle.SHOCKWAVE -> {
                drawCircle(
                    color = color.copy(alpha = alpha * 0.82f),
                    radius = r,
                    center = center,
                    style = Stroke(width = (8.5f * (1f - progress * 0.65f)).coerceAtLeast(2f))
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.55f),
                    radius = r * 0.76f,
                    center = center,
                    style = Stroke(width = (3.4f * (1f - progress * 0.5f)).coerceAtLeast(1.2f))
                )
            }

            AbilityVfxStyle.SLASH_CROSS -> {
                // Dramatic anime X-slash cleave + outer crimson/aura ring
                drawCircle(
                    color = color.copy(alpha = alpha * 0.65f),
                    radius = r * 0.92f,
                    center = center,
                    style = Stroke(width = 3.5f)
                )
                val len = r * 1.12f
                val w = (9f * (1f - progress * 0.55f)).coerceAtLeast(2.5f)
                drawLine(color.copy(alpha = alpha * 0.92f), Offset(center.x - len, center.y - len * 0.72f), Offset(center.x + len, center.y + len * 0.72f), strokeWidth = w, cap = StrokeCap.Round)
                drawLine(color.copy(alpha = alpha * 0.92f), Offset(center.x - len, center.y + len * 0.72f), Offset(center.x + len, center.y - len * 0.72f), strokeWidth = w, cap = StrokeCap.Round)
                drawLine(Color.White.copy(alpha = alpha * 0.80f), Offset(center.x - len * 0.85f, center.y - len * 0.61f), Offset(center.x + len * 0.85f, center.y + len * 0.61f), strokeWidth = w * 0.36f, cap = StrokeCap.Round)
                drawLine(Color.White.copy(alpha = alpha * 0.80f), Offset(center.x - len * 0.85f, center.y + len * 0.61f), Offset(center.x + len * 0.85f, center.y - len * 0.61f), strokeWidth = w * 0.36f, cap = StrokeCap.Round)
            }

            AbilityVfxStyle.DOMAIN_SIGIL -> {
                // Sacred geometric hexagonal domain expansion sigil
                drawCircle(color.copy(alpha = alpha * 0.20f), radius = r, center = center)
                drawCircle(color.copy(alpha = alpha * 0.85f), radius = r, center = center, style = Stroke(width = 4.2f))
                sigilPath.reset()
                for (k in 0 until 6) {
                    val a = progress * 1.4f + k * (PI.toFloat() / 3f)
                    val px = center.x + cos(a) * r * 0.86f
                    val py = center.y + sin(a) * r * 0.86f
                    if (k == 0) sigilPath.moveTo(px, py) else sigilPath.lineTo(px, py)
                }
                sigilPath.close()
                drawPath(sigilPath, Color.White.copy(alpha = alpha * 0.72f), style = Stroke(width = 2.4f, join = StrokeJoin.Round))
            }

            AbilityVfxStyle.LIGHTNING_NOVA -> {
                drawCircle(color.copy(alpha = alpha * 0.72f), radius = r * 0.78f, center = center, style = Stroke(width = 3.2f))
                for (b in 0 until 8) {
                    val a = b * (PI.toFloat() / 4f) + progress * 0.6f
                    val midR = r * 0.56f
                    val pMid = Offset(center.x + cos(a + 0.14f) * midR, center.y + sin(a + 0.14f) * midR)
                    val pEnd = Offset(center.x + cos(a) * r * 1.08f, center.y + sin(a) * r * 1.08f)
                    drawLine(color.copy(alpha = alpha * 0.90f), center, pMid, strokeWidth = 3.2f, cap = StrokeCap.Round)
                    drawLine(Color.White.copy(alpha = alpha * 0.85f), pMid, pEnd, strokeWidth = 2.4f, cap = StrokeCap.Round)
                }
            }

            AbilityVfxStyle.GRAVITY_IMPLODE -> {
                val invR = r * (1.18f - progress * 0.55f)
                drawCircle(Color(0xFF09090B).copy(alpha = alpha * 0.45f), radius = r * 0.42f, center = center)
                drawCircle(color.copy(alpha = alpha * 0.88f), radius = invR, center = center, style = Stroke(width = 5.0f))
                drawCircle(Color.White.copy(alpha = alpha * 0.60f), radius = r * 0.42f, center = center, style = Stroke(width = 2.2f))
            }

            AbilityVfxStyle.HEAL_BLOOM -> {
                drawCircle(color.copy(alpha = alpha * 0.75f), radius = r * 0.88f, center = center, style = Stroke(width = 4.2f))
                for (c in 0 until 4) {
                    val a = c * (PI.toFloat() * 0.5f) + 0.78f
                    val cx = center.x + cos(a) * r * 0.58f
                    val cy = center.y + sin(a) * r * 0.58f
                    val arm = r * 0.14f
                    drawLine(color.copy(alpha = alpha * 0.92f), Offset(cx - arm, cy), Offset(cx + arm, cy), strokeWidth = 3.4f, cap = StrokeCap.Round)
                    drawLine(color.copy(alpha = alpha * 0.92f), Offset(cx, cy - arm), Offset(cx, cy + arm), strokeWidth = 3.4f, cap = StrokeCap.Round)
                }
            }

            AbilityVfxStyle.TIME_DIAL -> {
                drawCircle(color.copy(alpha = alpha * 0.88f), radius = r, center = center, style = Stroke(width = 4.2f))
                for (t in 0 until 12) {
                    val a = t * (PI.toFloat() / 6f)
                    drawLine(
                        color = color.copy(alpha = alpha * 0.82f),
                        start = Offset(center.x + cos(a) * r * 0.82f, center.y + sin(a) * r * 0.82f),
                        end = Offset(center.x + cos(a) * r * 0.98f, center.y + sin(a) * r * 0.98f),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                }
                val h1 = -PI.toFloat() * 0.5f + progress * 4.2f
                drawLine(Color.White.copy(alpha = alpha * 0.9f), center, Offset(center.x + cos(h1) * r * 0.70f, center.y + sin(h1) * r * 0.70f), strokeWidth = 3.2f, cap = StrokeCap.Round)
                drawLine(color.copy(alpha = alpha * 0.9f), center, Offset(center.x + cos(0.4f) * r * 0.46f, center.y + sin(0.4f) * r * 0.46f), strokeWidth = 3.6f, cap = StrokeCap.Round)
            }

            AbilityVfxStyle.METEOR_BURST -> {
                drawCircle(color.copy(alpha = alpha * 0.30f), radius = r * 0.75f, center = center)
                drawCircle(color.copy(alpha = alpha * 0.88f), radius = r, center = center, style = Stroke(width = 6.5f))
                for (ray in 0 until 8) {
                    val a = ray * (PI.toFloat() / 4f)
                    drawLine(
                        color = Color(0xFFFEF08A).copy(alpha = alpha * 0.85f),
                        start = Offset(center.x + cos(a) * r * 0.45f, center.y + sin(a) * r * 0.45f),
                        end = Offset(center.x + cos(a) * r * 1.14f, center.y + sin(a) * r * 1.14f),
                        strokeWidth = 3.2f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
