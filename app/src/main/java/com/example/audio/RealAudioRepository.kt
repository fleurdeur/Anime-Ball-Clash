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
 * Fetches, title-verifies, magic-byte validates, and disk-caches crisp anime/arcade
 * sound effects, rock/action/metal battle background music tracks, and official recorded
 * anime character Ultimate voice clips from public web repositories
 * (Mixkit CDN, MyInstants, SoundInstants, Internet Archive).
 *
 * STRICTLY ZERO AI TEXT-TO-SPEECH OR SYNTHETIC VOICE IS EVER USED HERE OR ANYWHERE IN THE APP.
 */
object RealAudioRepository {

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

    private data class ScoredAudioCandidate(
        val title: String,
        val url: String
    )

    private val inFlightDownloads = ConcurrentHashMap<String, Boolean>()
    private val scrapedCategoryCache = ConcurrentHashMap<String, List<ScoredAudioCandidate>>()
    @Volatile
    private var legacyCachePurged = false

    private fun rootDir(context: Context): File {
        if (!legacyCachePurged) {
            legacyCachePurged = true
            try {
                File(context.filesDir, "real_audio_v2").deleteRecursively()
            } catch (_: Throwable) {
            }
        }
        return File(context.filesDir, "real_audio_v3").apply { mkdirs() }
    }

    fun getCachedSfxFile(context: Context, sfx: GameSfx): File? {
        val f = File(File(rootDir(context), "sfx").apply { mkdirs() }, "${sfx.name.lowercase()}.mp3")
        val maxLen = maxSfxBytes(sfx).toLong()
        return if (f.exists() && f.length() in 1024L..maxLen) f else null
    }

    fun getCachedBgmFile(context: Context, situation: BattleBgmSituation): File? {
        val f = File(File(rootDir(context), "bgm").apply { mkdirs() }, "${situation.name.lowercase()}.mp3")
        return if (f.exists() && f.length() > 8192L) f else null
    }

    fun getCachedVoiceFile(context: Context, characterId: String): File? {
        val safeId = characterId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val f = File(File(rootDir(context), "voices").apply { mkdirs() }, "$safeId.mp3")
        return if (f.exists() && f.length() > 1200L) f else null
    }

    private fun maxSfxBytes(sfx: GameSfx): Int = when (sfx) {
        GameSfx.UI_TAP -> 55_000
        GameSfx.WALL_BOUNCE -> 78_000
        GameSfx.BALL_CLASH, GameSfx.REDIRECT_DASH -> 125_000
        GameSfx.CRIT_CLASH, GameSfx.COUNTER_PARRY, GameSfx.PASSIVE_TRIGGER, GameSfx.UPGRADE_FORGE -> 165_000
        GameSfx.AWAKENING_PLAYER, GameSfx.AWAKENING_AI, GameSfx.KNOCKOUT_FINISH -> 290_000
    }

