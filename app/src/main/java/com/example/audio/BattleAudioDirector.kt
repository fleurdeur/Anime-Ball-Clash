package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.model.CharacterAlignment
import com.example.model.CharacterClass
import com.example.model.CharacterData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class BattleBgmSituation {
    NONE,
    OPENING_TENSION,
    NORMAL_CLASH,
    ULTIMATE_SURGE,
    CLUTCH_DANGER
}

/**
 * Directs situational fighting Background Music (BGM) and speaks each character's
 * iconic Ultimate voice line when Ultimate mode activates.
 *
 * - BGM dynamically shifts across 4 live combat states:
 *   1. OPENING_TENSION (Pre-launch / Aiming)
 *   2. NORMAL_CLASH (Standard high-energy duel)
 *   3. ULTIMATE_SURGE (Either fighter in Ultimate Awakening!)
 *   4. CLUTCH_DANGER (Low HP < 32% clutch showdown!)
 * - Voice Lines speak the character's official Ultimate callout via Android TextToSpeech
 *   with archetype-matched pitch & cadence while briefly ducking BGM volume.
 */
class BattleAudioDirector(context: Context?) {

    private val appContext: Context? = context?.applicationContext
    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var tts: TextToSpeech? = null
    @Volatile
    private var isTtsReady: Boolean = false

    @Volatile
    private var _voiceEnabled: Boolean = true

    var sfxEnabled: Boolean
        get() = _voiceEnabled
        set(value) {
            _voiceEnabled = value
            if (!value) {
                try {
                    tts?.stop()
                } catch (_: Throwable) {
                }
            }
        }

    @Volatile
    private var _musicEnabled: Boolean = true

    var musicEnabled: Boolean
        get() = _musicEnabled
        set(value) {
            _musicEnabled = value
            if (!value) {
                currentSituation = BattleBgmSituation.NONE
                stopMediaPlayerInternal()
            }
        }

    @Volatile
    private var isAppForeground: Boolean = true

    @Volatile
    private var voiceDuckingActive: Boolean = false

    private var currentSituation: BattleBgmSituation = BattleBgmSituation.NONE
    val activeSituation: BattleBgmSituation get() = currentSituation
    private var mediaPlayer: MediaPlayer? = null
    private val trackFiles = HashMap<BattleBgmSituation, File>()
    @Volatile
    private var tracksPrepared: Boolean = false

    // Unit-test & diagnostic counters
    @Volatile
    var spokenVoiceCount: Int = 0
        private set

    @Volatile
    var lastSpokenLine: String = ""
        private set

    init {
        if (appContext != null) {
            initTextToSpeech(appContext)
            audioScope.launch {
                prepareBgmWaveFiles(appContext)
            }
        }
    }

