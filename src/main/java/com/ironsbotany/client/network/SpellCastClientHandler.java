package com.ironsbotany.client.network;

import com.ironsbotany.common.network.SpellCastSyncPacket;
import com.ironsbotany.common.registry.IBParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class SpellCastClientHandler {
    private SpellCastClientHandler() {}

    public static void handleClient(SpellCastSyncPacket packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        BlockPos pos = packet.getPos();
        for (int i = 0; i < 20; i++) {
            level.addParticle(IBParticles.PETAL_MAGIC.get(),
                pos.getX() + 0.5 + level.random.nextGaussian() * 0.5,
                pos.getY() + 1.0 + level.random.nextDouble(),
                pos.getZ() + 0.5 + level.random.nextGaussian() * 0.5,
                0, 0.05, 0);
        }
    }
}
