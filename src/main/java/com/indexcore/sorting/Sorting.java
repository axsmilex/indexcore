package com.indexcore.sorting;

import java.util.Random;

/**
 * Custom sort/search implementations, kept intentionally separate from
 * java.util.Arrays so they can be benchmarked against the standard library
 * (see benchmarks/). Operates on int[] to keep the benchmark comparison
 * apples-to-apples and avoid autoboxing overhead skewing results.
 */
public final class Sorting {

    private Sorting() {}

    /** Randomized QuickSort (Lomuto partition, random pivot to avoid worst-case on sorted input). O(n log n) average, O(n^2) worst case. */
    public static void quickSort(int[] arr) {
        if (arr == null) throw new IllegalArgumentException("arr must not be null");
        if (arr.length <= 1) return;
        quickSort(arr, 0, arr.length - 1, new Random(42));
    }

    private static void quickSort(int[] arr, int low, int high, Random rand) {
        if (low >= high) return;
        int pivotIndex = low + rand.nextInt(high - low + 1);
        swap(arr, pivotIndex, high);
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        int p = i + 1;
        quickSort(arr, low, p - 1, rand);
        quickSort(arr, p + 1, high, rand);
    }

    /** Top-down MergeSort. O(n log n) guaranteed, O(n) extra space. Stable. */
    public static void mergeSort(int[] arr) {
        if (arr == null) throw new IllegalArgumentException("arr must not be null");
        if (arr.length <= 1) return;
        int[] buffer = new int[arr.length];
        mergeSort(arr, buffer, 0, arr.length - 1);
    }

    private static void mergeSort(int[] arr, int[] buffer, int low, int high) {
        if (low >= high) return;
        int mid = low + (high - low) / 2;
        mergeSort(arr, buffer, low, mid);
        mergeSort(arr, buffer, mid + 1, high);
        merge(arr, buffer, low, mid, high);
    }

    private static void merge(int[] arr, int[] buffer, int low, int mid, int high) {
        System.arraycopy(arr, low, buffer, low, high - low + 1);
        int i = low, j = mid + 1, k = low;
        while (i <= mid && j <= high) {
            arr[k++] = (buffer[i] <= buffer[j]) ? buffer[i++] : buffer[j++];
        }
        while (i <= mid) arr[k++] = buffer[i++];
        while (j <= high) arr[k++] = buffer[j++];
    }

    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    /** Classic iterative binary search on a sorted array. Returns index or -1 if absent. O(log n). */
    public static int binarySearch(int[] sortedArr, int target) {
        if (sortedArr == null) throw new IllegalArgumentException("sortedArr must not be null");
        int lo = 0, hi = sortedArr.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (sortedArr[mid] == target) return mid;
            if (sortedArr[mid] < target) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    /** Returns true if arr is sorted ascending -- used by tests/benchmarks to validate correctness. */
    public static boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i - 1] > arr[i]) return false;
        }
        return true;
    }
}
