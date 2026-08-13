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
   static final float j = 100.0F;
   static final float c = 15.0F;
   static final float k = 5.0F;
   static final float l = 0.5F;
   static final float b = 0.5F;
   static final ResourceLocation i = new ResourceLocation("sexmod", "textures/gui/command.png");
   float a = 0.0F;
   float g = 0.0F;
   float e = 0.0F;
   float d = 0.0F;
   float m = 0.0F;
   BaseGirlEntity f;
   boolean h = false;

   public GalathScreen(BaseGirlEntity var1) {
      this.f = var1;
      this.h = var1 instanceof GoblinEntity;
   }

   public void onGuiClosed() {
      super.onGuiClosed();
      if (this.d != 0.0F || this.m != 0.0F || this.g != 0.0F) {
         if (this.g > 0.0F) {
            this.c_clash396();
         } else if (this.h) {
            if (this.d > this.m) {
               this.a_clash394();
            } else {
               this.b_clash395();
            }
         }
      }
   }

   void a_clash394() {
      if (this.h) {
         ((GoblinEntity)this.f).c_clash239(Minecraft.getMinecraft().player.getPersistentID());
      }
   }

   void b_clash395() {
      ((GoblinEntity)this.f).b_clash240(Minecraft.getMinecraft().player.getPersistentID());
   }

   void c_clash396() {
      if (this.f.getInteractionPlayerUUID() == null) {
         this.f.setCurrentAction(Action.START_THROWING);
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
         this.a = Math.min(1.0F, this.a + this.mc.getTickLength() / 5.0F);
      } catch (NullPointerException var6) {
      }

      float var4 = (float)this.a_clash397(this.a);
      float var5 = (1.0F - var4) * 100.0F;
      this.g = this.g + (var1 < this.width / 2 ? 1 : -1) * this.mc.getTickLength();
      this.e = this.e + (var1 > this.width / 2 ? 1 : -1) * this.mc.getTickLength();
      this.d = this.d + (var2 < this.height / 2 - 1 ? 1 : -1) * this.mc.getTickLength();
      this.m = this.m + (var2 > this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.g = ThreadNames.b(this.g, 0.0F, 1.0F);
      this.e = ThreadNames.b(this.e, 0.0F, 1.0F);
      this.d = ThreadNames.b(this.d, 0.0F, 1.0F);
      this.m = ThreadNames.b(this.m, 0.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
      GlStateManager.scale(var4, var4, var4);
      this.mc.renderEngine.bindTexture(i);
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F + this.g * 0.5F, 1.0F + this.g * 0.5F, 1.0F);
      this.drawTexturedModalRect(-62.0F + var5 - this.g * 15.0F, var5 - 32.0F, 0, 0, 64, 64);
      this.drawTexturedModalRect(-62.0F + var5 - this.g * 15.0F, var5 - 32.0F, 64, 128, 64, 64);
      GlStateManager.popMatrix();
      if (!this.h) {
         GlStateManager.popMatrix();
         GL11.glDisable(3042);
      } else {
         GlStateManager.pushMatrix();
         GlStateManager.scale(1.0F - this.e, 1.0F - this.e, 1.0F);
         this.drawTexturedModalRect(-2.0F - var5 + this.e * 32.0F, -var5 - 32.0F, 0, 0, 64, 64);
         this.drawTexturedModalRect(-2.0F - var5 + this.e * 32.0F, -var5 - 32.0F, 0, 128, 64, 64);
         GlStateManager.popMatrix();
         if (this.e > 0.0F) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(-1.0F + this.e + 1.0F + this.d * 0.5F, -1.0F + this.e + 1.0F + this.d * 0.5F, 1.0F);
            this.drawTexturedModalRect(-2.0F - var5 + this.d * 5.0F, -var5 - 64.0F - this.d * 5.0F / 2.0F, 0, 0, 64, 64);
            this.drawTexturedModalRect(-2.0F - var5 + this.d * 5.0F, -var5 - 64.0F - this.d * 5.0F / 2.0F, 128, 128, 64, 64);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(-1.0F + this.e + 1.0F + this.m * 0.5F, -1.0F + this.e + 1.0F + this.m * 0.5F, 1.0F);
            this.drawTexturedModalRect(-2.0F - var5 + this.m * 5.0F, -var5 + this.m * 5.0F / 2.0F, 0, 0, 64, 64);
            this.drawTexturedModalRect(-2.0F - var5 + this.m * 5.0F, -var5 + this.m * 5.0F / 2.0F, 192, 128, 64, 64);
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
