package com.indexcore.unionfind;

import java.util.HashMap;
import java.util.Map;

/**
 * Union-Find (Disjoint Set Union) with path compression and union by rank,
 * used to cluster near-duplicate product listings (e.g., the same physical
 * item listed by multiple sellers under slightly different titles).
 *
 * Typical pipeline: a cheap similarity check (see BloomFilter for a fast
 * duplicate pre-check) flags candidate pairs, and union() merges them into
 * clusters; find() then answers "which cluster does this listing belong to"
 * in near-constant time.
 *
 * Amortized time complexity: O(alpha(n)) per operation, where alpha is the
 * inverse Ackermann function -- effectively constant for all practical n.
 */
public class DisjointSet<T> {

    private final Map<T, T> parent = new HashMap<>();
    private final Map<T, Integer> rank = new HashMap<>();
    private int componentCount = 0;

    /** Registers a new element as its own singleton set. No-op if already present. */
    public void makeSet(T element) {
        if (!parent.containsKey(element)) {
            parent.put(element, element);
            rank.put(element, 0);
            componentCount++;
        }
    }

    /**
     * Finds the representative (root) of the set containing element, applying
     * path compression along the way.
     *
     * @throws IllegalArgumentException if element was never registered via makeSet
     */
    public T find(T element) {
        requireElement(element);
        T p = parent.get(element);
        if (!p.equals(element)) {
            T root = find(p);
            parent.put(element, root); // path compression
            return root;
        }
        return element;
    }

    /**
     * Merges the sets containing a and b. Returns true if a merge happened,
     * false if they were already in the same set.
     */
    public boolean union(T a, T b) {
        requireElement(a);
        requireElement(b);
        T rootA = find(a);
        T rootB = find(b);
        if (rootA.equals(rootB)) {
            return false;
        }

        int rankA = rank.get(rootA);
        int rankB = rank.get(rootB);
        if (rankA < rankB) {
            parent.put(rootA, rootB);
        } else if (rankA > rankB) {
            parent.put(rootB, rootA);
        } else {
            parent.put(rootB, rootA);
            rank.put(rootA, rankA + 1);
        }
        componentCount--;
        return true;
    }

    public boolean connected(T a, T b) {
        requireElement(a);
        requireElement(b);
        return find(a).equals(find(b));
    }

    public int componentCount() {
        return componentCount;
    }

    public boolean contains(T element) {
        return parent.containsKey(element);
    }

    private void requireElement(T element) {
        if (!parent.containsKey(element)) {
            throw new IllegalArgumentException("element not registered via makeSet(): " + element);
        }
    }
}
