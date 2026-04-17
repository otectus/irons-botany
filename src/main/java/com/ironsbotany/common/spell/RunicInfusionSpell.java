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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import com.ironsbotany.common.spell.catalyst.SpellContext;

import java.util.List;

public class RunicInfusionSpell extends AbstractBotanicalSpell {
    public RunicInfusionSpell() {
        super(50000, 20000);
        this.manaCostPerLevel = 30;
        this.baseSpellPower = 15;
        this.spellPowerPerLevel = 4;
        this.castTime = 30;
        this.baseManaCost = 60;
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
                .setMaxLevel(10)
                .setCooldownSeconds(60)
                .build();
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("spell.ironsbotany.runic_infusion.description"));
    }

    @Override
    protected void executeBotanicalEffect(Level level, int spellLevel, LivingEntity entity,
                                                 CastSource castSource, MagicData playerMagicData,
                                                 SpellContext context) {
        if (level.isClientSide) {
            return;
        }

        int duration = 200 + (spellLevel * 40); // 10-30 seconds
        int amplifier = spellLevel - 1;

        // Scan inventory for Botania runes if enabled
        int runeBonus = 0;
        boolean hasFireRune = false;
        boolean hasWaterRune = false;
        boolean hasEarthRune = false;
        boolean hasAirRune = false;
        boolean hasManaRune = false;

        if (CommonConfig.RUNIC_INFUSION_RUNE_SCALING.get() && entity instanceof Player player) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) continue;

                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (itemId == null) continue;
                String path = itemId.toString();

                if (path.contains("botania:rune_fire") || path.contains("botania:rune_lust")) {
                    hasFireRune = true; runeBonus++;
                } else if (path.contains("botania:rune_water") || path.contains("botania:rune_sloth")) {
                    hasWaterRune = true; runeBonus++;
                } else if (path.contains("botania:rune_earth") || path.contains("botania:rune_greed")) {
                    hasEarthRune = true; runeBonus++;
                } else if (path.contains("botania:rune_air") || path.contains("botania:rune_wrath")) {
                    hasAirRune = true; runeBonus++;
                } else if (path.contains("botania:rune_mana") || path.contains("botania:rune_pride")) {
                    hasManaRune = true; runeBonus++;
                }
            }
            // Diminishing returns after 3 unique runes
            runeBonus = Math.min(runeBonus, 5);
        }

        // Duration bonus from Mana rune
        int durationBonus = hasManaRune ? (int) (duration * 0.5) : 0;
        int totalDuration = duration + durationBonus;

        // Apply base buffs (scaled by rune count)
        int bonusAmplifier = Math.min(runeBonus / 2, 2);

        // Strength (capped at V to prevent +33 damage at max level)
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.DAMAGE_BOOST,
            totalDuration,
            Math.min(amplifier + bonusAmplifier, 4),
            false, true
        ));

        // Speed (capped at IV)
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
            totalDuration,
            Math.min(amplifier + (hasAirRune ? 1 : 0), 3),
            false, true
        ));

        // Regeneration (base)
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.REGENERATION,
            totalDuration,
            Math.min(amplifier + (hasWaterRune ? 1 : 0), 3),
            false, true
        ));

        // Rune-specific bonuses
        if (hasFireRune) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE,
                totalDuration, 0, false, true
            ));
        }

        if (hasWaterRune) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WATER_BREATHING,
                totalDuration, 0, false, true
            ));
        }

        if (hasEarthRune) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                totalDuration, Math.min(amplifier, 2), false, true
            ));
        }

        if (hasAirRune) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.JUMP,
                totalDuration, Math.min(amplifier, 2), false, true
            ));
        }

        // Spawn runic particles (more particles with more runes)
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            int particleCount = 30 + (runeBonus * 10);
            for (int i = 0; i < particleCount; i++) {
                double angle = (i / (double) particleCount) * Math.PI * 2;
                double x = entity.getX() + Math.cos(angle) * 1.0;
                double z = entity.getZ() + Math.sin(angle) * 1.0;
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    x, entity.getY() + 1, z,
                    3, 0.1, 0.5, 0.1, 0.1
                );
            }

            // Extra portal particles if runes are present
            if (runeBonus > 0) {
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.PORTAL,
                    entity.getX(), entity.getY() + 1, entity.getZ(),
                    runeBonus * 10, 0.5, 0.5, 0.5, 0.3
                );
            }

            // Mod signature: mana transfer swirl inward on caster
            if (CommonConfig.ENABLE_SPELL_PARTICLES.get()) {
                serverLevel.sendParticles(
                    com.ironsbotany.common.registry.IBParticles.MANA_TRANSFER.get(),
                    entity.getX(), entity.getY() + 1, entity.getZ(),
                    20 + (runeBonus * 4), 0.8, 1.0, 0.8, 0.04
                );
            }
        }

    }

    @Override
    public ResourceLocation getSpellResource() {
        return new ResourceLocation(IronsBotany.MODID, "runic_infusion");
    }

}
