package fx.util;

import fx.model.ExecutionHistoryRow;
import engine.api.ExecutionResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Manages execution history separately for each program/function context.
 * Each context maintains its own history and run numbering.
 */
public class ExecutionHistoryManager {
    
    private final Map<String, ObservableList<ExecutionHistoryRow>> contextHistories;
    private final Map<String, List<ExecutionResult>> contextExecutionResults;
    private final Map<String, Integer> contextRunNumbers;
    private String currentContext;
    
    public ExecutionHistoryManager() {
        this.contextHistories = new HashMap<>();
        this.contextExecutionResults = new HashMap<>();
        this.contextRunNumbers = new HashMap<>();
        this.currentContext = "Main Program";
    }
    
    /**
     * Sets the current context program/function.
     * @param contextName The name of the context (e.g., "Main Program", "Minus", "Const7")
     */
    public void setCurrentContext(String contextName) {
        if (contextName == null || contextName.trim().isEmpty()) {
            contextName = "Main Program";
        }
        
        this.currentContext = contextName;
        
        // Initialize history and run number for new contexts
        if (!contextHistories.containsKey(contextName)) {
            contextHistories.put(contextName, FXCollections.observableArrayList());
            contextExecutionResults.put(contextName, new ArrayList<>());
            contextRunNumbers.put(contextName, 1);
        }
    }
    
    /**
     * Gets the current context name.
     * @return The current context name
     */
    public String getCurrentContext() {
        return currentContext;
    }
    
    /**
     * Gets the execution history for the current context.
     * @return ObservableList of ExecutionHistoryRow for current context
     */
    public ObservableList<ExecutionHistoryRow> getCurrentContextHistory() {
        return contextHistories.getOrDefault(currentContext, FXCollections.observableArrayList());
    }
    
    /**
     * Gets the execution history for a specific context.
     * @param contextName The context name
     * @return ObservableList of ExecutionHistoryRow for the specified context
     */
    public ObservableList<ExecutionHistoryRow> getContextHistory(String contextName) {
        return contextHistories.getOrDefault(contextName, FXCollections.observableArrayList());
    }
    
    /**
     * Gets the execution results for the current context.
     * @return List of ExecutionResult for current context
     */
    public List<ExecutionResult> getCurrentContextExecutionResults() {
        return contextExecutionResults.getOrDefault(currentContext, new ArrayList<>());
    }
    
    /**
     * Gets the execution results for a specific context.
     * @param contextName The context name
     * @return List of ExecutionResult for the specified context
     */
    public List<ExecutionResult> getContextExecutionResults(String contextName) {
        return contextExecutionResults.getOrDefault(contextName, new ArrayList<>());
    }
    
    /**
     * Gets the next run number for the current context.
     * @return The next run number for current context
     */
    public int getNextRunNumber() {
        int runNumber = contextRunNumbers.getOrDefault(currentContext, 1);
        contextRunNumbers.put(currentContext, runNumber + 1);
        return runNumber;
    }
    
    /**
     * Gets the next run number for a specific context.
     * @param contextName The context name
     * @return The next run number for the specified context
     */
    public int getNextRunNumber(String contextName) {
        int runNumber = contextRunNumbers.getOrDefault(contextName, 1);
        contextRunNumbers.put(contextName, runNumber + 1);
        return runNumber;
    }
    
    /**
     * Adds an execution result to the current context's history.
     * @param runNumber The run number for this execution
     * @param expansionLevel The expansion level used
     * @param inputs The input values as a formatted string
     * @param yValue The result Y value
     * @param totalCycles The total cycles consumed
     * @param actions The available actions (e.g., "show | re-run")
     */
    public void addExecutionToCurrentContext(int runNumber, int expansionLevel, String inputs, 
                                           int yValue, int totalCycles, String actions) {
        ExecutionHistoryRow historyRow = new ExecutionHistoryRow(
            String.valueOf(runNumber),
            String.valueOf(expansionLevel),
            inputs,
            String.valueOf(yValue),
            String.valueOf(totalCycles),
            actions,
            currentContext
        );
        
        getCurrentContextHistory().add(historyRow);
    }
    
    /**
     * Adds a full execution result to the current context's history.
     * @param executionResult The full execution result object
     * @param runNumber The run number for this execution
     * @param expansionLevel The expansion level used
     * @param inputs The input values as a formatted string
     * @param yValue The result Y value
     * @param totalCycles The total cycles consumed
     * @param actions The available actions (e.g., "show | re-run")
     */
    public void addExecutionResultToCurrentContext(ExecutionResult executionResult, int runNumber, 
                                                 int expansionLevel, String inputs, 
                                                 int yValue, int totalCycles, String actions) {
        System.out.println("ExecutionHistoryManager.addExecutionResultToCurrentContext called:");
        System.out.println("  - executionResult.getRunNumber(): " + executionResult.getRunNumber());
        System.out.println("  - passed runNumber parameter: " + runNumber);
        System.out.println("  - expansionLevel: " + expansionLevel);
        System.out.println("  - inputs: " + inputs);
        System.out.println("  - yValue: " + yValue);
        System.out.println("  - totalCycles: " + totalCycles);
        System.out.println("  - actions: " + actions);
        System.out.println("  - currentContext: " + currentContext);
        
        // Add to display history
        addExecutionToCurrentContext(runNumber, expansionLevel, inputs, yValue, totalCycles, actions);
        
        // Add to execution results storage
        getCurrentContextExecutionResults().add(executionResult);
        
        System.out.println("  - Added executionResult with runNumber " + executionResult.getRunNumber() + " to context " + currentContext);
    }
    
    /**
     * Clears the execution history for the current context.
     */
    public void clearCurrentContextHistory() {
        getCurrentContextHistory().clear();
        getCurrentContextExecutionResults().clear();
        contextRunNumbers.put(currentContext, 1);
    }
    
    /**
     * Clears the execution history for a specific context.
     * @param contextName The context name
     */
    public void clearContextHistory(String contextName) {
        getContextHistory(contextName).clear();
        getContextExecutionResults(contextName).clear();
        contextRunNumbers.put(contextName, 1);
    }
    
    /**
     * Clears all execution history for all contexts.
     */
    public void clearAllHistory() {
        contextHistories.clear();
        contextExecutionResults.clear();
        contextRunNumbers.clear();
        setCurrentContext("Main Program");
    }
    
    /**
     * Gets the number of execution runs for the current context.
     * @return The number of runs for current context
     */
    public int getCurrentContextRunCount() {
        return getCurrentContextHistory().size();
    }
    
    /**
     * Gets the number of execution runs for a specific context.
     * @param contextName The context name
     * @return The number of runs for the specified context
     */
    public int getContextRunCount(String contextName) {
        return getContextHistory(contextName).size();
    }
    
    /**
     * Gets all available context names that have execution history.
     * @return Array of context names
     */
    public String[] getAvailableContexts() {
        return contextHistories.keySet().toArray(new String[0]);
    }
    
    /**
     * Checks if a context has any execution history.
     * @param contextName The context name
     * @return True if the context has execution history
     */
    public boolean hasContextHistory(String contextName) {
        return contextHistories.containsKey(contextName) && !getContextHistory(contextName).isEmpty();
    }
}
