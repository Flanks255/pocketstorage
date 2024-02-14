package com.flanks255.psu.network;

import com.flanks255.psu.PocketStorage;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;

public class PSUNetwork {
    public static void register(final RegisterPayloadHandlerEvent event) {
        event.registrar(PocketStorage.MODID)
            .play(SlotClickPacket.ID, SlotClickPacket::new, handler -> handler.server(SlotClickPacket::handle));
    }
}
