package com.example.audio

import android.content.Context
import com.example.model.CharacterData
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

/**
 * Fetches, validates, and disk-caches real MP3/OGG sound effects, background music tracks,
 * and official recorded anime character Ultimate voice clips from public web repositories
 * (Mixkit CDN, MyInstants, SoundInstants, Internet Archive).
 *
 * STRICTLY ZERO AI TEXT-TO-SPEECH OR SYNTHETIC VOICE IS EVER USED HERE OR ANYWHERE IN THE APP.
 */
object RealAudioRepository {

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

    private val inFlightDownloads = ConcurrentHashMap<String, Boolean>()
    private val scrapedCategoryCache = ConcurrentHashMap<String, List<String>>()

    private fun rootDir(context: Context): File =
        File(context.filesDir, "real_audio_v2").apply { mkdirs() }

    fun getCachedSfxFile(context: Context, sfx: GameSfx): File? {
        val f = File(File(rootDir(context), "sfx").apply { mkdirs() }, "${sfx.name.lowercase()}.mp3")
        return if (f.exists() && f.length() > 1024L) f else null
    }

    fun getCachedBgmFile(context: Context, situation: BattleBgmSituation): File? {
        val f = File(File(rootDir(context), "bgm").apply { mkdirs() }, "${situation.name.lowercase()}.mp3")
        return if (f.exists() && f.length() > 4096L) f else null
    }

    fun getCachedVoiceFile(context: Context, characterId: String): File? {
        val safeId = characterId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val f = File(File(rootDir(context), "voices").apply { mkdirs() }, "$safeId.mp3")
        return if (f.exists() && f.length() > 1200L) f else null
    }

    /**
     * Downloads and caches real Mixkit / Internet Archive sound effects for combat & UI.
     */
    fun fetchSfxFile(context: Context, sfx: GameSfx): File? {
        getCachedSfxFile(context, sfx)?.let { return it }
        val lockKey = "sfx_${sfx.name}"
        if (inFlightDownloads.putIfAbsent(lockKey, true) != null) return null

        try {
            val dest = File(File(rootDir(context), "sfx").apply { mkdirs() }, "${sfx.name.lowercase()}.mp3")
            val directUrls = directMixkitSfxUrls(sfx)
            for (url in directUrls) {
                if (downloadValidAudioToFile(url, dest, minBytes = 1024)) {
                    return dest
                }
            }

            // Fallback: scrape live Mixkit free-sound-effects category page for fresh CDN preview MP3s
            val categorySlug = when (sfx) {
                GameSfx.WALL_BOUNCE, GameSfx.UI_TAP -> "click"
                GameSfx.BALL_CLASH, GameSfx.CRIT_CLASH -> "punch"
                GameSfx.COUNTER_PARRY, GameSfx.REDIRECT_DASH -> "sword"
                GameSfx.AWAKENING_PLAYER, GameSfx.AWAKENING_AI, GameSfx.PASSIVE_TRIGGER -> "electricity"
                GameSfx.KNOCKOUT_FINISH -> "explosion"
                GameSfx.UPGRADE_FORGE -> "win"
            }
            val scraped = scrapeMixkitMp3Links("https://mixkit.co/free-sound-effects/$categorySlug/")
            for ((idx, url) in scraped.withIndex()) {
                if (idx >= 4) break
                if (downloadValidAudioToFile(url, dest, minBytes = 1024)) {
                    return dest
                }
            }

            // Secondary fallback: Internet Archive CC0 / public-domain sound effects
            val archiveQuery = when (sfx) {
                GameSfx.WALL_BOUNCE -> "bounce impact sound effect"
                GameSfx.BALL_CLASH -> "punch hit sound effect"
                GameSfx.CRIT_CLASH -> "heavy anime impact sound effect"
                GameSfx.PASSIVE_TRIGGER -> "magic power up sound effect"
                GameSfx.REDIRECT_DASH -> "whoosh dash sound effect"
                GameSfx.COUNTER_PARRY -> "sword parry clang sound effect"
                GameSfx.AWAKENING_PLAYER, GameSfx.AWAKENING_AI -> "anime aura powerup sound effect"
                GameSfx.KNOCKOUT_FINISH -> "explosion boom impact sound effect"
                GameSfx.UPGRADE_FORGE -> "level up chime sound effect"
                GameSfx.UI_TAP -> "button click sound effect"
            }
            val archiveUrls = searchInternetArchiveAudioMp3s(archiveQuery, maxItems = 2)
            for (url in archiveUrls) {
                if (downloadValidAudioToFile(url, dest, minBytes = 1024)) {
                    return dest
                }
            }
            return null
        } finally {
            inFlightDownloads.remove(lockKey)
        }
    }

