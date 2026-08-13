package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.ao;
import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.GirlInventoryScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.entity.api.IEllie;
import com.trolmastercard.sexmod.item.LunaRodItem;
import com.trolmastercard.sexmod.networking.CatActivateFishingPacket;
import com.trolmastercard.sexmod.networking.CatEatingDonePacket;
import com.trolmastercard.sexmod.networking.CatThrowAwayItemPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SendChatMessagePacket;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SendGirlToSexPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.ad;
import com.trolmastercard.sexmod.util.af;
import com.trolmastercard.sexmod.util.ah;
import com.trolmastercard.sexmod.util.ak;
import com.trolmastercard.sexmod.util.am;
import com.trolmastercard.sexmod.util.an;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.fg;







import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class LunaEntity extends AbstractGirlNpcEntity implements IEllie, fg {
   public ItemStack ao = new ItemStack(LunaRodItem.a);
   public static final DataParameter<Float> Y = EntityDataManager.func_187226_a(LunaEntity.class, DataSerializers.field_187193_c)
      .func_187156_b()
      .func_187161_a(121);
   public static final DataParameter<ItemStack> az = EntityDataManager.func_187226_a(LunaEntity.class, DataSerializers.field_187196_f)
      .func_187156_b()
      .func_187161_a(120);
   public static final DataParameter<Boolean> af = EntityDataManager.func_187226_a(LunaEntity.class, DataSerializers.field_187198_h)
      .func_187156_b()
      .func_187161_a(119);
   public static final DataParameter<ItemStack> ag = EntityDataManager.func_187226_a(LunaEntity.class, DataSerializers.field_187196_f)
      .func_187156_b()
      .func_187161_a(118);
   static final float ah = 3.0F;
   static final float ax = 1200.0F;
   @Nullable
   public SexEntity av;
   public float aa = 1.0F;
   public float Z = 0.0F;
   int aj = 8000;
   public boolean ac = false;
   int aw = 0;
   boolean ay = false;
   int ak = 0;
   int ab = 0;
   public BlockPos ai;
   int at = 0;
   int as = 0;
   boolean am;
   long al = 0L;
   boolean ar = false;
   Path au = null;
   int aq = 0;
   HashSet<BlockPos> an = new HashSet<>();
   boolean ae = false;
   boolean ad = false;

   public LunaEntity(World var1) {
      super(var1);
      this.P = 230;
      this.O = 150;
      this.K = 320;
      this.V = new Vec3d(0.0, -0.05999999718368053, 0.10000001192092894);
      if (this.Q.getStackInSlot(0) == ItemStack.field_190927_a) {
         this.Q.setStackInSlot(0, new ItemStack(Items.field_151036_c));
      }

      if (this.Q.getStackInSlot(6) == ItemStack.field_190927_a) {
         this.Q.setStackInSlot(6, new ItemStack(Items.field_151112_aM));
      }
   }

   @Override
   public String c_clash241() {
      return "Luna";
   }

   @Override
   public float i_clash226() {
      return -0.2F;
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      this.m.func_187214_a(Y, 0.0F);
      this.m.func_187214_a(az, ItemStack.field_190927_a);
      this.m.func_187214_a(af, false);
      this.m.func_187214_a(ag, ItemStack.field_190927_a);
   }

   @Override
   public void c_clash237() {
      this.a_clash541("Love it here owo");
      this.a(SoundHandler.GIRLS_LUNA_OWO);
   }

   @Override
   public void b(fp var1) {
      if (this.y_clash492() != fp.COWGIRL_SITTING_CUM || var1 != fp.COWGIRL_SITTING_SLOW && var1 != fp.COWGIRL_SITTING_FAST) {
         if (this.y_clash492() != fp.TOUCH_BOOBS_CUM || var1 != fp.TOUCH_BOOBS_FAST && var1 != fp.TOUCH_BOOBS_SLOW) {
            super.b(var1);
         }
      }
   }

   @Override
   public void b_clash158() {
      this.ac = true;
   }

   public float func_70047_e() {
      return 1.34F;
   }

   public boolean func_184645_a(EntityPlayer var1, EnumHand var2) {
      if (super.func_184645_a(var1, var2)) {
         return true;
      }

      ItemStack var3 = var1.func_184586_b(var2);
      boolean var4 = var3.func_77973_b() == Items.field_151057_cb;
      if (var4) {
         var3.func_111282_a(var1, this, var2);
         return true;
      }

      if (this.field_70170_p.field_72995_K && !this.b_clash230(var1)) {
         this.a_clash541(I18n.func_135052_a("bia.dialogue.busy", new Object[0]));
      }

      return true;
   }

   @Override
   public boolean b_clash230(EntityPlayer var1) {
      String[] var2 = new String[]{"action.names.sex", "action.names.touchboobs", "action.names.headpat"};
      ItemStack[] var3 = new ItemStack[]{new ItemStack(Items.field_151115_aP, 3, 0), new ItemStack(Items.field_151115_aP, 2, 1), null};
      a(var1, this, var2, var3);
      return true;
   }

   @SideOnly(Side.CLIENT)
   protected static void a(EntityPlayer var0, BaseGirlEntity var1, String[] var2, ItemStack[] var3) {
      Minecraft.func_71410_x().func_147108_a(new GirlInventoryScreen(var1, var0, var2, var3, true));
   }

   public void b_clash383(ItemStack var1) {
      this.m.func_187227_b(ag, var1);
   }

   @Override
   public void g_clash238() {
      this.z = new EntityAIWanderAvoidWater(this, 0.35);
      this.o = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.field_70714_bg.func_75776_a(5, this.o);
      this.field_70714_bg.func_75776_a(5, this.z);
   }

   @Override
   public void func_70619_bc() {
      super.func_70619_bc();
      if (!this.J_clash526()) {
         this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(1.0);
      } else {
         this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.5);
      }

      this.m_clash393();
      this.i_clash389();
      this.m.func_187227_b(af, this.av != null && this.m.func_187225_a(ag) == ItemStack.field_190927_a);
      if (this.al == this.field_70170_p.func_82737_E() && this.av != null) {
         this.field_70170_p.func_72900_e(this.av);
         this.av = null;
      }

      if (this.ay) {
         double var1 = this.o_clash501().func_72438_d(this.func_174791_d());
         if (!(var1 < 0.5) && this.ak <= 200) {
            if (++this.ak == 60 || this.ak == 120) {
               this.func_70661_as().func_75499_g();
               this.func_70661_as().func_75492_a(this.o_clash501().field_72450_a, this.o_clash501().field_72448_b, this.o_clash501().field_72449_c, 0.2);
            }
         } else {
            this.ay = false;
            this.ak = 0;
            this.m.func_187227_b(G, true);
            this.field_70145_X = true;
            this.func_189654_d(true);
            this.field_70159_w = 0.0;
            this.field_70181_x = 0.0;
            this.field_70179_y = 0.0;
            this.b(fp.WAIT_CAT);
         }
      }

      if (this.ac) {
         this.aw++;
         if (!this.func_174791_d().equals(this.o_clash501()) && this.aw <= 40) {
            this.field_70177_z = this.I_clash415();
            this.func_189654_d(false);
            Vec3d var3 = RotationHelper.a(this.func_174791_d(), this.o_clash501(), 40 - this.aw);
            this.func_70107_b(var3.field_72450_a, var3.field_72448_b, var3.field_72449_c);
         } else {
            this.ac = false;
            this.aw = 0;
            this.b_clash431(this.field_70170_p.func_73046_m().func_184103_al().func_177451_a(this.ae_clash498()).field_70177_z + 180.0F);
            this.m.func_187227_b(G, true);
            this.func_70661_as().func_75499_g();
            this.U();
         }
      }

      this.d_clash384();
      this.m.func_187227_b(az, this.Q.getStackInSlot(6));
   }

   void d_clash384() {
      ItemStack var1 = this.ao;
      ItemStack var2 = (ItemStack)this.m.func_187225_a(az);
      if (!var2.equals(ItemStack.field_190927_a)) {
         Map var3 = EnchantmentHelper.func_82781_a(var2);
         EnchantmentHelper.func_82782_a(var3, var1);
      }
   }

   @Override
   public void func_70071_h_() {
      super.func_70071_h_();
      if (fp.WAIT_CAT.equals(this.y_clash492())) {
         this.f_clash385();
      } else {
         this.ab = 0;
      }
   }

   void f_clash385() {
      EntityPlayer var1 = this.field_70170_p.func_72890_a(this, 10.0);
      if (var1 != null) {
         if (!(var1.func_70032_d(this) > 1.25F)) {
            if (this.field_70170_p.field_72995_K) {
               this.a(var1, this.ab);
            } else if (this.ab == 25) {
               this.e_clash499(var1.getPersistentID());
               var1.func_191958_b(0.0F, 0.0F, 0.0F, 0.0F);
               var1.func_70634_a(this.func_174791_d().field_72450_a, this.func_174791_d().field_72448_b, this.func_174791_d().field_72449_c);
               this.b(fp.COWGIRL_SITTING_INTRO);
               var1.func_70034_d(this.I_clash415() + 180.0F);
               var1.field_70177_z = this.I_clash415() + 180.0F;
               var1.field_70126_B = this.I_clash415() + 180.0F;
               this.r = this.I_clash415() + 180.0F;
               this.a_clash536(0.0, -0.075F, -0.7109375, 0.0F, 0.0F);
               this.m.func_187227_b(D, 0);
            }

            this.ab++;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void a(EntityPlayer var1, int var2) {
      if (var2 == 0) {
         EntityPlayerSP var3 = Minecraft.func_71410_x().field_71439_g;
         if (var3.getPersistentID().equals(var1.getPersistentID())) {
            BeeScreen.b_clash732();
            var3.func_70016_h(0.0, 0.0, 0.0);
            d3.a_clash122(false);
         }
      }

      if (var2 == 25) {
         EntityPlayerSP var4 = Minecraft.func_71410_x().field_71439_g;
         if (var4.getPersistentID().equals(var1.getPersistentID())) {
            Minecraft.func_71410_x().field_71474_y.field_74320_O = 2;
         }
      }
   }

   @Override
   public void a_clash292() {
      this.m.func_187227_b(G, false);
      this.b(fp.NULL);
      this.ar = true;
      BlockPos var1 = this.a_clash525(this.func_180425_c());
      if (var1 == null) {
         this.a(SoundHandler.GIRLS_LUNA_GIGGLE);
         PacketHandler.b
            .sendToAllAround(
               new SendChatMessagePacket(
                  "<" + this.c_clash241() + "> Heh.. there is no bed nearby.. but I already ate the fish so nya~ hehe", this.field_71093_bK, this.f_clash491()
               ),
               this.P_clash535()
            );
      } else {
         Vec3d var2 = new Vec3d(var1.func_177958_n(), var1.func_177956_o(), var1.func_177952_p());
         int[] var3 = new int[]{0, 180, -90, 90};
         Vec3d[][] var4 = new Vec3d[][]{
            {new Vec3d(0.5, 0.0, -0.5), new Vec3d(0.0, 0.0, -1.0)},
            {new Vec3d(0.5, 0.0, 1.5), new Vec3d(0.0, 0.0, 1.0)},
            {new Vec3d(-0.5, 0.0, 0.5), new Vec3d(-1.0, 0.0, 0.0)},
            {new Vec3d(1.5, 0.0, 0.5), new Vec3d(1.0, 0.0, 0.0)}
         };
         int var5 = -1;

         for (int var6 = 0; var6 < var4.length; var6++) {
            Vec3d var7 = var2.func_178787_e(var4[var6][1]);
            if (this.field_70170_p.func_180495_p(new BlockPos(var7.field_72450_a, var7.field_72448_b, var7.field_72449_c)).func_177230_c()
               == Blocks.field_150350_a) {
               if (var5 == -1) {
                  var5 = var6;
               } else {
                  double var8 = this.func_180425_c()
                     .func_177954_c(
                        var2.func_178787_e(var4[var5][0]).field_72450_a,
                        var2.func_178787_e(var4[var5][0]).field_72448_b,
                        var2.func_178787_e(var4[var5][0]).field_72449_c
                     );
                  double var10 = this.func_180425_c()
                     .func_177954_c(
                        var2.func_178787_e(var4[var6][0]).field_72450_a,
                        var2.func_178787_e(var4[var6][0]).field_72448_b,
                        var2.func_178787_e(var4[var6][0]).field_72449_c
                     );
                  if (var10 < var8) {
                     var5 = var6;
                  }
               }
            }
         }

         if (var5 == -1) {
            this.a(SoundHandler.GIRLS_LUNA_GIGGLE);
            this.a_clash541("Heh.. the bed is obscured.. but I already ate the fish so nya~ hehe");
            return;
         }

         Vec3d var12 = var2.func_178787_e(var4[var5][0]);
         this.b_clash431(var3[var5]);
         this.c_clash502(new Vec3d(var12.field_72450_a, var12.field_72448_b, var12.field_72449_c));
         this.r = this.I_clash415();
         this.func_70661_as().func_75499_g();
         this.func_70661_as().func_75492_a(var12.field_72450_a, var12.field_72448_b, var12.field_72449_c, 0.2);
         this.ay = true;
         this.ak = 0;
      }
   }

   public void j_clash386() {
      EntityItem var1 = new EntityItem(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, (ItemStack)this.m.func_187225_a(ag));
      Vec3d var2 = ck.a_clash306(new Vec3d(0.0, 0.2F + Math.random() * 0.1F, -0.2F + Math.random() * -0.1F), this.field_70177_z);
      var1.field_70159_w = var2.field_72450_a;
      var1.field_70181_x = var2.field_72448_b;
      var1.field_70179_y = var2.field_72449_c;
      this.field_70170_p.func_72838_d(var1);
      this.m.func_187227_b(ag, ItemStack.field_190927_a);
   }

   public void q_clash387() {
      this.ai = null;
      this.at = 0;
      this.as = 0;
      this.am = false;
      this.m.func_187227_b(G, false);
      this.m.func_187227_b(ag, ItemStack.field_190927_a);
      this.func_174810_b(false);
      this.b(fp.NULL);
      if (this.av != null) {
         this.field_70170_p.func_72900_e(this.av);
         this.av = null;
      }

      if (this.ae_clash498() == null) {
         this.o = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
         this.field_70714_bg.func_75776_a(5, this.o);
         if (!this.J_clash526()) {
            this.z = new EntityAIWanderAvoidWater(this, 0.35);
            this.field_70714_bg.func_75776_a(5, this.z);
         }
      }
   }

   public void h_clash388() {
      this.q_clash387();
      if (++this.aq >= 3) {
         this.aq = 0;
         this.aj = 0;
      }
   }

   void i_clash389() {
      if (!this.J_clash526() && this.ae_clash498() == null && !this.ar) {
         if (!(++this.aj < 1200.0F)) {
            if (this.av != null && this.av.d == 15) {
               ((LunaRodItem)this.ao.func_77973_b()).a(this.field_70170_p, this, EnumHand.MAIN_HAND);
               this.al = this.field_70170_p.func_82737_E() + 20L;
               ItemStack var1 = (ItemStack)this.m.func_187225_a(ag);
               if (var1 != ItemStack.field_190927_a) {
                  if (var1.func_77973_b() instanceof ItemFood) {
                     this.b(fp.FISHING_EAT);
                  } else {
                     this.b(fp.FISHING_THROW_AWAY);
                  }
               }
            }

            if (!this.y_clash492().toString().toLowerCase().contains("fishing")) {
               this.n_clash392();
               this.e_clash391();
            }

            if (this.ai != null && this.au == null && this.func_70661_as().func_75505_d() == null && !this.field_70171_ac && this.field_70122_E) {
               this.field_70170_p
                  .func_72901_a(
                     this.func_174791_d().func_72441_c(0.0, this.func_70047_e(), 0.0),
                     new Vec3d(this.ai.func_177958_n(), this.ai.func_177956_o(), this.ai.func_177952_p()),
                     true
                  );
               this.func_174810_b(true);
               if (this.z != null) {
                  this.field_70714_bg.func_85156_a(this.z);
                  this.z = null;
               }

               if (this.o != null) {
                  this.field_70714_bg.func_85156_a(this.o);
                  this.o = null;
               }

               if (this.y_clash492() == fp.NULL) {
                  this.b(fp.FISHING_START);
                  this.c_clash502(this.func_174791_d());
                  this.m.func_187227_b(G, true);
                  this.b_clash431(
                     (float)Math.atan2(this.field_70161_v - this.ai.func_177952_p(), this.field_70165_t - this.ai.func_177958_n()) * (float) (180.0 / Math.PI)
                        + 90.0F
                  );
               }
            } else {
               this.au = this.func_70661_as().func_75505_d();
            }
         }
      } else {
         if ((Boolean)this.m.func_187225_a(af)) {
            this.q_clash387();
         }
      }
   }

   public void o_clash390() {
      this.an.add(this.ai);
      this.q_clash387();
   }

   void e_clash391() {
      if (this.ai != null) {
         PathNavigate var1 = this.func_70661_as();
         var1.func_75492_a(this.ai.func_177958_n(), this.ai.func_177956_o(), this.ai.func_177952_p(), 0.35F);
         Path var2 = var1.func_75505_d();
         if (var2 != null) {
            if (var2.func_75874_d() > var2.func_75873_e() + 1) {
               PathPoint var3 = var2.func_75877_a(var2.func_75873_e() + 1);
               PathPoint var4 = var2.func_75877_a(var2.func_75874_d() - 1);
               Vec3d var5 = new Vec3d(var4.field_75839_a, var4.field_75837_b, var4.field_75838_c);
               BlockPos var6 = new BlockPos(var3.field_75839_a, var3.field_75837_b, var3.field_75838_c);
               if (this.func_174791_d().func_72438_d(var5) < 0.75) {
                  var1.func_75499_g();
                  this.func_70107_b(var5.field_72450_a, var5.field_72448_b, var5.field_72449_c);
               }

               if (this.field_70170_p.func_180495_p(var6.func_177982_a(0, 1, 0)).func_177230_c() == Blocks.field_150355_j) {
                  var1.func_75499_g();
               }

               if (this.field_70170_p.func_180495_p(var6).func_177230_c() == Blocks.field_150355_j) {
                  var1.func_75499_g();
               }

               if (this.field_70170_p.func_180495_p(var6.func_177982_a(0, -1, 0)).func_177230_c() == Blocks.field_150355_j) {
                  var1.func_75499_g();
               }
            }
         }
      }
   }

   void n_clash392() {
      int var1 = 0;
      BlockPos var2 = null;
      int var3 = 0;

      while (++var1 < 50) {
         BlockPos var4 = this.a(
            this.func_180425_c(),
            var1 + 1,
            Blocks.field_150355_j,
            60,
            10,
            new HashSet<>(
               Arrays.asList(
                  Biomes.field_76781_i,
                  Biomes.field_76771_b,
                  Biomes.field_150575_M,
                  Biomes.field_76787_r,
                  Biomes.field_150576_N,
                  Biomes.field_76780_h,
                  Biomes.field_150599_m
               )
            )
         );
         if (var4 == null) {
            break;
         }

         while (this.field_70170_p.func_180495_p(var4.func_177982_a(0, 1, 0)).func_177230_c() == Blocks.field_150355_j) {
            var4 = var4.func_177982_a(0, 1, 0);
         }

         int var5 = 1;

         for (BlockPos var6 = var4; this.field_70170_p.func_180495_p(var6.func_177982_a(0, -1, 0)).func_177230_c() == Blocks.field_150355_j; var5++) {
            var6 = var6.func_177982_a(0, -1, 0);
         }

         if (!this.an.contains(var4)) {
            if (var2 == null) {
               var2 = var4;
               var3 = var5;
            } else if (var5 > var3) {
               var2 = var4;
               var3 = var5;
               if (var3 >= 6) {
                  break;
               }
            }
         }
      }

      if (var2 != null) {
         if (this.ai == null || this.at < var3) {
            this.ai = var2;
            this.at = var3;
         }

         if (this.ai.equals(var2)) {
            this.as = 0;
         } else if (++this.as > 20) {
            this.ai = var2;
            this.at = var3;
         }
      }
   }

   void m_clash393() {
      Path var1 = this.func_70661_as().func_75505_d();
      if (var1 != null) {
         PathPoint var2 = var1.func_75870_c();
         PathPoint var3 = new PathPoint(
            ThreadNames.a_clash169(this.field_70165_t), ThreadNames.a_clash169(this.field_70163_u), ThreadNames.a_clash169(this.field_70161_v)
         );
         if (var2 != null) {
            this.m.func_187227_b(Y, var2.func_75829_a(var3));
         }
      }
   }

   @Override
   public void a(String var1, UUID var2) {
      super.a(var1, var2);
      if ("action.names.touchboobs".equals(var1)) {
         this.e_clash499(var2);
         this.a(true, true, var2);
         this.a_clash490("animationFollowUp", "touch_boobs");
         this.a_clash490("currentModel", "0");
         d3.a_clash122(false);
      }

      if ("action.names.sex".equals(var1)) {
         this.e_clash499(var2);
         this.a(true, true, var2);
         this.a_clash490("animationFollowUp", "sex");
         d3.a_clash122(false);
      }

      if ("action.names.headpat".equals(var1)) {
         this.e_clash499(var2);
         this.a(true, true, var2);
         d3.a_clash122(false);
         this.a_clash490("animationFollowUp", "headpat");
      }
   }

   @Override
   protected fp c_clash235(fp var1) {
      if (var1 == fp.TOUCH_BOOBS_SLOW) {
         return fp.TOUCH_BOOBS_FAST;
      } else {
         return var1 == fp.COWGIRL_SITTING_SLOW ? fp.COWGIRL_SITTING_FAST : null;
      }
   }

   @Override
   protected fp a_clash236(fp var1) {
      if (var1 == fp.TOUCH_BOOBS_SLOW || var1 == fp.TOUCH_BOOBS_FAST) {
         return fp.TOUCH_BOOBS_CUM;
      } else {
         return var1 != fp.COWGIRL_SITTING_FAST && var1 != fp.COWGIRL_SITTING_SLOW ? null : fp.COWGIRL_SITTING_CUM;
      }
   }

   @Override
   protected void U() {
      switch ((String)this.m.func_187225_a(h)) {
         case "touch_boobs":
            if (this.y_clash492() != fp.PAYMENT) {
               this.b(fp.PAYMENT);
               return;
            }

            this.b(fp.TOUCH_BOOBS_INTRO);
            break;
         case "sex":
            if (this.y_clash492() != fp.PAYMENT) {
               this.b(fp.PAYMENT);
            } else {
               PacketHandler.b.sendToServer(new SendGirlToSexPacket(this.f_clash491()));
               PacketHandler.b.sendToServer(new ResetGirlPacket(this.f_clash491()));
            }

            return;
         case "headpat":
            this.b(fp.HEAD_PAT);
      }

      if (this.field_70170_p.field_72995_K) {
         this.a_clash490("animationFollowUp", "");
      } else {
         this.m.func_187227_b(h, "");
      }
   }

   protected void func_184581_c(DamageSource var1) {
      this.a(SoundHandler.GIRLS_LUNA_OUU);
   }

   @Nullable
   protected SoundEvent func_184615_bR() {
      return this.func_70681_au().nextFloat() * 100.0F > 95.0F ? SoundHandler.GIRLS_ALLIE_SCAWY[2] : SoundHandler.GIRLS_LUNA_OUU[12];
   }

   @Override
   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(1.0);
   }

   protected float func_175134_bD() {
      return this.func_70090_H() ? 1.0F : 0.5F;
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      if (this.field_70170_p instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.y_clash492() != fp.NULL) {
               this.a("animation.cat.null", true, var1);
            } else {
               this.a("animation.cat.blink", true, var1);
            }
            break;
         case "movement":
            if (this.y_clash492() != fp.NULL) {
               this.a("animation.cat.null", true, var1);
            } else if (this.func_184218_aH()) {
               this.a("animation.cat.sit", true, var1);
            } else if (Math.abs(this.field_70169_q - this.field_70165_t) + Math.abs(this.field_70166_s - this.field_70161_v) > 0.0) {
               if (this.field_70122_E && Math.abs(Math.abs(this.field_70167_r) - Math.abs(this.field_70163_u)) < 0.1F) {
                  this.a(this.m.func_187225_a(Y) < 3.0F ? "animation.cat.walk" : "animation.cat.run", true, var1);
               } else {
                  this.a("animation.cat.fly", true, var1);
               }

               this.field_70177_z = this.field_70759_as;
            } else {
               this.a("animation.cat.idle" + (this.ad ? "2" : ""), true, var1);
            }
            break;
         case "action":
            switch (this.y_clash492()) {
               case NULL:
                  this.a("animation.cat.null", true, var1);
                  break;
               case ATTACK:
                  this.a("animation.cat.attack" + this.S, false, var1);
                  break;
               case RIDE:
               case SIT:
                  this.a("animation.cat.sit", true, var1);
                  break;
               case BOW:
                  this.a("animation.cat.bowcharge", false, var1);
                  break;
               case THROW_PEARL:
                  this.a("animation.cat.throwpearl", true, var1);
                  break;
               case DOWNED:
                  this.a("animation.cat.downed", true, var1);
                  break;
               case FISHING_START:
                  this.a("animation.cat.start_fishing", false, var1);
                  break;
               case FISHING_IDLE:
                  this.a("animation.cat.idle_fishing", true, var1);
                  break;
               case FISHING_EAT:
                  this.a("animation.cat.eat_fishing", false, var1);
                  break;
               case FISHING_THROW_AWAY:
                  this.a("animation.cat.throw_away", false, var1);
                  break;
               case PAYMENT:
                  this.a("animation.cat.payment", false, var1);
                  break;
               case TOUCH_BOOBS_INTRO:
                  this.a("animation.cat.touch_boobs_intro", false, var1);
                  break;
               case TOUCH_BOOBS_SLOW:
                  this.a("animation.cat.touch_boobs_slow" + (this.ae ? "1" : ""), true, var1);
                  break;
               case TOUCH_BOOBS_FAST:
                  this.a("animation.cat.touch_boobs_fast", true, var1);
                  break;
               case TOUCH_BOOBS_CUM:
                  this.a("animation.cat.touch_boobs_cum", false, var1);
                  break;
               case WAIT_CAT:
                  this.a("animation.cat.wait", false, var1);
                  break;
               case COWGIRL_SITTING_INTRO:
                  this.a("animation.cat.sitting_intro", false, var1);
                  break;
               case COWGIRL_SITTING_SLOW:
                  this.a("animation.cat.sitting_slow", true, var1);
                  break;
               case COWGIRL_SITTING_FAST:
                  this.a("animation.cat.sitting_fast", true, var1);
                  break;
               case COWGIRL_SITTING_CUM:
                  this.a("animation.cat.sitting_cum", false, var1);
                  break;
               case HEAD_PAT:
                  this.a("animation.cat.head_pat", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

   @Override
   public void registerControllers(AnimationData var1) {
      if (this.C == null) {
         this.p_clash506();
      }

      AnimationController.ISoundListener var2 = var1x -> {
         switch (var1x.sound) {
            case "attackSound":
               this.a(SoundEvents.field_187727_dV);
               break;
            case "attackDone":
               this.b(fp.NULL);
               if (++this.S == 3) {
                  this.S = 0;
               }
               break;
            case "idleDone":
               this.ad = this.func_70681_au().nextInt(10) == 0;
               break;
            case "idle2Done":
               this.ad = false;
               break;
            case "pearl":
               PacketHandler.b.sendToServer(new SendCompanionHomePacket(this.f_clash491()));
               break;
            case "start_fishingDone":
               if (this.e_clash544()) {
                  this.b(fp.FISHING_IDLE);
               }
               break;
            case "rod_shoot":
               if (this.e_clash544()) {
                  PacketHandler.b.sendToServer(new CatActivateFishingPacket(this.f_clash491()));
               }
               break;
            case "eat":
               this.a(
                  SoundHandler.a_clash804(SoundHandler.MISC_EAT),
                  0.5F + 0.5F * this.field_70146_Z.nextInt(2),
                  (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.2F + 1.0F
               );
               this.aa -= 0.33333334F;
               break;
            case "eatPay":
               this.a(
                  SoundHandler.a_clash804(SoundHandler.MISC_EAT),
                  0.5F + 0.5F * this.field_70146_Z.nextInt(2),
                  (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.2F + 1.0F
               );
               this.n -= 0.33333334F;
               break;
            case "burp":
               this.a(SoundEvents.field_187739_dZ, 0.5F, this.field_70146_Z.nextFloat() * 0.1F + 0.9F);
               break;
            case "eatingDone":
               if (this.e_clash544()) {
                  PacketHandler.b.sendToServer(new CatEatingDonePacket(this.f_clash491()));
                  this.b(fp.NULL);
               }

               this.aa = 1.0F;
               this.Z = 0.0F;
               break;
            case "throw_away":
               if (this.e_clash544()) {
                  PacketHandler.b.sendToServer(new CatThrowAwayItemPacket(this.f_clash491()));
               }

               this.aa = 1.0F;
               this.Z = 0.0F;
               break;
            case "renderItem":
               this.Z = 1.0F;
               break;
            case "paymentMSG1":
               this.a(this.ae_clash498(), "Here, I know u like fish and yea.. these are for you");
               this.a(SoundHandler.MISC_PLOB[0]);
               break;
            case "paymentMSG2":
               this.a_clash541("huh~?");
               this.a(SoundHandler.GIRLS_LUNA_HUH);
               break;
            case "paymentMSG3":
               this.a_clash541("nyyyaaaa~ :D");
               int[] var4 = new int[]{1, 7, 10, 11};
               int var5 = var4[this.func_70681_au().nextInt(var4.length)];
               this.a(SoundHandler.GIRLS_LUNA_CUTENYA[var5]);
               break;
            case "paymentMSG4":
               this.a_clash541("tankuuuu owowowo");
               this.a(SoundHandler.GIRLS_LUNA_OWO);
               break;
            case "paymentDone":
               if (this.e_clash544()) {
                  this.U();
               }

               this.n = 1.0F;
               break;
            case "breath":
            case "rod_breath":
               this.a(SoundHandler.GIRLS_LUNA_LIGHTBREATHING);
               break;
            case "happyOh":
               this.a(SoundHandler.GIRLS_LUNA_HAPPYOH);
               break;
            case "cutenya3":
               this.a(SoundHandler.GIRLS_LUNA_CUTENYA[3]);
               break;
            case "cutenya2":
               this.a(SoundHandler.GIRLS_LUNA_CUTENYA[2]);
               break;
            case "huh":
               this.a(SoundHandler.GIRLS_LUNA_HUH);
               break;
            case "hmph":
               this.a(SoundHandler.GIRLS_LUNA_HMPH);
               break;
            case "hehe":
            case "giggle":
               this.a(SoundHandler.GIRLS_LUNA_GIGGLE);
               break;
            case "singing":
               this.a(SoundHandler.GIRLS_LUNA_SINGING);
               break;
            case "touch_boobsMSG1":
               this.a_clash541("comon~ touch me hihi~");
               this.a(SoundHandler.GIRLS_LUNA_GIGGLE);
               break;
            case "touch":
               this.a(SoundHandler.MISC_TOUCH);
               break;
            case "jump":
               this.a(SoundHandler.MISC_JUMP[0], 0.2F);
               break;
            case "horninya":
               this.a(SoundHandler.GIRLS_LUNA_HORNINYA);
               break;
            case "horninya2":
            case "touch_boobs_cumMSG3":
            case "sitting_cumMSG1":
               this.a(SoundHandler.GIRLS_LUNA_HORNINYA[1]);
               this.a(SoundHandler.MISC_CUMINFLATION[0], 5.0F);
               break;
            case "moan":
               this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_LUNA_MOAN));
               break;
            case "touch_boobs_introDone":
               this.b(fp.TOUCH_BOOBS_SLOW);
               if (this.n_clash537()) {
                  HornyMeterHud.b_clash363();
                  HornyMeterHud.d_clash358();
                  d3.a_clash122(false);
               }
               break;
            case "touch_boobs_slowDone":
               if (this.ae) {
                  this.ae = false;
               } else {
                  this.ae = Math.random() < 0.5;
               }
               break;
            case "addCumSlow":
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.02F);
               }
               break;
            case "addCumFast":
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.04F);
               }
               break;
            case "fastDone":
               if (this.n_clash537() && !d3.d) {
                  this.b(fp.TOUCH_BOOBS_SLOW);
               }
               break;
            case "moanOrNya":
               if (Math.random() > 0.5) {
                  this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_LUNA_MOAN));
               } else {
                  this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_LUNA_HORNINYA));
               }
               break;
            case "blackScreen":
               if (this.n_clash537()) {
                  BeeScreen.b_clash732();
               }
               break;
            case "touch_boobs_cumDone":
               if (this.n_clash537()) {
                  HornyMeterHud.b_clash363();
                  this.r_clash533();
               }
               break;
            case "resetGirl":
               if (this.e_clash544()) {
                  this.r_clash533();
               }
               break;
            case "touch_boobs_cumMSG1":
               this.a(SoundHandler.GIRLS_LUNA_HORNINYA[3]);
               break;
            case "touch_boobs_cumMSG2":
               this.a(SoundHandler.GIRLS_LUNA_HORNINYA[9]);
               break;
            case "call_playerMSG1":
               this.a(SoundHandler.GIRLS_LUNA_GIGGLE);
               this.a_clash541("come here - big guy hehe~");
               break;
            case "pounding":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING));
               break;
            case "sitting_introMSG1":
               this.a(SoundHandler.GIRLS_LUNA_GIGGLE);
               this.a_clash541("hehe~");
               break;
            case "sitting_introDone":
               if (this.n_clash537()) {
                  this.b(fp.COWGIRL_SITTING_SLOW);
                  HornyMeterHud.b_clash363();
                  HornyMeterHud.d_clash358();
               }
               break;
            case "sitting_slowMSG1":
               if (this.func_70681_au().nextBoolean()) {
                  if (this.func_70681_au().nextBoolean()) {
                     this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_LUNA_HORNINYA));
                     break;
                  }

                  this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_LUNA_MOAN));
               } else {
                  this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_LUNA_LIGHTBREATHING));
               }

               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.02);
               }
               break;
            case "sitting_fastMSG1":
               if (this.func_70681_au().nextBoolean()) {
                  this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_LUNA_HORNINYA));
               } else {
                  this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_LUNA_MOAN));
               }

               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.04);
               }
               break;
            case "sitting_fastDone":
               if (this.n_clash537() && !d3.d) {
                  this.b(fp.COWGIRL_SITTING_SLOW);
                  Vec3d var8 = new Vec3d(0.0, -0.075F, -0.7109375);
                  Vec3d var9 = ck.a_clash306(var8, this.I_clash415() + 180.0F);
                  Minecraft.func_71410_x()
                     .field_71439_g
                     .func_70107_b(
                        this.o_clash501().field_72450_a + var9.field_72450_a,
                        this.o_clash501().field_72448_b + var9.field_72448_b,
                        this.o_clash501().field_72449_c + var9.field_72449_c
                     );
               }
               break;
            case "sitting_fastTp":
               if (this.n_clash537()) {
                  Vec3d var6 = new Vec3d(0.0, -0.160625, -0.9925);
                  Vec3d var7 = ck.a_clash306(var6, this.I_clash415() + 180.0F);
                  Minecraft.func_71410_x()
                     .field_71439_g
                     .func_70107_b(
                        this.o_clash501().field_72450_a + var7.field_72450_a,
                        this.o_clash501().field_72448_b + var7.field_72448_b,
                        this.o_clash501().field_72449_c + var7.field_72449_c
                     );
               }
               break;
            case "headpatMSG1":
               this.a_clash541("huh?~");
               this.a(SoundHandler.GIRLS_LUNA_HUH);
               break;
            case "headpatMSG2":
               this.a(SoundHandler.GIRLS_LUNA_MMM);
               break;
            case "headpatMSG3":
               this.a_clash541("nya~");
               this.a(SoundHandler.GIRLS_LUNA_HORNINYA[0]);
         }
      };
      this.E.transitionLengthTicks = 10.0;
      this.C.registerSoundListener(var2);
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.E);
      var1.addAnimationController(this.s);
   }

   @Override
   public void func_70037_a(NBTTagCompound var1) {
      super.func_70037_a(var1);
      this.func_189654_d(false);
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }

   public static class a {
      @SubscribeEvent
      public void a(EntityJoinWorldEvent var1) {
         Entity var2 = var1.getEntity();
         if (var2 instanceof EntityCreeper) {
            EntityCreeper var3 = (EntityCreeper)var2;
            var3.field_70714_bg.func_75776_a(3, new EntityAIAvoidEntity(var3, LunaEntity.class, 6.0F, 1.0, 1.2));
         }
      }
   }
}
