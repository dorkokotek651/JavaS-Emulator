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
import fx.util.StyleManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.Node;
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
    @FXML private VBox topControlsSection;
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
    private FileController fileController;
    private ExecutionController executionController;
    private InputController inputController;
    private HighlightController highlightController;
    private Stage primaryStage;
    private int currentExpansionLevel = 0;
    private ObservableList<ExecutionHistoryRow> executionHistory = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.engine = new SEmulatorEngineImpl();
            this.fileService = new FileService(engine);
            
            // Initialize FileController
            this.fileController = new FileController(engine, fileService);
            setupFileControllerCallbacks();
            
            // Initialize ExecutionController
            this.executionController = new ExecutionController(engine);
            setupExecutionControllerCallbacks();
            
            // Initialize InputController
            this.inputController = new InputController();
            setupInputControllerCallbacks();
            
            // Initialize HighlightController
            this.highlightController = new HighlightController();
            setupHighlightControllerCallbacks();
            
            initializeTableColumns();
            initializeControlStates();
            initializeExecutionHistory();
            initializeTableStyling();
            inputController.addDefaultInput();
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
        
        // Set row factory for instructions table (prevents null row factory issues)
        instructionsTable.setRowFactory(tv -> new TableRow<>());
        
        // Set row factory for variables table to apply changed highlighting
        variablesTable.setRowFactory(tv -> {
            TableRow<VariableTableRow> row = new TableRow<VariableTableRow>() {
                @Override
                protected void updateItem(VariableTableRow item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        getStyleClass().remove("variable-changed");
                    } else {
                        // Listen for changes to the changed property
                        item.changedProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal) {
                                if (!getStyleClass().contains("variable-changed")) {
                                    getStyleClass().add("variable-changed");
                                }
                            } else {
                                getStyleClass().remove("variable-changed");
                            }
                        });
                        
                        // Apply initial styling
                        if (item.isChanged()) {
                            if (!getStyleClass().contains("variable-changed")) {
                                getStyleClass().add("variable-changed");
                            }
                        } else {
                            getStyleClass().remove("variable-changed");
                        }
                    }
                }
            };
            return row;
        });
        
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
        highlightController.disableHighlightSelection();
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
     * Sets up callbacks for FileController communication.
     */
    private void setupFileControllerCallbacks() {
        fileController.setStatusUpdater(this::updateStatusLabel);
        fileController.setOnProgramLoaded(this::onProgramLoaded);
        fileController.setOnProgramStateCleared(this::clearAllProgramState);
    }
    
    /**
     * Sets up callbacks and references for ExecutionController communication.
     */
    private void setupExecutionControllerCallbacks() {
        executionController.setStatusUpdater(this::updateStatusLabel);
        executionController.setOnHighlightingCleared(() -> highlightController.clearHighlighting());
        
        // Set UI component references (input fields will be set after InputController setup)
        executionController.setVariablesTable(variablesTable);
        executionController.setCyclesLabel(cyclesLabel);
        executionController.setStatisticsTable(statisticsTable);
        executionController.setExecutionHistory(executionHistory);
        
        // Set debug-specific callbacks
        executionController.setInstructionsTable(instructionsTable);
        executionController.setOnCurrentInstructionChanged(this::highlightCurrentInstruction);
        executionController.setOnVariablesChanged(this::highlightChangedVariables);
        
        // Set debug button state management callbacks
        executionController.setOnDebugSessionStarted(() -> {
            stepOverButton.setDisable(false);
            stopButton.setDisable(false);
            resumeButton.setDisable(false);
            startDebugButton.setDisable(true);
            startRunButton.setDisable(true);
            
            // Disable expand/collapse buttons during debug
            collapseButton.setDisable(true);
            expandButton.setDisable(true);
            
            // Disable input controls during debug
            addInputButton.setDisable(true);
            removeInputButton.setDisable(true);
            executionInputsSection.setDisable(true);
        });
        
        executionController.setOnDebugSessionEnded(() -> {
            stepOverButton.setDisable(true);
            stopButton.setDisable(true);
            resumeButton.setDisable(true);
            startDebugButton.setDisable(false);
            startRunButton.setDisable(false);
            
            // Re-enable expand/collapse buttons after debug
            updateControlStates();
            
            // Re-enable input controls after debug
            addInputButton.setDisable(false);
            removeInputButton.setDisable(false);
            executionInputsSection.setDisable(false);
        });
    }
    
    /**
     * Sets up callbacks and references for InputController communication.
     */
    private void setupInputControllerCallbacks() {
        inputController.setStatusUpdater(this::updateStatusLabel);
        
        // Set UI component references
        inputController.setInputsContainer(inputsContainer);
        inputController.setRemoveInputButton(removeInputButton);
        
        // Initialize and connect with ExecutionController
        inputController.initialize();
        executionController.setInputFields(inputController.getInputFields());
    }
    
    /**
     * Sets up callbacks and references for HighlightController communication.
     */
    private void setupHighlightControllerCallbacks() {
        highlightController.setStatusUpdater(this::updateStatusLabel);
        
        // Set UI component references
        highlightController.setHighlightSelectionCombo(highlightSelectionCombo);
        highlightController.setInstructionsTable(instructionsTable);
        
        // Set callback for highlighting with program context
        highlightController.setHighlightCallback(this::highlightInstructionsWithCurrentProgram);
        
        // Initialize
        highlightController.initialize();
    }
    
    /**
     * Initializes CSS styling for tables and other components.
     */
    private void initializeTableStyling() {
        // Apply table styles
        StyleManager.applyInstructionTableStyle(instructionsTable);
        StyleManager.applyInstructionTableStyle(historyChainTable);
        StyleManager.applyVariableTableStyle(variablesTable);
        StyleManager.applyHistoryTableStyle(statisticsTable);
        
        // Apply status label style
        StyleManager.applyStatusLabelStyle(statusLabel);
    }
    
    /**
     * Called when a program is successfully loaded.
     */
    private void onProgramLoaded() {
        if (engine.isProgramLoaded()) {
            SProgram program = engine.getCurrentProgram();
            currentFilePathLabel.setText(program.getName());
            currentExpansionLevel = 0;
            updateProgramDisplay();
            enableProgramControls();
        }
    }
    
    
    /**
     * Sets the primary stage reference for file dialogs.
     * 
     * @param primaryStage the primary stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        if (fileController != null) {
            fileController.setPrimaryStage(primaryStage);
        }
        if (executionController != null) {
            executionController.setPrimaryStage(primaryStage);
        }
    }
    
    // File menu handlers
    @FXML
    private void handleLoadFile() {
        fileController.handleLoadFile();
    }
    
    
    @FXML
    private void handleSaveState() {
        fileController.handleSaveState();
    }
    
    @FXML
    private void handleLoadState() {
        fileController.handleLoadState();
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
            executionController.setCurrentExpansionLevel(currentExpansionLevel);
            updateProgramDisplay();
        }
    }
    
    @FXML
    private void handleExpand() {
        if (engine.isProgramLoaded() && currentExpansionLevel < engine.getMaxExpansionLevel()) {
            currentExpansionLevel++;
            executionController.setCurrentExpansionLevel(currentExpansionLevel);
            updateProgramDisplay();
        }
    }
    
    // Execution control handlers
    @FXML
    private void handleStartRun() {
        executionController.handleStartRun();
    }
    
    @FXML
    private void handleStartDebug() {
        executionController.handleStartDebug();
    }
    
    @FXML
    private void handleStepOver() {
        executionController.handleStepOver();
    }
    
    @FXML
    private void handleStop() {
        executionController.handleStop();
    }
    
    @FXML
    private void handleResume() {
        executionController.handleResume();
    }
    
    // Input management handlers
    @FXML
    private void handleAddInput() {
        inputController.handleAddInput();
    }
    
    @FXML
    private void handleRemoveInput() {
        inputController.handleRemoveInput();
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
        highlightController.updateHighlightDropdown(program);
        
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
     * Clears the program display when no program is loaded.
     */
    private void clearProgramDisplay() {
        instructionsTable.setItems(FXCollections.observableArrayList());
        historyChainTable.setItems(FXCollections.observableArrayList());
        summaryLabel.setText("No program loaded");
        levelDisplayLabel.setText("0/0");
        highlightController.clearHighlightDropdown();
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
        
        // Reset execution controller state
        executionController.resetExecutionState();
        
        // Clear highlighting
        highlightController.clearHighlighting();
        highlightController.clearHighlightDropdown();
        
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
        inputController.clearInputFieldStyling();
        
        updateStatusLabel("Clearing previous program state...");
    }
    
    private void enableProgramControls() {
        programFunctionSelector.setDisable(false);
        highlightController.enableHighlightSelection();
        startRunButton.setDisable(false);
        startDebugButton.setDisable(false);
    }
    
    private void updateControlStates() {
        if (engine.isProgramLoaded()) {
            collapseButton.setDisable(currentExpansionLevel == 0);
            expandButton.setDisable(currentExpansionLevel >= engine.getMaxExpansionLevel());
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
     * Helper method to highlight instructions with current program context.
     * 
     * @param item the item to highlight
     */
    private void highlightInstructionsWithCurrentProgram(String item) {
        if (engine.isProgramLoaded()) {
            SProgram program = getCurrentDisplayProgram();
            if (program != null) {
                highlightController.highlightInstructionsUsing(item, program);
            }
        }
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
                
                // Populate history chain table - show selected instruction first, then its parents if they differ
                ObservableList<InstructionTableRow> historyData = FXCollections.observableArrayList();
                
                // First row: Always show the selected instruction
                historyData.add(new InstructionTableRow(
                    "1",
                    instruction.getType() == InstructionType.BASIC ? "B" : "S",
                    instruction.getLabel() != null ? instruction.getLabel() : "",
                    String.valueOf(instruction.getCycles()),
                    formatInstructionDisplay(instruction)
                ));
                
                // Subsequent rows: Show ancestry chain only if it represents actual different instructions
                if (!ancestryChain.isEmpty()) {
                    // Filter out ancestors that are identical to the current instruction
                    for (int i = 0; i < ancestryChain.size(); i++) {
                        SInstruction ancestorInstruction = ancestryChain.get(i);
                        
                        // Skip if this ancestor is essentially the same as the selected instruction
                        if (ancestorInstruction != instruction && 
                            !formatInstructionDisplay(ancestorInstruction).equals(formatInstructionDisplay(instruction))) {
                            
                            String commandNumber = String.valueOf(historyData.size() + 1);
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
                    }
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
    
    /**
     * Highlights the current instruction during debug mode.
     * 
     * @param instructionIndex the index of the current instruction, or -1 to clear highlighting
     */
    private void highlightCurrentInstruction(Integer instructionIndex) {
        if (instructionsTable == null) {
            return;
        }
        
        // Clear previous selection
        instructionsTable.getSelectionModel().clearSelection();
        
        if (instructionIndex != null && instructionIndex >= 0 && instructionIndex < instructionsTable.getItems().size()) {
            // Highlight current instruction using selection
            instructionsTable.getSelectionModel().select(instructionIndex);
            instructionsTable.scrollTo(instructionIndex);
            
            // Focus on the table to make the selection visible
            instructionsTable.requestFocus();
        }
    }
    
    /**
     * Highlights variables that changed during the last debug step.
     * 
     * @param changedVariables map of variable names to their new values
     */
    private void highlightChangedVariables(Map<String, Integer> changedVariables) {
        if (variablesTable == null || changedVariables == null) {
            return;
        }
        
        // Clear previous highlighting
        for (VariableTableRow row : variablesTable.getItems()) {
            if (row != null) {
                row.setChanged(false);
            }
        }
        
        // Highlight changed variables
        for (VariableTableRow row : variablesTable.getItems()) {
            if (row != null && changedVariables.containsKey(row.getVariableName())) {
                row.setChanged(true);
            }
        }
        
        // Refresh table to apply highlighting
        variablesTable.refresh();
    }
}
