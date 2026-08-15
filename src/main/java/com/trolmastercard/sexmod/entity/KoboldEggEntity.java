package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

/**
 * <b>Role.</b> The kobold egg — the hatchable egg item's placed entity
 * (KoboldNPC reproduction). Sits for 12000 ticks (with escalating wiggle
 * animations) then hatches into a new {@link KoboldEntity} joined to the
 * tribe ({@link KoboldManager}); any damage also kills it.
 * <p>
 * <b>State.</b> Data keys 115 ({@code EGG_COLOR}) and 116 ({@code EGG_TYPE},
 * the age counter — server-incremented each tick). {@code tribeId} persists
 * the tribe binding. The hatch animation is registered on the shared static
 * controller.
 */
public class KoboldEggEntity extends EntityLivingBase implements IAnimatable {
   static final int HATCH_TIME = 12000;
   private final AnimationFactory factory = new AnimationFactory(this);
   public UUID tribeId = null;
   static AnimationController<KoboldEggEntity> animationController;
   public static final DataParameter<String> EGG_COLOR = EntityDataManager.createKey(KoboldEggEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(115);
   public static final DataParameter<Integer> EGG_TYPE = EntityDataManager.createKey(KoboldEggEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(116);

   public KoboldEggEntity(World world) {
      super(world);
      this.setSize(0.5F, 0.5F);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(EGG_COLOR, KoboldEntity.aJ.toString());
      this.dataManager.register(EGG_TYPE, 0);
   }

   /**
    * SERVER/CLIENT: ages the egg; at 12000 ticks {@link #spawnHatchExplosion()}
    * hatches it. The age counter only advances on the SERVER (the client
    * reads the synced value for the wiggle animations).
    */
   public void onUpdate() {
      super.onUpdate();
      int eggType = (Integer)this.dataManager.get(EGG_TYPE);
      if (eggType >= 12000) {
         this.spawnHatchExplosion();
      }

      if (!this.world.isRemote) {
         this.dataManager.set(EGG_TYPE, eggType + 1);
      }
   }

   public boolean canTrample(World world, Block block, BlockPos pos, float fallDistance) {
      return false;
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
      boolean damaged = super.attackEntityFrom(source, amount);
      if (!damaged) {
         return false;
      }

      this.setDead();
      return true;
   }

   /**
    * SERVER: the hatch — spawns the explosion particles, creates the kobold
    * (tribe id minted if needed), joins it to the tribe, copies the tribe
    * master/name bindings and removes the egg. {@link #hatchEgg(KoboldEntity)}
    * notifies the master.
    */
   void spawnHatchExplosion() {
      for (int i = 0; i < 30; i++) {
         float vx = (Reference.RANDOM.nextBoolean() ? 1 : -1) * Reference.RANDOM.nextFloat();
         float vy = (Reference.RANDOM.nextBoolean() ? 1 : -1) * Reference.RANDOM.nextFloat();
         float vz = (Reference.RANDOM.nextBoolean() ? 1 : -1) * Reference.RANDOM.nextFloat();
         this.world
            .spawnParticle(
               EnumParticleTypes.EXPLOSION_NORMAL, 0.5 + this.posX, 0.5 + this.posY, 0.5 + this.posZ, vx, vy, vz, new int[0]
            );
      }

      if (!this.world.isRemote) {
         if (this.tribeId == null) {
            this.tribeId = UUID.randomUUID();
         }

         KoboldEntity kobold = KoboldEntity.createKobold(this.world, this.tribeId);
         KoboldManager.addTribeMember(this.tribeId, kobold);
         UUID masterId = KoboldManager.findTribeIdWith(this.tribeId);
         if (masterId != null) {
            kobold.getDataManager().set(BaseGirlEntity.MASTER, masterId.toString());
         }

         List members = KoboldManager.getTribeMembersList(this.tribeId);
         String masterName = null;

         for (KoboldEntity memberKobold : (java.util.Collection<KoboldEntity>) (members) ) {
            String name = (String)memberKobold.getDataManager().get(KoboldEntity.aU);
            if (!"".equals(name)) {
               masterName = name;
               break;
            }
         }

         if (masterName != null) {
            kobold.getDataManager().set(KoboldEntity.aU, masterName);
         }

         kobold.setPosition(0.5 + this.posX, this.posY, 0.5 + this.posZ);
         this.world.spawnEntity(kobold);
         this.hatchEgg(kobold);
         this.world.playSound(null, this.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.5F, 1.0F);
         this.world.removeEntity(this);
      }
   }

   /**
    * SERVER: announces the new tribe member to the kobold's master (colored
    * chat + hit/level-up sounds).
    */
   void hatchEgg(KoboldEntity kobold) {
      EntityPlayer player = kobold.getMasterPlayer();
      if (player != null) {
         EntityPlayerMP playerMP = (EntityPlayerMP)player;
         EyeAndKoboldColor color = KoboldManager.getTribeColor(this.tribeId);
         player.sendMessage(
            new TextComponentString(
               String.format(
                  "%s%s %shas become a %snew tribe member%s!",
                  color.getTextColor(),
                  kobold.getDisplayNameText(),
                  TextFormatting.WHITE,
                  TextFormatting.RED,
                  TextFormatting.WHITE
               )
            )
         );
         playerMP.connection
            .sendPacket(
               new SPacketSoundEffect(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.NEUTRAL, player.posX, player.posY, player.posZ, 1.0F, 1.0F)
            );
         playerMP.connection
            .sendPacket(
               new SPacketSoundEffect(
                  SoundEvents.ENTITY_FIREWORK_TWINKLE_FAR, SoundCategory.NEUTRAL, player.posX, player.posY, player.posZ, 1.0F, 1.0F
               )
            );
      }
   }

   @Override
   public void registerControllers(AnimationData data) {
      animationController = new AnimationController<>(this, "controller", 5.0F, this::animationPredicate);
      data.addAnimationController(animationController);
   }

   @Override
   public AnimationFactory getFactory() {
      return this.factory;
   }

   public void writeEntityToNBT(NBTTagCompound nbt) {
      if (this.tribeId != null) {
         nbt.setString("tribeID", this.tribeId.toString());
      }

      nbt.setString("egg_color", (String)this.dataManager.get(EGG_COLOR));
      nbt.setInteger("eggAge", (Integer)this.dataManager.get(EGG_TYPE));
      super.writeEntityToNBT(nbt);
   }

   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      String tribeIdStr = nbt.getString("tribeID");
      if (!"".equals(tribeIdStr)) {
         this.tribeId = UUID.fromString(tribeIdStr);
      }

      this.dataManager.set(EGG_COLOR, nbt.getString("egg_color"));
      this.dataManager.set(EGG_TYPE, nbt.getInteger("eggAge"));
   }

   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      int eggType = (Integer)this.dataManager.get(EGG_TYPE);
      if (12000 - eggType < 20) {
         event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.hatch", true));
         return PlayState.CONTINUE;
      } else {
         float progress = eggType / 12000.0F;
         if (progress > 0.98) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.veryfast", true));
            return PlayState.CONTINUE;
         } else if (progress > 0.85) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.fast", true));
            return PlayState.CONTINUE;
         } else if (progress > 0.75) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.medium", true));
            return PlayState.CONTINUE;
         } else if (progress > 0.5) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.slow", true));
            return PlayState.CONTINUE;
         } else {
            return PlayState.CONTINUE;
         }
      }
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
