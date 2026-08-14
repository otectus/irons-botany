package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.entity.SparkSwarmEntity;
import com.ironsbotany.common.registry.IBAttributes;
import com.ironsbotany.common.registry.IBEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityAttributeHandler {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(IBEntities.SPARK_SWARM.get(), SparkSwarmEntity.createAttributes().build());
    }

    /**
     * Attach the Botany school attributes to players.
     *
     * <p>Without this, {@link IBAttributes#BOTANY_SPELL_POWER} and
     * {@link IBAttributes#BOTANY_MAGIC_RESIST} were registered but present on no entity's
     * {@code AttributeMap}. ISS's {@code SchoolType} guards that case rather than crashing —
     *
     * <pre>
     *   getPowerFor(LivingEntity e) {
     *       return e.getAttributes().hasAttribute(powerAttribute.get())
     *              ? e.getAttributeValue(powerAttribute.get())
     *              : 1.0;                      // ← every Botany bonus silently became 1.0
     *   }
     * </pre>
     *
     * — so through 1.9.0 every Botany power/resistance modifier granted by every ring, staff,
     * amulet and armour set was inert while its tooltip advertised a bonus. Attaching the
     * attributes here is what makes those modifiers observable.
     *
     * <p>Scope is deliberately players only. Botany is a player-cast school: no mob in this
     * mod casts or is designed to resist it, and adding school attributes to every living
     * entity would grow every mob's attribute map for no gameplay reason.
     */
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, IBAttributes.BOTANY_SPELL_POWER.get());
        event.add(EntityType.PLAYER, IBAttributes.BOTANY_MAGIC_RESIST.get());
        event.add(EntityType.PLAYER, IBAttributes.MANA_EFFICIENCY.get());

        IronsBotany.LOGGER.debug("Attached Botany school attributes to players");
    }
}
