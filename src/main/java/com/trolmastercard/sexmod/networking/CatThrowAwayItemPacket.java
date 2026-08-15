package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER request to make the cat-girl Luna drop whatever she
 * is holding (e.g. a caught fish she is chewing on).
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Calls
 * {@link LunaEntity#dropHeldItem()} on the matching {@link LunaEntity} — the
 * held item becomes a world {@code EntityItem} on the server.
 */
public class CatThrowAwayItemPacket implements IMessage {
   boolean isValid = false;
   UUID catUUID;

   public CatThrowAwayItemPacket() {
   }

   public CatThrowAwayItemPacket(UUID catUUID) {
      this.catUUID = catUUID;
   }

   public void fromBytes(ByteBuf buf) {
      this.catUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.catUUID.toString());
   }

   public static class Handler implements IMessageHandler<CatThrowAwayItemPacket, IMessage> {
      public IMessage onMessage(CatThrowAwayItemPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity girl : BaseGirlEntity.girlList(packet.catUUID)) {
                  if (!girl.world.isRemote && girl instanceof LunaEntity) {
                     LunaEntity luna = (LunaEntity)girl;
                     luna.dropHeldItem();
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @CatThrowAwayItem :(");
            return null;
         }
      }

   }
}
