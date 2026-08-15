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
   public void onRenderSpecificHand(RenderSpecificHandEvent event) {
      AbstractPlayerGirlEntity.rebuildPlayerGirlTableFromWorld();
      AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(Minecraft.getMinecraft().player.getPersistentID());
      if (playerGirl != null) {
         int outfitIndex = playerGirl.getOutfitIndex();
         this.handModel = playerGirl.getHandModel(outfitIndex);
         this.handTexture = new ResourceLocation("sexmod", playerGirl.getHandTexture(outfitIndex));
         this.handColor = playerGirl.getHandColor(outfitIndex);
         if (this.handModel == null) {
            System.out.println("HAND IS NULL uwu did you forget to assign this girl a hand owo?");
         } else {
            this.mc = Minecraft.getMinecraft();
            float prevProgress = 0.0F;
            float curProgress = 0.0F;

            try {
               ItemRenderer itemRenderer = this.mc.getItemRenderer();
               if (DebugMode.isDeobfuscated()) {
                  // deobf/dev environment: MCP names
                  prevProgress = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, itemRenderer, "prevEquippedProgressMainHand");
                  curProgress = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, itemRenderer, "equippedProgressMainHand");
               } else {
                  // obfuscated runtime: SRG names (stable_39: field_187470_g = prevEquippedProgressMainHand,
                  // field_187469_f = equippedProgressMainHand). FML's remapper has no mcp->srg data at
                  // runtime, so the MCP names throw NoSuchFieldException here.
                  prevProgress = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, itemRenderer, "field_187470_g");
                  curProgress = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, itemRenderer, "field_187469_f");
               }
               SceneDebug.log(SceneDebug.IN_HAND, "InHandMapRenderer: prev=%.3f cur=%.3f", prevProgress, curProgress);

               this.PROGRESS_SCALE = 2.0F - (prevProgress + (curProgress - prevProgress) * event.getPartialTicks());
            } catch (Exception exception) {
               System.out.println("couldnt do the reflection thingy");
               StringWriter stringWriter = new StringWriter();
               exception.printStackTrace(new PrintWriter(stringWriter));
               Minecraft.getMinecraft().player.sendChatMessage(stringWriter.toString());
            }

            EntityPlayerSP player = this.mc.player;
            float swingProgress = player.getSwingProgress(event.getPartialTicks());
            ItemStack stack = this.mc.player.getHeldItemMainhand();
            GlStateManager.color(this.handColor.getX() / 255.0F, this.handColor.getY() / 255.0F, this.handColor.getZ() / 255.0F);
            if (event.getHand() == EnumHand.MAIN_HAND) {
               if (stack.isEmpty() || stack.getItem() instanceof ItemMap) {
                  event.setCanceled(true);
                  this.renderPlayerMap(stack, event.getPartialTicks(), player, this.PROGRESS_SCALE, swingProgress);
                  this.isRendering = true;
               } else if (curProgress < prevProgress) {
                  if (this.isRendering) {
                     event.setCanceled(true);
                     this.renderPlayerMap(stack, event.getPartialTicks(), player, this.PROGRESS_SCALE, swingProgress);
                  }
               } else {
                  this.isRendering = false;
               }
            } else if (this.mc.player.getHeldItemOffhand().getItem() instanceof ItemMap) {
               event.setCanceled(true);
               this.renderHandMap(EnumHandSide.LEFT, this.PROGRESS_SCALE - 1.0F, swingProgress, this.mc.player.getHeldItemOffhand());
            }

            GlStateManager.resetColor();
         }
      }
   }

   void renderPlayerMap(ItemStack stack, float partialTicks, AbstractClientPlayer player, float progressScale, float swingProgress) {
      if (stack.getItem() instanceof ItemMap) {
         if (player.getHeldItemOffhand().isEmpty()) {
            this.renderPlayerMap(stack, this.mc.getRenderPartialTicks(), player, swingProgress, partialTicks);
         } else {
            this.renderHandMap(EnumHandSide.RIGHT, progressScale - 1.0F, swingProgress, stack);
         }
      } else {
         this.renderMapView(swingProgress, partialTicks);
      }
   }

   void renderHandMap(EnumHandSide handSide, float progressScale, float swingProgress, ItemStack stack) {
      float side = handSide == EnumHandSide.RIGHT ? 1.0F : -1.0F;
      GlStateManager.translate(side * 0.125F, -0.125F, 0.0F);
      if (!this.mc.player.isInvisible()) {
         GlStateManager.pushMatrix();
         GlStateManager.rotate(side * 10.0F, 0.0F, 0.0F, 1.0F);
         this.renderMapPlane(progressScale, swingProgress, handSide);
         GlStateManager.translate(-0.5F, -1.1F, 0.0F);
         if (handSide == EnumHandSide.RIGHT) {
            GlStateManager.translate(0.48F, 0.15F, 0.0F);
         } else {
            GlStateManager.translate(0.44F, 1.3F, 1.0F);
         }

         Minecraft.getMinecraft().getTextureManager().bindTexture(this.handTexture);
         this.handModel.getModel().render(0.175F);
         GlStateManager.popMatrix();
      }

      GlStateManager.pushMatrix();
      GlStateManager.translate(side * 0.51F, -0.08F + progressScale * -1.2F, -0.75F);
      float swing = MathHelper.sqrt(swingProgress);
      float swingSin = MathHelper.sin(swing * (float) Math.PI);
      float swingSin2 = -0.5F * swingSin;
      float swingSin3 = 0.4F * MathHelper.sin(swing * (float) (Math.PI * 2));
      float swingSqSin = -0.3F * MathHelper.sin(swingProgress * (float) Math.PI);
      GlStateManager.translate(side * swingSin2, swingSin3 - 0.3F * swingSin, swingSqSin);
      GlStateManager.rotate(swingSin * -45.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(side * swingSin * -30.0F, 0.0F, 1.0F, 0.0F);
      this.renderMapItem(stack);
      GlStateManager.popMatrix();
   }

   void renderPlayerMap(ItemStack stack, AbstractClientPlayer player, float swingProgress, float partialTicks) {
      float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
      float swing = MathHelper.sqrt(swingProgress);
      float swingSin = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
      float swingSin2 = -0.4F * MathHelper.sin(swing * (float) Math.PI);
      GlStateManager.translate(0.0F, -swingSin / 2.0F, swingSin2);
      float scale = this.calculateMapScale(pitch);
      GlStateManager.translate(0.0F, 0.04F + (this.PROGRESS_SCALE - 1.0F) * -1.2F + scale * -0.5F, -0.72F);
      GlStateManager.rotate(scale * -85.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.disableCull();
      GlStateManager.pushMatrix();
      GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
      this.renderMapHand(EnumHandSide.RIGHT);
      this.renderMapHand(EnumHandSide.LEFT);
      GlStateManager.popMatrix();
      GlStateManager.enableCull();
      float swingSin3 = MathHelper.sin(swing * (float) Math.PI);
      GlStateManager.rotate(swingSin3 * 20.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(2.0F, 2.0F, 2.0F);
      this.renderMapItem(stack);
      GlStateManager.enableLighting();
   }

   void renderMapItem(ItemStack stack) {
      GlStateManager.resetColor();
      GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.scale(0.38F, 0.38F, 0.38F);
      GlStateManager.disableLighting();
      this.mc.getTextureManager().bindTexture(MAP_BACKGROUND);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      GlStateManager.translate(-0.5F, -0.5F, 0.0F);
      GlStateManager.scale(0.0078125F, 0.0078125F, 0.0078125F);
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
      buffer.pos(-7.0, 135.0, 0.0).tex(0.0, 1.0).endVertex();
      buffer.pos(135.0, 135.0, 0.0).tex(1.0, 1.0).endVertex();
      buffer.pos(135.0, -7.0, 0.0).tex(1.0, 0.0).endVertex();
      buffer.pos(-7.0, -7.0, 0.0).tex(0.0, 0.0).endVertex();
      tessellator.draw();
      MapData mapData = ((ItemMap)stack.getItem()).getMapData(stack, this.mc.world);
      if (mapData != null) {
         this.mc.entityRenderer.getMapItemRenderer().renderMap(mapData, false);
      }

      GlStateManager.color(this.handColor.getX() / 255.0F, this.handColor.getY() / 255.0F, this.handColor.getZ() / 255.0F);
   }

   private void renderMapHand(EnumHandSide handSide) {
      GlStateManager.pushMatrix();
      float side = handSide == EnumHandSide.RIGHT ? 1.0F : -1.0F;
      GlStateManager.rotate(92.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(side * -41.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.translate(side * 0.3F, -1.1F, 0.45F);
      if (handSide == EnumHandSide.RIGHT) {
         GlStateManager.translate(0.63F, 0.36F, 0.0F);
      } else {
         GlStateManager.translate(1.6F, 0.35F, 0.0F);
      }

      Minecraft.getMinecraft().getTextureManager().bindTexture(this.handTexture);
      this.handModel.getModel().render(0.175F);
      GlStateManager.popMatrix();
   }

   private float calculateMapScale(float pitch) {
      float scale = 1.0F - pitch / 45.0F + 0.1F;
      scale = MathHelper.clamp(scale, 0.0F, 1.0F);
      return -MathHelper.cos(scale * (float) Math.PI) * 0.5F + 0.5F;
   }

   void renderMapView(float progressScale, float partialTicks) {
      GlStateManager.disableCull();
      GlStateManager.pushMatrix();
      this.renderMapPlane(this.PROGRESS_SCALE, progressScale, EnumHandSide.RIGHT);
      Minecraft.getMinecraft().getTextureManager().bindTexture(this.handTexture);
      this.handModel.getModel().render(0.175F);
      GlStateManager.disableBlend();
      GlStateManager.enableCull();
      GlStateManager.popMatrix();
   }

   private void renderMapPlane(float progressScale, float partialTicks, EnumHandSide handSide) {
      boolean isRight = handSide != EnumHandSide.LEFT;
      float side = isRight ? 1.0F : -1.0F;
      float swing = MathHelper.sqrt(partialTicks);
      float offsetX = -0.3F * MathHelper.sin(swing * (float) Math.PI);
      float offsetY = 0.4F * MathHelper.sin(swing * (float) (Math.PI * 2));
      float offsetZ = -0.4F * MathHelper.sin(partialTicks * (float) Math.PI);
      GlStateManager.translate(side * (offsetX + 0.64000005F), offsetY + -0.6F + progressScale * -0.6F, offsetZ + -0.71999997F);
      GlStateManager.rotate(side * 45.0F, 0.0F, 1.0F, 0.0F);
      float swingSq = MathHelper.sin(partialTicks * partialTicks * (float) Math.PI);
      float swingSin = MathHelper.sin(swing * (float) Math.PI);
      GlStateManager.rotate(side * swingSin * 70.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(side * swingSq * -20.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.translate(side * -1.0F, 3.6F, 3.5F);
      GlStateManager.rotate(side * 120.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(side * -135.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(side * 5.6F, 0.0F, 0.0F);
      GlStateManager.translate(0.5F, 1.1F, 0.0F);
   }

}
