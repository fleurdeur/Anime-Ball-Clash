package com.example.model

/**
 * Characters 25–74 (50 Fighters):
 * Preserves all 24 fan-favorite IDs (Madara, Jotaro, DIO, Saitama, Alucard, Meruem, Guts,
 * Femto, Ryuko, Gabimaru, Ainz, Sukuna, Gilgamesh, Simon, Frieren, Vash, Bondrewd, Phos,
 * Caiman, En, Hyakkimaru, Tetsuo, Accelerator, Sung Jinwoo) with inflated costs & longer-match HP,
 * plus 26 major blockbuster rivals & heroes.
 */
object RosterExpansion {
    val expandedCharacters: List<CharacterData> = listOf(
        // 25. Madara Uchiha
        fighter(
            "madara_uchiha", "Madara", "Ghost of Uchiha", "Naruto Shippuden",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.FIRE,
            405, 16, 1.32f, 1.65f, 0xFF991B1B, 0xFF18181B, 0xFF6366F1, 0xFF09090B,
            WeaponVisualType.TITAN_BLADES,
            PassiveArchetype.INFINITY_BARRIER, "Susanoo Ribcage", "Ethereal chakra armor reduces incoming collision damage by 22%.",
            UltimateArchetype.METEOR_NUKE, "Tengai Shinsei", 5.5f, 9.0f, 1.68f,
            "Summons a twin meteor impact (-26 instant HP + blazing crater).",
            "Wake up to reality—dance before Susanoo!", 45_000, cape = true
        ),
        // 26. Jotaro Kujo
        fighter(
            "kujo_star", "Jotaro", "Stardust Stand", "JoJo's Bizarre Adventure",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            390, 16, 1.30f, 1.68f, 0xFF1E1B4B, 0xFF581C87, 0xFFC084FC, 0xFF09090B,
            WeaponVisualType.STAR_FISTS,
            PassiveArchetype.BLACK_FLASH_CRIT, "Ora Stand Flurry", "Every 3rd Stand collision triggers a 1.45x critical Ora finisher.",
            UltimateArchetype.TIME_STOP, "Star Platinum: The World", 4.5f, 9.0f, 1.62f,
            "Stops time for the rival sphere for 1.65 seconds—ORA ORA ORA!",
            "Star Platinum—Time has stopped.", 38_000, cape = true
        ),
        // 27. DIO
        fighter(
            "dio_world", "DIO", "Immortal Vampire", "JoJo's Bizarre Adventure",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            395, 15, 1.36f, 1.60f, 0xFFEAB308, 0xFF15803D, 0xFFFACC15, 0xFFFDE047,
            WeaponVisualType.STAR_FISTS,
            PassiveArchetype.VAMPIRIC_SIPHON, "Vampiric Ambition", "Drains 28% of collision damage dealt back as immortal HP.",
            UltimateArchetype.TIME_STOP, "The World: Time Stop", 4.8f, 9.0f, 1.66f,
            "Freezes the rival sphere in stopped time for 1.65s—MUDA MUDA!",
            "ZA WARUDO! Time belongs to me alone!", 46_000, cape = true
        ),
        // 28. Saitama
        fighter(
            "caped_baldy", "Saitama", "Hero For Fun", "One Punch Man",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            435, 16, 1.34f, 1.85f, 0xFFFACC15, 0xFFDC2626, 0xFFFEF08A, 0x00000000,
            WeaponVisualType.SERIOUS_GLOVE,
            PassiveArchetype.SHOCKWAVE_KNOCKBACK, "Casual Blow", "Effortless air-pressure punches blast rivals with +38% knockback.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Serious Series: Punch", 5.5f, 9.5f, 1.75f,
            "Arms a devastating 2.5x megaton Serious Punch on next collision!",
            "Okay. Consecutive Normal Punches.", 95_000, cape = true
        ),
        // 29. Alucard
        fighter(
            "alucard_hellsing", "Alucard", "No-Life King", "Hellsing Ultimate",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            400, 15, 1.32f, 1.46f, 0xFF991B1B, 0xFF09090B, 0xFFEF4444, 0xFF09090B,
            WeaponVisualType.HELLSING_PISTOLS,
            PassiveArchetype.VAMPIRIC_SIPHON, "Blood Familiar Feast", "Casull & Jackal impacts siphon 28% of damage dealt as vitality.",
            UltimateArchetype.SOUL_DEVOUR, "Restriction Level Zero", 5.5f, 9.0f, 1.65f,
            "Unleashes Baskerville soul flood, stealing rival HP and Ultimate charge.",
            "Release Control Art Restriction System—Level Zero.", 52_000, cape = true
        ),
        // 30. Meruem
        fighter(
            "meruem_chimera", "Meruem", "Chimera Ant King", "Hunter x Hunter",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.HEAVY,
            420, 16, 1.40f, 1.65f, 0xFF15803D, 0xFF4C1D95, 0xFFA855F7, 0x00000000,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.ADAPTIVE_EVOLUTION, "Aura Synthesis", "Permanently gains +3 attack power & +3% armor per hit (up to 8 stacks).",
            UltimateArchetype.HYPER_AWAKEN, "Post-Rose Metamorphosis", 5.5f, 9.0f, 1.70f,
            "Spreads radiant En wings for +34% speed and overwhelming royal force.",
            "I was born to rule at the pinnacle of evolution.", 74_000, cape = true
        ),
        // 31. Guts
        fighter(
            "guts_berserk", "Guts", "Black Swordsman", "Berserk",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.SLASH,
            425, 16, 1.26f, 1.82f, 0xFF18181B, 0xFF7F1D1D, 0xFFDC2626, 0xFF09090B,
            WeaponVisualType.GRIMOIRE_GREATSWORD,
            PassiveArchetype.BERSERKER_RAGE, "Struggler's Rage", "Hits up to 45% harder with his heavy iron sword as his HP gets low.",
            UltimateArchetype.HYPER_AWAKEN, "Berserker Armor", 6.0f, 9.0f, 1.72f,
            "Dons the Berserker wolf helm and grows his Dragon Slayer sword huge (+32% speed, wider reach, 1.72x damage).",
            "If you meet God in my way, tell Him to leave me alone!", 68_000, cape = true
        ),
        // 32. Griffith / Femto
        fighter(
            "griffith_femto", "Femto", "Wings of Darkness", "Berserk",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            395, 15, 1.40f, 1.42f, 0xFF312E81, 0xFF09090B, 0xFF818CF8, 0xFFE2E8F0,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.INFINITY_BARRIER, "Causality Repulsion", "Warps trajectories to reduce incoming collision damage by 22%.",
            UltimateArchetype.GRAVITY_CRUSH, "Eclipse Singularity", 5.0f, 9.0f, 1.66f,
            "Compresses space around the rival (-25 instant HP + stun crush).",
            "My dream will never be tarnished.", 78_000, cape = true
        ),
        // 33. Ryuko Matoi
        fighter(
            "ryuko_scissor", "Ryuko", "Life Fiber Rebel", "Kill la Kill",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            355, 14, 1.48f, 1.18f, 0xFF1E293B, 0xFFDC2626, 0xFFEF4444, 0xFF09090B,
            WeaponVisualType.CRESCENT_SCYTHE,
            PassiveArchetype.CRIMSON_BLEED, "Scissor Decapitation", "Rending scissor arcs inflict 4 Bleed ticks and boost sprint speed.",
            UltimateArchetype.HYPER_AWAKEN, "Senketsu Kisaragi", 5.5f, 8.0f, 1.64f,
            "Ignites golden Life Fiber form with +36% flight velocity and 1.64x slash.",
            "Don't lose your way—Senketsu!", 32_000
        ),
        // 34. Gabimaru
        fighter(
            "gabimaru_hollow", "Gabimaru", "The Hollow Shinobi", "Hell's Paradise",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.FIRE,
            350, 14, 1.46f, 1.14f, 0xFF1E293B, 0xFFEA580C, 0xFFF97316, 0xFFF8FAFC,
            WeaponVisualType.LIGHTNING_KUNAI,
            PassiveArchetype.SCORCH_BURN, "Ninpo: Ascetic Blaze", "Ignites target on contact and leaves burning Tao embers.",
            UltimateArchetype.HAZARD_STORM, "Flower Tao Inferno", 5.5f, 8.0f, 1.60f,
            "Blankets wall rebounds in blazing crimson Tao flame zones.",
            "I refuse to die before returning home!", 29_000
        ),
        // 35. Ainz Ooal Gown
        fighter(
            "ainz_overlord", "Ainz", "Sorcerer King", "Overlord",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            415, 15, 1.24f, 1.64f, 0xFF1E1B4B, 0xFFFACC15, 0xFFA855F7, 0x00000000,
            WeaponVisualType.MAGE_STAFF,
            PassiveArchetype.FROST_GRAVITY_SLOW, "Despair Aura", "Chills rival speed by 20% for 1.8s and delays rival redirect.",
            UltimateArchetype.METEOR_NUKE, "Fallen Down", 5.5f, 9.5f, 1.70f,
            "Casts Super-Tier magic for -28 instant arcane burst damage!",
            "Applaud my supreme power—Fallen Down!", 64_000, cape = true
        ),
        // 36. Ryomen Sukuna
        fighter(
            "sukuna_shrine", "Sukuna", "King of Curses", "Jujutsu Kaisen",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.SLASH,
            410, 16, 1.38f, 1.56f, 0xFFE11D48, 0xFF18181B, 0xFFFB7185, 0xFFFDA4AF,
            WeaponVisualType.CURSED_CLEAVE,
            PassiveArchetype.EXECUTE_SHRINE, "Dismantle & Cleave", "Inflicts +30% lethal slash damage whenever rival HP is below 50%.",
            UltimateArchetype.HAZARD_STORM, "Malevolent Shrine", 5.5f, 9.0f, 1.72f,
            "Expands Malevolent Shrine, shredding the arena with relentless slash zones.",
            "Stand proud—Domain Expansion: Malevolent Shrine!", 82_000
        ),
        // 37. Gilgamesh
        fighter(
            "gilgamesh_babylon", "Gilgamesh", "King of Heroes", "Fate/Zero",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.SLASH,
            395, 16, 1.32f, 1.54f, 0xFFEAB308, 0xFF991B1B, 0xFFFACC15, 0xFFFDE047,
            WeaponVisualType.ROYAL_GATES,
            PassiveArchetype.ARCANE_OVERCHARGE, "Golden Rule", "Gate of Babylon charges Ultimate 35% faster with extended weapon reach.",
            UltimateArchetype.METEOR_NUKE, "Enuma Elish", 5.0f, 9.0f, 1.74f,
            "Spins Sword of Rupture Ea for -28 instant genesis rift damage!",
            "Mongrel! Witness Enuma Elish!", 76_000, cape = true
        ),
        // 38. Simon the Digger
        fighter(
            "simon_spiral", "Simon", "Star Piercer", "Gurren Lagann",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.HEAVY,
            400, 15, 1.38f, 1.60f, 0xFF1D4ED8, 0xFFDC2626, 0xFF34D399, 0xFF1E3A8A,
            WeaponVisualType.SPIRAL_DRILL,
            PassiveArchetype.ADAPTIVE_EVOLUTION, "Spiral Willpower", "Evolves +3 attack & +3% armor on every collision as fighting spirit rises.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Giga Drill Break", 5.5f, 8.5f, 1.75f,
            "Forms a cosmic emerald drill that pierces all shields for 2.4x impact!",
            "My drill is the drill that creates the heavens!", 72_000, cape = true
        ),
        // 39. Frieren
        fighter(
            "frieren_zoltraak", "Frieren", "Last Great Mage", "Frieren: Beyond Journey's End",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            365, 14, 1.34f, 1.24f, 0xFFF8FAFC, 0xFFCA8A04, 0xFF38BDF8, 0xFFE2E8F0,
            WeaponVisualType.MAGE_STAFF,
            PassiveArchetype.ARCANE_OVERCHARGE, "Millennium Mana Suppression", "Accumulates Ultimate meter 35% faster and pierces barriers when Awakened.",
            UltimateArchetype.DOMAIN_STUN, "Height of Magic: Zoltraak", 5.5f, 8.0f, 1.66f,
            "Binds the rival in ancient mana and fires defense-piercing Zoltraak beams.",
            "Zoltraak—ordinary offensive magic is enough.", 48_000, cape = true
        ),
        // 40. Vash the Stampede
        fighter(
            "vash_stampede", "Vash", "Humanoid Typhoon", "Trigun",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.SPEEDSTER, ElementTag.ELEC,
            355, 14, 1.48f, 1.15f, 0xFFDC2626, 0xFF18181B, 0xFFFACC15, 0xFFFDE047,
            WeaponVisualType.HELLSING_PISTOLS,
            PassiveArchetype.ELASTIC_MOMENTUM, "60 Billion $$ Ricochet", "Trick-shot geometry grants +50% bank-shot bonus damage off walls.",
            UltimateArchetype.HYPER_AWAKEN, "Angel Arm Cannon", 5.5f, 8.5f, 1.66f,
            "Unfolds radiant Angel Arm wing for +34% speed and high-caliber blasts.",
            "Love and Peace! This world is worth saving!", 58_000, cape = true
        ),
        // 41. Bondrewd
        fighter(
            "bondrewd_dawn", "Bondrewd", "Sovereign of Dawn", "Made in Abyss",
            CharacterAlignment.ANTAGONIST, AnimeTier.NICHE, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            405, 14, 1.30f, 1.52f, 0xFF27272A, 0xFF581C87, 0xFFD946EF, 0x00000000,
            WeaponVisualType.CYBER_CANNON,
            PassiveArchetype.INFINITY_BARRIER, "Blessing Cartridges", "Deflects Abyssal trauma to reduce incoming damage by 22%.",
            UltimateArchetype.GRAVITY_CRUSH, "Sparagmos: Gangway", 5.5f, 9.0f, 1.68f,
            "Fires bent-light Sparagmos beams (-25 instant HP, pierces defense).",
            "Subarashii... marvelous scientific triumph!", 88_000, cape = true
        ),
        // 42. Phosphophyllite
        fighter(
            "phosphophyllite_gem", "Phos", "Alloy Lustrous", "Land of the Lustrous",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.CONTROL_MAGIC, ElementTag.SLASH,
            350, 14, 1.42f, 1.28f, 0xFF14B8A6, 0xFFEAB308, 0xFF2DD4BF, 0xFF5EEAD4,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.VECTOR_REFLECT, "Gold-Platinum Fracture", "Crystalline facets reflect 26% of collision force back at attackers.",
            UltimateArchetype.HEAL_REFORGE, "Kintsugi Reforge", 5.5f, 8.0f, 1.58f,
            "Rebuilds shattered gem body (+52 HP heal) and extends golden alloy ribbons.",
            "Even if I shatter, gold binds me anew.", 62_000
        ),
        // 43. Caiman
        fighter(
            "caiman_dorohedoro", "Caiman", "Magic-Immune Reptile", "Dorohedoro",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.ATTACKER, ElementTag.SLASH,
            420, 15, 1.26f, 1.72f, 0xFF4D7C0F, 0xFF18181B, 0xFF84CC16, 0x00000000,
            WeaponVisualType.SHADOW_DAGGERS,
            PassiveArchetype.ANTI_MAGIC_DRAIN, "Sorcerer Bite", "Anti-magic jaws chew 11% off rival Ultimate meter on every collision.",
            UltimateArchetype.HEAL_REFORGE, "Gyoza Madness", 5.5f, 8.5f, 1.60f,
            "Devours steaming gyoza for +55 HP recovery and frenzied bayonet slashes.",
            "What did the guy inside my mouth say?!", 54_000
        ),
        // 44. En
        fighter(
            "en_dorohedoro", "En", "Mushroom Crime Boss", "Dorohedoro",
            CharacterAlignment.ANTAGONIST, AnimeTier.NICHE, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            385, 14, 1.28f, 1.44f, 0xFF991B1B, 0xFFF8FAFC, 0xFFF43F5E, 0xFFBE123C,
            WeaponVisualType.PSYCHIC_DEBRIS,
            PassiveArchetype.FROST_GRAVITY_SLOW, "Mycelium Spores", "Fungal smoke slows rival movement by 22% for 1.8s on contact.",
            UltimateArchetype.HAZARD_STORM, "Smoke World: En-Mycelium", 5.5f, 8.5f, 1.58f,
            "Carpets the arena in toxic mushroom spore zones on bounce and activation.",
            "Everything becomes mushrooms in my world.", 56_000
        ),
        // 45. Hyakkimaru
        fighter(
            "hyakkimaru_dororo", "Hyakkimaru", "Demon-Limb Ronin", "Dororo",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            360, 15, 1.46f, 1.18f, 0xFF27272A, 0xFF991B1B, 0xFFEF4444, 0xFF09090B,
            WeaponVisualType.TITAN_BLADES,
            PassiveArchetype.CRIMSON_BLEED, "Prosthetic Twin Blades", "Hidden arm-swords carve 4 Bleed ticks and +12% crit damage.",
            UltimateArchetype.SOUL_DEVOUR, "Reclaim Humanity", 5.5f, 8.5f, 1.64f,
            "Slays demon essence to drain rival HP and surge at +34% blade speed.",
            "Give me back what belongs to me.", 59_000
        ),
        // 46. Tetsuo Shima
        fighter(
            "tetsuo_psychic", "Tetsuo", "Neo-Tokyo Esper #41", "Akira",
            CharacterAlignment.ANTAGONIST, AnimeTier.NICHE, CharacterClass.CONTROL_MAGIC, ElementTag.HEAVY,
            400, 16, 1.34f, 1.66f, 0xFFB91C1C, 0xFF18181B, 0xFFEF4444, 0xFF09090B,
            WeaponVisualType.PSYCHIC_DEBRIS,
            PassiveArchetype.SHOCKWAVE_KNOCKBACK, "Telekinetic Repulsion", "Psychic shockwaves hurl rivals across the court (+38% impulse).",
            UltimateArchetype.METEOR_NUKE, "Sol Orbital Singularity", 5.0f, 9.0f, 1.72f,
            "Detonates a Neo-Tokyo psychic dome (-27 instant HP + crater).",
            "KANEDAAA! Look at my power!", 84_000, cape = true
        ),
        // 47. Accelerator
        fighter(
            "accelerator_vector", "Accelerator", "Level 5 #1 Esper", "A Certain Scientific Accelerator",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            375, 15, 1.40f, 1.35f, 0xFF18181B, 0xFFF8FAFC, 0xFFEF4444, 0xFFF8FAFC,
            WeaponVisualType.LIMITLESS_ORBS,
            PassiveArchetype.VECTOR_REFLECT, "Vector Reflection", "Reverses incoming vectors, reflecting 26% of collision damage back.",
            UltimateArchetype.HYPER_AWAKEN, "Platinum Vector Wings", 5.5f, 9.0f, 1.70f,
            "Manifests dark/platinum vector wings for +36% speed and 1.70x plasma force.",
            "Every vector in this world obeys me!", 92_000, cape = true
        ),
        // 48. Sung Jinwoo
        fighter(
            "jinwoo_monarch", "Jinwoo", "Shadow Monarch", "Solo Leveling",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            385, 16, 1.46f, 1.34f, 0xFF0F172A, 0xFF581C87, 0xFFA855F7, 0xFF09090B,
            WeaponVisualType.SHADOW_DAGGERS,
            PassiveArchetype.SHADOW_MONARCH_STACK, "Shadow Extraction", "Each collision permanently stacks +3% speed and +2 attack (max 8).",
            UltimateArchetype.SOUL_DEVOUR, "Arise: Shadow Army", 5.5f, 8.5f, 1.70f,
            "Commands the Shadow Legion to drain rival vitality and strike at 1.70x power.",
            "Arise.", 98_000, cape = true
        ),
        // 49. Vegeta
        fighter(
            "vegeta_ego", "Vegeta", "Prince of Saiyans", "Dragon Ball Super",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            385, 16, 1.32f, 1.52f, 0xFF1E3A8A, 0xFFF8FAFC, 0xFFA855F7, 0xFF09090B,
            WeaponVisualType.KI_HALO,
            PassiveArchetype.BERSERKER_RAGE, "Ultra Ego Pride", "Battle damage fuels Saiyan pride for up to +45% stronger Final Impacts.",
            UltimateArchetype.METEOR_NUKE, "Final Flash", 5.5f, 8.5f, 1.70f,
            "Unleashes a golden-violet Final Flash (-26 instant HP + 1.70x clash).",
            "I am the Prince of all Saiyans!", 44_000, CrownVisualStyle.SAIYAN_WILD.let { EyeVisualStyle.FIERCE_SHONEN }, CrownVisualStyle.SAIYAN_WILD
        ),
        // 50. Broly
        fighter(
            "broly_legendary", "Broly", "Legendary Super Saiyan", "Dragon Ball Super",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            445, 17, 1.24f, 1.98f, 0xFF15803D, 0xFF581C87, 0xFF4ADE80, 0xFF22C55E,
            WeaponVisualType.KI_HALO,
            PassiveArchetype.TITAN_JUGGERNAUT, "Unstoppable Berserker", "Colossal mass shrugs off knockback and crushes lighter targets (+20% DMG).",
            UltimateArchetype.METEOR_NUKE, "Omega Blaster", 6.0f, 9.5f, 1.75f,
            "Detonates emerald planet-crusher energy (-28 instant HP + crater).",
            "KAKAROT!!", 86_000, EyeVisualStyle.ESPER_PSYCHIC, CrownVisualStyle.SAIYAN_WILD
        ),
        // 51. Gohan Beast
        fighter(
            "gohan_beast", "Gohan", "Awakened Beast", "Dragon Ball Super",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.ELEC,
            380, 16, 1.38f, 1.48f, 0xFF4C1D95, 0xFFDC2626, 0xFFE11D48, 0xFFE2E8F0,
            WeaponVisualType.SPIRAL_DRILL,
            PassiveArchetype.BLACK_FLASH_CRIT, "Mystic Potential", "Every 3rd collision unleashes a piercing 1.45x Special Beam critical.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Special Beam Cannon", 5.5f, 8.5f, 1.72f,
            "Charges Beast aura to arm a shield-piercing 2.4x drill beam!",
            "It's my turn to protect everyone!", 66_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.SAIYAN_WILD, cape = true
        ),
        // 52. Goku Black
        fighter(
            "goku_black_rose", "Goku Black", "Super Saiyan Rosé", "Dragon Ball Super",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            370, 15, 1.44f, 1.34f, 0xFF18181B, 0xFFBE185D, 0xFFF472B6, 0xFFF472B6,
            WeaponVisualType.CRESCENT_SCYTHE,
            PassiveArchetype.EXECUTE_SHRINE, "Scythe of Sorrow", "Divine ki scythe deals +30% execution damage below 50% rival HP.",
            UltimateArchetype.HAZARD_STORM, "Divine Lasso", 5.5f, 8.5f, 1.66f,
            "Slices dimensional rifts that leave Rosé ki explosion zones.",
            "Gaze upon the beauty of Super Saiyan Rosé!", 55_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SAIYAN_WILD
        ),
        // 53. Cell (Perfect)
        fighter(
            "perfect_cell", "Cell", "Ultimate Bio-Android", "Dragon Ball Z",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.HEAVY,
            410, 15, 1.34f, 1.58f, 0xFF16A34A, 0xFF1E1B4B, 0xFFFACC15, 0xFF14532D,
            WeaponVisualType.KI_HALO,
            PassiveArchetype.REGEN_IMMORTAL, "Namekian Nucleus", "Regenerates +2% Max HP every 3rd wall bounce with bio-armor.",
            UltimateArchetype.METEOR_NUKE, "Solar Kamehameha", 5.5f, 9.0f, 1.68f,
            "Blasts a system-wiping Solar Kamehameha (-26 instant HP).",
            "Behold True Perfection!", 42_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.DEMON_HORNS, cape = true
        ),
        // 54. Kid Buu
        fighter(
            "majin_kid_buu", "Majin Buu", "Pure Chaos Majin", "Dragon Ball Z",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.MAGIC,
            395, 15, 1.46f, 1.25f, 0xFFF472B6, 0xFFF8FAFC, 0xFFEC4899, 0xFFF9A8D4,
            WeaponVisualType.GOMU_FIST,
            PassiveArchetype.ELASTIC_MOMENTUM, "Elastic Majin Body", "Bounces wildly off walls with +20% speed and +50% bank damage.",
            UltimateArchetype.METEOR_NUKE, "Planet Burst", 5.5f, 9.0f, 1.68f,
            "Hurls a condensed pink Planet Burst sphere (-27 instant HP).",
            "Gyahahaha!!", 49_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.TWIN_TAILS
        ),
        // 55. Naruto Uzumaki (Baryon)
        fighter(
            "naruto_baryon", "Naruto", "Seventh Hokage", "Naruto / Boruto",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.FIRE,
            390, 16, 1.46f, 1.42f, 0xFFF97316, 0xFF18181B, 0xFFEF4444, 0xFFFACC15,
            WeaponVisualType.LIMITLESS_ORBS,
            PassiveArchetype.SCORCH_BURN, "Baryon Nuclear Fusion", "Shaves rival lifespan with nuclear chakra burn on every collision.",
            UltimateArchetype.HYPER_AWAKEN, "Baryon Mode", 6.0f, 8.5f, 1.72f,
            "Ignites scarlet Nine-Tails Baryon cloak (+35% speed, 1.72x Rasengan).",
            "I never go back on my word—that's my Ninja Way!", 75_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.SHINOBI_HEADBAND, FaceMarkVisualStyle.WHISKER_MARKS, cape = true
        ),
        // 56. Sasuke Uchiha
        fighter(
            "sasuke_rinnegan", "Sasuke", "Shadow Hokage", "Naruto Shippuden",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.ELEC,
            365, 16, 1.48f, 1.28f, 0xFF1E1B4B, 0xFF312E81, 0xFF818CF8, 0xFF09090B,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.LIGHTNING_PARALYZE, "Amenotejikara", "Rinnegan lightning swaps stun rivals for 0.26s on contact.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Indra's Arrow", 5.5f, 8.5f, 1.70f,
            "Draws Susanoo lightning bow for a 2.4x piercing Indra's Arrow!",
            "I will revolutionize the shinobi world from the shadows.", 68_000, EyeVisualStyle.RINNEGAN_RIPPLE, CrownVisualStyle.SLICK_VILLAIN, cape = true
        ),
        // 57. Itachi Uchiha
        fighter(
            "itachi_tsukuyomi", "Itachi", "Crimson Crow", "Naruto Shippuden",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.FIRE,
            355, 15, 1.42f, 1.24f, 0xFF18181B, 0xFFDC2626, 0xFFEF4444, 0xFF09090B,
            WeaponVisualType.LIGHTNING_KUNAI,
            PassiveArchetype.SCORCH_BURN, "Amaterasu Black Flames", "Inextinguishable black flames burn rivals and coat the arena.",
            UltimateArchetype.TIME_STOP, "Tsukuyomi Genjutsu", 5.0f, 8.5f, 1.62f,
            "Traps the rival in 72 hours of Tsukuyomi paralysis (1.65s stun)!",
            "You are already under my Genjutsu.", 62_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.SHINOBI_HEADBAND, cape = true
        ),
        // 58. Pain (Nagato)
        fighter(
            "pain_deva", "Pain", "Six Paths Deva", "Naruto Shippuden",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            385, 15, 1.30f, 1.56f, 0xFF18181B, 0xFFEA580C, 0xFFA855F7, 0xFFF97316,
            WeaponVisualType.PSYCHIC_DEBRIS,
            PassiveArchetype.SHOCKWAVE_KNOCKBACK, "Almighty Push", "Shinra Tensei repulsion blasts rivals violently off course.",
            UltimateArchetype.GRAVITY_CRUSH, "Planetary Devastation", 5.5f, 9.0f, 1.68f,
            "Crushes rival inside Chibaku Tensei gravity core (-26 HP + slow).",
            "This world shall know Pain.", 54_000, EyeVisualStyle.RINNEGAN_RIPPLE, CrownVisualStyle.SHONEN_SPIKY, FaceMarkVisualStyle.CYBER_SEAMS, cape = true
        ),
        // 59. Minato Namikaze
        fighter(
            "minato_flash", "Minato", "Yellow Flash", "Naruto Shippuden",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.ELEC,
            345, 14, 1.58f, 1.06f, 0xFFFACC15, 0xFFF8FAFC, 0xFF38BDF8, 0xFFFDE047,
            WeaponVisualType.LIGHTNING_KUNAI,
            PassiveArchetype.ELASTIC_MOMENTUM, "Flying Raijin Step", "Teleports off arena walls with +20% flash velocity and bank crits.",
            UltimateArchetype.HYPER_AWAKEN, "Kurama KCM Flash", 5.5f, 8.0f, 1.62f,
            "Ignites golden Nine-Tails cloak for maximum arena blitz speed.",
            "Blink and the battle is already over.", 52_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHONEN_SPIKY, cape = true
        ),
        // 60. Obito Uchiha
        fighter(
            "obito_ten_tails", "Obito", "Ten-Tails Jinchuriki", "Naruto Shippuden",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            405, 15, 1.36f, 1.55f, 0xFFE2E8F0, 0xFF4C1D95, 0xFF818CF8, 0xFFF8FAFC,
            WeaponVisualType.LIMITLESS_ORBS,
            PassiveArchetype.INFINITY_BARRIER, "Kamui Intangibility", "Phases through 22% of incoming collision damage effortlessly.",
            UltimateArchetype.METEOR_NUKE, "Sword of Nunoboko", 5.5f, 9.0f, 1.66f,
            "Cleaves the court with Truth-Seeking Orbs (-26 instant HP).",
            "In a world of despair, I will weave Infinite Tsukuyomi.", 56_000, EyeVisualStyle.RINNEGAN_RIPPLE, CrownVisualStyle.DEMON_HORNS
        ),
        // 61. Shanks
        fighter(
            "shanks_redhair", "Shanks", "Red-Haired Emperor", "One Piece",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.SLASH,
            395, 17, 1.40f, 1.54f, 0xFF991B1B, 0xFF18181B, 0xFFEF4444, 0xFFDC2626,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.ANTI_MAGIC_DRAIN, "Observation Killer Haki", "Supreme Conqueror Haki crushes 11% of rival Ultimate gauge per hit.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Divine Departure", 5.5f, 8.5f, 1.75f,
            "Unleashes Gryphon's Kamusari slash for 2.45x Conqueror damage!",
            "Listen up—putting a pistol to someone means risking your life.", 89_000, EyeVisualStyle.SCARRED_ONE_EYE, CrownVisualStyle.FLOWING_MANE, FaceMarkVisualStyle.BLOOD_STRIPE, cape = true
        ),
        // 62. Kaido
        fighter(
            "kaido_dragon", "Kaido", "Strongest Creature", "One Piece",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.FIRE,
            450, 17, 1.24f, 2.02f, 0xFF0284C7, 0xFF1E1B4B, 0xFFF97316, 0xFF09090B,
            WeaponVisualType.DEMON_KANABO,
            PassiveArchetype.TITAN_JUGGERNAUT, "Azure Dragon Scales", "Mythical dragon scales resist knockback 35% and crush foes (+20% DMG).",
            UltimateArchetype.METEOR_NUKE, "Flame Bagua Destroyer", 6.0f, 9.5f, 1.76f,
            "Coats Hassaikai in magma dragon fire (-28 instant HP + burn crater).",
            "Worororo! Only Haki can transcend all!", 92_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.DEMON_HORNS, cape = true
        ),
        // 63. Trafalgar Law
        fighter(
            "trafalgar_room", "Law", "Surgeon of Death", "One Piece",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            360, 15, 1.40f, 1.26f, 0xFF18181B, 0xFFFACC15, 0xFF38BDF8, 0xFF09090B,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.ARCANE_OVERCHARGE, "Ope Ope Tact", "Surgical precision builds Ultimate 35% faster and bypasses armor when Awakened.",
            UltimateArchetype.DOMAIN_STUN, "K-ROOM: Puncture Wille", 5.0f, 8.5f, 1.68f,
            "Opens anesthesia ROOM, paralyzing rival 1.55s and piercing internally.",
            "ROOM—Shambles!", 41_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.WITCH_HOOD, FaceMarkVisualStyle.CURSE_TATTOOS
        ),
        // 64. Sanji
        fighter(
            "sanji_ifrit", "Sanji", "Black Leg Vinsmoke", "One Piece",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.FIRE,
            370, 14, 1.52f, 1.22f, 0xFF0F172A, 0xFF2563EB, 0xFF38BDF8, 0xFFFACC15,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.SCORCH_BURN, "Diable Jambe", "White-hot sky-walk kicks ignite rivals and heal Germa exoskeleton.",
            UltimateArchetype.HYPER_AWAKEN, "Ifrit Jambe: Boeuf Burst", 5.5f, 8.0f, 1.65f,
            "Blue lightning-flame legs boost velocity +36% and 1.65x combo kicks.",
            "Ifrit Jambe—let's turn up the heat!", 39_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SLICK_VILLAIN
        ),
        // 65. Doflamingo
        fighter(
            "doflamingo_ito", "Doflamingo", "Heavenly Yaksha", "One Piece",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.SLASH,
            380, 15, 1.38f, 1.36f, 0xFFEC4899, 0xFFF8FAFC, 0xFFF43F5E, 0xFFFDE047,
            WeaponVisualType.CHAKRAM_RINGS,
            PassiveArchetype.CRIMSON_BLEED, "Overheat Razor Strings", "Razor-wire threads slice rivals for 4 Bleed ticks on contact.",
            UltimateArchetype.DOMAIN_STUN, "Awakened Birdcage", 5.5f, 8.5f, 1.64f,
            "Binds rival sphere in parasite strings (1.5s stun) and shreds HP.",
            "Fufufu! Justice will prevail? Of course it will—whoever wins IS justice!", 43_000, EyeVisualStyle.STAR_SHADES, CrownVisualStyle.SHONEN_SPIKY, cape = true
        ),
        // 66. Yuji Itadori
        fighter(
            "yuji_blackflash", "Yuji", "Black Flash Vessel", "Jujutsu Kaisen",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            385, 15, 1.40f, 1.46f, 0xFF1E293B, 0xFFDC2626, 0xFFEF4444, 0xFFF472B6,
            WeaponVisualType.SERIOUS_GLOVE,
            PassiveArchetype.BLACK_FLASH_CRIT, "Sparks of Black", "Every 3rd martial impact lands a guaranteed 1.45x Black Flash critical!",
            UltimateArchetype.HYPER_AWAKEN, "Benevolent Soul Shrine", 5.5f, 8.0f, 1.68f,
            "Enters the zone of consecutive Black Flashes (+34% speed, 1.68x soul blows).",
            "The sparks of black do not choose who to bless—Black Flash!", 36_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHONEN_SPIKY, FaceMarkVisualStyle.FOREHEAD_SCAR
        ),
        // 67. Yuta Okkotsu
        fighter(
            "yuta_rika", "Yuta", "Queen of Curses Prodigy", "Jujutsu Kaisen",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            385, 15, 1.38f, 1.40f, 0xFFF8FAFC, 0xFF1E1B4B, 0xFFA855F7, 0xFF09090B,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.ARCANE_OVERCHARGE, "Boundless Cursed Energy", "Rika's reservoir charges Ultimate 35% faster and empowers katana arcs.",
            UltimateArchetype.METEOR_NUKE, "Authentic Mutual Love: Pure Love Beam", 5.5f, 8.5f, 1.70f,
            "Fully manifests Rika to blast a Pure Love Beam (-27 instant HP).",
            "Come, Rika—give me everything!", 64_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SLICK_VILLAIN
        ),
        // 68. Toji Fushiguro
        fighter(
            "toji_heavenly", "Toji", "Sorcerer Killer", "Jujutsu Kaisen",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            375, 16, 1.54f, 1.34f, 0xFF18181B, 0xFF4C1D95, 0xFF10B981, 0xFF09090B,
            WeaponVisualType.SHADOW_DAGGERS,
            PassiveArchetype.ANTI_MAGIC_DRAIN, "Inverted Spear of Heaven", "Zero cursed energy nullifies barriers and drains 11% rival Ultimate.",
            UltimateArchetype.ULT_NULLIFY, "Heavenly Restriction Blitz", 5.5f, 8.0f, 1.68f,
            "Cancels active rival Ultimate immediately and shreds with Split Soul Katana!",
            "No cursed energy—just pure physical lethality.", 71_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SLICK_VILLAIN, FaceMarkVisualStyle.BLOOD_STRIPE
        ),
        // 69. Megumi Fushiguro / Mahoraga
        fighter(
            "mahoraga_divine", "Mahoraga", "Eight-Handled General", "Jujutsu Kaisen",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            440, 16, 1.28f, 1.90f, 0xFFF8FAFC, 0xFFCA8A04, 0xFFFACC15, 0xFFE2E8F0,
            WeaponVisualType.CHAKRAM_RINGS,
            PassiveArchetype.ADAPTIVE_EVOLUTION, "Dharma Wheel Adaptation", "Wheel turns on every clash: +3 attack & +3% damage reduction (up to 8x)!",
            UltimateArchetype.HEAL_REFORGE, "World-Cutting Adaptation", 6.0f, 9.5f, 1.70f,
            "Completes adaptation to heal +55 HP and cleave through any defense.",
            "With this sacred treasure, I summon...", 94_000, EyeVisualStyle.HYPNOTIC_RINGS, CrownVisualStyle.HALO_CROWN, cape = true
        ),
        // 70. Kenpachi Zaraki
        fighter(
            "kenpachi_nozarashi", "Kenpachi", "11th Division Demon", "Bleach: TYBW",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.SLASH,
            435, 17, 1.28f, 1.85f, 0xFFDC2626, 0xFFF8FAFC, 0xFFFACC15, 0xFF09090B,
            WeaponVisualType.GRIMOIRE_GREATSWORD,
            PassiveArchetype.BERSERKER_RAGE, "Eyepatch Unsealed", "Relishes bloodshed—gaining up to +45% slash power as HP drops.",
            UltimateArchetype.HYPER_AWAKEN, "Crimson Berserk Bankai", 5.5f, 9.0f, 1.78f,
            "Transforms into crimson oni form for 1.78x cleaving carnage!",
            "There is nothing my Nozarashi cannot cut!", 69_000, EyeVisualStyle.SCARRED_ONE_EYE, CrownVisualStyle.SAIYAN_WILD, FaceMarkVisualStyle.CROSS_SCAR, cape = true
        ),
        // 71. Byakuya Kuchiki
        fighter(
            "byakuya_senbonzakura", "Byakuya", "Noble Petal Captain", "Bleach: TYBW",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.SLASH,
            370, 15, 1.38f, 1.32f, 0xFFF8FAFC, 0xFF18181B, 0xFFF472B6, 0xFF09090B,
            WeaponVisualType.ROYAL_GATES,
            PassiveArchetype.CRIMSON_BLEED, "Senbonzakura Million Blades", "Orbiting cherry-blossom blades inflict 4 Bleed ticks on every graze.",
            UltimateArchetype.HAZARD_STORM, "Senkei: Ikka Senjinka", 5.5f, 8.5f, 1.65f,
            "Swarms the court in lethal pink blade vortexes on bounce and clash.",
            "Scatter, Senbonzakura Kageyoshi.", 47_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SAMURAI_KABUTO, cape = true
        ),
        // 72. Yhwach
        fighter(
            "yhwach_almighty", "Yhwach", "Quincy Father", "Bleach: TYBW",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            425, 16, 1.32f, 1.68f, 0xFF09090B, 0xFF1E3A8A, 0xFF38BDF8, 0xFF09090B,
            WeaponVisualType.SPIRIT_BOW,
            PassiveArchetype.INFINITY_BARRIER, "The Almighty Foresight", "Alters future collisions to reduce incoming damage by 22%.",
            UltimateArchetype.SOUL_DEVOUR, "Auswählen Selection", 5.5f, 9.5f, 1.72f,
            "Drains rival HP & Ultimate gauge via sacred Quincy light pillars.",
            "My eyes see every grain of the future—The Almighty.", 96_000, EyeVisualStyle.HYPNOTIC_RINGS, CrownVisualStyle.ROYAL_CROWN, cape = true
        ),
        // 73. Katsuki Bakugo
        fighter(
            "bakugo_cluster", "Bakugo", "Great Explosion Murder God", "My Hero Academia",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.FIRE,
            360, 15, 1.50f, 1.24f, 0xFF18181B, 0xFFEA580C, 0xFFFACC15, 0xFFFDE047,
            WeaponVisualType.CYBER_CANNON,
            PassiveArchetype.ELASTIC_MOMENTUM, "Cluster Nitro Detonation", "Propels off every wall bounce with +20% blast speed & bank crits.",
            UltimateArchetype.METEOR_NUKE, "Howitzer Impact: Cluster", 5.5f, 8.5f, 1.68f,
            "Spins into a thermobaric tornado (-26 instant HP + burn zone).",
            "Out of my way, extras—HOWITZER IMPACT!", 37_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.SAIYAN_WILD
        ),
        // 74. All Might
        fighter(
            "all_might_symbol", "All Might", "Symbol of Peace", "My Hero Academia",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            430, 16, 1.34f, 1.86f, 0xFF1D4ED8, 0xFFDC2626, 0xFFFACC15, 0xFFFDE047,
            WeaponVisualType.SERIOUS_GLOVE,
            PassiveArchetype.SHOCKWAVE_KNOCKBACK, "Texas Smash Pressure", "Heroic wind pressure blasts rivals across the court (+38% knockback).",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "United States of Smash", 5.5f, 9.0f, 1.75f,
            "Channels every last ember into a 2.45x United States of Smash!",
            "Fear not—because I AM HERE!", 65_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.TWIN_TAILS, cape = true
        )
    )
}
