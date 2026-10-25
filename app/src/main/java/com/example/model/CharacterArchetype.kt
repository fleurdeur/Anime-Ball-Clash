package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max

enum class CharacterClass(val displayName: String, val shortRole: String) {
    ATTACKER("Heavy", "Strong Hits"),
    SPEEDSTER("Fast", "Quick Speed"),
    CONTROL_MAGIC("Magic", "Control")
}

enum class CharacterAlignment(val displayName: String, val badgeLabel: String) {
    PROTAGONIST("Hero", "HERO"),
    ANTAGONIST("Villain", "VILLAIN")
}

enum class AnimeTier(val displayName: String) {
    POPULAR("Popular"),
    CULT("Classic"),
    NICHE("Rare")
}

enum class ElementTag(val displayName: String, val badgeColor: Long) {
    ALL("All", 0xFF475569),
    FIRE("Fire", 0xFFE5392D),
    ELEC("Bolt", 0xFF0284C7),
    SLASH("Slash", 0xFF16A34A),
    HEAVY("Smash", 0xFFD97706),
    MAGIC("Magic", 0xFF7C3AED)
}

enum class WeaponVisualType {
    KI_HALO,
    NICHIRIN_KATANA,
    LIMITLESS_ORBS,
    LIGHTNING_KUNAI,
    SERIOUS_GLOVE,
    GOMU_FIST,
    TRIPLE_KATANA,
    CHAINSAW_BLADE,
    CURSED_CLEAVE,
    ZANGETSU_BLADE,
    TITAN_BLADES,
    DRAGON_CLAW,
    SHADOW_DAGGERS,
    PSYCHIC_DEBRIS,
    ALCHEMY_SPEAR,
    STAR_FISTS,
    ALLOY_YOYOS,
    GRIMOIRE_GREATSWORD,
    HELLSING_PISTOLS,
    MAGE_STAFF,
    CRESCENT_SCYTHE,
    SPIRAL_DRILL,
    SPIRIT_BOW,
    MECHA_FUNNELS,
    ROYAL_GATES,
    DEMON_KANABO,
    CHAKRAM_RINGS,
    ARCANE_CARDS,
    EXCALIBUR_BLADE,
    CYBER_CANNON
}

/**
 * 18 deterministic, working combat passives so EVERY character in the 220+ roster
 * triggers authentic gameplay mechanics on collision, defense, or wall ricochet.
 */
enum class PassiveArchetype(val shortTag: String) {
    SHOCKWAVE_KNOCKBACK("BIG PUSH"),
    SCORCH_BURN("FIRE BURN"),
    CRIMSON_BLEED("BLEED"),
    LIGHTNING_PARALYZE("STUN ZAP"),
    ELASTIC_MOMENTUM("WALL BOOST"),
    INFINITY_BARRIER("SHIELD"),
    VAMPIRIC_SIPHON("LIFE STEAL"),
    ANTI_MAGIC_DRAIN("DRAIN ULT"),
    BERSERKER_RAGE("LOW HP RAGE"),
    VECTOR_REFLECT("REFLECT"),
    SHADOW_MONARCH_STACK("SPEED UP"),
    ADAPTIVE_EVOLUTION("POWER UP"),
    FROST_GRAVITY_SLOW("SLOW DOWN"),
    BLACK_FLASH_CRIT("CRIT HIT"),
    REGEN_IMMORTAL("HEAL"),
    EXECUTE_SHRINE("FINISHER"),
    ARCANE_OVERCHARGE("FAST ULT"),
    TITAN_JUGGERNAUT("HEAVY ARMOR")
}

/**
 * 10 distinct Ultimate Awakening mechanics across all 220+ fighters.
 */
enum class UltimateArchetype {
    HYPER_AWAKEN,      // Massive speed + damage aura transformation
    TIME_STOP,         // Freezes rival sphere in place for 1.65s
    DOMAIN_STUN,       // Spatial lock + defense shred
    METEOR_NUKE,       // Instant AoE burst damage + knockback + crater hazard
    SERIOUS_ONE_PUNCH, // Arms a devastating single-hit impact multiplier
    HEAL_REFORGE,      // Restores substantial HP + grants barrier
    ULT_NULLIFY,       // Cancels active rival Ultimate or drains rival gauge + boosts slash
    HAZARD_STORM,      // Spawns multiple elemental hazard zones across the court
    GRAVITY_CRUSH,     // Pulls/crushes rival + heavy slow + burst damage
    SOUL_DEVOUR        // Steals rival HP & Ultimate meter while boosting speed
}

/**
 * Active Counter Ability system across all 224 characters:
 * - Normal fighters block & push back incoming attacks.
 * - Specialized fighters gain thematic immunities (Time Immunity, Anti-Magic, Infinity, Stand Rush, Full Reflect, etc.).
 */
enum class CounterArchetype(
    val shortLabel: String,
    val simpleName: String,
    val simpleDesc: String,
    val popupCallout: String
) {
    GUARD_PARRY(
        shortLabel = "AUTO BLOCK",
        simpleName = "Block & Push",
        simpleDesc = "Blocks the hit with zero damage and pushes rival back.",
        popupCallout = "BLOCKED!"
    ),
    TELEPORT_STRIKE(
        shortLabel = "TELEPORT",
        simpleName = "Teleport Behind",
        simpleDesc = "Teleports behind rival to dodge the hit and attack from behind!",
        popupCallout = "TELEPORT BEHIND!"
    ),
    TIME_IMMUNITY(
        shortLabel = "TIME IMMUNE",
        simpleName = "Time Immune",
        simpleDesc = "Breaks Time Stop & stuns, blocks damage, and freezes rival.",
        popupCallout = "TIME IMMUNE!"
    ),
    ANTI_MAGIC_NULL(
        shortLabel = "ANTI-MAGIC",
        simpleName = "Anti-Magic",
        simpleDesc = "Blocks magic & Ultimates, cancels rival Ult, and drains meter.",
        popupCallout = "ANTI-MAGIC!"
    ),
    INFINITY_BARRIER_COUNTER(
        shortLabel = "INFINITY",
        simpleName = "Untouchable",
        simpleDesc = "Stops the hit before it touches you and blasts rival away.",
        popupCallout = "INFINITY!"
    ),
    STAND_RUSH_COUNTER(
        shortLabel = "STAND GUARD",
        simpleName = "Stand Counter",
        simpleDesc = "Stand spirit blocks the hit and punches back fast!",
        popupCallout = "STAND RUSH!"
    ),
    VECTOR_REFLECT_COUNTER(
        shortLabel = "REFLECT",
        simpleName = "Full Reflect",
        simpleDesc = "Blocks all damage and sends the hit right back at rival.",
        popupCallout = "REFLECTED!"
    ),
    SHADOW_DODGE(
        shortLabel = "DODGE",
        simpleName = "Quick Dodge",
        simpleDesc = "Slips past the attack with zero damage and boosts speed.",
        popupCallout = "DODGED!"
    ),
    TITAN_ARMOR_COUNTER(
        shortLabel = "IRON GUARD",
        simpleName = "Iron Armor",
        simpleDesc = "Shrugs off hits and stuns with zero damage, then slams back.",
        popupCallout = "ARMOR SLAM!"
    )
}

enum class JoJoStandStyle(val standName: String, val battleCry: String) {
    NONE("", ""),
    STAR_PLATINUM("Star Platinum", "ORA ORA ORA!"),
    THE_WORLD("The World", "MUDA MUDA MUDA!"),
    GOLD_EXPERIENCE_REQUIEM("Gold Experience Requiem", "MUDA MUDA! ZERO!"),
    KING_CRIMSON("King Crimson", "TIME ERASED!"),
    KILLER_QUEEN("Killer Queen", "CLICK! BOOM!"),
    MADE_IN_HEAVEN("Made in Heaven", "HEAVEN SPEED!"),
    HERMIT_PURPLE("Hermit Purple", "HAMON OVERDRIVE!")
}

enum class UpgradeStatType(val title: String, val shortLabel: String) {
    HEALTH("Health", "HP"),
    POWER("Power", "PWR"),
    COOLDOWN("Faster Ult", "CD")
}

