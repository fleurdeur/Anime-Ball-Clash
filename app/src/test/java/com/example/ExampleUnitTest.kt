package com.example

import com.example.model.AppThemeMode
import com.example.model.BattleSimulation
import com.example.model.BotDifficulty
import com.example.model.CharacterRoster
import com.example.model.CombatBall
import com.example.model.EngineSettings
import com.example.model.InitialGameData
import com.example.model.PhysicsMesh
import com.example.model.PlayerProfile
import com.example.model.RenderMode
import com.example.model.TargetFPS
import com.example.model.Vector2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun testFreshZeroProgressAccountDefaults() {
        val profile = PlayerProfile()
        assertEquals(1, profile.level)
        assertEquals(0, profile.exp)
        assertEquals(0, profile.gold)
        assertEquals(0, profile.gems)
        assertEquals(0, profile.rankedRating)
        assertEquals("Unranked", profile.rankTier)
        assertEquals(0, profile.wins)
        assertEquals(0, profile.losses)

        val quests = InitialGameData.defaultQuests
        assertTrue(quests.isNotEmpty())
        quests.forEach { q ->
            assertEquals("Quest ${q.id} must start at 0 progress", 0, q.currentProgress)
            assertFalse("Quest ${q.id} must start unclaimed", q.isClaimed)
        }
    }

    @Test
    fun testTwentyAnimeCharacterRosterIntegrity() {
        val roster = CharacterRoster.characters
        assertEquals(20, roster.size)
        assertEquals(20, roster.map { it.id }.toSet().size)
        assertEquals(4, CharacterRoster.starterIds.size)

        val kakarot = CharacterRoster.getById("kakarot_z")
        assertNotNull(kakarot)
        assertEquals(100, kakarot.maxHp)
        assertTrue(kakarot.isStarter)

        val jinwoo = CharacterRoster.getById("jinwoo_monarch")
        assertNotNull(jinwoo)
        assertTrue(jinwoo.unlockCostGold > 0)
    }

    @Test
    fun testNonstopPerpetualRicochetUntilZeroHp() {
        val pBall = CombatBall(
            id = "player",
            name = "Player",
            position = Vector2.Zero,
            velocity = Vector2.Zero,
            character = CharacterRoster.getById("kakarot_z")
        )
        val oBall = CombatBall(
            id = "opponent",
            name = "Rival",
            position = Vector2.Zero,
            velocity = Vector2.Zero,
            character = CharacterRoster.getById("kamado_sun")
        )

        var bounceCount = 0
        var collisionCount = 0
        var knockoutOccurred = false

        val sim = BattleSimulation(
            playerBall = pBall,
            opponentBall = oBall,
            engineSettings = EngineSettings(),
            onCollisionImpact = { _, _, _, _ -> collisionCount++ },
            onWallBounce = { bounceCount++ },
            onKnockout = { knockoutOccurred = true }
        )

        sim.configureArenaBounds(1000f, 1600f)

        // Launch opening shot -> BOTH balls must ricochet nonstop without ever halting until 0 HP!
        sim.startNonstopClash(Vector2(0.55f, -0.83f), BotDifficulty.MEDIUM)

        var stepsRun = 0
        while (sim.isRoundActive && stepsRun < 3000) {
            sim.update(0.016f)
            sim.performAiTacticalStep(0.016f, BotDifficulty.MEDIUM)
            if (sim.isRoundActive) {
                if (!sim.playerBall.isStunned) {
                    assertTrue(
                        "Player ball must maintain nonstop velocity at step $stepsRun",
                        sim.playerBall.velocity.length() > 300f
                    )
                }
                if (!sim.opponentBall.isStunned) {
                    assertTrue(
                        "Opponent ball must maintain nonstop velocity at step $stepsRun",
                        sim.opponentBall.velocity.length() > 300f
                    )
                }
            }
            stepsRun++
        }

        assertTrue("Expected continuous wall bounces, got $bounceCount", bounceCount >= 10)
        assertTrue("Expected repeated collisions, got $collisionCount", collisionCount >= 4)
        assertTrue("Nonstop clash must run until one ball reaches 0 HP", knockoutOccurred)
        assertTrue(sim.playerBall.isDead || sim.opponentBall.isDead)
    }

    @Test
    fun testEngineSettingsDefaultsAndThemeModes() {
        val settings = EngineSettings()
        assertEquals(AppThemeMode.DARK, settings.themeMode)
        assertEquals(RenderMode.BALANCED, settings.renderMode)
        assertEquals(PhysicsMesh.DYNAMIC, settings.physicsMesh)
        assertEquals(TargetFPS.FPS_60, settings.targetFps)

        val lightSettings = settings.copy(themeMode = AppThemeMode.LIGHT)
        assertEquals(AppThemeMode.LIGHT, lightSettings.themeMode)
    }
}
