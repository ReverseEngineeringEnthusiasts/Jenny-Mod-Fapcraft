package com.trolmastercard.sexmod.item;

import com.trolmastercard.sexmod.client.renderer.AlliesLampRenderer;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.AllieEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.WorldUtils;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;







import net.minecraft.util.ResourceLocation;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class AlliesLampItem extends Item implements IAnimatable {
   static final String ALLIE_IN_USE_KEY = "sexmodAllieInUse";
   static final String ALLIE_IN_USE_TICKS_KEY = "sexmodAllieInUseTicks";
   public static final String USES_KEY = "sexmodUses";
   public static final String ALLIE_ID_KEY = "sexmodAllieID";
   static final Integer SUMMON_TICK = 95;
   static final Integer PARTICLE_START_TICK = 50;
   public static final int PARTICLE_COUNT = 150;
   public static final float PARTICLE_SPREAD = 0.75F;
   public static final AlliesLampItem ALLIES_LAMP = new AlliesLampItem();
   private final AnimationFactory animationFactory = new AnimationFactory(this);
   AnimationController<AlliesLampItem> controller;

   public AlliesLampItem() {
      this.setCreativeTab(CreativeTabs.MISC);
      this.maxStackSize = 1;
   }

   public static void register() {
      ALLIES_LAMP.setRegistryName(new ResourceLocation("sexmod", "allies_lamp"));
      ALLIES_LAMP.setTranslationKey("allies_lamp");
      MinecraftForge.EVENT_BUS.register(AlliesLampItem.class);
   }

   @SubscribeEvent
   public static void a(Register<Item> var0) {
      var0.getRegistry().register(ALLIES_LAMP);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void a(ModelRegistryEvent var0) {
      ModelLoader.setCustomModelResourceLocation(ALLIES_LAMP, 0, new ModelResourceLocation("sexmod:allies_lamp"));
      ALLIES_LAMP.setTileEntityItemStackRenderer(new AlliesLampRenderer());
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(Pre var1) {
      NBTTagCompound var2 = Minecraft.getMinecraft().player.getEntityData();
      if (var2.getBoolean("sexmodAllieInUse")) {
         var1.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void a(LootTableLoadEvent var1) {
      HashSet var2 = new HashSet();
      var2.add(LootTableList.CHESTS_ABANDONED_MINESHAFT);
      var2.add(LootTableList.CHESTS_DESERT_PYRAMID);
      var2.add(LootTableList.CHESTS_SIMPLE_DUNGEON);
      var2.add(LootTableList.CHESTS_WOODLAND_MANSION);
      if (var2.contains(var1.getName())) {
         LootPool var3 = var1.getTable().getPool("pool3");
         if (var3 == null) {
            var3 = var1.getTable().getPool("pool2");
         }

         if (var3 != null) {
            var3.addEntry(new LootEntryItem(ALLIES_LAMP, 5, 0, new LootFunction[0], new LootCondition[0], "sexmod:allies_lamp"));
         }
      }
   }

   @Override
   public void registerControllers(AnimationData var1) {
      this.controller = new AnimationController<>(this, "controller", 2.0F, this::animationPredicate);
      var1.addAnimationController(this.controller);
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, World var2, List<String> var3, ITooltipFlag var4) {
      NBTTagCompound var5 = var1.getTagCompound();
      if (var5 != null) {
         int var6 = 3 - var1.getTagCompound().getInteger("sexmodUses");
         switch (var6) {
            case 0:
               var3.add("no wishes left");
               break;
            case 1:
               var3.add("1 wish left");
               break;
            case 2:
               var3.add("2 wishes left");
         }
      }
   }

   @SideOnly(Side.CLIENT)
   protected <segs extends IAnimatable> PlayState animationPredicate(AnimationEvent<segs> var1) {
      EntityPlayerSP var2 = Minecraft.getMinecraft().player;
      NBTTagCompound var3 = var2.getEntityData();
      boolean var4 = var3.getBoolean("sexmodAllieInUse");
      if (!var4) {
         var1.getController().clearAnimationCache();
         return PlayState.STOP;
      } else {
         var1.getController().setAnimation(new AnimationBuilder().addAnimation("animation.lamp.rub", ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME));
         return PlayState.CONTINUE;
      }
   }

   public void onUpdate(ItemStack var1, World var2, Entity var3, int var4, boolean var5) {
      if (var3 instanceof EntityPlayer) {
         EntityPlayer var6 = (EntityPlayer)var3;
         NBTTagCompound var7 = var3.getEntityData();
         if (var1.equals(var6.getHeldItemMainhand()) || var1.equals(var6.getHeldItemOffhand())) {
            boolean var8 = var7.getBoolean("sexmodAllieInUse");
            int var9 = var7.getInteger("sexmodAllieInUseTicks");
            if (var8) {
               var7.setInteger("sexmodAllieInUseTicks", var9 + 1);
               if (var9 > PARTICLE_START_TICK && var9 < SUMMON_TICK) {
                  double var10 = (float)(var9 - PARTICLE_START_TICK) / (SUMMON_TICK - PARTICLE_START_TICK);
                  var10 = RotationHelper.h(var10);
                  Vec3d var12 = new Vec3d(0.0, var6.eyeHeight * (1.0 - var10), 0.0);
                  WorldUtils.a(var2, EnumParticleTypes.CRIT_MAGIC, this.getLampOffset(var6).add(var12), (int)(var10 * 150.0), var10 * 0.75, var10);
               }

               if (var9 >= SUMMON_TICK) {
                  WorldUtils.a(var2, EnumParticleTypes.CRIT_MAGIC, this.getLampOffset(var6), 150, 0.75, 2.0);
                  var7.setBoolean("sexmodAllieInUse", false);
                  var7.setInteger("sexmodAllieInUseTicks", 0);
                  if (var2.isRemote) {
                     HandlePlayerMovement.setMovementLock(false);
                  } else {
                     NBTTagCompound var15 = var1.getTagCompound();
                     if (var15 == null) {
                        var15 = new NBTTagCompound();
                     }

                     var15.setInteger("sexmodUses", var15.getInteger("sexmodUses") + 1);
                     AllieEntity var11 = new AllieEntity(var6.world, var6.getHeldItemMainhand());
                     var11.setInteractionPlayerUUID(var6.getPersistentID());
                     Vec3d var16 = this.getLampOffset(var6);
                     var11.setPositionAndRotation(var16.x, var16.y, var16.z, var6.rotationYaw + 180.0F, var6.rotationPitch);
                     var11.setTargetPosition(var11.getPositionVector());
                     var11.setYawRotation(var6.rotationYaw + 180.0F);
                     var11.setAnchored(true);
                     var11.setNoGravity(true);
                     var11.noClip = true;
                     var6.world.spawnEntity(var11);
                     BlockPos var13 = var11.getPosition().add(0, -1, 0);
                     if (var11.world.getBlockState(var13).getBlock().equals(Blocks.SAND)) {
                        var11.setCurrentAction(Action.SUMMON_SAND);
                     } else {
                        var11.setCurrentAction(var11.hasLampItem() ? Action.SUMMON : Action.SUMMON_NORMAL);
                     }

                     var1.setTagCompound(var15);
                  }
               }
            }
         }
      }
   }

   Vec3d getLampOffset(EntityPlayer var1) {
      return var1.getPositionVector().add(VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 2.0), var1.rotationYawHead));
   }

   @Override
   public AnimationFactory getFactory() {
      return this.animationFactory;
   }


   public static class a {
      @SubscribeEvent
      public void a(PlayerLoggedOutEvent var1) {
         var1.player.getEntityData().setBoolean("sexmodAllieInUse", false);
      }

      @SubscribeEvent
      public void a(RightClickItem var1) {
         EntityPlayer var2 = var1.getEntityPlayer();
         EnumHand var3 = var1.getHand();
         ItemStack var4 = var2.getHeldItem(var3);
         if (!AbstractPlayerGirlEntity.e(var2)) {
            if (!var2.world.isRemote || HandlePlayerMovement.isSneakingState()) {
               if (!var2.world.isRemote) {
                  try {
                     for (BaseGirlEntity var6 : BaseGirlEntity.getGirlEntityList()) {
                        if (!var6.isDead && var6 instanceof AllieEntity) {
                           AllieEntity var7 = (AllieEntity)var6;
                           ItemStack var8 = (ItemStack)var7.getDataManager().get(AllieEntity.LAMP_ITEM);
                           if (var4.equals(var8)) {
                              return;
                           }
                        }
                     }
                  } catch (ConcurrentModificationException var9) {
                  }
               }

               if (var4.getItem() == AlliesLampItem.ALLIES_LAMP) {
                  NBTTagCompound var10 = var4.getTagCompound();
                  if (var10 == null || var10.getInteger("sexmodUses") < 3) {
                     NBTTagCompound var11 = var2.getEntityData();
                     boolean var12 = var11.getBoolean("sexmodAllieInUse");
                     if (!var12) {
                        var11.setBoolean("sexmodAllieInUse", true);
                        var11.setInteger("sexmodAllieInUseTicks", 0);
                     }
                  }
               }
            }
         }
      }

   }
}
