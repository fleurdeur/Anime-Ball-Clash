package com.example.model

import kotlin.math.abs

/**
 * Canon anime race & origin classification across all 224 characters,
 * written in simple everyday words for UI and used by LorePowerBalancer.
 */
enum class AnimeRace(
    val simpleName: String,
    val traitSimpleDesc: String
) {
    SAIYAN_COSMIC(
        simpleName = "Saiyan / Alien",
        traitSimpleDesc = "Hits harder at low HP and shoots big energy blasts."
    ),
    GOD_CELESTIAL(
        simpleName = "God / Cosmic",
        traitSimpleDesc = "High HP aura and stronger Ultimate blasts."
    ),
    HEROIC_SPIRIT(
        simpleName = "Heroic Spirit",
        traitSimpleDesc = "Summons extra mystic weapons and charges Ult fast."
    ),
    DEMON_CURSE(
        simpleName = "Demon / Curse",
        traitSimpleDesc = "Heals a bit on big hits and cuts wounded rivals harder."
    ),
    DEVIL_HYBRID(
        simpleName = "Devil / Hybrid",
        traitSimpleDesc = "Fast wild rush and heals HP when using Ultimate."
    ),
    SOUL_REAPER(
        simpleName = "Soul Reaper",
        traitSimpleDesc = "Shoots wide blade waves and lands strong crits."
    ),
    MAGE_ESPER(
        simpleName = "Mage / Psychic",
        traitSimpleDesc = "Fires fast magic shots and fills Ult bar quicker."
    ),
    STAND_USER(
        simpleName = "Stand Master",
        traitSimpleDesc = "Stand spirit blocks hits more often and punches back."
    ),
    MONSTER_KAIJU(
        simpleName = "Titan / Monster",
        traitSimpleDesc = "Extra tough HP and heavy smash knockback."
    ),
    HUMAN_SHINOBI(
        simpleName = "Human / Ninja",
        traitSimpleDesc = "High crit skill and quick auto-dodge or teleport reflex."
    )
}

/**
 * World-building power scale tier for each anime universe & character form.
 */
enum class WorldScaleTier(
    val rank: Int,
    val simpleName: String
) {
    TACTICAL_MASTER(1, "Master Fighter"),
    ARCANE_DOMAIN(2, "Magic & Domain"),
    WORLD_CALAMITY(3, "World Threat"),
    COSMIC_OVERLORD(4, "Cosmic Power")
}

/**
 * Travelling canon projectile styles that visibly shoot out toward the opposing
 * character, deal real impact damage/effects, and smoothly fade out.
 */
enum class CanonProjectileStyle(
    val simpleMoveLabel: String
) {
    HOLLOW_PURPLE_ORB("Shoots Hollow Purple orb"),
    BABYLON_SWORD_RAIN("Summons & throws multiple swords"),
    FLYING_RAIJIN_KUNAI("Throws teleport kunai & strikes behind"),
    KI_ENERGY_BLAST("Shoots big Ki energy blast"),
    ZOLTRAAK_ARCANE_BEAM("Shoots fast magic beam"),
    CRIMSON_SLASH_WAVE("Shoots flying sword slash wave"),
    FIRE_METEOR_ORB("Shoots blazing fire meteor"),
    LIGHTNING_SPEAR("Shoots fast lightning spear"),
    GRAVITY_DARK_ORB("Shoots dark gravity sphere"),
    PSYCHIC_FORCE_SHOT("Shoots shockwave force blast"),
    BULLET_CANNON_VOLLEY("Fires rapid energy shots")
}

/**
 * Asymmetric anime matchup calculation between two opposing fighters:
 * - Preserves overpowered canon identity (Cosmic HP/blast size, Saiyan low-HP Zenkai,
 *   Servant extra sword count, Demon regen, Shinobi lethal crits & teleport reflex).
 * - Dynamically balances cross-universe fights so a Master Fighter facing a Cosmic Power
 *   gains sharp weak-spot crit multipliers, faster Ult charge, and higher auto-counter
 *   reflex chance while the Cosmic fighter keeps boss-like HP and giant blasts!
 */
