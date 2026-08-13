package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GoblinEntity;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.TrigMath;







import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;

public class GoblinNpcModel extends GirlModel<BaseGirlEntity> {
   final float legSwingAngle = 60.0F;
   Minecraft mc = Minecraft.getMinecraft();

   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{
         new ResourceLocation("sexmod", "geo/goblin/goblin.geo.json"), new ResourceLocation("sexmod", "geo/goblin/armored.geo.json")
      };
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity var0) {
      return new ResourceLocation("sexmod", "textures/entity/goblin/goblin.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity var0) { return new ResourceLocation("sexmod", "animations/goblin/goblin.animation.json");
   }

   @Override
   protected boolean f_clash312(BaseGirlEntity var1) {
      if (!(var1 instanceof GoblinEntity)) {
         return super.f_clash312(var1);
      }

      GoblinEntity var2 = (GoblinEntity)var1;
      UUID var3 = var2.getInteractionPlayerUUID();
      if (var3 == null) {
         var3 = var2.getOwnerUUID();
      }

      if (var3 == null) {
         return true;
      }

      World var4 = var2.world;
      AbstractClientPlayer var5 = (AbstractClientPlayer)var4.getPlayerEntityByUUID(var3);
      return var5 == null ? true : "default".equals(var5.getSkinType());
   }

   @Override
   public void setLivingAnimations(BaseGirlEntity var1, Integer var2, AnimationEvent var3) {
      super.setLivingAnimations(var1, var2, var3);
      if (!(var1.world instanceof SexWorldClient)) {
         AnimationProcessor var4 = this.getAnimationProcessor();
         if (var1 instanceof GoblinEntity) {
            IBone var12 = var4.getBone("preggy");
            var12.setHidden(!(Boolean)var1.getDataManager().get(GoblinEntity.aV));
            IBone var13 = var4.getBone("body");
            IBone var14 = var4.getBone("head");
            Action var15 = var1.getCurrentAction();
            if ((var15 == Action.BREEDING_SLOW_2 || var15 == Action.BREEDING_FAST_2 || var15 == Action.BREEDING_CUM_2) && this.mc.gameSettings.thirdPersonView == 0) {
               var13.setPositionY(var13.getPositionY() + 1.5F);
            }

            IGoblin var16 = (IGoblin)var1;
            if (var15 == Action.AWAIT_PICK_UP || var15 == Action.VANISH) {
               this.a(var1, var13, var14);
            }

            if (var15 == Action.SIT) {
               this.a(var1, var14);
            }

            if (var15 == Action.START_THROWING) {
               if (this.mc.player.getPersistentID().equals(var16.getOwnerUUID())) {
                  this.a(var13, var4, var1, var16);
               } else {
                  this.a(var13, var4, var1);
               }
            } else {
               var13.setHidden(false);
            }

            if (!var13.isHidden() && var15 == Action.START_THROWING || var15 == Action.THROWN) {
               Vec3d var17 = d_clash346(var1);
               var13.setRotationX((float)var17.x);
               var13.setPositionY((float)var17.y);
               var13.setPositionZ((float)var17.z);
            }

            if (var15 == Action.START_THROWING || var15 == Action.PICK_UP) {
               this.a(var4, var16, var1);
            }
         } else {
            IBone var6 = var4.getBone("preggy");
            var6.setHidden(!(Boolean)var1.getDataManager().get(GoblinEntity.aV));
            IBone var7 = var4.getBone("body");
            IBone var8 = var4.getBone("head");
            Action var9 = var1.getCurrentAction();
            if ((var9 == Action.BREEDING_SLOW_2 || var9 == Action.BREEDING_FAST_2 || var9 == Action.BREEDING_CUM_2) && this.mc.gameSettings.thirdPersonView == 0) {
               var7.setPositionY(var7.getPositionY() + 1.5F);
            }

            IGoblin var10 = (IGoblin)var1;
            if (var9 == Action.VANISH) {
               this.a(var1, var7, var8);
            }

            if (var9 == Action.START_THROWING) {
               if (this.mc.player.getPersistentID().equals(var10.getOwnerUUID())) {
                  this.a(var7, var4, var1, var10);
               } else {
                  this.a(var7, var4, var1);
               }
            } else {
               var7.setHidden(false);
            }

            if (!var7.isHidden() && var9 == Action.START_THROWING || var9 == Action.THROWN) {
               Vec3d var11 = d_clash346(var1);
               var7.setRotationX((float)var11.x);
               var7.setPositionY((float)var11.y);
               var7.setPositionZ((float)var11.z);
            }

            if (var9 == Action.START_THROWING || var9 == Action.PICK_UP) {
               this.a(var4, var10, var1);
            }

            this.b(var4, var1);
            this.a(var4, var1);
         }
      }
   }

   void a(AnimationProcessor var1, BaseGirlEntity var2) {
      if (var2.getCurrentAction() == Action.START_THROWING) {
         if (this.mc.gameSettings.thirdPersonView == 0 && this.mc.player.getPersistentID().equals(((AbstractPlayerGirlEntity)var2).getOwnerUserUUID())) {
            IBone var3 = var1.getBone("body");
            if (var3 != null) {
               var3.setHidden(true);
            }
         }
      }
   }

   void b(AnimationProcessor var1, BaseGirlEntity var2) {
      if (var2.getCurrentAction() == Action.PICK_UP) {
         if (this.mc.gameSettings.thirdPersonView != 0 || !this.mc.player.getPersistentID().equals(((IGoblin)var2).getOwnerUUID())) {
            IBone var3 = var1.getBone("body");
            if (var3 != null) {
               IBone var4 = var1.getBone("steve");
               if (var4 != null) {
                  var3.setPositionY(var3.getPositionY() - 32.0F);
                  var4.setPositionY(var4.getPositionY() - 32.0F);
               }
            }
         }
      }
   }

   void a(AnimationProcessor var1, IGoblin var2, BaseGirlEntity var3) {
      UUID var4 = var2.getOwnerUUID();
      if (var4 != null) {
         EntityPlayer var5 = var3.world.getPlayerEntityByUUID(var4);
         if (var5 != null) {
            float var6 = RotationHelper.lerp(var5.prevLimbSwingAmount, var5.limbSwingAmount, this.mc.getRenderPartialTicks());
            float var7 = var5.limbSwing;
            float var8 = (float)Math.sin(var7);
            IBone var9 = var1.getBone("LeftLeg");
            IBone var10 = var1.getBone("RightLeg");
            float var11 = TrigMath.wrapDegrees(60.0F * var8 * var6);
            var9.setRotationX(var11);
            var10.setRotationX(-var11);
         }
      } else {
         var3.getInteractionPlayerUUID();
      }
   }

   void a(BaseGirlEntity var1, IBone var2) {
      EntityPlayer var3 = var1.world.getClosestPlayerToEntity(var1, 15.0);
      if (var3 != null) {
         Vec3d var4 = var3.getPositionVector();
         Vec3d var5 = var1.getPositionVector();
         Vec3d var6 = var4.subtract(var5);
         float var7 = var1.rotationYaw;
         boolean var8 = false;
         switch ((int)var7) {
            case -90:
               var8 = var4.x > var5.x;
               break;
            case 0:
               var8 = var4.z > var5.z;
               break;
            case 90:
               var8 = var4.x < var5.x;
               break;
            case 180:
               var8 = var4.z < var5.z;
         }

         if (!var8) {
            var2.setRotationY(0.0F);
         } else {
            float var9 = 0.0F;
            switch ((int)var7) {
               case 0:
                  var9 = -90.0F;
                  break;
               case 90:
                  var9 = 180.0F;
                  break;
               case 180:
                  var9 = 90.0F;
            }

            float var10 = (float)(-(MathHelper.atan2(var6.z, var6.x) * (180.0 / Math.PI) + var9));
            float var11 = ThreadNames.b((float)(var3.getEyeHeight() + var4.y - (var1.getEyeHeight() + var5.y)), -0.75F, 0.75F);
            var2.setRotationY(TrigMath.wrapDegrees(var10));
            var2.setRotationX(var11);
         }
      }
   }

   void a(BaseGirlEntity var1, IBone var2, IBone var3) {
      EntityPlayer var4 = var1.world.getClosestPlayerToEntity(var1, 15.0);
      if (var4 != null) {
         Vec3d var5 = var4.getPositionVector();
         Vec3d var6 = var1.getPositionVector();
         Vec3d var7 = var5.subtract(var6);
         float var8 = (float)(-(Math.atan2(var7.z, var7.x) * (180.0 / Math.PI))) + 90.0F;
         float var9 = ThreadNames.b((float)(var4.getEyeHeight() + var5.y - (var1.getEyeHeight() + var6.y)), -0.75F, 0.75F);
         var2.setRotationY(TrigMath.wrapDegrees(var8));
         var3.setRotationX(var9);
      }
   }

   void a(IBone var1, AnimationProcessor var2, BaseGirlEntity var3) {
      if (var3.isLocallyRegistered()) {
         var1.setHidden(true);
      } else {
         var1.setHidden(false);
         var2.getBone("steve").setHidden(true);
      }
   }

   void a(IBone var1, AnimationProcessor var2, BaseGirlEntity var3, IGoblin var4) {
      if (var3.isLocallyRegistered()) {
         var1.setHidden(true);
      } else {
         var1.setHidden(var4.getThrowProgress() < 15);
      }

      if (!var3.isLocallyRegistered()) {
         var2.getBone("steve").setHidden(true);
      }
   }

   @Override
   public String[] c() {
      return new String[]{"armorHelmet"};
   }

   @Override
   public String[] f() {
      return new String[]{"armorBoobL", "armorBoobR"};
   }

   @Override
   public String[] a() {
      return new String[]{"nippleL", "nippleR"};
   }

   @Override
   public String[] h() {
      return new String[]{"armorCheekR", "armorCheekL", "armorLegL", "armorLegR", "armorShinL", "armorShinR", "armorTorso"};
   }

   @Override
   public String[] e() {
      return new String[]{"fuckhole", "vagina", "meatCheekR", "meatCheekL", "meatLegL", "meatLegR", "meatShinL", "meatShinR"};
   }

   @Override
   public String[] b() {
      return new String[]{"armorFootL", "armorFootR"};
   }

   @Override
   public String[] d() {
      return new String[]{"meatFootL", "meatFootR"};
   }

}
