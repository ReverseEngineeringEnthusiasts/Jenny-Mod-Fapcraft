package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.model.api.IGirlModelInfo;
import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.TrailSegment;
import com.trolmastercard.sexmod.util.TrigMath;







import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public abstract class GirlModel<T extends BaseGirlEntity> extends GirlModelBase<T> implements IGirlModelInfo {
   public static final List<String> b = Arrays.asList(
      "braStringMidStartR",
      "braStringMidMid1R",
      "braStringMidMid2R",
      "braStringMidMid3R",
      "braStringMidEndR",
      "braStringBackR",
      "braStringRightEndR",
      "braStringRightStartR",
      "braStringRightL",
      "braStringMidMid1L",
      "braStringMidMid2L",
      "braStringMidMid3L",
      "braStringMidEndL",
      "braStringBackL",
      "braStringLeftEndL",
      "braStringLeftStartL",
      "braStringMidStartL",
      "braStringRightR"
   );
   public static final List<String> e = Arrays.asList("boyCam", "girlCam");
   public static boolean d = true;
   protected ResourceLocation[] c = this.getModelLocations();
   protected Minecraft a = Minecraft.getMinecraft();

   protected GirlModel() {
   }

   protected abstract ResourceLocation[] getModelLocations();

   public ResourceLocation getTextureLocation() { return this.getTextureLocation((BaseGirlEntity) null); }

   @Override
   public ResourceLocation getModelLocation(BaseGirlEntity var0) {
      return this.a_clash34(var0);
   }


   public abstract ResourceLocation getTextureLocation(BaseGirlEntity var0);
   public String[] c() { return new String[0]; }
   public String[] g() { return new String[0]; }
   public String[] f() { return new String[0]; }
   public String[] a() { return new String[0]; }
   public String[] h() { return new String[0]; }
   public String[] e() { return new String[0]; }
   public String[] b() { return new String[0]; }
   public String[] d() { return new String[0]; }


   public ResourceLocation c_clash344(BaseGirlEntity var1) {
      return this.getTextureLocation(var1);
   }

   public ResourceLocation a_clash34(BaseGirlEntity var1) {
      if (var1.world instanceof SexWorldClient) {
         return this.c[0];
      } else if ((Integer)var1.getDataManager().get(BaseGirlEntity.OUTFIT_INDEX) > this.c.length) {
         System.out.println("Girl doesn't have an outfit Nr." + var1.getDataManager().get(BaseGirlEntity.OUTFIT_INDEX) + " so im just making her nude lol");
         return this.c[0];
      } else {
         return this.c[var1.getDataManager().get(BaseGirlEntity.OUTFIT_INDEX)];
      }
   }

   public ResourceLocation g_clash345(BaseGirlEntity var1) {
      return this.getTextureLocation();
   }

   @Override
   public void setMolangQueries(IAnimatable var1, double var2) {
      if (Minecraft.getMinecraft().world != null) {
         super.setMolangQueries(var1, var2);
      }
   }

   @Override
   public void setLivingAnimations(T var1, Integer var2, AnimationEvent var3) {
      super.setLivingAnimations((T)var1, var2, var3);
      AnimationProcessor var4 = this.getAnimationProcessor();
      this.a((T)var1, var4);
      if (!(var1.world instanceof SexWorldClient)) {
         if ((Boolean)var1.getDataManager().get(BaseGirlEntity.IS_ANCHORED)) {
            var1.setPositionAndRotationDirect(
               var1.getTargetPosition().x, var1.getTargetPosition().y, var1.getTargetPosition().z, var1.getYawRotation(), 0.0F, 3, true
            );
         }

         if (var1.actionController != null) {
            var1.actionController.transitionLengthTicks = !(var1.world instanceof SexWorldClient) && var1.getCurrentAction() != null ? var1.getCurrentAction().transitionTick : 5.0;
         }

         this.a((T)var1, var4, var3);
         if (var1 instanceof AbstractGirlNpcEntity && !var1.isLocallyRegistered() && var1.getOutfitIndex() != 0) {
            this.a(
               var4,
               (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.X),
               (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.T),
               (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.U),
               (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.W)
            );
         } else {
            this.a(var4);
         }
      }
   }

   public static Vec3d d_clash346(BaseGirlEntity var0) {
      return a_clash347(new Vec3d(var0.lastTickPosX, var0.lastTickPosY, var0.lastTickPosZ), var0.getPositionVector());
   }

   public static Vec3d a(BaseGirlEntity var0, Vec3d var1) {
      return a_clash347(var1, var0.getPositionVector());
   }

   public static Vec3d a_clash347(Vec3d var0, Vec3d var1) {
      Vec3d var2 = var1.subtract(var0);
      Vec3d var3 = new Vec3d(Math.abs(var2.x), Math.abs(var2.y), Math.abs(var2.z));
      double var4 = var3.x / (var3.x + var3.y + var3.z);
      double var6 = var3.y / (var3.x + var3.y + var3.z);
      double var8 = var3.z / (var3.x + var3.y + var3.z);
      Vec3d var10 = new Vec3d(
         (var2.x > 0.0 ? 1 : -1) * var4, (var2.y > 0.0 ? 1 : -1) * var6, (var2.z > 0.0 ? 1 : -1) * var8
      );
      double var11 = var10.y / 2.0 + 0.5;
      float var13 = (float)RotationHelper.b(-180.0, 0.0, var11);
      if (Float.isNaN(var13)) {
         var13 = -90.0F;
      }

      float var14 = var11 < 0.5 ? 0.0F : (float)RotationHelper.b(0.0, 16.0, -var11);
      if (Float.isNaN(var14)) {
         var14 = 0.0F;
      }

      float var15 = (float)(4.0 - Math.sin((Math.PI / 2) + var11 * 2.0 * Math.PI) * 4.0);
      if (Float.isNaN(var15)) {
         var15 = 8.0F;
      }

      return new Vec3d(TrigMath.wrapDegrees(var13), var14, var15);
   }

   void a(AnimationProcessor<T> var1, ItemStack var2, ItemStack var3, ItemStack var4, ItemStack var5) {
      this.c(var1, !var2.isEmpty());
      this.b(var1, var3.getItem() instanceof ItemArmor);
      this.d(var1, !var4.isEmpty());
      this.a(var1, !var5.isEmpty());
   }

   protected void a(AnimationProcessor<T> var1) {
      this.c(var1, false);
      this.b(var1, false);
      this.d(var1, false);
      this.a(var1, false);
   }

   void c(AnimationProcessor var1, boolean var2) {
      this.a(this.c(), var2, var1);
      this.a(this.g(), !var2, var1);
   }

   void b(AnimationProcessor<T> var1, boolean var2) {
      this.a(this.f(), var2, var1);
      this.a(this.a(), !var2, var1);
   }

   void d(AnimationProcessor<T> var1, boolean var2) {
      this.a(this.h(), var2, var1);
      this.a(this.e(), !var2, var1);
   }

   void a(AnimationProcessor<T> var1, boolean var2) {
      this.a(this.b(), var2, var1);
      this.a(this.d(), !var2, var1);
   }

   void a(String[] var1, boolean var2, AnimationProcessor<T> var3) {
      for (String var7 : var1) {
         this.a(var7, var2, var3);
      }
   }

   void a(String var1, boolean var2, AnimationProcessor<T> var3) {
      if (var3.getBone(var1) != null) {
         var3.getBone(var1).setHidden(!var2);
      }
   }

   protected boolean f_clash312(T var1) {
      UUID var2 = var1.getInteractionPlayerUUID();
      if (var2 == null) {
         return true;
      }

      World var3 = var1.world;
      AbstractClientPlayer var4 = (AbstractClientPlayer)var3.getPlayerEntityByUUID(var2);
      return var4 == null ? true : "default".equals(var4.getSkinType());
   }

   void a(T var1, AnimationProcessor<T> var2) {
      boolean var3 = this.f_clash312((T)var1);
      if (var3) {
         var2.getBone("rightArmAlex").setHidden(var3);
         var2.getBone("rightLowerArmAlex").setHidden(var3);
         IBone var10 = var2.getBone("rightArmSteve");
         var10.setHidden(false);
         IBone var11 = var2.getBone("rightLowerArmSteve");
         var11.setHidden(false);
         var2.getBone("leftArmAlex").setHidden(var3);
         var2.getBone("leftLowerArmAlex").setHidden(var3);
         IBone var12 = var2.getBone("leftArmSteve");
         var12.setHidden(false);
         IBone var13 = var2.getBone("leftLowerArmSteve");
         var13.setHidden(false);
         IBone var9 = var2.getBone("steve");
         if (var9 != null) {
            var9.setHidden(!var1.getCurrentAction().hasPlayer);
         }
      } else {
         var2.getBone("rightArmAlex").setHidden(var3);
         var2.getBone("rightLowerArmAlex").setHidden(var3);
         IBone var5 = var2.getBone("rightArmSteve");
         var5.setHidden(true);
         IBone var6 = var2.getBone("rightLowerArmSteve");
         var6.setHidden(true);
         var2.getBone("leftArmAlex").setHidden(var3);
         var2.getBone("leftLowerArmAlex").setHidden(var3);
         IBone var7 = var2.getBone("leftArmSteve");
         var7.setHidden(true);
         IBone var8 = var2.getBone("leftLowerArmSteve");
         var8.setHidden(true);
         IBone var4 = var2.getBone("steve");
         if (var4 != null) {
            var4.setHidden(!var1.getCurrentAction().hasPlayer);
         }
      }
   }

   protected boolean e_clash170(T var1) {
      return true;
   }

   protected void a(T var1, AnimationProcessor<T> var2, AnimationEvent var3) {
      if (!(var1.world instanceof SexWorldClient)) {
         if (this.e_clash170(var1)) {
            if (var1.getCurrentAction() == Action.NULL || var1.getCurrentAction() == Action.ATTACK || var1.getCurrentAction() == Action.BOW) {
               EntityModelData var4 = (EntityModelData) var3.getExtraDataOfType(EntityModelData.class).get(0);
               IBone var5 = var2.getBone("neck");
               var5.setRotationY(var4.netHeadYaw * 0.5F * (float) (Math.PI / 180.0));
               IBone var6 = var2.getBone("head");
               var6.setRotationY(var4.netHeadYaw * (float) (Math.PI / 180.0));
               var6.setRotationX(var4.headPitch * (float) (Math.PI / 180.0));
               IBone var7 = var2.getBone("body") == null ? var2.getBone("dd") : var2.getBone("body");
               var7.setRotationY(0.0F);
            }
         }
      }
   }

   public ItemStack a_clash348(BaseGirlEntity var1, String var2) {
      if (Arrays.asList(this.c()).contains(var2)) {
         return (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.X);
      } else if (Arrays.asList(this.f()).contains(var2)) {
         return (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.T);
      } else if (Arrays.asList(this.h()).contains(var2)) {
         return (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.U);
      } else {
         return Arrays.asList(this.b()).contains(var2) ? (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.W) : ItemStack.EMPTY;
      }
   }

}
