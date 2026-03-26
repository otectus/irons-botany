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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TerrasteelSpellBladeItem extends SwordItem {
    private static final UUID SPELL_POWER_UUID = UUID.fromString("a8b3c4d5-e6f7-8901-2345-6789abcdef01");
    private static final UUID MAX_MANA_UUID = UUID.fromString("b9c4d5e6-f7a8-9012-3456-789abcdef012");
    
    public TerrasteelSpellBladeItem(Properties properties) {
        super(TerrasteelTier.INSTANCE, 3, -2.4F, properties);
    }

    private static final UUID MANA_COST_UUID = UUID.fromString("c0d5e6f7-a8b9-0123-4567-89abcdef0123");

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(super.getDefaultAttributeModifiers(slot));

            // Add spell power (+25%)
            builder.put(AttributeRegistry.SPELL_POWER.get(),
                    new AttributeModifier(SPELL_POWER_UUID, "Terrasteel Spell Power",
                            CommonConfig.TERRASTEEL_BLADE_SPELL_POWER.get(),
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

            // Add max mana (+200)
            builder.put(AttributeRegistry.MAX_MANA.get(),
                    new AttributeModifier(MAX_MANA_UUID, "Terrasteel Max Mana",
                            CommonConfig.TERRASTEEL_BLADE_MAX_MANA.get(),
                            AttributeModifier.Operation.ADDITION));

            // Cooldown reduction (-20%)
            builder.put(AttributeRegistry.COOLDOWN_REDUCTION.get(),
                    new AttributeModifier(MANA_COST_UUID, "Terrasteel Cooldown Reduction",
                            CommonConfig.TERRASTEEL_BLADE_MANA_COST_REDUCTION.get(),
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

            return builder.build();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(""));
        int spellPowerPercent = (int) (CommonConfig.TERRASTEEL_BLADE_SPELL_POWER.get() * 100);
        int maxMana = CommonConfig.TERRASTEEL_BLADE_MAX_MANA.get();
        int cooldownPercent = (int) (CommonConfig.TERRASTEEL_BLADE_MANA_COST_REDUCTION.get() * 100);
        tooltip.add(Component.literal("+" + spellPowerPercent + "% All Spell Power").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("+" + maxMana + " Max Mana").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("-" + cooldownPercent + "% Cooldown").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.ironsbotany.terrasteel_spell_blade.tooltip")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private static class TerrasteelTier implements Tier {
        public static final TerrasteelTier INSTANCE = new TerrasteelTier();

        @Override
        public int getUses() {
            return 3000;
        }

        @Override
        public float getSpeed() {
            return 9.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 7.0F;
        }

        @Override
        public int getLevel() {
            return 4;
        }

        @Override
        public int getEnchantmentValue() {
            return 20;
        }

        @Override
        public Ingredient getRepairIngredient() {
            net.minecraft.world.item.Item terrasteel = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                    net.minecraft.resources.ResourceLocation.tryParse("botania:terrasteel_ingot"));
            return terrasteel != null ? Ingredient.of(terrasteel) : Ingredient.EMPTY;
        }
    }
}
