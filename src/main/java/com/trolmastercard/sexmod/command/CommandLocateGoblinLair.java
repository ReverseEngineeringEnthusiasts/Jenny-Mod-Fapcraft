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

/**
 * <b>Role.</b> {@code /locatenearestgoblinlair} — finds the closest goblin lair
 * (a {@link GoblinEntity} flagged {@code aX}) to the sender and prints its
 * coordinates; rejects the Nether/End outright. Registered by
 * {@link Main#onWorldStart}.
 */
public class CommandLocateGoblinLair extends CommandBase {
   public static final CommandLocateGoblinLair LOCATE_GOBLIN_LAIR_COMMAND = new CommandLocateGoblinLair();

   public String getName() {
      return "locatenearestgoblinlair";
   }

   public String getUsage(ICommandSender sender) {
      return "/locatenearestgoblinlair";
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
      Entity target = sender.getCommandSenderEntity();
      if (target != null && target.dimension != 0) {
         sender.sendMessage(
            new TextComponentString(
               TextFormatting.YELLOW
                  + "goblin lairs don't exist in the "
                  + (target.dimension == -1 ? TextFormatting.RED + "Nether" : TextFormatting.DARK_PURPLE + "End")
            )
         );
      } else {
         GoblinEntity lair = null;

         try {
            for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
               if (girl instanceof GoblinEntity) {
                  GoblinEntity goblin = (GoblinEntity)girl;
                  if (goblin.aX) {
                     if (lair == null) {
                        lair = goblin;
                     } else if (goblin.getDistanceSq(sender.getPosition()) < lair.getDistanceSq(sender.getPosition())) {
                        lair = goblin;
                     }
                  }
               }
            }
         } catch (ConcurrentModificationException e) {
         }

         if (lair == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "No nearby goblin lair found uwu"));
         } else {
            BlockPos pos = lair.getPosition();
            sender.sendMessage(
               new TextComponentString(
                  String.format(
                     "%sgoblin lair found at %s%s %s%s %s%s",
                     TextFormatting.YELLOW,
                     TextFormatting.RED,
                     pos.getX(),
                     TextFormatting.GREEN,
                     pos.getY(),
                     TextFormatting.BLUE,
                     pos.getZ()
                  )
               )
            );
         }
      }
   }

}
