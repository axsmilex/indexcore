package com.indexcore.segmenttree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FenwickTreeTest {

    private FenwickTree tree;

    @BeforeEach
    void setUp() {
        tree = new FenwickTree(6);
        // Initial stock levels for 6 category slots: [5, 3, 8, 2, 0, 7]
        int[] stock = {5, 3, 8, 2, 0, 7};
        for (int i = 0; i < stock.length; i++) {
            tree.update(i, stock[i]);
        }
    }

    @Nested
    class Construction {

        @Test
        void zeroSizeThrows() {
            assertThrows(IllegalArgumentException.class, () -> new FenwickTree(0));
        }

        @Test
        void negativeSizeThrows() {
            assertThrows(IllegalArgumentException.class, () -> new FenwickTree(-1));
        }
    }

    @Nested
    class PrefixAndRangeSum {

        @Test
        void prefixSumAtFirstIndexEqualsFirstValue() {
            assertEquals(5, tree.prefixSum(0));
        }

        @Test
        void prefixSumAccumulatesCorrectly() {
            assertEquals(5 + 3 + 8, tree.prefixSum(2));
        }

        @Test
        void rangeSumMatchesManualSum() {
            // slots 2..4 inclusive: 8 + 2 + 0 = 10
            assertEquals(10, tree.rangeSum(2, 4));
        }

        @Test
        void rangeSumOverFullArrayEqualsTotal() {
            assertEquals(5 + 3 + 8 + 2 + 0 + 7, tree.rangeSum(0, 5));
        }

        @Test
        void rangeSumSingleElementEqualsThatElement() {
            assertEquals(8, tree.rangeSum(2, 2));
        }

        @Test
        void fromGreaterThanToThrows() {
            assertThrows(IllegalArgumentException.class, () -> tree.rangeSum(4, 2));
        }
    }

    @Nested
    class Updates {

        @Test
        void updateReflectsInSubsequentQueries() {
            tree.update(0, 10); // sell 10 more units at slot 0 (or restock, if negative)
            assertEquals(15, tree.prefixSum(0));
        }

        @Test
        void negativeDeltaDecreasesSum() {
            tree.update(2, -8); // sell out slot 2 entirely
            assertEquals(0, tree.rangeSum(2, 2));
        }

        @Test
        void multipleUpdatesToSameIndexAccumulate() {
            tree.update(1, 1);
            tree.update(1, 1);
            tree.update(1, 1);
            assertEquals(3 + 3, tree.rangeSum(1, 1));
        }
    }

    @Nested
    class BoundsChecking {

        @Test
        void negativeIndexThrows() {
            assertThrows(IndexOutOfBoundsException.class, () -> tree.update(-1, 5));
        }

        @Test
        void indexEqualToSizeThrows() {
            assertThrows(IndexOutOfBoundsException.class, () -> tree.prefixSum(6));
        }
    }
}
