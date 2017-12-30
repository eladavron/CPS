package client.GUI;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConnectionController implements Initializable {

    @FXML
    private TextField txtPort;

    @FXML
    private Button btnConnect;

    @FXML
    private Label lblStatus;

    @FXML
    private TextField txtHostname;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtPort.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    txtPort.setText(newValue.replaceAll("[^\\d]",""));
                }
            }
        });
    }

    @FXML
    void attemptConnection(ActionEvent event) {
        try {
            if (txtHostname.getText().equals(""))
            {
                Helpers.showTooltip(txtHostname,"Hostname can not be empty!");
                return;
            }
            if (txtPort.getText().equals("") || Integer.valueOf(txtPort.getText()) < 0)
            {
                Helpers.showTooltip(txtPort, "Invalid port number!");
                return;
            }
            String host = txtHostname.getText();
            Integer port = Integer.valueOf(txtPort.getText());
            lblStatus.setText("Connecting...");
            CPSClientGUI.getInstance().connect(host, port); //TODO: Waiting screen
            CPSClientGUI.getInstance().changeGUI("CustomerScreen.fxml"); //TODO: Change screen according to use
        }
        catch (IOException io)
        {
            lblStatus.setText("Connection error! Please check information and try asgain.");
            lblStatus.setTextFill(Color.RED);
        }
    }

}
