package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.NpcType;
import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Constructor;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER horny-potion transformation — toggles the sending
 * player between his vanilla self and a player-girl entity of the given
 * {@link NpcType}.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread.
 * <ul>
 * <li>{@code npcType == null} ("player"): revert — remove every entity matching
 *     the girl's id with {@code world.removeEntity}, clear the girl registry
 *     entry ({@code AbstractPlayerGirlEntity.al}) and un-set the owner id.</li>
 * <li>{@code npcType != null}: spawn a fresh player-girl of that type 69 blocks
 *     above the player (reflection constructor {@code (World, UUID)}), gravity
 *     off, no-clip, and call {@code B_clash233()} to finalize her state.</li>
 * </ul>
 * <p>
 * <b>Pitfall.</b> The revert branch removes entities ({@code world.removeEntity})
 * — the concurrent-modification guard around the girl-list iteration must stay.
 * The spawn height is intentionally far above the player; her AI drops her in
 * place.
 */
public class UpdatePlayerModelPacket implements IMessage {
   boolean isValid = false;
   NpcType npcType;

   public UpdatePlayerModelPacket() {
   }

   public UpdatePlayerModelPacket(NpcType npcType) {
      this.npcType = npcType;
   }

   public void fromBytes(ByteBuf buf) {
      String typeName = ByteBufUtils.readUTF8String(buf);
      if ("player".equals(typeName)) {
         this.npcType = null;
      } else {
         this.npcType = NpcType.valueOf(typeName);
      }

      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      if (this.npcType == null) {
         ByteBufUtils.writeUTF8String(buf, "player");
      } else {
         ByteBufUtils.writeUTF8String(buf, this.npcType.toString());
      }
   }

   public static class Handler implements IMessageHandler<UpdatePlayerModelPacket, IMessage> {
      public IMessage onMessage(UpdatePlayerModelPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               EntityPlayerMP player = ctx.getServerHandler().player;
               World world = player.world;
               UUID playerUuid = ctx.getServerHandler().player.getPersistentID();
               AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(playerUuid);
               if (playerGirl != null) {
                  try {
                     for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
                        if (!girl.world.isRemote && girl.getGirlId().equals(playerGirl.getGirlId())) {
                           world.removeEntity(girl);
                        }
                     }
                  } catch (ConcurrentModificationException exception) {
                  }

                  playerGirl.onTickClient();
                  AbstractPlayerGirlEntity.al.remove(playerUuid);
                  BaseGirlEntity.getGirlEntityList().remove(playerGirl);
                  playerGirl.setOwnerId(Optional.absent());
               }

               NpcType npcType = packet.npcType;
               if (npcType != null) {
                  AbstractPlayerGirlEntity newPlayerGirl;
                  try {
                     Constructor constructor = npcType.playerClass.getConstructor(World.class, UUID.class);
                     newPlayerGirl = (AbstractPlayerGirlEntity)constructor.newInstance(world, ctx.getServerHandler().player.getPersistentID());
                  } catch (Exception exception) {
                     exception.printStackTrace();
                     return;
                  }

                  newPlayerGirl.setNoGravity(true);
                  newPlayerGirl.noClip = true;
                  newPlayerGirl.motionX = 0.0;
                  newPlayerGirl.motionY = 0.0;
                  newPlayerGirl.motionZ = 0.0;
                  newPlayerGirl.setPosition(player.posX, player.posY + 69.0, player.posZ);
                  world.spawnEntity(newPlayerGirl);
                  newPlayerGirl.B_clash233();
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @UpdatePlayerModel :(");
            return null;
         }
      }

   }
}
