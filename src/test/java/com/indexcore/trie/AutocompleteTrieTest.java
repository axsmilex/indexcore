package com.indexcore.trie;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AutocompleteTrieTest {

    private AutocompleteTrie trie;

    @BeforeEach
    void setUp() {
        trie = new AutocompleteTrie();
    }

    @Nested
    class InsertAndContains {

        @Test
        void containsReturnsTrueForInsertedWord() {
            trie.insert("laptop");
            assertTrue(trie.contains("laptop"));
        }

        @Test
        void containsReturnsFalseForNonInsertedWord() {
            trie.insert("laptop");
            assertFalse(trie.contains("laptops"));
        }

        @Test
        void containsReturnsFalseForPrefixThatWasNeverTerminal() {
            trie.insert("laptop");
            assertFalse(trie.contains("lap"));
            assertTrue(trie.hasPrefix("lap"));
        }

        @Test
        void reinsertingWithLowerWeightDoesNotDemote() {
            trie.insert("phone", 100);
            trie.insert("phone", 5);
            List<String> results = trie.topKSuggestions("phone", 1);
            assertEquals(List.of("phone"), results);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void insertRejectsBlankOrEmptyWhereApplicable(String word) {
            if (word.isEmpty()) {
                assertThrows(IllegalArgumentException.class, () -> trie.insert(word));
            } else {
                // a single space IS a valid "word" for this structure -- just documenting the boundary
                assertDoesNotThrow(() -> trie.insert(word));
            }
        }

        @Test
        void insertRejectsNull() {
            assertThrows(IllegalArgumentException.class, () -> trie.insert(null));
        }

        @Test
        void insertRejectsNegativeWeight() {
            assertThrows(IllegalArgumentException.class, () -> trie.insert("tv", -1));
        }
    }

    @Nested
    class TopKSuggestions {

        @BeforeEach
        void seedData() {
            trie.insert("shirt", 10);
            trie.insert("shoes", 50);
            trie.insert("shorts", 30);
            trie.insert("shovel", 5);
        }

        @Test
        void returnsResultsOrderedByDescendingWeight() {
            List<String> results = trie.topKSuggestions("sh", 4);
            assertEquals(List.of("shoes", "shorts", "shirt", "shovel"), results);
        }

        @Test
        void respectsKLimit() {
            List<String> results = trie.topKSuggestions("sh", 2);
            assertEquals(2, results.size());
            assertEquals(List.of("shoes", "shorts"), results);
        }

        @Test
        void returnsEmptyListForUnknownPrefix() {
            assertEquals(List.of(), trie.topKSuggestions("xyz", 5));
        }

        @Test
        void kLargerThanMatchCountReturnsAllMatches() {
            List<String> results = trie.topKSuggestions("sh", 100);
            assertEquals(4, results.size());
        }

        @Test
        void kEqualsZeroReturnsEmptyList() {
            assertEquals(List.of(), trie.topKSuggestions("sh", 0));
        }

        @Test
        void negativeKThrows() {
            assertThrows(IllegalArgumentException.class, () -> trie.topKSuggestions("sh", -1));
        }

        @Test
        void nullPrefixThrows() {
            assertThrows(IllegalArgumentException.class, () -> trie.topKSuggestions(null, 3));
        }

        @Test
        void emptyPrefixMatchesEverything() {
            List<String> results = trie.topKSuggestions("", 10);
            assertEquals(4, results.size());
        }

        @Test
        void exactWordPrefixIncludesItself() {
            trie.insert("shoe", 1); // prefix of "shoes", lower weight
            List<String> results = trie.topKSuggestions("shoe", 10);
            assertTrue(results.contains("shoe"));
            assertTrue(results.contains("shoes"));
        }
    }

    @Nested
    class SizeAndEmpty {

        @Test
        void newTrieIsEmpty() {
            assertTrue(trie.isEmpty());
            assertEquals(0, trie.size());
        }

        @Test
        void sizeCountsDistinctWordsNotNodes() {
            trie.insert("cat");
            trie.insert("car");
            trie.insert("cat"); // duplicate insert should not double-count
            assertEquals(2, trie.size());
        }
    }

    @Nested
    class EditDistance {

        @ParameterizedTest
        @CsvSource({
                "kitten, sitting, 3",
                "flaw, lawn, 2",
                "'', abc, 3",
                "same, same, 0",
                "a, '', 1"
        })
        void computesCorrectLevenshteinDistance(String a, String b, int expected) {
            assertEquals(expected, AutocompleteTrie.editDistance(a, b));
        }

        @Test
        void distanceIsSymmetric() {
            assertEquals(
                    AutocompleteTrie.editDistance("phone", "phony"),
                    AutocompleteTrie.editDistance("phony", "phone")
            );
        }
    }
}
