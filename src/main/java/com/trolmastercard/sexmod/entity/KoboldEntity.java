package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.api.KoboldNames;
import com.trolmastercard.sexmod.block.SexFireBlock;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.GirlInventoryScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.gui.TribeNameScreen;
import com.trolmastercard.sexmod.client.renderer.KoboldRenderer;
import com.trolmastercard.sexmod.client.renderer.WildSlimeFaceLayer;
import com.trolmastercard.sexmod.entity.ai.DoorInteractAiGoal;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.entity.api.IEllie;
import com.trolmastercard.sexmod.entity.api.IKobold;
import com.trolmastercard.sexmod.item.DragonStaffItem;
import com.trolmastercard.sexmod.item.KoboldEggItem;
import com.trolmastercard.sexmod.networking.GetTribeUiValuesPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetControllerPacket;
import com.trolmastercard.sexmod.networking.SendBlocksPacket;
import com.trolmastercard.sexmod.networking.SpawnParticlePacket;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.potion.HornyPotion;
import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.KoboldTask;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.WorldUtils;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.Point2D;
import com.trolmastercard.sexmod.util.SceneDebug;
import com.trolmastercard.sexmod.util.TribeState;
import com.google.common.base.Optional;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import javax.vecmath.Vector4d;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.resource.GeckoLibCache;

/**
 * Kobold NPC — the tribe creature with oral, anal and mating scenes, plus the
 * full tribe AI (tasks, mining, combat, breeding, follow modes).
 * <p>
 * <b>Tribe system:</b> kobolds belong to {@code KoboldManager.Tribe} objects
 * keyed by tribe UUID ({@code aL} data parameter). The tribe leader AI,
 * task assignment and saved positions are managed by
 * {@code KoboldManager}/{@code KoboldTask}.
 * <p>
 * <b>Scene entry</b> (shared with Jenny/Bia/Luna): client {@code doAction}
 * sets {@code animationFollowUp} (GIRL_HAND_STATES) via
 * {@code ChangeDataParameterPacket} and sends {@code KoboldStatePacket}; the
 * server calls {@code setDismounted()} ({@code sitLerpActive}), then
 * {@link #isSitting()} lerps her to {@code TARGET_POS} for ~40 ticks
 * ({@code sitLerpTicks} counter), anchors her and calls {@link #U()} which dispatches
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
 *   <li>{@link #U()} splits on the potion/master flags exactly as written —
 *       the payment gate is intentional, not a bug.</li>
 *   <li>Do not add {@code setDead}/removal logic to {@code onUpdate} —
 *       the girl hierarchy must never self-remove on benign tick errors.</li>
 * </ul>
 */
