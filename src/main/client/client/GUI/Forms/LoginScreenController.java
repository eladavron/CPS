package client.GUI.Forms;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.Inits;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
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

    private CPSClientGUI parentGUI;

    //region Login FXML
    @FXML
    private TextField txtLoginUsr;

    @FXML
    private PasswordField txtLoginPwd;

    @FXML
    private ComboBox<ParkingLot> cmbParkingLots;

    @FXML
    private TitledPane paneLogin;
    //endregion

    //region Registration FXML
    @FXML
    private PasswordField txtRegisterPwd;

    @FXML
    private Button btnRegister;

    @FXML
    private FlowPane listCarIDs;

    @FXML
    private TextField txtRegisterEmail;

    @FXML
    private TitledPane paneRegister;

    @FXML
    private TextField txtRegisterName;
    //endregion

    //region Connection FXML
    @FXML
    private Accordion loginRoot;

    @FXML
    private TitledPane paneConnection;


    @FXML
    private TextField txtPort;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnConnect;

    @FXML
    private TextField txtHostname;
    //endregion


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
                    attemptConnect(); //This is in a "run later" so that it runs AFTER the gui is loaded.
                }
            });
        } else { //Connection was reset, need to reconnect.
            setConnectedGUI(false);
        }
        addCarField();
    }

    /**
     * Validates a car textfield.
     * If valid, asks to add another to the form.
     * If empty, deletes the field (unless it's the last one, in which case it just empties it).
     * @param txtCar TextField to validate.
     * @return True if everything is valid, false otherwise.
     */
    private boolean validateCarTextField(TextField txtCar)
    {
        if (txtCar.getText().matches("^\\s*$")) //If new value is blank
        {
            if (listCarIDs.getChildren().indexOf(txtCar) != listCarIDs.getChildren().size() - 1) //If not the last one.
                listCarIDs.getChildren().remove(txtCar);
        }
        else if (!Validation.carNumber(txtCar.getText())) { //If it's not a valid car number
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

    /**
     * Adds an "Enter Car" textfield to the form.
     * Also called upon when a car number is entered to allow adding others.
     */
    private void addCarField()
    {
        TextField newCarID = new TextField();
        if (listCarIDs.getChildren().size() > 0) //If this is not the first field.
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
        if (!Validation.notEmpty(txtRegisterName, txtRegisterEmail))
            return;

        WaitScreen waitScreen = new WaitScreen();
        ArrayList<Integer> carList = new ArrayList<>();
        for (Node carText: listCarIDs.getChildren())
        {
            if (carText instanceof TextField && !((TextField) carText).getText().isEmpty() && Validation.carNumber(((TextField) carText).getText()))
            {
                carList.add(Integer.valueOf(((TextField) carText).getText()));
            }
        }
        Customer newCustomer = new Customer(-1, txtRegisterName.getText(), txtRegisterPwd.getText(), txtRegisterEmail.getText(), carList);

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
                        attemptLogin(newCustomer.getEmail(), newCustomer.getPassword());
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
                                + "Password: " + newCustomer.getPassword() + "\n"
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
        if (!Validation.notEmpty(txtHostname, txtPort))
            return;
        WaitScreen waitScreen = new WaitScreen();
        if (CPSClientGUI.getClient() != null && CPSClientGUI.getClient().isConnected()) //If already connected
        {
            Inits.initParkingLots(cmbParkingLots, true);
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
                    Inits.initParkingLots(cmbParkingLots, true);
                }
            });
            setConnectedGUI(true);
        });
        waitScreen.run(_attemptConnect);
    }

    private void attemptLogin(String email, String password)
    {
        WaitScreen waitScreen = new WaitScreen();
        ParkingLot parkingLot;
        parkingLot = (ParkingLot)cmbParkingLots.getSelectionModel().getSelectedItem();
        if (parkingLot == null)
        {
            parkingLot = (ParkingLot)cmbParkingLots.getItems().get(0);
        }
        Message loginMessage = new Message(Message.MessageType.LOGIN, Message.DataType.PRIMITIVE, email, password, parkingLot);
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
        if (!Validation.notEmpty(txtLoginUsr,txtLoginPwd))
            return;

        attemptLogin(txtLoginUsr.getText(), txtLoginPwd.getText());
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