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

/**
 * Direction-pad control screen for Galath (and goblin throw/pickup): the
 * player holds the mouse in one of four quadrants around the screen center and
 * closes the screen (release key or Escape) to commit the action.
 * <p>
 * <b>Action decision.</b> While open, per-frame mouse position accumulates
 * quadrant indicators ({@code animLeft/Right/Top/Bottom}, clamped to 0..1).
 * {@link #onGuiClosed()} then picks: left = start throwing
 * ({@code Action.START_THROWING}), top = throw the goblin at the player,
 * bottom = pick the goblin up. For non-goblin targets only the left action is
 * meaningful.
 * <p>
 * CLIENT-side only. Closing the screen is what commits the action — if a
 * future editor makes closing conditional, the action must still be dispatched
 * exactly once.
 */
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

   public GalathScreen(BaseGirlEntity target) {
      this.targetEntity = target;
      this.isGoblinTarget = target instanceof GoblinEntity;
   }

   /**
    * Commits the chosen action from the accumulated quadrant indicators (see
    * class javadoc). Must fire exactly once per screen lifetime.
    */
   public void onGuiClosed() {
      super.onGuiClosed();
      if (this.animTop != 0.0F || this.animBottom != 0.0F || this.animLeft != 0.0F) {
         if (this.animLeft > 0.0F) {
            this.startGoblinThrow();
         } else if (this.isGoblinTarget) {
            if (this.animTop > this.animBottom) {
               this.throwGoblin();
            } else {
               this.pickupGoblin();
            }
         }
      }
   }

   /**
    * Marks the goblin to be thrown at the local player (entity-side flag set
    * via {@code setThrowTarget}).
    */
   void throwGoblin() {
      if (this.isGoblinTarget) {
         ((GoblinEntity)this.targetEntity).setThrowTarget(Minecraft.getMinecraft().player.getPersistentID());
      }
   }

   /**
    * Marks the goblin to be picked up by the local player
    * ({@code setPickupTarget}).
    */
   void pickupGoblin() {
      ((GoblinEntity)this.targetEntity).setPickupTarget(Minecraft.getMinecraft().player.getPersistentID());
   }

   /**
    * Starts the throw sequence ({@code Action.START_THROWING}) — only valid
    * while the target girl is not already interacting with another player.
    */
   void startGoblinThrow() {
      if (this.targetEntity.getInteractionPlayerUUID() == null) {
         this.targetEntity.setCurrentAction(Action.START_THROWING);
      }
   }

   /**
    * Closes the screen when the pad toggle key (ClientProxy key binding 0) is
    * released; everything else is handled by the parent.
    */
   public void handleKeyboardInput() {
      if (ClientProxy.keyBindings[0].getKeyCode() == Keyboard.getEventKey() && !Keyboard.getEventKeyState()) {
         Minecraft.getMinecraft().player.closeScreen();
      } else {
         super.handleKeyboardInput();
      }
   }

   /**
    * Draws the animated control pad: four directional buttons scale/translate
    * toward the mouse position with an ease-out-back entrance, plus a
    * goblin-specific throw/pickup layout. Mouse-side accumulation
    * (animLeft/Right/Top/Bottom) happens here, which {@link #onGuiClosed()}
    * reads when the screen closes.
    */
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      super.drawScreen(mouseX, mouseY, partialTicks);
      GL11.glEnable(3042);
      OpenGlHelper.glBlendFunc(770, 771, 1, 0);
      GL11.glBlendFunc(770, 771);

      try {
         this.animProgress = Math.min(1.0F, this.animProgress + this.mc.getTickLength() / 5.0F);
      } catch (NullPointerException npe) {
      }

      float scale = (float)this.easeOutBack(this.animProgress);
      float offset = (1.0F - scale) * 100.0F;
      this.animLeft = this.animLeft + (mouseX < this.width / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animRight = this.animRight + (mouseX > this.width / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animTop = this.animTop + (mouseY < this.height / 2 - 1 ? 1 : -1) * this.mc.getTickLength();
      this.animBottom = this.animBottom + (mouseY > this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animLeft = ThreadNames.clampFloat(this.animLeft, 0.0F, 1.0F);
      this.animRight = ThreadNames.clampFloat(this.animRight, 0.0F, 1.0F);
      this.animTop = ThreadNames.clampFloat(this.animTop, 0.0F, 1.0F);
      this.animBottom = ThreadNames.clampFloat(this.animBottom, 0.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
      GlStateManager.scale(scale, scale, scale);
      this.mc.renderEngine.bindTexture(GUI_TEXTURE);
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F + this.animLeft * 0.5F, 1.0F + this.animLeft * 0.5F, 1.0F);
      this.drawTexturedModalRect(-62.0F + offset - this.animLeft * 15.0F, offset - 32.0F, 0, 0, 64, 64);
      this.drawTexturedModalRect(-62.0F + offset - this.animLeft * 15.0F, offset - 32.0F, 64, 128, 64, 64);
      GlStateManager.popMatrix();
      if (!this.isGoblinTarget) {
         GlStateManager.popMatrix();
         GL11.glDisable(3042);
      } else {
         GlStateManager.pushMatrix();
         GlStateManager.scale(1.0F - this.animRight, 1.0F - this.animRight, 1.0F);
         this.drawTexturedModalRect(-2.0F - offset + this.animRight * 32.0F, -offset - 32.0F, 0, 0, 64, 64);
         this.drawTexturedModalRect(-2.0F - offset + this.animRight * 32.0F, -offset - 32.0F, 0, 128, 64, 64);
         GlStateManager.popMatrix();
         if (this.animRight > 0.0F) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(-1.0F + this.animRight + 1.0F + this.animTop * 0.5F, -1.0F + this.animRight + 1.0F + this.animTop * 0.5F, 1.0F);
            this.drawTexturedModalRect(-2.0F - offset + this.animTop * 5.0F, -offset - 64.0F - this.animTop * 5.0F / 2.0F, 0, 0, 64, 64);
            this.drawTexturedModalRect(-2.0F - offset + this.animTop * 5.0F, -offset - 64.0F - this.animTop * 5.0F / 2.0F, 128, 128, 64, 64);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(-1.0F + this.animRight + 1.0F + this.animBottom * 0.5F, -1.0F + this.animRight + 1.0F + this.animBottom * 0.5F, 1.0F);
            this.drawTexturedModalRect(-2.0F - offset + this.animBottom * 5.0F, -offset + this.animBottom * 5.0F / 2.0F, 0, 0, 64, 64);
            this.drawTexturedModalRect(-2.0F - offset + this.animBottom * 5.0F, -offset + this.animBottom * 5.0F / 2.0F, 192, 128, 64, 64);
            GlStateManager.popMatrix();
         }

         GlStateManager.popMatrix();
         GL11.glDisable(3042);
      }
   }

   double easeOutBack(double t) {
      double c1 = 1.70158;
      double c3 = 2.70158;
      return 1.0 + c3 * Math.pow(t - 1.0, 3.0) + c1 * Math.pow(t - 1.0, 2.0);
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

}