    /**
     * Downloads and caches real background music tracks from Mixkit Action/Rock stock music & Internet Archive.
     */
    fun fetchBgmFile(context: Context, situation: BattleBgmSituation): File? {
        if (situation == BattleBgmSituation.NONE) return null
        getCachedBgmFile(context, situation)?.let { return it }
        val lockKey = "bgm_${situation.name}"
        if (inFlightDownloads.putIfAbsent(lockKey, true) != null) return null

        try {
            val dest = File(File(rootDir(context), "bgm").apply { mkdirs() }, "${situation.name.lowercase()}.mp3")

            // 1. Try direct verified Mixkit stock music preview MP3s
            for (url in directMixkitBgmUrls(situation)) {
                if (downloadValidAudioToFile(url, dest, minBytes = 8192)) {
                    return dest
                }
            }

            // 2. Scrape live Mixkit stock music category pages (each situation picks a distinct track index)
            val categoryUrl = when (situation) {
                BattleBgmSituation.OPENING_TENSION -> "https://mixkit.co/free-stock-music/cinematic/"
                BattleBgmSituation.NORMAL_CLASH -> "https://mixkit.co/free-stock-music/action/"
                BattleBgmSituation.ULTIMATE_SURGE -> "https://mixkit.co/free-stock-music/rock/"
                BattleBgmSituation.CLUTCH_DANGER -> "https://mixkit.co/free-stock-music/trap/"
                BattleBgmSituation.NONE -> ""
            }
            if (categoryUrl.isNotEmpty()) {
                val tracks = scrapeMixkitMp3Links(categoryUrl)
                val offset = situation.ordinal % (tracks.size.coerceAtLeast(1))
                val ordered = if (tracks.isNotEmpty()) {
                    tracks.drop(offset) + tracks.take(offset)
                } else {
                    emptyList()
                }
                for (url in ordered.take(4)) {
                    if (downloadValidAudioToFile(url, dest, minBytes = 8192)) {
                        return dest
                    }
                }
            }

            // 3. Fallback: Internet Archive battle / rock / action instrumental BGM
            val archiveQuery = when (situation) {
                BattleBgmSituation.OPENING_TENSION -> "anime battle intro tension instrumental"
                BattleBgmSituation.NORMAL_CLASH -> "anime action fight rock bgm"
                BattleBgmSituation.ULTIMATE_SURGE -> "epic boss battle rock theme"
                BattleBgmSituation.CLUTCH_DANGER -> "fast intense battle music"
                BattleBgmSituation.NONE -> ""
            }
            if (archiveQuery.isNotEmpty()) {
                for (url in searchInternetArchiveAudioMp3s(archiveQuery, maxItems = 3)) {
                    if (downloadValidAudioToFile(url, dest, minBytes = 8192)) {
                        return dest
                    }
                }
            }

            return null
        } finally {
            inFlightDownloads.remove(lockKey)
        }
    }

