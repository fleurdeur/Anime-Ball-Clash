package com.example

import com.example.model.AnimeTier
import com.example.model.AppThemeMode
import com.example.model.BattleSimulation
import com.example.model.BotDifficulty
import com.example.model.CharacterAlignment
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
    fun testExpandedRosterCoversPopularToNicheAndProtagonistAntagonist() {
        val roster = CharacterRoster.characters
        assertEquals(48, roster.size)
        assertEquals(48, roster.map { it.id }.toSet().size)
        assertEquals(4, CharacterRoster.starterIds.size)

        val protagonists = roster.filter { it.alignment == CharacterAlignment.PROTAGONIST }
        val antagonists = roster.filter { it.alignment == CharacterAlignment.ANTAGONIST }
        assertTrue("Must have >= 25 protagonists", protagonists.size >= 25)
        assertTrue("Must have >= 15 antagonists", antagonists.size >= 15)

        assertTrue(roster.any { it.popularityTier == AnimeTier.POPULAR })
        assertTrue(roster.any { it.popularityTier == AnimeTier.CULT })
        assertTrue(roster.any { it.popularityTier == AnimeTier.NICHE })

        val bondrewd = CharacterRoster.getById("bondrewd_dawn")
        assertNotNull(bondrewd)
        assertEquals(CharacterAlignment.ANTAGONIST, bondrewd.alignment)
        assertEquals(AnimeTier.NICHE, bondrewd.popularityTier)
    }

    @Test
    fun testSymmetricAiAndPlayerUltimateProgressAndCooldown() {
        val pBall = CombatBall(
            id = "player",
            name = "Player",
            position = Vector2(500f, 1200f),
            velocity = Vector2.Zero,
            character = CharacterRoster.getById("kakarot_z")
        )
        val oBall = CombatBall(
            id = "opponent",
            name = "AI Rival",
            position = Vector2(500f, 300f),
            velocity = Vector2.Zero,
            character = CharacterRoster.getById("frieza_emperor")
        )

        var aiUltFired = false
        var playerUltFired = false

        val sim = BattleSimulation(
            playerBall = pBall,
            opponentBall = oBall,
            engineSettings = EngineSettings(),
            onCollisionImpact = { _, _, _, _ -> },
            onWallBounce = {},
            onKnockout = {},
            onUltimateActivated = { isPlayer, _ ->
                if (isPlayer) playerUltFired = true else aiUltFired = true
            }
        )
        sim.configureArenaBounds(1000f, 1600f)
        sim.startNonstopClash(Vector2(0.45f, -0.89f), BotDifficulty.HARD)

        // Charge both spheres to 100%
        pBall.ultimateGauge = 100f
        oBall.ultimateGauge = 100f
        assertTrue(pBall.canActivateUltimate)
        assertTrue(oBall.canActivateUltimate)

        // Player triggers ultimate manually
        assertTrue(sim.triggerUltimate(pBall))
        assertTrue(playerUltFired)
        assertTrue(pBall.isUltimateActive)

        // Step AI tactical loop past HARD reaction delay (0.14s) so AI activates Ultimate automatically
        repeat(15) {
            sim.update(0.02f)
            sim.performAiTacticalStep(0.02f, BotDifficulty.HARD)
        }
        assertTrue("AI should activate its Ultimate once 100% charged", aiUltFired)
        assertTrue(oBall.isUltimateActive)

        // Prevent KO while testing full Ultimate expiration -> Cooldown transition
        for (step in 0 until 320) {
            pBall.currentHp = 100f
            oBall.currentHp = 100f
            sim.update(0.02f)
        }

        // Both ultimates must have finished and entered mandatory Cooldown!
        assertFalse(pBall.isUltimateActive)
        assertFalse(oBall.isUltimateActive)
        assertTrue("Player must enter Ultimate cooldown after duration", pBall.isUltimateOnCooldown)
        assertTrue("AI must enter Ultimate cooldown after duration", oBall.isUltimateOnCooldown)
        assertEquals(0f, pBall.ultimateGauge, 0.01f)
        assertEquals(0f, oBall.ultimateGauge, 0.01f)

        // Cannot charge or re-activate while on cooldown
        pBall.addUltimate(50f)
        oBall.addUltimate(50f)
        assertEquals(0f, pBall.ultimateGauge, 0.01f)
        assertEquals(0f, oBall.ultimateGauge, 0.01f)
        assertFalse(sim.triggerUltimate(pBall))
        assertFalse(sim.triggerUltimate(oBall))
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
