package com.trolmastercard.sexmod.entity.api;
/**
 * <b>Role.</b> Marker contract for the kobold girls: {@link #isBlockedByCeiling()}
 * tells the AI/pathing whether a ceiling blocks the kobold's head space
 * (used by the kobold NPC tasks — see {@link KoboldTask}).
 */
public interface IKobold {
   boolean isBlockedByCeiling();
}
