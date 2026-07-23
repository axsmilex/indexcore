package com.indexcore.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * A fixed-capacity Least-Recently-Used cache, used to hold "hot" search
 * queries or product lookups so repeated requests skip the expensive path
 * (e.g., re-running the recommendation graph).
 *
 * Implementation: a HashMap for O(1) key lookup, backed by a doubly linked
 * list that tracks recency order. Every get/put is O(1) -- this is the
 * classic interview structure, but here it's wired up as an actual cache
 * a search layer would sit in front of.
 *
 * Not thread-safe by design (keeps the implementation honest and simple);
 * callers needing concurrency should wrap access externally.
 */
public class LRUCache<K, V> {

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final Map<K, Node<K, V>> map;
    // Sentinel head/tail avoid null-checking edge cases on every operation.
    private final Node<K, V> head = new Node<>(null, null);
    private final Node<K, V> tail = new Node<>(null, null);

    private long hits = 0;
    private long misses = 0;

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
        this.map = new HashMap<>();
        head.next = tail;
        tail.prev = head;
    }

    /** Returns the value for key, or null if absent. Marks the entry as most-recently-used. */
    public V get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null) {
            misses++;
            return null;
        }
        hits++;
        moveToFront(node);
        return node.value;
    }

    /** Inserts or updates a key. Evicts the least-recently-used entry if over capacity. */
    public void put(K key, V value) {
        Node<K, V> existing = map.get(key);
        if (existing != null) {
            existing.value = value;
            moveToFront(existing);
            return;
        }

        Node<K, V> node = new Node<>(key, value);
        map.put(key, node);
        addToFront(node);

        if (map.size() > capacity) {
            Node<K, V> lru = tail.prev;
            removeNode(lru);
            map.remove(lru.key);
        }
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public int size() {
        return map.size();
    }

    public int capacity() {
        return capacity;
    }

    public double hitRate() {
        long total = hits + misses;
        return total == 0 ? 0.0 : (double) hits / total;
    }

    private void moveToFront(Node<K, V> node) {
        removeNode(node);
        addToFront(node);
    }

    private void addToFront(Node<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
}
