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
    PRACTICE_BOT("Practice", "VS Bot", "Fight a computer bot on Easy, Hard, or Extreme."),
    CUSTOM_MATCH("Custom", "Pick Both", "Pick your fighter and choose who you want to fight."),
    MULTIPLAYER("VS Friend", "Wi-Fi & Code", "Fight friends on Wi-Fi or anywhere with a Room Code."),
    ONLINE_MATCH("Ranked", "Online", "Play real online fights to win Rank Points.")
}

enum class BotDifficulty(
    val title: String,
    val shortLabel: String,
    val aimAccuracy: Float,
    val speedFactor: Float,
    val redirectCooldownSec: Float
) {
    EASY("Easy", "Easy", 0.42f, 0.72f, 6.80f),
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
    val isClaimed: Boolean = false,
    val isDaily: Boolean = false
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

    fun buildDailyQuests(epochDay: Long): List<QuestItem> {
        val variant = (epochDay % 3L).toInt()
        val matchTarget = when (variant) {
            0 -> 2
            1 -> 3
            else -> 2
        }
        val counterTarget = when (variant) {
            0 -> 3
            1 -> 4
            else -> 2
        }
        return listOf(
            QuestItem(
                id = "dq_play_$epochDay",
                title = "Daily Warmup",
                description = "Play $matchTarget fights today.",
                currentProgress = 0,
                targetProgress = matchTarget,
                rewardGold = 1_800,
                rewardGems = 8,
                isClaimed = false,
                isDaily = true
            ),
            QuestItem(
                id = "dq_win_$epochDay",
                title = "Daily Winner",
                description = "Win 1 fight today in any mode.",
                currentProgress = 0,
                targetProgress = 1,
                rewardGold = 2_600,
                rewardGems = 12,
                isClaimed = false,
                isDaily = true
            ),
            QuestItem(
                id = "dq_counter_$epochDay",
                title = "Auto Reflex",
                description = "Trigger Auto-Counter $counterTarget times today.",
                currentProgress = 0,
                targetProgress = counterTarget,
                rewardGold = 2_200,
                rewardGems = 10,
                isClaimed = false,
                isDaily = true
            ),
            QuestItem(
                id = "dq_ult_$epochDay",
                title = "Big Finish",
                description = "Use your Ultimate move 2 times today.",
                currentProgress = 0,
                targetProgress = 2,
                rewardGold = 2_400,
                rewardGems = 10,
                isClaimed = false,
                isDaily = true
            )
        )
    }

    val combatTips = listOf(
        "Tap Counter to block hits or ignore enemy powers!",
        "In Ultimate form, you can Dash many times fast!",
        "Check Daily Quests every day for free Gold and Gems.",
        "Use Gold in the Shop to boost Health, Power, and Faster Ult."
    )
}
