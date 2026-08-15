package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.api.SkinColor;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.GalathScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.entity.ai.DoorInteractAiGoal;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.WorldUtils;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.Point2D;
import com.trolmastercard.sexmod.util.EyeColor;
import com.trolmastercard.sexmod.util.HairColor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector2f;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/**
 * <b>Role.</b> The Goblin NPC (implements {@link IGoblin}) — the tamed
 * shoulder-rider/throwable goblin and the queen system. A goblin can be picked
 * up (PICK_UP -&gt; SHOULDER_IDLE on the owner's shoulder), thrown
 * (START_THROWING -&gt; THROWN -&gt; STAND_UP), or be a queen (SIT on a throne,
 * guards, breeding scenes, stolen gold). Scenes: paizuri, nelson, breeding.
 * <p>
 * <b>State.</b> Own data keys (do not reorder): {@code OWNER_UUID} (122) =
 * carrying player, {@code aK} (123) = queen goblin's girl id, {@code a0}
 * (124) = stolen item stack, {@code aC} (125) = tamed flag, {@code aV} (126) =
 * queen pregnant flag. Appearance comes from the {@link AbstractNpcOnlyEntity}
 * DNA (119/120/121) with the trailing segment (index 9) holding the model
 * part index. {@code aX} = is queen, {@code al}/{@code ac} = throne
 * pos/rotation, {@code ab} = guard list, {@code aR} = throw progress (-1
 * idle), {@code aQ} = held-player countdown (45).
 * <p>
 * <b>Flow.</b> Pickup: {@link #processInteract(EntityPlayer, EnumHand)} sets
 * the owner + PICK_UP; {@link #handlePickUpState(BaseGirlEntity)} glues her to
 * the owner and, after 45 ticks, reaches SHOULDER_IDLE. Throw:
 * {@link #updateThrowProgress()} launches her at tick 15 (velocity from the
 * owner's aim via {@link #getGoblinThrowPos(BaseGirlEntity)} +
 * {@link #getGoblinThrowHeight(BaseGirlEntity)}) and THROWN at tick 39.
 * Queen: {@link #B_clash269()} sends a RUN goblin to steal gold from a nearby
 * player, {@link #E_clash260()} starts the breeding intro when a player stands
 * in the throne zone, {@link #handleJumpThrow()} throws the guards onto the
 * player and {@link #handleThrowCooldown()} releases two AWAIT_PICK_UP guards
 * after breeding.
 * <p>
 * <b>Pitfalls.</b> {@link #setCurrentAction(Action)} guards the cum loops,
 * fires {@link #handlePlayerInteract()}/{@link #handlePlayerLook()}/
 * {@link #L_clash281()} on the server for PAIZURI_START/NELSON_INTRO/
 * START_THROWING, marks {@code aV} on BREEDING_CUM_0/NELSON_CUM and calls
 * {@link #D_clash278()} (reset + despawn) when PAIZURI_CUM leaves for NULL.
 * {@code getModelPartIndex()} reads DNA index 7. Dead goblins drop their
 * stolen item on the server.
 */
