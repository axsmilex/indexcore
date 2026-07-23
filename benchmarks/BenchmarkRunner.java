import com.indexcore.sorting.Sorting;

import java.util.Arrays;
import java.util.Random;

/**
 * Standalone benchmark harness (not a JUnit test) comparing custom
 * QuickSort/MergeSort against java.util.Arrays.sort across input sizes.
 *
 * This is intentionally simple wall-clock timing rather than JMH, to keep
 * the project buildable without an extra benchmarking dependency. For a
 * more rigorous version, swap this for a JMH harness (see README stretch
 * goals) which handles JIT warmup and measurement noise properly --
 * numbers here should be read as directional, not authoritative.
 *
 * Run with (after `mvn compile`):
 *   javac -cp target/classes -d target/classes benchmarks/BenchmarkRunner.java
 *   java -cp target/classes BenchmarkRunner
 * (no package declaration -- this class lives in the default package, matching
 * the commands above; see README.md's Benchmarks section.)
 */
public class BenchmarkRunner {

    private static final int[] SIZES = {1_000, 10_000, 100_000, 1_000_000};
    private static final int WARMUP_RUNS = 3;

    public static void main(String[] args) {
        System.out.printf("%-12s %-15s %-15s %-15s%n", "Size", "QuickSort(ms)", "MergeSort(ms)", "Arrays.sort(ms)");
        System.out.println("-".repeat(60));

        for (int size : SIZES) {
            int[] base = randomArray(size, 1234);

            // Warmup to let the JIT compile hot paths before timing.
            for (int i = 0; i < WARMUP_RUNS; i++) {
                timeSort(base.clone(), Sorting::quickSort);
                timeSort(base.clone(), Sorting::mergeSort);
                timeSort(base.clone(), Arrays::sort);
            }

            long quickMs = timeSort(base.clone(), Sorting::quickSort);
            long mergeMs = timeSort(base.clone(), Sorting::mergeSort);
            long stdlibMs = timeSort(base.clone(), Arrays::sort);

            System.out.printf("%-12d %-15d %-15d %-15d%n", size, quickMs, mergeMs, stdlibMs);
        }

        System.out.println();
        System.out.println("Results are directional (single-machine wall-clock timing).");
        System.out.println("See README.md benchmarks section for methodology notes.");
    }

    private static long timeSort(int[] arr, java.util.function.Consumer<int[]> sortFn) {
        long start = System.nanoTime();
        sortFn.accept(arr);
        long end = System.nanoTime();
        return (end - start) / 1_000_000;
    }

    private static int[] randomArray(int size, long seed) {
        Random rand = new Random(seed);
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = rand.nextInt();
        }
        return arr;
    }
}
