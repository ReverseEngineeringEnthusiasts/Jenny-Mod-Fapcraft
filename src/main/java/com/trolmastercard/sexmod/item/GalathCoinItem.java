package com.trolmastercard.sexmod.item;

import com.trolmastercard.sexmod.client.particle.DragonBreathParticle;
import com.trolmastercard.sexmod.client.renderer.GalathCoinRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.networking.InformOfOwnershipPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.util.GirlSavedData;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.VectorMath;
import net.minecraft.util.ResourceLocation;
import java.util.ConcurrentModificationException;
import java.util.Random;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
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
 * <b>Role.</b> The Galath coin — binds a defeated succubus to her victor
 * (ownership via {@link GirlSavedData}) and summons/dismisses her.
 * <p>
 * <b>Flow.</b> Right-click starts a 4-second summon window (activation time in
 * the player's entity data); on the client, energy particles stream from the
 * coin to the summon point; after the window the server spawns the
 * {@link GalathEntity} ({@link #writeCooldownNBT} SERVER branch) and grants
 * ownership. Right-clicking the owned Galath starts the de-summon animation;
 * the summon/de-summon particle visuals run client-side while the timestamps
 * advance on both sides.
 * <p>
 * <b>Pitfalls.</b> The timing keys ({@code sexmod:galath_coin_activation_time},
 * {@code ..._deactivation_time}, {@code ..._de_summoning_animation_time}) are
 * the single source of truth for the animation state machine — both sides must
 * agree on the 4000/1000/3000 ms windows or the girl desyncs. The client-side
 * {@link GirlSavedData#debugEnabled} mirrors ownership; see
 * {@link InformOfOwnershipPacket}.
 */
public class GalathCoinItem extends Item implements IAnimatable {
   public static final GalathCoinItem GALATH_COIN = new GalathCoinItem();
   public static final long SUMMON_DURATION = 4000L;
   public static final long ANIMATION_START_DELAY = 1000L;
   public static final long ANIMATION_END_DELAY = 3000L;
   public static final float HORIZONTAL_OFFSET = 0.1F;
   public static final float BASE_VERTICAL_OFFSET = -0.01F;
   public static final float PITCH_MULTIPLIER = 0.0015F;
   public static final float SPAWN_DISTANCE = 2.0F;
   public static final float HEIGHT_OFFSET = 1.5F;
   public static final float PARTICLE_VELOCITY = 0.03F;
   public static final float PARTICLE_COUNT = 100.0F;
   public static final float PARTICLE_VELOCITY_SPREAD = 0.2F;
   public static final float PARTICLE_SPAWN_SPREAD = 1.5F;
   public static final String ACTIVATION_TIME_KEY = "sexmod:galath_coin_activation_time";
   public static final String DEACTIVATION_TIME_KEY = "sexmod:galath_coin_deactivation_time";
   public static final String DE_SUMMON_ANIMATION_KEY = "sexmod:galath_coin_de_summoning_animation_time";
   public static final String DESCRIPTION = "Defeating a succubus makes her accept the victor as her master, granting him a coin to which her soul is bound. Using the coin summons her, offering services on demand. If her master uses the coin on her or goes too far, she returns to the coin";
   private final AnimationFactory animationFactory = new AnimationFactory(this);
   AnimationController<GalathCoinItem> controller;

   public GalathCoinItem() {
      this.maxStackSize = 1;
   }

   public static void register() {
      GALATH_COIN.setRegistryName(new ResourceLocation("sexmod", "galath_coin"));
      GALATH_COIN.setTranslationKey("galath_coin");
      MinecraftForge.EVENT_BUS.register(GalathCoinItem.class);
   }

   @SubscribeEvent
   public static void registerItems(Register<Item> event) {
      event.getRegistry().register(GALATH_COIN);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void onModelRegistry(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(GALATH_COIN, 0, new ModelResourceLocation("sexmod:galath_coin"));
      GALATH_COIN.setTileEntityItemStackRenderer(new GalathCoinRenderer());
   }

   public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
      NBTTagCompound entityData = player.getEntityData();
      ActionResult result = new ActionResult(EnumActionResult.FAIL, player.getHeldItem(hand));
      if (entityData.getLong("sexmod:galath_coin_deactivation_time") != 0L) {
         return result;
      } else if (entityData.getLong("sexmod:galath_coin_activation_time") != 0L) {
         return result;
      } else if (!this.canSummon(world, player)) {
         world.playSound(player.posX, player.posY, player.posZ, SoundHandler.MISC_BEEW[0], SoundCategory.PLAYERS, 1.0F, 1.0F, false);
         return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
      } else {
         world.playSound(player.posX, player.posY, player.posZ, SoundHandler.MISC_WEOWEO[1], SoundCategory.PLAYERS, 1.0F, 1.0F, false);
         entityData.setLong("sexmod:galath_coin_activation_time", System.currentTimeMillis());
         return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
      }
   }

   boolean canSummon(World world, EntityPlayer player) {
      return !world.isRemote ? !GirlSavedData.hasOwner(player.getPersistentID()) : !GirlSavedData.debugEnabled;
   }

   @SubscribeEvent
   public void onEntityInteract(EntityInteract event) {
      EntityPlayer player = event.getEntityPlayer();
      ItemStack stack = player.getHeldItem(event.getHand());
      if (GALATH_COIN.equals(stack.getItem())) {
         Entity target = event.getTarget();
         if (target instanceof GalathEntity) {
            GalathEntity galath = (GalathEntity)target;
            if (player.getPersistentID().equals(galath.getMasterUUID())) {
               player.world
                  .playSound(
                     player.posX, player.posY, player.posZ, SoundHandler.MISC_WEOWEO[0], SoundCategory.PLAYERS, 1.0F, 1.0F, false
                  );
               player.getEntityData().setLong("sexmod:galath_coin_deactivation_time", System.currentTimeMillis());
               event.setCanceled(true);
            }
         }
      }
   }

   public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.onUpdate(stack, world, entity, slot, selected);
      if (entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         NBTTagCompound entityData = player.getEntityData();
         long activationTime = entityData.getLong("sexmod:galath_coin_activation_time");
         long deactivationTime = entityData.getLong("sexmod:galath_coin_deactivation_time");
         long now = System.currentTimeMillis();
         this.writeCooldownNBT(player, entityData, now, activationTime);
         this.writeCooldownNBT(player, entityData, now, deactivationTime);
         if (deactivationTime != 0L && now > deactivationTime + 4000L) {
            entityData.setLong("sexmod:galath_coin_deactivation_time", 0L);
            entityData.setBoolean("sexmod:galath_coin_de_summoning_animation_time", false);
         }

         if (world.isRemote) {
            this.isSummonWindow(player, now, activationTime);
            this.isCooldownElapsed(player, now, deactivationTime);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void isCooldownElapsed(EntityPlayer player, long now, long startTime) {
      if (startTime != 0L) {
         if (now > startTime + 1000L && now < startTime + 3000L) {
            GalathEntity galath = null;

            try {
               for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
                  if (!girl.isDead && girl.world.isRemote && girl instanceof GalathEntity && player.equals(girl.getMasterPlayer())) {
                     galath = (GalathEntity)girl;
                     break;
                  }
               }
            } catch (ConcurrentModificationException cme) {
            }

            if (galath != null) {
               Vec3d targetPos = galath.getTargetPosition().add(0.0, 1.5, 0.0);
               Vec3d eyePos = player.getPositionVector().add(0.0, player.getEyeHeight(), 0.0);
               Vec3d coinPos = eyePos.add(
                  VectorMath.rotateByYaw((player.getHeldItemMainhand().getItem().equals(GALATH_COIN) ? 1 : -1) * 0.1F, -0.01F + player.rotationPitch * 0.0015F, 0.0, player.renderYawOffset)
               );
               float progress = (float)(now - startTime - 1000L) / 2000.0F;
               Vec3d lerpedPos = RotationHelper.lerpVec3dDouble(targetPos, coinPos, progress);
               DragonBreathParticle.BREATH_SCALE = 0.2F;
               Minecraft.getMinecraft().effectRenderer.addEffect(new DragonBreathParticle(player.world, lerpedPos.x, lerpedPos.y, lerpedPos.z));
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void handleCoinClick(EntityPlayer player) {
      if (Minecraft.getMinecraft().player.getPersistentID().equals(player.getPersistentID())) {
         GirlSavedData.debugEnabled = true;
      }
   }

   @SideOnly(Side.CLIENT)
   void isSummonWindow(EntityPlayer player, long now, long startTime) {
      if (now > startTime + 1000L && now < startTime + 3000L) {
         Vec3d eyePos = player.getPositionVector().add(0.0, player.getEyeHeight(), 0.0);
         Vec3d coinPos = eyePos.add(
            VectorMath.rotateByYaw((player.getHeldItemMainhand().getItem().equals(GALATH_COIN) ? 1 : -1) * 0.1F, -0.01F + player.rotationPitch * 0.0015F, 0.0, player.renderYawOffset)
         );
         Vec3d summonPos = eyePos.add(player.getLookVec().normalize().scale(2.0));
         float progress = (float)(now - startTime - 1000L) / 2000.0F;
         Vec3d lerpedPos = RotationHelper.lerpVec3dDouble(coinPos, summonPos, progress);
         DragonBreathParticle.BREATH_SCALE = 0.2F;
         Minecraft.getMinecraft().effectRenderer.addEffect(new DragonBreathParticle(player.world, lerpedPos.x, lerpedPos.y, lerpedPos.z));
      }
   }

   @SubscribeEvent
   public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      EntityPlayer player = event.player;
      if (!player.world.isRemote) {
         UUID ownerUuid = GirlSavedData.getOwnerOf(player);
         BaseGirlEntity girl = BaseGirlEntity.getServerGirlEntity(ownerUuid);
         if (girl != null) {
            GirlSavedData.updateMangleliePartner((GalathEntity)girl);
            PacketHandler.networkWrapper.sendTo(new InformOfOwnershipPacket(false), (EntityPlayerMP)player);
         }
      }
   }

   void writeCooldownNBT(EntityPlayer player, NBTTagCompound entityData, long now, long startTime) {
      if (startTime != 0L) {
         if (now - startTime > 4000L) {
            entityData.setLong("sexmod:galath_coin_activation_time", 0L);
            Vec3d eyePos = player.getPositionVector().add(0.0, player.getEyeHeight(), 0.0);
            Vec3d summonPos = eyePos.add(player.getLookVec().normalize().scale(2.0));
            Random random = player.getRNG();

            for (int i = 0; i < 100.0F; i++) {
               player.world
                  .spawnParticle(
                     EnumParticleTypes.DRAGON_BREATH,
                     summonPos.x,
                     summonPos.y,
                     summonPos.z,
                     (2.0F * random.nextFloat() - 1.0F) * 0.2F,
                     (2.0F * random.nextFloat() - 1.0F) * 0.2F,
                     (2.0F * random.nextFloat() - 1.0F) * 0.2F,
                     new int[0]
                  );
            }

            World world = player.world;
            if (world.isRemote) {
               this.handleCoinClick(player);
            } else {
               GalathEntity galath = new GalathEntity(player.world, player, summonPos);
               galath.setPositionAndUpdate(summonPos.x, summonPos.y, summonPos.z);
               GirlSavedData.grantOwnership(player, galath);
               player.world.spawnEntity(galath);
               if (GirlSavedData.isManglelieOwned(player.getPersistentID())) {
                  galath.canStartPussyLicking();
               }
            }
         }
      }
   }

   void handleCoinUse(EntityPlayer player) {
      if (player.world.isRemote) {
         this.summonGalath(player);
      } else {
         this.summonGalathFor(player);
      }
   }

   void summonGalathFor(EntityPlayer player) {
      UUID ownerUuid = GirlSavedData.getOwnerOf(player);
      BaseGirlEntity girl = BaseGirlEntity.getServerGirlEntity(ownerUuid);
      if (girl instanceof GalathEntity) {
         deSummonGalath((GalathEntity)girl);
      }
   }

   public static void deSummonGalath(GalathEntity galath) {
      galath.setCurrentAction(Action.GALATH_DE_SUMMON);
      galath.aC();
      galath.setAnchored(true);
      galath.setTargetPosition(galath.getPositionVector());
      galath.setYawRotation(galath.rotationYaw);
   }

   @SideOnly(Side.CLIENT)
   void summonGalath(EntityPlayer player) {
      GalathEntity galath = null;

      try {
         for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
            if (!girl.isDead && girl.world.isRemote && girl instanceof GalathEntity && player.equals(girl.getMasterPlayer())) {
               galath = (GalathEntity)girl;
               break;
            }
         }
      } catch (ConcurrentModificationException cme) {
      }

      if (galath != null) {
         summonForPlayer(player, galath);
      }
   }

   @SideOnly(Side.CLIENT)
   public static void summonGalathFor(UUID ownerUuid, GalathEntity galath) {
      World world = galath.world;
      Vec3d basePos = galath.isAnchored() ? galath.getTargetPosition() : galath.getPositionVector();
      Vec3d targetPos = basePos.add(0.0, 1.5, 0.0);
      Random random = galath.getRNG();

      for (int i = 0; i < 100.0F; i++) {
         Vec3d offset = new Vec3d((random.nextFloat() * 2.0F - 1.0F) * 1.5F, (random.nextFloat() * 2.0F - 1.0F) * 1.5F, (random.nextFloat() * 2.0F - 1.0F) * 1.5F);
         Vec3d spawnPos = targetPos.add(offset);
         Vec3d velocity = offset.scale(-0.03F);
         world.spawnParticle(
            EnumParticleTypes.DRAGON_BREATH,
            spawnPos.x,
            spawnPos.y,
            spawnPos.z,
            velocity.x,
            velocity.y,
            velocity.z,
            new int[0]
         );
      }

      if (Minecraft.getMinecraft().player.getPersistentID().equals(ownerUuid)) {
         GirlSavedData.debugEnabled = false;
      }
   }

   public static void summonForPlayer(EntityPlayer player, GalathEntity galath) {
      summonGalathFor(player.getPersistentID(), galath);
   }

   void readCooldownNBT(EntityPlayer player, NBTTagCompound entityData, long now, long startTime) {
      if (startTime != 0L) {
         long elapsed = now - startTime;
         World world = player.world;
         boolean animationStarted = entityData.getBoolean("sexmod:galath_coin_de_summoning_animation_time");
         if (!animationStarted && elapsed > 1000L - (world.isRemote ? 0 : 150)) {
            entityData.setBoolean("sexmod:galath_coin_de_summoning_animation_time", true);
            this.handleCoinUse(player);
         }

         if (!world.isRemote) {
            if (now - startTime > 3000L) {
               UUID ownerUuid = GirlSavedData.getOwnerOf(player);
               BaseGirlEntity girl = BaseGirlEntity.getServerGirlEntity(ownerUuid);
               if (girl instanceof GalathEntity) {
                  GirlSavedData.updateMangleliePartner((GalathEntity)girl);
               }
            }
         }
      }
   }

   @Override
   public void registerControllers(AnimationData data) {
      this.controller = new AnimationController<>(this, "controller", 0.0F, this::animationPredicate);
      data.addAnimationController(this.controller);
   }

   @SideOnly(Side.CLIENT)
   protected <segs extends IAnimatable> PlayState animationPredicate(AnimationEvent<segs> event) {
      NBTTagCompound entityData = Minecraft.getMinecraft().player.getEntityData();
      if (entityData.getLong("sexmod:galath_coin_activation_time") == 0L && entityData.getLong("sexmod:galath_coin_deactivation_time") == 0L) {
         event.getController().clearAnimationCache();
         return PlayState.STOP;
      } else {
         this.controller.setAnimation(new AnimationBuilder().addAnimation("animation.galath_coin.summon", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
         return PlayState.CONTINUE;
      }
   }

   @Override
   public AnimationFactory getFactory() {
      return this.animationFactory;
   }

}
