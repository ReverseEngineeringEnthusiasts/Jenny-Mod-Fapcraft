package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;







import java.io.PrintWriter;
import java.io.StringWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class InHandMapRenderer {
   Minecraft mc;
   float PROGRESS_SCALE = 2.0F;
   boolean isRendering = false;
   private static final ResourceLocation MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
   IVanillaModel handModel;
   ResourceLocation handTexture;
   Vec3i handColor;
   float ANIM_OFFSET = 0.0F;

   @SubscribeEvent
   public void a(RenderSpecificHandEvent var1) {
      AbstractPlayerGirlEntity.rebuildPlayerGirlTable();
      AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(Minecraft.getMinecraft().player.getPersistentID());
      if (var2 != null) {
         int var3 = var2.getOutfitIndex();
         this.handModel = var2.getHandModel(var3);
         this.handTexture = new ResourceLocation("sexmod", var2.getHandTexture(var3));
         this.handColor = var2.getHandColor(var3);
         if (this.handModel == null) {
            System.out.println("HAND IS NULL uwu did you forget to assign this girl a hand owo?");
         } else {
            this.mc = Minecraft.getMinecraft();
            float var4 = 0.0F;
            float var5 = 0.0F;

            try {
               ItemRenderer var6 = this.mc.getItemRenderer();
               if (DebugMode.isDeobfuscated()) {
                  var4 = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, var6, "prevEquippedProgressMainHand");
                  var5 = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, var6, "equippedProgressMainHand");
               } else {
                  var4 = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, var6, "prevEquippedProgressMainHand");
                  var5 = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, var6, "equippedProgressMainHand");
               }

               this.PROGRESS_SCALE = 2.0F - (var4 + (var5 - var4) * var1.getPartialTicks());
            } catch (Exception var9) {
               System.out.println("couldnt do the reflection thingy");
               StringWriter var7 = new StringWriter();
               var9.printStackTrace(new PrintWriter(var7));
               Minecraft.getMinecraft().player.sendChatMessage(var7.toString());
            }

            EntityPlayerSP var10 = this.mc.player;
            float var11 = var10.getSwingProgress(var1.getPartialTicks());
            ItemStack var8 = this.mc.player.getHeldItemMainhand();
            GlStateManager.color(this.handColor.getX() / 255.0F, this.handColor.getY() / 255.0F, this.handColor.getZ() / 255.0F);
            if (var1.getHand() == EnumHand.MAIN_HAND) {
               if (var8.isEmpty() || var8.getItem() instanceof ItemMap) {
                  var1.setCanceled(true);
                  this.a(var8, var1.getPartialTicks(), var10, this.PROGRESS_SCALE, var11);
                  this.isRendering = true;
               } else if (var5 < var4) {
                  if (this.isRendering) {
                     var1.setCanceled(true);
                     this.a(var8, var1.getPartialTicks(), var10, this.PROGRESS_SCALE, var11);
                  }
               } else {
                  this.isRendering = false;
               }
            } else if (this.mc.player.getHeldItemOffhand().getItem() instanceof ItemMap) {
               var1.setCanceled(true);
               this.a(EnumHandSide.LEFT, this.PROGRESS_SCALE - 1.0F, var11, this.mc.player.getHeldItemOffhand());
            }

            GlStateManager.resetColor();
         }
      }
   }

   void a(ItemStack var1, float var2, AbstractClientPlayer var3, float var4, float var5) {
      if (var1.getItem() instanceof ItemMap) {
         if (var3.getHeldItemOffhand().isEmpty()) {
            this.a(var1, var3, var5, var2);
         } else {
            this.a(EnumHandSide.RIGHT, var4 - 1.0F, var5, var1);
         }
      } else {
         this.renderMapView(var5, var2);
      }
   }

   void a(EnumHandSide var1, float var2, float var3, ItemStack var4) {
      float var5 = var1 == EnumHandSide.RIGHT ? 1.0F : -1.0F;
      GlStateManager.translate(var5 * 0.125F, -0.125F, 0.0F);
      if (!this.mc.player.isInvisible()) {
         GlStateManager.pushMatrix();
         GlStateManager.rotate(var5 * 10.0F, 0.0F, 0.0F, 1.0F);
         this.a(var2, var3, var1);
         GlStateManager.translate(-0.5F, -1.1F, 0.0F);
         if (var1 == EnumHandSide.RIGHT) {
            GlStateManager.translate(0.48F, 0.15F, 0.0F);
         } else {
            GlStateManager.translate(0.44F, 1.3F, 1.0F);
         }

         Minecraft.getMinecraft().getTextureManager().bindTexture(this.handTexture);
         this.handModel.getModel().render(0.175F);
         GlStateManager.popMatrix();
      }

      GlStateManager.pushMatrix();
      GlStateManager.translate(var5 * 0.51F, -0.08F + var2 * -1.2F, -0.75F);
      float var6 = MathHelper.sqrt(var3);
      float var7 = MathHelper.sin(var6 * (float) Math.PI);
      float var8 = -0.5F * var7;
      float var9 = 0.4F * MathHelper.sin(var6 * (float) (Math.PI * 2));
      float var10 = -0.3F * MathHelper.sin(var3 * (float) Math.PI);
      GlStateManager.translate(var5 * var8, var9 - 0.3F * var7, var10);
      GlStateManager.rotate(var7 * -45.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(var5 * var7 * -30.0F, 0.0F, 1.0F, 0.0F);
      this.renderMapItem(var4);
      GlStateManager.popMatrix();
   }

   void a(ItemStack var1, AbstractClientPlayer var2, float var3, float var4) {
      float var5 = var2.prevRotationPitch + (var2.rotationPitch - var2.prevRotationPitch) * var4;
      float var6 = MathHelper.sqrt(var3);
      float var7 = -0.2F * MathHelper.sin(var3 * (float) Math.PI);
      float var8 = -0.4F * MathHelper.sin(var6 * (float) Math.PI);
      GlStateManager.translate(0.0F, -var7 / 2.0F, var8);
      float var9 = this.calculateMapScale(var5);
      GlStateManager.translate(0.0F, 0.04F + (this.PROGRESS_SCALE - 1.0F) * -1.2F + var9 * -0.5F, -0.72F);
      GlStateManager.rotate(var9 * -85.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.disableCull();
      GlStateManager.pushMatrix();
      GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
      this.a(EnumHandSide.RIGHT);
      this.a(EnumHandSide.LEFT);
      GlStateManager.popMatrix();
      GlStateManager.enableCull();
      float var10 = MathHelper.sin(var6 * (float) Math.PI);
      GlStateManager.rotate(var10 * 20.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(2.0F, 2.0F, 2.0F);
      this.renderMapItem(var1);
      GlStateManager.enableLighting();
   }

   void renderMapItem(ItemStack var1) {
      GlStateManager.resetColor();
      GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.scale(0.38F, 0.38F, 0.38F);
      GlStateManager.disableLighting();
      this.mc.getTextureManager().bindTexture(MAP_BACKGROUND);
      Tessellator var2 = Tessellator.getInstance();
      BufferBuilder var3 = var2.getBuffer();
      GlStateManager.translate(-0.5F, -0.5F, 0.0F);
      GlStateManager.scale(0.0078125F, 0.0078125F, 0.0078125F);
      var3.begin(7, DefaultVertexFormats.POSITION_TEX);
      var3.pos(-7.0, 135.0, 0.0).tex(0.0, 1.0).endVertex();
      var3.pos(135.0, 135.0, 0.0).tex(1.0, 1.0).endVertex();
      var3.pos(135.0, -7.0, 0.0).tex(1.0, 0.0).endVertex();
      var3.pos(-7.0, -7.0, 0.0).tex(0.0, 0.0).endVertex();
      var2.draw();
      MapData var4 = ((ItemMap)var1.getItem()).getMapData(var1, this.mc.world);
      if (var4 != null) {
         this.mc.entityRenderer.getMapItemRenderer().renderMap(var4, false);
      }

      GlStateManager.color(this.handColor.getX() / 255.0F, this.handColor.getY() / 255.0F, this.handColor.getZ() / 255.0F);
   }

   private void a(EnumHandSide var1) {
      GlStateManager.pushMatrix();
      float var2 = var1 == EnumHandSide.RIGHT ? 1.0F : -1.0F;
      GlStateManager.rotate(92.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(var2 * -41.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.translate(var2 * 0.3F, -1.1F, 0.45F);
      if (var1 == EnumHandSide.RIGHT) {
         GlStateManager.translate(0.63F, 0.36F, 0.0F);
      } else {
         GlStateManager.translate(1.6F, 0.35F, 0.0F);
      }

      Minecraft.getMinecraft().getTextureManager().bindTexture(this.handTexture);
      this.handModel.getModel().render(0.175F);
      GlStateManager.popMatrix();
   }

   private float calculateMapScale(float var1) {
      float var2 = 1.0F - var1 / 45.0F + 0.1F;
      var2 = MathHelper.clamp(var2, 0.0F, 1.0F);
      return -MathHelper.cos(var2 * (float) Math.PI) * 0.5F + 0.5F;
   }

   void renderMapView(float var1, float var2) {
      GlStateManager.disableCull();
      GlStateManager.pushMatrix();
      this.a(this.PROGRESS_SCALE, var1, EnumHandSide.RIGHT);
      Minecraft.getMinecraft().getTextureManager().bindTexture(this.handTexture);
      this.handModel.getModel().render(0.175F);
      GlStateManager.disableBlend();
      GlStateManager.enableCull();
      GlStateManager.popMatrix();
   }

   private void a(float var1, float var2, EnumHandSide var3) {
      boolean var4 = var3 != EnumHandSide.LEFT;
      float var5 = var4 ? 1.0F : -1.0F;
      float var6 = MathHelper.sqrt(var2);
      float var7 = -0.3F * MathHelper.sin(var6 * (float) Math.PI);
      float var8 = 0.4F * MathHelper.sin(var6 * (float) (Math.PI * 2));
      float var9 = -0.4F * MathHelper.sin(var2 * (float) Math.PI);
      GlStateManager.translate(var5 * (var7 + 0.64000005F), var8 + -0.6F + var1 * -0.6F, var9 + -0.71999997F);
      GlStateManager.rotate(var5 * 45.0F, 0.0F, 1.0F, 0.0F);
      float var10 = MathHelper.sin(var2 * var2 * (float) Math.PI);
      float var11 = MathHelper.sin(var6 * (float) Math.PI);
      GlStateManager.rotate(var5 * var11 * 70.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(var5 * var10 * -20.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.translate(var5 * -1.0F, 3.6F, 3.5F);
      GlStateManager.rotate(var5 * 120.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(var5 * -135.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(var5 * 5.6F, 0.0F, 0.0F);
      GlStateManager.translate(0.5F, 1.1F, 0.0F);
   }

}
