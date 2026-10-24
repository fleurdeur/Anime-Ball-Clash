package com.example

import com.example.audio.GameSfx
import com.example.audio.SoundManager
import com.example.model.AnimeTier
import com.example.model.AppThemeMode
import com.example.model.BattleSimulation
import com.example.model.BotDifficulty
import com.example.model.CharacterAlignment
import com.example.model.CharacterRoster
import com.example.model.CharacterUpgradeState
import com.example.model.CombatBall
import com.example.model.EngineSettings
import com.example.model.InitialGameData
import com.example.model.PassiveArchetype
import com.example.model.PhysicsMesh
import com.example.model.PlayerProfile
import com.example.model.RenderMode
import com.example.model.TargetFPS
import com.example.model.AbilityVfxStyle
import com.example.model.GameMode
import com.example.model.UltimateArchetype
import com.example.model.UpgradeStatType
import com.example.model.Vector2
import com.example.network.OnlineArenaRepository
import com.example.ui.theme.DarkAppPalette
import com.example.ui.theme.LightAppPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun testRosterExceeds200CharactersWithInflatedCostsAndAlignments() {
        val roster = CharacterRoster.characters
        assertTrue("Expected 200+ characters, got ${roster.size}", roster.size >= 200)
        assertEquals(224, roster.size)
        assertEquals("Every character ID must be strictly unique", roster.size, roster.map { it.id }.toSet().size)
        assertEquals(4, CharacterRoster.starterIds.size)

        val starters = roster.filter { it.id in CharacterRoster.starterIds }
        assertEquals(4, starters.size)
        starters.forEach { starter ->
            assertEquals("Starter ${starter.id} must be free on fresh install", 0, starter.unlockCostGold)
        }

        val unlockables = roster.filter { it.id !in CharacterRoster.starterIds }
        assertTrue(unlockables.all { it.unlockCostGold >= 5_500 })
        assertTrue("Top mythic characters must have heavily inflated costs", unlockables.maxOf { it.unlockCostGold } >= 100_000)

        // Longer match base HP across all 224 characters
        assertTrue("All characters must have elevated HP for longer matches", roster.all { it.maxHp >= 320 })

        val protagonists = roster.filter { it.alignment == CharacterAlignment.PROTAGONIST }
        val antagonists = roster.filter { it.alignment == CharacterAlignment.ANTAGONIST }
        assertTrue("Must have >= 140 protagonists, got ${protagonists.size}", protagonists.size >= 140)
        assertTrue("Must have >= 70 antagonists, got ${antagonists.size}", antagonists.size >= 70)

        assertTrue(roster.any { it.popularityTier == AnimeTier.POPULAR })
        assertTrue(roster.any { it.popularityTier == AnimeTier.CULT })
        assertTrue(roster.any { it.popularityTier == AnimeTier.NICHE })

        // Verify all 18 passives and all 10 ultimates are represented across the roster
        assertEquals(PassiveArchetype.entries.size, roster.map { it.passiveArchetype }.toSet().size)
        assertEquals(UltimateArchetype.entries.size, roster.map { it.ultimateArchetype }.toSet().size)

        val bondrewd = CharacterRoster.getById("bondrewd_dawn")
        assertNotNull(bondrewd)
        assertEquals(CharacterAlignment.ANTAGONIST, bondrewd.alignment)
        assertEquals(AnimeTier.NICHE, bondrewd.popularityTier)
    }

    @Test
    fun testHighCostCharacterUpgradesScaleHealthPowerAndCooldown() {
        val baseChar = CharacterRoster.getById("kakarot_z")
        val zeroUpgrades = CharacterUpgradeState()
        assertEquals(2_500, zeroUpgrades.nextCostGold(UpgradeStatType.HEALTH))
        assertEquals(2_500, zeroUpgrades.nextCostGold(UpgradeStatType.POWER))
        assertEquals(2_500, zeroUpgrades.nextCostGold(UpgradeStatType.COOLDOWN))

        val highUpgrades = CharacterUpgradeState(
            healthLevel = 5,
            powerLevel = 6,
            cooldownLevel = 9
        )
        assertEquals(baseChar.maxHp + 5 * 35, highUpgrades.effectiveMaxHp(baseChar.maxHp))
        assertEquals(baseChar.baseDamage + 6 * 2, highUpgrades.effectiveBaseDamage(baseChar.baseDamage))
        assertTrue(highUpgrades.effectiveUltimateCooldown(baseChar.ultimateCooldownSeconds) < baseChar.ultimateCooldownSeconds)
        assertEquals(135_000, highUpgrades.nextCostGold(UpgradeStatType.COOLDOWN))

        val maxed = CharacterUpgradeState(healthLevel = 10, powerLevel = 10, cooldownLevel = 10)
        assertTrue(maxed.isMaxed(UpgradeStatType.HEALTH))
        assertTrue(maxed.isMaxed(UpgradeStatType.POWER))
        assertTrue(maxed.isMaxed(UpgradeStatType.COOLDOWN))
        assertEquals(0, maxed.nextCostGold(UpgradeStatType.HEALTH))

        val upgradedBall = CombatBall(
            id = "player",
            name = baseChar.name,
            position = Vector2(400f, 800f),
            velocity = Vector2.Zero,
            character = baseChar,
            upgrades = highUpgrades
        )
        assertEquals(highUpgrades.effectiveMaxHp(baseChar.maxHp).toFloat(), upgradedBall.maxHp, 0.01f)
        assertEquals(upgradedBall.maxHp, upgradedBall.currentHp, 0.01f)
        assertEquals(highUpgrades.effectiveBaseDamage(baseChar.baseDamage), upgradedBall.effectiveBaseDamage)
        assertEquals(
            highUpgrades.effectiveUltimateCooldown(baseChar.ultimateCooldownSeconds),
            upgradedBall.effectiveUltCooldownSec,
            0.01f
        )
    }

    @Test
    fun testSoundManagerSynthesizesWavAndTracksCollisionPassiveAndAwakeningSfx() {
        GameSfx.entries.forEach { sfx ->
            val wav = SoundManager.synthesizeSfxWav(sfx)
            assertTrue("WAV for $sfx must contain valid header + PCM data", wav.size > 500)
            assertEquals('R'.code.toByte(), wav[0])
            assertEquals('I'.code.toByte(), wav[1])
            assertEquals('F'.code.toByte(), wav[2])
            assertEquals('F'.code.toByte(), wav[3])
        }

        val soundManager = SoundManager(null)
        assertTrue(soundManager.isEnabled)
        soundManager.playWallBounce(combo = 3)
        soundManager.playBallClash(isCrit = false)
        soundManager.playBallClash(isCrit = true)
        soundManager.playPassiveTrigger()
        soundManager.playAwakening(isPlayer = true)
        soundManager.playAwakening(isPlayer = false)
        soundManager.playUpgrade()

        assertEquals(1, soundManager.getPlayCount(GameSfx.WALL_BOUNCE))
        assertEquals(1, soundManager.getPlayCount(GameSfx.BALL_CLASH))
        assertEquals(1, soundManager.getPlayCount(GameSfx.CRIT_CLASH))
        assertEquals(1, soundManager.getPlayCount(GameSfx.PASSIVE_TRIGGER))
        assertEquals(1, soundManager.getPlayCount(GameSfx.AWAKENING_PLAYER))
        assertEquals(1, soundManager.getPlayCount(GameSfx.AWAKENING_AI))
        assertEquals(1, soundManager.getPlayCount(GameSfx.UPGRADE_FORGE))

        // Mute disables new plays
        soundManager.isEnabled = false
        soundManager.playKnockout()
        assertEquals(0, soundManager.getPlayCount(GameSfx.KNOCKOUT_FINISH))
    }

    @Test
    fun testAllEighteenPassivesExecuteCleanlyInBattleSimulation() {
        var totalPassiveProcs = 0
        PassiveArchetype.entries.forEach { archetype ->
            val fighterA = CharacterRoster.characters.first { it.passiveArchetype == archetype }
            val fighterB = CharacterRoster.characters.last { it.id != fighterA.id }

            val pBall = CombatBall(
                id = "player",
                name = fighterA.name,
                position = Vector2(460f, 800f),
                velocity = Vector2(680f, 0f),
                character = fighterA
            )
            val oBall = CombatBall(
                id = "opponent",
                name = fighterB.name,
                position = Vector2(540f, 800f),
                velocity = Vector2(-680f, 0f),
                character = fighterB
            )

            val sim = BattleSimulation(
                playerBall = pBall,
                opponentBall = oBall,
                engineSettings = EngineSettings(),
                onCollisionImpact = { _, _, _, _ -> },
                onWallBounce = {},
                onKnockout = {},
                onPassiveTriggered = { _, _ -> totalPassiveProcs++ }
            )
            sim.configureArenaBounds(1000f, 1600f)
            sim.isRoundActive = true
            sim.isClashStarted = true
            pBall.position = Vector2(465f, 800f)
            oBall.position = Vector2(535f, 800f)
            pBall.velocity = Vector2(780f, 0f)
            oBall.velocity = Vector2(-780f, 0f)

            // Force low HP & 60% Ult scenario partway through so rage/execute/overcharge/regen passives also fire
            repeat(80) { step ->
                if (step == 35) {
                    pBall.currentHp = pBall.maxHp * 0.35f
                    oBall.currentHp = oBall.maxHp * 0.35f
                    pBall.ultimateGauge = 60f
                    pBall.position = Vector2(465f, 800f)
                    oBall.position = Vector2(535f, 800f)
                    pBall.velocity = Vector2(780f, 0f)
                    oBall.velocity = Vector2(-780f, 0f)
                }
                sim.update(0.016f)
            }
        }
        assertTrue("Expected passives to trigger across collision/wall/defense simulations, got $totalPassiveProcs", totalPassiveProcs >= 10)
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

        // Step AI tactical loop past HARD reaction delay so AI activates Ultimate automatically
        repeat(15) {
            sim.update(0.02f)
            sim.performAiTacticalStep(0.02f, BotDifficulty.HARD)
        }
        assertTrue("AI should activate its Ultimate once 100% charged", aiUltFired)
        assertTrue(oBall.isUltimateActive)

        // Prevent KO while testing full Ultimate expiration -> Cooldown transition
        for (step in 0 until 320) {
            pBall.currentHp = pBall.maxHp
            oBall.currentHp = oBall.maxHp
            sim.update(0.02f)
        }

        // Both ultimates must have finished and entered mandatory Cooldown
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
    fun testNonstopPerpetualRicochetLongerMatchUntilZeroHp() {
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
        while (sim.isRoundActive && stepsRun < 20000) {
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

        assertTrue("Longer match should last > 800 frames, got $stepsRun", stepsRun > 800)
        assertTrue("Expected continuous wall bounces, got $bounceCount", bounceCount >= 20)
        assertTrue("Expected repeated collisions, got $collisionCount", collisionCount >= 10)
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
        assertTrue(settings.sfxEnabled)

        val lightSettings = settings.copy(themeMode = AppThemeMode.LIGHT, sfxEnabled = false)
        assertEquals(AppThemeMode.LIGHT, lightSettings.themeMode)
        assertFalse(lightSettings.sfxEnabled)

        assertTrue(DarkAppPalette.isDark)
        assertFalse(LightAppPalette.isDark)
        assertNotEquals(DarkAppPalette.background, LightAppPalette.background)
        assertNotEquals(DarkAppPalette.stageSurface, LightAppPalette.stageSurface)
        assertNotEquals(DarkAppPalette.damageTextDefault, LightAppPalette.damageTextDefault)
    }

    @Test
    fun testGutsUltimateGrowsDragonSlayerAndTriggersSpecialEffects() {
        val guts = CharacterRoster.getById("guts_berserk")
        val rival = CharacterRoster.getById("griffith_hawk")

        val gutsBall = CombatBall(
            id = "player",
            name = guts.name,
            position = Vector2(460f, 800f),
            velocity = Vector2(520f, -420f),
            character = guts
        )
        val rivalBall = CombatBall(
            id = "opponent",
            name = rival.name,
            position = Vector2(600f, 500f),
            velocity = Vector2(-420f, 480f),
            character = rival
        )

        assertEquals(1.0f, gutsBall.weaponScale, 0.01f)
        assertEquals(1.0f, gutsBall.targetWeaponScale, 0.01f)

        val sim = BattleSimulation(
            playerBall = gutsBall,
            opponentBall = rivalBall,
            engineSettings = EngineSettings(renderMode = RenderMode.ULTRA),
            onCollisionImpact = { _, _, _, _ -> },
            onWallBounce = {},
            onKnockout = {}
        )
        sim.configureArenaBounds(1000f, 1600f)
        sim.isRoundActive = true
        sim.isClashStarted = true

        gutsBall.ultimateGauge = 100f
        assertTrue(sim.triggerUltimate(gutsBall))
        assertTrue(gutsBall.isUltimateActive)
        assertEquals(1.92f, gutsBall.targetWeaponScale, 0.01f)
        assertEquals(1.92f, gutsBall.weaponScale, 0.01f)
        assertTrue(
            "Activating Guts' Ultimate should spawn bespoke SLASH_CROSS VFX rings",
            sim.shockwaves.any { it.style == AbilityVfxStyle.SLASH_CROSS }
        )
    }

    @Test
    fun testRenderModeOptimizationBudgetsScaleCleanly() {
        assertEquals("Low", RenderMode.PIXELIZED.title)
        assertEquals("Medium", RenderMode.BALANCED.title)
        assertEquals("High", RenderMode.REALISTIC.title)
        assertEquals("Ultra", RenderMode.ULTRA.title)

        assertEquals(0, RenderMode.PIXELIZED.maxTrailNodes)
        assertFalse(RenderMode.PIXELIZED.ambientAuraFx)
        assertFalse(RenderMode.PIXELIZED.bloomGlow)

        assertTrue(RenderMode.BALANCED.maxParticles < RenderMode.REALISTIC.maxParticles)
        assertTrue(RenderMode.REALISTIC.maxParticles < RenderMode.ULTRA.maxParticles)
        assertTrue(RenderMode.ULTRA.bloomGlow)
    }

    @Test
    fun testGameModesExcludeFreePlayAndSupportCustomMultiplayerAndOnlineRanked() {
        val modeNames = GameMode.entries.map { it.name }.toSet()
        assertFalse("Free Play (FRIENDS_LOBBY) must be removed", modeNames.contains("FRIENDS_LOBBY"))
        assertFalse("No GameMode title may be Free Play", GameMode.entries.any { it.title.equals("Free Play", ignoreCase = true) })
        assertTrue(modeNames.contains("PRACTICE_BOT"))
        assertTrue(modeNames.contains("CUSTOM_MATCH"))
        assertTrue(modeNames.contains("MULTIPLAYER"))
        assertTrue(modeNames.contains("ONLINE_MATCH"))
    }

    @Test
    fun testNormalIsHardAndHardIsExtremeAiDifficulty() {
        assertEquals("Hard", BotDifficulty.MEDIUM.shortLabel)
        assertEquals("Extreme", BotDifficulty.HARD.shortLabel)

        assertTrue(BotDifficulty.MEDIUM.aimAccuracy >= 0.94f)
        assertTrue(BotDifficulty.HARD.aimAccuracy >= 0.99f)
        assertTrue(BotDifficulty.MEDIUM.speedFactor >= 1.14f)
        assertTrue(BotDifficulty.HARD.speedFactor >= 1.28f)
        assertTrue(BotDifficulty.HARD.redirectCooldownSec < BotDifficulty.MEDIUM.redirectCooldownSec)
        assertTrue(BotDifficulty.MEDIUM.redirectCooldownSec < BotDifficulty.EASY.redirectCooldownSec)
    }

    @Test
    fun testRemoteMultiplayerCommandMirroringAndCloudUpgradePacking() {
        val pBall = CombatBall(
            id = "player",
            name = "Host",
            position = Vector2(500f, 1100f),
            velocity = Vector2.Zero,
            character = CharacterRoster.getById("kakarot_z")
        )
        val oBall = CombatBall(
            id = "opponent",
            name = "Friend",
            position = Vector2(500f, 350f),
            velocity = Vector2.Zero,
            character = CharacterRoster.getById("satoru_void")
        )
        val sim = BattleSimulation(
            playerBall = pBall,
            opponentBall = oBall,
            engineSettings = EngineSettings(),
            isRemoteMultiplayer = true,
            onCollisionImpact = { _, _, _, _ -> },
            onWallBounce = {},
            onKnockout = {}
        )
        sim.configureArenaBounds(1000f, 1600f)
        sim.applyRemoteOpponentCommand(Vector2(0.35f, 0.93f), isOpening = true)
        assertTrue(sim.isClashStarted)
        assertTrue(sim.opponentBall.velocity.length() > 300f)

        sim.applyMirroredLanSync(
            localNormX = 0.52f,
            localNormY = 0.72f,
            localNormVx = 0.30f,
            localNormVy = -0.80f,
            remoteNormX = 0.48f,
            remoteNormY = 0.28f,
            remoteNormVx = -0.30f,
            remoteNormVy = 0.80f,
            localHp = 310f,
            remoteHp = 295f,
            localUlt = 44f,
            remoteUlt = 58f
        )
        assertEquals(310f, sim.playerBall.currentHp, 0.1f)
        assertEquals(295f, sim.opponentBall.currentHp, 0.1f)

        val code = OnlineArenaRepository.generateRoomCode()
        assertEquals(6, code.length)

        val originalUpgrades = mapOf(
            "guts_berserk" to CharacterUpgradeState(healthLevel = 4, powerLevel = 7, cooldownLevel = 3),
            "kakarot_z" to CharacterUpgradeState(healthLevel = 2, powerLevel = 2, cooldownLevel = 5)
        )
        val packed = OnlineArenaRepository.packUpgrades(originalUpgrades)
        val unpacked = OnlineArenaRepository.unpackUpgrades(packed)
        assertEquals(originalUpgrades, unpacked)
    }
}
