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

/**
 * <b>Role.</b> The wild Galath's flight action state machine. Each enum
 * constant bundles a start/update/finish callback set (via
 * {@link IGalathStart}/{@link IGalathUpdate}/{@link IGalathFinish}/
 * {@link GalathActionListener}/{@link IGalathExecute}) that drives one
 * behaviour:
 * <ul>
 * <li>{@link #CHANGE_POSITION} — pick a new flight position near the target
 * (weighted by open air space), glide there, then pick the next action.</li>
 * <li>{@link #SUMMON_SKELETON} — charge the two energy balls ({@code ad}
 * ticks) and spawn {@link DragonEntity} charges at the target from the hands
 * (mirrored by the {@code ay} data key); only while fewer than 2 skeleton
 * charges exist.</li>
 * <li>{@link #ATTACK_SWORD} — lunge at the target, damage at sword-progress
 * 36/40, then back off to flight.</li>
 * <li>{@link #RAPE} — hover-then-pounce onto a grounded player; on contact
 * clears the skeleton charges and starts the rape scene
 * ({@link Action#RAPE_INTRO}, player movement locked, entity look packet).
 * The pounce path is an eased progress lerp along a parabolic flight path
 * (uses {@link RotationHelper#lerpDouble} with ease curves), with the
 * progress stored in the {@code bO} data key.</li>
 * </ul>
 * <p>
 * <b>Flow.</b> {@link GalathEntity#initFlightData()} picks a random constant
 * whose {@code canExecuteAction} predicate passes and calls
 * {@link #executeStart(GalathEntity)}; every AI tick
 * {@link #executeUpdate(GalathEntity)} runs the movement lambda, and when it
 * reports finished, {@link #checkFinished(GalathEntity)} runs the finish
 * lambda so the next action can be chosen.
 * <p>
 * <b>Pitfalls.</b> {@code applyAttackCoolDown} constants must end with the
 * attack cooldown collapse (initFlightData switches to CHANGE_POSITION after
 * them). {@code onlyDoThisOnPlayers} actions must never be picked for mob
 * targets. The RAPE pounce MUST use progress lerps (0..1) on the segment
 * halves — this is a path interpolation, not the 40-tick dismount lerp, so
 * {@code lerpVec3dDouble} is correct here.
 */
