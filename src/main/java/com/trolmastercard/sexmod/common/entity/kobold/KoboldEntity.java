package com.trolmastercard.sexmod.common.entity.kobold;

import com.trolmastercard.sexmod.client.gui.hud.HornyMeterHud;
import com.trolmastercard.sexmod.client.gui.screen.SceneFadeScreen;
import com.trolmastercard.sexmod.client.network.ModelPreviewWorldClient;
import com.trolmastercard.sexmod.common.util.math.VectorMath;
import com.trolmastercard.sexmod.network.kobold.TribeBlocksPacket;
import com.trolmastercard.sexmod.network.scene.ResetControllerPacket;
import com.trolmastercard.sexmod.network.scene.TeleportPlayerPacket;
import java.util.Map.Entry;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLog;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.items.IItemHandler;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.resource.GeckoLibCache;

import com.trolmastercard.sexmod.common.entity.base.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.common.entity.base.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.common.entity.base.Action;
import com.trolmastercard.sexmod.common.entity.base.BaseGirlEntity;
import com.trolmastercard.sexmod.common.entity.base.EyeAndKoboldColor;


import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.common.entity.kobold.KoboldNames;
import com.trolmastercard.sexmod.client.gui.screen.GirlInventoryScreen;
import com.trolmastercard.sexmod.client.gui.screen.TribeNameScreen;
import com.trolmastercard.sexmod.client.render.entity.KoboldRenderer;
import com.trolmastercard.sexmod.client.render.entity.WildSlimeFaceLayer;
import com.trolmastercard.sexmod.common.entity.ai.DoorInteractAiGoal;
import com.trolmastercard.sexmod.common.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.api.IEllie;
import com.trolmastercard.sexmod.api.IKobold;
import com.trolmastercard.sexmod.common.item.DragonStaffItem;
import com.trolmastercard.sexmod.network.kobold.GetTribeUiValuesPacket;
import com.trolmastercard.sexmod.network.PacketHandler;
import com.trolmastercard.sexmod.network.scene.SpawnParticlePacket;
import com.trolmastercard.sexmod.common.potion.HornyPotion;
import com.trolmastercard.sexmod.common.entity.kobold.KoboldManager;
import com.trolmastercard.sexmod.common.entity.kobold.KoboldTask;
import com.trolmastercard.sexmod.common.util.math.RotationHelper;
import com.trolmastercard.sexmod.registry.SoundHandler;
import com.trolmastercard.sexmod.common.world.WorldUtils;
import com.trolmastercard.sexmod.client.event.PlayerMovementController;
import com.trolmastercard.sexmod.common.util.math.Point2D;
import com.trolmastercard.sexmod.common.util.SceneDebugFlags;
import com.trolmastercard.sexmod.common.entity.kobold.TribeState;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.vecmath.Vector4d;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/**
 * Kobold NPC — the tribe creature with oral, anal and mating scenes, plus the
 * full tribe AI (tasks, mining, combat, breeding, follow modes).
 * <p>
 * <b>Tribe system:</b> kobolds belong to {@code KoboldManager.Tribe} objects
 * keyed by tribe UUID ({@code TRIBE_ID} data parameter). The tribe leader AI,
 * task assignment and saved positions are managed by
 * {@code KoboldManager}/{@code KoboldTask}.
 * <p>
 * <b>Scene entry</b> (shared with Jenny/Bia/Luna): client {@code doAction}
 * sets {@code animationFollowUp} (GIRL_HAND_STATES) via
 * {@code ChangeDataParameterPacket} and sends {@code KoboldStatePacket}; the
 * server calls {@code setDismounted()} ({@code sitLerpActive}), then
 * {@link #isSitting()} lerps her to {@code TARGET_POS} for ~40 ticks
 * ({@code sitLerpTicks} counter), anchors her and calls {@link #startActionFromHandState()} which dispatches
 * on GIRL_HAND_STATES: oral -> STARTBLOWJOB, anal -> KOBOLD_ANAL_START,
 * mating -> MATING_PRESS_START. Without the horny potion effect (or a
 * master match), the scene first passes through {@link Action#PAYMENT}
 * (the player pays).
 * <p>
 * <b>Pitfalls:</b>
 * <ul>
 *   <li>The dismount lerp in {@link #isSitting()} MUST use
 *       {@code RotationHelper.lerpVec3d(pos, target, 40 - sitLerpTicks)} (INT step
 *       variant) — the double variant flings the kobold and it vanishes.</li>
 *   <li>{@link #startActionFromHandState()} splits on the potion/master flags exactly as written —
 *       the payment gate is intentional, not a bug.</li>
 *   <li>Do not add {@code setDead}/removal logic to {@code onUpdate} —
 *       the girl hierarchy must never self-remove on benign tick errors.</li>
 * </ul>
 */
