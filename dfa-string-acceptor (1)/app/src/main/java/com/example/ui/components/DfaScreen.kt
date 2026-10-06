package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.model.DfaPresets
import com.example.model.DfaSimulationResult
import com.example.model.TransitionStep
import com.example.ui.theme.AcceptGreen
import com.example.ui.theme.AcceptGreenContainer
import com.example.ui.theme.AcceptGreenOnContainer
import com.example.ui.theme.RejectRed
import com.example.ui.theme.RejectRedContainer
import com.example.ui.theme.RejectRedOnContainer
import com.example.viewmodel.DfaUiState
import com.example.viewmodel.DfaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfaScreen(
    viewModel: DfaViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "DFA String Acceptor",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "M = (Q, Σ, δ, q₀, F)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearAll() },
                        modifier = Modifier.testTag("clear_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset / Clear All",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            DfaPersistentBottomBar(
                simulationResult = uiState.simulationResult,
                inputString = uiState.inputString,
                onRunDfa = {
                    viewModel.runDfa()
                    coroutineScope.launch {
                        listState.animateScrollToItem(7)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Preset Selection Carousel
            item {
                PresetSelectorSection(
                    uiState = uiState,
                    onSelectPreset = { preset, index -> viewModel.loadPreset(preset, index) }
                )
            }

            // Section 1: States Q
            item {
                StatesSection(
                    statesText = uiState.statesText,
                    parsedStates = uiState.parsedStates,
                    onStatesChanged = viewModel::onStatesChanged
                )
            }

            // Section 2: Alphabet Σ
            item {
                AlphabetSection(
                    alphabetText = uiState.alphabetText,
                    parsedAlphabet = uiState.parsedAlphabet,
                    onAlphabetChanged = viewModel::onAlphabetChanged
                )
            }

            // Section 3: Initial State q₀ & Final States F
            item {
                InitialAndFinalStatesSection(
                    parsedStates = uiState.parsedStates,
                    initialState = uiState.initialStateText,
                    finalStates = uiState.parsedFinalStates,
                    finalStatesText = uiState.finalStatesText,
                    onInitialStateChanged = viewModel::onInitialStateChanged,
                    onToggleFinalState = viewModel::toggleFinalState,
                    onFinalStatesTextChanged = viewModel::onFinalStatesChanged
                )
            }

            // Section 4: Transition Function δ
            item {
                TransitionFunctionSection(
                    states = uiState.parsedStates,
                    alphabet = uiState.parsedAlphabet,
                    transitionsMap = uiState.transitionsMap,
                    missingTransitions = uiState.missingTransitions,
                    onTransitionChanged = viewModel::setTransition
                )
            }

            // Section 5: Given String w (Accept or Reject)
            item {
                InputStringSection(
                    inputString = uiState.inputString,
                    alphabet = uiState.parsedAlphabet,
                    onInputChanged = viewModel::onInputStringChanged,
                    onAppendSymbol = viewModel::appendInputSymbol,
                    onBackspace = viewModel::backspaceInput,
                    onClear = viewModel::clearInput,
                    onSetEmpty = viewModel::setEmptyString,
                    onCheckCondition = {
                        viewModel.runDfa()
                        coroutineScope.launch {
                            listState.animateScrollToItem(6)
                        }
                    }
                )
            }

            // Section 6: Given String Accept or Reject Output Card
            item {
                StringAcceptanceDecisionCard(
                    simulationResult = uiState.simulationResult,
                    inputString = uiState.inputString,
                    onViewTrace = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(9)
                        }
                    }
                )
            }

            // Section 7: Batch Test Multiple Given Strings (Accept or Reject Table)
            item {
                BatchGivenStringsSection(
                    batchText = uiState.batchInputStringsText,
                    results = uiState.batchResults,
                    onBatchTextChanged = viewModel::onBatchStringsChanged,
                    onSelectString = { selected ->
                        viewModel.onInputStringChanged(selected)
                        coroutineScope.launch {
                            listState.animateScrollToItem(9)
                        }
                    }
                )
            }

            // Section 8: Action Button RUN DFA & INSPECT TRACE
            item {
                Button(
                    onClick = {
                        viewModel.runDfa()
                        coroutineScope.launch {
                            listState.animateScrollToItem(9)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("run_dfa_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RUN DFA & VIEW TRACE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Section 9: Simulation Results & Transition Trace
            item {
                uiState.simulationResult?.let { result ->
                    SimulationResultCard(
                        result = result,
                        selectedStep = uiState.selectedStepIndex,
                        onStepSelected = viewModel::selectStep
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PresetSelectorSection(
    uiState: DfaUiState,
    onSelectPreset: (com.example.model.DfaPreset, Int) -> Unit
) {
    Column {
        Text(
            text = "Quick Presets & Test Cases",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(DfaPresets.presets) { index, preset ->
                val isSelected = uiState.selectedPresetIndex == index
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectPreset(preset, index) },
                    label = {
                        Text(
                            text = preset.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("preset_chip_$index")
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatesSection(
    statesText: String,
    parsedStates: List<String>,
    onStatesChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "1. States Q",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${parsedStates.size} states",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Finite set of states, separated by commas (e.g. q0, q1, q2)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = statesText,
                onValueChange = onStatesChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("states_input_field"),
                placeholder = { Text("e.g. q0, q1, q2") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (parsedStates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    parsedStates.forEach { state ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = state,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlphabetSection(
    alphabetText: String,
    parsedAlphabet: List<String>,
    onAlphabetChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "2. Alphabet Σ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${parsedAlphabet.size} symbols",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = "Valid input symbols, separated by commas (e.g. 0, 1 or a, b)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = alphabetText,
                onValueChange = onAlphabetChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alphabet_input_field"),
                placeholder = { Text("e.g. 0, 1") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (parsedAlphabet.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    parsedAlphabet.forEach { symbol ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "'$symbol'",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InitialAndFinalStatesSection(
    parsedStates: List<String>,
    initialState: String,
    finalStates: Set<String>,
    finalStatesText: String,
    onInitialStateChanged: (String) -> Unit,
    onToggleFinalState: (String) -> Unit,
    onFinalStatesTextChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "3. Initial State q₀ & Final States F",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Initial State q0
            Text(
                text = "Initial State q₀ (must be in Q)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (parsedStates.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    parsedStates.forEach { st ->
                        val isSelected = st == initialState
                        FilterChip(
                            selected = isSelected,
                            onClick = { onInitialStateChanged(st) },
                            label = { Text("q₀ = $st") },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.testTag("initial_state_chip_$st")
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = initialState,
                    onValueChange = onInitialStateChanged,
                    placeholder = { Text("e.g. q0") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Final States F
            Text(
                text = "Final States F ⊆ Q (tap to toggle accept states)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (parsedStates.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    parsedStates.forEach { st ->
                        val isFinal = finalStates.contains(st)
                        FilterChip(
                            selected = isFinal,
                            onClick = { onToggleFinalState(st) },
                            label = { Text(if (isFinal) "◎ $st (Accept)" else "○ $st") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AcceptGreenContainer,
                                selectedLabelColor = AcceptGreenOnContainer
                            ),
                            modifier = Modifier.testTag("final_state_chip_$st")
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = finalStatesText,
                    onValueChange = onFinalStatesTextChanged,
                    placeholder = { Text("e.g. q1 or q1, q2") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "F = {${finalStates.joinToString(", ")}}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun TransitionFunctionSection(
    states: List<String>,
    alphabet: List<String>,
    transitionsMap: Map<Pair<String, String>, String>,
    missingTransitions: List<Pair<String, String>>,
    onTransitionChanged: (String, String, String) -> Unit
) {
    val totalRequired = states.size * alphabet.size
    val definedCount = totalRequired - missingTransitions.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "4. Transition Function δ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (missingTransitions.isEmpty() && totalRequired > 0)
                        AcceptGreenContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (totalRequired == 0) "Define Q & Σ" else "$definedCount / $totalRequired defined",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (missingTransitions.isEmpty() && totalRequired > 0)
                            AcceptGreenOnContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "δ(current state, input symbol) = next state",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (states.isEmpty() || alphabet.isEmpty()) {
                Text(
                    text = "Please enter States Q and Alphabet Σ above to configure transitions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                // Table layout of transitions
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    states.forEach { st ->
                        alphabet.forEach { sym ->
                            val currentNext = transitionsMap[Pair(st, sym)].orEmpty()
                            val isMissing = currentNext.isBlank()

                            TransitionEntryRow(
                                currentState = st,
                                symbol = sym,
                                nextState = currentNext,
                                availableStates = states,
                                isMissing = isMissing,
                                onSelectNextState = { next ->
                                    onTransitionChanged(st, sym, next)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransitionEntryRow(
    currentState: String,
    symbol: String,
    nextState: String,
    availableStates: List<String>,
    isMissing: Boolean,
    onSelectNextState: (String) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isMissing) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "δ(",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = currentState,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = ", ",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = symbol,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    text = ") = ",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Box {
                OutlinedButton(
                    onClick = { dropdownExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("transition_btn_${currentState}_$symbol")
                ) {
                    Text(
                        text = if (nextState.isBlank()) "Select..." else nextState,
                        fontWeight = FontWeight.Bold,
                        color = if (nextState.isBlank()) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    availableStates.forEach { optState ->
                        DropdownMenuItem(
                            text = { Text(optState, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                onSelectNextState(optState)
                                dropdownExpanded = false
                            },
                            modifier = Modifier.testTag("dropdown_item_${currentState}_${symbol}_$optState")
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InputStringSection(
    inputString: String,
    alphabet: List<String>,
    onInputChanged: (String) -> Unit,
    onAppendSymbol: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSetEmpty: () -> Unit,
    onCheckCondition: () -> Unit
) {
    val quickSampleStrings = remember(alphabet) {
        if (alphabet.contains("a") || alphabet.contains("b")) {
            listOf("abba", "a", "b", "ba", "ε")
        } else {
            listOf("101", "100", "111", "0", "ε")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "5. Given String w",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (inputString == "ε" || inputString.isEmpty()) "Length: 0 (Empty ε)" else "Length: ${inputString.length}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = "Enter any given string w to determine if it is ACCEPT or REJECT:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = inputString,
                onValueChange = onInputChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_string_field"),
                placeholder = { Text("e.g. 10101 (or leave blank for ε)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Given String Samples
            Text(
                text = "Sample Given Strings to Test:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickSampleStrings.forEach { sample ->
                    FilterChip(
                        selected = inputString == sample,
                        onClick = { onInputChanged(sample) },
                        label = {
                            Text(
                                text = "w = $sample",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        modifier = Modifier.testTag("quick_sample_chip_$sample")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mobile Keypad for rapid input
            Text(
                text = "Quick Symbol Pad:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                alphabet.forEach { sym ->
                    Button(
                        onClick = { onAppendSymbol(sym) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("symbol_btn_$sym")
                    ) {
                        Text(
                            text = sym,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Empty string button
                OutlinedButton(
                    onClick = onSetEmpty,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("empty_string_btn")
                ) {
                    Text("ε (Empty)", fontWeight = FontWeight.SemiBold)
                }

                // Backspace button
                IconButton(
                    onClick = onBackspace,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("backspace_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Backspace",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Clear button
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("clear_input_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onCheckCondition,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("check_condition_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FactCheck,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CHECK CONDITION (ACCEPT OR REJECT)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun SimulationResultCard(
    result: DfaSimulationResult,
    selectedStep: Int,
    onStepSelected: (Int) -> Unit
) {
    when (result) {
        is DfaSimulationResult.Error -> {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_error"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = RejectRedContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = RejectRed,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Validation / Execution Error",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RejectRedOnContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = RejectRedOnContainer
                        )
                    }
                }
            }
        }

        is DfaSimulationResult.Success -> {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Banner for ACCEPT / REJECT
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(if (result.isAccepted) "result_accept" else "result_reject"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (result.isAccepted) AcceptGreenContainer else RejectRedContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (result.isAccepted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (result.isAccepted) AcceptGreen else RejectRed,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (result.isAccepted) "Result: ACCEPT" else "Result: REJECT",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (result.isAccepted) AcceptGreenOnContainer else RejectRedOnContainer,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Final State: ${result.finalState}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (result.isAccepted) AcceptGreenOnContainer else RejectRedOnContainer
                        )

                        Text(
                            text = if (result.isAccepted) {
                                "State '${result.finalState}' belongs to final states F = {${result.finalStates.joinToString(", ")}}"
                            } else {
                                "State '${result.finalState}' does NOT belong to final states F = {${result.finalStates.joinToString(", ")}}"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (result.isAccepted) AcceptGreenOnContainer.copy(alpha = 0.85f) else RejectRedOnContainer.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Transition Trace Card
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transition_trace_table"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transition Trace",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Input w = '${result.inputString}'",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (result.isEmptyString) {
                            // Empty string explanation
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Empty string ε processed (0 transitions):",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "• Starts at initial state: ${result.initialState}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "• Transitions executed: 0",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "• Final reached state: ${result.finalState}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = if (result.isAccepted)
                                            "• Because ${result.initialState} ∈ F, string is ACCEPTED."
                                        else
                                            "• Because ${result.initialState} ∉ F, string is REJECTED.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (result.isAccepted) AcceptGreen else RejectRed
                                    )
                                }
                            }
                        } else {
                            // Step Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Step",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Current",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1.5f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Symbol",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1.2f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Next State",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1.5f),
                                    textAlign = TextAlign.End
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Step Table Rows
                            result.steps.forEachIndexed { index, step ->
                                val isHighlighted = selectedStep == index

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            else if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                            else Color.Transparent
                                        )
                                        .clickable { onStepSelected(index) }
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$index",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = step.currentState,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1.5f),
                                        textAlign = TextAlign.Center
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Text(
                                            text = step.symbol,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = step.nextState,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1.5f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }

                            // Interactive Step Navigator
                            if (result.steps.isNotEmpty() && selectedStep in result.steps.indices) {
                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))

                                val curStep = result.steps[selectedStep]
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Step $selectedStep Inspector",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "δ( ",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = curStep.currentState,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = " , ",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "'${curStep.symbol}'",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = " ) ──► ",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = curStep.nextState,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (selectedStep == result.steps.lastIndex && result.isAccepted) AcceptGreen else MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            OutlinedButton(
                                                onClick = { if (selectedStep > 0) onStepSelected(selectedStep - 1) },
                                                enabled = selectedStep > 0,
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.NavigateBefore, contentDescription = null)
                                                Text("Prev")
                                            }

                                            Text(
                                                text = "${selectedStep + 1} of ${result.steps.size}",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.align(Alignment.CenterVertically)
                                            )

                                            OutlinedButton(
                                                onClick = { if (selectedStep < result.steps.lastIndex) onStepSelected(selectedStep + 1) },
                                                enabled = selectedStep < result.steps.lastIndex,
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text("Next")
                                                Icon(Icons.Default.NavigateNext, contentDescription = null)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StringAcceptanceDecisionCard(
    simulationResult: DfaSimulationResult?,
    inputString: String,
    onViewTrace: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("string_acceptance_decision_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (simulationResult) {
                is DfaSimulationResult.Success -> if (simulationResult.isAccepted) AcceptGreenContainer else RejectRedContainer
                is DfaSimulationResult.Error -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GIVEN STRING: ACCEPT OR REJECT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = when (simulationResult) {
                        is DfaSimulationResult.Success -> if (simulationResult.isAccepted) AcceptGreenOnContainer else RejectRedOnContainer
                        is DfaSimulationResult.Error -> MaterialTheme.colorScheme.onErrorContainer
                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (simulationResult is DfaSimulationResult.Success) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (simulationResult.isAccepted) AcceptGreen else RejectRed,
                        modifier = Modifier.testTag("string_output_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (simulationResult.isAccepted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (simulationResult.isAccepted) "ACCEPT" else "REJECT",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (simulationResult) {
                is DfaSimulationResult.Success -> {
                    Text(
                        text = if (simulationResult.isAccepted) "The given string is ACCEPTED" else "The given string is REJECTED",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (simulationResult.isAccepted) AcceptGreenOnContainer else RejectRedOnContainer
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (simulationResult.isAccepted) {
                            "Given string w = \"${simulationResult.inputString}\" terminates at final state '${simulationResult.finalState}' ∈ F. Result: ACCEPT."
                        } else {
                            "Given string w = \"${simulationResult.inputString}\" terminates at state '${simulationResult.finalState}' ∉ F = {${simulationResult.finalStates.joinToString(", ")}}. Result: REJECT."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (simulationResult.isAccepted) AcceptGreenOnContainer.copy(alpha = 0.9f) else RejectRedOnContainer.copy(alpha = 0.9f),
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Acceptance Condition Verification Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("condition_breakdown_card"),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Acceptance Condition Verification:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Condition 1: Alphabet Condition
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AcceptGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "1. Alphabet: All symbols in '${simulationResult.inputString}' ∈ Σ [PASSED]",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Condition 2: Transition Path
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AcceptGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2. Path: δ*(q₀, '${simulationResult.inputString}') = ${simulationResult.finalState} (${simulationResult.steps.size} transitions) [PASSED]",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Condition 3: Final State Membership Condition
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (simulationResult.isAccepted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (simulationResult.isAccepted) AcceptGreen else RejectRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (simulationResult.isAccepted) {
                                        "3. Final State Condition: ${simulationResult.finalState} ∈ F {${simulationResult.finalStates.joinToString(", ")}} [CONDITION MET: ACCEPT]"
                                    } else {
                                        "3. Final State Condition: ${simulationResult.finalState} ∉ F {${simulationResult.finalStates.joinToString(", ")}} [CONDITION NOT MET: REJECT]"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (simulationResult.isAccepted) AcceptGreen else RejectRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onViewTrace,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (simulationResult.isAccepted) AcceptGreenOnContainer else RejectRedOnContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View Step-by-Step Trace (${simulationResult.steps.size} steps)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                is DfaSimulationResult.Error -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = simulationResult.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                null -> {
                    Text(
                        text = "Enter input string w and tap 'RUN DFA' to evaluate acceptance.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BatchGivenStringsSection(
    batchText: String,
    results: List<com.example.model.GivenStringCheckResult>,
    onBatchTextChanged: (String) -> Unit,
    onSelectString: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("batch_given_strings_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "7. Multi-String Acceptance Tester",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${results.size} Strings Tested",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Enter or paste given strings separated by commas to test whether each is ACCEPT or REJECT:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = batchText,
                onValueChange = onBatchTextChanged,
                placeholder = { Text("e.g. 101, 100, 111, 0, ε") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("batch_strings_input_field"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (results.isNotEmpty()) {
                Text(
                    text = "Accept / Reject Results:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    results.forEachIndexed { index, item ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("batch_result_item_$index"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (item.isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                else if (item.isAccepted) AcceptGreenContainer.copy(alpha = 0.35f)
                                else RejectRedContainer.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Given String: ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = "\"${item.givenString}\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    if (!item.isError) {
                                        Text(
                                            text = "Final State: ${item.finalState}  (${item.transitionCount} transitions)",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Text(
                                            text = item.errorMessage ?: "Invalid Symbol",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (item.isError) MaterialTheme.colorScheme.error
                                        else if (item.isAccepted) AcceptGreen
                                        else RejectRed
                                    ) {
                                        Text(
                                            text = if (item.isError) "ERROR" else if (item.isAccepted) "ACCEPT" else "REJECT",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = { onSelectString(item.givenString) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("select_batch_string_$index")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "Test in trace",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DfaPersistentBottomBar(
    simulationResult: DfaSimulationResult?,
    inputString: String,
    onRunDfa: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dfa_persistent_bottom_bar"),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "w = \"${if (inputString.isEmpty()) "ε" else inputString}\"",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                when (simulationResult) {
                    is DfaSimulationResult.Success -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (simulationResult.isAccepted) AcceptGreenContainer else RejectRedContainer
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (simulationResult.isAccepted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (simulationResult.isAccepted) AcceptGreen else RejectRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (simulationResult.isAccepted) "ACCEPT" else "REJECT",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (simulationResult.isAccepted) AcceptGreenOnContainer else RejectRedOnContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "-> ${simulationResult.finalState}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is DfaSimulationResult.Error -> {
                        Text(
                            text = "Invalid DFA or Symbol",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    null -> {
                        Text(
                            text = "Ready to evaluate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onRunDfa,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("bottom_bar_run_dfa_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "RUN DFA",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
