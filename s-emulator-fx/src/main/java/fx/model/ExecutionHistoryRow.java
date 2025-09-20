package fx.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ExecutionHistoryRow {
    
    private final StringProperty runNumber;
    private final StringProperty expansionLevel;
    private final StringProperty inputs;
    private final StringProperty yValue;
    private final StringProperty totalCycles;
    private final StringProperty actions;
    private final StringProperty context;
    
    public ExecutionHistoryRow(String runNumber, String expansionLevel, String inputs, 
                              String yValue, String totalCycles, String actions) {
        this(runNumber, expansionLevel, inputs, yValue, totalCycles, actions, "Main Program");
    }
    
    public ExecutionHistoryRow(String runNumber, String expansionLevel, String inputs, 
                              String yValue, String totalCycles, String actions, String context) {
        this.runNumber = new SimpleStringProperty(runNumber);
        this.expansionLevel = new SimpleStringProperty(expansionLevel);
        this.inputs = new SimpleStringProperty(inputs);
        this.yValue = new SimpleStringProperty(yValue);
        this.totalCycles = new SimpleStringProperty(totalCycles);
        this.actions = new SimpleStringProperty(actions);
        this.context = new SimpleStringProperty(context);
    }

    public StringProperty runNumberProperty() {
        return runNumber;
    }
    
    public String getRunNumber() {
        return runNumber.get();
    }
    
    public void setRunNumber(String runNumber) {
        this.runNumber.set(runNumber);
    }

    public StringProperty expansionLevelProperty() {
        return expansionLevel;
    }
    
    public String getExpansionLevel() {
        return expansionLevel.get();
    }
    
    public void setExpansionLevel(String expansionLevel) {
        this.expansionLevel.set(expansionLevel);
    }

    public StringProperty inputsProperty() {
        return inputs;
    }
    
    public String getInputs() {
        return inputs.get();
    }
    
    public void setInputs(String inputs) {
        this.inputs.set(inputs);
    }

    public StringProperty yValueProperty() {
        return yValue;
    }
    
    public String getYValue() {
        return yValue.get();
    }
    
    public void setYValue(String yValue) {
        this.yValue.set(yValue);
    }

    public StringProperty totalCyclesProperty() {
        return totalCycles;
    }
    
    public String getTotalCycles() {
        return totalCycles.get();
    }
    
    public void setTotalCycles(String totalCycles) {
        this.totalCycles.set(totalCycles);
    }

    public StringProperty actionsProperty() {
        return actions;
    }
    
    public String getActions() {
        return actions.get();
    }
    
    public void setActions(String actions) {
        this.actions.set(actions);
    }
    
    public StringProperty contextProperty() {
        return context;
    }
    
    public String getContext() {
        return context.get();
    }
    
    public void setContext(String context) {
        this.context.set(context);
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
                ", context=" + getContext() +
                '}';
    }
}
