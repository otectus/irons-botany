package com.ironsbotany.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.ironsbotany.common.config.CommonConfig;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Generic, config-driven melee mage sword for the Botania-tiered ladder
 * (Elementium → Gaia). One class parameterized by {@link Tier}; the existing
 * Terrasteel Spell Blade keeps its own {@link TerrasteelSpellBladeItem}, so the
 * full melee ladder is Elementium → Terrasteel → Gaia.
 *
 * <p>Each sword adds spell power / max mana / cooldown reduction while held and
 * generates Botania mana on a qualifying hit (see {@link ManaGeneratingWeapon}
 * and {@code TerrasteelBladeHandler}). Mirrors the armor approach in
 * {@link MageArmorItem}.
 */
public class MageSwordItem extends SwordItem implements ManaGeneratingWeapon {

    /** A rung of the melee mage-sword ladder (the Terrasteel rung is the Spell Blade). */
    public enum Tier implements net.minecraft.world.item.Tier {
        ELEMENTIUM("elementium_mage_sword", 2000, 8.0F, 6.0F, 3, 18, "botania:elementium_ingot"),
        GAIA("gaia_mage_sword", 4000, 9.0F, 9.0F, 4, 22, "botania:gaia_ingot");

        final String id;
        private final int uses;
        private final float speed;
        private final float attackDamageBonus;
        private final int level;
        private final int enchantmentValue;
        private final String repairItemId;

        Tier(String id, int uses, float speed, float attackDamageBonus, int level,
             int enchantmentValue, String repairItemId) {
            this.id = id;
            this.uses = uses;
            this.speed = speed;
            this.attackDamageBonus = attackDamageBonus;
            this.level = level;
            this.enchantmentValue = enchantmentValue;
            this.repairItemId = repairItemId;
        }

        // --- SwordItem base stats ---
        int baseDamage() {
            return this == GAIA ? 4 : 3;
        }

        float attackSpeed() {
            return -2.4F;
        }

        // --- Config-ref accessors ---
        ForgeConfigSpec.DoubleValue spellPower() {
            return this == GAIA ? CommonConfig.GAIA_SWORD_SPELL_POWER : CommonConfig.ELEMENTIUM_SWORD_SPELL_POWER;
        }

        ForgeConfigSpec.IntValue maxMana() {
            return this == GAIA ? CommonConfig.GAIA_SWORD_MAX_MANA : CommonConfig.ELEMENTIUM_SWORD_MAX_MANA;
        }

        ForgeConfigSpec.DoubleValue cooldownReduction() {
            return this == GAIA ? CommonConfig.GAIA_SWORD_COOLDOWN_REDUCTION : CommonConfig.ELEMENTIUM_SWORD_COOLDOWN_REDUCTION;
        }

        ForgeConfigSpec.IntValue manaPerHit() {
            return this == GAIA ? CommonConfig.GAIA_SWORD_MANA_PER_HIT : CommonConfig.ELEMENTIUM_SWORD_MANA_PER_HIT;
        }

        // --- net.minecraft.world.item.Tier ---
        @Override public int getUses() { return uses; }
        @Override public float getSpeed() { return speed; }
        @Override public float getAttackDamageBonus() { return attackDamageBonus; }
        @Override public int getLevel() { return level; }
        @Override public int getEnchantmentValue() { return enchantmentValue; }
        @Override public Ingredient getRepairIngredient() {
            net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                    net.minecraft.resources.ResourceLocation.tryParse(repairItemId));
            return item != null ? Ingredient.of(item) : Ingredient.EMPTY;
        }
    }

    private final Tier tier;

    public MageSwordItem(Tier tier, Properties properties) {
        super(tier, tier.baseDamage(), tier.attackSpeed(), properties);
        this.tier = tier;
    }

    private UUID uuidFor(String attrKey) {
        return UUID.nameUUIDFromBytes(
                ("ironsbotany:mage_sword:" + tier.id + ":" + attrKey).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int getManaPerHit() {
        return tier.manaPerHit().get();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(super.getDefaultAttributeModifiers(slot));

        builder.put(AttributeRegistry.SPELL_POWER.get(),
                new AttributeModifier(uuidFor("spell_power"), tier.id + " Spell Power",
                        tier.spellPower().get(), AttributeModifier.Operation.MULTIPLY_TOTAL));

        builder.put(AttributeRegistry.MAX_MANA.get(),
                new AttributeModifier(uuidFor("max_mana"), tier.id + " Max Mana",
                        tier.maxMana().get(), AttributeModifier.Operation.ADDITION));

        builder.put(AttributeRegistry.COOLDOWN_REDUCTION.get(),
                new AttributeModifier(uuidFor("cooldown"), tier.id + " Cooldown Reduction",
                        tier.cooldownReduction().get(), AttributeModifier.Operation.MULTIPLY_TOTAL));

        return builder.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(""));
        int spellPowerPercent = (int) (tier.spellPower().get() * 100);
        int maxMana = tier.maxMana().get();
        int cooldownPercent = (int) (tier.cooldownReduction().get() * 100);
        tooltip.add(Component.literal("+" + spellPowerPercent + "% All Spell Power").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("+" + maxMana + " Max Mana").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("-" + cooldownPercent + "% Cooldown").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.ironsbotany." + tier.id + ".tooltip")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
