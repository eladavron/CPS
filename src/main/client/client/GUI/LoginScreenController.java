package client.GUI;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;

public class LoginScreenController {

    private CPSClientGUI parentGUI;

    @FXML
    private TextField txtLoginUsr;

    @FXML
    private Button btnLogin;

    @FXML
    private TitledPane paneLogin;

    @FXML
    private Accordion loginRoot;

    @FXML
    private TitledPane paneRegister;

    @FXML
    private PasswordField txtLoginPwd;

    @FXML
    private TextField txtPort;

    @FXML
    private Button btnConnect;

    @FXML
    private TextField txtHostname;

    private CPSClientGUI _guiInstance;

    @FXML
    void initialize() {
        _guiInstance = CPSClientGUI.getInstance();
        assert paneLogin != null : "fx:id=\"paneLogin\" was not injected: check your FXML file 'LoginScreen.fxml'.";
        assert loginRoot != null : "fx:id=\"loginRoot\" was not injected: check your FXML file 'LoginScreen.fxml'.";
        assert paneRegister != null : "fx:id=\"paneRegister\" was not injected: check your FXML file 'LoginScreen.fxml'.";
        txtPort.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    txtPort.setText(newValue.replaceAll("[^\\d]",""));
                }
            }
        });
        loginRoot.setExpandedPane(paneLogin);
    }

    @FXML
    void attemptLogin(ActionEvent event) throws IOException {
        try {
            if (txtHostname.getText().equals(""))
            {
                Helpers.showError(txtHostname,"Hostname can not be empty!");
                _guiInstance.setStatus("Hostname can not be empty!", Color.RED);
                return;
            }
            if (txtPort.getText().equals("") || Integer.valueOf(txtPort.getText()) < 0)
            {
                Helpers.showError(txtPort, "Invalid port number!");
                _guiInstance.setStatus("Invalid port number!", Color.RED);
                return;
            }
            String host = txtHostname.getText();
            Integer port = Integer.valueOf(txtPort.getText());
            _guiInstance.setStatus("Connecting...", Color.BLACK);
            _guiInstance.connect(host, port); //TODO: Waiting screen
            _guiInstance.changeGUI("CustomerScreen.fxml"); //TODO: Change screen according to use
        }
        catch (IOException io)
        {
            _guiInstance.setStatus("Connection error! Please check information and try again.", Color.RED);
        }
    }
}
