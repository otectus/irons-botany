package com.ironsbotany.common.block.entity;

import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.registry.IBBlockEntities;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpellReservoirBlockEntity blockEntity) {
        if (level.getGameTime() % 20 != 0) return; // Tick once per second
        
        // Find nearby players
        AABB searchBox = new AABB(pos).inflate(TRANSFER_RADIUS);
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, searchBox);
        
        for (Player player : nearbyPlayers) {
            MagicData magicData = MagicData.getPlayerMagicData(player);
            if (magicData != null && blockEntity.storedISSMana > 0) {
                float currentMana = magicData.getMana();
                float maxMana = (float) player.getAttributeValue(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA.get());
                
                if (currentMana < maxMana) {
                    int toTransfer = Math.min(getTransferRate(), blockEntity.storedISSMana);
                    toTransfer = Math.min(toTransfer, (int)(maxMana - currentMana));
                    
                    magicData.addMana(toTransfer);
                    blockEntity.storedISSMana -= toTransfer;
                    blockEntity.setChanged();
                }
            }
        }
    }

    public int getStoredMana() {
        return storedISSMana;
    }

    public void addMana(int amount) {
        int maxCapacity = CommonConfig.SPELL_RESERVOIR_CAPACITY.get();
        this.storedISSMana = Math.min(this.storedISSMana + amount, maxCapacity);
        setChanged();
    }

    public int drainMana(int amount) {
        int drained = Math.min(amount, this.storedISSMana);
        this.storedISSMana -= drained;
        setChanged();
        return drained;
    }
}
