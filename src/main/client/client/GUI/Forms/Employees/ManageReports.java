package client.GUI.Forms.Employees;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.GUIController;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.ReportUtils;
import entity.Message;
import entity.Report;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;

import static entity.Message.DataType.REPORT;
import static entity.Message.MessageType.QUERY;

/**
 * A GUI controller for the Managers' report generation GUI.
 */
public class ManageReports extends GUIController implements Initializable {

    @FXML
    private Button btnBack;

    @FXML
    private Button btnPDF;

    @FXML
    private TextArea txtReport;

    @FXML
    private VBox paneReport;

    @FXML
    private Button btnGenerate;

    @FXML
    private VBox vboxRoot;

    @FXML
    private ComboBox<Report.ReportType> cmbReportType;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbReportType.setItems(FXCollections.observableArrayList(Report.ReportType.values()));
        cmbReportType.setConverter(new StringConverter<Report.ReportType>() {
            @Override
            public String toString(Report.ReportType object) {
                switch (object)
                {
                    case DAILY_LATED_ORDERS:
                        return "Daily Late Arrivals";
                    case DAILY_FINISHED_ORDERS:
                        return "Daily Finished Orders";
                    case DAILY_CANCELED_ORDERS:
                        return "Daily Cancelled Orders";
                    case QUARTERLY_ORDERS:
                        return "Quarterly Orders";
                    case QUARTERLY_COMPLAINTS:
                        return "Quarterly Complaints";
                    case QUARTERLY_UNAVAILABLE_PARKING_SPACES:
                        return "Quarterly Unavailable Parking Spaces";
                    case ACTIVITY_REPORT:
                        return  "Activity Report";
                    case WEEKLY_DAILY:
                        return  "Weekly Daily statistics";
                    default:
                        return object.toString();
                }
            }

            @Override
            public Report.ReportType fromString(String string) {
                return null;
            }
        });
        btnGenerate.disableProperty().bind(cmbReportType.valueProperty().isNull());
        vboxRoot.getChildren().remove(paneReport);
    }

    /**
     * Handles the "Generate Report" button click event.<br>
     * Queries the server to generate the report type selected, then displays it in a text box.
     * @param event the button click event.
     */
    @FXML
    void generateReport(ActionEvent event) {
        Message queryMessage = new Message(QUERY, REPORT, CPSClientGUI.getSession().getUserId(), cmbReportType.getValue(), CPSClientGUI.getSession().getParkingLot().getParkingLotID());
        WaitScreen waitScreen = new WaitScreen();
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                String report = (String) getMessage().getData().get(0);
                if (!vboxRoot.getChildren().contains(paneReport))
                    vboxRoot.getChildren().add(paneReport);
                txtReport.setText(report);
                waitScreen.hide();
            }
        };
        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };
        MessageTasker tasker = new MessageTasker(queryMessage,onSuccess,onFailure,"Generating report...");
        waitScreen.run(tasker);
    }

    /**
     * Handles the "Save to PDF" event by calling the {@link ReportUtils#createPDF(String, boolean)} method.
     * @param event The click event.
     */
    @FXML
    void savePDF(ActionEvent event)
    {
        ReportUtils.createPDF(txtReport.getText(), true);
    }

    /**
     * Goes back to the previous screen. The name is remnant of an older GUI scheme.
     * @param event the click event.
     */
    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(false);
    }

}
