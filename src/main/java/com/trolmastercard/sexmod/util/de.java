package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.KoboldRenderer;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.entity.KoboldPlayerEntity;







import javax.vecmath.Vector4f;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class de extends d9 {
   public de(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   @Override
   protected Vec3i a_clash192(String var1) {
      EntityDataManager var2 = this.j.func_184212_Q();
      EyeAndKoboldColor var3 = EyeAndKoboldColor.valueOf((String)var2.func_187225_a(KoboldEntity.N));
      BlockPos var4 = (BlockPos)var2.func_187225_a(KoboldEntity.K);
      if (KoboldRenderer.t.contains(var1)) {
         return var3.getMainColor();
      } else if (KoboldRenderer.u.contains(var1)) {
         return var3.getSecondaryColor();
      } else {
         return (Vec3i)(!"irisR".equals(var1) && !"irisL".equals(var1) ? z : var4);
      }
   }

   @Override
   protected Vector4f a(String var1, float var2, float var3, float var4) {
      if ("mouth".equals(var1)) {
         String[] var5 = AbstractNpcOnlyEntity.a_clash225(this.j);
         int var6 = Integer.parseInt(var5[7]);
         if (var6 == 1) {
            return new Vector4f(var2, var3, var4, -0.078125F);
         }
      }

      return super.a(var1, var2, var3, var4);
   }

   @Override
   protected void d_clash331() {
      float var1 = 0.25F - (Float)this.j.func_184212_Q().func_187225_a(KoboldPlayerEntity.aA);
      GlStateManager.func_179152_a(1.0F - var1, 1.0F - var1, 1.0F - var1);
   }

   @Override
   protected void b_clash332() {
      float var1 = 0.25F - (Float)this.j.func_184212_Q().func_187225_a(KoboldPlayerEntity.aA);
      double var2 = 1.0 / (1.0 - var1);
      GlStateManager.func_179139_a(var2, var2, var2);
   }

   @Override
   protected void c_clash145() {
      GlStateManager.func_179137_b(0.0, -0.8F, 0.05);
      GlStateManager.func_179139_a(0.5, 0.5, 0.5);
   }

   @Override
   protected void a(boolean var1, ItemStack var2) {
      super.a(var1, var2);
      if (var2.func_77973_b().func_77661_b(var2) == EnumAction.BOW) {
         if (!var1) {
            GlStateManager.func_179114_b(170.0F, 1.0F, 0.0F, 0.0F);
         }

         if (var1) {
            GlStateManager.func_179109_b(0.1F, 0.0F, 0.0F);
         }
      } else {
         GlStateManager.func_179114_b(var1 ? 80.0F : 180.0F, 1.0F, 0.0F, 0.0F);
      }
   }

   @Override
   protected void a(boolean var1, boolean var2) {
      super.a(var1, var2);
      if (var1) {
         if (var2) {
            GlStateManager.func_179137_b(0.06, 0.0, -0.13);
            GlStateManager.func_179114_b(60.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.func_179114_b(38.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.func_179114_b(90.0F, 0.0F, 0.0F, 1.0F);
         } else {
            GlStateManager.func_179114_b(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.func_179137_b(0.0, -0.3F, -0.13);
         }
      } else if (var2) {
         GlStateManager.func_179114_b(150.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.func_179137_b(0.0, -0.35, 0.0);
      } else {
         GlStateManager.func_179137_b(0.0, -0.1, -0.083F);
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
