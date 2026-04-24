package com.ironsbotany.common.spell;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.alfheim.AlfheimSpellBoost;
import com.ironsbotany.common.alfheim.SpellbookAttunement;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ConfigHelper;
import com.ironsbotany.common.config.ManaUnificationMode;
import com.ironsbotany.common.corporea.SpellCircleReagentSystem;
import com.ironsbotany.common.network.PacketHandler;
import com.ironsbotany.common.network.SpellCastSyncPacket;
import com.ironsbotany.common.registry.IBSchools;
import com.ironsbotany.common.spell.SpellManaNetworkIntegration;
import com.ironsbotany.common.flower.ActiveFlowerAura;
import com.ironsbotany.common.flower.FlowerAuraRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import com.ironsbotany.common.spell.catalyst.CatalystEffect;
import com.ironsbotany.common.spell.catalyst.SpellCatalystRegistry;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import com.ironsbotany.common.util.DataKeys;
import com.ironsbotany.common.util.ManaHelper;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.List;

public abstract class AbstractBotanicalSpell extends AbstractSpell {
    
    protected final int baseBotaniaManaCost;
    protected final int botaniaManaCostPerLevel;

    public AbstractBotanicalSpell(int baseBotaniaCost, int botaniaCostPerLevel) {
        this.baseBotaniaManaCost = baseBotaniaCost;
        this.botaniaManaCostPerLevel = botaniaCostPerLevel;
    }

    /**
     * Calculate Botania mana cost for a given spell level
     */
    public int getBotaniaManaCost(int spellLevel) {
        return baseBotaniaManaCost + (botaniaManaCostPerLevel * (spellLevel - 1));
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level.isClientSide) {
            return;
        }

        // Create spell context
        SpellContext context = new SpellContext(level, entity, spellLevel, castSource);

        // Track catalysts and auras for use in post-cast logic
        List<CatalystEffect> catalysts = List.of();
        List<ActiveFlowerAura> auras = List.of();

        // Player-specific pre-cast logic
        if (entity instanceof Player player) {
            // Get active catalysts from player inventory
            catalysts = SpellCatalystRegistry.getActiveCatalysts(player);

            // Apply catalyst effects
            SpellCatalystRegistry.applyCatalysts(this, context, catalysts);

            // Consume or damage catalysts based on config
            if (!catalysts.isEmpty()) {
                consumeCatalysts(player, catalysts);
            }

            // Apply flower auras
            auras = FlowerAuraRegistry.getActiveAuras(player, 16);
            for (ActiveFlowerAura activeAura : auras) {
                if (activeAura.getAura().appliesTo(this)) {
                    activeAura.getAura().applyToSpell(context, activeAura.getStrength());
                }
            }

            // Write ACTIVE_AURAS flag for GaiaSpellTrials
            player.getPersistentData().putBoolean(DataKeys.ACTIVE_AURAS, !auras.isEmpty());

            // Apply casting channel modifiers from held items
            if (ConfigHelper.areChannelsEnabled()) {
                com.ironsbotany.common.casting.CastingChannel channel =
                    com.ironsbotany.common.casting.CastingChannelRegistry.getChannelForItem(player.getMainHandItem());
                if (channel == null) {
                    channel = com.ironsbotany.common.casting.CastingChannelRegistry.getChannelForItem(player.getOffhandItem());
                }
                if (channel != null && channel.canCast(this, player)) {
                    context.multiplyDamage(channel.getBurstDamageMultiplier());
                    context.multiplyCooldown(channel.getCooldownMultiplier());
                    context.multiplyCastingSpeed(channel.getCastingSpeedMultiplier());
                }
            }

            // Apply Alfheim portal proximity boost
            AlfheimSpellBoost.applyAlfheimBoost(context, this, player);

            // Apply spellbook attunement bonuses
            if (ConfigHelper.isAlfheimEnabled()) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();
                ItemStack spellbook = SpellbookAttunement.isAttuned(mainHand) ? mainHand :
                                      SpellbookAttunement.isAttuned(offHand) ? offHand : ItemStack.EMPTY;
                if (!spellbook.isEmpty()) {
                    context.multiplyCooldown(1.0f - SpellbookAttunement.getCooldownReduction(spellbook));
                    context.multiplyManaCost(1.0f - SpellbookAttunement.getManaCostReduction(spellbook));
                    context.multiplyDamage(1.0f + SpellbookAttunement.getSpellPowerBonus(spellbook));
                }
            }

            // Check Corporea logistics for ritual spells
            if (!SpellCircleReagentSystem.prepareSpellCircle(player, this, spellLevel)) {
                player.displayClientMessage(
                    Component.translatable("ironsbotany.spell.missing_reagents"),
                    true);
                return;
            }

            // Handle mana costs based on unification mode
            ManaUnificationMode manaMode = CommonConfig.MANA_UNIFICATION_MODE.get();

