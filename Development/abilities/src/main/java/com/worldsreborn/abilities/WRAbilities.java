package com.worldsreborn.abilities;

import com.worldsreborn.core.model.AbilityDef;
import com.worldsreborn.core.model.WRContent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Worlds Reborn: Abilities. Unlocks, three equipped slots, cooldowns and server-validated activation.
 * The first milestone ships no story abilities (Steve doesn't consciously use his power yet); only a developer test
 * ability exists so the framework can be exercised with /wr abilities.
 */
@Mod(WRAbilities.MODID)
public final class WRAbilities {
    public static final String MODID = "wrabilities";

    /** Developer-only ability used to test slots, cooldowns and the HUD. Not part of the story. */
    public static final AbilityDef DEV_PULSE = WRContent.ABILITIES.register(new AbilityDef(
            MODID + ":dev_pulse", "ability.wrabilities.dev_pulse", "ability.wrabilities.dev_pulse.desc", 100, MODID));

    public WRAbilities(IEventBus modBus, ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, AbilitiesConfig.SPEC);
        modBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(SteveStrength::onDamage);
        NeoForge.EVENT_BUS.addListener(AbilityService::onAbilityUsed);
        NeoForge.EVENT_BUS.addListener(AbilityCommands::register);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(UseAbilityPayload.TYPE, UseAbilityPayload.CODEC,
                (p, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        AbilityService.use(player, p.slot());
                    }
                }));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
