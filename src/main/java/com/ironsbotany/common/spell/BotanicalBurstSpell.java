package com.ironsbotany.common.spell;

import com.ironsbotany.IronsBotany;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import com.ironsbotany.common.registry.IBSchools;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import com.ironsbotany.common.spell.catalyst.SpellContext;

import java.util.List;

public class BotanicalBurstSpell extends AbstractBotanicalSpell {
    
    public BotanicalBurstSpell() {
        super(20000, 10000);
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 30;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.COMMON)
                .setSchoolResource(IBSchools.BOTANY_RESOURCE)
                .setMaxLevel(8)
                .setCooldownSeconds(10)
                .build();
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("spell.ironsbotany.botanical_burst.description"));
    }

    @Override
    protected void executeBotanicalEffect(Level level, int spellLevel, LivingEntity entity,
                                                 CastSource castSource, MagicData playerMagicData,
                                                 SpellContext context) {
        if (level.isClientSide) {
            return;
        }

        // Get modified damage from context
        float baseDamage = getSpellPower(spellLevel, entity);
        float damage = context.getModifiedDamage(baseDamage);
        
        // Get projectile speed from context
        Float speedMultiplier = context.getCustomData("projectile_speed_multiplier", Float.class);
        float speed = speedMultiplier != null ? speedMultiplier : 1.5f;
        
        // Create custom botanical burst projectile
        com.ironsbotany.common.entity.BotanicalBurstProjectile projectile = 
            new com.ironsbotany.common.entity.BotanicalBurstProjectile(level, entity, damage);
        
        // Apply context modifications
        if (context.isPiercing()) {
            Integer maxPierce = context.getCustomData("max_pierce", Integer.class);
            if (maxPierce != null) {
                // Store pierce data in projectile NBT for now
                projectile.getPersistentData().putInt("max_pierce", maxPierce);
                projectile.getPersistentData().putBoolean("piercing", true);
            }
        }
        
        // Set position and velocity
        projectile.setPos(entity.getX(), entity.getEyeY() - 0.1, entity.getZ());
        projectile.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, speed, 1.0F);
        
        level.addFreshEntity(projectile);
        
        // Spawn additional projectiles if context specifies
        int additionalProjectiles = context.getAdditionalProjectiles();
        for (int i = 0; i < additionalProjectiles; i++) {
            com.ironsbotany.common.entity.BotanicalBurstProjectile extra = 
                new com.ironsbotany.common.entity.BotanicalBurstProjectile(level, entity, damage * 0.7f);
            
            // Spread pattern
            float angleOffset = (i + 1) * 15.0f * (i % 2 == 0 ? 1 : -1);
            extra.setPos(entity.getX(), entity.getEyeY() - 0.1, entity.getZ());
            extra.shootFromRotation(entity, entity.getXRot(), 
                                   entity.getYRot() + angleOffset, 
                                   0.0F, speed, 1.0F);
            
            level.addFreshEntity(extra);
        }
    }

    @Override
    public ResourceLocation getSpellResource() {
        return new ResourceLocation(IronsBotany.MODID, "botanical_burst");
    }

}
