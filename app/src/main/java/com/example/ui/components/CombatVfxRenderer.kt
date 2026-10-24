package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.model.AbilityProjectile
import com.example.model.AbilityVfxStyle
import com.example.model.CanonProjectileStyle
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
    private val projectilePath = Path()
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

    fun drawProjectiles(
        scope: DrawScope,
        projectiles: List<AbilityProjectile>,
        globalTime: Float,
        renderMode: RenderMode
    ) {
        if (projectiles.isEmpty()) return
        val useBloom = renderMode.bloomGlow
        with(scope) {
            for (i in projectiles.indices) {
                val p = projectiles[i]
                val alpha = p.currentAlpha
                if (alpha <= 0.01f) continue

                val deg = p.angle * (180f / PI.toFloat())
                val fadeScale = if (p.isFadingOut) 1f + (1f - alpha) * 0.28f else 1f
                val r = p.radius * fadeScale

                withTransform({
                    translate(left = p.x, top = p.y)
                    rotate(degrees = deg, pivot = Offset.Zero)
                }) {
                    when (p.style) {
                        CanonProjectileStyle.HOLLOW_PURPLE_ORB -> {
                            // Wake trail behind orb
                            if (p.hoverTimer <= 0f) {
                                drawLine(
                                    color = p.primaryColor.copy(alpha = alpha * 0.42f),
                                    start = Offset(-r * 2.4f, 0f),
                                    end = Offset.Zero,
                                    strokeWidth = r * 1.35f,
                                    cap = StrokeCap.Round
                                )
                            }
                            if (useBloom) {
                                drawCircle(
                                    color = p.primaryColor.copy(alpha = alpha * 0.30f),
                                    radius = r * 1.55f,
                                    center = Offset.Zero
                                )
                            }
                            // Swirling Red & Blue sub-orbs merging into Hollow Purple
                            val spin = globalTime * 11f + p.id
                            val orbitDist = if (p.hoverTimer > 0f) r * 0.72f else r * 0.36f
                            drawCircle(
                                color = Color(0xFFEF4444).copy(alpha = alpha * 0.78f),
                                radius = r * 0.36f,
                                center = Offset(cos(spin) * orbitDist, sin(spin) * orbitDist)
                            )
                            drawCircle(
                                color = Color(0xFF38BDF8).copy(alpha = alpha * 0.78f),
                                radius = r * 0.36f,
                                center = Offset(-cos(spin) * orbitDist, -sin(spin) * orbitDist)
                            )
                            // Main Hollow Purple sphere + white singularity ring
                            drawCircle(
                                color = p.primaryColor.copy(alpha = alpha * 0.92f),
                                radius = r,
                                center = Offset.Zero
                            )
                            drawCircle(
                                color = Color(0xFF1E1B4B).copy(alpha = alpha * 0.68f),
                                radius = r * 0.56f,
                                center = Offset.Zero
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = alpha * 0.94f),
                                radius = r * 0.74f,
                                center = Offset.Zero,
                                style = Stroke(width = 2.8f)
                            )
                        }

                        CanonProjectileStyle.BABYLON_SWORD_RAIN -> {
                            // Golden Gate of Babylon portal ripple at rear while hovering or launching
                            val portalAlpha = if (p.hoverTimer > 0f) alpha * 0.92f else alpha * 0.48f
                            drawOval(
                                color = Color(0xFFFACC15).copy(alpha = portalAlpha * 0.34f),
                                topLeft = Offset(-r * 1.55f, -r * 0.85f),
                                size = Size(r * 0.75f, r * 1.7f)
                            )
                            drawOval(
                                color = Color(0xFFFEF08A).copy(alpha = portalAlpha),
                                topLeft = Offset(-r * 1.55f, -r * 0.85f),
                                size = Size(r * 0.75f, r * 1.7f),
                                style = Stroke(width = 2.2f)
                            )
                            if (p.hoverTimer <= 0f) {
                                drawLine(
                                    color = p.primaryColor.copy(alpha = alpha * 0.45f),
                                    start = Offset(-r * 2.2f, 0f),
                                    end = Offset(-r * 0.4f, 0f),
                                    strokeWidth = 4.2f,
                                    cap = StrokeCap.Round
                                )
                            }
                            // Noble Phantasm Sword Blade pointing +X toward enemy
                            projectilePath.reset()
                            projectilePath.moveTo(r * 1.65f, 0f)
                            projectilePath.lineTo(-r * 0.45f, -r * 0.28f)
                            projectilePath.lineTo(-r * 0.65f, 0f)
                            projectilePath.lineTo(-r * 0.45f, r * 0.28f)
                            projectilePath.close()
                            drawPath(
                                path = projectilePath,
                                color = p.primaryColor.copy(alpha = alpha * 0.95f)
                            )
                            // White razor spine + crossguard + grip + pommel
                            drawLine(
                                color = Color.White.copy(alpha = alpha * 0.92f),
                                start = Offset(-r * 0.45f, 0f),
                                end = Offset(r * 1.55f, 0f),
                                strokeWidth = 2.1f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = p.secondaryColor.copy(alpha = alpha * 0.95f),
                                start = Offset(-r * 0.52f, -r * 0.62f),
                                end = Offset(-r * 0.52f, r * 0.62f),
                                strokeWidth = 3.6f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color(0xFF78350F).copy(alpha = alpha * 0.95f),
                                start = Offset(-r * 1.08f, 0f),
                                end = Offset(-r * 0.54f, 0f),
                                strokeWidth = 3.2f,
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = Color(0xFFFACC15).copy(alpha = alpha),
                                radius = r * 0.18f,
                                center = Offset(-r * 1.15f, 0f)
                            )
                        }

                        CanonProjectileStyle.FLYING_RAIJIN_KUNAI -> {
                            // Yellow Flash streak
                            drawLine(
                                color = Color(0xFFFACC15).copy(alpha = alpha * 0.58f),
                                start = Offset(-r * 2.8f, 0f),
                                end = Offset.Zero,
                                strokeWidth = 4.6f,
                                cap = StrokeCap.Round
                            )
                            // Center kunai spearhead
                            projectilePath.reset()
                            projectilePath.moveTo(r * 1.65f, 0f)
                            projectilePath.lineTo(0f, -r * 0.34f)
                            projectilePath.lineTo(-r * 0.25f, 0f)
                            projectilePath.lineTo(0f, r * 0.34f)
                            projectilePath.close()
                            drawPath(projectilePath, Color(0xFFF8FAFC).copy(alpha = alpha * 0.96f))
                            // Side prongs (Minato's 3-pronged kunai)
                            drawLine(
                                color = Color(0xFFE2E8F0).copy(alpha = alpha * 0.95f),
                                start = Offset(-r * 0.15f, -r * 0.22f),
                                end = Offset(r * 0.85f, -r * 0.68f),
                                strokeWidth = 3.0f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color(0xFFE2E8F0).copy(alpha = alpha * 0.95f),
                                start = Offset(-r * 0.15f, r * 0.22f),
                                end = Offset(r * 0.85f, r * 0.68f),
                                strokeWidth = 3.0f,
                                cap = StrokeCap.Round
                            )
                            // Formula-wrapped handle + ring
                            drawLine(
                                color = Color(0xFFEF4444).copy(alpha = alpha * 0.95f),
                                start = Offset(-r * 0.95f, 0f),
                                end = Offset(-r * 0.22f, 0f),
                                strokeWidth = 3.6f,
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = Color(0xFFFACC15).copy(alpha = alpha),
                                radius = r * 0.24f,
                                center = Offset(-r * 1.18f, 0f),
                                style = Stroke(width = 2.4f)
                            )
                        }

                        CanonProjectileStyle.KI_ENERGY_BLAST -> {
                            drawLine(
                                color = p.primaryColor.copy(alpha = alpha * 0.55f),
                                start = Offset(-r * 2.7f, 0f),
                                end = Offset.Zero,
                                strokeWidth = r * 1.25f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color.White.copy(alpha = alpha * 0.72f),
                                start = Offset(-r * 2.1f, 0f),
                                end = Offset.Zero,
                                strokeWidth = r * 0.52f,
                                cap = StrokeCap.Round
                            )
                            if (useBloom) {
                                drawCircle(p.primaryColor.copy(alpha = alpha * 0.34f), radius = r * 1.5f, center = Offset.Zero)
                            }
                            drawCircle(p.primaryColor.copy(alpha = alpha * 0.92f), radius = r, center = Offset.Zero)
                            drawCircle(Color.White.copy(alpha = alpha * 0.94f), radius = r * 0.54f, center = Offset.Zero)
                        }

                        CanonProjectileStyle.ZOLTRAAK_ARCANE_BEAM -> {
                            // Arcane caster ring + piercing magic spear
                            drawOval(
                                color = p.primaryColor.copy(alpha = alpha * 0.82f),
                                topLeft = Offset(-r * 1.3f, -r * 0.85f),
                                size = Size(r * 0.55f, r * 1.7f),
                                style = Stroke(width = 2.2f)
                            )
                            drawLine(
                                color = p.primaryColor.copy(alpha = alpha * 0.75f),
                                start = Offset(-r * 2.5f, 0f),
                                end = Offset(r * 1.45f, 0f),
                                strokeWidth = r * 0.82f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color.White.copy(alpha = alpha * 0.96f),
                                start = Offset(-r * 1.9f, 0f),
                                end = Offset(r * 1.65f, 0f),
                                strokeWidth = r * 0.36f,
                                cap = StrokeCap.Round
                            )
                        }

                        CanonProjectileStyle.CRIMSON_SLASH_WAVE -> {
                            // Curved crescent sword wave (Getsuga / Breathing / Cleave)
                            val arcSize = Size(r * 2.2f, r * 2.6f)
                            val topLeft = Offset(-r * 1.1f, -r * 1.3f)
                            drawArc(
                                color = p.primaryColor.copy(alpha = alpha * 0.88f),
                                startAngle = -68f,
                                sweepAngle = 136f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = (r * 0.46f).coerceAtLeast(4f), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = Color.White.copy(alpha = alpha * 0.90f),
                                startAngle = -56f,
                                sweepAngle = 112f,
                                useCenter = false,
                                topLeft = Offset(-r * 0.98f, -r * 1.15f),
                                size = Size(r * 1.96f, r * 2.3f),
                                style = Stroke(width = (r * 0.19f).coerceAtLeast(2f), cap = StrokeCap.Round)
                            )
                        }

                        CanonProjectileStyle.FIRE_METEOR_ORB -> {
                            drawLine(
                                color = Color(0xFFEA580C).copy(alpha = alpha * 0.62f),
                                start = Offset(-r * 2.5f, 0f),
                                end = Offset.Zero,
                                strokeWidth = r * 1.35f,
                                cap = StrokeCap.Round
                            )
                            drawCircle(Color(0xFFEF4444).copy(alpha = alpha * 0.92f), radius = r * 1.05f, center = Offset.Zero)
                            drawCircle(Color(0xFFFACC15).copy(alpha = alpha * 0.94f), radius = r * 0.68f, center = Offset.Zero)
                            drawCircle(Color.White.copy(alpha = alpha * 0.90f), radius = r * 0.34f, center = Offset.Zero)
                        }

                        CanonProjectileStyle.LIGHTNING_SPEAR -> {
                            val zig = sin(globalTime * 36f + p.id) * r * 0.35f
                            drawLine(
                                color = p.primaryColor.copy(alpha = alpha * 0.85f),
                                start = Offset(-r * 2.1f, -zig * 0.5f),
                                end = Offset(-r * 0.4f, zig),
                                strokeWidth = 4.2f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = p.primaryColor.copy(alpha = alpha * 0.92f),
                                start = Offset(-r * 0.4f, zig),
                                end = Offset(r * 1.65f, 0f),
                                strokeWidth = 4.6f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color.White.copy(alpha = alpha * 0.95f),
                                start = Offset(-r * 1.6f, 0f),
                                end = Offset(r * 1.65f, 0f),
                                strokeWidth = 2.2f,
                                cap = StrokeCap.Round
                            )
                        }

                        CanonProjectileStyle.GRAVITY_DARK_ORB -> {
                            drawCircle(
                                color = p.primaryColor.copy(alpha = alpha * 0.42f),
                                radius = r * 1.38f,
                                center = Offset.Zero
                            )
                            drawCircle(
                                color = Color(0xFF09090B).copy(alpha = alpha * 0.92f),
                                radius = r * 0.86f,
                                center = Offset.Zero
                            )
                            drawCircle(
                                color = p.primaryColor.copy(alpha = alpha * 0.95f),
                                radius = r * 1.08f,
                                center = Offset.Zero,
                                style = Stroke(width = 3.0f)
                            )
                        }

                        CanonProjectileStyle.PSYCHIC_FORCE_SHOT -> {
                            drawCircle(
                                color = p.primaryColor.copy(alpha = alpha * 0.85f),
                                radius = r,
                                center = Offset.Zero,
                                style = Stroke(width = 3.2f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = alpha * 0.80f),
                                radius = r * 0.58f,
                                center = Offset(r * 0.25f, 0f),
                                style = Stroke(width = 2.2f)
                            )
                            drawCircle(
                                color = p.primaryColor.copy(alpha = alpha * 0.65f),
                                radius = r * 0.36f,
                                center = Offset.Zero
                            )
                        }

                        CanonProjectileStyle.BULLET_CANNON_VOLLEY -> {
                            drawLine(
                                color = p.primaryColor.copy(alpha = alpha * 0.65f),
                                start = Offset(-r * 3.0f, 0f),
                                end = Offset(r * 0.8f, 0f),
                                strokeWidth = r * 0.55f,
                                cap = StrokeCap.Round
                            )
                            drawOval(
                                color = Color.White.copy(alpha = alpha * 0.96f),
                                topLeft = Offset(-r * 0.5f, -r * 0.42f),
                                size = Size(r * 1.85f, r * 0.84f)
                            )
                        }
                    }
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
