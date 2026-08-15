package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.item.LunaRodItem;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER request to make the cat-girl Luna cast her fishing
 * rod. Sent from the interaction menu ("go fishing") after the player hands her a
 * {@link LunaRodItem}.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Finds the
 * {@link LunaEntity} by UUID and calls
 * {@link LunaRodItem#castFishingRod(World, LunaEntity, EnumHand)} with the rod
 * she is holding — spawning the {@code SexEntity} bobber (SERVER side; the
 * entity spawn itself syncs to clients).
 */
public class CatActivateFishingPacket implements IMessage {
   boolean isValid = false;
   UUID catUUID;

   public CatActivateFishingPacket() {
   }

   public CatActivateFishingPacket(UUID catUUID) {
      this.catUUID = catUUID;
   }

   public void fromBytes(ByteBuf buf) {
      this.catUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.catUUID.toString());
   }

   public static class Handler implements IMessageHandler<CatActivateFishingPacket, IMessage> {
      public IMessage onMessage(CatActivateFishingPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity girl : BaseGirlEntity.girlList(packet.catUUID)) {
                  if (!girl.world.isRemote && girl instanceof LunaEntity) {
                     LunaEntity luna = (LunaEntity)girl;
                     ItemStack stack = luna.ao;
                     LunaRodItem rodItem = (LunaRodItem)stack.getItem();
                     rodItem.castFishingRod(ctx.getServerHandler().player.world, luna, EnumHand.MAIN_HAND);
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @CatActivateFishing :(");
            return null;
         }
      }

   }
}
