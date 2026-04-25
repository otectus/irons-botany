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
 * 12-slot Rare-tier spellbook themed around Botania's Terrasteel
 * progression. Carries a multimap of stat bumps:
 * <ul>
 *   <li>+200 max mana</li>
 *   <li>+15% Botany spell power</li>
 *   <li>+10% Nature spell power</li>
 *   <li>+0.10 mana efficiency</li>
 * </ul>
 *
 * <p>Mana network participation (200,000 buffer) is wired in
 * {@code IBCapabilityHandler.capacityFor}.
 *
 * <p>Note: {@link SimpleAttributeSpellBook} is marked for-removal in
 * a future ISS release. We pin to 3.15.2 explicitly and accept the
 * warning until that version drops; the migration to a direct
 * {@code SpellBook.withAttribute} chain is a one-file change.
 */
@SuppressWarnings("removal")
public class TerrasteelSpellbookItem extends SimpleAttributeSpellBook {

    private static final UUID UUID_MAX_MANA = UUID.fromString("d7e1f2a0-1c1c-4b1c-9d1c-1c1c4b1c9d1c");
    private static final UUID UUID_BOTANY_POWER = UUID.fromString("d7e1f2a0-1c1c-4b1c-9d1c-1c1c4b1c9d1d");
    private static final UUID UUID_NATURE_POWER = UUID.fromString("d7e1f2a0-1c1c-4b1c-9d1c-1c1c4b1c9d1e");
    private static final UUID UUID_MANA_EFF = UUID.fromString("d7e1f2a0-1c1c-4b1c-9d1c-1c1c4b1c9d1f");

    public TerrasteelSpellbookItem() {
        super(12, SpellRarity.RARE, buildAttributes(),
                new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    private static Multimap<Attribute, AttributeModifier> buildAttributes() {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        map.put(AttributeRegistry.MAX_MANA.get(),
                new AttributeModifier(UUID_MAX_MANA, "ironsbotany.terrasteel_spellbook.mana",
                        200.0, AttributeModifier.Operation.ADDITION));
        map.put(IBAttributes.BOTANY_SPELL_POWER.get(),
                new AttributeModifier(UUID_BOTANY_POWER, "ironsbotany.terrasteel_spellbook.botany_power",
                        0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        map.put(AttributeRegistry.NATURE_SPELL_POWER.get(),
                new AttributeModifier(UUID_NATURE_POWER, "ironsbotany.terrasteel_spellbook.nature_power",
                        0.10, AttributeModifier.Operation.MULTIPLY_BASE));
        map.put(IBAttributes.MANA_EFFICIENCY.get(),
                new AttributeModifier(UUID_MANA_EFF, "ironsbotany.terrasteel_spellbook.mana_efficiency",
                        0.10, AttributeModifier.Operation.ADDITION));
        return map;
    }
}
