package com.ironsbotany.common.block;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.block.entity.SpellReservoirBlockEntity;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.registry.IBBlockEntities;
import com.ironsbotany.common.util.ManaHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ManaItemHandler;

import java.util.List;

public class SpellReservoirBlock extends BaseEntityBlock {

    public SpellReservoirBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpellReservoirBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SpellReservoirBlockEntity reservoir)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            // Show stored mana info on empty-hand right-click
            int capacity = CommonConfig.SPELL_RESERVOIR_CAPACITY.get();
            player.displayClientMessage(
                Component.translatable("block.ironsbotany.spell_reservoir.info",
                    reservoir.getStoredMana(), capacity),
                true);
            return InteractionResult.SUCCESS;
        }

        int transferRate = CommonConfig.MANA_TRANSFER_RATE.get();
        try {
            // Check conversion yields something before draining
            int issGained = ManaHelper.convertBotaniaToISS(transferRate);
            if (issGained <= 0) {
                return InteractionResult.PASS;
            }

            // Check reservoir has room
            int capacity = CommonConfig.SPELL_RESERVOIR_CAPACITY.get();
            int currentMana = reservoir.getStoredMana();
            if (currentMana >= capacity) {
                return InteractionResult.PASS;
            }

            // Clamp to available capacity
            int roomLeft = capacity - currentMana;
            if (issGained > roomLeft) {
                issGained = roomLeft;
                // Only drain the exact Botania amount needed for the clamped ISS gain
                int ratio = CommonConfig.MANA_CONVERSION_RATIO.get();
                transferRate = issGained * ratio;
            }

            // Now drain — we know it will convert cleanly
            if (ManaItemHandler.instance().requestManaExact(held, player, transferRate, true)) {
                reservoir.addMana(issGained);
                level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.5f, 1.2f);
                return InteractionResult.CONSUME;
            }
        } catch (Exception e) {
            IronsBotany.LOGGER.debug("Reservoir mana transfer failed: {}", e.getMessage());
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SpellReservoirBlockEntity reservoir) {
            int capacity = CommonConfig.SPELL_RESERVOIR_CAPACITY.get();
            if (capacity <= 0) return 0;
            return Math.min(15, reservoir.getStoredMana() * 15 / capacity);
        }
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.ironsbotany.spell_reservoir.tooltip")
            .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, IBBlockEntities.SPELL_RESERVOIR.get(),
                SpellReservoirBlockEntity::serverTick);
    }
}
