package com.example.model

enum class AppScreen {
    LOADING,
    HOME_HUB,
    CHARACTER_SELECT,
    MATCHMAKING,
    BATTLE_ARENA,
    RESULT,
    SHOP,
    QUESTS,
    LEADERBOARD
}

enum class GameMode(val title: String, val tagLabel: String, val subtitle: String) {
    PRACTICE_BOT("VS AI BOT", "SOLO ARENA", "Adaptive tactical AI with reactive bank-shot physics"),
    ONLINE_MATCH("RANKED CLASH", "COMPETITIVE", "Simulated ranked ladder duel with live MMR stakes"),
    FRIENDS_LOBBY("CUSTOM DUEL", "FREE PLAY", "High-velocity 1v1 exhibition with custom arena rules")
}

enum class BotDifficulty(val title: String, val shortLabel: String, val aimAccuracy: Float, val power: Float, val bankShotChance: Float) {
    EASY("Rookie AI", "ROOKIE", 0.72f, 0.78f, 0.15f),
    MEDIUM("Challenger AI", "CHALLENGER", 0.90f, 0.96f, 0.35f),
    HARD("Overlord AI", "OVERLORD", 0.98f, 1.12f, 0.55f)
}

enum class TurnState {
    PLAYER_AIMING,
    PLAYER_MOVING,
    AI_AIMING,
    AI_MOVING,
    KNOCKOUT_SLOWMO
}

data class PlayerProfile(
    val username: String = "ShadowSlayer99",
    val level: Int = 42,
    val exp: Int = 3450,
    val maxExp: Int = 5000,
    val gold: Int = 14250,
    val gems: Int = 320,
    val selectedCharacterId: String = "saiyan_spirit",
    val rankedRating: Int = 1840,
    val rankTier: String = "DIAMOND IV",
    val wins: Int = 89,
    val losses: Int = 23
)

data class QuestItem(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val rewardGold: Int,
    val rewardGems: Int,
    val isClaimed: Boolean = false
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val characterId: String,
    val rating: Int,
    val tier: String,
    val winRate: String,
    val isPlayer: Boolean = false
)

data class MatchStatistics(
    val isPlayerWinner: Boolean,
    val playerCharacterId: String,
    val opponentCharacterId: String,
    val playerCharacterName: String,
    val opponentCharacterName: String,
    val playerFinalHp: Int,
    val opponentFinalHp: Int,
    val damageDealt: Int,
    val damageTaken: Int,
    val maxCombo: Int,
    val wallBounces: Int,
    val ultimatesUsed: Int,
    val matchDurationSeconds: Int,
    val goldEarned: Int,
    val gemsEarned: Int,
    val expEarned: Int
)

object InitialGameData {
    val defaultQuests = listOf(
        QuestItem(
            id = "q1",
            title = "First Clash of the Day",
            description = "Complete 1 arena battle in any mode.",
            currentProgress = 1,
            targetProgress = 1,
            rewardGold = 250,
            rewardGems = 10,
            isClaimed = false
        ),
        QuestItem(
            id = "q2",
            title = "Ricochet Specialist",
            description = "Trigger 15 high-speed wall rebounds across battles.",
            currentProgress = 12,
            targetProgress = 15,
            rewardGold = 500,
            rewardGems = 20,
            isClaimed = false
        ),
        QuestItem(
            id = "q3",
            title = "Limit Breaker",
            description = "Unleash your Timed Ultimate Awakening 3 times.",
            currentProgress = 2,
            targetProgress = 3,
            rewardGold = 600,
            rewardGems = 25,
            isClaimed = false
        ),
        QuestItem(
            id = "q4",
            title = "Flawless Dominator",
            description = "Win a match with at least 40 HP remaining.",
            currentProgress = 0,
            targetProgress = 1,
            rewardGold = 1000,
            rewardGems = 50,
            isClaimed = false
        )
    )

    val leaderboardEntries = listOf(
        LeaderboardEntry(1, "VortexRider", "saiyan_spirit", 2420, "GRANDMASTER", "84%"),
        LeaderboardEntry(2, "DemonBlade_X", "demon_slash", 2380, "GRANDMASTER", "81%"),
        LeaderboardEntry(3, "GojoSensei_00", "six_glow_magic", 2310, "MASTER I", "79%"),
        LeaderboardEntry(4, "ThunderKakashi", "lightning_ninja", 2250, "MASTER I", "77%"),
        LeaderboardEntry(5, "BaldCapeHero", "one_punch", 2180, "MASTER II", "76%"),
        LeaderboardEntry(6, "ShadowSlayer99", "saiyan_spirit", 1840, "DIAMOND IV", "79%", isPlayer = true),
        LeaderboardEntry(7, "NatsuFlame", "flame_dragon", 1810, "DIAMOND IV", "71%"),
        LeaderboardEntry(8, "TitanShifter", "titan_buster", 1750, "DIAMOND V", "68%"),
        LeaderboardEntry(9, "MugiwaraGear", "rubber_stretch", 1720, "PLATINUM I", "65%"),
        LeaderboardEntry(10, "AuraKnight", "saiyan_spirit", 1680, "PLATINUM II", "62%")
    )

    val combatTips = listOf(
        "Bank shots off arena walls add +2 bonus impact damage per bounce before hitting your rival!",
        "Saiyan Spirit's Ki Shockwave adds +40% impulse force, slamming enemies into corner hazards.",
        "Demon Slash inflicts a 4-tick Bleed DoT that drains enemy HP even after disengagement.",
        "Six-Glow Magic's Unlimited Void freezes the rival sphere in space, granting a free follow-up shot!",
        "Lightning Ninja gains +10% velocity on every wall rebound while Lightning Speed is active.",
        "Drag anywhere on the arena floor to aim — you don't need to cover your sphere with your thumb!"
    )
}
