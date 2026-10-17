package com.worldsreborn.abilities;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AbilitiesConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue PUNCH_BONUS = B
            .comment("Extra damage when Steve punches with an empty hand (vanilla fist = 1). He is the strongest there is.")
            .defineInRange("steve.punchBonus", 5.0, 0.0, 50.0);
    public static final ModConfigSpec.DoubleValue PUNCH_KNOCKBACK = B
            .comment("Extra knockback on an empty-hand punch")
            .defineInRange("steve.punchKnockback", 0.6, 0.0, 5.0);

    public static final ModConfigSpec SPEC = B.build();

    private AbilitiesConfig() {
    }
}