    private fun initTextToSpeech(ctx: Context) {
        try {
            tts = TextToSpeech(ctx) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        tts?.language = Locale.US
                        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                voiceDuckingActive = true
                                applyCurrentPlayerVolume()
                            }

                            override fun onDone(utteranceId: String?) {
                                voiceDuckingActive = false
                                applyCurrentPlayerVolume()
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                voiceDuckingActive = false
                                applyCurrentPlayerVolume()
                            }
                        })
                        isTtsReady = true
                    } catch (_: Throwable) {
                        isTtsReady = false
                    }
                }
            }
        } catch (_: Throwable) {
            tts = null
            isTtsReady = false
        }
    }

    private fun prepareBgmWaveFiles(ctx: Context) {
        try {
            val bgmDir = File(ctx.cacheDir, "abc_bgm_v1").apply { mkdirs() }
            val situations = listOf(
                BattleBgmSituation.OPENING_TENSION,
                BattleBgmSituation.NORMAL_CLASH,
                BattleBgmSituation.ULTIMATE_SURGE,
                BattleBgmSituation.CLUTCH_DANGER
            )
            for (sit in situations) {
                val file = File(bgmDir, "${sit.name.lowercase()}.wav")
                if (!file.exists() || file.length() < 1024L) {
                    val wavBytes = synthesizeBgmTrackWav(sit)
                    FileOutputStream(file).use { it.write(wavBytes) }
                }
                synchronized(trackFiles) {
                    trackFiles[sit] = file
                }
            }
            tracksPrepared = true
            // If battle already started while tracks were writing, kick off playback now
            val desired = currentSituation
            if (desired != BattleBgmSituation.NONE && _musicEnabled && isAppForeground) {
                switchMediaPlayerTo(desired)
            }
        } catch (_: Throwable) {
            tracksPrepared = false
        }
    }

    /**
     * Speaks the character's Ultimate voice line audibly when Ultimate activates!
     */
    fun speakUltimateVoiceLine(character: CharacterData, isPlayer: Boolean) {
        if (!_voiceEnabled) return
        val line = character.officialUltVoiceLine.ifBlank { character.voiceLine }
        spokenVoiceCount++
        lastSpokenLine = line

        val engine = tts ?: return
        if (!isTtsReady) return

        try {
            val pitch = when {
                character.id in setOf("dio_world", "madara_uchiha", "sukuna_shrine", "guts_berserk", "blackbeard_tremor", "muzan_progenitor", "aizen_kyoka") -> 0.78f
                character.alignment == CharacterAlignment.ANTAGONIST -> 0.85f
                character.classType == CharacterClass.SPEEDSTER -> 1.04f
                else -> 0.94f
            }
            val rate = when {
                character.id in setOf("kujo_star", "dio_world", "giorno_requiem") -> 1.14f
                character.classType == CharacterClass.SPEEDSTER -> 1.12f
                else -> 1.05f
            }
            engine.setPitch(pitch)
            engine.setSpeechRate(rate)
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, if (isPlayer) 1.0f else 0.90f)
            }
            val queueMode = if (isPlayer) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            engine.speak(line, queueMode, params, "ult_${character.id}_${System.currentTimeMillis()}")
        } catch (_: Throwable) {
        }
    }

    fun updateBattleSituation(
        inBattleArena: Boolean,
        isClashStarted: Boolean,
        eitherUltimateActive: Boolean,
        lowestHpRatio: Float
    ) {
        updateBattleSituation(
            inBattleScreen = inBattleArena,
            isPaused = false,
            isClashStarted = isClashStarted,
            isRoundOver = false,
            anyUltimateActive = eitherUltimateActive,
            lowestHpRatio = lowestHpRatio,
            musicEnabled = _musicEnabled
        )
    }

    /**
     * Evaluates the live battle situation and switches BGM seamlessly when situation changes.
     */
    fun updateBattleSituation(
        inBattleScreen: Boolean,
        isPaused: Boolean,
        isClashStarted: Boolean,
        isRoundOver: Boolean,
        anyUltimateActive: Boolean,
        lowestHpRatio: Float,
        musicEnabled: Boolean
    ) {
        _musicEnabled = musicEnabled
        val target = when {
            !inBattleScreen || isPaused || isRoundOver || !musicEnabled || !isAppForeground ->
                BattleBgmSituation.NONE
            !isClashStarted ->
                BattleBgmSituation.OPENING_TENSION
            anyUltimateActive ->
                BattleBgmSituation.ULTIMATE_SURGE
            lowestHpRatio <= 0.32f ->
                BattleBgmSituation.CLUTCH_DANGER
            else ->
                BattleBgmSituation.NORMAL_CLASH
        }

        if (target == currentSituation) {
            if (target == BattleBgmSituation.NONE) {
                stopMediaPlayerInternal()
            }
            return
        }
        currentSituation = target
        if (target == BattleBgmSituation.NONE) {
            stopMediaPlayerInternal()
        } else if (tracksPrepared) {
            audioScope.launch {
                switchMediaPlayerTo(target)
            }
        }
    }

    fun stopBattleMusic() {
        currentSituation = BattleBgmSituation.NONE
        stopMediaPlayerInternal()
    }

    fun onAppPause() {
        isAppForeground = false
        try {
            tts?.stop()
        } catch (_: Throwable) {
        }
        stopBattleMusic()
    }

    fun onAppResume() {
        isAppForeground = true
    }

    @Synchronized
    private fun switchMediaPlayerTo(situation: BattleBgmSituation) {
        stopMediaPlayerInternal()
        if (!_musicEnabled || !isAppForeground || situation == BattleBgmSituation.NONE) return
        val file = synchronized(trackFiles) { trackFiles[situation] } ?: return
        if (!file.exists()) return

        try {
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mp.setDataSource(file.absolutePath)
            mp.isLooping = true
            mp.prepare()
            val vol = computeTargetVolume(situation)
            mp.setVolume(vol, vol)
            mp.start()
            mediaPlayer = mp
        } catch (_: Throwable) {
            mediaPlayer = null
        }
    }

    @Synchronized
    private fun applyCurrentPlayerVolume() {
        val mp = mediaPlayer ?: return
        try {
            val vol = computeTargetVolume(currentSituation)
            mp.setVolume(vol, vol)
        } catch (_: Throwable) {
        }
    }

    private fun computeTargetVolume(situation: BattleBgmSituation): Float {
        val base = when (situation) {
            BattleBgmSituation.NONE -> 0f
            BattleBgmSituation.OPENING_TENSION -> 0.28f
            BattleBgmSituation.NORMAL_CLASH -> 0.36f
            BattleBgmSituation.ULTIMATE_SURGE -> 0.44f
            BattleBgmSituation.CLUTCH_DANGER -> 0.42f
        }
        return if (voiceDuckingActive) base * 0.42f else base
    }

    @Synchronized
    private fun stopMediaPlayerInternal() {
        val mp = mediaPlayer ?: return
        mediaPlayer = null
        try {
            if (mp.isPlaying) mp.stop()
        } catch (_: Throwable) {
        }
        try {
            mp.release()
        } catch (_: Throwable) {
        }
    }

    fun release() {
        stopMediaPlayerInternal()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Throwable) {
        }
        tts = null
        audioScope.cancel()
    }

    companion object {
        private const val SAMPLE_RATE = 16000

        /**
         * Synthesizes a seamless 16-step looping anime battle music bar tailored to the fight situation.
         */
        fun synthesizeBgmTrackWav(situation: BattleBgmSituation): ByteArray {
            val stepDurationSec = when (situation) {
                BattleBgmSituation.OPENING_TENSION -> 0.24f // ~125 BPM half-time pulse
                BattleBgmSituation.NORMAL_CLASH -> 0.145f   // ~138 BPM shonen groove
                BattleBgmSituation.ULTIMATE_SURGE -> 0.118f // ~169 BPM awakening overdrive
                BattleBgmSituation.CLUTCH_DANGER -> 0.110f  // ~181 BPM low-HP clutch
                BattleBgmSituation.NONE -> 0.10f
            }
            val totalSteps = 16
            val samplesPerStep = (SAMPLE_RATE * stepDurationSec).toInt().coerceAtLeast(256)
            val totalSamples = samplesPerStep * totalSteps
            val pcm = ShortArray(totalSamples)

            // Japanese Yo / Hirajoshi & Shonen heroic scales (Hz)
            val bassNotes = when (situation) {
                BattleBgmSituation.OPENING_TENSION -> doubleArrayOf(
                    110.0, 110.0, 130.81, 110.0, 98.0, 98.0, 123.47, 110.0,
                    110.0, 110.0, 146.83, 130.81, 98.0, 110.0, 123.47, 130.81
                )
                BattleBgmSituation.NORMAL_CLASH -> doubleArrayOf(
                    146.83, 146.83, 174.61, 196.00, 220.00, 220.00, 196.00, 174.61,
                    130.81, 130.81, 146.83, 174.61, 196.00, 174.61, 164.81, 146.83
                )
                BattleBgmSituation.ULTIMATE_SURGE -> doubleArrayOf(
                    174.61, 174.61, 220.00, 261.63, 293.66, 293.66, 261.63, 220.00,
                    196.00, 196.00, 220.00, 261.63, 329.63, 293.66, 261.63, 220.00
                )
                else -> doubleArrayOf(
                    155.56, 155.56, 185.00, 155.56, 146.83, 146.83, 174.61, 146.83,
                    138.59, 138.59, 164.81, 185.00, 196.00, 185.00, 174.61, 155.56
                )
            }

            val leadNotes = when (situation) {
                BattleBgmSituation.OPENING_TENSION -> doubleArrayOf(
                    440.0, 0.0, 523.25, 493.88, 440.0, 0.0, 392.0, 440.0,
                    523.25, 0.0, 587.33, 523.25, 493.88, 440.0, 392.0, 0.0
                )
                BattleBgmSituation.NORMAL_CLASH -> doubleArrayOf(
                    587.33, 698.46, 783.99, 880.00, 783.99, 698.46, 587.33, 523.25,
                    587.33, 698.46, 880.00, 1046.50, 880.00, 783.99, 698.46, 659.25
                )
                BattleBgmSituation.ULTIMATE_SURGE -> doubleArrayOf(
                    698.46, 880.00, 1046.50, 1174.66, 1318.51, 1174.66, 1046.50, 880.00,
                    783.99, 987.77, 1174.66, 1318.51, 1567.98, 1318.51, 1174.66, 1046.50
                )
                else -> doubleArrayOf(
                    622.25, 739.99, 932.33, 739.99, 587.33, 698.46, 880.00, 698.46,
                    554.37, 659.25, 830.61, 932.33, 987.77, 932.33, 880.00, 783.99
                )
            }

            val rng = Random(situation.ordinal * 4099 + 17)
            var bassPhase = 0.0
            var leadPhase = 0.0
            var kickPhase = 0.0

            for (step in 0 until totalSteps) {
                val bassFreq = bassNotes[step % bassNotes.size]
                val leadFreq = leadNotes[step % leadNotes.size]
                val hasKick = (step % 4 == 0) || (situation == BattleBgmSituation.ULTIMATE_SURGE && step % 2 == 0) ||
                    (situation == BattleBgmSituation.CLUTCH_DANGER && (step % 4 == 0 || step % 4 == 3))
                val hasSnare = (step % 4 == 2) && situation != BattleBgmSituation.OPENING_TENSION
                val hasHat = situation != BattleBgmSituation.OPENING_TENSION || (step % 2 == 0)

                if (hasKick) kickPhase = 0.0

                for (s in 0 until samplesPerStep) {
                    val stepT = s.toDouble() / SAMPLE_RATE
                    val stepNorm = s.toDouble() / samplesPerStep

                    // Driving synth bass
                    bassPhase += 2.0 * PI * bassFreq / SAMPLE_RATE
                    val bassEnv = (1.0 - stepNorm * 0.55)
                    val bassSig = (sin(bassPhase) * 0.65 + sin(bassPhase * 2.0) * 0.35) * bassEnv * 0.34

                    // Shonen lead synth
                    val leadSig = if (leadFreq > 1.0) {
                        leadPhase += 2.0 * PI * leadFreq / SAMPLE_RATE
                        val attack = (stepNorm * 14.0).coerceAtMost(1.0)
                        val decay = exp(-stepT * 7.5)
                        (sin(leadPhase) * 0.62 + sin(leadPhase * 1.5) * 0.18 + sin(leadPhase * 2.0) * 0.20) *
                            attack * decay * 0.30
                    } else {
                        0.0
                    }

                    // Taiko / Kick drum
                    val kickSig = if (hasKick) {
                        val kf = 135.0 * exp(-stepT * 28.0) + 46.0
                        kickPhase += 2.0 * PI * kf / SAMPLE_RATE
                        sin(kickPhase) * exp(-stepT * 19.0) * 0.42
                    } else {
                        0.0
                    }

                    // Snare & Hi-hat percussion
                    val snareSig = if (hasSnare) {
                        (rng.nextDouble() * 2.0 - 1.0) * exp(-stepT * 26.0) * 0.24
                    } else {
                        0.0
                    }
                    val hatSig = if (hasHat) {
                        (rng.nextDouble() * 2.0 - 1.0) * exp(-stepT * 62.0) * 0.08
                    } else {
                        0.0
                    }

                    val mixed = (bassSig + leadSig + kickSig + snareSig + hatSig).coerceIn(-0.92, 0.92)
                    pcm[step * samplesPerStep + s] = (mixed * Short.MAX_VALUE).toInt().toShort()
                }
            }

            return SoundManager.encodeMono16BitWav(pcm, SAMPLE_RATE)
        }
    }
}
