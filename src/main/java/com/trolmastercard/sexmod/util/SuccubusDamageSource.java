package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.api.by;
import com.trolmastercard.sexmod.entity.GalathEntity;







import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class SuccubusDamageSource extends DamageSource {
   GalathEntity a;
   Vec3d b;

   public SuccubusDamageSource(GalathEntity var1) {
      super("galath");
      this.a = var1;
      this.b = var1.func_174791_d();
   }

   public ITextComponent func_151519_b(EntityLivingBase var1) {
      return new TextComponentString(var1.func_70005_c_() + " got his cum drained by a Succubus");
   }

   public boolean func_76363_c() {
      return true;
   }

   public boolean func_76357_e() {
      return true;
   }

   @Nullable
   public Entity func_76364_f() {
      return this.a;
   }

   @Nullable
   public Entity func_76346_g() {
      return this.a;
   }

   @Nullable
   public Vec3d func_188404_v() {
      return this.b;
   }
}
