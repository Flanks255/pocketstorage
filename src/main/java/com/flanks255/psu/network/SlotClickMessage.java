package com.flanks255.psu.network;

import com.flanks255.psu.gui.PSUContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;

import java.util.function.Supplier;

public class SlotClickMessage {
    public SlotClickMessage(int slotIDIn, boolean shiftIn, boolean ctrlIn, boolean rightClickIn) {
        slotID = slotIDIn;
        shift = shiftIn;
        ctrl = ctrlIn;
        rightClick = rightClickIn;
    }
    int slotID;
    boolean shift;
    boolean ctrl;
    boolean rightClick;

    public static SlotClickMessage decode(final PacketBuffer buffer) {
        return new SlotClickMessage(buffer.readInt(),buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }

    public static void encode(final SlotClickMessage message, final PacketBuffer buffer) {
        buffer.writeInt(message.slotID);
        buffer.writeBoolean(message.shift);
        buffer.writeBoolean(message.ctrl);
        buffer.writeBoolean(message.rightClick);
    }

    public static void handle(final SlotClickMessage message, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(
                () -> {
                    PlayerEntity player = ctx.get().getSender();
                    if (player.openContainer instanceof PSUContainer) {
                        ((PSUContainer) player.openContainer).networkSlotClick(message.slotID, message.shift, message.ctrl, message.rightClick);
                    }
                });
        ctx.get().setPacketHandled(true);
    }
}
