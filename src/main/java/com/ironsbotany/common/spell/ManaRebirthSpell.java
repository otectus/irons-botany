package com.ironsbotany.common.spell;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.util.DataKeys;
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

public class ManaRebirthSpell extends AbstractBotanicalSpell {
    public ManaRebirthSpell() {
        super(50000, 25000);
        this.manaCostPerLevel = 40;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 30;
        this.baseManaCost = 50;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.EPIC)
                .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
                .setMaxLevel(5)
                .setCooldownSeconds(300)
                .build();
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("spell.ironsbotany.mana_rebirth.description"));
    }

    @Override
    protected void executeBotanicalEffect(Level level, int spellLevel, LivingEntity entity,
                                                 CastSource castSource, MagicData playerMagicData,
                                                 SpellContext context) {
        if (level.isClientSide) {
            return;
        }

        // Heal the caster
        float healAmount = 4.0f + (spellLevel * 2.0f); // 6-14 hearts
        entity.heal(healAmount);

        // Remove negative effects
        entity.removeEffect(net.minecraft.world.effect.MobEffects.POISON);
        entity.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
        entity.removeEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);
        entity.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);

        // Apply regeneration
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.REGENERATION,
            200,
            spellLevel,
            false,
            true
        ));

        // Apply absorption
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.ABSORPTION,
            600,
            spellLevel - 1,
            false,
            true
        ));

        // Apply death prevention buff via persistent data
        if (com.ironsbotany.common.config.CommonConfig.MANA_REBIRTH_DEATH_PREVENTION.get()) {
            int buffDuration = 600 + (spellLevel * 600); // 30-90 seconds in ticks
            entity.getPersistentData().putInt(DataKeys.MANA_REBIRTH_LEVEL, spellLevel);
            entity.getPersistentData().putLong(DataKeys.MANA_REBIRTH_EXPIRY,
                level.getGameTime() + buffDuration);
        }

        // Spawn healing particles
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.HEART,
                entity.getX(), entity.getY() + 1, entity.getZ(),
                20, 0.5, 0.5, 0.5, 0.1
            );

            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                entity.getX(), entity.getY() + 1, entity.getZ(),
                30, 0.5, 0.5, 0.5, 0.2
            );
        }

    }

    @Override
    public ResourceLocation getSpellResource() {
        return ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "mana_rebirth");
    }

}
