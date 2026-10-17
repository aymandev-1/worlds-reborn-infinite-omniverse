package com.worldsreborn.characters;

import com.worldsreborn.characters.entity.HostileNpcEntity;
import com.worldsreborn.characters.entity.StoryNpcEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Worlds Reborn: Characters. Generic story NPC and hostile humanoid entities. Which character an entity represents
 * (name, skin, model) comes from {@link Characters}, so campaigns add characters without new entity types.
 */
@Mod(WRCharacters.MODID)
public final class WRCharacters {
    public static final String MODID = "wrcharacters";

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    /** Humanoid story character (player-shaped model). */
    public static final DeferredHolder<EntityType<?>, EntityType<StoryNpcEntity>> STORY_NPC = ENTITIES.register("story_npc",
            () -> EntityType.Builder.<StoryNpcEntity>of(StoryNpcEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f).eyeHeight(1.62f).clientTrackingRange(10).build("story_npc"));

    /** Villager-shaped story character (e.g. the Cartographer). */
    public static final DeferredHolder<EntityType<?>, EntityType<StoryNpcEntity>> STORY_VILLAGER = ENTITIES.register("story_villager",
            () -> EntityType.Builder.<StoryNpcEntity>of(StoryNpcEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f).eyeHeight(1.62f).clientTrackingRange(10).build("story_villager"));

    /** Hostile humanoid used for human antagonists such as street robbers. */
    public static final DeferredHolder<EntityType<?>, EntityType<HostileNpcEntity>> HOSTILE_NPC = ENTITIES.register("hostile_npc",
            () -> EntityType.Builder.<HostileNpcEntity>of(HostileNpcEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f).eyeHeight(1.62f).clientTrackingRange(10).build("hostile_npc"));

    public WRCharacters(IEventBus modBus, ModContainer container) {
        ENTITIES.register(modBus);
        modBus.addListener(this::attributes);
    }

    private void attributes(EntityAttributeCreationEvent event) {
        event.put(STORY_NPC.get(), StoryNpcEntity.createAttributes().build());
        event.put(STORY_VILLAGER.get(), StoryNpcEntity.createAttributes().build());
        event.put(HOSTILE_NPC.get(), HostileNpcEntity.createAttributes().build());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
