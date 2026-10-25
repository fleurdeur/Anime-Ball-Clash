package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.model.CharacterData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
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
 * Directs situational fighting Background Music (BGM) and plays each character's
 * official recorded Ultimate anime voice audio clip when Ultimate mode activates.
 *
 * - BGM dynamically shifts across 4 live combat states using real Mixkit / Internet Archive
 *   MP3 tracks cached via [RealAudioRepository] (with offline acoustic WAV fallback):
 *   1. OPENING_TENSION (Pre-launch / Aiming)
 *   2. NORMAL_CLASH (Standard high-energy duel)
 *   3. ULTIMATE_SURGE (Either fighter in Ultimate Awakening!)
 *   4. CLUTCH_DANGER (Low HP < 32% clutch showdown!)
 * - Ultimate Voice Audio plays genuine recorded anime voice MP3 clips cached per character
 *   through [MediaPlayer] while ducking BGM volume. Strictly zero synthetic or AI voice is ever used.
 */
class BattleAudioDirector(context: Context?) {

    private val appContext: Context? = context?.applicationContext
    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var _voiceEnabled: Boolean = true

    var sfxEnabled: Boolean
        get() = _voiceEnabled
        set(value) {
            _voiceEnabled = value
            if (!value) {
                stopVoicePlayerInternal()
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
    private var voicePlayer: MediaPlayer? = null
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
            audioScope.launch {
                prepareBgmTracks(appContext)
            }
        }
    }

    /**
     * Pre-fetches official recorded anime Ultimate voice MP3 clips for selected fighters
     * in the background so playback is instantaneous when Ultimate triggers.
     */
    fun prefetchCharacterVoices(vararg characters: CharacterData) {
        val ctx = appContext ?: return
        val distinctChars = characters.distinctBy { it.id }
        audioScope.launch {
            distinctChars.forEach { character ->
                try {
                    RealAudioRepository.fetchOfficialUltVoiceFile(ctx, character)
                } catch (_: Throwable) {
                }
            }
        }
    }

    private fun prepareBgmTracks(ctx: Context) {
        val situations = listOf(
            BattleBgmSituation.OPENING_TENSION,
            BattleBgmSituation.NORMAL_CLASH,
            BattleBgmSituation.ULTIMATE_SURGE,
            BattleBgmSituation.CLUTCH_DANGER
        )
        try {
            val bgmDir = File(ctx.cacheDir, "abc_bgm_v1").apply { mkdirs() }
            for (sit in situations) {
                val cachedRealMp3 = RealAudioRepository.getCachedBgmFile(ctx, sit)
                val initialTrack = if (cachedRealMp3 != null) {
                    cachedRealMp3
                } else {
                    val file = File(bgmDir, "${sit.name.lowercase()}.wav")
                    if (!file.exists() || file.length() < 1024L) {
                        val wavBytes = synthesizeBgmTrackWav(sit)
                        FileOutputStream(file).use { it.write(wavBytes) }
                    }
                    file
                }
                synchronized(trackFiles) {
                    trackFiles[sit] = initialTrack
                }
            }
            tracksPrepared = true
            val desired = currentSituation
            if (desired != BattleBgmSituation.NONE && _musicEnabled && isAppForeground) {
                switchMediaPlayerTo(desired)
            }
        } catch (_: Throwable) {
            tracksPrepared = false
        }

        // Download real Mixkit / Internet Archive BGM MP3s and seamlessly upgrade tracks
        for (sit in situations) {
            if (RealAudioRepository.getCachedBgmFile(ctx, sit) == null) {
                audioScope.launch {
                    try {
                        val realMp3 = RealAudioRepository.fetchBgmFile(ctx, sit)
                        if (realMp3 != null) {
                            synchronized(trackFiles) {
                                trackFiles[sit] = realMp3
                            }
                            if (currentSituation == sit && _musicEnabled && isAppForeground) {
                                switchMediaPlayerTo(sit)
                            }
                        }
                    } catch (_: Throwable) {
                    }
                }
            }
        }
    }

    /**
     * Plays the character's official recorded Ultimate anime voice audio clip when Ultimate activates!
     * Strictly uses real recorded audio files via MediaPlayer — never synthetic speech.
     */
    fun speakUltimateVoiceLine(character: CharacterData, isPlayer: Boolean) {
        if (!_voiceEnabled) return
        val line = character.officialUltVoiceLine.ifBlank { character.voiceLine }
        spokenVoiceCount++
        lastSpokenLine = line

        val ctx = appContext ?: return
        audioScope.launch {
            val voiceFile = RealAudioRepository.getCachedVoiceFile(ctx, character.id)
                ?: RealAudioRepository.fetchOfficialUltVoiceFile(ctx, character)
                ?: return@launch

            if (!_voiceEnabled || !isAppForeground) return@launch
            playOfficialVoiceClip(voiceFile, isPlayer)
        }
    }

    @Synchronized
    private fun playOfficialVoiceClip(file: File, isPlayer: Boolean) {
        if (!_voiceEnabled || !isAppForeground || !file.exists()) return
        stopVoicePlayerInternal()
        try {
            val vp = MediaPlayer()
            vp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            vp.setDataSource(file.absolutePath)
            val vol = if (isPlayer) 1.0f else 0.90f
            vp.setVolume(vol, vol)
            vp.setOnCompletionListener {
                stopVoicePlayerInternal()
            }
            vp.setOnErrorListener { _, _, _ ->
                stopVoicePlayerInternal()
                true
            }
            vp.prepare()
            voiceDuckingActive = true
            applyCurrentPlayerVolume()
            vp.start()
            voicePlayer = vp
        } catch (_: Throwable) {
            stopVoicePlayerInternal()
        }
    }

    @Synchronized
    private fun stopVoicePlayerInternal() {
        val vp = voicePlayer
        voicePlayer = null
        if (voiceDuckingActive) {
            voiceDuckingActive = false
            applyCurrentPlayerVolume()
        }
        if (vp != null) {
            try {
                if (vp.isPlaying) vp.stop()
            } catch (_: Throwable) {
            }
            try {
                vp.release()
            } catch (_: Throwable) {
            }
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
        stopVoicePlayerInternal()
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
        return if (voiceDuckingActive) base * 0.36f else base
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
        stopVoicePlayerInternal()
        stopMediaPlayerInternal()
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
