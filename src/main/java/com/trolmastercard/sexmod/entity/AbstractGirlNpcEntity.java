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
   public int nextAttack = 1;
   public int slashSwordRot;
   public int stabSwordRot = 0;
   public int holdBowRot;
   public Vec3d swordOffsetStab = Vec3d.ZERO;
   public boolean downed;
   public ItemStackHandler inventory = new ItemStackHandler(7);
   public static final DataParameter<ItemStack> WEAPON = EntityDataManager.createKey(AbstractGirlNpcEntity.class, DataSerializers.ITEM_STACK)
      .getSerializer()
      .createKey(117);
   public static final DataParameter<ItemStack> BOW = EntityDataManager.createKey(AbstractGirlNpcEntity.class, DataSerializers.ITEM_STACK)
      .getSerializer()
      .createKey(116);
   public static final DataParameter<ItemStack> HELMET_SLOT = EntityDataManager.createKey(AbstractGirlNpcEntity.class, DataSerializers.ITEM_STACK)
      .getSerializer()
      .createKey(115);
   public static final DataParameter<ItemStack> CHEST_SLOT = EntityDataManager.createKey(AbstractGirlNpcEntity.class, DataSerializers.ITEM_STACK)
      .getSerializer()
      .createKey(114);
   public static final DataParameter<ItemStack> LEGS_SLOT = EntityDataManager.createKey(AbstractGirlNpcEntity.class, DataSerializers.ITEM_STACK)
      .getSerializer()
      .createKey(113);
   public static final DataParameter<ItemStack> BOOTS_SLOT = EntityDataManager.createKey(AbstractGirlNpcEntity.class, DataSerializers.ITEM_STACK)
      .getSerializer()
      .createKey(112);
   public static final DataParameter<Integer> ATTACK_MODE = EntityDataManager.createKey(AbstractGirlNpcEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(111);

   protected AbstractGirlNpcEntity(World var1) {
      super(var1);
      if (this.inventory.getStackInSlot(0) == ItemStack.EMPTY) {
         this.inventory.setStackInSlot(0, new ItemStack(Items.IRON_SWORD));
      }

      if (this.inventory.getStackInSlot(1) == ItemStack.EMPTY) {
         this.inventory.setStackInSlot(1, new ItemStack(Items.BOW));
      }
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.entityDataManager.register(ATTACK_MODE, 0);
      this.entityDataManager.register(WEAPON, ItemStack.EMPTY);
      this.entityDataManager.register(BOW, ItemStack.EMPTY);
      this.entityDataManager.register(HELMET_SLOT, ItemStack.EMPTY);
      this.entityDataManager.register(CHEST_SLOT, ItemStack.EMPTY);
      this.entityDataManager.register(LEGS_SLOT, ItemStack.EMPTY);
      this.entityDataManager.register(BOOTS_SLOT, ItemStack.EMPTY);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(1, new GirlFollowGoal(this));
   }

   public void onArriveHome() {
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (this.ticksExisted % 80 == 0 && this.getHealth() != this.getMaxHealth()) {
         if (!this.hasMaster()) {
            this.heal(1.0F);
         } else {
            List var1 = this.world
               .getEntitiesWithinAABB(
                  EntityMob.class,
                  new AxisAlignedBB(
                     new BlockPos(this.posX - 7.0, this.posY - 1.0, this.posZ - 7.0),
                     new BlockPos(this.posX + 7.0, this.posY + 1.0, this.posZ + 7.0)
                  )
               );
            int var2 = var1.isEmpty() ? 4 : 1;
            this.heal(var2);
            ((WorldServer)this.world)
               .spawnParticle(
                  EnumParticleTypes.HEART,
                  false,
                  this.posX,
                  this.posY + 1.0 + Reference.RANDOM.nextDouble(),
                  this.posZ,
                  var2,
                  1.0,
                  1.0,
                  1.0,
                  Reference.RANDOM.nextGaussian(),
                  new int[0]
               );
         }
      }

      if (this.downed && !this.hasMaster()) {
         this.downed = false;
      }

      this.entityDataManager.set(HAND_STATES, Byte.valueOf("1"));
      this.entityDataManager.set(WEAPON, this.inventory.getStackInSlot(0));
      this.entityDataManager.set(BOW, this.inventory.getStackInSlot(1));
      this.entityDataManager.set(HELMET_SLOT, this.inventory.getStackInSlot(2));
      this.entityDataManager.set(CHEST_SLOT, this.inventory.getStackInSlot(3));
      this.entityDataManager.set(LEGS_SLOT, this.inventory.getStackInSlot(4));
      this.entityDataManager.set(BOOTS_SLOT, this.inventory.getStackInSlot(5));
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void doAction(String var1, UUID var2) {
      if ("action.names.followme".equals(var1)) {
         this.changeDataParameterFromClient("master", var2.toString());
      } else if ("action.names.stopfollowme".equals(var1)) {
         this.goHome();
      } else if ("action.names.equipment".equals(var1)) {
         EntityPlayerSP var3 = Minecraft.getMinecraft().player;
         PacketHandler.networkWrapper.sendToServer(new PlayerActionPacket(this.getGirlId(), var3.getPersistentID()));
      } else if ("action.names.gohome".equals(var1)) {
         this.goHome();
         PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
      } else if ("action.names.setnewhome".equals(var1)) {
         this.onArriveHome();
         PacketHandler.networkWrapper.sendToServer(new SetNewHomePacket(this.getGirlId(), new Vec3d(this.getPosition())));
      }
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound var1) {
      var1.setTag("inventory", this.inventory.serializeNBT());
      super.writeEntityToNBT(var1);
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.inventory.deserializeNBT(var1.getCompoundTag("inventory"));
   }

   public boolean hasCapability(Capability var1, EnumFacing var2) {
      return var1 == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(var1, var2);
   }

   public Object getCapability(Capability var1, EnumFacing var2) {
      return var1 == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? this.inventory : super.getCapability(var1, var2);
   }

}
