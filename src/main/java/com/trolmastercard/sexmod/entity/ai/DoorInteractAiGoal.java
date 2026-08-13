package com.trolmastercard.sexmod.entity.ai;


import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;

public class DoorInteractAiGoal extends EntityAIBase {
   protected EntityLiving c;
   protected BlockPos b = BlockPos.field_177992_a;
   protected BlockDoor d;
   boolean e;
   float f;
   float a;
   int g = 10;

   public DoorInteractAiGoal(EntityLiving var1) {
      this.c = var1;
      if (!(var1.func_70661_as() instanceof PathNavigateGround)) {
         throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
      }
   }

   public boolean func_75250_a() {
      boolean var1 = true;

      for (int var2 = -3; var2 < 5; var2++) {
         for (int var3 = -3; var3 < 5; var3++) {
            IBlockState var4 = this.c.field_70170_p.func_180495_p(this.c.func_180425_c().func_177982_a(var2, 0, var3));
            if (var4.func_177230_c() instanceof BlockDoor && var4.func_185904_a() == Material.field_151575_d) {
               var1 = false;
               break;
            }
         }

         if (!var1) {
            break;
         }
      }

      if (var1) {
         return false;
      }

      PathNavigateGround var6 = (PathNavigateGround)this.c.func_70661_as();
      Path var7 = var6.func_75505_d();
      if (var7 != null && !var7.func_75879_b() && var6.func_179686_g()) {
         for (int var8 = 0; var8 < Math.min(var7.func_75873_e() + 2, var7.func_75874_d()); var8++) {
            PathPoint var5 = var7.func_75877_a(var8);
            this.b = new BlockPos(var5.field_75839_a, var5.field_75837_b + 1, var5.field_75838_c);
            if (this.c.func_70092_e(this.b.func_177958_n(), this.c.field_70163_u, this.b.func_177952_p()) <= 2.25) {
               this.d = this.a_clash800(this.b);
               if (this.d != null) {
                  return true;
               }
            }
         }

         this.b = new BlockPos(this.c).func_177984_a();
         this.d = this.a_clash800(this.b);
         return this.d != null;
      } else {
         return false;
      }
   }

   public boolean func_75253_b() {
      return this.g >= 0;
   }

   public void func_75249_e() {
      this.e = false;
      this.f = (float)(this.b.func_177958_n() + 0.5F - this.c.field_70165_t);
      this.a = (float)(this.b.func_177952_p() + 0.5F - this.c.field_70161_v);
      this.d.func_176512_a(this.c.field_70170_p, this.b, true);
   }

   public void func_75246_d() {
      float var1 = (float)(this.b.func_177958_n() + 0.5F - this.c.field_70165_t);
      float var2 = (float)(this.b.func_177952_p() + 0.5F - this.c.field_70161_v);
      float var3 = this.f * var1 + this.a * var2;
      if (var3 < 0.0F && --this.g <= 0) {
         this.d.func_176512_a(this.c.field_70170_p, this.b, false);
         this.e = true;
      }
   }

   public void func_75251_c() {
      this.g = 10;
   }

   private BlockDoor a_clash800(BlockPos var1) {
      IBlockState var2 = this.c.field_70170_p.func_180495_p(var1);
      Block var3 = var2.func_177230_c();
      return var3 instanceof BlockDoor && var2.func_185904_a() == Material.field_151575_d ? (BlockDoor)var3 : null;
   }

}
