package com.example.model

enum class AppThemeMode(val title: String, val subtitle: String) {
    DARK("Dark", "Matte Slate"),
    LIGHT("Light", "Warm Stone"),
    SYSTEM("System", "Match Device")
}

enum class RenderMode(
    val title: String,
    val subtitle: String,
    val description: String,
    val particleCountOnHit: Int
) {
    PIXELIZED(
        title = "Retro",
        subtitle = "Minimal FX",
        description = "Pixel sparks and zero motion trails for maximum battery efficiency.",
        particleCountOnHit = 6
    ),
    BALANCED(
        title = "Balanced",
        subtitle = "Native 60 FPS",
        description = "Crisp cel-shaded outlines, subtle speed trails, and clean collision bursts.",
        particleCountOnHit = 12
    ),
    REALISTIC(
        title = "Ultra",
        subtitle = "High FX",
        description = "Extended speed afterimages and richer arena collision sparks.",
        particleCountOnHit = 22
    )
}

enum class PhysicsMesh(val title: String, val description: String) {
    SIMPLE("Rigid", "Clean sphere geometry with zero impact squash deformation."),
    DYNAMIC("Elastic", "Responsive impact squash-and-stretch with locked anime details.")
}

enum class TargetFPS(val fps: Int, val label: String) {
    FPS_30(30, "30 FPS"),
    FPS_60(60, "60 FPS"),
    FPS_90(90, "90 FPS"),
    FPS_120(120, "120 FPS")
}

data class EngineSettings(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val renderMode: RenderMode = RenderMode.BALANCED,
    val physicsMesh: PhysicsMesh = PhysicsMesh.DYNAMIC,
    val targetFps: TargetFPS = TargetFPS.FPS_60,
    val sfxEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val bloomPostFx: Boolean = false,
    val screenShake: Boolean = true
)
