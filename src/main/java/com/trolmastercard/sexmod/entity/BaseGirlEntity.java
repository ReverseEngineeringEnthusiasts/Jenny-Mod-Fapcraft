package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.GirlInventoryScreen;
import com.trolmastercard.sexmod.client.model.GirlModel;
import com.trolmastercard.sexmod.client.renderer.GirlRenderer;
import com.trolmastercard.sexmod.client.renderer.SexSceneRenderer;
import com.trolmastercard.sexmod.entity.ai.DoorInteractAiGoal;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.networking.ChangeDataParameterPacket;
import com.trolmastercard.sexmod.networking.KoboldStatePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetControllerPacket;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SendChatMessagePacket;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.cj;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.dz;
import com.trolmastercard.sexmod.util.e1;
import com.trolmastercard.sexmod.util.fs;
import com.trolmastercard.sexmod.util.g0;







import com.mojang.realmsclient.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.item.EntityEnderPearl;
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
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.util.MatrixStack;

public abstract class BaseGirlEntity extends EntityCreature implements IAnimatable {
   protected static final long t = 20L;
   private final AnimationFactory g = new AnimationFactory(this);
   public EntityAIWanderAvoidWater z;
   public WatchClosestGirlGoal o;
   public static HashSet<BaseGirlEntity> k = new HashSet<>();
   public Vec3d B;
   protected float r;
   public EntityDataManager m;
   public PathNavigate f;
   public Vec3d l = Vec3d.field_186680_a;
   public EntityEnderPearl q;
   public float n = 1.0F;
   public boolean F = false;
   private boolean i = false;
   HashMap<String, Vec3d> x = new HashMap<>();
   public static final DataParameter<String> v = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(110);
   public static final DataParameter<Boolean> G = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187198_h)
      .func_187156_b()
      .func_187161_a(109);
   public static final DataParameter<String> e = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(108);
   public static final DataParameter<Float> w = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187193_c)
      .func_187156_b()
      .func_187161_a(107);
   public static final DataParameter<String> u = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(106);
   public static final DataParameter<Integer> D = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187192_b)
      .func_187156_b()
      .func_187161_a(105);
   public static final DataParameter<String> J = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(104);
   public static final DataParameter<String> h = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(103);
   public static final DataParameter<String> y = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(102);
   public static final DataParameter<String> a = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(101);
   public static final DataParameter<String> b = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(100);
   public static final DataParameter<String> c = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(99);
   protected static final List<Item> I = Arrays.asList(Items.field_151166_bC, Items.field_151045_i, Items.field_151043_k, Items.field_151079_bi);
   public AnimationController C;
   public AnimationController E;
   public AnimationController s;
   HashMap<String, Pair<Integer, Integer>> A = new HashMap<>();
   AnimationProcessor<?> H = null;
   public List<String> p = new ArrayList<>();
   protected List<Entry<BoneType, Entry<List<String>, Integer>>> d = null;

   public void a(BaseGirlEntity.BaseGirlEntityState var1) {
      this.m.func_187227_b(a, var1.toString());
   }

   public BaseGirlEntity.BaseGirlEntityState q_clash489() {
      return BaseGirlEntity.BaseGirlEntityState.valueOf((String)this.m.func_187225_a(a));
   }

   @SideOnly(Side.CLIENT)
   protected void a_clash490(String var1, String var2) {
      PacketHandler.b.sendToServer(new ChangeDataParameterPacket(this.f_clash491(), var1, var2));
   }

   public UUID f_clash491() {
      try {
         return UUID.fromString((String)this.m.func_187225_a(u));
      } catch (Exception var3) {
         UUID var2 = UUID.randomUUID();
         this.m.func_187227_b(u, var2.toString());
         return var2;
      }
   }

   public fp y_clash492() {
      return fp.valueOf((String)this.m.func_187225_a(J));
   }

   public void b(fp var1) {
      fp var2 = this.y_clash492();
      if (var2 != var1) {
         if (var1 != fp.ATTACK || var2 == fp.NULL) {
            var1 = var1 == null ? fp.NULL : var1;
            if (this.field_70170_p.field_72995_K) {
               this.a_clash490("currentAction", var1.toString());
            } else {
               var2.ticksPlaying = new int[]{0, 0};
               this.m.func_187227_b(J, var1.toString());
            }
         }
      }
   }

   public int ah_clash493() {
      return (Integer)this.m.func_187225_a(D);
   }

   public void f(int var1) {
      if (this.field_70170_p.field_72995_K) {
         this.a_clash490("currentModel", "0");
      } else {
         this.m.func_187227_b(D, var1);
      }
   }

   public boolean m_clash494() {
      return false;
   }

   @Nullable
   public EntityPlayer S_clash495() {
      UUID var1 = this.ae_clash498();
      return var1 == null ? null : this.field_70170_p.func_152378_a(var1);
   }

   public static void a_clash496(BaseGirlEntity var0, String var1) {
      for (EntityPlayer var3 : cj.a_clash303(var0)) {
         var3.func_145747_a(new TextComponentString(var1));
      }
   }

   public static void a(BaseGirlEntity var0, SoundEvent var1, boolean var2) {
      Vec3d var3 = var0.func_174791_d();

      for (EntityPlayer var5 : cj.a_clash303(var0)) {
         Vec3d var6;
         if (!var2) {
            var6 = var3;
         } else {
            Vec3d var7 = var5.func_174791_d();
            Vec3d var8 = var3.func_178788_d(var7).func_72432_b();
            var6 = var7.func_178787_e(var8);
         }

         ((EntityPlayerMP)var5)
            .field_71135_a
            .func_147359_a(new SPacketSoundEffect(var1, SoundCategory.AMBIENT, var6.field_72450_a, var6.field_72448_b, var6.field_72449_c, 1.0F, 1.0F));
      }
   }

   public static void a(BaseGirlEntity var0, SoundEvent var1) {
      a(var0, var1, false);
   }

   public static void a(BaseGirlEntity var0, SoundEvent[] var1) {
      a(var0, SoundHandler.a_clash804(var1));
   }

   public static void a(BaseGirlEntity var0, SoundEvent[] var1, boolean var2) {
      a(var0, SoundHandler.a_clash804(var1), var2);
   }

   @SideOnly(Side.CLIENT)
   public Vec3d A_clash497() {
      Vec3d var1 = Minecraft.func_71410_x().field_71439_g.func_174791_d();
      Vec3d var2 = this.func_174791_d();
      Vec3d var3 = var2.func_178788_d(var1).func_72432_b();
      return var1.func_178787_e(var3);
   }

   @Nullable
   public UUID ae_clash498() {
      String var1 = (String)this.m.func_187225_a(y);
      return var1.equals("null") ? null : UUID.fromString(var1);
   }

   public void e_clash499(UUID var1) {
      if (this.field_70170_p.field_72995_K) {
         if (var1 == null) {
            this.a_clash490("playerSheHasSexWith", null);
         } else {
            this.a_clash490("playerSheHasSexWith", var1.toString());
         }
      } else {
         if (var1 == null) {
            this.m.func_187227_b(y, "null");
         } else {
            this.m.func_187227_b(y, var1.toString());
         }
      }
   }

   public void a_clash500(@Nonnull EntityPlayer var1) {
      this.e_clash499(var1.getPersistentID());
   }

   public Vec3d o_clash501() {
      String[] var1 = ((String)this.m.func_187225_a(e)).split("\\|");
      return new Vec3d(Double.parseDouble(var1[0]), Double.parseDouble(var1[1]), Double.parseDouble(var1[2]));
   }

   public void c_clash502(Vec3d var1) {
      if (this.field_70170_p.field_72995_K) {
         String var2 = var1.field_72450_a + "f" + var1.field_72448_b + "f" + var1.field_72449_c + "f";
         this.a_clash490("targetPos", var2);
      } else {
         this.m.func_187227_b(e, var1.field_72450_a + "|" + var1.field_72448_b + "|" + var1.field_72449_c);
      }
   }

   public void a_clash503(Vec3d var1) {
      this.m.func_187227_b(e, var1.field_72450_a + "|" + var1.field_72448_b + "|" + var1.field_72449_c);
   }

   public Float I_clash415() {
      return (Float)this.m.func_187225_a(w);
   }

   public void b_clash431(float var1) {
      this.m.func_187227_b(w, var1);
   }

   public void a_clash504(boolean var1) {
      if (this.field_70170_p.field_72995_K) {
         this.a_clash490("shouldbeattargetpos", String.valueOf(var1));
      } else {
         this.m.func_187227_b(G, var1);
      }
   }

   public boolean Q_clash505() {
      return (Boolean)this.m.func_187225_a(G);
   }

   protected boolean func_70692_ba() {
      return false;
   }

   protected BaseGirlEntity(World var1) {
      super(var1);
      if (var1.field_72995_K) {
         this.p_clash506();
      }

      if (!var1.field_72995_K || !(var1 instanceof SexWorldClient)) {
         PathNavigate var2 = this.func_70661_as();
         if (var2 instanceof PathNavigateGround) {
            ((PathNavigateGround)var2).func_179688_b(true);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   protected void p_clash506() {
      this.C = new AnimationController<>(this, "action", 0.0F, this::a);
      this.E = new AnimationController<>(this, "movement", 5.0F, this::a);
      this.s = new AnimationController<>(this, "eyes", 10.0F, this::a);
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.f = this.func_70661_as();
      this.m = this.func_184212_Q();
      this.m.func_187214_a(u, UUID.randomUUID().toString());
      this.m.func_187214_a(D, 1);
      this.m.func_187214_a(J, fp.NULL.toString());
      this.m.func_187214_a(h, "");
      this.m.func_187214_a(y, "null");
      this.m.func_187214_a(G, false);
      this.m.func_187214_a(w, 0.0F);
      this.m.func_187214_a(e, "0|0|0");
      this.m.func_187214_a(v, "");
      this.m.func_187214_a(a, BaseGirlEntity.BaseGirlEntityState.WALK.toString());
      this.m.func_187214_a(b, "");
      this.m.func_187214_a(c, "");
   }

   public void b_clash507(boolean var1) {
      this.i = var1;
      if (var1) {
         fs.b_clash710(this);
      } else {
         fs.a_clash711(this);
      }
   }

   public boolean h_clash508() {
      return this.i;
   }

   public static List<BaseGirlEntity> ad_clash509() {
      if (!g0.a_clash472()) {
         return Z_clash510();
      }

      WorldServer[] var0 = FMLCommonHandler.instance().getMinecraftServerInstance().field_71305_c;
      if (var0.length == 0) {
         return new ArrayList<>();
      }

      ArrayList var1 = new ArrayList();

      for (WorldServer var5 : var0) {
         var1.addAll(var5.func_175644_a(BaseGirlEntity.class, var0x -> true));
      }

      return var1;
   }

   @SideOnly(Side.CLIENT)
   private static List<BaseGirlEntity> Z_clash510() {
      WorldClient var0 = Minecraft.func_71410_x().field_71441_e;
      return var0 == null ? new ArrayList<>() : var0.func_175644_a(BaseGirlEntity.class, var0x -> true);
   }

   public boolean B_clash511() {
      return true;
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(20.0);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.5);
      this.func_110148_a(SharedMonsterAttributes.field_111265_b).func_111128_a(30.0);
   }

   protected void func_184651_r() {
      this.z = new EntityAIWanderAvoidWater(this, 0.35);
      this.o = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.field_70714_bg.func_75776_a(0, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(2, new EntityAITempt(this, 0.4, false, new HashSet<>(I)));
      this.field_70714_bg.func_75776_a(3, new DoorInteractAiGoal(this));
      this.field_70714_bg.func_75776_a(5, this.o);
      this.field_70714_bg.func_75776_a(5, this.z);
   }

   public void func_70014_b(NBTTagCompound var1) {
      var1.func_74780_a("homeX", this.l.field_72450_a);
      var1.func_74780_a("homeY", this.l.field_72448_b);
      var1.func_74780_a("homeZ", this.l.field_72449_c);
      var1.func_74778_a("girlID", (String)this.m.func_187225_a(u));
      String var2 = this.w_clash539();
      if (!"".equals(var2)) {
         var1.func_74778_a("sexmod:customname", var2);
      }

      if (this.X_clash438()) {
         var1.func_74778_a("sexmod:customModel", this.C_clash559());
      }

      super.func_70014_b(var1);
   }

   protected boolean X_clash438() {
      return a_clash542(this);
   }

   public void func_70037_a(NBTTagCompound var1) {
      super.func_70037_a(var1);
      this.l = new Vec3d(var1.func_74769_h("homeX"), var1.func_74769_h("homeY"), var1.func_74769_h("homeZ"));
      String var2 = var1.func_74779_i("sexmod:customname");
      if (!"".equals(var2)) {
         this.g_clash538(var2);
      }

      String var3 = var1.func_74779_i("girlID");
      if (!"".equals(var3)) {
         UUID var4 = UUID.fromString(var3);
         boolean var5 = false;

         for (BaseGirlEntity var7 : g_clash524(var4)) {
            if (!var7.field_70170_p.field_72995_K && var7 != this && !var7.field_70128_L && var7.isAddedToWorld()) {
               var5 = true;
               break;
            }
         }

         if (var5) {
            Main.LOGGER.log(Level.WARN, String.format("got a duped %s with id '%s'. Deleted her", this.c_clash241(), var4));
            this.field_70170_p.func_72900_e(this);
         } else {
            this.m.func_187227_b(u, var4.toString());
            if (this.X_clash438()) {
               this.f_clash439(var1.func_74779_i("sexmod:customModel"));
            }
         }
      }
   }

   public boolean d_clash453() {
      return true;
   }

   public void func_70016_h(double var1, double var3, double var5) {
      this.field_70159_w = var1;
      this.field_70181_x = var3;
      this.field_70179_y = var5;
   }

   public void b_clash512(Vec3d var1) {
      this.field_70159_w = var1.field_72450_a;
      this.field_70181_x = var1.field_72448_b;
      this.field_70179_y = var1.field_72449_c;
   }

   public Vec3d j_clash513() {
      return new Vec3d(this.field_70142_S, this.field_70137_T, this.field_70136_U);
   }

   public void func_70619_bc() {
      if ((Boolean)this.m.func_187225_a(G)) {
         this.func_70034_d(this.I_clash415());
         this.func_70080_a(this.o_clash501().field_72450_a, this.o_clash501().field_72448_b, this.o_clash501().field_72449_c, this.I_clash415(), 0.0F);
         this.func_70101_b(this.I_clash415(), this.field_70125_A);
      }

      if (this.l.equals(Vec3d.field_186680_a)) {
         this.l = new Vec3d(this.func_180425_c());
      }

      this.G();
   }

   public void func_70071_h_() {
      super.func_70071_h_();
      this.l_clash514();
   }

   protected void G() {
      if (ServerWhitelistManager.e) {
         HashSet var1 = this.Y_clash561();
         NpcType var2 = NpcType.a_clash751(this);
         HashSet var3 = new HashSet();
         String var4 = ServerWhitelistManager.h_clash132();

         for (String var6 : (java.util.Collection<String>) (var1) ) {
            if (!"".equals(ServerWhitelistManager.a_clash136(var6, var4))) {
               var3.add(var6);
            } else {
               HashSet var7 = ServerWhitelistManager.a_clash139(var6);
               if (var7 == null) {
                  var3.add(var6);
               } else if (!var7.isEmpty() && !var7.contains(var2)) {
                  var3.add(var6);
               }
            }
         }

         if (!var3.isEmpty()) {
            var1.removeAll(var3);
            this.f_clash439(a_clash560(var1));
         }
      }
   }

   protected void l_clash514() {
      fp var1 = this.y_clash492();
      if (++var1.ticksPlaying[this.field_70170_p.field_72995_K ? 1 : 0] >= var1.length) {
         if (var1.followUp != null && !this.field_70170_p.field_72995_K) {
            this.b(var1.followUp);
         }
      }
   }

   protected void k_clash515() {
      Path var1 = this.func_70661_as().func_75505_d();
      if (var1 != null) {
         if (!this.field_70122_E && !this.func_70090_H()) {
            int var2 = var1.func_75873_e();
            int var3 = var1.func_75874_d();
            if (var3 != var2 && var3 - 1 != var2) {
               PathPoint var4 = var1.func_75877_a(var2);
               PathPoint var5 = var1.func_75877_a(var2 + 1);
               Vec3d var6 = new Vec3d(var5.field_75839_a - var4.field_75839_a, var5.field_75837_b - var4.field_75837_b, var5.field_75838_c - var4.field_75838_c);
               this.field_70159_w = var6.field_72450_a / 7.0;
               this.field_70179_y = var6.field_72449_c / 7.0;
            }
         }
      }
   }

   public void g_clash238() {
   }

   @SideOnly(Side.CLIENT)
   public boolean b_clash230(EntityPlayer var1) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   protected static void a_clash516(EntityPlayer var0, BaseGirlEntity var1) {
      Minecraft.func_71410_x().func_147108_a(new GirlInventoryScreen(var1, var0));
   }

   @SideOnly(Side.CLIENT)
   protected static void a(EntityPlayer var0, BaseGirlEntity var1, String[] var2, ItemStack[] var3, boolean var4) {
      Minecraft.func_71410_x().func_147108_a(new GirlInventoryScreen(var1, var0, var2, var3, var4));
   }

   @SideOnly(Side.CLIENT)
   protected static void a(EntityPlayer var0, BaseGirlEntity var1, String[] var2, boolean var3) {
      Minecraft.func_71410_x().func_147108_a(new GirlInventoryScreen(var1, var0, var2, null, var3));
   }

   public void a_clash517(ItemStack var1) {
      this.field_184627_bm = var1;
   }

   public void d(int var1) {
      this.field_184628_bn = var1;
   }

   public Vec3d M_clash518() {
      return new Vec3d(this.field_70169_q, this.field_70167_r, this.field_70166_s);
   }

   protected static Vec3d a_clash519(BaseGirlEntity var0) {
      return new Vec3d(var0.field_70169_q, var0.field_70167_r, var0.field_70166_s);
   }

   public BaseGirlEntity af_clash520() {
      return this;
   }

   public void x_clash475() {
      if (this.field_70170_p.field_72995_K) {
         this.a_clash490("master", "");
         this.a_clash490("walk speed", BaseGirlEntity.BaseGirlEntityState.WALK.toString());
      } else {
         this.m.func_187227_b(v, "");
         this.m.func_187227_b(a, BaseGirlEntity.BaseGirlEntityState.WALK.toString());
      }
   }

   protected void a(EntityPlayerMP var1, boolean var2) {
      var1.field_70159_w = 0.0;
      var1.field_70181_x = 0.0;
      var1.field_70179_y = 0.0;
      if (var2) {
         Vec3d var3 = this.a_clash546(0.35);
         var1.func_70634_a(var3.field_72450_a, var3.field_72448_b, var3.field_72449_c);
      }
   }

   public void j_clash521(UUID var1) {
      EntityPlayer var2 = this.field_70170_p.func_152378_a(var1);
      var2.field_70159_w = 0.0;
      var2.field_70181_x = 0.0;
      var2.field_70179_y = 0.0;
      Vec3d var3 = this.a_clash546(0.35);
      var2.func_70634_a(var3.field_72450_a, var3.field_72448_b, var3.field_72449_c);
      this.b_clash431(var2.field_70759_as + 180.0F);
   }

   protected void a(boolean var1, boolean var2, UUID var3) {
      if (this.field_70170_p.field_72995_K) {
         PacketHandler.b.sendToServer(new KoboldStatePacket(this.f_clash491(), var3, var1, var2));
      } else {
         KoboldStatePacket.Handler.a(this.f_clash491(), var3, var1, var2);
      }
   }

   public static BaseGirlEntity b_clash522(UUID var0) {
      if (var0 == null) {
         return null;
      }

      for (BaseGirlEntity var2 : g_clash524(var0)) {
         if (var2.field_70170_p.field_72995_K) {
            return var2;
         }
      }

      return null;
   }

   public static BaseGirlEntity a_clash523(UUID var0) {
      if (var0 == null) {
         return null;
      }

      for (BaseGirlEntity var2 : g_clash524(var0)) {
         if (!var2.field_70170_p.field_72995_K) {
            return var2;
         }
      }

      return null;
   }

   public static ArrayList<BaseGirlEntity> g_clash524(UUID var0) {
      ArrayList var1 = new ArrayList();

      try {
         for (BaseGirlEntity var3 : ad_clash509()) {
            if (var3 != null && var3.f_clash491().equals(var0)) {
               var1.add(var3);
            }
         }
      } catch (ConcurrentModificationException var4) {
         System.out.println("had a ConcurrentModificationException while cycling through the girl list... hopefully nothin borke owo");
         var4.printStackTrace();
      }

      return var1;
   }

   protected BlockPos a_clash525(BlockPos var1) {
      return this.a(var1, 1);
   }

   public BlockPos a(BlockPos var1, int var2) {
      return this.a(var1, var2, Blocks.field_150324_C, 22, 3, null);
   }

   public void W() {
      this.m.func_187227_b(field_184621_as, Byte.valueOf("1"));
   }

   public void K() {
      this.m.func_187227_b(field_184621_as, Byte.valueOf("0"));
   }

   public BlockPos a(BlockPos var1, int var2, Block var3, int var4, int var5, @Nullable HashSet<Biome> var6) {
      int var7 = 1;
      byte var8 = -1;
      BlockPos var9 = var1;
      int var10 = 0;

      while (var7 < var4) {
         for (int var11 = 0; var11 < 2; var11++) {
            var8 *= -1;

            for (int var12 = 0; var12 < var7; var12++) {
               var9 = var9.func_177982_a(0, 0, var8);

               for (int var13 = -var5; var13 < var5 + 1; var13++) {
                  if (this.field_70170_p.func_180495_p(var9.func_177982_a(0, var13, var8)).func_177230_c() == var3) {
                     var10++;
                     if (var10 >= var2 && (var6 == null || var6.contains(this.field_70170_p.func_180494_b(var9.func_177982_a(var8, var13, 0))))) {
                        return var9.func_177982_a(0, var13, var8);
                     }
                  }
               }
            }

            for (int var14 = 0; var14 < var7; var14++) {
               var9 = var9.func_177982_a(var8, 0, 0);

               for (int var15 = -var5; var15 < var5 + 1; var15++) {
                  if (this.field_70170_p.func_180495_p(var9.func_177982_a(var8, var15, 0)).func_177230_c() == var3) {
                     var10++;
                     if (var10 >= var2 && (var6 == null || var6.contains(this.field_70170_p.func_180494_b(var9.func_177982_a(var8, var15, 0))))) {
                        return var9.func_177982_a(var8, var15, 0);
                     }
                  }
               }
            }

            var7++;
         }
      }

      return null;
   }

   protected List<BlockPos> a(BlockPos var1, Class var2, int var3, int var4, @Nullable HashSet<Biome> var5) {
      int var6 = 1;
      byte var7 = -1;
      BlockPos var8 = var1;
      ArrayList var9 = new ArrayList();

      while (var6 < var3) {
         for (int var10 = 0; var10 < 2; var10++) {
            var7 *= -1;

            for (int var11 = 0; var11 < var6; var11++) {
               var8 = var8.func_177982_a(0, 0, var7);

               for (int var12 = -var4; var12 < var4 + 1; var12++) {
                  if (var2.isInstance(this.field_70170_p.func_180495_p(var8.func_177982_a(0, var12, var7)).func_177230_c())
                     && (var5 == null || var5.contains(this.field_70170_p.func_180494_b(var8.func_177982_a(var7, var12, 0))))) {
                     var9.add(var8.func_177982_a(0, var12, var7));
                  }
               }
            }

            for (int var13 = 0; var13 < var6; var13++) {
               var8 = var8.func_177982_a(var7, 0, 0);

               for (int var14 = -var4; var14 < var4 + 1; var14++) {
                  if (var2.isInstance(this.field_70170_p.func_180495_p(var8.func_177982_a(var7, var14, 0)).func_177230_c())
                     && (var5 == null || var5.contains(this.field_70170_p.func_180494_b(var8.func_177982_a(var7, var14, 0))))) {
                     var9.add(var8.func_177982_a(var7, var14, 0));
                  }
               }
            }

            var6++;
         }
      }

      return var9;
   }

   public boolean J_clash526() {
      return !((String)this.m.func_187225_a(v)).equals("");
   }

   @Nullable
   public UUID O_clash527() {
      String var1 = (String)this.m.func_187225_a(v);
      if ("".equals(var1)) {
         return null;
      }

      try {
         return UUID.fromString(var1);
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   @Nullable
   public EntityPlayer z_clash528() {
      UUID var1 = this.O_clash527();
      return var1 == null ? null : this.field_70170_p.func_152378_a(var1);
   }

   protected ResourceLocation func_184647_J() {
      return dz.d;
   }

   @SideOnly(Side.CLIENT)
   public void a(String var1, UUID var2) {
   }

   @SideOnly(Side.CLIENT)
   protected abstract <E extends IAnimatable> PlayState a(AnimationEvent<E> var1);

   @SideOnly(Side.CLIENT)
   protected boolean a(fp var1, String var2, boolean var3, AnimationEvent var4) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   protected void a(String var1, boolean var2, AnimationEvent var3, boolean var4) {
      if (var4 || !fp.b_clash719(this, var3.getPartialTick()) || !this.a(this.y_clash492(), var1, d3.d, var3)) {
         ILoopType.EDefaultLoopTypes var5 = var2 ? ILoopType.EDefaultLoopTypes.LOOP : ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME;
         var3.getController().setAnimation(new AnimationBuilder().addAnimation(var1, var5));
         var3.getController().transitionLengthTicks = 0.0;
      }
   }

   @SideOnly(Side.CLIENT)
   protected void a(String var1, boolean var2, AnimationEvent var3) {
      this.a(var1, var2, var3, false);
   }

   @SideOnly(Side.CLIENT)
   protected void a(String var1, int var2, float var3, AnimationEvent var4, boolean var5) {
      if (var5 || !fp.b_clash719(this, var4.getPartialTick()) || !this.a(this.y_clash492(), var1, d3.d, var4)) {
         AnimationController var6 = var4.getController();
         Pair var7 = this.A.get(var1);
         if (var7 == null) {
            var7 = Pair.of(0, 0);
         }

         int var8 = (Integer)var7.first();
         int var9 = (Integer)var7.second();
         if (!fp.b_clash719(this, var4.getPartialTick())) {
            var4.getController().setAnimation(new AnimationBuilder().addAnimation(var8 == 0 ? var1 : var1 + var8, ILoopType.EDefaultLoopTypes.LOOP));
            var4.getController().transitionLengthTicks = 0.0;
         } else {
            int var10 = this.a(var8, var9, var2, var3);
            AnimationBuilder var12 = new AnimationBuilder();
            AnimationController var11 = var6;
            AnimationController var10000;
            AnimationBuilder var10001;
            String var10002;
            if (var10 == 0) {
               var10000 = var11;
               var10001 = var12;
               var10002 = var1;
            } else {
               var10000 = var11;
               var10001 = var12;
               var10002 = var1 + var10;
            }

            var10000.setAnimation(var10001.addAnimation(var10002, ILoopType.EDefaultLoopTypes.LOOP));
            var6.transitionLengthTicks = 0.0;
            HashMap var16 = this.A;
            Integer var15 = var10;
            String var14 = var1;
            HashMap var13 = var16;
            HashMap var17;
            String var18;
            Integer var19;
            int var10003;
            if (var10 == 0) {
               var17 = var13;
               var18 = var14;
               var19 = var15;
               var10003 = var9;
            } else {
               var17 = var13;
               var18 = var14;
               var19 = var15;
               var10003 = var10;
            }

            var17.put(var18, Pair.of(var19, var10003));
         }
      }
   }

   @SideOnly(Side.CLIENT)
   protected void a(String var1, int var2, float var3, AnimationEvent var4) {
      this.a(var1, var2, var3, var4, false);
   }

   int a(int var1, int var2, int var3, float var4) {
      if (var1 != 0) {
         return 0;
      }

      Random var5 = this.func_70681_au();
      if (var5.nextFloat() > var4) {
         return 0;
      }

      int var6;
      do {
         var6 = var5.nextInt(var3);
      } while ((var6 == var2 || var6 == 0) && var3 > 2);

      return var6;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public abstract void registerControllers(AnimationData var1);

   protected void s() {
      if (this.field_70170_p.field_72995_K && this.n_clash537()) {
         this.B = null;
         PacketHandler.b.sendToServer(new ResetGirlPacket(this.f_clash491(), true));
      } else if (!this.field_70170_p.field_72995_K) {
         ResetGirlPacket.Handler.a((EntityPlayerMP)this.field_70170_p.func_152378_a(this.ae_clash498()));
      }
   }

   public static BaseGirlEntity c_clash529(EntityPlayer var0) {
      return var0 == null ? null : i_clash530(var0.getPersistentID());
   }

   @SideOnly(Side.CLIENT)
   public Vec3d a(Minecraft var1, SexSceneEntity var2, EntityLivingBase var3, float var4) {
      return SexSceneRenderer.a(var1, var2, var3, this, var4);
   }

   public static BaseGirlEntity i_clash530(@Nonnull UUID var0) {
      return a(var0, (Boolean)null);
   }

   public static BaseGirlEntity a(@Nonnull UUID var0, Boolean var1) {
      try {
         for (BaseGirlEntity var3 : ad_clash509()) {
            if (!var3.field_70128_L && var0.equals(var3.ae_clash498())) {
               if (var1 == null) {
                  return var3;
               }

               boolean var4 = var3.field_70170_p.field_72995_K;
               if (var4 && !var1) {
                  return var3;
               }

               if (!var4 && var1) {
                  return var3;
               }
            }
         }
      } catch (ConcurrentModificationException var5) {
      }

      return null;
   }

   @Nullable
   public static BaseGirlEntity c_clash531(@Nonnull UUID var0) {
      boolean var1 = FMLCommonHandler.instance().getMinecraftServerInstance() == null;

      try {
         for (BaseGirlEntity var3 : ad_clash509()) {
            if (!var3.field_70128_L) {
               boolean var4 = var3.field_70170_p.field_72995_K;
               if (var4 == var1 && var0.equals(var3.ae_clash498())) {
                  return var3;
               }
            }
         }
      } catch (ConcurrentModificationException var5) {
      }

      return null;
   }

   public static BaseGirlEntity d_clash532(@Nonnull EntityPlayer var0) {
      return c_clash531(var0.getPersistentID());
   }

   @SideOnly(Side.CLIENT)
   public void ac() {
   }

   public void r_clash533() {
      this.B = null;
      this.func_189654_d(false);
      this.b((fp)null);
      if (this.field_70170_p.field_72995_K) {
         this.V();
      }
   }

   @SideOnly(Side.CLIENT)
   protected void V() {
      if (this.n_clash537()) {
         d3.a_clash122(true);
         Minecraft.func_71410_x().field_71439_g.func_82142_c(false);
         PacketHandler.b.sendToServer(new ResetGirlPacket(this.f_clash491()));
      }
   }

   @SideOnly(Side.CLIENT)
   public static void k(UUID var0) {
      try {
         for (BaseGirlEntity var2 : ad_clash509()) {
            UUID var3 = var2.ae_clash498();
            if (var3 != null && var3.equals(var0)) {
               fp var4 = var2.c_clash235(var2.y_clash492());
               if (var4 == null) {
                  return;
               }

               var2.b(var4);
               return;
            }
         }
      } catch (ConcurrentModificationException var5) {
      }
   }

   @SideOnly(Side.CLIENT)
   public static void f_clash534(UUID var0) {
      try {
         for (BaseGirlEntity var2 : ad_clash509()) {
            if (!var2.field_70128_L && var2.field_70170_p.field_72995_K) {
               UUID var3 = var2.ae_clash498();
               if (var3 != null && var3.equals(var0)) {
                  fp var4 = var2.a_clash236(var2.y_clash492());
                  if (var4 != null) {
                     var2.b(var4);
                  }
               }
            }
         }
      } catch (ConcurrentModificationException var5) {
      }
   }

   public void N() {
      this.ag();
      PacketHandler.b.sendToServer(new ResetControllerPacket(this.f_clash491()));
   }

   @SideOnly(Side.CLIENT)
   public void ag() {
      this.C.tickOffset = 0.0;
   }

   @SideOnly(Side.CLIENT)
   @Nullable
   protected abstract fp c_clash235(fp var1);

   @SideOnly(Side.CLIENT)
   protected abstract fp a_clash236(fp var1);

   public TargetPoint P_clash535() {
      return new TargetPoint(this.field_71093_bK, this.field_70165_t, this.field_70163_u, this.field_70161_v, 50.0);
   }

   protected void a_clash536(double var1, double var3, double var5, float var7, float var8) {
      if (this.ae_clash498() == null) {
         System.out.println("couldnt move camera because the player isn't set");
      } else {
         EntityPlayer var9 = this.field_70170_p.func_152378_a(this.ae_clash498());
         if (this.B == null) {
            this.B = var9.func_174791_d();
         }

         Vec3d var10 = this.B;
         var10 = var10.func_72441_c(-Math.sin((this.r + 90.0F) * (Math.PI / 180.0)) * var1, 0.0, Math.cos((this.r + 90.0F) * (Math.PI / 180.0)) * var1);
         var10 = var10.func_72441_c(0.0, var3, 0.0);
         var10 = var10.func_72441_c(-Math.sin(this.r * (Math.PI / 180.0)) * var5, 0.0, Math.cos(this.r * (Math.PI / 180.0)) * var5);
         if (this.field_70170_p.field_72995_K) {
            PacketHandler.b.sendToServer(new TeleportPlayerPacket(var9.getPersistentID().toString(), var10, this.r + var7, var8));
         } else {
            var9.func_70080_a(var10.field_72450_a, var10.field_72448_b, var10.field_72449_c, this.r + var7, var8);
            var9.func_70634_a(var10.field_72450_a, var10.field_72448_b, var10.field_72449_c);
            this.field_70159_w = 0.0;
            this.field_70181_x = 0.0;
            this.field_70179_y = 0.0;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean n_clash537() {
      if (!this.field_70170_p.field_72995_K) {
         return false;
      }

      EntityPlayerSP var1 = Minecraft.func_71410_x().field_71439_g;
      return var1.getPersistentID().equals(this.ae_clash498()) || var1.func_110124_au().equals(this.ae_clash498());
   }

   protected void U() {
   }

   public void g_clash538(String var1) {
      this.m.func_187227_b(c, var1);
   }

   public String w_clash539() {
      return (String)this.m.func_187225_a(c);
   }

   public abstract String c_clash241();

   public String ab_clash540() {
      String var1 = (String)this.m.func_187225_a(c);
      return !"".equals(var1) ? var1 : this.c_clash241();
   }

   public abstract float i_clash226();

   @SideOnly(Side.CLIENT)
   public boolean t_clash283() {
      return true;
   }

   public void h(String var1) {
      if (!this.field_70170_p.field_72995_K) {
         PacketHandler.b
            .sendToAllAround(
               new SendChatMessagePacket(String.format("<%s> %s", this.ab_clash540(), var1), this.field_71093_bK, this.f_clash491()),
               new TargetPoint(this.field_71093_bK, this.field_70165_t, this.field_70163_u, this.field_70161_v, 40.0)
            );
      } else if (this.n_clash537()) {
         PacketHandler.b.sendToServer(new SendChatMessagePacket(String.format("<%s> %s", this.ab_clash540(), var1), this.field_71093_bK, this.f_clash491()));
      }
   }

   protected void b(String var1, boolean var2) {
      if (!var2) {
         this.h(var1);
      }

      if (!this.field_70170_p.field_72995_K) {
         PacketHandler.b
            .sendToAllAround(
               new SendChatMessagePacket(var1, this.field_71093_bK, this.f_clash491()),
               new TargetPoint(this.field_71093_bK, this.field_70165_t, this.field_70163_u, this.field_70161_v, 40.0)
            );
      } else {
         if (this.n_clash537()) {
            PacketHandler.b.sendToServer(new SendChatMessagePacket(var1, this.field_71093_bK, this.f_clash491()));
         }
      }
   }

   protected void a_clash541(String var1) {
      if (this.field_70170_p.field_72995_K) {
         Minecraft.func_71410_x().field_71439_g.func_145747_a(new TextComponentString(String.format("<%s> %s", this.ab_clash540(), var1)));
      }
   }

   protected void a(UUID var1, String var2) {
      EntityPlayer var3 = this.field_70170_p.func_152378_a(var1);
      if (var3 == null) {
         System.out.println("Player with UUID " + var1.toString() + " not found");
      } else {
         if (this.field_70170_p.field_72995_K) {
            Minecraft.func_71410_x().field_71439_g.func_145747_a(new TextComponentString("<" + var3.func_70005_c_() + "> " + var2));
         }
      }
   }

   public void a(SoundEvent var1, float var2, float var3) {
      this.field_70170_p
         .func_184134_a(
            this.func_180425_c().func_177958_n(),
            this.func_180425_c().func_177956_o(),
            this.func_180425_c().func_177952_p(),
            var1,
            SoundCategory.NEUTRAL,
            var2,
            var3,
            false
         );
   }

   public void a(SoundEvent var1) {
      this.a(var1, 1.0F, 1.0F);
   }

   public void a(SoundEvent[] var1, int... var2) {
      if (var2.length == 0) {
         this.a(var1[this.func_70681_au().nextInt(var1.length)]);
      } else {
         this.a(var1[var2[this.func_70681_au().nextInt(var2.length)]], 1.0F, 1.0F);
      }
   }

   public void a(SoundEvent[] var1, float var2) {
      this.a(var1[this.func_70681_au().nextInt(var1.length)], var2, 1.0F);
   }

   public void a(SoundEvent var1, float var2) {
      this.a(var1, var2, 1.0F);
   }

   public static boolean a_clash542(Entity var0) {
      if (var0 == null) {
         return false;
      } else {
         return !(var0 instanceof BaseGirlEntity) ? false : !(var0 instanceof AbstractPlayerGirlEntity);
      }
   }

   @SideOnly(Side.CLIENT)
   public BaseGirlEntity E_clash543() {
      return this;
   }

   @SideOnly(Side.CLIENT)
   public boolean e_clash544() {
      EntityPlayer var1 = this.field_70170_p.func_72890_a(this, 50.0);
      return var1 == null ? false : var1.getPersistentID().equals(Minecraft.func_71410_x().field_71439_g.getPersistentID());
   }

   public Vec3d aa_clash545() {
      return this.a_clash546(1.0);
   }

   public Vec3d a_clash546(double var1) {
      EntityPlayer var3 = this.field_70170_p.func_152378_a(this.ae_clash498());
      float var4 = var3.field_70177_z;
      return var3.func_174791_d().func_72441_c(-Math.sin(var4 * (Math.PI / 180.0)) * var1, 0.0, Math.cos(var4 * (Math.PI / 180.0)) * var1);
   }

   public Vec3d a_clash432(Vec3d var1, float var2) {
      return var1;
   }

   public static void a(EnumParticleTypes var0, BaseGirlEntity var1) {
      double var2 = Reference.f.nextGaussian() * 0.02;
      double var4 = Reference.f.nextGaussian() * 0.02;
      double var6 = Reference.f.nextGaussian() * 0.02;
      var1.field_70170_p
         .func_175688_a(
            var0,
            var1.field_70165_t + Reference.f.nextFloat() * var1.field_70130_N * 2.0F - var1.field_70130_N,
            var1.field_70163_u + 0.5 + Reference.f.nextFloat() * var1.field_70131_O,
            var1.field_70161_v + Reference.f.nextFloat() * var1.field_70130_N * 2.0F - var1.field_70130_N,
            var2,
            var4,
            var6,
            new int[0]
         );
   }

   public static void a(EnumParticleTypes var0, BaseGirlEntity var1, int var2) {
      for (int var3 = 0; var3 < var2; var3++) {
         a(var0, var1);
      }
   }

   @Override
   public AnimationFactory getFactory() {
      return this.g;
   }

   public boolean func_70104_M() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   protected SoundEvent func_184639_G() {
      if (this.func_70681_au().nextInt(10000) == 0) {
         if (this.field_70170_p.field_72995_K && Minecraft.func_71410_x().field_71439_g.func_174791_d().func_72438_d(this.func_174791_d()) < 10.0) {
            this.a_clash541("whopa");
         }

         return SoundHandler.a_clash804(SoundHandler.MISC_FART);
      } else {
         return null;
      }
   }

   public float T_clash293() {
      return 0.0F;
   }

   public float ai_clash294() {
      return 0.0F;
   }

   @SideOnly(Side.CLIENT)
   public MatrixStack a(String var1, boolean var2) {
      if (this.H == null) {
         this.H = this.b_clash552();
      }

      IBone var3 = this.H.getBone(var1);
      if (var3 == null) {
         if (!GirlModel.e.contains(var1)) {
            Main.LOGGER.log(Level.WARN, String.format("The bone '%s' does not exist on %s. Bone model matrix couldn't be calculated", var1, this.c_clash241()));
            this.p.remove(var1);
         }

         return new MatrixStack();
      } else {
         GeoBone var4 = (GeoBone)var3;
         ArrayList var5 = new ArrayList();
         GeoBone var6 = var4;

         while (var6.parent != null) {
            GeoBone var7 = var6.parent;
            var5.add(var7);
            var6 = var7;
         }

         Collections.reverse(var5);
         MatrixStack var9 = new MatrixStack();
         if (this.Q_clash505()) {
            var9.rotateY((float)(-Math.toRadians(this.I_clash415().floatValue())));
         } else if (var2) {
            var9.rotateY(
               (float)(-Math.toRadians(RotationHelper.a_clash25(this.field_70760_ar, this.field_70761_aq, Minecraft.func_71410_x().func_184121_ak())))
            );
         }

         for (GeoBone var8 : (java.util.Collection<GeoBone>) (var5) ) {
            var9.translate(var8);
            var9.moveToPivot(var8);
            var9.rotate(var8);
            var9.scale(var8);
            var9.moveBackFromPivot(var8);
         }

         var9.translate(var4);
         var9.moveToPivot(var4);
         var9.rotate(var4);
         var9.scale(var4);
         return this.a(var9);
      }
   }

   protected MatrixStack a(MatrixStack var1) {
      return var1;
   }

   @SideOnly(Side.CLIENT)
   public Vec3d b_clash547(String var1) {
      Vec3d var2 = this.x.get(var1);
      if (var2 != null) {
         return var2;
      }

      if (!this.p.contains(var1)) {
         this.p.add(var1);
      }

      return Vec3d.field_186680_a;
   }

   @SideOnly(Side.CLIENT)
   public Vec3d d_clash548(String var1) {
      return this.b_clash547(var1).func_178787_e(this.func_174791_d());
   }

   public void a(String var1, Vec3d var2) {
      this.x.put(var1, var2);
   }

   @SideOnly(Side.CLIENT)
   public float R_clash549() {
      AnimationProcessor var1 = this.b_clash552();
      IBone var2 = var1.getBone("girlCam");
      if (var2 == null) {
         return 0.0F;
      }

      float var3 = var2.getPivotY();
      var3 = this.a_clash356(var3);
      return var3 / 16.0F;
   }

   @SideOnly(Side.CLIENT)
   public float v_clash550() {
      return 1.0F;
   }

   protected float a_clash356(float var1) {
      return var1;
   }

   public AnimatedGeoModel<? extends BaseGirlEntity> a_clash551() {
      Minecraft var1 = Minecraft.func_71410_x();
      Render var2 = var1.func_175598_ae().func_78713_a(this);
      if (var2 == null) {
         return null;
      } else if (!(var2 instanceof GirlRenderer)) {
         return null;
      } else {
         GeoEntityRenderer var3 = (GeoEntityRenderer)var2;
         GeoModelProvider var4 = var3.getGeoModelProvider();
         if (var4 == null) {
            return null;
         } else {
            return !(var4 instanceof AnimatedGeoModel) ? null : (AnimatedGeoModel)var4;
         }
      }
   }

   public AnimationProcessor<?> b_clash552() {
      return this.a_clash551().getAnimationProcessor();
   }

   public boolean h(int var1) {
      ArrayList var2 = this.D_clash243();
      return var2.size() - 1 < var1 ? false : (Integer)var2.get(var1) == 101;
   }

   public e1 g(int var1) {
      return e1.a;
   }

   public void a_clash245(List<Integer> var1) {
      if (this instanceof AbstractNpcOnlyEntity || this instanceof AbstractKoboldPlayerEntity) {
         StringBuilder var2 = new StringBuilder();

         for (int var4 : var1) {
            AbstractNpcOnlyEntity.c(var2, var4);
         }

         this.m.func_187227_b(AbstractNpcOnlyEntity.M, var2.toString());
      }
   }

   public String F_clash553() {
      return !(this instanceof AbstractNpcOnlyEntity) && !(this instanceof AbstractKoboldPlayerEntity)
         ? ""
         : (String)this.m.func_187225_a(AbstractNpcOnlyEntity.M);
   }

   public static String c(List<Integer> var0) {
      StringBuilder var1 = new StringBuilder();

      for (int var3 : var0) {
         var1.append(var3);
         var1.append("-");
      }

      return var1.toString();
   }

   public static List<Integer> c_clash554(String var0) {
      ArrayList var1 = new ArrayList();
      String[] var2 = var0.split("-");

      for (String var6 : var2) {
         var1.add(Integer.parseInt(var6));
      }

      return var1;
   }

   public static List<Integer> h_clash555(UUID var0) {
      BaseGirlEntity var1 = null instanceof ClientProxy ? b_clash522(var0) : a_clash523(var0);
      ArrayList var2 = new ArrayList<>(var1.L_clash353());
      if (var1 instanceof AbstractNpcOnlyEntity || var1 instanceof AbstractKoboldPlayerEntity) {
         var2.addAll(c_clash554((String)var1.func_184212_Q().func_187225_a(AbstractNpcOnlyEntity.M)));
      }

      return var2;
   }

   public ArrayList<Integer> L_clash353() {
      return new ArrayList<>();
   }

   public List<Entry<BoneType, Entry<List<String>, Integer>>> d_clash556(UUID var1) {
      if (this.d != null) {
         return this.d;
      }

      ArrayList var2 = this.D_clash243();
      if (var2.isEmpty()) {
         this.d = new ArrayList<>();
         return this.d;
      }

      ArrayList var3 = new ArrayList();
      List var4 = h_clash555(var1);

      for (int var5 = 0; var5 < var2.size(); var5++) {
         var3.add(new SimpleEntry<>(BoneType.GIRL_SPECIFIC, new SimpleEntry<>(this.e((Integer)var2.get(var5)), var4.get(var5))));
      }

      this.d = var3;
      return var3;
   }

   public void b(List<Entry<BoneType, Entry<List<String>, Integer>>> var1) {
      this.d = var1;
   }

   public void a_clash557(int var1, int var2) {
      if (this.d != null) {
         if (this.d.size() - 1 >= var1) {
            Entry var3 = this.d.get(var1);
            ((Entry)var3.getValue()).setValue(var2);
            this.d.set(var1, var3);
         }
      }
   }

   public void e_clash558(String var1) {
      if (this instanceof AbstractNpcOnlyEntity || this instanceof AbstractKoboldPlayerEntity) {
         this.m.func_187227_b(AbstractNpcOnlyEntity.M, var1);
      }
   }

   private List<String> e(int var1) {
      ArrayList var2 = new ArrayList();

      for (int var3 = 0; var3 < var1; var3++) {
         var2.add("");
      }

      return var2;
   }

   public ArrayList<Integer> D_clash243() {
      return new ArrayList<>();
   }

   public List<Integer> u_clash244() {
      return new ArrayList<>();
   }

   public void f_clash439(String var1) {
      this.m.func_187227_b(b, var1);
   }

   public String C_clash559() {
      return (String)this.m.func_187225_a(b);
   }

   public static String a_clash560(HashSet<String> var0) {
      if (var0 == null) {
         return "";
      }

      if (var0.isEmpty()) {
         return "";
      }

      StringBuilder var1 = new StringBuilder();

      for (String var3 : var0) {
         var1.append(var3);
         var1.append("#");
      }

      return var1.toString();
   }

   public HashSet<String> Y_clash561() {
      String var1 = this.C_clash559();
      String[] var2 = var1.split("#");
      HashSet var3 = new HashSet();

      for (String var7 : var2) {
         if (!"".equals(var7) && !"cross".equals(var7)) {
            var3.add(var7);
         }
      }

      return var3;
   }

   @SideOnly(Side.CLIENT)
   public boolean H_clash562() {
      return true;
   }

   public enum BaseGirlEntityState {
      WALK,
      FAST_WALK,
      RUN;
   }
}
