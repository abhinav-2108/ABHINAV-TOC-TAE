package com.example.model

/**
 * Represents the formal 5-tuple M = (Q, Σ, δ, q₀, F) of a Deterministic Finite Automaton.
 */
data class DfaDefinition(
    val states: List<String>,
    val alphabet: List<String>,
    val initialState: String,
    val finalStates: Set<String>,
    val transitions: Map<Pair<String, String>, String>
)

/**
 * A single recorded state transition in the execution trace.
 * Step i: δ(currentState, symbol) = nextState
 */
data class TransitionStep(
    val stepIndex: Int,
    val currentState: String,
    val symbol: String,
    val nextState: String
)

/**
 * Validation result for user DFA specification.
 */
sealed interface ValidationResult {
    data class Valid(val dfa: DfaDefinition) : ValidationResult
    data class Invalid(val message: String) : ValidationResult
}

/**
 * DFA simulation result for an input string w.
 */
sealed interface DfaSimulationResult {
    data class Success(
        val isAccepted: Boolean,
        val inputString: String,
        val initialState: String,
        val finalState: String,
        val finalStates: Set<String>,
        val steps: List<TransitionStep>,
        val isEmptyString: Boolean
    ) : DfaSimulationResult

    data class Error(val message: String) : DfaSimulationResult
}

/**
 * Result of checking a given string for Accept or Reject.
 */
