package com.indexcore.sorting;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SortingTest {

    @Nested
    class QuickSortTests {

        @Test
        void sortsUnsortedArray() {
            int[] arr = {5, 2, 8, 1, 9, 3};
            Sorting.quickSort(arr);
            assertArrayEquals(new int[]{1, 2, 3, 5, 8, 9}, arr);
        }

        @Test
        void handlesAlreadySortedArray() {
            int[] arr = {1, 2, 3, 4, 5};
            Sorting.quickSort(arr);
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, arr);
        }

        @Test
        void handlesReverseSortedArray() {
            int[] arr = {5, 4, 3, 2, 1};
            Sorting.quickSort(arr);
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, arr);
        }

        @Test
        void handlesAllDuplicates() {
            int[] arr = {7, 7, 7, 7, 7};
            Sorting.quickSort(arr);
            assertArrayEquals(new int[]{7, 7, 7, 7, 7}, arr);
        }

        @Test
        void handlesSingleElement() {
            int[] arr = {42};
            Sorting.quickSort(arr);
            assertArrayEquals(new int[]{42}, arr);
        }

        @Test
        void handlesEmptyArray() {
            int[] arr = {};
            Sorting.quickSort(arr);
            assertArrayEquals(new int[]{}, arr);
        }

        @Test
        void handlesNegativeNumbers() {
            int[] arr = {-3, 5, -1, 0, -8, 2};
            Sorting.quickSort(arr);
            assertArrayEquals(new int[]{-8, -3, -1, 0, 2, 5}, arr);
        }

        @Test
        void nullArrayThrows() {
            assertThrows(IllegalArgumentException.class, () -> Sorting.quickSort(null));
        }

        @ParameterizedTest
        @ValueSource(ints = {10, 100, 1000})
        void matchesJavaStandardLibraryOnRandomInput(int size) {
            int[] arr = randomArray(size, 42);
            int[] expected = arr.clone();
            Arrays.sort(expected);
            Sorting.quickSort(arr);
            assertArrayEquals(expected, arr);
        }
    }

    @Nested
    class MergeSortTests {

        @Test
        void sortsUnsortedArray() {
            int[] arr = {5, 2, 8, 1, 9, 3};
            Sorting.mergeSort(arr);
            assertArrayEquals(new int[]{1, 2, 3, 5, 8, 9}, arr);
        }

        @Test
        void handlesEmptyArray() {
            int[] arr = {};
            Sorting.mergeSort(arr);
            assertArrayEquals(new int[]{}, arr);
        }

        @Test
        void handlesSingleElement() {
            int[] arr = {1};
            Sorting.mergeSort(arr);
            assertArrayEquals(new int[]{1}, arr);
        }

        @Test
        void handlesAllDuplicates() {
            int[] arr = {3, 3, 3};
            Sorting.mergeSort(arr);
            assertArrayEquals(new int[]{3, 3, 3}, arr);
        }

        @Test
        void nullArrayThrows() {
            assertThrows(IllegalArgumentException.class, () -> Sorting.mergeSort(null));
        }

        @ParameterizedTest
        @ValueSource(ints = {10, 100, 1000})
        void matchesJavaStandardLibraryOnRandomInput(int size) {
            int[] arr = randomArray(size, 7);
            int[] expected = arr.clone();
            Arrays.sort(expected);
            Sorting.mergeSort(arr);
            assertArrayEquals(expected, arr);
        }
    }

    @Nested
    class BinarySearchTests {

        private final int[] sorted = {1, 3, 5, 7, 9, 11, 13};

        @Test
        void findsExistingElement() {
            assertEquals(3, Sorting.binarySearch(sorted, 7));
        }

        @Test
        void findsFirstElement() {
            assertEquals(0, Sorting.binarySearch(sorted, 1));
        }

        @Test
        void findsLastElement() {
            assertEquals(6, Sorting.binarySearch(sorted, 13));
        }

        @Test
        void returnsMinusOneForAbsentElement() {
            assertEquals(-1, Sorting.binarySearch(sorted, 6));
        }

        @Test
        void returnsMinusOneForEmptyArray() {
            assertEquals(-1, Sorting.binarySearch(new int[]{}, 5));
        }

        @Test
        void singleElementArrayMatch() {
            assertEquals(0, Sorting.binarySearch(new int[]{42}, 42));
        }

        @Test
        void singleElementArrayNoMatch() {
            assertEquals(-1, Sorting.binarySearch(new int[]{42}, 1));
        }

        @Test
        void belowRangeReturnsMinusOne() {
            assertEquals(-1, Sorting.binarySearch(sorted, -100));
        }

        @Test
        void aboveRangeReturnsMinusOne() {
            assertEquals(-1, Sorting.binarySearch(sorted, 100));
        }
    }

    private static int[] randomArray(int size, long seed) {
        Random rand = new Random(seed);
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = rand.nextInt(10_000) - 5_000;
        }
        return arr;
    }
}
