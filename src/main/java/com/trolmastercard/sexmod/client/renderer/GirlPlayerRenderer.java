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
   public ItemStack s = ItemStack.field_190927_a;
   public ItemStack x = ItemStack.field_190927_a;
   public boolean r = false;
   public boolean u = false;
   protected AbstractPlayerGirlEntity w;
   protected float y;
   float t = 0.0F;

   public GirlPlayerRenderer(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2, 0.0);
   }

   public void func_76979_b(Entity var1, double var2, double var4, double var6, float var8, float var9) {
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
            EntityPlayer var11 = Minecraft.func_71410_x().field_71439_g.field_70170_p.func_152378_a(var10.getOwnerUserUUID());
            if (var11 != null) {
               this.s = var11.func_184614_ca();
               this.x = var11.func_184592_cb();
               this.u = var10.ah;
               this.r = var10.ad;
               this.w = (AbstractPlayerGirlEntity)var1;
               this.y = var9;
               var10.f(var11);
               if (this.a_clash366(var11, var1)) {
                  this.func_147906_a(var1, var11.func_70005_c_(), var2, var4 + var10.i_clash226(), var6, 300);
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
      if (var1.getPersistentID().equals(Minecraft.func_71410_x().field_71439_g.getPersistentID())) {
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
      if (this.u && (this.s.func_77973_b() instanceof ItemBow || this.x.func_77973_b() instanceof ItemBow)) {
         if (var7.equals("armR")) {
            var2.setRotationX(var2.getRotationX() - this.j.field_70125_A / 50.0F);
         }

         if (var7.equals("armL")) {
            var2.setRotationY(var2.getRotationY() - this.j.field_70125_A / 50.0F);
         }

         if (this.x.func_77973_b() instanceof ItemBow) {
            ItemStack var8 = this.x;
            this.x = this.s;
            this.s = var8;
         }
      }

      if (this.u && this.s.func_77973_b() instanceof ItemShield) {
         if (var7.equals("armR")) {
            var2.setRotationZ(0.0F);
            var2.setRotationX(0.5F);
         } else if (this.x.func_77973_b() instanceof ItemShield && var7.equals("armL")) {
            var2.setRotationZ(0.0F);
            var2.setRotationX(0.5F);
         }
      }

      if (var7.equals("weapon") && !this.s.func_190926_b()) {
         this.a(var1, var2, false);
      }

      if (var7.equals("offhand") && !this.x.func_190926_b()) {
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
                  GlStateManager.func_179094_E();
                  this.q = var2;
                  this.a(var1, var12, var3, var4, var5, var6, (double)var9);
                  GlStateManager.func_179121_F();
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
         return i.field_71474_y.field_74320_O != 0 ? true : i.field_71462_r instanceof GuiInventory || i.field_71462_r instanceof GuiContainerCreative;
      }
   }

   public void a(BufferBuilder var1, GeoBone var2, Color var3) {
      GlStateManager.func_179094_E();
      Tessellator.func_178181_a().func_78381_a();
      com.trolmastercard.sexmod.MatrixHelper.a(IGeoRenderer.MATRIX_STACK, var2);
      GL11.glEnable(2896);
      this.c_clash145();
      new GirlLayerRenderer(this).render(this.j, this.j.field_184619_aG, this.j.field_70721_aZ, this.y, 0.0F, 0.0F, 0.0F, var3);
      this.func_110776_a(Objects.requireNonNull(this.getEntityTexture(this.j)));
      var1.func_181668_a(7, DefaultVertexFormats.field_181712_l);
      GlStateManager.func_179147_l();
      GlStateManager.func_187401_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      GL11.glDisable(2896);
      GlStateManager.func_179121_F();
   }

   protected void c_clash145() {
   }

   public void a(BufferBuilder var1, GeoBone var2, boolean var3) {
      ItemRenderer var4 = Minecraft.func_71410_x().func_175597_ag();
      GlStateManager.func_179094_E();
      Tessellator.func_178181_a().func_78381_a();
      com.trolmastercard.sexmod.MatrixHelper.a(IGeoRenderer.MATRIX_STACK, var2);
      GL11.glEnable(2896);
      GlStateManager.func_179147_l();
      GlStateManager.func_187401_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      ItemStack var5 = var3 ? this.x : this.s;
      switch (var5.func_77973_b().func_77661_b(var5)) {
         case BOW:
            this.a_clash146(var3);
            break;
         case BLOCK:
            this.a(var3, this.u);
      }

      if (this.u && !var3 && var5.func_77973_b() instanceof ItemBow) {
         this.t += 0.015F;
         this.j.d(Math.round(-this.t * 20.0F + var5.func_77988_m()));
         this.j.a_clash517(var5);
         this.j.func_184598_c(EnumHand.MAIN_HAND);
         this.j.W();
      } else {
         this.t = 0.0F;
         this.j.d(0);
         this.j.a_clash517(ItemStack.field_190927_a);
         this.j.W();
      }

      this.a(var3, var5);
      GlStateManager.func_179152_a(0.75F, 0.75F, 0.75F);
      var4.func_178099_a(this.j, var5, TransformType.THIRD_PERSON_RIGHT_HAND);
      var1.func_181668_a(7, DefaultVertexFormats.field_181712_l);
      this.func_110776_a(Objects.requireNonNull(this.getEntityTexture(this.j)));
      GL11.glDisable(2896);
      GlStateManager.func_179121_F();
      GlStateManager.func_179147_l();
      GlStateManager.func_187401_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
   }

   protected void a(boolean var1, ItemStack var2) {
      GlStateManager.func_179114_b(var1 ? 200.0F : 90.0F, 1.0F, 0.0F, 0.0F);
   }

   protected void a_clash146(boolean var1) {
      GlStateManager.func_179114_b(20.0F, 1.0F, 0.0F, 0.0F);
   }

   protected void a(boolean var1, boolean var2) {
      if (var1) {
         GlStateManager.func_179114_b(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.func_179114_b(90.0F, 1.0F, 0.0F, 0.0F);
         if (var2) {
            GlStateManager.func_179114_b(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.func_179114_b(35.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.func_179114_b(-20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.func_179109_b(0.0F, 0.0F, 0.228F);
         }
      } else if (var2) {
         GlStateManager.func_179114_b(-90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.func_179114_b(-90.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.func_179109_b(0.0F, 0.165F, 0.0F);
      }
   }

}
