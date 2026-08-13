package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.networking.ClaimTribePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;







import java.io.IOException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class TribeNameScreen extends GuiScreen {
   static final int b = 15;
   static final int a = 100;
   static final int c = 20;
   UUID e;
   GuiTextField d;

   public TribeNameScreen(UUID var1) {
      this.e = var1;
   }

   public boolean func_73868_f() {
      return false;
   }

   public void func_73866_w_() {
      super.func_73866_w_();
      this.d = new GuiTextField(0, this.field_146297_k.field_71466_p, this.field_146294_l / 2 - 50, this.field_146295_m / 2 - 10, 100, 20);
      this.d.func_146195_b(true);
      this.field_146292_n.add(new GuiButton(0, this.field_146294_l / 2 - 25, this.field_146295_m / 2 + 20, 50, 20, "set"));
   }

   public void func_73876_c() {
      this.d.func_146178_a();
      super.func_73876_c();
   }

   public void func_73863_a(int var1, int var2, float var3) {
      this.func_146279_a("Name Tribe", this.field_146294_l / 2 - 39, this.field_146295_m / 2 - 10);
      this.d.func_146194_f();
      super.func_73863_a(var1, var2, var3);
   }

   protected void func_73869_a(char var1, int var2) {
      this.d.func_146201_a(var1, var2);
      String var3 = this.d.func_146179_b();
      if (var3.length() > 15) {
         this.d.func_146180_a(var3.substring(0, 15));
      }

      super.func_73869_a(var1, var2);
   }

   protected void func_146284_a(GuiButton var1) {
      super.func_146284_a(var1);
      String var2 = this.d.func_146179_b().trim();
      if (var2.length() != 0) {
         PacketHandler.b.sendToServer(new ClaimTribePacket(this.e, Minecraft.func_71410_x().field_71439_g.getPersistentID(), var2));
         Minecraft.func_71410_x().field_71439_g.func_71053_j();
      }
   }

}
