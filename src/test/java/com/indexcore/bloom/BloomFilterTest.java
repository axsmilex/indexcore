package com.indexcore.bloom;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BloomFilterTest {

    @Nested
    class Construction {

        @Test
        void zeroExpectedInsertionsThrows() {
            assertThrows(IllegalArgumentException.class, () -> new BloomFilter(0, 0.01));
        }

        @Test
        void negativeExpectedInsertionsThrows() {
            assertThrows(IllegalArgumentException.class, () -> new BloomFilter(-5, 0.01));
        }

        @Test
        void falsePositiveRateOutOfRangeThrows() {
            assertThrows(IllegalArgumentException.class, () -> new BloomFilter(100, 0.0));
            assertThrows(IllegalArgumentException.class, () -> new BloomFilter(100, 1.0));
            assertThrows(IllegalArgumentException.class, () -> new BloomFilter(100, -0.5));
        }

        @Test
        void bitSizeGrowsWithExpectedInsertions() {
            BloomFilter small = new BloomFilter(10, 0.01);
            BloomFilter large = new BloomFilter(10_000, 0.01);
            assertTrue(large.bitSize() > small.bitSize());
        }
    }

    @Nested
    class NoFalseNegatives {

        @Test
        void everyAddedElementIsAlwaysReportedAsPossiblyPresent() {
            BloomFilter filter = new BloomFilter(1000, 0.01);
            String[] items = {"sku-1001", "sku-1002", "sku-2093", "sku-9999", "widget-blue-large"};
            for (String item : items) {
                filter.add(item);
            }
            // This is the core Bloom filter guarantee: zero false negatives, ever.
            for (String item : items) {
                assertTrue(filter.mightContain(item), item + " should never be a false negative");
            }
        }
    }

    @Nested
    class DefiniteAbsence {

        @Test
        void emptyFilterReportsAbsenceForAnything() {
            BloomFilter filter = new BloomFilter(1000, 0.01);
            assertFalse(filter.mightContain("anything"));
        }

        @Test
        void falsePositiveRateStaysReasonablyBounded() {
            // Statistical test: with a well-tuned filter, false positive rate
            // among NON-inserted items should stay roughly within the configured bound.
            BloomFilter filter = new BloomFilter(1000, 0.01);
            Set<String> inserted = new HashSet<>();
            for (int i = 0; i < 1000; i++) {
                String key = "product-" + i;
                inserted.add(key);
                filter.add(key);
            }

            int falsePositives = 0;
            int trials = 10_000;
            for (int i = 0; i < trials; i++) {
                String probe = "absent-" + i;
                if (!inserted.contains(probe) && filter.mightContain(probe)) {
                    falsePositives++;
                }
            }

            double observedRate = (double) falsePositives / trials;
            // Generous upper bound (5x the configured 1%) to keep the test from being flaky
            // while still catching a badly broken hash/sizing implementation.
            assertTrue(observedRate < 0.05,
                    "observed false-positive rate " + observedRate + " far exceeds configured bound");
        }
    }

    @Nested
    class Counters {

        @Test
        void approximateInsertedCountTracksAddCalls() {
            BloomFilter filter = new BloomFilter(100, 0.01);
            filter.add("a");
            filter.add("b");
            filter.add("a"); // re-adding still increments -- it's a raw counter, not a distinct-count
            assertEquals(3, filter.approximateInsertedCount());
        }

        @Test
        void hashCountIsAtLeastOne() {
            BloomFilter filter = new BloomFilter(1, 0.5);
            assertTrue(filter.hashCount() >= 1);
        }
    }
}
