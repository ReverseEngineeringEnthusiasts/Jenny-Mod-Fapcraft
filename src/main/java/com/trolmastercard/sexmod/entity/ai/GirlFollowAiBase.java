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

/**
 * <b>Role.</b> Base class for the girl follow goals. Runs while the girl has
 * a master UUID in the {@code MASTER} data key (110); the subclass state
 * machine picks one of {@link GirlFollowAiBaseState#ATTACK}/{@link #FOLLOW}/
 * {@link #IDLE}/{@link #RIDE}/{@link #DOWNED} each tick. Shared helpers:
 * teleport-fallback navigation ({@link #updateNavigation()}), speed/state
 * derivation from the master's sprint state ({@link #getFollowDistance()}).
 * <p>
 * <b>Pitfalls.</b> {@link #resetTask()} clears the master binding and nulls
 * the cached navigator/data-manager — a reset followed by a re-start
 * re-derives them in {@link #startExecuting()}. The
 * {@link #onLivingDeath(LivingDeathEvent)} handler cancels girl deaths while
 * a master is bound. {@code resetTask} sets {@link Action#NULL} via
 * {@code setCurrentAction} — routed through the packet on the client.
 */
public abstract class GirlFollowAiBase extends EntityAIBase {
   public BaseGirlEntity girl;
   public EntityPlayer master;
   public PathNavigate navigator;
   public EntityDataManager dataManager;
   public GirlFollowAiBase.GirlFollowAiBaseState state = GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
   public static final double followDistance = 0.5;
   public static final double attackDistance = 0.7;
   public static final int updateTicks = 60;

   public GirlFollowAiBase(BaseGirlEntity girl) {
      this.girl = girl;
      this.navigator = girl.getNavigator();
      this.dataManager = girl.getDataManager();
   }

   /**
    * Teleport-fallback navigation: tries up to 20 random offsets around the
    * master; if none is teleportable the girl snaps onto the master's
    * position. Always zeroes her motion afterwards.
    */
   protected void updateNavigation() {
      int attempts = 0;

      BlockPos targetPos;
      do {
         targetPos = this.master.getPosition().add(Reference.RANDOM.nextInt(10), 0, Reference.RANDOM.nextInt(10));
      } while (++attempts < 20 && !this.girl.attemptTeleport(targetPos.getX(), targetPos.getY(), targetPos.getZ()));

      if (attempts >= 20) {
         this.girl.setPosition(this.master.posX, this.master.posY, this.master.posZ);
      }

      this.girl.motionX = 0.0;
      this.girl.motionY = 0.0;
      this.girl.motionZ = 0.0;
   }

   protected double getFollowDistance() {
      float dist = this.girl.getDistance(this.master);
      double followDistance;
      BaseGirlEntity.BaseGirlEntityState state;
      if (this.master.isSprinting()) {
         followDistance = 0.7;
         state = BaseGirlEntity.BaseGirlEntityState.RUN;
      } else {
         followDistance = 0.5;
         state = BaseGirlEntity.BaseGirlEntityState.WALK;
      }

      double extra = Math.floor(dist / 5.0F) * 0.2;
      followDistance += extra;
      if (this.girl.isInWater()) {
         followDistance *= 60.0;
         state = BaseGirlEntity.BaseGirlEntityState.WALK;
      }

      this.navigator.setSpeed(followDistance);
      this.girl.setWalkSpeed(state);
      return followDistance;
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
      String masterUuid = (String)this.dataManager.get(BaseGirlEntity.MASTER);
      return !masterUuid.equals("") && this.girl.world.getPlayerEntityByUUID(UUID.fromString(masterUuid)) != null;
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

      this.setState(this.state);
   }

   protected abstract GirlFollowAiBase.GirlFollowAiBaseState getCurrentState();

   protected abstract void setState(GirlFollowAiBase.GirlFollowAiBaseState state);

   @SubscribeEvent
   public void onLivingDeath(LivingDeathEvent event) {
      if (event.getEntityLiving() instanceof BaseGirlEntity) {
         BaseGirlEntity girl = (BaseGirlEntity)event.getEntityLiving();
         if (!((String)girl.getDataManager().get(BaseGirlEntity.MASTER)).equals("")) {
            event.setCanceled(true);
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
