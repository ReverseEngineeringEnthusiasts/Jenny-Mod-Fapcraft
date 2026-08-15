package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER upload of a custom model code (+ optional part-id
 * list) for a girl, from the clothing editor / {@code /setmodelcode} command.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. For NPC girls the
 * code/part list is stored on the entity ({@code setCustomModelCode}/
 * {@code setCustomPartList}); for transformed player-girls it is persisted on
 * the player's NBT ({@code sexmod:CustomModel<type>} and, if the part list
 * passed validation, {@code sexmod:GirlSpecific<type>}).
 * <p>
 * <b>Validation.</b> {@link #isValidModelCode(BaseGirlEntity, List)} rejects a
 * part-id list unless every id is strictly smaller than the girl's current ids —
 * part ids encode nested "hide parent, show child" choices, so a non-decreasing
 * list would be a no-op or a rollback.
 */
public class UploadModelStringPacket implements IMessage {
   boolean isValid = false;
   String modelCode;
   List<Integer> partIds = new ArrayList<>();
   UUID girlUUID;

   public UploadModelStringPacket() {
   }

   public UploadModelStringPacket(String modelCode, UUID girlUUID) {
      this.modelCode = modelCode;
      this.girlUUID = girlUUID;
   }

   public UploadModelStringPacket(String modelCode, UUID girlUUID, List<Integer> partIds) {
      this.modelCode = modelCode;
      this.girlUUID = girlUUID;
      this.partIds = partIds;
   }

   public void fromBytes(ByteBuf buf) {
      this.modelCode = ByteBufUtils.readUTF8String(buf);
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      int count = buf.readInt();

      for (int i = 0; i < count; i++) {
         this.partIds.add(buf.readInt());
      }

      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.modelCode);
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      buf.writeInt(this.partIds.size());

      for (int partId : this.partIds) {
         buf.writeInt(partId);
      }
   }

   public static class Handler implements IMessageHandler<UploadModelStringPacket, IMessage> {
      public IMessage onMessage(UploadModelStringPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               BaseGirlEntity girl = BaseGirlEntity.getServerGirlEntity(packet.girlUUID);
               if (packet.partIds.size() > 0) {
                  boolean valid = this.isValidModelCode(girl, packet.partIds);
                  if (valid) {
                     girl.setCustomPartList(packet.partIds);
                  }

                  if (!(girl instanceof AbstractPlayerGirlEntity)) {
                     girl.setCustomModelCode(packet.modelCode);
                  } else {
                     EntityPlayerMP player = ctx.getServerHandler().player;
                     NBTTagCompound entityData = player.getEntityData();
                     AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player);
                     if (playerGirl != null) {
                        NpcType npcType = NpcType.getNpcType(playerGirl);
                        entityData.setString("sexmod:CustomModel" + npcType.toString(), packet.modelCode);
                        if (valid) {
                           entityData.setString("sexmod:GirlSpecific" + npcType.toString(), BaseGirlEntity.encodePartIdList(packet.partIds));
                        }
                     }
                  }
               } else if (!(girl instanceof AbstractPlayerGirlEntity)) {
                  girl.setCustomModelCode(packet.modelCode);
               } else {
                  EntityPlayerMP player2 = ctx.getServerHandler().player;
                  NBTTagCompound entityData2 = player2.getEntityData();
                  AbstractPlayerGirlEntity playerGirl2 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player2);
                  if (playerGirl2 != null) {
                     NpcType npcType2 = NpcType.getNpcType(playerGirl2);
                     entityData2.setString("sexmod:CustomModel" + npcType2.toString(), packet.modelCode);
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @UploadModelString :(");
            return null;
         }
      }

      /**
    * True if every part id in {@code partIds} is strictly smaller than the
    * corresponding current id of the girl. SERVER-side validation for the custom
    * part list; a mismatched list size is invalid.
    */
   boolean isValidModelCode(BaseGirlEntity girl, List<Integer> partIds) {
         ArrayList currentIds = girl.getCustomPartIdList();

         try {
            for (int i = 0; i < currentIds.size(); i++) {
               if ((Integer)currentIds.get(i) <= (Integer)partIds.get(i)) {
                  return false;
               }
            }

            return true;
         } catch (IndexOutOfBoundsException exception) {
            return false;
         }
      }

   }
}
