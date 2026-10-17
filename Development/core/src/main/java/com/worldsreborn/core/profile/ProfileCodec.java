package com.worldsreborn.core.profile;

import com.worldsreborn.core.WRCore;
import com.worldsreborn.core.model.Location;
import com.worldsreborn.core.model.ObjectiveTarget;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.QuestState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.Collection;
import java.util.Map;

/** Converts {@link PlayerProgress} to and from NBT. Field names are part of the save format: never rename them. */
public final class ProfileCodec {
    private ProfileCodec() {
    }

    public static CompoundTag write(PlayerProgress p) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("dataVersion", PlayerProgress.CURRENT_DATA_VERSION);

        CompoundTag quests = new CompoundTag();
        for (Map.Entry<String, QuestState> e : p.quests().entrySet()) {
            CompoundTag q = new CompoundTag();
            q.putInt("step", e.getValue().stepIndex);
            q.putInt("count", e.getValue().count);
            q.putBoolean("done", e.getValue().completed);
            quests.put(e.getKey(), q);
        }
        tag.put("quests", quests);
        tag.put("rewards", strings(p.claimedRewards()));
        tag.put("flags", strings(p.flags()));
        tag.put("destUnlocked", strings(p.unlockedDestinations()));
        tag.put("destDiscovered", strings(p.discoveredDestinations()));
        tag.put("abilities", strings(p.unlockedAbilities()));

        ListTag equipped = new ListTag();
        for (int i = 0; i < PlayerProgress.ABILITY_SLOTS; i++) {
            String a = p.equipped(i);
            equipped.add(StringTag.valueOf(a == null ? "" : a));
        }
        tag.put("equipped", equipped);

        CompoundTag cooldowns = new CompoundTag();
        p.abilityCooldowns().forEach(cooldowns::putLong);
        tag.put("cooldowns", cooldowns);

        putString(tag, "tracked", p.trackedQuest());
        putString(tag, "pendingDialogue", p.pendingDialogue());
        putString(tag, "cinematic", p.activeCinematic());
        tag.putInt("cinematicTicks", p.cinematicTicksLeft());
        putString(tag, "transitDest", p.transitDestination());
        putString(tag, "transitCinematic", p.transitCinematic());
        tag.putInt("transitTicks", p.transitTicksLeft());
        putString(tag, "perspective", p.perspectiveSkin());

        ObjectiveTarget t = p.target();
        if (t != null) {
            CompoundTag tt = new CompoundTag();
            tt.putString("quest", t.questId());
            tt.putString("step", t.stepId());
            tt.put("loc", location(t.location()));
            tt.putDouble("radius", t.radius());
            tag.put("target", tt);
        }
        if (p.checkpoint() != null) {
            tag.put("checkpoint", location(p.checkpoint()));
        }
        if (p.returnLocation() != null) {
            tag.put("return", location(p.returnLocation()));
        }
        return tag;
    }

    public static void read(PlayerProgress p, CompoundTag tag) {
        p.reset();
        int version = tag.contains("dataVersion") ? tag.getInt("dataVersion") : 0;

        CompoundTag quests = tag.getCompound("quests");
        for (String id : quests.getAllKeys()) {
            CompoundTag q = quests.getCompound(id);
            p.quests().put(id, new QuestState(q.getInt("step"), q.getInt("count"), q.getBoolean("done")));
        }
        readStrings(tag, "rewards", p.claimedRewards());
        readStrings(tag, "flags", p.flags());
        readStrings(tag, "destUnlocked", p.unlockedDestinations());
        readStrings(tag, "destDiscovered", p.discoveredDestinations());
        readStrings(tag, "abilities", p.unlockedAbilities());

        ListTag equipped = tag.getList("equipped", Tag.TAG_STRING);
        for (int i = 0; i < Math.min(equipped.size(), PlayerProgress.ABILITY_SLOTS); i++) {
            String a = equipped.getString(i);
            p.equip(i, a.isEmpty() ? null : a);
        }
        CompoundTag cooldowns = tag.getCompound("cooldowns");
        for (String id : cooldowns.getAllKeys()) {
            p.abilityCooldowns().put(id, cooldowns.getLong(id));
        }

        p.setTrackedQuest(getString(tag, "tracked"));
        p.setPendingDialogue(getString(tag, "pendingDialogue"));
        p.setCinematic(getString(tag, "cinematic"), tag.getInt("cinematicTicks"));
        p.setTransit(getString(tag, "transitDest"), getString(tag, "transitCinematic"), tag.getInt("transitTicks"));
        p.setPerspectiveSkin(getString(tag, "perspective"));

        if (tag.contains("target", Tag.TAG_COMPOUND)) {
            CompoundTag tt = tag.getCompound("target");
            p.setTarget(new ObjectiveTarget(tt.getString("quest"), tt.getString("step"),
                    location(tt.getCompound("loc")), tt.getDouble("radius")));
        }
        if (tag.contains("checkpoint", Tag.TAG_COMPOUND)) {
            p.setCheckpoint(location(tag.getCompound("checkpoint")));
        }
        if (tag.contains("return", Tag.TAG_COMPOUND)) {
            p.setReturnLocation(location(tag.getCompound("return")));
        }

        try {
            p.migrate(version);
        } catch (IllegalStateException e) {
            WRCore.LOGGER.error("[Worlds Reborn] {}", e.getMessage());
            throw e;
        }
    }

    private static ListTag strings(Collection<String> values) {
        ListTag list = new ListTag();
        values.forEach(v -> list.add(StringTag.valueOf(v)));
        return list;
    }

    private static void readStrings(CompoundTag tag, String key, Collection<String> into) {
        ListTag list = tag.getList(key, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            into.add(list.getString(i));
        }
    }

    private static void putString(CompoundTag tag, String key, String value) {
        if (value != null) {
            tag.putString(key, value);
        }
    }

    private static String getString(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : null;
    }

    private static CompoundTag location(Location l) {
        CompoundTag t = new CompoundTag();
        t.putString("dim", l.dimension());
        t.putDouble("x", l.x());
        t.putDouble("y", l.y());
        t.putDouble("z", l.z());
        t.putFloat("yaw", l.yaw());
        t.putFloat("pitch", l.pitch());
        return t;
    }

    private static Location location(CompoundTag t) {
        return new Location(t.getString("dim"), t.getDouble("x"), t.getDouble("y"), t.getDouble("z"),
                t.getFloat("yaw"), t.getFloat("pitch"));
    }
}
