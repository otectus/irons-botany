package com.ironsbotany.common.flower;

import net.minecraft.core.BlockPos;

/**
 * Represents an active flower aura affecting a player
 */
public class ActiveFlowerAura {
    private final FlowerAura aura;
    private final BlockPos position;
    private final float strength;
    
    public ActiveFlowerAura(FlowerAura aura, BlockPos position, float strength) {
        this.aura = aura;
        this.position = position;
        this.strength = strength;
    }
    
    public FlowerAura getAura() {
        return aura;
    }
    
    public BlockPos getPosition() {
        return position;
    }
    
    public float getStrength() {
        return strength;
    }
}
