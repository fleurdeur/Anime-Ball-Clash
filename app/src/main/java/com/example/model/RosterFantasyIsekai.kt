package com.example.model

/**
 * Characters 125–174 (50 Fighters):
 * Fantasy, Dark Fantasy, Isekai & Historical Legends (Frieren, Solo Leveling, Re:Zero,
 * Mushoku Tensei, Konosuba, Eminence in Shadow, Fate, Vinland Saga, Claymore, Magi & more).
 */
object RosterFantasyIsekai {
    val characters: List<CharacterData> = listOf(
        // 125. Fern
        fighter(
            "fern_rapid_zoltraak", "Fern", "First-Class Prodigy", "Frieren: Beyond Journey's End",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.MAGIC,
            355, 14, 1.48f, 1.18f, 0xFF312E81, 0xFF18181B, 0xFFC084FC, 0xFF6B21A8,
            WeaponVisualType.MAGE_STAFF,
            PassiveArchetype.BLACK_FLASH_CRIT, "Quick-Draw Zoltraak", "Lightning-fast casting triggers a 1.45x piercing volley every 3rd hit.",
            UltimateArchetype.HYPER_AWAKEN, "Saturation Barrage", 5.5f, 8.0f, 1.66f,
            "Overwhelms defenses with relentless rapid-fire Zoltraak (+35% speed).",
            "Simple offensive magic, fired faster than anyone else.", 38_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.FLOWING_MANE, cape = true
        ),
        // 126. Stark
        fighter(
            "stark_lightning_axe", "Stark", "Vanguard Warrior", "Frieren: Beyond Journey's End",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.SLASH,
            415, 16, 1.28f, 1.76f, 0xFF991B1B, 0xFFF8FAFC, 0xFFFACC15, 0xFFDC2626,
            WeaponVisualType.GRIMOIRE_GREATSWORD,
            PassiveArchetype.BERSERKER_RAGE, "Eisen's Resolve", "Even trembling knees strike harder—up to +45% axe cleave when wounded.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Lightning Strike Cleave", 5.5f, 8.5f, 1.72f,
            "Brings down a canyon-splitting 2.4x overhead warrior axe strike!",
            "Lightning Strike!!", 36_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHONEN_SPIKY, FaceMarkVisualStyle.FOREHEAD_SCAR
        ),
        // 127. Aura the Guillotine
        fighter(
            "aura_scales", "Aura", "Sage of Destruction", "Frieren: Beyond Journey's End",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            375, 15, 1.34f, 1.38f, 0xFF4C1D95, 0xFFFACC15, 0xFFE879F9, 0xFFF5D0FE,
            WeaponVisualType.CHAKRAM_RINGS,
            PassiveArchetype.ANTI_MAGIC_DRAIN, "Scales of Obedience", "Weighs souls on impact, draining 11% of rival Ultimate mana.",
            UltimateArchetype.DOMAIN_STUN, "Auserlese Submission", 5.0f, 8.5f, 1.64f,
            "Tips the scales to command rival stillness for 1.6s!",
            "Place your soul upon the Scales of Obedience.", 42_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.DEMON_HORNS, cape = true
        ),
        // 128. Antares (Solo Leveling)
        fighter(
            "antares_destruction", "Antares", "Monarch of Destruction", "Solo Leveling",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.FIRE,
            450, 17, 1.34f, 1.96f, 0xFF991B1B, 0xFF09090B, 0xFFEF4444, 0xFFDC2626,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.SCORCH_BURN, "Dragon's Fear", "Primordial dragon fire scorches rivals and melts arena ground.",
            UltimateArchetype.METEOR_NUKE, "Breath of Destruction", 6.0f, 9.5f, 1.80f,
            "Erases matter with a scarlet Breath of Destruction (-30 instant HP)!",
            "All creation returns to ash before the Dragon King!", 125_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.DEMON_HORNS, cape = true
        ),
        // 129. Beru (Solo Leveling)
        fighter(
            "beru_marshal", "Beru", "Shadow Ant Marshal", "Solo Leveling",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            385, 16, 1.56f, 1.36f, 0xFF0F172A, 0xFF6D28D9, 0xFFA855F7, 0xFF09090B,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.VAMPIRIC_SIPHON, "Gluttonous Predation", "Devours enemy essence to heal 28% of claw damage dealt.",
            UltimateArchetype.HYPER_AWAKEN, "My Liege's Fury", 5.5f, 8.0f, 1.70f,
            "Spreads shadow insect wings for +40% blitz velocity and 1.70x shred!",
            "KIIIIEEEK! For my Liege!", 78_000, EyeVisualStyle.ESPER_PSYCHIC, CrownVisualStyle.DEMON_HORNS, cape = true
        ),
        // 130. Igris (Solo Leveling)
        fighter(
            "igris_bloodred", "Igris", "Blood-Red Knight Marshal", "Solo Leveling",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.SLASH,
            405, 16, 1.40f, 1.56f, 0xFF18181B, 0xFF991B1B, 0xFFA855F7, 0xFFDC2626,
            WeaponVisualType.GRIMOIRE_GREATSWORD,
            PassiveArchetype.CRIMSON_BLEED, "Demon King's Longsword", "Telekinetic greatsword arcs carve 4 Bleed ticks and lightning sparks.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Knight's Guillotine", 5.5f, 8.5f, 1.72f,
            "Delivers a silent, honorable 2.4x shadow greatsword execution!",
            "...", 74_000, EyeVisualStyle.CYBER_VISOR, CrownVisualStyle.KNIGHT_HELM, cape = true
        ),
        // 131. Artoria Pendragon / Saber
        fighter(
            "saber_artoria", "Saber", "King of Knights", "Fate/stay night: UBW",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.MAGIC,
            400, 16, 1.38f, 1.52f, 0xFF1D4ED8, 0xFFF8FAFC, 0xFFFACC15, 0xFFFDE047,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.INFINITY_BARRIER, "Invisible Air & Avalon", "Wind sheath & fairy scabbard reduce incoming impact damage by 22%.",
            UltimateArchetype.METEOR_NUKE, "Excalibur: Sword of Promised Victory", 5.5f, 9.0f, 1.75f,
            "Unleashes a golden pillar of light (-28 instant HP + shockwave)!",
            "EX—CALIBURRR!!", 82_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.ROYAL_CROWN, cape = true
        ),
        // 132. Archer EMIYA
        fighter(
            "archer_ubw", "EMIYA", "Wrought Iron Hero", "Fate/stay night: UBW",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.SLASH,
            375, 15, 1.42f, 1.34f, 0xFF991B1B, 0xFF18181B, 0xFF38BDF8, 0xFFF8FAFC,
            WeaponVisualType.SPIRIT_BOW,
            PassiveArchetype.VECTOR_REFLECT, "Rho Aias Seven Petals", "Conceptual shield reflects 26% of incoming collision damage.",
            UltimateArchetype.HAZARD_STORM, "Unlimited Blade Works", 6.0f, 9.0f, 1.68f,
            "Projects an infinite reality marble of swords across the arena!",
            "I am the bone of my sword—Unlimited Blade Works!", 68_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHONEN_SPIKY, cape = true
        ),
        // 133. Saber Alter
        fighter(
            "saber_alter", "Saber Alter", " blackened Tyrant", "Fate/stay night: Heaven's Feel",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.MAGIC,
            425, 17, 1.26f, 1.78f, 0xFF09090B, 0xFF7F1D1D, 0xFFEF4444, 0xFFFEF08A,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.TITAN_JUGGERNAUT, "Blackened Grail Armor", "Heavy mana armor resists knockback 35% and crushes lighter foes (+20% DMG).",
            UltimateArchetype.METEOR_NUKE, "Excalibur Morgan", 5.5f, 9.0f, 1.76f,
            "Detonates a black-crimson mana deluge (-29 instant HP)!",
            "Swallow the light—Excalibur Morgan!", 84_000, EyeVisualStyle.CYBER_VISOR, CrownVisualStyle.KNIGHT_HELM, cape = true
        ),
        // 134. Cu Chulainn / Lancer
        fighter(
            "lancer_gae_bolg", "Cú Chulainn", "Hound of Culann", "Fate/stay night: UBW",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            365, 15, 1.52f, 1.24f, 0xFF1D4ED8, 0xFFE2E8F0, 0xFFEF4444, 0xFF2563EB,
            WeaponVisualType.ALCHEMY_SPEAR,
            PassiveArchetype.EXECUTE_SHRINE, "Reverse Causality", "Barbedcrimson spear deals +30% heart-seeking damage below 50% rival HP.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Gáe Bolg: Soaring Spear", 5.0f, 8.0f, 1.74f,
            "Reverses cause and effect for a guaranteed 2.45x heart-pierce!",
            "Your heart is already mine—Gáe Bolg!", 56_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.FLOWING_MANE
        ),
        // 135. Cid Kagenou / Shadow
        fighter(
            "cid_atomic", "Shadow", "Eminence in Shadow", "The Eminence in Shadow",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            395, 16, 1.46f, 1.40f, 0xFF09090B, 0xFF4C1D95, 0xFFA855F7, 0xFF09090B,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.ARCANE_OVERCHARGE, "Slime Bodysuit Condenser", "99.99% mana conductivity charges Ultimate 35% faster.",
            UltimateArchetype.METEOR_NUKE, "I... Am... ATOMIC", 5.5f, 9.5f, 1.82f,
            "Vaporizes the night in violet radiance (-30 instant HP + shockwave)!",
            "I... am... Atomic.", 115_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.WITCH_HOOD, cape = true
        ),
        // 136. Subaru Natsuki
        fighter(
            "subaru_rbd", "Subaru", "Knight of Emilia", "Re:Zero",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            380, 13, 1.36f, 1.28f, 0xFF18181B, 0xFFEA580C, 0xFF7C3AED, 0xFF09090B,
            WeaponVisualType.CHAKRAM_RINGS,
            PassiveArchetype.BERSERKER_RAGE, "Return by Resolve", "Defying despair boosts Invisible Providence power up to +45% at low HP.",
            UltimateArchetype.HEAL_REFORGE, "Cor Leonis Second Shift", 5.5f, 8.0f, 1.62f,
            "Restores +58 HP with Unseen Hand fortitude and rallies counterattacks!",
            "I will save everyone—no matter how many tries it takes!", 35_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SLICK_VILLAIN
        ),
        // 137. Rem
        fighter(
            "rem_morningstar", "Rem", "Oni Maid Berserker", "Re:Zero",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            385, 15, 1.38f, 1.54f, 0xFF1E293B, 0xFFF8FAFC, 0xFF38BDF8, 0xFF60A5FA,
            WeaponVisualType.DEMON_KANABO,
            PassiveArchetype.FROST_GRAVITY_SLOW, "Al Huma Ice Spikes", "Flail & ice shards chill rival speed by 22% for 1.8s.",
            UltimateArchetype.HYPER_AWAKEN, "Single-Horn Oni Frenzy", 5.5f, 8.0f, 1.68f,
            "Grows glowing white oni horn for +34% speed and 1.68x morningstar slams!",
            "Fanatical like a demon!", 44_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.DEMON_HORNS
        ),
        // 138. Regulus Corneas
        fighter(
            "regulus_greed", "Regulus", "Archbishop of Greed", "Re:Zero",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            400, 15, 1.34f, 1.50f, 0xFFF8FAFC, 0xFFEAB308, 0xFF38BDF8, 0xFFE2E8F0,
            WeaponVisualType.PSYCHIC_DEBRIS,
            PassiveArchetype.INFINITY_BARRIER, "Lion's Heart Stillness", "Stops personal object time to ignore 22% of incoming collision force.",
            UltimateArchetype.METEOR_NUKE, "Unstoppable Gravel Barrage", 5.0f, 9.0f, 1.68f,
            "Flings time-frozen debris that rips through space (-26 instant HP)!",
            "You are infringing upon my rights!", 62_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SLICK_VILLAIN
        ),
        // 139. Reinhard van Astrea
        fighter(
            "reinhard_sword_saint", "Reinhard", "Sword Saint", "Re:Zero",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.SLASH,
            430, 17, 1.46f, 1.64f, 0xFFF8FAFC, 0xFFDC2626, 0xFF38BDF8, 0xFFEF4444,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.ADAPTIVE_EVOLUTION, "Infinite Divine Protections", "Receives new blessings every hit (+3 attack & +3% defense, max 8).",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Dragon Sword Reid Unsheathed", 5.5f, 9.0f, 1.78f,
            "Draws Reid to cleave sky and arena alike for 2.5x damage!",
            "The Dragon Sword deems you worthy.", 105_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SHONEN_SPIKY, cape = true
        ),
        // 140. Megumin
        fighter(
            "megumin_explosion", "Megumin", "Crimson Demon Archwizard", "KonoSuba",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.FIRE,
            335, 15, 1.36f, 1.16f, 0xFF991B1B, 0xFF18181B, 0xFFEF4444, 0xFF27272A,
            WeaponVisualType.MAGE_STAFF,
            PassiveArchetype.ARCANE_OVERCHARGE, "Chant Obsession", "Single-minded Explosion passion charges Ultimate 35% faster!",
            UltimateArchetype.METEOR_NUKE, "EX—PLOSION!!", 4.5f, 9.5f, 1.85f,
            "Detonates the arena's biggest nuke (-32 instant HP + huge fire crater)!",
            "Darkness blacker than black—EXPLOSION!!", 55_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.WITCH_HOOD, cape = true
        ),
        // 141. Rudeus Greyrat
        fighter(
            "rudeus_quagmire", "Rudeus", "Quagmire Mage", "Mushoku Tensei",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            375, 15, 1.38f, 1.42f, 0xFF78350F, 0xFF334155, 0xFF38BDF8, 0xFFD97706,
            WeaponVisualType.MAGE_STAFF,
            PassiveArchetype.FROST_GRAVITY_SLOW, "Quagmire & Eye of Foresight", "Mires rival velocity by 22% while predicting collision angles.",
            UltimateArchetype.METEOR_NUKE, "Magic Armor Mk.I: Stone Cannon", 5.5f, 8.5f, 1.70f,
            "Fires hyper-dense gatling Stone Cannons (-27 instant HP)!",
            "Silent Voiceless Incantation—Stone Cannon!", 52_000, EyeVisualStyle.ESPER_PSYCHIC, CrownVisualStyle.FLOWING_MANE, cape = true
        ),
        // 142. Orsted
        fighter(
            "orsted_dragon_god", "Orsted", "Dragon God #2", "Mushoku Tensei",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.SLASH,
            435, 17, 1.42f, 1.78f, 0xFFF8FAFC, 0xFF18181B, 0xFFFACC15, 0xFFE2E8F0,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.INFINITY_BARRIER, "Saint Dragon Touki", "Impenetrable Dragon Gate aura reduces incoming damage by 22%.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Godblade Unsheathed", 5.5f, 9.5f, 1.80f,
            "Draws the cursed Godblade for an inescapable 2.5x Dragon God strike!",
            "Do you serve Hitogami?", 98_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.SLICK_VILLAIN, cape = true
        ),
        // 143. Kirito
        fighter(
            "kirito_starburst", "Kirito", "Black Swordsman VR", "Sword Art Online",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            365, 15, 1.50f, 1.24f, 0xFF09090B, 0xFF334155, 0xFF38BDF8, 0xFF09090B,
            WeaponVisualType.TITAN_BLADES,
            PassiveArchetype.BLACK_FLASH_CRIT, "Dual Blades Vorpal", "Elucidator & Dark Repulser trigger a 1.45x Sword Skill crit every 3rd hit.",
            UltimateArchetype.HYPER_AWAKEN, "Starburst Stream (16-Hit)", 5.5f, 8.0f, 1.68f,
            "Accelerates +36% in blue system glow for relentless twin-sword combos!",
            "Link Start—Starburst Stream!", 43_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHONEN_SPIKY, cape = true
        ),
        // 144. Asuna Yuuki
        fighter(
            "asuna_flash", "Asuna", "Lightning Flash", "Sword Art Online",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.ELEC,
            355, 14, 1.54f, 1.16f, 0xFFF8FAFC, 0xFFDC2626, 0xFFF43F5E, 0xFFEA580C,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.ELASTIC_MOMENTUM, "Lambent Light Quadruple Pain", "Rapier lunges gain +20% speed off walls and +50% bank damage.",
            UltimateArchetype.HEAL_REFORGE, "Goddess Stacia: Mother's Rosario", 5.5f, 8.0f, 1.64f,
            "Restores +50 HP in radiant Stacia light and unleashes an 11-hit star thrust!",
            "Mother's Rosario—shine through!", 41_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.FLOWING_MANE, cape = true
        ),
        // 145. Sunraku (Shangri-La Frontier)
        fighter(
            "sunraku_bird", "Sunraku", "Bird-Mask Speedrunner", "Shangri-La Frontier",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            340, 16, 1.58f, 1.05f, 0xFF0284C7, 0xFF78350F, 0xFF38BDF8, 0xFF0369A1,
            WeaponVisualType.SHADOW_DAGGERS,
            PassiveArchetype.BLACK_FLASH_CRIT, "Vorpal Soul Crit", "Frame-perfect parry timing lands a guaranteed 1.45x Vorpal crit every 3rd hit!",
            UltimateArchetype.HYPER_AWAKEN, "Exceed Charge: Moon Blade", 5.5f, 7.5f, 1.70f,
            "Pushes AGI to cap (+40% speed) with zero-armor frame-perfect slashes!",
            "Trash-game reflexes make boss patterns child's play!", 53_000, EyeVisualStyle.HOLLOW_MASK, CrownVisualStyle.MECHA_VFIN, FaceMarkVisualStyle.FANG_JAW
        ),
        // 146. Naofumi Iwatani
        fighter(
            "naofumi_shield", "Naofumi", "Shield Hero", "Rising of the Shield Hero",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            445, 13, 1.22f, 1.92f, 0xFF166534, 0xFF18181B, 0xFF4ADE80, 0xFF09090B,
            WeaponVisualType.CHAKRAM_RINGS,
            PassiveArchetype.VECTOR_REFLECT, "Legendary Cardinal Shield", "Impenetrable buckler reflects 26% of all collision damage back!",
            UltimateArchetype.DOMAIN_STUN, "Iron Maiden & Rage Shield", 5.5f, 8.5f, 1.64f,
            "Locks the rival inside Iron Maiden (1.6s stun) with cursed dragon fire!",
            "Shield Prison—Iron Maiden!", 42_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHONEN_SPIKY, cape = true
        ),
        // 147. Albedo (Overlord)
        fighter(
            "albedo_guardian", "Albedo", "Guardian Overseer", "Overlord",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.HEAVY,
            435, 15, 1.30f, 1.84f, 0xFF09090B, 0xFFFACC15, 0xFFA855F7, 0xFF09090B,
            WeaponVisualType.GRIMOIRE_GREATSWORD,
            PassiveArchetype.INFINITY_BARRIER, "Hermes Trismegistus", "Triple-layered divine plate reduces incoming collision damage by 22%.",
            UltimateArchetype.METEOR_NUKE, "Ginnungagap World Cleave", 5.5f, 9.0f, 1.68f,
            "Swings her World-Item bardiche to shatter the court (-27 instant HP)!",
            "None shall sully Lord Ainz's glorious name!", 59_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.DEMON_HORNS, cape = true
        ),
        // 148. Shalltear Bloodfallen
        fighter(
            "shalltear_valkyrie", "Shalltear", "Bloody Valkyrie", "Overlord",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            395, 15, 1.44f, 1.42f, 0xFF991B1B, 0xFFF8FAFC, 0xFFEF4444, 0xFFE2E8F0,
            WeaponVisualType.ALCHEMY_SPEAR,
            PassiveArchetype.VAMPIRIC_SIPHON, "Spuit Lance", "Crimson pipette lance heals 28% of every collision's damage dealt!",
            UltimateArchetype.SOUL_DEVOUR, "Einherjar Blood Frenzy", 5.5f, 8.5f, 1.68f,
            "Summons spectral avatar to drain rival HP and boost lance speed!",
            "Drink deep, my Spuit Lance!", 57_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.ROYAL_CROWN
        ),
        // 149. Thorfinn Karlsefni
        fighter(
            "thorfinn_daggers", "Thorfinn", "True Warrior of Vinland", "Vinland Saga",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            375, 15, 1.52f, 1.24f, 0xFF78350F, 0xFF1E293B, 0xFFFACC15, 0xFFFDE047,
            WeaponVisualType.SHADOW_DAGGERS,
            PassiveArchetype.ELASTIC_MOMENTUM, "Dual-Dagger Acrobatics", "Nimble Viking footwork gains +20% rebound speed and +50% bank damage.",
            UltimateArchetype.HYPER_AWAKEN, "Hundred-Punch Resolve", 5.5f, 8.0f, 1.66f,
            "Enters absolute calm (+36% speed) to disarm and overwhelm any rival.",
            "I have no enemies—only resolve.", 54_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.FLOWING_MANE
        ),
        // 150. Thorkell the Tall
        fighter(
            "thorkell_viking", "Thorkell", "Jomsviking Giant", "Vinland Saga",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.HEAVY,
            450, 17, 1.24f, 2.00f, 0xFF991B1B, 0xFF78350F, 0xFFF97316, 0xFFFDE047,
            WeaponVisualType.TITAN_BLADES,
            PassiveArchetype.TITAN_JUGGERNAUT, "Twin Battle-Axes", "Giant Viking mass resists knockback 35% and cleaves smaller spheres (+20% DMG).",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Horizon Spear Hurl", 5.5f, 9.0f, 1.75f,
            "Launches a 2.45x log-sized Viking axe smash!",
            "Bwahaha! Nothing beats a glorious battle!", 52_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHINOBI_HEADBAND, FaceMarkVisualStyle.FOREHEAD_SCAR, cape = true
        ),
        // 151. Askeladd
        fighter(
            "askeladd_lucius", "Askeladd", "Artorius's Descendant", "Vinland Saga",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.SLASH,
            375, 15, 1.40f, 1.34f, 0xFF334155, 0xFFCA8A04, 0xFFFACC15, 0xFFE2E8F0,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.EXECUTE_SHRINE, "Cunning Taunt", "Exploits emotional openings for +30% damage below 50% rival HP.",
            UltimateArchetype.TIME_STOP, "King's Gambit Feint", 5.0f, 8.5f, 1.64f,
            "Baits the rival into a 1.6s stun opening for a clean Roman sword cut.",
            "Everyone is a slave to something.", 49_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SLICK_VILLAIN, cape = true
        ),
        // 152. Clare (Claymore)
        fighter(
            "clare_quicksword", "Clare", "No. 47 Claymore", "Claymore",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            365, 16, 1.52f, 1.24f, 0xFFE2E8F0, 0xFF475569, 0xFFFACC15, 0xFFFEF08A,
            WeaponVisualType.GRIMOIRE_GREATSWORD,
            PassiveArchetype.CRIMSON_BLEED, "Irene's Quicksword", "Supersonic arm vibration inflicts 4 Bleed ticks on every greatsword clash.",
            UltimateArchetype.HYPER_AWAKEN, "Twin Goddess Teresa Awakening", 5.5f, 8.5f, 1.74f,
            "Unfurls four radiant white wings (+38% speed, 1.74x Quicksword flurry)!",
            "Awaken within me, Teresa!", 66_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.SHONEN_SPIKY, cape = true
        ),
        // 153. Priscilla (Claymore)
        fighter(
            "priscilla_abyssal", "Priscilla", "One-Horned Awakened", "Claymore",
            CharacterAlignment.ANTAGONIST, AnimeTier.NICHE, CharacterClass.ATTACKER, ElementTag.SLASH,
            415, 17, 1.44f, 1.58f, 0xFF4C1D95, 0xFF18181B, 0xFFD946EF, 0xFF581C87,
            WeaponVisualType.CURSED_CLEAVE,
            PassiveArchetype.REGEN_IMMORTAL, "Bottomless Yoki Regen", "Regenerates +2% Max HP every 3rd bounce with limitless Abyssal Yoki.",
            UltimateArchetype.SOUL_DEVOUR, "Abyssal Devouring", 5.5f, 9.0f, 1.72f,
            "Sprouts winged blades to drain rival HP and shred defenses.",
            "My hunger can never be sated...", 72_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.DEMON_HORNS, cape = true
        ),
        // 154. Bojji (Ranking of Kings)
        fighter(
            "bojji_needle", "Bojji", "First Prince Giant-less", "Ranking of Kings",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            335, 15, 1.60f, 0.92f, 0xFF2563EB, 0xFFDC2626, 0xFFFACC15, 0xFF451A03,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.BLACK_FLASH_CRIT, "Despa's Acupressure Strike", "Pinpoints vital seams—every 3rd rapier tap triggers a 1.45x armor-bypass crit!",
            UltimateArchetype.TIME_STOP, "Pressure-Point Knockout", 5.0f, 8.0f, 1.68f,
            "Taps exact fault lines to stun the rival sphere cleanly for 1.65s!",
            "Ah—uuh! (With Kage at my side, I fear nothing!)", 58_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.ROYAL_CROWN, cape = true
        ),
        // 155. Laios Touden (Delicious in Dungeon)
        fighter(
            "laios_kensuke", "Laios", "Monster Gourmet Knight", "Delicious in Dungeon",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.SLASH,
            405, 15, 1.28f, 1.66f, 0xFF94A3B8, 0xFF1E3A8A, 0xFFFACC15, 0xFFFDE047,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.ADAPTIVE_EVOLUTION, "Monster Anatomy Guide", "Studies enemy habits per hit (+3 attack & +3% armor, up to 8x)!",
            UltimateArchetype.HEAL_REFORGE, "Senshi's Hotpot Feast", 5.5f, 8.0f, 1.60f,
            "Restores +58 HP with dungeon cuisine and swings living sword Kensuke!",
            "Wait—I wonder what this rival tastes like?!", 39_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.KNIGHT_HELM, cape = true
        ),
        // 156. Marcille Donato
        fighter(
            "marcille_ancient", "Marcille", "Ancient Magic Scholar", "Delicious in Dungeon",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.FIRE,
            355, 15, 1.36f, 1.20f, 0xFF1D4ED8, 0xFFDC2626, 0xFFF97316, 0xFFFDE047,
            WeaponVisualType.MAGE_STAFF,
            PassiveArchetype.SCORCH_BURN, "Ambrosia Flash Blast", "Explosive dungeon mana scorches rivals and spawns blast craters.",
            UltimateArchetype.METEOR_NUKE, "Forbidden Ancient Familiar", 5.5f, 8.5f, 1.68f,
            "Detonates chain-reaction mana bombs (-27 instant HP)!",
            "I did NOT sign up to eat monsters—eat magic instead!", 38_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.TWIN_TAILS
        ),
        // 157. Sinbad (Magi)
        fighter(
            "sinbad_baal", "Sinbad", "High King of Seven Seas", "Magi: Labyrinth of Magic",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.ELEC,
            410, 17, 1.44f, 1.56f, 0xFF581C87, 0xFFFACC15, 0xFF38BDF8, 0xFF7E22CE,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.LIGHTNING_PARALYZE, "Seven Djinn Equip: Baal", "Wrath & Heroes lightning stuns rivals 0.26s on contact.",
            UltimateArchetype.METEOR_NUKE, "Bararaq Saiqa", 5.5f, 9.0f, 1.78f,
            "Calls down Baal's Extreme Magic lightning sword (-29 instant HP)!",
            "Dwell within my blade, Spirit of Wrath—Baal!", 88_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.ROYAL_CROWN, cape = true
        ),
        // 158. Aladdin (Magi)
        fighter(
            "aladdin_solomon", "Aladdin", "Magi of Solomon", "Magi: Labyrinth of Magic",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            370, 15, 1.38f, 1.32f, 0xFF2563EB, 0xFFF8FAFC, 0xFFFACC15, 0xFF3B82F6,
            WeaponVisualType.MAGE_STAFF,
            PassiveArchetype.VECTOR_REFLECT, "Borg & Vector Law", "Rukh hemisphere reflects 26% of incoming impact damage.",
            UltimateArchetype.GRAVITY_CRUSH, "Solomon's Wisdom: Dhoruf", 5.5f, 8.5f, 1.68f,
            "Applies infinite inertial vector force (-26 instant HP + slow)!",
            "The Rukh guide our destiny!", 56_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SHINOBI_HEADBAND, FaceMarkVisualStyle.ROYAL_GEM
        ),
        // 159. Bell Cranel
        fighter(
            "bell_argonaut", "Bell", "Little Rookie Rabbit", "Is It Wrong to Try to Pick Up Girls in a Dungeon?",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.FIRE,
            355, 15, 1.54f, 1.14f, 0xFFF8FAFC, 0xFF1E293B, 0xFFEF4444, 0xFFF8FAFC,
            WeaponVisualType.SHADOW_DAGGERS,
            PassiveArchetype.ARCANE_OVERCHARGE, "Liaris Freese Growth", "Single-hearted yearning builds Ultimate 35% faster!",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Full-Chime Argonaut Firebolt", 5.5f, 8.5f, 1.76f,
            "Rings the grand hero bell for a 2.45x Hestia Knife Firebolt!",
            "FIREBOLT—full Argonaut charge!", 44_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.SHONEN_SPIKY
        ),
        // 160. Goblin Slayer
        fighter(
            "goblin_slayer", "Orcbolg", "Silver-Ranked Slayer", "Goblin Slayer",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.SLASH,
            395, 15, 1.32f, 1.58f, 0xFF475569, 0xFF78350F, 0xFFEF4444, 0xFF334155,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.EXECUTE_SHRINE, "Ruthless Improvisation", "Deals +30% lethal finishing damage when rival drops below 50% HP.",
            UltimateArchetype.METEOR_NUKE, "Gate Scroll: Ocean Bottom Pressure", 5.0f, 8.5f, 1.68f,
            "Unrolls a deep-sea pressure scroll (-27 instant HP)!",
            "I see.", 43_000, EyeVisualStyle.CYBER_VISOR, CrownVisualStyle.KNIGHT_HELM
        ),
        // 161. Vampire Hunter D
        fighter(
            "hunter_d_dhampir", "D", "Sacred Ancestor Dhampir", "Vampire Hunter D: Bloodlust",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            405, 16, 1.46f, 1.48f, 0xFF09090B, 0xFF1E3A8A, 0xFFEF4444, 0xFF09090B,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.VAMPIRIC_SIPHON, "Left Hand Symbiote", "Devours element & blood to heal 28% of longsword damage dealt.",
            UltimateArchetype.ULT_NULLIFY, "Space-Severing Stroke", 5.5f, 8.5f, 1.74f,
            "Cancels active rival Awakening and slices the fount of life!",
            "Even immortality can be cut.", 86_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.FEDORA_TRILBY, cape = true
        ),
        // 162. Trevor Belmont
        fighter(
            "trevor_vampire_killer", "Trevor", "Last Son of Belmont", "Castlevania",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.FIRE,
            385, 15, 1.36f, 1.46f, 0xFF78350F, 0xFF991B1B, 0xFFF97316, 0xFF451A03,
            WeaponVisualType.CHAKRAM_RINGS,
            PassiveArchetype.SCORCH_BURN, "Consecrated Morning Star", "Holy whip detonates creatures of the night on every impact.",
            UltimateArchetype.METEOR_NUKE, "Grand Cross & Cross Boomerang", 5.5f, 8.5f, 1.68f,
            "Erupts pillars of consecrated flame (-26 instant HP)!",
            "I'm Trevor Belmont—and dying has never frightened me.", 48_000, EyeVisualStyle.SCARRED_ONE_EYE, CrownVisualStyle.FLOWING_MANE, FaceMarkVisualStyle.CROSS_SCAR, cape = true
        ),
        // 163. Vlad Dracula Tepes
        fighter(
            "dracula_tepes", "Dracula", "Lord of Wallachia", "Castlevania",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.FIRE,
            435, 17, 1.34f, 1.76f, 0xFF09090B, 0xFF991B1B, 0xFFEF4444, 0xFF09090B,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.VAMPIRIC_SIPHON, "Crimson Night Siphon", "Restores 28% of collision damage dealt via dark alchemy.",
            UltimateArchetype.METEOR_NUKE, "Dark Inferno Meteor", 5.5f, 9.5f, 1.76f,
            "Hurls hellfire meteors across the court (-29 instant HP)!",
            "What is a man? Enough talk—have at you!", 82_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.SLICK_VILLAIN, cape = true
        ),
        // 164. Sora & Shiro (Blank)
        fighter(
            "blank_ngnl", "Blank", "Undefeated Gamers 「　」", "No Game No Life",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            355, 14, 1.44f, 1.20f, 0xFF7C3AED, 0xFFEC4899, 0xFFFACC15, 0xFFF8FAFC,
            WeaponVisualType.ARCANE_CARDS,
            PassiveArchetype.ELASTIC_MOMENTUM, "100-Move Chess Trajectory", "Calculates every wall angle for +20% speed and +50% bank damage.",
            UltimateArchetype.TIME_STOP, "Aschente: Checkmate", 5.0f, 8.5f, 1.66f,
            "Binds rival by Ten Pledges checkmate for 1.65s!",
            "Blank never loses!", 54_000, EyeVisualStyle.HYPNOTIC_RINGS, CrownVisualStyle.ROYAL_CROWN, cape = true
        ),
        // 165. Jibril
        fighter(
            "jibril_flugel", "Jibril", "War-Born Flügel", "No Game No Life",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            385, 16, 1.44f, 1.38f, 0xFFEC4899, 0xFFF8FAFC, 0xFFF472B6, 0xFFFBCFE8,
            WeaponVisualType.KI_HALO,
            PassiveArchetype.ARCANE_OVERCHARGE, "Spirit Circuit Shift", "God-killing Flügel halo charges Ultimate 35% faster.",
            UltimateArchetype.METEOR_NUKE, "Heaven's Strike", 5.5f, 9.0f, 1.74f,
            "Condenses atmospheric spirits into a cratering blast (-28 HP)!",
            "Heaven's Strike—farewell!", 66_000, EyeVisualStyle.HYPNOTIC_RINGS, CrownVisualStyle.HALO_CROWN, cape = true
        ),
        // 166. Tanya Degurechaff
        fighter(
            "tanya_imperial", "Tanya", "Devil of the Rhine", "Saga of Tanya the Evil",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.FIRE,
            355, 16, 1.54f, 1.18f, 0xFF1E3A8A, 0xFF15803D, 0xFFFACC15, 0xFFFDE047,
            WeaponVisualType.CYBER_CANNON,
            PassiveArchetype.BLACK_FLASH_CRIT, "Elenium Type-95 Orb", "Miracle computation triggers 1.45x artillery crits every 3rd hit.",
            UltimateArchetype.METEOR_NUKE, "Golden Prayer Artillery", 5.0f, 8.5f, 1.72f,
            "Reluctant prayer unleashes golden optical nuke (-27 instant HP)!",
            "Oh God, smash them to pieces!", 61_000, EyeVisualStyle.ESPER_PSYCHIC, CrownVisualStyle.PIRATE_TRICORNE
        ),
        // 167. Reg (Made in Abyss)
        fighter(
            "reg_incinerator", "Reg", "Aubade Artifact Boy", "Made in Abyss",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.ATTACKER, ElementTag.FIRE,
            395, 15, 1.42f, 1.52f, 0xFF78350F, 0xFFDC2626, 0xFF38BDF8, 0xFF451A03,
            WeaponVisualType.CYBER_CANNON,
            PassiveArchetype.ELASTIC_MOMENTUM, "Extendable Grapple Arms", "Slingshots off arena walls with +20% speed & +50% bank damage.",
            UltimateArchetype.METEOR_NUKE, "Rewrite Rules: Incinerator", 5.5f, 9.0f, 1.74f,
            "Fires Relic beam that rewrites Abyssal laws (-28 instant HP)!",
            "Irredeemable! Incinerator—fire!", 63_000, EyeVisualStyle.CYBER_VISOR, CrownVisualStyle.KNIGHT_HELM, cape = true
        ),
        // 168. Faputa (Made in Abyss)
        fighter(
            "faputa_princess", "Faputa", "Princess of Hollows", "Made in Abyss",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            370, 16, 1.56f, 1.22f, 0xFFF8FAFC, 0xFF991B1B, 0xFFFACC15, 0xFFF8FAFC,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.REGEN_IMMORTAL, "Immortal Value Claws", "Heals +2% Max HP every 3rd bounce with undying sosu ferocity.",
            UltimateArchetype.HYPER_AWAKEN, "Golden Wrath Sosu", 5.5f, 8.0f, 1.70f,
            "Ignites golden claws (+38% speed, 1.70x tearing combos)!",
            "Faputa is the embodiment of value, sosu!", 68_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.DEMON_HORNS
        ),
        // 169. Milim Nava
        fighter(
            "milim_destroyer", "Milim", "Destroyer Dragonoid", "Reincarnated as a Slime",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.FIRE,
            420, 17, 1.48f, 1.66f, 0xFFEC4899, 0xFF18181B, 0xFFF43F5E, 0xFFF9A8D4,
            WeaponVisualType.STAR_FISTS,
            PassiveArchetype.BERSERKER_RAGE, "Wrathful King Satanael", "Infinite mana breeder scales attack up to +45% in heated brawls.",
            UltimateArchetype.METEOR_NUKE, "Drago Nova", 5.5f, 9.0f, 1.78f,
            "Fires stardust particle beam Drago Nova (-29 instant HP)!",
            "Wahahaha! Witness Drago Nova!", 84_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.TWIN_TAILS, cape = true
        ),
        // 170. Diablo (Slime)
        fighter(
            "diablo_noir", "Diablo", "Primordial Black Noir", "Reincarnated as a Slime",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            395, 16, 1.44f, 1.42f, 0xFF09090B, 0xFFDC2626, 0xFFFACC15, 0xFF09090B,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.ANTI_MAGIC_DRAIN, "Temptation World", "Butler claws unravel rival magic—draining 11% Ultimate meter.",
            UltimateArchetype.DOMAIN_STUN, "End of World Collapse", 5.5f, 8.5f, 1.70f,
            "Collapses reality marble to stun rival 1.6s and shred defenses!",
            "Kufufu... allow me to tidy up Lord Rimuru's enemies.", 72_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.SLICK_VILLAIN, cape = true
        ),
        // 171. Veldora Tempest
        fighter(
            "veldora_storm", "Veldora", "Storm True Dragon", "Reincarnated as a Slime",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.ELEC,
            435, 16, 1.38f, 1.82f, 0xFFCA8A04, 0xFF1E293B, 0xFF38BDF8, 0xFFFACC15,
            WeaponVisualType.KI_HALO,
            PassiveArchetype.SHOCKWAVE_KNOCKBACK, "Veldora-Style Killing Arts", "Manga-inspired tempest blasts knock rivals back +38%.",
            UltimateArchetype.HAZARD_STORM, "Black Lightning Roar", 5.5f, 9.0f, 1.70f,
            "Blankets the court in black storm-dragon tempest zones!",
            "Kwahaha! Tremble before Storm Dragon Veldora!", 74_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SAIYAN_WILD, cape = true
        ),
        // 172. Bercouli / Alice Zuberg
        fighter(
            "alice_fragrant", "Alice", "Integrity Knight S30", "Sword Art Online: Alicization",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.SLASH,
            385, 15, 1.38f, 1.45f, 0xFFFACC15, 0xFF1D4ED8, 0xFFFDE047, 0xFFFDE047,
            WeaponVisualType.ROYAL_GATES,
            PassiveArchetype.CRIMSON_BLEED, "Fragrant Olive Blossom", "Thousand golden petals inflict 4 Bleed ticks across wide orbit.",
            UltimateArchetype.HAZARD_STORM, "Release Recollection", 5.5f, 8.5f, 1.66f,
            "Sweeps golden blossom vortices across every wall rebound!",
            "Enhance Armament—Fragrant Olive Sword!", 49_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.KNIGHT_HELM, cape = true
        ),
        // 173. Gilgamesh Rival: Iskandar
        fighter(
            "iskandar_ionioi", "Iskandar", "King of Conquerors", "Fate/Zero",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.ELEC,
            440, 16, 1.34f, 1.90f, 0xFF991B1B, 0xFF78350F, 0xFFFACC15, 0xFFDC2626,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.TITAN_JUGGERNAUT, "Gordius Wheel Charge", "Divine bull chariot tramples lighter spheres (+20% DMG, +35% poise).",
            UltimateArchetype.METEOR_NUKE, "Ionioi Hetairoi", 5.5f, 9.0f, 1.72f,
            "Charges with ten thousand immortal companions (-27 instant HP)!",
            "ALALALALAAAI!! Charge to Oceanus!", 64_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.ROYAL_CROWN, cape = true
        ),
        // 174. Kotomine Kirei
        fighter(
            "kirei_black_keys", "Kirei", "Executor Priest", "Fate/Zero",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            380, 15, 1.44f, 1.40f, 0xFF09090B, 0xFF7F1D1D, 0xFFEF4444, 0xFF451A03,
            WeaponVisualType.SHADOW_DAGGERS,
            PassiveArchetype.BLACK_FLASH_CRIT, "Baji Quan & Black Keys", "Close-range Bajiquan strikes trigger a 1.45x heart-shock crit every 3rd hit.",
            UltimateArchetype.SOUL_DEVOUR, "Angra Mainyu Mud", 5.5f, 8.5f, 1.66f,
            "Curses rival with Grail mud—draining HP and Ultimate meter!",
            "Yorokobe, shounen. Your wish shall be granted.", 51_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SLICK_VILLAIN, FaceMarkVisualStyle.CROSS_SCAR, cape = true
        )
    )
}
