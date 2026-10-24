package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AppScreen
import com.example.model.BotDifficulty
import com.example.model.CharacterRoster
import com.example.model.GameMode
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read core strings and verify custom match viewmodel flow`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Anime Ball Clash", context.getString(R.string.app_name))
        assertEquals("Sign in with Google", context.getString(R.string.sign_in_with_google))
        assertEquals("Custom Match", context.getString(R.string.custom_play_title))
        assertEquals("VS Friend", context.getString(R.string.multiplayer_lobby_title))

        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GameViewModel(app)
        val chosenRival = CharacterRoster.getById("bondrewd_dawn")
        vm.selectCustomOpponent(chosenRival)
        vm.setBotDifficulty(BotDifficulty.HARD)
        assertEquals("bondrewd_dawn", vm.customOpponentCharacter.value.id)
        assertEquals(BotDifficulty.HARD, vm.botDifficulty.value)

        vm.startCustomMatch()
        assertEquals(GameMode.CUSTOM_MATCH, vm.selectedMode.value)
        assertEquals("bondrewd_dawn", vm.opponentCharacter.value.id)
        assertTrue(vm.opponentName.value.contains(chosenRival.name))
        assertTrue(vm.hudState.value.autoCounterPercent in 18..48)
        assertTrue(vm.hudState.value.counterLabel.isNotBlank())

        vm.navigateTo(AppScreen.MULTIPLAYER_LOBBY)
        assertEquals(AppScreen.MULTIPLAYER_LOBBY, vm.currentScreen.value)
    }

    @Test
    fun `verify back stack steps back through history and daily quests initialize`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GameViewModel(app)
        vm.navigateToHomeReset()
        assertEquals(AppScreen.HOME_HUB, vm.currentScreen.value)

        // Navigate Home -> Shop -> CharacterSelect -> Quests
        vm.navigateTo(AppScreen.SHOP)
        assertEquals(AppScreen.SHOP, vm.currentScreen.value)

        vm.navigateTo(AppScreen.CHARACTER_SELECT)
        assertEquals(AppScreen.CHARACTER_SELECT, vm.currentScreen.value)

        vm.navigateTo(AppScreen.QUESTS)
        assertEquals(AppScreen.QUESTS, vm.currentScreen.value)

        // Pressing Back steps back to CHARACTER_SELECT (not straight to HOME_HUB!)
        assertTrue(vm.navigateBack())
        assertEquals(AppScreen.CHARACTER_SELECT, vm.currentScreen.value)

        // Pressing Back again steps back to SHOP
        assertTrue(vm.navigateBack())
        assertEquals(AppScreen.SHOP, vm.currentScreen.value)

        // Pressing Back again steps back to HOME_HUB
        assertTrue(vm.navigateBack())
        assertEquals(AppScreen.HOME_HUB, vm.currentScreen.value)

        // Daily quests & music toggle work cleanly
        assertEquals(4, vm.dailyQuests.value.size)
        val initialMusic = vm.engineSettings.value.musicEnabled
        vm.toggleMusic()
        assertEquals(!initialMusic, vm.engineSettings.value.musicEnabled)
    }
}
