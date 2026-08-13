package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.ao;
import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.api.IEllie;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.potion.HornyPotion;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ad;
import com.trolmastercard.sexmod.util.af;
import com.trolmastercard.sexmod.util.ah;
import com.trolmastercard.sexmod.util.ak;
import com.trolmastercard.sexmod.util.am;
import com.trolmastercard.sexmod.util.an;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.dz;







import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class EllieEntity extends AbstractGirlNpcEntity implements IEllie {
   static final float ad = 10.0F;
   static final int ao = 16;
   static final int ap = 79;
   static final int ag = 109;
   static final int as = 150;
   static final int ar = 20;
   static final int ab = 110;
   static final int an = 4;
   int ak = -1;
   boolean aq = false;
   boolean ae = false;
   boolean ac = false;
   int af = -1;
   int Y = -1;
   int al = -1;
   int ai = -1;
   boolean ah = false;
   Object[] am;
   int Z = -1;
   int aa = 1;
   boolean aj = false;

   public EllieEntity(World var1) {
      super(var1);
      this.P = -85;
      this.O = -175;
      this.K = -85;
      this.V = new Vec3d(-0.1, 0.05, 0.0);
   }

   @Override
   public void c_clash237() {
      this.sendChatMessage("Okay, I will be residing here then..");
      this.a(SoundHandler.GIRLS_ELLIE_HUH[0], 6.0F);
   }

   @Override
   public String getDisplayNameText() {
      return "Ellie";
   }

   @Override
   protected ResourceLocation func_184647_J() {
      return dz.a;
   }

   boolean i_clash474() {
      return this.isLocallyRegistered() ? false : this.field_70170_p.func_180495_p(this.func_180425_c().func_177982_a(0, 2, 0)).func_177230_c() != Blocks.field_150350_a;
   }

   public float func_70047_e() {
      return this.i_clash474() ? 1.53F : 1.9F;
   }

   @Override
   public float i_clash226() {
      return 0.4F;
   }

   @Override
   public void b_clash158() {
      UUID var1 = this.getInteractionPlayerUUID();
      if (var1 == null) {
         this.f_clash488();
      } else {
         EntityPlayer var2 = this.field_70170_p.func_152378_a(var1);
         if (var2 == null) {
            this.f_clash488();
         } else {
            float var3 = var2.field_70177_z - 180.0F;
            this.setYawRotation(var3);
            this.b(fp.CARRY_INTRO);
            this.setAnchored(true);
         }
      }
   }

   @Override
   public boolean t_clash283() {
      return this.getCurrentAction() != fp.CARRY_INTRO;
   }

   public boolean a(EntityPlayer var1, boolean var2) {
      if (var2) {
         a(var1, this, new String[]{"action.names.cowgirl", "action.names.missionary"}, false);
         return true;
      } else if ((Integer)this.m.func_187225_a(D) == 0) {
         a(var1, this, new String[]{"action.names.dressup"}, true);
         return true;
      } else {
         a(var1, this, new String[]{"Face fuck"}, true);
         return true;
      }
   }

   @Override
   public void x_clash475() {
      super.x_clash475();
      this.sendChatMessage("stay safe darling~");
      this.a(SoundHandler.GIRLS_ELLIE_SIGH[1], 6.0F);
   }

   @Override
   public void a(String var1, UUID var2) {
      super.a(var1, var2);
      this.aq = true;
      switch (var1) {
         case "action.names.missionary":
            this.b(fp.HUGSELECTED);
            this.changeDataParameterFromClient("animationFollowUp", "Missionary");
            break;
         case "action.names.cowgirl":
            this.b(fp.HUGSELECTED);
            this.changeDataParameterFromClient("animationFollowUp", "cowgirl");
            break;
         case "action.names.dressup":
         case "action.names.strip":
            this.b(fp.STRIP);
            this.changeDataParameterFromClient("animationFollowUp", "");
            break;
         case "Face fuck":
            this.a(true, true, var2);
            d3.setMovementLock(false);
      }
   }

   @Override
   protected void a(EntityPlayerMP var1, boolean var2) {
   }

   @Override
   public void b(fp var1) {
      fp var2 = this.getCurrentAction();
      if (var1 == fp.HUGSELECTED && !this.field_70170_p.field_72995_K) {
         this.ai = 79;
      }

      if (var2 != fp.MISSIONARY_CUM || var1 != fp.MISSIONARY_FAST && var1 != fp.MISSIONARY_SLOW) {
         if (var2 != fp.COWGIRLCUM || var1 != fp.COWGIRLSLOW && var1 != fp.COWGIRLFAST) {
            if (var2 != fp.CARRY_CUM || var1 != fp.CARRY_SLOW && var1 != fp.CARRY_FAST) {
               if (var1 == fp.CARRY_INTRO) {
                  this.ak = 0;
               }

               super.b(var1);
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void func_70071_h_() {
      super.func_70071_h_();
      if (this.ae) {
         this.a(Minecraft.func_71410_x().field_71439_g, true);
         this.ae = false;
      }

      this.m_clash478();
      this.h_clash476();
   }

   void h_clash476() {
      if (!HornyMeterHud.a_clash361()) {
         if (this.getCurrentAction() == fp.CARRY_SLOW) {
            HornyMeterHud.showHornyMeter();
         }
      }
   }

   void e_clash477() {
      if (this.ak != -1) {
         if (++this.ak >= 110) {
            this.ak = -1;
            if (this.getCurrentAction() == fp.CARRY_INTRO) {
               UUID var1 = this.getInteractionPlayerUUID();
               if (var1 != null) {
                  EntityPlayer var2 = this.field_70170_p.func_152378_a(var1);
                  if (var2 != null) {
                     float var3 = this.getYawRotation();
                     Vec3d var4 = this.getTargetPosition().func_178787_e(ck.rotateByYaw(new Vec3d(0.0, 2.5625F - var2.func_70047_e(), -0.3125), 180.0F + var3));
                     var2.func_70634_a(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c);
                  }
               }
            }
         }
      }
   }

   void m_clash478() {
      if (this.getCurrentAction() == fp.SITDOWNIDLE) {
         EntityPlayer var1 = this.field_70170_p.func_72890_a(this, 10.0);
         if (var1 != null) {
            if (!(this.func_70032_d(var1) > 1.5F)) {
               if (var1.getPersistentID().equals(Minecraft.func_71410_x().field_71439_g.getPersistentID())) {
                  BeeScreen.enableInteraction();
               }
            }
         }
      }
   }

   @Override
   public void func_70619_bc() {
      super.func_70619_bc();
      this.o_clash479();
      this.d_clash486();
      this.n_clash487();
      this.q_clash484();
      this.j_clash483();
      this.a_clash482();
      this.t_clash481();
      this.u_clash480();
   }

   void o_clash479() {
      if (!this.ac) {
         this.ac = true;
         this.field_70145_X = false;
         this.func_189654_d(false);
      }
   }

   @Override
   protected void U() {
      String var1 = (String)this.m.func_187225_a(h);
      if ("Missionary".equals(var1)) {
         this.m.func_187227_b(D, 0);
         this.b(fp.MISSIONARY_START);
         UUID var2 = this.getInteractionPlayerUUID();
         if (var2 == null) {
            return;
         }

         EntityPlayer var3 = this.field_70170_p.func_152378_a(var2);
         if (var3 == null) {
            this.r_clash533();
            return;
         }

         var3.func_189654_d(true);
         var3.field_70145_X = true;
         Vec3d var4 = this.getTargetPosition();
         var3.field_70177_z = this.getYawRotation();
         Vec3d var5 = ck.rotateByYaw(new Vec3d(0.0, 0.0, 0.1), var3.field_70177_z);
         var4 = var4.func_178787_e(var5);
         var3.func_70634_a(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c);
         PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var3);
      }

      if ("cowgirl".equals(var1)) {
         this.m.func_187227_b(D, 0);
         this.b(fp.COWGIRLSTART);
         UUID var6 = this.getInteractionPlayerUUID();
         if (var6 == null) {
            return;
         }

         EntityPlayer var7 = this.field_70170_p.func_152378_a(var6);
         if (var7 == null) {
            this.r_clash533();
            return;
         }

         var7.func_189654_d(true);
         var7.field_70145_X = true;
         Vec3d var9 = this.getTargetPosition();
         var7.field_70177_z = this.getYawRotation() + 180.0F;
         Vec3d var11 = ck.rotateByYaw(new Vec3d(0.0, 1.0 - var7.eyeHeight, -1.8125), var7.field_70177_z);
         var9 = var9.func_178787_e(var11);
         var7.func_70634_a(var9.field_72450_a, var9.field_72448_b, var9.field_72449_c);
         PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var7);
      }
   }

   void u_clash480() {
      if (--this.af == 0) {
         this.U();
      }
   }

   void t_clash481() {
      if (this.getCurrentAction() == fp.SITDOWNIDLE && this.af < 0) {
         EntityPlayer var1 = this.field_70170_p.func_72890_a(this, 10.0);
         if (var1 != null) {
            if (!(this.func_70032_d(var1) > 1.5F)) {
               this.af = 20;
               this.setInteractionPlayerUUID(var1.getPersistentID());
            }
         }
      }
   }

   void a_clash482() {
      if (--this.Y == 0) {
         this.b(fp.HUGIDLE);
      }
   }

   void j_clash483() {
      if (--this.al == 0) {
         this.b(fp.SITDOWNIDLE);
      }
   }

   void q_clash484() {
      if (--this.ai == 0 || this.ah) {
         this.ah = true;
         this.m.func_187227_b(G, false);
         this.b(fp.NULL);
         this.field_70145_X = false;
         this.func_189654_d(false);
         if (this.am == null) {
            this.am = this.g_clash485();
         }

         if (this.am == null) {
            this.h("no bed in sight...");
            this.field_70170_p.func_184133_a(null, this.func_180425_c(), SoundHandler.GIRLS_ELLIE_SIGH[0], SoundCategory.NEUTRAL, 6.0F, 1.0F);
            this.s();
            this.f_clash488();
         } else {
            EntityPlayer var1 = this.field_70170_p.func_152378_a(this.getInteractionPlayerUUID());
            if (var1 != null) {
               var1.func_189654_d(false);
               var1.field_70145_X = false;
            }

            Vec3d var2 = (Vec3d)this.am[0];
            int var3 = (Integer)this.am[1];
            if (var2.func_72438_d(this.func_174791_d()) > 1.0) {
               this.func_70661_as().func_75492_a(var2.field_72450_a, var2.field_72448_b, var2.field_72449_c, 0.35F);
               this.tickPathVelocity();
            } else {
               this.setTargetPosition(var2);
               this.setYawRotation(var3);
               this.b(fp.SITDOWN);
               this.m.func_187227_b(G, true);
               this.al = 109;
               this.field_70145_X = true;
               this.func_189654_d(true);
               this.ah = false;
               this.am = null;
            }
         }
      }
   }

   @Override
   public void reinitTasks() {
      super.reinitTasks();
      this.Y = -1;
   }

   Object[] g_clash485() {
      int var1 = -1;
      int var2 = 0;
      Vec3d[][] var4 = new Vec3d[][]{
         {new Vec3d(0.5, 0.0, -0.18), new Vec3d(0.0, 0.0, -1.0), new Vec3d(0.0, 0.0, 1.0)},
         {new Vec3d(0.5, 0.0, 1.18), new Vec3d(0.0, 0.0, 1.0), new Vec3d(0.0, 0.0, -1.0)},
         {new Vec3d(-0.18, 0.0, 0.5), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(1.0, 0.0, 0.0)},
         {new Vec3d(1.18, 0.0, 0.5), new Vec3d(1.0, 0.0, 0.0), new Vec3d(-1.0, 0.0, 0.0)}
      };
      int[] var5 = new int[]{0, 180, -90, 90};

      Vec3d var3;
      do {
         BlockPos var6 = this.a(this.func_180425_c(), ++var2);
         if (var6 == null) {
            return null;
         }

         var3 = new Vec3d(var6.func_177958_n(), var6.func_177956_o(), var6.func_177952_p());

         for (int var7 = 0; var7 < var4.length; var7++) {
            Vec3d var8 = var3.func_178787_e(var4[var7][1]);
            Block var9 = this.field_70170_p.func_180495_p(new BlockPos(var8.field_72450_a, var8.field_72448_b, var8.field_72449_c)).func_177230_c();
            Vec3d var10 = var3.func_178787_e(var4[var7][2]);
            Block var11 = this.field_70170_p.func_180495_p(new BlockPos(var10.field_72450_a, var10.field_72448_b, var10.field_72449_c)).func_177230_c();
            if (var9 == Blocks.field_150350_a && var11 == Blocks.field_150324_C) {
               if (var1 == -1) {
                  var1 = var7;
               } else {
                  double var12 = this.func_180425_c()
                     .func_177954_c(
                        var3.func_178787_e(var4[var1][0]).field_72450_a,
                        var3.func_178787_e(var4[var1][0]).field_72448_b,
                        var3.func_178787_e(var4[var1][0]).field_72449_c
                     );
                  double var14 = this.func_180425_c()
                     .func_177954_c(
                        var3.func_178787_e(var4[var7][0]).field_72450_a,
                        var3.func_178787_e(var4[var7][0]).field_72448_b,
                        var3.func_178787_e(var4[var7][0]).field_72449_c
                     );
                  if (var14 < var12) {
                     var1 = var7;
                  }
               }
            }
         }
      } while (var1 == -1);

      Vec3d var16 = var3.func_178787_e(var4[var1][0]);
      return new Object[]{var16, var5[var1]};
   }

   void d_clash486() {
      if (this.func_70660_b(HornyPotion.b) != null) {
         EntityPlayer var1 = this.field_70170_p.func_72890_a(this, 10.0);
         if (var1 != null) {
            this.func_184596_c(HornyPotion.b);
            this.setInteractionPlayerUUID(var1.getPersistentID());
            float var2 = (float)(Math.atan2(this.field_70161_v - var1.field_70161_v, this.field_70165_t - var1.field_70165_t) * (180.0 / Math.PI));
            this.setYawRotation(var2);
            this.setTargetPosition(this.func_174791_d());
            this.m.func_187227_b(G, true);
            this.b(fp.DASH);
            this.Z = 16;
            this.func_189654_d(true);
            this.field_70145_X = true;
            PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var1);
            this.field_70714_bg.func_85156_a(this.z);
            this.field_70714_bg.func_85156_a(this.o);
         }
      }
   }

   void n_clash487() {
      if (--this.Z == 0) {
         UUID var1 = this.getInteractionPlayerUUID();
         if (var1 == null) {
            this.f_clash488();
         } else {
            EntityPlayer var2 = this.field_70170_p.func_152378_a(var1);
            if (var2 == null) {
               this.f_clash488();
            } else {
               var2.func_189654_d(true);
               var2.field_70145_X = true;
               Vec3d var3 = ck.rotateByYaw(new Vec3d(0.0, 0.0, -0.5), var2.field_70177_z);
               Vec3d var4 = var3.func_178787_e(var2.func_174791_d());
               this.setTargetPosition(var4);
               this.setYawRotation(var2.field_70177_z);
               this.b(fp.HUG);
               this.Y = 150;
            }
         }
      }
   }

   void f_clash488() {
      this.m.func_187227_b(G, false);
      this.b(fp.NULL);
      this.setInteractionPlayerUUID(null);
      this.field_70145_X = false;
      this.func_189654_d(false);
      this.ah = false;
      this.Y = -1;
      this.Z = -1;
      this.ai = -1;
      this.am = null;
   }

   protected boolean func_184645_a(EntityPlayer var1, EnumHand var2) {
      if (d_clash532(var1) != null) {
         return false;
      }

      if (this.getInteractionPlayerUUID() != null) {
         return false;
      }

      if (this.field_70170_p.field_72995_K) {
         this.a(var1, false);
      }

      return true;
   }

   @Override
   protected fp getCumAction(fp var1) {
      if (var1 == fp.COWGIRLFAST || var1 == fp.COWGIRLSLOW) {
         return fp.COWGIRLCUM;
      } else if (var1 == fp.MISSIONARY_FAST || var1 == fp.MISSIONARY_SLOW) {
         return fp.MISSIONARY_CUM;
      } else {
         return var1 != fp.CARRY_SLOW && var1 != fp.CARRY_FAST ? null : fp.CARRY_CUM;
      }
   }

   @Override
   protected fp getNextAction(fp var1) {
      if (var1 == fp.COWGIRLSLOW) {
         return fp.COWGIRLFAST;
      } else if (var1 == fp.MISSIONARY_SLOW) {
         return fp.MISSIONARY_FAST;
      } else {
         return var1 == fp.CARRY_SLOW ? fp.CARRY_FAST : null;
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      if (this.field_70170_p instanceof SexWorldClient) {
         return null;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == fp.NULL && this.getCurrentAction().autoBlink) {
               this.a("animation.ellie.eyes", true, var1);
            } else {
               this.a("animation.ellie.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != fp.NULL) {
               this.a("animation.ellie.null", true, var1);
            } else {
               double var4 = Math.abs(this.field_70169_q - this.field_70165_t) + Math.abs(this.field_70166_s - this.field_70161_v);
               if (var4 == 0.0) {
                  this.a(this.i_clash474() ? "animation.ellie.crouchidle" : "animation.ellie.idle", true, var1);
               } else if (this.i_clash474()) {
                  this.a("animation.ellie.crouchwalk", true, var1);
               } else {
                  switch (this.q_clash489()) {
                     case RUN:
                        this.a("animation.ellie.run", true, var1);
                        return PlayState.CONTINUE;
                     case FAST_WALK:
                        this.a("animation.ellie.fastwalk", true, var1);
                        return PlayState.CONTINUE;
                     case WALK:
                        this.a("animation.ellie.walk", true, var1);
                  }
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.a("animation.ellie.null", true, var1);
                  break;
               case STRIP:
                  this.a("animation.ellie.strip", false, var1);
                  break;
               case DASH:
                  this.a("animation.ellie.dash", false, var1);
                  break;
               case HUG:
                  this.a("animation.ellie.hug", false, var1);
                  break;
               case HUGIDLE:
                  this.a("animation.ellie.hugidle", true, var1);
                  break;
               case HUGSELECTED:
                  this.a("animation.ellie.hugselected", false, var1);
                  break;
               case SITDOWN:
                  this.a("animation.ellie.sitdown", false, var1);
                  break;
               case SITDOWNIDLE:
                  this.a("animation.ellie.sitdownidle", true, var1);
                  break;
               case COWGIRLSTART:
                  this.a("animation.ellie.cowgirlstart", false, var1);
                  break;
               case COWGIRLSLOW:
                  this.a("animation.ellie.cowgirlslow2", true, var1);
                  break;
               case COWGIRLFAST:
                  this.a("animation.ellie.cowgirlfast", true, var1);
                  break;
               case COWGIRLCUM:
                  this.a("animation.ellie.cowgirlcum", true, var1);
                  break;
               case ATTACK:
                  this.a("animation.ellie.attack" + this.S, false, var1);
                  break;
               case BOW:
                  this.a("animation.ellie.bowcharge", false, var1);
                  break;
               case RIDE:
                  this.a("animation.ellie.ride", true, var1);
                  break;
               case SIT:
                  this.a("animation.ellie.sit", true, var1);
                  break;
               case THROW_PEARL:
                  this.a("animation.ellie.throwpearl", false, var1);
                  break;
               case DOWNED:
                  this.a("animation.ellie.downed", true, var1);
                  break;
               case MISSIONARY_START:
                  this.a("animation.ellie.missionary_start", false, var1);
                  break;
               case MISSIONARY_SLOW:
                  this.a("animation.ellie.missionary_slow", true, var1);
                  break;
               case MISSIONARY_FAST:
                  this.a("animation.ellie.missionary_fast", true, var1);
                  break;
               case MISSIONARY_CUM:
                  this.a("animation.ellie.missionary_cum", false, var1);
                  break;
               case CARRY_INTRO:
                  this.a("animation.ellie.carry_intro", false, var1);
                  break;
               case CARRY_SLOW:
                  this.a("animation.ellie.carry_slow" + this.aa, true, var1);
                  break;
               case CARRY_FAST:
                  this.a("animation.ellie.carry_fast", true, var1);
                  break;
               case CARRY_CUM:
                  this.a("animation.ellie.carry_cum", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData var1) {
      if (this.C == null) {
         this.p_clash506();
      }

      AnimationController.ISoundListener var2 = var1x -> {
         switch (var1x.sound) {
            case "becomeNude":
               if (this.isLocalPlayerNearby()) {
                  this.changeDataParameterFromClient("currentModel", this.m.func_187225_a(D) == 1 ? "0" : "1");
               }
               break;
            case "stripDone":
               this.b((fp) null);
               this.r_clash533();
               this.U();
               break;
            case "hugMSG2":
               this.h("Hmm...");
               this.a(SoundHandler.GIRLS_ELLIE_HMPH[3], 6.0F);
               break;
            case "hugMSG3":
               this.h("Hey!");
               this.a(SoundHandler.GIRLS_ELLIE_HUH[1], 1.0F);
               break;
            case "hugMSG4":
               this.h(I18n.func_135052_a("ellie.dialogue.mommyhorny", new Object[0]));
               this.a(SoundHandler.GIRLS_ELLIE_MOMMYHORNY, 0.5F);
               break;
            case "hugMSG5":
               this.h(I18n.func_135052_a("ellie.dialogue.whattodo", new Object[0]));
               this.a(SoundHandler.GIRLS_ELLIE_HUH[1], 6.0F);
               break;
            case "hugDone":
               if (this.isControlledByLocalPlayer()) {
                  this.a(Minecraft.func_71410_x().field_71439_g, true);
               }
               break;
            case "hugselectedMSG1":
               this.h(I18n.func_135052_a("ellie.dialogue.iknow", new Object[0]));
               this.a(SoundHandler.GIRLS_ELLIE_HMPH[3], 6.0F);
               break;
            case "hugselectedMSG2":
               this.h(I18n.func_135052_a("ellie.dialogue.followmedarling", new Object[0]));
               this.a(SoundHandler.GIRLS_ELLIE_GIGGLE[3], 6.0F);
               if (this.isControlledByLocalPlayer()) {
                  d3.setMovementLock(true);
               }
               break;
            case "sitdownMSG1":
               this.a(SoundHandler.GIRLS_ELLIE_COMETOMOMMY, 0.5F);
               if (this.isLocalPlayerNearby()) {
                  this.h(I18n.func_135052_a("ellie.dialogue.cometomommy", new Object[0]));
               }
               break;
            case "cowgirlStartMSG0":
               this.a(SoundHandler.GIRLS_ELLIE_GIGGLE[4], 6.0F);
               break;
            case "cowgirlStartMSG1":
               if (this.isLocalPlayerNearby()) {
                  this.sendChatMessage(I18n.func_135052_a("ellie.dialogue.like", new Object[0]));
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "cowgirlStartMSG2":
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 6.0F);
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "cowgirlStartDone":
               if (this.isControlledByLocalPlayer()) {
                  this.b(fp.COWGIRLSLOW);
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "cowgirlfastMSG1":
               if (this.aj) {
                  this.aj = false;
               } else {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 6.0F);
               }

               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "cowgirlfastDone":
               if (this.isControlledByLocalPlayer() && !d3.d) {
                  this.b(fp.COWGIRLSLOW);
               }
               break;
            case "cowgirlfastdomMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.2);
               }
               break;
            case "cowgirlcumMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 6.0F);
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               break;
            case "cowgirlcumMSG2":
               this.a(SoundHandler.GIRLS_ELLIE_MOAN[5], 3.0F);
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               break;
            case "cowgirlcumMSG3":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               break;
            case "cowgirlcumMSG4":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "cowgirlcumMSG5":
            case "missionary_cumMSG2":
               this.a(SoundHandler.GIRLS_ELLIE_GOODBOY, 0.5F);
               if (this.isControlledByLocalPlayer()) {
                  this.sendChatMessage(I18n.func_135052_a("ellie.dialogue.goodboy", new Object[0]));
               }
               break;
            case "cowgirlcumMSG6":
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "missionary_cumDone":
            case "cowgirlcumDone":
            case "carry_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.r_clash533();
               }
               break;
            case "attackSound":
               this.a(SoundEvents.field_187727_dV);
               break;
            case "attackDone":
               this.b(fp.NULL);
               if (++this.S == 3) {
                  this.S = 0;
               }
               break;
            case "pearl":
               PacketHandler.b.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               break;
            case "openSexUi":
               if (this.isLocalPlayerNearby()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "missionary_slowMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.func_70681_au().nextBoolean() && this.func_70681_au().nextBoolean()) {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_MOAN), 6.0F);
               } else {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 6.0F);
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "missionary_fastMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (!this.func_70681_au().nextBoolean() && !this.func_70681_au().nextBoolean()) {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 6.0F);
               } else {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_MOAN), 6.0F);
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.05);
               }
               break;
            case "missionary_startDone":
               if (this.isControlledByLocalPlayer()) {
                  this.b(fp.MISSIONARY_SLOW);
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "missionary_fastDone":
               if (this.isControlledByLocalPlayer() && !d3.d) {
                  this.b(fp.MISSIONARY_SLOW);
               }
               break;
            case "bedRustle":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               this.a(SoundHandler.MISC_BEDRUSTLE[0]);
               break;
            case "bedRustle1":
               this.a(SoundHandler.MISC_BEDRUSTLE[1]);
               break;
            case "missionary_cumMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 6.0F);
               break;
            case "carry_introMSG1":
               this.sendChatMessage("I'm hungry..");
               this.a(SoundHandler.GIRLS_ELLIE_HMPH, 6.0F);
               break;
            case "carry_introMSG2":
               this.sendChatMessage("heh~");
               this.a(SoundHandler.GIRLS_ELLIE_GIGGLE[3], 6.0F);
               break;
            case "lipsound":
               this.a(SoundHandler.GIRLS_ALLIE_LIPSOUND);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "cum":
               this.a(SoundHandler.MISC_INSERTS, 6.0F);
               this.a(SoundHandler.MISC_POUNDING);
               break;
            case "pound":
               this.a(SoundHandler.MISC_POUNDING);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "carry_slowDone":
               int var4 = this.aa;

               do {
                  this.aa = this.func_70681_au().nextInt(4) + 1;
               } while (this.aa == var4);

               return;
            case "carry_fastDone":
               if (this.isControlledByLocalPlayer() && !d3.d) {
                  this.b(fp.CARRY_SLOW);
               }
         }
      };
      this.C.registerSoundListener(var2);
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.E);
      var1.addAnimationController(this.s);
   }

}
