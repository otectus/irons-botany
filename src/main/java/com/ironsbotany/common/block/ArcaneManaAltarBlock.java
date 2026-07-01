package com.ironsbotany.common.block;

import com.ironsbotany.common.block.entity.ArcaneManaAltarBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Player-facing block whose BE participates in the Botania mana network
 * as a {@link vazkii.botania.api.mana.ManaPool ManaPool},
 * {@link vazkii.botania.api.mana.ManaReceiver ManaReceiver}, and
 * {@link vazkii.botania.api.mana.spark.SparkAttachable SparkAttachable}.
 *
 * <p>See {@link ArcaneManaAltarBlockEntity} for the actual mana storage
 * and capability wiring.
 */
public class ArcaneManaAltarBlock extends BaseEntityBlock {

    public ArcaneManaAltarBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneManaAltarBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ArcaneManaAltarBlockEntity altar) {
            player.displayClientMessage(
                Component.translatable("block.ironsbotany.arcane_mana_altar.info",
                    altar.getCurrentMana(), altar.getMaxMana()),
                true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.ironsbotany.arcane_mana_altar.tooltip")
            .withStyle(ChatFormatting.GRAY));
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
        if (be instanceof ArcaneManaAltarBlockEntity altar) {
            int capacity = altar.getMaxMana();
            if (capacity <= 0) return 0;
            return Math.min(15, altar.getCurrentMana() * 15 / capacity);
        }
        return 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        // Altar is event-driven (sparks, spell-cost drains, burst hits) — no per-tick logic needed.
        return null;
    }
}
