package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.SlimeModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SetPlayerForGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.eh;







import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class JennyPlayerEntity extends AbstractPlayerGirlEntity {
   boolean ap = false;
   boolean ar = false;
   int aq = 0;
   boolean as = false;

   protected JennyPlayerEntity(World var1) {
      super(var1);
   }

   public JennyPlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public float i_clash226() {
      return 1.75F;
   }

   @Override
   public float getLeftArmAngle() {
      return 35.0F;
   }

   @Override
   public float getRightArmAngle() {
      return 140.0F;
   }

   public float getEyeHeight() {
      return 1.64F;
   }

   @Override
   public void u_clash377() {
      this.b(fp.STARTDOGGY);
      this.entityDataManager.set(BaseGirlEntity.OUTFIT_INDEX, 0);
      this.cameraYaw = (Float)this.entityDataManager.get(BaseGirlEntity.YAW_ROTATION);
   }

   @Override
   public boolean A_clash381() {
      return false;
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new SlimeModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return var1 == 0 ? "textures/entity/jenny/hand_nude.png" : "textures/entity/jenny/hand.png";
   }

   @Override
   public void b(String var1, UUID var2) {
      if ("action.names.boobjob".equals(var1)) {
         this.entityDataManager.set(BaseGirlEntity.OUTFIT_INDEX, 0);
         this.b(fp.PAIZURI_START);
         this.a(0, fp.PAIZURI_START);
         this.b_clash577(var2);
      }

      if ("action.names.blowjob".equals(var1)) {
         this.b(fp.STARTBLOWJOB);
         this.a(this.getOutfitIndex(), fp.PAIZURI_START);
         this.b_clash577(var2);
      }
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (this.getCurrentAction() == fp.WAITDOGGY) {
         EntityPlayer var1 = this.j_clash575();
         if (var1 != null && var1.getDistance(this.w_clash576().x, this.w_clash576().y, this.w_clash576().z) < 1.0) {
            if (this.c_clash587(var1.getPersistentID())) {
               var1.sendMessage(new TextComponentString(TextFormatting.DARK_PURPLE + "sowy no lesbo action yet uwu"));
               return;
            }

            this.setInteractionPlayerUUID(var1.getPersistentID());
            var1.setPositionAndUpdate(this.getPositionVector().x, this.w_clash576().y, this.getPositionVector().z);
            this.a((EntityPlayerMP)var1, false);
            var1.moveRelative(0.0F, 0.0F, 0.0F, 0.0F);
            var1.capabilities.isFlying = true;
            this.world.getPlayerEntityByUUID(this.getOwnerUserUUID()).capabilities.isFlying = true;
            this.positionPlayerRelative(0.0, 0.0, 0.4, 0.0F, 60.0F);
            this.cameraOriginPos = null;
            this.b(fp.DOGGYSTART);
            PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var1);
         }
      }
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      BaseGirlEntity.a(var1, this, new String[]{"action.names.blowjob", "action.names.boobjob"}, false);
      return true;
   }

   @Override
   protected fp getNextAction(fp var1) {
      switch (var1) {
         case SUCKBLOWJOB:
            return fp.THRUSTBLOWJOB;
         case DOGGYSLOW:
            return fp.DOGGYFAST;
         case PAIZURI_SLOW:
            if (this.as) {
               this.as = false;
               this.positionPlayerRelative(0.0, 0.0, 0.0, 0.0F, 70.0F);
            }

            return fp.PAIZURI_FAST;
         default:
            return null;
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
   public void b(fp var1) {
      fp var2 = this.getCurrentAction();
      if (var2 != fp.DOGGYCUM || var1 != fp.DOGGYSLOW && var1 != fp.DOGGYFAST) {
         if (var2 != fp.CUMBLOWJOB || var1 != fp.THRUSTBLOWJOB && var1 != fp.SUCKBLOWJOB) {
            if (var2 != fp.PAIZURI_CUM || var1 != fp.PAIZURI_SLOW && var1 != fp.PAIZURI_FAST) {
               super.b(var1);
            }
         }
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == fp.NULL && this.getCurrentAction().autoBlink) {
               this.a("animation.jenny.fhappy", true, var1);
            } else {
               this.a("animation.jenny.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != fp.NULL) {
               this.a("animation.jenny.null", true, var1);
            } else if (this.ak) {
               this.a("animation.jenny.sit", true, var1);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.ap = !this.ap;
               }

               if (!this.af) {
                  this.a("animation.jenny.fly" + (this.ap ? "2" : ""), true, var1);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.movementController.setAnimationSpeed(1.2F);
                     this.a("animation.jenny.run", true, var1);
                  } else if (this.ao.y >= -0.1F) {
                     this.movementController.setAnimationSpeed(1.5);
                     this.a("animation.jenny.fastwalk", true, var1);
                  } else {
                     this.movementController.setAnimationSpeed(1.2F);
                     this.a("animation.jenny.backwards_walk", true, var1);
                  }
               } else {
                  this.a("animation.jenny.idle", true, var1);
               }
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
                  this.a("animation.jenny.doggyfast_" + (this.ar ? "hard" : "soft"), true, var1);
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
            }
      }

      return PlayState.CONTINUE;
   }

   @Override
   public void registerControllers(AnimationData var1) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener var2 = var1x -> {
         switch (var1x.sound) {
            case "attackDone":
               if (++this.S == 3) {
                  this.S = 0;
               }
               break;
            case "stripMSG1":
               this.h("Hihi~");
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_GIGGLE));
               break;
            case "paymentMSG1":
               this.h("Huh?");
               this.a(SoundHandler.GIRLS_JENNY_HUH[1]);
               break;
            case "paymentMSG2":
               this.a(SoundHandler.MISC_PLOB[0], 0.5F);
               String var4 = "<" + Minecraft.getMinecraft().player.getName() + "> ";
               switch ((String)this.entityDataManager.get(BaseGirlEntity.GIRL_HAND_STATES)) {
                  case "strip":
                     this.b(var4 + "show Bobs and vegana pls", true);
                     return;
                  case "blowjob":
                     this.b(var4 + "Give me the sucky sucky and these are yours", true);
                     return;
                  case "doggy":
                     this.b(var4 + "Give me the sex pls :)", true);
                     return;
                  case "boobjob":
                     this.b(var4 + "gib boba OwO", true);
                     return;
                  default:
                     this.b(var4 + "sex pls", true);
                     return;
               }
            case "paymentMSG3":
               this.h("Hehe~");
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
               this.h("What are you...");
               this.a(SoundHandler.GIRLS_JENNY_MMM[8]);
               this.cameraYaw = 180.0F;
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "bjiMSG2":
               this.h("eh... boys...");
               this.a(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[8]);
               break;
            case "bjiMSG3":
               this.h("OHOhh...!");
               this.a(SoundHandler.GIRLS_JENNY_AFTERSESSIONMOAN[0]);
               break;
            case "bjiMSG4":
               this.a(SoundHandler.MISC_BELLJINGLE[0]);
               break;
            case "bjiMSG5":
               this.h("Was this really necessary?!");
               this.a(SoundHandler.GIRLS_JENNY_HMPH[1], 0.5F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "bjiMSG6":
               this.h("Oh~");
               this.a(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[8]);
               break;
            case "bjiMSG7":
               this.h("You like it?~");
               this.a(SoundHandler.GIRLS_JENNY_GIGGLE[4]);
               break;
            case "bjiMSG8":
               this.b("<" + Minecraft.getMinecraft().player.getName() + "> Yee", true);
               this.a(SoundHandler.MISC_PLOB[0], 0.5F);
               break;
            case "bjiMSG9":
               this.h("Hihihi~");
               this.a(SoundHandler.GIRLS_JENNY_GIGGLE[2]);
               break;
            case "bjiMSG10":
               if (this.isControlledByLocalPlayer()) {
                  this.positionPlayerRelative(-0.4, -0.8, -0.2, 60.0F, -3.0F);
               }
               break;
            case "bjiMSG11":
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
                  this.ar = true;
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
                  this.resetCameraAndPhysics();
               }
               break;
            case "doggyGoOnBedMSG1":
               this.a(SoundHandler.MISC_BEDRUSTLE[0]);
               this.cameraYaw = this.rotationYaw;
               break;
            case "doggyGoOnBedMSG2":
               this.sendChatMessage("what are you waiting for?~");
               this.a(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[9]);
               break;
            case "doggyGoOnBedMSG3":
               this.sendChatMessage("this ass ain't gonna fuck itself...");
               this.a(SoundHandler.GIRLS_JENNY_GIGGLE[0]);
               break;
            case "doggyGoOnBedMSG4":
               this.a(SoundHandler.MISC_SLAP[0], 0.75F);
               break;
            case "doggyGoOnBedDone":
               PacketHandler.b.sendToServer(new SetPlayerForGirlPacket(this.getGirlId(), Minecraft.getMinecraft().player.getPersistentID()));
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
               this.ar = false;
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

               this.aq++;
               if (this.aq % 2 == 0) {
                  int var9 = Reference.f.nextInt(2);
                  if (var9 == 0) {
                     this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MOAN));
                  } else {
                     this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING));
                  }
               } else {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_AHH));
               }
               break;
            case "doggyfastDone":
               this.ar = false;
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
               if (this.isControlledByLocalPlayer() && !this.as) {
                  this.as = true;
                  this.cameraYaw = 180.0F;
                  this.positionPlayerRelative(-0.7, -0.6, -0.2, 60.0F, -3.0F);
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
               if (this.getRNG().nextBoolean()) {
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
               if (this.isControlledByLocalPlayer() && !this.as) {
                  this.as = true;
                  this.positionPlayerRelative(-0.7, -0.6, -0.2, 60.0F, -3.0F);
               }
               break;
            case "paizuri_startStep":
               IBlockState var6 = this.world.getBlockState(this.getPosition().subtract(new Vec3i(0, 1, 0)));
               this.a(var6.getBlock().getSoundType(var6, this.world, this.getPosition(), this).getStepSound());
               break;
            case "paizuri_cumStart":
               if (this.isControlledByLocalPlayer() && !this.as) {
                  this.positionPlayerRelative(-0.7, -0.6, -0.2, 60.0F, -3.0F);
               }
         }
      };
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
   }

}
