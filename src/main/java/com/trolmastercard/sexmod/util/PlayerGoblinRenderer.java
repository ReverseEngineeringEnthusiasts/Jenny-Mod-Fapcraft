package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GoblinEntity;
import com.trolmastercard.sexmod.entity.GoblinPlayerEntity;
import com.trolmastercard.sexmod.entity.Action;







import java.util.HashSet;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.vecmath.Vector4f;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PlayerGoblinRenderer extends AbstractPlayerKoblinGoboldRenderer {
   GoblinPlayerEntity B = null;
   boolean C = false;
   boolean E = false;
   boolean D = false;

   public PlayerGoblinRenderer(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   @Override
   protected Vec3i resolveBoneColor(String var1) {
      String[] var2 = AbstractNpcOnlyEntity.getModelCodeParts(this.j);
      if (var2.length < 8) {
         return z;
      } else if (var1.contains("band")) {
         return GoblinRenderer.w;
      } else if (var1.contains("eyeColor") || var1.contains("eyeColor2")) {
         return GoblinRenderer.b_clash401(var2[8]);
      } else if (var1.contains("variant") || var1.contains("boob")) {
         return GoblinRenderer.c_clash402(var2[7]);
      } else if (var1.contains("hair")) {
         return GoblinRenderer.d_clash403(var2[6]);
      } else if (GoblinRenderer.D.contains(var1)) {
         return GoblinRenderer.c_clash402(var2[7]);
      } else {
         return GoblinRenderer.M.contains(var1) ? GoblinRenderer.d_clash403(var2[6]) : z;
      }
   }

   @Override
   protected Vector4f calculateBoneArmorColor(String var1, float var2, float var3, float var4) {
      if (var1.startsWith("crown")) {
         ItemStack var5 = (ItemStack)this.j.getDataManager().get(AbstractGirlNpcEntity.X);
         if (var5.isEmpty()) {
            return super.calculateBoneArmorColor(var1, var2, var3, var4);
         }

         ItemArmor var6 = (ItemArmor)var5.getItem();
         ArmorMaterial var7 = var6.getArmorMaterial();
         float var8 = 0.0F;
         switch (var7) {
            case GOLD:
               var8 = 1.0F;
               break;
            case CHAIN:
            case IRON:
               var8 = 2.0F;
               break;
            case LEATHER:
               var8 = 4.0F;
               int var9 = var6.getColor(var5);
               float var10 = (var9 >> 16 & 0xFF) / 255.0F;
               float var11 = (var9 >> 8 & 0xFF) / 255.0F;
               float var12 = (var9 & 0xFF) / 255.0F;
               var2 = var10;
               var3 = var11;
               var4 = var12;
         }

         return new Vector4f(var2, var3, var4, 72.0F * var8 / 4096.0F);
      } else {
         return super.calculateBoneArmorColor(var1, var2, var3, var4);
      }
   }

   @Override
   protected boolean isBoneBlacklisted(String var1) {
      return var1.startsWith("crown") ? true : super.isBoneBlacklisted(var1);
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
   protected void onBoneRenderStart(String var1, GeoBone var2) {
      String[] var3 = AbstractNpcOnlyEntity.getModelCodeParts(this.j);
      if (var3.length >= 8) {
         switch (var1) {
            case "earL":
               GoblinRenderer.a(var2, var3[0], var3[1], var3[3]);
               break;
            case "earR":
               GoblinRenderer.a(var2, var3[0], var3[2], var3[4]);
               break;
            case "hair":
               GoblinRenderer.a(var2, var3[5]);
               break;
            case "body":
               var2.setPivotY(-0.15F);
               GoblinRenderer.a(this.j, var2);
               break;
            case "LegR":
               GoblinRenderer.a(this.C, var2, 25.0F, 25.0F);
               break;
            case "boobR":
               GoblinRenderer.a(this.C, var2, 30.0F, 30.0F);
               break;
            case "boobR1":
               GoblinRenderer.a(this.C, var2, 10.0F, 15.0F);
               break;
            case "boobR2":
               GoblinRenderer.a(this.C, var2, 5.0F, 3.0F);
         }

         if (var1.contains("crown")) {
            GoblinRenderer.a(this.j, var2, var3[9]);
         }
      }
   }

   @Override
   public void a(BaseGirlEntity var1, double var2, double var4, double var6, float var8, float var9) {
      this.D = v;
      this.B = (GoblinPlayerEntity)var1;
      this.C = -420.69F == var8 && var1.getCurrentAction() == Action.SHOULDER_IDLE;
      this.E = -420.69F == var8 && var1.getCurrentAction() == Action.PICK_UP;
      this.y = var9;
      GoblinRenderer.B = var8;
      Action var10 = var1.getCurrentAction();
      UUID var11 = this.B.getOwnerUUID();
      if (var11 != null) {
         if (var1.isLocallyRegistered()) {
            Vec3d var19 = GoblinRenderer.a(var1.world, var1, var11, var2, var4, var6);
            var2 = var19.x;
            var4 = var19.y;
            var6 = var19.z;
         }

         if (var10 == Action.THROWN || var10 == Action.START_THROWING) {
            if (i.gameSettings.thirdPersonView == 0 && var8 == -420.69F && !var1.isLocallyRegistered()) {
               return;
            }

            if (!var1.isLocallyRegistered()) {
               float var20 = var1.getYawRotation();
               var1.prevRenderYawOffset = var20;
               var1.renderYawOffset = var20;
            }
         }

         if (GoblinRenderer.a(var1, var10)) {
            if (i.player.getPersistentID().equals(var11)) {
               if (-420.69F != var8) {
                  return;
               }

               var1.renderYawOffset = i.player.rotationYaw + 180.0F;
               var1.prevRenderYawOffset = i.player.rotationYaw + 180.0F;
               Vec3d var21 = i.player.getLookVec();
               GlStateManager.pushMatrix();
               GlStateManager.translate(var21.x, var21.y + i.player.getEyeHeight(), var21.z);
               Vec3d var28 = GoblinEntity.b(new Vec3d(-Math.abs(i.player.rotationPitch), 0.0, 0.0), i.player.rotationYaw);
               GlStateManager.rotate(i.player.rotationPitch, (float)var28.x, 0.0F, (float)var28.z);
               var2 = 0.0;
               var4 = 0.0;
               var6 = 0.0;
            } else if (!this.B.getOwnerUserUUID().equals(i.player.getPersistentID())) {
               if (!var1.isLocallyRegistered() || i.player.getPersistentID().equals(var11)) {
                  if (!i.player.getPersistentID().equals(var11)) {
                     EntityPlayer var22 = var1.world.getPlayerEntityByUUID(var11);
                     if (var22 != null) {
                        var1.renderYawOffset = var22.rotationYaw;
                        var1.prevRenderYawOffset = var22.rotationYaw;
                     }
                  } else {
                     var1.renderYawOffset = i.player.rotationYaw;
                     var1.prevRenderYawOffset = i.player.rotationYaw;
                  }
               }

               Vec3d var23 = GoblinRenderer.a(var1, this.B.getOwnerUUID(), var9);
               var2 = var23.x;
               var4 = var23.y;
               var6 = var23.z;
            }
         } else if (this.C) {
            GoblinRenderer.a_clash399(var9);
            Vec3d var24 = new Vec3d(RotationHelper.lerp(-0.1F, 0.2F, i.gameSettings.fovSetting / 110.0F), 0.0, 0.0);
            var24 = GoblinEntity.b(var24, i.player.rotationYaw);
            var2 = var24.x;
            var4 = var24.y;
            var6 = var24.z;
            var1.renderYawOffset = i.player.rotationYaw;
            var1.prevRenderYawOffset = i.player.prevRotationYaw;
            if (i.player.isSneaking()) {
               var4 -= 0.075;
            }
         } else if (var10 == Action.SHOULDER_IDLE) {
            if (i.player.getPersistentID().equals(var11) && i.gameSettings.thirdPersonView == 0) {
               return;
            }

            EntityPlayer var26 = var1.world.getPlayerEntityByUUID(var11);
            if (var26 == null) {
               return;
            }

            Vector4f var29 = GoblinRenderer.a_clash400(var26, var9);
            var2 = var29.x;
            var4 = var29.y;
            var6 = var29.z;
            var1.renderYawOffset = var29.w;
            if (var26.isSneaking()) {
               var4 -= 0.32;
            }
         } else if (var10 == Action.PICK_UP) {
            EntityPlayer var27 = var1.world.getPlayerEntityByUUID(var11);
            if (var27 != null) {
               var1.prevRenderYawOffset = var27.prevRotationYawHead;
               var1.renderYawOffset = var27.rotationYawHead;
            }
         }

         super.a(var1, (double)var2, (double)var4, (double)var6, var8, var9);
         if (GoblinRenderer.a(var1, var10) && i.gameSettings.thirdPersonView == 0 && i.player.getPersistentID().equals(var11)) {
            GlStateManager.popMatrix();
         }
      } else {
         if (var1.isLocallyRegistered()) {
            Vec3d var12 = GoblinRenderer.a(var1.world, var1, var11, var2, var4, var6);
            var2 = var12.x;
            var4 = var12.y;
            var6 = var12.z;
         }

         if (var10 == Action.THROWN || var10 == Action.START_THROWING) {
            if (i.gameSettings.thirdPersonView == 0 && var8 == -420.69F && !var1.isLocallyRegistered()) {
               return;
            }

            if (!var1.isLocallyRegistered()) {
               float var14 = var1.getYawRotation();
               var1.prevRenderYawOffset = var14;
               var1.renderYawOffset = var14;
            }
         }

         if (GoblinRenderer.a(var1, var10)) {
            if (i.player.getPersistentID().equals(var11)) {
               if (-420.69F != var8) {
                  return;
               }

               var1.renderYawOffset = i.player.rotationYaw + 180.0F;
               var1.prevRenderYawOffset = i.player.rotationYaw + 180.0F;
               Vec3d var15 = i.player.getLookVec();
               GlStateManager.pushMatrix();
               GlStateManager.translate(var15.x, var15.y + i.player.getEyeHeight(), var15.z);
               Vec3d var13 = GoblinEntity.b(new Vec3d(-Math.abs(i.player.rotationPitch), 0.0, 0.0), i.player.rotationYaw);
               GlStateManager.rotate(i.player.rotationPitch, (float)var13.x, 0.0F, (float)var13.z);
               var2 = 0.0;
               var4 = 0.0;
               var6 = 0.0;
            } else if (!this.B.getOwnerUserUUID().equals(i.player.getPersistentID())) {
               if (var1.isLocallyRegistered()) {
               }

               var1.renderYawOffset = i.player.rotationYaw;
               var1.prevRenderYawOffset = i.player.rotationYaw;
               Vec3d var16 = GoblinRenderer.a(var1, this.B.getOwnerUUID(), var9);
               var2 = var16.x;
               var4 = var16.y;
               var6 = var16.z;
            }
         } else if (this.C) {
            GoblinRenderer.a_clash399(var9);
            Vec3d var17 = new Vec3d(RotationHelper.lerp(-0.1F, 0.2F, i.gameSettings.fovSetting / 110.0F), 0.0, 0.0);
            var17 = GoblinEntity.b(var17, i.player.rotationYaw);
            var2 = var17.x;
            var4 = var17.y;
            var6 = var17.z;
            var1.renderYawOffset = i.player.rotationYaw;
            var1.prevRenderYawOffset = i.player.prevRotationYaw;
            if (i.player.isSneaking()) {
               var4 -= 0.075;
            }
         } else {
            if (var10 == Action.SHOULDER_IDLE) {
               return;
            }

            if (var10 == Action.PICK_UP) {
            }
         }

         super.a(var1, (double)var2, (double)var4, (double)var6, var8, var9);
         if (GoblinRenderer.a(var1, var10) && i.gameSettings.thirdPersonView == 0 && i.player.getPersistentID().equals(var11)) {
            GlStateManager.popMatrix();
         }
      }
   }

   @Override
   protected void drawOverlayLines(Tessellator var1, BufferBuilder var2, BaseGirlEntity var3, Vector3fSexmodSpecial var4, float var5) {
      a(var1, var2, var3, var4, var5);
   }

   @Nullable
   @Override
   protected Vector3fSexmodSpecial getAdditionalOverlayColor(BaseGirlEntity var1) {
      if (!this.D) {
         return null;
      }

      if (!(var1 instanceof GoblinPlayerEntity)) {
         return null;
      }

      GoblinPlayerEntity var2 = (GoblinPlayerEntity)var1;
      UUID var3 = var2.getOwnerUserUUID();
      EntityPlayerSP var4 = i.player;
      if (var3 != null && (i.gameSettings.thirdPersonView != 0 || !var4.getPersistentID().equals(var3))) {
         EntityPlayer var5 = var2.k_clash584();
         if (var5 == null) {
            return null;
         }

         ItemStack var6 = (ItemStack)var2.getDataManager().get(AbstractGirlNpcEntity.T);
         if (var6.isEmpty()) {
            return null;
         }

         if (!(var6.getItem() instanceof ItemArmor)) {
            return null;
         }

         ItemArmor var7 = (ItemArmor)var6.getItem();
         switch (var7.getArmorMaterial()) {
            case GOLD:
               return new Vector3fSexmodSpecial(99.0F, 98.0F, 14.0F);
            case CHAIN:
            case IRON:
               return new Vector3fSexmodSpecial(85.0F, 85.0F, 85.0F);
            case LEATHER:
               int var8 = var7.getColor(var6);
               float var9 = var8 >> 16 & 0xFF;
               float var10 = var8 >> 8 & 0xFF;
               float var11 = var8 & 0xFF;
               return new Vector3fSexmodSpecial(var9, var10, var11);
            case DIAMOND:
            default:
               return new Vector3fSexmodSpecial(23.0F, 100.0F, 93.0F);
         }
      } else {
         return null;
      }
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0, -0.77, -0.05);
      GlStateManager.scale(0.5, 0.5, 0.5);
   }

   @Override
   protected void applyItemPostRotation(boolean var1, ItemStack var2) {
      super.applyItemPostRotation(var1, var2);
      if (var2.getItem().getItemUseAction(var2) == EnumAction.BOW) {
         if (var1) {
            GlStateManager.translate(0.1F, 0.0F, 0.0F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         } else {
            GlStateManager.rotate(170.0F, 1.0F, 0.0F, 0.0F);
         }
      } else {
         GlStateManager.rotate(var1 ? 70.0F : 180.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.translate(0.0, 0.05, -0.03);
      }
   }

   @Override
   protected void applyBowRotation(boolean var1) {
   }

   @Override
   protected void applyShieldBlockingTransform(boolean var1, boolean var2) {
      super.applyShieldBlockingTransform(var1, var2);
      if (var1) {
         if (var2) {
            GlStateManager.translate(0.0, 0.2, -0.25);
            GlStateManager.rotate(85.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(38.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
         } else {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0, -0.265, -0.04);
         }
      } else if (var2) {
         GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(150.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(0.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.translate(0.0, -0.33, -0.1);
      } else {
         GlStateManager.translate(-0.02, -0.05, -0.05);
      }
   }

}
