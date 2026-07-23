package com.indexcore.graph;

import java.util.*;

/**
 * A weighted, directed graph modeling product relationships -- e.g., an edge
 * A -> B with weight w means "co-purchase strength" or "inverse similarity"
 * between products A and B. This backs a "customers who viewed X also viewed
 * Y" / "related products" recommendation feature.
 *
 * Supports:
 *   - BFS: "related products within N hops" (ignores weight, pure connectivity)
 *   - DFS: full reachability / connected-component exploration
 *   - Dijkstra: cheapest path between two products (weights must be non-negative)
 */
public class ProductGraph {

    public record Edge(String to, double weight) {}

    private final Map<String, List<Edge>> adjacency = new HashMap<>();

    /** Ensures a node exists even if it has no edges yet (e.g., a brand-new product). */
    public void addNode(String node) {
        Objects.requireNonNull(node, "node must not be null");
        adjacency.putIfAbsent(node, new ArrayList<>());
    }

    /**
     * Adds a directed edge from -> to with the given weight.
     * @throws IllegalArgumentException if weight is negative (Dijkstra requires non-negative weights)
     */
    public void addEdge(String from, String to, double weight) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if (weight < 0) {
            throw new IllegalArgumentException("edge weight must be non-negative, got " + weight);
        }
        addNode(from);
        addNode(to);
        adjacency.get(from).add(new Edge(to, weight));
    }

    /** Adds edges in both directions with the same weight (symmetric relationship). */
    public void addUndirectedEdge(String a, String b, double weight) {
        addEdge(a, b, weight);
        addEdge(b, a, weight);
    }

    public boolean containsNode(String node) {
        return adjacency.containsKey(node);
    }

    public int nodeCount() {
        return adjacency.size();
    }

    public List<Edge> neighbors(String node) {
        List<Edge> edges = adjacency.get(node);
        if (edges == null) {
            throw new NoSuchElementException("no such node: " + node);
        }
        return Collections.unmodifiableList(edges);
    }

    /**
     * Breadth-first traversal from {@code start}, returning nodes reachable
     * within {@code maxHops} hops, in the order discovered (ignores weight).
     * maxHops == 0 returns just the start node; maxHops < 0 is invalid.
     */
    public List<String> relatedWithinHops(String start, int maxHops) {
        requireNode(start);
        if (maxHops < 0) {
            throw new IllegalArgumentException("maxHops must be non-negative");
        }

        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        Map<String, Integer> distance = new HashMap<>();

        queue.add(start);
        visited.add(start);
        distance.put(start, 0);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(current);
            int d = distance.get(current);
            if (d == maxHops) continue;

            for (Edge e : adjacency.getOrDefault(current, List.of())) {
                if (visited.add(e.to())) {
                    distance.put(e.to(), d + 1);
                    queue.add(e.to());
                }
            }
        }
        return result;
    }

    /** Depth-first traversal from {@code start}; returns all reachable nodes in visitation order. */
    public List<String> depthFirstTraversal(String start) {
        requireNode(start);
        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        dfsHelper(start, visited, result);
        return result;
    }

    private void dfsHelper(String node, Set<String> visited, List<String> result) {
        if (!visited.add(node)) return;
        result.add(node);
        for (Edge e : adjacency.getOrDefault(node, List.of())) {
            dfsHelper(e.to(), visited, result);
        }
    }

    /**
     * Dijkstra's algorithm: shortest (cheapest) path from source to target.
     * Returns an empty list if target is unreachable.
     *
     * @throws IllegalArgumentException if either node is absent from the graph
     */
    public List<String> shortestPath(String source, String target) {
        requireNode(source);
        requireNode(target);

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> dist.getOrDefault(n, Double.POSITIVE_INFINITY)));

        for (String node : adjacency.keySet()) {
            dist.put(node, Double.POSITIVE_INFINITY);
        }
        dist.put(source, 0.0);
        pq.add(source);

        Set<String> finalized = new HashSet<>();

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (!finalized.add(u)) continue;
            if (u.equals(target)) break;

            for (Edge e : adjacency.getOrDefault(u, List.of())) {
                double candidate = dist.get(u) + e.weight();
                if (candidate < dist.getOrDefault(e.to(), Double.POSITIVE_INFINITY)) {
                    dist.put(e.to(), candidate);
                    prev.put(e.to(), u);
                    pq.add(e.to());
                }
            }
        }

        if (dist.getOrDefault(target, Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY) {
            return List.of();
        }

        LinkedList<String> path = new LinkedList<>();
        String current = target;
        while (current != null) {
            path.addFirst(current);
            current = prev.get(current);
        }
        return path;
    }

    private void requireNode(String node) {
        if (!adjacency.containsKey(node)) {
            throw new IllegalArgumentException("no such node: " + node);
        }
    }
}
