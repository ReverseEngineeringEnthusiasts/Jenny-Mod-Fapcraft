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

public class SuccubusDamageSource extends DamageSource {
   GalathEntity galathEntity;
   Vec3d sourcePos;

   public SuccubusDamageSource(GalathEntity var1) {
      super("galath");
      this.galathEntity = var1;
      this.sourcePos = var1.getPositionVector();
   }

   public ITextComponent getDeathMessage(EntityLivingBase var1) {
      return new TextComponentString(var1.getName() + " got his cum drained by a Succubus");
   }

   public boolean isUnblockable() {
      return true;
   }

   public boolean canHarmInCreative() {
      return true;
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
