package com.worldsreborn.ui.mixin;

import com.worldsreborn.ui.client.UiConfig;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * "Play as Steve": every player is drawn with Steve's classic skin (setting, on by default). While a story puts the
 * local player in another character's place, that character's skin is used instead.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @Unique
    private static final PlayerSkin WR_STEVE = new PlayerSkin(
            ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png"),
            null, null, null, PlayerSkin.Model.WIDE, true);

    @Unique
    private static String wr$perspectiveId;
    @Unique
    private static PlayerSkin wr$perspective;

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void worldsreborn$playAsSteve(CallbackInfoReturnable<PlayerSkin> cir) {
        // A story perspective (e.g. playing as Spider-Man in Marvel Scene 11) dresses the local player only.
        String skin = com.worldsreborn.core.api.ClientState.progress().perspectiveSkin();
        if (skin != null && (Object) this == net.minecraft.client.Minecraft.getInstance().player) {
            if (!skin.equals(wr$perspectiveId)) {
                ResourceLocation id = ResourceLocation.tryParse(skin);
                wr$perspectiveId = skin;
                wr$perspective = id == null ? null : new PlayerSkin(id, null, null, null, PlayerSkin.Model.WIDE, true);
            }
            if (wr$perspective != null) {
                cir.setReturnValue(wr$perspective);
                return;
            }
        }
        if (UiConfig.SPEC.isLoaded() && UiConfig.PLAY_AS_STEVE.getAsBoolean()) {
            cir.setReturnValue(WR_STEVE);
        }
    }
}
