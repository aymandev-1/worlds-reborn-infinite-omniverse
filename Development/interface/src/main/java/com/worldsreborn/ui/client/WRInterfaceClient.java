package com.worldsreborn.ui.client;

import com.worldsreborn.core.api.ClientHooks;
import com.worldsreborn.core.api.ClientState;
import com.worldsreborn.ui.WRInterface;
import com.worldsreborn.ui.client.hud.HudLayers;
import com.worldsreborn.ui.client.screen.JournalScreen;
import com.worldsreborn.ui.client.screen.MultiverseMapScreen;
import com.worldsreborn.ui.client.screen.WRTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

/** Client half of Worlds Reborn: Interface. */
@Mod(value = WRInterface.MODID, dist = Dist.CLIENT)
public final class WRInterfaceClient {
    public WRInterfaceClient(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, UiConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerKeys);
        modBus.addListener(this::registerLayers);

        NeoForge.EVENT_BUS.addListener(this::onScreenOpening);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onLoggingOut);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        ClientHooks.install(new InterfaceHooks());
        event.enqueueWork(() -> {
            if (UiConfig.SKIP_VANILLA_TUTORIAL.getAsBoolean()) {
                Minecraft.getInstance().getTutorial().setStep(TutorialSteps.NONE);
            }
        });
    }

    private void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(InterfaceKeys.JOURNAL);
        event.register(InterfaceKeys.MAP);
    }

    private void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, WRInterface.id("prompt"), HudLayers::prompt);
        event.registerAbove(VanillaGuiLayers.HOTBAR, WRInterface.id("abilities"), HudLayers::abilities);
        event.registerAboveAll(WRInterface.id("quest_tracker"), HudLayers::questTracker);
        event.registerAboveAll(WRInterface.id("toasts"), HudLayers::toasts);
        event.registerAboveAll(WRInterface.id("subtitles"), HudLayers::subtitles);
    }

    private void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() != null && event.getNewScreen().getClass() == TitleScreen.class
                && UiConfig.REPLACE_TITLE_SCREEN.getAsBoolean()) {
            event.setNewScreen(new WRTitleScreen());
        }
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        InterfaceHooks.openQueued();
        if (mc.screen != null) {
            return;
        }
        while (InterfaceKeys.JOURNAL.consumeClick()) {
            mc.setScreen(new JournalScreen());
        }
        while (InterfaceKeys.MAP.consumeClick()) {
            mc.setScreen(new MultiverseMapScreen());
        }
    }

    private void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientState.clear();
        InterfaceHooks.clearQueue();
        ToastQueue.clear();
        Subtitles.clear();
        Voice.stop();
    }
}
