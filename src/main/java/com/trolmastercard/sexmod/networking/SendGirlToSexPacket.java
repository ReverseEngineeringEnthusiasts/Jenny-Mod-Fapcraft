package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.IBeddableSexGirl;
import com.trolmastercard.sexmod.util.SceneDebug;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * CLIENT -&gt; SERVER: asks a beddable girl ({@link IBeddableSexGirl}) to walk to
 * the nearest bed and begin her bed scene.
 * <p>
 * Used by the {@code anal}/{@code doggy} choices (Bia) and the {@code sex}
 * choice (Luna): after the girl's {@code U()} picks the hand-state, the client
 * sends this packet, and the SERVER handler calls
 * {@link IBeddableSexGirl#goToSexBed()} which finds a bed, sets
 * {@code TARGET_POS}/{@code YAW_ROTATION}, and starts the walk; on arrival the
 * girl anchors and enters {@code WAIT_CAT}/{@code ANAL_WAIT} to pick up the
 * scene.
 * <p>
 * <b>Pitfall:</b> Luna's {@code U()} "sex" case sends this packet together
 * with a full {@link ResetGirlPacket} (jar-faithful) — the reset clears the
 * interaction partner before the walk, and the girl re-binds it on arrival.
 * Do not reorder.
 */
public class SendGirlToSexPacket implements IMessage {
   boolean isValid;
   UUID girlUUID;

   public SendGirlToSexPacket() {
      this.isValid = false;
   }

   public SendGirlToSexPacket(UUID var1) {
      this.girlUUID = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
   }

   public static class Handler implements IMessageHandler<SendGirlToSexPacket, IMessage> {
      public IMessage onMessage(SendGirlToSexPacket var1, MessageContext var2) {
         if (!var1.isValid) {
            System.out.println("received an invalid message @SendGirlToSex :(");
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.girlUUID)) {
                  if (!var3.world.isRemote && var3 instanceof IBeddableSexGirl) {
                     SceneDebug.log(SceneDebug.PACKETS, "SendGirlToSexPacket: %s -> goToSexBed (action=%s anchored=%s)", var3.getDisplayNameText(), var3.getCurrentAction(), var3.isAnchored());
                     ((IBeddableSexGirl)var3).goToSexBed();
                  }
               }
            });
            return null;
         }
      }

   }
}
