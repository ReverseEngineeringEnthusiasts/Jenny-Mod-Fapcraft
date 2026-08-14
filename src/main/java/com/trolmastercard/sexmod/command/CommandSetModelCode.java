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

public class CommandSetModelCode extends CommandBase implements IClientCommand {
   public static final CommandSetModelCode SET_MODEL_CODE_COMMAND = new CommandSetModelCode();

   public boolean allowUsageWithoutPrefix(ICommandSender var1, String var2) {
      return false;
   }

   public String getName() {
      return "setmodelcode";
   }

   public String getUsage(ICommandSender var1) {
      return "/setmodelcode";
   }

   public boolean checkPermission(MinecraftServer var1, ICommandSender var2) {
      return true;
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) {
      Minecraft var4 = Minecraft.getMinecraft();
      EntityPlayerSP var5 = var4.player;
      String var6 = "";
      String var7 = "";
      if (var3.length > 0) {
         String[] var8 = var3[0].split("\\$");
         var6 = var8[0];
         if (var8.length > 1) {
            var7 = var8[1];
         }
      }

      RayTraceResult var10 = Minecraft.getMinecraft().objectMouseOver;
      BaseGirlEntity var9 = this.getTargetGirl(var10);
      if (var9 == null) {
         var5.sendStatusMessage(new TextComponentString("You gotta transform into the girl you want to apply the model-code to"), true);
      } else if ("".equals(var7)) {
         PacketHandler.networkWrapper.sendToServer(new UploadModelStringPacket(var6, var9.getGirlId()));
         var5.sendStatusMessage(new TextComponentString(this.getModelCodeText(var9)), true);
      } else {
         PacketHandler.networkWrapper.sendToServer(new UploadModelStringPacket(var6, var9.getGirlId(), BaseGirlEntity.decodePartIdList(var7)));
         var5.sendStatusMessage(new TextComponentString(this.getModelCodeText(var9)), true);
      }
   }

   String getModelCodeText(BaseGirlEntity var1) {
      return var1 instanceof AbstractPlayerGirlEntity
         ? TextFormatting.YELLOW + "applied model code to your player-" + ThreadNames.capitalizeFirst(NpcType.getNpcType(var1).toString())
         : TextFormatting.YELLOW + "applied model code to this " + var1.getDisplayNameText();
   }

   @SideOnly(Side.CLIENT)
   BaseGirlEntity getTargetGirl(RayTraceResult var1) {
      if (var1 == null) {
         return AbstractPlayerGirlEntity.getPlayerGirlByUUID(Minecraft.getMinecraft().player);
      } else {
         return BaseGirlEntity.isValidGirl(var1.entityHit)
            ? (BaseGirlEntity)var1.entityHit
            : AbstractPlayerGirlEntity.getPlayerGirlByUUID(Minecraft.getMinecraft().player);
      }
   }

}
