package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.GirlSavedData;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER request to ride the player's owned Galath
 * (controlled flight).
 * <p>
 * <b>Handler.</b> SERVER-side. Looks up the girl owned by the sender
 * ({@link GirlSavedData#getOwnerOf(EntityPlayer)}) and, if she exists, mounts the
 * player on her, switches her to {@link Action#CONTROLLED_FLIGHT}, sets the
 * interaction player and gives her an upward velocity.
 * <p>
 * <b>Pitfall — side effects.</b> The girl is removed from her chunk
 * ({@code world.getChunk(...).removeEntity(girl)}) so she is not ticked/render-
 * synced as a normal entity while being ridden. Do not remove that line: the
 * riding player is the authority for her position; keeping her in the chunk
 * causes a desync loop where the server snaps her back to the chunk position.
 * There is no reverse packet — dismounting restores her via the normal
 * entity/chunk re-add path.
 */
public class RequestRidingPacket implements IMessage {
   boolean isValid = false;

   public void fromBytes(ByteBuf buf) {
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
   }

   public static class Handler implements IMessageHandler<RequestRidingPacket, IMessage> {
      public IMessage onMessage(RequestRidingPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.SERVER)) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            UUID ownerUuid = GirlSavedData.getOwnerOf(player);
            BaseGirlEntity girl = BaseGirlEntity.getServerGirlEntity(ownerUuid);
            if (girl == null) {
               return null;
            }

            player.startRiding(girl, true);
            girl.setCurrentAction(Action.CONTROLLED_FLIGHT);
            girl.setInteractionPlayer(player);
            girl.motionY = 0.25;
            player.world.getChunk(girl.getPosition()).removeEntity(girl);
            return null;
         } else {
            System.out.println("received an invalid message @RequestRiding :(");
            return null;
         }
      }

   }
}
