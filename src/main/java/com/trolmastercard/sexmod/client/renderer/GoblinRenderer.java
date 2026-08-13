package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.api.by;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GoblinEntity;
import com.trolmastercard.sexmod.entity.GoblinPlayerEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.eh;
import com.trolmastercard.sexmod.util.g5;
import com.trolmastercard.sexmod.util.gc;







import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.vecmath.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GoblinRenderer extends GirlRendererBase<GoblinEntity> {
   public static final Vec3i w = new Vec3i(255, 255, 255);
   static final float K = -420.69F;
   static final float A = 8.0F;
   static final float L = 3.0F;
   public static final Vec3d G = new Vec3d(10.0, -20.0, -10.0);
   static final float J = 0.1F;
   public static final HashSet<String> D = new HashSet<>(
      Arrays.asList(
         "meatTorso",
         "meatCheekR",
         "meatCheekL",
         "meatFootR",
         "meatFootL",
         "meatShinR",
         "meatShinL",
         "meatLegL",
         "meatLegR",
         "nippleR",
         "nippleL",
         "preggy",
         "shoeL",
         "shoeR",
         "frontAndInside",
         "Lside",
         "Rside",
         "cheekR",
         "cheekL",
         "fuckhole",
         "head",
         "nose",
         "neck",
         "armL",
         "lowerArmL",
         "armR",
         "lowerArmR",
         "torso",
         "LegL",
         "LegR",
         "shinL",
         "shinR"
      )
   );
   public static final HashSet<String> M = new HashSet<>(Arrays.asList("lashR", "lashL", "closedR", "closedL", "browL", "browR", "closedL", "closedL"));
   static final HashSet<String> C = new HashSet<>(Arrays.asList("meatLegR", "meatShinR", "meatFootR", "boobR", "boobR1", "boobR2"));
   static Minecraft y;
   float v = 0.0F;
   boolean u = false;
   boolean F = false;
   public static float B = 0.0F;
   float z = 0.0F;
   public static float H = 0.0F;
   public static float t = 0.0F;
   public static float I = 0.0F;
   public static float E = 0.0F;
   public static float N = 0.0F;
   public static float x = 0.0F;

   public GoblinRenderer(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
      y = Minecraft.getMinecraft();
   }

   protected ResourceLocation a(GoblinEntity var1) {
      UUID var3 = var1.getInteractionPlayerUUID();
      if (var3 == null) {
         var3 = var1.getOwnerUUID();
      }

      ResourceLocation var2;
      if (!(var1.world instanceof SexWorldClient) && var3 != null) {
         var2 = l.get(var3);
         if (var2 == null) {
            return this.a_clash329(var3, var1.world);
         }
      } else {
         var2 = l.get(y.getSession().getProfile().getId());
         if (var2 == null) {
            return this.a_clash329(y.getSession().getProfile().getId(), var1.world);
         }
      }

      return var2;
   }

   public static void a_clash398(BaseGirlEntity var0, float var1) {
      y.getRenderManager().renderEntity(var0, 0.0, 0.0, 0.0, -420.69F, var1, false);
   }

   public static void a_clash399(float var0) {
      if (y.getRenderViewEntity() instanceof EntityPlayer) {
         EntityPlayer var1 = (EntityPlayer)y.getRenderViewEntity();
         float var2 = var1.distanceWalkedModified - var1.prevDistanceWalkedModified;
         float var3 = -(var1.distanceWalkedModified + var2 * var0);
         float var4 = var1.prevCameraYaw + (var1.cameraYaw - var1.prevCameraYaw) * var0;
         float var5 = MathHelper.sin(var3 * (float) Math.PI) * var4 * 0.5F;
         GlStateManager.translate(
            Math.cos(y.player.rotationYaw * (Math.PI / 180.0)) * var5,
            Math.abs(MathHelper.cos(var3 * (float) Math.PI) * var4),
            Math.sin(y.player.rotationYaw * (Math.PI / 180.0)) * var5
         );
      }
   }

   public void a(GeoModel var1, GoblinEntity var2, float var3, float var4, float var5, float var6, float var7) {
      super.a(var1, var2, var3, var4, var5, var6, var2.ar);
   }

   public void doRenderShadowAndFire(Entity var1, double var2, double var4, double var6, float var8, float var9) {
      if (!(var1 instanceof GoblinEntity)) {
         super.doRenderShadowAndFire(var1, var2, var4, var6, var8, var9);
      } else {
         GoblinEntity var10 = (GoblinEntity)var1;
         if (var10.getCurrentAction() != fp.PICK_UP && var10.getCurrentAction() != fp.SHOULDER_IDLE) {
            super.doRenderShadowAndFire(var1, var2, var4, var6, var8, var9);
         }
      }
   }

   public static Vec3d a(World var0, BaseGirlEntity var1, UUID var2, double var3, double var5, double var7) {
      if (var0 == null) {
         return new Vec3d(var3, var5, var7);
      }

      if (var2 == null) {
         return new Vec3d(var3, var5, var7);
      }

      if (var1 == null) {
         return new Vec3d(var3, var5, var7);
      }

      EntityPlayer var9 = var0.getPlayerEntityByUUID(var2);
      if (var9 == null) {
         return new Vec3d(var3, var5, var7);
      }

      Vec3d var10 = var9.getPositionVector();
      Vec3d var11 = y.player.getPositionVector();
      var1.prevRenderYawOffset = var9.prevRotationYawHead;
      var1.renderYawOffset = var9.rotationYawHead;
      var1.setCurrentAction(fp.START_THROWING);
      return var10.subtract(var11);
   }

   public void a(GoblinEntity var1, double var2, double var4, double var6, float var8, float var9) {
      this.j = var1;
      this.u = -420.69F == var8 && var1.getCurrentAction() == fp.SHOULDER_IDLE;
      this.F = -420.69F == var8 && var1.getCurrentAction() == fp.PICK_UP;
      this.z = var1.world.getLight(var1.getPosition(), true);
      this.v = var9;
      B = var8;
      fp var10 = var1.getCurrentAction();
      UUID var11 = var1.getOwnerUUID();
      if (var11 != null) {
         if (var1.isLocallyRegistered()) {
            Vec3d var19 = a(var1.world, var1, var11, var2, var4, var6);
            var2 = var19.x;
            var4 = var19.y;
            var6 = var19.z;
         }

         if (var10 == fp.THROWN || var10 == fp.START_THROWING) {
            if (y.gameSettings.thirdPersonView == 0 && var8 == -420.69F && !var1.isLocallyRegistered()) {
               return;
            }

            if (!var1.isLocallyRegistered()) {
               float var20 = var1.getYawRotation();
               var1.prevRenderYawOffset = var20;
               var1.renderYawOffset = var20;
            }
         }

         if (a(var1, var10)) {
            if (y.player.getPersistentID().equals(var11)) {
               if (-420.69F != var8) {
                  return;
               }

               var1.renderYawOffset = y.player.rotationYaw + 180.0F;
               var1.prevRenderYawOffset = y.player.rotationYaw + 180.0F;
               Vec3d var21 = y.player.getLookVec();
               GlStateManager.pushMatrix();
               GlStateManager.translate(var21.x, var21.y + y.player.getEyeHeight(), var21.z);
               Vec3d var28 = GoblinEntity.b(new Vec3d(-Math.abs(y.player.rotationPitch), 0.0, 0.0), y.player.rotationYaw);
               GlStateManager.rotate(y.player.rotationPitch, (float)var28.x, 0.0F, (float)var28.z);
               var2 = 0.0;
               var4 = 0.0;
               var6 = 0.0;
            } else {
               if (!var1.isLocallyRegistered() || y.player.getPersistentID().equals(var11)) {
                  if (!y.player.getPersistentID().equals(var11)) {
                     EntityPlayer var22 = var1.world.getPlayerEntityByUUID(var11);
                     if (var22 != null) {
                        var1.renderYawOffset = var22.rotationYaw;
                        var1.prevRenderYawOffset = var22.rotationYaw;
                     }
                  } else {
                     var1.renderYawOffset = y.player.rotationYaw;
                     var1.prevRenderYawOffset = y.player.rotationYaw;
                  }
               }

               Vec3d var23 = a(var1, var1.getOwnerUUID(), var9);
               var2 = var23.x;
               var4 = var23.y;
               var6 = var23.z;
            }
         } else if (this.u) {
            a_clash399(var9);
            Vec3d var24 = new Vec3d(RotationHelper.lerp(-0.1F, 0.2F, y.gameSettings.fovSetting / 110.0F), 0.0, 0.0);
            var24 = GoblinEntity.b(var24, y.player.rotationYaw);
            var2 = var24.x;
            var4 = var24.y;
            var6 = var24.z;
            var1.renderYawOffset = y.player.rotationYaw;
            var1.prevRenderYawOffset = y.player.prevRotationYaw;
            if (y.player.isSneaking()) {
               var4 -= 0.075;
            }
         } else if (var10 == fp.SHOULDER_IDLE) {
            if (y.player.getPersistentID().equals(var11) && y.gameSettings.thirdPersonView == 0) {
               return;
            }

            EntityPlayer var26 = var1.world.getPlayerEntityByUUID(var11);
            if (var26 == null) {
               return;
            }

            Vector4f var29 = a_clash400(var26, var9);
            var2 = var29.x;
            var4 = var29.y;
            var6 = var29.z;
            var1.renderYawOffset = var29.w;
            if (var26.isSneaking()) {
               var4 -= 0.32;
            }
         } else if (var10 == fp.PICK_UP) {
            EntityPlayer var27 = var1.world.getPlayerEntityByUUID(var11);
            if (var27 != null) {
               var1.prevRenderYawOffset = var27.prevRotationYawHead;
               var1.renderYawOffset = var27.rotationYawHead;
            }
         }

         super.a(var1, var2, var4, var6, var8, var9);
         if (a(var1, var10) && y.gameSettings.thirdPersonView == 0 && y.player.getPersistentID().equals(var11)) {
            GlStateManager.popMatrix();
         }
      } else {
         if (var1.isLocallyRegistered()) {
            Vec3d var12 = a(var1.world, var1, var11, var2, var4, var6);
            var2 = var12.x;
            var4 = var12.y;
            var6 = var12.z;
         }

         if (var10 == fp.THROWN || var10 == fp.START_THROWING) {
            if (y.gameSettings.thirdPersonView == 0 && var8 == -420.69F && !var1.isLocallyRegistered()) {
               return;
            }

            if (!var1.isLocallyRegistered()) {
               float var14 = var1.getYawRotation();
               var1.prevRenderYawOffset = var14;
               var1.renderYawOffset = var14;
            }
         }

         if (a(var1, var10)) {
            if (y.player.getPersistentID().equals(var11)) {
               if (-420.69F != var8) {
                  return;
               }

               var1.renderYawOffset = y.player.rotationYaw + 180.0F;
               var1.prevRenderYawOffset = y.player.rotationYaw + 180.0F;
               Vec3d var15 = y.player.getLookVec();
               GlStateManager.pushMatrix();
               GlStateManager.translate(var15.x, var15.y + y.player.getEyeHeight(), var15.z);
               Vec3d var13 = GoblinEntity.b(new Vec3d(-Math.abs(y.player.rotationPitch), 0.0, 0.0), y.player.rotationYaw);
               GlStateManager.rotate(y.player.rotationPitch, (float)var13.x, 0.0F, (float)var13.z);
               var2 = 0.0;
               var4 = 0.0;
               var6 = 0.0;
            } else {
               if (var1.isLocallyRegistered()) {
               }

               var1.renderYawOffset = y.player.rotationYaw;
               var1.prevRenderYawOffset = y.player.rotationYaw;
               Vec3d var16 = a(var1, var1.getOwnerUUID(), var9);
               var2 = var16.x;
               var4 = var16.y;
               var6 = var16.z;
            }
         } else if (this.u) {
            a_clash399(var9);
            Vec3d var17 = new Vec3d(RotationHelper.lerp(-0.1F, 0.2F, y.gameSettings.fovSetting / 110.0F), 0.0, 0.0);
            var17 = GoblinEntity.b(var17, y.player.rotationYaw);
            var2 = var17.x;
            var4 = var17.y;
            var6 = var17.z;
            var1.renderYawOffset = y.player.rotationYaw;
            var1.prevRenderYawOffset = y.player.prevRotationYaw;
            if (y.player.isSneaking()) {
               var4 -= 0.075;
            }
         } else {
            if (var10 == fp.SHOULDER_IDLE) {
               return;
            }

            if (var10 == fp.PICK_UP) {
            }
         }

         super.a(var1, var2, var4, var6, var8, var9);
         if (a(var1, var10) && y.gameSettings.thirdPersonView == 0 && y.player.getPersistentID().equals(var11)) {
            GlStateManager.popMatrix();
         }
      }
   }

   public static boolean a(BaseGirlEntity var0, fp var1) {
      if (var1 == fp.START_THROWING && !var0.isLocallyRegistered()) {
         return false;
      }

      if (y.gameSettings.thirdPersonView == 0 || var1 != fp.START_THROWING && var1 != fp.PICK_UP) {
         switch (var1) {
            case PICK_UP:
            case CATCH:
            case CATCH_BJ:
            case CATCH_BJ_IDLE:
            case START_THROWING:
               return true;
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public static Vec3d a(BaseGirlEntity var0, UUID var1, float var2) {
      if (var1 == null) {
         return Vec3d.ZERO;
      }

      EntityPlayer var3 = var0.world.getPlayerEntityByUUID(var1);
      if (var3 == null) {
         return Vec3d.ZERO;
      }

      Vec3d var4 = RotationHelper.a(new Vec3d(var3.prevPosX, var3.prevPosY, var3.prevPosZ), var3.getPositionVector(), var2);
      Vec3d var5 = RotationHelper.a(
         new Vec3d(y.player.prevPosX, y.player.prevPosY, y.player.prevPosZ), y.player.getPositionVector(), var2
      );
      return var4.subtract(var5);
   }

   public static Vector4f a_clash400(EntityPlayer var0, float var1) {
      EntityPlayerSP var2 = y.player;
      float var3 = RotationHelper.lerp(var0.prevRenderYawOffset, var0.renderYawOffset, var1);
      Vec3d var4 = RotationHelper.a(new Vec3d(var0.lastTickPosX, var0.lastTickPosY, var0.lastTickPosZ), var0.getPositionVector(), var1);
      Vec3d var5 = RotationHelper.a(new Vec3d(var2.lastTickPosX, var2.lastTickPosY, var2.lastTickPosZ), var2.getPositionVector(), var1);
      Vec3d var6 = var4.subtract(var5);
      return new Vector4f((float)var6.x, (float)var6.y, (float)var6.z, var3);
   }

   @Override
   protected Vec3i getBoneColor(String var1) {
      String[] var2 = AbstractNpcOnlyEntity.getModelCodeParts(this.j);
      if (var2.length < 8) {
         return r;
      } else if (var1.contains("band")) {
         return w;
      } else if (var1.contains("eyeColor") || var1.contains("eyeColor2")) {
         return b_clash401(var2[8]);
      } else if (var1.contains("variant") || var1.contains("boob")) {
         return c_clash402(var2[7]);
      } else if (var1.contains("hair")) {
         return d_clash403(var2[6]);
      } else if (D.contains(var1)) {
         return c_clash402(var2[7]);
      } else {
         return M.contains(var1) ? d_clash403(var2[6]) : r;
      }
   }

   public static Vec3i b_clash401(String var0) {
      return eh.values()[Integer.parseInt(var0)].a_clash565();
   }

   public static Vec3i c_clash402(String var0) {
      return by.values()[Integer.parseInt(var0)].a_clash189();
   }

   public static Vec3i d_clash403(String var0) {
      return g5.values()[Integer.parseInt(var0)].a_clash448();
   }

   @Override
   protected void a(BufferBuilder var1, String var2, GeoBone var3) {
      if (!(this.j.world instanceof SexWorldClient)) {
         String[] var4 = AbstractNpcOnlyEntity.getModelCodeParts(this.j);
         if (var4.length >= 8) {
            switch (var2) {
               case "earL":
                  a(var3, var4[0], var4[1], var4[3]);
                  break;
               case "earR":
                  a(var3, var4[0], var4[2], var4[4]);
                  break;
               case "hair":
                  a(var3, var4[5]);
                  break;
               case "body":
                  var3.setPivotY(-0.15F);
                  a(this.j, var3);
                  break;
               case "LegR":
                  a(this.u, var3, 25.0F, 25.0F);
                  break;
               case "boobR":
                  a(this.u, var3, 30.0F, 30.0F);
                  break;
               case "boobR1":
                  a(this.u, var3, 10.0F, 15.0F);
                  break;
               case "boobR2":
                  a(this.u, var3, 5.0F, 3.0F);
            }

            if (var2.contains("crown")) {
               a(this.j, var3, var4[9]);
            }
         }
      }
   }

   public static void a(BaseGirlEntity var0, GeoBone var1, String var2) {
      if (var0.isLocallyRegistered()) {
         var1.setHidden(true);
      } else if (var0 instanceof GoblinEntity) {
         int var3 = Integer.parseInt(var2);
         var1.setHidden(var3 == 0);
      } else if (var0 instanceof GoblinPlayerEntity) {
         var1.setHidden(((ItemStack)var0.getDataManager().get(AbstractGirlNpcEntity.X)).isEmpty());
      }
   }

   public static void a(boolean var0, GeoBone var1, float var2, float var3) {
      if (!y.isGamePaused()) {
         if (var0) {
            var1.setRotationX(var1.getRotationX() + gc.wrapDegrees(ThreadNames.b(x, -var2, var2)));
            var1.setRotationZ(var1.getRotationZ() + gc.wrapDegrees(ThreadNames.b(N, -var3, var3)));
         }
      }
   }

   public static void a(BaseGirlEntity var0, GeoBone var1) {
      if (B == -420.69F && var0.getCurrentAction() == fp.SHOULDER_IDLE) {
         float var2 = -y.getRenderManager().playerViewX;
         var1.setPivotY(8.0F);
         if (!y.isGamePaused()) {
            var1.setRotationX(var1.getRotationX() + gc.wrapDegrees(var2));
         }
      }
   }

   public static void a(GeoBone var0, String var1) {
      int var2 = Integer.parseInt(var1);
      a(var0, var2);
   }

   static HashSet<Integer> b(int var0, String var1) {
      int var2 = Integer.parseInt(var1);
      int var3 = var0 - 1;
      ArrayList var4 = a_clash404(var3);

      while (var2 >= var4.size()) {
         var2 -= var4.size();
      }

      return (HashSet<Integer>)var4.get(var2);
   }

   static ArrayList<HashSet<Integer>> a_clash404(int var0) {
      ArrayList var1 = new ArrayList();
      a(0, new HashSet<>(), var0, var1);
      return var1;
   }

   static void a(int var0, HashSet<Integer> var1, int var2, ArrayList<HashSet<Integer>> var3) {
      if (var0 > var2) {
         var3.add(var1);
      } else {
         HashSet var4 = new HashSet(var1);
         a(var0 + 1, var1, var2, var3);
         var4.add(var0);
         a(var0 + 1, var4, var2, var3);
      }
   }

   static HashSet<Integer> a(int var0, String var1) {
      HashSet var2 = new HashSet();
      int var3 = Integer.parseInt(var1);
      var3 = (int)(0.01F * var3 * var3);
      int var4 = Math.round(var3 / 100.0F * var0);
      Random var5 = new Random(var3);

      for (int var6 = 0; var6 < var4; var6++) {
         int var7 = var5.nextInt(var0);
         if (!var2.contains(var7)) {
            var2.add(var7);
         } else {
            var6--;
         }
      }

      return var2;
   }

   public static void a(GeoBone var0, String var1, String var2, String var3) {
      GeoBone var4 = a(var0, Integer.parseInt(var1));
      GeoBone var5 = a(var4, Integer.parseInt(var2));
      List var6 = var5.childBones;
      int var7 = var6.size();
      HashSet<Integer> var8 = b(var7, var3);
      var5.childBones.forEach(var0x -> var0x.setHidden(true));
      var8.forEach(var1x -> b(var5, var1x));
   }

   @Override
   protected Vec3i a_clash219(Vec3i var1) {
      if (!this.u && !this.F) {
         return var1;
      }

      float var2 = ThreadNames.b(this.z, 2.0F, 15.0F) / 15.0F;
      return new Vec3i(var1.getX() * var2, var1.getY() * var2, var1.getZ() * var2);
   }

   @Override
   protected ItemStack a_clash341(@Nullable ItemStack var1) {
      fp var2 = this.j.getCurrentAction();
      return var2 != fp.RUN && var2 != fp.CATCH ? var1 : (ItemStack)this.j.getDataManager().get(GoblinEntity.a0);
   }

   @Override
   public HashSet<String> a() {
      return new HashSet<String>() {
         {
            this.add("boobs");
            this.add("booty");
            this.add("vagina");
            this.add("fuckhole");
            this.add("preggy");
            this.add("LegL");
            this.add("LegR");
            this.add("cheekR");
            this.add("cheekL");
         }
      };
   }

   @Override
   protected float a_clash217() {
      return this.j.getCurrentAction() == fp.CATCH ? 0.5F : 1.0F;
   }

   @Override
   protected Vec3d a_clash218(ItemStack var1) {
      if (var1 == null) {
         return Vec3d.ZERO;
      } else {
         return !(var1.getItem() instanceof ItemBlock) && var1.getMaxStackSize() != 1 ? new Vec3d(180.0, 0.0, 0.0) : super.a_clash218(var1);
      }
   }

   @Override
   public void a(BufferBuilder var1, GeoCube var2, GeoBone var3, float var4, float var5, float var6, float var7, double var8) {
      if (!this.u || C.contains(var3.getName())) {
         if (!this.p.contains(var3.getName())) {
            this.q = var3;
            super.a(var1, var2, var3, var4, var5, var6, var7, var8);
         }
      }
   }

}
