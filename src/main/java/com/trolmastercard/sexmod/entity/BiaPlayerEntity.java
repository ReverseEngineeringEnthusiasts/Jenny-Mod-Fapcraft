package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.JennyModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.am;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.d3;







import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class BiaPlayerEntity extends AbstractPlayerGirlEntity {
   int ar = -1;
   boolean ap = false;
   int aq = 1;

   public BiaPlayerEntity(World var1) {
      super(var1);
   }

   public BiaPlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public float i_clash226() {
      return 1.5F;
   }

   public float getEyeHeight() {
      return 1.5F;
   }

   @Override
   public void u_clash377() {
   }

   @Override
   public boolean a_clash571(String var1) {
      if ("anal".equals(var1)) {
         this.b(fp.ANAL_PREPARE);
         this.f(0);
         return true;
      } else if ("doggy".equals(var1)) {
         this.b(fp.SITDOWN);
         this.f(0);
         return true;
      } else {
         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void H_clash570() {
      BaseGirlEntity.a(Minecraft.getMinecraft().player, this, new String[]{"anal", "doggy"}, false);
   }

   @Override
   public void b(String var1, UUID var2) {
      if ("action.names.headpat".equals(var1)) {
         this.b_clash577(var2);
         this.b(fp.HEAD_PAT);
         this.a(this.getOutfitIndex(), fp.HEAD_PAT);
      }
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new JennyModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return "textures/entity/bia/hand.png";
   }

   @Override
   public float getLeftArmAngle() {
      return 35.0F;
   }

   @Override
   public float getRightArmAngle() {
      return 140.0F;
   }

   @Override
   public boolean A_clash381() {
      return false;
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      BaseGirlEntity.a(var1, this, new String[]{"action.names.headpat"}, false);
      return true;
   }

   @Override
   public void b(fp var1) {
      if (this.getCurrentAction() != fp.ANAL_CUM || var1 != fp.ANAL_FAST && var1 != fp.ANAL_SLOW) {
         if (this.getCurrentAction() != fp.PRONE_DOGGY_CUM || var1 != fp.PRONE_DOGGY_HARD && var1 != fp.PRONE_DOGGY_SOFT) {
            super.b(var1);
         }
      }
   }

   @Override
   protected fp getNextAction(fp var1) {
      if (var1 == fp.ANAL_SLOW) {
         return fp.ANAL_FAST;
      } else {
         return var1 == fp.PRONE_DOGGY_INTRO ? fp.PRONE_DOGGY_INSERT : null;
      }
   }

   @Override
   protected fp getCumAction(fp var1) {
      if (var1 == fp.ANAL_SLOW || var1 == fp.ANAL_FAST) {
         return fp.ANAL_CUM;
      } else {
         return var1 != fp.PRONE_DOGGY_SOFT && var1 != fp.PRONE_DOGGY_HARD ? null : fp.PRONE_DOGGY_CUM;
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      this.a_clash590();
   }

   @Override
   protected void V() {
      super.V();
      this.ar = -1;
   }

   @SideOnly(Side.CLIENT)
   public boolean a_clash589(EntityPlayer var1) {
      return Minecraft.getMinecraft().player.getPersistentID().equals(var1.getPersistentID());
   }

   void a_clash590() {
      fp var1 = this.getCurrentAction();
      if (var1 == fp.ANAL_WAIT || var1 == fp.SITDOWNIDLE) {
         EntityPlayer var2 = this.j_clash575();
         if (var2 != null) {
            if (!(var2.getDistance(this) > 1.0F)) {
               if (!this.world.isRemote || this.a_clash589(var2)) {
                  if (this.ar == -1) {
                     if (this.world.isRemote) {
                        BeeScreen.enableInteraction();
                        d3.setMovementLock(false);
                     } else {
                        this.setInteractionPlayerUUID(var2.getPersistentID());
                     }

                     this.ar = 22;
                  } else if (--this.ar <= 0) {
                     this.ar = -1;
                     var2.noClip = true;
                     var2.setNoGravity(true);
                     if (var1 == fp.ANAL_WAIT) {
                        if (!this.world.isRemote) {
                           this.b(fp.ANAL_START);
                           Vec3d var8 = this.getTargetPosition().add(ck.a(-0.3, -1.0, -0.5, this.getYawRotation()));
                           var2.setPositionAndUpdate(var8.x, var8.y, var8.z);
                        } else if (this.isControlledByLocalPlayer()) {
                           HornyMeterHud.showHornyMeter();
                        }
                     } else {
                        float var3 = this.getYawRotation();
                        var2.rotationYaw = var3;
                        var2.rotationPitch = 60.0F;
                        if (!this.world.isRemote) {
                           this.f(0);
                           this.b(fp.PRONE_DOGGY_INTRO);
                           Vec3d var4 = this.getTargetPosition();
                           Vec3d var5 = var4.add(ck.a(0.0, 0.0, 1.0, var3));
                           this.setTargetPosition(var5);
                           EntityPlayer var6 = this.k_clash584();
                           if (var6 != null) {
                              var6.setPositionAndUpdate(var5.x, var5.y, var5.z);
                           }

                           Vec3d var7 = var4.add(ck.a(0.0, 1.1875 - var2.getEyeHeight(), 0.5, var3));
                           var2.setPositionAndUpdate(var7.x, var7.y, var7.z);
                           this.setAnchored(true);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void ag() {
      super.ag();
      if (this.getCurrentAction() == fp.PRONE_DOGGY_HARD) {
         int var1 = this.aq;

         do {
            this.aq = this.getRNG().nextInt(3) + 1;
         } while (var1 == this.aq);
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == fp.NULL && this.getCurrentAction().autoBlink) {
               this.a("animation.bia.fhappy", true, var1);
            } else {
               this.a("animation.bia.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != fp.NULL) {
               this.a("animation.bia.null", true, var1);
            } else if (this.ak) {
               this.a("animation.bia.sit", true, var1);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.ap = !this.ap;
               }

               if (!this.af) {
                  this.a("animation.bia.fly" + (this.ap ? "2" : ""), true, var1);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.movementController.setAnimationSpeed(1.2);
                     this.a("animation.bia.run", true, var1);
                  } else if (this.ao.y >= -0.1F) {
                     this.movementController.setAnimationSpeed(1.2);
                     this.a("animation.bia.fastwalk", true, var1);
                  } else {
                     this.movementController.setAnimationSpeed(1.2);
                     this.a("animation.bia.backwards_walk", true, var1);
                  }
               } else {
                  this.a("animation.bia.idle", true, var1);
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.a("animation.bia.null", true, var1);
                  break;
               case STRIP:
                  this.a("animation.bia.strip", false, var1);
                  break;
               case ATTACK:
                  this.a("animation.bia.attack" + this.S, false, var1);
                  break;
               case BOW:
                  this.a("animation.bia.bowcharge", false, var1);
                  break;
               case RIDE:
                  this.a("animation.bia.ride", true, var1);
                  break;
               case SIT:
                  this.a("animation.bia.sit", true, var1);
                  break;
               case THROW_PEARL:
                  this.a("animation.bia.throwpearl", false, var1);
                  break;
               case DOWNED:
                  this.a("animation.bia.downed", true, var1);
                  break;
               case TALK_HORNY:
                  this.a("animation.bia.talk_horny", false, var1);
                  break;
               case TALK_IDLE:
                  this.a("animation.bia.talk_idle", true, var1);
                  break;
               case TALK_RESPONSE:
                  this.a("animation.bia.talk_response", true, var1);
                  break;
               case ANAL_PREPARE:
                  this.a("animation.bia.anal_prepare", false, var1);
                  break;
               case ANAL_WAIT:
                  this.a("animation.bia.anal_wait", true, var1);
                  break;
               case ANAL_START:
                  this.a("animation.bia.anal_start", true, var1);
                  break;
               case ANAL_SLOW:
                  this.a("animation.bia.anal_slow", true, var1);
                  break;
               case ANAL_FAST:
                  this.a("animation.bia.anal_fast", true, var1);
                  break;
               case ANAL_CUM:
                  this.a("animation.bia.anal_cum", false, var1);
                  break;
               case HEAD_PAT:
                  this.a("animation.bia.headpat", false, var1);
                  break;
               case SITDOWN:
                  this.a("animation.bia.sitdown", false, var1);
                  break;
               case SITDOWNIDLE:
                  this.a("animation.bia.sitdownidle", true, var1);
                  break;
               case PRONE_DOGGY_INTRO:
                  this.a("animation.bia.prone_doggy_intro", true, var1);
                  break;
               case PRONE_DOGGY_INSERT:
                  this.a("animation.bia.prone_doggy_insert", true, var1);
                  break;
               case PRONE_DOGGY_SOFT:
                  this.a("animation.bia.prone_doggy_soft", true, var1);
                  break;
               case PRONE_DOGGY_HARD:
                  this.a("animation.bia.prone_doggy_hard" + this.aq, true, var1);
                  break;
               case PRONE_DOGGY_CUM:
                  this.a("animation.bia.prone_doggy_cum", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

   @SideOnly(Side.CLIENT)
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
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_BIA_GIGGLE));
               break;
            case "sexUiOn":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "pearl":
               PacketHandler.b.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               break;
            case "talk_hornyMSG1":
               this.sendChatMessage("Heyaaa~");
               this.a(SoundHandler.GIRLS_BIA_HEY[3]);
               break;
            case "talk_hornyMSG2":
               this.sendChatMessage("I am Hornyyyyy~");
               this.a(SoundHandler.GIRLS_BIA_GIGGLE[2]);
               break;
            case "talk_hornyMSG3":
               this.sendChatMessage("So...");
               this.a(SoundHandler.GIRLS_BIA_BREATH[0]);
               break;
            case "talk_hornyMSG4":
               this.sendChatMessage("Are we gonna have some fun nyaa?");
               this.a(SoundHandler.GIRLS_BIA_HUH[0]);
               break;
            case "talk_responseMSG1":
               this.sendChatMessage("Huh?!...");
               this.a(SoundHandler.GIRLS_BIA_HUH[2]);
               break;
            case "talk_responseMSG2":
               this.sendChatMessage("I... uhm...");
               this.a(SoundHandler.GIRLS_BIA_BREATH[1]);
               break;
            case "talk_responseMSG3":
               this.sendChatMessage("yes~");
               this.a(SoundHandler.GIRLS_BIA_GIGGLE[0]);
               break;
            case "talk_responseDone":
               this.s();
               if ((Integer)this.entityDataManager.get(BaseGirlEntity.OUTFIT_INDEX) != 0) {
                  this.b(fp.STRIP);
               } else {
                  this.U();
               }
               break;
            case "anal_prepareMSG1":
               this.a(SoundHandler.MISC_PLOB[0]);
               break;
            case "anal_prepareMSG2":
               this.a(SoundHandler.MISC_BEDRUSTLE[0]);
               break;
            case "anal_prepareDone":
               this.b(fp.ANAL_WAIT);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "anal_startMSG1":
               this.a(SoundHandler.GIRLS_BIA_MMM[3]);
               this.a(SoundHandler.MISC_POUNDING[34]);
               break;
            case "anal_fastMSG1":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }

               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.5F);
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_BIA_AHH));
               break;
            case "anal_slowMSG1":
            case "anal_startMSG2":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }

               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.5F);
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_BIA_AHH));
               break;
            case "anal_fastDone":
               if (!this.isControlledByLocalPlayer() || d3.d) {
                  return;
               }
            case "anal_startDone":
               this.b(fp.ANAL_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "anal_cumMSG2":
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_BIA_AHH));
               break;
            case "anal_cumBlackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "doggy_cumDone":
            case "anal_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }

               this.resetCameraAndPhysics();
               break;
            case "headpatMSG1":
               this.sendChatMessage("Ooh headpats!");
               this.a(SoundHandler.GIRLS_BIA_BREATH[0]);
               break;
            case "headpatMSG2":
               this.sendChatMessage("Hmmm.... :D");
               this.a(SoundHandler.GIRLS_BIA_MMM[0]);
               break;
            case "headpatMSG3":
               this.sendChatMessage("huh...?");
               this.a(SoundHandler.GIRLS_BIA_HUH[0]);
               break;
            case "headpatMSG4":
               this.sendChatMessage("Tanku hehe");
               this.a(SoundHandler.GIRLS_BIA_GIGGLE[1]);
               break;
            case "headpatDone":
               if (this.isLocalPlayerNearby()) {
                  this.resetCameraAndPhysics();
               }
               break;
            case "sitdownMSG1":
               this.sendChatMessage("come here big boy~");
               this.playRandomSound(SoundHandler.GIRLS_BIA_BREATH);
               break;
            case "sitdownDone":
               this.b(fp.SITDOWNIDLE);
               break;
            case "slide":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_SLIDE));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.005);
               }
               break;
            case "pound":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               break;
            case "doggyMoan":
               this.playRandomSound(this.getRNG().nextBoolean() ? SoundHandler.GIRLS_BIA_AHH : SoundHandler.GIRLS_BIA_MMM);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "doggySwitch":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.b(fp.PRONE_DOGGY_HARD);
               }
               break;
            case "doggyReset":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.N();
               }
               break;
            case "cum":
               this.a(SoundHandler.MISC_INSERTS, 6.0F);
               break;
            case "orgasm1":
               this.a(SoundHandler.GIRLS_BIA_MMM[6]);
               break;
            case "orgasm2":
               this.a(SoundHandler.GIRLS_BIA_MMM[7]);
               break;
            case "openSexUI":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
         }
      };
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
      var1.addAnimationController(this.actionController);
   }

}
