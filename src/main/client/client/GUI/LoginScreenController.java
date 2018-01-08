package client.GUI;

import Exceptions.LoginException;
import entity.Message;
import entity.User;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

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
    void attemptLogin(ActionEvent event) throws IOException {
        boolean connected = (CPSClientGUI.getClient() != null);
        if (!connected) //Not connected yet
        {
            if (txtHostname.getText().equals(""))
            {
                Helpers.showError(txtHostname,"Hostname can not be empty!");
                CPSClientGUI.setStatus("Hostname can not be empty!", Color.RED);
                return;
            }
            if (txtPort.getText().equals("") || Integer.valueOf(txtPort.getText()) < 0)
            {
                Helpers.showError(txtPort, "Invalid port number!");
                CPSClientGUI.setStatus("Invalid port number!", Color.RED);
                return;
            }
        }
        String host = txtHostname.getText();
        Integer port = Integer.valueOf(txtPort.getText());
        paneConnection.setDisable(connected);

        if (txtLoginUsr.getText().equals(""))
        {
            Helpers.showError(txtLoginUsr, "Username can not be empty!");
            return;
        }
        if (txtLoginPwd.getText().equals(""))
        {
            Helpers.showError(txtLoginPwd, "Password can not be empty!");
            return;
        }

        WaitScreen waitScreen = new WaitScreen();
        Task<Void> _loginTask = new Task<Void>() {
            public volatile boolean doTerminate;

            @Override
            protected Void call() throws Exception {
                if (!connected) {
                    updateMessage("Connecting...");
                    CPSClientGUI.connect(host, port);
                    paneConnection.setDisable(true);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            CPSClientGUI.setStatus("Connected to: " + CPSClientGUI.getClient().getHost(), Color.GREEN);
                        }
                    });
                }
                updateMessage("Sending user information...");
                Message loginMessage = new Message(Message.MessageType.LOGIN, Message.DataType.STRING, txtLoginUsr.getText(), txtLoginPwd.getText());
                long sid = loginMessage.getSID();
                CPSClientGUI.sendToServer(loginMessage);

                while (CPSClientGUI.getMessageQueue(sid) == null) //Wait for incoming message
                {
                    if (Thread.currentThread().isInterrupted())
                    {
                        throw new TimeoutException();
                    }
                }

                while(true){
                    Message incoming = CPSClientGUI.popMessageQueue(sid);
                    if (incoming != null)
                    {
                        switch (incoming.getType())
                        {
                            case QUEUED:
                                updateMessage("Verifying User...");
                                break;
                            case FAILED:
                                updateMessage("Verification failed!");
                                throw new LoginException((String)incoming.getData().get(0));
                            case FINISHED:
                                User user = (User)incoming.getData().get(0);
                                CPSClientGUI.setCurrentUser(user);
                                updateMessage("Successful!");
                                return null;
                            default:
                                throw new LoginException("Unexpected response type: " + incoming.getType().toString());
                        }
                    }
                }
            }
        };

        _loginTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        waitScreen.showSuccess("Welcome " + CPSClientGUI.getCurrentUser().getName(),"You are being redirected to the main screen.", 3);
                        waitScreen.setOnClose(new Runnable() {
                            @Override
                            public void run() {
                                CPSClientGUI.changeGUI("CustomerScreen.fxml");
                            }
                        });
                    }
                });
            }
        });
        _loginTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        waitScreen.showError("Login Failed!", _loginTask.getException().getMessage(), 3);
                    }
                });
            }
        });
        waitScreen.run(_loginTask, 10);
    }
}
