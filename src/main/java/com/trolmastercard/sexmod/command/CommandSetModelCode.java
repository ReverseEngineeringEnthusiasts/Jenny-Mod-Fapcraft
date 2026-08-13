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
   public static final CommandSetModelCode a = new CommandSetModelCode();

   public boolean allowUsageWithoutPrefix(ICommandSender var1, String var2) {
      return false;
   }

   public String func_71517_b() {
      return "setmodelcode";
   }

   public String func_71518_a(ICommandSender var1) {
      return "/setmodelcode";
   }

   public boolean func_184882_a(MinecraftServer var1, ICommandSender var2) {
      return true;
   }

   public void func_184881_a(MinecraftServer var1, ICommandSender var2, String[] var3) {
      Minecraft var4 = Minecraft.func_71410_x();
      EntityPlayerSP var5 = var4.field_71439_g;
      String var6 = "";
      String var7 = "";
      if (var3.length > 0) {
         String[] var8 = var3[0].split("\\$");
         var6 = var8[0];
         if (var8.length > 1) {
            var7 = var8[1];
         }
      }

      RayTraceResult var10 = Minecraft.func_71410_x().field_71476_x;
      BaseGirlEntity var9 = this.a(var10);
      if (var9 == null) {
         var5.func_146105_b(new TextComponentString("You gotta transform into the girl you want to apply the model-code to"), true);
      } else if ("".equals(var7)) {
         PacketHandler.b.sendToServer(new UploadModelStringPacket(var6, var9.f_clash491()));
         var5.func_146105_b(new TextComponentString(this.a_clash756(var9)), true);
      } else {
         PacketHandler.b.sendToServer(new UploadModelStringPacket(var6, var9.f_clash491(), BaseGirlEntity.c_clash554(var7)));
         var5.func_146105_b(new TextComponentString(this.a_clash756(var9)), true);
      }
   }

   String a_clash756(BaseGirlEntity var1) {
      return var1 instanceof AbstractPlayerGirlEntity
         ? TextFormatting.YELLOW + "applied model code to your player-" + ThreadNames.b_clash163(NpcType.a_clash751(var1).toString())
         : TextFormatting.YELLOW + "applied model code to this " + var1.c_clash241();
   }

   @SideOnly(Side.CLIENT)
   BaseGirlEntity a(RayTraceResult var1) {
      if (var1 == null) {
         return AbstractPlayerGirlEntity.g(Minecraft.func_71410_x().field_71439_g);
      } else {
         return BaseGirlEntity.a_clash542(var1.field_72308_g)
            ? (BaseGirlEntity)var1.field_72308_g
            : AbstractPlayerGirlEntity.g(Minecraft.func_71410_x().field_71439_g);
      }
   }

   private static Exception a(Exception var0) {
      return var0;
   }
}
