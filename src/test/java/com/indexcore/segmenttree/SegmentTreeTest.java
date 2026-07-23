package com.indexcore.segmenttree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SegmentTreeTest {

    private SegmentTree tree;

    @BeforeEach
    void setUp() {
        // Stock levels: [5, 3, 8, 2, 0, 7]
        tree = new SegmentTree(new long[]{5, 3, 8, 2, 0, 7});
    }

    @Nested
    class Construction {

        @Test
        void nullValuesThrows() {
            assertThrows(IllegalArgumentException.class, () -> new SegmentTree(null));
        }

        @Test
        void emptyValuesThrows() {
            assertThrows(IllegalArgumentException.class, () -> new SegmentTree(new long[0]));
        }

        @Test
        void singleElementTreeWorks() {
            SegmentTree single = new SegmentTree(new long[]{42});
            assertEquals(42, single.rangeMin(0, 0));
        }
    }

    @Nested
    class RangeMinQueries {

        @Test
        void singleIndexRangeReturnsThatValue() {
            assertEquals(8, tree.rangeMin(2, 2));
        }

        @Test
        void findsMinimumAcrossRange() {
            // slots 2..4: 8, 2, 0 -> min is 0
            assertEquals(0, tree.rangeMin(2, 4));
        }

        @Test
        void fullRangeReturnsGlobalMinimum() {
            assertEquals(0, tree.rangeMin(0, 5));
        }

        @Test
        void rangeExcludingTheMinimumSlotSkipsIt() {
            // exclude index 4 (the 0) -> min of [5,3,8,2] is 2
            assertEquals(2, tree.rangeMin(0, 3));
        }

        @Test
        void fromGreaterThanToThrows() {
            assertThrows(IllegalArgumentException.class, () -> tree.rangeMin(3, 1));
        }

        @Test
        void outOfBoundsIndexThrows() {
            assertThrows(IndexOutOfBoundsException.class, () -> tree.rangeMin(0, 6));
            assertThrows(IndexOutOfBoundsException.class, () -> tree.rangeMin(-1, 2));
        }
    }

    @Nested
    class PointUpdates {

        @Test
        void updateChangesGetValue() {
            tree.update(4, 99);
            assertEquals(99, tree.get(4));
        }

        @Test
        void updatePropagatesToRangeMinQueries() {
            // restock slot 4 (was the 0, driving the global min) to 50
            tree.update(4, 50);
            assertEquals(2, tree.rangeMin(0, 5)); // new global min is now index 3's value, 2
        }

        @Test
        void loweringAValueCanCreateNewMinimum() {
            tree.update(0, -10);
            assertEquals(-10, tree.rangeMin(0, 5));
        }
    }
}
