package com.indexcore.graph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductGraphTest {

    private ProductGraph graph;

    @BeforeEach
    void setUp() {
        graph = new ProductGraph();
    }

    @Nested
    class NodeAndEdgeManagement {

        @Test
        void addNodeMakesItPresent() {
            graph.addNode("A");
            assertTrue(graph.containsNode("A"));
            assertEquals(1, graph.nodeCount());
        }

        @Test
        void addEdgeImplicitlyCreatesNodes() {
            graph.addEdge("A", "B", 1.0);
            assertTrue(graph.containsNode("A"));
            assertTrue(graph.containsNode("B"));
        }

        @Test
        void negativeWeightThrows() {
            assertThrows(IllegalArgumentException.class, () -> graph.addEdge("A", "B", -1.0));
        }

        @Test
        void neighborsOfUnknownNodeThrows() {
            assertThrows(java.util.NoSuchElementException.class, () -> graph.neighbors("ghost"));
        }

        @Test
        void undirectedEdgeCreatesBothDirections() {
            graph.addUndirectedEdge("A", "B", 2.0);
            assertEquals(1, graph.neighbors("A").size());
            assertEquals(1, graph.neighbors("B").size());
        }
    }

    @Nested
    class BreadthFirstSearch {

        @BeforeEach
        void buildChain() {
            // A -> B -> C -> D  (a simple chain, 3 hops apart end to end)
            graph.addEdge("A", "B", 1);
            graph.addEdge("B", "C", 1);
            graph.addEdge("C", "D", 1);
        }

        @Test
        void zeroHopsReturnsOnlyStart() {
            assertEquals(List.of("A"), graph.relatedWithinHops("A", 0));
        }

        @Test
        void oneHopReturnsImmediateNeighbor() {
            assertEquals(List.of("A", "B"), graph.relatedWithinHops("A", 1));
        }

        @Test
        void moreHopsThanGraphDiameterReturnsEverythingReachable() {
            List<String> result = graph.relatedWithinHops("A", 100);
            assertEquals(4, result.size());
            assertTrue(result.containsAll(List.of("A", "B", "C", "D")));
        }

        @Test
        void negativeHopsThrows() {
            assertThrows(IllegalArgumentException.class, () -> graph.relatedWithinHops("A", -1));
        }

        @Test
        void unknownStartNodeThrows() {
            assertThrows(IllegalArgumentException.class, () -> graph.relatedWithinHops("ghost", 1));
        }

        @Test
        void isolatedNodeReturnsOnlyItself() {
            graph.addNode("isolated");
            assertEquals(List.of("isolated"), graph.relatedWithinHops("isolated", 5));
        }
    }

    @Nested
    class DepthFirstSearch {

        @Test
        void traversesAllReachableNodes() {
            graph.addEdge("A", "B", 1);
            graph.addEdge("A", "C", 1);
            graph.addEdge("B", "D", 1);

            List<String> result = graph.depthFirstTraversal("A");
            assertEquals(4, result.size());
            assertTrue(result.containsAll(List.of("A", "B", "C", "D")));
            assertEquals("A", result.get(0)); // start node always first
        }

        @Test
        void handlesCyclesWithoutInfiniteLoop() {
            graph.addEdge("A", "B", 1);
            graph.addEdge("B", "A", 1); // cycle
            List<String> result = graph.depthFirstTraversal("A");
            assertEquals(2, result.size());
        }
    }

    @Nested
    class DijkstraShortestPath {

        @Test
        void findsCheapestPathNotJustFewestHops() {
            // Direct A->C costs 10, but A->B->C costs 1+1=2 -- Dijkstra must prefer the cheaper path
            graph.addEdge("A", "C", 10);
            graph.addEdge("A", "B", 1);
            graph.addEdge("B", "C", 1);

            List<String> path = graph.shortestPath("A", "C");
            assertEquals(List.of("A", "B", "C"), path);
        }

        @Test
        void unreachableTargetReturnsEmptyList() {
            graph.addNode("A");
            graph.addNode("isolated");
            assertEquals(List.of(), graph.shortestPath("A", "isolated"));
        }

        @Test
        void sameSourceAndTargetReturnsSingleNodePath() {
            graph.addNode("A");
            assertEquals(List.of("A"), graph.shortestPath("A", "A"));
        }

        @Test
        void unknownSourceThrows() {
            graph.addNode("A");
            assertThrows(IllegalArgumentException.class, () -> graph.shortestPath("ghost", "A"));
        }

        @Test
        void unknownTargetThrows() {
            graph.addNode("A");
            assertThrows(IllegalArgumentException.class, () -> graph.shortestPath("A", "ghost"));
        }
    }
}
