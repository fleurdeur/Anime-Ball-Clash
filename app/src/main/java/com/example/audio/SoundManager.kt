package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
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
 * Low-latency polyphonic procedural audio engine for Anime Ball Clash.
 * Synthesizes 16-bit 22.05kHz PCM WAV waveforms on first launch and plays them through
 * Android SoundPool with pitch/volume modulation for collisions, passives, and awakenings.
 * Gracefully degrades to event-counting mode in headless JVM unit tests.
 */
class SoundManager(context: Context?) {

    private var soundPool: SoundPool? = null
    private val soundIds = HashMap<GameSfx, Int>()
    private val loadedSoundIds = HashSet<Int>()
    private val lastPlayMillis = HashMap<GameSfx, Long>()

    @Volatile
    var isEnabled: Boolean = true

    // Exposed counters for deterministic diagnostics and unit tests
    private val playCounts = HashMap<GameSfx, Int>()

    init {
        if (context != null) {
            try {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val pool = SoundPool.Builder()
                    .setMaxStreams(10)
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

                val sfxDir = File(context.cacheDir, "abc_sfx_v2").apply { mkdirs() }
                GameSfx.entries.forEach { sfx ->
                    val wavFile = File(sfxDir, "${sfx.name.lowercase()}.wav")
                    if (!wavFile.exists() || wavFile.length() < 64L) {
                        val pcmBytes = synthesizeSfxWav(sfx)
                        FileOutputStream(wavFile).use { it.write(pcmBytes) }
                    }
                    val id = pool.load(wavFile.absolutePath, 1)
                    if (id > 0) {
                        soundIds[sfx] = id
                    }
                }
            } catch (_: Throwable) {
                // Safe fallback in unit test / headless environments
                soundPool = null
            }
        }
    }

    fun getPlayCount(sfx: GameSfx): Int = synchronized(playCounts) {
        playCounts[sfx] ?: 0
    }

    fun playWallBounce(combo: Int = 1) {
        val pitch = (0.94f + (combo.coerceAtMost(8) * 0.045f)).coerceIn(0.85f, 1.35f)
        play(GameSfx.WALL_BOUNCE, volume = 0.48f, pitch = pitch, minIntervalMs = 42L)
    }

    fun playBallClash(isCrit: Boolean) {
        if (isCrit) {
            val pitch = 0.96f + Random.nextFloat() * 0.10f
            play(GameSfx.CRIT_CLASH, volume = 0.88f, pitch = pitch, minIntervalMs = 60L)
        } else {
            val pitch = 0.92f + Random.nextFloat() * 0.14f
            play(GameSfx.BALL_CLASH, volume = 0.74f, pitch = pitch, minIntervalMs = 55L)
        }
    }

    fun playPassiveTrigger() {
        val pitch = 0.96f + Random.nextFloat() * 0.12f
        play(GameSfx.PASSIVE_TRIGGER, volume = 0.66f, pitch = pitch, minIntervalMs = 130L)
    }

    fun playRedirectDash() {
        play(GameSfx.REDIRECT_DASH, volume = 0.65f, pitch = 1.02f, minIntervalMs = 90L)
    }

    fun playCounterParry() {
        play(GameSfx.COUNTER_PARRY, volume = 0.86f, pitch = 1.04f, minIntervalMs = 90L)
    }

    fun playAwakening(isPlayer: Boolean) {
        val sfx = if (isPlayer) GameSfx.AWAKENING_PLAYER else GameSfx.AWAKENING_AI
        play(sfx, volume = 0.95f, pitch = 1.0f, minIntervalMs = 180L)
    }

    fun playKnockout() {
        play(GameSfx.KNOCKOUT_FINISH, volume = 1.0f, pitch = 0.98f, minIntervalMs = 250L)
    }

    fun playUpgrade() {
        play(GameSfx.UPGRADE_FORGE, volume = 0.82f, pitch = 1.0f, minIntervalMs = 80L)
    }

