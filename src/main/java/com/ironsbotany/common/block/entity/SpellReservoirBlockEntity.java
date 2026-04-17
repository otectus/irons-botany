package com.ironsbotany.common.block.entity;

import com.ironsbotany.common.compat.ArsNSpellsCompat;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.registry.IBBlockEntities;
import com.ironsbotany.common.registry.IBParticles;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.core.BlockPos;
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

import java.util.List;

public class SpellReservoirBlockEntity extends BlockEntity {
    private int storedISSMana = 0;
    private static final int TRANSFER_RADIUS = 5;
    private static int getTransferRate() {
        return CommonConfig.BLOCK_ENTITY_TRANSFER_RATE.get();
    }

    public SpellReservoirBlockEntity(BlockPos pos, BlockState state) {
        super(IBBlockEntities.SPELL_RESERVOIR.get(), pos, state);
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpellReservoirBlockEntity blockEntity) {
        if (level.getGameTime() % 20 != 0) return; // Tick once per second

        // Ambient petal pollen when active — ~25% of ticks, scaled by fill
        if (CommonConfig.ENABLE_BLOCK_AMBIENT_PARTICLES.get()
                && level instanceof ServerLevel serverLevel && blockEntity.storedISSMana > 0
                && level.random.nextFloat() < 0.25F) {
            double ox = pos.getX() + 0.3 + level.random.nextDouble() * 0.4;
            double oy = pos.getY() + 1.0;
            double oz = pos.getZ() + 0.3 + level.random.nextDouble() * 0.4;
            serverLevel.sendParticles(IBParticles.PETAL_MAGIC.get(), ox, oy, oz,
                1, 0.15, 0.05, 0.15, 0.01);
        }

        // Find nearby players
        AABB searchBox = new AABB(pos).inflate(TRANSFER_RADIUS);
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, searchBox);

        for (Player player : nearbyPlayers) {
            MagicData magicData = MagicData.getPlayerMagicData(player);
            if (magicData != null && blockEntity.storedISSMana > 0) {
                float currentMana = magicData.getMana();
                // Under ANS ARS_PRIMARY, getMana() is mixin-redirected to the
                // Ars pool — use the matching max from the bridge so our
                // clamp ceiling is in the same units.
                float maxMana = ArsNSpellsCompat.getEffectiveMaxMana(player);
                
                if (currentMana < maxMana) {
                    int toTransfer = Math.min(getTransferRate(), blockEntity.storedISSMana);
                    toTransfer = Math.min(toTransfer, (int)(maxMana - currentMana));
                    
                    magicData.addMana(toTransfer);
                    blockEntity.storedISSMana -= toTransfer;
                    blockEntity.notifyChanged();
                }
            }
        }
    }

    public int getStoredMana() {
        return storedISSMana;
    }

    /**
     * Add mana to the reservoir.
     * @return the amount of mana actually accepted (clamped by remaining capacity)
     */
    public int addMana(int amount) {
        int maxCapacity = CommonConfig.SPELL_RESERVOIR_CAPACITY.get();
        int accepted = Math.min(amount, maxCapacity - this.storedISSMana);
        this.storedISSMana += accepted;
        notifyChanged();
        return accepted;
    }

    public int drainMana(int amount) {
        int drained = Math.min(amount, this.storedISSMana);
        this.storedISSMana -= drained;
        notifyChanged();
        return drained;
    }

    private void notifyChanged() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            Block block = state.getBlock();
            level.updateNeighbourForOutputSignal(worldPosition, block);
            if (!level.isClientSide) {
                level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
            }
        }
    }
}
