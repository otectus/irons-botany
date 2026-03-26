package com.ironsbotany.common.spell;

import com.ironsbotany.IronsBotany;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import com.ironsbotany.common.spell.catalyst.SpellContext;

import java.util.List;

public class GaiaWrathSpell extends AbstractBotanicalSpell {
    public GaiaWrathSpell() {
        super(150000, 50000);
        this.manaCostPerLevel = 50;
        this.baseSpellPower = 30;
        this.spellPowerPerLevel = 8;
        this.castTime = 60;
        this.baseManaCost = 100;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.LEGENDARY)
                .setSchoolResource(ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "botanical"))
                .setMaxLevel(10)
                .setCooldownSeconds(180)
                .build();
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("spell.ironsbotany.gaia_wrath.description"));
    }

    @Override
    protected void executeBotanicalEffect(Level level, int spellLevel, LivingEntity entity,
                                                 CastSource castSource, MagicData playerMagicData,
                                                 SpellContext context) {
        if (level.isClientSide) {
            return;
        }

        float damage = getSpellPower(spellLevel, entity) * 2.0f; // Double damage for ultimate
        int radius = 8 + spellLevel; // 9-18 block radius
        
        // Find all enemies in radius
        java.util.List<LivingEntity> targets = level.getEntitiesOfClass(
            LivingEntity.class,
            entity.getBoundingBox().inflate(radius),
            target -> target != entity && !target.isAlliedTo(entity)
        );
        
        for (LivingEntity target : targets) {
            // Deal massive damage
            target.hurt(level.damageSources().magic(), damage);
            
            // Apply debuffs
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WITHER,
                100,
                spellLevel - 1,
                false,
                true
            ));
            
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS,
                200,
                spellLevel,
                false,
                true
            ));
            
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                100,
                2,
                false,
                true
            ));
            
            // Knockback
            double dx = target.getX() - entity.getX();
            double dz = target.getZ() - entity.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > 0) {
                double knockback = 1.5;
                target.push(dx / distance * knockback, 0.5, dz / distance * knockback);
            }
            
            // Spawn impact particles on each target
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                    target.getX(), target.getY() + 1, target.getZ(),
                    3, 0.5, 0.5, 0.5, 0.0
                );
                
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                    target.getX(), target.getY() + 1, target.getZ(),
                    20, 0.5, 0.5, 0.5, 0.1
                );
            }
        }
        
        // Massive particle effect at caster
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Ground shockwave
            for (int i = 0; i < 100; i++) {
                double angle = (i / 100.0) * Math.PI * 2;
                for (int r = 1; r <= radius; r++) {
                    double x = entity.getX() + Math.cos(angle) * r;
                    double z = entity.getZ() + Math.sin(angle) * r;
                    
                    serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                        x, entity.getY() + 0.1, z,
                        1, 0.1, 0.1, 0.1, 0.02
                    );
                }
            }
            
            // Upward explosion
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                entity.getX(), entity.getY() + 1, entity.getZ(),
                5, 0.0, 0.0, 0.0, 0.0
            );
        }
        
    }

    @Override
    public ResourceLocation getSpellResource() {
        return ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "gaia_wrath");
    }

}
