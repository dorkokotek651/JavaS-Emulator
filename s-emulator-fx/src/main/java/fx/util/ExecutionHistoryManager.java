package fx.util;

import fx.model.ExecutionHistoryRow;
import engine.api.ExecutionResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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

    public void setCurrentContext(String contextName) {
        if (contextName == null || contextName.trim().isEmpty()) {
            contextName = "Main Program";
        }
        
        this.currentContext = contextName;
        
        if (!contextHistories.containsKey(contextName)) {
            contextHistories.put(contextName, FXCollections.observableArrayList());
            contextExecutionResults.put(contextName, new ArrayList<>());
            contextRunNumbers.put(contextName, 1);
        }
    }

    public String getCurrentContext() {
        return currentContext;
    }

    public ObservableList<ExecutionHistoryRow> getCurrentContextHistory() {
        return contextHistories.getOrDefault(currentContext, FXCollections.observableArrayList());
    }

    public ObservableList<ExecutionHistoryRow> getContextHistory(String contextName) {
        return contextHistories.getOrDefault(contextName, FXCollections.observableArrayList());
    }

    public List<ExecutionResult> getCurrentContextExecutionResults() {
        return contextExecutionResults.getOrDefault(currentContext, new ArrayList<>());
    }

    public List<ExecutionResult> getContextExecutionResults(String contextName) {
        return contextExecutionResults.getOrDefault(contextName, new ArrayList<>());
    }

    public int getNextRunNumber() {
        int runNumber = contextRunNumbers.getOrDefault(currentContext, 1);
        contextRunNumbers.put(currentContext, runNumber + 1);
        return runNumber;
    }

    public int getNextRunNumber(String contextName) {
        int runNumber = contextRunNumbers.getOrDefault(contextName, 1);
        contextRunNumbers.put(contextName, runNumber + 1);
        return runNumber;
    }

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

    public void addExecutionResultToCurrentContext(ExecutionResult executionResult, int runNumber, 
                                                 int expansionLevel, String inputs, 
                                                 int yValue, int totalCycles, String actions) {
        
        addExecutionToCurrentContext(runNumber, expansionLevel, inputs, yValue, totalCycles, actions);
        
        getCurrentContextExecutionResults().add(executionResult);
        
    }

    public void clearCurrentContextHistory() {
        getCurrentContextHistory().clear();
        getCurrentContextExecutionResults().clear();
        contextRunNumbers.put(currentContext, 1);
    }

    public void clearContextHistory(String contextName) {
        getContextHistory(contextName).clear();
        getContextExecutionResults(contextName).clear();
        contextRunNumbers.put(contextName, 1);
    }

    public void clearAllHistory() {
        contextHistories.clear();
        contextExecutionResults.clear();
        contextRunNumbers.clear();
        setCurrentContext("Main Program");
    }

    public int getCurrentContextRunCount() {
        return getCurrentContextHistory().size();
    }

    public int getContextRunCount(String contextName) {
        return getContextHistory(contextName).size();
    }

    public String[] getAvailableContexts() {
        return contextHistories.keySet().toArray(new String[0]);
    }

    public boolean hasContextHistory(String contextName) {
        return contextHistories.containsKey(contextName) && !getContextHistory(contextName).isEmpty();
    }
}
