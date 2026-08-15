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

   public GoblinPlayerEntity(World var1) {
      super(var1);
   }

   public GoblinPlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public float getScaleFactor() {
      return 0.9F;
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
      String[] var2 = getModelCodeParts(this);
      return var2.length < 8 ? super.getHandColor(var1) : SkinColor.values()[Integer.parseInt(var2[7])].getColor();
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      EyeColor var1 = EyeColor.values()[this.getRNG().nextInt(EyeColor.values().length)];
      this.entityDataManager.register(au, new BlockPos(var1.getColor()));
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
   public void handleOwnerCommand(String var1, UUID var2) {
      if ("anal".equals(var1)) {
         this.teleportPlayerToGirl(var2);
         this.setCurrentAction(Action.NELSON_INTRO);
         this.sendActionPacket(this.getOutfitIndex(), Action.NELSON_INTRO);
         this.setOutfitIndex(0);
      }

      if ("paizuri".equals(var1)) {
         this.teleportPlayerToGirl(var2);
         this.setCurrentAction(Action.PAIZURI_START);
         this.sendActionPacket(this.getOutfitIndex(), Action.PAIZURI_START);
         this.setOutfitIndex(0);
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(this, var1, new String[]{"anal", "paizuri"}, null, false));
      return true;
   }

   @Override
   public EntityPlayer resolvePlayerEntity(EntityPlayer var1) {
      UUID var2 = this.getOwnerUUID();
      if (var2 == null) {
         return var1;
      }

      EntityPlayer var3 = this.world.getPlayerEntityByUUID(var2);
      return var3 == null ? var1 : var3;
   }

   @Override
   public boolean shouldRenderModel() {
      return this.getOwnerUUID() == null || !Minecraft.getMinecraft().player.getPersistentID().equals(this.getOwnerUserUUID());
   }

   @Override
   public boolean isRidingSomething() {
      UUID var1 = this.getOwnerUUID();
      return var1 == null;
   }

   @Override
   public Vec3d getOwnerLookVector(Vec3d var1, float var2) {
      UUID var3 = this.getOwnerUUID();
      if (var3 == null) {
         return var1;
      }

      EntityPlayer var4 = this.world.getPlayerEntityByUUID(var3);
      if (var4 == null) {
         return var1;
      }

      Vec3d var5 = var4.getPositionVector();
      Vec3d var6 = new Vec3d(var4.lastTickPosX, var4.lastTickPosY, var4.lastTickPosZ);
      return RotationHelper.lerpVec3dDouble(var6, var5, var2);
   }

   /**
    * SERVER: the pickup trigger (sneak+click by a non-transformed player on
    * the carried player's entity) — binds the carrier and starts
    * {@link Action#PICK_UP} with the 45-tick hold countdown, locking the
    * carrying player's physics.
    */
   void handlePlayerThrow(EntityPlayer var1) {
      if (this.getCurrentAction() == Action.NULL) {
         if (this.getOwnerUUID() == null) {
            if (GoblinEntity.hasGoblinWithUUID(var1.getPersistentID())) {
               var1.sendStatusMessage(new TextComponentString("you are already carrying a Goblin"), true);
            } else {
               this.setOwnerUUID(var1.getPersistentID());
               this.setCurrentAction(Action.PICK_UP);
               this.setHeldPlayerDistance(45);
               EntityPlayer var2 = this.getOwnerPlayer();
               if (var2 != null) {
                  var2.setNoGravity(true);
                  var2.noClip = true;
                  if (!this.world.isRemote) {
                     PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var2);
                  }
               }
            }
         }
      }
   }

   @Override
   protected String buildModelCodeDNA(StringBuilder var1) {
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 3);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 7);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 7);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 5);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, HairColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, SkinColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, EyeColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 0);
      return var1.toString();
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
   public boolean canPerformAction(Action var1, EntityPlayer var2) {
      UUID var3 = this.getOwnerUUID();
      if (var3 == null) {
         return false;
      }

      EntityPlayer var4 = this.world.getPlayerEntityByUUID(var3);
      if (var4 == null) {
         return false;
      }

      float var5 = var2.rotationYaw;
      float var6 = var1 == Action.PICK_UP ? 180.0F : 0.0F;
      float var7 = var4.rotationYaw - 90.0F + var6;
      float var8 = var4.rotationYaw + 90.0F + var6;
      if (var5 < var7) {
         var2.rotationYaw = var7;
      }

      if (var5 > var8) {
         var2.rotationYaw = var8;
      }

      float var9 = var2.rotationPitch;
      float var10 = var1 == Action.PICK_UP ? 0.0F : 37.5F;
      if (var9 > var10) {
         var2.rotationPitch = var10;
      }

      return true;
   }

   @Override
   public Vec3d getOwnerAimVector(Vec3d var1, float var2) {
      UUID var3 = this.getOwnerUUID();
      if (var3 == null) {
         return var1;
      }

      EntityPlayer var4 = this.world.getPlayerEntityByUUID(var3);
      if (var4 == null) {
         return var1;
      }

      float var5 = RotationHelper.lerp(var4.prevRenderYawOffset, var4.renderYawOffset, var2);
      Vec3d var6 = var1;
      float var7 = 135.0F;
      Action var8 = this.getCurrentAction();
      if (var8 == Action.PICK_UP) {
         var6 = new Vec3d(var1.x, var1.y, -var1.z);
         var7 = 175.0F;
      } else if (var8 != Action.START_THROWING) {
         var6 = var6.subtract(0.0, 2.0, 0.0);
      }

      return VectorMath.rotateByYaw(var6, var5 + var7);
   }

   @SideOnly(Side.CLIENT)
   void handleOwnerThrow() {
      EntityPlayer var1 = this.getOwnerPlayer();
      if (var1 != null) {
         if (this.getCurrentAction() == Action.START_THROWING) {
            var1.isDead = false;
            if (!this.world.loadedEntityList.contains(var1)) {
               this.world.spawnEntity(var1);
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
         Action var1 = this.getCurrentAction();
         this.handleLocalAction(var1);
         this.handleNelsonAction(var1);
         this.aw = var1;
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
      Action var1 = this.getCurrentAction();
      if (var1 != Action.THROWN) {
         if (var1 != Action.START_THROWING || this.getThrowProgress() <= 15) {
            UUID var2 = this.getOwnerUUID();
            if (var2 != null) {
               EntityPlayer var3 = this.world.getPlayerEntityByUUID(var2);
               if (var3 != null) {
                  EntityPlayer var4 = this.getOwnerPlayer();
                  if (var4 != null) {
                     var4.noClip = true;
                     var4.setNoGravity(true);
                     var4.setPosition(var3.posX, var3.posY + 2.0, var3.posZ);
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
      GoblinPlayerEntity var1 = this;
      int var2 = var1.getThrowProgress();
      if (var2 != -1) {
         var1.setThrowProgress(++var2);
         EntityPlayer var3 = this.getOwnerPlayer();
         if (var3 != null) {
            if (var2 == 15) {
               GoblinEntity.getGoblinThrowPos(this);
               float var5 = GoblinEntity.getGoblinThrowHeight(this);
               float var6 = GoblinEntity.getGoblinThrowDistance(this);
               if (this.world.isRemote && this.hasOwnerUUID()) {
                  HandlePlayerMovement.setMovementLock(true);
               }

               Vec3d var7 = GoblinEntity.rotateVectorPitchYaw(new Vec3d(0.0, 0.0, 1.5), var5, var6);
               var3.motionX = var7.x;
               var3.motionY = var7.y;
               var3.motionZ = var7.z;
               if (!this.world.isRemote) {
                  this.setYawRotation(var6);
               }
            }

            var3.noClip = false;
            var3.setNoGravity(false);
            if (var2 == 39) {
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
         EntityPlayer var1 = this.getOwnerPlayer();
         if (var1 != null) {
            if (var1.onGround) {
               int var2 = this.getThrowTickCount() + 1;
               this.setThrowTickCount(var2);
               if (var2 >= 30) {
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
      String var1 = (String)this.entityDataManager.get(ax);
      if ("".equals(var1)) {
         return null;
      }

      try {
         return UUID.fromString((String)this.entityDataManager.get(ax));
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   @Override
   public void setOwnerUUID(UUID var1) {
      if (var1 == null) {
         this.entityDataManager.set(ax, "");
      } else {
         this.entityDataManager.set(ax, var1.toString());
      }
   }

   public EntityPlayer getOwnerPlayer() {
      UUID var1 = this.getOwnerUUID();
      return var1 == null ? null : this.world.getPlayerEntityByUUID(var1);
   }

   @Override
   public void setThrowProgress(int var1) {
      this.az = var1;
   }

   @Override
   public int getThrowProgress() {
      return this.az;
   }

   @Override
   public void setThrowTickCount(int var1) {
      this.aG = var1;
   }

   @Override
   public int getThrowTickCount() {
      return this.aG;
   }

   @Override
   public void setPreviousAction(Action var1) {
      this.aw = var1;
   }

   @Override
   public Action getPreviousAction() {
      return this.aw;
   }

   @Override
   public void setHeldPlayerDistance(int var1) {
      this.aE = var1;
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
         EntityPlayer var1 = this.getOwnerPlayer();
         if (var1 != null) {
            PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)var1);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void handleNelsonAction(Action var1) {
      if (var1 == Action.NELSON_FAST && this.aw != Action.NELSON_FAST) {
         this.aF = false;
      }
   }

   @SideOnly(Side.CLIENT)
   void handleLocalAction(Action var1) {
      Minecraft var2 = Minecraft.getMinecraft();
      if (var2.player.getPersistentID().equals(this.getInteractionPlayerUUID())) {
         if (var2.gameSettings.thirdPersonView == 0) {
            switch (var1) {
               case NELSON_CUM:
               case NELSON_FAST:
               case NELSON_INTRO:
               case NELSON_SLOW:
                  var2.gameSettings.thirdPersonView = 2;
            }
         }
      }
   }

   @Override
   public void setCustomPartList(List<Integer> var1) {
      StringBuilder var2 = new StringBuilder();

      for (int var4 : var1) {
         AbstractNpcOnlyEntity.appendPaddedNumber(var2, var4);
      }

      AbstractNpcOnlyEntity.appendPaddedNumber(var2, 1);
      this.entityDataManager.set(at, var2.toString());
   }

   @Nullable
   @Override
   protected Action getNextAction(Action var1) {
      switch (var1) {
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
      Action var2 = this.getCurrentAction();
      if (var2 != Action.PAIZURI_CUM || action != Action.PAIZURI_SLOW && action != Action.PAIZURI_FAST) {
         if (var2 != Action.NELSON_CUM || action != Action.NELSON_SLOW && action != Action.NELSON_FAST) {
            if (var2 != Action.BREEDING_CUM_0 || action != Action.BREEDING_SLOW_0 && action != Action.BREEDING_FAST_0) {
               if (action == Action.PAIZURI_START && !this.world.isRemote) {
                  this.handlePlayerInteract();
               }

               if (action == Action.NELSON_INTRO && !this.world.isRemote) {
                  this.handlePlayerLook();
               }

               if (action == Action.NELSON_CUM) {
                  this.entityDataManager.set(aA, true);
               }

               if (var2 == Action.NELSON_CUM && action != Action.NELSON_CUM) {
                  this.entityDataManager.set(aA, false);
               }

               super.setCurrentAction(action);
            }
         }
      }
   }

   void handlePlayerLook() {
      EntityPlayer var1 = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      if (var1 != null) {
         this.setYawRotation(var1.rotationYaw);
         this.noClip = true;
         this.setNoGravity(true);
         var1.setNoGravity(true);
         var1.noClip = true;
         var1.setPositionAndUpdate(var1.posX, var1.posY, var1.posZ - 1.0);
      }
   }

   void handlePlayerInteract() {
      EntityPlayer var1 = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      if (var1 != null) {
         this.setYawRotation(var1.rotationYaw + 180.0F);
         this.noClip = true;
         this.setNoGravity(true);
         var1.setNoGravity(true);
         var1.noClip = true;
         var1.setPositionAndUpdate(var1.posX, var1.posY - 0.5, var1.posZ - 0.6F);
         var1.rotationPitch = 70.0F;
         var1.prevRotationPitch = 70.0F;
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
   public void onOwnerInteract(EntityPlayer var1) {
      if (var1.getPersistentID().equals(this.getOwnerUUID())) {
         ResetGirlPacket.Handler.resetGirl(this);
         this.setAnchored(false);
         this.setCurrentAction(Action.NULL);
         this.setOwnerUUID(null);
      }
   }

   @Override
   protected Action getCumAction(Action var1) {
      switch (var1) {
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
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      if (this.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.goblin.blink", true, var1);
            } else {
               this.createAnimation("animation.goblin.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.goblin.null", true, var1);
            } else if (this.ak) {
               this.createAnimation("animation.goblin.sit", true, var1);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.aC = !this.aC;
               }

               if (!this.af) {
                  this.createAnimation("animation.goblin.fly" + (this.aC ? "2" : ""), true, var1);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.movementController.setAnimationSpeed(1.2F);
                     this.createAnimation("animation.goblin.running", true, var1);
                  } else if (this.ao.y >= -0.1F) {
                     this.movementController.setAnimationSpeed(2.0);
                     this.createAnimation("animation.goblin.walk", true, var1);
                  } else {
                     this.movementController.setAnimationSpeed(1.5);
                     this.createAnimation("animation.goblin.backwards_walk", true, var1);
                  }
               } else {
                  this.createAnimation("animation.goblin.idle", true, var1);
               }
            }
            break;
         case "action":
            Minecraft var4 = Minecraft.getMinecraft();
            String var5 = var4.player.getPersistentID().equals(this.getOwnerUUID()) && var4.gameSettings.thirdPersonView == 0 ? "1" : "3";
            switch (this.getCurrentAction()) {
               case NELSON_CUM:
                  this.createAnimation("animation.goblin.nelson_cum", true, var1);
                  break;
               case NELSON_FAST:
                  this.createAnimation("animation.goblin.nelson_fast" + (this.aF ? "c" : "s"), true, var1);
                  break;
               case NELSON_INTRO:
                  this.createAnimation("animation.goblin.nelson_intro", true, var1);
                  break;
               case NELSON_SLOW:
                  this.createAnimation("animation.goblin.nelson_slow" + (this.ay ? "" : "2"), true, var1);
                  break;
               case PAIZURI_IDLE:
                  this.createAnimation("animation.goblin.paizuri_idle", true, var1);
                  break;
               case PAIZURI_SLOW:
                  this.createAnimation("animation.goblin.paizuri_slow" + this.aD, true, var1);
                  break;
               case BREEDING_SLOW_0:
                  this.createAnimation("animation.goblin.breeding_slow_1" + (this.aB ? "l" : "r"), true, var1);
                  break;
               case BREEDING_SLOW_2:
                  this.createAnimation("animation.goblin.breeding_slow_3", true, var1);
                  break;
               case PAIZURI_FAST:
                  this.createAnimation("animation.goblin.paizuri_fast", true, var1);
                  break;
               case PAIZURI_FAST_CONTINUES:
                  this.createAnimation("animation.goblin.paizuri_fast_countinues", true, var1);
                  break;
               case BREEDING_1:
                  this.createAnimation("animation.goblin.breeding_2", true, var1);
                  break;
               case BREEDING_FAST_2:
                  this.createAnimation("animation.goblin.breeding_fast_3", true, var1);
                  break;
               case SHOULDER_IDLE:
                  this.createAnimation("animation.goblin.shoulder_idle", true, var1);
                  break;
               case PICK_UP:
                  this.createAnimation(String.format("animation.goblin.pick_up_%sperson", var5), true, var1);
                  break;
               case START_THROWING:
                  this.createAnimation(String.format("animation.goblin.throw_%sperson", var5), true, var1);
                  break;
               case THROWN:
                  this.createAnimation("animation.goblin.thrown", true, var1);
                  break;
               case NULL:
                  this.createAnimation("animation.goblin.null", true, var1);
                  break;
               case STAND_UP:
                  this.createAnimation("animation.goblin.stand_up", false, var1);
                  break;
               case STRIP:
                  this.createAnimation("animation.goblin.strip", false, var1);
                  break;
               case ATTACK:
                  this.createAnimation("animation.goblin.attack" + this.nextAttack, false, var1);
                  break;
               case BOW:
                  this.createAnimation("animation.goblin.bowcharge", false, var1);
                  break;
               case SIT:
                  this.createAnimation("animation.goblin.sit", true, var1);
                  break;
               case BREEDING_INTRO_0:
                  this.createAnimation("animation.goblin.breeding_intro_1", true, var1);
                  break;
               case BREEDING_INTRO_1:
                  this.createAnimation("animation.goblin.breeding_intro_2", true, var1);
                  break;
               case BREEDING_INTRO_2:
                  this.createAnimation("animation.goblin.breeding_intro_3", true, var1);
                  break;
               case BREEDING_FAST_0:
                  this.createAnimation("animation.goblin.breeding_fast_1" + (this.aH ? "c" : "s"), true, var1);
                  break;
               case BREEDING_CUM_0:
                  this.createAnimation("animation.goblin.breeding_cum_1", true, var1);
                  break;
               case BREEDING_CUM_1:
                  this.createAnimation("animation.goblin.breeding_cum_2", true, var1);
                  break;
               case BREEDING_CUM_2:
                  this.createAnimation("animation.goblin.breeding_cum_3", true, var1);
                  break;
               case PAIZURI_START:
                  this.createAnimation("animation.goblin.paizuri_start", true, var1);
                  break;
               case PAIZURI_CUM:
                  this.createAnimation("animation.goblin.paizuri_cum", true, var1);
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
                  EntityPlayerSP var6 = Minecraft.getMinecraft().player;
                  openInventoryGui(var6, this, new String[]{"use her", "take ur stuff back"}, null, false);
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
                  EntityPlayerSP var4 = Minecraft.getMinecraft().player;
                  var4.rotationPitch = 70.0F;
                  var4.prevRotationPitch = 70.0F;
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
                  Minecraft var9 = Minecraft.getMinecraft();
                  var9.player.rotationYaw = this.getYawRotation() + 170.0F;
                  var9.player.rotationPitch = -20.0F;
                  var9.player.rotationYawHead = var9.player.rotationYaw;
                  var9.gameSettings.thirdPersonView = 2;
               }
               break;
            case "breedingHmm":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft var8 = Minecraft.getMinecraft();
                  var8.player.rotationYaw = this.getYawRotation() + 180.0F;
                  var8.player.rotationPitch = -15.0F;
                  var8.player.rotationYawHead = var8.player.rotationYaw;
                  var8.gameSettings.thirdPersonView = 0;
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
                  Minecraft var7 = Minecraft.getMinecraft();
                  var7.gameSettings.thirdPersonView = 2;
                  var7.player.rotationYaw = this.getYawRotation() - 120.0F;
                  var7.player.rotationPitch = -30.0F;
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
                  Minecraft var5 = Minecraft.getMinecraft();
                  var5.gameSettings.thirdPersonView = 0;
                  var5.player.rotationYaw = this.getYawRotation() + 180.0F;
                  var5.player.rotationPitch = -15.0F;
                  var5.player.rotationYawHead = var5.player.rotationYaw;
                  var5.gameSettings.thirdPersonView = 0;
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
      this.actionController.registerSoundListener(var2);
      this.movementController.transitionLengthTicks = 2.0;
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
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
      public void onRenderHand(RenderHandEvent var1) {
         AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(Minecraft.getMinecraft().player);
         if (var2 != null) {
            if (var2 instanceof IGoblin) {
               if (((IGoblin)var2).getOwnerUUID() != null) {
                  var1.setCanceled(true);
               }
            }
         }
      }

      @SubscribeEvent
      public void onPlayerTick(PlayerTickEvent var1) {
         EntityPlayer var2 = var1.player;
         if (var2 != null) {
            this.handlePlayerOwner(var2);
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onRenderTickSync(RenderTickEvent var1) {
         if (var1.phase != Phase.END) {
            EntityPlayerSP var2 = Minecraft.getMinecraft().player;
            if (var2 != null) {
               this.handlePlayerOwner(var2);
            }
         }
      }

      void handlePlayerOwner(EntityPlayer var1) {
         AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1);
         if (var2 instanceof GoblinPlayerEntity) {
            Action var3 = var2.getCurrentAction();
            if (var3 != Action.THROWN) {
               if (var3 != Action.START_THROWING || ((IGoblin)var2).getThrowProgress() <= 15) {
                  UUID var4 = ((GoblinPlayerEntity)var2).getOwnerUUID();
                  if (var4 != null) {
                     EntityPlayer var5 = var1.world.getPlayerEntityByUUID(var4);
                     if (var5 != null) {
                        var1.noClip = true;
                        var1.setNoGravity(true);
                        var2.noClip = true;
                        var2.setNoGravity(true);
                        var1.setPosition(var5.posX, var5.posY + 2.0, var5.posZ);
                        var1.lastTickPosX = var5.lastTickPosX;
                        var1.lastTickPosY = var5.lastTickPosY + 2.0;
                        var1.lastTickPosZ = var5.lastTickPosZ;
                     }
                  }
               }
            }
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onRenderWorldLast(RenderWorldLastEvent var1) {
         Minecraft var2 = Minecraft.getMinecraft();
         RenderManager var3 = var2.getRenderManager();
         EntityPlayerSP var4 = var2.player;
         if (var2.player != null) {
            Vec3d var5 = var4.getPositionVector();

            for (EntityPlayer var7 : this.playersToRender) {
               Vec3d var8 = var7.getPositionVector();
               Vec3d var9 = var8.subtract(var5);
               var3.renderEntity(var7, var9.x, var9.y, var9.z, 69.0F, var1.getPartialTicks(), true);
            }

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onRenderTick(RenderTickEvent var1) {
         if (var1.phase == Phase.START) {
            this.clearFakePlayers();
         } else {
            this.killFakePlayers();
         }
      }

      @SideOnly(Side.CLIENT)
      void killFakePlayers() {
         for (EntityPlayer var2 : this.playersToRender) {
            var2.isDead = true;
         }
      }

      @SideOnly(Side.CLIENT)
      void clearFakePlayers() {
         this.playersToRender.clear();
         Minecraft var1 = Minecraft.getMinecraft();
         EntityPlayerSP var2 = var1.player;
         if (var1.world != null) {
            for (EntityPlayer var4 : var1.world.playerEntities) {
               if (var4 != var2) {
                  AbstractPlayerGirlEntity var5 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var4);
                  if (var5 instanceof GoblinPlayerEntity) {
                     GoblinPlayerEntity var6 = (GoblinPlayerEntity)var5;
                     if (var6.getOwnerUUID() != null) {
                        Action var7 = var6.getCurrentAction();
                        if (var7 == Action.THROWN || var7 == Action.START_THROWING) {
                           return;
                        }

                        this.playersToRender.add(var4);
                        var4.isDead = false;
                     }
                  }
               }
            }
         }
      }

      @SubscribeEvent
      public void onEntityInteract(EntityInteract var1) {
         EntityPlayer var2 = var1.getEntityPlayer();
         if (var2.isSneaking()) {
            if (var1.getTarget() instanceof EntityPlayer) {
               AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1.getTarget().getPersistentID());
               if (var3 instanceof GoblinPlayerEntity) {
                  AbstractPlayerGirlEntity var4 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var2.getPersistentID());
                  if (var4 == null) {
                     ((GoblinPlayerEntity)var3).handlePlayerThrow(var1.getEntityPlayer());
                  }
               }
            }
         }
      }

   }
}
