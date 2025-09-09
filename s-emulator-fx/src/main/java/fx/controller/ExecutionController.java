package fx.controller;

import engine.api.SEmulatorEngine;
import engine.api.ExecutionResult;
import engine.exception.SProgramException;
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
    
    // UI Components (injected from MainController)
    private List<TextField> inputFields;
    private TableView<VariableTableRow> variablesTable;
    private Label cyclesLabel;
    private TableView<ExecutionHistoryRow> statisticsTable;
    private ObservableList<ExecutionHistoryRow> executionHistory;
    
    // Callbacks for communication with main controller
    private Consumer<String> statusUpdater;
    private Runnable onHighlightingCleared;
    
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
            updateStatus("Running program with inputs: " + inputs + " at expansion level " + currentExpansionLevel);
            
            // Execute the program
            ExecutionResult result = engine.runProgram(currentExpansionLevel, inputs);
            
            // Update UI with results
            updateExecutionResults(result);
            
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
     * Handles start debug request (placeholder for Phase 5).
     */
    public void handleStartDebug() {
        updateStatus("Start debug functionality will be implemented in Phase 5");
    }
    
    /**
     * Handles step over request (placeholder for Phase 5).
     */
    public void handleStepOver() {
        updateStatus("Step over functionality will be implemented in Phase 5");
    }
    
    /**
     * Handles stop request (placeholder for Phase 5).
     */
    public void handleStop() {
        updateStatus("Stop functionality will be implemented in Phase 5");
    }
    
    /**
     * Handles resume request (placeholder for Phase 5).
     */
    public void handleResume() {
        updateStatus("Resume functionality will be implemented in Phase 5");
    }
    
    /**
     * Collects input values from all input fields.
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
        // Update variables table
        updateVariablesTable(result);
        
        // Update cycles display
        updateCyclesDisplay(result.getTotalCycles());
        
        // Add to execution history
        addToExecutionHistory(result);
        
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
        // Format inputs as comma-separated string
        String inputsString = result.getInputs().stream()
            .map(String::valueOf)
            .collect(java.util.stream.Collectors.joining(", "));
        
        // For Phase 4, use placeholder for actions (will be implemented in Phase 6)
        String actions = "show | re-run";
        
        ExecutionHistoryRow historyRow = new ExecutionHistoryRow(
            String.valueOf(result.getRunNumber()),
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
}
