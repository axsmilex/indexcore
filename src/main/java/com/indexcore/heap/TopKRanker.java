package com.indexcore.heap;

import java.util.*;

/**
 * Maintains the top-K highest-scored items seen so far, using a bounded
 * min-heap of size K. This backs "top rated," "best discount," and
 * "closest match" style rankings where you never want to hold the full
 * result set in memory or sort it -- just the K that matter.
 *
 * Time complexity: O(log k) per offer, O(k log k) to extract sorted results.
 * Space complexity: O(k), regardless of how many items are offered.
 */
public class TopKRanker<T> {

    private final int k;
    private final PriorityQueue<ScoredItem<T>> minHeap;

    public record ScoredItem<T>(T item, double score) {}

    public TopKRanker(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        this.k = k;
        this.minHeap = new PriorityQueue<>(k, Comparator.comparingDouble(ScoredItem::score));
    }

    /** Offers an item with a score; retains it only if it's in the current top-k. */
    public void offer(T item, double score) {
        if (minHeap.size() < k) {
            minHeap.offer(new ScoredItem<>(item, score));
        } else if (!minHeap.isEmpty() && minHeap.peek().score() < score) {
            minHeap.poll();
            minHeap.offer(new ScoredItem<>(item, score));
        }
    }

    /** Returns the current top items, sorted descending by score. */
    public List<ScoredItem<T>> getTopK() {
        List<ScoredItem<T>> result = new ArrayList<>(minHeap);
        result.sort((a, b) -> Double.compare(b.score(), a.score()));
        return result;
    }

    public int size() {
        return minHeap.size();
    }

    public int capacity() {
        return k;
    }

    /** The lowest score currently retained, or NaN if empty -- useful as an early-exit threshold. */
    public double currentThreshold() {
        return minHeap.isEmpty() ? Double.NaN : minHeap.peek().score();
    }
}
