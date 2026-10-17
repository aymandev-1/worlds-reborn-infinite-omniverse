package com.worldsreborn.core;

import com.mojang.logging.LogUtils;
import com.worldsreborn.core.command.CoreCommands;
import com.worldsreborn.core.model.UniverseId;
import com.worldsreborn.core.model.UniverseInfo;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.network.WRNetwork;
import com.worldsreborn.core.profile.ProfileEvents;
import com.worldsreborn.core.profile.WRAttachments;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Worlds Reborn: Core. Shared registries, the per-player save profile, networking, configuration and developer commands.
 * Every other Worlds Reborn module depends on this one.
 */
@Mod(WRCore.MODID)
public final class WRCore {
    public static final String MODID = "wrcore";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** MC-616: home universe of Worlds Reborn's playable Steve. Shared by every campaign. */
    public static final UniverseId MC_616 = UniverseId.parse("MC-616");

    public WRCore(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, CoreConfig.SPEC);
        WRAttachments.ATTACHMENTS.register(modBus);
        modBus.addListener(WRNetwork::register);
        modBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.addListener(CoreCommands::register);
        NeoForge.EVENT_BUS.register(ProfileEvents.class);

        WRContent.UNIVERSES.register(new UniverseInfo(MC_616, "universe.wrcore.mc616", "minecraft", false));
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            WRContent.freezeAll();
            LOGGER.info("[Worlds Reborn] Content frozen: {} universes, {} quests, {} dialogues, {} destinations, {} abilities",
                    WRContent.UNIVERSES.size(), WRContent.QUESTS.size(), WRContent.DIALOGUES.size(),
                    WRContent.DESTINATIONS.size(), WRContent.ABILITIES.size());
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    /** Debug log that only prints when enabled in the common config. */
    public static void debug(String message, Object... args) {
        if (CoreConfig.DEBUG_LOGGING.getAsBoolean()) {
            LOGGER.info("[Worlds Reborn/debug] " + message, args);
        }
    }
}
