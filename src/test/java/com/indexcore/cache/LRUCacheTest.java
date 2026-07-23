package com.indexcore.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LRUCacheTest {

    private LRUCache<String, Integer> cache;

    @BeforeEach
    void setUp() {
        cache = new LRUCache<>(3);
    }

    @Nested
    class BasicOperations {

        @Test
        void putThenGetReturnsValue() {
            cache.put("a", 1);
            assertEquals(1, cache.get("a"));
        }

        @Test
        void getOnMissingKeyReturnsNull() {
            assertNull(cache.get("missing"));
        }

        @Test
        void updatingExistingKeyOverwritesValue() {
            cache.put("a", 1);
            cache.put("a", 2);
            assertEquals(2, cache.get("a"));
            assertEquals(1, cache.size());
        }

        @Test
        void containsKeyReflectsPresence() {
            cache.put("a", 1);
            assertTrue(cache.containsKey("a"));
            assertFalse(cache.containsKey("b"));
        }
    }

    @Nested
    class EvictionBehavior {

        @Test
        void evictsLeastRecentlyUsedWhenOverCapacity() {
            cache.put("a", 1);
            cache.put("b", 2);
            cache.put("c", 3);
            cache.put("d", 4); // capacity is 3, "a" should be evicted

            assertFalse(cache.containsKey("a"));
            assertTrue(cache.containsKey("b"));
            assertTrue(cache.containsKey("c"));
            assertTrue(cache.containsKey("d"));
            assertEquals(3, cache.size());
        }

        @Test
        void getRefreshesRecencyPreventingEviction() {
            cache.put("a", 1);
            cache.put("b", 2);
            cache.put("c", 3);
            cache.get("a"); // "a" is now most-recently-used
            cache.put("d", 4); // "b" should be evicted instead of "a"

            assertTrue(cache.containsKey("a"));
            assertFalse(cache.containsKey("b"));
        }

        @Test
        void puttingExistingKeyRefreshesRecency() {
            cache.put("a", 1);
            cache.put("b", 2);
            cache.put("c", 3);
            cache.put("a", 100); // refresh "a"
            cache.put("d", 4); // "b" should be evicted

            assertTrue(cache.containsKey("a"));
            assertEquals(100, cache.get("a"));
            assertFalse(cache.containsKey("b"));
        }

        @Test
        void capacityOneAlwaysHoldsOnlyLatest() {
            LRUCache<String, Integer> tiny = new LRUCache<>(1);
            tiny.put("a", 1);
            tiny.put("b", 2);
            assertFalse(tiny.containsKey("a"));
            assertTrue(tiny.containsKey("b"));
            assertEquals(1, tiny.size());
        }
    }

    @Nested
    class EdgeCasesAndValidation {

        @Test
        void zeroCapacityThrows() {
            assertThrows(IllegalArgumentException.class, () -> new LRUCache<String, Integer>(0));
        }

        @Test
        void negativeCapacityThrows() {
            assertThrows(IllegalArgumentException.class, () -> new LRUCache<String, Integer>(-5));
        }

        @Test
        void emptyCacheSizeIsZero() {
            assertEquals(0, cache.size());
        }
    }

    @Nested
    class HitRateTracking {

        @Test
        void hitRateIsZeroWithNoAccesses() {
            assertEquals(0.0, cache.hitRate());
        }

        @Test
        void hitRateReflectsHitsAndMisses() {
            cache.put("a", 1);
            cache.get("a");     // hit
            cache.get("a");     // hit
            cache.get("missing"); // miss
            assertEquals(2.0 / 3.0, cache.hitRate(), 1e-9);
        }
    }
}
