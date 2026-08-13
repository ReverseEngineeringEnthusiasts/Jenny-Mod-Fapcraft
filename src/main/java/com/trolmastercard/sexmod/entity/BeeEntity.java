package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeDialogueScreen;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.ai.GirlGotoGoal;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.potion.HornyPotion;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.d3;







import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWaterFlying;
import net.minecraft.entity.ai.EntityFlyHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class BeeEntity extends BeeEntityBase {
   public float N = 3200.0F;
   int P = 0;
   static final float O = 4800.0F;
   static final float Q = 10.0F;
   public static final DataParameter<Boolean> M = EntityDataManager.func_187226_a(BeeEntity.class, DataSerializers.field_187198_h)
      .func_187156_b()
      .func_187161_a(112);

   public BeeEntity(World var1) {
      super(var1);
      this.field_70765_h = new EntityFlyHelper(this);
      this.func_70105_a(0.3F, 1.5F);
   }

   @Override
   public String c_clash241() {
      return "Bee";
   }

   @Override
   public float i_clash226() {
      return -0.1F;
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      this.m.func_187214_a(M, false);
   }

   protected PathNavigate func_175447_b(World var1) {
      PathNavigateFlying var2 = new PathNavigateFlying(this, var1);
      var2.func_192879_a(false);
      var2.func_192877_c(true);
      var2.func_192878_b(true);
      this.f = var2;
      return var2;
   }

   @Override
   protected void func_110147_ax() {
      this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111267_a);
      this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111266_c);
      this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111263_d);
      this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_188791_g);
      this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_189429_h);
      this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111265_b).func_111128_a(16.0);
      this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_193334_e);
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(12.0);
      this.func_110148_a(SharedMonsterAttributes.field_193334_e).func_111128_a(0.4F);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.2F);
   }

   @Override
   protected void func_184651_r() {
      this.o = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.field_70714_bg.func_75776_a(0, new GirlGotoGoal(this));
      this.field_70714_bg.func_75776_a(1, new EntityAIPanic(this, 1.25));
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(2, this.o);
      this.field_70714_bg.func_75776_a(3, new EntityAIWanderAvoidWaterFlying(this, 1.0));
   }

   @Override
   public void func_70619_bc() {
      super.func_70619_bc();
      if (this.func_70644_a(HornyPotion.b) && this.N < 4800.0F && this.ae_clash498() == null) {
         this.func_184589_d(HornyPotion.b);
         this.N = 6.9420184E7F;
      }

      this.c_clash752();
      if (this.y_clash492().equals(fp.CITIZEN_CUM)) {
         this.P = Math.max(1, this.P);
      }

      this.a_clash754();
      this.b_clash753();
   }

   @Override
   public void b(fp var1) {
      if (this.y_clash492() != fp.CITIZEN_CUM || var1 != fp.CITIZEN_FAST && var1 != fp.COWGIRLSLOW) {
         super.b(var1);
      }
   }

   void c_clash752() {
      if (this.ae_clash498() == null) {
         if (!this.J_clash526()) {
            this.N++;
            if (!(this.N < 4800.0F)) {
               EntityPlayer var1 = this.field_70170_p.func_72890_a(this, 10.0);
               if (var1 != null) {
                  if (d_clash532(var1) == null) {
                     if (!AbstractPlayerGirlEntity.e(var1)) {
                        if (var1.func_70032_d(this) < 1.5F) {
                           this.N = 0.0F;
                           this.e_clash499(var1.getPersistentID());
                           this.m.func_187227_b(G, true);
                           this.c_clash502(this.aa_clash545());
                           this.b_clash431(var1.field_70177_z - 180.0F);
                           this.f.func_75499_g();
                           PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var1);
                           this.b(fp.CITIZEN_START);
                           Vec3d var2 = this.a_clash546(0.2);
                           var1.func_70634_a(var2.field_72450_a, var2.field_72448_b, var2.field_72449_c);
                        } else {
                           this.f.func_75499_g();
                           this.f.func_75497_a(var1, 1.0);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   void b_clash753() {
      RayTraceResult var1 = this.field_70170_p.func_72933_a(this.func_174791_d(), new Vec3d(this.field_70165_t, 0.0, this.field_70161_v));
      if (var1 != null) {
         BlockPos var2 = var1.func_178782_a();
         double var3 = this.field_70163_u - var2.func_177956_o();
         if (var3 > 3.0 && this.field_70181_x > 0.0) {
            this.field_70181_x = 0.0;
         }
      }
   }

   void a_clash754() {
      if (this.P != 0) {
         this.P++;
         if ((Boolean)this.m.func_187225_a(M)) {
            if (this.P < 40) {
               for (EntityPlayer var2 : this.field_70170_p.field_73010_i) {
                  if (var2.func_70032_d(this) < 15.0F) {
                     ((EntityPlayerMP)var2)
                        .field_71135_a
                        .func_147359_a(
                           new SPacketParticles(
                              EnumParticleTypes.HEART,
                              true,
                              (float)this.field_70165_t,
                              (float)this.field_70163_u + 0.3F,
                              (float)this.field_70161_v,
                              0.2F,
                              0.3F,
                              0.2F,
                              0.25F,
                              1,
                              new int[0]
                           )
                        );
                  }
               }
            } else {
               this.P = 0;
            }
         } else if (this.P < 200) {
            for (EntityPlayer var6 : this.field_70170_p.field_73010_i) {
               if (var6.func_70032_d(this) < 15.0F) {
                  ((EntityPlayerMP)var6)
                     .field_71135_a
                     .func_147359_a(
                        new SPacketParticles(
                           EnumParticleTypes.SPELL,
                           true,
                           (float)this.field_70165_t,
                           (float)this.field_70163_u + 0.3F,
                           (float)this.field_70161_v,
                           0.2F,
                           0.3F,
                           0.2F,
                           0.25F,
                           1,
                           new int[0]
                        )
                     );
               }
            }
         } else if (this.P == 200) {
            this.m.func_187227_b(M, this.func_70681_au().nextBoolean());
         } else if (this.P < 250) {
            for (EntityPlayer var7 : this.field_70170_p.field_73010_i) {
               if (var7.func_70032_d(this) < 15.0F) {
                  ((EntityPlayerMP)var7)
                     .field_71135_a
                     .func_147359_a(
                        new SPacketParticles(
                           this.m.func_187225_a(M) ? EnumParticleTypes.HEART : EnumParticleTypes.VILLAGER_ANGRY,
                           true,
                           (float)this.field_70165_t,
                           (float)this.field_70163_u + 0.3F,
                           (float)this.field_70161_v,
                           0.2F,
                           0.3F,
                           0.2F,
                           0.25F,
                           3,
                           new int[0]
                        )
                     );
               }
            }
         } else {
            this.P = 0;
         }

         for (EntityPlayer var8 : this.field_70170_p.field_73010_i) {
            if (var8.func_70032_d(this) < 15.0F) {
               ((EntityPlayerMP)var8)
                  .field_71135_a
                  .func_147359_a(
                     new SPacketParticles(
                        EnumParticleTypes.SPELL,
                        true,
                        (float)this.field_70165_t,
                        (float)this.field_70163_u + 0.3F,
                        (float)this.field_70161_v,
                        0.2F,
                        0.3F,
                        0.2F,
                        0.25F,
                        10,
                        new int[0]
                     )
                  );
            }
         }
      }
   }

   @Override
   public void func_70071_h_() {
      super.func_70071_h_();
      if (this.N < 4800.0F && !this.field_70122_E && this.field_70181_x < 0.0) {
         this.field_70181_x *= 0.4;
      }
   }

   public void func_180430_e(float var1, float var2) {
   }

   protected boolean func_184645_a(EntityPlayer var1, EnumHand var2) {
      if ((Boolean)this.m.func_187225_a(M)
         && !(Boolean)this.m.func_187225_a(K)
         && var1.func_184586_b(var2).func_77973_b() == Item.func_150898_a(Blocks.field_150486_ae)) {
         this.m.func_187227_b(K, true);
         var1.func_184586_b(var2).func_190918_g(1);
         return super.func_184645_a(var1, var2);
      }

      if (this.field_70170_p.field_72995_K && (Boolean)this.m.func_187225_a(M)) {
         this.b_clash755(var1);
      }

      return super.func_184645_a(var1, var2);
   }

   @SideOnly(Side.CLIENT)
   void b_clash755(EntityPlayer var1) {
      Minecraft.func_71410_x().func_147108_a(new BeeDialogueScreen(this, var1));
   }

   @Override
   public boolean b_clash230(EntityPlayer var1) {
      return false;
   }

   @Override
   public void a(String var1, UUID var2) {
   }

   @Override
   protected fp c_clash235(fp var1) {
      return var1 == fp.CITIZEN_SLOW ? fp.CITIZEN_FAST : null;
   }

   @Override
   protected fp a_clash236(fp var1) {
      return var1 != fp.CITIZEN_FAST && var1 != fp.CITIZEN_SLOW ? null : fp.CITIZEN_CUM;
   }

   @Override
   protected void U() {
   }

   @Override
   public void func_70014_b(NBTTagCompound var1) {
      super.func_70014_b(var1);
      var1.func_74757_a("isTamed", (Boolean)this.m.func_187225_a(M));
      var1.func_74757_a("hasChest", (Boolean)this.m.func_187225_a(K));
      var1.func_74782_a("inventory", this.L.serializeNBT());
   }

   public void func_70020_e(NBTTagCompound var1) {
      super.func_70020_e(var1);
      if (var1.func_74764_b("isTamed")) {
         this.m.func_187227_b(M, var1.func_74767_n("isTamed"));
      }

      this.m.func_187227_b(K, var1.func_74767_n("hasChest"));
      this.L.deserializeNBT(var1.func_74775_l("inventory"));
   }

   @SideOnly(Side.CLIENT)
   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      if (this.field_70170_p instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      switch (var1.getController().getName()) {
         case "movement":
            if (this.y_clash492() != fp.NULL) {
               this.a("animation.bee.null", true, var1);
            } else {
               this.a("animation.bee." + (this.m.func_187225_a(K) ? "idle_has_chest" : "idle"), true, var1);
            }
            break;
         case "action":
            switch (this.y_clash492()) {
               case CITIZEN_START:
                  this.a("animation.bee.sex_start", false, var1);
                  break;
               case CITIZEN_SLOW:
                  this.a("animation.bee.sex_slow", true, var1);
                  break;
               case CITIZEN_FAST:
                  this.a("animation.bee.sex_fast", true, var1);
                  break;
               case CITIZEN_CUM:
                  this.a("animation.bee.sex_cum", false, var1);
                  break;
               case THROW_PEARL:
                  this.a("animation.bee.throw_pearl", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

   @Override
   public void registerControllers(AnimationData var1) {
      if (this.C == null) {
         this.p_clash506();
      }

      AnimationController.ISoundListener var2 = var1x -> {
         switch (var1x.sound) {
            case "pearl":
               if (this.e_clash544() && this.y_clash492() == fp.THROW_PEARL) {
                  PacketHandler.b.sendToServer(new SendCompanionHomePacket(this.f_clash491()));
               }
               break;
            case "resetCumPercentage":
               if (this.n_clash537()) {
                  HornyMeterHud.b_clash363();
               }
               break;
            case "sex_fastMSG1":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING));
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.04F);
               }
               break;
            case "sex_startMSG1":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING));
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.02F);
               }
               break;
            case "sex_fastDone":
               if (!this.n_clash537() || d3.d) {
                  return;
               }
            case "sex_startDone":
               this.b(fp.CITIZEN_SLOW);
               if (this.n_clash537()) {
                  HornyMeterHud.d_clash358();
               }
               break;
            case "sex_cumMSG1":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_CUMINFLATION), 2.0F);
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING));
               break;
            case "blackscreen":
               if (this.n_clash537()) {
                  BeeScreen.b_clash732();
               }
               break;
            case "sex_cumDone":
               if (this.n_clash537()) {
                  HornyMeterHud.b_clash363();
                  this.r_clash533();
               }
               break;
            case "sex_fastReady":
               if (this.n_clash537() && d3.d) {
                  this.N();
               }
         }
      };
      this.C.registerSoundListener(var2);
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.E);
      var1.addAnimationController(this.s);
   }

   private static RuntimeException b(RuntimeException var0) {
      return var0;
   }
}
