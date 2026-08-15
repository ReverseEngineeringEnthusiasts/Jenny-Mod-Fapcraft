package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.SkinColor;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.GirlInventoryScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.AllieModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.PlayerGoblinRenderer;
import com.trolmastercard.sexmod.util.EyeColor;
import com.trolmastercard.sexmod.util.HairColor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/**
 * <b>Role.</b> Player-form Goblin (implements {@link IGoblin}) — the
 * transformation that can itself be picked up and thrown like the NPC goblin.
 * When carried (owner UUID set), the girl renders on the owner's shoulder and
 * the OWNING player's entity is the one thrown; when unowned it behaves like a
 * normal player girl with nelson/paizuri scenes and the breeding scenes of the
 * NPC queen.
 * <p>
 * <b>State.</b> Own keys: {@code ax} (122) = carrying player's UUID,
 * {@code aA} (126) = pregnancy flag (NELSON_CUM). Inherits the kobold DNA keys
 * (119-121) via {@link AbstractKoboldPlayerEntity}; {@code setCustomPartList}
 * packs the trailing model-part index 9.
 * <p>
 * <b>Flow.</b> {@link #handlePlayerThrow(EntityPlayer)} starts the pickup;
 * {@link #updatePlayerThrowProgress()} launches the carrying player at tick 15
 * and unbinds at tick 39; {@link #handleThrowAction()} glues the transformed
 * player to the carrier's head while unthrown. Scenes start via
 * {@link #handleOwnerCommand(String, UUID)} (anal -&gt; nelson, paizuri);
 * {@link #handlePlayerInteract()}/{@link #handlePlayerLook()} position the
 * player for paizuri/nelson.
 * <p>
 * <b>Pitfalls.</b> {@link #isAnchored()} is true whenever carried
 * (owner != null) — many scene checks depend on it. {@link #isPlayerGirl()}
 * returns false while carried. {@link #onOwnerInteract(EntityPlayer)} resets
 * the girl when the carrier interacts. The inner class {@code a} handles the
 * client-side fake-player rendering and the sneak+click pickup trigger.
 */
public class GoblinPlayerEntity extends AbstractKoboldPlayerEntity implements IGoblin {
   public static final float aI = 2.0F;
   public static final DataParameter<String> ax = EntityDataManager.createKey(GoblinPlayerEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(122);
   public static final DataParameter<Boolean> aA = EntityDataManager.createKey(GoblinPlayerEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(126);
   int aJ = 0;
   int az = -1;
   int aG = 0;
   Action aw = Action.NULL;
   int aE = -1;
   boolean aC = false;
   boolean aB = true;
   boolean ay = true;
   boolean aF = false;
   boolean aH = false;
   String aD = "";

   public GoblinPlayerEntity(World world) {
      super(world);
   }

   public GoblinPlayerEntity(World world, UUID uuid) {
      super(world, uuid);
   }

   @Override
   public float getScaleFactor() {
      return 0.9F;
   }

   @Override
   public IVanillaModel getHandModel(int index) {
      return new AllieModel();
   }

   @Override
   public String getHandTexture(int index) {
      return "textures/entity/kobold/hand.png";
   }

   @Override
   public Vec3i getHandColor(int index) {
      String[] parts = getModelCodeParts(this);
      return parts.length < 8 ? super.getHandColor(index) : SkinColor.values()[Integer.parseInt(parts[7])].getColor();
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      EyeColor color = EyeColor.values()[this.getRNG().nextInt(EyeColor.values().length)];
      this.entityDataManager.register(au, new BlockPos(color.getColor()));
      this.entityDataManager.register(as, GoblinEntity.ax.name());
      this.entityDataManager.register(aA, false);
      this.entityDataManager.register(ax, "");
   }

   /**
    * SERVER: owner commands — {@code anal} starts {@link Action#NELSON_INTRO},
    * {@code paizuri} starts {@link Action#PAIZURI_START}. Both broadcast,
    * strip and teleport the acting player into the scene.
    */
   @Override
   public void handleOwnerCommand(String command, UUID uuid) {
      if ("anal".equals(command)) {
         this.teleportPlayerToGirl(uuid);
         this.setCurrentAction(Action.NELSON_INTRO);
         this.sendActionPacket(this.getOutfitIndex(), Action.NELSON_INTRO);
         this.setOutfitIndex(0);
      }

      if ("paizuri".equals(command)) {
         this.teleportPlayerToGirl(uuid);
         this.setCurrentAction(Action.PAIZURI_START);
         this.sendActionPacket(this.getOutfitIndex(), Action.PAIZURI_START);
         this.setOutfitIndex(0);
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean openInteractionMenu(EntityPlayer player) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(this, player, new String[]{"anal", "paizuri"}, null, false));
      return true;
   }

   @Override
   public EntityPlayer resolvePlayerEntity(EntityPlayer player) {
      UUID uuid = this.getOwnerUUID();
      if (uuid == null) {
         return player;
      }

      EntityPlayer ownerPlayer = this.world.getPlayerEntityByUUID(uuid);
      return ownerPlayer == null ? player : ownerPlayer;
   }

   @Override
   public boolean shouldRenderModel() {
      return this.getOwnerUUID() == null || !Minecraft.getMinecraft().player.getPersistentID().equals(this.getOwnerUserUUID());
   }

   @Override
   public boolean isRidingSomething() {
      UUID uuid = this.getOwnerUUID();
      return uuid == null;
   }

   @Override
   public Vec3d getOwnerLookVector(Vec3d vec, float partialTicks) {
      UUID uuid = this.getOwnerUUID();
      if (uuid == null) {
         return vec;
      }

      EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
      if (player == null) {
         return vec;
      }

      Vec3d curPos = player.getPositionVector();
      Vec3d prevPos = new Vec3d(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ);
      return RotationHelper.lerpVec3dDouble(prevPos, curPos, partialTicks);
   }

   /**
    * SERVER: the pickup trigger (sneak+click by a non-transformed player on
    * the carried player's entity) — binds the carrier and starts
    * {@link Action#PICK_UP} with the 45-tick hold countdown, locking the
    * carrying player's physics.
    */
   void handlePlayerThrow(EntityPlayer player) {
      if (this.getCurrentAction() == Action.NULL) {
         if (this.getOwnerUUID() == null) {
            if (GoblinEntity.hasGoblinWithUUID(player.getPersistentID())) {
               player.sendStatusMessage(new TextComponentString("you are already carrying a Goblin"), true);
            } else {
               this.setOwnerUUID(player.getPersistentID());
               this.setCurrentAction(Action.PICK_UP);
               this.setHeldPlayerDistance(45);
               EntityPlayer ownerPlayer = this.getOwnerPlayer();
               if (ownerPlayer != null) {
                  ownerPlayer.setNoGravity(true);
                  ownerPlayer.noClip = true;
                  if (!this.world.isRemote) {
                     PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)ownerPlayer);
                  }
               }
            }
         }
      }
   }

