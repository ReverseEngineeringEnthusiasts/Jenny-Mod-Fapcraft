package com.trolmastercard.sexmod.command;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UploadModelStringPacket;
import com.trolmastercard.sexmod.util.ThreadNames;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.IClientCommand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> {@code /setmodelcode} — client-side upload of a custom model code
 * (and optional {@code $}-separated part-id list) for the targeted girl; without
 * a valid girl target it applies to the sender's own player-girl. The payload is
 * sent via {@link UploadModelStringPacket} to the server for validation and
 * persistence.
 */
public class CommandSetModelCode extends CommandBase implements IClientCommand {
   public static final CommandSetModelCode SET_MODEL_CODE_COMMAND = new CommandSetModelCode();

   public boolean allowUsageWithoutPrefix(ICommandSender sender, String args) {
      return false;
   }

   public String getName() {
      return "setmodelcode";
   }

   public String getUsage(ICommandSender sender) {
      return "/setmodelcode";
   }

   public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
      return true;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
      Minecraft mc = Minecraft.getMinecraft();
      EntityPlayerSP player = mc.player;
      String modelCode = "";
      String parts = "";
      if (args.length > 0) {
         String[] partsArr = args[0].split("\\$");
         modelCode = partsArr[0];
         if (partsArr.length > 1) {
            parts = partsArr[1];
         }
      }

      RayTraceResult rayTrace = Minecraft.getMinecraft().objectMouseOver;
      BaseGirlEntity target = this.getTargetGirl(rayTrace);
      if (target == null) {
         player.sendStatusMessage(new TextComponentString("You gotta transform into the girl you want to apply the model-code to"), true);
      } else if ("".equals(parts)) {
         PacketHandler.networkWrapper.sendToServer(new UploadModelStringPacket(modelCode, target.getGirlId()));
         player.sendStatusMessage(new TextComponentString(this.getModelCodeText(target)), true);
      } else {
         PacketHandler.networkWrapper.sendToServer(new UploadModelStringPacket(modelCode, target.getGirlId(), BaseGirlEntity.decodePartIdList(parts)));
         player.sendStatusMessage(new TextComponentString(this.getModelCodeText(target)), true);
      }
   }

   String getModelCodeText(BaseGirlEntity girl) {
      return girl instanceof AbstractPlayerGirlEntity
         ? TextFormatting.YELLOW + "applied model code to your player-" + ThreadNames.capitalizeFirst(NpcType.getNpcType(girl).toString())
         : TextFormatting.YELLOW + "applied model code to this " + girl.getDisplayNameText();
   }

   @SideOnly(Side.CLIENT)
   BaseGirlEntity getTargetGirl(RayTraceResult target) {
      if (target == null) {
         return AbstractPlayerGirlEntity.getPlayerGirlByUUID(Minecraft.getMinecraft().player);
      } else {
         return BaseGirlEntity.isValidGirl(target.entityHit)
            ? (BaseGirlEntity)target.entityHit
            : AbstractPlayerGirlEntity.getPlayerGirlByUUID(Minecraft.getMinecraft().player);
      }
   }

}
