package client.GUI.Forms;

import client.GUI.CPSClientGUI;
import client.GUI.Helpers.Common;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.RunnableWithMessage;
import entity.Message;
import entity.User;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

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
    private TitledPane paneConnection;

    @FXML
    private PasswordField txtLoginPwd;

    @FXML
    private TextField txtPort;

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
        loginRoot.setExpandedPane(paneLogin);
    }

    @FXML
    private void attemptConnect(ActionEvent globalEvent)
    {
        WaitScreen waitScreen = new WaitScreen();
        if (CPSClientGUI.getClient() != null) //If already connected
        {
            paneConnection.setDisable(true);
            attemptLogin(globalEvent);
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
                CPSClientGUI.connect(host, port);
                return null;
            }
        };
        _attemptConnect.setOnFailed(event ->
                Platform.runLater(() ->
                        waitScreen.showError("Connection error!",
                                _attemptConnect.getException().getMessage(),
                                5)));
        _attemptConnect.setOnSucceeded(event -> {
            waitScreen.hide();
            paneConnection.setDisable(true);
            attemptLogin(globalEvent);
        });
        waitScreen.run(_attemptConnect);
    }

    /**
     * Attempts to login.
     * Assumes connection is already established.
     * @param event The event from the click or keystroke.
     */
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
        Message loginMessage = new Message(Message.MessageType.LOGIN, Message.DataType.STRING, txtLoginUsr.getText(), txtLoginPwd.getText());
        RunnableWithMessage onSuccess = new RunnableWithMessage() {
            @Override
            public void run() {
                User user = (User)getIncoming().getData().get(0);
                CPSClientGUI.setCurrentUser(user);
                waitScreen.redirectOnClose(CPSClientGUI.CUSTOMER_SCREEN);
                waitScreen.showSuccess("Welcome " + CPSClientGUI.getCurrentUser().getName(),"You are being redirected to the main screen.", 3);
            }
        };
        RunnableWithMessage onFailure = new RunnableWithMessage() {
            @Override
            public void run() {
                waitScreen.showError("Login Failed!", (String)getIncoming().getData().get(0), 3);
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
}