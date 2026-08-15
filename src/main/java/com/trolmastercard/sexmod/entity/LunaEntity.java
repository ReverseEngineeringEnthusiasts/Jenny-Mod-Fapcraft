package com.trolmastercard.sexmod.entity;

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
import com.trolmastercard.sexmod.util.SceneDebug;
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

/**
 * Luna NPC — the catgirl who fishes, eats, and has touch-boobs and sitting
 * cowgirl scenes.
 * <p>
 * <b>Scene entry</b> (shared with Jenny/Bia/Kobold): client {@code doAction}
 * sets {@code animationFollowUp} (GIRL_HAND_STATES) via
 * {@code ChangeDataParameterPacket} and sends {@code KoboldStatePacket}; the
 * server calls {@code setDismounted()} ({@link #ac}), then
 * {@link #updateAITasks()} lerps her to {@code TARGET_POS} for ~40 ticks
 * ({@code aw} counter), anchors her and calls {@link #U()}:
 * touch_boobs -> PAYMENT (fish!) -> TOUCH_BOOBS_INTRO; sex ->
 * PAYMENT -> {@code SendGirlToSexPacket} + full {@code ResetGirlPacket}
 * (jar-faithful order) -&gt; {@link #goToSexBed()} walks her to the nearest
 * bed ({@link #ay}/{@code ak}), anchors, enters {@link Action#WAIT_CAT},
 * and {@link #handleNearbyPlayer()} starts COWGIRL_SITTING_INTRO once the
 * player stands within 1.25 blocks (25-tick counter {@code ab}).
 * <p>
 * <b>Idle behavior:</b> without a master/interaction she periodically fishes
 * (finds water via {@link #findFishingSpot()}, casts her rod, may eat the
 * catch; {@link #av} is the hook entity, {@link #al} schedules its removal).
 * <p>
 * <b>Pitfalls:</b>
 * <ul>
 *   <li>The dismount/bed lerps MUST use
 *       {@code RotationHelper.lerpVec3d(pos, target, 40 - counter)} (INT
 *       step variant) — the double variant flings her and she vanishes.</li>
 *   <li>{@link #readEntityFromNBT} forcing {@code setNoGravity(false)} on
 *       load is an invented (non-jar) band-aid; keep only if the float-on-
 *       reload symptom reappears.</li>
 *   <li>Data-parameter IDs 118-121 on this hierarchy are explicit — never
 *       renumber (see {@code BaseGirlEntity} class doc).</li>
 * </ul>
 */
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

   public LunaEntity(World world) {
      super(world);
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

   public boolean processInteract(EntityPlayer player, EnumHand hand) {
      if (super.processInteract(player, hand)) {
         return true;
      }

      ItemStack stack = player.getHeldItem(hand);
      boolean isNameTag = stack.getItem() == Items.NAME_TAG;
      if (isNameTag) {
         stack.interactWithEntity(player, this, hand);
         return true;
      }

      if (this.world.isRemote && !this.openInteractionMenu(player)) {
         this.sendChatMessage(I18n.format("bia.dialogue.busy", new Object[0]));
      }

      return true;
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer player) {
      String[] options = new String[]{"action.names.sex", "action.names.touchboobs", "action.names.headpat"};
      ItemStack[] rewards = new ItemStack[]{new ItemStack(Items.FISH, 3, 0), new ItemStack(Items.FISH, 2, 1), null};
      onPlayerApproach(player, this, options, rewards);
      return true;
   }

   @SideOnly(Side.CLIENT)
   protected static void onPlayerApproach(EntityPlayer player, BaseGirlEntity girl, String[] options, ItemStack[] rewards) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(girl, player, options, rewards, true));
   }

   public void setHeldItemStack(ItemStack stack) {
      this.entityDataManager.set(ag, stack);
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
         double dist = this.getTargetPosition().distanceTo(this.getPositionVector());
         if (!(dist < 0.5) && this.ak <= 200) {
            if (++this.ak == 60 || this.ak == 120) {
               SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna: walking to bed ak=%d dist=%.2f", this.ak, dist);
               this.getNavigator().clearPath();
               this.getNavigator().tryMoveToXYZ(this.getTargetPosition().x, this.getTargetPosition().y, this.getTargetPosition().z, 0.2);
            }
         } else {
            SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna: arrived at bed, WAIT_CAT (dist=%.2f ak=%d)", dist, this.ak);
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
            Vec3d pos = RotationHelper.lerpVec3d(this.getPositionVector(), this.getTargetPosition(), 40 - this.aw);
            this.setPosition(pos.x, pos.y, pos.z);
         } else {
            this.ac = false;
            this.aw = 0;
            EntityPlayer lerpPlayer = this.world.getMinecraftServer().getPlayerList().getPlayerByUUID(this.getInteractionPlayerUUID());
            if (lerpPlayer != null) {
               this.setYawRotation(lerpPlayer.rotationYaw + 180.0F);
            }

            this.entityDataManager.set(IS_ANCHORED, true);
            this.getNavigator().clearPath();
            this.U();
         }
      }

      this.syncHeldItem();
      this.entityDataManager.set(az, this.inventory.getStackInSlot(6));
   }

   void syncHeldItem() {
      ItemStack heldItem = this.ao;
      ItemStack syncedItem = (ItemStack)this.entityDataManager.get(az);
      if (!syncedItem.equals(ItemStack.EMPTY)) {
         Map enchantments = EnchantmentHelper.getEnchantments(syncedItem);
         EnchantmentHelper.setEnchantments(enchantments, heldItem);
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
      EntityPlayer player = this.world.getClosestPlayerToEntity(this, 10.0);
      if (player != null) {
         if (!(player.getDistance(this) > 1.25F)) {
            if (this.ab % 10 == 0) {
               SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna.handleNearbyPlayer remote=%s ab=%d playerDist=%.2f", this.world.isRemote, this.ab, player.getDistance(this));
            }
            if (this.world.isRemote) {
               this.setFishingLevelFor(player, this.ab);
            } else if (this.ab == 25) {
               SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna.handleNearbyPlayer SERVER: ab==25 -> COWGIRL_SITTING_INTRO");
               this.setInteractionPlayerUUID(player.getPersistentID());
               player.moveRelative(0.0F, 0.0F, 0.0F, 0.0F);
               player.setPositionAndUpdate(this.getPositionVector().x, this.getPositionVector().y, this.getPositionVector().z);
               this.setCurrentAction(Action.COWGIRL_SITTING_INTRO);
               player.setRotationYawHead(this.getYawRotation() + 180.0F);
               player.rotationYaw = this.getYawRotation() + 180.0F;
               player.prevRotationYaw = this.getYawRotation() + 180.0F;
               this.cameraYaw = this.getYawRotation() + 180.0F;
               this.positionPlayerRelative(0.0, -0.075F, -0.7109375, 0.0F, 0.0F);
               this.entityDataManager.set(OUTFIT_INDEX, 0);
            }

            this.ab++;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void setFishingLevelFor(EntityPlayer player, int level) {
      if (level == 0) {
         EntityPlayerSP localPlayer = Minecraft.getMinecraft().player;
         if (player.getPersistentID().equals(localPlayer.getPersistentID())) {
            BeeScreen.enableInteraction();
            localPlayer.setVelocity(0.0, 0.0, 0.0);
            HandlePlayerMovement.setMovementLock(false);
         }
      }

      if (level == 25) {
         EntityPlayerSP localPlayer = Minecraft.getMinecraft().player;
         if (player.getPersistentID().equals(localPlayer.getPersistentID())) {
            Minecraft.getMinecraft().gameSettings.thirdPersonView = 2;
         }
      }
   }

   @Override
   public void goToSexBed() {
      this.entityDataManager.set(IS_ANCHORED, false);
      this.setCurrentAction(Action.NULL);
      this.ar = true;
      BlockPos bedPos = this.getNearestBed(this.getPosition());
      if (bedPos == null) {
         this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
         PacketHandler.networkWrapper
            .sendToAllAround(
               new SendChatMessagePacket(
                  "<" + this.getDisplayNameText() + "> Heh.. there is no bed nearby.. but I already ate the fish so nya~ hehe", this.dimension, this.getGirlId()
               ),
               this.getTargetNetworkPoint()
            );
      } else {
         Vec3d bedVec = new Vec3d(bedPos.getX(), bedPos.getY(), bedPos.getZ());
         int[] yaws = new int[]{0, 180, -90, 90};
         Vec3d[][] offsets = new Vec3d[][]{
            {new Vec3d(0.5, 0.0, -0.5), new Vec3d(0.0, 0.0, -1.0)},
            {new Vec3d(0.5, 0.0, 1.5), new Vec3d(0.0, 0.0, 1.0)},
            {new Vec3d(-0.5, 0.0, 0.5), new Vec3d(-1.0, 0.0, 0.0)},
            {new Vec3d(1.5, 0.0, 0.5), new Vec3d(1.0, 0.0, 0.0)}
         };
         int bestIndex = -1;

         for (int i = 0; i < offsets.length; i++) {
            Vec3d offsetVec = bedVec.add(offsets[i][1]);
            if (this.world.getBlockState(new BlockPos(offsetVec.x, offsetVec.y, offsetVec.z)).getBlock()
               == Blocks.AIR) {
               if (bestIndex == -1) {
                  bestIndex = i;
               } else {
                  double bestDist = this.getPosition()
                     .distanceSq(
                        bedVec.add(offsets[bestIndex][0]).x,
                        bedVec.add(offsets[bestIndex][0]).y,
                        bedVec.add(offsets[bestIndex][0]).z
                     );
                  double dist = this.getPosition()
                     .distanceSq(
                        bedVec.add(offsets[i][0]).x,
                        bedVec.add(offsets[i][0]).y,
                        bedVec.add(offsets[i][0]).z
                     );
                  if (dist < bestDist) {
                     bestIndex = i;
                  }
               }
            }
         }

         if (bestIndex == -1) {
            this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
            this.sendChatMessage("Heh.. the bed is obscured.. but I already ate the fish so nya~ hehe");
            return;
         }

         Vec3d bedOffset = bedVec.add(offsets[bestIndex][0]);
         this.setYawRotation(yaws[bestIndex]);
         this.setTargetPosition(new Vec3d(bedOffset.x, bedOffset.y, bedOffset.z));
         this.cameraYaw = this.getYawRotation();
         this.getNavigator().clearPath();
         this.getNavigator().tryMoveToXYZ(bedOffset.x, bedOffset.y, bedOffset.z, 0.2);
         this.ay = true;
         this.ak = 0;
      }
   }

   public void dropHeldItem() {
      EntityItem item = new EntityItem(this.world, this.posX, this.posY, this.posZ, (ItemStack)this.entityDataManager.get(ag));
      Vec3d throwVec = VectorMath.rotateByYaw(new Vec3d(0.0, 0.2F + Math.random() * 0.1F, -0.2F + Math.random() * -0.1F), this.rotationYaw);
      item.motionX = throwVec.x;
      item.motionY = throwVec.y;
      item.motionZ = throwVec.z;
      this.world.spawnEntity(item);
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
               ((LunaRodItem)this.ao.getItem()).castFishingRod(this.world, this, EnumHand.MAIN_HAND);
               this.al = this.world.getTotalWorldTime() + 20L;
               ItemStack heldItem = (ItemStack)this.entityDataManager.get(ag);
               if (heldItem != ItemStack.EMPTY) {
                  if (heldItem.getItem() instanceof ItemFood) {
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
         PathNavigate navigator = this.getNavigator();
         navigator.tryMoveToXYZ(this.ai.getX(), this.ai.getY(), this.ai.getZ(), 0.35F);
         Path path = navigator.getPath();
         if (path != null) {
            if (path.getCurrentPathLength() > path.getCurrentPathIndex() + 1) {
               PathPoint nextPoint = path.getPathPointFromIndex(path.getCurrentPathIndex() + 1);
               PathPoint finalPoint = path.getPathPointFromIndex(path.getCurrentPathLength() - 1);
               Vec3d finalVec = new Vec3d(finalPoint.x, finalPoint.y, finalPoint.z);
               BlockPos pos = new BlockPos(nextPoint.x, nextPoint.y, nextPoint.z);
               if (this.getPositionVector().distanceTo(finalVec) < 0.75) {
                  navigator.clearPath();
                  this.setPosition(finalVec.x, finalVec.y, finalVec.z);
               }

               if (this.world.getBlockState(pos.add(0, 1, 0)).getBlock() == Blocks.WATER) {
                  navigator.clearPath();
               }

               if (this.world.getBlockState(pos).getBlock() == Blocks.WATER) {
                  navigator.clearPath();
               }

               if (this.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.WATER) {
                  navigator.clearPath();
               }
            }
         }
      }
   }

   void findFishingSpot() {
      int attempts = 0;
      BlockPos spot = null;
      int bestDepth = 0;

      while (++attempts < 50) {
         BlockPos candidate = this.findNearestStructureBlock(
            this.getPosition(),
            attempts + 1,
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
         if (candidate == null) {
            break;
         }

         while (this.world.getBlockState(candidate.add(0, 1, 0)).getBlock() == Blocks.WATER) {
            candidate = candidate.add(0, 1, 0);
         }

         int depth = 1;

         for (BlockPos probe = candidate; this.world.getBlockState(probe.add(0, -1, 0)).getBlock() == Blocks.WATER; depth++) {
            probe = probe.add(0, -1, 0);
         }

         if (!this.an.contains(candidate)) {
            if (spot == null) {
               spot = candidate;
               bestDepth = depth;
            } else if (depth > bestDepth) {
               spot = candidate;
               bestDepth = depth;
               if (bestDepth >= 6) {
                  break;
               }
            }
         }
      }

      if (spot != null) {
         if (this.ai == null || this.at < bestDepth) {
            this.ai = spot;
            this.at = bestDepth;
         }

         if (this.ai.equals(spot)) {
            this.as = 0;
         } else if (++this.as > 20) {
            this.ai = spot;
            this.at = bestDepth;
         }
      }
   }

   void handleFishingPath() {
      Path path = this.getNavigator().getPath();
      if (path != null) {
         PathPoint finalPoint = path.getFinalPathPoint();
         PathPoint targetPoint = new PathPoint(
            ThreadNames.roundToInt(this.posX), ThreadNames.roundToInt(this.posY), ThreadNames.roundToInt(this.posZ)
         );
         if (finalPoint != null) {
            this.entityDataManager.set(yFlag, finalPoint.distanceTo(targetPoint));
         }
      }
   }

   @Override
   public void doAction(String action, UUID uuid) {
      SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna.doAction %s player=%s (remote=%s)", action, uuid, this.world.isRemote);
      super.doAction(action, uuid);
      if ("action.names.touchboobs".equals(action)) {
         this.setInteractionPlayerUUID(uuid);
         this.triggerActionSync(true, true, uuid);
         this.changeDataParameterFromClient("animationFollowUp", "touch_boobs");
         this.changeDataParameterFromClient("currentModel", "0");
         HandlePlayerMovement.setMovementLock(false);
      }

      if ("action.names.sex".equals(action)) {
         this.setInteractionPlayerUUID(uuid);
         this.triggerActionSync(true, true, uuid);
         this.changeDataParameterFromClient("animationFollowUp", "sex");
         HandlePlayerMovement.setMovementLock(false);
      }

      if ("action.names.headpat".equals(action)) {
         this.setInteractionPlayerUUID(uuid);
         this.triggerActionSync(true, true, uuid);
         HandlePlayerMovement.setMovementLock(false);
         this.changeDataParameterFromClient("animationFollowUp", "headpat");
      }
   }

   @Override
   protected Action getNextAction(Action action) {
      if (action == Action.TOUCH_BOOBS_SLOW) {
         return Action.TOUCH_BOOBS_FAST;
      } else {
         return action == Action.COWGIRL_SITTING_SLOW ? Action.COWGIRL_SITTING_FAST : null;
      }
   }

   @Override
   protected Action getCumAction(Action action) {
      if (action == Action.TOUCH_BOOBS_SLOW || action == Action.TOUCH_BOOBS_FAST) {
         return Action.TOUCH_BOOBS_CUM;
      } else {
         return action != Action.COWGIRL_SITTING_FAST && action != Action.COWGIRL_SITTING_SLOW ? null : Action.COWGIRL_SITTING_CUM;
      }
   }

   @Override
   protected void U() {
      SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna.U() handState=%s action=%s remote=%s", this.entityDataManager.get(GIRL_HAND_STATES), this.getCurrentAction(), this.world.isRemote);
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

   protected void playHurtSound(DamageSource source) {
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
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      if (this.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.cat.null", true, event);
            } else {
               this.createAnimation("animation.cat.blink", true, event);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.cat.null", true, event);
            } else if (this.isRiding()) {
               this.createAnimation("animation.cat.sit", true, event);
            } else if (Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ) > 0.0) {
               if (this.onGround && Math.abs(Math.abs(this.prevPosY) - Math.abs(this.posY)) < 0.1F) {
                  this.createAnimation(this.entityDataManager.get(yFlag) < 3.0F ? "animation.cat.walk" : "animation.cat.run", true, event);
               } else {
                  this.createAnimation("animation.cat.fly", true, event);
               }

               this.rotationYaw = this.rotationYawHead;
            } else {
               this.createAnimation("animation.cat.idle" + (this.ad ? "2" : ""), true, event);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.cat.null", true, event);
                  break;
               case ATTACK:
                  this.createAnimation("animation.cat.attack" + this.nextAttack, false, event);
                  break;
               case RIDE:
               case SIT:
                  this.createAnimation("animation.cat.sit", true, event);
                  break;
               case BOW:
                  this.createAnimation("animation.cat.bowcharge", false, event);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.cat.throwpearl", true, event);
                  break;
               case DOWNED:
                  this.createAnimation("animation.cat.downed", true, event);
                  break;
               case FISHING_START:
                  this.createAnimation("animation.cat.start_fishing", false, event);
                  break;
               case FISHING_IDLE:
                  this.createAnimation("animation.cat.idle_fishing", true, event);
                  break;
               case FISHING_EAT:
                  this.createAnimation("animation.cat.eat_fishing", false, event);
                  break;
               case FISHING_THROW_AWAY:
                  this.createAnimation("animation.cat.throw_away", false, event);
                  break;
               case PAYMENT:
                  this.createAnimation("animation.cat.payment", false, event);
                  break;
               case TOUCH_BOOBS_INTRO:
                  this.createAnimation("animation.cat.touch_boobs_intro", false, event);
                  break;
               case TOUCH_BOOBS_SLOW:
                  this.createAnimation("animation.cat.touch_boobs_slow" + (this.ae ? "1" : ""), true, event);
                  break;
               case TOUCH_BOOBS_FAST:
                  this.createAnimation("animation.cat.touch_boobs_fast", true, event);
                  break;
               case TOUCH_BOOBS_CUM:
                  this.createAnimation("animation.cat.touch_boobs_cum", false, event);
                  break;
               case WAIT_CAT:
                  this.createAnimation("animation.cat.wait", false, event);
                  break;
               case COWGIRL_SITTING_INTRO:
                  this.createAnimation("animation.cat.sitting_intro", false, event);
                  break;
               case COWGIRL_SITTING_SLOW:
                  this.createAnimation("animation.cat.sitting_slow", true, event);
                  break;
               case COWGIRL_SITTING_FAST:
                  this.createAnimation("animation.cat.sitting_fast", true, event);
                  break;
               case COWGIRL_SITTING_CUM:
                  this.createAnimation("animation.cat.sitting_cum", false, event);
                  break;
               case HEAD_PAT:
                  this.createAnimation("animation.cat.head_pat", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   @Override
   public void registerControllers(AnimationData data) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
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
               int[] soundIds = new int[]{1, 7, 10, 11};
               int soundId = soundIds[this.getRNG().nextInt(soundIds.length)];
               this.playSound(SoundHandler.GIRLS_LUNA_CUTENYA[soundId]);
               break;
            case "paymentMSG4":
               this.sendChatMessage("tankuuuu owowowo");
               this.playRandomSound(SoundHandler.GIRLS_LUNA_OWO);
               break;
            case "paymentDone":
               SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna.sound paymentDone (remote=%s, action=%s, nearby=%s)", this.world.isRemote, this.getCurrentAction(), this.isLocalPlayerNearby());
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
               SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna.sound touch_boobs_introDone (remote=%s, action=%s, controlled=%s)", this.world.isRemote, this.getCurrentAction(), this.isControlledByLocalPlayer());
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
               SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna.sound touch_boobs_cumDone (remote=%s, action=%s, controlled=%s)", this.world.isRemote, this.getCurrentAction(), this.isControlledByLocalPlayer());
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
                  Vec3d headOffset = new Vec3d(0.0, -0.075F, -0.7109375);
                  Vec3d rotatedHead = VectorMath.rotateByYaw(headOffset, this.getYawRotation() + 180.0F);
                  Minecraft.getMinecraft()
                     .player
                     .setPosition(
                        this.getTargetPosition().x + rotatedHead.x,
                        this.getTargetPosition().y + rotatedHead.y,
                        this.getTargetPosition().z + rotatedHead.z
                     );
               }
               break;
            case "sitting_fastTp":
               if (this.isControlledByLocalPlayer()) {
                  Vec3d backOffset = new Vec3d(0.0, -0.160625, -0.9925);
                  Vec3d rotatedBack = VectorMath.rotateByYaw(backOffset, this.getYawRotation() + 180.0F);
                  Minecraft.getMinecraft()
                     .player
                     .setPosition(
                        this.getTargetPosition().x + rotatedBack.x,
                        this.getTargetPosition().y + rotatedBack.y,
                        this.getTargetPosition().z + rotatedBack.z
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
      this.actionController.registerSoundListener(soundListener);
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      this.setNoGravity(false);
   }

   public static class a {
      @SubscribeEvent
      public void onEntityJoinWorld(EntityJoinWorldEvent event) {
         Entity entity = event.getEntity();
         if (entity instanceof EntityCreeper) {
            EntityCreeper creeper = (EntityCreeper)entity;
            creeper.tasks.addTask(3, new EntityAIAvoidEntity(creeper, LunaEntity.class, 6.0F, 1.0, 1.2));
         }
      }
   }
}
