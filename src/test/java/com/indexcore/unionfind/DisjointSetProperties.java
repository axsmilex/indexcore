package com.indexcore.unionfind;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based tests for Union-Find: rather than hand-picking a few merge
 * sequences, these assert structural invariants that must hold no matter
 * what sequence of unions is applied -- exactly the kind of thing that's
 * easy to get subtly wrong in a path-compression implementation.
 */
class DisjointSetProperties {

    @Property
    void everyElementIsAlwaysConnectedToItself(@ForAll @IntRange(min = 0, max = 50) int id) {
        DisjointSet<Integer> ds = new DisjointSet<>();
        ds.makeSet(id);
        assertTrue(ds.connected(id, id));
    }

    @Property
    void unionIsSymmetric(@ForAll @IntRange(min = 0, max = 50) int a, @ForAll @IntRange(min = 0, max = 50) int b) {
        DisjointSet<Integer> ds = new DisjointSet<>();
        ds.makeSet(a);
        ds.makeSet(b);
        ds.union(a, b);
        // connected(a, b) must equal connected(b, a) -- the relation is symmetric by definition
        assertTrue(ds.connected(a, b) == ds.connected(b, a));
    }

    @Property
    void unionOfAChainMakesAllElementsMutuallyConnected(
            @ForAll @Size(min = 2, max = 20) List<@IntRange(min = 0, max = 999) Integer> ids) {
        DisjointSet<Integer> ds = new DisjointSet<>();
        for (int id : ids) {
            ds.makeSet(id);
        }
        // Union each consecutive pair in a chain: ids[0]-ids[1]-ids[2]-...
        for (int i = 0; i < ids.size() - 1; i++) {
            ds.union(ids.get(i), ids.get(i + 1));
        }
        // Transitivity: first and last must now be connected, regardless of chain length
        int first = ids.get(0);
        int last = ids.get(ids.size() - 1);
        assertTrue(ds.connected(first, last));
    }

    @Property
    void componentCountNeverExceedsElementCount(
            @ForAll @Size(min = 1, max = 30) List<@IntRange(min = 0, max = 999) Integer> ids) {
        DisjointSet<Integer> ds = new DisjointSet<>();
        for (int id : ids) {
            ds.makeSet(id);
        }
        long distinctElements = ids.stream().distinct().count();
        assertTrue(ds.componentCount() <= distinctElements);
        assertTrue(ds.componentCount() >= 1);
    }
}
