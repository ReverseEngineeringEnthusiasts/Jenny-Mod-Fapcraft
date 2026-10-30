package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * <b>Role.</b> Base class for the player-form kobold/goblin transformations
 * ({@link KoboldPlayerEntity}, {@link GoblinPlayerEntity}). Manages the
 * custom-model "DNA" string (the {@code APPEARANCE_DNA} twin of
 * {@link AbstractNpcOnlyEntity}) plus the three shared player-specific keys.
 * <p>
 * <b>State.</b> Data-manager keys with EXPLICIT ids — do not reorder or
 * re-id: {@code SKIN_COLOR_NAME} (119) = skin/body color name, {@code EYE_COLOR} (120) = eye
 * color as {@link BlockPos}, {@code MODEL_CODE} (121) = model-code DNA string.
 * The DNA string is only registered on the server or on non-{@link SexWorldClient}
 * worlds; the client twin is synced via {@code syncModelCodeClient()}.
 * <p>
 * <b>Pitfalls.</b> The 119/120/121 ids are shared with
 * {@link AbstractNpcOnlyEntity#CURRENT_ACTION}/{@code ACTION_TARGET_POS}/
 * {@code APPEARANCE_DNA} — both classes are never instantiated as the same
 * entity, but any id change here MUST be mirrored there. {@code syncModelCodeClient}
 * clears bone colors whenever the synced triplet changes, so the renderer
 * cache never shows stale skin/eye colors after a wardrobe edit.
 */
public abstract class AbstractKoboldPlayerEntity extends AbstractPlayerGirlEntity {
   public static final DataParameter<String> SKIN_COLOR_NAME = EntityDataManager.createKey(AbstractKoboldPlayerEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(119);
   public static final DataParameter<BlockPos> EYE_COLOR = EntityDataManager.createKey(AbstractKoboldPlayerEntity.class, DataSerializers.BLOCK_POS)
      .getSerializer()
      .createKey(120);
   public static final DataParameter<String> MODEL_CODE = EntityDataManager.createKey(AbstractKoboldPlayerEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(121);
   boolean customPartsPending = true;
   String lastSkinColor = null;
   String lastModelCode = null;
   BlockPos lastEyeColor = null;

   protected AbstractKoboldPlayerEntity(World world) {
      super(world);
   }

   protected AbstractKoboldPlayerEntity(World world, UUID uuid) {
      super(world, uuid);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      if (!this.world.isRemote || !(this.world instanceof SexWorldClient)) {
         this.entityDataManager.register(MODEL_CODE, this.buildModelCodeDNA(new StringBuilder()));
      }
   }

   abstract String buildModelCodeDNA(StringBuilder builder);

   public static String[] getModelCodeParts(BaseGirlEntity girl) {
      return ((String)girl.getDataManager().get(MODEL_CODE)).split("-");
   }

   /**
    * CLIENT/SERVER: rebuilds the owner-bound custom part list once, when the
    * first update arrives. On the server the part list is loaded from the
    * owning player's persistent data ({@code "sexmod:GirlSpecific" + npc type});
    * on the client the one-shot flag just gates {@code clearBoneColors()}.
    */
   @Override
   public void onUpdate() {
      super.onUpdate();
      this.syncModelCodeClient();
      if (this.customPartsPending) {
         if (this.world.isRemote) {
            this.clearBoneColors();
            this.customPartsPending = true;
         } else {
            EntityPlayer player = this.getOwnerPlayer();
            if (player != null) {
               String modelCode = player.getEntityData().getString("sexmod:GirlSpecific" + NpcType.getNpcType(this));
               this.customPartsPending = false;
               if (!"".equals(modelCode)) {
                  this.setCustomPartList(decodePartIdList(modelCode));
               }
            }
         }
      }
   }

   /**
    * CLIENT-side: watches the synced skin/eye/DNA triplet and clears the
    * renderer's bone-color cache whenever any of the three changed, then
    * caches the new values. Keeps the rendered kobold in sync with server
    * wardrobe edits.
    */
   void syncModelCodeClient() {
      if (this.world.isRemote) {
         String modelCode = (String)this.entityDataManager.get(SKIN_COLOR_NAME);
         String boneColors = (String)this.entityDataManager.get(MODEL_CODE);
         BlockPos heldPos = (BlockPos)this.entityDataManager.get(EYE_COLOR);
         if (this.lastSkinColor == null) {
            this.lastSkinColor = modelCode;
            this.lastModelCode = boneColors;
            this.lastEyeColor = heldPos;
         } else {
            if (!this.lastModelCode.equals(boneColors) || !this.lastSkinColor.equals(modelCode) || !this.lastEyeColor.equals(heldPos)) {
               this.clearBoneColors();
            }

            this.lastSkinColor = modelCode;
            this.lastModelCode = boneColors;
            this.lastEyeColor = heldPos;
         }
      }
   }

   protected abstract void clearBoneColors();

}
