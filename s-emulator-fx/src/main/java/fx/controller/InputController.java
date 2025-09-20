package fx.controller;

import fx.util.StyleManager;
import engine.api.SProgram;
import engine.api.SInstruction;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;

public class InputController {
    
    private List<TextField> inputFields = new ArrayList<>();
    private Map<String, TextField> variableToFieldMap = new HashMap<>();
    private List<String> requiredVariables = new ArrayList<>();

    private VBox inputsContainer;
    private Button removeInputButton;

    private Consumer<String> statusUpdater;
    private Runnable onInputsReady;

    public void setInputsContainer(VBox inputsContainer) {
        this.inputsContainer = inputsContainer;
    }
    
    public void setRemoveInputButton(Button removeInputButton) {
        this.removeInputButton = removeInputButton;
    }
    
    public void setStatusUpdater(Consumer<String> statusUpdater) {
        this.statusUpdater = statusUpdater;
    }
    
    public void setOnInputsReady(Runnable onInputsReady) {
        this.onInputsReady = onInputsReady;
    }

    public void updateInputFieldsForProgram(SProgram program) {
        if (program == null) {
            clearAllInputs();
            return;
        }
        
        requiredVariables = program.getInputVariables();

        if (requiredVariables.isEmpty()) {
            requiredVariables = analyzeInputVariablesManually(program);
        }
        
        clearInputFieldsOnly();
        
        for (String variable : requiredVariables) {
            createInputFieldForVariable(variable);
        }
        
        if (removeInputButton != null) {
            removeInputButton.setVisible(false);
        }
        
        updateStatus("Generated " + requiredVariables.size() + " input fields for variables: " + requiredVariables);
        
        if (onInputsReady != null) {
            onInputsReady.run();
        }
    }
    
    public List<TextField> getInputFields() {
        return inputFields;
    }
    
    public void handleAddInput() {
        updateStatus("Cannot add input fields in restricted mode - fields are based on program requirements");
    }
    
    public void handleRemoveInput() {
        updateStatus("Cannot remove input fields in restricted mode - fields are based on program requirements");
    }
    
    public void addDefaultInput() {
        addInputField();
    }
    
    public void clearInputFieldStyling() {
        for (TextField inputField : inputFields) {
            StyleManager.applyInputFieldStyle(inputField);
        }
    }
    
    private void addInputField() {
        TextField inputField = new TextField();
        inputField.setPromptText("Input " + (inputFields.size() + 1));
        inputField.setPrefWidth(80);

        StyleManager.applyInputFieldStyle(inputField);

        inputField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                inputField.setText(newValue.replaceAll("[^\\d]", ""));
            }

