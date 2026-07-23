package com.indexcore.unionfind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisjointSetTest {

    private DisjointSet<String> ds;

    @BeforeEach
    void setUp() {
        ds = new DisjointSet<>();
    }

    @Nested
    class MakeSetAndFind {

        @Test
        void newElementIsItsOwnRoot() {
            ds.makeSet("sku1");
            assertEquals("sku1", ds.find("sku1"));
        }

        @Test
        void findOnUnregisteredElementThrows() {
            assertThrows(IllegalArgumentException.class, () -> ds.find("unknown"));
        }

        @Test
        void makeSetIsIdempotent() {
            ds.makeSet("sku1");
            ds.union("sku1", "sku1"); // shouldn't blow up or double count
            ds.makeSet("sku1");
            assertEquals(1, ds.componentCount());
        }
    }

    @Nested
    class UnionAndConnectivity {

        @BeforeEach
        void seedElements() {
            ds.makeSet("sku1");
            ds.makeSet("sku2");
            ds.makeSet("sku3");
            ds.makeSet("sku4");
        }

        @Test
        void unmergedElementsAreNotConnected() {
            assertFalse(ds.connected("sku1", "sku2"));
        }

        @Test
        void unionMergesTwoElements() {
            ds.union("sku1", "sku2");
            assertTrue(ds.connected("sku1", "sku2"));
        }

        @Test
        void unionIsTransitive() {
            ds.union("sku1", "sku2");
            ds.union("sku2", "sku3");
            assertTrue(ds.connected("sku1", "sku3")); // transitivity via path compression
        }

        @Test
        void unionOfAlreadyConnectedElementsReturnsFalse() {
            ds.union("sku1", "sku2");
            assertFalse(ds.union("sku1", "sku2"));
        }

        @Test
        void unionOfNewPairReturnsTrue() {
            assertTrue(ds.union("sku1", "sku2"));
        }

        @Test
        void componentCountDecreasesOnMerge() {
            assertEquals(4, ds.componentCount());
            ds.union("sku1", "sku2");
            assertEquals(3, ds.componentCount());
            ds.union("sku3", "sku4");
            assertEquals(2, ds.componentCount());
            ds.union("sku1", "sku4"); // merges the two remaining clusters
            assertEquals(1, ds.componentCount());
        }

        @Test
        void selfUnionDoesNotChangeComponentCount() {
            int before = ds.componentCount();
            ds.union("sku1", "sku1");
            assertEquals(before, ds.componentCount());
        }
    }

    @Nested
    class Validation {

        @Test
        void unionWithUnregisteredElementThrows() {
            ds.makeSet("sku1");
            assertThrows(IllegalArgumentException.class, () -> ds.union("sku1", "ghost"));
        }

        @Test
        void connectedWithUnregisteredElementThrows() {
            ds.makeSet("sku1");
            assertThrows(IllegalArgumentException.class, () -> ds.connected("sku1", "ghost"));
        }

        @Test
        void containsReflectsRegistration() {
            ds.makeSet("sku1");
            assertTrue(ds.contains("sku1"));
            assertFalse(ds.contains("ghost"));
        }
    }
}
