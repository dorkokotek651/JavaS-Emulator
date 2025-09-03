package fx.controller;

import engine.api.SEmulatorEngine;
import engine.api.SProgram;
import engine.api.SInstruction;
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
    @FXML private TableColumn<InstructionTableRow, String> historyCyclesColumn;
    @FXML private TableColumn<InstructionTableRow, String> historyInstructionColumn;
    
    @FXML private TableView<ExecutionHistoryRow> statisticsTable;
    @FXML private TableColumn<ExecutionHistoryRow, String> runNumberColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> expansionLevelColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> inputsColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> yValueColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> totalCyclesColumn;
    @FXML private TableColumn<ExecutionHistoryRow, String> actionsColumn;
    
    // Application state
    private SEmulatorEngine engine;
    private FileService fileService;
    private Stage primaryStage;
    private int currentExpansionLevel = 0;
    private List<TextField> inputFields = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.engine = new SEmulatorEngineImpl();
            this.fileService = new FileService(engine);
            initializeTableColumns();
            initializeControlStates();
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
        cyclesColumn.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty());
        instructionColumn.setCellValueFactory(cellData -> cellData.getValue().instructionProperty());
        
        // Variables table columns
        variableNameColumn.setCellValueFactory(cellData -> cellData.getValue().variableNameProperty());
        variableValueColumn.setCellValueFactory(cellData -> cellData.getValue().variableValueProperty());
        
        // History chain table columns
        historyCommandNumberColumn.setCellValueFactory(cellData -> cellData.getValue().commandNumberProperty());
        historyCommandTypeColumn.setCellValueFactory(cellData -> cellData.getValue().commandTypeProperty());
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
            loadProgramFileWithProgress(selectedFile);
        }
    }
    
    /**
     * Loads a program file with progress indication.
     */
    private void loadProgramFileWithProgress(File file) {
        fileService.loadProgramFileWithProgress(
            file, 
            primaryStage,
            () -> {
                // Success callback
                currentFilePathLabel.setText(file.getName());
                currentExpansionLevel = 0;
                updateProgramDisplay();
                enableProgramControls();
                updateStatusLabel("Program loaded successfully: " + file.getName());
            },
            (errorMessage) -> {
                // Error callback
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
        updateStatusLabel("Start run functionality will be implemented in Phase 4");
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
        if (!engine.isProgramLoaded()) {
            clearProgramDisplay();
            return;
        }
        
        SProgram program = getCurrentDisplayProgram();
        if (program == null) {
            clearProgramDisplay();
            return;
        }
        
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
    }
    
    /**
     * Gets the program to display based on current expansion level.
     */
    private SProgram getCurrentDisplayProgram() {
        if (currentExpansionLevel == 0) {
            return engine.getCurrentProgram();
        }
        
        try {
            // Use engine's expansion method instead of program's expandToLevel
            String expandedDisplay = engine.expandProgram(currentExpansionLevel);
            
            // For now, return the original program and let the display method handle expansion
            // This will be improved when we implement proper expansion display
            return engine.getCurrentProgram();
        } catch (Exception e) {
            // If expansion fails, fall back to original program
            updateStatusLabel("Warning: Could not expand to level " + currentExpansionLevel + ": " + e.getMessage());
            return engine.getCurrentProgram();
        }
    }
    
    /**
     * Populates the instructions table with program data.
     */
    private void populateInstructionsTable(SProgram program) {
        ObservableList<InstructionTableRow> instructionData = FXCollections.observableArrayList();
        
        List<SInstruction> instructions = program.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            SInstruction instruction = instructions.get(i);
            
            String commandNumber = String.valueOf(i + 1); // 1-based indexing
            String commandType = instruction.getType() == InstructionType.BASIC ? "B" : "S";
            String cycles = String.valueOf(instruction.getCycles());
            String instructionText = formatInstructionDisplay(instruction);
            
            instructionData.add(new InstructionTableRow(commandNumber, commandType, cycles, instructionText));
        }
        
        instructionsTable.setItems(instructionData);
        
        // Add selection listener for history chain display
        instructionsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> updateHistoryChain(newValue, program)
        );
    }
    
    /**
     * Formats an instruction for display in the table.
     */
    private String formatInstructionDisplay(SInstruction instruction) {
        StringBuilder display = new StringBuilder();
        
        // Add label if present
        String label = instruction.getLabel();
        if (label != null && !label.trim().isEmpty()) {
            display.append("[").append(label).append("] ");
        }
        
        // Add instruction display format
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
        
        // Add validation for natural numbers only
        inputField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                inputField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        inputFields.add(inputField);
        inputsContainer.getChildren().add(inputField);
        
        // Enable remove button if we have more than one input
        removeInputButton.setDisable(inputFields.size() <= 1);
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
        currentFilePathLabel.setText(message);
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
        
        List<SInstruction> instructions = program.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            SInstruction instruction = instructions.get(i);
            
            boolean shouldHighlight = 
                item.equals(instruction.getLabel()) ||
                item.equals(instruction.getVariable()) ||
                instruction.getArguments().values().contains(item);
            
            if (shouldHighlight) {
                // Select and highlight the row
                instructionsTable.getSelectionModel().select(i);
                
                // Apply highlighting style (will be enhanced in later phases)
                InstructionTableRow row = instructionsTable.getItems().get(i);
                // For now, just select the row - visual highlighting will be improved later
            }
        }
        
        updateStatusLabel("Highlighted instructions using: " + item);
    }
    
    /**
     * Clears instruction highlighting.
     */
    private void clearHighlighting() {
        instructionsTable.getSelectionModel().clearSelection();
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
                        
                        historyData.add(new InstructionTableRow(commandNumber, commandType, cycles, instructionText));
                    }
                    
                    // Add the current instruction as the final result
                    historyData.add(new InstructionTableRow(
                        String.valueOf(ancestryChain.size() + 1),
                        instruction.getType() == InstructionType.BASIC ? "B" : "S",
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
