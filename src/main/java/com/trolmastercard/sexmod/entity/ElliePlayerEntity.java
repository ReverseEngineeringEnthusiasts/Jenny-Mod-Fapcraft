package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.IPositionProvider;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.EllieModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.ChangeDataParameterPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SendGirlToSexPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.networking.SexPromptPacket;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.google.common.base.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class ElliePlayerEntity extends AbstractPlayerGirlEntity {
   boolean ar = false;
   boolean aq = false;
   int ap = 1;

   protected ElliePlayerEntity(World var1) {
      super(var1);
   }

   public ElliePlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public float getScaleFactor() {
      return 2.05F;
   }

   public float getEyeHeight() {
      return this.hasNoOwner() ? 1.53F : 1.9F;
   }

   @Override
   public void handleInteraction() {
      this.setCurrentAction(Action.SITDOWN);
   }

   @Override
   public void handleOwnerCommand(String var1, UUID var2) {
      if ("Face fuck".equals(var1)) {
         this.teleportPlayerToGirl(var2);
         this.setCurrentAction(Action.CARRY_INTRO);
         this.sendActionPacket(this.getOutfitIndex(), Action.CARRY_INTRO);
      }
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new EllieModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return var1 == 0 ? "textures/entity/ellie/hand_nude.png" : "textures/entity/ellie/hand.png";
   }

   @Override
   public boolean canOpenInteractionMenu() {
      return true;
   }

   @Override
   public void doAction(String var1, UUID var2) {
      if ("action.names.cowgirl".equals(var1)) {
         this.changeDataParameterFromClient("animationFollowUp", "Cowgirl");
      } else if ("action.names.missionary".equals(var1)) {
         this.changeDataParameterFromClient("animationFollowUp", "Missionary");
      } else if (((Optional)this.entityDataManager.get(ai)).isPresent()) {
         PacketHandler.networkWrapper.sendToServer(new SexPromptPacket(var1, var2, (UUID)((Optional)this.entityDataManager.get(ai)).get(), this.ab));
         this.ab = true;
      }
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      openInventoryGui(var1, this, new String[]{"Face fuck"}, false);
      return true;
   }

   void openEllieInventory(EntityPlayer var1) {
      openInventoryGui(var1, this, new String[]{"action.names.cowgirl", "action.names.missionary"}, false);
   }

   @Override
   public boolean A_clash381() {
      return false;
   }

   @Override
   public void setCurrentAction(Action action) {
      Action var2 = this.getCurrentAction();
      if (var2 != Action.MISSIONARY_CUM || action != Action.MISSIONARY_FAST && action != Action.MISSIONARY_SLOW) {
         if (var2 != Action.COWGIRLCUM || action != Action.COWGIRLSLOW && action != Action.COWGIRLFAST) {
            super.setCurrentAction(action);
         }
      }
   }

   @Override
   protected Action getNextAction(Action var1) {
      if (var1 == Action.COWGIRLSLOW) {
         return Action.COWGIRLFAST;
      } else if (var1 == Action.MISSIONARY_SLOW) {
         return Action.MISSIONARY_FAST;
      } else {
         return var1 == Action.CARRY_SLOW ? Action.CARRY_FAST : null;
      }
   }

   @Override
   protected Action getCumAction(Action var1) {
      if (var1 == Action.COWGIRLFAST || var1 == Action.COWGIRLSLOW) {
         return Action.COWGIRLCUM;
      } else if (var1 == Action.MISSIONARY_FAST || var1 == Action.MISSIONARY_SLOW) {
         return Action.MISSIONARY_CUM;
      } else {
         return var1 != Action.CARRY_SLOW && var1 != Action.CARRY_FAST ? null : Action.CARRY_CUM;
      }
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (this.getCurrentAction() == Action.SITDOWNIDLE) {
         String var1 = (String)this.entityDataManager.get(BaseGirlEntity.GIRL_HAND_STATES);
         if (!"Missionary".equals(var1) && !"Cowgirl".equals(var1)) {
            return;
         }

         EntityPlayer var2 = this.getNearestPlayer();
         if (var2 == null || var2.getDistance(this.getPositionVec3d().x, this.getPositionVec3d().y, this.getPositionVec3d().z) > 1.0) {
            return;
         }

         this.entityDataManager.set(BaseGirlEntity.GIRL_HAND_STATES, "");
         this.entityDataManager.set(BaseGirlEntity.OUTFIT_INDEX, 0);
         this.setInteractionPlayerUUID(var2.getPersistentID());
         EntityPlayerMP var3 = (EntityPlayerMP)this.world.getPlayerEntityByUUID((UUID)((Optional)this.entityDataManager.get(ai)).get());
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var2);
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), var3);
         var2.moveRelative(0.0F, 0.0F, 0.0F, 0.0F);
         var3.capabilities.isFlying = true;
         var2.capabilities.isFlying = true;
         var3.noClip = true;
         var2.noClip = true;
         var3.setNoGravity(true);
         var2.setNoGravity(true);
         if ("Missionary".equals(var1)) {
            this.setCurrentAction(Action.MISSIONARY_START);
            Vec3d var4 = this.getPositionVec3d().subtract(0.0, 0.1, 0.0);
            var2.setPositionAndRotation(var4.x, var4.y, var4.z, this.getYawRotation(), 60.0F);
            var2.setPositionAndUpdate(var4.x, var4.y, var4.z);
         } else {
            this.setCurrentAction(Action.COWGIRLSTART);
            Vec3d var5 = this.getPositionVec3d()
               .add(
                  new Vec3d(
                     -Math.sin(this.getYawRotation().floatValue() * (Math.PI / 180.0)) * 1.8,
                     -0.65,
                     Math.cos(this.getYawRotation().floatValue() * (Math.PI / 180.0)) * 1.8
                  )
               );
            var2.setPositionAndRotation(var5.x, var5.y, var5.z, 180.0F + this.getYawRotation(), -30.0F);
            var2.setPositionAndUpdate(var5.x, var5.y, var5.z);
         }
      }
   }

   boolean hasNoOwner() {
      EntityPlayer var1 = this.getOwnerPlayer();
      return var1 == null
         ? false
         : this.world.getBlockState(var1.getPosition().up().up()).getBlock() != Blocks.AIR;
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.ellie.eyes", true, var1);
            } else {
               this.createAnimation("animation.ellie.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.ellie.null", true, var1);
            } else if (this.ak) {
               this.createAnimation("animation.ellie.ride", true, var1);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.ar = !this.ar;
               }

               if (!this.af) {
                  this.createAnimation("animation.ellie.fly" + (this.ar ? "2" : ""), true, var1);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.movementController.setAnimationSpeed(1.5);
                     this.createAnimation(this.hasNoOwner() ? "animation.ellie.crouchwalk" : "animation.ellie.run", true, var1);
                  } else if (this.ao.y >= -0.1F) {
                     this.movementController.setAnimationSpeed(2.0);
                     this.createAnimation(this.hasNoOwner() ? "animation.ellie.crouchwalk" : "animation.ellie.fastwalk", true, var1);
                  } else {
                     this.movementController.setAnimationSpeed(1.5);
                     this.createAnimation(this.hasNoOwner() ? "animation.ellie.crouchwalk" : "animation.ellie.backwards_walk", true, var1);
                  }
               } else {
                  this.createAnimation(this.hasNoOwner() ? "animation.ellie.crouchidle" : "animation.ellie.idle", true, var1);
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.ellie.null", true, var1);
                  break;
               case STRIP:
                  this.createAnimation("animation.ellie.strip", false, var1);
                  break;
               case DASH:
                  this.createAnimation("animation.ellie.dash", false, var1);
                  break;
               case HUG:
                  this.createAnimation("animation.ellie.hug", false, var1);
                  break;
               case HUGIDLE:
                  this.createAnimation("animation.ellie.hugidle", true, var1);
                  break;
               case HUGSELECTED:
                  this.createAnimation("animation.ellie.hugselected", false, var1);
                  break;
               case SITDOWN:
                  this.createAnimation("animation.ellie.sitdown", false, var1);
                  break;
               case SITDOWNIDLE:
                  this.createAnimation("animation.ellie.sitdownidle", true, var1);
                  break;
               case COWGIRLSTART:
                  this.createAnimation("animation.ellie.cowgirlstart", false, var1);
                  break;
               case COWGIRLSLOW:
                  this.createAnimation("animation.ellie.cowgirlslow2", true, var1);
                  break;
               case COWGIRLFAST:
                  this.createAnimation("animation.ellie.cowgirlfast", true, var1);
                  break;
               case COWGIRLCUM:
                  this.createAnimation("animation.ellie.cowgirlcum", true, var1);
                  break;
               case ATTACK:
                  this.createAnimation("animation.ellie.attack" + this.nextAttack, false, var1);
                  break;
               case BOW:
                  this.createAnimation("animation.ellie.bowcharge", false, var1);
                  break;
               case RIDE:
                  this.createAnimation("animation.ellie.ride", true, var1);
                  break;
               case SIT:
                  this.createAnimation("animation.ellie.sit", true, var1);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.ellie.throwpearl", false, var1);
                  break;
               case DOWNED:
                  this.createAnimation("animation.ellie.downed", true, var1);
                  break;
               case MISSIONARY_START:
                  this.createAnimation("animation.ellie.missionary_start", false, var1);
                  break;
               case MISSIONARY_SLOW:
                  this.createAnimation("animation.ellie.missionary_slow", true, var1);
                  break;
               case MISSIONARY_FAST:
                  this.createAnimation("animation.ellie.missionary_fast", true, var1);
                  break;
               case MISSIONARY_CUM:
                  this.createAnimation("animation.ellie.missionary_cum", false, var1);
                  break;
               case CARRY_INTRO:
                  this.createAnimation("animation.ellie.carry_intro", false, var1);
                  break;
               case CARRY_SLOW:
                  this.createAnimation("animation.ellie.carry_slow" + this.ap, true, var1);
                  break;
               case CARRY_FAST:
                  this.createAnimation("animation.ellie.carry_fast", true, var1);
                  break;
               case CARRY_CUM:
                  this.createAnimation("animation.ellie.carry_cum", true, var1);
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
            case "dashMSG1":
               EntityPlayer var9 = this.world.getClosestPlayerToEntity(this, 15.0);
               if (var9 != null) {
                  Vec3d var14 = this.getPositionVector().subtract(var9.getPositionVector());
                  float var15 = (float)Math.atan2(var14.z, var14.x) * (float) (180.0 / Math.PI);
                  this.rotationYaw = var15;
                  this.rotationYawHead = var15;
                  this.renderYawOffset = var15;
               }
               break;
            case "dashReady":
               if (this.isLocalPlayerNearby()) {
                  return;
               }
               break;
            case "dashDone":
               this.setCurrentAction(Action.HUG);
               EntityPlayer var8 = this.world.getClosestPlayerToEntity(this, 15.0);
               if (var8 != null) {
                  float var13 = var8.rotationYaw;
                  this.rotationYaw = var13;
                  this.rotationYawHead = var13;
                  this.renderYawOffset = var13;
               }
               break;
            case "hugMSG1":
               EntityPlayerSP var7 = Minecraft.getMinecraft().player;
               if (var7.getPersistentID().equals(this.getInteractionPlayerUUID()) || var7.getUniqueID().equals(this.getInteractionPlayerUUID())) {
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(var7.getUniqueID().toString(), var7.getPositionVector(), var7.rotationYaw - 80.0F, var7.rotationPitch)
                     );
               }
               break;
            case "hugMSG2":
               this.sendGirlChatMessage("Hmm...");
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_HMPH[3], 3.0F);
               break;
            case "hugMSG3":
               this.sendGirlChatMessage("Hey!");
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_AHH[2], 3.0F);
               break;
            case "hugMSG4":
               this.sendGirlChatMessage(I18n.format("ellie.dialogue.mommyhorny", new Object[0]));
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_GIGGLE[0], 3.0F);
               break;
            case "hugMSG5":
               this.sendGirlChatMessage(I18n.format("ellie.dialogue.whattodo", new Object[0]));
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_HUH[1], 3.0F);
               break;
            case "hugDone":
               EntityPlayerSP var4 = Minecraft.getMinecraft().player;
               if (var4.getPersistentID().equals(this.getInteractionPlayerUUID())) {
                  this.setCurrentAction(Action.HUGIDLE);
                  this.openEllieInventory(var4);
               }
               break;
            case "hugselectedMSG1":
               this.sendGirlChatMessage(I18n.format("ellie.dialogue.iknow", new Object[0]));
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_MMM[0], 3.0F);
               break;
            case "hugselectedMSG2":
               this.sendGirlChatMessage(I18n.format("ellie.dialogue.followmedarling", new Object[0]));
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_GIGGLE[3], 3.0F);
               break;
            case "hugselectedDone":
               if (this.isLocalPlayerNearby()) {
                  Vec3d var10 = this.getPositionVector();
                  var10 = var10.add(
                     -Math.sin((this.rotationYaw + 90.0F) * (Math.PI / 180.0)) * -0.7803125F,
                     0.0,
                     Math.cos((this.rotationYaw + 90.0F) * (Math.PI / 180.0)) * -0.7803125F
                  );
                  var10 = var10.add(
                     -Math.sin(this.rotationYaw * (Math.PI / 180.0)) * 0.5296875F, 0.0, Math.cos(this.rotationYaw * (Math.PI / 180.0)) * 0.5296875F
                  );
                  String var6 = var10.x + "f" + var10.y + "f" + var10.z + "f";
                  PacketHandler.networkWrapper.sendToServer(new ChangeDataParameterPacket(this.getGirlId(), "targetPos", var6));
                  this.resetCameraAndPhysics();
                  PacketHandler.networkWrapper.sendToServer(new SendGirlToSexPacket(this.getGirlId()));
                  this.setCurrentAction(Action.NULL);
               }
               break;
            case "sitdownMSG1":
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_GIGGLE[3], 3.0F);
               if (this.isLocalPlayerNearby()) {
                  this.sendGirlChatMessage(I18n.format("ellie.dialogue.cometomommy", new Object[0]));
               }
               break;
            case "sitdownDone":
               if (this.hasOwnerUUID()) {
                  this.setCurrentAction(Action.SITDOWNIDLE);
                  this.openEllieInventory(this.world.getPlayerEntityByUUID(this.getOwnerUserUUID()));
               }
               break;
            case "missionary_startDone":
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.MISSIONARY_SLOW);
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "cowgirlStartMSG0":
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_GIGGLE[4], 3.0F);
               break;
            case "cowgirlStartMSG1":
               if (this.isLocalPlayerNearby()) {
                  this.sendChatMessage(I18n.format("ellie.dialogue.like", new Object[0]));
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "cowgirlStartMSG2":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 3.0F);
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "cowgirlStartDone":
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.COWGIRLSLOW);
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "cowgirlfastMSG1":
               if (this.aq) {
                  this.aq = false;
               } else {
                  this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 3.0F);
               }

               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "cowgirlfastReady":
               if (this.isControlledByLocalPlayer()) {
                  if (!HandlePlayerMovement.isJumping) {
                     this.setCurrentAction(Action.COWGIRLSLOW);
                  } else if (Reference.RANDOM.nextInt(4) != 1) {
                     this.actionController.clearAnimationCache();
                  }
               }
               break;
            case "cowgirlfastdomMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.2);
               }
               break;
            case "cowgirlcumMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 3.0F);
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               break;
            case "cowgirlcumMSG2":
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_MOAN[5], 3.0F);
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               break;
            case "cowgirlcumMSG3":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               break;
            case "cowgirlcumMSG4":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "cowgirlcumMSG5":
            case "missionary_cumMSG2":
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_GIGGLE[4], 3.0F);
               if (this.isControlledByLocalPlayer()) {
                  this.sendChatMessage(I18n.format("ellie.dialogue.goodboy", new Object[0]));
               }
               break;
            case "cowgirlcumMSG6":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "missionary_cumDone":
            case "cowgirlcumDone":
            case "carry_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.resetCameraAndPhysics();
               }
               break;
            case "attackDone":
               if (++this.nextAttack == 3) {
                  this.nextAttack = 0;
               }
               break;
            case "pearl":
               PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               break;
            case "openSexUi":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "missionary_slowMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.getRNG().nextBoolean() && this.getRNG().nextBoolean()) {
                  this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_MOAN), 3.0F);
               } else {
                  this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 3.0F);
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "missionary_fastMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (!this.getRNG().nextBoolean() && !this.getRNG().nextBoolean()) {
                  this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 3.0F);
               } else {
                  this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_MOAN), 3.0F);
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.05);
               }
               break;
            case "missionary_fastDone":
               if (this.isControlledByLocalPlayer()) {
                  if (HandlePlayerMovement.isJumping) {
                     this.setCurrentAction(Action.MISSIONARY_FAST);
                  } else {
                     this.setCurrentAction(Action.MISSIONARY_SLOW);
                  }
               }
               break;
            case "bedRustle":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               this.playSound(SoundHandler.MISC_BEDRUSTLE[0]);
               break;
            case "bedRustle1":
               this.playSound(SoundHandler.MISC_BEDRUSTLE[1]);
               break;
            case "missionary_cumMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 3.0F);
               break;
            case "carry_introMSG1":
               this.sendChatMessage("I'm hungry..");
               this.playRandomSoundAtVolume(SoundHandler.GIRLS_ELLIE_HMPH, 6.0F);
               break;
            case "carry_introMSG2":
               this.sendChatMessage("heh~");
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_GIGGLE[3], 6.0F);
               break;
            case "lipsound":
               this.playRandomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "cum":
               this.playRandomSoundAtVolume(SoundHandler.MISC_INSERTS, 6.0F);
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               break;
            case "pound":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "carry_slowDone":
               int var5 = this.ap;

               do {
                  this.ap = this.getRNG().nextInt(4) + 1;
               } while (this.ap == var5);

               return;
            case "carry_fastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.CARRY_SLOW);
               }
               break;
            case "sexUI":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
         }
      };
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
   }

}
