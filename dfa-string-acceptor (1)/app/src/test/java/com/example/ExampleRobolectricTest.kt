package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DfaDefinition
import com.example.model.DfaEngine
import com.example.model.DfaSimulationResult
import com.example.model.ValidationResult
import com.example.viewmodel.DfaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DFA Acceptor", appName)
    }

    /**
     * Test 1 — Accept
     * DFA:
     * Q = {q0, q1}
     * Σ = {0, 1}
     * q₀ = q0
     * F = {q1}
     * δ(q0,0) = q0
     * δ(q0,1) = q1
     * δ(q1,0) = q0
     * δ(q1,1) = q1
     * Input: 101
     * Expected: ACCEPT
     */
    @Test
    fun `test 1 - accept input 101`() {
        val states = listOf("q0", "q1")
        val alphabet = listOf("0", "1")
        val initialState = "q0"
        val finalStates = setOf("q1")
        val transitions = mapOf(
            Pair("q0", "0") to "q0",
            Pair("q0", "1") to "q1",
            Pair("q1", "0") to "q0",
            Pair("q1", "1") to "q1"
        )

        val validation = DfaEngine.validateDfa(states, alphabet, initialState, finalStates, transitions)
        assertTrue("DFA should be valid", validation is ValidationResult.Valid)

        val dfa = (validation as ValidationResult.Valid).dfa
        val result = DfaEngine.simulate(dfa, "101")

        assertTrue("Result should be success", result is DfaSimulationResult.Success)
        val success = result as DfaSimulationResult.Success
        assertTrue("String '101' should be ACCEPTED", success.isAccepted)
        assertEquals("Final state should be q1", "q1", success.finalState)
        assertEquals("Trace should have 3 transitions", 3, success.steps.size)

        // Trace verification
        assertEquals("Step 0 from q0 on '1' -> q1", "q0", success.steps[0].currentState)
        assertEquals("Step 0 symbol", "1", success.steps[0].symbol)
        assertEquals("Step 0 next", "q1", success.steps[0].nextState)

        assertEquals("Step 1 from q1 on '0' -> q0", "q1", success.steps[1].currentState)
        assertEquals("Step 1 symbol", "0", success.steps[1].symbol)
        assertEquals("Step 1 next", "q0", success.steps[1].nextState)

        assertEquals("Step 2 from q0 on '1' -> q1", "q0", success.steps[2].currentState)
        assertEquals("Step 2 symbol", "1", success.steps[2].symbol)
        assertEquals("Step 2 next", "q1", success.steps[2].nextState)
    }

    /**
     * Test 2 — Reject
     * Same DFA, Input: 100
     * Expected: REJECT
     */
    @Test
    fun `test 2 - reject input 100`() {
        val states = listOf("q0", "q1")
        val alphabet = listOf("0", "1")
        val initialState = "q0"
        val finalStates = setOf("q1")
        val transitions = mapOf(
            Pair("q0", "0") to "q0",
            Pair("q0", "1") to "q1",
            Pair("q1", "0") to "q0",
            Pair("q1", "1") to "q1"
        )

        val dfa = DfaDefinition(states, alphabet, initialState, finalStates, transitions)
        val result = DfaEngine.simulate(dfa, "100")

        assertTrue("Result should be success", result is DfaSimulationResult.Success)
        val success = result as DfaSimulationResult.Success
        assertFalse("String '100' should be REJECTED", success.isAccepted)
        assertEquals("Final state should be q0", "q0", success.finalState)
    }

    /**
     * Test 3 — Invalid Input
     * Alphabet: {0, 1}
     * Input: 102
     * Expected: Error: Input symbol '2' is not present in the alphabet Σ = {0, 1}.
     */
    @Test
    fun `test 3 - invalid symbol in input`() {
        val states = listOf("q0", "q1")
        val alphabet = listOf("0", "1")
        val initialState = "q0"
        val finalStates = setOf("q1")
        val transitions = mapOf(
            Pair("q0", "0") to "q0",
            Pair("q0", "1") to "q1",
            Pair("q1", "0") to "q0",
            Pair("q1", "1") to "q1"
        )

        val dfa = DfaDefinition(states, alphabet, initialState, finalStates, transitions)
        val result = DfaEngine.simulate(dfa, "102")

        assertTrue("Result should be Error", result is DfaSimulationResult.Error)
        val error = result as DfaSimulationResult.Error
        assertTrue("Error should mention symbol '2'", error.message.contains("'2'"))
        assertTrue("Error should mention alphabet", error.message.contains("alphabet"))
    }

    /**
     * Test 4 — Empty String
     * Use a DFA where q0 is a final state.
     * Input: ε
     * Expected: ACCEPT
     */
    @Test
    fun `test 4 - empty string accepted when q0 in F`() {
        val states = listOf("q0", "q1")
        val alphabet = listOf("0", "1")
        val initialState = "q0"
        val finalStates = setOf("q0") // q0 ∈ F
        val transitions = mapOf(
            Pair("q0", "0") to "q1",
            Pair("q0", "1") to "q0",
            Pair("q1", "0") to "q0",
            Pair("q1", "1") to "q1"
        )

        val dfa = DfaDefinition(states, alphabet, initialState, finalStates, transitions)

        // Test with literal "ε"
        val resultEpsilon = DfaEngine.simulate(dfa, "ε")
        assertTrue(resultEpsilon is DfaSimulationResult.Success)
        val successEpsilon = resultEpsilon as DfaSimulationResult.Success
        assertTrue("Empty string ε should be ACCEPTED", successEpsilon.isAccepted)
        assertEquals("q0", successEpsilon.finalState)
        assertEquals(0, successEpsilon.steps.size)

        // Test with blank string ""
        val resultBlank = DfaEngine.simulate(dfa, "")
        assertTrue(resultBlank is DfaSimulationResult.Success)
        val successBlank = resultBlank as DfaSimulationResult.Success
        assertTrue("Blank input should be ACCEPTED as ε", successBlank.isAccepted)
    }

    /**
     * Test Missing Transition Validation
     * δ(q1, 0) missing
     */
    @Test
    fun `test missing transition detection`() {
        val states = listOf("q0", "q1")
        val alphabet = listOf("0", "1")
        val initialState = "q0"
        val finalStates = setOf("q1")
        // Missing δ(q1, 0)
        val transitions = mapOf(
            Pair("q0", "0") to "q0",
            Pair("q0", "1") to "q1",
            Pair("q1", "1") to "q1"
        )

        val validation = DfaEngine.validateDfa(states, alphabet, initialState, finalStates, transitions)
        assertTrue("Validation should fail", validation is ValidationResult.Invalid)
        val invalid = validation as ValidationResult.Invalid
        assertTrue("Should report missing transition for q1, 0", invalid.message.contains("δ(q1, 0)"))
    }

    /**
     * Test ViewModel Integration
     */
    @Test
    fun `test ViewModel runDfa produces accept for default preset`() {
        val viewModel = DfaViewModel()
        viewModel.runDfa()
        val result = viewModel.uiState.value.simulationResult
        assertTrue(result is DfaSimulationResult.Success)
        assertTrue((result as DfaSimulationResult.Success).isAccepted)
    }

    /**
     * Test Given String Accept or Reject feature
     */
    @Test
    fun `test checkGivenString accept and reject`() {
        val states = listOf("q0", "q1")
        val alphabet = listOf("0", "1")
        val initialState = "q0"
        val finalStates = setOf("q1")
        val transitions = mapOf(
            Pair("q0", "0") to "q0",
            Pair("q0", "1") to "q1",
            Pair("q1", "0") to "q0",
            Pair("q1", "1") to "q1"
        )
        val dfa = DfaDefinition(states, alphabet, initialState, finalStates, transitions)

        val acceptCheck = DfaEngine.checkGivenString(dfa, "101")
        assertTrue("Given string 101 must be accepted", acceptCheck.isAccepted)
        assertEquals("q1", acceptCheck.finalState)

        val rejectCheck = DfaEngine.checkGivenString(dfa, "100")
        assertFalse("Given string 100 must be rejected", rejectCheck.isAccepted)
        assertEquals("q0", rejectCheck.finalState)

        // Batch test given strings
        val batch = DfaEngine.batchCheckGivenStrings(dfa, "101, 100, 111, 0, ε")
        assertEquals(5, batch.size)
        assertTrue("101 accepted", batch[0].isAccepted)
        assertFalse("100 rejected", batch[1].isAccepted)
        assertTrue("111 accepted", batch[2].isAccepted)
        assertFalse("0 rejected", batch[3].isAccepted)
        assertFalse("ε rejected", batch[4].isAccepted)
    }
}
