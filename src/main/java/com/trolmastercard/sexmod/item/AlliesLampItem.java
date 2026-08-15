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

/**
 * <b>Role.</b> The Allies lamp — summons an {@link AllieEntity} over a
 * 95-tick rub animation (3 wishes per stack, tracked in {@code sexmodUses} NBT).
 * <p>
 * <b>Flow.</b> Right-click starts {@code sexmodAllieInUse} on the player's
 * entity data; {@link #onUpdate} advances the tick counter (client shows the
 * rub animation + ramping particles, server spawns the Allie at the lamp offset
 * at tick 95). {@code AlliesLampItem.a} handles the right-click start (blocked
 * for player-girls / while an Allie holds the same lamp instance) and resets the
 * flag on logout. The lamp is also injected into dungeon/mineshaft loot tables.
 * <p>
 * <b>Pitfall.</b> The {@code sexmodAllieInUse} flag lives on the *player's*
 * entity data and gates the first-person hand render
 * ({@link AlliesLampItem#onPre}) — it must be reset on logout or the player's
 * hand stays hidden after a disconnect.
 */
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
   public static void registerItems(Register<Item> event) {
      event.getRegistry().register(ALLIES_LAMP);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void onModelRegistry(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(ALLIES_LAMP, 0, new ModelResourceLocation("sexmod:allies_lamp"));
      ALLIES_LAMP.setTileEntityItemStackRenderer(new AlliesLampRenderer());
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onPre(Pre event) {
      NBTTagCompound entityData = Minecraft.getMinecraft().player.getEntityData();
      if (entityData.getBoolean("sexmodAllieInUse")) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onLootTableLoad(LootTableLoadEvent event) {
      HashSet lootTables = new HashSet();
      lootTables.add(LootTableList.CHESTS_ABANDONED_MINESHAFT);
      lootTables.add(LootTableList.CHESTS_DESERT_PYRAMID);
      lootTables.add(LootTableList.CHESTS_SIMPLE_DUNGEON);
      lootTables.add(LootTableList.CHESTS_WOODLAND_MANSION);
      if (lootTables.contains(event.getName())) {
         LootPool pool = event.getTable().getPool("pool3");
         if (pool == null) {
            pool = event.getTable().getPool("pool2");
         }

         if (pool != null) {
            pool.addEntry(new LootEntryItem(ALLIES_LAMP, 5, 0, new LootFunction[0], new LootCondition[0], "sexmod:allies_lamp"));
         }
      }
   }

   @Override
   public void registerControllers(AnimationData data) {
      this.controller = new AnimationController<>(this, "controller", 2.0F, this::animationPredicate);
      data.addAnimationController(this.controller);
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
      NBTTagCompound tag = stack.getTagCompound();
      if (tag != null) {
         int uses = 3 - stack.getTagCompound().getInteger("sexmodUses");
         switch (uses) {
            case 0:
               tooltip.add("no wishes left");
               break;
            case 1:
               tooltip.add("1 wish left");
               break;
            case 2:
               tooltip.add("2 wishes left");
         }
      }
   }

   @SideOnly(Side.CLIENT)
   protected <segs extends IAnimatable> PlayState animationPredicate(AnimationEvent<segs> event) {
      EntityPlayerSP player = Minecraft.getMinecraft().player;
      NBTTagCompound entityData = player.getEntityData();
      boolean inUse = entityData.getBoolean("sexmodAllieInUse");
      if (!inUse) {
         event.getController().clearAnimationCache();
         return PlayState.STOP;
      } else {
         event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.lamp.rub", ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME));
         return PlayState.CONTINUE;
      }
   }

   public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      if (entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         NBTTagCompound entityData = entity.getEntityData();
         if (stack.equals(player.getHeldItemMainhand()) || stack.equals(player.getHeldItemOffhand())) {
            boolean inUse = entityData.getBoolean("sexmodAllieInUse");
            int ticks = entityData.getInteger("sexmodAllieInUseTicks");
            if (inUse) {
               entityData.setInteger("sexmodAllieInUseTicks", ticks + 1);
               if (ticks > PARTICLE_START_TICK && ticks < SUMMON_TICK) {
                  double progress = (float)(ticks - PARTICLE_START_TICK) / (SUMMON_TICK - PARTICLE_START_TICK);
                  progress = RotationHelper.easeInOutQuad(progress);
                  Vec3d offset = new Vec3d(0.0, player.eyeHeight * (1.0 - progress), 0.0);
                  WorldUtils.spawnParticles(world, EnumParticleTypes.CRIT_MAGIC, this.getLampOffset(player).add(offset), (int)(progress * 150.0), progress * 0.75, progress);
               }

               if (ticks >= SUMMON_TICK) {
                  WorldUtils.spawnParticles(world, EnumParticleTypes.CRIT_MAGIC, this.getLampOffset(player), 150, 0.75, 2.0);
                  entityData.setBoolean("sexmodAllieInUse", false);
                  entityData.setInteger("sexmodAllieInUseTicks", 0);
                  if (world.isRemote) {
                     HandlePlayerMovement.setMovementLock(false);
                  } else {
                     NBTTagCompound tag = stack.getTagCompound();
                     if (tag == null) {
                        tag = new NBTTagCompound();
                     }

                     tag.setInteger("sexmodUses", tag.getInteger("sexmodUses") + 1);
                     AllieEntity allie = new AllieEntity(player.world, player.getHeldItemMainhand());
                     allie.setInteractionPlayerUUID(player.getPersistentID());
                     Vec3d offset2 = this.getLampOffset(player);
                     allie.setPositionAndRotation(offset2.x, offset2.y, offset2.z, player.rotationYaw + 180.0F, player.rotationPitch);
                     allie.setTargetPosition(allie.getPositionVector());
                     allie.setYawRotation(player.rotationYaw + 180.0F);
                     allie.setAnchored(true);
                     allie.setNoGravity(true);
                     allie.noClip = true;
                     player.world.spawnEntity(allie);
                     BlockPos pos = allie.getPosition().add(0, -1, 0);
                     if (allie.world.getBlockState(pos).getBlock().equals(Blocks.SAND)) {
                        allie.setCurrentAction(Action.SUMMON_SAND);
                     } else {
                        allie.setCurrentAction(allie.hasLampItem() ? Action.SUMMON : Action.SUMMON_NORMAL);
                     }

                     stack.setTagCompound(tag);
                  }
               }
            }
         }
      }
   }

   Vec3d getLampOffset(EntityPlayer player) {
      return player.getPositionVector().add(VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 2.0), player.rotationYawHead));
   }

   @Override
   public AnimationFactory getFactory() {
      return this.animationFactory;
   }

   public static class a {
      @SubscribeEvent
      public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
         event.player.getEntityData().setBoolean("sexmodAllieInUse", false);
      }

      @SubscribeEvent
      public void onRightClickItem(RightClickItem event) {
         EntityPlayer player = event.getEntityPlayer();
         EnumHand hand = event.getHand();
         ItemStack stack = player.getHeldItem(hand);
         if (!AbstractPlayerGirlEntity.isOwnerPlayer(player)) {
            if (!player.world.isRemote || HandlePlayerMovement.isSneakingState()) {
               if (!player.world.isRemote) {
                  try {
                     for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
                        if (!girl.isDead && girl instanceof AllieEntity) {
                           AllieEntity allie = (AllieEntity)girl;
                           ItemStack lampStack = (ItemStack)allie.getDataManager().get(AllieEntity.LAMP_ITEM);
                           if (stack.equals(lampStack)) {
                              return;
                           }
                        }
                     }
                  } catch (ConcurrentModificationException cme) {
                  }
               }

               if (stack.getItem() == AlliesLampItem.ALLIES_LAMP) {
                  NBTTagCompound tag = stack.getTagCompound();
                  if (tag == null || tag.getInteger("sexmodUses") < 3) {
                     NBTTagCompound entityData = player.getEntityData();
                     boolean inUse = entityData.getBoolean("sexmodAllieInUse");
                     if (!inUse) {
                        entityData.setBoolean("sexmodAllieInUse", true);
                        entityData.setInteger("sexmodAllieInUseTicks", 0);
                     }
                  }
               }
            }
         }
      }

   }
}
