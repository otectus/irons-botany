package com.ironsbotany.common.entity;

import com.ironsbotany.common.registry.IBEntities;
import com.ironsbotany.common.registry.IBParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.UUID;
import java.util.function.Predicate;

public class SparkSwarmEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> LIFETIME =
        SynchedEntityData.defineId(SparkSwarmEntity.class, EntityDataSerializers.INT);

    private UUID ownerUUID;
    private int maxLifetime = 200; // 10 seconds

    public SparkSwarmEntity(EntityType<? extends SparkSwarmEntity> type, Level level) {
        super(type, level);
    }

    public SparkSwarmEntity(Level level, LivingEntity owner, int lifetime) {
        super(IBEntities.SPARK_SWARM.get(), level);
        this.ownerUUID = owner.getUUID();
        this.maxLifetime = lifetime;
        this.setPos(owner.getX(), owner.getY() + 1.5, owner.getZ());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LIFETIME, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.5D, false));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, LivingEntity.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));

        // Only target monsters within 8 blocks of the owner
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
            (target) -> {
                if (ownerUUID == null) return false;
                Player owner = level().getPlayerByUUID(ownerUUID);
                if (owner == null || !owner.isAlive()) return false;
                return target.distanceTo(owner) <= 8.0;
            }
        ));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 10.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.4D)
            .add(Attributes.ATTACK_DAMAGE, 4.0D)
            .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public void tick() {
        super.tick();

        int currentLifetime = this.entityData.get(LIFETIME);
        this.entityData.set(LIFETIME, currentLifetime + 1);

        // Follow-owner logic (server side only)
        if (!this.level().isClientSide && ownerUUID != null) {
            Player owner = this.level().getPlayerByUUID(ownerUUID);

            if (owner == null || !owner.isAlive()) {
                this.discard();
                return;
            }

            double distToOwner = this.distanceTo(owner);

            // Teleport near owner if too far away
            if (distToOwner > 16.0) {
                double tx = owner.getX() + (this.random.nextDouble() - 0.5) * 4.0;
                double ty = owner.getY();
                double tz = owner.getZ() + (this.random.nextDouble() - 0.5) * 4.0;
                this.teleportTo(tx, ty, tz);
                this.getNavigation().stop();
            }
            // Navigate toward owner if drifting too far and not currently attacking
            else if (distToOwner > 4.0 && this.getTarget() == null) {
                this.getNavigation().moveTo(owner, 1.2D);
            }
        }

        // Spawn particles (throttled to every other tick for performance with multiple swarms)
        if (this.level().isClientSide && this.tickCount % 2 == 0) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(
                    IBParticles.MANA_TRANSFER.get(),
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getY() + this.random.nextDouble() * 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                    0, 0.05, 0
                );
            }
        }

        // Remove after lifetime expires
        if (currentLifetime > maxLifetime) {
            if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    IBParticles.MANA_TRANSFER.get(),
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    20, 0.15, 0.15, 0.15, 0.1
                );
            }
            this.discard();
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);

        if (hit && !this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                IBParticles.MANA_TRANSFER.get(),
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                10, 0.1, 0.1, 0.1, 0.05
            );
        }

        return hit;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
        tag.putInt("MaxLifetime", this.maxLifetime);
        tag.putInt("CurrentLifetime", this.entityData.get(LIFETIME));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        this.maxLifetime = tag.getInt("MaxLifetime");
        this.entityData.set(LIFETIME, tag.getInt("CurrentLifetime"));
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }
}
