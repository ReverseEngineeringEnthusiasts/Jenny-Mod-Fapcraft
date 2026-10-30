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
   private final HashMap<K, V> byKey = new HashMap<>();
   private final HashMap<V, K> byValue = new HashMap<>();

   public void put(K key, V value) {
      Object oldValue = this.byKey.put((K)key, (V)value);
      this.byValue.remove(oldValue);
      this.byValue.put((V)value, (K)key);
   }

   public V getByKey(K key) {
      return this.byKey.get(key);
   }

   public K getByValue(V value) {
      return this.byValue.get(value);
   }

   public int size() {
      return this.byKey.size();
   }

   public void removeByKey(K key) {
      Object removedValue = this.byKey.get(key);
      if (removedValue != null) {
         this.byKey.remove(key);
         this.byValue.remove(removedValue);
      }
   }

   public Set<Entry<K, V>> entrySet() {
      return this.byKey.entrySet();
   }

   public Set<K> keySet() {
      return this.byKey.keySet();
   }

   public Set<V> valueSet() {
      return this.byValue.keySet();
   }

   public void clear() {
      this.byValue.clear();
      this.byKey.clear();
   }

}
