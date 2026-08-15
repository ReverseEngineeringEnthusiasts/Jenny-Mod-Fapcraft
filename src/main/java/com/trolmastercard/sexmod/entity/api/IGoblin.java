package com.trolmastercard.sexmod.entity.api;

import com.trolmastercard.sexmod.entity.Action;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * <b>Role.</b> The goblin carry/throw contract shared by {@link GoblinEntity}
 * and {@link GoblinPlayerEntity}: owner binding, the throw flight state
 * ({@code throwProgress} -1 idle, 0..39 in flight, {@code throwTickCount}
 * grounded roll ticks, {@code heldPlayerDistance} pickup countdown) and the
 * previous-action diff used to detect a fresh throw. The shared static
 * helpers in {@link GoblinEntity} (e.g. {@code handlePickUpState},
 * {@code handleGoblinThrowAction}, {@code getGoblinThrowPos}) operate on this
 * interface — do not remove any member.
 */
public interface IGoblin {
   @Nullable
   UUID getOwnerUUID();

   void setOwnerUUID(UUID ownerUuid);

   int getHeldPlayerDistance();

   void setThrowProgress(int throwProgress);

   int getThrowProgress();

   void setThrowTickCount(int throwTickCount);

   int getThrowTickCount();

   void setPreviousAction(Action previousAction);

   Action getPreviousAction();

   void setHeldPlayerDistance(int heldPlayerDistance);
}
