package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BiaPlayerEntity;
import com.trolmastercard.sexmod.entity.ElliePlayerEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.item.AlliesLampItem;
import com.trolmastercard.sexmod.networking.InformOfOwnershipPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SendBlocksPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

/**
 * <b>Role.</b> Login/logout handling for the horny-potion player-girl
 * transformation: on login the player is restored to vanilla state, his
 * player-girl (if any) is released from scenes, and the special fixed-UUID
 * players (Bia, Ellie) get their permanent player-girl re-registered; on logout
 * all girls he interacted with are reset so no scene is left half-open.
 * <p>
 * <b>Login flow (SERVER-side events):</b> invisible/gravity/clip flags cleared,
 * flight disabled outside creative; movement lock + ownership status sent to the
 * client ({@link SetPlayerMovementPacket}, {@link InformOfOwnershipPacket});
 * Allies lamp NBT re-keyed; tribe markers resent ({@link SendBlocksPacket});
 * player-girl table rebuilt, any existing girl un-anchored and fully reset via
 * {@link ResetGirlPacket.Handler#resetGirl}; fixed-UUID girls re-spawned
 * ({@link #registerBia}, {@link #registerEllie}).
 * <p>
 * <b>Logout flow:</b> every girl with the leaving player as interaction partner
 * is reset (single-arg full reset) and un-anchored; the girl's owner gets a
 * movement unlock. Concurrent-modification guards on the girl list must stay.
 */
public class PlayerIds {
   static final UUID playerId = UUID.fromString("b91e6484-8911-4def-ab04-9fa3452fca5f");
   static final UUID girlId = UUID.fromString("adf20149-2adc-4a9d-9af5-8e9aeda019d6");

   @SubscribeEvent
   public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      EntityPlayerMP player = event.player.world.getMinecraftServer().getPlayerList().getPlayerByUUID(event.player.getPersistentID());
      player.setInvisible(false);
      player.setNoGravity(false);
      player.noClip = false;
      if (!player.capabilities.isCreativeMode && player.capabilities.isFlying) {
         player.capabilities.isFlying = false;
      }

      PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), player);
      PacketHandler.networkWrapper.sendTo(new InformOfOwnershipPacket(GirlSavedData.hasOwner(player.getPersistentID())), player);

      for (ItemStack stack : player.inventory.mainInventory) {
         if (stack.getItem() == AlliesLampItem.ALLIES_LAMP && stack.hasTagCompound()) {
            stack.getTagCompound().setUniqueId("user", UUID.randomUUID());
         }
      }

      UUID tribeUuid = KoboldManager.getTribeUUID(player.getPersistentID());
      if (tribeUuid != null) {
         HashSet blocks = KoboldManager.getAllTribeBlocks(tribeUuid);
         PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(blocks, true), player);
      }

      AbstractPlayerGirlEntity.rebuildPlayerGirlTableFromWorld();
      AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(event.player.getPersistentID());
      World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
      this.registerJenny(world, player, playerGirl);
      if (playerGirl != null) {
         playerGirl.setAnchored(false);
         playerGirl.setCurrentAction(Action.NULL);
         ResetGirlPacket.Handler.resetGirl(playerGirl);
      }

      UUID playerUuid = event.player.getPersistentID();
      if (playerUuid.equals(playerId)) {
         this.registerBia(world, player, playerUuid);
      }

      if (playerUuid.equals(girlId)) {
         this.registerEllie(world, player, playerUuid);
      }

      GalathEntity.handlePlayerJoin(player);
   }

   void registerBia(World world, EntityPlayer player, UUID uuid) {
      BiaPlayerEntity bia = new BiaPlayerEntity(world, uuid);
      bia.setNoGravity(true);
      bia.noClip = true;
      bia.motionX = 0.0;
      bia.motionY = 0.0;
      bia.motionZ = 0.0;
      bia.setPosition(player.posX, player.posY + 69.0, player.posZ);
      world.spawnEntity(bia);
      bia.B_clash233();
   }

   void registerEllie(World world, EntityPlayer player, UUID uuid) {
      ElliePlayerEntity ellie = new ElliePlayerEntity(world, uuid);
      ellie.setNoGravity(true);
      ellie.noClip = true;
      ellie.motionX = 0.0;
      ellie.motionY = 0.0;
      ellie.motionZ = 0.0;
      ellie.setPosition(player.posX, player.posY + 69.0, player.posZ);
      world.spawnEntity(ellie);
      ellie.B_clash233();
   }

   void registerJenny(World world, EntityPlayer player, AbstractPlayerGirlEntity existingGirl) {
      Predicate predicate = girl -> true;

      for (AbstractPlayerGirlEntity playerGirl : world.getEntities(AbstractPlayerGirlEntity.class, predicate::test)) {
         if (playerGirl.getOwnerUserUUID().equals(player.getPersistentID()) && (existingGirl == null || playerGirl.getEntityId() != existingGirl.getEntityId())) {
            world.removeEntity(playerGirl);
         }
      }
   }

   @SubscribeEvent
   public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
      EntityPlayer player = event.player;

      try {
         for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
            if (girl instanceof AbstractPlayerGirlEntity) {
               ((AbstractPlayerGirlEntity)girl).onOwnerInteract(player);
            }

            if (girl.getInteractionPlayerUUID() != null) {
               if (girl.getInteractionPlayerUUID().equals(player.getPersistentID()) || girl.getInteractionPlayerUUID().equals(player.getUniqueID())) {
                  ResetGirlPacket.Handler.resetGirl(girl);
                  girl.setAnchored(false);
                  girl.setCurrentAction(Action.NULL);
               }

               if (girl instanceof AbstractPlayerGirlEntity
                  && ((AbstractPlayerGirlEntity)girl).getOwnerUserUUID().equals(player.getPersistentID())
                  && girl.getInteractionPlayerUUID() != null) {
                  EntityPlayerMP ridingPlayer = (EntityPlayerMP)event.player.world.getPlayerEntityByUUID(girl.getInteractionPlayerUUID());
                  PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), ridingPlayer);
                  ResetGirlPacket.Handler.resetGirls(ridingPlayer);
                  player.setInvisible(false);
                  girl.setInteractionPlayerUUID(null);
               }
            }
         }
      } catch (ConcurrentModificationException exception) {
      }
   }

}
