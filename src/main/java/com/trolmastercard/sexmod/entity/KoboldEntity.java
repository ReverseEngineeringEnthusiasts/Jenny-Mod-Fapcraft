package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.api.IGalathFinish;
import com.trolmastercard.sexmod.api.IPositionProvider;
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
import com.trolmastercard.sexmod.util.DebugMode;
import com.trolmastercard.sexmod.util.GalathGeometryRender;
import com.trolmastercard.sexmod.util.GirlCombatProtection;
import com.trolmastercard.sexmod.util.EntityLookVectorHelper;
import com.trolmastercard.sexmod.util.GoblinFirstPersonRenderer;
import com.trolmastercard.sexmod.util.TrailSegment;
import com.trolmastercard.sexmod.util.WorldUtils;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.Point2D;
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
   boolean a2 = false;
   int aD = 0;
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
   int aK = 0;
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

   public KoboldEntity(World var1) {
      super(var1);
      this.setSize(0.5F, 0.99F);
   }

   KoboldEntity(World var1, UUID var2, float var3) {
      this(var1);
      this.entityDataManager.set(aL, Optional.of(var2));
      this.entityDataManager.set(aE, var3);
   }

   public static KoboldEntity createKobold(World var0, UUID var1) {
      float var2 = getRandomThrowDelay();
      return createKoboldWithSpeed(var0, var1, var2);
   }

   public static KoboldEntity createKoboldWithSpeed(World var0, UUID var1, float var2) {
      af = 10.0 - var2 * 25.0;
      return new KoboldEntity(var0, var1, var2);
   }

   @Override
   protected String buildModelCodeDNA(StringBuilder var1) {
      b(var1, 8);
      b(var1, 3);
      appendRandomGene(var1);
      appendRandomGene(var1);
      appendPaddedNumber(var1, 2);
      appendPaddedNumber(var1, 2);
      appendPaddedNumber(var1, 1);
      appendPaddedNumber(var1, 1);
      return var1.toString();
   }

   @Override
   public ArrayList<Integer> getCustomPartIdList() {
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

   @Override
   public ArrayList<Integer> getBasePartIdList() {
      ArrayList var1 = new ArrayList();
      var1.add(Math.round((Float)this.entityDataManager.get(aE) * 100.0F / 0.25F));
      var1.add(EyeAndKoboldColor.indexOf(EyeAndKoboldColor.safeValueOf((String)this.entityDataManager.get(CURRENT_ACTION))));
      var1.add(EyeAndKoboldColor.indexOf(EyeAndKoboldColor.safeValueOf((Vec3i)this.entityDataManager.get(ACTION_TARGET_POS))));
      return var1;
   }

   @Override
   public void setCustomPartList(List<Integer> var1) {
      StringBuilder var2 = new StringBuilder();

      for (int var3 = 0; var3 < var1.size(); var3++) {
         int var4 = (Integer)var1.get(var3);
         switch (var3) {
            case 0:
               this.entityDataManager.set(aE, var4 / 100.0F * 0.25F);
               break;
            case 1:
               String var5 = (String)this.entityDataManager.get(CURRENT_ACTION);
               String var6 = EyeAndKoboldColor.values()[var4].toString();
               if (!var6.equals(var5)) {
                  this.aA = true;
               }

               this.entityDataManager.set(CURRENT_ACTION, var6);
               break;
            case 2:
               this.entityDataManager.set(ACTION_TARGET_POS, new BlockPos(EyeAndKoboldColor.values()[var4].getMainColor()));
               break;
            default:
               c(var2, var4);
         }
      }

      this.entityDataManager.set(APPEARANCE_DNA, var2.toString());
      KoboldRenderer.clearBoneColors();
   }

   void updateModelCodeDNA() {
      if (this.customPartsData != null) {
         StringBuilder var1 = new StringBuilder();

         for (int var2 = 0; var2 < this.customPartsData.size(); var2++) {
            Entry var3 = this.customPartsData.get(var2);
            int var4 = (Integer)((Entry)var3.getValue()).getValue();
            switch (var2) {
               case 0:
                  this.entityDataManager.set(aE, var4 / 100.0F * 0.25F);
                  break;
               case 1:
                  this.entityDataManager.set(CURRENT_ACTION, EyeAndKoboldColor.values()[var4].toString());
                  break;
               case 2:
                  this.entityDataManager.set(ACTION_TARGET_POS, new BlockPos(EyeAndKoboldColor.values()[var4].getMainColor()));
                  break;
               default:
                  c(var1, var4);
            }
         }

         this.entityDataManager.set(APPEARANCE_DNA, var1.toString());
         KoboldRenderer.clearBoneColors();
      }
   }

   @Override
   public Point2D getModelPartByIndex(int var1) {
      switch (var1) {
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
      EyeAndKoboldColor var1 = EyeAndKoboldColor.values()[this.getRNG().nextInt(EyeAndKoboldColor.values().length)];
      this.entityDataManager.register(ACTION_TARGET_POS, new BlockPos(var1.getMainColor()));
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

   protected boolean processInteract(EntityPlayer var1, EnumHand var2) {
      if (this.getInteractionPlayerUUID() != null) {
         return false;
      }

      ItemStack var3 = var1.getHeldItem(EnumHand.MAIN_HAND);
      if (!var3.getItem().equals(Items.NAME_TAG)) {
         var3 = var1.getHeldItem(EnumHand.OFF_HAND);
      }

      if (var3.getItem().equals(Items.NAME_TAG) && var1.getPersistentID().toString().equals(this.entityDataManager.get(MASTER))) {
         this.entityDataManager.set(KOBOLD_NAME, var3.getDisplayName());
         var3.shrink(1);
         return true;
      }

      if ((Boolean)this.entityDataManager.get(aC)) {
         return false;
      }

      if (this.getCurrentAction() == Action.SLEEP) {
         return false;
      }

      ItemStack var4 = var1.getHeldItem(EnumHand.MAIN_HAND);
      if (var4.getItem() != DragonStaffItem.DRAGON_STAFF) {
         var4 = var1.getHeldItem(EnumHand.OFF_HAND);
      }

      if (!this.hasMaster() && var4.getItem() == DragonStaffItem.DRAGON_STAFF) {
         if (!this.world.isRemote) {
            return true;
         }

         Optional var5 = (Optional)this.entityDataManager.get(aL);
         if (!var5.isPresent()) {
            return true;
         }

         if (!aY.isEmpty()) {
            return true;
         }

         this.openTribeNameScreen((UUID)var5.get());
         return true;
      } else {
         if (this.hasMaster() && var4.getItem() == DragonStaffItem.DRAGON_STAFF && ((String)this.entityDataManager.get(MASTER)).equals(var1.getPersistentID().toString())) {
            var1.openGui(
               null, 1, this.world, this.getPosition().getX(), this.getPosition().getY(), this.getPosition().getZ()
            );
            return true;
         }

         if (this.world.isRemote) {
            if (this.hasMaster() && ((String)this.entityDataManager.get(MASTER)).equals(var1.getPersistentID().toString())) {
               this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MASTER);
            }

            this.openInteractionMenu(var1);
         } else {
            this.setInteractionPlayerUUID(var1.getPersistentID());
            this.getNavigator().clearPath();
            this.setYawRotation((float)(Math.atan2(this.posZ - var1.posZ, this.posX - var1.posX) * (180.0 / Math.PI) + 90.0));
            this.setTargetPosition(new Vec3d(this.posX, Math.floor(this.posY), this.posZ));
            this.entityDataManager.set(IS_ANCHORED, true);
            this.setCurrentAction(Action.NULL);
         }

         return true;
      }
   }

   @SideOnly(Side.CLIENT)
   void openTribeNameScreen(UUID var1) {
      Minecraft.getMinecraft().displayGuiScreen(new TribeNameScreen(var1));
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      if (this.hasMaster() && var1.getPersistentID().toString().equals(this.entityDataManager.get(MASTER))) {
         Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(this, var1, new String[]{"anal", "oral", "mating"}, null, false));
         return true;
      } else if (this.getActivePotionEffect(HornyPotion.HORNY_POTION) != null) {
         Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(this, var1, new String[]{"anal", "oral"}, null, false));
         return true;
      } else {
         Minecraft.getMinecraft()
            .displayGuiScreen(
               new GirlInventoryScreen(
                  this, var1, new String[]{"anal", "oral"}, new ItemStack[]{new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.IRON_PICKAXE)}, false
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

   protected void triggerActionSync(boolean var1, UUID var2) {
      super.triggerActionSync(var1, true, var2);
      HandlePlayerMovement.setMovementLock(false);
   }

   @Override
   public void doAction(String var1, UUID var2) {
      this.az = true;
      if ("oral".equals(var1)) {
         this.changeDataParameterFromClient("animationFollowUp", Action.STARTBLOWJOB.toString());
         this.triggerActionSync(true, var2);
      }

      if ("anal".equals(var1)) {
         this.changeDataParameterFromClient("animationFollowUp", Action.KOBOLD_ANAL_START.toString());
         this.triggerActionSync(true, var2);
      }

      if ("mating".equals(var1)) {
         this.changeDataParameterFromClient("animationFollowUp", Action.MATING_PRESS_START.toString());
         this.triggerActionSync(true, var2);
      }
   }

   @Override
   public void setDismounted() {
      this.a2 = true;
      this.entityDataManager.set(IS_ANCHORED, false);
   }

   @Override
   protected void clearBoneColors() {
      KoboldRenderer.clearBoneColors();
   }

   boolean isSitting() {
      if (!this.a2) {
         return false;
      }

      this.aD++;
      this.noClip = false;
      this.setNoGravity(false);
      if (this.aD > 40) {
         this.a2 = false;
         this.aD = 0;
         EntityPlayer var6 = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
         this.setYawRotation(var6.rotationYaw + 180.0F);
         this.entityDataManager.set(IS_ANCHORED, true);
         var6.noClip = true;
         var6.setNoGravity(true);
         this.noClip = true;
         this.setNoGravity(true);
         this.getNavigator().clearPath();
         this.U();
         return true;
      }

      this.rotationYaw = this.getYawRotation();
      this.setNoGravity(false);
      Vec3d var1 = RotationHelper.lerpVec3dDouble(this.getPositionVector(), this.getTargetPosition(), 40 - this.aD);
      this.setPosition(var1.x, var1.y, var1.z);
      this.setCurrentAction(Action.NULL);
      Optional var2 = (Optional)this.entityDataManager.get(aL);
      if (!var2.isPresent()) {
         return true;
      }

      Collection var3 = KoboldManager.getTribeTasks((UUID)var2.get());
      if (var3 == null) {
         return true;
      }

      for (KoboldTask var5 : (java.util.Collection<KoboldTask>) (var3) ) {
         var5.addWorker(this);
      }

      return true;
   }

   void handleActionCooldown(UUID var1) {
      if (this.actionCooldown != -1) {
         if (++this.actionCooldown >= 132) {
            this.actionCooldown = -1;
            if (this.getCurrentAction() == Action.MATING_PRESS_CUM) {
               UUID var2 = this.getInteractionPlayerUUID();
               if (var2 != null) {
                  EntityPlayer var3 = this.world.getPlayerEntityByUUID(var2);
                  if (var3 != null) {
                     EyeAndKoboldColor var4 = KoboldManager.getTribeColor(var1);
                     ItemStack var5 = new ItemStack(KoboldEggItem.KOBOLD_EGG_ITEM, 1, var4.getWoolMeta());
                     NBTTagCompound var6 = var5.getTagCompound();
                     if (var6 == null) {
                        var6 = new NBTTagCompound();
                     }

                     var6.setString("tribeID", var1.toString());
                     var6.setString("tribeColor", var4.toString());
                     var5.setTagCompound(var6);
                     var3.inventory.addItemStackToInventory(var5);
                  }
               }
            }
         }
      }
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      this.ax = false;
      Optional var1 = (Optional)this.entityDataManager.get(aL);
      if (var1.isPresent()) {
         this.handleActionCooldown((UUID)var1.get());
         KoboldManager.triggerFastSexAction((UUID)var1.get());
         EntityPlayer var2 = this.getMasterPlayer();
         if (var2 != null) {
            KoboldManager.assignMaster((UUID)var1.get(), var2.getPersistentID());
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

            if (var1.isPresent()) {
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
                     HashSet var6 = KoboldManager.getTribeTargets((UUID)var1.get());
                     HashSet var3 = new HashSet();

                     for (EntityLivingBase var5 : (java.util.Collection<EntityLivingBase>) (var6) ) {
                        if (!(var5.getDistance(this) > 2.0F)) {
                           var5.attackEntityFrom(DamageSource.causeMobDamage(this), 5.0F);
                           if (var5.isDead) {
                              var3.add(var5);
                           }
                        }
                     }

                     for (EntityLivingBase var8 : (java.util.Collection<EntityLivingBase>) (var3) ) {
                        KoboldManager.removeCombatant((UUID)var1.get(), var8);
                     }
                  }

                  if (84 <= this.animationTicks) {
                     this.setCurrentAction(Action.NULL);
                     this.entityDataManager.set(IS_ANCHORED, false);
                     this.animationTicks = 0;
                  }
               } else {
                  this.entityDataManager.set(aC, this.handleTribeCombat((UUID)var1.get(), false));
                  this.entityDataManager.set(aZ, KoboldManager.isTribeMember((UUID)var1.get(), this));
                  this.entityDataManager.set(ak, KoboldManager.isTribeAlerted((UUID)var1.get()));
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
                        String var1 = (String)this.entityDataManager.get(MASTER);
                        EntityPlayer var2 = this.world.getClosestPlayerToEntity(this, 10.0);
                        if (var2 == null) {
                           this.nearestDistance = Float.MAX_VALUE;
                        } else if (var2.getPersistentID().toString().equals(var1)) {
                           float var3 = this.getDistance(var2);
                           if (var3 < 2.0F && this.nearestDistance > 2.0F) {
                              this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_KOBOLD_HEYMASTER));
                              this.sendChatMessage("Hey master!");
                              aV = this.world.getTotalWorldTime();
                           }

                           this.nearestDistance = var3;
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
                  EntityPlayer var1 = this.world.getPlayerEntityByUUID(UUID.fromString((String)this.entityDataManager.get(MASTER)));
                  if (var1 != null) {
                     this.handleKoboldOwner(var1);
                  }
               }
            }
         }
      }
   }

   void handleTribeState() {
      if (!(Boolean)this.entityDataManager.get(aC)) {
         if (!this.hasMaster()) {
            Optional var1 = (Optional)this.entityDataManager.get(aL);
            if (var1.isPresent()) {
               for (EntityPlayer var3 : this.world.playerEntities) {
                  double var4 = var3.getPositionVector().distanceTo(this.getPositionVector());
                  double var6 = var4;
                  if (!this.world.isRemote) {
                     for (KoboldEntity var9 : KoboldManager.getTribeMembersList((UUID)var1.get())) {
                        double var10 = var3.getPositionVector().distanceTo(var9.getPositionVector());
                        if (var10 < var6) {
                           var6 = var10;
                        }
                     }
                  }

                  if (!(var6 > 10.0)) {
                     if (var3.getHeldItem(EnumHand.MAIN_HAND).getItem() != DragonStaffItem.DRAGON_STAFF
                        && var3.getHeldItem(EnumHand.OFF_HAND).getItem() != DragonStaffItem.DRAGON_STAFF) {
                        return;
                     }

                     PathNavigate var12 = this.getNavigator();
                     var12.clearPath();
                     if (this.world.isRemote) {
                        this.handleKoboldOwner(var3);
                     } else if (var4 > 2.0) {
                        BlockPos var13 = this.findStandPos(var3.getPosition());
                        var12.tryMoveToXYZ(var13.getX(), var13.getY(), var13.getZ(), 0.35F);
                     }

                     return;
                  }
               }
            }
         }
      }
   }

   @Override
   protected void U() {
      String var1 = (String)this.entityDataManager.get(BaseGirlEntity.GIRL_HAND_STATES);
      boolean var2 = this.getActivePotionEffect(HornyPotion.HORNY_POTION) != null;
      boolean var3 = false;
      if (this.hasMaster()) {
         var3 = ((String)this.entityDataManager.get(MASTER)).equals(this.getInteractionPlayerUUID().toString());
      }

      if (!var2 && !var3) {
         if (var1.equals(Action.STARTBLOWJOB.toString())) {
            if (this.getCurrentAction() == Action.PAYMENT) {
               this.setCurrentAction(Action.STARTBLOWJOB);
            } else {
               this.setCurrentAction(Action.PAYMENT);
            }
         }

         if (var1.equals(Action.KOBOLD_ANAL_START.toString())) {
            if (this.getCurrentAction() == Action.PAYMENT) {
               this.setCurrentAction(Action.KOBOLD_ANAL_START);
            } else {
               this.setCurrentAction(Action.PAYMENT);
            }
         }

         if (var1.equals(Action.MATING_PRESS_START.toString())) {
            this.setCurrentAction(Action.MATING_PRESS_START);
         }
      } else {
         if (var1.equals(Action.STARTBLOWJOB.toString())) {
            this.setCurrentAction(Action.STARTBLOWJOB);
         }

         if (var1.equals(Action.KOBOLD_ANAL_START.toString())) {
            this.setCurrentAction(Action.KOBOLD_ANAL_START);
         }

         if (var1.equals(Action.MATING_PRESS_START.toString())) {
            this.setCurrentAction(Action.MATING_PRESS_START);
         }
      }
   }

   void handleInteraction() {
      if (this.world.isRemote) {
         UUID var1 = this.getInteractionPlayerUUID();
         if (var1 != null) {
            if ((Boolean)this.entityDataManager.get(IS_ANCHORED)) {
               if (this.getCurrentAction() == Action.NULL) {
                  EntityPlayer var2 = this.world.getPlayerEntityByUUID(var1);
                  if (var2 != null) {
                     this.handleKoboldOwner(var2);
                  }
               }
            }
         }
      }
   }

   void handleKoboldOwner(EntityPlayer var1) {
      AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1.getPersistentID());
      Vec3d var3 = new Vec3d(var1.posX, var1.posY + (var2 == null ? var1.eyeHeight : var2.getEyeHeight()), var1.posZ);
      Vec3d var4 = new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
      double var5 = var4.distanceTo(var3);
      double var7 = var3.y - var4.y;
      this.rotationPitch = (float)(-(Math.sin(var7 / var5) * (180.0 / Math.PI)));
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
      Optional var1 = (Optional)this.entityDataManager.get(aL);
      if (var1.isPresent()) {
         UUID var2 = (UUID)var1.get();
         if (!(Boolean)this.entityDataManager.get(aC) && KoboldManager.isTribeAlerted(var2)) {
            if (!this.hasMaster()) {
               return;
            }

            EntityPlayer var3 = this.getMasterPlayer();
            if (var3 == null) {
               return;
            }

            for (KoboldTask var5 : KoboldManager.getTribeTasks(var2)) {
               if (var5.hasWorker(this)) {
                  var5.addWorker(this);
                  this.setCurrentAction(Action.NULL);
                  this.entityDataManager.set(IS_ANCHORED, false);
               }
            }

            this.noClip = false;
            this.setNoGravity(false);
            PathNavigate var7 = this.getNavigator();
            double var8 = this.getPositionVector().distanceTo(var3.getPositionVector());
            if (var8 > 2.0) {
               var7.tryMoveToEntityLiving(var3, this.getKickDistance(var3, var8));
               this.tickPathVelocity();
               if (var8 > 15.0) {
                  this.handlePlayerDismount(var3);
               }
            }
         } else if (KoboldManager.isTribeMember(var2, this)) {
            this.handleTribeRequest(var2);
         } else {
            this.handleTribeJoin(var2);
         }
      }
   }

   protected double getKickDistance(EntityPlayer var1, double var2) {
      double var4;
      if (var1.isSprinting()) {
         var4 = 0.7;
      } else {
         var4 = 0.35;
      }

      double var6 = Math.floor(var2 / 5.0) * 0.3;
      var4 += var6;
      if (this.isInWater()) {
         var4 *= 60.0;
      }

      return var4;
   }

   void teleportToHome(UUID var1) {
      BlockPos var2 = KoboldManager.getTribeHomePos(var1);
      if (var2 != null) {
         if (this.aX != null) {
            this.world.setBlockState(var2, this.aX);
         }

         if (this.blockBelowState != null) {
            this.world.setBlockState(var2.add(0, -1, 0), this.blockBelowState);
         }
      }
   }

   void handleTribeRequest(UUID var1) {
      if (!this.isTribeTaskDone(var1)) {
         if (!this.hasMaster() && KoboldManager.hasAssignedMaster(var1)) {
            this.getNavigator().clearPath();
            this.aM = null;
         } else {
            TribeState var2 = KoboldManager.getTribeState(var1);
            TribeState var3 = this.getTribeStateForTime();
            if (var2 != var3) {
KoboldManager.setTribeState(var1, var3);
               switch (var3) {
                  case REST:
                     this.handleTaskAssign(var1);
                     KoboldManager.setTribeHome(var1, (BlockPos)null);
                     this.sendGirlChatMessage("okay resting time owo");
                     break;
                  case ACTIVE:
                     this.teleportToHome(var1);
                     this.handleMemberSync(var1);
               }
            }

            switch (var3) {
               case REST:
                  this.handleTaskRequest(var1);
                  break;
               case ACTIVE:
                  this.aF = null;
                  this.handleHomeRelease(var1);
            }
         }
      }
   }

   void handleTaskAssign(UUID var1) {
      Collection var2 = KoboldManager.getTribeTasks(var1);
      if (var2 != null) {
         for (KoboldTask var4 : (java.util.Collection<KoboldTask>) (var2) ) {
            var4.releaseWorkers();
         }
      }
   }

   void handleMemberSync(UUID var1) {
      if (this.hasMaster()) {
         for (KoboldEntity var4 : KoboldManager.getTribeMembersList(var1)) {
            KoboldManager.setTribeLeader(var4);
            if (var4.getInteractionPlayerUUID() == null) {
               var4.noClip = false;
               var4.setNoGravity(false);
               var4.getDataManager().set(IS_ANCHORED, false);
               var4.setCurrentAction(Action.NULL);
            }
         }
      }
   }

   void handleTaskRequest(UUID var1) {
      Collection var2 = KoboldManager.getTribeTasks(var1);
      if (var2 != null) {
         for (KoboldTask var4 : (java.util.Collection<KoboldTask>) (var2) ) {
            var4.addWorker(this);
         }
      }

      if (this.hasMaster()) {
         this.handleBedRequest(var1);
      } else {
         this.handleHomeRequest(var1);
      }
   }

   void handleBedRequest(UUID var1) {
      BlockPos[] var2 = KoboldManager.getBedForKobold(this);
      if (var2 != null) {
         Vec3d var11 = new Vec3d(var2[0].getX() + 0.5F, var2[0].getY() + 0.5625, var2[0].getZ() + 0.5F);
         Vec3d var12 = new Vec3d(var2[1].getX() + 0.5F, var2[1].getY() + 0.5625, var2[1].getZ() + 0.5F);
         boolean var14 = var11.subtract(var12).x == 0.0;
         Vec3d var15 = RotationHelper.lerpVec3dDouble(var11, var12, 0.5);
         this.entityDataManager.set(IS_ANCHORED, true);
         this.setTargetPosition(var15);
         this.setYawRotation(var14 ? 0.0F : 90.0F);
         this.noClip = true;
         this.setNoGravity(true);
      } else {
         HashSet var3 = KoboldManager.getTribeBeds(var1);
         BlockPos var4 = null;
         if (var3 != null) {
            for (BlockPos var6 : (java.util.Collection<BlockPos>) (var3) ) {
               IBlockState var7 = this.world.getBlockState(var6);
               boolean var8 = false;
               UnmodifiableIterator var9 = var7.getProperties().entrySet().iterator();

               while (var9.hasNext()) {
                  Entry var10 = (Entry)var9.next();
                  if (var10.getKey() instanceof PropertyBool) {
                     var8 = Boolean.valueOf((Boolean)var10.getValue());
                     break;
                  }
               }

               if (!var8 && !KoboldManager.isBedAssigned(var6)) {
                  if (var4 == null) {
                     var4 = var6;
                  } else if (this.getDistanceSq(var4) > this.getDistanceSq(var6)) {
                     var4 = var6;
                  }
               }
            }

            if (var4 != null) {
               if (var4.getDistance((int)this.posX, (int)this.posY, (int)this.posZ) > 2.0) {
                  if (Math.abs(var4.subtract(this.getPosition()).getY()) > 4) {
                     this.syncTribeBlocks(var4.add(0, 1, 0));
                  } else {
                     BlockPos var13 = this.findStandPos(var4);
                     this.getNavigator().tryMoveToXYZ(var13.getX(), var13.getY(), var13.getZ(), 0.35F);
                     if (this.getNavigator().getPath() == null) {
                        this.syncTribeBlocks(var4.add(0, 1, 0));
                     }
                  }
               } else {
KoboldManager.assignBed(this, var4);
                  this.setCurrentAction(Action.SLEEP);
               }
            }
         }
      }
   }

   void handleHomeRequest(UUID var1) {
      BlockPos var2 = KoboldManager.getTribeHomePos(var1);
      if (var2 != null) {
         if (this.aF == null) {
            this.aF = var2.add(
               (this.getRNG().nextBoolean() ? 1 : -1) * (this.getRNG().nextInt(2) + 1),
               0,
               (this.getRNG().nextBoolean() ? 1 : -1) * (this.getRNG().nextInt(2) + 1)
            );
         }

         this.getNavigator().tryMoveToXYZ(this.aF.getX(), this.aF.getY(), this.aF.getZ(), 0.35F);
         this.tickPathVelocity();
      } else {
         if (KoboldManager.isTribeMember(var1, this)) {
            BlockPos var3 = this.getPosition().add(1, 0, 0);
            this.blockBelowState = this.world.getBlockState(var3.add(0, -1, 0));
            this.aX = this.world.getBlockState(var3);
            this.world.setBlockState(var3.add(0, -1, 0), Blocks.NETHERRACK.getDefaultState());
            this.world.setBlockState(var3, SexFireBlock.FIRE.getDefaultState());
            KoboldManager.setTribeHome(var1, var3);
         }
      }
   }

   void handleHomeRelease(UUID var1) {
      if (this.hasMaster()) {
         KoboldManager.setTribeHome(var1, (BlockPos)null);
         this.handleTaskFollow(var1);
      } else {
         Collection var2 = KoboldManager.getTribeTasks(var1);
         if (var2 != null) {
            if (this.ao) {
               this.aM = null;
               this.handleTribeTasks(var1, var2);
            } else {
               this.handleTribeTasksInit(var1, var2);
            }
         }
      }
   }

   void handleTribeTasks(UUID var1, Collection<KoboldTask> var2) {
      if (var2.isEmpty()) {
         this.ao = false;
         this.checkTribeHome(var1);
         this.sendGirlChatMessage("Lets go somewhere else");
      }
   }

   void handleTribeTasksInit(UUID var1, Collection<KoboldTask> var2) {
      BlockPos var3 = KoboldManager.getTribeHomePos(var1);
      if (var3 == null) {
         this.checkTribeHome(var1);
      } else {
         if (this.ticksExisted % 40 == 0) {
            if (this.aS.equals(this.getPositionVector())) {
               this.checkTribeHome(var1);
               this.aM = null;
            }

            this.aS = this.getPositionVector();
         }

         if (this.aM == null || this.aM.getDistance((int)this.posX, (int)this.posY, (int)this.posZ) < 4.0) {
            this.aM = this.getTribeHomePos(var1);
         }

         this.getNavigator().tryMoveToXYZ(this.aM.getX(), this.aM.getY(), this.aM.getZ(), 0.35F);
         this.tickPathVelocity();
         if (!(Math.sqrt(this.getPosition().distanceSq(var3)) > 5.0)) {
            this.ao = true;
            this.sendGirlChatMessage("Time to work bitches!");
            int var4 = KoboldManager.getTribeMemberCount(var1);

            for (int var5 = 1; var5 < var4; var5++) {
               this.findConnectedLogs(var1, var2);
            }

            KoboldManager.setTribeHome(var1, (BlockPos)null);
         }
      }
   }

   protected void handlePlayerDismount(EntityPlayer var1) {
      int var3 = 0;

      BlockPos var2;
      do {
         var2 = var1.getPosition().add(Reference.RANDOM.nextInt(10), 0, Reference.RANDOM.nextInt(10));
      } while (++var3 < 20 && !this.attemptTeleport(var2.getX(), var2.getY(), var2.getZ()));

      if (var3 == 20) {
         this.setPosition(var1.posX, var1.posY, var1.posZ);
      }

      this.motionX = 0.0;
      this.motionY = 0.0;
      this.motionZ = 0.0;
   }

   BlockPos getTribeHomePos(UUID var1) {
      BlockPos var2 = KoboldManager.getTribeHomePos(var1);
      return var2 == null ? BlockPos.ORIGIN : this.findStandPos(var2);
   }

   BlockPos findStandPos(BlockPos var1) {
      BlockPos var2 = this.getPosition();
      BlockPos var3 = var1.subtract(var2);
      if (Math.abs(var3.getX()) + Math.abs(var3.getZ()) < 20) {
         return var1;
      }

      double var4 = Math.min(Math.abs(var3.getX()), Math.abs(var3.getZ()));
      double var6 = Math.max(Math.abs(var3.getX()), Math.abs(var3.getZ()));
      double var8 = var4 / (var6 + var4);
      int var10 = (int)((var3.getX() > 0 ? 1 : -1) * 20 * (var4 == Math.abs(var3.getX()) ? var8 : 1.0 - var8));
      int var11 = (int)((var3.getZ() > 0 ? 1 : -1) * 20 * (var4 == Math.abs(var3.getZ()) ? var8 : 1.0 - var8));
      BlockPos var12 = this.getPosition().add(var10, 0, var11);
      return new BlockPos(var12.getX(), WorldUtils.a(this.world, var12.getX(), var12.getZ()) + 1, var12.getZ());
   }

   void checkTribeHome(UUID var1) {
      int var3 = 0;

      BlockPos var5;
      do {
         var3++;
         var5 = this.getPosition();
         var5 = var5.add(
            (50 + this.getRNG().nextInt(50)) * (this.getRNG().nextBoolean() ? 1 : -1),
            0,
            (50 + this.getRNG().nextInt(50)) * (this.getRNG().nextBoolean() ? 1 : -1)
         );
         var5 = new BlockPos(var5.getX(), WorldUtils.a(this.world, var5.getX(), var5.getZ()), var5.getZ());
      } while ((var5.getY() <= 0 || !this.getNavigator().canEntityStandOnPos(var5)) && var3 < 100);

      KoboldManager.setTribeHome(var1, var5);
   }

   void findConnectedLogs(UUID var1, Collection<KoboldTask> var2) {
      List var3 = this.findBlocksInRadius(this.getPosition(), BlockLog.class, 30, 4, null);
      BlockPos var4 = null;

      for (BlockPos var6 : (java.util.Collection<BlockPos>) (var3) ) {
         Block var7 = this.world.getBlockState(var6.down()).getBlock();
         if (!(var7 instanceof BlockLog) && var7 != Blocks.AIR) {
            boolean var8 = false;

            for (KoboldTask var10 : var2) {
               if (var10.isMiningTarget(var6)) {
                  var8 = true;
                  break;
               }
            }

            if (!var8) {
               var4 = var6;
               break;
            }
         }
      }

      if (var4 != null) {
         KoboldTask.a(this.world, var4, var1);
         this.sendGirlChatMessage("Someone, go fall this tree!");
      }
   }

   TribeState getTribeStateForTime() {
      long var1 = this.world.getWorldTime();
      return var1 < 12000L ? TribeState.ACTIVE : TribeState.REST;
   }

   boolean isTribeTaskDone(UUID var1) {
      return this.handleTribeCombat(var1, true);
   }

   boolean handleTribeCombat(UUID var1, boolean var2) {
      HashSet var3 = KoboldManager.getTribeTargets(var1);
      KoboldEntity var4 = KoboldManager.getTribeLeader(var1);
      if (var4 == null) {
         return false;
      }

      for (KoboldEntity var6 : this.world
         .getEntitiesWithinAABB(
            KoboldEntity.class,
            new AxisAlignedBB(
               var4.posX - 30.0,
               var4.posY - 30.0,
               var4.posZ - 30.0,
               var4.posX + 30.0,
               var4.posY + 30.0,
               var4.posZ + 30.0
            )
         )) {
         if (this.canEntityBeSeen(var6) && (!var6.hasMaster() || !this.hasMaster())) {
            Optional var7 = (Optional)var6.getDataManager().get(aL);
            if (!var7.isPresent()) {
               var3.add(var6);
            } else if (!((UUID)var7.get()).equals(var1)) {
               var3.add(var6);
            }
         }
      }

      EntityLivingBase var9 = null;
      ArrayList var10 = new ArrayList();

      for (EntityLivingBase var8 : (java.util.Collection<EntityLivingBase>) (var3) ) {
         if (var8.isDead) {
            var10.add(var8);
         } else if (!(var4.getDistance(var8) > 30.0F) && (var9 == null || this.getDistance(var9) > this.getDistance(var8))) {
            var9 = var8;
         }
      }

      for (EntityLivingBase var14 : (java.util.Collection<EntityLivingBase>) (var10) ) {
         KoboldManager.removeCombatant(var1, var14);
      }

      if (var9 == null) {
         return false;
      }

      if (!var2) {
         return true;
      }

      if (this.getCurrentAction() != Action.ATTACK) {
         this.entityDataManager.set(IS_ANCHORED, false);
         this.setCurrentAction(Action.NULL);
      }

      BlockPos var13 = this.findStandPos(var9.getPosition());
      this.getNavigator().tryMoveToXYZ(var13.getX(), var13.getY(), var13.getZ(), 0.7);
      this.tickPathVelocity();
      if (this.getDistance(var9) > 1.5F) {
         return true;
      }

      if (this.aP > 0) {
         return true;
      }

      float var15 = (float)(Math.atan2(this.posZ - var9.posZ, this.posX - var9.posX) * (180.0 / Math.PI) + 90.0);
      this.setYawRotation(var15);
      this.setCurrentAction(Action.ATTACK);
      this.aP = 84;
      return true;
   }

   void handleTribeJoin(UUID var1) {
      if (!this.isTribeTaskDone(var1)) {
         TribeState var2 = KoboldManager.getTribeState(var1);
         switch (var2) {
            case REST:
               this.handleTaskRequest(var1);
               break;
            case ACTIVE:
               this.aF = null;
               this.handleHomeTeleport(var1);
         }
      }
   }

   void handleHomeTeleport(UUID var1) {
      BlockPos var2 = KoboldManager.getTribeHomePos(var1);
      if (var2 == null) {
         this.aM = null;
         this.handleTaskFollow(var1);
      } else {
         KoboldEntity var3 = KoboldManager.getTribeLeader(var1);
         if (KoboldManager.hasAssignedMaster(var1)) {
            this.getNavigator().clearPath();
            this.aM = null;
         } else if (var3 == null) {
            System.out.println("leader of tribe " + var1 + " is null");
         } else {
            if (var3.getDistance(this) > 20.0F) {
               this.setPosition(var3.posX, var3.posY, var3.posZ);
               this.aM = null;
            }

            if (this.ticksExisted % 40 == 0) {
               if (this.aS.equals(this.getPositionVector())) {
                  this.aM = this.getTribeHomePos(var1);
               }

               this.aS = this.getPositionVector();
            }

            if (this.aM == null || this.aM.getDistance((int)this.posX, (int)this.posY, (int)this.posZ) < 4.0) {
               this.aM = this.getTribeHomePos(var1);
            }

            this.getNavigator().tryMoveToXYZ(this.aM.getX(), this.aM.getY(), this.aM.getZ(), 0.35F);
            this.tickPathVelocity();
         }
      }
   }

   void handleTaskFollow(UUID var1) {
      if (this.getInteractionPlayerUUID() == null) {
         Collection var2 = KoboldManager.getTribeTasks(var1);
         if (var2 != null) {
            KoboldTask var3 = null;

            for (KoboldTask var5 : (java.util.Collection<KoboldTask>) (var2) ) {
               if (var5.hasWorker(this)) {
                  var3 = var5;
                  break;
               }
            }

            if (var3 == null) {
               for (KoboldTask var7 : (java.util.Collection<KoboldTask>) (var2) ) {
                  if (!this.hasMaster() || this.assignTaskToKobold(var1, var7)) {
                     if (!this.canAssignTask(var7)) {
                        this.ax = true;
                     } else if (var7.addWorker(this)) {
                        var3 = var7;
                        this.aI = null;
                        if (var7.getTaskType() == KoboldTask.TaskType.FALL_TREE) {
                           this.sendGirlChatMessage("Ima fall this tree owo");
                        } else {
                           this.sendGirlChatMessage("Ima go mine uwu");
                           this.syncTribeBlocks(var7.getTargetPos());
                           this.world.setBlockState(var7.getTargetPos(), Blocks.AIR.getDefaultState());
                        }
                        break;
                     }
                  }
               }
            }

            if (var3 == null) {
               this.handleNearbyPlayerTick(var1);
            } else {
               if (var3.getTaskType() == KoboldTask.TaskType.FALL_TREE) {
                  this.startMiningTask(var1, var3.getTargetPos(), var3);
               }

               if (var3.getTaskType() == KoboldTask.TaskType.MINE) {
                  this.handleTribeTasks(var1, var2);
               }
            }
         }
      }
   }

   void syncTribeBlocks(BlockPos var1) {
      PacketHandler.networkWrapper
         .sendToAllTracking(
            new SpawnParticlePacket(this.getGirlId(), EnumParticleTypes.PORTAL.getParticleName(), 30),
            new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 30.0)
         );
      this.setPosition(0.5F + var1.getX(), var1.getY(), 0.5F + var1.getZ());
      PacketHandler.networkWrapper
         .sendToAllTracking(
            new SpawnParticlePacket(this.getGirlId(), EnumParticleTypes.PORTAL.getParticleName(), 30),
            new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 30.0)
         );
   }

   void handleTaskState(UUID var1, KoboldTask var2) {
      if (this.getCurrentAction() != Action.MINE) {
         this.handleTaskNavigation(var1, var2);
      } else {
         this.taskTimer--;
         this.ai--;
         if (this.ai == 0) {
            IBlockState var3 = this.world.getBlockState(this.aI.up());
            if (!(var3.getBlock() instanceof BlockFalling)) {
               var2.removeMiningTarget(this.aI);
               EntityPlayer var4 = this.getMasterPlayer();
               if (var4 != null) {
                  PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(this.aI, false), (EntityPlayerMP)var4);
               }
            }

            IBlockState var5 = this.world.getBlockState(this.aI);
            this.canExtractItem(new ItemStack(var5.getBlock().getItemDropped(var5, this.getRNG(), 0), 1, var5.getBlock().damageDropped(var5)));
            this.world.destroyBlock(this.aI, false);
         }

         if (this.taskTimer <= 0) {
            this.taskTimer = 100;
            this.ai = 24;
            this.setCurrentAction(Action.NULL);
         }
      }
   }

   void handleTaskNavigation(UUID var1, KoboldTask var2) {
      PathNavigate var3 = this.getNavigator();
      if (this.aI != null && var2.getMiningTargets().contains(this.aI)) {
         IBlockState var10 = this.world.getBlockState(this.aI);
         if (!this.canInsertItem(new ItemStack(var10.getBlock().getItemDropped(var10, Reference.RANDOM, 0)))) {
            this.ax = true;
            this.canStoreInventory(var1, true);
         } else if (this.motionX == 0.0
            && this.motionZ == 0.0
            && this.onGround
            && !(this.getDistance(this.aI.getX(), this.aI.getY(), this.aI.getZ()) > 3.0)
            && ++this.aK >= 10) {
            var3.clearPath();
            this.aK = 0;
            this.setCurrentAction(Action.MINE);
            this.rotationYawHead = (float)(
               Math.atan2(this.posZ - this.aI.getZ(), this.posX - this.aI.getX()) * (180.0 / Math.PI) + 90.0
            );
            this.rotationYaw = this.rotationYawHead;
            this.entityDataManager.set(at, false);
         } else {
            BlockPos var11 = this.aI.add(var2.getFacing().getOpposite().getDirectionVec());
            var3.tryMoveToXYZ(var11.getX(), var11.getY(), var11.getZ(), 0.35F);
         }
      } else {
         this.aI = this.executeMiningTask(var2, var1);
         if (this.aI == null) {
            boolean var9 = var2.getMiningTargets().isEmpty();
            HashSet var5 = KoboldManager.removeTaskAndGetBlocks(var1, var2);
            UUID var6 = KoboldManager.findTribeIdWith(var1);
            if (var6 != null) {
               EntityPlayer var7 = this.world.getPlayerEntityByUUID(var6);
               if (var7 != null) {
                  if (!var9) {
                     var7.sendMessage(new TextComponentString(String.format("<%s> It's impossible to mine here...", this.getDisplayNameText())));
                  }

                  PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var5, false), (EntityPlayerMP)var7);
               }
            }
         } else {
            if (Math.abs(this.getPosition().getY() - var2.getTargetPos().getY()) > 3) {
               BlockPos var4 = var2.getTargetPos().add(var2.getFacing().getOpposite().getDirectionVec());
               this.world.setBlockState(var4, Blocks.AIR.getDefaultState());
               this.syncTribeBlocks(var4);
            }

            BlockPos var8 = this.aI.add(var2.getFacing().getOpposite().getDirectionVec());
            var3.tryMoveToXYZ(var8.getX(), var8.getY(), var8.getZ(), 0.35F);
         }
      }
   }

   BlockPos executeMiningTask(KoboldTask var1, UUID var2) {
      HashSet var3 = var1.getMiningTargets();
      EnumFacing var4 = var1.getFacing();
      ArrayList var5 = new ArrayList();
      Integer var6 = null;
      if (var3.isEmpty()) {
         return null;
      }

      for (BlockPos var8 : (java.util.Collection<BlockPos>) (var3) ) {
         switch (var4) {
            case NORTH:
               if (var6 == null || var8.getZ() >= var6) {
                  var6 = var8.getZ();
                  var5.add(var8);
               }
               break;
            case SOUTH:
               if (var6 == null || var8.getZ() <= var6) {
                  var6 = var8.getZ();
                  var5.add(var8);
               }
               break;
            case EAST:
               if (var6 == null || var8.getX() <= var6) {
                  var6 = var8.getX();
                  var5.add(var8);
               }
               break;
            case WEST:
               if (var6 == null || var8.getX() >= var6) {
                  var6 = var8.getX();
                  var5.add(var8);
               }
         }
      }

      ArrayList var17 = new ArrayList();

      for (BlockPos var9 : (java.util.Collection<BlockPos>) (var5) ) {
         if ((var4 == EnumFacing.NORTH || var4 == EnumFacing.SOUTH) && var9.getZ() == var6) {
            var17.add(var9);
         }

         if ((var4 == EnumFacing.EAST || var4 == EnumFacing.WEST) && var9.getX() == var6) {
            var17.add(var9);
         }
      }

      if (var17.isEmpty()) {
         return null;
      }

      ArrayList var19 = new ArrayList();
      EnumFacing var20 = var1.getFacing();
      BlockPos var10 = var1.getTargetPos();
      BlockPos var21;
      if (var20.getAxis() == Axis.Z) {
         var21 = new BlockPos(var10.getX(), var10.getY(), ((BlockPos)var17.get(0)).getZ());
         if (var20 == EnumFacing.NORTH) {
            var21 = var21.north();
         } else {
            var21 = var21.south();
         }

         var19.add(var21.down());
         var19.add(var21.down().east());
         var19.add(var21.down().west());
         var19.add(var21);
         var19.add(var21.up());
         var19.add(var21.up().up());
         var19.add(var21.up().up().up());
         var19.add(var21.west());
         var19.add(var21.west().up());
         var19.add(var21.west().up().up());
         var19.add(var21.west().up().up().up());
         var19.add(var21.west().west());
         var19.add(var21.west().west().up());
         var19.add(var21.west().west().up().up());
         var19.add(var21.east());
         var19.add(var21.east().up());
         var19.add(var21.east().up().up());
         var19.add(var21.east().up().up().up());
         var19.add(var21.east().east());
         var19.add(var21.east().east().up());
         var19.add(var21.east().east().up().up());
      } else {
         var21 = new BlockPos(((BlockPos)var17.get(0)).getX(), var10.getY(), var10.getZ());
         if (var20 == EnumFacing.EAST) {
            var21 = var21.east();
         } else {
            var21 = var21.west();
         }

         var19.add(var21.down());
         var19.add(var21.down().north());
         var19.add(var21.down().south());
         var19.add(var21);
         var19.add(var21.up());
         var19.add(var21.up().up());
         var19.add(var21.up().up().up());
         var19.add(var21.south());
         var19.add(var21.south().up());
         var19.add(var21.south().up().up());
         var19.add(var21.south().up().up().up());
         var19.add(var21.south().south());
         var19.add(var21.south().south().up());
         var19.add(var21.south().south().up().up());
         var19.add(var21.north());
         var19.add(var21.north().up());
         var19.add(var21.north().up().up());
         var19.add(var21.north().up().up().up());
         var19.add(var21.north().north());
         var19.add(var21.north().north().up());
         var19.add(var21.north().north().up().up());
      }

      HashSet var12 = new HashSet();

      for (BlockPos var14 : (java.util.Collection<BlockPos>) (var19) ) {
         if (this.world.getBlockState(var14).getMaterial().isLiquid()) {
            this.world.setBlockState(var14, Blocks.COBBLESTONE.getDefaultState(), 2);
            if (var17.contains(var14)) {
               var12.add(var14);
            }
         }
      }

      if (!var12.isEmpty()) {
         var1.addMiningTargets(var12);
         EntityPlayer var23 = this.getMasterPlayer();
         if (var23 != null) {
            PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var12, true), (EntityPlayerMP)var23);
         }
      }

      var19.clear();
      var19.add(var21.down());
      if (var20.getAxis() == Axis.Z) {
         var19.add(var21.down().west());
         var19.add(var21.down().east());
      } else {
         var19.add(var21.down().north());
         var19.add(var21.down().south());
      }

      for (BlockPos var26 : (java.util.Collection<BlockPos>) (var19) ) {
         if (this.world.getBlockState(var26).getBlock().isPassable(this.world, var26)) {
            this.world.setBlockState(var26, Blocks.COBBLESTONE.getDefaultState());
         }
      }

      HashSet var25 = new HashSet();

      for (BlockPos var15 : (java.util.Collection<BlockPos>) (var17) ) {
         Block var16 = this.world.getBlockState(var15).getBlock();
         if (var16 == Blocks.AIR) {
            var25.add(var15);
         }
      }

      if (!var25.isEmpty()) {
         var17.removeAll(var25);
         var1.setMiningTargets(var25);
         UUID var28 = KoboldManager.findTribeIdWith(var2);
         if (var28 != null) {
            EntityPlayer var30 = this.world.getPlayerEntityByUUID(var28);
            if (var30 != null) {
               PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var25, false), (EntityPlayerMP)var30);
            }
         }
      }

      if (var17.isEmpty()) {
         return this.executeMiningTask(var1, var2);
      }

      BlockPos var29 = null;
      List var31 = var1.getWorkers();

      for (int var32 = 0; var32 < var31.size(); var32++) {
         if (((KoboldEntity)var31.get(var32)).getEntityId() == this.getEntityId()) {
            if (var32 == 0) {
               var29 = this.findTaskBlock(var17, -1, var1.getFacing(), var1.getTargetPos());
               if (var29 == null) {
                  var29 = this.findTaskBlock(var17, 0, var1.getFacing(), var1.getTargetPos());
                  if (var29 == null) {
                     var29 = this.findTaskBlock(var17, 1, var1.getFacing(), var1.getTargetPos());
                  }
               }
               break;
            }

            if (var32 == 1) {
               var29 = this.findTaskBlock(var17, 1, var1.getFacing(), var1.getTargetPos());
               if (var29 == null) {
                  var29 = this.findTaskBlock(var17, 0, var1.getFacing(), var1.getTargetPos());
                  if (var29 == null) {
                     var29 = this.findTaskBlock(var17, -1, var1.getFacing(), var1.getTargetPos());
                  }
               }
               break;
            }

            if (var32 == 2) {
               var29 = this.findTaskBlock(var17, 0, var1.getFacing(), var1.getTargetPos());
               if (var29 == null) {
                  var29 = this.findTaskBlock(var17, 1, var1.getFacing(), var1.getTargetPos());
                  if (var29 == null) {
                     var29 = this.findTaskBlock(var17, -1, var1.getFacing(), var1.getTargetPos());
                  }
               }
               break;
            }
         }
      }

      return var29;
   }

   @Nullable
   BlockPos findTaskBlock(List<BlockPos> var1, int var2, EnumFacing var3, BlockPos var4) {
      if (var1.isEmpty()) {
         return null;
      }

      ArrayList var5 = new ArrayList();
      ArrayList var6 = new ArrayList();
      ArrayList var7 = new ArrayList();
      int var8 = var3 != EnumFacing.SOUTH && var3 != EnumFacing.WEST ? 1 : -1;
      if (var3.getAxis() == Axis.Z) {
         BlockPos var9 = new BlockPos(var4.getX(), var4.getY(), ((BlockPos)var1.get(0)).getZ());
         var7.add(var9);
         var7.add(var9.up());
         var7.add(var9.up().up());
         var7.add(var9.west());
         var7.add(var9.west().up());
         var7.add(var9.west().up().up());
         var7.add(var9.east());
         var7.add(var9.east().up());
         var7.add(var9.east().up().up());
         if (var2 == 0) {
            for (BlockPos var11 : (java.util.Collection<BlockPos>) (var7) ) {
               var6.add(var11.east(2));
               var6.add(var11.east(-2));
            }

            for (BlockPos var20 : var1) {
               if (!var6.contains(var20)) {
                  var5.add(var20);
               }
            }
         } else {
            for (BlockPos var21 : (java.util.Collection<BlockPos>) (var7) ) {
               var6.add(var21.east(var8 * 2 * var2));
            }

            for (BlockPos var22 : (java.util.Collection<BlockPos>) (var6) ) {
               if (var1.contains(var22)) {
                  var5.add(var22);
               }
            }
         }
      }

      if (var3.getAxis() == Axis.X) {
         BlockPos var12 = new BlockPos(((BlockPos)var1.get(0)).getX(), var4.getY(), var4.getZ());
         var7.add(var12);
         var7.add(var12.up());
         var7.add(var12.up().up());
         var7.add(var12.north());
         var7.add(var12.north().up());
         var7.add(var12.north().up().up());
         var7.add(var12.south());
         var7.add(var12.south().up());
         var7.add(var12.south().up().up());
         if (var2 == 0) {
            for (BlockPos var23 : (java.util.Collection<BlockPos>) (var7) ) {
               var6.add(var23.south(2));
               var6.add(var23.south(-2));
            }

            for (BlockPos var24 : var1) {
               if (!var6.contains(var24)) {
                  var5.add(var24);
               }
            }
         } else {
            for (BlockPos var25 : (java.util.Collection<BlockPos>) (var7) ) {
               var6.add(var25.south(var8 * 2 * var2));
            }

            for (BlockPos var26 : (java.util.Collection<BlockPos>) (var6) ) {
               if (var1.contains(var26)) {
                  var5.add(var26);
               }
            }
         }
      }

      return var5.isEmpty() ? null : (BlockPos)var5.get(this.getRNG().nextInt(var5.size()));
   }

   void handleNearbyPlayerTick(UUID var1) {
      if (!this.canStoreInventory(var1, false)) {
         this.handleNearbyPlayer();
      }
   }

   void handleNearbyPlayer() {
      EntityPlayer var1 = this.world.getClosestPlayerToEntity(this, 15.0);
      if (this.hasMaster() && var1 != null && var1.getDistance(this) < 2.0F && ((String)this.entityDataManager.get(MASTER)).equals(var1.getPersistentID().toString())) {
         this.getNavigator().clearPath();
      } else {
         if (this.ap == null
            || this.getDistance(this.ap.getX(), this.ap.getY(), this.ap.getZ()) > this.getWanderRange()
            || this.ab > 100) {
            int var2 = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(5);
            int var3 = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(5);
            int var4 = WorldUtils.a(this.world, this.getPosition().getX() + var2, this.getPosition().getZ() + var3);
            this.ap = new BlockPos(this.getPosition().getX() + var2, var4, this.getPosition().getZ() + var3);
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

   boolean canStoreInventory(UUID var1, boolean var2) {
      if (this.hasInventoryItems()) {
         return false;
      }

      if (this.isTribeChestOpen(var1, var2)) {
         this.a0 = 0;
         return true;
      }

      if (--this.a0 < 0 && this.ax) {
         this.a0 = 300;
         EntityPlayer var3 = this.world.getPlayerEntityByUUID(UUID.fromString((String)this.entityDataManager.get(MASTER)));
         EyeAndKoboldColor var4 = EyeAndKoboldColor.valueOf((String)this.entityDataManager.get(CURRENT_ACTION));
         if (var3 != null) {
            var3.sendStatusMessage(
               new TextComponentString(
                  var4.getTextColor()
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

   boolean isTribeChestOpen(UUID var1, boolean var2) {
      HashSet var3 = KoboldManager.getTribeChests(var1);
      if (var3 == null) {
         return false;
      }

      BlockPos var4 = null;

      for (BlockPos var6 : (java.util.Collection<BlockPos>) (var3) ) {
         TileEntityChest var7 = (TileEntityChest)this.world.getTileEntity(var6);
         IItemHandler var8 = var7.getSingleChestHandler();
         boolean var9 = false;

         for (int var10 = 0; var10 < this.inventory.getSlots(); var10++) {
            ItemStack var11 = this.inventory.getStackInSlot(var10);
            if (!var11.isEmpty()) {
               for (int var12 = 0; var12 < var8.getSlots(); var12++) {
                  ItemStack var13 = var8.insertItem(var12, var11, true);
                  if (var13.getCount() != var11.getCount()) {
                     var9 = true;
                     break;
                  }
               }

               if (var9) {
                  break;
               }
            }
         }

         if (var9) {
            if (var4 == null) {
               var4 = var6;
            } else if (this.getDistanceSq(var4) > this.getDistanceSq(var6)) {
               var4 = var6;
            }
         }
      }

      if (var4 == null) {
         return false;
      }

      if (!(this.getDistance(var4.getX(), var4.getY(), var4.getZ()) < 2.0)) {
         if (Math.abs(var4.getY() - this.getPosition().getY()) > 4) {
            if (!var2) {
               return false;
            }

            this.syncTribeBlocks(var4);
         } else {
            PathNavigate var15 = this.getNavigator();
            BlockPos var17 = this.findStandPos(var4);
            var15.tryMoveToXYZ(var17.getX(), var17.getY(), var17.getZ(), 0.35F);
            if (var15.getPath() == null) {
               if (!var2) {
                  return false;
               }

               this.syncTribeBlocks(var4);
            }
         }

         return true;
      } else {
         TileEntityChest var14 = (TileEntityChest)this.world.getTileEntity(var4);
         IItemHandler var16 = var14.getSingleChestHandler();

         for (int var18 = 0; var18 < this.inventory.getSlots(); var18++) {
            ItemStack var19 = this.inventory.getStackInSlot(var18);
            if (!var19.isEmpty()) {
               for (int var20 = 0; var20 < var16.getSlots(); var20++) {
                  ItemStack var21 = var16.insertItem(var20, var19, false);
                  if (var21.getCount() <= 0) {
                     this.inventory.setStackInSlot(var18, ItemStack.EMPTY);
                     break;
                  }

                  this.inventory.setStackInSlot(var18, var21);
                  var19 = var21;
               }
            }
         }

         this.world.playSound(null, var4, SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
         return true;
      }
   }

   boolean assignTaskToKobold(UUID var1, KoboldTask var2) {
      List var3 = KoboldManager.getTribeMembersList(var1);
      Collection var4 = KoboldManager.getTribeTasks(var1);
      KoboldEntity var5 = null;
      Vec3d var6 = new Vec3d(var2.getTargetPos().getX(), var2.getTargetPos().getY(), var2.getTargetPos().getZ());

      for (KoboldEntity var8 : (java.util.Collection<KoboldEntity>) (var3) ) {
         boolean var9 = false;

         for (KoboldTask var11 : (java.util.Collection<KoboldTask>) (var4) ) {
            if (var11.hasWorker(var8)) {
               var9 = true;
               break;
            }
         }

         if (!var9 && var8.getInteractionPlayerUUID() == null) {
            if (var5 == null) {
               var5 = var8;
            } else if (var5.getPositionVector().distanceTo(var6) > var8.getPositionVector().distanceTo(var6)) {
               var5 = var8;
            }
         }
      }

      return this.equals(var5);
   }

   void navigateToTask(UUID var1, KoboldTask var2, BlockPos var3) {
      if (this.ad == null) {
         this.aR = 24;
         this.cooldownTicks = 0;
         this.setCurrentAction(Action.NULL);
         this.entityDataManager.set(IS_ANCHORED, false);
         EntityPlayer var6 = this.getMasterPlayer();
         HashSet var7 = var2.getMiningTargets();
         if (var6 != null && !var7.isEmpty()) {
            PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var7, false), (EntityPlayerMP)var6);
         }

         KoboldManager.setLeaderKobold(var1, this);
      } else {
         switch (this.ad.getMetadata()) {
            case 3:
            case 5:
               this.world
                  .setBlockState(
                     var3,
                     Blocks.SAPLING
                        .getStateForPlacement(
                           this.world,
                           var3,
                           EnumFacing.NORTH,
                           var3.getX(),
                           var3.getY(),
                           var3.getZ(),
                           this.ad.getMetadata(),
                           this,
                           EnumHand.MAIN_HAND
                        )
                  );
               this.world
                  .setBlockState(
                     var3.north(),
                     Blocks.SAPLING
                        .getStateForPlacement(
                           this.world,
                           var3.north(),
                           EnumFacing.NORTH,
                           var3.getX(),
                           var3.getY(),
                           var3.getZ() + 1,
                           this.ad.getMetadata(),
                           this,
                           EnumHand.MAIN_HAND
                        )
                  );
               this.world
                  .setBlockState(
                     var3.west(),
                     Blocks.SAPLING
                        .getStateForPlacement(
                           this.world,
                           var3.west(),
                           EnumFacing.NORTH,
                           var3.getX() + 1,
                           var3.getY(),
                           var3.getZ(),
                           this.ad.getMetadata(),
                           this,
                           EnumHand.MAIN_HAND
                        )
                  );
               this.world
                  .setBlockState(
                     var3.north().west(),
                     Blocks.SAPLING
                        .getStateForPlacement(
                           this.world,
                           var3.north().west(),
                           EnumFacing.NORTH,
                           var3.getX() + 1,
                           var3.getY(),
                           var3.getZ() + 1,
                           this.ad.getMetadata(),
                           this,
                           EnumHand.MAIN_HAND
                        )
                  );
               break;
            default:
               this.world
                  .setBlockState(
                     var3,
                     Blocks.SAPLING
                        .getStateForPlacement(
                           this.world,
                           var3,
                           EnumFacing.NORTH,
                           var3.getX(),
                           var3.getY(),
                           var3.getZ(),
                           this.ad.getMetadata(),
                           this,
                           EnumHand.MAIN_HAND
                        )
                  );
         }

         this.aR = 24;
         this.cooldownTicks = 0;
         this.ad = null;
         this.setCurrentAction(Action.NULL);
         this.setAnchored(false);
         EntityPlayer var4 = this.getMasterPlayer();
         HashSet var5 = var2.getMiningTargets();
         if (var4 != null && !var5.isEmpty()) {
            PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var5, false), (EntityPlayerMP)var4);
         }

         KoboldManager.setLeaderKobold(var1, this);
      }
   }

   void startMiningTask(UUID var1, BlockPos var2, KoboldTask var3) {
      if (this.getCurrentAction() != Action.MINE) {
         this.mineBlockAt(var2, var1);
      } else {
         this.cooldownTicks--;
         if (this.cooldownTicks <= 0) {
            if (this.cooldownTicks == 0) {
               PacketHandler.networkWrapper.sendToAllAround(new ResetControllerPacket(this.getGirlId()), this.getTargetNetworkPoint());
            }

            if (this.world.getBlockState(var2).getBlock() == Blocks.AIR) {
               this.navigateToTask(var1, var3, var2);
            } else {
               this.aR--;
               if (this.aR < 0) {
                  this.aR = 24;
                  this.cooldownTicks = 78;
                  HashSet var4 = new HashSet();
                  EntityPlayer var5 = this.getMasterPlayer();

                  for (BlockPos var7 : var3.getMiningTargets()) {
                     if (this.world.getBlockState(var7).getBlock() != Blocks.AIR) {
                        if (var7.getX() != var2.getX() || var7.getZ() != var2.getZ()) {
                           try {
                              ItemStack var8 = this.world
                                 .getBlockState(var7)
                                 .getBlock()
                                 .getItem(this.world, var2, this.world.getBlockState(var2));
                              if (var8.getItem() != Items.AIR) {
                                 this.canExtractItem(var8);
                              }
                           } catch (IllegalArgumentException var13) {
                              Main.LOGGER
                                 .error(
                                    "Couldn't get an item out of the block that a kobold just destroyed when falling a tree. As a result, the block wasn't added into the kobolds inventory. If you see this message, pls tell trol about it and send her the following stacktrace. Do you maybe remember what block the kobold just removed? Stacktrace follwing:"
                                 );
                              Main.LOGGER.warn("block in question: " + this.world.getBlockState(var7).getBlock().getTranslationKey());
                              Main.LOGGER.error(var13.getMessage());
                           }

                           this.ad = this.getBlockItem(var7);
                           this.world.destroyBlock(var7, false);
                           var3.removeMiningTarget(var7);
                           var3.setMiningTargets(var4);
                           var4.add(var7);
                           if (var5 != null) {
                              PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var4, false), (EntityPlayerMP)var5);
                           }

                           return;
                        }
                     } else {
                        var4.add(var7);
                     }
                  }

                  try {
                     ItemStack var15 = this.world
                        .getBlockState(var2)
                        .getBlock()
                        .getItem(this.world, var2, this.world.getBlockState(var2));
                     if (var15.getItem() != Items.AIR) {
                        this.canExtractItem(var15);
                     }
                  } catch (IllegalArgumentException var14) {
                     Main.LOGGER
                        .error(
                           "Couldn't get an item out of the block that a kobold just destroyed when falling a tree. As a result, the block wasn't added into the kobolds inventory. If you see this message, pls tell trol about it and send her the following stacktrace. Do you maybe remember what block the kobold just removed? Stacktrace follwing:"
                        );
                     Main.LOGGER.warn("block in question: " + this.world.getBlockState(var2).getBlock().getTranslationKey());
                     Main.LOGGER.error(var14.getMessage());
                  }

                  this.ad = this.getBlockItem(var2);
                  this.world.destroyBlock(var2, false);
                  int var16 = 0;

                  for (BlockPos var19 : var3.getMiningTargets()) {
                     if (this.world.getBlockState(var19).getBlock() instanceof BlockLog) {
                        var16++;
                     }
                  }

                  HashSet var18 = new HashSet();

                  for (int var20 = 0; var20 < var16; var20++) {
                     var18.add(var2.add(0, var20, 0));
                  }

                  HashSet var21 = new HashSet();

                  for (BlockPos var10 : var3.getMiningTargets()) {
                     if (!var18.contains(var10)) {
                        var21.add(var10);
                     }
                  }

                  if (!var21.isEmpty() && var5 != null) {
                     PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var21, false), (EntityPlayerMP)var5);
                  }

                  int var22 = 1;

                  while (true) {
                     BlockPos var23 = var2.add(0, var22, 0);
                     IBlockState var11 = this.world.getBlockState(var23);
                     if (this.world.getBlockState(var23).getBlock() instanceof BlockLog) {
                        this.world.destroyBlock(var23, false);
                        EntityFallingBlock var12 = new EntityFallingBlock(
                           this.world, var23.getX() + 0.5, var23.getY(), var23.getZ() + 0.5, var11
                        );
                        var12.fallTime = 1;
                        this.world.spawnEntity(var12);
                     }

                     if (!var3.getMiningTargets().contains(var23)) {
                        return;
                     }

                     var22++;
                  }
               }
            }
         }
      }
   }

   ItemStack getBlockItem(BlockPos var1) {
      ItemStack var2;
      try {
         var2 = this.world.getBlockState(var1).getBlock().getItem(this.world, var1, this.world.getBlockState(var1));
      } catch (IllegalArgumentException var5) {
         Main.LOGGER
            .error(
               "Couldn't turn a wooden block into an item to get its meta data. As a result the kobold is just gonna plant a oak saplinig instead. If you see this message, pls tell trol about it and send her the following stacktrace. Do you maybe remember what block the kobold just removed? Stacktrace follwing:"
            );
         Main.LOGGER.warn("block in question: " + this.world.getBlockState(var1).getBlock().getTranslationKey());
         Main.LOGGER.error(var5.getMessage());
         return new ItemStack(Blocks.SAPLING, 1, 0);
      }

      int var3 = ItemBlock.getIdFromItem(var2.getItem());
      int var4 = var2.getItem().getMetadata(var2);
      if (var3 == 17 && var4 == 1) {
         return new ItemStack(Blocks.SAPLING, 1, 1);
      } else if (var3 == 17 && var4 == 2) {
         return new ItemStack(Blocks.SAPLING, 1, 2);
      } else if (var3 == 17 && var4 == 3) {
         return new ItemStack(Blocks.SAPLING, 1, 3);
      } else if (var3 == 162 && var4 == 0) {
         return new ItemStack(Blocks.SAPLING, 1, 4);
      } else {
         return var3 == 162 && var4 == 1 ? new ItemStack(Blocks.SAPLING, 1, 5) : new ItemStack(Blocks.SAPLING, 1, 0);
      }
   }

   void mineBlockAt(BlockPos var1, UUID var2) {
      BlockPos var3 = null;
      ArrayList var4 = new ArrayList();
      if (this.world.getBlockState(var1.north().down()).isFullCube()
         && !this.world.getBlockState(var1.north()).isFullBlock()) {
         var4.add(var1.north());
      }

      if (this.world.getBlockState(var1.east().down()).isFullCube()
         && !this.world.getBlockState(var1.east()).isFullBlock()) {
         var4.add(var1.east());
      }

      if (this.world.getBlockState(var1.south().down()).isFullCube()
         && !this.world.getBlockState(var1.south()).isFullBlock()) {
         var4.add(var1.south());
      }

      if (this.world.getBlockState(var1.west().down()).isFullCube()
         && !this.world.getBlockState(var1.west()).isFullBlock()) {
         var4.add(var1.west());
      }

      for (BlockPos var6 : (java.util.Collection<BlockPos>) (var4) ) {
         if (var3 == null) {
            var3 = var6;
         } else {
            double var7 = new Vec3d(var3.getX() + 0.5F, var3.getY(), var3.getZ() + 0.5F).distanceTo(this.getPositionVector());
            double var9 = new Vec3d(var6.getX() + 0.5F, var6.getY(), var6.getZ() + 0.5F).distanceTo(this.getPositionVector());
            if (var9 < var7) {
               var3 = var6;
            }
         }
      }

      if (var3 == null) {
         KoboldManager.setLeaderKobold(var2, this);
         EntityPlayer var13 = this.getMasterPlayer();
         if (var13 != null) {
            var13.sendStatusMessage(new TextComponentString("Your kobolds cannot fall this tree because it starts underground"), true);
         }
      } else if (!(this.getPosition().getDistance(var3.getX(), var3.getY(), var3.getZ()) > 1.0)) {
         float var11 = 0.0F;
         if (var3.subtract(var1).equals(new BlockPos(0, 0, -1))) {
            var11 = 0.0F;
         }

         if (var3.subtract(var1).equals(new BlockPos(1, 0, 0))) {
            var11 = 90.0F;
         }

         if (var3.subtract(var1).equals(new BlockPos(0, 0, 1))) {
            var11 = 180.0F;
         }

         if (var3.subtract(var1).equals(new BlockPos(-1, 0, 0))) {
            var11 = -90.0F;
         }

         this.setTargetPosition(new Vec3d(var3.getX() + 0.5, var3.getY(), var3.getZ() + 0.5));
         this.setYawRotation(var11);
         this.entityDataManager.set(IS_ANCHORED, true);
         this.entityDataManager.set(at, true);
         this.setCurrentAction(Action.MINE);
         this.world.destroyBlock(var3.up(), false);
      } else if (Math.abs(this.getPosition().getY() - var3.getY()) > 4) {
         this.syncTribeBlocks(var3);
      } else {
         BlockPos var12 = this.findStandPos(var3);
         this.getNavigator().tryMoveToXYZ(var12.getX() + 0.5, var12.getY(), var12.getZ() + 0.5, 0.35);
         this.tickPathVelocity();
      }
   }

   void handleModelSync() {
      if (!this.aA) {
         Optional var1 = (Optional)this.entityDataManager.get(aL);
         if (var1.isPresent()) {
            this.entityDataManager.set(CURRENT_ACTION, KoboldManager.getTribeColor((UUID)var1.get()).toString());
         }
      }
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

   public void onDeath(DamageSource var1) {
      super.onDeath(var1);
      if (!this.world.isRemote) {
         Optional var2 = (Optional)this.entityDataManager.get(aL);
         if (var2.isPresent()) {
            UUID var3 = (UUID)var2.get();
KoboldManager.setTribeLeader(var3, this);
            if (this.hasMaster()) {
               EntityPlayer var4 = this.world.getPlayerEntityByUUID(UUID.fromString((String)this.getDataManager().get(MASTER)));
               if (var4 != null) {
                  var4.sendMessage(
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
   protected Action getNextAction(Action var1) {
      if (var1 == Action.SUCKBLOWJOB_BLINK) {
         return Action.THRUSTBLOWJOB;
      } else {
         return var1 == Action.KOBOLD_ANAL_SLOW ? Action.KOBOLD_ANAL_FAST : null;
      }
   }

   @Override
   protected Action getCumAction(Action var1) {
      if (var1 == Action.THRUSTBLOWJOB || var1 == Action.SUCKBLOWJOB_BLINK) {
         return Action.CUMBLOWJOB;
      } else if (var1 == Action.KOBOLD_ANAL_SLOW || var1 == Action.KOBOLD_ANAL_FAST) {
         return Action.KOBOLD_ANAL_CUM;
      } else {
         return var1 != Action.MATING_PRESS_HARD && var1 != Action.MATING_PRESS_SOFT ? null : Action.MATING_PRESS_CUM;
      }
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setFloat("body_size", (Float)this.entityDataManager.get(aE));
      var1.setInteger("eyeColorX", ((BlockPos)this.entityDataManager.get(ACTION_TARGET_POS)).getX());
      var1.setInteger("eyeColorY", ((BlockPos)this.entityDataManager.get(ACTION_TARGET_POS)).getY());
      var1.setInteger("eyeColorZ", ((BlockPos)this.entityDataManager.get(ACTION_TARGET_POS)).getZ());
      var1.setString("model", (String)this.entityDataManager.get(APPEARANCE_DNA));
      var1.setString("name", (String)this.entityDataManager.get(KOBOLD_NAME));
      var1.setString("master", (String)this.entityDataManager.get(MASTER));
      var1.setTag("inventory", this.inventory.serializeNBT());
      var1.setString("bodyColor", (String)this.entityDataManager.get(CURRENT_ACTION));
      var1.setBoolean("editedColorManually", this.aA);
      Optional var2 = (Optional)this.entityDataManager.get(aL);
      if (var2.isPresent()) {
         var1.setUniqueId("tribeId", (UUID)var2.get());
         var1.setBoolean("isLeader", KoboldManager.isTribeMember((UUID)var2.get(), this));
         var1.setString("tribeName", (String)this.entityDataManager.get(aU));
      }
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      String var2 = var1.getString("model");
      if (!"".equals(var2)) {
         this.entityDataManager.set(APPEARANCE_DNA, var2);
      }

      BlockPos var3 = new BlockPos(var1.getInteger("eyeColorX"), var1.getInteger("eyeColorY"), var1.getInteger("eyeColorZ"));
      if (!BlockPos.ORIGIN.equals(var3)) {
         this.entityDataManager.set(ACTION_TARGET_POS, var3);
      }

      this.entityDataManager.set(aE, var1.getFloat("body_size"));
      this.entityDataManager.set(KOBOLD_NAME, var1.getString("name"));
      this.entityDataManager.set(MASTER, var1.getString("master"));
      this.inventory.deserializeNBT(var1.getCompoundTag("inventory"));
      String var4 = var1.getString("bodyColor");
      if (!"".equals(var4)) {
         this.entityDataManager.set(CURRENT_ACTION, var1.getString("bodyColor"));
      }

      this.aA = var1.getBoolean("editedColorManually");
      UUID var5 = var1.getUniqueId("tribeId");
      if (var5 != null && !this.isDead) {
         this.entityDataManager.set(aL, Optional.of(var5));
         if (!KoboldManager.doesTribeExist(var5)) {
            KoboldManager.setTribeColor(var5, EyeAndKoboldColor.valueOf((String)this.entityDataManager.get(CURRENT_ACTION)));
         }

         KoboldManager.addTribeMember(var5, this);
         if (var1.getBoolean("isLeader")) {
            KoboldManager.isTribeMember(var5, this);
         }

         this.entityDataManager.set(aU, var1.getString("tribeName"));
      }
   }

   @Override
   public boolean isBlockedByCeiling() {
      if (this.isLocallyRegistered()) {
         return false;
      }

      Block var1 = this.world.getBlockState(this.getPosition().add(0, 1, 0)).getBlock();
      return !var1.isPassable(this.world, this.getPosition().add(0, 1, 0));
   }

   boolean hasInventoryItems() {
      for (int var1 = 0; var1 < this.inventory.getSlots(); var1++) {
         if (!this.inventory.getStackInSlot(var1).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   boolean canAssignTask(KoboldTask var1) {
      ArrayList var2 = new ArrayList();

      for (BlockPos var4 : var1.getMiningTargets()) {
         try {
            IBlockState var5 = this.world.getBlockState(var4);
            ItemStack var6 = var5.getBlock().getItem(this.world, var4, var5);
            var2.add(var6);
         } catch (IllegalArgumentException var7) {
         }
      }

      return this.canStoreItems(var2);
   }

   boolean canInsertItem(ItemStack var1) {
      return this.canInsertItemStack(this.inventory, var1, true, false);
   }

   boolean canStoreItems(List<ItemStack> var1) {
      ItemStackHandler var2 = new ItemStackHandler(this.inventory.getSlots());

      for (int var3 = 0; var3 < var2.getSlots(); var3++) {
         var2.setStackInSlot(var3, this.inventory.getStackInSlot(var3));
      }

      for (ItemStack var4 : var1) {
         if (!this.canInsertItemStack(var2, var4, true, false)) {
            return false;
         }
      }

      return true;
   }

   boolean canExtractItem(ItemStack var1) {
      return this.canInsertItemStack(this.inventory, var1, false, true);
   }

   boolean canInsertItemStack(ItemStackHandler var1, ItemStack var2, boolean var3, boolean var4) {
      for (int var5 = 0; var5 < var1.getSlots(); var5++) {
         ItemStack var6 = var1.getStackInSlot(var5);
         if (var6.getItem() == var2.getItem() && var6.getMetadata() == var2.getMetadata()) {
            int var7 = var6.getMaxStackSize();
            if (var7 > var2.getCount() + var6.getCount()) {
               if (!var3) {
                  var6.setCount(var6.getCount() + var2.getCount());
               }

               return true;
            }

            int var8 = var7 - var6.getCount();
            var6.setCount(var7);
            var2.setCount(var2.getCount() - var8);
         }
      }

      for (int var9 = 0; var9 < var1.getSlots(); var9++) {
         ItemStack var11 = var1.getStackInSlot(var9);
         if (var11.getItem() == Items.AIR) {
            if (!var3) {
               var1.setStackInSlot(var9, var2);
            }

            return true;
         }
      }

      if (var3) {
         return false;
      }

      if (!var4) {
         return false;
      }

      EntityItem var10 = new EntityItem(this.world);
      var10.setItem(var2);
      var10.setPosition(this.posX, this.posY, this.posZ);
      this.world.spawnEntity(var10);
      return false;
   }

   @Override
   public void playSoundAtVolume(SoundEvent var1, float var2) {
      float var3 = 0.25F - (Float)this.entityDataManager.get(aE);
      double var4 = var3 / 0.25F;
      float var6 = (float)RotationHelper.lerpDouble(0.9F, 1.1F, var4);
      this.playSoundAtPosition(var1, var2, var6);
   }

   @Override
   public void playSound(SoundEvent var1) {
      this.playSoundAtVolume(var1, 1.0F);
   }

   void playRandomSounds(SoundEvent[] var1) {
      this.playRandomSound(var1, 1.0F);
   }

   void playRandomSound(SoundEvent[] var1, float var2) {
      this.playSoundAtVolume(var1[this.getRNG().nextInt(var1.length)], var2);
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      if (this.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      float var2 = 0.25F - (Float)this.getDataManager().get(aE);
      GeckoLibCache.getInstance().parser.setValue("size", var2);
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.kobold.null", true, var1);
            } else {
               this.createAnimation("animation.kobold.blink", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.kobold.null", true, var1);
            } else if (this.isRiding()) {
               this.createAnimation("animation.kobold.sit", true, var1);
            } else {
               double var5 = Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ);
               if (!(Boolean)this.entityDataManager.get(IS_ANCHORED) && var5 > 0.0) {
                  if (this.onGround && Math.abs(Math.abs(this.prevPosY) - Math.abs(this.posY)) < 0.1F) {
                     this.rotationYaw = this.rotationYawHead;
                     double var9 = 1.0 + var2 * 2.0F;
                     this.movementController.setAnimationSpeed(var9);
                     if (this.isBlockedByCeiling()) {
                        this.createAnimation("animation.kobold.crouch_walk", true, var1);
                     } else if ((Boolean)this.entityDataManager.get(aC)) {
                        this.createAnimation("animation.kobold.run_armed", true, var1);
                     } else if (var5 > 0.2F) {
                        this.createAnimation("animation.kobold.run", true, var1);
                     } else {
                        this.createAnimation("animation.kobold.walk", true, var1);
                     }
                  } else {
                     this.createAnimation("animation.kobold.fly", true, var1);
                  }
               } else if (this.isBlockedByCeiling()) {
                  this.createAnimation("animation.kobold.crouch_idle", true, var1);
               } else {
                  this.createAnimation(this.entityDataManager.get(aC) ? "animation.kobold.idle_armed" : "animation.kobold.idle", true, var1);
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.kobold.null", true, var1);
                  break;
               case ATTACK:
                  this.createAnimation("animation.kobold.attack", false, var1);
                  break;
               case RIDE:
               case SIT:
                  this.createAnimation("animation.kobold.sit", true, var1);
                  break;
               case MINE:
                  this.createAnimation("animation.kobold.fall_tree", true, var1);
                  break;
               case PAYMENT:
                  this.createAnimation("animation.kobold.paymentBackpack", true, var1);
                  break;
               case STARTBLOWJOB:
                  this.createAnimation("animation.kobold.blowjobStart", false, var1);
                  break;
               case SUCKBLOWJOB_BLINK:
                  String var7 = this.WildSlimeFaceLayer ? "R" : "L";
                  String var8 = this.aT ? "Switch" : "";
                  this.createAnimation("animation.kobold.blowjobSlow" + var7 + var8, true, var1);
                  break;
               case THRUSTBLOWJOB:
                  this.createAnimation("animation.kobold.blowjobFast", true, var1);
                  break;
               case CUMBLOWJOB:
                  this.createAnimation("animation.kobold.blowjobCum", false, var1);
                  break;
               case KOBOLD_ANAL_START:
                  this.createAnimation("animation.kobold.analStart", false, var1);
                  break;
               case KOBOLD_ANAL_SLOW:
                  this.createAnimation("animation.kobold.analSoft", true, var1);
                  break;
               case KOBOLD_ANAL_FAST:
                  this.createAnimation("animation.kobold.analHard", true, var1);
                  break;
               case KOBOLD_ANAL_CUM:
                  this.createAnimation("animation.kobold.analCum", true, var1);
                  break;
               case SLEEP:
                  this.createAnimation("animation.kobold.sleep", true, var1);
                  break;
               case MATING_PRESS_START:
                  this.createAnimation("animation.kobold.mating_press_start", false, var1);
                  break;
               case MATING_PRESS_SOFT:
                  this.createAnimation("animation.kobold.mating_press_soft", true, var1);
                  break;
               case MATING_PRESS_HARD:
                  this.createAnimation("animation.kobold.mating_press_hard", true, var1);
                  break;
               case MATING_PRESS_CUM:
                  this.createAnimation("animation.kobold.mating_press_cum", true, var1);
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
                  BeeScreen.enableInteraction();
               }
               break;
            case "paymentDone":
               if (this.isControlledByLocalPlayer()) {
                  this.U();
               }
               break;
            case "blowjobStartMSG1":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var11 = Minecraft.getMinecraft().player;
                  Vec3d var13 = VectorMath.rotateByYaw(new Vec3d(0.0, 0.625 - var11.getEyeHeight(), -1.0), this.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(this.getInteractionPlayerUUID().toString(), this.getTargetPosition().add(var13), this.getYawRotation() + 180.0F, 0.0F)
                     );
               }
               break;
            case "blowjobStartMSG2":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var10 = Minecraft.getMinecraft().player;
                  Vec3d var12 = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5 - var10.getEyeHeight(), -0.6875), this.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(
                           this.getInteractionPlayerUUID().toString(), this.getTargetPosition().add(var12), this.getYawRotation() + 180.0F - 40.0F, 0.0F
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
               this.aT = false;
               this.WildSlimeFaceLayer = true;
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "switch":
               this.aT = this.getRNG().nextBoolean();
               this.actionController.clearAnimationCache();
               break;
            case "endSwitch":
               this.aT = false;
               this.WildSlimeFaceLayer = !this.WildSlimeFaceLayer;
               this.actionController.clearAnimationCache();
               break;
            case "blowjobFastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
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
                  EntityPlayerSP var9 = Minecraft.getMinecraft().player;
                  Vec3d var5 = VectorMath.rotateByYaw(new Vec3d(0.0, 0.5625 - var9.getEyeHeight(), 0.5625), this.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(new TeleportPlayerPacket(this.getInteractionPlayerUUID().toString(), this.getTargetPosition().add(var5), this.getYawRotation(), 0.0F));
               }
               break;
            case "pounding":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               break;
            case "analFastRapid":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
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
               this.aN--;
               if (this.aN <= 0) {
                  this.aN = 3;
                  this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MOAN);
               }
               break;
            case "analHardMSG1":
               this.aN--;
               if (this.aN <= 0) {
                  this.aN = 4;
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
               int var6 = this.getRNG().nextInt(3);
               this.playSound(SoundHandler.GIRLS_KOBOLD_LIGHTBREATHING[var6]);
               break;
            case "matingCam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var8 = Minecraft.getMinecraft().player;
                  Vec3d var16 = new Vec3d(0.0, 0.4375 - var8.eyeHeight, -0.6875);
                  var16 = VectorMath.rotateByYaw(var16, this.getYawRotation() + 180.0F);
                  var16 = var16.add(this.getTargetPosition());
                  PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(var8.getPersistentID().toString(), var16, this.getYawRotation() + 180.0F, 10.0F));
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

               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.MATING_PRESS_HARD);
               }
               break;
            case "mating_press_hardReady":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "mating_cum_cam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var4 = Minecraft.getMinecraft().player;
                  Vec3d var7 = new Vec3d(0.0, 1.1875 - var4.eyeHeight, 0.125);
                  var7 = VectorMath.rotateByYaw(var7, this.getYawRotation() + 180.0F);
                  var7 = var7.add(this.getTargetPosition());
                  PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(var4.getPersistentID().toString(), var7, this.getYawRotation() + 180.0F, 70.0F));
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
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
   }

   public int getSizeInventory() {
      return 27;
   }

   public boolean isEmpty() {
      return false;
   }

   public ItemStack getStackInSlot(int var1) {
      return var1 >= this.inventory.getSlots() ? ItemStack.EMPTY : this.inventory.getStackInSlot(var1);
   }

   public ItemStack decrStackSize(int var1, int var2) {
      return this.inventory.extractItem(var1, var2, false);
   }

   public ItemStack removeStackFromSlot(int var1) {
      return this.inventory.extractItem(var1, this.inventory.getStackInSlot(var1).getCount(), false);
   }

   public void setInventorySlotContents(int var1, ItemStack var2) {
      this.inventory.setStackInSlot(var1, var2);
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public void markDirty() {
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return true;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      return true;
   }

   public int getField(int var1) {
      return var1;
   }

   public void setField(int var1, int var2) {
   }

   public int getFieldCount() {
      return 0;
   }

   public void clear() {
   }

   public static class c {
      int tickCounter = 0;

      @SubscribeEvent
      public void onLivingDeath(LivingDeathEvent var1) {
         if (var1.getEntityLiving() instanceof KoboldEntity) {
            KoboldEntity var2 = (KoboldEntity)var1.getEntityLiving();
            if (var2.world.isRemote) {
               return;
            }

            for (int var3 = 0; var3 < var2.inventory.getSlots(); var3++) {
               ItemStack var4 = var2.inventory.getStackInSlot(var3);
               if (var4.getItem() != Items.AIR) {
                  var2.dropItem(var4.getItem(), var4.getCount());
               }
            }
         }
      }

      @SubscribeEvent
      public void onLivingHurtPlayer(LivingHurtEvent var1) {
         Entity var2 = var1.getEntity();
         World var3 = var2.getEntityWorld();
         if (!var3.isRemote) {
            if (var2 instanceof KoboldEntity) {
               KoboldEntity var4 = (KoboldEntity)var2;
               Optional var5 = (Optional)var4.getDataManager().get(KoboldEntity.aL);
               if (var5.isPresent()) {
                  Entity var6 = var1.getSource().getTrueSource();
                  if (var6 != null) {
                     if (var6 instanceof EntityLivingBase) {
                        if (var6 instanceof EntityPlayer) {
                           EntityPlayer var7 = (EntityPlayer)var6;
                           if (var7.capabilities.isCreativeMode) {
                              return;
                           }

                           if (var7.equals(var4.getMasterPlayer())) {
                              return;
                           }
                        }

                        EntityPlayer var8 = var4.getMasterPlayer();
                        if (var8 != null) {
                           var8.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Your Tribe is under Attack!"), true);
                        }

                        KoboldManager.addCombatant((UUID)var5.get(), (EntityLivingBase)var6);
                     }
                  }
               }
            }
         }
      }

      @SubscribeEvent
      public void onWorldUnload(Unload var1) {
         try {
            for (BaseGirlEntity var3 : BaseGirlEntity.getGirlEntityList()) {
               if (var3 instanceof KoboldEntity) {
                  KoboldEntity var4 = (KoboldEntity)var3;
                  Optional var5 = (Optional)var4.getDataManager().get(KoboldEntity.aL);
                  if (var5.isPresent() && KoboldManager.isTribeMember((UUID)var5.get(), var4)) {
                     var4.teleportToHome((UUID)var5.get());
                  }
               }
            }
         } catch (ConcurrentModificationException var6) {
         }
      }

      @SubscribeEvent
      public void onLivingHurtCancel(LivingHurtEvent var1) {
         if (var1.getSource() == DamageSource.IN_WALL) {
            Entity var2 = var1.getEntity();
            if (var2 instanceof KoboldEntity) {
               var2.setPosition(var2.posX, var2.posY + 1.0, var2.posZ);
               var1.setCanceled(true);
            }
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onClientTick(ClientTickEvent var1) {
         WorldClient var2 = Minecraft.getMinecraft().world;
         if (var2 != null) {
            if (++this.tickCounter % 20 == 0) {
               PacketHandler.networkWrapper.sendToServer(new GetTribeUiValuesPacket());
            }
         }
      }

   }
}
