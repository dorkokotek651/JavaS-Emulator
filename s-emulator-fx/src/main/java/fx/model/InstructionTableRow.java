package fx.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class InstructionTableRow {
    
    private final StringProperty commandNumber;
    private final StringProperty commandType;
    private final StringProperty label;
    private final StringProperty cycles;
    private final StringProperty instruction;
    
    public InstructionTableRow(String commandNumber, String commandType, String label, String cycles, String instruction) {
        this.commandNumber = new SimpleStringProperty(commandNumber);
        this.commandType = new SimpleStringProperty(commandType);
        this.label = new SimpleStringProperty(label);
        this.cycles = new SimpleStringProperty(cycles);
        this.instruction = new SimpleStringProperty(instruction);
    }

    public StringProperty commandNumberProperty() {
        return commandNumber;
    }
    
    public String getCommandNumber() {
        return commandNumber.get();
    }
    
    public void setCommandNumber(String commandNumber) {
        this.commandNumber.set(commandNumber);
    }

    public StringProperty commandTypeProperty() {
        return commandType;
    }
    
    public String getCommandType() {
        return commandType.get();
    }
    
    public void setCommandType(String commandType) {
        this.commandType.set(commandType);
    }

    public StringProperty labelProperty() {
        return label;
    }
    
    public String getLabel() {
        return label.get();
    }
    
    public void setLabel(String label) {
        this.label.set(label);
    }

    public StringProperty cyclesProperty() {
        return cycles;
    }
    
    public String getCycles() {
        return cycles.get();
    }
    
    public void setCycles(String cycles) {
        this.cycles.set(cycles);
    }

    public StringProperty instructionProperty() {
        return instruction;
    }
    
    public String getInstruction() {
        return instruction.get();
    }
    
    public void setInstruction(String instruction) {
        this.instruction.set(instruction);
    }
    
    @Override
    public String toString() {
        return "InstructionTableRow{" +
                "commandNumber=" + getCommandNumber() +
                ", commandType=" + getCommandType() +
                ", label=" + getLabel() +
                ", cycles=" + getCycles() +
                ", instruction=" + getInstruction() +
                '}';
    }
}