data class LoreMatchupBalance(
    val hpMultiplier: Float = 1.0f,
    val damageMultiplier: Float = 1.0f,
    val critBonusMultiplier: Float = 1.0f,
    val ultChargeMultiplier: Float = 1.0f,
    val autoCounterBonusChance: Float = 0.0f,
    val projectileScale: Float = 1.0f,
    val extraVolleyCount: Int = 0,
    val lowHpZenkaiScale: Float = 0.0f,
    val onCritHealFlat: Float = 0.0f,
    val simpleMatchupNote: String = "Balanced Anime Power"
)

object LorePowerBalancer {

    private val TELEPORT_COUNTER_IDS = setOf(
        "minato_flash",
        "sasuke_rinnegan",
        "kakarot_z",
        "trafalgar_room",
        "obito_ten_tails",
        "kurosaki_hollow",
        "killua_godspeed",
        "raikiri_shinobi",
        "zenitsu_thunder",
        "yhwach_almighty",
        "vegeta_ego",
        "goku_black_rose"
    )

    private val SWORD_RAIN_IDS = setOf(
        "gilgamesh_babylon",
        "archer_ubw",
        "byakuya_senbonzakura"
    )

    fun isTeleportCounterCharacter(char: CharacterData): Boolean {
        if (char.id in TELEPORT_COUNTER_IDS) return true
        val anime = char.animeInspiration.lowercase()
        val passive = char.passiveName.lowercase()
        val ult = char.ultimateName.lowercase()
        if ("teleport" in passive || "raijin" in passive || "shambles" in ult || "sonido" in passive || "flash step" in passive) {
            return true
        }
        return char.classType == CharacterClass.SPEEDSTER &&
            char.element == ElementTag.ELEC &&
            ("naruto" in anime || "bleach" in anime)
    }

    fun resolveCanonCounterCallout(char: CharacterData): String = when (char.id) {
        "minato_flash" -> "FLYING RAIJIN!"
        "sasuke_rinnegan" -> "RINNEGAN SWAP!"
        "kakarot_z", "vegeta_ego", "goku_black_rose" -> "INSTANT TELEPORT!"
        "trafalgar_room" -> "ROOM SHAMBLES!"
        "obito_ten_tails", "raikiri_shinobi" -> "KAMUI WARP!"
        "kurosaki_hollow" -> "FLASH STEP!"
        "killua_godspeed" -> "GODSPEED STEP!"
        "satoru_void" -> "INFINITY!"
        "asta_grimoire" -> "ANTI-MAGIC!"
        "meliodas_wrath" -> "FULL COUNTER!"
        "accelerator_vector" -> "VECTOR REFLECT!"
        else -> char.counterArchetype.popupCallout
    }

    fun resolveAnimeRace(char: CharacterData): AnimeRace {
        val id = char.id
        val anime = char.animeInspiration.lowercase()
        val title = char.title.lowercase()

        if (char.hasStandSpirit || "jojo" in anime) {
            return if (id == "dio_world" || id == "kars_ultimate") AnimeRace.DEMON_CURSE else AnimeRace.STAND_USER
        }
        if ("dragon ball" in anime) {
            return if (id in setOf("goku_black_rose", "majin_kid_buu")) AnimeRace.GOD_CELESTIAL else AnimeRace.SAIYAN_COSMIC
        }
        if (id in setOf("caped_baldy", "anti_spiral_void", "simon_spiral", "griffith_femto", "rimuru_tempest", "ainz_overlord", "yhwach_almighty", "mugiwara_joy")) {
            return AnimeRace.GOD_CELESTIAL
        }
        if ("fate/" in anime || char.weaponType == WeaponVisualType.ROYAL_GATES) {
            return AnimeRace.HEROIC_SPIRIT
        }
        if ("bleach" in anime || "yu yu hakusho" in anime || "soul eater" in anime) {
            return AnimeRace.SOUL_REAPER
        }
        if ("chainsaw man" in anime || id in setOf("asta_grimoire", "meliodas_wrath")) {
            return AnimeRace.DEVIL_HYBRID
        }
        if (
            "demon" in title || "curse" in title || "vampire" in title || "ghoul" in anime ||
            id in setOf("sukuna_shrine", "muzan_progenitor", "alucard_hellsing", "akaza_compass", "kokushibo_moon", "mahoraga_divine", "aura_scales")
        ) {
            return AnimeRace.DEMON_CURSE
        }
        if (
            "titan" in anime || "kaiju" in anime || "evangelion" in anime ||
            id in setOf("yeager_titan", "kafka_kaiju", "kaido_dragon", "meruem_chimera", "antares_destruction", "beru_marshal", "caiman_dorohedoro")
        ) {
            return AnimeRace.MONSTER_KAIJU
        }
        if (
            char.element == ElementTag.MAGIC || char.classType == CharacterClass.CONTROL_MAGIC ||
            "frieren" in anime || "mob psycho" in anime || "akira" in anime || "accelerator" in anime || "overlord" in anime
        ) {
            return AnimeRace.MAGE_ESPER
        }
        return AnimeRace.HUMAN_SHINOBI
    }

