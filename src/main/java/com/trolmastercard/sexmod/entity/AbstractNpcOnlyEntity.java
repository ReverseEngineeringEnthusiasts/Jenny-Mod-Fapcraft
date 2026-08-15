package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.util.Reference;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * <b>Role.</b> Base class for NPC-only girls that expose their appearance as a
 * packed model-code "DNA" string and a synced action/target pair — currently
 * {@link GoblinEntity}. This is the NPC twin of
 * {@link AbstractKoboldPlayerEntity} (which reuses the same three key ids for
 * the player forms).
 * <p>
 * <b>State.</b> Data-manager keys with EXPLICIT ids — do not reorder or
 * re-id: {@link #CURRENT_ACTION} (119) = skin/body color name,
 * {@link #ACTION_TARGET_POS} (120) = eye color packed as {@link BlockPos},
 * {@link #APPEARANCE_DNA} (121) = the model-code DNA string. The DNA is
 * registered only on the server (or non-{@link SexWorldClient} worlds);
 * {@code tickClientDataCheck()} detects data-manager changes client-side and
 * clears the renderer's bone-color cache.
 * <p>
 * <b>Pitfalls.</b> Ids 119/120/121 are SHARED with
 * {@link AbstractKoboldPlayerEntity#as}/{@code au}/{@code at} — change both
 * classes together or synced wardrobe edits break. The DNA format is a
 * dash-separated, zero-padded number list (see {@link #appendPaddedNumber}),
 * consumed by {@link #getModelCodeParts} and the renderer; {@link GoblinEntity}
 * appends a trailing segment (index 9) that holds the model-part index.
 */
public abstract class AbstractNpcOnlyEntity extends BaseGirlEntity {
   public static final DataParameter<String> CURRENT_ACTION = EntityDataManager.createKey(AbstractNpcOnlyEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(119);
   public static final DataParameter<BlockPos> ACTION_TARGET_POS = EntityDataManager.createKey(AbstractNpcOnlyEntity.class, DataSerializers.BLOCK_POS)
      .getSerializer()
      .createKey(120);
   public static final DataParameter<String> APPEARANCE_DNA = EntityDataManager.createKey(AbstractNpcOnlyEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(121);
   String lastCachedAction = null;
   String lastCachedDNA = null;
   BlockPos lastCachedTargetPos = null;

   protected AbstractNpcOnlyEntity(World world) {
      super(world);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      if (!this.world.isRemote || !(this.world instanceof SexWorldClient)) {
         this.entityDataManager.register(APPEARANCE_DNA, this.buildModelCodeDNA(new StringBuilder()));
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      this.tickClientDataCheck();
   }

   /**
    * CLIENT-side: caches the synced action/DNA/target triplet and clears the
    * renderer's bone-color cache whenever any element changes. Prevents stale
    * colors after skin/eye/wardrobe edits on the server.
    */
   void tickClientDataCheck() {
      if (this.world.isRemote) {
         String action = (String)this.entityDataManager.get(CURRENT_ACTION);
         String dna = (String)this.entityDataManager.get(APPEARANCE_DNA);
         BlockPos targetPos = (BlockPos)this.entityDataManager.get(ACTION_TARGET_POS);
         if (this.lastCachedAction == null) {
            this.lastCachedAction = action;
            this.lastCachedDNA = dna;
            this.lastCachedTargetPos = targetPos;
         } else {
            if (!this.lastCachedDNA.equals(dna) || !this.lastCachedAction.equals(action) || !this.lastCachedTargetPos.equals(targetPos)) {
               this.clearBoneColors();
            }

            this.lastCachedAction = action;
            this.lastCachedDNA = dna;
            this.lastCachedTargetPos = targetPos;
         }
      }
   }

   protected abstract void clearBoneColors();

   abstract String buildModelCodeDNA(StringBuilder builder);

   /**
    * Appends {@code value} as a zero-padded (2-digit) dash-terminated segment
    * of the model-code DNA string. Keeps every DNA segment a fixed width so
    * the string can be split by {@code "-"} and indexed positionally.
    */
   public static void appendPaddedNumber(StringBuilder builder, int number) {
      if (number < 10) {
         builder.append(0);
      }

      builder.append(number);
      builder.append("-");
   }

   /**
    * Appends a uniformly-random segment in {@code [0, bound]} as a padded
    * dash-terminated DNA segment (used for randomization of trait ranges).
    */
   public static void appendRandomSegment(StringBuilder builder, int bound) {
      int digit = Reference.RANDOM.nextInt(bound + 1);
      if (digit < 10) {
         builder.append(0);
      }

      builder.append(digit);
      builder.append("-");
   }

   /**
    * Appends a Gaussian-weighted gene segment: samples a value from a
    * bell-curve over [-2.5, 2.5] and writes its two fractional digits as the
    * DNA segment (drives naturally-distributed trait values like size).
    */
   public static void appendRandomGene(StringBuilder builder) {
      double weight = Reference.RANDOM.nextDouble();
      double geneValue = Math.pow(Math.E, -Math.pow(-2.5 + 5.0 * weight, 2.0));
      String formatted = String.format("%.2f", geneValue);
      String[] parts = formatted.split("\\.");
      if (parts.length < 2) {
         parts = formatted.split(",");
      }

      formatted = parts[1];
      builder.append(formatted).append("-");
   }

   public static void appendPaddedLetter(StringBuilder builder, int bound) {
      int digit = Reference.RANDOM.nextInt(bound);
      if (digit < 10) {
         builder.append(0);
      }

      builder.append(digit);
      builder.append("-");
   }

   public static String[] getModelCodeParts(BaseGirlEntity girl) {
      return ((String)girl.getDataManager().get(APPEARANCE_DNA)).split("-");
   }

}
