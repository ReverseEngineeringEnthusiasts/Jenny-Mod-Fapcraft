package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.api.SkinColor;
import com.trolmastercard.sexmod.entity.GalathEntity;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

/**
 * DamageSource for galath attacks.
 */
public class GalathDamageSource extends DamageSource {
   GalathEntity galathEntity;
   Vec3d sourcePos;

   public GalathDamageSource(GalathEntity galath) {
      super("galath");
      this.galathEntity = galath;
      this.sourcePos = galath.getPositionVector();
   }

   public ITextComponent getDeathMessage(EntityLivingBase victim) {
      return new TextComponentString(victim.getName() + " was slain by Galath");
   }

   @Nullable
   public Entity getImmediateSource() {
      return this.galathEntity;
   }

   @Nullable
   public Entity getTrueSource() {
      return this.galathEntity;
   }

   @Nullable
   public Vec3d getDamageLocation() {
      return this.sourcePos;
   }
}