    /**
     * Scavenges and caches official recorded anime character Ultimate voice clips from
     * MyInstants, SoundInstants, and Internet Archive audio collections.
     *
     * STRICTLY NO AI TTS OR SYNTHETIC VOICE GENERATION.
     */
    fun fetchOfficialUltVoiceFile(context: Context, character: CharacterData): File? {
        getCachedVoiceFile(context, character.id)?.let { return it }
        val lockKey = "voice_${character.id}"
        if (inFlightDownloads.putIfAbsent(lockKey, true) != null) return null

        try {
            val safeId = character.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            val dest = File(File(rootDir(context), "voices").apply { mkdirs() }, "$safeId.mp3")

            // 1. Check curated direct MyInstants official anime voice MP3 URLs for iconic fighters
            for (directUrl in curatedDirectVoiceMp3Urls(character.id)) {
                if (downloadValidAudioToFile(directUrl, dest, minBytes = 1500)) {
                    return dest
                }
            }

            // 2. Scavenge MyInstants & SoundInstants search pages using canon JP/EN Ultimate move queries
            val queries = buildVoiceSearchQueries(character)
            for (query in queries) {
                val myInstantsHits = searchMyInstantsMp3s(query)
                for (mp3Url in myInstantsHits.take(3)) {
                    if (downloadValidAudioToFile(mp3Url, dest, minBytes = 1500)) {
                        return dest
                    }
                }

                val soundInstantsHits = searchSoundInstantsMp3s(query)
                for (mp3Url in soundInstantsHits.take(2)) {
                    if (downloadValidAudioToFile(mp3Url, dest, minBytes = 1500)) {
                        return dest
                    }
                }
            }

            // 3. Scavenge Internet Archive anime voice clips
            for (query in queries.take(2)) {
                val archiveHits = searchInternetArchiveAudioMp3s("$query anime voice", maxItems = 2)
                for (mp3Url in archiveHits) {
                    if (downloadValidAudioToFile(mp3Url, dest, minBytes = 1500)) {
                        return dest
                    }
                }
            }

            return null
        } finally {
            inFlightDownloads.remove(lockKey)
        }
    }