            if (newValue.matches("\\d*")) {
                StyleManager.applyInputFieldStyle(inputField);
            }
        });

        inputField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && inputField.getText().isEmpty()) {

                inputField.setText("0");
            }
        });

        Tooltip tooltip = new Tooltip("Enter a natural number (≥ 0). Default: 0");
        inputField.setTooltip(tooltip);
        
        inputFields.add(inputField);
        inputsContainer.getChildren().add(inputField);

        updateRemoveButtonState();

        inputField.requestFocus();
        
        updateStatus("Added input field " + inputFields.size());
    }
    
    private void updateRemoveButtonState() {
        if (removeInputButton != null) {
            removeInputButton.setDisable(inputFields.size() <= 1);
        }
    }
    
    public boolean validateAllInputs() {
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
    
    public List<Integer> collectInputValues() {
        Map<String, Integer> providedInputs = new HashMap<>();
        
        for (String variable : requiredVariables) {
            TextField field = variableToFieldMap.get(variable);
            if (field != null) {
                String text = field.getText().trim();
                if (text.isEmpty()) {
                    providedInputs.put(variable, 0);
                } else {
                    try {
                        int value = Integer.parseInt(text);
                        if (value < 0) {
                            StyleManager.applyInputFieldErrorStyle(field);
                            throw new NumberFormatException("Negative numbers not allowed for " + variable);
                        }
                        providedInputs.put(variable, value);
                    } catch (NumberFormatException e) {
                        StyleManager.applyInputFieldErrorStyle(field);
                        throw new IllegalArgumentException("Invalid input for " + variable + ": " + text, e);
                    }
                }
            }
        }
        
        List<Integer> completeInputs = new ArrayList<>();
        if (!requiredVariables.isEmpty()) {
            int maxVarNumber = 0;
            for (String variable : requiredVariables) {
                int varNumber = extractVariableNumber(variable);
                maxVarNumber = Math.max(maxVarNumber, varNumber);
            }
            
            for (int i = 1; i <= maxVarNumber; i++) {
                String varName = "x" + i;
                Integer value = providedInputs.get(varName);
                completeInputs.add(value != null ? value : 0);
            }
        }
        
        return completeInputs;
    }

    private int extractVariableNumber(String variableName) {
        if (variableName == null || !variableName.startsWith("x")) {
            return 0;
        }
        
        try {
            return Integer.parseInt(variableName.substring(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public int getInputFieldCount() {
        return inputFields.size();
    }
    
    public void initialize() {
        updateRemoveButtonState();
    }
    
    private void updateStatus(String message) {
        if (statusUpdater != null) {
            statusUpdater.accept(message);
        }

    }
    
    public void populateInputsFromHistory(List<Integer> historicalInputs) {
        if (historicalInputs == null || historicalInputs.isEmpty()) {
            return;
        }
        
        clearInputFieldsOnly();
        
        for (int i = 0; i < historicalInputs.size(); i++) {
            Integer inputValue = historicalInputs.get(i);
            
            if (i < requiredVariables.size()) {
                String variableName = requiredVariables.get(i);
                TextField field = createInputFieldForVariable(variableName);
                field.setText(String.valueOf(inputValue));
            } else {
                addInputField();
                if (!inputFields.isEmpty()) {
                    TextField lastField = inputFields.get(inputFields.size() - 1);
                    lastField.setText(String.valueOf(inputValue));
                }
            }
        }
        
        updateStatus("Populated " + historicalInputs.size() + " inputs from historical run");
    }

    private TextField createInputFieldForVariable(String variableName) {
        HBox inputRow = new HBox(5);
        
        Label variableLabel = new Label(variableName + ":");
        variableLabel.setPrefWidth(30);
        variableLabel.setStyle("-fx-font-weight: bold;");
        
        TextField inputField = new TextField();
        inputField.setPromptText("0");
        inputField.setPrefWidth(80);
        
        StyleManager.applyInputFieldStyle(inputField);
        
        inputField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                inputField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            
            if (newValue.matches("\\d*")) {
                StyleManager.applyInputFieldStyle(inputField);
            }
        });
        
        inputField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && inputField.getText().isEmpty()) {
                inputField.setText("0");
            }
        });
        
        Tooltip tooltip = new Tooltip("Enter a natural number (≥ 0) for " + variableName + ". Default: 0");
        inputField.setTooltip(tooltip);
        
        inputRow.getChildren().addAll(variableLabel, inputField);
        inputsContainer.getChildren().add(inputRow);
        
        inputFields.add(inputField);
        variableToFieldMap.put(variableName, inputField);
        
        return inputField;
    }

    private List<String> analyzeInputVariablesManually(SProgram program) {
        Set<String> usedVariables = new HashSet<>();
        java.util.regex.Pattern xPattern = java.util.regex.Pattern.compile("x\\d+");
        
        for (SInstruction instruction : program.getInstructions()) {
            String variable = instruction.getVariable();
            if (variable != null && xPattern.matcher(variable).matches()) {
                usedVariables.add(variable);
            }
            
            for (String argValue : instruction.getArguments().values()) {
                if (argValue != null) {
                    java.util.regex.Matcher matcher = xPattern.matcher(argValue);
                    while (matcher.find()) {
                        String foundVar = matcher.group();
                        usedVariables.add(foundVar);
                    }
                }
            }
        }
        
        List<String> sortedVariables = new ArrayList<>(usedVariables);
        sortedVariables.sort((a, b) -> {
            int numA = extractVariableNumber(a);
            int numB = extractVariableNumber(b);
            return Integer.compare(numA, numB);
        });
        
        return sortedVariables;
    }
    
    public void clearInputFieldsOnly() {
        if (inputsContainer != null) {
            inputsContainer.getChildren().clear();
        }
        inputFields.clear();
        variableToFieldMap.clear();
    }
    
    public void clearAllInputs() {
        if (inputsContainer != null) {
            inputsContainer.getChildren().clear();
        }
        inputFields.clear();
        variableToFieldMap.clear();
        requiredVariables.clear();
    }
    
    public void setInputFieldsEnabled(boolean enabled) {
        for (TextField field : inputFields) {
            field.setDisable(!enabled);
        }
    }
}
