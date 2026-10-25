package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.Vector2
import kotlin.math.min
import kotlin.math.sin

object TrajectoryPredictor {

    private val dashEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)

    fun drawReadyReticle(
        scope: DrawScope,
        ballPosition: Vector2,
        ballRadius: Float,
        globalTime: Float,
        reticleColor: Color = Color(0xFFF8FAFC)
    ) {
        with(scope) {
            val pulse = 1.38f + sin(globalTime * 5f) * 0.06f
            drawCircle(
                color = reticleColor.copy(alpha = 0.52f),
                radius = ballRadius * pulse,
                center = Offset(ballPosition.x, ballPosition.y),
                style = Stroke(width = 2.5f, pathEffect = dashEffect)
            )
        }
    }

    fun drawAimTrajectory(
        scope: DrawScope,
        ballPosition: Vector2,
        dragVector: Vector2,
        maxPullPx: Float,
        wallLeft: Float,
        wallRight: Float,
        wallTop: Float,
        wallBottom: Float,
        ballRadius: Float,
        isRedirectAim: Boolean = false,
        reticleColor: Color = Color(0xFFF8FAFC)
    ) {
        with(scope) {
            val dragLen = dragVector.length()
            if (dragLen < 16f) return

            val launchDir = Vector2(-dragVector.x, -dragVector.y).normalized()
            val powerRatio = (dragLen / maxPullPx.coerceAtLeast(80f)).coerceIn(0.15f, 1.0f)

            val lowStretchColor = if (isRedirectAim) Color(0xFF38BDF8) else Color(0xFFFB923C)
            val highStretchColor = if (powerRatio >= 0.86f) Color(0xFFEF4444) else Color(0xFFFACC15)
            val activeAim = Color(
                red = lowStretchColor.red + (highStretchColor.red - lowStretchColor.red) * powerRatio,
                green = lowStretchColor.green + (highStretchColor.green - lowStretchColor.green) * powerRatio,
                blue = lowStretchColor.blue + (highStretchColor.blue - lowStretchColor.blue) * powerRatio,
                alpha = 1f
            )

            val ballCenter = Offset(ballPosition.x, ballPosition.y)
            val pullEnd = Offset(
                ballPosition.x + (dragVector.normalized().x * min(dragLen, maxPullPx)),
                ballPosition.y + (dragVector.normalized().y * min(dragLen, maxPullPx))
            )

            // Stretch tension aura ring around fighter ball
            drawCircle(
                color = activeAim.copy(alpha = 0.25f + powerRatio * 0.35f),
                radius = ballRadius * (1.05f + powerRatio * 0.48f),
                center = ballCenter,
                style = Stroke(width = 2.0f + powerRatio * 2.5f)
            )

            drawLine(
                color = activeAim.copy(alpha = 0.65f),
                start = ballCenter,
                end = pullEnd,
                strokeWidth = 2.6f + powerRatio * 3.8f,
                cap = StrokeCap.Round
            )
            drawCircle(color = activeAim, radius = 5.5f + powerRatio * 4.5f, center = pullEnd)

            val totalRayDist = (wallRight - wallLeft) * (0.48f + powerRatio * 1.48f)
            traceRayBounces(
                origin = ballPosition,
                initialDir = launchDir,
                totalDist = totalRayDist,
                wallLeft = wallLeft,
                wallRight = wallRight,
                wallTop = wallTop,
                wallBottom = wallBottom,
                maxBounces = if (isRedirectAim) 1 else 2,
                primaryColor = activeAim,
                nodeColor = reticleColor,
                baseStrokeWidth = 2.6f + powerRatio * 3.0f
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
        primaryColor: Color,
        nodeColor: Color,
        baseStrokeWidth: Float = 4.5f
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

            val segAlpha = (0.88f - bounce * 0.28f).coerceAtLeast(0.28f)
            val strokeW = (baseStrokeWidth - bounce * 1.0f).coerceAtLeast(2.0f)

            if (tMin < rem && bounce < maxBounces) {
                val hit = currPos + (currDir * tMin)
                drawLine(
                    color = primaryColor.copy(alpha = segAlpha),
                    start = Offset(currPos.x, currPos.y),
                    end = Offset(hit.x, hit.y),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round,
                    pathEffect = dashEffect
                )
                drawCircle(color = nodeColor, radius = 5f, center = Offset(hit.x, hit.y))
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
                    pathEffect = dashEffect
                )
                drawCircle(color = primaryColor, radius = 5f, center = Offset(end.x, end.y))
                break
            }
        }
    }
}