    private fun directMixkitSfxUrls(sfx: GameSfx): List<String> = when (sfx) {
        GameSfx.WALL_BOUNCE -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/238/238-preview.mp3",
            "https://assets.mixkit.co/active_storage/sfx/2571/2571-preview.mp3"
        )
        GameSfx.BALL_CLASH -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/2155/2155-preview.mp3",
            "https://assets.mixkit.co/active_storage/sfx/2151/2151-preview.mp3"
        )
        GameSfx.CRIT_CLASH -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/2198/2198-preview.mp3",
            "https://assets.mixkit.co/active_storage/sfx/2127/2127-preview.mp3"
        )
        GameSfx.PASSIVE_TRIGGER -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/2592/2592-preview.mp3",
            "https://assets.mixkit.co/active_storage/sfx/2588/2588-preview.mp3"
        )
        GameSfx.REDIRECT_DASH -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/2043/2043-preview.mp3",
            "https://assets.mixkit.co/active_storage/sfx/2044/2044-preview.mp3"
        )
        GameSfx.COUNTER_PARRY -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/1330/1330-preview.mp3",
            "https://assets.mixkit.co/active_storage/sfx/2791/2791-preview.mp3"
        )
        GameSfx.AWAKENING_PLAYER -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/2600/2600-preview.mp3",
            "https://assets.mixkit.co/active_storage/sfx/2589/2589-preview.mp3"
        )
        GameSfx.AWAKENING_AI -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/2590/2590-preview.mp3",
            "https://assets.mixkit.co/active_storage/sfx/2600/2600-preview.mp3"
        )
        GameSfx.KNOCKOUT_FINISH -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/3004/3004-preview.mp3",
            "https://assets.mixkit.co/active_storage/sfx/1684/1684-preview.mp3"
        )
        GameSfx.UPGRADE_FORGE -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/2003/2003-preview.mp3",
            "https://assets.mixkit.co/active_storage/sfx/2019/2019-preview.mp3"
        )
        GameSfx.UI_TAP -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/2568/2568-preview.mp3",
            "https://assets.mixkit.co/active_storage/sfx/961/961-preview.mp3"
        )
    }

    private fun directMixkitBgmUrls(situation: BattleBgmSituation): List<String> = when (situation) {
        BattleBgmSituation.OPENING_TENSION -> listOf(
            "https://assets.mixkit.co/music/preview/mixkit-hazy-after-hours-132.mp3",
            "https://assets.mixkit.co/music/preview/mixkit-deep-urban-623.mp3"
        )
        BattleBgmSituation.NORMAL_CLASH -> listOf(
            "https://assets.mixkit.co/music/preview/mixkit-tech-house-vibes-130.mp3",
            "https://assets.mixkit.co/music/preview/mixkit-games-worldbeat-466.mp3"
        )
        BattleBgmSituation.ULTIMATE_SURGE -> listOf(
            "https://assets.mixkit.co/music/preview/mixkit-cyberpunk-city-8-bit-607.mp3",
            "https://assets.mixkit.co/music/preview/mixkit-driving-ambition-32.mp3"
        )
        BattleBgmSituation.CLUTCH_DANGER -> listOf(
            "https://assets.mixkit.co/music/preview/mixkit-happy-party-152.mp3",
            "https://assets.mixkit.co/music/preview/mixkit-hip-hop-02-738.mp3"
        )
        BattleBgmSituation.NONE -> emptyList()
    }

    private fun curatedDirectVoiceMp3Urls(charId: String): List<String> = when (charId) {
        "dio_world" -> listOf(
            "https://www.myinstants.com/media/sounds/za-warudo-sound-effect.mp3",
            "https://www.myinstants.com/media/sounds/za-warudo_1.mp3"
        )
        "kujo_star" -> listOf(
            "https://www.myinstants.com/media/sounds/ora-ora-ora.mp3",
            "https://www.myinstants.com/media/sounds/star-platinum-za-warudo.mp3"
        )
        "kakarot_z" -> listOf(
            "https://www.myinstants.com/media/sounds/kamehameha_1.mp3",
            "https://www.myinstants.com/media/sounds/kamehameha.mp3",
            "https://www.myinstants.com/media/sounds/kamehameha-final-22527.mp3"
        )
        "satoru_void" -> listOf(
            "https://www.myinstants.com/media/sounds/hollow-purple-6717.mp3",
            "https://www.myinstants.com/media/sounds/gojo-200-hollow-purple-74264.mp3",
            "https://www.myinstants.com/media/sounds/hollow-purple-jjk-60677.mp3"
        )
        "kurosaki_hollow", "byakuya_senbonzakura", "aizen_kyoka" -> listOf(
            "https://www.myinstants.com/media/sounds/bankai-bleach-16131.mp3"
        )
        "naruto_baryon", "minato_flash" -> listOf(
            "https://www.myinstants.com/media/sounds/naruto-rasengan-shippuden.mp3",
            "https://www.myinstants.com/media/sounds/rasengan.mp3"
        )
        "sukuna_shrine", "ryomen_shrine" -> listOf(
            "https://www.myinstants.com/media/sounds/domain-expansion-sukuna.mp3",
            "https://www.myinstants.com/media/sounds/domain-expansion.mp3"
        )
        else -> emptyList()
    }

    private fun buildVoiceSearchQueries(character: CharacterData): List<String> {
        val custom = when (character.id) {
            "satoru_void" -> listOf("gojo hollow purple", "gojo domain expansion", "ryoiki tenkai gojo")
            "gilgamesh_babylon" -> listOf("gilgamesh enuma elish", "gate of babylon gilgamesh", "gilgamesh zasshu")
            "minato_flash" -> listOf("minato rasengan", "minato flying raijin", "namikaze minato")
            "dio_world" -> listOf("dio za warudo", "muda muda dio")
            "kujo_star" -> listOf("jotaro star platinum", "ora ora jotaro")
            "giorno_requiem" -> listOf("giorno giovanna muda", "gold experience requiem")
            "diavolo_crimson" -> listOf("king crimson diavolo", "diavolo")
            "kira_killer_queen" -> listOf("killer queen bites the dust", "kira yoshikage")
            "pucci_heaven" -> listOf("made in heaven pucci", "enrico pucci")
            "kakarot_z" -> listOf("goku kamehameha", "goku ultra instinct")
            "frieza_emperor" -> listOf("frieza anime", "golden frieza")
            "kamado_sun" -> listOf("tanjiro hinokami kagura", "tanjiro kamado")
            "raikiri_shinobi" -> listOf("kakashi chidori", "kakashi kamui")
            "mugiwara_joy" -> listOf("luffy gear 5", "monkey d luffy")
            "roronoa_ashura" -> listOf("zoro santoryu", "roronoa zoro")
            "blackbeard_tremor" -> listOf("blackbeard zehahaha", "marshall d teach")
            "midoriya_ofa" -> listOf("deku detroit smash", "izuku midoriya")
            "shigaraki_decay" -> listOf("shigaraki tomura", "shigaraki")
            "sukuna_shrine", "ryomen_shrine" -> listOf("sukuna domain expansion", "ryomen sukuna")
            "naruto_baryon" -> listOf("naruto rasengan", "uzumaki naruto")
            "sasuke_rinnegan" -> listOf("sasuke chidori", "sasuke amaterasu")
            "madara_uchiha" -> listOf("madara wake up to reality", "madara uchiha")
            "kurosaki_hollow" -> listOf("ichigo bankai", "getsuga tensho")
            "byakuya_senbonzakura" -> listOf("senbonzakura kageyoshi", "byakuya bankai")
            "aizen_kyoka" -> listOf("aizen sosuke", "kyoka suigetsu")
            "archer_ubw" -> listOf("unlimited blade works archer", "emiya archer")
            "caped_baldy" -> listOf("saitama serious punch", "one punch man saitama")
            "yeager_titan" -> listOf("eren yeager tatakae", "eren titan roar")
            "chainsaw_devil" -> listOf("denji chainsaw man", "chainsaw man")
            "makima_control" -> listOf("makima bang", "makima")
            "asta_grimoire" -> listOf("asta black clover", "black asta")
            "guts_berserk" -> listOf("guts berserk", "griffith guts")
            "alucard_hellsing" -> listOf("alucard hellsing", "alucard")
            "rimuru_tempest" -> listOf("rimuru tempest", "great sage rimuru")
            "killua_godspeed" -> listOf("killua godspeed", "killua zoldyck")
            else -> emptyList()
        }
        val cleanUlt = character.ultimateName.replace(Regex("[^A-Za-z0-9 ]"), " ").trim()
        val cleanName = character.name.replace(Regex("[^A-Za-z0-9 ]"), " ").trim()
        val cleanAnime = character.animeInspiration.replace(Regex("[^A-Za-z0-9 ]"), " ").trim()
        return (custom + listOf(
            "$cleanName $cleanUlt",
            "$cleanName $cleanAnime",
            cleanName
        )).map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    }

    private fun searchMyInstantsMp3s(query: String): List<String> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val html = fetchText("https://www.myinstants.com/en/search/?name=$encoded") ?: return emptyList()
        val regex = Regex("""/media/sounds/[A-Za-z0-9_.\-%]+\.mp3""")
        return regex.findAll(html)
            .map { "https://www.myinstants.com${it.value}" }
            .distinct()
            .take(5)
            .toList()
    }

    private fun searchSoundInstantsMp3s(query: String): List<String> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val html = fetchText("https://soundinstants.com/en/search/?name=$encoded") ?: return emptyList()
        val regex = Regex("""(?:https?://[^\s"'<>]+\.mp3|/media/sounds/[A-Za-z0-9_.\-%]+\.mp3)""")
        return regex.findAll(html)
            .map { match ->
                val raw = match.value
                if (raw.startsWith("/")) "https://soundinstants.com$raw" else raw
            }
            .distinct()
            .take(4)
            .toList()
    }

    private fun scrapeMixkitMp3Links(pageUrl: String): List<String> {
        scrapedCategoryCache[pageUrl]?.let { return it }
        val html = fetchText(pageUrl) ?: return emptyList()
        val regex = Regex("""https://assets\.mixkit\.co/[A-Za-z0-9_./\-]+\.mp3""")
        val found = regex.findAll(html).map { it.value }.distinct().toList()
        if (found.isNotEmpty()) {
            scrapedCategoryCache[pageUrl] = found
        }
        return found
    }

    private fun searchInternetArchiveAudioMp3s(query: String, maxItems: Int): List<String> {
        val encoded = URLEncoder.encode("($query) AND mediatype:(audio)", "UTF-8")
        val searchUrl =
            "https://archive.org/advancedsearch.php?q=$encoded&fl[]=identifier&rows=$maxItems&page=1&output=json"
        val json = fetchText(searchUrl) ?: return emptyList()
        val idRegex = Regex(""""identifier"\s*:\s*"([^"]+)"""")
        val identifiers = idRegex.findAll(json).map { it.groupValues[1] }.distinct().take(maxItems).toList()
        if (identifiers.isEmpty()) return emptyList()

        val mp3Urls = ArrayList<String>()
        val fileRegex = Regex(""""name"\s*:\s*"([^"]+\.mp3)"""", RegexOption.IGNORE_CASE)
        for (id in identifiers) {
            val metaJson = fetchText("https://archive.org/metadata/$id") ?: continue
            val firstMp3 = fileRegex.find(metaJson)?.groupValues?.getOrNull(1) ?: continue
            val encodedFile = firstMp3.split("/").joinToString("/") { seg ->
                URLEncoder.encode(seg, "UTF-8").replace("+", "%20")
            }
            mp3Urls.add("https://archive.org/download/$id/$encodedFile")
        }
        return mp3Urls
    }

    private fun fetchText(urlString: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5500
                readTimeout = 7000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "text/html,application/json,*/*")
            }
            if (conn.responseCode !in 200..299) return null
            conn.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                val sb = StringBuilder()
                val buf = CharArray(4096)
                var total = 0
                while (total < 260_000) {
                    val n = reader.read(buf)
                    if (n <= 0) break
                    sb.append(buf, 0, n)
                    total += n
                }
                sb.toString()
            }
        } catch (_: Throwable) {
            null
        } finally {
            try {
                conn?.disconnect()
            } catch (_: Throwable) {
            }
        }
    }

    private fun downloadValidAudioToFile(urlString: String, destFile: File, minBytes: Int): Boolean {
        var conn: HttpURLConnection? = null
        val tmpFile = File(destFile.parentFile, "${destFile.name}.tmp_${System.nanoTime()}")
        return try {
            conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 9000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "audio/mpeg,audio/ogg,audio/*,*/*;q=0.8")
            }
            if (conn.responseCode !in 200..299) return false
            val contentType = (conn.contentType ?: "").lowercase()
            if (contentType.contains("text/html") || contentType.contains("application/xhtml")) {
                return false
            }

            var totalBytes = 0
            val header = ByteArray(16)
            var headerFilled = 0

            conn.inputStream.use { input ->
                FileOutputStream(tmpFile).use { output ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        if (headerFilled < header.size) {
                            val copyLen = minOf(read, header.size - headerFilled)
                            System.arraycopy(buffer, 0, header, headerFilled, copyLen)
                            headerFilled += copyLen
                        }
                        output.write(buffer, 0, read)
                        totalBytes += read
                        // Cap single clip download at 8 MB for safety & speed
                        if (totalBytes > 8 * 1024 * 1024) break
                    }
                }
            }

            if (totalBytes < minBytes || !isValidAudioHeader(header, headerFilled)) {
                tmpFile.delete()
                return false
            }

            if (destFile.exists()) destFile.delete()
            if (!tmpFile.renameTo(destFile)) {
                tmpFile.copyTo(destFile, overwrite = true)
                tmpFile.delete()
            }
            true
        } catch (_: Throwable) {
            try {
                tmpFile.delete()
            } catch (_: Throwable) {
            }
            false
        } finally {
            try {
                conn?.disconnect()
            } catch (_: Throwable) {
            }
        }
    }

    internal fun isValidAudioHeader(header: ByteArray, len: Int): Boolean {
        if (len < 4) return false
        val b0 = header[0].toInt() and 0xFF
        val b1 = header[1].toInt() and 0xFF
        val b2 = header[2].toInt() and 0xFF
        val b3 = header[3].toInt() and 0xFF

        // Reject HTML ("<!DO", "<htm", "<HTM", "<?xm", "{")
        if (b0 == '<'.code || b0 == '{'.code) return false

        // "ID3" tag for MP3
        if (b0 == 'I'.code && b1 == 'D'.code && b2 == '3'.code) return true
        // Raw MPEG audio frame sync (0xFFE0..0xFFFF)
        if (b0 == 0xFF && (b1 and 0xE0) == 0xE0) return true
        // "OggS" for OGG Vorbis / Opus
        if (b0 == 'O'.code && b1 == 'g'.code && b2 == 'g'.code && b3 == 'S'.code) return true
        // "RIFF" for WAV
        if (b0 == 'R'.code && b1 == 'I'.code && b2 == 'F'.code && b3 == 'F'.code) return true
        // "fLaC" for FLAC
        if (b0 == 'f'.code && b1 == 'L'.code && b2 == 'a'.code && b3 == 'C'.code) return true
        // MP4/M4A "ftyp" at offset 4
        if (len >= 8 &&
            header[4].toInt().toChar() == 'f' &&
            header[5].toInt().toChar() == 't' &&
            header[6].toInt().toChar() == 'y' &&
            header[7].toInt().toChar() == 'p'
        ) return true

        return false
    }
}
