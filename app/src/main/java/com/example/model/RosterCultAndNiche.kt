package com.example.model

/**
 * Characters 175–224 (50 Fighters):
 * Mecha, Cyberpunk, JoJo Parts 2–7, Martial Arts / Sports, Magical Girl, and Deep-Niche Classics!
 * Brings the complete Anime Ball Clash roster to 224 distinct heroes & villains.
 */
object RosterCultAndNiche {
    val characters: List<CharacterData> = listOf(
        // 175. Shinji Ikari / EVA-01
        fighter(
            "shinji_eva01", "EVA-01", "Test Type Awakening", "Neon Genesis Evangelion",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.MAGIC,
            425, 16, 1.36f, 1.78f, 0xFF581C87, 0xFF22C55E, 0xFFEF4444, 0xFF4ADE80,
            WeaponVisualType.ALCHEMY_SPEAR,
            PassiveArchetype.INFINITY_BARRIER, "A.T. Field Phase", "Absolute Terror Field neutralizes 22% of incoming impact force.",
            UltimateArchetype.HYPER_AWAKEN, "400% Sync: Third Impact Halo", 6.0f, 9.0f, 1.78f,
            "Shatters restraints with radiant red halo (+35% speed, 1.78x Lance of Longinus)!",
            "I mustn't run away—400% Synchro!", 92_000, EyeVisualStyle.CYBER_VISOR, CrownVisualStyle.MECHA_VFIN, cape = true
        ),
        // 176. Asuka Langley / EVA-02
        fighter(
            "asuka_eva02", "Asuka", "Second Child Ace", "Neon Genesis Evangelion",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            375, 16, 1.50f, 1.34f, 0xFFDC2626, 0xFFEA580C, 0xFFFACC15, 0xFFD97706,
            WeaponVisualType.TITAN_BLADES,
            PassiveArchetype.CRIMSON_BLEED, "Progressive Knife Blitz", "High-frequency vibration knives carve 4 Bleed ticks on contact.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Code 777 Beast Mode", 5.5f, 8.5f, 1.74f,
            "Unlocks Beast Mode for a ferocious 2.4x dual-glaive lunge!",
            "Are you stupid?! Watch a real ace pilot!", 64_000, EyeVisualStyle.SCARRED_ONE_EYE, CrownVisualStyle.TWIN_TAILS
        ),
        // 177. Kaworu Nagisa
        fighter(
            "kaworu_tabris", "Kaworu", "Seventeenth Angel", "Neon Genesis Evangelion",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            380, 15, 1.40f, 1.36f, 0xFFE2E8F0, 0xFF312E81, 0xFF818CF8, 0xFFCBD5E1,
            WeaponVisualType.ALCHEMY_SPEAR,
            PassiveArchetype.ARCANE_OVERCHARGE, "Tabris Song of Grace", "Supreme Angelic A.T. resonance charges Ultimate 35% faster.",
            UltimateArchetype.GRAVITY_CRUSH, "Dual Spears of Cassius & Longinus", 5.5f, 8.5f, 1.70f,
            "Levitates & crushes rival momentum (-26 instant HP + slow)!",
            "Music is the finest creation of the Lilin.", 68_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.HALO_CROWN
        ),
        // 178. Lelouch Lamperouge / Zero
        fighter(
            "lelouch_geass", "Lelouch", "Zero of Black Knights", "Code Geass",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            365, 15, 1.36f, 1.32f, 0xFF18181B, 0xFF6D28D9, 0xFFEC4899, 0xFF09090B,
            WeaponVisualType.MECHA_FUNNELS,
            PassiveArchetype.VECTOR_REFLECT, "Shinkiro Absolute Protection", "Hexagonal prism shield reflects 26% of collision damage.",
            UltimateArchetype.TIME_STOP, "Absolute Obedience Geass", 5.0f, 9.0f, 1.68f,
            "Lelouch vi Britannia commands you: FREEZE! (1.65s stun + Hadron Cannon).",
            "Lelouch vi Britannia commands you—obey me, world!", 84_000, EyeVisualStyle.HYPNOTIC_RINGS, CrownVisualStyle.KNIGHT_HELM, cape = true
        ),
        // 179. Suzaku Kururugi / Lancelot Albion
        fighter(
            "suzaku_albion", "Suzaku", "Knight of Zero", "Code Geass",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.ELEC,
            385, 15, 1.56f, 1.34f, 0xFFF8FAFC, 0xFFCA8A04, 0xFF34D399, 0xFF451A03,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.BERSERKER_RAGE, "'LIVE!' Geass Curse", "Survival reflex surges power up to +45% when HP drops critical!",
            UltimateArchetype.HYPER_AWAKEN, "Energy Wings: Lancelot Albion", 5.5f, 8.0f, 1.72f,
            "Spreads emerald Energy Wings for +40% speed and MVS twin-sword blitz!",
            "That Geass command forces me to LIVE!", 66_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.MECHA_VFIN, cape = true
        ),
        // 180. Kamina
        fighter(
            "kamina_gurren", "Kamina", "Team Dai-Gurren Leader", "Gurren Lagann",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.FIRE,
            395, 16, 1.40f, 1.54f, 0xFFDC2626, 0xFF1D4ED8, 0xFFF97316, 0xFF2563EB,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.BERSERKER_RAGE, "Manly Fighting Spirit", "Refuses to fall—scaling attack up to +45% as HP burns down!",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Who the Hell Do You Think I Am?!", 5.5f, 8.5f, 1.76f,
            "Combines spirit into a blazing 2.45x Gurren boomerang cleave!",
            "Believe in the you who believes in yourself!", 69_000, EyeVisualStyle.STAR_SHADES, CrownVisualStyle.SAIYAN_WILD, FaceMarkVisualStyle.CURSE_TATTOOS, cape = true
        ),
        // 181. Anti-Spiral
        fighter(
            "anti_spiral_void", "Anti-Spiral", "Multiverse Collective", "Gurren Lagann",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            445, 17, 1.36f, 1.92f, 0xFF09090B, 0xFFF8FAFC, 0xFFA855F7, 0x00000000,
            WeaponVisualType.PSYCHIC_DEBRIS,
            PassiveArchetype.FROST_GRAVITY_SLOW, "Multiverse Labyrinth", "Entropic despair slows rival speed by 22% on every contact.",
            UltimateArchetype.METEOR_NUKE, "Infinity Big Bang Storm", 6.0f, 9.5f, 1.82f,
            "Hurls colliding galaxies for -30 instant cosmic damage!",
            "Know absolute despair, Spiral beings!", 135_000, EyeVisualStyle.HOLLOW_MASK, CrownVisualStyle.HALO_CROWN
        ),
        // 182. Spike Spiegel
        fighter(
            "spike_bebop", "Spike", "Jeet Kune Do Bounty Hunter", "Cowboy Bebop",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.HEAVY,
            365, 15, 1.48f, 1.22f, 0xFF1E3A8A, 0xFFFACC15, 0xFF38BDF8, 0xFF14532D,
            WeaponVisualType.HELLSING_PISTOLS,
            PassiveArchetype.ELASTIC_MOMENTUM, "Flow Like Water", "Fluid Jeet Kune Do rebounds gain +20% speed & +50% bank damage.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Swordfish II Plasma Cannon", 5.0f, 8.0f, 1.72f,
            "Lines up Jericho 941 & Swordfish plasma for a 2.4x precision shot!",
            "Bang.", 65_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SAIYAN_WILD
        ),
        // 183. Vicious
        fighter(
            "vicious_syndicate", "Vicious", "Red Dragon Elder", "Cowboy Bebop",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            365, 16, 1.46f, 1.26f, 0xFF09090B, 0xFF475569, 0xFFEF4444, 0xFFCBD5E1,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.EXECUTE_SHRINE, "Merciless Syndicate Blade", "Deals +30% execution damage when rival falls below 50% HP.",
            UltimateArchetype.HYPER_AWAKEN, "Red-Eye Katana Frenzy", 5.5f, 8.5f, 1.68f,
            "Boosts slash velocity +36% with ruthless Red-Eye focus.",
            "Only I can take your life, Spike.", 54_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.FLOWING_MANE, cape = true
        ),
        // 184. Mugen
        fighter(
            "mugen_champloo", "Mugen", "Breakdance Renegade", "Samurai Champloo",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            370, 15, 1.54f, 1.24f, 0xFFDC2626, 0xFF1E293B, 0xFFF97316, 0xFF09090B,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.ELASTIC_MOMENTUM, "Champuru Capoeira Spin", "Unpredictable wall flips grant +20% burst & +50% bank crits!",
            UltimateArchetype.HYPER_AWAKEN, "Rooster Footwork Cyclone", 5.5f, 8.0f, 1.68f,
            "Whirls typhoon sword kicks at +38% speed!",
            "Don't bother guessing my style—I make it up as I cut!", 52_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SAIYAN_WILD, FaceMarkVisualStyle.CURSE_TATTOOS
        ),
        // 185. Jin (Samurai Champloo)
        fighter(
            "jin_ronin", "Jin", "Mujushin-Ryu Master", "Samurai Champloo",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.SLASH,
            375, 15, 1.42f, 1.32f, 0xFF1E3A8A, 0xFFF8FAFC, 0xFF38BDF8, 0xFF09090B,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.BLACK_FLASH_CRIT, "Night-Owl Iaijutsu", "Calm counter-draws land a guaranteed 1.45x crit every 3rd clash.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "sacrifice Counter Stroke", 5.0f, 8.0f, 1.74f,
            "Invites a graze to deliver a fatal 2.45x iai counter-cut!",
            "Stillness before the single stroke.", 52_000, EyeVisualStyle.STAR_SHADES, CrownVisualStyle.FLOWING_MANE
        ),
        // 186. David Martinez
        fighter(
            "david_sandevistan", "David", "Edgerunner Legend", "Cyberpunk: Edgerunners",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.ELEC,
            360, 15, 1.58f, 1.22f, 0xFFFACC15, 0xFF09090B, 0xFF22D3EE, 0xFF451A03,
            WeaponVisualType.CYBER_CANNON,
            PassiveArchetype.LIGHTNING_PARALYZE, "Military Sandevistan", "Neural reflex dilation micro-stuns rivals on high-speed impact.",
            UltimateArchetype.TIME_STOP, "Apogee Sandevistan Overclock", 5.0f, 8.5f, 1.70f,
            "Dilates time to freeze the rival 1.65s in neon afterimages!",
            "I'm built different—taking you to the moon!", 72_000, EyeVisualStyle.CYBER_VISOR, CrownVisualStyle.SLICK_VILLAIN, FaceMarkVisualStyle.CYBER_SEAMS
        ),
        // 187. Adam Smasher
        fighter(
            "adam_smasher", "Adam Smasher", "Arasaka Full-Borg", "Cyberpunk: Edgerunners",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            450, 17, 1.32f, 2.04f, 0xFF18181B, 0xFFDC2626, 0xFFEF4444, 0x00000000,
            WeaponVisualType.CYBER_CANNON,
            PassiveArchetype.TITAN_JUGGERNAUT, "96% Dragoon Chassis", "Full-borg armor shrugs off knockback 35% and crushes flesh (+20% DMG).",
            UltimateArchetype.METEOR_NUKE, "Micro-Missile Salvo", 5.5f, 9.0f, 1.76f,
            "Rains shoulder-pod missiles (-28 instant HP + blast crater)!",
            "None leave the slaughterhouse—not alive!", 86_000, EyeVisualStyle.CYBER_VISOR, CrownVisualStyle.KNIGHT_HELM, FaceMarkVisualStyle.CYBER_SEAMS
        ),
        // 188. Lucyna Kushinada
        fighter(
            "lucy_monowire", "Lucy", "Deep-Dive Netrunner", "Cyberpunk: Edgerunners",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.ELEC,
            345, 15, 1.50f, 1.12f, 0xFFF8FAFC, 0xFFEC4899, 0xFF22D3EE, 0xFFE2E8F0,
            WeaponVisualType.CHAKRAM_RINGS,
            PassiveArchetype.ANTI_MAGIC_DRAIN, "Short-Circuit Quickhack", "ICE-breaker monowire drains 11% of rival Ultimate charge per hit.",
            UltimateArchetype.DOMAIN_STUN, "System Collapse Daemons", 5.0f, 8.0f, 1.66f,
            "Uploads neural freeze quickhack (1.6s stun) and thermal wire whip!",
            "Stay out of my subnet.", 58_000, EyeVisualStyle.ESPER_PSYCHIC, CrownVisualStyle.SLICK_VILLAIN, FaceMarkVisualStyle.BLOOD_STRIPE
        ),
        // 189. Motoko Kusanagi
        fighter(
            "motoko_major", "Major Motoko", "Section 9 Cyborg", "Ghost in the Shell",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.ELEC,
            375, 15, 1.48f, 1.32f, 0xFF312E81, 0xFF18181B, 0xFF22D3EE, 0xFF4C1D95,
            WeaponVisualType.HELLSING_PISTOLS,
            PassiveArchetype.INFINITY_BARRIER, "Thermoptic Camouflage", "Optical cloak deflects direct hits to reduce incoming damage by 22%.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Ghost Dive Assault", 5.0f, 8.0f, 1.70f,
            "Uncloaks point-blank for a 2.4x prosthetic tactical strike!",
            "My ghost whispers when to pull the trigger.", 62_000, EyeVisualStyle.CYBER_VISOR, CrownVisualStyle.SLICK_VILLAIN
        ),
        // 190. Shogo Makishima
        fighter(
            "makishima_asymptomatic", "Makishima", "Criminally Asymptomatic", "Psycho-Pass",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            360, 15, 1.46f, 1.20f, 0xFFF8FAFC, 0xFF1E293B, 0xFF38BDF8, 0xFFF8FAFC,
            WeaponVisualType.SHADOW_DAGGERS,
            PassiveArchetype.CRIMSON_BLEED, "Straight-Razor Precision", "Crime Coefficient 0.0—slices carotid seams for 4 Bleed ticks.",
            UltimateArchetype.ULT_NULLIFY, "Sibyl Blindspot", 5.0f, 8.5f, 1.66f,
            "Nullifies rival Awakening judgment and strikes unseen!",
            "Sibyl's Dominators cannot judge a truly free soul.", 54_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.FLOWING_MANE
        ),
        // 191. Giorno Giovanna (GER)
        fighter(
            "giorno_requiem", "Giorno", "Gold Experience Requiem", "JoJo Part 5: Golden Wind",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            400, 16, 1.44f, 1.46f, 0xFFEC4899, 0xFFFACC15, 0xFFFDE047, 0xFFFDE047,
            WeaponVisualType.STAR_FISTS,
            PassiveArchetype.VECTOR_REFLECT, "Life-Giver Reflection", "Damage dealt to Giorno's creations reflects 26% straight back!",
            UltimateArchetype.ULT_NULLIFY, "Return to Zero", 5.5f, 9.5f, 1.76f,
            "Reverts active rival Awakening back to ZERO and unleashes 7-page Muda!",
            "You will never arrive at the truth—Requiem!", 120_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.ROYAL_CROWN
        ),
        // 192. Diavolo (King Crimson)
        fighter(
            "diavolo_crimson", "Diavolo", "Passione Boss", "JoJo Part 5: Golden Wind",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            380, 16, 1.48f, 1.38f, 0xFFBE123C, 0xFF18181B, 0xFFF43F5E, 0xFFEC4899,
            WeaponVisualType.STAR_FISTS,
            PassiveArchetype.EXECUTE_SHRINE, "Epitaph Foreknowledge", "Skips to the fatal frame—dealing +30% damage below 50% rival HP.",
            UltimateArchetype.TIME_STOP, "King Crimson: Time Erase", 5.0f, 8.5f, 1.72f,
            "Erases 1.65s of rival time—leaving only the donut result!",
            "Only the results remain in this world!", 76_000, EyeVisualStyle.HYPNOTIC_RINGS, CrownVisualStyle.FLOWING_MANE, FaceMarkVisualStyle.CURSE_TATTOOS
        ),
        // 193. Yoshikage Kira (Killer Queen)
        fighter(
            "kira_killer_queen", "Kira", "Morioh Serial Bomber", "JoJo Part 4: Diamond is Unbreakable",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.FIRE,
            375, 15, 1.36f, 1.34f, 0xFF7E22CE, 0xFFFACC15, 0xFFE879F9, 0xFFFDE047,
            WeaponVisualType.KI_HALO,
            PassiveArchetype.SCORCH_BURN, "First Bomb Contact", "Turns touched surfaces into contact bombs with lingering burn.",
            UltimateArchetype.METEOR_NUKE, "Bites the Dust", 5.0f, 9.0f, 1.70f,
            "Clicks the thumb switch for a -27 HP temporal detonation!",
            "Killer Queen has already touched that wall.", 68_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SLICK_VILLAIN
        ),
        // 194. Enrico Pucci (Made in Heaven)
        fighter(
            "pucci_heaven", "Pucci", "Universe Accelerator", "JoJo Part 6: Stone Ocean",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.MAGIC,
            375, 15, 1.62f, 1.20f, 0xFF1E1B4B, 0xFFFACC15, 0xFFF8FAFC, 0xFFF8FAFC,
            WeaponVisualType.CHAKRAM_RINGS,
            PassiveArchetype.ADAPTIVE_EVOLUTION, "Gravitational Acceleration", "Time speeds up per collision (+3 attack & +3% defense, max 8)!",
            UltimateArchetype.HYPER_AWAKEN, "Made in Heaven Singularity", 6.0f, 8.5f, 1.72f,
            "Accelerates to infinite cosmic velocity (+42% speed, 1.72x slice)!",
            "Time is accelerating—Made in Heaven!", 89_000, EyeVisualStyle.HYPNOTIC_RINGS, CrownVisualStyle.HALO_CROWN, FaceMarkVisualStyle.STAR_TEAR, cape = true
        ),
        // 195. Joseph Joestar
        fighter(
            "joseph_hamon", "Joseph", "Hamon Trickster", "JoJo Part 2: Battle Tendency",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.ELEC,
            375, 15, 1.44f, 1.34f, 0xFF15803D, 0xFFFACC15, 0xFFFDE047, 0xFF451A03,
            WeaponVisualType.ALLOY_YOYOS,
            PassiveArchetype.ELASTIC_MOMENTUM, "Clacker Volley Bank Shot", "Hamon clackers ricochet with +20% speed & +50% bank damage!",
            UltimateArchetype.TIME_STOP, "Your Next Line Is...!", 5.0f, 8.0f, 1.64f,
            "Predicts rival's exact line—stunning them in shock for 1.6s!",
            "Your next line is: 'How did that bank shot hit me?!'", 48_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHINOBI_HEADBAND
        ),
        // 196. Kars (Ultimate Lifeform)
        fighter(
            "kars_ultimate", "Kars", "Ultimate Lifeform", "JoJo Part 2: Battle Tendency",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.SLASH,
            430, 16, 1.46f, 1.66f, 0xFF581C87, 0xFFFACC15, 0xFFF472B6, 0xFF4C1D95,
            WeaponVisualType.TITAN_BLADES,
            PassiveArchetype.REGEN_IMMORTAL, "All Earth's DNA", "Immortal Pillar Man cells heal +2% Max HP every 3rd bounce.",
            UltimateArchetype.HYPER_AWAKEN, "Red Stone of Aja Dawn", 5.5f, 9.0f, 1.74f,
            "Conquers the sun with prism Light Blades (+36% speed, 1.74x slash)!",
            "I have finally conquered the sun!", 79_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.FLOWING_MANE, cape = true
        ),
        // 197. Baki Hanma
        fighter(
            "baki_demon_brain", "Baki", "Underground Champion", "Baki Hanma",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.HEAVY,
            395, 16, 1.54f, 1.44f, 0xFFDC2626, 0xFF18181B, 0xFFEF4444, 0xFF78350F,
            WeaponVisualType.SERIOUS_GLOVE,
            PassiveArchetype.BLACK_FLASH_CRIT, "0.5-Second Unconscious Jab", "Cockroach Dash lands a 1.45x Triceratops Fist crit every 3rd hit!",
            UltimateArchetype.HYPER_AWAKEN, "Demon Back Awakening", 5.5f, 8.0f, 1.72f,
            "Flexes Hanma Demon Back (+38% speed, 1.72x martial arts fury)!",
            "Accelerate from 0 to top speed in 0.01 seconds!", 62_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.FLOWING_MANE, FaceMarkVisualStyle.CROSS_SCAR
        ),
        // 198. Yujiro Hanma
        fighter(
            "yujiro_ogre", "Yujiro", "Strongest Creature on Earth", "Baki Hanma",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.ATTACKER, ElementTag.HEAVY,
            455, 18, 1.42f, 2.05f, 0xFF7F1D1D, 0xFF09090B, 0xFFEF4444, 0xFF991B1B,
            WeaponVisualType.SERIOUS_GLOVE,
            PassiveArchetype.TITAN_JUGGERNAUT, "Ogre's Supremacy", "Stops earthquakes with a punch—35% knockback poise & +20% crush DMG.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Demon Back: Dress", 6.0f, 9.5f, 1.82f,
            "Unleashes pure Hanma violence for a 2.5x Ogre smash!",
            "Strength that bends nations—that is the Ogre!", 140_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.SAIYAN_WILD
        ),
        // 199. Ohma Tokita
        fighter(
            "ohma_advance", "Ohma", "The Ashura", "Kengan Ashura",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.HEAVY,
            390, 16, 1.46f, 1.45f, 0xFF991B1B, 0xFF18181B, 0xFFF43F5E, 0xFF09090B,
            WeaponVisualType.SERIOUS_GLOVE,
            PassiveArchetype.VECTOR_REFLECT, "Niko Style: Demonsbane", "Formless counter redirects 26% of rival's own impact force back!",
            UltimateArchetype.HYPER_AWAKEN, "Possessing Spirit: Advance", 5.5f, 8.5f, 1.70f,
            "Overclocks heartbeat for +36% torque speed and Ironbreaker blows!",
            "Wanna fight, too? Niko Style—Demonsbane!", 56_000, EyeVisualStyle.ESPER_PSYCHIC, CrownVisualStyle.FLOWING_MANE
        ),
        // 200. Makunouchi Ippo
        fighter(
            "ippo_dempsey", "Ippo", "Wind God Boxer", "Hajime no Ippo",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.HEAVY,
            400, 15, 1.40f, 1.56f, 0xFF1D4ED8, 0xFFF8FAFC, 0xFF38BDF8, 0xFF09090B,
            WeaponVisualType.SERIOUS_GLOVE,
            PassiveArchetype.BERSERKER_RAGE, " Courageous Gazelle Step", "Digs toes into the canvas—up to +45% liver-blow power at low HP!",
            UltimateArchetype.HYPER_AWAKEN, "New Freeform Dempsey Roll", 5.5f, 8.0f, 1.72f,
            "Weaves figure-eight jet-engine hooks (+35% speed, 1.72x smash)!",
            "Jet engines roaring—DEMPSEY ROLL!!", 55_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHONEN_SPIKY
        ),
        // 201. Yoichi Isagi
        fighter(
            "isagi_metavision", "Isagi", "Blue Lock Egoist", "Blue Lock",
            CharacterAlignment.PROTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.MAGIC,
            350, 15, 1.52f, 1.15f, 0xFF1D4ED8, 0xFF09090B, 0xFF22D3EE, 0xFF0F172A,
            WeaponVisualType.KI_HALO,
            PassiveArchetype.ADAPTIVE_EVOLUTION, "Metavision Puzzle", "Devours the field—gaining +3 attack & +3% defense per collision!",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Two-Stage Direct Volley", 5.0f, 8.0f, 1.74f,
            "Assembles the final puzzle piece for a 2.45x Direct Shoot!",
            "I'll devour everyone on this court—Direct Shoot!", 46_000, EyeVisualStyle.HYPNOTIC_RINGS, CrownVisualStyle.SHONEN_SPIKY
        ),
        // 202. Rin Itoshi
        fighter(
            "rin_destroyer", "Rin Itoshi", "Flow State Striker", "Blue Lock",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            355, 16, 1.54f, 1.18f, 0xFF0F766E, 0xFF09090B, 0xFF2DD4BF, 0xFF042F2E,
            WeaponVisualType.CHAKRAM_RINGS,
            PassiveArchetype.ELASTIC_MOMENTUM, "Trivela Curve Shot", "Curved wall rebounds gain +20% velocity and +50% bank damage!",
            UltimateArchetype.HYPER_AWAKEN, "Hideous Destroyer Flow", 5.5f, 8.0f, 1.70f,
            "Crushes rivals at their strongest point (+36% speed, 1.70x strike)!",
            "Lukewarm... I'll crush you utterly!", 48_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.SLICK_VILLAIN
        ),
        // 203. Madoka Kaname
        fighter(
            "madoka_law_of_cycles", "Madoka", "Law of Cycles Goddess", "Puella Magi Madoka Magica",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            405, 16, 1.44f, 1.42f, 0xFFF472B6, 0xFFF8FAFC, 0xFFFBCFE8, 0xFFF9A8D4,
            WeaponVisualType.SPIRIT_BOW,
            PassiveArchetype.REGEN_IMMORTAL, "Hope Beyond Despair", "Heals +2% Max HP every 3rd bounce and purifies curses.",
            UltimateArchetype.METEOR_NUKE, "Infinite Arrow Rain", 5.5f, 9.0f, 1.78f,
            "Rewrites universe rules with cosmic pink starlight (-29 instant HP)!",
            "If someone tells me it's a mistake to have hope, I'll tell them they're wrong!", 108_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.TWIN_TAILS, cape = true
        ),
        // 204. Homura Akemi
        fighter(
            "homura_time_shield", "Homura", "Time-Loop Rebel", "Puella Magi Madoka Magica",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            375, 15, 1.44f, 1.30f, 0xFF1E1B4B, 0xFF6D28D9, 0xFFA855F7, 0xFF09090B,
            WeaponVisualType.HELLSING_PISTOLS,
            PassiveArchetype.ARCANE_OVERCHARGE, "Sandglass Buckler", "Rewound timelines build Ultimate 35% faster.",
            UltimateArchetype.TIME_STOP, "Timestop Ordnance Barrage", 5.0f, 8.5f, 1.70f,
            "Halts time for 1.65s to surround the rival in heavy artillery!",
            "I will repeat this maze as many times as it takes.", 78_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.FLOWING_MANE, cape = true
        ),
        // 205. Kiss-Shot Acerola-Orion Heart-Under-Blade (Shinobu)
        fighter(
            "kissshot_shinobu", "Kiss-Shot", "Iron-Blooded Vampire", "Monogatari Series",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.ATTACKER, ElementTag.SLASH,
            415, 17, 1.46f, 1.56f, 0xFFEAB308, 0xFF991B1B, 0xFFFACC15, 0xFFFDE047,
            WeaponVisualType.NICHIRIN_KATANA,
            PassiveArchetype.VAMPIRIC_SIPHON, "King of Apparitions", "Kokorowatari drains energy & heals 28% of slash damage dealt.",
            UltimateArchetype.SOUL_DEVOUR, "Heart-Under-Blade Feast", 5.5f, 9.0f, 1.76f,
            "Reclaims full adult vampire majesty, draining rival HP & meter!",
            "Ka-ka! Kneel before the Iron-Blooded, Hot-Blooded Vampire!", 88_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.FLOWING_MANE, cape = true
        ),
        // 206. Akira Fudo / Devilman
        fighter(
            "akira_devilman", "Devilman", "Amon Human Heart", "Devilman Crybaby",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.FIRE,
            405, 16, 1.48f, 1.54f, 0xFF18181B, 0xFF991B1B, 0xFFEF4444, 0xFF09090B,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.BERSERKER_RAGE, "Tears of Amon", "Human empathy + demon fury scales damage up to +45% at low HP.",
            UltimateArchetype.HYPER_AWAKEN, "Hellfire Wing Slash", 5.5f, 8.5f, 1.74f,
            "Unfurls bat wings in crimson lightning (+38% speed, 1.74x claws)!",
            "I am DEVILMAN!!", 68_000, EyeVisualStyle.DEMON_SLIT, CrownVisualStyle.DEMON_HORNS, FaceMarkVisualStyle.BLOOD_STRIPE, cape = true
        ),
        // 207. Ryo Asuka / Satan
        fighter(
            "ryo_satan", "Ryo", "Twelve-Winged Fallen", "Devilman Crybaby",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            420, 17, 1.44f, 1.58f, 0xFFF8FAFC, 0xFFFACC15, 0xFF38BDF8, 0xFFFEF08A,
            WeaponVisualType.KI_HALO,
            PassiveArchetype.INFINITY_BARRIER, "Seraphim Light Halo", "Twelve angelic wings deflect 22% of incoming collision force.",
            UltimateArchetype.METEOR_NUKE, "Genesis Judgment", 5.5f, 9.5f, 1.78f,
            "Splits seas and court with blinding seraphim beams (-29 HP)!",
            "Love does not exist—yet why am I crying?", 92_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.HALO_CROWN, cape = true
        ),
        // 208. Shinichi Izumi & Migi
        fighter(
            "shinichi_migi", "Shinichi", "Right-Hand Parasyte", "Parasyte: The Maxim",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            370, 15, 1.48f, 1.28f, 0xFF1E293B, 0xFFDC2626, 0xFF4ADE80, 0xFF09090B,
            WeaponVisualType.CURSED_CLEAVE,
            PassiveArchetype.VECTOR_REFLECT, "Migi Auto-Parry", "Autonomous right-hand blade parries & reflects 26% impact damage.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Heart-Stone Sling", 5.0f, 8.0f, 1.70f,
            "Synchronizes Migi & cold resolve for a 2.4x lethal counter-slice!",
            "Migi, handle the defense.", 52_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SLICK_VILLAIN
        ),
        // 209. Shichika Yasuri
        fighter(
            "shichika_kyotoryu", "Shichika", "Swordsman Without a Sword", "Katanagatari",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.ATTACKER, ElementTag.SLASH,
            405, 17, 1.46f, 1.52f, 0xFF15803D, 0xFFDC2626, 0xFFF43F5E, 0xFF09090B,
            WeaponVisualType.STAR_FISTS,
            PassiveArchetype.ANTI_MAGIC_DRAIN, "Kyotoryu Blade-Breaker", "Shatters rival weapons—draining 11% Ultimate meter on contact!",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Seven-Blossoms Rending", 5.5f, 8.5f, 1.78f,
            "By now, you'll be torn to pieces—2.5x bare-handed sword combo!",
            "However, by that point you'll already be torn to pieces.", 84_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SAIYAN_WILD, FaceMarkVisualStyle.CURSE_TATTOOS
        ),
        // 210. Haruko Haruhara
        fighter(
            "haruko_flcl", "Haruko", "Vespa Rickenbacker Alien", "FLCL",
            CharacterAlignment.ANTAGONIST, AnimeTier.NICHE, CharacterClass.SPEEDSTER, ElementTag.ELEC,
            365, 16, 1.56f, 1.32f, 0xFFFACC15, 0xFFEC4899, 0xFF38BDF8, 0xFFF472B6,
            WeaponVisualType.GRIMOIRE_GREATSWORD,
            PassiveArchetype.SHOCKWAVE_KNOCKBACK, "Rickenbacker 4001 Swing", "Bass-guitar home-run swings blast rivals +38% across the arena!",
            UltimateArchetype.HYPER_AWAKEN, "Atomsk Pirate King", 5.5f, 8.0f, 1.72f,
            "Channels Atomsk's crimson guitar power (+40% speed, 1.72x smash)!",
            "Nothing amazing happens here unless you swing the bat!", 74_000, EyeVisualStyle.STAR_SHADES, CrownVisualStyle.SHINOBI_HEADBAND
        ),
        // 211. Sweet JP (Redline)
        fighter(
            "sweet_jp_transam", "Sweet JP", "TransAm20000 Racer", "Redline",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.SPEEDSTER, ElementTag.FIRE,
            360, 15, 1.64f, 1.18f, 0xFF18181B, 0xFFFACC15, 0xFFF97316, 0xFF09090B,
            WeaponVisualType.SPIRAL_DRILL,
            PassiveArchetype.ELASTIC_MOMENTUM, "Steamlight Nitro Boost", "Every wall drift adds +20% nitro speed and +50% bank impact!",
            UltimateArchetype.HYPER_AWAKEN, "Platinum Nitro Finish", 5.5f, 7.5f, 1.70f,
            "Pops the final Platinum Nitro capsule for top speed in the galaxy!",
            "No weapons—just pure horsepower!", 72_000, EyeVisualStyle.STAR_SHADES, CrownVisualStyle.SLICK_VILLAIN
        ),
        // 212. Galo Thymos & Lio Fotia (Promare)
        fighter(
            "lio_promare", "Lio Fotia", "Mad Burnish Boss", "Promare",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.SPEEDSTER, ElementTag.FIRE,
            365, 15, 1.52f, 1.22f, 0xFF09090B, 0xFFEC4899, 0xFF22D3EE, 0xFFCCFBF1,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.SCORCH_BURN, "Burnish Triangle Flame", "Geometric neon magenta-cyan flames scorch rivals on contact.",
            UltimateArchetype.HAZARD_STORM, "Deus X Machina Promare", 5.5f, 8.0f, 1.70f,
            "Paints the court in roaring geometric Burnish fire dragons!",
            "If we don't burn, we can't live!", 66_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.FLOWING_MANE, cape = true
        ),
        // 213. Vincent Law / Ergo Proxy
        fighter(
            "ergo_proxy_vincent", "Ergo Proxy", "Agent of Death", "Ergo Proxy",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            405, 16, 1.40f, 1.52f, 0xFF18181B, 0xFF334155, 0xFF60A5FA, 0xFF09090B,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.REGEN_IMMORTAL, "Amrita Cell Immortality", "Creator Proxy cells heal +2% Max HP every 3rd wall bounce.",
            UltimateArchetype.GRAVITY_CRUSH, "Pulse of the Awakening", 5.5f, 9.0f, 1.72f,
            "Unmasks Porcelain Proxy face (-27 instant HP + shadow crush)!",
            "I am Ergo Proxy—emissary of reckoning.", 79_000, EyeVisualStyle.HOLLOW_MASK, CrownVisualStyle.SLICK_VILLAIN, cape = true
        ),
        // 214. Lain Iwakura
        fighter(
            "lain_wired", "Lain", "Omnipresent Wired Entity", "Serial Experiments Lain",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.CONTROL_MAGIC, ElementTag.ELEC,
            375, 15, 1.42f, 1.24f, 0xFF1E1B4B, 0xFFBE185D, 0xFF22D3EE, 0xFF78350F,
            WeaponVisualType.PSYCHIC_DEBRIS,
            PassiveArchetype.ARCANE_OVERCHARGE, "Protocol 7 Hum", "Schumann resonance charges Ultimate 35% faster across the Wired.",
            UltimateArchetype.ULT_NULLIFY, "Reset Reality Cache", 5.5f, 8.5f, 1.70f,
            "Deletes active rival Awakening from memory and overloads circuits!",
            "Present day, present time—let's all love Lain.", 95_000, EyeVisualStyle.ESPER_PSYCHIC, CrownVisualStyle.WITCH_HOOD
        ),
        // 215. Ichise (Texhnolyze)
        fighter(
            "ichise_texhnolyze", "Ichise", "Lux Underground Prizefighter", "Texhnolyze",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.ATTACKER, ElementTag.HEAVY,
            405, 16, 1.34f, 1.62f, 0xFF27272A, 0xFF52525B, 0xFF94A3B8, 0xFF09090B,
            WeaponVisualType.SERIOUS_GLOVE,
            PassiveArchetype.BERSERKER_RAGE, "Raffia Prosthetic Rage", "Animalistic survival drive adds up to +45% prosthetic smash power.",
            UltimateArchetype.SERIOUS_ONE_PUNCH, "Obelisk Smash", 5.5f, 8.5f, 1.72f,
            "Drives Texhnolyze limb into a 2.4x underground knockout!",
            "I won't rot in the dark!", 64_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SHONEN_SPIKY, FaceMarkVisualStyle.CYBER_SEAMS
        ),
        // 216. Casshern
        fighter(
            "casshern_sins", "Casshern", "Immortal Ruin Neoroid", "Casshern Sins",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.SPEEDSTER, ElementTag.ELEC,
            395, 16, 1.52f, 1.38f, 0xFFF8FAFC, 0xFF1D4ED8, 0xFF38BDF8, 0xFFE2E8F0,
            WeaponVisualType.DRAGON_CLAW,
            PassiveArchetype.REGEN_IMMORTAL, "Undying Neoroid Frame", "Reconstructs shattered alloy (+2% HP every 3rd bounce).",
            UltimateArchetype.HYPER_AWAKEN, "Pulsar Berserk Blitz", 5.5f, 8.5f, 1.72f,
            "Drops visor mask for supersonic white-blue blitz (+38% speed)!",
            "If you seek death, face Casshern!", 71_000, EyeVisualStyle.CYBER_VISOR, CrownVisualStyle.MECHA_VFIN, cape = true
        ),
        // 217. Utena Tenjou
        fighter(
            "utena_rose", "Utena", "Revolutionary Duelist", "Revolutionary Girl Utena",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            370, 15, 1.48f, 1.24f, 0xFF09090B, 0xFFEC4899, 0xFFF43F5E, 0xFFF472B6,
            WeaponVisualType.EXCALIBUR_BLADE,
            PassiveArchetype.BLACK_FLASH_CRIT, "Rose Signet Parry", "Every 3rd duel clash blooms a 1.45x Sword of Dios critical!",
            UltimateArchetype.ULT_NULLIFY, "Revolutionize the World", 5.5f, 8.5f, 1.70f,
            "Shatters rival's Rose Bride seal (nullifies active Ult) and strikes true!",
            "Grant me the power to bring the world revolution!", 69_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.FLOWING_MANE, cape = true
        ),
        // 218. Claire Stanfield / Vino (Baccano!)
        fighter(
            "vino_rail_tracer", "Rail Tracer", "Flying Pussyfoot Conductor", "Baccano!",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            375, 16, 1.54f, 1.28f, 0xFF7F1D1D, 0xFF18181B, 0xFFEF4444, 0xFFDC2626,
            WeaponVisualType.SHADOW_DAGGERS,
            PassiveArchetype.ELASTIC_MOMENTUM, "Acrobatic Rail Swing", "Bounces off walls like train bogies (+20% speed & +50% bank DMG)!",
            UltimateArchetype.HYPER_AWAKEN, "Solipsist Execution", 5.5f, 8.0f, 1.70f,
            "The world is my dream—unstoppable crimson assassin dance!",
            "The Rail Tracer is coming down the car!", 62_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.SLICK_VILLAIN, FaceMarkVisualStyle.BLOOD_STRIPE
        ),
        // 219. Celty Sturluson
        fighter(
            "celty_dullahan", "Celty", "Black Rider Dullahan", "Durarara!!",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.SPEEDSTER, ElementTag.SLASH,
            385, 15, 1.50f, 1.34f, 0xFF09090B, 0xFFFACC15, 0xFFA855F7, 0x00000000,
            WeaponVisualType.CRESCENT_SCYTHE,
            PassiveArchetype.FROST_GRAVITY_SLOW, "Shadow Webbing", "Ikebukuro shadow tendrils slow rival velocity 22% on contact.",
            UltimateArchetype.HAZARD_STORM, "Shooter Cycle Drift", 5.5f, 8.0f, 1.66f,
            "Carves black shadow scythe trails across every wall rebound!",
            "[Typing on PDA: Get out of my road.]", 56_000, EyeVisualStyle.CYBER_VISOR, CrownVisualStyle.KNIGHT_HELM, cape = true
        ),
        // 220. Shizuo Heiwajima
        fighter(
            "shizuo_vending", "Shizuo", "Strongest in Ikebukuro", "Durarara!!",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.HEAVY,
            435, 17, 1.32f, 1.92f, 0xFF18181B, 0xFFF8FAFC, 0xFF38BDF8, 0xFFFDE047,
            WeaponVisualType.DEMON_KANABO,
            PassiveArchetype.SHOCKWAVE_KNOCKBACK, "Street-Sign Home Run", "Zero limiter muscles hurl rivals +38% across Ikebukuro!",
            UltimateArchetype.METEOR_NUKE, "Vending Machine Hurl", 5.5f, 8.5f, 1.74f,
            "Smashes a flying vending machine for -28 instant HP!",
            "IZAYAAAA!!", 58_000, EyeVisualStyle.STAR_SHADES, CrownVisualStyle.SLICK_VILLAIN
        ),
        // 221. Saichi Sugimoto
        fighter(
            "sugimoto_immortal", "Sugimoto", "Immortal Sugimoto", "Golden Kamuy",
            CharacterAlignment.PROTAGONIST, AnimeTier.CULT, CharacterClass.ATTACKER, ElementTag.SLASH,
            420, 15, 1.36f, 1.68f, 0xFF1E3A8A, 0xFF991B1B, 0xFFFACC15, 0xFF09090B,
            WeaponVisualType.ALCHEMY_SPEAR,
            PassiveArchetype.BERSERKER_RAGE, "203 Hill Survivor", "Refuses to die—gaining up to +45% Type-30 bayonet ferocity at low HP!",
            UltimateArchetype.HEAL_REFORGE, "I AM IMMORTAL SUGIMOTO!!", 5.5f, 8.0f, 1.68f,
            "Roars back with +58 HP second wind and relentless bayonet thrusts!",
            "I AM IMMORTAL SUGIMOTO!!", 54_000, EyeVisualStyle.FIERCE_SHONEN, CrownVisualStyle.PIRATE_TRICORNE, FaceMarkVisualStyle.CROSS_SCAR, cape = true
        ),
        // 222. Gesicht (Pluto)
        fighter(
            "gesicht_zeronium", "Gesicht", "Europol Zeronium Inspector", "Pluto",
            CharacterAlignment.PROTAGONIST, AnimeTier.NICHE, CharacterClass.ATTACKER, ElementTag.HEAVY,
            430, 16, 1.30f, 1.86f, 0xFF334155, 0xFF1E293B, 0xFF38BDF8, 0xFF94A3B8,
            WeaponVisualType.CYBER_CANNON,
            PassiveArchetype.INFINITY_BARRIER, "Zeronium Alloy Shell", "World's hardest robot alloy reduces incoming collision damage by 22%.",
            UltimateArchetype.METEOR_NUKE, "Zeronium Gas Shell", 5.5f, 9.0f, 1.72f,
            "Fires arm-mounted Zeronium cannon (-27 instant HP)!",
            "Hatred leaves nothing behind.", 67_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SLICK_VILLAIN, cape = true
        ),
        // 223. Johan Liebert
        fighter(
            "johan_monster", "Johan", "Nameless Monster", "Monster",
            CharacterAlignment.ANTAGONIST, AnimeTier.CULT, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            375, 15, 1.38f, 1.34f, 0xFF1E293B, 0xFFF8FAFC, 0xFF60A5FA, 0xFFFDE68A,
            WeaponVisualType.HELLSING_PISTOLS,
            PassiveArchetype.ANTI_MAGIC_DRAIN, "Nihilistic Whisper", "Erodes rival resolve—draining 11% Ultimate meter on every pass.",
            UltimateArchetype.TIME_STOP, "Point Here—Look At Me", 5.0f, 9.0f, 1.68f,
            "Paralyzes the rival in existential dread for 1.65s!",
            "The only thing humans are equal in... is death.", 85_000, EyeVisualStyle.SERENE_NOBLE, CrownVisualStyle.SLICK_VILLAIN
        ),
        // 224. Light Yagami / Kira & Ryuk
        fighter(
            "light_kira_note", "Light", "God of the New World", "Death Note",
            CharacterAlignment.ANTAGONIST, AnimeTier.POPULAR, CharacterClass.CONTROL_MAGIC, ElementTag.MAGIC,
            370, 15, 1.40f, 1.30f, 0xFF18181B, 0xFF991B1B, 0xFFEF4444, 0xFF78350F,
            WeaponVisualType.CRESCENT_SCYTHE,
            PassiveArchetype.EXECUTE_SHRINE, "40-Second Rule", "Deals +30% lethal cardiac damage once rival drops below 50% HP!",
            UltimateArchetype.SOUL_DEVOUR, "Shinigami Eyes Judgment", 5.5f, 9.0f, 1.74f,
            "Writes rival's name with Ryuk's shadow—draining HP & Ultimate gauge!",
            "Just according to keikaku—I am Justice!", 88_000, EyeVisualStyle.CRIMSON_SHARINGAN, CrownVisualStyle.SLICK_VILLAIN, cape = true
        )
    )
}
