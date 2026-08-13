package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.IPositionProvider;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.LunaModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;







import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
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

public class LunaPlayerEntity extends AbstractPlayerGirlEntity {
   int ar = 0;
   boolean aq = false;
   boolean ap = false;
   boolean as = false;

   protected LunaPlayerEntity(World var1) {
      super(var1);
   }

   public LunaPlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public float getScaleFactor() {
      return 1.6F;
   }

   public float getEyeHeight() {
      return 1.34F;
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new LunaModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return "textures/entity/cat/hand.png";
   }

   @Override
   public void b(String var1, UUID var2) {
      if ("action.names.touchboobs".equals(var1)) {
         this.a(0, Action.TOUCH_BOOBS_INTRO);
         this.setCurrentAction(Action.TOUCH_BOOBS_INTRO);
         this.entityDataManager.set(OUTFIT_INDEX, 0);
         this.teleportPlayerToGirl(var2);
      }

      if ("action.names.headpat".equals(var1)) {
         this.setCurrentAction(Action.HEAD_PAT);
         this.teleportPlayerToGirl(var2);
      }
   }

   @Override
   public void handleInteraction() {
      this.setCurrentAction(Action.WAIT_CAT);
   }

   @Override
   public boolean canBeInteracted() {
      return true;
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      openInventoryGui(var1, this, new String[]{"action.names.touchboobs", "action.names.headpat"}, false);
      return true;
   }

