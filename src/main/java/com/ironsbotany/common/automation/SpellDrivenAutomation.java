package com.ironsbotany.common.automation;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.util.DataKeys;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import vazkii.botania.api.mana.ManaSpreader;
import vazkii.botania.api.internal.ManaBurst;

import java.util.List;

/**
 * Spell-Driven Automation - Players become walking automation nodes.
 *
 * Spells can operate Botania machines:
 * - Telekinesis moves Mana Spreaders
 * - Growth accelerates passive flowers
 * - Gravity redirects Mana Bursts
 * - Illusion duplicates burst visuals
 */
public class SpellDrivenAutomation {

    /**
     * Apply spell automation effects
     * @param spell The spell being cast
     * @param player The caster
     * @param targetPos The target position
     */
    public static void applyAutomationEffect(AbstractSpell spell, Player player, BlockPos targetPos) {
        if (!ModList.get().isLoaded("botania")) {
            return;
        }

        Level level = player.level();
        String spellId = spell.getSpellId();

        // Telekinesis - move Mana Spreaders
        if (spellId.contains("telekinesis") || spellId.contains("teleport") || spellId.contains("move")) {
            applyTelekinesisAutomation(level, targetPos, player);
        }

        // Growth - accelerate passive flowers
        if (spellId.contains("growth") || spellId.contains("bloom") || spellId.contains("nature")) {
            applyGrowthAutomation(level, targetPos, player);
        }

        // Gravity - redirect Mana Bursts
        if (spellId.contains("gravity") || spellId.contains("pull") || spellId.contains("push")) {
            applyGravityAutomation(level, targetPos, player);
        }

        // Illusion - duplicate burst visuals
        if (spellId.contains("illusion") || spellId.contains("mirror") || spellId.contains("duplicate")) {
            applyIllusionAutomation(level, targetPos, player);
        }
    }

    /**
     * Telekinesis - rotate nearby Mana Spreaders to face target
     */
    private static void applyTelekinesisAutomation(Level level, BlockPos targetPos, Player player) {
        AABB searchBox = new AABB(targetPos).inflate(5);

        for (BlockPos pos : BlockPos.betweenClosed(
            (int)searchBox.minX, (int)searchBox.minY, (int)searchBox.minZ,
            (int)searchBox.maxX, (int)searchBox.maxY, (int)searchBox.maxZ)) {

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ManaSpreader) {
                rotateSpreader(be, pos, player.blockPosition());
            }
        }
    }

    /**
     * Growth - accelerate passive flowers
     */
    private static void applyGrowthAutomation(Level level, BlockPos targetPos, Player player) {
        AABB searchBox = new AABB(targetPos).inflate(8);

        for (BlockPos pos : BlockPos.betweenClosed(
            (int)searchBox.minX, (int)searchBox.minY, (int)searchBox.minZ,
            (int)searchBox.maxX, (int)searchBox.maxY, (int)searchBox.maxZ)) {

            BlockEntity be = level.getBlockEntity(pos);
            if (be != null && isBotaniaFlowerEntity(be)) {
                CompoundTag tag = be.getPersistentData();
                tag.putFloat(DataKeys.GROWTH_BOOST, 2.0f);
                tag.putLong(DataKeys.GROWTH_EXPIRY, level.getGameTime() + 200);
            }
        }
    }

    /**
     * Gravity - redirect nearby Mana Bursts toward target position
     */
    private static void applyGravityAutomation(Level level, BlockPos targetPos, Player player) {
        AABB searchBox = new AABB(targetPos).inflate(8);

        for (net.minecraft.world.entity.Entity entity : level.getEntities((net.minecraft.world.entity.Entity) null, searchBox, e -> e instanceof ManaBurst)) {
            ManaBurst burst = (ManaBurst) entity;
            // Use Botania's built-in magnetize mechanic
            burst.setMagnetizePos(targetPos);

            // Blend velocity toward target for smooth gravity-well effect
            net.minecraft.world.entity.Entity burstEntity = burst.entity();
            if (burstEntity != null) {
                Vec3 entityPos = burstEntity.position();
                Vec3 target = new Vec3(
                    targetPos.getX() + 0.5,
                    targetPos.getY() + 0.5,
                    targetPos.getZ() + 0.5);
                Vec3 direction = target.subtract(entityPos).normalize();
                double speed = burstEntity.getDeltaMovement().length();

                Vec3 currentVel = burstEntity.getDeltaMovement();
                Vec3 newVel = currentVel.scale(0.3).add(direction.scale(speed * 0.7));
                burstEntity.setDeltaMovement(newVel);
            }
        }
    }

    /**
     * Illusion - duplicate burst visuals
     */
    private static void applyIllusionAutomation(Level level, BlockPos targetPos, Player player) {
        CompoundTag playerData = player.getPersistentData();
        playerData.putBoolean(DataKeys.ILLUSION_ACTIVE, true);
        playerData.putLong(DataKeys.ILLUSION_EXPIRY, level.getGameTime() + 200);
    }

    /**
     * Check if block entity is a Botania flower (generating or functional)
     */
    private static boolean isBotaniaFlowerEntity(BlockEntity be) {
        return com.ironsbotany.common.util.BotaniaIntegration.isBotaniaBlockEntity(be);
    }

    /**
     * Rotate a ManaSpreader to face a target position using the Botania API
     */
    private static void rotateSpreader(BlockEntity be, BlockPos spreaderPos, BlockPos targetPos) {
        if (!(be instanceof ManaSpreader spreader)) {
            return;
        }

        double dx = targetPos.getX() + 0.5 - (spreaderPos.getX() + 0.5);
        double dy = targetPos.getY() + 0.5 - (spreaderPos.getY() + 0.5);
        double dz = targetPos.getZ() + 0.5 - (spreaderPos.getZ() + 0.5);

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        float rotationX = (float) -(Math.atan2(dy, horizontalDist) * 180.0 / Math.PI);
        float rotationY = (float) (Math.atan2(dx, dz) * 180.0 / Math.PI);

        spreader.setRotationX(rotationX);
        spreader.setRotationY(rotationY);
        spreader.commitRedirection();

        be.setChanged();
    }
}
