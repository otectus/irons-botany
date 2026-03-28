package com.ironsbotany.common.spell;

import com.ironsbotany.IronsBotany;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import com.ironsbotany.common.spell.catalyst.SpellContext;

import java.util.List;

public class PetalStormSpell extends AbstractBotanicalSpell {
    public PetalStormSpell() {
        super(25000, 12000);
        this.manaCostPerLevel = 18;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 3;
        this.castTime = 15;
        this.baseManaCost = 35;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.UNCOMMON)
                .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
                .setMaxLevel(5)
                .setCooldownSeconds(25)
                .build();
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("spell.ironsbotany.petal_storm.description"));
    }

    @Override
    protected void executeBotanicalEffect(Level level, int spellLevel, LivingEntity entity,
                                                 CastSource castSource, MagicData playerMagicData,
                                                 SpellContext context) {
        if (level.isClientSide) {
            return;
        }

        float damage = getSpellPower(spellLevel, entity) * 0.5f;
        int radius = 4 + spellLevel;
        
        // Find nearby enemies
        java.util.List<LivingEntity> targets = level.getEntitiesOfClass(
            LivingEntity.class,
            entity.getBoundingBox().inflate(radius),
            target -> target != entity && !target.isAlliedTo(entity)
        );
        
        // Spiral petal wind-up animation (petals gathering inward)
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (int wave = 0; wave < 3; wave++) {
                double waveRadius = radius - (wave * (radius / 3.0));
                int particlesPerWave = 20 + (wave * 10);
                for (int i = 0; i < particlesPerWave; i++) {
                    double angle = (i / (double) particlesPerWave) * Math.PI * 2 + (wave * 0.5);
                    double x = entity.getX() + Math.cos(angle) * waveRadius;
                    double z = entity.getZ() + Math.sin(angle) * waveRadius;
                    serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.CHERRY_LEAVES,
                        x, entity.getY() + 0.5 + wave * 0.5, z,
                        3, 0.2, 0.1, 0.2, 0.08
                    );
                }
            }
        }

        for (LivingEntity target : targets) {
            // Deal damage
            target.hurt(level.damageSources().magic(), damage);

            // Knockback
            double dx = target.getX() - entity.getX();
            double dz = target.getZ() - entity.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > 0) {
                target.push(dx / distance * 0.5, 0.3, dz / distance * 0.5);
            }

            // Petal slash on each target
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.CHERRY_LEAVES,
                    target.getX(), target.getY() + 1, target.getZ(),
                    30, 0.5, 0.5, 0.5, 0.2
                );
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    1, 0, 0, 0, 0
                );
            }
        }

        // Outward spiral burst
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (int ring = 1; ring <= 4; ring++) {
                double ringRadius = (radius * ring) / 4.0;
                int particleCount = 15 * ring;
                for (int i = 0; i < particleCount; i++) {
                    double angle = (i / (double) particleCount) * Math.PI * 2 + (ring * 0.3);
                    double x = entity.getX() + Math.cos(angle) * ringRadius;
                    double z = entity.getZ() + Math.sin(angle) * ringRadius;
                    serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.CHERRY_LEAVES,
                        x, entity.getY() + 1, z,
                        2, 0.1, 0.1, 0.1, 0.05
                    );
                }
            }
        }
        
    }

    @Override
    public ResourceLocation getSpellResource() {
        return new ResourceLocation(IronsBotany.MODID, "petal_storm");
    }

}
