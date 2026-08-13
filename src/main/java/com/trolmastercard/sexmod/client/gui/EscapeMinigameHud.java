package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.networking.GalathBackOffRapePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.EscapeDirectionKey;







import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EscapeMinigameHud extends Gui {
   static final ResourceLocation HUD_TEXTURE = new ResourceLocation("sexmod", "textures/gui/escape_minigame_ui.png");
   static final int KEY_SIZE = 52;
   static final float TEXTURE_SCALE = 20.0F;
   static final int KEY_SPACING = 35;
   static final float KEY_PRESS_SPEED = 0.08F;
   static final float KEY_DECAY = 0.006F;
   static final int TIMER_MODULUS = 2;
   static final float PROGRESS_THRESHOLD = 0.33F;
   static boolean showHud = false;
   static EscapeDirectionKey currentKey = null;
   static float progress = 0.0F;
   static float timer = 0.0F;
   static boolean blinkState = true;
   static float failTimer = 0.0F;
   static boolean failed = false;
   static Minecraft mc = Minecraft.getMinecraft();
   static boolean hasStarted = false;

   public static void e_clash735() {
      if (showHud) {
         if (mc.world == null) {
            showHud = false;
            hasStarted = false;
            timer = 0.0F;
            progress = 0.0F;
            failTimer = 0.0F;
            failed = false;
         }

         if (failed) {
            blinkState = false;
            failTimer++;
            if (failTimer >= 20.0F) {
               showHud = false;
            }
         } else {
            timer++;
            if (timer % Math.max(1, 2) == 0.0F) {
               blinkState = !blinkState;
            }

            progress = Math.max(0.0F, progress - 0.006F);
            if (!(timer < 20.0F)) {
               if (timer % 35.0F == 0.0F || currentKey == null) {
                  b_clash736();
               }
            }
         }
      }
   }

   static void b_clash736() {
      EscapeDirectionKey var0 = currentKey;
      Random var1 = new Random();

      do {
         currentKey = EscapeDirectionKey.values()[var1.nextInt(EscapeDirectionKey.values().length)];
      } while (var0 == currentKey);
   }

   static void c_clash737() {
      if (showHud) {
         if (!hasStarted) {
            hasStarted = true;
            PacketHandler.networkWrapper.sendToServer(new GalathBackOffRapePacket());
            d_clash739();
         }
      }
   }

   public static void a_clash738() {
      showHud = true;
      hasStarted = false;
      timer = 0.0F;
      progress = 0.0F;
      failTimer = 0.0F;
      failed = false;
   }

   public static void d_clash739() {
      failed = true;
      failTimer = 0.0F;
   }

   @SubscribeEvent
   public void a(RenderGameOverlayEvent var1) {
      if (showHud) {
         if (var1.getType() == ElementType.TEXT) {
            int var2 = var1.getResolution().getScaledWidth();
            int var3 = var1.getResolution().getScaledHeight();
            float var4 = var1.getPartialTicks();
            mc.getTextureManager().bindTexture(HUD_TEXTURE);
            double var5;
            if (failed) {
               var5 = 1.0 - RotationHelper.d((failTimer + var4) / 20.0F);
            } else {
               var5 = Math.min(1.0, RotationHelper.c_clash26((timer + var4) / 20.0F));
            }

            int var7 = var3 + 385;
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.33F, 0.33F, 0.33F);
            GlStateManager.translate(485.0F, 0.0F, 0.0F);
            int var8 = 4 * var3;
            this.drawTexturedModalRect(var2 / 2 - 87, (int)RotationHelper.b(var8, var7, var5), 0, 104, 174, 48);
            this.drawTexturedModalRect((int)(var2 / 2.0F - 78.0F), (int)RotationHelper.b(var8, var7 - 52, var5), 52, blinkState && currentKey == EscapeDirectionKey.A ? 52 : 0, 52, 52);
            this.drawTexturedModalRect((int)(var2 / 2.0F - 26.0F), (int)RotationHelper.b(var8, var7 - 52, var5), 104, blinkState && currentKey == EscapeDirectionKey.S ? 52 : 0, 52, 52);
            this.drawTexturedModalRect((int)(var2 / 2.0F + 26.0F), (int)RotationHelper.b(var8, var7 - 52, var5), 156, blinkState && currentKey == EscapeDirectionKey.D ? 52 : 0, 52, 52);
            this.drawTexturedModalRect((int)(var2 / 2.0F - 26.0F), (int)RotationHelper.b(var8, var7 - 104, var5), 0, blinkState && currentKey == EscapeDirectionKey.W ? 52 : 0, 52, 52);
            this.drawTexturedModalRect(var2 / 2 - 87 + 8, (int)RotationHelper.b(var8 - 8, var7 + 8, var5), 8, 152, (int)(158.0F * progress), 32);
            GlStateManager.popMatrix();
         }
      }
   }

   @SubscribeEvent
   public void a(ClientTickEvent var1) {
      if (var1.phase != Phase.END) {
         e_clash735();
      }
   }

   @SubscribeEvent
   public void a(KeyInputEvent var1) {
      GameSettings var2 = Minecraft.getMinecraft().gameSettings;
      if (GameSettings.isKeyDown(var2.keyBindLeft)) {
         if (currentKey == EscapeDirectionKey.A) {
            progress += 0.08F;
         } else {
            progress -= 0.04F;
         }
      } else if (GameSettings.isKeyDown(var2.keyBindRight)) {
         if (currentKey == EscapeDirectionKey.D) {
            progress += 0.08F;
         } else {
            progress -= 0.04F;
         }
      } else if (GameSettings.isKeyDown(var2.keyBindForward)) {
         if (currentKey == EscapeDirectionKey.W) {
            progress += 0.08F;
         } else {
            progress -= 0.04F;
         }
      } else if (GameSettings.isKeyDown(var2.keyBindBack)) {
         if (currentKey == EscapeDirectionKey.S) {
            progress += 0.08F;
         } else {
            progress -= 0.04F;
         }
      } else {
         if (progress >= 1.0F) {
            c_clash737();
         }
      }
   }

}
