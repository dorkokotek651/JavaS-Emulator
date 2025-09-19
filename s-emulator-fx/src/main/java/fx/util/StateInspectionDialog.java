package fx.util;

import engine.api.ExecutionResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Map;

public class StateInspectionDialog {
    
    public static void showHistoricalState(Stage parentStage, ExecutionResult executionResult, int runNumber) {
        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Historical State - Run #" + runNumber);
        dialog.setResizable(true);
        dialog.setMinWidth(500);
        dialog.setMinHeight(400);
        
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(15));
        
        Label titleLabel = new Label("Variable States at End of Run #" + runNumber);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(5);
        
        infoGrid.add(new Label("Expansion Level:"), 0, 0);
        infoGrid.add(new Label(String.valueOf(executionResult.getExpansionLevel())), 1, 0);
        
        infoGrid.add(new Label("Inputs:"), 0, 1);
        String inputsString = executionResult.getInputs().stream()
            .map(String::valueOf)
            .collect(java.util.stream.Collectors.joining(", "));
        infoGrid.add(new Label(inputsString), 1, 1);
        
        infoGrid.add(new Label("Y Value:"), 0, 2);
        infoGrid.add(new Label(String.valueOf(executionResult.getYValue())), 1, 2);
        
        infoGrid.add(new Label("Total Cycles:"), 0, 3);
        infoGrid.add(new Label(String.valueOf(executionResult.getTotalCycles())), 1, 3);
        
        Label variablesLabel = new Label("Variable States:");
        variablesLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");
        
        TableView<VariableStateRow> variablesTable = createVariablesTable(executionResult);
        
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialog.close());
        closeButton.setDefaultButton(true);
        
        mainContainer.getChildren().addAll(
            titleLabel,
            infoGrid,
            variablesLabel,
            variablesTable,
            closeButton
        );
        
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        dialog.setScene(new javafx.scene.Scene(scrollPane));
        dialog.showAndWait();
    }
    
    private static TableView<VariableStateRow> createVariablesTable(ExecutionResult executionResult) {
        TableView<VariableStateRow> table = new TableView<>();
        
        TableColumn<VariableStateRow, String> nameColumn = new TableColumn<>("Variable");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameColumn.setPrefWidth(100);
        
        TableColumn<VariableStateRow, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        valueColumn.setPrefWidth(100);
        
        TableColumn<VariableStateRow, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        typeColumn.setPrefWidth(100);
        
        table.getColumns().add(nameColumn);
        table.getColumns().add(valueColumn);
        table.getColumns().add(typeColumn);
        
        ObservableList<VariableStateRow> data = FXCollections.observableArrayList();
        
        Map<String, Integer> inputVars = executionResult.getInputVariables();
        Map<String, Integer> workingVars = executionResult.getWorkingVariables();
        
        inputVars.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                data.add(new VariableStateRow(entry.getKey(), String.valueOf(entry.getValue()), "Input"));
            });
        
        workingVars.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                data.add(new VariableStateRow(entry.getKey(), String.valueOf(entry.getValue()), "Working"));
            });
        
        data.add(new VariableStateRow("y", String.valueOf(executionResult.getYValue()), "Result"));
        
        table.setItems(data);
        table.setPrefHeight(200);
        
        return table;
    }
    
    public static class VariableStateRow {
        private final javafx.beans.property.StringProperty name;
        private final javafx.beans.property.StringProperty value;
        private final javafx.beans.property.StringProperty type;
        
        public VariableStateRow(String name, String value, String type) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.value = new javafx.beans.property.SimpleStringProperty(value);
            this.type = new javafx.beans.property.SimpleStringProperty(type);
        }
        
        public javafx.beans.property.StringProperty nameProperty() {
            return name;
        }
        
        public javafx.beans.property.StringProperty valueProperty() {
            return value;
        }
        
        public javafx.beans.property.StringProperty typeProperty() {
            return type;
        }
    }
}
