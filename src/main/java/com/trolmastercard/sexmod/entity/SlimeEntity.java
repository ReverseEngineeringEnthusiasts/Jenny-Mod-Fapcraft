package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.potion.HornyPotion;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.dz;







import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class SlimeEntity extends BaseGirlEntity {
   static final double Q = 0.7F;
   static final float W = 0.9F;
   static final double M = 100.0;
   static final float L = 0.1F;
   static final int O = 2400;
   SlimeEntity.SlimeEntityState S = SlimeEntity.SlimeEntityState.IDLE;
   public static DataParameter<Integer> U = EntityDataManager.func_187226_a(SlimeEntity.class, DataSerializers.field_187192_b)
      .func_187156_b()
      .func_187161_a(113);
   public static DataParameter<Float> R = EntityDataManager.func_187226_a(SlimeEntity.class, DataSerializers.field_187193_c).func_187156_b().func_187161_a(112);
   public static DataParameter<Integer> T = EntityDataManager.func_187226_a(SlimeEntity.class, DataSerializers.field_187192_b)
      .func_187156_b()
      .func_187161_a(111);
   int N = 0;
   boolean K = true;
   boolean V = false;
   int P = 0;

   public SlimeEntity(World var1) {
      super(var1);
   }

   @Override
   public String c_clash241() {
      return "Slime";
   }

   @Override
   public float i_clash226() {
      return 1.6F;
   }

   @Override
   public void b(fp var1) {
      if (this.y_clash492() != fp.CUMBLOWJOB || var1 != fp.THRUSTBLOWJOB && var1 != fp.SUCKBLOWJOB) {
         if (this.y_clash492() != fp.DOGGYCUM || var1 != fp.DOGGYFAST && var1 != fp.DOGGYSLOW) {
            super.b(var1);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean t_clash283() {
      return false;
   }

   @Override
   protected void func_184651_r() {
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      this.func_184212_Q().func_187214_a(T, 0);
      this.func_184212_Q().func_187214_a(R, 0.0F);
      this.func_184212_Q().func_187214_a(U, -1);
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
   protected fp c_clash235(fp var1) {
      if (var1 == fp.SUCKBLOWJOB) {
         return fp.THRUSTBLOWJOB;
      } else {
         return var1 == fp.DOGGYSLOW ? fp.DOGGYFAST : null;
      }
   }

   protected float func_175134_bD() {
      return 0.9F;
   }

   @Override
   public void func_70014_b(NBTTagCompound var1) {
      super.func_70014_b(var1);
      var1.func_74768_a("hornyLevel", (Integer)this.m.func_187225_a(T));
      var1.func_74768_a("ticksUntilBirth", (Integer)this.m.func_187225_a(U));
   }

   @Override
   public void func_70037_a(NBTTagCompound var1) {
      super.func_70037_a(var1);
      this.m.func_187227_b(T, var1.func_74762_e("hornyLevel"));
      this.m.func_187227_b(U, var1.func_74762_e("ticksUntilBirth"));
      if ((Integer)this.m.func_187225_a(T) != 0) {
         this.m.func_187227_b(D, 0);
      }

      this.field_70145_X = false;
      this.func_189654_d(false);
   }

   @Override
   protected ResourceLocation func_184647_J() {
      return dz.b;
   }

   @Override
   public void g_clash238() {
      this.m.func_187227_b(T, 0);
      this.m.func_187227_b(D, 1);
   }

   @Override
   public void func_70619_bc() {
      super.func_70619_bc();
      this.a_clash725();
      this.c_clash724();
      if (this.func_70644_a(HornyPotion.b) && this.S == SlimeEntity.SlimeEntityState.IDLE && (Integer)this.m.func_187225_a(U) == -1) {
         this.m.func_187227_b(T, 2);
         if ((Integer)this.m.func_187225_a(D) == 1) {
            this.b(fp.UNDRESS);
         }

         this.func_184589_d(HornyPotion.b);
      }
   }

   @Override
   public void func_70071_h_() {
      super.func_70071_h_();
      if (this.y_clash492() == fp.NULL) {
         this.b_clash726();
      }

      if ((Integer)this.m.func_187225_a(T) >= 2 && this.field_70173_aa % 10 == 0) {
         a(EnumParticleTypes.HEART, this);
      }

      if (this.field_70170_p.field_72995_K) {
         this.d_clash723();
         this.i_clash722();
      }
   }

   @SideOnly(Side.CLIENT)
   void i_clash722() {
      if (this.ae_clash498() != null) {
         EntityPlayerSP var1 = Minecraft.func_71410_x().field_71439_g;
         if (this.ae_clash498().equals(var1.getPersistentID())) {
            Vec3d var2 = this.func_174791_d();
            Vec3d var3 = ck.a_clash306(new Vec3d(0.0, 0.0, 0.65F), this.I_clash415());
            var2 = var2.func_178787_e(var3);
            var1.func_70107_b(var2.field_72450_a, var2.field_72448_b, var2.field_72449_c);
            var1.func_70016_h(0.0, 0.0, 0.0);
         }
      }
   }

   void d_clash723() {
      int var1 = (Integer)this.m.func_187225_a(U);
      if (var1 != -1) {
         a(EnumParticleTypes.SPELL_WITCH, this);
         if (var1 == 0) {
            this.a(SoundHandler.MISC_PLOB[0]);
         }
      }
   }

   void c_clash724() {
      int var1 = (Integer)this.m.func_187225_a(U);
      if (var1 != -1) {
         this.m.func_187227_b(U, var1 - 1);
         if (--var1 < 0) {
            WildSlimeEntity var2 = new WildSlimeEntity(this.field_70170_p);
            var2.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
            this.field_70170_p.func_72838_d(var2);
            this.m.func_187227_b(U, -1);
         }
      }
   }

   void a_clash725() {
      int var1 = (Integer)this.m.func_187225_a(T);
      if (var1 >= 2) {
         if (var1 >= 4 && this.field_70122_E && this.y_clash492() == fp.NULL) {
            this.c_clash502(this.func_174791_d());
            this.b_clash431(this.field_70177_z);
            this.m.func_187227_b(G, true);
            this.func_189654_d(true);
            this.field_70145_X = true;
            this.b(fp.STARTDOGGY);
         } else {
            EntityPlayer var2 = this.field_70170_p.func_72890_a(this, 1.0);
            if (var2 != null && var2.field_70122_E && d_clash532(var2) == null) {
               this.c_clash502(this.func_174791_d());
               this.b_clash431(this.field_70177_z);
               this.m.func_187227_b(G, true);
               this.func_189654_d(true);
               this.field_70145_X = true;
               var2.func_189654_d(true);
               var2.field_70145_X = true;
               PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var2);
               this.e_clash499(var2.getPersistentID());
               var2.field_70177_z = this.I_clash415();
               Vec3d var3 = ck.a_clash306(new Vec3d(0.0, 0.0, 0.65F), this.I_clash415());
               var2.func_70107_b(this.field_70165_t + var3.field_72450_a, this.field_70163_u, this.field_70161_v + var3.field_72449_c);
               if (this.y_clash492() == fp.WAITDOGGY) {
                  this.b(fp.DOGGYSTART);
               } else {
                  this.b(fp.SUCKBLOWJOB);
               }
            }
         }
      }
   }

   void b_clash726() {
      if (this.field_70170_p.field_72995_K) {
         if (this.N == 90.0) {
            this.S = SlimeEntity.SlimeEntityState.JUMP_START;
         }

         if (!this.K && this.field_70122_E) {
            this.S = SlimeEntity.SlimeEntityState.JUMP_END;
            this.N = 0;
         }

         float var1 = (Float)this.m.func_187225_a(R);
         this.field_70177_z = var1;
         this.field_70759_as = var1;
         this.field_70761_aq = var1;
      } else {
         if (this.N == 85.0) {
            this.m.func_187227_b(R, this.e_clash728());
         }

         if (this.N == 100.0) {
            this.h_clash727();
         }

         if (!this.K && this.field_70122_E) {
            this.V = (Integer)this.m.func_187225_a(U) == -1 && this.func_70681_au().nextFloat() < 0.1F;
         }

         if (this.V && this.N == 50) {
            int var3 = (Integer)this.m.func_187225_a(T);
            int var2 = var3 + 1;
            this.m.func_187227_b(T, var2);
            if (var2 == 1) {
               this.b(fp.UNDRESS);
            }
         }
      }

      if (this.field_70122_E) {
         this.N++;
      }

      this.K = this.field_70122_E;
   }

   void h_clash727() {
      this.field_70159_w = 0.0;
      this.field_70181_x = 0.0;
      this.field_70179_y = 0.0;
      this.func_70664_aZ();
      float var1 = (Float)this.m.func_187225_a(R);
      this.field_70177_z = var1;
      this.field_70126_B = var1;
      Vec3d var2 = new Vec3d(0.0, 0.0, 0.7F);
      var2 = ck.a_clash306(var2, var1);
      this.field_70159_w = var2.field_72450_a;
      this.field_70179_y = var2.field_72449_c;
      this.N = 0;
   }

   float e_clash728() {
      int var1 = (Integer)this.m.func_187225_a(T);
      if ((Integer)this.m.func_187225_a(U) != -1) {
         return this.f_clash729();
      } else if (var1 < 2) {
         return this.f_clash729();
      } else {
         EntityPlayer var2 = this.field_70170_p.func_72890_a(this, 30.0);
         if (var2 == null) {
            return this.f_clash729();
         } else {
            return d_clash532(var2) != null
               ? this.f_clash729()
               : (float)Math.atan2(this.field_70161_v - var2.field_70161_v, this.field_70165_t - var2.field_70165_t) * (float) (180.0 / Math.PI) + 90.0F;
         }
      }
   }

   float f_clash729() {
      return Reference.f.nextFloat() * 360.0F;
   }

   public void func_180430_e(float var1, float var2) {
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      if (this.field_70170_p instanceof SexWorldClient) {
         return null;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.y_clash492() != fp.NULL && this.y_clash492().autoBlink) {
               this.a("animation.slime.fhappy", true, var1);
            } else {
               this.a("animation.slime.null", true, var1);
            }
            break;
         case "action":
            if (this.y_clash492() == fp.NULL) {
               this.a(this.S.a, true, var1);
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
                  case STARTBLOWJOB:
                     this.a("animation.slime.blowjobintro", false, var1);
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
               }
            }
      }

      return PlayState.CONTINUE;
   }

   @Override
   public void registerControllers(AnimationData var1) {
      AnimationController.ISoundListener var2 = var1x -> {
         switch (var1x.sound) {
            case "undress":
               if (this.e_clash544()) {
                  this.a_clash490("currentModel", "0");
                  this.b(fp.NULL);
               }
               break;
            case "dress":
               if (this.e_clash544()) {
                  this.m.func_187227_b(D, 1);
                  this.b((fp) null);
                  this.r_clash533();
               }
               break;
            case "becomeNude":
               this.m.func_187227_b(D, 0);
               break;
            case "sexUiOn":
               if (this.n_clash537() && !HornyMeterHud.d) {
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
            case "bjtReady":
            case "doggyfastReady":
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
                  this.a_clash490("pregnant", String.valueOf(2400));
               }
               break;
            case "doggyGoOnBedMSG1":
               this.a(SoundEvents.field_187886_fs);
               this.r = this.field_70177_z;
               break;
            case "doggyGoOnBedDone":
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
               int var4 = Reference.f.nextInt(4);
               if (var4 == 0) {
                  var4 = Reference.f.nextInt(2);
                  if (var4 == 0) {
                     this.a(SoundEvents.field_187882_fq);
                  } else {
                     this.a(SoundEvents.field_187886_fs);
                  }
               } else {
                  this.a(SoundEvents.field_187878_fo);
               }

               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.02);
               }
               break;
            case "doggyfastMSG1":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.04);
               }

               this.P++;
               if (this.P % 2 == 0) {
                  int var5 = Reference.f.nextInt(2);
                  if (var5 == 0) {
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
               break;
            case "jumpStart":
               this.a(SoundEvents.field_187882_fq);
               break;
            case "jumpStartDone":
               this.S = SlimeEntity.SlimeEntityState.JUMP_AIR;
               break;
            case "jumpEndSound":
               this.a(SoundEvents.field_187886_fs);
               break;
            case "jumpEndDone":
               this.S = SlimeEntity.SlimeEntityState.IDLE;
         }
      };
      this.C.registerSoundListener(var2);
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.s);
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }

   enum SlimeEntityState {
      IDLE("animation.slime.idle"),
      JUMP_START("animation.slime.jumpstart"),
      JUMP_AIR("animation.slime.jumpair"),
      JUMP_END("animation.slime.jumpend");

      String a;

      public String a_clash867() {
         return this.a;
      }

      SlimeEntityState(String var3) {
         this.a = var3;
      }
   }
}
