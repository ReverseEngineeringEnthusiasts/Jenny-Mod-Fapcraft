package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.IGalathFinish;
import com.trolmastercard.sexmod.client.gui.GenderSwapScreen;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.ForcePlayerGirlUpdatePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.networking.SexPromptPacket;
import com.trolmastercard.sexmod.util.DebugMode;
import com.trolmastercard.sexmod.util.GalathGeometryRender;
import com.trolmastercard.sexmod.util.GirlCombatProtection;
import com.trolmastercard.sexmod.util.EntityLookVectorHelper;
import com.trolmastercard.sexmod.util.GoblinFirstPersonRenderer;
import com.trolmastercard.sexmod.util.TrailSegment;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector2f;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> Base class for the player-form girls — the entity the player
 * turns INTO when drinking the horny potion. Subclasses (Jenny, Bia, Luna,
 * Ellie, Slime, Bee, Allie, Kobold, Goblin, Galath) mirror their NPC twin's
 * scenes but bind a real player ({@code ai}, data-manager id 118) whose model,
 * camera and abilities are replaced by the girl's while the transformation is
 * active. Registered statically in {@link #playerGirlList}/{@link #al}.
 * <p>
 * <b>Scene flow.</b> Same machinery as {@link BaseGirlEntity}: actions are
 * dispatched from {@link #doAction(String, UUID)} (which routes unknown
 * actions to the server via {@link SexPromptPacket}), the owner command GUI
 * calls {@link #handleOwnerCommand(String, UUID)}, and scene entry funnels
 * through {@link #teleportPlayerToGirl(UUID)} (snaps both players in, anchors
 * the girl, sets {@code IS_ANCHORED}). Scene exit goes through
 * {@link #resetCameraAndPhysics()} -&gt; {@link #resetLocalPlayerClientState()}
 * which unlocks movement, un-hides the player and sends
 * {@link ResetGirlPacket}.
 * <p>
 * <b>State.</b> {@code ai} (118) = owner player UUID; {@code ab} = "new player"
 * flag consumed by {@link SexPromptPacket}; {@code yFlag} = strip-phase tick
 * threshold (65); {@code an} = STRIP countdown (-1 = inactive, toggles the
 * outfit at 65 and ends the strip on the client at 100).
 * <p>
 * <b>Pitfalls.</b> {@link #setCurrentAction(Action)} refuses
 * {@code NULL} while anchored (the "prevented a potential animation break"
 * guard) — do not remove; {@link #resetCameraAndPhysics()} only resets
 * physics locally and delegates the player reset to the server. The camera
 * origin is stored in {@code cameraOriginPos} and must be cleared on every
 * reset path or the next scene repositions the player from a stale anchor.
 * Player girls are {@code noClip} + no-gravity and never collide
 * ({@code canBeCollidedWith} = false).
 */
public abstract class AbstractPlayerGirlEntity extends AbstractGirlNpcEntity {
   public static final String aa = "sexmod:CustomModel";
   public static final String ae = "sexmod:GirlSpecific";
   public static final float ac = 0.0F;
   public static final int am = 100;
   public static final int yFlag = 65;
   public static boolean ag = true;
   public Vector2f ao = new Vector2f(0.0F, 0.0F);
   public boolean ad = false;
   public boolean aj = false;
   public boolean ak = false;
   public boolean af = true;
   public boolean ah = false;
   protected static final DataParameter<Optional<UUID>> ai = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID)
      .getSerializer()
      .createKey(118);
   public static Hashtable<UUID, AbstractPlayerGirlEntity> al = new Hashtable<>();
   public static List<AbstractPlayerGirlEntity> playerGirlList = new ArrayList<>();
   int an = -1;
   public boolean ab = true;

   protected AbstractPlayerGirlEntity(World world) {
      super(world);
      this.setSize(0.01F, 0.01F);
      playerGirlList.add(this);
   }

   protected AbstractPlayerGirlEntity(World world, UUID uuid) {
      this(world);
      this.entityDataManager.set(ai, Optional.of(uuid));
   }

   @Nullable
   public static AbstractPlayerGirlEntity getPlayerGirlByUUID(UUID uuid) {
      return al.get(uuid);
   }

   @Nullable
   public static AbstractPlayerGirlEntity getPlayerGirlByUUID(@Nonnull EntityPlayer player) {
      return al.get(player.getPersistentID());
   }

   @Nullable
   public static AbstractPlayerGirlEntity getPlayerGirlByOwner(UUID uuid) {
      try {
         for (BaseGirlEntity girl : getGirlEntityList()) {
            if (!girl.world.isRemote && girl instanceof AbstractPlayerGirlEntity) {
               AbstractPlayerGirlEntity playerGirl = (AbstractPlayerGirlEntity)girl;
               if (uuid.equals(playerGirl.getOwnerUserUUID())) {
                  return playerGirl;
               }
            }
         }
      } catch (ConcurrentModificationException ex) {
      }

      return null;
   }

   @Override
   public TargetPoint getTargetNetworkPoint() {
      return new TargetPoint(this.dimension, this.posX, this.posY - 0.0, this.posZ, 50.0);
   }

   /**
    * Broadcasts an action change to every entity tracking this girl
    * (50-block radius) via {@link ForcePlayerGirlUpdatePacket}, so clients
    * other than the owner see the scene action too. SERVER-side.
    */
   public void sendActionPacket(int actionId, Action action) {
      PacketHandler.networkWrapper.sendToAllTracking(new ForcePlayerGirlUpdatePacket(this.getOwnerUserUUID(), actionId, action), this.getTargetNetworkPoint());
   }

   public EntityPlayer resolvePlayerEntity(EntityPlayer player) {
      return player;
   }

   public boolean isRidingSomething() {
      return true;
   }

   public Vec3d getOwnerLookVector(Vec3d vec, float partialTicks) {
      return vec;
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean canBeInteracted() {
      return true;
   }

   public boolean canMountPlayer() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public void H_clash570() {
   }

   public boolean canOpenInteractionMenu() {
      return true;
   }

   public boolean handleActionRequest(String actionName) {
      return false;
   }

   public boolean A_clash381() {
      return true;
   }

   @Override
   public String getDisplayNameText() {
      if (((Optional)this.entityDataManager.get(ai)).isPresent()) {
         EntityPlayer player = this.world.getPlayerEntityByUUID((UUID)((Optional)this.entityDataManager.get(ai)).get());
         if (player != null) {
            return player.getName();
         }
      }

      return "anonymous horny girl";
   }

   public void handleInteraction() {
   }

   public abstract void handleOwnerCommand(String command, UUID uuid);

   public abstract IVanillaModel getHandModel(int index);

   public abstract String getHandTexture(int index);

   public Vec3i getHandColor(int index) {
      return new Vec3i(255, 255, 255);
   }

   @Override
   public boolean canBePushed() {
      return false;
   }

   public boolean isNotColliding() {
      return true;
   }

   public boolean F_clash231() {
      return false;
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.entityDataManager.register(ai, Optional.absent());
   }

   /**
    * CLIENT-side entry point used by the camera/escape handlers: looks up the
    * local player's girl entity and resets it.
    */
   @SideOnly(Side.CLIENT)
   public static void resetPlayerGirlCamera() {
      AbstractPlayerGirlEntity playerGirl = getPlayerGirlByUUID(Minecraft.getMinecraft().player.getPersistentID());
      if (playerGirl != null) {
         playerGirl.resetCameraAndPhysics();
      }
   }

   /**
    * Ends the transformation locally: clears the camera origin, re-enables
    * gravity, and (CLIENT) hands off to {@link #resetLocalPlayerClientState()}.
    * The player-side state (visibility, movement lock, abilities) is only
    * restored when the server processes the resulting {@link ResetGirlPacket}.
    */
   @Override
   public void resetCameraAndPhysics() {
      this.cameraOriginPos = null;
      this.setNoGravity(false);
      if (this.world.isRemote) {
         this.resetLocalPlayerClientState();
      }
   }

   /**
    * CLIENT: unlocks player movement, un-hides the player and tells the
    * server to reset the girl. Note the single-arg {@link ResetGirlPacket}
    * here — that is the FULL reset form ({@code resetPose=false}); the
    * two-arg form is only used for player-only resets.
    */
   @SideOnly(Side.CLIENT)
   @Override
   protected void resetLocalPlayerClientState() {
      if (this.isControlledByLocalPlayer() || this.hasOwnerUUID()) {
         HandlePlayerMovement.setMovementLock(true);
         EntityPlayerSP player = Minecraft.getMinecraft().player;
         player.setInvisible(false);
         player.setNoGravity(false);
         player.noClip = false;
         this.entityDataManager.set(IS_ANCHORED, false);
         PacketHandler.networkWrapper.sendToServer(new ResetGirlPacket(this.getGirlId()));
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean hasCustomParts() {
      Minecraft mc = Minecraft.getMinecraft();
      return !this.hasOwnerUUID() || mc.gameSettings.thirdPersonView != 0;
   }

   /**
    * Grants/revokes flight on the owning player (SERIOUS: uses the player's
    * abilities and sends them to the client). {@code ag} is a global kill
    * switch. Called by subclasses' {@code B_clash233}/{@code onTickClient}
    * pairs on transformation enter/exit.
    */
   protected void handleOwnerUUID(boolean allowFly) {
      if (ag) {
         if (this.getOwnerUserUUID() != null) {
            EntityPlayer player = this.world.getPlayerEntityByUUID(this.getOwnerUserUUID());
            if (player != null) {
               player.capabilities.allowFlying = allowFly;
               if (!allowFly) {
                  player.capabilities.isFlying = false;
               }

               player.sendPlayerAbilities();
            }
         }
      }
   }

   public static boolean hasPlayerGirlWithUUID(UUID uuid) {
      rebuildPlayerGirlTableFromWorld();

      for (Entry entry : al.entrySet()) {
         UUID candidateUuid = (UUID)entry.getKey();
         if (uuid.equals(candidateUuid)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isOwnerPlayer(EntityPlayer player) {
      return player == null ? false : hasPlayerGirlWithUUID(player.getPersistentID());
   }

   public AxisAlignedBB getEntityBoundingBox() {
      return super.getEntityBoundingBox().offset(0.0, 0.5, 0.0);
   }

   protected EntityPlayer getNearestPlayer() {
      List players = this.world.playerEntities;
      EntityPlayer nearest = null;

      for (EntityPlayer player : (java.util.Collection<EntityPlayer>) (players) ) {
         if (!player.getPersistentID().equals(((Optional)this.entityDataManager.get(ai)).get())) {
            if (nearest == null) {
               nearest = player;
            } else {
               double closestDist = nearest.getDistanceSq(this.getPositionVec3d().x, this.getPositionVec3d().y, this.getPositionVec3d().z);
               double dist = player.getDistanceSq(this.getPositionVec3d().x, this.getPositionVec3d().y, this.getPositionVec3d().z);
               if (dist < closestDist) {
                  nearest = player;
               }
            }
         }
      }

      return nearest;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean isLocalPlayerNearby() {
      EntityPlayer player = this.getNearestPlayer();
      return player == null ? false : player.getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID());
   }

   public Vec3d getPositionVec3d() {
      return new Vec3d(this.posX, this.posY - 0.0, this.posZ);
   }

   /**
    * SERVER: snaps both the acting player and the girl's owner into the scene
    * around the girl's position, locks their movement (SetPlayerMovementPacket),
    * anchors the girl and records the target position/yaw. This is the standard
    * scene-entry funnel for every owner-command action.
    */
   protected void teleportPlayerToGirl(UUID uuid) {
      EntityPlayerMP player = (EntityPlayerMP)this.world.getPlayerEntityByUUID(uuid);
      EntityPlayerMP ownerPlayer = (EntityPlayerMP)this.world.getPlayerEntityByUUID((UUID)((Optional)this.entityDataManager.get(ai)).get());
      PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), player);
      PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), ownerPlayer);
      this.setInteractionPlayerUUID(uuid);
      this.rotationYaw = 0.0F;
      this.rotationYawHead = 0.0F;
      player.rotationYaw = 180.0F;
      player.rotationYawHead = 180.0F;
      player.setNoGravity(true);
      player.noClip = true;
      Vec3d pos = this.getPositionVector();
      player.setPositionAndUpdate(pos.x, pos.y, pos.z + 1.0);
      player.capabilities.isFlying = true;
      ownerPlayer.capabilities.isFlying = true;
      this.snapPlayerToPosition(uuid);
      this.entityDataManager.set(IS_ANCHORED, true);
      this.setTargetPosition(pos);
      this.setYawRotation(0.0F);
   }

   protected void playStepSound(BlockPos pos, Block block) {
      super.playStepSound(pos, block);
   }

   public AxisAlignedBB getPlayerCollisionBox(EntityPlayer player) {
      return player.getEntityBoundingBox();
   }

   /**
    * Runs every tick on both sides: forces noClip + no gravity, keeps the
    * girl glued to the owner player's position when not anchored, mirrors the
    * player's swing into the {@link Action#ATTACK} scene action, and ticks the
    * gender-swap screen client-side.
    */
   @Override
   public void onUpdate() {
      this.noClip = true;
      this.setNoGravity(true);
      super.onUpdate();
      this.D_clash581();
      if (this.world.isRemote) {
         if (this.hasOwnerUUID()) {
            GenderSwapScreen.instance.tick();
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void updateEyeHeight() {
      Minecraft.getMinecraft().player.eyeHeight = this.getEyeHeight();
   }

   @SideOnly(Side.CLIENT)
   public boolean hasOwnerUUID() {
      return !((Optional)this.entityDataManager.get(ai)).isPresent()
         ? false
         : ((UUID)((Optional)this.entityDataManager.get(ai)).get()).equals(Minecraft.getMinecraft().player.getPersistentID());
   }

   public boolean E_clash458() {
      return false;
   }

   void saveOwnerData(EntityPlayer player) {
      NBTTagCompound nbt = player.getEntityData();
      String modelCode = nbt.getString("sexmod:CustomModel" + NpcType.getNpcType(this));
      this.setCustomModelCode(modelCode);
   }

   @Override
   public void updateAITasks() {
      rebuildPlayerGirlTableFromWorld();
      this.tickFollowUpTransitions();
      this.updateCustomModelParts();
      UUID uuid = this.getOwnerUserUUID();
      if (uuid != null) {
         EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
         if (player == null) {
            this.setPositionAndUpdate(this.posX, 0.0, this.posZ);
         } else {
            this.saveOwnerData(player);
            if (this.isAnchored()) {
               Vec3d pos = this.getTargetPosition();
               this.setPositionAndUpdate(pos.x, pos.y, pos.z);
            } else {
               this.setPositionAndUpdate(player.posX, player.posY + 0.0, player.posZ);
            }

            Action action = this.getCurrentAction();
            if (action == Action.NULL && player.isSwingInProgress) {
               this.setCurrentAction(Action.ATTACK);
            }

            if (action == Action.ATTACK && !player.isSwingInProgress) {
               this.setCurrentAction(Action.NULL);
            }
         }
      }
   }

   /**
    * Ticks the strip sequence: at tick 65 the outfit index toggles (server),
    * at tick 100 the client ends the strip (camera/third-person reset) and
    * the server returns the girl to {@link Action#NULL}. {@code an} is -1
    * while no strip is running.
    */
   void D_clash581() {
      if (this.an != -1) {
         this.an++;
         if (!this.world.isRemote && this.an == 65) {
            this.setOutfitIndex(this.getOutfitIndex() == 0 ? 1 : 0);
         }

         if (this.an >= 100) {
            if (this.getCurrentAction() == Action.STRIP) {
               if (this.world.isRemote) {
                  this.handleClientOwner();
               } else {
                  this.setCurrentAction(Action.NULL);
               }
            }
         }
      }
   }

   /**
    * CLIENT: forces first-person view, reloads the entity shader and locks
    * movement at the end of the strip animation.
    */
   @SideOnly(Side.CLIENT)
   void handleClientOwner() {
      if (this.hasOwnerUUID()) {
         Minecraft mc = Minecraft.getMinecraft();
         mc.gameSettings.thirdPersonView = 0;
         mc.entityRenderer.loadEntityShader(mc.getRenderViewEntity());
         HandlePlayerMovement.setMovementLock(true);
      }
   }

   public boolean isSceneActive() {
      return this.isAnchored();
   }

   public Vec3d getOwnerAimVector(Vec3d vec, float partialTicks) {
      return vec;
   }

   public boolean canPerformAction(Action action, EntityPlayer player) {
      return false;
   }

   public boolean isPlayerGirl() {
      return true;
   }

   public void onOwnerInteract(EntityPlayer player) {
   }

   /**
    * Guards the action state machine: never allows a transition into
    * {@link Action#NULL} while the girl is anchored (would desync the scene),
    * and arms the strip countdown ({@code an}) when entering
    * {@link Action#STRIP}. CLIENT writes are routed through
    * {@code changeDataParameterFromClient} by the super implementation.
    */
   @Override
   public void setCurrentAction(Action action) {
      if (!this.world.isRemote && action == Action.NULL && this.isAnchored()) {
         System.out.println("prevented a potential animation break");
      } else {
         if (action == Action.STRIP) {
            this.an = this.world.isRemote ? 5 : 0;
         }

         super.setCurrentAction(action);
      }
   }

   /**
    * Mirrors the owner player's equipped armor into the girl's data-manager
    * armor slots (elytra maps to the chest slot), so the transformed model
    * renders the same equipment. SERVER or CLIENT; writes are direct data-manager
    * sets.
    */
   public void syncArmor(EntityPlayer player) {
      this.entityDataManager.set(HELMET_SLOT, ItemStack.EMPTY);
      this.entityDataManager.set(CHEST_SLOT, ItemStack.EMPTY);
      this.entityDataManager.set(LEGS_SLOT, ItemStack.EMPTY);
      this.entityDataManager.set(BOOTS_SLOT, ItemStack.EMPTY);

      for (ItemStack stack : player.getArmorInventoryList()) {
         if (stack.getItem() instanceof ItemElytra) {
            this.entityDataManager.set(CHEST_SLOT, stack);
         } else if (stack.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor)stack.getItem();
            switch (armor.getEquipmentSlot()) {
               case HEAD:
                  this.entityDataManager.set(HELMET_SLOT, stack);
                  break;
               case CHEST:
                  this.entityDataManager.set(CHEST_SLOT, stack);
                  break;
               case LEGS:
                  this.entityDataManager.set(LEGS_SLOT, stack);
                  break;
               case FEET:
                  this.entityDataManager.set(BOOTS_SLOT, stack);
            }
         }
      }
   }

   public UUID getOwnerUserUUID() {
      return ((Optional)this.entityDataManager.get(ai)).isPresent() ? (UUID)((Optional)this.entityDataManager.get(ai)).get() : null;
   }

   @Nullable
   public EntityPlayer getOwnerPlayer() {
      UUID uuid = this.getOwnerUserUUID();
      return uuid == null ? null : this.world.getPlayerEntityByUUID(uuid);
   }

   public void setOwnerId(Optional<UUID> uuidOpt) {
      this.entityDataManager.set(ai, uuidOpt);
   }

   public void onTickClient() {
   }

   public void B_clash233() {
   }

   /**
    * Maintains the static UUID-&gt;girl tables: moves girls with a bound owner
    * from {@link #playerGirlList} into {@link #al}, then prunes dead girls.
    * Called from {@link #updateAITasks()} every tick; tolerates concurrent
    * modification during world teardown.
    */
   public static void rebuildPlayerGirlTableFromWorld() {
      ArrayList toRemove = new ArrayList();

      try {
         for (AbstractPlayerGirlEntity playerGirl : playerGirlList) {
            if (playerGirl.getOwnerUserUUID() != null) {
               al.put(playerGirl.getOwnerUserUUID(), playerGirl);
               toRemove.add(playerGirl);
            }
         }
      } catch (ConcurrentModificationException ex) {
      }

      for (AbstractPlayerGirlEntity playerGirl : (java.util.Collection<AbstractPlayerGirlEntity>) (toRemove) ) {
         playerGirlList.remove(playerGirl);
      }

      rebuildPlayerGirlTableInternal();
   }

   static void rebuildPlayerGirlTableInternal() {
      ArrayList toRemove = new ArrayList();

      for (Entry entry : al.entrySet()) {
         if (((AbstractPlayerGirlEntity)entry.getValue()).isDead) {
            toRemove.add(entry.getKey());
         }
      }

      for (UUID uuid : (java.util.Collection<UUID>) (toRemove) ) {
         al.remove(uuid);
      }
   }

   protected boolean isOwnerUUID(UUID uuid) {
      if (uuid == null) {
         return false;
      }

      AbstractPlayerGirlEntity playerGirl = getPlayerGirlByUUID(uuid);
      return playerGirl != null;
   }

   /**
    * CLIENT-side action dispatch: first asks {@link #handleActionRequest(String)},
    * and if unhandled forwards the action to the server as a
    * {@link SexPromptPacket} carrying the owner UUID and the {@code ab}
    * "first interaction" flag. The flag is consumed (reset to true) on send.
    */
   @Override
   public void doAction(String action, UUID uuid) {
      if (!this.handleActionRequest(action)) {
         if (((Optional)this.entityDataManager.get(ai)).isPresent()) {
            PacketHandler.networkWrapper.sendToServer(new SexPromptPacket(action, uuid, (UUID)((Optional)this.entityDataManager.get(ai)).get(), this.ab));
            this.ab = true;
         }
      }
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      nbt.setString("owner", ((UUID)((Optional)this.entityDataManager.get(ai)).get()).toString());
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      this.entityDataManager.set(ai, Optional.of(UUID.fromString(nbt.getString("owner"))));
      playerGirlList.add(this);
   }

   /**
    * Plays a sound at the girl's position; on the client it is a local world
    * sound ({@link SoundCategory#NEUTRAL}), on the server a broadcast to all
    * players ({@link SoundCategory#PLAYERS}).
    */
   @Override
   public void playSoundAtPosition(SoundEvent sound, float volume, float pitch) {
      Vec3d pos = this.getPositionVec3d();
      if (this.world.isRemote) {
         this.world.playSound(pos.x, pos.y, pos.z, sound, SoundCategory.NEUTRAL, volume, pitch, false);
      } else {
         this.world
            .playSound(null, new BlockPos(pos.x, pos.y, pos.z), sound, SoundCategory.PLAYERS, volume, pitch);
      }
   }

   @Override
   public void playSound(SoundEvent sound) {
      this.playSoundAtPosition(sound, 1.0F, 1.0F);
   }

   public void playRandomSound(SoundEvent[] sounds) {
      this.playSoundAtPosition(sounds[this.getRNG().nextInt(sounds.length)], 1.0F, 1.0F);
   }

   @Override
   public void playSoundAtVolume(SoundEvent sound, float volume) {
      this.playSoundAtPosition(sound, volume, 1.0F);
   }

   @Override
   protected void U() {
   }

}
