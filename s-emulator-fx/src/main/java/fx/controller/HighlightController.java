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

public class HighlightController {

    private ComboBox<String> highlightSelectionCombo;
    private TableView<InstructionTableRow> instructionsTable;

    private Consumer<String> statusUpdater;

    public void setHighlightSelectionCombo(ComboBox<String> highlightSelectionCombo) {
        this.highlightSelectionCombo = highlightSelectionCombo;
    }
    
    public void setInstructionsTable(TableView<InstructionTableRow> instructionsTable) {
        this.instructionsTable = instructionsTable;
    }
    
    public void setStatusUpdater(Consumer<String> statusUpdater) {
        this.statusUpdater = statusUpdater;
    }
    
    public void updateHighlightDropdown(SProgram program) {
        ObservableList<String> highlightItems = FXCollections.observableArrayList();
        highlightItems.add("Clear Highlighting");

        List<String> labels = program.getLabels();
        if (!labels.isEmpty()) {
            highlightItems.add("--- Labels ---");
            highlightItems.addAll(labels);
        }

        List<String> inputVars = program.getInputVariables();
        if (!inputVars.isEmpty()) {
            highlightItems.add("--- Input Variables ---");
            highlightItems.addAll(inputVars);
        }

        Set<String> workingVars = extractWorkingVariables(program);
        if (!workingVars.isEmpty()) {
            highlightItems.add("--- Working Variables ---");
            highlightItems.addAll(workingVars.stream().sorted().collect(java.util.stream.Collectors.toList()));
        }
        
        highlightSelectionCombo.setItems(highlightItems);
        highlightSelectionCombo.setValue("Clear Highlighting");

        highlightSelectionCombo.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                } else {
                    setText(item);

                    setDisable("---".equals(item.substring(0, Math.min(3, item.length()))));

                    if (item.startsWith("---")) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: gray;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        highlightSelectionCombo.setOnAction(e -> handleHighlightSelection());
    }
    
    private Set<String> extractWorkingVariables(SProgram program) {
        Set<String> workingVars = new java.util.HashSet<>();
        
        for (SInstruction instruction : program.getInstructions()) {
            String variable = instruction.getVariable();
            if (variable != null && variable.startsWith("z")) {
                workingVars.add(variable);
            }

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
    
    public void handleHighlightSelection() {
        String selectedItem = highlightSelectionCombo.getValue();
        if (selectedItem == null || selectedItem.equals("Clear Highlighting") || selectedItem.startsWith("---")) {
            clearHighlighting();
            return;
        }
        
        highlightInstructionsUsing(selectedItem);
    }
    
    public void highlightInstructionsUsing(String item, SProgram program) {
        if (program == null) {
            return;
        }

        clearHighlighting();

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
    
    private void highlightInstructionsUsing(String item) {
        if (highlightCallback != null) {
            highlightCallback.accept(item);
        } else {
            updateStatus("Highlighting instructions using: " + item);
        }
    }
    
    public void setHighlightCallback(Consumer<String> highlightCallback) {
        this.highlightCallback = highlightCallback;
    }
    
    private Consumer<String> highlightCallback;
    
    public void clearHighlighting() {
        if (instructionsTable != null) {
            instructionsTable.getSelectionModel().clearSelection();

            instructionsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        updateStatus("Highlighting cleared");
    }
    
    public void clearHighlightDropdown() {
        if (highlightSelectionCombo != null) {
            highlightSelectionCombo.setItems(FXCollections.observableArrayList());
            highlightSelectionCombo.setValue(null);
        }
    }
    
    public void enableHighlightSelection() {
        if (highlightSelectionCombo != null) {
            highlightSelectionCombo.setDisable(false);
        }
    }
    
    public void disableHighlightSelection() {
        if (highlightSelectionCombo != null) {
            highlightSelectionCombo.setDisable(true);
        }
    }
    
    public void initialize() {
        disableHighlightSelection();
    }
    
    private void updateStatus(String message) {
        if (statusUpdater != null) {
            statusUpdater.accept(message);
        }

    }
}
