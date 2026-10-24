package com.example.model

enum class AppThemeMode(val title: String, val subtitle: String) {
    DARK("Dark", "Night"),
    LIGHT("Light", "Day"),
    SYSTEM("Auto", "Device")
}

enum class RenderMode(
    val title: String,
    val subtitle: String,
    val description: String,
    val particleCountOnHit: Int,
    val maxParticles: Int,
    val maxShockwaves: Int,
    val maxTrailNodes: Int,
    val ambientAuraFx: Boolean,
    val bloomGlow: Boolean
) {
    PIXELIZED(
        title = "Low",
        subtitle = "Fast",
        description = "Simple pixel sparks and no trails to save battery.",
        particleCountOnHit = 6,
        maxParticles = 24,
        maxShockwaves = 4,
        maxTrailNodes = 0,
        ambientAuraFx = false,
        bloomGlow = false
    ),
    BALANCED(
        title = "Medium",
        subtitle = "Smooth",
        description = "Clean outlines, short speed trails, and smooth sparks.",
        particleCountOnHit = 12,
        maxParticles = 48,
        maxShockwaves = 8,
        maxTrailNodes = 5,
        ambientAuraFx = true,
        bloomGlow = false
    ),
    REALISTIC(
        title = "High",
        subtitle = "Detailed",
        description = "Longer trails, glowing auras, and elemental sparks.",
        particleCountOnHit = 18,
        maxParticles = 72,
        maxShockwaves = 12,
        maxTrailNodes = 7,
        ambientAuraFx = true,
        bloomGlow = true
    ),
    ULTRA(
        title = "Ultra",
        subtitle = "Max FX",
        description = "Full weapon trails, dense sparks, aura lightning, and glow.",
        particleCountOnHit = 26,
        maxParticles = 96,
        maxShockwaves = 16,
        maxTrailNodes = 10,
        ambientAuraFx = true,
        bloomGlow = true
    )
}

enum class PhysicsMesh(val title: String, val description: String) {
    SIMPLE("Solid", "Balls keep a firm round shape on hits."),
    DYNAMIC("Bouncy", "Balls squash and stretch on fast hits.")
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
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val bloomPostFx: Boolean = false,
    val screenShake: Boolean = true
)
