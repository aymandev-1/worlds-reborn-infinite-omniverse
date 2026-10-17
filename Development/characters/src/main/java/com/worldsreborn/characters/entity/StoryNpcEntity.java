package com.worldsreborn.characters.entity;

import com.worldsreborn.characters.CharacterDef;
import com.worldsreborn.characters.Characters;
import com.worldsreborn.core.api.EntityPromptProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

/**
 * A story character. Invulnerable, persistent and stationary unless told to walk somewhere. Optional behaviours:
 * <ul>
 *   <li>shy: vanishes when a player looks straight at it (the unnoticed observer)</li>
 *   <li>cower: crouches in place (frightened civilians)</li>
 *   <li>depart: leaps away and disappears after a short delay</li>
 *   <li>fighter: attacks hostile story characters nearby (allies such as Spider-Man), webbing them to slow them</li>
 *   <li>ambient: background townsfolk and guards; wander near home, nothing to talk about</li>
 * </ul>
 */
public class StoryNpcEntity extends PathfinderMob implements EntityPromptProvider, VillagerDataHolder {
    private static final EntityDataAccessor<String> CHARACTER = SynchedEntityData.defineId(StoryNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> COWER = SynchedEntityData.defineId(StoryNpcEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AMBIENT = SynchedEntityData.defineId(StoryNpcEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean shy;
    private String storyTag = "";
    private int departTicks = -1;
    private BlockPos walkTarget;
    private double walkSpeed = 0.8;
    private boolean fighter;
    private final WebFightGoal meleeGoal = new WebFightGoal();
    private final NearestAttackableTargetGoal<HostileNpcEntity> enemyGoal =
            new NearestAttackableTargetGoal<>(this, HostileNpcEntity.class, false);
    private final WaterAvoidingRandomStrollGoal strollGoal = new WaterAvoidingRandomStrollGoal(this, 0.6);

    public StoryNpcEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setInvulnerable(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARACTER, "");
        builder.define(COWER, false);
        builder.define(AMBIENT, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0f));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    // ------------------------------------------------------------------ character data

    public String getCharacterId() {
        return entityData.get(CHARACTER);
    }

    public void setCharacterId(String id) {
        entityData.set(CHARACTER, id);
        CharacterDef def = Characters.get(id);
        if (def != null) {
            setCustomName(Component.translatable(def.nameKey()));
            setCustomNameVisible(false);
            if (def.heldItem() != null) {
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BuiltInRegistries.ITEM.get(def.heldItem())));
                setDropChance(EquipmentSlot.MAINHAND, 0f);
            }
        }
    }

    // ------------------------------------------------------------------ villager look (profession clothes)

    @Override
    public VillagerData getVillagerData() {
        CharacterDef def = getCharacter();
        VillagerProfession profession = def == null || def.villagerProfession() == null ? VillagerProfession.NONE
                : BuiltInRegistries.VILLAGER_PROFESSION.get(ResourceLocation.parse(def.villagerProfession()));
        return new VillagerData(VillagerType.PLAINS, profession, 3);
    }

    @Override
    public void setVillagerData(VillagerData data) {
        // Appearance comes from the character definition.
    }

    // ------------------------------------------------------------------ fighting allies

    public boolean isFighter() {
        return fighter;
    }

    public void setFighter(boolean fighter) {
        if (this.fighter == fighter) {
            return;
        }
        this.fighter = fighter;
        if (fighter) {
            goalSelector.addGoal(2, meleeGoal);
            targetSelector.addGoal(1, enemyGoal);
        } else {
            goalSelector.removeGoal(meleeGoal);
            targetSelector.removeGoal(enemyGoal);
            setTarget(null);
        }
    }

    public boolean isAmbient() {
        return entityData.get(AMBIENT);
    }

