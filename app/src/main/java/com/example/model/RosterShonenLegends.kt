package com.example.model

/**
 * Characters 75–124 (50 Fighters):
 * Iconic Shonen Heroes & Villains across Demon Slayer, MHA, HxH, Black Clover, Yu Yu Hakusho,
 * Fire Force, Tokyo Ghoul, Seven Deadly Sins, Soul Eater, Kagurabachi, Sakamoto Days, Mashle & more.
 */
object RosterShonenLegends {
    val characters: List<CharacterData> = listOf(
        // 75. Kyojuro Rengoku
        fighter(
            "rengoku_flame", "Rengoku", "Flame Hashira", "Demon Slayer",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.FIRE,
            395, 16, 1.38f, 1.48f, 0xFFEA580C, 0xFFF8FAFC, 0xFFFACC15, 0xFFFDE047,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.BERSERKER_RAGE, "Set Your Heart Ablaze", "Unwavering Hashira spirit boosts Flame slash power up to +45% at low HP.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Ninth Form: Rengoku", 5.5f, 8.5f, 1.74f,
            "Charges forward as a roaring fire dragon for 2.4x purgatory damage!",
            "Set your heart ablaze—go beyond your limits!", 48_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.FLOWING_MANE, cape = true
        ),
        // 76. Akaza
        fighter(
            "akaza_compass", "Akaza", "Upper Moon Three", "Demon Slayer",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.HEAVY,
            390, 15, 1.48f, 1.38f, 0xFFF8FAFC, 0xFF1D4ED8, 0xFF38BDF8, 0xFFF43F5E,
            WeaponVisualType.STAR_FISTS,
            PassiveArchetype.BLACK_FLASH_CRIT, "Compass Needle", "Detects battle spirit—every 3rd martial clash lands a 1.45x Annihilation crit.",
            UltimateArchetype.HYPER_AWAKEN, "Destructive Death: Blue Silver", 5.5f, 8.5f, 1.68f,
            "Unleashes 100 omni-directional shockwave blows at +35% speed.",
            "Become a demon, Kyojuro—fight me forever!", 54_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.SHONEN_SPIKY, FaceMarkVisualStyle.CURSE_TATTOOS
        ),
        // 77. Kokushibo
        fighter(
            "kokushibo_moon", "Kokushibo", "Upper Moon One", "Demon Slayer",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.SLASH,
            420, 17, 1.42f, 1.54f, 0xFF4C1D95, 0xFF18181B, 0xFFC084FC, 0xFF09090B,
            WeaponVisualType.TRIPLE_KATANA,
            PassiveArchetype.CRIMSON_BLEED, "Crescent Moon Blades", "Erratic flesh-katana crescents carve 4 Bleed ticks across wide range.",
            UltimateArchetype.HAZARD_STORM, "Sixteenth Form: Moonbow", 5.5f, 9.0f, 1.74f,
            "Rains crescent moon blade craters across the arena.",
            "Six eyes see every flaw in your breathing.", 84_000, EyeVisualStyle.HYPNOTIC_RINGS, CrownVisualStyle.FLOWING_MANE, FaceMarkVisualStyle.FOREHEAD_SCAR
        ),
        // 78. Zenitsu Agatsuma
        fighter(
            "zenitsu_thunder", "Zenitsu", "Sleeping Thunderclap", "Demon Slayer",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.ELEC,
            330, 14, 1.60f, 0.98f, 0xFFFACC15, 0xFFEA580C, 0xFF38BDF8, 0xFFFDE047,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.ELASTIC_MOMENTUM, "Sixfold Ricochet", "Accelerates +20% off every wall bounce for thunderous bank-shot crits.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Flaming Thunder God", 5.0f, 8.0f, 1.72f,
            "Awakens golden lightning dragon dash for a 2.4x single-stroke slash!",
            "Thunder Breathing, Seventh Form—Flaming Thunder God!", 34_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHONEN_SPIKY, cape = true
        ),
        // 79. Tengen Uzui
        fighter(
            "tengen_sound", "Tengen", "Sound Hashira", "Demon Slayer",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.FIRE,
            385, 15, 1.46f, 1.40f, 0xFF1E293B, 0xFF991B1B, 0xFFF43F5E, 0xFFF8FAFC,
            WeaponVisualType.TITAN_BLADES,
            PassiveArchetype.ADAPTIVE_EVOLUTION, "Musical Score", "Reads enemy rhythm—gaining +3 attack & +3% defense per collision.",
            UltimateArchetype.METEOR_NUKE, "String Performance Blast", 5.5f, 8.5f, 1.66f,
            "Detonates anti-demon bombs with twin cleavers (-26 instant HP)!",
            "From here on out, things are getting flamboyant!", 41_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHINOBI_HEADBAND, FaceMarkVisualStyle.STAR_TEAR
        ),
        // 80. Giyu Tomioka
        fighter(
            "giyu_calm", "Giyu", "Water Hashira", "Demon Slayer",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.SLASH,
            375, 14, 1.36f, 1.32f, 0xFF0284C7, 0xFF991B1B, 0xFF38BDF8, 0xFF09090B,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.INFINITY_BARRIER, "Eleventh Form: Dead Calm", "Still water parries 22% of incoming impact damage.",
            UltimateArchetype.TIME_STOP, "Lull of Still Waters", 5.0f, 8.5f, 1.60f,
            "Calms the arena tide, halting rival momentum for 1.6s.",
            "Eleventh Form—Dead Calm.", 38_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SLICK_VILLAIN, cape = true
        ),
        // 81. Shoto Todoroki
        fighter(
            "todoroki_frostfire", "Todoroki", "Half-Cold Half-Hot", "My Hero Academia",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.FIRE,
            370, 15, 1.36f, 1.34f, 0xFF1E3A8A, 0xFFDC2626, 0xFF38BDF8, 0xFFF8FAFC,
            WeaponVisualType.KI_HALO,
            PassiveArchetype.FROST_GRAVITY_SLOW, "Glacier Wall Chill", "Flash-freezes rivals on contact, slowing speed 22% for 1.8s.",
            UltimateArchetype.METEOR_NUKE, "Phosphor Great Glacial Aegir", 5.5f, 8.5f, 1.68f,
            "Combines cold & flame into a thermal shockwave (-26 instant HP).",
            "This power is mine alone—Phosphor!", 39_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SLICK_VILLAIN, FaceMarkVisualStyle.FOREHEAD_SCAR
        ),
        // 82. All For One
        fighter(
            "all_for_one", "All For One", "Demon Lord of Kamino", "My Hero Academia",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            425, 16, 1.26f, 1.80f, 0xFF09090B, 0xFF4C1D95, 0xFFEF4444, 0xFFE2E8F0,
            WeaponVisualType.CYBER_CANNON,
            PassiveArchetype.ANTI_MAGIC_DRAIN, "Quirk Plunder", "Steals 11% of rival Ultimate meter on every collision.",
            UltimateArchetype.SOUL_DEVOUR, "Omni-Factor Unleash", 5.5f, 9.0f, 1.72f,
            "Fuses stolen Quirks to drain rival HP and smash with Air Cannon.",
            "Everything exists solely for my sake!", 76_000, EyeVisualStyle.HOLLOW_MASK, CrownVisualStyle.KNIGHT_HELM, cape = true
        ),
        // 83. Dabi (Toya Todoroki)
        fighter(
            "dabi_cremation", "Dabi", "Blueflame Cremation", "My Hero Academia",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.FIRE,
            350, 16, 1.40f, 1.24f, 0xFF0F172A, 0xFF2563EB, 0xFF38BDF8, 0xFFF8FAFC,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.SCORCH_BURN, "2,000°C Blueflame", "Searing cobalt flames burn rivals and leave inferno hazards.",
            UltimateArchetype.HAZARD_STORM, "Prominence Burn", 5.5f, 8.5f, 1.72f,
            "Turns the entire court into a cobalt cremation furnace!",
            "The past never dies—dance with me in hell!", 44_000, EyeVisualStyle.ESPER_PSYCHIC, CrownVisualStyle.SHONEN_SPIKY, FaceMarkVisualStyle.CYBER_SEAMS, cape = true
        ),
        // 84. Gon Freecss
        fighter(
            "gon_jajanken", "Gon", "Rookie Hunter", "Hunter x Hunter",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            380, 15, 1.38f, 1.46f, 0xFF16A34A, 0xFFEA580C, 0xFFFACC15, 0xFF09090B,
            WeaponVisualType.SERIOUS_GLOVE,
            PassiveArchetype.SHOCKWAVE_KNOCKBACK, "Jajanken: Rock", "Focused Enhancer aura blasts rivals back with +38% impact force.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Limitation Covenant", 5.5f, 9.0f, 1.80f,
            "Sacrifices everything for a colossal 2.5x Adult Nen strike!",
            "First comes Rock... Jajanken!", 52_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SAIYAN_WILD
        ),
        // 85. Hisoka Morow
        fighter(
            "hisoka_bungee", "Hisoka", "Grim Magician", "Hunter x Hunter",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            370, 15, 1.44f, 1.28f, 0xFFEC4899, 0xFF0D9488, 0xFFF472B6, 0xFFE11D48,
            WeaponVisualType.ARCANE_CARDS,
            PassiveArchetype.ELASTIC_MOMENTUM, "Bungee Gum Properties", "Possesses the properties of BOTH rubber and gum (+20% rebound & +50% bank DMG)!",
            UltimateArchetype.DOMAIN_STUN, "Texture Surprise Snap", 5.0f, 8.0f, 1.64f,
            "Snaps Bungee Gum tethers to stun the rival 1.55s and slice with Shu cards.",
            "Bungee Gum has the properties of both rubber and gum~", 49_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.SLICK_VILLAIN, FaceMarkVisualStyle.STAR_TEAR
        ),
        // 86. Kurapika
        fighter(
            "kurapika_chains", "Kurapika", "Scarlet Eyes Avenger", "Hunter x Hunter",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            365, 14, 1.40f, 1.28f, 0xFF1D4ED8, 0xFFFACC15, 0xFFEF4444, 0xFFFDE047,
            WeaponVisualType.CHAKRAM_RINGS,
            PassiveArchetype.ANTI_MAGIC_DRAIN, "Chain Jail Zetsu", "Holy chains seal 11% of rival Ultimate energy on every clash.",
            UltimateArchetype.SOUL_DEVOUR, "Emperor Time", 5.5f, 8.5f, 1.66f,
            "Ignites Scarlet Eyes for 100% Nen mastery, draining HP & meter.",
            "Judgment Chain—abide by my rule or perish.", 46_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.FLOWING_MANE
        ),
        // 87. Chrollo Lucilfer
        fighter(
            "chrollo_bandit", "Chrollo", "Phantom Troupe Boss", "Hunter x Hunter",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            380, 15, 1.38f, 1.36f, 0xFF1E1B4B, 0xFFF8FAFC, 0xFFA855F7, 0xFF09090B,
            WeaponVisualType.ARCANE_CARDS,
            PassiveArchetype.ARCANE_OVERCHARGE, "Skill Hunter Tome", "Stolen Nen pages accelerate Ultimate charge by 35%.",
            UltimateArchetype.METEOR_NUKE, "Sun and Moon Seal", 5.5f, 8.5f, 1.68f,
            "Triggers twin Sun & Moon Nen explosions (-27 instant HP).",
            "The spider's legs move even without the head.", 58_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SLICK_VILLAIN, FaceMarkVisualStyle.CROSS_SCAR, cape = true
        ),
        // 88. Isaac Netero
        fighter(
            "netero_bodhisattva", "Netero", "12th Hunter Chairman", "Hunter x Hunter",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.HEAVY,
            400, 16, 1.46f, 1.56f, 0xFFFACC15, 0xFFF8FAFC, 0xFFFDE047, 0xFFE2E8F0,
            WeaponVisualType.STAR_FISTS,
            PassiveArchetype.SHOCKWAVE_KNOCKBACK, "100-Type Guanyin Palm", "Supersonic prayer palms swat rivals away with +38% knockback.",
            UltimateArchetype.METEOR_NUKE, "Zero Hand", 5.5f, 9.0f, 1.76f,
            "Pours every drop of aura into a blinding golden Zero Hand (-28 HP)!",
            "A prayer comes from the heart!", 82_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.HALO_CROWN
        ),
        // 89. Yami Sukehiro
        fighter(
            "yami_dimension", "Yami", "Black Bulls Captain", "Black Clover",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.SLASH,
            405, 16, 1.34f, 1.62f, 0xFF18181B, 0xFF581C87, 0xFFA855F7, 0xFF09090B,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.EXECUTE_SHRINE, "Dark Cloaked Dimension Slash", "Cuts space itself—dealing +30% execution damage below 50% rival HP.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Death Thrust", 5.5f, 8.5f, 1.75f,
            "Condenses Mana Zone into a defense-shredding 2.45x Death Thrust!",
            "Surpass your limits—right here, right now!", 57_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHONEN_SPIKY, cape = true
        ),
        // 90. Yuno Grinberryall
        fighter(
            "yuno_star_wind", "Yuno", "Star & Wind Prince", "Black Clover",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.MAGIC,
            360, 15, 1.50f, 1.20f, 0xFF0D9488, 0xFFFACC15, 0xFF34D399, 0xFF09090B,
            WeaponVisualType.SPIRIT_BOW,
            PassiveArchetype.ARCANE_OVERCHARGE, "Four-Leaf & Star Grimoire", "Dual grimoires charge Ultimate 35% faster and pierce armor in Spirit Dive.",
            UltimateArchetype.HYPER_AWAKEN, "Saint Spirit of Zephyr", 5.5f, 8.0f, 1.68f,
            "Donning emerald crown & wing for +36% wind speed and star beams.",
            "I will become the Wizard King!", 48_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.ROYAL_CROWN, cape = true
        ),
        // 91. Lucius Zogratis
        fighter(
            "lucius_chrono", "Lucius", "Soul & Time Paladin", "Black Clover",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            415, 16, 1.38f, 1.52f, 0xFFF8FAFC, 0xFF4C1D95, 0xFFFACC15, 0xFF09090B,
            WeaponVisualType.KI_HALO,
            PassiveArchetype.INFINITY_BARRIER, "Chrono Anastasis", "Rewinds minor impact wounds to mitigate 22% of incoming damage.",
            UltimateArchetype.TIME_STOP, "Chrono Stasis Sphere", 5.0f, 9.0f, 1.70f,
            "Freezes the rival sphere inside a time orb for 1.65s!",
            "True world peace requires my final judgment.", 85_000, EyeVisualStyle.HYPNOTIC_RINGS, CrownVisualStyle.DEMON_HORNS, FaceMarkVisualStyle.CURSE_TATTOOS, cape = true
        ),
        // 92. Yusuke Urameshi
        fighter(
            "yusuke_reigun", "Yusuke", "Spirit Detective", "Yu Yu Hakusho",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.HEAVY,
            390, 15, 1.38f, 1.48f, 0xFF15803D, 0xFFFACC15, 0xFF38BDF8, 0xFF09090B,
            WeaponVisualType.KI_HALO,
            PassiveArchetype.BERSERKER_RAGE, "Mazoku Awakening", "Demon ancestry surges up to +45% attack power when battered.",
            UltimateArchetype.METEOR_NUKE, "Demon World Spirit Gun Mega", 5.5f, 8.5f, 1.70f,
            "Fires a massive sapphire Spirit Wave (-27 instant HP)!",
            "Bang—eat my Spirit Gun!", 51_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SLICK_VILLAIN, FaceMarkVisualStyle.CURSE_TATTOOS
        ),
        // 93. Hiei
        fighter(
            "hiei_dragon_flame", "Hiei", "Master of the Evil Eye", "Yu Yu Hakusho",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.FIRE,
            350, 16, 1.54f, 1.14f, 0xFF09090B, 0xFFF8FAFC, 0xFF7C3AED, 0xFF09090B,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.SCORCH_BURN, "Purgatory Black Flame", "Mortal-demon black flames scorch rivals and spawn shadow fire.",
            UltimateArchetype.HAZARD_STORM, "Dragon of the Darkness Flame", 5.5f, 8.5f, 1.72f,
            "Absorbs the black dragon to engulf every bounce in abyssal fire!",
            "Once unleashed, the Darkness Dragon devours everything.", 58_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.SAIYAN_WILD, FaceMarkVisualStyle.ROYAL_GEM, cape = true
        ),
        // 94. Younger Toguro
        fighter(
            "toguro_100", "Toguro", "100% Demon Muscle", "Yu Yu Hakusho",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.HEAVY,
            440, 17, 1.22f, 1.96f, 0xFF3F3F46, 0xFF18181B, 0xFFA855F7, 0xFF09090B,
            WeaponVisualType.SERIOUS_GLOVE,
            PassiveArchetype.TITAN_JUGGERNAUT, "80% Muscle Density", "Unmovable demon bulk resists knockback 35% and crushes foes (+20% DMG).",
            UltimateArchetype.SOUL_DEVOUR, "100% of 100%", 5.5f, 9.0f, 1.74f,
            "Inhales surrounding souls to heal and strike with catastrophic mass!",
            "You think this is even 80% of my strength?", 53_000, EyeVisualStyle.STAR_SHADES, CrownVisualStyle.SLICK_VILLAIN
        ),
        // 95. Shinra Kusakabe
        fighter(
            "shinra_bansho", "Shinra", "Devil's Footprints", "Fire Force",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.FIRE,
            360, 15, 1.56f, 1.18f, 0xFF18181B, 0xFFEA580C, 0xFF38BDF8, 0xFF09090B,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.ELASTIC_MOMENTUM, "Rapid Adolla Burst", "Jet-propelled kicks gain +20% speed off walls and +50% bank damage.",
            UltimateArchetype.HYPER_AWAKEN, "Shinrabanshoman", 5.5f, 8.5f, 1.72f,
            "Transcends lightspeed in Adolla form (+40% speed, 1.72x kicks).",
            "A hero always arrives to blast away despair!", 61_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.DEMON_HORNS, FaceMarkVisualStyle.FANG_JAW
        ),
        // 96. Benimaru Shinmon
        fighter(
            "benimaru_crimson", "Benimaru", "Asakusa Destruction King", "Fire Force",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.FIRE,
            395, 16, 1.38f, 1.54f, 0xFF18181B, 0xFFDC2626, 0xFFEF4444, 0xFF09090B,
            WeaponVisualType.KI_HALO,
            PassiveArchetype.SCORCH_BURN, "2nd & 3rd Gen Hybrid", "Controls rival flames while igniting explosive matoi fire rings.",
            UltimateArchetype.METEOR_NUKE, "Crimson Moon: Nichirin", 5.5f, 9.0f, 1.74f,
            "Drops a blazing Crimson Moon palm strike (-28 instant HP)!",
            "Crimson Moon—turn to ash.", 67_000, EyeVisualStyle.HYPNOTIC_RINGS, CrownVisualStyle.SHONEN_SPIKY
        ),
        // 97. Ken Kaneki
        fighter(
            "kaneki_centipede", "Kaneki", "One-Eyed King", "Tokyo Ghoul",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            375, 15, 1.44f, 1.34f, 0xFF18181B, 0xFFDC2626, 0xFFE11D48, 0xFFF8FAFC,
            WeaponVisualType.CURSED_CLEAVE,
            PassiveArchetype.VAMPIRIC_SIPHON, "Rinkaku Kagune", "Whip-like crimson kagune siphons 28% of slash damage as ghoul regen.",
            UltimateArchetype.HYPER_AWAKEN, "Kakuja Centipede", 5.5f, 8.5f, 1.66f,
            "Armors sphere in centipede Kakuja plating (+34% speed, 1.66x rend).",
            "What's 1000 minus 7?", 47_000, EyeVisualStyle.SCARRED_ONE_EYE, CrownVisualStyle.FLOWING_MANE, FaceMarkVisualStyle.FANG_JAW
        ),
        // 98. Kishou Arima
        fighter(
            "arima_reaper", "Arima", "CCG White Reaper", "Tokyo Ghoul",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.ELEC,
            370, 16, 1.48f, 1.30f, 0xFFF8FAFC, 0xFF1E293B, 0xFF818CF8, 0xFFE2E8F0,
            WeaponVisualType.ALCHEMY_SPEAR,
            PassiveArchetype.LIGHTNING_PARALYZE, "Narukami Quinque", "Condenses Rc lightning bolts that micro-stun rivals on impact.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Owl Quinque Execution", 5.0f, 8.5f, 1.70f,
            "Executes a flawless zero-blindspot 2.4x SSS-rate thrust!",
            "Step forward—my Quinque never misses.", 56_000, EyeVisualStyle.STAR_SHADES, CrownVisualStyle.SLICK_VILLAIN, cape = true
        ),
        // 99. Meliodas
        fighter(
            "meliodas_wrath", "Meliodas", "Dragon Sin of Wrath", "Seven Deadly Sins",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.MAGIC,
            390, 16, 1.40f, 1.44f, 0xFF15803D, 0xFF18181B, 0xFF7C3AED, 0xFFFACC15,
            WeaponVisualType.ZANGETSU_BLADE,
            PassiveArchetype.VECTOR_REFLECT, "Full Counter", "Lostvayne reflects 26% of incoming collision damage straight back!",
            UltimateArchetype.HYPER_AWAKEN, "Assault Mode Demon King", 5.5f, 8.5f, 1.72f,
            "Unleashes purgatory darkness wings (+34% speed, 1.72x Hellblaze).",
            "Sate, sate, sate—Full Counter!", 59_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.SHONEN_SPIKY, FaceMarkVisualStyle.CURSE_TATTOOS, cape = true
        ),
        // 100. Escanor
        fighter(
            "escanor_sun", "Escanor", "Lion Sin of Pride", "Seven Deadly Sins",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.FIRE,
            445, 17, 1.24f, 1.98f, 0xFFD97706, 0xFFFACC15, 0xFFF97316, 0xFFEA580C,
            WeaponVisualType.GRIMOIRE_GREATSWORD,
            PassiveArchetype.TITAN_JUGGERNAUT, "Sunshine High Noon", "Towering solar mass shrugs off knockback and burns lighter foes (+20% DMG).",
            UltimateArchetype.METEOR_NUKE, "Cruel Sun: The One", 6.0f, 9.5f, 1.78f,
            "Hurls a miniature star (-29 instant HP + solar inferno crater)!",
            "Who decided that?! I am The One who stands at the pinnacle!", 91_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.ROYAL_CROWN, cape = true
        ),
        // 101. Chihiro Rokuhira (Kagurabachi)
        fighter(
            "chihiro_entenslash", "Chihiro", "Enten Enchanted Blade", "Kagurabachi",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            360, 16, 1.50f, 1.22f, 0xFF09090B, 0xFFDC2626, 0xFFEF4444, 0xFF09090B,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.BLACK_FLASH_CRIT, "Kuro: Black Goldfish", "Every 3rd enchanted katana draw triggers a 1.45x Kuro slash crit.",
            UltimateArchetype.SOUL_DEVOUR, "Aka & Nishiki Convergence", 5.5f, 8.0f, 1.70f,
            "Absorbs rival energy via Aka while cloaking in tricolor goldfish speed!",
            "Every morning I wake up with fresh hatred—Kuro!", 63_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.SLICK_VILLAIN, FaceMarkVisualStyle.FOREHEAD_SCAR, cape = true
        ),
        // 102. Taro Sakamoto
        fighter(
            "sakamoto_hitman", "Sakamoto", "Legendary Ex-Hitman", "Sakamoto Days",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            405, 15, 1.44f, 1.58f, 0xFF16A34A, 0xFFFACC15, 0xFF38BDF8, 0xFFE2E8F0,
            WeaponVisualType.HELLSING_PISTOLS,
            PassiveArchetype.ELASTIC_MOMENTUM, "Store-Clerk Improvisation", "Turns every wall bounce into a +20% speed & +50% bank-shot counter!",
            UltimateArchetype.HYPER_AWAKEN, "Slim Prime Hitman Mode", 5.5f, 8.0f, 1.68f,
            "Burns calories instantly into prime assassin mode (+38% speed, 1.68x DMG).",
            "Family Rule #1: No killing—just total knockout.", 55_000, EyeVisualStyle.STAR_SHADES, CrownVisualStyle.SLICK_VILLAIN
        ),
        // 103. Nagumo (Sakamoto Days)
        fighter(
            "nagumo_trickblade", "Nagumo", "Order Disguise Master", "Sakamoto Days",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            360, 15, 1.50f, 1.20f, 0xFF1E293B, 0xFF334155, 0xFF22D3EE, 0xFF09090B,
            WeaponVisualType.TRIPLE_KATANA,
            PassiveArchetype.CRIMSON_BLEED, "Six-Tool Swiss Blade", "Multi-weapon roll inflicts 4 Bleed ticks and +12% surprise crits.",
            UltimateArchetype.TIME_STOP, "Mirage Feint", 5.0f, 8.5f, 1.64f,
            "Bamboozles the rival for 1.6s with an invisible weapon switch!",
            "Roll the dice—pick how you lose.", 51_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SHONEN_SPIKY, FaceMarkVisualStyle.CURSE_TATTOOS
        ),
        // 104. Mash Burnedead
        fighter(
            "mash_muscle", "Mash", "Muscle Magic Savant", "Mashle: Magic and Muscles",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            425, 16, 1.40f, 1.84f, 0xFF18181B, 0xFFFACC15, 0xFFFDE047, 0xFF09090B,
            WeaponVisualType.SERIOUS_GLOVE,
            PassiveArchetype.VECTOR_REFLECT, "Cream-Puff Pectorals", "Bounces spells right off bare knuckles, reflecting 26% collision damage.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Unlimited Physical Mode", 5.5f, 8.5f, 1.76f,
            "Removes iron wrist weights for a 2.45x Hurricane Rush!",
            "Sorry—I solved your spell with my quads.", 58_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.BOWL_ESPER, FaceMarkVisualStyle.LIGHTNING_BOLTS, cape = true
        ),
        // 105. Andy (Undead Unluck)
        fighter(
            "andy_undead", "Andy", "Negator: UNDEAD", "Undead Unluck",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.SLASH,
            415, 14, 1.38f, 1.50f, 0xFF334155, 0xFF991B1B, 0xFFEF4444, 0xFFE2E8F0,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.REGEN_IMMORTAL, "Negate Death", "Regenerates +2% Max HP every 3rd wall bounce via Parts Bullet propulsion.",
            UltimateArchetype.HEAL_REFORGE, "Crimson Crescent Clone", 5.5f, 8.0f, 1.62f,
            "Rebuilds +55 HP instantly and fires high-pressure blood slashes!",
            "Sick! Wicked sick!", 46_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SAIYAN_WILD, FaceMarkVisualStyle.FOREHEAD_SCAR
        ),
        // 106. Rudo Surebrec (Gachiakuta)
        fighter(
            "rudo_gachiakuta", "Rudo", "3R Giver Cleaner", "Gachiakuta",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.HEAVY,
            375, 15, 1.40f, 1.44f, 0xFF3F3F46, 0xFFDC2626, 0xFFEF4444, 0xFFE2E8F0,
            WeaponVisualType.SERIOUS_GLOVE,
            PassiveArchetype.BERSERKER_RAGE, "Watchman Gloves", "Extracts maximum soul value from scraps—up to +45% power at low HP.",
            UltimateArchetype.METEOR_NUKE, "Anima Overdrive", 5.5f, 8.5f, 1.68f,
            "Weaponizes junk into a colossal trash-beast impact (-26 HP)!",
            "Don't call something worthless before it's spent!", 45_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.SHONEN_SPIKY, FaceMarkVisualStyle.BLOOD_STRIPE
        ),
        // 107. Maka Albarn & Soul
        fighter(
            "maka_scythe", "Maka", "Scythe Meister", "Soul Eater",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            360, 15, 1.44f, 1.25f, 0xFF18181B, 0xFFDC2626, 0xFF818CF8, 0xFFFDE68A,
            WeaponVisualType.CRESCENT_SCYTHE,
            PassiveArchetype.ANTI_MAGIC_DRAIN, "Anti-Demon Wavelength", "Witch-Hunter resonance purges 11% of rival Ultimate meter per slash.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Kishin Hunter", 5.5f, 8.5f, 1.72f,
            "Resonates Soul into silver Kishin Hunter blade for 2.4x slice!",
            "Soul Resonance—Kishin Hunter!", 48_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.TWIN_TAILS, cape = true
        ),
        // 108. Black Star
        fighter(
            "black_star_tsu", "Black☆Star", "Shadow Star Assassin", "Soul Eater",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.ELEC,
            370, 15, 1.50f, 1.32f, 0xFF1D4ED8, 0xFFF8FAFC, 0xFF38BDF8, 0xFF38BDF8,
            WeaponVisualType.CHAKRAM_RINGS,
            PassiveArchetype.SHOCKWAVE_KNOCKBACK, "Big Wave Soul Menace", "Punches wavelength directly through armor with +38% knockback.",
            UltimateArchetype.HYPER_AWAKEN, "Uncanny Sword: Shadow Star", 5.5f, 8.0f, 1.66f,
            "Merges with shadow blade Tsubaki for +36% speed and 1.66x strikes.",
            "I am the man who will surpass God!", 44_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SAIYAN_WILD, FaceMarkVisualStyle.STAR_TEAR
        ),
        // 109. Asura (Kishin)
        fighter(
            "kishin_asura", "Asura", "First Kishin of Madness", "Soul Eater",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            405, 15, 1.36f, 1.48f, 0xFF7F1D1D, 0xFFF8FAFC, 0xFFEF4444, 0xFF09090B,
            WeaponVisualType.CURSED_CLEAVE,
            PassiveArchetype.FROST_GRAVITY_SLOW, "Madness Wavelength", "Paranoia aura warps rival focus, slowing speed 22% for 1.8s.",
            UltimateArchetype.HAZARD_STORM, "Vajra Madness Storm", 5.5f, 9.0f, 1.68f,
            "Floods the court with crimson scarf & Vajra laser zones.",
            "Fear is the root of all imagination...", 62_000, EyeVisualStyle.HYPNOTIC_RINGS, CrownVisualStyle.WITCH_HOOD, cape = true
        ),
        // 110. Rin Okumura
        fighter(
            "rin_blueflame", "Rin", "Son of Satan", "Blue Exorcist",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.FIRE,
            375, 15, 1.40f, 1.38f, 0xFF1E3A8A, 0xFF09090B, 0xFF38BDF8, 0xFF0F172A,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.SCORCH_BURN, "Kurikara Unsheathed", "Satanic blue flames ignite rivals on every katana clash.",
            UltimateArchetype.HYPER_AWAKEN, "Gehenna Demon Heart", 5.5f, 8.5f, 1.66f,
            "Blazes with twin blue flame horns for +34% speed and 1.66x slash.",
            "I'm gonna kick Satan's ass!", 39_000, EyeVisualStyle.ESPER_PSYCHIC, CrownVisualStyle.DEMON_HORNS
        ),
        // 111. Korosensei
        fighter(
            "korosensei_mach20", "Korosensei", "Mach 20 Teacher", "Assassination Classroom",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.MAGIC,
            380, 14, 1.62f, 1.12f, 0xFFFACC15, 0xFF18181B, 0xFFFDE047, 0x00000000,
            WeaponVisualType.CHAKRAM_RINGS,
            PassiveArchetype.ELASTIC_MOMENTUM, "Mach 20 Evasion", "ricochets off walls at Mach 20 (+20% burst & +50% bank damage).",
            UltimateArchetype.HEAL_REFORGE, "Absolute Defense Mucus", 5.5f, 8.5f, 1.58f,
            "Sheds skin & reforms (+52 HP heal) while firing antimatter beams!",
            "Nurufufufu! Attendance time, class!", 64_000, EyeVisualStyle.STAR_SHADES, CrownVisualStyle.ROYAL_CROWN, FaceMarkVisualStyle.FANG_JAW, cape = true
        ),
        // 112.Karma Akabane
        fighter(
            "karma_assassin", "Karma", "Class 3-E Sadist", "Assassination Classroom",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            350, 15, 1.46f, 1.18f, 0xFF991B1B, 0xFF18181B, 0xFFEF4444, 0xFFDC2626,
            WeaponVisualType.SHADOW_DAGGERS,
            PassiveArchetype.EXECUTE_SHRINE, "Wasabi Ambush", "Ruthless precision deals +30% extra damage when rival is below 50% HP.",
            UltimateArchetype.TIME_STOP, "Calculated Trap", 5.0f, 8.0f, 1.62f,
            "Springs a feint stun trap (1.6s freeze) for free critical stabs.",
            "Expecting a fair fight from me was your first mistake.", 33_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.SHONEN_SPIKY
        ),
        // 113. Yor Forger
        fighter(
            "yor_thorn", "Yor", "Thorn Princess", "Spy x Family",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            365, 16, 1.52f, 1.26f, 0xFF09090B, 0xFFDC2626, 0xFFF43F5E, 0xFF09090B,
            WeaponVisualType.SHADOW_DAGGERS,
            PassiveArchetype.CRIMSON_BLEED, "Golden Stilettos", "Lethal Garden needles inflict 4 Bleed ticks and +12% crit damage.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Supersonic Home-Run Kick", 5.0f, 8.0f, 1.72f,
            "Shatters the sound barrier with a 2.4x Thorn Princess heel kick!",
            "May I have the honor of taking your life?", 45_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.ROYAL_CROWN
        ),
        // 114. Senku Ishigami
        fighter(
            "senku_science", "Senku", "10 Billion % Scientist", "Dr. Stone",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.ELEC,
            350, 14, 1.38f, 1.24f, 0xFFF8FAFC, 0xFF16A34A, 0xFF4ADE80, 0xFFDCFCE7,
            WeaponVisualType.CYBER_CANNON,
            PassiveArchetype.ARCANE_OVERCHARGE, "Kingdom of Science", "Chemical catalysis charges Ultimate 35% faster every second.",
            UltimateArchetype.TIME_STOP, "Medusa Petrification Beam", 5.0f, 8.5f, 1.64f,
            "12,800,000 meters—1 second: petrifies rival sphere in stone for 1.65s!",
            "Get excited—this is ten billion percent exhilarating!", 47_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.SAIYAN_WILD, FaceMarkVisualStyle.CYBER_SEAMS
        ),
        // 115. Tsukasa Shishio
        fighter(
            "tsukasa_primate", "Tsukasa", "Strongest Primate", "Dr. Stone",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            415, 16, 1.32f, 1.78f, 0xFF78350F, 0xFF18181B, 0xFFD97706, 0xFF451A03,
            WeaponVisualType.GRIMOIRE_GREATSWORD,
            PassiveArchetype.SHOCKWAVE_KNOCKBACK, "Lion-Slayer Fist", "Shatters stone statues in one punch (+38% knockback shockwave).",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Stone-Sword Cleave", 5.5f, 8.5f, 1.70f,
            "Delivers a 2.4x primal greatsword smash!",
            "In this stone world, only pure youth shall thrive.", 38_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.FLOWING_MANE, FaceMarkVisualStyle.CYBER_SEAMS, cape = true
        ),
        // 116. Kenshin Himura
        fighter(
            "kenshin_battosai", "Kenshin", "Hitokiri Battosai", "Rurouni Kenshin",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            355, 15, 1.54f, 1.15f, 0xFFDC2626, 0xFFF8FAFC, 0xFF38BDF8, 0xFFB91C1C,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.BLACK_FLASH_CRIT, "Hiten Mitsurugi Godspeed", "Every 3rd reverse-blade draw lands a 1.45x Nine-Headed Dragon crit.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Amakakeru Ryu no Hirameki", 5.0f, 8.0f, 1.74f,
            "Steps with the left foot for a vacuum-creating 2.45x Batoujutsu!",
            "This reverse-blade sword protects the weak!", 56_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.FLOWING_MANE, FaceMarkVisualStyle.CROSS_SCAR
        ),
        // 117. Makoto Shishio
        fighter(
            "shishio_inferno", "Shishio", "Bandaged Inferno", "Rurouni Kenshin",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.FIRE,
            385, 16, 1.38f, 1.42f, 0xFFF8FAFC, 0xFF7F1D1D, 0xFFF97316, 0xFFE2E8F0,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.SCORCH_BURN, "Mugenjin Serrated Oils", "Human-oil soaked blade ignites on every slash, burning the rival.",
            UltimateArchetype.HAZARD_STORM, "Final Secret Sword: Kaguzuchi", 5.5f, 8.5f, 1.70f,
            "Swirls a cyclone of fire around Mugenjin across the court!",
            "If you're strong, you live; if you're weak, you die!", 54_000, EyeVisualStyle.HOLLOW_MASK, CrownVisualStyle.SHINOBI_HEADBAND, cape = true
        ),
        // 118. Gintoki Sakata
        fighter(
            "gintoki_shiroyasha", "Gintoki", "White Yaksha", "Gintama",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.SLASH,
            390, 15, 1.38f, 1.48f, 0xFFF8FAFC, 0xFF0284C7, 0xFF38BDF8, 0xFFBAE6FD,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.BERSERKER_RAGE, "Lake Toya Bokuto", "Lazy samurai turns terrifying when cornered (+45% low-HP damage).",
            UltimateArchetype.HYPER_AWAKEN, "Shiroyasha War Demon", 5.5f, 8.5f, 1.70f,
            "Drops the jokes—unleashing Joui War demon speed (+34%) and lethal cuts.",
            "Protect my silver soul to the very end!", 60_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHONEN_SPIKY
        ),
        // 119. Shinsuke Takasugi
        fighter(
            "takasugi_kiheitai", "Takasugi", "Kiheitai Commander", "Gintama",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            365, 16, 1.44f, 1.28f, 0xFF581C87, 0xFFFACC15, 0xFFA855F7, 0xFF3B0764,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.EXECUTE_SHRINE, "Destroy This Rotten World", "Deals +30% ruthless execution damage against wounded rivals.",
            UltimateArchetype.METEOR_NUKE, "Kiheitai Barrage", 5.0f, 8.5f, 1.66f,
            "Calls down airship cannon fire (-26 instant HP) and strikes from smoke.",
            "I simply destroy this rotten world.", 52_000, EyeVisualStyle.SCARRED_ONE_EYE, CrownVisualStyle.SLICK_VILLAIN, cape = true
        ),
        // 120. Inuyasha
        fighter(
            "inuyasha_tessaiga", "Inuyasha", "Half-Demon Dog", "Inuyasha",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.SLASH,
            385, 15, 1.38f, 1.46f, 0xFFDC2626, 0xFFF8FAFC, 0xFFFACC15, 0xFFE2E8F0,
            WeaponVisualType.ZANGETSU_BLADE,
            PassiveArchetype.SHOCKWAVE_KNOCKBACK, "Wind Scar Rift", "Swings Tessaiga to blast demonic wind fissures (+38% knockback).",
            UltimateArchetype.METEOR_NUKE, "Backlash Wave: Meido Zangetsuha", 5.5f, 8.5f, 1.68f,
            "Carves a crescent Meido portal (-27 instant HP + vortex).",
            "Sit? Never! Eat my Wind Scar!", 44_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.TWIN_TAILS
        ),
        // 121. Sesshomaru
        fighter(
            "sesshomaru_bakusaiga", "Sesshomaru", "Great Dog Daiyokai", "Inuyasha",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            400, 16, 1.46f, 1.44f, 0xFFF8FAFC, 0xFF991B1B, 0xFF4ADE80, 0xFFF8FAFC,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.CRIMSON_BLEED, "Bakusaiga Decomposition", "Acidic daiyokai slashes inflict 4 corrosive Bleed ticks.",
            UltimateArchetype.HYPER_AWAKEN, "True Daiyokai Awakening", 5.5f, 8.5f, 1.72f,
            "Soars on toxic light trails (+34% speed, 1.72x Bakusaiga decomposition).",
            "I, Sesshomaru, bow to nothing in heaven or earth.", 68_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.FLOWING_MANE, FaceMarkVisualStyle.ROYAL_GEM, cape = true
        ),
        // 122. Tsuna Sawada
        fighter(
            "tsuna_vongola", "Tsuna", "Vongola Decimo", "Katekyo Hitman Reborn!",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.FIRE,
            360, 15, 1.48f, 1.22f, 0xFF18181B, 0xFFEA580C, 0xFFF97316, 0xFF451A03,
            WeaponVisualType.SERIOUS_GLOVE,
            PassiveArchetype.SCORCH_BURN, "Dying Will Sky Flame", "X-Gloves ignite high-purity Sky Flames on every impact.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "X-Burner Full Power", 5.5f, 8.5f, 1.74f,
            "Balances soft & hard flames for a 2.45x X-Burner cannon blast!",
            "Operation X—X-Burner!!", 49_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SAIYAN_WILD, FaceMarkVisualStyle.ROYAL_GEM, cape = true
        ),
        // 123. Allen Walker
        fighter(
            "allen_crown_clown", "Allen", "Crown Clown Exorcist", "D.Gray-man",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.SLASH,
            370, 15, 1.42f, 1.30f, 0xFFF8FAFC, 0xFF18181B, 0xFF38BDF8, 0xFFE2E8F0,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.ANTI_MAGIC_DRAIN, "Innocence Sword of Exorcism", "Purifies dark matter, draining 11% of rival Ultimate meter.",
            UltimateArchetype.HYPER_AWAKEN, "Crown Clown Critical", 5.5f, 8.5f, 1.66f,
            "Dons gleaming white cowl & greatsword (+34% speed, 1.66x slash).",
            "Activate Innocence—Crown Belt!", 48_000, EyeVisualStyle.SCARRED_ONE_EYE, CrownVisualStyle.WITCH_HOOD, FaceMarkVisualStyle.STAR_TEAR, cape = true
        ),
        // 124. Boros
        fighter(
            "boros_meteoric", "Boros", "Dominator of the Universe", "One Punch Man",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.ELEC,
            435, 17, 1.46f, 1.72f, 0xFF1E1B4B, 0xFFEC4899, 0xFF38BDF8, 0xFFF472B6,
            WeaponVisualType.KI_HALO,
            PassiveArchetype.REGEN_IMMORTAL, "Apex Alien Regeneration", "Instant cellular focus heals +2% Max HP every 3rd wall bounce.",
            UltimateArchetype.METEOR_NUKE, "Collapsing Star Roaring Cannon", 6.0f, 9.5f, 1.80f,
            "Enters Meteoric Burst to fire a planet-razing beam (-30 instant HP)!",
            "Meteoric Burst—Collapsing Star Roaring Cannon!", 110_000, EyeVisualStyle.CYBER_VISOR, CrownVisualStyle.SAIYAN_WILD
        )
    )
}
