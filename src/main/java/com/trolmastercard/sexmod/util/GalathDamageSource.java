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

public class GalathDamageSource extends DamageSource {
   GalathEntity a;
   Vec3d b;

   public GalathDamageSource(GalathEntity var1) {
      super("galath");
      this.a = var1;
      this.b = var1.getPositionVector();
   }

   public ITextComponent getDeathMessage(EntityLivingBase var1) {
      return new TextComponentString(var1.getName() + " was slain by Galath");
   }

   @Nullable
   public Entity getImmediateSource() {
      return this.a;
   }

   @Nullable
   public Entity getTrueSource() {
      return this.a;
   }

   @Nullable
   public Vec3d getDamageLocation() {
      return this.b;
   }
}
