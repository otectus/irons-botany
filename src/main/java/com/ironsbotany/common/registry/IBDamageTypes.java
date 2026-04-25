package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

/**
 * Datapack-backed damage type identifiers for Iron's Botany. The
 * actual {@link DamageType} JSON lives at
 * {@code data/ironsbotany/damage_type/botany.json}; this class only
 * exposes the {@link ResourceKey} so {@link IBSchools#BOTANY} can
 * reference it at construction time.
 *
 * <p>Damage-type resource keys are not registered via DeferredRegister —
 * they are just identifiers that the datapack supplies. The JSON is
 * required for the key to resolve at runtime.
 */
public final class IBDamageTypes {

    public static final ResourceKey<DamageType> BOTANY = ResourceKey.create(
            net.minecraft.core.registries.Registries.DAMAGE_TYPE,
            new ResourceLocation(IronsBotany.MODID, "botany"));

    private IBDamageTypes() {}
}
