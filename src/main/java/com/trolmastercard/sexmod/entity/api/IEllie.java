package com.trolmastercard.sexmod.entity.api;
/**
 * <b>Role.</b> Marker interface for the girls with a dismount/scene-entry
 * hook ({@link EllieEntity}, and implicitly the other NPC girls that react to
 * {@link KoboldStatePacket}). {@link KoboldStatePacket.Handler#sendState}
 * calls {@link #setDismounted()} on the target girl to start the
 * carry/scene-entry flow — do not rename or re-signature this method, the
 * packet handler type-checks against {@link IEllie}.
 */
public interface IEllie {
   void setDismounted();
}
