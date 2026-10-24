package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.model.FireHazardZone
import com.example.model.FloatingCombatText
import com.example.model.Particle
import com.example.model.RenderMode
import com.example.model.ShockwaveRing
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object CombatVfxRenderer {

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
                    color = baseColor.copy(alpha = alpha * 0.28f),
                    radius = r,
                    center = c
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.26f),
                    radius = r * 0.56f,
                    center = c
                )
                drawCircle(
                    color = baseColor.copy(alpha = alpha * 0.78f),
                    radius = r,
                    center = c,
                    style = Stroke(width = 2.6f)
                )

                // Rotating elemental rune spokes
                for (spoke in 0 until 4) {
                    val a = globalTime * 2.8f + spoke * (PI.toFloat() * 0.5f)
                    drawLine(
                        color = baseColor.copy(alpha = alpha * 0.65f),
                        start = Offset(c.x + cos(a) * r * 0.35f, c.y + sin(a) * r * 0.35f),
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
        renderMode: RenderMode
    ) {
        with(scope) {
            // 1. Dual-ring anime impact shockwaves
            for (i in shockwaves.indices) {
                val ring = shockwaves[i]
                val progress = 1f - (ring.life / ring.maxLife).coerceIn(0f, 1f)
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val currentR = ring.maxRadius * (0.26f + progress * 0.74f)
                val center = Offset(ring.x, ring.y)
                drawCircle(
                    color = ring.color.copy(alpha = alpha * 0.82f),
                    radius = currentR,
                    center = center,
                    style = Stroke(width = (8.5f * (1f - progress * 0.65f)).coerceAtLeast(2f))
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.55f),
                    radius = currentR * 0.76f,
                    center = center,
                    style = Stroke(width = (3.5f * (1f - progress * 0.5f)).coerceAtLeast(1.2f))
                )
            }

            // 2. Hit sparks & velocity streaks
            for (i in particles.indices) {
                val p = particles[i]
                val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                val pColor = p.color.copy(alpha = alpha)

                if (p.isPixel || renderMode == RenderMode.PIXELIZED) {
                    drawRect(
                        color = pColor,
                        topLeft = Offset(p.x - p.size, p.y - p.size),
                        size = Size(p.size * 2f, p.size * 2f)
                    )
                } else {
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

            // 3. Floating combat damage & passive callouts
            for (i in floatingTexts.indices) {
                val ft = floatingTexts[i]
                val alpha = (ft.life / ft.maxLife).coerceIn(0f, 1f)
                val fontSize = if (ft.isCrit) 19.sp else 15.sp
                val style = TextStyle(
                    fontFamily = displayFont,
                    color = ft.color.copy(alpha = alpha),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )

                val layout = textMeasurer.measure(text = ft.text, style = style)
                val tx = ft.x - layout.size.width / 2f
                val ty = ft.y

                drawText(
                    textMeasurer = textMeasurer,
                    text = ft.text,
                    topLeft = Offset(tx + 2f, ty + 2f),
                    style = style.copy(color = Color(0xFF09090B).copy(alpha = alpha))
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = ft.text,
                    topLeft = Offset(tx, ty),
                    style = style
                )
            }
        }
    }
}
