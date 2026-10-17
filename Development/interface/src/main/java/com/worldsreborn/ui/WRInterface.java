package com.worldsreborn.ui;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;

/**
 * Worlds Reborn: Interface. Everything the player sees: title screen, HUD, journal, dialogue, cinematics, the
 * multiverse map and accessibility settings. All of it is client-side ({@code client.WRInterfaceClient}); on a
 * dedicated server this mod only exists so the dependency is satisfied.
 */
@Mod(WRInterface.MODID)
public final class WRInterface {
    public static final String MODID = "wrinterface";

    public WRInterface() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
