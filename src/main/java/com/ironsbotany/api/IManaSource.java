package com.ironsbotany.api;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.player.Player;

/**
 * Cross-bridge mana source contract. A {@code IManaSource} represents one
 * pool of mana that can be queried for affordability and debited for cost.
 *
 * <p>This interface is the foundation for the planned shared
 * {@code otectus-mana-bridge-common} jar-in-jar (see Iron's Botany
 * enhancement doc §5c). Today only Iron's Botany implements it; the
 * intent is that Ars 'n Spells and any future bridge mod will adopt the
 * same interface so the priority chain becomes a true polymorphic
 * pipeline rather than a series of hard-coded if/else branches inside
 * {@code ManaBridgeManager}.
 *
 * <p>Implementations must be:
 * <ul>
 *   <li><b>Side-aware</b>: client-side calls should return {@code false}
 *       from {@link #canAfford} without I/O. The bridge manager
 *       short-circuits client paths but defensive impls help.</li>
 *   <li><b>Idempotent under simulation</b>: {@code canAfford} must not
 *       mutate state. {@code charge} mutates and is called only after
 *       {@code canAfford} returned true.</li>
 *   <li><b>Race-tolerant</b>: between the {@code canAfford} check and the
 *       {@code charge} call another tick may have drained the pool;
 *       implementations should re-check inside {@code charge} and return
 *       false if the resource has gone away.</li>
 * </ul>
 */
public interface IManaSource {

    /** Stable identifier — must match a {@code ManaSource} enum token. */
    String getId();

    /** Whether this source has at least {@code amount} units available now. */
    boolean canAfford(Player player, AbstractSpell spell, int level, int amount);

    /**
     * Drain {@code amount} units from this source.
     *
     * @return true if the full amount was charged. False if the source
     *         could not cover the cost; the caller should fall through
     *         to the next source in the priority chain.
     */
    boolean charge(Player player, AbstractSpell spell, int level, int amount);
}
