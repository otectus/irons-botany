package com.ironsbotany.common.util;

/**
 * Fair division of one per-tick transfer budget across the recipients in range.
 *
 * <p>Extracted so the arithmetic is testable without a world. The Spell Reservoir and Mana Conduit
 * both used to grant <em>each</em> nearby player a full {@code blockEntityTransferRate} every tick,
 * which made the configured rate per-player in practice while the config described it as the
 * block's — a reservoir beside four players drained four times as fast as advertised. Service order
 * also followed the entity list, so whoever happened to be first was served first every tick and
 * could starve the others once mana ran low.
 */
public final class TransferBudget {

    private TransferBudget() {}

    /**
     * This recipient's share of {@code budget}.
     *
     * <p>Splits evenly and hands the remainder to the first {@code budget % count} recipients in
     * rotated order, so the whole budget is used and no recipient is permanently favoured.
     *
     * @param budget         total units available this tick; non-positive yields 0
     * @param count          number of recipients; non-positive yields 0
     * @param rotatedIndex   this recipient's index <em>after</em> rotation, in {@code [0, count)}
     * @return the units this recipient may take
     */
    public static int shareFor(int budget, int count, int rotatedIndex) {
        if (budget <= 0 || count <= 0) return 0;
        if (rotatedIndex < 0 || rotatedIndex >= count) return 0;
        return budget / count + (rotatedIndex < budget % count ? 1 : 0);
    }

    /**
     * Which recipient index is served first this tick.
     *
     * <p>Rotating by the world clock means the remainder — and the head of the queue when supply
     * runs short — moves between recipients instead of always landing on the same one.
     */
    public static int rotationFor(long gameTime, int count) {
        if (count <= 0) return 0;
        return (int) Math.floorMod(gameTime, count);
    }
}
