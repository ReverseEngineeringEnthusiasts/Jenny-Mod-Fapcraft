package com.trolmastercard.sexmod.entity.ai;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.Reference;
import java.util.UUID;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public abstract class GirlFollowAiBase extends EntityAIBase {
   public BaseGirlEntity girl;
   public EntityPlayer master;
   public PathNavigate navigator;
   public EntityDataManager dataManager;
   public GirlFollowAiBase.GirlFollowAiBaseState state = GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
   public static final double followDistance = 0.5;
   public static final double attackDistance = 0.7;
   public static final int updateTicks = 60;

   public GirlFollowAiBase(BaseGirlEntity var1) {
      this.girl = var1;
      this.navigator = var1.getNavigator();
      this.dataManager = var1.getDataManager();
   }

   protected void updateNavigation() {
      int var2 = 0;

      BlockPos var1;
      do {
         var1 = this.master.getPosition().add(Reference.RANDOM.nextInt(10), 0, Reference.RANDOM.nextInt(10));
      } while (++var2 < 20 && !this.girl.attemptTeleport(var1.getX(), var1.getY(), var1.getZ()));

      if (var2 >= 20) {
         this.girl.setPosition(this.master.posX, this.master.posY, this.master.posZ);
      }

      this.girl.motionX = 0.0;
      this.girl.motionY = 0.0;
      this.girl.motionZ = 0.0;
   }

   protected double getFollowDistance() {
      float var1 = this.girl.getDistance(this.master);
      double var2;
      BaseGirlEntity.BaseGirlEntityState var4;
      if (this.master.isSprinting()) {
         var2 = 0.7;
         var4 = BaseGirlEntity.BaseGirlEntityState.RUN;
      } else {
         var2 = 0.5;
         var4 = BaseGirlEntity.BaseGirlEntityState.WALK;
      }

      double var5 = Math.floor(var1 / 5.0F) * 0.2;
      var2 += var5;
      if (this.girl.isInWater()) {
         var2 *= 60.0;
         var4 = BaseGirlEntity.BaseGirlEntityState.WALK;
      }

      this.navigator.setSpeed(var2);
      this.girl.setWalkSpeed(var4);
      return var2;
   }

   public void resetTask() {
      this.navigator.clearPath();
      this.state = GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
      this.girl.setCurrentAction(Action.NULL);
      this.dataManager.set(BaseGirlEntity.MASTER, "");
      this.navigator = null;
      this.dataManager = null;
      this.master = null;
   }

   public boolean shouldExecute() {
      return !((String)this.girl.getDataManager().get(BaseGirlEntity.MASTER)).equals("");
   }

   public boolean shouldContinueExecuting() {
      String var1 = (String)this.dataManager.get(BaseGirlEntity.MASTER);
      return !var1.equals("") && this.girl.world.getPlayerEntityByUUID(UUID.fromString(var1)) != null;
   }

   public void startExecuting() {
      this.navigator = this.girl.getNavigator();
      this.dataManager = this.girl.getDataManager();
      this.master = this.girl.world.getPlayerEntityByUUID(UUID.fromString((String)this.dataManager.get(BaseGirlEntity.MASTER)));
   }

   public void updateTask() {
      this.state = this.getCurrentState();
      if (this.girl.watchClosestGirlGoal != null) {
         this.girl.watchClosestGirlGoal.isWatching = this.state == GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
      }

      this.a(this.state);
   }

   protected abstract GirlFollowAiBase.GirlFollowAiBaseState getCurrentState();

   protected abstract void a(GirlFollowAiBase.GirlFollowAiBaseState var1);

   @SubscribeEvent
   public void a(LivingDeathEvent var1) {
      if (var1.getEntityLiving() instanceof BaseGirlEntity) {
         BaseGirlEntity var2 = (BaseGirlEntity)var1.getEntityLiving();
         if (!((String)var2.getDataManager().get(BaseGirlEntity.MASTER)).equals("")) {
            var1.setCanceled(true);
         }
      }
   }

   public enum GirlFollowAiBaseState {
      ATTACK,
      FOLLOW,
      IDLE,
      RIDE,
      DOWNED;
   }
}
