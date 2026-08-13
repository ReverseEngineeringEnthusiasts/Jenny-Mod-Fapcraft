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

public class KoboldEggProjectileEntity extends EntityEnderPearl {
   public KoboldEggProjectileEntity(World var1) {
      super(var1);
   }

   public KoboldEggProjectileEntity(World var1, EntityLivingBase var2) {
      super(var1, var2);
   }

   protected void onImpact(RayTraceResult var1) {
      EntityLivingBase var2 = this.getThrower();
      if (var2 != null) {
         if (var1.typeOfHit == Type.BLOCK) {
            BlockPos var7 = var1.getBlockPos();
            TileEntity var10 = this.world.getTileEntity(var7);
            if (var10 instanceof TileEntityEndGateway) {
               TileEntityEndGateway var12 = (TileEntityEndGateway)var10;
               if (var2 instanceof EntityPlayerMP) {
                  CriteriaTriggers.ENTER_BLOCK.trigger((EntityPlayerMP)var2, this.world.getBlockState(var7));
               }

               var12.teleportEntity(var2);
               this.setDead();
               return;
            }
         }

         for (int var8 = 0; var8 < 32; var8++) {
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
            BaseGirlEntity var9 = (BaseGirlEntity)var2;
            if (var9.l.distanceTo(this.getPositionVector()) < 5.0) {
               EnderTeleportEvent var11 = new EnderTeleportEvent(var2, this.posX, this.posY, this.posZ, 5.0F);
               if (!MinecraftForge.EVENT_BUS.post(var11)) {
                  if (var2.isRiding()) {
                     var2.dismountRidingEntity();
                  }

                  var2.setPositionAndUpdate(this.posX, this.posY, this.posZ);
                  var2.fallDistance = 0.0F;
               }
            }

            this.setDead();
         }
      } else {
         if (var1.typeOfHit == Type.BLOCK) {
            BlockPos var3 = var1.getBlockPos();
            TileEntity var4 = this.world.getTileEntity(var3);
            if (var4 instanceof TileEntityEndGateway) {
               TileEntityEndGateway var5 = (TileEntityEndGateway)var4;
               var5.teleportEntity(this);
               return;
            }
         }

         for (int var6 = 0; var6 < 32; var6++) {
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
      public void a(EnderTeleportEvent var1) {
         if (var1.getEntityLiving() instanceof BaseGirlEntity) {
            BaseGirlEntity var2 = (BaseGirlEntity)var1.getEntityLiving();
            var2.q = null;
            var2.b(fp.NULL);
            var2.getDataManager().set(BaseGirlEntity.G, false);
            var2.x_clash475();
         }
      }
   }
}