public class KoboldEntity extends AbstractNpcOnlyEntity implements IEllie, IInventory, IKobold {
   public static final EyeAndKoboldColor aJ = EyeAndKoboldColor.PURPLE;
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
   public static final DataParameter<Float> aE = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.FLOAT)
      .getSerializer()
      .createKey(122);
   public static final DataParameter<String> KOBOLD_NAME = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(123);
   public static final DataParameter<Boolean> aC = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(124);
   public static final DataParameter<Boolean> aZ = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(125);
   public static final DataParameter<String> aU = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(126);
   public static final DataParameter<Boolean> ak = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(127);
   public static final DataParameter<Boolean> at = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(128);
   public static final DataParameter<Optional<UUID>> aL = EntityDataManager.createKey(KoboldEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID)
      .getSerializer()
      .createKey(129);
   public static final int av = 24;
   public static double af = 69.0;
   public static List<Vector4d> aY = new ArrayList<>();
   ItemStackHandler inventory = new ItemStackHandler(27);
   public String as = null;
   boolean az = false;
   int aP = 0;
   int animationTicks = 0;
   boolean sitLerpActive = false;
   int sitLerpTicks = 0;
   int a5 = 0;
   float nearestDistance = Float.MAX_VALUE;
   static long aV = Long.MIN_VALUE;
   String[] an = new String[]{
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
   IBlockState aX = null;
   BlockPos aF = null;
   boolean ao = true;
   Vec3d aS = Vec3d.ZERO;
   BlockPos aM = null;
   BlockPos aI = null;
   int ai = 0;
   int taskTimer = 0;
   int miningStandTicks = 0;
   int a0 = 0;
   boolean ax = false;
   BlockPos ap = null;
   int ab = 0;
   int aR = 24;
   int cooldownTicks = 0;
   ItemStack ad = null;
   public boolean aA = false;
   int actionCooldown = -1;
   boolean WildSlimeFaceLayer = true;
   boolean aT = false;
   public boolean isRenderEgg = false;
   int aN = 0;

   public KoboldEntity(World world) {
      super(world);
      this.setSize(0.5F, 0.99F);
   }

   KoboldEntity(World world, UUID tribeId, float size) {
      this(world);
      this.entityDataManager.set(aL, Optional.of(tribeId));
      this.entityDataManager.set(aE, size);
   }

   public static KoboldEntity createKobold(World world, UUID tribeId) {
      float throwDelay = getRandomThrowDelay();
      return createKoboldWithSpeed(world, tribeId, throwDelay);
   }

   public static KoboldEntity createKoboldWithSpeed(World world, UUID tribeId, float throwDelay) {
      af = 10.0 - throwDelay * 25.0;
      return new KoboldEntity(world, tribeId, throwDelay);
   }

   @Override
   protected String buildModelCodeDNA(StringBuilder builder) {
      return KoboldModelCode.buildModelCodeDNA(this, builder);
   }

   @Override
   public ArrayList<Integer> getCustomPartIdList() {
      return KoboldModelCode.getCustomPartIdList(this);
   }

   @Override
   public ArrayList<Integer> getBasePartIdList() {
      return KoboldModelCode.getBasePartIdList(this);
   }

   @Override
   public void setCustomPartList(List<Integer> parts) {
      KoboldModelCode.setCustomPartList(this, parts);
   }

   void updateModelCodeDNA() {
      KoboldModelCode.updateModelCodeDNA(this);
   }

   @Override
   public Point2D getModelPartByIndex(int index) {
      return KoboldModelCode.getModelPartByIndex(this, index);
   }

   @Override
   public String getDisplayNameText() {
      return (String)this.entityDataManager.get(KOBOLD_NAME);
   }

   @Override
   public float getScaleFactor() {
      return 0.2F - (0.25F - (Float)this.entityDataManager.get(aE));
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
      this.entityDataManager.register(CURRENT_ACTION, aJ.name());
      this.entityDataManager.register(aL, Optional.absent());
      this.entityDataManager.register(aE, 0.0F);
      this.entityDataManager.register(KOBOLD_NAME, KoboldNames.values()[this.getRNG().nextInt(KoboldNames.values().length)].toString());
      this.entityDataManager.register(aC, false);
      this.entityDataManager.register(aZ, false);
      this.entityDataManager.register(aU, "null");
      this.entityDataManager.register(ak, false);
      this.entityDataManager.register(at, false);
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
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(af);
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

      if ((Boolean)this.entityDataManager.get(aC)) {
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

         Optional tribeIdOpt = (Optional)this.entityDataManager.get(aL);
         if (!tribeIdOpt.isPresent()) {
            return true;
         }

         if (!aY.isEmpty()) {
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
   public void ac() {
      if (this.az) {
         this.az = false;
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
      HandlePlayerMovement.setMovementLock(false);
   }

   @Override
   public void doAction(String action, UUID uuid) {
      this.az = true;
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
         SceneDebug.log(SceneDebug.SITTING, "Kobold.isSitting: lerp done, U() (sitLerpTicks=%d, action=%s, handState=%s)", this.sitLerpTicks, this.getCurrentAction(), this.entityDataManager.get(GIRL_HAND_STATES));
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
         this.U();
         return true;
      }

      this.rotationYaw = this.getYawRotation();
      this.setNoGravity(false);
      Vec3d pos = RotationHelper.lerpVec3d(this.getPositionVector(), this.getTargetPosition(), 40 - this.sitLerpTicks);
      this.setPosition(pos.x, pos.y, pos.z);
      this.setCurrentAction(Action.NULL);
      Optional tribeIdOpt = (Optional)this.entityDataManager.get(aL);
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
      this.ax = false;
      Optional tribeIdOpt = (Optional)this.entityDataManager.get(aL);
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
            if (!(Boolean)this.entityDataManager.get(aC)) {
               if (this.getHealth() != this.getMaxHealth() && ++this.a5 >= 100) {
                  this.setHealth(this.getHealth() + 2.0F);
                  this.a5 = 0;
                  PacketHandler.networkWrapper.sendToAllTracking(new SpawnParticlePacket(this.getGirlId(), EnumParticleTypes.HEART.getParticleName()), this);
               }
            } else {
               this.a5 = 0;
            }

            if (!(Boolean)this.entityDataManager.get(IS_ANCHORED)) {
               this.setNoGravity(false);
            }

            if (tribeIdOpt.isPresent()) {
               this.aP--;
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
                  this.entityDataManager.set(aC, this.handleTribeCombat((UUID)tribeIdOpt.get(), false));
                  this.entityDataManager.set(aZ, KoboldManager.isTribeMember((UUID)tribeIdOpt.get(), this));
                  this.entityDataManager.set(ak, KoboldManager.isTribeAlerted((UUID)tribeIdOpt.get()));
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
         if (this.world.getTotalWorldTime() - 300L >= aV) {
            if (this.hasMaster()) {
               if (this.getCurrentAction() == Action.NULL) {
                  if ("".equals(this.entityDataManager.get(GIRL_HAND_STATES))) {
                     if (!(Boolean)this.entityDataManager.get(ak)) {
                        String masterUuid = (String)this.entityDataManager.get(MASTER);
                        EntityPlayer player = this.world.getClosestPlayerToEntity(this, 10.0);
                        if (player == null) {
                           this.nearestDistance = Float.MAX_VALUE;
                        } else if (player.getPersistentID().toString().equals(masterUuid)) {
                           float dist = this.getDistance(player);
                           if (dist < 2.0F && this.nearestDistance > 2.0F) {
                              this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_KOBOLD_HEYMASTER));
                              this.sendChatMessage("Hey master!");
                              aV = this.world.getTotalWorldTime();
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
            if ((Boolean)this.entityDataManager.get(ak)) {
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
   protected void U() {
      String handState = (String)this.entityDataManager.get(BaseGirlEntity.GIRL_HAND_STATES);
      SceneDebug.log(SceneDebug.SCENE_ENTRY, "Kobold.U() handState=%s action=%s remote=%s potion=%s master=%s", handState, this.getCurrentAction(), this.world.isRemote, this.getActivePotionEffect(HornyPotion.HORNY_POTION) != null, this.hasMaster());
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
      KoboldMining.handleTaskState(this, tribeId, task);
   }

   void handleTaskNavigation(UUID tribeId, KoboldTask task) {
      KoboldMining.handleTaskNavigation(this, tribeId, task);
   }

   BlockPos executeMiningTask(KoboldTask task, UUID tribeId) {
      return KoboldMining.executeMiningTask(this, task, tribeId);
   }

   @Nullable
   BlockPos findTaskBlock(List<BlockPos> blocks, int index, EnumFacing facing, BlockPos targetPos) {
      return KoboldMining.findTaskBlock(this, blocks, index, facing, targetPos);
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
         if (this.ap == null
            || this.getDistance(this.ap.getX(), this.ap.getY(), this.ap.getZ()) > this.getWanderRange()
            || this.ab > 100) {
            int xOffset = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(5);
            int zOffset = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(5);
            int height = WorldUtils.getHeightAt(this.world, this.getPosition().getX() + xOffset, this.getPosition().getZ() + zOffset);
            this.ap = new BlockPos(this.getPosition().getX() + xOffset, height, this.getPosition().getZ() + zOffset);
            this.ab = 0;
         }

         if (Math.sqrt(this.ap.distanceSq(this.getPosition())) > 2.0) {
            this.getNavigator().tryMoveToXYZ(this.ap.getX(), this.ap.getY(), this.ap.getZ(), 0.35F);
            this.tickPathVelocity();
         } else {
            this.ab++;
         }
      }
   }

   double getWanderRange() {
      return Math.sqrt(800.0);
   }

   boolean canStoreInventory(UUID tribeId, boolean checkOpen) {
      return KoboldInventory.canStoreInventory(this, tribeId, checkOpen);
   }

   boolean isTribeChestOpen(UUID tribeId, boolean checkOpen) {
      return KoboldInventory.isTribeChestOpen(this, tribeId, checkOpen);
   }

   boolean assignTaskToKobold(UUID tribeId, KoboldTask task) {
      return KoboldTribeState.assignTaskToKobold(this, tribeId, task);
   }

   void navigateToTask(UUID tribeId, KoboldTask task, BlockPos pos) {
      KoboldMining.navigateToTask(this, tribeId, task, pos);
   }

   void startMiningTask(UUID tribeId, BlockPos pos, KoboldTask task) {
      KoboldMining.startMiningTask(this, tribeId, pos, task);
   }

   ItemStack getBlockItem(BlockPos pos) {
      return KoboldMining.getBlockItem(this, pos);
   }

   void mineBlockAt(BlockPos pos, UUID tribeId) {
      KoboldMining.mineBlockAt(this, pos, tribeId);
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
         Optional tribeOpt = (Optional)this.entityDataManager.get(aL);
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
      KoboldNbtIO.write(this, nbt);
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      KoboldNbtIO.read(this, nbt);
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
      return KoboldInventory.hasInventoryItems(this);
   }

   boolean canAssignTask(KoboldTask task) {
      return KoboldInventory.canAssignTask(this, task);
   }

   boolean canInsertItem(ItemStack stack) {
      return KoboldInventory.canInsertItem(this, stack);
   }

   boolean canStoreItems(List<ItemStack> stacks) {
      return KoboldInventory.canStoreItems(this, stacks);
   }

   boolean canExtractItem(ItemStack stack) {
      return KoboldInventory.canExtractItem(this, stack);
   }

   boolean canInsertItemStack(ItemStackHandler handler, ItemStack stack, boolean simulate, boolean extract) {
      return KoboldInventory.canInsertItemStack(this, handler, stack, simulate, extract);
   }

   @Override
   public void playSoundAtVolume(SoundEvent sound, float volume) {
      float shrink = 0.25F - (Float)this.entityDataManager.get(aE);
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
      return KoboldAnimation.animationPredicate(this, event);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData data) {
      KoboldAnimation.registerControllers(this, data);
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

   public static class c {
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
               Optional tribeOpt = (Optional)kobold.getDataManager().get(KoboldEntity.aL);
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
                  Optional tribeOpt = (Optional)kobold.getDataManager().get(KoboldEntity.aL);
                  if (tribeOpt.isPresent() && KoboldManager.isTribeMember((UUID)tribeOpt.get(), kobold)) {
                     kobold.teleportToHome((UUID)tribeOpt.get());
                  }
               }
            }
         } catch (ConcurrentModificationException ex) {
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
}
