package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.item.KoboldEggItem;
import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER "give me a tribe egg" request from the dragon-staff
 * UI. Gives the player a {@link KoboldEggItem} dyed in the tribe color and tagged
 * with the tribe UUID, so hatching it produces a kobold of *their* tribe.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. No-op if the sender
 * has no tribe. The egg carries {@code tribeID} NBT which
 * {@link KoboldEggItem} reads when placing the egg entity.
 */
public class SendEggPacket implements IMessage {
   boolean isValid;

   public void fromBytes(ByteBuf buf) {
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
   }

   public static class Handler implements IMessageHandler<SendEggPacket, IMessage> {
      public IMessage onMessage(SendEggPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               EntityPlayerMP player = ctx.getServerHandler().player;
               UUID tribeUuid = KoboldManager.getTribeUUID(player.getPersistentID());
               if (tribeUuid != null) {
                  EyeAndKoboldColor color = KoboldManager.getTribeColor(tribeUuid);
                  ItemStack stack = new ItemStack(KoboldEggItem.KOBOLD_EGG_ITEM, 1, color.getWoolMeta());
                  NBTTagCompound tag = stack.getTagCompound();
                  if (tag == null) {
                     tag = new NBTTagCompound();
                  }

                  tag.setString("tribeID", tribeUuid.toString());
                  stack.setTagCompound(tag);
                  player.inventory.addItemStackToInventory(stack);
               }
            });
            return null;
         } else {
            System.out.println("received an invalid Message @SendEgg :(");
            return null;
         }
      }

   }
}
