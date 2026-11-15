package com.trolmastercard.sexmod.common.entity.kobold;
/**
 * <b>Role.</b> Tribe activity state: {@code REST} (idle) vs {@code ACTIVE}
 * (working/fighting). Stored on {@link KoboldManager.Tribe} and consumed by the
 * kobold AI and the tribe UI.
 */
public enum TribeState {
   ACTIVE,
   REST;
}
