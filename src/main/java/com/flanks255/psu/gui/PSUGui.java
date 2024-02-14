package com.flanks255.psu.gui;

import com.flanks255.psu.PocketStorage;
import com.flanks255.psu.network.SlotClickPacket;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.List;

public class PSUGui extends AbstractContainerScreen<PSUContainer> {
    public PSUGui (PSUContainer container, Inventory inventory, Component name) {
        super(container, inventory, name);
        imageWidth = 176;
        imageHeight = 180;
    }

    @Override
    protected void init() {
        super.init();

        Button.OnPress slotClick = button -> {
            PacketDistributor.SERVER.noArg().send(new SlotClickPacket(((GUISlot)button).slot + scroll, Screen.hasShiftDown(), Screen.hasControlDown(), false));
            menu.networkSlotClick(((GUISlot)button).slot+scroll, Screen.hasShiftDown(), Screen.hasControlDown(), false);
        };

        addRenderableWidget(new GUISlot(leftPos + 8, topPos + 19, 34,36,0 , slotClick));
        addRenderableWidget(new GUISlot(leftPos + 8 + 36, topPos + 19, 34,36,1 , slotClick));
        addRenderableWidget(new GUISlot(leftPos + 8 + 72, topPos + 19, 34,36,2 , slotClick));
        addRenderableWidget(new GUISlot(leftPos + 8 + 108, topPos + 19, 34,36,3 , slotClick));
        addRenderableWidget(new GUISlot(leftPos + 8, topPos + 19 + 38, 34,36,4 , slotClick));
        addRenderableWidget(new GUISlot(leftPos + 8 + 36, topPos + 19 + 38, 34,36,5 , slotClick));
        addRenderableWidget(new GUISlot(leftPos + 8 + 72, topPos + 19 + 38, 34,36,6 , slotClick));
        addRenderableWidget(new GUISlot(leftPos + 8 + 108, topPos + 19 + 38, 34,36,7 , slotClick));
        addRenderableWidget(new ScrollButton(leftPos+ 152,topPos + 18, 16,37, true, (A) ->  scroll = scroll <= 0?0:scroll - 4 ));
        addRenderableWidget(new ScrollButton(leftPos + 152,topPos + 55, 16,37, false, (A) ->  scroll = scroll >= menu.handler.getSlots()-8?menu.handler.getSlots()-8:scroll + 4 ));
    }

