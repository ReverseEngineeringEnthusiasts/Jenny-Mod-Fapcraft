package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.StartStandingSexAnimationPacket;







import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GenderSwapScreen {
   public static GenderSwapScreen a;
   private GenderSwapScreen.a b;

   public void a_clash861() {
      if (a.b != null) {
         if (--a.b.e <= 0.0F) {
            Minecraft.func_71410_x()
               .field_71439_g
               .func_145747_a(new TextComponentString(TextFormatting.DARK_PURPLE + I18n.func_135052_a("genderswap.sexpromt.timeout", new Object[0])));
            this.c_clash863();
         }
      }
   }

   public GenderSwapScreen.a b_clash862() {
      return a.b;
   }

   void c_clash863() {
      a.b = null;
   }

   public void a(@Nonnull GenderSwapScreen.a var1) {
      World var2 = Minecraft.func_71410_x().field_71439_g.field_70170_p;
      EntityPlayer var3 = var2.func_152378_a(var1.d);
      EntityPlayer var4 = var2.func_152378_a(var1.c);
      if (var4 != null && var3 != null) {
         TextComponentString var5 = new TextComponentString(
            TextFormatting.LIGHT_PURPLE
               + (var1.b ? var4.func_70005_c_() : var3.func_70005_c_())
               + " "
               + TextFormatting.DARK_PURPLE
               + I18n.func_135052_a("genderswap.sexpromt.playerxaskedfory", new Object[0])
               + " "
               + TextFormatting.LIGHT_PURPLE
               + I18n.func_135052_a(var1.a, new Object[0])
         );
         TextComponentString var6 = new TextComponentString(TextFormatting.DARK_PURPLE + I18n.func_135052_a("genderswap.sexpromt.autodeletion", new Object[0]));
         TextComponentString var7 = new TextComponentString(
            TextFormatting.DARK_PURPLE
               + "[ "
               + TextFormatting.LIGHT_PURPLE
               + I18n.func_135052_a("genderswap.sexpromt.accept", new Object[0])
               + TextFormatting.DARK_PURPLE
               + " | "
               + TextFormatting.LIGHT_PURPLE
               + I18n.func_135052_a("genderswap.sexpromt.decline", new Object[0])
               + TextFormatting.DARK_PURPLE
               + " ]"
         );
         var3.func_145747_a(var5);
         var3.func_145747_a(var6);
         var3.func_145747_a(var7);
         this.b = var1;
      }
   }

   @SubscribeEvent
   public void a(ClientChatEvent var1) {
      if (a.b_clash862() != null) {
         String var2 = var1.getMessage().toLowerCase();
         if (var2.equals(I18n.func_135052_a("genderswap.sexpromt.accept", new Object[0]).toLowerCase())) {
            GenderSwapScreen.a var3 = a.b_clash862();
            this.a(var3.a, var3.d, var3.c);
            this.c_clash863();
            var1.setCanceled(true);
         }

         if (var2.equals(I18n.func_135052_a("genderswap.sexpromt.decline", new Object[0]).toLowerCase())) {
            Minecraft.func_71410_x()
               .field_71439_g
               .func_145747_a(
                  new TextComponentString(TextFormatting.DARK_PURPLE + I18n.func_135052_a("genderswap.sexpromt.declineconformation", new Object[0]))
               );
            this.c_clash863();
            var1.setCanceled(true);
         }
      }
   }

   void a(String var1, UUID var2, UUID var3) {
      PacketHandler.b.sendToServer(new StartStandingSexAnimationPacket(var2, var3, var1));
   }


   public static class a {
      public String a;
      public UUID c;
      public UUID d;
      public float e;
      boolean b;

      public a(String var1, UUID var2, UUID var3, boolean var4) {
         this.a = var1;
         this.c = var2;
         this.d = var3;
         this.e = 1200.0F;
         this.b = var4;
      }
   }
}
