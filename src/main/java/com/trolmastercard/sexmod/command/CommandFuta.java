package com.trolmastercard.sexmod.command;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.util.ThreadNames;







import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Random;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.IClientCommand;

public class CommandFuta extends CommandBase implements IClientCommand {
   static final String d = "sexmod/futa";
   static final int a = 10;
   static final float c = 0.025F;
   public static boolean e = true;
   public static final CommandFuta b = new CommandFuta();

   public CommandFuta() {
      String var1 = "";

      try {
         var1 = new BufferedReader(new FileReader("sexmod/futa")).readLine().toLowerCase();
      } catch (Exception var2) {
      }

      if (!"".equals(var1)) {
         if ("true".equals(var1)) {
            e = true;
         }

         if ("false".equals(var1)) {
            e = false;
         }
      }
   }

   public String func_71517_b() {
      return "futa";
   }

   public String func_71518_a(ICommandSender var1) {
      return "/futa <true|false>";
   }

   public void func_184881_a(MinecraftServer var1, ICommandSender var2, String[] var3) {
      if (var3.length < 1) {
         this.a(var2);
      } else {
         String var4 = var3[0].toLowerCase();
         if ("true".equals(var4)) {
            e = true;
         } else {
            if (!"false".equals(var4)) {
               this.a(var2);
               return;
            }

            e = false;
         }

         try {
            FileWriter var5 = new FileWriter("sexmod/futa");
            var5.write(var4);
            var5.close();
         } catch (IOException var10) {
            var10.printStackTrace();
         }

         try {
            for (BaseGirlEntity var6 : BaseGirlEntity.getGirlEntityList()) {
               if (!var6.field_70128_L && var6.field_70170_p.field_72995_K && var6 instanceof GalathEntity) {
                  Vec3d var7 = var6.getCachedBoneOffset("cockParticles").func_178787_e(var6.func_174791_d());
                  Random var8 = var6.func_70681_au();

                  for (int var9 = 0; var9 < 10; var9++) {
                     var6.field_70170_p
                        .func_175688_a(
                           EnumParticleTypes.DRAGON_BREATH,
                           var7.field_72450_a,
                           var7.field_72448_b,
                           var7.field_72449_c,
                           var8.nextFloat() * 0.025F * ThreadNames.a_clash166(),
                           var8.nextFloat() * 0.025F * ThreadNames.a_clash166(),
                           var8.nextFloat() * 0.025F * ThreadNames.a_clash166(),
                           new int[0]
                        );
                  }
               }
            }
         } catch (ConcurrentModificationException var11) {
         }
      }
   }

   void a(ICommandSender var1) {
      var1.func_145747_a(
         new TextComponentString(
            String.format(
               "%sYou can either do %s/futa true %sor %s/futa false", TextFormatting.YELLOW, TextFormatting.GRAY, TextFormatting.YELLOW, TextFormatting.GRAY
            )
         )
      );
   }

   public boolean allowUsageWithoutPrefix(ICommandSender var1, String var2) {
      return false;
   }

}
