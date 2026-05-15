package com.ironsbotany.common.item;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.progression.ProgressionGates;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.api.mana.ManaPool;

import java.util.List;

/**
 * Curios charm that, when worn, authorises a single bound Botania mana pool
 * as a supplementary Botania-mana source for Nature-school spells (only).
 *
 * <p>Binding workflow:</p>
 * <ol>
 *   <li>Player must have earned the Gaia Guardian advancement
 *       ({@link ProgressionGates#isOverchargeUnlocked}).</li>
 *   <li>Right-click the charm on a Mana Pool block.</li>
 *   <li>The pool's position and dimension are stored in the charm's NBT.</li>
 * </ol>
 *
 * <p>The bound pool is consulted by Botania-mana payment via
 * {@link com.ironsbotany.common.util.ManaHelper}; range and bandwidth caps live
 * in {@link com.ironsbotany.common.config.CommonConfig}. Nature-school filter
 * is enforced by the spell pipeline before payment is requested.</p>
 */
public class PoolAttunementCharm extends Item implements ICurioItem {

    public static final String TAG_BOUND_X = "BoundX";
    public static final String TAG_BOUND_Y = "BoundY";
    public static final String TAG_BOUND_Z = "BoundZ";
    public static final String TAG_BOUND_DIM = "BoundDim";

    public PoolAttunementCharm(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ManaPool)) {
            return InteractionResult.PASS;
        }

        if (!ProgressionGates.isOverchargeUnlocked(player)) {
            player.displayClientMessage(
                Component.translatable("item.ironsbotany.pool_attunement_charm.requires_overcharge")
                    .withStyle(ChatFormatting.RED),
                true);
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_BOUND_X, pos.getX());
        tag.putInt(TAG_BOUND_Y, pos.getY());
        tag.putInt(TAG_BOUND_Z, pos.getZ());
        tag.putString(TAG_BOUND_DIM, level.dimension().location().toString());

        player.displayClientMessage(
            Component.translatable("item.ironsbotany.pool_attunement_charm.bound",
                pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.AQUA),
            true);
        IronsBotany.LOGGER.debug("Player {} bound pool attunement charm to {} in {}",
            player.getName().getString(), pos, level.dimension().location());
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (isBound(stack)) {
            CompoundTag tag = stack.getOrCreateTag();
            tooltip.add(Component.translatable(
                "item.ironsbotany.pool_attunement_charm.tooltip.bound",
                tag.getInt(TAG_BOUND_X), tag.getInt(TAG_BOUND_Y), tag.getInt(TAG_BOUND_Z))
                .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable(
                "item.ironsbotany.pool_attunement_charm.tooltip.dim",
                tag.getString(TAG_BOUND_DIM))
                .withStyle(ChatFormatting.DARK_AQUA));
        } else {
            tooltip.add(Component.translatable("item.ironsbotany.pool_attunement_charm.tooltip.unbound")
                .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.ironsbotany.pool_attunement_charm.tooltip.nature_only")
            .withStyle(ChatFormatting.GREEN));
    }

    public static boolean isBound(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_BOUND_X) && tag.contains(TAG_BOUND_DIM);
    }

    @Nullable
    public static BlockPos getBoundPos(ItemStack stack) {
        if (!isBound(stack)) return null;
        CompoundTag tag = stack.getTag();
        return new BlockPos(tag.getInt(TAG_BOUND_X), tag.getInt(TAG_BOUND_Y), tag.getInt(TAG_BOUND_Z));
    }

    @Nullable
    public static ResourceLocation getBoundDimension(ItemStack stack) {
        if (!isBound(stack)) return null;
        return ResourceLocation.tryParse(stack.getTag().getString(TAG_BOUND_DIM));
    }
}
