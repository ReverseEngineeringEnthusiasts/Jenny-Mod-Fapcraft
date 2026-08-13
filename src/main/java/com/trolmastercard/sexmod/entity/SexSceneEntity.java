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
   static final float DESPAWN_DISTANCE = 11000.0F;
   public static final DataParameter<String> modelCode = EntityDataManager.createKey(SexSceneEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(101);
   public static final DataParameter<String> modelData = EntityDataManager.createKey(SexSceneEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(102);
   AnimationFactory factory = new AnimationFactory(this);
   public boolean isItemModel = false;
   public MatrixStack matrixStack = new MatrixStack();
   public BoneType boneType = null;

   public SexSceneEntity(World var1) {
      super(var1);
      this.width = 0.1F;
      this.height = 0.1F;
   }

   public SexSceneEntity(World var1, UUID var2, String var3) {
      this(var1);
      this.dataManager.set(modelCode, var2.toString());
      this.dataManager.set(modelData, var3);
   }

   public static SexSceneEntity a(World var0, UUID var1, BoneType var2) {
      SexSceneEntity var3 = new SexSceneEntity(var0);
      var3.getDataManager().set(modelCode, var1.toString());
      var3.isItemModel = true;
      var3.boneType = var2;
      return var3;
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(modelCode, "");
      this.dataManager.register(modelData, "");
   }

   public AxisAlignedBB getRenderBoundingBox() {
      BlockPos var1 = this.getPosition();
      Vec3i var2 = new Vec3i(0.5, 0.5, 0.5);
      return new AxisAlignedBB(var1.subtract(var2), var1.add(var2));
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRender3d(double var1, double var3, double var5) {
      double var7 = this.posX - var1;
      double var9 = this.posY - var3;
      double var11 = this.posZ - var5;
      double var13 = var7 * var7 + var9 * var9 + var11 * var11;
      return this.isInRangeToRenderDist(var13);
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double var1) {
      return var1 < 11000.0;
   }

   @Nullable
   public UUID b_clash342() {
      String var1 = (String)this.dataManager.get(modelCode);
      return "".equals(var1) ? null : UUID.fromString(var1);
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      return var1 != DamageSource.OUT_OF_WORLD ? false : super.attackEntityFrom(var1, var2);
   }

   @Nullable
   public String a_clash343() {
      String var1 = (String)this.dataManager.get(modelData);
      return "".equals(var1) ? null : var1;
   }

   public boolean canBePushed() {
      return false;
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public void onDeath(DamageSource var1) {
      super.onDeath(var1);
   }

   @Override
   public AnimationFactory getFactory() {
      return this.factory;
   }

   @Override
   public void registerControllers(AnimationData var1) {
   }

   public Iterable<ItemStack> getArmorInventoryList() {
      return new ArrayList<>();
   }

   public ItemStack getItemStackFromSlot(EntityEquipmentSlot var1) {
      return ItemStack.EMPTY;
   }

   public void setItemStackToSlot(EntityEquipmentSlot var1, ItemStack var2) {
   }

   public EnumHandSide getPrimaryHand() {
      return EnumHandSide.LEFT;
   }

}
