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

   public CatThrowAwayItemPacket(UUID var1) {
      this.catUUID = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.catUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.catUUID.toString());
   }

   public static class Handler implements IMessageHandler<CatThrowAwayItemPacket, IMessage> {
      public IMessage onMessage(CatThrowAwayItemPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.catUUID)) {
                  if (!var3.world.isRemote && var3 instanceof LunaEntity) {
                     LunaEntity var4 = (LunaEntity)var3;
                     var4.dropHeldItem();
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
