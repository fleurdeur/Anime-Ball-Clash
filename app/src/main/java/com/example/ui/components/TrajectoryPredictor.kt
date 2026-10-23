package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.Vector2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object TrajectoryPredictor {

    /**
     * Subtle animated reticle around the player's sphere when TurnState == PLAYER_AIMING.
     */
    fun drawReadyReticle(
        scope: DrawScope,
        ballPosition: Vector2,
        ballRadius: Float,
        globalTime: Float
    ) {
        with(scope) {
            val pulse = 1.42f + sin(globalTime * 5.5f) * 0.08f
            val ringR = ballRadius * pulse
            val center = Offset(ballPosition.x, ballPosition.y)

            drawCircle(
                color = Color(0xFF00F0FF).copy(alpha = 0.55f),
                radius = ringR,
                center = center,
                style = Stroke(
                    width = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), globalTime * 45f)
                )
            )
        }
    }

    /**
     * Draws player pullback power ring + 2-bounce ricochet trajectory laser.
     */
    fun drawAimTrajectory(
        scope: DrawScope,
        ballPosition: Vector2,
        dragVector: Vector2,
        maxPullPx: Float,
        wallLeft: Float,
        wallRight: Float,
        wallTop: Float,
        wallBottom: Float,
        ballRadius: Float
    ) {
        with(scope) {
            val dragLen = dragVector.length()
            if (dragLen < 16f) return

            val launchDir = Vector2(-dragVector.x, -dragVector.y).normalized()
            val powerRatio = min(dragLen / maxPullPx.coerceAtLeast(100f), 1.0f)

            val aimColor = when {
                powerRatio < 0.40f -> Color(0xFF00F0FF)
                powerRatio < 0.78f -> Color(0xFFFFC800)
                else -> Color(0xFFFF3D00)
            }

            val ballCenter = Offset(ballPosition.x, ballPosition.y)
            val pullEnd = Offset(
                ballPosition.x + (dragVector.normalized().x * min(dragLen, maxPullPx)),
                ballPosition.y + (dragVector.normalized().y * min(dragLen, maxPullPx))
            )

            // Pullback bow tether behind ball
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = ballCenter,
                end = pullEnd,
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            drawCircle(color = aimColor, radius = 9f, center = pullEnd)

            // Power arc around sphere
            val gaugeR = ballRadius * 1.48f
            drawArc(
                color = aimColor.copy(alpha = 0.25f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(ballCenter.x - gaugeR, ballCenter.y - gaugeR),
                size = Size(gaugeR * 2f, gaugeR * 2f),
                style = Stroke(width = 6f)
            )
            drawArc(
                color = aimColor,
                startAngle = -90f,
                sweepAngle = powerRatio * 360f,
                useCenter = false,
                topLeft = Offset(ballCenter.x - gaugeR, ballCenter.y - gaugeR),
                size = Size(gaugeR * 2f, gaugeR * 2f),
                style = Stroke(width = 7f, cap = StrokeCap.Round)
            )

            // Multi-bounce raycast (up to 2 wall rebounds!)
            val totalRayDist = (wallRight - wallLeft) * (0.65f + powerRatio * 1.45f)
            traceRayBounces(
                origin = ballPosition,
                initialDir = launchDir,
                totalDist = totalRayDist,
                wallLeft = wallLeft,
                wallRight = wallRight,
                wallTop = wallTop,
                wallBottom = wallBottom,
                maxBounces = 2,
                primaryColor = aimColor
            )
        }
    }

    /**
     * Draws AI opponent's telegraph aiming laser right before it launches its counter-attack!
     */
    fun drawAiTelegraph(
        scope: DrawScope,
        aiPosition: Vector2,
        aiDirection: Vector2,
        chargeProgress: Float,
        wallLeft: Float,
        wallRight: Float,
        wallTop: Float,
        wallBottom: Float,
        ballRadius: Float
    ) {
        with(scope) {
            val center = Offset(aiPosition.x, aiPosition.y)
            val warnColor = Color(0xFFFF1E42)
            val ringR = ballRadius * (1.75f - chargeProgress * 0.32f)

            // Contracting lock-on ring
            drawCircle(
                color = warnColor.copy(alpha = 0.35f + chargeProgress * 0.55f),
                radius = ringR,
                center = center,
                style = Stroke(width = 5f)
            )

            val rayDist = (wallRight - wallLeft) * (0.55f + chargeProgress * 0.85f)
            traceRayBounces(
                origin = aiPosition,
                initialDir = aiDirection.normalized(),
                totalDist = rayDist,
                wallLeft = wallLeft,
                wallRight = wallRight,
                wallTop = wallTop,
                wallBottom = wallBottom,
                maxBounces = 1,
                primaryColor = warnColor
            )
        }
    }

    private fun DrawScope.traceRayBounces(
        origin: Vector2,
        initialDir: Vector2,
        totalDist: Float,
        wallLeft: Float,
        wallRight: Float,
        wallTop: Float,
        wallBottom: Float,
        maxBounces: Int,
        primaryColor: Color
    ) {
        var currPos = origin
        var currDir = initialDir
        var rem = totalDist

        for (bounce in 0..maxBounces) {
            if (rem <= 4f) break

            var tMin = Float.MAX_VALUE
            var normal = Vector2.Zero

            if (currDir.x > 0.0001f) {
                val t = (wallRight - currPos.x) / currDir.x
                if (t in 0.5f..tMin) {
                    tMin = t
                    normal = Vector2(-1f, 0f)
                }
            } else if (currDir.x < -0.0001f) {
                val t = (wallLeft - currPos.x) / currDir.x
                if (t in 0.5f..tMin) {
                    tMin = t
                    normal = Vector2(1f, 0f)
                }
            }

            if (currDir.y > 0.0001f) {
                val t = (wallBottom - currPos.y) / currDir.y
                if (t in 0.5f..tMin) {
                    tMin = t
                    normal = Vector2(0f, -1f)
                }
            } else if (currDir.y < -0.0001f) {
                val t = (wallTop - currPos.y) / currDir.y
                if (t in 0.5f..tMin) {
                    tMin = t
                    normal = Vector2(0f, 1f)
                }
            }

            val segAlpha = (0.92f - bounce * 0.28f).coerceAtLeast(0.25f)
            val strokeW = (5.5f - bounce * 1.4f).coerceAtLeast(2.5f)

            if (tMin < rem && bounce < maxBounces) {
                val hit = currPos + (currDir * tMin)
                drawLine(
                    color = primaryColor.copy(alpha = segAlpha),
                    start = Offset(currPos.x, currPos.y),
                    end = Offset(hit.x, hit.y),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
                )
                // Ricochet node spark
                drawCircle(color = Color.White, radius = 6f, center = Offset(hit.x, hit.y))
                drawCircle(color = primaryColor, radius = 11f, center = Offset(hit.x, hit.y), style = Stroke(width = 2.5f))

                rem -= tMin
                currPos = hit
                currDir = (currDir - (normal * (2f * currDir.dot(normal)))).normalized()
            } else {
                val end = currPos + (currDir * min(rem, tMin))
                drawLine(
                    color = primaryColor.copy(alpha = segAlpha),
                    start = Offset(currPos.x, currPos.y),
                    end = Offset(end.x, end.y),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
                )
                drawCircle(color = primaryColor, radius = 6f, center = Offset(end.x, end.y))
                break
            }
        }
    }
}
