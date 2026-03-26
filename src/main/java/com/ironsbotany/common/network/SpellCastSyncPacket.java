package com.ironsbotany.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SpellCastSyncPacket {
    private final BlockPos pos;
    private final String spellId;

    public SpellCastSyncPacket(BlockPos pos, String spellId) {
        this.pos = pos;
        this.spellId = spellId;
    }

    public SpellCastSyncPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.spellId = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(spellId);
    }

    public static void handle(SpellCastSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet));
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleClient(SpellCastSyncPacket packet) {
        Level level = net.minecraft.client.Minecraft.getInstance().level;
        if (level == null) return;

        BlockPos pos = packet.pos;
        for (int i = 0; i < 20; i++) {
            level.addParticle(ParticleTypes.CHERRY_LEAVES,
                pos.getX() + 0.5 + level.random.nextGaussian() * 0.5,
                pos.getY() + 1.0 + level.random.nextDouble(),
                pos.getZ() + 0.5 + level.random.nextGaussian() * 0.5,
                0, 0.05, 0);
        }
    }
}
