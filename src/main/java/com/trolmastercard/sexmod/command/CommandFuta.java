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

/**
 * <b>Role.</b> Client-side toggle for the Galath futa feature: {@code /futa
 * true|false} persists the flag to {@code sexmod/futa} (read back in the
 * constructor) and, when enabled, spawns dragon-breath particles at the
 * {@code cockParticles} bone of every local Galath. Registered by
 * {@link ClientProxy}.
 */
public class CommandFuta extends CommandBase implements IClientCommand {
   static final String CONFIG_FILE_PATH = "sexmod/futa";
   static final int PARTICLE_COUNT = 10;
   static final float PARTICLE_SPREAD = 0.025F;
   public static boolean ENABLED = true;
   public static final CommandFuta FUTA_COMMAND = new CommandFuta();

   public CommandFuta() {
      String line = "";

      try {
         line = new BufferedReader(new FileReader("sexmod/futa")).readLine().toLowerCase();
      } catch (Exception e) {
      }

      if (!"".equals(line)) {
         if ("true".equals(line)) {
            ENABLED = true;
         }

         if ("false".equals(line)) {
            ENABLED = false;
         }
      }
   }

   public String getName() {
      return "futa";
   }

   public String getUsage(ICommandSender sender) {
      return "/futa <true|false>";
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
      if (args.length < 1) {
         this.executeFuta(sender);
      } else {
         String arg = args[0].toLowerCase();
         if ("true".equals(arg)) {
            ENABLED = true;
         } else {
            if (!"false".equals(arg)) {
               this.executeFuta(sender);
               return;
            }

            ENABLED = false;
         }

         try {
            FileWriter writer = new FileWriter("sexmod/futa");
            writer.write(arg);
            writer.close();
         } catch (IOException ioe) {
            ioe.printStackTrace();
         }

         try {
            for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
               if (!girl.isDead && girl.world.isRemote && girl instanceof GalathEntity) {
                  Vec3d bonePos = girl.getCachedBoneOffset("cockParticles").add(girl.getPositionVector());
                  Random random = girl.getRNG();

                  for (int i = 0; i < 10; i++) {
                     girl.world
                        .spawnParticle(
                           EnumParticleTypes.DRAGON_BREATH,
                           bonePos.x,
                           bonePos.y,
                           bonePos.z,
                           random.nextFloat() * 0.025F * ThreadNames.randomSign(),
                           random.nextFloat() * 0.025F * ThreadNames.randomSign(),
                           random.nextFloat() * 0.025F * ThreadNames.randomSign(),
                           new int[0]
                        );
                  }
               }
            }
         } catch (ConcurrentModificationException cme) {
         }
      }
   }

   void executeFuta(ICommandSender sender) {
      sender.sendMessage(
         new TextComponentString(
            String.format(
               "%sYou can either do %s/futa true %sor %s/futa false", TextFormatting.YELLOW, TextFormatting.GRAY, TextFormatting.YELLOW, TextFormatting.GRAY
            )
         )
      );
   }

   public boolean allowUsageWithoutPrefix(ICommandSender sender, String args) {
      return false;
   }

}