data class CharacterUpgradeState(
    val healthLevel: Int = 0,
    val powerLevel: Int = 0,
    val cooldownLevel: Int = 0
) {
    val totalUpgrades: Int get() = healthLevel + powerLevel + cooldownLevel

    fun getLevel(type: UpgradeStatType): Int = when (type) {
        UpgradeStatType.HEALTH -> healthLevel
        UpgradeStatType.POWER -> powerLevel
        UpgradeStatType.COOLDOWN -> cooldownLevel
    }

    fun isMaxed(type: UpgradeStatType): Boolean = getLevel(type) >= MAX_LEVEL

    fun nextCostGold(type: UpgradeStatType): Int = costForNextLevel(getLevel(type))

    fun effectiveMaxHp(baseMaxHp: Int): Int = baseMaxHp + (healthLevel * HP_PER_LEVEL)

    fun effectiveBaseDamage(baseDamage: Int): Int = baseDamage + (powerLevel * DAMAGE_PER_LEVEL)

    fun effectiveUltimateMultiplier(baseMult: Float): Float = baseMult + (powerLevel * 0.04f)

    fun effectiveUltimateCooldown(baseCd: Float): Float =
        max(MIN_ULT_COOLDOWN_SEC, baseCd - cooldownLevel * CD_REDUCTION_PER_LEVEL)

    fun redirectCooldownReduction(): Float = cooldownLevel * 0.09f

    companion object {
        const val MAX_LEVEL = 10
        const val HP_PER_LEVEL = 35
        const val DAMAGE_PER_LEVEL = 2
        const val CD_REDUCTION_PER_LEVEL = 0.45f
        const val MIN_ULT_COOLDOWN_SEC = 3.2f

        private val LEVEL_COSTS = intArrayOf(
            2_500,   // Lv 0 -> 1
            5_000,   // Lv 1 -> 2
            9_000,   // Lv 2 -> 3
            15_000,  // Lv 3 -> 4
            24_000,  // Lv 4 -> 5
            36_000,  // Lv 5 -> 6
            52_000,  // Lv 6 -> 7
            72_000,  // Lv 7 -> 8
            98_000,  // Lv 8 -> 9
            135_000  // Lv 9 -> 10
        )

        fun costForNextLevel(currentLevel: Int): Int {
            if (currentLevel >= MAX_LEVEL) return 0
            return LEVEL_COSTS[currentLevel.coerceIn(0, LEVEL_COSTS.lastIndex)]
        }
    }
}

enum class EyeVisualStyle {
    FIERCE_SHONEN,
    CRIMSON_SHARINGAN,
    RINNEGAN_RIPPLE,
    HYPNOTIC_RINGS,
    SIX_EYES_VISOR,
    CYBER_VISOR,
    STAR_SHADES,
    DEMON_SLIT,
    HOLLOW_MASK,
    ESPER_PSYCHIC,
    SERENE_NOBLE,
    SCARRED_ONE_EYE
}

enum class CrownVisualStyle {
    SAIYAN_WILD,
    SHONEN_SPIKY,
    SLICK_VILLAIN,
    FLOWING_MANE,
    TWIN_TAILS,
    BOWL_ESPER,
    STRAW_HAT,
    FEDORA_TRILBY,
    PIRATE_TRICORNE,
    SHINOBI_HEADBAND,
    DEMON_HORNS,
    ROYAL_CROWN,
    MECHA_VFIN,
    KNIGHT_HELM,
    SAMURAI_KABUTO,
    WITCH_HOOD,
    HALO_CROWN,
    BALD_GLEAM
}

enum class FaceMarkVisualStyle {
    NONE,
    WHISKER_MARKS,
    FOREHEAD_SCAR,
    CURSE_TATTOOS,
    LIGHTNING_BOLTS,
    CROSS_SCAR,
    FANG_JAW,
    SHINOBI_MASK,
    CYBER_SEAMS,
    ROYAL_GEM,
    STAR_TEAR,
    BLOOD_STRIPE
}

