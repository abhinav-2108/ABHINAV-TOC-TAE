package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.DfaEngine
import com.example.model.DfaPreset
import com.example.model.DfaPresets
import com.example.model.DfaSimulationResult
import com.example.model.GivenStringCheckResult
import com.example.model.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DfaUiState(
    val statesText: String = "q0, q1",
    val alphabetText: String = "0, 1",
    val initialStateText: String = "q0",
    val finalStatesText: String = "q1",
    val transitionsMap: Map<Pair<String, String>, String> = mapOf(
        Pair("q0", "0") to "q0",
        Pair("q0", "1") to "q1",
        Pair("q1", "0") to "q0",
        Pair("q1", "1") to "q1"
    ),
    val inputString: String = "101",
    val simulationResult: DfaSimulationResult? = null,
    val selectedStepIndex: Int = -1,
    val selectedPresetIndex: Int = 0,
    val batchInputStringsText: String = "101, 100, 111, 0, ε",
    val batchResults: List<GivenStringCheckResult> = emptyList(),
    val showBatchTester: Boolean = true
) {
    val parsedStates: List<String> get() = DfaEngine.parseStates(statesText)
    val parsedAlphabet: List<String> get() = DfaEngine.parseAlphabet(alphabetText)
    val parsedFinalStates: Set<String>
        get() = DfaEngine.parseStates(finalStatesText).toSet()

    /**
     * Identifies any (state, symbol) pairs from Q × Σ that have not been filled out yet.
     */
    val missingTransitions: List<Pair<String, String>>
        get() {
            val list = mutableListOf<Pair<String, String>>()
            for (st in parsedStates) {
                for (sym in parsedAlphabet) {
                    val next = transitionsMap[Pair(st, sym)]
                    if (next.isNullOrBlank()) {
                        list.add(Pair(st, sym))
                    }
                }
            }
            return list
        }
}

class DfaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DfaUiState())
    val uiState: StateFlow<DfaUiState> = _uiState.asStateFlow()

    init {
        runDfa()
    }

    private fun recomputeAll(state: DfaUiState): DfaUiState {
        val states = state.parsedStates
        val alphabet = state.parsedAlphabet
        val initialState = state.initialStateText
        val finalStates = state.parsedFinalStates
        val transitions = state.transitionsMap

        val validation = DfaEngine.validateDfa(
            states = states,
            alphabet = alphabet,
            initialState = initialState,
            finalStates = finalStates,
            transitions = transitions
        )

        return when (validation) {
            is ValidationResult.Invalid -> state.copy(
                simulationResult = DfaSimulationResult.Error(validation.message),
                batchResults = emptyList()
            )
            is ValidationResult.Valid -> {
                val sim = DfaEngine.simulate(validation.dfa, state.inputString)
                val batch = DfaEngine.batchCheckGivenStrings(validation.dfa, state.batchInputStringsText)
                state.copy(
                    simulationResult = sim,
                    batchResults = batch
                )
            }
        }
    }

    fun onStatesChanged(text: String) {
        _uiState.update { current ->
            recomputeAll(current.copy(statesText = text))
        }
    }

    fun onAlphabetChanged(text: String) {
        _uiState.update { current ->
            recomputeAll(current.copy(alphabetText = text))
        }
    }

    fun onInitialStateChanged(state: String) {
        _uiState.update { current ->
            recomputeAll(current.copy(initialStateText = state.trim()))
        }
    }

    fun onFinalStatesChanged(text: String) {
        _uiState.update { current ->
            recomputeAll(current.copy(finalStatesText = text))
        }
    }

    fun toggleFinalState(state: String) {
        _uiState.update { current ->
            val currentFinals = current.parsedFinalStates.toMutableSet()
            if (currentFinals.contains(state)) {
                currentFinals.remove(state)
            } else {
                currentFinals.add(state)
            }
            val updated = current.copy(finalStatesText = currentFinals.joinToString(", "))
            recomputeAll(updated)
        }
    }

    fun setTransition(state: String, symbol: String, nextState: String) {
        _uiState.update { current ->
            val newTransitions = current.transitionsMap.toMutableMap()
            if (nextState.isBlank()) {
                newTransitions.remove(Pair(state, symbol))
            } else {
                newTransitions[Pair(state, symbol)] = nextState.trim()
            }
            val updated = current.copy(transitionsMap = newTransitions)
            recomputeAll(updated)
        }
    }

    fun onInputStringChanged(input: String) {
        _uiState.update { current ->
            val updated = current.copy(inputString = input, selectedStepIndex = -1)
            recomputeAll(updated)
        }
    }

    fun appendInputSymbol(symbol: String) {
        _uiState.update { current ->
            val currentInput = if (current.inputString == "ε") "" else current.inputString
            val updated = current.copy(
                inputString = currentInput + symbol,
                selectedStepIndex = -1
            )
            recomputeAll(updated)
        }
    }

    fun backspaceInput() {
        _uiState.update { current ->
            val currentInput = if (current.inputString == "ε") "" else current.inputString
            if (currentInput.isNotEmpty()) {
                val updated = current.copy(
                    inputString = currentInput.dropLast(1),
                    selectedStepIndex = -1
                )
                recomputeAll(updated)
            } else {
                current
            }
        }
    }

    fun clearInput() {
        _uiState.update { current ->
            val updated = current.copy(
                inputString = "",
                selectedStepIndex = -1
            )
            recomputeAll(updated)
        }
    }

    fun setEmptyString() {
        _uiState.update { current ->
            val updated = current.copy(
                inputString = "ε",
                selectedStepIndex = -1
            )
            recomputeAll(updated)
        }
    }

    fun onBatchStringsChanged(text: String) {
        _uiState.update { current ->
            val updated = current.copy(batchInputStringsText = text)
            recomputeAll(updated)
        }
    }

    fun toggleBatchTester() {
        _uiState.update { it.copy(showBatchTester = !it.showBatchTester) }
    }

    fun selectStep(index: Int) {
        _uiState.update { current ->
            current.copy(selectedStepIndex = index)
        }
    }

    fun loadPreset(preset: DfaPreset, presetIndex: Int) {
        val newState = DfaUiState(
            statesText = preset.states,
            alphabetText = preset.alphabet,
            initialStateText = preset.initialState,
            finalStatesText = preset.finalStates,
            transitionsMap = preset.transitions,
            inputString = preset.sampleInput,
            simulationResult = null,
            selectedStepIndex = -1,
            selectedPresetIndex = presetIndex,
            batchInputStringsText = if (preset.alphabet == "a, b") "abba, a, b, ba, ε" else "101, 100, 111, 0, ε"
        )
        _uiState.value = recomputeAll(newState)
    }

    fun clearAll() {
        _uiState.update {
            DfaUiState(
                statesText = "",
                alphabetText = "",
                initialStateText = "",
                finalStatesText = "",
                transitionsMap = emptyMap(),
                inputString = "",
                simulationResult = null,
                selectedStepIndex = -1,
                selectedPresetIndex = -1,
                batchInputStringsText = "",
                batchResults = emptyList()
            )
        }
    }

    /**
     * Executes the DFA algorithm:
     * 1. Validate the user-defined DFA
     * 2. Read input string symbol-by-symbol
     * 3. Apply δ(currentState, a)
     * 4. Record transition trace
     * 5. Check if final state ∈ F
     * 6. Produce ACCEPT or REJECT
     */
    fun runDfa() {
        _uiState.update { current ->
            val recomputed = recomputeAll(current)
            val stepIdx = if (recomputed.simulationResult is DfaSimulationResult.Success && recomputed.simulationResult.steps.isNotEmpty()) 0 else -1
            recomputed.copy(selectedStepIndex = stepIdx)
        }
    }
}
