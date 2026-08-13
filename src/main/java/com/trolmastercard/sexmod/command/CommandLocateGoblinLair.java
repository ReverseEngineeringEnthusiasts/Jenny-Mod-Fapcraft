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

   public String func_71517_b() {
      return "locatenearestgoblinlair";
   }

   public String func_71518_a(ICommandSender var1) {
      return "/locatenearestgoblinlair";
   }

   public void func_184881_a(MinecraftServer var1, ICommandSender var2, String[] var3) {
      Entity var4 = var2.func_174793_f();
      if (var4 != null && var4.field_71093_bK != 0) {
         var2.func_145747_a(
            new TextComponentString(
               TextFormatting.YELLOW
                  + "goblin lairs don't exist in the "
                  + (var4.field_71093_bK == -1 ? TextFormatting.RED + "Nether" : TextFormatting.DARK_PURPLE + "End")
            )
         );
      } else {
         GoblinEntity var5 = null;

         try {
            for (BaseGirlEntity var7 : BaseGirlEntity.ad_clash509()) {
               if (var7 instanceof GoblinEntity) {
                  GoblinEntity var8 = (GoblinEntity)var7;
                  if (var8.aX) {
                     if (var5 == null) {
                        var5 = var8;
                     } else if (var8.func_174818_b(var2.func_180425_c()) < var5.func_174818_b(var2.func_180425_c())) {
                        var5 = var8;
                     }
                  }
               }
            }
         } catch (ConcurrentModificationException var9) {
         }

         if (var5 == null) {
            var2.func_145747_a(new TextComponentString(TextFormatting.RED + "No nearby goblin lair found uwu"));
         } else {
            BlockPos var10 = var5.func_180425_c();
            var2.func_145747_a(
               new TextComponentString(
                  String.format(
                     "%sgoblin lair found at %s%s %s%s %s%s",
                     TextFormatting.YELLOW,
                     TextFormatting.RED,
                     var10.func_177958_n(),
                     TextFormatting.GREEN,
                     var10.func_177956_o(),
                     TextFormatting.BLUE,
                     var10.func_177952_p()
                  )
               )
            );
         }
      }
   }

   private static ConcurrentModificationException a(ConcurrentModificationException var0) {
      return var0;
   }
}
