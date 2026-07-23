package com.indexcore.sorting;

import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based tests (jqwik): instead of hand-picked examples, these assert
 * invariants that must hold for ANY input the framework generates -- jqwik
 * will try hundreds of random arrays, including edge cases like empty arrays,
 * single elements, and arrays full of duplicates or extreme values, which is
 * exactly the kind of case a hand-written example test tends to miss.
 */
class SortingProperties {

    @Property
    void quickSortResultIsAlwaysSorted(@ForAll int[] input) {
        int[] arr = input.clone();
        Sorting.quickSort(arr);
        assertTrue(Sorting.isSorted(arr));
    }

    @Property
    void mergeSortResultIsAlwaysSorted(@ForAll int[] input) {
        int[] arr = input.clone();
        Sorting.mergeSort(arr);
        assertTrue(Sorting.isSorted(arr));
    }

    @Property
    void sortingIsAPermutation_neverAddsOrDropsElements(@ForAll int[] input) {
        int[] arr = input.clone();
        int[] original = input.clone();
        Sorting.quickSort(arr);

        Arrays.sort(original); // reference sort for comparison
        // A correct sort is a permutation of the input -- same multiset of values.
        assertEquals(Arrays.toString(original), Arrays.toString(arr));
    }

    @Property
    void quickSortAndMergeSortAlwaysAgree(@ForAll int[] input) {
        int[] a = input.clone();
        int[] b = input.clone();
        Sorting.quickSort(a);
        Sorting.mergeSort(b);
        assertEquals(Arrays.toString(a), Arrays.toString(b));
    }

    @Property
    void binarySearchFindsEveryElementThatIsActuallyPresent(
            @ForAll @Size(min = 1, max = 200) int[] input) {
        int[] arr = input.clone();
        Sorting.quickSort(arr);
        for (int value : arr) {
            int index = Sorting.binarySearch(arr, value);
            assertTrue(index >= 0 && arr[index] == value,
                    "expected to find " + value + " in " + Arrays.toString(arr));
        }
    }

    @Property
    void binarySearchNeverFindsAValueBelowTheMinimum(@ForAll int[] input) {
        int[] arr = input.clone();
        Sorting.quickSort(arr);
        if (arr.length == 0 || arr[0] == Integer.MIN_VALUE) return; // avoid underflow
        int belowMin = arr[0] - 1;
        assertEquals(-1, Sorting.binarySearch(arr, belowMin));
    }
}
