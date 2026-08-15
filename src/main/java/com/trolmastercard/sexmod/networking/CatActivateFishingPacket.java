package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.item.LunaRodItem;
import com.trolmastercard.sexmod.util.TrailSegment;
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

   public CatActivateFishingPacket(UUID var1) {
      this.catUUID = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.catUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.catUUID.toString());
   }

   public static class Handler implements IMessageHandler<CatActivateFishingPacket, IMessage> {
      public IMessage onMessage(CatActivateFishingPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity var4 : BaseGirlEntity.girlList(var1.catUUID)) {
                  if (!var4.world.isRemote && var4 instanceof LunaEntity) {
                     LunaEntity var5 = (LunaEntity)var4;
                     ItemStack var6 = var5.ao;
                     LunaRodItem var7 = (LunaRodItem)var6.getItem();
                     var7.castFishingRod(var2.getServerHandler().player.world, var5, EnumHand.MAIN_HAND);
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
