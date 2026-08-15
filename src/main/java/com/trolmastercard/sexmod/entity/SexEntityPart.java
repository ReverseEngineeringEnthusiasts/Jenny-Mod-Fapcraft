package com.trolmastercard.sexmod.entity;

import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.world.World;

/**
 * <b>Role.</b> A multi-part hitbox part for multi-part entities — used by
 * {@link GalathEntity} for its two energy-ball hitboxes. Collidable only
 * while {@link #isActive} is set, so the hitboxes exist exactly during the
 * summon charge window.
 */
public class SexEntityPart extends MultiPartEntityPart {
   public boolean isActive = false;

   public SexEntityPart(World var1) {
      super(null, "", 0.0F, 0.0F);
   }

   public SexEntityPart(IEntityMultiPart var1, String var2, float var3, float var4) {
      super(var1, var2, var3, var4);
   }

   public boolean canBeCollidedWith() {
      return this.isActive;
   }
}
