package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.client.gui.StructureCommandScreen;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import javax.vecmath.Vector4d;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>Role.</b> Tribe-overview request/response for the dragon-staff
 * {@link StructureCommandScreen}.
 * <p>
 * CLIENT->SERVER: sent when the staff UI opens ({@code isTribeLeader=false}).
 * The SERVER-side handler (scheduled on the main thread) collects the tribe's
 * alerted state, saved positions, member positions (one {@link Vector4d} per
 * member, w = wool color meta) and replies SERVER->CLIENT. If the sender has no
 * tribe it replies with an empty packet.
 * <p>
 * CLIENT-side handler: stores the reply in {@code KoboldEntity.aY} and the
 * alerted flag in {@code StructureCommandScreen.isErasing} — the UI reads those
 * statics to draw the tribe overview.
 */
public class GetTribeUiValuesPacket implements IMessage {
   boolean isTribeLeader = false;
   boolean isValid;
   List<Vector4d> tribeMembers;

   public GetTribeUiValuesPacket() {
      this.isValid = false;
      this.tribeMembers = new ArrayList<>();
   }

   public GetTribeUiValuesPacket(boolean isValid, List<Vector4d> tribeMembers) {
      this.isValid = isValid;
      this.tribeMembers = tribeMembers;
   }

   static GetTribeUiValuesPacket createEmptyPacket() {
      return new GetTribeUiValuesPacket(false, new ArrayList<>());
   }

   public void fromBytes(ByteBuf buf) {
      this.isValid = buf.readBoolean();
      int count = buf.readInt();

      for (int i = 0; i < count; i++) {
         this.tribeMembers.add(new Vector4d(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt()));
      }

      this.isTribeLeader = true;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.isValid);
      buf.writeInt(this.tribeMembers.size());

      for (Vector4d member : this.tribeMembers) {
         buf.writeInt((int)member.getX());
         buf.writeInt((int)member.getY());
         buf.writeInt((int)member.getZ());
         buf.writeInt((int)member.getW());
      }
   }

   public static class Handler implements IMessageHandler<GetTribeUiValuesPacket, IMessage> {
      public IMessage onMessage(GetTribeUiValuesPacket packet, MessageContext ctx) {
         if (!packet.isTribeLeader) {
            System.out.println("received an invalid message @GetTribeUIValues :(");
            return null;
         } else if (ctx.side.isClient()) {
            StructureCommandScreen.isErasing = packet.isValid;
            KoboldEntity.aY = packet.tribeMembers;
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               UUID tribeUuid = KoboldManager.getTribeUUID(ctx.getServerHandler().player.getPersistentID());
               if (tribeUuid == null) {
                  PacketHandler.networkWrapper.sendTo(GetTribeUiValuesPacket.createEmptyPacket(), ctx.getServerHandler().player);
               } else {
                  boolean isAlerted = KoboldManager.isTribeAlerted(tribeUuid);
                  EntityPlayerMP player = ctx.getServerHandler().player;
                  HashMap savedPositions = KoboldManager.getTribeSavedPositions(tribeUuid, player.world);
                  List membersList = KoboldManager.getTribeMembersList(tribeUuid);
                  ArrayList positions = new ArrayList();
                  int woolColor = KoboldManager.getTribeColor(tribeUuid).getWoolMeta();
                  HashSet seenIds = new HashSet();

                  for (KoboldEntity kobold : (java.util.Collection<KoboldEntity>) (membersList) ) {
                     if (!kobold.isDead) {
                        UUID girlId = kobold.getGirlId();
                        if (!seenIds.contains(girlId)) {
                           if (kobold.aA) {
                              woolColor = EyeAndKoboldColor.safeValueOf((String)kobold.getDataManager().get(AbstractNpcOnlyEntity.CURRENT_ACTION)).getWoolMeta();
                           }

                           positions.add(new Vector4d(kobold.posX, kobold.posY, kobold.posZ, woolColor));
                           seenIds.add(girlId);
                        }
                     }
                  }

                  for (Entry entry : (java.util.Set<Entry>) savedPositions.entrySet()) {
                     if (!seenIds.contains(entry.getKey())) {
                        BlockPos pos = (BlockPos)entry.getValue();
                        positions.add(new Vector4d(pos.getX(), pos.getY(), pos.getZ(), woolColor));
                     }
                  }

                  PacketHandler.networkWrapper.sendTo(new GetTribeUiValuesPacket(isAlerted, positions), player);
               }
            });
            return null;
         }
      }

   }
}
