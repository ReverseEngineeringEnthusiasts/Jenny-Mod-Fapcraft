package com.trolmastercard.sexmod.network.scene;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.common.entity.base.BaseGirlEntity;
import com.trolmastercard.sexmod.api.IBeddableSexGirl;
import com.trolmastercard.sexmod.common.util.SceneDebugFlags;
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
 * choice (Luna): after the girl's {@code startActionFromHandState()} picks the hand-state, the client
 * sends this packet, and the SERVER handler calls
 * {@link IBeddableSexGirl#goToSexBed()} which finds a bed, sets
 * {@code TARGET_POS}/{@code YAW_ROTATION}, and starts the walk; on arrival the
 * girl anchors and enters {@code WAIT_CAT}/{@code ANAL_WAIT} to pick up the
 * scene.
 * <p>
 * <b>Pitfall:</b> Luna's {@code startActionFromHandState()} "sex" case sends this packet together
 * with a full {@link ResetGirlPacket} (jar-faithful) — the reset clears the
 * interaction partner before the walk, and the girl re-binds it on arrival.
 * Do not reorder.
 */
public class SendGirlToBedPacket implements IMessage {
   boolean isValid;
   UUID girlUUID;

   public SendGirlToBedPacket() {
      this.isValid = false;
   }

   public SendGirlToBedPacket(UUID girlUUID) {
      this.girlUUID = girlUUID;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
   }

   public static class Handler implements IMessageHandler<SendGirlToBedPacket, IMessage> {
      public IMessage onMessage(SendGirlToBedPacket packet, MessageContext ctx) {
         if (!packet.isValid) {
            Main.LOGGER.warn("received an invalid message @SendGirlToSex :(");
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity girl : BaseGirlEntity.girlList(packet.girlUUID)) {
                  if (!girl.world.isRemote && girl instanceof IBeddableSexGirl) {
                     SceneDebugFlags.log(SceneDebugFlags.PACKETS, "SendGirlToSexPacket: %s -> goToSexBed (action=%s anchored=%s)", girl.getDisplayNameText(), girl.getCurrentAction(), girl.isAnchored());
                     ((IBeddableSexGirl)girl).goToSexBed();
                  }
               }
            });
            return null;
         }
      }

   }
}
