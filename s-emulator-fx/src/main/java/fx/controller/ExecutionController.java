package fx.controller;

import engine.api.SEmulatorEngine;
import engine.api.ExecutionResult;
import engine.exception.SProgramException;
import engine.execution.ExecutionContext;
import engine.execution.VariableManager;
import fx.model.ExecutionHistoryRow;
import fx.model.VariableTableRow;
import fx.util.ErrorDialogUtil;
import fx.util.StyleManager;
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

/**
 * Controller responsible for program execution and debugging in the S-Emulator application.
 * Handles execution flow, input validation, result display, and execution history.
 */
public class ExecutionController {
    
    private final SEmulatorEngine engine;
    private Stage primaryStage;
    private int currentExpansionLevel = 0;
    private int nextLocalRunNumber = 1; // Track run numbers locally for debug sessions
    
    // UI Components (injected from MainController)
    private List<TextField> inputFields;
    private TableView<VariableTableRow> variablesTable;
    private Label cyclesLabel;
    private TableView<ExecutionHistoryRow> statisticsTable;
    private ObservableList<ExecutionHistoryRow> executionHistory;
    
    // Callbacks for communication with main controller
    private Consumer<String> statusUpdater;
    private Runnable onHighlightingCleared;
    
    // Debug mode state
    private boolean debugSessionActive = false;
    private List<Integer> debugOriginalInputs = null; // Store original inputs for debug sessions
    private TableView<?> instructionsTable;
    private Consumer<Integer> onCurrentInstructionChanged;
    private Consumer<Map<String, Integer>> onVariablesChanged;
    private Runnable onDebugSessionStarted;
    private Runnable onDebugSessionEnded;
    
    /**
     * Creates a new ExecutionController.
     * 
     * @param engine the S-Emulator engine instance
     */
    public ExecutionController(SEmulatorEngine engine) {
        this.engine = engine;
    }
    
    /**
     * Sets the primary stage for error dialogs.
     * 
     * @param primaryStage the primary stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Sets the current expansion level.
     * 
     * @param level the expansion level
     */
    public void setCurrentExpansionLevel(int level) {
        this.currentExpansionLevel = level;
    }
    
    /**
     * Sets the input fields reference.
     * 
     * @param inputFields the list of input text fields
     */
    public void setInputFields(List<TextField> inputFields) {
        this.inputFields = inputFields;
    }
    
    /**
     * Sets the variables table reference.
     * 
     * @param variablesTable the variables table view
     */
    public void setVariablesTable(TableView<VariableTableRow> variablesTable) {
        this.variablesTable = variablesTable;
    }
    
    /**
     * Sets the cycles label reference.
     * 
     * @param cyclesLabel the cycles display label
     */
    public void setCyclesLabel(Label cyclesLabel) {
        this.cyclesLabel = cyclesLabel;
    }
    
    /**
     * Sets the statistics table reference.
     * 
     * @param statisticsTable the statistics table view
     */
    public void setStatisticsTable(TableView<ExecutionHistoryRow> statisticsTable) {
        this.statisticsTable = statisticsTable;
    }
    
    /**
     * Sets the execution history list reference.
     * 
     * @param executionHistory the execution history observable list
     */
    public void setExecutionHistory(ObservableList<ExecutionHistoryRow> executionHistory) {
        this.executionHistory = executionHistory;
    }
    
    /**
     * Sets the status update callback.
     * 
     * @param statusUpdater callback to update status messages
     */
    public void setStatusUpdater(Consumer<String> statusUpdater) {
        this.statusUpdater = statusUpdater;
    }
    
    /**
     * Sets the highlighting cleared callback.
     * 
     * @param onHighlightingCleared callback when highlighting should be cleared
     */
    public void setOnHighlightingCleared(Runnable onHighlightingCleared) {
        this.onHighlightingCleared = onHighlightingCleared;
    }
    
    /**
     * Sets the instructions table for debug highlighting.
     * 
     * @param instructionsTable the instructions table view
     */
    public void setInstructionsTable(TableView<?> instructionsTable) {
        this.instructionsTable = instructionsTable;
    }
    
