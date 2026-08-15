package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.SlimeEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.SceneDebug;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * CLIENT -&gt; SERVER bridge that writes a single entity data-manager entry on
 * the server girl identified by {@link #girlUUID}.
 * <p>
 * This is how every client-side scene mutation reaches the server. Supported
 * parameter names:
 * <ul>
 *   <li>{@code "currentAction"} — sets {@link BaseGirlEntity#CUR_ACTION}
 *       (guarded: ATTACK only allowed from NULL).</li>
 *   <li>{@code "animationFollowUp"} — sets {@link BaseGirlEntity#GIRL_HAND_STATES},
 *       the scene-entry hand-state consumed by each girl's {@code U()}.</li>
 *   <li>{@code "playerSheHasSexWith"} — sets/clears
 *       {@link BaseGirlEntity#INTERACTION_PARTNER_UUID}.</li>
 *   <li>{@code "targetPos"} — sets {@link BaseGirlEntity#TARGET_POS}
 *       (value is {@code xfyfz}-formatted).</li>
 *   <li>{@code "shouldbeattargetpos"} — sets {@link BaseGirlEntity#IS_ANCHORED}.</li>
 *   <li>{@code "currentModel"} — sets {@link BaseGirlEntity#OUTFIT_INDEX}.</li>
 *   <li>{@code "master"}, {@code "walk speed"}, {@code "pregnant"} — master
 *       UUID, walk state, slime horny level respectively.</li>
 * </ul>
 * <p>
 * <b>Scene flow position:</b> the entry chain is
 * {@code doAction} (client) -&gt; this packet (animationFollowUp) -&gt;
 * {@link KoboldStatePacket} (dismount/position) -&gt; {@code updateAITasks}
 * lerp -&gt; anchored -&gt; {@code U()} reads GIRL_HAND_STATES and starts the
 * scene action.
 */
public class ChangeDataParameterPacket implements IMessage {
   boolean isValid;
   UUID girlUUID;
   String parameterName;
   String value;

   public ChangeDataParameterPacket() {
      this.isValid = false;
   }

   public ChangeDataParameterPacket(UUID girlUUID, String parameterName, String value) {
      this.girlUUID = girlUUID;
      this.parameterName = parameterName;
      this.value = value;
      this.isValid = true;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.parameterName = ByteBufUtils.readUTF8String(buf);
      this.value = ByteBufUtils.readUTF8String(buf);
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      ByteBufUtils.writeUTF8String(buf, this.parameterName);
      ByteBufUtils.writeUTF8String(buf, this.value == null ? "null" : this.value);
   }

   public static class Handler implements IMessageHandler<ChangeDataParameterPacket, IMessage> {
      public IMessage onMessage(ChangeDataParameterPacket packet, MessageContext ctx) {
         if (!packet.isValid) {
            System.out.println("received an invalid message @ChangeDataParameter :(");
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               BaseGirlEntity girl = BaseGirlEntity.getServerGirlEntity(packet.girlUUID);
               if (girl != null) {
                  SceneDebug.log(SceneDebug.PACKETS, "ChangeDataParameter: %s param=%s value=%s (remote=%s)", girl.getDisplayNameText(), packet.parameterName, packet.value, girl.world.isRemote);
                  switch (packet.parameterName) {
                     case "pregnant":
                        girl.getDataManager().set(SlimeEntity.HORNY_LEVEL, Integer.valueOf(packet.value));
                        break;
                     case "currentModel":
                        girl.getDataManager().set(BaseGirlEntity.OUTFIT_INDEX, Integer.valueOf(packet.value));
                        break;
                     case "currentAction":
                        if (Action.valueOf(packet.value) != Action.ATTACK || girl.getCurrentAction() == Action.NULL) {
                           girl.setCurrentAction(Action.valueOf(packet.value));
                        }
                        break;
                     case "animationFollowUp":
                        girl.getDataManager().set(BaseGirlEntity.GIRL_HAND_STATES, packet.value);
                        break;
                     case "playerSheHasSexWith":
                        if (packet.value.equals("null")) {
                           girl.setInteractionPlayerUUID(null);
                        } else {
                           girl.setInteractionPlayerUUID(UUID.fromString(packet.value));
                        }
                        break;
                     case "targetPos":
                        String[] parts = packet.value.split("f");
                        Vec3d targetPos = new Vec3d(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
                        girl.setTargetPosition(targetPos);
                        break;
                     case "master":
                        girl.getDataManager().set(BaseGirlEntity.MASTER, packet.value);
                        break;
                     case "walk speed":
                        girl.getDataManager().set(BaseGirlEntity.WALK_SPEED, packet.value);
                        break;
                     case "shouldbeattargetpos":
                        girl.getDataManager().set(BaseGirlEntity.IS_ANCHORED, Boolean.valueOf(packet.value));
                  }
               }
            });
            return null;
         }
      }

   }
}
