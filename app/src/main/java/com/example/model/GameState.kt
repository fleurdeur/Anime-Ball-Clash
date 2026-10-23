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
    PRACTICE_BOT("Solo Practice", "AI Bot", "Train nonstop ricochet combos against adaptive AI tiers"),
    ONLINE_MATCH("Ranked Arena", "Competitive", "Climb from Unranked to Master in high-stakes 1v1 clashes"),
    FRIENDS_LOBBY("Exhibition Duel", "Free Play", "Fast custom 1v1 arena bout with balanced tournament rules")
}

enum class BotDifficulty(
    val title: String,
    val shortLabel: String,
    val aimAccuracy: Float,
    val speedFactor: Float,
    val redirectCooldownSec: Float
) {
    EASY("Rookie", "Rookie", 0.72f, 0.90f, 5.2f),
    MEDIUM("Challenger", "Challenger", 0.88f, 1.00f, 3.8f),
    HARD("Grandmaster", "Master", 0.97f, 1.10f, 2.7f)
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
            title = "Opening Bell",
            description = "Complete 1 nonstop arena battle in any mode.",
            currentProgress = 0,
            targetProgress = 1,
            rewardGold = 200,
            rewardGems = 10,
            isClaimed = false
        ),
        QuestItem(
            id = "q_wall_ricochet",
            title = "Pinball Kineticist",
            description = "Trigger 25 wall ricochets across arena matches.",
            currentProgress = 0,
            targetProgress = 25,
            rewardGold = 350,
            rewardGems = 15,
            isClaimed = false
        ),
        QuestItem(
            id = "q_awakenings",
            title = "Limit Breaker",
            description = "Activate an Ultimate Awakening 2 times in combat.",
            currentProgress = 0,
            targetProgress = 2,
            rewardGold = 400,
            rewardGems = 20,
            isClaimed = false
        ),
        QuestItem(
            id = "q_clean_victory",
            title = "Decisive Knockout",
            description = "Win a match with at least 40 HP remaining.",
            currentProgress = 0,
            targetProgress = 1,
            rewardGold = 500,
            rewardGems = 25,
            isClaimed = false
        ),
        QuestItem(
            id = "q_three_wins",
            title = "Arena Contender",
            description = "Win 3 arena clashes against AI or Ranked rivals.",
            currentProgress = 0,
            targetProgress = 3,
            rewardGold = 650,
            rewardGems = 35,
            isClaimed = false
        )
    )

    private val rivalSeeds = listOf(
        LeaderboardEntry(1, "SungShadow_KR", "jinwoo_monarch", 1890, "Master", "84%"),
        LeaderboardEntry(2, "SixEyes_Satoru", "satoru_void", 1740, "Master", "81%"),
        LeaderboardEntry(3, "Zoltraak_Mage", "frieren_zoltraak", 1520, "Diamond", "78%"),
        LeaderboardEntry(4, "ShrineKing", "sukuna_shrine", 1340, "Diamond", "75%"),
        LeaderboardEntry(5, "SeriousCaped", "caped_baldy", 1120, "Diamond", "73%"),
        LeaderboardEntry(6, "ZoldyckVolt", "killua_godspeed", 890, "Platinum", "70%"),
        LeaderboardEntry(7, "Santoryu_Z", "roronoa_ashura", 680, "Gold", "66%"),
        LeaderboardEntry(8, "HollowMask_15", "kurosaki_hollow", 440, "Gold", "62%"),
        LeaderboardEntry(9, "SunBreather", "kamado_sun", 220, "Silver", "58%"),
        LeaderboardEntry(10, "Rookie Sparring", "mugiwara_joy", 80, "Bronze", "51%")
    )

    fun buildLeaderboard(profile: PlayerProfile): List<LeaderboardEntry> {
        val playerEntry = LeaderboardEntry(
            rank = 0,
            name = profile.username,
            characterId = profile.selectedCharacterId,
            rating = profile.rankedRating,
            tier = PlayerProfile.computeTier(profile.rankedRating, profile.totalMatches),
            winRate = "${profile.winRatePercent}%",
            isPlayer = true
        )
        return (rivalSeeds + playerEntry)
            .sortedWith(compareByDescending<LeaderboardEntry> { it.rating }.thenByDescending { it.isPlayer })
            .mapIndexed { idx, entry -> entry.copy(rank = idx + 1) }
    }

    val combatTips = listOf(
        "Once launched, both spheres ricochet nonstop off walls and each other until one hits 0 HP.",
        "Every wall bounce before hitting your rival adds +2 bank-shot bonus damage (up to +10).",
        "Drag anywhere on the court when Redirect is ready to bend your trajectory mid-flight.",
        "Tap Awaken as soon as your Ultimate meter hits 100% to turn the tide."
    )
}
