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

public class ExecutionController {
    
    private final SEmulatorEngine engine;
    private Stage primaryStage;
    private int currentExpansionLevel = 0;
    private int nextLocalRunNumber = 1;
    
    private List<TextField> inputFields;
    private TableView<VariableTableRow> variablesTable;
    private Label cyclesLabel;
    private TableView<ExecutionHistoryRow> statisticsTable;
    private ObservableList<ExecutionHistoryRow> executionHistory;
    
    private Consumer<String> statusUpdater;
    private Runnable onHighlightingCleared;
    
    private boolean debugSessionActive = false;
    private List<Integer> debugOriginalInputs = null;
    private TableView<?> instructionsTable;
    private Consumer<Integer> onCurrentInstructionChanged;
    private Consumer<Map<String, Integer>> onVariablesChanged;
    private Runnable onDebugSessionStarted;
    private Runnable onDebugSessionEnded;
    
    public ExecutionController(SEmulatorEngine engine) {
        this.engine = engine;
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
    
    public void setExecutionHistory(ObservableList<ExecutionHistoryRow> executionHistory) {
        this.executionHistory = executionHistory;
    }
    
    public void setStatusUpdater(Consumer<String> statusUpdater) {
        this.statusUpdater = statusUpdater;
    }
    
    public void setOnHighlightingCleared(Runnable onHighlightingCleared) {
        this.onHighlightingCleared = onHighlightingCleared;
    }
    
    public void setInstructionsTable(TableView<?> instructionsTable) {
        this.instructionsTable = instructionsTable;
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
        nextLocalRunNumber = 1;
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
            
            ExecutionResult result = engine.runProgram(executionLevel, inputs);
            
            int currentRunNumber = nextLocalRunNumber;
            
            nextLocalRunNumber = Math.max(nextLocalRunNumber + 1, result.getRunNumber() + 1);
            
            updateExecutionResults(result, displayInputs, currentRunNumber);
            
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
            
            debugOriginalInputs = new ArrayList<>(inputs);
            
            engine.startDebugSession(executionLevel, inputs);
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
            

            ExecutionResult result = engine.resumeExecution();
            

            List<Integer> originalInputsForDisplay = debugOriginalInputs != null ? 
                new ArrayList<>(debugOriginalInputs) : null;
            
            debugSessionActive = false;
            debugOriginalInputs = null;
            

            int currentRunNumber = nextLocalRunNumber;
            

            nextLocalRunNumber = Math.max(nextLocalRunNumber + 1, result.getRunNumber() + 1);
            

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
                    throw new IllegalArgumentException("Invalid input in field " + (inputFields.indexOf(inputField) + 1) + ": " + text);
                }
            }
        }
        
        return inputs;
    }
    
    private List<Integer> formatInputsForDisplay(List<Integer> userInputs) {

        List<Integer> displayInputs = new ArrayList<>();
        

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
    
    private void updateExecutionResults(ExecutionResult result) {
        updateExecutionResults(result, null);
    }
    
    private void updateExecutionResults(ExecutionResult result, List<Integer> displayInputs) {
        updateExecutionResults(result, displayInputs, result.getRunNumber());
    }
    
    private void updateExecutionResults(ExecutionResult result, List<Integer> displayInputs, int runNumber) {

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
            

            if ("y".equals(name1)) return 1;
            if ("y".equals(name2)) return -1;
            

            if (name1.startsWith("x") && name2.startsWith("z")) return -1;
            if (name1.startsWith("z") && name2.startsWith("x")) return 1;
            

            return name1.compareTo(name2);
        });
        
        variablesTable.setItems(variableData);
    }
    
    private void updateCyclesDisplay(int totalCycles) {
        cyclesLabel.setText("Total Cycles: " + totalCycles);
    }
    
    private void addToExecutionHistory(ExecutionResult result) {
        addToExecutionHistory(result, null);
    }
    
    private void addToExecutionHistory(ExecutionResult result, List<Integer> displayInputs) {
        addToExecutionHistory(result, displayInputs, result.getRunNumber());
    }
    
    private void addToExecutionHistory(ExecutionResult result, List<Integer> displayInputs, int runNumber) {

        List<Integer> inputsToShow = (displayInputs != null) ? displayInputs : result.getInputs();
        

        String inputsString = inputsToShow.stream()
            .map(String::valueOf)
            .collect(java.util.stream.Collectors.joining(", "));
        

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
        

        statisticsTable.scrollTo(historyRow);
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
            
            if (name1.equals("y")) return 1;
            if (name2.equals("y")) return -1;
            if (name1.startsWith("x") && name2.startsWith("z")) return -1;
            if (name1.startsWith("z") && name2.startsWith("x")) return 1;
            
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
                

                addDebugSessionToHistory(context);
            }
            

            engine.stopDebugSession();
            debugSessionActive = false;
            debugOriginalInputs = null;
            

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
    
    private void addDebugSessionToHistory(ExecutionContext context) {
        try {
            VariableManager variableManager = context.getVariableManager();
            

            List<Integer> originalInputs = debugOriginalInputs != null ? 
                debugOriginalInputs : new ArrayList<>();
            

            if (originalInputs.isEmpty()) {
                Map<String, Integer> inputVars = variableManager.getSortedInputVariablesMap();
                for (int i = 1; i <= inputVars.size(); i++) {
                    String varName = "x" + i;
                    originalInputs.add(inputVars.getOrDefault(varName, 0));
                }
            }
            

            String inputsString = originalInputs.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(", "));
            

            int runNumber = nextLocalRunNumber;
            nextLocalRunNumber++;
            
            ExecutionHistoryRow historyRow = new ExecutionHistoryRow(
                String.valueOf(runNumber),
                String.valueOf(currentExpansionLevel),
                inputsString,
                String.valueOf(variableManager.getYValue()),
                String.valueOf(context.getTotalCycles()),
                "show | re-run"
            );
            
            executionHistory.add(historyRow);
            
        } catch (Exception e) {
            updateStatus("Warning: Could not add debug session to history: " + e.getMessage());
        }
    }
    
    private int determineExecutionLevel() {
        if (!engine.isProgramLoaded()) {
            return currentExpansionLevel;
        }
        


        return currentExpansionLevel;
    }
    
}