    private final ResourceLocation GUI = new ResourceLocation(PocketStorage.MODID, "textures/gui/psugui.png");
    private int scroll = 0;

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (pScrollY < 0)
            scroll = Mth.clamp(scroll + 4, 0, menu.handler.getSlots() -8);
        if (pScrollY > 0)
            scroll = Mth.clamp(scroll - 4, 0, menu.handler.getSlots() -8);
        return false;
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics gg, float partialTicks, int mouseX, int mouseY) {
        gg.blit(GUI, leftPos, topPos, 0,0, 176,180, 176 ,180);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics gg, int mouseX, int mouseY) {
        gg.drawString(font, this.title.getString(), 7,6,0x404040, false);
    }

    @Override
    public void render(@Nonnull GuiGraphics gg, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gg, mouseX, mouseY, partialTicks);
        super.render(gg, mouseX, mouseY, partialTicks);
        this.renderTooltip(gg, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(@Nonnull GuiGraphics gg, int x, int y) {
        children().forEach(listener -> {
            if (listener instanceof GUISlot)
                ((GUISlot) listener).renderToolTip(gg, x, y);
        });
        super.renderTooltip(gg, x, y);
    }

    class ScrollButton extends Button {
        public ScrollButton (int x, int y, int width, int height, boolean upIn, Button.OnPress pressable) {
            super(x,y,width,height,Component.empty(),pressable, Button.DEFAULT_NARRATION);
            up = upIn;
        }
        private final boolean up;
        private final ResourceLocation TEX = new ResourceLocation(PocketStorage.MODID, "textures/gui/buttons.png");


        @Override
        public void renderWidget(@Nonnull GuiGraphics gg, int mouseX, int mouseY, float partialTicks) {
            if (mouseX >= getX() && mouseX < getX() + width && mouseY >= getY() && mouseY < getY() + height)
                gg.blit(TEX, getX(), getY(), 16, up?0:37, 16, 37, 32, 74);
            else
                gg.blit(TEX, getX(), getY(), 0, up?0:37, 16, 37, 32, 74);
        }
    }


    class GUISlot extends Button {
        public GUISlot(int x, int y, int width, int height,int slotIn, Button.OnPress pressable) {
            super(x, y, width, height, Component.empty(), pressable, Button.DEFAULT_NARRATION);
            this.slot = slotIn;
        }
        public final int slot;

        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            if (this.active && this.visible) {
                if (pButton == 0 && this.clicked(pMouseX,pMouseY)) {
                    //this.playDownSound(Minecraft.getInstance().getSoundHandler());
                    this.onClick(pMouseX, pMouseY);
                    return true;
                } else if (pButton == 1 && this.clicked(pMouseX,pMouseY)) {
                    PacketDistributor.SERVER.noArg().send(new SlotClickPacket(slot + scroll, Screen.hasShiftDown(), Screen.hasControlDown(), true));
                    menu.networkSlotClick(slot+scroll, Screen.hasShiftDown(), Screen.hasControlDown(), true);
                    return true;
                }
            }
            return false;
        }

        public void renderToolTip(@Nonnull GuiGraphics gg, int mx, int my) {
            if (mx >= getX() && mx < getX() + width && my >= getY() && my < getY() + height && menu != null && menu.handler != null) {
                ItemStack stack = menu.handler.getStackInSlot(slot + scroll);
                if(!stack.isEmpty()) {
                    List<Component> tooltip = getTooltipFromItem(Minecraft.getInstance(), stack);
                    tooltip.add(Component.translatable("pocketstorage.util.count").withStyle(ChatFormatting.WHITE).append(String.valueOf(stack.getCount())));
                    //renderTooltip with list
                    gg.renderComponentTooltip(font, tooltip, mx, my);
                }
            }
        }

        public String formatAmount(int input) {
            if (input >= 1000 && input < 1000000)
                return String.format("%.2fK", input / 1000.0f);
            else if (input >= 1000000 && input < 1000000000)
                return String.format("%.2fM", input / 1000000.0f);
            else if (input >= 1000000000)
                return String.format("%.2fG", input / 1000000000.0f);
            else
                return String.valueOf(input);
        }

        @Override
        public void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTicks) {
            gg.pose().pushPose();
            Font fontRenderer = Minecraft.getInstance().font;

            boolean hovered = mouseX >= getX() && mouseX < getX() + width && mouseY >= getY() && mouseY < getY() + height;

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            if (hovered) {
                gg.fill(getX(), getY() - 1, getX() + width, getY() + height, -2130706433);
            }
            gg.pose().pushPose();
            gg.pose().translate(getX() + 0.5, getY() + 0.5, 0);
            gg.pose().scale(0.5f, 0.5f, 0.5f);
            gg.drawString(font,"#" + (slot + scroll), 0, 0, 0x454545, false);
            gg.pose().popPose();
            if (menu.handler != null) {
                ItemStack tmp = menu.handler.getStackInSlot(slot + scroll);
                if (tmp != null) {
                    //itemRenderer.blitOffset = 100F;
                    RenderSystem.enableDepthTest();
                    Lighting.setupForFlatItems();
                    gg.renderItem(tmp, getX() + 9, getY() + 4);
                    if (tmp.getCount() > 0) {
                        String count = Integer.toString(tmp.getCount());
                        int stringWidth = fontRenderer.width(count);

                        gg.drawString(font, formatAmount(tmp.getCount()), getX() + 1 + (width / 2) - (stringWidth / 2), getY() + 22, 0x000000, false);
                    } else
                        gg.drawString(font, Component.translatable("pocketstorage.util.empty"), getX() + 1 + (width / 2) - (fontRenderer.width(Component.translatable("pocketstorage.util.empty")) / 2), getY() + 20, 0x000000, false);
                    //itemRenderer.blitOffset = 0F;
                    Lighting.setupFor3DItems();
                }
            }
            gg.pose().popPose();
        }
    }
}