    fun resolveWorldScaleTier(char: CharacterData): WorldScaleTier {
        val id = char.id
        val anime = char.animeInspiration.lowercase()

        if (
            "dragon ball" in anime || "gurren lagann" in anime ||
            id in setOf("caped_baldy", "rimuru_tempest", "ainz_overlord", "antares_destruction", "jinwoo_monarch", "yhwach_almighty", "anti_spiral_void", "madoka_goddess", "sailor_cosmos")
        ) {
            return WorldScaleTier.COSMIC_OVERLORD
        }

        if (
            "bleach" in anime || "fate/" in anime || "solo leveling" in anime ||
            "seven deadly sins" in anime || "fairy tail" in anime || "evangelion" in anime ||
            "akira" in anime || "certain scientific" in anime ||
            id in setOf(
                "naruto_baryon", "sasuke_rinnegan", "madara_uchiha", "obito_ten_tails", "pain_deva",
                "mugiwara_joy", "kaido_dragon", "shanks_redhair", "blackbeard_tremor", "griffith_femto"
            )
        ) {
            return WorldScaleTier.WORLD_CALAMITY
        }

        if (
            "jujutsu kaisen" in anime || "black clover" in anime || "chainsaw man" in anime ||
            "frieren" in anime || "jojo" in anime || "hellsing" in anime || "made in abyss" in anime ||
            "one piece" in anime || "naruto" in anime || "my hero academia" in anime ||
            "code geass" in anime || "trigun" in anime || "dorohedoro" in anime ||
            char.element == ElementTag.MAGIC
        ) {
            return WorldScaleTier.ARCANE_DOMAIN
        }

        return WorldScaleTier.TACTICAL_MASTER
    }

    fun resolveCanonProjectileStyle(char: CharacterData): CanonProjectileStyle {
        val id = char.id
        val anime = char.animeInspiration.lowercase()

        return when {
            id == "satoru_void" -> CanonProjectileStyle.HOLLOW_PURPLE_ORB
            id in SWORD_RAIN_IDS || char.weaponType == WeaponVisualType.ROYAL_GATES ->
                CanonProjectileStyle.BABYLON_SWORD_RAIN
            id in setOf("minato_flash", "raikiri_shinobi", "gabimaru_hollow") ||
                char.weaponType == WeaponVisualType.LIGHTNING_KUNAI ->
                CanonProjectileStyle.FLYING_RAIJIN_KUNAI
            char.weaponType in setOf(WeaponVisualType.HELLSING_PISTOLS, WeaponVisualType.CYBER_CANNON, WeaponVisualType.MECHA_FUNNELS) ->
                CanonProjectileStyle.BULLET_CANNON_VOLLEY
            "dragon ball" in anime || id in setOf("naruto_baryon", "saber_artoria", "saber_alter", "simon_spiral") ||
                char.weaponType == WeaponVisualType.KI_HALO ->
                CanonProjectileStyle.KI_ENERGY_BLAST
            "frieren" in anime || id in setOf("ainz_overlord", "yuta_rika", "rimuru_tempest", "kaworu_tabris") ||
                char.weaponType in setOf(WeaponVisualType.MAGE_STAFF, WeaponVisualType.ARCANE_CARDS) ->
                CanonProjectileStyle.ZOLTRAAK_ARCANE_BEAM
            id in setOf("pain_deva", "blackbeard_tremor", "griffith_femto", "obito_ten_tails", "tetsuo_psychic", "accelerator_vector", "jinwoo_monarch", "all_for_one") ||
                char.ultimateArchetype in setOf(UltimateArchetype.GRAVITY_CRUSH, UltimateArchetype.SOUL_DEVOUR) ->
                CanonProjectileStyle.GRAVITY_DARK_ORB
            id in setOf("makima_control", "kageyama_esper", "caped_baldy", "all_might_symbol", "midoriya_ofa", "gon_jajanken") ||
                char.hasStandSpirit || char.weaponType == WeaponVisualType.PSYCHIC_DEBRIS ->
                CanonProjectileStyle.PSYCHIC_FORCE_SHOT
            char.element == ElementTag.FIRE || id in setOf("madara_uchiha", "kaido_dragon", "itachi_tsukuyomi") ->
                CanonProjectileStyle.FIRE_METEOR_ORB
            char.element == ElementTag.ELEC || char.weaponType == WeaponVisualType.SPIRIT_BOW ->
                CanonProjectileStyle.LIGHTNING_SPEAR
            char.element == ElementTag.SLASH || id in setOf("sukuna_shrine", "kurosaki_hollow", "roronoa_ashura", "shanks_redhair", "kamado_sun", "guts_berserk") ->
                CanonProjectileStyle.CRIMSON_SLASH_WAVE
            else -> CanonProjectileStyle.PSYCHIC_FORCE_SHOT
        }
    }

