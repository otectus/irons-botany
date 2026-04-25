package com.ironsbotany.datagen;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.registry.IBAttributes;
import com.ironsbotany.common.registry.IBBlocks;
import com.ironsbotany.common.registry.IBItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * Generates en_us.json. The hand-written file at
 * {@code assets/ironsbotany/lang/en_us.json} stays for now — it
 * carries non-item strings (spell descriptions, mana-event flavor,
 * Patchouli page text) that the registries don't see. As Phase 6 adds
 * new items, those strings land here; the existing hand-written file
 * absorbs the gap until a future provider migration sweeps it.
 *
 * <p>Other locales (25 currently in the repo) are NOT generated — they
 * are translator-maintained.
 */
public class IBLanguageProvider extends LanguageProvider {

    public IBLanguageProvider(PackOutput output) {
        super(output, IronsBotany.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // Creative tab + school
        add("itemGroup.ironsbotany", "Iron's Botany");
        add("school.ironsbotany.botany", "Botany");

        // Attributes — keyed by descriptionId since LanguageProvider.add has no Attribute overload
        add(IBAttributes.MANA_EFFICIENCY.get().getDescriptionId(), "Mana Efficiency");
        add(IBAttributes.BOTANY_SPELL_POWER.get().getDescriptionId(), "Botany Spell Power");
        add(IBAttributes.BOTANY_MAGIC_RESIST.get().getDescriptionId(), "Botany Magic Resistance");

        // Blocks
        add(IBBlocks.SPELL_RESERVOIR.get(), "Spell Reservoir");
        add(IBBlocks.MANA_CONDUIT.get(), "Mana Conduit");
        add(IBBlocks.ARCANE_MANA_ALTAR.get(), "Arcane Mana Altar");

        // Items registered in IBItems get auto-named via the registry name
        // — bulk-add with title-case conversion so we don't silently miss
        // a new entry.
        for (var entry : IBItems.ITEMS.getEntries()) {
            String path = entry.getId().getPath();
            // Skip block-items (already added above as blocks)
            if (path.equals("spell_reservoir")
                    || path.equals("mana_conduit")
                    || path.equals("arcane_mana_altar")) continue;
            add(entry.get(), titleCase(path));
        }
    }

    private static String titleCase(String snake) {
        StringBuilder out = new StringBuilder();
        boolean upper = true;
        for (char c : snake.toCharArray()) {
            if (c == '_') { out.append(' '); upper = true; }
            else if (upper) { out.append(Character.toUpperCase(c)); upper = false; }
            else out.append(c);
        }
        return out.toString();
    }
}
