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

/**
 * <b>Role.</b> Base class for the NPC (non-player) girls that follow and fight
 * alongside a player — Jenny, Bia, Luna, Ellie, Bee, Goblin and the rest.
 * Adds a 7-slot inventory ({@link ItemStackHandler}), weapon/bow/armor slots
 * synced via the data manager, an attack-mode parameter and the follow AI
 * ({@link GirlFollowGoal}).
 * <p>
 * <b>State.</b> Data-manager keys {@code WEAPON/BOW/HELMET_SLOT/CHEST_SLOT/LEGS_SLOT/BOOTS_SLOT}
 * (IDs 117..112) mirror {@link #inventory} slots 0..5 every AI tick;
 * {@code ATTACK_MODE} (ID 111) is set by the follow goal (0 = idle, 1 = melee,
 * 2 = bow). IDs 111-117 must NOT be reordered — they are a contiguous block
 * above the {@link BaseGirlEntity} keys and are referenced by the client
 * renderer and {@link GirlFollowGoal}.
 * <p>
 * <b>Pitfalls.</b> {@link #updateAITasks()} force-sets {@code HAND_STATES} to
 * {@code "1"} every tick (combat hand-raise flag) — do not "fix" this. The
 * {@code downed} flag is cleared here when the girl has no master; combat
 * downed-state transitions live in {@link GirlFollowGoal.a}.
 */
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

   protected AbstractGirlNpcEntity(World world) {
      super(world);
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

   /**
    * Called on both sides every AI tick. Regenerates the girl (heal + heart
    * particles) every 80 ticks when hurt, clears the {@code downed} flag when
    * unbounded, and re-syncs the inventory slots into the data manager so the
    * client renderer and follow-goal see current equipment.
    * <p>
    * SERVER-side for the heal/particle branch (uses {@link WorldServer});
    * the data-manager writes run on both sides.
    */
   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (this.ticksExisted % 80 == 0 && this.getHealth() != this.getMaxHealth()) {
         if (!this.hasMaster()) {
            this.heal(1.0F);
         } else {
            List mobs = this.world
               .getEntitiesWithinAABB(
                  EntityMob.class,
                  new AxisAlignedBB(
                     new BlockPos(this.posX - 7.0, this.posY - 1.0, this.posZ - 7.0),
                     new BlockPos(this.posX + 7.0, this.posY + 1.0, this.posZ + 7.0)
                  )
               );
            int healAmount = mobs.isEmpty() ? 4 : 1;
            this.heal(healAmount);
            ((WorldServer)this.world)
               .spawnParticle(
                  EnumParticleTypes.HEART,
                  false,
                  this.posX,
                  this.posY + 1.0 + Reference.RANDOM.nextDouble(),
                  this.posZ,
                  healAmount,
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

   /**
    * CLIENT-side action dispatch from the interaction GUI:
    * {@code followme} binds the acting player as master (via
    * {@link ChangeDataParameterPacket} through {@code changeDataParameterFromClient}),
    * {@code stopfollowme}/{@code gohome} send the girl home, {@code equipment}
    * opens the equipment GUI and {@code setnewhome} stores a new home position.
    * Ordering: the packet names must stay in sync with {@link GirlFollowGoal}
    * and the GUI action strings.
    */
   @SideOnly(Side.CLIENT)
   @Override
   public void doAction(String actionName, UUID playerUuid) {
      if ("action.names.followme".equals(actionName)) {
         this.changeDataParameterFromClient("master", playerUuid.toString());
      } else if ("action.names.stopfollowme".equals(actionName)) {
         this.goHome();
      } else if ("action.names.equipment".equals(actionName)) {
         EntityPlayerSP player = Minecraft.getMinecraft().player;
         PacketHandler.networkWrapper.sendToServer(new PlayerActionPacket(this.getGirlId(), player.getPersistentID()));
      } else if ("action.names.gohome".equals(actionName)) {
         this.goHome();
         PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
      } else if ("action.names.setnewhome".equals(actionName)) {
         this.onArriveHome();
         PacketHandler.networkWrapper.sendToServer(new SetNewHomePacket(this.getGirlId(), new Vec3d(this.getPosition())));
      }
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound nbt) {
      nbt.setTag("inventory", this.inventory.serializeNBT());
      super.writeEntityToNBT(nbt);
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      this.inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
   }

   public boolean hasCapability(Capability capability, EnumFacing facing) {
      return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
   }

   public Object getCapability(Capability capability, EnumFacing facing) {
      return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? this.inventory : super.getCapability(capability, facing);
   }

}
