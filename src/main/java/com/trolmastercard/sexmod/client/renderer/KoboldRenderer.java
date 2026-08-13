package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.entity.KoboldPlayerEntity;







import java.util.Arrays;
import java.util.HashSet;
import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class KoboldRenderer extends GirlRendererBase<KoboldEntity> {
   public static final HashSet<String> t = new HashSet<>(
      Arrays.asList(
         "colorSpots",
         "neck",
         "head",
         "snout",
         "midSectionR",
         "midSectionL",
         "innerCheekLR",
         "innerCheekRR",
         "gayL",
         "gayR",
         "legR",
         "legL",
         "shinL",
         "toesL",
         "kneeL",
         "curvesL",
         "shinR",
         "toesR",
         "kneeR",
         "curvesR",
         "sideL",
         "sideR",
         "hip",
         "torsoL",
         "torsoR",
         "armR",
         "lowerArmR",
         "ellbowR",
         "armL",
         "lowerArmL",
         "ellbowL",
         "hornUL",
         "hornUR",
         "tail",
         "tail2",
         "tail3",
         "tail4",
         "tail5",
         "hornDL2",
         "hornDR2",
         "hornDR3M",
         "hornDL3M",
         "frecklesAL1",
         "frecklesAL2",
         "frecklesAR1",
         "frecklesAR2",
         "frecklesHL1",
         "frecklesHL2",
         "frecklesHR1",
         "frecklesHR2"
      )
   );
   public static final HashSet<String> u = new HashSet<>(
      Arrays.asList(
         "boobR",
         "boobL",
         "frontNeck",
         "Rside",
         "Lside",
         "frontAndInside",
         "innerCheekLL",
         "innerCheekRL",
         "layer",
         "layer2",
         "down",
         "down2",
         "down3",
         "down4",
         "down5",
         "fuckhole",
         "hornDR3S",
         "hornDL3S",
         "assholeCoverUp",
         "assholeCoverUp2"
      )
   );
   Minecraft w = Minecraft.getMinecraft();
   Vector3f v;

   public KoboldRenderer(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
   }

   @Override
   protected Vec3i getBoneColor(String var1) {
      EntityDataManager var2 = this.j.getDataManager();
      EyeAndKoboldColor var3 = EyeAndKoboldColor.valueOf((String)var2.get(KoboldEntity.N));
      BlockPos var4 = (BlockPos)var2.get(KoboldEntity.K);
      if (t.contains(var1)) {
         return var3.getMainColor();
      } else if (u.contains(var1)) {
         return var3.getSecondaryColor();
      } else {
         return (Vec3i)(!"irisR".equals(var1) && !"irisL".equals(var1) ? r : var4);
      }
   }

   @Override
   protected ItemStack a_clash341(@Nullable ItemStack var1) {
      switch (this.j.getCurrentAction()) {
         case MINE:
            if ((Boolean)this.j.getDataManager().get(KoboldEntity.at)) {
               return new ItemStack(Items.IRON_AXE);
            }

            return new ItemStack(Items.IRON_PICKAXE);
         case NULL:
            if ((Boolean)this.j.getDataManager().get(KoboldEntity.aC)) {
               return new ItemStack(Items.IRON_SWORD);
            }
         default:
            return var1;
         case ATTACK:
            return new ItemStack(Items.IRON_SWORD);
      }
   }

   @Override
   public void a(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6, double var7) {
      if (!(this.j.world instanceof SexWorldClient)) {
         String var9 = var2.getName();
         if ("blowOpening".equals(var9)) {
            var7 = 0.0;
         }

         if ("mouth".equals(var9)) {
            String[] var10 = AbstractNpcOnlyEntity.getModelCodeParts(this.j);
            int var11 = Integer.parseInt(var10[7]);
            if (var11 == 1) {
               var7 = -0.078125;
            }
         }

         super.a(var1, var2, var3, var4, var5, var6, var7);
      }
   }

   @Override
   protected void d_clash331() {
      float var1 = 0.25F - (Float)this.j.getDataManager().get(KoboldPlayerEntity.aA);
      GlStateManager.scale(1.0F - var1, 1.0F - var1, 1.0F - var1);
   }

   @Override
   protected void b_clash332() {
      float var1 = 0.25F - (Float)this.j.getDataManager().get(KoboldPlayerEntity.aA);
      double var2 = 1.0 / (1.0 - var1);
      GlStateManager.scale(var2, var2, var2);
   }

   @Override
   protected ItemStack a_clash340() {
      String var1 = (String)this.j.getDataManager().get(BaseGirlEntity.h);
      if ("STARTBLOWJOB".equals(var1)) {
         return new ItemStack(Items.IRON_PICKAXE);
      } else {
         return "ANAL_START".equals(var1) ? new ItemStack(Items.GOLD_INGOT, 3) : null;
      }
   }

   public void a(KoboldEntity var1, double var2, double var4, double var6, float var8, float var9) {
      String var10 = (String)var1.getDataManager().get(AbstractNpcOnlyEntity.N);
      if (var1.as == null) {
         var1.as = var10;
      }

      if (!var1.as.equals(var10)) {
         clearBoneColors();
         var1.as = var10;
      }

      this.v = new Vector3f((float)var2, (float)var4, (float)var6);
      super.a(var1, var2, var4, var6, var8, var9);
   }

   @Override
   protected void a_clash199(double var1, double var3, double var5) {
      EntityDataManager var7 = this.j.getDataManager();
      String var8 = (String)var7.get(KoboldEntity.aU);
      if ("null".equals(var8)) {
         super.a_clash199(var1, var3, var5);
      } else {
         EyeAndKoboldColor var9 = EyeAndKoboldColor.valueOf((String)var7.get(KoboldEntity.N));
         var8 = var9.getTextColor() + " -" + var8 + "-";
         this.renderLivingLabel(this.j, this.j.ab_clash540() + var8, var1, var3 + this.j.i_clash226(), var5, 300);
      }
   }

}
