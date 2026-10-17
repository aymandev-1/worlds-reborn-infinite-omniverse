package com.worldsreborn.marvel;

import com.worldsreborn.marvel.block.MarvelBlocks;
import com.worldsreborn.travel.TravelService;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Worlds Reborn: Marvel. MC-616 Steve's first crossover, into MU-616. This build contains the first playable
 * milestones: Act 1 (Doomsday), Scenes 1-5 (the village map to the TVA failing to locate MC-616) and Scenes 6-11
 * (the traveller in the records, the evacuation, the breach, the Watchers' sanctuary and Spider-Man's search) and
 * Scenes 12-16 (Sam Wilson, Bucky Barnes, Sue Storm and Cyclops as Earth's heroes respond).
 */
@Mod(WRMarvel.MODID)
public final class WRMarvel {
    public static final String MODID = "wrmarvel";

    public WRMarvel(IEventBus modBus, ModContainer container) {
        MarvelBlocks.BLOCKS.register(modBus);
        com.worldsreborn.marvel.item.MarvelItems.ITEMS.register(modBus);
        MarvelContent.register();

        TravelService.registerArrivalResolver(MarvelIds.DEST_NEW_YORK, (player, level) -> MarvelSites.nyArrival(level));
        TravelService.registerArrivalResolver(MarvelIds.DEST_TVA, (player, level) -> MarvelSites.tvaArrival(level));
        TravelService.registerArrivalResolver(MarvelIds.DEST_HOME, TravelService::overworldReturn);

        TravelService.registerArrivalResolver(MarvelIds.DEST_WATCHERS, (player, level) -> com.worldsreborn.marvel.doomsday.DoomsdaySites.watchersArrival(level));
        modBus.addListener(com.worldsreborn.marvel.doomsday.MarvelPayloads::register);

        NeoForge.EVENT_BUS.register(MarvelStory.class);
        NeoForge.EVENT_BUS.register(com.worldsreborn.marvel.doomsday.Doomsday.class);
        NeoForge.EVENT_BUS.register(com.worldsreborn.marvel.doomsday.Responders.class);
        NeoForge.EVENT_BUS.addListener(MarvelCommands::register);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
