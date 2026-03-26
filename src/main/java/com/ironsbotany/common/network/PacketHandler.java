package com.ironsbotany.common.network;

import com.ironsbotany.IronsBotany;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.messageBuilder(SpellCastSyncPacket.class, packetId++)
                .encoder(SpellCastSyncPacket::encode)
                .decoder(SpellCastSyncPacket::new)
                .consumerMainThread(SpellCastSyncPacket::handle)
                .add();

        IronsBotany.LOGGER.info("Network packets registered");
    }
}
