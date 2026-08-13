package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeDialogueScreen;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.ai.GirlGotoGoal;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.potion.HornyPotion;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.d3;







import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWaterFlying;
import net.minecraft.entity.ai.EntityFlyHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class BeeEntity extends BeeEntityBase {
   public float N = 3200.0F;
   int P = 0;
   static final float O = 4800.0F;
   static final float Q = 10.0F;
   public static final DataParameter<Boolean> M = EntityDataManager.createKey(BeeEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(112);

   public BeeEntity(World var1) {
      super(var1);
      this.moveHelper = new EntityFlyHelper(this);
      this.setSize(0.3F, 1.5F);
   }

   @Override
   public String getDisplayNameText() {
      return "Bee";
   }

   @Override
   public float i_clash226() {
      return -0.1F;
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.entityDataManager.register(M, false);
   }

   protected PathNavigate createNavigator(World var1) {
      PathNavigateFlying var2 = new PathNavigateFlying(this, var1);
      var2.setCanOpenDoors(false);
      var2.setCanFloat(true);
      var2.setCanEnterDoors(true);
      this.pathNavigator = var2;
      return var2;
   }

   @Override
   protected void applyEntityAttributes() {
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.MAX_HEALTH);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12.0);
      this.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.4F);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2F);
   }

   @Override
   protected void initEntityAI() {
      this.watchClosestGirlGoal = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.tasks.addTask(0, new GirlGotoGoal(this));
      this.tasks.addTask(1, new EntityAIPanic(this, 1.25));
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, this.watchClosestGirlGoal);
      this.tasks.addTask(3, new EntityAIWanderAvoidWaterFlying(this, 1.0));
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (this.isPotionActive(HornyPotion.b) && this.N < 4800.0F && this.getInteractionPlayerUUID() == null) {
         this.removePotionEffect(HornyPotion.b);
         this.N = 6.9420184E7F;
      }

      this.c_clash752();
      if (this.getCurrentAction().equals(fp.CITIZEN_CUM)) {
         this.P = Math.max(1, this.P);
      }

      this.a_clash754();
      this.b_clash753();
   }

   @Override
   public void b(fp var1) {
      if (this.getCurrentAction() != fp.CITIZEN_CUM || var1 != fp.CITIZEN_FAST && var1 != fp.COWGIRLSLOW) {
         super.b(var1);
      }
   }

   void c_clash752() {
      if (this.getInteractionPlayerUUID() == null) {
         if (!this.hasMaster()) {
            this.N++;
            if (!(this.N < 4800.0F)) {
               EntityPlayer var1 = this.world.getClosestPlayerToEntity(this, 10.0);
               if (var1 != null) {
                  if (getActiveSceneInfo(var1) == null) {
                     if (!AbstractPlayerGirlEntity.e(var1)) {
                        if (var1.getDistance(this) < 1.5F) {
                           this.N = 0.0F;
                           this.setInteractionPlayerUUID(var1.getPersistentID());
                           this.entityDataManager.set(IS_ANCHORED, true);
                           this.setTargetPosition(this.getFrontOffsetVector());
                           this.setYawRotation(var1.rotationYaw - 180.0F);
                           this.pathNavigator.clearPath();
                           PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var1);
                           this.b(fp.CITIZEN_START);
                           Vec3d var2 = this.getVectorTowardPlayer(0.2);
                           var1.setPositionAndUpdate(var2.x, var2.y, var2.z);
                        } else {
                           this.pathNavigator.clearPath();
                           this.pathNavigator.tryMoveToEntityLiving(var1, 1.0);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   void b_clash753() {
      RayTraceResult var1 = this.world.rayTraceBlocks(this.getPositionVector(), new Vec3d(this.posX, 0.0, this.posZ));
      if (var1 != null) {
         BlockPos var2 = var1.getBlockPos();
         double var3 = this.posY - var2.getY();
         if (var3 > 3.0 && this.motionY > 0.0) {
            this.motionY = 0.0;
         }
      }
   }

   void a_clash754() {
      if (this.P != 0) {
         this.P++;
         if ((Boolean)this.entityDataManager.get(M)) {
            if (this.P < 40) {
               for (EntityPlayer var2 : this.world.playerEntities) {
                  if (var2.getDistance(this) < 15.0F) {
                     ((EntityPlayerMP)var2)
                        .connection
                        .sendPacket(
                           new SPacketParticles(
                              EnumParticleTypes.HEART,
                              true,
                              (float)this.posX,
                              (float)this.posY + 0.3F,
                              (float)this.posZ,
                              0.2F,
                              0.3F,
                              0.2F,
                              0.25F,
                              1,
                              new int[0]
                           )
                        );
                  }
               }
            } else {
               this.P = 0;
            }
         } else if (this.P < 200) {
            for (EntityPlayer var6 : this.world.playerEntities) {
               if (var6.getDistance(this) < 15.0F) {
                  ((EntityPlayerMP)var6)
                     .connection
                     .sendPacket(
                        new SPacketParticles(
                           EnumParticleTypes.SPELL,
                           true,
                           (float)this.posX,
                           (float)this.posY + 0.3F,
                           (float)this.posZ,
                           0.2F,
                           0.3F,
                           0.2F,
                           0.25F,
                           1,
                           new int[0]
                        )
                     );
               }
            }
         } else if (this.P == 200) {
            this.entityDataManager.set(M, this.getRNG().nextBoolean());
         } else if (this.P < 250) {
            for (EntityPlayer var7 : this.world.playerEntities) {
               if (var7.getDistance(this) < 15.0F) {
                  ((EntityPlayerMP)var7)
                     .connection
                     .sendPacket(
                        new SPacketParticles(
                           this.entityDataManager.get(M) ? EnumParticleTypes.HEART : EnumParticleTypes.VILLAGER_ANGRY,
                           true,
                           (float)this.posX,
                           (float)this.posY + 0.3F,
                           (float)this.posZ,
                           0.2F,
                           0.3F,
                           0.2F,
                           0.25F,
                           3,
                           new int[0]
                        )
                     );
               }
            }
         } else {
            this.P = 0;
         }

         for (EntityPlayer var8 : this.world.playerEntities) {
            if (var8.getDistance(this) < 15.0F) {
               ((EntityPlayerMP)var8)
                  .connection
                  .sendPacket(
                     new SPacketParticles(
                        EnumParticleTypes.SPELL,
                        true,
                        (float)this.posX,
                        (float)this.posY + 0.3F,
                        (float)this.posZ,
                        0.2F,
                        0.3F,
                        0.2F,
                        0.25F,
                        10,
                        new int[0]
                     )
                  );
            }
         }
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (this.N < 4800.0F && !this.onGround && this.motionY < 0.0) {
         this.motionY *= 0.4;
      }
   }

   public void fall(float var1, float var2) {
   }

   protected boolean processInteract(EntityPlayer var1, EnumHand var2) {
      if ((Boolean)this.entityDataManager.get(M)
         && !(Boolean)this.entityDataManager.get(K)
         && var1.getHeldItem(var2).getItem() == Item.getItemFromBlock(Blocks.CHEST)) {
         this.entityDataManager.set(K, true);
         var1.getHeldItem(var2).shrink(1);
         return super.processInteract(var1, var2);
      }

      if (this.world.isRemote && (Boolean)this.entityDataManager.get(M)) {
         this.b_clash755(var1);
      }

      return super.processInteract(var1, var2);
   }

   @SideOnly(Side.CLIENT)
   void b_clash755(EntityPlayer var1) {
      Minecraft.getMinecraft().displayGuiScreen(new BeeDialogueScreen(this, var1));
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      return false;
   }

   @Override
   public void a(String var1, UUID var2) {
   }

   @Override
   protected fp getNextAction(fp var1) {
      return var1 == fp.CITIZEN_SLOW ? fp.CITIZEN_FAST : null;
   }

   @Override
   protected fp getCumAction(fp var1) {
      return var1 != fp.CITIZEN_FAST && var1 != fp.CITIZEN_SLOW ? null : fp.CITIZEN_CUM;
   }

   @Override
   protected void U() {
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setBoolean("isTamed", (Boolean)this.entityDataManager.get(M));
      var1.setBoolean("hasChest", (Boolean)this.entityDataManager.get(K));
      var1.setTag("inventory", this.L.serializeNBT());
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      if (var1.hasKey("isTamed")) {
         this.entityDataManager.set(M, var1.getBoolean("isTamed"));
      }

      this.entityDataManager.set(K, var1.getBoolean("hasChest"));
      this.L.deserializeNBT(var1.getCompoundTag("inventory"));
   }

   @SideOnly(Side.CLIENT)
   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      if (this.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      switch (var1.getController().getName()) {
         case "movement":
            if (this.getCurrentAction() != fp.NULL) {
               this.a("animation.bee.null", true, var1);
            } else {
               this.a("animation.bee." + (this.entityDataManager.get(K) ? "idle_has_chest" : "idle"), true, var1);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case CITIZEN_START:
                  this.a("animation.bee.sex_start", false, var1);
                  break;
               case CITIZEN_SLOW:
                  this.a("animation.bee.sex_slow", true, var1);
                  break;
               case CITIZEN_FAST:
                  this.a("animation.bee.sex_fast", true, var1);
                  break;
               case CITIZEN_CUM:
                  this.a("animation.bee.sex_cum", false, var1);
                  break;
               case THROW_PEARL:
                  this.a("animation.bee.throw_pearl", true, var1);
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
            case "pearl":
               if (this.isLocalPlayerNearby() && this.getCurrentAction() == fp.THROW_PEARL) {
                  PacketHandler.b.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               }
               break;
            case "resetCumPercentage":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "sex_fastMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "sex_startMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "sex_fastDone":
               if (!this.isControlledByLocalPlayer() || d3.d) {
                  return;
               }
            case "sex_startDone":
               this.b(fp.CITIZEN_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "sex_cumMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_CUMINFLATION), 2.0F);
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               break;
            case "blackscreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "sex_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.resetCameraAndPhysics();
               }
               break;
            case "sex_fastReady":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.N();
               }
         }
      };
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
   }

}
