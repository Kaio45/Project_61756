package client;

import java.util.ArrayList;
import common.Order;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableRow; 
import javafx.stage.Stage;

/**
 * Controller for the Order History window.
 * Displays a list of orders in a table and allows selection to view details.
 * @author Group-17
 * @version 1.0
 */
public class HistoryController {

    @FXML private TableView<Order> historyTable; 
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colTime;
    @FXML private TableColumn<Order, Integer> colGuests;
    @FXML private TableColumn<Order, String> colStatus;
    
    private OrderFrameController mainController;

    /**
     * Initializes the controller class.
     * Sets up table columns and row double-click listeners.
     */
    public void initialize() {
        // Bind columns to Order properties
        colId.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().get_order_number()).asObject());

        colDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get_order_date()));

        colTime.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get_order_time()));

        colGuests.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().get_number_of_guests()).asObject());

        colStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get_status()));
        
        // Handle row double-click to load order details
        historyTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Order rowData = row.getItem();
                    
                    // Check if bridge exists
                    if (mainController != null) {
                        mainController.loadOrderFromHistory(rowData);
                        
                        // Close history window after selection
                        ((Stage)row.getScene().getWindow()).close();
                    }
                }
            });
            return row;
        });
    }

    /**
     * Sets the main controller to allow callbacks when an order is selected.
     * @param controller the main OrderFrameController
     */
    public void setMainController(OrderFrameController controller) {
        this.mainController = controller;
    }

    /**
     * Populates the history table with a list of orders.
     * @param orders the list of orders to display
     */
    public void setOrderList(ArrayList<Order> orders) {
        ObservableList<Order> list = FXCollections.observableArrayList(orders);
        historyTable.setItems(list);
    }

    @FXML
    public void closeWindow(ActionEvent event) {
        ((Stage) historyTable.getScene().getWindow()).close();
    }
}