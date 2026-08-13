package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.BiaModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.KoboldStatePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.d3;







import java.util.UUID;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
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

public class AlliePlayerEntity extends AbstractPlayerGirlEntity {
   static final double au = 4.0;
   static final double at = 4.0;
   public float aq = 0.0F;
   EntityPlayer as = null;
   boolean ap = false;
   int ar = 1;
   int av = 1;

   protected AlliePlayerEntity(World var1) {
      super(var1);
   }

   public AlliePlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public float i_clash226() {
      return 1.9F + this.aq;
   }

   public float getEyeHeight() {
      return 1.63F;
   }

   @Override
   public boolean v_clash227() {
      return false;
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new BiaModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return "textures/entity/allie/hand.png";
   }

   @Override
   public void b(String var1, UUID var2) {
      if ("action.names.deepthroat".equals(var1)) {
         this.b(fp.DEEPTHROAT_START);
         this.a(this.getOutfitIndex(), fp.DEEPTHROAT_START);
         this.b_clash577(var2);
      }

      if ("Reverse cowgirl".equals(var1)) {
         this.b(fp.REVERSE_COWGIRL_START);
         this.a(0, fp.REVERSE_COWGIRL_START);
         this.b_clash577(var2);
      }
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      a(var1, this, new String[]{"action.names.deepthroat", "Reverse cowgirl"}, false);
      return true;
   }

   @Override
   public void b(fp var1) {
      if (this.getCurrentAction() != fp.DEEPTHROAT_CUM || var1 != fp.DEEPTHROAT_FAST && var1 != fp.DEEPTHROAT_SLOW) {
         if (this.getCurrentAction() != fp.REVERSE_COWGIRL_CUM
            || var1 != fp.REVERSE_COWGIRL_SLOW && var1 != fp.REVERSE_COWGIRL_FAST_START && var1 != fp.REVERSE_COWGIRL_FAST_CONTINUES) {
            super.b(var1);
         }
      }
   }

   @Override
   public boolean F_clash231() {
      switch (this.getCurrentAction()) {
         case ALLIE_PREPARE_NORMAL:
         case DEEPTHROAT_START:
         case DEEPTHROAT_CUM:
         case DEEPTHROAT_FAST:
         case ALLIE_PREPARE_FIRST_TIME:
         case DEEPTHROAT_SLOW:
            return true;
         default:
            return false;
      }
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (this.getOwnerUserUUID() != null) {
         EntityPlayer var1 = this.world.getPlayerEntityByUUID(this.getOwnerUserUUID());
         if (var1 != null && this.as == null) {
            this.c_clash573(true);
         }

         this.as = var1;
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote) {
         this.a_clash232();
      }
   }

