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
   static final String CONFIG_FILE_PATH = "sexmod/futa";
   static final int PARTICLE_COUNT = 10;
   static final float PARTICLE_SPREAD = 0.025F;
   public static boolean ENABLED = true;
   public static final CommandFuta FUTA_COMMAND = new CommandFuta();

   public CommandFuta() {
      String var1 = "";

      try {
         var1 = new BufferedReader(new FileReader("sexmod/futa")).readLine().toLowerCase();
      } catch (Exception var2) {
      }

      if (!"".equals(var1)) {
         if ("true".equals(var1)) {
            ENABLED = true;
         }

         if ("false".equals(var1)) {
            ENABLED = false;
         }
      }
   }

   public String getName() {
      return "futa";
   }

   public String getUsage(ICommandSender var1) {
      return "/futa <true|false>";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) {
      if (var3.length < 1) {
         this.a(var2);
      } else {
         String var4 = var3[0].toLowerCase();
         if ("true".equals(var4)) {
            ENABLED = true;
         } else {
            if (!"false".equals(var4)) {
               this.a(var2);
               return;
            }

            ENABLED = false;
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
               if (!var6.isDead && var6.world.isRemote && var6 instanceof GalathEntity) {
                  Vec3d var7 = var6.getCachedBoneOffset("cockParticles").add(var6.getPositionVector());
                  Random var8 = var6.getRNG();

                  for (int var9 = 0; var9 < 10; var9++) {
                     var6.world
                        .spawnParticle(
                           EnumParticleTypes.DRAGON_BREATH,
                           var7.x,
                           var7.y,
                           var7.z,
                           var8.nextFloat() * 0.025F * ThreadNames.randomSign(),
                           var8.nextFloat() * 0.025F * ThreadNames.randomSign(),
                           var8.nextFloat() * 0.025F * ThreadNames.randomSign(),
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
      var1.sendMessage(
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