data class CharacterData(
    val id: String,
    val name: String,
    val title: String,
    val animeInspiration: String,
    val alignment: CharacterAlignment = CharacterAlignment.PROTAGONIST,
    val popularityTier: AnimeTier = AnimeTier.POPULAR,
    val classType: CharacterClass,
    val element: ElementTag,
    val maxHp: Int = 360,
    val baseDamage: Int,
    val speedRating: Float, // 1.0 to 1.65 cruise speed multiplier
    val massRating: Float,  // 0.85 to 2.1 collision momentum
    val primaryColor: Color,
    val secondaryColor: Color,
    val auraColor: Color,
    val hairColor: Color,
    val weaponType: WeaponVisualType,
    val passiveArchetype: PassiveArchetype = inferPassiveArchetype(classType, element, alignment),
    val ultimateArchetype: UltimateArchetype = inferUltimateArchetype(classType, element, alignment),
    val passiveName: String,
    val passiveDescription: String,
    val ultimateName: String,
    val ultimateDurationSeconds: Float,
    val ultimateCooldownSeconds: Float = 8.5f,
    val ultimateDamageMultiplier: Float,
    val ultimateDescription: String,
    val voiceLine: String,
    val unlockCostGold: Int = 0,
    val unlockCostGems: Int = 0,
    val isStarter: Boolean = false,
    val eyeStyle: EyeVisualStyle? = null,
    val crownStyle: CrownVisualStyle? = null,
    val faceMarkStyle: FaceMarkVisualStyle? = null,
    val hasCapeOrWings: Boolean = false
) {
    val resolvedEyeStyle: EyeVisualStyle
        get() = eyeStyle ?: run {
            val h = abs(id.hashCode())
            when {
                alignment == CharacterAlignment.ANTAGONIST && element == ElementTag.MAGIC ->
                    if (h % 2 == 0) EyeVisualStyle.RINNEGAN_RIPPLE else EyeVisualStyle.HYPNOTIC_RINGS
                alignment == CharacterAlignment.ANTAGONIST ->
                    when (h % 4) {
                        0 -> EyeVisualStyle.DEMON_SLIT
                        1 -> EyeVisualStyle.CRIMSON_SHARINGAN
                        2 -> EyeVisualStyle.HOLLOW_MASK
                        else -> EyeVisualStyle.HYPNOTIC_RINGS
                    }
                element == ElementTag.MAGIC ->
                    if (h % 2 == 0) EyeVisualStyle.SERENE_NOBLE else EyeVisualStyle.ESPER_PSYCHIC
                element == ElementTag.ELEC ->
                    if (h % 3 == 0) EyeVisualStyle.CYBER_VISOR else EyeVisualStyle.FIERCE_SHONEN
                element == ElementTag.SLASH ->
                    if (h % 3 == 0) EyeVisualStyle.SCARRED_ONE_EYE else EyeVisualStyle.FIERCE_SHONEN
                else ->
                    if (h % 5 == 0) EyeVisualStyle.STAR_SHADES else EyeVisualStyle.FIERCE_SHONEN
            }
        }

    val resolvedCrownStyle: CrownVisualStyle
        get() = crownStyle ?: run {
            if (hairColor.alpha == 0f) return@run CrownVisualStyle.BALD_GLEAM
            val h = abs(id.hashCode() / 7)
            when {
                weaponType == WeaponVisualType.MECHA_FUNNELS || weaponType == WeaponVisualType.CYBER_CANNON -> CrownVisualStyle.MECHA_VFIN
                alignment == CharacterAlignment.ANTAGONIST && h % 5 == 0 -> CrownVisualStyle.DEMON_HORNS
                alignment == CharacterAlignment.ANTAGONIST && h % 5 == 1 -> CrownVisualStyle.ROYAL_CROWN
                alignment == CharacterAlignment.ANTAGONIST && h % 5 == 2 -> CrownVisualStyle.SLICK_VILLAIN
                element == ElementTag.MAGIC && h % 4 == 0 -> CrownVisualStyle.WITCH_HOOD
                element == ElementTag.MAGIC && h % 4 == 1 -> CrownVisualStyle.TWIN_TAILS
                element == ElementTag.SLASH && h % 4 == 0 -> CrownVisualStyle.SAMURAI_KABUTO
                element == ElementTag.SLASH && h % 4 == 1 -> CrownVisualStyle.SHINOBI_HEADBAND
                element == ElementTag.HEAVY && h % 4 == 0 -> CrownVisualStyle.SAIYAN_WILD
                element == ElementTag.HEAVY && h % 4 == 1 -> CrownVisualStyle.KNIGHT_HELM
                h % 3 == 0 -> CrownVisualStyle.FLOWING_MANE
                else -> CrownVisualStyle.SHONEN_SPIKY
            }
        }

    val resolvedFaceMarkStyle: FaceMarkVisualStyle
        get() = faceMarkStyle ?: run {
            val h = abs(id.hashCode() / 13)
            when (h % 10) {
                0 -> FaceMarkVisualStyle.FOREHEAD_SCAR
                1 -> FaceMarkVisualStyle.CURSE_TATTOOS
                2 -> FaceMarkVisualStyle.CROSS_SCAR
                3 -> FaceMarkVisualStyle.SHINOBI_MASK
                4 -> FaceMarkVisualStyle.LIGHTNING_BOLTS
                5 -> FaceMarkVisualStyle.ROYAL_GEM
                6 -> FaceMarkVisualStyle.FANG_JAW
                7 -> FaceMarkVisualStyle.CYBER_SEAMS
                8 -> FaceMarkVisualStyle.BLOOD_STRIPE
                else -> FaceMarkVisualStyle.STAR_TEAR
            }
        }

    val joJoStandStyle: JoJoStandStyle
        get() = when (id) {
            "kujo_star" -> JoJoStandStyle.STAR_PLATINUM
            "dio_world" -> JoJoStandStyle.THE_WORLD
            "giorno_requiem" -> JoJoStandStyle.GOLD_EXPERIENCE_REQUIEM
            "diavolo_crimson" -> JoJoStandStyle.KING_CRIMSON
            "kira_killer_queen" -> JoJoStandStyle.KILLER_QUEEN
            "pucci_heaven" -> JoJoStandStyle.MADE_IN_HEAVEN
            "joseph_hamon" -> JoJoStandStyle.HERMIT_PURPLE
            else -> JoJoStandStyle.NONE
        }

    val hasStandSpirit: Boolean
        get() = joJoStandStyle != JoJoStandStyle.NONE

    val counterArchetype: CounterArchetype
        get() = when {
            id in setOf("kujo_star", "dio_world", "diavolo_crimson", "pucci_heaven") ->
                CounterArchetype.TIME_IMMUNITY
            id in setOf("giorno_requiem", "kira_killer_queen", "joseph_hamon") ->
                CounterArchetype.STAND_RUSH_COUNTER
            id in setOf("asta_grimoire", "blackbeard_tremor") ||
                passiveArchetype == PassiveArchetype.ANTI_MAGIC_DRAIN ||
                ultimateArchetype == UltimateArchetype.ULT_NULLIFY ->
                CounterArchetype.ANTI_MAGIC_NULL
            id in setOf("satoru_void", "griffith_femto", "madara_uchiha", "aizen_kyoka") ||
                passiveArchetype == PassiveArchetype.INFINITY_BARRIER ->
                CounterArchetype.INFINITY_BARRIER_COUNTER
            id in setOf("elric_fullmetal", "accelerator_vector", "meliodas_wrath", "makima_control") ||
                passiveArchetype == PassiveArchetype.VECTOR_REFLECT ->
                CounterArchetype.VECTOR_REFLECT_COUNTER
            LorePowerBalancer.isTeleportCounterCharacter(this) ->
                CounterArchetype.TELEPORT_STRIKE
            ultimateArchetype == UltimateArchetype.TIME_STOP ->
                CounterArchetype.TIME_IMMUNITY
            id in setOf("yeager_titan", "guts_berserk", "caped_baldy", "kafka_kaiju", "kars_ultimate") ||
                passiveArchetype == PassiveArchetype.TITAN_JUGGERNAUT ||
                (element == ElementTag.HEAVY && massRating >= 1.62f) ->
                CounterArchetype.TITAN_ARMOR_COUNTER
            classType == CharacterClass.SPEEDSTER ->
                CounterArchetype.SHADOW_DODGE
            else ->
                CounterArchetype.GUARD_PARRY
        }

    val animeRace: AnimeRace
        get() = LorePowerBalancer.resolveAnimeRace(this)

    val worldScaleTier: WorldScaleTier
        get() = LorePowerBalancer.resolveWorldScaleTier(this)

    val canonProjectileStyle: CanonProjectileStyle
        get() = LorePowerBalancer.resolveCanonProjectileStyle(this)

    val baseAutoCounterChance: Float
        get() = LorePowerBalancer.resolveBaseAutoCounterChance(this)

    val autoCounterPercent: Int
        get() = (baseAutoCounterChance * 100f).toInt()

    val simpleAutoCounterSummary: String
        get() = "Random chance when hit: ${counterArchetype.simpleDesc}"

    val simplePassiveSummary: String
        get() = when (passiveArchetype) {
            PassiveArchetype.SHOCKWAVE_KNOCKBACK -> "Pushes rival back further and hits +4 harder."
            PassiveArchetype.SCORCH_BURN -> "Burns rival with fire damage after each hit."
            PassiveArchetype.CRIMSON_BLEED -> "Cuts rival to deal extra bleed damage over time."
            PassiveArchetype.LIGHTNING_PARALYZE -> "Zaps and briefly stops rival on hit."
            PassiveArchetype.ELASTIC_MOMENTUM -> "Bounces off walls faster for +50% wall-shot power."
            PassiveArchetype.INFINITY_BARRIER -> "Magic shield cuts incoming hit damage by 22%."
            PassiveArchetype.VAMPIRIC_SIPHON -> "Heals your HP for 28% of hit damage dealt."
            PassiveArchetype.ANTI_MAGIC_DRAIN -> "Drains 14% of rival's Ult bar on every hit."
            PassiveArchetype.BERSERKER_RAGE -> "Hits up to 48% harder when your HP gets low."
            PassiveArchetype.VECTOR_REFLECT -> "Bounces 28% of hit damage right back at rival."
            PassiveArchetype.SHADOW_MONARCH_STACK -> "Gets faster every time you hit the rival."
            PassiveArchetype.ADAPTIVE_EVOLUTION -> "Gets stronger on every hit and charges Ult faster."
            PassiveArchetype.FROST_GRAVITY_SLOW -> "Slows down the rival ball on hit."
            PassiveArchetype.BLACK_FLASH_CRIT -> "Every 3rd hit lands a big 1.45x critical smash."
            PassiveArchetype.REGEN_IMMORTAL -> "Heals HP every 3rd wall bounce."
            PassiveArchetype.EXECUTE_SHRINE -> "Deals +32% extra damage when rival is below half HP."
            PassiveArchetype.ARCANE_OVERCHARGE -> "Fills your Ult bar faster and hits harder above 50% Ult."
            PassiveArchetype.TITAN_JUGGERNAUT -> "Resists pushback and crushes lighter rivals."
        }

    val simpleUltimateSummary: String
        get() = when {
            id == "satoru_void" ->
                "Freezes rival in Domain and shoots Hollow Purple orb at rival + 3–5x Multi-Dash!"
            canonProjectileStyle == CanonProjectileStyle.BABYLON_SWORD_RAIN ->
                "Summons multiple visible swords and throws them at rival for big damage + 3–5x Multi-Dash!"
            canonProjectileStyle == CanonProjectileStyle.FLYING_RAIJIN_KUNAI ->
                "Throws teleport kunai at rival to warp behind and attack + 3–5x Multi-Dash!"
            ultimateArchetype == UltimateArchetype.TIME_STOP ->
                "Stops time to freeze rival and shoots ${canonProjectileStyle.simpleMoveLabel.lowercase().removePrefix("shoots ").removePrefix("fires ")} + 3–5x Multi-Dash!"
            ultimateArchetype == UltimateArchetype.DOMAIN_STUN ->
                "Traps rival in your Domain and ${canonProjectileStyle.simpleMoveLabel.lowercase()} + 3–5x Multi-Dash!"
            ultimateArchetype == UltimateArchetype.METEOR_NUKE ->
                "${canonProjectileStyle.simpleMoveLabel} at rival for big AoE damage + 3–5x Multi-Dash!"
            ultimateArchetype == UltimateArchetype.SERIOUS_ONE_PUNCH ->
                "Next hit deals massive one-punch damage and ${canonProjectileStyle.simpleMoveLabel.lowercase()} + 3–5x Multi-Dash!"
            ultimateArchetype == UltimateArchetype.HEAL_REFORGE ->
                "Heals big HP right away, powers up hits, and ${canonProjectileStyle.simpleMoveLabel.lowercase()} + 3–5x Multi-Dash!"
            ultimateArchetype == UltimateArchetype.ULT_NULLIFY ->
                "Cancels rival's Ultimate, drains Ult bar, and ${canonProjectileStyle.simpleMoveLabel.lowercase()} + 3–5x Multi-Dash!"
            ultimateArchetype == UltimateArchetype.HAZARD_STORM ->
                "${canonProjectileStyle.simpleMoveLabel} and leaves fire zones across the arena + 3–5x Multi-Dash!"
            ultimateArchetype == UltimateArchetype.GRAVITY_CRUSH ->
                "${canonProjectileStyle.simpleMoveLabel} to crush and slow rival + 3–5x Multi-Dash!"
            ultimateArchetype == UltimateArchetype.SOUL_DEVOUR ->
                "${canonProjectileStyle.simpleMoveLabel} to steal rival HP and Ult power + 3–5x Multi-Dash!"
            else ->
                "Big speed boost, ${canonProjectileStyle.simpleMoveLabel.lowercase()}, and 3–5x Multi-Dash!"
        }

    val officialUltVoiceLine: String
        get() = when (id) {
            "kakarot_z" -> "Ka... Me... Ha... Me... HAAA! Ultra Instinct!"
            "kamado_sun" -> "Hinokami Kagura! Clear Blue Sky!"
            "raikiri_shinobi" -> "Kamui! Lightning Blade!"
            "mugiwara_joy" -> "Gear Fifth! Drums of Liberation!"
            "shigaraki_decay" -> "Decay! Everything turns to dust!"
            "midoriya_ofa" -> "One For All, Full Cowling, One Hundred Percent! Smash!"
            "roronoa_ashura" -> "King of Hell! Three Sword Style, Ashura!"
            "dragneel_ignis" -> "Fire Dragon King's Demolition Fist!"
            "satoru_void" -> "Domain Expansion: Unlimited Void! Hollow Purple!"
            "gilgamesh_babylon" -> "Gate of Babylon! Enuma Elish! Rain of Swords!"
            "minato_flash" -> "Flying Raijin! Flash Rasengan!"
            "archer_ubw" -> "I am the bone of my sword! Unlimited Blade Works!"
            "byakuya_senbonzakura" -> "Scatter... Senbonzakura Kageyoshi!"
            "frieza_emperor" -> "Tremble before Golden Frieza!"
            "chainsaw_devil" -> "Roar, Chainsaw! Tear them apart!"
            "elric_fullmetal" -> "Fullmetal Alchemy! Transmute!"
            "muzan_progenitor" -> "Blood Demon Art! Bow before Muzan!"
            "asta_grimoire" -> "Black Asta! Devil Union! My magic is never giving up!"
            "okarun_turbo" -> "All-out full throttle! Turbo Power!"
            "kurosaki_hollow" -> "Bankai! Tensa Zangetsu! Getsuga Tensho!"
            "aizen_kyoka" -> "Shatter, Kyoka Suigetsu! Kurohitsugi!"
            "yeager_titan" -> "The Rumbling begins! Keep moving forward!"
            "rimuru_tempest" -> "Beelzebuth! Devour everything!"
            "killua_godspeed" -> "Kanmuru! Whirlwind Godspeed!"
            "kafka_kaiju" -> "Kaiju Number Eight, Full Release!"
            "makima_control" -> "Control Devil Contract... Bang!"
            "kageyama_esper" -> "One Hundred Percent! Psychic Explosion!"
            "blackbeard_tremor" -> "Zehahaha! Black Vortex! Quake Crash!"
            "madara_uchiha" -> "Wake up to reality! Perfect Susanoo!"
            "kujo_star" -> "Star Platinum: The World! Time has stopped! ORA ORA ORA!"
            "dio_world" -> "ZA WARUDO! Stop time! MUDA MUDA MUDA!"
            "caped_baldy" -> "Serious Series: Serious Punch!"
            "alucard_hellsing" -> "Release Control Art Restriction Level Zero!"
            "meruem_chimera" -> "Kneel before the King of Evolution!"
            "guts_berserk" -> "Berserker Armor! Dragon Slayer Crush!"
            "griffith_femto" -> "Wings of Darkness! Eclipse Gravity!"
            "sukuna_shrine" -> "Domain Expansion: Malevolent Shrine! Cleave!"
            "naruto_baryon" -> "Baryon Mode! Rasengan Barrage!"
            "sasuke_rinnegan" -> "Perfect Susanoo! Indra's Arrow!"
            "giorno_requiem" -> "Gold Experience Requiem! Return to Zero! MUDA MUDA MUDA!"
            "diavolo_crimson" -> "King Crimson! Time has been erased!"
            "kira_killer_queen" -> "Killer Queen! Third Bomb, Bites the Dust!"
            "pucci_heaven" -> "Made in Heaven! Time accelerates!"
            "joseph_hamon" -> "Hermit Purple! Sunlight Yellow Overdrive!"
            "kars_ultimate" -> "Ultimate Lifeform! Red Stone Dawn!"
            "meliodas_wrath" -> "Assault Mode! Full Counter!"
            else -> voiceLine
        }

    companion object {
        fun inferPassiveArchetype(
            cls: CharacterClass,
            elem: ElementTag,
            align: CharacterAlignment
        ): PassiveArchetype = when {
            elem == ElementTag.FIRE -> PassiveArchetype.SCORCH_BURN
            elem == ElementTag.ELEC -> PassiveArchetype.LIGHTNING_PARALYZE
            elem == ElementTag.SLASH && align == CharacterAlignment.ANTAGONIST -> PassiveArchetype.EXECUTE_SHRINE
            elem == ElementTag.SLASH -> PassiveArchetype.CRIMSON_BLEED
            elem == ElementTag.MAGIC && align == CharacterAlignment.ANTAGONIST -> PassiveArchetype.ANTI_MAGIC_DRAIN
            elem == ElementTag.MAGIC -> PassiveArchetype.ARCANE_OVERCHARGE
            cls == CharacterClass.SPEEDSTER -> PassiveArchetype.ELASTIC_MOMENTUM
            align == CharacterAlignment.ANTAGONIST -> PassiveArchetype.VAMPIRIC_SIPHON
            else -> PassiveArchetype.SHOCKWAVE_KNOCKBACK
        }

        fun inferUltimateArchetype(
            cls: CharacterClass,
            elem: ElementTag,
            align: CharacterAlignment
        ): UltimateArchetype = when {
            elem == ElementTag.FIRE -> UltimateArchetype.HAZARD_STORM
            elem == ElementTag.MAGIC && align == CharacterAlignment.ANTAGONIST -> UltimateArchetype.GRAVITY_CRUSH
            elem == ElementTag.MAGIC -> UltimateArchetype.DOMAIN_STUN
            cls == CharacterClass.CONTROL_MAGIC -> UltimateArchetype.TIME_STOP
            align == CharacterAlignment.ANTAGONIST && elem == ElementTag.HEAVY -> UltimateArchetype.METEOR_NUKE
            else -> UltimateArchetype.HYPER_AWAKEN
        }
    }
}

