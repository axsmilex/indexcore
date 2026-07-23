package com.indexcore.segmenttree;

/**
 * A Segment Tree supporting range-minimum queries and point updates in
 * O(log n). Complements FenwickTree (which handles range-sum): here you'd
 * ask "what's the lowest stock level across category slots 3-7" to flag a
 * restock alert, which a Fenwick tree cannot answer since min isn't
 * invertible the way sum is.
 *
 * Built as an iterative array-backed tree (2n array), which is faster and
 * simpler than a pointer-based recursive tree for this fixed-size use case.
 */
public class SegmentTree {

    private final int n;
    private final long[] tree;
    private static final long IDENTITY = Long.MAX_VALUE; // identity for min

    public SegmentTree(long[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values must be non-null and non-empty");
        }
        this.n = values.length;
        this.tree = new long[2 * n];
        buildFrom(values);
    }

    private void buildFrom(long[] values) {
        System.arraycopy(values, 0, tree, n, n);
        for (int i = n - 1; i > 0; i--) {
            tree[i] = Math.min(tree[2 * i], tree[2 * i + 1]);
        }
    }

    /** Sets the value at index (0-indexed) and updates ancestors. */
    public void update(int index, long value) {
        checkIndex(index);
        int i = index + n;
        tree[i] = value;
        while (i > 1) {
            i /= 2;
            tree[i] = Math.min(tree[2 * i], tree[2 * i + 1]);
        }
    }

    /** Minimum value in [from, to] inclusive (0-indexed). */
    public long rangeMin(int from, int to) {
        checkIndex(from);
        checkIndex(to);
        if (from > to) {
            throw new IllegalArgumentException("from must be <= to");
        }
        long result = IDENTITY;
        int lo = from + n;
        int hi = to + n + 1;
        while (lo < hi) {
            if ((lo & 1) == 1) {
                result = Math.min(result, tree[lo]);
                lo++;
            }
            if ((hi & 1) == 1) {
                hi--;
                result = Math.min(result, tree[hi]);
            }
            lo /= 2;
            hi /= 2;
        }
        return result;
    }

    public long get(int index) {
        checkIndex(index);
        return tree[index + n];
    }

    public int size() {
        return n;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= n) {
            throw new IndexOutOfBoundsException("index " + index + " out of range [0, " + n + ")");
        }
    }
}
