package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GoblinEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.ThreadNames;







import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GalathScreen extends GuiScreen {
   static final float SIZE_100 = 100.0F;
   static final float OFFSET_15 = 15.0F;
   static final float OFFSET_5 = 5.0F;
   static final float SCALE_0_5 = 0.5F;
   static final float SCALE_B = 0.5F;
   static final ResourceLocation GUI_TEXTURE = new ResourceLocation("sexmod", "textures/gui/command.png");
   float animProgress = 0.0F;
   float animLeft = 0.0F;
   float animRight = 0.0F;
   float animTop = 0.0F;
   float animBottom = 0.0F;
   BaseGirlEntity targetEntity;
   boolean isGoblinTarget = false;

   public GalathScreen(BaseGirlEntity var1) {
      this.targetEntity = var1;
      this.isGoblinTarget = var1 instanceof GoblinEntity;
   }

   public void onGuiClosed() {
      super.onGuiClosed();
      if (this.animTop != 0.0F || this.animBottom != 0.0F || this.animLeft != 0.0F) {
         if (this.animLeft > 0.0F) {
            this.c_clash396();
         } else if (this.isGoblinTarget) {
            if (this.animTop > this.animBottom) {
               this.a_clash394();
            } else {
               this.b_clash395();
            }
         }
      }
   }

   void a_clash394() {
      if (this.isGoblinTarget) {
         ((GoblinEntity)this.targetEntity).c_clash239(Minecraft.getMinecraft().player.getPersistentID());
      }
   }

   void b_clash395() {
      ((GoblinEntity)this.targetEntity).b_clash240(Minecraft.getMinecraft().player.getPersistentID());
   }

   void c_clash396() {
      if (this.targetEntity.getInteractionPlayerUUID() == null) {
         this.targetEntity.setCurrentAction(Action.START_THROWING);
      }
   }

   public void handleKeyboardInput() {
      if (ClientProxy.keyBindings[0].getKeyCode() == Keyboard.getEventKey() && !Keyboard.getEventKeyState()) {
         Minecraft.getMinecraft().player.closeScreen();
      } else {
         super.handleKeyboardInput();
      }
   }

   public void drawScreen(int var1, int var2, float var3) {
      super.drawScreen(var1, var2, var3);
      GL11.glEnable(3042);
      OpenGlHelper.glBlendFunc(770, 771, 1, 0);
      GL11.glBlendFunc(770, 771);

      try {
         this.animProgress = Math.min(1.0F, this.animProgress + this.mc.getTickLength() / 5.0F);
      } catch (NullPointerException var6) {
      }

      float var4 = (float)this.a_clash397(this.animProgress);
      float var5 = (1.0F - var4) * 100.0F;
      this.animLeft = this.animLeft + (var1 < this.width / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animRight = this.animRight + (var1 > this.width / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animTop = this.animTop + (var2 < this.height / 2 - 1 ? 1 : -1) * this.mc.getTickLength();
      this.animBottom = this.animBottom + (var2 > this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animLeft = ThreadNames.b(this.animLeft, 0.0F, 1.0F);
      this.animRight = ThreadNames.b(this.animRight, 0.0F, 1.0F);
      this.animTop = ThreadNames.b(this.animTop, 0.0F, 1.0F);
      this.animBottom = ThreadNames.b(this.animBottom, 0.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
      GlStateManager.scale(var4, var4, var4);
      this.mc.renderEngine.bindTexture(GUI_TEXTURE);
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F + this.animLeft * 0.5F, 1.0F + this.animLeft * 0.5F, 1.0F);
      this.drawTexturedModalRect(-62.0F + var5 - this.animLeft * 15.0F, var5 - 32.0F, 0, 0, 64, 64);
      this.drawTexturedModalRect(-62.0F + var5 - this.animLeft * 15.0F, var5 - 32.0F, 64, 128, 64, 64);
      GlStateManager.popMatrix();
      if (!this.isGoblinTarget) {
         GlStateManager.popMatrix();
         GL11.glDisable(3042);
      } else {
         GlStateManager.pushMatrix();
         GlStateManager.scale(1.0F - this.animRight, 1.0F - this.animRight, 1.0F);
         this.drawTexturedModalRect(-2.0F - var5 + this.animRight * 32.0F, -var5 - 32.0F, 0, 0, 64, 64);
         this.drawTexturedModalRect(-2.0F - var5 + this.animRight * 32.0F, -var5 - 32.0F, 0, 128, 64, 64);
         GlStateManager.popMatrix();
         if (this.animRight > 0.0F) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(-1.0F + this.animRight + 1.0F + this.animTop * 0.5F, -1.0F + this.animRight + 1.0F + this.animTop * 0.5F, 1.0F);
            this.drawTexturedModalRect(-2.0F - var5 + this.animTop * 5.0F, -var5 - 64.0F - this.animTop * 5.0F / 2.0F, 0, 0, 64, 64);
            this.drawTexturedModalRect(-2.0F - var5 + this.animTop * 5.0F, -var5 - 64.0F - this.animTop * 5.0F / 2.0F, 128, 128, 64, 64);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(-1.0F + this.animRight + 1.0F + this.animBottom * 0.5F, -1.0F + this.animRight + 1.0F + this.animBottom * 0.5F, 1.0F);
            this.drawTexturedModalRect(-2.0F - var5 + this.animBottom * 5.0F, -var5 + this.animBottom * 5.0F / 2.0F, 0, 0, 64, 64);
            this.drawTexturedModalRect(-2.0F - var5 + this.animBottom * 5.0F, -var5 + this.animBottom * 5.0F / 2.0F, 192, 128, 64, 64);
            GlStateManager.popMatrix();
         }

         GlStateManager.popMatrix();
         GL11.glDisable(3042);
      }
   }

   double a_clash397(double var1) {
      double var3 = 1.70158;
      double var5 = 2.70158;
      return 1.0 + var5 * Math.pow(var1 - 1.0, 3.0) + var3 * Math.pow(var1 - 1.0, 2.0);
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

}
