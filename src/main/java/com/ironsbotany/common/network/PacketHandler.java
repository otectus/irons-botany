package com.ironsbotany.common.network;

import com.ironsbotany.IronsBotany;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(IronsBotany.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        // Explicit direction (server → client only): hardens against a spoofed
        // client → server packet, and the handler no-ops off-client anyway.
        CHANNEL.messageBuilder(SpellCastSyncPacket.class, packetId++,
                        net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SpellCastSyncPacket::encode)
                .decoder(SpellCastSyncPacket::new)
                .consumerMainThread(SpellCastSyncPacket::handle)
                .add();

        IronsBotany.LOGGER.info("Network packets registered");
    }
}
