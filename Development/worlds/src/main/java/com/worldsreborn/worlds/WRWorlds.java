package com.worldsreborn.worlds;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Worlds Reborn: Worlds. Tools campaigns use to build their places: a bounded set-piece builder, persistent site
 * records (so each area is built exactly once per world), surface and safe-spot finders. Campaign mods ship their own
 * dimensions as data (data/&lt;modid&gt;/dimension). Also enforces the spider-free creature rule.
 */
@Mod(WRWorlds.MODID)
public final class WRWorlds {
    public static final String MODID = "wrworlds";

    public WRWorlds(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, WorldsConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(WorldsCommands::register);
        NeoForge.EVENT_BUS.addListener(CreatureRules::onJoin);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