public class KoboldEntity extends AbstractNpcOnlyEntity implements IEllie, IInventory, IKobold {
   public static final EyeAndKoboldColor DEFAULT_EYE_COLOR = EyeAndKoboldColor.PURPLE;
   public static final float SCALE_0_25 = 0.25F;
   static final int ar = 20;
   static final int ag = 2;
   static final int aG = 30;
   static final int ah = 84;
   static final int a3 = 32;
   static final int a1 = 5;
   static final float ae = 1.5F;
   static final float aW = 20.0F;
   static final double au = 10.0;
   static final double ay = 2.0;
   static final double al = 3.0;
   static final int aQ = 300;
   static final int aq = 5;
   static final int aO = 100;
   static final int aB = 100;
   static final int ac = 2;
   static final float am = 2.0F;
   static final int aw = 300;
   static final float aj = 0.2F;
   static final double aH = 0.7;
   static final int aa = 142;
   public static final DataParameter<Float> SIZE_SCALAR = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.FLOAT)
      .getSerializer()
      .createKey(122);
   public static final DataParameter<String> KOBOLD_NAME = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(123);
   public static final DataParameter<Boolean> ARMED = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(124);
   public static final DataParameter<Boolean> TRIBE_MEMBER = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(125);
   public static final DataParameter<String> TRIBE_NAME = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(126);
   public static final DataParameter<Boolean> TRIBE_ALERTED = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(127);
   public static final DataParameter<Boolean> MINING_WITH_AXE = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(128);
   public static final DataParameter<Optional<UUID>> TRIBE_ID = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID)
      .getSerializer()
      .createKey(129);
   public static final int av = 24;
   public static double spawnHealth = 69.0;
   public static List<Vector4d> tribeMembers = new ArrayList<>();
   ItemStackHandler inventory = new ItemStackHandler(27);
   public String lastRenderedAction = null;
   boolean menuActionInFlight = false;
   int attackCooldown = 0;
   int animationTicks = 0;
   boolean sitLerpActive = false;
   int sitLerpTicks = 0;
   int healthRegenTicks = 0;
   float nearestDistance = Float.MAX_VALUE;
   static long lastTribeAlertTick = Long.MIN_VALUE;
   String[] TOXIC_LINES = new String[]{
      "What the fuck did you just fucking say about me, you little bitch? I'll have you know I graduated top of my class in the Navy Seals, and I've been involved in numerous secret raids on Al-Quaeda, and I have over 300 confirmed kills. I am trained in gorilla warfare and I'm the top sniper in the entire US armed forces. You are nothing to me but just another target. I will wipe you the fuck out with precision the likes of which has never been seen before on this Earth, mark my fucking words. You think you can get away with saying that shit to me over the Internet? Think again, fucker. As we speak I am contacting my secret network of spies across the USA and your IP is being traced right now so you better prepare for the storm, maggot. The storm that wipes out the pathetic little thing you call your life. You're fucking dead, kid. I can be anywhere, anytime, and I can kill you in over seven hundred ways, and that's just with my bare hands. Not only am I extensively trained in unarmed combat, but I have access to the entire arsenal of the United States Marine Corps and I will use it to its full extent to wipe your miserable ass off the face of the continent, you little shit. If only you could have known what unholy retribution your little \"clever\" comment was about to bring down upon you, maybe you would have held your fucking tongue. But you couldn't, you didn't, and now you're paying the price, you goddamn idiot. I will shit fury all over you and you will drown in it. You're fucking dead, kiddo.",
      "suck my iron cock you worthless piece of shit!",
      "you'll die a fucking virgin!",
      "not even Johnny sins would wanna stick his cock up ur ass",
      "fuck you with ur borderline illegal fetishes!",
      "ur cum tastes terrible!",
      "I've always faked my orgasms when having sex with you!",
      "Not even Jenny would fuck you for 6 diamonds!",
      "U look like u'd use a shovel to mine diamonds, fucking idiot!",
      "Why tf does ur cock smell like my asshole???",
      "do all of us a favor and hit [ALT]+[F4]!",
      "I'm about to say the N word!",
      "you are under attack retard",
      "Eat my ass!",
      "my tongue is longer than ur fucking dick bitch!",
      "Ligma titties!",
      "touch some grass bitch!"
   };
   IBlockState blockBelowState = null;
   IBlockState savedHomeBlockState = null;
   BlockPos homeApproachPos = null;
   boolean tribeTasksInitialized = true;
   Vec3d lastStuckCheckPos = Vec3d.ZERO;
   BlockPos tribeWanderTarget = null;
   BlockPos miningTargetPos = null;
   int miningCooldown = 0;
   int taskTimer = 0;
   int miningStandTicks = 0;
   int inventoryNotifyCooldown = 0;
   boolean tribeBlocked = false;
   BlockPos wanderTarget = null;
   int wanderTicks = 0;
   int mineCooldown = 24;
   int cooldownTicks = 0;
   ItemStack miningDropItem = null;
   public boolean editedColorManually = false;
   int actionCooldown = -1;
   boolean WildSlimeFaceLayer = true;
   boolean switchVariant = false;
   public boolean isRenderEgg = false;
   int moanCooldown = 0;

   public KoboldEntity(World world) {
      super(world);
      this.setSize(0.5F, 0.99F);
   }

   KoboldEntity(World world, UUID tribeId, float size) {
      this(world);
      this.entityDataManager.set(TRIBE_ID, Optional.of(tribeId));
      this.entityDataManager.set(SIZE_SCALAR, size);
   }

   public static KoboldEntity createKobold(World world, UUID tribeId) {
      float throwDelay = getRandomThrowDelay();
      return createKoboldWithSpeed(world, tribeId, throwDelay);
   }

   public static KoboldEntity createKoboldWithSpeed(World world, UUID tribeId, float throwDelay) {
      spawnHealth = 10.0 - throwDelay * 25.0;
      return new KoboldEntity(world, tribeId, throwDelay);
   }

   @Override
   protected String buildModelCodeDNA(StringBuilder builder) {
      return this.buildModelCodeDNAImpl(builder);
   }

   @Override
   public ArrayList<Integer> getCustomPartIdList() {
      return this.getCustomPartIdListImpl();
   }

   @Override
   public ArrayList<Integer> getBasePartIdList() {
      return this.getBasePartIdListImpl();
   }

   @Override
   public void setCustomPartList(List<Integer> parts) {
      this.setCustomPartListImpl(parts);
   }

   void updateModelCodeDNA() {
      this.updateModelCodeDNAImpl();
   }

   @Override
   public Point2D getModelPartByIndex(int index) {
      return this.getModelPartByIndexImpl(index);
   }

   @Override
   public String getDisplayNameText() {
      return (String)this.entityDataManager.get(KOBOLD_NAME);
   }

   @Override
   public float getScaleFactor() {
      return 0.2F - (0.25F - (Float)this.entityDataManager.get(SIZE_SCALAR));
   }

   public float getEyeHeight() {
      return 0.94F;
   }

   public static float getRandomThrowDelay() {
      return (float)(Math.random() * 0.25);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      EyeAndKoboldColor color = EyeAndKoboldColor.values()[this.getRNG().nextInt(EyeAndKoboldColor.values().length)];
      this.entityDataManager.register(ACTION_TARGET_POS, new BlockPos(color.getMainColor()));
      this.entityDataManager.register(CURRENT_ACTION, DEFAULT_EYE_COLOR.name());
      this.entityDataManager.register(TRIBE_ID, Optional.absent());
      this.entityDataManager.register(SIZE_SCALAR, 0.0F);
      this.entityDataManager.register(KOBOLD_NAME, KoboldNames.values()[this.getRNG().nextInt(KoboldNames.values().length)].toString());
      this.entityDataManager.register(ARMED, false);
      this.entityDataManager.register(TRIBE_MEMBER, false);
      this.entityDataManager.register(TRIBE_NAME, "null");
      this.entityDataManager.register(TRIBE_ALERTED, false);
      this.entityDataManager.register(MINING_WITH_AXE, false);
   }

   @Override
   protected void initEntityAI() {
      this.watchClosestGirlGoal = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAITempt(this, 0.4, false, new HashSet<>(TEMPTATION_ITEMS)));
      this.tasks.addTask(3, new DoorInteractAiGoal(this));
      this.tasks.addTask(5, this.watchClosestGirlGoal);
   }

   protected float getJumpUpwardsMotion() {
      return 0.45F;
   }

   @Override
   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(spawnHealth);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(30.0);
   }

   @Override
   public boolean canBePushed() {
      return true;
   }

   protected boolean processInteract(EntityPlayer player, EnumHand hand) {
      if (this.getInteractionPlayerUUID() != null) {
         return false;
      }

      ItemStack nameTagStack = player.getHeldItem(EnumHand.MAIN_HAND);
      if (!nameTagStack.getItem().equals(Items.NAME_TAG)) {
         nameTagStack = player.getHeldItem(EnumHand.OFF_HAND);
      }

      if (nameTagStack.getItem().equals(Items.NAME_TAG) && player.getPersistentID().toString().equals(this.entityDataManager.get(MASTER))) {
         this.entityDataManager.set(KOBOLD_NAME, nameTagStack.getDisplayName());
         nameTagStack.shrink(1);
         return true;
      }

      if ((Boolean)this.entityDataManager.get(ARMED)) {
         return false;
      }

      if (this.getCurrentAction() == Action.SLEEP) {
         return false;
      }

      ItemStack staffStack = player.getHeldItem(EnumHand.MAIN_HAND);
      if (staffStack.getItem() != DragonStaffItem.DRAGON_STAFF) {
         staffStack = player.getHeldItem(EnumHand.OFF_HAND);
      }

      if (!this.hasMaster() && staffStack.getItem() == DragonStaffItem.DRAGON_STAFF) {
         if (!this.world.isRemote) {
            return true;
         }

         Optional tribeIdOpt = (Optional)this.entityDataManager.get(TRIBE_ID);
         if (!tribeIdOpt.isPresent()) {
            return true;
         }

         if (!tribeMembers.isEmpty()) {
            return true;
         }

         this.openTribeNameScreen((UUID)tribeIdOpt.get());
         return true;
      } else {
         if (this.hasMaster() && staffStack.getItem() == DragonStaffItem.DRAGON_STAFF && ((String)this.entityDataManager.get(MASTER)).equals(player.getPersistentID().toString())) {
            // jar-faithful: the mod instance is Main.instance — a deobf regression
            // passed null here and crashed NetworkRegistry.getLocalGuiContainer.
            player.openGui(
               Main.instance, 1, this.world, this.getPosition().getX(), this.getPosition().getY(), this.getPosition().getZ()
            );
            return true;
         }

         if (this.world.isRemote) {
            if (this.hasMaster() && ((String)this.entityDataManager.get(MASTER)).equals(player.getPersistentID().toString())) {
               this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MASTER);
            }

            this.openInteractionMenu(player);
         } else {
            this.setInteractionPlayerUUID(player.getPersistentID());
            this.getNavigator().clearPath();
            this.setYawRotation((float)(Math.atan2(this.posZ - player.posZ, this.posX - player.posX) * (180.0 / Math.PI) + 90.0));
            this.setTargetPosition(new Vec3d(this.posX, Math.floor(this.posY), this.posZ));
            this.entityDataManager.set(IS_ANCHORED, true);
            this.setCurrentAction(Action.NULL);
         }

         return true;
      }
   }

   @SideOnly(Side.CLIENT)
   void openTribeNameScreen(UUID tribeId) {
      Minecraft.getMinecraft().displayGuiScreen(new TribeNameScreen(tribeId));
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean openInteractionMenu(EntityPlayer player) {
      if (this.hasMaster() && player.getPersistentID().toString().equals(this.entityDataManager.get(MASTER))) {
         Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(this, player, new String[]{"anal", "oral", "mating"}, null, false));
         return true;
      } else if (this.getActivePotionEffect(HornyPotion.HORNY_POTION) != null) {
         Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(this, player, new String[]{"anal", "oral"}, null, false));
         return true;
      } else {
         Minecraft.getMinecraft()
            .displayGuiScreen(
               new GirlInventoryScreen(
                  this, player, new String[]{"anal", "oral"}, new ItemStack[]{new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.IRON_PICKAXE)}, false
               )
            );
         return true;
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void onInteractionMenuClosed() {
      if (this.menuActionInFlight) {
         this.menuActionInFlight = false;
      } else {
         this.setInteractionPlayerUUID(null);
         this.changeDataParameterFromClient("shouldbeattargetpos", "false");
      }
   }

   @Override
   public void resetCameraAndPhysics() {
      this.isRenderEgg = false;
      super.resetCameraAndPhysics();
   }

   protected void triggerActionSync(boolean flag, UUID uuid) {
      super.triggerActionSync(flag, true, uuid);
      PlayerMovementController.setMovementLock(false);
   }

   @Override
   public void doAction(String action, UUID uuid) {
      this.menuActionInFlight = true;
      if ("oral".equals(action)) {
         this.changeDataParameterFromClient("animationFollowUp", Action.STARTBLOWJOB.toString());
         this.triggerActionSync(true, uuid);
      }

      if ("anal".equals(action)) {
         this.changeDataParameterFromClient("animationFollowUp", Action.KOBOLD_ANAL_START.toString());
         this.triggerActionSync(true, uuid);
      }

      if ("mating".equals(action)) {
         this.changeDataParameterFromClient("animationFollowUp", Action.MATING_PRESS_START.toString());
         this.triggerActionSync(true, uuid);
      }
   }

   @Override
   public void setDismounted() {
      this.sitLerpActive = true;
      this.entityDataManager.set(IS_ANCHORED, false);
   }

   @Override
   protected void clearBoneColors() {
      KoboldRenderer.clearBoneColors();
   }

   boolean isSitting() {
      if (!this.sitLerpActive) {
         return false;
      }

      this.sitLerpTicks++;
      this.noClip = false;
      this.setNoGravity(false);
      if (this.sitLerpTicks > 40) {
         SceneDebugFlags.log(SceneDebugFlags.SITTING, "Kobold.isSitting: lerp done, startActionFromHandState (sitLerpTicks=%d, action=%s, handState=%s)", this.sitLerpTicks, this.getCurrentAction(), this.entityDataManager.get(GIRL_HAND_STATES));
         this.sitLerpActive = false;
         this.sitLerpTicks = 0;
         EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
         if (player == null) {
            this.sitLerpActive = false;
            return true;
         }

         this.setYawRotation(player.rotationYaw + 180.0F);
         this.entityDataManager.set(IS_ANCHORED, true);
         player.noClip = true;
         player.setNoGravity(true);
         this.noClip = true;
         this.setNoGravity(true);
         this.getNavigator().clearPath();
         this.startActionFromHandState();
         return true;
      }

      this.rotationYaw = this.getYawRotation();
      this.setNoGravity(false);
      Vec3d pos = RotationHelper.lerpVec3d(this.getPositionVector(), this.getTargetPosition(), 40 - this.sitLerpTicks);
      this.setPosition(pos.x, pos.y, pos.z);
      this.setCurrentAction(Action.NULL);
      Optional tribeIdOpt = (Optional)this.entityDataManager.get(TRIBE_ID);
      if (!tribeIdOpt.isPresent()) {
         return true;
      }

      Collection tasks = KoboldManager.getTribeTasks((UUID)tribeIdOpt.get());
      if (tasks == null) {
         return true;
      }

      for (KoboldTask task : (java.util.Collection<KoboldTask>) (tasks) ) {
         task.addWorker(this);
      }

      return true;
   }

   void handleActionCooldown(UUID tribeId) {
      KoboldTribeState.handleActionCooldown(this, tribeId);
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      this.tribeBlocked = false;
      Optional tribeIdOpt = (Optional)this.entityDataManager.get(TRIBE_ID);
      if (tribeIdOpt.isPresent()) {
         this.handleActionCooldown((UUID)tribeIdOpt.get());
         KoboldManager.triggerFastSexAction((UUID)tribeIdOpt.get());
         EntityPlayer master = this.getMasterPlayer();
         if (master != null) {
            KoboldManager.assignMaster((UUID)tribeIdOpt.get(), master.getPersistentID());
         }
      }

      if (!this.isSitting()) {
         if (this.getInteractionPlayerUUID() == null) {
            if (!(Boolean)this.entityDataManager.get(ARMED)) {
               if (this.getHealth() != this.getMaxHealth() && ++this.healthRegenTicks >= 100) {
                  this.setHealth(this.getHealth() + 2.0F);
                  this.healthRegenTicks = 0;
                  PacketHandler.networkWrapper.sendToAllTracking(new SpawnParticlePacket(this.getGirlId(), EnumParticleTypes.HEART.getParticleName()), this);
               }
            } else {
               this.healthRegenTicks = 0;
            }

            if (!(Boolean)this.entityDataManager.get(IS_ANCHORED)) {
               this.setNoGravity(false);
            }

            if (tribeIdOpt.isPresent()) {
               this.attackCooldown--;
               if (this.getCurrentAction() == Action.ATTACK) {
                  this.getNavigator().clearPath();
                  this.rotationYaw = this.getYawRotation();
                  this.rotationYawHead = this.getYawRotation();
                  this.animationTicks++;
                  if (22 == this.animationTicks) {
                     this.onTickEmpty();
                  }

                  if (32 == this.animationTicks) {
                     HashSet targets = KoboldManager.getTribeTargets((UUID)tribeIdOpt.get());
                     HashSet toRemove = new HashSet();

                     for (EntityLivingBase target : (java.util.Collection<EntityLivingBase>) (targets) ) {
                        if (!(target.getDistance(this) > 2.0F)) {
                           target.attackEntityFrom(DamageSource.causeMobDamage(this), 5.0F);
                           if (target.isDead) {
                              toRemove.add(target);
                           }
                        }
                     }

                     for (EntityLivingBase target : (java.util.Collection<EntityLivingBase>) (toRemove) ) {
                        KoboldManager.removeCombatant((UUID)tribeIdOpt.get(), target);
                     }
                  }

                  if (84 <= this.animationTicks) {
                     this.setCurrentAction(Action.NULL);
                     this.entityDataManager.set(IS_ANCHORED, false);
                     this.animationTicks = 0;
                  }
               } else {
                  this.entityDataManager.set(ARMED, this.handleTribeCombat((UUID)tribeIdOpt.get(), false));
                  this.entityDataManager.set(TRIBE_MEMBER, KoboldManager.isTribeMember((UUID)tribeIdOpt.get(), this));
                  this.entityDataManager.set(TRIBE_ALERTED, KoboldManager.isTribeAlerted((UUID)tribeIdOpt.get()));
                  this.handleMasterPresence();
                  this.handleModelSync();
                  this.watchClosestGirlGoal.isWatching = this.isIdle();
               }
            }
         }
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      this.handleTribeState();
      this.handleInteraction();
      this.handleIdleState();
      this.handleSleepState();
      this.updateModelCodeDNA();
   }

   void handleSleepState() {
      if (this.world.isRemote) {
         if (this.world.getTotalWorldTime() - 300L >= lastTribeAlertTick) {
            if (this.hasMaster()) {
               if (this.getCurrentAction() == Action.NULL) {
                  if ("".equals(this.entityDataManager.get(GIRL_HAND_STATES))) {
                     if (!(Boolean)this.entityDataManager.get(TRIBE_ALERTED)) {
                        String masterUuid = (String)this.entityDataManager.get(MASTER);
                        EntityPlayer player = this.world.getClosestPlayerToEntity(this, 10.0);
                        if (player == null) {
                           this.nearestDistance = Float.MAX_VALUE;
                        } else if (player.getPersistentID().toString().equals(masterUuid)) {
                           float dist = this.getDistance(player);
                           if (dist < 2.0F && this.nearestDistance > 2.0F) {
                              this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_KOBOLD_HEYMASTER));
                              this.sendChatMessage("Hey master!");
                              lastTribeAlertTick = this.world.getTotalWorldTime();
                           }

                           this.nearestDistance = dist;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   void handleIdleState() {
      if (this.world.isRemote) {
         if (this.getCurrentAction() != Action.SLEEP) {
            if ((Boolean)this.entityDataManager.get(TRIBE_ALERTED)) {
               if (this.hasMaster()) {
                  EntityPlayer player = this.world.getPlayerEntityByUUID(UUID.fromString((String)this.entityDataManager.get(MASTER)));
                  if (player != null) {
                     this.handleKoboldOwner(player);
                  }
               }
            }
         }
      }
   }

   void handleTribeState() {
      KoboldTribeState.handleTribeState(this);
   }

   @Override
   protected void startActionFromHandState() {
      String handState = (String)this.entityDataManager.get(BaseGirlEntity.GIRL_HAND_STATES);
      SceneDebugFlags.log(SceneDebugFlags.SCENE_ENTRY, "Kobold.startActionFromHandState handState=%s action=%s remote=%s potion=%s master=%s", handState, this.getCurrentAction(), this.world.isRemote, this.getActivePotionEffect(HornyPotion.HORNY_POTION) != null, this.hasMaster());
      boolean hasHornyPotion = this.getActivePotionEffect(HornyPotion.HORNY_POTION) != null;
      boolean isMasterNear = false;
      if (this.hasMaster()) {
         isMasterNear = ((String)this.entityDataManager.get(MASTER)).equals(this.getInteractionPlayerUUID().toString());
      }

      if (!hasHornyPotion && !isMasterNear) {
         if (handState.equals(Action.STARTBLOWJOB.toString())) {
            if (this.getCurrentAction() == Action.PAYMENT) {
               this.setCurrentAction(Action.STARTBLOWJOB);
            } else {
               this.setCurrentAction(Action.PAYMENT);
            }
         }

         if (handState.equals(Action.KOBOLD_ANAL_START.toString())) {
            if (this.getCurrentAction() == Action.PAYMENT) {
               this.setCurrentAction(Action.KOBOLD_ANAL_START);
            } else {
               this.setCurrentAction(Action.PAYMENT);
            }
         }

         if (handState.equals(Action.MATING_PRESS_START.toString())) {
            this.setCurrentAction(Action.MATING_PRESS_START);
         }
      } else {
         if (handState.equals(Action.STARTBLOWJOB.toString())) {
            this.setCurrentAction(Action.STARTBLOWJOB);
         }

         if (handState.equals(Action.KOBOLD_ANAL_START.toString())) {
            this.setCurrentAction(Action.KOBOLD_ANAL_START);
         }

         if (handState.equals(Action.MATING_PRESS_START.toString())) {
            this.setCurrentAction(Action.MATING_PRESS_START);
         }
      }
   }

   void handleInteraction() {
      if (this.world.isRemote) {
         UUID uuid = this.getInteractionPlayerUUID();
         if (uuid != null) {
            if ((Boolean)this.entityDataManager.get(IS_ANCHORED)) {
               if (this.getCurrentAction() == Action.NULL) {
                  EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
                  if (player != null) {
                     this.handleKoboldOwner(player);
                  }
               }
            }
         }
      }
   }

   void handleKoboldOwner(EntityPlayer player) {
      AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player.getPersistentID());
      Vec3d headPos = new Vec3d(player.posX, player.posY + (playerGirl == null ? player.eyeHeight : playerGirl.getEyeHeight()), player.posZ);
      Vec3d eyePos = new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
      double dist = eyePos.distanceTo(headPos);
      double heightDiff = headPos.y - eyePos.y;
      this.rotationPitch = (float)(-(Math.sin(heightDiff / dist) * (180.0 / Math.PI)));
   }

   void onTickEmpty() {
   }

   boolean isIdle() {
      if (this.getCurrentAction() != Action.NULL) {
         return false;
      } else {
         return Math.abs(this.motionX) + Math.abs(this.motionZ) > 0.01 ? false : !this.isBlockedByCeiling();
      }
   }

   void handleMasterPresence() {
      KoboldTribeState.handleMasterPresence(this);
   }

   protected double getKickDistance(EntityPlayer player, double dist) {
      return KoboldTribeState.getKickDistance(this, player, dist);
   }

   void teleportToHome(UUID tribeId) {
      KoboldTribeState.teleportToHome(this, tribeId);
   }

   void handleTribeRequest(UUID tribeId) {
      KoboldTribeState.handleTribeRequest(this, tribeId);
   }

   void handleTaskAssign(UUID tribeId) {
      KoboldTribeState.handleTaskAssign(this, tribeId);
   }

   void handleMemberSync(UUID tribeId) {
      KoboldTribeState.handleMemberSync(this, tribeId);
   }

   void handleTaskRequest(UUID tribeId) {
      KoboldTribeState.handleTaskRequest(this, tribeId);
   }

   void handleBedRequest(UUID tribeId) {
      KoboldTribeState.handleBedRequest(this, tribeId);
   }

   void handleHomeRequest(UUID tribeId) {
      KoboldTribeState.handleHomeRequest(this, tribeId);
   }

   void handleHomeRelease(UUID tribeId) {
      KoboldTribeState.handleHomeRelease(this, tribeId);
   }

   void handleTribeTasks(UUID tribeId, Collection<KoboldTask> tasks) {
      KoboldTribeState.handleTribeTasks(this, tribeId, tasks);
   }

   void handleTribeTasksInit(UUID tribeId, Collection<KoboldTask> tasks) {
      KoboldTribeState.handleTribeTasksInit(this, tribeId, tasks);
   }

   protected void handlePlayerDismount(EntityPlayer player) {
      KoboldTribeState.handlePlayerDismount(this, player);
   }

   BlockPos getTribeHomePos(UUID tribeId) {
      return KoboldTribeState.getTribeHomePos(this, tribeId);
   }

   BlockPos findStandPos(BlockPos pos) {
      BlockPos standPos = this.getPosition();
      BlockPos delta = pos.subtract(standPos);
      if (Math.abs(delta.getX()) + Math.abs(delta.getZ()) < 20) {
         return pos;
      }

      double minAxis = Math.min(Math.abs(delta.getX()), Math.abs(delta.getZ()));
      double maxAxis = Math.max(Math.abs(delta.getX()), Math.abs(delta.getZ()));
      double ratio = minAxis / (maxAxis + minAxis);
      int xOffset = (int)((delta.getX() > 0 ? 1 : -1) * 20 * (minAxis == Math.abs(delta.getX()) ? ratio : 1.0 - ratio));
      int zOffset = (int)((delta.getZ() > 0 ? 1 : -1) * 20 * (minAxis == Math.abs(delta.getZ()) ? ratio : 1.0 - ratio));
      BlockPos candidate = this.getPosition().add(xOffset, 0, zOffset);
      return new BlockPos(candidate.getX(), WorldUtils.getHeightAt(this.world, candidate.getX(), candidate.getZ()) + 1, candidate.getZ());
   }

   void checkTribeHome(UUID tribeId) {
      KoboldTribeState.checkTribeHome(this, tribeId);
   }

   void findConnectedLogs(UUID tribeId, Collection<KoboldTask> tasks) {
      KoboldTribeState.findConnectedLogs(this, tribeId, tasks);
   }

   TribeState getTribeStateForTime() {
      return KoboldTribeState.getTribeStateForTime(this);
   }

   boolean isTribeTaskDone(UUID tribeId) {
      return KoboldTribeState.isTribeTaskDone(this, tribeId);
   }

   boolean handleTribeCombat(UUID tribeId, boolean isLeader) {
      return KoboldTribeState.handleTribeCombat(this, tribeId, isLeader);
   }

   void handleTribeJoin(UUID tribeId) {
      KoboldTribeState.handleTribeJoin(this, tribeId);
   }

   void handleHomeTeleport(UUID tribeId) {
      KoboldTribeState.handleHomeTeleport(this, tribeId);
   }

   void handleTaskFollow(UUID tribeId) {
      KoboldTribeState.handleTaskFollow(this, tribeId);
   }

   void syncTribeBlocks(BlockPos pos) {
      PacketHandler.networkWrapper
         .sendToAllTracking(
            new SpawnParticlePacket(this.getGirlId(), EnumParticleTypes.PORTAL.getParticleName(), 30),
            new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 30.0)
         );
      this.setPosition(0.5F + pos.getX(), pos.getY(), 0.5F + pos.getZ());
      PacketHandler.networkWrapper
         .sendToAllTracking(
            new SpawnParticlePacket(this.getGirlId(), EnumParticleTypes.PORTAL.getParticleName(), 30),
            new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 30.0)
         );
   }

   void handleTaskState(UUID tribeId, KoboldTask task) {
      this.handleTaskStateImpl(tribeId, task);
   }

   void handleTaskNavigation(UUID tribeId, KoboldTask task) {
      this.handleTaskNavigationImpl(tribeId, task);
   }

   BlockPos executeMiningTask(KoboldTask task, UUID tribeId) {
      return this.executeMiningTaskImpl(task, tribeId);
   }

   @Nullable
   BlockPos findTaskBlock(List<BlockPos> blocks, int index, EnumFacing facing, BlockPos targetPos) {
      return this.findTaskBlockImpl(blocks, index, facing, targetPos);
   }

   void handleNearbyPlayerTick(UUID tribeId) {
      if (!this.canStoreInventory(tribeId, false)) {
         this.handleNearbyPlayer();
      }
   }

   void handleNearbyPlayer() {
      EntityPlayer player = this.world.getClosestPlayerToEntity(this, 15.0);
      if (this.hasMaster() && player != null && player.getDistance(this) < 2.0F && ((String)this.entityDataManager.get(MASTER)).equals(player.getPersistentID().toString())) {
         this.getNavigator().clearPath();
      } else {
         if (this.wanderTarget == null
            || this.getDistance(this.wanderTarget.getX(), this.wanderTarget.getY(), this.wanderTarget.getZ()) > this.getWanderRange()
            || this.wanderTicks > 100) {
            int xOffset = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(5);
            int zOffset = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(5);
            int height = WorldUtils.getHeightAt(this.world, this.getPosition().getX() + xOffset, this.getPosition().getZ() + zOffset);
            this.wanderTarget = new BlockPos(this.getPosition().getX() + xOffset, height, this.getPosition().getZ() + zOffset);
            this.wanderTicks = 0;
         }

         if (Math.sqrt(this.wanderTarget.distanceSq(this.getPosition())) > 2.0) {
            this.getNavigator().tryMoveToXYZ(this.wanderTarget.getX(), this.wanderTarget.getY(), this.wanderTarget.getZ(), 0.35F);
            this.tickPathVelocity();
         } else {
            this.wanderTicks++;
         }
      }
   }

   double getWanderRange() {
      return Math.sqrt(800.0);
   }

   boolean canStoreInventory(UUID tribeId, boolean checkOpen) {
      return this.canStoreInventoryImpl(tribeId, checkOpen);
   }

   boolean isTribeChestOpen(UUID tribeId, boolean checkOpen) {
      return this.isTribeChestOpenImpl(tribeId, checkOpen);
   }

   boolean assignTaskToKobold(UUID tribeId, KoboldTask task) {
      return KoboldTribeState.assignTaskToKobold(this, tribeId, task);
   }

   void navigateToTask(UUID tribeId, KoboldTask task, BlockPos pos) {
      this.navigateToTaskImpl(tribeId, task, pos);
   }

   void startMiningTask(UUID tribeId, BlockPos pos, KoboldTask task) {
      this.startMiningTaskImpl(tribeId, pos, task);
   }

   ItemStack getBlockItem(BlockPos pos) {
      return this.getBlockItemImpl(pos);
   }

   void mineBlockAt(BlockPos pos, UUID tribeId) {
      this.mineBlockAtImpl(pos, tribeId);
   }

   void handleModelSync() {
      KoboldTribeState.handleModelSync(this);
   }

   @Override
   public void setCurrentAction(Action action) {
      if (this.getCurrentAction() != Action.MATING_PRESS_CUM || action != Action.MATING_PRESS_SOFT && action != Action.MATING_PRESS_HARD) {
         if (this.getCurrentAction() != Action.KOBOLD_ANAL_CUM || action != Action.KOBOLD_ANAL_SLOW && action != Action.KOBOLD_ANAL_FAST) {
            if (this.getCurrentAction() != Action.CUMBLOWJOB || action != Action.SUCKBLOWJOB && action != Action.THRUSTBLOWJOB) {
               if (action == Action.MATING_PRESS_CUM) {
                  this.actionCooldown = 0;
               }

               super.setCurrentAction(action);
            }
         }
      }
   }

   public void onDeath(DamageSource source) {
      super.onDeath(source);
      if (!this.world.isRemote) {
         Optional tribeOpt = (Optional)this.entityDataManager.get(TRIBE_ID);
         if (tribeOpt.isPresent()) {
            UUID tribeId = (UUID)tribeOpt.get();
KoboldManager.setTribeLeader(tribeId, this);
            if (this.hasMaster()) {
               EntityPlayer master = this.world.getPlayerEntityByUUID(UUID.fromString((String)this.getDataManager().get(MASTER)));
               if (master != null) {
                  master.sendMessage(
                     new TextComponentString(
                        String.format("%s%s%s has perished %suwu", TextFormatting.RED, this.getDisplayNameText(), TextFormatting.WHITE, TextFormatting.RED)
                     )
                  );
               }
            }
         }
      }
   }

   @Override
   protected Action getNextAction(Action action) {
      if (action == Action.SUCKBLOWJOB_BLINK) {
         return Action.THRUSTBLOWJOB;
      } else {
         return action == Action.KOBOLD_ANAL_SLOW ? Action.KOBOLD_ANAL_FAST : null;
      }
   }

   @Override
   protected Action getCumAction(Action action) {
      if (action == Action.THRUSTBLOWJOB || action == Action.SUCKBLOWJOB_BLINK) {
         return Action.CUMBLOWJOB;
      } else if (action == Action.KOBOLD_ANAL_SLOW || action == Action.KOBOLD_ANAL_FAST) {
         return Action.KOBOLD_ANAL_CUM;
      } else {
         return action != Action.MATING_PRESS_HARD && action != Action.MATING_PRESS_SOFT ? null : Action.MATING_PRESS_CUM;
      }
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      this.write(nbt);
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      this.read(nbt);
   }

   @Override
   public boolean isBlockedByCeiling() {
      if (this.isLocallyRegistered()) {
         return false;
      }

      Block block = this.world.getBlockState(this.getPosition().add(0, 1, 0)).getBlock();
      return !block.isPassable(this.world, this.getPosition().add(0, 1, 0));
   }

   boolean hasInventoryItems() {
      return this.hasInventoryItemsImpl();
   }

   boolean canAssignTask(KoboldTask task) {
      return this.canAssignTaskImpl(task);
   }

   boolean canInsertItem(ItemStack stack) {
      return this.canInsertItemImpl(stack);
   }

   boolean canStoreItems(List<ItemStack> stacks) {
      return this.canStoreItemsImpl(stacks);
   }

   boolean canExtractItem(ItemStack stack) {
      return this.canExtractItemImpl(stack);
   }

   boolean canInsertItemStack(ItemStackHandler handler, ItemStack stack, boolean simulate, boolean extract) {
      return this.canInsertItemStackImpl(handler, stack, simulate, extract);
   }

   @Override
   public void playSoundAtVolume(SoundEvent sound, float volume) {
      float shrink = 0.25F - (Float)this.entityDataManager.get(SIZE_SCALAR);
      double progress = shrink / 0.25F;
      float pitch = (float)RotationHelper.lerpDouble(0.9F, 1.1F, progress);
      this.playSoundAtPosition(sound, volume, pitch);
   }

   @Override
   public void playSound(SoundEvent sound) {
      this.playSoundAtVolume(sound, 1.0F);
   }

   void playRandomSounds(SoundEvent[] sounds) {
      this.playRandomSound(sounds, 1.0F);
   }

   void playRandomSound(SoundEvent[] sounds, float volume) {
      this.playSoundAtVolume(sounds[this.getRNG().nextInt(sounds.length)], volume);
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      return this.animationPredicateImpl(event);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData data) {
      this.registerControllersImpl(data);
   }

   public int getSizeInventory() {
      return 27;
   }

   public boolean isEmpty() {
      return false;
   }

   public ItemStack getStackInSlot(int slot) {
      return slot >= this.inventory.getSlots() ? ItemStack.EMPTY : this.inventory.getStackInSlot(slot);
   }

   public ItemStack decrStackSize(int slot, int amount) {
      return this.inventory.extractItem(slot, amount, false);
   }

   public ItemStack removeStackFromSlot(int slot) {
      return this.inventory.extractItem(slot, this.inventory.getStackInSlot(slot).getCount(), false);
   }

   public void setInventorySlotContents(int slot, ItemStack stack) {
      this.inventory.setStackInSlot(slot, stack);
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public void markDirty() {
   }

   public boolean isUsableByPlayer(EntityPlayer player) {
      return true;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean isItemValidForSlot(int slot, ItemStack stack) {
      return true;
   }

   public int getField(int id) {
      return id;
   }

   public void setField(int id, int value) {
   }

   public int getFieldCount() {
      return 0;
   }

   public void clear() {
   }

   public static class KoboldTribeEvents {
      int tickCounter = 0;

      @SubscribeEvent
      public void onLivingDeath(LivingDeathEvent event) {
         if (event.getEntityLiving() instanceof KoboldEntity) {
            KoboldEntity kobold = (KoboldEntity)event.getEntityLiving();
            if (kobold.world.isRemote) {
               return;
            }

            for (int slot = 0; slot < kobold.inventory.getSlots(); slot++) {
               ItemStack stack = kobold.inventory.getStackInSlot(slot);
               if (stack.getItem() != Items.AIR) {
                  kobold.dropItem(stack.getItem(), stack.getCount());
               }
            }
         }
      }

      @SubscribeEvent
      public void onLivingHurtPlayer(LivingHurtEvent event) {
         Entity entity = event.getEntity();
         World world = entity.getEntityWorld();
         if (!world.isRemote) {
            if (entity instanceof KoboldEntity) {
               KoboldEntity kobold = (KoboldEntity)entity;
               Optional tribeOpt = (Optional)kobold.getDataManager().get(KoboldEntity.TRIBE_ID);
               if (tribeOpt.isPresent()) {
                  Entity attacker = event.getSource().getTrueSource();
                  if (attacker != null) {
                     if (attacker instanceof EntityLivingBase) {
                        if (attacker instanceof EntityPlayer) {
                           EntityPlayer player = (EntityPlayer)attacker;
                           if (player.capabilities.isCreativeMode) {
                              return;
                           }

                           if (player.equals(kobold.getMasterPlayer())) {
                              return;
                           }
                        }

                        EntityPlayer master = kobold.getMasterPlayer();
                        if (master != null) {
                           master.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Your Tribe is under Attack!"), true);
                        }

                        KoboldManager.addCombatant((UUID)tribeOpt.get(), (EntityLivingBase)attacker);
                     }
                  }
               }
            }
         }
      }

      @SubscribeEvent
      public void onWorldUnload(Unload event) {
         try {
            for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
               if (girl instanceof KoboldEntity) {
                  KoboldEntity kobold = (KoboldEntity)girl;
                  Optional tribeOpt = (Optional)kobold.getDataManager().get(KoboldEntity.TRIBE_ID);
                  if (tribeOpt.isPresent() && KoboldManager.isTribeMember((UUID)tribeOpt.get(), kobold)) {
                     kobold.teleportToHome((UUID)tribeOpt.get());
                  }
               }
            }
         } catch (ConcurrentModificationException ex) {
            // ignored: entity list mutated during iteration; retried on the next tick
         }
      }

      @SubscribeEvent
      public void onLivingHurtCancel(LivingHurtEvent event) {
         if (event.getSource() == DamageSource.IN_WALL) {
            Entity entity = event.getEntity();
            if (entity instanceof KoboldEntity) {
               entity.setPosition(entity.posX, entity.posY + 1.0, entity.posZ);
               event.setCanceled(true);
            }
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onClientTick(ClientTickEvent event) {
         WorldClient world = Minecraft.getMinecraft().world;
         if (world != null) {
            if (++this.tickCounter % 20 == 0) {
               PacketHandler.networkWrapper.sendToServer(new GetTribeUiValuesPacket());
            }
         }
      }

   }


   // ------------------------------------------------------------------
   // Entity behaviour lives here; the per-entity static helpers were folded in.
   // ------------------------------------------------------------------



   <E extends IAnimatable> PlayState animationPredicateImpl(AnimationEvent<E> event) {
      if (this.world instanceof ModelPreviewWorldClient) {
         return PlayState.STOP;
      }

      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      float shrink = 0.25F - (Float)this.getDataManager().get(KoboldEntity.SIZE_SCALAR);
      GeckoLibCache.getInstance().parser.setValue("size", shrink);
      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.kobold.null", true, event);
            } else {
               this.createAnimation("animation.kobold.blink", true, event);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.kobold.null", true, event);
            } else if (this.isRiding()) {
               this.createAnimation("animation.kobold.sit", true, event);
            } else {
               double moved = Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ);
               if (!(Boolean)this.getDataManager().get(KoboldEntity.IS_ANCHORED) && moved > 0.0) {
                  if (this.onGround && Math.abs(Math.abs(this.prevPosY) - Math.abs(this.posY)) < 0.1F) {
                     this.rotationYaw = this.rotationYawHead;
                     double speed = 1.0 + shrink * 2.0F;
                     this.movementController.setAnimationSpeed(speed);
                     if (this.isBlockedByCeiling()) {
                        this.createAnimation("animation.kobold.crouch_walk", true, event);
                     } else if ((Boolean)this.getDataManager().get(KoboldEntity.ARMED)) {
                        this.createAnimation("animation.kobold.run_armed", true, event);
                     } else if (moved > 0.2F) {
                        this.createAnimation("animation.kobold.run", true, event);
                     } else {
                        this.createAnimation("animation.kobold.walk", true, event);
                     }
                  } else {
                     this.createAnimation("animation.kobold.fly", true, event);
                  }
               } else if (this.isBlockedByCeiling()) {
                  this.createAnimation("animation.kobold.crouch_idle", true, event);
               } else {
                  this.createAnimation(this.getDataManager().get(KoboldEntity.ARMED) ? "animation.kobold.idle_armed" : "animation.kobold.idle", true, event);
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.kobold.null", true, event);
                  break;
               case ATTACK:
                  this.createAnimation("animation.kobold.attack", false, event);
                  break;
               case RIDE:
               case SIT:
                  this.createAnimation("animation.kobold.sit", true, event);
                  break;
               case MINE:
                  this.createAnimation("animation.kobold.fall_tree", true, event);
                  break;
               case PAYMENT:
                  this.createAnimation("animation.kobold.paymentBackpack", true, event);
                  break;
               case STARTBLOWJOB:
                  this.createAnimation("animation.kobold.blowjobStart", false, event);
                  break;
               case SUCKBLOWJOB_BLINK:
                  String side = this.WildSlimeFaceLayer ? "R" : "L";
                  String animSuffix = this.switchVariant ? "Switch" : "";
                  this.createAnimation("animation.kobold.blowjobSlow" + side + animSuffix, true, event);
                  break;
               case THRUSTBLOWJOB:
                  this.createAnimation("animation.kobold.blowjobFast", true, event);
                  break;
               case CUMBLOWJOB:
                  this.createAnimation("animation.kobold.blowjobCum", false, event);
                  break;
               case KOBOLD_ANAL_START:
                  this.createAnimation("animation.kobold.analStart", false, event);
                  break;
               case KOBOLD_ANAL_SLOW:
                  this.createAnimation("animation.kobold.analSoft", true, event);
                  break;
               case KOBOLD_ANAL_FAST:
                  this.createAnimation("animation.kobold.analHard", true, event);
                  break;
               case KOBOLD_ANAL_CUM:
                  this.createAnimation("animation.kobold.analCum", true, event);
                  break;
               case SLEEP:
                  this.createAnimation("animation.kobold.sleep", true, event);
                  break;
               case MATING_PRESS_START:
                  this.createAnimation("animation.kobold.mating_press_start", false, event);
                  break;
               case MATING_PRESS_SOFT:
                  this.createAnimation("animation.kobold.mating_press_soft", true, event);
                  break;
               case MATING_PRESS_HARD:
                  this.createAnimation("animation.kobold.mating_press_hard", true, event);
                  break;
               case MATING_PRESS_CUM:
                  this.createAnimation("animation.kobold.mating_press_cum", true, event);
            }
      }

      return PlayState.CONTINUE;
   }



   void registerControllersImpl(AnimationData data) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
            case "attackSound":
               this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_STRONG);
               break;
            case "paymentMSG1":
               this.sendChatMessageToPlayer(this.getInteractionPlayerUUID(), "I'd like to use ur services owo");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "plob":
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  SceneFadeScreen.enableInteraction();
               }
               break;
            case "paymentDone":
               if (this.isControlledByLocalPlayer()) {
                  this.startActionFromHandState();
               }
               break;
            case "blowjobStartMSG1":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.0, 0.625 - player.getEyeHeight(), -1.0), this.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(this.getInteractionPlayerUUID().toString(), this.getTargetPosition().add(pos), this.getYawRotation() + 180.0F, 0.0F)
                     );
               }
               break;
            case "blowjobStartMSG2":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5 - player.getEyeHeight(), -0.6875), this.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(
                           this.getInteractionPlayerUUID().toString(), this.getTargetPosition().add(pos), this.getYawRotation() + 180.0F - 40.0F, 0.0F
                        )
                     );
               }
               break;
            case "lipsound":
               if (this.getRNG().nextBoolean()) {
                  this.playRandomSoundAtVolume(SoundHandler.GIRLS_ALLIE_LIPSOUND, 1.5F);
               } else {
                  this.playRandomSoundAtVolume(SoundHandler.GIRLS_JENNY_LIPSOUND, 1.5F);
               }

               HornyMeterHud.addToHornyMeter(0.02F);
               break;
            case "touch":
               this.playRandomSound(SoundHandler.MISC_TOUCH);
               break;
            case "blowjobStartDone":
               this.setCurrentAction(Action.SUCKBLOWJOB_BLINK);
               this.switchVariant = false;
               this.WildSlimeFaceLayer = true;
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "switch":
               this.switchVariant = this.getRNG().nextBoolean();
               this.actionController.clearAnimationCache();
               break;
            case "endSwitch":
               this.switchVariant = false;
               this.WildSlimeFaceLayer = !this.WildSlimeFaceLayer;
               this.actionController.clearAnimationCache();
               break;
            case "blowjobFastDone":
               if (this.isControlledByLocalPlayer() && !PlayerMovementController.isJumping) {
                  this.setCurrentAction(Action.SUCKBLOWJOB_BLINK);
               }
               break;
            case "cumLoud":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "cumQuiet":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "analCumDone":
            case "blowjobCumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.resetCameraAndPhysics();
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "analStartDone":
               this.setCurrentAction(Action.KOBOLD_ANAL_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "analStartCam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.0, 0.5625 - player.getEyeHeight(), 0.5625), this.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(new TeleportPlayerPacket(this.getInteractionPlayerUUID().toString(), this.getTargetPosition().add(pos), this.getYawRotation(), 0.0F));
               }
               break;
            case "pounding":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               break;
            case "analFastRapid":
               if (this.isControlledByLocalPlayer() && PlayerMovementController.isJumping) {
                  if (this.getCurrentAction() == Action.KOBOLD_ANAL_FAST) {
                     this.actionController.tickOffset = 0.0;
                  }

                  this.setCurrentAction(Action.KOBOLD_ANAL_FAST);
               }
               break;
            case "analDone":
               if (this.getCurrentAction() == Action.KOBOLD_ANAL_FAST) {
                  this.setCurrentAction(Action.KOBOLD_ANAL_SLOW);
               }
               break;
            case "analHard":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "analSoft":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "cum":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "giggle":
               this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_GIGGLE);
               break;
            case "moan":
               this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MOAN);
               break;
            case "moanMating":
               this.moanCooldown--;
               if (this.moanCooldown <= 0) {
                  this.moanCooldown = 3;
                  this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MOAN);
               }
               break;
            case "analHardMSG1":
               this.moanCooldown--;
               if (this.moanCooldown <= 0) {
                  this.moanCooldown = 4;
                  this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MOAN);
               }
               break;
            case "orgasm":
               this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_ORGASM);
               break;
            case "breath":
               this.playRandomSound(SoundHandler.GIRLS_KOBOLD_LIGHTBREATHING, 0.5F);
               break;
            case "haa":
               this.playRandomSound(SoundHandler.GIRLS_KOBOLD_HAA, 0.7F);
               break;
            case "interested":
               this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_INTERESTED);
               break;
            case "yep":
               this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_YEP);
               break;
            case "bjmoan":
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_KOBOLD_BJMOAN));
               break;
            case "blowjobStartbreath":
               int soundId = this.getRNG().nextInt(3);
               this.playSound(SoundHandler.GIRLS_KOBOLD_LIGHTBREATHING[soundId]);
               break;
            case "matingCam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = new Vec3d(0.0, 0.4375 - player.eyeHeight, -0.6875);
                  pos = VectorMath.rotateByYaw(pos, this.getYawRotation() + 180.0F);
                  pos = pos.add(this.getTargetPosition());
                  PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(player.getPersistentID().toString(), pos, this.getYawRotation() + 180.0F, 10.0F));
               }
               break;
            case "mating_press_startDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
            case "mating_press_hardDone":
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.MATING_PRESS_SOFT);
               }
               break;
            case "mating_press_softReady":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (this.isControlledByLocalPlayer() && PlayerMovementController.isJumping) {
                  this.setCurrentAction(Action.MATING_PRESS_HARD);
               }
               break;
            case "mating_press_hardReady":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (this.isControlledByLocalPlayer() && PlayerMovementController.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "mating_cum_cam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = new Vec3d(0.0, 1.1875 - player.eyeHeight, 0.125);
                  pos = VectorMath.rotateByYaw(pos, this.getYawRotation() + 180.0F);
                  pos = pos.add(this.getTargetPosition());
                  PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(player.getPersistentID().toString(), pos, this.getYawRotation() + 180.0F, 70.0F));
               }
               break;
            case "cumMsg":
               this.sendChatMessage("I.. hope I am satisfying you sir");
               this.playSound(SoundHandler.GIRLS_KOBOLD_SAD[this.getRNG().nextInt(1)]);
               break;
            case "renderEgg":
               this.isRenderEgg = true;
               this.playRandomSoundAtVolume(SoundHandler.MISC_PLOB, 0.5F);
               break;
            case "mating_press_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.resetCameraAndPhysics();
               }
         }
      };
      this.movementController.transitionLengthTicks = 10.0;
      this.actionController.registerSoundListener(soundListener);
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
   }



   boolean canStoreInventoryImpl(UUID tribeId, boolean checkOpen) {
      if (hasInventoryItemsImpl()) {
         return false;
      }

      if (isTribeChestOpenImpl(tribeId, checkOpen)) {
         this.inventoryNotifyCooldown = 0;
         return true;
      }

      if (--this.inventoryNotifyCooldown < 0 && this.tribeBlocked) {
         this.inventoryNotifyCooldown = 300;
         EntityPlayer master = this.world.getPlayerEntityByUUID(UUID.fromString((String)this.getDataManager().get(KoboldEntity.MASTER)));
         EyeAndKoboldColor tribeColor = EyeAndKoboldColor.valueOf((String)this.getDataManager().get(KoboldEntity.CURRENT_ACTION));
         if (master != null) {
            master.sendStatusMessage(
               new TextComponentString(
                  tribeColor.getTextColor()
                     + this.getDisplayNameText()
                     + "s "
                     + TextFormatting.WHITE
                     + "inventory is full and there are either no chests to put her items in or said chests are full as well"
               ),
               false
            );
         }

         return false;
      } else {
         return false;
      }
   }



   boolean isTribeChestOpenImpl(UUID tribeId, boolean checkOpen) {
      HashSet chests = KoboldManager.getTribeChests(tribeId);
      if (chests == null) {
         return false;
      }

      BlockPos chosenChest = null;
      ArrayList staleChests = new ArrayList();

      for (BlockPos chestPos : (java.util.Collection<BlockPos>) (chests) ) {
         TileEntity tileEntity = this.world.getTileEntity(chestPos);
         if (!(tileEntity instanceof TileEntityChest)) {
            staleChests.add(chestPos);
            continue;
         }

         TileEntityChest chest = (TileEntityChest)tileEntity;
         IItemHandler chestHandler = chest.getSingleChestHandler();
         boolean canStore = false;

         for (int i = 0; i < this.inventory.getSlots(); i++) {
            ItemStack stack = this.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
               for (int slot = 0; slot < chestHandler.getSlots(); slot++) {
                  ItemStack remaining = chestHandler.insertItem(slot, stack, true);
                  if (remaining.getCount() != stack.getCount()) {
                     canStore = true;
                     break;
                  }
               }

               if (canStore) {
                  break;
               }
            }
         }

         if (canStore) {
            if (chosenChest == null) {
               chosenChest = chestPos;
            } else if (this.getDistanceSq(chosenChest) > this.getDistanceSq(chestPos)) {
               chosenChest = chestPos;
            }
         }
      }

      if (!staleChests.isEmpty()) {
         chests.removeAll(staleChests);
      }

      if (chosenChest == null) {
         return false;
      }

      if (!(this.getDistance(chosenChest.getX(), chosenChest.getY(), chosenChest.getZ()) < 2.0)) {
         if (Math.abs(chosenChest.getY() - this.getPosition().getY()) > 4) {
            if (!checkOpen) {
               return false;
            }

            this.syncTribeBlocks(chosenChest);
         } else {
            PathNavigate navigator = this.getNavigator();
            BlockPos standPos = this.findStandPos(chosenChest);
            navigator.tryMoveToXYZ(standPos.getX(), standPos.getY(), standPos.getZ(), 0.35F);
            if (navigator.getPath() == null) {
               if (!checkOpen) {
                  return false;
               }

               this.syncTribeBlocks(chosenChest);
            }
         }

         return true;
      } else {
         TileEntity tileEntity = this.world.getTileEntity(chosenChest);
         if (!(tileEntity instanceof TileEntityChest)) {
            return false;
         }

         TileEntityChest chest = (TileEntityChest)tileEntity;
         IItemHandler chestHandler = chest.getSingleChestHandler();

         for (int i = 0; i < this.inventory.getSlots(); i++) {
            ItemStack stack = this.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
               for (int slot = 0; slot < chestHandler.getSlots(); slot++) {
                  ItemStack remaining = chestHandler.insertItem(slot, stack, false);
                  if (remaining.getCount() <= 0) {
                     this.inventory.setStackInSlot(i, ItemStack.EMPTY);
                     break;
                  }

                  this.inventory.setStackInSlot(i, remaining);
                  stack = remaining;
               }
            }
         }

         this.world.playSound(null, chosenChest, SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
         return true;
      }
   }



   boolean hasInventoryItemsImpl() {
      for (int slot = 0; slot < this.inventory.getSlots(); slot++) {
         if (!this.inventory.getStackInSlot(slot).isEmpty()) {
            return false;
         }
      }

      return true;
   }



   boolean canAssignTaskImpl(KoboldTask task) {
      ArrayList drops = new ArrayList();

      for (BlockPos target : task.getMiningTargets()) {
         try {
            IBlockState state = this.world.getBlockState(target);
            ItemStack drop = state.getBlock().getItem(this.world, target, state);
            drops.add(drop);
         } catch (IllegalArgumentException ex) {
            // ignored: blocks without an item form contribute no drop
         }
      }

      return canStoreItemsImpl(drops);
   }



   boolean canInsertItemImpl(ItemStack stack) {
      return canInsertItemStackImpl(this.inventory, stack, true, false);
   }



   boolean canStoreItemsImpl(List<ItemStack> stacks) {
      ItemStackHandler simulated = new ItemStackHandler(this.inventory.getSlots());

      for (int slot = 0; slot < simulated.getSlots(); slot++) {
         simulated.setStackInSlot(slot, this.inventory.getStackInSlot(slot));
      }

      for (ItemStack stack : stacks) {
         if (!canInsertItemStackImpl(simulated, stack, true, false)) {
            return false;
         }
      }

      return true;
   }



   boolean canExtractItemImpl(ItemStack stack) {
      return canInsertItemStackImpl(this.inventory, stack, false, true);
   }



   boolean canInsertItemStackImpl(ItemStackHandler handler, ItemStack stack, boolean simulate, boolean extract) {
      for (int slot = 0; slot < handler.getSlots(); slot++) {
         ItemStack existing = handler.getStackInSlot(slot);
         if (existing.getItem() == stack.getItem() && existing.getMetadata() == stack.getMetadata()) {
            int maxStack = existing.getMaxStackSize();
            if (maxStack > stack.getCount() + existing.getCount()) {
               if (!simulate) {
                  existing.setCount(existing.getCount() + stack.getCount());
               }

               return true;
            }

            int space = maxStack - existing.getCount();
            existing.setCount(maxStack);
            stack.setCount(stack.getCount() - space);
         }
      }

      for (int slot = 0; slot < handler.getSlots(); slot++) {
         ItemStack slotStack = handler.getStackInSlot(slot);
         if (slotStack.getItem() == Items.AIR) {
            if (!simulate) {
               handler.setStackInSlot(slot, stack);
            }

            return true;
         }
      }

      if (simulate) {
         return false;
      }

      if (!extract) {
         return false;
      }

      EntityItem itemEntity = new EntityItem(this.world);
      itemEntity.setItem(stack);
      itemEntity.setPosition(this.posX, this.posY, this.posZ);
      this.world.spawnEntity(itemEntity);
      return false;
   }



   void handleTaskStateImpl(UUID tribeId, KoboldTask task) {
      if (this.getCurrentAction() != Action.MINE) {
         handleTaskNavigationImpl(tribeId, task);
      } else {
         this.taskTimer--;
         this.miningCooldown--;
         if (this.miningCooldown == 0) {
            IBlockState fallingState = this.world.getBlockState(this.miningTargetPos.up());
            if (!(fallingState.getBlock() instanceof BlockFalling)) {
               task.removeMiningTarget(this.miningTargetPos);
               EntityPlayer master = this.getMasterPlayer();
               if (master != null) {
                  PacketHandler.networkWrapper.sendTo(new TribeBlocksPacket(this.miningTargetPos, false), (EntityPlayerMP)master);
               }
            }

            IBlockState blockState = this.world.getBlockState(this.miningTargetPos);
            this.canExtractItem(new ItemStack(blockState.getBlock().getItemDropped(blockState, this.getRNG(), 0), 1, blockState.getBlock().damageDropped(blockState)));
            this.world.destroyBlock(this.miningTargetPos, false);
         }

         if (this.taskTimer <= 0) {
            this.taskTimer = 100;
            this.miningCooldown = 24;
            this.setCurrentAction(Action.NULL);
         }
      }
   }



   void handleTaskNavigationImpl(UUID tribeId, KoboldTask task) {
      PathNavigate navigator = this.getNavigator();
      if (this.miningTargetPos != null && task.getMiningTargets().contains(this.miningTargetPos)) {
         IBlockState blockState = this.world.getBlockState(this.miningTargetPos);
         if (!this.canInsertItem(new ItemStack(blockState.getBlock().getItemDropped(blockState, Main.RANDOM, 0)))) {
            this.tribeBlocked = true;
            this.canStoreInventory(tribeId, true);
         } else if (this.motionX == 0.0
            && this.motionZ == 0.0
            && this.onGround
            && !(this.getDistance(this.miningTargetPos.getX(), this.miningTargetPos.getY(), this.miningTargetPos.getZ()) > 3.0)
            && ++this.miningStandTicks >= 10) {
            navigator.clearPath();
            this.miningStandTicks = 0;
            this.setCurrentAction(Action.MINE);
            this.rotationYawHead = (float)(
               Math.atan2(this.posZ - this.miningTargetPos.getZ(), this.posX - this.miningTargetPos.getX()) * (180.0 / Math.PI) + 90.0
            );
            this.rotationYaw = this.rotationYawHead;
            this.getDataManager().set(KoboldEntity.MINING_WITH_AXE, false);
         } else {
            BlockPos standPos = this.miningTargetPos.add(task.getFacing().getOpposite().getDirectionVec());
            navigator.tryMoveToXYZ(standPos.getX(), standPos.getY(), standPos.getZ(), 0.35F);
         }
      } else {
         this.miningTargetPos = executeMiningTaskImpl(task, tribeId);
         if (this.miningTargetPos == null) {
            boolean noTargets = task.getMiningTargets().isEmpty();
            HashSet blocks = KoboldManager.removeTaskAndGetBlocks(tribeId, task);
            UUID tribeUuid = KoboldManager.findTribeIdWith(tribeId);
            if (tribeUuid != null) {
               EntityPlayer player = this.world.getPlayerEntityByUUID(tribeUuid);
               if (player != null) {
                  if (!noTargets) {
                     player.sendMessage(new TextComponentString(String.format("<%s> It's impossible to mine here...", this.getDisplayNameText())));
                  }

                  PacketHandler.networkWrapper.sendTo(new TribeBlocksPacket(blocks, false), (EntityPlayerMP)player);
               }
            }
         } else {
            if (Math.abs(this.getPosition().getY() - task.getTargetPos().getY()) > 3) {
               BlockPos targetPos = task.getTargetPos().add(task.getFacing().getOpposite().getDirectionVec());
               this.world.setBlockState(targetPos, Blocks.AIR.getDefaultState());
               this.syncTribeBlocks(targetPos);
            }

            BlockPos standPos = this.miningTargetPos.add(task.getFacing().getOpposite().getDirectionVec());
            navigator.tryMoveToXYZ(standPos.getX(), standPos.getY(), standPos.getZ(), 0.35F);
         }
      }
   }



   BlockPos executeMiningTaskImpl(KoboldTask task, UUID tribeId) {
      HashSet miningTargets = task.getMiningTargets();
      EnumFacing facing = task.getFacing();
      ArrayList row = new ArrayList();
      Integer maxZ = null;
      if (miningTargets.isEmpty()) {
         return null;
      }

      for (BlockPos target : (java.util.Collection<BlockPos>) (miningTargets) ) {
         switch (facing) {
            case NORTH:
               if (maxZ == null || target.getZ() >= maxZ) {
                  maxZ = target.getZ();
                  row.add(target);
               }
               break;
            case SOUTH:
               if (maxZ == null || target.getZ() <= maxZ) {
                  maxZ = target.getZ();
                  row.add(target);
               }
               break;
            case EAST:
               if (maxZ == null || target.getX() <= maxZ) {
                  maxZ = target.getX();
                  row.add(target);
               }
               break;
            case WEST:
               if (maxZ == null || target.getX() >= maxZ) {
                  maxZ = target.getX();
                  row.add(target);
               }
         }
      }

      ArrayList column = new ArrayList();

      for (BlockPos pos : (java.util.Collection<BlockPos>) (row) ) {
         if ((facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) && pos.getZ() == maxZ) {
            column.add(pos);
         }

         if ((facing == EnumFacing.EAST || facing == EnumFacing.WEST) && pos.getX() == maxZ) {
            column.add(pos);
         }
      }

      if (column.isEmpty()) {
         return null;
      }

      ArrayList positions = new ArrayList();
      EnumFacing taskFacing = task.getFacing();
      BlockPos targetPos = task.getTargetPos();
      BlockPos minePos;
      if (taskFacing.getAxis() == Axis.Z) {
         minePos = new BlockPos(targetPos.getX(), targetPos.getY(), ((BlockPos)column.get(0)).getZ());
         if (taskFacing == EnumFacing.NORTH) {
            minePos = minePos.north();
         } else {
            minePos = minePos.south();
         }

         positions.add(minePos.down());
         positions.add(minePos.down().east());
         positions.add(minePos.down().west());
         positions.add(minePos);
         positions.add(minePos.up());
         positions.add(minePos.up().up());
         positions.add(minePos.up().up().up());
         positions.add(minePos.west());
         positions.add(minePos.west().up());
         positions.add(minePos.west().up().up());
         positions.add(minePos.west().up().up().up());
         positions.add(minePos.west().west());
         positions.add(minePos.west().west().up());
         positions.add(minePos.west().west().up().up());
         positions.add(minePos.east());
         positions.add(minePos.east().up());
         positions.add(minePos.east().up().up());
         positions.add(minePos.east().up().up().up());
         positions.add(minePos.east().east());
         positions.add(minePos.east().east().up());
         positions.add(minePos.east().east().up().up());
      } else {
         minePos = new BlockPos(((BlockPos)column.get(0)).getX(), targetPos.getY(), targetPos.getZ());
         if (taskFacing == EnumFacing.EAST) {
            minePos = minePos.east();
         } else {
            minePos = minePos.west();
         }

         positions.add(minePos.down());
         positions.add(minePos.down().north());
         positions.add(minePos.down().south());
         positions.add(minePos);
         positions.add(minePos.up());
         positions.add(minePos.up().up());
         positions.add(minePos.up().up().up());
         positions.add(minePos.south());
         positions.add(minePos.south().up());
         positions.add(minePos.south().up().up());
         positions.add(minePos.south().up().up().up());
         positions.add(minePos.south().south());
         positions.add(minePos.south().south().up());
         positions.add(minePos.south().south().up().up());
         positions.add(minePos.north());
         positions.add(minePos.north().up());
         positions.add(minePos.north().up().up());
         positions.add(minePos.north().up().up().up());
         positions.add(minePos.north().north());
         positions.add(minePos.north().north().up());
         positions.add(minePos.north().north().up().up());
      }

      HashSet liquidBlocks = new HashSet();

      for (BlockPos pos : (java.util.Collection<BlockPos>) (positions) ) {
         if (this.world.getBlockState(pos).getMaterial().isLiquid()) {
            this.world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState(), 2);
            if (column.contains(pos)) {
               liquidBlocks.add(pos);
            }
         }
      }

      if (!liquidBlocks.isEmpty()) {
         task.addMiningTargets(liquidBlocks);
         EntityPlayer master = this.getMasterPlayer();
         if (master != null) {
            PacketHandler.networkWrapper.sendTo(new TribeBlocksPacket(liquidBlocks, true), (EntityPlayerMP)master);
         }
      }

      positions.clear();
      positions.add(minePos.down());
      if (taskFacing.getAxis() == Axis.Z) {
         positions.add(minePos.down().west());
         positions.add(minePos.down().east());
      } else {
         positions.add(minePos.down().north());
         positions.add(minePos.down().south());
      }

      for (BlockPos pos : (java.util.Collection<BlockPos>) (positions) ) {
         if (this.world.getBlockState(pos).getBlock().isPassable(this.world, pos)) {
            this.world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
         }
      }

      HashSet airBlocks = new HashSet();

      for (BlockPos pos : (java.util.Collection<BlockPos>) (column) ) {
         Block block = this.world.getBlockState(pos).getBlock();
         if (block == Blocks.AIR) {
            airBlocks.add(pos);
         }
      }

      if (!airBlocks.isEmpty()) {
         column.removeAll(airBlocks);
         task.setMiningTargets(airBlocks);
         UUID tribeUuid = KoboldManager.findTribeIdWith(tribeId);
         if (tribeUuid != null) {
            EntityPlayer player = this.world.getPlayerEntityByUUID(tribeUuid);
            if (player != null) {
               PacketHandler.networkWrapper.sendTo(new TribeBlocksPacket(airBlocks, false), (EntityPlayerMP)player);
            }
         }
      }

      if (column.isEmpty()) {
         return executeMiningTaskImpl(task, tribeId);
      }

      BlockPos workerMinePos = null;
      List workers = task.getWorkers();

      for (int i = 0; i < workers.size(); i++) {
         if (((KoboldEntity)workers.get(i)).getEntityId() == this.getEntityId()) {
            if (i == 0) {
               workerMinePos = findTaskBlockImpl(column, -1, task.getFacing(), task.getTargetPos());
               if (workerMinePos == null) {
                  workerMinePos = findTaskBlockImpl(column, 0, task.getFacing(), task.getTargetPos());
                  if (workerMinePos == null) {
                     workerMinePos = findTaskBlockImpl(column, 1, task.getFacing(), task.getTargetPos());
                  }
               }
               break;
            }

            if (i == 1) {
               workerMinePos = findTaskBlockImpl(column, 1, task.getFacing(), task.getTargetPos());
               if (workerMinePos == null) {
                  workerMinePos = findTaskBlockImpl(column, 0, task.getFacing(), task.getTargetPos());
                  if (workerMinePos == null) {
                     workerMinePos = findTaskBlockImpl(column, -1, task.getFacing(), task.getTargetPos());
                  }
               }
               break;
            }

            if (i == 2) {
               workerMinePos = findTaskBlockImpl(column, 0, task.getFacing(), task.getTargetPos());
               if (workerMinePos == null) {
                  workerMinePos = findTaskBlockImpl(column, 1, task.getFacing(), task.getTargetPos());
                  if (workerMinePos == null) {
                     workerMinePos = findTaskBlockImpl(column, -1, task.getFacing(), task.getTargetPos());
                  }
               }
               break;
            }
         }
      }

      return workerMinePos;
   }



   BlockPos findTaskBlockImpl(List<BlockPos> blocks, int index, EnumFacing facing, BlockPos targetPos) {
      if (blocks.isEmpty()) {
         return null;
      }

      ArrayList extraBlocks = new ArrayList();
      ArrayList sideBlocks = new ArrayList();
      ArrayList rowBlocks = new ArrayList();
      int dir = facing != EnumFacing.SOUTH && facing != EnumFacing.WEST ? 1 : -1;
      if (facing.getAxis() == Axis.Z) {
         BlockPos rowAnchor = new BlockPos(targetPos.getX(), targetPos.getY(), ((BlockPos)blocks.get(0)).getZ());
         rowBlocks.add(rowAnchor);
         rowBlocks.add(rowAnchor.up());
         rowBlocks.add(rowAnchor.up().up());
         rowBlocks.add(rowAnchor.west());
         rowBlocks.add(rowAnchor.west().up());
         rowBlocks.add(rowAnchor.west().up().up());
         rowBlocks.add(rowAnchor.east());
         rowBlocks.add(rowAnchor.east().up());
         rowBlocks.add(rowAnchor.east().up().up());
         if (index == 0) {
            for (BlockPos rowPos : (java.util.Collection<BlockPos>) (rowBlocks) ) {
               sideBlocks.add(rowPos.east(2));
               sideBlocks.add(rowPos.east(-2));
            }

            for (BlockPos blockPos : blocks) {
               if (!sideBlocks.contains(blockPos)) {
                  extraBlocks.add(blockPos);
               }
            }
         } else {
            for (BlockPos rowPos : (java.util.Collection<BlockPos>) (rowBlocks) ) {
               sideBlocks.add(rowPos.east(dir * 2 * index));
            }

            for (BlockPos sidePos : (java.util.Collection<BlockPos>) (sideBlocks) ) {
               if (blocks.contains(sidePos)) {
                  extraBlocks.add(sidePos);
               }
            }
         }
      }

      if (facing.getAxis() == Axis.X) {
         BlockPos colAnchor = new BlockPos(((BlockPos)blocks.get(0)).getX(), targetPos.getY(), targetPos.getZ());
         rowBlocks.add(colAnchor);
         rowBlocks.add(colAnchor.up());
         rowBlocks.add(colAnchor.up().up());
         rowBlocks.add(colAnchor.north());
         rowBlocks.add(colAnchor.north().up());
         rowBlocks.add(colAnchor.north().up().up());
         rowBlocks.add(colAnchor.south());
         rowBlocks.add(colAnchor.south().up());
         rowBlocks.add(colAnchor.south().up().up());
         if (index == 0) {
            for (BlockPos colPos : (java.util.Collection<BlockPos>) (rowBlocks) ) {
               sideBlocks.add(colPos.south(2));
               sideBlocks.add(colPos.south(-2));
            }

            for (BlockPos blockPos : blocks) {
               if (!sideBlocks.contains(blockPos)) {
                  extraBlocks.add(blockPos);
               }
            }
         } else {
            for (BlockPos colPos : (java.util.Collection<BlockPos>) (rowBlocks) ) {
               sideBlocks.add(colPos.south(dir * 2 * index));
            }

            for (BlockPos sidePos : (java.util.Collection<BlockPos>) (sideBlocks) ) {
               if (blocks.contains(sidePos)) {
                  extraBlocks.add(sidePos);
               }
            }
         }
      }

      return extraBlocks.isEmpty() ? null : (BlockPos)extraBlocks.get(this.getRNG().nextInt(extraBlocks.size()));
   }



   void navigateToTaskImpl(UUID tribeId, KoboldTask task, BlockPos pos) {
      if (this.miningDropItem == null) {
         this.mineCooldown = 24;
         this.cooldownTicks = 0;
         this.setCurrentAction(Action.NULL);
         this.getDataManager().set(KoboldEntity.IS_ANCHORED, false);
         EntityPlayer master = this.getMasterPlayer();
         HashSet blocks = task.getMiningTargets();
         if (master != null && !blocks.isEmpty()) {
            PacketHandler.networkWrapper.sendTo(new TribeBlocksPacket(blocks, false), (EntityPlayerMP)master);
         }

         KoboldManager.setLeaderKobold(tribeId, this);
      } else {
         switch (this.miningDropItem.getMetadata()) {
            case 3:
            case 5:
               this.world
                  .setBlockState(
                     pos,
                     Blocks.SAPLING
                        .getStateForPlacement(
                           this.world,
                           pos,
                           EnumFacing.NORTH,
                           pos.getX(),
                           pos.getY(),
                           pos.getZ(),
                           this.miningDropItem.getMetadata(),
                           this,
                           EnumHand.MAIN_HAND
                        )
                  );
               this.world
                  .setBlockState(
                     pos.north(),
                     Blocks.SAPLING
                        .getStateForPlacement(
                           this.world,
                           pos.north(),
                           EnumFacing.NORTH,
                           pos.getX(),
                           pos.getY(),
                           pos.getZ() + 1,
                           this.miningDropItem.getMetadata(),
                           this,
                           EnumHand.MAIN_HAND
                        )
                  );
               this.world
                  .setBlockState(
                     pos.west(),
                     Blocks.SAPLING
                        .getStateForPlacement(
                           this.world,
                           pos.west(),
                           EnumFacing.NORTH,
                           pos.getX() + 1,
                           pos.getY(),
                           pos.getZ(),
                           this.miningDropItem.getMetadata(),
                           this,
                           EnumHand.MAIN_HAND
                        )
                  );
               this.world
                  .setBlockState(
                     pos.north().west(),
                     Blocks.SAPLING
                        .getStateForPlacement(
                           this.world,
                           pos.north().west(),
                           EnumFacing.NORTH,
                           pos.getX() + 1,
                           pos.getY(),
                           pos.getZ() + 1,
                           this.miningDropItem.getMetadata(),
                           this,
                           EnumHand.MAIN_HAND
                        )
                  );
               break;
            default:
               this.world
                  .setBlockState(
                     pos,
                     Blocks.SAPLING
                        .getStateForPlacement(
                           this.world,
                           pos,
                           EnumFacing.NORTH,
                           pos.getX(),
                           pos.getY(),
                           pos.getZ(),
                           this.miningDropItem.getMetadata(),
                           this,
                           EnumHand.MAIN_HAND
                        )
                  );
         }

         this.mineCooldown = 24;
         this.cooldownTicks = 0;
         this.miningDropItem = null;
         this.setCurrentAction(Action.NULL);
         this.setAnchored(false);
         EntityPlayer master = this.getMasterPlayer();
         HashSet blocks = task.getMiningTargets();
         if (master != null && !blocks.isEmpty()) {
            PacketHandler.networkWrapper.sendTo(new TribeBlocksPacket(blocks, false), (EntityPlayerMP)master);
         }

         KoboldManager.setLeaderKobold(tribeId, this);
      }
   }



   void startMiningTaskImpl(UUID tribeId, BlockPos pos, KoboldTask task) {
      if (this.getCurrentAction() != Action.MINE) {
         mineBlockAtImpl(pos, tribeId);
      } else {
         this.cooldownTicks--;
         if (this.cooldownTicks <= 0) {
            if (this.cooldownTicks == 0) {
               PacketHandler.networkWrapper.sendToAllAround(new ResetControllerPacket(this.getGirlId()), this.getTargetNetworkPoint());
            }

            if (this.world.getBlockState(pos).getBlock() == Blocks.AIR) {
               navigateToTaskImpl(tribeId, task, pos);
            } else {
               this.mineCooldown--;
               if (this.mineCooldown < 0) {
                  this.mineCooldown = 24;
                  this.cooldownTicks = 78;
                  HashSet targets = new HashSet();
                  EntityPlayer master = this.getMasterPlayer();

                  for (BlockPos target : task.getMiningTargets()) {
                     if (this.world.getBlockState(target).getBlock() != Blocks.AIR) {
                        if (target.getX() != pos.getX() || target.getZ() != pos.getZ()) {
                           try {
                              ItemStack dropStack = this.world
                                 .getBlockState(target)
                                 .getBlock()
                                 .getItem(this.world, target, this.world.getBlockState(target));
                              if (dropStack.getItem() != Items.AIR) {
                                 this.canExtractItem(dropStack);
                              }
                           } catch (IllegalArgumentException ex) {
                              Main.LOGGER
                                 .error(
                                    "Couldn't get an item out of the block that a kobold just destroyed when falling a tree. As a result, the block wasn't added into the kobolds inventory. If you see this message, pls tell trol about it and send her the following stacktrace. Do you maybe remember what block the kobold just removed? Stacktrace follwing:"
                                 );
                              Main.LOGGER.warn("block in question: " + this.world.getBlockState(target).getBlock().getTranslationKey());
                              Main.LOGGER.error(ex.getMessage());
                           }

                           this.miningDropItem = getBlockItemImpl(target);
                           this.world.destroyBlock(target, false);
                           task.removeMiningTarget(target);
                           targets.add(target);
                           task.setMiningTargets(targets);
                           if (master != null) {
                              PacketHandler.networkWrapper.sendTo(new TribeBlocksPacket(targets, false), (EntityPlayerMP)master);
                           }

                           return;
                        }
                     } else {
                        targets.add(target);
                     }
                  }

                  try {
                     ItemStack dropStack = this.world
                        .getBlockState(pos)
                        .getBlock()
                        .getItem(this.world, pos, this.world.getBlockState(pos));
                     if (dropStack.getItem() != Items.AIR) {
                        this.canExtractItem(dropStack);
                     }
                  } catch (IllegalArgumentException ex) {
                     Main.LOGGER
                        .error(
                           "Couldn't get an item out of the block that a kobold just destroyed when falling a tree. As a result, the block wasn't added into the kobolds inventory. If you see this message, pls tell trol about it and send her the following stacktrace. Do you maybe remember what block the kobold just removed? Stacktrace follwing:"
                        );
                     Main.LOGGER.warn("block in question: " + this.world.getBlockState(pos).getBlock().getTranslationKey());
                     Main.LOGGER.error(ex.getMessage());
                  }

                  this.miningDropItem = getBlockItemImpl(pos);
                  this.world.destroyBlock(pos, false);
                  int logCount = 0;

                  for (BlockPos target : task.getMiningTargets()) {
                     if (this.world.getBlockState(target).getBlock() instanceof BlockLog) {
                        logCount++;
                     }
                  }

                  HashSet trunkBlocks = new HashSet();

                  for (int i = 0; i < logCount; i++) {
                     trunkBlocks.add(pos.add(0, i, 0));
                  }

                  HashSet otherBlocks = new HashSet();

                  for (BlockPos target : task.getMiningTargets()) {
                     if (!trunkBlocks.contains(target)) {
                        otherBlocks.add(target);
                     }
                  }

                  if (!otherBlocks.isEmpty() && master != null) {
                     PacketHandler.networkWrapper.sendTo(new TribeBlocksPacket(otherBlocks, false), (EntityPlayerMP)master);
                  }

                  int height = 1;

                  while (true) {
                     BlockPos logPos = pos.add(0, height, 0);
                     IBlockState logState = this.world.getBlockState(logPos);
                     if (this.world.getBlockState(logPos).getBlock() instanceof BlockLog) {
                        this.world.destroyBlock(logPos, false);
                        EntityFallingBlock fallingBlock = new EntityFallingBlock(
                           this.world, logPos.getX() + 0.5, logPos.getY(), logPos.getZ() + 0.5, logState
                        );
                        fallingBlock.fallTime = 1;
                        this.world.spawnEntity(fallingBlock);
                     }

                     if (!task.getMiningTargets().contains(logPos)) {
                        return;
                     }

                     height++;
                  }
               }
            }
         }
      }
   }



   ItemStack getBlockItemImpl(BlockPos pos) {
      ItemStack stack;
      try {
         stack = this.world.getBlockState(pos).getBlock().getItem(this.world, pos, this.world.getBlockState(pos));
      } catch (IllegalArgumentException ex) {
         Main.LOGGER
            .error(
               "Couldn't turn a wooden block into an item to get its meta data. As a result the kobold is just gonna plant a oak saplinig instead. If you see this message, pls tell trol about it and send her the following stacktrace. Do you maybe remember what block the kobold just removed? Stacktrace follwing:"
            );
         Main.LOGGER.warn("block in question: " + this.world.getBlockState(pos).getBlock().getTranslationKey());
         Main.LOGGER.error(ex.getMessage());
         return new ItemStack(Blocks.SAPLING, 1, 0);
      }

      int blockId = ItemBlock.getIdFromItem(stack.getItem());
      int metadata = stack.getItem().getMetadata(stack);
      if (blockId == 17 && metadata == 1) {
         return new ItemStack(Blocks.SAPLING, 1, 1);
      } else if (blockId == 17 && metadata == 2) {
         return new ItemStack(Blocks.SAPLING, 1, 2);
      } else if (blockId == 17 && metadata == 3) {
         return new ItemStack(Blocks.SAPLING, 1, 3);
      } else if (blockId == 162 && metadata == 0) {
         return new ItemStack(Blocks.SAPLING, 1, 4);
      } else {
         return blockId == 162 && metadata == 1 ? new ItemStack(Blocks.SAPLING, 1, 5) : new ItemStack(Blocks.SAPLING, 1, 0);
      }
   }



   void mineBlockAtImpl(BlockPos pos, UUID tribeId) {
      BlockPos bestBlock = null;
      ArrayList neighbors = new ArrayList();
      if (this.world.getBlockState(pos.north().down()).isFullCube()
         && !this.world.getBlockState(pos.north()).isFullBlock()) {
         neighbors.add(pos.north());
      }

      if (this.world.getBlockState(pos.east().down()).isFullCube()
         && !this.world.getBlockState(pos.east()).isFullBlock()) {
         neighbors.add(pos.east());
      }

      if (this.world.getBlockState(pos.south().down()).isFullCube()
         && !this.world.getBlockState(pos.south()).isFullBlock()) {
         neighbors.add(pos.south());
      }

      if (this.world.getBlockState(pos.west().down()).isFullCube()
         && !this.world.getBlockState(pos.west()).isFullBlock()) {
         neighbors.add(pos.west());
      }

      for (BlockPos neighbor : (java.util.Collection<BlockPos>) (neighbors) ) {
         if (bestBlock == null) {
            bestBlock = neighbor;
         } else {
            double bestDist = new Vec3d(bestBlock.getX() + 0.5F, bestBlock.getY(), bestBlock.getZ() + 0.5F).distanceTo(this.getPositionVector());
            double dist = new Vec3d(neighbor.getX() + 0.5F, neighbor.getY(), neighbor.getZ() + 0.5F).distanceTo(this.getPositionVector());
            if (dist < bestDist) {
               bestBlock = neighbor;
            }
         }
      }

      if (bestBlock == null) {
         KoboldManager.setLeaderKobold(tribeId, this);
         EntityPlayer master = this.getMasterPlayer();
         if (master != null) {
            master.sendStatusMessage(new TextComponentString("Your kobolds cannot fall this tree because it starts underground"), true);
         }
      } else if (!(this.getPosition().getDistance(bestBlock.getX(), bestBlock.getY(), bestBlock.getZ()) > 1.0)) {
         float yaw = 0.0F;
         if (bestBlock.subtract(pos).equals(new BlockPos(0, 0, -1))) {
            yaw = 0.0F;
         }

         if (bestBlock.subtract(pos).equals(new BlockPos(1, 0, 0))) {
            yaw = 90.0F;
         }

         if (bestBlock.subtract(pos).equals(new BlockPos(0, 0, 1))) {
            yaw = 180.0F;
         }

         if (bestBlock.subtract(pos).equals(new BlockPos(-1, 0, 0))) {
            yaw = -90.0F;
         }

         this.setTargetPosition(new Vec3d(bestBlock.getX() + 0.5, bestBlock.getY(), bestBlock.getZ() + 0.5));
         this.setYawRotation(yaw);
         this.getDataManager().set(KoboldEntity.IS_ANCHORED, true);
         this.getDataManager().set(KoboldEntity.MINING_WITH_AXE, true);
         this.setCurrentAction(Action.MINE);
         this.world.destroyBlock(bestBlock.up(), false);
      } else if (Math.abs(this.getPosition().getY() - bestBlock.getY()) > 4) {
         this.syncTribeBlocks(bestBlock);
      } else {
         BlockPos standPos = this.findStandPos(bestBlock);
         this.getNavigator().tryMoveToXYZ(standPos.getX() + 0.5, standPos.getY(), standPos.getZ() + 0.5, 0.35);
         this.tickPathVelocity();
      }
   }



   String buildModelCodeDNAImpl(StringBuilder builder) {
      KoboldEntity.appendPaddedLetter(builder, 8);
      KoboldEntity.appendPaddedLetter(builder, 3);
      KoboldEntity.appendRandomGene(builder);
      KoboldEntity.appendRandomGene(builder);
      KoboldEntity.appendPaddedNumber(builder, 2);
      KoboldEntity.appendPaddedNumber(builder, 2);
      KoboldEntity.appendPaddedNumber(builder, 1);
      KoboldEntity.appendPaddedNumber(builder, 1);
      return builder.toString();
   }



   ArrayList<Integer> getCustomPartIdListImpl() {
      return new ArrayList<Integer>() {
         {
            this.add(101);
            this.add(EyeAndKoboldColor.values().length);
            this.add(EyeAndKoboldColor.values().length);
            this.add(8);
            this.add(3);
            this.add(101);
            this.add(101);
            this.add(3);
            this.add(3);
            this.add(4);
            this.add(2);
         }
      };
   }



   ArrayList<Integer> getBasePartIdListImpl() {
      ArrayList parts = new ArrayList();
      parts.add(Math.round((Float)this.getDataManager().get(KoboldEntity.SIZE_SCALAR) * 100.0F / 0.25F));
      parts.add(EyeAndKoboldColor.indexOf(EyeAndKoboldColor.safeValueOf((String)this.getDataManager().get(KoboldEntity.CURRENT_ACTION))));
      parts.add(EyeAndKoboldColor.indexOf(EyeAndKoboldColor.safeValueOf((Vec3i)this.getDataManager().get(KoboldEntity.ACTION_TARGET_POS))));
      return parts;
   }



   void setCustomPartListImpl(List<Integer> parts) {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < parts.size(); i++) {
         int partId = (Integer)parts.get(i);
         switch (i) {
            case 0:
               this.getDataManager().set(KoboldEntity.SIZE_SCALAR, partId / 100.0F * 0.25F);
               break;
            case 1:
               String currentColor = (String)this.getDataManager().get(KoboldEntity.CURRENT_ACTION);
               String newColor = EyeAndKoboldColor.values()[partId].toString();
               if (!newColor.equals(currentColor)) {
                  this.editedColorManually = true;
               }

               this.getDataManager().set(KoboldEntity.CURRENT_ACTION, newColor);
               break;
            case 2:
               this.getDataManager().set(KoboldEntity.ACTION_TARGET_POS, new BlockPos(EyeAndKoboldColor.values()[partId].getMainColor()));
               break;
            default:
               KoboldEntity.appendPaddedNumber(builder, partId);
         }
      }

      this.getDataManager().set(KoboldEntity.APPEARANCE_DNA, builder.toString());
      KoboldRenderer.clearBoneColors();
   }



   void updateModelCodeDNAImpl() {
      if (this.customPartsData != null) {
         String prevDna = (String)this.getDataManager().get(KoboldEntity.APPEARANCE_DNA);
         StringBuilder builder = new StringBuilder();

         for (int i = 0; i < this.customPartsData.size(); i++) {
            Entry entry = this.customPartsData.get(i);
            int partId = (Integer)((Entry)entry.getValue()).getValue();
            switch (i) {
               case 0:
                  this.getDataManager().set(KoboldEntity.SIZE_SCALAR, partId / 100.0F * 0.25F);
                  break;
               case 1:
                  this.getDataManager().set(KoboldEntity.CURRENT_ACTION, EyeAndKoboldColor.values()[partId].toString());
                  break;
               case 2:
                  this.getDataManager().set(KoboldEntity.ACTION_TARGET_POS, new BlockPos(EyeAndKoboldColor.values()[partId].getMainColor()));
                  break;
               default:
                  KoboldEntity.appendPaddedNumber(builder, partId);
            }
         }

         this.getDataManager().set(KoboldEntity.APPEARANCE_DNA, builder.toString());
         String newDna = (String)this.getDataManager().get(KoboldEntity.APPEARANCE_DNA);
         if (!newDna.equals(prevDna)) {
            Main.LOGGER.info("[sexmod-pack] KoboldEntity.updateModelCodeDNA '" + prevDna + "' -> '" + newDna + "'");
         }
         KoboldRenderer.clearBoneColors();
      }
   }



   Point2D getModelPartByIndexImpl(int index) {
      switch (index) {
         case 0:
            return new Point2D(160, 0);
         case 1:
            return new Point2D(180, 0);
         case 2:
            return new Point2D(200, 0);
         case 3:
            return new Point2D(220, 0);
         case 4:
            return new Point2D(227, 20);
         case 5:
            return new Point2D(140, 40);
         case 6:
            return new Point2D(160, 40);
         case 7:
            return new Point2D(180, 40);
         case 8:
            return new Point2D(227, 40);
         case 9:
            return new Point2D(0, 130);
         case 10:
            return new Point2D(20, 130);
         default:
            return Point2D.ZERO;
      }
   }



   void write(NBTTagCompound nbt) {
      nbt.setFloat("body_size", (Float)this.getDataManager().get(KoboldEntity.SIZE_SCALAR));
      nbt.setInteger("eyeColorX", ((BlockPos)this.getDataManager().get(KoboldEntity.ACTION_TARGET_POS)).getX());
      nbt.setInteger("eyeColorY", ((BlockPos)this.getDataManager().get(KoboldEntity.ACTION_TARGET_POS)).getY());
      nbt.setInteger("eyeColorZ", ((BlockPos)this.getDataManager().get(KoboldEntity.ACTION_TARGET_POS)).getZ());
      nbt.setString("model", (String)this.getDataManager().get(KoboldEntity.APPEARANCE_DNA));
      nbt.setString("name", (String)this.getDataManager().get(KoboldEntity.KOBOLD_NAME));
      nbt.setString("master", (String)this.getDataManager().get(KoboldEntity.MASTER));
      nbt.setTag("inventory", this.inventory.serializeNBT());
      nbt.setString("bodyColor", (String)this.getDataManager().get(KoboldEntity.CURRENT_ACTION));
      nbt.setBoolean("editedColorManually", this.editedColorManually);
      Optional tribeOpt = (Optional)this.getDataManager().get(KoboldEntity.TRIBE_ID);
      if (tribeOpt.isPresent()) {
         nbt.setUniqueId("tribeId", (UUID)tribeOpt.get());
         nbt.setBoolean("isLeader", KoboldManager.isTribeMember((UUID)tribeOpt.get(), this));
         nbt.setString("tribeName", (String)this.getDataManager().get(KoboldEntity.TRIBE_NAME));
      }
   }



   void read(NBTTagCompound nbt) {
      String modelCode = nbt.getString("model");
      if (!"".equals(modelCode)) {
         this.getDataManager().set(KoboldEntity.APPEARANCE_DNA, modelCode);
      }

      BlockPos eyeColor = new BlockPos(nbt.getInteger("eyeColorX"), nbt.getInteger("eyeColorY"), nbt.getInteger("eyeColorZ"));
      if (!BlockPos.ORIGIN.equals(eyeColor)) {
         this.getDataManager().set(KoboldEntity.ACTION_TARGET_POS, eyeColor);
      }

      this.getDataManager().set(KoboldEntity.SIZE_SCALAR, nbt.getFloat("body_size"));
      this.getDataManager().set(KoboldEntity.KOBOLD_NAME, nbt.getString("name"));
      this.getDataManager().set(KoboldEntity.MASTER, nbt.getString("master"));
      this.inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
      String bodyColor = nbt.getString("bodyColor");
      if (!"".equals(bodyColor)) {
         this.getDataManager().set(KoboldEntity.CURRENT_ACTION, nbt.getString("bodyColor"));
      }

      this.editedColorManually = nbt.getBoolean("editedColorManually");
      UUID tribeId = nbt.getUniqueId("tribeId");
      if (tribeId != null && !this.isDead) {
         this.getDataManager().set(KoboldEntity.TRIBE_ID, Optional.of(tribeId));
         if (!KoboldManager.doesTribeExist(tribeId)) {
            KoboldManager.setTribeColor(tribeId, EyeAndKoboldColor.valueOf((String)this.getDataManager().get(KoboldEntity.CURRENT_ACTION)));
         }

         KoboldManager.addTribeMember(tribeId, this);
         if (nbt.getBoolean("isLeader")) {
            KoboldManager.isTribeMember(tribeId, this);
         }

         this.getDataManager().set(KoboldEntity.TRIBE_NAME, nbt.getString("tribeName"));
      }
   }

}
