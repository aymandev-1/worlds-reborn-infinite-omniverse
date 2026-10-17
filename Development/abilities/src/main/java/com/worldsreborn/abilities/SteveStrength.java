package com.worldsreborn.abilities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Steve's raw strength. He doesn't consciously use his power yet, but his bare-handed punches already hit far
 * harder than a normal player's. Weapons are unaffected.
 */
public final class SteveStrength {
    private SteveStrength() {
    }

    public static void onDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof ServerPlayer player) || !player.getMainHandItem().isEmpty()) {
            return;
        }
        double bonus = AbilitiesConfig.PUNCH_BONUS.getAsDouble();
        if (bonus <= 0) {
            return;
        }
        // Vanilla already scaled the fist's 1 damage by the swing charge (0.2 for spam-clicking, 1 when charged;
        // the ticker is reset before damage is dealt), so the incoming amount doubles as the charge.
        float charge = Math.min(1f, event.getAmount());
        event.setAmount(event.getAmount() + (float) (bonus * charge));
        LivingEntity target = event.getEntity();
        double kb = AbilitiesConfig.PUNCH_KNOCKBACK.getAsDouble() * charge;
        if (kb > 0) {
            target.knockback(kb, player.getX() - target.getX(), player.getZ() - target.getZ());
        }
        if (charge > 0.9f && player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(), 8, 0.3, 0.3, 0.3, 0.2);
            level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1f, 0.8f);
        }
    }
}
