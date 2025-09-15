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

public class MainController implements Initializable {
    

    @FXML private MenuItem loadFileMenuItem;
    @FXML private MenuItem saveStateMenuItem;
    @FXML private MenuItem loadStateMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem aboutMenuItem;
    

    @FXML private VBox topControlsSection;
    @FXML private Button loadFileButton;
    @FXML private Label currentFilePathLabel;
    @FXML private ComboBox<String> programFunctionSelector;
    @FXML private Button collapseButton;
    @FXML private Label levelDisplayLabel;
    @FXML private Button expandButton;
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
            inputController.addDefaultInput();
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
            TableRow<VariableTableRow> row = new TableRow<VariableTableRow>() {
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
        actionsColumn.setCellValueFactory(cellData -> cellData.getValue().actionsProperty());
    }
    
    private void initializeControlStates() {

        collapseButton.setDisable(true);
        expandButton.setDisable(true);
        programFunctionSelector.setDisable(true);
        highlightController.disableHighlightSelection();
        startRunButton.setDisable(true);
        startDebugButton.setDisable(true);
        

        stepOverButton.setDisable(true);
        stopButton.setDisable(true);
        resumeButton.setDisable(true);
        

        programFunctionSelector.setItems(FXCollections.observableArrayList("Main Program"));
        programFunctionSelector.setValue("Main Program");
        

        programFunctionSelector.setOnAction(e -> handleProgramFunctionSelection());
    }
    
    private void initializeExecutionHistory() {
        statisticsTable.setItems(executionHistory);
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
        executionController.setExecutionHistory(executionHistory);
        

        executionController.setInstructionsTable(instructionsTable);
        executionController.setOnCurrentInstructionChanged(this::highlightCurrentInstruction);
        executionController.setOnVariablesChanged(this::highlightChangedVariables);
        

        executionController.setOnDebugSessionStarted(() -> {
            stepOverButton.setDisable(false);
            stopButton.setDisable(false);
            resumeButton.setDisable(false);
            startDebugButton.setDisable(true);
            startRunButton.setDisable(true);
            

            collapseButton.setDisable(true);
            expandButton.setDisable(true);
            

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
            updateProgramDisplay();
            enableProgramControls();
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
        

        levelDisplayLabel.setText(currentExpansionLevel + "/" + engine.getMaxExpansionLevel());
        

        populateInstructionsTable(program);
        

        updateSummaryLine(program);
        

        highlightController.updateHighlightDropdown(program);
        

        updateControlStates();
        
        System.out.println("Program display update completed");
    }
    
    private SProgram getCurrentDisplayProgram() {
        try {

            return engine.getExpandedProgram(currentExpansionLevel);
        } catch (SProgramException e) {

            updateStatusLabel("Warning: Could not expand to level " + currentExpansionLevel + ": " + e.getMessage());
            return engine.getCurrentProgram();
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
            
            System.out.println("Instruction " + commandNumber + ": " + instructionText);
            
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
        

        executionHistory.clear();
        

        executionController.resetExecutionState();
        

        highlightController.clearHighlighting();
        highlightController.clearHighlightDropdown();
        

        currentExpansionLevel = 0;
        levelDisplayLabel.setText("0/0");
        

        summaryLabel.setText("Loading...");
        

        currentFilePathLabel.setText("No file loaded");
        

        programFunctionSelector.setItems(FXCollections.observableArrayList("Main Program"));
        programFunctionSelector.setValue("Main Program");
        

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

        System.out.println("Status: " + message);
    }
    
    private void highlightInstructionsWithCurrentProgram(String item) {
        if (engine.isProgramLoaded()) {
            SProgram program = getCurrentDisplayProgram();
            if (program != null) {
                highlightController.highlightInstructionsUsing(item, program);
            }
        }
    }
    
    
    private void handleProgramFunctionSelection() {
        String selectedItem = programFunctionSelector.getValue();
        if (selectedItem == null) {
            return;
        }
        
        if ("Main Program".equals(selectedItem)) {

            updateProgramDisplay();
            updateStatusLabel("Displaying main program");
        } else {

            updateStatusLabel("Function display will be implemented when function support is added");
        }
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
}
