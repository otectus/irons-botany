package com.ironsbotany.common.item;

import com.ironsbotany.IronsBotany;
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

/**
 * An Iron's Spells upgrade orb registered by Iron's Botany.
 *
 * <h3>What was wrong before 1.10.0</h3>
 * Twelve orb items shared four orb types. The eight school-named orbs were each handed one of the
 * four base keys, which meant every one of them applied the wrong attribute <em>and</em> returned
 * the wrong item when consumed, because an orb type's {@code containerItem} names the orb it gives
 * back:
 *
 * <ul>
 *   <li>Orb of Fire Power and Orb of Holy Power both used the <i>flora</i> type — so both granted
 *       ISS <i>nature</i> spell power and both handed back an Orb of Flora.</li>
 *   <li>Orb of Frost Power and Orb of Ender Power both used the <i>pool</i> type — so both granted
 *       +100 max mana rather than any school power, and handed back an Orb of the Pool.</li>
 *   <li>Orb of Lightning Power and Orb of Blood Power both used <i>bursting</i>; Orb of Nature
 *       Power and Orb of Eldritch Power both used <i>terran</i>. All four granted generic spell
 *       power and handed back the base orb.</li>
 * </ul>
 *
 * The tooltip switch also had no case for any of the eight names, so they displayed no bonus line
 * at all — a player had no way to see that the item did something other than what it was called.
 * Separately, the Orb of Flora boosted ISS Nature while this mod's own spells are Botany.
 *
 * <h3>Now</h3>
 * Every orb has its own {@link UpgradeOrbType} data file naming its own attribute and its own
 * container item, and its tooltip key is supplied at registration rather than resolved through a
 * switch that could silently miss a case. {@code BotanicalUpgradeOrbItemTest} asserts the
 * one-to-one mapping holds for every registered orb, so a future orb cannot be added without one.
 */
public class BotanicalUpgradeOrbItem extends UpgradeOrbItem {

    /** Orb-type registry keys, one per orb item. Kept in sync with the JSON by test. */
    public static ResourceKey<UpgradeOrbType> typeKey(String path) {
        return ResourceKey.create(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY,
                new ResourceLocation(IronsBotany.MODID, path));
    }

    public static final ResourceKey<UpgradeOrbType> FLORA_ORB_TYPE = typeKey("flora");
    public static final ResourceKey<UpgradeOrbType> POOL_ORB_TYPE = typeKey("pool");
    public static final ResourceKey<UpgradeOrbType> BURSTING_ORB_TYPE = typeKey("bursting");
    public static final ResourceKey<UpgradeOrbType> TERRAN_ORB_TYPE = typeKey("terran");

    private final String orbType;
    private final ChatFormatting bonusColor;

    /**
     * @param orbTypeKey  the orb's own type; never shared with another orb item
     * @param orbType     registry path of that type, used to build the tooltip key
     * @param bonusColor  colour for the bonus line
     */
    public BotanicalUpgradeOrbItem(Properties properties, ResourceKey<UpgradeOrbType> orbTypeKey,
                                   String orbType, ChatFormatting bonusColor) {
        super(properties, orbTypeKey);
        this.orbType = orbType;
        this.bonusColor = bonusColor;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // Derived from the orb type rather than matched by a switch, so an orb added later cannot
        // silently render with no bonus line.
        tooltip.add(Component.translatable("item.ironsbotany.orb." + orbType + ".bonus").withStyle(bonusColor));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.ironsbotany.upgrade_orb.usage")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    public String getOrbType() {
        return orbType;
    }
}
