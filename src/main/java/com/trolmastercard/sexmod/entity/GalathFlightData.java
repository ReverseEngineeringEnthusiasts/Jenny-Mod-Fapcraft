package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.GalathActionListener;
import com.trolmastercard.sexmod.api.IGalathFinish;
import com.trolmastercard.sexmod.api.IGalathStart;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetControllerPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.networking.SpawnEnergyBallParticlesPacket2;
import com.trolmastercard.sexmod.util.GalathDamageSource;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.IGalathExecute;
import com.trolmastercard.sexmod.util.Vector2d;
import com.trolmastercard.sexmod.util.TrigMath;
import com.trolmastercard.sexmod.util.IGalathUpdate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketEntity.S16PacketEntityLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public enum GalathFlightData {
   CHANGE_POSITION(
      var0 -> {
         World var1 = var0.world;
         BlockPos var2 = var0.getPosition();
         BlockPos var3 = var0.getTargetEntity().getPosition();
         ArrayList var4 = new ArrayList();
         HashMap var5 = new HashMap();
         int var6 = 0;
         boolean var7 = !var1.isAirBlock(var2.down());

         for (int var8 = -10; var8 < 10; var8++) {
            for (int var9 = -10; var9 < 10; var9++) {
               for (int var10 = -10; var10 < 10; var10++) {
                  if (var8 != 0 || var9 != 0 || var10 != 0) {
                     BlockPos var11 = var3.add(new BlockPos(var8, var9, var10));
                     if ((!var7 || var2.getY() < var11.getY())
                        && var1.isAirBlock(var11)
                        && var1.isAirBlock(var11.up())
                        && var1.isAirBlock(var11.up().up())) {
                        RayTraceResult var12 = var1.rayTraceBlocks(new Vec3d(var2), new Vec3d(var11), true, true, true);
                        if (var12 == null) {
                           int var13 = var11.getY();

                           do {
                              var13--;
                           } while (
                              var13 >= 0
                                 && var1.getBlockState(new BlockPos(var11.getX(), var13, var11.getZ())).getBlock() instanceof BlockAir
                           );

                           if (!(var1.getBlockState(new BlockPos(var11.getX(), var13, var11.getZ())).getBlock() instanceof BlockLiquid)) {
                              var4.add(var11);
                              if (var1.isAirBlock(var11.down())
                                 && var1.isAirBlock(var11.down().down())
                                 && !(var3.getDistance(var11.getX(), var11.getY(), var11.getZ()) < 5.0)
                                 && !(var2.getDistance(var11.getX(), var11.getY(), var11.getZ()) < 3.0)) {
                                 int var14 = 0;

                                 for (int var15 = -1; var15 < 2; var15++) {
                                    for (int var16 = -1; var16 < 2; var16++) {
                                       for (int var17 = -1; var17 < 4; var17++) {
                                          if (var1.isAirBlock(var11.add(var15, var17, var16))) {
                                             var14++;
                                          }
                                       }
                                    }
                                 }

                                 if (var14 >= 25) {
                                    var5.put(var11, var14);
                                    if (var14 > var6) {
                                       var6 = var14;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         if (!var5.isEmpty()) {
            ArrayList<Entry> var18 = new ArrayList<Entry>(var5.entrySet());
            var18.sort((var0x, var1x) -> ((Integer)((Entry) var1x).getValue()).compareTo((Integer)((Entry) var0x).getValue()));
            var0.flightTargetPosition = new Vec3d((Vec3i)((Entry)var18.get(ThreadNames.weightedRandomIndex(var18.size() - 1))).getKey());
         } else if (var4.isEmpty()) {
            var0.flightTargetPosition = new Vec3d(
               var3.add(ThreadNames.randomSignedFloat(10.0F, true), ThreadNames.randomSignedFloat(10.0F, false), ThreadNames.randomSignedFloat(10.0F, true))
            );
         } else {
            var0.flightTargetPosition = new Vec3d((Vec3i)var4.get(Reference.RANDOM.nextInt(var4.size())));
         }

         var0.bL = null;
         var0.setSwordAttackProgress(0);
         var0.setCurrentAction(Action.FLY);
         PacketHandler.networkWrapper.sendToAllTracking(new ResetControllerPacket(var0.getGirlId()), var0);
      },
      var0 -> {
         Vec3d var1 = var0.getPositionVector();
         Vec3d var2 = var0.flightTargetPosition;
         if (var2 != null) {
            var0.bL = var1;
            int var3 = var0.ar();
            var0.setSwordAttackProgress(var3 + 1);
            if (var3 == 0) {
               Vec3d var4 = var2.subtract(var1);
               Vec3d var5 = var4.normalize();
               var0.motionX = var5.x * 0.6F;
               var0.motionZ = var5.z * 0.6F;
               var0.motionY = ThreadNames.b(var4.y * 0.6F, -0.6F, 0.6F);
            }
         }
      },
      var0 -> var0.ar() > 23,
      var0 -> {
         var0.setVelocity(Vec3d.ZERO);
         var0.setSwordAttackProgress(0);
         var0.bL = null;
      },
      false,
      var0 -> true,
      false
   ),
   SUMMON_SKELETON(
      var0 -> {
         var0.setCurrentAction(Action.SUMMON_SKELETON);
         var0.ad = 0;
         EntityDataManager var1 = var0.getDataManager();
         var1.set(GalathEntity.bN, true);
         var1.set(GalathEntity.b7, true);
         var1.set(GalathEntity.ay, var0.getRNG().nextBoolean());
         BaseGirlEntity.playRandomSound(var0, SoundHandler.GIRLS_GALATH_STRONGCHARGE, true);
      },
      var0 -> {
         var0.setVelocity(Vec3d.ZERO);
         if (var0.ad == 30.0F) {
            GalathEntity.getAimYaw(var0, 0.0F);
            Vec3d var1 = var0.getPositionVector();
            Vec3d var2 = var0.getTargetEntity().getPositionVector();
            Random var3 = var0.getRNG();
            if ((Boolean)var0.getDataManager().get(GalathEntity.ay)) {
               if ((Boolean)var0.getDataManager().get(GalathEntity.bN)) {
                  Vec3d var31 = var1;
                  Vec3d var12 = var31.add(VectorMath.rotateByYaw(VectorMath.MirrorXZ(GalathEntity.bz), 180.0F + var0.renderYawOffset));
                  Vec3d var19 = var2.subtract(var12).normalize();
                  var19 = new Vec3d(
                     var19.x + var3.nextDouble() * 0.3F,
                     var19.y + var3.nextDouble() * 0.3F,
                     var19.z + var3.nextDouble() * 0.3F
                  );
                  var19 = var19.normalize();
                  Vec3d var26 = new Vec3d(var19.x * 0.4F, var19.y * 0.4F, var19.z * 0.4F);
                  DragonEntity var29 = new DragonEntity(var0.world, var0, var26);
                  var29.setPositionAndUpdate(var12.x, var12.y, var12.z);
                  var0.world.spawnEntity(var29);
               }

               if ((Boolean)var0.getDataManager().get(GalathEntity.b7)) {
                  Vec3d var32 = var1;
                  Vec3d var13 = var32.add(VectorMath.rotateByYaw(VectorMath.MirrorXZ(GalathEntity.bC), 180.0F + var0.renderYawOffset));
                  Vec3d var22 = var2.subtract(var13).normalize();
                  var22 = new Vec3d(
                     var22.x + var3.nextDouble() * 0.3F,
                     var22.y + var3.nextDouble() * 0.3F,
                     var22.z + var3.nextDouble() * 0.3F
                  );
                  var22 = var22.normalize();
                  Vec3d var27 = new Vec3d(var22.x * 0.4F, var22.y * 0.4F, var22.z * 0.4F);
                  DragonEntity var30 = new DragonEntity(var0.world, var0, var27);
                  var30.setPositionAndUpdate(var13.x, var13.y, var13.z);
                  var0.world.spawnEntity(var30);
               }
            } else {
               if ((Boolean)var0.getDataManager().get(GalathEntity.bN)) {
                  Vec3d var9 = var1;
                  Vec3d var5 = var9.add(VectorMath.rotateByYaw(GalathEntity.bz, 180.0F + var0.renderYawOffset));
                  Vec3d var6 = var2.subtract(var5).normalize();
                  var6 = new Vec3d(
                     var6.x + var3.nextDouble() * 0.3F,
                     var6.y + var3.nextDouble() * 0.3F,
                     var6.z + var3.nextDouble() * 0.3F
                  );
                  var6 = var6.normalize();
                  Vec3d var7 = new Vec3d(var6.x * 0.4F, var6.y * 0.4F, var6.z * 0.4F);
                  DragonEntity var8 = new DragonEntity(var0.world, var0, var7);
                  var8.setPositionAndUpdate(var5.x, var5.y, var5.z);
                  var0.world.spawnEntity(var8);
               }

               if ((Boolean)var0.getDataManager().get(GalathEntity.b7)) {
                  Vec3d var10 = var1;
                  Vec3d var11 = var10.add(VectorMath.rotateByYaw(GalathEntity.bC, 180.0F + var0.renderYawOffset));
                  Vec3d var16 = var2.subtract(var11).normalize();
                  var16 = new Vec3d(
                     var16.x + var3.nextDouble() * 0.3F,
                     var16.y + var3.nextDouble() * 0.3F,
                     var16.z + var3.nextDouble() * 0.3F
                  );
                  var16 = var16.normalize();
                  Vec3d var25 = new Vec3d(var16.x * 0.4F, var16.y * 0.4F, var16.z * 0.4F);
                  DragonEntity var28 = new DragonEntity(var0.world, var0, var25);
                  var28.setPositionAndUpdate(var11.x, var11.y, var11.z);
                  var0.world.spawnEntity(var28);
               }
            }
         }
      },
      var0 -> var0.ad >= 45,
      var0 -> var0.ad = 0,
      true,
      var0 -> var0.bI.size() < 2,
      true
   ),
   ATTACK_SWORD(var0 -> {
      var0.setSwordAttackProgress(0);
      var0.setCurrentAction(Action.ATTACK_SWORD);
      var0.setVelocity(Vec3d.ZERO);
      Vec3d var1 = var0.getPositionVector();
      var0.e(var1);
      Vec3d var2 = var0.getTargetEntity().getPositionVector();
      Vector2d var3 = new Vector2d(var2.x - var1.x, var2.z - var1.z);
      double var4 = TrigMath.b(Math.atan2(var3.x, var3.y)) - 90.0;
      var0.setAnchored(true);
      var0.setTargetPosition(var1);
      var0.setYawRotation((float)var4);
      BaseGirlEntity.playRandomSound(var0, SoundHandler.GIRLS_GALATH_STRONGCHARGE, true);
   }, var0 -> {
      EntityLivingBase var1 = var0.getTargetEntity();
      int var2 = var0.az() + 1;
      var0.setSwordAttackProgress(var2);
      if (ThreadNames.isBetween(var2, 24.0, 32.0)) {
         Vec3d var3 = var1.getPositionVector().add(0.0, var1.getEyeHeight(), 0.0);
         Vector2d var4 = new Vector2d(var3.x - var0.posX, var3.z - var0.posZ);
         double var5 = TrigMath.b(Math.atan2(var4.x, var4.y)) - 90.0;
         var0.setYawRotation((float)var5);
         Vec3d var7 = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 3.0), (float)(var5 + 180.0));
         Vec3d var8 = var0.B_clash642();
         Vec3d var9 = var3.add(var7);
         float var10 = (var2 - 24) / 8.0F;
         Vec3d var11 = RotationHelper.a(var8, var9, var10);
         var0.setTargetPosition(var11);
      } else if (ThreadNames.isBetween(var2, 32.0, 54.0)) {
         Vec3d var12 = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 1.5), var0.getYawRotation() + 180.0F);
         Vec3d var14 = var1.getPositionVector().add(var12);
         var0.setTargetPosition(var14);
         GalathDamageSource var15 = new GalathDamageSource(var0);
         var1.hurtTime = 0;
         var1.hurtResistantTime = 0;
         if (var2 == 36) {
            var1.attackEntityFrom(var15, 5.0F);
         }

         if (var2 == 40) {
            var1.attackEntityFrom(var15, 5.0F);
         }
      } else if (var2 == 54) {
         var0.setAnchored(false);
         var0.setCurrentAction(Action.FLY);
         Vec3d var13 = var0.B_clash642().subtract(var0.getPositionVector()).normalize();
         var0.motionX = var13.x * 0.6F;
         var0.motionY = var13.y * 0.6F;
         var0.motionZ = var13.z * 0.6F;
         var0.setSwordAttackProgress(1);
      } else {
         var0.setSwordAttackProgress(var0.ar() + 1);
      }
   }, var0 -> var0.ar() > 23, var0 -> {
      var0.setSwordAttackProgress(0);
      var0.setVelocity(Vec3d.ZERO);
      var0.setSwordAttackProgress(-1);
      var0.setAnchored(false);
   }, true, var0 -> true, false),
   RAPE(
      var0 -> {
         var0.setCurrentAction(Action.RAPE_PREPARE);
         var0.aF = 0;
         var0.bd = null;
         var0.flightTargetPosition = null;
         var0.getDataManager().set(GalathEntity.bO, 0.0F);
      },
      var0 -> {
         if (++var0.aF >= 48) {
            var0.setCurrentAction(Action.RAPE_CHARGE);
            EntityLivingBase var1 = var0.getTargetEntity();
            if (var0.bd == null) {
               var0.flightTargetPosition = var1.getPositionVector().add(0.0, var1.getEyeHeight() / 2.0F, 0.0);
               var0.bd = var0.getPositionVector();
               Vec3d var2 = var1.getPositionVector().subtract(var0.getPositionVector()).normalize();
               var0.setYawRotation((float)(TrigMath.b(Math.atan2(var2.z, var2.x)) - 90.0));
            }

            Vec3d var20 = var0.getPositionVector();
            Vec3d var3 = var20.subtract(0.65F, 0.65F, 0.65F);
            Vec3d var4 = var20.add(0.65F, 0.65F, 0.65F);
            AxisAlignedBB var5 = new AxisAlignedBB(
               var3.x, var3.y, var3.z, var4.x, var4.y, var4.z
            );

            for (EntityPlayer var8 : var0.world.getEntitiesWithinAABB(EntityPlayer.class, var5)) {
               if (!var8.isDead && var8.onGround && BaseGirlEntity.getGirlByUUID(var8.getPersistentID(), Boolean.valueOf(true)) == null) {
                  Vec3d var9 = var8.getPositionVector();
                  Vec3d var10 = var20.subtract(var9);
                  Vec3d var11 = VectorMath.rotateByYaw(var10, var0.getYawRotation());
                  double var12 = Math.abs(var11.x);
                  if (!(var12 > 0.65F)) {
                     for (EntityWitherSkeleton var15 : var0.bI) {
                        Vec3d var16 = var15.getPositionVector();
                        var15.world.removeEntity(var15);
                        PacketHandler.networkWrapper
                           .sendToAllTracking(
                              new SpawnEnergyBallParticlesPacket2(var16, true),
                              new TargetPoint(var15.dimension, var16.x, var16.y, var16.z, 50.0)
                           );
                     }

                     var0.bI.clear();
                     EntityPlayerMP var30 = (EntityPlayerMP)var8;
                     var0.setTargetPosition(var8.getPositionVector());
                     var0.setInteractionPlayerUUID(var8.getPersistentID());
                     var0.setAnchored(true);
                     var0.setCurrentAction(Action.RAPE_INTRO);
                     byte var32 = (byte)MathHelper.floor((var0.getYawRotation() + 180.0F) * 256.0F / 360.0F);
                     PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), var30);
                     var30.connection.sendPacket(new SPacketEntityVelocity(var30.getEntityId(), 0.0, 0.0, 0.0));
                     var30.connection.sendPacket(new S16PacketEntityLook(var30.getEntityId(), var32, (byte)-14, true));
                     return;
                  }
               }
            }

            Vec3d var22 = var0.bd;
            Vec3d var23 = var0.flightTargetPosition;
            Vec3d var24 = var23.subtract(var22);
            Vec3d var25 = var23.add(var24);
            var25 = new Vec3d(var25.x, var22.y, var25.z);
            boolean var27 = var20.distanceTo(new Vec3d(var22.x, var20.y, var22.z))
               > var20.distanceTo(new Vec3d(var25.x, var20.y, var25.z));
            double var28;
            double var31;
            if (var27) {
               var28 = VectorMath.a(var23, var25, var20);
               var31 = var23.distanceTo(var25);
            } else {
               var28 = VectorMath.a(var22, var23, var20);
               var31 = var22.distanceTo(var23);
            }

            double var33 = var31 / 0.05F;
            double var18 = 1.0 / var33 * 20.0;
            var28 += var18;
            if (!var27 && var28 < 0.9F) {
               var0.flightTargetPosition = var1.getPositionVector().add(0.0, var1.getEyeHeight() / 2.0F, 0.0);
            }

            if (var27) {
               var20 = new Vec3d(
                  RotationHelper.b(var23.x, var25.x, Math.min(1.0, var28)),
                  RotationHelper.b(var23.y, var25.y, Math.min(1.0, RotationHelper.smoothDamp(var28))),
                  RotationHelper.b(var23.z, var25.z, Math.min(1.0, var28))
               );
            } else {
               var20 = new Vec3d(
                  RotationHelper.b(var22.x, var23.x, var28),
                  RotationHelper.b(var22.y, var23.y, RotationHelper.g(var28)),
                  RotationHelper.b(var22.z, var23.z, var28)
               );
            }

            var0.setPosition(var20.x, var20.y, var20.z);
            if (var27) {
               var0.getDataManager().set(GalathEntity.bO, (float)var28);
            }
         }
      },
      var0 -> {
         if (var0.getCurrentAction() == Action.RAPE_INTRO) {
            return true;
         }

         Vec3d var1 = var0.bd;
         Vec3d var2 = var0.flightTargetPosition;
         if (var1 == null) {
            return false;
         }

         Vec3d var3 = var2.subtract(var1);
         Vec3d var4 = var2.add(var3);
         var4 = new Vec3d(var4.x, var1.y, var4.z);
         return var0.getDistance(var4.x, var4.y, var4.z) < 0.1F;
      },
      var0 -> {
         var0.flightTargetPosition = null;
         var0.bd = null;
         var0.aF = 0;
         var0.getDataManager().set(GalathEntity.bO, 0.0F);
      },
      true,
      var0 -> true,
      true
   );

   final IGalathUpdate updateAction;
   final IGalathStart startAction;
   final IGalathFinish finishAction;
   final GalathActionListener stopAction;
   final IGalathExecute canExecuteAction;
   public final boolean applyAttackCoolDown;
   public final boolean onlyDoThisOnPlayers;

   GalathFlightData(IGalathStart var3, IGalathFinish var4, IGalathUpdate var5, GalathActionListener var6, boolean var7, IGalathExecute var8, boolean var9) {
      this.updateAction = var5;
      this.startAction = var3;
      this.finishAction = var4;
      this.stopAction = var6;
      this.applyAttackCoolDown = var7;
      this.canExecuteAction = var8;
      this.onlyDoThisOnPlayers = var9;
   }

   public void executeStart(GalathEntity var1) {
      this.startAction.start(var1);
   }

   public boolean executeUpdate(GalathEntity var1) {
      return this.updateAction.update(var1);
   }

   public void checkFinished(GalathEntity var1) {
      this.finishAction.finish(var1);
   }

   public void e(GalathEntity var1) {
      this.stopAction.stop(var1);
   }

   public boolean canExecute(GalathEntity var1) {
      return this.canExecuteAction.canExecute(var1);
   }

}
