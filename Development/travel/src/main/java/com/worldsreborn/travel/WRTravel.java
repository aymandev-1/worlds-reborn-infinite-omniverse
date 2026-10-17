package com.worldsreborn.travel;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Worlds Reborn: Travel. Destinations and unlocks, portal contact, transit (with a cinematic or loading transition),
 * safe arrival, return locations and respawn checkpoints. Transit is server-timed and resumes after a disconnect.
 * Vehicles and spacecraft are PLANNED and will plug into {@link TravelService#beginTransit}.
 */
@Mod(WRTravel.MODID)
public final class WRTravel {
    public static final String MODID = "wrtravel";
    /** Generic fade-to-black travel transition, rendered by the Interface module. */
    public static final String LOADING_CINEMATIC = MODID + ":loading";
    /** Player flag that allows travelling from the multiverse map (granted by campaigns). */
    public static final String FREE_TRAVEL_FLAG = MODID + ":free_travel";

    public WRTravel(IEventBus modBus, ModContainer container) {
        NeoForge.EVENT_BUS.register(TravelEvents.class);
        NeoForge.EVENT_BUS.addListener(TravelCommands::register);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
