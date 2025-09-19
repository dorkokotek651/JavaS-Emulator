package fx.controller;

import engine.api.SEmulatorEngine;
import engine.api.SProgram;
import engine.api.SInstruction;
import engine.exception.SProgramException;
import engine.model.InstructionType;
import engine.model.FunctionRegistry;
import engine.model.SProgramImpl;
import engine.model.instruction.InstructionFactory;
import engine.model.SEmulatorConstants;
import engine.expansion.ExpansionEngine;
import engine.model.SEmulatorEngineImpl;
import fx.model.ExecutionHistoryRow;
import fx.model.InstructionTableRow;
import fx.model.VariableTableRow;
import fx.service.FileService;
import fx.util.StyleManager;
import fx.util.ActionButtonCellFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    

    @FXML private MenuItem loadFileMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem aboutMenuItem;
    

    @FXML private VBox topControlsSection;
    @FXML private Button loadFileButton;
    @FXML private Label currentFilePathLabel;
    @FXML private ComboBox<String> programFunctionSelector;
    @FXML private ComboBox<String> levelSelector;
    @FXML private Label levelDisplayLabel;
    @FXML private ComboBox<String> highlightSelectionCombo;
    

    @FXML private SplitPane middleSplitPane;
    @FXML private TableView<InstructionTableRow> instructionsTable;
    @FXML private TableColumn<InstructionTableRow, String> commandNumberColumn;
    @FXML private TableColumn<InstructionTableRow, String> commandTypeColumn;
    @FXML private TableColumn<InstructionTableRow, String> labelColumn;
    @FXML private TableColumn<InstructionTableRow, String> cyclesColumn;
    @FXML private TableColumn<InstructionTableRow, String> instructionColumn;
    @FXML private Label summaryLabel;
    

    @FXML private VBox debugControlsSection;
    @FXML private Button startRunButton;
    @FXML private Button startDebugButton;
    @FXML private Button stepOverButton;
    @FXML private Button stopButton;
    @FXML private Button resumeButton;
    

    @FXML private VBox variablesSection;
    @FXML private TableView<VariableTableRow> variablesTable;
    @FXML private TableColumn<VariableTableRow, String> variableNameColumn;
    @FXML private TableColumn<VariableTableRow, String> variableValueColumn;
    

    @FXML private VBox executionInputsSection;
    @FXML private VBox inputsContainer;
    @FXML private Button addInputButton;
    @FXML private Button removeInputButton;
    

    @FXML private VBox cyclesSection;
    @FXML private Label cyclesLabel;
    

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
    

    @FXML private Label statusLabel;
    

    private SEmulatorEngine engine;
    private FileController fileController;
    private ExecutionController executionController;
    private InputController inputController;
    private HighlightController highlightController;
    private Stage primaryStage;
    private int currentExpansionLevel = 0;
    private String currentContextProgram = "Main Program";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.engine = new SEmulatorEngineImpl();
            FileService fileService = new FileService(engine);
            

            this.fileController = new FileController(engine, fileService);
            setupFileControllerCallbacks();
            

            this.executionController = new ExecutionController(engine);
            setupExecutionControllerCallbacks();
            

            this.inputController = new InputController();
            setupInputControllerCallbacks();
            

            this.highlightController = new HighlightController();
            setupHighlightControllerCallbacks();
            
            initializeTableColumns();
            initializeControlStates();
            initializeExecutionHistory();
            initializeTableStyling();
            updateStatusLabel("S-Emulator initialized successfully");
        } catch (SProgramException e) {
            updateStatusLabel("Error: Failed to initialize S-Emulator engine - " + e.getMessage());
        }
    }
    
    private void initializeTableColumns() {

        commandNumberColumn.setCellValueFactory(cellData -> cellData.getValue().commandNumberProperty());
        commandTypeColumn.setCellValueFactory(cellData -> cellData.getValue().commandTypeProperty());
        labelColumn.setCellValueFactory(cellData -> cellData.getValue().labelProperty());
        cyclesColumn.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty());
        instructionColumn.setCellValueFactory(cellData -> cellData.getValue().instructionProperty());
        

        variableNameColumn.setCellValueFactory(cellData -> cellData.getValue().variableNameProperty());
        variableValueColumn.setCellValueFactory(cellData -> cellData.getValue().variableValueProperty());
        

        instructionsTable.setRowFactory(tv -> new TableRow<>());
        

        variablesTable.setRowFactory(tv -> {
            TableRow<VariableTableRow> row = new TableRow<>() {
                @Override
                protected void updateItem(VariableTableRow item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        getStyleClass().remove("variable-changed");
                    } else {

                        item.changedProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal) {
                                if (!getStyleClass().contains("variable-changed")) {
                                    getStyleClass().add("variable-changed");
                                }
                            } else {
                                getStyleClass().remove("variable-changed");
                            }
                        });
                        

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
        

        historyCommandNumberColumn.setCellValueFactory(cellData -> cellData.getValue().commandNumberProperty());
        historyCommandTypeColumn.setCellValueFactory(cellData -> cellData.getValue().commandTypeProperty());
        historyLabelColumn.setCellValueFactory(cellData -> cellData.getValue().labelProperty());
        historyCyclesColumn.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty());
        historyInstructionColumn.setCellValueFactory(cellData -> cellData.getValue().instructionProperty());
        

        runNumberColumn.setCellValueFactory(cellData -> cellData.getValue().runNumberProperty());
        expansionLevelColumn.setCellValueFactory(cellData -> cellData.getValue().expansionLevelProperty());
        inputsColumn.setCellValueFactory(cellData -> cellData.getValue().inputsProperty());
        yValueColumn.setCellValueFactory(cellData -> cellData.getValue().yValueProperty());
        totalCyclesColumn.setCellValueFactory(cellData -> cellData.getValue().totalCyclesProperty());
        actionsColumn.setCellFactory(new ActionButtonCellFactory(executionController));
    }
    
    private void initializeControlStates() {

        levelSelector.setDisable(true);
        programFunctionSelector.setDisable(true);
        highlightController.disableHighlightSelection();
        startRunButton.setDisable(true);
        startDebugButton.setDisable(true);
        

        stepOverButton.setDisable(true);
        stopButton.setDisable(true);
        resumeButton.setDisable(true);
        

        updateProgramFunctionSelector();
        

        programFunctionSelector.setOnAction(e -> handleProgramFunctionSelection());
    }
    
    private void initializeExecutionHistory() {
        // Statistics table is now managed by ExecutionController's context-aware history manager
    }
    
    private void setupFileControllerCallbacks() {
        fileController.setStatusUpdater(this::updateStatusLabel);
        fileController.setOnProgramLoaded(this::onProgramLoaded);
        fileController.setOnProgramStateCleared(this::clearAllProgramState);
    }
    
    private void setupExecutionControllerCallbacks() {
        executionController.setStatusUpdater(this::updateStatusLabel);
        executionController.setOnHighlightingCleared(() -> highlightController.clearHighlighting());
        

        executionController.setVariablesTable(variablesTable);
        executionController.setCyclesLabel(cyclesLabel);
        executionController.setStatisticsTable(statisticsTable);
        

        executionController.setInstructionsTable(instructionsTable);
        executionController.setOnCurrentInstructionChanged(this::highlightCurrentInstruction);
        executionController.setOnVariablesChanged(this::highlightChangedVariables);
        

        executionController.setOnDebugSessionStarted(() -> {
            stepOverButton.setDisable(false);
            stopButton.setDisable(false);
            resumeButton.setDisable(false);
            startDebugButton.setDisable(true);
            startRunButton.setDisable(true);
            

            levelSelector.setDisable(true);
            

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
            

            updateControlStates();
            

            addInputButton.setDisable(false);
            removeInputButton.setDisable(false);
            executionInputsSection.setDisable(false);
        });
        
        executionController.setOnInputsPopulated(this::populateInputsFromHistory);
        executionController.setOnExpansionLevelSet(this::setExpansionLevelFromHistory);
        executionController.setGetCurrentContextProgram(this::getContextProgram);
    }
    
    private void setupInputControllerCallbacks() {
        inputController.setStatusUpdater(this::updateStatusLabel);
        

        inputController.setInputsContainer(inputsContainer);
        inputController.setRemoveInputButton(removeInputButton);
        

        inputController.initialize();
        executionController.setInputFields(inputController.getInputFields());
    }
    
    private void setupHighlightControllerCallbacks() {
        highlightController.setStatusUpdater(this::updateStatusLabel);
        

        highlightController.setHighlightSelectionCombo(highlightSelectionCombo);
        highlightController.setInstructionsTable(instructionsTable);
        

        highlightController.setHighlightCallback(this::highlightInstructionsWithCurrentProgram);
        

        highlightController.initialize();
    }
    
    private void initializeTableStyling() {

        StyleManager.applyInstructionTableStyle(instructionsTable);
        StyleManager.applyInstructionTableStyle(historyChainTable);
        StyleManager.applyVariableTableStyle(variablesTable);
        StyleManager.applyHistoryTableStyle(statisticsTable);
        

        StyleManager.applyStatusLabelStyle(statusLabel);
    }
    
    private void onProgramLoaded() {
        if (engine.isProgramLoaded()) {
            SProgram program = engine.getCurrentProgram();
            currentFilePathLabel.setText(program.getName());
            currentExpansionLevel = 0;
            currentContextProgram = "Main Program";
            executionController.setCurrentContext("Main Program");
            updateProgramFunctionSelector();
            updateProgramDisplay();
            updateLevelSelector();
            enableProgramControls();
            
            // Update input fields based on the main program's requirements
            inputController.updateInputFieldsForProgram(program);
        }
    }
    
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        if (fileController != null) {
            fileController.setPrimaryStage(primaryStage);
        }
        if (executionController != null) {
            executionController.setPrimaryStage(primaryStage);
        }
    }
    

    @FXML
    private void handleLoadFile() {
        fileController.handleLoadFile();
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
    
    @FXML
    private void handleLevelSelection() {
        String selectedLevel = levelSelector.getValue();
        if (selectedLevel != null && !selectedLevel.trim().isEmpty()) {
            try {
                int level = Integer.parseInt(selectedLevel);
                selectExpansionLevel(level);
            } catch (NumberFormatException e) {
                updateStatusLabel("Invalid level selection: " + selectedLevel);
            }
        }
    }
    
    /**
     * Selects a specific expansion level directly.
     */
    private void selectExpansionLevel(int level) {
        if (!engine.isProgramLoaded()) {
            updateStatusLabel("No program loaded");
            return;
        }
        
        SProgram contextProgram = getContextProgram();
        if (contextProgram == null) {
            updateStatusLabel("No context program available");
            return;
        }
        
        int maxLevel = contextProgram.getMaxExpansionLevel();
        
        // Validate level selection
        if (level < 0 || level > maxLevel) {
            updateStatusLabel("Invalid level: " + level + ". Must be between 0 and " + maxLevel);
            return;
        }
        
        // Update expansion level
        currentExpansionLevel = level;
            executionController.setCurrentExpansionLevel(currentExpansionLevel);
            updateProgramDisplay();
        
        updateStatusLabel("Selected expansion level: " + level + "/" + maxLevel);
    }
    
    /**
     * Updates the level selector dropdown with available levels for the current context program.
     */
    private void updateLevelSelector() {
        if (levelSelector == null) {
            return;
        }
        
        if (!engine.isProgramLoaded()) {
            levelSelector.setItems(FXCollections.observableArrayList());
            levelSelector.setValue(null);
            return;
        }
        
        SProgram contextProgram = getContextProgram();
        if (contextProgram == null) {
            levelSelector.setItems(FXCollections.observableArrayList());
            levelSelector.setValue(null);
            return;
        }
        
        int maxLevel = contextProgram.getMaxExpansionLevel();
        ObservableList<String> levelOptions = FXCollections.observableArrayList();
        
        // Add levels from 0 to maxLevel
        for (int i = 0; i <= maxLevel; i++) {
            levelOptions.add(String.valueOf(i));
        }
        
        levelSelector.setItems(levelOptions);
        
        // Set current level as selected
        levelSelector.setValue(String.valueOf(currentExpansionLevel));
        
        System.out.println("Updated level selector with levels 0-" + maxLevel + ", current: " + currentExpansionLevel);
    }
    
    
    

    
    

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
    

    @FXML
    private void handleAddInput() {
        inputController.handleAddInput();
    }
    
    @FXML
    private void handleRemoveInput() {
        inputController.handleRemoveInput();
    }
    

    
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
        

        SProgram contextProgram = getContextProgram();
        if (contextProgram != null) {
            levelDisplayLabel.setText(currentExpansionLevel + "/" + contextProgram.getMaxExpansionLevel());
        } else {
            levelDisplayLabel.setText("0/0");
        }
        

        populateInstructionsTable(program);
        

        updateSummaryLine(program);
        

        highlightController.updateHighlightDropdown(program);
        

        updateControlStates();
        
        System.out.println("Program display update completed");
    }
    
    private SProgram getCurrentDisplayProgram() {
        if (!engine.isProgramLoaded()) {
            return null;
        }
        
        SProgram contextProgram = getContextProgram();
        if (contextProgram == null) {
            return null;
        }
        
        try {
            if (currentExpansionLevel == 0) {
                System.out.println("getCurrentDisplayProgram: Returning context program at level 0: " + contextProgram.getName());
                return contextProgram;
            } else {
                System.out.println("getCurrentDisplayProgram: Expanding " + contextProgram.getName() + " to level " + currentExpansionLevel);
                
                SProgram expandedProgram;
                
                // For both main program and function wrapper programs, use the engine's expansion methods
                // because SProgramImpl.expandToLevel() is not implemented properly
                System.out.println("getCurrentDisplayProgram: Using engine expansion for " + currentContextProgram);
                
                // Create a temporary engine instance to expand the context program
                
                // For function wrapper programs, we need to use the expansion engine directly
                if ("Main Program".equals(currentContextProgram)) {
                    expandedProgram = engine.getExpandedProgram(currentExpansionLevel);
                } else {
                    // For function wrapper programs, we need to use the expansion engine directly
                    // since the wrapper program's expandToLevel() method doesn't work
                    try {
                        // Use the expansion engine to expand the wrapper program
                        ExpansionEngine expansionEngine = new ExpansionEngine();
                        expandedProgram = expansionEngine.expandProgram(contextProgram, currentExpansionLevel);
                        
                        // Ensure the expanded program has access to the function registry
                        if (expandedProgram.getFunctionRegistry() == null && engine.getCurrentProgram().getFunctionRegistry() != null) {
                            expandedProgram.setFunctionRegistry(engine.getCurrentProgram().getFunctionRegistry());
                        }
                    } catch (Exception e) {
                        System.out.println("getCurrentDisplayProgram: Error expanding function wrapper: " + e.getMessage());
                        throw e;
                    }
                }
                
                System.out.println("getCurrentDisplayProgram: Expanded program has " + expandedProgram.getInstructions().size() + " instructions");
                return expandedProgram;
            }
        } catch (Exception e) {
            System.out.println("getCurrentDisplayProgram: Error expanding: " + e.getMessage());
            updateStatusLabel("Warning: Could not expand to level " + currentExpansionLevel + ": " + e.getMessage());
            return contextProgram;
        }
    }
    
    private SProgram getContextProgram() {
        if ("Main Program".equals(currentContextProgram)) {
            return engine.getCurrentProgram();
        } else {
            FunctionRegistry registry = engine.getFunctionRegistry();
            if (registry == null) {
                return null;
            }
            
            Map<String, String> functions = registry.getAllFunctions();
            for (Map.Entry<String, String> entry : functions.entrySet()) {
                if (currentContextProgram.equals(entry.getValue())) {
                    // Create a wrapper program that contains a QUOTE instruction calling this function
                    return createFunctionWrapperProgram(entry.getKey(), entry.getValue());
                }
            }
            return null;
        }
    }
    
    private SProgram createFunctionWrapperProgram(String functionName, String userString) {
        try {
            // Get the actual function to determine its input variables
            SProgram actualFunction = engine.getFunction(functionName);
            if (actualFunction == null) {
                System.out.println("Function not found: " + functionName);
                return null;
            }
            
            // Create a simple program that calls the function
            SProgramImpl wrapperProgram = new SProgramImpl(userString);
            
            // Get the actual input variables for this function
            List<String> inputVariables = actualFunction.getInputVariables();
            String functionArguments = String.join(",", inputVariables);
            
            System.out.println("Function " + functionName + " has input variables: " + inputVariables);
            
            // Create a QUOTE instruction that calls the function with correct arguments
            Map<String, String> quoteArgs = Map.of(
                SEmulatorConstants.FUNCTION_NAME_ARG, functionName,
                SEmulatorConstants.FUNCTION_ARGUMENTS_ARG, functionArguments
            );
            
            SInstruction quoteInstruction = InstructionFactory.createInstruction(
                SEmulatorConstants.QUOTE_NAME, "y", null, quoteArgs);
            
            // Add the instruction to the wrapper program
            wrapperProgram.addInstruction(quoteInstruction);
            
            // Set the function registry so expansion works
            wrapperProgram.setFunctionRegistry(engine.getCurrentProgram().getFunctionRegistry());
            
            return wrapperProgram;
        } catch (Exception e) {
            System.out.println("Error creating function wrapper: " + e.getMessage());
            // Fallback to returning the actual function program
            return engine.getFunction(functionName);
        }
    }
    
    private void populateInstructionsTable(SProgram program) {
        System.out.println("populateInstructionsTable() called for program: " + program.getName());
        System.out.println("Current expansion level: " + currentExpansionLevel);
        
        ObservableList<InstructionTableRow> instructionData = FXCollections.observableArrayList();
        


        List<SInstruction> instructions = program.getInstructions();
        System.out.println("Found " + instructions.size() + " instructions at level " + currentExpansionLevel);
        
        for (int i = 0; i < instructions.size(); i++) {
            SInstruction instruction = instructions.get(i);
            
            String commandNumber = String.valueOf(i + 1);
            String commandType = instruction.getType() == InstructionType.BASIC ? "B" : "S";
            String label = instruction.getLabel() != null ? instruction.getLabel() : "";
            String cycles = String.valueOf(instruction.getCycles());
            String instructionText = formatInstructionDisplay(instruction);
            
            instructionData.add(new InstructionTableRow(commandNumber, commandType, label, cycles, instructionText));
        }
        
        System.out.println("Setting " + instructionData.size() + " items to instructions table");
        instructionsTable.setItems(instructionData);
        

        instructionsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> updateHistoryChain(newValue, program)
        );
        
        System.out.println("Instructions table populated successfully");
    }
    
    
    private String formatInstructionDisplay(SInstruction instruction) {
        StringBuilder display = new StringBuilder();
        

        String displayFormat = instruction.getDisplayFormat();
        if (displayFormat != null && !displayFormat.trim().isEmpty()) {
            display.append(displayFormat);
        } else {

            display.append(instruction.getName());
            if (instruction.getVariable() != null) {
                display.append(" ").append(instruction.getVariable());
            }
        }
        
        return display.toString();
    }
    
    private void updateSummaryLine(SProgram program) {
        List<SInstruction> instructions = program.getInstructions();
        

        long basicCount = instructions.stream()
            .filter(instr -> instr.getType() == InstructionType.BASIC)
            .count();
        long syntheticCount = instructions.size() - basicCount;
        
        // Get the max expansion level for the current context program
        SProgram contextProgram = getContextProgram();
        int maxLevel = contextProgram != null ? contextProgram.getMaxExpansionLevel() : 0;
        
        String summary = String.format("Program: %s | Total: %d instructions | Basic: %d | Synthetic: %d | Level: %d/%d",
            program.getName(),
            instructions.size(),
            basicCount,
            syntheticCount,
            currentExpansionLevel,
            maxLevel
        );
        
        summaryLabel.setText(summary);
    }
    
    
    private void clearProgramDisplay() {
        instructionsTable.setItems(FXCollections.observableArrayList());
        historyChainTable.setItems(FXCollections.observableArrayList());
        summaryLabel.setText("No program loaded");
        levelDisplayLabel.setText("0/0");
        highlightController.clearHighlightDropdown();
    }
    
    private void clearAllProgramState() {

        instructionsTable.setItems(FXCollections.observableArrayList());
        historyChainTable.setItems(FXCollections.observableArrayList());
        

        variablesTable.setItems(FXCollections.observableArrayList());
        

        cyclesLabel.setText("Total Cycles: 0");
        

        // Execution history is now managed by ExecutionController's context-aware history manager
        

        executionController.resetExecutionState();
        

        highlightController.clearHighlighting();
        highlightController.clearHighlightDropdown();
        

        currentExpansionLevel = 0;
        currentContextProgram = "Main Program";
        executionController.setCurrentContext("Main Program");
        executionController.setCurrentExpansionLevel(currentExpansionLevel);
        levelDisplayLabel.setText("0/0");
        

        summaryLabel.setText("Loading...");
        

        currentFilePathLabel.setText("No file loaded");
        

        updateProgramFunctionSelector();
        

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
            SProgram contextProgram = getContextProgram();
            if (contextProgram != null) {
                // Level selector is always enabled when program is loaded
                levelSelector.setDisable(false);
            }
        }
    }
    
    
    private void updateStatusLabel(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }

        System.out.println("Status: " + message);
    }
    
    /**
     * Clears the variables table.
     */
    private void clearVariablesTable() {
        if (variablesTable != null) {
            variablesTable.setItems(FXCollections.observableArrayList());
        }
    }
    
    private void highlightInstructionsWithCurrentProgram(String item) {
        if (engine.isProgramLoaded()) {
            SProgram program = getCurrentDisplayProgram();
            if (program != null) {
                highlightController.highlightInstructionsUsing(item, program);
            }
        }
    }
    
    
    private void updateProgramFunctionSelector() {
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add("Main Program");
        
        if (engine.isProgramLoaded()) {
            FunctionRegistry registry = engine.getFunctionRegistry();
            if (registry != null && !registry.isEmpty()) {
                Map<String, String> functions = registry.getAllFunctions();
                for (Map.Entry<String, String> entry : functions.entrySet()) {
                    String userString = entry.getValue();
                    items.add(userString);
                }
            }
        }
        
        programFunctionSelector.setItems(items);
        programFunctionSelector.setValue(currentContextProgram);
    }
    
    private void handleProgramFunctionSelection() {
        String selectedItem = programFunctionSelector.getValue();
        if (selectedItem == null) {
            return;
        }
        
        setContextProgram(selectedItem);
    }
    
    private void setContextProgram(String programName) {
        if (programName == null || programName.trim().isEmpty()) {
            return;
        }
        
        currentContextProgram = programName;
        
        // Update execution history context
        executionController.setCurrentContext(programName);
        
        // Clear variables table when switching contexts
        clearVariablesTable();
        
        if ("Main Program".equals(programName)) {
            updateProgramDisplay();
            updateLevelSelector();
            updateStatusLabel("Displaying main program");
            // Update input fields for main program
            inputController.updateInputFieldsForProgram(engine.getCurrentProgram());
        } else {
            updateFunctionDisplay(programName);
            updateLevelSelector();
            updateStatusLabel("Displaying function: " + programName);
            // Update input fields for the selected function
            SProgram contextProgram = getContextProgram();
            inputController.updateInputFieldsForProgram(contextProgram);
        }
    }
    
    private void updateFunctionDisplay(String functionUserString) {
        if (!engine.isProgramLoaded()) {
            return;
        }
        
        FunctionRegistry registry = engine.getFunctionRegistry();
        if (registry == null) {
            updateStatusLabel("No functions available");
            return;
        }
        
        String functionName = null;
        Map<String, String> functions = registry.getAllFunctions();
        for (Map.Entry<String, String> entry : functions.entrySet()) {
            if (functionUserString.equals(entry.getValue())) {
                functionName = entry.getKey();
                break;
            }
        }
        
        if (functionName == null) {
            updateStatusLabel("Function not found: " + functionUserString);
            return;
        }
        
        SProgram functionProgram = engine.getFunction(functionName);
        if (functionProgram == null) {
            updateStatusLabel("Function program not found: " + functionName);
            return;
        }
        
        displayFunctionProgram(functionProgram);
    }
    
    private void displayFunctionProgram(SProgram functionProgram) {
        currentExpansionLevel = 0;
        executionController.setCurrentExpansionLevel(currentExpansionLevel);
        
        // Use updateProgramDisplay to ensure proper expansion handling
        updateProgramDisplay();
    }
    
    private void updateHistoryChain(InstructionTableRow selectedRow, SProgram program) {
        if (selectedRow == null) {
            historyChainTable.setItems(FXCollections.observableArrayList());
            return;
        }
        
        try {

            int instructionIndex = Integer.parseInt(selectedRow.getCommandNumber()) - 1;
            
            if (instructionIndex >= 0 && instructionIndex < program.getInstructions().size()) {
                SInstruction instruction = program.getInstructions().get(instructionIndex);
                

                List<SInstruction> ancestryChain = instruction.getAncestryChain();
                

                ObservableList<InstructionTableRow> historyData = FXCollections.observableArrayList();
                

                historyData.add(new InstructionTableRow(
                    "1",
                    instruction.getType() == InstructionType.BASIC ? "B" : "S",
                    instruction.getLabel() != null ? instruction.getLabel() : "",
                    String.valueOf(instruction.getCycles()),
                    formatInstructionDisplay(instruction)
                ));
                

                if (!ancestryChain.isEmpty()) {

                    for (int i = 0; i < ancestryChain.size(); i++) {
                        SInstruction ancestorInstruction = ancestryChain.get(i);
                        

                        if (ancestorInstruction != instruction && 
                            !formatInstructionDisplay(ancestorInstruction).equals(formatInstructionDisplay(instruction))) {
                            
                            String commandNumber = String.valueOf(historyData.size() + 1);
                            String commandType = ancestorInstruction.getType() == InstructionType.BASIC ? "B" : "S";
                            String cycles = String.valueOf(ancestorInstruction.getCycles());
                            String instructionText = formatInstructionDisplay(ancestorInstruction);
                            

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

            historyChainTable.setItems(FXCollections.observableArrayList());
        }
    }
    
    public SEmulatorEngine getEngine() {
        return engine;
    }
    
    private void highlightCurrentInstruction(Integer instructionIndex) {
        if (instructionsTable == null) {
            return;
        }
        

        instructionsTable.getSelectionModel().clearSelection();
        
        if (instructionIndex != null && instructionIndex >= 0 && instructionIndex < instructionsTable.getItems().size()) {

            instructionsTable.getSelectionModel().select(instructionIndex);
            instructionsTable.scrollTo(instructionIndex);
            

            instructionsTable.requestFocus();
        }
    }
    
    private void highlightChangedVariables(Map<String, Integer> changedVariables) {
        if (variablesTable == null || changedVariables == null) {
            return;
        }
        

        for (VariableTableRow row : variablesTable.getItems()) {
            if (row != null) {
                row.setChanged(false);
            }
        }
        

        for (VariableTableRow row : variablesTable.getItems()) {
            if (row != null && changedVariables.containsKey(row.getVariableName())) {
                row.setChanged(true);
            }
        }
        

        variablesTable.refresh();
    }
    
    private void populateInputsFromHistory(List<Integer> historicalInputs) {
        if (inputController != null && historicalInputs != null) {
            inputController.populateInputsFromHistory(historicalInputs);
        }
    }
    
    private void setExpansionLevelFromHistory(int expansionLevel) {
        if (expansionLevel >= 0 && expansionLevel <= engine.getMaxExpansionLevel()) {
            currentExpansionLevel = expansionLevel;
            executionController.setCurrentExpansionLevel(currentExpansionLevel);
            updateLevelDisplay();
            updateProgramDisplay();
            
            // Update the dropdown selector to match the historical expansion level
            if (levelSelector != null) {
                levelSelector.setValue(String.valueOf(currentExpansionLevel));
            }
        }
    }
    
    private void updateLevelDisplay() {
        if (levelDisplayLabel != null && engine.isProgramLoaded()) {
            levelDisplayLabel.setText(currentExpansionLevel + "/" + engine.getMaxExpansionLevel());
        }
    }
}
