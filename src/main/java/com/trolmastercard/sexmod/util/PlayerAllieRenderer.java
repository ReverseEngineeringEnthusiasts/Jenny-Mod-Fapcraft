package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import com.trolmastercard.sexmod.entity.AlliePlayerEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;







import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PlayerAllieRenderer extends GirlPlayerRenderer {
   static final float E = 8.0F;
   static final float K = 1.68F;
   static final float M = 5.0F;
   static Collection<PlayerAllieRenderer> J = new ArrayList<>();
   double C = 0.0;
   double z = 0.0;
   double A = 0.0;
   double D = 0.0;
   float F = 0.0F;
   float B = 0.0F;
   float G;
   float I;
   double H = 0.0;
   double L = 0.0;

   public PlayerAllieRenderer(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
      J.add(this);
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0F, -1.1F, 0.0F);
      GlStateManager.scale(0.7F, 0.7F, 0.7F);
   }

   @Override
   protected void applyItemPostRotation(boolean var1, ItemStack var2) {
      super.applyItemPostRotation(var1, var2);
      switch (var2.getItem().getItemUseAction(var2)) {
         default:
            if (!var1) {
               GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
            }

            GlStateManager.translate(0.0, 0.05, 0.0);
         case BLOCK:
         case BOW:
      }
   }

   @Override
   protected void applyBowRotation(boolean var1) {
      super.applyBowRotation(var1);
      if (var1) {
         GlStateManager.translate(0.15, 0.0, 0.0);
      } else {
         GlStateManager.translate(-0.05, 0.0, 0.0);
      }
   }

   @Override
   protected void applyShieldBlockingTransform(boolean var1, boolean var2) {
      super.applyShieldBlockingTransform(var1, var2);
      if (var1 && !var2) {
         GlStateManager.translate(-0.025, -0.1, -0.1);
         GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.0F);
      } else if (!var1 && !var2) {
         GlStateManager.translate(-0.05, -0.125, 0.125);
         GlStateManager.rotate(50.0F, 1.0F, 0.0F, 0.0F);
      }
   }

   @Override
   protected void onBoneRenderStart(String var1, GeoBone var2) {
      if (!(Boolean)this.w.getDataManager().get(BaseGirlEntity.IS_ANCHORED)) {
         if ("tail".equals(var1)) {
            this.a(var2, 0.0F, 0.0F, 1.0F);
         }

         if ("body".equals(var1)) {
            this.a_clash408(var2);
         }

         if (this.w.getCurrentAction() != Action.BOW) {
            if ("armL".equals(var1)) {
               this.a(var2, 0.0F, (float) (-Math.PI / 9), 0.15F);
            }

            if (this.w.getCurrentAction() != Action.ATTACK) {
               if ("armR".equals(var1)) {
                  this.a(var2, 0.0F, (float) (Math.PI / 9), 0.15F);
               }
            }
         }
      }
   }

   void a(GeoBone var1, float var2, float var3, float var4) {
      double var5 = this.C - this.A;
      double var7 = this.z - this.D;
      double var9 = (Math.PI / 180.0) * this.w.rotationYaw;
      Vec2f var11 = new Vec2f((float)(var5 * Math.cos(var9) + var7 * Math.sin(var9)), (float)(-var5 * Math.sin(var9) + var7 * Math.cos(var9)));
      this.G = var11.y * -8.0F;
      this.I = var11.x * 8.0F;
      this.G = ThreadNames.b(this.G, -1.68F, 1.68F);
      this.I = ThreadNames.b(this.I, -1.68F, 1.68F);
      this.G = RotationHelper.lerp(this.F, this.G, this.y);
      this.I = RotationHelper.lerp(this.B, this.I, this.y);
      var1.setRotationX(var2 + this.G * var4);
      var1.setRotationZ(var3 + this.I * var4);
   }

   void a_clash408(GeoBone var1) {
      double var2 = this.C - this.A;
      double var4 = this.z - this.D;
      this.L = (Math.abs(var2) + Math.abs(var4)) * 5.0;
      this.L = ThreadNames.b((float)this.L, 0.0F, 1.0F);
      var1.setPositionY((float)RotationHelper.a_clash28(5.0, 0.0, RotationHelper.b(this.H, this.L, this.y)));
      if (this.w instanceof AlliePlayerEntity) {
         ((AlliePlayerEntity)this.w).aq = (float)RotationHelper.a_clash28(0.3F, 0.0, RotationHelper.b(this.H, this.L, this.y));
      }
   }

   void a_clash409() {
      if (this.w != null) {
         this.F = this.G;
         this.B = this.I;
         this.H = this.L;
         if (this.w.getOwnerUserUUID() != null) {
            EntityPlayer var1 = this.j.world.getPlayerEntityByUUID(this.w.getOwnerUserUUID());
            if (var1 != null) {
               this.A = this.C;
               this.D = this.z;
               this.C = var1.posX;
               this.z = var1.posZ;
            }
         }
      }
   }


   public static class a {
      @SubscribeEvent
      public void a(ClientTickEvent var1) {
         for (PlayerAllieRenderer var3 : PlayerAllieRenderer.J) {
            var3.a_clash409();
         }
      }
   }
}
