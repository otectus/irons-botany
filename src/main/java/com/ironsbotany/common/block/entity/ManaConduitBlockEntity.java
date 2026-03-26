package com.ironsbotany.common.block.entity;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.registry.IBBlockEntities;
import com.ironsbotany.common.util.ManaHelper;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Method;
import java.util.List;

public class ManaConduitBlockEntity extends BlockEntity {
    private int storedISSMana = 0;
    private static final int MANA_PER_PLAYER_PER_SECOND = 5;

    // Cached reflection for Botania mana pool API
    private static boolean reflectionInitialized = false;
    private static boolean reflectionAvailable = false;
    private static Class<?> manaPoolClass;
    private static Method getCurrentManaMethod;
    private static Method receiveManaMethod;

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

    private static void initReflection() {
        if (reflectionInitialized) return;
        reflectionInitialized = true;
        try {
            manaPoolClass = Class.forName("vazkii.botania.api.mana.ManaPool");
            getCurrentManaMethod = manaPoolClass.getMethod("getCurrentMana");
            receiveManaMethod = manaPoolClass.getMethod("receiveMana", int.class);
            reflectionAvailable = true;
        } catch (Exception e) {
            reflectionAvailable = false;
            IronsBotany.LOGGER.debug("Botania mana pool API not available: {}", e.getMessage());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ManaConduitBlockEntity blockEntity) {
        if (level.getGameTime() % 20 != 0) return;

        int maxCapacity = CommonConfig.MANA_CONDUIT_CAPACITY.get();
        int conversionRate = CommonConfig.MANA_CONDUIT_CONVERSION_RATE.get();
        int radius = CommonConfig.MANA_CONDUIT_RADIUS.get();

        // Phase 1: Try to drain from adjacent Botania mana pools
        if (blockEntity.storedISSMana < maxCapacity) {
            initReflection();
            if (reflectionAvailable) {
                for (Direction dir : Direction.values()) {
                    BlockPos neighborPos = pos.relative(dir);
                    BlockEntity neighbor = level.getBlockEntity(neighborPos);
                    if (neighbor != null) {
                        int toDrain = Math.min(conversionRate, maxCapacity - blockEntity.storedISSMana);
                        if (tryDrainFromPool(neighbor, toDrain)) {
                            int issGained = ManaHelper.convertBotaniaToISS(toDrain);
                            blockEntity.storedISSMana = Math.min(blockEntity.storedISSMana + issGained, maxCapacity);
                            blockEntity.setChanged();
                            break; // Only drain from one pool per tick
                        }
                    }
                }
            }
        }

        // Phase 2: Distribute ISS mana to nearby players
        if (blockEntity.storedISSMana > 0) {
            AABB searchBox = new AABB(pos).inflate(radius);
            List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, searchBox);

            for (Player player : nearbyPlayers) {
                if (blockEntity.storedISSMana <= 0) break;

                MagicData magicData = MagicData.getPlayerMagicData(player);
                if (magicData != null) {
                    float currentMana = magicData.getMana();
                    float maxMana = (float) player.getAttributeValue(
                            io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA.get());

                    if (currentMana < maxMana) {
                        int toTransfer = Math.min(MANA_PER_PLAYER_PER_SECOND, blockEntity.storedISSMana);
                        toTransfer = Math.min(toTransfer, (int) (maxMana - currentMana));

                        magicData.addMana(toTransfer);
                        blockEntity.storedISSMana -= toTransfer;
                        blockEntity.setChanged();
                    }
                }
            }
        }
    }

    private static boolean tryDrainFromPool(BlockEntity poolEntity, int amount) {
        if (!reflectionAvailable) return false;
        try {
            if (manaPoolClass.isInstance(poolEntity)) {
                int currentMana = (int) getCurrentManaMethod.invoke(poolEntity);
                if (currentMana >= amount) {
                    receiveManaMethod.invoke(poolEntity, -amount);
                    return true;
                }
            }
        } catch (Exception e) {
            IronsBotany.LOGGER.debug("Failed to drain from Botania mana pool: {}", e.getMessage());
        }
        return false;
    }

    public int getStoredMana() {
        return storedISSMana;
    }
}
