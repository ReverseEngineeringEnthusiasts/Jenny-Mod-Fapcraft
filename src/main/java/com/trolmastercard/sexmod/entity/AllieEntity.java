package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.networking.KoboldStatePacket;
import com.trolmastercard.sexmod.networking.MakeRichWishPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UploadInventoryToServerPacket2;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.WorldUtils;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;







import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class AllieEntity extends BaseGirlEntity {
   public static final int LAMP_RANGE = 300;
   public static final int LAMP_SLOTS = 8;
   public static final Vec3d LAMP_OFFSET = new Vec3d(0.5, 1.0, 0.0);
   public float LAMP_SCALE = 1.0F;
   public boolean isLampActive = false;
   public static final DataParameter<ItemStack> LAMP_ITEM = EntityDataManager.createKey(AllieEntity.class, DataSerializers.ITEM_STACK)
      .getSerializer()
      .createKey(111);
   boolean isDefaultState = true;
   int stateIndex = 1;
   int stateCount = 1;
   boolean stateFlag = false;
   boolean stateFlag2 = false;

   public AllieEntity(World var1) {
      super(var1);
      this.setSize((float)LAMP_OFFSET.x, (float)LAMP_OFFSET.y);
   }

   public AllieEntity(World var1, ItemStack var2) {
      this(var1);
      this.entityDataManager.set(LAMP_ITEM, var2);
   }

   @Override
   public String getDisplayNameText() {
      return "Allie";
   }

   @Override
   public float getScaleFactor() {
      return 1.0F;
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.entityDataManager.register(LAMP_ITEM, ItemStack.EMPTY);
   }

   public boolean hasLampItem() {
      NBTTagCompound var1 = ((ItemStack)this.entityDataManager.get(LAMP_ITEM)).getTagCompound();
      return var1 == null ? true : var1.getInteger("sexmodUses") == 1;
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (this.getCurrentAction() == Action.NULL) {
         this.world.removeEntity(this);
      }

      UUID var1 = this.getInteractionPlayerUUID();
      if (var1 != null) {
         EntityPlayer var2 = this.world.getPlayerEntityByUUID(var1);
         if (var2 == null) {
            this.world.removeEntity(this);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void ac() {
      if (!this.stateFlag2) {
         this.isLampActive = true;
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (this.LAMP_SCALE != 1.0F && this.LAMP_SCALE != -69.0F && this.LAMP_SCALE <= 0.0F) {
         if (this.isControlledByLocalPlayer()) {
            PacketHandler.networkWrapper.sendToServer(new UploadInventoryToServerPacket2(this.getGirlId()));
            HandlePlayerMovement.setMovementLock(true);
         }

         this.LAMP_SCALE = -69.0F;
      }

      if (this.world.isRemote) {
         if (this.isLampActive) {
            this.openInteraction();
         }

         if (this.isDefaultState) {
            this.resetToDefaultState();
         }

         this.spawnRandomParticles();
      }
   }

   void spawnRandomParticles() {
      if (this.ticksExisted % 10 == 0) {
         int var1 = this.getRNG().nextInt(8);
         Vec3d var2 = this.getCachedBoneOffset("tail" + var1).add(this.getPositionVector());
         this.world
            .spawnParticle(
               EnumParticleTypes.PORTAL,
               var2.x,
               var2.y,
               var2.z,
               this.getRNG().nextGaussian() * 0.01F,
               this.getRNG().nextGaussian() * 0.01F,
               this.getRNG().nextGaussian() * 0.01F,
               new int[0]
            );
      }
   }

   @SideOnly(Side.CLIENT)
   void resetToDefaultState() {
      this.isDefaultState = false;
      WorldUtils.a(this.world, EnumParticleTypes.PORTAL, this.getPositionVector(), 300, 0.75, 1.5);
   }

   @SideOnly(Side.CLIENT)
   void openInteraction() {
      this.openInteractionMenu(Minecraft.getMinecraft().player);
      this.isLampActive = false;
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      this.stateFlag2 = false;
      String[] var2 = new String[]{"action.names.makemerichallie", "action.names.deepthroat", "Reverse cowgirl"};
      openInventoryGui(var1, this, var2, false);
      return true;
   }

   @Override
   protected Action getNextAction(Action var1) {
      if (var1 == Action.DEEPTHROAT_SLOW) {
         return Action.DEEPTHROAT_FAST;
      } else {
         return var1 == Action.REVERSE_COWGIRL_SLOW ? Action.REVERSE_COWGIRL_FAST_START : null;
      }
   }

   @Override
   protected Action getCumAction(Action var1) {
      if (var1 == Action.DEEPTHROAT_FAST || var1 == Action.DEEPTHROAT_SLOW) {
         return Action.DEEPTHROAT_CUM;
      } else {
         return var1 != Action.REVERSE_COWGIRL_SLOW && var1 != Action.REVERSE_COWGIRL_FAST_START && var1 != Action.REVERSE_COWGIRL_FAST_CONTINUES
            ? null
            : Action.REVERSE_COWGIRL_CUM;
      }
   }

   @Override
   public void setCurrentAction(Action action) {
      if (this.getCurrentAction() != Action.DEEPTHROAT_CUM || action != Action.DEEPTHROAT_FAST && action != Action.DEEPTHROAT_SLOW) {
         if (this.getCurrentAction() != Action.REVERSE_COWGIRL_CUM
            || action != Action.REVERSE_COWGIRL_SLOW && action != Action.REVERSE_COWGIRL_FAST_START && action != Action.REVERSE_COWGIRL_FAST_CONTINUES) {
            if (!this.world.isRemote && action == Action.REVERSE_COWGIRL_START) {
               this.handleAllieOwner();
            }

            super.setCurrentAction(action);
         }
      }
   }

   void handleAllieOwner() {
      EntityPlayer var1 = this.getPlayerEntity();
      if (var1 != null) {
         Vec3d var2 = this.getTargetPosition();
         var1.setPositionAndUpdate(var2.x, var2.y, var2.z);
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      if (this.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != Action.NULL || !this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.allie.null", true, var1);
            }
            break;
         case "movement":
            this.createAnimation("animation.allie.tail", true, var1);
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case SUMMON:
                  this.createAnimation("animation.allie.summon", false, var1);
                  break;
               case SUMMON_NORMAL:
                  this.createAnimation("animation.allie.summon_normal", false, var1);
                  break;
               case SUMMON_NORMAL_WAIT:
                  this.createAnimation("animation.allie.summon_normal_wait", true, var1);
                  break;
               case SUMMON_WAIT:
                  this.createAnimation("animation.allie.summon_wait", true, var1);
                  break;
               case ALLIE_PREPARE_FIRST_TIME:
                  this.createAnimation("animation.allie.deepthroat_prepare", false, var1);
                  break;
               case ALLIE_PREPARE_NORMAL:
                  this.createAnimation("animation.allie.deepthroat_normal_prepare", false, var1);
                  break;
               case DEEPTHROAT_START:
                  this.createAnimation("animation.allie.deepthroat_start", false, var1);
                  break;
               case DEEPTHROAT_SLOW:
                  this.createAnimation("animation.allie.deepthroat_slow", true, var1);
                  break;
               case DEEPTHROAT_FAST:
                  this.createAnimation("animation.allie.deepthroat_fast", true, var1);
                  break;
               case DEEPTHROAT_CUM:
                  this.createAnimation("animation.allie.deepthroat_cum", false, var1);
                  break;
               case RICH_FIRST_TIME:
                  this.createAnimation("animation.allie.rich", false, var1);
                  break;
               case RICH_NORMAL:
                  this.createAnimation("animation.allie.rich_normal", false, var1);
                  break;
               case SUMMON_SAND:
                  this.createAnimation("animation.allie.summon_sand", false, var1);
                  break;
               case REVERSE_COWGIRL_START:
                  this.createAnimation("animation.allie.reverse_cowgirl_start", true, var1);
                  break;
               case REVERSE_COWGIRL_SLOW:
                  this.createAnimation("animation.allie.reverse_cowgirl_slow" + this.stateIndex, true, var1);
                  break;
               case REVERSE_COWGIRL_FAST_CONTINUES:
                  this.createAnimation("animation.allie.reverse_cowgirl_fastc" + this.stateCount, true, var1);
                  break;
               case REVERSE_COWGIRL_FAST_START:
                  this.createAnimation("animation.allie.reverse_cowgirl_fasts", true, var1);
                  break;
               case REVERSE_COWGIRL_CUM:
                  this.createAnimation("animation.allie.reverse_cowgirl_cum", true, var1);
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
            case "summonMSG1":
               this.sendChatMessage(I18n.format("allie.dialogue.summon1", new Object[0]));
               this.playSoundAtVolume(SoundHandler.GIRLS_ALLIE_SCAWY[0], 0.5F);
               break;
            case "summonMSG2":
               this.sendChatMessage(I18n.format("allie.dialogue.summon2", new Object[0]));
               this.playSound(SoundHandler.GIRLS_ALLIE_GIGGLE[this.getRNG().nextInt(4)]);
               break;
            case "summonMSG3":
               this.sendChatMessage(I18n.format("allie.dialogue.summon3", new Object[0]));
               break;
            case "summonMSG4":
               this.sendChatMessage(I18n.format("allie.dialogue.summon4", new Object[0]));
               this.playSound(SoundHandler.GIRLS_ALLIE_LIGHTBREATHING[2]);
               break;
            case "summonMSG5":
               this.sendChatMessage(I18n.format("allie.dialogue.summon5", new Object[0]));
               this.playSound(SoundHandler.GIRLS_ALLIE_HMPH[4]);
               break;
            case "summonMSG6":
               this.sendChatMessage(I18n.format("allie.dialogue.summon6", new Object[0]));
               this.playSound(SoundHandler.GIRLS_ALLIE_GIGGLE[3]);
               break;
            case "summonMSG7":
               this.sendChatMessage(I18n.format("allie.dialogue.summon7", new Object[0]));
               break;
            case "summonMSG8":
               this.sendChatMessage(I18n.format("allie.dialogue.summon8", new Object[0]));
               this.playRandomSound(SoundHandler.GIRLS_ALLIE_HUH);
               if (this.isControlledByLocalPlayer()) {
                  this.openInteractionMenu(this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID()));
               }
               break;
            case "summonDone":
               this.setCurrentAction(Action.SUMMON_WAIT);
               break;
            case "deepthroat_prepareMSG1":
               this.sendChatMessage(I18n.format("allie.dialogue.hihi", new Object[0]));
               this.playRandomSound(SoundHandler.GIRLS_ALLIE_GIGGLE);
               break;
            case "deepthroat_prepareMSG2":
               this.sendChatMessage(I18n.format("allie.dialogue.boys", new Object[0]));
               this.playSound(SoundHandler.GIRLS_ALLIE_SIGH[0]);
               break;
            case "scream":
               this.playRandomSound(SoundHandler.MISC_SCREAM);
               break;
            case "blackscreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "deepthroat_prepareDone":
               if (this.isControlledByLocalPlayer()) {
                  if ("reverse_cowgirl".equals(this.entityDataManager.get(GIRL_HAND_STATES))) {
                     this.rotationPitch = 30.0F;
                     this.setCurrentAction(Action.REVERSE_COWGIRL_START);
                  } else {
                     this.setCurrentAction(Action.DEEPTHROAT_START);
                     PacketHandler.networkWrapper.sendToServer(new KoboldStatePacket(this.getGirlId(), this.getInteractionPlayerUUID(), false, true));
                     this.cameraYaw = this.rotationYaw + 180.0F;
                     this.positionPlayerRelative(0.0, 0.0, 1.35F, 0.0F, 30.0F);
                     HornyMeterHud.resetHornyMeter();
                  }
               }
               break;
            case "deepthroat_fastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.DEEPTHROAT_SLOW);
               }
               break;
            case "deepthroat_startDone":
               this.setCurrentAction(Action.DEEPTHROAT_SLOW);
               break;
            case "deepthroat_fastMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_BJMOAN));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "deepthroat_slowMSG1":
               if (this.getRNG().nextFloat() > 0.33F) {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND));
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_BJMOAN));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "deepthroat_cumMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_MOAN));
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND));
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_CUMINFLATION), 1.5F);
               break;
            case "cowgirl_cumDone":
            case "deepthroat_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.resetCameraAndPhysics();
                  PacketHandler.networkWrapper.sendToServer(new UploadInventoryToServerPacket2(this.getGirlId()));
               }
               break;
            case "summon_normalMSG1":
               this.sendChatMessage(I18n.format("allie.dialogue.sup", new Object[0]));
               this.playSound(SoundHandler.GIRLS_ALLIE_GIGGLE[this.getRNG().nextInt(4)]);
               break;
            case "summon_normalMSG2":
               this.sendChatMessage(I18n.format("allie.dialogue.youhave", new Object[0]));
               break;
            case "summon_normalMSG3":
               if (((ItemStack)this.entityDataManager.get(LAMP_ITEM)).getTagCompound().getInteger("sexmodUses") == 2) {
                  this.sendChatMessage(I18n.format("allie.dialogue.2wishes", new Object[0]));
               } else {
                  this.sendChatMessage(I18n.format("allie.dialogue.1wish", new Object[0]));
               }

               this.playSound(SoundHandler.GIRLS_ALLIE_HMPH[4]);
               break;
            case "summon_normalMSG4":
               this.sendChatMessage("So...");
               break;
            case "summon_normalMSG5":
               this.sendChatMessage(I18n.format("allie.dialogue.tellme", new Object[0]));
               this.playRandomSound(SoundHandler.GIRLS_ALLIE_HUH);
               break;
            case "summon_normalDone":
               this.setCurrentAction(Action.SUMMON_NORMAL_WAIT);
               if (this.isControlledByLocalPlayer()) {
                  this.openInteractionMenu(Minecraft.getMinecraft().player);
               }
               break;
            case "deepthroat_normal_prepareMSG1":
               this.sendChatMessage(I18n.format("allie.dialogue.alright", new Object[0]));
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_GIGGLE));
               break;
            case "rich_MSG1":
               this.sendChatMessage(I18n.format("allie.dialogue.wishgranted", new Object[0]));
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_PLOB));
               if (this.isControlledByLocalPlayer()) {
                  PacketHandler.networkWrapper.sendToServer(new MakeRichWishPacket(this.getPositionVector()));
               }
               break;
            case "disappear":
               this.LAMP_SCALE = 0.99F;
               break;
            case "summon_sandMSG1":
               this.sendChatMessage(I18n.format("allie.dialogue.nooo", new Object[0]));
               this.playSound(SoundHandler.GIRLS_ALLIE_SCAWY[2]);
               break;
            case "summon_sandMSG2":
               if (this.isLocalPlayerNearby()) {
                  this.broadcastChatAround(I18n.format("allie.dialogue.phobia", new Object[0]), true);
               }
               break;
            case "giggle":
               this.playRandomSound(SoundHandler.GIRLS_ALLIE_GIGGLE);
               break;
            case "pounding":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               break;
            case "moan":
               this.playRandomSound(SoundHandler.GIRLS_ALLIE_MOAN);
               break;
            case "mmm":
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_MMM));
               break;
            case "slide":
               this.playRandomSound(SoundHandler.MISC_SLIDE, 0, 1, 4, 6);
               break;
            case "slowMoan":
               if (this.getRNG().nextBoolean()) {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_AHH));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "cowgirlSlowDone":
               int var6 = this.stateIndex;

               do {
                  this.stateIndex = this.getRNG().nextInt(3) + 1;
               } while (this.stateIndex == var6);

               return;
            case "fastMoan":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (!this.stateFlag) {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_MOAN));
                  this.stateFlag = true;
               } else {
                  this.stateFlag = false;
               }
               break;
            case "fastSwitch":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  Action var5 = this.getCurrentAction();
                  if (var5 == Action.REVERSE_COWGIRL_FAST_START) {
                     this.setCurrentAction(Action.REVERSE_COWGIRL_FAST_CONTINUES);
                  } else {
                     this.resetAnimationControllerOffset();
                     int var4 = this.stateCount;

                     do {
                        this.stateCount = this.getRNG().nextInt(3) + 1;
                     } while (this.stateCount == var4);
                  }
               }
               break;
            case "openSexUi":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "cum":
               this.playRandomSoundAtVolume(SoundHandler.MISC_INSERTS, 6.0F);
               break;
            case "aftermoan":
               this.playRandomSound(SoundHandler.GIRLS_ALLIE_AFTERSESSIONMOAN);
         }
      };
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
   }

   @Override
   public void doAction(String var1, UUID var2) {
      this.stateFlag2 = true;
      if ("action.names.makemerichallie".equals(var1)) {
         this.setCurrentAction(this.hasLampItem() ? Action.RICH_FIRST_TIME : Action.RICH_NORMAL);
      } else {
         this.changeDataParameterFromClient("animationFollowUp", "action.names.deepthroat".equals(var1) ? "deepthroat" : "reverse_cowgirl");
         this.setCurrentAction(this.hasLampItem() ? Action.ALLIE_PREPARE_FIRST_TIME : Action.ALLIE_PREPARE_NORMAL);
      }
   }

}
