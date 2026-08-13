package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BeeEntityBase;
import com.trolmastercard.sexmod.networking.BeeOpenChestPacket;
import com.trolmastercard.sexmod.networking.ChangeDataParameterPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SetNewHomePacket;







import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;

public class BeeDialogueScreen extends GuiScreen {
   BeeEntityBase c;
   EntityPlayer a;
   boolean e;
   static final ResourceLocation b = new ResourceLocation("sexmod", "textures/gui/girlinventory.png");
   double d = 0.0;

   public BeeDialogueScreen(BeeEntityBase var1, EntityPlayer var2) {
      this.c = var1;
      this.a = var2;
      this.e = !"".equals(var1.func_184212_Q().func_187225_a(BaseGirlEntity.v));
   }

   public boolean func_73868_f() {
      return false;
   }

   public void func_73863_a(int var1, int var2, float var3) {
      super.func_73863_a(var1, var2, var3);
      this.field_146292_n.clear();
      ScaledResolution var4 = new ScaledResolution(this.field_146297_k);
      int var5 = var4.func_78326_a();
      this.d = Math.min(1.0, this.d + this.field_146297_k.func_193989_ak() / 5.0F);
      this.field_146292_n
         .add(
            new GuiButton(
               0,
               var5 / 2 - 119 + (int)(100.0 - 100.0 * this.d),
               30,
               (int)(this.d * 100.0),
               20,
               this.e ? I18n.func_135052_a("action.names.stopfollowme", new Object[0]) : I18n.func_135052_a("action.names.followme", new Object[0])
            )
         );
      this.field_146292_n.add(new GuiButton(1, var5 / 2 + 19, 30, (int)(this.d * 100.0), 20, I18n.func_135052_a("action.names.gohome", new Object[0])));
      this.field_146297_k.field_71446_o.func_110577_a(b);
      this.func_73729_b(var5 / 2 - 7, 61 - (int)(15.0 - this.d * 15.0), 32, 0, 15, 15);
      this.field_146292_n.add(new GuiButton(2, var5 / 2 - 10, 59 - (int)(15.0 - this.d * 15.0), 20, 20, ""));
      this.func_73729_b(var5 / 2 - 20, 20, this.c.func_184212_Q().func_187225_a(BeeEntityBase.K) ? 0 : 40, 130, 40, 40);
   }

   protected void func_73864_a(int var1, int var2, int var3) {
      ScaledResolution var4 = new ScaledResolution(this.field_146297_k);
      int var5 = var4.func_78326_a();
      if ((Boolean)this.c.func_184212_Q().func_187225_a(BeeEntityBase.K) && var1 >= var5 / 2 - 20 && var1 <= var5 / 2 + 20 && var2 >= 20 && var2 <= 60) {
         PacketHandler.b.sendToServer(new BeeOpenChestPacket(this.c.getGirlId(), this.a.getPersistentID()));
         this.func_146281_b();
      }

      super.func_73864_a(var1, var2, var3);
   }

   protected void func_146284_a(GuiButton var1) {
      super.func_146284_a(var1);
      if (var1.field_146127_k == 0) {
         if (this.e) {
            PacketHandler.b.sendToServer(new ChangeDataParameterPacket(this.c.getGirlId(), "master", ""));
            this.a.func_145747_a(new TextComponentString(I18n.func_135052_a("bee.dialogue.sad", new Object[0])));
         } else {
            PacketHandler.b.sendToServer(new ChangeDataParameterPacket(this.c.getGirlId(), "master", this.a.getPersistentID().toString()));
            this.a.func_145747_a(new TextComponentString(I18n.func_135052_a("bee.dialogue.exited", new Object[0])));
         }

         this.e = !this.e;
         this.a.func_71053_j();
      }

      if (var1.field_146127_k == 1) {
         PacketHandler.b.sendToServer(new SendCompanionHomePacket(this.c.getGirlId()));
         this.a.func_71053_j();
      }

      if (var1.field_146127_k == 2) {
         PacketHandler.b.sendToServer(new SetNewHomePacket(this.c.getGirlId(), new Vec3d(this.c.field_70165_t, this.c.field_70163_u, this.c.field_70161_v)));
         this.a.func_71053_j();
         this.a.func_145747_a(new TextComponentString(I18n.func_135052_a("bee.dialogue.home", new Object[0])));
      }
   }

}