    /**
     * Downloads and caches crisp, punchy combat & UI sound effects verified by title and file duration/size.
     */
    fun fetchSfxFile(context: Context, sfx: GameSfx): File? {
        getCachedSfxFile(context, sfx)?.let { return it }
        val lockKey = "sfx_${sfx.name}"
        if (inFlightDownloads.putIfAbsent(lockKey, true) != null) return null

        try {
            val dest = File(File(rootDir(context), "sfx").apply { mkdirs() }, "${sfx.name.lowercase()}.mp3")
            val maxBytes = maxSfxBytes(sfx)

            // 1. Try verified title-matched Mixkit SFX URLs first
            for (url in verifiedMixkitSfxUrls(sfx)) {
                if (downloadValidAudioToFile(url, dest, minBytes = 1024, maxBytes = maxBytes)) {
                    return dest
                }
            }

            // 2. Scrape live Mixkit combat SFX categories and rank candidates by sound-design keywords
            val categoryUrls = when (sfx) {
                GameSfx.WALL_BOUNCE -> listOf(
                    "https://mixkit.co/free-sound-effects/ball/",
                    "https://mixkit.co/free-sound-effects/hit/"
                )
                GameSfx.BALL_CLASH -> listOf(
                    "https://mixkit.co/free-sound-effects/martial-arts/",
                    "https://mixkit.co/free-sound-effects/punch/"
                )
                GameSfx.CRIT_CLASH -> listOf(
                    "https://mixkit.co/free-sound-effects/sword/",
                    "https://mixkit.co/free-sound-effects/martial-arts/"
                )
                GameSfx.REDIRECT_DASH -> listOf(
                    "https://mixkit.co/free-sound-effects/whoosh/",
                    "https://mixkit.co/free-sound-effects/swish/"
                )
                GameSfx.COUNTER_PARRY -> listOf(
                    "https://mixkit.co/free-sound-effects/sword/"
                )
                GameSfx.PASSIVE_TRIGGER -> listOf(
                    "https://mixkit.co/free-sound-effects/magic/",
                    "https://mixkit.co/free-sound-effects/sparkles/"
                )
                GameSfx.AWAKENING_PLAYER, GameSfx.AWAKENING_AI -> listOf(
                    "https://mixkit.co/free-sound-effects/electricity/",
                    "https://mixkit.co/free-sound-effects/magic/"
                )
                GameSfx.KNOCKOUT_FINISH -> listOf(
                    "https://mixkit.co/free-sound-effects/explosion/",
                    "https://mixkit.co/free-sound-effects/impact/"
                )
                GameSfx.UPGRADE_FORGE -> listOf(
                    "https://mixkit.co/free-sound-effects/win/"
                )
                GameSfx.UI_TAP -> listOf(
                    "https://mixkit.co/free-sound-effects/click/"
                )
            }

            val rankedCandidates = categoryUrls
                .flatMap { scrapeMixkitScoredItems(it) }
                .distinctBy { it.url }
                .map { it to scoreSfxCandidate(sfx, it.title) }
                .filter { it.second > 0 }
                .sortedByDescending { it.second }

            val offset = if (sfx == GameSfx.AWAKENING_AI) 1 else 0
            val orderedUrls = rankedCandidates.drop(offset).map { it.first.url } +
                rankedCandidates.take(offset).map { it.first.url }

            for (url in orderedUrls.take(5)) {
                if (downloadValidAudioToFile(url, dest, minBytes = 1024, maxBytes = maxBytes)) {
                    return dest
                }
            }

            return null
        } finally {
            inFlightDownloads.remove(lockKey)
        }
    }