data class GivenStringCheckResult(
    val givenString: String,
    val isAccepted: Boolean,
    val finalState: String,
    val transitionCount: Int,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Pure business logic engine for parsing, validating, and simulating a DFA.
 */
object DfaEngine {

    /**
     * Parses a comma-separated or space-separated string of state names into a list of unique names.
     */
    fun parseStates(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        val tokens = if (raw.contains(",")) {
            raw.split(",")
        } else {
            raw.trim().split(Regex("\\s+"))
        }
        return tokens.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    }

    /**
     * Parses alphabet symbols.
     * Supports comma-separated (e.g. "0, 1" or "a, b") or space-separated or individual characters.
     */
    fun parseAlphabet(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        val tokens = if (raw.contains(",")) {
            raw.split(",")
        } else if (raw.trim().contains(" ")) {
            raw.trim().split(Regex("\\s+"))
        } else {
            // Sequence of single chars e.g. "01" or "ab"
            raw.trim().map { it.toString() }
        }
        return tokens.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    }

    /**
     * Validates the 5-tuple M = (Q, Σ, δ, q₀, F).
     */
    fun validateDfa(
        states: List<String>,
        alphabet: List<String>,
        initialState: String,
        finalStates: Set<String>,
        transitions: Map<Pair<String, String>, String>
    ): ValidationResult {
        // 1. States Q validation
        if (states.isEmpty()) {
            return ValidationResult.Invalid("Error: Set of states Q cannot be empty.")
        }

        // 2. Alphabet Σ validation
        if (alphabet.isEmpty()) {
            return ValidationResult.Invalid("Error: Alphabet Σ cannot be empty.")
        }

        // 3. Initial State q₀ validation
        if (initialState.isBlank()) {
            return ValidationResult.Invalid("Error: Initial state q₀ is not specified.")
        }
        if (!states.contains(initialState)) {
            return ValidationResult.Invalid("Error: Initial state '$initialState' does not exist in Q = {${states.joinToString(", ")}}.")
        }

        // 4. Final States F validation
        for (f in finalStates) {
            if (!states.contains(f)) {
                return ValidationResult.Invalid("Error: Final state '$f' does not exist in Q = {${states.joinToString(", ")}}.")
            }
        }

        // 5. Transitions δ validation: Every pair (state, symbol) must have a valid next state in Q
        for (state in states) {
            for (symbol in alphabet) {
                val nextState = transitions[Pair(state, symbol)]
                if (nextState.isNullOrBlank()) {
                    return ValidationResult.Invalid("Error: Transition δ($state, $symbol) is missing.")
                }
                if (!states.contains(nextState)) {
                    return ValidationResult.Invalid("Error: Transition δ($state, $symbol) = '$nextState' is invalid because '$nextState' is not in Q.")
                }
            }
        }

        return ValidationResult.Valid(
            DfaDefinition(
                states = states,
                alphabet = alphabet,
                initialState = initialState,
                finalStates = finalStates,
                transitions = transitions
            )
        )
    }

    /**
     * Simulates the validated DFA on input string w.
     */
    fun simulate(dfa: DfaDefinition, rawInput: String): DfaSimulationResult {
        val trimmed = rawInput.trim()
        val isEmptyString = trimmed.isEmpty() || trimmed == "ε" || trimmed == "\\epsilon" || trimmed == "e" && !dfa.alphabet.contains("e")

        // Case: Empty String ε
        if (isEmptyString) {
            val currentState = dfa.initialState
            val isAccepted = dfa.finalStates.contains(currentState)
            return DfaSimulationResult.Success(
                isAccepted = isAccepted,
                inputString = "ε",
                initialState = dfa.initialState,
                finalState = currentState,
                finalStates = dfa.finalStates,
                steps = emptyList(),
                isEmptyString = true
            )
        }

        // Parse input symbols.
        // If alphabet consists of single-character symbols, we split input into chars.
        // If alphabet has multi-character symbols, we can match prefixes.
        val symbols: List<String> = parseInputString(trimmed, dfa.alphabet)
            ?: return DfaSimulationResult.Error("Error: Input contains symbols not in the alphabet Σ = {${dfa.alphabet.joinToString(", ")}}.")

        // Check if any individual symbol in symbols is outside Σ
        for (sym in symbols) {
            if (!dfa.alphabet.contains(sym)) {
                return DfaSimulationResult.Error("Error: Input symbol '$sym' is not present in the alphabet Σ = {${dfa.alphabet.joinToString(", ")}}.")
            }
        }

        var currentState = dfa.initialState
        val steps = mutableListOf<TransitionStep>()

        for ((index, symbol) in symbols.withIndex()) {
            val nextState = dfa.transitions[Pair(currentState, symbol)]
                ?: return DfaSimulationResult.Error("Error: Transition δ($currentState, $symbol) is missing.")

            steps.add(
                TransitionStep(
                    stepIndex = index,
                    currentState = currentState,
                    symbol = symbol,
                    nextState = nextState
                )
            )
            currentState = nextState
        }

        val isAccepted = dfa.finalStates.contains(currentState)

        return DfaSimulationResult.Success(
            isAccepted = isAccepted,
            inputString = trimmed,
            initialState = dfa.initialState,
            finalState = currentState,
            finalStates = dfa.finalStates,
            steps = steps,
            isEmptyString = false
        )
    }

    /**
     * Splits an input string into symbols according to the alphabet.
     * Checks for invalid characters immediately.
     */
    fun parseInputString(input: String, alphabet: List<String>): List<String>? {
        if (input.isEmpty()) return emptyList()

        // Standard case: alphabet is 1-char symbols (e.g. '0', '1', 'a', 'b')
        val allSingleChar = alphabet.all { it.length == 1 }
        if (allSingleChar) {
            val result = mutableListOf<String>()
            for (ch in input) {
                val s = ch.toString()
                result.add(s)
            }
            return result
        }

        // Greedy matching for multi-char alphabet tokens if any
        val sortedAlphabet = alphabet.sortedByDescending { it.length }
        val result = mutableListOf<String>()
        var index = 0
        while (index < input.length) {
            val matched = sortedAlphabet.firstOrNull { input.startsWith(it, index) }
            if (matched != null) {
                result.add(matched)
                index += matched.length
            } else {
                // Char not matching any alphabet symbol
                result.add(input[index].toString())
                index++
            }
        }
        return result
    }

    /**
     * Checks whether a single given string is accepted or rejected by the DFA.
     */
    fun checkGivenString(dfa: DfaDefinition, rawInput: String): GivenStringCheckResult {
        return when (val sim = simulate(dfa, rawInput)) {
            is DfaSimulationResult.Success -> {
                GivenStringCheckResult(
                    givenString = rawInput.trim().ifEmpty { "ε" },
                    isAccepted = sim.isAccepted,
                    finalState = sim.finalState,
                    transitionCount = sim.steps.size,
                    isError = false,
                    errorMessage = null
                )
            }
            is DfaSimulationResult.Error -> {
                GivenStringCheckResult(
                    givenString = rawInput.trim().ifEmpty { "ε" },
                    isAccepted = false,
                    finalState = "-",
                    transitionCount = 0,
                    isError = true,
                    errorMessage = sim.message
                )
            }
        }
    }

    /**
     * Checks multiple given strings at once and returns the list of Accept/Reject results.
     */
    fun batchCheckGivenStrings(dfa: DfaDefinition, rawStringsText: String): List<GivenStringCheckResult> {
        if (rawStringsText.isBlank()) return emptyList()
        val strings = if (rawStringsText.contains(",")) {
            rawStringsText.split(",")
        } else if (rawStringsText.contains("\n")) {
            rawStringsText.split("\n")
        } else {
            rawStringsText.trim().split(Regex("\\s+"))
        }

        return strings.map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { str -> checkGivenString(dfa, str) }
    }
}
