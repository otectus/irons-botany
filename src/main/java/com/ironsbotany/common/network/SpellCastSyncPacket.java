package com.ironsbotany.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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

    public BlockPos getPos() {
        return pos;
    }

    public String getSpellId() {
        return spellId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(spellId);
    }

    public static void handle(SpellCastSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.ironsbotany.client.network.SpellCastClientHandler.handleClient(packet))
        );
        ctx.get().setPacketHandled(true);
    }
}
