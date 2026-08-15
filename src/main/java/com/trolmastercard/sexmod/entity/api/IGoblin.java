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

   void setOwnerUUID(UUID var1);

   int getHeldPlayerDistance();

   void setThrowProgress(int var1);

   int getThrowProgress();

   void setThrowTickCount(int var1);

   int getThrowTickCount();

   void setPreviousAction(Action var1);

   Action getPreviousAction();

   void setHeldPlayerDistance(int var1);
}
