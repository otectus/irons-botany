package com.ironsbotany.common.block;

import com.ironsbotany.common.block.entity.SpellReservoirBlockEntity;
import com.ironsbotany.common.registry.IBBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import org.jetbrains.annotations.Nullable;

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
