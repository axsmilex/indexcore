package com.indexcore.bloom;

import java.util.BitSet;

/**
 * A Bloom filter: a space-efficient probabilistic set that answers "definitely
 * not present" or "possibly present" -- never a false negative, but tunable
 * false positives. Used here as a cheap first-pass filter before the more
 * expensive Union-Find dedup logic runs: most non-duplicate listings get
 * rejected in O(k) with no hashmap lookups at all.
 *
 * Hashing strategy: double hashing (Kirsch-Mitzenmacher), which simulates k
 * independent hash functions from just two real ones:
 *   h_i(x) = h1(x) + i * h2(x)   for i in [0, k)
 * This avoids needing k actual hash function implementations while keeping
 * a low, predictable false-positive rate.
 */
public class BloomFilter {

    private final BitSet bits;
    private final int bitSize;
    private final int hashCount;
    private long insertedCount = 0;

    public BloomFilter(int expectedInsertions, double falsePositiveRate) {
        if (expectedInsertions <= 0) {
            throw new IllegalArgumentException("expectedInsertions must be positive");
        }
        if (falsePositiveRate <= 0 || falsePositiveRate >= 1) {
            throw new IllegalArgumentException("falsePositiveRate must be in (0, 1)");
        }
        this.bitSize = optimalBitSize(expectedInsertions, falsePositiveRate);
        this.hashCount = optimalHashCount(bitSize, expectedInsertions);
        this.bits = new BitSet(bitSize);
    }

    /** Adds an element to the filter. */
    public void add(String element) {
        for (int index : hashIndices(element)) {
            bits.set(index);
        }
        insertedCount++;
    }

    /**
     * Returns false if the element is DEFINITELY not in the filter.
     * Returns true if the element is POSSIBLY in the filter (may be a false positive).
     */
    public boolean mightContain(String element) {
        for (int index : hashIndices(element)) {
            if (!bits.get(index)) {
                return false;
            }
        }
        return true;
    }

    private int[] hashIndices(String element) {
        int h1 = smear(element.hashCode());
        int h2 = smear(reverseHash(element));
        int[] indices = new int[hashCount];
        for (int i = 0; i < hashCount; i++) {
            int combined = h1 + i * h2;
            indices[i] = Math.floorMod(combined, bitSize);
        }
        return indices;
    }

    // A second, independent-ish hash derived from the string's characters.
    private int reverseHash(String s) {
        int hash = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            hash = 31 * hash + s.charAt(i);
        }
        return hash;
    }

    // Bit-mixing step (Murmur-style finalizer) to reduce clustering from
    // low-quality hashCode() distributions.
    private int smear(int hash) {
        hash ^= (hash >>> 16);
        hash *= 0x85ebca6b;
        hash ^= (hash >>> 13);
        hash *= 0xc2b2ae35;
        hash ^= (hash >>> 16);
        return hash;
    }

    private static int optimalBitSize(int n, double p) {
        double m = -(n * Math.log(p)) / (Math.log(2) * Math.log(2));
        return Math.max(8, (int) Math.ceil(m));
    }

    private static int optimalHashCount(int m, int n) {
        int k = (int) Math.round((m / (double) n) * Math.log(2));
        return Math.max(1, k);
    }

    public int bitSize() {
        return bitSize;
    }

    public int hashCount() {
        return hashCount;
    }

    public long approximateInsertedCount() {
        return insertedCount;
    }
}
