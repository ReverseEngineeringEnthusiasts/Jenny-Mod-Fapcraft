package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.GirlInventoryScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.AllieModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.client.renderer.KoboldRenderer;
import com.trolmastercard.sexmod.entity.api.IKobold;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.am;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.de;







import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
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
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.geckolib3.util.MatrixStack;

public class KoboldPlayerEntity extends AbstractKoboldPlayerEntity implements IKobold {
   public static final EyeAndKoboldColor aw = EyeAndKoboldColor.PURPLE;
   public static final DataParameter<Float> aA = EntityDataManager.func_187226_a(KoboldPlayerEntity.class, DataSerializers.field_187193_c)
      .func_187156_b()
      .func_187161_a(122);
   boolean aB = false;
   boolean az = true;
   boolean ay = false;
   int ax = 0;

   protected KoboldPlayerEntity(World var1) {
      super(var1);
   }

   public KoboldPlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      EyeAndKoboldColor var1 = EyeAndKoboldColor.values()[this.func_70681_au().nextInt(EyeAndKoboldColor.values().length)];
      this.m.func_187214_a(au, new BlockPos(var1.getMainColor()));
      this.m.func_187214_a(as, aw.name());
      this.m.func_187214_a(aA, 0.0F);
   }

   @Override
   public AxisAlignedBB a_clash352(EntityPlayer var1) {
      return new AxisAlignedBB(
         var1.field_70165_t - 0.3F,
         var1.field_70163_u,
         var1.field_70161_v - 0.3F,
         var1.field_70165_t + 0.3F,
         var1.field_70163_u + 0.9F,
         var1.field_70161_v + 0.3F
      );
   }

   @Override
   public void a_clash245(List<Integer> var1) {
      StringBuilder var2 = new StringBuilder();

      for (int var3 = 0; var3 < var1.size(); var3++) {
         int var4 = (Integer)var1.get(var3);
         switch (var3) {
            case 0:
               this.m.func_187227_b(aA, var4 / 100.0F * 0.25F);
               break;
            case 1:
               this.m.func_187227_b(as, EyeAndKoboldColor.values()[var4].toString());
               break;
            case 2:
               this.m.func_187227_b(au, new BlockPos(EyeAndKoboldColor.values()[var4].getMainColor()));
               break;
            default:
               AbstractNpcOnlyEntity.c(var2, var4);
         }
      }

      this.m.func_187227_b(at, var2.toString());
      if (this.field_70170_p.field_72995_K) {
         de.e_clash190();
      }
   }

   @Override
   public ArrayList<Integer> L_clash353() {
      ArrayList var1 = new ArrayList();
      var1.add(Math.round((Float)this.m.func_187225_a(aA) * 100.0F / 0.25F));
      var1.add(EyeAndKoboldColor.indexOf(EyeAndKoboldColor.safeValueOf((String)this.m.func_187225_a(as))));
      var1.add(EyeAndKoboldColor.indexOf(EyeAndKoboldColor.safeValueOf((Vec3i)this.m.func_187225_a(au))));
      return var1;
   }

   @Override
   protected String a(StringBuilder var1) {
      AbstractNpcOnlyEntity.b(var1, 8);
      AbstractNpcOnlyEntity.b(var1, 3);
      AbstractNpcOnlyEntity.b_clash224(var1);
      AbstractNpcOnlyEntity.b_clash224(var1);
      AbstractNpcOnlyEntity.a_clash223(var1, 2);
      AbstractNpcOnlyEntity.a_clash223(var1, 2);
      AbstractNpcOnlyEntity.a_clash223(var1, 1);
      AbstractNpcOnlyEntity.a_clash223(var1, 1);
      return var1.toString();
   }

   @Override
   public ArrayList<Integer> D_clash243() {
      return new ArrayList<Integer>() {
         {
            this.add(101);
            this.add(EyeAndKoboldColor.values().length);
            this.add(EyeAndKoboldColor.values().length);
            this.add(8);
            this.add(3);
            this.add(101);
            this.add(101);
            this.add(3);
            this.add(3);
            this.add(4);
            this.add(2);
         }
      };
   }

   @Override
   protected void a_clash354() {
      de.e_clash190();
      KoboldRenderer.clearBoneColors();
   }

   @Override
   public float i_clash226() {
      float var1 = 0.25F - (Float)this.m.func_187225_a(aA);
      return 1.4F - var1;
   }

   @Override
   public void b(String var1, UUID var2) {
      if ("anal".equals(var1)) {
         this.b_clash577(var2);
         this.b(fp.KOBOLD_ANAL_START);
         this.a(this.getOutfitIndex(), fp.KOBOLD_ANAL_START);
         this.f(0);
      }

      if ("oral".equals(var1)) {
         this.b_clash577(var2);
         this.b(fp.STARTBLOWJOB);
         this.a(this.getOutfitIndex(), fp.STARTBLOWJOB);
         this.f(0);
      }

      if ("mating".equals(var1)) {
         this.b_clash577(var2);
         this.b(fp.MATING_PRESS_START);
         this.a(this.getOutfitIndex(), fp.MATING_PRESS_START);
         this.f(0);
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      Minecraft.func_71410_x().func_147108_a(new GirlInventoryScreen(this, var1, new String[]{"anal", "oral", "mating"}, null, false));
      return true;
   }

   @Override
   public boolean a_clash355() {
      Block var1 = this.field_70170_p.func_180495_p(this.func_180425_c().func_177982_a(0, 1, 0)).func_177230_c();
      return !var1.func_176205_b(this.field_70170_p, this.func_180425_c().func_177982_a(0, 1, 0));
   }

   @Override
   protected MatrixStack a(MatrixStack var1) {
      float var2 = 0.25F - (Float)this.m.func_187225_a(aA);
      var1.scale(1.0F - var2, 1.0F - var2, 1.0F - var2);
      return var1;
   }

   @Override
   protected float a_clash356(float var1) {
      float var2 = 1.0F - (0.25F - (Float)this.m.func_187225_a(aA));
      return var1 * var2;
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new AllieModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return "textures/entity/kobold/hand.png";
   }

   @Override
   public Vec3i getHandColor(int var1) {
      try {
         return EyeAndKoboldColor.valueOf((String)this.m.func_187225_a(as)).getMainColor();
      } catch (Exception var3) {
         var3.printStackTrace();
         return super.getHandColor(var1);
      }
   }

   @Nullable
   @Override
   protected fp getNextAction(fp var1) {
      if (var1 == fp.SUCKBLOWJOB_BLINK) {
         return fp.THRUSTBLOWJOB;
      } else {
         return var1 == fp.KOBOLD_ANAL_SLOW ? fp.KOBOLD_ANAL_FAST : null;
      }
   }

   @Override
   protected fp getCumAction(fp var1) {
      if (var1 == fp.THRUSTBLOWJOB || var1 == fp.SUCKBLOWJOB_BLINK) {
         return fp.CUMBLOWJOB;
      } else if (var1 == fp.KOBOLD_ANAL_SLOW || var1 == fp.KOBOLD_ANAL_FAST) {
         return fp.KOBOLD_ANAL_CUM;
      } else {
         return var1 != fp.MATING_PRESS_HARD && var1 != fp.MATING_PRESS_SOFT ? null : fp.MATING_PRESS_CUM;
      }
   }

   @Override
   public void b(fp var1) {
      fp var2 = this.getCurrentAction();
      if (var2 != fp.MATING_PRESS_CUM || var1 != fp.MATING_PRESS_SOFT && var1 != fp.MATING_PRESS_HARD) {
         if (var2 != fp.KOBOLD_ANAL_CUM || var1 != fp.KOBOLD_ANAL_SLOW && var1 != fp.KOBOLD_ANAL_FAST) {
            if (var2 != fp.CUMBLOWJOB || var1 != fp.SUCKBLOWJOB && var1 != fp.THRUSTBLOWJOB) {
               super.b(var1);
            }
         }
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      if (this.field_70170_p instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      float var2 = 0.25F - (Float)this.func_184212_Q().func_187225_a(KoboldEntity.aE);
      GeckoLibCache.getInstance().parser.setValue("size", var2);
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == fp.NULL && this.getCurrentAction().autoBlink) {
               this.a("animation.kobold.blink", true, var1);
            } else {
               this.a("animation.kobold.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != fp.NULL) {
               this.a("animation.kobold.null", true, var1);
            } else if (this.ak) {
               this.a("animation.kobold.sit", true, var1);
            } else {
               if (this.E.getCurrentAnimation() != null && this.E.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.aB = !this.aB;
               }

               if (!this.af) {
                  this.a("animation.kobold.fly" + (this.aB ? "2" : ""), true, var1);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.E.setAnimationSpeed(1.2F);
                     this.a("animation.kobold.run", true, var1);
                  } else if (this.ao.y >= -0.1F) {
                     this.E.setAnimationSpeed(2.0);
                     this.a("animation.kobold.walk", true, var1);
                  } else {
                     this.E.setAnimationSpeed(1.75);
                     this.a("animation.kobold.backwards_walk", true, var1);
                  }
               } else {
                  this.a("animation.kobold.idle", true, var1);
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.a("animation.kobold.null", true, var1);
                  break;
               case STRIP:
                  this.a("animation.kobold.strip", false, var1);
                  break;
               case ATTACK:
                  this.a("animation.kobold.attack" + this.S, false, var1);
                  break;
               case BOW:
                  this.a("animation.kobold.bowcharge", false, var1);
                  break;
               case SIT:
                  this.a("animation.kobold.sit", true, var1);
                  break;
               case MINE:
                  this.a("animation.kobold.fall_tree", true, var1);
                  break;
               case PAYMENT:
                  this.a("animation.kobold.paymentBackpack", true, var1);
                  break;
               case STARTBLOWJOB:
                  this.a("animation.kobold.blowjobStart", false, var1);
                  break;
               case SUCKBLOWJOB_BLINK:
                  String var5 = this.az ? "R" : "L";
                  String var6 = this.ay ? "Switch" : "";
                  this.a("animation.kobold.blowjobSlow" + var5 + var6, true, var1);
                  break;
               case THRUSTBLOWJOB:
                  this.a("animation.kobold.blowjobFast", true, var1);
                  break;
               case CUMBLOWJOB:
                  this.a("animation.kobold.blowjobCum", false, var1);
                  break;
               case KOBOLD_ANAL_START:
                  this.a("animation.kobold.analStart", false, var1);
                  break;
               case KOBOLD_ANAL_SLOW:
                  this.a("animation.kobold.analSoft", true, var1);
                  break;
               case KOBOLD_ANAL_FAST:
                  this.a("animation.kobold.analHard", true, var1);
                  break;
               case KOBOLD_ANAL_CUM:
                  this.a("animation.kobold.analCum", true, var1);
                  break;
               case SLEEP:
                  this.a("animation.kobold.sleep", true, var1);
                  break;
               case MATING_PRESS_START:
                  this.a("animation.kobold.mating_press_start", false, var1);
                  break;
               case MATING_PRESS_SOFT:
                  this.a("animation.kobold.mating_press_soft", true, var1);
                  break;
               case MATING_PRESS_HARD:
                  this.a("animation.kobold.mating_press_hard", true, var1);
                  break;
               case MATING_PRESS_CUM:
                  this.a("animation.kobold.mating_press_cum", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

   void b(SoundEvent var1) {
      this.b(var1, 1.0F);
   }

   void b(SoundEvent[] var1) {
      this.b(var1, 1.0F);
   }

   void b(SoundEvent[] var1, float var2) {
      this.b(var1[this.func_70681_au().nextInt(var1.length)], var2);
   }

   void b(SoundEvent var1, float var2) {
      float var3 = 0.25F - (Float)this.m.func_187225_a(aA);
      double var4 = var3 / 0.25F;
      float var6 = (float)RotationHelper.b(0.9F, 1.1F, var4);
      this.a(var1, var2, var6);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData var1) {
      if (this.C == null) {
         this.p_clash506();
      }

      AnimationController.ISoundListener var2 = var1x -> {
         switch (var1x.sound) {
            case "attackDone":
               if (++this.S == 3) {
                  this.S = 0;
               }
               break;
            case "paymentMSG1":
               this.a(this.getInteractionPlayerUUID(), "I'd like to use ur services owo");
               this.a_clash588(SoundHandler.MISC_PLOB);
               break;
            case "plob":
               this.a_clash588(SoundHandler.MISC_PLOB);
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.b_clash732();
               }
               break;
            case "paymentDone":
               if (this.isControlledByLocalPlayer()) {
                  this.U();
               }
               break;
            case "blowjobStartMSG1":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var11 = Minecraft.func_71410_x().field_71439_g;
                  Vec3d var13 = ck.rotateByYaw(new Vec3d(0.0, 0.625 - var11.func_70047_e(), -1.0), this.getYawRotation() + 180.0F);
                  PacketHandler.b
                     .sendToServer(
                        new TeleportPlayerPacket(this.getInteractionPlayerUUID().toString(), this.getTargetPosition().func_178787_e(var13), this.getYawRotation() + 180.0F, 0.0F)
                     );
               }
               break;
            case "blowjobStartMSG2":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var10 = Minecraft.func_71410_x().field_71439_g;
                  Vec3d var12 = ck.rotateByYaw(new Vec3d(0.5, 0.5 - var10.func_70047_e(), -0.6875), this.getYawRotation() + 180.0F);
                  PacketHandler.b
                     .sendToServer(
                        new TeleportPlayerPacket(
                           this.getInteractionPlayerUUID().toString(), this.getTargetPosition().func_178787_e(var12), this.getYawRotation() + 180.0F - 40.0F, 0.0F
                        )
                     );
               }
               break;
            case "lipsound":
               if (this.func_70681_au().nextBoolean()) {
                  this.a(SoundHandler.GIRLS_ALLIE_LIPSOUND, 1.5F);
               } else {
                  this.a(SoundHandler.GIRLS_JENNY_LIPSOUND, 1.5F);
               }

               HornyMeterHud.addToHornyMeter(0.02F);
               break;
            case "touch":
               this.a_clash588(SoundHandler.MISC_TOUCH);
               break;
            case "blowjobStartDone":
               this.b(fp.SUCKBLOWJOB_BLINK);
               this.ay = false;
               this.az = true;
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "switch":
               this.ay = this.func_70681_au().nextBoolean();
               this.C.clearAnimationCache();
               break;
            case "endSwitch":
               this.ay = false;
               this.az = !this.az;
               this.C.clearAnimationCache();
               break;
            case "blowjobFastDone":
               if (this.isControlledByLocalPlayer() && !d3.d) {
                  this.b(fp.SUCKBLOWJOB_BLINK);
               }
               break;
            case "cumLoud":
               this.a(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "cumQuiet":
               this.a(SoundHandler.MISC_SMALLINSERTS, 1.5F);
               break;
            case "analCumDone":
            case "blowjobCumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.r_clash533();
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "analStartDone":
               this.b(fp.KOBOLD_ANAL_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "analStartCam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var9 = Minecraft.func_71410_x().field_71439_g;
                  Vec3d var5 = ck.rotateByYaw(new Vec3d(0.0, 0.5625 - var9.func_70047_e(), 0.5625), this.getYawRotation() + 180.0F);
                  PacketHandler.b
                     .sendToServer(new TeleportPlayerPacket(this.getInteractionPlayerUUID().toString(), this.getTargetPosition().func_178787_e(var5), this.getYawRotation(), 0.0F));
               }
               break;
            case "pounding":
               this.a_clash588(SoundHandler.MISC_POUNDING);
               break;
            case "analFastRapid":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  if (this.getCurrentAction() == fp.KOBOLD_ANAL_FAST) {
                     this.N();
                  } else {
                     this.b(fp.KOBOLD_ANAL_FAST);
                  }
               }
               break;
            case "analDone":
               if (this.getCurrentAction() == fp.KOBOLD_ANAL_FAST) {
                  this.b(fp.KOBOLD_ANAL_SLOW);
               }
               break;
            case "analHard":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "analSoft":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "cum":
               this.a(SoundHandler.MISC_SMALLINSERTS, 2.0F);
               break;
            case "giggle":
               this.b(SoundHandler.GIRLS_KOBOLD_GIGGLE);
               break;
            case "moan":
               this.b(SoundHandler.GIRLS_KOBOLD_MOAN);
               break;
            case "moanMating":
               this.ax--;
               if (this.ax <= 0) {
                  this.ax = 3;
                  this.b(SoundHandler.GIRLS_KOBOLD_MOAN);
               }
               break;
            case "analHardMSG1":
               this.ax--;
               if (this.ax <= 0) {
                  this.ax = 4;
                  this.b(SoundHandler.GIRLS_KOBOLD_MOAN);
               }
               break;
            case "orgasm":
               this.b(SoundHandler.GIRLS_KOBOLD_ORGASM);
               break;
            case "breath":
               this.b(SoundHandler.GIRLS_KOBOLD_LIGHTBREATHING, 0.5F);
               break;
            case "haa":
               this.b(SoundHandler.GIRLS_KOBOLD_HAA, 0.7F);
               break;
            case "interested":
               this.b(SoundHandler.GIRLS_KOBOLD_INTERESTED);
               break;
            case "yep":
               this.b(SoundHandler.GIRLS_KOBOLD_YEP);
               break;
            case "bjmoan":
               this.b(SoundHandler.randomSound(SoundHandler.GIRLS_KOBOLD_BJMOAN));
               break;
            case "blowjobStartbreath":
               int var6 = this.func_70681_au().nextInt(3);
               this.b(SoundHandler.GIRLS_KOBOLD_LIGHTBREATHING[var6]);
               break;
            case "matingCam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var8 = Minecraft.func_71410_x().field_71439_g;
                  Vec3d var16 = new Vec3d(0.0, 0.4375 - var8.eyeHeight, -0.6875);
                  var16 = ck.rotateByYaw(var16, this.getYawRotation() + 180.0F);
                  var16 = var16.func_178787_e(this.getTargetPosition());
                  PacketHandler.b.sendToServer(new TeleportPlayerPacket(var8.getPersistentID().toString(), var16, this.getYawRotation() + 180.0F, 10.0F));
               }
               break;
            case "mating_press_startDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
            case "mating_press_hardDone":
               if (this.isControlledByLocalPlayer()) {
                  this.b(fp.MATING_PRESS_SOFT);
               }
               break;
            case "mating_press_softReady":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.b(fp.MATING_PRESS_HARD);
               }
               break;
            case "mating_press_hardReady":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.N();
               }
               break;
            case "mating_cum_cam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var4 = Minecraft.func_71410_x().field_71439_g;
                  Vec3d var7 = new Vec3d(0.0, 1.1875 - var4.eyeHeight, 0.125);
                  var7 = ck.rotateByYaw(var7, this.getYawRotation() + 180.0F);
                  var7 = var7.func_178787_e(this.getTargetPosition());
                  PacketHandler.b.sendToServer(new TeleportPlayerPacket(var4.getPersistentID().toString(), var7, this.getYawRotation() + 180.0F, 70.0F));
               }
               break;
            case "cumMsg":
               this.sendChatMessage("I.. hope I am satisfying you sir");
               this.b(SoundHandler.GIRLS_KOBOLD_SAD[this.func_70681_au().nextInt(1)]);
               break;
            case "mating_press_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.r_clash533();
               }
         }
      };
      this.E.transitionLengthTicks = 3.0;
      this.C.registerSoundListener(var2);
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.E);
      var1.addAnimationController(this.s);
   }

}
