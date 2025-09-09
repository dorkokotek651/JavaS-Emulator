package fx.controller;

import engine.api.SProgram;
import engine.api.SInstruction;
import fx.model.InstructionTableRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Controller responsible for highlighting functionality in the S-Emulator application.
 * Handles highlight dropdown management, instruction highlighting, and selection clearing.
 */
public class HighlightController {
    
    // UI Components (injected from MainController)
    private ComboBox<String> highlightSelectionCombo;
    private TableView<InstructionTableRow> instructionsTable;
    
    // Callbacks for communication with main controller
    private Consumer<String> statusUpdater;
    
    /**
     * Creates a new HighlightController.
     */
    public HighlightController() {
    }
    
    /**
     * Sets the highlight selection combo box reference.
     * 
     * @param highlightSelectionCombo the highlight selection combo box
     */
    public void setHighlightSelectionCombo(ComboBox<String> highlightSelectionCombo) {
        this.highlightSelectionCombo = highlightSelectionCombo;
    }
    
    /**
     * Sets the instructions table reference.
     * 
     * @param instructionsTable the instructions table view
     */
    public void setInstructionsTable(TableView<InstructionTableRow> instructionsTable) {
        this.instructionsTable = instructionsTable;
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
     * Updates the highlight dropdown with available labels and variables from the program.
     * 
     * @param program the current program to extract labels and variables from
     */
    public void updateHighlightDropdown(SProgram program) {
        ObservableList<String> highlightItems = FXCollections.observableArrayList();
        highlightItems.add("Clear Highlighting");
        
        // Add all labels
        List<String> labels = program.getLabels();
        if (!labels.isEmpty()) {
            highlightItems.add("--- Labels ---");
            highlightItems.addAll(labels);
        }
        
        // Add all variables
        List<String> inputVars = program.getInputVariables();
        if (!inputVars.isEmpty()) {
            highlightItems.add("--- Input Variables ---");
            highlightItems.addAll(inputVars);
        }
        
        // Add working variables (extract from instructions)
        Set<String> workingVars = extractWorkingVariables(program);
        if (!workingVars.isEmpty()) {
            highlightItems.add("--- Working Variables ---");
            highlightItems.addAll(workingVars.stream().sorted().collect(java.util.stream.Collectors.toList()));
        }
        
        highlightSelectionCombo.setItems(highlightItems);
        highlightSelectionCombo.setValue("Clear Highlighting");
        
        // Configure ComboBox to disable section headers
        highlightSelectionCombo.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                } else {
                    setText(item);
                    // Disable section headers (items starting with "---")
                    setDisable(item.startsWith("---"));
                    // Style section headers differently
                    if (item.startsWith("---")) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: gray;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        // Add selection listener
        highlightSelectionCombo.setOnAction(e -> handleHighlightSelection());
    }
    
    /**
     * Extracts working variables from program instructions.
     * 
     * @param program the program to extract variables from
     * @return set of working variable names
     */
    private Set<String> extractWorkingVariables(SProgram program) {
        Set<String> workingVars = new java.util.HashSet<>();
        
        for (SInstruction instruction : program.getInstructions()) {
            String variable = instruction.getVariable();
            if (variable != null && variable.startsWith("z")) {
                workingVars.add(variable);
            }
            
            // Also check arguments for variables
            for (String arg : instruction.getArguments().values()) {
                if (arg != null && arg.matches("[xyz]\\d+")) {
                    if (arg.startsWith("z")) {
                        workingVars.add(arg);
                    }
                }
            }
        }
        
        return workingVars;
    }
    
    /**
     * Handles highlighting selection from dropdown.
     */
    public void handleHighlightSelection() {
        String selectedItem = highlightSelectionCombo.getValue();
        if (selectedItem == null || selectedItem.equals("Clear Highlighting") || selectedItem.startsWith("---")) {
            clearHighlighting();
            return;
        }
        
        highlightInstructionsUsing(selectedItem);
    }
    
    /**
     * Highlights instructions that use the specified label or variable.
     * 
     * @param item the label or variable to highlight
     * @param program the current program for instruction access
     */
    public void highlightInstructionsUsing(String item, SProgram program) {
        if (program == null) {
            return;
        }
        
        // Clear previous highlighting
        clearHighlighting();
        
        // Set selection mode to multiple to allow selecting multiple rows
        instructionsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        List<SInstruction> instructions = program.getInstructions();
        List<Integer> rowsToHighlight = new ArrayList<>();
        
        for (int i = 0; i < instructions.size(); i++) {
            SInstruction instruction = instructions.get(i);
            
            boolean shouldHighlight = 
                item.equals(instruction.getLabel()) ||
                item.equals(instruction.getVariable()) ||
                instruction.getArguments().values().contains(item);
            
            if (shouldHighlight) {
                rowsToHighlight.add(i);
            }
        }
        
        // Select all matching rows at once
        if (!rowsToHighlight.isEmpty()) {
            instructionsTable.getSelectionModel().selectIndices(
                rowsToHighlight.get(0), 
                rowsToHighlight.stream().skip(1).mapToInt(Integer::intValue).toArray()
            );
            updateStatus("Highlighted " + rowsToHighlight.size() + " instructions using: " + item);
        } else {
            updateStatus("No instructions found using: " + item);
        }
    }
    
    /**
     * Highlights instructions using the currently selected item from dropdown.
     * This method requires the program context to be provided by the caller.
     * 
     * @param item the item to highlight
     */
    private void highlightInstructionsUsing(String item) {
        if (highlightCallback != null) {
            highlightCallback.accept(item);
        } else {
            updateStatus("Highlighting instructions using: " + item);
        }
    }
    
    /**
     * Sets a callback for highlighting instructions with program context.
     * 
     * @param highlightCallback callback that takes an item and highlights instructions
     */
    public void setHighlightCallback(Consumer<String> highlightCallback) {
        this.highlightCallback = highlightCallback;
    }
    
    private Consumer<String> highlightCallback;
    
    /**
     * Clears instruction highlighting.
     */
    public void clearHighlighting() {
        if (instructionsTable != null) {
            instructionsTable.getSelectionModel().clearSelection();
            // Reset to single selection mode for normal operation
            instructionsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        updateStatus("Highlighting cleared");
    }
    
    /**
     * Clears the highlight dropdown.
     */
    public void clearHighlightDropdown() {
        if (highlightSelectionCombo != null) {
            highlightSelectionCombo.setItems(FXCollections.observableArrayList());
            highlightSelectionCombo.setValue(null);
        }
    }
    
    /**
     * Enables the highlight selection combo box.
     */
    public void enableHighlightSelection() {
        if (highlightSelectionCombo != null) {
            highlightSelectionCombo.setDisable(false);
        }
    }
    
    /**
     * Disables the highlight selection combo box.
     */
    public void disableHighlightSelection() {
        if (highlightSelectionCombo != null) {
            highlightSelectionCombo.setDisable(true);
        }
    }
    
    /**
     * Initializes the highlight controller.
     */
    public void initialize() {
        disableHighlightSelection(); // Start disabled until program is loaded
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
        System.out.println("HighlightController Status: " + message);
    }
}
