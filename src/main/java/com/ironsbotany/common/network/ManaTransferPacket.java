package com.ironsbotany.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ManaTransferPacket {
    private final int botaniaAmount;
    private final int issAmount;

    public ManaTransferPacket(int botaniaAmount, int issAmount) {
        this.botaniaAmount = botaniaAmount;
        this.issAmount = issAmount;
    }

    public ManaTransferPacket(FriendlyByteBuf buf) {
        this.botaniaAmount = buf.readInt();
        this.issAmount = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(botaniaAmount);
        buf.writeInt(issAmount);
    }

    public static void handle(ManaTransferPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // Visual/audio feedback
                player.level().playSound(null, player.blockPosition(),
                        SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.5f, 1.2f);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