   @SideOnly(Side.CLIENT)
   void a_clash232() {
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

   @Override
   public void B_clash233() {
      this.c_clash573(true);
   }

   @Override
   public void y_clash234() {
      this.c_clash573(false);
   }

   @Override
   protected fp getNextAction(fp var1) {
      if (var1 == fp.DEEPTHROAT_SLOW) {
         return fp.DEEPTHROAT_FAST;
      } else {
         return var1 == fp.REVERSE_COWGIRL_SLOW ? fp.REVERSE_COWGIRL_FAST_START : null;
      }
   }

   @Override
   protected fp getCumAction(fp var1) {
      if (var1 == fp.DEEPTHROAT_FAST || var1 == fp.DEEPTHROAT_SLOW) {
         return fp.DEEPTHROAT_CUM;
      } else {
         return var1 != fp.REVERSE_COWGIRL_SLOW && var1 != fp.REVERSE_COWGIRL_FAST_START && var1 != fp.REVERSE_COWGIRL_FAST_CONTINUES
            ? null
            : fp.REVERSE_COWGIRL_CUM;
      }
   }

   @Override
   public void registerControllers(AnimationData var1) {
      if (this.C == null) {
         this.p_clash506();
      }

      AnimationController.ISoundListener var2 = var1x -> {
         switch (var1x.sound) {
            case "attackDone":
               if (++this.S == 3) {
                  this.S = 0;
               }
               break;
            case "deepthroat_prepareMSG1":
               this.sendChatMessage(I18n.format("allie.dialogue.hihi", new Object[0]));
               this.a(SoundHandler.MISC_PLOB[0]);
               break;
            case "deepthroat_prepareMSG2":
               this.sendChatMessage(I18n.format("allie.dialogue.boys", new Object[0]));
               this.a(SoundHandler.MISC_PLOB[0]);
               break;
            case "blackscreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "deepthroat_prepareDone":
               this.b(fp.DEEPTHROAT_START);
               if (this.isControlledByLocalPlayer()) {
                  PacketHandler.b.sendToServer(new KoboldStatePacket(this.getGirlId(), this.getInteractionPlayerUUID(), false, true));
                  this.r = this.rotationYaw + 180.0F;
                  this.positionPlayerRelative(0.0, 0.0, 1.35F, 0.0F, 30.0F);
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "deepthroat_fastMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_BJMOAN));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "deepthroat_fastDone":
               if (this.isControlledByLocalPlayer() && !d3.d) {
                  this.b(fp.DEEPTHROAT_SLOW);
               }
               break;
            case "deepthroat_startDone":
               this.b(fp.DEEPTHROAT_SLOW);
               break;
            case "deepthroat_slowMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "deepthroat_cumMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND));
               this.a(SoundHandler.randomSound(SoundHandler.MISC_CUMINFLATION), 1.5F);
               break;
            case "cowgirl_cumDone":
            case "deepthroat_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.r_clash533();
               }
               break;
            case "deepthroat_normal_prepareMSG1":
               this.sendChatMessage(I18n.format("allie.dialogue.alright", new Object[0]));
               this.a(SoundHandler.randomSound(SoundHandler.MISC_PLOB));
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
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_MMM));
               break;
            case "slide":
               this.a(SoundHandler.MISC_SLIDE, 0, 1, 4, 6);
               break;
            case "slowMoan":
               if (this.getRNG().nextBoolean()) {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_AHH));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "cowgirlSlowDone":
               int var6 = this.ar;

               do {
                  this.ar = this.getRNG().nextInt(3) + 1;
               } while (this.ar == var6);

               return;
            case "fastMoan":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (!this.ap) {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_MOAN));
                  this.ap = true;
               } else {
                  this.ap = false;
               }
               break;
            case "fastSwitch":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  fp var5 = this.getCurrentAction();
                  if (var5 == fp.REVERSE_COWGIRL_FAST_START) {
                     this.b(fp.REVERSE_COWGIRL_FAST_CONTINUES);
                  } else {
                     this.N();
                     int var4 = this.av;

                     do {
                        this.av = this.getRNG().nextInt(3) + 1;
                     } while (this.av == var4);
                  }
               }
               break;
            case "openSexUi":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "cum":
               this.a(SoundHandler.MISC_INSERTS, 6.0F);
               break;
            case "aftermoan":
               this.playRandomSound(SoundHandler.GIRLS_ALLIE_AFTERSESSIONMOAN);
         }
      };
      this.C.registerSoundListener(var2);
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.E);
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      if (this.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == fp.NULL && this.getCurrentAction().autoBlink) {
               this.a("animation.bia.blink", true, var1);
            } else {
               this.a("animation.allie.null", true, var1);
            }
            break;
         case "movement":
            double var4 = 4.0
               * (
                  Math.abs(this.posX - this.lastTickPosX)
                     + Math.abs(this.posY - this.lastTickPosY)
                     + Math.abs(this.posZ - this.lastTickPosZ)
               );
            var4 = Math.min(1.0 + var4, 4.0);
            this.E.setAnimationSpeed(var4);
            this.a("animation.allie.tail", true, var1);
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case ALLIE_PREPARE_NORMAL:
                  this.a("animation.allie.deepthroat_normal_prepare", false, var1);
                  break;
               case DEEPTHROAT_START:
                  this.a("animation.allie.deepthroat_start", false, var1);
                  break;
               case DEEPTHROAT_CUM:
                  this.a("animation.allie.deepthroat_cum", false, var1);
                  break;
               case DEEPTHROAT_FAST:
                  this.a("animation.allie.deepthroat_fast", true, var1);
                  break;
               case ALLIE_PREPARE_FIRST_TIME:
                  this.a("animation.allie.deepthroat_prepare", false, var1);
                  break;
               case DEEPTHROAT_SLOW:
                  this.a("animation.allie.deepthroat_slow", true, var1);
                  break;
               case NULL:
                  this.a("animation.allie.null", true, var1);
                  break;
               case SUMMON:
                  this.a("animation.allie.summon", false, var1);
                  break;
               case SUMMON_NORMAL:
                  this.a("animation.allie.summon_normal", false, var1);
                  break;
               case SUMMON_NORMAL_WAIT:
                  this.a("animation.allie.summon_normal_wait", true, var1);
                  break;
               case SUMMON_WAIT:
                  this.a("animation.allie.summon_wait", true, var1);
                  break;
               case RICH_FIRST_TIME:
                  this.a("animation.allie.rich", false, var1);
                  break;
               case RICH_NORMAL:
                  this.a("animation.allie.rich_normal", false, var1);
                  break;
               case SUMMON_SAND:
                  this.a("animation.allie.summon_sand", false, var1);
                  break;
               case ATTACK:
                  this.a("animation.allie.attack" + this.S, false, var1);
                  break;
               case BOW:
                  this.a("animation.allie.bowcharge", false, var1);
                  break;
               case REVERSE_COWGIRL_START:
                  this.a("animation.allie.reverse_cowgirl_start", true, var1);
                  break;
               case REVERSE_COWGIRL_SLOW:
                  this.a("animation.allie.reverse_cowgirl_slow" + this.ar, true, var1);
                  break;
               case REVERSE_COWGIRL_FAST_CONTINUES:
                  this.a("animation.allie.reverse_cowgirl_fastc" + this.av, true, var1);
                  break;
               case REVERSE_COWGIRL_FAST_START:
                  this.a("animation.allie.reverse_cowgirl_fasts", true, var1);
                  break;
               case REVERSE_COWGIRL_CUM:
                  this.a("animation.allie.reverse_cowgirl_cum", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

}
