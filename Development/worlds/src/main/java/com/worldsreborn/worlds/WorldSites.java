package com.worldsreborn.worlds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * World-level record of built sites and shared world flags (stored with the Overworld's data). A site is built once;
 * afterwards everyone uses the recorded position.
 */
public final class WorldSites extends SavedData {
    private static final String NAME = "worldsreborn_sites";

    public record Site(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private final Map<String, Site> sites = new LinkedHashMap<>();
    private final Set<String> flags = new LinkedHashSet<>();

    public static WorldSites get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WorldSites::new, WorldSites::load, null), NAME);
    }

    public Optional<Site> site(String id) {
        return Optional.ofNullable(sites.get(id));
    }

    public boolean has(String id) {
        return sites.containsKey(id);
    }

    public void record(String id, ResourceKey<Level> dimension, BlockPos pos) {
        sites.put(id, new Site(dimension, pos.immutable()));
        setDirty();
    }

    public void remove(String id) {
        if (sites.remove(id) != null) {
            setDirty();
        }
    }

    public Map<String, Site> all() {
        return sites;
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public void setFlag(String flag) {
        if (flags.add(flag)) {
            setDirty();
        }
    }

    public void clearFlag(String flag) {
        if (flags.remove(flag)) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag s = new CompoundTag();
        sites.forEach((id, site) -> {
            CompoundTag t = new CompoundTag();
            t.putString("dim", site.dimension().location().toString());
            t.put("pos", NbtUtils.writeBlockPos(site.pos()));
            s.put(id, t);
        });
        tag.put("sites", s);
        CompoundTag f = new CompoundTag();
        flags.forEach(flag -> f.putBoolean(flag, true));
        tag.put("flags", f);
        return tag;
    }

    private static WorldSites load(CompoundTag tag, HolderLookup.Provider registries) {
        WorldSites data = new WorldSites();
        CompoundTag s = tag.getCompound("sites");
        for (String id : s.getAllKeys()) {
            CompoundTag t = s.getCompound(id);
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(t.getString("dim")));
            NbtUtils.readBlockPos(t, "pos").ifPresent(pos -> data.sites.put(id, new Site(dim, pos)));
        }
        data.flags.addAll(tag.getCompound("flags").getAllKeys());
        return data;
    }
}
