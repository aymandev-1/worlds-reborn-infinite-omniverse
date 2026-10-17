package com.worldsreborn.story;

import com.worldsreborn.core.WRCore;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Worlds Reborn: Story. Quests and objectives, dialogue sessions, cinematic sessions and campaign progression.
 * Campaign mods (e.g. Marvel) define content and call {@link StoryService}; this module keeps the rules safe:
 * steps advance in order, rewards are claimed once, and interrupted sessions recover on the next login.
 */
@Mod(WRStory.MODID)
public final class WRStory {
    public static final String MODID = "wrstory";

    public WRStory(IEventBus modBus, ModContainer container) {
        NeoForge.EVENT_BUS.register(StoryEvents.class);
        NeoForge.EVENT_BUS.addListener(StoryCommands::register);
        WRCore.LOGGER.info("[Worlds Reborn] Story module loaded");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
