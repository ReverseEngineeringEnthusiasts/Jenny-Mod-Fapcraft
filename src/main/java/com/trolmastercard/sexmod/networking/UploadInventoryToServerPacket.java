package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BeeEntityBase;
import com.trolmastercard.sexmod.entity.LunaEntity;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER inventory upload from the girl's chest GUI — writes
 * the 36 player-inventory slots plus the girl's armor slots back onto the server.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Slots 0-35 go into
 * the interacting player's inventory; slot 36+ into the girl's inventory (7 slots
 * for Luna, 6 for other {@link AbstractGirlNpcEntity}s, 27 for
 * {@link BeeEntityBase} — the target layout depends on the girl type).
 * <p>
 * <b>Pitfall.</b> The payload layout must match what the client GUI sent; a
 * length mismatch throws {@link ArrayIndexOutOfBoundsException} on the server.
 * The {@code girlUUID} resolves the girl, {@code playerUUID} the inventory owner.
 */
public class UploadInventoryToServerPacket implements IMessage {
   boolean isValid = false;
   ItemStack[] d;
   UUID girlUUID;
   UUID playerUUID;

   public UploadInventoryToServerPacket() {
   }

   public UploadInventoryToServerPacket(UUID girlUUID, UUID playerUUID, ItemStack[] inventoryItems) {
      this.girlUUID = girlUUID;
      this.d = inventoryItems;
      this.playerUUID = playerUUID;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      int count = buf.readInt();
      this.d = new ItemStack[count];

      for (int i = 0; i < count; i++) {
         this.d[i] = ByteBufUtils.readItemStack(buf);
      }

      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      ByteBufUtils.writeUTF8String(buf, this.playerUUID.toString());
      buf.writeInt(this.d.length);

      for (ItemStack stack : this.d) {
         ByteBufUtils.writeItemStack(buf, stack);
      }
   }

   public static class Handler implements IMessageHandler<UploadInventoryToServerPacket, IMessage> {
      public IMessage onMessage(UploadInventoryToServerPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity girl : BaseGirlEntity.girlList(packet.girlUUID)) {
                  if (!girl.world.isRemote) {
                     EntityPlayer player = girl.world.getPlayerEntityByUUID(packet.playerUUID);
                     if (player == null) {
                        return;
                     }

                     InventoryPlayer inventory = player.inventory;

                     // bounds-guard: a truncated/short payload must not index OOB
                     if (packet.d.length < 36) {
                        return;
                     }

                     for (int i = 0; i < 36; i++) {
                        inventory.setInventorySlotContents(i, packet.d[i]);
                     }

                     if (girl instanceof LunaEntity) {
                        AbstractGirlNpcEntity npcGirl = (AbstractGirlNpcEntity)girl;
                        npcGirl.inventory.setStackInSlot(0, packet.d[36]);
                        npcGirl.inventory.setStackInSlot(1, packet.d[37]);
                        npcGirl.inventory.setStackInSlot(2, packet.d[38]);
                        npcGirl.inventory.setStackInSlot(3, packet.d[39]);
                        npcGirl.inventory.setStackInSlot(4, packet.d[40]);
                        npcGirl.inventory.setStackInSlot(5, packet.d[41]);
                        npcGirl.inventory.setStackInSlot(6, packet.d[42]);
                     } else if (girl instanceof AbstractGirlNpcEntity) {
                        AbstractGirlNpcEntity npcGirl2 = (AbstractGirlNpcEntity)girl;
                        npcGirl2.inventory.setStackInSlot(0, packet.d[36]);
                        npcGirl2.inventory.setStackInSlot(1, packet.d[37]);
                        npcGirl2.inventory.setStackInSlot(2, packet.d[38]);
                        npcGirl2.inventory.setStackInSlot(3, packet.d[39]);
                        npcGirl2.inventory.setStackInSlot(4, packet.d[40]);
                        npcGirl2.inventory.setStackInSlot(5, packet.d[41]);
                     }

                     if (girl instanceof BeeEntityBase) {
                        BeeEntityBase beeGirl = (BeeEntityBase)girl;

                        for (int i2 = 0; i2 < 27; i2++) {
                           beeGirl.inventory.setStackInSlot(i2, packet.d[i2 + 36]);
                        }
                     }
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @UploadInventoryToServer :(");
            return null;
         }
      }

   }
}
