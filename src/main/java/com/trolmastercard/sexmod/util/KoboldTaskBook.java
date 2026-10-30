package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.KoboldEntity;
import java.util.Collection;
import net.minecraft.util.math.BlockPos;

/**
 * <b>Role.</b> Read-side task lookups for {@link KoboldManager}: which task owns
 * a mined block, which task a kobold is working on, and handing every task of a
 * tribe its new worker.
 * <p>
 * <b>Pitfalls.</b> The worker lookup keeps the *last* matching task, the block
 * lookup stops at the first — a kobold assigned to two tasks therefore loses the
 * earlier one. A block can legitimately appear in several tasks; only the first
 * is returned.
 */
final class KoboldTaskBook {
   private KoboldTaskBook() {
   }

   static KoboldTask findTaskWithTarget(Collection<KoboldTask> tasks, BlockPos pos) {
      KoboldTask found = null;

      for (KoboldTask task : tasks) {
         if (task.miningTargets.contains(pos)) {
            found = task;
            break;
         }
      }

      return found;
   }

   static KoboldTask findTaskWithWorker(Collection<KoboldTask> tasks, KoboldEntity kobold) {
      KoboldTask found = null;

      for (KoboldTask task : tasks) {
         if (task.hasWorker(kobold)) {
            found = task;
         }
      }

      return found;
   }

   static void addWorkerToAll(Collection<KoboldTask> tasks, KoboldEntity kobold) {
      for (KoboldTask task : tasks) {
         task.addWorker(kobold);
      }
   }
}
