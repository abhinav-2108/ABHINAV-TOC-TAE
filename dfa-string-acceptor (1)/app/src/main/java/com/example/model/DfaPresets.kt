package com.example.model

data class DfaPreset(
    val title: String,
    val description: String,
    val states: String,
    val alphabet: String,
    val initialState: String,
    val finalStates: String,
    val transitions: Map<Pair<String, String>, String>,
    val sampleInput: String
)

object DfaPresets {
    val presets = listOf(
        DfaPreset(
            title = "Binary ends with 1 (Test 1 & 2)",
            description = "Accepts binary strings ending in 1. Tests '101' (Accept) and '100' (Reject).",
            states = "q0, q1",
            alphabet = "0, 1",
            initialState = "q0",
            finalStates = "q1",
            transitions = mapOf(
                Pair("q0", "0") to "q0",
                Pair("q0", "1") to "q1",
                Pair("q1", "0") to "q0",
                Pair("q1", "1") to "q1"
            ),
            sampleInput = "101"
        ),
        DfaPreset(
            title = "Even number of 0s (Test 4 - Accepts ε)",
            description = "Accepts strings with an even number of 0s. Initial state q0 ∈ F, so empty string ε is ACCEPTED.",
            states = "q0, q1",
            alphabet = "0, 1",
            initialState = "q0",
            finalStates = "q0",
            transitions = mapOf(
                Pair("q0", "0") to "q1",
                Pair("q0", "1") to "q0",
                Pair("q1", "0") to "q0",
                Pair("q1", "1") to "q1"
            ),
            sampleInput = "ε"
        ),
        DfaPreset(
            title = "Contains substring '101'",
            description = "Accepts binary strings containing the substring 101. Final state is q3.",
            states = "q0, q1, q2, q3",
            alphabet = "0, 1",
            initialState = "q0",
            finalStates = "q3",
            transitions = mapOf(
                Pair("q0", "0") to "q0",
                Pair("q0", "1") to "q1",
                Pair("q1", "0") to "q2",
                Pair("q1", "1") to "q1",
                Pair("q2", "0") to "q0",
                Pair("q2", "1") to "q3",
                Pair("q3", "0") to "q3",
                Pair("q3", "1") to "q3"
            ),
            sampleInput = "11010"
        ),
        DfaPreset(
            title = "Alphabet {a, b} - Starts with 'a'",
            description = "Demonstrates custom alphabet {a, b}. Accepts any string starting with 'a'.",
            states = "q0, q1, q2",
            alphabet = "a, b",
            initialState = "q0",
            finalStates = "q1",
            transitions = mapOf(
                Pair("q0", "a") to "q1",
                Pair("q0", "b") to "q2",
                Pair("q1", "a") to "q1",
                Pair("q1", "b") to "q1",
                Pair("q2", "a") to "q2",
                Pair("q2", "b") to "q2"
            ),
            sampleInput = "abba"
        )
    )
}
