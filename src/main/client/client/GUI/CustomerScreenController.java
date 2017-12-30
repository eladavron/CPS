package client.GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class CustomerScreenController {

    @FXML
    private Button btnCreateOrder;

    @FXML
    private Button btnEnterParking;

    @FXML
    private Button btnFileComplaint;

    @FXML
    private Button btnCheckComplaint;

    @FXML
    private Button btnExitParking;

    @FXML
    private Button btnManageSubs;

    @FXML
    private Button btnEditOrder;

    @FXML
    void handleCustomerButton(ActionEvent event) throws IOException {
        if (event.getSource() == btnCreateOrder)
        {
            CPSClientGUI.getInstance().changeGUI("NewOrder.fxml");
        }
    }
}
