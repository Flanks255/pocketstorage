package com.flanks255.psu.network;

import com.flanks255.psu.PocketStorage;
import com.flanks255.psu.gui.PSUContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SlotClickPacket(int slotID, boolean shift, boolean ctrl, boolean rightClick) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(PocketStorage.MODID, "slotclick");
    public SlotClickPacket(final FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }

    public static void handle(final SlotClickPacket packet, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(
            () -> {
                ctx.player().ifPresent(p -> ((PSUContainer) p.containerMenu).networkSlotClick(packet.slotID, packet.shift, packet.ctrl, packet.rightClick));
            });
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(slotID);
        buffer.writeBoolean(shift);
        buffer.writeBoolean(ctrl);
        buffer.writeBoolean(rightClick);

    }

    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}
