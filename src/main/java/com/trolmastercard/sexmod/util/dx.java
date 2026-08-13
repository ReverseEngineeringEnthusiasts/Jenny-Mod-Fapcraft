package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.renderer.GalathRenderer;
import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BodyParts;
import com.trolmastercard.sexmod.entity.api.IGalath;







import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import javax.annotation.Nullable;
import javax.vecmath.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class dx extends GirlPlayerRenderer {
   static final HashSet<String> z = new HashSet<>(
      Arrays.asList(
         "kneeL",
         "kneeR",
         "shinL",
         "shinR",
         "armorHelmet",
         "sockL",
         "sockR",
         "braBoobL",
         "braBoobR",
         "armorNippleR",
         "armorNippleL",
         "slip",
         "turnable",
         "static"
      )
   );

   public dx(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   @Nullable
   @Override
   protected f7 e_clash326(BaseGirlEntity var1) {
      if (var1.world instanceof SexWorldClient) {
         return null;
      } else {
         return ((IGalath)var1).c_clash21() ? null : GalathRenderer.y;
      }
   }

   @Override
   public HashSet<String> a() {
      GalathRenderer.E.addAll(BodyParts.a);
      return GalathRenderer.E;
   }

   @Override
   protected void b(Tessellator var1, BufferBuilder var2, BaseGirlEntity var3, f7 var4, float var5) {
      a(var1, var2, var3, var4, var5);
   }

   @Override
   public void a(BaseGirlEntity var1, double var2, double var4, double var6, float var8, float var9) {
      super.a(var1, var2, var4, var6, var8, var9);
      if (i.gameSettings.thirdPersonView != 0 || !i.player.getPersistentID().equals(((AbstractPlayerGirlEntity)var1).getOwnerUserUUID()) || var1.isAnchored()) {
         GalathRenderer.a_clash324(var1, var9);
      }
   }

   @Override
   protected void a_clash146(boolean var1) {
      super.a_clash146(var1);
      if (var1) {
         GlStateManager.translate(0.15, 0.0, 0.0);
      }
   }

   @Override
   protected void a(boolean var1, boolean var2) {
      super.a(var1, var2);
      if (var1) {
         GlStateManager.translate(0.0, -0.05, -0.05);
         GlStateManager.rotate(15.0F, 1.0F, 0.0F, 0.0F);
         if (var2) {
            GlStateManager.translate(0.3, 0.2, 0.0);
            GlStateManager.rotate(-30.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(15.0F, 0.0F, 0.0F, 1.0F);
         }
      } else {
         GlStateManager.translate(0.0, 0.0, 0.1);
         GlStateManager.rotate(30.0F, 1.0F, 0.0F, 0.0F);
         if (var2) {
            GlStateManager.rotate(-29.0F, 1.0F, 0.0F, 0.0F);
         }
      }
   }

   @Override
   protected Vector4f a(String var1, float var2, float var3, float var4) {
      if (!z.contains(var1)) {
         return this.a_clash337(var2, var3, var4);
      }

      if ("armorHelmet".equals(var1)) {
         return super.a(var1, var2, var3, var4);
      }

      ItemStack var5 = ItemStack.EMPTY;
      switch (var1) {
         case "braBoobL":
         case "braBoobR":
         case "armorNippleR":
         case "armorNippleL":
            var5 = (ItemStack)this.j.getDataManager().get(AbstractGirlNpcEntity.T);
            break;
         case "turnable":
         case "static":
         case "slip":
            var5 = (ItemStack)this.j.getDataManager().get(AbstractGirlNpcEntity.U);
            break;
         case "shinL":
         case "shinR":
         case "sockL":
         case "sockR":
         case "kneeL":
         case "kneeR":
            var5 = (ItemStack)this.j.getDataManager().get(AbstractGirlNpcEntity.W);
      }

      if (!(var5.getItem() instanceof ItemArmor)) {
         return this.a_clash337(var2, var3, var4);
      }

      ItemArmor var14 = (ItemArmor)var5.getItem();
      switch (var14.getArmorMaterial()) {
         case GOLD:
            return new Vector4f(var2, var3, var4, -0.15625F);
         case IRON:
         case CHAIN:
            return new Vector4f(var2, var3, var4, -0.125F);
         case LEATHER:
            int var15 = var14.getColor(var5);
            float var8 = (var15 >> 16 & 0xFF) / 255.0F;
            float var9 = (var15 >> 8 & 0xFF) / 255.0F;
            float var10 = (var15 & 0xFF) / 255.0F;
            var2 *= var8;
            var3 *= var9;
            var4 *= var10;
            return new Vector4f(var2, var3, var4, -0.09375F);
         default:
            return new Vector4f(var2, var3, var4, -0.1875F);
      }
   }

   @Override
   protected void a(GeoModel var1, BufferBuilder var2, BaseGirlEntity var3, float var4, float var5, float var6, float var7, float var8) {
      GeoBone var9 = var1.topLevelBones.get(0);
      GeoBone var10 = null;
      GeoBone var11 = null;

      for (GeoBone var13 : var9.childBones) {
         switch (var13.getName()) {
            case "steve":
               var11 = var13;
               break;
            case "body":
               var10 = var13;
         }
      }

      MATRIX_STACK.push();
      MATRIX_STACK.translate(var9);
      MATRIX_STACK.moveToPivot(var9);
      MATRIX_STACK.rotate(var9);
      MATRIX_STACK.scale(var9);
      MATRIX_STACK.moveBackFromPivot(var9);
      this.renderRecursively(var2, var10, var4, var5, var6, var7);
      Tessellator.getInstance().draw();
      var2.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

      Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture(this.j));

      this.renderRecursively(var2, var11, var4, var5, var6, this.j.v_clash550());
      Tessellator.getInstance().draw();
      MATRIX_STACK.pop();
   }

}
