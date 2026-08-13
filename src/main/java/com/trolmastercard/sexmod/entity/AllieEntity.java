package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.networking.KoboldStatePacket;
import com.trolmastercard.sexmod.networking.MakeRichWishPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UploadInventoryToServerPacket2;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.cj;
import com.trolmastercard.sexmod.util.d3;







import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class AllieEntity extends BaseGirlEntity {
   public static final int Q = 300;
   public static final int K = 8;
   public static final Vec3d O = new Vec3d(0.5, 1.0, 0.0);
   public float U = 1.0F;
   public boolean P = false;
   public static final DataParameter<ItemStack> N = EntityDataManager.func_187226_a(AllieEntity.class, DataSerializers.field_187196_f)
      .func_187156_b()
      .func_187161_a(111);
   boolean S = true;
   int T = 1;
   int L = 1;
   boolean M = false;
   boolean R = false;

   public AllieEntity(World var1) {
      super(var1);
      this.func_70105_a((float)O.field_72450_a, (float)O.field_72448_b);
   }

   public AllieEntity(World var1, ItemStack var2) {
      this(var1);
      this.m.func_187227_b(N, var2);
   }

   @Override
   public String getDisplayNameText() {
      return "Allie";
   }

   @Override
   public float i_clash226() {
      return 1.0F;
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      this.m.func_187214_a(N, ItemStack.field_190927_a);
   }

   public boolean f_clash697() {
      NBTTagCompound var1 = ((ItemStack)this.m.func_187225_a(N)).func_77978_p();
      return var1 == null ? true : var1.func_74762_e("sexmodUses") == 1;
   }

   @Override
   public void func_70619_bc() {
      super.func_70619_bc();
      if (this.getCurrentAction() == fp.NULL) {
         this.field_70170_p.func_72900_e(this);
      }

      UUID var1 = this.getInteractionPlayerUUID();
      if (var1 != null) {
         EntityPlayer var2 = this.field_70170_p.func_152378_a(var1);
         if (var2 == null) {
            this.field_70170_p.func_72900_e(this);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void ac() {
      if (!this.R) {
         this.P = true;
      }
   }

   @Override
   public void func_70071_h_() {
      super.func_70071_h_();
      if (this.U != 1.0F && this.U != -69.0F && this.U <= 0.0F) {
         if (this.isControlledByLocalPlayer()) {
            PacketHandler.b.sendToServer(new UploadInventoryToServerPacket2(this.getGirlId()));
            d3.setMovementLock(true);
         }

         this.U = -69.0F;
      }

      if (this.field_70170_p.field_72995_K) {
         if (this.P) {
            this.c_clash700();
         }

         if (this.S) {
            this.d_clash699();
         }

         this.b_clash698();
      }
   }

   void b_clash698() {
      if (this.field_70173_aa % 10 == 0) {
         int var1 = this.func_70681_au().nextInt(8);
         Vec3d var2 = this.getCachedBoneOffset("tail" + var1).func_178787_e(this.func_174791_d());
         this.field_70170_p
            .func_175688_a(
               EnumParticleTypes.PORTAL,
               var2.field_72450_a,
               var2.field_72448_b,
               var2.field_72449_c,
               this.func_70681_au().nextGaussian() * 0.01F,
               this.func_70681_au().nextGaussian() * 0.01F,
               this.func_70681_au().nextGaussian() * 0.01F,
               new int[0]
            );
      }
   }

   @SideOnly(Side.CLIENT)
   void d_clash699() {
      this.S = false;
      cj.a(this.field_70170_p, EnumParticleTypes.PORTAL, this.func_174791_d(), 300, 0.75, 1.5);
   }

   @SideOnly(Side.CLIENT)
   void c_clash700() {
      this.openInteractionMenu(Minecraft.func_71410_x().field_71439_g);
      this.P = false;
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      this.R = false;
      String[] var2 = new String[]{"action.names.makemerichallie", "action.names.deepthroat", "Reverse cowgirl"};
      a(var1, this, var2, false);
      return true;
   }

   @Override
   protected fp getNextAction(fp var1) {
      if (var1 == fp.DEEPTHROAT_SLOW) {
         return fp.DEEPTHROAT_FAST;
      } else {
         return var1 == fp.REVERSE_COWGIRL_SLOW ? fp.REVERSE_COWGIRL_FAST_START : null;
      }
   }

   @Override
   protected fp getCumAction(fp var1) {
      if (var1 == fp.DEEPTHROAT_FAST || var1 == fp.DEEPTHROAT_SLOW) {
         return fp.DEEPTHROAT_CUM;
      } else {
         return var1 != fp.REVERSE_COWGIRL_SLOW && var1 != fp.REVERSE_COWGIRL_FAST_START && var1 != fp.REVERSE_COWGIRL_FAST_CONTINUES
            ? null
            : fp.REVERSE_COWGIRL_CUM;
      }
   }

   @Override
   public void b(fp var1) {
      if (this.getCurrentAction() != fp.DEEPTHROAT_CUM || var1 != fp.DEEPTHROAT_FAST && var1 != fp.DEEPTHROAT_SLOW) {
         if (this.getCurrentAction() != fp.REVERSE_COWGIRL_CUM
            || var1 != fp.REVERSE_COWGIRL_SLOW && var1 != fp.REVERSE_COWGIRL_FAST_START && var1 != fp.REVERSE_COWGIRL_FAST_CONTINUES) {
            if (!this.field_70170_p.field_72995_K && var1 == fp.REVERSE_COWGIRL_START) {
               this.a_clash701();
            }

            super.b(var1);
         }
      }
   }

   void a_clash701() {
      EntityPlayer var1 = this.S_clash495();
      if (var1 != null) {
         Vec3d var2 = this.getTargetPosition();
         var1.func_70634_a(var2.field_72450_a, var2.field_72448_b, var2.field_72449_c);
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      if (this.field_70170_p instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != fp.NULL || !this.getCurrentAction().autoBlink) {
               this.a("animation.allie.null", true, var1);
            }
            break;
         case "movement":
            this.a("animation.allie.tail", true, var1);
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case SUMMON:
                  this.a("animation.allie.summon", false, var1);
                  break;
               case SUMMON_NORMAL:
                  this.a("animation.allie.summon_normal", false, var1);
                  break;
               case SUMMON_NORMAL_WAIT:
                  this.a("animation.allie.summon_normal_wait", true, var1);
                  break;
               case SUMMON_WAIT:
                  this.a("animation.allie.summon_wait", true, var1);
                  break;
               case ALLIE_PREPARE_FIRST_TIME:
                  this.a("animation.allie.deepthroat_prepare", false, var1);
                  break;
               case ALLIE_PREPARE_NORMAL:
                  this.a("animation.allie.deepthroat_normal_prepare", false, var1);
                  break;
               case DEEPTHROAT_START:
                  this.a("animation.allie.deepthroat_start", false, var1);
                  break;
               case DEEPTHROAT_SLOW:
                  this.a("animation.allie.deepthroat_slow", true, var1);
                  break;
               case DEEPTHROAT_FAST:
                  this.a("animation.allie.deepthroat_fast", true, var1);
                  break;
               case DEEPTHROAT_CUM:
                  this.a("animation.allie.deepthroat_cum", false, var1);
                  break;
               case RICH_FIRST_TIME:
                  this.a("animation.allie.rich", false, var1);
                  break;
               case RICH_NORMAL:
                  this.a("animation.allie.rich_normal", false, var1);
                  break;
               case SUMMON_SAND:
                  this.a("animation.allie.summon_sand", false, var1);
                  break;
               case REVERSE_COWGIRL_START:
                  this.a("animation.allie.reverse_cowgirl_start", true, var1);
                  break;
               case REVERSE_COWGIRL_SLOW:
                  this.a("animation.allie.reverse_cowgirl_slow" + this.T, true, var1);
                  break;
               case REVERSE_COWGIRL_FAST_CONTINUES:
                  this.a("animation.allie.reverse_cowgirl_fastc" + this.L, true, var1);
                  break;
               case REVERSE_COWGIRL_FAST_START:
                  this.a("animation.allie.reverse_cowgirl_fasts", true, var1);
                  break;
               case REVERSE_COWGIRL_CUM:
                  this.a("animation.allie.reverse_cowgirl_cum", true, var1);
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
            case "summonMSG1":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.summon1", new Object[0]));
               this.a(SoundHandler.GIRLS_ALLIE_SCAWY[0], 0.5F);
               break;
            case "summonMSG2":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.summon2", new Object[0]));
               this.a(SoundHandler.GIRLS_ALLIE_GIGGLE[this.func_70681_au().nextInt(4)]);
               break;
            case "summonMSG3":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.summon3", new Object[0]));
               break;
            case "summonMSG4":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.summon4", new Object[0]));
               this.a(SoundHandler.GIRLS_ALLIE_LIGHTBREATHING[2]);
               break;
            case "summonMSG5":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.summon5", new Object[0]));
               this.a(SoundHandler.GIRLS_ALLIE_HMPH[4]);
               break;
            case "summonMSG6":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.summon6", new Object[0]));
               this.a(SoundHandler.GIRLS_ALLIE_GIGGLE[3]);
               break;
            case "summonMSG7":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.summon7", new Object[0]));
               break;
            case "summonMSG8":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.summon8", new Object[0]));
               this.a(SoundHandler.GIRLS_ALLIE_HUH);
               if (this.isControlledByLocalPlayer()) {
                  this.openInteractionMenu(this.field_70170_p.func_152378_a(this.getInteractionPlayerUUID()));
               }
               break;
            case "summonDone":
               this.b(fp.SUMMON_WAIT);
               break;
            case "deepthroat_prepareMSG1":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.hihi", new Object[0]));
               this.a(SoundHandler.GIRLS_ALLIE_GIGGLE);
               break;
            case "deepthroat_prepareMSG2":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.boys", new Object[0]));
               this.a(SoundHandler.GIRLS_ALLIE_SIGH[0]);
               break;
            case "scream":
               this.a(SoundHandler.MISC_SCREAM);
               break;
            case "blackscreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "deepthroat_prepareDone":
               if (this.isControlledByLocalPlayer()) {
                  if ("reverse_cowgirl".equals(this.m.func_187225_a(h))) {
                     this.field_70125_A = 30.0F;
                     this.b(fp.REVERSE_COWGIRL_START);
                  } else {
                     this.b(fp.DEEPTHROAT_START);
                     PacketHandler.b.sendToServer(new KoboldStatePacket(this.getGirlId(), this.getInteractionPlayerUUID(), false, true));
                     this.r = this.field_70177_z + 180.0F;
                     this.positionPlayerRelative(0.0, 0.0, 1.35F, 0.0F, 30.0F);
                     HornyMeterHud.resetHornyMeter();
                  }
               }
               break;
            case "deepthroat_fastDone":
               if (this.isControlledByLocalPlayer() && !d3.d) {
                  this.b(fp.DEEPTHROAT_SLOW);
               }
               break;
            case "deepthroat_startDone":
               this.b(fp.DEEPTHROAT_SLOW);
               break;
            case "deepthroat_fastMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_BJMOAN));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "deepthroat_slowMSG1":
               if (this.func_70681_au().nextFloat() > 0.33F) {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND));
               } else {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_BJMOAN));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "deepthroat_cumMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_MOAN));
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND));
               this.a(SoundHandler.randomSound(SoundHandler.MISC_CUMINFLATION), 1.5F);
               break;
            case "cowgirl_cumDone":
            case "deepthroat_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.r_clash533();
                  PacketHandler.b.sendToServer(new UploadInventoryToServerPacket2(this.getGirlId()));
               }
               break;
            case "summon_normalMSG1":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.sup", new Object[0]));
               this.a(SoundHandler.GIRLS_ALLIE_GIGGLE[this.func_70681_au().nextInt(4)]);
               break;
            case "summon_normalMSG2":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.youhave", new Object[0]));
               break;
            case "summon_normalMSG3":
               if (((ItemStack)this.m.func_187225_a(N)).func_77978_p().func_74762_e("sexmodUses") == 2) {
                  this.sendChatMessage(I18n.func_135052_a("allie.dialogue.2wishes", new Object[0]));
               } else {
                  this.sendChatMessage(I18n.func_135052_a("allie.dialogue.1wish", new Object[0]));
               }

               this.a(SoundHandler.GIRLS_ALLIE_HMPH[4]);
               break;
            case "summon_normalMSG4":
               this.sendChatMessage("So...");
               break;
            case "summon_normalMSG5":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.tellme", new Object[0]));
               this.a(SoundHandler.GIRLS_ALLIE_HUH);
               break;
            case "summon_normalDone":
               this.b(fp.SUMMON_NORMAL_WAIT);
               if (this.isControlledByLocalPlayer()) {
                  this.openInteractionMenu(Minecraft.func_71410_x().field_71439_g);
               }
               break;
            case "deepthroat_normal_prepareMSG1":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.alright", new Object[0]));
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_GIGGLE));
               break;
            case "rich_MSG1":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.wishgranted", new Object[0]));
               this.a(SoundHandler.randomSound(SoundHandler.MISC_PLOB));
               if (this.isControlledByLocalPlayer()) {
                  PacketHandler.b.sendToServer(new MakeRichWishPacket(this.func_174791_d()));
               }
               break;
            case "disappear":
               this.U = 0.99F;
               break;
            case "summon_sandMSG1":
               this.sendChatMessage(I18n.func_135052_a("allie.dialogue.nooo", new Object[0]));
               this.a(SoundHandler.GIRLS_ALLIE_SCAWY[2]);
               break;
            case "summon_sandMSG2":
               if (this.isLocalPlayerNearby()) {
                  this.b(I18n.func_135052_a("allie.dialogue.phobia", new Object[0]), true);
               }
               break;
            case "giggle":
               this.a(SoundHandler.GIRLS_ALLIE_GIGGLE);
               break;
            case "pounding":
               this.a(SoundHandler.MISC_POUNDING);
               break;
            case "moan":
               this.a(SoundHandler.GIRLS_ALLIE_MOAN);
               break;
            case "mmm":
               this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_MMM));
               break;
            case "slide":
               this.a(SoundHandler.MISC_SLIDE, 0, 1, 4, 6);
               break;
            case "slowMoan":
               if (this.func_70681_au().nextBoolean()) {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_AHH));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "cowgirlSlowDone":
               int var6 = this.T;

               do {
                  this.T = this.func_70681_au().nextInt(3) + 1;
               } while (this.T == var6);

               return;
            case "fastMoan":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (!this.M) {
                  this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_MOAN));
                  this.M = true;
               } else {
                  this.M = false;
               }
               break;
            case "fastSwitch":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  fp var5 = this.getCurrentAction();
                  if (var5 == fp.REVERSE_COWGIRL_FAST_START) {
                     this.b(fp.REVERSE_COWGIRL_FAST_CONTINUES);
                  } else {
                     this.N();
                     int var4 = this.L;

                     do {
                        this.L = this.func_70681_au().nextInt(3) + 1;
                     } while (this.L == var4);
                  }
               }
               break;
            case "openSexUi":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "cum":
               this.a(SoundHandler.MISC_INSERTS, 6.0F);
               break;
            case "aftermoan":
               this.a(SoundHandler.GIRLS_ALLIE_AFTERSESSIONMOAN);
         }
      };
      this.C.registerSoundListener(var2);
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.E);
      var1.addAnimationController(this.s);
   }

   @Override
   public void a(String var1, UUID var2) {
      this.R = true;
      if ("action.names.makemerichallie".equals(var1)) {
         this.b(this.f_clash697() ? fp.RICH_FIRST_TIME : fp.RICH_NORMAL);
      } else {
         this.changeDataParameterFromClient("animationFollowUp", "action.names.deepthroat".equals(var1) ? "deepthroat" : "reverse_cowgirl");
         this.b(this.f_clash697() ? fp.ALLIE_PREPARE_FIRST_TIME : fp.ALLIE_PREPARE_NORMAL);
      }
   }

}
