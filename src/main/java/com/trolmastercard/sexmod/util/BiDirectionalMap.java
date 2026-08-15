package com.trolmastercard.sexmod.util;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

/**
 * <b>Role.</b> Minimal two-way map (K->V and V->K) with consistent mutation
 * invariants: every {@link #put} keeps both directions in sync, {@link #removeByKey}
 * removes both entries. Used by {@link GirlSavedData} for player<->galath
 * ownership pairs.
 * <p>
 * <b>Invariant.</b> {@link #put} with an existing value silently drops the old
 * reverse mapping (the value is re-bound to the new key) — callers relying on
 * multi-mapping must not use this class.
 */
public class BiDirectionalMap<K, V> {
   private final HashMap<K, V> b = new HashMap<>();
   private final HashMap<V, K> a = new HashMap<>();

   public void put(K var1, V var2) {
      Object var3 = this.b.put((K)var1, (V)var2);
      this.a.remove(var3);
      this.a.put((V)var2, (K)var1);
   }

   public V getByKey(K var1) {
      return this.b.get(var1);
   }

   public K getByValue(V var1) {
      return this.a.get(var1);
   }

   public int size() {
      return this.b.size();
   }

   public void removeByKey(K var1) {
      Object var2 = this.b.get(var1);
      if (var2 != null) {
         this.b.remove(var1);
         this.a.remove(var2);
      }
   }

   public Set<Entry<K, V>> entrySet() {
      return this.b.entrySet();
   }

   public Set<K> keySet() {
      return this.b.keySet();
   }

   public Set<V> valueSet() {
      return this.a.keySet();
   }

   public void clear() {
      this.a.clear();
      this.b.clear();
   }

}
