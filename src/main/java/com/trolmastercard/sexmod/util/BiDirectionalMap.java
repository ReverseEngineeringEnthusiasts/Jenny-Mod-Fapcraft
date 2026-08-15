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

   public void put(K key, V value) {
      Object oldValue = this.b.put((K)key, (V)value);
      this.a.remove(oldValue);
      this.a.put((V)value, (K)key);
   }

   public V getByKey(K key) {
      return this.b.get(key);
   }

   public K getByValue(V value) {
      return this.a.get(value);
   }

   public int size() {
      return this.b.size();
   }

   public void removeByKey(K key) {
      Object removedValue = this.b.get(key);
      if (removedValue != null) {
         this.b.remove(key);
         this.a.remove(removedValue);
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
