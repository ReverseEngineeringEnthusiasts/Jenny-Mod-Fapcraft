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

/**
 * <b>Role.</b> An invisible marker entity used as the render/placement anchor
 * for custom models: the scene-position reference ({@link #modelCode} = girl
 * UUID, {@link #modelData} = model DNA) and the item-preview mode
 * ({@link #isItemModel} + {@link #boneType}) for the inventory/wardrobe
 * screens. It is what {@link BaseGirlEntity#renderCustomModelTransform} and
 * the girl renderers use to position scene models.
 * <p>
 * <b>State.</b> Data keys 101 ({@code modelCode}) and 102 ({@code modelData}),
 * both strings. Tiny (0.1 x 0.1), non-collidable, never pushed, and only
 * damageable by out-of-world damage. No controllers are registered
 * (geckolib is driven from the referenced girl instead).
 */
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

   public SexSceneEntity(World world) {
      super(world);
      this.width = 0.1F;
      this.height = 0.1F;
   }

   public SexSceneEntity(World world, UUID uuid, String modelDataStr) {
      this(world);
      this.dataManager.set(modelCode, uuid.toString());
      this.dataManager.set(modelData, modelDataStr);
   }

   /**
    * Factory for the item-preview variant: links the scene entity to a girl
    * UUID and marks it as a model-bone preview ({@code isItemModel},
    * {@code boneType}) so the renderer shows only the named bone.
    */
   public static SexSceneEntity createSceneEntity(World world, UUID uuid, BoneType boneType) {
      SexSceneEntity scene = new SexSceneEntity(world);
      scene.getDataManager().set(modelCode, uuid.toString());
      scene.isItemModel = true;
      scene.boneType = boneType;
      return scene;
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(modelCode, "");
      this.dataManager.register(modelData, "");
   }

   public AxisAlignedBB getRenderBoundingBox() {
      BlockPos pos = this.getPosition();
      Vec3i halfSize = new Vec3i(0.5, 0.5, 0.5);
      return new AxisAlignedBB(pos.subtract(halfSize), pos.add(halfSize));
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRender3d(double x, double y, double z) {
      double dx = this.posX - x;
      double dy = this.posY - y;
      double dz = this.posZ - z;
      double distSq = dx * dx + dy * dy + dz * dz;
      return this.isInRangeToRenderDist(distSq);
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double dist) {
      return dist < 11000.0;
   }

   @Nullable
   public UUID getGirlIdFromCode() {
      String code = (String)this.dataManager.get(modelCode);
      return "".equals(code) ? null : UUID.fromString(code);
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
      return source != DamageSource.OUT_OF_WORLD ? false : super.attackEntityFrom(source, amount);
   }

   @Nullable
   public String getModelCode() {
      String data = (String)this.dataManager.get(modelData);
      return "".equals(data) ? null : data;
   }

   public boolean canBePushed() {
      return false;
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public void onDeath(DamageSource source) {
      super.onDeath(source);
   }

   @Override
   public AnimationFactory getFactory() {
      return this.factory;
   }

   @Override
   public void registerControllers(AnimationData data) {
   }

   public Iterable<ItemStack> getArmorInventoryList() {
      return new ArrayList<>();
   }

   public ItemStack getItemStackFromSlot(EntityEquipmentSlot slot) {
      return ItemStack.EMPTY;
   }

   public void setItemStackToSlot(EntityEquipmentSlot slot, ItemStack stack) {
   }

   public EnumHandSide getPrimaryHand() {
      return EnumHandSide.LEFT;
   }

}
