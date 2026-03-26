package com.ironsbotany.common.block.entity;

import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.registry.IBBlockEntities;
import com.ironsbotany.common.util.ManaHelper;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import vazkii.botania.api.mana.ManaPool;

import java.util.List;

public class ManaConduitBlockEntity extends BlockEntity {
    private int storedISSMana = 0;
    private static int getTransferRate() {
        return CommonConfig.BLOCK_ENTITY_TRANSFER_RATE.get();
    }

    public ManaConduitBlockEntity(BlockPos pos, BlockState state) {
        super(IBBlockEntities.MANA_CONDUIT.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("StoredMana", storedISSMana);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storedISSMana = tag.getInt("StoredMana");
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ManaConduitBlockEntity blockEntity) {
        if (level.getGameTime() % 20 != 0) return;

        int maxCapacity = CommonConfig.MANA_CONDUIT_CAPACITY.get();
        int conversionRate = CommonConfig.MANA_CONDUIT_CONVERSION_RATE.get();
        int radius = CommonConfig.MANA_CONDUIT_RADIUS.get();

        // Phase 1: Try to drain from adjacent Botania mana pools
        if (blockEntity.storedISSMana < maxCapacity) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                BlockEntity neighbor = level.getBlockEntity(neighborPos);
                if (neighbor instanceof ManaPool pool) {
                    int toDrain = Math.min(conversionRate, maxCapacity - blockEntity.storedISSMana);
                    if (pool.getCurrentMana() >= toDrain) {
                        pool.receiveMana(-toDrain);
                        int issGained = ManaHelper.convertBotaniaToISS(toDrain);
                        blockEntity.storedISSMana = Math.min(blockEntity.storedISSMana + issGained, maxCapacity);
                        blockEntity.setChanged();
                        break; // Only drain from one pool per tick
                    }
                }
            }
        }

        // Phase 2: Feed adjacent Spell Reservoirs
        if (blockEntity.storedISSMana > 0) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                BlockEntity neighbor = level.getBlockEntity(neighborPos);
                if (neighbor instanceof SpellReservoirBlockEntity reservoir && blockEntity.storedISSMana > 0) {
                    int toTransfer = Math.min(getTransferRate(), blockEntity.storedISSMana);
                    reservoir.addMana(toTransfer);
                    blockEntity.storedISSMana -= toTransfer;
                    blockEntity.setChanged();
                }
            }
        }

        // Phase 3: Distribute ISS mana to nearby players
        if (blockEntity.storedISSMana > 0) {
            AABB searchBox = new AABB(pos).inflate(radius);
            List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, searchBox);

            for (Player player : nearbyPlayers) {
                if (blockEntity.storedISSMana <= 0) break;

                MagicData magicData = MagicData.getPlayerMagicData(player);
                if (magicData != null) {
                    float currentMana = magicData.getMana();
                    float maxMana = (float) player.getAttributeValue(
                            io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA.get());

                    if (currentMana < maxMana) {
                        int toTransfer = Math.min(getTransferRate(), blockEntity.storedISSMana);
                        toTransfer = Math.min(toTransfer, (int) (maxMana - currentMana));

                        magicData.addMana(toTransfer);
                        blockEntity.storedISSMana -= toTransfer;
                        blockEntity.setChanged();
                    }
                }
            }
        }
    }

    public int getMaxCapacity() {
        return CommonConfig.MANA_CONDUIT_CAPACITY.get();
    }

    public int getStoredMana() {
        return storedISSMana;
    }
}
