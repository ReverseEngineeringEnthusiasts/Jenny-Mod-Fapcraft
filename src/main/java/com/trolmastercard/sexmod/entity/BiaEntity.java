package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.entity.api.IEllie;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SendGirlToSexPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ad;
import com.trolmastercard.sexmod.util.af;
import com.trolmastercard.sexmod.util.ah;
import com.trolmastercard.sexmod.util.am;
import com.trolmastercard.sexmod.util.cj;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.dz;
import com.trolmastercard.sexmod.util.fg;







import java.util.UUID;
import javax.vecmath.Vector4d;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class BiaEntity extends AbstractGirlNpcEntity implements IEllie, fg {
   static final int ae = 3;
   public boolean Y = false;
   int ag = 0;
   boolean af = false;
   int Z = 0;
   boolean ab = true;
   int ac = -1;
   boolean aa = false;
   final int[] ai = new int[]{0, 180, -90, 90};
   final Vec3d[][] ad = new Vec3d[][]{
      {new Vec3d(0.5, 0.0, -0.5), new Vec3d(0.0, 0.0, -1.0)},
      {new Vec3d(0.5, 0.0, 1.5), new Vec3d(0.0, 0.0, 1.0)},
      {new Vec3d(-0.5, 0.0, 0.5), new Vec3d(-1.0, 0.0, 0.0)},
      {new Vec3d(1.5, 0.0, 0.5), new Vec3d(1.0, 0.0, 0.0)}
   };
   int ah = 1;

   public BiaEntity(World var1) {
      super(var1);
      this.func_70105_a(0.49F, 1.65F);
      this.P = 140;
      this.O = 50;
      this.K = 140;
      this.V = new Vec3d(0.0, -0.029999997854232782, -0.2);
   }

   @Override
   public String c_clash241() {
      return "Bia";
   }

   @Override
   public float i_clash226() {
      return -0.2F;
   }

   @Override
   public void c_clash237() {
      this.a_clash541("I am living here now nya~");
      this.a(SoundHandler.GIRLS_BIA_BREATH);
   }

   @Override
   public void b_clash158() {
      this.Y = true;
   }

   @Override
   public void b(fp var1) {
      fp var2 = this.y_clash492();
      if (var2 == fp.ANAL_CUM || var2 == fp.PRONE_DOGGY_CUM) {
         this.m.func_187227_b(h, "");
      }

      if (var2 != fp.ANAL_CUM || var1 != fp.ANAL_FAST && var1 != fp.ANAL_SLOW) {
         if (var2 != fp.PRONE_DOGGY_CUM || var1 != fp.PRONE_DOGGY_HARD && var1 != fp.PRONE_DOGGY_SOFT) {
            super.b(var1);
         }
      }
   }

   @Override
   protected ResourceLocation func_184647_J() {
      return dz.c;
   }

   @Override
   public void func_70619_bc() {
      super.func_70619_bc();
      if (this.ab) {
         this.func_189654_d(false);
         this.field_70145_X = false;
         this.ab = false;
      }

      if (this.Y) {
         this.ag++;
         if (!this.func_174791_d().equals(this.o_clash501()) && this.ag <= 40) {
            this.field_70177_z = this.I_clash415();

            try {
               e.equals(null);
            } catch (NullPointerException var2) {
               this.c_clash502(this.aa_clash545());
            }

            this.func_189654_d(false);
            Vec3d var1 = RotationHelper.a(this.func_174791_d(), this.o_clash501(), 40 - this.ag);
            this.func_70107_b(var1.field_72450_a, var1.field_72448_b, var1.field_72449_c);
         } else {
            this.Y = false;
            this.ag = 0;
            this.b_clash431(this.field_70170_p.func_73046_m().func_184103_al().func_177451_a(this.ae_clash498()).field_70177_z + 180.0F);
            this.m.func_187227_b(G, true);
            this.func_70661_as().func_75499_g();
            this.U();
         }
      }

      if (this.af) {
         if (!(this.func_174791_d().func_72438_d(this.o_clash501()) < 0.6) && this.Z <= 200) {
            this.Z++;
            if (this.Z == 60 || this.Z == 120) {
               this.func_70661_as().func_75499_g();
               this.func_70661_as().func_75492_a(this.o_clash501().field_72450_a, this.o_clash501().field_72448_b, this.o_clash501().field_72449_c, 0.35);
            }
         } else {
            this.af = false;
            this.m.func_187227_b(G, true);
            this.Z = 0;
            this.field_70145_X = true;
            this.func_189654_d(true);
            this.field_70159_w = 0.0;
            this.field_70181_x = 0.0;
            this.field_70179_y = 0.0;
            if ("anal".equals(this.m.func_187225_a(h))) {
               this.b(fp.ANAL_PREPARE);
               this.f(0);
            } else {
               this.b(fp.SITDOWN);
            }
         }
      }
   }

   public boolean func_184645_a(EntityPlayer var1, EnumHand var2) {
      if (super.func_184645_a(var1, var2)) {
         return true;
      }

      if (this.y_clash492() == fp.SITDOWNIDLE) {
         return true;
      }

      ItemStack var3 = var1.func_184586_b(var2);
      boolean var4 = var3.func_77973_b() == Items.field_151057_cb;
      if (var4) {
         var3.func_111282_a(var1, this, var2);
         return true;
      }

      if (this.field_70170_p.field_72995_K && !this.b_clash230(var1)) {
         this.a_clash541(I18n.func_135052_a("bia.dialogue.busy", new Object[0]));
      }

      return true;
   }

   @Override
   public boolean b_clash230(EntityPlayer var1) {
      if (this.ae_clash498() == null
         && (!this.J_clash526() || ((String)this.m.func_187225_a(v)).equals(Minecraft.func_71410_x().field_71439_g.getPersistentID().toString()))) {
         String[] var2 = new String[]{
            this.m.func_187225_a(D) == 1 ? "action.names.strip" : "action.names.dressup", "action.names.talk", "action.names.headpat"
         };
         a(var1, this, var2, true);
         return true;
      } else {
         return false;
      }
   }

   void b_clash286(EntityPlayer var1) {
      a(var1, this, new String[]{"action.names.anal", "doggy"}, false);
   }

   @Override
   public void ac() {
      if (this.Q_clash505() && !this.aa) {
         this.r_clash533();
      }

      this.aa = false;
   }

   @Override
   public void func_70071_h_() {
      super.func_70071_h_();
      if (this.field_70170_p.field_72995_K && this.n_clash537() && this.y_clash492() == fp.PRONE_DOGGY_INTRO && !BeeScreen.a_clash731()) {
         HornyMeterHud.d_clash358();
      }

      this.d_clash287();
   }

   @Override
   protected void V() {
      super.V();
      this.ac = -1;
   }

   void d_clash287() {
      fp var1 = this.y_clash492();
      if (var1 == fp.ANAL_WAIT || var1 == fp.SITDOWNIDLE) {
         EntityPlayer var2 = this.field_70170_p.func_72890_a(this, 10.0);
         if (var2 != null) {
            if (!(var2.func_70032_d(this) > 1.0F)) {
               if (this.ac == -1) {
                  if (this.field_70170_p.field_72995_K) {
                     BeeScreen.b_clash732();
                     d3.a_clash122(false);
                  } else {
                     this.e_clash499(var2.getPersistentID());
                  }

                  this.ac = -1;
               } else if (--this.ac <= 0) {
                  this.ac = -1;
                  var2.field_70145_X = true;
                  var2.func_189654_d(true);
                  if (var1 == fp.ANAL_WAIT) {
                     if (!this.field_70170_p.field_72995_K) {
                        this.b(fp.ANAL_START);
                        Vec3d var7 = this.o_clash501().func_178787_e(ck.a(-0.3, -1.0, -0.5, this.I_clash415()));
                        var2.func_70634_a(var7.field_72450_a, var7.field_72448_b, var7.field_72449_c);
                     } else if (this.n_clash537()) {
                        HornyMeterHud.d_clash358();
                     }
                  } else {
                     float var3 = this.I_clash415();
                     var2.field_70177_z = var3;
                     var2.field_70125_A = 60.0F;
                     if (!this.field_70170_p.field_72995_K) {
                        this.f(0);
                        this.b(fp.PRONE_DOGGY_INTRO);
                        Vec3d var4 = this.o_clash501();
                        Vec3d var5 = var4.func_178787_e(ck.a(0.0, 0.0, 1.0, var3));
                        this.c_clash502(var5);
                        Vec3d var6 = var4.func_178787_e(ck.a(0.0, 1.1875 - var2.func_70047_e(), 0.5, var3));
                        var2.func_70634_a(var6.field_72450_a, var6.field_72448_b, var6.field_72449_c);
                        this.a_clash504(true);
                     }
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void ag() {
      super.ag();
      if (this.y_clash492() == fp.PRONE_DOGGY_HARD) {
         int var1 = this.ah;

         do {
            this.ah = this.func_70681_au().nextInt(3) + 1;
         } while (var1 == this.ah);
      }
   }

   @Override
   public void g_clash238() {
      this.z = new EntityAIWanderAvoidWater(this, 0.35);
      this.o = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.field_70714_bg.func_75776_a(5, this.o);
      this.field_70714_bg.func_75776_a(5, this.z);
   }

   @Override
   public void a(String var1, UUID var2) {
      super.a(var1, var2);
      switch (var1) {
         case "action.names.talk":
            this.e_clash499(Minecraft.func_71410_x().field_71439_g.getPersistentID());
            this.a_clash490("playerSheHasSexWith", Minecraft.func_71410_x().field_71439_g.getPersistentID().toString());
            this.a_clash490("animationFollowUp", "talkHorny");
            this.a_clash288(var2);
            break;
         case "action.names.headpat":
            this.e_clash499(Minecraft.func_71410_x().field_71439_g.getPersistentID());
            this.a_clash490("playerSheHasSexWith", Minecraft.func_71410_x().field_71439_g.getPersistentID().toString());
            this.a_clash490("animationFollowUp", "Headpat");
            this.a_clash288(var2);
            break;
         case "action.names.anal":
            this.a_clash490("animationFollowUp", "anal");
            this.b(fp.TALK_RESPONSE);
            this.aa = true;
            break;
         case "doggy":
            this.a_clash490("animationFollowUp", "doggy");
            this.b(fp.TALK_RESPONSE);
            this.aa = true;
            break;
         case "action.names.dressup":
         case "action.names.strip":
            this.b(fp.STRIP);
      }
   }

   public void func_70645_a(DamageSource var1) {
      super.func_70645_a(var1);
      if (!this.field_70170_p.field_72995_K) {
         EntityItem var2 = new EntityItem(
            this.field_70170_p,
            this.field_70165_t,
            this.field_70163_u,
            this.field_70161_v,
            new ItemStack(Blocks.field_150325_L, this.func_70681_au().nextInt(4), 12)
         );
         this.field_70170_p.func_72838_d(var2);
      }
   }

   void a_clash288(UUID var1) {
      this.a(true, true, var1);
      d3.a_clash122(false);
   }

   Vector4d a_clash289() {
      BlockPos var1 = null;
      int var2 = 0;

      while (!this.a_clash290(var1)) {
         var1 = this.a(this.func_180425_c(), var2);
         if (++var2 == 50) {
            break;
         }
      }

      if (var1 != null && var2 != 50) {
         this.field_70714_bg.func_85156_a(this.z);
         this.field_70714_bg.func_85156_a(this.o);
         Vec3d var3 = new Vec3d(var1.func_177958_n(), var1.func_177956_o(), var1.func_177952_p());
         int var4 = -1;

         for (int var5 = 0; var5 < this.ad.length; var5++) {
            Vec3d var6 = var3.func_178787_e(this.ad[var5][1]);
            Vec3d var7 = var3.func_178788_d(this.ad[var5][1]);
            Block var8 = this.field_70170_p.func_180495_p(new BlockPos(var6.field_72450_a, var6.field_72448_b, var6.field_72449_c)).func_177230_c();
            if (var8 == Blocks.field_150350_a && cj.b(this.field_70170_p, new BlockPos(var7))) {
               if (var4 == -1) {
                  var4 = var5;
               } else {
                  double var9 = this.func_180425_c()
                     .func_177954_c(
                        var3.func_178787_e(this.ad[var4][0]).field_72450_a,
                        var3.func_178787_e(this.ad[var4][0]).field_72448_b,
                        var3.func_178787_e(this.ad[var4][0]).field_72449_c
                     );
                  double var11 = this.func_180425_c()
                     .func_177954_c(
                        var3.func_178787_e(this.ad[var5][0]).field_72450_a,
                        var3.func_178787_e(this.ad[var5][0]).field_72448_b,
                        var3.func_178787_e(this.ad[var5][0]).field_72449_c
                     );
                  if (var11 < var9) {
                     var4 = var5;
                  }
               }
            }
         }

         if (var4 == -1) {
            this.a(SoundHandler.GIRLS_BIA_BREATH[2]);
            this.a_clash541(I18n.func_135052_a("jenny.dialogue.nobedinsight", new Object[0]));
            return null;
         } else {
            Vec3d var13 = var3.func_178787_e(this.ad[var4][0]);
            return new Vector4d(var13.field_72450_a, var13.field_72448_b, var13.field_72449_c, this.ai[var4]);
         }
      } else {
         this.a(SoundHandler.GIRLS_BIA_BREATH[2]);
         this.a_clash541(I18n.func_135052_a("jenny.dialogue.nobedinsight", new Object[0]));
         return null;
      }
   }

   boolean a_clash290(BlockPos var1) {
      if (var1 == null) {
         return false;
      } else if (cj.b(this.field_70170_p, var1.func_177978_c()) && this.field_70170_p.func_175623_d(var1.func_177968_d())) {
         return true;
      } else if (cj.b(this.field_70170_p, var1.func_177974_f()) && this.field_70170_p.func_175623_d(var1.func_177976_e())) {
         return true;
      } else {
         return cj.b(this.field_70170_p, var1.func_177968_d()) && this.field_70170_p.func_175623_d(var1.func_177978_c())
            ? true
            : cj.b(this.field_70170_p, var1.func_177976_e()) && this.field_70170_p.func_175623_d(var1.func_177974_f());
      }
   }

   Vector4d b_clash291() {
      BlockPos var1 = this.a_clash525(this.func_180425_c());
      if (var1 == null) {
         this.a(SoundHandler.GIRLS_BIA_BREATH[2]);
         this.a_clash541(I18n.func_135052_a("jenny.dialogue.nobedinsight", new Object[0]));
         return null;
      }

      this.field_70714_bg.func_85156_a(this.z);
      this.field_70714_bg.func_85156_a(this.o);
      Vec3d var2 = new Vec3d(var1.func_177958_n(), var1.func_177956_o(), var1.func_177952_p());
      int var3 = -1;

      for (int var4 = 0; var4 < this.ad.length; var4++) {
         Vec3d var5 = var2.func_178787_e(this.ad[var4][1]);
         if (this.field_70170_p.func_180495_p(new BlockPos(var5.field_72450_a, var5.field_72448_b, var5.field_72449_c)).func_177230_c()
            == Blocks.field_150350_a) {
            if (var3 == -1) {
               var3 = var4;
            } else {
               double var6 = this.func_180425_c()
                  .func_177954_c(
                     var2.func_178787_e(this.ad[var3][0]).field_72450_a,
                     var2.func_178787_e(this.ad[var3][0]).field_72448_b,
                     var2.func_178787_e(this.ad[var3][0]).field_72449_c
                  );
               double var8 = this.func_180425_c()
                  .func_177954_c(
                     var2.func_178787_e(this.ad[var4][0]).field_72450_a,
                     var2.func_178787_e(this.ad[var4][0]).field_72448_b,
                     var2.func_178787_e(this.ad[var4][0]).field_72449_c
                  );
               if (var8 < var6) {
                  var3 = var4;
               }
            }
         }
      }

      if (var3 == -1) {
         this.a(SoundHandler.GIRLS_BIA_BREATH[2]);
         this.a_clash541(I18n.func_135052_a("jenny.dialogue.bedobscured", new Object[0]));
         return null;
      } else {
         Vec3d var10 = var2.func_178787_e(this.ad[var3][0]);
         return new Vector4d(var10.field_72450_a, var10.field_72448_b, var10.field_72449_c, this.ai[var3]);
      }
   }

   @Override
   public void a_clash292() {
      String var1 = (String)this.m.func_187225_a(h);
      Vector4d var2 = var1.equals("anal") ? this.b_clash291() : this.a_clash289();
      if (var2 != null) {
         Vec3d var3 = new Vec3d(var2.getX(), var2.getY(), var2.getZ());
         this.b_clash431((float)var2.getW());
         this.c_clash502(var3);
         this.r = this.I_clash415();
         this.func_70661_as().func_75499_g();
         this.func_70661_as().func_75492_a(var3.field_72450_a, var3.field_72448_b, var3.field_72449_c, 0.35);
         this.af = true;
         this.Z = 0;
      }
   }

   @Override
   protected fp c_clash235(fp var1) {
      if (var1 == fp.ANAL_SLOW) {
         return fp.ANAL_FAST;
      } else {
         return var1 == fp.PRONE_DOGGY_INTRO ? fp.PRONE_DOGGY_INSERT : null;
      }
   }

   @Override
   protected fp a_clash236(fp var1) {
      if (var1 == fp.ANAL_SLOW || var1 == fp.ANAL_FAST) {
         return fp.ANAL_CUM;
      } else {
         return var1 != fp.PRONE_DOGGY_SOFT && var1 != fp.PRONE_DOGGY_HARD ? null : fp.PRONE_DOGGY_CUM;
      }
   }

   @Override
   protected void U() {
      switch ((String)this.m.func_187225_a(h)) {
         case "talkHorny":
            this.b(fp.TALK_HORNY);
            break;
         case "Headpat":
            this.b(fp.HEAD_PAT);
            break;
         case "doggy":
         case "anal":
            this.r_clash533();
            PacketHandler.b.sendToServer(new SendGirlToSexPacket(this.f_clash491()));
            return;
      }

      if (this.field_70170_p.field_72995_K) {
         this.a_clash490("animationFollowUp", "");
      } else {
         this.m.func_187227_b(h, "");
      }
   }

   @Override
   public float T_clash293() {
      return 35.0F;
   }

   @Override
   public float ai_clash294() {
      return 140.0F;
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      if (this.field_70170_p instanceof SexWorldClient) {
         return null;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.y_clash492() == fp.NULL && this.y_clash492().autoBlink) {
               this.a("animation.bia.fhappy", true, var1);
            } else {
               this.a("animation.bia.null", true, var1);
            }
            break;
         case "movement":
            if (this.y_clash492() != fp.NULL) {
               this.a("animation.bia.null", true, var1);
            } else if (this.func_184218_aH()) {
               this.a("animation.bia.sit", true, var1);
            } else if (Math.abs(this.field_70169_q - this.field_70165_t) + Math.abs(this.field_70166_s - this.field_70161_v) > 0.0) {
               switch (this.q_clash489()) {
                  case RUN:
                     this.a("animation.bia.run", true, var1);
                     break;
                  case FAST_WALK:
                     this.a("animation.bia.fastwalk", true, var1);
                     break;
                  case WALK:
                     this.a("animation.bia.walk", true, var1);
               }

               this.field_70177_z = this.field_70759_as;
            } else {
               this.a("animation.bia.idle", true, var1);
            }
            break;
         case "action":
            switch (this.y_clash492()) {
               case NULL:
                  this.a("animation.bia.null", true, var1);
                  break;
               case STRIP:
                  this.a("animation.bia.strip", false, var1);
                  break;
               case ATTACK:
                  this.a("animation.bia.attack" + this.S, false, var1);
                  break;
               case BOW:
                  this.a("animation.bia.bowcharge", false, var1);
                  break;
               case RIDE:
                  this.a("animation.bia.ride", true, var1);
                  break;
               case SIT:
                  this.a("animation.bia.sit", true, var1);
                  break;
               case THROW_PEARL:
                  this.a("animation.bia.throwpearl", false, var1);
                  break;
               case DOWNED:
                  this.a("animation.bia.downed", true, var1);
                  break;
               case TALK_HORNY:
                  this.a("animation.bia.talk_horny2", true, var1);
                  break;
               case TALK_IDLE:
                  this.a("animation.bia.talk_idle2", true, var1);
                  break;
               case TALK_RESPONSE:
                  this.a("animation.bia.talk_response", true, var1);
                  break;
               case ANAL_PREPARE:
                  this.a("animation.bia.anal_prepare", false, var1);
                  break;
               case ANAL_WAIT:
                  this.a("animation.bia.anal_wait", false, var1);
                  break;
               case ANAL_START:
                  this.a("animation.bia.anal_start", true, var1);
                  break;
               case ANAL_SLOW:
                  this.a("animation.bia.anal_slow", true, var1);
                  break;
               case ANAL_FAST:
                  this.a("animation.bia.anal_fast", true, var1);
                  break;
               case ANAL_CUM:
                  this.a("animation.bia.anal_cum", false, var1);
                  break;
               case HEAD_PAT:
                  this.a("animation.bia.headpat", false, var1);
                  break;
               case SITDOWN:
                  this.a("animation.bia.sitdown", false, var1);
                  break;
               case SITDOWNIDLE:
                  this.a("animation.bia.sitdownidle", true, var1);
                  break;
               case PRONE_DOGGY_INTRO:
                  this.a("animation.bia.prone_doggy_intro", true, var1);
                  break;
               case PRONE_DOGGY_INSERT:
                  this.a("animation.bia.prone_doggy_insert", true, var1);
                  break;
               case PRONE_DOGGY_SOFT:
                  this.a("animation.bia.prone_doggy_soft", true, var1);
                  break;
               case PRONE_DOGGY_HARD:
                  this.a("animation.bia.prone_doggy_hard" + this.ah, true, var1);
                  break;
               case PRONE_DOGGY_CUM:
                  this.a("animation.bia.prone_doggy_cum", true, var1);
                  break;
               case WAVE_IDLE:
                  this.a("animation.bia.wave_idle", true, var1);
                  break;
               case WAVE:
                  this.a("animation.bia.wave", true, var1);
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
               this.b(fp.NULL);
               if (++this.S == 3) {
                  this.S = 0;
               }
               break;
            case "becomeNude":
               if (this.e_clash544()) {
                  this.a_clash490("currentModel", this.m.func_187225_a(D) == 1 ? "0" : "1");
               }
               break;
            case "stripDone":
               this.r_clash533();
               this.U();
               break;
            case "stripMSG1":
               this.a_clash541(I18n.func_135052_a("bia.dialogue.hihi", new Object[0]));
               this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_BIA_GIGGLE));
               break;
            case "sexUiOn":
               if (this.n_clash537()) {
                  HornyMeterHud.d_clash358();
               }
               break;
            case "pearl":
               PacketHandler.b.sendToServer(new SendCompanionHomePacket(this.f_clash491()));
               break;
            case "talk_hornyMSG1":
               this.a_clash541(I18n.func_135052_a("bia.dialogue.heya", new Object[0]));
               this.a(SoundHandler.GIRLS_BIA_HEY);
               break;
            case "talk_hornyMSG2":
               this.a_clash541(I18n.func_135052_a("bia.dialogue.horny", new Object[0]));
               this.a(SoundHandler.GIRLS_BIA_GIGGLE[2]);
               break;
            case "talk_hornyMSG3":
               this.a_clash541(I18n.func_135052_a("bia.dialogue.so", new Object[0]));
               this.a(SoundHandler.GIRLS_BIA_BREATH[0]);
               break;
            case "talk_hornyMSG4":
               this.a_clash541(I18n.func_135052_a("bia.dialogue.fun", new Object[0]));
               this.a(SoundHandler.GIRLS_BIA_HUH[0]);
               break;
            case "talk_hornyDone":
               this.b(fp.TALK_IDLE);
               if (this.n_clash537()) {
                  this.b_clash286(Minecraft.func_71410_x().field_71439_g);
               }
               break;
            case "talk_responseMSG1":
               this.a_clash541(I18n.func_135052_a("bia.dialogue.huh", new Object[0]));
               this.a(SoundHandler.GIRLS_BIA_HUH[2]);
               break;
            case "talk_responseMSG2":
               this.a_clash541(I18n.func_135052_a("bia.dialogue.iuhm", new Object[0]));
               this.a(SoundHandler.GIRLS_BIA_BREATH[1]);
               break;
            case "talk_responseMSG3":
               this.a_clash541(I18n.func_135052_a("bia.dialogue.yes", new Object[0]));
               this.a(SoundHandler.GIRLS_BIA_GIGGLE[0]);
               break;
            case "talk_responseDone":
               if (this.n_clash537()) {
                  this.s();
               }

               this.U();
               break;
            case "anal_prepareMSG1":
               this.a(SoundHandler.MISC_PLOB[0]);
               break;
            case "anal_prepareMSG2":
               this.a(SoundHandler.MISC_BEDRUSTLE[0]);
               break;
            case "anal_prepareDone":
               this.b(fp.ANAL_WAIT);
               if (this.n_clash537()) {
                  HornyMeterHud.b_clash363();
               }
               break;
            case "anal_startMSG1":
               this.a(SoundHandler.GIRLS_BIA_MMM[3]);
               this.a(SoundHandler.MISC_POUNDING[34]);
               break;
            case "anal_fastMSG1":
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.02);
               }
            case "anal_slowMSG1":
            case "anal_startMSG2":
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.02);
               }

               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING), 0.5F);
               this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_BIA_AHH));
               break;
            case "anal_fastDone":
               if (!this.n_clash537() || d3.d) {
                  return;
               }
            case "anal_startDone":
               this.b(fp.ANAL_SLOW);
               if (this.n_clash537()) {
                  HornyMeterHud.d_clash358();
               }
               break;
            case "anal_cumMSG2":
               this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_BIA_AHH));
               break;
            case "blackScreen":
            case "anal_cumBlackScreen":
               if (this.n_clash537()) {
                  BeeScreen.b_clash732();
               }
               break;
            case "doggy_cumDone":
            case "anal_cumDone":
               if (this.n_clash537()) {
                  HornyMeterHud.b_clash363();
                  this.r_clash533();
               }
               break;
            case "headpatMSG1":
               this.a_clash541(I18n.func_135052_a("bia.dialogue.headpats", new Object[0]));
               this.a(SoundHandler.GIRLS_BIA_BREATH[0]);
               break;
            case "headpatMSG2":
               this.a_clash541(I18n.func_135052_a("bia.dialogue.hmm", new Object[0]));
               this.a(SoundHandler.GIRLS_BIA_MMM[0]);
               break;
            case "headpatMSG3":
               this.a_clash541(I18n.func_135052_a("bia.dialogue.huh2", new Object[0]));
               this.a(SoundHandler.GIRLS_BIA_HUH[0]);
               break;
            case "headpatMSG4":
               this.a_clash541(I18n.func_135052_a("bia.dialogue.thankyou", new Object[0]));
               this.a(SoundHandler.GIRLS_BIA_GIGGLE[1]);
               break;
            case "headpatDone":
               this.r_clash533();
               break;
            case "sitdownMSG1":
               this.a_clash541("come here big boy~");
               this.a(SoundHandler.GIRLS_BIA_BREATH);
               break;
            case "sitdownDone":
               this.b(fp.SITDOWNIDLE);
               break;
            case "slide":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_SLIDE));
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.005);
               }
               break;
            case "pound":
               this.a(SoundHandler.MISC_POUNDING);
               break;
            case "doggyMoan":
               this.a(this.func_70681_au().nextBoolean() ? SoundHandler.GIRLS_BIA_AHH : SoundHandler.GIRLS_BIA_MMM);
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.04);
               }
               break;
            case "doggySwitch":
               if (this.n_clash537() && d3.d) {
                  this.b(fp.PRONE_DOGGY_HARD);
               }
               break;
            case "doggyReset":
               if (this.n_clash537() && d3.d) {
                  this.N();
               }
               break;
            case "cum":
               this.a(SoundHandler.MISC_INSERTS, 6.0F);
               break;
            case "orgasm1":
               this.a(SoundHandler.GIRLS_BIA_MMM[6]);
               break;
            case "orgasm2":
               this.a(SoundHandler.GIRLS_BIA_MMM[7]);
         }
      };
      this.C.registerSoundListener(var2);
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.E);
      var1.addAnimationController(this.s);
   }

   private static NullPointerException a(NullPointerException var0) {
      return var0;
   }
}
