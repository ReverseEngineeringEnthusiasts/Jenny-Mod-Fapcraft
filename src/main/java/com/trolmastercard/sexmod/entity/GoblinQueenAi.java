package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;

/**
 * Queen-goblin AI for {@link GoblinEntity}: the gold-steal cycle, the throne
 * breeding trigger and its intro/teardown cooldowns, plus the guard spawning
 * and throw/hold glue those depend on.
 * <p>
 * {@link #tickGoldSteal} sends a RUN goblin to steal gold from a nearby player
 * every 32000 ticks, {@link #tryStartBreeding} starts the breeding intro when a
 * player stands in the throne zone, {@link #handleJumpThrow} throws the guards
 * onto the player and {@link #handleThrowCooldown} releases two AWAIT_PICK_UP
 * guards after breeding. All entry points take the queen as an argument; they
 * only touch her public/package-private surface.
 */
final class GoblinQueenAi {
   private GoblinQueenAi() {
   }

   static void handleHeldThrow(GoblinEntity queen) {
      if (queen.isQueen) {
         if ((Boolean)queen.entityDataManager.get(GoblinEntity.aV)) {
            if (queen.impregnationTick + 8400L < queen.world.getTotalWorldTime()) {
               queen.entityDataManager.set(GoblinEntity.aV, false);
            }
         }
      }
   }

   static void handleHeldParticles(GoblinEntity queen) {
      if (queen.isQueen) {
         if (!queen.guards.isEmpty()) {
            boolean particlesSpawned = false;

            for (GoblinEntity goblin : queen.guards) {
               if ((Boolean)goblin.getDataManager().get(GoblinEntity.aC)) {
                  particlesSpawned = true;
               }
            }

            if (particlesSpawned) {
               queen.sendGirlChatMessage("Farewell my knight. You are welcome once I am breedable again.");

               for (GoblinEntity goblin : queen.guards) {
                  if (!(Boolean)goblin.getDataManager().get(GoblinEntity.aC)) {
                     goblin.setCurrentAction(Action.VANISH);
                  }
               }

               queen.guards.clear();
               queen.setInteractionPlayerUUID(null);
            }
         }
      }
   }

