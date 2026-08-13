package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.AlliePlayerEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BeePlayerEntity;
import com.trolmastercard.sexmod.entity.SlimePlayerEntity;
import com.trolmastercard.sexmod.entity.fp;







import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class fu {
   static final int a = 284453;

   @SubscribeEvent
   public void a(PlayerSleepInBedEvent var1) {
      EntityPlayer var2 = var1.getEntityPlayer();
      AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.g(var2);
      if (var3 != null) {
         if (var2.func_70093_af()) {
            var1.setResult(SleepResult.OTHER_PROBLEM);
         }
      }
   }

   @SubscribeEvent
   public void a(GetCollisionBoxesEvent var1) {
   }

   @SubscribeEvent
   public void a(RightClickBlock var1) {
      AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.d_clash567(var1.getEntityPlayer().getPersistentID());
      BlockPos var3 = var1.getPos();
      World var4 = var1.getEntityPlayer().field_70170_p;
      EntityPlayer var5 = var1.getEntityPlayer();
      if (var2 != null) {
         if (var2.v_clash227()) {
            if (cj.a(var4, var3, var1.getHitVec(), var1.getFace(), var5)) {
               if ((Boolean)var2.func_184212_Q().func_187225_a(BaseGirlEntity.G)) {
                  var1.setCanceled(true);
               } else if (var5.func_70093_af()) {
                  ArrayList var6 = new ArrayList();
                  if (var4.func_180495_p(var3.func_177978_c()).func_177230_c() == Blocks.field_150350_a) {
                     var6.add(var3.func_177978_c());
                  }

                  if (var4.func_180495_p(var3.func_177974_f()).func_177230_c() == Blocks.field_150350_a) {
                     var6.add(var3.func_177974_f());
                  }

                  if (var4.func_180495_p(var3.func_177968_d()).func_177230_c() == Blocks.field_150350_a) {
                     var6.add(var3.func_177968_d());
                  }

                  if (var4.func_180495_p(var3.func_177976_e()).func_177230_c() == Blocks.field_150350_a) {
                     var6.add(var3.func_177976_e());
                  }

                  BlockPos var7 = null;

                  for (BlockPos var9 : (java.util.Collection<BlockPos>) (var6) ) {
                     if (var7 == null) {
                        var7 = var9;
                     } else {
                        Vec3d var10 = var5.func_174791_d();
                        double var11 = this.a(
                           var9.func_177958_n(), var9.func_177956_o(), var9.func_177952_p(), var10.field_72450_a, var10.field_72448_b, var10.field_72449_c
                        );
                        double var13 = this.a(
                           var7.func_177958_n(), var7.func_177956_o(), var7.func_177952_p(), var10.field_72450_a, var10.field_72448_b, var10.field_72449_c
                        );
                        if (var11 < var13) {
                           var7 = var9;
                        }
                     }
                  }

                  if (var7 == null) {
                     var5.func_145747_a(new TextComponentString("Bed is obscured"));
                  } else {
                     var5.func_70107_b(var7.func_177958_n() + 0.5, var7.func_177956_o(), var7.func_177952_p() + 0.5);
                     if (var3.func_177978_c().equals(var7)) {
                        var5.field_70177_z = 0.0F;
                     }

                     if (var3.func_177974_f().equals(var7)) {
                        var5.field_70177_z = 90.0F;
                     }

                     if (var3.func_177968_d().equals(var7)) {
                        var5.field_70177_z = 180.0F;
                     }

                     if (var3.func_177976_e().equals(var7)) {
                        var5.field_70177_z = -90.0F;
                     }

                     if (var1.getWorld().field_72995_K) {
                        d3.a_clash122(false);
                        var2.H_clash570();
                     } else {
                        var2.c_clash502(new Vec3d(var7.func_177958_n() + 0.5, var7.func_177956_o() + 0.0F, var7.func_177952_p() + 0.5));
                        var2.b_clash431(var5.field_70177_z);
                        var2.func_184212_Q().func_187227_b(BaseGirlEntity.G, true);
                        var2.u_clash377();
                     }
                  }
               }
            }
         }
      }
   }

   double a(double var1, double var3, double var5, double var7, double var9, double var11) {
      double var13 = var1 - var7;
      double var15 = var3 - var9;
      double var17 = var5 - var11;
      return Math.sqrt(var13 * var13 + var15 * var15 + var17 * var17);
   }

   @SubscribeEvent
   public void a(PlayerRespawnEvent var1) {
      EntityPlayer var2 = var1.player;
      if (var2 != null) {
         AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.a_clash568(var2.getPersistentID());
         if (var3 != null) {
            Vec3d var4 = var2.func_174791_d();
            var3.field_71093_bK = var2.field_71093_bK;
            var3.func_70634_a(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c);
            var3.func_70619_bc();
            System.out.println(var2.field_70170_p.func_175697_a(var3.func_180425_c(), 2));
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void b(EntityInteract var1) {
      if (var1.getTarget() instanceof EntityPlayer) {
         if (!var1.getEntityPlayer().func_70093_af()) {
            if (var1.getEntityPlayer().getPersistentID().equals(Minecraft.func_71410_x().field_71439_g.getPersistentID())) {
               EntityPlayerSP var2 = Minecraft.func_71410_x().field_71439_g;
               AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.d_clash567(var2.getPersistentID());
               EntityPlayer var4 = (EntityPlayer)var1.getTarget();
               AbstractPlayerGirlEntity var5 = AbstractPlayerGirlEntity.g(var4);
               if (var5 != null) {
                  if (var3 != null) {
                     var2.func_146105_b(new TextComponentString("no lesbo yet owo"), true);
                  } else if (var5.l_clash467()) {
                     if (var5.p_clash379()) {
                        var5.b_clash230(Minecraft.func_71410_x().field_71439_g);
                     }
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(EntityInteract var1) {
      if (var1.getTarget() instanceof EntityPlayer) {
         if (var1.getEntityPlayer().getPersistentID().equals(Minecraft.func_71410_x().field_71439_g.getPersistentID())) {
            EntityPlayerSP var2 = Minecraft.func_71410_x().field_71439_g;
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.d_clash567(var2.getPersistentID());
            if (var3 != null) {
               EntityPlayer var4 = (EntityPlayer)var1.getTarget();
               AbstractPlayerGirlEntity var5 = AbstractPlayerGirlEntity.d_clash567(var4.getPersistentID());
               if (var5 != null) {
                  var4.func_146105_b(new TextComponentString("no lesbo yet owo"), true);
               } else {
                  if (var3.p_clash379()) {
                     var3.ab = false;
                     var3.b_clash230(var4);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void b(RightClickBlock var1) {
      EntityPlayer var2 = var1.getEntityPlayer();
      AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.g(var2);
      if (var3 != null) {
         if (var3 instanceof SlimePlayerEntity) {
            if (var2.func_70093_af()) {
               if (var2.func_184614_ca().equals(ItemStack.field_190927_a)) {
                  if (!(Boolean)var3.func_184212_Q().func_187225_a(BaseGirlEntity.G)) {
                     if (!(var2.field_70125_A < 20.0F)) {
                        Vec3d var4 = var1.getHitVec();
                        if (var4 != null) {
                           Vec3d var5 = new Vec3d(var4.field_72450_a, Math.floor(var4.field_72448_b) + 0.0, var4.field_72449_c);
                           if (!(var4.func_72438_d(var2.func_174791_d()) > 3.0)) {
                              var2.func_70107_b(var5.field_72450_a, Math.floor(var4.field_72448_b), var5.field_72449_c);
                              var3.c_clash502(var5);
                              var3.b_clash431(var2.field_70177_z);
                              var3.func_184212_Q().func_187227_b(BaseGirlEntity.G, true);
                              var3.func_184212_Q().func_187227_b(BaseGirlEntity.D, 0);
                              var3.b(fp.STARTDOGGY);
                              if (var1.getWorld().field_72995_K && Minecraft.func_71410_x().field_71439_g.getPersistentID().equals(var2.getPersistentID())) {
                                 d3.a_clash122(false);
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

   @SubscribeEvent
   public void a(LivingHurtEvent var1) {
      if (var1.getEntityLiving() instanceof EntityPlayer) {
         if (var1.getSource() == DamageSource.field_76379_h) {
            EntityPlayer var2 = (EntityPlayer)var1.getEntityLiving();
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.g(var2);
            if (var3 != null) {
               if (var3 instanceof AlliePlayerEntity || var3 instanceof BeePlayerEntity) {
                  var1.setCanceled(true);
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(InitGuiEvent var1) {
      GuiScreen var2 = var1.getGui();
      if (var2 instanceof GuiInventory || var2 instanceof GuiContainerCreative) {
         EntityPlayerSP var3 = Minecraft.func_71410_x().field_71439_g;
         if (var3 != null) {
            AbstractPlayerGirlEntity var4 = AbstractPlayerGirlEntity.g(var3);
            if (var4 != null) {
               if (!var4.A_clash381()) {
                  List var5 = var1.getButtonList();
                  String var6 = I18n.func_135052_a(var4.ah_clash493() == 0 ? "action.names.dressup" : "action.names.strip", new Object[0]);
                  var5.add(new GuiButton(284453, (int)(var2.field_146294_l * 0.5 - 35.0), (int)(var2.field_146295_m * 0.87), 70, 20, var6));
                  var1.setButtonList(var5);
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(ActionPerformedEvent var1) {
      GuiScreen var2 = var1.getGui();
      if (var2 instanceof GuiInventory || var2 instanceof GuiContainerCreative) {
         if (var1.getButton().field_146127_k == 284453) {
            Minecraft var3 = Minecraft.func_71410_x();
            AbstractPlayerGirlEntity var4 = AbstractPlayerGirlEntity.d_clash567(var3.field_71439_g.getPersistentID());
            if (var4 != null) {
               if (!var4.A_clash381()) {
                  if (var4.ae_clash498() == null) {
                     if (var4.y_clash492() == fp.NULL) {
                        var3.field_71474_y.field_74320_O = 2;
                        var3.field_71460_t.func_175066_a(null);
                        var4.b(fp.STRIP);
                        d3.a_clash122(false);
                        var3.field_71439_g.func_71053_j();
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void a(LivingDamageEvent var1) {
      if (var1.getSource() == DamageSource.field_76379_h) {
         EntityLivingBase var2 = var1.getEntityLiving();
         if (var2 instanceof EntityPlayer) {
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.d_clash567(var2.getPersistentID());
            if (var3 != null) {
               if (var3 instanceof SlimePlayerEntity) {
                  var1.setResult(Result.DENY);
                  var1.setAmount(0.0F);
                  var1.setCanceled(true);
               }
            }
         }
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
