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
 * <b>Role.</b> CLIENT->SERVER "eat/reel in" tick for Luna's fishing minigame. Sent
 * by the client while Luna is eating a caught fish, advancing her bobber state.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Calls
 * {@link LunaEntity#onFishingTick()} on the Luna entity matching the UUID so the
 * fishing catch timer / reel-in logic runs on the server. Must only be sent while
 * her {@code SexEntity} bobber is active; a stray packet with no matching girl is
 * ignored.
 */
public class CatEatingDonePacket implements IMessage {
   boolean isValid = false;
   UUID catUUID;

   public CatEatingDonePacket() {
   }

   public CatEatingDonePacket(UUID var1) {
      this.catUUID = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.catUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.catUUID.toString());
   }

   public static class Handler implements IMessageHandler<CatEatingDonePacket, IMessage> {
      public IMessage onMessage(CatEatingDonePacket var1, MessageContext var2) {
         if (var1.isValid && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.catUUID)) {
                  if (!var3.world.isRemote && var3 instanceof LunaEntity) {
                     LunaEntity var4 = (LunaEntity)var3;
                     var4.onFishingTick();
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @CatEatingDone :(");
            return null;
         }
      }

   }
}
