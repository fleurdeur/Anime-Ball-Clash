package com.example.model

enum class RenderMode(
    val title: String,
    val subtitle: String,
    val description: String,
    val particleCountOnHit: Int
) {
    PIXELIZED(
        title = "PIXELIZED",
        subtitle = "8-Bit Retro",
        description = "Downscaled retro styling with pixel snap, flat sprite rings, 4-bit pixel burst particles. Ultra high performance.",
        particleCountOnHit = 6
    ),
    BALANCED(
        title = "BALANCED",
        subtitle = "60 FPS Native",
        description = "Standard 2D anime outline rendering, 2-segment kinematic hair chain, rotating sprite sheaths, 16 vector particles.",
        particleCountOnHit = 16
    ),
    REALISTIC(
        title = "REALISTIC",
        subtitle = "HD Shaders/Bloom",
        description = "Dynamic bloom lighting, motion blur trails, full Verlet rope hair, 2.5D rotating weapon meshes + 40+ dynamic GPU particles.",
        particleCountOnHit = 40
    )
}

enum class PhysicsMesh(val title: String, val description: String) {
    SIMPLE("SIMPLE", "Fast squish/stretch scale deformations based on velocity vector and impact normal."),
    DYNAMIC("DYNAMIC", "Full spring-mesh squish + Verlet integration rope hair chains with momentum lag and rebound.")
}

enum class TargetFPS(val fps: Int, val label: String) {
    FPS_30(30, "30 FPS (Battery Saver)"),
    FPS_60(60, "60 FPS (Native)"),
    FPS_90(90, "90 FPS (Smooth)"),
    FPS_120(120, "120 FPS (Ultra Flagship)")
}

data class EngineSettings(
    val renderMode: RenderMode = RenderMode.BALANCED,
    val physicsMesh: PhysicsMesh = PhysicsMesh.DYNAMIC,
    val targetFps: TargetFPS = TargetFPS.FPS_60,
    val sfxEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val bloomPostFx: Boolean = true,
    val screenShake: Boolean = true
)
