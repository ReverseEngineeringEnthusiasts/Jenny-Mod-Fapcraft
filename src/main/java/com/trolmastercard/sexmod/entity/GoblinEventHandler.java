package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.gui.GalathScreen;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Forge event handler for {@link GoblinEntity} (registered from
 * {@code ForgeEventHandler}). Keeps a carried goblin consistent across
 * dimensions and clients: the client tick despawns local goblins whose owner
 * left the dimension, the dimension-change hook respawns the carried goblin in
 * the new world, attacks on an owned goblin are cancelled, and the G keybind
 * opens the throw/pickup pad for a goblin riding the local player's shoulder.
 */
public class GoblinEventHandler {
   static Minecraft mc = null;

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onClientTick(ClientTickEvent event) {
      if (event.phase != Phase.START) {
         ArrayList goblins = new ArrayList();

         try {
            for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
               if (girl.world.isRemote && girl instanceof GoblinEntity) {
                  GoblinEntity goblin = (GoblinEntity)girl;
                  UUID uuid = goblin.getOwnerUUID();
                  if (uuid != null) {
                     EntityPlayer player = goblin.world.getPlayerEntityByUUID(uuid);
                     if (player != null && player.dimension != goblin.dimension) {
                        goblins.add(goblin);
                     }
                  }
               }
            }
         } catch (ConcurrentModificationException ex) {
            // ignored: entity list mutated during iteration; retried on the next tick
         }

         for (GoblinEntity goblin : (java.util.Collection<GoblinEntity>) (goblins) ) {
            goblin.setOwnerUUID(null);
            goblin.setInteractionPlayerUUID(null);
            goblin.setDead();
         }
      }
   }

   @SubscribeEvent
   public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      EntityPlayer player = event.player;
      UUID uuid = player.getPersistentID();
      int dimension = event.toDim;
      World world = player.world;
      GoblinEntity existing = null;

      try {
         for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
            if (!girl.world.isRemote && girl instanceof GoblinEntity) {
               GoblinEntity goblin = (GoblinEntity)girl;
               if (uuid.equals(goblin.getOwnerUUID())) {
                  String modelCode = goblin.getCustomModelCode();
                  String partCode = goblin.getCustomPartListCode();
                  existing = goblin;
                  existing.setOwnerUUID(null);
                  existing.setInteractionPlayerUUID(null);
                  existing.setCurrentAction(Action.NULL);
                  GoblinEntity newGoblin = new GoblinEntity(world);
                  newGoblin.dimension = dimension;
                  newGoblin.forceSpawn = true;
                  newGoblin.setCustomModelCode(modelCode);
                  newGoblin.setCustomPartListCode(partCode);
                  newGoblin.entityDataManager.set(GoblinEntity.aC, true);
                  world.spawnEntity(newGoblin);
                  newGoblin.setPositionAndUpdate(player.posX, player.posY, player.posZ);
                  newGoblin.setOwnerUUID(uuid);
                  newGoblin.setCurrentAction(Action.SHOULDER_IDLE);
                  break;
               }
            }
         }
      } catch (ConcurrentModificationException ex) {
         // ignored: entity list mutated during iteration; retried on the next tick
      }

      if (existing != null) {
         world.removeEntity(existing);
         BaseGirlEntity.getGirlEntityList().remove(existing);
      }
   }

   @SubscribeEvent
   public void onLivingAttack(LivingAttackEvent event) {
      if (event.getSource() != DamageSource.OUT_OF_WORLD) {
         EntityLivingBase living = event.getEntityLiving();
         if (living instanceof GoblinEntity) {
            GoblinEntity goblin = (GoblinEntity)living;
            if (goblin.getOwnerUUID() != null) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent
   @SideOnly(Side.CLIENT)
   public void onKeyInput(KeyInputEvent event) {
      if (mc == null) {
         mc = Minecraft.getMinecraft();
      }

      if (!(mc.currentScreen instanceof GalathScreen)) {
         if (ClientProxy.keyBindings[0].isPressed()) {
            BaseGirlEntity interactingGirl = null;
            UUID uuid = Minecraft.getMinecraft().player.getPersistentID();

            try {
               for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
                  if (girl.world.isRemote && girl instanceof IGoblin) {
                     IGoblin goblin = (IGoblin)girl;
                     if (uuid.equals(goblin.getOwnerUUID())) {
                        interactingGirl = girl;
                        break;
                     }
                  }
               }
            } catch (ConcurrentModificationException ex) {
               // ignored: entity list mutated during iteration; retried on the next tick
            }

            if (interactingGirl != null) {
               if (interactingGirl.getCurrentAction() == Action.SHOULDER_IDLE) {
                  Minecraft.getMinecraft().displayGuiScreen(new GalathScreen(interactingGirl));
               }
            }
         }
      }
   }

}
