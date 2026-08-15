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

/**
 * <b>Role.</b> Player-form Ellie — the transformation with the same scene set
 * as the NPC (carry "Face fuck", cowgirl, missionary) plus the sit-down
 * interaction flow.
 * <p>
 * <b>Scene flow.</b> {@code Face fuck} starts the carry via owner command;
 * cowgirl/missionary are chosen in {@link #doAction(String, UUID)} which
 * stores the choice in {@code GIRL_HAND_STATES} ({@code animationFollowUp}
 * packet). {@link #updateAITasks()} then waits in
 * {@link Action#SITDOWNIDLE} until the nearest player is within 1 block,
 * locks both players in and starts {@link Action#MISSIONARY_START} or
 * {@link Action#COWGIRLSTART} (rotating/positioning the player).
 * <p>
 * <b>Pitfalls.</b> {@link #setCurrentAction(Action)} forbids re-entering loop
 * phases while the cum animation plays. {@code hasNoOwner()} (ceiling check
 * over the owner's head) selects the crouched animations and the eye height.
 */
public class ElliePlayerEntity extends AbstractPlayerGirlEntity {
   boolean ar = false;
   boolean aq = false;
   int ap = 1;

   protected ElliePlayerEntity(World world) {
      super(world);
   }

   public ElliePlayerEntity(World world, UUID uuid) {
      super(world, uuid);
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

   /**
    * SERVER: owner command {@code Face fuck} — teleports the acting player in
    * and starts the carry intro, broadcasting it to tracking players.
    */
   @Override
   public void handleOwnerCommand(String command, UUID uuid) {
      if ("Face fuck".equals(command)) {
         this.teleportPlayerToGirl(uuid);
         this.setCurrentAction(Action.CARRY_INTRO);
         this.sendActionPacket(this.getOutfitIndex(), Action.CARRY_INTRO);
      }
   }

   @Override
   public IVanillaModel getHandModel(int index) {
      return new EllieModel();
   }

   @Override
   public String getHandTexture(int index) {
      return index == 0 ? "textures/entity/ellie/hand_nude.png" : "textures/entity/ellie/hand.png";
   }

   @Override
   public boolean canOpenInteractionMenu() {
      return true;
   }

   /**
    * CLIENT: scene chooser — stores the cowgirl/missionary choice in
    * {@code GIRL_HAND_STATES} via the {@code animationFollowUp} packet (the
    * server-side {@link #updateAITasks()} consumes it), and forwards any other
    * action to the server as a {@link SexPromptPacket}.
    */
   @Override
   public void doAction(String action, UUID uuid) {
      if ("action.names.cowgirl".equals(action)) {
         this.changeDataParameterFromClient("animationFollowUp", "Cowgirl");
      } else if ("action.names.missionary".equals(action)) {
         this.changeDataParameterFromClient("animationFollowUp", "Missionary");
      } else if (((Optional)this.entityDataManager.get(ai)).isPresent()) {
         PacketHandler.networkWrapper.sendToServer(new SexPromptPacket(action, uuid, (UUID)((Optional)this.entityDataManager.get(ai)).get(), this.ab));
         this.ab = true;
      }
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer player) {
      openInventoryGui(player, this, new String[]{"Face fuck"}, false);
      return true;
   }

   void openEllieInventory(EntityPlayer player) {
      openInventoryGui(player, this, new String[]{"action.names.cowgirl", "action.names.missionary"}, false);
   }

   @Override
   public boolean A_clash381() {
      return false;
   }

   @Override
   public void setCurrentAction(Action action) {
      Action currentAction = this.getCurrentAction();
      if (currentAction != Action.MISSIONARY_CUM || action != Action.MISSIONARY_FAST && action != Action.MISSIONARY_SLOW) {
         if (currentAction != Action.COWGIRLCUM || action != Action.COWGIRLSLOW && action != Action.COWGIRLFAST) {
            super.setCurrentAction(action);
         }
      }
   }

   @Override
   protected Action getNextAction(Action action) {
      if (action == Action.COWGIRLSLOW) {
         return Action.COWGIRLFAST;
      } else if (action == Action.MISSIONARY_SLOW) {
         return Action.MISSIONARY_FAST;
      } else {
         return action == Action.CARRY_SLOW ? Action.CARRY_FAST : null;
      }
   }

   @Override
   protected Action getCumAction(Action action) {
      if (action == Action.COWGIRLFAST || action == Action.COWGIRLSLOW) {
         return Action.COWGIRLCUM;
      } else if (action == Action.MISSIONARY_FAST || action == Action.MISSIONARY_SLOW) {
         return Action.MISSIONARY_CUM;
      } else {
         return action != Action.CARRY_SLOW && action != Action.CARRY_FAST ? null : Action.CARRY_CUM;
      }
   }

   /**
    * SERVER (and CLIENT mirror): waits in {@link Action#SITDOWNIDLE} for a
    * scene choice ({@code GIRL_HAND_STATES} = "Missionary"/"Cowgirl") AND a
    * nearby player within 1 block, then locks both players into the scene
    * (movement lock, noClip, noGravity, flying) and starts the chosen intro
    * action with the player positioned and rotated to match.
    */
   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (this.getCurrentAction() == Action.SITDOWNIDLE) {
         String handState = (String)this.entityDataManager.get(BaseGirlEntity.GIRL_HAND_STATES);
         if (!"Missionary".equals(handState) && !"Cowgirl".equals(handState)) {
            return;
         }

         EntityPlayer player = this.getNearestPlayer();
         if (player == null || player.getDistance(this.getPositionVec3d().x, this.getPositionVec3d().y, this.getPositionVec3d().z) > 1.0) {
            return;
         }

         this.entityDataManager.set(BaseGirlEntity.GIRL_HAND_STATES, "");
         this.entityDataManager.set(BaseGirlEntity.OUTFIT_INDEX, 0);
         this.setInteractionPlayerUUID(player.getPersistentID());
         EntityPlayerMP playerMP = (EntityPlayerMP)this.world.getPlayerEntityByUUID((UUID)((Optional)this.entityDataManager.get(ai)).get());
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), playerMP);
         player.moveRelative(0.0F, 0.0F, 0.0F, 0.0F);
         playerMP.capabilities.isFlying = true;
         player.capabilities.isFlying = true;
         playerMP.noClip = true;
         player.noClip = true;
         playerMP.setNoGravity(true);
         player.setNoGravity(true);
         if ("Missionary".equals(handState)) {
            this.setCurrentAction(Action.MISSIONARY_START);
            Vec3d pos = this.getPositionVec3d().subtract(0.0, 0.1, 0.0);
            player.setPositionAndRotation(pos.x, pos.y, pos.z, this.getYawRotation(), 60.0F);
            player.setPositionAndUpdate(pos.x, pos.y, pos.z);
         } else {
            this.setCurrentAction(Action.COWGIRLSTART);
            Vec3d pos2 = this.getPositionVec3d()
               .add(
                  new Vec3d(
                     -Math.sin(this.getYawRotation().floatValue() * (Math.PI / 180.0)) * 1.8,
                     -0.65,
                     Math.cos(this.getYawRotation().floatValue() * (Math.PI / 180.0)) * 1.8
                  )
               );
            player.setPositionAndRotation(pos2.x, pos2.y, pos2.z, 180.0F + this.getYawRotation(), -30.0F);
            player.setPositionAndUpdate(pos2.x, pos2.y, pos2.z);
         }
      }
   }

   boolean hasNoOwner() {
      EntityPlayer player = this.getOwnerPlayer();
      return player == null
         ? false
         : this.world.getBlockState(player.getPosition().up().up()).getBlock() != Blocks.AIR;
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.ellie.eyes", true, event);
            } else {
               this.createAnimation("animation.ellie.null", true, event);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.ellie.null", true, event);
            } else if (this.ak) {
               this.createAnimation("animation.ellie.ride", true, event);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.ar = !this.ar;
               }

               if (!this.af) {
                  this.createAnimation("animation.ellie.fly" + (this.ar ? "2" : ""), true, event);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.movementController.setAnimationSpeed(1.5);
                     this.createAnimation(this.hasNoOwner() ? "animation.ellie.crouchwalk" : "animation.ellie.run", true, event);
                  } else if (this.ao.y >= -0.1F) {
                     this.movementController.setAnimationSpeed(2.0);
                     this.createAnimation(this.hasNoOwner() ? "animation.ellie.crouchwalk" : "animation.ellie.fastwalk", true, event);
                  } else {
                     this.movementController.setAnimationSpeed(1.5);
                     this.createAnimation(this.hasNoOwner() ? "animation.ellie.crouchwalk" : "animation.ellie.backwards_walk", true, event);
                  }
               } else {
                  this.createAnimation(this.hasNoOwner() ? "animation.ellie.crouchidle" : "animation.ellie.idle", true, event);
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.ellie.null", true, event);
                  break;
               case STRIP:
                  this.createAnimation("animation.ellie.strip", false, event);
                  break;
               case DASH:
                  this.createAnimation("animation.ellie.dash", false, event);
                  break;
               case HUG:
                  this.createAnimation("animation.ellie.hug", false, event);
                  break;
               case HUGIDLE:
                  this.createAnimation("animation.ellie.hugidle", true, event);
                  break;
               case HUGSELECTED:
                  this.createAnimation("animation.ellie.hugselected", false, event);
                  break;
               case SITDOWN:
                  this.createAnimation("animation.ellie.sitdown", false, event);
                  break;
               case SITDOWNIDLE:
                  this.createAnimation("animation.ellie.sitdownidle", true, event);
                  break;
               case COWGIRLSTART:
                  this.createAnimation("animation.ellie.cowgirlstart", false, event);
                  break;
               case COWGIRLSLOW:
                  this.createAnimation("animation.ellie.cowgirlslow2", true, event);
                  break;
               case COWGIRLFAST:
                  this.createAnimation("animation.ellie.cowgirlfast", true, event);
                  break;
               case COWGIRLCUM:
                  this.createAnimation("animation.ellie.cowgirlcum", true, event);
                  break;
               case ATTACK:
                  this.createAnimation("animation.ellie.attack" + this.nextAttack, false, event);
                  break;
               case BOW:
                  this.createAnimation("animation.ellie.bowcharge", false, event);
                  break;
               case RIDE:
                  this.createAnimation("animation.ellie.ride", true, event);
                  break;
               case SIT:
                  this.createAnimation("animation.ellie.sit", true, event);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.ellie.throwpearl", false, event);
                  break;
               case DOWNED:
                  this.createAnimation("animation.ellie.downed", true, event);
                  break;
               case MISSIONARY_START:
                  this.createAnimation("animation.ellie.missionary_start", false, event);
                  break;
               case MISSIONARY_SLOW:
                  this.createAnimation("animation.ellie.missionary_slow", true, event);
                  break;
               case MISSIONARY_FAST:
                  this.createAnimation("animation.ellie.missionary_fast", true, event);
                  break;
               case MISSIONARY_CUM:
                  this.createAnimation("animation.ellie.missionary_cum", false, event);
                  break;
               case CARRY_INTRO:
                  this.createAnimation("animation.ellie.carry_intro", false, event);
                  break;
               case CARRY_SLOW:
                  this.createAnimation("animation.ellie.carry_slow" + this.ap, true, event);
                  break;
               case CARRY_FAST:
                  this.createAnimation("animation.ellie.carry_fast", true, event);
                  break;
               case CARRY_CUM:
                  this.createAnimation("animation.ellie.carry_cum", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   /**
    * CLIENT: registers the controllers plus the sound listener driving the
    * carry/cowgirl/missionary scenes. Key transitions:
    * {@code sitdownDone} -&gt; {@link Action#SITDOWNIDLE} + scene menu,
    * {@code hugDone} -&gt; {@link Action#HUGIDLE} + menu,
    * {@code cowgirlStartDone}/{@code missionary_startDone} -&gt; slow loops,
    * {@code cowgirlfastReady}/{@code missionary_fastDone} gate fast on jump,
    * {@code carry_slowDone} re-rolls the variant suffix ({@code ap}),
    * {@code missionary_cumDone}/{@code cowgirlcumDone}/{@code carry_cumDone}
    * -&gt; {@code resetCameraAndPhysics()}. {@code hugselectedDone} targets
    * the girl's position via {@code targetPos} packet and sends
    * {@link SendGirlToSexPacket}.
    */
   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData data) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
            case "dashMSG1":
               EntityPlayer closestPlayer = this.world.getClosestPlayerToEntity(this, 15.0);
               if (closestPlayer != null) {
                  Vec3d delta = this.getPositionVector().subtract(closestPlayer.getPositionVector());
                  float yaw = (float)Math.atan2(delta.z, delta.x) * (float) (180.0 / Math.PI);
                  this.rotationYaw = yaw;
                  this.rotationYawHead = yaw;
                  this.renderYawOffset = yaw;
               }
               break;
            case "dashReady":
               if (this.isLocalPlayerNearby()) {
                  return;
               }
               break;
            case "dashDone":
               this.setCurrentAction(Action.HUG);
               EntityPlayer player = this.world.getClosestPlayerToEntity(this, 15.0);
               if (player != null) {
                  float yaw = player.rotationYaw;
                  this.rotationYaw = yaw;
                  this.rotationYawHead = yaw;
                  this.renderYawOffset = yaw;
               }
               break;
            case "hugMSG1":
               EntityPlayerSP localPlayer = Minecraft.getMinecraft().player;
               if (localPlayer.getPersistentID().equals(this.getInteractionPlayerUUID()) || localPlayer.getUniqueID().equals(this.getInteractionPlayerUUID())) {
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(localPlayer.getUniqueID().toString(), localPlayer.getPositionVector(), localPlayer.rotationYaw - 80.0F, localPlayer.rotationPitch)
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
               EntityPlayerSP hugPlayer = Minecraft.getMinecraft().player;
               if (hugPlayer.getPersistentID().equals(this.getInteractionPlayerUUID())) {
                  this.setCurrentAction(Action.HUGIDLE);
                  this.openEllieInventory(hugPlayer);
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
                  Vec3d pos = this.getPositionVector();
                  pos = pos.add(
                     -Math.sin((this.rotationYaw + 90.0F) * (Math.PI / 180.0)) * -0.7803125F,
                     0.0,
                     Math.cos((this.rotationYaw + 90.0F) * (Math.PI / 180.0)) * -0.7803125F
                  );
                  pos = pos.add(
                     -Math.sin(this.rotationYaw * (Math.PI / 180.0)) * 0.5296875F, 0.0, Math.cos(this.rotationYaw * (Math.PI / 180.0)) * 0.5296875F
                  );
                  String posStr = pos.x + "f" + pos.y + "f" + pos.z + "f";
                  PacketHandler.networkWrapper.sendToServer(new ChangeDataParameterPacket(this.getGirlId(), "targetPos", posStr));
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
               int oldState = this.ap;

               do {
                  this.ap = this.getRNG().nextInt(4) + 1;
               } while (this.ap == oldState);

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
      this.actionController.registerSoundListener(soundListener);
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
   }

}
