package com.trolmastercard.sexmod.entity;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * <b>Role.</b> The ender-pearl projectile thrown by kobold girls
 * ({@link Action#THROW_PEARL}). Like a vanilla pearl it teleports the thrower
 * to the impact point — but only if the impact is within 5 blocks of the
 * girl's home position (pearls near home), with end-gateway support.
 * <p>
 * <b>Pitfalls.</b> The thrower is expected to be a {@link BaseGirlEntity}
 * (cast directly). The inner class {@code a} clears the girl's active pearl
 * and resets her scene state on every ender-teleport event.
 */
public class KoboldEggProjectileEntity extends EntityEnderPearl {
   public KoboldEggProjectileEntity(World world) {
      super(world);
   }

   public KoboldEggProjectileEntity(World world, EntityLivingBase thrower) {
      super(world, thrower);
   }

   /**
    * SERVER/CLIENT: impact handling — end gateways teleport the thrower
    * directly; otherwise portal particles spawn and (SERVER, only within
    * 5 blocks of the girl's home) the thrower is teleported via the
    * ender-teleport event and dismounted.
    */
   protected void onImpact(RayTraceResult rayTrace) {
      EntityLivingBase thrower = this.getThrower();
      if (thrower != null) {
         if (rayTrace.typeOfHit == Type.BLOCK) {
            BlockPos blockPos = rayTrace.getBlockPos();
            TileEntity tileEntity = this.world.getTileEntity(blockPos);
            if (tileEntity instanceof TileEntityEndGateway) {
               TileEntityEndGateway gateway = (TileEntityEndGateway)tileEntity;
               if (thrower instanceof EntityPlayerMP) {
                  CriteriaTriggers.ENTER_BLOCK.trigger((EntityPlayerMP)thrower, this.world.getBlockState(blockPos));
               }

               gateway.teleportEntity(thrower);
               this.setDead();
               return;
            }
         }

         for (int i = 0; i < 32; i++) {
            this.world
               .spawnParticle(
                  EnumParticleTypes.PORTAL,
                  this.posX,
                  this.posY + this.rand.nextDouble() * 2.0,
                  this.posZ,
                  this.rand.nextGaussian(),
                  0.0,
                  this.rand.nextGaussian(),
                  new int[0]
               );
         }

         if (!this.world.isRemote) {
            BaseGirlEntity girl = (BaseGirlEntity)thrower;
            if (girl.homePos.distanceTo(this.getPositionVector()) < 5.0) {
               EnderTeleportEvent teleportEvent = new EnderTeleportEvent(thrower, this.posX, this.posY, this.posZ, 5.0F);
               if (!MinecraftForge.EVENT_BUS.post(teleportEvent)) {
                  if (thrower.isRiding()) {
                     thrower.dismountRidingEntity();
                  }

                  thrower.setPositionAndUpdate(this.posX, this.posY, this.posZ);
                  thrower.fallDistance = 0.0F;
               }
            }

            this.setDead();
         }
      } else {
         if (rayTrace.typeOfHit == Type.BLOCK) {
            BlockPos blockPos = rayTrace.getBlockPos();
            TileEntity tileEntity = this.world.getTileEntity(blockPos);
            if (tileEntity instanceof TileEntityEndGateway) {
               TileEntityEndGateway gateway = (TileEntityEndGateway)tileEntity;
               gateway.teleportEntity(this);
               return;
            }
         }

         for (int i = 0; i < 32; i++) {
            this.world
               .spawnParticle(
                  EnumParticleTypes.PORTAL,
                  this.posX,
                  this.posY + this.rand.nextDouble() * 2.0,
                  this.posZ,
                  this.rand.nextGaussian(),
                  0.0,
                  this.rand.nextGaussian(),
                  new int[0]
               );
         }

         if (!this.world.isRemote) {
            this.setDead();
         }
      }
   }

   public static class a {
      @SubscribeEvent
      public void onEnderTeleport(EnderTeleportEvent event) {
         if (event.getEntityLiving() instanceof BaseGirlEntity) {
            BaseGirlEntity girl = (BaseGirlEntity)event.getEntityLiving();
            girl.activeEnderPearl = null;
            girl.setCurrentAction(Action.NULL);
            girl.getDataManager().set(BaseGirlEntity.IS_ANCHORED, false);
            girl.goHome();
         }
      }
   }
}
