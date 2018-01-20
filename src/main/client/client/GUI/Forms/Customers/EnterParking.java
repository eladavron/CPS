package client.GUI.Forms.Customers;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.DateTimeCombo;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.*;
import entity.Message;
import entity.Order;
import entity.PreOrder;
import entity.Subscription;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * The controller for entering a parking session.
 */
public class EnterParking extends GUIController implements Initializable, Refreshable {

    @FXML
    private Button btnBack;

    @FXML
    private ComboBox<PreOrder> cmbOrder;

    @FXML
    private ComboBox<String> cmbExitMinute;

    @FXML
    private Button btnCreate;

    @FXML
    private ComboBox<Subscription> cmbSubscription;

    @FXML
    private Hyperlink linkManageSubs;


    @FXML
    private Button btnReset;


    @FXML
    private VBox paneOrderDetails;


    @FXML
    private ComboBox<String> cmbExitHour;

    @FXML
    private DatePicker exitDate;

    @FXML
    private ComboBox<Integer> cmbCar;

    @FXML
    private FlowPane flowSubscription;

    private DateTimeCombo _exitDateTime;

    private ObservableList<Subscription> _subList = FXCollections.observableArrayList();

    /**
     * Inits all the elements. Binds the ComboBoxes and Reset buttons together.
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setTooltip(new Tooltip("Back"));
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Inits.initCars(cmbCar);
                Inits.initPreorders(cmbOrder);
                Inits.initSubscriptions(cmbSubscription);
            }
        });
        cmbOrder.valueProperty().addListener((observable, oldValue, newValue) -> fillOrder());
        btnReset.disableProperty().bind(cmbOrder.valueProperty().isNull());
        paneOrderDetails.disableProperty().bind(cmbOrder.valueProperty().isNotNull());
        _exitDateTime = new DateTimeCombo(exitDate, cmbExitHour, cmbExitMinute);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh()
    {
        Inits.initCars(cmbCar);
        Inits.initPreorders(cmbOrder);
        Inits.initSubscriptions(cmbSubscription);
    }

    /**
     * Validates the form. Highlights any problematic fields and displays an error message if fails.
     * @return true if form is valid, false otherwise.
     */
    private boolean validateForm() {
        Validation.clearAllHighlighted();
        boolean validate = Validation.notEmpty(cmbCar);
        if (cmbSubscription.getValue() != null && !cmbSubscription.getValue().getCarsID().contains(cmbCar.getValue())) //If the car isn't in the subscription.
        {
            Validation.showError(cmbCar, "This car is not registered the subscription you have selected!");
            validate = false;
        }
        return Validation.validateTimes(null, _exitDateTime) && validate;
    }

    /**
     * A new parking session is created by creating an "Order".
     * @param event the action of clicking the "Enter Parking" button.
     */
    @FXML
    void enterParking(ActionEvent event) {
        if (!validateForm())
            return;

        WaitScreen waitScreen = new WaitScreen();
        Date exitTime = _exitDateTime.getDateTime();
        Integer parkingLotNumber = CPSClientGUI.getSession().getParkingLot().getParkingLotID();
        Order newOrder = new Order(CPSClientGUI.getLoggedInUserID(), cmbCar.getValue(),exitTime,parkingLotNumber);
        if (cmbOrder.getValue() != null)
        {
            newOrder.setOrderID(cmbOrder.getValue().getOrderID());
            newOrder.setCarID(cmbOrder.getValue().getCarID());
        }
        else
        {
            newOrder.setOrderID(0);
            newOrder.setCarID(cmbCar.getValue());
        }
        newOrder.setOrderStatus(Order.OrderStatus.IN_PROGRESS);
        Message newMessage = new Message(Message.MessageType.CREATE, Message.DataType.ORDER, newOrder);

        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                Order order = (Order) getMessage().getData().get(0);
                waitScreen.showSuccess("Car Parked!", order.toGUIString());
                waitScreen.redirectOnClose(CPSClientGUI.CUSTOMER_SCREEN, null);
            }
        };
        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showError("Parking Failed!", getErrorString());
            }
        };
        MessageTasker createOrder = new MessageTasker(newMessage, onSuccess, onFailure, "Attempting to park...");
        waitScreen.run(createOrder, 10);
    }

    /**
     * Fills the order once selected in the drop down menu
     */
    private void fillOrder() {
        PreOrder selectedOrder = cmbOrder.getValue();
        cmbCar.getSelectionModel().select(selectedOrder.getCarID());
        _exitDateTime.setDateTime(selectedOrder.getEstimatedExitTime());
        validateForm();
    }


    /**
     * Resets the form.
     * @param event The mouse click event.
     */
    @FXML
    void resetForm(ActionEvent event) {
        cmbOrder.getSelectionModel().clearSelection();
        cmbCar.getSelectionModel().clearSelection();
        exitDate.setValue(null);
    }

    /**
     * Refresh the list of Preorders.
     * This is needed in case the user cancels the part where we try to query the server for preorders..
     * @param event The click event.
     */
    @FXML
    private void refreshOrders(ActionEvent event)
    {
        Inits.initPreorders(cmbOrder);
    }


    /**
     * Goes to the {@link ManageCars} screen.
     * @param event The Click event.
     */
    @FXML
    void manageCars(ActionEvent event) {
        CPSClientGUI.changeGUI(CPSClientGUI.MANAGE_CARS, this);
    }

    /**
     * Goes to the {@link ManageSubscriptions} screen.
     * @param event Handles the Click event.
     */
    @FXML
    void manageSubs(ActionEvent event) {
        CPSClientGUI.changeGUI(CPSClientGUI.MANAGE_SUBSCRIPTIONS, this);
    }

    /**
     * Handles the "back" button click. (Name is remnant of an older GUI scheme.)
     * @param event The Click event
     * @throws IOException In case the loading fails.
     */
    @FXML
    void returnToMain(ActionEvent event) throws IOException {
        CPSClientGUI.goBack(true);
    }
}
