package com.example

import com.example.model.BattleSimulation
import com.example.model.BotDifficulty
import com.example.model.CharacterRoster
import com.example.model.CombatBall
import com.example.model.EngineSettings
import com.example.model.PhysicsMesh
import com.example.model.RenderMode
import com.example.model.TargetFPS
import com.example.model.Vector2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testVectorMathematics() {
        val v1 = Vector2(3f, 4f)
        assertEquals(5f, v1.length(), 0.001f)

        val v2 = Vector2(1f, 2f)
        val sum = v1 + v2
        assertEquals(4f, sum.x, 0.001f)
        assertEquals(6f, sum.y, 0.001f)

        val dot = v1.dot(Vector2(2f, 0f))
        assertEquals(6f, dot, 0.001f)

        val normalized = v1.normalized()
        assertEquals(1f, normalized.length(), 0.001f)
    }

    @Test
    fun testBallMovementWallBouncesAndAiResponse() {
        val pBall = CombatBall(
            id = "player",
            name = "Player",
            position = Vector2.Zero,
            velocity = Vector2.Zero,
            character = CharacterRoster.getById("saiyan_spirit")
        )
        val oBall = CombatBall(
            id = "opponent",
            name = "AI",
            position = Vector2.Zero,
            velocity = Vector2.Zero,
            character = CharacterRoster.getById("demon_slash")
        )

        var bounceCount = 0
        var collisionCount = 0

        val sim = BattleSimulation(
            playerBall = pBall,
            opponentBall = oBall,
            engineSettings = EngineSettings(),
            onCollisionImpact = { _, _, _, _ -> collisionCount++ },
            onWallBounce = { bounceCount++ },
            onKnockout = {}
        )

        sim.configureArenaBounds(1000f, 1600f)
        val initialPlayerPos = sim.playerBall.position

        // 1. Launch player ball diagonally at walls -> must move & bounce!
        sim.launchPlayer(Vector2(0.8f, -0.6f), 1.0f)
        repeat(90) { sim.update(0.016f) }

        assertNotEquals(initialPlayerPos.x, sim.playerBall.position.x, 1f)
        assertTrue("Expected at least 1 wall bounce, got $bounceCount", bounceCount >= 1)

        // 2. Simulate until rest, then AI aims & launches its own ball!
        repeat(150) { sim.update(0.016f) }
        sim.haltBalls()

        val initialAiPos = sim.opponentBall.position
        val aiDir = sim.computeAiAimDirection(BotDifficulty.MEDIUM)
        sim.launchOpponent(aiDir, 0.9f)
        repeat(45) { sim.update(0.016f) }

        assertTrue(
            "AI ball must move significantly after launching",
            sim.opponentBall.position.distanceTo(initialAiPos) > 120f
        )
        assertTrue("Collision should have occurred when AI aimed at player", collisionCount >= 1 || sim.playerBall.currentHp < 100f)
    }

    @Test
    fun testCharacterRosterIntegrity() {
        val roster = CharacterRoster.characters
        assertTrue(roster.size >= 8)
        val saiyan = CharacterRoster.getById("saiyan_spirit")
        assertNotNull(saiyan)
        assertEquals(100, saiyan.maxHp)
        assertEquals(16, saiyan.baseDamage)
    }

    @Test
    fun testEngineSettingsDefaults() {
        val settings = EngineSettings()
        assertEquals(RenderMode.BALANCED, settings.renderMode)
        assertEquals(PhysicsMesh.DYNAMIC, settings.physicsMesh)
        assertEquals(TargetFPS.FPS_60, settings.targetFps)
    }
}
