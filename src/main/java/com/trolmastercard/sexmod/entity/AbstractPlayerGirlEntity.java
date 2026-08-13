package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.ao;
import com.trolmastercard.sexmod.client.gui.GenderSwapScreen;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.ForcePlayerGirlUpdatePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.networking.SexPromptPacket;
import com.trolmastercard.sexmod.util.ad;
import com.trolmastercard.sexmod.util.af;
import com.trolmastercard.sexmod.util.ah;
import com.trolmastercard.sexmod.util.ak;
import com.trolmastercard.sexmod.util.am;
import com.trolmastercard.sexmod.util.an;
import com.trolmastercard.sexmod.util.d3;







import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector2f;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AbstractPlayerGirlEntity extends AbstractGirlNpcEntity {
   public static final String aa = "sexmod:CustomModel";
   public static final String ae = "sexmod:GirlSpecific";
   public static final float ac = 0.0F;
   public static final int am = 100;
   public static final int Y = 65;
   public static boolean ag = true;
   public Vector2f ao = new Vector2f(0.0F, 0.0F);
   public boolean ad = false;
   public boolean aj = false;
   public boolean ak = false;
   public boolean af = true;
   public boolean ah = false;
   protected static final DataParameter<Optional<UUID>> ai = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187203_m)
      .func_187156_b()
      .func_187161_a(118);
   public static Hashtable<UUID, AbstractPlayerGirlEntity> al = new Hashtable<>();
   public static List<AbstractPlayerGirlEntity> Z = new ArrayList<>();
   int an = -1;
   public boolean ab = true;

   protected AbstractPlayerGirlEntity(World var1) {
      super(var1);
      this.func_70105_a(0.01F, 0.01F);
      Z.add(this);
   }

   protected AbstractPlayerGirlEntity(World var1, UUID var2) {
      this(var1);
      this.m.func_187227_b(ai, Optional.of(var2));
   }

   @Nullable
   public static AbstractPlayerGirlEntity d_clash567(UUID var0) {
      return al.get(var0);
   }

   @Nullable
   public static AbstractPlayerGirlEntity g(@Nonnull EntityPlayer var0) {
      return al.get(var0.getPersistentID());
   }

   @Nullable
   public static AbstractPlayerGirlEntity a_clash568(UUID var0) {
      try {
         for (BaseGirlEntity var2 : ad_clash509()) {
            if (!var2.field_70170_p.field_72995_K && var2 instanceof AbstractPlayerGirlEntity) {
               AbstractPlayerGirlEntity var3 = (AbstractPlayerGirlEntity)var2;
               if (var0.equals(var3.m_clash583())) {
                  return var3;
               }
            }
         }
      } catch (ConcurrentModificationException var4) {
      }

      return null;
   }

   @Override
   public TargetPoint P_clash535() {
      return new TargetPoint(this.field_71093_bK, this.field_70165_t, this.field_70163_u - 0.0, this.field_70161_v, 50.0);
   }

   public void a(int var1, fp var2) {
      PacketHandler.b.sendToAllTracking(new ForcePlayerGirlUpdatePacket(this.m_clash583(), var1, var2), this.P_clash535());
   }

   public EntityPlayer c_clash452(EntityPlayer var1) {
      return var1;
   }

   public boolean z_clash454() {
      return true;
   }

   public Vec3d c(Vec3d var1, float var2) {
      return var1;
   }

   public boolean func_70067_L() {
      return false;
   }

   public boolean v_clash227() {
      return true;
   }

   public boolean q_clash569() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public void H_clash570() {
   }

   public boolean p_clash379() {
      return true;
   }

   public boolean a_clash571(String var1) {
      return false;
   }

   public boolean A_clash381() {
      return true;
   }

   @Override
   public String c_clash241() {
      if (((Optional)this.m.func_187225_a(ai)).isPresent()) {
         EntityPlayer var1 = this.field_70170_p.func_152378_a((UUID)((Optional)this.m.func_187225_a(ai)).get());
         if (var1 != null) {
            return var1.func_70005_c_();
         }
      }

      return "anonymous horny girl";
   }

   public void u_clash377() {
   }

   public abstract void b(String var1, UUID var2);

   public abstract IVanillaModel a_clash228(int var1);

   public abstract String c_clash229(int var1);

   public Vec3i b_clash357(int var1) {
      return new Vec3i(255, 255, 255);
   }

   @Override
   public boolean func_70104_M() {
      return false;
   }

   public boolean func_70058_J() {
      return true;
   }

   public boolean F_clash231() {
      return false;
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      this.m.func_187214_a(ai, Optional.absent());
   }

   @SideOnly(Side.CLIENT)
   public static void i_clash572() {
      AbstractPlayerGirlEntity var0 = d_clash567(Minecraft.func_71410_x().field_71439_g.getPersistentID());
      if (var0 != null) {
         var0.r_clash533();
      }
   }

   @Override
   public void r_clash533() {
      this.B = null;
      this.func_189654_d(false);
      if (this.field_70170_p.field_72995_K) {
         this.V();
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   protected void V() {
      if (this.n_clash537() || this.f_clash579()) {
         d3.a_clash122(true);
         EntityPlayerSP var1 = Minecraft.func_71410_x().field_71439_g;
         var1.func_82142_c(false);
         var1.func_189654_d(false);
         var1.field_70145_X = false;
         this.m.func_187227_b(G, false);
         PacketHandler.b.sendToServer(new ResetGirlPacket(this.f_clash491()));
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean H_clash562() {
      Minecraft var1 = Minecraft.func_71410_x();
      return !this.f_clash579() || var1.field_71474_y.field_74320_O != 0;
   }

   protected void c_clash573(boolean var1) {
      if (ag) {
         if (this.m_clash583() != null) {
            EntityPlayer var2 = this.field_70170_p.func_152378_a(this.m_clash583());
            if (var2 != null) {
               var2.field_71075_bZ.field_75101_c = var1;
               if (!var1) {
                  var2.field_71075_bZ.field_75100_b = false;
               }

               var2.func_71016_p();
            }
         }
      }
   }

   public static boolean e_clash574(UUID var0) {
      C_clash585();

      for (Entry var2 : al.entrySet()) {
         UUID var3 = (UUID)var2.getKey();
         if (var0.equals(var3)) {
            return true;
         }
      }

      return false;
   }

   public static boolean e(EntityPlayer var0) {
      return var0 == null ? false : e_clash574(var0.getPersistentID());
   }

   public AxisAlignedBB func_174813_aQ() {
      return super.func_174813_aQ().func_72317_d(0.0, 0.5, 0.0);
   }

   protected EntityPlayer j_clash575() {
      List var1 = this.field_70170_p.field_73010_i;
      EntityPlayer var2 = null;

      for (EntityPlayer var4 : (java.util.Collection<EntityPlayer>) (var1) ) {
         if (!var4.getPersistentID().equals(((Optional)this.m.func_187225_a(ai)).get())) {
            if (var2 == null) {
               var2 = var4;
            } else {
               double var5 = var2.func_70092_e(this.w_clash576().field_72450_a, this.w_clash576().field_72448_b, this.w_clash576().field_72449_c);
               double var7 = var4.func_70092_e(this.w_clash576().field_72450_a, this.w_clash576().field_72448_b, this.w_clash576().field_72449_c);
               if (var7 < var5) {
                  var2 = var4;
               }
            }
         }
      }

      return var2;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean e_clash544() {
      EntityPlayer var1 = this.j_clash575();
      return var1 == null ? false : var1.getPersistentID().equals(Minecraft.func_71410_x().field_71439_g.getPersistentID());
   }

   public Vec3d w_clash576() {
      return new Vec3d(this.field_70165_t, this.field_70163_u - 0.0, this.field_70161_v);
   }

   protected void b_clash577(UUID var1) {
      EntityPlayerMP var2 = (EntityPlayerMP)this.field_70170_p.func_152378_a(var1);
      EntityPlayerMP var3 = (EntityPlayerMP)this.field_70170_p.func_152378_a((UUID)((Optional)this.m.func_187225_a(ai)).get());
      PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), var2);
      PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), var3);
      this.e_clash499(var1);
      this.field_70177_z = 0.0F;
      this.field_70759_as = 0.0F;
      var2.field_70177_z = 180.0F;
      var2.field_70759_as = 180.0F;
      var2.func_189654_d(true);
      var2.field_70145_X = true;
      Vec3d var4 = this.func_174791_d();
      var2.func_70634_a(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c + 1.0);
      var2.field_71075_bZ.field_75100_b = true;
      var3.field_71075_bZ.field_75100_b = true;
      this.j_clash521(var1);
      this.m.func_187227_b(G, true);
      this.c_clash502(var4);
      this.b_clash431(0.0F);
   }

   protected void func_180429_a(BlockPos var1, Block var2) {
      super.func_180429_a(var1, var2);
   }

   public AxisAlignedBB a_clash352(EntityPlayer var1) {
      return var1.func_174813_aQ();
   }

   @Override
   public void func_70071_h_() {
      this.field_70145_X = true;
      this.func_189654_d(true);
      super.func_70071_h_();
      this.D_clash581();
      if (this.field_70170_p.field_72995_K) {
         if (this.f_clash579()) {
            GenderSwapScreen.a.a_clash861();
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void h_clash578() {
      Minecraft.func_71410_x().field_71439_g.eyeHeight = this.func_70047_e();
   }

   @SideOnly(Side.CLIENT)
   public boolean f_clash579() {
      return !((Optional)this.m.func_187225_a(ai)).isPresent()
         ? false
         : ((UUID)((Optional)this.m.func_187225_a(ai)).get()).equals(Minecraft.func_71410_x().field_71439_g.getPersistentID());
   }

   public boolean E_clash458() {
      return false;
   }

   void d_clash580(EntityPlayer var1) {
      NBTTagCompound var2 = var1.getEntityData();
      String var3 = var2.func_74779_i("sexmod:CustomModel" + NpcType.a_clash751(this));
      this.f_clash439(var3);
   }

   @Override
   public void func_70619_bc() {
      C_clash585();
      this.l_clash514();
      this.G();
      UUID var1 = this.m_clash583();
      if (var1 != null) {
         EntityPlayer var2 = this.field_70170_p.func_152378_a(var1);
         if (var2 == null) {
            this.func_70634_a(this.field_70165_t, 0.0, this.field_70161_v);
         } else {
            this.d_clash580(var2);
            if (this.Q_clash505()) {
               Vec3d var3 = this.o_clash501();
               this.func_70634_a(var3.field_72450_a, var3.field_72448_b, var3.field_72449_c);
            } else {
               this.func_70634_a(var2.field_70165_t, var2.field_70163_u + 0.0, var2.field_70161_v);
            }

            fp var4 = this.y_clash492();
            if (var4 == fp.NULL && var2.field_82175_bq) {
               this.b(fp.ATTACK);
            }

            if (var4 == fp.ATTACK && !var2.field_82175_bq) {
               this.b(fp.NULL);
            }
         }
      }
   }

   void D_clash581() {
      if (this.an != -1) {
         this.an++;
         if (!this.field_70170_p.field_72995_K && this.an == 65) {
            this.f(this.ah_clash493() == 0 ? 1 : 0);
         }

         if (this.an >= 100) {
            if (this.y_clash492() == fp.STRIP) {
               if (this.field_70170_p.field_72995_K) {
                  this.n_clash582();
               } else {
                  this.b(fp.NULL);
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void n_clash582() {
      if (this.f_clash579()) {
         Minecraft var1 = Minecraft.func_71410_x();
         var1.field_71474_y.field_74320_O = 0;
         var1.field_71460_t.func_175066_a(var1.func_175606_aa());
         d3.a_clash122(true);
      }
   }

   public boolean o_clash456() {
      return this.Q_clash505();
   }

   public Vec3d b(Vec3d var1, float var2) {
      return var1;
   }

   public boolean a(fp var1, EntityPlayer var2) {
      return false;
   }

   public boolean l_clash467() {
      return true;
   }

   public void b_clash468(EntityPlayer var1) {
   }

   @Override
   public void b(fp var1) {
      if (!this.field_70170_p.field_72995_K && var1 == fp.NULL && this.Q_clash505()) {
         System.out.println("prevented a potential animation break");
      } else {
         if (var1 == fp.STRIP) {
            this.an = this.field_70170_p.field_72995_K ? 5 : 0;
         }

         super.b(var1);
      }
   }

   public void f(EntityPlayer var1) {
      this.m.func_187227_b(X, ItemStack.field_190927_a);
      this.m.func_187227_b(T, ItemStack.field_190927_a);
      this.m.func_187227_b(U, ItemStack.field_190927_a);
      this.m.func_187227_b(W, ItemStack.field_190927_a);

      for (ItemStack var3 : var1.func_184193_aE()) {
         if (var3.func_77973_b() instanceof ItemElytra) {
            this.m.func_187227_b(T, var3);
         } else if (var3.func_77973_b() instanceof ItemArmor) {
            ItemArmor var4 = (ItemArmor)var3.func_77973_b();
            switch (var4.func_185083_B_()) {
               case HEAD:
                  this.m.func_187227_b(X, var3);
                  break;
               case CHEST:
                  this.m.func_187227_b(T, var3);
                  break;
               case LEGS:
                  this.m.func_187227_b(U, var3);
                  break;
               case FEET:
                  this.m.func_187227_b(W, var3);
            }
         }
      }
   }

   public UUID m_clash583() {
      return ((Optional)this.m.func_187225_a(ai)).isPresent() ? (UUID)((Optional)this.m.func_187225_a(ai)).get() : null;
   }

   @Nullable
   public EntityPlayer k_clash584() {
      UUID var1 = this.m_clash583();
      return var1 == null ? null : this.field_70170_p.func_152378_a(var1);
   }

   public void a(Optional<UUID> var1) {
      this.m.func_187227_b(ai, var1);
   }

   public void y_clash234() {
   }

   public void B_clash233() {
   }

   public static void C_clash585() {
      ArrayList var0 = new ArrayList();

      try {
         for (AbstractPlayerGirlEntity var2 : Z) {
            if (var2.m_clash583() != null) {
               al.put(var2.m_clash583(), var2);
               var0.add(var2);
            }
         }
      } catch (ConcurrentModificationException var3) {
      }

      for (AbstractPlayerGirlEntity var5 : (java.util.Collection<AbstractPlayerGirlEntity>) (var0) ) {
         Z.remove(var5);
      }

      t_clash586();
   }

   static void t_clash586() {
      ArrayList var0 = new ArrayList();

      for (Entry var2 : al.entrySet()) {
         if (((AbstractPlayerGirlEntity)var2.getValue()).field_70128_L) {
            var0.add(var2.getKey());
         }
      }

      for (UUID var4 : (java.util.Collection<UUID>) (var0) ) {
         al.remove(var4);
      }
   }

   protected boolean c_clash587(UUID var1) {
      if (var1 == null) {
         return false;
      }

      AbstractPlayerGirlEntity var2 = d_clash567(var1);
      return var2 != null;
   }

   @Override
   public void a(String var1, UUID var2) {
      if (!this.a_clash571(var1)) {
         if (((Optional)this.m.func_187225_a(ai)).isPresent()) {
            PacketHandler.b.sendToServer(new SexPromptPacket(var1, var2, (UUID)((Optional)this.m.func_187225_a(ai)).get(), this.ab));
            this.ab = true;
         }
      }
   }

   @Override
   public void func_70014_b(NBTTagCompound var1) {
      super.func_70014_b(var1);
      var1.func_74778_a("owner", ((UUID)((Optional)this.m.func_187225_a(ai)).get()).toString());
   }

   @Override
   public void func_70037_a(NBTTagCompound var1) {
      super.func_70037_a(var1);
      this.m.func_187227_b(ai, Optional.of(UUID.fromString(var1.func_74779_i("owner"))));
      Z.add(this);
   }

   @Override
   public void a(SoundEvent var1, float var2, float var3) {
      Vec3d var4 = this.w_clash576();
      if (this.field_70170_p.field_72995_K) {
         this.field_70170_p.func_184134_a(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c, var1, SoundCategory.NEUTRAL, var2, var3, false);
      } else {
         this.field_70170_p
            .func_184133_a(null, new BlockPos(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c), var1, SoundCategory.PLAYERS, var2, var3);
      }
   }

   @Override
   public void a(SoundEvent var1) {
      this.a(var1, 1.0F, 1.0F);
   }

   public void a_clash588(SoundEvent[] var1) {
      this.a(var1[this.func_70681_au().nextInt(var1.length)], 1.0F, 1.0F);
   }

   @Override
   public void a(SoundEvent var1, float var2) {
      this.a(var1, var2, 1.0F);
   }

   @Override
   protected void U() {
   }

   private static ConcurrentModificationException a(ConcurrentModificationException var0) {
      return var0;
   }
}
