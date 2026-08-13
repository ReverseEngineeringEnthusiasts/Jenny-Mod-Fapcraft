package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UpdatePlayerModelPacket;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GirlScreenBase extends GuiScreen {
   List<EntityLivingBase> nearbyEntities = new ArrayList<>();
   int renderIndex = 0;
   static float progress = 0.0F;

   public GirlScreenBase(HashMap<NpcType, String> var1) {
      this.mc = Minecraft.getMinecraft();

      for (NpcType var5 : NpcType.values()) {
         if (!var5.isNpcOnly) {
            try {
               Constructor var6 = var5.npcClass.getConstructor(World.class);
               BaseGirlEntity var7 = (BaseGirlEntity)var6.newInstance(this.mc.world);
               var7.setLocallyRegistered(true);
               this.nearbyEntities.add(var7);
               String var8 = (String)var1.get(var5);
               if (var8 != null) {
                  var7.setCustomPartList(BaseGirlEntity.decodePartIdList(var8));
               }
            } catch (Exception var9) {
               var9.printStackTrace();
            }
         }
      }

      this.nearbyEntities.add(this.mc.player);
   }

   public void drawScreen(int var1, int var2, float var3) {
      super.drawScreen(var1, var2, var3);
      this.buttonList.clear();
      a(this.width / 2, this.height / 2 + 20, 30, this.nearbyEntities.get(this.renderIndex));
      this.buttonList.add(new GuiButton(1, this.width / 2 + 30, this.height / 2 - 10, 20, 20, ">"));
      this.buttonList.add(new GuiButton(2, this.width / 2 - 50, this.height / 2 - 10, 20, 20, "<"));
      this.buttonList.add(new GuiButton(0, this.width / 2 - 30, this.height / 2 + 30, 60, 20, "pick"));
   }

   protected void actionPerformed(GuiButton var1) {
      if (">".equals(var1.displayString) && ++this.renderIndex >= this.nearbyEntities.size()) {
         this.renderIndex = 0;
      }

      if ("<".equals(var1.displayString) && --this.renderIndex < 0) {
         this.renderIndex = this.nearbyEntities.size() - 1;
      }

      if (var1.id == 0) {
         PacketHandler.networkWrapper.sendToServer(new UpdatePlayerModelPacket(NpcType.getNpcType((Entity)this.nearbyEntities.get(this.renderIndex))));
         EntityPlayerSP var2 = Minecraft.getMinecraft().player;
         var2.closeScreen();
         var2.eyeHeight = var2.getDefaultEyeHeight();
         if (!var2.capabilities.allowFlying) {
            var2.capabilities.allowFlying = var2.capabilities.isCreativeMode;
         }
      }
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   public static void a(int var0, int var1, int var2, EntityLivingBase var3) {
      float var4 = var3.renderYawOffset;
      float var5 = var3.rotationYaw;
      float var6 = var3.rotationPitch;
      float var7 = var3.prevRotationYawHead;
      float var8 = var3.rotationYawHead;
      if (!(var3 instanceof EntityPlayer)) {
         var3.posX = 0.0;
         var3.posY = 0.0;
         var3.posZ = 0.0;
      }

      var3.renderYawOffset = 0.0F;
      var3.rotationYaw = 0.0F;
      var3.rotationPitch = 0.0F;
      var3.prevRotationYawHead = 0.0F;
      var3.rotationYawHead = 0.0F;
      float var9 = Minecraft.getDebugFPS();
      if (var9 == 0.0F) {
         var9 = 0.1F;
      }

      progress += 60.0F / var9;
      GlStateManager.enableColorMaterial();
      GlStateManager.pushMatrix();
      GlStateManager.translate(var0, var1, 50.0F);
      GlStateManager.scale(-var2, var2, var2);
      GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
      RenderHelper.enableStandardItemLighting();
      GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(progress, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(0.0F, 0.0F, 0.0F);
      RenderManager var10 = Minecraft.getMinecraft().getRenderManager();
      var10.setPlayerViewY(180.0F);
      var10.setRenderShadow(false);
      var10.renderEntity(var3, 0.0, 0.0, 0.0, 0.0F, 1.2345679F, false);
      var10.setRenderShadow(true);
      GlStateManager.popMatrix();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableRescaleNormal();
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.disableTexture2D();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      var3.renderYawOffset = var4;
      var3.rotationYaw = var5;
      var3.rotationPitch = var6;
      var3.prevRotationYawHead = var7;
      var3.rotationYawHead = var8;
   }

}