   /**
    * SERVER: ends the breeding — after 100 ticks the queen unbinds the
    * player, releases both guards anchored as AWAIT_PICK_UP (rewards) and
    * restores the player's physics.
    */
   static void handleThrowCooldown(GoblinEntity queen) {
      if (queen.isQueen) {
         if (queen.throwCooldown != -1) {
            if (++queen.throwCooldown >= 100) {
               queen.throwCooldown = -1;
               UUID uuid = queen.getInteractionPlayerUUID();
               if (uuid == null) {
                  queen.resetCameraAndPhysics();
               } else {
                  EntityPlayer player = queen.world.getPlayerEntityByUUID(uuid);
                  if (player == null) {
                     queen.resetCameraAndPhysics();
                  } else {
                     queen.setInteractionPlayerUUID(null);

                     for (GoblinEntity goblin : queen.guards) {
                        goblin.setInteractionPlayerUUID(null);
                     }

                     List positions = ensureGuardsSpawned(queen);
                     float yaw = queen.throneYaw + 180.0F;
                     Vec3d leftPos = queen.thronePos.add(GoblinThrow.rotateVectorYaw(GoblinEntity.aT, yaw));
                     Vec3d rightPos = queen.thronePos.add(GoblinThrow.rotateVectorYaw(GoblinEntity.ap, yaw));
                     Vec3d playerPos = queen.thronePos.add(GoblinThrow.rotateVectorYaw(GoblinEntity.as, yaw));
                     GoblinEntity leftGoblin = (GoblinEntity)positions.get(0);
                     GoblinEntity rightGoblin = (GoblinEntity)positions.get(1);
                     leftGoblin.setTargetPosition(leftPos);
                     rightGoblin.setTargetPosition(rightPos);
                     leftGoblin.setYawRotation(0.0F);
                     rightGoblin.setYawRotation(0.0F);
                     leftGoblin.setAnchored(true);
                     rightGoblin.setAnchored(true);
                     leftGoblin.setCurrentAction(Action.AWAIT_PICK_UP);
                     rightGoblin.setCurrentAction(Action.AWAIT_PICK_UP);
                     leftGoblin.setNoGravity(false);
                     rightGoblin.setNoGravity(false);
                     player.setNoGravity(false);
                     leftGoblin.noClip = false;
                     rightGoblin.noClip = false;
                     player.noClip = false;
                     player.rotationYaw = yaw;
                     player.rotationPitch = 30.0F;
                     player.setPositionAndUpdate(playerPos.x, playerPos.y, playerPos.z);
                     PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)player);
                     queen.sendGirlChatMessage(
                        "Thanks to you, my clan is soon going to get a few new members! In return I will bear of one of my guards to serve as your personal Onahole. Choose wisely~"
                     );
                  }
               }
            }
         }
      }
   }

   static void handleHoldCooldown(GoblinEntity queen) {
      if (queen.isQueen) {
         if (queen.holdCooldown != -1) {
            if (++queen.holdCooldown >= 205) {
               queen.holdCooldown = -1;
               UUID uuid = queen.getInteractionPlayerUUID();
               if (uuid != null) {
                  EntityPlayer player = queen.world.getPlayerEntityByUUID(uuid);
                  if (player != null) {
                     Vec3d pos = GoblinThrow.rotateVectorYaw(new Vec3d(0.0, 0.15625 - player.getEyeHeight(), -0.8859375), queen.throneYaw - 180.0F);
                     pos = pos.add(queen.getTargetPosition());
                     player.setPositionAndUpdate(pos.x, pos.y, pos.z);
                  }
               }
            }
         }
      }
   }

   /**
    * SERVER: throws the guards onto the breeding player — after 26 ticks in
    * {@link Action#JUMP_0} the queen repositions (per throne rotation) and
    * starts the breeding intro for herself and both guards; the 205-tick hold
    * cooldown ({@code holdCooldown}) then positions the player.
    */
   static void handleJumpThrow(GoblinEntity queen) {
      if (queen.isQueen) {
         if (queen.getCurrentAction() == Action.JUMP_0) {
            if (++queen.jumpThrowTicks >= 26) {
               queen.jumpThrowTicks = 0;
               Vec3d throwPos;
               switch ((int)queen.throneYaw) {
                  case -90:
                     throwPos = queen.thronePos.add(GoblinEntity.at);
                     break;
                  case 90:
                     throwPos = queen.thronePos.add(GoblinEntity.au);
                     break;
                  case 180:
                     throwPos = queen.thronePos.add(GoblinEntity.THROW_OFFSET_W);
                     break;
                  default:
                     throwPos = queen.thronePos.add(GoblinEntity.af);
               }

               UUID uuid = queen.getInteractionPlayerUUID();
               if (uuid != null) {
                  EntityPlayer player = queen.world.getPlayerEntityByUUID(uuid);
                  if (player != null) {
                     queen.setTargetPosition(throwPos);
                     queen.setYawRotation(queen.throneYaw);
                     queen.setCurrentAction(Action.BREEDING_INTRO_0);
                     queen.noClip = true;
                     queen.setNoGravity(true);
                     Vec3d pos = GoblinThrow.rotateVectorYaw(new Vec3d(0.0, 0.44375 - player.eyeHeight, -0.7875), queen.throneYaw - 180.0F);
                     player.noClip = true;
                     player.setNoGravity(true);
                     player.setPositionAndUpdate(
                        pos.x + throwPos.x, pos.y + throwPos.y, pos.z + throwPos.z
                     );
                     List goblins = ensureGuardsSpawned(queen);
                     if (goblins.size() >= 1) {
                        GoblinEntity goblin = (GoblinEntity)goblins.get(0);
                        goblin.setTargetPosition(throwPos);
                        goblin.setYawRotation(queen.throneYaw);
                        goblin.setCurrentAction(Action.BREEDING_INTRO_1);
                        goblin.noClip = true;
                        goblin.setNoGravity(true);
                     }

                     if (goblins.size() >= 2) {
                        GoblinEntity goblin2 = (GoblinEntity)goblins.get(1);
                        goblin2.setTargetPosition(throwPos);
                        goblin2.setYawRotation(queen.throneYaw);
                        goblin2.setCurrentAction(Action.BREEDING_INTRO_2);
                        goblin2.noClip = true;
                        goblin2.setNoGravity(true);
                     }

                     queen.holdCooldown = 0;
                  }
               }
            }
         }
      }
   }

   static AxisAlignedBB createThrowHitbox(Vec3d min, Vec3d max) {
      return new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
   }

   /**
    * SERVER: queen breeding — scans the throne zone in front of the queen
    * (per throne rotation) for a grounded player; when found (and not
    * pregnant) locks them in and starts the breeding intro
    * ({@link Action#JUMP_0}, guards get JUMP_1/JUMP_2).
    */
   static void tryStartBreeding(GoblinEntity queen) {
      if (queen.isQueen) {
         if (queen.getInteractionPlayerUUID() == null) {
            Vec3d basePos = null;
            switch ((int)queen.throneYaw) {
               case -90:
                  basePos = GoblinEntity.ao;
                  break;
               case 0:
                  basePos = GoblinEntity.aM;
                  break;
               case 90:
                  basePos = GoblinEntity.THROW_OFFSET_U;
                  break;
               case 180:
                  basePos = GoblinEntity.aB;
            }

            if (basePos != null) {
               Vec3d offset = queen.thronePos.subtract(0.5, 0.0, 0.5).subtract(basePos);
               AxisAlignedBB aabb = createThrowHitbox(offset, offset.add(GoblinEntity.ah.getX(), GoblinEntity.ah.getY(), GoblinEntity.ah.getZ()));
               List players = queen.world.getEntitiesWithinAABB(EntityPlayer.class, aabb);
               if (!players.isEmpty()) {
                  EntityPlayer player = (EntityPlayer)players.get(0);
                  if (player.onGround) {
                     if ((Boolean)queen.entityDataManager.get(GoblinEntity.aV)) {
                        if (queen.pregnantNoticeTick + 1200L < queen.world.getTotalWorldTime()) {
                           player.sendStatusMessage(new TextComponentString("The Queen is still pregnant - so no breeding for you uwu"), true);
                           queen.pregnantNoticeTick = queen.world.getTotalWorldTime();
                        }
                     } else {
                        UUID uuid = player.getPersistentID();
                        Vec3d pos = player.getPositionVector();
                        float yaw = player.rotationYaw + 180.0F;
                        PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
                        queen.setInteractionPlayerUUID(uuid);
                        queen.setCurrentAction(Action.JUMP_0);
                        queen.setTargetPosition(pos);
                        queen.setYawRotation(yaw);
                        queen.setAnchored(true);
                        List goblins = ensureGuardsSpawned(queen);
                        if (goblins.size() > 0) {
                           GoblinEntity goblin = (GoblinEntity)goblins.get(0);
                           goblin.setInteractionPlayerUUID(uuid);
                           goblin.setCurrentAction(Action.JUMP_1);
                           goblin.setTargetPosition(pos);
                           goblin.setYawRotation(yaw);
                           goblin.setAnchored(true);
                           if (goblins.size() > 1) {
                              GoblinEntity goblin2 = (GoblinEntity)goblins.get(1);
                              goblin2.setInteractionPlayerUUID(uuid);
                              goblin2.setCurrentAction(Action.JUMP_2);
                              goblin2.setTargetPosition(pos);
                              goblin2.setYawRotation(yaw);
                              goblin2.setAnchored(true);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * SERVER: returns the queen's two guard goblins, respawning them (same
    * model part, her girl id as queen key) when fewer than two exist.
    */
   static List<GoblinEntity> ensureGuardsSpawned(GoblinEntity queen) {
      if (queen.guards.size() > 1) {
         return queen.guards;
      }

      for (GoblinEntity goblin : queen.guards) {
         queen.world.removeEntity(goblin);
      }

      queen.guards.clear();
      GoblinEntity goblin = new GoblinEntity(queen.world, queen.getGirlId().toString(), queen.getModelPartIndex());
      goblin.setPosition(queen.posX, queen.posY, queen.posZ);
      queen.world.spawnEntity(goblin);
      queen.guards.add(goblin);
      GoblinEntity goblin2 = new GoblinEntity(queen.world, queen.getGirlId().toString(), queen.getModelPartIndex());
      goblin2.setPosition(queen.posX, queen.posY, queen.posZ);
      queen.world.spawnEntity(goblin2);
      queen.guards.add(goblin2);
      return queen.guards;
   }

   static void fleeFromNearestPlayer(GoblinEntity queen) {
      if (queen.onGround) {
         if (queen.getCurrentAction() == Action.RUN) {
            EntityPlayer player = queen.world.getClosestPlayerToEntity(queen, 100.0);
            if (player != null) {
               double distance = 20.0;

               while (!(distance <= 0.0)) {
                  Vec3d delta = queen.getPositionVector().subtract(player.getPositionVector());
                  Vec3d absDelta = new Vec3d(Math.abs(delta.x), Math.abs(delta.y), Math.abs(delta.z));
                  double xWeight = absDelta.x / (absDelta.x + absDelta.z);
                  double zWeight = absDelta.z / (absDelta.x + absDelta.z);
                  Vec3d targetPos = queen.getPositionVector()
                     .add(new Vec3d((delta.x > 0.0 ? 1 : -1) * xWeight * distance, 0.0, (delta.z > 0.0 ? 1 : -1) * zWeight * distance));
                  PathNavigate navigator = queen.getNavigator();
                  navigator.clearPath();
                  boolean moved = navigator.tryMoveToXYZ(targetPos.x, targetPos.y, targetPos.z, 0.825F);
                  distance--;
                  if (moved) {
                     return;
                  }
               }
            }
         }
      }
   }

   /**
    * SERVER: the queen's gold-steal cycle — every 32000 ticks while sitting
    * and unowned she spawns a RUN goblin that grabs a random gold item from
    * the nearest grounded player's inventory and brings it home.
    */
   static void tickGoldSteal(GoblinEntity queen) {
      if (queen.isQueen) {
         if (!(Boolean)queen.entityDataManager.get(GoblinEntity.aC)) {
            if (!(Boolean)queen.entityDataManager.get(GoblinEntity.aV)) {
               if (queen.getCurrentAction() == Action.SIT) {
                  if (++queen.robTicks >= 32000) {
                     EntityPlayer player = queen.world.getClosestPlayerToEntity(queen, 3000.0);
                     if (player != null) {
                        if (player.onGround) {
                           if (!player.isAirBorne) {
                              Integer slotIndex = findThrowTarget(queen, player);
                              if (slotIndex != null) {
                                 Vec3d playerPos = player.getPositionVector();
                                 Vec3d selfPos = queen.getPositionVector();
                                 Vec3d delta = playerPos.subtract(selfPos);
                                 double dist = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
                                 if (!(dist > 100.0)) {
                                    ItemStack stolenStack = player.inventory.getStackInSlot(slotIndex).copy();
                                    GoblinEntity goblin = new GoblinEntity(queen.world, queen.getGirlId().toString(), queen.getModelPartIndex());
                                    Vec3d offset = GoblinThrow.rotateVectorYaw(new Vec3d(0.0, 0.0, -0.2F), player.rotationYawHead);
                                    goblin.setPosition(player.posX + offset.x, player.posY, player.posZ + offset.z);
                                    goblin.setCurrentAction(Action.RUN);
                                    queen.world.spawnEntity(goblin);
                                    goblin.entityDataManager.set(GoblinEntity.a0, stolenStack);
                                    player.sendMessage(
                                       new TextComponentString(String.format("<%s> I got your %s hehe~", goblin.getDisplayNameText(), stolenStack.getDisplayName()))
                                    );
                                    player.inventory.removeStackFromSlot(slotIndex);
                                    queen.robTicks = 0;
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
   }

   @Nullable
   static Integer findThrowTarget(GoblinEntity queen, EntityPlayer player) {
      NonNullList inventory = player.inventory.mainInventory;
      ArrayList validSlots = new ArrayList();

      for (int i = 0; i < inventory.size(); i++) {
         ItemStack stack = (ItemStack)inventory.get(i);
         if (stack != ItemStack.EMPTY && GoblinEntity.GOLD_ITEMS.contains(stack.getItem())) {
            validSlots.add(i);
         }
      }

      return validSlots.isEmpty() ? null : (Integer)validSlots.get(queen.getRNG().nextInt(validSlots.size()));
   }

   static void handleThrowState(GoblinEntity queen) {
      if (queen.isQueen) {
         if (queen.getInteractionPlayerUUID() == null) {
            queen.setTargetPosition(queen.thronePos);
            queen.setYawRotation(queen.throneYaw);
            queen.setAnchored(true);
            queen.setNoGravity(true);
            queen.setCurrentAction(Action.SIT);
         }
      }
   }
}
