package com.ironsbotany.common.block.entity;

import com.ironsbotany.common.registry.IBBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;

import java.util.Optional;

/**
 * Block entity behind the Arcane Mana Altar — a hybrid mana pool that
 * lets a nearby player drain its stored Botania mana for spell costs.
 * Implements three Botania interfaces simultaneously:
 *
 * <ul>
 *   <li>{@link ManaPool} — appears to spreaders, sparks, the HUD
 *       overlay, and recipe systems as a real mana pool with a
 *       configurable cap.</li>
 *   <li>{@link ManaReceiver} — accepts mana bursts from spreaders.</li>
 *   <li>{@link SparkAttachable} — sparks can attach for ten-fold
 *       throughput and pool-to-pool routing.</li>
 * </ul>
 *
 * <p>The altar is the bridge piece for the Phase 3 vision: stand within
 * proximity, and spell costs route through the altar's mana before
 * touching the player's tablets. The proximity check + actual debit is
 * handled by {@code ManaBridgeManager} when it scans for nearby altars
 * (Phase 4 work).
 */
public class ArcaneManaAltarBlockEntity extends BlockEntity implements ManaPool, SparkAttachable {

    public static final int DEFAULT_MAX_MANA = 1_000_000;
    private static final String NBT_MANA = "ironsbotany_altar_mana";
    private static final String NBT_COLOR = "ironsbotany_altar_color";

    private int storedMana;
    private Optional<net.minecraft.world.item.DyeColor> color = Optional.empty();
    private ManaSpark attachedSpark;

    private final LazyOptional<ManaReceiver> receiverCap = LazyOptional.of(() -> this);
    private final LazyOptional<SparkAttachable> sparkCap = LazyOptional.of(() -> this);

    public ArcaneManaAltarBlockEntity(BlockPos pos, BlockState state) {
        super(IBBlockEntities.ARCANE_MANA_ALTAR.get(), pos, state);
    }

    // --- BlockEntity persistence ---

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(NBT_MANA, storedMana);
        color.ifPresent(c -> tag.putInt(NBT_COLOR, c.getId()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storedMana = tag.getInt(NBT_MANA);
        if (tag.contains(NBT_COLOR)) {
            color = Optional.of(net.minecraft.world.item.DyeColor.byId(tag.getInt(NBT_COLOR)));
        }
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

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        receiverCap.invalidate();
        sparkCap.invalidate();
    }

    // --- Capability exposure ---

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) return receiverCap.cast();
        if (cap == BotaniaForgeCapabilities.SPARK_ATTACHABLE) return sparkCap.cast();
        return super.getCapability(cap, side);
    }

    // --- ManaPool / ManaReceiver ---

    @Override
    public Level getManaReceiverLevel() {
        return level;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return worldPosition;
    }

    @Override
    public int getCurrentMana() {
        return storedMana;
    }

    @Override
    public boolean isFull() {
        return storedMana >= getMaxMana();
    }

    @Override
    public void receiveMana(int amount) {
        int next = Math.max(0, Math.min(storedMana + amount, getMaxMana()));
        if (next != storedMana) {
            storedMana = next;
            markDirty();
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    @Override
    public boolean isOutputtingPower() {
        return storedMana > 0;
    }

    @Override
    public int getMaxMana() {
        return DEFAULT_MAX_MANA;
    }

    @Override
    public Optional<net.minecraft.world.item.DyeColor> getColor() {
        return color;
    }

    @Override
    public void setColor(Optional<net.minecraft.world.item.DyeColor> newColor) {
        this.color = newColor;
        markDirty();
    }

    // --- SparkAttachable ---

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return attachedSpark == null;
    }

    @Override
    public void attachSpark(ManaSpark spark) {
        this.attachedSpark = spark;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(0, getMaxMana() - storedMana);
    }

    @Override
    public ManaSpark getAttachedSpark() {
        return attachedSpark;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return isFull();
    }

    // --- Helpers ---

    /**
     * Drain {@code amount} mana for use by a nearby player's spell cost.
     * Called by {@code ManaBridgeManager} during cost routing.
     */
    public boolean tryDrainForCast(int amount) {
        if (storedMana < amount) return false;
        storedMana -= amount;
        markDirty();
        return true;
    }

    /**
     * @return players within {@code radius} blocks of the altar — the set
     *         eligible to draw from this pool during cast resolution.
     */
    public java.util.List<Player> playersInRange(int radius) {
        if (level == null) return java.util.List.of();
        return level.getEntitiesOfClass(Player.class,
                new AABB(worldPosition).inflate(radius));
    }

    private void markDirty() {
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
