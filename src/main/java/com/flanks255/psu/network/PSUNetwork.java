package com.flanks255.psu.network;

import com.flanks255.psu.PocketStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PSUNetwork {
    public static final ResourceLocation channelName = new ResourceLocation(PocketStorage.MODID, "network");
    public static final String networkVersion = new ResourceLocation(PocketStorage.MODID, "1").toString();

    public static SimpleChannel getNetworkChannel() {
        final SimpleChannel channel = NetworkRegistry.ChannelBuilder.named(channelName)
            .clientAcceptedVersions(version -> true)
            .serverAcceptedVersions(version -> true)
            .networkProtocolVersion(() -> networkVersion)
            .simpleChannel();

        channel.registerMessage(1, SlotClickMessage.class, SlotClickMessage::encode, SlotClickMessage::decode, SlotClickMessage::handle);

        return channel;
    }
}
