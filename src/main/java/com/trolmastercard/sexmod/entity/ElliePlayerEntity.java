package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.EllieModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.ChangeDataParameterPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SendGirlToSexPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.networking.SexPromptPacket;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.d3;







import com.google.common.base.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class ElliePlayerEntity extends AbstractPlayerGirlEntity {
   boolean ar = false;
   boolean aq = false;
   int ap = 1;

   protected ElliePlayerEntity(World var1) {
      super(var1);
   }

   public ElliePlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public float i_clash226() {
      return 2.05F;
   }

   public float func_70047_e() {
      return this.a_clash382() ? 1.53F : 1.9F;
   }

   @Override
   public void u_clash377() {
      this.b(fp.SITDOWN);
   }

   @Override
   public void b(String var1, UUID var2) {
      if ("Face fuck".equals(var1)) {
         this.b_clash577(var2);
         this.b(fp.CARRY_INTRO);
         this.a(this.ah_clash493(), fp.CARRY_INTRO);
      }
   }

   @Override
   public IVanillaModel a_clash228(int var1) {
      return new EllieModel();
   }

   @Override
   public String c_clash229(int var1) {
      return var1 == 0 ? "textures/entity/ellie/hand_nude.png" : "textures/entity/ellie/hand.png";
   }

   @Override
   public boolean p_clash379() {
      return true;
   }

   @Override
   public void a(String var1, UUID var2) {
      if ("action.names.cowgirl".equals(var1)) {
         this.a_clash490("animationFollowUp", "Cowgirl");
      } else if ("action.names.missionary".equals(var1)) {
         this.a_clash490("animationFollowUp", "Missionary");
      } else if (((Optional)this.m.func_187225_a(ai)).isPresent()) {
         PacketHandler.b.sendToServer(new SexPromptPacket(var1, var2, (UUID)((Optional)this.m.func_187225_a(ai)).get(), this.ab));
         this.ab = true;
      }
   }

   @Override
   public boolean b_clash230(EntityPlayer var1) {
      a(var1, this, new String[]{"Face fuck"}, false);
      return true;
   }

   void c_clash380(EntityPlayer var1) {
      a(var1, this, new String[]{"action.names.cowgirl", "action.names.missionary"}, false);
   }

   @Override
   public boolean A_clash381() {
      return false;
   }

   @Override
   public void b(fp var1) {
      fp var2 = this.y_clash492();
      if (var2 != fp.MISSIONARY_CUM || var1 != fp.MISSIONARY_FAST && var1 != fp.MISSIONARY_SLOW) {
         if (var2 != fp.COWGIRLCUM || var1 != fp.COWGIRLSLOW && var1 != fp.COWGIRLFAST) {
            super.b(var1);
         }
      }
   }

   @Override
   protected fp c_clash235(fp var1) {
      if (var1 == fp.COWGIRLSLOW) {
         return fp.COWGIRLFAST;
      } else if (var1 == fp.MISSIONARY_SLOW) {
         return fp.MISSIONARY_FAST;
      } else {
         return var1 == fp.CARRY_SLOW ? fp.CARRY_FAST : null;
      }
   }

   @Override
   protected fp a_clash236(fp var1) {
      if (var1 == fp.COWGIRLFAST || var1 == fp.COWGIRLSLOW) {
         return fp.COWGIRLCUM;
      } else if (var1 == fp.MISSIONARY_FAST || var1 == fp.MISSIONARY_SLOW) {
         return fp.MISSIONARY_CUM;
      } else {
         return var1 != fp.CARRY_SLOW && var1 != fp.CARRY_FAST ? null : fp.CARRY_CUM;
      }
   }

   @Override
   public void func_70619_bc() {
      super.func_70619_bc();
      if (this.y_clash492() == fp.SITDOWNIDLE) {
         String var1 = (String)this.m.func_187225_a(BaseGirlEntity.h);
         if (!"Missionary".equals(var1) && !"Cowgirl".equals(var1)) {
            return;
         }

         EntityPlayer var2 = this.j_clash575();
         if (var2 == null || var2.func_70011_f(this.w_clash576().field_72450_a, this.w_clash576().field_72448_b, this.w_clash576().field_72449_c) > 1.0) {
            return;
         }

         this.m.func_187227_b(BaseGirlEntity.h, "");
         this.m.func_187227_b(BaseGirlEntity.D, 0);
         this.e_clash499(var2.getPersistentID());
         EntityPlayerMP var3 = (EntityPlayerMP)this.field_70170_p.func_152378_a((UUID)((Optional)this.m.func_187225_a(ai)).get());
         PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var2);
         PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), var3);
         var2.func_191958_b(0.0F, 0.0F, 0.0F, 0.0F);
         var3.field_71075_bZ.field_75100_b = true;
         var2.field_71075_bZ.field_75100_b = true;
         var3.field_70145_X = true;
         var2.field_70145_X = true;
         var3.func_189654_d(true);
         var2.func_189654_d(true);
         if ("Missionary".equals(var1)) {
            this.b(fp.MISSIONARY_START);
            Vec3d var4 = this.w_clash576().func_178786_a(0.0, 0.1, 0.0);
            var2.func_70080_a(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c, this.I_clash415(), 60.0F);
            var2.func_70634_a(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c);
         } else {
            this.b(fp.COWGIRLSTART);
            Vec3d var5 = this.w_clash576()
               .func_178787_e(
                  new Vec3d(
                     -Math.sin(this.I_clash415().floatValue() * (Math.PI / 180.0)) * 1.8,
                     -0.65,
                     Math.cos(this.I_clash415().floatValue() * (Math.PI / 180.0)) * 1.8
                  )
               );
            var2.func_70080_a(var5.field_72450_a, var5.field_72448_b, var5.field_72449_c, 180.0F + this.I_clash415(), -30.0F);
            var2.func_70634_a(var5.field_72450_a, var5.field_72448_b, var5.field_72449_c);
         }
      }
   }

   boolean a_clash382() {
      EntityPlayer var1 = this.k_clash584();
      return var1 == null
         ? false
         : this.field_70170_p.func_180495_p(var1.func_180425_c().func_177984_a().func_177984_a()).func_177230_c() != Blocks.field_150350_a;
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.y_clash492() == fp.NULL && this.y_clash492().autoBlink) {
               this.a("animation.ellie.eyes", true, var1);
            } else {
               this.a("animation.ellie.null", true, var1);
            }
            break;
         case "movement":
            if (this.y_clash492() != fp.NULL) {
               this.a("animation.ellie.null", true, var1);
            } else if (this.ak) {
               this.a("animation.ellie.ride", true, var1);
            } else {
               if (this.E.getCurrentAnimation() != null && this.E.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.ar = !this.ar;
               }

               if (!this.af) {
                  this.a("animation.ellie.fly" + (this.ar ? "2" : ""), true, var1);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.E.setAnimationSpeed(1.5);
                     this.a(this.a_clash382() ? "animation.ellie.crouchwalk" : "animation.ellie.run", true, var1);
                  } else if (this.ao.y >= -0.1F) {
                     this.E.setAnimationSpeed(2.0);
                     this.a(this.a_clash382() ? "animation.ellie.crouchwalk" : "animation.ellie.fastwalk", true, var1);
                  } else {
                     this.E.setAnimationSpeed(1.5);
                     this.a(this.a_clash382() ? "animation.ellie.crouchwalk" : "animation.ellie.backwards_walk", true, var1);
                  }
               } else {
                  this.a(this.a_clash382() ? "animation.ellie.crouchidle" : "animation.ellie.idle", true, var1);
               }
            }
            break;
         case "action":
            switch (this.y_clash492()) {
               case NULL:
                  this.a("animation.ellie.null", true, var1);
                  break;
               case STRIP:
                  this.a("animation.ellie.strip", false, var1);
                  break;
               case DASH:
                  this.a("animation.ellie.dash", false, var1);
                  break;
               case HUG:
                  this.a("animation.ellie.hug", false, var1);
                  break;
               case HUGIDLE:
                  this.a("animation.ellie.hugidle", true, var1);
                  break;
               case HUGSELECTED:
                  this.a("animation.ellie.hugselected", false, var1);
                  break;
               case SITDOWN:
                  this.a("animation.ellie.sitdown", false, var1);
                  break;
               case SITDOWNIDLE:
                  this.a("animation.ellie.sitdownidle", true, var1);
                  break;
               case COWGIRLSTART:
                  this.a("animation.ellie.cowgirlstart", false, var1);
                  break;
               case COWGIRLSLOW:
                  this.a("animation.ellie.cowgirlslow2", true, var1);
                  break;
               case COWGIRLFAST:
                  this.a("animation.ellie.cowgirlfast", true, var1);
                  break;
               case COWGIRLCUM:
                  this.a("animation.ellie.cowgirlcum", true, var1);
                  break;
               case ATTACK:
                  this.a("animation.ellie.attack" + this.S, false, var1);
                  break;
               case BOW:
                  this.a("animation.ellie.bowcharge", false, var1);
                  break;
               case RIDE:
                  this.a("animation.ellie.ride", true, var1);
                  break;
               case SIT:
                  this.a("animation.ellie.sit", true, var1);
                  break;
               case THROW_PEARL:
                  this.a("animation.ellie.throwpearl", false, var1);
                  break;
               case DOWNED:
                  this.a("animation.ellie.downed", true, var1);
                  break;
               case MISSIONARY_START:
                  this.a("animation.ellie.missionary_start", false, var1);
                  break;
               case MISSIONARY_SLOW:
                  this.a("animation.ellie.missionary_slow", true, var1);
                  break;
               case MISSIONARY_FAST:
                  this.a("animation.ellie.missionary_fast", true, var1);
                  break;
               case MISSIONARY_CUM:
                  this.a("animation.ellie.missionary_cum", false, var1);
                  break;
               case CARRY_INTRO:
                  this.a("animation.ellie.carry_intro", false, var1);
                  break;
               case CARRY_SLOW:
                  this.a("animation.ellie.carry_slow" + this.ap, true, var1);
                  break;
               case CARRY_FAST:
                  this.a("animation.ellie.carry_fast", true, var1);
                  break;
               case CARRY_CUM:
                  this.a("animation.ellie.carry_cum", true, var1);
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
            case "dashMSG1":
               EntityPlayer var9 = this.field_70170_p.func_72890_a(this, 15.0);
               if (var9 != null) {
                  Vec3d var14 = this.func_174791_d().func_178788_d(var9.func_174791_d());
                  float var15 = (float)Math.atan2(var14.field_72449_c, var14.field_72450_a) * (float) (180.0 / Math.PI);
                  this.field_70177_z = var15;
                  this.field_70759_as = var15;
                  this.field_70761_aq = var15;
               }
               break;
            case "dashReady":
               if (this.e_clash544()) {
                  return;
               }
               break;
            case "dashDone":
               this.b(fp.HUG);
               EntityPlayer var8 = this.field_70170_p.func_72890_a(this, 15.0);
               if (var8 != null) {
                  float var13 = var8.field_70177_z;
                  this.field_70177_z = var13;
                  this.field_70759_as = var13;
                  this.field_70761_aq = var13;
               }
               break;
            case "hugMSG1":
               EntityPlayerSP var7 = Minecraft.func_71410_x().field_71439_g;
               if (var7.getPersistentID().equals(this.ae_clash498()) || var7.func_110124_au().equals(this.ae_clash498())) {
                  PacketHandler.b
                     .sendToServer(
                        new TeleportPlayerPacket(var7.func_110124_au().toString(), var7.func_174791_d(), var7.field_70177_z - 80.0F, var7.field_70125_A)
                     );
               }
               break;
            case "hugMSG2":
               this.h("Hmm...");
               this.a(SoundHandler.GIRLS_ELLIE_HMPH[3], 3.0F);
               break;
            case "hugMSG3":
               this.h("Hey!");
               this.a(SoundHandler.GIRLS_ELLIE_AHH[2], 3.0F);
               break;
            case "hugMSG4":
               this.h(I18n.func_135052_a("ellie.dialogue.mommyhorny", new Object[0]));
               this.a(SoundHandler.GIRLS_ELLIE_GIGGLE[0], 3.0F);
               break;
            case "hugMSG5":
               this.h(I18n.func_135052_a("ellie.dialogue.whattodo", new Object[0]));
               this.a(SoundHandler.GIRLS_ELLIE_HUH[1], 3.0F);
               break;
            case "hugDone":
               EntityPlayerSP var4 = Minecraft.func_71410_x().field_71439_g;
               if (var4.getPersistentID().equals(this.ae_clash498())) {
                  this.b(fp.HUGIDLE);
                  this.c_clash380(var4);
               }
               break;
            case "hugselectedMSG1":
               this.h(I18n.func_135052_a("ellie.dialogue.iknow", new Object[0]));
               this.a(SoundHandler.GIRLS_ELLIE_MMM[0], 3.0F);
               break;
            case "hugselectedMSG2":
               this.h(I18n.func_135052_a("ellie.dialogue.followmedarling", new Object[0]));
               this.a(SoundHandler.GIRLS_ELLIE_GIGGLE[3], 3.0F);
               break;
            case "hugselectedDone":
               if (this.e_clash544()) {
                  Vec3d var10 = this.func_174791_d();
                  var10 = var10.func_72441_c(
                     -Math.sin((this.field_70177_z + 90.0F) * (Math.PI / 180.0)) * -0.7803125F,
                     0.0,
                     Math.cos((this.field_70177_z + 90.0F) * (Math.PI / 180.0)) * -0.7803125F
                  );
                  var10 = var10.func_72441_c(
                     -Math.sin(this.field_70177_z * (Math.PI / 180.0)) * 0.5296875F, 0.0, Math.cos(this.field_70177_z * (Math.PI / 180.0)) * 0.5296875F
                  );
                  String var6 = var10.field_72450_a + "f" + var10.field_72448_b + "f" + var10.field_72449_c + "f";
                  PacketHandler.b.sendToServer(new ChangeDataParameterPacket(this.f_clash491(), "targetPos", var6));
                  this.r_clash533();
                  PacketHandler.b.sendToServer(new SendGirlToSexPacket(this.f_clash491()));
                  this.b(fp.NULL);
               }
               break;
            case "sitdownMSG1":
               this.a(SoundHandler.GIRLS_ELLIE_GIGGLE[3], 3.0F);
               if (this.e_clash544()) {
                  this.h(I18n.func_135052_a("ellie.dialogue.cometomommy", new Object[0]));
               }
               break;
            case "sitdownDone":
               if (this.f_clash579()) {
                  this.b(fp.SITDOWNIDLE);
                  this.c_clash380(this.field_70170_p.func_152378_a(this.m_clash583()));
               }
               break;
            case "missionary_startDone":
               if (this.n_clash537()) {
                  this.b(fp.MISSIONARY_SLOW);
                  HornyMeterHud.d_clash358();
               }
               break;
            case "cowgirlStartMSG0":
               this.a(SoundHandler.GIRLS_ELLIE_GIGGLE[4], 3.0F);
               break;
            case "cowgirlStartMSG1":
               if (this.e_clash544()) {
                  this.a_clash541(I18n.func_135052_a("ellie.dialogue.like", new Object[0]));
                  HornyMeterHud.b_clash363();
               }
               break;
            case "cowgirlStartMSG2":
               this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_ELLIE_AHH), 3.0F);
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.02);
               }
               break;
            case "cowgirlStartDone":
               if (this.n_clash537()) {
                  this.b(fp.COWGIRLSLOW);
                  HornyMeterHud.d_clash358();
               }
               break;
            case "cowgirlfastMSG1":
               if (this.aq) {
                  this.aq = false;
               } else {
                  this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_ELLIE_AHH), 3.0F);
               }

               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.04);
               }
               break;
            case "cowgirlfastReady":
               if (this.n_clash537()) {
                  if (!d3.d) {
                     this.b(fp.COWGIRLSLOW);
                  } else if (Reference.f.nextInt(4) != 1) {
                     this.C.clearAnimationCache();
                  }
               }
               break;
            case "cowgirlfastdomMSG1":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.2);
               }
               break;
            case "cowgirlcumMSG1":
               this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_ELLIE_AHH), 3.0F);
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING), 0.75F);
               break;
            case "cowgirlcumMSG2":
               this.a(SoundHandler.GIRLS_ELLIE_MOAN[5], 3.0F);
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING), 0.75F);
               break;
            case "cowgirlcumMSG3":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING), 0.75F);
               break;
            case "cowgirlcumMSG4":
               if (this.n_clash537()) {
                  HornyMeterHud.c_clash360();
               }
               break;
            case "cowgirlcumMSG5":
            case "missionary_cumMSG2":
               this.a(SoundHandler.GIRLS_ELLIE_GIGGLE[4], 3.0F);
               if (this.n_clash537()) {
                  this.a_clash541(I18n.func_135052_a("ellie.dialogue.goodboy", new Object[0]));
               }
               break;
            case "cowgirlcumMSG6":
               if (this.n_clash537()) {
                  BeeScreen.b_clash732();
               }
               break;
            case "missionary_cumDone":
            case "cowgirlcumDone":
            case "carry_cumDone":
               if (this.n_clash537()) {
                  HornyMeterHud.b_clash363();
                  this.r_clash533();
               }
               break;
            case "attackDone":
               if (++this.S == 3) {
                  this.S = 0;
               }
               break;
            case "pearl":
               PacketHandler.b.sendToServer(new SendCompanionHomePacket(this.f_clash491()));
               break;
            case "openSexUi":
               if (this.n_clash537()) {
                  HornyMeterHud.d_clash358();
               }
               break;
            case "missionary_slowMSG1":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING));
               if (this.func_70681_au().nextBoolean() && this.func_70681_au().nextBoolean()) {
                  this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_ELLIE_MOAN), 3.0F);
               } else {
                  this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_ELLIE_AHH), 3.0F);
               }

               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.02);
               }
               break;
            case "missionary_fastMSG1":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING));
               if (!this.func_70681_au().nextBoolean() && !this.func_70681_au().nextBoolean()) {
                  this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_ELLIE_AHH), 3.0F);
               } else {
                  this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_ELLIE_MOAN), 3.0F);
               }

               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.05);
               }
               break;
            case "missionary_fastDone":
               if (this.n_clash537()) {
                  if (d3.d) {
                     this.b(fp.MISSIONARY_FAST);
                  } else {
                     this.b(fp.MISSIONARY_SLOW);
                  }
               }
               break;
            case "bedRustle":
               this.a(SoundHandler.a_clash804(SoundHandler.MISC_POUNDING));
               this.a(SoundHandler.MISC_BEDRUSTLE[0]);
               break;
            case "bedRustle1":
               this.a(SoundHandler.MISC_BEDRUSTLE[1]);
               break;
            case "missionary_cumMSG1":
               this.a(SoundHandler.a_clash804(SoundHandler.GIRLS_ELLIE_AHH), 3.0F);
               break;
            case "carry_introMSG1":
               this.a_clash541("I'm hungry..");
               this.a(SoundHandler.GIRLS_ELLIE_HMPH, 6.0F);
               break;
            case "carry_introMSG2":
               this.a_clash541("heh~");
               this.a(SoundHandler.GIRLS_ELLIE_GIGGLE[3], 6.0F);
               break;
            case "lipsound":
               this.a_clash588(SoundHandler.GIRLS_ALLIE_LIPSOUND);
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.02);
               }
               break;
            case "cum":
               this.a(SoundHandler.MISC_INSERTS, 6.0F);
               this.a_clash588(SoundHandler.MISC_POUNDING);
               break;
            case "pound":
               this.a_clash588(SoundHandler.MISC_POUNDING);
               if (this.n_clash537()) {
                  HornyMeterHud.a_clash362(0.04);
               }
               break;
            case "carry_slowDone":
               int var5 = this.ap;

               do {
                  this.ap = this.func_70681_au().nextInt(4) + 1;
               } while (this.ap == var5);

               return;
            case "carry_fastDone":
               if (this.n_clash537() && !d3.d) {
                  this.b(fp.CARRY_SLOW);
               }
               break;
            case "sexUI":
               if (this.n_clash537()) {
                  HornyMeterHud.d_clash358();
               }
         }
      };
      this.C.registerSoundListener(var2);
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.E);
      var1.addAnimationController(this.s);
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
