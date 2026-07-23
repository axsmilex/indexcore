# IndexCore

**A Java data-structures-and-algorithms engine modeling the core layer of an
e-commerce search, recommendation, caching, and deduplication system.**

Every structure in this repo exists because a real subsystem needs it — this
isn't a loose collection of textbook exercises. The modules compose into the
kind of pipeline a product-search backend actually runs:

```
 query ──► AutocompleteTrie ──► LRUCache (hot queries) ──► results
                                       │
 new listing ──► BloomFilter (cheap pre-check) ──► DisjointSet (cluster duplicates)
                                       │
 "related products" ──► ProductGraph (BFS / Dijkstra)
                                       │
 ranking ──► TopKRanker (bounded heap)
                                       │
 inventory ──► FenwickTree (range sum) / SegmentTree (range min)
```

## Modules

| Module | Structure | Backs | Complexity |
|---|---|---|---|
| `trie/AutocompleteTrie` | Weighted Trie | Search-box autocomplete, ranked by popularity | insert O(L), topK O(L + M log k) |
| `cache/LRUCache` | HashMap + doubly linked list | Hot-query cache in front of expensive lookups | O(1) get/put |
| `graph/ProductGraph` | Weighted directed graph | "Related products," cheapest-path recommendations | BFS/DFS O(V+E), Dijkstra O((V+E) log V) |
| `unionfind/DisjointSet` | Union-Find, path compression + union by rank | Clustering near-duplicate listings | ~O(α(n)) amortized |
| `bloom/BloomFilter` | Bloom filter, double hashing | Cheap duplicate pre-check before Union-Find | O(k) per op |
| `heap/TopKRanker` | Bounded min-heap | "Top rated" / "best discount" rankings | O(log k) per offer |
| `segmenttree/FenwickTree` | Binary Indexed Tree | Inventory range-sum queries | O(log n) |
| `segmenttree/SegmentTree` | Array-backed segment tree | Inventory range-min (restock alerts) | O(log n) |
| `sorting/Sorting` | QuickSort, MergeSort, binary search | Benchmarked against `java.util.Arrays` | O(n log n) / O(log n) |

## Why this design

- **Bloom filter before Union-Find**: a two-stage dedup pipeline where the
  cheap, probabilistic filter rejects the vast majority of non-duplicates in
  O(k) before the more expensive exact clustering step runs.
- **Fenwick tree *and* segment tree**: they look redundant until you need
  range-min — Fenwick trees are built around invertible operations (sum),
  segment trees generalize to non-invertible ones (min/max), which is why
  both exist here rather than just picking one.
- **Weighted Trie, not a plain one**: a real autocomplete box needs "most
  popular completions," not just "all valid completions" — the top-K heap
  inside the Trie is the actual product requirement, not decoration.

## Testing Strategy

This is the part most junior portfolios skip, and it's the actual point of
the project:

- **JUnit 5** — parameterized tests (`@ParameterizedTest`), nested test
  classes per behavior group, and explicit edge cases (empty input, single
  element, all-duplicates, negative/invalid input, unreachable graph nodes,
  self-unions).
- **jqwik (property-based testing)** — instead of hand-picked examples,
  properties like "sort output is always a permutation of the input" and
  "union is always symmetric" are checked against hundreds of
  framework-generated random inputs, including adversarial edge cases a
  human wouldn't think to write by hand.
- **JaCoCo** — line/branch coverage, run automatically in CI.
- **PIT (mutation testing)** — the differentiator: PIT injects small bugs
  ("mutants") into the compiled code and reruns the test suite against each
  one. If tests still pass, they didn't actually verify that logic. This
  produces a *mutation score*, which is a meaningfully different — and
  harder to game — signal than line coverage. A test suite can hit 100%
  coverage while still missing real bugs; mutation score catches that.

Run everything locally:

```bash
mvn clean test                                   # JUnit5 + jqwik + JaCoCo
mvn org.pitest:pitest-maven:mutationCoverage     # PIT mutation report
```

Reports land in `target/site/jacoco/index.html` and `target/pit-reports/`.

_Coverage: **[fill in after running]**% lines · Mutation score: **[fill in
after running]**% — numbers intentionally left blank until they're real._

## Benchmarks

`benchmarks/BenchmarkRunner.java` times the custom QuickSort/MergeSort
against `Arrays.sort` at 1K/10K/100K/1M elements:

```bash
mvn compile
javac -cp target/classes -d target/classes benchmarks/BenchmarkRunner.java
java -cp target/classes BenchmarkRunner
```

_(This is simple wall-clock timing with JIT warmup, not a rigorous JMH
harness — results are directional. A JMH-based version is a listed stretch
goal.)_

## CI

Every push runs the full test suite, JaCoCo coverage, and PIT mutation
analysis via GitHub Actions (`.github/workflows/ci.yml`), with both reports
uploaded as build artifacts.

## Project Structure

```
indexcore/
├── src/main/java/com/indexcore/
│   ├── trie/            AutocompleteTrie.java
│   ├── graph/            ProductGraph.java
│   ├── cache/             LRUCache.java
│   ├── heap/             TopKRanker.java
│   ├── unionfind/       DisjointSet.java
│   ├── bloom/           BloomFilter.java
│   ├── segmenttree/     SegmentTree.java, FenwickTree.java
│   └── sorting/          Sorting.java
├── src/test/java/com/indexcore/   (mirrors main/ - one *Test.java per module,
│                                    plus *Properties.java for jqwik)
├── benchmarks/           BenchmarkRunner.java
├── .github/workflows/    ci.yml
├── AI_USAGE.md
├── pom.xml
└── README.md
```

## Stretch Goals

- Small Spring Boot REST wrapper exposing `/autocomplete`, `/recommend`,
  `/search` endpoints backed by IndexCore
- JMH-based rigorous benchmarking in place of the current wall-clock harness
- Swap the demo data in the graph module for a real public dataset
  (e.g., a co-purchase or MovieLens-style dataset) instead of synthetic edges
- Small HTML/JS visualizer for Trie/Graph/Heap operations

## Requirements

- JDK 21+
- Maven 3.9+
