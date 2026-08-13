package com.trolmastercard.sexmod.entity.api;

import com.trolmastercard.sexmod.entity.Action;







import java.util.UUID;
import javax.annotation.Nullable;

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
