package com.example

import com.example.model.CharacterRoster
import com.example.model.CharacterUpgradeState
import com.example.model.PlayerProfile
import com.example.network.OnlineArenaRepository
import com.example.network.RankedQueueResult
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnlineArenaRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun `unauthenticated user fails leaderboard observation with PERMISSION_DENIED`() = runBlocking {
        auth.signOut()
        val repo = OnlineArenaRepository(firestore)

        val outcome = runCatching {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeOnlineLeaderboard().first()
            }
        }
        assertTrue("Expected unauthenticated query to fail", outcome.isFailure)
        val firestoreEx = outcome.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull("Expected nested FirebaseFirestoreException", firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun `user profile syncs and blocks unauthorized cross-user writes`() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = OnlineArenaRepository(firestore)

        val profile = PlayerProfile(
            username = "AliceSaiyan",
            level = 4,
            exp = 80,
            maxExp = 500,
            gold = 14500,
            gems = 55,
            selectedCharacterId = "kakarot_z",
            rankedRating = 340,
            rankTier = "Silver",
            wins = 8,
            losses = 3
        )
        val upgrades = mapOf("kakarot_z" to CharacterUpgradeState(2, 3, 1))

        val created = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.fetchOrCreateUserProfile(
                userId = aliceUid,
                fallbackProfile = profile,
                fallbackUnlocked = CharacterRoster.starterIds + "guts_berserk",
                fallbackUpgrades = upgrades
            )
        }
        assertEquals("AliceSaiyan", created.profile.username)
        assertTrue(created.unlockedIds.contains("guts_berserk"))

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveUserProfile(
                userId = aliceUid,
                profile = profile.copy(gold = 22000, wins = 9),
                unlockedIds = created.unlockedIds,
                upgrades = upgrades
            )
        }
        val reloaded = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.fetchOrCreateUserProfile(
                userId = aliceUid,
                fallbackProfile = PlayerProfile(),
                fallbackUnlocked = CharacterRoster.starterIds,
                fallbackUpgrades = emptyMap()
            )
        }
        assertEquals(22000, reloaded.profile.gold)
        assertEquals(9, reloaded.profile.wins)
        assertEquals(3, reloaded.upgrades["kakarot_z"]?.powerLevel)

        // Mallory cannot overwrite or read Alice's profile
        signInTestUser(MALLORY_EMAIL)
        val attack = runCatching {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repo.saveUserProfile(
                    userId = aliceUid,
                    profile = profile.copy(gold = 999999),
                    unlockedIds = CharacterRoster.starterIds,
                    upgrades = emptyMap()
                )
            }
        }
        assertTrue("Cross-user profile write must be denied by security rules", attack.isFailure)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            attack.exceptionOrNull()?.unwrapFirestoreException()?.code
        )
    }

    @Test
    fun `online leaderboard syncs and streams public rankings`() = runBlocking {
        val repo = OnlineArenaRepository(firestore)

        val aliceUid = signInTestUser(ALICE_EMAIL)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.syncLeaderboardEntry(
                userId = aliceUid,
                profile = PlayerProfile(username = "AliceTop", selectedCharacterId = "guts_berserk", rankedRating = 880, wins = 12, losses = 2)
            )
        }

        val bobUid = signInTestUser(BOB_EMAIL)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.syncLeaderboardEntry(
                userId = bobUid,
                profile = PlayerProfile(username = "BobRival", selectedCharacterId = "satoru_void", rankedRating = 620, wins = 7, losses = 4)
            )
        }

        val entries = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeOnlineLeaderboard().first { it.size >= 2 }
        }
        assertEquals("AliceTop", entries[0].username)
        assertEquals(880, entries[0].rating)
        assertEquals("BobRival", entries[1].username)
    }

    @Test
    fun `friend room code creation, guest join, and ranked matchmaking flow pass rules`() = runBlocking {
        val repo = OnlineArenaRepository(firestore)
        val hostProfile = PlayerProfile(username = "HostGuts", selectedCharacterId = "guts_berserk", rankedRating = 450)
        val guestProfile = PlayerProfile(username = "GuestGojo", selectedCharacterId = "satoru_void", rankedRating = 470)

        val uniqueCode = UUID.randomUUID().toString().replace("-", "").take(6).uppercase()

        // 1. Host creates Internet Friend Room
        val hostUid = signInTestUser(HOST_EMAIL)
        val createdRoom = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createFriendRoom(
                userId = hostUid,
                profile = hostProfile,
                upgrades = CharacterUpgradeState(1, 2, 0),
                explicitCode = uniqueCode
            )
        }
        assertEquals("waiting", createdRoom.status)
        assertEquals(uniqueCode, createdRoom.roomCode)

        // 2. Guest joins by room code
        val guestUid = signInTestUser(GUEST_EMAIL)
        val joinedRoom = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.joinFriendRoomByCode(
                rawCode = uniqueCode.lowercase(),
                userId = guestUid,
                profile = guestProfile,
                upgrades = CharacterUpgradeState(2, 1, 1)
            )
        }
        assertNotNull(joinedRoom)
        assertEquals("active", joinedRoom!!.status)
        assertEquals(guestUid, joinedRoom.guestUid)

        // Guest sends combat update
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.sendGuestCombatUpdate(
                roomId = joinedRoom.roomId,
                cmdSeq = 1,
                dirX = -0.5f,
                dirY = 0.86f,
                ultSeq = 1,
                guestSyncHp = 355
            )
        }

        // Host sends combat update and finishes match
        signInTestUser(HOST_EMAIL)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.sendHostCombatUpdate(
                roomId = joinedRoom.roomId,
                cmdSeq = 2,
                dirX = 0.4f,
                dirY = -0.9f,
                ultSeq = 1,
                hostSyncHp = 320,
                guestSyncHp = 0,
                status = "finished",
                winnerUid = hostUid
            )
        }
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.deleteRoomSafely(joinedRoom.roomId)
        }

        // 3. Online Ranked Matchmaking Queue flow
        val hostQueue = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.findOrCreateRankedRoom(hostUid, hostProfile, CharacterUpgradeState())
        }
        assertTrue(hostQueue is RankedQueueResult.HostingWaiting)

        signInTestUser(GUEST_EMAIL)
        val guestQueue = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.findOrCreateRankedRoom(guestUid, guestProfile, CharacterUpgradeState())
        }
        assertTrue(guestQueue is RankedQueueResult.JoinedExisting)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val MALLORY_EMAIL = "mallory@test.com"
        const val HOST_EMAIL = "host@test.com"
        const val GUEST_EMAIL = "guest@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
