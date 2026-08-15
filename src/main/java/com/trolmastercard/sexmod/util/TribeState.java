package com.trolmastercard.sexmod.util;
/**
 * <b>Role.</b> Tribe activity state: {@code REST} (idle) vs {@code ACTIVE}
 * (working/fighting). Stored on {@link KoboldManager.Tribe} and consumed by the
 * kobold AI and the tribe UI.
 */
public enum TribeState {
   ACTIVE,
   REST;
}
