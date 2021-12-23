package com.flanks255.psu.network;

import com.flanks255.psu.gui.PSUContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SlotClickMessage {
    public SlotClickMessage(int slotIDIn, boolean shiftIn, boolean ctrlIn, boolean rightClickIn) {
        slotID = slotIDIn;
        shift = shiftIn;
        ctrl = ctrlIn;
        rightClick = rightClickIn;
    }
    private final int slotID;
    private final boolean shift;
    private final boolean ctrl;
    private final boolean rightClick;

    public static SlotClickMessage decode(final FriendlyByteBuf buffer) {
        return new SlotClickMessage(buffer.readInt(),buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }

    public static void encode(final SlotClickMessage message, final FriendlyByteBuf buffer) {
        buffer.writeInt(message.slotID);
        buffer.writeBoolean(message.shift);
        buffer.writeBoolean(message.ctrl);
        buffer.writeBoolean(message.rightClick);
    }

    public static void handle(final SlotClickMessage message, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(
            () -> {
                Player player = ctx.get().getSender();
                if (player.containerMenu instanceof PSUContainer) {
                    ((PSUContainer) player.containerMenu).networkSlotClick(message.slotID, message.shift, message.ctrl, message.rightClick);
                }
            });
        ctx.get().setPacketHandled(true);
    }
}
