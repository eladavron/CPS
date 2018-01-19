package client.GUI.Forms.Employees;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.ReportCell;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.GUIController;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.Refreshable;
import entity.FinalReport;
import entity.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static entity.Message.DataType.FINAL_REPORT;
import static entity.Message.MessageType.QUERY;

public class ManageAllReports extends GUIController implements Initializable, Refreshable {

    @FXML
    private Button btnBack;

    @FXML
    private Button btnRefresh;

    @FXML
    private ListView<FinalReport> listViewAllReports;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listViewAllReports.setCellFactory(new Callback<ListView<FinalReport>, ListCell<FinalReport>>() {
            @Override
            public ListCell<FinalReport> call(ListView<FinalReport> param) {
                return new ReportCell();
            }
        });
        Platform.runLater(()->initReports());
    }

    private void initReports()
    {
        WaitScreen waitScreen = new WaitScreen();
        Message query = new Message(QUERY, FINAL_REPORT);
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                ArrayList<FinalReport> results = new ArrayList<FinalReport>();
                for (Object obj : getMessage().getData())
                {
                    if (obj instanceof FinalReport)
                    {
                        results.add((FinalReport) obj);
                    }
                }
                listViewAllReports.setItems(FXCollections.observableArrayList(results));
                waitScreen.hide();
            }
        };
        MessageRunnable onFailed = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };
        MessageTasker taskQuery = new MessageTasker(query, onSuccess,onFailed, "Getting Reports...");
        waitScreen.run(taskQuery);
    }

    @FXML
    void refreshReports(ActionEvent event) {
        initReports();
    }


    @Override
    public void refresh() {
        initReports();
    }

    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(false);
    }

}