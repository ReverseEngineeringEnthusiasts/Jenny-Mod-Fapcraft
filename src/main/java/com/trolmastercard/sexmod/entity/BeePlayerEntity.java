package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.BeeModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.d3;







import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class BeePlayerEntity extends AbstractPlayerGirlEntity {
   protected BeePlayerEntity(World var1) {
      super(var1);
   }

   public BeePlayerEntity(World var1, UUID var2) {
      super(var1, var2);
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
   public float i_clash226() {
      return 1.4F;
   }

   public float func_70047_e() {
      return 1.3F;
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new BeeModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return "textures/entity/bee/hand.png";
   }

   @Override
   public void b(String var1, UUID var2) {
      this.a(0, fp.CITIZEN_START);
      this.f(0);
      this.b(fp.CITIZEN_START);
      this.b_clash577(var2);
      EntityPlayer var3 = this.field_70170_p.func_152378_a(var2);
      if (var3 != null) {
         Vec3d var4 = this.getVectorTowardPlayer(-0.2);
         var3.func_70634_a(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c);
      }
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      a(var1, this, new String[]{"action.names.sex"}, false);
      return true;
   }

   @Override
   public void b(fp var1) {
      if (this.getCurrentAction() != fp.CITIZEN_CUM || var1 != fp.CITIZEN_FAST && var1 != fp.COWGIRLSLOW) {
         super.b(var1);
      }
   }

   @Override
   public void func_70071_h_() {
      super.func_70071_h_();
   }

   @Override
   public boolean v_clash227() {
      return false;
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
   public void reinitTasks() {
      super.reinitTasks();
      this.f(1);
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      switch (var1.getController().getName()) {
         case "movement":
            if (this.getCurrentAction() != fp.NULL) {
               this.a("animation.bee.null", true, var1);
            } else {
               this.a("animation.bee.idle", true, var1);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.a("animation.bee.null", false, var1);
                  break;
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
                  break;
               case ATTACK:
                  this.a("animation.bee.attack" + this.S, false, var1);
                  break;
               case BOW:
                  this.a("animation.bee.bowcharge", false, var1);
                  break;
               case RIDE:
                  this.a("animation.bee.ride", true, var1);
            }
      }

      return PlayState.CONTINUE;
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
            case "sex_fastReady":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.N();
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
                  this.r_clash533();
               }
         }
      };
      this.C.registerSoundListener(var2);
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.E);
   }

}
