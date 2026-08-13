package com.trolmastercard.sexmod.entity;


import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class KoboldEggProjectileEntity extends EntityEnderPearl {
   public KoboldEggProjectileEntity(World var1) {
      super(var1);
   }

   public KoboldEggProjectileEntity(World var1, EntityLivingBase var2) {
      super(var1, var2);
   }

   protected void func_70184_a(RayTraceResult var1) {
      EntityLivingBase var2 = this.func_85052_h();
      if (var2 != null) {
         if (var1.field_72313_a == Type.BLOCK) {
            BlockPos var7 = var1.func_178782_a();
            TileEntity var10 = this.field_70170_p.func_175625_s(var7);
            if (var10 instanceof TileEntityEndGateway) {
               TileEntityEndGateway var12 = (TileEntityEndGateway)var10;
               if (var2 instanceof EntityPlayerMP) {
                  CriteriaTriggers.field_192124_d.func_192193_a((EntityPlayerMP)var2, this.field_70170_p.func_180495_p(var7));
               }

               var12.func_184306_a(var2);
               this.func_70106_y();
               return;
            }
         }

         for (int var8 = 0; var8 < 32; var8++) {
            this.field_70170_p
               .func_175688_a(
                  EnumParticleTypes.PORTAL,
                  this.field_70165_t,
                  this.field_70163_u + this.field_70146_Z.nextDouble() * 2.0,
                  this.field_70161_v,
                  this.field_70146_Z.nextGaussian(),
                  0.0,
                  this.field_70146_Z.nextGaussian(),
                  new int[0]
               );
         }

         if (!this.field_70170_p.field_72995_K) {
            BaseGirlEntity var9 = (BaseGirlEntity)var2;
            if (var9.l.func_72438_d(this.func_174791_d()) < 5.0) {
               EnderTeleportEvent var11 = new EnderTeleportEvent(var2, this.field_70165_t, this.field_70163_u, this.field_70161_v, 5.0F);
               if (!MinecraftForge.EVENT_BUS.post(var11)) {
                  if (var2.func_184218_aH()) {
                     var2.func_184210_p();
                  }

                  var2.func_70634_a(this.field_70165_t, this.field_70163_u, this.field_70161_v);
                  var2.field_70143_R = 0.0F;
               }
            }

            this.func_70106_y();
         }
      } else {
         if (var1.field_72313_a == Type.BLOCK) {
            BlockPos var3 = var1.func_178782_a();
            TileEntity var4 = this.field_70170_p.func_175625_s(var3);
            if (var4 instanceof TileEntityEndGateway) {
               TileEntityEndGateway var5 = (TileEntityEndGateway)var4;
               var5.func_184306_a(this);
               return;
            }
         }

         for (int var6 = 0; var6 < 32; var6++) {
            this.field_70170_p
               .func_175688_a(
                  EnumParticleTypes.PORTAL,
                  this.field_70165_t,
                  this.field_70163_u + this.field_70146_Z.nextDouble() * 2.0,
                  this.field_70161_v,
                  this.field_70146_Z.nextGaussian(),
                  0.0,
                  this.field_70146_Z.nextGaussian(),
                  new int[0]
               );
         }

         if (!this.field_70170_p.field_72995_K) {
            this.func_70106_y();
         }
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }

   public static class a {
      @SubscribeEvent
      public void a(EnderTeleportEvent var1) {
         if (var1.getEntityLiving() instanceof BaseGirlEntity) {
            BaseGirlEntity var2 = (BaseGirlEntity)var1.getEntityLiving();
            var2.q = null;
            var2.b(fp.NULL);
            var2.func_184212_Q().func_187227_b(BaseGirlEntity.G, false);
            var2.x_clash475();
         }
      }
   }
}
