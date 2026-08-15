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

/**
 * <b>Role.</b> CLIENT-side custom in-hand map rendering for transformed
 * player-girls: when the local player is a player-girl, holding a map (or
 * anything, while the item-switch animation is running) renders the map +
 * the girl's hand model in first person instead of the vanilla arm.
 * <p>
 * <b>State.</b> Per-outfit hand model/texture/color from
 * {@link AbstractPlayerGirlEntity#getHandModel(int)} etc.; {@code PROGRESS_SCALE}
 * derives from the vanilla {@link ItemRenderer} equipped-progress fields read
 * via {@link ObfuscationReflectionHelper}.
 * <p>
 * <b>CRITICAL PITFALL (SRG names).</b> The reflection branch is environment-
 * dependent: in a deobfuscated (dev) environment the MCP names
 * {@code prevEquippedProgressMainHand}/{@code equippedProgressMainHand} are
 * used; in the obfuscated runtime the SRG names {@code field_187470_g} /
 * {@code field_187469_f} MUST be used — FML's remapper has no MCP->SRG data at
 * runtime and the MCP names throw {@link NoSuchFieldException} there. Keep both
 * branches; do not unify them.
 */
/**
 * CLIENT: replaces the vanilla in-hand map render with a custom girl-hand
 * version while the player is transformed (holds a map). Renders the map
 * plane plus the girl's hand model.
 * <p>
 * <b>Pitfall (reflection fix):</b> the {@code ItemRenderer} progress fields
 * are read via {@code ObfuscationReflectionHelper}. In the obfuscated runtime
 * the MCP names throw {@code NoSuchFieldException} (FML has no mcp-&gt;srg
 * field data at runtime) — the obfuscated branch MUST pass the SRG names
 * {@code field_187470_g} (prevEquippedProgressMainHand) and
 * {@code field_187469_f} (equippedProgressMainHand). Only the dev/deobf
 * branch uses the MCP names. The remap once collapsed both branches to the
 * MCP names, spamming the chat every frame.
 */
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
   public void onRenderSpecificHand(RenderSpecificHandEvent var1) {
      AbstractPlayerGirlEntity.rebuildPlayerGirlTableFromWorld();
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
                  // deobf/dev environment: MCP names
                  var4 = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, var6, "prevEquippedProgressMainHand");
                  var5 = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, var6, "equippedProgressMainHand");
               } else {
                  // obfuscated runtime: SRG names (stable_39: field_187470_g = prevEquippedProgressMainHand,
                  // field_187469_f = equippedProgressMainHand). FML's remapper has no mcp->srg data at
                  // runtime, so the MCP names throw NoSuchFieldException here.
                  var4 = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, var6, "field_187470_g");
                  var5 = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, var6, "field_187469_f");
               }
               SceneDebug.log(SceneDebug.IN_HAND, "InHandMapRenderer: prev=%.3f cur=%.3f", var4, var5);

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
                  this.renderPlayerMap(var8, var1.getPartialTicks(), var10, this.PROGRESS_SCALE, var11);
                  this.isRendering = true;
               } else if (var5 < var4) {
                  if (this.isRendering) {
                     var1.setCanceled(true);
                     this.renderPlayerMap(var8, var1.getPartialTicks(), var10, this.PROGRESS_SCALE, var11);
                  }
               } else {
                  this.isRendering = false;
               }
            } else if (this.mc.player.getHeldItemOffhand().getItem() instanceof ItemMap) {
               var1.setCanceled(true);
               this.renderHandMap(EnumHandSide.LEFT, this.PROGRESS_SCALE - 1.0F, var11, this.mc.player.getHeldItemOffhand());
            }

            GlStateManager.resetColor();
         }
      }
   }

   void renderPlayerMap(ItemStack var1, float var2, AbstractClientPlayer var3, float var4, float var5) {
      if (var1.getItem() instanceof ItemMap) {
         if (var3.getHeldItemOffhand().isEmpty()) {
            this.renderPlayerMap(var1, this.mc.getRenderPartialTicks(), var3, var5, var2);
         } else {
            this.renderHandMap(EnumHandSide.RIGHT, var4 - 1.0F, var5, var1);
         }
      } else {
         this.renderMapView(var5, var2);
      }
   }

   void renderHandMap(EnumHandSide var1, float var2, float var3, ItemStack var4) {
      float var5 = var1 == EnumHandSide.RIGHT ? 1.0F : -1.0F;
      GlStateManager.translate(var5 * 0.125F, -0.125F, 0.0F);
      if (!this.mc.player.isInvisible()) {
         GlStateManager.pushMatrix();
         GlStateManager.rotate(var5 * 10.0F, 0.0F, 0.0F, 1.0F);
         this.renderMapPlane(var2, var3, var1);
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

   void renderPlayerMap(ItemStack var1, AbstractClientPlayer var2, float var3, float var4) {
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
      this.renderMapHand(EnumHandSide.RIGHT);
      this.renderMapHand(EnumHandSide.LEFT);
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

   private void renderMapHand(EnumHandSide var1) {
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
      this.renderMapPlane(this.PROGRESS_SCALE, var1, EnumHandSide.RIGHT);
      Minecraft.getMinecraft().getTextureManager().bindTexture(this.handTexture);
      this.handModel.getModel().render(0.175F);
      GlStateManager.disableBlend();
      GlStateManager.enableCull();
      GlStateManager.popMatrix();
   }

   private void renderMapPlane(float var1, float var2, EnumHandSide var3) {
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
