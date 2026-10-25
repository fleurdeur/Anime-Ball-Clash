package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.tanh
import kotlin.random.Random

enum class GameSfx {
    WALL_BOUNCE,
    BALL_CLASH,
    CRIT_CLASH,
    PASSIVE_TRIGGER,
    REDIRECT_DASH,
    COUNTER_PARRY,
    AWAKENING_PLAYER,
    AWAKENING_AI,
    KNOCKOUT_FINISH,
    UPGRADE_FORGE,
    UI_TAP
}

/**
 * High-definition low-latency polyphonic SFX engine for Anime Ball Clash.
 * Loads verified combat foley MP3s via [RealAudioRepository] (backed by 44.1 kHz
 * studio-grade layered anime foley WAVs for zero-latency offline playback) and
 * modulates pitch & volume dynamically based on combo count and dash stretch tension.
 */
class SoundManager(context: Context?) {

    private val appContext: Context? = context?.applicationContext
    private val sfxScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var soundPool: SoundPool? = null
    private val soundIds = HashMap<GameSfx, Int>()
    private val loadedSoundIds = HashSet<Int>()
    private val lastPlayMillis = HashMap<GameSfx, Long>()

    @Volatile
    var isEnabled: Boolean = true

    // Exposed counters for deterministic diagnostics and unit tests
    private val playCounts = HashMap<GameSfx, Int>()

