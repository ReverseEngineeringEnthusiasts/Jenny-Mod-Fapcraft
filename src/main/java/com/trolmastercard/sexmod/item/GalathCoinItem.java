package com.trolmastercard.sexmod.item;

import com.trolmastercard.sexmod.client.particle.DragonBreathParticle;
import com.trolmastercard.sexmod.client.renderer.GalathCoinRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.networking.InformOfOwnershipPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.util.GirlSavedData;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ck;







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

public class GalathCoinItem extends Item implements IAnimatable {
   public static final GalathCoinItem r = new GalathCoinItem();
   public static final long c = 4000L;
   public static final long g = 1000L;
   public static final long j = 3000L;
   public static final float q = 0.1F;
   public static final float p = -0.01F;
   public static final float e = 0.0015F;
   public static final float k = 2.0F;
   public static final float h = 1.5F;
   public static final float d = 0.03F;
   public static final float s = 100.0F;
   public static final float l = 0.2F;
   public static final float o = 1.5F;
   public static final String b = "sexmod:galath_coin_activation_time";
   public static final String m = "sexmod:galath_coin_deactivation_time";
   public static final String n = "sexmod:galath_coin_de_summoning_animation_time";
   public static final String f = "Defeating a succubus makes her accept the victor as her master, granting him a coin to which her soul is bound. Using the coin summons her, offering services on demand. If her master uses the coin on her or goes too far, she returns to the coin";
   private final AnimationFactory i = new AnimationFactory(this);
   AnimationController<GalathCoinItem> a;

   public GalathCoinItem() {
      this.field_77777_bU = 1;
   }

   public static void register() {
      r.setRegistryName(new ResourceLocation("sexmod", "galath_coin"));
      r.func_77655_b("galath_coin");
      MinecraftForge.EVENT_BUS.register(GalathCoinItem.class);
   }