    fun playUpgradeForge() = playUpgrade()

    fun playUiTap() {
        play(GameSfx.UI_TAP, volume = 0.42f, pitch = 1.05f, minIntervalMs = 45L)
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
        val soundId = soundIds[sfx] ?: return
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
        try {
            soundPool?.release()
        } catch (_: Throwable) {
        }
        soundPool = null
    }

    companion object {
        private const val SAMPLE_RATE = 22050

        fun synthesizeSfxWav(sfx: GameSfx): ByteArray {
            val durationSec = when (sfx) {
                GameSfx.WALL_BOUNCE -> 0.085f
                GameSfx.BALL_CLASH -> 0.16f
                GameSfx.CRIT_CLASH -> 0.26f
                GameSfx.PASSIVE_TRIGGER -> 0.22f
                GameSfx.REDIRECT_DASH -> 0.15f
                GameSfx.COUNTER_PARRY -> 0.28f
                GameSfx.AWAKENING_PLAYER -> 0.46f
                GameSfx.AWAKENING_AI -> 0.44f
                GameSfx.KNOCKOUT_FINISH -> 0.55f
                GameSfx.UPGRADE_FORGE -> 0.30f
                GameSfx.UI_TAP -> 0.055f
            }

            val totalSamples = (SAMPLE_RATE * durationSec).toInt().coerceAtLeast(128)
            val samples = ShortArray(totalSamples)
            val rng = Random(sfx.ordinal * 9973 + 42)
            var phase1 = 0.0
            var phase2 = 0.0
            var phase3 = 0.0

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val norm = i.toDouble() / totalSamples

                val rawSignal: Double = when (sfx) {
                    GameSfx.WALL_BOUNCE -> {
                        val freq = 460.0 * exp(-t * 18.0) + 140.0
                        phase1 += 2.0 * PI * freq / SAMPLE_RATE
                        val env = exp(-t * 28.0)
                        (sin(phase1) * 0.8 + sin(phase1 * 2.4) * 0.25) * env
                    }

                    GameSfx.BALL_CLASH -> {
                        val freq = 220.0 * exp(-t * 16.0) + 58.0
                        phase1 += 2.0 * PI * freq / SAMPLE_RATE
                        val punch = sin(phase1) * exp(-t * 15.0)
                        val snap = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 42.0) * 0.55
                        punch * 0.85 + snap
                    }

                    GameSfx.CRIT_CLASH -> {
                        val subFreq = 280.0 * exp(-t * 14.0) + 52.0
                        val ringFreq = 1180.0 + sin(t * 55.0) * 60.0
                        phase1 += 2.0 * PI * subFreq / SAMPLE_RATE
                        phase2 += 2.0 * PI * ringFreq / SAMPLE_RATE
                        val sub = sin(phase1) * exp(-t * 10.5)
                        val ring = sin(phase2) * exp(-t * 13.0) * 0.42
                        val burst = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 36.0) * 0.48
                        sub * 0.82 + ring + burst
                    }

                    GameSfx.PASSIVE_TRIGGER -> {
                        val stepFreq = when {
                            norm < 0.33 -> 587.33 // D5
                            norm < 0.66 -> 880.00 // A5
                            else -> 1174.66       // D6
                        }
                        phase1 += 2.0 * PI * stepFreq / SAMPLE_RATE
                        phase2 += 2.0 * PI * (stepFreq * 1.5) / SAMPLE_RATE
                        val env = exp(-t * 8.5) * (1.0 - norm * 0.35)
                        (sin(phase1) * 0.72 + sin(phase2) * 0.28) * env
                    }

                    GameSfx.REDIRECT_DASH -> {
                        val sweep = 280.0 + 920.0 * norm * norm
                        phase1 += 2.0 * PI * sweep / SAMPLE_RATE
                        val whoosh = (rng.nextDouble() * 2.0 - 1.0) * 0.32
                        val env = sin(PI * norm)
                        (sin(phase1) * 0.68 + whoosh) * env
                    }

                    GameSfx.COUNTER_PARRY -> {
                        // Crisp anime blade/shield parry clang + harmonic time-warp shimmer
                        val clangFreq = 987.77 + 440.0 * exp(-t * 22.0)
                        val subFreq = 493.88
                        phase1 += 2.0 * PI * clangFreq / SAMPLE_RATE
                        phase2 += 2.0 * PI * subFreq / SAMPLE_RATE
                        phase3 += 2.0 * PI * 1975.53 / SAMPLE_RATE
                        val transient = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 55.0) * 0.45
                        val env = exp(-t * 7.8)
                        (sin(phase1) * 0.55 + sin(phase2) * 0.28 + sin(phase3) * 0.22) * env + transient
                    }

