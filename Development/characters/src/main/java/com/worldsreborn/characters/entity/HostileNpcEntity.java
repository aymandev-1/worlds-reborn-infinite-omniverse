package com.worldsreborn.characters.entity;

import com.worldsreborn.characters.CharacterDef;
import com.worldsreborn.characters.Characters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

/** A human antagonist (robber, thug). Fights with fists, stays in its area, persists until defeated. */
public class HostileNpcEntity extends PathfinderMob implements Enemy {
    private static final EntityDataAccessor<String> CHARACTER = SynchedEntityData.defineId(HostileNpcEntity.class, EntityDataSerializers.STRING);

    private String storyTag = "";
    private UUID encounterId;
    /** Ticks left before this enemy may attack; -1 holds until released. Used while a scene's lines play out. */
    private int holdTicks;

    public HostileNpcEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        xpReward = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARACTER, "");
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false));
        goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Stand still and don't attack for {@code ticks} (-1: until {@link #release}). */
    public void hold(int ticks) {
        holdTicks = ticks;
        setTarget(null);
        getNavigation().stop();
    }

    public void release() {
        holdTicks = 0;
    }

    public boolean isHeldUntilReleased() {
        return holdTicks < 0;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (holdTicks != 0) {
            if (holdTicks > 0) {
                holdTicks--;
            }
            setTarget(null);
            getNavigation().stop();
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (source.getEntity() instanceof Player) {
            holdTicks = 0; // attacked: fight back
        }
        return super.hurt(source, amount);
    }

    public String getCharacterId() {
        return entityData.get(CHARACTER);
    }

    public void setCharacterId(String id) {
        entityData.set(CHARACTER, id);
        CharacterDef def = Characters.get(id);
        if (def != null) {
            setCustomName(Component.translatable(def.nameKey()));
            setCustomNameVisible(false);
        }
    }

    public String getStoryTag() {
        return storyTag;
    }

    public void setStoryTag(String storyTag) {
        this.storyTag = storyTag == null ? "" : storyTag;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.encounterId = encounterId;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("WRCharacter", getCharacterId());
        tag.putString("WRStoryTag", storyTag);
        if (encounterId != null) {
            tag.putUUID("WREncounter", encounterId);
        }
        tag.putInt("WRHold", holdTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setCharacterId(tag.getString("WRCharacter"));
        storyTag = tag.getString("WRStoryTag");
        encounterId = tag.hasUUID("WREncounter") ? tag.getUUID("WREncounter") : null;
        holdTicks = tag.getInt("WRHold");
    }
}
