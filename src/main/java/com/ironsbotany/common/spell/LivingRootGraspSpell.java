package com.ironsbotany.common.spell;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
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

public class LivingRootGraspSpell extends AbstractBotanicalSpell {
    public LivingRootGraspSpell() {
        super(15000, 7500);
        this.manaCostPerLevel = 12;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 2;
        this.castTime = 15;
        this.baseManaCost = 25;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.COMMON)
                .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
                .setMaxLevel(6)
                .setCooldownSeconds(15)
                .build();
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("spell.ironsbotany.living_root_grasp.description"));
    }

    @Override
    protected void executeBotanicalEffect(Level level, int spellLevel, LivingEntity entity,
                                                 CastSource castSource, MagicData playerMagicData,
                                                 SpellContext context) {
        if (level.isClientSide) {
            return;
        }

        int radius = 3 + spellLevel;
        int duration = 60 + (spellLevel * 20); // 3-9 seconds
        
        // Find nearby enemies
        java.util.List<LivingEntity> targets = level.getEntitiesOfClass(
            LivingEntity.class,
            entity.getBoundingBox().inflate(radius),
            target -> target != entity && !target.isAlliedTo(entity)
        );
        
        for (LivingEntity target : targets) {
            // Apply true immobilization or strong slowness
            int slowLevel = CommonConfig.LIVING_ROOT_IMMOBILIZE.get() ? 10 : 3;
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                duration,
                slowLevel,
                false,
                true
            ));

            // Apply weakness
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS,
                duration,
                spellLevel - 1,
                false,
                true
            ));

            // Sync the slowness effect immediately
            target.hurtMarked = true;

            // Spawn vine/root particles at feet in a ring
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                // Root burst at feet
                for (int i = 0; i < 16; i++) {
                    double angle = (i / 16.0) * Math.PI * 2;
                    double px = target.getX() + Math.cos(angle) * 0.5;
                    double pz = target.getZ() + Math.sin(angle) * 0.5;
                    serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.COMPOSTER,
                        px, target.getY(), pz,
                        3, 0.1, 0.2, 0.1, 0.02
                    );
                }

                // Vine growth particles rising upward
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + 0.5, target.getZ(),
                    15, 0.3, 0.5, 0.3, 0.05
                );
            }
        }
        
    }

    @Override
    public ResourceLocation getSpellResource() {
        return ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "living_root_grasp");
    }

}
