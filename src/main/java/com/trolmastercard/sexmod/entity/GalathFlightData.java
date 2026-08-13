package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.GalathActionListener;
import com.trolmastercard.sexmod.api.ao;
import com.trolmastercard.sexmod.api.b2;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetControllerPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.networking.SpawnEnergyBallParticlesPacket2;
import com.trolmastercard.sexmod.util.GalathDamageSource;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.g1;
import com.trolmastercard.sexmod.util.g8;
import com.trolmastercard.sexmod.util.gc;
import com.trolmastercard.sexmod.util.h_;








import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketEntity.S16PacketEntityLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public enum GalathFlightData {
   CHANGE_POSITION(
      var0 -> {
         World var1 = var0.field_70170_p;
         BlockPos var2 = var0.func_180425_c();
         BlockPos var3 = var0.M_clash691().func_180425_c();
         ArrayList var4 = new ArrayList();
         HashMap var5 = new HashMap();
         int var6 = 0;
         boolean var7 = !var1.func_175623_d(var2.func_177977_b());

         for (int var8 = -10; var8 < 10; var8++) {
            for (int var9 = -10; var9 < 10; var9++) {
               for (int var10 = -10; var10 < 10; var10++) {
                  if (var8 != 0 || var9 != 0 || var10 != 0) {
                     BlockPos var11 = var3.func_177971_a(new BlockPos(var8, var9, var10));
                     if ((!var7 || var2.func_177956_o() < var11.func_177956_o())
                        && var1.func_175623_d(var11)
                        && var1.func_175623_d(var11.func_177984_a())
                        && var1.func_175623_d(var11.func_177984_a().func_177984_a())) {
                        RayTraceResult var12 = var1.func_147447_a(new Vec3d(var2), new Vec3d(var11), true, true, true);
                        if (var12 == null) {
                           int var13 = var11.func_177956_o();

                           do {
                              var13--;
                           } while (
                              var13 >= 0
                                 && var1.func_180495_p(new BlockPos(var11.func_177958_n(), var13, var11.func_177952_p())).func_177230_c() instanceof BlockAir
                           );

                           if (!(var1.func_180495_p(new BlockPos(var11.func_177958_n(), var13, var11.func_177952_p())).func_177230_c() instanceof BlockLiquid)) {
                              var4.add(var11);
                              if (var1.func_175623_d(var11.func_177977_b())
                                 && var1.func_175623_d(var11.func_177977_b().func_177977_b())
                                 && !(var3.func_185332_f(var11.func_177958_n(), var11.func_177956_o(), var11.func_177952_p()) < 5.0)
                                 && !(var2.func_185332_f(var11.func_177958_n(), var11.func_177956_o(), var11.func_177952_p()) < 3.0)) {
                                 int var14 = 0;

                                 for (int var15 = -1; var15 < 2; var15++) {
                                    for (int var16 = -1; var16 < 2; var16++) {
                                       for (int var17 = -1; var17 < 4; var17++) {
                                          if (var1.func_175623_d(var11.func_177982_a(var15, var17, var16))) {
                                             var14++;
                                          }
                                       }
                                    }
                                 }

                                 if (var14 >= 25) {
                                    var5.put(var11, var14);
                                    if (var14 > var6) {
                                       var6 = var14;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         if (!var5.isEmpty()) {
            ArrayList<Entry> var18 = new ArrayList<Entry>(var5.entrySet());
            var18.sort((var0x, var1x) -> ((Integer)((Entry) var1x).getValue()).compareTo((Integer)((Entry) var0x).getValue()));
            var0.O = new Vec3d((Vec3i)((Entry)var18.get(ThreadNames.a_clash165(var18.size() - 1))).getKey());
         } else if (var4.isEmpty()) {
            var0.O = new Vec3d(
               var3.func_177963_a(ThreadNames.a_clash167(10.0F, true), ThreadNames.a_clash167(10.0F, false), ThreadNames.a_clash167(10.0F, true))
            );
         } else {
            var0.O = new Vec3d((Vec3i)var4.get(Reference.f.nextInt(var4.size())));
         }

         var0.bL = null;
         var0.b_clash690(0);
         var0.b(fp.FLY);
         PacketHandler.b.sendToAllTracking(new ResetControllerPacket(var0.getGirlId()), var0);
      },
      var0 -> {
         Vec3d var1 = var0.func_174791_d();
         Vec3d var2 = var0.O;
         if (var2 != null) {
            var0.bL = var1;
            int var3 = var0.ar();
            var0.b_clash690(var3 + 1);
            if (var3 == 0) {
               Vec3d var4 = var2.func_178788_d(var1);
               Vec3d var5 = var4.func_72432_b();
               var0.field_70159_w = var5.field_72450_a * 0.6F;
               var0.field_70179_y = var5.field_72449_c * 0.6F;
               var0.field_70181_x = ThreadNames.b(var4.field_72448_b * 0.6F, -0.6F, 0.6F);
            }
         }
      },
      var0 -> var0.ar() > 23,
      var0 -> {
         var0.setVelocity(Vec3d.field_186680_a);
         var0.b_clash690(0);
         var0.bL = null;
      },
      false,
      var0 -> true,
      false
   ),
   SUMMON_SKELETON(
      var0 -> {
         var0.b(fp.SUMMON_SKELETON);
         var0.ad = 0;
         EntityDataManager var1 = var0.func_184212_Q();
         var1.func_187227_b(GalathEntity.bN, true);
         var1.func_187227_b(GalathEntity.b7, true);
         var1.func_187227_b(GalathEntity.ay, var0.func_70681_au().nextBoolean());
         BaseGirlEntity.a(var0, SoundHandler.GIRLS_GALATH_STRONGCHARGE, true);
      },
      var0 -> {
         var0.setVelocity(Vec3d.field_186680_a);
         if (var0.ad == 30.0F) {
            GalathEntity.a_clash692(var0, 0.0F);
            Vec3d var1 = var0.func_174791_d();
            Vec3d var2 = var0.M_clash691().func_174791_d();
            Random var3 = var0.func_70681_au();
            if ((Boolean)var0.func_184212_Q().func_187225_a(GalathEntity.ay)) {
               if ((Boolean)var0.func_184212_Q().func_187225_a(GalathEntity.bN)) {
                  Vec3d var31 = var1;
                  Vec3d var12 = var31.func_178787_e(ck.rotateByYaw(ck.c_clash308(GalathEntity.bz), 180.0F + var0.field_70761_aq));
                  Vec3d var19 = var2.func_178788_d(var12).func_72432_b();
                  var19 = new Vec3d(
                     var19.field_72450_a + var3.nextDouble() * 0.3F,
                     var19.field_72448_b + var3.nextDouble() * 0.3F,
                     var19.field_72449_c + var3.nextDouble() * 0.3F
                  );
                  var19 = var19.func_72432_b();
                  Vec3d var26 = new Vec3d(var19.field_72450_a * 0.4F, var19.field_72448_b * 0.4F, var19.field_72449_c * 0.4F);
                  DragonEntity var29 = new DragonEntity(var0.field_70170_p, var0, var26);
                  var29.func_70634_a(var12.field_72450_a, var12.field_72448_b, var12.field_72449_c);
                  var0.field_70170_p.func_72838_d(var29);
               }

               if ((Boolean)var0.func_184212_Q().func_187225_a(GalathEntity.b7)) {
                  Vec3d var32 = var1;
                  Vec3d var13 = var32.func_178787_e(ck.rotateByYaw(ck.c_clash308(GalathEntity.bC), 180.0F + var0.field_70761_aq));
                  Vec3d var22 = var2.func_178788_d(var13).func_72432_b();
                  var22 = new Vec3d(
                     var22.field_72450_a + var3.nextDouble() * 0.3F,
                     var22.field_72448_b + var3.nextDouble() * 0.3F,
                     var22.field_72449_c + var3.nextDouble() * 0.3F
                  );
                  var22 = var22.func_72432_b();
                  Vec3d var27 = new Vec3d(var22.field_72450_a * 0.4F, var22.field_72448_b * 0.4F, var22.field_72449_c * 0.4F);
                  DragonEntity var30 = new DragonEntity(var0.field_70170_p, var0, var27);
                  var30.func_70634_a(var13.field_72450_a, var13.field_72448_b, var13.field_72449_c);
                  var0.field_70170_p.func_72838_d(var30);
               }
            } else {
               if ((Boolean)var0.func_184212_Q().func_187225_a(GalathEntity.bN)) {
                  Vec3d var9 = var1;
                  Vec3d var5 = var9.func_178787_e(ck.rotateByYaw(GalathEntity.bz, 180.0F + var0.field_70761_aq));
                  Vec3d var6 = var2.func_178788_d(var5).func_72432_b();
                  var6 = new Vec3d(
                     var6.field_72450_a + var3.nextDouble() * 0.3F,
                     var6.field_72448_b + var3.nextDouble() * 0.3F,
                     var6.field_72449_c + var3.nextDouble() * 0.3F
                  );
                  var6 = var6.func_72432_b();
                  Vec3d var7 = new Vec3d(var6.field_72450_a * 0.4F, var6.field_72448_b * 0.4F, var6.field_72449_c * 0.4F);
                  DragonEntity var8 = new DragonEntity(var0.field_70170_p, var0, var7);
                  var8.func_70634_a(var5.field_72450_a, var5.field_72448_b, var5.field_72449_c);
                  var0.field_70170_p.func_72838_d(var8);
               }

               if ((Boolean)var0.func_184212_Q().func_187225_a(GalathEntity.b7)) {
                  Vec3d var10 = var1;
                  Vec3d var11 = var10.func_178787_e(ck.rotateByYaw(GalathEntity.bC, 180.0F + var0.field_70761_aq));
                  Vec3d var16 = var2.func_178788_d(var11).func_72432_b();
                  var16 = new Vec3d(
                     var16.field_72450_a + var3.nextDouble() * 0.3F,
                     var16.field_72448_b + var3.nextDouble() * 0.3F,
                     var16.field_72449_c + var3.nextDouble() * 0.3F
                  );
                  var16 = var16.func_72432_b();
                  Vec3d var25 = new Vec3d(var16.field_72450_a * 0.4F, var16.field_72448_b * 0.4F, var16.field_72449_c * 0.4F);
                  DragonEntity var28 = new DragonEntity(var0.field_70170_p, var0, var25);
                  var28.func_70634_a(var11.field_72450_a, var11.field_72448_b, var11.field_72449_c);
                  var0.field_70170_p.func_72838_d(var28);
               }
            }
         }
      },
      var0 -> var0.ad >= 45,
      var0 -> var0.ad = 0,
      true,
      var0 -> var0.bI.size() < 2,
      true
   ),
   ATTACK_SWORD(var0 -> {
      var0.a_clash643(0);
      var0.b(fp.ATTACK_SWORD);
      var0.setVelocity(Vec3d.field_186680_a);
      Vec3d var1 = var0.func_174791_d();
      var0.e(var1);
      Vec3d var2 = var0.M_clash691().func_174791_d();
      g8 var3 = new g8(var2.field_72450_a - var1.field_72450_a, var2.field_72449_c - var1.field_72449_c);
      double var4 = gc.b(Math.atan2(var3.a, var3.b)) - 90.0;
      var0.setAnchored(true);
      var0.setTargetPosition(var1);
      var0.setYawRotation((float)var4);
      BaseGirlEntity.a(var0, SoundHandler.GIRLS_GALATH_STRONGCHARGE, true);
   }, var0 -> {
      EntityLivingBase var1 = var0.M_clash691();
      int var2 = var0.az() + 1;
      var0.a_clash643(var2);
      if (ThreadNames.a_clash164(var2, 24.0, 32.0)) {
         Vec3d var3 = var1.func_174791_d().func_72441_c(0.0, var1.func_70047_e(), 0.0);
         g8 var4 = new g8(var3.field_72450_a - var0.field_70165_t, var3.field_72449_c - var0.field_70161_v);
         double var5 = gc.b(Math.atan2(var4.a, var4.b)) - 90.0;
         var0.setYawRotation((float)var5);
         Vec3d var7 = ck.rotateByYaw(new Vec3d(0.0, 0.0, 3.0), (float)(var5 + 180.0));
         Vec3d var8 = var0.B_clash642();
         Vec3d var9 = var3.func_178787_e(var7);
         float var10 = (var2 - 24) / 8.0F;
         Vec3d var11 = RotationHelper.a(var8, var9, var10);
         var0.setTargetPosition(var11);
      } else if (ThreadNames.a_clash164(var2, 32.0, 54.0)) {
         Vec3d var12 = ck.rotateByYaw(new Vec3d(0.0, 0.0, 1.5), var0.getYawRotation() + 180.0F);
         Vec3d var14 = var1.func_174791_d().func_178787_e(var12);
         var0.setTargetPosition(var14);
         GalathDamageSource var15 = new GalathDamageSource(var0);
         var1.field_70737_aN = 0;
         var1.field_70172_ad = 0;
         if (var2 == 36) {
            var1.func_70097_a(var15, 5.0F);
         }

         if (var2 == 40) {
            var1.func_70097_a(var15, 5.0F);
         }
      } else if (var2 == 54) {
         var0.setAnchored(false);
         var0.b(fp.FLY);
         Vec3d var13 = var0.B_clash642().func_178788_d(var0.func_174791_d()).func_72432_b();
         var0.field_70159_w = var13.field_72450_a * 0.6F;
         var0.field_70181_x = var13.field_72448_b * 0.6F;
         var0.field_70179_y = var13.field_72449_c * 0.6F;
         var0.b_clash690(1);
      } else {
         var0.b_clash690(var0.ar() + 1);
      }
   }, var0 -> var0.ar() > 23, var0 -> {
      var0.b_clash690(0);
      var0.setVelocity(Vec3d.field_186680_a);
      var0.a_clash643(-1);
      var0.setAnchored(false);
   }, true, var0 -> true, false),
   RAPE(
      var0 -> {
         var0.b(fp.RAPE_PREPARE);
         var0.aF = 0;
         var0.bd = null;
         var0.O = null;
         var0.func_184212_Q().func_187227_b(GalathEntity.bO, 0.0F);
      },
      var0 -> {
         if (++var0.aF >= 48) {
            var0.b(fp.RAPE_CHARGE);
            EntityLivingBase var1 = var0.M_clash691();
            if (var0.bd == null) {
               var0.O = var1.func_174791_d().func_72441_c(0.0, var1.func_70047_e() / 2.0F, 0.0);
               var0.bd = var0.func_174791_d();
               Vec3d var2 = var1.func_174791_d().func_178788_d(var0.func_174791_d()).func_72432_b();
               var0.setYawRotation((float)(gc.b(Math.atan2(var2.field_72449_c, var2.field_72450_a)) - 90.0));
            }

            Vec3d var20 = var0.func_174791_d();
            Vec3d var3 = var20.func_178786_a(0.65F, 0.65F, 0.65F);
            Vec3d var4 = var20.func_72441_c(0.65F, 0.65F, 0.65F);
            AxisAlignedBB var5 = new AxisAlignedBB(
               var3.field_72450_a, var3.field_72448_b, var3.field_72449_c, var4.field_72450_a, var4.field_72448_b, var4.field_72449_c
            );

            for (EntityPlayer var8 : var0.field_70170_p.func_72872_a(EntityPlayer.class, var5)) {
               if (!var8.field_70128_L && var8.field_70122_E && BaseGirlEntity.a(var8.getPersistentID(), Boolean.valueOf(true)) == null) {
                  Vec3d var9 = var8.func_174791_d();
                  Vec3d var10 = var20.func_178788_d(var9);
                  Vec3d var11 = ck.rotateByYaw(var10, var0.getYawRotation());
                  double var12 = Math.abs(var11.field_72450_a);
                  if (!(var12 > 0.65F)) {
                     for (EntityWitherSkeleton var15 : var0.bI) {
                        Vec3d var16 = var15.func_174791_d();
                        var15.field_70170_p.func_72900_e(var15);
                        PacketHandler.b
                           .sendToAllTracking(
                              new SpawnEnergyBallParticlesPacket2(var16, true),
                              new TargetPoint(var15.field_71093_bK, var16.field_72450_a, var16.field_72448_b, var16.field_72449_c, 50.0)
                           );
                     }

                     var0.bI.clear();
                     EntityPlayerMP var30 = (EntityPlayerMP)var8;
                     var0.setTargetPosition(var8.func_174791_d());
                     var0.setInteractionPlayerUUID(var8.getPersistentID());
                     var0.setAnchored(true);
                     var0.b(fp.RAPE_INTRO);
                     byte var32 = (byte)MathHelper.func_76141_d((var0.getYawRotation() + 180.0F) * 256.0F / 360.0F);
                     PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), var30);
                     var30.field_71135_a.func_147359_a(new SPacketEntityVelocity(var30.func_145782_y(), 0.0, 0.0, 0.0));
                     var30.field_71135_a.func_147359_a(new S16PacketEntityLook(var30.func_145782_y(), var32, (byte)-14, true));
                     return;
                  }
               }
            }

            Vec3d var22 = var0.bd;
            Vec3d var23 = var0.O;
            Vec3d var24 = var23.func_178788_d(var22);
            Vec3d var25 = var23.func_178787_e(var24);
            var25 = new Vec3d(var25.field_72450_a, var22.field_72448_b, var25.field_72449_c);
            boolean var27 = var20.func_72438_d(new Vec3d(var22.field_72450_a, var20.field_72448_b, var22.field_72449_c))
               > var20.func_72438_d(new Vec3d(var25.field_72450_a, var20.field_72448_b, var25.field_72449_c));
            double var28;
            double var31;
            if (var27) {
               var28 = ck.a(var23, var25, var20);
               var31 = var23.func_72438_d(var25);
            } else {
               var28 = ck.a(var22, var23, var20);
               var31 = var22.func_72438_d(var23);
            }

            double var33 = var31 / 0.05F;
            double var18 = 1.0 / var33 * 20.0;
            var28 += var18;
            if (!var27 && var28 < 0.9F) {
               var0.O = var1.func_174791_d().func_72441_c(0.0, var1.func_70047_e() / 2.0F, 0.0);
            }

            if (var27) {
               var20 = new Vec3d(
                  RotationHelper.b(var23.field_72450_a, var25.field_72450_a, Math.min(1.0, var28)),
                  RotationHelper.b(var23.field_72448_b, var25.field_72448_b, Math.min(1.0, RotationHelper.a_clash27(var28))),
                  RotationHelper.b(var23.field_72449_c, var25.field_72449_c, Math.min(1.0, var28))
               );
            } else {
               var20 = new Vec3d(
                  RotationHelper.b(var22.field_72450_a, var23.field_72450_a, var28),
                  RotationHelper.b(var22.field_72448_b, var23.field_72448_b, RotationHelper.g(var28)),
                  RotationHelper.b(var22.field_72449_c, var23.field_72449_c, var28)
               );
            }

            var0.func_70107_b(var20.field_72450_a, var20.field_72448_b, var20.field_72449_c);
            if (var27) {
               var0.func_184212_Q().func_187227_b(GalathEntity.bO, (float)var28);
            }
         }
      },
      var0 -> {
         if (var0.getCurrentAction() == fp.RAPE_INTRO) {
            return true;
         }

         Vec3d var1 = var0.bd;
         Vec3d var2 = var0.O;
         if (var1 == null) {
            return false;
         }

         Vec3d var3 = var2.func_178788_d(var1);
         Vec3d var4 = var2.func_178787_e(var3);
         var4 = new Vec3d(var4.field_72450_a, var1.field_72448_b, var4.field_72449_c);
         return var0.func_70011_f(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c) < 0.1F;
      },
      var0 -> {
         var0.O = null;
         var0.bd = null;
         var0.aF = 0;
         var0.func_184212_Q().func_187227_b(GalathEntity.bO, 0.0F);
      },
      true,
      var0 -> true,
      true
   );

   final h_ a;
   final b2 f;
   final ao c;
   final GalathActionListener b;
   final g1 d;
   public final boolean applyAttackCoolDown;
   public final boolean onlyDoThisOnPlayers;

   GalathFlightData(b2 var3, ao var4, h_ var5, GalathActionListener var6, boolean var7, g1 var8, boolean var9) {
      this.a = var5;
      this.f = var3;
      this.c = var4;
      this.b = var6;
      this.applyAttackCoolDown = var7;
      this.d = var8;
      this.onlyDoThisOnPlayers = var9;
   }

   public void b_clash706(GalathEntity var1) {
      this.f.a_clash18(var1);
   }

   public boolean c_clash707(GalathEntity var1) {
      return this.a.a_clash794(var1);
   }

   public void a_clash708(GalathEntity var1) {
      this.c.a_clash46(var1);
   }

   public void e(GalathEntity var1) {
      this.b.a_clash857(var1);
   }

   public boolean d_clash709(GalathEntity var1) {
      return this.d.a_clash473(var1);
   }

}