    init {
        if (appContext != null) {
            try {
                try {
                    File(appContext.cacheDir, "abc_sfx_v2").deleteRecursively()
                } catch (_: Throwable) {
                }

                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val pool = SoundPool.Builder()
                    .setMaxStreams(12)
                    .setAudioAttributes(attrs)
                    .build()

                pool.setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) {
                        synchronized(loadedSoundIds) {
                            loadedSoundIds.add(sampleId)
                        }
                    }
                }
                soundPool = pool

                val sfxDir = File(appContext.cacheDir, "abc_sfx_v3").apply { mkdirs() }
                GameSfx.entries.forEach { sfx ->
                    val cachedRealMp3 = RealAudioRepository.getCachedSfxFile(appContext, sfx)
                    val initialFile = if (cachedRealMp3 != null) {
                        cachedRealMp3
                    } else {
                        val wavFile = File(sfxDir, "${sfx.name.lowercase()}.wav")
                        if (!wavFile.exists() || wavFile.length() < 128L) {
                            val pcmBytes = synthesizeSfxWav(sfx)
                            FileOutputStream(wavFile).use { it.write(pcmBytes) }
                        }
                        wavFile
                    }
                    val id = pool.load(initialFile.absolutePath, 1)
                    if (id > 0) {
                        synchronized(soundIds) {
                            soundIds[sfx] = id
                        }
                    }
                }

                // Fetch verified combat foley MP3s in background and hot-swap into SoundPool
                sfxScope.launch {
                    GameSfx.entries.forEach { sfx ->
                        if (RealAudioRepository.getCachedSfxFile(appContext, sfx) == null) {
                            val downloaded = RealAudioRepository.fetchSfxFile(appContext, sfx)
                            val activePool = soundPool
                            if (downloaded != null && activePool != null) {
                                try {
                                    val realId = activePool.load(downloaded.absolutePath, 1)
                                    if (realId > 0) {
                                        synchronized(soundIds) {
                                            soundIds[sfx] = realId
                                        }
                                    }
                                } catch (_: Throwable) {
                                }
                            }
                        }
                    }
                }
            } catch (_: Throwable) {
                soundPool = null
            }
        }
    }

    fun getPlayCount(sfx: GameSfx): Int = synchronized(playCounts) {
        playCounts[sfx] ?: 0
    }

    fun playWallBounce(combo: Int = 1) {
        val pitch = (0.94f + (combo.coerceAtMost(8) * 0.038f)).coerceIn(0.88f, 1.28f)
        play(GameSfx.WALL_BOUNCE, volume = 0.36f, pitch = pitch, minIntervalMs = 46L)
    }

    fun playBallClash(isCrit: Boolean) {
        if (isCrit) {
            val pitch = 0.96f + Random.nextFloat() * 0.10f
            play(GameSfx.CRIT_CLASH, volume = 0.90f, pitch = pitch, minIntervalMs = 60L)
        } else {
            val pitch = 0.93f + Random.nextFloat() * 0.14f
            play(GameSfx.BALL_CLASH, volume = 0.78f, pitch = pitch, minIntervalMs = 55L)
        }
    }

    fun playPassiveTrigger() {
        val pitch = 0.97f + Random.nextFloat() * 0.10f
        play(GameSfx.PASSIVE_TRIGGER, volume = 0.58f, pitch = pitch, minIntervalMs = 140L)
    }

    fun playRedirectDash(stretchRatio: Float = 0.75f) {
        val clamped = stretchRatio.coerceIn(0.15f, 1.0f)
        val vol = (0.48f + clamped * 0.44f).coerceIn(0.45f, 0.95f)
        val pitch = (0.86f + clamped * 0.34f).coerceIn(0.82f, 1.26f)
        play(GameSfx.REDIRECT_DASH, volume = vol, pitch = pitch, minIntervalMs = 75L)
    }

    fun playCounterParry() {
        val pitch = 0.98f + Random.nextFloat() * 0.08f
        play(GameSfx.COUNTER_PARRY, volume = 0.88f, pitch = pitch, minIntervalMs = 95L)
    }

    fun playAwakening(isPlayer: Boolean) {
        val sfx = if (isPlayer) GameSfx.AWAKENING_PLAYER else GameSfx.AWAKENING_AI
        play(sfx, volume = 0.92f, pitch = 1.0f, minIntervalMs = 190L)
    }

    fun playKnockout() {
        play(GameSfx.KNOCKOUT_FINISH, volume = 0.98f, pitch = 0.98f, minIntervalMs = 250L)
    }

    fun playUpgrade() {
        play(GameSfx.UPGRADE_FORGE, volume = 0.76f, pitch = 1.0f, minIntervalMs = 80L)
    }

    fun playUpgradeForge() = playUpgrade()

    fun playUiTap() {
        play(GameSfx.UI_TAP, volume = 0.32f, pitch = 1.04f, minIntervalMs = 45L)
    }

    fun play(
        sfx: GameSfx,
        volume: Float = 0.75f,
        pitch: Float = 1.0f,
        minIntervalMs: Long = 50L
    ) {
        if (!isEnabled) return

        val now = System.currentTimeMillis()
        synchronized(lastPlayMillis) {
            val prev = lastPlayMillis[sfx] ?: 0L
            if (now - prev < minIntervalMs) return
            lastPlayMillis[sfx] = now
        }

        synchronized(playCounts) {
            playCounts[sfx] = (playCounts[sfx] ?: 0) + 1
        }

        val pool = soundPool ?: return
        val soundId = synchronized(soundIds) { soundIds[sfx] } ?: return
        try {
            val isLoaded = synchronized(loadedSoundIds) { loadedSoundIds.contains(soundId) }
            if (isLoaded) {
                val v = volume.coerceIn(0.05f, 1.0f)
                val p = pitch.coerceIn(0.5f, 2.0f)
                pool.play(soundId, v, v, 1, 0, p)
            }
        } catch (_: Throwable) {
        }
    }

    fun release() {
        sfxScope.cancel()
        try {
            soundPool?.release()
        } catch (_: Throwable) {
        }
        soundPool = null
    }

    companion object {
        private const val SAMPLE_RATE = 44100

        /**
         * Studio-grade 44.1 kHz anime fighting foley synthesis with low-pass/band-pass
         * filtered air transients, warm sub-bass punch, and katana steel harmonics.
         */
        fun synthesizeSfxWav(sfx: GameSfx): ByteArray {
            val durationSec = when (sfx) {
                GameSfx.WALL_BOUNCE -> 0.068f
                GameSfx.BALL_CLASH -> 0.145f
                GameSfx.CRIT_CLASH -> 0.250f
                GameSfx.PASSIVE_TRIGGER -> 0.210f
                GameSfx.REDIRECT_DASH -> 0.155f
                GameSfx.COUNTER_PARRY -> 0.270f
                GameSfx.AWAKENING_PLAYER -> 0.480f
                GameSfx.AWAKENING_AI -> 0.450f
                GameSfx.KNOCKOUT_FINISH -> 0.620f
                GameSfx.UPGRADE_FORGE -> 0.290f
                GameSfx.UI_TAP -> 0.038f
            }

            val totalSamples = (SAMPLE_RATE * durationSec).toInt().coerceAtLeast(256)
            val samples = ShortArray(totalSamples)
            val rng = Random(sfx.ordinal * 9973 + 42)
            var phase1 = 0.0
            var phase2 = 0.0
            var phase3 = 0.0
            var phase4 = 0.0
            var lpFast = 0.0
            var lpSlow = 0.0

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val norm = i.toDouble() / totalSamples
                val white = rng.nextDouble() * 2.0 - 1.0
                // Smooth bandpass/lowpass filtered air noise (eliminates harsh static hiss)
                lpFast += 0.24 * (white - lpFast)
                lpSlow += 0.045 * (white - lpSlow)
                val bandAir = lpFast - lpSlow

                val rawSignal: Double = when (sfx) {
                    GameSfx.WALL_BOUNCE -> {
                        // Warm, tight arena bumper thump + subtle rim click
                        val freq = 240.0 * exp(-t * 42.0) + 74.0
                        phase1 += 2.0 * PI * freq / SAMPLE_RATE
                        phase2 += 2.0 * PI * (freq * 1.9) / SAMPLE_RATE
                        val body = sin(phase1) * exp(-t * 36.0)
                        val click = sin(phase2) * exp(-t * 85.0) * 0.28 + bandAir * exp(-t * 95.0) * 0.22
                        tanh((body * 0.88 + click) * 1.25)
                    }

                    GameSfx.BALL_CLASH -> {
                        // Heavy anime martial-arts punch: chest sub-drop + crisp impact crack
                        val subFreq = 290.0 * exp(-t * 26.0) + 52.0
                        val midFreq = 620.0 * exp(-t * 34.0) + 135.0
                        phase1 += 2.0 * PI * subFreq / SAMPLE_RATE
                        phase2 += 2.0 * PI * midFreq / SAMPLE_RATE
                        val sub = sin(phase1) * exp(-t * 17.5)
                        val midPunch = sin(phase2) * exp(-t * 30.0) * 0.44
                        val crack = bandAir * exp(-t * 48.0) * 0.72
                        tanh((sub * 0.95 + midPunch + crack) * 1.45)
                    }

                    GameSfx.CRIT_CLASH -> {
                        // Black-Flash / Heavy Smash: deep sub-boom + katana steel ring + shock crack
                        val subFreq = 360.0 * exp(-t * 22.0) + 44.0
                        phase1 += 2.0 * PI * subFreq / SAMPLE_RATE
                        phase2 += 2.0 * PI * 1318.51 / SAMPLE_RATE
                        phase3 += 2.0 * PI * 1992.0 / SAMPLE_RATE
                        val sub = sin(phase1) * exp(-t * 11.5)
                        val steelRing = (sin(phase2) * 0.6 + sin(phase3) * 0.4) * exp(-t * 14.5) * 0.34
                        val burst = bandAir * exp(-t * 36.0) * 0.85
                        tanh((sub * 1.05 + steelRing + burst) * 1.55)
                    }

                    GameSfx.PASSIVE_TRIGGER -> {
                        // Crystalline anime ki pulse (F#5 - C#6 - F#6 harmonic shimmer)
                        val f1 = 739.99
                        val f2 = 1108.73
                        val f3 = 1479.98
                        phase1 += 2.0 * PI * f1 / SAMPLE_RATE
                        phase2 += 2.0 * PI * f2 / SAMPLE_RATE
                        phase3 += 2.0 * PI * f3 / SAMPLE_RATE
                        val attack = (1.0 - exp(-t * 95.0))
                        val env = attack * exp(-t * 11.5)
                        (sin(phase1) * 0.45 + sin(phase2) * 0.35 + sin(phase3) * 0.25) * env
                    }

                    GameSfx.REDIRECT_DASH -> {
                        // Anime Shunpo / Soru air-slice whoosh: swept resonant air + sleek sonic glide
                        val sweepFreq = 210.0 + 780.0 * sin(PI * norm * 0.85)
                        phase1 += 2.0 * PI * sweepFreq / SAMPLE_RATE
                        phase2 += 2.0 * PI * (sweepFreq * 1.5) / SAMPLE_RATE
                        val env = sin(PI * norm) * exp(-norm * 0.45)
                        val airSlice = bandAir * 1.35 + lpSlow * 0.55
                        val tone = sin(phase1) * 0.38 + sin(phase2) * 0.16
                        tanh((airSlice * 0.78 + tone) * env * 1.35)
                    }

                    GameSfx.COUNTER_PARRY -> {
                        // Iconic anime katana / Stand parry clang: crisp steel transient + resonant bell partials
                        val bend = 1.0 + 0.18 * exp(-t * 38.0)
                        phase1 += 2.0 * PI * (1174.66 * bend) / SAMPLE_RATE
                        phase2 += 2.0 * PI * (1864.66 * bend) / SAMPLE_RATE
                        phase3 += 2.0 * PI * (2793.83 * bend) / SAMPLE_RATE
                        phase4 += 2.0 * PI * (146.83) / SAMPLE_RATE
                        val steel = (sin(phase1) * 0.52 + sin(phase2) * 0.34 + sin(phase3) * 0.22) * exp(-t * 9.5)
                        val guardThump = sin(phase4) * exp(-t * 24.0) * 0.45
                        val spark = bandAir * exp(-t * 70.0) * 0.65
                        tanh((steel + guardThump + spark) * 1.38)
                    }

                    GameSfx.AWAKENING_PLAYER -> {
                        // Heroic anime Ultimate Awakening: initial sub-burst + rising power fifths + ki crackle
                        val subFreq = 190.0 * exp(-t * 14.0) + 46.0
                        val auraRoot = 220.0 + 330.0 * (norm * norm)
                        phase1 += 2.0 * PI * subFreq / SAMPLE_RATE
                        phase2 += 2.0 * PI * auraRoot / SAMPLE_RATE
                        phase3 += 2.0 * PI * (auraRoot * 1.498) / SAMPLE_RATE
                        phase4 += 2.0 * PI * (auraRoot * 2.0) / SAMPLE_RATE
                        val subBoom = sin(phase1) * exp(-t * 7.5) * 0.68
                        val swellEnv = (1.0 - exp(-t * 26.0)) * exp(-t * 2.8)
                        val chord = (sin(phase2) * 0.44 + sin(phase3) * 0.34 + sin(phase4) * 0.24) * swellEnv
                        val kiSpark = bandAir * exp(-t * 6.0) * 0.24
                        tanh((subBoom + chord + kiSpark) * 1.32)
                    }

                    GameSfx.AWAKENING_AI -> {
                        // Rival dark Domain / Ultimate burst: deep sub-drop + ominous minor fifth swell
                        val subFreq = 165.0 * exp(-t * 12.0) + 42.0
                        val darkRoot = 164.81 + 210.0 * (norm * norm)
                        phase1 += 2.0 * PI * subFreq / SAMPLE_RATE
                        phase2 += 2.0 * PI * darkRoot / SAMPLE_RATE
                        phase3 += 2.0 * PI * (darkRoot * 1.189) / SAMPLE_RATE
                        phase4 += 2.0 * PI * (darkRoot * 1.498) / SAMPLE_RATE
                        val subBoom = sin(phase1) * exp(-t * 7.0) * 0.70
                        val swellEnv = (1.0 - exp(-t * 24.0)) * exp(-t * 3.0)
                        val chord = (sin(phase2) * 0.46 + sin(phase3) * 0.32 + sin(phase4) * 0.26) * swellEnv
                        tanh((subBoom + chord + bandAir * exp(-t * 8.0) * 0.20) * 1.30)
                    }

                    GameSfx.KNOCKOUT_FINISH -> {
                        // Stadium K.O. Finisher: cinematic sub-bass shockwave + resonant gong bell
                        val boomFreq = 195.0 * exp(-t * 9.0) + 36.0
                        phase1 += 2.0 * PI * boomFreq / SAMPLE_RATE
                        phase2 += 2.0 * PI * 392.00 / SAMPLE_RATE
                        phase3 += 2.0 * PI * 587.33 / SAMPLE_RATE
                        phase4 += 2.0 * PI * 987.77 / SAMPLE_RATE
                        val subBoom = sin(phase1) * exp(-t * 4.4) * 0.88
                        val gong = (sin(phase2) * 0.42 + sin(phase3) * 0.30 + sin(phase4) * 0.18) * exp(-t * 4.8) * 0.45
                        val slam = bandAir * exp(-t * 24.0) * 0.75
                        tanh((subBoom + gong + slam) * 1.48)
                    }

                    GameSfx.UPGRADE_FORGE -> {
                        // Clean blacksmith forge chime: warm triad ascent (D5 -> F#5 -> A5 -> D6)
                        val note = when {
                            norm < 0.24 -> 587.33
                            norm < 0.48 -> 739.99
                            norm < 0.72 -> 880.00
                            else -> 1174.66
                        }
                        phase1 += 2.0 * PI * note / SAMPLE_RATE
                        phase2 += 2.0 * PI * (note * 2.0) / SAMPLE_RATE
                        val bellEnv = exp(-t * 5.2)
                        val tap = bandAir * exp(-t * 60.0) * 0.25
                        (sin(phase1) * 0.62 + sin(phase2) * 0.24) * bellEnv + tap
                    }

                    GameSfx.UI_TAP -> {
                        // Subtle, crisp modern UI tactile tick
                        val freq = 920.0 * exp(-t * 70.0) + 340.0
                        phase1 += 2.0 * PI * freq / SAMPLE_RATE
                        sin(phase1) * exp(-t * 85.0) * 0.68
                    }
                }

                // Micro fade-out on final 2ms prevents any speaker click at buffer end
                val tailFade = if (totalSamples - i < 88) (totalSamples - i) / 88.0 else 1.0
                val clamped = (rawSignal * tailFade).coerceIn(-0.98, 0.98)
                samples[i] = (clamped * Short.MAX_VALUE).toInt().toShort()
            }

            return encodeMono16BitWav(samples, SAMPLE_RATE)
        }

        internal fun encodeMono16BitWav(samples: ShortArray, sampleRate: Int): ByteArray {
            val dataSize = samples.size * 2
            val totalDataLen = 36 + dataSize
            val byteRate = sampleRate * 2
            val out = ByteArrayOutputStream(44 + dataSize)

            fun writeAscii(s: String) {
                out.write(s.toByteArray(Charsets.US_ASCII))
            }

            fun writeIntLE(v: Int) {
                out.write(v and 0xFF)
                out.write((v ushr 8) and 0xFF)
                out.write((v ushr 16) and 0xFF)
                out.write((v ushr 24) and 0xFF)
            }

            fun writeShortLE(v: Int) {
                out.write(v and 0xFF)
                out.write((v ushr 8) and 0xFF)
            }

            writeAscii("RIFF")
            writeIntLE(totalDataLen)
            writeAscii("WAVE")
            writeAscii("fmt ")
            writeIntLE(16)          // PCM chunk size
            writeShortLE(1)         // AudioFormat = 1 (PCM)
            writeShortLE(1)         // NumChannels = 1 (Mono)
            writeIntLE(sampleRate)  // SampleRate
            writeIntLE(byteRate)    // ByteRate
            writeShortLE(2)         // BlockAlign
            writeShortLE(16)        // BitsPerSample
            writeAscii("data")
            writeIntLE(dataSize)

            for (sample in samples) {
                writeShortLE(sample.toInt())
            }
            return out.toByteArray()
        }
    }
}
