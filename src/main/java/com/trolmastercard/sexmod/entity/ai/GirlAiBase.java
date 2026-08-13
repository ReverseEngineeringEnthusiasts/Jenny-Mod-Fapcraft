package com.trolmastercard.sexmod.entity.ai;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;

public class GirlAiBase extends EntityAIBase {
   private final EntityVillager c;
   private EntityVillager d;
   private final World a;
   private int b;

   public GirlAiBase(EntityVillager var1) {
      this.c = var1;
      this.a = var1.field_70170_p;
      this.func_75248_a(3);
   }

   public boolean func_75250_a() {
      if (this.b != 0) {
         return false;
      }

      Entity var1 = this.a.func_72857_a(EntityVillager.class, this.c.func_174813_aQ().func_72314_b(8.0, 3.0, 8.0), this.c);
      if (var1 == null) {
         return false;
      }

      this.d = (EntityVillager)var1;
      return true;
   }

   public void func_75249_e() {
      this.b = 300;
      this.c.func_70947_e(true);
   }

   public void func_75251_c() {
   }

   public boolean func_75253_b() {
      return true;
   }

   public void func_75246_d() {
      this.b--;
      this.c.func_70671_ap().func_75651_a(this.d, 10.0F, 30.0F);
      if (this.c.func_70068_e(this.d) > 2.25) {
         this.c.func_70661_as().func_75497_a(this.d, 0.25);
      }

      if (this.b <= 0) {
         this.a_clash349();
         this.c.field_70714_bg.func_85156_a(this);
      }

      if (this.c.func_70681_au().nextInt(35) == 0) {
         this.a.func_72960_a(this.c, (byte)12);
      }
   }

   private void a_clash349() {
      EntityVillager var1 = this.c.func_90011_a(this.d);
      this.d.func_70873_a(6000);
      this.c.func_70873_a(6000);
      this.d.func_175549_o(false);
      this.c.func_175549_o(false);
      BabyEntitySpawnEvent var2 = new BabyEntitySpawnEvent(this.c, this.d, var1);
      if (!MinecraftForge.EVENT_BUS.post(var2) && var2.getChild() != null) {
         EntityAgeable var3 = var2.getChild();
         var3.func_70873_a(-24000);
         var3.func_70012_b(this.c.field_70165_t, this.c.field_70163_u, this.c.field_70161_v, 0.0F, 0.0F);
         this.a.func_72838_d(var3);
         this.a.func_72960_a(var3, (byte)12);
      }
   }

}
