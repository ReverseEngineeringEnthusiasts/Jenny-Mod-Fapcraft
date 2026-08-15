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

   public CatEatingDonePacket(UUID catUUID) {
      this.catUUID = catUUID;
   }

   public void fromBytes(ByteBuf buf) {
      this.catUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.catUUID.toString());
   }

   public static class Handler implements IMessageHandler<CatEatingDonePacket, IMessage> {
      public IMessage onMessage(CatEatingDonePacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity girl : BaseGirlEntity.girlList(packet.catUUID)) {
                  if (!girl.world.isRemote && girl instanceof LunaEntity) {
                     LunaEntity luna = (LunaEntity)girl;
                     luna.onFishingTick();
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
