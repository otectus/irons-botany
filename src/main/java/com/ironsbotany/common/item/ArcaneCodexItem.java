package com.ironsbotany.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ironsbotany.common.registry.IBAttributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.spell_books.SimpleAttributeSpellBook;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.UUID;

/**
 * 14-slot Epic-tier legendary spellbook crafted at the Botania Terra
 * Plate. The endgame Botany progression target.
 *
 * <p>Stats:
 * <ul>
 *   <li>+300 max mana</li>
 *   <li>+20% cooldown reduction</li>
 *   <li>+15% Botany spell power</li>
 *   <li>+10% spell power (all schools)</li>
 *   <li>+0.15 mana efficiency</li>
 * </ul>
 *
 * <p>Mana network participation (500,000 buffer) is wired in
 * {@code IBCapabilityHandler.capacityFor}.
 *
 * <p>Suppresses the ISS deprecation warning per the project pattern —
 * we're pinned to 3.15.2 and {@link SimpleAttributeSpellBook} is the
 * cleanest constructor surface available.
 */
@SuppressWarnings("removal")
public class ArcaneCodexItem extends SimpleAttributeSpellBook {

    private static final UUID UUID_MAX_MANA = UUID.fromString("8a1d3e6f-2b2b-4c2c-8e2c-2c2c4c2c8e2c");
    private static final UUID UUID_CDR = UUID.fromString("8a1d3e6f-2b2b-4c2c-8e2c-2c2c4c2c8e2d");
    private static final UUID UUID_BOTANY_POWER = UUID.fromString("8a1d3e6f-2b2b-4c2c-8e2c-2c2c4c2c8e2e");
    private static final UUID UUID_SPELL_POWER = UUID.fromString("8a1d3e6f-2b2b-4c2c-8e2c-2c2c4c2c8e2f");
    private static final UUID UUID_MANA_EFF = UUID.fromString("8a1d3e6f-2b2b-4c2c-8e2c-2c2c4c2c8e30");

    public ArcaneCodexItem() {
        super(14, SpellRarity.EPIC, buildAttributes(),
                new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    private static Multimap<Attribute, AttributeModifier> buildAttributes() {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        map.put(AttributeRegistry.MAX_MANA.get(),
                new AttributeModifier(UUID_MAX_MANA, "ironsbotany.arcane_codex.mana",
                        300.0, AttributeModifier.Operation.ADDITION));
        map.put(AttributeRegistry.COOLDOWN_REDUCTION.get(),
                new AttributeModifier(UUID_CDR, "ironsbotany.arcane_codex.cdr",
                        0.20, AttributeModifier.Operation.MULTIPLY_BASE));
        map.put(IBAttributes.BOTANY_SPELL_POWER.get(),
                new AttributeModifier(UUID_BOTANY_POWER, "ironsbotany.arcane_codex.botany_power",
                        0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        map.put(AttributeRegistry.SPELL_POWER.get(),
                new AttributeModifier(UUID_SPELL_POWER, "ironsbotany.arcane_codex.spell_power",
                        0.10, AttributeModifier.Operation.MULTIPLY_BASE));
        map.put(IBAttributes.MANA_EFFICIENCY.get(),
                new AttributeModifier(UUID_MANA_EFF, "ironsbotany.arcane_codex.mana_efficiency",
                        0.15, AttributeModifier.Operation.ADDITION));
        return map;
    }
}
