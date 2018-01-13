package client.GUI.Forms;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.Queries;
import client.GUI.Helpers.Validation;
import entity.Customer;
import entity.Message;
import entity.ParkingLot;
import entity.Session;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;

public class LoginScreenController {

    //TODO: Figure out why you get double lists when you navigate back here.

    //TODO: Organize FXML privates by sreen

    private CPSClientGUI parentGUI;

    @FXML
    private FlowPane listCarIDs;

    @FXML
    private TextField txtRegisterEmail;

    @FXML
    private TextField txtRegisterName;

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
    private Button btnRegister;

    @FXML
    void initialize() {
        assert paneLogin != null : "fx:id=\"paneLogin\" was not injected: check your FXML file 'LoginScreen.fxml'.";
        assert loginRoot != null : "fx:id=\"loginRoot\" was not injected: check your FXML file 'LoginScreen.fxml'.";
        assert paneRegister != null : "fx:id=\"paneRegister\" was not injected: check your FXML file 'LoginScreen.fxml'.";
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
        addCarField();
    }

    private boolean validateCarTextField(TextField txtCar)
    {
        if (txtCar.getText().matches("^\\s*$")) //If new value is blank
        {
            if (listCarIDs.getChildren().indexOf(txtCar) != listCarIDs.getChildren().size() - 1) //If not the last one.
                listCarIDs.getChildren().remove(txtCar);
        }
        else if (!txtCar.getText().matches("\\d{7,8}")) { //If it's not a valid car number
            Validation.showError(txtCar, "Please enter a valid car registration number!");
            return false;
        }
        else if (listCarIDs.getChildren().indexOf(txtCar) == listCarIDs.getChildren().size() - 1) //Valid, not last, not blank.
        {
            addCarField();
        }
        Validation.removeHighlight(txtCar);
        return true;
    }

    private void addCarField()
    {
        TextField newCarID = new TextField();
        if (listCarIDs.getChildren().size() > 0)
            newCarID.setPromptText("Add another...");
        newCarID.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue)
                {
                    if (!validateCarTextField(newCarID))
                        newCarID.requestFocus();
                }
            }
        });
        newCarID.setOnAction(value -> listCarIDs.requestFocus());
        listCarIDs.getChildren().add(newCarID);
    }

    @FXML
    void attemptRegister(ActionEvent event) {
        if (!Validation.validateNotEmpty(txtRegisterName, txtRegisterEmail))
            return;

        WaitScreen waitScreen = new WaitScreen();
        ArrayList<Integer> carList = new ArrayList<>();
        for (Node carText: listCarIDs.getChildren())
        {
            if (carText instanceof TextField && !((TextField) carText).getText().isEmpty()) //TODO: Validate valid car number
            {
                carList.add(Integer.valueOf(((TextField) carText).getText()));
            }
        }
        Customer newCustomer = new Customer(-1, txtRegisterName.getText(), txtRegisterEmail.getText(), carList);

        Message newUserMessage = new Message(Message.MessageType.CREATE,
                Message.DataType.CUSTOMER,
                newCustomer);
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                Customer newCustomer = (Customer) getMessage().getData().get(0);
                waitScreen.setOnClose(new Runnable() {
                    @Override
                    public void run() {
                        attemptLogin(newCustomer.getEmail());
                    }
                });
                StringBuilder carStrings = new StringBuilder();
                ArrayList<Integer> cars = newCustomer.getCarIDList();
                int numOfCars = cars.size();
                for (int i = 0 ; i < numOfCars; i++)
                {
                    carStrings.append(cars.get(i));
                    if (i < numOfCars - 1)
                        carStrings.append(", ");
                }

                waitScreen.showSuccess("Welcome to the CPS family!",
                        "Thank you, " + newCustomer.getName() + ". You are now registered:\n"
                                + "User No. " + newCustomer.getUID() + "\n"
                        + "Email (you log in with this): " + newCustomer.getEmail() + "\n"
                        + "Password: p\n" //TODO: Decide what to do with passwords.
                + "Cars registered: " + carStrings.toString());
            }
        };
        MessageRunnable onFailed = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            };
        };

        MessageTasker taskRegister = new MessageTasker("Registering...",
                "Registering...",
                "Registration Successful!",
                "Registration failed!",
                newUserMessage, onSuccess,onFailed);
        waitScreen.run(taskRegister);
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
        if (!Validation.validateNotEmpty(txtHostname, txtPort))
            return;
        WaitScreen waitScreen = new WaitScreen();
        if (CPSClientGUI.getClient() != null && CPSClientGUI.getClient().isConnected()) //If already connected
        {
            Queries.initParkingLots(cmbParkingLots, true);
        }

        /*
        Validate Form
         */
        if (txtPort.getText().equals("") || Integer.valueOf(txtPort.getText()) < 0)
        {
            Validation.showError(txtPort, "Invalid port number!");
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
                    Queries.initParkingLots(cmbParkingLots, true);
                }
            });
            setConnectedGUI(true);
        });
        waitScreen.run(_attemptConnect);
    }

    private void attemptLogin(String username)
    {
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
     * Attempts to login.
     * Assumes connection is already established.
     * @param event The event from the click or keystroke.
     */
    @FXML
    private void attemptLogin(ActionEvent event){
       if (!Validation.validateNotEmpty(txtLoginUsr,txtLoginPwd))
           return;

       attemptLogin(txtLoginUsr.getText());
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