                    GameSfx.AWAKENING_PLAYER -> {
                        // Heroic anime aura surge: rising harmonic triad + shimmer
                        val root = 220.0 + 440.0 * norm
                        phase1 += 2.0 * PI * root / SAMPLE_RATE
                        phase2 += 2.0 * PI * (root * 1.25) / SAMPLE_RATE
                        phase3 += 2.0 * PI * (root * 1.5) / SAMPLE_RATE
                        val env = (1.0 - exp(-t * 32.0)) * exp(-t * 3.2)
                        val spark = sin(t * 2.0 * PI * 1760.0) * 0.16 * exp(-t * 5.0)
                        (sin(phase1) * 0.48 + sin(phase2) * 0.30 + sin(phase3) * 0.28 + spark) * env
                    }

                    GameSfx.AWAKENING_AI -> {
                        // Rival dark awakening: ominous minor surge
                        val root = 174.61 + 260.0 * norm
                        phase1 += 2.0 * PI * root / SAMPLE_RATE
                        phase2 += 2.0 * PI * (root * 1.189) / SAMPLE_RATE
                        phase3 += 2.0 * PI * (root * 1.498) / SAMPLE_RATE
                        val env = (1.0 - exp(-t * 28.0)) * exp(-t * 3.4)
                        (sin(phase1) * 0.52 + sin(phase2) * 0.30 + sin(phase3) * 0.26) * env
                    }

                    GameSfx.KNOCKOUT_FINISH -> {
                        val boomFreq = 150.0 * exp(-t * 7.5) + 42.0
                        phase1 += 2.0 * PI * boomFreq / SAMPLE_RATE
                        phase2 += 2.0 * PI * 523.25 / SAMPLE_RATE
                        val boom = sin(phase1) * exp(-t * 4.8)
                        val bell = sin(phase2) * exp(-t * 5.5) * 0.35
                        val impact = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 26.0) * 0.45
                        boom * 0.78 + bell + impact
                    }

                    GameSfx.UPGRADE_FORGE -> {
                        val note = when {
                            norm < 0.28 -> 523.25 // C5
                            norm < 0.56 -> 659.25 // E5
                            else -> 1046.50       // C6
                        }
                        phase1 += 2.0 * PI * note / SAMPLE_RATE
                        phase2 += 2.0 * PI * (note * 2.0) / SAMPLE_RATE
                        val anvil = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 45.0) * 0.35
                        val env = exp(-t * 5.5)
                        (sin(phase1) * 0.65 + sin(phase2) * 0.25) * env + anvil
                    }

                    GameSfx.UI_TAP -> {
                        phase1 += 2.0 * PI * 760.0 / SAMPLE_RATE
                        sin(phase1) * exp(-t * 48.0) * 0.65
                    }
                }

                val clamped = rawSignal.coerceIn(-0.98, 0.98)
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
                val v = sample.toInt()
                out.write(v and 0xFF)
                out.write((v ushr 8) and 0xFF)
            }
            return out.toByteArray()
        }
    }
}
