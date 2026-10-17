package com.worldsreborn.ui.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** Journal (J) and multiverse map (M). Remappable in Controls under "Worlds Reborn". */
public final class InterfaceKeys {
    public static final String CATEGORY = "key.categories.worldsreborn";
    public static final KeyMapping JOURNAL = new KeyMapping("key.wrinterface.journal", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);
    public static final KeyMapping MAP = new KeyMapping("key.wrinterface.map", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY);

    private InterfaceKeys() {
    }
}
