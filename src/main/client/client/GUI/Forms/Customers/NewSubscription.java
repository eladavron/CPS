package client.GUI.Forms.Customers;

import Exceptions.NotImplementedException;
import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.Inits;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.Validation;
import controller.CustomerController.SubscriptionOperationReturnCodes;
import entity.*;
import entity.Subscription.SubscriptionType;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import utils.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class NewSubscription implements Initializable{

    @FXML
    private FlowPane flowCarSelection;

    @FXML
    private Button btnBack;


    @FXML
    private ComboBox<String> cmbExitMinute;

    /**
     * This control is populated in load time by the FXML.
     */

    @FXML
    private ComboBox<String> cmbSubType;


    @FXML
    private ComboBox<Integer> cmbSingleCar;

    @FXML
    private FlowPane flowCarMultiple;

    @FXML
    private FlowPane flowEstimatedExit;

    @FXML
    private FlowPane flowCarSingle;

    @FXML
    private HBox timeExit;

    @FXML
    private VBox contentArea;

    @FXML
    private ComboBox<String> cmbExitHour;

    @FXML
    private Button btnSend;

    @FXML
    private ComboBox<ParkingLot> cmbParkingLot;

    @FXML
    private AnchorPane paneParkingLot;

    @FXML
    private FlowPane flowParkingLot;

    private static final int REGULAR = 0;
    private static final int REGULAR_MULTICAR = 1;
    private static final int FULL = 2;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()->{
            Inits.initCars(cmbSingleCar);
            Inits.initParkingLots(cmbParkingLot);
            Inits.initTimeSelection(cmbExitHour,cmbExitMinute);
        });
        for (Integer carNum : cmbSingleCar.getItems())
        {
            CheckBox newCar = new CheckBox();
            newCar.setText(carNum.toString());
            flowCarSelection.getChildren().add(newCar);
        }
        contentArea.getChildren().clear();
        paneParkingLot.getChildren().clear();
        cmbSubType.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.equals(oldValue))
                    return;

                if (newValue.equals(REGULAR_MULTICAR))
                {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(flowCarMultiple);
                }
                else
                {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(flowCarSingle);
                }
                if (newValue.equals(FULL))
                {
                    paneParkingLot.getChildren().clear();
                }
                else
                {
                    if (paneParkingLot.getChildren().isEmpty())
                        paneParkingLot.getChildren().add(flowParkingLot);
                }
                flowEstimatedExit.setVisible(!newValue.equals(FULL));
            }
        });
    }

    @FXML
    void attemptNew(ActionEvent event) {
        attemptNewSubscription();
    }

    private void attemptNewSubscription() {
        if (!validateForm())
            return;

        WaitScreen waitScreen = new WaitScreen();
        Message newSubMessage = new Message(Message.MessageType.CREATE, Message.DataType.SUBSCRIPTION);
        Subscription newSubscription;
        Integer userID = CPSClientGUI.getLoggedInUserID();
        switch (cmbSubType.getSelectionModel().getSelectedIndex()) {
            case REGULAR:
                newSubMessage.addData(SubscriptionType.REGULAR);
                newSubscription = new RegularSubscription(userID, cmbSingleCar.getValue(),
                        cmbExitHour.getValue() + ":" + cmbExitMinute.getValue(),
                        cmbParkingLot.getValue().getParkingLotID());
                break;
            case REGULAR_MULTICAR:
                newSubMessage.addData(SubscriptionType.REGULAR_MULTIPLE);
                newSubscription = new RegularSubscription(userID, cmbSingleCar.getValue(),
                        cmbExitHour.getValue() + ":" + cmbExitMinute.getValue(),
                        cmbParkingLot.getValue().getParkingLotID());
                break;
            case FULL:
                newSubMessage.addData(SubscriptionType.FULL);
                newSubscription = new FullSubscription(userID, cmbSingleCar.getValue());
                break;
            default:
                throw new NotImplementedException("Unexpected subscription type: " + cmbSubType.getValue());
        }
        newSubMessage.addData(newSubscription);
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {

                SubscriptionOperationReturnCodes returnCodes = (SubscriptionOperationReturnCodes) getMessage().getData().get(0);
                Subscription newSub = (Subscription) getMessage().getData().get(1);
                String subName = StringUtils.SubscriptionTypeName(newSub.getSubscriptionType());
                String message = returnCodes.equals(SubscriptionOperationReturnCodes.SUCCESS_ADDED) ?
                        String.format("You now have a new %s subscription!", subName)
                        : String.format("Your %s subscription has been renewed!", subName);

                message += "\nSubscription ID: " + newSub.getSubscriptionID() + "\n"
                        + "For car: " + newSub.getCarID() + "\n"
                        + "Now Expires on: " + newSub.getExpiration();
                waitScreen.setGoBackOnClose(true);
                waitScreen.showSuccess("Congratulations!", message);
            };
        };

        MessageRunnable onFailed = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.setGoBackOnClose(false);
                waitScreen.showError("Something went wrong...", getErrorString());
            }
        };

        MessageTasker taskCreation = new MessageTasker("Requestion Subscription...",
                "Waiting for response...",
                "Subscription successful!",
                "Subscription Failed!",
                newSubMessage, onSuccess, onFailed);
        waitScreen.run(taskCreation);
    }

    private boolean validateForm()
    {
        if (!Validation.notEmpty(cmbSubType))
            return false;

        boolean validate = true;
        switch (cmbSubType.getSelectionModel().getSelectedIndex())
        {
            case FULL:
                return Validation.notEmpty(cmbSingleCar);
            case REGULAR:
                return Validation.notEmpty(cmbSingleCar, cmbParkingLot);
            case REGULAR_MULTICAR:
                boolean anyChecked = false;
                validate = Validation.notEmpty(cmbSingleCar, cmbParkingLot);
                for (Node control : flowCarSelection.getChildren())
                {
                    if (control instanceof CheckBox && ((CheckBox) control).isSelected())
                        anyChecked = true;
                }
                if (!anyChecked)
                {
                    Validation.showError(flowCarSelection, "You must select at least one car!");
                }
                return anyChecked && validate;
            default:
                return false;
        }
    }

    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(true);
    }


}
