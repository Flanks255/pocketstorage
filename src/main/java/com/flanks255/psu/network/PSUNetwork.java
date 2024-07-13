package com.flanks255.psu.network;

import com.flanks255.psu.PocketStorage;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class PSUNetwork {
    public static void register(final RegisterPayloadHandlersEvent event) {
        event.registrar(PocketStorage.MODID)
                .playToServer(SlotClickPacket.TYPE, SlotClickPacket.CODEC, SlotClickPacket::handle);
    }
}
