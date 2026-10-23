package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
                val alpha = (hazard.life / hazard.maxLife).coerceIn(0f, 0.85f)
                val pulse = sin(globalTime * 10f + hazard.id) * 0.08f + 1.0f
                val r = hazard.radius * pulse
                val c = Offset(hazard.x, hazard.y)

                // Flat concentric cel-shaded fire rings (zero Brush allocation!)
                drawCircle(
                    color = Color(0xFFF97316).copy(alpha = alpha * 0.32f),
                    radius = r,
                    center = c
                )
                drawCircle(
                    color = Color(0xFFFACC15).copy(alpha = alpha * 0.45f),
                    radius = r * 0.58f,
                    center = c
                )
                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = alpha * 0.70f),
                    radius = r,
                    center = c,
                    style = Stroke(width = 2.5f)
                )
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
            // 1. Expanding impact shockwaves
            for (i in shockwaves.indices) {
                val ring = shockwaves[i]
                val progress = 1f - (ring.life / ring.maxLife).coerceIn(0f, 1f)
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val currentR = ring.maxRadius * (0.28f + progress * 0.72f)
                drawCircle(
                    color = ring.color.copy(alpha = alpha * 0.80f),
                    radius = currentR,
                    center = Offset(ring.x, ring.y),
                    style = Stroke(width = (8f * (1f - progress * 0.65f)).coerceAtLeast(2f))
                )
            }

            // 2. Hit sparks
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
                    drawCircle(
                        color = pColor,
                        radius = p.size,
                        center = Offset(p.x, p.y)
                    )
                }
            }

            // 3. Floating combat damage callouts
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
