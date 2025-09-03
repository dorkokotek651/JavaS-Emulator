package fx.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for instruction table rows in the JavaFX TableView.
 * Represents a single instruction with its display properties.
 */
public class InstructionTableRow {
    
    private final StringProperty commandNumber;
    private final StringProperty commandType;
    private final StringProperty cycles;
    private final StringProperty instruction;
    
    /**
     * Creates a new instruction table row.
     * 
     * @param commandNumber the command number (1-based)
     * @param commandType the command type (B for Basic, S for Synthetic)
     * @param cycles the number of cycles
     * @param instruction the instruction display text
     */
    public InstructionTableRow(String commandNumber, String commandType, String cycles, String instruction) {
        this.commandNumber = new SimpleStringProperty(commandNumber);
        this.commandType = new SimpleStringProperty(commandType);
        this.cycles = new SimpleStringProperty(cycles);
        this.instruction = new SimpleStringProperty(instruction);
    }
    
    // Command Number property
    public StringProperty commandNumberProperty() {
        return commandNumber;
    }
    
    public String getCommandNumber() {
        return commandNumber.get();
    }
    
    public void setCommandNumber(String commandNumber) {
        this.commandNumber.set(commandNumber);
    }
    
    // Command Type property
    public StringProperty commandTypeProperty() {
        return commandType;
    }
    
    public String getCommandType() {
        return commandType.get();
    }
    
    public void setCommandType(String commandType) {
        this.commandType.set(commandType);
    }
    
    // Cycles property
    public StringProperty cyclesProperty() {
        return cycles;
    }
    
    public String getCycles() {
        return cycles.get();
    }
    
    public void setCycles(String cycles) {
        this.cycles.set(cycles);
    }
    
    // Instruction property
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
                ", cycles=" + getCycles() +
                ", instruction=" + getInstruction() +
                '}';
    }
}
