package com.ironsbotany.common.block.entity;

import com.ironsbotany.common.compat.ArsNSpellsCompat;
import com.ironsbotany.common.config.CommonConfig;
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

        // Phase 1: Try to drain from adjacent Botania mana pools
        if (blockEntity.storedISSMana < maxCapacity) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                BlockEntity neighbor = level.getBlockEntity(neighborPos);
                if (neighbor instanceof ManaPool pool) {
                    // Compute how much ISS mana we can accept
                    int issRoom = maxCapacity - blockEntity.storedISSMana;
                    // Compute maximum Botania mana to drain for that ISS room
                    int ratio = CommonConfig.MANA_CONVERSION_RATIO.get();
                    int issFromConversion = ManaHelper.convertBotaniaToISS(conversionRate);
                    if (issFromConversion <= 0) break; // Conversion not allowed in this mode

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
                    blockEntity.storedISSMana -= accepted;
                    notifyChanged(blockEntity);
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
                    // See SpellReservoirBlockEntity — use bridge max under
                    // ANS ARS_PRIMARY so units match the redirected getMana.
                    float maxMana = ArsNSpellsCompat.getEffectiveMaxMana(player);

                    if (currentMana < maxMana) {
                        int toTransfer = Math.min(getTransferRate(), blockEntity.storedISSMana);
                        toTransfer = Math.min(toTransfer, (int) (maxMana - currentMana));

                        magicData.addMana(toTransfer);
                        blockEntity.storedISSMana -= toTransfer;
                        notifyChanged(blockEntity);
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
