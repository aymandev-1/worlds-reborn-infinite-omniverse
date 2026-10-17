package com.worldsreborn.characters;

import com.worldsreborn.characters.entity.HostileNpcEntity;
import com.worldsreborn.characters.entity.StoryNpcEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

/** Spawning and lookup helpers for campaign code. */
public final class NpcSpawner {
    private NpcSpawner() {
    }

    public static StoryNpcEntity spawnStoryNpc(ServerLevel level, String characterId, String storyTag, BlockPos pos, float yaw) {
        CharacterDef def = Characters.get(characterId);
        EntityType<StoryNpcEntity> type = def != null && def.model() == CharacterDef.Model.VILLAGER
                ? WRCharacters.STORY_VILLAGER.get() : WRCharacters.STORY_NPC.get();
        StoryNpcEntity npc = type.create(level);
        if (npc == null) {
            throw new IllegalStateException("Could not create story NPC " + characterId);
        }
        npc.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0f);
        npc.setYHeadRot(yaw);
        npc.setYBodyRot(yaw);
        npc.setCharacterId(characterId);
        npc.setStoryTag(storyTag);
        level.addFreshEntity(npc);
        return npc;
    }

    public static HostileNpcEntity spawnHostile(ServerLevel level, String characterId, String storyTag, UUID encounter,
                                                BlockPos pos, float yaw) {
        HostileNpcEntity npc = WRCharacters.HOSTILE_NPC.get().create(level);
        if (npc == null) {
            throw new IllegalStateException("Could not create hostile NPC " + characterId);
        }
        npc.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0f);
        npc.setCharacterId(characterId);
        npc.setStoryTag(storyTag);
        npc.setEncounterId(encounter);
        npc.restrictTo(pos, 10); // drifts back to its post after a chase
        level.addFreshEntity(npc);
        return npc;
    }

    /** Story NPCs with this tag within {@code radius} of {@code center} (loaded chunks only). */
    public static List<StoryNpcEntity> findStoryNpcs(ServerLevel level, String storyTag, BlockPos center, double radius) {
        return level.getEntitiesOfClass(StoryNpcEntity.class, new AABB(center).inflate(radius),
                e -> e.isAlive() && storyTag.equals(e.getStoryTag()));
    }

    public static List<HostileNpcEntity> findHostiles(ServerLevel level, String storyTag, BlockPos center, double radius) {
        return level.getEntitiesOfClass(HostileNpcEntity.class, new AABB(center).inflate(radius),
                e -> e.isAlive() && storyTag.equals(e.getStoryTag()));
    }

    /** Removes existing NPCs with the tag nearby, then spawns a fresh one. Keeps a single instance per tag. */
    public static StoryNpcEntity ensureSingle(ServerLevel level, String characterId, String storyTag, BlockPos pos, float yaw) {
        findStoryNpcs(level, storyTag, pos, 96).forEach(StoryNpcEntity::discard);
        return spawnStoryNpc(level, characterId, storyTag, pos, yaw);
    }
}
