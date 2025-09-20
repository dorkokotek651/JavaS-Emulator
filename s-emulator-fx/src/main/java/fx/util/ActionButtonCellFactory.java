package fx.util;

import fx.controller.ExecutionController;
import fx.model.ExecutionHistoryRow;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class ActionButtonCellFactory implements Callback<TableColumn<ExecutionHistoryRow, String>, TableCell<ExecutionHistoryRow, String>> {
    
    private final ExecutionController executionController;
    
    public ActionButtonCellFactory(ExecutionController executionController) {
        this.executionController = executionController;
    }
    
    @Override
    public TableCell<ExecutionHistoryRow, String> call(TableColumn<ExecutionHistoryRow, String> param) {
        return new TableCell<ExecutionHistoryRow, String>() {
            private final Button showButton = new Button("show");
            private final Button rerunButton = new Button("re-run");
            private final HBox buttonContainer = new HBox(5);
            
            {
                showButton.setText("show");
                showButton.setStyle("-fx-font-size: 11px; -fx-padding: 3 6 3 6; -fx-min-width: 40px;");
                showButton.setPrefWidth(40);
                
                rerunButton.setText("re-run");
                rerunButton.setStyle("-fx-font-size: 11px; -fx-padding: 3 6 3 6; -fx-min-width: 50px;");
                rerunButton.setPrefWidth(50);
                
                showButton.setOnAction(event -> {
                    ExecutionHistoryRow row = getTableView().getItems().get(getIndex());
                    executionController.handleShowHistoricalState(row);
                });
                
                rerunButton.setOnAction(event -> {
                    ExecutionHistoryRow row = getTableView().getItems().get(getIndex());
                    executionController.handleRerunHistoricalExecution(row);
                });
                
                buttonContainer.getChildren().addAll(showButton, rerunButton);
                buttonContainer.setSpacing(3);
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonContainer);
                }
            }
        };
    }
}