public enum GalathFlightData {
   CHANGE_POSITION(
      galath -> {
         World world = galath.world;
         BlockPos pos = galath.getPosition();
         BlockPos targetPos = galath.getTargetEntity().getPosition();
         ArrayList candidates = new ArrayList();
         HashMap weighted = new HashMap();
         int bestWeight = 0;
         boolean onGround = !world.isAirBlock(pos.down());

         for (int dx = -10; dx < 10; dx++) {
            for (int dy = -10; dy < 10; dy++) {
               for (int dz = -10; dz < 10; dz++) {
                  if (dx != 0 || dy != 0 || dz != 0) {
                     BlockPos candidatePos = targetPos.add(new BlockPos(dx, dy, dz));
                     if ((!onGround || pos.getY() < candidatePos.getY())
                        && world.isAirBlock(candidatePos)
                        && world.isAirBlock(candidatePos.up())
                        && world.isAirBlock(candidatePos.up().up())) {
                        RayTraceResult rayTrace = world.rayTraceBlocks(new Vec3d(pos), new Vec3d(candidatePos), true, true, true);
                        if (rayTrace == null) {
                           int groundY = candidatePos.getY();

                           do {
                              groundY--;
                           } while (
                              groundY >= 0
                                 && world.getBlockState(new BlockPos(candidatePos.getX(), groundY, candidatePos.getZ())).getBlock() instanceof BlockAir
                           );

                           if (!(world.getBlockState(new BlockPos(candidatePos.getX(), groundY, candidatePos.getZ())).getBlock() instanceof BlockLiquid)) {
                              candidates.add(candidatePos);
                              if (world.isAirBlock(candidatePos.down())
                                 && world.isAirBlock(candidatePos.down().down())
                                 && !(targetPos.getDistance(candidatePos.getX(), candidatePos.getY(), candidatePos.getZ()) < 5.0)
                                 && !(pos.getDistance(candidatePos.getX(), candidatePos.getY(), candidatePos.getZ()) < 3.0)) {
                                 int count = 0;

                                 for (int ox = -1; ox < 2; ox++) {
                                    for (int oz = -1; oz < 2; oz++) {
                                       for (int oy = -1; oy < 4; oy++) {
                                          if (world.isAirBlock(candidatePos.add(ox, oy, oz))) {
                                             count++;
                                          }
                                       }
                                    }
                                 }

                                 if (count >= 25) {
                                    weighted.put(candidatePos, count);
                                    if (count > bestWeight) {
                                       bestWeight = count;
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

         if (!weighted.isEmpty()) {
            ArrayList<Entry> entries = new ArrayList<Entry>(weighted.entrySet());
            entries.sort((entryA, entryB) -> ((Integer)((Entry) galath).getValue()).compareTo((Integer)((Entry) galath).getValue()));
            galath.flightTargetPosition = new Vec3d((Vec3i)((Entry)entries.get(ThreadNames.weightedRandomIndex(entries.size() - 1))).getKey());
         } else if (candidates.isEmpty()) {
            galath.flightTargetPosition = new Vec3d(
               targetPos.add(ThreadNames.randomSignedFloat(10.0F, true), ThreadNames.randomSignedFloat(10.0F, false), ThreadNames.randomSignedFloat(10.0F, true))
            );
         } else {
            galath.flightTargetPosition = new Vec3d((Vec3i)candidates.get(Reference.RANDOM.nextInt(candidates.size())));
         }

         galath.bL = null;
         galath.setSwordAttackProgress(0);
         galath.setCurrentAction(Action.FLY);
         PacketHandler.networkWrapper.sendToAllTracking(new ResetControllerPacket(galath.getGirlId()), galath);
      },
      galath -> {
         Vec3d pos = galath.getPositionVector();
         Vec3d targetPos = galath.flightTargetPosition;
         if (targetPos != null) {
            galath.bL = pos;
            int attackProgress = galath.ar();
            galath.setSwordAttackProgress(attackProgress + 1);
            if (attackProgress == 0) {
               Vec3d delta = targetPos.subtract(pos);
               Vec3d dir = delta.normalize();
               galath.motionX = dir.x * 0.6F;
               galath.motionZ = dir.z * 0.6F;
               galath.motionY = (float)ThreadNames.clampDouble(delta.y * 0.6F, -0.6F, 0.6F);
            }
         }
      },
      galath -> galath.ar() > 23,
      galath -> {
         galath.setVelocity(Vec3d.ZERO);
         galath.setSwordAttackProgress(0);
         galath.bL = null;
      },
      false,
      galath -> true,
      false
   ),
   SUMMON_SKELETON(
      galath -> {
         galath.setCurrentAction(Action.SUMMON_SKELETON);
         galath.ad = 0;
         EntityDataManager dataManager = galath.getDataManager();
         dataManager.set(GalathEntity.bN, true);
         dataManager.set(GalathEntity.b7, true);
         dataManager.set(GalathEntity.ay, galath.getRNG().nextBoolean());
         BaseGirlEntity.playRandomSound(galath, SoundHandler.GIRLS_GALATH_STRONGCHARGE, true);
      },
      galath -> {
         galath.setVelocity(Vec3d.ZERO);
         if (galath.ad == 30.0F) {
            GalathEntity.getAimYaw(galath, 0.0F);
            Vec3d pos = galath.getPositionVector();
            Vec3d targetPos = galath.getTargetEntity().getPositionVector();
            Random random = galath.getRNG();
            if ((Boolean)galath.getDataManager().get(GalathEntity.ay)) {
               if ((Boolean)galath.getDataManager().get(GalathEntity.bN)) {
                  Vec3d headPos = pos;
                  Vec3d headAnchor = headPos.add(VectorMath.rotateByYaw(VectorMath.MirrorXZ(GalathEntity.bz), 180.0F + galath.renderYawOffset));
                  Vec3d aim = targetPos.subtract(headAnchor).normalize();
                  aim = new Vec3d(
                     aim.x + random.nextDouble() * 0.3F,
                     aim.y + random.nextDouble() * 0.3F,
                     aim.z + random.nextDouble() * 0.3F
                  );
                  aim = aim.normalize();
                  Vec3d headVel = new Vec3d(aim.x * 0.4F, aim.y * 0.4F, aim.z * 0.4F);
                  DragonEntity dragon = new DragonEntity(galath.world, galath, headVel);
                  dragon.setPositionAndUpdate(headAnchor.x, headAnchor.y, headAnchor.z);
                  galath.world.spawnEntity(dragon);
               }

               if ((Boolean)galath.getDataManager().get(GalathEntity.b7)) {
                  Vec3d backPos = pos;
                  Vec3d backAnchor = backPos.add(VectorMath.rotateByYaw(VectorMath.MirrorXZ(GalathEntity.bC), 180.0F + galath.renderYawOffset));
                  Vec3d aim2 = targetPos.subtract(backAnchor).normalize();
                  aim2 = new Vec3d(
                     aim2.x + random.nextDouble() * 0.3F,
                     aim2.y + random.nextDouble() * 0.3F,
                     aim2.z + random.nextDouble() * 0.3F
                  );
                  aim2 = aim2.normalize();
                  Vec3d backVel = new Vec3d(aim2.x * 0.4F, aim2.y * 0.4F, aim2.z * 0.4F);
                  DragonEntity dragon2 = new DragonEntity(galath.world, galath, backVel);
                  dragon2.setPositionAndUpdate(backAnchor.x, backAnchor.y, backAnchor.z);
                  galath.world.spawnEntity(dragon2);
               }
            } else {
               if ((Boolean)galath.getDataManager().get(GalathEntity.bN)) {
                  Vec3d pos9 = pos;
                  Vec3d anchor5 = pos9.add(VectorMath.rotateByYaw(GalathEntity.bz, 180.0F + galath.renderYawOffset));
                  Vec3d aim3 = targetPos.subtract(anchor5).normalize();
                  aim3 = new Vec3d(
                     aim3.x + random.nextDouble() * 0.3F,
                     aim3.y + random.nextDouble() * 0.3F,
                     aim3.z + random.nextDouble() * 0.3F
                  );
                  aim3 = aim3.normalize();
                  Vec3d vel7 = new Vec3d(aim3.x * 0.4F, aim3.y * 0.4F, aim3.z * 0.4F);
                  DragonEntity dragon3 = new DragonEntity(galath.world, galath, vel7);
                  dragon3.setPositionAndUpdate(anchor5.x, anchor5.y, anchor5.z);
                  galath.world.spawnEntity(dragon3);
               }

               if ((Boolean)galath.getDataManager().get(GalathEntity.b7)) {
                  Vec3d pos10 = pos;
                  Vec3d anchor11 = pos10.add(VectorMath.rotateByYaw(GalathEntity.bC, 180.0F + galath.renderYawOffset));
                  Vec3d aim4 = targetPos.subtract(anchor11).normalize();
                  aim4 = new Vec3d(
                     aim4.x + random.nextDouble() * 0.3F,
                     aim4.y + random.nextDouble() * 0.3F,
                     aim4.z + random.nextDouble() * 0.3F
                  );
                  aim4 = aim4.normalize();
                  Vec3d vel25 = new Vec3d(aim4.x * 0.4F, aim4.y * 0.4F, aim4.z * 0.4F);
                  DragonEntity dragon4 = new DragonEntity(galath.world, galath, vel25);
                  dragon4.setPositionAndUpdate(anchor11.x, anchor11.y, anchor11.z);
                  galath.world.spawnEntity(dragon4);
               }
            }
         }
      },
      galath -> galath.ad >= 45,
      galath -> galath.ad = 0,
      true,
      galath -> galath.bI.size() < 2,
      true
   ),
   ATTACK_SWORD(galath -> {
      galath.setSwordAttackProgress(0);
      galath.setCurrentAction(Action.ATTACK_SWORD);
      galath.setVelocity(Vec3d.ZERO);
      Vec3d pos = galath.getPositionVector();
      galath.setFlightTargetPos(pos);
      Vec3d targetPos = galath.getTargetEntity().getPositionVector();
      Vector2d delta = new Vector2d(targetPos.x - pos.x, targetPos.z - pos.z);
      double yaw = TrigMath.sinDegrees(Math.atan2(delta.x, delta.y)) - 90.0;
      galath.setAnchored(true);
      galath.setTargetPosition(pos);
      galath.setYawRotation((float)yaw);
      BaseGirlEntity.playRandomSound(galath, SoundHandler.GIRLS_GALATH_STRONGCHARGE, true);
   }, galath -> {
      EntityLivingBase target = galath.getTargetEntity();
      int attackProgress = galath.az() + 1;
      galath.setSwordAttackProgress(attackProgress);
      if (ThreadNames.isBetween(attackProgress, 24.0, 32.0)) {
         Vec3d eyePos = target.getPositionVector().add(0.0, target.getEyeHeight(), 0.0);
         Vector2d delta2 = new Vector2d(eyePos.x - galath.posX, eyePos.z - galath.posZ);
         double yaw = TrigMath.sinDegrees(Math.atan2(delta2.x, delta2.y)) - 90.0;
         galath.setYawRotation((float)yaw);
         Vec3d forward = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 3.0), (float)(yaw + 180.0));
         Vec3d from = galath.B_clash642();
         Vec3d to = eyePos.add(forward);
         float progress = (attackProgress - 24) / 8.0F;
         Vec3d lerped = RotationHelper.lerpVec3dDouble(from, to, progress);
         galath.setTargetPosition(lerped);
      } else if (ThreadNames.isBetween(attackProgress, 32.0, 54.0)) {
         Vec3d behind = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 1.5), galath.getYawRotation() + 180.0F);
         Vec3d targetPos2 = target.getPositionVector().add(behind);
         galath.setTargetPosition(targetPos2);
         GalathDamageSource damageSource = new GalathDamageSource(galath);
         target.hurtTime = 0;
         target.hurtResistantTime = 0;
         if (attackProgress == 36) {
            target.attackEntityFrom(damageSource, 5.0F);
         }

         if (attackProgress == 40) {
            target.attackEntityFrom(damageSource, 5.0F);
         }
      } else if (attackProgress == 54) {
         galath.setAnchored(false);
         galath.setCurrentAction(Action.FLY);
         Vec3d dir = galath.B_clash642().subtract(galath.getPositionVector()).normalize();
         galath.motionX = dir.x * 0.6F;
         galath.motionY = dir.y * 0.6F;
         galath.motionZ = dir.z * 0.6F;
         galath.setSwordAttackProgress(1);
      } else {
         galath.setSwordAttackProgress(galath.ar() + 1);
      }
   }, galath -> galath.ar() > 23, galath -> {
      galath.setSwordAttackProgress(0);
      galath.setVelocity(Vec3d.ZERO);
      galath.setSwordAttackProgress(-1);
      galath.setAnchored(false);
   }, true, galath -> true, false),
   RAPE(
      galath -> {
         galath.setCurrentAction(Action.RAPE_PREPARE);
         galath.aF = 0;
         galath.bd = null;
         galath.flightTargetPosition = null;
         galath.getDataManager().set(GalathEntity.bO, 0.0F);
      },
      galath -> {
         if (++galath.aF >= 48) {
            galath.setCurrentAction(Action.RAPE_CHARGE);
            EntityLivingBase target = galath.getTargetEntity();
            if (galath.bd == null) {
               galath.flightTargetPosition = target.getPositionVector().add(0.0, target.getEyeHeight() / 2.0F, 0.0);
               galath.bd = galath.getPositionVector();
               Vec3d dir = target.getPositionVector().subtract(galath.getPositionVector()).normalize();
               galath.setYawRotation((float)(TrigMath.sinDegrees(Math.atan2(dir.z, dir.x)) - 90.0));
            }

            Vec3d pos = galath.getPositionVector();
            Vec3d min = pos.subtract(0.65F, 0.65F, 0.65F);
            Vec3d max = pos.add(0.65F, 0.65F, 0.65F);
            AxisAlignedBB aabb = new AxisAlignedBB(
               min.x, min.y, min.z, max.x, max.y, max.z
            );

            for (EntityPlayer player : galath.world.getEntitiesWithinAABB(EntityPlayer.class, aabb)) {
               if (!player.isDead && player.onGround && BaseGirlEntity.getGirlByUUID(player.getPersistentID(), Boolean.valueOf(true)) == null) {
                  Vec3d playerPos = player.getPositionVector();
                  Vec3d toPlayer = pos.subtract(playerPos);
                  Vec3d rotated = VectorMath.rotateByYaw(toPlayer, galath.getYawRotation());
                  double dist = Math.abs(rotated.x);
                  if (!(dist > 0.65F)) {
                     for (EntityWitherSkeleton skeleton : galath.bI) {
                        Vec3d skeletonPos = skeleton.getPositionVector();
                        skeleton.world.removeEntity(skeleton);
                        PacketHandler.networkWrapper
                           .sendToAllTracking(
                              new SpawnEnergyBallParticlesPacket2(skeletonPos, true),
                              new TargetPoint(skeleton.dimension, skeletonPos.x, skeletonPos.y, skeletonPos.z, 50.0)
                           );
                     }

                     galath.bI.clear();
                     EntityPlayerMP playerMP = (EntityPlayerMP)player;
                     galath.setTargetPosition(player.getPositionVector());
                     galath.setInteractionPlayerUUID(player.getPersistentID());
                     galath.setAnchored(true);
                     galath.setCurrentAction(Action.RAPE_INTRO);
                     byte yawByte = (byte)MathHelper.floor((galath.getYawRotation() + 180.0F) * 256.0F / 360.0F);
                     PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), playerMP);
                     playerMP.connection.sendPacket(new SPacketEntityVelocity(playerMP.getEntityId(), 0.0, 0.0, 0.0));
                     playerMP.connection.sendPacket(new S16PacketEntityLook(playerMP.getEntityId(), yawByte, (byte)-14, true));
                     return;
                  }
               }
            }

            Vec3d from = galath.bd;
            Vec3d to = galath.flightTargetPosition;
            Vec3d delta = to.subtract(from);
            Vec3d newPos = to.add(delta);
            newPos = new Vec3d(newPos.x, from.y, newPos.z);
            boolean isPast = pos.distanceTo(new Vec3d(from.x, pos.y, from.z))
               > pos.distanceTo(new Vec3d(newPos.x, pos.y, newPos.z));
            double distAlong;
            double totalDist;
            if (isPast) {
               distAlong = VectorMath.getLinearFactor(to, newPos, pos);
               totalDist = to.distanceTo(newPos);
            } else {
               distAlong = VectorMath.getLinearFactor(from, to, pos);
               totalDist = from.distanceTo(to);
            }

            double steps = totalDist / 0.05F;
            double speedFactor = 1.0 / steps * 20.0;
            distAlong += speedFactor;
            if (!isPast && distAlong < 0.9F) {
               galath.flightTargetPosition = target.getPositionVector().add(0.0, target.getEyeHeight() / 2.0F, 0.0);
            }

            if (isPast) {
               pos = new Vec3d(
                  RotationHelper.lerpDouble(to.x, newPos.x, Math.min(1.0, distAlong)),
                  RotationHelper.lerpDouble(to.y, newPos.y, Math.min(1.0, RotationHelper.easeInCubic(distAlong))),
                  RotationHelper.lerpDouble(to.z, newPos.z, Math.min(1.0, distAlong))
               );
            } else {
               pos = new Vec3d(
                  RotationHelper.lerpDouble(from.x, to.x, distAlong),
                  RotationHelper.lerpDouble(from.y, to.y, RotationHelper.easeInOutQuad(distAlong)),
                  RotationHelper.lerpDouble(from.z, to.z, distAlong)
               );
            }

            galath.setPosition(pos.x, pos.y, pos.z);
            if (isPast) {
               galath.getDataManager().set(GalathEntity.bO, (float)distAlong);
            }
         }
      },
      galath -> {
         if (galath.getCurrentAction() == Action.RAPE_INTRO) {
            return true;
         }

         Vec3d from = galath.bd;
         Vec3d to = galath.flightTargetPosition;
         if (from == null) {
            return false;
         }

         Vec3d delta = to.subtract(from);
         Vec3d newPos = to.add(delta);
         newPos = new Vec3d(newPos.x, from.y, newPos.z);
         return galath.getDistance(newPos.x, newPos.y, newPos.z) < 0.1F;
      },
      galath -> {
         galath.flightTargetPosition = null;
         galath.bd = null;
         galath.aF = 0;
         galath.getDataManager().set(GalathEntity.bO, 0.0F);
      },
      true,
      galath -> true,
      true
   );

   final IGalathUpdate updateAction;
   final IGalathStart startAction;
   final IGalathFinish finishAction;
   final GalathActionListener stopAction;
   final IGalathExecute canExecuteAction;
   public final boolean applyAttackCoolDown;
   public final boolean onlyDoThisOnPlayers;

   GalathFlightData(IGalathStart startAction, IGalathFinish finishAction, IGalathUpdate updateAction, GalathActionListener stopAction, boolean applyAttackCoolDown, IGalathExecute canExecuteAction, boolean onlyDoThisOnPlayers) {
      this.updateAction = updateAction;
      this.startAction = startAction;
      this.finishAction = finishAction;
      this.stopAction = stopAction;
      this.applyAttackCoolDown = applyAttackCoolDown;
      this.canExecuteAction = canExecuteAction;
      this.onlyDoThisOnPlayers = onlyDoThisOnPlayers;
   }

   public void executeStart(GalathEntity galath) {
      this.startAction.start(galath);
   }

   public boolean executeUpdate(GalathEntity galath) {
      return this.updateAction.update(galath);
   }

   public void checkFinished(GalathEntity galath) {
      this.finishAction.finish(galath);
   }

   public void updateFlight(GalathEntity galath) {
      this.stopAction.stop(galath);
   }

   public boolean canExecute(GalathEntity galath) {
      return this.canExecuteAction.canExecute(galath);
   }

}
