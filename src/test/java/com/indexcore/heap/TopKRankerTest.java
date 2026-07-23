package com.indexcore.heap;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TopKRankerTest {

    @Nested
    class Construction {

        @Test
        void zeroKThrows() {
            assertThrows(IllegalArgumentException.class, () -> new TopKRanker<String>(0));
        }

        @Test
        void negativeKThrows() {
            assertThrows(IllegalArgumentException.class, () -> new TopKRanker<String>(-3));
        }
    }

    @Nested
    class BasicRanking {

        @Test
        void keepsHighestScoresWhenOverCapacity() {
            TopKRanker<String> ranker = new TopKRanker<>(2);
            ranker.offer("cheap-item", 1.0);
            ranker.offer("mid-item", 5.0);
            ranker.offer("best-item", 10.0);

            List<TopKRanker.ScoredItem<String>> top = ranker.getTopK();
            assertEquals(2, top.size());
            assertEquals("best-item", top.get(0).item());
            assertEquals("mid-item", top.get(1).item());
        }

        @Test
        void sizeNeverExceedsCapacity() {
            TopKRanker<Integer> ranker = new TopKRanker<>(3);
            for (int i = 0; i < 100; i++) {
                ranker.offer(i, i);
            }
            assertEquals(3, ranker.size());
        }

        @Test
        void resultsAreSortedDescending() {
            TopKRanker<Integer> ranker = new TopKRanker<>(5);
            int[] scores = {3, 1, 4, 1, 5, 9, 2, 6};
            for (int s : scores) {
                ranker.offer(s, s);
            }
            List<TopKRanker.ScoredItem<Integer>> top = ranker.getTopK();
            for (int i = 1; i < top.size(); i++) {
                assertTrue(top.get(i - 1).score() >= top.get(i).score());
            }
        }

        @Test
        void fewerItemsThanKReturnsAllOfThem() {
            TopKRanker<String> ranker = new TopKRanker<>(10);
            ranker.offer("a", 1.0);
            ranker.offer("b", 2.0);
            assertEquals(2, ranker.getTopK().size());
        }

        @Test
        void lowerScoreThanCurrentMinimumIsDiscardedOnceFull() {
            TopKRanker<String> ranker = new TopKRanker<>(1);
            ranker.offer("first", 5.0);
            ranker.offer("worse", 1.0);
            assertEquals("first", ranker.getTopK().get(0).item());
        }

        @Test
        void emptyRankerReturnsEmptyList() {
            TopKRanker<String> ranker = new TopKRanker<>(5);
            assertTrue(ranker.getTopK().isEmpty());
            assertEquals(0, ranker.size());
        }
    }

    @Nested
    class Threshold {

        @Test
        void thresholdIsNaNWhenEmpty() {
            TopKRanker<String> ranker = new TopKRanker<>(3);
            assertTrue(Double.isNaN(ranker.currentThreshold()));
        }

        @Test
        void thresholdReflectsLowestRetainedScore() {
            TopKRanker<String> ranker = new TopKRanker<>(2);
            ranker.offer("a", 10.0);
            ranker.offer("b", 20.0);
            ranker.offer("c", 30.0); // "a" evicted
            assertEquals(20.0, ranker.currentThreshold());
        }
    }
}
