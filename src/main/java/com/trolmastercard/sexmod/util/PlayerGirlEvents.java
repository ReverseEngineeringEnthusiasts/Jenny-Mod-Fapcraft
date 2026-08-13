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

public class PlayerGirlEvents {
   static final int eventCooldown = 284453;

   @SubscribeEvent
   public void a(PlayerSleepInBedEvent var1) {
      EntityPlayer var2 = var1.getEntityPlayer();
      AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.g(var2);
      if (var3 != null) {
         if (var2.isSneaking()) {
            var1.setResult(SleepResult.OTHER_PROBLEM);
         }
      }
   }

   @SubscribeEvent
   public void a(GetCollisionBoxesEvent var1) {
   }

   @SubscribeEvent
   public void a(RightClickBlock var1) {
      AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1.getEntityPlayer().getPersistentID());
      BlockPos var3 = var1.getPos();
      World var4 = var1.getEntityPlayer().world;
      EntityPlayer var5 = var1.getEntityPlayer();
      if (var2 != null) {
         if (var2.canBeInteracted()) {
            if (WorldUtils.a(var4, var3, var1.getHitVec(), var1.getFace(), var5)) {
               if ((Boolean)var2.getDataManager().get(BaseGirlEntity.IS_ANCHORED)) {
                  var1.setCanceled(true);
               } else if (var5.isSneaking()) {
                  ArrayList var6 = new ArrayList();
                  if (var4.getBlockState(var3.north()).getBlock() == Blocks.AIR) {
                     var6.add(var3.north());
                  }

                  if (var4.getBlockState(var3.east()).getBlock() == Blocks.AIR) {
                     var6.add(var3.east());
                  }

                  if (var4.getBlockState(var3.south()).getBlock() == Blocks.AIR) {
                     var6.add(var3.south());
                  }

                  if (var4.getBlockState(var3.west()).getBlock() == Blocks.AIR) {
                     var6.add(var3.west());
                  }

                  BlockPos var7 = null;

                  for (BlockPos var9 : (java.util.Collection<BlockPos>) (var6) ) {
                     if (var7 == null) {
                        var7 = var9;
                     } else {
                        Vec3d var10 = var5.getPositionVector();
                        double var11 = this.a(
                           var9.getX(), var9.getY(), var9.getZ(), var10.x, var10.y, var10.z
                        );
                        double var13 = this.a(
                           var7.getX(), var7.getY(), var7.getZ(), var10.x, var10.y, var10.z
                        );
                        if (var11 < var13) {
                           var7 = var9;
                        }
                     }
                  }

                  if (var7 == null) {
                     var5.sendMessage(new TextComponentString("Bed is obscured"));
                  } else {
                     var5.setPosition(var7.getX() + 0.5, var7.getY(), var7.getZ() + 0.5);
                     if (var3.north().equals(var7)) {
                        var5.rotationYaw = 0.0F;
                     }

                     if (var3.east().equals(var7)) {
                        var5.rotationYaw = 90.0F;
                     }

                     if (var3.south().equals(var7)) {
                        var5.rotationYaw = 180.0F;
                     }

                     if (var3.west().equals(var7)) {
                        var5.rotationYaw = -90.0F;
                     }

                     if (var1.getWorld().isRemote) {
                        HandlePlayerMovement.setMovementLock(false);
                        var2.H_clash570();
                     } else {
                        var2.setTargetPosition(new Vec3d(var7.getX() + 0.5, var7.getY() + 0.0F, var7.getZ() + 0.5));
                        var2.setYawRotation(var5.rotationYaw);
                        var2.getDataManager().set(BaseGirlEntity.IS_ANCHORED, true);
                        var2.handleInteraction();
                     }
                  }
               }
            }
         }
      }
   }

   double a(double var1, double var3, double var5, double var7, double var9, double var11) {
      double var13 = var1 - var7;
      double var15 = var3 - var9;
      double var17 = var5 - var11;
      return Math.sqrt(var13 * var13 + var15 * var15 + var17 * var17);
   }

   @SubscribeEvent
   public void a(PlayerRespawnEvent var1) {
      EntityPlayer var2 = var1.player;
      if (var2 != null) {
         AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByOwner(var2.getPersistentID());
         if (var3 != null) {
            Vec3d var4 = var2.getPositionVector();
            var3.dimension = var2.dimension;
            var3.setPositionAndUpdate(var4.x, var4.y, var4.z);
            var3.updateAITasks();
            System.out.println(var2.world.isAreaLoaded(var3.getPosition(), 2));
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void b(EntityInteract var1) {
      if (var1.getTarget() instanceof EntityPlayer) {
         if (!var1.getEntityPlayer().isSneaking()) {
            if (var1.getEntityPlayer().getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID())) {
               EntityPlayerSP var2 = Minecraft.getMinecraft().player;
               AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var2.getPersistentID());
               EntityPlayer var4 = (EntityPlayer)var1.getTarget();
               AbstractPlayerGirlEntity var5 = AbstractPlayerGirlEntity.g(var4);
               if (var5 != null) {
                  if (var3 != null) {
                     var2.sendStatusMessage(new TextComponentString("no lesbo yet owo"), true);
                  } else if (var5.isPlayerGirl()) {
                     if (var5.canOpenInteractionMenu()) {
                        var5.openInteractionMenu(Minecraft.getMinecraft().player);
                     }
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(EntityInteract var1) {
      if (var1.getTarget() instanceof EntityPlayer) {
         if (var1.getEntityPlayer().getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID())) {
            EntityPlayerSP var2 = Minecraft.getMinecraft().player;
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var2.getPersistentID());
            if (var3 != null) {
               EntityPlayer var4 = (EntityPlayer)var1.getTarget();
               AbstractPlayerGirlEntity var5 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var4.getPersistentID());
               if (var5 != null) {
                  var4.sendStatusMessage(new TextComponentString("no lesbo yet owo"), true);
               } else {
                  if (var3.canOpenInteractionMenu()) {
                     var3.ab = false;
                     var3.openInteractionMenu(var4);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void b(RightClickBlock var1) {
      EntityPlayer var2 = var1.getEntityPlayer();
      AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.g(var2);
      if (var3 != null) {
         if (var3 instanceof SlimePlayerEntity) {
            if (var2.isSneaking()) {
               if (var2.getHeldItemMainhand().equals(ItemStack.EMPTY)) {
                  if (!(Boolean)var3.getDataManager().get(BaseGirlEntity.IS_ANCHORED)) {
                     if (!(var2.rotationPitch < 20.0F)) {
                        Vec3d var4 = var1.getHitVec();
                        if (var4 != null) {
                           Vec3d var5 = new Vec3d(var4.x, Math.floor(var4.y) + 0.0, var4.z);
                           if (!(var4.distanceTo(var2.getPositionVector()) > 3.0)) {
                              var2.setPosition(var5.x, Math.floor(var4.y), var5.z);
                              var3.setTargetPosition(var5);
                              var3.setYawRotation(var2.rotationYaw);
                              var3.getDataManager().set(BaseGirlEntity.IS_ANCHORED, true);
                              var3.getDataManager().set(BaseGirlEntity.OUTFIT_INDEX, 0);
                              var3.setCurrentAction(Action.STARTDOGGY);
                              if (var1.getWorld().isRemote && Minecraft.getMinecraft().player.getPersistentID().equals(var2.getPersistentID())) {
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
   public void a(LivingHurtEvent var1) {
      if (var1.getEntityLiving() instanceof EntityPlayer) {
         if (var1.getSource() == DamageSource.FALL) {
            EntityPlayer var2 = (EntityPlayer)var1.getEntityLiving();
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.g(var2);
            if (var3 != null) {
               if (var3 instanceof AlliePlayerEntity || var3 instanceof BeePlayerEntity) {
                  var1.setCanceled(true);
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(InitGuiEvent var1) {
      GuiScreen var2 = var1.getGui();
      if (var2 instanceof GuiInventory || var2 instanceof GuiContainerCreative) {
         EntityPlayerSP var3 = Minecraft.getMinecraft().player;
         if (var3 != null) {
            AbstractPlayerGirlEntity var4 = AbstractPlayerGirlEntity.g(var3);
            if (var4 != null) {
               if (!var4.A_clash381()) {
                  List var5 = var1.getButtonList();
                  String var6 = I18n.format(var4.getOutfitIndex() == 0 ? "action.names.dressup" : "action.names.strip", new Object[0]);
                  var5.add(new GuiButton(284453, (int)(var2.width * 0.5 - 35.0), (int)(var2.height * 0.87), 70, 20, var6));
                  var1.setButtonList(var5);
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(ActionPerformedEvent var1) {
      GuiScreen var2 = var1.getGui();
      if (var2 instanceof GuiInventory || var2 instanceof GuiContainerCreative) {
         if (var1.getButton().id == 284453) {
            Minecraft var3 = Minecraft.getMinecraft();
            AbstractPlayerGirlEntity var4 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var3.player.getPersistentID());
            if (var4 != null) {
               if (!var4.A_clash381()) {
                  if (var4.getInteractionPlayerUUID() == null) {
                     if (var4.getCurrentAction() == Action.NULL) {
                        var3.gameSettings.thirdPersonView = 2;
                        var3.entityRenderer.loadEntityShader(null);
                        var4.setCurrentAction(Action.STRIP);
                        HandlePlayerMovement.setMovementLock(false);
                        var3.player.closeScreen();
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void a(LivingDamageEvent var1) {
      if (var1.getSource() == DamageSource.FALL) {
         EntityLivingBase var2 = var1.getEntityLiving();
         if (var2 instanceof EntityPlayer) {
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var2.getPersistentID());
            if (var3 != null) {
               if (var3 instanceof SlimePlayerEntity) {
                  var1.setResult(Result.DENY);
                  var1.setAmount(0.0F);
                  var1.setCanceled(true);
               }
            }
         }
      }
   }

}
