package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.client.gui.GenderSwapScreen;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> Sex-prompt routing between a girl's owner (female) and the male
 * player for girl-girl pair scenes. The action name travels with the prompt so
 * the recipient's {@link GenderSwapScreen} can render the accept/decline
 * buttons.
 * <p>
 * CLIENT->SERVER: either player's answer to a prompt. SERVER-side handler
 * (scheduled on the main thread) validates that both players exist and forwards
 * the prompt to the *other* participant — {@code accepted=true} is sent to the
 * female (owner), {@code accepted=false} to the male.
 * <p>
 * CLIENT-side handler: feeds the button click into
 * {@link GenderSwapScreen#onButtonClicked}.
 */
public class SexPromptPacket implements IMessage {
   boolean isValid = false;
   String actionName;
   UUID playerUUID;
   UUID girlUUID;
   boolean accepted;

   public SexPromptPacket() {
   }

   public SexPromptPacket(String actionName, UUID playerUUID, UUID girlUUID, boolean accepted) {
      this.actionName = actionName;
      this.playerUUID = playerUUID;
      this.girlUUID = girlUUID;
      this.accepted = accepted;
   }

   public void fromBytes(ByteBuf buf) {
      this.actionName = ByteBufUtils.readUTF8String(buf);
      this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.accepted = buf.readBoolean();
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.actionName);
      ByteBufUtils.writeUTF8String(buf, this.playerUUID.toString());
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      buf.writeBoolean(this.accepted);
   }

   public static class Handler implements IMessageHandler<SexPromptPacket, IMessage> {
      public IMessage onMessage(SexPromptPacket packet, MessageContext ctx) {
         if (!packet.isValid) {
            System.out.println("received an invalid message @SexPrompt :(");
            return null;
         } else if (ctx.side.equals(Side.CLIENT)) {
            GenderSwapScreen.instance.onButtonClicked(new GenderSwapScreen.SwapButton(packet.actionName, packet.playerUUID, packet.girlUUID, packet.accepted));
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               World world = ctx.getServerHandler().player.world;
               EntityPlayer girlPlayer = world.getPlayerEntityByUUID(packet.girlUUID);
               EntityPlayer player = world.getPlayerEntityByUUID(packet.playerUUID);
               if (girlPlayer == null) {
                  System.out.println("Sex prompt invalid -> female player not found");
               } else if (player == null) {
                  System.out.println("Sex prompt invalid -> male player not found");
               } else {
                  PacketHandler.networkWrapper.sendTo(new SexPromptPacket(packet.actionName, packet.playerUUID, packet.girlUUID, packet.accepted), (EntityPlayerMP)(packet.accepted ? girlPlayer : player));
               }
            });
            return null;
         }
      }

   }
}
