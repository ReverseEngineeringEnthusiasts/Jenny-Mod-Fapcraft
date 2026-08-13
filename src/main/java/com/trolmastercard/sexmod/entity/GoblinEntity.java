package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.api.ao;
import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.api.by;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.GalathScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.entity.ai.DoorInteractAiGoal;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ad;
import com.trolmastercard.sexmod.util.af;
import com.trolmastercard.sexmod.util.ah;
import com.trolmastercard.sexmod.util.ak;
import com.trolmastercard.sexmod.util.am;
import com.trolmastercard.sexmod.util.an;
import com.trolmastercard.sexmod.util.cj;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.e1;
import com.trolmastercard.sexmod.util.eh;
import com.trolmastercard.sexmod.util.g5;







import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector2f;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class GoblinEntity extends AbstractNpcOnlyEntity implements IGoblin {
   public static final by ax = by.DARK_GREEN;
   public static final Vec3i ah = new Vec3i(11, 6, 11);
   public static final Vec3d aB = new Vec3d(5.0, 1.0, 9.0);
   public static final Vec3d af = new Vec3d(3.0, -1.0, 6.0);
   public static final Vec3d ao = new Vec3d(1.0, 1.0, 5.0);
   public static final Vec3d au = new Vec3d(-6.0, -1.0, 3.0);
   public static final Vec3d aM = new Vec3d(5.0, 1.0, 1.0);
   public static final Vec3d W = new Vec3d(-3.0, -1.0, -6.0);
   public static final Vec3d U = new Vec3d(9.0, 1.0, 5.0);
   public static final Vec3d as = new Vec3d(0.0, -1.0, -4.0);
   public static final Vec3d aT = new Vec3d(1.0, -1.0, -3.0);
   public static final Vec3d ap = new Vec3d(-1.0, -1.0, -3.0);
   public static final Vec3d at = new Vec3d(6.0, -1.0, -3.0);
   public static final int aj = 39;
   public static final int ae = 15;
   public static final int aE = 8400;
   static final int aH = 45;
   static final int ad = 32000;
   static final int aw = 26;
   static final int V = 205;
   static final int aL = 100;
   static final int aA = 1200;
   static final int ak = 30;
   static final int aW = 37;
   static final float aU = 2.0F;
   static final int aI = 5;
   static final int S = 100;
   static final int aq = 20;
   static final float aG = 0.825F;
   static final Vector2f aS = new Vector2f(0.5F, 0.99F);
   static final HashSet<Item> ag = new HashSet<>(
      Arrays.asList(
         Items.field_151013_M,
         Items.field_151136_bY,
         Items.field_151043_k,
         Items.field_151153_ao,
         Items.field_151006_E,
         Items.field_151011_C,
         Items.field_151005_D,
         Items.field_151010_B,
         Items.field_151150_bK,
         Items.field_151169_ag,
         Items.field_151151_aj,
         Items.field_151171_ah,
         Items.field_151149_ai,
         Items.field_151043_k,
         Items.field_151074_bl,
         Item.func_150898_a(Blocks.field_150340_R),
         Item.func_150898_a(Blocks.field_150352_o)
      )
   );
   public static final DataParameter<String> Q = EntityDataManager.func_187226_a(GoblinEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(122);
   public static final DataParameter<String> aK = EntityDataManager.func_187226_a(GoblinEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(123);
   public static final DataParameter<ItemStack> a0 = EntityDataManager.func_187226_a(GoblinEntity.class, DataSerializers.field_187196_f)
      .func_187156_b()
      .func_187161_a(124);
   public static final DataParameter<Boolean> aC = EntityDataManager.func_187226_a(GoblinEntity.class, DataSerializers.field_187198_h)
      .func_187156_b()
      .func_187161_a(125);
   public static final DataParameter<Boolean> aV = EntityDataManager.func_187226_a(GoblinEntity.class, DataSerializers.field_187198_h)
      .func_187156_b()
      .func_187161_a(126);
   public boolean aX = false;
   public float ac = 0.0F;
   public long av = -1L;
   public Vec3d al = Vec3d.field_186680_a;
   List<UUID> T = new ArrayList<>();
   int aO = 31520;
   int aQ = -1;
   public int aR = -1;
   boolean aZ = false;
   BlockPos R = null;
   int Y = 0;
   int aa = 0;
   int aJ = 0;
   int an = -1;
   int am = 0;
   long ai = 0L;
   List<GoblinEntity> ab = new ArrayList<>();
   int aY = -1;
   int az = -1;
   fp aN = null;
   public float ar = 1.0F;
   int Z = -1;
   boolean aD = true;
   boolean aF = true;
   boolean X = false;
   String aP = "";
   boolean ay = false;

   public GoblinEntity(World var1) {
      super(var1);
      this.func_70105_a(aS.x, aS.y);
   }

   public GoblinEntity(World var1, @Nonnull String var2, int var3) {
      this(var1);
      this.m.func_187227_b(aK, var2);
      this.m.func_187227_b(M, this.a_clash247(new StringBuilder(), var3));
   }

   public GoblinEntity(World var1, boolean var2, float var3, Vec3d var4) {
      this(var1);
      if (var2) {
         this.m.func_187227_b(M, this.b_clash242(new StringBuilder()));
         this.ac = var3;
         this.al = var4;
         this.aX = true;
         this.c_clash502(var4);
         this.b_clash431(var3);
         this.b(fp.SIT);
         this.a_clash504(true);
         this.func_70107_b(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c);
      }
   }

   @Override
   public void g_clash238() {
      super.g_clash238();
      this.a_clash55(null);
      this.field_70145_X = false;
      this.func_189654_d(false);
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      eh var1 = eh.values()[this.func_70681_au().nextInt(eh.values().length)];
      this.m.func_187214_a(K, new BlockPos(var1.a_clash565()));
      this.m.func_187214_a(N, ax.name());
      this.m.func_187214_a(Q, "");
      this.m.func_187214_a(aK, "");
      this.m.func_187214_a(a0, ItemStack.field_190927_a);
      this.m.func_187214_a(aC, false);
      this.m.func_187214_a(aV, false);
   }

   @Override
   protected void a_clash222() {
      GoblinRenderer.c_clash214();
   }

   public void func_70106_y() {
      super.func_70106_y();
      this.a_clash55(null);
      if (!this.field_70170_p.field_72995_K) {
         ItemStack var1 = (ItemStack)this.m.func_187225_a(a0);
         if (var1 != ItemStack.field_190927_a) {
            EntityItem var2 = new EntityItem(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, var1);
            this.field_70170_p.func_72838_d(var2);
         }
      }
   }

   @Override
   public void a(String var1, UUID var2) {
      if ("take ur stuff back".equals(var1)) {
         this.b(fp.START_THROWING);
      }

      if ("use her".equals(var1)) {
         this.c_clash239(var2);
      }
   }

   public void c_clash239(UUID var1) {
      this.aY = 0;
      BeeScreen.b_clash732();
      d3.a_clash122(false);
      this.e_clash499(var1);
   }

   public void b_clash240(UUID var1) {
      this.az = 0;
      BeeScreen.b_clash732();
      d3.a_clash122(false);
      this.e_clash499(var1);
   }

   @Override
   public String c_clash241() {
      return "Goblin";
   }

   public float func_70047_e() {
      return 0.75F;
   }

   @Override
   public float i_clash226() {
      return 0.1F;
   }

   @Override
   public void a_clash55(UUID var1) {
      if (var1 == null) {
         this.m.func_187227_b(Q, "");
      } else {
         this.m.func_187227_b(Q, var1.toString());
      }
   }

   @Nullable
   @Override
   public UUID e_clash54() {
      String var1 = (String)this.m.func_187225_a(Q);
      if ("".equals(var1)) {
         return null;
      }

      try {
         return UUID.fromString((String)this.m.func_187225_a(Q));
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   @Override
   public int c_clash56() {
      return this.aQ;
   }

   @Override
   public void b_clash63(int var1) {
      this.aQ = var1;
   }

   protected String b_clash242(StringBuilder var1) {
      a_clash223(var1, 3);
      a_clash223(var1, 2);
      a_clash223(var1, 2);
      c(var1, 7);
      c(var1, 7);
      a_clash223(var1, 5);
      a_clash223(var1, g5.values().length - 1);
      a_clash223(var1, by.values().length - 1);
      a_clash223(var1, eh.values().length - 1);
      c(var1, 1);
      return var1.toString();
   }

   @Override
   protected String a(StringBuilder var1) {
      a_clash223(var1, 3);
      a_clash223(var1, 2);
      a_clash223(var1, 2);
      a_clash223(var1, 8);
      a_clash223(var1, 8);
      a_clash223(var1, 5);
      a_clash223(var1, g5.values().length - 1);
      a_clash223(var1, by.values().length - 1);
      a_clash223(var1, eh.values().length - 1);
      c(var1, 0);
      return var1.toString();
   }

   @Override
   public ArrayList<Integer> D_clash243() {
      return new ArrayList<Integer>() {
         {
            this.add(4);
            this.add(3);
            this.add(3);
            this.add(16);
            this.add(16);
            this.add(6);
            this.add(g5.values().length);
            this.add(by.values().length);
            this.add(eh.values().length);
         }
      };
   }

   @Override
   public List<Integer> u_clash244() {
      return Collections.singletonList(2);
   }

   @Override
   public e1 g(int var1) {
      switch (var1) {
         case 0:
            return new e1(40, 130);
         case 1:
            return new e1(60, 130);
         case 2:
            return new e1(80, 130);
         case 3:
            return new e1(100, 130);
         case 4:
            return new e1(120, 130);
         case 5:
            return new e1(140, 130);
         case 6:
            return new e1(160, 130);
         case 7:
            return new e1(180, 130);
         case 8:
            return new e1(200, 0);
         case 9:
            return new e1(200, 130);
         default:
            return e1.a;
      }
   }

   @Override
   public void a_clash245(List<Integer> var1) {
      StringBuilder var2 = new StringBuilder();

      for (int var4 : var1) {
         c(var2, var4);
      }

      c(var2, Integer.parseInt(a_clash225(this)[9]));
      this.m.func_187227_b(M, var2.toString());
      if (null instanceof ClientProxy) {
         GoblinRenderer.c_clash214();
      }
   }

   void i_clash246() {
      if (this.d != null) {
         StringBuilder var1 = new StringBuilder();

         for (Entry var3 : this.d) {
            int var4 = (Integer)((Entry)var3.getValue()).getValue();
            c(var1, var4);
         }

         c(var1, Integer.parseInt(a_clash225(this)[9]));
         this.m.func_187227_b(M, var1.toString());
         GoblinRenderer.c_clash214();
      }
   }

   protected String a_clash247(StringBuilder var1, int var2) {
      a_clash223(var1, 3);
      a_clash223(var1, 2);
      a_clash223(var1, 2);
      a_clash223(var1, 7);
      a_clash223(var1, 7);
      a_clash223(var1, 5);
      a_clash223(var1, g5.values().length - 1);
      c(var1, var2);
      a_clash223(var1, eh.values().length - 1);
      c(var1, 0);
      return var1.toString();
   }

   @Override
   public void func_70014_b(NBTTagCompound var1) {
      super.func_70014_b(var1);
      var1.func_74778_a("bodyColor", (String)this.m.func_187225_a(N));
      var1.func_74768_a("eyeColorX", ((BlockPos)this.m.func_187225_a(K)).func_177958_n());
      var1.func_74768_a("eyeColorY", ((BlockPos)this.m.func_187225_a(K)).func_177956_o());
      var1.func_74768_a("eyeColorZ", ((BlockPos)this.m.func_187225_a(K)).func_177952_p());
      var1.func_74778_a("model", (String)this.m.func_187225_a(M));
      var1.func_74778_a("girlID", (String)this.m.func_187225_a(u));
      var1.func_74778_a("queen", (String)this.m.func_187225_a(aK));
      var1.func_74757_a("isQueen", this.aX);
      var1.func_74757_a("isTamed", (Boolean)this.m.func_187225_a(aC));
      var1.func_74768_a("robTicks", this.aO);
      if (this.aX) {
         var1.func_74757_a("preggo", (Boolean)this.m.func_187225_a(aV));
         var1.func_74776_a("throneRot", this.ac);
         var1.func_74780_a("thronePosX", this.al.field_72450_a);
         var1.func_74780_a("thronePosY", this.al.field_72448_b);
         var1.func_74780_a("thronePosZ", this.al.field_72449_c);
         var1.func_74772_a("impregnationTick", this.av);

         for (int var2 = 0; var2 < this.T.size(); var2++) {
            var1.func_74778_a("guard" + var2, this.T.get(var2).toString());
         }
      }
   }

   @Override
   public void func_70037_a(NBTTagCompound var1) {
      super.func_70037_a(var1);
      this.aX = var1.func_74767_n("isQueen");
      this.m.func_187227_b(M, var1.func_74779_i("model"));
      this.m.func_187227_b(N, var1.func_74779_i("bodyColor"));
      String[] var2 = a_clash225(this);
      if (Integer.parseInt(var2[3]) > 7 || Integer.parseInt(var2[4]) > 7) {
         this.m.func_187227_b(M, this.a_clash247(new StringBuilder(), this.k_clash270()));
         Main.LOGGER.log(Level.INFO, "updated an old Goblin");
      }

      this.m.func_187227_b(K, new BlockPos(var1.func_74762_e("eyeColorX"), var1.func_74762_e("eyeColorY"), var1.func_74762_e("eyeColorZ")));
      this.m.func_187227_b(u, var1.func_74779_i("girlID"));
      this.m.func_187227_b(aK, var1.func_74779_i("queen"));
      this.m.func_187227_b(aC, var1.func_74767_n("isTamed"));
      this.aO = var1.func_74762_e("robTicks");
      if (this.aX) {
         this.ac = var1.func_74760_g("throneRot");
         this.al = new Vec3d(var1.func_74769_h("thronePosX"), var1.func_74769_h("thronePosY"), var1.func_74769_h("thronePosZ"));

         for (int var3 = 0; !"".equals(var1.func_74779_i("guard" + var3)); var3++) {
            this.T.add(UUID.fromString(var1.func_74779_i("guard" + var3)));
         }

         this.m.func_187227_b(aV, var1.func_74767_n("preggo"));
         this.av = var1.func_74763_f("impregnationTick");
      }
   }

   protected boolean func_184645_a(EntityPlayer var1, EnumHand var2) {
      if (this.field_70170_p.field_72995_K) {
         return true;
      }

      if (this.aX) {
         return true;
      }

      if (this.y_clash492() == fp.RUN) {
         if (this.func_70032_d(var1) > 3.5) {
            var1.func_146105_b(new TextComponentString("get a bit closer..."), true);
         } else {
            this.c_clash502(var1.func_174791_d());
            this.b_clash431(var1.field_70177_z);
            this.b(fp.CATCH);
            this.m.func_187227_b(h, "bj");
            this.a_clash55(var1.getPersistentID());
            this.e_clash499(var1.getPersistentID());
            this.func_70661_as().func_75499_g();
            this.field_70159_w = 0.0;
            this.field_70181_x = 0.0;
            this.field_70179_y = 0.0;
         }

         return true;
      } else {
         if (d_clash248(var1.getPersistentID())) {
            var1.func_146105_b(new TextComponentString("you are already carrying a Goblin"), true);
         } else {
            this.a_clash55(var1.getPersistentID());
            this.b(fp.PICK_UP);
            this.aQ = 45;
            this.a_clash504(false);
            this.m.func_187227_b(aC, true);
            this.func_70661_as().func_75499_g();
         }

         return true;
      }
   }

   public static boolean d_clash248(UUID var0) {
      if (var0 == null) {
         return false;
      }

      try {
         for (BaseGirlEntity var2 : BaseGirlEntity.ad_clash509()) {
            if (var2 instanceof IGoblin && !var2.field_70170_p.field_72995_K && !var2.field_70128_L) {
               UUID var3 = ((IGoblin)var2).e_clash54();
               if (var0.equals(var3)) {
                  return true;
               }
            }
         }
      } catch (ConcurrentModificationException var4) {
      }

      return false;
   }

   @Override
   protected void func_184651_r() {
      this.o = new WatchClosestGirlGoal(this, EntityPlayer.class, 2.0F, 1.0F);
      this.field_70714_bg.func_75776_a(0, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(3, new DoorInteractAiGoal(this));
      this.field_70714_bg.func_75776_a(5, this.o);
   }

   @Override
   public void func_70619_bc() {
      super.func_70619_bc();
      this.f_clash262();
      a_clash282(this);
      this.m_clash272();
      this.B_clash269();
      this.J_clash267();
      this.E_clash260();
      this.t_clash258();
      this.w_clash257();
      this.b_clash256();
      this.d_clash255();
      this.h_clash254();
      this.o_clash253();
      this.u_clash252();
      this.n_clash250();
   }

   public boolean func_70067_L() {
      fp var1 = this.y_clash492();
      if (var1 == fp.THROWN) {
         return false;
      } else if (var1 == fp.RUN) {
         return super.func_70067_L();
      } else if (var1 == fp.AWAIT_PICK_UP) {
         return super.func_70067_L();
      } else if (this.e_clash54() != null) {
         return false;
      } else {
         return var1 != fp.NULL ? false : super.func_70067_L();
      }
   }

   void b_clash249(EntityPlayer var1) {
      AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.d_clash567(var1.getPersistentID());
      Vec3d var3 = new Vec3d(var1.field_70165_t, var1.field_70163_u + (var2 == null ? var1.eyeHeight : var2.func_70047_e()), var1.field_70161_v);
      Vec3d var4 = new Vec3d(this.field_70165_t, this.field_70163_u + this.func_70047_e(), this.field_70161_v);
      double var5 = var4.func_72438_d(var3);
      double var7 = var3.field_72448_b - var4.field_72448_b;
      this.field_70125_A = (float)(-(Math.sin(var7 / var5) * (180.0 / Math.PI)));
   }

   void n_clash250() {
      if ((Boolean)this.m.func_187225_a(aC)) {
         if (this.ae_clash498() == null) {
            if (this.y_clash492() == fp.NULL) {
               EntityPlayer var1 = this.field_70170_p.func_72890_a(this, 15.0);
               if (var1 != null && var1.func_70032_d(this) < 2.0F) {
                  this.b_clash249(var1);
                  this.func_70661_as().func_75499_g();
               } else {
                  if (this.R == null
                     || this.func_70011_f(this.R.func_177958_n(), this.R.func_177956_o(), this.R.func_177952_p()) > this.l_clash251()
                     || this.Y > 100) {
                     int var2 = (this.func_70681_au().nextBoolean() ? 1 : -1) * this.func_70681_au().nextInt(5);
                     int var3 = (this.func_70681_au().nextBoolean() ? 1 : -1) * this.func_70681_au().nextInt(5);
                     int var4 = cj.a(this.field_70170_p, this.func_180425_c().func_177958_n() + var2, this.func_180425_c().func_177952_p() + var3);
                     this.R = new BlockPos(this.func_180425_c().func_177958_n() + var2, var4, this.func_180425_c().func_177952_p() + var3);
                     this.Y = 0;
                  }

                  if (Math.sqrt(this.R.func_177951_i(this.func_180425_c())) > 2.0) {
                     this.func_70661_as().func_75492_a(this.R.func_177958_n(), this.R.func_177956_o(), this.R.func_177952_p(), 0.3F);
                     this.k_clash515();
                  } else {
                     this.Y++;
                  }
               }
            }
         }
      }
   }

   double l_clash251() {
      return Math.sqrt(800.0);
   }

   void u_clash252() {
      if (this.y_clash492() == fp.STAND_UP) {
         if (++this.aa >= 37) {
            this.aa = 0;
            this.b(fp.NULL);
         }
      }
   }

   @Override
   public void a_clash59(int var1) {
      this.aJ = var1;
   }

   @Override
   public int d_clash60() {
      return this.aJ;
   }

   void o_clash253() {
      if (this.y_clash492() == fp.THROWN) {
         if (this.field_70122_E) {
            int var1 = this.d_clash60() + 1;
            this.a_clash59(var1);
            if (var1 >= 30) {
               this.a_clash59(0);
               this.b(fp.STAND_UP);
            }
         }
      }
   }

   void h_clash254() {
      if (this.aX) {
         if ((Boolean)this.m.func_187225_a(aV)) {
            if (this.av + 8400L < this.field_70170_p.func_82737_E()) {
               this.m.func_187227_b(aV, false);
            }
         }
      }
   }

   void d_clash255() {
      if (this.aX) {
         if (!this.ab.isEmpty()) {
            boolean var1 = false;

            for (GoblinEntity var3 : this.ab) {
               if ((Boolean)var3.func_184212_Q().func_187225_a(aC)) {
                  var1 = true;
               }
            }

            if (var1) {
               this.h("Farewell my knight. You are welcome once I am breedable again.");

               for (GoblinEntity var5 : this.ab) {
                  if (!(Boolean)var5.func_184212_Q().func_187225_a(aC)) {
                     var5.b(fp.VANISH);
                  }
               }

               this.ab.clear();
               this.e_clash499(null);
            }
         }
      }
   }

   void b_clash256() {
      if (this.aX) {
         if (this.Z != -1) {
            if (++this.Z >= 100) {
               this.Z = -1;
               UUID var1 = this.ae_clash498();
               if (var1 == null) {
                  this.r_clash533();
               } else {
                  EntityPlayer var2 = this.field_70170_p.func_152378_a(var1);
                  if (var2 == null) {
                     this.r_clash533();
                  } else {
                     this.e_clash499(null);

                     for (GoblinEntity var4 : this.ab) {
                        var4.e_clash499(null);
                     }

                     List var10 = this.I_clash261();
                     float var11 = this.ac + 180.0F;
                     Vec3d var5 = this.al.func_178787_e(b(aT, var11));
                     Vec3d var6 = this.al.func_178787_e(b(ap, var11));
                     Vec3d var7 = this.al.func_178787_e(b(as, var11));
                     GoblinEntity var8 = (GoblinEntity)var10.get(0);
                     GoblinEntity var9 = (GoblinEntity)var10.get(1);
                     var8.c_clash502(var5);
                     var9.c_clash502(var6);
                     var8.b_clash431(0.0F);
                     var9.b_clash431(0.0F);
                     var8.a_clash504(true);
                     var9.a_clash504(true);
                     var8.b(fp.AWAIT_PICK_UP);
                     var9.b(fp.AWAIT_PICK_UP);
                     var8.func_189654_d(false);
                     var9.func_189654_d(false);
                     var2.func_189654_d(false);
                     var8.field_70145_X = false;
                     var9.field_70145_X = false;
                     var2.field_70145_X = false;
                     var2.field_70177_z = var11;
                     var2.field_70125_A = 30.0F;
                     var2.func_70634_a(var7.field_72450_a, var7.field_72448_b, var7.field_72449_c);
                     PacketHandler.b.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)var2);
                     this.h(
                        "Thanks to you, my clan is soon going to get a few new members! In return I will bear of one of my guards to serve as your personal Onahole. Choose wisely~"
                     );
                  }
               }
            }
         }
      }
   }

   void w_clash257() {
      if (this.aX) {
         if (this.an != -1) {
            if (++this.an >= 205) {
               this.an = -1;
               UUID var1 = this.ae_clash498();
               if (var1 != null) {
                  EntityPlayer var2 = this.field_70170_p.func_152378_a(var1);
                  if (var2 != null) {
                     Vec3d var3 = b(new Vec3d(0.0, 0.15625 - var2.func_70047_e(), -0.8859375), this.ac - 180.0F);
                     var3 = var3.func_178787_e(this.o_clash501());
                     var2.func_70634_a(var3.field_72450_a, var3.field_72448_b, var3.field_72449_c);
                  }
               }
            }
         }
      }
   }

   public static Vec3d b(Vec3d var0, float var1) {
      return a(var0, 0.0F, var1);
   }

   public static Vec3d a(Vec3d var0, float var1, float var2) {
      Vec3d var3 = new Vec3d(
         var0.field_72450_a,
         var0.field_72448_b * Math.cos(var1 * (Math.PI / 180.0)) - var0.field_72449_c * Math.sin(var1 * (Math.PI / 180.0)),
         var0.field_72448_b * Math.sin(var1 * (Math.PI / 180.0)) + var0.field_72449_c * Math.cos(var1 * (Math.PI / 180.0))
      );
      return new Vec3d(
         -Math.sin((var2 + 90.0F) * (Math.PI / 180.0)) * var3.field_72450_a - Math.sin(var2 * (Math.PI / 180.0)) * var3.field_72449_c,
         var3.field_72448_b,
         Math.cos((var2 + 90.0F) * (Math.PI / 180.0)) * var3.field_72450_a + Math.cos(var2 * (Math.PI / 180.0)) * var3.field_72449_c
      );
   }

   void t_clash258() {
      if (this.aX) {
         if (this.y_clash492() == fp.JUMP_0) {
            if (++this.am >= 26) {
               this.am = 0;
               Vec3d var1;
               switch ((int)this.ac) {
                  case -90:
                     var1 = this.al.func_178787_e(at);
                     break;
                  case 90:
                     var1 = this.al.func_178787_e(au);
                     break;
                  case 180:
                     var1 = this.al.func_178787_e(W);
                     break;
                  default:
                     var1 = this.al.func_178787_e(af);
               }

               UUID var2 = this.ae_clash498();
               if (var2 != null) {
                  EntityPlayer var3 = this.field_70170_p.func_152378_a(var2);
                  if (var3 != null) {
                     this.c_clash502(var1);
                     this.b_clash431(this.ac);
                     this.b(fp.BREEDING_INTRO_0);
                     this.field_70145_X = true;
                     this.func_189654_d(true);
                     Vec3d var4 = b(new Vec3d(0.0, 0.44375 - var3.eyeHeight, -0.7875), this.ac - 180.0F);
                     var3.field_70145_X = true;
                     var3.func_189654_d(true);
                     var3.func_70634_a(
                        var4.field_72450_a + var1.field_72450_a, var4.field_72448_b + var1.field_72448_b, var4.field_72449_c + var1.field_72449_c
                     );
                     List var5 = this.I_clash261();
                     if (var5.size() >= 1) {
                        GoblinEntity var6 = (GoblinEntity)var5.get(0);
                        var6.c_clash502(var1);
                        var6.b_clash431(this.ac);
                        var6.b(fp.BREEDING_INTRO_1);
                        var6.field_70145_X = true;
                        var6.func_189654_d(true);
                     }

                     if (var5.size() >= 2) {
                        GoblinEntity var7 = (GoblinEntity)var5.get(1);
                        var7.c_clash502(var1);
                        var7.b_clash431(this.ac);
                        var7.b(fp.BREEDING_INTRO_2);
                        var7.field_70145_X = true;
                        var7.func_189654_d(true);
                     }

                     this.an = 0;
                  }
               }
            }
         }
      }
   }

   AxisAlignedBB a_clash259(Vec3d var1, Vec3d var2) {
      return new AxisAlignedBB(var1.field_72450_a, var1.field_72448_b, var1.field_72449_c, var2.field_72450_a, var2.field_72448_b, var2.field_72449_c);
   }

   void E_clash260() {
      if (this.aX) {
         if (this.ae_clash498() == null) {
            Vec3d var1 = null;
            switch ((int)this.ac) {
               case -90:
                  var1 = ao;
                  break;
               case 0:
                  var1 = aM;
                  break;
               case 90:
                  var1 = U;
                  break;
               case 180:
                  var1 = aB;
            }

            if (var1 != null) {
               Vec3d var2 = this.al.func_178786_a(0.5, 0.0, 0.5).func_178788_d(var1);
               AxisAlignedBB var3 = this.a_clash259(var2, var2.func_72441_c(ah.func_177958_n(), ah.func_177956_o(), ah.func_177952_p()));
               List var4 = this.field_70170_p.func_72872_a(EntityPlayer.class, var3);
               if (!var4.isEmpty()) {
                  EntityPlayer var5 = (EntityPlayer)var4.get(0);
                  if (var5.field_70122_E) {
                     if ((Boolean)this.m.func_187225_a(aV)) {
                        if (this.ai + 1200L < this.field_70170_p.func_82737_E()) {
                           var5.func_146105_b(new TextComponentString("The Queen is still pregnant - so no breeding for you uwu"), true);
                           this.ai = this.field_70170_p.func_82737_E();
                        }
                     } else {
                        UUID var6 = var5.getPersistentID();
                        Vec3d var7 = var5.func_174791_d();
                        float var8 = var5.field_70177_z + 180.0F;
                        PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var5);
                        this.e_clash499(var6);
                        this.b(fp.JUMP_0);
                        this.c_clash502(var7);
                        this.b_clash431(var8);
                        this.a_clash504(true);
                        List var9 = this.I_clash261();
                        if (var9.size() > 0) {
                           GoblinEntity var10 = (GoblinEntity)var9.get(0);
                           var10.e_clash499(var6);
                           var10.b(fp.JUMP_1);
                           var10.c_clash502(var7);
                           var10.b_clash431(var8);
                           var10.a_clash504(true);
                           if (var9.size() > 1) {
                              GoblinEntity var11 = (GoblinEntity)var9.get(1);
                              var11.e_clash499(var6);
                              var11.b(fp.JUMP_2);
                              var11.c_clash502(var7);
                              var11.b_clash431(var8);
                              var11.a_clash504(true);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   List<GoblinEntity> I_clash261() {
      if (this.ab.size() > 1) {
         return this.ab;
      }

      for (GoblinEntity var2 : this.ab) {
         this.field_70170_p.func_72900_e(var2);
      }

      this.ab.clear();
      GoblinEntity var3 = new GoblinEntity(this.field_70170_p, this.f_clash491().toString(), this.k_clash270());
      var3.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
      this.field_70170_p.func_72838_d(var3);
      this.ab.add(var3);
      GoblinEntity var4 = new GoblinEntity(this.field_70170_p, this.f_clash491().toString(), this.k_clash270());
      var4.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
      this.field_70170_p.func_72838_d(var4);
      this.ab.add(var4);
      return this.ab;
   }

   void f_clash262() {
      if (!this.aZ) {
         this.field_70145_X = false;
         this.func_189654_d(false);
         if (!this.aX && !(Boolean)this.m.func_187225_a(aC) && !((String)this.m.func_187225_a(aK)).equals("") && this.y_clash492() == fp.NULL) {
            this.field_70170_p.func_72900_e(this);
         }

         this.aZ = true;
      }
   }

   void e_clash263() {
      GoblinEntity var1 = this;
      int var2 = var1.a_clash58();
      if (var2 != -1) {
         var1.c_clash57(++var2);
         if (var2 == 15) {
            Vec3d var3 = b_clash264(this);
            float var4 = d_clash266(this);
            float var5 = c_clash265(this);
            this.func_70634_a(var3.field_72450_a, var3.field_72448_b, var3.field_72449_c);
            Vec3d var6 = a(new Vec3d(0.0, 0.0, 1.5), var4, var5);
            this.field_70159_w = var6.field_72450_a;
            this.field_70181_x = var6.field_72448_b;
            this.field_70179_y = var6.field_72449_c;
            if (!this.field_70170_p.field_72995_K) {
               this.b_clash431(var5);
            }
         }

         this.field_70145_X = false;
         this.func_189654_d(false);
         if (var2 == 39) {
            this.c_clash57(-1);
            this.b(fp.THROWN);
            this.e_clash499(null);
            this.a_clash55(null);
         }
      }
   }

   public static Vec3d b_clash264(BaseGirlEntity var0) {
      IGoblin var1 = (IGoblin)var0;
      UUID var2 = var1.e_clash54();
      if (var2 == null) {
         return var0.func_174791_d();
      }

      EntityPlayer var3 = var0.field_70170_p.func_152378_a(var2);
      return var3 == null
         ? var0.func_174791_d()
         : var3.func_174791_d().func_72441_c(0.0, var3.func_70047_e(), 0.0).func_178787_e(a(new Vec3d(0.4F, 0.0, 0.0), d_clash266(var0), c_clash265(var0)));
   }

   public static float c_clash265(BaseGirlEntity var0) {
      IGoblin var1 = (IGoblin)var0;
      UUID var2 = var1.e_clash54();
      if (var2 == null) {
         return 0.0F;
      }

      EntityPlayer var3 = var0.field_70170_p.func_152378_a(var2);
      return var3 == null ? 0.0F : var3.field_70759_as;
   }

   public static float d_clash266(BaseGirlEntity var0) {
      IGoblin var1 = (IGoblin)var0;
      UUID var2 = var1.e_clash54();
      if (var2 == null) {
         return 0.0F;
      }

      EntityPlayer var3 = var0.field_70170_p.func_152378_a(var2);
      return var3 == null ? 0.0F : var3.field_70125_A;
   }

   void J_clash267() {
      if (this.field_70122_E) {
         if (this.y_clash492() == fp.RUN) {
            EntityPlayer var1 = this.field_70170_p.func_72890_a(this, 100.0);
            if (var1 != null) {
               double var2 = 20.0;

               while (!(var2 <= 0.0)) {
                  Vec3d var5 = this.func_174791_d().func_178788_d(var1.func_174791_d());
                  Vec3d var6 = new Vec3d(Math.abs(var5.field_72450_a), Math.abs(var5.field_72448_b), Math.abs(var5.field_72449_c));
                  double var7 = var6.field_72450_a / (var6.field_72450_a + var6.field_72449_c);
                  double var9 = var6.field_72449_c / (var6.field_72450_a + var6.field_72449_c);
                  Vec3d var11 = this.func_174791_d()
                     .func_178787_e(new Vec3d((var5.field_72450_a > 0.0 ? 1 : -1) * var7 * var2, 0.0, (var5.field_72449_c > 0.0 ? 1 : -1) * var9 * var2));
                  PathNavigate var12 = this.func_70661_as();
                  var12.func_75499_g();
                  boolean var4 = var12.func_75492_a(var11.field_72450_a, var11.field_72448_b, var11.field_72449_c, 0.825F);
                  var2--;
                  if (var4) {
                     return;
                  }
               }
            }
         }
      }
   }

   protected void func_70664_aZ() {
      if (this.y_clash492() != fp.RUN || this.j_clash268()) {
         super.func_70664_aZ();
      }
   }

   boolean j_clash268() {
      PathNavigate var1 = this.func_70661_as();
      Path var2 = var1.func_75505_d();
      if (var2 == null) {
         return true;
      } else {
         int var3 = var2.func_75873_e();
         int var4 = var2.func_75874_d();
         if (var4 != var3 && var4 - 1 != var3) {
            PathPoint var5 = var2.func_75877_a(var3);
            PathPoint var6 = var2.func_75877_a(var3 + 1);
            return var6.field_75837_b - var5.field_75837_b == 1;
         } else {
            return true;
         }
      }
   }

   void B_clash269() {
      if (this.aX) {
         if (!(Boolean)this.m.func_187225_a(aC)) {
            if (!(Boolean)this.m.func_187225_a(aV)) {
               if (this.y_clash492() == fp.SIT) {
                  if (++this.aO >= 32000) {
                     EntityPlayer var1 = this.field_70170_p.func_72890_a(this, 3000.0);
                     if (var1 != null) {
                        if (var1.field_70122_E) {
                           if (!var1.field_70160_al) {
                              Integer var2 = this.c_clash271(var1);
                              if (var2 != null) {
                                 Vec3d var3 = var1.func_174791_d();
                                 Vec3d var4 = this.func_174791_d();
                                 Vec3d var5 = var3.func_178788_d(var4);
                                 double var6 = Math.sqrt(var5.field_72450_a * var5.field_72450_a + var5.field_72449_c * var5.field_72449_c);
                                 if (!(var6 > 100.0)) {
                                    ItemStack var8 = var1.field_71071_by.func_70301_a(var2).func_77946_l();
                                    GoblinEntity var9 = new GoblinEntity(this.field_70170_p, this.f_clash491().toString(), this.k_clash270());
                                    Vec3d var10 = b(new Vec3d(0.0, 0.0, -0.2F), var1.field_70759_as);
                                    var9.func_70107_b(var1.field_70165_t + var10.field_72450_a, var1.field_70163_u, var1.field_70161_v + var10.field_72449_c);
                                    var9.b(fp.RUN);
                                    this.field_70170_p.func_72838_d(var9);
                                    var9.m.func_187227_b(a0, var8);
                                    var1.func_145747_a(
                                       new TextComponentString(String.format("<%s> I got your %s hehe~", var9.c_clash241(), var8.func_82833_r()))
                                    );
                                    var1.field_71071_by.func_70304_b(var2);
                                    this.aO = 0;
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
   }

   int k_clash270() {
      return Integer.parseInt(a_clash225(this)[7]);
   }

   @Nullable
   Integer c_clash271(EntityPlayer var1) {
      NonNullList var2 = var1.field_71071_by.field_70462_a;
      ArrayList var3 = new ArrayList();

      for (int var4 = 0; var4 < var2.size(); var4++) {
         ItemStack var5 = (ItemStack)var2.get(var4);
         if (var5 != ItemStack.field_190927_a && ag.contains(var5.func_77973_b())) {
            var3.add(var4);
         }
      }

      return var3.isEmpty() ? null : (Integer)var3.get(this.func_70681_au().nextInt(var3.size()));
   }

   void m_clash272() {
      if (this.aX) {
         if (this.ae_clash498() == null) {
            this.c_clash502(this.al);
            this.b_clash431(this.ac);
            this.a_clash504(true);
            this.func_189654_d(true);
            this.b(fp.SIT);
         }
      }
   }

   @Override
   public void func_70071_h_() {
      this.i_clash246();
      e_clash273(this);
      this.e_clash263();
      if (this.e_clash54() != null) {
         this.field_71087_bX = false;
      }

      super.func_70071_h_();
      this.y_clash284();
      this.H_clash275();
      this.F_clash274();
      if (this.field_70170_p.field_72995_K) {
         this.v_clash276();
         this.A_clash277();
         if (this.e_clash54() != null) {
            this.field_70145_X = true;
         }
      }
   }

   @Override
   public fp b_clash62() {
      return this.aN;
   }

   @Override
   public void a_clash61(fp var1) {
      this.aN = var1;
   }

   @Override
   public void c_clash57(int var1) {
      this.aR = var1;
   }

   @Override
   public int a_clash58() {
      return this.aR;
   }

   public static void e_clash273(BaseGirlEntity var0) {
      fp var1 = var0.y_clash492();
      IGoblin var2 = (IGoblin)var0;
      if (var2.b_clash62() != fp.START_THROWING && var1 == fp.START_THROWING) {
         var2.c_clash57(0);
      }

      var2.a_clash61(var1);
   }

   public void func_70015_d(int var1) {
      if (this.e_clash54() == null) {
         super.func_70015_d(var1);
      }
   }

   void F_clash274() {
      if (this.y_clash492() == fp.VANISH) {
         this.ar -= 0.05F;
         if (!(this.ar > 0.0F)) {
            this.field_70170_p.func_72900_e(this);
         }
      }
   }

   void H_clash275() {
      if (!(Boolean)this.m.func_187225_a(aC)) {
         if (this.y_clash492() == fp.THROWN) {
            if (this.field_70122_E || this.func_70090_H()) {
               this.ar = (float)(this.ar - 0.05);
               if (!(this.ar > 0.0F)) {
                  if (!this.field_70170_p.field_72995_K) {
                     this.b(fp.NULL);
                     this.e_clash499(null);
                     this.a_clash55(null);
                     this.field_70170_p.func_72900_e(this);
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void v_clash276() {
      if (this.aY != -1) {
         if (++this.aY == 15) {
            this.aY = -1;
            this.b(fp.PAIZURI_START);
            Minecraft.func_71410_x().field_71439_g.func_71053_j();
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void A_clash277() {
      if (this.az != -1) {
         if (++this.az == 15) {
            this.az = -1;
            this.b(fp.NELSON_INTRO);
            Minecraft var1 = Minecraft.func_71410_x();
            var1.field_71439_g.func_71053_j();
            var1.field_71474_y.field_74320_O = 2;
         }
      }
   }

   @Override
   public void b(fp var1) {
      fp var2 = this.y_clash492();
      if (var2 != fp.PAIZURI_CUM || var1 != fp.PAIZURI_SLOW && var1 != fp.PAIZURI_FAST) {
         if (var2 != fp.NELSON_CUM || var1 != fp.NELSON_SLOW && var1 != fp.NELSON_FAST) {
            if (var2 != fp.BREEDING_CUM_0 || var1 != fp.BREEDING_SLOW_0 && var1 != fp.BREEDING_FAST_0) {
               if (var1 == fp.START_THROWING && !this.field_70170_p.field_72995_K) {
                  this.e_clash499(this.e_clash54());
                  this.L_clash281();
               }

               if (var1 == fp.PAIZURI_START && !this.field_70170_p.field_72995_K) {
                  this.z_clash280();
               }

               if (var1 == fp.NELSON_INTRO && !this.field_70170_p.field_72995_K) {
                  this.q_clash279();
               }

               if (this.y_clash492() == fp.PAIZURI_CUM && var1 == fp.NULL && !this.field_70170_p.field_72995_K) {
                  this.D_clash278();
               }

               if (var1 == fp.BREEDING_CUM_0) {
                  this.m.func_187227_b(aV, true);
                  this.av = this.field_70170_p.func_82737_E();
                  this.ai = this.field_70170_p.func_82737_E();
               }

               if (var1 == fp.BREEDING_CUM_0) {
                  this.Z = 0;
               }

               if (var1 == fp.NELSON_CUM) {
                  this.m.func_187227_b(aV, true);
               }

               if (var2 == fp.NELSON_CUM && var1 != fp.NELSON_CUM) {
                  this.m.func_187227_b(aV, false);
               }

               super.b(var1);
            }
         }
      }
   }

   void D_clash278() {
      EntityPlayer var1 = this.field_70170_p.func_152378_a(this.ae_clash498());
      if (var1 != null) {
         ResetGirlPacket.Handler.a((EntityPlayerMP)var1);
      }

      this.e_clash499(null);
      this.a_clash504(false);
      this.field_70145_X = false;
      this.func_189654_d(false);
      this.m.func_187227_b(a0, ItemStack.field_190927_a);
      if (!(Boolean)this.m.func_187225_a(aC)) {
         this.func_70634_a(this.l.field_72450_a, this.l.field_72448_b, this.l.field_72449_c);
         this.field_70170_p.func_72900_e(this);
      }
   }

   void q_clash279() {
      EntityPlayer var1 = this.field_70170_p.func_152378_a(this.ae_clash498());
      if (var1 != null) {
         this.a_clash55(null);
         this.c_clash502(var1.func_174791_d());
         this.b_clash431(var1.field_70177_z);
         this.a_clash504(true);
         this.field_70145_X = true;
         this.func_189654_d(true);
         var1.func_189654_d(true);
         var1.field_70145_X = true;
         this.e_clash499(var1.getPersistentID());
      }
   }

   void z_clash280() {
      EntityPlayer var1 = this.field_70170_p.func_152378_a(this.ae_clash498());
      if (var1 != null) {
         this.a_clash55(null);
         this.c_clash502(var1.func_174791_d());
         this.b_clash431(var1.field_70177_z + 180.0F);
         this.a_clash504(true);
         this.field_70145_X = true;
         this.func_189654_d(true);
         var1.func_189654_d(true);
         var1.field_70145_X = true;
         this.e_clash499(var1.getPersistentID());
         var1.func_70634_a(var1.field_70165_t, var1.field_70163_u - 0.5, var1.field_70161_v);
         var1.field_70125_A = 70.0F;
         var1.field_70127_C = 70.0F;
      }
   }

   void L_clash281() {
      ItemStack var1 = (ItemStack)this.m.func_187225_a(a0);
      if (var1 != ItemStack.field_190927_a) {
         EntityPlayer var2 = this.field_70170_p.func_152378_a(this.ae_clash498());
         if (var2 != null) {
            var2.field_71071_by.func_70441_a(var1.func_77946_l());
            this.m.func_187227_b(a0, ItemStack.field_190927_a);
         }
      }
   }

   public static void a_clash282(BaseGirlEntity var0) {
      if (var0.y_clash492() == fp.PICK_UP) {
         IGoblin var1 = (IGoblin)var0;
         UUID var2 = var1.e_clash54();
         if (var2 == null) {
            var1.b_clash63(-1);
            var0.b(fp.NULL);
            var1.a_clash55(null);
         } else {
            EntityPlayer var3 = var0.field_70170_p.func_152378_a(var2);
            if (var3 == null) {
               var1.b_clash63(-1);
               var0.b(fp.NULL);
               var1.a_clash55(null);
            } else {
               var0.func_70107_b(var3.field_70165_t, var3.field_70163_u, var3.field_70161_v);
               if (var0.func_174791_d().func_72438_d(var3.func_174791_d()) > 10.0) {
                  var1.b_clash63(-1);
                  var0.b(fp.NULL);
                  var1.a_clash55(null);
               } else {
                  int var4 = var1.c_clash56() - 1;
                  var1.b_clash63(var4);
                  if (var4 == 0) {
                     var0.b(fp.SHOULDER_IDLE);
                     var0.field_70145_X = true;
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean t_clash283() {
      if (this.y_clash492() != fp.NULL) {
         return false;
      } else if (this.e_clash54() != null) {
         return false;
      } else {
         return !this.m.func_187225_a(aC) && !Minecraft.func_71410_x().field_71439_g.func_70685_l(this) ? false : this.e_clash54() == null;
      }
   }

   void y_clash284() {
      if (this.y_clash492() == fp.SHOULDER_IDLE) {
         UUID var1 = this.e_clash54();
         if (var1 != null) {
            EntityPlayer var2 = this.field_70170_p.func_152378_a(var1);
            if (var2 != null) {
               this.func_70107_b(var2.field_70165_t, var2.field_70163_u, var2.field_70161_v);
               this.field_70145_X = true;
               this.func_189654_d(true);
            }
         }
      }
   }

   @Override
   protected fp c_clash235(fp var1) {
      switch (var1) {
         case PAIZURI_IDLE:
         case PAIZURI_SLOW:
            return fp.PAIZURI_FAST;
         case BREEDING_SLOW_0:
            return fp.BREEDING_FAST_0;
         case BREEDING_SLOW_2:
            return fp.BREEDING_FAST_2;
         case NELSON_SLOW:
            return fp.NELSON_FAST;
         default:
            return null;
      }
   }

   @Override
   protected fp a_clash236(fp var1) {
      switch (var1) {
         case PAIZURI_SLOW:
         case PAIZURI_FAST:
         case PAIZURI_FAST_CONTINUES:
            return fp.PAIZURI_CUM;
         case BREEDING_SLOW_0:
         case BREEDING_FAST_0:
            for (GoblinEntity var3 : this.ab) {
               var3.a_clash236(var1);
            }

            return fp.BREEDING_CUM_0;
         case BREEDING_SLOW_2:
         case BREEDING_FAST_2:
            return fp.BREEDING_CUM_2;
         case NELSON_SLOW:
         case NELSON_FAST:
            return fp.NELSON_CUM;
         case BREEDING_1:
            return fp.BREEDING_CUM_1;
         default:
            return null;
      }
   }

   public boolean C_clash285() {
      Block var1 = this.field_70170_p.func_180495_p(this.func_180425_c().func_177982_a(0, 1, 0)).func_177230_c();
      return !var1.func_176205_b(this.field_70170_p, this.func_180425_c().func_177982_a(0, 1, 0));
   }

   public void func_180430_e(float var1, float var2) {
      fp var3 = this.y_clash492();
      if (var3 != fp.THROWN && var3 != fp.START_THROWING) {
         super.func_180430_e(var1, var2);
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      if (this.field_70170_p instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      if (this.C == null) {
         this.p_clash506();
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.y_clash492() != fp.NULL) {
               this.a("animation.goblin.null", true, var1);
            } else {
               this.a("animation.goblin.blink", true, var1);
            }
            break;
         case "movement":
            if (this.y_clash492() != fp.NULL) {
               this.a("animation.goblin.null", true, var1);
            } else {
               double var4 = Math.abs(this.field_70169_q - this.field_70165_t) + Math.abs(this.field_70166_s - this.field_70161_v);
               if (!(Boolean)this.m.func_187225_a(G) && var4 > 0.0) {
                  if (this.field_70122_E && Math.abs(Math.abs(this.field_70167_r) - Math.abs(this.field_70163_u)) < 0.1F) {
                     if (var4 > 0.2F) {
                        this.a("animation.goblin.walk", true, var1);
                     } else {
                        this.a("animation.goblin.walk", true, var1);
                     }

                     this.field_70177_z = this.field_70759_as;
                  } else {
                     this.a("animation.goblin.fly", true, var1);
                  }
               } else {
                  this.a("animation.goblin.idle", true, var1);
               }
            }
            break;
         case "action":
            Minecraft var6 = Minecraft.func_71410_x();
            String var7 = var6.field_71439_g.getPersistentID().equals(this.e_clash54()) && var6.field_71474_y.field_74320_O == 0 ? "1" : "3";
            switch (this.y_clash492()) {
               case PAIZURI_IDLE:
                  this.a("animation.goblin.paizuri_idle", true, var1);
                  break;
               case PAIZURI_SLOW:
                  this.a("animation.goblin.paizuri_slow" + this.aP, true, var1);
                  break;
               case BREEDING_SLOW_0:
                  this.a("animation.goblin.breeding_slow_1" + (this.aD ? "l" : "r"), true, var1);
                  break;
               case BREEDING_SLOW_2:
                  this.a("animation.goblin.breeding_slow_3", true, var1);
                  break;
               case NELSON_SLOW:
                  this.a("animation.goblin.nelson_slow" + (this.aF ? "" : "2"), true, var1);
                  break;
               case PAIZURI_FAST:
                  this.a("animation.goblin.paizuri_fast", true, var1);
                  break;
               case PAIZURI_FAST_CONTINUES:
                  this.a("animation.goblin.paizuri_fast_countinues", true, var1);
                  break;
               case BREEDING_1:
                  this.a("animation.goblin.breeding_2", true, var1);
                  break;
               case BREEDING_FAST_2:
                  this.a("animation.goblin.breeding_fast_3", true, var1);
                  break;
               case NELSON_FAST:
                  this.a("animation.goblin.nelson_fast" + (this.X ? "c" : "s"), true, var1);
                  break;
               case BREEDING_FAST_0:
                  this.a("animation.goblin.breeding_fast_1" + (this.ay ? "c" : "s"), true, var1);
                  break;
               case NULL:
                  this.a("animation.goblin.null", true, var1);
                  break;
               case SHOULDER_IDLE:
                  this.a("animation.goblin.shoulder_idle", true, var1);
                  break;
               case PICK_UP:
                  this.a(String.format("animation.goblin.pick_up_%sperson", var7), true, var1);
                  break;
               case SIT:
                  this.a("animation.goblin.sit", true, var1);
                  break;
               case RUN:
                  if (this.field_70122_E) {
                     this.a("animation.goblin.running", true, var1);
                  } else {
                     this.a("animation.goblin.fly", true, var1);
                  }
                  break;
               case CATCH:
                  this.a(String.format("animation.goblin.catch_%sperson", var7), true, var1);
                  break;
               case CATCH_BJ:
                  this.a(String.format("animation.goblin.catch_%spersonBj", var7), true, var1);
                  break;
               case CATCH_BJ_IDLE:
                  this.a(String.format("animation.goblin.catch_%spersonBj_idle", var7), true, var1);
                  break;
               case START_THROWING:
                  this.a(String.format("animation.goblin.throw_%sperson", var7), true, var1);
                  break;
               case THROWN:
                  this.a("animation.goblin.thrown", true, var1);
                  break;
               case PAIZURI_START:
                  this.a("animation.goblin.paizuri_start", true, var1);
                  break;
               case PAIZURI_CUM:
                  this.a("animation.goblin.paizuri_cum", true, var1);
                  break;
               case JUMP_0:
                  this.a("animation.goblin.jump_1", true, var1);
                  break;
               case JUMP_1:
                  this.a("animation.goblin.jump_2", true, var1);
                  break;
               case JUMP_2:
                  this.a("animation.goblin.jump_3", true, var1);
                  break;
               case BREEDING_INTRO_0:
                  this.a("animation.goblin.breeding_intro_1", true, var1);
                  break;
               case BREEDING_INTRO_1:
                  this.a("animation.goblin.breeding_intro_2", true, var1);
                  break;
               case BREEDING_INTRO_2:
                  this.a("animation.goblin.breeding_intro_3", true, var1);
                  break;
               case BREEDING_CUM_0:
                  this.a("animation.goblin.breeding_cum_1", true, var1);
                  break;
               case BREEDING_CUM_1:
                  this.a("animation.goblin.breeding_cum_2", true, var1);
                  break;
               case BREEDING_CUM_2:
                  this.a("animation.goblin.breeding_cum_3", true, var1);
                  break;
               case VANISH:
               case AWAIT_PICK_UP:
                  this.a("animation.goblin.await_pick_up", true, var1);
                  break;
               case STAND_UP:
                  this.a("animation.goblin.stand_up", false, var1);
                  break;
               case NELSON_INTRO:
                  this.a("animation.goblin.nelson_intro", true, var1);
                  break;
               case NELSON_CUM:
                  this.a("animation.goblin.nelson_cum", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData var1) {
      if (this.C == null) {
         this.p_clash506();
      }

      AnimationController.ISoundListener var2 = var1x -> {
         switch (var1x.sound) {
            case "catchEh":
               this.a_clash541("ehh..");
               this.a(SoundHandler.MISC_PLOB);
               break;
            case "catchAkward":
               this.a_clash541("awkward..");
               this.a(SoundHandler.MISC_PLOB);
               break;
            case "catchWell":
               this.a_clash541("well...");
               this.a(SoundHandler.MISC_PLOB);
               break;
            case "catchRather":
               this.a_clash541("would you rather have this stupid... thing?");
               this.a(SoundHandler.MISC_PLOB);
               break;
            case "catchMe":
               this.a_clash541("...or use me?~");
               this.a(SoundHandler.MISC_PLOB);
               break;
            case "catchDone":
               if ("bj".equals(this.m.func_187225_a(h))) {
                  this.b(fp.CATCH_BJ);
               }
               break;
            case "catchBjDone":
               this.b(fp.CATCH_BJ_IDLE);
               if (this.n_clash537()) {
                  EntityPlayerSP var6 = Minecraft.func_71410_x().field_71439_g;
                  a(var6, this, new String[]{"use her", "take ur stuff back"}, null, false);
               }
               break;
            case "paizuriChoice":
               this.a_clash541("good choice!~");
               this.a(SoundHandler.MISC_PLOB);
               break;
            case "paizuriBoth":
               this.a_clash541("...for both of us!");
               this.a(SoundHandler.MISC_PLOB);
               break;
            case "paizruiUse":
               this.a_clash541("now use me like a fuck toy!~");
               this.a(SoundHandler.MISC_PLOB);
               break;
            case "paizuriSwitch":
               if (!this.func_70681_au().nextBoolean()) {
                  this.aP = "".equals(this.aP) ? "2" : "";
               }
               break;
            case "touch":
               this.a(SoundHandler.MISC_TOUCH, 3.0F);
               break;
            case "pound":
               this.a(SoundHandler.MISC_POUNDING);
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.04F);
               }
               break;
            case "paizuri_startDone":
               this.b(fp.PAIZURI_IDLE);
               if (this.n_clash537()) {
                  HornyMeterHud.d_clash358();
               }
               break;
            case "paizuriFastDone":
               this.b(fp.PAIZURI_SLOW);
               break;
            case "paizuriFastReady":
               if (this.n_clash537() && d3.d) {
                  this.b(fp.PAIZURI_FAST_CONTINUES);
               }
               break;
            case "paizuriFastContinuesReady":
               if (this.n_clash537() && d3.d) {
                  this.N();
               }
               break;
            case "smallPound":
               this.a(SoundHandler.MISC_POUNDING, 0.25F);
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.02F);
               }
               break;
            case "paizruiCam":
               if (this.n_clash537()) {
                  EntityPlayerSP var4 = Minecraft.func_71410_x().field_71439_g;
                  var4.field_70125_A = 70.0F;
                  var4.field_70127_C = 70.0F;
               }
               break;
            case "blackScreen":
               if (this.n_clash537()) {
                  BeeScreen.b_clash732();
               }
               break;
            case "paizuriCumDone":
               this.b(fp.NULL);
               break;
            case "cumSound":
               this.a(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "jumpCam":
               if (this.n_clash537()) {
                  Minecraft var9 = Minecraft.func_71410_x();
                  var9.field_71439_g.field_70177_z = this.I_clash415() + 170.0F;
                  var9.field_71439_g.field_70125_A = -20.0F;
                  var9.field_71439_g.field_70759_as = var9.field_71439_g.field_70177_z;
                  var9.field_71474_y.field_74320_O = 2;
               }
               break;
            case "breedingHmm":
               if (this.n_clash537()) {
                  Minecraft var8 = Minecraft.func_71410_x();
                  var8.field_71439_g.field_70177_z = this.I_clash415() + 180.0F;
                  var8.field_71439_g.field_70125_A = -15.0F;
                  var8.field_71439_g.field_70759_as = var8.field_71439_g.field_70177_z;
                  var8.field_71474_y.field_74320_O = 0;
               }

               this.a_clash541("hmm...");
               this.a(SoundHandler.MISC_PLOB);
               break;
            case "breedingFound":
               this.a_clash541("guess we found a worthy breeding partner!");
               this.a(SoundHandler.MISC_PLOB);
               break;
            case "breedingEnough":
               this.a_clash541("Eh.. go pin him down, before he runs off!");
               this.a(SoundHandler.MISC_PLOB);
               break;
            case "breedingCam2":
               if (this.n_clash537()) {
                  Minecraft var7 = Minecraft.func_71410_x();
                  var7.field_71474_y.field_74320_O = 2;
                  var7.field_71439_g.field_70177_z = this.I_clash415() - 120.0F;
                  var7.field_71439_g.field_70125_A = -30.0F;
               }
            case "breedingIntroDone":
               this.b(fp.BREEDING_SLOW_0);
               if (this.n_clash537()) {
                  HornyMeterHud.d_clash358();
               }
               break;
            case "breeding_slow1Done":
               if (this.func_70681_au().nextBoolean()) {
                  this.aD = !this.aD;
               }

               if (this.n_clash537() && d3.d) {
                  this.b(fp.BREEDING_FAST_0);
                  this.ay = false;
               }
               break;
            case "breeding_fast1Done":
               this.b(fp.BREEDING_SLOW_0);
               if (this.n_clash537()) {
                  this.ay = false;
               }
               break;
            case "breeding_fast1Ready":
               if (this.n_clash537() && d3.d) {
                  this.ay = true;
                  this.N();
                  this.C.tickOffset = 0.0;
               }
               break;
            case "cum":
               this.a(SoundHandler.MISC_SMALLINSERTS, 2.0F);
               break;
            case "breeding_intro_3Done":
               this.b(fp.BREEDING_SLOW_2);
               break;
            case "breeding_3_wiggle":
               if (this.func_70681_au().nextBoolean()) {
                  this.C.tickOffset = 0.0;
               }
               break;
            case "breeding_fast_3Done":
               if (this.n_clash537() && !d3.d) {
                  this.b(fp.BREEDING_SLOW_2);
               }
               break;
            case "breeding_intro_2Done":
               this.b(fp.BREEDING_1);
               break;
            case "breeding_cumCam":
               if (this.n_clash537()) {
                  Minecraft var5 = Minecraft.func_71410_x();
                  var5.field_71474_y.field_74320_O = 0;
                  var5.field_71439_g.field_70177_z = this.I_clash415() + 180.0F;
                  var5.field_71439_g.field_70125_A = -15.0F;
                  var5.field_71439_g.field_70759_as = var5.field_71439_g.field_70177_z;
                  var5.field_71474_y.field_74320_O = 0;
               }
               break;
            case "neslon_introDone":
               this.b(fp.NELSON_SLOW);
               if (this.n_clash537()) {
                  HornyMeterHud.d_clash358();
               }
               break;
            case "nelson_slowDone":
               if (this.func_70681_au().nextBoolean()) {
                  this.aF = !this.aF;
               }
               break;
            case "neslon_fastSwitch":
               if (!this.n_clash537()) {
                  this.X = true;
                  return;
               }

               if (d3.d) {
                  this.X = true;
               }
               break;
            case "neslon_fastBackSwitch":
               if (!this.n_clash537()) {
                  this.C.tickOffset = 0.0;
               } else if (d3.d) {
                  this.C.tickOffset = 0.0;
               }
               break;
            case "nelsonFastDone":
               this.X = false;
               if (this.n_clash537()) {
                  this.b(fp.NELSON_SLOW);
               }
               break;
            case "nelson_cumDone":
               if (this.n_clash537()) {
                  this.r_clash533();
                  this.b(fp.NULL);
               }
         }
      };
      this.C.registerSoundListener(var2);
      this.E.transitionLengthTicks = 10.0;
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.E);
      var1.addAnimationController(this.s);
   }

   public static class c {
      static Minecraft a = null;

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void a(ClientTickEvent var1) {
         if (var1.phase != Phase.START) {
            ArrayList var2 = new ArrayList();

            try {
               for (BaseGirlEntity var4 : BaseGirlEntity.ad_clash509()) {
                  if (var4.field_70170_p.field_72995_K && var4 instanceof GoblinEntity) {
                     GoblinEntity var5 = (GoblinEntity)var4;
                     UUID var6 = var5.e_clash54();
                     if (var6 != null) {
                        EntityPlayer var7 = var5.field_70170_p.func_152378_a(var6);
                        if (var7 != null && var7.field_71093_bK != var5.field_71093_bK) {
                           var2.add(var5);
                        }
                     }
                  }
               }
            } catch (ConcurrentModificationException var8) {
            }

            for (GoblinEntity var10 : (java.util.Collection<GoblinEntity>) (var2) ) {
               var10.a_clash55(null);
               var10.e_clash499(null);
               var10.func_70106_y();
            }
         }
      }

      @SubscribeEvent
      public void a(PlayerChangedDimensionEvent var1) {
         EntityPlayer var2 = var1.player;
         UUID var3 = var2.getPersistentID();
         int var4 = var1.toDim;
         World var5 = var2.field_70170_p;
         GoblinEntity var6 = null;

         try {
            for (BaseGirlEntity var8 : BaseGirlEntity.ad_clash509()) {
               if (!var8.field_70170_p.field_72995_K && var8 instanceof GoblinEntity) {
                  GoblinEntity var9 = (GoblinEntity)var8;
                  if (var3.equals(var9.e_clash54())) {
                     String var10 = var9.C_clash559();
                     String var11 = var9.F_clash553();
                     var6 = var9;
                     var6.a_clash55(null);
                     var6.e_clash499(null);
                     var6.b(fp.NULL);
                     GoblinEntity var12 = new GoblinEntity(var5);
                     var12.field_71093_bK = var4;
                     var12.field_98038_p = true;
                     var12.f_clash439(var10);
                     var12.e_clash558(var11);
                     var12.m.func_187227_b(GoblinEntity.aC, true);
                     var5.func_72838_d(var12);
                     var12.func_70634_a(var2.field_70165_t, var2.field_70163_u, var2.field_70161_v);
                     var12.a_clash55(var3);
                     var12.b(fp.SHOULDER_IDLE);
                     break;
                  }
               }
            }
         } catch (ConcurrentModificationException var13) {
         }

         if (var6 != null) {
            var5.func_72900_e(var6);
            BaseGirlEntity.ad_clash509().remove(var6);
         }
      }

      @SubscribeEvent
      public void a(LivingAttackEvent var1) {
         if (var1.getSource() != DamageSource.field_76380_i) {
            EntityLivingBase var2 = var1.getEntityLiving();
            if (var2 instanceof GoblinEntity) {
               GoblinEntity var3 = (GoblinEntity)var2;
               if (var3.e_clash54() != null) {
                  var1.setCanceled(true);
               }
            }
         }
      }

      @SubscribeEvent
      @SideOnly(Side.CLIENT)
      public void a(KeyInputEvent var1) {
         if (a == null) {
            a = Minecraft.func_71410_x();
         }

         if (!(a.field_71462_r instanceof GalathScreen)) {
            if (ClientProxy.keyBindings[0].func_151468_f()) {
               BaseGirlEntity var2 = null;
               UUID var3 = Minecraft.func_71410_x().field_71439_g.getPersistentID();

               try {
                  for (BaseGirlEntity var5 : BaseGirlEntity.ad_clash509()) {
                     if (var5.field_70170_p.field_72995_K && var5 instanceof IGoblin) {
                        IGoblin var6 = (IGoblin)var5;
                        if (var3.equals(var6.e_clash54())) {
                           var2 = var5;
                           break;
                        }
                     }
                  }
               } catch (ConcurrentModificationException var7) {
               }

               if (var2 != null) {
                  if (var2.y_clash492() == fp.SHOULDER_IDLE) {
                     Minecraft.func_71410_x().func_147108_a(new GalathScreen(var2));
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