public class GoblinEntity extends AbstractNpcOnlyEntity implements IGoblin {
   public static final SkinColor ax = SkinColor.DARK_GREEN;
   public static final Vec3i ah = new Vec3i(11, 6, 11);
   public static final Vec3d aB = new Vec3d(5.0, 1.0, 9.0);
   public static final Vec3d af = new Vec3d(3.0, -1.0, 6.0);
   public static final Vec3d ao = new Vec3d(1.0, 1.0, 5.0);
   public static final Vec3d au = new Vec3d(-6.0, -1.0, 3.0);
   public static final Vec3d aM = new Vec3d(5.0, 1.0, 1.0);
   public static final Vec3d THROW_OFFSET_W = new Vec3d(-3.0, -1.0, -6.0);
   public static final Vec3d THROW_OFFSET_U = new Vec3d(9.0, 1.0, 5.0);
   public static final Vec3d as = new Vec3d(0.0, -1.0, -4.0);
   public static final Vec3d aT = new Vec3d(1.0, -1.0, -3.0);
   public static final Vec3d ap = new Vec3d(-1.0, -1.0, -3.0);
   public static final Vec3d at = new Vec3d(6.0, -1.0, -3.0);
   public static final int aj = 39;
   public static final int ae = 15;
   public static final int aE = 8400;
   static final int aH = 45;
   static final int ad = 32000;
   static final int aw = 26;
   static final int THROW_TICKS_205 = 205;
   static final int aL = 100;
   static final int aA = 1200;
   static final int ak = 30;
   static final int aW = 37;
   static final float aU = 2.0F;
   static final int aI = 5;
   static final int THROW_TICKS_100 = 100;
   static final int aq = 20;
   static final float aG = 0.825F;
   static final Vector2f aS = new Vector2f(0.5F, 0.99F);
   static final HashSet<Item> ag = new HashSet<>(
      Arrays.asList(
         Items.GOLDEN_HOE,
         Items.GOLDEN_HORSE_ARMOR,
         Items.GOLD_INGOT,
         Items.GOLDEN_APPLE,
         Items.GOLDEN_AXE,
         Items.GOLDEN_SHOVEL,
         Items.GOLDEN_PICKAXE,
         Items.GOLDEN_SWORD,
         Items.GOLDEN_CARROT,
         Items.GOLDEN_HELMET,
         Items.GOLDEN_BOOTS,
         Items.GOLDEN_CHESTPLATE,
         Items.GOLDEN_LEGGINGS,
         Items.GOLD_INGOT,
         Items.GOLD_NUGGET,
         Item.getItemFromBlock(Blocks.GOLD_BLOCK),
         Item.getItemFromBlock(Blocks.GOLD_ORE)
      )
   );
   public static final DataParameter<String> OWNER_UUID = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(122);
   public static final DataParameter<String> aK = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(123);
   public static final DataParameter<ItemStack> a0 = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.ITEM_STACK)
      .getSerializer()
      .createKey(124);
   public static final DataParameter<Boolean> aC = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(125);
   public static final DataParameter<Boolean> aV = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(126);
   public boolean aX = false;
   public float ac = 0.0F;
   public long av = -1L;
   public Vec3d al = Vec3d.ZERO;
   List<UUID> guardUUIDs = new ArrayList<>();
   int aO = 31520;
   int aQ = -1;
   public int aR = -1;
   boolean aZ = false;
   BlockPos guardPost = null;
   int guardPostTicks = 0;
   int aa = 0;
   int aJ = 0;
   int an = -1;
   int am = 0;
   long ai = 0L;
   List<GoblinEntity> ab = new ArrayList<>();
   int aY = -1;
   int az = -1;
   Action aN = null;
   public float ar = 1.0F;
   int throwCooldown = -1;
   boolean aD = true;
   boolean aF = true;
   boolean nelsonAltPose = false;
   String aP = "";
   boolean ay = false;

   public GoblinEntity(World world) {
      super(world);
      this.setSize(aS.x, aS.y);
   }

   public GoblinEntity(World world, @Nonnull String girlId, int modelPartIndex) {
      this(world);
      this.entityDataManager.set(aK, girlId);
      this.entityDataManager.set(APPEARANCE_DNA, this.buildModelCodeDNA(new StringBuilder(), modelPartIndex));
   }

   public GoblinEntity(World world, boolean isThrown, float yaw, Vec3d pos) {
      this(world);
      if (isThrown) {
         this.entityDataManager.set(APPEARANCE_DNA, this.buildModelCodeDNA(new StringBuilder()));
         this.ac = yaw;
         this.al = pos;
         this.aX = true;
         this.setTargetPosition(pos);
         this.setYawRotation(yaw);
         this.setCurrentAction(Action.SIT);
         this.setAnchored(true);
         this.setPosition(pos.x, pos.y, pos.z);
      }
   }

   @Override
   public void reinitTasks() {
      super.reinitTasks();
      this.setOwnerUUID(null);
      this.noClip = false;
      this.setNoGravity(false);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      EyeColor color = EyeColor.values()[this.getRNG().nextInt(EyeColor.values().length)];
      this.entityDataManager.register(ACTION_TARGET_POS, new BlockPos(color.getColor()));
      this.entityDataManager.register(CURRENT_ACTION, ax.name());
      this.entityDataManager.register(OWNER_UUID, "");
      this.entityDataManager.register(aK, "");
      this.entityDataManager.register(a0, ItemStack.EMPTY);
      this.entityDataManager.register(aC, false);
      this.entityDataManager.register(aV, false);
   }

   @Override
   protected void clearBoneColors() {
      GoblinRenderer.clearBoneColors();
   }

   public void setDead() {
      super.setDead();
      this.setOwnerUUID(null);
      if (!this.world.isRemote) {
         ItemStack heldItem = (ItemStack)this.entityDataManager.get(a0);
         if (heldItem != ItemStack.EMPTY) {
            EntityItem item = new EntityItem(this.world, this.posX, this.posY, this.posZ, heldItem);
            this.world.spawnEntity(item);
         }
      }
   }

   /**
    * CLIENT: action dispatch from the goblin's GUI — {@code take ur stuff
    * back} starts the throw, {@code use her} binds the acting player as the
    * throw/pickup target.
    */
   @Override
   public void doAction(String action, UUID uuid) {
      if ("take ur stuff back".equals(action)) {
         this.setCurrentAction(Action.START_THROWING);
      }

      if ("use her".equals(action)) {
         this.setThrowTarget(uuid);
      }
   }

   /**
    * CLIENT: arms the "use her" timer ({@code aY} = 0) — after 15 ticks the
    * paizuri scene starts (see {@link #handleHoldTick()}).
    */
   public void setThrowTarget(UUID uuid) {
      this.aY = 0;
      BeeScreen.enableInteraction();
      HandlePlayerMovement.setMovementLock(false);
      this.setInteractionPlayerUUID(uuid);
   }

   /**
    * CLIENT: arms the nelson "pickup" timer ({@code az} = 0) — after 15 ticks
    * the nelson scene starts (see {@link #A_clash277()}).
    */
   public void setPickupTarget(UUID uuid) {
      this.az = 0;
      BeeScreen.enableInteraction();
      HandlePlayerMovement.setMovementLock(false);
      this.setInteractionPlayerUUID(uuid);
   }

   @Override
   public String getDisplayNameText() {
      return "Goblin";
   }

   public float getEyeHeight() {
      return 0.75F;
   }

   @Override
   public float getScaleFactor() {
      return 0.1F;
   }

   @Override
   public void setOwnerUUID(UUID uuid) {
      if (uuid == null) {
         this.entityDataManager.set(OWNER_UUID, "");
      } else {
         this.entityDataManager.set(OWNER_UUID, uuid.toString());
      }
   }

   @Nullable
   @Override
   public UUID getOwnerUUID() {
      String ownerStr = (String)this.entityDataManager.get(OWNER_UUID);
      if ("".equals(ownerStr)) {
         return null;
      }

      try {
         return UUID.fromString((String)this.entityDataManager.get(OWNER_UUID));
      } catch (Exception ex) {
         ex.printStackTrace();
         return null;
      }
   }

   @Override
   public int getHeldPlayerDistance() {
      return this.aQ;
   }

   @Override
   public void setHeldPlayerDistance(int distance) {
      this.aQ = distance;
   }

   protected String buildModelCodeDNA(StringBuilder builder) {
      appendPaddedNumber(builder, 3);
      appendPaddedNumber(builder, 2);
      appendPaddedNumber(builder, 2);
      appendPaddedNumber(builder, 8);
      appendPaddedNumber(builder, 8);
      appendPaddedNumber(builder, 5);
      appendPaddedNumber(builder, HairColor.values().length - 1);
      appendPaddedNumber(builder, SkinColor.values().length - 1);
      appendPaddedNumber(builder, EyeColor.values().length - 1);
      appendPaddedNumber(builder, 0);
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
   public Point2D getModelPartByIndex(int index) {
      switch (index) {
         case 0:
            return new Point2D(40, 130);
         case 1:
            return new Point2D(60, 130);
         case 2:
            return new Point2D(80, 130);
         case 3:
            return new Point2D(100, 130);
         case 4:
            return new Point2D(120, 130);
         case 5:
            return new Point2D(140, 130);
         case 6:
            return new Point2D(160, 130);
         case 7:
            return new Point2D(180, 130);
         case 8:
            return new Point2D(200, 0);
         case 9:
            return new Point2D(200, 130);
         default:
            return Point2D.ZERO;
      }
   }

   @Override
   public void setCustomPartList(List<Integer> parts) {
      StringBuilder builder = new StringBuilder();

      for (int partId : parts) {
         appendPaddedNumber(builder, partId);
      }

      appendPaddedNumber(builder, Integer.parseInt(getModelCodeParts(this)[9]));
      this.entityDataManager.set(APPEARANCE_DNA, builder.toString());
      if (Main.proxy instanceof ClientProxy) {
         GoblinRenderer.clearBoneColors();
      }
   }

   void updateModelCodeDNA() {
      if (this.customPartsData != null) {
         StringBuilder builder = new StringBuilder();

         for (Entry entry : this.customPartsData) {
            int partId = (Integer)((Entry)entry.getValue()).getValue();
            appendPaddedNumber(builder, partId);
         }

         appendPaddedNumber(builder, Integer.parseInt(getModelCodeParts(this)[9]));
         this.entityDataManager.set(APPEARANCE_DNA, builder.toString());
         GoblinRenderer.clearBoneColors();
      }
   }

   protected String buildModelCodeDNA(StringBuilder builder, int partIndex) {
      appendPaddedNumber(builder, 3);
      appendPaddedNumber(builder, 2);
      appendPaddedNumber(builder, 2);
      appendPaddedNumber(builder, 7);
      appendPaddedNumber(builder, 7);
      appendPaddedNumber(builder, 5);
      appendPaddedNumber(builder, HairColor.values().length - 1);
      appendPaddedNumber(builder, partIndex);
      appendPaddedNumber(builder, EyeColor.values().length - 1);
      appendPaddedNumber(builder, 0);
      return builder.toString();
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      nbt.setString("bodyColor", (String)this.entityDataManager.get(CURRENT_ACTION));
      nbt.setInteger("eyeColorX", ((BlockPos)this.entityDataManager.get(ACTION_TARGET_POS)).getX());
      nbt.setInteger("eyeColorY", ((BlockPos)this.entityDataManager.get(ACTION_TARGET_POS)).getY());
      nbt.setInteger("eyeColorZ", ((BlockPos)this.entityDataManager.get(ACTION_TARGET_POS)).getZ());
      nbt.setString("model", (String)this.entityDataManager.get(APPEARANCE_DNA));
      nbt.setString("girlID", (String)this.entityDataManager.get(GIRL_ID));
      nbt.setString("queen", (String)this.entityDataManager.get(aK));
      nbt.setBoolean("isQueen", this.aX);
      nbt.setBoolean("isTamed", (Boolean)this.entityDataManager.get(aC));
      nbt.setInteger("robTicks", this.aO);
      if (this.aX) {
         nbt.setBoolean("preggo", (Boolean)this.entityDataManager.get(aV));
         nbt.setFloat("throneRot", this.ac);
         nbt.setDouble("thronePosX", this.al.x);
         nbt.setDouble("thronePosY", this.al.y);
         nbt.setDouble("thronePosZ", this.al.z);
         nbt.setLong("impregnationTick", this.av);

         for (int i = 0; i < this.guardUUIDs.size(); i++) {
            nbt.setString("guard" + i, this.guardUUIDs.get(i).toString());
         }
      }
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      this.aX = nbt.getBoolean("isQueen");
      this.entityDataManager.set(APPEARANCE_DNA, nbt.getString("model"));
      this.entityDataManager.set(CURRENT_ACTION, nbt.getString("bodyColor"));
      String[] parts = getModelCodeParts(this);
      if (Integer.parseInt(parts[3]) > 7 || Integer.parseInt(parts[4]) > 7) {
         this.entityDataManager.set(APPEARANCE_DNA, this.buildModelCodeDNA(new StringBuilder(), this.getModelPartIndex()));
         Main.LOGGER.log(Level.INFO, "updated an old Goblin");
      }

      this.entityDataManager.set(ACTION_TARGET_POS, new BlockPos(nbt.getInteger("eyeColorX"), nbt.getInteger("eyeColorY"), nbt.getInteger("eyeColorZ")));
      this.entityDataManager.set(GIRL_ID, nbt.getString("girlID"));
      this.entityDataManager.set(aK, nbt.getString("queen"));
      this.entityDataManager.set(aC, nbt.getBoolean("isTamed"));
      this.aO = nbt.getInteger("robTicks");
      if (this.aX) {
         this.ac = nbt.getFloat("throneRot");
         this.al = new Vec3d(nbt.getDouble("thronePosX"), nbt.getDouble("thronePosY"), nbt.getDouble("thronePosZ"));

         for (int i = 0; !"".equals(nbt.getString("guard" + i)); i++) {
            this.guardUUIDs.add(UUID.fromString(nbt.getString("guard" + i)));
         }

         this.entityDataManager.set(aV, nbt.getBoolean("preggo"));
         this.av = nbt.getLong("impregnationTick");
      }
   }

   protected boolean processInteract(EntityPlayer player, EnumHand hand) {
      if (this.world.isRemote) {
         return true;
      }

      if (this.aX) {
         return true;
      }

      if (this.getCurrentAction() == Action.RUN) {
         if (this.getDistance(player) > 3.5) {
            player.sendStatusMessage(new TextComponentString("get a bit closer..."), true);
         } else {
            this.setTargetPosition(player.getPositionVector());
            this.setYawRotation(player.rotationYaw);
            this.setCurrentAction(Action.CATCH);
            this.entityDataManager.set(GIRL_HAND_STATES, "bj");
            this.setOwnerUUID(player.getPersistentID());
            this.setInteractionPlayerUUID(player.getPersistentID());
            this.getNavigator().clearPath();
            this.motionX = 0.0;
            this.motionY = 0.0;
            this.motionZ = 0.0;
         }

         return true;
      } else {
         if (hasGoblinWithUUID(player.getPersistentID())) {
            player.sendStatusMessage(new TextComponentString("you are already carrying a Goblin"), true);
         } else {
            this.setOwnerUUID(player.getPersistentID());
            this.setCurrentAction(Action.PICK_UP);
            this.aQ = 45;
            this.setAnchored(false);
            this.entityDataManager.set(aC, true);
            this.getNavigator().clearPath();
         }

         return true;
      }
   }

   public static boolean hasGoblinWithUUID(UUID uuid) {
      if (uuid == null) {
         return false;
      }

      try {
         for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
            if (girl instanceof IGoblin && !girl.world.isRemote && !girl.isDead) {
               UUID candidateUuid = ((IGoblin)girl).getOwnerUUID();
               if (candidateUuid.equals(uuid)) {
                  return true;
               }
            }
         }
      } catch (ConcurrentModificationException ex) {
      }

      return false;
   }

   @Override
   protected void initEntityAI() {
      this.watchClosestGirlGoal = new WatchClosestGirlGoal(this, EntityPlayer.class, 2.0F, 1.0F);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(3, new DoorInteractAiGoal(this));
      this.tasks.addTask(5, this.watchClosestGirlGoal);
   }

   /**
    * BOTH sides, every AI tick: dispatches every goblin subsystem —
    * gravity/despawn guard, pickup glue, throw progression, queen AI
    * (steal/breeding), held-state guarding, cooldowns and particle cycles.
    * Ordering is significant; see the individual handlers.
    */
   @Override
   public void updateAITasks() {
      super.updateAITasks();
      this.handleGravity();
      handlePickUpState(this);
      this.handleThrowState();
      this.B_clash269();
      this.J_clash267();
      this.E_clash260();
      this.handleJumpThrow();
      this.handleHoldCooldown();
      this.handleThrowCooldown();
      this.handleHeldParticles();
      this.handleHeldThrow();
      this.handleThrownLand();
      this.handleStandUp();
      this.handleHeldState();
   }

   public boolean canBeCollidedWith() {
      Action action = this.getCurrentAction();
      if (action == Action.THROWN) {
         return false;
      } else if (action == Action.RUN) {
         return super.canBeCollidedWith();
      } else if (action == Action.AWAIT_PICK_UP) {
         return super.canBeCollidedWith();
      } else if (this.getOwnerUUID() != null) {
         return false;
      } else {
         return action != Action.NULL ? false : super.canBeCollidedWith();
      }
   }

   void handleGoblinOwner(EntityPlayer player) {
      AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player.getPersistentID());
      Vec3d headPos = new Vec3d(player.posX, player.posY + (playerGirl == null ? player.eyeHeight : playerGirl.getEyeHeight()), player.posZ);
      Vec3d eyePos = new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
      double dist = eyePos.distanceTo(headPos);
      double heightDiff = headPos.y - eyePos.y;
      this.rotationPitch = (float)(-(Math.sin(heightDiff / dist) * (180.0 / Math.PI)));
   }

   void handleHeldState() {
      if ((Boolean)this.entityDataManager.get(aC)) {
         if (this.getInteractionPlayerUUID() == null) {
            if (this.getCurrentAction() == Action.NULL) {
               EntityPlayer player = this.world.getClosestPlayerToEntity(this, 15.0);
               if (player != null && player.getDistance(this) < 2.0F) {
                  this.handleGoblinOwner(player);
                  this.getNavigator().clearPath();
               } else {
                  if (this.guardPost == null
                     || this.getDistance(this.guardPost.getX(), this.guardPost.getY(), this.guardPost.getZ()) > this.getThrowRange()
                     || this.guardPostTicks > 100) {
                     int xOffset = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(5);
                     int zOffset = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(5);
                     int height = WorldUtils.getHeightAt(this.world, this.getPosition().getX() + xOffset, this.getPosition().getZ() + zOffset);
                     this.guardPost = new BlockPos(this.getPosition().getX() + xOffset, height, this.getPosition().getZ() + zOffset);
                     this.guardPostTicks = 0;
                  }

                  if (Math.sqrt(this.guardPost.distanceSq(this.getPosition())) > 2.0) {
                     this.getNavigator().tryMoveToXYZ(this.guardPost.getX(), this.guardPost.getY(), this.guardPost.getZ(), 0.3F);
                     this.tickPathVelocity();
                  } else {
                     this.guardPostTicks++;
                  }
               }
            }
         }
      }
   }

   double getThrowRange() {
      return Math.sqrt(800.0);
   }

   void handleStandUp() {
      if (this.getCurrentAction() == Action.STAND_UP) {
         if (++this.aa >= 37) {
            this.aa = 0;
            this.setCurrentAction(Action.NULL);
         }
      }
   }

   @Override
   public void setThrowTickCount(int tickCount) {
      this.aJ = tickCount;
   }

   @Override
   public int getThrowTickCount() {
      return this.aJ;
   }

   void handleThrownLand() {
      if (this.getCurrentAction() == Action.THROWN) {
         if (this.onGround) {
            int throwTick = this.getThrowTickCount() + 1;
            this.setThrowTickCount(throwTick);
            if (throwTick >= 30) {
               this.setThrowTickCount(0);
               this.setCurrentAction(Action.STAND_UP);
            }
         }
      }
   }

   void handleHeldThrow() {
      if (this.aX) {
         if ((Boolean)this.entityDataManager.get(aV)) {
            if (this.av + 8400L < this.world.getTotalWorldTime()) {
               this.entityDataManager.set(aV, false);
            }
         }
      }
   }

   void handleHeldParticles() {
      if (this.aX) {
         if (!this.ab.isEmpty()) {
            boolean particlesSpawned = false;

            for (GoblinEntity goblin : this.ab) {
               if ((Boolean)goblin.getDataManager().get(aC)) {
                  particlesSpawned = true;
               }
            }

            if (particlesSpawned) {
               this.sendGirlChatMessage("Farewell my knight. You are welcome once I am breedable again.");

               for (GoblinEntity goblin : this.ab) {
                  if (!(Boolean)goblin.getDataManager().get(aC)) {
                     goblin.setCurrentAction(Action.VANISH);
                  }
               }

               this.ab.clear();
               this.setInteractionPlayerUUID(null);
            }
         }
      }
   }

   /**
    * SERVER: ends the breeding — after 100 ticks the queen unbinds the
    * player, releases both guards anchored as AWAIT_PICK_UP (rewards) and
    * restores the player's physics.
    */
   void handleThrowCooldown() {
      if (this.aX) {
         if (this.throwCooldown != -1) {
            if (++this.throwCooldown >= 100) {
               this.throwCooldown = -1;
               UUID uuid = this.getInteractionPlayerUUID();
               if (uuid == null) {
                  this.resetCameraAndPhysics();
               } else {
                  EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
                  if (player == null) {
                     this.resetCameraAndPhysics();
                  } else {
                     this.setInteractionPlayerUUID(null);

                     for (GoblinEntity goblin : this.ab) {
                        goblin.setInteractionPlayerUUID(null);
                     }

                     List positions = this.I_clash261();
                     float yaw = this.ac + 180.0F;
                     Vec3d leftPos = this.al.add(rotateVectorYaw(aT, yaw));
                     Vec3d rightPos = this.al.add(rotateVectorYaw(ap, yaw));
                     Vec3d playerPos = this.al.add(rotateVectorYaw(as, yaw));
                     GoblinEntity leftGoblin = (GoblinEntity)positions.get(0);
                     GoblinEntity rightGoblin = (GoblinEntity)positions.get(1);
                     leftGoblin.setTargetPosition(leftPos);
                     rightGoblin.setTargetPosition(rightPos);
                     leftGoblin.setYawRotation(0.0F);
                     rightGoblin.setYawRotation(0.0F);
                     leftGoblin.setAnchored(true);
                     rightGoblin.setAnchored(true);
                     leftGoblin.setCurrentAction(Action.AWAIT_PICK_UP);
                     rightGoblin.setCurrentAction(Action.AWAIT_PICK_UP);
                     leftGoblin.setNoGravity(false);
                     rightGoblin.setNoGravity(false);
                     player.setNoGravity(false);
                     leftGoblin.noClip = false;
                     rightGoblin.noClip = false;
                     player.noClip = false;
                     player.rotationYaw = yaw;
                     player.rotationPitch = 30.0F;
                     player.setPositionAndUpdate(playerPos.x, playerPos.y, playerPos.z);
                     PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)player);
                     this.sendGirlChatMessage(
                        "Thanks to you, my clan is soon going to get a few new members! In return I will bear of one of my guards to serve as your personal Onahole. Choose wisely~"
                     );
                  }
               }
            }
         }
      }
   }

   void handleHoldCooldown() {
      if (this.aX) {
         if (this.an != -1) {
            if (++this.an >= 205) {
               this.an = -1;
               UUID uuid = this.getInteractionPlayerUUID();
               if (uuid != null) {
                  EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
                  if (player != null) {
                     Vec3d pos = rotateVectorYaw(new Vec3d(0.0, 0.15625 - player.getEyeHeight(), -0.8859375), this.ac - 180.0F);
                     pos = pos.add(this.getTargetPosition());
                     player.setPositionAndUpdate(pos.x, pos.y, pos.z);
                  }
               }
            }
         }
      }
   }

   public static Vec3d rotateVectorYaw(Vec3d vec, float yaw) {
      return rotateVectorPitchYaw(vec, 0.0F, yaw);
   }

   public static Vec3d rotateVectorPitchYaw(Vec3d vec, float pitch, float yaw) {
      Vec3d rotated = new Vec3d(
         vec.x,
         vec.y * Math.cos(pitch * (Math.PI / 180.0)) - vec.z * Math.sin(pitch * (Math.PI / 180.0)),
         vec.y * Math.sin(pitch * (Math.PI / 180.0)) + vec.z * Math.cos(pitch * (Math.PI / 180.0))
      );
      return new Vec3d(
         -Math.sin((yaw + 90.0F) * (Math.PI / 180.0)) * rotated.x - Math.sin(yaw * (Math.PI / 180.0)) * rotated.z,
         rotated.y,
         Math.cos((yaw + 90.0F) * (Math.PI / 180.0)) * rotated.x + Math.cos(yaw * (Math.PI / 180.0)) * rotated.z
      );
   }

   /**
    * SERVER: throws the guards onto the breeding player — after 26 ticks in
    * {@link Action#JUMP_0} the queen repositions (per throne rotation) and
    * starts the breeding intro for herself and both guards; the 205-tick hold
    * cooldown ({@code an}) then positions the player.
    */
   void handleJumpThrow() {
      if (this.aX) {
         if (this.getCurrentAction() == Action.JUMP_0) {
            if (++this.am >= 26) {
               this.am = 0;
               Vec3d throwPos;
               switch ((int)this.ac) {
                  case -90:
                     throwPos = this.al.add(at);
                     break;
                  case 90:
                     throwPos = this.al.add(au);
                     break;
                  case 180:
                     throwPos = this.al.add(THROW_OFFSET_W);
                     break;
                  default:
                     throwPos = this.al.add(af);
               }

               UUID uuid = this.getInteractionPlayerUUID();
               if (uuid != null) {
                  EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
                  if (player != null) {
                     this.setTargetPosition(throwPos);
                     this.setYawRotation(this.ac);
                     this.setCurrentAction(Action.BREEDING_INTRO_0);
                     this.noClip = true;
                     this.setNoGravity(true);
                     Vec3d pos = rotateVectorYaw(new Vec3d(0.0, 0.44375 - player.eyeHeight, -0.7875), this.ac - 180.0F);
                     player.noClip = true;
                     player.setNoGravity(true);
                     player.setPositionAndUpdate(
                        pos.x + throwPos.x, pos.y + throwPos.y, pos.z + throwPos.z
                     );
                     List goblins = this.I_clash261();
                     if (goblins.size() >= 1) {
                        GoblinEntity goblin = (GoblinEntity)goblins.get(0);
                        goblin.setTargetPosition(throwPos);
                        goblin.setYawRotation(this.ac);
                        goblin.setCurrentAction(Action.BREEDING_INTRO_1);
                        goblin.noClip = true;
                        goblin.setNoGravity(true);
                     }

                     if (goblins.size() >= 2) {
                        GoblinEntity goblin2 = (GoblinEntity)goblins.get(1);
                        goblin2.setTargetPosition(throwPos);
                        goblin2.setYawRotation(this.ac);
                        goblin2.setCurrentAction(Action.BREEDING_INTRO_2);
                        goblin2.noClip = true;
                        goblin2.setNoGravity(true);
                     }

                     this.an = 0;
                  }
               }
            }
         }
      }
   }

   AxisAlignedBB createThrowHitbox(Vec3d min, Vec3d max) {
      return new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
   }

   /**
    * SERVER: queen breeding — scans the throne zone in front of the queen
    * (per throne rotation) for a grounded player; when found (and not
    * pregnant) locks them in and starts the breeding intro
    * ({@link Action#JUMP_0}, guards get JUMP_1/JUMP_2).
    */
   void E_clash260() {
      if (this.aX) {
         if (this.getInteractionPlayerUUID() == null) {
            Vec3d basePos = null;
            switch ((int)this.ac) {
               case -90:
                  basePos = ao;
                  break;
               case 0:
                  basePos = aM;
                  break;
               case 90:
                  basePos = THROW_OFFSET_U;
                  break;
               case 180:
                  basePos = aB;
            }

            if (basePos != null) {
               Vec3d offset = this.al.subtract(0.5, 0.0, 0.5).subtract(basePos);
               AxisAlignedBB aabb = this.createThrowHitbox(offset, offset.add(ah.getX(), ah.getY(), ah.getZ()));
               List players = this.world.getEntitiesWithinAABB(EntityPlayer.class, aabb);
               if (!players.isEmpty()) {
                  EntityPlayer player = (EntityPlayer)players.get(0);
                  if (player.onGround) {
                     if ((Boolean)this.entityDataManager.get(aV)) {
                        if (this.ai + 1200L < this.world.getTotalWorldTime()) {
                           player.sendStatusMessage(new TextComponentString("The Queen is still pregnant - so no breeding for you uwu"), true);
                           this.ai = this.world.getTotalWorldTime();
                        }
                     } else {
                        UUID uuid = player.getPersistentID();
                        Vec3d pos = player.getPositionVector();
                        float yaw = player.rotationYaw + 180.0F;
                        PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
                        this.setInteractionPlayerUUID(uuid);
                        this.setCurrentAction(Action.JUMP_0);
                        this.setTargetPosition(pos);
                        this.setYawRotation(yaw);
                        this.setAnchored(true);
                        List goblins = this.I_clash261();
                        if (goblins.size() > 0) {
                           GoblinEntity goblin = (GoblinEntity)goblins.get(0);
                           goblin.setInteractionPlayerUUID(uuid);
                           goblin.setCurrentAction(Action.JUMP_1);
                           goblin.setTargetPosition(pos);
                           goblin.setYawRotation(yaw);
                           goblin.setAnchored(true);
                           if (goblins.size() > 1) {
                              GoblinEntity goblin2 = (GoblinEntity)goblins.get(1);
                              goblin2.setInteractionPlayerUUID(uuid);
                              goblin2.setCurrentAction(Action.JUMP_2);
                              goblin2.setTargetPosition(pos);
                              goblin2.setYawRotation(yaw);
                              goblin2.setAnchored(true);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * SERVER: returns the queen's two guard goblins, respawning them (same
    * model part, her girl id as queen key) when fewer than two exist.
    */
   List<GoblinEntity> I_clash261() {
      if (this.ab.size() > 1) {
         return this.ab;
      }

      for (GoblinEntity goblin : this.ab) {
         this.world.removeEntity(goblin);
      }

      this.ab.clear();
      GoblinEntity goblin = new GoblinEntity(this.world, this.getGirlId().toString(), this.getModelPartIndex());
      goblin.setPosition(this.posX, this.posY, this.posZ);
      this.world.spawnEntity(goblin);
      this.ab.add(goblin);
      GoblinEntity goblin2 = new GoblinEntity(this.world, this.getGirlId().toString(), this.getModelPartIndex());
      goblin2.setPosition(this.posX, this.posY, this.posZ);
      this.world.spawnEntity(goblin2);
      this.ab.add(goblin2);
      return this.ab;
   }

   void handleGravity() {
      if (!this.aZ) {
         this.noClip = false;
         this.setNoGravity(false);
         if (!this.aX && !(Boolean)this.entityDataManager.get(aC) && !((String)this.entityDataManager.get(aK)).equals("") && this.getCurrentAction() == Action.NULL) {
            this.world.removeEntity(this);
         }

         this.aZ = true;
      }
   }

   /**
    * SERVER: throw flight — advances the throw progress; at tick 15 the
    * goblin is launched from the owner's eye with the owner's aim
    * (pitch/yaw), at tick 39 she lands (THROWN) and unbinds owner +
    * interaction player.
    */
   void updateThrowProgress() {
      GoblinEntity goblin = this;
      int progress = goblin.getThrowProgress();
      if (progress != -1) {
         goblin.setThrowProgress(++progress);
         if (progress == 15) {
            Vec3d pos = getGoblinThrowPos(this);
            float pitch = getGoblinThrowHeight(this);
            float yaw = getGoblinThrowDistance(this);
            this.setPositionAndUpdate(pos.x, pos.y, pos.z);
            Vec3d vec = rotateVectorPitchYaw(new Vec3d(0.0, 0.0, 1.5), pitch, yaw);
            this.motionX = vec.x;
            this.motionY = vec.y;
            this.motionZ = vec.z;
            if (!this.world.isRemote) {
               this.setYawRotation(yaw);
            }
         }

         this.noClip = false;
         this.setNoGravity(false);
         if (progress == 39) {
            this.setThrowProgress(-1);
            this.setCurrentAction(Action.THROWN);
            this.setInteractionPlayerUUID(null);
            this.setOwnerUUID(null);
         }
      }
   }

   public static Vec3d getGoblinThrowPos(BaseGirlEntity girl) {
      IGoblin goblin = (IGoblin)girl;
      UUID uuid = goblin.getOwnerUUID();
      if (uuid == null) {
         return girl.getPositionVector();
      }

      EntityPlayer player = girl.world.getPlayerEntityByUUID(uuid);
      return player == null
         ? girl.getPositionVector()
         : player.getPositionVector().add(0.0, player.getEyeHeight(), 0.0).add(rotateVectorPitchYaw(new Vec3d(0.4F, 0.0, 0.0), getGoblinThrowHeight(girl), getGoblinThrowDistance(girl)));
   }

   public static float getGoblinThrowDistance(BaseGirlEntity girl) {
      IGoblin goblin = (IGoblin)girl;
      UUID uuid = goblin.getOwnerUUID();
      if (uuid == null) {
         return 0.0F;
      }

      EntityPlayer player = girl.world.getPlayerEntityByUUID(uuid);
      return player == null ? 0.0F : player.rotationYawHead;
   }

   public static float getGoblinThrowHeight(BaseGirlEntity girl) {
      IGoblin goblin = (IGoblin)girl;
      UUID uuid = goblin.getOwnerUUID();
      if (uuid == null) {
         return 0.0F;
      }

      EntityPlayer player = girl.world.getPlayerEntityByUUID(uuid);
      return player == null ? 0.0F : player.rotationPitch;
   }

   void J_clash267() {
      if (this.onGround) {
         if (this.getCurrentAction() == Action.RUN) {
            EntityPlayer player = this.world.getClosestPlayerToEntity(this, 100.0);
            if (player != null) {
               double distance = 20.0;

               while (!(distance <= 0.0)) {
                  Vec3d delta = this.getPositionVector().subtract(player.getPositionVector());
                  Vec3d absDelta = new Vec3d(Math.abs(delta.x), Math.abs(delta.y), Math.abs(delta.z));
                  double xWeight = absDelta.x / (absDelta.x + absDelta.z);
                  double zWeight = absDelta.z / (absDelta.x + absDelta.z);
                  Vec3d targetPos = this.getPositionVector()
                     .add(new Vec3d((delta.x > 0.0 ? 1 : -1) * xWeight * distance, 0.0, (delta.z > 0.0 ? 1 : -1) * zWeight * distance));
                  PathNavigate navigator = this.getNavigator();
                  navigator.clearPath();
                  boolean moved = navigator.tryMoveToXYZ(targetPos.x, targetPos.y, targetPos.z, 0.825F);
                  distance--;
                  if (moved) {
                     return;
                  }
               }
            }
         }
      }
   }

   protected void jump() {
      if (this.getCurrentAction() != Action.RUN || this.hasValidPath()) {
         super.jump();
      }
   }

   boolean hasValidPath() {
      PathNavigate navigator = this.getNavigator();
      Path path = navigator.getPath();
      if (path == null) {
         return true;
      } else {
         int currentIndex = path.getCurrentPathIndex();
         int length = path.getCurrentPathLength();
         if (length != currentIndex && length - 1 != currentIndex) {
            PathPoint currentPoint = path.getPathPointFromIndex(currentIndex);
            PathPoint nextPoint = path.getPathPointFromIndex(currentIndex + 1);
            return nextPoint.y - currentPoint.y == 1;
         } else {
            return true;
         }
      }
   }

   /**
    * SERVER: the queen's gold-steal cycle — every 32000 ticks while sitting
    * and unowned she spawns a RUN goblin that grabs a random gold item from
    * the nearest grounded player's inventory and brings it home.
    */
   void B_clash269() {
      if (this.aX) {
         if (!(Boolean)this.entityDataManager.get(aC)) {
            if (!(Boolean)this.entityDataManager.get(aV)) {
               if (this.getCurrentAction() == Action.SIT) {
                  if (++this.aO >= 32000) {
                     EntityPlayer player = this.world.getClosestPlayerToEntity(this, 3000.0);
                     if (player != null) {
                        if (player.onGround) {
                           if (!player.isAirBorne) {
                              Integer slotIndex = this.findThrowTarget(player);
                              if (slotIndex != null) {
                                 Vec3d playerPos = player.getPositionVector();
                                 Vec3d selfPos = this.getPositionVector();
                                 Vec3d delta = playerPos.subtract(selfPos);
                                 double dist = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
                                 if (!(dist > 100.0)) {
                                    ItemStack stolenStack = player.inventory.getStackInSlot(slotIndex).copy();
                                    GoblinEntity goblin = new GoblinEntity(this.world, this.getGirlId().toString(), this.getModelPartIndex());
                                    Vec3d offset = rotateVectorYaw(new Vec3d(0.0, 0.0, -0.2F), player.rotationYawHead);
                                    goblin.setPosition(player.posX + offset.x, player.posY, player.posZ + offset.z);
                                    goblin.setCurrentAction(Action.RUN);
                                    this.world.spawnEntity(goblin);
                                    goblin.entityDataManager.set(a0, stolenStack);
                                    player.sendMessage(
                                       new TextComponentString(String.format("<%s> I got your %s hehe~", goblin.getDisplayNameText(), stolenStack.getDisplayName()))
                                    );
                                    player.inventory.removeStackFromSlot(slotIndex);
                                    this.aO = 0;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   int getModelPartIndex() {
      return Integer.parseInt(getModelCodeParts(this)[7]);
   }

   @Nullable
   Integer findThrowTarget(EntityPlayer player) {
      NonNullList inventory = player.inventory.mainInventory;
      ArrayList validSlots = new ArrayList();

      for (int i = 0; i < inventory.size(); i++) {
         ItemStack stack = (ItemStack)inventory.get(i);
         if (stack != ItemStack.EMPTY && ag.contains(stack.getItem())) {
            validSlots.add(i);
         }
      }

      return validSlots.isEmpty() ? null : (Integer)validSlots.get(this.getRNG().nextInt(validSlots.size()));
   }

   void handleThrowState() {
      if (this.aX) {
         if (this.getInteractionPlayerUUID() == null) {
            this.setTargetPosition(this.al);
            this.setYawRotation(this.ac);
            this.setAnchored(true);
            this.setNoGravity(true);
            this.setCurrentAction(Action.SIT);
         }
      }
   }

   @Override
   public void onUpdate() {
      this.updateModelCodeDNA();
      handleGoblinThrowAction(this);
      this.updateThrowProgress();
      if (this.getOwnerUUID() != null) {
         this.inPortal = false;
      }

      super.onUpdate();
      this.handleShoulderIdle();
      this.H_clash275();
      this.F_clash274();
      if (this.world.isRemote) {
         this.handleHoldTick();
         this.A_clash277();
         if (this.getOwnerUUID() != null) {
            this.noClip = true;
         }
      }
   }

   @Override
   public Action getPreviousAction() {
      return this.aN;
   }

   @Override
   public void setPreviousAction(Action action) {
      this.aN = action;
   }

   @Override
   public void setThrowProgress(int progress) {
      this.aR = progress;
   }

   @Override
   public int getThrowProgress() {
      return this.aR;
   }

   public static void handleGoblinThrowAction(BaseGirlEntity girl) {
      Action action = girl.getCurrentAction();
      IGoblin goblin = (IGoblin)girl;
      if (goblin.getPreviousAction() != Action.START_THROWING && action == Action.START_THROWING) {
         goblin.setThrowProgress(0);
      }

      goblin.setPreviousAction(action);
   }

   public void setFire(int seconds) {
      if (this.getOwnerUUID() == null) {
         super.setFire(seconds);
      }
   }

   void F_clash274() {
      if (this.getCurrentAction() == Action.VANISH) {
         this.ar -= 0.05F;
         if (!(this.ar > 0.0F)) {
            this.world.removeEntity(this);
         }
      }
   }

   void H_clash275() {
      if (!(Boolean)this.entityDataManager.get(aC)) {
         if (this.getCurrentAction() == Action.THROWN) {
            if (this.onGround || this.isInWater()) {
               this.ar = (float)(this.ar - 0.05);
               if (!(this.ar > 0.0F)) {
                  if (!this.world.isRemote) {
                     this.setCurrentAction(Action.NULL);
                     this.setInteractionPlayerUUID(null);
                     this.setOwnerUUID(null);
                     this.world.removeEntity(this);
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void handleHoldTick() {
      if (this.aY != -1) {
         if (++this.aY == 15) {
            this.aY = -1;
            this.setCurrentAction(Action.PAIZURI_START);
            Minecraft.getMinecraft().player.closeScreen();
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void A_clash277() {
      if (this.az != -1) {
         if (++this.az == 15) {
            this.az = -1;
            this.setCurrentAction(Action.NELSON_INTRO);
            Minecraft mc = Minecraft.getMinecraft();
            mc.player.closeScreen();
            mc.gameSettings.thirdPersonView = 2;
         }
      }
   }

   /**
    * SERVER: the goblin state machine core — guards the cum actions
    * (paizuri/nelson/breeding), and on the server fires the per-action setup
    * hooks (START_THROWING returns the stolen item, PAIZURI_START/
    * NELSON_INTRO position the player, BREEDING_CUM_0 marks pregnant and
    * arms the release cooldown, NELSON_CUM toggles the pregnancy flag, and
    * leaving PAIZURI_CUM runs {@link #D_clash278()}).
    */
   @Override
   public void setCurrentAction(Action action) {
      Action currentAction = this.getCurrentAction();
      if (currentAction != Action.PAIZURI_CUM || action != Action.PAIZURI_SLOW && action != Action.PAIZURI_FAST) {
         if (currentAction != Action.NELSON_CUM || action != Action.NELSON_SLOW && action != Action.NELSON_FAST) {
            if (currentAction != Action.BREEDING_CUM_0 || action != Action.BREEDING_SLOW_0 && action != Action.BREEDING_FAST_0) {
               if (action == Action.START_THROWING && !this.world.isRemote) {
                  this.setInteractionPlayerUUID(this.getOwnerUUID());
                  this.L_clash281();
               }

               if (action == Action.PAIZURI_START && !this.world.isRemote) {
                  this.handlePlayerInteract();
               }

               if (action == Action.NELSON_INTRO && !this.world.isRemote) {
                  this.handlePlayerLook();
               }

               if (this.getCurrentAction() == Action.PAIZURI_CUM && action == Action.NULL && !this.world.isRemote) {
                  this.D_clash278();
               }

               if (action == Action.BREEDING_CUM_0) {
                  this.entityDataManager.set(aV, true);
                  this.av = this.world.getTotalWorldTime();
                  this.ai = this.world.getTotalWorldTime();
               }

               if (action == Action.BREEDING_CUM_0) {
                  this.throwCooldown = 0;
               }

               if (action == Action.NELSON_CUM) {
                  this.entityDataManager.set(aV, true);
               }

               if (currentAction == Action.NELSON_CUM && action != Action.NELSON_CUM) {
                  this.entityDataManager.set(aV, false);
               }

               super.setCurrentAction(action);
            }
         }
      }
   }

   /**
    * SERVER: paizuri scene teardown — resets the bound player, unbinds,
    * un-anchors, restores physics, drops the stolen item and (untamed) sends
    * her home and despawns her.
    */
   void D_clash278() {
      EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      if (player != null) {
         ResetGirlPacket.Handler.resetGirls((EntityPlayerMP)player);
      }

      this.setInteractionPlayerUUID(null);
      this.setAnchored(false);
      this.noClip = false;
      this.setNoGravity(false);
      this.entityDataManager.set(a0, ItemStack.EMPTY);
      if (!(Boolean)this.entityDataManager.get(aC)) {
         this.setPositionAndUpdate(this.homePos.x, this.homePos.y, this.homePos.z);
         this.world.removeEntity(this);
      }
   }

   void handlePlayerLook() {
      EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      if (player != null) {
         this.setOwnerUUID(null);
         this.setTargetPosition(player.getPositionVector());
         this.setYawRotation(player.rotationYaw);
         this.setAnchored(true);
         this.noClip = true;
         this.setNoGravity(true);
         player.setNoGravity(true);
         player.noClip = true;
         this.setInteractionPlayerUUID(player.getPersistentID());
      }
   }

   void handlePlayerInteract() {
      EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      if (player != null) {
         this.setOwnerUUID(null);
         this.setTargetPosition(player.getPositionVector());
         this.setYawRotation(player.rotationYaw + 180.0F);
         this.setAnchored(true);
         this.noClip = true;
         this.setNoGravity(true);
         player.setNoGravity(true);
         player.noClip = true;
         this.setInteractionPlayerUUID(player.getPersistentID());
         player.setPositionAndUpdate(player.posX, player.posY - 0.5, player.posZ);
         player.rotationPitch = 70.0F;
         player.prevRotationPitch = 70.0F;
      }
   }

   /**
    * SERVER: returns the stolen item stack to the interaction player's
    * inventory and clears the stolen-item key.
    */
   void L_clash281() {
      ItemStack heldItem = (ItemStack)this.entityDataManager.get(a0);
      if (heldItem != ItemStack.EMPTY) {
         EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
         if (player != null) {
            player.inventory.addItemStackToInventory(heldItem.copy());
            this.entityDataManager.set(a0, ItemStack.EMPTY);
         }
      }
   }

   /**
    * SERVER: pickup glue — while PICK_UP with a bound owner, sticks the goblin
    * to the owner's position, counts down {@code aQ} (45) and reaches
    * SHOULDER_IDLE (noClip) at 0. Falls back to NULL when the owner vanishes
    * or exceeds 10 blocks.
    */
   public static void handlePickUpState(BaseGirlEntity girl) {
      if (girl.getCurrentAction() == Action.PICK_UP) {
         IGoblin goblin = (IGoblin)girl;
         UUID uuid = goblin.getOwnerUUID();
         if (uuid == null) {
            goblin.setHeldPlayerDistance(-1);
            girl.setCurrentAction(Action.NULL);
            goblin.setOwnerUUID(null);
         } else {
            EntityPlayer player = girl.world.getPlayerEntityByUUID(uuid);
            if (player == null) {
               goblin.setHeldPlayerDistance(-1);
               girl.setCurrentAction(Action.NULL);
               goblin.setOwnerUUID(null);
            } else {
               girl.setPosition(player.posX, player.posY, player.posZ);
               if (girl.getPositionVector().distanceTo(player.getPositionVector()) > 10.0) {
                  goblin.setHeldPlayerDistance(-1);
                  girl.setCurrentAction(Action.NULL);
                  goblin.setOwnerUUID(null);
               } else {
                  int distance = goblin.getHeldPlayerDistance() - 1;
                  goblin.setHeldPlayerDistance(distance);
                  if (distance == 0) {
                     girl.setCurrentAction(Action.SHOULDER_IDLE);
                     girl.noClip = true;
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean shouldRenderNameTag() {
      if (this.getCurrentAction() != Action.NULL) {
         return false;
      } else if (this.getOwnerUUID() != null) {
         return false;
      } else {
         return !this.entityDataManager.get(aC) && !Minecraft.getMinecraft().player.canEntityBeSeen(this) ? false : this.getOwnerUUID() == null;
      }
   }

   void handleShoulderIdle() {
      if (this.getCurrentAction() == Action.SHOULDER_IDLE) {
         UUID uuid = this.getOwnerUUID();
         if (uuid != null) {
            EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
            if (player != null) {
               this.setPosition(player.posX, player.posY, player.posZ);
               this.noClip = true;
               this.setNoGravity(true);
            }
         }
      }
   }

   @Override
   protected Action getNextAction(Action action) {
      switch (action) {
         case PAIZURI_IDLE:
         case PAIZURI_SLOW:
            return Action.PAIZURI_FAST;
         case BREEDING_SLOW_0:
            return Action.BREEDING_FAST_0;
         case BREEDING_SLOW_2:
            return Action.BREEDING_FAST_2;
         case NELSON_SLOW:
            return Action.NELSON_FAST;
         default:
            return null;
      }
   }

   @Override
   protected Action getCumAction(Action action) {
      switch (action) {
         case PAIZURI_SLOW:
         case PAIZURI_FAST:
         case PAIZURI_FAST_CONTINUES:
            return Action.PAIZURI_CUM;
         case BREEDING_SLOW_0:
         case BREEDING_FAST_0:
            for (GoblinEntity goblin : this.ab) {
               goblin.getCumAction(action);
            }

            return Action.BREEDING_CUM_0;
         case BREEDING_SLOW_2:
         case BREEDING_FAST_2:
            return Action.BREEDING_CUM_2;
         case NELSON_SLOW:
         case NELSON_FAST:
            return Action.NELSON_CUM;
         case BREEDING_1:
            return Action.BREEDING_CUM_1;
         default:
            return null;
      }
   }

   public boolean C_clash285() {
      Block block = this.world.getBlockState(this.getPosition().add(0, 1, 0)).getBlock();
      return !block.isPassable(this.world, this.getPosition().add(0, 1, 0));
   }

   public void fall(float distance, float multiplier) {
      Action action = this.getCurrentAction();
      if (action != Action.THROWN && action != Action.START_THROWING) {
         super.fall(distance, multiplier);
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      if (this.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.goblin.null", true, event);
            } else {
               this.createAnimation("animation.goblin.blink", true, event);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.goblin.null", true, event);
            } else {
               double moved = Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ);
               if (!(Boolean)this.entityDataManager.get(IS_ANCHORED) && moved > 0.0) {
                  if (this.onGround && Math.abs(Math.abs(this.prevPosY) - Math.abs(this.posY)) < 0.1F) {
                     if (moved > 0.2F) {
                        this.createAnimation("animation.goblin.walk", true, event);
                     } else {
                        this.createAnimation("animation.goblin.walk", true, event);
                     }

                     this.rotationYaw = this.rotationYawHead;
                  } else {
                     this.createAnimation("animation.goblin.fly", true, event);
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
               case PAIZURI_IDLE:
                  this.createAnimation("animation.goblin.paizuri_idle", true, event);
                  break;
               case PAIZURI_SLOW:
                  this.createAnimation("animation.goblin.paizuri_slow" + this.aP, true, event);
                  break;
               case BREEDING_SLOW_0:
                  this.createAnimation("animation.goblin.breeding_slow_1" + (this.aD ? "l" : "r"), true, event);
                  break;
               case BREEDING_SLOW_2:
                  this.createAnimation("animation.goblin.breeding_slow_3", true, event);
                  break;
               case NELSON_SLOW:
                  this.createAnimation("animation.goblin.nelson_slow" + (this.aF ? "" : "2"), true, event);
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
               case NELSON_FAST:
                  this.createAnimation("animation.goblin.nelson_fast" + (this.nelsonAltPose ? "c" : "s"), true, event);
                  break;
               case BREEDING_FAST_0:
                  this.createAnimation("animation.goblin.breeding_fast_1" + (this.ay ? "c" : "s"), true, event);
                  break;
               case NULL:
                  this.createAnimation("animation.goblin.null", true, event);
                  break;
               case SHOULDER_IDLE:
                  this.createAnimation("animation.goblin.shoulder_idle", true, event);
                  break;
               case PICK_UP:
                  this.createAnimation(String.format("animation.goblin.pick_up_%sperson", camMode), true, event);
                  break;
               case SIT:
                  this.createAnimation("animation.goblin.sit", true, event);
                  break;
               case RUN:
                  if (this.onGround) {
                     this.createAnimation("animation.goblin.running", true, event);
                  } else {
                     this.createAnimation("animation.goblin.fly", true, event);
                  }
                  break;
               case CATCH:
                  this.createAnimation(String.format("animation.goblin.catch_%sperson", camMode), true, event);
                  break;
               case CATCH_BJ:
                  this.createAnimation(String.format("animation.goblin.catch_%spersonBj", camMode), true, event);
                  break;
               case CATCH_BJ_IDLE:
                  this.createAnimation(String.format("animation.goblin.catch_%spersonBj_idle", camMode), true, event);
                  break;
               case START_THROWING:
                  this.createAnimation(String.format("animation.goblin.throw_%sperson", camMode), true, event);
                  break;
               case THROWN:
                  this.createAnimation("animation.goblin.thrown", true, event);
                  break;
               case PAIZURI_START:
                  this.createAnimation("animation.goblin.paizuri_start", true, event);
                  break;
               case PAIZURI_CUM:
                  this.createAnimation("animation.goblin.paizuri_cum", true, event);
                  break;
               case JUMP_0:
                  this.createAnimation("animation.goblin.jump_1", true, event);
                  break;
               case JUMP_1:
                  this.createAnimation("animation.goblin.jump_2", true, event);
                  break;
               case JUMP_2:
                  this.createAnimation("animation.goblin.jump_3", true, event);
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
               case BREEDING_CUM_0:
                  this.createAnimation("animation.goblin.breeding_cum_1", true, event);
                  break;
               case BREEDING_CUM_1:
                  this.createAnimation("animation.goblin.breeding_cum_2", true, event);
                  break;
               case BREEDING_CUM_2:
                  this.createAnimation("animation.goblin.breeding_cum_3", true, event);
                  break;
               case VANISH:
               case AWAIT_PICK_UP:
                  this.createAnimation("animation.goblin.await_pick_up", true, event);
                  break;
               case STAND_UP:
                  this.createAnimation("animation.goblin.stand_up", false, event);
                  break;
               case NELSON_INTRO:
                  this.createAnimation("animation.goblin.nelson_intro", true, event);
                  break;
               case NELSON_CUM:
                  this.createAnimation("animation.goblin.nelson_cum", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   /**
    * CLIENT: registers the controllers plus the sound listener driving the
    * catch dialogue, paizuri, nelson and breeding scenes. Key transitions:
    * {@code catchDone} -&gt; {@link Action#CATCH_BJ} when the catch was a
    * blowjob, {@code paizuri_startDone} -&gt; {@link Action#PAIZURI_IDLE},
    * {@code neslon_introDone} -&gt; {@link Action#NELSON_SLOW},
    * {@code breedingIntroDone} -&gt; {@link Action#BREEDING_SLOW_0}, jump on
    * the ready keyframes switches fast/hard, {@code paizuriCumDone}/
    * {@code nelson_cumDone} -&gt; {@code resetCameraAndPhysics()} + NULL.
    * Movement controller uses a 2-tick transition.
    */
   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData data) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
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
                  this.aP = "".equals(this.aP) ? "2" : "";
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
            case "paizuriCumDone":
               this.setCurrentAction(Action.NULL);
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
                  this.aD = !this.aD;
               }

               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.BREEDING_FAST_0);
                  this.ay = false;
               }
               break;
            case "breeding_fast1Done":
               this.setCurrentAction(Action.BREEDING_SLOW_0);
               if (this.isControlledByLocalPlayer()) {
                  this.ay = false;
               }
               break;
            case "breeding_fast1Ready":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.ay = true;
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
                  this.aF = !this.aF;
               }
               break;
            case "neslon_fastSwitch":
               if (!this.isControlledByLocalPlayer()) {
                  this.nelsonAltPose = true;
                  return;
               }

               if (HandlePlayerMovement.isJumping) {
                  this.nelsonAltPose = true;
               }
               break;
            case "neslon_fastBackSwitch":
               if (!this.isControlledByLocalPlayer()) {
                  this.actionController.tickOffset = 0.0;
               } else if (HandlePlayerMovement.isJumping) {
                  this.actionController.tickOffset = 0.0;
               }
               break;
            case "nelsonFastDone":
               this.nelsonAltPose = false;
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.NELSON_SLOW);
               }
               break;
            case "nelson_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.resetCameraAndPhysics();
                  this.setCurrentAction(Action.NULL);
               }
         }
      };
      this.actionController.registerSoundListener(soundListener);
      this.movementController.transitionLengthTicks = 10.0;
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
   }

   public static class c {
      static Minecraft mcUnused = null;

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onClientTick(ClientTickEvent event) {
         if (event.phase != Phase.START) {
            ArrayList goblins = new ArrayList();

            try {
               for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
                  if (girl.world.isRemote && girl instanceof GoblinEntity) {
                     GoblinEntity goblin = (GoblinEntity)girl;
                     UUID uuid = goblin.getOwnerUUID();
                     if (uuid != null) {
                        EntityPlayer player = goblin.world.getPlayerEntityByUUID(uuid);
                        if (player != null && player.dimension != goblin.dimension) {
                           goblins.add(goblin);
                        }
                     }
                  }
               }
            } catch (ConcurrentModificationException ex) {
            }

            for (GoblinEntity goblin : (java.util.Collection<GoblinEntity>) (goblins) ) {
               goblin.setOwnerUUID(null);
               goblin.setInteractionPlayerUUID(null);
               goblin.setDead();
            }
         }
      }

      @SubscribeEvent
      public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
         EntityPlayer player = event.player;
         UUID uuid = player.getPersistentID();
         int dimension = event.toDim;
         World world = player.world;
         GoblinEntity existing = null;

         try {
            for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
               if (!girl.world.isRemote && girl instanceof GoblinEntity) {
                  GoblinEntity goblin = (GoblinEntity)girl;
                  if (uuid.equals(goblin.getOwnerUUID())) {
                     String modelCode = goblin.getCustomModelCode();
                     String partCode = goblin.getCustomPartListCode();
                     existing = goblin;
                     existing.setOwnerUUID(null);
                     existing.setInteractionPlayerUUID(null);
                     existing.setCurrentAction(Action.NULL);
                     GoblinEntity newGoblin = new GoblinEntity(world);
                     newGoblin.dimension = dimension;
                     newGoblin.forceSpawn = true;
                     newGoblin.setCustomModelCode(modelCode);
                     newGoblin.setCustomPartListCode(partCode);
                     newGoblin.entityDataManager.set(GoblinEntity.aC, true);
                     world.spawnEntity(newGoblin);
                     newGoblin.setPositionAndUpdate(player.posX, player.posY, player.posZ);
                     newGoblin.setOwnerUUID(uuid);
                     newGoblin.setCurrentAction(Action.SHOULDER_IDLE);
                     break;
                  }
               }
            }
         } catch (ConcurrentModificationException ex) {
         }

         if (existing != null) {
            world.removeEntity(existing);
            BaseGirlEntity.getGirlEntityList().remove(existing);
         }
      }

      @SubscribeEvent
      public void onLivingAttack(LivingAttackEvent event) {
         if (event.getSource() != DamageSource.OUT_OF_WORLD) {
            EntityLivingBase living = event.getEntityLiving();
            if (living instanceof GoblinEntity) {
               GoblinEntity goblin = (GoblinEntity)living;
               if (goblin.getOwnerUUID() != null) {
                  event.setCanceled(true);
               }
            }
         }
      }

      @SubscribeEvent
      @SideOnly(Side.CLIENT)
      public void onKeyInput(KeyInputEvent event) {
         if (mcUnused == null) {
            mcUnused = Minecraft.getMinecraft();
         }

         if (!(mcUnused.currentScreen instanceof GalathScreen)) {
            if (ClientProxy.keyBindings[0].isPressed()) {
               BaseGirlEntity interactingGirl = null;
               UUID uuid = Minecraft.getMinecraft().player.getPersistentID();

               try {
                  for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
                     if (girl.world.isRemote && girl instanceof IGoblin) {
                        IGoblin goblin = (IGoblin)girl;
                        if (uuid.equals(goblin.getOwnerUUID())) {
                           interactingGirl = girl;
                           break;
                        }
                     }
                  }
               } catch (ConcurrentModificationException ex) {
               }

               if (interactingGirl != null) {
                  if (interactingGirl.getCurrentAction() == Action.SHOULDER_IDLE) {
                     Minecraft.getMinecraft().displayGuiScreen(new GalathScreen(interactingGirl));
                  }
               }
            }
         }
      }

   }
}