    /**
     * Sets the callback for current instruction changes during debug mode.
     * 
     * @param onCurrentInstructionChanged callback that receives the current instruction index
     */
    public void setOnCurrentInstructionChanged(Consumer<Integer> onCurrentInstructionChanged) {
        this.onCurrentInstructionChanged = onCurrentInstructionChanged;
    }
    
    /**
     * Sets the callback for variable changes during debug mode.
     * 
     * @param onVariablesChanged callback that receives the changed variables map
     */
    public void setOnVariablesChanged(Consumer<Map<String, Integer>> onVariablesChanged) {
        this.onVariablesChanged = onVariablesChanged;
    }
    
    /**
     * Sets the callback for when a debug session starts.
     * 
     * @param onDebugSessionStarted callback when debug session starts
     */
    public void setOnDebugSessionStarted(Runnable onDebugSessionStarted) {
        this.onDebugSessionStarted = onDebugSessionStarted;
    }
    
    /**
     * Sets the callback for when a debug session ends.
     * 
     * @param onDebugSessionEnded callback when debug session ends
     */
    public void setOnDebugSessionEnded(Runnable onDebugSessionEnded) {
        this.onDebugSessionEnded = onDebugSessionEnded;
    }
    
    /**
     * Resets the execution state including run number counter.
     * Called when program state is cleared.
     */
    public void resetExecutionState() {
        nextLocalRunNumber = 1;
        debugSessionActive = false;
        debugOriginalInputs = null;
    }
    
