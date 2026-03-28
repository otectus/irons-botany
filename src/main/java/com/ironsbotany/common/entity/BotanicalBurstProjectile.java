package com.ironsbotany.common.entity;

import com.ironsbotany.common.registry.IBEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.HashSet;
import java.util.Set;

public class BotanicalBurstProjectile extends ThrowableProjectile {
    private float damage = 8.0f;
    private int pierceCount = 0;
    private final Set<Integer> hitEntities = new HashSet<>();

    public BotanicalBurstProjectile(EntityType<? extends BotanicalBurstProjectile> type, Level level) {
        super(type, level);
    }

    public BotanicalBurstProjectile(Level level, LivingEntity shooter, float damage) {
        super(IBEntities.BOTANICAL_BURST.get(), shooter, level);
        this.damage = damage;
    }

    @Override
    protected void defineSynchedData() {
        // No synced data needed
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            // Spawn trail particles
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(
                    ParticleTypes.CHERRY_LEAVES,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    0, 0, 0
                );
            }
        }

        // Remove after 10 seconds
        if (this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity target) {
            boolean isPiercing = this.getPersistentData().getBoolean("piercing");

            if (isPiercing) {
                // Skip entities we already hit
                int targetId = target.getId();
                if (hitEntities.contains(targetId)) {
                    return;
                }
                hitEntities.add(targetId);

                int maxPierce = this.getPersistentData().getInt("max_pierce");
                if (maxPierce <= 0) {
                    maxPierce = 3;
                }

                // Reduce damage by 20% per pierce
                float effectiveDamage = this.damage * (1.0f - 0.2f * pierceCount);
                target.hurt(this.damageSources().magic(), effectiveDamage);

                // Spawn impact particles
                spawnImpactParticles();

                pierceCount++;
                if (pierceCount >= maxPierce) {
                    this.discard();
                }
            } else {
                // Non-piercing: original behavior
                target.hurt(this.damageSources().magic(), this.damage);
                spawnImpactParticles();
                this.discard();
            }
        }
    }

    private void spawnImpactParticles() {
        for (int i = 0; i < 20; i++) {
            double angle = (i / 20.0) * Math.PI * 2;
            double speed = 0.3;
            this.level().addParticle(
                ParticleTypes.CHERRY_LEAVES,
                this.getX(),
                this.getY(),
                this.getZ(),
                Math.cos(angle) * speed,
                0.2,
                Math.sin(angle) * speed
            );
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide) {
            // Spawn impact particles
            for (int i = 0; i < 15; i++) {
                this.level().addParticle(
                    ParticleTypes.CHERRY_LEAVES,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.5,
                    (this.random.nextDouble() - 0.5) * 0.5,
                    (this.random.nextDouble() - 0.5) * 0.5
                );
            }

            this.discard();
        }
    }

    @Override
    protected float getGravity() {
        return 0.0F; // No gravity
    }
}
