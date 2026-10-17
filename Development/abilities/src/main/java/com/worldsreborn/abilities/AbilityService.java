package com.worldsreborn.abilities;

import com.worldsreborn.core.WRCore;
import com.worldsreborn.core.model.AbilityDef;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.profile.Profiles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

public final class AbilityService {
    private AbilityService() {
    }

    public static boolean unlock(ServerPlayer player, String abilityId) {
        if (!WRContent.ABILITIES.contains(abilityId)) {
            return false;
        }
        boolean added = Profiles.get(player).unlockedAbilities().add(abilityId);
        Profiles.sync(player);
        return added;
    }

    public static boolean equip(ServerPlayer player, int slot, String abilityId) {
        boolean ok = Profiles.get(player).equip(slot, abilityId);
        Profiles.sync(player);
        return ok;
    }

    /** Validates and fires the ability in {@code slot}. Rejected requests change nothing. */
    public static boolean use(ServerPlayer player, int slot) {
        PlayerProgress p = Profiles.get(player);
        String id = p.equipped(slot);
        AbilityDef def = WRContent.ABILITIES.getOrNull(id);
        if (def == null || !p.unlockedAbilities().contains(id)) {
            return false;
        }
        long now = player.level().getGameTime();
        if (p.cooldownRemaining(id, now) > 0) {
            return false;
        }
        p.abilityCooldowns().put(id, now + def.cooldownTicks());
        WRCore.debug("{} used ability {}", player.getScoreboardName(), id);
        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(player, def));
        Profiles.sync(player);
        return true;
    }

    /** Effect for the developer test ability: a harmless shockwave that nudges nearby mobs away. */
    static void onAbilityUsed(AbilityUsedEvent event) {
        if (event.getAbility() != WRAbilities.DEV_PULSE) {
            return;
        }
        ServerPlayer player = event.getPlayer();
        player.serverLevel().sendParticles(ParticleTypes.SONIC_BOOM, player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0);
        player.level().playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.4f, 1.6f);
        for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5),
                e -> e != player)) {
            Vec3 push = e.position().subtract(player.position()).normalize().scale(0.8);
            e.push(push.x, 0.3, push.z);
            e.hurtMarked = true;
        }
    }
}
