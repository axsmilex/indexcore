package com.indexcore.trie;

import java.util.*;

/**
 * A weighted Trie (prefix tree) used to power product/query autocomplete.
 *
 * Each inserted term carries a "weight" (e.g., historical search frequency,
 * or product popularity). {@link #topKSuggestions(String, int)} returns the
 * k highest-weighted completions of a given prefix, which is the behavior
 * you actually want in a search box -- not just "all matches."
 *
 * Time complexity:
 *   insert(word)                 O(L)              L = word length
 *   topKSuggestions(prefix, k)   O(L + M log k)     M = number of matches under the prefix
 *
 * Space complexity: O(total characters across all inserted words), bounded
 * by shared prefixes.
 */
public class AutocompleteTrie {

    private static class Node {
        final Map<Character, Node> children = new HashMap<>();
        boolean isTerminal = false;
        long weight = 0; // only meaningful when isTerminal == true
    }

    private final Node root = new Node();
    private int size = 0;

    /**
     * Inserts a word with the given weight. If the word already exists, the
     * weight is updated to {@code max(existingWeight, weight)} -- re-inserting
     * with a lower weight never demotes a term that's already popular.
     *
     * @throws IllegalArgumentException if word is null/empty or weight is negative
     */
    public void insert(String word, long weight) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("word must be non-null and non-empty");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("weight must be non-negative");
        }
        Node current = root;
        for (char c : word.toCharArray()) {
            current = current.children.computeIfAbsent(c, ch -> new Node());
        }
        if (!current.isTerminal) {
            size++;
        }
        current.isTerminal = true;
        current.weight = Math.max(current.weight, weight);
    }

    /** Convenience overload: inserts with weight 1. */
    public void insert(String word) {
        insert(word, 1);
    }

    /** Returns true if the exact word was inserted (not just a prefix of something inserted). */
    public boolean contains(String word) {
        Node node = findNode(word);
        return node != null && node.isTerminal;
    }

    /** Returns true if any inserted word starts with the given prefix. */
    public boolean hasPrefix(String prefix) {
        return findNode(prefix) != null;
    }

    /**
     * Returns up to k completions of {@code prefix}, ordered by descending
     * weight (ties broken lexicographically for determinism).
     */
    public List<String> topKSuggestions(String prefix, int k) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null");
        }
        if (k < 0) {
            throw new IllegalArgumentException("k must be non-negative");
        }
        if (k == 0) {
            return List.of();
        }

        Node prefixNode = findNode(prefix);
        if (prefixNode == null) {
            return List.of();
        }

        // Min-heap of size k keyed by weight; keeps the top-k without sorting
        // every match. Ties broken so results are deterministic.
        PriorityQueue<Map.Entry<String, Long>> heap = new PriorityQueue<>(
                (a, b) -> {
                    int byWeight = Long.compare(a.getValue(), b.getValue());
                    if (byWeight != 0) return byWeight;
                    return b.getKey().compareTo(a.getKey()); // reverse for min-heap tie-break
                }
        );

        collect(prefixNode, new StringBuilder(prefix), heap, k);

        List<Map.Entry<String, Long>> result = new ArrayList<>(heap);
        result.sort((a, b) -> {
            int byWeight = Long.compare(b.getValue(), a.getValue());
            if (byWeight != 0) return byWeight;
            return a.getKey().compareTo(b.getKey());
        });

        List<String> words = new ArrayList<>(result.size());
        for (Map.Entry<String, Long> e : result) {
            words.add(e.getKey());
        }
        return words;
    }

    private void collect(Node node, StringBuilder path, PriorityQueue<Map.Entry<String, Long>> heap, int k) {
        if (node.isTerminal) {
            heap.offer(Map.entry(path.toString(), node.weight));
            if (heap.size() > k) {
                heap.poll();
            }
        }
        for (Map.Entry<Character, Node> entry : node.children.entrySet()) {
            path.append(entry.getKey());
            collect(entry.getValue(), path, heap, k);
            path.deleteCharAt(path.length() - 1);
        }
    }

    private Node findNode(String prefix) {
        Node current = root;
        for (char c : prefix.toCharArray()) {
            current = current.children.get(c);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Standard Levenshtein edit distance between two strings -- used as a
     * fallback when a prefix has zero exact matches (typo tolerance).
     * O(n*m) time, O(n*m) space.
     */
    public static int editDistance(String a, String b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[a.length()][b.length()];
    }
}
