package fx.controller;

import fx.util.StyleManager;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InputController {
    
    private List<TextField> inputFields = new ArrayList<>();
    

    private VBox inputsContainer;
    private Button removeInputButton;
    

    private Consumer<String> statusUpdater;
    
    public InputController() {
    }
    
    public void setInputsContainer(VBox inputsContainer) {
        this.inputsContainer = inputsContainer;
    }
    
    public void setRemoveInputButton(Button removeInputButton) {
        this.removeInputButton = removeInputButton;
    }
    
    public void setStatusUpdater(Consumer<String> statusUpdater) {
        this.statusUpdater = statusUpdater;
    }
    
    public List<TextField> getInputFields() {
        return inputFields;
    }
    
    public void handleAddInput() {
        addInputField();
    }
    
    public void handleRemoveInput() {
        removeInputField();
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
    
    private void removeInputField() {
        if (inputFields.size() > 1) {
            TextField lastField = inputFields.remove(inputFields.size() - 1);
            inputsContainer.getChildren().remove(lastField);
            
            updateRemoveButtonState();
            updateStatus("Removed input field. " + inputFields.size() + " input(s) remaining");
        } else {
            updateStatus("Cannot remove the last input field");
        }
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
        List<Integer> inputs = new ArrayList<>();
        
        for (TextField inputField : inputFields) {
            String text = inputField.getText().trim();
            if (text.isEmpty()) {
                inputs.add(0);
            } else {
                try {
                    int value = Integer.parseInt(text);
                    if (value < 0) {
                        StyleManager.applyInputFieldErrorStyle(inputField);
                        throw new NumberFormatException("Negative numbers not allowed");
                    }
                    inputs.add(value);
                } catch (NumberFormatException e) {
                    StyleManager.applyInputFieldErrorStyle(inputField);
                    throw new IllegalArgumentException("Invalid input in field " + (inputFields.indexOf(inputField) + 1) + ": " + text);
                }
            }
        }
        
        return inputs;
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

        System.out.println("InputController Status: " + message);
    }
}
