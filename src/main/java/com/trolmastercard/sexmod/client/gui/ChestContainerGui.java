package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UploadInventoryToServerPacket;







import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ChestContainerGui extends GuiContainer {
   private static final ResourceLocation f = new ResourceLocation("textures/gui/container/generic_54.png");
   private final IInventory e;
   private final IInventory d;
   private final int g;
   UUID c;
   BaseGirlEntity b;
   UUID a;

   public ChestContainerGui(EntityPlayer var1, BaseGirlEntity var2, UUID var3) {
      super(new GirlInventoryContainer(var1.field_71071_by, (IInventory)var2, var1, var3));
      this.c = var3;
      this.b = var2;
      this.a = var1.getPersistentID();
      this.e = var1.field_71071_by;
      this.d = (IInventory)var2;
      this.field_146291_p = false;
      this.g = ((IInventory)var2).func_70302_i_() / 9;
      this.field_147000_g = 114 + this.g * 18;
   }

   public void func_73863_a(int var1, int var2, float var3) {
      this.func_146276_q_();
      super.func_73863_a(var1, var2, var3);
      this.func_191948_b(var1, var2);
   }

   protected void func_146979_b(int var1, int var2) {
      this.field_146289_q.func_78276_b(this.b.c_clash241(), 8, 6, 4210752);
      this.field_146289_q.func_78276_b(this.e.func_145748_c_().func_150260_c(), 8, this.field_147000_g - 96 + 2, 4210752);
   }

   protected void func_146976_a(float var1, int var2, int var3) {
      GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
      this.field_146297_k.func_110434_K().func_110577_a(f);
      int var4 = (this.field_146294_l - this.field_146999_f) / 2;
      int var5 = (this.field_146295_m - this.field_147000_g) / 2;
      this.func_73729_b(var4, var5, 0, 0, this.field_146999_f, this.g * 18 + 17);
      this.func_73729_b(var4, var5 + this.g * 18 + 17, 0, 126, this.field_146999_f, 96);
   }

   public void func_146281_b() {
      super.func_146281_b();

      for (ChestContainer var2 : ChestContainer.c) {
         if (var2.a.equals(this.c)) {
            ItemStack[] var3 = new ItemStack[63];
            Minecraft.func_71410_x().field_71439_g.field_71071_by.field_70462_a.toArray(var3);

            for (int var4 = 0; var4 < 27; var4++) {
               var3[var4 + 36] = var2.func_75139_a(var4).func_75211_c();
            }

            PacketHandler.b.sendToServer(new UploadInventoryToServerPacket(this.b.f_clash491(), this.a, var3));
         }
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
