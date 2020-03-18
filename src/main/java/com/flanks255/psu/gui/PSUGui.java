package com.flanks255.psu.gui;

import com.flanks255.psu.PocketStorage;
import com.flanks255.psu.network.SlotClickMessage;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class PSUGui extends ContainerScreen<PSUContainer> {
    public PSUGui (PSUContainer container, PlayerInventory inventory, ITextComponent name) {
        super(container, inventory, name);
        xSize = 176;
        ySize = 180;
    }

    @Override
    protected void init() {
        super.init();

        Button.IPressable slotclick = new Button.IPressable() {
            @Override
            public void onPress(Button button) {
                PocketStorage.network.sendToServer(new SlotClickMessage(((GUISlot)button).slot + scroll, Screen.hasShiftDown(), Screen.hasControlDown(), false));
                container.networkSlotClick(((GUISlot)button).slot+scroll, Screen.hasShiftDown(), Screen.hasControlDown(), false);
            }
        };

        addButton(new GUISlot(guiLeft + 8, guiTop + 19, 34,36,0 , slotclick));
        addButton(new GUISlot(guiLeft + 8 + 36, guiTop + 19, 34,36,1 , slotclick));
        addButton(new GUISlot(guiLeft + 8 + 72, guiTop + 19, 34,36,2 , slotclick));
        addButton(new GUISlot(guiLeft + 8 + 108, guiTop + 19, 34,36,3 , slotclick));
        addButton(new GUISlot(guiLeft + 8, guiTop + 19 + 38, 34,36,4 , slotclick));
        addButton(new GUISlot(guiLeft + 8 + 36, guiTop + 19 + 38, 34,36,5 , slotclick));
        addButton(new GUISlot(guiLeft + 8 + 72, guiTop + 19 + 38, 34,36,6 , slotclick));
        addButton(new GUISlot(guiLeft + 8 + 108, guiTop + 19 + 38, 34,36,7 , slotclick));
        addButton(new ScrollButton(guiLeft+ 152,guiTop + 18, 16,37, true, (A) ->  scroll = scroll <= 0?0:scroll - 4 ));
        addButton(new ScrollButton(guiLeft + 152,guiTop + 55, 16,37, false, (A) ->  scroll = scroll >= container.handler.getSlots()-8?container.handler.getSlots()-8:scroll + 4 ));
    }

    private ResourceLocation GUI = new ResourceLocation(PocketStorage.MODID, "textures/gui/psugui.png");
    private int scroll = 0;

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        if (p_mouseScrolled_5_ < 0)
            scroll = MathHelper.clamp(scroll + 4, 0, container.handler.getSlots() -8);
        if (p_mouseScrolled_5_ > 0)
            scroll = MathHelper.clamp(scroll - 4, 0, container.handler.getSlots() -8);
        return false;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f ,1.0f);
        this.getMinecraft().textureManager.bindTexture(GUI);
        drawTexturedQuad(guiLeft, guiTop, xSize, ySize, 0, 0, 1, 1, 0);
    }

    private void drawTexturedQuad(int x, int y, int width, int height, float tx, float ty, float tw, float th, float z) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();

        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.vertex((double)x + 0, (double) y + height, (double) z).texture(tx,ty + th).endVertex();
        buffer.vertex((double) x + width,(double) y + height, (double) z).texture(tx + tw,ty + th).endVertex();
        buffer.vertex((double) x + width, (double) y + 0, (double) z).texture(tx + tw,ty).endVertex();
        buffer.vertex((double) x + 0, (double) y + 0, (double) z).texture(tx,ty).endVertex();

        tess.draw();
    }


    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        RenderSystem.pushMatrix();
        RenderSystem.color4f(0.25f, 0.25f, 0.25f, 1.0f);
        Minecraft.getInstance().fontRenderer.drawString(this.title.getString(), 7,6,0x404040);
        RenderSystem.color4f(1f, 1f, 1f, 1.0f);
        RenderSystem.popMatrix();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        for (IGuiEventListener listener : children) {
            if (listener instanceof GUISlot)
                ((IRenderable) listener).render(mouseX, mouseY, partialTicks);
        }
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void renderHoveredToolTip(int x, int y) {
        for (IGuiEventListener listener : children) {
            if (listener instanceof GUISlot)
                ((GUISlot) listener).renderToolTip(x, y);
        }
        super.renderHoveredToolTip(x, y);
    }

    class ScrollButton extends Button {
        public ScrollButton (int x, int y, int width, int height, boolean upIn, Button.IPressable pressable) {
            super(x,y,width,height,"",pressable);
            up = upIn;
        }
        boolean up;
        private ResourceLocation TEX = new ResourceLocation(PocketStorage.MODID, "textures/gui/buttons.png");


        @Override
        public void renderButton(int mouseX, int mouseY, float partialTicks) {
            minecraft.getTextureManager().bindTexture(TEX);
            if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height)
                drawTexturedQuad(x,y,width,height, 0.5f,up?0:.5f,.5f,.5f, 100F);
            else
                drawTexturedQuad(x,y,width,height, 0.0f, up?0:.5f,.5f,.5f, 100F);
        }
    }


    class GUISlot extends Button {
        public GUISlot(int x, int y, int width, int height,int slotIn, Button.IPressable pressable) {
            super(x,y,width,height,"", pressable);
            this.slot = slotIn;
        }
        public int slot;

        @Override
        public void renderToolTip(int mx, int my) {
            if (mx >= x && mx < x + width && my >= y && my < y + height ) {
                ItemStack stack = container.handler.getStackInSlot(slot + scroll);
                if(stack != null && !stack.isEmpty()) {
                    net.minecraftforge.fml.client.gui.GuiUtils.preItemToolTip(stack);
                    List<String> tooltip = getTooltipFromItem(stack);
                    tooltip.add(I18n.format("pocketstorage.count",stack.getCount()));
                    renderTooltip(tooltip, mx, my, Minecraft.getInstance().fontRenderer);
                    net.minecraftforge.fml.client.gui.GuiUtils.postItemToolTip();
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
        public void renderButton(int mouseX, int mouseY, float partialTicks) {
            RenderSystem.pushMatrix();
            RenderSystem.color3f(1.0f,1.0f,1.0f);
            FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;

            boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.field_225655_p_, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.field_225654_o_, GlStateManager.SourceFactor.ONE.field_225655_p_, GlStateManager.DestFactor.ZERO.field_225654_o_);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA.field_225655_p_, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.field_225654_o_);

            if (hovered)
                fill(x, y-1, x + width, y + height, -2130706433);

            if (container.handler != null) {
                ItemStack tmp = container.handler.getStackInSlot(slot + scroll);
                if (tmp != null) {
                    itemRenderer.zLevel = 100F;
                    RenderSystem.enableDepthTest();
                    RenderHelper.enableGuiDepthLighting();
                    itemRenderer.renderItemAndEffectIntoGUI(tmp, x + 9, y + 4);
                    if (tmp.getCount() > 0) {
                        String count = Integer.toString(tmp.getCount());
                        int strwidth = fontRenderer.getStringWidth(count);

                        fontRenderer.drawString(formatAmount(tmp.getCount()), x + 1 + (width / 2.0f) - (strwidth / 2.0f), y + 22, 0x000000);
                    } else
                        fontRenderer.drawString(I18n.format("pocketstorage.empty"), x + 1 + (width / 2.0f) - (fontRenderer.getStringWidth(I18n.format("pocketstorage.empty")) / 2.0f), y + 20, 0x000000);
                    itemRenderer.zLevel = 0F;

                }
            }

            RenderSystem.popMatrix();
        }
    }
}
