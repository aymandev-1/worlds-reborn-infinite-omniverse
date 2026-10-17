package com.worldsreborn.abilities.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.worldsreborn.abilities.UseAbilityPayload;
import com.worldsreborn.abilities.WRAbilities;
import com.worldsreborn.core.api.ClientState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Ability keys (default Z, X, C; remappable in Controls). They do nothing until an ability is equipped. */
public final class AbilityKeys {
    public static final String CATEGORY = "key.categories.worldsreborn";
    public static final KeyMapping[] SLOTS = {
            new KeyMapping("key.wrabilities.slot1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY),
            new KeyMapping("key.wrabilities.slot2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY),
            new KeyMapping("key.wrabilities.slot3", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY),
    };

    private AbilityKeys() {
    }

    @EventBusSubscriber(modid = WRAbilities.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) {
            for (KeyMapping k : SLOTS) {
                event.register(k);
            }
        }
    }

    @EventBusSubscriber(modid = WRAbilities.MODID, value = Dist.CLIENT)
    public static final class Input {
        private Input() {
        }

        @SubscribeEvent
        public static void tick(ClientTickEvent.Post event) {
            if (Minecraft.getInstance().player == null || Minecraft.getInstance().screen != null) {
                return;
            }
            for (int i = 0; i < SLOTS.length; i++) {
                while (SLOTS[i].consumeClick()) {
                    if (ClientState.progress().equipped(i) != null) {
                        PacketDistributor.sendToServer(new UseAbilityPayload(i));
                    }
                }
            }
        }
    }
}