/**
 * Concise factory helper used by our expansion packs to create rich, fully-specified
 * anime fighters with authentic passives, ultimates, visuals, and inflated costs.
 */
fun fighter(
    id: String,
    name: String,
    title: String,
    anime: String,
    align: CharacterAlignment,
    tier: AnimeTier,
    cls: CharacterClass,
    elem: ElementTag,
    hp: Int,
    dmg: Int,
    spd: Float,
    mass: Float,
    c1: Long,
    c2: Long,
    aura: Long,
    hair: Long,
    weapon: WeaponVisualType,
    passiveType: PassiveArchetype,
    passiveName: String,
    passiveDesc: String,
    ultType: UltimateArchetype,
    ultName: String,
    ultDur: Float,
    ultCd: Float,
    ultMult: Float,
    ultDesc: String,
    quote: String,
    costGold: Int,
    eye: EyeVisualStyle? = null,
    crown: CrownVisualStyle? = null,
    mark: FaceMarkVisualStyle? = null,
    cape: Boolean = false
): CharacterData = CharacterData(
    id = id,
    name = name,
    title = title,
    animeInspiration = anime,
    alignment = align,
    popularityTier = tier,
    classType = cls,
    element = elem,
    maxHp = hp,
    baseDamage = dmg,
    speedRating = spd,
    massRating = mass,
    primaryColor = Color(c1),
    secondaryColor = Color(c2),
    auraColor = Color(aura),
    hairColor = Color(hair),
    weaponType = weapon,
    passiveArchetype = passiveType,
    ultimateArchetype = ultType,
    passiveName = passiveName,
    passiveDescription = passiveDesc,
    ultimateName = ultName,
    ultimateDurationSeconds = ultDur,
    ultimateCooldownSeconds = ultCd,
    ultimateDamageMultiplier = ultMult,
    ultimateDescription = ultDesc,
    voiceLine = quote,
    unlockCostGold = costGold,
    eyeStyle = eye,
    crownStyle = crown,
    faceMarkStyle = mark,
    hasCapeOrWings = cape
)

