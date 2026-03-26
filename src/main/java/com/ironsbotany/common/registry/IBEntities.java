package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.entity.BotanicalBurstProjectile;
import com.ironsbotany.common.entity.SparkSwarmEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IBEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, IronsBotany.MODID);

    public static final RegistryObject<EntityType<BotanicalBurstProjectile>> BOTANICAL_BURST =
        ENTITIES.register("botanical_burst", () -> EntityType.Builder
            .<BotanicalBurstProjectile>of(BotanicalBurstProjectile::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build("botanical_burst"));

    public static final RegistryObject<EntityType<SparkSwarmEntity>> SPARK_SWARM =
        ENTITIES.register("spark_swarm", () -> EntityType.Builder
            .<SparkSwarmEntity>of(SparkSwarmEntity::new, MobCategory.MISC)
            .sized(0.3F, 0.3F)
            .clientTrackingRange(8)
            .updateInterval(5)
            .build("spark_swarm"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
