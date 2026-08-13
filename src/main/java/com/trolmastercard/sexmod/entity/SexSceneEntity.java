package com.trolmastercard.sexmod.entity;


import java.util.ArrayList;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.MatrixStack;

public class SexSceneEntity extends EntityLivingBase implements IAnimatable {
   static final float e = 11000.0F;
   public static final DataParameter<String> a = EntityDataManager.func_187226_a(SexSceneEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(101);
   public static final DataParameter<String> b = EntityDataManager.func_187226_a(SexSceneEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(102);
   AnimationFactory g = new AnimationFactory(this);
   public boolean f = false;
   public MatrixStack c = new MatrixStack();
   public BoneType d = null;

   public SexSceneEntity(World var1) {
      super(var1);
      this.field_70130_N = 0.1F;
      this.field_70131_O = 0.1F;
   }

   public SexSceneEntity(World var1, UUID var2, String var3) {
      this(var1);
      this.field_70180_af.func_187227_b(a, var2.toString());
      this.field_70180_af.func_187227_b(b, var3);
   }

   public static SexSceneEntity a(World var0, UUID var1, BoneType var2) {
      SexSceneEntity var3 = new SexSceneEntity(var0);
      var3.func_184212_Q().func_187227_b(a, var1.toString());
      var3.f = true;
      var3.d = var2;
      return var3;
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_187214_a(a, "");
      this.field_70180_af.func_187214_a(b, "");
   }

   public AxisAlignedBB func_184177_bl() {
      BlockPos var1 = this.func_180425_c();
      Vec3i var2 = new Vec3i(0.5, 0.5, 0.5);
      return new AxisAlignedBB(var1.func_177973_b(var2), var1.func_177971_a(var2));
   }

   @SideOnly(Side.CLIENT)
   public boolean func_145770_h(double var1, double var3, double var5) {
      double var7 = this.field_70165_t - var1;
      double var9 = this.field_70163_u - var3;
      double var11 = this.field_70161_v - var5;
      double var13 = var7 * var7 + var9 * var9 + var11 * var11;
      return this.func_70112_a(var13);
   }

   @SideOnly(Side.CLIENT)
   public boolean func_70112_a(double var1) {
      return var1 < 11000.0;
   }

   @Nullable
   public UUID b_clash342() {
      String var1 = (String)this.field_70180_af.func_187225_a(a);
      return "".equals(var1) ? null : UUID.fromString(var1);
   }

   public boolean func_70097_a(DamageSource var1, float var2) {
      return var1 != DamageSource.field_76380_i ? false : super.func_70097_a(var1, var2);
   }

   @Nullable
   public String a_clash343() {
      String var1 = (String)this.field_70180_af.func_187225_a(b);
      return "".equals(var1) ? null : var1;
   }

   public boolean func_70104_M() {
      return false;
   }

   public boolean func_70067_L() {
      return false;
   }

   public void func_70645_a(DamageSource var1) {
      super.func_70645_a(var1);
   }

   @Override
   public AnimationFactory getFactory() {
      return this.g;
   }

   @Override
   public void registerControllers(AnimationData var1) {
   }

   public Iterable<ItemStack> func_184193_aE() {
      return new ArrayList<>();
   }

   public ItemStack func_184582_a(EntityEquipmentSlot var1) {
      return ItemStack.field_190927_a;
   }

   public void func_184201_a(EntityEquipmentSlot var1, ItemStack var2) {
   }

   public EnumHandSide func_184591_cq() {
      return EnumHandSide.LEFT;
   }

}
