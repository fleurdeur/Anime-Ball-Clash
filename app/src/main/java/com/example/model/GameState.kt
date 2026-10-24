package com.example.model

enum class AppScreen {
    LOADING,
    HOME_HUB,
    CHARACTER_SELECT,
    CUSTOM_MATCH,
    MULTIPLAYER_LOBBY,
    MATCHMAKING,
    BATTLE_ARENA,
    RESULT,
    SHOP,
    QUESTS,
    LEADERBOARD
}

enum class GameMode(val title: String, val tagLabel: String, val subtitle: String) {
    PRACTICE_BOT("Practice", "VS Bot", "Fight nonstop bounce battles against Easy, Normal (Hard), or Hard (Extreme) bots."),
    CUSTOM_MATCH("Custom", "Pick Rival", "Pick both your fighter and any of the 224 anime rivals to duel."),
    MULTIPLAYER("VS Friend", "LAN & Room", "Battle friends over local Wi-Fi (LAN) or anywhere online using a Room Code."),
    ONLINE_MATCH("Ranked", "Online", "Play online 1v1 ranked clashes to earn Rank Points and climb the global ladder.")
}

enum class BotDifficulty(
    val title: String,
    val shortLabel: String,
    val aimAccuracy: Float,
    val speedFactor: Float,
    val redirectCooldownSec: Float
) {
    EASY("Easy", "Easy", 0.74f, 0.92f, 4.80f),
    MEDIUM("Normal", "Hard", 0.95f, 1.15f, 2.45f),
    HARD("Hard", "Extreme", 0.995f, 1.30f, 1.68f)
}

enum class TurnState {
    AIM_OPENING,
    NONSTOP_CLASH,
    KNOCKOUT_SLOWMO
}

data class PlayerProfile(
    val username: String = "Player",
    val level: Int = 1,
    val exp: Int = 0,
    val maxExp: Int = 200,
    val gold: Int = 0,
    val gems: Int = 0,
    val selectedCharacterId: String = "kakarot_z",
    val rankedRating: Int = 0,
    val rankTier: String = "Unranked",
    val wins: Int = 0,
    val losses: Int = 0
) {
    val totalMatches: Int get() = wins + losses
    val winRatePercent: Int
        get() = if (totalMatches == 0) 0 else ((wins * 100f) / totalMatches).toInt()

    companion object {
        fun computeTier(rating: Int, totalMatches: Int): String {
            if (totalMatches == 0 && rating == 0) return "Unranked"
            return when {
                rating >= 1600 -> "Master"
                rating >= 1100 -> "Diamond"
                rating >= 700 -> "Platinum"
                rating >= 400 -> "Gold"
                rating >= 150 -> "Silver"
                else -> "Bronze"
            }
        }
    }
}

data class QuestItem(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int = 0,
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
            id = "q_first_match",
            title = "First Fight",
            description = "Finish 1 arena match in any mode.",
            currentProgress = 0,
            targetProgress = 1,
            rewardGold = 1_800,
            rewardGems = 15,
            isClaimed = false
        ),
        QuestItem(
            id = "q_upgrade_forge",
            title = "First Upgrade",
            description = "Upgrade Health, Power, or Faster Ult 1 time.",
            currentProgress = 0,
            targetProgress = 1,
            rewardGold = 2_500,
            rewardGems = 20,
            isClaimed = false
        ),
        QuestItem(
            id = "q_wall_ricochet",
            title = "Wall Bouncer",
            description = "Bounce off arena walls 40 times.",
            currentProgress = 0,
            targetProgress = 40,
            rewardGold = 3_800,
            rewardGems = 25,
            isClaimed = false
        ),
        QuestItem(
            id = "q_awakenings",
            title = "Ultimate Power",
            description = "Use your Ultimate attack 3 times.",
            currentProgress = 0,
            targetProgress = 3,
            rewardGold = 5_200,
            rewardGems = 35,
            isClaimed = false
        ),
        QuestItem(
            id = "q_clean_victory",
            title = "Strong Win",
            description = "Win a match with 120 HP or more left.",
            currentProgress = 0,
            targetProgress = 1,
            rewardGold = 6_800,
            rewardGems = 45,
            isClaimed = false
        ),
        QuestItem(
            id = "q_three_wins",
            title = "Three Wins",
            description = "Win 3 matches in any mode.",
            currentProgress = 0,
            targetProgress = 3,
            rewardGold = 9_500,
            rewardGems = 60,
            isClaimed = false
        )
    )

    fun buildLeaderboard(
        profile: PlayerProfile,
        onlineEntries: List<LeaderboardEntry> = emptyList()
    ): List<LeaderboardEntry> {
        val playerEntry = LeaderboardEntry(
            rank = 0,
            name = profile.username,
            characterId = profile.selectedCharacterId,
            rating = profile.rankedRating,
            tier = PlayerProfile.computeTier(profile.rankedRating, profile.totalMatches),
            winRate = "${profile.winRatePercent}%",
            isPlayer = true
        )
        val merged = if (onlineEntries.any { it.isPlayer }) {
            onlineEntries
        } else {
            onlineEntries + playerEntry
        }
        return merged
            .sortedWith(compareByDescending<LeaderboardEntry> { it.rating }.thenByDescending { it.isPlayer })
            .mapIndexed { idx, entry -> entry.copy(rank = idx + 1) }
    }

    val combatTips = listOf(
        "Balls bounce nonstop until one fighter hits 0 HP.",
        "Every fighter has a special skill and a strong Ultimate attack.",
        "Use Gold in the Shop to upgrade Health, Power, and Faster Ult.",
        "Drag on the arena when Dash is ready to change direction."
    )
}
