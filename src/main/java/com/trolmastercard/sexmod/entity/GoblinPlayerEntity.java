package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.by;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.GirlInventoryScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.AllieModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.dg;
import com.trolmastercard.sexmod.util.eh;
import com.trolmastercard.sexmod.util.g5;







import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class GoblinPlayerEntity extends AbstractKoboldPlayerEntity implements IGoblin {
   public static final float aI = 2.0F;
   public static final DataParameter<String> ax = EntityDataManager.func_187226_a(GoblinPlayerEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(122);
   public static final DataParameter<Boolean> aA = EntityDataManager.func_187226_a(GoblinPlayerEntity.class, DataSerializers.field_187198_h)
      .func_187156_b()
      .func_187161_a(126);
   int aJ = 0;
   int az = -1;
   int aG = 0;
   fp aw = fp.NULL;
   int aE = -1;
   boolean aC = false;
   boolean aB = true;
   boolean ay = true;
   boolean aF = false;
   boolean aH = false;
   String aD = "";

   public GoblinPlayerEntity(World var1) {
      super(var1);
   }

   public GoblinPlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public float i_clash226() {
      return 0.9F;
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new AllieModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return "textures/entity/kobold/hand.png";
   }

   @Override
   public Vec3i getHandColor(int var1) {
      String[] var2 = a_clash702(this);
      return var2.length < 8 ? super.getHandColor(var1) : by.values()[Integer.parseInt(var2[7])].a_clash189();
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      eh var1 = eh.values()[this.func_70681_au().nextInt(eh.values().length)];
      this.m.func_187214_a(au, new BlockPos(var1.a_clash565()));
      this.m.func_187214_a(as, GoblinEntity.ax.name());
      this.m.func_187214_a(aA, false);
      this.m.func_187214_a(ax, "");
   }

   @Override
   public void b(String var1, UUID var2) {
      if ("anal".equals(var1)) {
         this.b_clash577(var2);
         this.b(fp.NELSON_INTRO);
         this.a(this.getOutfitIndex(), fp.NELSON_INTRO);
         this.f(0);
      }

      if ("paizuri".equals(var1)) {
         this.b_clash577(var2);
         this.b(fp.PAIZURI_START);
         this.a(this.getOutfitIndex(), fp.PAIZURI_START);
         this.f(0);
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      Minecraft.func_71410_x().func_147108_a(new GirlInventoryScreen(this, var1, new String[]{"anal", "paizuri"}, null, false));
      return true;
   }

   @Override
   public EntityPlayer resolvePlayerEntity(EntityPlayer var1) {
      UUID var2 = this.getOwnerUUID();
      if (var2 == null) {
         return var1;
      }

      EntityPlayer var3 = this.field_70170_p.func_152378_a(var2);
      return var3 == null ? var1 : var3;
   }

   @Override
   public boolean d_clash453() {
      return this.getOwnerUUID() == null || !Minecraft.func_71410_x().field_71439_g.getPersistentID().equals(this.getOwnerUserUUID());
   }

   @Override
   public boolean z_clash454() {
      UUID var1 = this.getOwnerUUID();
      return var1 == null;
   }

   @Override
   public Vec3d c(Vec3d var1, float var2) {
      UUID var3 = this.getOwnerUUID();
      if (var3 == null) {
         return var1;
      }

      EntityPlayer var4 = this.field_70170_p.func_152378_a(var3);
      if (var4 == null) {
         return var1;
      }

      Vec3d var5 = var4.func_174791_d();
      Vec3d var6 = new Vec3d(var4.field_70142_S, var4.field_70137_T, var4.field_70136_U);
      return RotationHelper.a(var6, var5, var2);
   }

   void c_clash455(EntityPlayer var1) {
      if (this.getCurrentAction() == fp.NULL) {
         if (this.getOwnerUUID() == null) {
            if (GoblinEntity.d_clash248(var1.getPersistentID())) {
               var1.func_146105_b(new TextComponentString("you are already carrying a Goblin"), true);
            } else {
               this.setOwnerUUID(var1.getPersistentID());
               this.b(fp.PICK_UP);
               this.b_clash63(45);
               EntityPlayer var2 = this.k_clash584();
               if (var2 != null) {
                  var2.func_189654_d(true);
                  var2.field_70145_X = true;
                  if (!this.field_70170_p.field_72995_K) {
                     PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var2);
                  }
               }
            }
         }
      }
   }

   @Override
   protected String a(StringBuilder var1) {
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 3);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 7);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 7);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 5);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, g5.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, by.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, eh.values().length - 1);
      AbstractNpcOnlyEntity.c(var1, 0);
      return var1.toString();
   }

   @Override
   public ArrayList<Integer> D_clash243() {
      return new ArrayList<Integer>() {
         {
            this.add(4);
            this.add(3);
            this.add(3);
            this.add(16);
            this.add(16);
            this.add(6);
            this.add(g5.values().length);
            this.add(by.values().length);
            this.add(eh.values().length);
         }
      };
   }

   @Override
   public List<Integer> u_clash244() {
      return Collections.singletonList(2);
   }

   @Override
   protected void a_clash354() {
      dg.e_clash190();
      GoblinRenderer.clearBoneColors();
   }

   public float func_70047_e() {
      return 0.75F;
   }

   @Override
   public boolean o_clash456() {
      return this.isAnchored() || this.getOwnerUUID() != null;
   }

   @Override
   public boolean a(fp var1, EntityPlayer var2) {
      UUID var3 = this.getOwnerUUID();
      if (var3 == null) {
         return false;
      }

      EntityPlayer var4 = this.field_70170_p.func_152378_a(var3);
      if (var4 == null) {
         return false;
      }

      float var5 = var2.field_70177_z;
      float var6 = var1 == fp.PICK_UP ? 180.0F : 0.0F;
      float var7 = var4.field_70177_z - 90.0F + var6;
      float var8 = var4.field_70177_z + 90.0F + var6;
      if (var5 < var7) {
         var2.field_70177_z = var7;
      }

      if (var5 > var8) {
         var2.field_70177_z = var8;
      }

      float var9 = var2.field_70125_A;
      float var10 = var1 == fp.PICK_UP ? 0.0F : 37.5F;
      if (var9 > var10) {
         var2.field_70125_A = var10;
      }

      return true;
   }

   @Override
   public Vec3d b(Vec3d var1, float var2) {
      UUID var3 = this.getOwnerUUID();
      if (var3 == null) {
         return var1;
      }

      EntityPlayer var4 = this.field_70170_p.func_152378_a(var3);
      if (var4 == null) {
         return var1;
      }

      float var5 = RotationHelper.lerp(var4.field_70760_ar, var4.field_70761_aq, var2);
      Vec3d var6 = var1;
      float var7 = 135.0F;
      fp var8 = this.getCurrentAction();
      if (var8 == fp.PICK_UP) {
         var6 = new Vec3d(var1.field_72450_a, var1.field_72448_b, -var1.field_72449_c);
         var7 = 175.0F;
      } else if (var8 != fp.START_THROWING) {
         var6 = var6.func_178786_a(0.0, 2.0, 0.0);
      }

      return ck.rotateByYaw(var6, var5 + var7);
   }

   @SideOnly(Side.CLIENT)
   void f_clash457() {
      EntityPlayer var1 = this.k_clash584();
      if (var1 != null) {
         if (this.getCurrentAction() == fp.START_THROWING) {
            var1.field_70128_L = false;
            if (!this.field_70170_p.field_72996_f.contains(var1)) {
               this.field_70170_p.func_72838_d(var1);
            }
         }
      }
   }

   @Override
   public void func_70071_h_() {
      GoblinEntity.e_clash273(this);
      this.d_clash460();
      this.j_clash459();
      super.func_70071_h_();
      if (this.field_70170_p.field_72995_K) {
         this.f_clash457();
         fp var1 = this.getCurrentAction();
         this.d(var1);
         this.c_clash464(var1);
         this.aw = var1;
      }
   }

   @Override
   public boolean E_clash458() {
      return this.getOwnerUUID() != null;
   }

   void j_clash459() {
      fp var1 = this.getCurrentAction();
      if (var1 != fp.THROWN) {
         if (var1 != fp.START_THROWING || this.a_clash58() <= 15) {
            UUID var2 = this.getOwnerUUID();
            if (var2 != null) {
               EntityPlayer var3 = this.field_70170_p.func_152378_a(var2);
               if (var3 != null) {
                  EntityPlayer var4 = this.k_clash584();
                  if (var4 != null) {
                     var4.field_70145_X = true;
                     var4.func_189654_d(true);
                     var4.func_70107_b(var3.field_70165_t, var3.field_70163_u + 2.0, var3.field_70161_v);
                  }
               }
            }
         }
      }
   }

   void d_clash460() {
      GoblinPlayerEntity var1 = this;
      int var2 = var1.a_clash58();
      if (var2 != -1) {
         var1.c_clash57(++var2);
         EntityPlayer var3 = this.k_clash584();
         if (var3 != null) {
            if (var2 == 15) {
               GoblinEntity.b_clash264(this);
               float var5 = GoblinEntity.d_clash266(this);
               float var6 = GoblinEntity.c_clash265(this);
               if (this.field_70170_p.field_72995_K && this.f_clash579()) {
                  d3.setMovementLock(true);
               }

               Vec3d var7 = GoblinEntity.a(new Vec3d(0.0, 0.0, 1.5), var5, var6);
               var3.field_70159_w = var7.field_72450_a;
               var3.field_70181_x = var7.field_72448_b;
               var3.field_70179_y = var7.field_72449_c;
               if (!this.field_70170_p.field_72995_K) {
                  this.setYawRotation(var6);
               }
            }

            var3.field_70145_X = false;
            var3.func_189654_d(false);
            if (var2 == 39) {
               this.c_clash57(-1);
               this.b(fp.THROWN);
               this.setInteractionPlayerUUID(null);
               this.setOwnerUUID(null);
            }
         }
      }
   }

   @Override
   public void func_70619_bc() {
      super.func_70619_bc();
      GoblinEntity.a_clash282(this);
      this.o_clash462();
      this.e_clash461();
   }

   void e_clash461() {
      if (this.getCurrentAction() == fp.STAND_UP) {
         if (++this.aJ >= 37) {
            this.aJ = 0;
            this.b(fp.NULL);
         }
      }
   }

   void o_clash462() {
      if (this.getCurrentAction() == fp.THROWN) {
         EntityPlayer var1 = this.k_clash584();
         if (var1 != null) {
            if (var1.field_70122_E) {
               int var2 = this.d_clash60() + 1;
               this.a_clash59(var2);
               if (var2 >= 30) {
                  this.a_clash59(0);
                  this.b(fp.STAND_UP);
               }
            }
         }
      }
   }

   @Nullable
   @Override
   public UUID getOwnerUUID() {
      String var1 = (String)this.m.func_187225_a(ax);
      if ("".equals(var1)) {
         return null;
      }

      try {
         return UUID.fromString((String)this.m.func_187225_a(ax));
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   @Override
   public void setOwnerUUID(UUID var1) {
      if (var1 == null) {
         this.m.func_187227_b(ax, "");
      } else {
         this.m.func_187227_b(ax, var1.toString());
      }
   }

   public EntityPlayer r_clash463() {
      UUID var1 = this.getOwnerUUID();
      return var1 == null ? null : this.field_70170_p.func_152378_a(var1);
   }

   @Override
   public void c_clash57(int var1) {
      this.az = var1;
   }

   @Override
   public int a_clash58() {
      return this.az;
   }

   @Override
   public void a_clash59(int var1) {
      this.aG = var1;
   }

   @Override
   public int d_clash60() {
      return this.aG;
   }

   @Override
   public void a_clash61(fp var1) {
      this.aw = var1;
   }

   @Override
   public fp b_clash62() {
      return this.aw;
   }

   @Override
   public void b_clash63(int var1) {
      this.aE = var1;
   }

   @Override
   public int c_clash56() {
      return this.aE;
   }

   @Override
   public void reinitTasks() {
      super.reinitTasks();
      this.m.func_187227_b(aA, false);
      if (this.getOwnerUUID() != null) {
         this.setOwnerUUID(null);
         EntityPlayer var1 = this.k_clash584();
         if (var1 != null) {
            PacketHandler.b.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)var1);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void c_clash464(fp var1) {
      if (var1 == fp.NELSON_FAST && this.aw != fp.NELSON_FAST) {
         this.aF = false;
      }
   }

   @SideOnly(Side.CLIENT)
   void d(fp var1) {
      Minecraft var2 = Minecraft.func_71410_x();
      if (var2.field_71439_g.getPersistentID().equals(this.getInteractionPlayerUUID())) {
         if (var2.field_71474_y.field_74320_O == 0) {
            switch (var1) {
               case NELSON_CUM:
               case NELSON_FAST:
               case NELSON_INTRO:
               case NELSON_SLOW:
                  var2.field_71474_y.field_74320_O = 2;
            }
         }
      }
   }

   @Override
   public void a_clash245(List<Integer> var1) {
      StringBuilder var2 = new StringBuilder();

      for (int var4 : var1) {
         AbstractNpcOnlyEntity.c(var2, var4);
      }

      AbstractNpcOnlyEntity.c(var2, 1);
      this.m.func_187227_b(at, var2.toString());
   }

   @Nullable
   @Override
   protected fp getNextAction(fp var1) {
      switch (var1) {
         case NELSON_SLOW:
            return fp.NELSON_FAST;
         case PAIZURI_IDLE:
         case PAIZURI_SLOW:
            return fp.PAIZURI_FAST;
         case BREEDING_SLOW_0:
            return fp.BREEDING_FAST_0;
         case BREEDING_SLOW_2:
            return fp.BREEDING_FAST_2;
         default:
            return null;
      }
   }

   @Override
   public void b(fp var1) {
      fp var2 = this.getCurrentAction();
      if (var2 != fp.PAIZURI_CUM || var1 != fp.PAIZURI_SLOW && var1 != fp.PAIZURI_FAST) {
         if (var2 != fp.NELSON_CUM || var1 != fp.NELSON_SLOW && var1 != fp.NELSON_FAST) {
            if (var2 != fp.BREEDING_CUM_0 || var1 != fp.BREEDING_SLOW_0 && var1 != fp.BREEDING_FAST_0) {
               if (var1 == fp.PAIZURI_START && !this.field_70170_p.field_72995_K) {
                  this.m_clash466();
               }

               if (var1 == fp.NELSON_INTRO && !this.field_70170_p.field_72995_K) {
                  this.q_clash465();
               }

               if (var1 == fp.NELSON_CUM) {
                  this.m.func_187227_b(aA, true);
               }

               if (var2 == fp.NELSON_CUM && var1 != fp.NELSON_CUM) {
                  this.m.func_187227_b(aA, false);
               }

               super.b(var1);
            }
         }
      }
   }

   void q_clash465() {
      EntityPlayer var1 = this.field_70170_p.func_152378_a(this.getInteractionPlayerUUID());
      if (var1 != null) {
         this.setYawRotation(var1.field_70177_z);
         this.field_70145_X = true;
         this.func_189654_d(true);
         var1.func_189654_d(true);
         var1.field_70145_X = true;
         var1.func_70634_a(var1.field_70165_t, var1.field_70163_u, var1.field_70161_v - 1.0);
      }
   }

   void m_clash466() {
      EntityPlayer var1 = this.field_70170_p.func_152378_a(this.getInteractionPlayerUUID());
      if (var1 != null) {
         this.setYawRotation(var1.field_70177_z + 180.0F);
         this.field_70145_X = true;
         this.func_189654_d(true);
         var1.func_189654_d(true);
         var1.field_70145_X = true;
         var1.func_70634_a(var1.field_70165_t, var1.field_70163_u - 0.5, var1.field_70161_v - 0.6F);
         var1.field_70125_A = 70.0F;
         var1.field_70127_C = 70.0F;
      }
   }

   @Override
   public boolean l_clash467() {
      return this.getOwnerUUID() == null;
   }

   @Override
   public void b_clash468(EntityPlayer var1) {
      if (var1.getPersistentID().equals(this.getOwnerUUID())) {
         ResetGirlPacket.Handler.a_clash10(this);
         this.setAnchored(false);
         this.b(fp.NULL);
         this.setOwnerUUID(null);
      }
   }

   @Override
   protected fp getCumAction(fp var1) {
      switch (var1) {
         case NELSON_FAST:
         case NELSON_SLOW:
            return fp.NELSON_CUM;
         case NELSON_INTRO:
         case PAIZURI_IDLE:
         case BREEDING_SLOW_0:
         default:
            return null;
         case PAIZURI_SLOW:
         case PAIZURI_FAST:
         case PAIZURI_FAST_CONTINUES:
            return fp.PAIZURI_CUM;
         case BREEDING_SLOW_2:
         case BREEDING_FAST_2:
            return fp.BREEDING_CUM_2;
         case BREEDING_1:
            return fp.BREEDING_CUM_1;
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      if (this.field_70170_p instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == fp.NULL && this.getCurrentAction().autoBlink) {
               this.a("animation.goblin.blink", true, var1);
            } else {
               this.a("animation.goblin.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != fp.NULL) {
               this.a("animation.goblin.null", true, var1);
            } else if (this.ak) {
               this.a("animation.goblin.sit", true, var1);
            } else {
               if (this.E.getCurrentAnimation() != null && this.E.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.aC = !this.aC;
               }

               if (!this.af) {
                  this.a("animation.goblin.fly" + (this.aC ? "2" : ""), true, var1);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.E.setAnimationSpeed(1.2F);
                     this.a("animation.goblin.running", true, var1);
                  } else if (this.ao.y >= -0.1F) {
                     this.E.setAnimationSpeed(2.0);
                     this.a("animation.goblin.walk", true, var1);
                  } else {
                     this.E.setAnimationSpeed(1.5);
                     this.a("animation.goblin.backwards_walk", true, var1);
                  }
               } else {
                  this.a("animation.goblin.idle", true, var1);
               }
            }
            break;
         case "action":
            Minecraft var4 = Minecraft.func_71410_x();
            String var5 = var4.field_71439_g.getPersistentID().equals(this.getOwnerUUID()) && var4.field_71474_y.field_74320_O == 0 ? "1" : "3";
            switch (this.getCurrentAction()) {
               case NELSON_CUM:
                  this.a("animation.goblin.nelson_cum", true, var1);
                  break;
               case NELSON_FAST:
                  this.a("animation.goblin.nelson_fast" + (this.aF ? "c" : "s"), true, var1);
                  break;
               case NELSON_INTRO:
                  this.a("animation.goblin.nelson_intro", true, var1);
                  break;
               case NELSON_SLOW:
                  this.a("animation.goblin.nelson_slow" + (this.ay ? "" : "2"), true, var1);
                  break;
               case PAIZURI_IDLE:
                  this.a("animation.goblin.paizuri_idle", true, var1);
                  break;
               case PAIZURI_SLOW:
                  this.a("animation.goblin.paizuri_slow" + this.aD, true, var1);
                  break;
               case BREEDING_SLOW_0:
                  this.a("animation.goblin.breeding_slow_1" + (this.aB ? "l" : "r"), true, var1);
                  break;
               case BREEDING_SLOW_2:
                  this.a("animation.goblin.breeding_slow_3", true, var1);
                  break;
               case PAIZURI_FAST:
                  this.a("animation.goblin.paizuri_fast", true, var1);
                  break;
               case PAIZURI_FAST_CONTINUES:
                  this.a("animation.goblin.paizuri_fast_countinues", true, var1);
                  break;
               case BREEDING_1:
                  this.a("animation.goblin.breeding_2", true, var1);
                  break;
               case BREEDING_FAST_2:
                  this.a("animation.goblin.breeding_fast_3", true, var1);
                  break;
               case SHOULDER_IDLE:
                  this.a("animation.goblin.shoulder_idle", true, var1);
                  break;
               case PICK_UP:
                  this.a(String.format("animation.goblin.pick_up_%sperson", var5), true, var1);
                  break;
               case START_THROWING:
                  this.a(String.format("animation.goblin.throw_%sperson", var5), true, var1);
                  break;
               case THROWN:
                  this.a("animation.goblin.thrown", true, var1);
                  break;
               case NULL:
                  this.a("animation.goblin.null", true, var1);
                  break;
               case STAND_UP:
                  this.a("animation.goblin.stand_up", false, var1);
                  break;
               case STRIP:
                  this.a("animation.goblin.strip", false, var1);
                  break;
               case ATTACK:
                  this.a("animation.goblin.attack" + this.S, false, var1);
                  break;
               case BOW:
                  this.a("animation.goblin.bowcharge", false, var1);
                  break;
               case SIT:
                  this.a("animation.goblin.sit", true, var1);
                  break;
               case BREEDING_INTRO_0:
                  this.a("animation.goblin.breeding_intro_1", true, var1);
                  break;
               case BREEDING_INTRO_1:
                  this.a("animation.goblin.breeding_intro_2", true, var1);
                  break;
               case BREEDING_INTRO_2:
                  this.a("animation.goblin.breeding_intro_3", true, var1);
                  break;
               case BREEDING_FAST_0:
                  this.a("animation.goblin.breeding_fast_1" + (this.aH ? "c" : "s"), true, var1);
                  break;
               case BREEDING_CUM_0:
                  this.a("animation.goblin.breeding_cum_1", true, var1);
                  break;
               case BREEDING_CUM_1:
                  this.a("animation.goblin.breeding_cum_2", true, var1);
                  break;
               case BREEDING_CUM_2:
                  this.a("animation.goblin.breeding_cum_3", true, var1);
                  break;
               case PAIZURI_START:
                  this.a("animation.goblin.paizuri_start", true, var1);
                  break;
               case PAIZURI_CUM:
                  this.a("animation.goblin.paizuri_cum", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData var1) {
      if (this.C == null) {
         this.p_clash506();
      }

      AnimationController.ISoundListener var2 = var1x -> {
         switch (var1x.sound) {
            case "attackDone":
               if (++this.S == 3) {
                  this.S = 0;
               }
               break;
            case "catchEh":
               this.sendChatMessage("ehh..");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchAkward":
               this.sendChatMessage("awkward..");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchWell":
               this.sendChatMessage("well...");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchRather":
               this.sendChatMessage("would you rather have this stupid... thing?");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchMe":
               this.sendChatMessage("...or use me?~");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchDone":
               if ("bj".equals(this.m.func_187225_a(h))) {
                  this.b(fp.CATCH_BJ);
               }
               break;
            case "catchBjDone":
               this.b(fp.CATCH_BJ_IDLE);
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var6 = Minecraft.func_71410_x().field_71439_g;
                  a(var6, this, new String[]{"use her", "take ur stuff back"}, null, false);
               }
               break;
            case "paizuriChoice":
               this.sendChatMessage("good choice!~");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizuriBoth":
               this.sendChatMessage("...for both of us!");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizruiUse":
               this.sendChatMessage("now use me like a fuck toy!~");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizuriSwitch":
               if (!this.func_70681_au().nextBoolean()) {
                  this.aD = "".equals(this.aD) ? "2" : "";
               }
               break;
            case "touch":
               this.a(SoundHandler.MISC_TOUCH, 3.0F);
               break;
            case "pound":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "paizuri_startDone":
               this.b(fp.PAIZURI_IDLE);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "paizuriFastDone":
               this.b(fp.PAIZURI_SLOW);
               break;
            case "paizuriFastReady":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.b(fp.PAIZURI_FAST_CONTINUES);
               }
               break;
            case "paizuriFastContinuesReady":
            case "neslon_fastBackSwitch":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.N();
               }
               break;
            case "smallPound":
               this.a(SoundHandler.MISC_POUNDING, 0.25F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "paizruiCam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var4 = Minecraft.func_71410_x().field_71439_g;
                  var4.field_70125_A = 70.0F;
                  var4.field_70127_C = 70.0F;
               }
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "cumSound":
               this.a(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "jumpCam":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft var9 = Minecraft.func_71410_x();
                  var9.field_71439_g.field_70177_z = this.getYawRotation() + 170.0F;
                  var9.field_71439_g.field_70125_A = -20.0F;
                  var9.field_71439_g.field_70759_as = var9.field_71439_g.field_70177_z;
                  var9.field_71474_y.field_74320_O = 2;
               }
               break;
            case "breedingHmm":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft var8 = Minecraft.func_71410_x();
                  var8.field_71439_g.field_70177_z = this.getYawRotation() + 180.0F;
                  var8.field_71439_g.field_70125_A = -15.0F;
                  var8.field_71439_g.field_70759_as = var8.field_71439_g.field_70177_z;
                  var8.field_71474_y.field_74320_O = 0;
               }

               this.sendChatMessage("hmm...");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingFound":
               this.sendChatMessage("guess we found a worthy breeding partner!");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingEnough":
               this.sendChatMessage("Eh.. go pin him down, before he runs off!");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingCam2":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft var7 = Minecraft.func_71410_x();
                  var7.field_71474_y.field_74320_O = 2;
                  var7.field_71439_g.field_70177_z = this.getYawRotation() - 120.0F;
                  var7.field_71439_g.field_70125_A = -30.0F;
               }
            case "breedingIntroDone":
               this.b(fp.BREEDING_SLOW_0);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "breeding_slow1Done":
               if (this.func_70681_au().nextBoolean()) {
                  this.aB = !this.aB;
               }

               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.b(fp.BREEDING_FAST_0);
                  this.aH = false;
               }
               break;
            case "breeding_fast1Done":
               this.b(fp.BREEDING_SLOW_0);
               if (this.isControlledByLocalPlayer()) {
                  this.aH = false;
               }
               break;
            case "breeding_fast1Ready":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.aH = true;
                  this.N();
                  this.C.tickOffset = 0.0;
               }
               break;
            case "cum":
               this.a(SoundHandler.MISC_SMALLINSERTS, 2.0F);
               break;
            case "breeding_intro_3Done":
               this.b(fp.BREEDING_SLOW_2);
               break;
            case "breeding_3_wiggle":
               if (this.func_70681_au().nextBoolean()) {
                  this.C.tickOffset = 0.0;
               }
               break;
            case "breeding_fast_3Done":
               if (this.isControlledByLocalPlayer() && !d3.d) {
                  this.b(fp.BREEDING_SLOW_2);
               }
               break;
            case "breeding_intro_2Done":
               this.b(fp.BREEDING_1);
               break;
            case "breeding_cumCam":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft var5 = Minecraft.func_71410_x();
                  var5.field_71474_y.field_74320_O = 0;
                  var5.field_71439_g.field_70177_z = this.getYawRotation() + 180.0F;
                  var5.field_71439_g.field_70125_A = -15.0F;
                  var5.field_71439_g.field_70759_as = var5.field_71439_g.field_70177_z;
                  var5.field_71474_y.field_74320_O = 0;
               }
               break;
            case "neslon_introDone":
               this.b(fp.NELSON_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "nelson_slowDone":
               if (this.func_70681_au().nextBoolean()) {
                  this.ay = !this.ay;
               }
               break;
            case "neslon_fastSwitch":
               if (!this.isControlledByLocalPlayer()) {
                  this.aF = true;
                  return;
               }

               if (d3.d) {
                  this.aF = true;
               }
               break;
            case "nelsonFastDone":
               this.aF = false;
               if (this.isControlledByLocalPlayer()) {
                  this.b(fp.NELSON_SLOW);
               }
               break;
            case "paizuriCumDone":
            case "nelson_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.r_clash533();
                  this.b(fp.NULL);
               }
         }
      };
      this.C.registerSoundListener(var2);
      this.E.transitionLengthTicks = 2.0;
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.E);
      var1.addAnimationController(this.s);
   }


   public static class a {
      HashSet<EntityPlayer> a = new HashSet<>();

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void a(RenderHandEvent var1) {
         AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.g(Minecraft.func_71410_x().field_71439_g);
         if (var2 != null) {
            if (var2 instanceof IGoblin) {
               if (((IGoblin)var2).getOwnerUUID() != null) {
                  var1.setCanceled(true);
               }
            }
         }
      }

      @SubscribeEvent
      public void a(PlayerTickEvent var1) {
         EntityPlayer var2 = var1.player;
         if (var2 != null) {
            this.a_clash13(var2);
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void a(RenderTickEvent var1) {
         if (var1.phase != Phase.END) {
            EntityPlayerSP var2 = Minecraft.func_71410_x().field_71439_g;
            if (var2 != null) {
               this.a_clash13(var2);
            }
         }
      }

      void a_clash13(EntityPlayer var1) {
         AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.g(var1);
         if (var2 instanceof GoblinPlayerEntity) {
            fp var3 = var2.getCurrentAction();
            if (var3 != fp.THROWN) {
               if (var3 != fp.START_THROWING || ((IGoblin)var2).a_clash58() <= 15) {
                  UUID var4 = ((GoblinPlayerEntity)var2).getOwnerUUID();
                  if (var4 != null) {
                     EntityPlayer var5 = var1.field_70170_p.func_152378_a(var4);
                     if (var5 != null) {
                        var1.field_70145_X = true;
                        var1.func_189654_d(true);
                        var2.field_70145_X = true;
                        var2.func_189654_d(true);
                        var1.func_70107_b(var5.field_70165_t, var5.field_70163_u + 2.0, var5.field_70161_v);
                        var1.field_70142_S = var5.field_70142_S;
                        var1.field_70137_T = var5.field_70137_T + 2.0;
                        var1.field_70136_U = var5.field_70136_U;
                     }
                  }
               }
            }
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void a(RenderWorldLastEvent var1) {
         Minecraft var2 = Minecraft.func_71410_x();
         RenderManager var3 = var2.func_175598_ae();
         EntityPlayerSP var4 = var2.field_71439_g;
         if (var2.field_71439_g != null) {
            Vec3d var5 = var4.func_174791_d();

            for (EntityPlayer var7 : this.a) {
               Vec3d var8 = var7.func_174791_d();
               Vec3d var9 = var8.func_178788_d(var5);
               var3.func_188391_a(var7, var9.field_72450_a, var9.field_72448_b, var9.field_72449_c, 69.0F, var1.getPartialTicks(), true);
            }

            GlStateManager.func_179145_e();
            GlStateManager.func_179126_j();
            GlStateManager.func_179141_d();
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void b(RenderTickEvent var1) {
         if (var1.phase == Phase.START) {
            this.b_clash15();
         } else {
            this.a_clash14();
         }
      }

      @SideOnly(Side.CLIENT)
      void a_clash14() {
         for (EntityPlayer var2 : this.a) {
            var2.field_70128_L = true;
         }
      }

      @SideOnly(Side.CLIENT)
      void b_clash15() {
         this.a.clear();
         Minecraft var1 = Minecraft.func_71410_x();
         EntityPlayerSP var2 = var1.field_71439_g;
         if (var1.field_71441_e != null) {
            for (EntityPlayer var4 : var1.field_71441_e.field_73010_i) {
               if (var4 != var2) {
                  AbstractPlayerGirlEntity var5 = AbstractPlayerGirlEntity.g(var4);
                  if (var5 instanceof GoblinPlayerEntity) {
                     GoblinPlayerEntity var6 = (GoblinPlayerEntity)var5;
                     if (var6.getOwnerUUID() != null) {
                        fp var7 = var6.getCurrentAction();
                        if (var7 == fp.THROWN || var7 == fp.START_THROWING) {
                           return;
                        }

                        this.a.add(var4);
                        var4.field_70128_L = false;
                     }
                  }
               }
            }
         }
      }

      @SubscribeEvent
      public void a(EntityInteract var1) {
         EntityPlayer var2 = var1.getEntityPlayer();
         if (var2.func_70093_af()) {
            if (var1.getTarget() instanceof EntityPlayer) {
               AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1.getTarget().getPersistentID());
               if (var3 instanceof GoblinPlayerEntity) {
                  AbstractPlayerGirlEntity var4 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var2.getPersistentID());
                  if (var4 == null) {
                     ((GoblinPlayerEntity)var3).c_clash455(var1.getEntityPlayer());
                  }
               }
            }
         }
      }

   }
}
