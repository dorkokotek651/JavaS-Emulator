package fx.controller;

import engine.api.SEmulatorEngine;
import engine.api.ExecutionResult;
import engine.execution.ExecutionContext;
import engine.execution.VariableManager;
import fx.model.ExecutionHistoryRow;
import fx.model.VariableTableRow;
import fx.util.ErrorDialogUtil;
import fx.util.StyleManager;
import fx.util.StateInspectionDialog;
import fx.util.ExecutionHistoryManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ExecutionController {
    
    private final SEmulatorEngine engine;
    private Stage primaryStage;
    private int currentExpansionLevel = 0;
    
    private List<TextField> inputFields;
    private TableView<VariableTableRow> variablesTable;
    private Label cyclesLabel;
    private TableView<ExecutionHistoryRow> statisticsTable;
    private ExecutionHistoryManager historyManager;
    
    private Consumer<String> statusUpdater;
    private Runnable onHighlightingCleared;
    
    private boolean debugSessionActive = false;
    private List<Integer> debugOriginalInputs = null;
    private int debugRunNumber = -1;
    private Consumer<Integer> onCurrentInstructionChanged;
    private Consumer<Map<String, Integer>> onVariablesChanged;
    private Runnable onDebugSessionStarted;
    private Runnable onDebugSessionEnded;
    private Consumer<List<Integer>> onInputsPopulated;
    private Consumer<Integer> onExpansionLevelSet;
    private java.util.function.Supplier<engine.api.SProgram> getCurrentContextProgram;
    private Runnable onExecutionCompleted;
    
    public ExecutionController(SEmulatorEngine engine) {
        this.engine = engine;
        this.historyManager = new ExecutionHistoryManager();
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    public void setCurrentExpansionLevel(int level) {
        this.currentExpansionLevel = level;
    }
    
    public void setInputFields(List<TextField> inputFields) {
        this.inputFields = inputFields;
    }
    
    public void setVariablesTable(TableView<VariableTableRow> variablesTable) {
        this.variablesTable = variablesTable;
    }
    
    public void setCyclesLabel(Label cyclesLabel) {
        this.cyclesLabel = cyclesLabel;
    }
    
    public void setStatisticsTable(TableView<ExecutionHistoryRow> statisticsTable) {
        this.statisticsTable = statisticsTable;
    }
    
    public void setStatusUpdater(Consumer<String> statusUpdater) {
        this.statusUpdater = statusUpdater;
    }
    
    public void setOnHighlightingCleared(Runnable onHighlightingCleared) {
        this.onHighlightingCleared = onHighlightingCleared;
    }
    
    public void setInstructionsTable(TableView<?> instructionsTable) {
    }
    
    public void setOnCurrentInstructionChanged(Consumer<Integer> onCurrentInstructionChanged) {
        this.onCurrentInstructionChanged = onCurrentInstructionChanged;
    }
    
    public void setOnVariablesChanged(Consumer<Map<String, Integer>> onVariablesChanged) {
        this.onVariablesChanged = onVariablesChanged;
    }
    
    public void setOnDebugSessionStarted(Runnable onDebugSessionStarted) {
        this.onDebugSessionStarted = onDebugSessionStarted;
    }
    
    public void setOnDebugSessionEnded(Runnable onDebugSessionEnded) {
        this.onDebugSessionEnded = onDebugSessionEnded;
    }
    
    public void resetExecutionState() {
        debugSessionActive = false;
        debugOriginalInputs = null;
    }
    
    public void handleStartRun() {
        if (!engine.isProgramLoaded()) {
            updateStatus("Error: No program loaded");
            return;
        }
        
        try {
            if (!validateInputs()) {
                updateStatus("Error: Please correct invalid input values (must be natural numbers ≥ 0)");
                return;
            }
            
            List<Integer> inputs = collectInputs();
            List<Integer> displayInputs = formatInputsForDisplay(inputs);
            
            int executionLevel = determineExecutionLevel();
            updateStatus("Running program with inputs: " + displayInputs + " at expansion level " + executionLevel);
            
            // Get the next run number for the current context before execution
            int currentRunNumber = historyManager.getNextRunNumber();
            
            System.out.println("Normal run starting: assigning run number " + currentRunNumber + " in context: " + historyManager.getCurrentContext());
            
            engine.api.SProgram contextProgram = getCurrentContextProgram != null ? getCurrentContextProgram.get() : null;
            ExecutionResult result;
            if (contextProgram != null) {
                result = engine.runSpecificProgram(contextProgram, executionLevel, inputs, currentRunNumber);
            } else {
                result = engine.runProgram(executionLevel, inputs);
            }
            
            updateExecutionResults(result, displayInputs, currentRunNumber);
            
            if (onHighlightingCleared != null) {
                onHighlightingCleared.run();
            }
            
            if (onExecutionCompleted != null) {
                onExecutionCompleted.run();
            }
            
            updateStatus("Program execution completed. Y = " + result.getYValue() + ", Total cycles: " + result.getTotalCycles());
            
        } catch (IllegalArgumentException e) {
            updateStatus("Error: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Input Validation Error", e.getMessage());
        } catch (Exception e) {
            updateStatus("Error during execution: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Execution Error", 
                "Failed to execute program: " + e.getMessage());
        }
    }
    
    public void handleStartDebug() {
        if (!engine.isProgramLoaded()) {
            updateStatus("Error: No program loaded");
            return;
        }
        
        if (debugSessionActive) {
            updateStatus("Debug session already active. Stop current session before starting a new one.");
            return;
        }
        
        try {
            if (!validateInputs()) {
                updateStatus("Error: Please correct invalid input values (must be natural numbers ≥ 0)");
                return;
            }
            
            List<Integer> inputs = collectInputs();
            
            int executionLevel = determineExecutionLevel();
            updateStatus("Starting debug session with inputs: " + inputs + " at expansion level " + executionLevel + 
                       " (virtual execution mode enabled for QUOTE instructions)");
            
            // Get the run number for the debug session when it starts
            debugRunNumber = historyManager.getNextRunNumber();
            System.out.println("Debug session starting: assigning run number " + debugRunNumber + " in context: " + historyManager.getCurrentContext());
            
            debugOriginalInputs = new ArrayList<>(inputs);
            
            engine.api.SProgram contextProgram = getCurrentContextProgram != null ? getCurrentContextProgram.get() : null;
            if (contextProgram != null) {
                engine.startDebugSessionForProgram(contextProgram, executionLevel, inputs);
            } else {
                engine.startDebugSession(executionLevel, inputs);
            }
            debugSessionActive = true;
            
            engine.getCurrentExecutionState().enableVirtualExecutionMode();
            

            if (onDebugSessionStarted != null) {
                onDebugSessionStarted.run();
            }
            
            if (onHighlightingCleared != null) {
                onHighlightingCleared.run();
            }
            

            updateDebugVariableDisplay();
            

            if (onCurrentInstructionChanged != null) {
                onCurrentInstructionChanged.accept(0);
            }
            
            updateStatus("Debug session started. Use 'Step Over' to execute instructions step by step.");
            
        } catch (IllegalArgumentException e) {
            updateStatus("Error: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Input Validation Error", e.getMessage());
        } catch (Exception e) {
            debugSessionActive = false;
            updateStatus("Error starting debug session: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Debug Error", 
                "Failed to start debug session: " + e.getMessage());
        }
    }
    
    public void handleStepOver() {
        if (!debugSessionActive) {
            updateStatus("Error: No debug session active. Start debug mode first.");
            return;
        }
        
        try {

            if (!engine.canStepForward()) {
                updateStatus("Program execution completed. No more steps available.");
                handleDebugSessionEnd();
                return;
            }
            

            boolean continueExecution = engine.stepForward();
            

            if (!engine.canStepForward()) {

                updateStatus("Program execution completed.");
                handleDebugSessionEnd();
                return;
            }
            
            if (continueExecution) {

                updateDebugVariableDisplay();
                

                ExecutionContext context = engine.getCurrentExecutionState();
                if (context != null && onCurrentInstructionChanged != null) {
                    onCurrentInstructionChanged.accept(context.getCurrentInstructionIndex());
                }
                

                Map<String, Integer> changedVars = engine.getChangedVariables();
                if (onVariablesChanged != null) {
                    onVariablesChanged.accept(changedVars);
                }
                
                updateStatus("Executed instruction. " + changedVars.size() + " variable(s) changed.");
                
            } else {

                updateStatus("Program execution completed.");
                handleDebugSessionEnd();
            }
            
        } catch (Exception e) {
            updateStatus("Error during step execution: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Debug Step Error", 
                "Failed to execute debug step: " + e.getMessage());
        }
    }
    
    public void handleStop() {
        if (!debugSessionActive) {
            updateStatus("No debug session active to stop.");
            return;
        }
        
        try {

            engine.stopDebugSession();
            debugSessionActive = false;
            debugOriginalInputs = null;
            debugRunNumber = -1;
            

            if (onDebugSessionEnded != null) {
                onDebugSessionEnded.run();
            }
            

            if (onCurrentInstructionChanged != null) {
                onCurrentInstructionChanged.accept(-1);
            }
            

            if (onVariablesChanged != null) {
                onVariablesChanged.accept(new HashMap<>());
            }
            
            updateStatus("Debug session stopped.");
            
        } catch (Exception e) {
            debugSessionActive = false;
            updateStatus("Error stopping debug session: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Debug Stop Error", 
                "Error occurred while stopping debug session: " + e.getMessage());
        }
    }
    
    public void handleResume() {
        if (!debugSessionActive) {
            updateStatus("Error: No debug session active to resume.");
            return;
        }
        
        try {
            updateStatus("Resuming execution to completion...");
            

            ExecutionResult originalResult = engine.resumeExecution();
            System.out.println("engine.resumeExecution() returned ExecutionResult with runNumber: " + originalResult.getRunNumber());
            

            List<Integer> originalInputsForDisplay = debugOriginalInputs != null ? 
                new ArrayList<>(debugOriginalInputs) : null;
            
            debugSessionActive = false;
            debugOriginalInputs = null;
            

            // Use the run number that was assigned when the debug session started
            int currentRunNumber = debugRunNumber;
            
            System.out.println("Debug session completing: using run number " + currentRunNumber + " in context: " + historyManager.getCurrentContext());

            // Create a new ExecutionResult with the correct run number
            ExecutionResult result = new ExecutionResult(
                currentRunNumber,  // Use the correct run number
                originalResult.getExpansionLevel(),
                originalResult.getInputs(),
                originalResult.getYValue(),
                originalResult.getInputVariables(),
                originalResult.getWorkingVariables(),
                originalResult.getTotalCycles(),
                originalResult.getExecutedInstructions()
            );
            
            System.out.println("Created new ExecutionResult with runNumber: " + result.getRunNumber());

            if (onDebugSessionEnded != null) {
                onDebugSessionEnded.run();
            }
            

            updateExecutionResults(result, originalInputsForDisplay, currentRunNumber);
            

            if (onCurrentInstructionChanged != null) {
                onCurrentInstructionChanged.accept(-1);
            }
            

            if (onVariablesChanged != null) {
                onVariablesChanged.accept(new HashMap<>());
            }
            
            if (onHighlightingCleared != null) {
                onHighlightingCleared.run();
            }
            
            if (onExecutionCompleted != null) {
                onExecutionCompleted.run();
            }
            
            updateStatus("Execution resumed and completed. Y = " + result.getYValue() + 
                        ", Total cycles: " + result.getTotalCycles());
            
        } catch (Exception e) {
            debugSessionActive = false;
            updateStatus("Error during resume execution: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Debug Resume Error", 
                "Failed to resume execution: " + e.getMessage());
        }
    }
    
    private List<Integer> collectInputs() {
        List<Integer> inputs = new ArrayList<>();
        
        for (TextField inputField : inputFields) {
            String text = inputField.getText().trim();
            if (text.isEmpty()) {
                inputs.add(0);
            } else {
                try {
                    int value = Integer.parseInt(text);
                    if (value < 0) {

                        StyleManager.applyInputFieldErrorStyle(inputField);
                        throw new NumberFormatException("Negative numbers not allowed");
                    }
                    inputs.add(value);
                } catch (NumberFormatException e) {

                    StyleManager.applyInputFieldErrorStyle(inputField);
                    throw new IllegalArgumentException("Invalid input in field " + (inputFields.indexOf(inputField) + 1) + ": " + text, e);
                }
            }
        }
        
        return inputs;
    }
    
    private List<Integer> formatInputsForDisplay(List<Integer> userInputs) {

        List<Integer> displayInputs = new ArrayList<>();
        

        for (int i = 0; i < inputFields.size(); i++) {
            TextField field = inputFields.get(i);
            if (!field.getText().trim().isEmpty()) {
                if (i < userInputs.size()) {
                    displayInputs.add(userInputs.get(i));
                } else {
                    displayInputs.add(0);
                }
            }
        }
        

        if (displayInputs.isEmpty()) {
            displayInputs.add(0);
        }
        
        return displayInputs;
    }
    
    private boolean validateInputs() {
        boolean allValid = true;
        
        for (TextField inputField : inputFields) {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                try {
                    int value = Integer.parseInt(text);
                    if (value < 0) {
                        StyleManager.applyInputFieldErrorStyle(inputField);
                        allValid = false;
                    } else {
                        StyleManager.applyInputFieldStyle(inputField);
                    }
                } catch (NumberFormatException e) {
                    StyleManager.applyInputFieldErrorStyle(inputField);
                    allValid = false;
                }
            } else {
                StyleManager.applyInputFieldStyle(inputField);
            }
        }
        
        return allValid;
    }
    
    
    private void updateExecutionResults(ExecutionResult result, List<Integer> displayInputs, int runNumber) {
        System.out.println("updateExecutionResults called:");
        System.out.println("  - result.getRunNumber(): " + result.getRunNumber());
        System.out.println("  - passed runNumber parameter: " + runNumber);
        System.out.println("  - result.getYValue(): " + result.getYValue());
        System.out.println("  - result.getTotalCycles(): " + result.getTotalCycles());

        updateVariablesTable(result);
        

        updateCyclesDisplay(result.getTotalCycles());
        

        addToExecutionHistory(result, displayInputs, runNumber);
        
        System.out.println("Execution completed: Run " + result.getRunNumber() + 
                          ", Y = " + result.getYValue() + 
                          ", Cycles = " + result.getTotalCycles());
    }
    
    private void updateVariablesTable(ExecutionResult result) {
        ObservableList<VariableTableRow> variableData = FXCollections.observableArrayList();
        

        Map<String, Integer> inputVars = result.getInputVariables();
        for (Map.Entry<String, Integer> entry : inputVars.entrySet()) {
            variableData.add(new VariableTableRow(entry.getKey(), String.valueOf(entry.getValue())));
        }
        

        Map<String, Integer> workingVars = result.getWorkingVariables();
        for (Map.Entry<String, Integer> entry : workingVars.entrySet()) {
            variableData.add(new VariableTableRow(entry.getKey(), String.valueOf(entry.getValue())));
        }
        

        variableData.add(new VariableTableRow("y", String.valueOf(result.getYValue())));
        

        variableData.sort((v1, v2) -> {
            String name1 = v1.getVariableName();
            String name2 = v2.getVariableName();
            

            if ("y".equals(name1)) {
                return 1;
            }
            if ("y".equals(name2)) {
                return -1;
            }
            

            if (name1.startsWith("x") && name2.startsWith("z")) {
                return -1;
            }
            if (name1.startsWith("z") && name2.startsWith("x")) {
                return 1;
            }
            

            return name1.compareTo(name2);
        });
        
        variablesTable.setItems(variableData);
    }
    
    private void updateCyclesDisplay(int totalCycles) {
        cyclesLabel.setText("Total Cycles: " + totalCycles);
    }
    
    
    
    private void addToExecutionHistory(ExecutionResult result, List<Integer> displayInputs, int runNumber) {
        System.out.println("addToExecutionHistory called:");
        System.out.println("  - result.getRunNumber(): " + result.getRunNumber());
        System.out.println("  - passed runNumber parameter: " + runNumber);
        System.out.println("  - result.getExpansionLevel(): " + result.getExpansionLevel());
        System.out.println("  - result.getYValue(): " + result.getYValue());
        System.out.println("  - result.getTotalCycles(): " + result.getTotalCycles());

        List<Integer> inputsToShow = (displayInputs != null) ? displayInputs : result.getInputs();
        

        String inputsString = inputsToShow.stream()
            .map(String::valueOf)
            .collect(java.util.stream.Collectors.joining(", "));
        

        String actions = "show | re-run";
        
        System.out.println("  - inputsString: " + inputsString);
        System.out.println("  - actions: " + actions);
        System.out.println("  - Calling historyManager.addExecutionResultToCurrentContext with runNumber: " + runNumber);
        
        // Add to context-aware history manager (with full execution result)
        historyManager.addExecutionResultToCurrentContext(
            result,
            runNumber,
            result.getExpansionLevel(),
            inputsString,
            result.getYValue(),
            result.getTotalCycles(),
            actions
        );
        
        // Update the statistics table to show current context history
        updateStatisticsTable();
        
        // Scroll to the newly added row
        if (statisticsTable != null && !historyManager.getCurrentContextHistory().isEmpty()) {
            ExecutionHistoryRow lastRow = historyManager.getCurrentContextHistory().get(
                historyManager.getCurrentContextHistory().size() - 1);
            statisticsTable.scrollTo(lastRow);
        }
    }
    
    private void updateStatus(String message) {
        if (statusUpdater != null) {
            statusUpdater.accept(message);
        }

        System.out.println("ExecutionController Status: " + message);
    }
    
    private void updateDebugVariableDisplay() {
        ExecutionContext context = engine.getCurrentExecutionState();
        if (context == null || variablesTable == null) {
            return;
        }
        
        ObservableList<VariableTableRow> variableData = FXCollections.observableArrayList();
        VariableManager variableManager = context.getVariableManager();
        

        Map<String, Integer> inputVars = variableManager.getSortedInputVariablesMap();
        for (Map.Entry<String, Integer> entry : inputVars.entrySet()) {
            variableData.add(new VariableTableRow(entry.getKey(), String.valueOf(entry.getValue())));
        }
        

        Map<String, Integer> workingVars = variableManager.getSortedWorkingVariablesMap();
        for (Map.Entry<String, Integer> entry : workingVars.entrySet()) {
            variableData.add(new VariableTableRow(entry.getKey(), String.valueOf(entry.getValue())));
        }
        

        variableData.add(new VariableTableRow("y", String.valueOf(variableManager.getYValue())));
        

        variableData.sort((v1, v2) -> {
            String name1 = v1.getVariableName();
            String name2 = v2.getVariableName();
            
            if ("y".equals(name1)) {
                return 1;
            }
            if ("y".equals(name2)) {
                return -1;
            }
            if (name1.startsWith("x") && name2.startsWith("z")) {
                return -1;
            }
            if (name1.startsWith("z") && name2.startsWith("x")) {
                return 1;
            }
            
            return name1.compareTo(name2);
        });
        
        variablesTable.setItems(variableData);
        

        if (cyclesLabel != null) {
            cyclesLabel.setText("Cycles: " + context.getTotalCycles());
        }
    }
    
    private void handleDebugSessionEnd() {
        try {

            ExecutionContext context = engine.getCurrentExecutionState();
            
            if (context != null) {

                updateDebugVariableDisplay();
                
                // Don't add incomplete debug sessions to history when manually stopped
                System.out.println("Debug session manually stopped - not adding to history");
            }
            

            engine.stopDebugSession();
            debugSessionActive = false;
            debugOriginalInputs = null;
            debugRunNumber = -1;
            

            if (onDebugSessionEnded != null) {
                onDebugSessionEnded.run();
            }
            

            if (onCurrentInstructionChanged != null) {
                onCurrentInstructionChanged.accept(-1);
            }
            

            if (onVariablesChanged != null) {
                onVariablesChanged.accept(new HashMap<>());
            }
            
        } catch (Exception e) {
            debugSessionActive = false;
            updateStatus("Error ending debug session: " + e.getMessage());
        }
    }
    
    
    private int determineExecutionLevel() {
        if (!engine.isProgramLoaded()) {
            return currentExpansionLevel;
        }
        


        return currentExpansionLevel;
    }
    
    public void setOnInputsPopulated(Consumer<List<Integer>> onInputsPopulated) {
        this.onInputsPopulated = onInputsPopulated;
    }
    
    public void setOnExpansionLevelSet(Consumer<Integer> onExpansionLevelSet) {
        this.onExpansionLevelSet = onExpansionLevelSet;
    }
    
    public void setGetCurrentContextProgram(java.util.function.Supplier<engine.api.SProgram> getCurrentContextProgram) {
        this.getCurrentContextProgram = getCurrentContextProgram;
    }
    
    public void setOnExecutionCompleted(Runnable onExecutionCompleted) {
        this.onExecutionCompleted = onExecutionCompleted;
    }
    
    /**
     * Sets the current context program/function for execution history management.
     * @param contextName The name of the context (e.g., "Main Program", "Minus", "Const7")
     */
    public void setCurrentContext(String contextName) {
        historyManager.setCurrentContext(contextName);
        updateStatisticsTable();
    }
    
    /**
     * Updates the statistics table to show the current context's execution history.
     */
    private void updateStatisticsTable() {
        if (statisticsTable != null) {
            statisticsTable.setItems(historyManager.getCurrentContextHistory());
        }
    }
    
    public void handleShowHistoricalState(ExecutionHistoryRow historyRow) {
        if (historyRow == null) {
            updateStatus("Error: No historical run selected");
            return;
        }
        
        try {
            int runNumber = Integer.parseInt(historyRow.getRunNumber());
            int expansionLevel = Integer.parseInt(historyRow.getExpansionLevel());
            
            System.out.println("Show button clicked for run #" + runNumber + " in context: " + historyManager.getCurrentContext());
            
            // Use context-aware execution results
            List<ExecutionResult> contextExecutionResults = historyManager.getCurrentContextExecutionResults();
            System.out.println("Available execution results in context: " + contextExecutionResults.size());
            for (ExecutionResult result : contextExecutionResults) {
                System.out.println("  - Run #" + result.getRunNumber() + " at level " + result.getExpansionLevel());
            }
            
            ExecutionResult targetResult = null;
            
            // Find the execution result by run number and expansion level
            for (ExecutionResult result : contextExecutionResults) {
                if (result.getRunNumber() == runNumber && result.getExpansionLevel() == expansionLevel) {
                    targetResult = result;
                    break;
                }
            }
            
            if (targetResult == null) {
                updateStatus("Error: Could not find historical execution result for run #" + runNumber + " in context: " + historyManager.getCurrentContext());
                ErrorDialogUtil.showError(primaryStage, "Historical State Error", 
                    "Could not find execution result for run #" + runNumber + " in context: " + historyManager.getCurrentContext());
                return;
            }
            
            StateInspectionDialog.showHistoricalState(primaryStage, targetResult, runNumber);
            updateStatus("Displayed historical state for run #" + runNumber);
            
        } catch (NumberFormatException e) {
            updateStatus("Error: Invalid run number format");
            ErrorDialogUtil.showError(primaryStage, "Invalid Data", 
                "Invalid run number format: " + historyRow.getRunNumber());
        } catch (Exception e) {
            updateStatus("Error showing historical state: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Historical State Error", 
                "Failed to show historical state: " + e.getMessage());
        }
    }
    
    public void handleRerunHistoricalExecution(ExecutionHistoryRow historyRow) {
        if (historyRow == null) {
            updateStatus("Error: No historical run selected");
            return;
        }
        
        // Clear variables table when re-running
        if (variablesTable != null) {
            variablesTable.setItems(FXCollections.observableArrayList());
        }
        
        try {
            int runNumber = Integer.parseInt(historyRow.getRunNumber());
            int expansionLevel = Integer.parseInt(historyRow.getExpansionLevel());
            
            // Parse the inputs from the history row (context-aware)
            String inputsString = historyRow.getInputs();
            List<Integer> historicalInputs = new ArrayList<>();
            
            if (inputsString != null && !inputsString.trim().isEmpty()) {
                String[] inputParts = inputsString.split(",");
                for (String part : inputParts) {
                    try {
                        historicalInputs.add(Integer.parseInt(part.trim()));
                    } catch (NumberFormatException e) {
                        updateStatus("Warning: Could not parse input value: " + part.trim());
                    }
                }
            }
            
            if (historicalInputs.isEmpty()) {
                updateStatus("Error: No valid inputs found in historical run #" + runNumber);
                ErrorDialogUtil.showError(primaryStage, "Re-run Error", 
                    "No valid inputs found in historical run #" + runNumber);
                return;
            }
            
            if (onInputsPopulated != null) {
                onInputsPopulated.accept(historicalInputs);
            }
            
            if (onExpansionLevelSet != null) {
                onExpansionLevelSet.accept(expansionLevel);
            }
            
            updateStatus("Populated inputs from run #" + runNumber + ". Ready to re-run with inputs: " + historicalInputs);
            
        } catch (NumberFormatException e) {
            updateStatus("Error: Invalid run number format");
            ErrorDialogUtil.showError(primaryStage, "Invalid Data", 
                "Invalid run number format: " + historyRow.getRunNumber());
        } catch (Exception e) {
            updateStatus("Error preparing re-run: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Re-run Error", 
                "Failed to prepare re-run: " + e.getMessage());
        }
    }
    
    public void clearExecutionHistory() {
        historyManager.clearAllHistory();
        updateStatisticsTable();
        updateStatus("Execution history cleared");
    }
    
}
