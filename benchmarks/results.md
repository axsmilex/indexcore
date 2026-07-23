# Benchmark Results

Custom `QuickSort` and `MergeSort` (see `src/main/java/com/indexcore/sorting/Sorting.java`)
timed against the JDK's `java.util.Arrays.sort`, across four input sizes.

## Environment

- JDK 21 (Microsoft Build of OpenJDK), Windows x64
- Single-machine wall-clock timing, 3 JIT warmup runs before measurement
- Random `int[]` inputs, fixed seed for reproducibility

## Results

| Size      | QuickSort (ms) | MergeSort (ms) | Arrays.sort (ms) |
|-----------|----------------|----------------|------------------|
| 1,000     | 0              | 0              | 0                |
| 10,000    | 0              | 0              | 0                |
| 100,000   | 6              | 6              | 7                |
| 1,000,000 | 58             | 75             | 49               |

## Reading the numbers

At small and mid sizes the three are indistinguishable. At 1M elements the
custom implementations stay within the same order of magnitude as the
standard library: the randomized QuickSort (~58 ms) trails `Arrays.sort`
(~49 ms) by a modest margin, and MergeSort (~75 ms) is slower still, which
is expected. `Arrays.sort` uses a dual-pivot QuickSort for primitives that
has been tuned for years, and MergeSort pays for its O(n) auxiliary buffer
and guaranteed stability. The goal here was a correct, from-scratch
implementation that lands in the right performance neighborhood, not one
that beats a mature standard-library sort.

These are directional single-run wall-clock numbers, not a rigorous JMH
benchmark. A JMH-based harness (which properly handles warmup, dead-code
elimination, and measurement variance) is a listed stretch goal. Re-run with:

```bash
mvn compile
javac -cp target/classes -d target/classes benchmarks/BenchmarkRunner.java
java -cp target/classes BenchmarkRunner
```
