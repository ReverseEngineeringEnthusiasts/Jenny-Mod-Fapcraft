package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.AlliePlayerEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BeePlayerEntity;
import com.trolmastercard.sexmod.entity.SlimePlayerEntity;
import com.trolmastercard.sexmod.entity.Action;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> Event handlers around the player-girl transformation
 * ({@link AbstractPlayerGirlEntity}) and the player's interactions with other
 * player-girls:
 * <ul>
 * <li>bed placement / sleep restrictions while transformed (girl must be
 *     placed into her bed, not the player's)</li>
 * <li>bed-side placement of the player next to the girl's bed ({@code H_clash570}
 *     client-side, anchored target server-side)</li>
 * <li>respawn sync of the player-girl to the player's new position</li>
 * <li>interaction menu between player-girls (lesbo prompt), slime doggy start,
 *     fall-damage immunity (Allie/Bee/Slime forms)</li>
 * <li>dressup/strip button injected into the vanilla inventory GUI (id
 *     {@code 284453})</li>
 * </ul>
 * <p>
 * <b>Pitfalls.</b> {@link HandlePlayerMovement#setMovementLock(false)} is
 * released on both sides when the girl is bed-placed or strips — forgetting the
 * server-side unlock leaves the player unable to move. The GUI button id and
 * {@code eventCooldown} are the same constant; keep them equal.
 */
public class PlayerGirlEvents {
   static final int eventCooldown = 284453;

   @SubscribeEvent
   public void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
      EntityPlayer player = event.getEntityPlayer();
      AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player);
      if (playerGirl != null) {
         if (player.isSneaking()) {
            event.setResult(SleepResult.OTHER_PROBLEM);
         }
      }
   }

   @SubscribeEvent
   public void onGetCollisionBoxes(GetCollisionBoxesEvent event) {
   }

   @SubscribeEvent
   public void onRightClickBlockPlace(RightClickBlock event) {
      AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(event.getEntityPlayer().getPersistentID());
      BlockPos clickPos = event.getPos();
      World world = event.getEntityPlayer().world;
      EntityPlayer player = event.getEntityPlayer();
      if (playerGirl != null) {
         if (playerGirl.canBeInteracted()) {
            if (WorldUtils.canPlaceBlock(world, clickPos, event.getHitVec(), event.getFace(), player)) {
               if ((Boolean)playerGirl.getDataManager().get(BaseGirlEntity.IS_ANCHORED)) {
                  event.setCanceled(true);
               } else if (player.isSneaking()) {
                  ArrayList blocksAround = new ArrayList();
                  if (world.getBlockState(clickPos.north()).getBlock() == Blocks.AIR) {
                     blocksAround.add(clickPos.north());
                  }

                  if (world.getBlockState(clickPos.east()).getBlock() == Blocks.AIR) {
                     blocksAround.add(clickPos.east());
                  }

                  if (world.getBlockState(clickPos.south()).getBlock() == Blocks.AIR) {
                     blocksAround.add(clickPos.south());
                  }

                  if (world.getBlockState(clickPos.west()).getBlock() == Blocks.AIR) {
                     blocksAround.add(clickPos.west());
                  }

                  BlockPos nearestPos = null;

                  for (BlockPos candidatePos : (java.util.Collection<BlockPos>) (blocksAround) ) {
                     if (nearestPos == null) {
                        nearestPos = candidatePos;
                     } else {
                        Vec3d playerPos = player.getPositionVector();
                        double distA = this.interpolate(
                           candidatePos.getX(), candidatePos.getY(), candidatePos.getZ(), playerPos.x, playerPos.y, playerPos.z
                        );
                        double distB = this.interpolate(
                           nearestPos.getX(), nearestPos.getY(), nearestPos.getZ(), playerPos.x, playerPos.y, playerPos.z
                        );
                        if (distA < distB) {
                           nearestPos = candidatePos;
                        }
                     }
                  }

                  if (nearestPos == null) {
                     player.sendMessage(new TextComponentString("Bed is obscured"));
                  } else {
                     player.setPosition(nearestPos.getX() + 0.5, nearestPos.getY(), nearestPos.getZ() + 0.5);
                     if (clickPos.north().equals(nearestPos)) {
                        player.rotationYaw = 0.0F;
                     }

                     if (clickPos.east().equals(nearestPos)) {
                        player.rotationYaw = 90.0F;
                     }

                     if (clickPos.south().equals(nearestPos)) {
                        player.rotationYaw = 180.0F;
                     }

                     if (clickPos.west().equals(nearestPos)) {
                        player.rotationYaw = -90.0F;
                     }

                     if (event.getWorld().isRemote) {
                        HandlePlayerMovement.setMovementLock(false);
                        playerGirl.H_clash570();
                     } else {
                        playerGirl.setTargetPosition(new Vec3d(nearestPos.getX() + 0.5, nearestPos.getY() + 0.0F, nearestPos.getZ() + 0.5));
                        playerGirl.setYawRotation(player.rotationYaw);
                        playerGirl.getDataManager().set(BaseGirlEntity.IS_ANCHORED, true);
                        playerGirl.handleInteraction();
                     }
                  }
               }
            }
         }
      }
   }

   double interpolate(double x1, double y1, double z1, double x2, double y2, double z2) {
      double dx = x1 - x2;
      double dy = y1 - y2;
      double dz = z1 - z2;
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   @SubscribeEvent
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      EntityPlayer player = event.player;
      if (player != null) {
         AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByOwner(player.getPersistentID());
         if (playerGirl != null) {
            Vec3d respawnPos = player.getPositionVector();
            playerGirl.dimension = player.dimension;
            playerGirl.setPositionAndUpdate(respawnPos.x, respawnPos.y, respawnPos.z);
            playerGirl.updateAITasks();
            System.out.println(player.world.isAreaLoaded(playerGirl.getPosition(), 2));
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onEntityInteractOpenMenu(EntityInteract event) {
      if (event.getTarget() instanceof EntityPlayer) {
         if (!event.getEntityPlayer().isSneaking()) {
            if (event.getEntityPlayer().getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID())) {
               EntityPlayerSP clientPlayer = Minecraft.getMinecraft().player;
               AbstractPlayerGirlEntity clientPlayerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(clientPlayer.getPersistentID());
               EntityPlayer target = (EntityPlayer)event.getTarget();
               AbstractPlayerGirlEntity targetGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(target);
               if (targetGirl != null) {
                  if (clientPlayerGirl != null) {
                     clientPlayer.sendStatusMessage(new TextComponentString("no lesbo yet owo"), true);
                  } else if (targetGirl.isPlayerGirl()) {
                     if (targetGirl.canOpenInteractionMenu()) {
                        targetGirl.openInteractionMenu(Minecraft.getMinecraft().player);
                     }
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onEntityInteractLesbo(EntityInteract event) {
      if (event.getTarget() instanceof EntityPlayer) {
         if (event.getEntityPlayer().getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID())) {
            EntityPlayerSP clientPlayer = Minecraft.getMinecraft().player;
            AbstractPlayerGirlEntity clientPlayerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(clientPlayer.getPersistentID());
            if (clientPlayerGirl != null) {
               EntityPlayer target = (EntityPlayer)event.getTarget();
               AbstractPlayerGirlEntity targetGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(target.getPersistentID());
               if (targetGirl != null) {
                  target.sendStatusMessage(new TextComponentString("no lesbo yet owo"), true);
               } else {
                  if (clientPlayerGirl.canOpenInteractionMenu()) {
                     clientPlayerGirl.ab = false;
                     clientPlayerGirl.openInteractionMenu(target);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onRightClickBlockSlime(RightClickBlock event) {
      EntityPlayer player = event.getEntityPlayer();
      AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player);
      if (playerGirl != null) {
         if (playerGirl instanceof SlimePlayerEntity) {
            if (player.isSneaking()) {
               if (player.getHeldItemMainhand().equals(ItemStack.EMPTY)) {
                  if (!(Boolean)playerGirl.getDataManager().get(BaseGirlEntity.IS_ANCHORED)) {
                     if (!(player.rotationPitch < 20.0F)) {
                        Vec3d hitVec = event.getHitVec();
                        if (hitVec != null) {
                           Vec3d targetPos = new Vec3d(hitVec.x, Math.floor(hitVec.y) + 0.0, hitVec.z);
                           if (!(hitVec.distanceTo(player.getPositionVector()) > 3.0)) {
                              player.setPosition(targetPos.x, Math.floor(hitVec.y), targetPos.z);
                              playerGirl.setTargetPosition(targetPos);
                              playerGirl.setYawRotation(player.rotationYaw);
                              playerGirl.getDataManager().set(BaseGirlEntity.IS_ANCHORED, true);
                              playerGirl.getDataManager().set(BaseGirlEntity.OUTFIT_INDEX, 0);
                              playerGirl.setCurrentAction(Action.STARTDOGGY);
                              if (event.getWorld().isRemote && Minecraft.getMinecraft().player.getPersistentID().equals(player.getPersistentID())) {
                                 HandlePlayerMovement.setMovementLock(false);
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

   @SubscribeEvent
   public void onLivingHurt(LivingHurtEvent event) {
      if (event.getEntityLiving() instanceof EntityPlayer) {
         if (event.getSource() == DamageSource.FALL) {
            EntityPlayer entity = (EntityPlayer)event.getEntityLiving();
            AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(entity);
            if (playerGirl != null) {
               if (playerGirl instanceof AlliePlayerEntity || playerGirl instanceof BeePlayerEntity) {
                  event.setCanceled(true);
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onInitGui(InitGuiEvent event) {
      GuiScreen gui = event.getGui();
      if (gui instanceof GuiInventory || gui instanceof GuiContainerCreative) {
         EntityPlayerSP player = Minecraft.getMinecraft().player;
         if (player != null) {
            AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player);
            if (playerGirl != null) {
               if (!playerGirl.A_clash381()) {
                  List buttonList = event.getButtonList();
                  String label = I18n.format(playerGirl.getOutfitIndex() == 0 ? "action.names.dressup" : "action.names.strip", new Object[0]);
                  buttonList.add(new GuiButton(284453, (int)(gui.width * 0.5 - 35.0), (int)(gui.height * 0.87), 70, 20, label));
                  event.setButtonList(buttonList);
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onActionPerformed(ActionPerformedEvent event) {
      GuiScreen gui = event.getGui();
      if (gui instanceof GuiInventory || gui instanceof GuiContainerCreative) {
         if (event.getButton().id == 284453) {
            Minecraft minecraft = Minecraft.getMinecraft();
            AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(minecraft.player.getPersistentID());
            if (playerGirl != null) {
               if (!playerGirl.A_clash381()) {
                  if (playerGirl.getInteractionPlayerUUID() == null) {
                     if (playerGirl.getCurrentAction() == Action.NULL) {
                        minecraft.gameSettings.thirdPersonView = 2;
                        minecraft.entityRenderer.loadEntityShader(null);
                        playerGirl.setCurrentAction(Action.STRIP);
                        HandlePlayerMovement.setMovementLock(false);
                        minecraft.player.closeScreen();
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onLivingDamage(LivingDamageEvent event) {
      if (event.getSource() == DamageSource.FALL) {
         EntityLivingBase entity = event.getEntityLiving();
         if (entity instanceof EntityPlayer) {
            AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(entity.getPersistentID());
            if (playerGirl != null) {
               if (playerGirl instanceof SlimePlayerEntity) {
                  event.setResult(Result.DENY);
                  event.setAmount(0.0F);
                  event.setCanceled(true);
               }
            }
         }
      }
   }

}
