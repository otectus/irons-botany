package com.ironsbotany.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.registry.IBArmorMaterials;
import com.ironsbotany.common.registry.IBAttributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Generic, config-driven mage armor for the Botania-tiered ladder
 * (Elementium → Terrasteel → Gaia). One class parameterized by {@link Tier};
 * the entry-tier Manasteel set keeps its own {@link ManasteelWizardArmorItem}.
 *
 * <p>Each piece grants spell power and max mana (always) plus a tier-specific
 * "extra" modifier (mana efficiency, cooldown reduction). Set bonuses are
 * applied separately in {@code ArmorSetBonusHandler}. Defense/durability come
 * from the {@link IBArmorMaterials} material.
 */
public class MageArmorItem extends ArmorItem {

    /** A rung of the mage armor ladder above Manasteel. */
    public enum Tier {
        ELEMENTIUM("elementium_mage", IBArmorMaterials.ELEMENTIUM_MAGE),
        TERRASTEEL("terrasteel_mage", IBArmorMaterials.TERRASTEEL_MAGE),
        GAIA("gaia_mage", IBArmorMaterials.GAIA_MAGE);

        public final String id;
        public final IBArmorMaterials material;

        Tier(String id, IBArmorMaterials material) {
            this.id = id;
            this.material = material;
        }

        ForgeConfigSpec.DoubleValue spellPower() {
            return switch (this) {
                case ELEMENTIUM -> CommonConfig.ELEMENTIUM_ARMOR_SPELL_POWER;
                case TERRASTEEL -> CommonConfig.TERRASTEEL_ARMOR_SPELL_POWER;
                case GAIA -> CommonConfig.GAIA_ARMOR_SPELL_POWER;
            };
        }

        ForgeConfigSpec.IntValue maxMana() {
            return switch (this) {
                case ELEMENTIUM -> CommonConfig.ELEMENTIUM_ARMOR_MAX_MANA;
                case TERRASTEEL -> CommonConfig.TERRASTEEL_ARMOR_MAX_MANA;
                case GAIA -> CommonConfig.GAIA_ARMOR_MAX_MANA;
            };
        }
    }

    private final Tier tier;

    public MageArmorItem(Tier tier, Type type, Properties properties) {
        super(tier.material, type, properties);
        this.tier = tier;
    }

    private UUID uuidFor(EquipmentSlot slot, String attrKey) {
        return UUID.nameUUIDFromBytes(
                ("ironsbotany:mage_armor:" + tier.id + ":" + slot.getName() + ":" + attrKey)
                        .getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != this.type.getSlot()) {
            return super.getDefaultAttributeModifiers(slot);
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(super.getDefaultAttributeModifiers(slot));

        builder.put(AttributeRegistry.SPELL_POWER.get(),
                new AttributeModifier(uuidFor(slot, "spell_power"), tier.id + " Spell Power",
                        tier.spellPower().get(), AttributeModifier.Operation.MULTIPLY_TOTAL));

        builder.put(AttributeRegistry.MAX_MANA.get(),
                new AttributeModifier(uuidFor(slot, "max_mana"), tier.id + " Max Mana",
                        tier.maxMana().get(), AttributeModifier.Operation.ADDITION));

        switch (tier) {
            case ELEMENTIUM -> builder.put(IBAttributes.MANA_EFFICIENCY.get(),
                    new AttributeModifier(uuidFor(slot, "mana_efficiency"), tier.id + " Mana Efficiency",
                            CommonConfig.ELEMENTIUM_ARMOR_MANA_EFFICIENCY.get(),
                            AttributeModifier.Operation.ADDITION));
            case TERRASTEEL -> builder.put(AttributeRegistry.COOLDOWN_REDUCTION.get(),
                    new AttributeModifier(uuidFor(slot, "cooldown"), tier.id + " Cooldown Reduction",
                            CommonConfig.TERRASTEEL_ARMOR_COOLDOWN_REDUCTION.get(),
                            AttributeModifier.Operation.MULTIPLY_BASE));
            case GAIA -> builder.put(AttributeRegistry.COOLDOWN_REDUCTION.get(),
                    new AttributeModifier(uuidFor(slot, "cooldown"), tier.id + " Cooldown Reduction",
                            CommonConfig.GAIA_ARMOR_COOLDOWN_REDUCTION.get(),
                            AttributeModifier.Operation.MULTIPLY_BASE));
        }

        return builder.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int spellPowerPercent = (int) (tier.spellPower().get() * 100);
        tooltip.add(Component.translatable("item.ironsbotany." + tier.id + ".spell_power", spellPowerPercent)
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.ironsbotany." + tier.id + ".max_mana", tier.maxMana().get())
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.ironsbotany." + tier.id + ".set_bonus")
                .withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    public Tier getTier() {
        return tier;
    }
}
