package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
            for (hazard in hazards) {
                val alpha = (hazard.life / hazard.maxLife).coerceIn(0f, 0.85f)
                val pulse = sin(globalTime * 11f + hazard.id) * 0.14f + 1.0f
                val r = hazard.radius * pulse
                val c = Offset(hazard.x, hazard.y)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFEA00).copy(alpha = alpha * 0.85f),
                            Color(0xFFFF5E1A).copy(alpha = alpha * 0.65f),
                            Color.Transparent
                        ),
                        center = c,
                        radius = r
                    ),
                    radius = r,
                    center = c
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
            // 1. EXPANDING IMPACT SHOCKWAVES
            for (ring in shockwaves) {
                val progress = 1f - (ring.life / ring.maxLife).coerceIn(0f, 1f)
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val currentR = ring.maxRadius * (0.25f + progress * 0.75f)
                drawCircle(
                    color = ring.color.copy(alpha = alpha * 0.85f),
                    radius = currentR,
                    center = Offset(ring.x, ring.y),
                    style = Stroke(width = (10f * (1f - progress * 0.7f)).coerceAtLeast(2f))
                )
            }

            // 2. HIT SPARKS & PIXEL BURSTS
            for (p in particles) {
                val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                val pColor = p.color.copy(alpha = alpha)

                if (p.isPixel || renderMode == RenderMode.PIXELIZED) {
                    drawRect(
                        color = pColor,
                        topLeft = Offset(p.x - p.size, p.y - p.size),
                        size = Size(p.size * 2f, p.size * 2f)
                    )
                } else {
                    if (renderMode == RenderMode.REALISTIC) {
                        drawCircle(
                            color = pColor.copy(alpha = alpha * 0.35f),
                            radius = p.size * 2.1f,
                            center = Offset(p.x, p.y)
                        )
                    }
                    drawCircle(
                        color = pColor,
                        radius = p.size,
                        center = Offset(p.x, p.y)
                    )
                }
            }

            // 3. FLOATING ARCADE COMBAT NUMBERS
            for (ft in floatingTexts) {
                val alpha = (ft.life / ft.maxLife).coerceIn(0f, 1f)
                val fontSize = if (ft.isCrit) 21.sp else 16.sp
                val style = TextStyle(
                    fontFamily = displayFont,
                    color = ft.color.copy(alpha = alpha),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp
                )

                val layout = textMeasurer.measure(text = ft.text, style = style)
                val tx = ft.x - layout.size.width / 2f
                val ty = ft.y

                // Crisp dark drop-shadow outline
                drawText(
                    textMeasurer = textMeasurer,
                    text = ft.text,
                    topLeft = Offset(tx + 2.5f, ty + 2.5f),
                    style = style.copy(color = Color(0xFF07080F).copy(alpha = alpha))
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
