package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GoblinEntity;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.gc;







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
   final float g = 60.0F;
   Minecraft f = Minecraft.func_71410_x();

   @Override
   protected ResourceLocation[] a_clash33() {
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
      UUID var3 = var2.ae_clash498();
      if (var3 == null) {
         var3 = var2.e_clash54();
      }

      if (var3 == null) {
         return true;
      }

      World var4 = var2.field_70170_p;
      AbstractClientPlayer var5 = (AbstractClientPlayer)var4.func_152378_a(var3);
      return var5 == null ? true : "default".equals(var5.func_175154_l());
   }

   @Override
   public void setLivingAnimations(BaseGirlEntity var1, Integer var2, AnimationEvent var3) {
      super.setLivingAnimations(var1, var2, var3);
      if (!(var1.field_70170_p instanceof SexWorldClient)) {
         AnimationProcessor var4 = this.getAnimationProcessor();
         if (var1 instanceof GoblinEntity) {
            IBone var12 = var4.getBone("preggy");
            var12.setHidden(!(Boolean)var1.func_184212_Q().func_187225_a(GoblinEntity.aV));
            IBone var13 = var4.getBone("body");
            IBone var14 = var4.getBone("head");
            fp var15 = var1.y_clash492();
            if ((var15 == fp.BREEDING_SLOW_2 || var15 == fp.BREEDING_FAST_2 || var15 == fp.BREEDING_CUM_2) && this.f.field_71474_y.field_74320_O == 0) {
               var13.setPositionY(var13.getPositionY() + 1.5F);
            }

            IGoblin var16 = (IGoblin)var1;
            if (var15 == fp.AWAIT_PICK_UP || var15 == fp.VANISH) {
               this.a(var1, var13, var14);
            }

            if (var15 == fp.SIT) {
               this.a(var1, var14);
            }

            if (var15 == fp.START_THROWING) {
               if (this.f.field_71439_g.getPersistentID().equals(var16.e_clash54())) {
                  this.a(var13, var4, var1, var16);
               } else {
                  this.a(var13, var4, var1);
               }
            } else {
               var13.setHidden(false);
            }

            if (!var13.isHidden() && var15 == fp.START_THROWING || var15 == fp.THROWN) {
               Vec3d var17 = d_clash346(var1);
               var13.setRotationX((float)var17.field_72450_a);
               var13.setPositionY((float)var17.field_72448_b);
               var13.setPositionZ((float)var17.field_72449_c);
            }

            if (var15 == fp.START_THROWING || var15 == fp.PICK_UP) {
               this.a(var4, var16, var1);
            }
         } else {
            IBone var6 = var4.getBone("preggy");
            var6.setHidden(!(Boolean)var1.func_184212_Q().func_187225_a(GoblinEntity.aV));
            IBone var7 = var4.getBone("body");
            IBone var8 = var4.getBone("head");
            fp var9 = var1.y_clash492();
            if ((var9 == fp.BREEDING_SLOW_2 || var9 == fp.BREEDING_FAST_2 || var9 == fp.BREEDING_CUM_2) && this.f.field_71474_y.field_74320_O == 0) {
               var7.setPositionY(var7.getPositionY() + 1.5F);
            }

            IGoblin var10 = (IGoblin)var1;
            if (var9 == fp.VANISH) {
               this.a(var1, var7, var8);
            }

            if (var9 == fp.START_THROWING) {
               if (this.f.field_71439_g.getPersistentID().equals(var10.e_clash54())) {
                  this.a(var7, var4, var1, var10);
               } else {
                  this.a(var7, var4, var1);
               }
            } else {
               var7.setHidden(false);
            }

            if (!var7.isHidden() && var9 == fp.START_THROWING || var9 == fp.THROWN) {
               Vec3d var11 = d_clash346(var1);
               var7.setRotationX((float)var11.field_72450_a);
               var7.setPositionY((float)var11.field_72448_b);
               var7.setPositionZ((float)var11.field_72449_c);
            }

            if (var9 == fp.START_THROWING || var9 == fp.PICK_UP) {
               this.a(var4, var10, var1);
            }

            this.b(var4, var1);
            this.a(var4, var1);
         }
      }
   }

   void a(AnimationProcessor var1, BaseGirlEntity var2) {
      if (var2.y_clash492() == fp.START_THROWING) {
         if (this.f.field_71474_y.field_74320_O == 0 && this.f.field_71439_g.getPersistentID().equals(((AbstractPlayerGirlEntity)var2).m_clash583())) {
            IBone var3 = var1.getBone("body");
            if (var3 != null) {
               var3.setHidden(true);
            }
         }
      }
   }

   void b(AnimationProcessor var1, BaseGirlEntity var2) {
      if (var2.y_clash492() == fp.PICK_UP) {
         if (this.f.field_71474_y.field_74320_O != 0 || !this.f.field_71439_g.getPersistentID().equals(((IGoblin)var2).e_clash54())) {
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
      UUID var4 = var2.e_clash54();
      if (var4 != null) {
         EntityPlayer var5 = var3.field_70170_p.func_152378_a(var4);
         if (var5 != null) {
            float var6 = RotationHelper.a_clash25(var5.field_184618_aE, var5.field_70721_aZ, this.f.func_184121_ak());
            float var7 = var5.field_184619_aG;
            float var8 = (float)Math.sin(var7);
            IBone var9 = var1.getBone("LeftLeg");
            IBone var10 = var1.getBone("RightLeg");
            float var11 = gc.c_clash744(60.0F * var8 * var6);
            var9.setRotationX(var11);
            var10.setRotationX(-var11);
         }
      } else {
         var3.ae_clash498();
      }
   }

   void a(BaseGirlEntity var1, IBone var2) {
      EntityPlayer var3 = var1.field_70170_p.func_72890_a(var1, 15.0);
      if (var3 != null) {
         Vec3d var4 = var3.func_174791_d();
         Vec3d var5 = var1.func_174791_d();
         Vec3d var6 = var4.func_178788_d(var5);
         float var7 = var1.field_70177_z;
         boolean var8 = false;
         switch ((int)var7) {
            case -90:
               var8 = var4.field_72450_a > var5.field_72450_a;
               break;
            case 0:
               var8 = var4.field_72449_c > var5.field_72449_c;
               break;
            case 90:
               var8 = var4.field_72450_a < var5.field_72450_a;
               break;
            case 180:
               var8 = var4.field_72449_c < var5.field_72449_c;
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

            float var10 = (float)(-(MathHelper.func_181159_b(var6.field_72449_c, var6.field_72450_a) * (180.0 / Math.PI) + var9));
            float var11 = ThreadNames.b((float)(var3.func_70047_e() + var4.field_72448_b - (var1.func_70047_e() + var5.field_72448_b)), -0.75F, 0.75F);
            var2.setRotationY(gc.c_clash744(var10));
            var2.setRotationX(var11);
         }
      }
   }

   void a(BaseGirlEntity var1, IBone var2, IBone var3) {
      EntityPlayer var4 = var1.field_70170_p.func_72890_a(var1, 15.0);
      if (var4 != null) {
         Vec3d var5 = var4.func_174791_d();
         Vec3d var6 = var1.func_174791_d();
         Vec3d var7 = var5.func_178788_d(var6);
         float var8 = (float)(-(Math.atan2(var7.field_72449_c, var7.field_72450_a) * (180.0 / Math.PI))) + 90.0F;
         float var9 = ThreadNames.b((float)(var4.func_70047_e() + var5.field_72448_b - (var1.func_70047_e() + var6.field_72448_b)), -0.75F, 0.75F);
         var2.setRotationY(gc.c_clash744(var8));
         var3.setRotationX(var9);
      }
   }

   void a(IBone var1, AnimationProcessor var2, BaseGirlEntity var3) {
      if (var3.h_clash508()) {
         var1.setHidden(true);
      } else {
         var1.setHidden(false);
         var2.getBone("steve").setHidden(true);
      }
   }

   void a(IBone var1, AnimationProcessor var2, BaseGirlEntity var3, IGoblin var4) {
      if (var3.h_clash508()) {
         var1.setHidden(true);
      } else {
         var1.setHidden(var4.a_clash58() < 15);
      }

      if (!var3.h_clash508()) {
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

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
