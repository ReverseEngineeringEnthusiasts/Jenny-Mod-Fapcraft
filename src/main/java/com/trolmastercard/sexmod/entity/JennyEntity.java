package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.entity.api.IEllie;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SendGirlToSexPacket;
import com.trolmastercard.sexmod.networking.SetPlayerForGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.potion.HornyPotion;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ad;
import com.trolmastercard.sexmod.util.af;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.fg;







import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class JennyEntity extends AbstractGirlNpcEntity implements IEllie, fg {
   public boolean Z = false;
   public boolean ab = false;
   public boolean af = false;
   public static final DataParameter<Boolean> Y = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187198_h)
      .func_187156_b()
      .func_187161_a(118);
   int ac = 0;
   int ad = 0;
   boolean aa = false;
   int ag = 0;
   boolean ae = false;

   public JennyEntity(World var1) {
      super(var1);
      this.func_70105_a(0.49F, 1.95F);
      this.P = 140;
      this.O = 50;
      this.K = 140;
      this.V = new Vec3d(0.0, -0.029999997854232782, -0.2);
   }

   public static JennyEntity a(World var0) {
      JennyEntity var1 = new JennyEntity(var0);
      var1.F = true;
      return var1;
   }

   @Override
   public String getDisplayNameText() {
      return "Jenny";
   }

   @Override
   public float i_clash226() {
      return -0.2F;
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      this.m.func_187214_a(Y, false);
   }

   @Override
   public void c_clash237() {
      this.sendChatMessage("Alright, this is my new Home~");
      this.a(SoundHandler.GIRLS_JENNY_HAPPYOH[1]);
   }

   public float func_70047_e() {
      return 1.64F;
   }

   protected SoundEvent func_184615_bR() {
      return SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_SIGH);
   }

   protected SoundEvent func_184601_bQ(DamageSource var1) {
      return null;
   }

   @Override
   public void func_70619_bc() {
      super.func_70619_bc();
      EntityPlayer var1 = this.field_70170_p.func_72890_a(this, 15.0);
      if (this.af && var1 != null && var1.func_174791_d().func_72438_d(this.func_174791_d()) < 0.5) {
         this.af = false;
         this.m.func_187227_b(BaseGirlEntity.y, this.field_70170_p.func_72890_a(this, 15.0).getPersistentID().toString());
         EntityPlayerMP var2 = this.func_184102_h().func_184103_al().func_177451_a(this.getInteractionPlayerUUID());
         this.m.func_187227_b(BaseGirlEntity.y, var2.getPersistentID().toString());
         var2.func_70634_a(this.func_174791_d().field_72450_a, this.func_174791_d().field_72448_b, this.func_174791_d().field_72449_c);
         this.a(var2, false);
         var2.func_191958_b(0.0F, 0.0F, 0.0F, 0.0F);
         this.positionPlayerRelative(0.0, 0.0, 0.4, 0.0F, 60.0F);
         this.B = null;
         this.b(fp.DOGGYSTART);
         PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), var2);
      }

      if (this.Z) {
         if (!(this.func_174791_d().func_72438_d(this.getTargetPosition()) < 0.6) && this.ad <= 200) {
            this.ad++;
            if (this.ad == 60 || this.ad == 120) {
               this.func_70661_as().func_75499_g();
               this.func_70661_as().func_75492_a(this.getTargetPosition().field_72450_a, this.getTargetPosition().field_72448_b, this.getTargetPosition().field_72449_c, 0.35);
            }
         } else {
            this.Z = false;
            this.m.func_187227_b(BaseGirlEntity.G, true);
            this.ad = 0;
            this.field_70145_X = true;
            this.func_189654_d(true);
            this.field_70159_w = 0.0;
            this.field_70181_x = 0.0;
            this.field_70179_y = 0.0;
            this.b(fp.STARTDOGGY);
         }
      }

      if (this.ab) {
         this.ac++;
         if (!this.func_174791_d().equals(BaseGirlEntity.e) && this.ac <= 40) {
            this.field_70177_z = this.getYawRotation();
            this.setTargetPosition(this.aa_clash545());
            this.func_189654_d(false);
            Vec3d var3 = RotationHelper.a(this.func_174791_d(), this.getTargetPosition(), 40 - this.ac);
            this.func_70107_b(var3.field_72450_a, var3.field_72448_b, var3.field_72449_c);
         } else {
            this.ab = false;
            this.ac = 0;
            this.setYawRotation(this.field_70170_p.func_73046_m().func_184103_al().func_177451_a(this.getInteractionPlayerUUID()).field_70177_z + 180.0F);
            this.m.func_187227_b(BaseGirlEntity.G, true);
            this.func_70661_as().func_75499_g();
            if ((Boolean)this.m.func_187225_a(Y)) {
               this.U();
               return;
            }

            this.b(fp.PAYMENT);
         }
      }
   }

   public boolean func_184645_a(EntityPlayer var1, EnumHand var2) {
      if (super.func_184645_a(var1, var2)) {
         return true;
      }

      if (this.field_70170_p.field_72995_K && !this.openInteractionMenu(var1)) {
         this.sendChatMessage(I18n.func_135052_a("jenny.dialogue.busy", new Object[0]));
      }

      return true;
   }

   @Override
   public void func_70071_h_() {
      super.func_70071_h_();
      if (!this.field_70170_p.field_72995_K) {
         this.m.func_187227_b(Y, this.func_70644_a(HornyPotion.b));
      }
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      if (this.getInteractionPlayerUUID() == null
         && (!this.J_clash526() || ((String)this.m.func_187225_a(BaseGirlEntity.v)).equals(Minecraft.func_71410_x().field_71439_g.getPersistentID().toString()))
         )
       {
         String[] var2 = new String[]{
            "action.names.blowjob",
            "action.names.boobjob",
            "action.names.doggy",
            this.m.func_187225_a(BaseGirlEntity.D) == 1 ? "action.names.strip" : "action.names.dressup"
         };
         if ((Boolean)this.m.func_187225_a(Y)) {
            BaseGirlEntity.a(var1, this, var2, true);
            return true;
         } else {
            BaseGirlEntity.a(
               var1,
               this,
               var2,
               new ItemStack[]{
                  new ItemStack(Items.field_151166_bC, 3),
                  new ItemStack(Items.field_151079_bi, 2),
                  new ItemStack(Items.field_151045_i, 2),
                  this.m.func_187225_a(BaseGirlEntity.D) == 1 ? new ItemStack(Items.field_151043_k, 1) : new ItemStack(Items.field_190931_a, 0)
               },
               true
            );
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public void a(String var1, UUID var2) {
      super.a(var1, var2);
      if ("action.names.blowjob".equals(var1)) {
         this.changeDataParameterFromClient("animationFollowUp", "blowjob");
         this.a(true, var2);
      } else if ("action.names.boobjob".equals(var1)) {
         this.changeDataParameterFromClient("animationFollowUp", "boobjob");
         this.a(true, var2);
      } else if ("action.names.doggy".equals(var1)) {
         this.changeDataParameterFromClient("animationFollowUp", "doggy");
         this.a(true, var2);
      } else if ("action.names.strip".equals(var1)) {
         this.changeDataParameterFromClient("animationFollowUp", "strip");
         this.a(true, var2);
      } else if ("action.names.dressup".equals(var1)) {
         this.b(fp.STRIP);
      }
   }

   protected void a(boolean var1, UUID var2) {
      super.a(var1, true, var2);
      d3.setMovementLock(false);
   }

   @Override
   public void a_clash292() {
      BlockPos var1 = this.a_clash525(this.func_180425_c());
      if (var1 == null) {
         this.a(SoundHandler.GIRLS_JENNY_HMPH[2]);
         this.sendChatMessage(I18n.func_135052_a("jenny.dialogue.nobedinsight", new Object[0]));
      } else {
         this.field_70714_bg.func_85156_a(this.z);
         this.field_70714_bg.func_85156_a(this.o);
         Vec3d var2 = new Vec3d(var1.func_177958_n(), var1.func_177956_o(), var1.func_177952_p());
         int[] var3 = new int[]{0, 180, -90, 90};
         Vec3d[][] var4 = new Vec3d[][]{
            {new Vec3d(0.5, 0.0, -0.5), new Vec3d(0.0, 0.0, -1.0)},
            {new Vec3d(0.5, 0.0, 1.5), new Vec3d(0.0, 0.0, 1.0)},
            {new Vec3d(-0.5, 0.0, 0.5), new Vec3d(-1.0, 0.0, 0.0)},
            {new Vec3d(1.5, 0.0, 0.5), new Vec3d(1.0, 0.0, 0.0)}
         };
         int var5 = -1;

         for (int var6 = 0; var6 < var4.length; var6++) {
            Vec3d var7 = var2.func_178787_e(var4[var6][1]);
            if (this.field_70170_p.func_180495_p(new BlockPos(var7.field_72450_a, var7.field_72448_b, var7.field_72449_c)).func_177230_c()
               == Blocks.field_150350_a) {
               if (var5 == -1) {
                  var5 = var6;
               } else {
                  double var8 = this.func_180425_c()
                     .func_177954_c(
                        var2.func_178787_e(var4[var5][0]).field_72450_a,
                        var2.func_178787_e(var4[var5][0]).field_72448_b,
                        var2.func_178787_e(var4[var5][0]).field_72449_c
                     );
                  double var10 = this.func_180425_c()
                     .func_177954_c(
                        var2.func_178787_e(var4[var6][0]).field_72450_a,
                        var2.func_178787_e(var4[var6][0]).field_72448_b,
                        var2.func_178787_e(var4[var6][0]).field_72449_c
                     );
                  if (var10 < var8) {
                     var5 = var6;
                  }
               }
            }
         }

         if (var5 == -1) {
            this.a(SoundHandler.GIRLS_JENNY_HMPH[2]);
            this.sendChatMessage(I18n.func_135052_a("jenny.dialogue.bedobscured", new Object[0]));
            return;
         }

         Vec3d var12 = var2.func_178787_e(var4[var5][0]);
         this.setAnchored(false);
         this.setYawRotation(var3[var5]);
         this.setTargetPosition(new Vec3d(var12.field_72450_a, var12.field_72448_b, var12.field_72449_c));
         this.r = this.getYawRotation();
         this.func_70661_as().func_75499_g();
         this.func_70661_as().func_75492_a(var12.field_72450_a, var12.field_72448_b, var12.field_72449_c, 0.35);
         this.Z = true;
         this.ad = 0;
      }
   }

   @Override
   public void b(fp var1) {
      fp var2 = this.getCurrentAction();
      if (var2 != fp.DOGGYCUM || var1 != fp.DOGGYSLOW && var1 != fp.DOGGYFAST) {
         if (var2 != fp.CUMBLOWJOB || var1 != fp.THRUSTBLOWJOB && var1 != fp.SUCKBLOWJOB) {
            if (var2 != fp.PAIZURI_CUM || var1 != fp.PAIZURI_SLOW && var1 != fp.PAIZURI_FAST) {
               super.b(var1);
               if (var2 == fp.STARTBLOWJOB || var2 == fp.PAIZURI_START) {
                  UUID var3 = this.getInteractionPlayerUUID();
                  if (var3 != null) {
                     EntityPlayer var4 = this.field_70170_p.func_152378_a(var3);
                     if (var4 != null) {
                        Vec3d var5 = ck.rotateByYaw(new Vec3d(0.0, 0.0, 0.2), this.getYawRotation() + 180.0F);
                        var4.func_70634_a(var4.field_70165_t + var5.field_72450_a, var4.field_70163_u, var4.field_70161_v + var5.field_72449_c);
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   protected fp getCumAction(fp var1) {
      if (var1 == fp.SUCKBLOWJOB || var1 == fp.THRUSTBLOWJOB) {
         this.positionPlayerRelative(0.0, 0.0, 0.0, 0.0F, 70.0F);
         return fp.CUMBLOWJOB;
      } else if (var1 == fp.DOGGYSLOW || var1 == fp.DOGGYFAST) {
         return fp.DOGGYCUM;
      } else {
         return var1 != fp.PAIZURI_FAST && var1 != fp.PAIZURI_SLOW ? null : fp.PAIZURI_CUM;
      }
   }

   @Override
   protected fp getNextAction(fp var1) {
      switch (var1) {
         case SUCKBLOWJOB:
            return fp.THRUSTBLOWJOB;
         case DOGGYSLOW:
            return fp.DOGGYFAST;
         case PAIZURI_SLOW:
            if (this.ae) {
               this.ae = false;
               this.positionPlayerRelative(0.0, 0.0, 0.2F, 0.0F, 70.0F);
            }

            return fp.PAIZURI_FAST;
         default:
            return null;
      }
   }

   @Override
   public void b_clash158() {
      this.ab = true;
   }

   @Override
   public void reinitTasks() {
      this.z = new EntityAIWanderAvoidWater(this, 0.35);
      this.o = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.field_70714_bg.func_75776_a(5, this.o);
      this.field_70714_bg.func_75776_a(5, this.z);
   }

   @Override
   protected void U() {
      switch ((String)this.m.func_187225_a(BaseGirlEntity.h)) {
         case "strip":
            this.s();
            this.b(fp.STRIP);
            break;
         case "blowjob":
            this.b(fp.STARTBLOWJOB);
            break;
         case "boobjob":
            if ((Integer)this.m.func_187225_a(BaseGirlEntity.D) != 0) {
               this.b(fp.STRIP);
               return;
            }

            this.b(fp.PAIZURI_START);
            break;
         case "doggy":
            if ((Integer)this.m.func_187225_a(BaseGirlEntity.D) != 0) {
               this.b(fp.STRIP);
               this.s();
               return;
            }

            this.r_clash533();
            if (this.field_70170_p.field_72995_K) {
               PacketHandler.b.sendToServer(new SendGirlToSexPacket(this.getGirlId()));
            } else {
               this.s();
               this.a_clash292();
            }
      }

      if (this.field_70170_p.field_72995_K) {
         this.changeDataParameterFromClient("animationFollowUp", "");
      } else {
         this.m.func_187227_b(BaseGirlEntity.h, "");
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
               this.a("animation.jenny.fhappy", true, var1);
            } else {
               this.a("animation.jenny.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != fp.NULL && this.getCurrentAction() != null) {
               this.a("animation.jenny.null", true, var1);
            } else if (this.func_184218_aH()) {
               this.a("animation.jenny.sit", true, var1);
            } else if (Math.abs(this.field_70169_q - this.field_70165_t) + Math.abs(this.field_70166_s - this.field_70161_v) > 0.0) {
               switch (this.q_clash489()) {
                  case RUN:
                     this.a("animation.jenny.run", true, var1);
                     break;
                  case FAST_WALK:
                     this.a("animation.jenny.fastwalk", true, var1);
                     break;
                  case WALK:
                     this.a("animation.jenny.walk", true, var1);
               }

               this.field_70177_z = this.field_70759_as;
            } else {
               this.a("animation.jenny.idle", true, var1);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case SUCKBLOWJOB:
                  this.a("animation.jenny.blowjobsuck", true, var1);
                  break;
               case DOGGYSLOW:
                  this.a("animation.jenny.doggyslow", true, var1);
                  break;
               case PAIZURI_SLOW:
                  this.a("animation.jenny.paizuri_slow", true, var1);
                  break;
               case NULL:
                  this.a("animation.jenny.null", true, var1);
                  break;
               case STRIP:
                  this.a("animation.jenny.strip", false, var1);
                  break;
               case PAYMENT:
                  this.a("animation.jenny.payment", false, var1);
                  break;
               case STARTBLOWJOB:
                  this.a("animation.jenny.blowjobintro", false, var1);
                  break;
               case THRUSTBLOWJOB:
                  this.a("animation.jenny.blowjobthrust", true, var1);
                  break;
               case CUMBLOWJOB:
                  this.a("animation.jenny.blowjobcum", false, var1);
                  break;
               case STARTDOGGY:
                  this.a("animation.jenny.doggygoonbed", false, var1);
                  break;
               case WAITDOGGY:
                  this.a("animation.jenny.doggywait", true, var1);
                  break;
               case DOGGYSTART:
                  this.a("animation.jenny.doggystart", false, var1);
                  break;
               case DOGGYFAST:
                  this.a("animation.jenny.doggyfast_" + (this.aa ? "hard" : "soft"), true, var1);
                  break;
               case DOGGYCUM:
                  this.a("animation.jenny.doggycum", false, var1);
                  break;
               case ATTACK:
                  this.a("animation.jenny.attack" + this.S, false, var1);
                  break;
               case BOW:
                  this.a("animation.jenny.bowcharge", false, var1);
                  break;
               case RIDE:
                  this.a("animation.jenny.ride", true, var1);
                  break;
               case SIT:
                  this.a("animation.jenny.sit", true, var1);
                  break;
               case THROW_PEARL:
                  this.a("animation.jenny.throwpearl", false, var1);
                  break;
               case DOWNED:
                  this.a("animation.jenny.downed", true, var1);
                  break;
               case PAIZURI_START:
                  this.a("animation.jenny.paizuri_start", false, var1);
                  break;
               case PAIZURI_FAST:
                  this.a("animation.jenny.paizuri_fast", true, var1);
                  break;
               case PAIZURI_CUM:
                  this.a("animation.jenny.paizuri_cum", false, var1);
                  break;
               case WAVE:
                  this.a("animation.jenny.wave", true, var1);
                  break;
               case WAVE_IDLE:
                  this.a("animation.jenny.wave_idle", true, var1);
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
            case "attackSound":
               this.a(SoundEvents.field_187727_dV);
               break;
            case "attackDone":
               this.b(fp.NULL);
               if (++this.S == 3) {
                  this.S = 0;
               }
               break;
            case "becomeNude":
               if (this.isLocalPlayerNearby()) {
                  this.changeDataParameterFromClient("currentModel", this.m.func_187225_a(BaseGirlEntity.D) == 1 ? "0" : "1");
               }
               break;
            case "stripDone":
               if (!((String)this.m.func_187225_a(BaseGirlEntity.h)).equals("boobjob")) {
                  this.r_clash533();
               }

               this.U();
               break;
            case "stripMSG1":
               this.h(I18n.func_135052_a("jenny.dialogue.hihi", new Object[0]));
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_GIGGLE));
               break;
            case "paymentMSG1":
               this.h(I18n.func_135052_a("jenny.dialogue.huh", new Object[0]));
               this.a(SoundHandler.GIRLS_JENNY_HUH[1]);
               break;
            case "paymentMSG2":
               this.a(SoundHandler.MISC_PLOB[0], 0.5F);
               String var4 = "<" + Minecraft.func_71410_x().field_71439_g.func_70005_c_() + "> ";
               switch ((String)this.m.func_187225_a(BaseGirlEntity.h)) {
                  case "strip":
                     this.b(var4 + I18n.func_135052_a("jenny.dialogue.showBobsandveganapls", new Object[0]), true);
                     return;
                  case "blowjob":
                     this.b(var4 + I18n.func_135052_a("jenny.dialogue.giveblowjob", new Object[0]), true);
                     return;
                  case "doggy":
                     this.b(var4 + I18n.func_135052_a("jenny.dialogue.givesex", new Object[0]), true);
                     return;
                  case "boobjob":
                     this.b(var4 + I18n.func_135052_a("jenny.dialogue.givebooba", new Object[0]), true);
                     return;
                  default:
                     this.b(var4 + "sex pls", true);
                     return;
               }
            case "paymentMSG3":
               this.h(I18n.func_135052_a("jenny.dialogue.hehe", new Object[0]));
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_GIGGLE));
               break;
            case "sexUiOn":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "paymentMSG4":
               this.a(SoundHandler.MISC_PLOB[0], 0.25F);
               break;
            case "paymentDone":
               this.U();
               break;
            case "bjiMSG1":
               this.h(I18n.func_135052_a("jenny.dialogue.blowjobtext1", new Object[0]));
               this.a(SoundHandler.GIRLS_JENNY_MMM[8]);
               this.r = this.field_70177_z + 180.0F;
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "bjiMSG2":
               this.h(I18n.func_135052_a("jenny.dialogue.blowjobtext2", new Object[0]));
               this.a(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[8]);
               break;
            case "bjiMSG3":
               this.h(I18n.func_135052_a("jenny.dialogue.blowjobtext3", new Object[0]));
               this.a(SoundHandler.GIRLS_JENNY_AFTERSESSIONMOAN[0]);
               break;
            case "bjiMSG4":
               this.a(SoundHandler.MISC_BELLJINGLE[0]);
               break;
            case "bjiMSG5":
               this.h(I18n.func_135052_a("jenny.dialogue.blowjobtext4", new Object[0]));
               this.a(SoundHandler.GIRLS_JENNY_HMPH[1], 0.5F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "bjiMSG6":
               this.h(I18n.func_135052_a("jenny.dialogue.blowjobtext5", new Object[0]));
               this.a(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[8]);
               break;
            case "bjiMSG7":
               this.h(I18n.func_135052_a("jenny.dialogue.blowjobtext6", new Object[0]));
               this.a(SoundHandler.GIRLS_JENNY_GIGGLE[4]);
               break;
            case "bjiMSG8":
               this.b(
                  "<" + Minecraft.func_71410_x().field_71439_g.func_70005_c_() + "> " + I18n.func_135052_a("jenny.dialogue.blowjobtext7", new Object[0]), true
               );
               this.a(SoundHandler.MISC_PLOB[0], 0.5F);
               break;
            case "bjiMSG9":
               this.h(I18n.func_135052_a("jenny.dialogue.blowjobtext8", new Object[0]));
               this.a(SoundHandler.GIRLS_JENNY_GIGGLE[2]);
               break;
            case "bjiMSG10":
               if (this.isControlledByLocalPlayer()) {
                  this.positionPlayerRelative(-0.65, -0.8, -0.25, 60.0F, -3.0F);
               }
               break;
            case "bjiMSG11":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.N();
               }

               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_LIPSOUND));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "bjiMSG12":
               if (Reference.f.nextInt(5) == 0) {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_BJMOAN));
               }

               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_LIPSOUND));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "bjtMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MMM));
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_LIPSOUND));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "bjiDone":
               this.b(fp.SUCKBLOWJOB);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "bjtDone":
               this.b(fp.SUCKBLOWJOB);
               break;
            case "doggyfastReady":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.N();
                  this.aa = true;
               }
               break;
            case "bjtReady":
            case "paizuriReady":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.N();
               }
               break;
            case "bjcMSG1":
               this.a(SoundHandler.GIRLS_JENNY_BJMOAN[1]);
               break;
            case "bjcMSG2":
               this.a(SoundHandler.GIRLS_JENNY_BJMOAN[7]);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "bjcMSG3":
               this.a(SoundHandler.GIRLS_JENNY_AFTERSESSIONMOAN[1]);
               break;
            case "bjcMSG4":
               this.a(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[0]);
               break;
            case "bjcMSG5":
               this.a(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[1]);
               break;
            case "bjcMSG6":
               this.a(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[2]);
               break;
            case "bjcMSG7":
               this.a(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[3]);
               break;
            case "bjcBlackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "bjcDone":
            case "paizuri_cumDone":
            case "doggyCumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.r_clash533();
               }
               break;
            case "doggyGoOnBedMSG1":
               this.a(SoundHandler.MISC_BEDRUSTLE[0]);
               this.r = this.field_70177_z;
               break;
            case "doggyGoOnBedMSG2":
               this.sendChatMessage(I18n.func_135052_a("jenny.dialogue.doggytext1", new Object[0]));
               this.a(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[9]);
               break;
            case "doggyGoOnBedMSG3":
               this.sendChatMessage(I18n.func_135052_a("jenny.dialogue.doggytext2", new Object[0]));
               this.a(SoundHandler.GIRLS_JENNY_GIGGLE[0]);
               break;
            case "doggyGoOnBedMSG4":
               this.a(SoundHandler.MISC_SLAP[0], 0.75F);
               break;
            case "doggyGoOnBedDone":
               PacketHandler.b.sendToServer(new SetPlayerForGirlPacket(this.getGirlId(), Minecraft.func_71410_x().field_71439_g.getPersistentID()));
               this.b(fp.WAITDOGGY);
               break;
            case "doggystartMSG1":
               this.a(SoundHandler.MISC_TOUCH[0]);
               break;
            case "doggystartMSG2":
               this.a(SoundHandler.MISC_TOUCH[1]);
               break;
            case "doggystartMSG3":
               this.a(SoundHandler.MISC_BEDRUSTLE[1], 0.5F);
               break;
            case "doggystartMSG4":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_SMALLINSERTS));
               this.a(SoundHandler.GIRLS_JENNY_MMM[1]);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "doggystartMSG5":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.33F);
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MOAN));
               break;
            case "doggystartDone":
               this.b(fp.DOGGYSLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "doggyslowMSG1":
               this.aa = false;
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.33F);
               int var5 = Reference.f.nextInt(4);
               if (var5 == 0) {
                  var5 = Reference.f.nextInt(2);
                  if (var5 == 0) {
                     this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MMM));
                  } else {
                     this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MOAN));
                  }
               } else {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.00666);
               }
               break;
            case "doggyslowMSG2":
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING), 0.5F);
               break;
            case "doggyfastMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }

               this.ag++;
               if (this.ag % 2 == 0) {
                  int var10 = Reference.f.nextInt(2);
                  if (var10 == 0) {
                     this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MOAN));
                  } else {
                     this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING));
                  }
               } else {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_AHH));
               }
               break;
            case "doggyfastDone":
               this.aa = false;
               this.b(fp.DOGGYSLOW);
               break;
            case "doggycumMSG1":
               this.a(SoundHandler.MISC_CUMINFLATION[0], 2.0F);
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 2.0F);
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MOAN));
               break;
            case "doggycumMSG2":
               this.a(SoundHandler.GIRLS_JENNY_HEAVYBREATHING[4]);
               break;
            case "doggycumMSG3":
               this.a(SoundHandler.GIRLS_JENNY_HEAVYBREATHING[5]);
               break;
            case "doggycumMSG4":
               this.a(SoundHandler.GIRLS_JENNY_HEAVYBREATHING[6]);
               break;
            case "doggycumMSG5":
               this.a(SoundHandler.GIRLS_JENNY_HEAVYBREATHING[7]);
               break;
            case "pearl":
               PacketHandler.b.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               break;
            case "boobjob_camera":
               UUID var6 = Minecraft.func_71410_x().field_71439_g.getPersistentID();
               if (var6.equals(this.field_70170_p.func_72890_a(this.af_clash520(), 2.0).getPersistentID())) {
                  this.r = this.field_70170_p.func_152378_a(var6).field_70177_z;
                  this.setInteractionPlayerUUID(var6);
                  if (!this.ae) {
                     this.ae = true;
                     this.positionPlayerRelative(-0.7, -0.6, 0.2, 60.0F, -3.0F);
                  }
               }
               break;
            case "paizuri_startDone":
               if (this.isControlledByLocalPlayer()) {
                  this.b(fp.PAIZURI_SLOW);
                  HornyMeterHud.resetHornyMeter();
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "paizuriFastMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.func_70681_au().nextBoolean()) {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MMM));
               } else {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_AHH));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "paizuriSlowMSG1":
            case "paizuriStartMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "paizuri_fastDone":
               this.b(fp.PAIZURI_SLOW);
               if (this.isControlledByLocalPlayer() && !this.ae) {
                  this.ae = true;
                  this.positionPlayerRelative(-0.7, -0.6, 0.2, 60.0F, -3.0F);
               }
               break;
            case "paizuri_startStep":
               IBlockState var7 = this.field_70170_p.func_180495_p(this.func_180425_c().func_177973_b(new Vec3i(0, 1, 0)));
               this.a(var7.func_177230_c().getSoundType(var7, this.field_70170_p, this.func_180425_c(), this).func_185844_d());
               break;
            case "paizuri_cumStart":
               if (this.isControlledByLocalPlayer() && !this.ae) {
                  this.positionPlayerRelative(-0.7, -0.6, 0.2, 60.0F, -3.0F);
               }
         }
      };
      this.C.registerSoundListener(var2);
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.E);
      var1.addAnimationController(this.s);
   }

}
