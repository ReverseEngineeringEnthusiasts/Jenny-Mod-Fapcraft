package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.api.ao;
import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.ManglelieNpcModel;
import com.trolmastercard.sexmod.client.renderer.ManglelieRenderer;
import com.trolmastercard.sexmod.entity.ai.AvoidPlayerGoal;
import com.trolmastercard.sexmod.util.BeeWorldData;
import com.trolmastercard.sexmod.util.GirlSavedData;
import com.trolmastercard.sexmod.util.GirlWorldData;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.ad;
import com.trolmastercard.sexmod.util.af;
import com.trolmastercard.sexmod.util.ah;
import com.trolmastercard.sexmod.util.ak;
import com.trolmastercard.sexmod.util.am;
import com.trolmastercard.sexmod.util.an;
import com.trolmastercard.sexmod.util.cj;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.ep;
import com.trolmastercard.sexmod.util.gc;







import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.ProjectileImpactEvent.Arrow;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class ManglelieEntity extends BaseGirlEntity {
   public static final String ac = "sexmod:mommy";
   public static final float am = 60.0F;
   public static final float ag = 4.0F;
   public static final float P = 3.5F;
   public static final float ah = 28.0F;
   public static final float ae = 15.0F;
   public static final float K = 15.0F;
   public static final float L = 0.65F;
   public static final float ao = 3.65F;
   public static final float O = 6.0F;
   public static final float ak = 80.0F;
   public static final float X = 700.0F;
   public static final DataParameter<String> ad = EntityDataManager.func_187226_a(ManglelieEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(111);
   public static final DataParameter<Boolean> ap = EntityDataManager.func_187226_a(ManglelieEntity.class, DataSerializers.field_187198_h)
      .func_187156_b()
      .func_187161_a(112);
   public static final DataParameter<Integer> ab = EntityDataManager.func_187226_a(ManglelieEntity.class, DataSerializers.field_187192_b)
      .func_187156_b()
      .func_187161_a(113);
   public static final DataParameter<String> al = EntityDataManager.func_187226_a(ManglelieEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(114);
   public static final DataParameter<Boolean> ar = EntityDataManager.func_187226_a(ManglelieEntity.class, DataSerializers.field_187198_h)
      .func_187156_b()
      .func_187161_a(115);
   private UUID Q = null;
   public boolean aj = true;
   public Vec3d R = Vec3d.field_186680_a;
   public float V = 0.0F;
   boolean aq = true;
   boolean S = false;
   boolean U = false;
   public float af = 0.0F;
   public float W = 0.0F;
   public float T = 0.0F;
   public float ai = 0.0F;
   boolean aa = false;
   boolean Z = false;
   boolean N = false;
   boolean Y = false;
   boolean M = false;
   public int an = 2;

   public ManglelieEntity(World var1) {
      super(var1);
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      this.m.func_187214_a(ad, "");
      this.m.func_187214_a(ap, false);
      this.m.func_187214_a(ab, -1);
      this.m.func_187214_a(al, "");
      this.m.func_187214_a(ar, false);
   }

   @Override
   public String getDisplayNameText() {
      return "Manglelie";
   }

   @Override
   protected void func_184651_r() {
      super.func_184651_r();
      this.field_70714_bg.func_75776_a(1, new AvoidPlayerGoal(this, 20.0F, 1.0, 1.2));
   }

   @Override
   public float i_clash226() {
      return 0.0F;
   }

   public void c_clash410(boolean var1) {
      this.m.func_187227_b(ap, var1);
   }

   public boolean r_clash411() {
      return (Boolean)this.m.func_187225_a(ap);
   }

   @Nullable
   public UUID v_clash412() {
      String var1 = (String)this.m.func_187225_a(ad);
      if ("".equals(var1)) {
         return null;
      }

      try {
         return UUID.fromString(var1);
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   @Override
   public boolean t_clash283() {
      return !this.r_clash411();
   }

   @Nullable
   public GalathEntity a_clash413(boolean var1) {
      UUID var2 = this.v_clash412();
      if (var2 == null) {
         return null;
      }

      BaseGirlEntity var3 = var1 ? BaseGirlEntity.getServerGirlEntity(var2) : BaseGirlEntity.getClientGirlEntity(var2);
      return !(var3 instanceof GalathEntity) ? null : (GalathEntity)var3;
   }

   public void a_clash414(UUID var1) {
      if (var1 == null) {
         this.m.func_187227_b(ad, "");
      } else {
         this.m.func_187227_b(ad, var1.toString());
      }
   }

   @Override
   public Float getYawRotation() {
      float var1 = super.getYawRotation();
      if (ManglelieNpcModel.c_clash313(this)) {
         var1 += 180.0F;
      }

      return var1;
   }

   public void q_clash416() {
      this.S = true;
   }

   @Override
   public void func_70619_bc() {
      if (this.aa) {
         this.field_70170_p.func_72900_e(this);
      } else {
         this.f_clash440();
         this.w_clash430();
         super.func_70619_bc();
         this.j_clash429();
         this.c_clash433();
         this.d_clash426();
         this.i_clash428();
         this.n_clash427();
         this.u_clash422();
         this.h_clash421();
         this.a_clash418();
         this.t_clash417();
      }
   }

   void t_clash417() {
      if (this.v_clash412() != null) {
         this.aq = false;
      }

      if (!this.aq) {
         if (this.a_clash413(true) == null) {
            System.out.println("removed non-wild mang for lack of mommy");
            this.field_70170_p.func_72900_e(this);
         }
      }
   }

   void a_clash418() {
      GalathEntity var1 = this.a_clash413(true);
      if (var1 != null) {
         if (var1.aF() != null) {
            if (!this.getGirlId().equals(var1.aF())) {
               System.out.println("removed non-wild mang cuz her mommy disowned her and got another mang");
               this.field_70170_p.func_72900_e(this);
            }
         }
      }
   }

   public static GalathEntity a_clash419(BaseGirlEntity var0, boolean var1) {
      return !(var0 instanceof ManglelieEntity) ? null : ((ManglelieEntity)var0).a_clash413(var1);
   }

   public long e_clash420() {
      String var1 = (String)this.m.func_187225_a(al);
      if ("".equals(var1)) {
         return -1L;
      }

      try {
         return Long.parseLong(var1);
      } catch (Exception var2) {
         return -1L;
      }
   }

   public void a(long var1) {
      this.m.func_187227_b(al, Long.toString(var1));
      this.U = false;
   }

   void h_clash421() {
      long var1 = this.e_clash420();
      if (var1 != -1L) {
         long var3 = this.field_70170_p.func_82737_E();
         if (!((float)var3 < 28.0F + (float)var1)) {
            if (!this.U) {
               Entity var5 = this.b_clash424();
               if (var5 != null) {
                  GalathEntity var6 = this.a_clash413(true);
                  if (var6 != null) {
                     EntityTippedArrow var7 = new EntityTippedArrow(this.field_70170_p, this);
                     Vec3d var8 = var6.func_174791_d().func_72441_c(0.0, 3.5, 0.0);
                     var7.func_70634_a(var8.field_72450_a, var8.field_72448_b, var8.field_72449_c);
                     Vec3d var9 = var5.func_174791_d();
                     Vec3d var10 = var9.func_178788_d(var8).func_72432_b();
                     var7.field_70159_w = var10.field_72450_a * 4.0;
                     var7.field_70181_x = var10.field_72448_b * 4.0;
                     var7.field_70179_y = var10.field_72449_c * 4.0;
                     BaseGirlEntity.a(var6, SoundEvents.field_187737_v, true);
                     this.field_70170_p.func_72838_d(var7);
                     this.U = true;
                  }
               }
            }
         }
      }
   }

   public void func_70690_d(PotionEffect var1) {
   }

   void u_clash422() {
      boolean var1 = this.v_clash412() != null;
      this.func_189654_d(var1);
      this.field_70145_X = var1;
   }

   public boolean func_70067_L() {
      return this.v_clash412() == null;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public Vec3d a(Minecraft var1, SexSceneEntity var2, EntityLivingBase var3, float var4) {
      if (this.isLocallyRegistered()) {
         return super.a(var1, var2, var3, var4);
      }

      if (!this.r_clash411()) {
         return super.a(var1, var2, var3, var4);
      }

      GalathEntity var5 = this.a_clash413(false);
      if (var5 == null) {
         return super.a(var1, var2, var3, var4);
      }

      ManglelieRenderer.a(var5, var4, var2);
      return ManglelieRenderer.b(var5, var4);
   }

   public float b_clash423(float var1) {
      long var2 = this.e_clash420();
      if (var2 == -1L) {
         return 0.0F;
      }

      long var4 = this.field_70170_p.func_82737_E();
      float var6 = (float)(var4 - var2);
      return (var6 + var1) / 28.0F;
   }

   @Nullable
   public Entity b_clash424() {
      int var1 = (Integer)this.m.func_187225_a(ab);
      return var1 == -1 ? null : this.field_70170_p.func_73045_a(var1);
   }

   void a_clash425(int var1) {
      this.m.func_187227_b(ab, var1);
      this.a(var1 == -1 ? -1L : this.field_70170_p.func_82737_E());
   }

   void d_clash426() {
      Entity var1 = this.b_clash424();
      if (var1 != null) {
         GalathEntity var2 = this.a_clash413(true);
         if (var2 == null) {
            this.a_clash425(-1);
         } else if (!this.r_clash411()) {
            this.a_clash425(-1);
         } else {
            if (a(var1, var2)) {
               this.a_clash425(-1);
            }
         }
      }
   }

   public static boolean a(Entity var0, GalathEntity var1) {
      if (var0.field_70128_L) {
         return true;
      }

      if (var0.field_71093_bK != var1.field_71093_bK) {
         return true;
      }

      if (!com.trolmastercard.sexmod.MobPredicates.a_clash801(var0)) {
         return true;
      }

      if (!com.trolmastercard.sexmod.MobPredicates.a(var1.field_70170_p, var1.getTargetPosition().func_72441_c(0.0, var1.func_70047_e(), 0.0), var0)) {
         return true;
      }

      Vec3d var2 = var0.func_174791_d().func_178788_d(var1.func_174791_d());
      if (var2.field_72450_a * var2.field_72450_a + var2.field_72449_c * var2.field_72449_c > 225.0) {
         return true;
      }

      Float var3 = GalathEntity.a_clash692(var1, 0.0F);
      float var4 = var3 == null ? var1.field_70759_as : var3;
      Vec3d var5 = ck.rotateByYaw(var2, var4);
      return var5.field_72449_c < 0.0;
   }

   void n_clash427() {
      if (this.b_clash424() == null) {
         if (this.r_clash411()) {
            GalathEntity var1 = this.a_clash413(true);
            if (var1 != null) {
               if (var1.getInteractionPlayerUUID() == null) {
                  if (var1.getCurrentAction() != fp.MASTERBATE) {
                     BlockPos var2 = var1.func_180425_c();
                     BlockPos var3 = new BlockPos(15.0, 15.0, 15.0);

                     for (EntityMob var6 : this.field_70170_p
                        .func_72872_a(EntityMob.class, new AxisAlignedBB(var2.func_177971_a(var3), var2.func_177973_b(var3)))) {
                        if (!a(var6, var1)) {
                           this.a_clash425(var6.func_145782_y());
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   void i_clash428() {
      Entity var1 = this.b_clash424();
      if (var1 != null) {
         GalathEntity var2 = this.a_clash413(true);
         if (var2 != null) {
            long var3 = this.e_clash420();
            if (var3 != -1L) {
               long var5 = this.field_70170_p.func_82737_E();
               long var7 = var5 - this.e_clash420();
               if (!((float)var7 < 60.0F)) {
                  this.U = false;
                  this.a_clash425(-1);
               }
            }
         }
      }
   }

   void j_clash429() {
      if (this.Q != null) {
         BaseGirlEntity var1 = BaseGirlEntity.getServerGirlEntity(this.Q);
         if (var1 instanceof GalathEntity) {
            GalathEntity var2 = (GalathEntity)var1;
            this.a_clash414(this.Q);
            var2.a_clash640(this.getGirlId());
            this.c_clash410(true);
            this.b(fp.RIDE_MOMMY_HEAD);
            this.Q = null;
            if (var2.getCurrentAction() == fp.HUG_MANG) {
               var2.setAnchored(false);
               var2.b((fp)null);
            }
         }
      }
   }

   @Override
   public void b(fp var1) {
      if (this.getCurrentAction() != fp.THREESOME_CUM || !fp.a(var1, fp.THREESOME_FAST, fp.THREESOME_SLOW)) {
         if (!this.field_70170_p.field_72995_K && var1 == fp.THREESOME_CUM) {
            GirlSavedData.a(this.getInteractionPlayerUUID(), this.field_70170_p.func_82737_E());
         }

         super.b(var1);
      }
   }

   void w_clash430() {
      if (this.r_clash411() && !fp.a(this, fp.THREESOME_SLOW, fp.THREESOME_CUM, fp.THREESOME_FAST)) {
         GalathEntity var1 = this.a_clash413(true);
         if (var1 != null) {
            if (!var1.field_70128_L && this.getGirlId().equals(var1.aF())) {
               this.setYawRotation(0.0F);
               this.setTargetPosition(var1.func_174791_d());
               this.setAnchored(true);
            } else {
               Main.LOGGER.warn("A dead mommy has been saved onto a mang. Deleting her and creating a new one");
               this.field_70170_p.func_72900_e(this);
            }
         }
      }
   }

   @Override
   public void setYawRotation(float var1) {
      super.setYawRotation(var1);
   }

   @Override
   public Vec3d a_clash432(Vec3d var1, float var2) {
      if (!this.r_clash411()) {
         return var1;
      }

      if (ManglelieNpcModel.c_clash313(this)) {
         return var1;
      }

      GalathEntity var3 = this.a_clash413(false);
      return var3 == null ? var1 : ManglelieRenderer.b(var3, var2);
   }

   void c_clash433() {
      if (!this.r_clash411()) {
         if (this.v_clash412() == null) {
            BlockPos var1 = this.func_180425_c();
            BlockPos var2 = var1.func_177963_a(-15.0, -15.0, -15.0);
            BlockPos var3 = var1.func_177963_a(15.0, 15.0, 15.0);
            AxisAlignedBB var4 = new AxisAlignedBB(var2, var3);
            List var5 = this.field_70170_p.func_72872_a(GalathEntity.class, var4);
            GalathEntity var6 = null;

            for (GalathEntity var8 : (java.util.Collection<GalathEntity>) (var5) ) {
               if (!var8.field_70128_L && var8.a_clash638(true) == null && var8.field_70122_E) {
                  var6 = var8;
                  break;
               }
            }

            if (var6 == null) {
               if (this.getCurrentAction() == fp.RUN) {
                  this.b((fp) null);
                  this.func_70661_as().func_75499_g();
               }
            } else if (this.getCurrentAction() != fp.RIDE_MOMMY_HEAD) {
               this.b(fp.RUN);
               Vec3d var11 = this.func_174791_d();
               Vec3d var12 = var6.func_174791_d();
               Vec3d var9 = var12.func_178788_d(var11);
               float var10 = (float)gc.b(Math.atan2(var9.field_72449_c, var9.field_72450_a)) - 90.0F;
               this.setYawRotation(var10);
               this.f = this.func_70661_as();
               this.f.func_75499_g();
               this.f.func_75497_a(var6, 0.65F);
            }
         }
      }
   }

   public boolean a_clash434(Entity var1, float var2) {
      GalathEntity var3 = this.a_clash413(var2 == 1.0F);
      if (var3 == null) {
         return false;
      }

      Vec3d var4 = com.trolmastercard.sexmod.util.ak.a_clash52(this, var2);
      return this.a(com.trolmastercard.sexmod.util.ak.a_clash52(var1, var2).func_178788_d(var4), var3, var2);
   }

   public boolean a_clash435(Vec3d var1, float var2) {
      GalathEntity var3 = this.a_clash413(var2 == 1.0F);
      if (var3 == null) {
         return false;
      }

      Vec3d var4 = com.trolmastercard.sexmod.util.ak.a_clash52(this, var2);
      return this.a(var1.func_178788_d(var4), var3, var2);
   }

   boolean a(Vec3d var1, GalathEntity var2, float var3) {
      Vec3d var4 = ck.rotateByYaw(var1, RotationHelper.b(var2.field_70758_at, var2.field_70759_as, var3));
      return var4.field_72450_a > 0.35;
   }

   @Override
   public void func_70071_h_() {
      super.func_70071_h_();
      if (this.field_70170_p.field_72995_K) {
         this.m_clash436();
      }
   }

   @SideOnly(Side.CLIENT)
   void m_clash436() {
      if (Minecraft.func_71410_x().field_71439_g.field_70173_aa % 7.0F == 0.0F) {
         if (ManglelieRenderer.b(this)) {
            GalathEntity var1 = this.a_clash413(false);
            if (var1 != null) {
               Entity var2 = this.o_clash437();
               if (var2 == null) {
                  this.af = 0.0F;
                  this.W = 0.0F;
               } else {
                  Vec3d var3 = var2.func_174791_d().func_72441_c(0.0, var2.func_70047_e(), 0.0);
                  Vec3d var4 = var1.func_174791_d().func_178787_e(var1.getCachedBoneOffset("mangPos")).func_178787_e(this.getCachedBoneOffset("head"));
                  Vec3d var5 = var4.func_178788_d(var3);
                  float var6 = (float)(gc.b(Math.atan2(var5.field_72449_c, var5.field_72450_a)) + 90.0);
                  Float var7 = GalathEntity.a_clash692(var1, 0.0F);
                  var6 -= var1.field_70759_as;
                  if (var7 != null) {
                     var6 -= var7;
                  }

                  this.af = Math.abs(cj.a_clash300(0.0F, var6)) < 80.0F ? -gc.wrapDegrees(var6) : 0.0F;
                  this.W = this.af == 0.0F ? 0.0F : (float)ThreadNames.b(-var5.field_72448_b / 2.0, -0.75, 0.75);
               }
            }
         }
      }
   }

   public boolean func_70097_a(DamageSource var1, float var2) {
      if (var1 == DamageSource.field_76380_i) {
         return super.func_70097_a(var1, var2);
      }

      GalathEntity var3 = this.a_clash413(true);
      if (var3 == null) {
         return super.func_70097_a(var1, var2);
      }

      var3.func_70097_a(var1, var2);
      return false;
   }

   @Nullable
   Entity o_clash437() {
      Object var1 = this.b_clash424();
      if (var1 != null) {
         return (Entity)var1;
      }

      for (EntityPlayer var3 : this.field_70170_p.field_73010_i) {
         float var4 = var3.func_70032_d(this);
         if (!(var4 > 6.0F) && (var1 == null || ((Entity)var1).func_70032_d(this) > var4)) {
            var1 = var3;
         }
      }

      return (Entity)var1;
   }

   @Override
   public void func_70014_b(NBTTagCompound var1) {
      super.func_70014_b(var1);
      UUID var2 = this.v_clash412();
      var1.func_74778_a("sexmod:mommy", var2 == null ? "" : var2.toString());
      var1.func_74757_a("sexmod:iswild", this.aq);
      if (this.S) {
         var1.func_74757_a("sexmod:despawned", true);
      }
   }

   public void func_70020_e(NBTTagCompound var1) {
      super.func_70020_e(var1);
      String var2 = var1.func_74779_i("sexmod:mommy");
      if (!"".equals(var2)) {
         this.Q = UUID.fromString(var2);
      }

      if (var1.func_74767_n("sexmod:despawned")) {
         this.aa = true;
      }

      this.aq = var1.func_74767_n("sexmod:iswild");
   }

   @Override
   protected boolean X_clash438() {
      return false;
   }

   @Override
   public void setCustomModelCode(String var1) {
      super.setCustomModelCode(var1);
      GirlWorldData.a_clash153(this);
   }

   void f_clash440() {
      if (!this.Z) {
         this.setCustomModelCode(GirlWorldData.c_clash151(this));
         this.Z = true;
      }
   }

   @Nullable
   @Override
   protected fp getNextAction(fp var1) {
      return null;
   }

   @Override
   protected fp getCumAction(fp var1) {
      if (fp.a(var1, fp.THREESOME_FAST, fp.THREESOME_SLOW)) {
         this.N = true;
      }

      return null;
   }

   @Override
   public void reinitTasks() {
      if (this.r_clash411()) {
         this.b(fp.RIDE_MOMMY_HEAD);
         this.setYawRotation(0.0F);
         this.m.func_187217_b(w);
      }
   }

   public boolean func_70601_bi() {
      if (!super.func_70601_bi()) {
         return false;
      }

      BlockPos var1 = this.func_180425_c();
      ArrayList var2 = new ArrayList();
      var2.addAll(BeeWorldData.c);
      var2.addAll(BeeWorldData.b);

      for (BlockPos var4 : (java.util.Collection<BlockPos>) (var2) ) {
         if (Math.sqrt(var1.func_177951_i(var4)) < 700.0) {
            return false;
         }
      }

      BeeWorldData.a(var1, BeeWorldData.b);
      return true;
   }

   @Override
   protected boolean a(fp var1, String var2, boolean var3, AnimationEvent var4) {
      if (var1 == fp.THREESOME_CUM) {
         this.N = false;
         this.Y = false;
         this.M = false;
         this.an = 2;
         this.r_clash533();
         GalathEntity var8 = this.a_clash413(false);
         if (var8 != null) {
            var8.r_clash533();
            CummyEntity.a_clash747(var8);
         }

         CummyEntity.a_clash747(this);
         return true;
      } else if (this.N && var1 == fp.THREESOME_FAST) {
         this.b(fp.THREESOME_CUM);
         this.a("animation.shared.double_holding_cum", true, var4, true);
         GalathEntity var7 = this.a_clash413(false);
         if (var7 != null) {
            var7.b(fp.MASTERBATE_SITTING_CUM);
         }

         return true;
      } else if ((this.N || var3) && var1 == fp.THREESOME_SLOW) {
         this.Y = false;
         this.b(fp.THREESOME_FAST);
         this.a("animation.shared.double_holding_soft", true, var4, true);
         GalathEntity var6 = this.a_clash413(false);
         if (var6 != null) {
            var6.ak();
         }

         return true;
      } else {
         if (this.N) {
            return false;
         }

         if (var3 && !this.Y && var1 == fp.THREESOME_FAST) {
            this.Y = true;
            this.a("animation.shared.double_holding_hard", true, var4, true);
            return true;
         }

         if (!var3 && var1 == fp.THREESOME_FAST) {
            this.M = true;
            this.b(fp.THREESOME_SLOW);
            this.a("animation.shared.double_holding_back", true, var4, true);
            GalathEntity var5 = this.a_clash413(false);
            if (var5 != null) {
               var5.a_clash695();
            }

            return true;
         } else if (this.M && var1 == fp.THREESOME_SLOW) {
            this.M = false;
            this.a("animation.shared.double_holding_slow", true, var4, true);
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      AnimationController var2 = var1.getController();
      if (this.s == var2) {
         if (this.b_clash424() == null) {
            return PlayState.STOP;
         }

         this.a("animation.manglelie.angry_face", true, var1);
         return PlayState.CONTINUE;
      } else if (this.E == var2) {
         if (this.getCurrentAction() == fp.NULL && !this.r_clash411()) {
            if (Math.abs(this.field_70169_q - this.field_70165_t) + Math.abs(this.field_70166_s - this.field_70161_v) > 0.0) {
               if ((Boolean)this.m.func_187225_a(ar)) {
                  this.a("animation.manglelie.scared_run", true, var1);
               } else {
                  this.a("animation.manglelie.walk", true, var1);
               }

               this.field_70177_z = this.field_70759_as;
               return PlayState.CONTINUE;
            } else {
               this.a("animation.manglelie.idle", true, var1);
               return PlayState.CONTINUE;
            }
         } else {
            return PlayState.STOP;
         }
      } else {
         switch (this.getCurrentAction()) {
            case RUN:
               this.a("animation.manglelie.running", true, var1);
               break;
            case RIDE_MOMMY_HEAD:
               this.a("animation.manglelie.sit_on_galath", true, var1);
               break;
            case THREESOME_SLOW:
               if (this.M) {
                  this.a("animation.shared.double_holding_back", true, var1);
               } else {
                  this.a("animation.shared.double_holding_slow", 4, 0.33F, var1);
               }
               break;
            case THREESOME_FAST:
               if (this.Y) {
                  this.a("animation.shared.double_holding_hard", 3, 0.33F, var1);
               } else {
                  this.a("animation.shared.double_holding_soft", true, var1);
               }
               break;
            case THREESOME_CUM:
               this.a("animation.shared.double_holding_cum", true, var1);
               break;
            default:
               return PlayState.STOP;
         }

         return PlayState.CONTINUE;
      }
   }

   @Override
   public void registerControllers(AnimationData var1) {
      var1.addAnimationController(this.E);
      var1.addAnimationController(this.s);
      this.C.registerSoundListener(var1x -> {
         switch (var1x.sound) {
            case "pound":
               this.a(SoundHandler.MISC_POUNDING);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "cs0":
               this.an = 0;
               break;
            case "cs1":
               this.an = 1;
               break;
            case "cs2":
               this.an = 2;
               break;
            case "sexui":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "doubleSemen0":
               this.a(SoundHandler.MISC_INSERTS, 6.0F);
               this.a(SoundHandler.MISC_POUNDING);
            case "doubleSemen":
               CummyEntity.a(new ep(10, var0 -> {
                  Vec3d var1xx = var0.d_clash548("semenEmitter");
                  Vec3d var2 = var0.d_clash548("semenDir");
                  return var1xx.func_178788_d(var2).func_72432_b();
               }, var0 -> var0.getCachedBoneOffset("semenEmitter").func_178787_e(var0.getTargetPosition()), this, 0.3F, 0.3F));
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
         }
      });
      var1.addAnimationController(this.C);
   }


   public static class b {
      @SubscribeEvent
      public void a(Arrow var1) {
         RayTraceResult var2 = var1.getRayTraceResult();
         EntityArrow var3 = var1.getArrow();
         if (var3.field_70250_c instanceof ManglelieEntity) {
            if (var2.field_72308_g instanceof BaseGirlEntity) {
               var1.setCanceled(true);
            }
         }
      }

   }
}
