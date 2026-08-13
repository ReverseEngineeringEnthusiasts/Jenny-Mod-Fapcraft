package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.IGalathFinish;
import com.trolmastercard.sexmod.api.IPositionProvider;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.GirlInventoryScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.entity.api.IEllie;
import com.trolmastercard.sexmod.item.LunaRodItem;
import com.trolmastercard.sexmod.networking.CatActivateFishingPacket;
import com.trolmastercard.sexmod.networking.CatEatingDonePacket;
import com.trolmastercard.sexmod.networking.CatThrowAwayItemPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SendChatMessagePacket;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SendGirlToSexPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.DebugMode;
import com.trolmastercard.sexmod.util.GalathGeometryRender;
import com.trolmastercard.sexmod.util.GirlCombatProtection;
import com.trolmastercard.sexmod.util.EntityLookVectorHelper;
import com.trolmastercard.sexmod.util.GoblinFirstPersonRenderer;
import com.trolmastercard.sexmod.util.TrailSegment;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.IBeddableSexGirl;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
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
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class LunaEntity extends AbstractGirlNpcEntity implements IEllie, IBeddableSexGirl {
   public ItemStack ao = new ItemStack(LunaRodItem.LUNA_ROD);
   public static final DataParameter<Float> yFlag = EntityDataManager.createKey(LunaEntity.class, DataSerializers.FLOAT)
      .getSerializer()
      .createKey(121);
   public static final DataParameter<ItemStack> az = EntityDataManager.createKey(LunaEntity.class, DataSerializers.ITEM_STACK)
      .getSerializer()
      .createKey(120);
   public static final DataParameter<Boolean> af = EntityDataManager.createKey(LunaEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(119);
   public static final DataParameter<ItemStack> ag = EntityDataManager.createKey(LunaEntity.class, DataSerializers.ITEM_STACK)
      .getSerializer()
      .createKey(118);
   static final float ah = 3.0F;
   static final float ax = 1200.0F;
   @Nullable
   public SexEntity av;
   public float aa = 1.0F;
   public float zFlag = 0.0F;
   int aj = 8000;
   public boolean ac = false;
   int aw = 0;
   boolean ay = false;
   int ak = 0;
   int ab = 0;
   public BlockPos ai;
   int at = 0;
   int as = 0;
   boolean am;
   long al = 0L;
   boolean ar = false;
   Path au = null;
   int aq = 0;
   HashSet<BlockPos> an = new HashSet<>();
   boolean ae = false;
   boolean ad = false;

   public LunaEntity(World var1) {
      super(var1);
      this.slashSwordRot = 230;
      this.stabSwordRot = 150;
      this.holdBowRot = 320;
      this.swordOffsetStab = new Vec3d(0.0, -0.05999999718368053, 0.10000001192092894);
      if (this.inventory.getStackInSlot(0) == ItemStack.EMPTY) {
         this.inventory.setStackInSlot(0, new ItemStack(Items.IRON_AXE));
      }

      if (this.inventory.getStackInSlot(6) == ItemStack.EMPTY) {
         this.inventory.setStackInSlot(6, new ItemStack(Items.FISHING_ROD));
      }
   }

   @Override
   public String getDisplayNameText() {
      return "Luna";
   }

   @Override
   public float getScaleFactor() {
      return -0.2F;
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.entityDataManager.register(yFlag, 0.0F);
      this.entityDataManager.register(az, ItemStack.EMPTY);
      this.entityDataManager.register(af, false);
      this.entityDataManager.register(ag, ItemStack.EMPTY);
   }

   @Override
   public void onArriveHome() {
      this.sendChatMessage("Love it here owo");
      this.playRandomSound(SoundHandler.GIRLS_LUNA_OWO);
   }

   @Override
   public void setCurrentAction(Action action) {
      if (this.getCurrentAction() != Action.COWGIRL_SITTING_CUM || action != Action.COWGIRL_SITTING_SLOW && action != Action.COWGIRL_SITTING_FAST) {
         if (this.getCurrentAction() != Action.TOUCH_BOOBS_CUM || action != Action.TOUCH_BOOBS_FAST && action != Action.TOUCH_BOOBS_SLOW) {
            super.setCurrentAction(action);
         }
      }
   }

   @Override
   public void setDismounted() {
      this.ac = true;
   }

   public float getEyeHeight() {
      return 1.34F;
   }

   public boolean processInteract(EntityPlayer var1, EnumHand var2) {
      if (super.processInteract(var1, var2)) {
         return true;
      }

      ItemStack var3 = var1.getHeldItem(var2);
      boolean var4 = var3.getItem() == Items.NAME_TAG;
      if (var4) {
         var3.interactWithEntity(var1, this, var2);
         return true;
      }

      if (this.world.isRemote && !this.openInteractionMenu(var1)) {
         this.sendChatMessage(I18n.format("bia.dialogue.busy", new Object[0]));
      }

      return true;
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      String[] var2 = new String[]{"action.names.sex", "action.names.touchboobs", "action.names.headpat"};
      ItemStack[] var3 = new ItemStack[]{new ItemStack(Items.FISH, 3, 0), new ItemStack(Items.FISH, 2, 1), null};
      a(var1, this, var2, var3);
      return true;
   }

   @SideOnly(Side.CLIENT)
   protected static void a(EntityPlayer var0, BaseGirlEntity var1, String[] var2, ItemStack[] var3) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(var1, var0, var2, var3, true));
   }

   public void setHeldItemStack(ItemStack var1) {
      this.entityDataManager.set(ag, var1);
   }

   @Override
   public void reinitTasks() {
      this.wanderGoal = new EntityAIWanderAvoidWater(this, 0.35);
      this.watchClosestGirlGoal = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.tasks.addTask(5, this.watchClosestGirlGoal);
      this.tasks.addTask(5, this.wanderGoal);
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (!this.hasMaster()) {
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.0);
      } else {
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
      }

      this.handleFishingPath();
      this.handleFishingIdle();
      this.entityDataManager.set(af, this.av != null && this.entityDataManager.get(ag) == ItemStack.EMPTY);
      if (this.al == this.world.getTotalWorldTime() && this.av != null) {
         this.world.removeEntity(this.av);
         this.av = null;
      }

      if (this.ay) {
         double var1 = this.getTargetPosition().distanceTo(this.getPositionVector());
         if (!(var1 < 0.5) && this.ak <= 200) {
            if (++this.ak == 60 || this.ak == 120) {
               this.getNavigator().clearPath();
               this.getNavigator().tryMoveToXYZ(this.getTargetPosition().x, this.getTargetPosition().y, this.getTargetPosition().z, 0.2);
            }
         } else {
            this.ay = false;
            this.ak = 0;
            this.entityDataManager.set(IS_ANCHORED, true);
            this.noClip = true;
            this.setNoGravity(true);
            this.motionX = 0.0;
            this.motionY = 0.0;
            this.motionZ = 0.0;
            this.setCurrentAction(Action.WAIT_CAT);
         }
      }

      if (this.ac) {
         this.aw++;
         if (!this.getPositionVector().equals(this.getTargetPosition()) && this.aw <= 40) {
            this.rotationYaw = this.getYawRotation();
            this.setNoGravity(false);
            Vec3d var3 = RotationHelper.a(this.getPositionVector(), this.getTargetPosition(), 40 - this.aw);
            this.setPosition(var3.x, var3.y, var3.z);
         } else {
            this.ac = false;
            this.aw = 0;
            this.setYawRotation(this.world.getMinecraftServer().getPlayerList().getPlayerByUUID(this.getInteractionPlayerUUID()).rotationYaw + 180.0F);
            this.entityDataManager.set(IS_ANCHORED, true);
            this.getNavigator().clearPath();
            this.U();
         }
      }

      this.syncHeldItem();
      this.entityDataManager.set(az, this.inventory.getStackInSlot(6));
   }

   void syncHeldItem() {
      ItemStack var1 = this.ao;
      ItemStack var2 = (ItemStack)this.entityDataManager.get(az);
      if (!var2.equals(ItemStack.EMPTY)) {
         Map var3 = EnchantmentHelper.getEnchantments(var2);
         EnchantmentHelper.setEnchantments(var3, var1);
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (Action.WAIT_CAT.equals(this.getCurrentAction())) {
         this.handleNearbyPlayer();
      } else {
         this.ab = 0;
      }
   }

   void handleNearbyPlayer() {
      EntityPlayer var1 = this.world.getClosestPlayerToEntity(this, 10.0);
      if (var1 != null) {
         if (!(var1.getDistance(this) > 1.25F)) {
            if (this.world.isRemote) {
               this.a(var1, this.ab);
            } else if (this.ab == 25) {
               this.setInteractionPlayerUUID(var1.getPersistentID());
               var1.moveRelative(0.0F, 0.0F, 0.0F, 0.0F);
               var1.setPositionAndUpdate(this.getPositionVector().x, this.getPositionVector().y, this.getPositionVector().z);
               this.setCurrentAction(Action.COWGIRL_SITTING_INTRO);
               var1.setRotationYawHead(this.getYawRotation() + 180.0F);
               var1.rotationYaw = this.getYawRotation() + 180.0F;
               var1.prevRotationYaw = this.getYawRotation() + 180.0F;
               this.cameraYaw = this.getYawRotation() + 180.0F;
               this.positionPlayerRelative(0.0, -0.075F, -0.7109375, 0.0F, 0.0F);
               this.entityDataManager.set(OUTFIT_INDEX, 0);
            }

            this.ab++;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void a(EntityPlayer var1, int var2) {
      if (var2 == 0) {
         EntityPlayerSP var3 = Minecraft.getMinecraft().player;
         if (var3.getPersistentID().equals(var1.getPersistentID())) {
            BeeScreen.enableInteraction();
            var3.setVelocity(0.0, 0.0, 0.0);
            HandlePlayerMovement.setMovementLock(false);
         }
      }

      if (var2 == 25) {
         EntityPlayerSP var4 = Minecraft.getMinecraft().player;
         if (var4.getPersistentID().equals(var1.getPersistentID())) {
            Minecraft.getMinecraft().gameSettings.thirdPersonView = 2;
         }
      }
   }

   @Override
   public void goToSexBed() {
      this.entityDataManager.set(IS_ANCHORED, false);
      this.setCurrentAction(Action.NULL);
      this.ar = true;
      BlockPos var1 = this.getNearestBed(this.getPosition());
      if (var1 == null) {
         this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
         PacketHandler.networkWrapper
            .sendToAllAround(
               new SendChatMessagePacket(
                  "<" + this.getDisplayNameText() + "> Heh.. there is no bed nearby.. but I already ate the fish so nya~ hehe", this.dimension, this.getGirlId()
               ),
               this.getTargetNetworkPoint()
            );
      } else {
         Vec3d var2 = new Vec3d(var1.getX(), var1.getY(), var1.getZ());
         int[] var3 = new int[]{0, 180, -90, 90};
         Vec3d[][] var4 = new Vec3d[][]{
            {new Vec3d(0.5, 0.0, -0.5), new Vec3d(0.0, 0.0, -1.0)},
            {new Vec3d(0.5, 0.0, 1.5), new Vec3d(0.0, 0.0, 1.0)},
            {new Vec3d(-0.5, 0.0, 0.5), new Vec3d(-1.0, 0.0, 0.0)},
            {new Vec3d(1.5, 0.0, 0.5), new Vec3d(1.0, 0.0, 0.0)}
         };
         int var5 = -1;

         for (int var6 = 0; var6 < var4.length; var6++) {
            Vec3d var7 = var2.add(var4[var6][1]);
            if (this.world.getBlockState(new BlockPos(var7.x, var7.y, var7.z)).getBlock()
               == Blocks.AIR) {
               if (var5 == -1) {
                  var5 = var6;
               } else {
                  double var8 = this.getPosition()
                     .distanceSq(
                        var2.add(var4[var5][0]).x,
                        var2.add(var4[var5][0]).y,
                        var2.add(var4[var5][0]).z
                     );
                  double var10 = this.getPosition()
                     .distanceSq(
                        var2.add(var4[var6][0]).x,
                        var2.add(var4[var6][0]).y,
                        var2.add(var4[var6][0]).z
                     );
                  if (var10 < var8) {
                     var5 = var6;
                  }
               }
            }
         }

         if (var5 == -1) {
            this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
            this.sendChatMessage("Heh.. the bed is obscured.. but I already ate the fish so nya~ hehe");
            return;
         }

         Vec3d var12 = var2.add(var4[var5][0]);
         this.setYawRotation(var3[var5]);
         this.setTargetPosition(new Vec3d(var12.x, var12.y, var12.z));
         this.cameraYaw = this.getYawRotation();
         this.getNavigator().clearPath();
         this.getNavigator().tryMoveToXYZ(var12.x, var12.y, var12.z, 0.2);
         this.ay = true;
         this.ak = 0;
      }
   }

   public void dropHeldItem() {
      EntityItem var1 = new EntityItem(this.world, this.posX, this.posY, this.posZ, (ItemStack)this.entityDataManager.get(ag));
      Vec3d var2 = VectorMath.rotateByYaw(new Vec3d(0.0, 0.2F + Math.random() * 0.1F, -0.2F + Math.random() * -0.1F), this.rotationYaw);
      var1.motionX = var2.x;
      var1.motionY = var2.y;
      var1.motionZ = var2.z;
      this.world.spawnEntity(var1);
      this.entityDataManager.set(ag, ItemStack.EMPTY);
   }

   public void clearFishingState() {
      this.ai = null;
      this.at = 0;
      this.as = 0;
      this.am = false;
      this.entityDataManager.set(IS_ANCHORED, false);
      this.entityDataManager.set(ag, ItemStack.EMPTY);
      this.setSilent(false);
      this.setCurrentAction(Action.NULL);
      if (this.av != null) {
         this.world.removeEntity(this.av);
         this.av = null;
      }

      if (this.getInteractionPlayerUUID() == null) {
         this.watchClosestGirlGoal = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
         this.tasks.addTask(5, this.watchClosestGirlGoal);
         if (!this.hasMaster()) {
            this.wanderGoal = new EntityAIWanderAvoidWater(this, 0.35);
            this.tasks.addTask(5, this.wanderGoal);
         }
      }
   }

   public void onFishingTick() {
      this.clearFishingState();
      if (++this.aq >= 3) {
         this.aq = 0;
         this.aj = 0;
      }
   }

   void handleFishingIdle() {
      if (!this.hasMaster() && this.getInteractionPlayerUUID() == null && !this.ar) {
         if (!(++this.aj < 1200.0F)) {
            if (this.av != null && this.av.lureTimer == 15) {
               ((LunaRodItem)this.ao.getItem()).a(this.world, this, EnumHand.MAIN_HAND);
               this.al = this.world.getTotalWorldTime() + 20L;
               ItemStack var1 = (ItemStack)this.entityDataManager.get(ag);
               if (var1 != ItemStack.EMPTY) {
                  if (var1.getItem() instanceof ItemFood) {
                     this.setCurrentAction(Action.FISHING_EAT);
                  } else {
                     this.setCurrentAction(Action.FISHING_THROW_AWAY);
                  }
               }
            }

            if (!this.getCurrentAction().toString().toLowerCase().contains("fishing")) {
               this.findFishingSpot();
               this.handleFishingMove();
            }

            if (this.ai != null && this.au == null && this.getNavigator().getPath() == null && !this.inWater && this.onGround) {
               this.world
                  .rayTraceBlocks(
                     this.getPositionVector().add(0.0, this.getEyeHeight(), 0.0),
                     new Vec3d(this.ai.getX(), this.ai.getY(), this.ai.getZ()),
                     true
                  );
               this.setSilent(true);
               if (this.wanderGoal != null) {
                  this.tasks.removeTask(this.wanderGoal);
                  this.wanderGoal = null;
               }

               if (this.watchClosestGirlGoal != null) {
                  this.tasks.removeTask(this.watchClosestGirlGoal);
                  this.watchClosestGirlGoal = null;
               }

               if (this.getCurrentAction() == Action.NULL) {
                  this.setCurrentAction(Action.FISHING_START);
                  this.setTargetPosition(this.getPositionVector());
                  this.entityDataManager.set(IS_ANCHORED, true);
                  this.setYawRotation(
                     (float)Math.atan2(this.posZ - this.ai.getZ(), this.posX - this.ai.getX()) * (float) (180.0 / Math.PI)
                        + 90.0F
                  );
               }
            } else {
               this.au = this.getNavigator().getPath();
            }
         }
      } else {
         if ((Boolean)this.entityDataManager.get(af)) {
            this.clearFishingState();
         }
      }
   }

   public void addCaughtItem() {
      this.an.add(this.ai);
      this.clearFishingState();
   }

   void handleFishingMove() {
      if (this.ai != null) {
         PathNavigate var1 = this.getNavigator();
         var1.tryMoveToXYZ(this.ai.getX(), this.ai.getY(), this.ai.getZ(), 0.35F);
         Path var2 = var1.getPath();
         if (var2 != null) {
            if (var2.getCurrentPathLength() > var2.getCurrentPathIndex() + 1) {
               PathPoint var3 = var2.getPathPointFromIndex(var2.getCurrentPathIndex() + 1);
               PathPoint var4 = var2.getPathPointFromIndex(var2.getCurrentPathLength() - 1);
               Vec3d var5 = new Vec3d(var4.x, var4.y, var4.z);
               BlockPos var6 = new BlockPos(var3.x, var3.y, var3.z);
               if (this.getPositionVector().distanceTo(var5) < 0.75) {
                  var1.clearPath();
                  this.setPosition(var5.x, var5.y, var5.z);
               }

               if (this.world.getBlockState(var6.add(0, 1, 0)).getBlock() == Blocks.WATER) {
                  var1.clearPath();
               }

               if (this.world.getBlockState(var6).getBlock() == Blocks.WATER) {
                  var1.clearPath();
               }

               if (this.world.getBlockState(var6.add(0, -1, 0)).getBlock() == Blocks.WATER) {
                  var1.clearPath();
               }
            }
         }
      }
   }

   void findFishingSpot() {
      int var1 = 0;
      BlockPos var2 = null;
      int var3 = 0;

      while (++var1 < 50) {
         BlockPos var4 = this.findNearestStructureBlock(
            this.getPosition(),
            var1 + 1,
            Blocks.WATER,
            60,
            10,
            new HashSet<>(
               Arrays.asList(
                  Biomes.RIVER,
                  Biomes.OCEAN,
                  Biomes.DEEP_OCEAN,
                  Biomes.BEACH,
                  Biomes.STONE_BEACH,
                  Biomes.SWAMPLAND,
                  Biomes.MUTATED_SWAMPLAND
               )
            )
         );
         if (var4 == null) {
            break;
         }

         while (this.world.getBlockState(var4.add(0, 1, 0)).getBlock() == Blocks.WATER) {
            var4 = var4.add(0, 1, 0);
         }

         int var5 = 1;

         for (BlockPos var6 = var4; this.world.getBlockState(var6.add(0, -1, 0)).getBlock() == Blocks.WATER; var5++) {
            var6 = var6.add(0, -1, 0);
         }

         if (!this.an.contains(var4)) {
            if (var2 == null) {
               var2 = var4;
               var3 = var5;
            } else if (var5 > var3) {
               var2 = var4;
               var3 = var5;
               if (var3 >= 6) {
                  break;
               }
            }
         }
      }

      if (var2 != null) {
         if (this.ai == null || this.at < var3) {
            this.ai = var2;
            this.at = var3;
         }

         if (this.ai.equals(var2)) {
            this.as = 0;
         } else if (++this.as > 20) {
            this.ai = var2;
            this.at = var3;
         }
      }
   }

   void handleFishingPath() {
      Path var1 = this.getNavigator().getPath();
      if (var1 != null) {
         PathPoint var2 = var1.getFinalPathPoint();
         PathPoint var3 = new PathPoint(
            ThreadNames.roundToInt(this.posX), ThreadNames.roundToInt(this.posY), ThreadNames.roundToInt(this.posZ)
         );
         if (var2 != null) {
            this.entityDataManager.set(yFlag, var2.distanceTo(var3));
         }
      }
   }

   @Override
   public void doAction(String var1, UUID var2) {
      super.doAction(var1, var2);
      if ("action.names.touchboobs".equals(var1)) {
         this.setInteractionPlayerUUID(var2);
         this.triggerActionSync(true, true, var2);
         this.changeDataParameterFromClient("animationFollowUp", "touch_boobs");
         this.changeDataParameterFromClient("currentModel", "0");
         HandlePlayerMovement.setMovementLock(false);
      }

      if ("action.names.sex".equals(var1)) {
         this.setInteractionPlayerUUID(var2);
         this.triggerActionSync(true, true, var2);
         this.changeDataParameterFromClient("animationFollowUp", "sex");
         HandlePlayerMovement.setMovementLock(false);
      }

      if ("action.names.headpat".equals(var1)) {
         this.setInteractionPlayerUUID(var2);
         this.triggerActionSync(true, true, var2);
         HandlePlayerMovement.setMovementLock(false);
         this.changeDataParameterFromClient("animationFollowUp", "headpat");
      }
   }

   @Override
   protected Action getNextAction(Action var1) {
      if (var1 == Action.TOUCH_BOOBS_SLOW) {
         return Action.TOUCH_BOOBS_FAST;
      } else {
         return var1 == Action.COWGIRL_SITTING_SLOW ? Action.COWGIRL_SITTING_FAST : null;
      }
   }

   @Override
   protected Action getCumAction(Action var1) {
      if (var1 == Action.TOUCH_BOOBS_SLOW || var1 == Action.TOUCH_BOOBS_FAST) {
         return Action.TOUCH_BOOBS_CUM;
      } else {
         return var1 != Action.COWGIRL_SITTING_FAST && var1 != Action.COWGIRL_SITTING_SLOW ? null : Action.COWGIRL_SITTING_CUM;
      }
   }

   @Override
   protected void U() {
      switch ((String)this.entityDataManager.get(GIRL_HAND_STATES)) {
         case "touch_boobs":
            if (this.getCurrentAction() != Action.PAYMENT) {
               this.setCurrentAction(Action.PAYMENT);
               return;
            }

            this.setCurrentAction(Action.TOUCH_BOOBS_INTRO);
            break;
         case "sex":
            if (this.getCurrentAction() != Action.PAYMENT) {
               this.setCurrentAction(Action.PAYMENT);
            } else {
               PacketHandler.networkWrapper.sendToServer(new SendGirlToSexPacket(this.getGirlId()));
               PacketHandler.networkWrapper.sendToServer(new ResetGirlPacket(this.getGirlId()));
            }

            return;
         case "headpat":
            this.setCurrentAction(Action.HEAD_PAT);
      }

      if (this.world.isRemote) {
         this.changeDataParameterFromClient("animationFollowUp", "");
      } else {
         this.entityDataManager.set(GIRL_HAND_STATES, "");
      }
   }

   protected void playHurtSound(DamageSource var1) {
      this.playRandomSound(SoundHandler.GIRLS_LUNA_OUU);
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return this.getRNG().nextFloat() * 100.0F > 95.0F ? SoundHandler.GIRLS_ALLIE_SCAWY[2] : SoundHandler.GIRLS_LUNA_OUU[12];
   }

   @Override
   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.0);
   }

   protected float getJumpUpwardsMotion() {
      return this.isInWater() ? 1.0F : 0.5F;
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      if (this.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.cat.null", true, var1);
            } else {
               this.createAnimation("animation.cat.blink", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.cat.null", true, var1);
            } else if (this.isRiding()) {
               this.createAnimation("animation.cat.sit", true, var1);
            } else if (Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ) > 0.0) {
               if (this.onGround && Math.abs(Math.abs(this.prevPosY) - Math.abs(this.posY)) < 0.1F) {
                  this.createAnimation(this.entityDataManager.get(yFlag) < 3.0F ? "animation.cat.walk" : "animation.cat.run", true, var1);
               } else {
                  this.createAnimation("animation.cat.fly", true, var1);
               }

               this.rotationYaw = this.rotationYawHead;
            } else {
               this.createAnimation("animation.cat.idle" + (this.ad ? "2" : ""), true, var1);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.cat.null", true, var1);
                  break;
               case ATTACK:
                  this.createAnimation("animation.cat.attack" + this.nextAttack, false, var1);
                  break;
               case RIDE:
               case SIT:
                  this.createAnimation("animation.cat.sit", true, var1);
                  break;
               case BOW:
                  this.createAnimation("animation.cat.bowcharge", false, var1);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.cat.throwpearl", true, var1);
                  break;
               case DOWNED:
                  this.createAnimation("animation.cat.downed", true, var1);
                  break;
               case FISHING_START:
                  this.createAnimation("animation.cat.start_fishing", false, var1);
                  break;
               case FISHING_IDLE:
                  this.createAnimation("animation.cat.idle_fishing", true, var1);
                  break;
               case FISHING_EAT:
                  this.createAnimation("animation.cat.eat_fishing", false, var1);
                  break;
               case FISHING_THROW_AWAY:
                  this.createAnimation("animation.cat.throw_away", false, var1);
                  break;
               case PAYMENT:
                  this.createAnimation("animation.cat.payment", false, var1);
                  break;
               case TOUCH_BOOBS_INTRO:
                  this.createAnimation("animation.cat.touch_boobs_intro", false, var1);
                  break;
               case TOUCH_BOOBS_SLOW:
                  this.createAnimation("animation.cat.touch_boobs_slow" + (this.ae ? "1" : ""), true, var1);
                  break;
               case TOUCH_BOOBS_FAST:
                  this.createAnimation("animation.cat.touch_boobs_fast", true, var1);
                  break;
               case TOUCH_BOOBS_CUM:
                  this.createAnimation("animation.cat.touch_boobs_cum", false, var1);
                  break;
               case WAIT_CAT:
                  this.createAnimation("animation.cat.wait", false, var1);
                  break;
               case COWGIRL_SITTING_INTRO:
                  this.createAnimation("animation.cat.sitting_intro", false, var1);
                  break;
               case COWGIRL_SITTING_SLOW:
                  this.createAnimation("animation.cat.sitting_slow", true, var1);
                  break;
               case COWGIRL_SITTING_FAST:
                  this.createAnimation("animation.cat.sitting_fast", true, var1);
                  break;
               case COWGIRL_SITTING_CUM:
                  this.createAnimation("animation.cat.sitting_cum", false, var1);
                  break;
               case HEAD_PAT:
                  this.createAnimation("animation.cat.head_pat", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

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
            case "attackDone":
               this.setCurrentAction(Action.NULL);
               if (++this.nextAttack == 3) {
                  this.nextAttack = 0;
               }
               break;
            case "idleDone":
               this.ad = this.getRNG().nextInt(10) == 0;
               break;
            case "idle2Done":
               this.ad = false;
               break;
            case "pearl":
               PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               break;
            case "start_fishingDone":
               if (this.isLocalPlayerNearby()) {
                  this.setCurrentAction(Action.FISHING_IDLE);
               }
               break;
            case "rod_shoot":
               if (this.isLocalPlayerNearby()) {
                  PacketHandler.networkWrapper.sendToServer(new CatActivateFishingPacket(this.getGirlId()));
               }
               break;
            case "eat":
               this.playSoundAtPosition(
                  SoundHandler.randomSound(SoundHandler.MISC_EAT),
                  0.5F + 0.5F * this.rand.nextInt(2),
                  (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F
               );
               this.aa -= 0.33333334F;
               break;
            case "eatPay":
               this.playSoundAtPosition(
                  SoundHandler.randomSound(SoundHandler.MISC_EAT),
                  0.5F + 0.5F * this.rand.nextInt(2),
                  (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F
               );
               this.scaleFactor -= 0.33333334F;
               break;
            case "burp":
               this.playSoundAtPosition(SoundEvents.ENTITY_PLAYER_BURP, 0.5F, this.rand.nextFloat() * 0.1F + 0.9F);
               break;
            case "eatingDone":
               if (this.isLocalPlayerNearby()) {
                  PacketHandler.networkWrapper.sendToServer(new CatEatingDonePacket(this.getGirlId()));
                  this.setCurrentAction(Action.NULL);
               }

               this.aa = 1.0F;
               this.zFlag = 0.0F;
               break;
            case "throw_away":
               if (this.isLocalPlayerNearby()) {
                  PacketHandler.networkWrapper.sendToServer(new CatThrowAwayItemPacket(this.getGirlId()));
               }

               this.aa = 1.0F;
               this.zFlag = 0.0F;
               break;
            case "renderItem":
               this.zFlag = 1.0F;
               break;
            case "paymentMSG1":
               this.sendChatMessageToPlayer(this.getInteractionPlayerUUID(), "Here, I know u like fish and yea.. these are for you");
               this.playSound(SoundHandler.MISC_PLOB[0]);
               break;
            case "paymentMSG2":
               this.sendChatMessage("huh~?");
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HUH);
               break;
            case "paymentMSG3":
               this.sendChatMessage("nyyyaaaa~ :D");
               int[] var4 = new int[]{1, 7, 10, 11};
               int var5 = var4[this.getRNG().nextInt(var4.length)];
               this.playSound(SoundHandler.GIRLS_LUNA_CUTENYA[var5]);
               break;
            case "paymentMSG4":
               this.sendChatMessage("tankuuuu owowowo");
               this.playRandomSound(SoundHandler.GIRLS_LUNA_OWO);
               break;
            case "paymentDone":
               if (this.isLocalPlayerNearby()) {
                  this.U();
               }

               this.scaleFactor = 1.0F;
               break;
            case "breath":
            case "rod_breath":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_LIGHTBREATHING);
               break;
            case "happyOh":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HAPPYOH);
               break;
            case "cutenya3":
               this.playSound(SoundHandler.GIRLS_LUNA_CUTENYA[3]);
               break;
            case "cutenya2":
               this.playSound(SoundHandler.GIRLS_LUNA_CUTENYA[2]);
               break;
            case "huh":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HUH);
               break;
            case "hmph":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HMPH);
               break;
            case "hehe":
            case "giggle":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
               break;
            case "singing":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_SINGING);
               break;
            case "touch_boobsMSG1":
               this.sendChatMessage("comon~ touch me hihi~");
               this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
               break;
            case "touch":
               this.playRandomSound(SoundHandler.MISC_TOUCH);
               break;
            case "jump":
               this.playSoundAtVolume(SoundHandler.MISC_JUMP[0], 0.2F);
               break;
            case "horninya":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HORNINYA);
               break;
            case "horninya2":
            case "touch_boobs_cumMSG3":
            case "sitting_cumMSG1":
               this.playSound(SoundHandler.GIRLS_LUNA_HORNINYA[1]);
               this.playSoundAtVolume(SoundHandler.MISC_CUMINFLATION[0], 5.0F);
               break;
            case "moan":
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_MOAN));
               break;
            case "touch_boobs_introDone":
               this.setCurrentAction(Action.TOUCH_BOOBS_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  HornyMeterHud.showHornyMeter();
                  HandlePlayerMovement.setMovementLock(false);
               }
               break;
            case "touch_boobs_slowDone":
               if (this.ae) {
                  this.ae = false;
               } else {
                  this.ae = Math.random() < 0.5;
               }
               break;
            case "addCumSlow":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "addCumFast":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "fastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.TOUCH_BOOBS_SLOW);
               }
               break;
            case "moanOrNya":
               if (Math.random() > 0.5) {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_MOAN));
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_HORNINYA));
               }
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "touch_boobs_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.resetCameraAndPhysics();
               }
               break;
            case "resetGirl":
               if (this.isLocalPlayerNearby()) {
                  this.resetCameraAndPhysics();
               }
               break;
            case "touch_boobs_cumMSG1":
               this.playSound(SoundHandler.GIRLS_LUNA_HORNINYA[3]);
               break;
            case "touch_boobs_cumMSG2":
               this.playSound(SoundHandler.GIRLS_LUNA_HORNINYA[9]);
               break;
            case "call_playerMSG1":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
               this.sendChatMessage("come here - big guy hehe~");
               break;
            case "pounding":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               break;
            case "sitting_introMSG1":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
               this.sendChatMessage("hehe~");
               break;
            case "sitting_introDone":
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.COWGIRL_SITTING_SLOW);
                  HornyMeterHud.resetHornyMeter();
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "sitting_slowMSG1":
               if (this.getRNG().nextBoolean()) {
                  if (this.getRNG().nextBoolean()) {
                     this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_HORNINYA));
                     break;
                  }

                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_MOAN));
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_LIGHTBREATHING));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "sitting_fastMSG1":
               if (this.getRNG().nextBoolean()) {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_HORNINYA));
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_MOAN));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "sitting_fastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.COWGIRL_SITTING_SLOW);
                  Vec3d var8 = new Vec3d(0.0, -0.075F, -0.7109375);
                  Vec3d var9 = VectorMath.rotateByYaw(var8, this.getYawRotation() + 180.0F);
                  Minecraft.getMinecraft()
                     .player
                     .setPosition(
                        this.getTargetPosition().x + var9.x,
                        this.getTargetPosition().y + var9.y,
                        this.getTargetPosition().z + var9.z
                     );
               }
               break;
            case "sitting_fastTp":
               if (this.isControlledByLocalPlayer()) {
                  Vec3d var6 = new Vec3d(0.0, -0.160625, -0.9925);
                  Vec3d var7 = VectorMath.rotateByYaw(var6, this.getYawRotation() + 180.0F);
                  Minecraft.getMinecraft()
                     .player
                     .setPosition(
                        this.getTargetPosition().x + var7.x,
                        this.getTargetPosition().y + var7.y,
                        this.getTargetPosition().z + var7.z
                     );
               }
               break;
            case "headpatMSG1":
               this.sendChatMessage("huh?~");
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HUH);
               break;
            case "headpatMSG2":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_MMM);
               break;
            case "headpatMSG3":
               this.sendChatMessage("nya~");
               this.playSound(SoundHandler.GIRLS_LUNA_HORNINYA[0]);
         }
      };
      this.movementController.transitionLengthTicks = 10.0;
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.setNoGravity(false);
   }

   public static class a {
      @SubscribeEvent
      public void onEntityJoinWorld(EntityJoinWorldEvent var1) {
         Entity var2 = var1.getEntity();
         if (var2 instanceof EntityCreeper) {
            EntityCreeper var3 = (EntityCreeper)var2;
            var3.tasks.addTask(3, new EntityAIAvoidEntity(var3, LunaEntity.class, 6.0F, 1.0, 1.2));
         }
      }
   }
}
