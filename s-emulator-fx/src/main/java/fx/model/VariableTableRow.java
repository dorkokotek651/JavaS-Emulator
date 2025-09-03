package fx.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for variable table rows in the JavaFX TableView.
 * Represents a single variable with its name and value.
 */
public class VariableTableRow {
    
    private final StringProperty variableName;
    private final StringProperty variableValue;
    
    /**
     * Creates a new variable table row.
     * 
     * @param variableName the variable name (e.g., "x1", "z1", "y")
     * @param variableValue the variable value
     */
    public VariableTableRow(String variableName, String variableValue) {
        this.variableName = new SimpleStringProperty(variableName);
        this.variableValue = new SimpleStringProperty(variableValue);
    }
    
    // Variable Name property
    public StringProperty variableNameProperty() {
        return variableName;
    }
    
    public String getVariableName() {
        return variableName.get();
    }
    
    public void setVariableName(String variableName) {
        this.variableName.set(variableName);
    }
    
    // Variable Value property
    public StringProperty variableValueProperty() {
        return variableValue;
    }
    
    public String getVariableValue() {
        return variableValue.get();
    }
    
    public void setVariableValue(String variableValue) {
        this.variableValue.set(variableValue);
    }
    
    @Override
    public String toString() {
        return "VariableTableRow{" +
                "variableName=" + getVariableName() +
                ", variableValue=" + getVariableValue() +
                '}';
    }
}
