package fx.controller;

import fx.util.StyleManager;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller responsible for dynamic input field management in the S-Emulator application.
 * Handles creation, removal, validation, and styling of input fields.
 */
public class InputController {
    
    private List<TextField> inputFields = new ArrayList<>();
    
    // UI Components (injected from MainController)
    private VBox inputsContainer;
    private Button removeInputButton;
    
    // Callbacks for communication with main controller
    private Consumer<String> statusUpdater;
    
    /**
     * Creates a new InputController.
     */
    public InputController() {
    }
    
    /**
     * Sets the inputs container reference.
     * 
     * @param inputsContainer the VBox container for input fields
     */
    public void setInputsContainer(VBox inputsContainer) {
        this.inputsContainer = inputsContainer;
    }
    
    /**
     * Sets the remove input button reference.
     * 
     * @param removeInputButton the remove input button
     */
    public void setRemoveInputButton(Button removeInputButton) {
        this.removeInputButton = removeInputButton;
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
     * Gets the list of input fields.
     * 
     * @return the list of input text fields
     */
    public List<TextField> getInputFields() {
        return inputFields;
    }
    
    /**
     * Handles add input request from user.
     */
    public void handleAddInput() {
        addInputField();
    }
    
    /**
     * Handles remove input request from user.
     */
    public void handleRemoveInput() {
        removeInputField();
    }
    
    /**
     * Adds a default input field during initialization.
     */
    public void addDefaultInput() {
        addInputField();
    }
    
    /**
     * Clears any input field error styling.
     */
    public void clearInputFieldStyling() {
        for (TextField inputField : inputFields) {
            StyleManager.applyInputFieldStyle(inputField);
        }
    }
    
    /**
     * Adds a new input field with proper styling and validation.
     */
    private void addInputField() {
        TextField inputField = new TextField();
        inputField.setPromptText("Input " + (inputFields.size() + 1));
        inputField.setPrefWidth(80);
        
        // Apply CSS styling instead of inline styles
        StyleManager.applyInputFieldStyle(inputField);
        
        // Add validation for natural numbers only with enhanced feedback
        inputField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                inputField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            // Clear any error styling when user types valid input
            if (newValue.matches("\\d*")) {
                StyleManager.applyInputFieldStyle(inputField);
            }
        });
        
        // Add focus listener for better user experience
        inputField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && inputField.getText().isEmpty()) {
                // Set default value of 0 when field loses focus and is empty
                inputField.setText("0");
            }
        });
        
        // Add tooltip for better user guidance
        Tooltip tooltip = new Tooltip("Enter a natural number (≥ 0). Default: 0");
        inputField.setTooltip(tooltip);
        
        inputFields.add(inputField);
        inputsContainer.getChildren().add(inputField);
        
        // Enable remove button if we have more than one input
        updateRemoveButtonState();
        
        // Focus the new field for better user experience
        inputField.requestFocus();
        
        updateStatus("Added input field " + inputFields.size());
    }
    
    /**
     * Removes the last input field if more than one exists.
     */
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
    
    /**
     * Updates the state of the remove button based on number of input fields.
     */
    private void updateRemoveButtonState() {
        if (removeInputButton != null) {
            removeInputButton.setDisable(inputFields.size() <= 1);
        }
    }
    
    /**
     * Validates all input fields for natural numbers.
     * 
     * @return true if all inputs are valid, false otherwise
     */
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
    
    /**
     * Collects values from all input fields.
     * 
     * @return list of input values (0 for empty fields)
     * @throws IllegalArgumentException if any input is invalid
     */
    public List<Integer> collectInputValues() {
        List<Integer> inputs = new ArrayList<>();
        
        for (TextField inputField : inputFields) {
            String text = inputField.getText().trim();
            if (text.isEmpty()) {
                inputs.add(0); // Default value for empty fields
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
    
    /**
     * Gets the current number of input fields.
     * 
     * @return number of input fields
     */
    public int getInputFieldCount() {
        return inputFields.size();
    }
    
    /**
     * Sets initial state for the input controller.
     */
    public void initialize() {
        updateRemoveButtonState();
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
        System.out.println("InputController Status: " + message);
    }
}
