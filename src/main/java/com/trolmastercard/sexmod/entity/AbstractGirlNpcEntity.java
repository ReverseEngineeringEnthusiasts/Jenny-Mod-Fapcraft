package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.entity.ai.GirlFollowGoal;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.PlayerActionPacket;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SetNewHomePacket;
import com.trolmastercard.sexmod.util.Reference;







import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public abstract class AbstractGirlNpcEntity extends BaseGirlEntity {
   public int S = 1;
   public int P;
   public int O = 0;
   public int K;
   public Vec3d V = Vec3d.field_186680_a;
   public boolean N;
   public ItemStackHandler Q = new ItemStackHandler(7);
   public static final DataParameter<ItemStack> L = EntityDataManager.func_187226_a(AbstractGirlNpcEntity.class, DataSerializers.field_187196_f)
      .func_187156_b()
      .func_187161_a(117);
   public static final DataParameter<ItemStack> R = EntityDataManager.func_187226_a(AbstractGirlNpcEntity.class, DataSerializers.field_187196_f)
      .func_187156_b()
      .func_187161_a(116);
   public static final DataParameter<ItemStack> X = EntityDataManager.func_187226_a(AbstractGirlNpcEntity.class, DataSerializers.field_187196_f)
      .func_187156_b()
      .func_187161_a(115);
   public static final DataParameter<ItemStack> T = EntityDataManager.func_187226_a(AbstractGirlNpcEntity.class, DataSerializers.field_187196_f)
      .func_187156_b()
      .func_187161_a(114);
   public static final DataParameter<ItemStack> U = EntityDataManager.func_187226_a(AbstractGirlNpcEntity.class, DataSerializers.field_187196_f)
      .func_187156_b()
      .func_187161_a(113);
   public static final DataParameter<ItemStack> W = EntityDataManager.func_187226_a(AbstractGirlNpcEntity.class, DataSerializers.field_187196_f)
      .func_187156_b()
      .func_187161_a(112);
   public static final DataParameter<Integer> M = EntityDataManager.func_187226_a(AbstractGirlNpcEntity.class, DataSerializers.field_187192_b)
      .func_187156_b()
      .func_187161_a(111);

   protected AbstractGirlNpcEntity(World var1) {
      super(var1);
      if (this.Q.getStackInSlot(0) == ItemStack.field_190927_a) {
         this.Q.setStackInSlot(0, new ItemStack(Items.field_151040_l));
      }

      if (this.Q.getStackInSlot(1) == ItemStack.field_190927_a) {
         this.Q.setStackInSlot(1, new ItemStack(Items.field_151031_f));
      }
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      this.m.func_187214_a(M, 0);
      this.m.func_187214_a(L, ItemStack.field_190927_a);
      this.m.func_187214_a(R, ItemStack.field_190927_a);
      this.m.func_187214_a(X, ItemStack.field_190927_a);
      this.m.func_187214_a(T, ItemStack.field_190927_a);
      this.m.func_187214_a(U, ItemStack.field_190927_a);
      this.m.func_187214_a(W, ItemStack.field_190927_a);
   }

   @Override
   protected void func_184651_r() {
      super.func_184651_r();
      this.field_70714_bg.func_75776_a(1, new GirlFollowGoal(this));
   }

   public void c_clash237() {
   }

   @Override
   public void func_70619_bc() {
      super.func_70619_bc();
      if (this.field_70173_aa % 80 == 0 && this.func_110143_aJ() != this.func_110138_aP()) {
         if (!this.J_clash526()) {
            this.func_70691_i(1.0F);
         } else {
            List var1 = this.field_70170_p
               .func_72872_a(
                  EntityMob.class,
                  new AxisAlignedBB(
                     new BlockPos(this.field_70165_t - 7.0, this.field_70163_u - 1.0, this.field_70161_v - 7.0),
                     new BlockPos(this.field_70165_t + 7.0, this.field_70163_u + 1.0, this.field_70161_v + 7.0)
                  )
               );
            int var2 = var1.isEmpty() ? 4 : 1;
            this.func_70691_i(var2);
            ((WorldServer)this.field_70170_p)
               .func_180505_a(
                  EnumParticleTypes.HEART,
                  false,
                  this.field_70165_t,
                  this.field_70163_u + 1.0 + Reference.f.nextDouble(),
                  this.field_70161_v,
                  var2,
                  1.0,
                  1.0,
                  1.0,
                  Reference.f.nextGaussian(),
                  new int[0]
               );
         }
      }

      if (this.N && !this.J_clash526()) {
         this.N = false;
      }

      this.m.func_187227_b(field_184621_as, Byte.valueOf("1"));
      this.m.func_187227_b(L, this.Q.getStackInSlot(0));
      this.m.func_187227_b(R, this.Q.getStackInSlot(1));
      this.m.func_187227_b(X, this.Q.getStackInSlot(2));
      this.m.func_187227_b(T, this.Q.getStackInSlot(3));
      this.m.func_187227_b(U, this.Q.getStackInSlot(4));
      this.m.func_187227_b(W, this.Q.getStackInSlot(5));
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void a(String var1, UUID var2) {
      if ("action.names.followme".equals(var1)) {
         this.changeDataParameterFromClient("master", var2.toString());
      } else if ("action.names.stopfollowme".equals(var1)) {
         this.x_clash475();
      } else if ("action.names.equipment".equals(var1)) {
         EntityPlayerSP var3 = Minecraft.func_71410_x().field_71439_g;
         PacketHandler.b.sendToServer(new PlayerActionPacket(this.getGirlId(), var3.getPersistentID()));
      } else if ("action.names.gohome".equals(var1)) {
         this.x_clash475();
         PacketHandler.b.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
      } else if ("action.names.setnewhome".equals(var1)) {
         this.c_clash237();
         PacketHandler.b.sendToServer(new SetNewHomePacket(this.getGirlId(), new Vec3d(this.func_180425_c())));
      }
   }

   @Override
   public void func_70014_b(NBTTagCompound var1) {
      var1.func_74782_a("inventory", this.Q.serializeNBT());
      super.func_70014_b(var1);
   }

   @Override
   public void func_70037_a(NBTTagCompound var1) {
      super.func_70037_a(var1);
      this.Q.deserializeNBT(var1.func_74775_l("inventory"));
   }

   public boolean hasCapability(Capability var1, EnumFacing var2) {
      return var1 == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(var1, var2);
   }

   public Object getCapability(Capability var1, EnumFacing var2) {
      return var1 == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? this.Q : super.getCapability(var1, var2);
   }

}