    /** Background character: wanders within {@code range} blocks of where it stands (0 = stays put), no talk prompt. */
    public void setAmbient(boolean ambient, int range) {
        entityData.set(AMBIENT, ambient);
        goalSelector.removeGoal(strollGoal);
        if (ambient && range > 0) {
            restrictTo(blockPosition(), range);
            goalSelector.addGoal(4, strollGoal);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && fighter && target instanceof LivingEntity living && level() instanceof ServerLevel server) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()),
                    living.getX(), living.getY() + 1.0, living.getZ(), 14, 0.3, 0.5, 0.3, 0.05);
            server.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.NEUTRAL, 0.8f, 1.2f);
        }
        return hit;
    }

    /**
     * How a fighter ally (Spider-Man) fights: keeps a few blocks away, shoots webs that hit, slow and pin enemies,
     * and every third move web-zips in for a flying kick. Swings up off the ground when far from the fight.
     */
    private final class WebFightGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private int cooldown;
        private int moves;

        WebFightGoal() {
            setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = getTarget();
            return t != null && t.isAlive();
        }

        @Override
        public void stop() {
            getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity t = getTarget();
            if (t == null || !(level() instanceof ServerLevel server)) {
                return;
            }
            getLookControl().setLookAt(t, 30f, 30f);
            double d = distanceToSqr(t);
            boolean see = getSensing().hasLineOfSight(t);
            if (d > 100 || !see) {
                getNavigation().moveTo(t, 1.3);
                if (onGround() && d > 144 && getRandom().nextInt(30) == 0) {
                    // Web-swing: a high leap towards the fight.
                    Vec3 dir = t.position().subtract(position()).normalize();
                    setDeltaMovement(dir.x * 0.9, 0.75, dir.z * 0.9);
                    webLine(server, position().add(0, 1.6, 0), position().add(dir.x * 3, 9, dir.z * 3));
                }
            } else if (d < 9) {
                Vec3 away = position().subtract(t.position()).normalize();
                getNavigation().moveTo(getX() + away.x * 4, getY(), getZ() + away.z * 4, 1.3);
            } else {
                getNavigation().stop();
            }
            if (--cooldown > 0 || !see || d > 196) {
                return;
            }
            swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            if (++moves % 3 == 0) {
                // Web-zip in and kick.
                Vec3 dir = t.position().subtract(position()).normalize();
                webLine(server, position().add(0, 1.4, 0), t.position().add(0, 1, 0));
                setDeltaMovement(dir.x * 1.2, 0.35, dir.z * 1.2);
                t.hurt(damageSources().mobAttack(StoryNpcEntity.this), 6f);
                t.knockback(0.9, -dir.x, -dir.z);
                server.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.NEUTRAL, 1f, 1.1f);
            } else {
                // Web shot: hits, slows hard and wraps the target.
                webLine(server, position().add(0, 1.4, 0), t.position().add(0, 1, 0));
                t.hurt(damageSources().mobAttack(StoryNpcEntity.this), 3f);
                t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 4));
                t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()),
                        t.getX(), t.getY() + 1.0, t.getZ(), 18, 0.3, 0.6, 0.3, 0.05);
                server.playSound(null, blockPosition(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 1f, 1.6f);
                server.playSound(null, t.blockPosition(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.NEUTRAL, 1f, 1.4f);
            }
            cooldown = 22;
        }
    }

    /** A strand of web from {@code a} to {@code b}, drawn with particles. */
    private static void webLine(ServerLevel server, Vec3 a, Vec3 b) {
        int steps = (int) Math.max(4, a.distanceTo(b) * 3);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = a.lerp(b, i / (double) steps);
            server.sendParticles(ParticleTypes.WHITE_ASH, p.x, p.y, p.z, 2, 0.02, 0.02, 0.02, 0);
            server.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    @Nullable
    public CharacterDef getCharacter() {
        return Characters.get(getCharacterId());
    }

    /** Free-form tag campaigns use to find their NPCs again (e.g. "wrmarvel:cartographer"). */
    public String getStoryTag() {
        return storyTag;
    }

    public void setStoryTag(String storyTag) {
        this.storyTag = storyTag == null ? "" : storyTag;
    }

    public void setShy(boolean shy) {
        this.shy = shy;
    }

    public boolean isCowering() {
        return entityData.get(COWER);
    }

    public void setCowering(boolean cower) {
        entityData.set(COWER, cower);
        setShiftKeyDown(cower);
    }

    /** Leap upward and vanish after {@code ticks} (e.g. Spider-Man leaving for an emergency). */
    public void depart(int ticks) {
        this.departTicks = ticks;
        setDeltaMovement(getLookAngle().x * 0.6, 1.1, getLookAngle().z * 0.6);
        hasImpulse = true;
    }

    /** Walking pace for {@link #walkTo} (0.8 by default; slower for someone injured). Not saved. */
    public void setWalkSpeed(double speed) {
        this.walkSpeed = speed;
    }

    public void walkTo(BlockPos pos) {
        this.walkTarget = pos;
        stepTowards(pos);
    }

    public void stopWalking() {
        walkTarget = null;
        getNavigation().stop();
    }

    public boolean isWalking() {
        return walkTarget != null;
    }

    /** Paths straight to {@code pos}, or to a waypoint 16 blocks along the way when it is beyond path range. */
    private void stepTowards(BlockPos pos) {
        double dx = pos.getX() + 0.5 - getX();
        double dz = pos.getZ() + 0.5 - getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 20) {
            double k = 16 / dist;
            getNavigation().moveTo(getX() + dx * k, pos.getY(), getZ() + dz * k, walkSpeed);
        } else {
            getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, walkSpeed);
        }
    }

    // ------------------------------------------------------------------ behaviour

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (departTicks >= 0 && --departTicks <= 0) {
            vanish();
            return;
        }
        if (walkTarget != null && tickCount % 10 == 0) {
            if (blockPosition().closerThan(walkTarget, 1.5)) {
                walkTarget = null;
            } else if (level().getNearestPlayer(this, 9) == null) {
                getNavigation().stop(); // wait for the player to catch up
            } else if (getNavigation().isDone()) {
                stepTowards(walkTarget);
            }
        }
        if (shy && tickCount % 4 == 0) {
            for (Player player : level().players()) {
                if (player.distanceToSqr(this) < 28 * 28 && isLookedAtBy(player)) {
                    vanish();
                    return;
                }
            }
        }
    }

    private boolean isLookedAtBy(Player player) {
        Vec3 view = player.getViewVector(1.0f).normalize();
        Vec3 toMe = getEyePosition().subtract(player.getEyePosition());
        double dist = toMe.length();
        if (dist < 0.01) {
            return true;
        }
        double dot = view.dot(toMe.scale(1.0 / dist));
        return dot > 1.0 - 0.025 / Math.max(1.0, dist / 8.0) && player.hasLineOfSight(this);
    }

    /** Disappear with a puff of smoke. */
    public void vanish() {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0, getZ(), 12, 0.3, 0.6, 0.3, 0.01);
        }
        discard();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || isAmbient()) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            NeoForge.EVENT_BUS.post(new NpcInteractEvent(serverPlayer, this));
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    @Nullable
    public Component getInteractionPrompt(Player player) {
        CharacterDef def = getCharacter();
        return def == null || isAmbient() ? null : Component.translatable("prompt.wrcharacters.talk", Component.translatable(def.nameKey()));
    }

    // ------------------------------------------------------------------ invulnerable, persistent, not pushable

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("WRCharacter", getCharacterId());
        tag.putString("WRStoryTag", storyTag);
        tag.putBoolean("WRShy", shy);
        tag.putBoolean("WRCower", isCowering());
        tag.putInt("WRDepart", departTicks);
        tag.putBoolean("WRFighter", fighter);
        tag.putBoolean("WRAmbient", isAmbient());
        tag.putInt("WRRoam", hasRestriction() ? (int) getRestrictRadius() : 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setCharacterId(tag.getString("WRCharacter"));
        storyTag = tag.getString("WRStoryTag");
        shy = tag.getBoolean("WRShy");
        setCowering(tag.getBoolean("WRCower"));
        departTicks = tag.contains("WRDepart") ? tag.getInt("WRDepart") : -1;
        setFighter(tag.getBoolean("WRFighter"));
        if (tag.getBoolean("WRAmbient")) {
            setAmbient(true, tag.getInt("WRRoam"));
        }
    }
}
