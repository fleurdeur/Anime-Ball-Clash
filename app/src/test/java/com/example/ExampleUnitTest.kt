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

    @Test
    fun testAllEightCounterArchetypesAndThematicImmunitiesWork() {
        val roster = CharacterRoster.characters
        assertEquals(
            "All 9 CounterArchetypes (including TELEPORT_STRIKE) must be represented across the 224-character roster",
            com.example.model.CounterArchetype.entries.size,
            roster.map { it.counterArchetype }.toSet().size
        )

        // Verify Time Immunity cleanses Time Stop stun immediately
        val jotaro = CharacterRoster.getById("kujo_star")
        assertEquals(com.example.model.CounterArchetype.TIME_IMMUNITY, jotaro.counterArchetype)
        assertTrue(jotaro.hasStandSpirit)
        assertEquals(com.example.model.JoJoStandStyle.STAR_PLATINUM, jotaro.joJoStandStyle)

        val dio = CharacterRoster.getById("dio_world")
        assertEquals(com.example.model.JoJoStandStyle.THE_WORLD, dio.joJoStandStyle)

        val asta = CharacterRoster.getById("asta_grimoire")
        assertEquals(com.example.model.CounterArchetype.ANTI_MAGIC_NULL, asta.counterArchetype)

        val pBall = CombatBall(
            id = "player",
            name = jotaro.name,
            position = Vector2(480f, 800f),
            velocity = Vector2.Zero,
            character = jotaro
        )
        val oBall = CombatBall(
            id = "opponent",
            name = dio.name,
            position = Vector2(520f, 800f),
            velocity = Vector2(-600f, 0f),
            character = dio
        )
        var counterFired = false
        val sim = BattleSimulation(
            playerBall = pBall,
            opponentBall = oBall,
            engineSettings = EngineSettings(),
            onCollisionImpact = { _, _, _, _ -> },
            onWallBounce = {},
            onKnockout = {},
            onCounterActivated = { isPlayer, _ -> if (isPlayer) counterFired = true }
        )
        sim.configureArenaBounds(1000f, 1600f)
        sim.isRoundActive = true
        sim.isClashStarted = true

        // Simulate rival freezing player with Time Stop, then player activating TIME_IMMUNITY counter
        pBall.stunTimeRemaining = 1.6f
        assertTrue(pBall.isStunned)
        assertTrue(sim.triggerCounter(pBall))
        assertTrue(counterFired)
        assertFalse("Time Immunity counter must immediately break Time Stop stun", pBall.isStunned)
        assertTrue(pBall.isCounterActive)

        // Colliding while Counter is active deals zero damage to defender
        val hpBefore = pBall.currentHp
        pBall.position = Vector2(485f, 800f)
        oBall.position = Vector2(515f, 800f)
        oBall.velocity = Vector2(-780f, 0f)
        sim.update(0.016f)
        assertEquals("Counter must block incoming collision damage", hpBefore, pBall.currentHp, 0.01f)
    }

    @Test
    fun testUltimateModeUnlocksRapidMultiDash() {
        val fighter = CharacterRoster.getById("kakarot_z")
        val pBall = CombatBall(
            id = "player",
            name = fighter.name,
            position = Vector2(500f, 1000f),
            velocity = Vector2(0f, -500f),
            character = fighter
        )
        val oBall = CombatBall(
            id = "opponent",
            name = "Rival",
            position = Vector2(500f, 300f),
            velocity = Vector2(0f, 500f),
            character = CharacterRoster.getById("kamado_sun")
        )
        val sim = BattleSimulation(
            playerBall = pBall,
            opponentBall = oBall,
            engineSettings = EngineSettings(),
            onCollisionImpact = { _, _, _, _ -> },
            onWallBounce = {},
            onKnockout = {}
        )
        sim.configureArenaBounds(1000f, 1600f)
        sim.startNonstopClash(Vector2(0.3f, -0.95f), BotDifficulty.EASY)

        // Normal dash cooldown is ~3.2s; Ultimate dash cooldown drops to 0.32s for Multi-Dash!
        val normalCd = pBall.effectiveRedirectCooldownSec
        assertTrue(normalCd >= 2.0f)

        pBall.ultimateGauge = 100f
        assertTrue(sim.triggerUltimate(pBall))
        assertTrue(pBall.isUltimateActive)
        assertEquals(0.32f, pBall.effectiveRedirectCooldownSec, 0.01f)

        // Execute multiple rapid dashes while Ultimate is active
        sim.playerRedirectCooldown = 0f
        assertTrue(sim.redirectPlayerBall(Vector2(0.6f, -0.8f)))
        assertEquals(0.32f, sim.playerRedirectCooldown, 0.02f)

        repeat(22) { sim.update(0.016f) } // ~0.35s elapsed
        assertTrue("Multi-dash should be ready again after ~0.35s in Ultimate mode", sim.playerRedirectCooldown <= 0f)
        assertTrue(sim.redirectPlayerBall(Vector2(-0.6f, -0.8f)))
    }

    @Test
    fun testAiEasyModeIsTrulyBeginnerFriendly() {
        assertEquals("Easy", BotDifficulty.EASY.title)
        assertTrue("Easy AI accuracy must be gentle (< 0.55)", BotDifficulty.EASY.aimAccuracy <= 0.50f)
        assertTrue("Easy AI speed factor must be slower (< 0.85)", BotDifficulty.EASY.speedFactor <= 0.80f)
        assertTrue("Easy AI dash wait must be long (>= 6.0s)", BotDifficulty.EASY.redirectCooldownSec >= 6.0f)
    }

    @Test
    fun testZeroDesyncAuthoritativeSyncPackingAndFinish() {
        val hostPlayer = CombatBall(
            id = "player",
            name = "Host",
            position = Vector2(520f, 1120f),
            velocity = Vector2(320f, -640f),
            character = CharacterRoster.getById("kakarot_z")
        )
        val hostOpponent = CombatBall(
            id = "opponent",
            name = "Guest",
            position = Vector2(480f, 420f),
            velocity = Vector2(-280f, 610f),
            character = CharacterRoster.getById("satoru_void")
        )
        val hostSim = BattleSimulation(
            playerBall = hostPlayer,
            opponentBall = hostOpponent,
            engineSettings = EngineSettings(),
            isRemoteMultiplayer = true,
            isGuestClient = false,
            onCollisionImpact = { _, _, _, _ -> },
            onWallBounce = {},
            onKnockout = {}
        )
        hostSim.configureArenaBounds(1000f, 1600f)
        hostSim.isClashStarted = true
        hostPlayer.currentHp = 295f
        hostOpponent.currentHp = 240f
        hostPlayer.ultimateGauge = 64f
        hostOpponent.ultimateGauge = 78f

        val packed = hostSim.packHostSyncState()
        assertTrue(packed.isNotBlank())

        var guestWinnerResult: Boolean? = null
        val guestSim = BattleSimulation(
            playerBall = CombatBall("player", "Guest", Vector2.Zero, Vector2.Zero, character = CharacterRoster.getById("satoru_void")),
            opponentBall = CombatBall("opponent", "Host", Vector2.Zero, Vector2.Zero, character = CharacterRoster.getById("kakarot_z")),
            engineSettings = EngineSettings(),
            isRemoteMultiplayer = true,
            isGuestClient = true,
            onCollisionImpact = { _, _, _, _ -> },
            onWallBounce = {},
            onKnockout = { isWinner -> guestWinnerResult = isWinner }
        )
        guestSim.configureArenaBounds(1000f, 1600f)
        guestSim.applyPackedHostSyncOnGuest(packed)

        // Guest local HP must match Host's opponent HP (240), and Guest opponent HP must match Host's player HP (295)
        assertEquals(240f, guestSim.playerBall.currentHp, 0.5f)
        assertEquals(295f, guestSim.opponentBall.currentHp, 0.5f)
        assertEquals(78f, guestSim.playerBall.ultimateGauge, 1.0f)
        assertEquals(64f, guestSim.opponentBall.ultimateGauge, 1.0f)

        // Authoritative finish guarantees identical match winner on Guest
        guestSim.forceAuthoritativeFinish(isPlayerWinner = false, finalPlayerHp = 0f, finalOpponentHp = 180f)
        assertFalse(guestSim.isRoundActive)
        assertEquals(false, guestWinnerResult)
        assertEquals(0f, guestSim.playerBall.currentHp, 0.01f)
        assertEquals(180f, guestSim.opponentBall.currentHp, 0.01f)
    }

    @Test
    fun testDailyQuestsAndSituationalBattleAudioDirector() {
        val dailies = InitialGameData.buildDailyQuests(20385L)
        assertEquals(4, dailies.size)
        assertTrue(dailies.all { it.isDaily && !it.isClaimed && it.currentProgress == 0 })

        // Verify all 4 situational BGM WAV tracks synthesize valid RIFF headers
        val situations = listOf(
            com.example.audio.BattleBgmSituation.OPENING_TENSION,
            com.example.audio.BattleBgmSituation.NORMAL_CLASH,
            com.example.audio.BattleBgmSituation.ULTIMATE_SURGE,
            com.example.audio.BattleBgmSituation.CLUTCH_DANGER
        )
        for (sit in situations) {
            val wav = com.example.audio.BattleAudioDirector.synthesizeBgmTrackWav(sit)
            assertTrue("BGM WAV for $sit must be > 4KB", wav.size > 4096)
            assertEquals('R'.code.toByte(), wav[0])
            assertEquals('I'.code.toByte(), wav[1])
            assertEquals('F'.code.toByte(), wav[2])
            assertEquals('F'.code.toByte(), wav[3])
        }

        val director = com.example.audio.BattleAudioDirector(null)
        director.updateBattleSituation(inBattleArena = true, isClashStarted = false, eitherUltimateActive = false, lowestHpRatio = 1.0f)
        assertEquals(com.example.audio.BattleBgmSituation.OPENING_TENSION, director.activeSituation)

        director.updateBattleSituation(inBattleArena = true, isClashStarted = true, eitherUltimateActive = false, lowestHpRatio = 0.85f)
        assertEquals(com.example.audio.BattleBgmSituation.NORMAL_CLASH, director.activeSituation)

        director.updateBattleSituation(inBattleArena = true, isClashStarted = true, eitherUltimateActive = true, lowestHpRatio = 0.85f)
        assertEquals(com.example.audio.BattleBgmSituation.ULTIMATE_SURGE, director.activeSituation)

        director.updateBattleSituation(inBattleArena = true, isClashStarted = true, eitherUltimateActive = false, lowestHpRatio = 0.22f)
        assertEquals(com.example.audio.BattleBgmSituation.CLUTCH_DANGER, director.activeSituation)

        val dio = CharacterRoster.getById("dio_world")
        director.speakUltimateVoiceLine(dio, isPlayer = true)
        assertEquals(1, director.spokenVoiceCount)
        assertTrue(director.lastSpokenLine.contains("ZA WARUDO", ignoreCase = true) || director.lastSpokenLine.isNotBlank())

        director.stopBattleMusic()
        assertEquals(com.example.audio.BattleBgmSituation.NONE, director.activeSituation)
        director.release()
    }

    @Test
    fun testCanonProjectilesGilgameshSwordsGojoHollowPurpleMinatoTeleportAndLoreBalance() {
        // 1. LorePowerBalancer balances Street/Tactical fighters against Cosmic fighters while keeping both OP
        val tanjiro = CharacterRoster.getById("kamado_sun")
        val goku = CharacterRoster.getById("kakarot_z")
        val tanjiroVsGoku = com.example.model.LorePowerBalancer.computeMatchup(tanjiro, goku)
        val gokuVsTanjiro = com.example.model.LorePowerBalancer.computeMatchup(goku, tanjiro)
        assertTrue("Underdog world tier receives tactical precision & ult charge boost", tanjiroVsGoku.critBonusMultiplier > 1.0f && tanjiroVsGoku.ultChargeMultiplier > 1.0f)
        assertTrue("Cosmic Saiyan retains boss-scale HP & Zenkai surge", gokuVsTanjiro.hpMultiplier >= 1.0f && gokuVsTanjiro.lowHpZenkaiScale > 0f)
        assertTrue(tanjiroVsGoku.simpleMatchupNote.isNotBlank())

        // 2. Gilgamesh Ultimate spawns multiple visible Gate of Babylon swords that hover, shoot at rival, deal damage & fade out
        val gilgamesh = CharacterRoster.getById("gilgamesh_babylon")
        assertEquals(com.example.model.CanonProjectileStyle.BABYLON_SWORD_RAIN, gilgamesh.canonProjectileStyle)
        val gilBall = CombatBall("player", gilgamesh.name, Vector2(260f, 800f), Vector2(200f, 0f), character = gilgamesh)
        val rivalBall = CombatBall("opponent", tanjiro.name, Vector2(740f, 800f), Vector2.Zero, character = tanjiro)
        val gilSim = BattleSimulation(
            playerBall = gilBall,
            opponentBall = rivalBall,
            engineSettings = EngineSettings(),
            onCollisionImpact = { _, _, _, _ -> },
            onWallBounce = {},
            onKnockout = {}
        )
        gilSim.configureArenaBounds(1000f, 1600f)
        gilSim.isRoundActive = true
        gilSim.isClashStarted = true
        gilBall.ultimateGauge = 100f
        // Prevent auto-counter during damage check
        rivalBall.counterCooldownRemaining = 10f
        val rivalHpBeforeSwords = rivalBall.currentHp
        assertTrue(gilSim.triggerUltimate(gilBall))
        assertTrue(
            "Gilgamesh Ultimate must materialize multiple visible swords (>= 6)",
            gilSim.projectiles.count { it.style == com.example.model.CanonProjectileStyle.BABYLON_SWORD_RAIN } >= 6
        )
        // Step frames so swords finish hovering, fly across the arena, hit rival, and begin fading out
        var sawFadingSword = false
        repeat(80) {
            gilSim.positionLockForTest()
            rivalBall.counterCooldownRemaining = 10f
            gilSim.update(0.016f)
            if (gilSim.projectiles.any { it.isFadingOut && it.currentAlpha < 1f }) {
                sawFadingSword = true
            }
        }
        assertTrue("Gilgamesh's launched swords must deal real damage to opposing character", rivalBall.currentHp < rivalHpBeforeSwords)
        assertTrue("Projectiles must smoothly fade out after hitting", sawFadingSword || gilSim.projectiles.isEmpty())

        // 3. Gojo Ultimate shoots Hollow Purple toward opposing character, deals heavy damage, and smoothly fades out
        val gojo = CharacterRoster.getById("satoru_void")
        assertEquals(com.example.model.CanonProjectileStyle.HOLLOW_PURPLE_ORB, gojo.canonProjectileStyle)
        val gojoBall = CombatBall("player", gojo.name, Vector2(240f, 800f), Vector2.Zero, character = gojo)
        val sukunaBall = CombatBall("opponent", "Sukuna", Vector2(720f, 800f), Vector2.Zero, character = CharacterRoster.getById("ryomen_shrine"))
        val gojoSim = BattleSimulation(
            playerBall = gojoBall,
            opponentBall = sukunaBall,
            engineSettings = EngineSettings(),
            onCollisionImpact = { _, _, _, _ -> },
            onWallBounce = {},
            onKnockout = {}
        )
        gojoSim.configureArenaBounds(1000f, 1600f)
        gojoSim.isRoundActive = true
        gojoSim.isClashStarted = true
        gojoBall.ultimateGauge = 100f
        sukunaBall.counterCooldownRemaining = 10f
        val sukunaHpBefore = sukunaBall.currentHp
        assertTrue(gojoSim.triggerUltimate(gojoBall))
        val purpleOrb = gojoSim.projectiles.firstOrNull { it.style == com.example.model.CanonProjectileStyle.HOLLOW_PURPLE_ORB }
        assertTrue("Gojo must spawn Hollow Purple orb projectile", purpleOrb != null)

        var sawPurpleFade = false
        repeat(95) {
            gojoBall.position = Vector2(200f, 300f)
            gojoBall.velocity = Vector2.Zero
            sukunaBall.position = Vector2(720f, 800f)
            sukunaBall.velocity = Vector2.Zero
            sukunaBall.counterCooldownRemaining = 10f
            gojoSim.update(0.016f)
            if (gojoSim.projectiles.any { it.style == com.example.model.CanonProjectileStyle.HOLLOW_PURPLE_ORB && it.isFadingOut }) {
                sawPurpleFade = true
            }
        }
        assertTrue("Hollow Purple must hit opposing character for damage", sukunaBall.currentHp < sukunaHpBefore)
        assertTrue("Hollow Purple must smoothly fade out after shooting across", sawPurpleFade)

        // 4. Minato has TELEPORT_STRIKE and automatic chance-based counter teleports him behind the attacker!
        val minato = CharacterRoster.getById("minato_flash")
        assertEquals(com.example.model.CounterArchetype.TELEPORT_STRIKE, minato.counterArchetype)
        assertEquals(com.example.model.CanonProjectileStyle.FLYING_RAIJIN_KUNAI, minato.canonProjectileStyle)

        var autoCounterCount = 0
        val minatoBall = CombatBall("player", minato.name, Vector2(500f, 800f), Vector2.Zero, character = minato)
        val attackerBall = CombatBall("opponent", goku.name, Vector2(360f, 800f), Vector2(760f, 0f), character = goku)
        val minatoSim = BattleSimulation(
            playerBall = minatoBall,
            opponentBall = attackerBall,
            engineSettings = EngineSettings(),
            onCollisionImpact = { _, _, _, _ -> },
            onWallBounce = {},
            onKnockout = {},
            onCounterActivated = { isPlayer, _ -> if (isPlayer) autoCounterCount++ }
        )
        minatoSim.configureArenaBounds(1000f, 1600f)
        minatoSim.isRoundActive = true
        minatoSim.isClashStarted = true

        // Run repeated attack impacts with cooldown reset to verify probabilistic auto-counter triggers & teleports behind attacker
        var teleportedBehind = false
        for (attempt in 0 until 24) {
            minatoBall.currentHp = minatoBall.maxHp
            attackerBall.currentHp = attackerBall.maxHp
            minatoBall.counterCooldownRemaining = 0f
            minatoBall.counterActiveTimer = 0f
            // Attacker charges at high speed from left (x=455 -> +x) into Minato (x=515); behind attacker is x < attacker.x!
            attackerBall.speedBoostMultiplier = 1.45f
            attackerBall.speedBoostTimer = 1.0f
            attackerBall.position = Vector2(455f, 800f)
            attackerBall.velocity = Vector2(1480f, 0f)
            minatoBall.position = Vector2(515f, 800f)
            minatoBall.velocity = Vector2(-120f, 0f)
            val prevCount = autoCounterCount
            minatoSim.update(0.016f)
            if (autoCounterCount > prevCount && minatoBall.position.x < attackerBall.position.x) {
                teleportedBehind = true
                break
            }
            // Advance 0.25s in safe separation so the 0.22s collisionCooldown clears before next impact test
            attackerBall.position = Vector2(220f, 400f)
            attackerBall.stunTimeRemaining = 0.3f
            minatoBall.position = Vector2(780f, 1200f)
            minatoBall.stunTimeRemaining = 0.3f
            repeat(16) { minatoSim.update(0.016f) }
            attackerBall.stunTimeRemaining = 0f
            minatoBall.stunTimeRemaining = 0f
        }
        assertTrue("Automatic chance-based counter must trigger probabilistically on incoming attacks", autoCounterCount > 0)
        assertTrue("Minato TELEPORT_STRIKE counter must teleport him behind the opposing character", teleportedBehind)
    }

    private fun BattleSimulation.positionLockForTest() {
        playerBall.position = Vector2(220f, 800f)
        playerBall.velocity = Vector2.Zero
        playerBall.stunTimeRemaining = 1f
        opponentBall.position = Vector2(740f, 800f)
        opponentBall.velocity = Vector2.Zero
        opponentBall.stunTimeRemaining = 1f
    }
}