    fun resolveCanonProjectileName(char: CharacterData): String = when (char.id) {
        "satoru_void" -> "HOLLOW PURPLE"
        "gilgamesh_babylon" -> "GATE OF BABYLON"
        "archer_ubw" -> "BLADE WORKS"
        "byakuya_senbonzakura" -> "SENBONZAKURA"
        "minato_flash" -> "FLYING RAIJIN"
        "kakarot_z" -> "KAMEHAMEHA"
        "vegeta_ego" -> "FINAL FLASH"
        "broly_legendary" -> "OMEGA BLASTER"
        "gohan_beast" -> "SPECIAL BEAM"
        "frieza_emperor" -> "DEATH BEAM"
        "sukuna_shrine" -> "WORLD SLASH"
        "frieren_zoltraak", "fern_rapid_zoltraak" -> "ZOLTRAAK"
        "naruto_baryon" -> "RASENSHURIKEN"
        "sasuke_rinnegan" -> "INDRA'S ARROW"
        "madara_uchiha" -> "METEOR DROP"
        "kurosaki_hollow" -> "GETSUGA TENSHO"
        "makima_control" -> "BANG!"
        "caped_baldy" -> "SERIOUS PUNCH WAVE"
        "ainz_overlord" -> "FALLEN DOWN"
        "saber_artoria" -> "EXCALIBUR"
        "saber_alter" -> "EXCALIBUR MORGAN"
        "pain_deva" -> "GRAVITY CORE"
        else -> char.ultimateName.uppercase()
    }

    /**
     * Base automatic counter trigger probability (0.22..0.36) based on canon speed,
     * race, and defensive mastery.
     */
    fun resolveBaseAutoCounterChance(char: CharacterData): Float {
        val specific = when (char.id) {
            "minato_flash" -> 0.36f
            "satoru_void" -> 0.35f
            "kakarot_z" -> 0.33f
            "sasuke_rinnegan" -> 0.34f
            "giorno_requiem", "kujo_star", "dio_world" -> 0.33f
            "accelerator_vector", "meliodas_wrath" -> 0.34f
            "asta_grimoire", "toji_heavenly" -> 0.32f
            "killua_godspeed", "zenitsu_thunder" -> 0.34f
            else -> -1f
        }
        if (specific > 0f) return specific

        var chance = when (char.counterArchetype) {
            CounterArchetype.TELEPORT_STRIKE -> 0.32f
            CounterArchetype.INFINITY_BARRIER_COUNTER -> 0.31f
            CounterArchetype.VECTOR_REFLECT_COUNTER -> 0.30f
            CounterArchetype.TIME_IMMUNITY -> 0.29f
            CounterArchetype.STAND_RUSH_COUNTER -> 0.30f
            CounterArchetype.SHADOW_DODGE -> 0.29f
            CounterArchetype.ANTI_MAGIC_NULL -> 0.28f
            CounterArchetype.TITAN_ARMOR_COUNTER -> 0.25f
            CounterArchetype.GUARD_PARRY -> 0.24f
        }
        if (char.speedRating >= 1.48f) chance += 0.03f
        return chance.coerceIn(0.22f, 0.36f)
    }

