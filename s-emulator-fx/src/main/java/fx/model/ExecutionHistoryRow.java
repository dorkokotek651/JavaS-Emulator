package fx.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for execution history table rows in the JavaFX TableView.
 * Represents a single execution run with its details.
 */
public class ExecutionHistoryRow {
    
    private final StringProperty runNumber;
    private final StringProperty expansionLevel;
    private final StringProperty inputs;
    private final StringProperty yValue;
    private final StringProperty totalCycles;
    private final StringProperty actions;
    
    /**
     * Creates a new execution history table row.
     * 
     * @param runNumber the run number
     * @param expansionLevel the expansion level used
     * @param inputs the inputs (comma-separated)
     * @param yValue the final Y value
     * @param totalCycles the total cycles consumed
     * @param actions the action buttons (show | re-run)
     */
    public ExecutionHistoryRow(String runNumber, String expansionLevel, String inputs, 
                              String yValue, String totalCycles, String actions) {
        this.runNumber = new SimpleStringProperty(runNumber);
        this.expansionLevel = new SimpleStringProperty(expansionLevel);
        this.inputs = new SimpleStringProperty(inputs);
        this.yValue = new SimpleStringProperty(yValue);
        this.totalCycles = new SimpleStringProperty(totalCycles);
        this.actions = new SimpleStringProperty(actions);
    }
    
    // Run Number property
    public StringProperty runNumberProperty() {
        return runNumber;
    }
    
    public String getRunNumber() {
        return runNumber.get();
    }
    
    public void setRunNumber(String runNumber) {
        this.runNumber.set(runNumber);
    }
    
    // Expansion Level property
    public StringProperty expansionLevelProperty() {
        return expansionLevel;
    }
    
    public String getExpansionLevel() {
        return expansionLevel.get();
    }
    
    public void setExpansionLevel(String expansionLevel) {
        this.expansionLevel.set(expansionLevel);
    }
    
    // Inputs property
    public StringProperty inputsProperty() {
        return inputs;
    }
    
    public String getInputs() {
        return inputs.get();
    }
    
    public void setInputs(String inputs) {
        this.inputs.set(inputs);
    }
    
    // Y Value property
    public StringProperty yValueProperty() {
        return yValue;
    }
    
    public String getYValue() {
        return yValue.get();
    }
    
    public void setYValue(String yValue) {
        this.yValue.set(yValue);
    }
    
    // Total Cycles property
    public StringProperty totalCyclesProperty() {
        return totalCycles;
    }
    
    public String getTotalCycles() {
        return totalCycles.get();
    }
    
    public void setTotalCycles(String totalCycles) {
        this.totalCycles.set(totalCycles);
    }
    
    // Actions property
    public StringProperty actionsProperty() {
        return actions;
    }
    
    public String getActions() {
        return actions.get();
    }
    
    public void setActions(String actions) {
        this.actions.set(actions);
    }
    
    @Override
    public String toString() {
        return "ExecutionHistoryRow{" +
                "runNumber=" + getRunNumber() +
                ", expansionLevel=" + getExpansionLevel() +
                ", inputs=" + getInputs() +
                ", yValue=" + getYValue() +
                ", totalCycles=" + getTotalCycles() +
                ", actions=" + getActions() +
                '}';
    }
}
