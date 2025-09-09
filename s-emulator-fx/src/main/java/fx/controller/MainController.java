package fx.controller;

import engine.api.SEmulatorEngine;
import engine.api.SProgram;
import engine.api.SInstruction;
import engine.api.ExecutionResult;
import engine.exception.SProgramException;
import engine.exception.ExpansionException;
import engine.model.InstructionType;
import engine.model.SEmulatorEngineImpl;
import fx.model.ExecutionHistoryRow;
import fx.model.InstructionTableRow;
import fx.model.VariableTableRow;
import fx.service.FileService;
import fx.util.ErrorDialogUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Main controller for the S-Emulator JavaFX application.
 * Handles the primary window and coordinates between different UI components.
 */
public class MainController implements Initializable {
    
    // Menu items
    @FXML private MenuItem loadFileMenuItem;
    @FXML private MenuItem saveStateMenuItem;
    @FXML private MenuItem loadStateMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem aboutMenuItem;
    
    // Top section controls
    @FXML private HBox topControlsSection;
    @FXML private Button loadFileButton;
    @FXML private Label currentFilePathLabel;
    @FXML private ComboBox<String> programFunctionSelector;
    @FXML private Button collapseButton;
    @FXML private Label levelDisplayLabel;
    @FXML private Button expandButton;
    @FXML private ComboBox<String> highlightSelectionCombo;
    
    // Middle section - Instructions table
    @FXML private SplitPane middleSplitPane;
    @FXML private TableView<InstructionTableRow> instructionsTable;
    @FXML private TableColumn<InstructionTableRow, String> commandNumberColumn;
    @FXML private TableColumn<InstructionTableRow, String> commandTypeColumn;
    @FXML private TableColumn<InstructionTableRow, String> labelColumn;
    @FXML private TableColumn<InstructionTableRow, String> cyclesColumn;
    @FXML private TableColumn<InstructionTableRow, String> instructionColumn;
    @FXML private Label summaryLabel;
    
    // Middle section - Debug/Execution panel
    @FXML private VBox debugControlsSection;
    @FXML private Button startRunButton;
    @FXML private Button startDebugButton;
    @FXML private Button stepOverButton;
    @FXML private Button stopButton;
    @FXML private Button resumeButton;
    
    // Variables section (pink)
    @FXML private VBox variablesSection;
    @FXML private TableView<VariableTableRow> variablesTable;
    @FXML private TableColumn<VariableTableRow, String> variableNameColumn;
    @FXML private TableColumn<VariableTableRow, String> variableValueColumn;
    
    // Execution inputs section (blue)
    @FXML private VBox executionInputsSection;
    @FXML private VBox inputsContainer;
    @FXML private Button addInputButton;
    @FXML private Button removeInputButton;
    
    // Cycles section (pink)
    @FXML private VBox cyclesSection;
    @FXML private Label cyclesLabel;
    
    // Bottom section - History and statistics
    @FXML private SplitPane bottomSplitPane;
    @FXML private TableView<InstructionTableRow> historyChainTable;
    @FXML private TableColumn<InstructionTableRow, String> historyCommandNumberColumn;
    @FXML private TableColumn<InstructionTableRow, String> historyCommandTypeColumn;
    @FXML private TableColumn<InstructionTableRow, String> historyLabelColumn;
    @FXML private TableColumn<InstructionTableRow, String> historyCyclesColumn;
    @FXML private TableColumn<InstructionTableRow, String> historyInstructionColumn;
    
    @FXML private TableView<ExecutionHistoryRow> statisticsTable;
    @FXML private TableColumn<ExecutionHistoryRow, String> runNumberColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> expansionLevelColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> inputsColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> yValueColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> totalCyclesColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> actionsColumn;
    
    // Status bar
    @FXML private Label statusLabel;
    
