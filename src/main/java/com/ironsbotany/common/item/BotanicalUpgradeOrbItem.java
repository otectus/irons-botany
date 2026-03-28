package com.ironsbotany.common.item;

import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BotanicalUpgradeOrbItem extends UpgradeOrbItem {

    public static final ResourceKey<UpgradeOrbType> FLORA_ORB_TYPE = ResourceKey.create(
            UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY,
            new ResourceLocation("ironsbotany", "flora"));
    public static final ResourceKey<UpgradeOrbType> POOL_ORB_TYPE = ResourceKey.create(
            UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY,
            new ResourceLocation("ironsbotany", "pool"));
    public static final ResourceKey<UpgradeOrbType> BURSTING_ORB_TYPE = ResourceKey.create(
            UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY,
            new ResourceLocation("ironsbotany", "bursting"));
    public static final ResourceKey<UpgradeOrbType> TERRAN_ORB_TYPE = ResourceKey.create(
            UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY,
            new ResourceLocation("ironsbotany", "terran"));

    private final String orbType;

    public BotanicalUpgradeOrbItem(Properties properties, ResourceKey<UpgradeOrbType> orbTypeKey, String orbType) {
        super(properties, orbTypeKey);
        this.orbType = orbType;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        switch (orbType) {
            case "flora" -> {
                tooltip.add(Component.translatable("item.ironsbotany.orb_of_flora.bonus").withStyle(ChatFormatting.GREEN));
            }
            case "pool" -> {
                tooltip.add(Component.translatable("item.ironsbotany.orb_of_the_pool.bonus").withStyle(ChatFormatting.BLUE));
            }
            case "bursting" -> {
                tooltip.add(Component.translatable("item.ironsbotany.orb_of_bursting.bonus").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            case "terran" -> {
                tooltip.add(Component.translatable("item.ironsbotany.orb_of_terran_might.bonus").withStyle(ChatFormatting.GOLD));
            }
        }
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.ironsbotany.upgrade_orb.usage")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    public String getOrbType() {
        return orbType;
    }
}
