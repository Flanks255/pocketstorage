package com.flanks255.pocketstorage.network;

import com.flanks255.pocketstorage.PocketStorage;
import com.flanks255.pocketstorage.gui.PSUContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SlotKeyPacket(int slotID, Key key, boolean ctrl) implements CustomPacketPayload {
    public static final Type<SlotKeyPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PocketStorage.MODID, "slotkey"));
    public enum Key {
        DROP
    }

    public static final StreamCodec<FriendlyByteBuf, SlotKeyPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SlotKeyPacket::slotID,
            NeoForgeStreamCodecs.enumCodec(Key.class), SlotKeyPacket::key,
            ByteBufCodecs.BOOL, SlotKeyPacket::ctrl,
            SlotKeyPacket::new
            );


    public static void handle(final SlotKeyPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(
            () -> {
                ((PSUContainer) ctx.player().containerMenu).networkSlotKeyPress(packet.slotID, packet.key, packet.ctrl);
            });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
