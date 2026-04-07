package com.ironsbotany.common.entity;

import com.ironsbotany.common.registry.IBEntities;
import com.ironsbotany.common.registry.IBParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
            // Interpolated trail between previous and current position
            double dx = this.getX() - this.xOld;
            double dy = this.getY() - this.yOld;
            double dz = this.getZ() - this.zOld;

            // Compute perpendicular vectors for spiral offset
            double speed = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double nx, ny, nz, bx, by, bz;
            if (speed > 0.001) {
                // Normalize velocity
                double vx = dx / speed, vy = dy / speed, vz = dz / speed;
                // Cross with UP to get perpendicular
                nx = -vz; ny = 0; nz = vx;
                double nLen = Math.sqrt(nx * nx + nz * nz);
                if (nLen > 0.001) {
                    nx /= nLen; nz /= nLen;
                } else {
                    nx = 1; nz = 0;
                }
                // Second perpendicular via cross product
                bx = vy * nz - vz * ny;
                by = vz * nx - vx * nz;
                bz = vx * ny - vy * nx;
            } else {
                nx = 1; ny = 0; nz = 0;
                bx = 0; by = 1; bz = 0;
            }

            int steps = 4;
            for (int i = 0; i < steps; i++) {
                float t = (float) i / steps;
                double px = this.xOld + dx * t;
                double py = this.yOld + dy * t;
                double pz = this.zOld + dz * t;

                // Spiral offset
                double spiralAngle = (this.tickCount + t) * 1.5;
                double spiralRadius = 0.12;
                double offsetX = (nx * Math.cos(spiralAngle) + bx * Math.sin(spiralAngle)) * spiralRadius;
                double offsetY = (ny * Math.cos(spiralAngle) + by * Math.sin(spiralAngle)) * spiralRadius;
                double offsetZ = (nz * Math.cos(spiralAngle) + bz * Math.sin(spiralAngle)) * spiralRadius;

                this.level().addParticle(
                    IBParticles.BOTANICAL_BURST.get(),
                    px + offsetX + (this.random.nextDouble() - 0.5) * 0.05,
                    py + offsetY + (this.random.nextDouble() - 0.5) * 0.05,
                    pz + offsetZ + (this.random.nextDouble() - 0.5) * 0.05,
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
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        for (int i = 0; i < 20; i++) {
            double angle = (i / 20.0) * Math.PI * 2;
            double speed = 0.3;
            serverLevel.sendParticles(
                IBParticles.BOTANICAL_BURST.get(),
                this.getX(), this.getY(), this.getZ(),
                0,
                Math.cos(angle) * speed, 0.2, Math.sin(angle) * speed,
                1.0
            );
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide) {
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    IBParticles.BOTANICAL_BURST.get(),
                    this.getX(), this.getY(), this.getZ(),
                    15, 0.25, 0.25, 0.25, 0.1
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
