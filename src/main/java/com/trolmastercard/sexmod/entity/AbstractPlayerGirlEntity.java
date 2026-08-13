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

   protected AbstractPlayerGirlEntity(World var1) {
      super(var1);
      this.setSize(0.01F, 0.01F);
      playerGirlList.add(this);
   }

   protected AbstractPlayerGirlEntity(World var1, UUID var2) {
      this(var1);
      this.entityDataManager.set(ai, Optional.of(var2));
   }

   @Nullable
   public static AbstractPlayerGirlEntity getPlayerGirlByUUID(UUID var0) {
      return al.get(var0);
   }

   @Nullable
   public static AbstractPlayerGirlEntity g(@Nonnull EntityPlayer var0) {
      return al.get(var0.getPersistentID());
   }

   @Nullable
   public static AbstractPlayerGirlEntity getPlayerGirlByOwner(UUID var0) {
      try {
         for (BaseGirlEntity var2 : getGirlEntityList()) {
            if (!var2.world.isRemote && var2 instanceof AbstractPlayerGirlEntity) {
               AbstractPlayerGirlEntity var3 = (AbstractPlayerGirlEntity)var2;
               if (var0.equals(var3.getOwnerUserUUID())) {
                  return var3;
               }
            }
         }
      } catch (ConcurrentModificationException var4) {
      }

      return null;
   }

   @Override
   public TargetPoint getTargetNetworkPoint() {
      return new TargetPoint(this.dimension, this.posX, this.posY - 0.0, this.posZ, 50.0);
   }

   public void a(int var1, Action var2) {
      PacketHandler.networkWrapper.sendToAllTracking(new ForcePlayerGirlUpdatePacket(this.getOwnerUserUUID(), var1, var2), this.getTargetNetworkPoint());
   }

   public EntityPlayer resolvePlayerEntity(EntityPlayer var1) {
      return var1;
   }

   public boolean isRidingSomething() {
      return true;
   }

   public Vec3d c(Vec3d var1, float var2) {
      return var1;
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

   public boolean handleActionRequest(String var1) {
      return false;
   }

   public boolean A_clash381() {
      return true;
   }

   @Override
   public String getDisplayNameText() {
      if (((Optional)this.entityDataManager.get(ai)).isPresent()) {
         EntityPlayer var1 = this.world.getPlayerEntityByUUID((UUID)((Optional)this.entityDataManager.get(ai)).get());
         if (var1 != null) {
            return var1.getName();
         }
      }

      return "anonymous horny girl";
   }

   public void handleInteraction() {
   }

   public abstract void b(String var1, UUID var2);

   public abstract IVanillaModel getHandModel(int var1);

   public abstract String getHandTexture(int var1);

   public Vec3i getHandColor(int var1) {
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

   @SideOnly(Side.CLIENT)
   public static void rebuildPlayerGirlTable() {
      AbstractPlayerGirlEntity var0 = getPlayerGirlByUUID(Minecraft.getMinecraft().player.getPersistentID());
      if (var0 != null) {
         var0.resetCameraAndPhysics();
      }
   }

   @Override
   public void resetCameraAndPhysics() {
      this.cameraOriginPos = null;
      this.setNoGravity(false);
      if (this.world.isRemote) {
         this.resetLocalPlayerClientState();
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   protected void resetLocalPlayerClientState() {
      if (this.isControlledByLocalPlayer() || this.hasOwnerUUID()) {
         HandlePlayerMovement.setMovementLock(true);
         EntityPlayerSP var1 = Minecraft.getMinecraft().player;
         var1.setInvisible(false);
         var1.setNoGravity(false);
         var1.noClip = false;
         this.entityDataManager.set(IS_ANCHORED, false);
         PacketHandler.networkWrapper.sendToServer(new ResetGirlPacket(this.getGirlId()));
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean hasCustomParts() {
      Minecraft var1 = Minecraft.getMinecraft();
      return !this.hasOwnerUUID() || var1.gameSettings.thirdPersonView != 0;
   }

   protected void handleOwnerUUID(boolean var1) {
      if (ag) {
         if (this.getOwnerUserUUID() != null) {
            EntityPlayer var2 = this.world.getPlayerEntityByUUID(this.getOwnerUserUUID());
            if (var2 != null) {
               var2.capabilities.allowFlying = var1;
               if (!var1) {
                  var2.capabilities.isFlying = false;
               }

               var2.sendPlayerAbilities();
            }
         }
      }
   }

   public static boolean hasPlayerGirlWithUUID(UUID var0) {
      rebuildPlayerGirlTable();

      for (Entry var2 : al.entrySet()) {
         UUID var3 = (UUID)var2.getKey();
         if (var0.equals(var3)) {
            return true;
         }
      }

      return false;
   }

   public static boolean e(EntityPlayer var0) {
      return var0 == null ? false : hasPlayerGirlWithUUID(var0.getPersistentID());
   }

   public AxisAlignedBB getEntityBoundingBox() {
      return super.getEntityBoundingBox().offset(0.0, 0.5, 0.0);
   }

   protected EntityPlayer getNearestPlayer() {
      List var1 = this.world.playerEntities;
      EntityPlayer var2 = null;

      for (EntityPlayer var4 : (java.util.Collection<EntityPlayer>) (var1) ) {
         if (!var4.getPersistentID().equals(((Optional)this.entityDataManager.get(ai)).get())) {
            if (var2 == null) {
               var2 = var4;
            } else {
               double var5 = var2.getDistanceSq(this.getPositionVec3d().x, this.getPositionVec3d().y, this.getPositionVec3d().z);
               double var7 = var4.getDistanceSq(this.getPositionVec3d().x, this.getPositionVec3d().y, this.getPositionVec3d().z);
               if (var7 < var5) {
                  var2 = var4;
               }
            }
         }
      }

      return var2;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean isLocalPlayerNearby() {
      EntityPlayer var1 = this.getNearestPlayer();
      return var1 == null ? false : var1.getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID());
   }

   public Vec3d getPositionVec3d() {
      return new Vec3d(this.posX, this.posY - 0.0, this.posZ);
   }

   protected void teleportPlayerToGirl(UUID var1) {
      EntityPlayerMP var2 = (EntityPlayerMP)this.world.getPlayerEntityByUUID(var1);
      EntityPlayerMP var3 = (EntityPlayerMP)this.world.getPlayerEntityByUUID((UUID)((Optional)this.entityDataManager.get(ai)).get());
      PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), var2);
      PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), var3);
      this.setInteractionPlayerUUID(var1);
      this.rotationYaw = 0.0F;
      this.rotationYawHead = 0.0F;
      var2.rotationYaw = 180.0F;
      var2.rotationYawHead = 180.0F;
      var2.setNoGravity(true);
      var2.noClip = true;
      Vec3d var4 = this.getPositionVector();
      var2.setPositionAndUpdate(var4.x, var4.y, var4.z + 1.0);
      var2.capabilities.isFlying = true;
      var3.capabilities.isFlying = true;
      this.snapPlayerToPosition(var1);
      this.entityDataManager.set(IS_ANCHORED, true);
      this.setTargetPosition(var4);
      this.setYawRotation(0.0F);
   }

   protected void playStepSound(BlockPos var1, Block var2) {
      super.playStepSound(var1, var2);
   }

   public AxisAlignedBB getPlayerCollisionBox(EntityPlayer var1) {
      return var1.getEntityBoundingBox();
   }

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

   void saveOwnerData(EntityPlayer var1) {
      NBTTagCompound var2 = var1.getEntityData();
      String var3 = var2.getString("sexmod:CustomModel" + NpcType.getNpcType(this));
      this.setCustomModelCode(var3);
   }

   @Override
   public void updateAITasks() {
      rebuildPlayerGirlTable();
      this.tickFollowUpTransitions();
      this.updateCustomModelParts();
      UUID var1 = this.getOwnerUserUUID();
      if (var1 != null) {
         EntityPlayer var2 = this.world.getPlayerEntityByUUID(var1);
         if (var2 == null) {
            this.setPositionAndUpdate(this.posX, 0.0, this.posZ);
         } else {
            this.saveOwnerData(var2);
            if (this.isAnchored()) {
               Vec3d var3 = this.getTargetPosition();
               this.setPositionAndUpdate(var3.x, var3.y, var3.z);
            } else {
               this.setPositionAndUpdate(var2.posX, var2.posY + 0.0, var2.posZ);
            }

            Action var4 = this.getCurrentAction();
            if (var4 == Action.NULL && var2.isSwingInProgress) {
               this.setCurrentAction(Action.ATTACK);
            }

            if (var4 == Action.ATTACK && !var2.isSwingInProgress) {
               this.setCurrentAction(Action.NULL);
            }
         }
      }
   }

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

   @SideOnly(Side.CLIENT)
   void handleClientOwner() {
      if (this.hasOwnerUUID()) {
         Minecraft var1 = Minecraft.getMinecraft();
         var1.gameSettings.thirdPersonView = 0;
         var1.entityRenderer.loadEntityShader(var1.getRenderViewEntity());
         HandlePlayerMovement.setMovementLock(true);
      }
   }

   public boolean isAnchored() {
      return this.isAnchored();
   }

   public Vec3d b(Vec3d var1, float var2) {
      return var1;
   }

   public boolean a(Action var1, EntityPlayer var2) {
      return false;
   }

   public boolean isPlayerGirl() {
      return true;
   }

   public void onOwnerInteract(EntityPlayer var1) {
   }

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

   public void f(EntityPlayer var1) {
      this.entityDataManager.set(HELMET_SLOT, ItemStack.EMPTY);
      this.entityDataManager.set(CHEST_SLOT, ItemStack.EMPTY);
      this.entityDataManager.set(LEGS_SLOT, ItemStack.EMPTY);
      this.entityDataManager.set(BOOTS_SLOT, ItemStack.EMPTY);

      for (ItemStack var3 : var1.getArmorInventoryList()) {
         if (var3.getItem() instanceof ItemElytra) {
            this.entityDataManager.set(CHEST_SLOT, var3);
         } else if (var3.getItem() instanceof ItemArmor) {
            ItemArmor var4 = (ItemArmor)var3.getItem();
            switch (var4.getEquipmentSlot()) {
               case HEAD:
                  this.entityDataManager.set(HELMET_SLOT, var3);
                  break;
               case CHEST:
                  this.entityDataManager.set(CHEST_SLOT, var3);
                  break;
               case LEGS:
                  this.entityDataManager.set(LEGS_SLOT, var3);
                  break;
               case FEET:
                  this.entityDataManager.set(BOOTS_SLOT, var3);
            }
         }
      }
   }

   public UUID getOwnerUserUUID() {
      return ((Optional)this.entityDataManager.get(ai)).isPresent() ? (UUID)((Optional)this.entityDataManager.get(ai)).get() : null;
   }

   @Nullable
   public EntityPlayer getOwnerPlayer() {
      UUID var1 = this.getOwnerUserUUID();
      return var1 == null ? null : this.world.getPlayerEntityByUUID(var1);
   }

   public void a(Optional<UUID> var1) {
      this.entityDataManager.set(ai, var1);
   }

   public void onTickClient() {
   }

   public void B_clash233() {
   }

   public static void rebuildPlayerGirlTableFromWorld() {
      ArrayList var0 = new ArrayList();

      try {
         for (AbstractPlayerGirlEntity var2 : playerGirlList) {
            if (var2.getOwnerUserUUID() != null) {
               al.put(var2.getOwnerUserUUID(), var2);
               var0.add(var2);
            }
         }
      } catch (ConcurrentModificationException var3) {
      }

      for (AbstractPlayerGirlEntity var5 : (java.util.Collection<AbstractPlayerGirlEntity>) (var0) ) {
         playerGirlList.remove(var5);
      }

      rebuildPlayerGirlTableInternal();
   }

   static void rebuildPlayerGirlTableInternal() {
      ArrayList var0 = new ArrayList();

      for (Entry var2 : al.entrySet()) {
         if (((AbstractPlayerGirlEntity)var2.getValue()).isDead) {
            var0.add(var2.getKey());
         }
      }

      for (UUID var4 : (java.util.Collection<UUID>) (var0) ) {
         al.remove(var4);
      }
   }

   protected boolean isOwnerUUID(UUID var1) {
      if (var1 == null) {
         return false;
      }

      AbstractPlayerGirlEntity var2 = getPlayerGirlByUUID(var1);
      return var2 != null;
   }

   @Override
   public void doAction(String var1, UUID var2) {
      if (!this.handleActionRequest(var1)) {
         if (((Optional)this.entityDataManager.get(ai)).isPresent()) {
            PacketHandler.networkWrapper.sendToServer(new SexPromptPacket(var1, var2, (UUID)((Optional)this.entityDataManager.get(ai)).get(), this.ab));
            this.ab = true;
         }
      }
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setString("owner", ((UUID)((Optional)this.entityDataManager.get(ai)).get()).toString());
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.entityDataManager.set(ai, Optional.of(UUID.fromString(var1.getString("owner"))));
      playerGirlList.add(this);
   }

   @Override
   public void playSoundAtPosition(SoundEvent var1, float var2, float var3) {
      Vec3d var4 = this.getPositionVec3d();
      if (this.world.isRemote) {
         this.world.playSound(var4.x, var4.y, var4.z, var1, SoundCategory.NEUTRAL, var2, var3, false);
      } else {
         this.world
            .playSound(null, new BlockPos(var4.x, var4.y, var4.z), var1, SoundCategory.PLAYERS, var2, var3);
      }
   }

   @Override
   public void playSound(SoundEvent var1) {
      this.playSoundAtPosition(var1, 1.0F, 1.0F);
   }

   public void playRandomSound(SoundEvent[] var1) {
      this.playSoundAtPosition(var1[this.getRNG().nextInt(var1.length)], 1.0F, 1.0F);
   }

   @Override
   public void playSoundAtVolume(SoundEvent var1, float var2) {
      this.playSoundAtPosition(var1, var2, 1.0F);
   }

   @Override
   protected void U() {
   }

}
