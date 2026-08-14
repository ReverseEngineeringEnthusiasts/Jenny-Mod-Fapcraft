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

   public KoboldEggEntity(World var1) {
      super(var1);
      this.setSize(0.5F, 0.5F);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(EGG_COLOR, KoboldEntity.aJ.toString());
      this.dataManager.register(EGG_TYPE, 0);
   }

   public void onUpdate() {
      super.onUpdate();
      int var1 = (Integer)this.dataManager.get(EGG_TYPE);
      if (var1 >= 12000) {
         this.spawnHatchExplosion();
      }

      if (!this.world.isRemote) {
         this.dataManager.set(EGG_TYPE, var1 + 1);
      }
   }

   public boolean canTrample(World var1, Block var2, BlockPos var3, float var4) {
      return false;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      boolean var3 = super.attackEntityFrom(var1, var2);
      if (!var3) {
         return false;
      }

      this.setDead();
      return true;
   }

   void spawnHatchExplosion() {
      for (int var1 = 0; var1 < 30; var1++) {
         float var2 = (Reference.RANDOM.nextBoolean() ? 1 : -1) * Reference.RANDOM.nextFloat();
         float var3 = (Reference.RANDOM.nextBoolean() ? 1 : -1) * Reference.RANDOM.nextFloat();
         float var4 = (Reference.RANDOM.nextBoolean() ? 1 : -1) * Reference.RANDOM.nextFloat();
         this.world
            .spawnParticle(
               EnumParticleTypes.EXPLOSION_NORMAL, 0.5 + this.posX, 0.5 + this.posY, 0.5 + this.posZ, var2, var3, var4, new int[0]
            );
      }

      if (!this.world.isRemote) {
         if (this.tribeId == null) {
            this.tribeId = UUID.randomUUID();
         }

         KoboldEntity var8 = KoboldEntity.createKobold(this.world, this.tribeId);
         KoboldManager.addTribeMember(this.tribeId, var8);
         UUID var9 = KoboldManager.findTribeIdWith(this.tribeId);
         if (var9 != null) {
            var8.getDataManager().set(BaseGirlEntity.MASTER, var9.toString());
         }

         List var10 = KoboldManager.getTribeMembersList(this.tribeId);
         String var11 = null;

         for (KoboldEntity var6 : (java.util.Collection<KoboldEntity>) (var10) ) {
            String var7 = (String)var6.getDataManager().get(KoboldEntity.aU);
            if (!"".equals(var7)) {
               var11 = var7;
               break;
            }
         }

         if (var11 != null) {
            var8.getDataManager().set(KoboldEntity.aU, var11);
         }

         var8.setPosition(0.5 + this.posX, this.posY, 0.5 + this.posZ);
         this.world.spawnEntity(var8);
         this.hatchEgg(var8);
         this.world.playSound(null, this.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.5F, 1.0F);
         this.world.removeEntity(this);
      }
   }

   void hatchEgg(KoboldEntity var1) {
      EntityPlayer var2 = var1.getMasterPlayer();
      if (var2 != null) {
         EntityPlayerMP var3 = (EntityPlayerMP)var2;
         EyeAndKoboldColor var4 = KoboldManager.getTribeColor(this.tribeId);
         var2.sendMessage(
            new TextComponentString(
               String.format(
                  "%s%s %shas become a %snew tribe member%s!",
                  var4.getTextColor(),
                  var1.getDisplayNameText(),
                  TextFormatting.WHITE,
                  TextFormatting.RED,
                  TextFormatting.WHITE
               )
            )
         );
         var3.connection
            .sendPacket(
               new SPacketSoundEffect(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.NEUTRAL, var2.posX, var2.posY, var2.posZ, 1.0F, 1.0F)
            );
         var3.connection
            .sendPacket(
               new SPacketSoundEffect(
                  SoundEvents.ENTITY_FIREWORK_TWINKLE_FAR, SoundCategory.NEUTRAL, var2.posX, var2.posY, var2.posZ, 1.0F, 1.0F
               )
            );
      }
   }

   @Override
   public void registerControllers(AnimationData var1) {
      animationController = new AnimationController<>(this, "controller", 5.0F, this::animationPredicate);
      var1.addAnimationController(animationController);
   }

   @Override
   public AnimationFactory getFactory() {
      return this.factory;
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      if (this.tribeId != null) {
         var1.setString("tribeID", this.tribeId.toString());
      }

      var1.setString("egg_color", (String)this.dataManager.get(EGG_COLOR));
      var1.setInteger("eggAge", (Integer)this.dataManager.get(EGG_TYPE));
      super.writeEntityToNBT(var1);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      String var2 = var1.getString("tribeID");
      if (!"".equals(var2)) {
         this.tribeId = UUID.fromString(var2);
      }

      this.dataManager.set(EGG_COLOR, var1.getString("egg_color"));
      this.dataManager.set(EGG_TYPE, var1.getInteger("eggAge"));
   }

   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      int var2 = (Integer)this.dataManager.get(EGG_TYPE);
      if (12000 - var2 < 20) {
         var1.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.hatch", true));
         return PlayState.CONTINUE;
      } else {
         float var3 = var2 / 12000.0F;
         if (var3 > 0.98) {
            var1.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.veryfast", true));
            return PlayState.CONTINUE;
         } else if (var3 > 0.85) {
            var1.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.fast", true));
            return PlayState.CONTINUE;
         } else if (var3 > 0.75) {
            var1.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.medium", true));
            return PlayState.CONTINUE;
         } else if (var3 > 0.5) {
            var1.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.slow", true));
            return PlayState.CONTINUE;
         } else {
            return PlayState.CONTINUE;
         }
      }
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