object CharacterRoster {
    private val coreRoster = listOf(
        // 1. STARTER: Kakarot (Dragon Ball Z)
        CharacterData(
            id = "kakarot_z",
            name = "Kakarot",
            title = "Saiyan Vanguard",
            animeInspiration = "Dragon Ball Z",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.ATTACKER,
            element = ElementTag.HEAVY,
            maxHp = 370,
            baseDamage = 14,
            speedRating = 1.25f,
            massRating = 1.48f,
            primaryColor = Color(0xFFF97316),
            secondaryColor = Color(0xFF1D4ED8),
            auraColor = Color(0xFFFACC15),
            hairColor = Color(0xFF18181B),
            weaponType = WeaponVisualType.KI_HALO,
            passiveArchetype = PassiveArchetype.SHOCKWAVE_KNOCKBACK,
            ultimateArchetype = UltimateArchetype.HYPER_AWAKEN,
            passiveName = "Ki Shockwave",
            passiveDescription = "Blasts rivals away with +38% knockback impulse and +4 shockwave bonus damage.",
            ultimateName = "Ultra Awakening",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.65f,
            ultimateDescription = "Transforms hair to radiant silver-cyan, boosting speed +30% and collision power 1.65x.",
            voiceLine = "This is what lies beyond my limit!",
            unlockCostGold = 0,
            isStarter = true
        ),
        // 2. STARTER: Kamado (Demon Slayer)
        CharacterData(
            id = "kamado_sun",
            name = "Tanjiro",
            title = "Sun Breather",
            animeInspiration = "Demon Slayer",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.SPEEDSTER,
            element = ElementTag.SLASH,
            maxHp = 350,
            baseDamage = 12,
            speedRating = 1.38f,
            massRating = 1.08f,
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF18181B),
            auraColor = Color(0xFF38BDF8),
            hairColor = Color(0xFF7F1D1D),
            weaponType = WeaponVisualType.NICHIRIN_KATANA,
            passiveArchetype = PassiveArchetype.CRIMSON_BLEED,
            ultimateArchetype = UltimateArchetype.HAZARD_STORM,
            passiveName = "Crimson Nichirin",
            passiveDescription = "Inflicts 4 ticks of Crimson Bleed and +12% critical strike edge on sword clashes.",
            ultimateName = "Hinokami Kagura",
            ultimateDurationSeconds = 6.0f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.50f,
            ultimateDescription = "Ignites Nichirin blade, leaving blazing sun-fire zones across the court.",
            voiceLine = "Set your heart ablaze—Hinokami Dance!",
            unlockCostGold = 0,
            isStarter = true
        ),
        // 3. STARTER: Kakashi (Naruto)
        CharacterData(
            id = "raikiri_shinobi",
            name = "Kakashi",
            title = "Copy Ninja",
            animeInspiration = "Naruto Shippuden",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.SPEEDSTER,
            element = ElementTag.ELEC,
            maxHp = 340,
            baseDamage = 13,
            speedRating = 1.46f,
            massRating = 0.98f,
            primaryColor = Color(0xFF334155),
            secondaryColor = Color(0xFF0F172A),
            auraColor = Color(0xFF38BDF8),
            hairColor = Color(0xFFE2E8F0),
            weaponType = WeaponVisualType.LIGHTNING_KUNAI,
            passiveArchetype = PassiveArchetype.LIGHTNING_PARALYZE,
            ultimateArchetype = UltimateArchetype.HYPER_AWAKEN,
            passiveName = "Chidori Jolt",
            passiveDescription = "Paralyzes rival momentum for 0.25s on direct lightning impact and boosts agility.",
            ultimateName = "Kamui Overdrive",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.0f,
            ultimateDamageMultiplier = 1.48f,
            ultimateDescription = "Accelerates every wall ricochet by +8% up to hyper-sonic arena speed.",
            voiceLine = "Lightning Blade—one thousand birds!",
            unlockCostGold = 0,
            isStarter = true
        ),
        // 4. STARTER: Luffy (One Piece)
        CharacterData(
            id = "mugiwara_joy",
            name = "Luffy",
            title = "Sun God Captain",
            animeInspiration = "One Piece",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.SPEEDSTER,
            element = ElementTag.HEAVY,
            maxHp = 365,
            baseDamage = 13,
            speedRating = 1.42f,
            massRating = 1.16f,
            primaryColor = Color(0xFFEF4444),
            secondaryColor = Color(0xFFFACC15),
            auraColor = Color(0xFFF43F5E),
            hairColor = Color(0xFF18181B),
            weaponType = WeaponVisualType.GOMU_FIST,
            passiveArchetype = PassiveArchetype.ELASTIC_MOMENTUM,
            ultimateArchetype = UltimateArchetype.HYPER_AWAKEN,
            passiveName = "Rubber Snapback",
            passiveDescription = "Gains +20% slingshot speed burst off every wall bounce and +50% bank-shot power.",
            ultimateName = "Gear Fifth: Nika",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.60f,
            ultimateDescription = "Awakens cloud-white Sun God form, emitting liberation shockwaves on every bounce.",
            voiceLine = "The drums of liberation are beating!",
            unlockCostGold = 0,
            isStarter = true
        ),
        // 5. Shigaraki
        CharacterData(
            id = "shigaraki_decay",
            name = "Shigaraki",
            title = "Symbol of Fear",
            animeInspiration = "My Hero Academia",
            alignment = CharacterAlignment.ANTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.CONTROL_MAGIC,
            element = ElementTag.MAGIC,
            maxHp = 365,
            baseDamage = 14,
            speedRating = 1.32f,
            massRating = 1.28f,
            primaryColor = Color(0xFF334155),
            secondaryColor = Color(0xFF7F1D1D),
            auraColor = Color(0xFFEF4444),
            hairColor = Color(0xFFE2E8F0),
            weaponType = WeaponVisualType.CURSED_CLEAVE,
            passiveArchetype = PassiveArchetype.CRIMSON_BLEED,
            ultimateArchetype = UltimateArchetype.HAZARD_STORM,
            passiveName = "Decay Touch",
            passiveDescription = "Ignores shields and inflicts 4 ticks of crumbling Decay damage on contact.",
            ultimateName = "All For Decay",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 9.0f,
            ultimateDamageMultiplier = 1.55f,
            ultimateDescription = "Spreads crumbling decay zones across the court on activation and movement.",
            voiceLine = "Everything my fingers touch turns to dust!",
            unlockCostGold = 6_500,
            hasCapeOrWings = true
        ),
        // 6. Deku / Midoriya
        CharacterData(
            id = "midoriya_ofa",
            name = "Midoriya",
            title = "Ninth Inheritor",
            animeInspiration = "My Hero Academia",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.SPEEDSTER,
            element = ElementTag.HEAVY,
            maxHp = 355,
            baseDamage = 14,
            speedRating = 1.44f,
            massRating = 1.24f,
            primaryColor = Color(0xFF15803D),
            secondaryColor = Color(0xFF0F172A),
            auraColor = Color(0xFF34D399),
            hairColor = Color(0xFF064E3B),
            weaponType = WeaponVisualType.SERIOUS_GLOVE,
            passiveArchetype = PassiveArchetype.ELASTIC_MOMENTUM,
            ultimateArchetype = UltimateArchetype.HYPER_AWAKEN,
            passiveName = "Fa Jin Kinetic Bank",
            passiveDescription = "Stores kinetic energy per wall bounce for +50% stronger bank-shot smashes.",
            ultimateName = "Gearshift Overdrive",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.62f,
            ultimateDescription = "Breaks inertia laws with +36% hyper-speed and 1.62x Detroit Smash power.",
            voiceLine = "One For All—Full Cowling 100%!",
            unlockCostGold = 7_200,
            hasCapeOrWings = true
        ),
        // 7. Zoro
        CharacterData(
            id = "roronoa_ashura",
            name = "Zoro",
            title = "King of Hell",
            animeInspiration = "One Piece",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.ATTACKER,
            element = ElementTag.SLASH,
            maxHp = 385,
            baseDamage = 15,
            speedRating = 1.28f,
            massRating = 1.45f,
            primaryColor = Color(0xFF166534),
            secondaryColor = Color(0xFF18181B),
            auraColor = Color(0xFF22C55E),
            hairColor = Color(0xFF22C55E),
            weaponType = WeaponVisualType.TRIPLE_KATANA,
            passiveArchetype = PassiveArchetype.CRIMSON_BLEED,
            ultimateArchetype = UltimateArchetype.HYPER_AWAKEN,
            passiveName = "Santoryu Reach",
            passiveDescription = "Extended 3-sword hitbox inflicts deep Bleed and +12% critical slash damage.",
            ultimateName = "King of Hell: Ashura",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.68f,
            ultimateDescription = "Coats all three katanas in emerald Conqueror aura for 1.68x slash damage.",
            voiceLine = "Three-Sword Serpent—Ashura Blades Drawn!",
            unlockCostGold = 8_500
        ),
        // 8. Natsu
        CharacterData(
            id = "dragneel_ignis",
            name = "Natsu",
            title = "Salamander",
            animeInspiration = "Fairy Tail",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.ATTACKER,
            element = ElementTag.FIRE,
            maxHp = 375,
            baseDamage = 14,
            speedRating = 1.32f,
            massRating = 1.38f,
            primaryColor = Color(0xFFE11D48),
            secondaryColor = Color(0xFFF8FAFC),
            auraColor = Color(0xFFF97316),
            hairColor = Color(0xFFF472B6),
            weaponType = WeaponVisualType.DRAGON_CLAW,
            passiveArchetype = PassiveArchetype.SCORCH_BURN,
            ultimateArchetype = UltimateArchetype.HAZARD_STORM,
            passiveName = "Dragon Slayer Flame",
            passiveDescription = "Ignites the court on impact, scorching rivals with lingering dragon fire.",
            ultimateName = "Fire Dragon King Mode",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.60f,
            ultimateDescription = "Erupts blazing inferno craters on activation and every wall bounce.",
            voiceLine = "I'm all fired up now!",
            unlockCostGold = 9_200
        ),
        // 9. Gojo Satoru
        CharacterData(
            id = "satoru_void",
            name = "Gojo",
            title = "The Honored One",
            animeInspiration = "Jujutsu Kaisen",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.CONTROL_MAGIC,
            element = ElementTag.MAGIC,
            maxHp = 380,
            baseDamage = 14,
            speedRating = 1.36f,
            massRating = 1.35f,
            primaryColor = Color(0xFF1E293B),
            secondaryColor = Color(0xFF0F172A),
            auraColor = Color(0xFF38BDF8),
            hairColor = Color(0xFFF8FAFC),
            weaponType = WeaponVisualType.LIMITLESS_ORBS,
            passiveArchetype = PassiveArchetype.INFINITY_BARRIER,
            ultimateArchetype = UltimateArchetype.DOMAIN_STUN,
            passiveName = "Limitless Infinity",
            passiveDescription = "Spatial distortion barrier reduces all incoming collision damage by 22%.",
            ultimateName = "Unlimited Void",
            ultimateDurationSeconds = 5.0f,
            ultimateCooldownSeconds = 9.5f,
            ultimateDamageMultiplier = 1.62f,
            ultimateDescription = "Paralyzes the rival sphere inside Unlimited Void for 1.55s and unleashes Hollow Purple.",
            voiceLine = "Throughout Heaven and Earth, I alone am the honored one.",
            unlockCostGold = 42_000
        ),
        // 10. Frieza
        CharacterData(
            id = "frieza_emperor",
            name = "Frieza",
            title = "Galactic Tyrant",
            animeInspiration = "Dragon Ball Super",
            alignment = CharacterAlignment.ANTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.SPEEDSTER,
            element = ElementTag.MAGIC,
            maxHp = 360,
            baseDamage = 15,
            speedRating = 1.44f,
            massRating = 1.26f,
            primaryColor = Color(0xFFF8FAFC),
            secondaryColor = Color(0xFF7E22CE),
            auraColor = Color(0xFFA855F7),
            hairColor = Color(0x00000000),
            weaponType = WeaponVisualType.KI_HALO,
            passiveArchetype = PassiveArchetype.EXECUTE_SHRINE,
            ultimateArchetype = UltimateArchetype.HYPER_AWAKEN,
            passiveName = "Imperial Cruelty",
            passiveDescription = "Deals +30% ruthless execution damage against weakened rivals below 50% HP.",
            ultimateName = "Golden Emperor",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.66f,
            ultimateDescription = "Transforms into gleaming Golden Frieza with +32% speed and 1.66x Death Beam force.",
            voiceLine = "Kneel before Golden Frieza!",
            unlockCostGold = 24_000
        ),
        // 11. Denji / Chainsaw Man
        CharacterData(
            id = "chainsaw_devil",
            name = "Denji",
            title = "Chainsaw Hybrid",
            animeInspiration = "Chainsaw Man",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.ATTACKER,
            element = ElementTag.SLASH,
            maxHp = 375,
            baseDamage = 14,
            speedRating = 1.36f,
            massRating = 1.38f,
            primaryColor = Color(0xFFEA580C),
            secondaryColor = Color(0xFF18181B),
            auraColor = Color(0xFFEF4444),
            hairColor = Color(0xFFFACC15),
            weaponType = WeaponVisualType.CHAINSAW_BLADE,
            passiveArchetype = PassiveArchetype.VAMPIRIC_SIPHON,
            ultimateArchetype = UltimateArchetype.SOUL_DEVOUR,
            passiveName = "Blood Engine",
            passiveDescription = "Revving chainsaws siphon 28% of collision damage dealt back as fresh HP.",
            ultimateName = "Hero of Hell",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.60f,
            ultimateDescription = "Full devil rev drains rival vitality on activation and boosts speed +28%.",
            voiceLine = "Pull the starter cord—roar, Chainsaw!",
            unlockCostGold = 15_000
        ),
        // 12. Edward Elric
        CharacterData(
            id = "elric_fullmetal",
            name = "Edward",
            title = "Fullmetal Alchemist",
            animeInspiration = "Fullmetal Alchemist: Brotherhood",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.CONTROL_MAGIC,
            element = ElementTag.HEAVY,
            maxHp = 370,
            baseDamage = 13,
            speedRating = 1.32f,
            massRating = 1.40f,
            primaryColor = Color(0xFFDC2626),
            secondaryColor = Color(0xFF18181B),
            auraColor = Color(0xFF38BDF8),
            hairColor = Color(0xFFFACC15),
            weaponType = WeaponVisualType.ALCHEMY_SPEAR,
            passiveArchetype = PassiveArchetype.VECTOR_REFLECT,
            ultimateArchetype = UltimateArchetype.HEAL_REFORGE,
            passiveName = "Equivalent Exchange",
            passiveDescription = "Automail counter-transmutation reflects 26% of incoming collision damage back.",
            ultimateName = "Truth Transmutation",
            ultimateDurationSeconds = 5.0f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.52f,
            ultimateDescription = "Reforges armor to restore +48 HP immediately and amplify spear strikes.",
            voiceLine = "A lesson without pain is meaningless!",
            unlockCostGold = 13_500,
            hasCapeOrWings = true
        ),
        // 13. Muzan Kibutsuji
        CharacterData(
            id = "muzan_progenitor",
            name = "Muzan",
            title = "Demon King",
            animeInspiration = "Demon Slayer",
            alignment = CharacterAlignment.ANTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.ATTACKER,
            element = ElementTag.SLASH,
            maxHp = 395,
            baseDamage = 15,
            speedRating = 1.35f,
            massRating = 1.45f,
            primaryColor = Color(0xFF18181B),
            secondaryColor = Color(0xFF991B1B),
            auraColor = Color(0xFFE11D48),
            hairColor = Color(0xFF09090B),
            weaponType = WeaponVisualType.CURSED_CLEAVE,
            passiveArchetype = PassiveArchetype.VAMPIRIC_SIPHON,
            ultimateArchetype = UltimateArchetype.SOUL_DEVOUR,
            passiveName = "Demon Progenitor Blood",
            passiveDescription = "Siphons rival life force on every whip clash and regenerates cellular damage.",
            ultimateName = "Biokinesis Tempest",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 9.0f,
            ultimateDamageMultiplier = 1.64f,
            ultimateDescription = "Drains rival HP & Ultimate gauge while unleashing 1.64x demon whip fury.",
            voiceLine = "I am a natural disaster—bow and perish.",
            unlockCostGold = 28_000
        ),
        // 14. Asta
        CharacterData(
            id = "asta_grimoire",
            name = "Asta",
            title = "Anti-Magic Knight",
            animeInspiration = "Black Clover",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.ATTACKER,
            element = ElementTag.SLASH,
            maxHp = 380,
            baseDamage = 14,
            speedRating = 1.36f,
            massRating = 1.50f,
            primaryColor = Color(0xFF27272A),
            secondaryColor = Color(0xFF991B1B),
            auraColor = Color(0xFFEF4444),
            hairColor = Color(0xFFD4D4D8),
            weaponType = WeaponVisualType.GRIMOIRE_GREATSWORD,
            passiveArchetype = PassiveArchetype.ANTI_MAGIC_DRAIN,
            ultimateArchetype = UltimateArchetype.ULT_NULLIFY,
            passiveName = "Anti-Magic Sever",
            passiveDescription = "Every greatsword hit burns 11% off the rival's Ultimate meter.",
            ultimateName = "Devil Union: Black Divider",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.62f,
            ultimateDescription = "Cancels any active rival Awakening immediately and boosts slash force 1.62x!",
            voiceLine = "My magic is never giving up!",
            unlockCostGold = 16_500,
            hasCapeOrWings = true
        ),
        // 15. Okarun
        CharacterData(
            id = "okarun_turbo",
            name = "Okarun",
            title = "Turbo Curse Vessel",
            animeInspiration = "DanDaDan",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.SPEEDSTER,
            element = ElementTag.ELEC,
            maxHp = 335,
            baseDamage = 13,
            speedRating = 1.52f,
            massRating = 1.05f,
            primaryColor = Color(0xFF18181B),
            secondaryColor = Color(0xFFDC2626),
            auraColor = Color(0xFFEF4444),
            hairColor = Color(0xFFF8FAFC),
            weaponType = WeaponVisualType.KI_HALO,
            passiveArchetype = PassiveArchetype.ELASTIC_MOMENTUM,
            ultimateArchetype = UltimateArchetype.HYPER_AWAKEN,
            passiveName = "100 km/h Sprint",
            passiveDescription = "Explodes off arena walls with +20% Turbo Granny acceleration and bank damage.",
            ultimateName = "All-Out Turbo Throttle",
            ultimateDurationSeconds = 5.0f,
            ultimateCooldownSeconds = 8.0f,
            ultimateDamageMultiplier = 1.58f,
            ultimateDescription = "Pushes curse output to maximum for +40% blitz velocity and crushing headbutts.",
            voiceLine = "Going all-out at full throttle, yo...",
            unlockCostGold = 18_000
        ),
        // 16. Ichigo Kurosaki
        CharacterData(
            id = "kurosaki_hollow",
            name = "Ichigo",
            title = "Soul Reaper Vizard",
            animeInspiration = "Bleach: TYBW",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.SPEEDSTER,
            element = ElementTag.SLASH,
            maxHp = 370,
            baseDamage = 15,
            speedRating = 1.44f,
            massRating = 1.32f,
            primaryColor = Color(0xFF18181B),
            secondaryColor = Color(0xFFDC2626),
            auraColor = Color(0xFFEF4444),
            hairColor = Color(0xFFF97316),
            weaponType = WeaponVisualType.ZANGETSU_BLADE,
            passiveArchetype = PassiveArchetype.BLACK_FLASH_CRIT,
            ultimateArchetype = UltimateArchetype.HYPER_AWAKEN,
            passiveName = "Getsuga Surge",
            passiveDescription = "Every 3rd sword clash unleashes a guaranteed 1.45x critical Getsuga slash.",
            ultimateName = "True Tensa Zangetsu",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.66f,
            ultimateDescription = "Unleashes Bankai Shunpo speed (+35%) and 1.66x black-crimson Getsuga strikes.",
            voiceLine = "Bankai—Tensa Zangetsu!",
            unlockCostGold = 22_000
        ),
        // 17. Sosuke Aizen
        CharacterData(
            id = "aizen_kyoka",
            name = "Aizen",
            title = "Transcendent Ruler",
            animeInspiration = "Bleach: TYBW",
            alignment = CharacterAlignment.ANTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.CONTROL_MAGIC,
            element = ElementTag.MAGIC,
            maxHp = 390,
            baseDamage = 15,
            speedRating = 1.34f,
            massRating = 1.48f,
            primaryColor = Color(0xFFF8FAFC),
            secondaryColor = Color(0xFF4C1D95),
            auraColor = Color(0xFFA855F7),
            hairColor = Color(0xFF451A03),
            weaponType = WeaponVisualType.NICHIRIN_KATANA,
            passiveArchetype = PassiveArchetype.INFINITY_BARRIER,
            ultimateArchetype = UltimateArchetype.GRAVITY_CRUSH,
            passiveName = "Kyoka Suigetsu",
            passiveDescription = "Complete Hypnosis misdirects blows, reducing incoming damage by 22%.",
            ultimateName = "Hado #90: Kurohitsugi",
            ultimateDurationSeconds = 5.0f,
            ultimateCooldownSeconds = 9.0f,
            ultimateDamageMultiplier = 1.65f,
            ultimateDescription = "Crushes the rival inside a black gravity coffin (-24 instant HP + slow).",
            voiceLine = "Shatter, Kyoka Suigetsu.",
            unlockCostGold = 38_000,
            hasCapeOrWings = true
        ),
        // 18. Levi / Yeager Titan -> Eren / Titan Shifter
        CharacterData(
            id = "yeager_titan",
            name = "Eren",
            title = "Attack Titan",
            animeInspiration = "Attack on Titan",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.ATTACKER,
            element = ElementTag.HEAVY,
            maxHp = 425,
            baseDamage = 15,
            speedRating = 1.20f,
            massRating = 1.92f,
            primaryColor = Color(0xFF3F3F46),
            secondaryColor = Color(0xFF7F1D1D),
            auraColor = Color(0xFF10B981),
            hairColor = Color(0xFF27272A),
            weaponType = WeaponVisualType.TITAN_BLADES,
            passiveArchetype = PassiveArchetype.TITAN_JUGGERNAUT,
            ultimateArchetype = UltimateArchetype.METEOR_NUKE,
            passiveName = "Hardened Titan Mass",
            passiveDescription = "Colossal Titan mass resists knockback by 35% and crushes lighter spheres (+20% DMG).",
            ultimateName = "Founding Rumbling",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 9.0f,
            ultimateDamageMultiplier = 1.65f,
            ultimateDescription = "Detonates a colossal steam quake (-26 instant HP + fiery crater).",
            voiceLine = "I will keep moving forward!",
            unlockCostGold = 21_000
        ),
        // 19. Rimuru Tempest
        CharacterData(
            id = "rimuru_tempest",
            name = "Rimuru",
            title = "Demon Lord Slime",
            animeInspiration = "Reincarnated as a Slime",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.CONTROL_MAGIC,
            element = ElementTag.MAGIC,
            maxHp = 390,
            baseDamage = 14,
            speedRating = 1.38f,
            massRating = 1.30f,
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFF1E293B),
            auraColor = Color(0xFF22D3EE),
            hairColor = Color(0xFF7DD3FC),
            weaponType = WeaponVisualType.NICHIRIN_KATANA,
            passiveArchetype = PassiveArchetype.REGEN_IMMORTAL,
            ultimateArchetype = UltimateArchetype.SOUL_DEVOUR,
            passiveName = "Ultraspeed Regeneration",
            passiveDescription = "Heals +2% Max HP every 3rd wall bounce and charges Ultimate 15% faster.",
            ultimateName = "Lord of Gluttony: Beelzebuth",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.58f,
            ultimateDescription = "Devours 22% of rival Ultimate meter and siphons HP on activation.",
            voiceLine = "Raphael, analyze and devour!",
            unlockCostGold = 26_000,
            hasCapeOrWings = true
        ),
        // 20. Killua Zoldyck
        CharacterData(
            id = "killua_godspeed",
            name = "Killua",
            title = "Godspeed Assassin",
            animeInspiration = "Hunter x Hunter",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.SPEEDSTER,
            element = ElementTag.ELEC,
            maxHp = 335,
            baseDamage = 13,
            speedRating = 1.54f,
            massRating = 1.02f,
            primaryColor = Color(0xFFE2E8F0),
            secondaryColor = Color(0xFF1E3A8A),
            auraColor = Color(0xFF60A5FA),
            hairColor = Color(0xFFF8FAFC),
            weaponType = WeaponVisualType.ALLOY_YOYOS,
            passiveArchetype = PassiveArchetype.LIGHTNING_PARALYZE,
            ultimateArchetype = UltimateArchetype.HYPER_AWAKEN,
            passiveName = "Narukami Jolt",
            passiveDescription = "Alloy yo-yo voltage micro-stuns rivals on impact and boosts reflex speed.",
            ultimateName = "Kanmuru: Godspeed",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.0f,
            ultimateDamageMultiplier = 1.55f,
            ultimateDescription = "Surges to +40% lightning speed while halving incoming collision damage.",
            voiceLine = "Whirlwind—Godspeed!",
            unlockCostGold = 19_500
        ),
        // 21. Kafka Hibino
        CharacterData(
            id = "kafka_kaiju",
            name = "Kafka",
            title = "Kaiju No. 8",
            animeInspiration = "Kaiju No. 8",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.ATTACKER,
            element = ElementTag.HEAVY,
            maxHp = 415,
            baseDamage = 15,
            speedRating = 1.28f,
            massRating = 1.78f,
            primaryColor = Color(0xFF18181B),
            secondaryColor = Color(0xFF0E7490),
            auraColor = Color(0xFF22D3EE),
            hairColor = Color(0xFF09090B),
            weaponType = WeaponVisualType.SERIOUS_GLOVE,
            passiveArchetype = PassiveArchetype.SHOCKWAVE_KNOCKBACK,
            ultimateArchetype = UltimateArchetype.SERIOUS_ONE_PUNCH,
            passiveName = "Fortitude 9.8",
            passiveDescription = "Kaiju muscle shockwaves blast rivals back with heavy seismic impact damage.",
            ultimateName = "Defense Force Breaker",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.65f,
            ultimateDescription = "Charges core to maximum output, arming a colossal 2.4x Kaiju megaton punch!",
            voiceLine = "Full release—Fortitude 9.8!",
            unlockCostGold = 17_500
        ),
        // 22. Makima
        CharacterData(
            id = "makima_control",
            name = "Makima",
            title = "Control Devil",
            animeInspiration = "Chainsaw Man",
            alignment = CharacterAlignment.ANTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.CONTROL_MAGIC,
            element = ElementTag.MAGIC,
            maxHp = 375,
            baseDamage = 14,
            speedRating = 1.32f,
            massRating = 1.35f,
            primaryColor = Color(0xFF18181B),
            secondaryColor = Color(0xFFF8FAFC),
            auraColor = Color(0xFFDC2626),
            hairColor = Color(0xFFE11D48),
            weaponType = WeaponVisualType.LIMITLESS_ORBS,
            passiveArchetype = PassiveArchetype.VECTOR_REFLECT,
            ultimateArchetype = UltimateArchetype.GRAVITY_CRUSH,
            passiveName = "Prime Minister Contract",
            passiveDescription = "Transfers 26% of all damage suffered directly back onto the attacker.",
            ultimateName = "Ritual: Bang!",
            ultimateDurationSeconds = 5.0f,
            ultimateCooldownSeconds = 9.0f,
            ultimateDamageMultiplier = 1.60f,
            ultimateDescription = "Fires an invisible telekinetic Bang (-26 instant HP + heavy knockback).",
            voiceLine = "Say my name... Bang.",
            unlockCostGold = 36_000,
            hasCapeOrWings = true
        ),
        // 23. Mob / Shigeo Kageyama
        CharacterData(
            id = "kageyama_esper",
            name = "Mob",
            title = "100% Esper",
            animeInspiration = "Mob Psycho 100",
            alignment = CharacterAlignment.PROTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.CONTROL_MAGIC,
            element = ElementTag.MAGIC,
            maxHp = 365,
            baseDamage = 13,
            speedRating = 1.30f,
            massRating = 1.36f,
            primaryColor = Color(0xFF1E1B4B),
            secondaryColor = Color(0xFF312E81),
            auraColor = Color(0xFFA855F7),
            hairColor = Color(0xFF09090B),
            weaponType = WeaponVisualType.PSYCHIC_DEBRIS,
            passiveArchetype = PassiveArchetype.ARCANE_OVERCHARGE,
            ultimateArchetype = UltimateArchetype.GRAVITY_CRUSH,
            passiveName = "Emotional Vessel",
            passiveDescription = "Builds Ultimate meter 35% faster whenever colliding or absorbing psychic stress.",
            ultimateName = "???% Explosion",
            ultimateDurationSeconds = 5.5f,
            ultimateCooldownSeconds = 8.5f,
            ultimateDamageMultiplier = 1.72f,
            ultimateDescription = "Unleashes ???% telekinetic storm, crushing the rival and piercing barriers.",
            voiceLine = "100%... Psychic Explosion!",
            unlockCostGold = 25_000
        ),
        // 24. Blackbeard / Marshall D. Teach
        CharacterData(
            id = "blackbeard_tremor",
            name = "Teach",
            title = "Darkness Emperor",
            animeInspiration = "One Piece",
            alignment = CharacterAlignment.ANTAGONIST,
            popularityTier = AnimeTier.POPULAR,
            classType = CharacterClass.ATTACKER,
            element = ElementTag.HEAVY,
            maxHp = 430,
            baseDamage = 15,
            speedRating = 1.22f,
            massRating = 1.88f,
            primaryColor = Color(0xFF1E1B4B),
            secondaryColor = Color(0xFF7F1D1D),
            auraColor = Color(0xFF9333EA),
            hairColor = Color(0xFF09090B),
            weaponType = WeaponVisualType.LIMITLESS_ORBS,
            passiveArchetype = PassiveArchetype.ANTI_MAGIC_DRAIN,
            ultimateArchetype = UltimateArchetype.METEOR_NUKE,
            passiveName = "Yami Yami Vortex",
            passiveDescription = "Nullifies 11% of rival Ultimate meter on every collision with Yonko mass.",
            ultimateName = "Gura Gura Seaquake",
            ultimateDurationSeconds = 5.0f,
            ultimateCooldownSeconds = 9.0f,
            ultimateDamageMultiplier = 1.68f,
            ultimateDescription = "Shatters the air for -26 instant quake damage and 1.68x tremor collisions.",
            voiceLine = "Zehahaha! Darkness swallows everything!",
            unlockCostGold = 34_000,
            hasCapeOrWings = true
        )
    )

    val characters: List<CharacterData> by lazy {
        coreRoster +
            RosterExpansion.expandedCharacters +
            RosterShonenLegends.characters +
            RosterFantasyIsekai.characters +
            RosterCultAndNiche.characters
    }

    val starterIds: Set<String> by lazy {
        characters.filter { it.isStarter }.map { it.id }.toSet()
    }

    private val byIdMap: Map<String, CharacterData> by lazy {
        characters.associateBy { it.id }
    }

    fun getById(id: String): CharacterData {
        return byIdMap[id] ?: characters.first()
    }
}
