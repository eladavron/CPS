package client.GUI.Forms;

import Exceptions.LoginException;
import client.GUI.CPSClientGUI;
import client.GUI.Controls.CarLister;
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
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;

import static client.GUI.CPSClientGUI.CUSTOMER_SCREEN;
import static client.GUI.CPSClientGUI.EMPLOYEE_SCREEN;
import static entity.User.UserType.*;

public class LoginScreen {

    //TODO: Figure out why you get double lists when you navigate back here.

    private CPSClientGUI parentGUI;

    //region Login FXML
    @FXML
    private TextField txtLoginEmail;

    @FXML
    private PasswordField txtLoginPwd;

    @FXML
    private ComboBox<ParkingLot> cmbParkingLots;

    @FXML
    private TitledPane paneLogin;

    @FXML
    private CheckBox chkRemote;
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
    private Button btnRefreshLots;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnConnect;

    @FXML
    private TextField txtHostname;
    //endregion

    private CarLister _carLister;

    @FXML
    void initialize() {
        assert paneLogin != null : "fx:id=\"paneLogin\" was not injected: check your FXML file 'LoginScreen.fxml'.";
        assert loginRoot != null : "fx:id=\"loginRoot\" was not injected: check your FXML file 'LoginScreen.fxml'.";
        assert paneRegister != null : "fx:id=\"paneRegister\" was not injected: check your FXML file 'LoginScreen.fxml'.";
        if (CPSClientGUI.getConnectionStatus() != CPSClientGUI.ConnectionStatus.RESET)
        {
            Platform.runLater(this::attemptConnect); //This is in a "run later" so that it runs AFTER the gui is loaded.
        } else { //Connection was reset, need to reconnect.
            setConnectedGUI(false);
        }
        _carLister = new CarLister(listCarIDs);
        chkRemote.selectedProperty().bindBidirectional(cmbParkingLots.disableProperty());
        cmbParkingLots.disableProperty().addListener(observable -> Validation.removeHighlight(cmbParkingLots));
        if (CPSClientGUI.getLastConnectionIP() != null)
        {
            txtHostname.setText(CPSClientGUI.getLastConnectionIP());
        }
        txtLoginEmail.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue) //lost focus
                {
                    Validation.emailValidation(txtLoginEmail); //If not valid
                }
            }
        });
    }


    @FXML
    void attemptRegister(ActionEvent event) {
        if (!Validation.notEmpty(txtRegisterName, txtRegisterEmail) || !Validation.emailValidation(txtRegisterEmail))
            return;

        WaitScreen waitScreen = new WaitScreen();
        ArrayList<Integer> carList = _carLister.getAllNumbers();
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
                        Platform.runLater(()-> setConnectedGUI(true));
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

        MessageTasker taskRegister = new MessageTasker(newUserMessage, onSuccess,onFailed, "Registering...");
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

        if (CPSClientGUI.getClient() != null && CPSClientGUI.getClient().isConnected()) //If already connected
        {
            Inits.initParkingLots(cmbParkingLots);
        }

        if (!Validation.notEmpty(txtHostname, txtPort))
            return;

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

        WaitScreen waitScreen = new WaitScreen();
        Task<Void> _attemptConnect = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Connecting...");
                try {
                    CPSClientGUI.setLastConnectionIP(host);
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
                    Inits.initParkingLots(cmbParkingLots);
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
        if (chkRemote.isSelected())
        {
            parkingLot = new ParkingLot();
            parkingLot.setParkingLotID(-1);
            parkingLot.setLocation("Remote Login");
        }
        else
        {
            parkingLot = (ParkingLot)cmbParkingLots.getSelectionModel().getSelectedItem();
        }
        Message loginMessage = new Message(Message.MessageType.LOGIN, Message.DataType.PRIMITIVE, email, password, parkingLot);
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                Session session = (Session) getMessage().getData().get(0);
                CPSClientGUI.setSession(session);
                /*
                Check if Customer or Employee
                 */
                String redirect;
                switch (session.getUserType())
                {
                    case CUSTOMER:
                        redirect = CUSTOMER_SCREEN;
                        break;
                    case EMPLOYEE:
                        if (session.getParkingLot().getParkingLotManagerID().equals(session.getUser().getUID())) //Employee is the manager of this lot!
                        {
                            session.getUser().setUserType(MANAGER);
                        }
                        if (session.getUser().getEmail().equals("cs@cps.com"))
                        {
                            session.getUser().setUserType(CUSTOMER_SERVICE);
                        }
                        if (session.getUser().getUID() == 0)
                        {
                            session.getUser().setUserType(SUPERMAN);
                        }
                        redirect = EMPLOYEE_SCREEN;
                        break;
                    default:
                        throw new LoginException("Unknown user type " + session.getUserType());
                }
                waitScreen.redirectOnClose(redirect);
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
        MessageTasker _loginTask = new MessageTasker(loginMessage, onSuccess, onFailure, "Logging in...");
        waitScreen.run(_loginTask, 10);
    }

    @FXML
    private void refreshLots(ActionEvent event)
    {
        Inits.initParkingLots(cmbParkingLots);
    }

    /**
     * Attempts to login.
     * Assumes connection is already established.
     * @param event The event from the click or keystroke.
     */
    @FXML
    private void attemptLogin(ActionEvent event){
        if (!Validation.notEmpty(txtLoginEmail,txtLoginPwd, cmbParkingLots) || !Validation.emailValidation(txtLoginEmail))
            return;

        attemptLogin(txtLoginEmail.getText(), txtLoginPwd.getText());
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