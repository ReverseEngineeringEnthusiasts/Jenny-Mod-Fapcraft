package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.GalathFlightHud;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.CatModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.entity.api.IGalath;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.networking.UpdateVelocityPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.ep;
import com.trolmastercard.sexmod.util.f2;







import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class GalathPlayerEntity extends AbstractPlayerGirlEntity implements IGalath {
   boolean ap = false;
   int ar = 0;
   boolean as = false;
   boolean aq = false;

   public GalathPlayerEntity(World var1) {
      super(var1);
   }

   public GalathPlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new CatModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return "textures/entity/galath/hand.png";
   }

   @Nullable
   @Override
   protected fp getNextAction(fp var1) {
      return null;
   }

   @Override
   protected fp getCumAction(fp var1) {
      if (var1 == fp.CORRUPT_FAST || var1 == fp.CORRUPT_SLOW) {
         return fp.CORRUPT_CUM;
      } else {
         return var1 == fp.RAPE_ON_GOING ? fp.RAPE_CUM : null;
      }
   }

   @Override
   public float i_clash226() {
      return 2.3F;
   }

   @Override
   public void b(String var1, UUID var2) {
      if ("cowgirl".equals(var1)) {
         this.b_clash577(var2);
         this.b(fp.RAPE_INTRO);
         this.a(this.getOutfitIndex(), fp.RAPE_INTRO);
      } else if ("mating press".equals(var1)) {
         this.b_clash577(var2);
         this.b(fp.CORRUPT_SLOW);
         this.a(this.getOutfitIndex(), fp.CORRUPT_SLOW);
         this.a_clash442();
      }
   }

   @Override
   public void b(fp var1) {
      fp var2 = this.getCurrentAction();
      if (var2 != fp.CORRUPT_CUM || var1 != fp.CORRUPT_FAST && var1 != fp.CORRUPT_SLOW) {
         if (var2 != fp.RAPE_CUM || var1 != fp.RAPE_ON_GOING) {
            if (var2 != fp.RAPE_CUM || var1 != fp.RAPE_CUM_IDLE) {
               if (var1 == fp.CORRUPT_SLOW) {
                  this.as = false;
               }

               super.b(var1);
            }
         }
      }
   }

   void a_clash442() {
      EntityPlayer var1 = this.S_clash495();
      if (var1 != null) {
         Vec3d var2 = ck.rotateByYaw(new Vec3d(0.5, 0.5F - var1.func_70047_e(), 0.4F), this.getYawRotation()).func_178787_e(this.getTargetPosition());
         var1.func_70634_a(var2.field_72450_a, var2.field_72448_b, var2.field_72449_c);
      }
   }

   @Override
   public boolean b_clash23() {
      return false;
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      a(var1, this, new String[]{"cowgirl", "mating press", "ride"}, false);
      return true;
   }

   @Override
   public boolean v_clash227() {
      return false;
   }

   @Override
   public boolean A_clash381() {
      return false;
   }

   @Override
   public f2 d_clash20() {
      return new f2(0.0, 0.0, 0.0, 0.0);
   }

   @Override
   public boolean c_clash21() {
      return this.getOutfitIndex() == 0 || this.ap;
   }

   @Override
   public boolean a_clash22() {
      switch (this.getCurrentAction()) {
         case CORRUPT_CUM:
         case CORRUPT_FAST:
         case CORRUPT_SLOW:
         case COWGIRLCUM:
            return false;
         default:
            return true;
      }
   }

   @Override
   public void B_clash233() {
      this.c_clash573(true);
   }

   @Override
   public void func_70071_h_() {
      super.func_70071_h_();
      this.b_clash444();
      if (this.field_70170_p.field_72995_K) {
         this.d_clash443();
      }
   }

   @SideOnly(Side.CLIENT)
   void d_clash443() {
      if (this.isControlledByLocalPlayer()) {
         if (this.getCurrentAction() == fp.RAPE_INTRO) {
            HornyMeterHud.a_clash359(false);
         }
      }
   }

   void b_clash444() {
      switch (this.getCurrentAction()) {
         case CORRUPT_CUM:
         case CORRUPT_FAST:
         case CORRUPT_SLOW:
         case RAPE_INTRO:
         case RAPE_ON_GOING:
         case RAPE_CUM:
         case RAPE_CHARGE:
         case RAPE_CUM_IDLE:
            this.ap = true;
            return;
         case COWGIRLCUM:
         default:
            this.ap = false;
      }
   }

   boolean g_clash445() {
      EntityPlayer var1 = this.k_clash584();
      return var1 == null
         ? false
         : this.field_70170_p.func_180495_p(var1.func_180425_c().func_177984_a().func_177984_a()).func_177230_c() != Blocks.field_150350_a;
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == fp.NULL && this.getCurrentAction().autoBlink) {
               this.a("animation.galath.blink", true, var1);
            } else {
               this.a("animation.galath.null", true, var1);
            }
            break;
         case "movement":
            this.E.setAnimationSpeed(1.0);
            if (this.getCurrentAction() != fp.NULL) {
               this.a("animation.galath.null", true, var1);
            } else if (this.ak) {
               this.a("animation.galath.sit", true, var1);
            } else if (!this.af) {
               this.a("animation.galath.controlled_flight", true, var1);
            } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) == 0.0F) {
               this.a(this.g_clash445() ? "animation.galath.crouchidle" : "animation.galath.idle", true, var1);
            } else if (this.aj) {
               this.E.setAnimationSpeed(1.5);
               this.a(this.g_clash445() ? "animation.galath.crouchwalk" : "animation.galath.run", true, var1);
            } else if (this.ao.y >= -0.1F) {
               this.E.setAnimationSpeed(2.0);
               this.a(this.g_clash445() ? "animation.galath.crouchwalk" : "animation.galath.walk", true, var1);
            } else {
               this.E.setAnimationSpeed(1.5);
               this.a(this.g_clash445() ? "animation.galath.crouchwalk" : "animation.galath.backwards_walk", true, var1);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case CORRUPT_CUM:
                  this.a("animation.galath.corrupt_cum", true, var1);
                  break;
               case CORRUPT_FAST:
                  this.a("animation.galath.corrupt_" + (this.as ? "hard" : "soft"), true, var1);
                  break;
               case CORRUPT_SLOW:
                  this.a("animation.galath.corrupt_slow", true, var1);
               case COWGIRLCUM:
               case RAPE_CHARGE:
               default:
                  break;
               case RAPE_INTRO:
                  this.a("animation.galath.rape_intro", true, var1);
                  break;
               case RAPE_ON_GOING:
                  this.a("animation.galath.rape" + this.ar, true, var1);
                  break;
               case RAPE_CUM:
                  this.a("animation.galath.rape_cum", true, var1);
                  break;
               case RAPE_CUM_IDLE:
                  this.a("animation.galath.rape_cum_idle", true, var1);
                  break;
               case NULL:
                  return PlayState.STOP;
               case STRIP:
                  this.a("animation.galath.strip", true, var1);
                  break;
               case ATTACK:
                  this.a("animation.galath.attack" + this.S, true, var1);
                  break;
               case BOW:
                  this.a("animation.galath.bowcharge", true, var1);
                  break;
               case RIDE:
               case SIT:
                  this.a("animation.galath.sit", true, var1);
                  break;
               case CORRUPT_INTRO:
                  this.a("animation.galath.corrupt_intro", true, var1);
                  break;
               case CONTROLLED_FLIGHT:
                  this.a("animation.galath.controlled_flight", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData var1) {
      this.p_clash506();
      this.C
         .registerSoundListener(
            var1x -> {
               switch (var1x.sound) {
                  case "attackDone":
                     if (++this.S == 3) {
                        this.S = 0;
                     }
                     break;
                  case "cum":
                     this.a(SoundHandler.MISC_SMALLINSERTS, 2.0F);
                     break;
                  case "pound":
                     this.a_clash588(SoundHandler.MISC_POUNDING);
                     break;
                  case "flap":
                     this.a_clash588(SoundHandler.MISC_FLAP);
                     break;
                  case "setNude":
                     this.ap = true;
                     Vec3d var4 = this.func_174791_d();
                     Vec3d var5 = this.getCachedBoneOffset("slipR").func_178787_e(var4);
                     Vec3d var6 = this.getCachedBoneOffset("slipL").func_178787_e(var4);
                     Vec3d var7 = this.getCachedBoneOffset("turnable").func_178787_e(var4);
                     this.field_70170_p
                        .func_175688_a(EnumParticleTypes.DRAGON_BREATH, var5.field_72450_a, var5.field_72448_b, var5.field_72449_c, 0.0, 0.0, 0.0, new int[0]);
                     this.field_70170_p
                        .func_175688_a(EnumParticleTypes.DRAGON_BREATH, var6.field_72450_a, var6.field_72448_b, var6.field_72449_c, 0.0, 0.0, 0.0, new int[0]);
                     this.field_70170_p
                        .func_175688_a(EnumParticleTypes.DRAGON_BREATH, var7.field_72450_a, var7.field_72448_b, var7.field_72449_c, 0.0, 0.0, 0.0, new int[0]);
                     break;
                  case "rapeIntroDone":
                     if (this.isControlledByLocalPlayer()) {
                        this.b(fp.RAPE_ON_GOING);
                     }
                     break;
                  case "rape_switch":
                     Random var8 = this.func_70681_au();
                     int var9 = this.ar;

                     do {
                        this.ar = var8.nextInt(3);
                     } while (this.ar == var9);

                     return;
                  case "poundRape":
                     this.a_clash588(SoundHandler.MISC_POUNDING);
                     if (this.isControlledByLocalPlayer()) {
                        HornyMeterHud.addToHornyMeter(0.03F);
                     }
                     break;
                  case "enableRapeUI":
                     if (this.isControlledByLocalPlayer()) {
                        HornyMeterHud.a_clash359(false);
                     }
                     break;
                  case "reloadRenderer":
                     if (!this.isControlledByLocalPlayer()) {
                        return;
                     }

                     Minecraft var18 = Minecraft.func_71410_x();
                     if (var18.field_71474_y.field_74320_O != 0) {
                        var18.field_71438_f.func_72712_a();
                     }
                     break;
                  case "corruptSwitch":
                     if (this.isControlledByLocalPlayer() && d3.d) {
                        this.b(fp.CORRUPT_FAST);
                     }
                     break;
                  case "corrupt_hard":
                     if (this.isControlledByLocalPlayer() && d3.d) {
                        this.as = true;
                        this.N();
                     }
                     break;
                  case "corrupt_hard_end":
                     this.b(fp.CORRUPT_SLOW);
                     this.as = false;
                     break;
                  case "addCum":
                     HornyMeterHud.addToHornyMeter(0.03);
                     break;
                  case "clearcum":
                     CummyEntity.a_clash747(this);
                  case "reset":
                     if (this.isControlledByLocalPlayer()) {
                        this.r_clash533();
                     }
                     break;
                  case "setCamCorrupt":
                     if (!this.isControlledByLocalPlayer()) {
                        return;
                     }

                     this.aq = true;
                     EntityPlayerSP var11 = Minecraft.func_71410_x().field_71439_g;
                     float var12 = this.getYawRotation() + 220.0F;
                     Vec3d var13 = ck.rotateByYaw(new Vec3d(0.5, 0.5F - var11.func_70047_e(), 0.4F), this.getYawRotation()).func_178787_e(this.getTargetPosition());
                     PacketHandler.b.sendToServer(new TeleportPlayerPacket(var11.getPersistentID().toString(), var13, var12, 15.0F));
                     HornyMeterHud.showHornyMeter();
                     break;
                  case "enableBoyCam":
                     if (this.isControlledByLocalPlayer()) {
                        this.aq = false;
                     }
                     break;
                  case "creampie":
                     CummyEntity.a(new ep(130, var0 -> {
                        Vec3d var1xx = var0.d_clash548("futaCockTip");
                        Vec3d var2 = var0.d_clash548("futaCockTipDirHelp");
                        return var1xx.func_178788_d(var2).func_72432_b();
                     }, var0 -> var0.getCachedBoneOffset("futaCockTip").func_178787_e(var0.getTargetPosition()), this, 0.3F, 0.3F));
                     CummyEntity.a(
                        new ep(
                           100,
                           var1xx -> ck.rotateByYaw(new Vec3d(0.0, 0.0, 0.6F), this.getYawRotation()),
                           var0 -> var0.getCachedBoneOffset("creampiePos").func_178787_e(var0.getTargetPosition()),
                           this,
                           0.6F,
                           0.5F
                        )
                     );
                     this.a(SoundHandler.randomSound(SoundHandler.MISC_SMALLINSERTS), 3.0F);
                     break;
                  case "blackScreenTamed":
                  case "blackScreen":
                     if (this.isControlledByLocalPlayer()) {
                        BeeScreen.b_clash732();
                     }
                     break;
                  case "flapControlled":
                     if (this.isControlledByLocalPlayer()) {
                        GalathFlightHud.f_clash791();
                        this.a_clash588(SoundHandler.MISC_FLAP);
                        Minecraft var10 = Minecraft.func_71410_x();
                        EntityPlayerSP var14 = var10.field_71439_g;
                        MovementInput var15 = var14.field_71158_b;
                        Vec2f var16 = var15.func_190020_b();
                        if (var16.field_189982_i != 0.0F || var16.field_189983_j != 0.0F) {
                           Vec3d var17 = ck.a(
                              new Vec3d(-var16.field_189982_i, 0.0, var16.field_189983_j),
                              RotationHelper.lerp(var14.field_70127_C, var14.field_70125_A, var10.func_184121_ak()),
                              RotationHelper.lerp(var14.field_70758_at, var14.field_70759_as, var10.func_184121_ak())
                           );
                           PacketHandler.b.sendToServer(new UpdateVelocityPacket(var17, this.getGirlId()));
                        }
                     }
                     break;
                  case "clap":
                     this.a_clash588(SoundHandler.MISC_CLAP);
                     break;
                  case "energysound":
                     this.a(SoundHandler.MISC_BEEW[1]);
                     break;
                  case "energy2":
                     this.a(SoundHandler.MISC_BEEW[2]);
                     break;
                  case "tpSound":
                     this.a(SoundHandler.MISC_WEOWEO[2]);
                     break;
                  case "sexui":
                     if (this.isControlledByLocalPlayer()) {
                        HornyMeterHud.showHornyMeter();
                     }
               }
            }
         );
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.s);
      var1.addAnimationController(this.E);
   }

}
