package client.GUI.Forms;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.Common;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import entity.Message;
import entity.ParkingLot;
import entity.Session;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.SocketException;

public class LoginScreenController {

    //TODO: Figure out why you get double lists when you navigate back here.

    private CPSClientGUI parentGUI;

    @FXML
    private TextField txtLoginUsr;

    @FXML
    private ComboBox<ParkingLot> cmbParkingLots;

    @FXML
    private TitledPane paneLogin;

    @FXML
    private Accordion loginRoot;

    @FXML
    private TitledPane paneRegister;

    @FXML
    private TitledPane paneConnection;

    @FXML
    private PasswordField txtLoginPwd;

    @FXML
    private TextField txtPort;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnConnect;


    @FXML
    private TextField txtHostname;

    @FXML
    void initialize() {
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
        if (CPSClientGUI.getConnectionStatus() != CPSClientGUI.ConnectionStatus.RESET)
        {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    attemptConnect(); //This is in a "runlater" so that it runs AFTER the gui is loaded.
                }
            });
        } else { //Connection was reset, need to reconnect.
            setConnectedGUI(false);
        }
    }

    /**
     * Handle the "connect" button.
     * @param event the clicking event.
     */
    @FXML
    private void attemptConnect(ActionEvent event)
    {
        attemptConnect();
    }

    /**
     * Attempts a connection to the server.
     */
    private void attemptConnect()
    {
        WaitScreen waitScreen = new WaitScreen();
        if (CPSClientGUI.getClient() != null && CPSClientGUI.getClient().isConnected()) //If already connected
        {
            Common.initParkingLots(cmbParkingLots, true);
        }

        /*
        Validate Form
         */
        if (txtHostname.getText().equals(""))
        {
            Common.showError(txtHostname,"Hostname can not be empty!");
            CPSClientGUI.setStatus("Hostname can not be empty!", Color.RED);
            return;
        }
        if (txtPort.getText().equals("") || Integer.valueOf(txtPort.getText()) < 0)
        {
            Common.showError(txtPort, "Invalid port number!");
            CPSClientGUI.setStatus("Invalid port number!", Color.RED);
            return;
        }
        String host = txtHostname.getText();
        Integer port = Integer.valueOf(txtPort.getText());

        Task<Void> _attemptConnect = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Connecting...");
                try {
                    CPSClientGUI.connect(host, port);
                } catch (IOException io){
                    throw io;
                }
                return null;
            }
        };
        _attemptConnect.setOnFailed(event ->
        {
            waitScreen.setOnClose(new Runnable() {
                @Override
                public void run() {
                    setConnectedGUI(false);
                }
            });
            waitScreen.showError("Connection error!",
                    _attemptConnect.getException().getMessage(),
                    5);
        });

        _attemptConnect.setOnSucceeded(event -> {
            waitScreen.hide();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Common.initParkingLots(cmbParkingLots, true);
                }
            });
            setConnectedGUI(true);
        });
        waitScreen.run(_attemptConnect);
    }

    /**
     * Attempts to login.
     * Assumes connection is already established.
     * @param event The event from the click or keystroke.
     */
    @FXML
    private void attemptLogin(ActionEvent event){
        if (txtLoginUsr.getText().equals(""))
        {
            Common.showError(txtLoginUsr, "Username can not be empty!");
            return;
        }
        if (txtLoginPwd.getText().equals(""))
        {
            Common.showError(txtLoginPwd, "Password can not be empty!");
            return;
        }

        WaitScreen waitScreen = new WaitScreen();
        ParkingLot parkingLot;
        parkingLot = (ParkingLot)cmbParkingLots.getSelectionModel().getSelectedItem();
        if (parkingLot == null)
        {
            parkingLot = (ParkingLot)cmbParkingLots.getItems().get(0);
        }
        Message loginMessage = new Message(Message.MessageType.LOGIN, Message.DataType.PRIMITIVE, txtLoginUsr.getText(), txtLoginPwd.getText(), parkingLot);
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                Session session = (Session) getMessage().getData().get(0);
                CPSClientGUI.setSession(session);
                waitScreen.redirectOnClose(CPSClientGUI.CUSTOMER_SCREEN);
                waitScreen.showSuccess("Welcome " + session.getUser().getName(),"Welcome to the CPS service!", 2);
            }
        };
        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                if (getException() instanceof SocketException)
                {
                    setConnectedGUI(false);
                }
                waitScreen.showError("Login Failed!", getErrorString(), 3);
            }
        };
        MessageTasker _loginTask = new MessageTasker("Sending user details...",
                "Verifying...",
                "Successful!",
                "Verification failed!",
                loginMessage,
                onSuccess,
                onFailure);
        waitScreen.run(_loginTask, 10);
    }

    /**
     * A quick way to toggle between "Connect" and "Login" modes.
     * @param isConnected is the server currently connected.
     */
    private void setConnectedGUI(boolean isConnected)
    {
        paneLogin.setDisable(!isConnected);
        paneRegister.setDisable(!isConnected);
        paneConnection.setDisable(isConnected);
        if (isConnected)
        {
            loginRoot.setExpandedPane(paneLogin);
        }
        else
        {
            loginRoot.setExpandedPane(paneConnection);
        }
    }
}