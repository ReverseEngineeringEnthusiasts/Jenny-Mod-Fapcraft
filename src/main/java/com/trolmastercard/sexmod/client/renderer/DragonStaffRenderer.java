package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.model.DragonStaffModel;
import com.trolmastercard.sexmod.client.model.GalathModel;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.item.DragonStaffItem;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ck;







import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class DragonStaffRenderer extends GeoItemRenderer<DragonStaffItem> {
   private static final ResourceLocation c = new ResourceLocation("textures/entity/endercrystal/endercrystal.png");
   private final GalathModel q = new GalathModel();
   static final float p = 10.0F;
   static final float f = 1.5F;
   static final float m = 0.175F;
   static final float r = 0.1F;
   static final float g = 0.04F;
   static final float d = 8.0F;
   static final float i = 6.0F;
   static final float a = 1.3F;
   static final Vector2f[] l = new Vector2f[]{
      new Vector2f(1.0F, 0.0F),
      new Vector2f(0.0F, 1.0F),
      new Vector2f(0.0F, 0.0F),
      new Vector2f(0.5F, 0.5F),
      new Vector2f(0.75F, 0.25F),
      new Vector2f(0.25F, 0.75F),
      new Vector2f(0.25F, 0.75F)
   };
   static boolean o = false;
   Minecraft e;
   Vector2f j;
   double b = 0.0;
   EntityPlayer k;
   ItemStack h;
   static HashMap<ItemStack, Vector3f> n = new HashMap<>();

   public DragonStaffRenderer() {
      super(new DragonStaffModel());
      this.e = Minecraft.getMinecraft();
   }

   public static boolean b_clash631() {
      return o;
   }

   public static void a_clash632() {
      o = !o;
   }

   @Override
   public void render(DragonStaffItem var1, ItemStack var2) {
      this.a(var1, var2);
   }

   public void a(DragonStaffItem var1, ItemStack var2) {
      EntityPlayer var3 = null;

      for (EntityPlayer var5 : this.e.world.playerEntities) {
         if (var5.inventory.mainInventory.contains(var2)) {
            var3 = var5;
            break;
         }

         if (var5.inventory.offHandInventory.contains(var2)) {
            var3 = var5;
            break;
         }
      }

      if (var3 != null) {
         double var10 = var3.posX - var3.lastTickPosX;
         double var6 = var3.posZ - var3.lastTickPosZ;
         double var8 = (Math.PI / 180.0) * var3.rotationYaw;
         this.j = new Vector2f((float)(var10 * Math.cos(var8) + var6 * Math.sin(var8)), (float)(-var10 * Math.sin(var8) + var6 * Math.cos(var8)));
      } else {
         this.j = new Vector2f(0.0F, 0.0F);
      }

      if (!Minecraft.getMinecraft().isGamePaused()) {
         this.b = Minecraft.getMinecraft().player.ticksExisted + this.e.getRenderPartialTicks();
      }

      this.h = var2;
      this.k = var3;
      super.render(var1, var2);
   }

   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      if ("staff".equals(var2.getName())) {
         GlStateManager.pushMatrix();
         Tessellator.getInstance().draw();
         com.trolmastercard.sexmod.MatrixHelper.a(IGeoRenderer.MATRIX_STACK, var2);
         GlStateManager.translate(0.0, 1.5 + 0.001 * Math.sin(0.005 * this.b) + 0.001, 0.0);
         Vector3f var7 = n.get(this.h);
         GlStateManager.scale(this.d_clash636(), this.d_clash636(), this.d_clash636());
         if (var7 == null) {
            var7 = new Vector3f(0.0F, 0.0F, 0.0F);
         }

         var7.add(new Vector3f(this.j.x, this.k == null ? 0.0F : (float)(this.k.posY - this.k.lastTickPosY), this.j.y));
         GlStateManager.rotate(var7.z * 10.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(var7.x * 10.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(-var7.y * 10.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.rotate((float)(this.b * 0.1F), 1.0F, 1.0F, 1.0F);
         n.put(this.h, var7);
         this.e.getTextureManager().bindTexture(c);
         this.q.render(Minecraft.getMinecraft().player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.popMatrix();
         if (this.k != null) {
            this.c_clash633();
         }

         this.e.getTextureManager().bindTexture(new DragonStaffModel().getTextureLocation((DragonStaffItem) null));
         var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      }

      super.renderRecursively(var1, var2, var3, var4, var5, var6);
   }

   void c_clash633() {
      ArrayList var1 = new ArrayList();
      ArrayList var2 = new ArrayList();

      for (Vector4d var4 : KoboldEntity.aY) {
         var1.add((int)var4.getW());
         var2.add(new Vec3d(var4.getX(), var4.getY(), var4.getZ()));
      }

      if (var1.size() != 0) {
         if (o) {
            this.a(var1, var2);
         } else {
            this.a_clash634(var1);
         }
      }
   }

   void a(List<Integer> var1, List<Vec3d> var2) {
      for (int var3 = 0; var3 < var1.size(); var3++) {
         float var4 = RotationHelper.lerp(this.k.prevRotationYawHead, this.k.rotationYawHead, this.e.getRenderPartialTicks());
         float var5 = RotationHelper.lerp(this.k.prevRotationPitch, this.k.rotationPitch, this.e.getRenderPartialTicks());
         Vec3d var6 = RotationHelper.a(
            new Vec3d(this.k.prevPosX, this.k.prevPosY + this.k.getEyeHeight(), this.k.prevPosZ),
            this.k.getPositionVector().add(0.0, this.k.getEyeHeight(), 0.0),
            this.e.getRenderPartialTicks()
         );
         Vec3d var7 = var6.subtract((Vec3d)var2.get(var3));
         var7 = ck.a(var7, -var5, var4);
         double var8 = Math.abs(var7.x) + Math.abs(var7.z) + Math.abs(var7.y);
         double var10 = -var7.x / var8;
         double var12 = -var7.y / var8;
         double var14 = var7.z / var8;
         var10 = this.a_clash635(var10);
         var12 = this.a_clash635(var12);
         var14 = this.a_clash635(var14);
         var10 *= 1.3F;
         var12 *= 1.3F;
         var14 *= 1.3F;
         this.b((Integer)var1.get(var3), (float)var10, (float)var12, (float)var14);
      }
   }

   void a_clash634(List<Integer> var1) {
      float var2 = 1.0F / var1.size();
      float var3 = 0.0F;

      for (int var4 = 0; var4 < var1.size(); var4++) {
         var3 += var2;
         this.a((Integer)var1.get(var4), 1.0F - var3, 0.0F + var3, (float)RotationHelper.b(0.8F, 1.2F, (double)var4 / var1.size()));
      }
   }

   double a_clash635(double var1) {
      return var1 * Math.sqrt(1.0 - var1 * var1 / 2.0);
   }

   double d_clash636() {
      return 0.175F + 0.025 * Math.sin(0.005 * this.b) + 0.025;
   }

   void a(int var1, float var2, float var3, float var4) {
      this.a(new ItemStack(Blocks.WOOL, 1, var1), var2, var3, var4);
   }

   void b(int var1, float var2, float var3, float var4) {
      this.b(new ItemStack(Blocks.WOOL, 1, var1), var2, var3, var4);
   }

   void b(ItemStack var1, float var2, float var3, float var4) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0, 1.5 + 0.001 * Math.sin(0.005 * this.b) + 0.001, 0.0);
      GlStateManager.scale(0.04F, 0.04F, 0.04F);
      GlStateManager.translate(var2 * 6.0F, var3 * 6.0F, var4 * 6.0F);
      this.e.getItemRenderer().renderItem(Minecraft.getMinecraft().player, var1, TransformType.NONE);
      GlStateManager.popMatrix();
   }

   void a(ItemStack var1, float var2, float var3, float var4) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0, 1.5 + 0.001 * Math.sin(0.005 * this.b) + 0.001, 0.0);
      GlStateManager.scale(0.04F, 0.04F, 0.04F);
      GlStateManager.rotate((float)(this.b * 8.0 * var4), 0.0F, var2, var3);
      GlStateManager.translate(6.0F, 0.0F, 0.0F);
      this.e.getItemRenderer().renderItem(Minecraft.getMinecraft().player, var1, TransformType.NONE);
      GlStateManager.popMatrix();
   }

}