    // Application state
    private SEmulatorEngine engine;
    private FileService fileService;
    private Stage primaryStage;
    private int currentExpansionLevel = 0;
    private List<TextField> inputFields = new ArrayList<>();
    private ObservableList<ExecutionHistoryRow> executionHistory = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.engine = new SEmulatorEngineImpl();
            this.fileService = new FileService(engine);
            initializeTableColumns();
            initializeControlStates();
            initializeExecutionHistory();
            addDefaultInput();
            updateStatusLabel("S-Emulator initialized successfully");
        } catch (SProgramException e) {
            updateStatusLabel("Error: Failed to initialize S-Emulator engine - " + e.getMessage());
        }
    }
    
    /**
     * Initializes table column bindings.
     */
    private void initializeTableColumns() {
        // Instructions table columns
        commandNumberColumn.setCellValueFactory(cellData -> cellData.getValue().commandNumberProperty());
        commandTypeColumn.setCellValueFactory(cellData -> cellData.getValue().commandTypeProperty());
        labelColumn.setCellValueFactory(cellData -> cellData.getValue().labelProperty());
        cyclesColumn.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty());
        instructionColumn.setCellValueFactory(cellData -> cellData.getValue().instructionProperty());
        
        // Variables table columns
        variableNameColumn.setCellValueFactory(cellData -> cellData.getValue().variableNameProperty());
        variableValueColumn.setCellValueFactory(cellData -> cellData.getValue().variableValueProperty());
        
        // History chain table columns
        historyCommandNumberColumn.setCellValueFactory(cellData -> cellData.getValue().commandNumberProperty());
        historyCommandTypeColumn.setCellValueFactory(cellData -> cellData.getValue().commandTypeProperty());
        historyLabelColumn.setCellValueFactory(cellData -> cellData.getValue().labelProperty());
        historyCyclesColumn.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty());
        historyInstructionColumn.setCellValueFactory(cellData -> cellData.getValue().instructionProperty());
        
        // Statistics table columns
        runNumberColumn.setCellValueFactory(cellData -> cellData.getValue().runNumberProperty());
        expansionLevelColumn.setCellValueFactory(cellData -> cellData.getValue().expansionLevelProperty());
        inputsColumn.setCellValueFactory(cellData -> cellData.getValue().inputsProperty());
        yValueColumn.setCellValueFactory(cellData -> cellData.getValue().yValueProperty());
        totalCyclesColumn.setCellValueFactory(cellData -> cellData.getValue().totalCyclesProperty());
        actionsColumn.setCellValueFactory(cellData -> cellData.getValue().actionsProperty());
    }
    
    /**
     * Initializes control states and bindings.
     */
    private void initializeControlStates() {
        // Disable controls that require a loaded program
        collapseButton.setDisable(true);
        expandButton.setDisable(true);
        programFunctionSelector.setDisable(true);
        highlightSelectionCombo.setDisable(true);
        startRunButton.setDisable(true);
        startDebugButton.setDisable(true);
        
        // Disable debug controls initially
        stepOverButton.setDisable(true);
        stopButton.setDisable(true);
        resumeButton.setDisable(true);
        
        // Set up program/function selector
        programFunctionSelector.setItems(FXCollections.observableArrayList("Main Program"));
        programFunctionSelector.setValue("Main Program");
        
        // Add selection listener
        programFunctionSelector.setOnAction(e -> handleProgramFunctionSelection());
    }
    
    /**
     * Initializes the execution history table.
     */
    private void initializeExecutionHistory() {
        statisticsTable.setItems(executionHistory);
    }
    
    /**
     * Adds a default input field.
     */
    private void addDefaultInput() {
        addInputField();
    }
    
    /**
     * Sets the primary stage reference for file dialogs.
     * 
     * @param primaryStage the primary stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    // File menu handlers
    @FXML
    private void handleLoadFile() {
        if (primaryStage == null) {
            updateStatusLabel("Error: No primary stage available for file dialog");
            return;
        }
        
        File selectedFile = fileService.showLoadFileDialog(primaryStage);
        if (selectedFile != null) {
            updateStatusLabel("Selected file: " + selectedFile.getName());
            loadProgramFileDirectly(selectedFile);
        }
    }
    
    /**
     * Loads a program file directly without progress dialog for testing.
     */
    private void loadProgramFileDirectly(File file) {
        updateStatusLabel("Loading file: " + file.getName());
        
        try {
            // Clear all previous program state before loading new program
            clearAllProgramState();
            
            // Direct engine loading for debugging
            engine.loadProgram(file.getAbsolutePath());
            
            updateStatusLabel("Engine loading completed");
            
            // Check if program was actually loaded
            if (engine.isProgramLoaded()) {
                SProgram program = engine.getCurrentProgram();
                updateStatusLabel("Program loaded: " + program.getName() + " with " + program.getInstructions().size() + " instructions");
                
                // Update UI
                currentFilePathLabel.setText(file.getName());
                currentExpansionLevel = 0;
                updateProgramDisplay();
                enableProgramControls();
                
            } else {
                updateStatusLabel("Error: Program not loaded in engine");
            }
            
        } catch (SProgramException e) {
            updateStatusLabel("Error loading program: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "File Loading Failed", e.getMessage());
        } catch (Exception e) {
            updateStatusLabel("Unexpected error: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Unexpected Error", e.getMessage());
        }
    }
    
    /**
     * Loads a program file with progress indication.
     */
    private void loadProgramFileWithProgress(File file) {
        // Clear all previous program state before loading new program
        clearAllProgramState();
        
        fileService.loadProgramFileWithProgress(
            file, 
            primaryStage,
            () -> {
                // Success callback - debug what's happening
                System.out.println("File load success callback triggered");
                System.out.println("Engine program loaded: " + engine.isProgramLoaded());
                
                if (engine.isProgramLoaded()) {
                    SProgram program = engine.getCurrentProgram();
                    System.out.println("Program: " + (program != null ? program.getName() : "null"));
                    System.out.println("Instructions count: " + (program != null ? program.getInstructions().size() : "N/A"));
                }
                
                currentFilePathLabel.setText(file.getName());
                currentExpansionLevel = 0;
                updateProgramDisplay();
                enableProgramControls();
                updateStatusLabel("Program loaded successfully: " + file.getName());
            },
            (errorMessage) -> {
                // Error callback - debug what's happening
                System.out.println("File load error callback triggered: " + errorMessage);
                ErrorDialogUtil.showError(primaryStage, "File Loading Failed", errorMessage);
                updateStatusLabel("Failed to load file: " + file.getName());
            }
        );
    }
    
    @FXML
    private void handleSaveState() {
        updateStatusLabel("Save state functionality will be implemented in Phase 6");
    }
    
    @FXML
    private void handleLoadState() {
        // When loading state is implemented, we should also clear previous state
        // clearAllProgramState(); // Uncomment when state loading is implemented
        updateStatusLabel("Load state functionality will be implemented in Phase 6");
    }
    
    @FXML
    private void handleExit() {
        if (primaryStage != null) {
            primaryStage.close();
        }
    }
    
    @FXML
    private void handleAbout() {
        updateStatusLabel("S-Emulator v1.0 - A JavaFX implementation of the S programming language emulator");
    }
    
    // Top section control handlers
    @FXML
    private void handleCollapse() {
        if (currentExpansionLevel > 0) {
            currentExpansionLevel--;
            updateProgramDisplay();
            updateStatusLabel("Program collapsed to level " + currentExpansionLevel);
        }
    }
    
    @FXML
    private void handleExpand() {
        if (engine.isProgramLoaded() && currentExpansionLevel < engine.getMaxExpansionLevel()) {
            currentExpansionLevel++;
            updateProgramDisplay();
            updateStatusLabel("Program expanded to level " + currentExpansionLevel);
        }
    }
    
    // Execution control handlers
    @FXML
    private void handleStartRun() {
        if (!engine.isProgramLoaded()) {
            updateStatusLabel("Error: No program loaded");
            return;
        }
        
        try {
            // Validate and collect inputs
            if (!validateInputs()) {
                updateStatusLabel("Error: Please correct invalid input values (must be natural numbers ≥ 0)");
                return;
            }
            
            List<Integer> inputs = collectInputs();
            updateStatusLabel("Running program with inputs: " + inputs + " at expansion level " + currentExpansionLevel);
            
            // Execute the program
            ExecutionResult result = engine.runProgram(currentExpansionLevel, inputs);
            
            // Update UI with results
            updateExecutionResults(result);
            clearHighlighting(); // Clear any previous highlighting
            
            updateStatusLabel("Program execution completed. Y = " + result.getYValue() + ", Total cycles: " + result.getTotalCycles());
            
        } catch (IllegalArgumentException e) {
            updateStatusLabel("Error: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Input Validation Error", e.getMessage());
        } catch (Exception e) {
            updateStatusLabel("Error during execution: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Execution Error", 
                "Failed to execute program: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleStartDebug() {
        updateStatusLabel("Start debug functionality will be implemented in Phase 5");
    }
    
    @FXML
    private void handleStepOver() {
        updateStatusLabel("Step over functionality will be implemented in Phase 5");
    }
    
    @FXML
    private void handleStop() {
        updateStatusLabel("Stop functionality will be implemented in Phase 5");
    }
    
    @FXML
    private void handleResume() {
        updateStatusLabel("Resume functionality will be implemented in Phase 5");
    }
    
    // Input management handlers
    @FXML
    private void handleAddInput() {
        addInputField();
    }
    
    @FXML
    private void handleRemoveInput() {
        removeInputField();
    }
    
    // Private helper methods
    
    private void updateProgramDisplay() {
        System.out.println("updateProgramDisplay() called");
        System.out.println("Engine loaded: " + engine.isProgramLoaded());
        
        if (!engine.isProgramLoaded()) {
            System.out.println("No program loaded, clearing display");
            clearProgramDisplay();
            return;
        }
        
        SProgram program = getCurrentDisplayProgram();
        System.out.println("Display program: " + (program != null ? program.getName() : "null"));
        
        if (program == null) {
            System.out.println("Display program is null, clearing display");
            clearProgramDisplay();
            return;
        }
        
        System.out.println("Updating display for program: " + program.getName());
        
        // Update level display
        levelDisplayLabel.setText(currentExpansionLevel + "/" + engine.getMaxExpansionLevel());
        
        // Update instructions table with real data
        populateInstructionsTable(program);
        
        // Update summary line
        updateSummaryLine(program);
        
        // Update highlight dropdown
        updateHighlightDropdown(program);
        
        // Update control states
        updateControlStates();
        
        System.out.println("Program display update completed");
    }
    
    /**
     * Gets the program to display based on current expansion level.
     */
    private SProgram getCurrentDisplayProgram() {
        try {
            // Use the new structured API to get expanded program
            return engine.getExpandedProgram(currentExpansionLevel);
        } catch (SProgramException e) {
            // If expansion fails, fall back to original program
            updateStatusLabel("Warning: Could not expand to level " + currentExpansionLevel + ": " + e.getMessage());
            return engine.getCurrentProgram();
        }
    }
    
    /**
     * Populates the instructions table with program data.
     */
    private void populateInstructionsTable(SProgram program) {
        System.out.println("populateInstructionsTable() called for program: " + program.getName());
        System.out.println("Current expansion level: " + currentExpansionLevel);
        
        ObservableList<InstructionTableRow> instructionData = FXCollections.observableArrayList();
        
        // Now we can use consistent logic for all expansion levels
        // The program parameter already contains the expanded instructions
        List<SInstruction> instructions = program.getInstructions();
        System.out.println("Found " + instructions.size() + " instructions at level " + currentExpansionLevel);
        
        for (int i = 0; i < instructions.size(); i++) {
            SInstruction instruction = instructions.get(i);
            
            String commandNumber = String.valueOf(i + 1); // 1-based indexing
            String commandType = instruction.getType() == InstructionType.BASIC ? "B" : "S";
            String label = instruction.getLabel() != null ? instruction.getLabel() : "";
            String cycles = String.valueOf(instruction.getCycles());
            String instructionText = formatInstructionDisplay(instruction);
            
            System.out.println("Instruction " + commandNumber + ": " + instructionText);
            
            instructionData.add(new InstructionTableRow(commandNumber, commandType, label, cycles, instructionText));
        }
        
        System.out.println("Setting " + instructionData.size() + " items to instructions table");
        instructionsTable.setItems(instructionData);
        
        // Add selection listener for history chain display
        instructionsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> updateHistoryChain(newValue, program)
        );
        
        System.out.println("Instructions table populated successfully");
    }
    
    
    /**
     * Formats an instruction for display in the table.
     */
    private String formatInstructionDisplay(SInstruction instruction) {
        StringBuilder display = new StringBuilder();
        
        // Add instruction display format (without label since we have a separate column)
        String displayFormat = instruction.getDisplayFormat();
        if (displayFormat != null && !displayFormat.trim().isEmpty()) {
            display.append(displayFormat);
        } else {
            // Fallback to basic format
            display.append(instruction.getName());
            if (instruction.getVariable() != null) {
                display.append(" ").append(instruction.getVariable());
            }
        }
        
        return display.toString();
    }
    
    /**
     * Updates the summary line with program information.
     */
    private void updateSummaryLine(SProgram program) {
        List<SInstruction> instructions = program.getInstructions();
        
        // Count basic vs synthetic instructions
        long basicCount = instructions.stream()
            .filter(instr -> instr.getType() == InstructionType.BASIC)
            .count();
        long syntheticCount = instructions.size() - basicCount;
        
        String summary = String.format("Program: %s | Total: %d instructions | Basic: %d | Synthetic: %d | Level: %d/%d",
            program.getName(),
            instructions.size(),
            basicCount,
            syntheticCount,
            currentExpansionLevel,
            engine.getMaxExpansionLevel()
        );
        
        summaryLabel.setText(summary);
    }
    
    /**
     * Updates the highlight dropdown with available labels and variables.
     */
    private void updateHighlightDropdown(SProgram program) {
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
     * Clears the program display when no program is loaded.
     */
    private void clearProgramDisplay() {
        instructionsTable.setItems(FXCollections.observableArrayList());
        historyChainTable.setItems(FXCollections.observableArrayList());
        summaryLabel.setText("No program loaded");
        levelDisplayLabel.setText("0/0");
        highlightSelectionCombo.setItems(FXCollections.observableArrayList());
    }
    
    /**
     * Clears all program-related state when loading a new program.
     * This ensures a clean slate for the new program.
     */
    private void clearAllProgramState() {
        // Clear instruction tables
        instructionsTable.setItems(FXCollections.observableArrayList());
        historyChainTable.setItems(FXCollections.observableArrayList());
        
        // Clear variables table
        variablesTable.setItems(FXCollections.observableArrayList());
        
        // Reset cycles display
        cyclesLabel.setText("Total Cycles: 0");
        
        // Clear execution history
        executionHistory.clear();
        
        // Clear highlighting
        clearHighlighting();
        highlightSelectionCombo.setItems(FXCollections.observableArrayList());
        highlightSelectionCombo.setValue(null);
        
        // Reset expansion level
        currentExpansionLevel = 0;
        levelDisplayLabel.setText("0/0");
        
        // Reset summary
        summaryLabel.setText("Loading...");
        
        // Reset file path display
        currentFilePathLabel.setText("No file loaded");
        
        // Reset program selector
        programFunctionSelector.setItems(FXCollections.observableArrayList("Main Program"));
        programFunctionSelector.setValue("Main Program");
        
        // Clear any input field error styling
        for (TextField inputField : inputFields) {
            inputField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 3px; " +
                              "-fx-border-radius: 3px; -fx-border-color: #ddd; -fx-border-width: 1px;");
        }
        
        updateStatusLabel("Clearing previous program state...");
    }
    
    private void enableProgramControls() {
        programFunctionSelector.setDisable(false);
        highlightSelectionCombo.setDisable(false);
        startRunButton.setDisable(false);
        startDebugButton.setDisable(false);
    }
    
    private void updateControlStates() {
        if (engine.isProgramLoaded()) {
            collapseButton.setDisable(currentExpansionLevel == 0);
            expandButton.setDisable(currentExpansionLevel >= engine.getMaxExpansionLevel());
        }
    }
    
    private void addInputField() {
        TextField inputField = new TextField();
        inputField.setPromptText("Input " + (inputFields.size() + 1));
        inputField.setPrefWidth(80);
        
        // Enhanced styling for better visual appearance
        inputField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 3px; " +
                          "-fx-border-radius: 3px; -fx-border-color: #ddd; -fx-border-width: 1px;");
        
        // Add validation for natural numbers only with enhanced feedback
        inputField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                inputField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            // Clear any error styling when user types valid input
            if (newValue.matches("\\d*")) {
                inputField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 3px; " +
                                  "-fx-border-radius: 3px; -fx-border-color: #ddd; -fx-border-width: 1px;");
            }
        });
        
        // Add focus listener for better user experience
        inputField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Highlight field when focused
                inputField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 3px; " +
                                  "-fx-border-radius: 3px; -fx-border-color: #2196F3; -fx-border-width: 2px;");
            } else {
                // Reset to normal style when focus lost
                inputField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 3px; " +
                                  "-fx-border-radius: 3px; -fx-border-color: #ddd; -fx-border-width: 1px;");
                
                if (inputField.getText().isEmpty()) {
                    // Set default value of 0 when field loses focus and is empty
                    inputField.setText("0");
                }
            }
        });
        
        // Add tooltip for better user guidance
        Tooltip tooltip = new Tooltip("Enter a natural number (≥ 0). Default: 0");
        inputField.setTooltip(tooltip);
        
        inputFields.add(inputField);
        inputsContainer.getChildren().add(inputField);
        
        // Enable remove button if we have more than one input
        removeInputButton.setDisable(inputFields.size() <= 1);
        
        // Focus the new field for better user experience
        inputField.requestFocus();
    }
    
    private void removeInputField() {
        if (inputFields.size() > 1) {
            TextField lastField = inputFields.remove(inputFields.size() - 1);
            inputsContainer.getChildren().remove(lastField);
            
            // Disable remove button if we only have one input left
            removeInputButton.setDisable(inputFields.size() <= 1);
        }
    }
    
    private void updateStatusLabel(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
        // Also print to console for debugging
        System.out.println("Status: " + message);
    }
    
    /**
     * Collects input values from all input fields.
     * 
     * @return list of integer inputs, with 0 as default for empty fields
     */
    private List<Integer> collectInputs() {
        List<Integer> inputs = new ArrayList<>();
        
        for (TextField inputField : inputFields) {
            String text = inputField.getText().trim();
            if (text.isEmpty()) {
                inputs.add(0); // Default value for empty fields
            } else {
                try {
                    int value = Integer.parseInt(text);
                    if (value < 0) {
                        // Highlight invalid field with enhanced error styling
                        inputField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 3px; " +
                                          "-fx-border-radius: 3px; -fx-border-color: #f44336; -fx-border-width: 2px; " +
                                          "-fx-background-color: #ffebee;");
                        throw new NumberFormatException("Negative numbers not allowed");
                    }
                    inputs.add(value);
                } catch (NumberFormatException e) {
                    // Highlight invalid field with enhanced error styling
                    inputField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 3px; " +
                                      "-fx-border-radius: 3px; -fx-border-color: #f44336; -fx-border-width: 2px; " +
                                      "-fx-background-color: #ffebee;");
                    throw new IllegalArgumentException("Invalid input in field " + (inputFields.indexOf(inputField) + 1) + ": " + text);
                }
            }
        }
        
        return inputs;
    }
    
    /**
     * Validates all input fields and returns true if all are valid.
     * 
     * @return true if all inputs are valid natural numbers, false otherwise
     */
    private boolean validateInputs() {
        boolean allValid = true;
        
        for (TextField inputField : inputFields) {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                try {
                    int value = Integer.parseInt(text);
                    if (value < 0) {
                        inputField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 3px; " +
                                          "-fx-border-radius: 3px; -fx-border-color: #f44336; -fx-border-width: 2px; " +
                                          "-fx-background-color: #ffebee;");
                        allValid = false;
                    } else {
                        inputField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 3px; " +
                                          "-fx-border-radius: 3px; -fx-border-color: #ddd; -fx-border-width: 1px;");
                    }
                } catch (NumberFormatException e) {
                    inputField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 3px; " +
                                      "-fx-border-radius: 3px; -fx-border-color: #f44336; -fx-border-width: 2px; " +
                                      "-fx-background-color: #ffebee;");
                    allValid = false;
                }
            } else {
                inputField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 3px; " +
                                  "-fx-border-radius: 3px; -fx-border-color: #ddd; -fx-border-width: 1px;");
            }
        }
        
        return allValid;
    }
    
    /**
     * Updates the UI with execution results.
     * 
     * @param result the execution result to display
     */
    private void updateExecutionResults(ExecutionResult result) {
        // Update variables table
        updateVariablesTable(result);
        
        // Update cycles display
        updateCyclesDisplay(result.getTotalCycles());
        
        // Add to execution history
        addToExecutionHistory(result);
        
        System.out.println("Execution completed: Run " + result.getRunNumber() + 
                          ", Y = " + result.getYValue() + 
                          ", Cycles = " + result.getTotalCycles());
    }
    
    /**
     * Updates the variables table with execution results.
     * 
     * @param result the execution result containing variable values
     */
    private void updateVariablesTable(ExecutionResult result) {
        ObservableList<VariableTableRow> variableData = FXCollections.observableArrayList();
        
        // Add input variables (x1, x2, x3, ...)
        Map<String, Integer> inputVars = result.getInputVariables();
        for (Map.Entry<String, Integer> entry : inputVars.entrySet()) {
            variableData.add(new VariableTableRow(entry.getKey(), String.valueOf(entry.getValue())));
        }
        
        // Add working variables (z1, z2, z3, ...)
        Map<String, Integer> workingVars = result.getWorkingVariables();
        for (Map.Entry<String, Integer> entry : workingVars.entrySet()) {
            variableData.add(new VariableTableRow(entry.getKey(), String.valueOf(entry.getValue())));
        }
        
        // Add y variable (result)
        variableData.add(new VariableTableRow("y", String.valueOf(result.getYValue())));
        
        // Sort variables in proper order: x1, x2, ..., z1, z2, ..., y
        variableData.sort((v1, v2) -> {
            String name1 = v1.getVariableName();
            String name2 = v2.getVariableName();
            
            // y always comes last
            if ("y".equals(name1)) return 1;
            if ("y".equals(name2)) return -1;
            
            // x variables come before z variables
            if (name1.startsWith("x") && name2.startsWith("z")) return -1;
            if (name1.startsWith("z") && name2.startsWith("x")) return 1;
            
            // Within same type, sort numerically
            return name1.compareTo(name2);
        });
        
        variablesTable.setItems(variableData);
    }
    
    /**
     * Updates the cycles display with the total execution cycles.
     * 
     * @param totalCycles the total number of cycles executed
     */
    private void updateCyclesDisplay(int totalCycles) {
        cyclesLabel.setText("Total Cycles: " + totalCycles);
    }
    
    /**
     * Adds an execution result to the execution history table.
     * 
     * @param result the execution result to add
     */
    private void addToExecutionHistory(ExecutionResult result) {
        // Format inputs as comma-separated string
        String inputsString = result.getInputs().stream()
            .map(String::valueOf)
            .collect(java.util.stream.Collectors.joining(", "));
        
        // For Phase 4, use placeholder for actions (will be implemented in Phase 6)
        String actions = "show | re-run";
        
        ExecutionHistoryRow historyRow = new ExecutionHistoryRow(
            String.valueOf(result.getRunNumber()),
            String.valueOf(result.getExpansionLevel()),
            inputsString,
            String.valueOf(result.getYValue()),
            String.valueOf(result.getTotalCycles()),
            actions
        );
        
        executionHistory.add(historyRow);
        
        // Scroll to the new row for user visibility
        statisticsTable.scrollTo(historyRow);
    }
    
    /**
     * Handles highlighting selection from dropdown.
     */
    private void handleHighlightSelection() {
        String selectedItem = highlightSelectionCombo.getValue();
        if (selectedItem == null || selectedItem.equals("Clear Highlighting") || selectedItem.startsWith("---")) {
            clearHighlighting();
            return;
        }
        
        highlightInstructionsUsing(selectedItem);
    }
    
    /**
     * Highlights instructions that use the specified label or variable.
     */
    private void highlightInstructionsUsing(String item) {
        if (!engine.isProgramLoaded()) {
            return;
        }
        
        SProgram program = getCurrentDisplayProgram();
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
            updateStatusLabel("Highlighted " + rowsToHighlight.size() + " instructions using: " + item);
        } else {
            updateStatusLabel("No instructions found using: " + item);
        }
    }
    
    /**
     * Clears instruction highlighting.
     */
    private void clearHighlighting() {
        instructionsTable.getSelectionModel().clearSelection();
        // Reset to single selection mode for normal operation
        instructionsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        updateStatusLabel("Highlighting cleared");
    }
    
    /**
     * Handles program/function selection from dropdown.
     */
    private void handleProgramFunctionSelection() {
        String selectedItem = programFunctionSelector.getValue();
        if (selectedItem == null) {
            return;
        }
        
        if ("Main Program".equals(selectedItem)) {
            // Display main program
            updateProgramDisplay();
            updateStatusLabel("Displaying main program");
        } else {
            // Display selected function (placeholder for future function support)
            updateStatusLabel("Function display will be implemented when function support is added");
        }
    }
    
    /**
     * Updates the history chain display for a selected instruction.
     */
    private void updateHistoryChain(InstructionTableRow selectedRow, SProgram program) {
        if (selectedRow == null) {
            historyChainTable.setItems(FXCollections.observableArrayList());
            return;
        }
        
        try {
            // Get the instruction index (convert from 1-based to 0-based)
            int instructionIndex = Integer.parseInt(selectedRow.getCommandNumber()) - 1;
            
            if (instructionIndex >= 0 && instructionIndex < program.getInstructions().size()) {
                SInstruction instruction = program.getInstructions().get(instructionIndex);
                
                // Get ancestry chain
                List<SInstruction> ancestryChain = instruction.getAncestryChain();
                
                // Populate history chain table (reverse order - most historical at bottom)
                ObservableList<InstructionTableRow> historyData = FXCollections.observableArrayList();
                
                if (ancestryChain.isEmpty()) {
                    // If no ancestry, show the instruction itself
                    historyData.add(new InstructionTableRow(
                        "1",
                        instruction.getType() == InstructionType.BASIC ? "B" : "S",
                        instruction.getLabel() != null ? instruction.getLabel() : "",
                        String.valueOf(instruction.getCycles()),
                        formatInstructionDisplay(instruction) + " (Original)"
                    ));
                } else {
                    // Show full ancestry chain (most historical first, most recent last)
                    for (int i = 0; i < ancestryChain.size(); i++) {
                        SInstruction ancestorInstruction = ancestryChain.get(i);
                        String commandNumber = String.valueOf(i + 1);
                        String commandType = ancestorInstruction.getType() == InstructionType.BASIC ? "B" : "S";
                        String cycles = String.valueOf(ancestorInstruction.getCycles());
                        String instructionText = formatInstructionDisplay(ancestorInstruction);
                        
                        // Add level information to show expansion history
                        if (i == 0) {
                            instructionText += " (Original)";
                        } else {
                            instructionText += " (Level " + i + ")";
                        }
                        
                        historyData.add(new InstructionTableRow(commandNumber, commandType, 
                            ancestorInstruction.getLabel() != null ? ancestorInstruction.getLabel() : "", 
                            cycles, instructionText));
                    }
                    
                    // Add the current instruction as the final result
                    historyData.add(new InstructionTableRow(
                        String.valueOf(ancestryChain.size() + 1),
                        instruction.getType() == InstructionType.BASIC ? "B" : "S",
                        instruction.getLabel() != null ? instruction.getLabel() : "",
                        String.valueOf(instruction.getCycles()),
                        formatInstructionDisplay(instruction) + " (Current)"
                    ));
                }
                
                historyChainTable.setItems(historyData);
            }
        } catch (NumberFormatException e) {
            // Handle parsing error gracefully
            historyChainTable.setItems(FXCollections.observableArrayList());
        }
    }
    
    /**
     * Gets the engine instance for use by other controllers.
     * 
     * @return the S-Emulator engine instance
     */
    public SEmulatorEngine getEngine() {
        return engine;
    }
}
