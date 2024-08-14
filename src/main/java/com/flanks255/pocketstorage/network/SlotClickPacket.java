package com.flanks255.pocketstorage.network;

import com.flanks255.pocketstorage.PocketStorage;
import com.flanks255.pocketstorage.gui.PSUContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SlotClickPacket(int slotID, boolean shift, boolean ctrl, boolean rightClick) implements CustomPacketPayload {
    public static final Type<SlotClickPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PocketStorage.MODID, "slotclick"));

    public static final StreamCodec<FriendlyByteBuf, SlotClickPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SlotClickPacket::slotID,
            ByteBufCodecs.BOOL, SlotClickPacket::shift,
            ByteBufCodecs.BOOL, SlotClickPacket::ctrl,
            ByteBufCodecs.BOOL, SlotClickPacket::rightClick,
            SlotClickPacket::new
            );


    public static void handle(final SlotClickPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(
            () -> {
                ((PSUContainer) ctx.player().containerMenu).networkSlotClick(packet.slotID, packet.shift, packet.ctrl, packet.rightClick);
            });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
