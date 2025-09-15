package fx.util;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class StyleManager {
    
    private static final String STYLESHEET_PATH = "/fx/styles/semulator.css";
    

    public static final String INPUT_FIELD = "input-field";
    public static final String INPUT_FIELD_ERROR = "input-field-error";
    public static final String SECTION_HEADER = "section-header";
    public static final String PRIMARY_BUTTON = "primary-button";
    public static final String SECONDARY_BUTTON = "secondary-button";
    public static final String DEBUG_BUTTON = "debug-button";
    public static final String HIGHLIGHTED_ROW = "highlighted-row";
    public static final String STATUS_LABEL = "status-label";
    public static final String SECTION_TITLE = "section-title";
    public static final String FILE_PATH_LABEL = "file-path-label";
    public static final String SUMMARY_LABEL = "summary-label";
    public static final String LEVEL_LABEL = "level-label";
    public static final String INSTRUCTION_TABLE = "instruction-table";
    public static final String VARIABLE_TABLE = "variable-table";
    public static final String HISTORY_TABLE = "history-table";
    public static final String DEBUG_SECTION = "debug-section";
    public static final String VARIABLES_SECTION = "variables-section";
    public static final String INPUTS_SECTION = "inputs-section";
    public static final String CYCLES_SECTION = "cycles-section";
    
    public static void applyStylesheet(Scene scene) {
        String stylesheetUrl = StyleManager.class.getResource(STYLESHEET_PATH).toExternalForm();
        scene.getStylesheets().add(stylesheetUrl);
    }
    
    public static void applyInputFieldStyle(TextField field) {
        field.getStyleClass().removeAll(INPUT_FIELD_ERROR);
        if (!field.getStyleClass().contains(INPUT_FIELD)) {
            field.getStyleClass().add(INPUT_FIELD);
        }
    }
    
    public static void applyInputFieldErrorStyle(TextField field) {
        if (!field.getStyleClass().contains(INPUT_FIELD)) {
            field.getStyleClass().add(INPUT_FIELD);
        }
        if (!field.getStyleClass().contains(INPUT_FIELD_ERROR)) {
            field.getStyleClass().add(INPUT_FIELD_ERROR);
        }
    }
    
    public static void applyPrimaryButtonStyle(Button button) {
        button.getStyleClass().removeAll(SECONDARY_BUTTON, DEBUG_BUTTON);
        if (!button.getStyleClass().contains(PRIMARY_BUTTON)) {
            button.getStyleClass().add(PRIMARY_BUTTON);
        }
    }
    
    public static void applySecondaryButtonStyle(Button button) {
        button.getStyleClass().removeAll(PRIMARY_BUTTON, DEBUG_BUTTON);
        if (!button.getStyleClass().contains(SECONDARY_BUTTON)) {
            button.getStyleClass().add(SECONDARY_BUTTON);
        }
    }
    
    public static void applyDebugButtonStyle(Button button) {
        button.getStyleClass().removeAll(PRIMARY_BUTTON, SECONDARY_BUTTON);
        if (!button.getStyleClass().contains(DEBUG_BUTTON)) {
            button.getStyleClass().add(DEBUG_BUTTON);
        }
    }
    
    public static void applySectionTitleStyle(Label label) {
        if (!label.getStyleClass().contains(SECTION_TITLE)) {
            label.getStyleClass().add(SECTION_TITLE);
        }
    }
    
    public static void applyFilePathStyle(Label label) {
        if (!label.getStyleClass().contains(FILE_PATH_LABEL)) {
            label.getStyleClass().add(FILE_PATH_LABEL);
        }
    }
    
    public static void applyStatusLabelStyle(Label label) {
        if (!label.getStyleClass().contains(STATUS_LABEL)) {
            label.getStyleClass().add(STATUS_LABEL);
        }
    }
    
    public static void applyInstructionTableStyle(TableView<?> table) {
        if (!table.getStyleClass().contains(INSTRUCTION_TABLE)) {
            table.getStyleClass().add(INSTRUCTION_TABLE);
        }
    }
    
    public static void applyVariableTableStyle(TableView<?> table) {
        if (!table.getStyleClass().contains(VARIABLE_TABLE)) {
            table.getStyleClass().add(VARIABLE_TABLE);
        }
    }
    
    public static void applyHistoryTableStyle(TableView<?> table) {
        if (!table.getStyleClass().contains(HISTORY_TABLE)) {
            table.getStyleClass().add(HISTORY_TABLE);
        }
    }
    
    public static void clearInputFieldStyling(TextField field) {
        field.getStyleClass().removeAll(INPUT_FIELD, INPUT_FIELD_ERROR);
        field.setStyle("");
    }
    
    public static void clearButtonStyling(Button button) {
        button.getStyleClass().removeAll(PRIMARY_BUTTON, SECONDARY_BUTTON, DEBUG_BUTTON);
        button.setStyle("");
    }
}
