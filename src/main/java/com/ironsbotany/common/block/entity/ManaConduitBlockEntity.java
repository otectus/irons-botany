package com.ironsbotany.common.block.entity;

import com.ironsbotany.common.compat.ArsNSpellsCompat;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.util.TransferBudget;
import com.ironsbotany.common.registry.IBBlockEntities;
import com.ironsbotany.common.registry.IBParticles;
import com.ironsbotany.common.util.ManaHelper;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import vazkii.botania.api.mana.ManaPool;

import java.util.ArrayList;
import java.util.Comparator;
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

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ManaConduitBlockEntity blockEntity) {
        if (level.getGameTime() % 20 != 0) return;

        int maxCapacity = CommonConfig.MANA_CONDUIT_CAPACITY.get();
        int conversionRate = CommonConfig.MANA_CONDUIT_CONVERSION_RATE.get();
        int radius = CommonConfig.MANA_CONDUIT_RADIUS.get();

        // Ambient mana wisps when active — ~30% of ticks
        if (CommonConfig.ENABLE_BLOCK_AMBIENT_PARTICLES.get()
                && level instanceof ServerLevel serverLevel && blockEntity.storedISSMana > 0
                && level.random.nextFloat() < 0.3F) {
            double ox = pos.getX() + 0.4 + level.random.nextDouble() * 0.2;
            double oy = pos.getY() + 0.9;
            double oz = pos.getZ() + 0.4 + level.random.nextDouble() * 0.2;
            serverLevel.sendParticles(IBParticles.MANA_TRANSFER.get(), ox, oy, oz,
                1, 0.08, 0.3, 0.08, 0.03);
        }

        // Phase 1: Try to drain from adjacent Botania mana pools.
        // Conversion availability is mode-dependent and identical for every neighbor,
        // so evaluate it once before scanning rather than per-direction.
        int ratio = CommonConfig.MANA_CONVERSION_RATIO.get();
        int issFromConversion = ManaHelper.convertBotaniaToISS(conversionRate);
        if (blockEntity.storedISSMana < maxCapacity && issFromConversion > 0) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                BlockEntity neighbor = level.getBlockEntity(neighborPos);
                if (neighbor instanceof ManaPool pool) {
                    // Compute how much ISS mana we can accept
                    int issRoom = maxCapacity - blockEntity.storedISSMana;
                    int issToGain = Math.min(issFromConversion, issRoom);
                    // Only drain the exact Botania amount that converts cleanly
                    int botaniaToConsume = issToGain * ratio;

                    if (botaniaToConsume > 0 && pool.getCurrentMana() >= botaniaToConsume) {
                        pool.receiveMana(-botaniaToConsume);
                        blockEntity.storedISSMana += issToGain;
                        notifyChanged(blockEntity);
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
                    int accepted = reservoir.addMana(toTransfer);
                    if (accepted > 0) {   // a full reservoir accepts nothing: do not sync a no-op
                        blockEntity.storedISSMana -= accepted;
                        notifyChanged(blockEntity);
                    }
                }
            }
        }

        // Phase 3: Distribute ISS mana to nearby players
        if (blockEntity.storedISSMana > 0) {
            AABB searchBox = new AABB(pos).inflate(radius);
            List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, searchBox);

            // One budget per tick for the conduit, shared fairly across the players in range —
            // see SpellReservoirBlockEntity for why. Previously each player drew a full transfer
            // rate, so the configured rate was multiplied by the number of nearby players, and
            // entity-list order decided who was served first every tick.
            if (!nearbyPlayers.isEmpty()) {
                List<Player> ordered = new ArrayList<>(nearbyPlayers);
                ordered.sort(Comparator.comparing(Player::getUUID));

                int budget = Math.min(getTransferRate(), blockEntity.storedISSMana);
                int count = ordered.size();
                int rotation = TransferBudget.rotationFor(level.getGameTime(), count);

                int transferredTotal = 0;
                for (int i = 0; i < count && transferredTotal < budget; i++) {
                    Player player = ordered.get((i + rotation) % count);
                    int allowance = TransferBudget.shareFor(budget, count, i);
                    if (allowance <= 0) continue;

                    MagicData magicData = MagicData.getPlayerMagicData(player);
                    if (magicData == null) continue;

                    float currentMana = magicData.getMana();
                    // See SpellReservoirBlockEntity — use bridge max under ANS ARS_PRIMARY so
                    // units match the redirected getMana.
                    float maxMana = ArsNSpellsCompat.getEffectiveMaxMana(player);
                    if (currentMana >= maxMana) continue;

                    int room = (int) Math.max(0f, maxMana - currentMana);
                    int toTransfer = Math.min(Math.min(allowance, room),
                            blockEntity.storedISSMana - transferredTotal);
                    if (toTransfer <= 0) continue;

                    magicData.addMana(toTransfer);
                    transferredTotal += toTransfer;
                }

                // Sync once, and only when the stored value actually moved.
                if (transferredTotal > 0) {
                    blockEntity.storedISSMana -= transferredTotal;
                    notifyChanged(blockEntity);
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

    private static void notifyChanged(ManaConduitBlockEntity blockEntity) {
        blockEntity.setChanged();
        if (blockEntity.level != null) {
            BlockState state = blockEntity.getBlockState();
            Block block = state.getBlock();
            blockEntity.level.updateNeighbourForOutputSignal(blockEntity.worldPosition, block);
            if (!blockEntity.level.isClientSide) {
                blockEntity.level.sendBlockUpdated(blockEntity.worldPosition, state, state, Block.UPDATE_CLIENTS);
            }
        }
    }
}