    /**
     * Computes lore-faithful yet competitively balanced combat modifiers for `fighter`
     * when fighting `rival`.
     */
    fun computeMatchup(fighter: CharacterData, rival: CharacterData): LoreMatchupBalance {
        val myRace = fighter.animeRace
        val myWorld = fighter.worldScaleTier
        val rivalWorld = rival.worldScaleTier
        val tierDiff = rivalWorld.rank - myWorld.rank // >0 means fighter is facing a higher-scale universe!

        var hpMult = 1.0f
        var dmgMult = 1.0f
        var critMult = 1.0f
        var ultChargeMult = 1.0f
        var counterBonus = 0.0f
        var projScale = 1.0f
        var extraVolley = 0
        var zenkai = 0.0f
        var critHeal = 0.0f

        // 1. Canon Race Overpowered Traits (Always True to Anime!)
        when (myRace) {
            AnimeRace.SAIYAN_COSMIC -> {
                zenkai = 0.24f
                projScale += 0.14f
                dmgMult *= 1.04f
            }
            AnimeRace.GOD_CELESTIAL -> {
                hpMult *= 1.07f
                projScale += 0.16f
                dmgMult *= 1.05f
            }
            AnimeRace.HEROIC_SPIRIT -> {
                extraVolley += 2
                ultChargeMult *= 1.14f
                dmgMult *= 1.04f
            }
            AnimeRace.DEMON_CURSE -> {
                critHeal = 3.5f
                dmgMult *= 1.05f
            }
            AnimeRace.DEVIL_HYBRID -> {
                critHeal = 3.0f
                ultChargeMult *= 1.10f
                dmgMult *= 1.04f
            }
            AnimeRace.SOUL_REAPER -> {
                projScale += 0.12f
                critMult *= 1.10f
            }
            AnimeRace.MAGE_ESPER -> {
                ultChargeMult *= 1.16f
                projScale += 0.10f
            }
            AnimeRace.STAND_USER -> {
                counterBonus += 0.04f
                critMult *= 1.08f
            }
            AnimeRace.MONSTER_KAIJU -> {
                hpMult *= 1.08f
                dmgMult *= 1.04f
            }
            AnimeRace.HUMAN_SHINOBI -> {
                counterBonus += 0.05f
                critMult *= 1.12f
                ultChargeMult *= 1.08f
            }
        }

        // 2. Cross-Verse World-Building & Power Scaling Balance
        val note = when {
            tierDiff > 0 -> {
                // Underdog Tactical/Arcane fighter vs Cosmic/World-Threat foe:
                // Boost lethal precision, crit multiplier, Ult speed & auto-counter reflex!
                dmgMult *= (1.0f + tierDiff * 0.055f)
                critMult *= (1.0f + tierDiff * 0.08f)
                ultChargeMult *= (1.0f + tierDiff * 0.12f)
                counterBonus += tierDiff * 0.035f
                val pctBoost = ((dmgMult * critMult - 1f) * 100f).toInt().coerceAtLeast(8)
                "Skill Boost: +$pctBoost% Crit Power, Faster Ult & Sharper Auto-Counter vs ${rivalWorld.simpleName}"
            }
            tierDiff < 0 -> {
                // Higher-scale Cosmic/World-Threat powerhouse vs lower-tier foe:
                // Keep massive boss-like HP & giant blasts while gently pacing raw per-hit burst so fight stays thrilling!
                val advantage = abs(tierDiff)
                hpMult *= (1.0f + advantage * 0.045f)
                projScale += advantage * 0.07f
                dmgMult *= (1.0f - advantage * 0.035f).coerceAtLeast(0.88f)
                val hpPct = ((hpMult - 1f) * 100f).toInt().coerceAtLeast(5)
                "Boss Aura: +$hpPct% Bonus HP & Giant Blasts (${myWorld.simpleName})"
            }
            else -> {
                "Even Scale: Both fighters are ${myWorld.simpleName} (${myRace.simpleName})"
            }
        }

        return LoreMatchupBalance(
            hpMultiplier = hpMult.coerceIn(0.90f, 1.28f),
            damageMultiplier = dmgMult.coerceIn(0.86f, 1.32f),
            critBonusMultiplier = critMult.coerceIn(1.0f, 1.42f),
            ultChargeMultiplier = ultChargeMult.coerceIn(0.95f, 1.55f),
            autoCounterBonusChance = counterBonus.coerceIn(0f, 0.14f),
            projectileScale = projScale.coerceIn(0.95f, 1.45f),
            extraVolleyCount = extraVolley,
            lowHpZenkaiScale = zenkai,
            onCritHealFlat = critHeal,
            simpleMatchupNote = note
        )
    }
}
