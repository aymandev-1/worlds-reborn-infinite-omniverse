package com.worldsreborn.marvel.doomsday;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Steve's inventory, put aside while the player plays as Spider-Man (Scene 11). Saved with the world, keyed by player,
 * so a reload, a crash or dying mid-scene can never lose or duplicate it: it is taken once and given back once.
 */
public final class PerspectiveStash extends SavedData {
    private static final String NAME = "worldsreborn_marvel_stash";
    private final Map<UUID, ListTag> stashed = new HashMap<>();

    private static PerspectiveStash get(ServerPlayer player) {
        return player.server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PerspectiveStash::new, PerspectiveStash::load, null), NAME);
    }

    /** Takes the player's whole inventory (only if nothing is stashed for them yet) and empties it. */
    static void stash(ServerPlayer player) {
        PerspectiveStash data = get(player);
        if (data.stashed.containsKey(player.getUUID())) {
            return;
        }
        ListTag items = player.getInventory().save(new ListTag());
        data.stashed.put(player.getUUID(), items);
        data.setDirty();
        player.getInventory().clearContent();
    }

    /** Whether this player's own inventory is put aside (they are playing as someone else). */
    static boolean holds(ServerPlayer player) {
        return get(player).stashed.containsKey(player.getUUID());
    }

    /** Gives the stashed inventory back (Peter's kit is removed first). Does nothing if nothing is stashed. */
    static void restore(ServerPlayer player) {
        PerspectiveStash data = get(player);
        ListTag items = data.stashed.remove(player.getUUID());
        if (items == null) {
            return;
        }
        player.getInventory().clearContent();
        player.getInventory().load(items);
        data.setDirty();
    }

    private static PerspectiveStash load(CompoundTag tag, HolderLookup.Provider registries) {
        PerspectiveStash data = new PerspectiveStash();
        CompoundTag players = tag.getCompound("players");
        for (String key : players.getAllKeys()) {
            data.stashed.put(UUID.fromString(key), players.getList(key, Tag.TAG_COMPOUND));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag players = new CompoundTag();
        stashed.forEach((id, items) -> players.put(id.toString(), items));
        tag.put("players", players);
        return tag;
    }
}
