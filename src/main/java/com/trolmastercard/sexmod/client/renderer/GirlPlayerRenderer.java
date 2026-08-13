package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;







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
   public static boolean v = false;
   public ItemStack s = ItemStack.EMPTY;
   public ItemStack x = ItemStack.EMPTY;
   public boolean r = false;
   public boolean u = false;
   protected AbstractPlayerGirlEntity w;
   protected float y;
   float t = 0.0F;

   public GirlPlayerRenderer(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2, 0.0);
   }

   public void doRenderShadowAndFire(Entity var1, double var2, double var4, double var6, float var8, float var9) {
   }

   boolean a_clash365(BaseGirlEntity var1) {
      if (var1.isLocallyRegistered()) {
         return true;
      }

      boolean var2 = v;
      v = false;
      return var2;
   }

   @Override
   public void a(BaseGirlEntity var1, double var2, double var4, double var6, float var8, float var9) {
      if (this.a_clash365(var1)) {
         AbstractPlayerGirlEntity var10 = (AbstractPlayerGirlEntity)var1;
         if (var10.getOwnerUserUUID() != null) {
            EntityPlayer var11 = Minecraft.getMinecraft().player.world.getPlayerEntityByUUID(var10.getOwnerUserUUID());
            if (var11 != null) {
               this.s = var11.getHeldItemMainhand();
               this.x = var11.getHeldItemOffhand();
               this.u = var10.ah;
               this.r = var10.ad;
               this.w = (AbstractPlayerGirlEntity)var1;
               this.y = var9;
               var10.f(var11);
               if (this.a_clash366(var11, var1)) {
                  this.renderLivingLabel(var1, var11.getName(), var2, var4 + var10.i_clash226(), var6, 300);
               }

               super.a(var1, var2, var4, var6, var8, var9);
            }
         }
      }
   }

   @Override
   public Entity c_clash336(BaseGirlEntity var1) {
      if (!(var1 instanceof AbstractPlayerGirlEntity)) {
         return var1;
      }

      AbstractPlayerGirlEntity var2 = (AbstractPlayerGirlEntity)var1;
      EntityPlayer var3 = var2.k_clash584();
      return (Entity)(var3 == null ? var1 : var3);
   }

   boolean a_clash366(EntityPlayer var1, BaseGirlEntity var2) {
      if (var1.getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID())) {
         return false;
      }

      fp var3 = var2.getCurrentAction();
      return var3 == null ? true : !var3.hideNameTag;
   }

   protected void a(String var1, GeoBone var2) {
   }

   protected void a(String var1, GeoBone var2, AbstractPlayerGirlEntity var3, BufferBuilder var4) {
   }

   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      String var7 = var2.getName();
      if (this.r) {
         if (var7.equals("upperBody")) {
            var2.setRotationX(var2.getRotationX() - 0.5F);
         }

         if (var7.equals("head")) {
            var2.setRotationX(var2.getRotationX() + 0.5F);
         }
      }

      if (var7.equals("head")) {
         this.a(var1, var2, Color.ofRGB(var3, var4, var5));
      }

      this.a(var7, var2);
      this.a(var7, var2, this.w, var1);
      if (this.u && (this.s.getItem() instanceof ItemBow || this.x.getItem() instanceof ItemBow)) {
         if (var7.equals("armR")) {
            var2.setRotationX(var2.getRotationX() - this.j.rotationPitch / 50.0F);
         }

         if (var7.equals("armL")) {
            var2.setRotationY(var2.getRotationY() - this.j.rotationPitch / 50.0F);
         }

         if (this.x.getItem() instanceof ItemBow) {
            ItemStack var8 = this.x;
            this.x = this.s;
            this.s = var8;
         }
      }

      if (this.u && this.s.getItem() instanceof ItemShield) {
         if (var7.equals("armR")) {
            var2.setRotationZ(0.0F);
            var2.setRotationX(0.5F);
         } else if (this.x.getItem() instanceof ItemShield && var7.equals("armL")) {
            var2.setRotationZ(0.0F);
            var2.setRotationX(0.5F);
         }
      }

      if (var7.equals("weapon") && !this.s.isEmpty()) {
         this.a(var1, var2, false);
      }

      if (var7.equals("offhand") && !this.x.isEmpty()) {
         this.a(var1, var2, true);
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
            Vector4f var17 = this.a(var7, var3, var4, var5);
            var3 = var17.x;
            var4 = var17.y;
            var5 = var17.z;
            double var9 = var17.w;
            if (!this.p.contains(var7)) {
               for (GeoCube var12 : var2.childCubes) {
                  MATRIX_STACK.push();
                  GlStateManager.pushMatrix();
                  this.q = var2;
                  this.a(var1, var12, var3, var4, var5, var6, (double)var9);
                  GlStateManager.popMatrix();
                  MATRIX_STACK.pop();
               }
            }

            for (GeoBone var19 : var2.childBones) {
               if (var9 == 0.0) {
                  this.renderRecursively(var1, var19, var3, var4, var5, var6);
               } else {
                  this.a(var1, var19, var3, var4, var5, var6, (double)var9);
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
      if (!((AbstractPlayerGirlEntity)this.j).f_clash579()) {
         return true;
      } else {
         return i.gameSettings.thirdPersonView != 0 ? true : i.currentScreen instanceof GuiInventory || i.currentScreen instanceof GuiContainerCreative;
      }
   }

   public void a(BufferBuilder var1, GeoBone var2, Color var3) {
      GlStateManager.pushMatrix();
      Tessellator.getInstance().draw();
      com.trolmastercard.sexmod.MatrixHelper.a(IGeoRenderer.MATRIX_STACK, var2);
      GL11.glEnable(2896);
      this.c_clash145();
      new GirlLayerRenderer(this).render(this.j, this.j.limbSwing, this.j.limbSwingAmount, this.y, 0.0F, 0.0F, 0.0F, var3);
      this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.j)));
      var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      GL11.glDisable(2896);
      GlStateManager.popMatrix();
   }

   protected void c_clash145() {
   }

   public void a(BufferBuilder var1, GeoBone var2, boolean var3) {
      ItemRenderer var4 = Minecraft.getMinecraft().getItemRenderer();
      GlStateManager.pushMatrix();
      Tessellator.getInstance().draw();
      com.trolmastercard.sexmod.MatrixHelper.a(IGeoRenderer.MATRIX_STACK, var2);
      GL11.glEnable(2896);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      ItemStack var5 = var3 ? this.x : this.s;
      switch (var5.getItem().getItemUseAction(var5)) {
         case BOW:
            this.a_clash146(var3);
            break;
         case BLOCK:
            this.a(var3, this.u);
      }

      if (this.u && !var3 && var5.getItem() instanceof ItemBow) {
         this.t += 0.015F;
         this.j.d(Math.round(-this.t * 20.0F + var5.getMaxItemUseDuration()));
         this.j.setHeldItemOverride(var5);
         this.j.setActiveHand(EnumHand.MAIN_HAND);
         this.j.W();
      } else {
         this.t = 0.0F;
         this.j.d(0);
         this.j.setHeldItemOverride(ItemStack.EMPTY);
         this.j.W();
      }

      this.a(var3, var5);
      GlStateManager.scale(0.75F, 0.75F, 0.75F);
      var4.renderItem(this.j, var5, TransformType.THIRD_PERSON_RIGHT_HAND);
      var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.j)));
      GL11.glDisable(2896);
      GlStateManager.popMatrix();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
   }

   protected void a(boolean var1, ItemStack var2) {
      GlStateManager.rotate(var1 ? 200.0F : 90.0F, 1.0F, 0.0F, 0.0F);
   }

   protected void a_clash146(boolean var1) {
      GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
   }

   protected void a(boolean var1, boolean var2) {
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
