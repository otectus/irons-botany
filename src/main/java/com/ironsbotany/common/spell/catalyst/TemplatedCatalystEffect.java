package com.ironsbotany.common.spell.catalyst;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generic, data-driven {@link CatalystEffect} produced by
 * {@link CatalystDataLoader} from JSON definitions under
 * {@code data/ironsbotany/catalysts/}.
 *
 * <p>Schema:</p>
 * <pre>{@code
 * {
 *   "tier": "ADVANCED",
 *   "modifiers": {
 *     "damage": 1.2,
 *     "cooldown": 0.9,
 *     "cast_speed": 1.1,
 *     "mana_cost": 0.95,
 *     "burst": 1.15
 *   },
 *   "applies_to_schools": ["irons_spellbooks:nature"]
 * }
 * }</pre>
 *
 * <p>Java {@link CatalystEffect} implementations remain for catalysts that
 * need bespoke logic (mob effects, custom data, school-conditional damage);
 * templated catalysts cover the broad "rune × multiplier" majority.</p>
 */
public class TemplatedCatalystEffect implements CatalystEffect {

    private final ResourceLocation id;
    private final CatalystTier tier;
    private final float damageMul;
    private final float cooldownMul;
    private final float castSpeedMul;
    private final float manaCostMul;
    private final float burstMul;
    private final Set<ResourceLocation> applicableSchools;

    public TemplatedCatalystEffect(ResourceLocation id, CatalystTier tier,
                                   float damageMul, float cooldownMul,
                                   float castSpeedMul, float manaCostMul,
                                   float burstMul,
                                   Set<ResourceLocation> applicableSchools) {
        this.id = id;
        this.tier = tier;
        this.damageMul = damageMul;
        this.cooldownMul = cooldownMul;
        this.castSpeedMul = castSpeedMul;
        this.manaCostMul = manaCostMul;
        this.burstMul = burstMul;
        this.applicableSchools = applicableSchools;
    }

    public static TemplatedCatalystEffect fromJson(ResourceLocation id, JsonObject root) {
        CatalystTier tier = CatalystTier.valueOf(
            GsonHelper.getAsString(root, "tier", "BASIC").toUpperCase());

        JsonObject mods = GsonHelper.getAsJsonObject(root, "modifiers", new JsonObject());
        float damage = (float) GsonHelper.getAsDouble(mods, "damage", 1.0);
        float cooldown = (float) GsonHelper.getAsDouble(mods, "cooldown", 1.0);
        float castSpeed = (float) GsonHelper.getAsDouble(mods, "cast_speed", 1.0);
        float manaCost = (float) GsonHelper.getAsDouble(mods, "mana_cost", 1.0);
        float burst = (float) GsonHelper.getAsDouble(mods, "burst", 1.0);

        Set<ResourceLocation> schools = new HashSet<>();
        JsonArray schoolsArr = GsonHelper.getAsJsonArray(root, "applies_to_schools", new JsonArray());
        schoolsArr.forEach(el -> {
            ResourceLocation rl = ResourceLocation.tryParse(el.getAsString());
            if (rl != null) schools.add(rl);
        });

        return new TemplatedCatalystEffect(id, tier, damage, cooldown, castSpeed, manaCost, burst, schools);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean appliesTo(AbstractSpell spell) {
        if (applicableSchools.isEmpty()) return true;
        SchoolType school = spell.getSchoolType();
        if (school == null) return false;
        return applicableSchools.contains(school.getId());
    }

    @Override
    public void modifySpell(AbstractSpell spell, SpellContext context) {
        if (damageMul != 1.0f) context.multiplyDamage(damageMul);
        if (cooldownMul != 1.0f) context.multiplyCooldown(cooldownMul);
        if (castSpeedMul != 1.0f) context.multiplyCastingSpeed(castSpeedMul);
        if (manaCostMul != 1.0f) context.multiplyManaCost(manaCostMul);
        if (burstMul != 1.0f) context.multiplyDamage(burstMul);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("catalyst.ironsbotany." + id.getPath())
            .withStyle(tierColor(tier));
    }

    @Override
    public Component getDescription() {
        return Component.translatable("catalyst.ironsbotany." + id.getPath() + ".desc");
    }

    @Override
    public CatalystTier getTier() {
        return tier;
    }

    private static ChatFormatting tierColor(CatalystTier tier) {
        return switch (tier) {
            case BASIC -> ChatFormatting.WHITE;
            case ADVANCED -> ChatFormatting.BLUE;
            case ELITE -> ChatFormatting.DARK_PURPLE;
            case LEGENDARY -> ChatFormatting.GOLD;
        };
    }
}
