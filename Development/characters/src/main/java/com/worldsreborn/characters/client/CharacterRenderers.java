package com.worldsreborn.characters.client;

import com.worldsreborn.characters.CharacterDef;
import com.worldsreborn.characters.Characters;
import com.worldsreborn.characters.WRCharacters;
import com.worldsreborn.characters.entity.HostileNpcEntity;
import com.worldsreborn.characters.entity.StoryNpcEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = WRCharacters.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CharacterRenderers {
    /** Shown when a character id is unknown, so a missing skin is obvious instead of invisible. */
    public static final ResourceLocation MISSING = WRCharacters.id("textures/entity/missing_character.png");

    private CharacterRenderers() {
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(WRCharacters.STORY_NPC.get(), HumanoidNpcRenderer::new);
        event.registerEntityRenderer(WRCharacters.STORY_VILLAGER.get(), VillagerNpcRenderer::new);
        event.registerEntityRenderer(WRCharacters.HOSTILE_NPC.get(), HostileNpcRenderer::new);
    }

    static ResourceLocation texture(String characterId) {
        CharacterDef def = Characters.get(characterId);
        return def == null ? MISSING : def.texture();
    }

    static final class HumanoidNpcRenderer extends HumanoidMobRenderer<StoryNpcEntity, PlayerModel<StoryNpcEntity>> {
        HumanoidNpcRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        }

        @Override
        public ResourceLocation getTextureLocation(StoryNpcEntity entity) {
            return texture(entity.getCharacterId());
        }
    }

    static final class VillagerNpcRenderer extends MobRenderer<StoryNpcEntity, VillagerModel<StoryNpcEntity>> {
        VillagerNpcRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new VillagerModel<>(ctx.bakeLayer(ModelLayers.VILLAGER)), 0.5f);
            // Vanilla biome + profession clothes on top of the base villager skin.
            addLayer(new VillagerProfessionLayer<>(this, ctx.getResourceManager(), "villager"));
        }

        @Override
        public ResourceLocation getTextureLocation(StoryNpcEntity entity) {
            return texture(entity.getCharacterId());
        }
    }

    static final class HostileNpcRenderer extends HumanoidMobRenderer<HostileNpcEntity, PlayerModel<HostileNpcEntity>> {
        HostileNpcRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        }

        @Override
        public ResourceLocation getTextureLocation(HostileNpcEntity entity) {
            return texture(entity.getCharacterId());
        }
    }
}
