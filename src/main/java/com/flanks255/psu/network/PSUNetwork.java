package com.flanks255.psu.network;

import com.flanks255.psu.PocketStorage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PSUNetwork {
    public static final ResourceLocation channelName = new ResourceLocation(PocketStorage.MODID, "network");
    public static final String networkVersion = new ResourceLocation(PocketStorage.MODID, "1").toString();

    public static SimpleChannel getNetworkChannel() {
     final SimpleChannel channel = NetworkRegistry.ChannelBuilder.named(channelName)
        .clientAcceptedVersions(version -> true)
        .serverAcceptedVersions(version -> true)
        .networkProtocolVersion(() -> networkVersion)
        .simpleChannel();


        channel.messageBuilder(SlotClickMessage.class, 1)
                .decoder(SlotClickMessage::decode)
                .encoder(SlotClickMessage::encode)
                .consumer(SlotClickMessage::handle)
                .add();


        return channel;
    }
}