    /**
     * Downloads and caches genuine high-energy Action, Rock, and Metal battle BGM tracks from Mixkit & Internet Archive.
     */
    fun fetchBgmFile(context: Context, situation: BattleBgmSituation): File? {
        if (situation == BattleBgmSituation.NONE) return null
        getCachedBgmFile(context, situation)?.let { return it }
        val lockKey = "bgm_${situation.name}"
        if (inFlightDownloads.putIfAbsent(lockKey, true) != null) return null

        try {
            val dest = File(File(rootDir(context), "bgm").apply { mkdirs() }, "${situation.name.lowercase()}.mp3")

            // 1. Scrape Mixkit Rock, Metal, Fight, and Action stock music libraries and score by combat intensity
            val categoryUrls = when (situation) {
                BattleBgmSituation.OPENING_TENSION -> listOf(
                    "https://mixkit.co/free-stock-music/tag/dramatic/",
                    "https://mixkit.co/free-stock-music/action/"
                )
                BattleBgmSituation.NORMAL_CLASH -> listOf(
                    "https://mixkit.co/free-stock-music/tag/fight/",
                    "https://mixkit.co/free-stock-music/action/"
                )
                BattleBgmSituation.ULTIMATE_SURGE -> listOf(
                    "https://mixkit.co/free-stock-music/rock/",
                    "https://mixkit.co/free-stock-music/metal/"
                )
                BattleBgmSituation.CLUTCH_DANGER -> listOf(
                    "https://mixkit.co/free-stock-music/metal/",
                    "https://mixkit.co/free-stock-music/tag/fight/"
                )
                BattleBgmSituation.NONE -> emptyList()
            }

            val candidates = categoryUrls
                .flatMap { scrapeMixkitScoredItems(it) }
                .distinctBy { it.url }
                .map { it to scoreBgmCandidate(situation, it.title) }
                .filter { it.second > 0 }
                .sortedByDescending { it.second }

            val offset = situation.ordinal % (candidates.size.coerceAtLeast(1))
            val ordered = if (candidates.isNotEmpty()) {
                (candidates.drop(offset) + candidates.take(offset)).map { it.first.url }
            } else {
                emptyList()
            }

            for (url in ordered.take(4)) {
                if (downloadValidAudioToFile(url, dest, minBytes = 16_384, maxBytes = 7_500_000)) {
                    return dest
                }
            }

            // 2. Secondary fallback: Internet Archive anime rock / boss fight instrumental BGM
            val archiveQuery = when (situation) {
                BattleBgmSituation.OPENING_TENSION -> "epic dramatic battle intro instrumental"
                BattleBgmSituation.NORMAL_CLASH -> "fast action rock fight instrumental bgm"
                BattleBgmSituation.ULTIMATE_SURGE -> "epic heavy metal boss battle rock instrumental"
                BattleBgmSituation.CLUTCH_DANGER -> "intense final boss battle rock bgm"
                BattleBgmSituation.NONE -> ""
            }
            if (archiveQuery.isNotEmpty()) {
                for (url in searchInternetArchiveAudioMp3s(archiveQuery, maxItems = 3)) {
                    if (downloadValidAudioToFile(url, dest, minBytes = 16_384, maxBytes = 7_500_000)) {
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

            for (directUrl in curatedDirectVoiceMp3Urls(character.id)) {
                if (downloadValidAudioToFile(directUrl, dest, minBytes = 1500, maxBytes = 3_500_000)) {
                    return dest
                }
            }

            val queries = buildVoiceSearchQueries(character)
            for (query in queries) {
                val myInstantsHits = searchMyInstantsMp3s(query)
                for (mp3Url in myInstantsHits.take(3)) {
                    if (downloadValidAudioToFile(mp3Url, dest, minBytes = 1500, maxBytes = 3_500_000)) {
                        return dest
                    }
                }

                val soundInstantsHits = searchSoundInstantsMp3s(query)
                for (mp3Url in soundInstantsHits.take(2)) {
                    if (downloadValidAudioToFile(mp3Url, dest, minBytes = 1500, maxBytes = 3_500_000)) {
                        return dest
                    }
                }
            }

            for (query in queries.take(2)) {
                val archiveHits = searchInternetArchiveAudioMp3s("$query anime voice", maxItems = 2)
                for (mp3Url in archiveHits) {
                    if (downloadValidAudioToFile(mp3Url, dest, minBytes = 1500, maxBytes = 3_500_000)) {
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
     * Only includes Mixkit SFX IDs whose exact combat sound titles are verified:
     * - 2047: Martial arts fast punch
     * - 2789: Samurai sword impact
     * - 2792: Fast sword whoosh
     * - 2571: Select click
     */
    private fun verifiedMixkitSfxUrls(sfx: GameSfx): List<String> = when (sfx) {
        GameSfx.BALL_CLASH -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/2047/2047-preview.mp3"
        )
        GameSfx.CRIT_CLASH -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/2789/2789-preview.mp3"
        )
        GameSfx.REDIRECT_DASH -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/2792/2792-preview.mp3"
        )
        GameSfx.UI_TAP -> listOf(
            "https://assets.mixkit.co/active_storage/sfx/2571/2571-preview.mp3"
        )
        else -> emptyList()
    }

    private val BANNED_SFX_WORDS = listOf(
        "groan", "moan", "scream", "voice", "human", "ouch", "cry", "laugh",
        "cartoon", "funny", "comedy", "fart", "boing", "fail", "lose", "game over",
        "sad", "dog", "cat", "bird", "animal", "crowd", "applause", "cheer",
        "camera", "shutter", "typewriter", "keyboard", "mouse", "loop", "ambience",
        "ambient", "drone", "horror", "zombie", "monster", "footstep", "coin", "8-bit"
    )

    private fun scoreSfxCandidate(sfx: GameSfx, rawTitle: String): Int {
        val t = rawTitle.lowercase()
        if (BANNED_SFX_WORDS.any { t.contains(it) }) return -100

        var score = 10
        val preferred = when (sfx) {
            GameSfx.WALL_BOUNCE -> listOf("ball", "bounce", "tap", "thud", "impact", "quick", "short")
            GameSfx.BALL_CLASH -> listOf("martial arts", "punch", "body hit", "impact", "fast", "strike", "boxer")
            GameSfx.CRIT_CLASH -> listOf("samurai", "sword", "heavy", "impact", "smash", "strike", "blade")
            GameSfx.REDIRECT_DASH -> listOf("fast", "whoosh", "swish", "air", "swift", "wind", "sword whoosh")
            GameSfx.COUNTER_PARRY -> listOf("sword", "blade", "metal", "clang", "strike", "clash", "steel", "dagger")
            GameSfx.PASSIVE_TRIGGER -> listOf("magic", "sparkle", "shimmer", "spell", "chime", "sweep", "shine")
            GameSfx.AWAKENING_PLAYER, GameSfx.AWAKENING_AI -> listOf("electricity", "lightning", "power", "energy", "surge", "magic", "blast")
            GameSfx.KNOCKOUT_FINISH -> listOf("explosion", "boom", "epic", "impact", "blast", "hit", "thunder")
            GameSfx.UPGRADE_FORGE -> listOf("win", "chime", "bonus", "unlock", "level", "achievement", "crystal")
            GameSfx.UI_TAP -> listOf("modern", "select", "click", "interface", "ui", "button", "tap", "soft")
        }
        preferred.forEach { kw ->
            if (t.contains(kw)) score += 18
        }
        return score
    }

    private val BANNED_BGM_WORDS = listOf(
        "happy", "party", "jazz", "lounge", "chill", "hazy", "relax", "sleep",
        "lullaby", "romantic", "wedding", "christmas", "holiday", "ukulele",
        "corporate", "podcast", "sunny", "beach", "comedy", "funny", "kids", "piano"
    )

    private fun scoreBgmCandidate(situation: BattleBgmSituation, rawTitle: String): Int {
        val t = rawTitle.lowercase()
        if (BANNED_BGM_WORDS.any { t.contains(it) }) return -100

        var score = 10
        val preferred = when (situation) {
            BattleBgmSituation.OPENING_TENSION -> listOf("dramatic", "epic", "tension", "dark", "cinematic", "drums", "battle", "intro")
            BattleBgmSituation.NORMAL_CLASH -> listOf("brawl", "fight", "action", "hit the gas", "rock", "chase", "energetic", "combat")
            BattleBgmSituation.ULTIMATE_SURGE -> listOf("metal", "rock", "heavy", "guitar", "power", "aggressive", "adrenaline", "epic")
            BattleBgmSituation.CLUTCH_DANGER -> listOf("metal", "fury", "intense", "fast", "fight", "storm", "danger", "boss", "strike")
            BattleBgmSituation.NONE -> emptyList()
        }
        preferred.forEach { kw ->
            if (t.contains(kw)) score += 20
        }
        return score
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

    private fun scrapeMixkitScoredItems(pageUrl: String): List<ScoredAudioCandidate> {
        scrapedCategoryCache[pageUrl]?.let { return it }
        val html = fetchText(pageUrl) ?: return emptyList()
        val urlRegex = Regex("""https://assets\.mixkit\.co/[A-Za-z0-9_./\-]+\.mp3""")
        val tagStripRegex = Regex("""<[^>]+>""")
        val results = ArrayList<ScoredAudioCandidate>()

        for (match in urlRegex.findAll(html)) {
            val mp3Url = match.value
            val windowStart = (match.range.first - 650).coerceAtLeast(0)
            val windowEnd = (match.range.last + 650).coerceAtMost(html.length)
            val snippet = html.substring(windowStart, windowEnd)
                .replace(tagStripRegex, " ")
                .replace(Regex("""\s+"""), " ")
                .trim()
            val urlSlug = mp3Url.substringAfterLast('/').substringBeforeLast('.')
            results.add(ScoredAudioCandidate(title = "$urlSlug $snippet", url = mp3Url))
        }

        val deduped = results.distinctBy { it.url }
        if (deduped.isNotEmpty()) {
            scrapedCategoryCache[pageUrl] = deduped
        }
        return deduped
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
                while (total < 280_000) {
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

    private fun downloadValidAudioToFile(
        urlString: String,
        destFile: File,
        minBytes: Int,
        maxBytes: Int
    ): Boolean {
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
            val declaredLen = conn.contentLength
            if (declaredLen > maxBytes) {
                return false
            }

            var totalBytes = 0
            val header = ByteArray(16)
            var headerFilled = 0
            var exceededCap = false

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
                        totalBytes += read
                        if (totalBytes > maxBytes) {
                            exceededCap = true
                            break
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }

            if (exceededCap || totalBytes < minBytes || !isValidAudioHeader(header, headerFilled)) {
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

        if (b0 == '<'.code || b0 == '{'.code) return false
        if (b0 == 'I'.code && b1 == 'D'.code && b2 == '3'.code) return true
        if (b0 == 0xFF && (b1 and 0xE0) == 0xE0) return true
        if (b0 == 'O'.code && b1 == 'g'.code && b2 == 'g'.code && b3 == 'S'.code) return true
        if (b0 == 'R'.code && b1 == 'I'.code && b2 == 'F'.code && b3 == 'F'.code) return true
        if (b0 == 'f'.code && b1 == 'L'.code && b2 == 'a'.code && b3 == 'C'.code) return true
        if (len >= 8 &&
            header[4].toInt().toChar() == 'f' &&
            header[5].toInt().toChar() == 't' &&
            header[6].toInt().toChar() == 'y' &&
            header[7].toInt().toChar() == 'p'
        ) return true

        return false
    }
}
