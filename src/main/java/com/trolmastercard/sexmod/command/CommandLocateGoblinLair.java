package com.trolmastercard.sexmod.command;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GoblinEntity;







import java.util.ConcurrentModificationException;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandLocateGoblinLair extends CommandBase {
   public static final CommandLocateGoblinLair a = new CommandLocateGoblinLair();

   public String getName() {
      return "locatenearestgoblinlair";
   }

   public String getUsage(ICommandSender var1) {
      return "/locatenearestgoblinlair";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) {
      Entity var4 = var2.getCommandSenderEntity();
      if (var4 != null && var4.dimension != 0) {
         var2.sendMessage(
            new TextComponentString(
               TextFormatting.YELLOW
                  + "goblin lairs don't exist in the "
                  + (var4.dimension == -1 ? TextFormatting.RED + "Nether" : TextFormatting.DARK_PURPLE + "End")
            )
         );
      } else {
         GoblinEntity var5 = null;

         try {
            for (BaseGirlEntity var7 : BaseGirlEntity.getGirlEntityList()) {
               if (var7 instanceof GoblinEntity) {
                  GoblinEntity var8 = (GoblinEntity)var7;
                  if (var8.aX) {
                     if (var5 == null) {
                        var5 = var8;
                     } else if (var8.getDistanceSq(var2.getPosition()) < var5.getDistanceSq(var2.getPosition())) {
                        var5 = var8;
                     }
                  }
               }
            }
         } catch (ConcurrentModificationException var9) {
         }

         if (var5 == null) {
            var2.sendMessage(new TextComponentString(TextFormatting.RED + "No nearby goblin lair found uwu"));
         } else {
            BlockPos var10 = var5.getPosition();
            var2.sendMessage(
               new TextComponentString(
                  String.format(
                     "%sgoblin lair found at %s%s %s%s %s%s",
                     TextFormatting.YELLOW,
                     TextFormatting.RED,
                     var10.getX(),
                     TextFormatting.GREEN,
                     var10.getY(),
                     TextFormatting.BLUE,
                     var10.getZ()
                  )
               )
            );
         }
      }
   }

}
