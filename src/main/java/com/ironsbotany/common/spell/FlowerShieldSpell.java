package com.ironsbotany.common.spell;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.util.DataKeys;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class FlowerShieldSpell extends AbstractBotanicalSpell {
    
    public FlowerShieldSpell() {
        super(30000, 15000);
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 3;
        this.castTime = 10;
        this.baseManaCost = 40;
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
                .setMaxLevel(10)
                .setCooldownSeconds(20)
                .build();
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("spell.ironsbotany.flower_shield.description"));
    }

    @Override
    protected void executeBotanicalEffect(Level level, int spellLevel, LivingEntity entity,
                                                 CastSource castSource, MagicData playerMagicData,
                                                 SpellContext context) {
        if (level.isClientSide) {
            return;
        }

        int duration = 100 + (spellLevel * 20); // 5-15 seconds

        // Apply custom petal barrier via persistent data
        int shieldHp = com.ironsbotany.common.config.CommonConfig.FLOWER_SHIELD_BASE_HP.get()
            + (spellLevel * com.ironsbotany.common.config.CommonConfig.FLOWER_SHIELD_HP_PER_LEVEL.get());
        entity.getPersistentData().putInt(DataKeys.FLOWER_SHIELD_HP, shieldHp);
        entity.getPersistentData().putLong(DataKeys.FLOWER_SHIELD_EXPIRY,
            level.getGameTime() + duration);

        // Shield is the petal HP absorption buffer only — no Resistance stacking
        // Breaking the shield returns the player to full damage exposure

        // Spawn petal barrier particles
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (int i = 0; i < 30; i++) {
                double angle = (i / 30.0) * Math.PI * 2;
                double x = entity.getX() + Math.cos(angle) * 1.5;
                double z = entity.getZ() + Math.sin(angle) * 1.5;
                serverLevel.sendParticles(
                    com.ironsbotany.common.registry.IBParticles.PETAL_MAGIC.get(),
                    x, entity.getY() + 1, z,
                    8, 0.1, 0.5, 0.1, 0.02
                );
            }
            // Inner glow ring
            for (int i = 0; i < 20; i++) {
                double angle = (i / 20.0) * Math.PI * 2;
                double x = entity.getX() + Math.cos(angle) * 0.8;
                double z = entity.getZ() + Math.sin(angle) * 0.8;
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.END_ROD,
                    x, entity.getY() + 0.5, z,
                    2, 0, 0.3, 0, 0.01
                );
            }
        }

    }

    @Override
    public ResourceLocation getSpellResource() {
        return new ResourceLocation(IronsBotany.MODID, "flower_shield");
    }

}
