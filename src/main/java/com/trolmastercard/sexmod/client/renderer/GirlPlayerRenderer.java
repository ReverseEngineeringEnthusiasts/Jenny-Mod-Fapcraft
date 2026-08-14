package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import java.util.Objects;
import javax.vecmath.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class GirlPlayerRenderer extends GirlRenderer {
   public static boolean isFirstPerson = false;
   public ItemStack mainhandItem = ItemStack.EMPTY;
   public ItemStack offhandItem = ItemStack.EMPTY;
   public boolean isRendering = false;
   public boolean isUsingItem = false;
   protected AbstractPlayerGirlEntity playerGirl;
   protected float partialTicks;
   float bowPullProgress = 0.0F;

   public GirlPlayerRenderer(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2, 0.0);
   }

   public void doRenderShadowAndFire(Entity var1, double var2, double var4, double var6, float var8, float var9) {
   }

   boolean isGirlVisible(BaseGirlEntity var1) {
      if (var1.isLocallyRegistered()) {
         return true;
      }

      boolean var2 = isFirstPerson;
      isFirstPerson = false;
      return var2;
   }

   @Override
   public void doRenderEntity(BaseGirlEntity var1, double var2, double var4, double var6, float var8, float var9) {
      if (this.isGirlVisible(var1)) {
         AbstractPlayerGirlEntity var10 = (AbstractPlayerGirlEntity)var1;
         if (var10.getOwnerUserUUID() != null) {
            EntityPlayer var11 = Minecraft.getMinecraft().player.world.getPlayerEntityByUUID(var10.getOwnerUserUUID());
            if (var11 != null) {
               this.mainhandItem = var11.getHeldItemMainhand();
               this.offhandItem = var11.getHeldItemOffhand();
               this.isUsingItem = var10.ah;
               this.isRendering = var10.ad;
               this.playerGirl = (AbstractPlayerGirlEntity)var1;
               this.partialTicks = var9;
               var10.syncArmor(var11);
               if (this.isOwnPlayer(var11, var1)) {
                  this.renderLivingLabel(var1, var11.getName(), var2, var4 + var10.getScaleFactor(), var6, 300);
               }

               super.doRenderEntity(var1, var2, var4, var6, var8, var9);
            }
         }
      }
   }

   @Override
   public Entity getRenderEntity(BaseGirlEntity var1) {
      if (!(var1 instanceof AbstractPlayerGirlEntity)) {
         return var1;
      }

      AbstractPlayerGirlEntity var2 = (AbstractPlayerGirlEntity)var1;
      EntityPlayer var3 = var2.getOwnerPlayer();
      return (Entity)(var3 == null ? var1 : var3);
   }

   boolean isOwnPlayer(EntityPlayer var1, BaseGirlEntity var2) {
      if (var1.getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID())) {
         return false;
      }

      Action var3 = var2.getCurrentAction();
      return var3 == null ? true : !var3.hideNameTag;
   }

   protected void onBoneRenderStart(String var1, GeoBone var2) {
   }

   protected void onBoneRenderingLayer(String var1, GeoBone var2, AbstractPlayerGirlEntity var3, BufferBuilder var4) {
   }

   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      String var7 = var2.getName();
      if (this.isRendering) {
         if (var7.equals("upperBody")) {
            var2.setRotationX(var2.getRotationX() - 0.5F);
         }

         if (var7.equals("head")) {
            var2.setRotationX(var2.getRotationX() + 0.5F);
         }
      }

      if (var7.equals("head")) {
         this.renderOverlay(var1, var2, Color.ofRGB(var3, var4, var5));
      }

      this.onBoneRenderStart(var7, var2);
      this.onBoneRenderingLayer(var7, var2, this.playerGirl, var1);
      if (this.isUsingItem && (this.mainhandItem.getItem() instanceof ItemBow || this.offhandItem.getItem() instanceof ItemBow)) {
         if (var7.equals("armR")) {
            var2.setRotationX(var2.getRotationX() - this.playerGirl.rotationPitch / 50.0F);
         }

         if (var7.equals("armL")) {
            var2.setRotationY(var2.getRotationY() - this.playerGirl.rotationPitch / 50.0F);
         }

         if (this.offhandItem.getItem() instanceof ItemBow) {
            ItemStack var8 = this.offhandItem;
            this.offhandItem = this.mainhandItem;
            this.mainhandItem = var8;
         }
      }

      if (this.isUsingItem && this.mainhandItem.getItem() instanceof ItemShield) {
         if (var7.equals("armR")) {
            var2.setRotationZ(0.0F);
            var2.setRotationX(0.5F);
         } else if (this.offhandItem.getItem() instanceof ItemShield && var7.equals("armL")) {
            var2.setRotationZ(0.0F);
            var2.setRotationX(0.5F);
         }
      }

      if (var7.equals("weapon") && !this.mainhandItem.isEmpty()) {
         this.renderEquippedItem(var1, var2, false);
      }

      if (var7.equals("offhand") && !this.offhandItem.isEmpty()) {
         this.renderEquippedItem(var1, var2, true);
      }

      MATRIX_STACK.push();
      MATRIX_STACK.translate(var2);
      MATRIX_STACK.moveToPivot(var2);
      MATRIX_STACK.rotate(var2);
      MATRIX_STACK.scale(var2);
      MATRIX_STACK.moveBackFromPivot(var2);
      if ("Head2".equals(var7) && !this.shouldRenderHead2()) {
         MATRIX_STACK.pop();
      } else if (("neck".equals(var7) || "head".equals(var7)) && !this.shouldRenderFirstPersonHead()) {
         MATRIX_STACK.pop();
      } else {
         if (!var2.isHidden) {
            Vector4f var17 = this.calculateBoneArmorColor(var7, var3, var4, var5);
            var3 = var17.x;
            var4 = var17.y;
            var5 = var17.z;
            double var9 = var17.w;
            if (!this.activeCustomPartBones.contains(var7)) {
               for (GeoCube var12 : var2.childCubes) {
                  MATRIX_STACK.push();
                  GlStateManager.pushMatrix();
                  this.currentRenderingBone = var2;
                  this.renderCubeGeometry(var1, var12, var3, var4, var5, var6, (double)var9);
                  GlStateManager.popMatrix();
                  MATRIX_STACK.pop();
               }
            }

            for (GeoBone var19 : var2.childBones) {
               if (var9 == 0.0) {
                  this.renderRecursively(var1, var19, var3, var4, var5, var6);
               } else {
                  this.renderCustomBones(var1, var19, var3, var4, var5, var6, (double)var9);
               }
            }
         }

         try {
            MATRIX_STACK.pop();
         } catch (IllegalStateException var13) {
         }
      }
   }

   public boolean shouldRenderFirstPersonHead() {
      if (!((AbstractPlayerGirlEntity)this.playerGirl).hasOwnerUUID()) {
         return true;
      } else {
         return mc.gameSettings.thirdPersonView != 0 ? true : mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiContainerCreative;
      }
   }

   public void renderOverlay(BufferBuilder var1, GeoBone var2, Color var3) {
      GlStateManager.pushMatrix();
      Tessellator.getInstance().draw();
      com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(IGeoRenderer.MATRIX_STACK, var2);
      GL11.glEnable(2896);
      this.preRenderCallback();
      new GirlLayerRenderer(this).render(this.playerGirl, this.playerGirl.limbSwing, this.playerGirl.limbSwingAmount, this.partialTicks, 0.0F, 0.0F, 0.0F, var3);
      this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.playerGirl)));
      var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      GL11.glDisable(2896);
      GlStateManager.popMatrix();
   }

   protected void preRenderCallback() {
   }

   public void renderEquippedItem(BufferBuilder var1, GeoBone var2, boolean var3) {
      ItemRenderer var4 = Minecraft.getMinecraft().getItemRenderer();
      GlStateManager.pushMatrix();
      Tessellator.getInstance().draw();
      com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(IGeoRenderer.MATRIX_STACK, var2);
      GL11.glEnable(2896);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      ItemStack var5 = var3 ? this.offhandItem : this.mainhandItem;
      switch (var5.getItem().getItemUseAction(var5)) {
         case BOW:
            this.applyBowRotation(var3);
            break;
         case BLOCK:
            this.applyShieldBlockingTransform(var3, this.isUsingItem);
      }

      if (this.isUsingItem && !var3 && var5.getItem() instanceof ItemBow) {
         this.bowPullProgress += 0.015F;
         this.playerGirl.setItemUseCount(Math.round(-this.bowPullProgress * 20.0F + var5.getMaxItemUseDuration()));
         this.playerGirl.setHeldItemOverride(var5);
         this.playerGirl.setActiveHand(EnumHand.MAIN_HAND);
         this.playerGirl.setHandActiveState();
      } else {
         this.bowPullProgress = 0.0F;
         this.playerGirl.setItemUseCount(0);
         this.playerGirl.setHeldItemOverride(ItemStack.EMPTY);
         this.playerGirl.setHandActiveState();
      }

      this.applyItemPostRotation(var3, var5);
      GlStateManager.scale(0.75F, 0.75F, 0.75F);
      var4.renderItem(this.playerGirl, var5, TransformType.THIRD_PERSON_RIGHT_HAND);
      var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.playerGirl)));
      GL11.glDisable(2896);
      GlStateManager.popMatrix();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
   }

   protected void applyItemPostRotation(boolean var1, ItemStack var2) {
      GlStateManager.rotate(var1 ? 200.0F : 90.0F, 1.0F, 0.0F, 0.0F);
   }

   protected void applyBowRotation(boolean var1) {
      GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
   }

   protected void applyShieldBlockingTransform(boolean var1, boolean var2) {
      if (var1) {
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         if (var2) {
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(35.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0F, 0.0F, 0.228F);
         }
      } else if (var2) {
         GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.translate(0.0F, 0.165F, 0.0F);
      }
   }

}