    /**
     * Handles start run request from user.
     */
    public void handleStartRun() {
        if (!engine.isProgramLoaded()) {
            updateStatus("Error: No program loaded");
            return;
        }
        
        try {
            // Validate and collect inputs
            if (!validateInputs()) {
                updateStatus("Error: Please correct invalid input values (must be natural numbers ≥ 0)");
                return;
            }
            
            List<Integer> inputs = collectInputs();
            List<Integer> displayInputs = formatInputsForDisplay(inputs);
            
            // Determine the execution level - always use current level for virtual execution
            int executionLevel = determineExecutionLevel();
            updateStatus("Running program with inputs: " + displayInputs + " at expansion level " + executionLevel);
            
            // Execute the program
            ExecutionResult result = engine.runProgram(executionLevel, inputs);
            
            // Use current local run number for this execution
            int currentRunNumber = nextLocalRunNumber;
            
            // Synchronize local run number with engine run number, but only if engine is ahead
            // This prevents debug sessions from being overwritten by regular runs
            nextLocalRunNumber = Math.max(nextLocalRunNumber + 1, result.getRunNumber() + 1);
            
            // Update UI with results, passing the formatted inputs for display
            updateExecutionResults(result, displayInputs, currentRunNumber);
            
            // Clear any previous highlighting
            if (onHighlightingCleared != null) {
                onHighlightingCleared.run();
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
    
    /**
     * Handles start debug request from user.
     */
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
            // Validate and collect inputs
            if (!validateInputs()) {
                updateStatus("Error: Please correct invalid input values (must be natural numbers ≥ 0)");
                return;
            }
            
            List<Integer> inputs = collectInputs();
            
            // Determine the execution level - always use current level for virtual execution
            int executionLevel = determineExecutionLevel();
            updateStatus("Starting debug session with inputs: " + inputs + " at expansion level " + executionLevel + 
                       " (virtual execution mode enabled for QUOTE instructions)");
            
            // Store original inputs for later use in history
            debugOriginalInputs = new ArrayList<>(inputs);
            
            // Start debug session
            engine.startDebugSession(executionLevel, inputs);
            debugSessionActive = true;
            
            // Enable virtual execution mode for QUOTE instructions
            engine.getCurrentExecutionState().enableVirtualExecutionMode();
            
            // Notify UI that debug session started
            if (onDebugSessionStarted != null) {
                onDebugSessionStarted.run();
            }
            
            // Clear any previous highlighting first
            if (onHighlightingCleared != null) {
                onHighlightingCleared.run();
            }
            
            // Update variable display with initial state
            updateDebugVariableDisplay();
            
            // Highlight first instruction
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
    
    /**
     * Handles step over request from user.
     */
    public void handleStepOver() {
        if (!debugSessionActive) {
            updateStatus("Error: No debug session active. Start debug mode first.");
            return;
        }
        
        try {
            // Check if we can step forward
            if (!engine.canStepForward()) {
                updateStatus("Program execution completed. No more steps available.");
                handleDebugSessionEnd();
                return;
            }
            
            // Execute single instruction
            boolean continueExecution = engine.stepForward();
            
            // Check if we can still step forward after execution
            if (!engine.canStepForward()) {
                // Program ended after this instruction
                updateStatus("Program execution completed.");
                handleDebugSessionEnd();
                return;
            }
            
            if (continueExecution) {
                // Update variable display with changes
                updateDebugVariableDisplay();
                
                // Get current instruction index and highlight it
                ExecutionContext context = engine.getCurrentExecutionState();
                if (context != null && onCurrentInstructionChanged != null) {
                    onCurrentInstructionChanged.accept(context.getCurrentInstructionIndex());
                }
                
                // Show changed variables
                Map<String, Integer> changedVars = engine.getChangedVariables();
                if (onVariablesChanged != null) {
                    onVariablesChanged.accept(changedVars);
                }
                
                updateStatus("Executed instruction. " + changedVars.size() + " variable(s) changed.");
                
            } else {
                // Program ended
                updateStatus("Program execution completed.");
                handleDebugSessionEnd();
            }
            
        } catch (Exception e) {
            updateStatus("Error during step execution: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Debug Step Error", 
                "Failed to execute debug step: " + e.getMessage());
        }
    }
    
    /**
     * Handles stop request from user.
     */
    public void handleStop() {
        if (!debugSessionActive) {
            updateStatus("No debug session active to stop.");
            return;
        }
        
        try {
            // Stop the debug session
            engine.stopDebugSession();
            debugSessionActive = false;
            debugOriginalInputs = null;
            
            // Notify UI that debug session ended
            if (onDebugSessionEnded != null) {
                onDebugSessionEnded.run();
            }
            
            // Clear instruction highlighting
            if (onCurrentInstructionChanged != null) {
                onCurrentInstructionChanged.accept(-1); // -1 indicates no highlighting
            }
            
            // Clear variable change highlighting
            if (onVariablesChanged != null) {
                onVariablesChanged.accept(new HashMap<>());
            }
            
            updateStatus("Debug session stopped.");
            
        } catch (Exception e) {
            debugSessionActive = false; // Ensure state is consistent
            updateStatus("Error stopping debug session: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Debug Stop Error", 
                "Error occurred while stopping debug session: " + e.getMessage());
        }
    }
    
    /**
     * Handles resume request from user.
     */
    public void handleResume() {
        if (!debugSessionActive) {
            updateStatus("Error: No debug session active to resume.");
            return;
        }
        
        try {
            updateStatus("Resuming execution to completion...");
            
            // Resume execution
            ExecutionResult result = engine.resumeExecution();
            
            // Store original inputs before clearing them
            List<Integer> originalInputsForDisplay = debugOriginalInputs != null ? 
                new ArrayList<>(debugOriginalInputs) : null;
            
            debugSessionActive = false;
            debugOriginalInputs = null;
            
            // Use current local run number for this execution (debug session that completed)
            int currentRunNumber = nextLocalRunNumber;
            
            // Synchronize local run number with engine run number, but only if engine is ahead
            nextLocalRunNumber = Math.max(nextLocalRunNumber + 1, result.getRunNumber() + 1);
            
            // Notify UI that debug session ended
            if (onDebugSessionEnded != null) {
                onDebugSessionEnded.run();
            }
            
            // Update UI with final results using local run number and original inputs
            updateExecutionResults(result, originalInputsForDisplay, currentRunNumber);
            
            // Clear instruction highlighting
            if (onCurrentInstructionChanged != null) {
                onCurrentInstructionChanged.accept(-1); // -1 indicates no highlighting
            }
            
            // Clear variable change highlighting
            if (onVariablesChanged != null) {
                onVariablesChanged.accept(new HashMap<>());
            }
            
            // Clear any previous highlighting
            if (onHighlightingCleared != null) {
                onHighlightingCleared.run();
            }
            
            updateStatus("Execution resumed and completed. Y = " + result.getYValue() + 
                        ", Total cycles: " + result.getTotalCycles());
            
        } catch (Exception e) {
            debugSessionActive = false; // Ensure state is consistent
            updateStatus("Error during resume execution: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Debug Resume Error", 
                "Failed to resume execution: " + e.getMessage());
        }
    }
    
    /**
     * Collects input values from input fields exactly as the user provided them.
     * 
     * @return list of integer inputs, with 0 as default for empty fields
     */
    private List<Integer> collectInputs() {
        List<Integer> inputs = new ArrayList<>();
        
        for (TextField inputField : inputFields) {
            String text = inputField.getText().trim();
            if (text.isEmpty()) {
                inputs.add(0); // Default value for empty fields
            } else {
                try {
                    int value = Integer.parseInt(text);
                    if (value < 0) {
                        // Highlight invalid field with error styling
                        StyleManager.applyInputFieldErrorStyle(inputField);
                        throw new NumberFormatException("Negative numbers not allowed");
                    }
                    inputs.add(value);
                } catch (NumberFormatException e) {
                    // Highlight invalid field with error styling
                    StyleManager.applyInputFieldErrorStyle(inputField);
                    throw new IllegalArgumentException("Invalid input in field " + (inputFields.indexOf(inputField) + 1) + ": " + text);
                }
            }
        }
        
        return inputs;
    }
    
    /**
     * Formats inputs for display in history.
     * Shows only the non-empty inputs that the user actually provided.
     * 
     * @param userInputs the inputs collected from UI fields
     * @return formatted input list for display
     */
    private List<Integer> formatInputsForDisplay(List<Integer> userInputs) {
        // Only show inputs that were actually provided (non-empty fields)
        List<Integer> displayInputs = new ArrayList<>();
        
        // Count how many non-empty fields there were
        int nonEmptyFields = 0;
        for (int i = 0; i < inputFields.size(); i++) {
            TextField field = inputFields.get(i);
            if (!field.getText().trim().isEmpty()) {
                nonEmptyFields++;
                if (i < userInputs.size()) {
                    displayInputs.add(userInputs.get(i));
                } else {
                    displayInputs.add(0);
                }
            }
        }
        
        // If no fields had values, show one 0
        if (displayInputs.isEmpty()) {
            displayInputs.add(0);
        }
        
        return displayInputs;
    }
    
    /**
     * Validates all input fields and returns true if all are valid.
     * 
     * @return true if all inputs are valid natural numbers, false otherwise
     */
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
    
    /**
     * Updates the UI with execution results.
     * 
     * @param result the execution result to display
     */
    private void updateExecutionResults(ExecutionResult result) {
        updateExecutionResults(result, null);
    }
    
    /**
     * Updates the UI with execution results.
     * 
     * @param result the execution result to display
     * @param displayInputs the inputs to show in history (null to use result inputs)
     */
    private void updateExecutionResults(ExecutionResult result, List<Integer> displayInputs) {
        updateExecutionResults(result, displayInputs, result.getRunNumber());
    }
    
    /**
     * Updates the UI with execution results using a specific run number.
     * 
     * @param result the execution result to display
     * @param displayInputs the inputs to show in history (null to use result inputs)
     * @param runNumber the run number to use for this execution
     */
    private void updateExecutionResults(ExecutionResult result, List<Integer> displayInputs, int runNumber) {
        // Update variables table
        updateVariablesTable(result);
        
        // Update cycles display
        updateCyclesDisplay(result.getTotalCycles());
        
        // Add to execution history using specified run number
        addToExecutionHistory(result, displayInputs, runNumber);
        
        System.out.println("Execution completed: Run " + result.getRunNumber() + 
                          ", Y = " + result.getYValue() + 
                          ", Cycles = " + result.getTotalCycles());
    }
    
    /**
     * Updates the variables table with execution results.
     * 
     * @param result the execution result containing variable values
     */
    private void updateVariablesTable(ExecutionResult result) {
        ObservableList<VariableTableRow> variableData = FXCollections.observableArrayList();
        
        // Add input variables (x1, x2, x3, ...)
        Map<String, Integer> inputVars = result.getInputVariables();
        for (Map.Entry<String, Integer> entry : inputVars.entrySet()) {
            variableData.add(new VariableTableRow(entry.getKey(), String.valueOf(entry.getValue())));
        }
        
        // Add working variables (z1, z2, z3, ...)
        Map<String, Integer> workingVars = result.getWorkingVariables();
        for (Map.Entry<String, Integer> entry : workingVars.entrySet()) {
            variableData.add(new VariableTableRow(entry.getKey(), String.valueOf(entry.getValue())));
        }
        
        // Add y variable (result)
        variableData.add(new VariableTableRow("y", String.valueOf(result.getYValue())));
        
        // Sort variables in proper order: x1, x2, ..., z1, z2, ..., y
        variableData.sort((v1, v2) -> {
            String name1 = v1.getVariableName();
            String name2 = v2.getVariableName();
            
            // y always comes last
            if ("y".equals(name1)) return 1;
            if ("y".equals(name2)) return -1;
            
            // x variables come before z variables
            if (name1.startsWith("x") && name2.startsWith("z")) return -1;
            if (name1.startsWith("z") && name2.startsWith("x")) return 1;
            
            // Within same type, sort numerically
            return name1.compareTo(name2);
        });
        
        variablesTable.setItems(variableData);
    }
    
    /**
     * Updates the cycles display with the total execution cycles.
     * 
     * @param totalCycles the total number of cycles executed
     */
    private void updateCyclesDisplay(int totalCycles) {
        cyclesLabel.setText("Total Cycles: " + totalCycles);
    }
    
    /**
     * Adds an execution result to the execution history table.
     * 
     * @param result the execution result to add
     */
    private void addToExecutionHistory(ExecutionResult result) {
        addToExecutionHistory(result, null);
    }
    
    /**
     * Adds an execution result to the execution history table.
     * 
     * @param result the execution result to add
     * @param displayInputs the inputs to show in history (null to use result inputs)
     */
    private void addToExecutionHistory(ExecutionResult result, List<Integer> displayInputs) {
        addToExecutionHistory(result, displayInputs, result.getRunNumber());
    }
    
    /**
     * Adds an execution result to the execution history table with a specific run number.
     * 
     * @param result the execution result to add
     * @param displayInputs the inputs to show in history (null to use result inputs)
     * @param runNumber the run number to use for this entry
     */
    private void addToExecutionHistory(ExecutionResult result, List<Integer> displayInputs, int runNumber) {
        // Use display inputs if provided, otherwise use result inputs
        List<Integer> inputsToShow = (displayInputs != null) ? displayInputs : result.getInputs();
        
        // Format inputs as comma-separated string
        String inputsString = inputsToShow.stream()
            .map(String::valueOf)
            .collect(java.util.stream.Collectors.joining(", "));
        
        // For Phase 4, use placeholder for actions (will be implemented in Phase 6)
        String actions = "show | re-run";
        
        ExecutionHistoryRow historyRow = new ExecutionHistoryRow(
            String.valueOf(runNumber),
            String.valueOf(result.getExpansionLevel()),
            inputsString,
            String.valueOf(result.getYValue()),
            String.valueOf(result.getTotalCycles()),
            actions
        );
        
        executionHistory.add(historyRow);
        
        // Scroll to the new row for user visibility
        statisticsTable.scrollTo(historyRow);
    }
    
    /**
     * Updates status through the registered callback.
     * 
     * @param message the status message
     */
    private void updateStatus(String message) {
        if (statusUpdater != null) {
            statusUpdater.accept(message);
        }
        // Also print to console for debugging
        System.out.println("ExecutionController Status: " + message);
    }
    
    /**
     * Updates the variable display during debug mode.
     */
    private void updateDebugVariableDisplay() {
        ExecutionContext context = engine.getCurrentExecutionState();
        if (context == null || variablesTable == null) {
            return;
        }
        
        ObservableList<VariableTableRow> variableData = FXCollections.observableArrayList();
        VariableManager variableManager = context.getVariableManager();
        
        // Add input variables (x1, x2, x3, ...)
        Map<String, Integer> inputVars = variableManager.getSortedInputVariablesMap();
        for (Map.Entry<String, Integer> entry : inputVars.entrySet()) {
            variableData.add(new VariableTableRow(entry.getKey(), String.valueOf(entry.getValue())));
        }
        
        // Add working variables (z1, z2, z3, ...)
        Map<String, Integer> workingVars = variableManager.getSortedWorkingVariablesMap();
        for (Map.Entry<String, Integer> entry : workingVars.entrySet()) {
            variableData.add(new VariableTableRow(entry.getKey(), String.valueOf(entry.getValue())));
        }
        
        // Add y variable (result)
        variableData.add(new VariableTableRow("y", String.valueOf(variableManager.getYValue())));
        
        // Sort variables in proper order: x1, x2, ..., z1, z2, ..., y
        variableData.sort((v1, v2) -> {
            String name1 = v1.getVariableName();
            String name2 = v2.getVariableName();
            
            if (name1.equals("y")) return 1;
            if (name2.equals("y")) return -1;
            if (name1.startsWith("x") && name2.startsWith("z")) return -1;
            if (name1.startsWith("z") && name2.startsWith("x")) return 1;
            
            return name1.compareTo(name2);
        });
        
        variablesTable.setItems(variableData);
        
        // Update cycles display
        if (cyclesLabel != null) {
            cyclesLabel.setText("Cycles: " + context.getTotalCycles());
        }
    }
    
    /**
     * Handles the end of a debug session.
     */
    private void handleDebugSessionEnd() {
        try {
            // Get final execution state before stopping session
            ExecutionContext context = engine.getCurrentExecutionState();
            
            if (context != null) {
                // Update final variable display
                updateDebugVariableDisplay();
                
                // Add to execution history
                addDebugSessionToHistory(context);
            }
            
            // Stop debug session
            engine.stopDebugSession();
            debugSessionActive = false;
            debugOriginalInputs = null;
            
            // Notify UI that debug session ended
            if (onDebugSessionEnded != null) {
                onDebugSessionEnded.run();
            }
            
            // Clear instruction highlighting
            if (onCurrentInstructionChanged != null) {
                onCurrentInstructionChanged.accept(-1);
            }
            
            // Clear variable change highlighting
            if (onVariablesChanged != null) {
                onVariablesChanged.accept(new HashMap<>());
            }
            
        } catch (Exception e) {
            debugSessionActive = false; // Ensure state is consistent
            updateStatus("Error ending debug session: " + e.getMessage());
        }
    }
    
    /**
     * Adds debug session results to execution history.
     */
    private void addDebugSessionToHistory(ExecutionContext context) {
        try {
            VariableManager variableManager = context.getVariableManager();
            
            // Use stored original inputs from when debug session started
            List<Integer> originalInputs = debugOriginalInputs != null ? 
                debugOriginalInputs : new ArrayList<>();
            
            // If no stored inputs, fallback to collecting from variable manager
            if (originalInputs.isEmpty()) {
                Map<String, Integer> inputVars = variableManager.getSortedInputVariablesMap();
                for (int i = 1; i <= inputVars.size(); i++) {
                    String varName = "x" + i;
                    originalInputs.add(inputVars.getOrDefault(varName, 0));
                }
            }
            
            // Format inputs as comma-separated string
            String inputsString = originalInputs.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(", "));
            
            // Use local run number and increment it for next debug session
            int runNumber = nextLocalRunNumber;
            nextLocalRunNumber++;
            
            ExecutionHistoryRow historyRow = new ExecutionHistoryRow(
                String.valueOf(runNumber),
                String.valueOf(currentExpansionLevel),
                inputsString,
                String.valueOf(variableManager.getYValue()),
                String.valueOf(context.getTotalCycles()),
                "show | re-run" // Actions column
            );
            
            executionHistory.add(historyRow);
            
        } catch (Exception e) {
            updateStatus("Warning: Could not add debug session to history: " + e.getMessage());
        }
    }
    
    /**
     * Determines the appropriate execution level based on the current display level.
     * For debugging, always use the current expansion level to enable virtual execution.
     */
    private int determineExecutionLevel() {
        if (!engine.isProgramLoaded()) {
            return currentExpansionLevel;
        }
        
        // Always use the current expansion level for debugging
        // Virtual execution mode will handle QUOTE instructions
        return currentExpansionLevel;
    }
    
}
