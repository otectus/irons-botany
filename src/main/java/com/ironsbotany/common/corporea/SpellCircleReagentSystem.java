package com.ironsbotany.common.corporea;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.flower.ActiveFlowerAura;
import com.ironsbotany.common.flower.FlowerAuraRegistry;
import com.ironsbotany.common.flower.auras.RannuncarpusAura;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles automatic reagent requests for spell circles and rituals.
 * Long-cast spells can auto-pull components from Corporea networks.
 */
public class SpellCircleReagentSystem {

    // Cache of spell reagent requirements
    private static final Map<String, List<ItemStack>> SPELL_REAGENTS = new HashMap<>();

    /**
     * Check and request reagents for a spell circle
     * @param player The player casting
     * @param spell The spell being cast
     * @param spellLevel The spell level
     * @return true if reagents are available
     */
    public static boolean prepareSpellCircle(Player player, AbstractSpell spell, int spellLevel) {
        if (!ModList.get().isLoaded("botania")) {
            return true;
        }

        // Only apply to long-cast spells (rituals)
        if (spell.getCastType() != CastType.LONG && spell.getCastType() != CastType.CONTINUOUS) {
            return true;
        }

        // Get required reagents
        List<ItemStack> reagents = getCircleReagents(spell, spellLevel);
        if (reagents.isEmpty()) {
            return true;
        }

        // Auto-place ritual components
        if (shouldAutoPlace(player, spell)) {
            return autoPlaceComponents(player, spell, reagents);
        }

        return true;
    }

    /**
     * Get reagents required for a spell circle
     */
    private static List<ItemStack> getCircleReagents(AbstractSpell spell, int spellLevel) {
        String spellId = spell.getSpellId();

        // Check cache
        if (SPELL_REAGENTS.containsKey(spellId)) {
            return SPELL_REAGENTS.get(spellId);
        }

        List<ItemStack> reagents = new ArrayList<>();

        // Determine reagents based on spell type
        if (spellId.contains("ritual") || spellId.contains("circle")) {
            ItemStack rune = getRuneStack(spell, spellLevel);
            if (!rune.isEmpty()) {
                reagents.add(rune);
            }
        }

        if (spellId.contains("summon") || spellId.contains("communion")) {
            ItemStack petal = getPetalStack(spell, spellLevel);
            if (!petal.isEmpty()) {
                reagents.add(petal);
            }
        }

        if (spellLevel >= 8) {
            ItemStack terrasteel = getTerrasteelStack();
            if (!terrasteel.isEmpty()) {
                reagents.add(terrasteel);
            }
        }

        SPELL_REAGENTS.put(spellId, reagents);
        return reagents;
    }

    /**
     * Check if components should be auto-placed via RannuncarpusAura
     */
    private static boolean shouldAutoPlace(Player player, AbstractSpell spell) {
        List<ActiveFlowerAura> auras = FlowerAuraRegistry.getActiveAuras(player, 8);
        for (ActiveFlowerAura aura : auras) {
            if (aura.getAura() instanceof RannuncarpusAura && aura.getStrength() > 0.6f) {
                return true;
            }
        }
        return false;
    }

    /**
     * Auto-place ritual components in a circle pattern
     */
    private static boolean autoPlaceComponents(Player player, AbstractSpell spell, List<ItemStack> components) {
        Level level = player.level();
        BlockPos center = player.blockPosition();

        // Place components in a circle pattern
        int radius = 3;
        double angleStep = (2 * Math.PI) / components.size();

        for (int i = 0; i < components.size(); i++) {
            double angle = i * angleStep;
            int x = (int) (center.getX() + radius * Math.cos(angle));
            int z = (int) (center.getZ() + radius * Math.sin(angle));
            BlockPos pos = new BlockPos(x, center.getY(), z);

            // Check if position is valid
            if (level.getBlockState(pos).isAir() &&
                level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {

                ItemStack requested = components.get(i);
                if (!requested.isEmpty() && SpellLogisticsSystem.requestFromCorporea(
                        player, findNearestSpark(player), requested)) {
                    // Drop item as entity at the position (simulating placement)
                    net.minecraft.world.entity.item.ItemEntity itemEntity =
                        new net.minecraft.world.entity.item.ItemEntity(
                            level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            requested.copy());
                    itemEntity.setNoPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }
            }
        }

        return true;
    }

    /**
     * Find nearest block with a Corporea spark for auto-placement
     */
    private static BlockPos findNearestSpark(Player player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();
        int searchRadius = 16;

        try {
            var corporeaHelper = vazkii.botania.api.corporea.CorporeaHelper.instance();
            for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-searchRadius, -searchRadius, -searchRadius),
                playerPos.offset(searchRadius, searchRadius, searchRadius))) {
                if (corporeaHelper.doesBlockHaveSpark(level, pos)) {
                    return pos.immutable();
                }
            }
        } catch (Exception e) {
            IronsBotany.LOGGER.debug("Corporea spark search failed: {}", e.getMessage());
        }

        return playerPos; // Fallback - requestFromCorporea will handle null spark gracefully
    }

    /**
     * Get rune stack for spell school
     */
    private static ItemStack getRuneStack(AbstractSpell spell, int spellLevel) {
        String runeFieldName;
        var school = spell.getSchoolType();

        if (school == SchoolRegistry.FIRE.get()) {
            runeFieldName = "runeFire";
        } else if (school == SchoolRegistry.ICE.get()) {
            runeFieldName = "runeWater";
        } else if (school == SchoolRegistry.LIGHTNING.get()) {
            runeFieldName = "runeAir";
        } else if (school == SchoolRegistry.NATURE.get()) {
            runeFieldName = "runeEarth";
        } else {
            runeFieldName = "runeMana";
        }

        ItemStack rune = SpellLogisticsSystem.getBotaniaItem(runeFieldName);
        if (!rune.isEmpty()) {
            rune.setCount(Math.min(spellLevel / 2, 4));
            if (rune.getCount() < 1) rune.setCount(1);
        }
        return rune;
    }

    /**
     * Get petal stack for spell
     */
    private static ItemStack getPetalStack(AbstractSpell spell, int spellLevel) {
        ResourceLocation rl = ResourceLocation.tryParse("botania:white_petal");
        if (rl != null) {
            Item petal = ForgeRegistries.ITEMS.getValue(rl);
            if (petal != null) {
                return new ItemStack(petal, 2 + spellLevel);
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Get Terrasteel stack
     */
    private static ItemStack getTerrasteelStack() {
        return SpellLogisticsSystem.getBotaniaItem("terrasteel");
    }
}
