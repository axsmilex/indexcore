package com.indexcore.segmenttree;

/**
 * A Fenwick Tree (Binary Indexed Tree) supporting point updates and prefix/
 * range sum queries in O(log n). Used here to track inventory stock levels
 * across a linear category index -- e.g., "total stock across category slots
 * 3 through 7" -- with updates as fast as the queries whenever stock changes
 * (a naive array would need O(n) per range query; recomputing from scratch
 * on every sale isn't viable at scale).
 *
 * Indexing: public API is 0-indexed for caller convenience; internally
 * shifted to the 1-indexed representation the BIT algorithm requires.
 */
public class FenwickTree {

    private final long[] tree; // 1-indexed internally
    private final int size;

    public FenwickTree(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        this.size = size;
        this.tree = new long[size + 1];
    }

    /** Adds delta to the value at index (0-indexed). Delta may be negative (e.g., stock sold). */
    public void update(int index, long delta) {
        checkIndex(index);
        int i = index + 1;
        while (i <= size) {
            tree[i] += delta;
            i += i & (-i);
        }
    }

    /** Sum of values in [0, index] inclusive (0-indexed). */
    public long prefixSum(int index) {
        checkIndex(index);
        int i = index + 1;
        long sum = 0;
        while (i > 0) {
            sum += tree[i];
            i -= i & (-i);
        }
        return sum;
    }

    /** Sum of values in [from, to] inclusive (0-indexed, both bounds included). */
    public long rangeSum(int from, int to) {
        checkIndex(from);
        checkIndex(to);
        if (from > to) {
            throw new IllegalArgumentException("from must be <= to");
        }
        long sumTo = prefixSum(to);
        long sumBeforeFrom = from == 0 ? 0 : prefixSum(from - 1);
        return sumTo - sumBeforeFrom;
    }

    public int size() {
        return size;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + " out of range [0, " + size + ")");
        }
    }
}