   @SubscribeEvent
   public static void a(Register<Item> var0) {
      var0.getRegistry().register(r);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void a(ModelRegistryEvent var0) {
      ModelLoader.setCustomModelResourceLocation(r, 0, new ModelResourceLocation("sexmod:galath_coin"));
      r.setTileEntityItemStackRenderer(new GalathCoinRenderer());
   }

   public ActionResult<ItemStack> func_77659_a(World var1, EntityPlayer var2, EnumHand var3) {
      NBTTagCompound var4 = var2.getEntityData();
      ActionResult var5 = new ActionResult(EnumActionResult.FAIL, var2.func_184586_b(var3));
      if (var4.func_74763_f("sexmod:galath_coin_deactivation_time") != 0L) {
         return var5;
      } else if (var4.func_74763_f("sexmod:galath_coin_activation_time") != 0L) {
         return var5;
      } else if (!this.a(var1, var2)) {
         var1.func_184134_a(var2.field_70165_t, var2.field_70163_u, var2.field_70161_v, SoundHandler.MISC_BEEW[0], SoundCategory.PLAYERS, 1.0F, 1.0F, false);
         return new ActionResult(EnumActionResult.SUCCESS, var2.func_184586_b(var3));
      } else {
         var1.func_184134_a(var2.field_70165_t, var2.field_70163_u, var2.field_70161_v, SoundHandler.MISC_WEOWEO[1], SoundCategory.PLAYERS, 1.0F, 1.0F, false);
         var4.func_74772_a("sexmod:galath_coin_activation_time", System.currentTimeMillis());
         return new ActionResult(EnumActionResult.SUCCESS, var2.func_184586_b(var3));
      }
   }

   boolean a(World var1, EntityPlayer var2) {
      return !var1.field_72995_K ? !GirlSavedData.c_clash849(var2.getPersistentID()) : !GirlSavedData.f;
   }

   @SubscribeEvent
   public void a(EntityInteract var1) {
      EntityPlayer var2 = var1.getEntityPlayer();
      ItemStack var3 = var2.func_184586_b(var1.getHand());
      if (r.equals(var3.func_77973_b())) {
         Entity var4 = var1.getTarget();
         if (var4 instanceof GalathEntity) {
            GalathEntity var5 = (GalathEntity)var4;
            if (var2.getPersistentID().equals(var5.O_clash527())) {
               var2.field_70170_p
                  .func_184134_a(
                     var2.field_70165_t, var2.field_70163_u, var2.field_70161_v, SoundHandler.MISC_WEOWEO[0], SoundCategory.PLAYERS, 1.0F, 1.0F, false
                  );
               var2.getEntityData().func_74772_a("sexmod:galath_coin_deactivation_time", System.currentTimeMillis());
               var1.setCanceled(true);
            }
         }
      }
   }

   public void func_77663_a(ItemStack var1, World var2, Entity var3, int var4, boolean var5) {
      super.func_77663_a(var1, var2, var3, var4, var5);
      if (var3 instanceof EntityPlayer) {
         EntityPlayer var6 = (EntityPlayer)var3;
         NBTTagCompound var7 = var6.getEntityData();
         long var8 = var7.func_74763_f("sexmod:galath_coin_activation_time");
         long var10 = var7.func_74763_f("sexmod:galath_coin_deactivation_time");
         long var12 = System.currentTimeMillis();
         this.b(var6, var7, var12, var8);
         this.a(var6, var7, var12, var10);
         if (var10 != 0L && var12 > var10 + 4000L) {
            var7.func_74772_a("sexmod:galath_coin_deactivation_time", 0L);
            var7.func_74757_a("sexmod:galath_coin_de_summoning_animation_time", false);
         }

         if (var2.field_72995_K) {
            this.a(var6, var12, var8);
            this.b(var6, var12, var10);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void b(EntityPlayer var1, long var2, long var4) {
      if (var4 != 0L) {
         if (var2 > var4 + 1000L && var2 < var4 + 3000L) {
            GalathEntity var6 = null;

            try {
               for (BaseGirlEntity var8 : BaseGirlEntity.getGirlEntityList()) {
                  if (!var8.field_70128_L && var8.field_70170_p.field_72995_K && var8 instanceof GalathEntity && var1.equals(var8.z_clash528())) {
                     var6 = (GalathEntity)var8;
                     break;
                  }
               }
            } catch (ConcurrentModificationException var12) {
            }

            if (var6 != null) {
               Vec3d var13 = var6.getTargetPosition().func_72441_c(0.0, 1.5, 0.0);
               Vec3d var14 = var1.func_174791_d().func_72441_c(0.0, var1.func_70047_e(), 0.0);
               Vec3d var9 = var14.func_178787_e(
                  ck.a((var1.func_184614_ca().func_77973_b().equals(r) ? 1 : -1) * 0.1F, -0.01F + var1.field_70125_A * 0.0015F, 0.0, var1.field_70761_aq)
               );
               float var10 = (float)(var2 - var4 - 1000L) / 2000.0F;
               Vec3d var11 = RotationHelper.a(var13, var9, var10);
               DragonBreathParticle.b = 0.2F;
               Minecraft.func_71410_x().field_71452_i.func_78873_a(new DragonBreathParticle(var1.field_70170_p, var11.field_72450_a, var11.field_72448_b, var11.field_72449_c));
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void a_clash182(EntityPlayer var1) {
      if (Minecraft.func_71410_x().field_71439_g.getPersistentID().equals(var1.getPersistentID())) {
         GirlSavedData.f = true;
      }
   }

   @SideOnly(Side.CLIENT)
   void a(EntityPlayer var1, long var2, long var4) {
      if (var2 > var4 + 1000L && var2 < var4 + 3000L) {
         Vec3d var6 = var1.func_174791_d().func_72441_c(0.0, var1.func_70047_e(), 0.0);
         Vec3d var7 = var6.func_178787_e(
            ck.a((var1.func_184614_ca().func_77973_b().equals(r) ? 1 : -1) * 0.1F, -0.01F + var1.field_70125_A * 0.0015F, 0.0, var1.field_70761_aq)
         );
         Vec3d var8 = var6.func_178787_e(var1.func_70040_Z().func_72432_b().func_186678_a(2.0));
         float var9 = (float)(var2 - var4 - 1000L) / 2000.0F;
         Vec3d var10 = RotationHelper.a(var7, var8, var9);
         DragonBreathParticle.b = 0.2F;
         Minecraft.func_71410_x().field_71452_i.func_78873_a(new DragonBreathParticle(var1.field_70170_p, var10.field_72450_a, var10.field_72448_b, var10.field_72449_c));
      }
   }

   @SubscribeEvent
   public void a(PlayerChangedDimensionEvent var1) {
      EntityPlayer var2 = var1.player;
      if (!var2.field_70170_p.field_72995_K) {
         UUID var3 = GirlSavedData.b_clash853(var2);
         BaseGirlEntity var4 = BaseGirlEntity.getServerGirlEntity(var3);
         if (var4 != null) {
            GirlSavedData.a_clash848((GalathEntity)var4);
            PacketHandler.b.sendTo(new InformOfOwnershipPacket(false), (EntityPlayerMP)var2);
         }
      }
   }

   void b(EntityPlayer var1, NBTTagCompound var2, long var3, long var5) {
      if (var5 != 0L) {
         if (var3 - var5 > 4000L) {
            var2.func_74772_a("sexmod:galath_coin_activation_time", 0L);
            Vec3d var7 = var1.func_174791_d().func_72441_c(0.0, var1.func_70047_e(), 0.0);
            Vec3d var8 = var7.func_178787_e(var1.func_70040_Z().func_72432_b().func_186678_a(2.0));
            Random var9 = var1.func_70681_au();

            for (int var10 = 0; var10 < 100.0F; var10++) {
               var1.field_70170_p
                  .func_175688_a(
                     EnumParticleTypes.DRAGON_BREATH,
                     var8.field_72450_a,
                     var8.field_72448_b,
                     var8.field_72449_c,
                     (2.0F * var9.nextFloat() - 1.0F) * 0.2F,
                     (2.0F * var9.nextFloat() - 1.0F) * 0.2F,
                     (2.0F * var9.nextFloat() - 1.0F) * 0.2F,
                     new int[0]
                  );
            }

            World var12 = var1.field_70170_p;
            if (var12.field_72995_K) {
               this.a_clash182(var1);
            } else {
               GalathEntity var11 = new GalathEntity(var1.field_70170_p, var1, var8);
               var11.func_70634_a(var8.field_72450_a, var8.field_72448_b, var8.field_72449_c);
               GirlSavedData.a(var1, var11);
               var1.field_70170_p.func_72838_d(var11);
               if (GirlSavedData.b_clash846(var1.getPersistentID())) {
                  var11.v_clash675();
               }
            }
         }
      }
   }

   void d_clash183(EntityPlayer var1) {
      if (var1.field_70170_p.field_72995_K) {
         this.b_clash186(var1);
      } else {
         this.c_clash184(var1);
      }
   }

   void c_clash184(EntityPlayer var1) {
      UUID var2 = GirlSavedData.b_clash853(var1);
      BaseGirlEntity var3 = BaseGirlEntity.getServerGirlEntity(var2);
      if (var3 instanceof GalathEntity) {
         a_clash185((GalathEntity)var3);
      }
   }

   public static void a_clash185(GalathEntity var0) {
      var0.b(fp.GALATH_DE_SUMMON);
      var0.aC();
      var0.setAnchored(true);
      var0.setTargetPosition(var0.func_174791_d());
      var0.setYawRotation(var0.field_70177_z);
   }

   @SideOnly(Side.CLIENT)
   void b_clash186(EntityPlayer var1) {
      GalathEntity var2 = null;

      try {
         for (BaseGirlEntity var4 : BaseGirlEntity.getGirlEntityList()) {
            if (!var4.field_70128_L && var4.field_70170_p.field_72995_K && var4 instanceof GalathEntity && var1.equals(var4.z_clash528())) {
               var2 = (GalathEntity)var4;
               break;
            }
         }
      } catch (ConcurrentModificationException var5) {
      }

      if (var2 != null) {
         a(var1, var2);
      }
   }

   @SideOnly(Side.CLIENT)
   public static void a(UUID var0, GalathEntity var1) {
      World var2 = var1.field_70170_p;
      Vec3d var3 = var1.isAnchored() ? var1.getTargetPosition() : var1.func_174791_d();
      Vec3d var4 = var3.func_72441_c(0.0, 1.5, 0.0);
      Random var5 = var1.func_70681_au();

      for (int var6 = 0; var6 < 100.0F; var6++) {
         Vec3d var7 = new Vec3d((var5.nextFloat() * 2.0F - 1.0F) * 1.5F, (var5.nextFloat() * 2.0F - 1.0F) * 1.5F, (var5.nextFloat() * 2.0F - 1.0F) * 1.5F);
         Vec3d var8 = var4.func_178787_e(var7);
         Vec3d var9 = var7.func_186678_a(-0.03F);
         var2.func_175688_a(
            EnumParticleTypes.DRAGON_BREATH,
            var8.field_72450_a,
            var8.field_72448_b,
            var8.field_72449_c,
            var9.field_72450_a,
            var9.field_72448_b,
            var9.field_72449_c,
            new int[0]
         );
      }

      if (Minecraft.func_71410_x().field_71439_g.getPersistentID().equals(var0)) {
         GirlSavedData.f = false;
      }
   }

   public static void a(EntityPlayer var0, GalathEntity var1) {
      a(var0.getPersistentID(), var1);
   }

   void a(EntityPlayer var1, NBTTagCompound var2, long var3, long var5) {
      if (var5 != 0L) {
         long var7 = var3 - var5;
         World var9 = var1.field_70170_p;
         boolean var10 = var2.func_74767_n("sexmod:galath_coin_de_summoning_animation_time");
         if (!var10 && var7 > 1000L - (var9.field_72995_K ? 0 : 150)) {
            var2.func_74757_a("sexmod:galath_coin_de_summoning_animation_time", true);
            this.d_clash183(var1);
         }

         if (!var9.field_72995_K) {
            if (var3 - var5 > 3000L) {
               UUID var11 = GirlSavedData.b_clash853(var1);
               BaseGirlEntity var12 = BaseGirlEntity.getServerGirlEntity(var11);
               if (var12 instanceof GalathEntity) {
                  GirlSavedData.a_clash848((GalathEntity)var12);
               }
            }
         }
      }
   }

   @Override
   public void registerControllers(AnimationData var1) {
      this.a = new AnimationController<>(this, "controller", 0.0F, this::a);
      var1.addAnimationController(this.a);
   }

   @SideOnly(Side.CLIENT)
   protected <segs extends IAnimatable> PlayState a(AnimationEvent<segs> var1) {
      NBTTagCompound var2 = Minecraft.func_71410_x().field_71439_g.getEntityData();
      if (var2.func_74763_f("sexmod:galath_coin_activation_time") == 0L && var2.func_74763_f("sexmod:galath_coin_deactivation_time") == 0L) {
         var1.getController().clearAnimationCache();
         return PlayState.STOP;
      } else {
         this.a.setAnimation(new AnimationBuilder().addAnimation("animation.galath_coin.summon", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
         return PlayState.CONTINUE;
      }
   }

   @Override
   public AnimationFactory getFactory() {
      return this.i;
   }

}
