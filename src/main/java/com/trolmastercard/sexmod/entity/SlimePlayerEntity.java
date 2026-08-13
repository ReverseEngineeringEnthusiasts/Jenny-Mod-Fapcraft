package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.KoboldModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SetPlayerForGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.d3;







import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class SlimePlayerEntity extends AbstractPlayerGirlEntity {
   boolean ap = false;
   int aq = 0;

   protected SlimePlayerEntity(World var1) {
      super(var1);
   }

   public SlimePlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public float i_clash226() {
      return 1.6F;
   }

   public float func_70047_e() {
      return 1.64F;
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
   public IVanillaModel a_clash228(int var1) {
      return new KoboldModel();
   }

   @Override
   public String c_clash229(int var1) {
      return "textures/entity/slime/hand.png";
   }

   @Override
   public void b(String var1, UUID var2) {
      if ("action.names.blowjob".equals(var1)) {
         this.a(0, fp.SUCKBLOWJOB);
         this.b(fp.SUCKBLOWJOB);
         this.b_clash577(var2);
      }
   }

   @Override
   public boolean b_clash230(EntityPlayer var1) {
      a(var1, this, new String[]{"action.names.blowjob"}, false);
      return true;
   }

   @Override
   public void b(fp var1) {
      if (this.y_clash492() != fp.CUMBLOWJOB || var1 != fp.THRUSTBLOWJOB && var1 != fp.SUCKBLOWJOB) {
         if (this.y_clash492() != fp.DOGGYCUM || var1 != fp.DOGGYFAST && var1 != fp.DOGGYSLOW) {
            super.b(var1);
         }
      }
   }

   @Override
   protected fp c_clash235(fp var1) {
      if (var1 == fp.SUCKBLOWJOB) {
         return fp.THRUSTBLOWJOB;
      } else {
         return var1 == fp.DOGGYSLOW ? fp.DOGGYFAST : null;
      }
   }

   @Override
   protected fp a_clash236(fp var1) {
      if (var1 == fp.SUCKBLOWJOB || var1 == fp.THRUSTBLOWJOB) {
         return fp.CUMBLOWJOB;
      } else {
         return var1 != fp.DOGGYSLOW && var1 != fp.DOGGYFAST ? null : fp.DOGGYCUM;
      }
   }

   @Override
   public void func_70619_bc() {
      super.func_70619_bc();
      if (this.y_clash492() == fp.WAITDOGGY) {
         EntityPlayer var1 = this.j_clash575();
         if (var1 != null) {
            if (!(var1.func_174791_d().func_72438_d(this.w_clash576()) > 1.0)) {
               PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var1);
               this.e_clash499(var1.getPersistentID());
               var1.field_70177_z = this.I_clash415();
               this.r = this.I_clash415();
               var1.func_70107_b(this.w_clash576().field_72450_a, this.w_clash576().field_72448_b, this.w_clash576().field_72449_c);
               var1.func_191958_b(0.0F, 0.0F, 0.0F, 0.0F);
               this.a_clash536(0.0, 0.0, 0.4, 0.0F, 60.0F);
               this.b(fp.DOGGYSTART);
               var1.func_189654_d(true);
               var1.field_70145_X = true;
               EntityPlayer var2 = this.field_70170_p.func_152378_a(this.m_clash583());
               var2.func_189654_d(true);
               var1.field_70145_X = true;
               var1.field_71075_bZ.field_75100_b = true;
               var2.field_71075_bZ.field_75100_b = true;
            }
         }
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.y_clash492() != fp.NULL && this.y_clash492().autoBlink) {
               this.a("animation.slime.fhappy", true, var1);
            } else {
               this.a("animation.slime.null", true, var1);
            }
            break;
         case "movement":
            if (this.y_clash492() != fp.NULL) {
               this.a("animation.slime.null", true, var1);
            } else if (this.ak) {
               this.a("animation.slime.sit", true, var1);
            } else {
               if (this.E.getCurrentAnimation() != null && this.E.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.ap = !this.ap;
               }

               if (!this.af) {
                  this.a("animation.slime.fly" + (this.ap ? "2" : ""), true, var1);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.a("animation.slime.run", true, var1);
                  } else if (this.ao.y >= -0.1F) {
                     this.a("animation.slime.walk", true, var1);
                  } else {
                     this.a("animation.slime.backwards_walk", true, var1);
                  }
               } else {
                  this.a("animation.slime.idle", true, var1);
               }
            }
            break;
         case "action":
            if (this.y_clash492() == fp.NULL) {
               this.a("animation.slime.null", true, var1);
            } else {
               switch (this.y_clash492()) {
                  case UNDRESS:
                     this.a("animation.slime.undress", false, var1);
                     break;
                  case DRESS:
                     this.a("animation.slime.dress", false, var1);
                     break;
                  case STRIP:
                     this.a("animation.slime.strip", false, var1);
                     break;
                  case SUCKBLOWJOB:
                     this.a("animation.slime.blowjobsuck", true, var1);
                     break;
                  case THRUSTBLOWJOB:
                     this.a("animation.slime.blowjobthrust", true, var1);
                     break;
                  case CUMBLOWJOB:
                     this.a("animation.slime.blowjobcum", false, var1);
                     break;
                  case STARTDOGGY:
                     this.a("animation.slime.doggygoonbed", false, var1);
                     break;
                  case WAITDOGGY:
                     this.a("animation.slime.doggywait", true, var1);
                     break;
                  case DOGGYSTART:
                     this.a("animation.slime.doggystart", false, var1);
                     break;
                  case DOGGYSLOW:
                     this.a("animation.slime.doggyslow", true, var1);
                     break;
                  case DOGGYFAST:
                     this.a("animation.slime.doggyfast", true, var1);
                     break;
                  case DOGGYCUM:
                     this.a("animation.slime.doggycum", false, var1);
                     break;
                  case ATTACK:
                     this.a("animation.slime.attack" + this.S, false, var1);
                     break;
                  case BOW:
                     this.a("animation.slime.bowcharge", false, var1);
                     break;
                  case RIDE:
                     this.a("animation.slime.ride", true, var1);
                     break;
                  case SIT:
                     this.a("animation.slime.sit", true, var1);
               }
            }
      }

      return PlayState.CONTINUE;
   }

   @Override
   public void registerControllers(AnimationData var1) {
      if (this.C == null) {
         this.p_clash506();
      }

      AnimationController.ISoundListener var2 = var1x -> {
         String var2x = var1x.sound;
         switch (var2x) {
            case "attackDone":
               if (++this.S == 3) {
                  this.S = 0;
               }
               break;
            case "undress":
               if (this.e_clash544()) {
                  this.m.func_187227_b(D, 0);
                  this.r_clash533();
               }
               break;
            case "dress":
               if (this.e_clash544()) {
                  this.m.func_187227_b(D, 1);
                  this.b((fp) null);
                  this.r_clash533();
               }
               break;
            case "sexUiOn":
               if (this.n_clash537()) {
                  HornyMeterHud.d_clash358();
               }
               break;
            case "bjiMSG10":
               if (this.n_clash537()) {
                  this.a_clash536(-0.4, -0.8, -0.2, 60.0F, -3.0F);
               }
               break;
            case "bjiMSG11":
               this.a(SoundEvents.field_187886_fs, 0.5F);
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.02);
               }
               break;
            case "bjiMSG12":
               if (Reference.f.nextInt(5) == 0) {
                  this.a(SoundEvents.field_187882_fq, 0.5F);
               }

               this.a(SoundEvents.field_187886_fs, 0.5F);
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.02);
               }
               break;
            case "bjtMSG1":
               this.a(SoundEvents.field_187878_fo);
               this.a(SoundEvents.field_187874_fm);
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.04);
               }
               break;
            case "bjiDone":
               this.b(fp.SUCKBLOWJOB);
               if (this.n_clash537()) {
                  HornyMeterHud.d_clash358();
               }
               break;
            case "bjtDone":
               this.b(fp.SUCKBLOWJOB);
               break;
            case "doggyfastReady":
               if (this.n_clash537() && d3.d) {
                  this.N();
               }
               break;
            case "bjtReady":
               if (this.n_clash537() && d3.d) {
                  this.N();
               }
               break;
            case "bjcMSG1":
               this.a(SoundEvents.field_187882_fq);
               break;
            case "bjcMSG2":
               this.a(SoundEvents.field_187882_fq);
               if (this.n_clash537()) {
                  HornyMeterHud.c_clash360();
               }
               break;
            case "doggyslowMSG2":
               this.a(SoundEvents.field_187878_fo);
               break;
            case "bjcBlackScreen":
               if (this.n_clash537()) {
                  BeeScreen.b_clash732();
               }
               break;
            case "bjcDone":
            case "doggyCumDone":
               if (this.n_clash537()) {
                  HornyMeterHud.b_clash363();
                  this.r_clash533();
               }
               break;
            case "doggyGoOnBedMSG1":
               this.a(SoundEvents.field_187886_fs);
               this.r = this.field_70177_z;
               break;
            case "doggyGoOnBedDone":
               PacketHandler.b.sendToServer(new SetPlayerForGirlPacket(this.f_clash491(), Minecraft.func_71410_x().field_71439_g.getPersistentID()));
               this.b(fp.WAITDOGGY);
               break;
            case "doggystartMSG1":
               this.a(SoundHandler.MISC_TOUCH[0]);
               break;
            case "doggystartMSG2":
               this.a(SoundHandler.MISC_TOUCH[1]);
               break;
            case "doggystartMSG3":
               this.a(SoundEvents.field_187886_fs, 0.25F);
               break;
            case "doggystartMSG4":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_SMALLINSERTS), 1.5F);
               if (this.n_clash537()) {
                  HornyMeterHud.b_clash363();
               }
               break;
            case "doggystartMSG5":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING), 0.33F);
               this.a(SoundEvents.field_187878_fo);
               break;
            case "doggystartDone":
               this.b(fp.DOGGYSLOW);
               if (this.n_clash537()) {
                  HornyMeterHud.d_clash358();
               }
               break;
            case "doggyslowMSG1":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING), 0.33F);
               int var5 = Reference.f.nextInt(4);
               if (var5 == 0) {
                  var5 = Reference.f.nextInt(2);
                  if (var5 == 0) {
                     this.a(SoundEvents.field_187882_fq);
                  } else {
                     this.a(SoundEvents.field_187886_fs);
                  }
               } else {
                  this.a(SoundEvents.field_187878_fo);
               }

               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.00666);
               }
               break;
            case "doggyfastMSG1":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.02);
               }

               this.aq++;
               if (this.aq % 2 == 0) {
                  int var6 = Reference.f.nextInt(2);
                  if (var6 == 0) {
                     this.a(SoundEvents.field_187882_fq);
                  } else {
                     this.a(SoundEvents.field_187886_fs);
                  }
               } else {
                  this.a(SoundEvents.field_187878_fo);
               }
               break;
            case "doggyfastDone":
               this.b(fp.DOGGYSLOW);
               break;
            case "doggycumMSG1":
               this.a(SoundHandler.MISC_CUMINFLATION[0], 4.0F);
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING), 2.0F);
               this.a(SoundEvents.field_187874_fm);
         }
      };
      this.C.registerSoundListener(var2);
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.s);
      var1.addAnimationController(this.E);
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
