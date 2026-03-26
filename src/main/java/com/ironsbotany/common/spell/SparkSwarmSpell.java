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

public class SparkSwarmSpell extends AbstractBotanicalSpell {
    public SparkSwarmSpell() {
        super(40000, 15000);
        this.manaCostPerLevel = 25;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 3;
        this.castTime = 20;
        this.baseManaCost = 50;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.RARE)
                .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
                .setMaxLevel(7)
                .setCooldownSeconds(40)
                .build();
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("spell.ironsbotany.spark_swarm.description"));
    }

    @Override
    protected void executeBotanicalEffect(Level level, int spellLevel, LivingEntity entity,
                                                 CastSource castSource, MagicData playerMagicData,
                                                 SpellContext context) {
        if (level.isClientSide) {
            return;
        }

        int sparkCount = 2 + spellLevel; // 3-9 sparks
        int lifetime = 100 + (spellLevel * 20); // 5-13 seconds
        
        for (int i = 0; i < sparkCount; i++) {
            com.ironsbotany.common.entity.SparkSwarmEntity spark = 
                new com.ironsbotany.common.entity.SparkSwarmEntity(level, entity, lifetime);
            
            // Spawn in a circle around the caster
            double angle = (i / (double) sparkCount) * Math.PI * 2;
            double radius = 2.0;
            double x = entity.getX() + Math.cos(angle) * radius;
            double z = entity.getZ() + Math.sin(angle) * radius;
            
            spark.setPos(x, entity.getY() + 1.5, z);
            level.addFreshEntity(spark);
            
            // Spawn summoning particles
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                    x, entity.getY() + 1.5, z,
                    20, 0.3, 0.3, 0.3, 0.1
                );
            }
        }
        
    }

    @Override
    public ResourceLocation getSpellResource() {
        return ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "spark_swarm");
    }

}
