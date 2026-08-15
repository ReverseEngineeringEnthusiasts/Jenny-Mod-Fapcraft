package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.client.gui.GirlScreenBase;
import com.trolmastercard.sexmod.entity.NpcType;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> SERVER->CLIENT payload for the girl customization screen. Sent when
 * the player right-clicks a girl with the editor wand; carries the per-NPC-type
 * "girl-specific" custom model data (e.g. {@code sexmod:GirlSpecific<type>})
 * stored on the sender's player NBT.
 * <p>
 * <b>Handler.</b> CLIENT-side. Schedules
 * {@link #applyGirlData(HashMap)} on the client thread which opens a
 * {@link GirlScreenBase} populated with the received data.
 */
public class GirlDataPacket implements IMessage {
   boolean isValid = false;
   EntityPlayer player;
   HashMap<NpcType, String> a = new HashMap<>();

   public GirlDataPacket() {
   }

   public GirlDataPacket(EntityPlayer player) {
      this.player = player;
   }

   public void fromBytes(ByteBuf buf) {
      int count = buf.readInt();

      for (int i = 0; i < count; i++) {
         this.a.put(NpcType.valueOf(ByteBufUtils.readUTF8String(buf)), ByteBufUtils.readUTF8String(buf));
      }

      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      for (NpcType type : NpcType.values()) {
         if (type.hasSpecifics) {
            String value = this.player.getEntityData().getString("sexmod:GirlSpecific" + type);
            if (!"".equals(value)) {
               this.a.put(type, value);
            }
         }
      }

      buf.writeInt(this.a.size());

      for (Entry entry : this.a.entrySet()) {
         ByteBufUtils.writeUTF8String(buf, ((NpcType)entry.getKey()).toString());
         ByteBufUtils.writeUTF8String(buf, (String)entry.getValue());
      }
   }

   public static class Handler implements IMessageHandler<GirlDataPacket, IMessage> {
      public IMessage onMessage(GirlDataPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.CLIENT) {
            this.applyGirlData(packet.a);
            return null;
         } else {
            return null;
         }
      }

      @SideOnly(Side.CLIENT)
      public void applyGirlData(HashMap<NpcType, String> data) {
         Minecraft minecraft = Minecraft.getMinecraft();
         minecraft.addScheduledTask(() -> minecraft.displayGuiScreen(new GirlScreenBase(data)));
      }

   }
}
