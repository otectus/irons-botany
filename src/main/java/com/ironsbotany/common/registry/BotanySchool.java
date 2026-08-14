package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

/**
 * The single canonical definition of the Iron's Botany <b>Botany</b> school identity.
 *
 * <p>This class deliberately depends on nothing but Minecraft. {@link IBSchools} — which
 * must import Iron's Spells 'n Spellbooks to build the actual {@code SchoolType} — reads
 * its identity from here, as does every spell's {@code DefaultConfig}, the migration
 * service, and the datapack overrides under
 * {@code data/ironsbotany/irons_spellbooks_spell_config/}. Keeping the constants free of
 * ISS types is what lets them be unit-tested without a game runtime.
 *
 * <h3>Why {@link #DISPLAY_STYLE} is not optional</h3>
 * ISS's {@code SchoolType} constructor caches {@code displayName.getStyle()}, and
 * {@code SchoolType#getTargetingColor()} then evaluates
 * {@code displayStyle.getColor().getValue()} with <em>no null check</em>. A
 * {@code Component.translatable(key)} with no style carries {@link Style#EMPTY}, whose
 * {@code getColor()} is {@code null} — so an unstyled school display name makes
 * {@code getTargetingColor()} throw {@link NullPointerException}.
 *
 * <p>That getter is not obscure: ISS's own {@code AbstractSpell#getTargetingColor()}
 * delegates to it, and roughly twenty ISS classes call it, including
 * {@code render.SpellTargetingLayer} and {@code gui.overlays.RecastOverlay}. Add-ons that
 * inspect spell metadata (ISS-Scroll Descriptions, ISS-Restrictions) reach it too. In
 * 1.9.0 this crashed whenever anything asked a Botany spell for its targeting colour.
 *
 * <p>{@link BotanySchoolContractTest} pins the contract.
 */
public final class BotanySchool {

    /** Registry path of the school, and the name of its {@code SchoolType} registry entry. */
    public static final String PATH = "botany";

    /**
     * The canonical school resource ID: {@code ironsbotany:botany}.
     *
     * <p>Used for registration, for every spell's {@code DefaultConfig.setSchoolResource},
     * for the shipped spell-config datapack entries, and by the migration service. Nothing
     * in this mod may name a Botany spell's school any other way.
     */
    public static final ResourceLocation ID = new ResourceLocation(IronsBotany.MODID, PATH);

    /** String form of {@link #ID}, for NBT and JSON comparisons. */
    public static final String ID_STRING = IronsBotany.MODID + ":" + PATH;

    /**
     * Historical school ID shipped before 1.6.0. Recognised by the migration service and
     * never written.
     */
    public static final String LEGACY_ID_STRING = IronsBotany.MODID + ":botanical";

    /** ISS's Nature school ID — the school this mod's spells were transitionally filed under. */
    public static final String ISS_NATURE_ID_STRING = "irons_spellbooks:nature";

    /**
     * Botany's display/targeting colour, {@code #3FD8A0} — a mint-teal that sits between
     * Botania's mana palette and plant green.
     *
     * <p>Chosen to be distinguishable from the ISS schools it sits next to
     * ({@code ChatFormatting.GREEN} for Nature, {@code AQUA} for Lightning,
     * {@code DARK_AQUA}) and to clear WCAG AA against Minecraft's tooltip background —
     * roughly 11:1 contrast on the vanilla {@code #100010} tooltip fill.
     */
    public static final int DISPLAY_COLOR = 0x3FD8A0;

    /** The non-null style ISS requires. See the class javadoc for why this must never be empty. */
    public static final Style DISPLAY_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(DISPLAY_COLOR));

    /** Translation key for the school's display name; must exist in {@code en_us.json}. */
    public static final String TRANSLATION_KEY = "school.ironsbotany.botany";

    private BotanySchool() {}

    /**
     * The display component handed to ISS's {@code SchoolType}. Always carries
     * {@link #DISPLAY_STYLE}, so {@code getTargetingColor()} can never see a null colour.
     */
    public static MutableComponent displayName() {
        return Component.translatable(TRANSLATION_KEY).setStyle(DISPLAY_STYLE);
    }

    /**
     * @return {@code true} if {@code schoolId} names the Botany school under any spelling this
     *         mod has ever shipped — the canonical {@code ironsbotany:botany} or the pre-1.6.0
     *         {@code ironsbotany:botanical}. ISS Nature is deliberately <em>not</em> included:
     *         genuine Nature content belongs to Iron's Spells and must not be rewritten.
     */
    public static boolean isBotanyId(String schoolId) {
        return ID_STRING.equals(schoolId) || LEGACY_ID_STRING.equals(schoolId);
    }
}
