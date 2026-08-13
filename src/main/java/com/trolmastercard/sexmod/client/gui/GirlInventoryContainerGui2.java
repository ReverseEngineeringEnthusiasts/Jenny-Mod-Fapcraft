package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UploadInventoryToServerPacket;







import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GirlInventoryContainerGui2 extends GuiContainer {
   static final ResourceLocation c = new ResourceLocation("sexmod", "textures/gui/girlinventory.png");
   UUID a;
   BaseGirlEntity b;
   UUID d;

   public GirlInventoryContainerGui2(BaseGirlEntity var1, InventoryPlayer var2, UUID var3) {
      super(new ChestContainer(var1, var2, var3));
      this.a = var3;
      this.b = var1;
      this.d = var2.field_70458_d.getPersistentID();
   }

   public void func_73863_a(int var1, int var2, float var3) {
      this.func_146276_q_();
      super.func_73863_a(var1, var2, var3);
      this.func_191948_b(var1, var2);
   }

   public void func_146281_b() {
      super.func_146281_b();

      for (ChestContainer var2 : ChestContainer.c) {
         if (var2.a.equals(this.a)) {
            ItemStack[] var3 = new ItemStack[42];
            Minecraft.func_71410_x().field_71439_g.field_71071_by.field_70462_a.toArray(var3);
            var3[36] = var2.func_75139_a(0).func_75211_c();
            var3[37] = var2.func_75139_a(1).func_75211_c();
            var3[38] = var2.func_75139_a(2).func_75211_c();
            var3[39] = var2.func_75139_a(3).func_75211_c();
            var3[40] = var2.func_75139_a(4).func_75211_c();
            var3[41] = var2.func_75139_a(5).func_75211_c();
            PacketHandler.b.sendToServer(new UploadInventoryToServerPacket(this.b.f_clash491(), this.d, var3));
         }
      }
   }

   protected void func_146976_a(float var1, int var2, int var3) {
      GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
      this.field_146297_k.field_71446_o.func_110577_a(c);
      this.func_73729_b(this.field_146294_l / 2 - 88, this.field_146295_m / 2 - 7 - 24, 33, 16, 176, 114);
   }
}
