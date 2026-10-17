package com.worldsreborn.marvel.doomsday;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/**
 * Spider-Man's web-shooters (Scene 11, only while playing as him). Use while aiming:
 * <ul>
 *   <li>at a web anchor point: anchors a web line to it (securing the car, closing off the danger area);</li>
 *   <li>at a person or creature: webs them in place;</li>
 *   <li>at any other block: zips towards it (hold sneak for a softer, higher swing).</li>
 * </ul>
 */
public class WebShooterItem extends Item {
    private static final double RANGE = 48;
    private static final DustParticleOptions WEB = new DustParticleOptions(new Vector3f(0.95f, 0.95f, 0.98f), 0.7f);

    public WebShooterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer sp) || !(level instanceof ServerLevel server)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        player.getCooldowns().addCooldown(this, 6);
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1f).scale(RANGE));
        BlockHitResult block = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        Vec3 reach = block.getType() == HitResult.Type.MISS ? end : block.getLocation();
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(level, player, eye, reach,
                new AABB(eye, reach).inflate(1.0), e -> e instanceof LivingEntity && e != player && !e.isSpectator(), 0.3f);
        server.playSound(null, player.blockPosition(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 0.9f, 1.6f);
        if (entity != null) {
            Entity target = entity.getEntity();
            strand(server, eye, target.position().add(0, target.getBbHeight() * 0.6, 0));
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 5));
                Doomsday.onWebbedEntity(sp, living);
            }
            return InteractionResultHolder.success(stack);
        }
        if (block.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.success(stack);
        }
        BlockPos hit = block.getBlockPos();
        strand(server, eye, block.getLocation());
        if (level.getBlockState(hit).getBlock() instanceof WebAnchorBlock) {
            Doomsday.onWebAnchor(sp, hit);
            return InteractionResultHolder.success(stack);
        }
        // Rescue interactions should work by aiming at the car itself, rather than
        // demanding that players discover three hidden technical anchor blocks.
        if (Doomsday.tryWebAccidentCar(sp, hit)) {
            return InteractionResultHolder.success(stack);
        }
        // Zip towards the point.
        Vec3 to = block.getLocation().subtract(player.position());
        double dist = to.length();
        Vec3 dir = to.normalize();
        double speed = Math.min(2.4, 0.6 + dist * 0.09);
        Vec3 push = player.isShiftKeyDown() ? dir.scale(speed * 0.7).add(0, 0.75, 0) : dir.scale(speed).add(0, 0.35, 0);
        player.setDeltaMovement(push);
        player.hurtMarked = true;
        player.fallDistance = 0;
        return InteractionResultHolder.success(stack);
    }

    private static void strand(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 d = to.subtract(from);
        int n = (int) Math.min(80, d.length() * 2.5);
        for (int i = 1; i <= n; i++) {
            Vec3 p = from.add(d.scale(i / (double) n));
            level.sendParticles(WEB, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.wrmarvel.web_shooters.tip"));
    }
}
