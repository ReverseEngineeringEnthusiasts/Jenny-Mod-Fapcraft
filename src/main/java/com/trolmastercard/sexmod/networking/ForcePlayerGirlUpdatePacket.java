package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> SERVER->CLIENT forced sync of a transformed player-girl's
 * appearance. Sent by the server when the girl's outfit/action changed in a way
 * the data-manager sync alone would not cover (e.g. after re-login or a forced
 * model swap).
 * <p>
 * <b>Handler.</b> CLIENT-side, runs directly (no scheduling needed — it only
 * touches the entity's data manager). Looks up the {@link AbstractPlayerGirlEntity}
 * by UUID and writes {@code CUR_ACTION} and {@code OUTFIT_INDEX} into her data
 * manager, which drives her model/action on the render side.
 */
public class ForcePlayerGirlUpdatePacket implements IMessage {
   boolean isValid = false;
   UUID girlUUID;
   int modelVersion;
   Action action;

   public ForcePlayerGirlUpdatePacket() {
   }

   public ForcePlayerGirlUpdatePacket(UUID girlUUID, int modelVersion, Action action) {
      this.girlUUID = girlUUID;
      this.modelVersion = modelVersion;
      this.action = action;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.modelVersion = buf.readInt();
      this.action = Action.valueOf(ByteBufUtils.readUTF8String(buf));
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      buf.writeInt(this.modelVersion);
      ByteBufUtils.writeUTF8String(buf, this.action.toString());
   }

   public static class Handler implements IMessageHandler<ForcePlayerGirlUpdatePacket, IMessage> {
      public IMessage onMessage(ForcePlayerGirlUpdatePacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.CLIENT)) {
            AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(packet.girlUUID);
            if (playerGirl == null) {
               return null;
            }

            playerGirl.getDataManager().set(BaseGirlEntity.CUR_ACTION, packet.action.toString());
            playerGirl.getDataManager().set(BaseGirlEntity.OUTFIT_INDEX, packet.modelVersion);
            return null;
         } else {
            System.out.println("received an invalid message @ForcePlayerGirlUpdate :(");
            return null;
         }
      }

   }
}
