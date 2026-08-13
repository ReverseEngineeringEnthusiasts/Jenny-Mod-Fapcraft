package com.trolmastercard.sexmod.item;

import com.trolmastercard.sexmod.client.renderer.AlliesLampRenderer;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.AllieEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.cj;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.d3;







import net.minecraft.util.ResourceLocation;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class AlliesLampItem extends Item implements IAnimatable {
   static final String e = "sexmodAllieInUse";
   static final String d = "sexmodAllieInUseTicks";
   public static final String j = "sexmodUses";
   public static final String h = "sexmodAllieID";
   static final Integer c = 95;
   static final Integer k = 50;
   public static final int a = 150;
   public static final float f = 0.75F;
   public static final AlliesLampItem b = new AlliesLampItem();
   private final AnimationFactory i = new AnimationFactory(this);
   AnimationController<AlliesLampItem> g;

   public AlliesLampItem() {
      this.func_77637_a(CreativeTabs.field_78026_f);
      this.field_77777_bU = 1;
   }

   public static void register() {
      b.setRegistryName(new ResourceLocation("sexmod", "allies_lamp"));
      b.func_77655_b("allies_lamp");
      MinecraftForge.EVENT_BUS.register(AlliesLampItem.class);
   }

   @SubscribeEvent
   public static void a(Register<Item> var0) {
      var0.getRegistry().register(b);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void a(ModelRegistryEvent var0) {
      ModelLoader.setCustomModelResourceLocation(b, 0, new ModelResourceLocation("sexmod:allies_lamp"));
      b.setTileEntityItemStackRenderer(new AlliesLampRenderer());
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(Pre var1) {
      NBTTagCompound var2 = Minecraft.func_71410_x().field_71439_g.getEntityData();
      if (var2.func_74767_n("sexmodAllieInUse")) {
         var1.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void a(LootTableLoadEvent var1) {
      HashSet var2 = new HashSet();
      var2.add(LootTableList.field_186424_f);
      var2.add(LootTableList.field_186429_k);
      var2.add(LootTableList.field_186422_d);
      var2.add(LootTableList.field_191192_o);
      if (var2.contains(var1.getName())) {
         LootPool var3 = var1.getTable().getPool("pool3");
         if (var3 == null) {
            var3 = var1.getTable().getPool("pool2");
         }

         if (var3 != null) {
            var3.addEntry(new LootEntryItem(b, 5, 0, new LootFunction[0], new LootCondition[0], "sexmod:allies_lamp"));
         }
      }
   }

   @Override
   public void registerControllers(AnimationData var1) {
      this.g = new AnimationController<>(this, "controller", 2.0F, this::a);
      var1.addAnimationController(this.g);
   }

   @SideOnly(Side.CLIENT)
   public void func_77624_a(ItemStack var1, World var2, List<String> var3, ITooltipFlag var4) {
      NBTTagCompound var5 = var1.func_77978_p();
      if (var5 != null) {
         int var6 = 3 - var1.func_77978_p().func_74762_e("sexmodUses");
         switch (var6) {
            case 0:
               var3.add("no wishes left");
               break;
            case 1:
               var3.add("1 wish left");
               break;
            case 2:
               var3.add("2 wishes left");
         }
      }
   }

   @SideOnly(Side.CLIENT)
   protected <segs extends IAnimatable> PlayState a(AnimationEvent<segs> var1) {
      EntityPlayerSP var2 = Minecraft.func_71410_x().field_71439_g;
      NBTTagCompound var3 = var2.getEntityData();
      boolean var4 = var3.func_74767_n("sexmodAllieInUse");
      if (!var4) {
         var1.getController().clearAnimationCache();
         return PlayState.STOP;
      } else {
         var1.getController().setAnimation(new AnimationBuilder().addAnimation("animation.lamp.rub", ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME));
         return PlayState.CONTINUE;
      }
   }

   public void func_77663_a(ItemStack var1, World var2, Entity var3, int var4, boolean var5) {
      if (var3 instanceof EntityPlayer) {
         EntityPlayer var6 = (EntityPlayer)var3;
         NBTTagCompound var7 = var3.getEntityData();
         if (var1.equals(var6.func_184614_ca()) || var1.equals(var6.func_184592_cb())) {
            boolean var8 = var7.func_74767_n("sexmodAllieInUse");
            int var9 = var7.func_74762_e("sexmodAllieInUseTicks");
            if (var8) {
               var7.func_74768_a("sexmodAllieInUseTicks", var9 + 1);
               if (var9 > k && var9 < c) {
                  double var10 = (float)(var9 - k) / (c - k);
                  var10 = RotationHelper.h(var10);
                  Vec3d var12 = new Vec3d(0.0, var6.eyeHeight * (1.0 - var10), 0.0);
                  cj.a(var2, EnumParticleTypes.CRIT_MAGIC, this.a_clash32(var6).func_178787_e(var12), (int)(var10 * 150.0), var10 * 0.75, var10);
               }

               if (var9 >= c) {
                  cj.a(var2, EnumParticleTypes.CRIT_MAGIC, this.a_clash32(var6), 150, 0.75, 2.0);
                  var7.func_74757_a("sexmodAllieInUse", false);
                  var7.func_74768_a("sexmodAllieInUseTicks", 0);
                  if (var2.field_72995_K) {
                     d3.a_clash122(false);
                  } else {
                     NBTTagCompound var15 = var1.func_77978_p();
                     if (var15 == null) {
                        var15 = new NBTTagCompound();
                     }

                     var15.func_74768_a("sexmodUses", var15.func_74762_e("sexmodUses") + 1);
                     AllieEntity var11 = new AllieEntity(var6.field_70170_p, var6.func_184614_ca());
                     var11.e_clash499(var6.getPersistentID());
                     Vec3d var16 = this.a_clash32(var6);
                     var11.func_70080_a(var16.field_72450_a, var16.field_72448_b, var16.field_72449_c, var6.field_70177_z + 180.0F, var6.field_70125_A);
                     var11.c_clash502(var11.func_174791_d());
                     var11.b_clash431(var6.field_70177_z + 180.0F);
                     var11.a_clash504(true);
                     var11.func_189654_d(true);
                     var11.field_70145_X = true;
                     var6.field_70170_p.func_72838_d(var11);
                     BlockPos var13 = var11.func_180425_c().func_177982_a(0, -1, 0);
                     if (var11.field_70170_p.func_180495_p(var13).func_177230_c().equals(Blocks.field_150354_m)) {
                        var11.b(fp.SUMMON_SAND);
                     } else {
                        var11.b(var11.f_clash697() ? fp.SUMMON : fp.SUMMON_NORMAL);
                     }

                     var1.func_77982_d(var15);
                  }
               }
            }
         }
      }
   }

   Vec3d a_clash32(EntityPlayer var1) {
      return var1.func_174791_d().func_178787_e(ck.a_clash306(new Vec3d(0.0, 0.0, 2.0), var1.field_70759_as));
   }

   @Override
   public AnimationFactory getFactory() {
      return this.i;
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }

   public static class a {
      @SubscribeEvent
      public void a(PlayerLoggedOutEvent var1) {
         var1.player.getEntityData().func_74757_a("sexmodAllieInUse", false);
      }

      @SubscribeEvent
      public void a(RightClickItem var1) {
         EntityPlayer var2 = var1.getEntityPlayer();
         EnumHand var3 = var1.getHand();
         ItemStack var4 = var2.func_184586_b(var3);
         if (!AbstractPlayerGirlEntity.e(var2)) {
            if (!var2.field_70170_p.field_72995_K || d3.b_clash121()) {
               if (!var2.field_70170_p.field_72995_K) {
                  try {
                     for (BaseGirlEntity var6 : BaseGirlEntity.ad_clash509()) {
                        if (!var6.field_70128_L && var6 instanceof AllieEntity) {
                           AllieEntity var7 = (AllieEntity)var6;
                           ItemStack var8 = (ItemStack)var7.func_184212_Q().func_187225_a(AllieEntity.N);
                           if (var4.equals(var8)) {
                              return;
                           }
                        }
                     }
                  } catch (ConcurrentModificationException var9) {
                  }
               }

               if (var4.func_77973_b() == AlliesLampItem.b) {
                  NBTTagCompound var10 = var4.func_77978_p();
                  if (var10 == null || var10.func_74762_e("sexmodUses") < 3) {
                     NBTTagCompound var11 = var2.getEntityData();
                     boolean var12 = var11.func_74767_n("sexmodAllieInUse");
                     if (!var12) {
                        var11.func_74757_a("sexmodAllieInUse", true);
                        var11.func_74768_a("sexmodAllieInUseTicks", 0);
                     }
                  }
               }
            }
         }
      }

      private static ConcurrentModificationException a(ConcurrentModificationException var0) {
         return var0;
      }
   }
}