   @Override
   public void setCurrentAction(Action action) {
      if (this.getCurrentAction() != Action.COWGIRL_SITTING_CUM || action != Action.COWGIRL_SITTING_SLOW && action != Action.COWGIRL_SITTING_FAST) {
         if (this.getCurrentAction() != Action.TOUCH_BOOBS_CUM || action != Action.TOUCH_BOOBS_FAST && action != Action.TOUCH_BOOBS_SLOW) {
            super.setCurrentAction(action);
         }
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (Action.WAIT_CAT.equals(this.getCurrentAction())) {
         this.handleLunaOwner();
      } else {
         this.ar = 0;
      }
   }

   void handleLunaOwner() {
      EntityPlayer var1 = this.getNearestPlayer();
      if (var1 != null) {
         if (!(var1.getDistance(this.posX, this.getPositionVec3d().y, this.posZ) > 1.25)) {
            if (this.world.isRemote) {
               this.a(var1, this.ar);
            } else if (this.ar == 25) {
               this.setInteractionPlayerUUID(var1.getPersistentID());
               var1.moveRelative(0.0F, 0.0F, 0.0F, 0.0F);
               var1.setPositionAndUpdate(this.getPositionVector().x, this.getPositionVec3d().y, this.getPositionVector().z);
               this.setCurrentAction(Action.COWGIRL_SITTING_INTRO);
               var1.setRotationYawHead(this.getYawRotation() + 180.0F);
               var1.rotationYaw = this.getYawRotation() + 180.0F;
               var1.prevRotationYaw = this.getYawRotation() + 180.0F;
               this.cameraYaw = this.getYawRotation() + 180.0F;
               this.positionPlayerRelative(0.0, -0.075F, -0.7109375, 0.0F, 0.0F);
               this.entityDataManager.set(OUTFIT_INDEX, 0);
            }

            this.ar++;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void a(EntityPlayer var1, int var2) {
      if (var2 == 0) {
         EntityPlayerSP var3 = Minecraft.getMinecraft().player;
         if (var3.getPersistentID().equals(var1.getPersistentID())) {
            BeeScreen.enableInteraction();
            var3.setVelocity(0.0, 0.0, 0.0);
            HandlePlayerMovement.setMovementLock(false);
         }
      }

      if (var2 == 25) {
         EntityPlayerSP var4 = Minecraft.getMinecraft().player;
         if (var4.getPersistentID().equals(var1.getPersistentID())) {
            Minecraft.getMinecraft().gameSettings.thirdPersonView = 2;
         }
      }
   }

   @Override
   protected Action getNextAction(Action var1) {
      if (var1 == Action.TOUCH_BOOBS_SLOW) {
         return Action.TOUCH_BOOBS_FAST;
      } else {
         return var1 == Action.COWGIRL_SITTING_SLOW ? Action.COWGIRL_SITTING_FAST : null;
      }
   }

   @Override
   protected Action getCumAction(Action var1) {
      if (var1 == Action.TOUCH_BOOBS_SLOW || var1 == Action.TOUCH_BOOBS_FAST) {
         return Action.TOUCH_BOOBS_CUM;
      } else {
         return var1 != Action.COWGIRL_SITTING_FAST && var1 != Action.COWGIRL_SITTING_SLOW ? null : Action.COWGIRL_SITTING_CUM;
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.cat.blink", true, var1);
            } else {
               this.createAnimation("animation.cat.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.cat.null", true, var1);
            } else if (this.ak) {
               this.createAnimation("animation.cat.sit", true, var1);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.aq = !this.aq;
               }

               if (!this.af) {
                  this.createAnimation("animation.cat.fly" + (this.aq ? "2" : ""), true, var1);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.movementController.setAnimationSpeed(1.5);
                     this.createAnimation("animation.cat.run", true, var1);
                  } else if (this.ao.y >= -0.1F) {
                     this.movementController.setAnimationSpeed(2.0);
                     this.createAnimation("animation.cat.fastwalk", true, var1);
                  } else {
                     this.movementController.setAnimationSpeed(2.0);
                     this.createAnimation("animation.cat.backwards_walk", true, var1);
                  }
               } else {
                  this.createAnimation("animation.cat.idle", true, var1);
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.cat.null", true, var1);
                  break;
               case ATTACK:
                  this.createAnimation("animation.cat.attack" + this.nextAttack, false, var1);
                  break;
               case RIDE:
               case SIT:
                  this.createAnimation("animation.cat.sit", true, var1);
                  break;
               case BOW:
                  this.createAnimation("animation.cat.bowcharge", false, var1);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.cat.throwpearl", true, var1);
                  break;
               case DOWNED:
                  this.createAnimation("animation.cat.downed", true, var1);
                  break;
               case FISHING_START:
                  this.createAnimation("animation.cat.start_fishing", false, var1);
                  break;
               case FISHING_IDLE:
                  this.createAnimation("animation.cat.idle_fishing", true, var1);
                  break;
               case FISHING_EAT:
                  this.createAnimation("animation.cat.eat_fishing", false, var1);
                  break;
               case FISHING_THROW_AWAY:
                  this.createAnimation("animation.cat.throw_away", false, var1);
                  break;
               case PAYMENT:
                  this.createAnimation("animation.cat.payment", false, var1);
                  break;
               case TOUCH_BOOBS_INTRO:
                  this.createAnimation("animation.cat.touch_boobs_intro", false, var1);
                  break;
               case TOUCH_BOOBS_SLOW:
                  this.createAnimation("animation.cat.touch_boobs_slow" + (this.ap ? "1" : ""), true, var1);
                  break;
               case TOUCH_BOOBS_FAST:
                  this.createAnimation("animation.cat.touch_boobs_fast", true, var1);
                  break;
               case TOUCH_BOOBS_CUM:
                  this.createAnimation("animation.cat.touch_boobs_cum", false, var1);
                  break;
               case WAIT_CAT:
                  this.createAnimation("animation.cat.wait", false, var1);
                  break;
               case COWGIRL_SITTING_INTRO:
                  this.createAnimation("animation.cat.sitting_intro", false, var1);
                  break;
               case COWGIRL_SITTING_SLOW:
                  this.createAnimation("animation.cat.sitting_slow", true, var1);
                  break;
               case COWGIRL_SITTING_FAST:
                  this.createAnimation("animation.cat.sitting_fast", true, var1);
                  break;
               case COWGIRL_SITTING_CUM:
                  this.createAnimation("animation.cat.sitting_cum", true, var1);
                  break;
               case HEAD_PAT:
                  this.createAnimation("animation.cat.head_pat", true, var1);
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
               if (++this.nextAttack == 3) {
                  this.nextAttack = 0;
               }
               break;
            case "idleDone":
               this.as = this.getRNG().nextInt(10) == 0;
               break;
            case "idle2Done":
               this.as = false;
               break;
            case "pearl":
               PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               break;
            case "paymentMSG1":
               this.sendChatMessageToPlayer(this.getInteractionPlayerUUID(), "Here, I know u like fish and yea.. these are for you");
               this.playSound(SoundHandler.MISC_PLOB[0]);
               break;
            case "paymentMSG2":
               this.sendChatMessage("huh~?");
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HUH);
               break;
            case "paymentMSG3":
               this.sendChatMessage("nyyyaaaa~ :D");
               int[] var4 = new int[]{1, 7, 10, 11};
               int var5 = var4[this.getRNG().nextInt(var4.length)];
               this.playSound(SoundHandler.GIRLS_LUNA_CUTENYA[var5]);
               break;
            case "paymentMSG4":
               this.sendChatMessage("tankuuuu owowowo");
               this.playRandomSound(SoundHandler.GIRLS_LUNA_OWO);
               break;
            case "paymentDone":
               if (this.isLocalPlayerNearby()) {
                  this.U();
               }

               this.scaleFactor = 1.0F;
               break;
            case "breath":
            case "rod_breath":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_LIGHTBREATHING);
               break;
            case "happyOh":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HAPPYOH);
               break;
            case "cutenya3":
               this.playSound(SoundHandler.GIRLS_LUNA_CUTENYA[3]);
               break;
            case "cutenya2":
               this.playSound(SoundHandler.GIRLS_LUNA_CUTENYA[2]);
               break;
            case "huh":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HUH);
               break;
            case "hmph":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HMPH);
               break;
            case "hehe":
            case "giggle":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
               break;
            case "singing":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_SINGING);
               break;
            case "touch_boobsMSG1":
               this.sendChatMessage("comon~ touch me hihi~");
               this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
               break;
            case "touch":
               this.playRandomSound(SoundHandler.MISC_TOUCH);
               break;
            case "jump":
               this.playSoundAtVolume(SoundHandler.MISC_JUMP[0], 0.2F);
               break;
            case "horninya":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HORNINYA);
               break;
            case "horninya2":
            case "touch_boobs_cumMSG3":
            case "sitting_cumMSG1":
               this.playSound(SoundHandler.GIRLS_LUNA_HORNINYA[1]);
               this.playSoundAtVolume(SoundHandler.MISC_CUMINFLATION[0], 5.0F);
               break;
            case "moan":
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_MOAN));
               break;
            case "touch_boobs_introDone":
               this.setCurrentAction(Action.TOUCH_BOOBS_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  HornyMeterHud.showHornyMeter();
                  HandlePlayerMovement.setMovementLock(false);
               }
               break;
            case "touch_boobs_slowDone":
               if (this.ap) {
                  this.ap = false;
               } else {
                  this.ap = Math.random() < 0.5;
               }
               break;
            case "addCumSlow":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "addCumFast":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "fastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.TOUCH_BOOBS_SLOW);
               }
               break;
            case "moanOrNya":
               if (Math.random() > 0.5) {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_MOAN));
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_HORNINYA));
               }
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "touch_boobs_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.resetCameraAndPhysics();
               }
               break;
            case "resetGirl":
               if (this.isControlledByLocalPlayer()) {
                  this.resetCameraAndPhysics();
               }
               break;
            case "touch_boobs_cumMSG1":
               this.playSound(SoundHandler.GIRLS_LUNA_HORNINYA[3]);
               break;
            case "touch_boobs_cumMSG2":
               this.playSound(SoundHandler.GIRLS_LUNA_HORNINYA[9]);
               break;
            case "call_playerMSG1":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
               this.sendChatMessage("come here - big guy hehe~");
               break;
            case "pounding":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               break;
            case "sitting_introMSG1":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
               this.sendChatMessage("hehe~");
               break;
            case "sitting_introDone":
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.COWGIRL_SITTING_SLOW);
                  HornyMeterHud.resetHornyMeter();
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "sitting_slowMSG1":
               if (this.getRNG().nextBoolean()) {
                  if (this.getRNG().nextBoolean()) {
                     this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_HORNINYA));
                     break;
                  }

                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_MOAN));
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_LIGHTBREATHING));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "sitting_fastMSG1":
               if (this.getRNG().nextBoolean()) {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_HORNINYA));
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_MOAN));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "sitting_fastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.COWGIRL_SITTING_SLOW);
                  Vec3d var8 = new Vec3d(0.0, -0.075F, -0.7109375);
                  Vec3d var9 = VectorMath.rotateByYaw(var8, this.getYawRotation() + 180.0F);
                  Minecraft.getMinecraft()
                     .player
                     .setPosition(
                        this.getTargetPosition().x + var9.x,
                        this.getTargetPosition().y - 0.0 + var9.y,
                        this.getTargetPosition().z + var9.z
                     );
               }
               break;
            case "sitting_fastTp":
               if (this.isControlledByLocalPlayer()) {
                  Vec3d var6 = new Vec3d(0.0, -0.160625, -0.9925);
                  Vec3d var7 = VectorMath.rotateByYaw(var6, this.getYawRotation() + 180.0F);
                  Minecraft.getMinecraft()
                     .player
                     .setPosition(
                        this.getTargetPosition().x + var7.x,
                        this.getTargetPosition().y - 0.0 + var7.y,
                        this.getTargetPosition().z + var7.z
                     );
               }
               break;
            case "headpatMSG1":
               this.sendChatMessage("huh?~");
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HUH);
               break;
            case "headpatMSG2":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_MMM);
               break;
            case "headpatMSG3":
               this.sendChatMessage("nya~");
               this.playSound(SoundHandler.GIRLS_LUNA_HORNINYA[0]);
         }
      };
      this.movementController.transitionLengthTicks = 10.0;
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
   }

}