            if (manaMode == ManaUnificationMode.DISABLED) {
                // No mana integration - spell proceeds normally with ISS mana only
            } else if (manaMode == ManaUnificationMode.BOTANIA_PRIMARY) {
                // ISS spells consume Botania mana directly
                int botaniaRequired = context.getModifiedManaCost(getBotaniaManaCost(spellLevel));
                if (!ManaHelper.hasBotaniaMana(player, botaniaRequired)) {
                    player.displayClientMessage(
                            Component.translatable("ironsbotany.spell.insufficient_botania_mana",
                                    botaniaRequired),
                            true);
                    return;
                }
                if (!ManaHelper.drainBotaniaMana(player, botaniaRequired)) {
                    return;
                }
            } else if (manaMode == ManaUnificationMode.ISS_PRIMARY) {
                // Botania mana is converted to ISS mana automatically
                // No additional cost here - conversion happens passively
            } else if (manaMode == ManaUnificationMode.SEPARATE ||
                       (manaMode == ManaUnificationMode.HYBRID && CommonConfig.ENABLE_DUAL_COST_SPELLS.get())) {
                // Dual-cost: require both Botania and ISS mana
                int botaniaRequired = context.getModifiedManaCost(getBotaniaManaCost(spellLevel));
                if (!ManaHelper.hasBotaniaMana(player, botaniaRequired)) {
                    player.displayClientMessage(
                            Component.translatable("ironsbotany.spell.insufficient_botania_mana",
                                    botaniaRequired),
                            true);
                    return;
                }
                if (!ManaHelper.drainBotaniaMana(player, botaniaRequired)) {
                    return;
                }
            }

            // Show catalyst activation effects
            if (!catalysts.isEmpty()) {
                showCatalystActivation(player, catalysts);
            }

