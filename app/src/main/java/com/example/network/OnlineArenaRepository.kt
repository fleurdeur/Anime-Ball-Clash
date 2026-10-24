package com.example.network

import android.content.Context
import com.example.R
import com.example.model.CharacterRoster
import com.example.model.CharacterUpgradeState
import com.example.model.LeaderboardEntry
import com.example.model.PlayerProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

data class CloudAccountSnapshot(
    val profile: PlayerProfile,
    val unlockedIds: Set<String>,
    val upgrades: Map<String, CharacterUpgradeState>
)

data class OnlineLeaderboardDoc(
    val userId: String = "",
    val username: String = "",
    val characterId: String = "kakarot_z",
    val rating: Int = 0,
    val tier: String = "Unranked",
    val wins: Int = 0,
    val losses: Int = 0
) {
    fun toLeaderboardEntry(rank: Int, currentUserId: String?): LeaderboardEntry {
        val total = wins + losses
        val wr = if (total <= 0) "0%" else "${((wins * 100f) / total).toInt()}%"
        return LeaderboardEntry(
            rank = rank,
            name = username,
            characterId = characterId,
            rating = rating,
            tier = tier,
            winRate = wr,
            isPlayer = userId == currentUserId
        )
    }
}

data class MatchRoomDoc(
    val roomId: String = "",
    val roomCode: String = "",
    val mode: String = "ranked",
    val status: String = "waiting",
    val hostUid: String = "",
    val hostName: String = "",
    val hostCharId: String = "kakarot_z",
    val hostRating: Int = 0,
    val hostHpLvl: Int = 0,
    val hostPwrLvl: Int = 0,
    val hostCdLvl: Int = 0,
    val guestUid: String = "",
    val guestName: String = "",
    val guestCharId: String = "",
    val guestRating: Int = 0,
    val guestHpLvl: Int = 0,
    val guestPwrLvl: Int = 0,
    val guestCdLvl: Int = 0,
    val seed: Int = 12345,
    val hostCmdSeq: Int = 0,
    val hostDirX: Float = 0f,
    val hostDirY: Float = 0f,
    val hostUltSeq: Int = 0,
    val hostCounterSeq: Int = 0,
    val hostSyncHp: Int = 360,
    val guestCmdSeq: Int = 0,
    val guestDirX: Float = 0f,
    val guestDirY: Float = 0f,
    val guestUltSeq: Int = 0,
    val guestCounterSeq: Int = 0,
    val guestSyncHp: Int = 360,
    val syncStatePacked: String = "",
    val winnerUid: String = ""
)

sealed class RankedQueueResult {
    data class JoinedExisting(val room: MatchRoomDoc) : RankedQueueResult()
    data class HostingWaiting(val room: MatchRoomDoc) : RankedQueueResult()
}

class OnlineArenaRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    suspend fun fetchOrCreateUserProfile(
        userId: String,
        fallbackProfile: PlayerProfile,
        fallbackUnlocked: Set<String>,
        fallbackUpgrades: Map<String, CharacterUpgradeState>
    ): CloudAccountSnapshot {
        val path = "users/$userId"
        try {
            val ref = db.collection("users").document(userId)
            val snap = ref.get().await()
            if (snap.exists()) {
                val parsed = parseCloudAccount(snap)
                if (parsed != null) return parsed
            }

            val safeName = fallbackProfile.username.trim().ifEmpty { "Player" }.take(24)
            val safeUnlocked = (fallbackUnlocked + CharacterRoster.starterIds).take(300)
            val packedUpgrades = packUpgrades(fallbackUpgrades)
            val initialProfile = fallbackProfile.copy(
                username = safeName,
                rankTier = PlayerProfile.computeTier(fallbackProfile.rankedRating, fallbackProfile.totalMatches)
            )
            val payload = mapOf(
                "userId" to userId,
                "username" to initialProfile.username,
                "level" to initialProfile.level.coerceIn(1, 999),
                "exp" to initialProfile.exp.coerceIn(0, 10_000_000),
                "maxExp" to initialProfile.maxExp.coerceIn(100, 10_000_000),
                "gold" to initialProfile.gold.coerceIn(0, 999_999_999),
                "gems" to initialProfile.gems.coerceIn(0, 99_999_999),
                "selectedCharacterId" to initialProfile.selectedCharacterId.take(64),
                "rankedRating" to initialProfile.rankedRating.coerceIn(0, 100_000),
                "rankTier" to initialProfile.rankTier.take(24),
                "wins" to initialProfile.wins.coerceIn(0, 1_000_000),
                "losses" to initialProfile.losses.coerceIn(0, 1_000_000),
                "unlockedIds" to safeUnlocked,
                "upgradesPacked" to packedUpgrades,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            ref.set(payload).await()
            return CloudAccountSnapshot(
                profile = initialProfile,
                unlockedIds = safeUnlocked.toSet(),
                upgrades = fallbackUpgrades
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.logAndRethrow(e, OperationType.WRITE, path)
        }
    }

    suspend fun saveUserProfile(
        userId: String,
        profile: PlayerProfile,
        unlockedIds: Set<String>,
        upgrades: Map<String, CharacterUpgradeState>
    ) {
        val path = "users/$userId"
        try {
            val ref = db.collection("users").document(userId)
            val snap = ref.get().await()
            val safeName = profile.username.trim().ifEmpty { "Player" }.take(24)
            val safeUnlocked = (unlockedIds + CharacterRoster.starterIds).take(300)
            val packed = packUpgrades(upgrades)
            val tier = PlayerProfile.computeTier(profile.rankedRating, profile.totalMatches).take(24)

            if (!snap.exists()) {
                val createPayload = mapOf(
                    "userId" to userId,
                    "username" to safeName,
                    "level" to profile.level.coerceIn(1, 999),
                    "exp" to profile.exp.coerceIn(0, 10_000_000),
                    "maxExp" to profile.maxExp.coerceIn(100, 10_000_000),
                    "gold" to profile.gold.coerceIn(0, 999_999_999),
                    "gems" to profile.gems.coerceIn(0, 99_999_999),
                    "selectedCharacterId" to profile.selectedCharacterId.take(64),
                    "rankedRating" to profile.rankedRating.coerceIn(0, 100_000),
                    "rankTier" to tier,
                    "wins" to profile.wins.coerceIn(0, 1_000_000),
                    "losses" to profile.losses.coerceIn(0, 1_000_000),
                    "unlockedIds" to safeUnlocked,
                    "upgradesPacked" to packed,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                ref.set(createPayload).await()
            } else {
                val updatePayload = mapOf(
                    "username" to safeName,
                    "level" to profile.level.coerceIn(1, 999),
                    "exp" to profile.exp.coerceIn(0, 10_000_000),
                    "maxExp" to profile.maxExp.coerceIn(100, 10_000_000),
                    "gold" to profile.gold.coerceIn(0, 999_999_999),
                    "gems" to profile.gems.coerceIn(0, 99_999_999),
                    "selectedCharacterId" to profile.selectedCharacterId.take(64),
                    "rankedRating" to profile.rankedRating.coerceIn(0, 100_000),
                    "rankTier" to tier,
                    "wins" to profile.wins.coerceIn(0, 1_000_000),
                    "losses" to profile.losses.coerceIn(0, 1_000_000),
                    "unlockedIds" to safeUnlocked,
                    "upgradesPacked" to packed,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                ref.update(updatePayload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.logAndRethrow(e, OperationType.UPDATE, path)
        }
    }

    suspend fun syncLeaderboardEntry(userId: String, profile: PlayerProfile) {
        val path = "leaderboard/$userId"
        try {
            val ref = db.collection("leaderboard").document(userId)
            val snap = ref.get().await()
            val safeName = profile.username.trim().ifEmpty { "Player" }.take(24)
            val tier = PlayerProfile.computeTier(profile.rankedRating, profile.totalMatches).take(24)

            if (!snap.exists()) {
                ref.set(
                    mapOf(
                        "userId" to userId,
                        "username" to safeName,
                        "characterId" to profile.selectedCharacterId.take(64),
                        "rating" to profile.rankedRating.coerceIn(0, 100_000),
                        "tier" to tier,
                        "wins" to profile.wins.coerceIn(0, 1_000_000),
                        "losses" to profile.losses.coerceIn(0, 1_000_000),
                        "isPublic" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ref.update(
                    mapOf(
                        "username" to safeName,
                        "characterId" to profile.selectedCharacterId.take(64),
                        "rating" to profile.rankedRating.coerceIn(0, 100_000),
                        "tier" to tier,
                        "wins" to profile.wins.coerceIn(0, 1_000_000),
                        "losses" to profile.losses.coerceIn(0, 1_000_000),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.logAndRethrow(e, OperationType.WRITE, path)
        }
    }

    fun observeOnlineLeaderboard(): Flow<List<OnlineLeaderboardDoc>> = callbackFlow {
        val path = "leaderboard"
        val registration = db.collection("leaderboard")
            .whereEqualTo("isPublic", true)
            .limit(60)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    FirestoreErrorHandler.logListenerError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val docs = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val uid = doc.getString("userId") ?: return@mapNotNull null
                    val name = doc.getString("username") ?: "Player"
                    val charId = doc.getString("characterId") ?: "kakarot_z"
                    val rating = (doc.getLong("rating") ?: 0L).toInt()
                    val tier = doc.getString("tier") ?: "Unranked"
                    val wins = (doc.getLong("wins") ?: 0L).toInt()
                    val losses = (doc.getLong("losses") ?: 0L).toInt()
                    OnlineLeaderboardDoc(
                        userId = uid,
                        username = name,
                        characterId = charId,
                        rating = rating,
                        tier = tier,
                        wins = wins,
                        losses = losses
                    )
                }.sortedByDescending { it.rating }
                trySend(docs)
            }
        awaitClose { registration.remove() }
    }

    suspend fun createFriendRoom(
        userId: String,
        profile: PlayerProfile,
        upgrades: CharacterUpgradeState,
        explicitCode: String? = null
    ): MatchRoomDoc {
        val code = (explicitCode ?: generateRoomCode()).uppercase().take(12)
        val roomId = "room_$code"
        val path = "match_rooms/$roomId"
        try {
            val char = CharacterRoster.getById(profile.selectedCharacterId)
            val hostMaxHp = upgrades.effectiveMaxHp(char.maxHp).coerceIn(1, 2000)
            val seed = Random.nextInt(10_000, 999_999)
            val payload = buildInitialRoomMap(
                roomCode = code,
                mode = "friend",
                hostUid = userId,
                profile = profile,
                upgrades = upgrades,
                hostMaxHp = hostMaxHp,
                seed = seed
            )
            db.collection("match_rooms").document(roomId).set(payload).await()
            val snap = db.collection("match_rooms").document(roomId).get().await()
            return parseMatchRoom(snap) ?: error("Failed to read created room")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.logAndRethrow(e, OperationType.CREATE, path)
        }
    }

    suspend fun joinFriendRoomByCode(
        rawCode: String,
        userId: String,
        profile: PlayerProfile,
        upgrades: CharacterUpgradeState
    ): MatchRoomDoc? {
        val code = rawCode.trim().uppercase().replace(Regex("[^A-Z0-9]"), "")
        if (code.length < 4) return null
        val roomId = "room_$code"
        val path = "match_rooms/$roomId"
        try {
            val ref = db.collection("match_rooms").document(roomId)
            val snap = ref.get().await()
            if (!snap.exists()) return null
            val existing = parseMatchRoom(snap) ?: return null
            if (existing.status != "waiting" || existing.hostUid == userId) return null

            val char = CharacterRoster.getById(profile.selectedCharacterId)
            val guestMaxHp = upgrades.effectiveMaxHp(char.maxHp).coerceIn(1, 2000)
            ref.update(
                mapOf(
                    "status" to "active",
                    "guestUid" to userId,
                    "guestName" to profile.username.trim().ifEmpty { "Player" }.take(24),
                    "guestCharId" to profile.selectedCharacterId.take(64),
                    "guestRating" to profile.rankedRating.coerceIn(0, 100_000),
                    "guestHpLvl" to upgrades.healthLevel.coerceIn(0, 10),
                    "guestPwrLvl" to upgrades.powerLevel.coerceIn(0, 10),
                    "guestCdLvl" to upgrades.cooldownLevel.coerceIn(0, 10),
                    "guestSyncHp" to guestMaxHp,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            return parseMatchRoom(ref.get().await())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.logAndRethrow(e, OperationType.UPDATE, path)
        }
    }

    suspend fun findOrCreateRankedRoom(
        userId: String,
        profile: PlayerProfile,
        upgrades: CharacterUpgradeState
    ): RankedQueueResult {
        val listPath = "match_rooms"
        try {
            val querySnap = db.collection("match_rooms")
                .whereEqualTo("status", "waiting")
                .limit(20)
                .get()
                .await()

            val candidateDoc = querySnap.documents
                .mapNotNull { parseMatchRoom(it) }
                .firstOrNull { it.mode == "ranked" && it.hostUid != userId && it.guestUid.isEmpty() }

            if (candidateDoc != null) {
                val char = CharacterRoster.getById(profile.selectedCharacterId)
                val guestMaxHp = upgrades.effectiveMaxHp(char.maxHp).coerceIn(1, 2000)
                val ref = db.collection("match_rooms").document(candidateDoc.roomId)
                try {
                    ref.update(
                        mapOf(
                            "status" to "active",
                            "guestUid" to userId,
                            "guestName" to profile.username.trim().ifEmpty { "Player" }.take(24),
                            "guestCharId" to profile.selectedCharacterId.take(64),
                            "guestRating" to profile.rankedRating.coerceIn(0, 100_000),
                            "guestHpLvl" to upgrades.healthLevel.coerceIn(0, 10),
                            "guestPwrLvl" to upgrades.powerLevel.coerceIn(0, 10),
                            "guestCdLvl" to upgrades.cooldownLevel.coerceIn(0, 10),
                            "guestSyncHp" to guestMaxHp,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    val joined = parseMatchRoom(ref.get().await())
                    if (joined != null) {
                        return RankedQueueResult.JoinedExisting(joined)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Another contender claimed this room first; fall through to create a fresh ticket.
                }
            }

            val ticketCode = generateRoomCode()
            val roomId = "ranked_${ticketCode}_${Random.nextInt(100, 999)}"
            val char = CharacterRoster.getById(profile.selectedCharacterId)
            val hostMaxHp = upgrades.effectiveMaxHp(char.maxHp).coerceIn(1, 2000)
            val seed = Random.nextInt(10_000, 999_999)
            val payload = buildInitialRoomMap(
                roomCode = ticketCode,
                mode = "ranked",
                hostUid = userId,
                profile = profile,
                upgrades = upgrades,
                hostMaxHp = hostMaxHp,
                seed = seed
            )
            val hostRef = db.collection("match_rooms").document(roomId)
            hostRef.set(payload).await()
            val hosted = parseMatchRoom(hostRef.get().await()) ?: error("Failed to read hosted ranked room")
            return RankedQueueResult.HostingWaiting(hosted)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.logAndRethrow(e, OperationType.WRITE, listPath)
        }
    }

    fun observeMatchRoom(roomId: String): Flow<MatchRoomDoc?> = callbackFlow {
        val path = "match_rooms/$roomId"
        val registration = db.collection("match_rooms").document(roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    FirestoreErrorHandler.logListenerError(error, OperationType.GET, path)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    trySend(null)
                } else {
                    trySend(parseMatchRoom(snapshot))
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun sendHostCombatUpdate(
        roomId: String,
        cmdSeq: Int,
        dirX: Float,
        dirY: Float,
        ultSeq: Int,
        hostSyncHp: Int,
        guestSyncHp: Int,
        status: String = "active",
        winnerUid: String = "",
        counterSeq: Int = 0,
        syncStatePacked: String = ""
    ) {
        val path = "match_rooms/$roomId"
        try {
            val payload = HashMap<String, Any>(12)
            payload["status"] = status
            payload["hostCmdSeq"] = cmdSeq.coerceIn(0, 1_000_000)
            payload["hostDirX"] = dirX.coerceIn(-1.5f, 1.5f).toDouble()
            payload["hostDirY"] = dirY.coerceIn(-1.5f, 1.5f).toDouble()
            payload["hostUltSeq"] = ultSeq.coerceIn(0, 1_000_000)
            payload["hostCounterSeq"] = counterSeq.coerceIn(0, 1_000_000)
            payload["hostSyncHp"] = hostSyncHp.coerceIn(0, 2000)
            payload["guestSyncHp"] = guestSyncHp.coerceIn(0, 2000)
            if (syncStatePacked.isNotEmpty()) {
                payload["syncStatePacked"] = syncStatePacked.take(240)
            }
            payload["winnerUid"] = winnerUid.take(128)
            payload["updatedAt"] = FieldValue.serverTimestamp()
            db.collection("match_rooms").document(roomId).update(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.logAndRethrow(e, OperationType.UPDATE, path)
        }
    }

    suspend fun sendGuestCombatUpdate(
        roomId: String,
        cmdSeq: Int,
        dirX: Float,
        dirY: Float,
        ultSeq: Int,
        guestSyncHp: Int? = null,
        status: String = "active",
        winnerUid: String = "",
        counterSeq: Int = 0
    ) {
        val path = "match_rooms/$roomId"
        try {
            val payload = HashMap<String, Any>(10)
            payload["status"] = status
            payload["guestCmdSeq"] = cmdSeq.coerceIn(0, 1_000_000)
            payload["guestDirX"] = dirX.coerceIn(-1.5f, 1.5f).toDouble()
            payload["guestDirY"] = dirY.coerceIn(-1.5f, 1.5f).toDouble()
            payload["guestUltSeq"] = ultSeq.coerceIn(0, 1_000_000)
            payload["guestCounterSeq"] = counterSeq.coerceIn(0, 1_000_000)
            if (guestSyncHp != null) {
                payload["guestSyncHp"] = guestSyncHp.coerceIn(0, 2000)
            }
            payload["winnerUid"] = winnerUid.take(128)
            payload["updatedAt"] = FieldValue.serverTimestamp()
            db.collection("match_rooms").document(roomId).update(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.logAndRethrow(e, OperationType.UPDATE, path)
        }
    }

    suspend fun sendGuestForfeit(roomId: String, hostUid: String) {
        val path = "match_rooms/$roomId"
        try {
            db.collection("match_rooms").document(roomId).update(
                mapOf(
                    "status" to "finished",
                    "winnerUid" to hostUid.take(128),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.logListenerError(e, OperationType.UPDATE, path)
        }
    }

    suspend fun deleteRoomSafely(roomId: String) {
        val path = "match_rooms/$roomId"
        try {
            db.collection("match_rooms").document(roomId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.logListenerError(e, OperationType.DELETE, path)
        }
    }

    private fun buildInitialRoomMap(
        roomCode: String,
        mode: String,
        hostUid: String,
        profile: PlayerProfile,
        upgrades: CharacterUpgradeState,
        hostMaxHp: Int,
        seed: Int
    ): Map<String, Any> = mapOf(
        "roomCode" to roomCode,
        "mode" to mode,
        "status" to "waiting",
        "hostUid" to hostUid,
        "hostName" to profile.username.trim().ifEmpty { "Player" }.take(24),
        "hostCharId" to profile.selectedCharacterId.take(64),
        "hostRating" to profile.rankedRating.coerceIn(0, 100_000),
        "hostHpLvl" to upgrades.healthLevel.coerceIn(0, 10),
        "hostPwrLvl" to upgrades.powerLevel.coerceIn(0, 10),
        "hostCdLvl" to upgrades.cooldownLevel.coerceIn(0, 10),
        "guestUid" to "",
        "guestName" to "",
        "guestCharId" to "",
        "guestRating" to 0,
        "guestHpLvl" to 0,
        "guestPwrLvl" to 0,
        "guestCdLvl" to 0,
        "seed" to seed,
        "hostCmdSeq" to 0,
        "hostDirX" to 0.0,
        "hostDirY" to 0.0,
        "hostUltSeq" to 0,
        "hostCounterSeq" to 0,
        "hostSyncHp" to hostMaxHp,
        "guestCmdSeq" to 0,
        "guestDirX" to 0.0,
        "guestDirY" to 0.0,
        "guestUltSeq" to 0,
        "guestCounterSeq" to 0,
        "guestSyncHp" to 360,
        "syncStatePacked" to "",
        "winnerUid" to "",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    private fun parseCloudAccount(doc: DocumentSnapshot): CloudAccountSnapshot? {
        val name = doc.getString("username") ?: return null
        val level = (doc.getLong("level") ?: 1L).toInt().coerceIn(1, 999)
        val exp = (doc.getLong("exp") ?: 0L).toInt().coerceAtLeast(0)
        val maxExp = (doc.getLong("maxExp") ?: 200L).toInt().coerceAtLeast(100)
        val gold = (doc.getLong("gold") ?: 0L).toInt().coerceAtLeast(0)
        val gems = (doc.getLong("gems") ?: 0L).toInt().coerceAtLeast(0)
        val rawChar = doc.getString("selectedCharacterId") ?: "kakarot_z"
        val validChar = CharacterRoster.getById(rawChar).id
        val rating = (doc.getLong("rankedRating") ?: 0L).toInt().coerceAtLeast(0)
        val wins = (doc.getLong("wins") ?: 0L).toInt().coerceAtLeast(0)
        val losses = (doc.getLong("losses") ?: 0L).toInt().coerceAtLeast(0)
        val unlockedRaw = (doc.get("unlockedIds") as? List<*>)?.mapNotNull { it as? String }.orEmpty()
        val validCatalog = CharacterRoster.characters.map { it.id }.toSet()
        val unlockedSet = (unlockedRaw.intersect(validCatalog) + CharacterRoster.starterIds)
        val upgrades = unpackUpgrades(doc.getString("upgradesPacked") ?: "")

        val profile = PlayerProfile(
            username = name.take(16),
            level = level,
            exp = exp,
            maxExp = maxExp,
            gold = gold,
            gems = gems,
            selectedCharacterId = validChar,
            rankedRating = rating,
            rankTier = PlayerProfile.computeTier(rating, wins + losses),
            wins = wins,
            losses = losses
        )
        return CloudAccountSnapshot(profile = profile, unlockedIds = unlockedSet, upgrades = upgrades)
    }

    private fun parseMatchRoom(doc: DocumentSnapshot): MatchRoomDoc? {
        if (!doc.exists()) return null
        val roomCode = doc.getString("roomCode") ?: return null
        val mode = doc.getString("mode") ?: "ranked"
        val status = doc.getString("status") ?: "waiting"
        val hostUid = doc.getString("hostUid") ?: return null
        return MatchRoomDoc(
            roomId = doc.id,
            roomCode = roomCode,
            mode = mode,
            status = status,
            hostUid = hostUid,
            hostName = doc.getString("hostName") ?: "Host",
            hostCharId = doc.getString("hostCharId") ?: "kakarot_z",
            hostRating = (doc.getLong("hostRating") ?: 0L).toInt(),
            hostHpLvl = (doc.getLong("hostHpLvl") ?: 0L).toInt(),
            hostPwrLvl = (doc.getLong("hostPwrLvl") ?: 0L).toInt(),
            hostCdLvl = (doc.getLong("hostCdLvl") ?: 0L).toInt(),
            guestUid = doc.getString("guestUid") ?: "",
            guestName = doc.getString("guestName") ?: "",
            guestCharId = doc.getString("guestCharId") ?: "",
            guestRating = (doc.getLong("guestRating") ?: 0L).toInt(),
            guestHpLvl = (doc.getLong("guestHpLvl") ?: 0L).toInt(),
            guestPwrLvl = (doc.getLong("guestPwrLvl") ?: 0L).toInt(),
            guestCdLvl = (doc.getLong("guestCdLvl") ?: 0L).toInt(),
            seed = (doc.getLong("seed") ?: 12345L).toInt(),
            hostCmdSeq = (doc.getLong("hostCmdSeq") ?: 0L).toInt(),
            hostDirX = (doc.getDouble("hostDirX") ?: 0.0).toFloat(),
            hostDirY = (doc.getDouble("hostDirY") ?: 0.0).toFloat(),
            hostUltSeq = (doc.getLong("hostUltSeq") ?: 0L).toInt(),
            hostCounterSeq = (doc.getLong("hostCounterSeq") ?: 0L).toInt(),
            hostSyncHp = (doc.getLong("hostSyncHp") ?: 360L).toInt(),
            guestCmdSeq = (doc.getLong("guestCmdSeq") ?: 0L).toInt(),
            guestDirX = (doc.getDouble("guestDirX") ?: 0.0).toFloat(),
            guestDirY = (doc.getDouble("guestDirY") ?: 0.0).toFloat(),
            guestUltSeq = (doc.getLong("guestUltSeq") ?: 0L).toInt(),
            guestCounterSeq = (doc.getLong("guestCounterSeq") ?: 0L).toInt(),
            guestSyncHp = (doc.getLong("guestSyncHp") ?: 360L).toInt(),
            syncStatePacked = doc.getString("syncStatePacked") ?: "",
            winnerUid = doc.getString("winnerUid") ?: ""
        )
    }

    companion object {
        private const val CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        fun create(context: Context): OnlineArenaRepository {
            val databaseId = context.applicationContext.getString(R.string.firestore_database_id)
            return OnlineArenaRepository(FirebaseFirestore.getInstance(databaseId))
        }

        fun generateRoomCode(length: Int = 6): String {
            return buildString(length) {
                repeat(length) {
                    append(CODE_ALPHABET[Random.nextInt(CODE_ALPHABET.length)])
                }
            }
        }

        fun packUpgrades(upgrades: Map<String, CharacterUpgradeState>): String {
            return upgrades.entries
                .filter { it.value.totalUpgrades > 0 }
                .joinToString(";") { (id, st) ->
                    "$id:${st.healthLevel},${st.powerLevel},${st.cooldownLevel}"
                }
                .take(7900)
        }

        fun unpackUpgrades(packed: String): Map<String, CharacterUpgradeState> {
            if (packed.isBlank()) return emptyMap()
            val out = HashMap<String, CharacterUpgradeState>()
            for (token in packed.split(';')) {
                val parts = token.split(':')
                if (parts.size != 2) continue
                val id = parts[0]
                val nums = parts[1].split(',')
                if (nums.size != 3) continue
                val hp = nums[0].toIntOrNull()?.coerceIn(0, CharacterUpgradeState.MAX_LEVEL) ?: 0
                val pwr = nums[1].toIntOrNull()?.coerceIn(0, CharacterUpgradeState.MAX_LEVEL) ?: 0
                val cd = nums[2].toIntOrNull()?.coerceIn(0, CharacterUpgradeState.MAX_LEVEL) ?: 0
                if (hp > 0 || pwr > 0 || cd > 0) {
                    out[id] = CharacterUpgradeState(hp, pwr, cd)
                }
            }
            return out
        }
    }
}
