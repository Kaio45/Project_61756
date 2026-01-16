package client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label; 
import javafx.stage.Stage;
import common.ActionType;
import common.Message;

/**
 * Controller for the Monthly Reports window.
 * Displays graphical reports (Pie Chart and Bar Chart) regarding order statuses
 * and restaurant activity (arrival/departure times).
 * @author Group-17
 * @version 1.0
 */
public class ReportController {

    /** Pie chart displaying the distribution of order statuses. */
    @FXML private PieChart statusPieChart;      

    /** Bar chart displaying customer arrivals and departures by hour. */
    @FXML private BarChart<String, Number> activityChart; 

    /** Label for displaying the punctuality summary. */
    @FXML private Label delayLabel; 

    /**
     * Sends a request to the server to fetch the monthly report data.
     * Sets the current controller instance in ChatClient to handle the response.
     */
    public void requestReportData() {
        if (ClientUI.chat != null) {
            ChatClient.reportController = this; 
            ClientUI.chat.handleMessageFromClientUI(new Message(ActionType.GET_REPORT, null));
        }
    }

    /**
     * Updates the charts and labels with data received from the server.
     * This method runs on the JavaFX Application Thread.
     *
     * @param data a comma-separated string containing all report statistics
     */
    public void updateReport(String data) {
        Platform.runLater(() -> {
            try {
                String[] parts = data.split(",");
                
                // --- Part 1: Pie Chart Data ---
                int total = Integer.parseInt(parts[0]);
                int cancelled = Integer.parseInt(parts[1]);
                int noShow = Integer.parseInt(parts[2]);
                int waiting = Integer.parseInt(parts[3]);
                
                int approved = total - cancelled - noShow;
                if (approved < 0) approved = 0;

                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Approved (" + approved + ")", approved),
                    new PieChart.Data("Cancelled (" + cancelled + ")", cancelled),
                    new PieChart.Data("No-Show (" + noShow + ")", noShow),
                    new PieChart.Data("Waiting (" + waiting + ")", waiting)
                );
                statusPieChart.setData(pieData);
                statusPieChart.setTitle("Orders Status");

                // --- Part 2: Bar Chart Data ---
                activityChart.setAnimated(false);
                activityChart.getData().clear();
                activityChart.getXAxis().setLabel("Hour");
                activityChart.getYAxis().setLabel("Customers");

                XYChart.Series<String, Number> seriesArrived = new XYChart.Series<>();
                seriesArrived.setName("Arrived");
                XYChart.Series<String, Number> seriesLeft = new XYChart.Series<>();
                seriesLeft.setName("Left");
                
                int startHour = 12;

                // Loop for 12 hours (12:00 to 23:00)
                for (int i = 0; i < 12; i++) {
                    String timeLabel = String.format("%02d:00", startHour + i);
                    
                    // Safe parsing using fixed indices
                    int arrivedVal = Integer.parseInt(parts[4 + i]);
                    int leftVal = Integer.parseInt(parts[16 + i]);
                    
                    seriesArrived.getData().add(new XYChart.Data<>(timeLabel, arrivedVal));
                    seriesLeft.getData().add(new XYChart.Data<>(timeLabel, leftVal));
                }
                activityChart.getData().addAll(seriesArrived, seriesLeft);

                // --- Part 3: Update Delay Label ---
                if (parts.length >= 30) {
                    int lateVal = Integer.parseInt(parts[28]);   // Late arrivals
                    int actualVal = Integer.parseInt(parts[29]); // Total arrivals
                    
                    updateDelayLabel(actualVal, lateVal);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Updates the punctuality summary label.
     *
     * @param actualArrivals total number of customers who arrived
     * @param trueLate number of customers who arrived late (>10 mins)
     */
    private void updateDelayLabel(int actualArrivals, int trueLate) {
        if (delayLabel == null) return; 

        if (actualArrivals == 0) {
            delayLabel.setText("No arrivals data yet.");
            return;
        }

        double latePct = ((double) trueLate / actualArrivals) * 100;
        
        String text = String.format(
            "Punctuality Report:\n" +
            "Total Arrived: %d\n" +
            "❌ Late (>10m): %d (%.1f%%)",
            actualArrivals, trueLate, latePct
        );
        delayLabel.setText(text);
    }

    /**
     * Overloaded method for updating the delay label with detailed stats.
     *
     * @param approved total approved orders
     * @param waiting total orders in waiting list
     * @param noShow total no-shows
     * @param trueLate total late arrivals
     */
    private void updateDelayLabel(int approved, int waiting, int noShow, int trueLate) {
        if (delayLabel == null) return; 

        int arrived = approved; 
        if (arrived == 0) arrived = 1; 
        
        double latePct = ((double) trueLate / arrived) * 100;

        String text = String.format(
            "Punctuality Report:\n" +
            "✔ Arrived On Time: %d\n" +
            "⚠ Delayed (Waiting List): %d\n" +
            "❌ Actual Late Arrivals (>10m): %d (%.1f%% of diners)\n" +
            "❌ No-Show (Did not arrive): %d",
            (arrived - trueLate), waiting, trueLate, latePct, noShow
        );
        
        delayLabel.setText(text);
    }

    /**
     * Closes the report window.
     * @param event the button click event
     */
    @FXML
    public void closeWindow(ActionEvent event) {
        ((Stage) activityChart.getScene().getWindow()).close();
    }
}