            // Show aura activation effects
            if (!auras.isEmpty() && CommonConfig.SHOW_AURA_PARTICLES.get()) {
                showAuraActivation(player, auras);
            }
        }

        // Cap total damage multiplier to prevent extreme stacking from catalysts + auras + environment
        context.capDamageMultiplier(5.0f);

        // Execute spell effect for ANY LivingEntity
        executeBotanicalEffect(level, spellLevel, entity, castSource, playerMagicData, context);

        // Apply additional effects from catalysts/auras to caster
        for (net.minecraft.world.effect.MobEffectInstance effect : context.getAdditionalEffects()) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect));
        }

        // Player-specific post-cast logic
        if (entity instanceof Player player) {
            // Store last spell cast for cross-system tracking (e.g., Gaia trials)
            player.getPersistentData().putString(DataKeys.LAST_SPELL_ID, this.getSpellId());
            player.getPersistentData().putLong(DataKeys.LAST_SPELL_TIME, level.getGameTime());

            // Grant advancements based on spell state
            if (player instanceof ServerPlayer serverPlayer) {
                grantAdvancement(serverPlayer, "first_spell", "cast_botanical_spell");

                // Spell mastery: level 5+
                if (spellLevel >= 5) {
                    grantAdvancement(serverPlayer, "spell_mastery", "cast_high_level");
                }

                // Track unique spells for "Full Garden"
                String uniqueKey = DataKeys.UNIQUE_SPELLS;
                String spellId = this.getSpellId();
                net.minecraft.nbt.ListTag spellList = player.getPersistentData().getList(uniqueKey, net.minecraft.nbt.Tag.TAG_STRING);
                boolean found = false;
                for (int i = 0; i < spellList.size(); i++) {
                    if (spellList.getString(i).equals(spellId)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    spellList.add(net.minecraft.nbt.StringTag.valueOf(spellId));
                    player.getPersistentData().put(uniqueKey, spellList);
                    if (spellList.size() >= 9) {
                        grantAdvancement(serverPlayer, "all_spells", "cast_all_spells");
                    }
                }

                // Catalyst advancement
                if (!catalysts.isEmpty()) {
                    grantAdvancement(serverPlayer, "first_catalyst", "used_catalyst");
                    for (CatalystEffect catalyst : catalysts) {
                        if (catalyst.getTier() == CatalystEffect.CatalystTier.LEGENDARY) {
                            grantAdvancement(serverPlayer, "legendary_catalyst", "legendary_used");
                        }
                    }
                }

                // Aura advancement
                if (!auras.isEmpty()) {
                    grantAdvancement(serverPlayer, "first_aura", "near_aura");
                }

                // Alfheim advancement
                if (AlfheimSpellBoost.isNearAlfheimPortal(player)) {
                    grantAdvancement(serverPlayer, "alfheim_cast", "cast_in_alfheim");
                }
            }

            // Broadcast spell cast visuals to nearby players
            PacketHandler.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                    player.getX(), player.getY(), player.getZ(), 32, level.dimension())),
                new SpellCastSyncPacket(player.blockPosition(), this.getSpellId()));
        }

        // Trigger mana network effects if enabled (Stage 4: Spell-Triggered Mana Events)
        if (ConfigHelper.areManaEventsEnabled()) {
            SpellManaNetworkIntegration.triggerManaNetworkEffects(this, entity, spellLevel);
        }
    }

    /**
     * Override this method to implement the actual spell effect
     * @param context Spell context with catalyst modifications
     */
    protected abstract void executeBotanicalEffect(Level level, int spellLevel, LivingEntity entity,
                                                    CastSource castSource, MagicData playerMagicData,
                                                    SpellContext context);
    
    /**
     * Show visual feedback for catalyst activation
     */
    private void showCatalystActivation(Player player, List<CatalystEffect> catalysts) {
        if (player.level() instanceof ServerLevel serverLevel) {
            // Spawn particles for each catalyst tier
            for (CatalystEffect catalyst : catalysts) {
                ParticleOptions particle = switch (catalyst.getTier()) {
                    case BASIC -> ParticleTypes.ENCHANT;
                    case ADVANCED -> ParticleTypes.PORTAL;
                    case ELITE -> ParticleTypes.END_ROD;
                    case LEGENDARY -> ParticleTypes.DRAGON_BREATH;
                };
                
                serverLevel.sendParticles(particle,
                    player.getX(), player.getY() + 1, player.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1);
            }
            
            // Play sound based on highest tier
            CatalystEffect.CatalystTier highestTier = catalysts.stream()
                .map(CatalystEffect::getTier)
                .max(Comparator.naturalOrder())
                .orElse(CatalystEffect.CatalystTier.BASIC);
            
            SoundEvent sound = switch (highestTier) {
                case BASIC -> SoundEvents.ENCHANTMENT_TABLE_USE;
                case ADVANCED -> SoundEvents.AMETHYST_BLOCK_CHIME;
                case ELITE -> SoundEvents.END_PORTAL_SPAWN;
                case LEGENDARY -> SoundEvents.ENDER_DRAGON_GROWL;
            };
            
            serverLevel.playSound(null, player.blockPosition(), sound,
                SoundSource.PLAYERS, 0.5f, 1.0f);
        }
    }
    
    /**
     * Show visual feedback for flower aura activation
     */
    private void showAuraActivation(Player player, List<ActiveFlowerAura> auras) {
        if (player.level() instanceof ServerLevel serverLevel) {
            for (ActiveFlowerAura activeAura : auras) {
                // Spawn particles from flower to player
                BlockPos flowerPos = activeAura.getPosition();
                Vec3 start = new Vec3(flowerPos.getX() + 0.5, 
                                      flowerPos.getY() + 0.5, 
                                      flowerPos.getZ() + 0.5);
                Vec3 end = player.position().add(0, 1, 0);
                
                // Particle trail
                for (int i = 0; i < 10; i++) {
                    double t = i / 10.0;
                    Vec3 pos = start.lerp(end, t);
                    
                    serverLevel.sendParticles(
                        activeAura.getAura().getParticle(),
                        pos.x, pos.y, pos.z,
                        1, 0, 0, 0, 0
                    );
                }
            }
        }
    }

    /**
     * Grant an advancement to a player if not already granted
     */
    private static void grantAdvancement(ServerPlayer player, String advancementId, String criterionKey) {
        var advancement = player.server.getAdvancements()
            .getAdvancement(new ResourceLocation(IronsBotany.MODID, advancementId));
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterionKey);
        }
    }

    /**
     * Consume or damage catalyst items based on config settings
     */
    private void consumeCatalysts(Player player, List<CatalystEffect> catalysts) {
        double consumeChance = CommonConfig.CATALYST_CONSUMPTION_CHANCE.get();
        int durabilityDmg = CommonConfig.CATALYST_DURABILITY_DAMAGE.get();
        if (consumeChance <= 0 && durabilityDmg <= 0) return;

        for (CatalystEffect catalyst : catalysts) {
            Item catalystItem = SpellCatalystRegistry.getItemForCatalyst(catalyst);
            if (catalystItem == null) continue;

            // Find the item in the player's inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(catalystItem)) {
                    if (consumeChance > 0 && player.getRandom().nextDouble() < consumeChance) {
                        stack.shrink(1);
                    } else if (durabilityDmg > 0 && stack.isDamageableItem()) {
                        stack.hurtAndBreak(durabilityDmg, player, p -> {});
                    }
                    break;
                }
            }
        }
    }

    @Override
    public SchoolType getSchoolType() {
        if (CommonConfig.ENABLE_BOTANICAL_SCHOOL.get()) {
            SchoolType botany = IBSchools.BOTANY.get();
            if (botany != null) {
                return botany;
            }
        }
        return SchoolRegistry.NATURE.get();
    }
}