   @Override
   protected String buildModelCodeDNA(StringBuilder builder) {
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 3);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 7);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 7);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 5);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, HairColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, SkinColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, EyeColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 0);
      return builder.toString();
   }

   @Override
   public ArrayList<Integer> getCustomPartIdList() {
      return new ArrayList<Integer>() {
         {
            this.add(4);
            this.add(3);
            this.add(3);
            this.add(16);
            this.add(16);
            this.add(6);
            this.add(HairColor.values().length);
            this.add(SkinColor.values().length);
            this.add(EyeColor.values().length);
         }
      };
   }

   @Override
   public List<Integer> getCustomPartExtraIdList() {
      return Collections.singletonList(2);
   }

   @Override
   protected void clearBoneColors() {
      PlayerGoblinRenderer.clearRenderCache();
      GoblinRenderer.clearBoneColors();
   }

   public float getEyeHeight() {
      return 0.75F;
   }

   @Override
   public boolean isAnchored() {
      return super.isAnchored() || this.getOwnerUUID() != null;
   }

   @Override
   public boolean canPerformAction(Action action, EntityPlayer player) {
      UUID uuid = this.getOwnerUUID();
      if (uuid == null) {
         return false;
      }

      EntityPlayer ownerPlayer = this.world.getPlayerEntityByUUID(uuid);
      if (ownerPlayer == null) {
         return false;
      }

      float yaw = ownerPlayer.rotationYaw;
      float offset = action == Action.PICK_UP ? 180.0F : 0.0F;
      float minYaw = ownerPlayer.rotationYaw - 90.0F + offset;
      float maxYaw = ownerPlayer.rotationYaw + 90.0F + offset;
      if (yaw < minYaw) {
         player.rotationYaw = minYaw;
      }

      if (yaw > maxYaw) {
         player.rotationYaw = maxYaw;
      }

      float pitch = player.rotationPitch;
      float maxPitch = action == Action.PICK_UP ? 0.0F : 37.5F;
      if (pitch > maxPitch) {
         player.rotationPitch = maxPitch;
      }

      return true;
   }

   @Override
   public Vec3d getOwnerAimVector(Vec3d vec, float partialTicks) {
      UUID uuid = this.getOwnerUUID();
      if (uuid == null) {
         return vec;
      }

      EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
      if (player == null) {
         return vec;
      }

      float yaw = RotationHelper.lerp(player.prevRenderYawOffset, player.renderYawOffset, partialTicks);
      Vec3d aimVec = vec;
      float angle = 135.0F;
      Action action = this.getCurrentAction();
      if (action == Action.PICK_UP) {
         aimVec = new Vec3d(vec.x, vec.y, -vec.z);
         angle = 175.0F;
      } else if (action != Action.START_THROWING) {
         aimVec = aimVec.subtract(0.0, 2.0, 0.0);
      }

      return VectorMath.rotateByYaw(aimVec, yaw + angle);
   }

   @SideOnly(Side.CLIENT)
   void handleOwnerThrow() {
      EntityPlayer player = this.getOwnerPlayer();
      if (player != null) {
         if (this.getCurrentAction() == Action.START_THROWING) {
            player.isDead = false;
            if (!this.world.loadedEntityList.contains(player)) {
               this.world.spawnEntity(player);
            }
         }
      }
   }

   /**
    * BOTH sides: dispatches the throw glue ({@link #handleThrowAction()}),
    * the throw flight ({@link #updatePlayerThrowProgress()}) and the CLIENT
    * local action handling (owner revival, action diffing for the nelson
    * switch).
    */
   @Override
   public void onUpdate() {
      GoblinEntity.handleGoblinThrowAction(this);
      this.updatePlayerThrowProgress();
      this.handleThrowAction();
      super.onUpdate();
      if (this.world.isRemote) {
         this.handleOwnerThrow();
         Action action = this.getCurrentAction();
         this.handleLocalAction(action);
         this.handleNelsonAction(action);
         this.aw = action;
      }
   }

   @Override
   public boolean E_clash458() {
      return this.getOwnerUUID() != null;
   }

   /**
    * SERVER (mirrored on CLIENT): while carried (owner set) and not yet
    * thrown, keeps the transformed player glued 2 blocks above the carrier.
    */
   void handleThrowAction() {
      Action action = this.getCurrentAction();
      if (action != Action.THROWN) {
         if (action != Action.START_THROWING || this.getThrowProgress() <= 15) {
            UUID uuid = this.getOwnerUUID();
            if (uuid != null) {
               EntityPlayer owner = this.world.getPlayerEntityByUUID(uuid);
               if (owner != null) {
                  EntityPlayer player = this.getOwnerPlayer();
                  if (player != null) {
                     player.noClip = true;
                     player.setNoGravity(true);
                     player.setPosition(owner.posX, owner.posY + 2.0, owner.posZ);
                  }
               }
            }
         }
      }
   }

   /**
    * SERVER (mirrored on CLIENT): throw flight for the transformed player —
    * at tick 15 the carrying player is launched with the carrier's aim, at
    * tick 39 the throw ends (THROWN) and the owner/interaction bindings
    * clear.
    */
   void updatePlayerThrowProgress() {
      GoblinPlayerEntity goblin = this;
      int progress = goblin.getThrowProgress();
      if (progress != -1) {
         goblin.setThrowProgress(++progress);
         EntityPlayer player = this.getOwnerPlayer();
         if (player != null) {
            if (progress == 15) {
               GoblinEntity.getGoblinThrowPos(this);
               float pitch = GoblinEntity.getGoblinThrowHeight(this);
               float yaw = GoblinEntity.getGoblinThrowDistance(this);
               if (this.world.isRemote && this.hasOwnerUUID()) {
                  HandlePlayerMovement.setMovementLock(true);
               }

               Vec3d vec = GoblinEntity.rotateVectorPitchYaw(new Vec3d(0.0, 0.0, 1.5), pitch, yaw);
               player.motionX = vec.x;
               player.motionY = vec.y;
               player.motionZ = vec.z;
               if (!this.world.isRemote) {
                  this.setYawRotation(yaw);
               }
            }

            player.noClip = false;
            player.setNoGravity(false);
            if (progress == 39) {
               this.setThrowProgress(-1);
               this.setCurrentAction(Action.THROWN);
               this.setInteractionPlayerUUID(null);
               this.setOwnerUUID(null);
            }
         }
      }
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      GoblinEntity.handlePickUpState(this);
      this.handlePlayerThrown();
      this.handlePlayerStandUp();
   }

   void handlePlayerStandUp() {
      if (this.getCurrentAction() == Action.STAND_UP) {
         if (++this.aJ >= 37) {
            this.aJ = 0;
            this.setCurrentAction(Action.NULL);
         }
      }
   }

   void handlePlayerThrown() {
      if (this.getCurrentAction() == Action.THROWN) {
         EntityPlayer player = this.getOwnerPlayer();
         if (player != null) {
            if (player.onGround) {
               int throwTick = this.getThrowTickCount() + 1;
               this.setThrowTickCount(throwTick);
               if (throwTick >= 30) {
                  this.setThrowTickCount(0);
                  this.setCurrentAction(Action.STAND_UP);
               }
            }
         }
      }
   }

   @Nullable
   @Override
   public UUID getOwnerUUID() {
      String ownerStr = (String)this.entityDataManager.get(ax);
      if ("".equals(ownerStr)) {
         return null;
      }

      try {
         return UUID.fromString((String)this.entityDataManager.get(ax));
      } catch (Exception ex) {
         ex.printStackTrace();
         return null;
      }
   }

   @Override
   public void setOwnerUUID(UUID uuid) {
      if (uuid == null) {
         this.entityDataManager.set(ax, "");
      } else {
         this.entityDataManager.set(ax, uuid.toString());
      }
   }

   public EntityPlayer getOwnerPlayer() {
      UUID uuid = this.getOwnerUUID();
      return uuid == null ? null : this.world.getPlayerEntityByUUID(uuid);
   }

   @Override
   public void setThrowProgress(int progress) {
      this.az = progress;
   }

   @Override
   public int getThrowProgress() {
      return this.az;
   }

   @Override
   public void setThrowTickCount(int tickCount) {
      this.aG = tickCount;
   }

   @Override
   public int getThrowTickCount() {
      return this.aG;
   }

   @Override
   public void setPreviousAction(Action action) {
      this.aw = action;
   }

   @Override
   public Action getPreviousAction() {
      return this.aw;
   }

   @Override
   public void setHeldPlayerDistance(int distance) {
      this.aE = distance;
   }

   @Override
   public int getHeldPlayerDistance() {
      return this.aE;
   }

   @Override
   public void reinitTasks() {
      super.reinitTasks();
      this.entityDataManager.set(aA, false);
      if (this.getOwnerUUID() != null) {
         this.setOwnerUUID(null);
         EntityPlayer player = this.getOwnerPlayer();
         if (player != null) {
            PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)player);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void handleNelsonAction(Action action) {
      if (action == Action.NELSON_FAST && this.aw != Action.NELSON_FAST) {
         this.aF = false;
      }
   }

   @SideOnly(Side.CLIENT)
   void handleLocalAction(Action action) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.player.getPersistentID().equals(this.getInteractionPlayerUUID())) {
         if (mc.gameSettings.thirdPersonView == 0) {
            switch (action) {
               case NELSON_CUM:
               case NELSON_FAST:
               case NELSON_INTRO:
               case NELSON_SLOW:
                  mc.gameSettings.thirdPersonView = 2;
            }
         }
      }
   }

   @Override
   public void setCustomPartList(List<Integer> parts) {
      StringBuilder builder = new StringBuilder();

      for (int partId : parts) {
         AbstractNpcOnlyEntity.appendPaddedNumber(builder, partId);
      }

      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 1);
      this.entityDataManager.set(at, builder.toString());
   }

   @Nullable
   @Override
   protected Action getNextAction(Action action) {
      switch (action) {
         case NELSON_SLOW:
            return Action.NELSON_FAST;
         case PAIZURI_IDLE:
         case PAIZURI_SLOW:
            return Action.PAIZURI_FAST;
         case BREEDING_SLOW_0:
            return Action.BREEDING_FAST_0;
         case BREEDING_SLOW_2:
            return Action.BREEDING_FAST_2;
         default:
            return null;
      }
   }

   /**
    * Guards the state machine: refuses re-entry into loop phases while the
    * cum animation plays, positions the player for paizuri/nelson on the
    * server and toggles the pregnancy flag ({@code aA}) on NELSON_CUM.
    */
   @Override
   public void setCurrentAction(Action action) {
      Action currentAction = this.getCurrentAction();
      if (currentAction != Action.PAIZURI_CUM || action != Action.PAIZURI_SLOW && action != Action.PAIZURI_FAST) {
         if (currentAction != Action.NELSON_CUM || action != Action.NELSON_SLOW && action != Action.NELSON_FAST) {
            if (currentAction != Action.BREEDING_CUM_0 || action != Action.BREEDING_SLOW_0 && action != Action.BREEDING_FAST_0) {
               if (action == Action.PAIZURI_START && !this.world.isRemote) {
                  this.handlePlayerInteract();
               }

               if (action == Action.NELSON_INTRO && !this.world.isRemote) {
                  this.handlePlayerLook();
               }

               if (action == Action.NELSON_CUM) {
                  this.entityDataManager.set(aA, true);
               }

               if (currentAction == Action.NELSON_CUM && action != Action.NELSON_CUM) {
                  this.entityDataManager.set(aA, false);
               }

               super.setCurrentAction(action);
            }
         }
      }
   }

   void handlePlayerLook() {
      EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      if (player != null) {
         this.setYawRotation(player.rotationYaw);
         this.noClip = true;
         this.setNoGravity(true);
         player.setNoGravity(true);
         player.noClip = true;
         player.setPositionAndUpdate(player.posX, player.posY, player.posZ - 1.0);
      }
   }

   void handlePlayerInteract() {
      EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      if (player != null) {
         this.setYawRotation(player.rotationYaw + 180.0F);
         this.noClip = true;
         this.setNoGravity(true);
         player.setNoGravity(true);
         player.noClip = true;
         player.setPositionAndUpdate(player.posX, player.posY - 0.5, player.posZ - 0.6F);
         player.rotationPitch = 70.0F;
         player.prevRotationPitch = 70.0F;
      }
   }

   @Override
   public boolean isPlayerGirl() {
      return this.getOwnerUUID() == null;
   }

   /**
    * SERVER: the carrier's interaction — resets the girl, un-anchors and
    * unbinds the carrier.
    */
   @Override
   public void onOwnerInteract(EntityPlayer player) {
      if (player.getPersistentID().equals(this.getOwnerUUID())) {
         ResetGirlPacket.Handler.resetGirl(this);
         this.setAnchored(false);
         this.setCurrentAction(Action.NULL);
         this.setOwnerUUID(null);
      }
   }

   @Override
   protected Action getCumAction(Action action) {
      switch (action) {
         case NELSON_FAST:
         case NELSON_SLOW:
            return Action.NELSON_CUM;
         case NELSON_INTRO:
         case PAIZURI_IDLE:
         case BREEDING_SLOW_0:
         default:
            return null;
         case PAIZURI_SLOW:
         case PAIZURI_FAST:
         case PAIZURI_FAST_CONTINUES:
            return Action.PAIZURI_CUM;
         case BREEDING_SLOW_2:
         case BREEDING_FAST_2:
            return Action.BREEDING_CUM_2;
         case BREEDING_1:
            return Action.BREEDING_CUM_1;
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      if (this.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.goblin.blink", true, event);
            } else {
               this.createAnimation("animation.goblin.null", true, event);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.goblin.null", true, event);
            } else if (this.ak) {
               this.createAnimation("animation.goblin.sit", true, event);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.aC = !this.aC;
               }

               if (!this.af) {
                  this.createAnimation("animation.goblin.fly" + (this.aC ? "2" : ""), true, event);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.movementController.setAnimationSpeed(1.2F);
                     this.createAnimation("animation.goblin.running", true, event);
                  } else if (this.ao.y >= -0.1F) {
                     this.movementController.setAnimationSpeed(2.0);
                     this.createAnimation("animation.goblin.walk", true, event);
                  } else {
                     this.movementController.setAnimationSpeed(1.5);
                     this.createAnimation("animation.goblin.backwards_walk", true, event);
                  }
               } else {
                  this.createAnimation("animation.goblin.idle", true, event);
               }
            }
            break;
         case "action":
            Minecraft mc = Minecraft.getMinecraft();
            String camMode = mc.player.getPersistentID().equals(this.getOwnerUUID()) && mc.gameSettings.thirdPersonView == 0 ? "1" : "3";
            switch (this.getCurrentAction()) {
               case NELSON_CUM:
                  this.createAnimation("animation.goblin.nelson_cum", true, event);
                  break;
               case NELSON_FAST:
                  this.createAnimation("animation.goblin.nelson_fast" + (this.aF ? "c" : "s"), true, event);
                  break;
               case NELSON_INTRO:
                  this.createAnimation("animation.goblin.nelson_intro", true, event);
                  break;
               case NELSON_SLOW:
                  this.createAnimation("animation.goblin.nelson_slow" + (this.ay ? "" : "2"), true, event);
                  break;
               case PAIZURI_IDLE:
                  this.createAnimation("animation.goblin.paizuri_idle", true, event);
                  break;
               case PAIZURI_SLOW:
                  this.createAnimation("animation.goblin.paizuri_slow" + this.aD, true, event);
                  break;
               case BREEDING_SLOW_0:
                  this.createAnimation("animation.goblin.breeding_slow_1" + (this.aB ? "l" : "r"), true, event);
                  break;
               case BREEDING_SLOW_2:
                  this.createAnimation("animation.goblin.breeding_slow_3", true, event);
                  break;
               case PAIZURI_FAST:
                  this.createAnimation("animation.goblin.paizuri_fast", true, event);
                  break;
               case PAIZURI_FAST_CONTINUES:
                  this.createAnimation("animation.goblin.paizuri_fast_countinues", true, event);
                  break;
               case BREEDING_1:
                  this.createAnimation("animation.goblin.breeding_2", true, event);
                  break;
               case BREEDING_FAST_2:
                  this.createAnimation("animation.goblin.breeding_fast_3", true, event);
                  break;
               case SHOULDER_IDLE:
                  this.createAnimation("animation.goblin.shoulder_idle", true, event);
                  break;
               case PICK_UP:
                  this.createAnimation(String.format("animation.goblin.pick_up_%sperson", camMode), true, event);
                  break;
               case START_THROWING:
                  this.createAnimation(String.format("animation.goblin.throw_%sperson", camMode), true, event);
                  break;
               case THROWN:
                  this.createAnimation("animation.goblin.thrown", true, event);
                  break;
               case NULL:
                  this.createAnimation("animation.goblin.null", true, event);
                  break;
               case STAND_UP:
                  this.createAnimation("animation.goblin.stand_up", false, event);
                  break;
               case STRIP:
                  this.createAnimation("animation.goblin.strip", false, event);
                  break;
               case ATTACK:
                  this.createAnimation("animation.goblin.attack" + this.nextAttack, false, event);
                  break;
               case BOW:
                  this.createAnimation("animation.goblin.bowcharge", false, event);
                  break;
               case SIT:
                  this.createAnimation("animation.goblin.sit", true, event);
                  break;
               case BREEDING_INTRO_0:
                  this.createAnimation("animation.goblin.breeding_intro_1", true, event);
                  break;
               case BREEDING_INTRO_1:
                  this.createAnimation("animation.goblin.breeding_intro_2", true, event);
                  break;
               case BREEDING_INTRO_2:
                  this.createAnimation("animation.goblin.breeding_intro_3", true, event);
                  break;
               case BREEDING_FAST_0:
                  this.createAnimation("animation.goblin.breeding_fast_1" + (this.aH ? "c" : "s"), true, event);
                  break;
               case BREEDING_CUM_0:
                  this.createAnimation("animation.goblin.breeding_cum_1", true, event);
                  break;
               case BREEDING_CUM_1:
                  this.createAnimation("animation.goblin.breeding_cum_2", true, event);
                  break;
               case BREEDING_CUM_2:
                  this.createAnimation("animation.goblin.breeding_cum_3", true, event);
                  break;
               case PAIZURI_START:
                  this.createAnimation("animation.goblin.paizuri_start", true, event);
                  break;
               case PAIZURI_CUM:
                  this.createAnimation("animation.goblin.paizuri_cum", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   /**
    * CLIENT: registers the controllers plus the sound listener driving the
    * catch dialogue, paizuri, nelson and breeding scenes. Key transitions:
    * {@code catchDone} -&gt; {@link Action#CATCH_BJ}, {@code paizuri_startDone}
    * -&gt; {@link Action#PAIZURI_IDLE}, {@code neslon_introDone} -&gt;
    * {@link Action#NELSON_SLOW}, {@code breedingIntroDone} -&gt;
    * {@link Action#BREEDING_SLOW_0}, jump on the ready keyframes switches
    * fast/hard, {@code paizuriCumDone}/{@code nelson_cumDone} -&gt;
    * {@code resetCameraAndPhysics()} + NULL. Movement controller uses a
    * 2-tick transition.
    */
   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData data) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
            case "attackDone":
               if (++this.nextAttack == 3) {
                  this.nextAttack = 0;
               }
               break;
            case "catchEh":
               this.sendChatMessage("ehh..");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchAkward":
               this.sendChatMessage("awkward..");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchWell":
               this.sendChatMessage("well...");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchRather":
               this.sendChatMessage("would you rather have this stupid... thing?");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchMe":
               this.sendChatMessage("...or use me?~");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchDone":
               if ("bj".equals(this.entityDataManager.get(GIRL_HAND_STATES))) {
                  this.setCurrentAction(Action.CATCH_BJ);
               }
               break;
            case "catchBjDone":
               this.setCurrentAction(Action.CATCH_BJ_IDLE);
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  openInventoryGui(player, this, new String[]{"use her", "take ur stuff back"}, null, false);
               }
               break;
            case "paizuriChoice":
               this.sendChatMessage("good choice!~");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizuriBoth":
               this.sendChatMessage("...for both of us!");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizruiUse":
               this.sendChatMessage("now use me like a fuck toy!~");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizuriSwitch":
               if (!this.getRNG().nextBoolean()) {
                  this.aD = "".equals(this.aD) ? "2" : "";
               }
               break;
            case "touch":
               this.playRandomSoundAtVolume(SoundHandler.MISC_TOUCH, 3.0F);
               break;
            case "pound":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "paizuri_startDone":
               this.setCurrentAction(Action.PAIZURI_IDLE);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "paizuriFastDone":
               this.setCurrentAction(Action.PAIZURI_SLOW);
               break;
            case "paizuriFastReady":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.PAIZURI_FAST_CONTINUES);
               }
               break;
            case "paizuriFastContinuesReady":
            case "neslon_fastBackSwitch":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "smallPound":
               this.playRandomSoundAtVolume(SoundHandler.MISC_POUNDING, 0.25F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "paizruiCam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  player.rotationPitch = 70.0F;
                  player.prevRotationPitch = 70.0F;
               }
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "cumSound":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "jumpCam":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft mc = Minecraft.getMinecraft();
                  mc.player.rotationYaw = this.getYawRotation() + 170.0F;
                  mc.player.rotationPitch = -20.0F;
                  mc.player.rotationYawHead = mc.player.rotationYaw;
                  mc.gameSettings.thirdPersonView = 2;
               }
               break;
            case "breedingHmm":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft mc = Minecraft.getMinecraft();
                  mc.player.rotationYaw = this.getYawRotation() + 180.0F;
                  mc.player.rotationPitch = -15.0F;
                  mc.player.rotationYawHead = mc.player.rotationYaw;
                  mc.gameSettings.thirdPersonView = 0;
               }

               this.sendChatMessage("hmm...");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingFound":
               this.sendChatMessage("guess we found a worthy breeding partner!");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingEnough":
               this.sendChatMessage("Eh.. go pin him down, before he runs off!");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingCam2":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft mc = Minecraft.getMinecraft();
                  mc.gameSettings.thirdPersonView = 2;
                  mc.player.rotationYaw = this.getYawRotation() - 120.0F;
                  mc.player.rotationPitch = -30.0F;
               }
            case "breedingIntroDone":
               this.setCurrentAction(Action.BREEDING_SLOW_0);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "breeding_slow1Done":
               if (this.getRNG().nextBoolean()) {
                  this.aB = !this.aB;
               }

               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.BREEDING_FAST_0);
                  this.aH = false;
               }
               break;
            case "breeding_fast1Done":
               this.setCurrentAction(Action.BREEDING_SLOW_0);
               if (this.isControlledByLocalPlayer()) {
                  this.aH = false;
               }
               break;
            case "breeding_fast1Ready":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.aH = true;
                  this.resetAnimationControllerOffset();
                  this.actionController.tickOffset = 0.0;
               }
               break;
            case "cum":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "breeding_intro_3Done":
               this.setCurrentAction(Action.BREEDING_SLOW_2);
               break;
            case "breeding_3_wiggle":
               if (this.getRNG().nextBoolean()) {
                  this.actionController.tickOffset = 0.0;
               }
               break;
            case "breeding_fast_3Done":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.BREEDING_SLOW_2);
               }
               break;
            case "breeding_intro_2Done":
               this.setCurrentAction(Action.BREEDING_1);
               break;
            case "breeding_cumCam":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft mc = Minecraft.getMinecraft();
                  mc.gameSettings.thirdPersonView = 0;
                  mc.player.rotationYaw = this.getYawRotation() + 180.0F;
                  mc.player.rotationPitch = -15.0F;
                  mc.player.rotationYawHead = mc.player.rotationYaw;
                  mc.gameSettings.thirdPersonView = 0;
               }
               break;
            case "neslon_introDone":
               this.setCurrentAction(Action.NELSON_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "nelson_slowDone":
               if (this.getRNG().nextBoolean()) {
                  this.ay = !this.ay;
               }
               break;
            case "neslon_fastSwitch":
               if (!this.isControlledByLocalPlayer()) {
                  this.aF = true;
                  return;
               }

               if (HandlePlayerMovement.isJumping) {
                  this.aF = true;
               }
               break;
            case "nelsonFastDone":
               this.aF = false;
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.NELSON_SLOW);
               }
               break;
            case "paizuriCumDone":
            case "nelson_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.resetCameraAndPhysics();
                  this.setCurrentAction(Action.NULL);
               }
         }
      };
      this.actionController.registerSoundListener(soundListener);
      this.movementController.transitionLengthTicks = 2.0;
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
   }

   /**
    * CLIENT-only event handler for the goblin transformation: hides the
    * first-person hand while a goblin is carried ({@code onRenderHand}),
    * glues the carried player to the carrier's head every tick
    * ({@code onPlayerTick}/{@code onRenderTickSync}), re-renders fake player
    * entities of carried goblins ({@code onRenderWorldLast} with the
    * clear/kill fake-player pair around each frame), and starts the
    * pickup/throw when a non-transformed player sneaks+clicks a carried
    * goblin player ({@code onEntityInteract}).
    */
   public static class a {
      HashSet<EntityPlayer> playersToRender = new HashSet<>();

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onRenderHand(RenderHandEvent event) {
         AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(Minecraft.getMinecraft().player);
         if (playerGirl != null) {
            if (playerGirl instanceof IGoblin) {
               if (((IGoblin)playerGirl).getOwnerUUID() != null) {
                  event.setCanceled(true);
               }
            }
         }
      }

      @SubscribeEvent
      public void onPlayerTick(PlayerTickEvent event) {
         EntityPlayer player = event.player;
         if (player != null) {
            this.handlePlayerOwner(player);
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onRenderTickSync(RenderTickEvent event) {
         if (event.phase != Phase.END) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            if (player != null) {
               this.handlePlayerOwner(player);
            }
         }
      }

      void handlePlayerOwner(EntityPlayer player) {
         AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player);
         if (playerGirl instanceof GoblinPlayerEntity) {
            Action action = playerGirl.getCurrentAction();
            if (action != Action.THROWN) {
               if (action != Action.START_THROWING || ((IGoblin)playerGirl).getThrowProgress() <= 15) {
                  UUID uuid = ((GoblinPlayerEntity)playerGirl).getOwnerUUID();
                  if (uuid != null) {
                     EntityPlayer owner = player.world.getPlayerEntityByUUID(uuid);
                     if (owner != null) {
                        player.noClip = true;
                        player.setNoGravity(true);
                        playerGirl.noClip = true;
                        playerGirl.setNoGravity(true);
                        player.setPosition(owner.posX, owner.posY + 2.0, owner.posZ);
                        player.lastTickPosX = owner.lastTickPosX;
                        player.lastTickPosY = owner.lastTickPosY + 2.0;
                        player.lastTickPosZ = owner.lastTickPosZ;
                     }
                  }
               }
            }
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onRenderWorldLast(RenderWorldLastEvent event) {
         Minecraft mc = Minecraft.getMinecraft();
         RenderManager renderManager = mc.getRenderManager();
         EntityPlayerSP localPlayer = mc.player;
         if (mc.player != null) {
            Vec3d playerPos = localPlayer.getPositionVector();

            for (EntityPlayer player : this.playersToRender) {
               Vec3d playerVec = player.getPositionVector();
               Vec3d delta = playerVec.subtract(playerPos);
               renderManager.renderEntity(player, delta.x, delta.y, delta.z, 69.0F, event.getPartialTicks(), true);
            }

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onRenderTick(RenderTickEvent event) {
         if (event.phase == Phase.START) {
            this.clearFakePlayers();
         } else {
            this.killFakePlayers();
         }
      }

      @SideOnly(Side.CLIENT)
      void killFakePlayers() {
         for (EntityPlayer player : this.playersToRender) {
            player.isDead = true;
         }
      }

      @SideOnly(Side.CLIENT)
      void clearFakePlayers() {
         this.playersToRender.clear();
         Minecraft mc = Minecraft.getMinecraft();
         EntityPlayerSP localPlayer = mc.player;
         if (mc.world != null) {
            for (EntityPlayer player : mc.world.playerEntities) {
               if (player != localPlayer) {
                  AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player);
                  if (playerGirl instanceof GoblinPlayerEntity) {
                     GoblinPlayerEntity goblin = (GoblinPlayerEntity)playerGirl;
                     if (goblin.getOwnerUUID() != null) {
                        Action action = goblin.getCurrentAction();
                        if (action == Action.THROWN || action == Action.START_THROWING) {
                           return;
                        }

                        this.playersToRender.add(player);
                        player.isDead = false;
                     }
                  }
               }
            }
         }
      }

      @SubscribeEvent
      public void onEntityInteract(EntityInteract event) {
         EntityPlayer player = event.getEntityPlayer();
         if (player.isSneaking()) {
            if (event.getTarget() instanceof EntityPlayer) {
               AbstractPlayerGirlEntity targetGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(event.getTarget().getPersistentID());
               if (targetGirl instanceof GoblinPlayerEntity) {
                  AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player.getPersistentID());
                  if (playerGirl == null) {
                     ((GoblinPlayerEntity)targetGirl).handlePlayerThrow(event.getEntityPlayer());
                  }
               }
            }
         }
      }

   }
}
