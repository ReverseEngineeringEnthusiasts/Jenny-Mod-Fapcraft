package com.trolmastercard.sexmod.entity.ai;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;
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
   public BaseGirlEntity d;
   public EntityPlayer a;
   public PathNavigate c;
   public EntityDataManager e;
   public GirlFollowAiBase.GirlFollowAiBaseState f = GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
   public static final double g = 0.5;
   public static final double h = 0.7;
   public static final int b = 60;

   public GirlFollowAiBase(BaseGirlEntity var1) {
      this.d = var1;
      this.c = var1.getNavigator();
      this.e = var1.getDataManager();
   }

   protected void c_clash805() {
      int var2 = 0;

      BlockPos var1;
      do {
         var1 = this.a.getPosition().add(Reference.f.nextInt(10), 0, Reference.f.nextInt(10));
      } while (++var2 < 20 && !this.d.attemptTeleport(var1.getX(), var1.getY(), var1.getZ()));

      if (var2 >= 20) {
         this.d.setPosition(this.a.posX, this.a.posY, this.a.posZ);
      }

      this.d.motionX = 0.0;
      this.d.motionY = 0.0;
      this.d.motionZ = 0.0;
   }

   protected double b_clash806() {
      float var1 = this.d.getDistance(this.a);
      double var2;
      BaseGirlEntity.BaseGirlEntityState var4;
      if (this.a.isSprinting()) {
         var2 = 0.7;
         var4 = BaseGirlEntity.BaseGirlEntityState.RUN;
      } else {
         var2 = 0.5;
         var4 = BaseGirlEntity.BaseGirlEntityState.WALK;
      }

      double var5 = Math.floor(var1 / 5.0F) * 0.2;
      var2 += var5;
      if (this.d.isInWater()) {
         var2 *= 60.0;
         var4 = BaseGirlEntity.BaseGirlEntityState.WALK;
      }

      this.c.setSpeed(var2);
      this.d.a(var4);
      return var2;
   }

   public void resetTask() {
      this.c.clearPath();
      this.f = GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
      this.d.b(fp.NULL);
      this.e.set(BaseGirlEntity.v, "");
      this.c = null;
      this.e = null;
      this.a = null;
   }

   public boolean shouldExecute() {
      return !((String)this.d.getDataManager().get(BaseGirlEntity.v)).equals("");
   }

   public boolean shouldContinueExecuting() {
      String var1 = (String)this.e.get(BaseGirlEntity.v);
      return !var1.equals("") && this.d.world.getPlayerEntityByUUID(UUID.fromString(var1)) != null;
   }

   public void startExecuting() {
      this.c = this.d.getNavigator();
      this.e = this.d.getDataManager();
      this.a = this.d.world.getPlayerEntityByUUID(UUID.fromString((String)this.e.get(BaseGirlEntity.v)));
   }

   public void updateTask() {
      this.f = this.a_clash807();
      if (this.d.o != null) {
         this.d.o.a = this.f == GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
      }

      this.a(this.f);
   }

   protected abstract GirlFollowAiBase.GirlFollowAiBaseState a_clash807();

   protected abstract void a(GirlFollowAiBase.GirlFollowAiBaseState var1);

   @SubscribeEvent
   public void a(LivingDeathEvent var1) {
      if (var1.getEntityLiving() instanceof BaseGirlEntity) {
         BaseGirlEntity var2 = (BaseGirlEntity)var1.getEntityLiving();
         if (!((String)var2.getDataManager().get(BaseGirlEntity.v)).equals("")) {
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
