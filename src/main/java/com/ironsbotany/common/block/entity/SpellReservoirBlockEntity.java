package com.ironsbotany.common.block.entity;

import com.ironsbotany.common.compat.ArsNSpellsCompat;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.util.TransferBudget;
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

import java.util.ArrayList;
import java.util.Comparator;
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

        if (nearbyPlayers.isEmpty() || blockEntity.storedISSMana <= 0) return;

        // One budget per tick for the whole reservoir, shared fairly.
        //
        // Through 1.9.0 this loop granted each nearby player a FULL getTransferRate() every tick,
        // so a reservoir next to four players drained four times its configured rate — the rate
        // was per-player in practice while the config described it as the block's. It also
        // followed the entity list's order, so whoever happened to be first was served first every
        // tick and could starve the others when mana ran low.
        List<Player> ordered = new ArrayList<>(nearbyPlayers);
        ordered.sort(Comparator.comparing(Player::getUUID));

        int budget = Math.min(getTransferRate(), blockEntity.storedISSMana);
        if (budget <= 0) return;

        int count = ordered.size();
                        // Rotate who receives the remainder so the same player is not favoured every tick.
        int rotation = TransferBudget.rotationFor(level.getGameTime(), count);

        int transferredTotal = 0;
        for (int i = 0; i < count && transferredTotal < budget; i++) {
            Player player = ordered.get((i + rotation) % count);
            int allowance = TransferBudget.shareFor(budget, count, i);
            if (allowance <= 0) continue;

            MagicData magicData = MagicData.getPlayerMagicData(player);
            if (magicData == null) continue;

            float currentMana = magicData.getMana();
            // Under ANS ARS_PRIMARY, getMana() is mixin-redirected to the Ars pool — use the
            // matching max from the bridge so our clamp ceiling is in the same units.
            float maxMana = ArsNSpellsCompat.getEffectiveMaxMana(player);
            if (currentMana >= maxMana) continue;

            int room = (int) Math.max(0f, maxMana - currentMana);
            int toTransfer = Math.min(Math.min(allowance, room), blockEntity.storedISSMana - transferredTotal);
            if (toTransfer <= 0) continue;

            magicData.addMana(toTransfer);
            transferredTotal += toTransfer;
        }

        // Sync once, and only if the stored value actually moved. The old code called
        // notifyChanged() inside the per-player loop, sending a block update packet per player per
        // tick even when nothing had changed for that player.
        if (transferredTotal > 0) {
            blockEntity.storedISSMana -= transferredTotal;
            blockEntity.notifyChanged();
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
        // Clamp to >= 0 so a config-lowered capacity (below current stored mana) can't
        // produce a negative "accepted" — which would silently drain this reservoir and
        // hand negative mana back to the caller (the conduit), creating free mana.
        int accepted = Math.max(0, Math.min(amount, maxCapacity - this.storedISSMana));
        if (accepted > 0) {
            this.storedISSMana += accepted;
            notifyChanged();
        }
        return accepted;
    }

    public int drainMana(int amount) {
        int drained = Math.max(0, Math.min(amount, this.storedISSMana));
        if (drained <= 0) return 0;   // nothing moved: do not sync an unchanged value
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
