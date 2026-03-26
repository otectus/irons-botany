package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.entity.SparkSwarmEntity;
import com.ironsbotany.common.registry.IBEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityAttributeHandler {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(IBEntities.SPARK_SWARM.get(), SparkSwarmEntity.createAttributes().build());

        IronsBotany.LOGGER.info("Registered entity attributes");
    }
}
